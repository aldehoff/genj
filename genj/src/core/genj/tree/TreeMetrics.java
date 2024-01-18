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
    wIndis = Math.rint(windis  *100)/100;
    hIndis = Math.rint(hindis  *100)/100;
    wFams  = Math.rint(wfams   *100)/100;
    hFams  = Math.rint(hfams   *100)/100;
    wMarrs = Math.rint(wIndis/8*100)/100;
    hMarrs = Math.rint(hIndis/8*100)/100;
    pad    = Math.rint(padng   *100)/100;
    // done      
  }
  
  /**
   * Calculates the maximum value   */
  /*package*/ double calcMax() {
    double max = -Double.MAX_VALUE;
    if (wIndis>max) max=wIndis;
    if (hIndis>max) max=hIndis;
    if (wFams >max) max=wFams ;
    if (hFams >max) max=hFams ;
    if (pad   >max) max=pad   ;
    return max;
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    // check
    if (o==null||!(o instanceof TreeMetrics)) return false;
    // compare
    TreeMetrics other = (TreeMetrics)o;
    return 
      wIndis == other.wIndis&&
      hIndis == other.hIndis&&
      wFams  == other.wFams &&
      hFams  == other.hFams &&
      wMarrs == other.wMarrs&&
      hMarrs == other.hMarrs&&
      pad    == other.pad   ;
  }

  
} //TreeMetrics

