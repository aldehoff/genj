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

import gj.geom.Geometry;
import gj.layout.DefaultLayout;
import gj.layout.Layout2D;
import gj.layout.LayoutAlgorithmException;
import gj.layout.radial.RadialLayoutAlgorithm;
import gj.model.Graph;
import gj.model.Vertex;
import gj.ui.DefaultGraphRenderer;
import gj.ui.GraphWidget;
import gj.util.TreeGraphAdapter;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

/**
 * A simple example of using the graph API for showing a family tree
 */
public class InheritanceTree {
  
  /** main method */
  public static void main(String[] args) {
    
    final Class<?> root = JLabel.class;
    
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

    final TreeGraphAdapter<Class<?>> graphAdapter = new TreeGraphAdapter<Class<?>>(tree);
 
    // apply radial layout
    final int w = 150, h = 16;
    
    Layout2D layout = new DefaultLayout(new Rectangle2D.Double(-h/2,-w/2,h,w));
    
    try {
      RadialLayoutAlgorithm r = new RadialLayoutAlgorithm();
      r.setDistanceBetweenGenerations(220);
      r.apply(graphAdapter, layout, null, null);
    } catch (LayoutAlgorithmException e) {
      throw new RuntimeException("hmm, can't layout inheritance of "+root, e);
    }
    
    // stuff into a graph widget
    GraphWidget widget = new GraphWidget(layout);
    widget.setRenderer(new DefaultGraphRenderer() {
      @Override
      protected void renderVertex(Graph graph, Vertex vertex, Layout2D layout, java.awt.Graphics2D graphics) {
        
        // clip and position
        AffineTransform oldt = graphics.getTransform();
        Point2D pos = layout.getPositionOfVertex(vertex);
        graphics.translate(pos.getX(), pos.getY());
        graphics.transform(layout.getTransformOfVertex(vertex));
        
        // draw text vertically
        Class<?> clazz = graphAdapter.getContent(vertex);
        
        graphics.rotate(Geometry.QUARTER_RADIAN);
        StringBuffer content = new StringBuffer();
        content.append(clazz.getSimpleName());
        Method[] methods = clazz.getDeclaredMethods();
        for (int i=0,j=0;j<5 && i<methods.length;i++) {
          if (methods[i].getName().startsWith("get")) {
            content.append("\n"+methods[i].getName()+"()");
            j++;
          }
        }
        draw(content.toString(), new Rectangle2D.Double(-w/2,-h/2,w,h), 0, 0.5, graphics);
        
        // restore
        graphics.setTransform(oldt);
      }
    });
    widget.setGraph(graphAdapter);
 
    // and show
    JFrame frame = new JFrame("Inheritance of "+root);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(new JScrollPane(widget));
    frame.setSize(new Dimension(320,250));
    frame.setVisible(true);
    
    // done
  }

}
