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
public class PropertyMultilineValue extends Property implements MultiLineSupport {
  
  /** the tag */
  private String tag;
  
  /** our value */
  private StringBuffer value = new StringBuffer(80);
  
  /**
   * Which Proxy to use for this property
   */
  public String getProxy() {
    return "MLE";
  }
  
  /**
   * @see genj.gedcom.Property#getTag()
   */
  public String getTag() {
    return tag;
  }

  /**
   * @see genj.gedcom.Property#setTag(java.lang.String)
   */
  Property init(String set, String value) throws GedcomException {
    tag = set;
    return super.init(tag,value);
  }
  
  /**
   * @see genj.gedcom.Property#setValue(java.lang.String)
   */
  public void setValue(String set) {
    modNotify();
    value.setLength(0);
    value.append(set);
  }

  /**
   * Accessor Value
   */
  public String getValue() {
    return getFirstLine(value);
  }
  
  /**
   * @see genj.gedcom.MultiLineSupport#append(int, java.lang.String, java.lang.String)
   */
  public boolean append(int level, String taG, String vaLue) {
    // only level 1 (direct children)
    if (level!=1)
      return false;
    // gotta be CONT or CONC
    boolean 
      isCont = "CONT".equals(taG),
      isConc = "CONC".equals(taG);
    if (!(isConc||isCont))
      return false;
    // grab it
    if (isCont) value.append('\n');
    value.append(vaLue);
    // accepted
    return true;
  }
  
  /**
   * Helper to resolve the first line of a multiline value
   */
  /*package*/ static String getFirstLine(StringBuffer value) { 

    // More than one line ?
    int pos = value.indexOf("\n");
    if (pos>=0) 
      return value.substring(0,pos)+"...";
      
    // Longer than 255?
    if (value.length()>255)
      return value.substring(0,252)+"...";

    // Value
    return value.toString();
  }
  
  /**
   * @see genj.gedcom.MultiLineSupport#getLinesValue()
   */
  public String getLinesValue() {
    return value.toString();
  }

  
  /**
   * @see genj.gedcom.MultiLineSupport#getLines()
   */
  public Line getLines() {
    return new MLLine(getTag(), value);
  }
  
  /**
   * An iterator for lines
   */
  /*package*/ static class MLLine implements Line {
    
    /** the tag */
    private String tag, next;
    
    /** the value */
    private StringBuffer value;
    
    /** the current segment */
    private int start,end;
    
    /**
     * Constructor
     */
    /*package*/ MLLine(String top, StringBuffer linesValue) {
      
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
    public int next() {
      
      tag = next;
      
      // nothing more there?
      if (end==value.length()) 
        return 0;
      
      // continue from last end
      start = end;
      
      // assume taking all
      end = value.length();
      
      // skip leading whitespace
      while (true) {
        if (!Character.isWhitespace(value.charAt(start))) break;
        start++;
        if (start==end) 
          return 0;
      }
      
      // take all up to next CR
      // 20030604 value.indexOf() used here previously is 1.4
      for (int i=start;i<end;i++) {
        if (value.charAt(i)=='\n') {
          end = i;
          next = "CONT";
          break;
        }
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
      return end>start ? 1 : 0;
    }
    
  } //MLLine

} //PropertyMultilineValue
