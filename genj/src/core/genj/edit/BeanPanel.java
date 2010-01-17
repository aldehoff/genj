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
import genj.edit.beans.ReferencesBean;
import genj.edit.beans.RelationshipsBean;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyChoiceValue;
import genj.gedcom.PropertyComparator;
import genj.gedcom.PropertyNumericValue;
import genj.gedcom.PropertySimpleValue;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.gedcom.UnitOfWork;
import genj.util.ChangeSupport;
import genj.util.swing.Action2;
import genj.util.swing.ImageIcon;
import genj.util.swing.LinkWidget;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.PopupWidget;
import genj.view.ContextProvider;
import genj.view.ViewContext;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ContainerOrderFocusTraversalPolicy;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;

/**
 * A panel for laying out beans for an entity
 */
public class BeanPanel extends JPanel {

  private static final String
    PROXY_PROPERTY_ROOT = "beanpanel.bean.root",
    PROXY_PROPERTY_PATH = "beanpanel.bean.path";

  /** keep a cache of descriptors */
  private static Map<String, NestedBlockLayout> DESCRIPTORCACHE = new HashMap<String, NestedBlockLayout>();

  /** change support */
  protected ChangeSupport changeSupport = new ChangeSupport(this);
  
  /** beans */
  private List<PropertyBean> beans = new ArrayList<PropertyBean>(32);
  
  /** content */
  private JPanel detail = new JPanel();
  private JTabbedPane tabs = new ContextTabbedPane();

  private boolean isShowTabs = true;
  
  /**
   * Find a descriptor 
   */
  private static NestedBlockLayout getLayout(String key) {
    
    // already loaded?
    NestedBlockLayout result = DESCRIPTORCACHE.get(key);
    if (result!=null)
      return result.copy();
    if (DESCRIPTORCACHE.containsKey(key))
      return null;
    
    try {
      // read
      InputStream in = BeanPanel.class.getResourceAsStream(key);
      if (in!=null) try {
        result = new NestedBlockLayout(in);
      } finally {
        in.close();
      }
    } catch (Throwable t) {
      // 20060601 don't let IllegalArgumentException go through (cought IOExcption only previously)
      // A web-server might return an invalid 404 input stream instead of null that we end up reading
      EditView.LOG.log(Level.WARNING, "cannot read descriptor "+key+" ("+t.getMessage()+")");
    }
    
    // read only once ever
    DESCRIPTORCACHE.put(key, result);

    return result!=null ? result.copy() : null;
  }
  
  private static NestedBlockLayout getLayout(MetaProperty meta) {
    if (Entity.class.isAssignableFrom(meta.getType()))
      return getLayout("descriptors/entities/" + meta.getTag()+".xml");

    // try to read a descriptor by tag
    String key = "descriptors/properties/" + meta.getTag() +".xml";
    NestedBlockLayout result = getLayout(key);
    if (result!=null) 
      return result;
      
    // fallback to property type
    result = getLayout("descriptors/properties/" + meta.getType().getSimpleName() +".xml");
    if (result!=null)
      return result;
    
    // not found
    return null;
  }
  
  public BeanPanel() {
    
    // layout
    setLayout(new BorderLayout());
    add(detail, BorderLayout.CENTER);
    add(tabs, BorderLayout.SOUTH);
    
    // make user focus root
    setFocusTraversalPolicy(new FocusPolicy());
    setFocusCycleRoot(true);
  }
  
  public void addChangeListener(ChangeListener listener) {
    changeSupport.addChangeListener(listener);
  }
  
  public void removeChangeListener(ChangeListener listener) {
    changeSupport.removeChangeListener(listener);
  }
    
  /**
   * commit beans - transaction has to be running already
   */
  public void commit() {
    
    // loop over beans 
    for (PropertyBean bean : beans) {
      // check next
      if (bean.hasChanged()&&bean.getProperty()!=null) {
        
        // re-resolve the property we're going to commit too (bean might have been looking at a proxy)
        Property root = (Property)bean.getClientProperty(PROXY_PROPERTY_ROOT);
        TagPath path = (TagPath)bean.getClientProperty(PROXY_PROPERTY_PATH);
        Property prop = root.getProperty(path,false);
        if (prop==null)
          prop = root.setValue(path, "");
        
        // commit its changes
        bean.commit(prop);
        // next
      }
    }
    
    changeSupport.setChanged(false);
    
    // done
  }

