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

import java.util.*;
import java.awt.*;
import java.io.*;

import genj.util.*;

/**
 * Gedcom Property : BLOB
 */
public class PropertyBlob extends Property {

  /** the raw bytes */
  private byte[]  raw;

  /** the base64 string */
  private String  base64;

  /** transformed to image */
  private ImgIcon valueAsIcon;

  /** whether was checked for image */
  private boolean isIconChecked;

  /**
   * Member class for iterating through adress' lines of base64-encoded data
   */
  private class Base64LineIterator implements Property.LineIterator {

    /** the base64 string */
    private String base64;

    /** the offset in the string */
    private int offset;

    /** the break line value */
    private final int LINE = 72;

    /** Constructor */
    public Base64LineIterator(String base64) {
      this.base64 = base64;
      this.offset = 0;
    }

    /** whether this iterator has more lines */
    public boolean hasMoreValues() {
      return (offset < base64.length());
    }

    /** Returns the next line of this iterator */
    public String getNextValue() throws NoSuchElementException {

      String result;
      try {
        result = base64.substring( offset, Math.min(offset+LINE,base64.length()) );
      } catch (StringIndexOutOfBoundsException e) {
        throw new NoSuchElementException();
      }
      offset+=LINE;
      return result;
    }

    // EOC
  }

  /**
   * Constructor of Blob Gedcom-line
   */
  public PropertyBlob() {
    this("");
  }

  /**
   * Constructor of Blob Gedcom-line
   * @param in input to read data from
   */
  public PropertyBlob(File file) throws GedcomException {
    setValue(file);
  }

  /**
   * Constructor of Blob Gedcom-line
   */
  public PropertyBlob(String value) {
  }

  /**
   * Constructor of Blob Gedcom-line
   */
  public PropertyBlob(String tag, String value) {
  }

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
   * Returns a LineIterator which can be used to iterate through
   * several lines of this address
   */
  public LineIterator getLineIterator() {

    if (raw!=null) {
      String b64 = Base64.encode(raw);
      return new Base64LineIterator(b64);
    }
    if (base64!=null) {
      return new Base64LineIterator(base64);
    }

    return new Base64LineIterator("");
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   */
  public String getProxy() {
    return "Blob";
  }

  /**
   * Returns the name of the proxy-object which knows properties looked
   * up by TagPath
   * @return proxy's logical name
   */
  public static String getProxy(TagPath path) {
    return "Blob";
  }
  /**
   * Returns the tag of this property
   */
  public String getTag() {
    return "BLOB";
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
  public synchronized ImgIcon getValueAsIcon() {

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
    Image img = Toolkit.getDefaultToolkit().createImage(bs);
    if (img!=null) {
      valueAsIcon = new ImgIcon(img);
    }

    return valueAsIcon;
  }

  /**
   * This property incorporates several lines as block with no newline
   */
  public int isMultiLine() {
    return MULTI_BLOCK;
  }

  /**
   * Sets value to be taken from file
   */
  public void setValue(File file) throws GedcomException {

    // Try to open file
    FileInputStream in;
    try {
      in = new FileInputStream(file);
    } catch (FileNotFoundException ex) {
      throw new GedcomException("Couldn't open file "+file);
    }

    // Reasonable expectedSize ?
    int len = (int)file.length();

    // Read
    byte buffer[] = new byte[len];
    try {
      len = in.read(buffer);
    } catch (IOException e) {
      throw new GedcomException("Error while reading file "+file);
    } finally {
      try { in.close(); } catch (Exception ie) {};
    }

    // .. nothing read ?
    if (len!=buffer.length)
      throw new GedcomException("Couldn't read all "+buffer.length+" bytes of file "+file);

    // Successfull new information
    isIconChecked=false;
    valueAsIcon   =null;
    base64        =null;
    raw           =buffer;

    // Remember changed property
    noteModifiedProperty();

    // Done
  }

  /**
   * Sets a property value line
   */
  public boolean setValue(String value) {

    // Remember value
    raw    =null;
    base64 =value.trim();
    if (base64.length()==0)
      base64 = null;

    // Successfull new information
    isIconChecked=false;
    valueAsIcon   =null;

    // Remember changed property
    noteModifiedProperty();

    // Done
    return true;
  }          
}
