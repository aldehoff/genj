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


import java.awt.*;
import java.io.*;
import java.net.*;

import genj.util.*;

/**
 * Gedcom Property : FILE
 */
public class PropertyFile extends Property {

  /** the file-name */
  private String  file;

  /** whether we have an image yet */
  private boolean isIconChecked = false;

  /** whether file-name is relative or absolute */
  private boolean isRelativeChecked = false;

  /** the image */
  private ImgIcon valueAsIcon   = null;

  /**
   * Constructor of FILE Gedcom-line
   */
  public PropertyFile() {
  }

  /**
   * Constructor of FILE Gedcom-line
   */
  public PropertyFile(String file) {
    this.file=file;
  }

  /**
   * Constructor of FILE Gedcom-line
   */
  public PropertyFile(String tag, String value) {
    setValue(value);
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   */
  public String getProxy() {
    return "File";
  }

  /**
   * Returns the name of the proxy-object which knows properties looked
   * up by TagPath
   * @return proxy's logical name
   */
  public static String getProxy(TagPath path) {
    return "File";
  }

  /**
   * Returns the tag of this property
   */
  public String getTag() {
    return "FILE";
  }

  /**
   * Returns this property's value
   */
  public String getValue() {

    if (file==null)
      return "";

    if (!isRelativeChecked) {
      String relative = getGedcom().getOrigin().calcRelativeLocation(file);
      if (relative !=null)
        file = relative;
      isRelativeChecked = true;
    }
    return file;
  }

  /**
   * Tries to return the data of the referenced file as an icon
   */
  public synchronized ImgIcon getValueAsIcon() {

    // Already calculated?
    if (isIconChecked) {
      return valueAsIcon;
    }
    isIconChecked = true;
    valueAsIcon   = null;

    // Check File for Image ?
    if ((file==null)||(file.trim().length()==0)) {
      return null;
    }

    // Open InputStream
    try {
      valueAsIcon = new ImgIcon(getInputStream());
    } catch (IOException e) {
    }

    // Done
    return valueAsIcon;
  }

  /**
   * Sets this property's value
   */
  public boolean setValue(File f) {
    return setValue("file:"+f);
  }

  /**
   * Sets this property's value
   */
  public boolean setValue(String value) {

    // Remember the change
    noteModifiedProperty();

    // Remember the value
    file=value.replace('\\','/');

    // Reinit our icon calculation
    isIconChecked = false;
    isRelativeChecked = false;
    return true;
  }

  /**
   * Accessor File's InputStream
   */
  public InputStream getInputStream() throws IOException {
    return getGedcom().getOrigin().openFile(file).getInputStream();
  }
}
