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
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/** 
 * A list of editable BluePrints */
public class BlueprintList extends JSplitPane {
  
  /** we keep one editor */
  private BlueprintEditor editor = new BlueprintEditor();

  /** tree of blueprints */
  private JTree treeBlueprints;
  
  /** our buttons */
  private AbstractButton bAdd, bDel;
  
  /** resources */
  private final static Resources resources = new Resources(BlueprintEditor.class);
 
  /** the current Gedcom */
  private Gedcom gedcom; 
  
  /**
   * Constructor   */
  public BlueprintList(Gedcom geDcom) {
    
    // remember
    gedcom = geDcom;

    // we have one instance of our glue that ties everything together
    Glue glue = new Glue();
    
    // prepare tree
    treeBlueprints = new JTree(glue);
    treeBlueprints.setRootVisible(false);
    treeBlueprints.setShowsRootHandles(false);
    treeBlueprints.setCellRenderer(glue);
    treeBlueprints.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    treeBlueprints.getSelectionModel().addTreeSelectionListener(glue);
    
    // left section
    Box left = new Box(BoxLayout.Y_AXIS);
    JScrollPane scroll = new JScrollPane(treeBlueprints);
    scroll.setAlignmentX(0);
    left.add(scroll);
    
    ButtonHelper bh = new ButtonHelper()
      .setContainer(left)
      .setResources(resources)
      .setEnabled(false)
      .setMaximumSize(new Dimension(Integer.MAX_VALUE, -1));
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
      // check selection
      TreePath path = treeBlueprints.getSelectionPath();
      if (path==null) return;
      // get type this is for
      // get name
      JOptionPane.showInputDialog(
        BlueprintList.this,
        resources.getString("blueprint.add.confirm"),
        "",
        JOptionPane.OK_CANCEL_OPTION
      );
      // add
      // done
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
      // check selection
      TreePath path = treeBlueprints.getSelectionPath();
      if (path==null||!(path.getLastPathComponent() instanceof Blueprint)) return;
      // confirm
      Blueprint bp = (Blueprint)path.getLastPathComponent();
      JOptionPane.showConfirmDialog(
        BlueprintList.this, 
        resources.getString("blueprint.del.confirm", bp.getName()),
        "",
        JOptionPane.YES_NO_OPTION
      );
      // delete
      // done
    }
  } //ActionAdd

  /**
   * A tree model of blueprints   */
  private class Glue implements TreeModel, TreeCellRenderer, TreeSelectionListener { 
    
    /** a performance label we keep */
    private JLabel label = new HeadlessLabel();
    
    /** a color for selection backgrounds */
    private Color cSelection = new JTable().getSelectionBackground();
    
    /**
     * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      // the simple stuff
      label.setOpaque(selected);
      label.setBackground(cSelection);
      // a list of blueprints?
      if (value instanceof TypeList) {
        int t = ((TypeList)value).type;
        label.setText(Gedcom.getNameFor(t, true));
        label.setIcon(Gedcom.getImage(t));
      }
      // a blueprint
      if (value instanceof Blueprint) {
        label.setText(((Blueprint)value).getName());
        label.setIcon(null);
      }
      // done
      return label; 
    }
    /**
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent e) {
      
      // commit editor's changes
      editor.commit();
      
      // disallow no selection
      if (e.getNewLeadSelectionPath()==null) {
        treeBlueprints.setSelectionPath(e.getOldLeadSelectionPath());
        return;
      }
      
      // different Blueprint selected -> o.k.
      if (e.getPath().getLastPathComponent() instanceof Blueprint) {
        // .. buttons
        bAdd.setEnabled(true);
        bDel.setEnabled(true);
        // .. editor
        editor.set(gedcom, (Blueprint)e.getPath().getLastPathComponent());
        return;
      }
      
      // different type section selected 
      bAdd.setEnabled(true);
      bDel.setEnabled(false);
      
      // .. editor
      editor.set(null, null);

      // collapse old - expand new
      TreePath old = e.getOldLeadSelectionPath(); 
      if (old!=null&&old.getLastPathComponent() instanceof Blueprint) old = old.getParentPath();
      treeBlueprints.collapsePath(old);
      treeBlueprints.expandPath(e.getNewLeadSelectionPath());
    }
    /**
     * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
     */
    public void addTreeModelListener(TreeModelListener l) {
    }

    /**
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    public Object getChild(Object parent, int index) {
      // us as root?
      if (parent==this) {
        return new TypeList(index, BlueprintManager.getInstance().getBlueprints(index));
      }
      // can only be list
      return ((List)parent).get(index); 
    }

    /**
     * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
     */
    public int getChildCount(Object parent) {
      // us as root?
      if (parent==this) return Gedcom.NUM_TYPES;
      // can only be list
      return ((List)parent).size(); 
    }

    /**
     * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
     */
    public int getIndexOfChild(Object parent, Object child) {
      throw new RuntimeException();
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
      return node instanceof Blueprint;
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
      throw new RuntimeException();
    }

    /**
     * a list for a type     */
    private class TypeList extends ArrayList {
      int type;
      TypeList(int tYpe, List list) { super(list); type=tYpe; }
    } //TypeList
    
  } //BlueprintTree     

} //BluePrintList
