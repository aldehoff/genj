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
package genj.applet;

import genj.util.ActionDelegate;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.swing.ButtonHelper;
import genj.util.swing.LinkWidget;
import genj.view.ViewFactory;
import genj.view.ViewManager;
import genj.window.DefaultWindowManager;

import java.awt.GridLayout;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * THE GenJ Applet
 */
public class Applet extends java.applet.Applet {

  /** whether we're initialized */
  private boolean isInitialized = false;

  /**
   * @see java.applet.Applet#init()
   */
  public void init() {
    
    new File(".").list();
    
    // work to do?
    if (isInitialized)
      return;

    // open registry
    Registry registry = new Registry();

    // prepare window manager
    ViewManager vmanager = new ViewManager(registry, null, new DefaultWindowManager(registry));
    
    // show applet content
    GridBagHelper gh = new GridBagHelper(this);
    gh.add(getHeaderPanel()      ,1,1);
    gh.add(getLinkPanel(vmanager),1,2);

    // done
    isInitialized = true;
  }
  
  /**
   * Create a header
   */
  private JPanel getHeaderPanel() {
    
    JPanel p = new JPanel(new GridLayout(2,1));
    p.setBackground(getBackground());
    p.add(new JLabel("Foo.ged", SwingConstants.CENTER));
    p.add(new JLabel("1200 Individuals", SwingConstants.CENTER));
    
    return p;
  }
  
  /**
   * Collect buttons for views
   */
  private JPanel getLinkPanel(ViewManager vmanager) {

    // grab factories
    ViewFactory[] vfactories = vmanager.getFactories();

    // prepare the panel
    JPanel p = new JPanel(new GridLayout(vfactories.length, 1));
    p.setBackground(getBackground());
    
    ButtonHelper bh = new ButtonHelper().setContainer(p).setButtonType(LinkWidget.class);
    for (int v=0; v<vfactories.length; v++) {
      bh.create(new ActionView(vfactories[v]));
    }
    
    // done
    return p;
  }
  
  /**
   * @see java.applet.Applet#start()
   */
  public void start() {
  }

  /**
   * @see java.applet.Applet#stop()
   */
  public void stop() {
  }

  /**
   * Action to open view
   */
  private class ActionView extends ActionDelegate {
    /**
     * Constructor
     */
    private ActionView(ViewFactory vfactory) {
      setText(vfactory.getTitle(false));
      setImage(vfactory.getImage());
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      // FIXME Auto-generated method stub

    }
  } //ActionView
  
  
} //Applet
