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
package genj.entity;

import genj.gedcom.Change;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.renderer.EntityRenderer;
import genj.util.Registry;
import genj.util.Resources;
import genj.view.CurrentSupport;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Enumeration;

import javax.swing.JComponent;
import javax.swing.JToolBar;

/**
 * A rendering component showing the currently selected entity
 * via html
 */
public class EntityView extends JComponent implements ToolBarSupport, CurrentSupport {

  /** language resources we use */  
  /*package*/ final static Resources resources = new Resources("genj.entity");
  
  /** a registry we keep */
  private Registry registry;
  
  /** the renderer we're using */      
  private EntityRenderer renderer = null;
  
  /** the Gedcom we're for */
  private Gedcom gedcom = null;
  
  /**
   * Constructor
   */
  public EntityView(Gedcom ged, Registry reg, Frame frame) {
    // save some stuff
    registry = reg;
    gedcom = ged;
    // listen to gedcom
    gedcom.addListener(new GedcomConnector());
    // loop for htmls from defaults
    Enumeration keys = resources.getKeys();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement().toString();
      if (key.startsWith("html.")) {
        // .. if it's not declared in registry -> grab it
        if (registry.get(key,(String)null)==null) 
          registry.put(key, resources.getString(key));
      }
    }
    // done    
  }
  
  /**
   * @see javax.swing.JComponent#addNotify()
   */
  public void addNotify() {
    super.addNotify();
    // set first entity
    setEntity(ViewManager.getInstance().getCurrentEntity(gedcom));
  }

  /**
   * @see javax.swing.JComponent#paintComponent(Graphics)
   */
  protected void paintComponent(Graphics g) {
    Rectangle bounds = getBounds();
    g.setColor(Color.white);
    g.fillRect(0,0,bounds.width,bounds.height);
    g.setColor(Color.black);
    renderer.render(g, new Rectangle(0,0,bounds.width,bounds.height));
  }

  /**
   * @see genj.view.ToolBarSupport#populate(JToolBar)
   */
  public void populate(JToolBar bar) {
  }
  
  /**
   * Accessor - HTML for given entity type
   */
  public String getHtml(int type) {
    return registry.get("html."+Gedcom.getTagFor(type),"");
  }
  
  /**
   * Accessor - HTML for given entity type
   */
  public void setHtml(int type, String set) {
    registry.put("html."+Gedcom.getTagFor(type), set);
    Entity e = renderer.getEntity(); 
    renderer = null;
    setEntity(e);
  }
    
  /**
   * Sets the entity to show
   */
  public void setEntity(Entity e) {
    if (e==null) {
      renderer=new EntityRenderer(getGraphics(), resources.getString("html.select"));
    } else {
      if (renderer==null||renderer.getEntity()==null||renderer.getEntity().getType()!=e.getType()) {
        renderer = new EntityRenderer(getGraphics(), getHtml(e.getType()));
      }
      renderer.setEntity(e);
    }
    repaint();
  }
  
  /**
   * @see genj.view.CurrentSupport#setCurrentEntity(Entity)
   */
  public void setCurrentEntity(Entity entity) {
    // already?
    if (renderer.getEntity()==entity) return;
    // set it
    setEntity(entity);
  }

  /**
   * @see genj.view.CurrentSupport#setCurrentProperty(Property)
   */
  public void setCurrentProperty(Property property) {
    // ignored
  }

  /** 
   * Our connection to the Gedcom
   */
  private class GedcomConnector implements GedcomListener {
    /**
     * @see genj.gedcom.GedcomListener#handleChange(Change)
     */
    public void handleChange(Change change) {
      if (change.isChanged(change.EDEL)&&change.getEntities(change.EDEL).contains(renderer.getEntity())) {
        setEntity(null);
      }
      repaint();
    }
  } //GedcomConnector

} //EntityView
