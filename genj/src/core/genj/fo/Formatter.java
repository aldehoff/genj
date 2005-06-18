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

/**
 * A document formatter
 */
public abstract class Formatter {

  /** available formatters */
  public static Formatter[] FORMATTERS = { 
    new PDF(), new HTML(), new XMLFO(), new DocBook()
  };

  /** this format */
  private String format;

  /** constructor */
  protected Formatter(String format) {
    this.format = format;
  }
  
  /**
   * Text representation
   */
  public String toString() {
    return format;
  }

  /** Formatter - DocBook */
  private static class DocBook extends Formatter {
    private DocBook() {
      super("DocBook");
    }
  }
  
  /** Formatter - HTML */
  private static class HTML extends Formatter {
    private HTML() {
      super("HTML");
    }
  }
  
  /** Formatter - XML FO */
  private static class XMLFO extends Formatter {
    private XMLFO() {
      super("XML-FO");
    }
  }
  
  /** Formatter - PDF */
  private static class PDF extends Formatter {
    private PDF() {
      super("PDF");
    }
  }
  
  
}
