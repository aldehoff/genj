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
package genj.option;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

import genj.gedcom.*;

/**
 * Option - Layout Properties in Box
 */
public class OptionLayoutProperties extends Option implements MouseMotionListener, ChangeListener {

  private JLabel     lEntity;
  private JSlider    sGrid;
  private boolean    resizing = false;
  private Point      start;
  private static int grid = 0;

  /**
   * Constructor for Option
   */
  public OptionLayoutProperties(JFrame frame) {

    super(frame);

    // Setup this control
    setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

    // Label that will be layouted by user
    lEntity = new JLabel() {
      /** painting */
      public void paint(Graphics g) {
        int h = getHeight(),
          w = getWidth ();
        // Clear bg
        g.setColor(Color.white);
        g.fillRect(0 , 0, w-1, h-1);
        // Draw grid
        if (grid>1) {
          g.setColor(new Color(240,240,240));
          for (int x=grid*3;x<w;x+=grid*3)
          g.drawLine(x,0,x,h);
          for (int y=grid*3;y<h;y+=grid*3)
          g.drawLine(0,y,w,y);
        }
        // Done
        super.paint(g);
      }
      // EOC
    };
    lEntity.setBorder(BorderFactory.createLineBorder(Color.black));
    lEntity.addMouseMotionListener(this);
    add(lEntity);

    // Slider for grid-settings
    sGrid = new JSlider(JSlider.VERTICAL,0,8,grid);
    sGrid.addChangeListener(this);
    sGrid.setToolTipText("Choose grid setting");
    add(sGrid);

    // Done
  }

  /**
   * Adds a property to be laid out
   */
  public void add(TagPath path, Rectangle box) {

    // Try to find path in existing layout labels
    JComponent c=null;
    int maxy = 0, maxx = 0;
    for (int i=0;i<lEntity.getComponentCount();i++) {
      // .. check for existing property
      c = (JComponent)lEntity.getComponent(i);
      if ( c.getClientProperty("PATH").toString().equals(path.asString()) )
        return;
      // .. calculate max y position
      maxy = Math.max( maxy, c.getLocation().y + c.getSize().height);
      maxx = Math.max( maxx, c.getLocation().x + c.getSize().width);
    }

    // Add it
    JLabel l = new JLabel(path.asString(),
                new ImageIcon(Property.calcDefaultImage(path.getLast()).getImage()),
                JLabel.CENTER);
    l.setBorder(BorderFactory.createLineBorder(Color.lightGray));
    l.setHorizontalAlignment(JLabel.LEFT);
    if (box!=null)
      l.setBounds(box);
    else {
      // .. do the size & position
      l.setSize( l.getPreferredSize() );
      if (maxy > lEntity.getSize().height - l.getSize().height) {
        maxx = Math.min(maxx,lEntity.getSize().width-l.getSize().width);
        l.setLocation( new Point( maxx,lEntity.getSize().height - l.getSize().height) );
      } else {
        l.setLocation( new Point(1,maxy) );
      }
    }
    l.setFont(getFont());
    l.putClientProperty("PATH",path);
    lEntity.add(l,0);

    // Start listening
    l.addMouseMotionListener(this);

    // Done
    l.repaint();
  }

  /**
   * Layout out the enclosed components
   */
  public void doLayout() {
    // Layout GridSlider
    Dimension dim = sGrid.getPreferredSize();
    Insets i = getInsets();
    sGrid.setLocation( getSize().width - i.right - dim.width, i.top );
    sGrid.setSize    ( dim.width, getSize().height - i.top - i.bottom   );

    dim = sGrid.getSize();
    lEntity.setLocation(
      (getWidth ()-dim.width )/2 - lEntity.getSize().width /2,
       getHeight()            /2 - lEntity.getSize().height/2
    );
  }

  /**
   * Return the box for a path.
   */
  public Rectangle getBoxForPath(TagPath path) {

    // Try to find path in existing layout labels
    JComponent c=null;
    for (int i=0;i<lEntity.getComponentCount();i++) {
      c = (JComponent)lEntity.getComponent(i);
      if ( c.getClientProperty("PATH").toString().equals(path.asString()) ) {
        return c.getBounds();
      }
    }
    return null;
  }

  /**
   * Override Preferred Size
   */
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  /**
   * Override Preferred Size
   */
  public Dimension getPreferredSize() {
    return new Dimension(256,128);
  }

  /**
   * Returns size of entities' boxes
   */
  public Dimension getSizeOfEntities() {
    return lEntity.getSize();
  }

