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
package genj.fo;

import java.io.OutputStream;

import javax.xml.transform.sax.SAXResult;

/**
 * Formatter for PDF - using FOP
 */
public class PDFFormatter extends XSLFOFormatter {

  /**
   * Constructor
   */
  public PDFFormatter() {
    super("PDF", "pdf", false);
  }
  
  /**
   * our format logic
   */
  protected void formatImpl(Document doc, OutputStream out) throws Throwable {

// TRUNK - the new FOP 
    // create FOP tree builder that does the trick
    org.xml.sax.ContentHandler handler = new org.apache.fop.fo.FOTreeBuilder("application/pdf", new org.apache.fop.apps.FOUserAgent(), out);
    super.formatImpl(doc, new SAXResult(handler));

// MAINTENANCE - the 0.20.3 branch way of doing things
//    org.apache.fop.apps.Driver driver = new org.apache.fop.apps.Driver();
//    driver.setRenderer(new org.apache.fop.render.pdf.PDFRenderer());
//    //driver.setRenderer(new org.apache.fop.render.txt.TXTRenderer());
//    driver.setOutputStream(out);
//      super.formatImpl(doc, new SAXResult(driver.getContentHandler()));

      // done
  }
  
}
