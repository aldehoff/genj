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
package genj.tree;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ViewPortAdapter;

/**
 * TreeView
 */
public class TreeView extends JScrollPane {
  
  /*package*/ static final Resources resources = new Resources(TreeView.class); 
  
  /** our model */
  private Model model;

  /** our content */
  private Content content = new Content();
  
  /** our content renderer */
  private ContentRenderer contentRenderer = new ContentRenderer();
  
  /**
   * Constructor
   */
  public TreeView(Gedcom gedcm, Registry regstry, Frame frame) {
    // setup content
    setViewportView(new ViewPortAdapter(content));
    // init model
    model = new Model((Fam)gedcm.getEntities(Gedcom.FAMILIES).get(0));
    // done
  }
  
  /**
   * The content we use for drawing
   */
  private class Content extends JComponent {
    
    /**
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
      return contentRenderer.getDimension(model);
    }
  
    /**
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    protected void paintComponent(Graphics g) {
      g.setColor(Color.blue);
      g.fillRect(0,0,1024,1024);
      // init renderer
      contentRenderer.cBackground = Color.white;
      contentRenderer.cIndiShape  = Color.black;
      contentRenderer.cArcs       = Color.blue;
      // let the renderer do its work
      contentRenderer.render(g, model);
      // done
    }
    
  } //Content

} //TreeView
