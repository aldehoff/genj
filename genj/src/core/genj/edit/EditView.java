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
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.util.ActionDelegate;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.PopupWidget;
import genj.view.Context;
import genj.view.ContextListener;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;
import genj.window.CloseWindow;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

/**
 * Component for editing genealogic entity properties
 */
public class EditView extends JPanel implements ToolBarSupport, ContextListener {
  
  /** instances */
  private static List instances = new LinkedList();

  /** the gedcom we're looking at */
  private Gedcom  gedcom;
  
  /** the registry we use */
  private Registry registry;

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
  
  private static boolean flip;
  
  /**
   * Constructor
   */
  public EditView(String setTitle, Gedcom setGedcom, Registry setRegistry, ViewManager setManager) {
    
    super(new BorderLayout());
    
    // remember
    gedcom   = setGedcom;
    registry = setRegistry;
    manager  = setManager;

    // prepare undo/redo actions
    undo = new Undo(gedcom, manager);
    redo = new Redo(gedcom, manager);
    
    // prepare mode action
    mode = new Mode();
    
    // run mode switch if applicable
    if (registry.get("advanced", false))
      mode.trigger();

    // Done
  }
  
  /**
   * Set editor to use
   */
  private void setEditor(Editor set) {

    // get old context and set it on editor to force commit changes
    Context old = null;
    if (editor!=null) {
      old = editor.getContext();
      editor.setContext(old);
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
    contextMenu.update();
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
    Context context = manager.getContext(gedcom);
    try { 
      context = new Context(gedcom.getEntity(registry.get("sticky",(String)null))); 
      context.setSource(this);
      isSticky = true;
    } catch (Throwable t) {
    }
    setContext(context);

    // listen for available undos/removes
    gedcom.addGedcomListener(undo);
    gedcom.addGedcomListener(redo);

  }

  /**
   * Notification when component is not used any more
   */
  public void removeNotify() {
    
    // remember context
    Entity e = null;
    if (isSticky) 
      e = editor.getContext().getEntity();
    registry.put("sticky", e!=null?e.getId():"");

    // remember mode
    registry.put("advanced", mode.advanced);

    // dont listen for available undos
    gedcom.removeGedcomListener(undo);
    gedcom.removeGedcomListener(redo);

    // forget this instance
    instances.remove(this);
    
    // Continue
    super.removeNotify();

    // Done
  }
  
  /**
   * WindowManager
   */
  /*package*/ WindowManager getWindowManager() {
    return manager.getWindowManager();
  }
  
  /**
   * ViewManager
   */
  /*package*/ ViewManager getViewManager() {
    return manager;
  }
  
  /**
   * Ask the user whether he wants to commit changes 
   */
  /*package*/ boolean isCommitChanges() {
      
    if (Options.getInstance().isAutoCommit)
      return true;
    
    JCheckBox auto = new JCheckBox(resources.getString("confirm.autocomit"));
    auto.setFocusable(false);
    
    int rc = manager.getWindowManager().openDialog(null, 
        resources.getString("confirm.keep.changes"), WindowManager.IMG_QUESTION, 
        new JComponent[] {
          new JLabel(resources.getString("confirm.keep.changes")),
          auto
        },
        CloseWindow.YESandNO(), 
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
   * Context listener callback
   */
  public void setContext(Context context) {
    
    // check if we're following the context
    JComponent view = context.getView();
    if (view instanceof EditView) {
      // not if another editor was the view responsible for context change
      if (view!=this)
        return;
    } else {
      // not if we're sticky
      if (isSticky) 
        return;
    }
    
    // check current editor's context
    Context current = editor.getContext();
    if (current.getEntity()!=context.getEntity())
      back.push(current);
    
    // tell to editors - they're lazy and won't change if not needed
    editor.setContext(context);
    
    // update context menu button
    contextMenu.update();
    
    // done
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
      .setTextAllowed(false)
      .setContainer(bar);

    // return in history
    bh.setEnabled(true).create(back);
    
    // toggle sticky
    bh.create(sticky, Images.imgStickOn, isSticky);
    
    // add undo/redo
    bh.create(undo);
    bh.create(redo);
    
    // add actions
    bar.add(contextMenu);
    
    // add basic/advanced
    bar.addSeparator();
    bh.create(mode, Images.imgAdvanced, mode.advanced);
    
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
    }
    
    /** update */
    private void update() {
      setIcon(editor.getContext().getImage());
    }
    
    /** override - popup creation */
    protected JPopupMenu createPopup() {
      // force editor to commit
      editor.setContext(editor.getContext());
      // create popup
      return manager.getContextMenu(editor.getContext(), null, this);
    }
     
  } //ContextMenu
  
  /**
   * Action - toggle
   */
  private class Sticky extends ActionDelegate {
    /** constructor */
    protected Sticky() {
      super.setImage(Images.imgStickOff);
      super.setTip("action.stick.tip");
    }
    /** run */
    protected void execute() {
      isSticky = !isSticky;
    }
  } //ActionBack

  /**
   * Action - back
   */
  private class Back extends ActionDelegate {
    private boolean ignorePush = false;
    private Stack stack = new Stack();
    /** constructor */
    protected Back() {
      super.setImage(Images.imgReturn).setTip("action.return.tip");
    }
    /** run */
    protected void execute() {
      if (stack.size()==0)
        return;
      // pop first valid on stack
      while (!stack.isEmpty()) {
        Context context = (Context)stack.pop();
        if (context.isValid()&&context.getEntity()!=null) {
          ignorePush = true;
          context.setSource(EditView.this);
          setContext(context);
          ignorePush = false;
          return;
        }
      }
    }
    /** push another on stack */
    protected void push(Context context) {
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
  private class Mode extends ActionDelegate {
    private boolean advanced = false;
    private Mode() {
      setImage(Images.imgView);
      setEditor(new BasicEditor());
      setTip("action.mode");
    }
    protected void execute() {
      advanced = !advanced;
      setEditor(advanced ? (Editor)new AdvancedEditor() : new BasicEditor());
    }
  } //Advanced
  
} //EditView
