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
import genj.gedcom.PropertyNote;
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
import genj.view.ViewManager;
import genj.window.CloseWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

  // templates for entities
  private final static HashMap TEMPLATES = new HashMap();
  
  static {
    
    TEMPLATES.put(Gedcom.INDI,
     "'INDI'\n"+
     " INDI:NAME INDI:SEX\n"+
     "'INDI:BIRT' (INDI:BIRT:NOTE)\n"+
     " INDI:BIRT:DATE\n"+
     " INDI:BIRT:PLAC\n"+
     "'INDI:DEAT' (INDI:DEAT:NOTE)\n"+
     " INDI:DEAT:DATE\n"+ 
     " INDI:DEAT:PLAC\n"+ 
     "'INDI:OCCU' (INDI:OCCU:NOTE)\n"+
     " INDI:OCCU\n"+
     " INDI:OCCU:DATE\n"+
     " INDI:OCCU:PLAC\n"+
     "'INDI:RESI' (INDI:RESI:NOTE)\n"+
     " INDI:RESI:DATE\n"+
     " INDI:RESI:ADDR\n"+
     "'INDI:RESI:ADDR:CITY' INDI:RESI:ADDR:CITY\n"+
     "'INDI:RESI:ADDR:POST' INDI:RESI:ADDR:POST\t"+
     "'INDI:OBJE'\n"+
     "'INDI:OBJE:TITL' INDI:OBJE:TITL\n"+
     " INDI:OBJE:FILE\n"+
     "'INDI:NOTE'\n"+
     " INDI:NOTE");
    
    TEMPLATES.put(Gedcom.FAM,
     "'FAM'\n"+
     "'FAM:MARR' @FAM:MARR:NOTE\n"+
     " FAM:MARR:DATE\n"+
     " FAM:MARR:PLAC\n"+
     "'FAM:ENGA'\n"+
     " FAM:ENGA:DATE\n"+
     " FAM:ENGA:PLAC\n"+
     "'FAM:DIV'\n"+
     " FAM:DIV:DATE\n"+
     " FAM:DIV:PLAC\n"+
     "'FAM:EVEN'\n"+
     "'FAM:EVEN:TYPE' FAM:EVEN:TYPE\n"+
     " FAM:EVEN:DATE\n"+
     " FAM:EVEN:PLAC\t"+
     "'FAM:OBJE'\n"+
     "'FAM:OBJE:TITL' FAM:OBJE:TITL\n"+
     " FAM:OBJE:FILE\n"+
     "'FAM:NOTE'\n"+
     " FAM:NOTE");
  
    TEMPLATES.put(Gedcom.OBJE,
      "'OBJE'\n"+
      "'OBJE:TITL' OBJE:TITL\n"+
      "'OBJE:FORM' OBJE:FORM\n"+
      " OBJE:BLOB\n"+
      "'OBJE:NOTE'\n"+
      " OBJE:NOTE");
    
    TEMPLATES.put(Gedcom.NOTE,
      "'NOTE'\n"+
      " NOTE:NOTE");
      
    TEMPLATES.put(Gedcom.REPO,
      "'REPO'\n"+
      "'REPO:NAME' REPO:NAME\n"+
      "'REPO:ADDR'\n"+
      " REPO:ADDR\n"+
      "'REPO:ADDR:CITY' REPO:ADDR:CITY\n"+
      "'REPO:ADDR:POST' REPO:ADDR:POST\n"+
      "'REPO:NOTE'\n"+
      " REPO:NOTE");
    
    TEMPLATES.put(Gedcom.SOUR,
      "'SOUR:AUTH'\nSOUR:AUTH\n"+
      "'SOUR:TITL'\nSOUR:TITL\n"+
      "'SOUR:TEXT'\nSOUR:TEXT\n"+
      "'SOUR:OBJE'\n"+
      "'SOUR:OBJE:TITL' SOUR:OBJE:TITL\n"+
      " SOUR:OBJE:FILE\n"+
      "'SOUR:NOTE'\n"+
      " SOUR:NOTE");
    
    TEMPLATES.put(Gedcom.SUBM,
      "'SUBM:NAME' SUBM:NAME\n"+
      "'SUBM:ADDR'\n"+
      " SUBM:ADDR\n"+
      "'SUBM:ADDR:CITY' SUBM:ADDR:CITY\n"+
      "'SUBM:ADDR:POST' SUBM:ADDR:POST\n"+
      "'SUBM:OBJE'\n"+
      "'SUBM:OBJE:TITL' SUBM:OBJE:TITL\n"+
      " SUBM:OBJE:FILE\n"+
      "'SUBM:LANG' SUBM:LANG\n"+
      "'SUBM:RFN' SUBM:RFN\n"+
      "'SUBM:RIN' SUBM:RIN");
  }
  

  /** colors for tabborders */
  private final static Color[] COLORS = { Color.GRAY, new Color(192, 48, 48), new Color(48, 48, 128), new Color(48, 128, 48), new Color(48, 128, 128), new Color(128, 48, 128), new Color(96, 64, 32), new Color(32, 64, 96) };

  /** our gedcom */
  private Gedcom gedcom = null;

  /** current entity */
  private Entity entity = null;

  /** registry */
  private Registry registry;

  /** view manager */
  private ViewManager manager;

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
  public void init(Gedcom gedcom, ViewManager manager, Registry registry) {

    // remember
    this.gedcom = gedcom;
    this.manager = manager;
    this.registry = registry;

    // create panels for beans and links
    beanPanel = new JPanel(new NestedBlockLayout(true, 3));
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

    // set if new
    Entity set = context.getEntity();
    if (entity != set)
      setEntity(set);

    // check specific property
    Property prop = context.getProperty();
    if (prop != null) {
      TagPath path = prop.getPath();
      for (int i=0,j=beanPanel.getComponentCount();i<j;i++) {
        JComponent c = (JComponent)beanPanel.getComponent(i);
        if (c instanceof PropertyBean && ((PropertyBean)c).getPath().equals(path)) {
          c.requestFocusInWindow();
          break;
        }
      }
    }
    
    // done
  }

  /**
   * Set current entity
   */
  public void setEntity(Entity set) {

    // remember
    entity = set;

    // remove all current beans
    beanPanel.removeAll();
    beans.clear();

    // setup for new entity
    if (entity!=null) 
      createBeans(entity);

    // start without ok and cancel
    ok.setEnabled(false);
    cancel.setEnabled(false);

    // show
    revalidate();
    repaint();

    // done
  }
  
  /**
   * create a bean
   */
  private PropertyBean createBean(Entity entity, TagPath path) {
    
    MetaProperty meta = MetaProperty.get(path);

    // resolve prop & bean
    Property prop = entity.getProperty(path);
    for (int s=0;prop instanceof PropertyXRef;s++) {
      // we know how to handle a PropertyNote
      if (prop instanceof PropertyNote) {
        prop = prop.getProperty(new TagPath("*:..:NOTE"));
        if (prop!=null)
          break;
      }
      // try next
      prop = entity.getProperty(new TagPath(path, path.length()-1, s));
    }
    
    // .. fallback to newly created one?
    if (prop == null)
      prop = meta.create("");
    
    PropertyBean bean = PropertyBean.get(prop);
    bean.init(entity.getGedcom(), prop, path, manager, registry);
    bean.addChangeListener(changeCallback);
    
    // remember
    beans.add(bean);
    
    // done
    return bean;
  }
  
  /**
   * parse entity and path
   */
  private void createBeans(Entity entity, String path) {
    
    // a 'label'?
    if (path.startsWith("'")&&path.endsWith("'")) {
      path = path.substring(1, path.length()-1);
      // add label
      MetaProperty meta = MetaProperty.get(new TagPath(path));
      if (Entity.class.isAssignableFrom(meta.getType()))
        beanPanel.add(new JLabel(meta.getName() + ' ' + entity.getId(), entity.getImage(false), SwingConstants.LEFT));
      else
        beanPanel.add(new JLabel(meta.getName()));
      // done
      return;
    } 
    // maybe a popup bean?
    if (path.startsWith("(")&&path.endsWith(")")) {
      path = path.substring(1, path.length()-1);
      PopupBean popup = new PopupBean(createBean(entity, new TagPath(path)));
      beanPanel.add(popup);
      // done
      return;
    }
    
    // standard bean!
    PropertyBean bean = createBean(entity, new TagPath(path));
    beanPanel.add(bean, bean.getWeight());

    // done
  }
  
  /**
   * setup bean panel
   */
  private void createBeans(Entity entity) {

    NestedBlockLayout layout = (NestedBlockLayout)beanPanel.getLayout();

    // apply template
    String template = (String)TEMPLATES.get(entity.getTag());
    if (template==null)
      return;
    
    StringTokenizer rows = new StringTokenizer(template, "\n\t", true);
    while (rows.hasMoreTokens()) {
      String row = rows.nextToken();
      // new row?
      if (row.equals("\n")) {
        layout.createBlock(1);
        continue;
      }
      // new column?
      if (row.equals("\t")) {
        layout.createBlock(0);
        continue;
      }
      // parse elements
      StringTokenizer paths = new StringTokenizer(row, " ");
      while (paths.hasMoreTokens()) 
        createBeans(entity, paths.nextToken().trim());
      // next row
    }

    // done
    // FIXME need to add paths for INDI:FAMS, INDI:FAMC, FAM:HUSB, FAM:WIFE, FAM:CHIL
  }

  /**
   * A 'bean' we use for groups
   */
  private static class PopupBean extends PopupWidget implements PropertyChangeListener {
    
    private PropertyBean wrapped;
    
    private Popup popup = null;
    
    /**
     * constructor
     */
    private PopupBean(PropertyBean wrapped) {
      
      this.wrapped = wrapped;
  
      // prepare image
      Property prop = wrapped.getProperty();
      ImageIcon img = prop.getImage(false);
      if (prop.getParent()==null)
        img = img.getDisabled(50);
      setIcon(img);
  
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
      // screen pos
      Point pos = getLocationOnScreen();
      pos.x += getWidth()/2;
      pos.y += getHeight()/2;
      // show popup
      popup = PopupFactory.getSharedInstance().getPopup(this, wrapped, pos.x, pos.y);
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
      gedcom.startTransaction();

      // commit bean changes
      try {
        for (int i=0,j=beans.size();i<j;i++) {
          PropertyBean bean = (PropertyBean)beans.get(i);
          bean.commit();
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

} //BasicEditor
