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
 */package genj.fo;

import java.io.File;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;

/** 
 * Formatter for XSL-FO 
 */
public class XSLFOFormatter extends Formatter {
  
  private final static File XSL = new File("./contrib/docbook-xsl/fo/docbook.xsl");

  private final static String[] PAPER_TYPES = {
    "USletter", "A0",  "A1",  "A2",  "A3",  "A4",  "A5",  "A6",  "A7",  "A8",  "A9",  "A10",  "B0",  "B1",  "B2",  "B3",  "B4",  "B5",  "B6",  "B7",  "B8",  "B9",  "B10",  "C0",  "C1",  "C2",  "C3",  "C4",  "C5",  "C6",  "C7",  "C8",  "C9",  "C10"
  };
  
  private final static String[] PAGE_ORIENTATION = {
    "portrait",
    "landscape"
  };
  
  /**
   * Constructor
   */
  public XSLFOFormatter() {
    super("XSL-FO", "fo", true);
  }
  
  /**
   * Constructor for subclasses
   */
  public XSLFOFormatter(String format, String suffix, boolean isExternalizeFiles) {
    super(format, suffix, isExternalizeFiles);
  }
  
  /**
   * Formatting logic 
   */
  protected void formatImpl(Document doc, OutputStream out) throws Throwable {
    
    formatImpl(doc, new StreamResult(out));
    
  }
  
  protected void formatImpl(Document doc, Result result) throws Throwable {
    
    // grab xsl transformer
    Transformer transformer = getTemplates(XSL).newTransformer();
    
    // set : select indexterms based on type attribute value
    transformer.setParameter("index.on.type", "1");

    // set: page parameters
    transformer.setParameter("paper.type", "A4");
    transformer.setParameter("page.orientation", "portrait");
    
    // set: generate.toc
    if (!doc.isTOC())
      transformer.setParameter("generate.toc", "'/article nop'");

    // what about margins?
    //page.margin.bottom
    //page.margin.inner
    //page.margin.outer
    //page.margin.top
    
    // do the transformation
    transformer.transform(doc.getDOMSource(), result);

    // done
  }
  
}