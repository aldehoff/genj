/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.app;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import genj.gedcom.*;
import genj.util.swing.ImgIconConverter;

/**
 * Component that allows to look at a Tree of TagPaths
 */
public class TagPathTree extends JScrollPane {

  /** members */
  private Vector  listeners = new Vector();
  private Gedcom  gedcom;
  private JTree   tree;
  private Node    root;

  /** statics */
  private static final Color
    cSelection = new Color(160,160,255);

  /**
   * A Node in our TagPathSelector
   */
  static class Node implements TreeNode {
    // LCD
    /** members */
    String  tag;
    Vector  children;
    Node    parent;
    boolean selected;

    /** constructor */
    Node(String tag) {
      this.tag = tag;
      children = new Vector(4);
    }
    /** adds a child */
    public void addChild(Node node) {
      children.addElement(node);
    }
    /** returns a child for given tag */
    public Node getChildFor(String tag) {
      for (int i=0;i<children.size();i++) {
        Node child = (Node)children.elementAt(i);
        if (child.tag.equals(tag))
          return child;
        }
      return null;
    }
    /** return the parent */
    public TreeNode getParent() {
      return parent;
    }
    /** returns whether children are allowed */
    public boolean getAllowsChildren() {
      return true;
    }
    /** returns child by position */
    public TreeNode getChildAt(int index) {
      return (Node)children.elementAt(index);
    }
    /** returns child count */
    public int getChildCount() {
      return children.size();
    }
    /** returns child index */
    public int getIndex(TreeNode child) {
      return children.indexOf(child);
    }
    /** returns whether this is a leaf */
    public boolean isLeaf() {
      return children.size()==0;
    }
    /** returns children */
    public Enumeration children() {
      return children.elements();
    }
    // EOC
  }

  /**
   * Our Node Renderer
   */
  class NodeRenderer implements TreeCellRenderer {
    // LCD
    /** members */
    private JPanel    panel = new JPanel();
    private JCheckBox checkbox = new JCheckBox();
    private JLabel    label = new JLabel();

    /** constructor */
    NodeRenderer() {
      panel.setLayout(new BorderLayout());
      panel.add(checkbox,"West");
      panel.add(label   ,"Center");

      checkbox.setOpaque(false);
      label.setOpaque(false);
      panel.setOpaque(false);
    }
    /** callback for component that's responsible for rendering */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      Node node = (Node)value;
      label.setText( node.tag );
      label.setIcon( ImgIconConverter.get(Property.getDefaultImage(node.tag)) );
      checkbox.setSelected(node.selected);
      return panel;
    }
    // EOC
  }

  /**
   * Constructor
   */
  public TagPathTree() {

    // Prepare tree
    tree = new JTree(new DefaultTreeModel(new Node("")));
    tree.setShowsRootHandles(true);
    tree.setRootVisible(false);
    tree.setCellRenderer(new NodeRenderer());
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    // Listening
    MouseAdapter madapter = new MouseAdapter() {
      // LCD
      /** callback for mouse click */
      public void mouseClicked(MouseEvent me) {
        // Check wether some valid path has been clicked on
        TreePath path = tree.getPathForLocation(me.getX(),me.getY());
        if (path==null)
          return;
        // Get Node and invert selection
        Node node = (Node)path.getLastPathComponent();
        node.selected = ! node.selected;
        // Signal to listeners
        fireSelectionChanged(path,node.selected);
        // Show it ... simple repaint ... but could be done better ... probably
        tree.repaint();
      }
      // EOC
    };
    tree.addMouseListener(madapter);

    // Do a little bit of layouting
    setMinimumSize(new Dimension(160, 160));
    setPreferredSize(new Dimension(160, 160));
    getViewport().setView(tree);

    // Done
  }

  /**
   * Adds another listener to this
   */
  public void addTagPathTreeListener(TagPathTreeListener listener) {
    listeners.addElement(listener);
  }

  /**
   * Helper that creates nodes of given node from TagPaths
   */
  private void createNodesFor(Node root,TagPath path, int pos, boolean select) {

    // Is there a child with that tag already?
    Node child = root.getChildFor(path.get(pos));

    // Create a new Node for first tag of path
    if (child == null) {
      child = new Node(path.get(pos));
      root.addChild(child);
    }

    // Continue recursive?
    if (pos+1<path.length()) {
      createNodesFor(child,path,pos+1,select);
    // .. or end of path?
    } else {
      child.selected=select;
    }

    // Done
  }

  /**
   * Helper that expands all rows
   */
  private void expandRows() {
    for (int i=0;i<tree.getRowCount();i++) {
      tree.expandRow(i);
    }
  }

  /**
   * Signals a change in a node to the listeners
   * @param path the TreePath to the selected node
   */
  protected void fireSelectionChanged(TreePath path, boolean on) {

    // Loop through path elements (=Nodes)
    Object[] nodes = path.getPath();
    String s = "";

    // .. start with non-root as first
    for (int n=1;;n++) {
      s+=((Node)nodes[n]).tag;

      if (n+1==nodes.length) {
        break;
      }

      s+=":";
    }

    // .. and create a TagPath
    TagPath p = new TagPath(s);

    // Go through listeners
    Enumeration enum = listeners.elements();
    while (enum.hasMoreElements()) {
      ((TagPathTreeListener)enum.nextElement()).handleSelection(p,on);
    }
  }

  /**
   * Removes one of ther listener to this
   */
  public void removeTagPathTreeListener(TagPathTreeListener listener) {
    listeners.removeElement(listener);
  }

  /**
   * Sets the TagPaths to choose from
   */
  public void setPaths(TagPath[] paths) {

    // Create a new root of this tree
    root = new Node("Root");

    // Loop through used TagPaths
    for (int i=0;i<paths.length;i++) {
      createNodesFor(root,paths[i],0,false);
    }

    tree.setModel(new DefaultTreeModel(root));

    // Show all rows
    expandRows();

    // Done
  }

  /**
   * Sets the selection
   */
  public void setSelection(TagPath[] paths) {

    // Loop through selected TagPaths
    for (int i=0;i<paths.length;i++) {
      createNodesFor(root,paths[i],0,true);
    }

    tree.setModel(new DefaultTreeModel(root));

    // Show all rows
    expandRows();

    // Done
  }

}
