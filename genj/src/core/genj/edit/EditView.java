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

import genj.app.App;
import genj.gedcom.Change;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.ImgIcon;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImgIconConverter;
import genj.view.CurrentSupport;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 * Component for editing genealogic entity properties
 */
public class EditView extends JSplitPane implements TreeSelectionListener, GedcomListener, CurrentSupport, ToolBarSupport {

  /** the gedcom we're looking at */
  private Gedcom    gedcom;
  
  /** the current entity&property */
  private Entity    currentEntity;
  private Property  currentProperty = null;
  
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
                            actionButtonReturn;

  /** everything for the tree */
  private JTree             tree = null;
  private JScrollPane       treePane = null;
  
  /** everything for the proxy */
  private JPanel            proxyPane;
  private Proxy             currentProxy = null;

  /** stack of entities we've been looking at */
  private final static int  MAX_RETURN  = 10;
  private Stack             returnStack = new Stack();
  private boolean           isSticky = false;
  
  /**
   * Constructor
   */
  public EditView(Gedcom setGedcom, Registry setRegistry, Frame setFrame) {
    
    // remember
    this.gedcom   = setGedcom;
    this.frame    = setFrame;
    this.registry = setRegistry;

    // TREE Component's ScrollPane
    treePane = new JScrollPane();
    treePane.setMinimumSize  (new Dimension(160, 128));
    treePane.setPreferredSize(new Dimension(160, 128));

    // EDIT Component
    proxyPane = new JPanel();
    proxyPane.setLayout(new BoxLayout(proxyPane,BoxLayout.Y_AXIS));

    // layout
    setOrientation(JSplitPane.VERTICAL_SPLIT);
    setTopComponent(treePane);
    setBottomComponent(proxyPane);
    setContinuousLayout(true);
    setDividerLocation(registry.get("divider",-1));
    
    // Listeners
    gedcom.addListener(this);

    // Check if we can preset something to edit
    Entity entity = null;
    String last = registry.get("last",(String)null);
    if (last!=null) {
      try { 
        entity = gedcom.getEntityFromId(last); 
      } catch (Exception e) {
        entity = ViewManager.getInstance().getCurrentEntity();
      }
    }
    setCurrentEntity(entity);
    
    // sticky?
    isSticky = registry.get("sticky",false);

    // Done
  }

  /**
   * Starts a Gedcom transaction
   */
  private boolean startTransaction(String message) {

    // .. LockWrite
    if (gedcom.startTransaction()) {
      return true;
    }

    JOptionPane.showMessageDialog(
      this,
      message,
      resources.getString("error"),
      JOptionPane.ERROR_MESSAGE
    );

    return false;

  }

  /**
   * Ends the current Gedcom transaction
   */
  private void endTransaction() {
    gedcom.endTransaction();
  }

  /**
   * Let proxy flush editing property
   * @return status for successfull flushing of edit
   */
  boolean flushEditing(boolean alreadyLocked) {

    // Finish old editing
    if (currentProxy == null) {
      return true;
    }

    // Prepare for finishing changed and finish
    if (currentProxy.hasChanged()) {
      if (alreadyLocked) {
        currentProxy.finish();
      } else {
        Gedcom gedcom = currentEntity.getGedcom();
        if (gedcom.startTransaction()) {
          currentProxy.finish();
          gedcom.endTransaction();
        } else {
          int result = JOptionPane.showConfirmDialog(
            this,
            "Couldn't save",
            resources.getString("error"),
            JOptionPane.OK_CANCEL_OPTION
          );
          return (result==JOptionPane.CANCEL_OPTION);
        }
      }
    }

    // Done
    return true;
  }

