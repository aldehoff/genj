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
import genj.util.swing.ImgIconConverter;
import genj.util.swing.MenuHelper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.MenuSelectionManager;
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
  /*package*/ ViewWidget(JFrame frame, Gedcom gedcom, Registry registry, ViewFactory factory) {
    
    // remember
    this.registry = registry;
    this.gedcom = gedcom;
    this.frame = frame;
    
    // create the view component
    view = factory.createViewComponent(gedcom, registry, frame);

    // setup layout
    setLayout(new BorderLayout());
    add(view, BorderLayout.CENTER);

    // install a toolbar
    installToolBar(view, frame, factory);
    
    // install popup support
    installPopupSupport();
    
    // done
  }
  
  /**
   * Helper that creates the toolbar for the view
   */
  private void installToolBar(Component view, Frame frame, ViewFactory factory) {
    
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
    settings = factory.createSettingsComponent(view);
    if (settings!=null) {
      settings.setBorder(new TitledBorder(frame.getTitle()));
      bh.create(new ActionOpenSettings());
    }
  
    // .. a button for printing View
    PrintRenderer renderer = factory.createPrintRenderer(view);
    if (renderer!=null) {
      bh.create(new ActionPrint(renderer, frame));
    }
  
    // .. a button for closing the View
    bh.create(new ActionDelegate.ActionDisposeFrame(frame).setImage(Images.imgClose));

    // add it
    Dimension dim = factory.getDefaultDimension();
    String defaultOrientation = dim.width<dim.height ? BorderLayout.WEST : BorderLayout.SOUTH;
    add(bar, registry.get("toolbar", defaultOrientation));
    
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
   * Show a context menu for given point - at this
   * point we assume that view instanceof EntityPopupSupport
   */
  private void showContextMenu(Point point) {
    
    // grab the data we need
    EntityPopupSupport esp = (EntityPopupSupport)view;
    JComponent container = esp.getEntityPopupContainer();
    Entity entity = esp.getEntityAt(point);

    // 20021017 @see note at the bottom of file
    MenuSelectionManager.defaultManager().clearSelectedPath();

    // create a popup
    MenuHelper mh = new MenuHelper();
    JPopupMenu popup = mh.createPopup(frame.getTitle());
    
    // items for entity
    if (entity!=null) {
      List actions = ViewManager.getInstance().getActions(entity);
      if (!actions.isEmpty()) {
        mh.createMenu(entity.getId(), entity.getProperty().getImage(false));
        mh.createItems(actions);
        mh.popMenu();
      }
    }
    
    // items for gedcom
    List actions = ViewManager.getInstance().getActions(gedcom);
    if (!actions.isEmpty()) {
      mh.createMenu(gedcom.getName(), Gedcom.getImage());
      mh.createItems(actions);
      mh.popMenu();
    }
    
    // show the popup
    if (popup.getComponentCount()>0)
      popup.show(container, point.x, point.y);
    
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
    // delegate to view
    if (view instanceof CurrentSupport)
      ((CurrentSupport)view).setCurrentEntity(entity);
    // 20021017 @see note at the bottom of file
    MenuSelectionManager.defaultManager().clearSelectedPath();
    // done     
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
    // 20021017 @see note at the bottom of file
    MenuSelectionManager.defaultManager().clearSelectedPath();
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
      // show a context menu
      showContextMenu(e.getPoint());
      // done
    }
  } //EntityPopupMouseListener

  // 20021017 strangely Popups for JPopupMenu don't seem to
  // disappear even though of mouse-clicks somewhere in the
  // view. Calling
  //  MenuSelectionManager.defaultManager().clearSelectedPath();
  // before bringing up a popup makes sure that it disappears 
  // when anything is clicking in the view.
  // Also popups are not removed by Swing after opening up
  // a JPopupMenu in one view and clicking on components in 
  // another view. We could do some stuff with windowDeactivated 
  // but that seems too much to bother. So for now we'll call
  //  MenuSelectionManager.defaultManager().clearSelectedPath();
  // which will get rid of the popup anytime 
  //  setCurrentEntity() 
  // is called. That will make sure there's no current-change
  // with a popup still being open for the last current.
  // Lastly we also call 
  //  MenuSelectionManager.defaultManager().clearSelectedPath();
  // in removeNotify() when a view is removed. Otherwise Swing
  // might keep a popup open in another view showing items that
  // are applicable to an already closed view.
  
} //ViewWidget
