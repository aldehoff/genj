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

import genj.edit.beans.BeanFactory;
import genj.edit.beans.PropertyBean;
import genj.gedcom.Change;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertySimpleValue;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.gedcom.Transaction;
import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.Registry;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.LinkWidget;
import genj.util.swing.MenuHelper;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.PopupWidget;
import genj.view.Context;
import genj.window.CloseWindow;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ContainerOrderFocusTraversalPolicy;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The basic version of an editor for a entity. Tries to hide Gedcom complexity from the user while being flexible in what it offers to edit information pertaining to an entity.
 */
/* package */class BasicEditor extends Editor implements GedcomListener {

  /** entity tag to layout */
  private static Map FILE2LAYOUT = new HashMap();
  
  /** our gedcom */
  private Gedcom gedcom = null;

  /** current entity */
  private Entity entity = null;

  /** registry */
  private Registry registry;

  /** edit */
  private EditView view;

  /** panels */
  private JSplitPane splitPanel;
  private JTabbedPane tabPanel;
  
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

    // split for content of components
    splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    
    // create panel for actions
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    ButtonHelper bh = new ButtonHelper().setInsets(0).setContainer(buttons).setFocusable(false);
    bh.create(ok);
    bh.create(cancel);
    
    // layout
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, splitPanel);
    add(BorderLayout.SOUTH, buttons);

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
    // setup split panel position
    splitPanel.setDividerLocation(registry.get("basic.divider", 256));
    // done
  }

  /**
   * Intercepted remove notification
   */
  public void removeNotify() {
    // clean up state
    setEntity(null);
    // keep split panel position
    registry.put("basic.divider", splitPanel.getDividerLocation());
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
    for (Iterator it=beans.iterator(); it.hasNext(); ) {
      PropertyBean bean = (PropertyBean)it.next();
      if (bean.getProperty()==prop) {
        bean.requestFocusInWindow();
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
    int dividerLocation = splitPanel.getDividerLocation();

    // remove all current beans and tabs
    splitPanel.setLeftComponent(null);
    splitPanel.setRightComponent(null);

    // recycle beans
    BeanFactory factory = view.getBeanFactory();
    for (Iterator it=beans.iterator(); it.hasNext(); ) {
      PropertyBean bean = (PropertyBean)it.next();
      bean.removeChangeListener(changeCallback);
      factory.recycle(bean);
    }
    beans.clear();

    // setup components
    if (entity!=null) try {

      // 'create' standard panel (and collect topLevelTags)
      Set topLevelTags = new HashSet();
      JPanel mainPanel = createPanel(entity, getSharedDescriptor(entity.getMetaProperty()).copy(), topLevelTags);
      
      // 'create' tab panel
      tabPanel = new JTabbedPane();
      //tabPanel.addMouseListener(new RemoveTab());
      
      // .. and add all tabs
      Set skippedTags = new HashSet();
      props: for (int i=0, j=entity.getNoOfProperties(); i<j; i++) {
        Property prop = entity.getProperty(i);
        // check tag - skipped or covered already?
        String tag = prop.getTag();
        if (skippedTags.add(tag)&&topLevelTags.contains(tag)) 
          continue;
        topLevelTags.add(tag);
        // create a tab for it
        createTab(prop);
        // next
      }
      
      // 'create' a tab for creating new properties
      JPanel newTab = new JPanel(new FlowLayout(FlowLayout.LEFT));
      newTab.setPreferredSize(new Dimension(64,64));
      tabPanel.addTab("", Images.imgNew, newTab);
      
      // add buttons for creating sub-properties 
      MetaProperty[] nested = entity.getNestedMetaProperties(MetaProperty.FILTER_NOT_HIDDEN);
      Arrays.sort(nested);
      for (int i=0;i<nested.length;i++) {
        MetaProperty meta = nested[i];
        // if there's a descriptor for it
        NestedBlockLayout descriptor = getSharedDescriptor(meta);
        if (descriptor==null)
          continue;
        // .. and if there's no other already with isSingleton
        if (topLevelTags.contains(meta.getTag())&&meta.isSingleton())
          continue;
        // create a button for it
        newTab.add(new LinkWidget(new NewTab(meta)));
      }

      // finally finish setting up split panel
     splitPanel.setLeftComponent(new JScrollPane(mainPanel));
     splitPanel.setRightComponent(new JScrollPane(tabPanel));
     splitPanel.setDividerLocation(dividerLocation);
      
      // done
      
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
   * Find a descriptor for given property
   * @return private copy descriptor or null if n/a
   */
  private NestedBlockLayout getSharedDescriptor(MetaProperty meta) {
    
    // compute appropiate file name for entity or property
    String file  = "descriptors/" + (meta.isEntity() ? "entities" : "properties") + "/" + meta.getTag()+".xml";
    
    // got a cached one already?
    NestedBlockLayout descriptor  = (NestedBlockLayout)FILE2LAYOUT.get(file);
    if (descriptor==null) {
      
      // hmm, already determined we don't have one?
      if (FILE2LAYOUT.containsKey(file))
        return null;

      // try to read a descriptor - TAG.xml or Type.xml
      InputStream in = getClass().getResourceAsStream(file);
      if (in==null) 
        in = getClass().getResourceAsStream("descriptors/properties/"+meta.getType().getName().substring("genj.gedcom.".length())+".xml");
      if (in!=null) try {
        descriptor = new NestedBlockLayout(in);
      } catch (IOException e) {
        Debug.log(Debug.WARNING, this, "IO exception while reading descriptor "+file);
      }

      // cache it
      FILE2LAYOUT.put(file, descriptor);
    }

    // return private copy
    return descriptor;
  }
  
  /**
   * Create a tab
   */
  private void createTab(Property prop) {
    
    // got a descriptor for it?
    MetaProperty meta = prop.getMetaProperty();
    NestedBlockLayout descriptor = getSharedDescriptor(meta);
    if (descriptor==null) 
      return;
    
    // create the panel
    JPanel panel = createPanel(prop, descriptor.copy(), null);
      
    // keep it around
    tabPanel.insertTab(meta.getName(), meta.getImage(), new JScrollPane(panel), meta.getInfo(), 0);

    // done
  }
  
  /**
   * Create the panel content 
   */
  private JPanel createPanel(Property root, NestedBlockLayout descriptor, Set topLevelTags)  {

    JPanel result = new JPanel(descriptor);
    
    // fill cells with beans
    Iterator cells = descriptor.getCells().iterator();
    while (cells.hasNext()) {
      NestedBlockLayout.Cell cell = (NestedBlockLayout.Cell)cells.next();
      JComponent comp = createComponent(root, cell, topLevelTags);
      if (comp!=null) 
        result.add(comp, cell);
    }
    
    // done
    return result;
  }

  /**
   * create a component for given cell
   */
  private JComponent createComponent(Property root, NestedBlockLayout.Cell cell, Set topLevelTags) {
    
    TagPath path = new TagPath(cell.getAttribute("path"));
    
    if (topLevelTags!=null&&path.length()>1) topLevelTags.add(path.get(1));
    
    MetaProperty meta = root.getMetaProperty().getNestedRecursively(path, false);
    
    // a label?
    if ("label".equals(cell.getElement())) {

      JLabel label;
      if (path.length()==1&&path.getLast().equals(entity.getTag()))
        label = new JLabel(meta.getName() + ' ' + entity.getId(), entity.getImage(false), SwingConstants.LEFT);
      else
        label = new JLabel(meta.getName(cell.isAttribute("plural")), meta.getImage(), SwingConstants.LEFT);

      return label;
    }
    
    // a bean?
    if ("bean".equals(cell.getElement())) {
      // conditional?
      if (cell.getAttribute("ifexists")!=null) {
        if (root.getProperty(path)==null)
          return null;
      }
      // create bean
      PropertyBean bean = createBean(root, path, meta, cell.getAttribute("type"));
      if (bean==null)
        return null;
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

    // try to resolve existing prop - this has to be a property along
    // the first possible path to avoid that in this case:
    //  INDI
    //   BIRT
    //    DATE sometime
    //   BIRT
    //    PLAC somewhere
    // the result of INDI:BIRT:DATE/INDI:BIRT:PLAC is
    //   somtime/somewhere
    Property prop = root.getProperty(path);
    
    // .. for an existing reference we try to use a suitable
    // target property that we can edit inline
    if (prop instanceof PropertyXRef) {
      prop = ((PropertyXRef)prop).getTargetValueProperty();
      if (prop==null) 
        return null;
    }
    
    // addressed property doesn't exist yet? create a proxy that mirrors
    // the root and add create a temporary holder (enjoys the necessary
    // context - namely gedcom)
    if (prop==null) 
      prop = new Proxy(root, null).setValue(path, "");

    // create bean for property
    BeanFactory factory = view.getBeanFactory();
    PropertyBean bean = beanOverride!=null ? factory.get(beanOverride) : factory.get(prop);
    bean.setContext(prop, registry);
    bean.addChangeListener(changeCallback);
    beans.add(bean);
    
    // done
    return bean;
  }
  
  /**
   * A remove tab action
   */
  private class RemoveTab extends ActionDelegate implements MouseListener  {
    int tabIndex = -1;
    private RemoveTab() {
      setText("Remove");
      setImage(Images.imgCut);
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mouseClicked(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
      mouseReleased(e);
    }
    public void mouseReleased(MouseEvent e) {
      // popup?
      if (!e.isPopupTrigger())
        return;
      // calculate tab 
      tabIndex = tabPanel.indexAtLocation(e.getX(), e.getY());
      if (tabIndex<0)
        return;
      // show popup
      MenuHelper mh = new MenuHelper();
      JPopupMenu menu = mh.createPopup(tabPanel);
      mh.createItem(this);
      menu.show(tabPanel, e.getX(), e.getY());
      // done
    }
    protected void execute() {
      if (tabIndex<0)
        return;
      tabPanel.removeTabAt(tabIndex);
    }
  }

  /**
   * A proxy proparty - hooks up temporary properties to their context and propagates
   * changes to an original
   */
  private class Proxy extends PropertySimpleValue {
    /** the original root */
    private Property root;
    /** a late binding parent */
    private Property lateBindingParent;
    /** constructor */
    private Proxy(Property root , Property lateBindingParent) {
      this.root = root;
      this.lateBindingParent = lateBindingParent;
    }
    /** hook into change notifications */
    protected void propagateChange(Change change) {
      // a late binding parent to consider?
      if (lateBindingParent!=null) {
        root = lateBindingParent.addProperty(root.getTag(), "");
        lateBindingParent = null;
      }
      // consider value changes only at this point
      if (change instanceof Change.PropertyValue) {
        // something to consider?
        Property changed = ((Change.PropertyValue)change).getChanged();
        String value = changed.getValue();
        if (value.length()==0)
          return;
        root.setValue(getPathToNested(changed), value);
        // done
      }
    }
    /** offer context - gedcom and  transaction */
    public Gedcom getGedcom() { return gedcom; }
    protected Transaction getTransaction() { return gedcom.isTransaction() ? gedcom.getTransaction() : null; }
    /** proxied stuff */
    public String getTag() { return root.getTag(); }
    public TagPath getPath() { return root.getPath(); }
    public MetaProperty getMetaProperty() { return root.getMetaProperty(); }
  }
    
  /**
   * An action for adding 'new tabs'
   */
  private class NewTab extends ActionDelegate {
    
    private MetaProperty meta;
    
    /** constructor */
    private NewTab(MetaProperty meta) {
      // remember
      this.meta = meta;
      // looks
      setText(meta.getName());
      setImage(meta.getImage());
    }

    /** callback initiate create */
    protected void execute() {
      
      // create a temporary root that we'll add later to entity on change
      Property root = new Proxy(meta.create(""), entity);
      
      // create a tab for it
      createTab(root);
      
      // and select it
      tabPanel.setSelectedIndex(0);
      
      // done
    }
    
  } //AddTab
  
  /**
   * A 'bean' we use for groups
   */
  private class PopupBean extends PopupWidget {
    
    private PropertyBean wrapped;
    
    /**
     * constructor
     */
    private PopupBean(PropertyBean wrapped) {
      
      // remember wrapped
      this.wrapped = wrapped;
      wrapped.setAlignmentX(0);
      
      // prepare image
      Property prop = wrapped.getProperty();
      ImageIcon img = prop.getImage(false);
      if (prop.getValue().length()==0)
        img = img.getDisabled(50);
      setIcon(img);
      setToolTipText(wrapped.getProperty().getPropertyName());
      
      // fix looks
      setFocusable(false);
      setBorder(null);
      
      // prepare 'actions'
      List actions = new ArrayList();
      actions.add(new JLabel(wrapped.getProperty().getPropertyName()));
      actions.add(wrapped);
      setActions(actions);

      // done
    }
    
    /**
     * intercept popup
     */
    public void showPopup() {
      // let super do its thing
      super.showPopup();
      // request focus
      SwingUtilities.getWindowAncestor(wrapped).setFocusableWindowState(true);
      wrapped.requestFocus();
      // update image
      setIcon(wrapped.getProperty().getImage(false));
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
        for (Iterator it = beans.iterator(); it.hasNext();) {
          // let bean commit its changes 
          ( (PropertyBean)it.next()).commit();
        }
      } catch (Throwable t) {
        Debug.log(Debug.ERROR, this, t);
      }

      // end transaction - this will refresh our view as well
      gedcom.endTransaction();

      // done
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
  
}
