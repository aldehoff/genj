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

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * A layering based on longest paths from sinks
 */
public class LongestPathLA implements LayerAssignment {

  private final static VertexInLayerComparator BYXCOMPARATOR = new VertexByXPositionComparator();
  
  private Map<Vertex, Cell> vertex2cell;
  private List<Layer> layers;
  private int width;
  private VertexInLayerComparator initialOrdering;
  private Layout2D layout;

  /** layering algorithm */
  public void assignLayers(Graph graph, Layout2D layout, VertexInLayerComparator initialOrdering) throws GraphNotSupportedException {

    // prepare state
    this.layout = layout;
    this.initialOrdering = initialOrdering;
    
    vertex2cell = new HashMap<Vertex, Cell>();
    layers = new ArrayList<Layer>();
    
    width = 0;

    // find sinks
    for (Vertex v : graph.getVertices()) {
      if (LayoutHelper.isSink(v)) 
        sinkToSource(null, v, new Stack<Cell>());
    }
    
    // place vertices in resulting layers
    for (Vertex vertex : graph.getVertices()) {
      Cell cell = vertex2cell.get(vertex);
      Layer layer = layers.get(cell.layer);
      layer.add(cell, initialOrdering);
      width = Math.max(width, layer.size());

    }
    
    // add dummy vertices
    dummyVertices();

    // done
  }
  
