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
package genj.nav;

import genj.gedcom.Change;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.util.ActionDelegate;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.view.CurrentSupport;
import genj.view.ViewManager;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A navigator with buttons to easily navigate through Gedcom data
 */
public class NavigatorView extends JPanel implements CurrentSupport {
  
  /*package*/ static Resources resources = new Resources(NavigatorView.class);
  
  /** the label holding information about the current individual */
  private JLabel label;
  
  /** the current individual */
  private Indi indi;
  
  /** the buttons */
  private AbstractButton bFather, bMother, bOlder, bPartner, bYounger, bChild;
  
  /** the gedcom */
  private Gedcom gedcom;
  

  /**
   * Constructor
   */
  public NavigatorView(Gedcom useGedcom, Registry useRegistry, Frame useFrame) {
    
    // remember
    gedcom = useGedcom;
    
    // layout    
    setLayout(new BorderLayout());

    label = new JLabel();
    label.setFont(new Font("Arial", Font.PLAIN, 10));
    label.setBorder(BorderFactory.createTitledBorder(resources.getString("nav.current_entity.title")));
    add(label,BorderLayout.NORTH);
    
    JPanel panel = createPanel();
    add(panel,BorderLayout.CENTER);
    
    // date
    useGedcom.addListener(new GedcomListener() {
      public void handleChange(Change change) {
        if (change.getEntities(change.EDEL).contains(indi)) setCurrentEntity(null);
        else setCurrentEntity(indi);
      }
    });
    
    // init
    setCurrentEntity(ViewManager.getInstance().getCurrentEntity(gedcom));

    // done    

  }
  
  /**
   * @see genj.view.CurrentSupport#setCurrentEntity(Entity)
   */
  public void setCurrentEntity(Entity e) {
    
    // no entity
    if (e == null) {
      List list = gedcom.getEntities(Gedcom.INDIVIDUALS);
      if (!list.isEmpty()) e=(Entity)list.get(0);
    }
    if (e == null) {
      // data
      indi = null;
      label.setText("n/a");
      // buttons
      bFather.setEnabled(false);
      bMother.setEnabled(false);
      bOlder.setEnabled(false);
      bPartner.setEnabled(false);
      bYounger.setEnabled(false);
      bChild.setEnabled(false);
      return;
    }
    // individual
    if (e instanceof Indi) {
      // data
      indi = (Indi)e;
      label.setText(e.toString());
      // buttons
      bFather.setEnabled(indi.getFather()!=null);
      bMother.setEnabled(indi.getMother()!=null);
      bOlder.setEnabled(indi.getOlderSibling()!=null);
      bPartner.setEnabled(indi.getPartners().length>0);
      bYounger.setEnabled(indi.getYoungerSibling()!=null);
      bChild.setEnabled(indi.getChildren().length>0);
      
    }
    // stay where we are
  }
  
  /**
   * @see genj.view.CurrentSupport#setCurrentProperty(Property)
   */
  public void setCurrentProperty(Property property) {
    // ignored
  }
  
  /**
   * propagate the selection of an entity
   */
  private void fireCurrentEntity(Entity e) {
    if (e==null) return;
    ViewManager.getInstance().setCurrentEntity(e);
  }
  
  /**
   * Creates the panel
   */
  private JPanel createPanel() {    
    
    JPanel result = new JPanel();    
    result.setBorder(BorderFactory.createTitledBorder(resources.getString("nav.navigate.title")));
    GridBagHelper gh = new GridBagHelper(result);
    
    // add the buttons    
    ButtonHelper bh = new ButtonHelper()
      .setFocusable(false)
      .setBorder(false)
      .setInsets(0)
      .setResources(resources)
      .setEnabled(false);
    
      bFather = bh.create(new ActionFather()        );
      bMother = bh.create(new ActionMother()        );
      bOlder  = bh.create(new ActionOlderSibling()  );
      bPartner= bh.create(new ActionPartner()       );
      bYounger= bh.create(new ActionYoungerSibling());
      bChild  = bh.create(new ActionChild()         );

    gh.add(bFather ,2,1,1,1,gh.FILL_NONE);
    gh.add(bMother ,3,1,1,1,gh.FILL_NONE);
    gh.add(bOlder  ,0,2,2,1,gh.FILL_NONE);
    gh.add(bPartner,2,2,2,1,gh.FILL_NONE, new Insets(12,0,12,0));
    gh.add(bYounger,4,2,2,1,gh.FILL_NONE);
    gh.add(bChild  ,2,3,2,1,gh.FILL_NONE);

    // done
    return result;
  }

  /**
   * Navigate 2 Father
   */
  private class ActionFather extends ActionDelegate {
    /** constructor */
    protected ActionFather() {
      super.setRollover(Images.imgNavFatherOn).setImage(Images.imgNavFatherOff).setTip("tip.nav_father"  );
    }
    /** run */
    protected void execute() {
      fireCurrentEntity(indi.getFather());
    }
  }
      
  /**
   * Navigate 2 Mother
   */
  private class ActionMother extends ActionDelegate {
    /** constructor */
    protected ActionMother() {
      super.setRollover(Images.imgNavMotherOn).setImage(Images.imgNavMotherOff).setTip("tip.nav_mother"  );
    }
    /** run */
    protected void execute() {
      fireCurrentEntity(indi.getMother());
    }
  }
        
  /**
   * Navigate 2 Previous
   */
  private class ActionYoungerSibling extends ActionDelegate {
    /** constructor */
    protected ActionYoungerSibling() {
      super.setRollover(Images.imgNavYoungerSiblingOn).setImage(Images.imgNavYoungerSiblingOff).setTip("tip.nav_ysibling");
    }
    /** run */
    protected void execute() {
      fireCurrentEntity(indi.getYoungerSibling());
    }
  }      
  
  /**
   * Navigate 2 Next
   */
  private class ActionOlderSibling extends ActionDelegate {
    /** constructor */
    protected ActionOlderSibling() {
      super.setRollover(Images.imgNavOlderSiblingOn).setImage(Images.imgNavOlderSiblingOff).setTip("tip.nav_osibling");
    }
    /** run */
    protected void execute() {
      fireCurrentEntity(indi.getOlderSibling());
    }
  }
        
  /**
   * Navigate 2 Partner
   */
  private class ActionPartner extends ActionDelegate {
    /** constructor */
    protected ActionPartner() {
      super.setRollover(Images.imgNavPartnerOn).setImage(Images.imgNavPartnerOff).setTip("tip.nav_partner" );
    }
    /** run */
    protected void execute() {
      fireCurrentEntity(indi.getPartners()[0]);
    }
  }
        
  /**
   * Navigate 2 Child
   */
  private class ActionChild extends ActionDelegate {
    /** constructor */
    protected ActionChild() {
      super.setRollover(Images.imgNavChildOn).setImage(Images.imgNavChildOff).setTip("tip.nav_child"   );
    }
    /** run */
    protected void execute() {
      fireCurrentEntity(indi.getChildren()[0]);
    }
  }
  
} ///NavigatorView
