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

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import genj.gedcom.*;
import awtx.*;
import genj.app.*;
import genj.util.GridBagHelper;
import genj.option.*;
import genj.util.Resources;

/**
 * Class for providing PInfo information to a ViewEditor
 */
public class TreeViewSettings extends ViewSettingsWidget {

  private TreeView   tree;

  private int entity = Gedcom.INDIVIDUALS;

  private final static int[] filter = {
    Gedcom.INDIVIDUALS,
    Gedcom.FAMILIES
  };

  private JComboBox comboTypes = new JComboBox();
  private Scala scalaZoom = new Scala();
  private JList listBookmarks = new JList();
  private JCheckBox[] checkPaints = {
    new JCheckBox(resources.getString("options.show_shadows")),
    new JCheckBox(resources.getString("options.show_images" )),
    new JCheckBox(resources.getString("options.zoom_images" )),
    new JCheckBox(resources.getString("options.vertical")),
    new JCheckBox(resources.getString("options.abbreviate_dates"))
  };

  private TagPathTree pathTree = new TagPathTree();
  private OptionLayoutProperties layoutProperties = new OptionLayoutProperties(null);
  private JButton
    delBookmark = new JButton(resources.getString("bookmarks.delete")),
    addBookmark = new JButton(resources.getString("bookmarks.add")),
    upBookmark  = new JButton(resources.getString("bookmarks.up")),
    downBookmark= new JButton(resources.getString("bookmarks.down"));

  private JTextField labelBookmark = new JTextField();

  private EntitySelector entityBookmark = new EntitySelector();

  private JComboBox comboFont = new JComboBox();
  private final static Resources resources = new Resources("genj.tree");
  private JTextField textFont = new JTextField();

