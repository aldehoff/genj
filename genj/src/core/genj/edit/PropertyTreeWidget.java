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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.IconValueAvailable;
import genj.gedcom.MetaProperty;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.Transaction;
import genj.util.swing.HeadlessLabel;
import genj.util.swing.ImageIcon;
import genj.util.swing.TreeWidget;
import genj.view.Context;
import genj.view.ContextProvider;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.plaf.TreeUI;
import javax.swing.text.View;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * A Property Tree
 */
public class PropertyTreeWidget extends TreeWidget implements ContextProvider {

  /** a default renderer we keep around for colors */
  private DefaultTreeCellRenderer defaultRenderer;
  
  /** the model */
  private Model model;

  /** cached object per property */
  private Map property2view =  new HashMap();

  /**
   * Constructor
   */
  public PropertyTreeWidget(Property setRoot) {
    this(setRoot.getGedcom());
    setRoot(setRoot);
    setExpandsSelectedPaths(true);
    ToolTipManager.sharedInstance().registerComponent(this);
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
   * @see javax.swing.JComponent#removeNotify()
   */
  public void removeNotify() {
    // make sure model isn't connected to gedcom model anymoew
    model.setGedcom(null);
    // continue
    super.removeNotify();
  }
  
  /**
   * ContextProvider - context at given position
   */
  public Context getContextAt(Point pos) {

    // property at that point?
    Property prop = getPropertyAt(pos);
    // Entity known?
    Entity entity = null;
    Property root = getRoot();
    if (root!=null) {
      entity = root.getEntity();
    }
    // done
    return new Context(model.gedcom, entity, prop);
  }
  
  /**
   * @see javax.swing.JTree#getPreferredScrollableViewportSize()
   */
  public Dimension getPreferredScrollableViewportSize() {
    return new Dimension(256,128);
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
  }
  
  /**
   * The current root
   */
  public Property getRoot() {
    return model.root;
  }

  /**
   * Some LnFs have the habit of fixing the row height which
   * we don't want
   */  
  public void setRowHeight(int rowHeight) {
    super.setRowHeight(0);
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
  public Property getPropertyAt(int x, int y) {
    // calc path to node under mouse
    TreePath path = super.getPathForLocation(x, y);
    if ((path==null) || (path.getPathCount()==0)) 
      return null;
    // done
    return (Property)path.getLastPathComponent();
  }

  /**
   * Resolve property at given point
   */
  public Property getPropertyAt(Point pos) {
    return getPropertyAt(pos.x, pos.y);
  }
  
  /**
   * @see javax.swing.JTree#getToolTipText(MouseEvent)
   */
  public String getToolTipText(MouseEvent event) {
    // lookup property
    Property prop = getPropertyAt(event.getX(),event.getY());
    if (prop==null) return null;
    // .. transient?
    if (prop.isTransient()) return null;
    // .. won't work if property is not part of entity (e.g. Cliboard.Copy)
    if (prop.getEntity()==null) return null;
    // .. calc information text
    String info = prop.getInfo();
    if (info==null) 
      return null;
    // return text wrapped to 200 pixels
    return "<html><table width=200><tr><td>"+info+"</td></tr></table></html";    
  }

  /**
   * Intercept new ui to get default renderer that provides us with colors
   */  
  public void setUI(TreeUI ui) {
    // continue
    super.setUI(ui);
    // grab the default renderer now
    defaultRenderer = new DefaultTreeCellRenderer();
    // clear cache of view
    if (property2view!=null)
      property2view.clear();
    // make sure that propagates
    if (model!=null)
      model.fireStructureChanged();
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

    /** the gedcom we're looking at */
    private Gedcom gedcom;
    
    /**
     * Constructor
     */
    public Model(Gedcom gedcom) {
      setGedcom(gedcom);
    }
    
    /**
     * Gedcom to use
     */
    public void setGedcom(Gedcom set) {
      // old?
      if (gedcom!=null) {
        gedcom.removeGedcomListener(this);
        gedcom = null;
      }
      // new?
      if (set!=null) {
        gedcom = set;
        gedcom.addGedcomListener(this);
      }
    }          
  
    /**
     * Set the root
     */
    public void setRoot(Property set) {
      // remember
      root = set;
      // notify
      fireStructureChanged();
      // make sure we don't show null-root
      setRootVisible(root!=null);
    }
  
    /**
     * Adds a listener to this model
     */
    public void addTreeModelListener(TreeModelListener l) {
      listeners.add(l);
    }          
  
    /**
     * Removes a Listener from this model
     */
    public void removeTreeModelListener(TreeModelListener l) {
      listeners.remove(l);
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
        property2view.remove(prop);
  
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

      // clear cache of view
      property2view.clear();

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
    public void handleChange(Transaction tx) {
      
      // Could we be affected at all?
      if (root==null) 
        return;
        
      // our entity
      Entity entity = root.getEntity();

      // Entity deleted ?
      if ( !tx.getChanges(tx.EDEL).isEmpty() ) {
        // Loop through known entity ?
        boolean affected = false;
        Iterator ents = tx.getChanges(tx.EDEL).iterator();
        while (ents.hasNext()) {
          // the entity deleted
          Entity deleted = (Entity)ents.next();
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
      if (!tx.getChanges(tx.EMOD).contains(entity))
        return;

      // Property added/removed ?
      Iterator padds = tx.getChanges(tx.PADD).iterator();
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
      Iterator pdels = tx.getChanges(tx.PDEL).iterator();
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
      if ( !tx.getChanges(tx.PMOD).isEmpty() ) {
        firePropertiesChanged(tx.getChanges(tx.PMOD));
        return;
      }

      // Done
    }
  
  } //Model

  /**
   * Our renderer
   */
  private class Renderer extends HeadlessLabel implements TreeCellRenderer {
    
    /**
     * Constructor
     */
    private Renderer() {
      setOpaque(true);
    }
    
    /**
     * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object object, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

      // no property no luck      
      if (!(object instanceof Property))
        return this;
      Property prop = (Property)object;

      // grab current font
      setFont(PropertyTreeWidget.this.getFont());

      // prepare color
      if (defaultRenderer!=null) {
        if (sel) {
          setForeground(defaultRenderer.getTextSelectionColor());
          setBackground(defaultRenderer.getBackgroundSelectionColor());
        } else {
          setForeground(defaultRenderer.getTextNonSelectionColor());
          setBackground(defaultRenderer.getBackgroundNonSelectionColor());
        }
      }

      // calc image        
      ImageIcon img = prop.getImage(true);
      if (prop.isPrivate()) 
        img = img.getOverLayed(MetaProperty.IMG_PRIVATE);
      setIcon(img);

      // set view to use
      setView(getView(prop, sel));

      // done
      return this;
    }

    /**
     * Calculate View
     */
    private View getView(Property prop, boolean scratch) {
      
      // check if we got one
      View view = scratch ? null : (View)property2view.get(prop);

      // create if necessary
      if (view==null) {

        // create one
        view = setHTML(calcHTML(prop));
        
        // remember if not scratch
        if (!scratch) property2view.put(prop, view);
      
      }
      
      // done
      return view;
    }
    
    /**
     * Calculate HTML for a property node
     */
    private String calcHTML(Property prop) {

      StringBuffer html = new StringBuffer();
      
      html.append("<html>");
      
      if (prop instanceof Entity) 
        calcEntity(html, (Entity)prop);
      else
        calcProperty(html, prop);

      html.append("</html>");

      return html.toString();        
    }
    
    private void calcEntity(StringBuffer html, Entity entity) {        
        
      html.append("@").append(entity.getId()).append("@ ");
      html.append("<b>").append(entity.getTag()).append("</b>");
    } 
      
    private void calcProperty(StringBuffer html, Property prop) {

      // TAG      
      if (!prop.isTransient())
        html.append(" <b>").append(prop.getTag()).append("</b> ");

      // private?
      if (prop.isSecret()) {
        html.append("*****");
        return;
      }
      
      // multiline?          
      if (prop instanceof MultiLineProperty && !(prop instanceof IconValueAvailable)) {
        
        char[] chars = ((MultiLineProperty)prop).getLinesValue().toCharArray();
        for (int i=0; i<chars.length; i++) {
          char c = chars[i];
          if (c=='\n') html.append("<br>");
          else html.append(c);
        }
        
        return;
        
      } 
      
      // date?
      if (prop instanceof PropertyDate) {
        html.append(((PropertyDate)prop).toString(true));
        return;
      }
      
      // default!
      html.append(prop.getValue());
    }
    
  } //Renderer
    
} //PropertyTree
