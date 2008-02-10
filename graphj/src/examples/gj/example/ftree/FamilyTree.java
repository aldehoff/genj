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
import gj.model.Graph;
import gj.model.Tree;
import gj.model.impl.DefaultVertex;
import gj.model.impl.TreeGraphAdapter;
import gj.ui.GraphWidget;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

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
    
    Tree<DefaultVertex<String>> tree = new Tree<DefaultVertex<String>>() {
      public List<DefaultVertex<String>> getChildren(DefaultVertex<String> parent) {
        List<DefaultVertex<String>> result = new ArrayList<DefaultVertex<String>>();
        for (String relationship : family.split(",")) {
          if (relationship.startsWith(parent.getContent()+">"))
            result.add(new DefaultVertex<String>(relationship.substring(parent.getContent().length()+1)));
        }
        return result;
      }
      public DefaultVertex<String> getParent(DefaultVertex<String> child) {
        for (String relationship : family.split(",")) {
          if (relationship.endsWith(">"+child.getContent()))
            return new DefaultVertex<String>(relationship.substring(0, relationship.length()-child.getContent().length()-1));
        }
        return null;
      }
      public DefaultVertex<String> getRoot() {
        return new DefaultVertex<String>(family.substring(0, family.indexOf('>')));
      }
    };

    Graph graph = new TreeGraphAdapter<DefaultVertex<String>>(tree);
 
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
    JFrame frame = new JFrame("Family Tree in GraphJ");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(new JScrollPane(widget));
    frame.setSize(new Dimension(320,250));
    frame.setVisible(true);
    
    // done
  }

}
