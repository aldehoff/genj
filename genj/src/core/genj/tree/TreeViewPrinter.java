package genj.tree;

import genj.gedcom.Gedcom;
import genj.print.Printer;
import genj.util.swing.UnitGraphics;

import java.awt.Color;
import java.awt.Dimension;
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
   * @see genj.print.Printer#calcSize(java.awt.Point)
   */
  public Dimension calcSize(Point resolution) {
    
    Rectangle mmbounds = tree.getModel().getBounds();
    
    return new Dimension(
      (int)Math.ceil(mmbounds.width *0.1D/2.54D * resolution.x),
      (int)Math.ceil(mmbounds.height*0.1D/2.54D * resolution.y)
    );
    
  }

  /**
   * @see genj.print.PrintRenderer#renderPage(java.awt.Point, gj.ui.UnitGraphics)
   */
  public void renderPage(Graphics2D g, Point page, Point resolution, boolean preview) {

    UnitGraphics graphics = new UnitGraphics(g, resolution.x/2.54D*0.1D, resolution.y/2.54D*0.1D);

    ContentRenderer renderer = new ContentRenderer();
    renderer.cArcs          = Color.black;
    renderer.cFamShape      = Color.black;
    renderer.cIndiShape     = Color.black;
    renderer.selection      = null;

    if (!preview) {    
      renderer.indiRenderer   = tree.getEntityRenderer(Gedcom.INDI)
        .setResolution(resolution);
      renderer.famRenderer    = tree.getEntityRenderer(Gedcom.FAM )
        .setResolution(resolution);
    }
    
    renderer.render(graphics, tree.getModel());

  }

} //TreePrintRenderer
