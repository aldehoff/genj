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
import genj.util.swing.ListWidget;
import genj.util.swing.ColorChooser;
import genj.util.swing.DoubleValueSlider;
import genj.util.swing.FontChooser;
import genj.view.Settings;

import java.awt.Container;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * The settings component for the Tree View */
public class TreeViewSettings extends JTabbedPane implements Settings, genj.tree.ModelListener {

  /** keeping track of tree these settings are for */
  private TreeView view;

  /** sliders for box size */
  private DoubleValueSlider 
    sliderCmIndiWidth, 
    sliderCmIndiHeight,
    sliderCmFamWidth,
    sliderCmFamHeight,
    sliderCmPadding;
  
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
    
    sliderCmIndiWidth = createSlider(options, 1.0, 16.0, "indiwidth" );
    sliderCmIndiHeight= createSlider(options, 0.4, 16.0, "indiheight");
    sliderCmFamWidth  = createSlider(options, 1.0, 16.0, "famwidth"  );
    sliderCmFamHeight = createSlider(options, 0.4, 16.0, "famheight" );
    sliderCmPadding   = createSlider(options, 0.1,  4.0, "padding"   );
    
    // color chooser
    colors = new ColorChooser();
    
    // blueprint options
    blueprintList = new BlueprintList();
    
    // bookmarks
    Box bookmarks = new Box(BoxLayout.Y_AXIS);
    bookmarkList = new ListWidget();
    bookmarks.add(new JScrollPane(bookmarkList));
    JPanel bookmarkActions = new JPanel();
    ButtonHelper bh = new ButtonHelper().setContainer(bookmarkActions);
    final AbstractButton b = bh.setEnabled(false).create(new ActionBDelete());
    bookmarkList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        b.setEnabled(!bookmarkList.isSelectionEmpty());
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
   * Create a slider
   */
  private DoubleValueSlider createSlider(Container c, double min, double max, String key) {
    // create and preset
    DoubleValueSlider result = new DoubleValueSlider(min, max, (max+min)/2, false);
    result.setPreferredSliderWidth(128);
    result.setAlignmentX(0F);
    result.setText(resources.getString("info."+key));
    result.setToolTipText(resources.getString("info."+key+".tip"));
    c.add(result);
  
    // done
    return result;   }
  
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
    bookmarkList.setModel(new BookmarkListModel());
    // start listening to new?
    view.getModel().addListener(this);
    // done
  }
  
  /**
   * @see genj.tree.ModelListener#bookmarksChanged(genj.tree.Model)
   */
  public void bookmarksChanged(Model model) {
    bookmarkList.setModel(new BookmarkListModel());
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
    // metrics
    view.getModel().setMetrics(new TreeMetrics(
      (int)Math.rint(sliderCmIndiWidth .getValue()*10),
      (int)Math.rint(sliderCmIndiHeight.getValue()*10),
      (int)Math.rint(sliderCmFamWidth  .getValue()*10),
      (int)Math.rint(sliderCmFamHeight .getValue()*10),
      (int)Math.rint(sliderCmPadding   .getValue()*10)
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
    // metrics
    TreeMetrics m = view.getModel().getMetrics();
    sliderCmIndiWidth .setValue(m.wIndis/10D);
    sliderCmIndiHeight.setValue(m.hIndis/10D);
    sliderCmFamWidth  .setValue(m.wFams /10D);
    sliderCmFamHeight .setValue(m.hFams /10D);
    sliderCmPadding   .setValue(m.pad   /10D);
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
   * Action - delete a bookmark
   */
  private class ActionBDelete extends ActionDelegate {
    /**
     * Constructor
     */
    private ActionBDelete() {
      setText(resources.getString("bookmark.del"));
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      Object[] bs = bookmarkList.getSelectedValues();
      for (int b=0; b<bs.length; b++) {
        view.getModel().delBookmark((Bookmark)bs[b]);
      }
    }
  } //ActionBDelete
  
  /**
   * BookmarkListModel
   */
  private class BookmarkListModel extends AbstractListModel {
    /**
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public Object getElementAt(int index) {
      return view.getModel().getBookmarks().get(index); 
    }
    /**
     * @see javax.swing.ListModel#getSize()
     */
    public int getSize() {
      return view.getModel().getBookmarks().size();
    }
  } //BookmarkListModel

} //TreeViewSettings
