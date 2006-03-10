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
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyChange;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Transaction;
import genj.io.PropertyReader;
import genj.io.PropertyTransferable;
import genj.util.swing.HeadlessLabel;
import genj.util.swing.ImageIcon;
import genj.view.Context;
import genj.view.ContextProvider;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import swingx.dnd.tree.DnDTree;
import swingx.dnd.tree.DnDTreeModel;
import swingx.tree.AbstractTreeModel;

/**
 * A Property Tree
 */
public class PropertyTreeWidget extends DnDTree implements ContextProvider {
  
  private final static String UNIX_DND_FILE_PREFIX = "file:";
  
  /** a default renderer we keep around for colors */
  private DefaultTreeCellRenderer defaultRenderer;
  
  /** stored gedcom */
  private Gedcom lazyGedcom;

  /**
   * Constructor
   */
  public PropertyTreeWidget(Gedcom gedcom) {

    // initialize an empty model
    super.setModel(new Model());

    // remember
    this.lazyGedcom = gedcom;
    
    // setup callbacks
    setCellRenderer(new Renderer());
    getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    setToggleClickCount(Integer.MAX_VALUE);

    setExpandsSelectedPaths(true);
    ToolTipManager.sharedInstance().registerComponent(this);
    
    // done
  }
  
  /**
   * Accessor - current context 
   * @return Gedcom tree's root and selection 
   */
  public Context getContext() {
    Entity root = (Entity)getRoot();
    if (root==null) 
      return new Context(lazyGedcom);
    Context result = new Context(root);
    result.addProperties(getSelection(false));
    return result;
  }
  
  /**
   * Dont' allow to change the underlying model
   */
  public void setModel() {
    throw new IllegalArgumentException();
  }

  /**
   * Access to our specialized model
   */
  private Model getPropertyModel() {
    return (Model)getModel();
  }
  
  /**
   * return a path for a property
   */
  public Object[] getPathFor(Property property) {
    return getPropertyModel().getPathToRoot(property);
  }

