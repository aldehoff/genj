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
package gj.layout.hierarchical;

import gj.layout.Layout2D;
import gj.model.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * an ordered layer of nodes
 */
/*package*/ class Layer {
  
  private Layout2D layout;
  private List<Assignment> assignments = new ArrayList<Assignment>();

  /*package*/Layer(Layout2D layout) {
    this.layout = layout;
  }
  
  /**
   * Add a vertex to layer at given position
   */
  /*package*/ void add(Assignment assignment) {
    
    int pos = 0;
    while (pos<assignments.size()){
      if (layout.getPositionOfVertex(assignment.vertex).getX() < layout.getPositionOfVertex(assignments.get(pos).vertex).getX()) 
        break;
      pos ++;
    }
    
    assignment.position = pos;
    assignments.add(pos, assignment);
    
    while (++pos<assignments.size())
      assignments.get(pos).position++;
    
  }
  
  /*package*/ void swap(int u, int v) {
    Assignment vu = assignments.get(u);
    vu.position = v;
    
    Assignment vv = assignments.get(v);
    vv.position = u;
    
    assignments.set(u, vv);
    assignments.set(v, vu);
    
  }
  
  /*package*/ int size() {
    return assignments.size();
  }
  
  /*package*/ Assignment get(int pos) {
    return assignments.get(pos);
  }
  
  /*package*/ Vertex getVertex(int pos) {
    return get(pos).vertex;
  }

  @Override
  public String toString() {
    return assignments.toString();
  }
  
  /**
   * A vertex assigned to a layer
   */
  /*package*/ static class Assignment {

    private int layer = -1;
    private Vertex vertex;
    private int position = -1;
    private List<Assignment> adjacents = new ArrayList<Assignment>();
    
    /*package*/ Assignment(Vertex vertex, int layer) {
      this.vertex = vertex;
      this.layer = layer;
    }
    
    /*package*/ void add(Assignment adjacent) {
      this.adjacents.add(adjacent);
    }
    
    /*package*/ boolean push(int layer) {
      if (this.layer>=layer)
        return false;
      this.layer = layer;
      return true;
    }
    
    /*package*/ List<Assignment> adjacents() {
      return adjacents;
    }

    /*package*/ int pos() {
      return position;
    }
    
    /*package*/ int layer() {
      return layer;
    }
   
    @Override
    public String toString() {
      return vertex.toString();
    }
  } // Assignment
  
}
