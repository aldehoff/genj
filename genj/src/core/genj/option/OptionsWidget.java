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
package genj.option;

import genj.util.swing.ImageIcon;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import swingx.tree.AbstractTreeModel;

/**
 * A widget for displaying options in tabular way
 */
public class OptionsWidget extends JPanel {

  /** an image for options */
  public final static ImageIcon IMAGE = new ImageIcon(OptionsWidget.class, "images/Options.gif");

  /** tree we're using */
  private JTree tree;
  
  /** reference to window manager */
  private WindowManager manager;
  
  /** tree model */
  private Model model = new Model();
  
  /** first column width */
  private int widthOf1stColumn = 32;
  
  /**
   * Constructor
   */
  public OptionsWidget(WindowManager manager) {
    this(manager, null);
  }
  
  /**
   * Constructor
   */
  public OptionsWidget(WindowManager manager, List options) {

    this.manager = manager;
        
    // setup
    tree = new JTree(model) {
      public boolean isPathEditable(TreePath path) {
        return path.getLastPathComponent() instanceof Option;
      }
    };
    tree.setShowsRootHandles(false);
    tree.setRootVisible(false);
    tree.setCellRenderer(new Cell());
    tree.setCellEditor(new Cell());
    tree.setEditable(true);
    tree.setInvokesStopCellEditing(true);
     
    // layout
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new JScrollPane(tree));    
    
    // options?
    if (options!=null)
      setOptions(options);

    // done
  }
  
  /**
   * callback - lifecycle remove
   */
  public void removeNotify() {
    // make sure any edit is stopped
    tree.stopEditing();
    // continue
    super.removeNotify();
  }
  
  /**
   * Set options to display
   */
  public void setOptions(List set) {
    
    // check options - we don't keep any without ui
    ListIterator it = set.listIterator();
    while (it.hasNext()) {
      Option option = (Option)it.next();
      if (option.getUI(this)==null)
        it.remove();
    }
    
    // calculate longest width of option name
    FontRenderContext ctx = new FontRenderContext(null,false,false);
    Font font = tree.getFont();
    widthOf1stColumn = 0;
    for (int i = 0; i < set.size(); i++) {
      Option option = (Option)set.get(i);
      widthOf1stColumn = Math.max(widthOf1stColumn, (int)Math.ceil(font.getStringBounds(option.getName(), ctx).getWidth()));
    }
    
    // tell to model
    model.setOptions(set);
    
    // unfold all
    for (int i=0;i<tree.getRowCount();i++)
      tree.expandRow(i); 
    
    // layout
    doLayout();
  }

  /**
   * Access to window manager
   */  
  public WindowManager getWindowManager() {
    return manager;
  } 
  
  /**
   * A cell user either temporarily as renderer or editor
   */
  private class Cell extends AbstractCellEditor implements TreeCellRenderer, TreeCellEditor {
    
    /** current ui */
    private OptionUI ui;
    
    /** panel container */
    private JPanel panel = new JPanel();
    
    /** label for option name */
    private JLabel labelForName = new JLabel();
    
    /** label for option value */
    private JLabel labelForValue = new JLabel();
    
    /**
     * constructor
     */
    private Cell() {
      panel.setOpaque(false);
      panel.setLayout(new BorderLayout());
      panel.add(labelForName, BorderLayout.WEST);
    }
    /**
     * callback - component generation
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      // option?
      if (value instanceof Option) 
        return assemblePanel((Option)value, false);
      // must be string
      return new JLabel(value.toString());
    }

    /**
     * assemble the editor/renderer panel
     */
    private JPanel assemblePanel(Option option, boolean forceUI) {
      // remove old
      if (panel.getComponentCount()>1) 
        panel.remove(1);
      // lookup option and ui
      ui = option.getUI(OptionsWidget.this);
      // prepare name
      labelForName.setText(option.getName());
      labelForName.setPreferredSize(new Dimension(widthOf1stColumn,16));
      // and value (either text or ui)
      JComponent compForValue;
      String text = ui.getTextRepresentation();
      if (text!=null&&!forceUI) {
        labelForValue.setText(text);
        compForValue = labelForValue;
      } else {
        compForValue = ui.getComponentRepresentation();
      }
      panel.add(compForValue, BorderLayout.CENTER);
      
      // done
      return panel;
    }

    /**
     * callback - call for editor component
     */
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
      return assemblePanel((Option)value, true);
    }
    
    /**
     * callback - the resulting value
     */
    public Object getCellEditorValue() {
      return null;
    }

    /** callback - cancel editing */     
    public void cancelCellEditing() {
      ui = null;
      super.cancelCellEditing();
    }
  
    /** callback - stop editing = commit */     
    public boolean stopCellEditing() {
      if (ui!=null)
        ui.endRepresentation();
      return super.stopCellEditing();
    }

  } //Cell

  /** 
   * Model
   */
  private class Model extends AbstractTreeModel {
    
    /** top-level children */
    private List categories = new ArrayList();
    private Map cat2options = new HashMap();
  
    /**
     * the parent of options is the root (this)
     */
    protected Object getParent(Object node) {
      throw new IllegalArgumentException();
    }
    
    private List getCategory(String cat) {
      List result = (List)cat2options.get(cat);
      if (result==null) {
        result = new ArrayList();
        cat2options.put(cat, result);
        if (cat!=null)
          categories.add(cat);
      }
      return result;
    }
  
    /**
     * Set options to display
     */
    private void setOptions(List set) {
      
      // parse anew
      cat2options.clear();
      categories.clear();
      
      HashMap categories = new HashMap();
      for (int i = 0; i < set.size(); i++) {
        Option option = (Option)set.get(i);
        String cat = option.getCategory();
        getCategory(cat).add(option);
      }
      
      // notify
      fireTreeStructureChanged(this, new TreePath(this), null, null);
    }
    
    /**
     * the root is this
     */
    public Object getRoot() {
      return this;
    }
  
    /**
     * children are all options
     */
    public int getChildCount(Object parent) {
      if (parent==this)
        return getCategory(null).size() + cat2options.size()-1;
      return getCategory((String)parent).size();
    }
  
    /**
     * options are leafs
     */
    public boolean isLeaf(Object node) {
      return node instanceof Option;
    }
  
    /**
     * option by index
     */
    public Object getChild(Object parent, int index) {
      if (parent==this) {
        List toplevel = getCategory(null);
        if (index<toplevel.size())
          return toplevel.get(index);
        return categories.get(index - toplevel.size());
      }
      return getCategory((String)parent).get(index);
    }
  
    /**
     * reverse index lookup
     */
    public int getIndexOfChild(Object parent, Object child) {
      throw new IllegalArgumentException();
    }
    
  } //Model

} //OptionsWidget
