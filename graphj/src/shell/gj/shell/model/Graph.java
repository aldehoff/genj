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
package gj.shell.model;

import gj.geom.ShapeHelper;
import gj.model.DirectedGraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A default impl for 
 * @see gj.layout.Layout2D
 */
public class Graph implements DirectedGraph {
  
  /** current selection - either edge or vertex*/
  private Element selection;
  
  /** the contained nodes */
  protected Set<Vertex> vertices = new HashSet<Vertex>(10);

  /** the contained arcs */
  private Collection<Edge> edges = new HashSet<Edge>(10);
  
  /**
   * Constructor
   */
  public Graph() {
  }

  /**
   * Constructor
   */
  public Graph(Graph other) {
    this.vertices.addAll(other.vertices);
    this.edges.addAll(other.edges);
  }
  
  /**
   * Validate - good for subclasses like tree
   */
  public void validate() {
  }

  /**
   * add an edge
   */
  public Edge addEdge(Vertex from, Vertex to, Shape shape) {
    Edge edge = from.addEdge(to, shape);
    edges.add(edge);
    return edge;
  }
  
  /**
   * All Edges
   */
  public Collection<Edge> getEdges() {
    return edges;
  }
  
  /**
   * remove an edge
   */
  public void removeEdge(Edge edge) {
    if (!edges.remove(edge))
      throw new IllegalArgumentException("remove on non-graph edge");
    Vertex
     start = edge.getStart(),
     end   = edge.getEnd();
    start.removeEdge(edge, end);
    end  .removeEdge(edge, start);
  }

  /**
   * add a vertex
   */
  public Vertex addVertex(Point2D position, Shape shape, Object content) {
    Vertex node = new Vertex(position, shape, content);
    vertices.add(node);
    return node;
  }

  /**
   * removes a vertex
   */
  public void removeVertex(Vertex node) {
    
    for (Edge edge : new ArrayList<Edge>(node.getEdges()) )
      removeEdge(edge);
      
    vertices.remove(node);
  }
  
  /**
   * Access - current selection
   */
  public void setSelection(Element set) {
    selection = set;
  }
  
  /**
   * Access - current selection
   */
  public Element getSelection() {
    return selection;
  }
  
  /**
   * find a vertex or edge by position
   */
  public Element getElement(Point2D point) {
    
    // look through vertices
    Element result = getVertex(point);
    if (result!=null)
      return result;

    // look through edges
    result = getEdge(point);
    if (result!=null)
      return result;
    
    // not found
    return null;
    
  }
  
  /**
   * Get Edge by position
   */
  public Edge getEdge(Point2D point) {
    
    Edge result = null;

    for (Edge edge : edges) {
      // check an edge
      if (edge.contains(point))
        result = edge;
      else
        if (result!=null) break;
    }
    
    // not found
    return result;
  }
  
  
  /**
   * find a node by position
   */
  public Vertex getVertex(Point2D point) {

    // look through nodes
    Iterator<?> it = vertices.iterator();
    while (it.hasNext()) {
      
      // check a node
      Vertex node = (Vertex)it.next();
      if (node.contains(point)) 
        return node;
    }
    
    // not found
    return null;
  }
  
  /**
   * interface implementation
   */
  public int getNumVertices() {
    return vertices.size();
  }
  
  /**
   * interface implementation
   */
  public Iterable<Vertex> getVertices() {
    return vertices;
  }
  
  /**
   * interface implementation
   */
  public int getNumAdjacentVertices(Object vertex) {
    return ((Vertex)vertex).getNumNeighbours();
  }
  
  /**
   * interface implementation
   */
  public Iterable<Vertex> getAdjacentVertices(Object vertex) {
    return ((Vertex)vertex).getNeighbours();
  }
  
  /**
   * interface implementation
   */
  public int getNumDirectPredecessors(Object vertex) {
    int result = 0;
    for (Edge edge : ((Vertex)vertex).getEdges()) {
      if (edge.getEnd() == vertex) result++;
    }  
    return result;
  }
  
  /**
   * interface implementation
   */
  public Iterable<Vertex> getDirectPredecessors(Object vertex) {
    // FIXME this could be in-situ without temporary array
    List<Vertex> predecessors = new ArrayList<Vertex>();
    for (Edge edge : ((Vertex)vertex).getEdges()) {
      if (edge.getEnd() == vertex)
        predecessors.add(edge.getStart());
    }  
    return predecessors;
  }
  
  /**
   * interface implementation
   */
  public int getNumDirectSuccessors(Object vertex) {
    int result = 0;
    for (Edge edge : ((Vertex)vertex).getEdges()) {
      if (edge.getStart() == vertex) result++;
    }  
    return result;
  }
  
