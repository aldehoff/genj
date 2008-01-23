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
import gj.model.Tree;
import gj.ui.GraphWidget;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashSet;
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
    Tree tree = new Tree() {

      /** all vertices */
      public Set<?> getVertices() {
        return new HashSet<String>(Arrays.asList(family.split(",|>")));
      }

      /** neighbours */
      public Set<?> getNeighbours(Object vertex) {
        Set<String> result = new LinkedHashSet<String>();
        for (String relationship : family.split(",")) {
          String[] parent2child = relationship.split(">");
          if (parent2child[0].equals(vertex)) result.add(parent2child[1]);
          if (parent2child[1].equals(vertex)) result.add(parent2child[0]);
        }
        return result;
      }

      /** root */
      public Object getRoot() {
        return family.substring(0, family.indexOf('>'));
      }

    };
 
    // apply tree layout
    Layout2D layout = new DefaultLayout(new Rectangle2D.Double(-20,-16,40,32));
    
    try {
      new TreeLayoutAlgorithm().apply(tree, layout, null, null);
    } catch (LayoutAlgorithmException e) {
      throw new RuntimeException("hmm, can't layout my family", e);
    }
    
    // stuff into a graph widget
    GraphWidget widget = new GraphWidget(layout);
    widget.setGraph(tree);
 
    // and show
    JFrame frame = new JFrame("Family Tree on GraphJ");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(new JScrollPane(widget));
    frame.setSize(new Dimension(320,250));
    frame.setVisible(true);
    
    // done
  }

}
