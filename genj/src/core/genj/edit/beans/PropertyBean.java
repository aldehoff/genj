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
import genj.view.ContextListener;
import genj.view.ContextProvider;
import genj.view.ViewManager;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JPanel;
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
  protected Property property;
  
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
  /*package*/ final void initialize(BeanFactory factory, ViewManager viewManager) {
    // our state
    this.viewManager = viewManager;
    this.factory = factory;
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
   * @return default component to receive focus
   */
  public final void setContext(Property prop, Registry reg) {
    
    // remember property
    this.property = prop;
    this.registry = reg;
    
    // propagate to implementation
    setContextImpl(prop);
    
  }

  /**
   * Implementation's set context
   */
  protected void setContextImpl(Property prop) {
    
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
   * Listener
   */
  public void addContextListener(ContextListener l) {
  }
  
  /**
   * Listener
   */
  public void removeContextListener(ContextListener l) {
  }
  
  /**
   * Listener
   */
  
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
  public void commit() {
    // noop
  }
  
  /**
   * Editable? default is yes
   */
  public boolean isEditable() {
    return true;
  }
  
  /** 
   * overridden requestFocusInWindow()
   */
  public boolean requestFocusInWindow() {
    if (defaultFocus!=null)
      return defaultFocus.requestFocusInWindow();
    return super.requestFocusInWindow();
  }

  /** 
   * overridden requestFocus()
   */
  public void requestFocus() {
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
        renderer = new EntityRenderer(viewManager.getBlueprintManager().getBlueprint(entity.getTag(), ""));
      repaint();
    }
  } //Preview

} //Proxy
