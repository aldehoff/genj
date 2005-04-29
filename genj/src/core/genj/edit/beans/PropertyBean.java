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
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.renderer.EntityRenderer;
import genj.util.ChangeSupport;
import genj.util.Registry;
import genj.util.Resources;
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
public abstract class PropertyBean extends JPanel {
  
  /** the resources */
  protected final static Resources resources = Resources.get(PropertyBean.class); 
  
  /** factory for us */
  private BeanFactory factory;
  
  /** the current gedcom object */
  protected Gedcom gedcom;
  
  /** the root property */
  protected Property root;
  
  /** a path to the edited property */
  private TagPath path;
  
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
  protected abstract void initializeImpl();

  /**
   * Set context to edit
   * @return default component to receive focus
   */
  public final void setContext(Gedcom gedcom, Property root, TagPath path, Property prop, Registry reg) {
    
    // remember property
    this.gedcom = gedcom;
    this.root = root;
    this.path = path;
    this.property = prop;
    this.registry = reg;
    
    // propagate to implementation
    setContextImpl(gedcom, prop);
    
  }

  /**
   * Implementation's set context
   */
  protected abstract void setContextImpl(Gedcom ged, Property prop);
  
  /**
   * add hook
   */
  public void addNotify() {
    // allow super add
    super.addNotify();
  }
  
  /**
   * remove hook
   */
  public void removeNotify() {
    // stop serving
    changeSupport.removeAllChangeListeners();
    // continue ui remove
    super.removeNotify();
    // recycle
    factory.recycle(this);
    // done
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
   * Commit changes made by the user
   */
  public void commit() {
    
    // let impl do its things
    commitImpl();
    
    // check if property was temporary working copy without parent
    if (property.getValue().length() > 0 && property.getParent() == null)
      root.setValue(path, property.getValue());
    
    // done
  }
  
  /**
   * Commit any changes made by the user
   */
  protected void commitImpl() {
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
   * Current path 
   */
  public TagPath getPath() {
    return path;
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
