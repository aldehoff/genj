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

import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

/**
 * Gedcom Property : BLOB
 */
public class PropertyBlob extends Property implements MultiLineSupport, IconValueAvailable {
  
  private final static String BLOB = "BLOB";

  /** the raw bytes */
  private byte[]  raw;

  /** the base64 string */
  private String  base64;

  /** transformed to image */
  private ImageIcon valueAsIcon;

  /** whether was checked for image */
  private boolean isIconChecked;

  /**
   * Returns the data of this Blob
   */
  public byte[] getBlobData() {

    // Already present ?
    if (raw!=null) {
      return raw;
    }

    // No Base64 present ?
    if (base64==null) {
      return null;
    }

    // Decode Base64
    try {
      raw = Base64.decode(base64);
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
    return BLOB;
  }
  
  /**
   * @see genj.gedcom.Property#setTag(java.lang.String)
   */
  /*package*/ void setTag(String tag) throws GedcomException {
    assert(BLOB.equals(tag), UNSUPPORTED_TAG);
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

    if (raw!=null) {
      String b64 = Base64.encode(raw);
      return new Base64Iterator(b64);
    }
    if (base64!=null) {
      return new Base64Iterator(base64);
    }

    return new Base64Iterator(EMPTY_STRING);
  }
  
  /**
   * @see genj.gedcom.MultiLineSupport#getLinesValue()
   */
  public String getLinesValue() {
    if (base64==null) {
      return Base64.encode(raw);
    }
    return base64;
  }

  /**
   * Sets value to be taken from file
   */
  public PropertyBlob load(String file) throws GedcomException {
    
    // Reset state
    isIconChecked = false;
    valueAsIcon   = null;
    base64        = null;
    raw           = null;

    // Try to open file
    try {
      InputStream in = getGedcom().getOrigin().openFile(file).getInputStream();
      raw = new ByteArray(in, (int)file.length()).getBytes();
      in.close();
    } catch (IOException ex) {
      throw new GedcomException("Error reading "+file);
    }

    // check if we can update the TITL/FORM in parent OBJE
    Media.updateSubs(getParent(), file);

    // Remember changed property
    noteModifiedProperty();

    // Done
    return this;
  }

  /**
   * Sets a property value line
   */
  public void setValue(String value) {

    // reset current data
    raw    = null;
    base64 = null;
    
    // collect value
    StringBuffer buf = new StringBuffer();
    StringTokenizer lines = new StringTokenizer(value);
    while (lines.hasMoreTokens()) {
      buf.append(lines.nextToken());
    }
    if (buf.length()>0) 
      base64 = buf.toString();

    // Successfull new information
    isIconChecked = false;
    valueAsIcon   = null;

    // Remember changed property
    noteModifiedProperty();

    // Done
  }          

  /**
   * Member class for iterating through adress' lines of base64-encoded data
   */
  private class Base64Iterator implements MultiLineSupport.Line {

    /** the base64 string */
    private String base64;

    /** the offset in the string */
    private int offset;

    /** the break line value */
    private final int LINE = 72;

    /** Constructor */
    public Base64Iterator(String base64) {
      this.base64 = base64;
      this.offset = 0;
    }
    
    /** current tag */
    public String getTag() {
      return offset==0 ? PropertyBlob.this.getTag() : "CONT";
    }

    /** Returns the next line of this iterator */
    public String getValue() {
      return base64.substring( offset, Math.min(offset+LINE,base64.length()) );
    }

    /** set to next */
    public boolean next() {
      if (offset>=base64.length()) return false;
      offset+=LINE;
      return true;
    }

  } //Base64Iterator

} //PropertyBlob
