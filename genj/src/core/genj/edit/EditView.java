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

import genj.edit.actions.DelProperty;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.view.ContextSupport;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

/**
 * Component for editing genealogic entity properties
 */
public class EditView extends JPanel implements ToolBarSupport, ContextSupport {

  /** the gedcom we're looking at */
  private Gedcom    gedcom;
  
  /** the registry we use */
  /*package*/ Registry registry;

  /** the view manager */
  /*package*/ ViewManager manager;
  
  /** the resources we use */
  static final Resources resources = Resources.get(EditView.class);

  /** buttons we use */
  private AbstractButton    actionButtonAdd,
                            actionButtonRemove,
                            actionButtonUp,
                            actionButtonDown,
                            actionButtonReturn,
                            actionSticky;

  /** everything for the tree */
  /*package*/ PropertyTreeWidget tree = null;
  
  /** everything for the proxy */
  private JPanel            proxyPane;
  private Proxy             currentProxy = null;

  /** splitpane for tree/proxy */
  private JSplitPane        splitPane = null;
  
  /**
   * Constructor
   */
  public EditView(String title, Gedcom setGedcom, Registry setRegistry, ViewManager setManager) {
    
    // remember
    this.gedcom   = setGedcom;
    this.registry = setRegistry;
    this.manager  = setManager;

    // TREE Component's 
    TreeCallbackHandler callback = new TreeCallbackHandler();
    tree = new PropertyTreeWidget(setGedcom);
    tree.addTreeSelectionListener(callback);
    
    JScrollPane treePane = new JScrollPane(tree);
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
    
    // Done
  }

  /**
   * @see javax.swing.JComponent#addNotify()
   */
  public void addNotify() {
    
    // 20030505 in a desktop pane removeNotify()
    // and addNotify() can happen during z-reordering
    
    // Check if we can preset something to edit
    Entity entity = null;
    try { 
      entity = gedcom.getEntity(registry.get("last",(String)null)); 
    } catch (Exception e) {
    }
    if (entity==null) {
      Property context = manager.getContext(gedcom); 
      if (context!=null) entity = context.getEntity();
    }
    setEntity(entity);

    // continue
    super.addNotify();    
  }

  /**
   * Notification when component is not used any more
   */
  public void removeNotify() {
    
    // stop editing
    stopEdit();

    // Remember registry
    registry.put("divider",splitPane.getDividerLocation());
    registry.put("last", getCurrentEntity()!=null?getCurrentEntity().getId():"");
    registry.put("sticky", isSticky());

    // Continue
    super.removeNotify();

    // Done
  }

  /**
   * @see genj.view.ContextPopupSupport#setContext(genj.gedcom.Property)
   */
  public void setContext(Property property) {
    if (!isSticky()) {
      tree.setProperty(property);
    } 
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
   * returns the currently viewed entity
   */
  /*package*/ Entity getCurrentEntity() {
    return tree.getEntity();
  }
  
  /**
   * Set the entity to display
   */
  /*pacakge*/ void setEntity(Entity entity) {

    // already?
    if (getCurrentEntity()==entity) return;

    // Try to stop old editing first
    stopEdit();

    // Reset tree model
    tree.setEntity(entity);

    // Done
  }
  
  /**
   * Change sticky status
   */
  public boolean setSticky(boolean set) {
    boolean result = actionSticky.isSelected();
    actionSticky.setSelected(set);
    return result;
  }

  /**
   * Check sticky status
   */
  public boolean isSticky() {
    return actionSticky!=null && actionSticky.isSelected();
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

    if (proxy == "") return;

    // Create proxy
    try {
      if (keepSimple)
        throw new Exception();
      currentProxy = (Proxy) Class.forName( pkg + "Proxy" + proxy ).newInstance();
    } catch (Exception e) {
      currentProxy = new ProxyUnknown();
    }

    // Add proxy components
    try {
      JComponent focus = currentProxy.start(proxyPane,prop,this);
      if (focus!=null) focus.requestFocus();
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
  private void stopEdit() {

    // Prepare for finishing changed and finish
    if (currentProxy!=null) {
      if (currentProxy.hasChanged()) {
        Gedcom gedcom = getCurrentEntity().getGedcom();
        if (gedcom.startTransaction()) {
          currentProxy.finish();
          gedcom.endTransaction();
        }
      } else {
        currentProxy.finish();
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
      tree.setPreviousEntity();
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
      stopEdit();
  
      // .. only in case of single selection
      TreePath paths[] = tree.getSelectionPaths();
      if ( (paths==null) || (paths.length!=1) ) return;
      TreePath path = tree.getSelectionPath();
  
      // .. calculate new props
      Property prop = (Property)path.getLastPathComponent();
  
      // .. Confirm
      ChoosePropertyBean choose = new ChoosePropertyBean(prop, resources);
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
        startEdit(false);
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
        return;
      }
  
      // .. add properties
      if (!gedcom.startTransaction()) return;
      
      for (int i=0;i<props.length;i++) {
        Property newProp = prop.addProperty(props[i]);
        if (check.isSelected()) newProp.addDefaultProperties();
      } 
      gedcom.endTransaction();
     
      // .. select added
      Property select = props[0];
      if (select instanceof PropertyEvent) {
        Property pdate = ((PropertyEvent)select).getDate(false);
        if (pdate!=null) select = pdate;
      }
      tree.setSelectionPath(new TreePath(entity.getPathTo(select)));
      
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
      stopEdit();
      
      // go through selection
      Property[] ps = tree.getSelection();
      for (int i=0;i<ps.length;i++) {
        new DelProperty(ps[i], manager).setTarget(EditView.this).trigger();
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
      stopEdit();
  
      // .. LockWrite
      if (!gedcom.startTransaction()) return;
  
      // .. Calculate property that is moved
      prop.move(up ? -1 : 1);
  
      // .. UnlockWrite
      gedcom.endTransaction();
  
      // Expand the rows
      tree.expandRows();
  
      // Reselect the property
      tree.setSelectionPath(new TreePath(prop.getEntity().getPathTo(prop)));
      
      // Done  
    }
    
  }  //ActionPropertyUpDown

  /**
   * Handling selection of properties
   */
  private class TreeCallbackHandler implements TreeSelectionListener {
    
    /**
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent e) {

      // stop editing
      stopEdit();
  
      // Look if exactly one node has been selected
      if (tree.getSelectionCount()==0||tree.getCurrentProperty()==null) {
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
      Property prop = tree.getCurrentProperty();
      
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
    
  } //PropertyTree  

  
} //EditView
