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
  private StringBuffer lines = new StringBuffer(80);
  
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
    lines.setLength(0);
    lines.append(set);
  }

  /**
   * Accessor Value
   */
  public String getValue() {
    return getFirstLine(lines);
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
  public String getAllLines() {
    return lines.toString();
  }

  
  /**
   * @see genj.gedcom.MultiLineSupport#getLines()
   */
  public Lines getLines() {
    return new MyLines(getTag(), lines);
  }

  /**
   * @see genj.gedcom.MultiLineSupport#getContinuation()
   */
  public Continuation getContinuation() {
    return new MyContinuation();
  }
  
  /**
   * An iterator for lines
   */
  private static class MyLines implements Lines {
    
    /** the tag */
    private String tag, next;
    
    /** the value */
    private StringBuffer value;
    
    /** the current segment */
    private int start,end;
    
    /**
     * Constructor
     */
    /*package*/ MyLines(String top, StringBuffer linesValue) {
      
      tag = top;
      next = top;
      value = linesValue;
       
      start = 0;
      end = 0;
      next();
    }
    
    /**
     * @see genj.gedcom.MultiLineSupport.LineIterator#getIndent()
     */
    public int getIndent() {
      return start==0 ? 0 : 1;
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
      if (end==value.length()) 
        return false;
      
      // continue from last end
      start = end;
      
      // assume taking all
      end = value.length();
      
      // skip leading whitespace
      while (true) {
        if (!Character.isWhitespace(value.charAt(start))) break;
        start++;
        if (start==end) 
          return false;
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
      return end>start;
    }
    
  } //LineReader
  
  /**
   * An iterator for lines
   */
  private class MyContinuation implements Continuation {
    
    /** running collection */
    private StringBuffer buffer = new StringBuffer(lines.toString());
    
    /**
     * append some
     */
    public boolean append(int indent, String tag, String value) {
      
      // only level 1 (direct children)
      if (indent!=1)
        return false;
        
      // gotta be CONT or CONC
      boolean 
        isCont = "CONT".equals(tag),
        isConc = "CONC".equals(tag);
      if (!(isConc||isCont))
        return false;
        
      // grab it
      if (isCont) 
        buffer.append('\n');
        
      buffer.append(value);
      
      // accepted
      return true;
    }
    
    /**
     * @see genj.gedcom.MultiLineSupport.Writer#commit()
     */
    public void commit() {
      setValue(buffer.toString());
    }

  } //LineWriter

} //PropertyMultilineValue
