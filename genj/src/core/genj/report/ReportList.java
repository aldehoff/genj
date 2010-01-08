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
package genj.report;

import genj.util.Registry;
import genj.util.Resources;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Report list capable of displaying the report list, info and settings Reports
 * are either sorted alphabetically or it is a tree with reports within their
 * categories.
 */
/* package */class ReportList extends JTree {

  /**
   * Object with callback functions.
   */
  private Callback callback = new Callback();

  /**
   * Listener for changes in the currently selected report.
   */
  private ReportSelectionListener selectionListener = null;

  /**
   * Data model for displaying grouped reports.
   */
  private TreeModel treeModel = null;

  /**
   * Data model for displaying reports in a list (not grouped).
   */
  private TreeModel listModel = null;

  /**
   * Registry for storing configuration.
   */
  private Registry registry;

  /**
   * Language resources.
   */
  private static final Resources RESOURCES = Resources.get(ReportView.class);
  
  private boolean byGroup;

  /**
   * Creates the component.
   */
  public ReportList(Report[] reports, boolean byGroup) {
    
    this.byGroup = byGroup;

    setReports(reports);
    setVisibleRowCount(3);
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    setCellRenderer(callback);
    addTreeSelectionListener(callback);
    addTreeExpansionListener(callback);
    setRootVisible(false);

    // done
  }

  /**
   * Sets the given report as selected.
   */
  public void setSelection(Report report) {
    if (report == null) {
      clearSelection();
    } else {
      for (int i = 0; i < getRowCount(); i++) {
        TreePath path = getPathForRow(i);
        Object v = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
        if (v == report) {
          addSelectionPath(path);
          makeVisible(path);
          break;
        }
      }
    }
  }

  /**
   * Returns the currently selected report.
   */
  public Report getSelection() {
    TreePath selection = getSelectionPath();
    return selection!=null ? (Report)((DefaultMutableTreeNode)selection.getLastPathComponent()).getUserObject() : null;
  }

  /**
   * Sets the selection listener.
   */
  public void setSelectionListener(ReportSelectionListener listener) {
    selectionListener = listener;
  }

  /**
   * Sets a new list of reports.
   */
  public void setReports(Report[] reports) {
    
    setModel(byGroup ? createTree(reports) : createList(reports));
  }

  /**
   * Expands the groups which are configured in the registry to be expanded.
   */
  private void refreshExpanded() {
    for (int i = 0; i < getRowCount(); i++) {
      TreePath path = getPathForRow(i);
      Object v = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
      if (v instanceof Report.Category) {
        Report.Category category = (Report.Category) v;
        if (registry.get("expanded." + category.getName(), true))
          expandPath(path);
        else
          collapsePath(path);
      }
    }
  }

  /**
   * Creates the list data model.
   */
  private TreeModel createList(Report[] reports) {
    DefaultMutableTreeNode top = new DefaultMutableTreeNode();
    for (int i = 0; i < reports.length; i++)
      top.add(new DefaultMutableTreeNode(reports[i]));
    return new DefaultTreeModel(top);
  }

  /**
   * Creates the group tree data model.
   */
  private TreeModel createTree(Report[] reports) {
    SortedMap<String, CategoryList> categories = new TreeMap<String, CategoryList>();
    for (int i = 0; i < reports.length; i++) {
      String name = getCategoryText(reports[i].getCategory());
      CategoryList list = (CategoryList) categories.get(name);
      if (list == null) {
        list = new CategoryList(reports[i].getCategory());
        categories.put(name, list);
      }
      list.add(reports[i]);
    }

    DefaultMutableTreeNode top = new DefaultMutableTreeNode();
    for (CategoryList list : categories.values()) {
      DefaultMutableTreeNode cat = new DefaultMutableTreeNode(list.getCategory());
      Report[] reps = list.getReportsInCategory();
      for (int i = 0; i < reps.length; i++)
        cat.add(new DefaultMutableTreeNode(reps[i]));
      top.add(cat);
    }
    return new DefaultTreeModel(top);
  }

  /**
   * Returns the translated category name.
   */
  private String getCategoryText(Report.Category category) {
    String resourceName = "category." + category.getName();
    String text = RESOURCES.getString(resourceName);
    if (text.equals(resourceName))
      text = category.getName();
    return text;
  }

  /**
   * A private callback for various messages coming in.
   */
  private class Callback implements TreeCellRenderer, TreeSelectionListener, TreeExpansionListener {
    /**
     * a default renderer for tree
     */
    private DefaultTreeCellRenderer defTreeRenderer = new DefaultTreeCellRenderer();

    /**
     * Return component for rendering tree element
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean isExpanded, boolean isLeaf, int index, boolean hasFocus) {
      defTreeRenderer.getTreeCellRendererComponent(tree, value, isSelected, isExpanded, isLeaf, index, hasFocus);
      Object v = ((DefaultMutableTreeNode) value).getUserObject();
      if (v instanceof Report) {
        Report report = (Report) v;
        defTreeRenderer.setText(report.getName());
        defTreeRenderer.setIcon(report.getImage());
      } else if (v instanceof Report.Category) {
        Report.Category category = (Report.Category) v;
        defTreeRenderer.setText(getCategoryText(category));
        defTreeRenderer.setIcon(category.getImage());
      }

      return defTreeRenderer;
    }

    /**
     * Monitors changes to selection of reports.
     */
    public void valueChanged(TreeSelectionEvent e) {
      Report selection = null;
      TreePath path = getSelectionPath();
      if (path != null) {
        Object v = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
        if (v instanceof Report)
          selection = (Report) v;
      }
      if (selectionListener != null)
        selectionListener.valueChanged(selection);
    }

    /**
     * Saves the expansion state of groups in the registry.
     */
    public void treeExpanded(TreeExpansionEvent e) {
      Object v = ((DefaultMutableTreeNode) e.getPath().getLastPathComponent()).getUserObject();
      if (v instanceof Report.Category) {
        Report.Category category = (Report.Category) v;
        registry.put("expanded." + category.getName(), true);
      }
    }

    /**
     * Saves the expansion state of groups in the registry.
     */
    public void treeCollapsed(TreeExpansionEvent e) {
      Object v = ((DefaultMutableTreeNode) e.getPath().getLastPathComponent()).getUserObject();
      if (v instanceof Report.Category) {
        Report.Category category = (Report.Category) v;
        registry.put("expanded." + category.getName(), false);
      }
    }
  }

  /**
   * List of reports in a category.
   */
  private class CategoryList {
    private Report.Category category;
    private List<Report> reportsInCategory = new ArrayList<Report>();

    public CategoryList(Report.Category category) {
      this.category = category;
    }

    public Report.Category getCategory() {
      return category;
    }

    public Report[] getReportsInCategory() {
      return (Report[]) reportsInCategory.toArray(new Report[reportsInCategory.size()]);
    }

    public void add(Report report) {
      reportsInCategory.add(report);
    }
  }
}
