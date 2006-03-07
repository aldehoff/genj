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
package genj.edit.beans;

import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.renderer.EntityRenderer;
import genj.util.ChangeSupport;
import genj.util.Registry;
import genj.util.Resources;
import genj.view.Context;
import genj.view.ContextProvider;
import genj.view.ViewManager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;

/**
 * Beans allow the user to edit gedcom properties (a.k.a lines) - the lifecycle of a bean
 * looks like this:
 * <pre>
 * </pre>
 */
public abstract class PropertyBean extends JPanel implements ContextProvider {
  
  /** the resources */
  protected final static Resources resources = Resources.get(PropertyBean.class); 
  
  /** factory for us */
  private BeanFactory factory;
  
  /** the property to edit */
  private Property property;
  
  /** the current view manager */
  protected ViewManager viewManager;
  
  /** current registry */
  protected Registry registry;
  
  /** the default focus */
  protected JComponent defaultFocus = null;
  
  /** change support */
  protected ChangeSupport changeSupport = new ChangeSupport(this);
  
  /**
   * Initialize (happens once)
   */
  /*package*/ final void initialize(BeanFactory factory, ViewManager viewManager, Registry registry) {
    // our state
    this.viewManager = viewManager;
    this.factory = factory;
    this.registry = registry;
    // propagate init to sub-classes
    initializeImpl();
  }
  
  /**
   * Custom bean initialization code after member attributes have been initialized
   */
  protected void initializeImpl() {
    
  }

  /**
   * Set context to edit
   */
  public final void setProperty(Property property) {
    
    // remember property
    this.property = property;
    
    // tell to imple
    setPropertyImpl(property);
    
  }
  
  /**
   * Subtypes implementation for rendering a property
   */
  protected void setPropertyImpl(Property prop) {
    
  }

  /**
   * ContextProvider callback 
   */
  public Context getContext() {
    // ok, this is tricky since the property we're looking at might
    // actually not be part of an entity yet - we check for
    // that - no context in that case
    // (otherwise other code that relies on properties being
    // part of an entity might break)
    return property.getEntity()==null ? null : new Context(property);
  }
  
  /**
   * Current Property
   */
  public Property getProperty() {
    return property;
  }
  
  /**
   * Whether the bean has changed since first listener was attached
   */
  public boolean hasChanged() {
    return changeSupport.hasChanged();
  }
  
  /**
   * Listener 
   */
  public void addChangeListener(ChangeListener l) {
    changeSupport.addChangeListener(l);
  }
  
  /**
   * Listener 
   */
  public void removeChangeListener(ChangeListener l) {
    changeSupport.removeChangeListener(l);
  }

  /**
   * Commit any changes made by the user
   */
  public final void commit() {
    commitImpl(property);
  }
  
  /**
   * Commit any changes made by the user
   */
  public final void commit(Property overideProperty) {
    commitImpl(overideProperty);
  }
  
  /**
   * Commit any changes made by the user
   */
  protected void commitImpl(Property property) {
    // noop
  }
  
  /**
   * Editable? default is yes
   */
  public boolean isEditable() {
    return true;
  }
  
  /**
   * helper that makes this bean visible if possible
   */
  private void makeVisible() {
    // let's test if we're in a tabbed pane first
    Component c = getParent();
    while (c!=null) {
      // is it a tabbed pane?
      if (c.getParent() instanceof JTabbedPane) {
        ((JTabbedPane)c.getParent()).setSelectedComponent(c);
        return;
      }
      // continue lookin
      c = c.getParent();
    }
    // not contained in tabbed pane
  }
  
  /** 
   * overridden requestFocusInWindow()
   */
  public boolean requestFocusInWindow() {
    // make sure we're visible
    makeVisible();
    
    // delegate to default focus
    if (defaultFocus!=null)
      return defaultFocus.requestFocusInWindow();
    return super.requestFocusInWindow();
  }

  /** 
   * overridden requestFocus()
   */
  public void requestFocus() {
    //  make sure we're visible
    makeVisible();
    // delegate to default focus
    if (defaultFocus!=null)
      defaultFocus.requestFocus();
    else 
      super.requestFocus();
  }
  
  /**
   * A preview component using EntityRenderer for an entity
   */
  public class Preview extends JComponent {
    /** entity */
    private Entity entity;
    /** the blueprint renderer we're using */
    private EntityRenderer renderer;
    /**
     * Constructor
     */
    protected Preview() {
      setBorder(new EmptyBorder(4,4,4,4));
    }
    /**
     * @see genj.edit.ProxyXRef.Content#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g) {
      Insets insets = getInsets();
      Rectangle box = new Rectangle(insets.left,insets.top,getWidth()-insets.left-insets.right,getHeight()-insets.top-insets.bottom);     
      // clear background
      g.setColor(Color.WHITE); 
      g.fillRect(box.x, box.y, box.width, box.height);
      // render entity
      if (renderer!=null) 
        renderer.render(g, entity, box);
      // done
    }
    protected void setEntity(Entity ent) {
      entity = ent;
      if (entity!=null)
        renderer = new EntityRenderer(viewManager.getBlueprintManager().getBlueprint(entity.getGedcom().getOrigin(), entity.getTag(), ""));
      repaint();
    }
  } //Preview

} //Proxy
