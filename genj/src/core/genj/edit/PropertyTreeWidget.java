/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.edit;

import genj.gedcom.Change;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.IconValueAvailable;
import genj.gedcom.MetaProperty;
import genj.gedcom.MultiLineSupport;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.util.swing.HeadlessLabel;
import genj.util.swing.ImageIcon;
import genj.util.swing.TreeWidget;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
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
import javax.swing.tree.TreeSelectionModel;

/**
 * A Property Tree
 */
public class PropertyTreeWidget extends TreeWidget {
  
  /** the model */
  private Model model;

  /**
   * Constructor
   */
  public PropertyTreeWidget(Property setRoot) {
    this(setRoot.getGedcom());
    setRoot(setRoot);
    setExpandsSelectedPaths(true);
  }
    
  /**
   * Constructor
   */
  public PropertyTreeWidget(Gedcom gedcom) {
    
    // setup model
    model = new Model(gedcom);
    setModel(model);
    
    // setup callbacks
    setCellRenderer(new Renderer());
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    
    // done
  }
  
  /**
   * @see javax.swing.JTree#getPreferredScrollableViewportSize()
   */
  public Dimension getPreferredScrollableViewportSize() {
    return new Dimension(256,128);
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
   * Set the current root
   */
  public void setRoot(Property property) {
    // change?
    if (model.getRoot()==property)
      return;
    // propagate to model
    model.setRoot(property);
    // show
    expandRows();
    // select and show property
    setSelection(property);
  }
  
  /**
   * The current root
   */
  public Property getRoot() {
    return model.root;
  }
  
  /**
   * Selects a property (eventually switching root)
   */
  public void setSelection(Property select) {
    // safety check
    if (model.root==null||select==null)
      return;
    // get path
    Property[] path = model.root.getPathTo(select);
    if (path.length==0)
      return;
    // show and select
    TreePath tpath = new TreePath(path);
    scrollPathToVisible(tpath);
    setSelectionPath(tpath);
    // done
  }
  
  /**
   * Allows to return to previous entity
   */
  public void setPrevious() {
    // tell model to go to previous
    if (!model.setPrevious()) return;
    expandRows();
    // select and show property
    setSelection(model.root);
  }
  
  /**
   * returns the currently selected property
   */
  public Property getSelection() {
    // Calculate selection path
    TreePath path = getSelectionPath();
    if (path==null) 
      return null;
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
   * @see javax.swing.JTree#getToolTipText(MouseEvent)
   */
  public String getToolTipText(MouseEvent event) {
    // lookup property
    Property prop = getProperty(event.getX(),event.getY());
    if (prop==null) return null;
    // .. transient?
    if (prop.isTransient()) return null;
    // .. won't work if property is not part of entity (e.g. Cliboard.Copy)
    if (prop.getEntity()==null) return null;
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
    private Property root = null;

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
    }          
  
    /**
     * Set the root
     */
    public void setRoot(Property set) {
      // remember history
      if (root!=null) {
        history.push(root);
        if (history.size()>16) history.removeElementAt(0);
      }
      // remember
      root = set;
      // notify
      fireStructureChanged();
      // make sure we don't show null-root
      setRootVisible(root!=null);
    }
  
    /**
     * Sets the root to the previous one
     */
    public boolean setPrevious() {
      // is there one?
      if (history.isEmpty()) return false;
      // don't want current to end up on the stack
      root = null;
      // set it
      setRoot((Property)history.pop());
      // done
      return true;
    }
  
    /**
     * Adds a listener to this model
     */
    public void addTreeModelListener(TreeModelListener l) {
      // first?
      if (listeners.isEmpty()&&gedcom!=null) gedcom.addListener(this);
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
      if (listeners.isEmpty()&&gedcom!=null) gedcom.removeListener(this);
    }          
  
    /**
     * Signals to listeners that properties have changed
     */
    public void firePropertiesChanged(Set props) {

      // Do it for all changed properties
      for (Iterator it=props.iterator();it.hasNext();) {

        // .. use property
        Property prop = (Property)it.next();
  
        // .. forget cached value for prop
        property2cachedValue.remove(prop);
  
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
      Property[] children = prop.getProperties(prop.QUERY_SYSTEM_FALSE);
      return children[index];
    }          
  
    /**
     * Returns child count of parent
     */
    public int getChildCount(Object parent) {
      Property prop = (Property)parent;
      return prop.getProperties(prop.QUERY_SYSTEM_FALSE).length;
    }
    
    /**
     * Returns index of given child from parent
     */
    public int getIndexOfChild(Object parent, Object child) {
      Property prop = (Property)parent;
      Property[] children = prop.getProperties(prop.QUERY_SYSTEM_FALSE);
      for (int i=0;i<children.length;i++) {
        if (children[i]==child) return i; 
      } 
      return -1;
    }          
  
    /**
     * Returns root of tree
     */
    public Object getRoot() {
      return root!=null?root:DUMMY;
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
      if (root==null) 
        return;
        
      // our entity
      Entity entity = root.getEntity();

      // Entity deleted ?
      if ( !change.getChanges(Change.EDEL).isEmpty() ) {
        // Loop through known entity ?
        boolean affected = false;
        Iterator ents = change.getChanges(Change.EDEL).iterator();
        while (ents.hasNext()) {
          // the entity deleted
          Entity deleted = (Entity)ents.next();
          // ... a removed entity has to be removed from stack
          for (ListIterator it = history.listIterator(); it.hasNext(); ) {
            Property p = (Property)it.next();
            if (p.getEntity()==deleted) it.remove();
          } 
          // ... and might affect the current edit view
          affected |= (entity==deleted);
        }
        // Is this a show stopper at this point?
        if (affected==true) {
          root = null;
          setRoot(null);
          return;
        }
        // continue
      }
      
      // at least same entity modified?
      if (!change.getChanges(change.EMOD).contains(entity))
        return;

      // Property added/removed ?
      Iterator padds = change.getChanges(Change.PADD).iterator();
      while (padds.hasNext()) {
        Property padd = (Property)padds.next();
        if (!padd.isSystem()) {
          // reset
          fireStructureChanged();
          // show rows
          expandRows();
          // done
          return;
        }
      }
      Iterator pdels = change.getChanges(Change.PDEL).iterator();
      while (pdels.hasNext()) {
        Property pdel = (Property)pdels.next();
        if (!pdel.isSystem()) {
          // reset
          fireStructureChanged();
          // show rows
          expandRows();
          // done
          return;
        }
      }

      // A simple property modified?
      if ( !change.getChanges(change.PMOD).isEmpty() ) {
        firePropertiesChanged(change.getChanges(Change.PMOD));
        return;
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
      ImageIcon img = prop.getImage(true);
      if (prop.isPrivate()) 
        img = img.getOverLayed(MetaProperty.IMG_PRIVATE);
      setIcon(img);
      
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

            // 20030730 hack at this point - check for date to be localized
            if (prop instanceof PropertyDate)
              html.append(((PropertyDate)prop).toString(false, true));
            else
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
