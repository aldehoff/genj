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
package genj.tree;

/**
 * Metrics a Tree's model is based on 
 */  
public class TreeMetrics {
    
  /*package*/ double
    wIndis, hIndis,
    wFams, hFams,
    wMarrs, hMarrs,
    pad;
    
  /**
   * Constructor
   */
  public TreeMetrics(double windis, double hindis, double wfams, double hfams, double padng) {
    // remember
    wIndis = windis;
    hIndis = hindis;
    wFams  = wfams;
    hFams  = hfams;
    wMarrs = Math.rint(wIndis/8*100)/100;
    hMarrs = Math.rint(hIndis/8*100)/100;
    pad    = padng;
    // done      

  }
  
  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return super.toString() + "(" + 
      wIndis + ", " +
      hIndis + ", " +
      wFams  + ", " +
      hFams  + ", " +
      wMarrs + ", " +
      hMarrs + ", " +
      pad    + ")\n";
    
  }

  
} //TreeMetrics

