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
package genj.tree;

import genj.renderer.BlueprintList;
import genj.util.ActionDelegate;
import genj.util.ColorSet;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ColorChooser;
import genj.util.swing.FontChooser;
import genj.util.swing.ListWidget;
import genj.util.swing.SpinnerWidget;
import genj.view.Settings;

import java.awt.Container;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * The settings component for the Tree View */
public class TreeViewSettings extends JTabbedPane implements Settings, genj.tree.ModelListener {

  /** keeping track of tree these settings are for */
  private TreeView view;

  /** models for spinners */
  private SpinnerWidget.FractionModel[] spinModels = new SpinnerWidget.FractionModel[5]; 
  
  /** colorchooser for colors */
  private ColorChooser colors;
  
  /** blueprintlist */
  private BlueprintList blueprintList;
  
  /** resources */
  private Resources resources = Resources.get(this);

  /** Checkboxes */
  private JCheckBox 
    checkBending = new JCheckBox(resources.getString("bend" )),
    checkAntialiasing = new JCheckBox(resources.getString("antialiasing" )),
    checkAdjustFonts = new JCheckBox(resources.getString("adjustfonts" )),
    checkMarrSymbols = new JCheckBox(resources.getString("marrsymbols" ))
  ;
  
  /** buttons */
  private AbstractButton 
    bUp, bDown, bDelete;
  
  /** font chooser */
  private FontChooser fontChooser = new FontChooser();
  
  /** bookmark list */
  private JList bookmarkList;


  /**
   * Constructor   */
  public TreeViewSettings() {
    
    // panel for checkbox options    
    Box options = new Box(BoxLayout.Y_AXIS);

    checkBending.setToolTipText(resources.getString("bend.tip"));
    options.add(checkBending);
    checkAntialiasing.setToolTipText(resources.getString("antialiasing.tip"));
    options.add(checkAntialiasing);
    checkAdjustFonts.setToolTipText(resources.getString("adjustfonts.tip"));
    options.add(checkAdjustFonts);
    checkMarrSymbols.setToolTipText(resources.getString("marrsymbols.tip"));
    options.add(checkMarrSymbols);
    
    options.add(fontChooser);    
    
    spinModels[0] = createSpinner("indiwidth",  options, 1.0, 16.0);
    spinModels[1] = createSpinner("indiheight", options, 0.4, 16.0);
    spinModels[2] = createSpinner("famwidth",   options, 1.0, 16.0);
    spinModels[3] = createSpinner("famheight",  options, 0.4, 16.0);
    spinModels[4] = createSpinner("padding",    options, 1.0,  4.0);
    
    // color chooser
    colors = new ColorChooser();
    
    // blueprint options
    blueprintList = new BlueprintList();
    
    // bookmarks
    Box bookmarks = new Box(BoxLayout.Y_AXIS);
    bookmarkList = new ListWidget();
    bookmarkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    bookmarks.add(new JScrollPane(bookmarkList));
    
    JPanel bookmarkActions = new JPanel();
    ButtonHelper bh = new ButtonHelper().setContainer(bookmarkActions).setEnabled(false);
    bUp     = bh.create(new ActionMove(-1));
    bDown   = bh.create(new ActionMove( 1));
    bDelete = bh.create(new ActionDelete());
    bookmarkList.addListSelectionListener(new ListSelectionListener() {
      /** update buttons */
      public void valueChanged(ListSelectionEvent e) {
        int 
          i = bookmarkList.getSelectedIndex(),
          n = bookmarkList.getModel().getSize();
      
        bUp.setEnabled(i>0);
        bDown.setEnabled(i>=0&&i<n-1);
        bDelete.setEnabled(i>=0);
      }
    });
    bookmarks.add(bookmarkActions);
    
    // add those tabs
    add(resources.getString("page.main")  , options);
    add(resources.getString("page.colors"), colors);
    add(resources.getString("page.bookmarks"), bookmarks);
    add(resources.getString("page.blueprints"), blueprintList);
    
    // done
  }
  
  /**
   * Create a spinner
   */
  private SpinnerWidget.FractionModel createSpinner(String key, Container c, double min, double max) {
    
    // prepare data
    String 
      txt = resources.getString("info."+key),
      tip = resources.getString("info."+key+".tip");

    // prepare format
    NumberFormat format = NumberFormat.getInstance();
    format.setMinimumFractionDigits(1);
    format.setMaximumFractionDigits(1);

    // create
    SpinnerWidget.FractionModel result = new SpinnerWidget.FractionModel(min, max, 1);
        
    SpinnerWidget sw = new SpinnerWidget(txt, 5, result);
    sw.setToolTipText(tip);
    sw.setFormat(format);
    c.add(sw);
    
    // done
    return result;
  }
  
