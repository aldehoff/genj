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

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.PropertyXRef;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import genj.view.ContextProvider;
import genj.view.SelectionSink;
import genj.view.ToolBar;
import genj.view.View;
import genj.view.ViewContext;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

/**
 * Component for editing genealogic entity properties
 */
public class EditView extends View implements ContextProvider, SelectionSink  {
  
  /*package*/ final static Logger LOG = Logger.getLogger("genj.edit");
  private final static Registry REGISTRY = Registry.get(EditView.class);
  static final Resources RESOURCES = Resources.get(EditView.class);
  
  private Mode     mode = new Mode();
  private Sticky sticky = new Sticky();
  private Focus focus = new Focus();
  private Editor editor;
  private ToolBar toolbar;
  
  /**
   * Constructor
   */
  public EditView() {
    
    super(new BorderLayout());
    
    // check for current modes
    mode.setSelected(REGISTRY.get("advanced", false));
    focus.setSelected(REGISTRY.get("focus", false));

    // Done
  }
  

  /**
   * we're a sink for our contained components
   */
  public void fireSelection(Context context, boolean isActionPerformed) {
    boolean wasSelected = sticky.setSelected(true);
    try {
      SelectionSink.Dispatcher.fireSelection(getParent(), context, isActionPerformed);
    } finally {
      sticky.setSelected(wasSelected);
    }
  }
  
  /**
   * Set editor to use
   */
  private void setEditor(Editor set) {

    // commit old editor unless set==null
    Context old = null;
    if (set!=null) {
      
      // force commit
      commit();
  
      // preserve old context 
      old = editor!=null ? editor.getContext() : null;
    }
    
    // clear old editor
    if (editor!=null) {
      editor.setContext(new Context());
      editor = null;
      removeAll();
    }
      
    // set new and restore context
    editor = set;
    if (editor!=null) {
      add(editor, BorderLayout.CENTER);
      if (old!=null)
        editor.setContext(old);
    }
    
    // show
    revalidate();
    repaint();
  }
  
  /**
   * Check whether editor should grab focus or not
   */
  /*package*/ boolean isGrabFocus() {
    return focus.isSelected();
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
    
    JCheckBox auto = new JCheckBox(RESOURCES.getString("confirm.autocomit"));
    auto.setFocusable(false);
    
    int rc = DialogHelper.openDialog(RESOURCES.getString("confirm.keep.changes"), 
        DialogHelper.QUESTION_MESSAGE, new JComponent[] {
          new JLabel(RESOURCES.getString("confirm.keep.changes")),
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
   * ContextProvider callback
   */
  public ViewContext getContext() {
    return editor!=null ? editor.getContext() : null;
  }
  
  @Override
  public void commit() {
    if (editor!=null)
      editor.commit();
  }
  
  public void setContext(Context newContext, boolean isActionPerformed) {
    
    // new gedcom?
    if (newContext.getGedcom()==null) {
      sticky.setSelected(false);
      setEditor(null);
      populate(toolbar);
      return;
    }
    
    // new editor?
    if (newContext.getEntity()!=null && editor==null) {

      sticky.setSelected(false);
      if (mode.isSelected())
        setEditor(new AdvancedEditor(newContext.getGedcom(), this));
      else
        setEditor(new BasicEditor(newContext.getGedcom(), this));
        
    }

    if (newContext.getProperty() instanceof PropertyXRef) {
      PropertyXRef xref = (PropertyXRef)newContext.getProperty();
      xref = xref.getTarget();
      if (xref!=null)
        newContext = new Context(xref);
    }

    // anything we can refocus our editor to?
    if (editor!=null && newContext.getEntity()!=null && (!sticky.isSelected()||isActionPerformed)) 
      editor.setContext(newContext);
  
    // done
    populate(toolbar);
  }
  
  /**
   * @see genj.view.ToolBarSupport#populate(JToolBar)
   */
  public void populate(ToolBar toolbar) {

    this.toolbar = toolbar;
    if (toolbar==null)
      return;
    
    toolbar.beginUpdate();
    
    // editor?
    if (editor!=null) {
      for (Action a : editor.getActions())
        toolbar.add(a);
    }
    toolbar.addSeparator();

    // add sticky/focus/mode
    toolbar.add(new JToggleButton(sticky));
    toolbar.add(new JToggleButton(focus));
    toolbar.add(new JToggleButton(mode));
    
    // done
    toolbar.endUpdate();
  }
  
  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(256,480);
  }
  
  /**
   * Current entity
   */
  public Entity getEntity() {
    return editor.getContext().getEntity();
  }
    
//  /**
//   * ContextMenu
//   */
//  private class ContextMenu extends PopupWidget {
//    
//    /** constructor */
//    private ContextMenu() {
//      setIcon(Gedcom.getImage());
//      setToolTipText(resources.getString( "action.context.tip" ));
//    }
//    
//    /** override - popup creation */
//    protected JPopupMenu createPopup() {
//      // force editor to commit
//      editor.setContext(editor.getContext());
//      // create popup
//      return manager.getContextMenu(editor.getContext(), this);
//    }
//     
//  } //ContextMenu
  
  /**
   * Action - toggle sticky mode
   */
  private class Sticky extends Action2 {
    /** constructor */
    protected Sticky() {
      super.setImage(Images.imgStickOff);
      super.setTip(RESOURCES, "action.stick.tip");
      super.setSelected(false);
    }
    /** run */
    public void actionPerformed(ActionEvent event) {
      setSelected(isSelected());
    }
    @Override
    public boolean setSelected(boolean selected) {
      super.setImage(selected ? Images.imgStickOn : Images.imgStickOff);
      return super.setSelected(selected);
    }
  } //Sticky
  
  /**
   * Action - toggle focus mode
   */
  private class Focus extends Action2 {
    /** constructor */
    protected Focus() {
      super.setImage(Images.imgFocus);
      super.setTip(RESOURCES, "action.focus.tip");
      super.setSelected(false);
    }
    /** run */
    public void actionPerformed(ActionEvent event) {
      setSelected(isSelected());
      REGISTRY.put("focus", isSelected());
    }
  } //Sticky
  
  /**
   * Action - advanced or basic
   */
  private class Mode extends Action2 {
    private Mode() {
      setImage(Images.imgView);
      setTip(RESOURCES, "action.mode");
      super.setSelected(false);
    }
    public void actionPerformed(ActionEvent event) {
      setSelected(isSelected());
    }
    @Override
    public boolean setSelected(boolean selected) {
      REGISTRY.put("advanced", selected);
      if (getContext()!=null)
        setEditor(selected ? new AdvancedEditor(getContext().getGedcom(), EditView.this) : new BasicEditor(getContext().getGedcom(), EditView.this));
      populate(toolbar);
      return super.setSelected(selected);
    }
  } //Advanced

} //EditView
