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

import genj.util.Base64;
import genj.util.ByteArray;
import genj.util.swing.ImageIcon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Gedcom Property : BLOB
 */
public class PropertyBlob extends Property implements MultiLineSupport, IconValueAvailable {
  
  private final static String TAG = "BLOB";

  /** the content (either base64, raw bytes or an ImageIcon */
  private Object content = "";

  /** whether was checked for image */
  private boolean isIconChecked;

  /**
   * Returns the data of this Blob
   */
  public byte[] getBlobData() {

    // Already present ?
    if (content instanceof byte[])
      return (byte[])content;

    // A decoded image?
    if (content instanceof ImageIcon)
      return ((ImageIcon)content).getBytes();

    // Decode Base64
    try {
      content = Base64.decode(content.toString());
    } catch (IllegalArgumentException e) {
      content = "";
    }

    return (byte[])content;
  }
  
  /**
   * Title of Blob
   */
  public String getTitle() {
    Entity e = getEntity();
    return (e instanceof Media) ? ((Media)e).getTitle() : getTag();
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   */
  public String getProxy() {
    return "File";
  }

  /**
   * Returns the tag of this property
   */
  public String getTag() {
    return TAG;
  }
  
  /**
   * @see genj.gedcom.Property#setTag(java.lang.String)
   */
  /*package*/ Property init(String tag, String value) throws GedcomException {
    assume(TAG.equals(tag), UNSUPPORTED_TAG);
    return super.init(tag,value);
  }

  /**
   * Returns the property value line
   */
  public String getValue() {

    // Raw-Data existing ?
    if (content instanceof byte[])
      return ((byte[])content).length+" Raw Bytes";

    // Decoded image?
    if (content instanceof ImageIcon)
      return ((ImageIcon)content).getByteSize()+" Image Bytes";
      
    // gotta be base64 string
    return content.toString().length()+" Base64 Bytes";
  }

  /**
   * Tries to return the data as an Icon
   */
  public synchronized ImageIcon getValueAsIcon() {

    // Already image?
    if (content instanceof ImageIcon)
      return (ImageIcon)content;

    // Try to decode image only once
    if (isIconChecked)
      return null;
    isIconChecked = true;

    // Data for Image ?
    byte[] bs = getBlobData();
    if (bs==null)
      return null;

    // Try to create image
    ImageIcon result = null;
    try {
      result = new ImageIcon(getTitle(), bs);
      content = result;
    } catch (Throwable t) {
    }

    // done
    return result;
  }
  
  /**
   * @see genj.gedcom.MultiLineSupport#getContinuation()
   */
  public Continuation getContinuation() {
    return new MyContinuation();
  }

  /**
   * Returns an Iterator which can be used to iterate through
   * several lines of this blob's value
   */
  public MultiLineSupport.Lines getLines() {
    
    // raw?
    if (content instanceof byte[])
      return new Base64Lines(Base64.encode((byte[])content));
      
    // image?
    if (content instanceof ImageIcon)
      return new Base64Lines(Base64.encode(((ImageIcon)content).getBytes()));

    // string!
    return new Base64Lines(new StringBuffer(content.toString()));
  }
  
  /**
   * @see genj.gedcom.MultiLineSupport#getLinesValue()
   */
  public String getAllLines() {
    return getValue();
  }

  /**
   * Sets a property value line
   */
  public void setValue(String value) {

    // Successfull new information
    content = value;
    isIconChecked = false;

    // Remember changed property
    modNotify();

    // Done
  }
  
  /**
   * Sets this property's value
   */
  public void load(String file, boolean updateSubs) {
    
    // Remember changed property
    modNotify();

    // Reset state
    isIconChecked = false;
    content ="";
    
    // file?
    if (file.length()!=0) {
      // Try to open file
      try {
        InputStream in = getGedcom().getOrigin().open(file);
        content = new ByteArray(in, in.available()).getBytes();
        in.close();
      } catch (IOException ex) {
        return;
      }
    }
    
    // check
    Property media = getParent();
    if (!updateSubs||!(media instanceof PropertyMedia||media instanceof Media)) 
      return;
      
    // title?
    Property title = media.getProperty("TITL");
    if (title==null) 
      title = media.addProperty(new PropertySimpleValue());
    title.setValue(new File(file).getName());
      
    // format?
    Property format = media.getProperty("FORM");
    if (format==null)
      format = media.addProperty(new PropertySimpleValue()); 
    format.setValue(PropertyFile.getSuffix(file));
    
    // done  
  }

  /**
   * A continuation for gathering blob data
   *
   */
  private class MyContinuation implements MultiLineSupport.Continuation {
    
    /** current state */
    private StringBuffer buffer;
    
    /**
     * Constructor
     */ 
    private MyContinuation() {
      buffer = new StringBuffer(1024);
      if (content instanceof String) buffer.append(content);
    }
    
    /**
     * @see genj.gedcom.MultiLineSupport.Continuation#append(int, java.lang.String, java.lang.String)
     */
    public boolean append(int indent, String tag, String value) {
      
      // only level 1 (direct children)
      if (indent!=1)
        return false;
        
      // gotta be CONT 
      if (!"CONT".equals(tag))
        return false;
        
      // grab it
      buffer.append(value.trim());
      
      // accepted
      return true;
    }
    
    /**
     * @see genj.gedcom.MultiLineSupport.Continuation#commit()
     */
    public void commit() {
      setValue(buffer.toString());
    }

  } //MyContinuation

  /**
   * Member class for iterating through adress' lines of base64-encoded data
   */
  private static class Base64Lines implements MultiLineSupport.Lines {

    /** the base64 string */
    private StringBuffer base64;

    /** the offset in the string */
    private int offset;

    /** the break line value */
    private final int LINE = 72;

    /** Constructor */
    public Base64Lines(StringBuffer base64) {
      this.base64 = base64;
      this.offset = 0;
    }
    
    /**
     * @see genj.gedcom.MultiLineSupport.LineIterator#getIndent()
     */
    public int getIndent() {
      return offset==0?0:1;
    }
    
    /** current tag */
    public String getTag() {
      return offset==0 ? TAG : "CONT";
    }

    /** Returns the next line of this iterator */
    public String getValue() {
      return base64.substring( offset, Math.min(offset+LINE,base64.length()) );
    }

    /** set to next */
    public boolean next() {
      if (offset+LINE>=base64.length()) 
        return false;
      offset += LINE;
      return true;
    }

  } //Base64Iterator

} //PropertyBlob
