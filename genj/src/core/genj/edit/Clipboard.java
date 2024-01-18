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
package genj.edit;

import genj.gedcom.GedcomException;
import genj.gedcom.MetaProperty;
import genj.gedcom.MultiLineSupport;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;

import java.util.ArrayList;
import java.util.List;

/**
 * An app wide clipboard
 */
public class Clipboard {

  /** singleton instance */
  private static Clipboard instance;
  
  /** single copy we allow (guess could be more) */
  private Copy copy;
  
  /** singleton access */
  public static Clipboard getInstance() {
    if (instance==null)
      instance = new Clipboard();
    return instance;
  }
  
  /**
   * singleton constructor
   */
  private Clipboard() {
  }
  
  /**
   * whether there's something to paste
   */
  public boolean isEmpty() {
    return copy==null;
  }
  
  /**
   * copy
   */
  public void copy(Property what) {
    // can't be entity - has to have parent
    if (what.getParent()==null)
      throw new IllegalArgumentException("prop.getParent()==null");
    // can't be transient
    if (what.isTransient())
      throw new IllegalArgumentException("prop.isTransient()");
    // grab it
    copy = new Copy(what);
    // done
  }
  
  /**
   * paste
   */
  public Property paste(Property where) throws GedcomException {
    // check copy
    if (copy==null)
      return null;
    // apply copy
    return copy.paste(where);
  }

  /**
   * A Copy 
   */
  /*package*/ static class Copy {
    
    /** copied information */
    private String tag, value;
    
    /** sub */
    private List subs = new ArrayList();
    
    /**
     * Constructor
     */
    /*package*/  Copy(Property prop) {
      // remember
      tag = prop.getTag();
      if (prop instanceof MultiLineSupport)
        value = ((MultiLineSupport)prop).getLinesValue();
      else
        value = prop.getValue();
      // subs?
      Property[] children = prop.getProperties();
      for (int c=0; c<children.length; c++) {
        subs.add(new Copy(children[c]));
      }
      // done 
    }
    
    /**
     * paste
     */
    /*package*/ Property paste(Property target) throws GedcomException {
      return pasteRecursively(target, MetaProperty.get(target));
    }
    
    /**
     * paste recusively
     */
    private Property pasteRecursively(Property target, MetaProperty targetMeta) throws GedcomException {
      // add content to target
      MetaProperty meta = targetMeta.get(tag, false);
      Property prop = meta.create(value);
      target.addProperty(prop);
      // link and recurse
      try {
        // link?
        if (prop instanceof PropertyXRef) {
          ((PropertyXRef)prop).link();
        }
        // recurse into subs
        for (int s=0; s<subs.size(); s++) {
        	((Copy)subs.get(s)).pasteRecursively(prop, meta);
        }
      } catch (GedcomException e) {
        // rollback
        target.delProperty(prop);
        throw e;
      }
      // done
      return prop;
    }
    
  } //Copy

} //Clipboard