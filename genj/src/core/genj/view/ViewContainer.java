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

import genj.gedcom.Gedcom;
import genj.print.Printer;
import genj.util.ActionDelegate;
import genj.util.Registry;
import genj.util.swing.ButtonHelper;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

/**
 * A wrapper for our views enabling action buttons
 */
/*package*/ class ViewContainer extends JPanel {
  
  /** the registry this view is for */
  private Registry registry;
  
  /** the toolbar we're using */
  private JToolBar bar;
  
  /** the view we're wrapping */
  private JComponent view;
  
  /** the factory we've used */
  private ViewFactory factory;
  
  /** the gedcom this view looks at */
  private Gedcom gedcom;
  
  /** the manager */
  private ViewManager manager;
  
  /** title */
  private String title;
  
  /** key */
  private String key;
  
  
  /** 
   * Constructor
   */
  /*package*/ ViewContainer(String kEy, String tiTle, Gedcom geDcom, Registry regIstry, ViewFactory facTory, ViewManager manAger) {
    
    // remember
    manager = manAger;
    key = kEy;
    title = tiTle;
    gedcom = geDcom;
    registry = regIstry;
    factory = facTory;
    
    // create the view component
    view = factory.createView(title, gedcom, registry, manager);

    // setup layout
    setLayout(new BorderLayout());
    add(view, BorderLayout.CENTER);

    // done
  }
  
  /**
   * Install toolbar at time of add
   */
  public void addNotify() {
    // continue
    super.addNotify();
    // install a toolbar
    installToolBar(view, factory);
  }
  
  /**
   * Helper that creates the toolbar for the view
   */
  private void installToolBar(JComponent view, ViewFactory factory) {
    
    // only if ToolBarSupport
    if (!(view instanceof ToolBarSupport)) return;

    // Create one
    bar = new JToolBar();
    
    // Fill Toolbar
    ((ToolBarSupport)view).populate(bar);
    bar.add(Box.createGlue());

    // add our buttons     
    ButtonHelper bh = new ButtonHelper()
      .setFocusable(false)
      .setResources(ViewManager.resources)
      .setContainer(bar);

    // .. a button for editing the View's settings
    if (SettingsWidget.hasSettings(view))
      bh.create(new ActionOpenSettings());
    
    // .. a button for printing View
    if (manager.getPrintManager()!=null&&isPrintable()) 
      bh.create(new ActionPrint());

    // .. a button for closing the View
    bh.create(new ActionClose());

    // add it
    add(bar, registry.get("toolbar", BorderLayout.WEST));
    
    // done
  }
  
  /**
   * Checks whether this view is printable
   */
  /*package*/ boolean isPrintable() {
    try {
      if (Printer.class.isAssignableFrom(Class.forName(view.getClass().getName()+"Printer")))
        return true;
    } catch (Throwable t) {
    }
    return false;
  }
  
  /**
   * Accessor - the view
   */
  /*package*/ JComponent getView() {
    return view;
  }
  
  /**
   * Accessor - the gedcom
   */
  /*package*/ Gedcom getGedcom() {
    return gedcom;
  }
  
  /**
   * Accessor - the title
   */
  /*package*/ String getTitle() {
    return title;
  }
  
  /**
   * Accessor - the key
   */
  /*package*/ String getKey() {
    return key;
  }
  
  /**
   * When adding components we fix a Toolbar's sub-component's
   * orientation
   * @see java.awt.Container#addImpl(Component, Object, int)
   */
  protected void addImpl(Component comp, Object constraints, int index) {
    // go ahead with super
    super.addImpl(comp, constraints, index);
    // toolbar?
    if (comp==bar) {
      // remember
      registry.put("toolbar", constraints.toString());
      // find orientation
      int orientation = SwingConstants.HORIZONTAL;
      if (BorderLayout.WEST.equals(constraints)||BorderLayout.EAST.equals(constraints))
        orientation = SwingConstants.VERTICAL;
      // fix orientation for toolbar
      bar.setOrientation(orientation);
      // toolbar o.k.
    }
    // done
  }

  /**
   * Action - close view
   */
  private class ActionClose extends ActionDelegate {
    /** constructor */
    protected ActionClose() {
      super.setImage(Images.imgClose);
    }
    /** run */
    protected void execute() {
      manager.getWindowManager().close(key); 
    }
  } //ActionClose
  
  /**
   * Action - print view
   */
  private class ActionPrint extends ActionDelegate {
    /** constructor */
    protected ActionPrint() {
      super.setImage(Images.imgPrint).setTip("view.print.tip");
    }
    /** run */
    protected void execute() {
      try {
        Printer printer = (Printer)Class.forName(view.getClass().getName()+"Printer").newInstance();
        printer.setView(view);
        manager.getPrintManager().print(printer, title, view, registry); 
      } catch (Throwable t) {
      }
    }
  } //ActionPrint
  
  /**
   * Action - open the settings of a view
   */
  private class ActionOpenSettings extends ActionDelegate {
    /** constructor */
    protected ActionOpenSettings() {
      super.setImage(Images.imgSettings).setTip("view.settings.tip");
    }
    /** run */
    protected void execute() {
      manager.openSettings(ViewContainer.this);
    }
  } //ActionOpenSettings
  
} //ViewWidget
