package genj.edit;

import genj.gedcom.Change;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.IconValueAvailable;
import genj.gedcom.MetaProperty;
import genj.gedcom.MultiLineSupport;
import genj.gedcom.Property;
import genj.util.swing.HeadlessLabel;
import genj.util.swing.TreeWidget;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.text.View;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * A Property Tree
 */
public class PropertyTreeWidget extends TreeWidget {
  
  /** the model */
  private Model model;
    
  /**
   * Constructor
   */
  public PropertyTreeWidget(Gedcom gedcom) {
    
    // setup model
    model = new Model(gedcom);
    setModel(model);
    
    // setup callbacks
    setCellRenderer(new Renderer());

    // done
  }
  
  /**
   * @see javax.swing.JComponent#addNotify()
   */
  public void addNotify() {
    // continue
    super.addNotify();
    // ready for tooltips
    ToolTipManager.sharedInstance().registerComponent(this);
  }
    
  /**
   * @see javax.swing.JComponent#removeNotify()
   */
  public void removeNotify() {
    // stop tooltips
    ToolTipManager.sharedInstance().unregisterComponent(this);
    // continue
    super.removeNotify();
  }
    
  /**
   * Prepare the tree-model that lies behind the tree.
   */
  public void setEntity(Entity entity) {
    model.setEntity(entity);
    expandRows();
  }
  
  /**
   * Set the current property
   */
  public void setProperty(Property property) {
    // set entity first
    Entity e = property.getEntity();
    setEntity(e);
    // select and show property
    TreePath path = new TreePath(e.getPathTo(property));
    scrollPathToVisible(path);
    setSelectionPath(path);
  }
  
  /**
   * Access to entity
   */
  public Entity getEntity() {
    return model.getEntity();
  }
  
  /**
   * Allows to return to previous entity
   */
  public void setPreviousEntity() {
    model.setPrevious();
    expandRows();

    // select and show property
    Entity entity = model.getEntity();
    if (entity!=null) {
      TreePath path = new TreePath(entity);
      scrollPathToVisible(path);
      setSelectionPath(path);
    }
  }
  
  /**
   * returns the currently selected property
   */
  public Property getCurrentProperty() {
    // Calculate selection path
    TreePath path = getSelectionPath();
    if (path==null) return null;
    // got it
    if (path.getLastPathComponent() instanceof Property)
      return (Property)path.getLastPathComponent();
    // none found
    return null;
  }
  
  /**
   * Resolve property for location
   */
  public Property getProperty(int x, int y) {
    // calc path to node under mouse
    TreePath path = super.getPathForLocation(x, y);
    if ((path==null) || (path.getPathCount()==0)) 
      return null;
    // done
    return (Property)path.getLastPathComponent();
  }
  
  /**
   * the selected properties
   */
  public Property[] getSelection() {
    
    // check selection
    TreePath paths[] = getSelectionPaths();
    if ( (paths==null) || (paths.length==0) ) {
      return new Property[0];
    }

    // .. remove every selected node
    Property[] result = new Property[paths.length];
    for (int i=0;i<paths.length;i++) {
      result[i] = (Property)paths[i].getLastPathComponent();
    }

    // done
    return result;    
  }
  
  /**
   * @see javax.swing.JTree#getToolTipText(MouseEvent)
   */
  public String getToolTipText(MouseEvent event) {
    // lookup property
    Property prop = getProperty(event.getX(),event.getY());
    if (prop==null) return null;
    // .. transient?
    if (prop.isTransient()) return null;
    // .. calc information text
    String info = MetaProperty.get(prop).getInfo();
    if (info==null) return null;
    // return text wrapped to 200 pixels
    return "<html><table width=200><tr><td>"+info+"</td></tr></table></html";    
  }
  
  /**
   * Our model
   */
  private class Model implements TreeModel, GedcomListener {

    private Object DUMMY = new Object();

    /** listeners */
    private List listeners = new ArrayList();
  
    /** root of tree */
    private Entity root = null;

    /** history stack */
    private Stack history = new Stack();
  
    /** the gedcom we're looking at */
    private Gedcom gedcom;
    
    /** cached object per property */
    private Map property2cachedValue =  new HashMap();

    /**
     * Constructor
     */
    public Model(Gedcom gedcom) {
      this.gedcom=gedcom;
      setEntity(null);
    }          
  
    /**
     * Set the root
     */
    public void setEntity(Entity entity) {
      // remember history
      if (root!=null) {
        history.push(root);
        if (history.size()>16) history.removeElementAt(0);
      }
      // remember
      root = entity;
      // notify
      fireStructureChanged();
      // make sure we don't show null-root
      setRootVisible(root!=null);
    }
  
    /**
     * Sets the root to the previous one
     */
    public void setPrevious() {
      // is there one?
      if (history.isEmpty()) return;
      // don't want current to end up on the stack
      root = null;
      // set it
      setEntity((Entity)history.pop());
      // done
    }
  
    /**
     * Adds a listener to this model
     */
    public void addTreeModelListener(TreeModelListener l) {
      // first?
      if (listeners.isEmpty()) gedcom.addListener(this);
      // add
      listeners.add(l);
    }          
  
    /**
     * Removes a Listener from this model
     */
    public void removeTreeModelListener(TreeModelListener l) {
      // remove
      listeners.remove(l);
      // last?
      if (listeners.isEmpty()) gedcom.removeListener(this);
    }          
  
