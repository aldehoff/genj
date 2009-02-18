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
package gj.example.inheritance;

import gj.layout.DefaultLayout;
import gj.layout.Layout2D;
import gj.layout.LayoutAlgorithmException;
import gj.layout.radial.RadialLayoutAlgorithm;
import gj.model.Graph;
import gj.ui.DefaultGraphRenderer;
import gj.ui.GraphWidget;
import gj.util.TreeGraphAdapter;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 * A simple example of using the graph API for showing a family tree
 */
public class InheritanceTree {
  
  /** main method */
  public static void main(String[] args) {
    
    final Class<?> root = BasicComboBoxRenderer.class;
    
    // prepare our relationships
    TreeGraphAdapter.Tree<Class<?>> tree = new TreeGraphAdapter.Tree<Class<?>>() {
      public List<Class<?>> getChildren(Class<?> parent) {
        List<Class<?>> result = new ArrayList<Class<?>>();
        result.addAll(Arrays.asList(parent.getInterfaces()));
        result.remove(Serializable.class);
        if (parent.getSuperclass()!=null)
          result.add(parent.getSuperclass());
        return result;
      }
      public Class<?> getParent(Class<?> child) {
        return getParent(getRoot(), child);
      }
      private Class<?> getParent(Class<?> parent, Class<?> child) {
        throw new IllegalArgumentException("not supported");
      }
      public Class<?> getRoot() {
        return root;
      }
    };

    Graph graph = new TreeGraphAdapter<Class<?>>(tree);
 
    // apply radial layout
    Layout2D layout = new DefaultLayout(new Rectangle2D.Double(-8,-50,16,100));
    
    try {
      RadialLayoutAlgorithm r = new RadialLayoutAlgorithm();
      r.setDistanceBetweenGenerations(120);
      r.setAdjustDistances(false);
      r.apply(graph, layout, null, null);
    } catch (LayoutAlgorithmException e) {
      throw new RuntimeException("hmm, can't layout my family", e);
    }
    
    // stuff into a graph widget
    GraphWidget widget = new GraphWidget(layout);
    widget.setRenderer(new DefaultGraphRenderer() {
//      @Override
//      protected String getContent(Vertex vertex) {
//        // TODO Auto-generated method stub
//        return vertex.get
//      }
    });
    widget.setGraph(graph);
 
    // and show
    JFrame frame = new JFrame("Inheritance of "+root);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(new JScrollPane(widget));
    frame.setSize(new Dimension(320,250));
    frame.setVisible(true);
    
    // done
  }

}
