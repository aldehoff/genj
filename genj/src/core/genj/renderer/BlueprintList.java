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
import genj.window.WindowManager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/** 
 * A list of editable BluePrints */
public class BlueprintList extends JSplitPane {
  
  /** selection of blueprints */
  private Blueprint[] selection = new Blueprint[Gedcom.NUM_TYPES];
  
  /** we keep one editor */
  private BlueprintEditor editor;

  /** tree of blueprints */
  private JTree treeBlueprints;
  
  /** our buttons */
  private AbstractButton bAdd, bDel;
  
  /** resources */
  private final static Resources resources = Resources.get(BlueprintEditor.class);
 
  /** the current Gedcom */
  private Gedcom gedcom; 
  
  /** a reference to the BlueprintManager */
  private final static BlueprintManager bpManager = BlueprintManager.getInstance();
  
  /** the window manager */
  private WindowManager windowManager;
  
  /**
   * Constructor   */
  public BlueprintList(WindowManager winMgr) {
    
    // remember
    windowManager = winMgr;
    
    // create editor
    editor = new BlueprintEditor(windowManager);
    
    // prepare tree
    Callback glue = new Callback();
    treeBlueprints = new JTree(new Node());
    treeBlueprints.setRootVisible(false);
    treeBlueprints.setShowsRootHandles(true);
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
   * Sets the Gedcom to use   */
  public void setGedcom(Gedcom geDcom) {
    gedcom = geDcom;
  }
  
  /**
   * Acessor - selection   */
  public Blueprint[] getSelection() {
    editor.commit();
    return selection;
  }
  
  /**
   * Acessor - selection
   */
  public void setSelection(Blueprint[] selEction) {
    selection = selEction;
    treeBlueprints.repaint();
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
      Node node = (Node)path.getLastPathComponent();
      // get name
      String name = windowManager.openDialog(
        null,
        null,
        WindowManager.IMG_QUESTION,
        resources.getString("blueprint.add.confirm"),
        "",
        BlueprintList.this
      );
      if (name==null||name.length()==0) return;
      // get html
      String html = node.blueprint!=null?node.blueprint.getHTML():"";
      // add it
      Blueprint blueprint = bpManager.addBlueprint(node.type, name, html);
      // show it
      if (node.blueprint!=null) node = (Node)node.getParent();
      Node child = new Node(node.type, blueprint);
      DefaultTreeModel model = (DefaultTreeModel)treeBlueprints.getModel(); 
      model.insertNodeInto(child, node, node.getChildCount() );
      treeBlueprints.setSelectionPath(new TreePath(model.getPathToRoot(child)));
      // make sure the html editor shows
      editor.setHTMLVisible(true);
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
      if (path==null) return;
      Node node = (Node)path.getLastPathComponent();
      if (node.blueprint==null) return;
      // confirm
      Blueprint blueprint = node.blueprint;
      int rc = windowManager.openDialog(
        null,
        null,
        WindowManager.IMG_QUESTION,
        resources.getString("blueprint.del.confirm", blueprint.getName()),
        WindowManager.OPTIONS_OK_CANCEL,
        BlueprintList.this        
      ); 
      if (rc!=0) return;
      // update selection with default
      int type = bpManager.getType(blueprint);
      selection[type] = bpManager.getBlueprint(type, "");
      // delete it
      bpManager.delBlueprint(blueprint);
      // show it
      DefaultTreeModel model = (DefaultTreeModel)treeBlueprints.getModel();
      model.removeNodeFromParent(node);
      // done
    }
  } //ActionAdd

  /**
   * Glue for cell rendering and selection handling
   */
  private class Callback implements TreeCellRenderer, TreeSelectionListener {
     
    /** a radiobutton */
    private JRadioButton button = new JRadioButton();
    
    /** a label */
    private HeadlessLabel label = new HeadlessLabel();
    
    /** a color for selection backgrounds */
    private Color cSelection = new JTable().getSelectionBackground();
    
    /**
     * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      // node!
      Node node = (Node)value;
      // blueprint?
      if (node.blueprint!=null) {
        button.setOpaque(selected);
        button.setBackground(cSelection);
        button.setText(node.blueprint.getName());
        button.setSelected(selection[node.type]==node.blueprint);
        // done
        return button; 
      }
      // no blueprint -> show type
      label.setOpaque(selected);
      label.setBackground(cSelection);
      label.setText(Gedcom.getNameFor(node.type, true));
      label.setIcon(Gedcom.getImage(node.type));
      // done
      return label;
    }
    
    /**
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent e) {
      
      // commit editor's changes
      editor.commit();
      
      // selection?
      if (e.getNewLeadSelectionPath()!=null) {

        // analyse node
        Node node = (Node)e.getNewLeadSelectionPath().getLastPathComponent();
        
        // different Blueprint selected -> o.k.
        if (node.blueprint!=null) {
          // .. update selection
          selection[node.type] = node.blueprint;
          // .. gotta repaint for old
          treeBlueprints.repaint();
          // .. buttons
          bAdd.setEnabled(true);
          bDel.setEnabled(!node.blueprint.isReadOnly());
          // .. editor
          editor.set(gedcom, node.blueprint, !node.blueprint.isReadOnly());
          return;
        }
      
        // different type section selected 
        bAdd.setEnabled(true);
        bDel.setEnabled(false);

      } else {

        bAdd.setEnabled(false);
        bDel.setEnabled(false);
        
      }
            
      // .. editor
      editor.set(null, null, false);

      // done
    }
    
  } // Glue
    
  
  /**
   * A Node in our List(Tree)   */
  private class Node extends DefaultMutableTreeNode {
    
    /** the type of entity this if for */
    private int type;
    
    /** an optional blueprint */
    private Blueprint blueprint;
    
    /**
     * Constructor - Root
     */
    Node() {
      // create a sub-note for every entity type
      for (int t=0; t<Gedcom.NUM_TYPES; t++)
        add(new Node(t));
      // done
    }
    /**
     * Constructor - List of blueprints
     */
    Node(int tYpe) {
      // remember type
      type = tYpe;
      // create a sub-note for every blueprint
      List bps = bpManager.getBlueprints(type);      
      for (int b=0; b<bps.size(); b++)
        add(new Node(type,(Blueprint)bps.get(b)));
      // done
    }
    /**
     * Constructor - Blueprint
     */
    Node(int tYpe, Blueprint bluEprint) {
      type = tYpe;
      blueprint = bluEprint;
    }
  } //Node   

} //BluePrintList
