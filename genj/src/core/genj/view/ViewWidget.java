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

import genj.app.Images;
import genj.gedcom.Gedcom;
import genj.print.PrintProperties;
import genj.print.PrintRenderer;
import genj.print.Printer;
import genj.util.ActionDelegate;
import genj.util.ImgIcon;
import genj.util.Registry;
import genj.util.swing.ButtonHelper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

/**
 * A wrapper for our views enabling action buttons
 */
/*package*/ class ViewWidget extends JPanel {
  
  /** the registry this view is for */
  private Registry registry;
  
  /** the toolbar we're using */
  private JToolBar bar;
  
  /** the settings for this view */
  private JComponent settings;
  
  /** 
   * Constructor
   */
  public ViewWidget(JFrame frame, Gedcom gedcom, Registry registry, ViewManager.Descriptor descriptor) {
    
    // remember
    this.registry = registry;
    
    // create a factory
    ViewFactory factory = descriptor.instantiate();
  
    // create the view component
    Component view = factory.createViewComponent(gedcom, registry, frame);

    // Fill Toolbar
    boolean isBar = false;
    bar = new JToolBar();
    if (view instanceof ToolBarSupport) {
      ((ToolBarSupport)view).populate(bar);
      bar.add(Box.createGlue());
      isBar = true;
    }

    // add our buttons     
    ButtonHelper bh = new ButtonHelper()
      .setResources(ViewManager.resources)
      .setContainer(bar);

    // .. a button for editing the View's settings
    settings = factory.createSettingsComponent(view);
    if (settings!=null) {
      settings.setBorder(new TitledBorder(frame.getTitle()));
      bh.create(new ActionOpenSettings());
      isBar = true;
    }
  
    // .. a button for printing View
    PrintRenderer renderer = factory.createPrintRenderer(view);
    if (renderer!=null) {
      bh.create(new ActionPrint(renderer, frame));
      isBar = true;
    }
  
    // .. a button for closing the View
    bh.create(new ActionDelegate.ActionDisposeFrame(frame).setImage(genj.gedcom.Images.get("X")));
    
    // setup layout
    setLayout(new BorderLayout());
    if (isBar) add(bar, registry.get("toolbar", BorderLayout.SOUTH));
    add(view, BorderLayout.CENTER);
    
    // done
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
   * @see javax.swing.JComponent#removeNotify()
   */
  public void removeNotify() {
    // delegate
    super.removeNotify();
    // close property editor if open
    ViewManager.getInstance().closeSettings(settings);
    // done
  }
  
  /**
   * Action - print view
   */
  private class ActionPrint extends ActionDelegate {
    /** the renderer */
    private PrintRenderer renderer;
    /** the frame */
    private Frame frame;
    /** constructor */
    protected ActionPrint(PrintRenderer r, Frame f) {
      renderer=r;
      frame=f;
      super.setImage(Images.imgPrint).setTip("view.print.tip");
    }
    /** run */
    protected void execute() {
      Printer.print(frame, renderer, new PrintProperties(frame.getTitle()));
    }
  } //ActionOpenSettings
  
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
      ViewManager.getInstance().openSettings(settings);
    }
  } //ActionOpenSettings
  
} //ViewWidget
