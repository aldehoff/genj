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

/**
 * Class for editing the properties of a view
 */
class ViewEditor extends JPanel implements ViewInfo {

  /** statics */
  static private ViewEditor instance = null;
  static private JLabel lDefault = new JLabel("",JLabel.CENTER);
  static private Hashtable hViewInfos = new Hashtable();

  /** members */
  private Resources resources;
  private TitledBorder border;
  private Frame frame;
  private JPanel pSettings;
  private JButton bApply,bReset,bClose;

  private Component view;
  private ViewInfo  info;

  /**
   * Constructor
   */
  /*package*/ ViewEditor(Frame pFrame) {

    frame = pFrame;
    resources = new Resources(this);

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
      if ("APPLY".equals(e.getActionCommand())&&(view!=null))
        info.apply();
      if ("RESET".equals(e.getActionCommand())&&(view!=null))
        info.reset();
      }
    };

    bApply = ButtonHelper.createButton(resources.getString("view.apply"),null,"APPLY", alistener, true, true);
    bReset = ButtonHelper.createButton(resources.getString("view.reset"),null,"RESET", alistener, true, true);
    bClose = ButtonHelper.createButton(resources.getString("view.close"),null,"CLOSE", alistener, true, true);

    pActions.add(bApply);
    pActions.add(bReset);
    pActions.add(bClose);

    // Layout
    setLayout(new BorderLayout());
    add(pSettings,"Center");
    add(pActions ,"South" );

    // Done
    setEditingComponent(this,null,null);
  }

  /**
   * Remembers this instance
   */
  public void addNotify() {
    instance = this;
    super.addNotify();
  }

  /**
   * Since we act as the default ViewInfo (no View information) we
   * provide implementation for the callbacks
   */
  public void apply() {
  }

  /**
   * Since we act as the default ViewInfo (no View information) we
   * provide implementation for the callbacks
   */
  public void reset() {
  }

  /**
   * Since we act as the default ViewInfo (no View information) we
   * provide implementation for the callbacks
   */
  public void setView(Component comp) {
  }

  /**
   * Static signal that a view should not be edited anymore
   */
  static public void dontEdit(Component view) {

    // Instance there?
    if ((instance==null)||(instance.view!=view))
      return;

    // .. turn to non editing
    instance.setEditingComponent(instance,null,null);
  }

  /**
   * Static signal that a view should be edited
   */
  static public void edit(Component view, String title) {

    // Instance there?
    if ((instance==null)||(instance.view==view))
      return;

    // Calculate class to use and instantiate it
    ViewInfo info = null;

    String viName = view.getClass().getName()+"Info";

    info = (ViewInfo)hViewInfos.get(viName);
    if (info==null) {
      try {
        Class c = Class.forName(viName);
        info = (ViewInfo)c.newInstance();
        hViewInfos.put(viName,info);
      } catch (ClassCastException cce) {
        System.out.println("[Debug]"+viName+" is no valid ViewInfo class");
      } catch (Exception e) {
        //System.out.println("[Debug] Couldn't instantiate "+viName);
      }
    }

    // Minimum is default
    if (info==null) {
      info = instance;
    }

    instance.setEditingComponent(info,view,title);

    // Done
  }

  /**
   * Our editor component in case of missing ViewInfo
   */
  public Component getEditor() {
    lDefault.setText(resources.getString("view.choose"));
    return lDefault;
  }

  /**
   * Forgets this instance
   */
  public void removeNotify() {
    instance = null;
    super.removeNotify();
  }

  /**
   * Helper for adding an editing component
   */
  private void setEditingComponent(ViewInfo info,Component view, String title) {

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
    frame.pack();
    pSettings.invalidate();
    pSettings.repaint();

    // Remember
    this.view = view;
    this.info = info;
  }

}
