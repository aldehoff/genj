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
package genj.util;

import java.util.*;
import java.io.*;
import java.awt.*;

/**
 * Registry - improved java.util.Properties
 */
public class Registry {

  private String view;
  private Properties properties;
  private Registry parent;
  private boolean changed;

  private static Hashtable registries = new Hashtable();

  /**
   * Constructor for empty registry that can't be looked up
   * afterwards and won't be saved
   */
  public Registry() {
    view       ="";
    properties =new Properties();
  }

  /**
   * Constructor for registry loaded from InputStream
   * that can't be looked up and won't be saved
   * @param InputStream to load registry from
   */
  public Registry(InputStream in) {
    this();
    // Load settings
    try {
      properties.load(in);
    } catch (Exception ex) {
    }
  }

  /**
   * Constructor for registry loaded from local disk
   * @param InputStream to load registry from
   */
  public Registry(String name) {
    this(name, (Origin)null);
  }
  
  /**
   * Constructor for registry loaded relative to given Origin
   */
  public Registry(String name, Origin origin) {
    this();
    // read all relative to origin
    if (origin!=null) {
      try {
        Origin.Connection c = origin.openFile(name+".properties");
        InputStream in = c.getInputStream();
        properties.load(in);
        in.close();
      } catch (Throwable t) {
      }
    }
    // read all from local registry
    try {
      FileInputStream in = new FileInputStream(getFile(name));
      properties.load(in);
      in.close();
    } catch (Throwable t) {
    }
    // remember
    registries.put(name,this);
    // done
  }

  /**
   * Constructor for a view of a Registry
   * @param view the logical view as String
   */
  public Registry(Registry registry, String view) {

    // Make sure it's a valid name ?
    if ( (view==null) || ((view = view.trim()).length()==0) ) {
      throw new IllegalArgumentException("View can't be empty");
    }

    // Prepare data
    this.view       = view;
    this.parent     = registry;

    // Done
  }

  /**
   * Returns a registry for given logical name (might be null)
   */
  public static Registry lookup(String name) {
    return (Registry)registries.get(name);
  }
  
  /**
   * Returns array of ints by key
   */
  public int[] get(String key, int[] def) {

    // Get size of array
    int size = get(key,-1);
    if (size==-1)
      return def;

    // Gather array
    int result[] = new int[size];
    for (int i=0;i<size;i++) {
      result[i] = get(key+"."+(i+1),-1);
    }

    // Done
    return result;
  }

  /**
   * Returns array of Rectangles by key
   */
  public Rectangle[] get(String key, Rectangle[] def) {

    // Get size of array
    int size = get(key,-1);
    if (size==-1)
      return def;

    // Gather array
    Rectangle[] result = new Rectangle[size];
    Rectangle empty = new Rectangle(-1,-1,-1,-1);

    for (int i=0;i<size;i++) {
      result[i] = get(key+"."+(i+1),empty);
    }

    // Done
    return result;
  }

  /**
   * Returns array of strings by key
   */
  public String[] get(String key, String[] def) {

    // Get size of array
    int size = get(key,-1);
    if (size==-1)
      return def;

    // Gather array
    String result[] = new String[size];
    for (int i=0;i<size;i++) {
      result[i] = get(key+"."+(i+1),"");
    }

    // Done
    return result;
  }

  /**
   * Returns float parameter to key
   */
  public float get(String key, float def) {

    // Get property by key
    String result = get(key,(String)null);

    // .. existing ?
    if (result==null)
      return def;

    // .. number ?
    try {
      return Float.valueOf(result.trim()).floatValue();
    } catch (NumberFormatException ex) {
    }

    return def;
  }

  /**
   * Returns integer parameter to key
   */
  public int get(String key, int def) {

    // Get property by key
    String result = get(key,(String)null);

    // .. existing ?
    if (result==null)
      return def;

    // .. number ?
    try {
      return Integer.parseInt(result.trim());
    } catch (NumberFormatException ex) {
    }

    return def;
  }

  /**
   * Returns dimension parameter by key
   */
  public Dimension get(String key, Dimension def) {

    // Get box dimension
    int w = get(key+".w", -1);
    int h = get(key+".h", -1);

    // Missing ?
    if ( (w==-1) || (h==-1) )
      return def;

    // Done
    return new Dimension(w,h);
  }

  /**
   * Returns font parameter by key
   */
  public Font get(String key, Font def) {

    String face = get(key+".name" ,(String)null);
    int style   = get(key+".style",-1);
    int size    = get(key+".size" ,-1);

    if ( (face==null)||(style==-1)||(size==-1) )
      return def;

    return new Font(face,style,size);
  }

  /**
   * Returns point parameter by key
   */
  public Point get(String key, Point def) {

    // Get box dimension
    int x = get(key+".x", -1);
    int y = get(key+".y", -1);

    // Missing ?
    if ( (x==-1) || (y==-1) )
      return def;

    // Done
    return new Point(x,y);
  }

  /**
   * Returns rectangle parameter by key
   */
  public Rectangle get(String key, Rectangle def) {

    // Get box dimension
    int x = get(key+".x", Integer.MAX_VALUE);
    int y = get(key+".y", Integer.MAX_VALUE);
    int w = get(key+".w", Integer.MAX_VALUE);
    int h = get(key+".h", Integer.MAX_VALUE);

    // Missing ?
    if ( (x==Integer.MAX_VALUE) || (y==Integer.MAX_VALUE) || (w==Integer.MAX_VALUE) || (h==Integer.MAX_VALUE) )
      return def;

    // Done
    return new Rectangle(x,y,w,h);
  }