  /**
   * @see javax.swing.JComponent#addNotify()
   */
  public void addNotify() {
    // connect model to gedcom
    getPropertyModel().setGedcom(lazyGedcom);
    // continue
    super.addNotify();
    
    getRootPane().getGlassPane().addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        System.out.print("!");
      }
    });
    
  }

  
  /**
   * @see javax.swing.JComponent#removeNotify()
   */
  public void removeNotify() {
    // disconnect model from gedcom
    getPropertyModel().setGedcom(null);
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
    if (getPropertyModel().getRoot()==property)
      return;
    // propagate to model
    getPropertyModel().setRoot(property);
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
    
    Model model = getPropertyModel();
    Object node = root.getLastPathComponent();
    for (int i=0;i<model.getChildCount(node);i++)
      expandAll(root.pathByAddingChild(model.getChild(node, i)));
  }
  
  /**
   * The current root
   */
  public Property getRoot() {
    return getPropertyModel().getPropertyRoot();
  }

  /**
   * Some LnFs have the habit of fixing the row height which
   * we don't want
   */  
  public void setRowHeight(int rowHeight) {
    super.setRowHeight(0);
  }
  
  /**
   * Selects a property
   */
  public void setSelection(List select) {
    clearSelection();
    // safety check
    Property root = (Property)getPropertyModel().getRoot();
    if (root==null) 
      return;
    // add to selection
    TreePath first = null;
    for (Iterator ps = select.iterator(); ps.hasNext(); ) {
      try {
        TreePath path = new TreePath(getPropertyModel().getPathToRoot((Property)ps.next()));
        addSelectionPath(path);
        if (first==null) first = path;
      } catch (IllegalArgumentException e) {
        // ignore
      }
    }
    // show it 
    if (first!=null)
      scrollPathToVisible(first);
    // done
  }
  
  /**
   * returns the currently selected properties
   */
  public List getSelection(boolean normalize) {
    // go through selection paths
    List result = new ArrayList();
    TreePath[] paths = getSelectionPaths();
    for (int i=0;paths!=null&&i<paths.length;i++) {
      result.add(paths[i].getLastPathComponent());
    }
    // normalize?
    if (normalize) result = normalize(result);
    // done
    return result;
  }
  
  /**
   * normalizes a selection - that is for each element in result
   * <pre>
   * n(property) : for (Property property : list) !list.contains(property.getParent()) && n(property.getParent())
   * </pre>
   * @param selection
   * @return
   */
  private List normalize(List selection) {
    
    // switch to children for root
    Property root = (Property)((Model)getModel()).getRoot();
    if (selection.contains(root))
      return Arrays.asList(root.getProperties());
    
    ArrayList result = new ArrayList(selection.size());
    
    for (Iterator it = selection.iterator(); it.hasNext(); ) {
      Property prop = (Property)it.next();
      // any containing in selection as well?
      Property parent = prop.getParent();
      while (parent!=null) {
        if (selection.contains(parent)) break;
        parent = parent.getParent();
      }
      if (parent==null) result.add(prop);
    }
    
    // done
    return result;
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
  }
  
  /**
   * override to provide simple text value (is used by getNextMatch on key presses) 
   *
   */
  public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    if (value instanceof Property)
      return ((Property)value).getTag();
    return "";
  }
  
  /** the gedcom where a drag originated from */
  private static Gedcom draggingFrom = null;
  
  /**
   * Our model
   */
  private class Model extends AbstractTreeModel implements DnDTreeModel, GedcomListener {

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
      // .. tell about it
      rootExchanged();
      // make sure we don't show null-root
      setRootVisible(root!=null);
      // expand all
      expandAllRows();
    }
    
    public Property getPropertyRoot() {
      return root;
    }

    /**
     * DND support - transferable
     */
    public Transferable createTransferable(Object[] nodes) {
      
      // remember where we're dragging from
      draggingFrom = gedcom;
      
      // normalize selection
      List list = normalize(Arrays.asList(nodes));
      
      // done 
      return new PropertyTransferable(list);
    }

    public int getDragActions(Transferable transferable) {
      return COPY | MOVE;
    }

    public int getDropActions(Transferable transferable, Object parent, int index) {

      try {

        // an in-vm dnd?
        if (transferable.isDataFlavorSupported(PropertyTransferable.VMLOCAL_FLAVOR)) {
          // we don't allow drop on parent if parent is in list of dragged (recursive)
          List dragged = (List)transferable.getTransferData(PropertyTransferable.VMLOCAL_FLAVOR);
          Property pparent = (Property)parent;
          while (pparent!=null) {
            if (dragged.contains(pparent)) return 0;
            pparent = pparent.getParent();
          }
          return COPY | MOVE;
        }
        
        // a string dnd?
        if (transferable.isDataFlavorSupported(PropertyTransferable.STRING_FLAVOR)) 
          return COPY | MOVE;
        
        // files are ok as well
        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) 
          return COPY | MOVE;

      } catch (Exception e) {
      }
      
      // fallthrough result
      return 0;
    }

    /**
     * DND support - drop comes first.
     */
    public void drop(Transferable transferable, Object parent, int index, int action) throws IOException, UnsupportedFlavorException {

      Property newParent = (Property)parent;

      try {
        
        // start transaction 
        gedcom.startTransaction();
      
        // an in-vm drag?
        if (transferable.isDataFlavorSupported(PropertyTransferable.VMLOCAL_FLAVOR)) {
          
          String string = transferable.getTransferData(PropertyTransferable.STRING_FLAVOR).toString();
          
          // delete children for MOVE within same gedcom (drag won't do it)
          if (action==MOVE&&draggingFrom==gedcom) {
            List children = (List)transferable.getTransferData(PropertyTransferable.VMLOCAL_FLAVOR);
            for (int i=0;i<children.size();i++) {
              Property child = (Property)children.get(i);
              child.getParent().delProperty(child);
            }
          }
          
          // paste text
          new PropertyReader(new StringReader(string), true) {
            protected void trackXRef(PropertyXRef xref) {
              try { xref.link(); } catch (Throwable t) {}
            }
          }.read(newParent, index);
          
          // done
          return;
        }
      
        // a file drop? apparently a file drop is a simple text starting with file: on linux (kde/gnome)
        String string = null;
        if (transferable.isDataFlavorSupported(PropertyTransferable.STRING_FLAVOR)) {
          string = transferable.getTransferData(PropertyTransferable.STRING_FLAVOR).toString();
          if (string.startsWith(UNIX_DND_FILE_PREFIX)) {
            for (StringTokenizer files = new StringTokenizer(string, "\n"); files.hasMoreTokens(); ) {
              String file = files.nextToken().trim();
              if (file.startsWith(UNIX_DND_FILE_PREFIX)) 
                newParent.addFile(new File(file.substring(UNIX_DND_FILE_PREFIX.length())));
            }
            return;
          }
        }
        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
          for (Iterator files=((List)transferable.getTransferData(DataFlavor.javaFileListFlavor)).iterator(); files.hasNext(); ) 
            newParent.addFile((File)files.next());
          return;
        }
        
        // still some text we can paste into new parent?
        if (string!=null) {
          EditView.LOG.fine("reading dropped text '"+string+"'");
          new PropertyReader(new StringReader(string), true).read(newParent, index);
        }
        
        // done
        
      } finally {
        gedcom.endTransaction();
      }
      
      // done      
    }

    /**
     * DND support - drag after drop!
     */
    public void drag(Transferable transferable, int action) throws UnsupportedFlavorException, IOException {
      
      // anything to drag?
      List children = (List)transferable.getTransferData(PropertyTransferable.VMLOCAL_FLAVOR);
      if (children.isEmpty())
        return;
      
      // drag out children if it's a move to a different gedcom
      if (action==MOVE &&draggingFrom!=gedcom) {
        try {            
          gedcom.startTransaction();
          for (int i=0;i<children.size();i++) {
            Property child = (Property)children.get(i);
            child.getParent().delProperty(child);
          }
        } finally {
          gedcom.endTransaction();
        }
      }
      
      // done
    }
       
    public void releaseTransferable(Transferable transferable) {
      draggingFrom  = null;
    }
    
    /**
     * Returns parent of node
     */  
    protected Object getParent(Object node) {
      // none for root
      if (node==root)
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
      return root;
    }          
  
    /**
     * Tells wether object is a leaf
     */
    public boolean isLeaf(Object node) {
      // nothing is leaf - allows to drag everywhere
      return false;
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
      List selection = getSelection(false);
      
      // follow changes
      Change[] changes = tx.getChanges();
      for (int i=0;i<changes.length;i++) {
        // applicable?
        Change change = changes[i];
        if (change.getEntity()!=entity)
          continue;
        // structure change?
        if (change instanceof Change.PropertyStructure) {
          if (change instanceof Change.PropertyAdd&&!(((Change.PropertyAdd)change).getAdded() instanceof PropertyChange))
            selection = Collections.singletonList( ((Change.PropertyAdd)change).getAdded() );
          setRoot(root);
          break;
        }
        // simple change?
        if (change instanceof Change.PropertyValue) {
          Property changed = ((Change.PropertyValue)change).getChanged();
          // .. tell about it
          fireTreeNodesChanged(this, getPathFor(changed), null, null);
        }
        // go on checking
      }
      
      // set selection
      clearSelection();
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

      // calc text
      setText(prop instanceof Entity ? calcText((Entity)prop) : calcText(prop));

      // done
      return this;
    }

    private String calcText(Entity entity) {        
      return "@" + entity.getId() + "@ " + entity.getTag();
    } 
      
    private String calcText(Property prop) {

      StringBuffer result = new StringBuffer();
      
      if (!prop.isTransient()) {
        result.append(prop.getTag());
        result.append(' ');
      }

      // private?
      if (prop.isSecret()) {
        result.append("*****");
      } else {
        String val = prop.getDisplayValue();
        int nl = val.indexOf('\n');
        if (nl>=0) val = val.substring(0, nl) + "...";
        result.append(val);
      }
      
      // done
      return result.toString();
    }
    
  } //Renderer
    
} //PropertyTree
