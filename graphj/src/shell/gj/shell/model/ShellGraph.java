/**
 * 
 */
package gj.shell.model;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author nmeier
 */
public class ShellGraph implements gj.model.Graph {
  
  private ArrayList nodes = new ArrayList();
  
  private ArrayList arcs = new ArrayList();

  /**
   * Remove node
   */
  protected void removeNode(ShellNode node) {
    nodes.remove(node);
  }

  /**
   * Remove arc
   */
  protected void removeArc(ShellArc arc) {
    arcs.remove(arc);
  }

  /**
   * @see gj.model.Graph#getArcs()
   */
  public Collection getArcs() {
    return arcs;
  }

  /**
   * @see gj.model.Graph#getNodes()
   */
  public Collection getNodes() {
    return nodes;
  }

  /**
   */
  public ShellNode createNode(Shape shape, Object content) {
    return createNode(shape, content, null);
  }
  
  /**
   */
  public ShellNode createNode(Shape shape, Object content, Point2D pos) {
    ShellNode result = new ShellNode(this, shape, content);
    if (pos!=null) result.getPosition().setLocation(pos);
    nodes.add(result);
    return result;
  }

  /**
   */
  public ShellArc createArc(ShellNode from, ShellNode to) {
    ShellArc result = new ShellArc(this, from, to);
    from.addArc(result);
    to.addArc(result);
    arcs.add(result);
    return result;
  }

  /**
   * Return node at given point
   */
  public ShellNode getNode(Point2D point) {
    // look through nodes
    for (int n=0; n<nodes.size(); n++) {
      ShellNode node = (ShellNode)nodes.get(n);
      if (node.contains(point))
        return node;
    }
    // done
    return null;
  }
  
} //Graph