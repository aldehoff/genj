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
import java.awt.geom.Point2D;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;

/**
 * A Proxy is a ui representation of a property with interactiv components that the user
 * will use to change values
 */
public abstract class PropertyBean extends JPanel {
  
  /** the resources */
  protected final static Resources resources = Resources.get(PropertyBean.class); 
  
  /** the gedcom object */
  protected Gedcom gedcom;
  
  /** the proxied property */
  protected Property property;
  
  /** the view manager */
  protected ViewManager viewManager;
  
  /** current registry */
  protected Registry registry;
  
  /** buttons */
  protected AbstractButton ok, cancel;

  /** the default focus */
  protected JComponent defaultFocus = null;
  
  /** change support */
  protected ChangeSupport changeSupport = new ChangeSupport(this);
  
  /** an optional path */
  protected TagPath path;
  
  /**
   * Accessor
   */
  public static PropertyBean get(Property prop) {
    try {
      return (PropertyBean) Class.forName( "genj.edit.beans." + prop.getProxy() + "Bean").newInstance();
    } catch (Throwable t) {
      return new SimpleValueBean();
    }
  }
  
  /**
   * Constructor
   */  
  protected PropertyBean() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
  }
  
  /**
   * Setup an editor in given panel
   */
  public void init(Gedcom setGedcom, Property setProp, TagPath setPath, ViewManager setMgr, Registry setReg) {

    // remember property
    property = setProp;
    viewManager = setMgr;
    registry = setReg;
    gedcom = setGedcom;
    path = setPath;
    
    // done
  }
  
  /**
   * Property wrapped in bean
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
   * Commit any changes made by the user
   */
  public abstract void commit();
  
  /**
   * Editable? default is yes
   */
  public boolean isEditable() {
    return true;
  }
  
  /** 
   * Weight
   */
  public Point2D getWeight() {
    return null;
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
   * An optional path 
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
    protected Preview(Entity ent) {
      // remember
      entity = ent;
      setBorder(new EmptyBorder(4,4,4,4));
      // done
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
      if (renderer==null) 
        renderer = new EntityRenderer(viewManager.getBlueprintManager().getBlueprint(entity.getTag(), ""));
      renderer.render(g, entity, box);
      // done
    }
  } //Preview

} //Proxy
