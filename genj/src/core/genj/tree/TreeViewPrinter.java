package genj.tree;

import genj.gedcom.Gedcom;
import genj.print.Printer;
import genj.util.swing.UnitGraphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

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
  public Point calcPages(Point pageSize, Point resolution) {
    
    Rectangle mmbounds = tree.getModel().getBounds();
    
    return new Point(
      (int)Math.ceil(mmbounds.width *0.1D/2.54D * resolution.x / pageSize.x),
      (int)Math.ceil(mmbounds.height*0.1D/2.54D * resolution.y / pageSize.y)
    );
    
  }

  /**
   * @see genj.print.PrintRenderer#renderPage(java.awt.Point, gj.ui.UnitGraphics)
   */
  public void renderPage(Graphics2D g, Point page, Point resolution) {

    UnitGraphics graphics = new UnitGraphics(g, resolution.x/2.54D*0.1D, resolution.y/2.54D*0.1D);

    ContentRenderer renderer = new ContentRenderer();
    renderer.cArcs          = Color.black;
    renderer.cFamShape      = Color.black;
    renderer.cIndiShape     = Color.black;
    renderer.indiRenderer   = tree.getEntityRenderer(Gedcom.INDIVIDUALS)
      .setResolution(resolution);
    renderer.famRenderer    = tree.getEntityRenderer(Gedcom.FAMILIES   )
      .setResolution(resolution);
    
    renderer.render(graphics, tree.getModel());

  }

} //TreePrintRenderer
