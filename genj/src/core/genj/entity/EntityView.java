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
import genj.renderer.Blueprint;
import genj.renderer.BlueprintManager;
import genj.renderer.EntityRenderer;
import genj.util.Registry;
import genj.util.Resources;
import genj.view.CurrentSupport;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;
import gj.ui.UnitGraphics;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;

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
  /*package*/ Gedcom gedcom = null;
  
  /** the current entity */
  private Entity entity = null;
  
  /**
   * Constructor
   */
  public EntityView(Gedcom ged, Registry reg, Frame frame) {
    // save some stuff
    registry = reg;
    gedcom = ged;
    // listen to gedcom
    gedcom.addListener(new GedcomConnector());
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
    new UnitGraphics(g,1,1).setAntialiasing(true);
    renderer.render(g, entity, new Rectangle(0,0,bounds.width,bounds.height));
  }

  /**
   * @see genj.view.ToolBarSupport#populate(JToolBar)
   */
  public void populate(JToolBar bar) {
  }
  
  /**
   * Sets the entity to show
   */
  public void setEntity(Entity e) {
    if (e==null) {
      renderer=new EntityRenderer(
        getGraphics(),
        new Blueprint("foo", resources.getString("html.select"))
      );
    } else {
      renderer = new EntityRenderer(
        getGraphics(), 
        BlueprintManager.getInstance().getBlueprint(e.getType(), "min")
      );
    }
    entity = e;
    repaint();
  }
  
  /**
   * @see genj.view.CurrentSupport#setCurrentEntity(Entity)
   */
  public void setCurrentEntity(Entity e) {
    if (entity!=e) setEntity(e);
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
      if (change.isChanged(change.EDEL)&&change.getEntities(change.EDEL).contains(entity)) {
        setEntity(null);
      }
      repaint();
    }
  } //GedcomConnector

} //EntityView
