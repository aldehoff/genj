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
import genj.view.Context;
import genj.view.ContextListener;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.swing.JComponent;
import javax.swing.JPanel;
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

    // get old context
    Context old = editor!=null ? editor.getContext() : null;
      
    // remove old editor 
    removeAll();
      
    // keep new
    editor = set;
    editor.init(gedcom, manager, registry);

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
    
    // remember
    instances.add(this);
    
    // Check if we can preset something to edit
    Context context;
    try { 
      context = new Context(gedcom.getEntity(registry.get("last",(String)null))); 
    } catch (Exception e) {
      context = manager.getContext(gedcom); 
    }
    setContext(context);

    // listen for available undos/removes
    gedcom.addGedcomListener(undo);
    gedcom.addGedcomListener(redo);

    // continue
    super.addNotify();    
  }

  /**
   * Notification when component is not used any more
   */
  public void removeNotify() {
    
    // remember context
    Entity e = editor.getContext().getEntity();
    registry.put("last", e!=null?e.getId():"");

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
    
    // source is this view?
    JComponent view = context.getView();
    if (view instanceof EditView) {
      if (view!=this)
        return;

    } else {
      // don't follow context if one EditView is looking at this already
      EditView[] views = getInstances(context.getGedcom());
      for (int i = 0; i < views.length; i++) {
        if (views[i].editor.getContext().equals(context))
          return;
      }

      // not if we're sticky
      if (isSticky) 
        return;
    }
    
    // get current
    Context current = editor.getContext();
    if (current.equals(context))
      return;
      
    // remember current 
    back.push(current);
    
    // tell editor
    editor.setContext(context);
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
    
    // add basic/advanced
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
        if (context.isValid()) {
          editor.setContext(context);      
          return;
        }
      }
    }
    /** push another on stack */
    protected void push(Context context) {
      // won't put the same entity twice though
      if (!stack.isEmpty()) {
        Context current = (Context)stack.peek();
        if (current.getEntity()==context.getEntity())
          return;
      }
      // keep it
      stack.push(context);
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
