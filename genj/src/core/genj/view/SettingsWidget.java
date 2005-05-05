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
package genj.view;

import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;

import java.awt.BorderLayout;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * A settings component 
 */
/*package*/ class SettingsWidget extends JPanel {
  
  /** cached settings */
  private static Map cache = new WeakHashMap();
  
  /** components */
  private JPanel pSettings,pActions;
  private Vector vButtons = new Vector();
  
  /** settings */
  private Settings settings;
  
  /** ViewManager */
  private ViewManager viewManager;
  
  /**
   * Constructor
   */
  protected SettingsWidget(Resources resources, ViewManager manager) {
    
    // remember
    viewManager = manager;
    
    // Panel for ViewSettingsWidget
    pSettings = new JPanel(new BorderLayout());

    // Panel for Actions
    JPanel pActions = new JPanel();

    ButtonHelper bh = new ButtonHelper()
      .setResources(resources)
      .setContainer(pActions)
      .addCollection(vButtons)
      .setEnabled(false);
      
    bh.create(new ActionApply());
    bh.create(new ActionReset());
    bh.removeCollection(vButtons)
      .setEnabled(true)
      .create(new ActionClose());

    // Layout
    setLayout(new BorderLayout());
    add(pSettings,"Center");
    add(pActions ,"South" );
    
    // done
  }

  /**
   * Sets the ViewSettingsWidget to display
   */
  protected void setViewWidget(ViewContainer vw) {
    
    // clear content
    pSettings.removeAll();
    
    // try to get settings
    settings = getSettings(vw.getView());
    if (settings!=null) {
      settings.setView(vw.getView());
      JComponent editor = settings.getEditor();
      editor.setBorder(new TitledBorder(vw.getTitle()));
      pSettings.add(editor, BorderLayout.CENTER);
      settings.reset();
    }
      
    // enable buttons
    ButtonHelper.setEnabled(vButtons, settings!=null);
    
    // show
    pSettings.revalidate();
    pSettings.repaint();
    
    // done
  }

  /**
   * closes the settings
   */
  private class ActionClose extends ActionDelegate {
    private ActionClose() {
      setText("view.close");
    }
    protected void execute() {
      viewManager.getWindowManager().close("settings");
    }
  } //ActionClose
  
  /**
   * Applies the changes currently being done
   */
  private class ActionApply extends ActionDelegate {
    protected ActionApply() { super.setText("view.apply"); }
    protected void execute() {
      settings.apply();
    }
  }

  /**
   * Resets any change being done
   */
  private class ActionReset extends ActionDelegate {
    protected ActionReset() { super.setText("view.reset"); }
    protected void execute() {
      settings.reset();
    }
  }
  
  /**
   * Finds out whether given view has settings   */
  /*package*/ static boolean hasSettings(JComponent view) {
    try {
      if (Settings.class.isAssignableFrom(Class.forName(view.getClass().getName()+"Settings")))
      return true;
    } catch (Throwable t) {
    }
    return false;
  }
  
  /**
   * Gets settings for given view
   */
  /*package*/ Settings getSettings(JComponent view) {
    
    // known?
    Class viewType = view.getClass(); 
    Settings result = (Settings)cache.get(viewType);
    if (result!=null) return result;
    
    // create
    String type = viewType.getName()+"Settings";
    try {
      result = (Settings)Class.forName(type).newInstance();
      result.init(viewManager);
      cache.put(viewType, result);
    } catch (Throwable t) {
      Debug.log(Debug.WARNING, "couldn't initialize settings widget "+type, t);
    }
    
    // done
    return result;
  }
  
} //SettingsWidget

