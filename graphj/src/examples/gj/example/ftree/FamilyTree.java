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
    
    // prepare and layout our family tree
    final String family = 
      "Lars H.>Lars,"+
      "Lars H.>Sven,"+
      "Lars H.>Nils,"+
      "Lars>Yaro,"+
      "Sven>Jonas,"+
      "Sven>Alisa,"+
      "Sven>Luka";
    
    Tree tree = new Tree() {

      public Set<?> getVertices() {
        return new HashSet<String>(Arrays.asList(family.split(",|>")));
      }
      
      public Set<?> getNeighbours(Object vertex) {
        
        Set<String> result = new LinkedHashSet<String>();
        
        for (String relationship : family.split(",")) {
          
          String father = relationship.split(">")[0];
          String son = relationship.split(">")[1];
          
          if (father.equals(vertex))
            result.add(son);
          else if (son.equals(vertex))
            result.add(father);
        }
        
        return result;
      }
      
      public Object getRoot() {
        return family.split(",")[0].split(">")[0];
      }

    };
    
    Layout2D layout = new DefaultLayout(new Rectangle2D.Double(-20,-16,40,32));
    
    try {
      new TreeLayoutAlgorithm().apply(tree, layout, null);
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
    frame.setSize(new Dimension(600,400));
    frame.setVisible(true);
    
    // wait
  }

}
