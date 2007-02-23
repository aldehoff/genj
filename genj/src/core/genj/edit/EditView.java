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

import genj.edit.actions.Redo;
import genj.edit.actions.Undo;
import genj.edit.beans.BeanFactory;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.PropertyXRef;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import genj.util.swing.PopupWidget;
import genj.view.ContextProvider;
import genj.view.ContextSelectionEvent;
import genj.view.ToolBarSupport;
import genj.view.ViewContext;
import genj.view.ViewManager;
import genj.window.WindowBroadcastListener;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import spin.Spin;

/**
 * Component for editing genealogic entity properties
 */
public class EditView extends JPanel implements ToolBarSupport, WindowBroadcastListener, ContextProvider  {
  
  /*package*/ final static Logger LOG = Logger.getLogger("genj.edit");
  
  /** instances */
  private static List instances = new LinkedList();

  /** the gedcom we're looking at */
  private Gedcom  gedcom;
  
  /** the registry we use */
  private Registry registry;
  
  /** bean factory */
  private BeanFactory beanFactory;

  /** the view manager */
  private ViewManager manager;
  
  /** the resources we use */
  static final Resources resources = Resources.get(EditView.class);

  /** actions we offer */
  private Sticky   sticky = new Sticky();
  private Back     back   = new Back(); 
  private Undo     undo;
  private Redo     redo;
  private Mode     mode;
  private ContextMenu contextMenu = new ContextMenu();
  
  /** whether we're sticky */
  private  boolean isSticky = false;

  /** current editor */
  private Editor editor;
  
  /** whether all context selection should be ignored */
  private static boolean ignoreContextSelection = false;
  
  /**
   * Constructor
   */
  public EditView(String setTitle, Gedcom setGedcom, Registry setRegistry, ViewManager setManager) {
    
    super(new BorderLayout());
    
    // remember
    gedcom   = setGedcom;
    registry = setRegistry;
    manager  = setManager;
    beanFactory = new BeanFactory(manager, registry);

    // prepare undo/redo actions
    undo = new Undo(gedcom);
    redo = new Redo(gedcom);
    
    undo.setText(null);
    redo.setText(null);
    
    // prepare mode action
    mode = new Mode();
    
    // run mode switch if applicable
    if (registry.get("advanced", false))
      mode.trigger();
    
    // add keybindings
    InputMap imap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
    ActionMap amap = getActionMap();
    imap.put(KeyStroke.getKeyStroke("alt LEFT"), back);
    amap.put(back, back);

    // Done
  }
  
  

  
  /**
   * Set editor to use
   */
  private void setEditor(Editor set) {

    // preserve old context and reset current editor to force commit changes
    ViewContext old = null;
    if (editor!=null) {
      old = editor.getContext();
      editor.setContext(new ViewContext(gedcom));
    }
    
    // remove old editor 
    removeAll();
      
    // keep new
    editor = set;
    editor.init(gedcom, this, registry);

    // add to layout
    add(editor, BorderLayout.CENTER);

    // restore old context
    if (old!=null)
      editor.setContext(old);
      
    // show
    revalidate();
    repaint();
  }

  /**
   * @see javax.swing.JComponent#addNotify()
   */
  public void addNotify() {
    
    // let super do its thing first
    super.addNotify();    
    
    // remember
    instances.add(this);
    
    // Check if we can preset something to edit
    Entity entity = gedcom.getEntity(registry.get("entity", (String)null));
    if (entity==null) entity = gedcom.getFirstEntity(Gedcom.INDI);
    if (entity!=null) {
      isSticky = registry.get("sticky", false);
      setContext(new ViewContext(entity), false);
    }
    
    // listen for available undos/removes
    gedcom.addGedcomListener((GedcomListener)Spin.over(undo));
    gedcom.addGedcomListener((GedcomListener)Spin.over(redo));

  }

  /**
   * Notification when component is not used any more
   */
  public void removeNotify() {
    
    // remember context
    registry.put("sticky", isSticky);
    Entity entity = editor.getContext().getEntity();
    if (entity!=null)
      registry.put("entity", entity.getId());

    // remember mode
    registry.put("advanced", mode.advanced);

    // dont listen for available undos
    gedcom.removeGedcomListener((GedcomListener)Spin.over(undo));
    gedcom.removeGedcomListener((GedcomListener)Spin.over(redo));

    // forget this instance
    instances.remove(this);
    
    // Continue
    super.removeNotify();

    // Done
  }
  
  /**
   * BeanFactory
   */
  /*package*/ BeanFactory getBeanFactory() {
    return beanFactory;
  }
  
  /**
   * Ask the user whether he wants to commit changes 
   */
  /*package*/ boolean isCommitChanges() {
    
    // we only consider committing IF we're still in a visible top level ancestor (window) - otherwise we assume 
    // that the containing window was closed and we're not going to throw a dialog out there or do a change
    // behind the covers - we really would need a about-to-close hook for contained components here :(
    if (!getTopLevelAncestor().isVisible())
      return false;
      
    // check for auto commit
    if (Options.getInstance().isAutoCommit)
      return true;
    
    JCheckBox auto = new JCheckBox(resources.getString("confirm.autocomit"));
    auto.setFocusable(false);
    
    int rc = WindowManager.getInstance(this).openDialog(null, 
        resources.getString("confirm.keep.changes"), WindowManager.QUESTION_MESSAGE, 
        new JComponent[] {
          new JLabel(resources.getString("confirm.keep.changes")),
          auto
        },
        Action2.yesNo(), 
        this
    );
    
    if (rc!=0)
      return false;
    
    Options.getInstance().isAutoCommit = auto.isSelected();
    
    return true;
    
  }
  
