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
import genj.gedcom.Indi;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.gedcom.Transaction;
import genj.util.ActionDelegate;
import genj.util.Registry;
import genj.util.swing.ButtonHelper;
import genj.view.Context;
import genj.view.ViewManager;
import genj.window.CloseWindow;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The basic version of an editor for a entity. Tries to
 * hide Gedcom complexity from the user while being flexible
 * in what it offers to edit information pertaining to an
 * entity.
 */
/*package*/ class BasicEditor extends Editor implements GedcomListener {

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
  
  /** header */
  private JLabel header = new JLabel();
  
  /** bean container */
  private JPanel beanPanel;
  
  /** actions */
  private ActionDelegate    
    ok   = new OK(), 
    cancel = new Cancel();

  /** beans */
  private Map beans = new HashMap();

  /**
   * Callback - init for edit
   */
  public void init(Gedcom gedcom, ViewManager manager, Registry registry) {

    // remember
    this.gedcom = gedcom;
    this.manager = manager;
    this.registry = registry;

    // create panel for beans
    beanPanel = new JPanel(new ColumnLayout());

    // create panel for actions
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    ButtonHelper bh = new ButtonHelper().setInsets(0).setContainer(buttons).setFocusable(false);
    bh.create(ok);
    bh.create(cancel);

    // layout
    setLayout(new BorderLayout());
    add(header                    , BorderLayout.NORTH );
    add(new JScrollPane(beanPanel), BorderLayout.CENTER);
    add(buttons                   , BorderLayout.SOUTH );

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
    if (entity==null)
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
   * Callback - set current context
   */
  public void setContext(Context context) {

    // applicable?
    Entity set = context.getEntity();
    if (!(set instanceof Indi) || entity == set)
      return;

    // continue
    setEntity(set);
    
  }

  /**
   * Set current entity
   */    
  public void setEntity(Entity set) {

    // remember
    entity = set;
    
    // setup header
    header.setIcon(entity==null?null:entity.getImage(false));
    header.setText(entity==null?null:Gedcom.getName(entity.getTag())+' '+entity.getId());

    // remove all current beans
    beanPanel.removeAll();
    path2panels.clear();
    beans.clear();

    // setup for new entity
    if (entity!=null) {

      // add beans for given entity
      String[] paths = { 
        "INDI:NAME", 
        "INDI:SEX", 
        "INDI:BIRT:DATE", 
        "INDI:BIRT:PLAC", 
        "INDI:DEAT:DATE",  
        "INDI:DEAT:PLAC",
        "INDI:OBJE:FILE", "INDI:NOTE"
      };
  
      // loop over configured paths
      for (int i=0; i<paths.length; i++) {
  
        // next path
        TagPath path = new TagPath(paths[i]);
  
        // analyze & create if necessary
        MetaProperty meta = MetaProperty.get(path);
        Property prop = entity.getProperty(path, Property.QUERY_VALID_TRUE|Property.QUERY_NO_LINK);
        if (prop == null)
          prop = meta.create("");
  
        // prepare bean 
        PropertyBean bean = PropertyBean.get(prop);
        bean.setAlignmentX(0);
        bean.init(entity.getGedcom(), prop, manager, registry);
  
        // listen to bean changes
        bean.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            ok.setEnabled(true);
            cancel.setEnabled(true);
          }
        });
        
        // keep it
        beans.put(path, bean);
  
        // add to corect panel
        getPanel(path).add(bean);
  
      }

    }
    
    // FIXME add references section to basiceditor
    
    // start without ok and cancel
    ok.setEnabled(false);
    cancel.setEnabled(false);
    
    // show
    revalidate();
    repaint();

    // done    
  }
  
  /**
   * Resolve a panel for given path
   * <pre>
   *  +----------+
   *  |+Name----+|
   *  ||        ||
   *  |+--------+|
   *  |+Birth---+|
   *  ||+Place-+||
   *  |||      |||
   *  ||+------+||
   *  |+--------+|
   *  +----------+
   * </pre>
   */
  private Container getPanel(final TagPath path) {
    
    // already known?
    JPanel result = (JPanel)path2panels.get(path);
    if (result!=null)
      return result;
      
    // root -> use main bean panel
    if (path.length()==1)
      return beanPanel;

    // create panel for it
    result = new JPanel();
    result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
    result.setBorder(new TitledBorder(Gedcom.getName(path.getLast())));

    // add to parent panel
    getPanel(new TagPath(path, path.length()-1)).add(result);
    
    // remember
    path2panels.put(path, result);

    // done
    return result;
  }

  /**
   * Callback - our current context
   */
  public Context getContext() {
    return new Context(gedcom, entity, null);
  }

  /**
   * A layout that arranges components in columns
   */
  public static class ColumnLayout implements LayoutManager {

    private int columnSize = 4;

    private Container analyzed;

    private int[] colWidths, colHeights, colComps;
    
    /**
     * layout container
     */
    public void layoutContainer(Container parent) {
     
      // analyze first
      analyzeColumns(parent);
      
      // compute additional space horizontally
      int extrax = parent.getWidth();
      for (int col=0;col<colWidths.length;col++)
       extrax -= colWidths[col];

      // place components
      for (int i=0,j=parent.getComponentCount(),col=0,x=0,y=0;i<j;i++) {
        
        Component c = parent.getComponent(i);

        // compute additional space vertically
        int extray = parent.getHeight() - colHeights[col];      
        
        // column width + part of extraw / it's preferred height
        int 
          h = c.getPreferredSize().height + extray/colComps[col],
          w = colWidths[col] + extrax/colWidths.length;
        
        // place
        c.setBounds(x, y, w, h);
        
        // inc y
        y+=h;

        // step into next col?        
        if (i%columnSize==columnSize-1) {
          y = 0;
          x+= w;
          col++;
        }
        
        // next component
      }
      
      // done      
    }

    /**
     * Calculate minium size
     */
    public Dimension minimumLayoutSize(Container parent) {
      return preferredLayoutSize(parent);
    }

    /**
     * Analyze columns
     */
    private void analyzeColumns(Container parent) {
      
      // done?
      if (analyzed==parent&&colWidths!=null&&colHeights!=null&&colComps!=null)
        return;

      // calculate width and height of each column
      int cols = 1+parent.getComponentCount()/columnSize;
      colWidths  = new int[cols];
      colHeights = new int[cols];
      colComps   = new int[cols];
      
      for (int i=0,j=parent.getComponentCount(),col=0;i<j;i++) {

        Component c = parent.getComponent(i);
        Dimension dim = c.getPreferredSize();
        colWidths [col] = Math.max(colWidths[col], dim.width);
        colHeights[col] += dim.height;
        colComps  [col] ++;

        if (i%columnSize==columnSize-1)
          col++;
      }
      
      // done
    }

    /**
     * Calculate preferred size
     */
    public Dimension preferredLayoutSize(Container parent) {

      // analyze first
      analyzeColumns(parent);
      
      // wrap
      Dimension result = new Dimension(0,0);
      for (int col=0;col<colWidths.length;col++) {
        result.width += colWidths[col];
        result.height = Math.max(result.height, colHeights[col]);
      }
      
      // done
      return result;
    }

    /**
     * add callback
     */
    public void addLayoutComponent(String name, Component comp) {
      colWidths = null;
      colHeights = null;
    }

    /**
     * remove callback
     */
    public void removeLayoutComponent(Component comp) {
      colWidths = null;
      colHeights = null;
    }

  } //ColumnLayout
  
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
      Iterator paths = beans.keySet().iterator();
      while (paths.hasNext()) {
        TagPath path = (TagPath)paths.next();
        PropertyBean bean = (PropertyBean)beans.get(path);
        Property prop = bean.getProperty();
        bean.commit();
        if (prop.getValue().length()>0&&prop.getParent()==null)
          add(prop, path, path.length());
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
      
      TagPath ppath = new TagPath(path, len-1);
      Property parent = entity.getProperty(ppath);
      if (parent==null) 
        parent = add(MetaProperty.get(ppath).create(""), path, len-1);

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
