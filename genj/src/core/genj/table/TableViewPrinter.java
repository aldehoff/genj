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
package genj.table;

import genj.gedcom.Property;
import genj.print.Printer;
import genj.renderer.PropertyRenderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.Dimension2D;

import javax.swing.JComponent;

/**
 * A print renderer for table */
public class TableViewPrinter implements Printer {
  
  /** print state */
  private int[] 
    rowHeights,
    colWidths;
  private int headerHeight;

  
  /** the table view */
  private TableView table;
  
  /** font */
  private Font font = new Font("SansSerif", Font.PLAIN, 8);
  
  /**
   * Sets the view to print   */
  public void setView(JComponent view) {
    table = (TableView)view;
  }

  /**
   * @see genj.print.Printer#calcSize(Dimension2D, Point)
   */
  public Dimension calcSize(Dimension2D pageSizeInInches, Point dpi) {

    // grab model
    EntityTableModel model = table.getModel();

    // prepare our font render context
    FontRenderContext context = new FontRenderContext(null, false, true);

    // prepare data
    int pageWidth = (int)Math.ceil(pageSizeInInches.getWidth()*dpi.x);
    int pageHeight = (int)Math.ceil(pageSizeInInches.getHeight()*dpi.y);
    
    headerHeight = 0;
    rowHeights = new int[model.getRowCount()];
    colWidths = new int[model.getColumnCount()];
    
    // calculate header parameters
    for (int col=0;col<colWidths.length;col++) {
      String header = model.getColumnName(col);
      Dimension2D dim = PropertyRenderer.DEFAULT_RENDERER.getSize(font, context, null, header, PropertyRenderer.PREFER_TEXT, dpi);
      headerHeight = (int)Math.max(headerHeight, Math.ceil(dim.getHeight()));
      colWidths[col] = (int)Math.max(colWidths[col], Math.ceil(dim.getWidth()));
    }
    
    // loop rows
    for (int row=0, height=0;row<rowHeights.length;row++) {

      // loop columns
      for (int col=0;col<colWidths.length;col++) {
        
        // check property
        Property prop = model.getProperty(row, col);
        if (prop==null)
          continue;
        
        // grab size
        Dimension2D dim = PropertyRenderer.get(prop).getSize(font, context, prop, PropertyRenderer.PREFER_DEFAULT, dpi);
        
        // update col and row
        rowHeights[row] = (int)Math.max(rowHeights[row], Math.ceil(dim.getHeight()));
        colWidths[col] = (int)Math.max(colWidths[col], Math.ceil(dim.getWidth()));
        
        // next col
      }
      
      // check page fit vertically
    
      // next row
    }

    // Prepare result
    Dimension pages = new Dimension(1,1);
    
    // check page fit horizontally
    for (int col=0, width=0;col<colWidths.length;col++) {
      // too much for current page?
      if (width+colWidths[col]>pageWidth) {
        // expand previous column if possible
        if (width>0) {
          colWidths[col-1] += pageWidth-width;
          width = 0;
          pages.width++;
        }
      }
      // increase width
      width += colWidths[col];
    }
    
    
    // done
    return pages;
  }
  
  /**
   * @see genj.print.PrintRenderer#renderPage(java.awt.Point, gj.ui.UnitGraphics)
   */
  public void renderPage(Graphics2D g, Point page, Point dpi, boolean preview) {

    g.scale(dpi.x/72F, dpi.y/72F);

    // testing
    g.setColor(Color.LIGHT_GRAY);
    g.setFont(font.deriveFont(40F));
    g.drawString("TESTING ONLY", 100, 300);
    
    // prepare rendering characteristics
    g.setColor(Color.BLACK);
    g.setFont(font);

    // grab model
    EntityTableModel model = table.getModel();
    
    // draw header
    Shape clip;

    int y = 0;
    int x = 0;
    for (int col=0,cols=model.getColumnCount();col<cols;col++) {
      String header = model.getColumnName(col);
      Rectangle r = new Rectangle(x, 0, colWidths[col], headerHeight); 
      PropertyRenderer.DEFAULT_RENDERER.render(g, r, null, header, PropertyRenderer.PREFER_TEXT, dpi);
      x += colWidths[col];
    }
    y += headerHeight;
    
    // draw line demarcation
    g.drawLine(0, y, x, y++);
    
    // draw rows
    for (int row=0,rows=model.getRowCount();row<rows;row++) {

      // start on the left
      x = 0;
      
      // draw cols
      for (int col=0,cols=model.getColumnCount();col<cols;col++) {

        // calculate space to render in
        Rectangle r = new Rectangle(x, y, colWidths[col], rowHeights[row]);
        Property prop = model.getProperty(row, col);
        if (prop!=null) {
          PropertyRenderer.get(prop)
            .render(g, r, prop, PropertyRenderer.PREFER_DEFAULT, dpi);
        }
        
        // increase current horizontal position
        x += colWidths[col];
        
      }
      
      // increase current vertical position
      y += rowHeights[row];
      
      // draw line demarcation
      g.drawLine(0, y, x, y++);

      // next row
    }
    
    // draw vertical lines
    x = 0;
    for (int col=0,cols=model.getColumnCount();col<cols;col++) {
      x += colWidths[col];
      g.drawLine(x, 0, x, y);
    }

    // done
  }

} //TreePrintRenderer
