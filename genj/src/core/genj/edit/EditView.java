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
import genj.gedcom.GedcomException;
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
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
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
  
  /** the title we have */
  private String title;
  
  /** the registry we use */
  /*package*/ Registry registry;

  /** the view manager */
  /*package*/ ViewManager manager;
  
  /** the resources we use */
  static final Resources resources = Resources.get(EditView.class);

  /** buttons we use */
  private AbstractButton    actionButtonAdd,
                            actionButtonCut,
                            actionButtonCopy,
                            actionButtonPaste,
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
  public EditView(String setTitle, Gedcom setGedcom, Registry setRegistry, ViewManager setManager) {
    
    // remember
    title    = setTitle;
    gedcom   = setGedcom;
    registry = setRegistry;
    manager  = setManager;

    // TREE Component's 
    tree = new PropertyTreeWidget(setGedcom);
    tree.addTreeSelectionListener(new SelectionChange());
    
    JScrollPane treePane = new JScrollPane(tree);
    treePane.setMinimumSize  (new Dimension(160, 128));
    treePane.setPreferredSize(new Dimension(160, 128));
    
    // EDIT Component
    proxyPane = new JPanel();

    // SplitPane with tree/edit
    splitPane = new JSplitPane(
      JSplitPane.VERTICAL_SPLIT, 
      treePane, 
      new JScrollPane(proxyPane));
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
      tree.setRoot(property.getEntity());
      tree.setSelection(property);
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
    Property prop = tree.getSelection();
    if (prop!=null) 
      return new Context(prop);
    return new Context(getCurrentEntity());
  }
  
  /**
   * @see genj.view.ToolBarSupport#populate(JToolBar)
   */
  public void populate(JToolBar bar) {

    // buttons for property manipulation    
    ButtonHelper bh = new ButtonHelper()
      .setFocusable(false)
      .setEnabled(false)
      .setResources(resources)
      .setInsets(0)
      .setMinimumSize(new Dimension(0,0))
      .setContainer(bar);

    // add new    
    actionButtonAdd    = bh.create(new ActionAdd());
    
    // cut,copy,paste
    actionButtonCut    = bh.create(new ActionCut());
    actionButtonCopy   = bh.create(new ActionCopy());
    actionButtonPaste  = bh.create(new ActionPaste());
    
    // up, down
    actionButtonUp     = bh.create(new ActionUpDown(true));
    actionButtonDown   = bh.create(new ActionUpDown(false));
    
    // return in history
    actionButtonReturn = bh.setEnabled(true).create(new ActionBack());
    
    // toggle sticky
    actionSticky       = bh.create(new ActionSticky());
    actionSticky.setSelected(registry.get("sticky",false));

    // done
  }
  
  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(256,480);
  }
  
  /**
   * returns the currently viewed entity
   */
  /*package*/ Entity getCurrentEntity() {
    Property root = tree.getRoot();
    return root==null ? null : root.getEntity();
  }
  
  /**
   * Set the entity to display
   */
  /*pacakge*/ void setEntity(Entity entity) {

    // already?
    if (getCurrentEntity()==entity) 
      return;

    // Try to stop old editing first
    tree.clearSelection();

    // Reset tree model
    tree.setRoot(entity);

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
   * Action - toggle
   */
  private class ActionSticky extends ActionDelegate {
    /** constructor */
    protected ActionSticky() {
      super.setImage(Images.imgStickOff);
      super.setToggle(Images.imgStickOn);
      super.setTip("action.stick.tip");
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
      super.setImage(Images.imgReturn).setTip("action.return.tip");
    }
    /** run */
    protected void execute() {
      tree.setPrevious();
    }
  } //ActionBack
  
  /**
   * Action - add
   */
  private class ActionAdd extends ActionDelegate {
    /** constructor */
    protected ActionAdd() {
      super.setShortText("action.add");
      super.setImage(Images.imgAdd);
      super.setTip("action.add.tip");
    }
    /** run */
    protected void execute() {
  
      // Depending on Gedcom of current entity
      Entity entity = getCurrentEntity();
      if (entity==null)
        return;
  
      Gedcom gedcom = entity.getGedcom();
  
      // .. only in case of single selection
      TreePath paths[] = tree.getSelectionPaths();
      if ( (paths==null) || (paths.length!=1) ) 
        return;
      TreePath path = tree.getSelectionPath();
  
      // .. calculate new props
      Property prop = (Property)path.getLastPathComponent();
  
      // .. Confirm
      JLabel label = new JLabel(resources.getString("add.choose"));
      ChoosePropertyBean choose = new ChoosePropertyBean(prop, resources);
      JCheckBox check = new JCheckBox(resources.getString("add.default_too"),true);
  
      int option = manager.getWindowManager().openDialog(
        "add", 
        resources.getString("add.title"),
        WindowManager.IMG_QUESTION,
        new JComponent[]{ label, choose, check },
        WindowManager.OPTIONS_OK_CANCEL,
        EditView.this 
      ); 
      
      // .. not OK?
      if (option!=0)
        return;

      // .. stop current 
      tree.clearSelection();
  
      // .. Calculate chosen properties
      Property[] props = choose.getResultingProperties();
  
      if ( (props==null) || (props.length==0) ) {
        manager.getWindowManager().openDialog(
          null,
          null,
          WindowManager.IMG_ERROR,
          resources.getString("add.must_enter"),
          WindowManager.OPTIONS_OK,
          EditView.this
        );
        return;
      }
  
      // .. add properties
      gedcom.startTransaction();
      try {
        for (int i=0;i<props.length;i++) {
          Property newProp = prop.addProperty(props[i]);
          if (check.isSelected()) newProp.addDefaultProperties();
        } 
      } finally {
        gedcom.endTransaction();
      }
         
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
   * Action - up/down
   */
  private class ActionUpDown extends ActionDelegate {
    /** which */
    boolean up;
    /** constructor */
    protected ActionUpDown(boolean up) {
      this.up=up;
      if (up) super.setShortText("action.up").setTip("action.up.tip").setImage(Images.imgUp);
      else super.setShortText("action.down").setTip("action.down.tip").setImage(Images.imgDown);
    }
    /** run */
    protected void execute() {
      
      // get current property
      Property prop = tree.getSelection();
      if (prop==null)
        return;
        
      Property parent = prop.getParent(); // has to be non-null
  
      // .. Stop Editing
      tree.clearSelection();
  
      // .. LockWrite
      gedcom.startTransaction();
  
      // .. Calculate property that is moved
      Property sibling = (Property)tree.getModel().getChild(parent, tree.getModel().getIndexOfChild(parent, prop) + (up?-1:1));
      parent.swapProperties(prop, sibling);
  
      // .. UnlockWrite
      gedcom.endTransaction();
  
      // Expand the rows
      tree.expandRows();
  
      // Reselect the property
      TreePath path = new TreePath(prop.getEntity().getPathTo(prop));
      tree.setSelectionPath(path);
      tree.scrollPathToVisible(path);;
      
      // Done  
    }
    
  }  //ActionPropertyUpDown
  
  /**
   * Action - copy
   */
  private class ActionCopy extends ActionDelegate {
    /** constructor */
    private ActionCopy() {
      setImage(Images.imgCopy);
      setTip("action.copy.tip");
    }
    /** run */
    protected void execute() {
      
      // check selection
      Property prop = tree.getSelection();
      if (prop==null) 
        return; // shouldn't happen
        
      // grab it and its subs
      Clipboard.getInstance().copy(prop);
      
      // deselect to avoid user pasting to same node
      tree.setSelection(prop.getParent());
      
      // done
    }
  } //ActionCopy

  /**
   * Action - cut
   */
  private class ActionCut extends ActionCopy {
    /** constructor */
    private ActionCut() {
      super.setImage(Images.imgCut);
      super.setShortText("action.cut");
      super.setTip("action.cut.tip");
    }
    /** run */
    protected void execute() {
      // check selection
      Property prop = tree.getSelection();
      if (prop==null) 
        return; // shouldn't happen
      // warn about cut
      String veto = prop.getDeleteVeto();
      if (veto!=null) { 
        // Removing property {0} from {1} leads to:\n{2}
        String msg = resources.getString("cut.warning", new String[] { 
          prop.getTag(), prop.getEntity().getId(), veto 
        });
        // ask the user
        int option = manager.getWindowManager().openDialog(
          null,
          resources.getString("action.cut.tip"),
          WindowManager.IMG_WARNING,
          msg,
          new String[] { resources.getString("action.cut"), WindowManager.OPTION_CANCEL },
          EditView.this
        );
        if (option!=0)
          return;
        // continue
      }
      // copy first
      super.execute();
      // now cut
      gedcom.startTransaction();
      prop.getParent().delProperty(prop);
      gedcom.endTransaction();
      // done
    }
  } //ActionCut

  /**
   * Action - paste
   */
  private class ActionPaste extends ActionDelegate {
    /** constructor */
    private ActionPaste() {
      super.setImage(Images.imgPaste);
      super.setShortText("action.paste");
      super.setTip("action.paste.tip");
    }
    /** run */
    protected void execute() {
      
      // check selection
      Property prop = tree.getSelection();
      if (prop==null) 
        return; // shouldn't happen
        
      // safety - check existing copy
      Clipboard.Copy copy = Clipboard.getInstance().getCopy();
      if (copy==null) 
        return; // shouldn't happen
        
      // ask
      if (0!=manager.getWindowManager().openDialog(
        null,
        resources.getString("action.paste.tip"),
        WindowManager.IMG_QUESTION,
        new JScrollPane(new PropertyTreeWidget(copy)),
        new String[]{ resources.getString("action.paste"), WindowManager.OPTION_CANCEL},
        EditView.this
      )) return;        
        
      // paste contents
      gedcom.startTransaction();
      Property result = null;
      try {
        result = copy.paste(prop);
      } catch (GedcomException e) {
        manager.getWindowManager().openDialog(
          null,
          resources.getString("action.paste.tip"),
          WindowManager.IMG_WARNING,
          e.getMessage(),
          WindowManager.OPTIONS_OK,
          EditView.this
        );
      }
      gedcom.endTransaction();
      
      // propagate
      if (result!=null)
        manager.setContext(result);
      
      // done
    }
  } //ActionPaste

  /**
   * Handling selection of properties
   */
  private class SelectionChange implements TreeSelectionListener {
    
    /**
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent e) {

      // ask user for commit if
      //  + currentProxy with changes
      //  + no transaction going on
      if (!gedcom.isTransaction()&&currentProxy!=null&&currentProxy.hasChanged()) {
        if (0==manager.getWindowManager().openDialog(null, title, WindowManager.IMG_QUESTION, "Keep the changes you've made?", WindowManager.OPTIONS_YES_NO, proxyPane))
          currentProxy.ok.doClick();
      }

      // Check for 'no selection'
      Property prop = tree.getSelection(); 
      if (prop==null) {

        // Clean up
        currentProxy = null;
        proxyPane.removeAll();
        proxyPane.revalidate();
        proxyPane.repaint();

        // Disable action buttons
        actionButtonAdd   .setEnabled(false);
        actionButtonUp    .setEnabled(false);
        actionButtonDown  .setEnabled(false);
        actionButtonCut   .setEnabled(false);
        actionButtonCopy  .setEnabled(false);
        actionButtonPaste .setEnabled(false);

        // Done
        return;
      }
  
      // Starting with new one
      if (!prop.isSecret()) {

        // get a proxy for property
        currentProxy = getProxy(prop);
        
        // add proxy components
        try {
          currentProxy.start(proxyPane, prop, EditView.this);
        } catch (ClassCastException ex) {
          Debug.log(Debug.WARNING, this, "Seems like we're getting bad proxy for property "+prop, ex);
        }
        
      }

      // add      
      actionButtonAdd.setEnabled(true);
      
      // cut,copy,paste
      actionButtonCut  .setEnabled(prop.getParent()!=null&&!prop.isTransient());
      actionButtonCopy .setEnabled(prop.getParent()!=null&&!prop.isTransient());
      actionButtonPaste.setEnabled(Clipboard.getInstance().getCopy()!=null);
  
      // up, down
      int i=0,j=0;  
      if (prop!=tree.getRoot()) {        
        Property parent = prop.getParent();
        i = tree.getModel().getIndexOfChild(parent, prop);
        j = tree.getModel().getChildCount(parent);
      }

      actionButtonUp    .setEnabled(i>0);
      actionButtonDown  .setEnabled(i<j-1);
  
      // Done
    }

    /** calculate a proxy for given property */
    private Proxy getProxy(Property prop) {
      
      // calculate args
      String me    = getClass().getName(),
             pkg   = me.substring( 0, me.lastIndexOf(".") + 1 ),
             proxy = prop.getProxy();
          
      // create
      try {
        return (Proxy) Class.forName( pkg + "Proxy" + proxy ).newInstance();
      } catch (Throwable t) {
        return new ProxySimpleValue();
      }
      
      // done
    }
    
  } //Callback  

  
} //EditView
