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

import genj.util.EnvironmentChecker;
import genj.util.Origin;
import genj.util.swing.ImageIcon;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Gedcom Property : FILE
 */
public class PropertyFile extends Property implements IconValueAvailable {
  
  /** maximum we load of image */
  private final static int DEF_MAX_LOAD = 128*1024;  
  private static int max_load = -1;
  
  /** the file-name */
  private String  file;

  /** whether we have an image yet */
  private boolean isIconChecked = false;

  /** whether file-name is relative or absolute */
  private boolean isRelativeChecked = false;

  /** the image */
  private ImageIcon valueAsIcon   = null;

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
  public synchronized ImageIcon getValueAsIcon() {

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
      // try to create an image if smaller than max load
      Origin.Connection c = getGedcom().getOrigin().openFile(file);
      if (c.getLength()<getMaxValueAsIconSize()) { 
        valueAsIcon = new ImageIcon(c.getInputStream());
        // 20021205 for tiffs we get an image with size (-1,-1);
        if (valueAsIcon!=null&&(valueAsIcon.getIconWidth()<=0||valueAsIcon.getIconHeight()<=0))
          valueAsIcon = null;
      }
    } catch (Throwable t) {
    }

    // Done
    return valueAsIcon;
  }

  /**
   * Sets this property's value
   */
  public void setValue(File f) {
    setValue(f.toString());
  }

  /**
   * Sets this property's value
   */
  public void setValue(String value) {

    // Remember the change
    noteModifiedProperty();

    // Remember the value
    file=value.replace('\\','/');

    // Reinit our icon calculation
    isIconChecked = false;
    isRelativeChecked = false;

  }

  /**
   * Accessor File's InputStream
   */
  public InputStream getInputStream() throws IOException {
    return getGedcom().getOrigin().openFile(file).getInputStream();
  }
  
  /**
   * The files location (if externally accessible)    */
  public File getFile() {
    File result = getGedcom().getOrigin().calcAbsoluteLocation(file);
    if (!result.exists()||!result.isFile()) return null;
    return result;
  }
  
  /**
   * Resolve the maximum load   */
  public static int getMaxValueAsIconSize() {
    // already known?
    if (max_load>0) return max_load;
    // resolve
    max_load = DEF_MAX_LOAD;
    try {
      int i = Integer.parseInt(EnvironmentChecker.getProperty(PropertyFile.class, "genj.file.max", ""+max_load, "Maximum PropertyFile size to load"));
      if (i>0) max_load = i;
    } catch (Throwable t) {
    }
    // done
    return max_load;
  }
  
} //PropertyFile
