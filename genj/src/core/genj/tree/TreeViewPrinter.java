package genj.tree;

import genj.gedcom.Gedcom;
import genj.print.Printer;
import genj.util.swing.UnitGraphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

/**
 * A print renderer for tree */
public class TreeViewPrinter implements Printer {
  
  /** the tree view */
  private TreeView tree;
  
  /**
   * Sets the view to print   */
  public void setView(JComponent view) {
    tree = (TreeView)view;
  }

  /**
   * @see genj.print.PrintRenderer#getNumPages(java.awt.geom.Point2D)
   */
  public Point calcPages(Point2D pageSize, Point2D resolution) {
    return new Point(1,1);
  }

  /**
   * @see genj.print.PrintRenderer#renderPage(java.awt.Point, gj.ui.UnitGraphics)
   */
  public void renderPage(Graphics2D g, Point page, Point2D resolution) {

    UnitGraphics graphics = new UnitGraphics(g, resolution.getX(), resolution.getY());

    Rectangle2D clip = graphics.getClip();
    graphics.translate(clip.getMinX(), clip.getMinY());
    
    ContentRenderer renderer = new ContentRenderer();
    renderer.cArcs          = Color.black;
    renderer.cFamShape      = Color.black;
    renderer.cIndiShape     = Color.black;
    renderer.cUnknownShape  = Color.black;
    renderer.indiRenderer   = tree.getEntityRenderer(Gedcom.INDIVIDUALS)
      .setResolution(resolution);
    renderer.famRenderer    = tree.getEntityRenderer(Gedcom.FAMILIES   )
      .setResolution(resolution);
    
    renderer.render(graphics, tree.getModel());

  }

} //TreePrintRenderer
