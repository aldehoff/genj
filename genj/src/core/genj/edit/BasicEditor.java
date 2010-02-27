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
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.Property;
import genj.gedcom.UnitOfWork;
import genj.util.Registry;
import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import genj.view.ContextProvider;
import genj.view.ViewContext;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.FocusManager;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import spin.Spin;

/**
 * The basic version of an editor for a entity. Tries to hide Gedcom complexity from the user while being flexible in what it offers to edit information pertaining to an entity.
 */
/* package */class BasicEditor extends Editor implements ContextProvider {

  final static Registry REGISTRY = Registry.get(BasicEditor.class);
  
  /** our gedcom */
  private Gedcom gedcom = null;

  /** current entity */
  private Entity currentEntity = null;

  /** edit */
  private EditView view;
  
  /** actions */
  private OK ok = new OK();
  private Cancel cancel = new Cancel();
  
  /** current panels */
  private BeanPanel beanPanel;
  private JPanel buttonPanel;
  
  private transient GedcomListener callback = new Callback();

  /**
   * Constructor
   */
  public BasicEditor(Gedcom gedcom, EditView edit) {

    // remember
    this.gedcom = gedcom;
    this.view = edit;
    
    // create panel for beans
    beanPanel = new BeanPanel();
    beanPanel.addChangeListener(ok);
    beanPanel.addChangeListener(cancel);

    // create panel for actions
    buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    ButtonHelper bh = new ButtonHelper().setInsets(0).setContainer(buttonPanel);
    bh.create(ok).setFocusable(false);    
    bh.create(cancel).setFocusable(false);
    
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new JScrollPane(beanPanel));
    add(BorderLayout.SOUTH, buttonPanel);
    
    // done
  }
  
  /**
   * Intercepted add notification
   */
  @Override
  public void addNotify() {
    // let super continue
    super.addNotify();
    // listen to gedcom events
    gedcom.addGedcomListener((GedcomListener)Spin.over(callback));
    // done
  }

  /**
   * Intercepted remove notification
   */
  @Override
  public void removeNotify() {
    // stop listening to gedcom events
    gedcom.removeGedcomListener((GedcomListener)Spin.over(callback));
    // let super continue
    super.removeNotify();
  }

  /**
   * Callback - our current context
   */
  public ViewContext getContext() {
    // try to find a bean with focus
    PropertyBean bean = getFocus();
    if (bean!=null&&bean.getContext()!=null) 
      return bean.getContext();
    // currently edited?
    if (currentEntity!=null)
      return new ViewContext(currentEntity);
    // gedcom at least
    return new ViewContext(gedcom);
  }

  /**
   * Callback - set current context
   */
  @Override
  public void setContext(Context context) {
    
    actions.clear();
    
    // clear?
    if (context.getGedcom()==null) {
      setEntity(null, null);
      return;
    }
    
    // a different entity to look at?
    if (currentEntity != context.getEntity()) {
      
      // change entity being edited
      setEntity(context.getEntity(), context.getProperty());
      
    } else {

      // simply change focus if possible
      if (beanPanel!=null && view.isGrabFocus())
        beanPanel.select(context.getProperty());
      
    }
    
    // look for actions
    for (PropertyBean bean : beanPanel.getBeans())
      actions.addAll(bean.getActions());
    
    // done
  }
  
  @Override
  public void commit() {
    
    // something to commit?
    if (!ok.isEnabled())
      return;
    
    // commit changes (without listing to the change itself)
    try {
      gedcom.removeGedcomListener((GedcomListener)Spin.over(callback));
      gedcom.doMuteUnitOfWork(new UnitOfWork() {
        public void perform(Gedcom gedcom) {try {
          beanPanel.commit();
        } finally {
          ok.setEnabled(false);
        }}
      });
    } finally {
      gedcom.addGedcomListener((GedcomListener)Spin.over(callback));
    }

    // lookup current focus now (any temporary props are committed now)
    PropertyBean focussedBean = getFocus();
    Property focus = focussedBean !=null ? focussedBean.getProperty() : null;
    
    // set selection
    if (view.isGrabFocus())
      beanPanel.select(focus);

    // done
    ok.setEnabled(false);
    cancel.setEnabled(false);
  }
  
  /**
   * Set current entity
   */
  public void setEntity(Entity set, Property focus) {
    
    // commit what needs to be committed
    if (ok.isEnabled()&&!gedcom.isWriteLocked()&&currentEntity!=null&&view.isCommitChanges()) 
      commit();

    // remember
    currentEntity = set;
    
    // try to find focus receiver if need be
    if (focus==null) {
      // last bean's property would be most appropriate
      PropertyBean bean = getFocus();
      if (bean!=null&&bean.getProperty()!=null&&bean.getProperty().getEntity()==currentEntity) focus  = bean.getProperty();
      // fall back to entity itself
      if (focus==null) focus = currentEntity;
    }
    
    // remove all we've setup to this point
    beanPanel.setRoot(currentEntity);

    // start without ok and cancel
    ok.setEnabled(false);
    cancel.setEnabled(false);

    // set focus
    if (focus!=null && view.isGrabFocus())
      beanPanel.select(focus);

    // done
  }
  
  /**
   * Find currently focussed PropertyBean
   */
  private PropertyBean getFocus() {
    
    Component focus = FocusManager.getCurrentManager().getFocusOwner();
    while (focus!=null&&!(focus instanceof PropertyBean))
      focus = focus.getParent();
    
    if (focus==null)
      return null;
    
    return SwingUtilities.isDescendingFrom(focus, this) ? (PropertyBean)focus : null;

  }

  /**
   * A ok action
   */
  private class OK extends Action2 implements ChangeListener {

    /** constructor */
    private OK() {
      setText(Action2.TXT_OK);
    }

    /** cancel current proxy */
    public void actionPerformed(ActionEvent event) {
      commit();
    }
    
    public void stateChanged(ChangeEvent e) {
      setEnabled(true);
    }

  } //OK

  /**
   * A cancel action
   */
  private class Cancel extends Action2 implements ChangeListener {

    /** constructor */
    private Cancel() {
      setText(Action2.TXT_CANCEL);
    }

    /** cancel current proxy */
    public void actionPerformed(ActionEvent event) {
      // disable ok&cancel
      ok.setEnabled(false);
      cancel.setEnabled(false);

      // re-set for cancel
      setEntity(currentEntity, null);
    }
    
    public void stateChanged(ChangeEvent e) {
      setEnabled(true);
    }

  } //Cancel
  
  /**
   * our gedcom callback for others changing the gedcom information
   */
  private class Callback extends GedcomListenerAdapter {
    
    private Property setFocus;
    
    public void gedcomWriteLockAcquired(Gedcom gedcom) {
      setFocus = null;
    }
    
    public void gedcomWriteLockReleased(Gedcom gedcom) {
      setEntity(currentEntity, setFocus);
    }
    
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
      if (currentEntity==entity)
        currentEntity = null;
    }
    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
      if (setFocus==null && property.getEntity()==currentEntity) {
        setFocus = added;
      }
    }
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
      if (setFocus==null && property.getEntity()==currentEntity)
        setFocus = property;
    }
  };
  
} //BasicEditor
