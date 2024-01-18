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
public class PropertyMultilineValue extends Property implements MultiLineProperty {
  
  /** the tag */
  private String tag;
  
  /** our value */
  private String lines = "";
  
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
   * Can contain '\n' etc. since this is a mutli-line text
   * @see genj.gedcom.Property#setValue(java.lang.String)
   */
  public void setValue(String setValue) {
    modNotify();
    lines = setValue;
  }

  /**
   * Accessor Value
   */
  public String getValue() {
    return getFirstLine();
  }
  
  /**
   * Helper to resolve the first line of a multiline value
   */
  public String getFirstLine() { 

    // More than one line ?
    int pos = lines.indexOf("\n");
    if (pos>=0) 
      return lines.substring(0,pos)+"...";
      
    // Longer than 255?
    if (lines.length()>255)
      return lines.substring(0,252)+"...";

    // Value
    return lines;
  }
  
  /**
   * @see genj.gedcom.MultiLineSupport#getLinesValue()
   */
  public String getLinesValue() {
    return lines.toString();
  }

  
  /**
   * @see genj.gedcom.MultiLineSupport#getLines()
   */
  public Iterator getLineIterator() {
    return new ConcContIterator(getTag(), lines);
  }

  /**
   * @see genj.gedcom.MultiLineSupport#getContinuation()
   */
  public Collector getLineCollector() {
    return new ConcContCollector();
  }
  
  /**
   * An iterator for lines
   */
  private static class ConcContIterator implements Iterator {
    
    /** the tag */
    private String first, current, next;
    
    /** the value */
    private String value;
    
    /** the current segment */
    private int start,end;
    
    /**
     * Constructor
     */
    /*package*/ ConcContIterator(String top, String initValue) {
      first = top;
      setValue(initValue);
    }
    
    /**
     * @see genj.gedcom.MultiLineProperty.Iterator#setValue(java.lang.String)
     */
    public void setValue(String setValue) {

      value = setValue;

      current = first;       
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
      return current;
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
      
      // nothing more there?
      if (end==value.length()) 
        return false;

      // continue from last end
      start = end;
      
      // calc current tag      
      current = start==0?first:next;
      
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
  private class ConcContCollector implements Collector {
    
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
     * @see genj.gedcom.MultiLineProperty.Collector#getValue()
     */
    public String getValue() {
      return buffer.toString();
    }

  } //LineWriter

} //PropertyMultilineValue
