package genj.edit;

import genj.gedcom.Change;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.util.swing.TreeWidget;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
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
    ToolTipManager.sharedInstance().registerComponent(this);
    
    // done
  }
  
  /**
   * Prepare the tree-model that lies behind the tree.
   */
  public void setEntity(Entity entity) {
    model.setEntity(entity);
    expandRows();
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
    if (info==null) return "?";
    // .. return max 60
    info = info.replace('\n',' ');
    if (info.length()<=60)
      return info;
    return info.substring(0,60)+"...";
  }
  
  /**
   * Our model
   */
  public class Model implements TreeModel, GedcomListener {

    private Object DUMMY = new Object();

    /** listeners */
    private List listeners = new ArrayList();
  
    /** root of tree */
    private Entity root = null;

    /** history stack */
    private Stack history = new Stack();
  
    /** the gedcom we're looking at */
    private Gedcom gedcom;

    /**
     * Constructor
     */
    public Model(Gedcom gedcom) {
      this.gedcom=gedcom;
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
      // change
      root = entity;
      fireStructureChanged();
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
      int index = ((Property)parent).getIndexOf((Property)child);
  
      // This is zero-based
      return index-1;
    
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
          root=null;
          fireStructureChanged();
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
  private class Renderer extends DefaultTreeCellRenderer {
    
    /** original font */
    private Font font;
    
    /**
     * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      
      // delegate to super
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

      // do our own      
      if (value instanceof Property) {
        Property prop = (Property)value;

        // Set the text & image
        String tag = prop.getTag();
        if (prop instanceof Entity)
          tag = "@"+((Entity)prop).getId()+"@ "+tag;
    
        setText(tag+' '+prop.getValue());
        setIcon(prop.getImage(true));

        // font
        if (font==null) font = getFont();
        setFont(prop.isTransient() ? font.deriveFont(Font.ITALIC) : font);
      }      

      // done
      return this;
    }

  } //Renderer
    
} //PropertyTree
