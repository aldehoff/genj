/**
 * 
 */
package gj.model;

import java.awt.Shape;


/**
 * @author nmeier
 */
public interface Factory {

  public Graph createGraph();
  
  public Node createNode(Graph graph, Shape shape, Object content);
  
  public Arc createArc(Graph graph, Node from, Node to);

} //Factory