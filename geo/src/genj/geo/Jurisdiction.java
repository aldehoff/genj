/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2005 Nils Meier <nils@meiers.net>
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
package genj.geo;

import genj.util.Debug;
import genj.util.Resources;

import java.io.File;
import java.io.FileInputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * We support the concept of a top-level jurisdiction
 */
public class Jurisdiction {

  /** all known jurisdictions */
  private static List jurisdictions;
  
  /** state */
  private String code, displayName;
  
  /**
   * Constructor
   */
  private Jurisdiction(String code, String displayName) {
    
    // remember
    this.code = code.toLowerCase();
    this.displayName = displayName;
    
  }
  
  /**
   * Accessor
   */
  public String getDisplayName() {
    return displayName;
  }
  
  /**
   * Accessor
   */
  public String getCode() {
    return code;
  }
  
  /**
   * Equal if display name is same 
   */
  public boolean equals(Object obj) {
    Jurisdiction that = (Jurisdiction)obj;
    return this.displayName.equalsIgnoreCase(that.displayName);
  }
  
  /**
   * Access
   */
  public static Jurisdiction get(Collator collator, String displayName) {
    
    // initialized?
    if (jurisdictions==null) { 
      
      jurisdictions =  new ArrayList();
      
      // fill lookup of top level jurisdictions
      File[] files = GeoService.getInstance().getGeoFiles();
      for (int i = 0; i < files.length; i++) {
        // the right file either local or global?
        if (!files[i].getName().equals("jurisdictions.properties"))
          continue;
        // read it
        try {
          Resources meta = new Resources(new FileInputStream(files[i]));
          // look for all 'xx.yy = aaa,bbb,ccc'
          // where 
          //  xx = country
          //  yy = state code
          //  aaa,bbb,ccc = state names or abbreviations
          for (Iterator keys = meta.getKeys(); keys.hasNext(); ) {
            String key = keys.next().toString();
            if (key.length()!=5) continue;
            String code = key.substring(3,5);
            for (StringTokenizer names = new StringTokenizer(meta.getString(key), ","); names.hasMoreTokens(); )
              jurisdictions.add(new Jurisdiction(code, names.nextToken()));
          }
        } catch (Throwable t) {
          Debug.log(Debug.WARNING, Country.class, t);
        }

        // next
      }
      
      // initialized
    }

    // look it up
    for (int i = 0; i < jurisdictions.size(); i++) {
      Jurisdiction jurisdiction = (Jurisdiction)jurisdictions.get(i);
      if (collator.compare(jurisdiction.displayName, displayName)==0) 
        return new Jurisdiction(jurisdiction.code, displayName);
    }
    
    // not found
    return null;
  }

  
}
