package genj.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A color-attribute with background and foreground color
 */
public class ColorAttribute {

  /** attributes */
  protected Color fg;
  protected Color bg;
  protected String name;
  
  /** 
   * Constructor 
   */
  public ColorAttribute(String n, Color f, Color b) {
    name = n;
    fg = f;
    bg = b;
  }

  /** 
   * Accessor - name
   */
  public String getName() {
    return name;
  }
  
  /** 
   * Accessor - Foreground 
   */
  public Color getForeground() {
    return fg;
  }
  
  /** 
   * Accessor - Foreground 
   */
  public void setForeground(Color f) {
    fg=f;
  }
  
  /** 
   * Accessor - Background 
   */
  public Color getBackground() {
    return bg;
  }

  /** 
   * Accessor - Background 
   */
  public void setBackground(Color b) {
    bg=b;
  }

  /**
   * Group
   */
  public static class Group {
    /** keeping track of our colors */
    private List colors = new ArrayList(16);
    /** keeping track of our colors */
    private Map key2color = new HashMap(16);
    /**
     * Adds a color
     */
    public void add(String key, ColorAttribute ca) {
      colors.add(ca);
      key2color.put(key, ca);
    }
    /**
     * Resolves a color
     */
    public Color get(String key, boolean bg) {
      ColorAttribute ca = (ColorAttribute)key2color.get(key);
      return bg ? ca.getBackground() : ca.getForeground();
    }
    /**
     * Returns all colors
     */
    public List getAll() {
      return colors;
    }
    /**
     * Convenient factory
     */
    public void add(Registry registry, Resources resources, String key, Color deffg, Color defbg) {
      // localize name
      String name = resources==null ? key : resources.getString("color."+key);
      // create attr
      ColorAttribute ca = new ColorAttribute(
        name, 
        deffg!=null ? new Color(deffg.getRGB()) : null,
        defbg!=null ? new Color(defbg.getRGB()) : null
      );
      // remeber
      add(key,ca);
      // done
    }
  
  } //Group

} //ColorAttribute
