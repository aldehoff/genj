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
import genj.util.swing.ImageIcon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

/**
 * Gedcom Property : FILE
 */
public class PropertyFile extends Property implements IconValueAvailable {
  
  /** expected tag */
  private final static String TAG = "FILE";
  
  /** maximum we load of image */
  private final static int DEF_MAX_LOAD = 128*1024;  
  private static int max_load = -1;
  
  /** the file-name */
  private String  file;

  /** whether file-name is relative or absolute */
  private boolean isRelativeChecked = false;

  /** the image */
  private Object valueAsIcon = null;

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
  /*package*/ void setTag(String tag) throws GedcomException {
    assume(TAG.equals(tag), UNSUPPORTED_TAG);
  }

  /**
   * Returns this property's value
   */
  public String getValue() {

    if (file==null)
      return EMPTY_STRING;

    // we're checking the value for relative here because
    // in setValue() the parent might not be set yet so
    // getGedcom() wouldn't work there
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

    // ever loaded?
    if (valueAsIcon instanceof SoftReference) {
      
      // check reference
      ImageIcon result = (ImageIcon)((SoftReference)valueAsIcon).get();
      if (result!=null)
        return result;
     
      // reference was cut
      valueAsIcon = null;   
    }

    // never loaded or cut reference? 
    if (valueAsIcon==null) {
      
      // load it
      ImageIcon result = loadValueAsIcon();
      
      // remember
      if (result!=null)
        valueAsIcon = new SoftReference(result);
      else
        valueAsIcon = new Object(); // NULL

      // done    
      return result;
    }

    // checked and never loaded
    return null;
  }
  
  /**
   * Tries to Load the date of the referenced file 
   */
  private synchronized ImageIcon loadValueAsIcon() {

    ImageIcon result = null;

    // Check File for Image ?
    if (file!=null&&file.trim().length()>0) {

      // Open InputStream
      InputStream in = null;
      try {
        // try to create an image if smaller than max load
        in = getGedcom().getOrigin().open(file);
        if (in.available()<getMaxValueAsIconSize()) {
           
          result = new ImageIcon(file, in);
          
          // make sure the result makes sense
          if (result.getIconWidth()<=0||result.getIconHeight()<=0)
            result = null;
        }
      } catch (Throwable t) {
      } finally {
        if (in!=null) try { in.close(); } catch (IOException ioe) {};
      }
    }

    // done
    return result;
  }

  /**
   * Sets this property's value
   */
  public void setValue(String value) {

    // Remember the change
    noteModifiedProperty();

    // Remember the value
    file = value.replace('\\','/');
    isRelativeChecked = false;
    
    // Reinit our icon calculation
    valueAsIcon = null;

    // check if we can update the TITL/FORM in parent OBJE
    Media.updateSubs(getParent(), value);
    
    // done    
  }

  /**
   * Accessor File's InputStream
   */
  public InputStream getInputStream() throws IOException {
    return getGedcom().getOrigin().open(file);
  }
  
  /**
   * The files location (if externally accessible)    */
  public File getFile() {
    File result = getGedcom().getOrigin().getFile(file);
    if (result==null||!result.exists()||!result.isFile()) return null;
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

  /**
   * Calculate suffix of file (empty string if n/a)
   */
  public String getSuffix() {
    return getSuffix(file);
  }

  /**
   * Calculate suffix of file (empty string if n/a)
   */
  public static String getSuffix(String value) {
    // check for suffix
    String result = "";
    if (value!=null) {
      int i = value.lastIndexOf('.');
      if (i>=0) result = value.substring(i+1);
    }
    // done
    return result;
  }
  
} //PropertyFile