  /**
   * Select a property's bean
   */
  public void select(Property prop) {
    
    // find bean
    JComponent bean = find(prop);
    if (bean==null) 
      return;

    // bring forward in a tabbed pane
    Component parent = bean;
    while (true) {
      if (parent.getParent() instanceof JTabbedPane) {
        ((JTabbedPane)parent.getParent()).setSelectedComponent(parent);
      }
      parent = parent.getParent();
      if (parent==null||parent==this)
        break;
    }        

    // request now
    if (!bean.requestFocusInWindow())
      Logger.getLogger("genj.edit").fine("requestFocusInWindow()==false");
    
    // done
  }
  
  private JComponent find(Property prop) {
    if (prop==null||beans.isEmpty())
      return null;
    
    // look for appropriate bean showing prop
    for (PropertyBean bean : beans) {
      if (bean.getProperty()==prop) 
        return bean;
    }
    
    // check if one of the beans' properties is contained in prop
    for (PropertyBean bean : beans) {
      if (bean.isDisplayable() && bean.getProperty()!=null && bean.getProperty().isContained(prop)) 
        return bean;
    }
    
    // check tabs specifically (there might be no properties yet)
    for (Component c : tabs.getComponents()) {
      JComponent jc = (JComponent)c;
      if (jc.getClientProperty(Property.class)==prop) 
        return jc;
    }
    
    // otherwise use first bean
    return (PropertyBean)beans.get(0);
    
    // done
  }
  
  /** switch on detail */
  public void setShowTabs(boolean set) {
    isShowTabs = set;
    tabs.setVisible(set);
  }

  /** set context */
  public void setRoot(Property root) {
    
    // clean up first
    for (PropertyBean bean : beans) {
      bean.removeChangeListener(changeSupport);
      bean.getParent().remove(bean);
      PropertyBean.recycle(bean);
    }
    beans.clear();
    detail.removeAll();
    tabs.removeAll();

    // something to layout?
    if (root!=null) {
    
      // keep track of tags
      Set<String> beanifiedTags = new HashSet<String>();
      
      // layout from descriptor
      NestedBlockLayout descriptor = getLayout(root.getMetaProperty());
      if (descriptor!=null) 
        parse(detail, root, root, descriptor, beanifiedTags);

      if (isShowTabs) {
        // create tab for relationships of root
        createReferencesTabs(root);
        
        // create a tab for properties of root w/o descriptor
        createPropertiesTab(root, beanifiedTags);
    
        // create tabs for properties of root w/descriptor
        createEventTabs(root, beanifiedTags);
  
        // create a tab for links to create new
        createLinkTab(root, beanifiedTags);
      }
    }
      
    // done
    revalidate();
    repaint();
  }

  /**
   * create a tab for (simple) properties that we don't have descriptors for
   */
  private void createPropertiesTab(Property root, Set<String> beanifiedTags) {
    
    JPanel tab = new JPanel(new GridLayout(0,2));
    tab.setOpaque(false);
    
    MetaProperty[] nested = root.getNestedMetaProperties(MetaProperty.WHERE_NOT_HIDDEN);
    Arrays.sort(nested);
    for (MetaProperty meta : nested) {
      // ignore if we have a layout for a property specific tab
      if (getLayout(meta)!=null)
        continue;
      // ignore if not editable simple/choice value
      if (meta.getType()!=PropertySimpleValue.class
        &&meta.getType()!=PropertyChoiceValue.class
        &&meta.getType()!=PropertyNumericValue.class)
        continue;
      // ignore if already beanified
      if (!beanifiedTags.add(meta.getTag()))
        continue;
      tab.add(new JLabel(meta.getName(), meta.getImage(), SwingConstants.LEFT));
      tab.add(createBean(root, new TagPath(root.getTag()+":"+meta.getTag()), meta, null));
    }
    
    tabs.addTab("", MetaProperty.IMG_CUSTOM, tab);
  }
  
