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
import genj.edit.beans.XRefBean;
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
import genj.util.swing.ColumnLayout;
import genj.util.swing.ImageIcon;
import genj.view.Context;
import genj.view.ViewManager;
import genj.window.CloseWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The basic version of an editor for a entity. Tries to
 * hide Gedcom complexity from the user while being flexible
 * in what it offers to edit information pertaining to an
 * entity.
 */
/*package*/ class BasicEditor extends Editor implements GedcomListener {

  // beans for given entity
  private final static String[] PATHS = { 
    "INDI:NAME", 
    "INDI:SEX", 
    "INDI:BIRT:DATE", 
    "INDI:BIRT:PLAC", 
    "INDI:DEAT:DATE",  
    "INDI:DEAT:PLAC",
    "INDI:OCCU",
    "INDI:OCCU:DATE",
    "INDI:OCCU:PLAC",
    "INDI:RESI:DATE",
    "INDI:RESI:ADDR",
    "INDI:RESI:ADDR:CITY",
    "INDI:RESI:ADDR:POST",
    "INDI:OBJE:TITL",
    "INDI:OBJE:FILE", 
    "INDI:NOTE",
    
    "FAM:MARR:DATE",    "FAM:MARR:PLAC",
    "FAM:DIV:DATE",
    "FAM:DIV:PLAC",
    "FAM:OBJE:TITL",
    "FAM:OBJE:FILE", 
    "FAM:NOTE",
    
    "OBJE:TITL",
    "OBJE:FORM",
    "OBJE:BLOB",
    "OBJE:NOTE",
    
    "NOTE:NOTE",
    
    "REPO:NAME",
    "REPO:ADDR",
    "REPO:ADDR:CITY",
    "REPO:ADDR:POST",
    "REPO:NOTE",
    
    "SOUR:AUTH",
    "SOUR:TITL",
    "SOUR:TEXT",
    "SOUR:OBJE:TITL",
    "SOUR:OBJE:FILE",
    "SOUR:NOTE",
    
    "SUBM:NAME",
    "SUBM:ADDR",
    "SUBM:ADDR:CITY",
    "SUBM:ADDR:POST",
    "SUBM:OBJE:TITL",
    "SUBM:OBJE:FILE",
    "SUBM:LANG",
    "SUBM:RFN",
    "SUBM:RIN"
  };
  

  /** colors for tabborders */
  private final static Color[] COLORS = {
    Color.GRAY,
    new Color(192, 48, 48),
    new Color( 48, 48,128),
    new Color( 48,128, 48),
    new Color( 48,128,128),
    new Color(128, 48,128),
    new Color( 96, 64, 32),
    new Color( 32, 64, 96)
  };
    
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
  
  /** link container */
  private JPanel linkPanel;
  
  /** actions */
  private ActionDelegate    
    ok   = new OK(), 
    cancel = new Cancel();

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
    beanPanel = new JPanel(new ColumnLayout(6));
    linkPanel = new JPanel(new ColumnLayout(100));
    
    // create panel for actions
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    ButtonHelper bh = new ButtonHelper().setInsets(0).setContainer(buttons).setFocusable(false);
    bh.create(ok);
    bh.create(cancel);

    // layout
    setLayout(new BorderLayout());
    add(header                    , BorderLayout.NORTH );
    add(new JScrollPane(beanPanel), BorderLayout.CENTER);
//    add(new JScrollPane(linkPanel), BorderLayout.EAST);
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

    // set if new
    Entity set = context.getEntity();
    if (entity != set)
      setEntity(set);
      
    // check specific property
    Property prop = context.getProperty();
    if (prop!=null) {
      PropertyBean bean = (PropertyBean)path2beans.get(prop.getPath());
      if (bean!=null) 
        bean.requestFocusInWindow();
    }
    
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
    linkPanel.removeAll();
    path2panels.clear();
    path2beans.clear();

    // setup for new entity
    if (entity!=null) {

      // create beans for all configured paths
      for (int i=0; i<PATHS.length; i++) {
  
        // an applicable path?
        if (!PATHS[i].startsWith(entity.getTag()+':'))
          continue;

        TagPath path = new TagPath(PATHS[i]);
  
        // analyze & create if necessary
        MetaProperty meta = MetaProperty.get(path);
        Property prop = entity.getProperty(path);
        if (prop == null || prop instanceof PropertyXRef)
          prop = meta.create("");
  
        // prepare bean 
        PropertyBean bean = PropertyBean.get(prop);
        bean.init(entity.getGedcom(), prop, manager, registry);

        ColumnLayout.setWeight(bean, bean.getWeight());
  
        // listen to bean changes
        bean.addChangeListener(changeCallback);
        
        // keep it
        path2beans.put(path, bean);
  
        // add to corect panel
        getPanel(path).add(bean);
  
      }

// FIXME need links showing in basic mode
      // create links
      Iterator xrefs = entity.getProperties(PropertyXRef.class).iterator();
      while (xrefs.hasNext()) {
        PropertyXRef xref = (PropertyXRef)xrefs.next();
        if (xref.isValid()) {
          linkPanel.add(new JLabel(Gedcom.getName(xref.getTag())));
          XRefBean bean = new XRefBean();
          bean.init(entity.getGedcom(), xref, manager, registry);
          ColumnLayout.setWeight(bean, new Point2D.Double(1.0,0));
          linkPanel.add(bean);
        }
      }

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
   * Resolve a panel for given path
   * <pre>
   *  +-----------+
   *  |N+--------+|
   *  |A|        ||
   *  |M|        ||
   *  |E+--------+|
   *  |           |
   *  |B+--------+|
   *  |I|P+-----+||
   *  |R|L|     |||
   *  |T|A|     |||
   *  | |C+-----+||
   *  | |        ||
   *  | |D+-----+||
   *  | |A|     |||
   *  | |T|     |||
   *  | |E+-----+||
   *  | +--------+|
   *  |           |
   *  +-----------+
   * </pre>
   */
  private JComponent getPanel(final TagPath path) {
    
    // already known?
    JPanel result = (JPanel)path2panels.get(path);
    if (result!=null)
      return result;
      
    // root -> use main bean panel
    if (path.length()==1)
      return beanPanel;


    // create border & panel for it
    MetaProperty meta = MetaProperty.get(path);
    TabBorder border = new TabBorder(meta.getName(), meta.getImage());

    result = new JPanel();
    result.setLayout(new ColumnLayout(100));
    result.setBorder(border);
    result.setToolTipText(meta.getName());

    // get parent 
    JComponent parent = getPanel(new TagPath(path, path.length()-1));

    // check color
    if (parent.getBorder() instanceof TabBorder) {
      TabBorder tb = (TabBorder)parent.getBorder();
      border.setForeground(tb.getForeground());
      border.setBackground(tb.getBackground());
    } else {
      border.setBackground(COLORS[parent.getComponentCount()&7]);
      border.setForeground(Color.WHITE);
    }
    
    // add to parent panel
//    if (meta.getType()==PropertyEvent.class) {
//      JTabbedPane tabs = new JTabbedPane();
//      tabs.add("0", result);
//      parent.add(tabs);
//    } else {
      parent.add(result);
//    }
  
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
        Iterator paths = path2beans.keySet().iterator();
        while (paths.hasNext()) {
          TagPath path = (TagPath)paths.next();
          PropertyBean bean = (PropertyBean)path2beans.get(path);
          Property prop = bean.getProperty();
          bean.commit();
          if (prop.getValue().length()>0&&prop.getParent()==null)
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

  /**
   * A special broder that shows a tab on the left
   */
  public static class TabBorder implements Border {

    private String title;
    private ImageIcon img;
    private Color 
      background = Color.GRAY,
      foreground = Color.WHITE;
    
    /**
     * Constructor
     */
    public TabBorder(String title, ImageIcon img) {
      this.title = title;
      this.img   = img;
    }

    /**
     * our insets
     */    
    public Insets getBorderInsets(Component c) {
      
      // height of font
      int fh = c.getFontMetrics(c.getFont()).getHeight();
      
      // default
      int 
        top = 1,
        bottom = 1;
        
      // 1st in parent with same border?
      Container parent = c.getParent();
      if (parent instanceof JComponent) {
        if ( ((JComponent)parent).getBorder() instanceof TabBorder) {
          
          if (parent.getComponent(0)==c)
            top = 0;
          if (parent.getComponent(parent.getComponentCount()-1)==c)
            bottom = 0;
        }
      }
      
      // done
      return new Insets(top, fh+2, bottom, 1);
    }

    /**
     * yes, we're opaque
     */
    public boolean isBorderOpaque() {
      return true;
    }
    
    /**
     * Accessor - background color
     */
    public void setBackground(Color set) {
      background = set;
    }
    
    /**
     * Accessor - background color
     */
    public Color getBackground() {
      return background;
    }
    
    /**
     * Accessor - foreground color
     */
    public void setForeground(Color set) {
      foreground = set;
    }
    
    /**
     * Accessor - foreground color
     */
    public Color getForeground() {
      return foreground;
    }
    
    /**
     * paint it
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      
      // check insets
      Insets insets = getBorderInsets(c);

      // prepare font
      g.setFont(c.getFont());
      FontMetrics fm = g.getFontMetrics();

      // fill tab      
      g.setColor(background);
      g.fillRect(1, insets.top, fm.getHeight(), height-insets.top-insets.bottom);

      // prepare parms
      int bottom = y+height;
      y += insets.top;

      // show image
      if (img!=null) {
        img.paintIcon(g, 1, y+1);
        y += img.getIconHeight();
      }

      // show text
      if (title!=null) {
        if (y + fm.getStringBounds(title, g).getWidth() < bottom) {
          g.setColor(foreground);
          Graphics2D g2d = (Graphics2D)g;
          AffineTransform at = g2d.getTransform();
          g2d.rotate(Math.PI/2);
          g2d.drawString(title, y+1, -fm.getMaxDescent());
          g2d.setTransform(at);
        }
      }
      
      // done      
    }

  } //TabBorder
  
} //BasicEditor
