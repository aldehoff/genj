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
package genj.gedcom;


/**
 * Gedcom Property with multiple lines
 */
public class PropertyMultilineValue extends PropertySimpleValue implements MultiLineSupport {
  
  /**
   * Constructor of cause Gedcom-line
   */
  public PropertyMultilineValue(String tag, String value) {
    super(tag, value);
  }

  /**
   * Which Proxy to use for this property
   */
  public String getProxy() {
    return "MLE";
  }

  /**
   * Accessor Value
   */
  public String getValue() {
    return getFirstLine(super.getValue());
  }
  
  /**
   * Helper to resolve the first line of a multiline value
   */
  /*package*/ static String getFirstLine(String value) { 

    // More than one line ?
    int pos = value.indexOf('\n');
    if (pos>=0) 
      return value.substring(0,pos)+"...";
      
    // Longer than 255?
    if (value.length()>255)
      return value.substring(0,252)+"...";

    // Value
    return value;
  }
  
  /**
   * @see genj.gedcom.MultiLineSupport#getLinesValue()
   */
  public String getLinesValue() {
    return super.getValue();
  }

  
  /**
   * @see genj.gedcom.MultiLineSupport#getLines()
   */
  public Line getLines() {
    return new MLLine(getTag(), super.getValue());
  }
  
  /**
   * An iterator for lines
   */
  /*package*/ static class MLLine implements Line {
    
    /** the tag */
    private String tag, next;
    
    /** the value */
    private String value;
    
    /** the current segment */
    private int start,end;
    
    /**
     * Constructor
     */
    /*package*/ MLLine(String top, String linesValue) {
      
      tag = top;
      next = top;
      value = linesValue;
       
      start = 0;
      end = 0;
      next();
    }
    
    /**
     * @see genj.gedcom.MultiLineSupport.Line#getTag()
     */
    public String getTag() {
      return tag;
    }
    
    /**
     * @see genj.gedcom.MultiLineSupport.Line#getValue()
     */
    public String getValue() {
      return value.substring(start, end).trim();
    }
      
    /**
     * @see genj.gedcom.MultiLineSupport.Line#next()
     */
    public boolean next() {
      
      tag = next;
      
      // nothing more there?
      if (end==value.length()) return false;
      
      // continue from last end
      start = end;
      
      // assume taking all
      end = value.length();
      
      // skip leading whitespace
      while (true) {
        if (!Character.isWhitespace(value.charAt(start))) break;
        start++;
        if (start==end) return false;
      }
      
      // take all up to next CR
      int cr = value.indexOf('\n', start);
      if (cr>=0) {
        end = cr;
        next = "CONT";
      } 
      
      // but max of 255
      if (end-start>255) {
        end = start+255;
        next = "CONC";
        
        // make sure we don't end with white-space
        while ( (end>start+128) && (Character.isWhitespace(value.charAt(end-1)) || Character.isWhitespace(value.charAt(end))) )
          end--;
      }
      
      // done
      return end>start;
    }
    
  } //MLLine

} //PropertyMultilineValue
