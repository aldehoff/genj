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
import genj.gedcom.Indi;
import genj.gedcom.Selection;
import genj.gedcom.GedcomListener;
import genj.util.ActionDelegate;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A navigator with buttons to easily navigate through Gedcom data
 */
public class NavigatorView extends JPanel {
  
  private static Resources resources = new Resources(NavigatorView.class);
  
  /** the label holding information about the current individual */
  private JLabel label;
  
  /** the current individual */
  private Indi indi;
  
  /** the buttons */
  private JButton bFather, bMother, bOlder, bPartner, bYounger, bChild;
  

  /**
   * Constructor
   */
  public NavigatorView(Gedcom useGedcom, Registry useRegistry, Frame useFrame) {
    
    // super
    super(new BorderLayout());

    // layout    
    label = new JLabel();
    label.setFont(new Font("Arial", Font.PLAIN, 10));
    label.setBorder(BorderFactory.createTitledBorder(resources.getString("nav.current_entity.title")));
    add(label,BorderLayout.NORTH);
    
    JPanel panel = createPanel();
    add(panel,BorderLayout.CENTER);
    
    // date
    useGedcom.addListener(new GedcomListener() {
      public void handleChange(Change change) {
        if (change.getEntities(change.EDEL).contains(indi)) setEntity(change.getGedcom(), null);
        else setEntity(change.getGedcom(), indi);
      }
      public void handleClose(Gedcom which) {
      }
      public void handleSelection(Selection selection) {
        setEntity(selection.getEntity().getGedcom(), selection.getEntity());
      }
    });
    
    // init
    setEntity(useGedcom,useGedcom.getLastEntity());

    // done    

  }
  
  /**
   * Sets the current entity (only individuals accepted)
   */
  public void setEntity(Gedcom g, Entity e) {
    // no entity
    if ((e == null)&&(g.getEntities(Gedcom.INDIVIDUALS).getSize()>0)) e=g.getIndi(0);
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
   * Creates the panel
   */
  private JPanel createPanel() {    
    
    JPanel result = new JPanel();    
    result.setBorder(BorderFactory.createTitledBorder(resources.getString("nav.navigate.title")));
    GridBagHelper gh = new GridBagHelper(result);
    
    // our action delegation
    ActionDelegate ad = new ActionDelegate(this);
    ad.add("FATHER"  , "nav2Father")
      .add("MOTHER"  , "nav2Mother")
      .add("YSIBLING", "nav2YoungerSibling")
      .add("PARTNER" , "nav2Partner")
      .add("OSIBLING", "nav2OlderSibling")
      .add("CHILD"   , "nav2Child");
    
    // add the buttons    
    ButtonHelper bh = new ButtonHelper()
      .setFocusable(false)
      .setBorder(false)
      .setInsets(0)
      .setResources(resources)
      .setEnabled(false)
      .setListener(ad);
    
      bFather = bh.setAction("FATHER"  ).setRollover(Images.imgNavFatherOn         ).setImage(Images.imgNavFatherOff        ).setTip("tip.nav_father"  ).create();
      bMother = bh.setAction("MOTHER"  ).setRollover(Images.imgNavMotherOn         ).setImage(Images.imgNavMotherOff        ).setTip("tip.nav_mother"  ).create();
      bOlder  = bh.setAction("OSIBLING").setRollover(Images.imgNavOlderSiblingOn   ).setImage(Images.imgNavOlderSiblingOff  ).setTip("tip.nav_osibling").create();
      bPartner= bh.setAction("PARTNER" ).setRollover(Images.imgNavPartnerOn        ).setImage(Images.imgNavPartnerOff       ).setTip("tip.nav_partner" ).create();
      bYounger= bh.setAction("YSIBLING").setRollover(Images.imgNavYoungerSiblingOn ).setImage(Images.imgNavYoungerSiblingOff).setTip("tip.nav_ysibling").create();
      bChild  = bh.setAction("CHILD"   ).setRollover(Images.imgNavChildOn          ).setImage(Images.imgNavChildOff         ).setTip("tip.nav_child"   ).create();

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
  public void nav2Father() {
    indi.getGedcom().fireEntitySelected(null, indi.getFather(), false);
  }
    
  /**
   * Navigate 2 Mother
   */
  public void nav2Mother() {
    indi.getGedcom().fireEntitySelected(null, indi.getMother(), false);
  }
    
  /**
   * Navigate 2 Previous
   */
  public void nav2YoungerSibling() {
    indi.getGedcom().fireEntitySelected(null, indi.getYoungerSibling(), false);
  }
    
  /**
   * Navigate 2 Next
   */
  public void nav2OlderSibling() {
    indi.getGedcom().fireEntitySelected(null, indi.getOlderSibling(), false);
  }
    
  /**
   * Navigate 2 Partner
   */
  public void nav2Partner() {
    indi.getGedcom().fireEntitySelected(null, indi.getPartners()[0], false);
  }
    
  /**
   * Navigate 2 Child
   */
  public void nav2Child() {
    indi.getGedcom().fireEntitySelected(null, indi.getChildren()[0], false);
  }
    
}