  /**
   * Parse descriptor for beans into panel
   */
  private void parse(JPanel panel, Property root, Property property, NestedBlockLayout descriptor, Set<String> beanifiedTags)  {

    panel.setLayout(descriptor);
    
    // fill cells with beans
    for (NestedBlockLayout.Cell cell : descriptor.getCells()) {
      JComponent comp = createComponent(root, property, cell, beanifiedTags);
      if (comp!=null) 
        panel.add(comp, cell);
    }
    
    // done
  }
  
  /**
   * Create a component for given cell
   */
  private JComponent createComponent(Property root, Property property, NestedBlockLayout.Cell cell, Set<String> beanifiedTags) {
    
    String element = cell.getElement();
    
    // right gedcom version?
    String version = cell.getAttribute("gedcom");
    if (version!=null & !property.getGedcom().getGrammar().getVersion().equals(version))
      return null;
    
    // text?
    if ("text".equals(element)) {
      return new JLabel(cell.getAttribute("value"));
    }
    
    // prepare some info and state
    TagPath path = new TagPath(cell.getAttribute("path"));
    MetaProperty meta = property.getMetaProperty().getNestedRecursively(path, false);
    
    // a label?
    if ("label".equals(element)) {

      JLabel label;
      if (path.length()==1&&root instanceof Entity) 
        label = new JLabel(meta.getName() + ' ' + ((Entity)root).getId(), null, SwingConstants.LEFT);
      else
        label = new JLabel(meta.getName(cell.isAttribute("plural")), null, SwingConstants.LEFT);

      return label;
    }
    
    // a bean?
    if ("bean".equals(element)) {
      // create bean
      PropertyBean bean = createBean(property, path, meta, cell.getAttribute("type"));
      if (bean==null)
        return null;
      // patch it
      if ("horizontal".equals(cell.getAttribute("dir")))
        bean.setPreferHorizontal(true);
      // track it
      if (beanifiedTags!=null&&property==root&&path.length()>1)
        beanifiedTags.add(path.get(1));
      // finally wrap in popup if requested?
      return cell.getAttribute("popup")==null ? bean : (JComponent)new PopupBean(bean);
    }

    // bug in the descriptor
    throw new IllegalArgumentException("Template element "+cell.getElement()+" is unkown");
  }
  
  /**
   * create a bean
   * @param root we need the bean for
   * @param path path to property we need bean for
   * @param explicit bean type
   */
  private PropertyBean createBean(Property root, TagPath path, MetaProperty meta, String beanOverride) {

    // try to resolve existing prop (first possible path to avoid merging of branches)
    Property prop = root.getProperty(path, false);
    
    // addressed property doesn't exist yet? create a proxy that mirrors
    // the root and provides the necessary context
    if (prop==null||prop instanceof PropertyXRef) 
      prop = new PropertyProxy(root).setValue(path, "");
    
    // create bean for property
    PropertyBean bean = beanOverride==null ? PropertyBean.getBean(prop.getClass()) : PropertyBean.getBean(beanOverride);
    bean.setProperty(prop);
    bean.addChangeListener(changeSupport);
    beans.add(bean);

    // remember path
    bean.putClientProperty(PROXY_PROPERTY_ROOT, root);
    bean.putClientProperty(PROXY_PROPERTY_PATH, path);
    
    // done
    return bean;
  }
  
  private void createReferencesTabs(Property root) {
    tabs.addTab("", RelationshipsBean.IMG , new RelationshipsBean().setProperty(root));
    tabs.addTab("", ReferencesBean.IMG , new ReferencesBean().setProperty(root));
  }

  private void createLinkTab(Property root, Set<String> beanifiedTags) {
    
    // 'create' a tab with links to create sub-property that we have a descriptor for
    JPanel linksTab = new JPanel(new FlowLayout(FlowLayout.LEFT));
    linksTab.setPreferredSize(new Dimension(64,64));
    linksTab.setOpaque(false);
    tabs.addTab("", Images.imgNew, linksTab);
    MetaProperty[] nested = root.getNestedMetaProperties(MetaProperty.WHERE_NOT_HIDDEN);
    Arrays.sort(nested);
    for (int i=0;i<nested.length;i++) {
      MetaProperty meta = nested[i];
      // if there's a descriptor for it
      NestedBlockLayout descriptor = getLayout(meta);
      if (descriptor==null||descriptor.getCells().isEmpty())
        continue;
      // .. and if there's no other already with isSingleton
      if (beanifiedTags.contains(meta.getTag())&&meta.isSingleton())
        continue;
      // create a button for it
      linksTab.add(new LinkWidget(new AddTab(root, meta)));
    }
  }
  
