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
import genj.gedcom.MultiLineProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyMultilineValue;
import genj.gedcom.Transaction;
import genj.io.GedcomReader;
import genj.io.PropertyTransferable;
import genj.renderer.Options;
import genj.util.swing.HeadlessLabel;
import genj.util.swing.ImageIcon;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.plaf.TreeUI;
import javax.swing.text.View;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import swingx.tree.AbstractTreeModel;
import swingx.tree.DnDTree;
import swingx.tree.DnDTreeModel;

/**
 * A Property Tree
 */
public class PropertyTreeWidget extends DnDTree {
  
  /** a default renderer we keep around for colors */
  private DefaultTreeCellRenderer defaultRenderer;
  
  /** the model */
  private Model model = new Model();

  /** cached object per property */
  private Map property2view =  new HashMap();
  
  /** stored gedcom */
  private Gedcom gedcom;

//  /**
//   * Constructor
//   */
//  public PropertyTreeWidget(Property setRoot) {
//    this(setRoot.getGedcom());
//    setRoot(setRoot);
//    setExpandsSelectedPaths(true);
//    ToolTipManager.sharedInstance().registerComponent(this);
//  }
    
  /**
   * Constructor
   */
  public PropertyTreeWidget(Gedcom gedcom) {

    setModel(model);

    // remember
    this.gedcom = gedcom;
    
    // setup callbacks
    setCellRenderer(new Renderer());
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    setToggleClickCount(Integer.MAX_VALUE);

    setExpandsSelectedPaths(true);
    ToolTipManager.sharedInstance().registerComponent(this);
    
    // done
  }
  
  /**
   * return a path for a property
   */
  public TreePath getPathFor(Property property) {
    return model.getPathToRoot(property);
  }

  /**
   * @see javax.swing.JComponent#addNotify()
   */
  public void addNotify() {
    // connect model to gedcom
    model.setGedcom(gedcom);
    // continue
    super.addNotify();
  }

  
  /**
   * @see javax.swing.JComponent#removeNotify()
   */
  public void removeNotify() {
    // disconnect model from gedcom
    model.setGedcom(null);
    // continue
    super.removeNotify();
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
    // show all rows
    expandAllRows();
    // done
  }
  
  /**
   * Expand all rows
   */
  public void expandAllRows() {
    for (int i=0;i<getRowCount();i++)
      expandRow(i); 
  }
  
  /**
   * Expand 'under' path
   */
  public void expandAll(TreePath root) {
    
    //collapsePath(root);
    expandPath(root);
    
    Object node = root.getLastPathComponent();
    for (int i=0;i<model.getChildCount(node);i++)
      expandAll(root.pathByAddingChild(model.getChild(node, i)));
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
    if (model.root==null||select==null||select.getEntity()==null) {
      clearSelection();
      return;
    }
    // show and select
    TreePath tpath = model.getPathToRoot(select);
    scrollPathToVisible(tpath);
    setSelectionPath(tpath);
    // done
  }
  
  /**
   * returns the currently selected property
   */
  public Property getSelection() {
    // Calculate selection path
    TreePath[] paths = getSelectionPaths();
    if (paths==null||paths.length!=1) 
      return null;
    // got it
    TreePath path = paths[0];
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
    String info = prop.getPropertyInfo();
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
  }

  /**
   * Our model
   */
  private class Model extends AbstractTreeModel implements DnDTreeModel, GedcomListener {

    private Object NULL = new Object();

    /** root of tree */
    private Property root = null;

    /** the gedcom we're looking at */
    private Gedcom gedcom;
    
