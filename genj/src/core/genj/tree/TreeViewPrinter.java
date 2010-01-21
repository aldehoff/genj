/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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

import genj.print.Page;
import genj.print.PrintRenderer;

import java.awt.Dimension;
import java.awt.Graphics2D;

/**
 * A print renderer for tree */
public class TreeViewPrinter implements PrintRenderer {
  
  public boolean yes;
  
  /**
   * @see genj.print.PrintRenderer#getPages(Page)
   */
  public Dimension getPages(Page page) {
    
    // FIXME calc tree print size
    
    return new Dimension();
//    Rectangle mmbounds = tree.getModel().getBounds();
//    return new Dimension(
//      (int)Math.ceil(mmbounds.width*0.1F/2.54F / pageSizeInInches.getWidth()), 
//      (int)Math.ceil(mmbounds.height*0.1F/2.54F  / pageSizeInInches.getHeight())
//    );
  }

  /**
   * @see genj.print.PrintRenderer#renderPage(Graphics2D, Page)
   */
  public void renderPage(Graphics2D g, Page page) {
    
    // FIXME print render tree
    

//    // translate to correct page and give a hint of renderable space in gray
//    UnitGraphics ug = new UnitGraphics(g, dpi.x, dpi.y);
//    ug.setColor(Color.LIGHT_GRAY);
//    ug.draw(new Rectangle2D.Double(0,0,pageSizeInInches.getWidth(),pageSizeInInches.getHeight()),0,0);
//    ug.translate(
//      -page.x*pageSizeInInches.getWidth(), 
//      -page.y*pageSizeInInches.getHeight()
//    );
//
//    // prepare rendering on mm/10 space
//    UnitGraphics graphics = new UnitGraphics(g, dpi.x/2.54F*0.1D, dpi.y/2.54F*0.1D);
//    
//    ContentRenderer renderer = new ContentRenderer();
//    renderer.cArcs          = Color.black;
//    renderer.cFamShape      = Color.black;
//    renderer.cIndiShape     = Color.black;
//
//    if (!preview) {    
//      renderer.indiRenderer   = tree.createEntityRenderer(Gedcom.INDI);
//      renderer.famRenderer    =  tree.createEntityRenderer(Gedcom.FAM);
//    }
//    
//    g.setRenderingHint(DPIHintKey.KEY, dpi);
//    
//    renderer.render(graphics, tree.getModel());

  }

} //TreePrintRenderer