  /**
   * Returns String parameter to key
   */
  public String get(String key, String def) {

    // Get property by key
    String result;
    if (parent==null) {
      result = properties.getProperty(key);
    } else
      result = parent.get(view+"."+key,def);

    // .. existing ?
    if (result==null)
      return def;

    // .. information ?
    result = result.trim();
    if (result.length()==0)
      return def;

    // Done
    return result;
  }

  /**
   * Returns vector of strings by key
   */
  public Vector get(String key, Vector def) {

    // Get size of array
    int size = get(key,-1);
    if (size==-1)
      return def;

    // Gather array
    Vector result = new Vector(size);
    for (int i=0;i<size;i++) {
      result.addElement(get(key+"."+(i+1),""));
    }

    // Done
    return result;
  }

  /**
   * Returns boolean parameter by key
   */
  public boolean get(String key, boolean def) {

    // Get property by key
    String result = get(key,(String)null);

    // .. existing ?
    if (result==null)
      return def;

    // boolean value
    if (result.equals("1"))
      return true;
    if (result.equals("0"))
      return false;

    // Done
    return def;
  }

  /**
   * Returns this registry's view
   */
  public String getView() {

    // Base of registry ?
    if (parent==null)
      return "";

    // View of registry !
    String s = parent.getView();
    return (s.length()==0 ? "" : s+".")+view;
  }

  /**
   * Returns this registry's view's last part
   */
  public String getViewSuffix() {

    String v = getView();

    int pos = v.lastIndexOf('.');
    if (pos==-1)
      return v;

    return v.substring(pos+1);
  }

  /**
   * Lists all properties
   */
  public void list(PrintStream out) {
    properties.list(out);
  }

  /**
   * Remembers an array of ints
   */
  public void put(String key, int[] value) {

    // Remember
    int l = value.length;
    put(key,l);

    for (int i=0;i<l;i++)
      put(key+"."+(i+1),""+value[i]);

    // Done
  }

  /**
   * Remembers an array of Rectangles
   */
  public void put(String key, Rectangle[] value) {

    // Remember
    int l = value.length;

    put(key,""+l);

    for (int i=0;i<l;i++)
      put(key+"."+(i+1),value[i]);

    // Done
  }

  /**
   * Remembers an array of Strings(Objects)
   */
  public void put(String key, Object value[]) {
    put(key,value,value.length);
  }

  /**
   * Remembers an array of Strings
   */
  public void put(String key, Object value[], int length) {

    // Remember
    int l = Math.min(value.length,length);

    put(key,""+l);

    for (int i=0;i<l;i++) {
      put(key+"."+(i+1),value[i].toString());
    }

    // Done
  }

  /**
   * Remembers an float value
   */
  public void put(String key, float value) {
    put(key,""+value);
  }

  /**
   * Remembers an int value
   */
  public void put(String key, int value) {
    put(key,""+value);
  }

  /**
   * Remembers a point value
   */
  public void put(String key, Dimension value) {

    // Remember box dimension
    put(key+".w",value.width);
    put(key+".h",value.height);

    // Done
  }

  /**
   * Remembers a font value
   */
  public void put(String key, Font value) {

    // Remember box dimension
    put(key+".name" ,value.getName() );
    put(key+".style",value.getStyle());
    put(key+".size" ,value.getSize() );

    // Done
  }

  /**
   * Remembers a point value
   */
  public void put(String key, Point value) {

    // Remember box dimension
    put(key+".x",value.x);
    put(key+".y",value.y);

    // Done
  }

  /**
   * Remembers a rectangle value
   */
  public void put(String key, Rectangle value) {

    // Remember box dimension
    put(key+".x",value.x);
    put(key+".y",value.y);
    put(key+".w",value.width);
    put(key+".h",value.height);

    // Done
  }

  /**
   * Remembers a String value
   */
  public void put(String key, String value) {

    if (parent==null) {
      String old = properties.getProperty(key);
      if ( (old==null) || (!old.equals(value)) ) {
        changed=true;
        properties.put(key,value);
      }
    } else {
      parent.put(view+"."+key,value);
    }
  }

  /**
   * Remembers a vector of Strings
   */
  public void put(String key, Vector value) {

    // Remember
    int l = value.size();
    put(key,l);
    for (int i=0;i<l;i++) {
      put(key+"."+(i+1),value.elementAt(i).toString());
    }

    // Done
  }

  /**
   * Remembers a boolean value
   */
  public void put(String key, boolean value) {

    // Remember
    put(key,(value?"1":"0"));

    // Done
  }
  
  /**
   * Calculates a filename for given registry name
   */
  private static File getFile(String name) {
    String dir = EnvironmentChecker.getProperty(
      Registry.class,
      new String[]{ "user.home" },
      ".",
      "calculate dir for registry file"
    );
    return new File(dir,name+".properties");
  }

  /**
   * Save registries
   */
  public static void saveToDisk() {

    // Go through registries
    Enumeration keys = registries.keys();
    while (keys.hasMoreElements()) {

      // Get Registry
      String key = keys.nextElement().toString();
      Registry registry = (Registry)registries.get(key);

      // Open known file
      try {
        FileOutputStream out = new FileOutputStream(getFile(key));
        registry.properties.save(out,key);
        out.flush();
        out.close();
      } catch (IOException ex) {
        Debug.log(Debug.ERROR, Registry.class,"Couldn't save registry "+key,ex);
      }

    }

    // Done
  }
}