    /**
     * Gedcom to use
     */
    protected void setGedcom(Gedcom set) {
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
    protected void setRoot(Property set) {
      // remember
      root = set;
      // .. clear cache of view
      property2view.clear();
      // .. tell about it
      fireTreeStructureChanged(this, getPathToRoot(getRoot()), null, null);
      // make sure we don't show null-root
      setRootVisible(root!=null);
      // expand all
      expandAllRows();
    }

    /**
     * DND support - drag test
     */
    public int canDrag(List children) {
      return COPY | MOVE;
    }

    /**
     * DND support - drag
     */
    public void drag(int action, List children, Object parent, int index) {
      
      // do nothing on copy
      if (action==COPY)
        return;
      
      // check if this is an in-parent move - we'll leave the
      // shuffle of the children in parent to drop() in that case
      Property newParent = (Property)parent;
      if (newParent!=null&&newParent.isProperties(children)) 
        return;
      
      // start transaction for our gedcom
      gedcom.startTransaction();
      
      for (int i=0;i<children.size();i++) {
        
        // remove one by one
        Property child = (Property)children.get(i);
        Property childsParent = child.getParent();
        int pos = childsParent.getPropertyPosition(child);
        childsParent.delProperty(pos);
        
        // tell tree about it since that might change insert target
        fireTreeNodesRemoved(this, getPathToRoot(childsParent), new int[]{pos}, new Object[]{child});
      }
      
      // end transaction if newParent is not in same gedcom
      if (newParent==null||gedcom!=newParent.getGedcom())
        gedcom.endTransaction();
        
      // done
    }
    
    /**
     * DND support - transferable
     */
    public Transferable getTransferable(List children) {
      return new PropertyTransferable(children);
    }

    /**
     * DND support - drop test
     */
    public boolean canDrop(int action, Transferable transferable, Object parent, int index) {
      // copy or move
      if (!(action==COPY||action==MOVE))
        return false;
      return transferable.isDataFlavorSupported(PropertyTransferable.VMLOCAL_FLAVOR) 
        || transferable.isDataFlavorSupported(PropertyTransferable.STRING_FLAVOR);       
    }
    /**
     * DND support - drop
     */
    public List drop(int action, Transferable transferable, Object parent, int index) throws IOException, UnsupportedFlavorException {

      // start transaction for our gedcom?
      if (!gedcom.isTransaction())
        gedcom.startTransaction();
      
      // is this an in-parent move?
      Property newParent = (Property)parent;
      if (action==MOVE&&transferable.isDataFlavorSupported(PropertyTransferable.VMLOCAL_FLAVOR)) {
        List dragged = (List)transferable.getTransferData(PropertyTransferable.VMLOCAL_FLAVOR);
        if (newParent.isProperties(dragged)) {
          
          // re-shuffle children
          for (int i=0,j=dragged.size();i<j;i++) {
            if (newParent.getPropertyPosition((Property)dragged.get(i))<index)
              index--;
          }
          List shuffle = new ArrayList(Arrays.asList(newParent.getProperties()));
          shuffle.removeAll(dragged);
          shuffle.addAll(index, dragged);
          
          // commit
          newParent.setProperties(shuffle);
          gedcom.endTransaction();
          
          // done
          return dragged;
        }
      }
      
      // paste text into new parent
      try {
        String s = transferable.getTransferData(PropertyTransferable.STRING_FLAVOR).toString();
        return GedcomReader.read(new StringReader(s), newParent, index);
      } finally {
        gedcom.endTransaction();      
      }
      
      // done      
    }

    /**
     * Returns parent of node
     */  
    protected Object getParent(Object node) {
      // none for root
      if (node==NULL||node==root)
        return null;
      // otherwise its parent
      return ((Property)node).getParent();
    }
  
    /**
     * Returns child by index of parent
     */
    public Object getChild(Object parent, int index) {
      return ((Property)parent).getProperty(index);
    }          
  
    /**
     * Returns child count of parent
     */
    public int getChildCount(Object parent) {
      if (parent==NULL)
        return 0;
      return ((Property)parent).getNoOfProperties();
    }
    
    /**
     * Returns index of given child from parent
     */
    public int getIndexOfChild(Object parent, Object child) {
      try {
        return ((Property)parent).getPropertyPosition((Property)child);
      } catch (Throwable t) {
        return -1;
      }
    }          
  
    /**
     * Returns root of tree
     */
    public Object getRoot() {
      return root!=null?root:NULL;
    }          
  
    /**
     * Tells wether object is a leaf
     */
    public boolean isLeaf(Object node) {
      if (node==NULL)
        return true;
      return ((Property)node).getNoOfProperties()==0;
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
      if (tx.get(Transaction.ENTITIES_DELETED).contains(entity)) {
        setRoot(null);
        return;
      }
      
      // at least same entity modified?
      if (!tx.get(Transaction.ENTITIES_MODIFIED).contains(entity))
        return;
        
      // always try to stay with current selection
      Property selection = getSelection();
      
      // follow changes
      Change[] changes = tx.getChanges();
      for (int i=0;i<changes.length;i++) {
        // applicable?
        Change change = changes[i];
        if (change.getEntity()!=entity)
          continue;
        // structure change?
        if (change instanceof Change.PropertyRemoved||change instanceof Change.PropertyAdded) {
          if (change instanceof Change.PropertyAdded)
            selection = change.getProperty();
          setRoot(root);
          break;
        }
        // simple change?
        if (change instanceof Change.PropertyChanged) {
          // .. forget cached value for prop
          property2view.remove(change.getProperty());
          // .. tell about it
          fireTreeNodesChanged(this, getPathFor(change.getProperty()), null, null);
        }
        // go on checking
      }
      
      // restore selection
      setSelection(selection);
      
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
      setFont(Options.getInstance().getDefaultFont());
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
      
      // multiline (not using MultiLineProperty here)          
      if (prop instanceof PropertyMultilineValue && !(prop instanceof IconValueAvailable)) {
        MultiLineProperty.Iterator it = ((PropertyMultilineValue)prop).getLineIterator();
        while (true) {
          html.append(it.getValue());
          if (!it.next()) break;
          html.append("<br>");
        }
        return;
      } 
      
      // default!
      html.append(prop.getDisplayValue());
    }
    
  } //Renderer
    
} //PropertyTree
