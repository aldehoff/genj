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

import gj.awt.geom.Path;
import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;
import gj.util.ArcIterator;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A (mutable) Graph that knows about layering of Nodes
 * and how to handle dummy nodes
 */
/*package*/ class LayeredAcyclicGraph extends AcyclicGraph {
  
  /** layer information per node */
  private Map node2layer;
  
  /** arcs that were broken */
  private List broken = new ArrayList();
  
  /** 
   * Constructor
   */
  /*package*/ LayeredAcyclicGraph(Graph graph) {
    super(graph);
  }
  
  /**
   * Return layer index for given node 
   */
  public int getLayer(Node node) {
    if (node2layer==null) return 0;
    return ((Integer)node2layer.get(node)).intValue();
  }

  /**
   * Set layer index for given node
   */
  public int setLayer(Node node, int value) {
    if (node2layer==null) {
       node2layer = new HashMap();
    }
    node2layer.put(node, new Integer(value));
    return value;
  }
  
  /**
   * Accessor - the broken arcs
   */
  public List getBrokenArcs() {
    return broken;
  }
  
  /**
   * Break an Arc into number+1 segments that span
   * number nodes (e.g. arc.start-1-2-3-arc.end)
   */
  public Node[] breakArc(Arc arc, int number) {
    
    // remember stuff about old and remove
    boolean isReversed = getReversedArcs().contains(arc);
    Path path = arc.getPath();
    removeArc(arc);
    
    // create dummy nodes
    Node[] nodes = new Node[number+2];
    
    int i=0; nodes[i++] = arc.getStart();
    for (int n=0; n<number; n++) {
      nodes[i++]=createNode(new Point2D.Double(), null, null);
      createArc(nodes[i-2], nodes[i-1], new Path());
    }
    nodes[i++] = arc.getEnd();
    createArc(nodes[i-2], nodes[i-1], new Path());
    
    // remember
    broken.add(new BrokenArc(nodes, path, isReversed));
    
    // done
    return nodes;
  } 
  
  /**
   * Creating an arc modfies the nodes' layering
   * 
   * @see gj.model.MutableGraph#createArc(Node, Node, Path)
   */
  public Arc createArc(Node from, Node to, Path path) {
    
    // delegate the create
    Arc arc = super.createArc(from,to,path);

    // check layers of 'from' and 'to'
    int fromLayer = getLayer(from);
    int   toLayer = getLayer(to  );

    // if both are not layered yet then we'll
    // put them in layer '0' and '1' and forget about it
    if (fromLayer==Integer.MAX_VALUE&&Integer.MAX_VALUE==toLayer) {
      setLayer(from,0);
      setLayer(to  ,1);
      return arc;
    }
    // if we can we push the layering
    if (  toLayer!=Integer.MAX_VALUE) {
      pushLayering(to, true);
    }
    if (fromLayer!=Integer.MAX_VALUE) {
      pushLayering(from, false);
    }
    // done
    return arc;
  }
  
  /**
   * Push layer information
   */        
  private void pushLayering(Node root, boolean incoming) {
    // this is the layer we're assuming to be in
    int val = getLayer(root);
    // loop through arcs incoming/outgoing of root
    ArcIterator it = new DirectedArcIterator(root,incoming);
    while (it.next()) {
      // don't push twice in one direction
      if (it.i>0) continue;
      // don't loop
      if (it.isLoop) continue;
      // incoming / outgoing
      if (incoming) {
        // the other end's value
        int other = getLayer(it.arc.getStart());
        // don't push if already o.k.
        if ((other!=Integer.MAX_VALUE)&&(other<val)) continue;
        // push
        setLayer(it.arc.getStart(),val-1);
        pushLayering(it.arc.getStart(), true);
      } else {
        // the other end's value
        int other = getLayer(it.arc.getEnd());
        // don't push if already o.k.
        if ((other!=Integer.MAX_VALUE)&&(other>val)) continue;
        // push
        setLayer(it.arc.getEnd(),val+1);
        pushLayering(it.arc.getEnd(), false);
      }
    }
    // done
  }
      

  /**
   * Creating a node means we'll keep a layer for 'em
   * 
   * @see gj.model.MutableGraph#createNode(Point2D, Shape, Object)
   */
  public Node createNode(Point2D position, Shape shape, Object content) {
    
    // delegate the create
    Node result = super.createNode(position, shape, content);
    
    // remember layer '0'
    setLayer(result, Integer.MAX_VALUE);
    
    // done
    return result;
  }

  /**
   * A wrapper for remembering arcs that were broken down
   */
  /*package*/ class BrokenArc implements Arc {
    public Path path;
    public Node[] nodes;
    protected BrokenArc(Node[] ns, Path p, boolean isReversed) {
      if (isReversed) {
        for (int n=0; n<ns.length/2; n++) {
          Node t = ns[n];
          ns[n] = ns[ns.length-1-n];
          ns[ns.length-1-n] = t;
        }
      }
      nodes=ns;
      path=p;
    }
    public Node getEnd() {
      return nodes[nodes.length-1];
    }
    public Node getStart() {
      return nodes[0];
    }
    public Path getPath() {
      return path;
    }
    public Node[] getNodes() {
      return nodes;
    }
  } //BrokenArc
}