  /**
   * Notification that a change in a Gedcom-object took place.
   */
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
        cancelEditing();
        setEntity(null,true);
        return;
      }

      // continue
    }

    // Property added/removed ?
    if ( change.isChanged(Change.PADD)
       ||change.isChanged(Change.PDEL)) {

      prepareTreeModel();

      // .. select added
      List padd = change.getProperties(Change.PADD);
      if (padd.size()>0) {
        PropertyTreeModel model = (PropertyTreeModel)tree.getModel();
        Property root = (Property)model.getRoot();
        Property first = (Property)padd.get(0);
        if (first instanceof PropertyEvent) {
          Property pdate = ((PropertyEvent)first).getDate(false);
          first = (pdate!=null) ? pdate : first;
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
        return;
      }
    }

    // Done
  }

  /**
   * @see genj.view.CurrentSupport#setCurrentEntity(Entity)
   */
  public void setCurrentEntity(Entity entity) {
    if (!isSticky) {
      setEntity(entity, false);
    }
  }

  /**
   * @see genj.view.CurrentSupport#setCurrentProperty(Property)
   */
  public void setCurrentProperty(Property property) {
    // ignored
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
    actionButtonReturn = bh.create(new ActionBack());

    // sticky checkbox
    JCheckBox actionCheckStick  = new JCheckBox(ImgIconConverter.get(Images.imgStickOff));
    actionCheckStick.setSelectedIcon (ImgIconConverter.get(Images.imgStickOn ));
    actionCheckStick.setFocusPainted(false);
    actionCheckStick.setSelected(isSticky);
    actionCheckStick.setToolTipText(resources.getString("tip.stick"));
    actionCheckStick.setOpaque(false);
    bar.add(actionCheckStick);
    
    // done
  }

  /**
   * Prepare the tree-model that lies behind the tree.
   */
  private void prepareTreeModel() {

    // NM 16 Dec 1998 This is some kind of hack :(
    // When a new model is set for a JTree, it signals a
    // valueChanged to its SelectionListeners, which in this
    // case means (de-)activation of buttons. That leads to
    // a focus change which leads to a redraw of the JTree.
    // When the current model has changed just a little bit
    // (which happens during GedcomWriteLock), a null-Rectangle
    // is generated somewhere in Swing which crashes the repaint 8)
    // We'll detach from JTree for the time the new model is setup
    // and attach later on

    tree.removeTreeSelectionListener(this);
    tree.setModel(new PropertyTreeModel(currentEntity.getProperty()));
    tree.addTreeSelectionListener(this);

    // Expand all nodes
    for (int i=0;i<tree.getRowCount();i++) {
      tree.expandRow(i);
    }

    // Done
  }

  /**
   * Notification when component is not used any more
   */
  public void removeNotify() {

    // Remember registry
    registry.put("divider",getDividerLocation());
    registry.put("last", currentEntity.getId());
    registry.put("sticky", isSticky);

    // Stop Listening
    gedcom.removeListener(this);

    // Continue
    super.removeNotify();

    // Done
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
  public Entity getCurrentEntity() {
    return currentEntity;
  }

  /**
   * An entity in the gedcom data has been selected.
   * This method prepares editing of the selected entity.
   */
  /*package*/ void setEntity(Entity pEntity, boolean returned) {

    // Finish old editing
    if (!stopEditing(false)) {
      return;
    }

    // Put last entity on return-stack
    if ((!returned)&&(currentEntity!=null)) {
      returnStack.addElement(currentEntity);
      if (returnStack.size()>MAX_RETURN) {
        returnStack.removeElementAt(0);
      }
      actionButtonReturn.setEnabled(returnStack.size()>0);
    }

    // Remember entity
    currentEntity=pEntity;

    // Create tree
    if (currentEntity==null) {
      if (tree!=null) {
        tree.removeTreeSelectionListener(this);
        tree = null;
      }
    } else {
      // .. create the tree
      tree = new JTree() {
        // LCD
        /** Calculate ToopTipText depending on property under mouse */
        public String getToolTipText(MouseEvent event) {
          // .. calc path to node under mouse
          TreePath path = getPathForLocation(event.getX(),event.getY());
          if ((path==null) || (path.getPathCount()==0))
          return null;
          // .. calc property
          Property p = (Property)path.getLastPathComponent();
          // .. calc information text
          String info = p.getInfo();
          if (info==null) {
            return "Unknown";
          }
          // .. return max 60
          info = info.replace('\n',' ');
          if (info.length()<=60)
            return info;
          return info.substring(0,60)+"...";
        }
        // EOC
      };
      ToolTipManager.sharedInstance().registerComponent(tree);

      // .. prepare data
      prepareTreeModel();

      // .. prepare rendering
      tree.setCellRenderer(new PropertyCellRenderer());

      // .. done
    }


    // Update view
    treePane.getViewport().setView(tree);
    treePane.validate();
    treePane.repaint();

    // Pre-selected editing node ?
    if ((currentEntity!=null)&&(tree.isShowing())) {
      tree.setSelectionRow( 0 );
    }

    // Done
  }

  /**
   * Prepare a proxy for editing a property
   */
  private void startEditingOf(Property prop, boolean keepSimple) {

    // New prop ?
    if (prop == null) {
      return;
    }

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
    label.setIcon(ImgIconConverter.get(prop.getImage(true)));
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
   * Cancels proxy from editing
   */
  void cancelEditing() {

    // Clear up
    currentProxy = null;
    proxyPane.removeAll();

    // Layout change !
    proxyPane.invalidate();
    proxyPane.validate();
    proxyPane.repaint();

  }

  /**
   * Stop proxy from editing property
   * @return status for successfull stop of edit
   */
  boolean stopEditing(boolean alreadyLocked) {

    // Finish old editing
    if (currentProxy == null) {
      return true;
    }

    if (!flushEditing(alreadyLocked)) {
      return false;
    }

    cancelEditing();

    // Done
    return true;
  }

  /**
   * Called when the user changes a selection.
   * Changes the current proxy.
   */
  public void valueChanged(TreeSelectionEvent e) {

    // Look if exactly one node has been selected
    if (tree.getSelectionCount()==0) {
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

    // Calculate selection path
    TreePath path = tree.getSelectionPath();

    // Prepare proxy for editing propery behind that single node
    Property prop = (Property)path.getLastPathComponent();
    currentProperty = prop;

    // Stop editing via old proxy before starting with new one
    if (!stopEditing(false)) {
      // .. stop here
      return;
    }
    startEditingOf(prop,false);

    // Enable action buttons
    actionButtonAdd.setEnabled(true);
    if (path.getPathCount() > 1) {
      actionButtonRemove.setEnabled(true);
    } else {
      actionButtonRemove.setEnabled(false);
    }

    actionButtonUp    .setEnabled(currentProperty.getPreviousSibling()!=null);
    actionButtonDown  .setEnabled(currentProperty.getNextSibling()    !=null);

    // Done
  }
  
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
  
      // Return to last entity from return-stack
      int last = returnStack.size()-1;
      if (last==-1) {
        return;
      }

      Entity old = (Entity)returnStack.elementAt(last);
      returnStack.removeElementAt(last);
      setEntity(old,true);
      actionButtonReturn.setEnabled(returnStack.size()>0);

      // .. done
    }
  } //ActionBack
  
  /**
   * Action - add
   */
  private class ActionPropertyAdd extends ActionDelegate {
    /** constructor */
    protected ActionPropertyAdd() {
      super.setText("action.add");
      super.setTip("tip.add_prop");
    }
    /** run */
    protected void execute() {
  
      // Depending on Gedcom of current entity
      if (currentEntity==null)
        return;
  
      Gedcom gedcom = currentEntity.getGedcom();
  
      // .. LockWrite
      if (!startTransaction("Couldn't save")) {
        return;
      }
  
      // .. Stop Editing
      flushEditing(true);
  
      // .. only in case of single selection
      TreePath paths[] = tree.getSelectionPaths();
      if ( (paths==null) || (paths.length!=1) ) {
        endTransaction();
        return;
      }
      TreePath path = tree.getSelectionPath();
  
      // .. calculate new props
      Property prop = (Property)path.getLastPathComponent();
  
      // .. Confirm
      ChoosePropertyBean choose = new ChoosePropertyBean(prop.getKnownProperties(),resources);
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
      if (option != JOptionPane.OK_OPTION) {
        gedcom.endTransaction();
        return;
      }
  
      // .. Calculate chosen properties
      Property[] props = choose.getResultingProperties();
  
      if ( (props==null) || (props.length==0) ) {
        JOptionPane.showMessageDialog(
          EditView.this,
          resources.getString("add.must_enter"),
          resources.getString("error"),
          JOptionPane.ERROR_MESSAGE
        );
        gedcom.endTransaction();
        return;
      }
  
      // .. add properties
      boolean doSub = check.isSelected();
      for (int i=0;i<props.length;i++) {
        if (doSub) {
          props[i].addDefaultProperties();
        }
        prop.addProperty(props[i]);
      }
  
      // .. UnlockWrite
      endTransaction();
    }

  } //ActionPropertyAdd
    
  /**
   * Action - del
   */
  private class ActionPropertyDel extends ActionDelegate {
    /** constructor */
    protected ActionPropertyDel() {
      super.setText("action.del").setTip("tip.del_prop");
    }
    /** run */
    protected void execute() {
  
      TreePath paths[] = tree.getSelectionPaths();
      boolean changed = false;
  
      // .. check if there are some selections
      if ( (paths==null) || (paths.length==0) ) {
        return;
      }
  
      // .. LockWrite
      if (!gedcom.startTransaction()) {
        JOptionPane.showMessageDialog(
          EditView.this,
          "Couldn't save",
          resources.getString("error"),
          JOptionPane.ERROR_MESSAGE
        );
        return;
      }
  
      // .. Stop Editing
      stopEditing(true);
  
      // .. remove every selected node
      for (int i=0;i<paths.length;i++) {
  
        Property prop = (Property)paths[i].getLastPathComponent();
        String veto = prop.getDeleteVeto();
  
        if (veto!=null) {
  
          JTextPane tp = new JTextPane();
          tp.setText(veto);
          tp.setEditable(false);
          JScrollPane sp = new JScrollPane(tp,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
            public Dimension getPreferredSize() {
              return new Dimension(128,64);
            }
          };
  
          Object message[] = new Object[2];
          message[0] = resources.getString("del.leads_to",prop.getTag());
          message[1] = sp;
  
          // Show veto and respect user choice
          int rc = JOptionPane.showConfirmDialog(
            EditView.this,
            message,
            resources.getString("warning"),
            JOptionPane.OK_CANCEL_OPTION
          );
          if (rc==JOptionPane.OK_OPTION)
            veto=null;
  
          // Continue with/without veto
        }
  
        if (veto==null) {
          currentEntity.getProperty().delProperty( prop );
          changed = true;
        }
  
      // Next selected prop
      }
  
      // .. UnlockWrite
      gedcom.endTransaction();
  
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
      if (up) super.setText("action.up").setTip("tip.up_prop");
      else super.setText("action.down").setTip("tip.down_prop");
    }
    /** run */
    protected void execute() {
  
      // .. LockWrite
      if (!gedcom.startTransaction()) {
        JOptionPane.showMessageDialog(
          EditView.this,
          "Couldn't save",
          resources.getString("error"),
          JOptionPane.ERROR_MESSAGE
        );
        return;
      }
  
      // .. Stop Editing
      flushEditing(true);
  
      // .. Calculate property that is moved
      currentEntity.getProperty().moveProperty(
        currentProperty,
        up? Property.UP : Property.DOWN
      );
  
      // .. UnlockWrite
      gedcom.endTransaction();
  
      // 03.02.2000 Since the movement of properties is not
      // signalled by any event, we have to reselect the node again
      prepareTreeModel();
      TreePath path = new TreePath(currentEntity.getProperty().getPathTo(currentProperty));
      tree.setSelectionPath(path);
  
    }
    
  }  //ActionPropertyUpDown

  /**
   * Class for rendering tree cell nodes
   */
  private class PropertyCellRenderer extends JLabel implements TreeCellRenderer {
    
    /**
     * Constructor
     */
    private PropertyCellRenderer() {
      setOpaque(true);
      setFont(tree.getFont());
    }

    /**
     * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(JTree, Object, boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object object, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

      // we know there's a property here
      Property prop = (Property)object;

      // Set the text
      String tag = prop.getTag();
      if (prop instanceof Entity)
        tag = "@"+((Entity)prop).getId()+"@ "+tag;

      String value = prop.getValue();
      if (value==null) setText(tag);
      else setText(tag + ' ' + value);
      
      // Set the image
      setIcon(ImgIconConverter.get(prop.getImage(true)));
      
      // background
      if (selected) {
        setBackground(UIManager.getColor("Tree.selectionBackground"));
        setForeground(UIManager.getColor("Tree.selectionForeground"));
      } else {
        setBackground(UIManager.getColor("Tree.textBackground"));
        setForeground(UIManager.getColor("Tree.textForeground"));
      }

      // Done
      return this;
    }
  } //PropertyCellRenderer
  
}