  /**
   * add dummy vertices were edges span multiple layers
   */
  private void dummyVertices() {

    // loop over layers and check incoming
    for (int i=0;i<layers.size()-1;i++) {
      Layer layer = layers.get(i);
      
      for (Cell cell : layer.cells) {
        
        for (int j=0;j<cell.in.size();j++) {
          
          Cell2Cell arc = cell.in.get(j);
          
          if (arc.from.layer != i+1) {
            
            // create a dummy at same position as cell
            Cell dummy = new Cell(new DummyVertex(), i+1);
            layout.setPositionOfVertex(dummy.vertex, layout.getPositionOfVertex(cell.vertex));
            width = Math.max(width, layers.get(i+1).add(dummy, BYXCOMPARATOR));

            // delete old connection
            cell.in.remove(j);
            arc.from.out.remove(arc);
            
            // rewire
            dummy.addOut(arc.edge, cell);
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
  private void sinkToSource(Edge edge, Vertex vertex, Stack<Cell> path) throws GraphNotSupportedException{
    
    // check if we're back at a vertex we've seen in this iteration
    if (path.contains(vertex))
      throw new GraphNotSupportedException("graph has to be acyclic");
    
    // make sure we have enough layers
    if (layers.size()<path.size()+1)
      layers.add(new Layer(path.size()));
    
    // create or reuse an assignment
    Cell cell = vertex2cell.get(vertex);
    if (cell==null) {
      cell = new Cell(vertex, -1);
      vertex2cell.put(vertex, cell);
    }
    
    // add adjacent vertices (previous in path)
    if (edge!=null)
      cell.addOut(edge, path.peek());
    
    // push to new layer and continue if node's layer has changed
    if (!cell.push(path.size()))
      return;      

    // recurse into incoming edges direction of source
    path.push(cell);
    for (Edge e : vertex.getEdges()) {
      if (e.getEnd().equals(vertex))
        sinkToSource(e, e.getStart(), path);
    }
    path.pop();
    
    // done
  }
   
  public Point[] getRouting(Edge edge) {
    
    // start with edge's start cell and its layer
    Cell cell = vertex2cell.get(edge.getStart());
    List<Point> result = new ArrayList<Point>();
    result.add(new Point(cell.position,cell.layer));
    
    // find outgoing arc
    while (true) {
      
      Cell2Cell arc = null;
      for (int i=0;i<cell.out.size();i++) {
        arc = cell.out.get(i);
        if (arc.edge.equals(edge)) break;
        arc = null;
      }
  
      if (arc==null)
        throw new IllegalArgumentException("n/a");
  
      // add routing element
      result.add(new Point(arc.to.position, arc.to.layer));
      
      // done?
      if (!(arc.to.vertex instanceof DummyVertex)) break;
      
      // continue into dummy
      cell = arc.to;
    }
    
    // done
    return result.toArray(new Point[result.size()]);
  }

  public void swapVertices(int layer, int u, int v) {
    layers.get(layer).swap(u,v);
  }

  public int[] getOutgoingIndices(int layer, int u) {
    Cell cell = layers.get(layer).get(u);
    int[] result = new int[cell.out.size()];
    for (int i=0;i<cell.out.size();i++)
      result[i]= cell.out.get(i).to.position;
    return result;
  }

  public int[] getIncomingIndices(int layer, int u) {
    Cell cell = layers.get(layer).get(u);
    int[] result = new int[cell.in.size()];
    for (int i=0;i<cell.in.size();i++)
      result[i] = cell.in.get(i).from.position;
    return result;
  }

  public int getHeight() {
    return layers.size();
  }
  
  public int getWidth() {
    return width;
  }
  
  public int getWidth(int layer) {
    return layers.get(layer).size();
  }

  public Vertex getVertex(int layer, int u) {
    return layers.get(layer).get(u).vertex;
  }

  /**
   * an ordered layer of nodes
   */
  protected class Layer {
    
    protected List<Cell> cells = new ArrayList<Cell>();
    protected int layer;
    
    /** constructor */
    public Layer(int layer) {
      this.layer = layer;
    }
    
    /**
     * Add a vertex to layer at given position
     */
    protected int add(Cell cell, VertexInLayerComparator comparator) {

      // find appropriate position
      int pos = 0;
      while (pos<cells.size()){
        if (comparator.compare(cell.vertex, cells.get(pos).vertex, layer, layout)<0)
          break;
        pos ++;
      }

      // insert 
      cell.position = pos;
      cells.add(pos, cell);
      while (++pos<cells.size())
        cells.get(pos).position++;

      // done
      return cells.size();
    }
    
    protected void swap(int u, int v) {
      Cell vu = cells.get(u);
      vu.position = v;
      
      Cell vv = cells.get(v);
      vv.position = u;
      
      cells.set(u, vv);
      cells.set(v, vu);
    }
    
    protected Cell get(int u) {
      return cells.get(u);
    }
    
    protected int size() {
      return cells.size();
    }
    
    @Override
    public String toString() {
      return cells.toString();
    }
    
  } //LayerImpl
  
  protected static class Cell2Cell {
    protected Edge edge;
    protected Cell from, to;
    protected Cell2Cell(Edge edge, Cell from, Cell to) {
      this.edge = edge;
      this.from = from;
      this.to = to;
    }
  } //Connection
  
  /**
   * A vertex assigned to a layer cell
   */
  protected static class Cell {

    private int layer = -1;
    private Vertex vertex;
    private int position = -1;
    private List<Cell2Cell> out = new ArrayList<Cell2Cell>();
    private List<Cell2Cell> in = new ArrayList<Cell2Cell>();
    
    /**
     * A new vertex/layer cell 
     */
    protected Cell(Vertex vertex, int layer) {
      this.vertex = vertex;
      this.layer = layer;
    }
   
    
    protected void addOut(Edge edge, Cell to) {
      Cell2Cell arc = new Cell2Cell(edge, this, to);
      out.add(arc);
      to.in.add(arc);
    }
    
    protected void addIn(Edge edge, Cell from) {
      Cell2Cell arc = new Cell2Cell(edge, from, this);
      in.add(arc);
      from.out.add(arc);
    }
    
    protected boolean push(int layer) {
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

  /**
   * the default vertex comparator used for placing vertices into layers
   */
  public static class VertexByXPositionComparator implements VertexInLayerComparator {
  
    /**
     * compare two vertices
     */
    public int compare(Vertex v1, Vertex v2, int layer, Layout2D layout) {
      double d = layout.getPositionOfVertex(v1).getX() - layout.getPositionOfVertex(v2).getX();
      if (d==0) return 0;
      return d<0 ? -1 : 1;
    } 
    
  }//VertexByXPositionComparator
  
}
