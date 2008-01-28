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
package gj.example.ftree;

import gj.layout.DefaultLayout;
import gj.layout.Layout2D;
import gj.layout.LayoutAlgorithmException;
import gj.layout.tree.TreeLayoutAlgorithm;
import gj.model.DefaultEdge;
import gj.model.DefaultVertex;
import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;
import gj.ui.GraphWidget;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * A simple example of using the graph API for showing a family tree
 */
public class FamilyTree {
  
  /** main method */
  public static void main(String[] args) {
    
    // prepare our relationships
    final String family = 
      "L&M>L&A,"+
      "L&M>S&S,"+
      "L&M>Nils,"+
      "L&A>Yaro,"+
      "S&S>Jonas,"+
      "S&S>Alisa,"+
      "S&S>Luka";
    
    // wrap it in a tree model
    Graph graph = new Graph() {

      /** all vertices */
      public Set<Vertex> getVertices() {
        return DefaultVertex.wrap(family.split(",|>"));
      }
      
      public Set<Edge> getEdges() {
        Set<Edge> result = new LinkedHashSet<Edge>();
        for (String relationship : family.split(",")) 
          result.add(new DefaultEdge(DefaultVertex.wrap(relationship.split(">"))));
        return result;
      }
      
      public Set<Edge> getEdges(Vertex vertex) {
        Set<Edge> result = new LinkedHashSet<Edge>();
        for (String relationship : family.split(",")) {
          Edge edge = new DefaultEdge(DefaultVertex.wrap(relationship.split(">")));
          if (edge.getStart().equals(vertex)||edge.getEnd().equals(vertex)) result.add(edge);
        }
        return result;
      }

    };
 
    // apply tree layout
    Layout2D layout = new DefaultLayout(new Rectangle2D.Double(-20,-16,40,32));
    
    try {
      new TreeLayoutAlgorithm().apply(graph, layout, null, null);
    } catch (LayoutAlgorithmException e) {
      throw new RuntimeException("hmm, can't layout my family", e);
    }
    
    // stuff into a graph widget
    GraphWidget widget = new GraphWidget(layout);
    widget.setGraph(graph);
 
    // and show
    JFrame frame = new JFrame("Family Tree on GraphJ");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(new JScrollPane(widget));
    frame.setSize(new Dimension(320,250));
    frame.setVisible(true);
    
    // done
  }

}
