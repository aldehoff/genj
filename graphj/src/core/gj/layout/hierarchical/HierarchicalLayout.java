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

import gj.layout.AbstractLayout;
import gj.layout.Layout;
import gj.layout.LayoutException;
import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;
import gj.util.ModelHelper;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A Layout for graphs
 * TODO:
 *  node placement
 */
public class HierarchicalLayout extends AbstractLayout implements Layout {
  
  /** padding between layers */
  //public double padLayers = 8.0D;
  
  /** padding between elements in a layer */
  //public double padElements = 8.0D;
  
  /** minimum distance between layers */
  //public double minLayerDist = 0.0D;
  
  /** minimum distance between nodes */
  //public double minNodeDist = 0.0D;

  /** whether to use ports or not */
  //public boolean isUsePorts;

  /** whether to layout lines orthogonally or not */
  //public boolean isOrthogonalLines;
  
  
  /**
   * @see gj.layout.Layout#applyTo(Graph)
   */
  public void layout(Graph graph) throws LayoutException {
    
    // something to do?
    if (graph.getNodes().isEmpty()) return;
    
    // Algorithm :
    // (1) create a layered acyclic graph
    LayeredAcyclicGraph lag = new LayeredAcyclicGraph(graph);
    // (2) create dummy nodes
    createDummyNodes(lag);
    // (3) reduce the number of crossings
    reduceCrossings(lag);
    // (4) position nodes in layers
    positionInLayers(lag);
    // (5) layout arcs
    layoutArcs(lag);
    
    // calculate bounds
    graph.getBounds().setRect(ModelHelper.getBounds(graph.getNodes()));

    // Done
  }
  
  /**
   * Create dummy nodes
   */
  private void createDummyNodes(LayeredAcyclicGraph graph) {

    // insert dummy nodes
    Iterator it = new ArrayList(graph.getArcs()).iterator();
    while (it.hasNext()) {
      
      Arc arc = (Arc)it.next();
      
      int 
        s = graph.getLayer(arc.getStart()),
        e = graph.getLayer(arc.getEnd  ()),
        d = Math.abs(e-s);
        
      if (d>1) {
        
        // break the arc and get the sequence of nodes
        // that substituted the arc
        Node[] nodes = graph.breakArc(arc, d-1);
        
        // layer the new Nodes
        for (int i=0; i<nodes.length; i++) {
          graph.setLayer(nodes[i], s<e?s++:s--);
        }
      }
      
    }
    
    // done
  }
  
  /**
   * Reduce crossings 
   */
  private void reduceCrossings(LayeredAcyclicGraph graph) {
    
    Iterator it = graph.getNodes().iterator();
    for (int i=0;it.hasNext();i++) {
      Node node = (Node)it.next(); 
      node.getPosition().setLocation(
        i*48,
        graph.getLayer(node)*48
      );
    }
    
  }

  /**
   * Position nodes in layers
   */
  private void positionInLayers(LayeredAcyclicGraph graph) {
    // TODO
  }
  
  /**
   * Post Processing : Layout arcs
   */
  private void layoutArcs(LayeredAcyclicGraph graph) {

    // first layout all arcs - their paths are
    // the same instances as the original arc 
    // so we're good for all of those     
    Iterator it = graph.getArcs().iterator();
    while (it.hasNext()) {
      Arc arc = (Arc)it.next();
      arcLayout.layout(arc);
    }
    
    // then layout all arcs that were broken - the
    // list of broken arcs contains the same path
    // instances of the originals
    it = graph.getBrokenArcs().iterator();
    while (it.hasNext()) {
      LayeredAcyclicGraph.BrokenArc arc = (LayeredAcyclicGraph.BrokenArc)it.next();
      Node[] nodes = arc.getNodes();
      // the nodes' points form the path
      Point2D[] points = new Point2D[nodes.length];
      for (int d=0; d<nodes.length; d++) {
        points[d] = nodes[d].getPosition();
      }
      // which we reflect on the Path
      arcLayout.layout(
        arc.getPath(), 
        points,
        arc.getStart().getShape(),
        arc.getEnd().getShape()
      );
      // next broken arc
    }
 
    // done     
  }

}
