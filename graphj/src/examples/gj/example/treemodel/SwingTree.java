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

import gj.example.Example;
import gj.layout.Graph2D;
import gj.layout.LayoutException;
import gj.layout.graph.tree.TreeLayout;
import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;
import gj.ui.GraphRenderer;
import gj.ui.GraphWidget;
import gj.util.DefaultGraph;
import gj.util.DefaultLayoutContext;
import gj.util.DefaultVertex;
import gj.util.TreeGraphAdapter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

/**
 * A simple example of using the graph API for showing a tree of swing components
 */
public class SwingTree implements Example {
  
  public String getName() {
    return "SwingTree";
  }

  public JComponent prepare(GraphWidget graphWidget) {
    
    // create a swing tree widget
    final JTree treeWidget = new JTree();
    treeWidget.setVisibleRowCount(4);
    for (int i=0;i<treeWidget.getRowCount();i++)
      treeWidget.expandRow(i); 
    
    // create a graph representation of JTree's default model
    TreeGraphAdapter.Tree<DefaultVertex<TreeNode>> tree = new TreeGraphAdapter.Tree<DefaultVertex<TreeNode>>() {

      public DefaultVertex<TreeNode> getRoot() {
        return new DefaultVertex<TreeNode>((TreeNode)treeWidget.getModel().getRoot());
      }
      
      public DefaultVertex<TreeNode> getParent(DefaultVertex<TreeNode> child) {
        return new DefaultVertex<TreeNode>( child.getContent().getParent() );
      }
      
      public List<DefaultVertex<TreeNode>> getChildren(DefaultVertex<TreeNode> parent) {
        List<DefaultVertex<TreeNode>> result = new ArrayList<DefaultVertex<TreeNode>>();
        for (int i=parent.getContent().getChildCount(); i>0; i--)
          result.add(new DefaultVertex<TreeNode>(parent.getContent().getChildAt(i-1)));
        return result;
      }
      
    };
    
    final Graph graph = new TreeGraphAdapter<DefaultVertex<TreeNode>>(tree);
    
    // apply tree layout
    Graph2D graph2d = new DefaultGraph(graph) {
      @Override
      public Shape getShapeOfVertex(Vertex v) {
        boolean leaf = v.getEdges().size() == 1;
        Dimension dim = treeWidget.getCellRenderer().getTreeCellRendererComponent(treeWidget, v, false, false, leaf, 0, false).getPreferredSize();
        return new Rectangle(-dim.width/2, -dim.height/2, dim.width, dim.height);
      }
    };
    
    try {
      TreeLayout layout = new TreeLayout();
      layout.setDistanceBetweenGenerations(16);
      layout.setDistanceInGeneration(7);
      layout.setOrientation(90);
      layout.setAlignmentOfParent(1);
      layout.setBendArcs(true);
      layout.setOrderSiblingsByPosition(false);
      layout.apply(graph2d, new DefaultLayoutContext());
    } catch (LayoutException e) {
      throw new RuntimeException("hmm, can't layout swing tree model", e);
    }
    
    // stuff into a graph widget
    graphWidget.setGraph2D(graph2d);
    graphWidget.setRenderer(new GraphRenderer() {
      public void render(Graph2D graph2d, Graphics2D graphics) {
        // render vertices
        for (Vertex v : graph.getVertices()) {
          Point2D p = graph2d.getPositionOfVertex(v);
          Rectangle r = graph2d.getShapeOfVertex(v).getBounds();
          r.translate((int)p.getX(), (int)p.getY());
          boolean leaf = v.getEdges().size() == 1;
          Component c =treeWidget.getCellRenderer().getTreeCellRendererComponent(treeWidget, v, false, false, leaf, 0, false);
          c.setSize(r.getSize());
          c.paint(graphics.create(r.x, r.y, r.width, r.height ));
        }
        // render edges
        graphics.setColor(Color.LIGHT_GRAY);
        for (Edge edge : graph.getEdges()) {
          Point2D pos = graph2d.getPositionOfVertex(edge.getStart());
          AffineTransform old = graphics.getTransform();
          graphics.translate(pos.getX(), pos.getY());
          graphics.draw(graph2d.getPathOfEdge(edge));
          graphics.setTransform(old);
        }
      }
    });
 
    // and show
    JPanel content = new JPanel(new GridLayout(1,2));
    content.add(new JScrollPane(graphWidget));
    content.add(new JScrollPane(treeWidget));
    
    // done
    return content;
  }
}
