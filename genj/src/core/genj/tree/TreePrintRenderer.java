package genj.tree;

import genj.gedcom.Gedcom;
import genj.print.PrintRenderer;
import genj.util.swing.UnitGraphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A print renderer for tree */
public class TreePrintRenderer implements PrintRenderer {
  
  /** the tree view */
  private TreeView tree;
  
  /**
   * Constructor   */
  public TreePrintRenderer(TreeView trEe) {
    tree = trEe;
  }

  /**
   * @see genj.print.PrintRenderer#getNumPages(java.awt.geom.Point2D)
   */
  public Point getNumPages(Point2D pageSize, Point2D resolution) {
    return new Point(1,1);
  }

  /**
   * @see genj.print.PrintRenderer#renderPage(java.awt.Point, gj.ui.UnitGraphics)
   */
  public void renderPage(Point page, Graphics2D g, Point2D resolution) {

    UnitGraphics graphics = new UnitGraphics(g, resolution.getX(), resolution.getY());

    Rectangle2D clip = graphics.getClip();
    graphics.translate(clip.getMinX(), clip.getMinY());
    
    ContentRenderer renderer = new ContentRenderer();
    renderer.cArcs          = Color.black;
    renderer.cFamShape      = Color.black;
    renderer.cIndiShape     = Color.black;
    renderer.cUnknownShape  = Color.black;
    renderer.indiRenderer   = tree.getEntityRenderer(g, Gedcom.INDIVIDUALS);
    renderer.famRenderer    = tree.getEntityRenderer(g, Gedcom.FAMILIES   );
    renderer.render(graphics, tree.model);

  }

} //TreePrintRenderer
