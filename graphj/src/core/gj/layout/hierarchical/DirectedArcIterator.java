/**
 * GraphJ
 * 
 * Copyright (C) 2002 Nils Meier
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package gj.layout.hierarchical;

import gj.model.Node;
import gj.util.ArcIterator;

/**
 * Our own ArcIterator that differs incoming from outgoing arc
 */
/*package*/ class DirectedArcIterator extends ArcIterator {

  private Node node;    
  private boolean incoming;
  
  /**
   * Constructor
   */
  protected DirectedArcIterator(Node node, boolean incoming) {
    super(node);
    this.node = node;
    this.incoming = incoming;
  }    
  
  /**
   * @see gj.util.ArcIterator#next()
   */
  public boolean next() {
    // try to get a next that fits
    boolean isBadDirection;
    
    do {
      if (!super.next()) return false;
      isBadDirection = incoming^(arc.getEnd()==node);
    } while (isLoop||isBadDirection);
    
    // done
    return true;
  }

}
