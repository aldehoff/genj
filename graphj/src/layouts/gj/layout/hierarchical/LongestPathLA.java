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

import gj.layout.GraphNotSupportedException;
import gj.layout.Layout2D;
import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;
import gj.util.LayoutHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * A layering based on longest paths from sinks
 */
public class LongestPathLA implements LayerAssignment {
  
  /** layering algorithm */
  public List<Layer> assignLayers(Graph graph, Layout2D layout) throws GraphNotSupportedException {

    // prepare state
    Map<Vertex, Assignment> vertex2assignment = new HashMap<Vertex, Assignment>();
    List<LayerImpl> layers = new ArrayList<LayerImpl>();

    // find sinks
    for (Vertex v : graph.getVertices()) {
      if (LayoutHelper.isSink(v)) 
        sinkToSource(null, v, new Stack<Assignment>(), vertex2assignment, layers, layout);
    }
    
    // place vertices in resulting layers
    for (Vertex vertex : graph.getVertices()) {
      Assignment assignment = vertex2assignment.get(vertex);
      layers.get(assignment.layer).add(assignment);
    }
    
    // add dummy vertices
    dummyVertices(layers, layout);

    // done
    return new ArrayList<Layer>(layers);
  }
  
  /**
   * add dummy vertices were edges span multiple layers
   */
  private void dummyVertices(List<LayerImpl> layers, Layout2D layout) {

    // loop over layers and check incoming
    for (int i=0;i<layers.size()-1;i++) {
      LayerImpl layer = layers.get(i);
      
      for (Assignment assignment : layer.assignments) {
        
        for (int j=0;j<assignment.in.size();j++) {
          
          Connection arc = assignment.in.get(j);
          
          if (arc.from.layer != i+1) {
            
            // create a dummy
            Assignment dummy = new Assignment(Layer.DUMMY, i+1, layout.getPositionOfVertex(assignment.vertex).getX());
            layers.get(i+1).add(dummy);

            // delete old connection
            assignment.in.remove(j);
            arc.from.out.remove(arc);
            
            // rewire
            dummy.addOut(arc.edge, assignment);
            dummy.addIn(arc.edge, arc.from);

          }
        }
        
      }
      
    }

    // done
  }

  /**
   * walk from sink to source recursively and collect layer information plus incoming vertices
   */
  private void sinkToSource(Edge edge, Vertex vertex, Stack<Assignment> path, Map<Vertex, Assignment> vertex2assignment, List<LayerImpl> layers, Layout2D layout) throws GraphNotSupportedException{
    
    // check if we're back at a vertex we've seen in this iteration
    if (path.contains(vertex))
      throw new GraphNotSupportedException("graph has to be acyclic");
    
    // make sure we have enough layers
    if (layers.size()<path.size()+1)
      layers.add(new LayerImpl());
    
    // create or reuse an assignment
    Assignment assignment = vertex2assignment.get(vertex);
    if (assignment==null) {
      assignment = new Assignment(vertex, -1, layout.getPositionOfVertex(vertex).getX());
      vertex2assignment.put(vertex, assignment);
    }
    
    // add adjacent vertices (previous in path)
    if (edge!=null)
      assignment.addOut(edge, path.peek());
    
    // push to new layer and continue if node's layer has changed
    if (!assignment.push(path.size()))
      return;      

    // recurse into incoming edges direction of source
    path.push(assignment);
    for (Edge e : vertex.getEdges()) {
      if (e.getEnd().equals(vertex))
        sinkToSource(e, e.getStart(), path, vertex2assignment, layers, layout);
    }
    path.pop();
    
    // done
  }
   
  /**
   * an ordered layer of nodes
   */
  static private class LayerImpl implements Layer {
    
    private List<Assignment> assignments = new ArrayList<Assignment>();

    /**
     * Add a vertex to layer at given position
     */
    /*package*/ void add(Assignment assignment) {
      
      int pos = 0;
      while (pos<assignments.size()){
        if (assignment.originalx < assignments.get(pos).originalx) 
          break;
        pos ++;
      }

      add(assignment, pos);
    }
    
    /*package*/ void add(Assignment assignment, int pos) {
      
      assignment.position = pos;
      assignments.add(pos, assignment);
      
      while (++pos<assignments.size())
        assignments.get(pos).position++;
      
    }
    
    public void swap(int u, int v) {
      Assignment vu = assignments.get(u);
      vu.position = v;
      
      Assignment vv = assignments.get(v);
      vv.position = u;
      
      assignments.set(u, vv);
      assignments.set(v, vu);
      
    }
    
    public int[] getOutgoing(int u) {
      Assignment assignment = assignments.get(u);
      int[] result = new int[assignment.out.size()];
      for (int i=0;i<assignment.out.size();i++)
        result[i]= assignment.out.get(i).to.position;
      return result;
    }
    
    public int[] getIncoming(int u) {
      Assignment assignment = assignments.get(u);
      int[] result = new int[assignment.in.size()];
      for (int i=0;i<assignment.in.size();i++)
        result[i] = assignment.in.get(i).from.position;
      return result;
    }
    
    public int size() {
      return assignments.size();
    }
    
    public Vertex getVertex(int pos) {
      return assignments.get(pos).vertex;
    }
    
    @Override
    public String toString() {
      return assignments.toString();
    }
    
  } //LayerImpl
  
  private static class Connection {
    private Edge edge;
    private Assignment from, to;
    private Connection(Edge edge, Assignment from, Assignment to) {
      this.edge = edge;
      this.from = from;
      this.to = to;
    }
  } //Connection
  
  /**
   * A vertex assigned to a layer
   */
  private static class Assignment {

    private double originalx;
    private int layer = -1;
    private Vertex vertex;
    private int position = -1;
    private List<Connection> out = new ArrayList<Connection>();
    private List<Connection> in = new ArrayList<Connection>();
    
    /**
     * A new vertex/layer assignment 
     */
   /*package*/ Assignment(Vertex vertex, int layer, double originalx) {
      this.originalx = originalx;
      this.vertex = vertex;
      this.layer = layer;
    }
   
//    /**
//     * A dummy vertex/layer assignment between two given assignments
//     */
//    /*package*/ Node(Node source, Node sink, int layer, double originalx) {
//      this(DUMMY, layer, originalx);
//      addOutgoing(sink);
//      addIncoming(source);
//     
//      sink.incoming.remove(source);
//      sink.incoming.add(this);
//     
//      source.outgoing.remove(sink);
//      source.outgoing.add(this);
//    }
    
    /*package*/ void addOut(Edge edge, Assignment to) {
      Connection arc = new Connection(edge, this, to);
      out.add(arc);
      to.in.add(arc);
    }
    
    /*package*/ void addIn(Edge edge, Assignment from) {
      Connection arc = new Connection(edge, from, this);
      in.add(arc);
      from.out.add(arc);
    }
    
    /*package*/ boolean push(int layer) {
      if (this.layer>=layer)
        return false;
      this.layer = layer;
      return true;
    }
    
    @Override
    public String toString() {
      StringBuffer result = new StringBuffer();
      result.append("{");
      for (int i=0;i<in.size();i++) {
        if (i>0) result.append(",");
        result.append(in.get(i).from.vertex);
      }
      result.append("}");
      result.append(vertex);
      result.append("{");
      for (int i=0;i<out.size();i++) {
        if (i>0) result.append(",");
        result.append(out.get(i).to.vertex);
      }
      result.append("}");
      return result.toString();
    }
  } // Assignment
  
}