  /**
   * Constructor
   */
  public TreeViewSettings(TreeView tree) {
    
    // initial layout
    JTabbedPane tabbed = new JTabbedPane();
    setLayout(new BorderLayout());
    add(tabbed, BorderLayout.CENTER);
    
    // remember
    this.tree = tree;

    // Create a listener
    ActionListener alistener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      if ("ADD".equals(e.getActionCommand()))
        addBookmark();
      if ("DEL".equals(e.getActionCommand()))
        delBookmark();
      if ("UP".equals(e.getActionCommand()))
        moveBookmark(true);
      if ("DOWN".equals(e.getActionCommand()))
        moveBookmark(false);
      if ("TYPE".equals(e.getActionCommand()))
        changeType();
      }
    };
    addBookmark .addActionListener(alistener);
    delBookmark .addActionListener(alistener);
    upBookmark  .addActionListener(alistener);
    downBookmark.addActionListener(alistener);
    comboTypes  .addActionListener(alistener);
    addBookmark .setActionCommand("ADD");
    delBookmark .setActionCommand("DEL");
    upBookmark  .setActionCommand("UP");
    downBookmark.setActionCommand("DOWN");
    comboTypes  .setActionCommand("TYPE");

    TagPathTreeListener tlistener = new TagPathTreeListener() {
      // LCD
      public void handleSelection(TagPath p, boolean on) {
        if (on) {
          layoutProperties.add(p,null);
        } else {
          layoutProperties.remove(p);
        }
      }
      // EOC
    };
    pathTree.addTagPathTreeListener(tlistener);

    // Create Components
    for (int i=0;i<filter.length;i++) {
      comboTypes.addItem(Gedcom.getNameFor(filter[i],true));
    }
    entityBookmark.setFilter(filter);
    Insets ins = new Insets(0,0,0,0);
    addBookmark.setMargin(ins);
    delBookmark.setMargin(ins);
    upBookmark.setMargin(ins);
    downBookmark.setMargin(ins);

    Border defaultBorder = BorderFactory.createEmptyBorder(8,8,8,8);

    // Properties
    JPanel panel = new JPanel();panel.setBorder(defaultBorder);

      GridBagHelper helper = new GridBagHelper(panel);
      int row = 0;

      helper.add(new JLabel(resources.getString("properties.show")),0,row++,2,1);
      helper.addFiller(0,row,new Dimension(16,16));
      helper.add(comboTypes,1,row++,1,1,helper.GROW_HORIZONTAL|helper.FILL_HORIZONTAL);
      helper.add(pathTree,1,row++,1,1,helper.GROW_BOTH|helper.FILL_BOTH);
      helper.add(layoutProperties,1,row++,1,1,helper.GROW_BOTH|helper.FILL_BOTH);

    tabbed.add(resources.getString("page.properties"),panel);

    // Options
    panel = new JPanel();panel.setBorder(defaultBorder);

      helper = new GridBagHelper(panel);
      row = 0;

      helper.add(new JLabel(resources.getString("options.zoom")),0,row++,2,1);
      helper.addFiller(0,row,new Dimension(16,1));
      helper.add(scalaZoom,1,row++,1,1,helper.FILL_HORIZONTAL);

      helper.add(new JLabel(resources.getString("options.display")),0,row++,2,1);
      for (int i=0;i<checkPaints.length;i++) {
        helper.add(checkPaints[i],1,row++,1,1);
      }

      helper.add(new JLabel(resources.getString("options.font")),0,row++,2,1);
      helper.add(comboFont,1,row++,1,1);
      helper.add(textFont,1,row++,1,1);

    tabbed.add(resources.getString("page.options"),panel);

    // Bookmarks
    panel = new JPanel();panel.setBorder(defaultBorder);

      helper = new GridBagHelper(panel);
      row = 0;

      helper.add(entityBookmark                ,1,row++,5,1);
      helper.add(labelBookmark                 ,1,row++,5,1);

      helper.add(addBookmark                   ,1,row  ,1,1,helper.FILL_NONE);
      helper.add(delBookmark                   ,2,row  ,1,1,helper.FILL_NONE);
      helper.add(new JLabel()                  ,3,row  ,1,1,helper.GROW_HORIZONTAL);
      helper.add(upBookmark                    ,4,row  ,1,1,helper.FILL_NONE);
      helper.add(downBookmark                  ,5,row++,1,1,helper.FILL_NONE);

      helper.add(new JScrollPane(listBookmarks),1,row++,5,1,helper.GROW_BOTH|helper.FILL_BOTH);

    tabbed.add(resources.getString("page.bookmarks"),panel);

    // Done
  }

  /**
   * Adds a bookmark
   */
  private void addBookmark() {

    // Get information
    Entity entity = entityBookmark.getEntity();
    String label  = labelBookmark.getText().trim();

    if ((entity==null)||(label.length()==0)) {
      return;
    }

    // Add new bookmark
    ListModel old_model = listBookmarks.getModel();
    DefaultListModel new_model = new DefaultListModel();

    for (int i=0;i<old_model.getSize();i++) {
      new_model.addElement(old_model.getElementAt(i));
    }

    new_model.addElement(new Bookmark(entity,label));

    listBookmarks.setModel(new_model);

    // Done
  }

  /**
   * Changes the type to the current ComboBox selection
   */
  private void changeType() {
    int i = comboTypes.getSelectedIndex();
    if (i==-1) {
      return;
    }
    entity = filter[i];
    reset();
  }

  /**
   * Removes a bookmark
   */
  private void delBookmark() {

    // Get information
    int i = listBookmarks.getSelectedIndex();
    if (i<0) {
      return;
    }

    // Remove bookmark
    DefaultListModel model = (DefaultListModel)listBookmarks.getModel();
    model.removeElementAt(i);

    // Done
  }

  /**
   * Has to return the component used fir editing
   */
  public Component getEditor() {
    return this;
  }

  /**
   * Moves a bookmark up/down
   */
  private void moveBookmark(boolean up) {

    int i = listBookmarks.getSelectedIndex();
    int j = up ? i-1 : i+1;

    ListModel model = listBookmarks.getModel();
    Bookmark[] bms = new Bookmark[model.getSize()];

    if ( (i<0) || (j<0) || (j>=bms.length) ) {
      return;
    }

    for (int b=0;b<bms.length;b++) {
      bms[b] = (Bookmark)model.getElementAt(b);
    }

    Bookmark bm = bms[i];
    bms[i] = bms[j];
    bms[j] = bm;

    listBookmarks.setListData(bms);
    listBookmarks.setSelectedIndex(j);

    //  Done
  }

  /**
   * Tells the ViewInfo to reset made changes
   */
  public void reset() {

    // ?
    if (tree==null) {
      return;
    }

    // Clear reference to table for listeners
    TreeView t = tree;
    tree = null;

    // Read options
    checkPaints[0].setSelected(t.isShadow());
    checkPaints[1].setSelected(t.isPropertyImages());
    checkPaints[2].setSelected(t.isZoomBlobs());
    checkPaints[3].setSelected(t.isVertical());
    checkPaints[4].setSelected(t.isAbbreviateDates());

    scalaZoom.setValue(t.getZoom());

    String fontName = t.getModel().getFont().getName();
    int fontSize    = t.getModel().getFont().getSize();

    String[] sysFonts = Toolkit.getDefaultToolkit().getFontList();
    Vector ourFonts = new Vector(sysFonts.length+1);
    ourFonts.addElement(fontName);
    for (int f=0;f<sysFonts.length;f++) {
      if (!sysFonts[f].equals(fontName)) {
        ourFonts.addElement(sysFonts[f]);
      }
    }
    comboFont.setModel(new DefaultComboBoxModel(ourFonts));
    textFont.setText(""+fontSize);

    // Read bookmarks
    DefaultListModel model = new DefaultListModel();

    Enumeration bs = t.getBookmarks().elements();
    while (bs.hasMoreElements()) {
      model.addElement(bs.nextElement());
    }
    listBookmarks.setModel(model);

    // Reflect properties by TagPaths
    TagPath[] selectedPaths = t.getTagPaths(entity);
    TagPath[] usedPaths     = TagPath.getUsedTagPaths(t.getGedcom(),entity);

    pathTree.setPaths(usedPaths);
    pathTree.setSelection(selectedPaths);

    entityBookmark.setGedcom(t.getGedcom());

    layoutProperties.setFont(t.getFont());
    layoutProperties.setSizeOfEntities(t.getSize(entity));
    layoutProperties.removeAll();
    Proxy[] proxies = t.getProxies(entity);
    for (int i=0;i<proxies.length;i++) {
      Proxy proxy = proxies[i];
      layoutProperties.add(proxy.getPath(),proxy.getBox());
    }

    // Show table again
    tree = t;

    // Done
  }

  /**
   * Tells the ViewInfo to apply made changes
   */
  public void apply() {

    // Write Options
    tree.setShadow         (checkPaints[0].isSelected());
    tree.setPropertyImages (checkPaints[1].isSelected());
    tree.setZoomBlobs      (checkPaints[2].isSelected());
    tree.setVertical       (checkPaints[3].isSelected());
    tree.setAbbreviateDates(checkPaints[4].isSelected());
    tree.setZoom           (scalaZoom.getValue());

    int size = 10; try { size = Integer.parseInt(textFont.getText()); } catch (Exception e) {}
    tree.getModel().setFont(new Font(comboFont.getSelectedItem().toString(),Font.PLAIN,size));
    textFont.setText(""+size);

    // Write Bookmarks
    ListModel model = listBookmarks.getModel();
    Vector bs = new Vector(model.getSize());
    for (int i=0;i<model.getSize();i++)
      bs.addElement(model.getElementAt(i));
    tree.setBookmarks(bs);

    // Write Properties
    TagPath[] paths = layoutProperties.getTagPaths();
    Rectangle[] boxes = new Rectangle[paths.length];
    for (int i=0;i<paths.length;i++)
      boxes[i] = layoutProperties.getBoxForPath(paths[i]);

    Proxy[] proxies = tree.getModel().generateProxies(paths,boxes);
    tree.getModel().setProxiesOf(entity,proxies);
    tree.getModel().setSizeOf(entity,layoutProperties.getSizeOfEntities());

    // Done
  }

}
