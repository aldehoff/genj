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

  /** the raw bytes */
  private byte[]  raw;

  /** the base64 string */
  private StringBuffer base64;

  /** transformed to image */
  private ImageIcon valueAsIcon;

  /** whether was checked for image */
  private boolean isIconChecked;

  /**
   * Returns the data of this Blob
   */
  public byte[] getBlobData() {

    // Already present ?
    if (raw!=null) 
      return raw;

    // No Base64 present ?
    if (base64==null)
      return null;

    // Decode Base64
    try {
      raw = Base64.decode(base64);
      base64 = null; 
    } catch (IllegalArgumentException e) {
      return null;
    }

    return raw;
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
    if (raw!=null)
      return raw.length+" Raw Bytes";

    // Base64-Data existing ?
    if (base64!=null)
      return base64.length()+" Base64 Bytes";

    // None
    return "Empty";

  }

  /**
   * Tries to return the data as an Icon
   */
  public synchronized ImageIcon getValueAsIcon() {

    // Already calculated?
    if (isIconChecked) {
      return valueAsIcon;
    }
    isIconChecked = true;
    valueAsIcon   = null;

    // Data for Image ?
    byte[] bs = getBlobData();
    if (bs==null)
      return null;

    // Try to create image
    try {
      valueAsIcon = new ImageIcon(getTitle(), bs);
    } catch (Throwable t) {
    }

    return valueAsIcon;
  }

  /**
   * Returns an Iterator which can be used to iterate through
   * several lines of this blob's value
   */
  public MultiLineSupport.Line getLines() {

    if (raw!=null) 
      return new Base64Iterator(Base64.encode(raw));

    if (base64!=null) 
      return new Base64Iterator(base64);

    return new Base64Iterator(new StringBuffer());
  }
  
  /**
   * @see genj.gedcom.MultiLineSupport#getLinesValue()
   */
  public String getLinesValue() {
    if (base64!=null) 
      return base64.toString();
    if (raw!=null)
      return Base64.encode(raw).toString();
    return EMPTY_STRING;
  }

  /**
   * Sets a property value line
   */
  public void setValue(String value) {

    // reset current data
    raw    = null;
    
    // collect value
    base64 = new StringBuffer(4*4096);
    base64.append(value.trim());
    
    // Successfull new information
    isIconChecked = false;
    valueAsIcon   = null;

    // Remember changed property
    modNotify();

    // Done
  }
  
  /**
   * @see genj.gedcom.MultiLineSupport#append(int, java.lang.String, java.lang.String)
   */
  public boolean append(int level, String taG, String vaLue) {
    // gotta be in base64 mode
    if (base64==null)
      return false;
    // only level 1 (direct children)
    if (level!=1)
      return false;
    // gotta be CONT 
    if (!"CONT".equals(taG))
      return false;
    // grab it
    base64.append(vaLue.trim());
    // accepted
    return true;
  }

  /**
   * Sets this property's value
   */
  public void load(String file, boolean updateSubs) {
    
    // Remember changed property
    modNotify();

    // Reset state
    isIconChecked = false;
    valueAsIcon   = null;
    base64        = null;
    raw           = null;
    
    // file?
    if (file.length()!=0) {
      // Try to open file
      try {
        InputStream in = getGedcom().getOrigin().open(file);
        raw = new ByteArray(in, in.available()).getBytes();
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
   * Member class for iterating through adress' lines of base64-encoded data
   */
  private static class Base64Iterator implements MultiLineSupport.Line {

    /** the base64 string */
    private StringBuffer base64;

    /** the offset in the string */
    private int offset;

    /** the break line value */
    private final int LINE = 72;

    /** Constructor */
    public Base64Iterator(StringBuffer base64) {
      this.base64 = base64;
      this.offset = 0;
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
    public int next() {
      if (offset+LINE>=base64.length()) 
        return 0;
      offset += LINE;
      return 1;
    }

  } //Base64Iterator

} //PropertyBlob
