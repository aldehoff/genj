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
package genj.resume;

import genj.app.App;
import genj.gedcom.Change;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.GedcomListener;
import genj.renderer.EntityRenderer;
import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.view.ToolBarSupport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 * A rendering component showing a resume of the currently
 * selected entity
 */
public class ResumeView extends JPanel implements ToolBarSupport {

  /** language resources we use */  
  private final static Resources resources = new Resources("genj.resume");
  
  /** a registry we keep */
  private Registry registry;
  
  /** the renderer we're using */      
  private EntityRenderer renderer = NORENDERER;
  
  /** a unset renderer */      
  private final static EntityRenderer NORENDERER = new EntityRenderer(resources.getString("html.select"));
  
  /**
   * Constructor
   */
  public ResumeView(Gedcom gedcom, Registry reg, Frame frame) {
    // save some stuff
    registry = reg;
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
    // set first entity
    setEntity(gedcom.getLastEntity());
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
    renderer.render(g, new Dimension(bounds.width,bounds.height/2));
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
    renderer = NORENDERER;
    setEntity(e);
  }
    
  /**
   * Sets the entity to show the resume for
   */
  public void setEntity(Entity e) {
    if (e==null) renderer=NORENDERER;
    else {
      if (renderer==NORENDERER||renderer.getEntity().getType()!=e.getType()) {
        renderer = new EntityRenderer(getHtml(e.getType()));
      }
      renderer.setEntity(e);
    }
    repaint();
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
    /**
     * @see genj.gedcom.GedcomListener#handleSelection(Selection)
     */
    public void handleSelection(Entity entity, boolean emphasized) {
      setEntity(entity);
    }
  } //GedcomConnector

} //ResumeView
