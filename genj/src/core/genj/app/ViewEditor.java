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
import javax.swing.border.*;
import javax.swing.event.*;

import java.awt.*;
import java.util.*;
import java.awt.event.*;

import genj.gedcom.*;
import genj.util.*;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImgIconConverter;

/**
 * Class for editing the properties of a view
 */
class ViewEditor extends JPanel {

  /** statics */
  static private Hashtable hViewInfos = new Hashtable();

  /** members */
  private Resources resources = new Resources(ViewEditor.class);;
  private TitledBorder border;
  private Frame frame;
  private JPanel pSettings;
  private JButton bApply,bReset,bClose;
  private EmptyViewInfo emptyViewInfo = new EmptyViewInfo();

  private Component currentView;
  private ViewInfo  currentInfo;

  /**
   * Constructor
   */
  /*package*/ ViewEditor(Frame pFrame) {

    frame = pFrame;

    // Panel for View's settings
    pSettings = new JPanel();
    border = BorderFactory.createTitledBorder("");
    pSettings.setBorder(border);

    // Panel for Actions
    JPanel pActions = new JPanel();

    ActionListener alistener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if ("CLOSE".equals(e.getActionCommand()))
          frame.dispose();
        if ((currentView==null)||(!currentView.isValid())) {
          edit(null,null,null);
        }
        if ("APPLY".equals(e.getActionCommand())&&(currentView!=null))
          currentInfo.apply();
        if ("RESET".equals(e.getActionCommand())&&(currentView!=null))
          currentInfo.reset();
      }
    };

    ButtonHelper bh = new ButtonHelper().setResources(resources).setListener(alistener);

    bApply = bh.setText("view.apply").setAction("APPLY").create();
    bReset = bh.setText("view.reset").setAction("RESET").create();
    bClose = bh.setText("view.close").setAction("CLOSE").create();

    pActions.add(bApply);
    pActions.add(bReset);
    pActions.add(bClose);

    // Layout
    setLayout(new BorderLayout());
    add(pSettings,"Center");
    add(pActions ,"South" );

    // Done
    edit(emptyViewInfo,null,null);
  }

  /**
   * Get ViewInfo for given view
   */
  static public ViewInfo getViewInfo(Component view) {
    
    // tricky
    if (view==null) return null;

    // already known?
    String name = view.getClass().getName()+"Info";
    ViewInfo result = (ViewInfo)hViewInfos.get(name);
    if (result!=null) return result;

    // look for it    
    try {
      Class c = Class.forName(name);
      result = (ViewInfo)c.newInstance();
      hViewInfos.put(name,result);
    } catch (ClassCastException cce) {
    } catch (Exception e) {
    }
    
    // done
    return result;
  }

  /**
   * Static signal that a view shouldn't be edited (anymore)
   */
  public static void stopEditing(Component view) {
    JFrame frame = App.getInstance().getFrame("settings");
    if (frame==null) return;
    ViewEditor editor = (ViewEditor)frame.getContentPane().getComponent(0);
    if (editor.currentView!=view) return;
    startEditing(null,null);
  }

  /**
   * Static signal that a view should be edited
   */
  public static void startEditing(Component view, String title) {
    
    // get the ViewInfo
    ViewInfo info = getViewInfo(view);
    
    // editor already open?
    JFrame frame = App.getInstance().getFrame("settings");
    ViewEditor instance;
    if (frame==null) {
      // get a new
      frame = App.getInstance().createFrame(
        App.resources.getString("cc.title.settings_edit"),
        Images.imgGedcom,
        "settings",
        new Dimension(256,320)
      );
      instance = new ViewEditor(frame);
      frame.getContentPane().add(instance);
      frame.pack();
      frame.show();
    } else {
      instance = (ViewEditor)frame.getContentPane().getComponent(0);
    }
    
    instance.edit(info,view,title);

    // Done
  }

  /**
   * Helper for adding an editing component
   */
  private void edit(ViewInfo info,final Component view, String title) {
    
    // the editor we're using
    if (info==null) info = emptyViewInfo;
    Component editor = info.getEditor();

    // Remove last
    pSettings.removeAll();

    // Add new
    if (title!=null) {
      int i = title.indexOf('/');
      if (i>0) {
        title = title.substring(0,i);
      }
    }
    border.setTitle(title);
    pSettings.setLayout(new BorderLayout());
    pSettings.add(editor,"Center");
    editor.setVisible(true);

    // Change Buttons
    bApply.setEnabled(view!=null);
    bReset.setEnabled(view!=null);

    // Now tell ViewInfo to talk about View
    info.setView(view);

    // Show it
    pSettings.invalidate();
    pSettings.validate();
    pSettings.repaint();

    // Remember
    currentView = view;
    currentInfo = info;
  }

  /**
   * An empty ViewInfo
   */
  private class EmptyViewInfo implements ViewInfo {

    private JLabel lDefault;
    
    /**
     * Constructor
     */
    public EmptyViewInfo() {
      lDefault = new JLabel(resources.getString("view.choose"),ImgIconConverter.get(Images.imgSettings),JLabel.CENTER);
      lDefault.setHorizontalTextPosition(lDefault.LEADING);
    }
    
    /**
     * noop
     */
    public void apply() {
    }
  
    /**
     * noop
     */
    public void reset() {
    }
  
    /**
     * noop
     */
    public void setView(Component comp) {
    }

    /**
     * simple label
     */
    public Component getEditor() {
      return lDefault;
    }

  } // EmptyViewInfo
}
