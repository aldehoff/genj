/**
 * 
 */
package gj.shell.model;

import gj.model.Arc;
import gj.model.Factory;
import gj.model.Graph;
import gj.model.Node;

import java.awt.Shape;

/**
 * @author nmeier
 */
public class ShellFactory implements Factory {

  private Shape defaultShape;

  /**
   * Constructor
   */
  public ShellFactory(Shape shape) {
    defaultShape = shape;
  }

  /**
   * @see gj.model.Factory#createGraph()
   */
  public Graph createGraph() {
    return new ShellGraph(); 
  }

  /**
   * @see gj.model.Factory#createNode(gj.model.Graph, java.awt.Shape, java.lang.Object)
   */
  public Node createNode(Graph graph, Shape shape, Object content) {
    return ((ShellGraph)graph).createNode(shape!=null?shape:defaultShape, content);
  }

  /**
   * @see gj.model.Factory#createArc(gj.model.Graph, gj.model.Node, gj.model.Node)
   */
  public Arc createArc(Graph graph, Node from, Node to) {
    
    if (!(graph instanceof ShellGraph))
      throw new IllegalArgumentException("unknown graph");
      
    ShellGraph sgraph = (ShellGraph)graph;
    
    if (!sgraph.getNodes().contains(from))
      throw new IllegalArgumentException("unknown from");
    
    if (!sgraph.getNodes().contains(to))
      throw new IllegalArgumentException("unknown to");
      
    return sgraph.createArc((ShellNode)from, (ShellNode)to);
  }

} //Factory