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

import genj.edit.actions.*;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.HeadlessLabel;
import genj.util.swing.MenuHelper;
import genj.view.ContextPopupSupport;
import genj.view.CurrentSupport;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 * Component for editing genealogic entity properties
 */
public class EditView extends JPanel implements CurrentSupport, ToolBarSupport, ContextPopupSupport {

  /** the gedcom we're looking at */
  private Gedcom    gedcom;
  
  /** the frame we're in */
  private Frame     frame;

  /** the registry we use */
  private Registry registry;

  /** the resources we use */
  static final Resources resources = new Resources("genj.edit");

  /** buttons we use */
  private AbstractButton    actionButtonAdd,
                            actionButtonRemove,
                            actionButtonUp,
                            actionButtonDown,
                            actionButtonReturn,
                            actionSticky;

  /** everything for the tree */
  private PropertyTree      tree = null;
  private JScrollPane       treePane = null;
  
  /** everything for the proxy */
  private JPanel            proxyPane;
  private Proxy             currentProxy = null;

  /** splitpane for tree/proxy */
  private JSplitPane        splitPane = null;
  
  /** an entity to show in next open EditView */
  private static Entity preselectEntity = null;
  
  /**
   * Constructor
   */
  public EditView(Gedcom setGedcom, Registry setRegistry, Frame setFrame) {
    
    // remember
    this.gedcom   = setGedcom;
    this.frame    = setFrame;
    this.registry = setRegistry;

    // TREE Component's 
    tree = new PropertyTree();
    treePane = new JScrollPane(tree);
    treePane.setMinimumSize  (new Dimension(160, 128));
    treePane.setPreferredSize(new Dimension(160, 128));

    // EDIT Component
    proxyPane = new JPanel();
    proxyPane.setLayout(new BoxLayout(proxyPane,BoxLayout.Y_AXIS));

    // SplitPane with tree/edit
    splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treePane, proxyPane);
    splitPane.setDividerLocation(registry.get("divider",-1));

    // layout
    setLayout(new BorderLayout());
    add(splitPane, BorderLayout.CENTER);
    
    // menubar
    installMenuBar((JFrame)setFrame);
    
