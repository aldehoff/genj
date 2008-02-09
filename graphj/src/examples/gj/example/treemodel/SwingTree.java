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
package gj.example.treemodel;

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
import java.awt.GridLayout;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

/**
 * A simple example of using the graph API for showing a family tree
 */
public class SwingTree {
  
  /** main method */
  public static void main(String[] args) {
    
    // create a swing tree widget
    final JTree treeWidget = new JTree();
    for (int i=0;i<treeWidget.getRowCount();i++)
      treeWidget.expandRow(i); 
    
    // create a graph representation of JTree's silly default model
    Graph graph = new Graph() {

      public Set<? extends Vertex> getVertices() {
        return _getVertices((TreeNode)treeWidget.getModel().getRoot(), new LinkedHashSet<Vertex>());
      }
      
      private Set<Vertex> _getVertices(TreeNode node, Set<Vertex> result) {
        result.add(new DefaultVertex(node));
        for (int i=0;i<node.getChildCount();i++) 
          _getVertices(node.getChildAt(i), result);
        return result;
      }

      public Set<? extends Edge> getEdges() {
        return _getEdges((TreeNode)treeWidget.getModel().getRoot(), new HashSet<Edge>());
      }
      
      public Set<Edge> _getEdges(TreeNode node, Set<Edge> result) {
        for (int i=0;i<node.getChildCount();i++) {
          TreeNode child = node.getChildAt(i);
          result.add(new DefaultEdge(new DefaultVertex(node), new DefaultVertex(child)));
          _getEdges(child, result);
        }
        return result;
      }
        
      public Set<? extends Edge> getEdges(Vertex vertex) {
        Set<Edge> result = new LinkedHashSet<Edge>();
        TreeNode node = (TreeNode)((DefaultVertex)vertex).getContent();
        if (node.getParent()!=null) result.add(new DefaultEdge(new DefaultVertex(node.getParent()), new DefaultVertex(node)));
        for (int i=node.getChildCount()-1;i>=0;i--) {
          result.add(new DefaultEdge(vertex, new DefaultVertex(node.getChildAt(i))));
        }
        return result;
      }

    };
    
    
    // apply tree layout
    Layout2D layout = new DefaultLayout(new Rectangle2D.Double(-30,-8,60,16));
    
    try {
      TreeLayoutAlgorithm algorithm = new TreeLayoutAlgorithm();
      algorithm.setOrientation(90);
      algorithm.setAlignmentOfParent(1);
      algorithm.setBendArcs(true);
      algorithm.setOrderSiblingsByPosition(false);
      algorithm.apply(graph, layout, null, null);
    } catch (LayoutAlgorithmException e) {
      throw new RuntimeException("hmm, can't layout swing tree model", e);
    }
    
    // stuff into a graph widget
    GraphWidget graphWidget = new GraphWidget(layout);
    graphWidget.setGraph(graph);
 
    // and show
    JPanel content = new JPanel(new GridLayout(1,2));
    content.add(new JScrollPane(graphWidget));
    content.add(new JScrollPane(treeWidget));
    
    JFrame frame = new JFrame("SwingTree on GraphJ");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(content);
    frame.setSize(new Dimension(640,480));
    frame.setVisible(true);
    
    // done
  }
}
