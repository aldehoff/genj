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
import genj.view.Context;
import genj.view.ViewManager;
import genj.window.CloseWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
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
     " INDI:NAME INDI:SEX\n"+
     "$BIRT\n"+
     " INDI:BIRT:DATE\n"+
     " INDI:BIRT:PLAC\n"+
     "$DEAT\n"+
     " INDI:DEAT:DATE\n"+ 
     " INDI:DEAT:PLAC\n"+ 
     "$OCCU\n"+
     " INDI:OCCU\n"+
     " INDI:OCCU:DATE\n"+
     " INDI:OCCU:PLAC\n"+
     "$RESI\n"+
     " INDI:RESI:DATE\n"+
     " INDI:RESI:ADDR\n"+
     "$CITY INDI:RESI:ADDR:CITY\n"+
     "$POST INDI:RESI:ADDR:POST\t"+
     "$OBJE\n"+
     "$TITL INDI:OBJE:TITL\n"+
     " INDI:OBJE:FILE\n"+
     "$NOTE\n"+
     " INDI:NOTE");
    
    TEMPLATES.put(Gedcom.FAM,
     "$MARR\n"+
     " FAM:MARR:DATE\n"+
     " FAM:MARR:PLAC\n"+
     "$DIV\n"+
     " FAM:DIV:DATE\n"+
     " FAM:DIV:PLAC\n"+
     "$OBJE\n"+
     " $TITL FAM:OBJE:TITL\n"+
     " FAM:OBJE:FILE\n"+
     "$NOTE\n"+
     " FAM:NOTE");
  
    TEMPLATES.put(Gedcom.OBJE,
      "$TITL OBJE:TITL\n"+
      "$FORM OBJE:FORM\n"+
      " OBJE:BLOB\n"+
      "$NOTE\n"+
      " OBJE:NOTE");
    
    TEMPLATES.put(Gedcom.NOTE,
      " NOTE:NOTE");
      
    TEMPLATES.put(Gedcom.REPO,
      "$NAME REPO:NAME\n"+
      "$ADDR\n"+
      " REPO:ADDR\n"+
      "$CITY REPO:ADDR:CITY\n"+
      "$POST REPO:ADDR:POST\n"+
      "$NOTE\n"+
      " REPO:NOTE");
    
    TEMPLATES.put(Gedcom.SOUR,
      "$AUTH\n"+
      " SOUR:AUTH\n"+
      "$TITL\n"+
      " SOUR:TITL\n"+
      "$TEXT\n"+
      " SOUR:TEXT\n"+
      "$OBJE\n"+
      "$TITL SOUR:OBJE:TITL\n"+
      " SOUR:OBJE:FILE\n"+
      "$NOTE\n"+
      " SOUR:NOTE");
    
    TEMPLATES.put(Gedcom.SUBM,
      "$NAME SUBM:NAME\n"+
      "$ADDR\n"+
      " SUBM:ADDR\n"+
      "$CITY SUBM:ADDR:CITY\n"+
      "$POST SUBM:ADDR:POST\n"+
      "$OBJE\n"+
      "$TITL SUBM:OBJE:TITL\n"+
      " SUBM:OBJE:FILE\n"+
      "$LANG SUBM:LANG\n"+
      "$RFN SUBM:RFN\n"+
      "$RIN SUBM:RIN");
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

  /** path2panels */
  private HashMap path2panels = new HashMap();

  /** bean container */
  private JPanel beanPanel;

  /** actions */
  private ActionDelegate ok = new OK(), cancel = new Cancel();

  /** change callback */
  ChangeListener changeCallback = new ChangeListener() {
    public void stateChanged(ChangeEvent e) {
      ok.setEnabled(true);
      cancel.setEnabled(true);
    }
  };

  /** beans */
  private Map path2beans = new HashMap();

  /**
   * Callback - init for edit
   */
  public void init(Gedcom gedcom, ViewManager manager, Registry registry) {

    // remember
    this.gedcom = gedcom;
    this.manager = manager;
    this.registry = registry;

    // create panels for beans and links
    beanPanel = new JPanel(new NestedBlockLayout(true, 2));
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
      PropertyBean bean = (PropertyBean) path2beans.get(prop.getPath());
      if (bean != null)
        bean.requestFocusInWindow();
    }

  }

  /**
   * Set current entity
   */
  public void setEntity(Entity set) {

    // remember
    entity = set;

    // remove all current beans
    beanPanel.removeAll();
    path2panels.clear();
    path2beans.clear();

    // setup for new entity
    if (entity!=null) 
      setupBeanPanel(entity);

    // start without ok and cancel
    ok.setEnabled(false);
    cancel.setEnabled(false);

    // show
    revalidate();
    repaint();

    // done
  }
  
  /**
   * create a text label
   */
  private JLabel createLabel(String txt, ImageIcon img, boolean bold) {
    JLabel result = new JLabel(txt, img, SwingConstants.LEFT);
    if (bold)
      result.setFont(result.getFont().deriveFont(Font.BOLD));
    return result;
  }

  /**
   * setup bean panel
   */
  private void setupBeanPanel(Entity entity) {

    NestedBlockLayout layout = (NestedBlockLayout)beanPanel.getLayout();
    
    // add one special every time
    beanPanel.add(createLabel(Gedcom.getName(entity.getTag()) + ' ' + entity.getId(), entity.getImage(false), true));
    layout.createBlock(1);
    
    // apply template
    String template = (String)TEMPLATES.get(entity.getTag());
    if (template==null)
      return;
    
    StringTokenizer rows = new StringTokenizer(template, "\n\t", true);
    while (rows.hasMoreTokens()) {

      String row = rows.nextToken();
      
      // next row
      if (row.equals("\n")) {
        layout.createBlock(1);
        continue;
      }
      
      // next column
      if (row.equals("\t")) {
        layout.createBlock(0);
        continue;
      }
      
      // parse elements
      StringTokenizer comps = new StringTokenizer(row, " ");
      while (comps.hasMoreTokens()) {
        
        String comp = comps.nextToken().trim();

        // text or bean?
        if (comp.startsWith("$")) {
          beanPanel.add(createLabel(Gedcom.getName(comp.substring(1)), null, !comps.hasMoreTokens()));
        } else {
          TagPath path = new TagPath(comp);

          // get meta information for path
          MetaProperty meta = MetaProperty.get(path);

          // resolve prop for bean
          // FIXME gotta think about this one - can we simply skip xrefs?
          Property prop = entity.getProperty(path);
          if (prop == null || prop instanceof PropertyXRef)
            prop = meta.create("");

          // prepare bean
          PropertyBean bean = PropertyBean.get(prop);
          bean.init(entity.getGedcom(), prop, manager, registry);
          path2beans.put(path, bean);

          // add bean
          beanPanel.add(bean, bean.getWeight());

          // listen to bean changes
          bean.addChangeListener(changeCallback);

        }

      }

    }

    // done
    // FIXME need to add paths for INDI:FAMS, INDI:FAMC, FAM:HUSB, FAM:WIFE, FAM:CHIL
  }

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
      
      // FIXME creating missing parent props doesn't work all the time
      try {
        Iterator paths = path2beans.keySet().iterator();
        while (paths.hasNext()) {
          TagPath path = (TagPath) paths.next();
          PropertyBean bean = (PropertyBean) path2beans.get(path);
          Property prop = bean.getProperty();
          bean.commit();
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
