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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.Vector;

import genj.gedcom.*;

/**
 * A component that displays an overview over a tree
 */
public class Overview extends Component implements AdjustmentListener, TreeModelListener, PropertyChangeListener {

  private TreeView  tree      ;
  private Rectangle oldView   ;

  private float     zoom      ;
  private Dimension treeSize  = new Dimension(0,0);
  private Dimension viewSize  = treeSize;
  private Dimension overSize  = treeSize;
  private Point     treeOffset= new Point(0,0);
  private float     treeZoom  ;

  /**
   * Class which provides mouse handling
   */
  private class MouseHandler implements MouseMotionListener, MouseListener {

    private Point base;

    /**
     * Constructor
     */
    private MouseHandler(Component c) {
      c.addMouseMotionListener(this);
      c.addMouseListener(this);
    }

    /**
     * Mouse is dragged
     */
    public void mouseDragged(MouseEvent e) {

      // Dragging from point in view?
      if (base==null) {
        return;
      }

      // Drag view
      tree.setScrollPosition(new Point(
        (int)( (e.getX()-base.x)/zoom*treeZoom ) ,
        (int)( (e.getY()-base.y)/zoom*treeZoom )
      ));

      // Done
    }

    /**
     * Mouse has been clicked
     */
    public void mouseClicked(MouseEvent e) {

      tree.setScrollPosition(new Point(
        (int)( (e.getX())/zoom*treeZoom -viewSize.width /2),
        (int)( (e.getY())/zoom*treeZoom -viewSize.height/2)
      ));
    }

    /**
     * Mouse is moved above
     */
    public void mouseMoved(MouseEvent e) {

      // Draggable View
      if ( (oldView!=null) && (oldView.contains(e.getX(),e.getY())) ) {
        base = new Point(e.getX()-oldView.x,e.getY()-oldView.y);
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR   ));
      } else {
        base = null;
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }

      // Done
    }

    /** empty implementors */
    public void mouseEntered (MouseEvent e) {}
    public void mouseExited  (MouseEvent e) {}
    public void mousePressed (MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}

    // EOC
  }

  /**
   * Handles change in layout of tree
   */
  public void adjustmentValueChanged(AdjustmentEvent ae) {
    treeOffset = tree.getScrollPosition();
    paintView(getGraphics());
  }

  /**
   * Notification in case actual link has changed
   */
  public void handleActualChanged(Link oldActual, Link newActual) {
    repaint();
  }

  /**
   * Notification in case data and not structure of model has changed
   */
  public void handleDataChanged() {
  }

  /**
   * Notification in case entities have changed without change in layout
   * A listener can use iterate to do actions on links to that entities.
   */
  public void handleEntitiesChanged(Vector changed) {
  }

  /**
   * Notification that reading is possible again
   */
  public void handleReadAgain() {
  }

  /**
   * Notification in case structure of model has changed
   */
  public void handleStructureChanged() {
    repaint();
  }

  /**
   * Starts drawing
   */
  public void paint(Graphics g) {

    // Calc parms
    Dimension size = getSize();
    TreeModel model= tree.getModel();

    viewSize   = tree.getSize();
    treeOffset = tree.getScrollPosition();
    treeSize   = model.getSize();
    treeZoom   = tree.getZoom  ();
    zoom       = Math.min(1.0F, Math.min( (float)size.width / treeSize.width, (float)size.height / treeSize.height));
    overSize   = new Dimension( (int)(treeSize.width*zoom),(int)(treeSize.height*zoom) );

    // Clear background
    g.setColor(Color.gray);
    g.fillRect(0,0,size.width,size.height);
    g.setColor(Color.white);
    g.fillRect(0,0,overSize.width,overSize.height);

    // Draw individuals who are ancestors and draw the root
    final TreeGraphics tg = new TreeGraphics(
      g,
      tree.getModel(),
      zoom,
      false,
      false,
      false,
      false,
      false
    );

    java.util.Enumeration links = model.getLinks();
    while (links.hasMoreElements()) {
      ((Link)links.nextElement()).paint(tg,false,false);
    }

    Link actual = model.getActualLink();
    if (actual!=null) {
      tg.drawHighlight(actual);
    }

    // Draw border
    g.setColor(Color.black);
    g.drawRect(0,0,overSize.width-1,overSize.height-1);

    // Draw View
    oldView=null;
    paintView(g);

    // Done
  }

  /**
   * Helper that draw TreePane's view of Tree
   */
  void paintView(Graphics g) {

    // Prepare XOR
    g.setColor(Color.lightGray);
    g.setXORMode(Color.white);

    // Restore old view
    if (oldView!=null) {
      g.fillRect(oldView.x,oldView.y,oldView.width,oldView.height);
    }

    // Draw new view
    double x1 = treeOffset.x/treeZoom * zoom;
    double y1 = treeOffset.y/treeZoom * zoom;
    double x2 = Math.min(
      (treeOffset.x+viewSize.width )/treeZoom * zoom,
      overSize.width
    );
    double y2 = Math.min(
      (treeOffset.y+viewSize.height)/treeZoom * zoom,
      overSize.height
    );

    oldView = new Rectangle(
      (int)x1,
      (int)y1,
      (int)(x2-x1)+1,
      (int)(y2-y1)+1
    );

    g.fillRect(oldView.x,oldView.y,oldView.width,oldView.height);

    // Done
  }

  /**
   * Property Change Support
   */
  public void propertyChange(PropertyChangeEvent evt) {

    // Zoom of Tree?
    if ((evt.getPropertyName()=="zoom")||(evt.getPropertyName()=="size")) {
      repaint();
    }
  }

  /**
   * Notification that we're not used anymore - we'll detach
   * from listening to the adjustment of the Tree
   */
  public void removeNotify() {
    tree.removeAdjustmentListener(this);
    tree.removePropertyChangeListener(this);
    tree.getModel().removeTreeModelListener(this);
  }

  /**
   * Constructor
   */
  public Overview(TreeView setTree) {

    // Remember
    tree=setTree;

    // Listeners
    new MouseHandler(this);
    tree.getModel().addTreeModelListener(this);

    tree.addAdjustmentListener(this);
    tree.addPropertyChangeListener(this);

    // Done
  }

}
