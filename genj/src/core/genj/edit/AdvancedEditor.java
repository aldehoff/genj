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
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Transaction;
import genj.io.GedcomReader;
import genj.io.GedcomWriter;
import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.view.Context;
import genj.window.CloseWindow;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

/**
 * Our advanced version of the editor allowing low-level
 * access at the Gedcom record-structure
 */
/*package*/ class AdvancedEditor extends Editor {
  
  private final static Clipboard clipboard = initClipboard();

  /**
   * Initialize clipboard - trying system falling back to private
   */
  private static Clipboard initClipboard() {
    try {
      return Toolkit.getDefaultToolkit().getSystemClipboard();
    } catch (Throwable t) {
      return new Clipboard("GenJ");
    }

  }
  
  /** resources */
  private static Resources resources = Resources.get(AdvancedEditor.class);

  /** gedcom */
  private Gedcom gedcom;

  /** tree for record structure */
  private PropertyTreeWidget tree = null;

  /** everything for the bean */
  private JPanel            editPane;
  private PropertyBean      bean = null;

  /** splitpane for tree/bean */
  private JSplitPane        splitPane = null;

  /** view */
  private EditView editView;

  /** actions */
  private ActionDelegate    
    ok   = new OK(), 
    cancel = new Cancel();

  /** registry */
  private Registry registry;
  
  /** interaction callback */
  private InteractionListener callback;

  /**
   * Initialize
   */
  public void init(Gedcom ged, EditView view, Registry regty) {
    
    // remember
    gedcom = ged;
    editView = view;
    registry = regty;
    
    // TREE Component's 
    tree = new PropertyTreeWidget(gedcom);

    callback = new InteractionListener();
    tree.addTreeSelectionListener(callback);
    tree.addMouseListener(callback);
    
    JScrollPane treePane = new JScrollPane(tree);
    treePane.setMinimumSize  (new Dimension(160, 128));
    treePane.setPreferredSize(new Dimension(160, 128));
        
    // EDIT Component
    editPane = new JPanel(new BorderLayout());
    JScrollPane editScroll = new JScrollPane(editPane);
    // .. don't want scrollbars to get focus
    editScroll.getVerticalScrollBar().setFocusable(false);
    editScroll.getHorizontalScrollBar().setFocusable(false);

    // SplitPane with tree/edit
    splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treePane, editScroll);
    splitPane.setDividerLocation(registry.get("divider",-1));

    // layout
    setLayout(new BorderLayout());
    add(splitPane, BorderLayout.CENTER);
    
    // setup focus policy
    setFocusTraversalPolicy(new FocusPolicy());
    setFocusCycleRoot(true);
    
    // done    
  }
  
  /**
   * Component callback event in case removed from parent. Used
   * for storing current state.
   */
  public void removeNotify() {
    // remember
    registry.put("divider",splitPane.getDividerLocation());
    // continue
    super.removeNotify();
  }

  /**
   * Accessor - current context 
   * @return Gedcom tree's root and selection 
   */
  public Context getContext() {
    Context result = new Context(gedcom, (Entity)tree.getRoot(), tree.getSelection());
    result.setSource(this);
    return result;
  }
  
  /**
   * Accessor - current context 
   * @param context context to switch to
   */
  public void setContext(Context context) {
    
    // already there?
    if (context.getGedcom()==gedcom 
      && context.getEntity()==tree.getRoot()
		  && context.getProperty()==tree.getSelection())
      return;

    // clear current selection
    tree.clearSelection();

    // change root if necessary
    Entity entity = context.getEntity();
    if (entity!=tree.getRoot())
      tree.setRoot(entity);

    // set selection
    Property property = context.getProperty();
    if (property!=null) {
      tree.removeTreeSelectionListener(callback);
      tree.setSelection(property);  
      tree.addTreeSelectionListener(callback);
    }
  
    // Done
  }
  
  /**
   * Action - cut
   */
  private class Cut extends Copy {

    /** constructor */
    private Cut(Property deletee) {
      super(deletee);
      
      super.setImage(Images.imgCut);
      super.setText(resources.getString("action.cut"));

    }
    /** run */
    protected void execute() {
      
      // warn about cut
      String veto = selection.getDeleteVeto();
      if (veto!=null) { 
        // Removing property {0} from {1} leads to:\n{2}
        String msg = resources.getString("del.warning", new String[] { 
          selection.getTag(), selection.getEntity().toString(), veto 
        });
        // prepare actions
        ActionDelegate[] actions = {
          new CloseWindow(resources.getString("action.cut")), 
          new CloseWindow(CloseWindow.TXT_CANCEL)
        };
        // ask the user
        int rc = editView.getWindowManager().openDialog("cut.warning", resources.getString("action.cut"), WindowManager.IMG_WARNING, msg, actions, AdvancedEditor.this );
        if (rc!=0)
          return;
        // continue
      }
      
      // copy first
      super.execute();
      
      // now cut
      Gedcom gedcom = selection.getGedcom();
      gedcom.startTransaction();
      selection.getParent().delProperty(selection);
      gedcom.endTransaction();
      
      // done
    }
  } //Cut

  /**
   * Action - copy
   */
  private class Copy extends ActionDelegate {
  	
    /** selection */
    protected Property selection; 
    
    /** constructor */
    protected Copy(Property property) {

      super.setText(resources.getString("action.copy"));
      super.setImage(Images.imgCopy);

      this.selection = property;
      
      setEnabled(selection!=null && !(selection instanceof Entity) && !(selection.isTransient()));

    }
    /** run */
    protected void execute() {
      try {
        // Write properties and their subs into a transferable
        StringWriter out = new StringWriter();
        try {
          GedcomWriter.write(Collections.singletonList(selection), out);
        } catch (IOException e) {
          // can't happen
        }
        clipboard.setContents(new StringSelection(out.toString()), null);
      } catch (Throwable t) {
        Debug.log(Debug.WARNING, AdvancedEditor.this, "Couldn't ask system clipboard for flavor", t);
      }
    }

  } //ActionCopy
    
  /**
   * Action - paste
   */
  private class Paste extends ActionDelegate {
  	
    /** selection */
    private Property parent; 
    
    /** constructor */
    protected Paste(Property property) {
  
      super.setText(resources.getString("action.paste"));
      super.setImage(Images.imgPaste);
  
      this.parent = property;
      
      super.setEnabled(isPasteAvail());
    }
    /** check whether pasting is available */
    private boolean isPasteAvail() {
      try {
        return Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this).isDataFlavorSupported(DataFlavor.stringFlavor);
      } catch (Throwable t) {
        Debug.log(Debug.WARNING, AdvancedEditor.this, "Couldn't ask system clipboard for flavor", t);
      }
      return false;
    }
    /** run */
    protected void execute() {
      
      // forget about it if data flavor is no good
      if (!isPasteAvail())
        return;
      
      // start a transaction and grab from clipboard
      gedcom.startTransaction();
      try {
        String s = clipboard.getContents(null).getTransferData(DataFlavor.stringFlavor).toString();
        GedcomReader.read(new StringReader(s), parent, -1);
      } catch (Throwable t) {
        Debug.log(Debug.WARNING, this, t);
      }
      gedcom.endTransaction();
  
    }
  
  } //Paste
  
  /**
   * Action - add
   */
  private class Add extends ActionDelegate {
    /** parent */
    private Property parent; 
    /** constructor */
    protected Add(Property parent) {

      super.setText(resources.getString("action.add"));
      super.setImage(Images.imgNew);

      this.parent = parent;
      
      super.setEnabled(parent!=null);
    }
    /** run */
    protected void execute() {
  
      // .. Confirm
      JLabel label = new JLabel(resources.getString("add.choose"));
      ChoosePropertyBean choose = new ChoosePropertyBean(parent, resources);
      JCheckBox check = new JCheckBox(resources.getString("add.default_too"),true);
  
      int option = editView.getWindowManager().openDialog("add",resources.getString("add.title"),WindowManager.IMG_QUESTION,new JComponent[]{ label, choose, check },CloseWindow.OKandCANCEL(), AdvancedEditor.this); 
      
      // .. not OK?
      if (option!=0)
        return;

      // .. stop current 
      tree.clearSelection();
  
      // .. Calculate chosen properties
      Property[] props = choose.getResultingProperties();
  
      if ( (props==null) || (props.length==0) ) {
        editView.getWindowManager().openDialog(null,null,WindowManager.IMG_ERROR,resources.getString("add.must_enter"),CloseWindow.OK(), AdvancedEditor.this);
        return;
      }
  
      // .. add properties
      gedcom.startTransaction();
      try {
        for (int i=0;i<props.length;i++) {
          Property newProp = parent.addProperty(props[i]);
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
      tree.setSelectionPath(tree.getPathFor(select));
      
      // done
    }

  } //Add
    
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
        Transaction tx = gedcom.startTransaction();
        bean.commit(tx);
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
  private class InteractionListener extends MouseAdapter implements TreeSelectionListener, ChangeListener {
    
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
        if (target!=null) {
          // create new context
          Context ctx = new Context(target);
          // we're the source
          ctx.setSource(AdvancedEditor.this);
          // tell others
          editView.getViewManager().setContext(ctx);
        }
      }
    }
  
    /** callback - mouse press */
    public void mousePressed(MouseEvent e) {
      mouseReleased(e);
    }
    /** callback - mouse release */
    public void mouseReleased(MouseEvent e) {
      // no popup trigger no action
      if (!e.isPopupTrigger()) 
        return;
      Point pos = e.getPoint();
      
      // property at that point?
      Property prop = tree.getPropertyAt(pos);
      Property root = tree.getRoot();
      
      // make sure it's selected
      if (tree.getSelection()!=prop)
        tree.setSelection(prop);

      // 20040719 got to check transient - dont want to let the user control those
      if (prop!=null&&prop.isTransient())
        prop = null;
      
      // create Context
      Context context = new Context(gedcom, (Entity)root, prop);

      // cut/copy/paste
      List actions = Arrays.asList(new Object[]{
        new Cut(prop),
        new Copy(prop),
        new Paste(prop),
        ActionDelegate.NOOP,
        new Add(prop)
      });
      
      // show context menu
      editView.getViewManager().showContextMenu(context, actions, tree, e.getPoint());

      // done
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
        if (!gedcom.isTransaction()&&bean!=null&&ok.isEnabled()&&editView.isCommitChanges()) 
          ok.trigger();
  
      }

      // Clean up
      bean = null;
      editPane.removeAll();
      editPane.revalidate();
      editPane.repaint();
  
      // setup beans
      Property prop = tree.getSelection(); 
      if (prop!=null&&!prop.isSecret()) {
  
        // get a bean for property
        bean = PropertyBean.get(prop);
        
        try {
  
          // initialize bean
          bean.init(gedcom, prop, null, editView.getViewManager(), registry);
          
          // add bean to center of editPane 
          editPane.add(bean, BorderLayout.CENTER);
  
          // and a label to the top
          final JLabel label = new JLabel(Gedcom.getName(prop.getTag()), prop.getImage(false), SwingConstants.LEFT);
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
          bean.addChangeListener(this);
  
          // and request focus - this only works consistently
          // if invoked later (especially when tabbing)
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              requestFocusInWindow();
              bean.requestFocusInWindow();
            }
          });
          
        } catch (Throwable t) {
          Debug.log(Debug.WARNING, this, "Property bean "+bean+" failed with "+t.getMessage(), t);
        }
        
        // start without ok and cancel
        ok.setEnabled(false);
        cancel.setEnabled(false);
        
        // tell selection to everyone else
        editView.getViewManager().setContext(getContext());
    
      }
  
      // Done
    }

    /**
     * callback for state change - enable buttons
     */
    public void stateChanged(ChangeEvent e) {
      ok.setEnabled(true);
      cancel.setEnabled(true);
    }
  
  } //InteractionListener

  /**
   * Intercept focus policy requests to automate tree node traversal on TAB
   */
  private class FocusPolicy extends LayoutFocusTraversalPolicy {
    public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
      // let super find out who's getting focus - this might be null!
      Component result = super.getComponentAfter(focusCycleRoot, aComponent);
      if (result==null)
        return null;
      // choose next row in tree IF
      //  - a bean is still displayed at the moment
      //  - next component is not part of that bean
      if (bean!=null&&!SwingUtilities.isDescendingFrom(result, bean)) 
        tree.setSelectionRow( (tree.getSelectionRows()[0]+1) % tree.getRowCount());
      // done for me
      return result;
    }
    public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
      // let super find out who's getting focus - this might be null!
      Component result = super.getComponentBefore(focusCycleRoot, aComponent);
      if (result==null)
        return null;
      // choose previous row in tree IF
      //  - a bean is still displayed at the moment
      //  - prev component is not part of that bean
      if (bean!=null&&!SwingUtilities.isDescendingFrom(result, bean)) 
        tree.setSelectionRow( (tree.getSelectionRows()[0]-1) % tree.getRowCount());
      // done for me
      return result;
    }
  } //FocusPolicy
  
} //AdvancedEditor