    /**
     * Signals to listeners that properties have changed
     */
    public void firePropertiesChanged(List props) {

      // update cache of htmls
      for (int i=props.size()-1;i>=0;i--) {
        property2cachedValue.remove(props.get(i));
      }

      // Do it for all changed properties
      Iterator e = props.iterator();
      while (e.hasNext()) {
  
        // .. use property
        Property prop = (Property)e.next();
  
        // .. build event
        Object path[] = root.getPathTo(prop);
        if (path==null)
          continue;
  
        TreeModelEvent ev = new TreeModelEvent(this,path);
  
        // .. tell it to all listeners
        Iterator elisteners = listeners.iterator();
        while (elisteners.hasNext()) {
          ((TreeModelListener)elisteners.next()).treeNodesChanged(ev);
        }
  
        // .. next changed property
      }
    }          
  
    /**
     * Signals to listeners that structure has changed
     */
    public void fireStructureChanged() {

      // clear cache of htmls
      property2cachedValue.clear();

      // propagate even
      Object[] path = new Object[]{ root!=null ? (Object)root : ""};
      TreeModelEvent ev = new TreeModelEvent(this,path);

      // .. tell it to all listeners
      Iterator elisteners = listeners.iterator();
      while (elisteners.hasNext()) {
        ((TreeModelListener)elisteners.next()).treeStructureChanged(ev);
      }
    }          
  
    /**
     * Returns child by index of parent
     */
    public Object getChild(Object parent, int index) {
      Property prop = (Property)parent;
      return prop.getProperty(index);
    }          
  
    /**
     * Returns child count of parent
     */
    public int getChildCount(Object parent) {
      Property prop = (Property)parent;
      return prop.getNoOfProperties();
    }          
  
    /**
     * Returns index of given child from parent
     */
    public int getIndexOfChild(Object parent, Object child) {
      // Calculate index by fiven parent property
      return ((Property)parent).getIndexOf((Property)child);
    }          
  
    /**
     * Returns root of tree
     */
    public Object getRoot() {
      return root!=null?root:DUMMY;
    }          
  
    /** 
     * Returns root of tree
     */
    public Entity getEntity() {
      if (root==null) return null;
      return root.getEntity();
    }

    /**
     * Get cached value for given property
     */    
    private Object getCachedValue(Property prop) {
      return property2cachedValue.get(prop);
    }
    
    /**
     * Set cached value for given property
     */    
    private void setCachedValue(Property prop, Object value) {
      property2cachedValue.put(prop, value);
    }
  
    /**
     * Tells wether object is a leaf
     */
    public boolean isLeaf(Object node) {
      // since the root might be Object to 
      // keep pre jdk 1.4 running we check type here
      if (node==DUMMY) return true;
      // check property
      Property prop = (Property)node;
      return prop.getNoOfProperties()==0;
    }          
  
    /**
     * Changes a object at given path (not used here)
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
    } 
  
    /**
     * @see genj.gedcom.GedcomListener#handleChange(Change)
     */
    public void handleChange(Change change) {

      // Could we be affected at all?
      if (root==null) return;

      // Entity deleted ?
      if ( change.isChanged(Change.EDEL) ) {
        // Loop through known entity ?
        boolean affected = false;
        Iterator ents = change.getEntities(Change.EDEL).iterator();
        while (ents.hasNext()) {
          // the entity deleted
          Entity entity = (Entity)ents.next();
          // ... a removed entity has to be removed from stack
          while (history.removeElement(entity)) {};
          // ... and might affect the current edit view
          affected |= (entity==root);
        }
        // Is this a show stopper at this point?
        if (affected==true) {
          setEntity(null);
          return;
        }
        // continue
      }

      // Property added/removed ?
      if ( change.isChanged(Change.PADD)||change.isChanged(Change.PDEL)) {
        // reset
        fireStructureChanged();
        // show rows
        expandRows();
        // done
        return;
      }

      // Property modified ?
      if ( change.isChanged(change.PMOD) ) {
        if ( change.getEntities(Change.EMOD).contains(root.getEntity())) {
          firePropertiesChanged(change.getProperties(Change.PMOD));
          return;
        }
      }

      // Done
    }
  
  } //Model

  /**
   * Our renderer
   */
  private class Renderer extends HeadlessLabel implements TreeCellRenderer {
    
    /** a default we keep around for colors */
    private DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

    /**
     * Constructor
     */
    private Renderer() {
      setOpaque(true);
      setFont(PropertyTreeWidget.this.getFont());
      
      // 20031518 will have to choose color here
      setForeground(defaultRenderer.getTextNonSelectionColor());

    }
    
    /**
     * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object object, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

      // no property no luck      
      if (!(object instanceof Property))
        return this;
      Property prop = (Property)object;

      // prepare color
      if (sel) {
        setBackground(defaultRenderer.getBackgroundSelectionColor());
      } else {
        setBackground(defaultRenderer.getBackgroundNonSelectionColor());
      }

      // calc image        
      setIcon(prop.getImage(true));
      
      // calc text view
      View view = (View)model.getCachedValue(prop);
      if (view==null) {

        // create html text
        StringBuffer html = new StringBuffer();
      
        html.append("<html>");
      
        if (prop instanceof Entity) {
          html.append("@").append(((Entity)prop).getId()).append("@ ");
          html.append("<b>").append(prop.getTag()).append("</b>");
        } else {
          if (!prop.isTransient())
            html.append(" <b>").append(prop.getTag()).append("</b> ");
          
          if (prop instanceof MultiLineSupport && !(prop instanceof IconValueAvailable)) {
          
            char[] chars = ((MultiLineSupport)prop).getLinesValue().toCharArray();
            for (int i=0; i<chars.length; i++) {
              char c = chars[i];
              if (c=='\n') html.append("<br>");
              else html.append(c);
            }
          
          } else {
            html.append(prop.getValue());
          }
        }

        html.append("</html>");

        // convert
        view = setHTML(html.toString());
        
        // remember
        model.setCachedValue(prop, view);
      
        // done
      }
      setView(view);

      // done
      return this;
    }
    
  } //Renderer
    
} //PropertyTree