  /**
   * Create tabs for events
   */
  private void createEventTabs(Property root, Set<String> beanifiedTags) {
    
    // don't create tabs for already visited tabs unless it's a secondary
    Set<String> skippedOnceTags = new HashSet<String>();
    Property[] props = root.getProperties();
    Arrays.sort(props, new PropertyComparator(".:DATE"));
    for (Property prop : props) {
      // check tag - skipped or covered already?
      String tag = prop.getTag();
      if (skippedOnceTags.add(tag)&&beanifiedTags.contains(tag)) 
        continue;
      beanifiedTags.add(tag);
      // create a tab for it
      createEventTab(root, prop);
      // next
    }
    // done
  }
  
  /**
   * Create a tab
   */
  private void createEventTab(Property root, Property prop) {
     
    // don't do xrefs
    if (prop instanceof PropertyXRef)
      return;
     
    // got a descriptor for it?
    MetaProperty meta = prop.getMetaProperty();
    NestedBlockLayout descriptor = getLayout(meta);
    if (descriptor==null) 
      return;
     
    // create the panel
    JPanel tab = new JPanel();
    tab.putClientProperty(Property.class, prop);
    tab.setOpaque(false);
    
    parse(tab, root, prop, descriptor, null);
    tabs.addTab(meta.getName() + prop.format("{ $y}"), prop.getImage(false), tab, meta.getInfo());

    // done
  }
   
  private class ContextTabbedPane extends JTabbedPane implements ContextProvider {
    private ContextTabbedPane() {
      super(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
    }
    public ViewContext getContext() {
      // check if tab for property
      Component selection = tabs.getSelectedComponent();
      Property prop = (Property)((JComponent)selection).getClientProperty(Property.class);
      if (prop==null)
        return null;
      // provide a context with delete
      return new ViewContext(prop).addAction(new DelTab(prop));
    }
  } //ContextTabbedPane
    
   /** An action for adding 'new tabs' */
   private class AddTab extends Action2 {
     
     private MetaProperty meta;
     private Property root;
     private Property property;
     
     /** constructor */
     private AddTab(Property root, MetaProperty meta) {
       // remember
       this.meta = meta;
       this.root = root;
       // looks
       setText(meta.getName());
       setImage(meta.getImage());
       setTip(meta.getInfo());
     }
   
     /** callback initiate create */
     public void actionPerformed(ActionEvent event) {
       
       root.getGedcom().doMuteUnitOfWork(new UnitOfWork() {
         public void perform(Gedcom gedcom) {
           
           // commit bean changes
           if (BeanPanel.this.changeSupport.hasChanged())
             commit();
           
           // add property for tab
           property = root.addProperty(meta.getTag(), "");
         }
       });
       
       // send selection
       select(property);
       
       // done
     }
     
   } //AddTab
   
   /**
    * A remove tab action
    */
   private class DelTab extends Action2 {
     private Property prop;
     private DelTab(Property prop) {
       setText(EditView.RESOURCES.getString("action.del", prop.getPropertyName()));
       setImage(Images.imgCut);
       this.prop = prop;
     }
    public void actionPerformed(ActionEvent event) {
      prop.getGedcom().doMuteUnitOfWork(new UnitOfWork() {
        public void perform(Gedcom gedcom) {
          
          // commit bean changes
          if (BeanPanel.this.changeSupport.hasChanged())
            commit();
          
          // delete property
          prop.getParent().delProperty(prop);
          
        }
      });
      

      // done
    }
  }

  /**
   * A 'bean' we use for groups
   */
  private class PopupBean extends PopupWidget implements MouseMotionListener, MouseListener {
    
    private PropertyBean wrapped;
    private JPanel content;
    
