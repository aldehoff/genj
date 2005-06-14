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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Transaction;
import genj.renderer.Blueprint;
import genj.renderer.BlueprintManager;
import genj.renderer.EntityRenderer;
import genj.util.Registry;
import genj.util.Resources;
import genj.view.Context;
import genj.view.ContextListener;
import genj.view.ContextSelectionEvent;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 * A rendering component showing the currently selected entity
 * via html
 */
public class EntityView extends JPanel implements ContextListener, ToolBarSupport, GedcomListener {

  /** language resources we use */  
  /*package*/ final static Resources resources = Resources.get(EntityView.class);

  /** a dummy blueprint */
  private final static Blueprint BLUEPRINT_SELECT = new Blueprint(resources.getString("html.select"));
  
  /** a registry we keep */
  private Registry registry;
  
  /** the renderer we're using */      
  private EntityRenderer renderer = null;
  
  /** the Gedcom we're for */
  /*package*/ Gedcom gedcom = null;
  
  /** the current entity */
  private Entity entity = null;
  
  /** the blueprints we're using */
  private Map type2blueprint = new HashMap();
  
  /** whether we do antialiasing */
  private boolean isAntialiasing = false;
  
  /** the view manager */
  /*package*/ ViewManager viewManager;
  
  /**
   * Constructor
   */
  public EntityView(String title, Gedcom ged, Registry reg, ViewManager manager) {
    // save some stuff
    viewManager = manager;
    registry = reg;
    gedcom = ged;

    // listen to gedcom
    gedcom.addGedcomListener(this);

    // grab data from registry
    BlueprintManager bpm = viewManager.getBlueprintManager();
    for (int t=0;t<Gedcom.ENTITIES.length;t++) {
      String tag = Gedcom.ENTITIES[t];
      type2blueprint.put(tag, bpm.getBlueprint(tag, registry.get("blueprint."+tag, "")));
    }
    isAntialiasing  = registry.get("antial"  , false);
    
    // set first entity
    Context context = manager.getLastSelectedContext(gedcom); 
    if (context!=null) 
      setEntity(context.getEntity());
    
    // enable context popup
    addMouseListener(new MouseAdapter() {
      
      /** callback - mouse press */
      public void mousePressed(MouseEvent e) {
        mouseReleased(e);
      }
  
      /** callback - mouse release */
      public void mouseReleased(MouseEvent e) {
        // no popup trigger no action
        if (!e.isPopupTrigger()) 
          return;
        Point pos = e.getPoint();
        // context
        Context context = entity==null ? new Context(gedcom) : new Context(entity);
        viewManager.showContextMenu(context, null, EntityView.this, pos);
      }
    });
    
    // done    
  }

  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(256,160);
  }

  /**
   * @see javax.swing.JComponent#removeNotify()
   */
  public void removeNotify() {
    
    super.removeNotify();

    // stop listening to Gedcom    
    gedcom.removeGedcomListener(this);
    
    // store settings in registry
    for (int t=0;t<Gedcom.ENTITIES.length;t++) {
      String tag = Gedcom.ENTITIES[t];
      registry.put("blueprint."+tag, getBlueprint(tag).getName()); 
    }
    registry.put("antial"  , isAntialiasing );
    
    // done
  }


  /**
   * @see javax.swing.JComponent#paintComponent(Graphics)
   */
  protected void paintComponent(Graphics g) {
    Rectangle bounds = getBounds();
    g.setColor(Color.white);
    g.fillRect(0,0,bounds.width,bounds.height);
    g.setColor(Color.black);

      ((Graphics2D)g).setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        isAntialiasing ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF
      );

    renderer.render(g, entity, new Rectangle(0,0,bounds.width,bounds.height));
  }

  /**
   * @see genj.view.ToolBarSupport#populate(JToolBar)
   */
  public void populate(JToolBar bar) {
  }
  
  /** 
   * Get blueprint used for given type
   */
  /*package*/ Blueprint getBlueprint(String tag) {
    Blueprint result = (Blueprint)type2blueprint.get(tag);
    if (result==null) {
      result = viewManager.getBlueprintManager().getBlueprint(tag, "");
      type2blueprint.put(tag, result);
    }
    return result;
  }
  
  /**
   * Set the blueprints used (map tag to blueprint)
   */
  /*package*/ void setBlueprints(Map setType2Blueprints) {
    type2blueprint = setType2Blueprints;
    // show
    setEntity(entity);
  }
  
  /**
   * Gets the blueprints used (map tag to blueprint)
   */
  /*package*/ Map getBlueprints() {
    return type2blueprint;
  }
  
  /**
   * view callback
   */
  public void handleContextSelectionEvent(ContextSelectionEvent event) {
    Entity e = event.getContext().getEntity();
    if (e!=null)
      setEntity(e);
  }
  
  /**
   * Sets the entity to show
   */
  public void setEntity(Entity e) {
    // resolve blueprint & renderer
    Blueprint blueprint;
    if (e==null) blueprint = BLUEPRINT_SELECT;
    else blueprint = getBlueprint(e.getTag()); 
    renderer=new EntityRenderer(blueprint);
    // remember    
    entity = e;
    // repaint
    repaint();
    // done
  }
  
  /**
   * Sets isAntialiasing   */
  public void setAntialiasing(boolean set) {
    isAntialiasing = set;
    repaint();
  }
  
  /**
   * Gets isAntialiasing
   */
  public boolean isAntialiasing() {
    return isAntialiasing;
  }

  /**  
   * @see genj.gedcom.GedcomListener#handleChange(Transaction)
   */
  public void handleChange(Transaction tx) {
    if (tx.get(Transaction.ENTITIES_DELETED).contains(entity)) {
      setEntity(null);
    }
    repaint();
  }

} //EntityView
