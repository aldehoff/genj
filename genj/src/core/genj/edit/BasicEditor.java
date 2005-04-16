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
import genj.gedcom.GedcomListener;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.gedcom.Transaction;
import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.Registry;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.PopupWidget;
import genj.view.Context;
import genj.window.CloseWindow;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ContainerOrderFocusTraversalPolicy;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The basic version of an editor for a entity. Tries to hide Gedcom complexity from the user while being flexible in what it offers to edit information pertaining to an entity.
 */
/* package */class BasicEditor extends Editor implements GedcomListener {

  /** entity tag to layout */
  private static Map TAG2LAYOUT = new HashMap();
  
  /** our gedcom */
  private Gedcom gedcom = null;

  /** current entity */
  private Entity entity = null;

  /** registry */
  private Registry registry;

  /** edit */
  private EditView view;

  /** bean container */
  private JPanel beanPanel;
  
  /** beans */
  private List beans = new ArrayList(32);

  /** actions */
  private ActionDelegate ok = new OK(), cancel = new Cancel();

  /** change callback */
  ChangeListener changeCallback = new ChangeListener() {
    public void stateChanged(ChangeEvent e) {
      ok.setEnabled(true);
      cancel.setEnabled(true);
    }
  };

  /**
   * Callback - init for edit
   */
  public void init(Gedcom gedcom, EditView edit, Registry registry) {

    // remember
    this.gedcom = gedcom;
    this.view = edit;
    this.registry = registry;
    
    // make user focus root
    setFocusTraversalPolicy(new FocusPolicy());
    setFocusCycleRoot(true);

    // create panels for beans and links
    beanPanel = new JPanel();
    beanPanel.setBorder(new EmptyBorder(2,2,2,2));

    // create panel for actions
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    ButtonHelper bh = new ButtonHelper().setInsets(0).setContainer(buttons).setFocusable(false);
    bh.create(ok);
    bh.create(cancel);

    // layout
    setLayout(new BorderLayout());
    add(new JScrollPane(beanPanel), BorderLayout.CENTER);
    add(buttons, BorderLayout.SOUTH);

    // done
  }

  /**
   * Intercepted add notification
   */
  public void addNotify() {
    // let super continue
    super.addNotify();
    // listen to gedcom events
    gedcom.addGedcomListener(this);
  }

  /**
   * Intercepted remove notification
   */
  public void removeNotify() {
    // let super continue
    super.removeNotify();
    // stop listening to gedcom events
    gedcom.removeGedcomListener(this);
  }

  /**
   * Interpret gedcom changes
   */
  public void handleChange(Transaction tx) {
    // are we looking at something?
    if (entity == null)
      return;
    // entity affected?
    if (tx.get(Transaction.ENTITIES_DELETED).contains(entity)) {
      setEntity(null);
      return;
    }
    if (tx.get(Transaction.ENTITIES_MODIFIED).contains(entity)) {
      setEntity(entity);
    }
  }

  /**
   * Callback - our current context
   */
  public Context getContext() {
    return new Context(gedcom, entity, null);
  }

  /**
   * Callback - set current context
   */
  public void setContext(Context context) {

    // something to be committed?
    if (!gedcom.isTransaction()&&entity!=null&&ok.isEnabled()&&view.isCommitChanges()) 
      ok.trigger();

    // set if new
    Entity set = context.getEntity();
    if (entity != set) 
      setEntity(set);

    // select 1st/appropriate bean for property from context
    Property p = context.getProperty();
    select(p!=null?p:set);
    
    // done
  }
  
  /**
   * Select a property's bean
   */
  private void select(Property prop) {
    if (prop==null)
      return;
    // look for appropriate bean
    TagPath path = prop.getPath();
    for (int i=0,j=beanPanel.getComponentCount();i<j;i++) {
      JComponent c = (JComponent)beanPanel.getComponent(i);
      if (c instanceof PropertyBean && (path.length()==1||((PropertyBean)c).getPath().equals(path))) {
        c.requestFocusInWindow();
        break;
      }
    }
    // done
  }

  /**
   * Set current entity
   */
  private void setEntity(Entity set) {

    // remember
    entity = set;

    // remove all current beans
    beanPanel.removeAll();
    beans.clear();

    // setup layout
    if (entity!=null) try {
      
      // grab layout (lazy)
      NestedBlockLayout layout = getLayout(entity);
      beanPanel.setLayout(layout);

      Iterator cells = layout.getCells().iterator();
      while (cells.hasNext()) 
        parseCell((NestedBlockLayout.Cell)cells.next());
      
    } catch (Throwable t) {
      Debug.log(Debug.ERROR, this, t);
    }

    // start without ok and cancel
    ok.setEnabled(false);
    cancel.setEnabled(false);

    // show
    revalidate();
    repaint();

    // done
  }
  
  /**
   * Get a layout for given entity
   */
  private static NestedBlockLayout getLayout(Entity entity) throws IOException {

    // look up a cached one
    NestedBlockLayout result = (NestedBlockLayout)TAG2LAYOUT.get(entity.getTag());
    if (result==null) {
      result = new NestedBlockLayout(BasicEditor.class.getResourceAsStream("basic/"+entity.getTag()+".xml"));
      TAG2LAYOUT.put(entity.getTag(), result);
    }
    
    // return a private copy
    return result.copy();
  }

  /**
   * Init a cell 
   */
  private void parseCell(NestedBlockLayout.Cell cell) {
    TagPath path = new TagPath(cell.getAttribute("path"));
    
    // a label?
    if ("label".equals(cell.getElement())) {

      boolean plural = cell.getAttribute("plural")!=null;
      MetaProperty meta = MetaProperty.get(path);
      JLabel label;
      if (path.length()==1&&path.getLast().equals(entity.getTag()))
        label = new JLabel(Gedcom.getName(path.getLast()) + ' ' + entity.getId(), entity.getImage(false), SwingConstants.LEFT);
      else
        label = new JLabel(Gedcom.getName(path.getLast(), plural), meta.getImage(), SwingConstants.LEFT);
      
      beanPanel.add(label, cell);
      return;
    }
    
    // a bean?
    if ("bean".equals(cell.getElement())) {
      // conditional?
      if (cell.getAttribute("ifexists")!=null) {
        if (entity.getProperty(path)==null)
          return;
      }
      // create bean
      PropertyBean bean = createBean(entity, path, cell.getAttribute("type"));
      if (bean==null)
        return;
      
      // popup or normal?
      if (cell.getAttribute("popup")!=null) {
        beanPanel.add(new PopupBean(bean), cell);
      } else {
        beanPanel.add(bean, cell);
      }
      
      return;
    }

    // bug in the descriptor
    throw new IllegalArgumentException("Template element "+cell.getElement()+" is unkown");
  }
  
  /**
   * create a bean
   * @param entity we need the bean for
   * @param path path to property we need bean for
   * @param explicit bean type
   */
  private PropertyBean createBean(Entity entity, TagPath path, String type) {
    
    MetaProperty meta = MetaProperty.get(path, false);

    // try to resolve existing prop - this has to be a property along
    // the first possible path to avoid that in this case:
    //  INDI
    //   BIRT
    //    DATE sometime
    //   BIRT
    //    PLAC somewhere
    // the result of INDI:BIRT:DATE/INDI:BIRT:PLAC is
    //   somtime/somewhere
    Property prop = entity.getProperty(path);
    
    // .. for an existing reference we try to use a suitable
    // target property that we can edit inline
    if (prop instanceof PropertyXRef) {
      prop = ((PropertyXRef)prop).getTargetValueProperty();
      if (prop==null) 
        return null;
    }
    
    // created a new one?
    if (prop==null) 
      prop = meta.create("");
    
    // init bean
    PropertyBean bean = type!=null ? PropertyBean.get(type) : PropertyBean.get(prop);
    bean.init(entity.getGedcom(), prop, path, view.getViewManager(), registry);
    bean.addChangeListener(changeCallback);
    
    // remember
    beans.add(bean);
    
    // done
    return bean;
  }
  
  /**
   * A 'bean' we use for groups
   */
  private class PopupBean extends PopupWidget implements PropertyChangeListener {
    
    private PropertyBean wrapped;
    
    private Popup popup = null;
    
    /**
     * constructor
     */
    private PopupBean(PropertyBean wrapped) {
      
      // remember wrapped
      this.wrapped = wrapped;
      
      // prepare image
      Property prop = wrapped.getProperty();
      ImageIcon img = prop.getImage(false);
      if (prop.getParent()==null)
        img = img.getDisabled(50);
      setIcon(img);
      setToolTipText(wrapped.getProperty().getPropertyName());
  
      // fix looks
      setFocusable(false);
      setBorder(null);
      
      // done
    }
    
    /** lifecycle callback */
    public void addNotify() {
      // list to focus changes
      KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner", this);
      // continue
      super.addNotify();
    }
    
    /** lifecycle callback */
    public void removeNotify() {
      // still a popup showing?
      if (popup!=null) {
        popup.hide();
        popup = null;
      }
      // stop listening to focus changes
      KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener("focusOwner", this);
      // continue
      super.removeNotify();
    }
    /** button press callback */
    public void showPopup() {

      // clear current?
      if (popup!=null) {
        popup.hide();
        popup=null;
      }
      // prepare a panel with wrapped
      JPanel content = new JPanel(new BorderLayout());
      content.add(new JLabel(wrapped.getProperty().getPropertyName()), BorderLayout.NORTH);
      content.add(wrapped, BorderLayout.CENTER);
      // show popup
      Point pos = getLocationOnScreen();
      popup = PopupFactory.getSharedInstance().getPopup(this, content, pos.x+getWidth(), pos.y);
      popup.show();
      // request focus
      SwingUtilities.getWindowAncestor(wrapped).setFocusableWindowState(true);
      wrapped.requestFocus();
      // update image
      setIcon(wrapped.getProperty().getImage(false));
    }
    
    /** 
     * focus property change notification 
     * (this doesn't seem to be invoke in Java 1.4.1 with a user clicking on a textfield)
     */
    public void propertyChange(PropertyChangeEvent evt) {
      // popup visible?
      if (popup==null)
        return;
      // a new focus owner?
      if (evt.getNewValue()==null)
        return;
      // a sub-component of this?
      Component focus = (Component)evt.getNewValue();
      while (true) {
        if (focus==wrapped) 
          return;
        if (focus==null)
          break;
        focus = focus.getParent();
      }
      // get rid of popup
      popup.hide();
      popup = null;
      // done
    }        
  } //Label
  
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

      // commit changes
      Transaction tx = gedcom.startTransaction();

      // commit bean changes
      try {
        for (int i=0,j=beans.size();i<j;i++) {
          PropertyBean bean = (PropertyBean)beans.get(i);
          bean.commit(tx);
          Property prop = bean.getProperty();
          TagPath path = bean.getPath();
          if (prop.getValue().length() > 0 && prop.getParent() == null)
            add(prop, path, path.length());
        }
      } catch (Throwable t) {
        Debug.log(Debug.ERROR, this, t);
      }

      // end transaction - fake entity==null because we're doing the change here
      Entity old = entity;
      entity = null;
      gedcom.endTransaction();
      entity = old;

      // disable commit/cancel since all changes are committed
      ok.setEnabled(false);
      cancel.setEnabled(false);
      
      // done
    }

    private Property add(Property prop, TagPath path, int len) {

      TagPath ppath = new TagPath(path, len - 1);
      Property parent = entity.getProperty(ppath);
      if (parent == null)
        parent = add(MetaProperty.get(ppath).create(""), path, len - 1);

      // add it
      parent.addProperty(prop);

      // done
      return prop;
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

      // re-set for cancel
      setEntity(entity);
    }

  } //Cancel

  /**
   * The default container FocusTravelPolicy works based on
   * x/y coordinates which doesn't work well with the column
   * layout used.
   * ContainerOrderFocusTraversalPolicy would do fine but Sun
   * (namely David Mendenhall) in its eternal wisdom has decided
   * to put the working accept()-check into a protected method
   * of LayoutFocusTraversalPolicy basically rendering the
   * former layout useless.
   * I'm doing a hack to get the ContainerOrderFTP with
   * LayoutFTP's accept :(
   */
  private class FocusPolicy extends ContainerOrderFocusTraversalPolicy {
    private Hack hack = new Hack();
    protected boolean accept(Component c) {
      return hack.accept(c);
    }
    private class Hack extends LayoutFocusTraversalPolicy {
      protected boolean accept(Component c) {
        return super.accept(c);
      }
    }
  } //FocusPolicy
  
} //BasicEditor