    /**
     * constructor
     */
    private PopupBean(PropertyBean wrapped) {
      
      // fix button's looks
      setFocusable(false);
      setBorder(null);
      
      // remember wrapped bean
      this.wrapped = wrapped;
      
      // setup button's look
      Property prop = wrapped.getProperty();
      setToolTipText(prop.getPropertyName());
      ImageIcon img = prop.getImage(false);
      if (prop.getValue().length()==0)
        img = img.getGrayedOut();
      setIcon(img);
      
      // prepare panel we're going to show
      content = new JPanel(new BorderLayout());
      content.setAlignmentX(0);
      content.setBorder(new TitledBorder(prop.getPropertyName()));
      content.addMouseMotionListener(this);
      content.addMouseListener(this);
      content.add(wrapped);
      content.setFocusCycleRoot(true);
      
      // prepare 'actions'
      addItem(content);
  
      // done
    }
    
    private ImageIcon getImage(Property prop) {
      while (prop.getParent()!=null && !(prop.getParent() instanceof Entity)) {
        prop = prop.getParent();
      }
      return prop.getImage(false);
    }
    
    /**
     * intercept popup
     */
    public void showPopup() {
      // let super do its thing
      super.showPopup();
      // resize if available
      Dimension d = BasicEditor.REGISTRY.get("popup."+wrapped.getProperty().getTag(), (Dimension)null);
      if (d!=null) 
        setPopupSize(d);
      // request focus
      SwingUtilities.getWindowAncestor(wrapped).setFocusableWindowState(true);
      wrapped.requestFocus();
      // update image
      setIcon(wrapped.getProperty().getImage(false));
    }
    
    public void mouseDragged(MouseEvent e) {
      // allow to resize 
      if (content.getCursor()==Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR)) {
        Dimension d = new Dimension(e.getPoint().x, e.getPoint().y);
        BasicEditor.REGISTRY.put("popup."+wrapped.getProperty().getTag(), d);
        setPopupSize(d);
      }
    }
  
    public void mouseMoved(MouseEvent e) {
      // resize?
      if (e.getX()>content.getWidth()-content.getInsets().right
        &&e.getY()>content.getHeight()-content.getInsets().bottom) {
        content.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
        return;
      }
      // close?
      if (e.getY()<content.getInsets().top) try {
        content.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return;
      } catch (Throwable t) {}
      // default
      content.setCursor(Cursor.getDefaultCursor());
    }

    public void mouseClicked(MouseEvent e) {
      if (content.getCursor()==Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)) 
        cancelPopup();
    }

    public void mouseEntered(MouseEvent e) {
      // TODO Auto-generated method stub
      
    }

    public void mouseExited(MouseEvent e) {
      // TODO Auto-generated method stub
      
    }

    public void mousePressed(MouseEvent e) {
      // TODO Auto-generated method stub
      
    }

    public void mouseReleased(MouseEvent e) {
      // TODO Auto-generated method stub
      
    }
  
  } //Label

  /**
   * A proxy for a property - it can be used as a container
   * for temporary sub-properties that are not committed to
   * the proxied context 
   */
  private class PropertyProxy extends Property {
    private Property proxied;
    /** constructor */
    private PropertyProxy(Property prop) {
      this.proxied = prop;
    }
    public Property getProxied() {
      return proxied;
    }
    public boolean isContained(Property in) {
      return proxied==in ? true : proxied.isContained(in);
    }
    public Gedcom getGedcom() { return proxied.getGedcom(); }
    public String getValue() { throw new IllegalArgumentException(); };
    public void setValue(String val) { throw new IllegalArgumentException(); };
    public String getTag() { return proxied.getTag(); }
    public TagPath getPath() { return proxied.getPath(); }
    public MetaProperty getMetaProperty() { return proxied.getMetaProperty(); }
  }
     
  /**
   * The default container FocusTravelPolicy works based on
   * x/y coordinates which doesn't work well with the column
   * layout used.
   * ContainerOrderFocusTraversalPolicy would do fine accept()-check 
   * is placed in a protected method of LayoutFocusTraversalPolicy 
   * basically rendering the former layout useless.
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
 }