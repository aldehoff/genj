/**
 * 
 */
package gj.model.impl;

import java.awt.Shape;

import gj.model.Arc;
import gj.model.Factory;
import gj.model.Graph;
import gj.model.Node;

/**
 * @author nmeier
 */
public class DefaultFactory implements Factory {

//  /**
//   * Constructor - creates a mutable graph with nodes and arcs
//   * that are clones of those in given graph (node.getPosition(),
//   * node.getShape(), node.getContent(), arc.getPath() are shared!)
//   */
//  public DefaultGraph(Graph graph) {
//
//    // clone nodes
//    Map orig2clone = new HashMap(graph.getNodes().size());
//    Iterator it = graph.getNodes().iterator();
//    while (it.hasNext()) {
//      Node orig = (Node)it.next();
//      Node clone = addNode(orig.getPosition(), orig.getShape(), orig.getContent());
//      orig2clone.put(orig,clone);
//    }
//    
//    // clone arcs
//    it = graph.getArcs().iterator();
//    while (it.hasNext()) {
//      Arc orig = (Arc)it.next();
//      addArc((Node)orig2clone.get(orig.getStart()), (Node)orig2clone.get(orig.getEnd()), orig.getPath() );
//    }
//    
//    // done
//  }
//
//
//  /**
//   * @see MutableGraph#createArc(Node, Node)
//   */
//  public Arc addArc(Node from, Node to, Path path) {
//
//    DefaultNode 
//      iFrom = getImpl(from),
//      iTo   = getImpl(to);
//      
//    if (path==null) path = new Path();
//
//    DefaultArc arc = new DefaultArc(iFrom, iTo, path);
//    iFrom.addArc(arc);
//    iTo.addArc(arc);
//
//    arcs.add(arc);
//
//    return arc;
//  }
//  
//
//  /**
//   * @see MutableGraph#createNode(Point2D, Shape, Object)
//   */
//  public Node addNode(Point2D position, Shape shape, Object content) {
//    DefaultNode node = new DefaultNode(position, shape, content);
//    nodes.add(node);
//    return node;
//  }
//


  /**
   * @see gj.model.Factory#createArc(gj.model.Graph, gj.model.Node, gj.model.Node)
   */
  public Arc createArc(Graph graph, Node from, Node to) {
    return ((DefaultGraph)graph).addArc(from, to, null);
  }

  /**
   * @see gj.model.Factory#createGraph()
   */
  public Graph createGraph() {
    return new DefaultGraph();
  }

  /**
   * @see gj.model.Factory#createNode(gj.model.Graph, java.awt.Shape, java.lang.Object)
   */
  public Node createNode(Graph graph, Shape shape, Object content) {
    return ((DefaultGraph)graph).addNode(null, shape, content);
  }

} //SimpleFactory