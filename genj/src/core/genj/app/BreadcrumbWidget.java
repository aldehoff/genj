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
package genj.app;

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.util.Trackable;
import genj.util.swing.GraphicsHelper;
import genj.view.SelectionSink;
import genj.view.View;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 * A breadcrumb of entities for history like return-to-previous functionality
 */
public class BreadcrumbWidget extends JComponent {
  
  private final static int PAD = 4;
  private final static Icon STEP =  GraphicsHelper.getIcon(0, 0, 0, 8, 4, 4);
  
  private List<Entity> history = new ArrayList<Entity>();
  private List<Integer> xs = new ArrayList<Integer>(); 
  private int highlight = -1;
  private EventHandler events = new EventHandler();
  
  /**
   * Constructor
   */
  public BreadcrumbWidget(Workbench workbench) {
    workbench.addWorkbenchListener(events);
    addMouseListener(events);
    addMouseMotionListener(events);
  }
  
  @Override
  public Dimension getPreferredSize() {
    int h = Math.max(Gedcom.getImage().getIconHeight() + PAD, getFontMetrics(getFont()).getHeight() + PAD);
    return new Dimension(32*1024, h);
  }
  
  @Override
  public Dimension getMinimumSize() {
    int h = Math.max(Gedcom.getImage().getIconHeight() + PAD, getFontMetrics(getFont()).getHeight() + PAD);
    return new Dimension(0, h);
  }
  
  @Override
  public void paint(Graphics g) {
    
    Rectangle r = getBounds();
    Color c = getForeground();

    Graphics2D g2d = (Graphics2D)g;
    
    xs.clear();
 
    int y = r.height/2;
    int x = PAD;
    for (int i=0;i<history.size(); i++) {
      
      int last = x;
      
      Entity e = history.get(history.size()-(i+1));

      if (i>0) {
        STEP.paintIcon(this, g, x, y - STEP.getIconHeight()/2);
        x += STEP.getIconWidth() + PAD;
      }

      float fade = i==highlight ? 1F : Math.max(0, (1-x/(float)r.getWidth()));

      ImageIcon icon = e.getImage();
      Image img = icon.getImage();
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
      g.drawImage(img, x, y-icon.getIconHeight()/2, icon.getIconWidth(), icon.getIconHeight(), null);
      x += icon.getIconWidth() + PAD;

      Rectangle box = GraphicsHelper.render(g2d, e.toString(false), x, y, 0, 0.5);
      
      if (i==highlight)
        g.drawLine(box.x, box.y+box.height, box.x+box.width, box.y+box.height);
        
      x += (int)box.width + PAD;
      
      if (x>r.width) {
        g2d.setComposite(AlphaComposite.SrcOver);
        g.setColor(getBackground());
        g.fillRect(last, 0, x-last, r.height);
        break;
      }
      
      xs.add(x);

    }

    // done
  }
  
  private class EventHandler extends MouseAdapter implements WorkbenchListener, GedcomListener {

    @Override
    public void mouseMoved(MouseEvent e) {
      for (int i=0; i<xs.size(); i++) {
        if (e.getPoint().x<xs.get(i)) {
          setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          highlight = i;
          repaint();
          return;
        }
      }
      if (highlight>=0) {
        highlight = -1;
        repaint();
      }
      setCursor(null);
    }

    @Override
    public void mouseExited(MouseEvent e) {
      if (highlight>=0) {
        highlight = -1;
        repaint();
      }
    }
    
    public void mouseClicked(MouseEvent e) {
      for (int i=0; i<xs.size(); i++) {
        if (e.getPoint().x<xs.get(i)) {
          highlight = -1;
          SelectionSink.Dispatcher.fireSelection(BreadcrumbWidget.this, new Context(history.get(history.size()-1-i)), true);
          return;
        }
      }
    }

    @Override
    public void commitRequested(Workbench workbench) {
    }

    @Override
    public void gedcomClosed(Workbench workbench, Gedcom gedcom) {
      history.clear();
      xs.clear();
      gedcom.removeGedcomListener(this);
    }

    @Override
    public void gedcomOpened(Workbench workbench, Gedcom gedcom) {
      gedcom.addGedcomListener(this);
    }

    @Override
    public void processStarted(Workbench workbench, Trackable process) {
    }

    @Override
    public void processStopped(Workbench workbench, Trackable process) {
    }

    @Override
    public void selectionChanged(Workbench workbench, Context context, boolean isActionPerformed) {
      
      Entity e = context.getEntity();
      if (e==null)
        return;

      // don't add twice to tail
      if (!history.isEmpty() && history.get(history.size()-1) == e)
        return;

      // pull forward
      int i = history.indexOf(e);
      if (i>=0) 
        history.remove(i);

      // add
      history.add(e);
      
      // trim
      while (history.size()>50)
        history.remove(0);
      
      // show
      xs.clear();
      repaint();
    }

    @Override
    public void viewClosed(Workbench workbench, View view) {
    }

    @Override
    public void viewOpened(Workbench workbench, View view) {
    }

    @Override
    public void viewRestored(Workbench workbench, View view) {
    }

    @Override
    public boolean workbenchClosing(Workbench workbench) {
      return true;
    }

    @Override
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
    }

    @Override
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
      int i = history.indexOf(entity);
      if (i<0)
        return;
      
      history.remove(i);
      xs.clear();
      repaint();
      
      if (i==history.size()&&!history.isEmpty())
        SelectionSink.Dispatcher.fireSelection(BreadcrumbWidget.this, new Context(history.get(history.size()-1)), true);
    }

    @Override
    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
    }

    @Override
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
    }

    @Override
    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
    }
  }
}