  /**
   * Returns all layouted TagPaths
   */
  public TagPath[] getTagPaths() {

    // Try to find path in existing layout labels
    JComponent c=null;
    int count = lEntity.getComponentCount();

    TagPath[] result = new TagPath[count];

    for (int i=0;i<count;i++) {

      c = (JComponent)lEntity.getComponent(i);

      result[i]= (TagPath) c.getClientProperty("PATH");
    }

    return result;
  }

  /**
   * Fired when user dragges one of the components.
   * Resizes or moves that component.
   */
  public void mouseDragged(MouseEvent e) {

    // Calculate source label
    Component c = (Component)e.getSource();
    Rectangle r = c.getBounds();
    Dimension d;

    // Component entity ?
    if (c==lEntity) {

      // Resizing ?
      if (!resizing)
        return;

      // Calculate minimum Size
      int minwidth =48,
          minheight=16;

      Point p;
      for (int i=0;i<lEntity.getComponentCount();i++) {
        c = lEntity.getComponent(i);
        p = c.getLocation();
        d = c.getSize();

        minwidth = Math.max(p.x+d.width +1, minwidth);
        minheight= Math.max(p.y+d.height+1, minheight);
      }

      // Calculate new size
      d = new Dimension(getSize().width-sGrid.getSize().width
                       ,getSize().height);
      Insets isets = getInsets();
      int w = Math.min( Math.max(minwidth,e.getX()) , d.width  -r.x-isets.left-isets.right ),
          h = Math.min( Math.max(minheight,e.getY()) , d.height-r.y-isets.top-isets.bottom );
      lEntity.setSize(new Dimension(w,h));

      // Re-Center
      lEntity.setLocation(d.width/2 - w/2, d.height/2 - h/2 );

      // Done
      return;
    }

    // Dragging or resizing ?
    d = lEntity.getSize();
    int g = sGrid.getValue(),
        x = e.getX(),
        y = e.getY();

    if (resizing) {
      if (g>0) {
        x = ((int)(r.x+x)/(g*3))*(g*3) - r.x;
        y = ((int)(r.y+y)/(g*3))*(g*3) - r.y;
      }
      // Calculate new size
      int w = Math.min( Math.max(8,x) , d.width -r.x-1 ),
          h = Math.min( Math.max(8,y) , d.height-r.y-1 );
      c.setSize(new Dimension(w,h));
    } else {
      // Drag property
      int posx = Math.min( Math.max(1,r.x-start.x+x) , d.width -r.width -1 ),
          posy = Math.min( Math.max(1,r.y-start.y+y) , d.height-r.height-1 );
      if (g>0) {
        posx = ((int)posx/(g*3))*(g*3)+1;
        posy = ((int)posy/(g*3))*(g*3)+1;
      }
      c.setLocation(posx,posy);
    }

    // Done
  }

  /**
   * Fired when the users moves the mouse of one of the labels.
   * Prepares dragging/resizing.
   */
  public void mouseMoved(MouseEvent e) {

    // Calculate source label
    JComponent l = (JComponent)e.getSource();
    start = new Point(e.getX(),e.getY());

    // Check for move or resize
    if ( ( e.getX() > l.getSize().width -8 )
       &&( e.getY() > l.getSize().height-8 ) ) {
      l.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR ));
      resizing = true;
    } else {
      l.setCursor(Cursor.getPredefinedCursor( l==lEntity ? Cursor.DEFAULT_CURSOR : Cursor.MOVE_CURSOR ));
      resizing = false;
    }

    // Done
  }

  /**
   * Removes a property that has been laid out
   */
  public void remove(TagPath path) {

    // Try to find path in existing layout labels
    JComponent c=null;
    for (int i=0;i<lEntity.getComponentCount();i++) {
      c = (JComponent)lEntity.getComponent(i);
      if ( c.getClientProperty("PATH").toString().equals(path.asString()) )
      break;
    }

    // Not found ?
    if (c != null) {
      lEntity.remove(c);
      lEntity.repaint();
    }

    // Done
  }

  /**
   * Removes all properties that have been laid out
   */
  public void removeAll() {
    lEntity.removeAll();
    lEntity.repaint();
  }

  /**
   * Sets Size of entities' boxes
   */
  public void setSizeOfEntities(Dimension size) {
    lEntity.setSize(size);
    doLayout();
  }

  /**
   * Called in case slider value is changed by user
   */
  public void stateChanged(ChangeEvent e) {
    grid = sGrid.getValue();
    lEntity.repaint();
  }

}
