package genj.tree;

import genj.gedcom.Gedcom;
import genj.print.PrintRenderer;
import gj.ui.UnitGraphics;

import java.awt.Color;
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
  public Point getNumPages(Point2D pageSize) {
    return new Point(1,1);
  }

  /**
   * @see genj.print.PrintRenderer#renderPage(java.awt.Point, gj.ui.UnitGraphics)
   */
  public void renderPage(Point page, UnitGraphics g) {
   
    Rectangle2D clip = g.getClip();
    g.translate(clip.getMinX(), clip.getMinY());
    
    ContentRenderer renderer = new ContentRenderer();
    renderer.cArcs          = Color.black;
    renderer.cFamShape      = Color.black;
    renderer.cIndiShape     = Color.black;
    renderer.cUnknownShape  = Color.black;
    renderer.indiRenderer   = tree.getEntityRenderer(g.getGraphics(), Gedcom.INDIVIDUALS);
    renderer.famRenderer    = tree.getEntityRenderer(g.getGraphics(), Gedcom.FAMILIES   );
    renderer.render(g, tree.model);

  }

} //TreePrintRenderer
