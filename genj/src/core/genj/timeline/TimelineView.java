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

import java.awt.BorderLayout;
import java.awt.Frame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import genj.gedcom.Gedcom;
import genj.util.Registry;
import genj.util.swing.*;
import genj.view.ToolBarSupport;

/**
 * Component for showing entities' events in a timeline view
 */
public class TimelineView extends JPanel implements ToolBarSupport {
  
  /** our model */
  private Model model;
  
  /** our content */
  private Content content;
  
  /** our ruler */
  private Ruler ruler;
  
  /**
   * Constructor
   */
  public TimelineView(Gedcom gedcom, Registry registry, Frame frame) {
    
    // create our sub-parts
    model = new Model(gedcom);
    content = new Content(model);
    ruler = new Ruler(model);
    
    // all that fits in a scrollpane
    JScrollPane scroll = new JScrollPane(new ViewPortAdapter(content));
    scroll.setColumnHeaderView(new ViewPortAdapter(ruler));
        
    
    // layout
    setLayout(new BorderLayout());
    add(scroll, BorderLayout.CENTER);
    
    // done
  }

  /**
   * @see genj.view.ToolBarSupport#populate(JToolBar)
   */
  public void populate(JToolBar bar) {
  }
  
} //TimelineView
