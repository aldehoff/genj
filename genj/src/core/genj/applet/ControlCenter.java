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

import genj.gedcom.Gedcom;
import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.GridBagHelper;
import genj.util.WordBuffer;
import genj.util.swing.ButtonHelper;
import genj.util.swing.LinkWidget;
import genj.view.ViewFactory;
import genj.view.ViewManager;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * The main content from which the user start using GenJ in Applet
 */
public class ControlCenter extends JPanel {

  /** view manager */
  private ViewManager viewManager;
  
  /** gedcom */
  private Gedcom gedcom;

  /**
   * Constructor
   */
  public ControlCenter(ViewManager vmanager, Gedcom ged) {
    
    // remember
    viewManager = vmanager;
    gedcom = ged;
    
    // layout components
    GridBagHelper gh = new GridBagHelper(this);
    gh.add(getHeaderPanel() ,1,1);
    gh.add(getLinkPanel()   ,1,2);

    // make use white
    setBackground(Color.white);

    // done
  }

  /**
   * Create a header
   */
  private JPanel getHeaderPanel() {
    
    JPanel p = new JPanel(new GridLayout(2,1));
    p.setOpaque(false);
    p.add(new JLabel(gedcom.getOrigin().getFileName(), SwingConstants.CENTER));
    
    WordBuffer words = new WordBuffer();
    words.append(gedcom.getEntities(Gedcom.INDI).size()+" "+gedcom.getEntityName(Gedcom.INDI, true));
    words.append(gedcom.getEntities(Gedcom.FAM ).size()+" "+gedcom.getEntityName(Gedcom.FAM , true));
    
    p.add(new JLabel(words.toString(), SwingConstants.CENTER));
    
    return p;
  }
  

  /**
   * Collect buttons for views
   */
  private JPanel getLinkPanel() {

    // grab factories
    ViewFactory[] vfactories = viewManager.getFactories();

    // prepare the panel
    JPanel p = new JPanel(new GridLayout(vfactories.length, 1));
    p.setOpaque(false);
    
    ButtonHelper bh = new ButtonHelper().setContainer(p).setButtonType(LinkWidget.class);
    for (int v=0; v<vfactories.length; v++) {
      bh.create(new ActionView(vfactories[v]));
    }
    
    // done
    return p;
  }
  

  /**
   * Action to open view
   */
  private class ActionView extends ActionDelegate {
    /** factory */
    private ViewFactory factory;
    /**
     * Constructor
     */
    private ActionView(ViewFactory vfactory) {
      factory = vfactory;
      setText(vfactory.getTitle(false));
      setImage(vfactory.getImage());
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      Debug.log(Debug.INFO, ControlCenter.this, "factory="+factory);
      viewManager.openView(factory, gedcom);
    }
  } //ActionView
  
  

} //ControlCenter