  /**
   * Return all open instances for given gedcom
   */
  /*package*/ static EditView[] getInstances(Gedcom gedcom) {
    List result = new ArrayList();
    Iterator it = instances.iterator();
    while (it.hasNext()) {
      EditView edit = (EditView)it.next();
      if (edit.gedcom==gedcom)
        result.add(edit);
    }
    return (EditView[])result.toArray(new EditView[result.size()]);
  }
  
  /**
   * ContextProvider callback
   */
  public ViewContext getContext() {
    return editor.getContext();
  }

  /**
   * Context listener callback
   */
  public boolean handleBroadcastEvent(genj.window.WindowBroadcastEvent event) {
    
    // ignore it?
    if (ignoreContextSelection)
      return false;
    
    ContextSelectionEvent cse = ContextSelectionEvent.narrow(event, gedcom);
    if (cse==null)
      return false;
    
    ViewContext context = cse.getContext();
    
    // coming from some other view?
    if (cse.getSource()==null || !SwingUtilities.isDescendingFrom( cse.getSource(), this)) {
      
      // take it if not sticky
      if (!isSticky) setContext(context, false);
      
      // done
      return false;
    }
      
    // came from us - needs to be a double click
    if (!cse.isActionPerformed())
      return false;
    
    if (context.getProperty() instanceof PropertyXRef) {
      
      PropertyXRef xref = (PropertyXRef)context.getProperty();
      xref = xref.getTarget();
      if (xref!=null)
        context = new ViewContext(xref);
    }
    
    // change context
    setContext(context, false);
      
    // done
    return false;
  }
  
  public void setContext(ViewContext context, boolean tellOthers) {
    
    // tell to others view (non EditViews)
    if (tellOthers)  fireContextSelected(context);
    
    // check current editor's context
    ViewContext current = editor.getContext();
    if (current.getEntity()!=context.getEntity())
      back.push(current);
    
    // tell to editors - they're lazy and won't change if not needed
    editor.setContext(context);
    
    // done
  }
  
  /**package*/ void fireContextSelected(ViewContext context) {
    
    // already in process?
    if (ignoreContextSelection)
      return;
    
    try {
      ignoreContextSelection = true;
      WindowManager.getInstance(this).broadcast(new ContextSelectionEvent(context, this));
    } finally { 
      ignoreContextSelection = false;
    }
    
  }

  /**
   * @see genj.view.ToolBarSupport#populate(JToolBar)
   */
  public void populate(JToolBar bar) {

    // buttons for property manipulation    
    ButtonHelper bh = new ButtonHelper()
      .setInsets(0)
      .setContainer(bar);

    // return in history
    bh.create(back);
    
    // toggle sticky
    bh.create(sticky, Images.imgStickOn, isSticky);
    
    // add undo/redo
    bh.create(undo);
    bh.create(redo);
    undo.setText(null);
    redo.setText(null);
    
    // add actions
    bar.add(contextMenu);
    
    // add basic/advanced
    bar.addSeparator();
    bh.create(mode, Images.imgAdvanced, mode.advanced).setFocusable(false);
    
    // done
  }
  
  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(256,480);
  }
  
  /**
   * whether we're sticky
   */
  public boolean isSticky() {
    return isSticky;
  }
  
  /**
   * Current entity
   */
  public Entity getEntity() {
    return editor.getContext().getEntity();
  }
  
  /**
   * ContextMenu
   */
  private class ContextMenu extends PopupWidget {
    
    /** constructor */
    private ContextMenu() {
      setIcon(Gedcom.getImage());
      setToolTipText(resources.getString( "action.context.tip" ));
    }
    
    /** override - popup creation */
    protected JPopupMenu createPopup() {
      // force editor to commit
      editor.setContext(editor.getContext());
      // create popup
      return manager.getContextMenu(editor.getContext(), this);
    }
     
  } //ContextMenu
  
  /**
   * Action - toggle
   */
  private class Sticky extends Action2 {
    /** constructor */
    protected Sticky() {
      super.setImage(Images.imgStickOff);
      super.setTip(resources, "action.stick.tip");
    }
    /** run */
    protected void execute() {
      isSticky = !isSticky;
    }
  } //ActionBack

  /**
   * Action - back
   */
  private class Back extends Action2 {
    private boolean ignorePush = false;
    private Stack stack = new Stack();
    /** constructor */
    protected Back() {
      super.setImage(Images.imgReturn).setTip(resources, "action.return.tip");
    }
    /** run */
    protected void execute() {
      if (stack.size()==0)
        return;
      // pop first valid on stack
      while (!stack.isEmpty()) {
        ViewContext context = (ViewContext)stack.pop();
        if (context.getEntity()!=null) {
          ignorePush = true;
          setContext(context, true);
          ignorePush = false;
          return;
        }
      }
    }
    /** push another on stack */
    protected void push(ViewContext context) {
      // ignore it?
      if (ignorePush)
        return;
      // keep it
      stack.push(context);
      // trim stack - arbitrarily chosen size :)
      while (stack.size()>32)
        stack.remove(0);
    }
  } //Back
  
  /**
   * Action - advanced or basic
   */
  private class Mode extends Action2 {
    private boolean advanced = false;
    private Mode() {
      setImage(Images.imgView);
      setEditor(new BasicEditor());
      setTip(resources, "action.mode");
    }
    protected void execute() {
      advanced = !advanced;
      setEditor(advanced ? (Editor)new AdvancedEditor() : new BasicEditor());
    }
  } //Advanced
  
} //EditView
