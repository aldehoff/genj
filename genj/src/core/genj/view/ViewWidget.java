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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.print.PrintProperties;
import genj.print.PrintRenderer;
import genj.print.Printer;
import genj.util.ActionDelegate;
import genj.util.ImgIcon;
import genj.util.Registry;
import genj.util.swing.ButtonHelper;
import genj.util.swing.MenuHelper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
  
  /** the view we're wrapping */
  private Component view;
  
  /** the settings for this view */
  private JComponent settings;
  
  /** the gedcom this view looks at */
  private Gedcom gedcom;
  
  /** the frame its contained in */
  private JFrame frame;
  
  /** 
   * Constructor
   */
  /*package*/ ViewWidget(JFrame frame, Gedcom gedcom, Registry registry, ViewManager.Descriptor descriptor) {
    
    // remember
    this.registry = registry;
    this.gedcom = gedcom;
    this.frame = frame;
    
    // create the view component
    view = descriptor.factory.createViewComponent(gedcom, registry, frame);

    // setup layout
    setLayout(new BorderLayout());
    add(view, BorderLayout.CENTER);

    // install a toolbar
    installToolBar(descriptor, view, frame);
    
    // install popup support
    installPopupSupport();
    
    // done
  }
  
  /**
   * Helper that creates the toolbar for the view
   */
  private void installToolBar(ViewManager.Descriptor descriptor, Component view, Frame frame) {
    
    // only if ToolBarSupport
    if (!(view instanceof ToolBarSupport)) return;

    // Create one
    bar = new JToolBar();
    
    // Fill Toolbar
    ((ToolBarSupport)view).populate(bar);
    bar.add(Box.createGlue());

    // add our buttons     
    ButtonHelper bh = new ButtonHelper()
      .setResources(ViewManager.resources)
      .setContainer(bar);

    // .. a button for editing the View's settings
    settings = descriptor.factory.createSettingsComponent(view);
    if (settings!=null) {
      settings.setBorder(new TitledBorder(frame.getTitle()));
      bh.create(new ActionOpenSettings());
    }
  
    // .. a button for printing View
    PrintRenderer renderer = descriptor.factory.createPrintRenderer(view);
    if (renderer!=null) {
      bh.create(new ActionPrint(renderer, frame));
    }
  
    // .. a button for closing the View
    bh.create(new ActionDelegate.ActionDisposeFrame(frame).setImage(genj.gedcom.Images.get("X")));

    // add it
    String o = descriptor.dim.width<descriptor.dim.height ? BorderLayout.WEST : BorderLayout.SOUTH;
    add(bar, registry.get("toolbar", o));
    
    // done
  }
  
  /**
   * Install the popup support
   */
  private void installPopupSupport() {
    // check for support
    if (!(view instanceof EntityPopupSupport)) return;
    // install it
    EntityPopupSupport eps = (EntityPopupSupport)view;
    eps.getEntityPopupContainer().addMouseListener(new EntityPopupMouseListener());
    // done
  }
  
  /**
   * Accessor - the view
   */
  /*package*/ Component getView() {
    return view;
  }
  
  /**
   * Accessor - the settings
   */
  /*package*/ JComponent getSettings() {
    return settings;
  }
  
  /**
   * Accessor - the gedcom
   */
  /*package*/ Gedcom getGedcom() {
    return gedcom;
  }
  
  /**
   * Accessor - the frame
   */
  /*package*/ JFrame getFrame() {
    return frame;
  }
  
  /**
   * Sets the view's current entity
   */
  /*package*/ void setCurrentEntity(Entity entity) {
    if (view instanceof CurrentSupport)
      ((CurrentSupport)view).setCurrentEntity(entity);
  }
  
  /**
   * Sets the view's current property
   */
  /*package*/ void setCurrentProperty(Property property) {
    if (view instanceof CurrentSupport)
      ((CurrentSupport)view).setCurrentProperty(property);
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
    // propagate to manager
    ViewManager.getInstance().closeNotify(this);
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
      ViewManager.getInstance().openSettings(ViewWidget.this);
    }
  } //ActionOpenSettings
  
  /**
   * Our listener for mouse clicks on EntityPopupSupport components
   */
  private class EntityPopupMouseListener extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
      // 20020829 on some OSes isPopupTrigger() will
      // be true on mousePressed
      mouseReleased(e);
    }
    public void mouseReleased(MouseEvent e) {
      // no popup trigger no action
      if (!e.isPopupTrigger()) return;
      // grab the data we need
      EntityPopupSupport esp = (EntityPopupSupport)view;
      JComponent container = esp.getEntityPopupContainer();
      Entity entity = esp.getEntityAt(e.getPoint());
      if (entity==null) return;
      // get the actions for that entity
      List actions = ViewManager.getInstance().getActions(entity);
      if (actions.isEmpty()) return;
      // fill them into a popup
      MenuHelper mh = new MenuHelper();
      JPopupMenu popup = mh.createPopup(entity.getId());
      Iterator it = actions.iterator();
      while (it.hasNext()) {
        ActionDelegate ad = (ActionDelegate)it.next();
        mh.createItem(ad);
      }
      // show the popup
      popup.show(container, e.getPoint().x, e.getPoint().y);
      // done
    }
  } //EntityPopupMouseListener
  
} //ViewWidget
