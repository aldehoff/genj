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
package genj.renderer;

import genj.gedcom.Gedcom;
import genj.util.ActionDelegate;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.HeadlessLabel;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/** 
 * A list of editable BluePrints */
public class BlueprintList extends JSplitPane {
  
  /** we keep one editor */
  private BlueprintEditor editor = new BlueprintEditor();

  /** blueprints */
  private List[] blueprints;
    
  /** tree of blueprints */
  private JTree treeBlueprints;
  
  /** our buttons */
  private AbstractButton bAdd, bDel;
  
  /** resources */
  private final static Resources resources = new Resources(BlueprintEditor.class);
 
  /**
   * Constructor   */
  public BlueprintList() {
    
    // prepare blueprints
    blueprints = new List[Gedcom.NUM_TYPES];
    for (int i=0; i<Gedcom.NUM_TYPES; i++) {
      List bs = new ArrayList();
      bs.add(new Blueprint("Minimum", "a"));
      bs.add(new Blueprint("Maximum", "a"));
      bs.add(new Blueprint("Colors", "a"));
      blueprints[i] = bs;
    }
    
    // prepare tree
    Model model = new Model();
    treeBlueprints = new JTree(model);
    treeBlueprints.setRootVisible(false);
    treeBlueprints.setCellRenderer(model);
    for (int r=0;r<treeBlueprints.getRowCount();r++)
      treeBlueprints.expandRow(r);
    
    // left section
    Box left = new Box(BoxLayout.Y_AXIS);
    left.add(new JScrollPane(treeBlueprints));
    
    ButtonHelper bh = new ButtonHelper().setContainer(left).setResources(resources);
    bAdd = bh.create(new ActionAdd());
    bDel = bh.create(new ActionDel());
    // children
    setLeftComponent(left);
    setRightComponent(editor);
    // done    
  }
  
  /**
   * Action Add
   */
  private class ActionAdd extends ActionDelegate {
    /**
     * Constructor     */
    private ActionAdd() {
      super.setText("blueprint.add");
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
    }
  } //ActionAdd

  /**
   * Action Remove
   */
  private class ActionDel extends ActionDelegate {
    /**
     * Constructor
     */
    private ActionDel() {
      super.setText("blueprint.del");
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
    }
  } //ActionAdd

  /**
   * A tree model of blueprints   */
  private class Model implements TreeModel, TreeCellRenderer { 
    
    /** a performance label we keep */
    private JLabel label = new HeadlessLabel();
    
    /** a color for selection backgrounds */
    private Color cSelection = new JTable().getSelectionBackground();
    
    /**
     * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
     */
    public void addTreeModelListener(TreeModelListener l) {
    }
    /**
     * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
     */
    public int getChildCount(Object parent) {
      if (parent==this) return blueprints.length;
      if (parent instanceof List) return  ((List)parent).size();
      return 0;
    }
    /**
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    public Object getChild(Object parent, int index) {
      if (parent==this) return blueprints[index];
      if (parent instanceof List) return ((List)parent).get(index);
      return null;
    }
    /**
     * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
     */
    public int getIndexOfChild(Object parent, Object child) {
      throw new IllegalArgumentException();
    }
    /**
     * @see javax.swing.tree.TreeModel#getRoot()
     */
    public Object getRoot() {
      return this;
    }
    /**
     * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
     */
    public boolean isLeaf(Object node) {
      if (node == this) return false;
      if (node instanceof List) return false;
      return true;
    }
    /**
     * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
     */
    public void removeTreeModelListener(TreeModelListener l) {
    }
    /**
     * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
    }
    /**
     * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      
      if (value instanceof List) {
        for (int i=0; i<blueprints.length; i++) {
          if (blueprints[i]==value) {
            label.setText(Gedcom.getNameFor(i, true));
            label.setIcon(Gedcom.getImage(i));
            break;
          }
        }
      } else {
        label.setText(value.toString());
        label.setIcon(null);
      }
      label.setOpaque(selected);
      label.setBackground(cSelection);
      return label;
    }
  } //BlueprintTree     

} //BluePrintList