  /**
   * @see genj.view.Settings#setView(javax.swing.JComponent)
   */
  public void setView(JComponent viEw) {
    // stop listening to old?
    if (view!=null) view.getModel().removeListener(this);
    // remember
    view = (TreeView)viEw;
    // update characteristics
    colors.setColorSets(new ColorSet[]{view.colors});
    blueprintList.setGedcom(view.getModel().getGedcom());
    // start listening to new?
    view.getModel().addListener(this);
    // done
  }
  
  /**
   * @see genj.tree.ModelListener#nodesChanged(genj.tree.Model, java.util.List)
   */
  public void nodesChanged(Model model, List nodes) {
  }

  /**
   * @see genj.tree.ModelListener#structureChanged(genj.tree.Model)
   */
  public void structureChanged(Model model) {
  }

  /**
   * @see genj.view.ApplyResetSupport#apply()
   */
  public void apply() {
    // options
    view.getModel().setBendArcs(checkBending.isSelected());
    view.setAntialiasing(checkAntialiasing.isSelected());
    view.setAdjustFonts(checkAdjustFonts.isSelected());
    view.setContentFont(fontChooser.getSelectedFont());
    view.getModel().setMarrSymbols(checkMarrSymbols.isSelected());
    // colors
    colors.apply();
    // bookmarks
    List bookmarks = new ArrayList();
    ListModel list = bookmarkList.getModel();
    for (int i=0;i<list.getSize();i++) {
      bookmarks.add(list.getElementAt(i));
    }
    view.getModel().setBookmarks(bookmarks);
    // metrics
    view.getModel().setMetrics(new TreeMetrics(
      (int)(spinModels[0].getDoubleValue()*10),
      (int)(spinModels[1].getDoubleValue()*10),
      (int)(spinModels[2].getDoubleValue()*10),
      (int)(spinModels[3].getDoubleValue()*10),
      (int)(spinModels[4].getDoubleValue()*10)
    ));
    // blueprints
    view.setBlueprints(blueprintList.getSelection());
    // done
  }

  /**
   * @see genj.view.ApplyResetSupport#reset()
   */
  public void reset() {
    // options
    checkBending.setSelected(view.getModel().isBendArcs());
    checkAntialiasing.setSelected(view.isAntialising());
    checkAdjustFonts.setSelected(view.isAdjustFonts());
    fontChooser.setSelectedFont(view.getContentFont());
    checkMarrSymbols.setSelected(view.getModel().isMarrSymbols());
    // colors
    colors.reset();
    // bookmarks
    bookmarkList.setModel(new DefaultComboBoxModel(view.getModel().getBookmarks().toArray()));
    // metrics
    TreeMetrics m = view.getModel().getMetrics();
    int[] values = new int[] {
      m.wIndis, m.hIndis, m.wFams, m.hFams, m.pad   
    };
    for (int i=0;i<values.length;i++) {
      spinModels[i].setDoubleValue(values[i]*0.1D);
    }
    // blueprints
    blueprintList.setSelection(view.getBlueprints());
    // done
  }
  
  /**
   * @see genj.view.Settings#getEditor()
   */
  public JComponent getEditor() {
    return this;
  }

  /**
   * Action - move a bookmark
   */
  private class ActionMove extends ActionDelegate {
    /** by how much to move */
    private int by;
    /**
     * Constructor
     */
    private ActionMove(int how) {
      setText(resources.getString("bookmark.move."+how));
      by = how;
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      int i = bookmarkList.getSelectedIndex();
      DefaultComboBoxModel model = (DefaultComboBoxModel)bookmarkList.getModel();
      Object bookmark = model.getElementAt(i);
      model.removeElementAt(i);
      model.insertElementAt(bookmark, i+by);
      bookmarkList.setSelectedIndex(i+by);
    }
  } //ActionMove
  
  /**
   * Action - delete a bookmark
   */
  private class ActionDelete extends ActionDelegate {
    /**
     * Constructor
     */
    private ActionDelete() {
      setText(resources.getString("bookmark.del"));
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      int i = bookmarkList.getSelectedIndex();
      if (i>=0)
        ((DefaultComboBoxModel)bookmarkList.getModel()).removeElementAt(i);
    }
  } //ActionDelete
  
} //TreeViewSettings
