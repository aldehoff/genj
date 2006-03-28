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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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
  /*package*/ void initialize(ViewManager setViewManager, Registry setRegistry) {
    // our state
    viewManager = setViewManager;
    registry = setRegistry;
  }
  
  /**
   * test for setter
   */
  /*package*/ boolean accepts(Property prop) {
    return getSetter(prop)!=null;
  }
  
  /**
   * set property to look at
   */
  public void setProperty(Property prop) {
    Method m = getSetter(prop);
    if (m==null)
      throw new IllegalArgumentException(getClass().getName()+".setProperty("+prop.getClass().getName()+") n/a");
      
    try {
      m.invoke(this, new Object[]{ prop });
    } catch (Throwable t) {
      throw new RuntimeException("unexpected throwable in "+getClass().getName()+".setProperty("+prop.getClass().getName());
    }
  }
  
  private Method getSetter(Property prop) {
    
    try {
      Method[] ms = getClass().getDeclaredMethods();
      for (int i=0;i<ms.length;i++) {
        Method m = ms[i];
        if ("setProperty".equals(m.getName())&&Modifier.isPublic(m.getModifiers())) {
          Class[] argTypes = m.getParameterTypes();
          if (argTypes.length==1&&argTypes[0].isAssignableFrom(prop.getClass()))
            return m;
        }
      }
    } catch (Throwable t) {
    }
    
    return null;
  }
  
  /**
   * ContextProvider callback 
   */
  public Context getContext() {
    // ok, this is tricky since some beans might not
    // want to expose a property (is null) and the one
    // we're looking at might actually not be part of 
    // an entity yet - no context in those cases
    // (otherwise other code that relies on properties being
    // part of an entity might break)
    return property==null||property.getEntity()==null ? null : new Context(property);
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
  public void commit() {
    commit(property);
  }
  
  /**
   * Commit any changes made by the user switching target property
   */
  public void commit(Property property) {
    // remember property
    this.property = property;
    // nothing more
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
