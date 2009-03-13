/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2002-2004 Nils Meier
 * 
 * GraphJ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GraphJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphJ; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package gj.layout.graph.radial;

import gj.model.Edge;
import gj.model.Vertex;

import java.util.HashMap;
import java.util.Map;

/**
 * Graph attribute for radial layout
 */
/*package*/ class GraphAttributes {

  private Map<Edge, Integer> edge2length = new HashMap<Edge, Integer>();
  private Vertex root;
  
  /*package*/ int getLength(Edge edge) {
    Integer result = edge2length.get(edge);
    if (result!=null)
      return result.intValue();
    return 1;
  }
  
  /*package*/ Vertex getRoot() {
    return root;
  }
  
  /*package*/ void setRoot(Vertex vertex) {
    root = vertex;
  }

  /*package*/ void setLength(Edge edge, int length) {
    if (length < 1)
      edge2length.remove(edge);
    else
      edge2length.put(edge, new Integer(length));
  }
}