    // Done
  }
  
  /**
   * @see javax.swing.JComponent#addNotify()
   */
  public void addNotify() {
    super.addNotify();

    // Check if we can preset something to edit
    // 20021217 #655478 EditView follows current if closed UNLESS sticky
    Entity entity = preselectEntity;
    if (entity==null&&isSticky()) {
      try { 
        entity = gedcom.getEntity(registry.get("last",(String)null)); 
      } catch (Exception e) {
      }
    }
    if (entity==null) {
      entity = ViewManager.getInstance().getCurrentEntity(gedcom);
    }
    setEntity(entity);
    preselectEntity=null;
    
  }

  /**
   * Notification when component is not used any more
   */
  public void removeNotify() {

    // Remember registry
    registry.put("divider",splitPane.getDividerLocation());
    registry.put("last", getCurrentEntity()!=null?getCurrentEntity().getId():"");
    registry.put("sticky", isSticky());

    // Continue
    super.removeNotify();

    // Done
  }

  /**
  public void handleChange(Change change) {

    // Do I show an entity's properties now ?
    if (currentEntity==null) {
      return;
    }

    // Entity deleted ?
    if ( change.isChanged(Change.EDEL) ) {

      // Loop through known entity ?
      boolean affected = false;

      Iterator ents = change.getEntities(Change.EDEL).iterator();
      while (ents.hasNext()) {

        Object ent = ents.next();

        // ... a removed entity has to be removed from stack
        while (returnStack.removeElement(ent)) {};

        // ... and might affect the current edit view
        affected |= (ent==currentEntity);
      }

      // Is this a show stopper at this point?
      if (affected==true) {
        setEntity(null);
        return;
      }

      // continue
    }

    // Property added/removed ?
    if ( change.isChanged(Change.PADD)
       ||change.isChanged(Change.PDEL)) {

      tree.setRoot(currentEntity.getProperty());

      // .. select added
      List padd = change.getProperties(Change.PADD);
      if (padd.size()>0) {
        PropertyTreeModel model = (PropertyTreeModel)tree.getModel();
        Property root = (Property)model.getRoot();
        Property first = (Property)padd.get(0);
        if (first instanceof PropertyEvent) {
          Property pdate = ((PropertyEvent)first).getDate(false);
          if (padd.contains(pdate))
            first = pdate!=null ? pdate : first;
        }
        Property[] path = root.getPathTo(first);
        if (path!=null) {
          tree.setSelectionPath(new TreePath(path));
        }
      }
      return;
    }

    // Property modified ?
    if ( change.isChanged(change.PMOD) ) {
      if ( change.getEntities(Change.EMOD).contains(currentEntity)) {
        PropertyTreeModel treeModel = (PropertyTreeModel)tree.getModel();
        treeModel.firePropertiesChanged(change.getProperties(Change.PMOD));
        //if (change.getProperties(change.PMOD).contains(currentProperty))
        //  stopEdit(false);
        return;
      }
    }

    // Done
  }
*/

  /**
   * @see genj.view.CurrentSupport#setCurrentEntity(Entity)
   */
  public void setCurrentEntity(Entity entity) {
    if (!isSticky()) setEntity(entity);
  }

  /**
   * @see genj.view.CurrentSupport#setCurrentProperty(Property)
   */
  public void setCurrentProperty(Property property) {
    // ignored
  }
  
  /**
   * @see genj.view.EntityPopupSupport#getEntityPopupContainer()
   */
  public JComponent getContextPopupContainer() {
    return tree;
  }

  /**
   * @see genj.view.EntityPopupSupport#getEntityAt(Point)
   */
  public Context getContextAt(Point pos) {
    // selection o.k.?
    TreePath path = tree.getPathForLocation(pos.x, pos.y);
    if (path!=null) tree.setSelectionPath(path);
    // try to resolve a return value
    Property prop = tree.getCurrentProperty();
    if (prop!=null) return new Context(prop);
    return new Context(getCurrentEntity());
  }
  
  /**
   * @see genj.view.ToolBarSupport#populate(JToolBar)
   */
  public void populate(JToolBar bar) {

    // buttons for property manipulation    
    ButtonHelper bh = new ButtonHelper()
      .setEnabled(false)
      .setResources(resources)
      .setInsets(0)
      .setMinimumSize(new Dimension(0,0))
      .setContainer(bar);
    
    actionButtonAdd    = bh.create(new ActionPropertyAdd());
    actionButtonRemove = bh.create(new ActionPropertyDel());
    actionButtonUp     = bh.create(new ActionPropertyUpDown(true));
    actionButtonDown   = bh.create(new ActionPropertyUpDown(false));
    actionButtonReturn = bh.setEnabled(true).create(new ActionBack());
    actionSticky       = bh.create(new ActionSticky());
    
    actionSticky.setSelected(registry.get("sticky",false));

    // done
  }
  
  /**
   * Open EditView on entity   */
  public static void open(Entity entity) {
    preselectEntity = entity;
    ViewManager.getInstance().openView(EditViewFactory.class, entity.getGedcom());
  }

  /**
   * returns the frame this control resides in
   */
  /*package*/ Frame getFrame() {
    return frame;
  }

  /**
   * returns the currently viewed entity
   */
  /*package*/ Entity getCurrentEntity() {
    return tree.getPropertyModel().getEntity();
  }
  
  /**
   * Set the entity to display
   */
  /*pacakge*/ void setEntity(Entity entity) {

    // already?
    if (getCurrentEntity()==entity) return;

    // Try to stop old editing first
    stopEdit(true);

    // Reset tree model
    tree.setEntity(entity);

    // Pre-selected editing node ?
    if ((entity!=null)) {//&&(tree.isShowing())) {
      tree.setSelectionRow(Math.min(1,tree.getRowCount()));
    }
    
    // Done
  }
  
  /**
   * Change sticky status
   */
  /*package*/ void setSticky(boolean set) {
    actionSticky.setSelected(set);
  }

  /**
   * Check sticky status
   */
  /*package*/ boolean isSticky() {
    return actionSticky!=null && actionSticky.isSelected();
  }

  /**
   * Creates the top menu bar
   */
  private void installMenuBar(JFrame frame) {
    
    // prepare bar
    MenuHelper mh = new MenuHelper();
    mh.setTarget(this);
    frame.setJMenuBar(mh.createBar());
    
    // create action menu
    mh.createMenu(new ActionMenu());
    
  }
  
  /**
   * Prepare a proxy for editing a property
   */
  private void startEdit(boolean keepSimple) {

    // Property?
    Property prop = tree.getCurrentProperty();
    if (prop == null) return;

    // Calculate editing for property
    String me    = getClass().getName(),
           pkg   = me.substring( 0, me.lastIndexOf(".") + 1 ),
           proxy = prop.getProxy();

    if (proxy == "") {
      return;
    }

    // Create proxy
    try {
      if (keepSimple)
        throw new Exception();
      currentProxy = (Proxy) Class.forName( pkg + "Proxy" + proxy ).newInstance();
    } catch (Exception e) {
      currentProxy = new ProxyUnknown();
    }

    // Add Image+Heading
    JLabel label = new JLabel();
    label.setIcon(prop.getImage(true));
    label.setText(prop.getTag());
    label.setAlignmentX(0);
    label.setBorder(new EmptyBorder(2,0,8,0));
    proxyPane.add(label);

    // Add proxy components
    try {
      currentProxy.start(proxyPane,label,prop,this);
    } catch (ClassCastException ex) {
      Debug.log(Debug.WARNING, this, "Seems like we're getting bad proxy for property "+prop, ex);
    }
    
    // Layout change !
    proxyPane.validate();
    proxyPane.doLayout();

    // Done
  }

  /**
   * Stop editing
   */
  private void stopEdit(boolean commit) {

    // Prepare for finishing changed and finish
    if (commit&&currentProxy!=null&&currentProxy.hasChanged()) {
      Gedcom gedcom = getCurrentEntity().getGedcom();
      if (gedcom.startTransaction()) {
        currentProxy.finish();
        gedcom.endTransaction();
      }
    }

    // Clear up
    currentProxy = null;

    proxyPane.removeAll();

    // Layout change !
    proxyPane.revalidate();
    proxyPane.repaint();

    // Done
  }
  
  /**
   * the selected properties
   */
  private Property[] getSelection() {
    
    // check selection
    TreePath paths[] = tree.getSelectionPaths();
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
   * Fill the action-menu
   */
  private class ActionMenu extends ActionDelegate {
    /**
     * Constructor
     */
    private ActionMenu() {
      setText(resources.getString("action"));
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      
      // prepare menu for actions
      JMenu menu = (JMenu)target;
      menu.removeAll();
      MenuHelper mh = new MenuHelper().setTarget(EditView.this).pushMenu(menu);
      
      // check selection
      Property[] selection = getSelection();
      ContextPopupSupport.Context context = new Context(
        selection.length==1 ? (Object)selection[0] : (Object)getCurrentEntity()
      );
      ViewManager.getInstance().fillContextMenu(mh, gedcom, context);
      
      // done
    }

  } //ActionMenu
  
  /**
   * Action - toggle
   */
  private class ActionSticky extends ActionDelegate {
    /** constructor */
    protected ActionSticky() {
      super.setImage(Images.imgStickOff);
      super.setToggle(Images.imgStickOn);
      super.setTip("tip.stick");
    }
    /** run */
    protected void execute() {
      //noop
    }
  } //ActionBack

  /**
   * Action - back
   */
  private class ActionBack extends ActionDelegate {
    /** constructor */
    protected ActionBack() {
      super.setImage(Images.imgReturn).setTip("tip.return");
    }
    /** run */
    protected void execute() {
      tree.getPropertyModel().setPrevious();
      tree.expandRows();
    }
  } //ActionBack
  
  /**
   * Action - add
   */
  private class ActionPropertyAdd extends ActionDelegate {
    /** constructor */
    protected ActionPropertyAdd() {
      super.setShortText("action.add");
      super.setImage(Images.imgAdd);
      super.setTip("tip.add_prop");
    }
    /** run */
    protected void execute() {
  
      // Depending on Gedcom of current entity
      Entity entity = getCurrentEntity();
      if (entity==null) return;
  
      Gedcom gedcom = entity.getGedcom();
  
      // .. Stop Editing
      stopEdit(true);
  
      // .. only in case of single selection
      TreePath paths[] = tree.getSelectionPaths();
      if ( (paths==null) || (paths.length!=1) ) return;
      TreePath path = tree.getSelectionPath();
  
      // .. calculate new props
      Property prop = (Property)path.getLastPathComponent();
  
      // .. Confirm
      ChoosePropertyBean choose = new ChoosePropertyBean(MetaProperty.get(prop).getSubs(false), resources);
      JCheckBox check = new JCheckBox(resources.getString("add.default_too"),true);
  
      Object[] message = new Object[3];
      message[0] = resources.getString("add.choose");
      message[1] = choose;
      message[2] = check;
  
      int option = JOptionPane.showOptionDialog(
        EditView.this,
        message,
        resources.getString("add.title"),
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null, null, null
      );
  
      // .. OK or Cancel ?
      if (option != JOptionPane.OK_OPTION) return;
  
      // .. Calculate chosen properties
      Property[] props = choose.getResultingProperties(check.isSelected());
  
      if ( (props==null) || (props.length==0) ) {
        JOptionPane.showMessageDialog(
          EditView.this,
          resources.getString("add.must_enter"),
          resources.getString("error"),
          JOptionPane.ERROR_MESSAGE
        );
        return;
      }
  
      // .. add properties
      if (!gedcom.startTransaction()) return;
      for (int i=0;i<props.length;i++) prop.addProperty(props[i]);
      gedcom.endTransaction();
     
      // .. select added
      Property select = props[0];
      if (select instanceof PropertyEvent) {
        Property pdate = ((PropertyEvent)select).getDate(false);
        if (pdate!=null) select = pdate;
      }
      tree.setSelectionPath(new TreePath(entity.getProperty().getPathTo(select)));
      
      // done
    }

  } //ActionPropertyAdd
    
  /**
   * Action - del
   */
  private class ActionPropertyDel extends ActionDelegate {
    /** constructor */
    protected ActionPropertyDel() {
      super.setShortText("action.del").setTip("tip.del_prop");
      super.setImage(Images.imgDelete);
    }
    /** run */
    protected void execute() {
  
      // .. Stop Editing
      stopEdit(true);
      
      // go through selection
      Property[] ps = getSelection();
      for (int i=0;i<ps.length;i++) {
        new DelProperty(ps[i]).setTarget(EditView.this).trigger();
      }
  
      // go to parent property
      tree.setSelectionRow(0);
  
      // .. done
    }
  } //ActionPropertyDel
  
  /**
   * Action - up/down
   */
  private class ActionPropertyUpDown extends ActionDelegate {
    /** which */
    boolean up;
    /** constructor */
    protected ActionPropertyUpDown(boolean up) {
      this.up=up;
      if (up) super.setShortText("action.up").setTip("tip.up_prop").setImage(Images.imgUp);
      else super.setShortText("action.down").setTip("tip.down_prop").setImage(Images.imgDown);
    }
    /** run */
    protected void execute() {
      
      // get current property
      Property prop = tree.getCurrentProperty();
      if (prop==null) return;
  
      // .. Stop Editing
      stopEdit(true);
  
      // .. LockWrite
      if (!gedcom.startTransaction()) return;
  
      // .. Calculate property that is moved
      getCurrentEntity().getProperty().moveProperty(
        prop,
        up? Property.UP : Property.DOWN
      );
  
      // .. UnlockWrite
      gedcom.endTransaction();
  
      // Expand the rows
      tree.expandRows();
  
      // Reselect the property
      tree.setSelectionPath(new TreePath(prop.getEntity().getProperty().getPathTo(prop)));
      
      // Done  
    }
    
  }  //ActionPropertyUpDown

  /**
   * Class for rendering tree cell nodes
   */
  private class PropertyTree extends JTree implements TreeCellRenderer, TreeSelectionListener, TreeModelListener {
    
    /** a label for rendering */
    private HeadlessLabel label = new HeadlessLabel();
    
    /**
     * Constructor
     */
    private PropertyTree() {
      super(new PropertyTreeModel(gedcom));
      label.setOpaque(true);
      ToolTipManager.sharedInstance().registerComponent(this);
      setCellRenderer(this);
      getSelectionModel().addTreeSelectionListener(this);
      getModel().addTreeModelListener(this);
    }
    
    /**
     * @see javax.swing.JComponent#removeNotify()
     */
    public void removeNotify() {
      getModel().removeTreeModelListener(this);
      ((PropertyTreeModel)getModel()).destructor();
      super.removeNotify();
    }

    /**
     * Access to the underlying model
     */
    private PropertyTreeModel getPropertyModel() {
      return (PropertyTreeModel)getModel();
    }

    /**
     * Prepare the tree-model that lies behind the tree.
     */
    private void setEntity(Entity entity) {
      getPropertyModel().setEntity(entity);
      expandRows();
    }
    
    /** 
     * Expands all rows
     */
    private void expandRows() {
       for (int i=0;i<getRowCount();i++) expandRow(i);
    }
    
    /**
     * returns the currently selected property
     */
    private Property getCurrentProperty() {
      // Calculate selection path
      TreePath path = tree.getSelectionPath();
      if (path==null) return null;
      // got it
      if (path.getLastPathComponent() instanceof Property)
        return (Property)path.getLastPathComponent();
      // none found
      return null;
    }
    
    /**
     * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(JTree, Object, boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object object, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

      if (!(object instanceof Property)) {
        label.setText(null);
        label.setIcon(null);
        return label;
      }

      // only accepting properties here
      Property prop = (Property)object;

      // Set the text
      String tag = prop.getTag();
      if (prop instanceof Entity)
        tag = "@"+((Entity)prop).getId()+"@ "+tag;

      String value = prop.getValue();
      if (value==null) label.setText(tag);
      else label.setText(tag + ' ' + value);
      
      // Set the image
      label.setIcon(prop.getImage(true));
      
      // background
      Color fg,bg;
      if (selected) {
        bg = UIManager.getColor("Tree.selectionBackground");
        fg = UIManager.getColor("Tree.selectionForeground");
      } else {
        bg = UIManager.getColor("Tree.textBackground");
        fg = UIManager.getColor("Tree.textForeground");
      }
      label.setBackground(bg);
      label.setForeground(fg);
      
      // font
      if (prop.isTransient()) {
        label.setFont(getFont().deriveFont(Font.ITALIC));
      } else {
        label.setFont(getFont());
      }

      // Done
      return label;
    }

    /**
     * @see javax.swing.JTree#getToolTipText(MouseEvent)
     */
    public String getToolTipText(MouseEvent event) {
      // .. calc path to node under mouse
      TreePath path = super.getPathForLocation(event.getX(),event.getY());
      if ((path==null) || (path.getPathCount()==0)) return null;
      // .. calc property
      Object p = path.getLastPathComponent();
      if (!(p instanceof Property)) return "";
      // .. transient?
      if (((Property)p).isTransient()) return null;
      // .. calc information text
      String info = MetaProperty.get((Property)p).getInfo();
      if (info==null) return "?";
      // .. return max 60
      info = info.replace('\n',' ');
      if (info.length()<=60)
        return info;
      return info.substring(0,60)+"...";
    }
    
    /**
     * @see javax.swing.event.TreeSelectionListener#valueChanged(TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent e) {
      
      // stop editing
      stopEdit(true);
  
      // Look if exactly one node has been selected
      if (tree.getSelectionCount()==0||getCurrentProperty()==null) {
        // Disable action buttons
        actionButtonAdd   .setEnabled(false);
        actionButtonRemove.setEnabled(false);
        actionButtonUp    .setEnabled(false);
        actionButtonDown  .setEnabled(false);
        // Done
        return;
      }
  
      if (tree.getSelectionCount()>1) {
        // En/Disable action buttons
        actionButtonAdd   .setEnabled(false);
        actionButtonRemove.setEnabled(true );
        actionButtonUp    .setEnabled(false);
        actionButtonDown  .setEnabled(false);
        // Done
        return;
      }
  
      // Starting with new one
      startEdit(false);
  
      // Enable action buttons
      Property prop = getCurrentProperty();
      
      actionButtonAdd.setEnabled(true);
      if (prop.getParent()!=null) {
        actionButtonRemove.setEnabled(true);
      } else {
        actionButtonRemove.setEnabled(false);
      }
  
      actionButtonUp    .setEnabled(prop.getPreviousSibling()!=null);
      actionButtonDown  .setEnabled(prop.getNextSibling()    !=null);
  
      // Done
    }
    
    /**
     * @see javax.swing.event.TreeModelListener#treeNodesChanged(TreeModelEvent)
     */
    public void treeNodesChanged(TreeModelEvent e) {
      treeStructureChanged(e);
    }
    /**
     * @see javax.swing.event.TreeModelListener#treeNodesInserted(TreeModelEvent)
     */
    public void treeNodesInserted(TreeModelEvent e) {
      treeStructureChanged(e);
    }
    /**
     * @see javax.swing.event.TreeModelListener#treeNodesRemoved(TreeModelEvent)
     */
    public void treeNodesRemoved(TreeModelEvent e) {
      treeStructureChanged(e);
    }
    /**
     * @see javax.swing.event.TreeModelListener#treeStructureChanged(TreeModelEvent)
     */
    public void treeStructureChanged(TreeModelEvent e) {
      // show all rows 
      expandRows();
    }
    
  } //PropertyTree  

} //EditView
