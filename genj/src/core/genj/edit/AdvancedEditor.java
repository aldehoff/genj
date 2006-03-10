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
import genj.io.PropertyReader;
import genj.io.PropertyTransferable;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import genj.view.Context;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;

import javax.swing.Action;
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
import javax.swing.tree.TreePath;

/**
 * Our advanced version of the editor allowing low-level
 * access at the Gedcom record-structure
 */
/*package*/ class AdvancedEditor extends Editor {
  
  private final static String
    ACC_CUT = "ctrl X",
    ACC_COPY = "ctrl C",
    ACC_PASTE = "ctrl V";

  private final static Clipboard clipboard = initClipboard();
  
  private boolean ignoreSelection = false;

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
  private Action2    
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
    tree = new PropertyTreeWidget(gedcom) {
      public Context getContext() {
//        // check selection
       Context context = super.getContext();
//       Property[] properties = context.getProperties();
//       if (properties.length==0)
//         return context;
//       for (int i=0;i<properties.length;i++)
//         if (properties[i].isTransient())
//           return context;
//       // add actions
//        context.addAction(new Cut(properties));
//        context.addAction(new Copy(properties));
//        if (properties.length==1)
//          context.addAction(new Paste(properties[0]));
//        context.addAction(Action2.NOOP);
//        if (properties.length==1)
//          context.addAction(new Add(properties[0]));
//        try {
//          context.addAction(new Propagate(properties));
//        } catch (IllegalArgumentException i) {
//        }
//        context.addAction(Action2.NOOP);
//        // done
        return context;
      }
    };

    callback = new InteractionListener();
    tree.addTreeSelectionListener(callback);
    
    JScrollPane treePane = new JScrollPane(tree);
    treePane.setMinimumSize  (new Dimension(160, 128));
    treePane.setPreferredSize(new Dimension(160, 128));
    treePane.getHorizontalScrollBar().setFocusable(false); // dont allow focus on scroll bars
    treePane.getVerticalScrollBar().setFocusable(false);
        
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
    
    // shortcuts
    new Cut().install(tree, JComponent.WHEN_FOCUSED);
    new Copy().install(tree, JComponent.WHEN_FOCUSED);
    new Paste().install(tree, JComponent.WHEN_FOCUSED);
    
    // done    
  }
  
  /**
   * Provider current context 
   */
  public Context getContext() {
    return tree.getContext();
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
   * @param context context to switch to
   */
  public void setContext(Context context) {
    
    // ignore?
    if (ignoreSelection)
      return;

    // clear current selection
    tree.clearSelection();

    // change root if necessary
    Entity entity = context.getEntity();
    if (entity!=tree.getRoot())
      tree.setRoot(entity);

    // set selection
    Property property = context.getProperty();
    tree.setSelection(Collections.singletonList(property!=null ? property : entity));  
    
    // 20060301 set focus since selection change won't do that anymore
    if (bean!=null)
      bean.requestFocusInWindow();
    
  
    // Done
  }
  
//  /**
//   * Action - propagate properties
//   */
//  private class Propagate extends Action2 {
//    /** state */
//    private Property property;
//    /** constructor */
//    private Propagate(Property[] properties) {
//      // remember
////      property = prop;
//      setText(resources.getString("action.propagate"));
////      setImage(prop.getImage(false));
//      setEnabled(false);
//    }
//    /** apply it */
//    protected void execute() {
//      // prepare options
//      final Entity entity = property.getEntity();
//      final TextAreaWidget text = new TextAreaWidget("", 4, 10, false, true);
//      final SelectEntityWidget select = new SelectEntityWidget(gedcom, entity.getTag(), resources.getString("action.propagate.toall"));
//      select.addActionListener(new ActionListener() {
//        public void actionPerformed(ActionEvent e) {
//          String what = property.getTag() + " " + property.toString();
//          Entity selection = select.getSelection();
//          String string = selection==null ? resources.getString("action.propagate.all", new Object[] { what, ""+select.getEntityCount(), Gedcom.getName(entity.getTag()) } )
//              : resources.getString("action.propagate.one", new Object[]{ what, selection.getId(), Gedcom.getName(selection.getTag()) });
//          text.setText(string);
//        }
//      });
//      
//      JCheckBox check = new JCheckBox(resources.getString("action.propagate.value"));
//      
//      JPanel panel = new JPanel(new NestedBlockLayout("<col><select wx=\"1\"/><note wx=\"1\" wy=\"1\"/><check wx=\"1\"/></col>"));
//      panel.add(select);
//      panel.add(new JScrollPane(text));
//      panel.add(check);
//      
//      // preselect something?
//      select.setSelection(gedcom.getEntity(registry.get("select."+entity.getTag(), (String)null)));
//
//      // show it
//      boolean cancel = 0!=editView.getWindowManager().openDialog("propagate", getText(), WindowManager.WARNING_MESSAGE, panel, Action2.okCancel(), AdvancedEditor.this);
//      if (cancel)
//        return;
//
//      Entity selection = select.getSelection();
//      
//      // remember selection
//      registry.put("select."+entity.getTag(), selection!=null ? selection.getId() : null);
//      
//      // change it
//      try {
//        gedcom.startTransaction();
//      } catch (IllegalStateException ise) {
//        return;
//      }
//        
//      try {
//        if (selection!=null)
//          execute(selection, check.isSelected());
//        else for (Iterator it = gedcom.getEntities(entity.getTag()).iterator(); it.hasNext(); ) 
//          execute((Entity)it.next(), check.isSelected());
//      } finally {
//        gedcom.endTransaction();
//      }
//
//      // change focus
//      if (selection!=null)
//        editView.setContext(new Context(selection), true);
//    }
//    
//    /** apply the template to an entity */
//    private void execute(Entity entity, boolean values) {
//      // make sure we're not propagating to self
//      if (property.getEntity()==entity)
//        return;
//      // check we got the root of the property's path
//      TagPath path = property.getPath();
//      Property to = entity;
//      for (int i=1;i<path.length()-1;i++) {
//        Property p = to.getProperty(path.get(i));
//        to = p!=null ? p :  to.addProperty(path.get(i), "");
//      }
//      copy(property, to, values);
//    }
//    private void copy(Property prop,  Property to, boolean values) {
//      // check to for child
//      Property copy = to.getProperty(prop.getTag(), false);
//      if (copy==null)
//        copy = to.addProperty(prop.getTag(), values ? prop.getValue() : "");
//      // loop over children of prop
//      for (int i=0, j=prop.getNoOfProperties(); i<j; i++) {
//        Property child = prop.getProperty(i);
//        // apply to non-xrefs, non-transient, non-existent 
//        if ( !(child instanceof PropertyXRef) && !child.isTransient() && to.getProperty(child.getTag(), false)==null) 
//          copy(child, copy, values);
//        // next
//      }
//      // done
//    }
//  } //Template
  
  /**
   * Action - cut
   */
  private class Cut extends Action2 {

    /** selection */
    protected List presetSelection; 
    
    /** constructor */
    private Cut(Property[] properties) {
      presetSelection = Arrays.asList(properties);
      super.setImage(Images.imgCut);
      super.setText(resources.getString("action.cut"));
    }
    
    /** constructor */
    private Cut() {
      setAccelerator(ACC_CUT);
    }
    
    /** run */
    protected void execute() {
      
      // available
      List selection = presetSelection;
      if (selection==null)
        selection = tree.getSelection(true);
      if (selection.isEmpty())
        return;
      
      // warn about cut
      String veto = getVeto(selection);
      if (veto.length()>0) {
        int rc = editView.getWindowManager().openDialog("cut.warning", resources.getString("action.cut"), WindowManager.WARNING_MESSAGE, veto, new Action[]{ new Action2(resources.getString("action.cut")), Action2.cancel() }, AdvancedEditor.this );
        if (rc!=0)
          return;
      }
      
      // copy first
      try {
        clipboard.setContents(new PropertyTransferable(selection).getStringTransferable(), null);
      } catch (Throwable t) {
        EditView.LOG.log(Level.WARNING, "Couldn't copy properties", t);
        return;
      }
      
      // now cut
      gedcom.startTransaction();
      for (ListIterator props = selection.listIterator(); props.hasNext(); )  {
        Property p = (Property)props.next();
        p.getParent().delProperty(p);
      }
      gedcom.endTransaction();
      
      // done
    }

    /** assemble a list of vetos for cutting properties */
    private String getVeto(List properties) {
      
      StringBuffer result = new StringBuffer();
      for (ListIterator checks=properties.listIterator(); checks.hasNext(); ) {
        
        Property p = (Property)checks.next();
        String veto = p.getDeleteVeto();
        if (veto!=null) 
          // Removing property {0} from {1} leads to:\n{2}
          result.append(resources.getString("del.warning", new String[] { p.getPropertyName(), p.getParent().getPropertyName(), veto  }));
      }

      return result.toString();
    }
      
  } //Cut

  /**
   * Action - copy
   */
  private class Copy extends Action2 {
  	
    /** selection */
    protected List presetSelection; 
    
    /** constructor */
    protected Copy(Property[] properties) {
      presetSelection = Arrays.asList(properties);
      setText(resources.getString("action.copy"));
      setImage(Images.imgCopy);
    }
    /** constructor */
    protected Copy() {
      setAccelerator(ACC_COPY);
    }
    /** run */
    protected void execute() {
      
      // check selection
      List selection = presetSelection;
      if (selection==null)
        selection = tree.getSelection(true);
      
      // contains entity?
      if (selection.contains(tree.getRoot()))
        selection = Arrays.asList(tree.getRoot().getProperties());
      
      try {
        clipboard.setContents(new PropertyTransferable(selection).getStringTransferable(), null);
      } catch (Throwable t) {
        EditView.LOG.log(Level.WARNING, "Couldn't copy properties", t);
      }
    }

  } //ActionCopy
    
  /**
   * Action - paste
   */
  private class Paste extends Action2 {
  	
    /** selection */
    private Property presetParent; 
    
    /** constructor */
    protected Paste(Property property) {
      presetParent = property;
      setText(resources.getString("action.paste"));
      setImage(Images.imgPaste);
      setEnabled(isPasteAvail());
    }
    /** constructor */
    protected Paste() {
      setAccelerator(ACC_PASTE);
    }
    /** check whether pasting is available */
    private boolean isPasteAvail() {
      try {
        return Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this).isDataFlavorSupported(DataFlavor.stringFlavor);
      } catch (Throwable t) {
        EditView.LOG.log(Level.WARNING, "Accessing system clipboard failed", t);
      }
      return false;
    }
    /** run */
    protected void execute() {
      
      Property parent = presetParent;
      
      // got a parent already?
      if (parent==null) {
        List selection = tree.getSelection(false);
        if (selection.size()!=1)
          return;
        parent = (Property)selection.get(0);
      }
      
      // forget about it if data flavor is no good
      if (!isPasteAvail())
        return;
      
      // start a transaction and grab from clipboard
      gedcom.startTransaction();
      try {
        String s = clipboard.getContents(null).getTransferData(DataFlavor.stringFlavor).toString();
        new PropertyReader(new StringReader(s), true) {
          /** intercept add so we can add/merge */
          protected Property addProperty(Property prop, String tag, String value, int pos) {
            // reuse prop's existing child with same tag if singleton
            Property child = prop.getProperty(tag);
            if (child!=null&&prop.getMetaProperty().getNested(tag, false).isSingleton()&&!(child instanceof PropertyXRef)) {
              child.setValue(value);
              return child;
            }
            return super.addProperty(prop, tag, value, pos);
          }
          /** intercept xrefs so we can link 'em */
          protected void trackXRef(PropertyXRef xref) {
            try {
              xref.link();
            } catch (Throwable t) {
              xref.getParent().delProperty(xref);
            }
          }
        }.read(parent);
      } catch (Throwable t) {
        EditView.LOG.log(Level.WARNING, "Couldn't paste clipboard content", t);
      }
      gedcom.endTransaction();
  
    }
  
  } //Paste
  
  /**
   * Action - add
   */
  private class Add extends Action2 {
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
      int option = editView.getWindowManager().openDialog("add",resources.getString("add.title"),WindowManager.QUESTION_MESSAGE,new JComponent[]{ label, choose, check },Action2.okCancel(), AdvancedEditor.this); 
      
      // .. not OK?
      if (option!=0)
        return;

      // .. stop current 
      tree.clearSelection();
  
      // .. calculate chosen tags
      String[] tags = choose.getSelectedTags();
      if (tags.length==0)  {
        editView.getWindowManager().openDialog(null,null,WindowManager.ERROR_MESSAGE,resources.getString("add.must_enter"),Action2.okOnly(), AdvancedEditor.this);
        return;
      }
  
      // .. add properties
      gedcom.startTransaction();
      Property newProp = null;
      try {
        for (int i=0;i<tags.length;i++) {
          newProp = parent.addProperty(tags[i], "");
          if (check.isSelected()) newProp.addDefaultProperties();
        } 
      } finally {
        gedcom.endTransaction();
      }
         
      // .. select added
      if (newProp instanceof PropertyEvent) {
        Property pdate = ((PropertyEvent)newProp).getDate(false);
        if (pdate!=null) newProp = pdate;
      }
      tree.setSelectionPath(new TreePath(tree.getPathFor(newProp)));
      
      // done
    }

  } //Add
    
  /**
   * A ok action
   */
  private class OK extends Action2 {
  
    /** constructor */
    private OK() {
      setText(Action2.TXT_OK);
    }
  
    /** cancel current proxy */
    protected void execute() {
  
      Property root = tree.getRoot();
      if (root==null)
        return;
      Gedcom gedcom = root.getGedcom();
  
      if (bean!=null) try {
        Transaction tx = gedcom.startTransaction();
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
  private class Cancel extends Action2 {
  
    /** constructor */
    private Cancel() {
      setText(Action2.TXT_CANCEL);
    }
  
    /** cancel current proxy */
    protected void execute() {
      // disable ok&cancel
      ok.setEnabled(false);
      cancel.setEnabled(false);
      // simulate a selection change
      List selection = tree.getSelection(false);
      tree.clearSelection();
      tree.setSelection(selection);
    }
  
  } //Cancel
  
  /**
   * Handling selection of properties
   */
  private class InteractionListener implements TreeSelectionListener, ChangeListener {
    
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
      List selection = tree.getSelection(false); 
      if (selection.isEmpty())
        return;
      
      // can show bean for first selection
      Property prop = (Property)selection.get(0);
      try {

        // get a bean for property
        bean = editView.getBeanFactory().get(prop);
        
        // add bean to center of editPane 
        editPane.add(bean, BorderLayout.CENTER);

        // and a label to the top
        final JLabel label = new JLabel(Gedcom.getName(prop.getTag()), prop.getImage(false), SwingConstants.LEFT);
        editPane.add(label, BorderLayout.NORTH);

        // and actions to the bottom
        if (bean.isEditable()) {
          JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
          ButtonHelper bh = new ButtonHelper().setInsets(0).setContainer(buttons);
          bh.create(ok);
          bh.create(cancel);
          editPane.add(buttons, BorderLayout.SOUTH);
        }
        
        // listen to it
        bean.addChangeListener(this);

      } catch (Throwable t) {
        EditView.LOG.log(Level.WARNING,  "Property bean "+bean, t);
      }
      
      // start without ok and cancel
      ok.setEnabled(false);
      cancel.setEnabled(false);
      
      // tell to view
      try {
        ignoreSelection = true;
        editView.setContext(getContext(), true);
      } finally {
        ignoreSelection = false;
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
      if (bean!=null&&!SwingUtilities.isDescendingFrom(result, bean)) {
        tree.setSelectionRow( (tree.getSelectionRows()[0]+1) % tree.getRowCount());
      }
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
