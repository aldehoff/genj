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
package genj.timeline;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.Scrollable;

/**
 * The content rendering the timeline model
 */
/*package*/ class Content extends JComponent {
  
  /** the model */
  private Model model;
  
  /**
   * Constructor
   */
  /*package*/ Content(Model model) {
    
    // remember
    this.model = model;
    
    // done
  }

  /**
   * @see java.awt.Component#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension((int)(model.getSpan()*32),128);
  }

  /**
   * @see javax.swing.JComponent#paintComponent(Graphics)
   */
  protected void paintComponent(Graphics g) {
    g.setColor(Color.lightGray);
    g.fillRect(0,0,getWidth(),getHeight());
    g.setColor(Color.red);
    g.drawRect(0,0,getWidth()-1,getHeight()-1);
  }

} //TimelineContent
