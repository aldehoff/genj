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

import genj.edit.beans.PropertyBean;
import genj.edit.beans.SimpleValueBean;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyXRef;
import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.view.Context;
import genj.view.ViewManager;
import genj.window.CloseWindow;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

/**
 * Our advanced version of the editor allowing low-level
 * access at the Gedcom record-structure
 */
/*package*/ class AdvancedEditor extends Editor {
  
  /** resources */
  private Resources resources = Resources.get(this);

  /** gedcom */
  private Gedcom gedcom;

  /** tree for record structure */
  private PropertyTreeWidget tree = null;

  /** everything for the bean */
  private JPanel            editPane;
  private PropertyBean      bean = null;

  /** splitpane for tree/bean */
  private JSplitPane        splitPane = null;

  /** window manager */
  private WindowManager     winManager;

  /** view manager */
  private ViewManager       viewManager;

  /** actions */
  private ActionDelegate    
    add  = new Add(), 
    cut  = new Cut(), 
    copy = new Copy(), 
    paste= new Paste(), 
    up   = new UpDown(true), 
    down = new UpDown(false),
    ok   = new OK(), 
    cancel = new Cancel();

  /** registry */
  private Registry registry;

  /**
   * Initialize
   */
  public void init(Gedcom ged, ViewManager manager, Registry regty) {
    
    // remember
    gedcom = ged;
    viewManager = manager;
    winManager = viewManager.getWindowManager();
    registry = regty;
    
    // TREE Component's 
    tree = new PropertyTreeWidget(gedcom);

    Callback callback = new Callback();
    tree.addTreeSelectionListener(callback);
    tree.addMouseListener(callback);
    
    JScrollPane treePane = new JScrollPane(tree);
    treePane.setMinimumSize  (new Dimension(160, 128));
    treePane.setPreferredSize(new Dimension(160, 128));
        
    // EDIT Component
    editPane = new JPanel(new BorderLayout());

    // SplitPane with tree/edit
    splitPane = new JSplitPane(
      JSplitPane.VERTICAL_SPLIT, 
      treePane, 
      new JScrollPane(editPane));
    splitPane.setDividerLocation(registry.get("divider",-1));

    // layout
    setLayout(new BorderLayout());
    add(splitPane, BorderLayout.CENTER);
    
    // register as context provider
    manager.registerContextProvider(tree, tree);

    // done    
  }
  
  /**
   * callback - removed
   */
  public void removeNotify() {
    // remember
    registry.put("divider",splitPane.getDividerLocation());
    // continue
    super.removeNotify();
  }


  /**
   * current 
   */
  public Context getContext() {
    return new Context(gedcom, (Entity)tree.getRoot(), tree.getSelection());
  }
  
  /**
   * current 
   */
  public void setContext(Context context) {

    // need entity
    Entity entity = context.getEntity();
    if (entity==null)
      return;
    Property property = context.getProperty();

    tree.setRoot(entity);
    tree.setSelection(property!=null?property:entity);  
  
    // Done
  }
  
  /**
   * Our actions
   */
  public List getActions() {
    ArrayList result = new ArrayList();
    result.add(add);
    result.add(cut);
    result.add(copy);
    result.add(paste);
    result.add(up);
    result.add(down);
    return result;
  }
  
  /**
   * Action - add
   */
  private class Add extends ActionDelegate {
    /** constructor */
    protected Add() {
      super.setShortText("action.add");
      super.setImage(Images.imgAdd);
      super.setTip("action.add.tip");
    }
    /** run */
    protected void execute() {
  
      // check root
      Property root = tree.getRoot();
      if (root==null)
        return;
  
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
  
      int option = winManager.openDialog("add",resources.getString("add.title"),WindowManager.IMG_QUESTION,new JComponent[]{ label, choose, check },CloseWindow.OKandCANCEL(), AdvancedEditor.this); 
      
      // .. not OK?
      if (option!=0)
        return;

      // .. stop current 
      tree.clearSelection();
  
      // .. Calculate chosen properties
      Property[] props = choose.getResultingProperties();
  
      if ( (props==null) || (props.length==0) ) {
        winManager.openDialog(null,null,WindowManager.IMG_ERROR,resources.getString("add.must_enter"),CloseWindow.OK(), AdvancedEditor.this);
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
      tree.setSelectionPath(new TreePath(root.getPathTo(select)));
      
      // done
    }

  } //ActionPropertyAdd
    
  /**
   * Action - up/down
   */
  private class UpDown extends ActionDelegate {
    /** which */
    boolean up;
    /** constructor */
    protected UpDown(boolean up) {
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
      Gedcom gedcom = prop.getGedcom();
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
  private class Copy extends ActionDelegate {
    /** constructor */
    private Copy() {
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
  private class Cut extends Copy {
    /** constructor */
    private Cut() {
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
        // prepare actions
        ActionDelegate[] actions = {
          new CloseWindow(resources.getString("action.cut")), 
          new CloseWindow(CloseWindow.TXT_CANCEL)
        };
        // ask the user
        int rc = winManager.openDialog(null, resources.getString("action.cut.tip"), WindowManager.IMG_WARNING, msg, actions, AdvancedEditor.this );
        if (rc!=0)
          return;
        // continue
      }
      
      // copy first
      super.execute();
      
      // now cut
      Gedcom gedcom = prop.getGedcom();
      gedcom.startTransaction();
      prop.getParent().delProperty(prop);
      gedcom.endTransaction();
      // done
    }
  } //ActionCut

  /**
   * Action - paste
   */
  private class Paste extends ActionDelegate {
    /** constructor */
    private Paste() {
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
        
      // prepare actions
      ActionDelegate[] actions = {
        new CloseWindow(resources.getString("action.paste")), 
        new CloseWindow(CloseWindow.TXT_CANCEL)
      };
      
      // ask
      if (0!=winManager.openDialog(null, resources.getString("action.paste.tip"), WindowManager.IMG_QUESTION, new JScrollPane(new PropertyTreeWidget(copy)), actions, AdvancedEditor.this)) 
        return;        
        
      // paste contents
      Gedcom gedcom = prop.getGedcom();
      gedcom.startTransaction();
      Property result = null;
      try {
        result = copy.paste(prop);
      } catch (GedcomException e) {
        winManager.openDialog(null,resources.getString("action.paste.tip"),WindowManager.IMG_WARNING,e.getMessage(),CloseWindow.OK(), AdvancedEditor.this);
      }
      gedcom.endTransaction();
      
      // propagate
      if (result!=null)
        viewManager.setContext(new Context(result));
      
      // done
    }
  } //ActionPaste

  /**
   * A ok action
   */
  private class OK extends ActionDelegate {
  
    /** constructor */
    private OK() {
      setText(CloseWindow.TXT_OK);
    }
  
    /** cancel current proxy */
    protected void execute() {
  
      Property root = tree.getRoot();
      if (root==null)
        return;
      Gedcom gedcom = root.getGedcom();
  
      if (bean!=null) try {
        gedcom.startTransaction();
        bean.commit();
      } finally {
        gedcom.endTransaction();
      }
        
      ok.setEnabled(false);
      cancel.setEnabled(false);
    }
  
  } //OK
  
  /**
   * A cancel action
   */
  private class Cancel extends ActionDelegate {
  
    /** constructor */
    private Cancel() {
      setText(CloseWindow.TXT_CANCEL);
    }
  
    /** cancel current proxy */
    protected void execute() {
      // disable ok&cancel
      ok.setEnabled(false);
      cancel.setEnabled(false);
      // simulate a selection change
      Property p = tree.getSelection();
      tree.clearSelection();
      tree.setSelection(p);
    }
  
  } //Cancel
  
  /**
   * Handling selection of properties
   */
  private class Callback extends MouseAdapter implements TreeSelectionListener {
    
    /**
     * callback - mouse doubleclick
     */
    public void mouseClicked(MouseEvent e) {
      // double-click?
      if (e.getClickCount()<2)
        return;
      // check against selected property
      Property prop = tree.getPropertyAt(e.getPoint());
      if (prop==null||prop!=tree.getSelection())
        return;
      // propagate any reference
      if (prop instanceof PropertyXRef) {
        Property target = ((PropertyXRef)prop).getTarget();
        if (target!=null)
          viewManager.setContext(new Context(target));
      }
    }
  
    /**
     * callback - selection in tree has changed
     */
    public void valueChanged(TreeSelectionEvent e) {

      // current root
      Property root = tree.getRoot();
      if (root!=null) {
  
        Gedcom gedcom = root.getGedcom();
  
        // ask user for commit if
        if (!gedcom.isTransaction()&&bean!=null&&ok.isEnabled()) {
          if (0==winManager.openDialog(null, null, WindowManager.IMG_QUESTION, resources.getString("confirm.keep.changes"), CloseWindow.YESandNO(), editPane))
            ok.trigger();
        }
  
      }
  
      // Clean up
      bean = null;
      editPane.removeAll();
      editPane.revalidate();
      editPane.repaint();
  
      // Check for 'no selection'
      Property prop = tree.getSelection(); 
      if (prop==null) {
  
        // Disable action buttons
        add  .setEnabled(false);
        up   .setEnabled(false);
        down .setEnabled(false);
        cut  .setEnabled(false);
        copy .setEnabled(false);
        paste.setEnabled(false);
        
        // Done
        return;
      }
      
      // Starting with new one
      if (!prop.isSecret()) {
  
        // get a bean for property
        try {
          bean = (PropertyBean) Class.forName( "genj.edit.beans." + prop.getProxy() + "Bean").newInstance();
        } catch (Throwable t) {
          bean = new SimpleValueBean();
        }
        
        try {
  
          // add bean to center of editPane 
          editPane.add(bean, BorderLayout.CENTER);
  
          // initialize bean
          bean.init(prop, viewManager, registry);
          
          // and a label to the top
          final JLabel label = new JLabel(bean.getLabel(), bean.getImage(), SwingConstants.LEFT);
          editPane.add(label, BorderLayout.NORTH);
  
          // and actions to the bottom
          if (bean.isEditable()) {
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            ButtonHelper bh = new ButtonHelper().setInsets(0).setContainer(buttons).setFocusable(false);
            bh.create(ok);
            bh.create(cancel);
            editPane.add(buttons, BorderLayout.SOUTH);
          }
          
          // listen to it
          bean.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
              label.setText(bean.getLabel());
              label.setIcon(bean.getImage());
              ok.setEnabled(true);
              cancel.setEnabled(true);
            }
          });
  
          // and request focus
          bean.requestFocusInWindow();
          
        } catch (Throwable t) {
          Debug.log(Debug.WARNING, this, "Property bean "+bean+" failed with "+t.getMessage(), t);
        }
        
        // start without ok and cancel
        ok.setEnabled(false);
        cancel.setEnabled(false);
      }
  
      // add      
      add  .setEnabled(true);
      
      // cut,copy,paste
      cut  .setEnabled(prop.getParent()!=null&&!prop.isTransient());
      copy .setEnabled(prop.getParent()!=null&&!prop.isTransient());
      paste.setEnabled(Clipboard.getInstance().getCopy()!=null);
  
      // up, down
      int i=0,j=0;  
      if (prop!=tree.getRoot()) {        
        Property parent = prop.getParent();
        i = tree.getModel().getIndexOfChild(parent, prop);
        j = tree.getModel().getChildCount(parent);
      }
  
      up   .setEnabled(i>0);
      down .setEnabled(i<j-1);
  
      // tell everyone
      viewManager.setContext(getContext());
  
      // Done
    }
  
  } //Callback  

} //AdvancedEditor
