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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.renderer.EntityRenderer;
import genj.util.Resources;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property
 */
/*package*/ abstract class Proxy  {
  
  /** the resources */
  protected final static Resources resources = EditView.resources;
  
  /** the proxied property */
  protected Property property;
  
  /** the edit view */
  protected EditView view;
  
  /** the label header */
  protected JLabel label;

  /**
   * Start editing a property through proxy
   * @return component to receive focus
   */
  protected final JComponent start(JPanel panel, Property prop, EditView edit) {
    // remember
    property = prop;
    view = edit;
    // a text for the user
    String txt = prop.getTag() + " - "+ Gedcom.getName(prop.getTag());
    // setup a label
    label = new JLabel(txt, prop.getImage(true), SwingConstants.LEFT);
    panel.add(label);
    // continue with sub-implementation dependent 
    return start(panel);
  }
  
  /**
   * Implementation
   */
  protected abstract JComponent start(JPanel panel);
  
  /**
   * Returns change state of proxy
   */
  protected abstract boolean hasChanged();

  /**
   * Stop editing a property through proxy. Return <b>true</b>
   * in case that sub-properties have been created/removed.
   */
  protected abstract void finish();
  
  /**
   * Our content
   */
  protected class Preview extends JComponent {
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
      g.setColor(Color.white); //Color.WHITE is 1.4 only
      g.fillRect(box.x, box.y, box.width, box.height);
      // render entity
      if (renderer==null) 
        renderer = new EntityRenderer(view.manager.getBlueprintManager().getBlueprint(entity.getTag(), ""));
      renderer.render(g, entity, box);
      // done
    }
  } //Content

} //Proxy