  /**
   * interface implementation
   */
  public Iterable<?> getDirectSuccessors(Object vertex) {
    // FIXME this could be in-situ without temporary array
    List<Vertex> successors = new ArrayList<Vertex>();
    for (Edge edge : ((Vertex)vertex).getEdges()) {
      if (edge.getStart() == vertex)
        successors.add(edge.getEnd());
    }  
    return successors;
  }
  
  /**
   * The rendering functionality
   */
  public void render(Graphics2D graphics) {
  
    // the arcs
    renderEdges(graphics);    
    
    // the nodes
    renderVertices(graphics);
  
    // done
  }

  /**
   * Renders all Nodes
   */
  private void renderVertices(Graphics2D graphics) {
    
    // Loop through the graph's nodes
    for (Vertex vertex : vertices) {
      renderVertex(vertex, graphics);
    }
    
    // Done
  }

  private void renderVertex(Vertex vertex, Graphics2D graphics) {
    
    // figure out its color
    Color color = getColor(vertex);
    Stroke stroke = getStroke(vertex);
  
    // draw its shape
    Point2D pos = vertex.getPosition();
    graphics.setColor(color);
    graphics.setStroke(stroke);
    Shape shape = vertex.getShape(); 
    draw(shape, pos, graphics);
  
    // and content    
    Object content = vertex.getContent();
    if (content==null) 
      return;
  
    Shape oldcp = graphics.getClip();
    graphics.clip(ShapeHelper.createShape(shape, 1, pos));
    
    draw(content.toString(), pos, graphics);
  
    graphics.setClip(oldcp);
    // done
  }
  
  /**
   * Color resolve
   */
  protected Color getColor(Vertex vertex) {
    return vertex==selection ? Color.BLUE : Color.BLACK;    
  }

  /**
   * Color resolve
   */
  protected Color getColor(Edge edge) {
    return edge==selection ? Color.BLUE : Color.BLACK;    
  }

  /**
   * Stroke resolve
   */
  protected Stroke getStroke(Vertex vertex) {
    return new BasicStroke();    
  }

  /**
   * Renders all Arcs
   */
  private void renderEdges(Graphics2D graphics) {
    
    // Loop through the graph's arcs
    Iterator<?> it = edges.iterator();
    while (it.hasNext()) 
      renderEdge((Edge)it.next(), graphics);
  
    // Done
  }

  /**
   * Renders an Arc
   */
  private void renderEdge(Edge edge, Graphics2D graphics) {
    
    // arbitrary color
    graphics.setColor(getColor(edge));
    
    // the path's shape
    Shape shape = edge.getShape();
    graphics.draw(shape);
    
    // debugging
//    Vertex 
//     v1 = edge.getStart(),
//     v2 = edge.getEnd();
//    
//    double distance = Geometry.getDistance(
//      new ResettablePathIterator(v1.getShape(), v1.getPosition()),
//      new ResettablePathIterator(v2.getShape(), v2.getPosition()),
//      Geometry.getAngle(v1.getPosition(), v2.getPosition())
//    );
//    
//    if (distance==Double.MAX_VALUE)
//      return;
//    
//    Point2D 
//     a = edge.getStart().getPosition(),
//     b = edge.getEnd  ().getPosition();
//    
//    double 
//     x = a.getX() + (b.getX()-a.getX())/2,
//     y = a.getY() + (b.getY()-a.getY())/2; 
//    
//    graphics.drawString(""+(int)distance, (float)x, (float)y);
    
    // done      
  }

  /**
   * Helper that renders a shape at given position
   */
  private void draw(Shape shape, Point2D at, Graphics2D graphics) {
    draw(shape, at, 0, false, graphics);
  }

  /**
   * Helper that renders a shape at given position with given rotation
   */
  private void draw(Shape shape, Point2D at, double theta, boolean fill, Graphics2D graphics) {
    AffineTransform old = graphics.getTransform();
    graphics.translate(at.getX(), at.getY());
    graphics.rotate(theta);
    if (fill) graphics.fill(shape);
    else graphics.draw(shape);
    graphics.setTransform(old);
  }

  /**
   * Helper that renders a string at given position
   */
  private void draw(String str, Point2D at, Graphics2D graphics) {
    float
      x = (float)at.getX(),
      y = (float)at.getY();
    FontMetrics fm = graphics.getFontMetrics();
    Rectangle2D r = fm.getStringBounds(str, graphics);
    LineMetrics lm = fm.getLineMetrics(str, graphics);
    float
      w = (float)r.getWidth(),
      h = (float)r.getHeight();
    //  graphics.draw(new Rectangle2D.Double(
    //    x-w/2, y-h/2, w, h     
    //  ));
    graphics.drawString(str, x-w/2, y+h/2-lm.getDescent());
  }
  
} //DefaultGraph