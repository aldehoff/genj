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
import genj.gedcom.PropertySex;
import genj.util.ActionDelegate;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ImageIcon;
import genj.util.swing.PopupWidget;
import genj.view.ContextSupport;
import genj.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * A navigator with buttons to easily navigate through Gedcom data
 */
public class NavigatorView extends JPanel implements ContextSupport {
  
  private static Resources resources = Resources.get(NavigatorView.class);

  private final static String 
    FATHER   = "tip.father",
    MOTHER   = "tip.mother",
    YSIBLING = "tip.ysibling",
    OSIBLING = "tip.osibling",
    PARTNER  = "tip.partner",
    CHILD    = "tip.child";

  private final static int gp = 70;

  private final static ImageIcon
//FIXME  
    imgNavYoungerSiblingOn = new ImageIcon(NavigatorView.class,"NavYoungerSiblingOn.gif"),
    imgNavYoungerSiblingOff= imgNavYoungerSiblingOn.getDisabled(gp),
    imgNavOlderSiblingOn   = new ImageIcon(NavigatorView.class,"NavOlderSiblingOn.gif"),
    imgNavOlderSiblingOff  = imgNavYoungerSiblingOn.getDisabled(gp),
    imgNavChildOn          = new ImageIcon(NavigatorView.class,"NavChildOn.gif"),
    imgNavChildOff         = imgNavChildOn.getDisabled(gp),
    imgNavFatherOn         = Indi.IMG_MALE,//new ImageIcon(NavigatorView.class,"NavFatherOn.gif"),
    imgNavFatherOff        = imgNavFatherOn.getDisabled(gp),
    imgNavMotherOn         = Indi.IMG_FEMALE,//new ImageIcon(NavigatorView.class,"NavMotherOn.gif"),
    imgNavMotherOff        = imgNavMotherOn.getDisabled(gp),
    imgNavMalePartnerOn    = Indi.IMG_MALE,//new ImageIcon(NavigatorView.class,"NavMalePartnerOn.gif"),
    imgNavMalePartnerOff   = imgNavMalePartnerOn.getDisabled(gp),
    imgNavFemalePartnerOn  = Indi.IMG_FEMALE,//new ImageIcon(NavigatorView.class,"NavFemalePartnerOn.gif"),
    imgNavFemalePartnerOff = imgNavFemalePartnerOn.getDisabled(gp);


  /** the label holding information about the current individual */
  private JLabel labelCurrent, labelSelf;
  
  /** the current individual */
  private Indi current;
  
  /** jumps per key */
  private Map key2jumps = new HashMap();
  
  /** popups per key */
  private Map key2popup = new HashMap();
  
  /** the gedcom */
  private Gedcom gedcom;
  
  /** the view manager */
  private ViewManager manager;

  /**
   * Constructor
   */
  public NavigatorView(String title, Gedcom useGedcom, Registry useRegistry, ViewManager useManager) {
    
    // remember
    manager = useManager;
    gedcom = useGedcom;
    
    // layout    
    setLayout(new BorderLayout());

    labelCurrent = new JLabel();
    labelCurrent.setBorder(BorderFactory.createTitledBorder(Gedcom.getNameFor(Gedcom.INDIVIDUALS,false)));
    add(labelCurrent,BorderLayout.NORTH);
    
    JPanel panel = createPopupPanel();
    add(panel,BorderLayout.CENTER);
    
    // date
    useGedcom.addListener(new GedcomListener() {
      public void handleChange(Change change) {
        if (change.getEntities(change.EDEL).contains(current)) setCurrentEntity(null);
        else setCurrentEntity(current);
      }
    });
    
    // init
    Property context = manager.getContext(gedcom);
    setCurrentEntity(context!=null?context.getEntity():null);

    // done    

  }
  
  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(140,200);
  }

  /**
   * @see genj.view.ContextPopupSupport#getContextPopupContainer()
   */
  public JComponent getContextPopupContainer() {
    return null;
  }
  
  /**
   * @see genj.view.ContextPopupSupport#getContextAt(java.awt.Point)
   */
  public Context getContextAt(Point pos) {
    return null;
  }

  /**
   * @see genj.view.ContextPopupSupport#setContext(genj.gedcom.Property)
   */
  public void setContext(Property property) {
    setCurrentEntity(property.getEntity());
  }
  
  /**
   * Set the current entity
   */
  public void setCurrentEntity(Entity e) {
    
    // try to get one if entity==null
    if (e == null) {
      List list = gedcom.getEntities(Gedcom.INDIVIDUALS);
      if (!list.isEmpty()) e=(Entity)list.get(0);
    }

    // only individual
    if (e!=null&&!(e instanceof Indi)) 
      return;
    
    // forget jumps
    key2jumps.clear();
    
    // and current
    current = (Indi)e;

    // nothing?
    if (current == null) {
      // no jumps
      setJump(FATHER  , null);
      setJump(MOTHER  , null);
      setJump(OSIBLING, null);
      setJumps(PARTNER , null);
      setJump(YSIBLING, null);
      setJumps(CHILD   , null);
      // update label
      labelCurrent.setText("n/a");
      labelCurrent.setIcon(null);
    } else {
      // jumps
      setJump (FATHER  , current.getFather());
      setJump (MOTHER  , current.getMother());
      setJumps(OSIBLING, current.getOlderSiblings());
      setJumps(PARTNER , current.getPartners());
      setJumps(YSIBLING, current.getYoungerSiblings());
      setJumps(CHILD   , current.getChildren());
      // update label
      labelCurrent.setText(getNameOrID(current));
      labelCurrent.setIcon(current.getImage(false));

      // update the self label/partner popup images
      PopupWidget partner = getPopup(PARTNER);
      switch (current.getSex()) {
        case PropertySex.FEMALE:
          labelSelf.setIcon(imgNavFemalePartnerOn);
          partner.setIcon(imgNavMalePartnerOn);
          // FIXME partner.setRolloverIcon(imgNavMalePartnerOn);
          break;
        case PropertySex.MALE:
          labelSelf.setIcon(imgNavMalePartnerOn);
          partner.setIcon(imgNavFemalePartnerOn);
          // FIXME partner.setRolloverIcon(imgNavFemalePartnerOn);
          break;
      }

    }
          
    // done
  }
  
  /**
   * Return popup by key
   */
  private PopupWidget getPopup(String key) {
    return (PopupWidget)key2popup.get(key);  
  }

  /**
   * Resolve name or ID from indi
   */
  private String getNameOrID(Indi indi) {
    String name = indi.getName();
    if (name.length()>0) return name;
    return indi.getId();
  }
  
  /**
   * remember a jump to individual
   */
  private void setJump(String key, Indi i) {
    setJumps(key, i==null ? new Indi[0] : new Indi[]{ i });
  }
  
  /**
   * remember jumps to individuals
   */
  private void setJumps(String key, Indi[] is) {
    // lookup popup
    PopupWidget popup = getPopup(key);
    // no jumps?
    popup.setEnabled(is.length>0);
    // loop jumps
    ArrayList jumps = new ArrayList();
    for (int i=0;i<is.length;i++) {
      jumps.add(new Jump(is[i]));
    }
    popup.setActions(jumps);
    
    // done
  }
    
  /**
   * propagate the selection of an entity
   */
  private void fireCurrentEntity(Entity e) {
    if (e==null) return;
    manager.setContext(e);
  }

  /**
   * Creates a button
   */
  private JComponent createPopup(String key, ImageIcon i, ImageIcon r) {
    
    // create result
    PopupWidget result = new PopupWidget();
    result.setIcon(r);
    // FIXME
    //result.setRolloverIcon(r);
    result.setFocusPainted(false);

    // no can do pre 1.4
    try {    
      result.setFocusable(false);
    } catch (Throwable t) { 
      result.setRequestFocusEnabled(false);
    }
   
    result.setBorder(new EmptyBorder(2,2,2,2));
    result.setEnabled(false);
    result.setToolTipText(resources.getString(key));

    // remember    
    key2popup.put(key, result);
    
    // done
    return result;
  }

  /**
   * Creates the panel
   */
  private JPanel createPopupPanel() {    
    
    final String title = resources.getString("nav.navigate.title");
    final TitledBorder border = BorderFactory.createTitledBorder(title);
    final JPanel result = new PopupPanel();
    result.setBorder(border);
    GridBagHelper gh = new GridBagHelper(result);
    
    // add the buttons
    JComponent
      popFather   = createPopup(FATHER, imgNavFatherOff, imgNavFatherOn),
      popMother   = createPopup(MOTHER, imgNavMotherOff, imgNavMotherOn),
      popOSibling = createPopup(OSIBLING, imgNavOlderSiblingOff, imgNavOlderSiblingOn),
      popPartner  = createPopup(PARTNER, imgNavMalePartnerOff, imgNavMalePartnerOn),
      popYSibling = createPopup(YSIBLING, imgNavYoungerSiblingOff, imgNavYoungerSiblingOn),
      popChildren = createPopup(CHILD, imgNavChildOff, imgNavChildOn); 

    labelSelf = new JLabel(Gedcom.getImage(Gedcom.INDIVIDUALS),SwingConstants.CENTER);

    popPartner.setPreferredSize(popOSibling.getPreferredSize());
    popFather .setPreferredSize(popOSibling.getPreferredSize());
    popMother .setPreferredSize(popOSibling.getPreferredSize());
    labelSelf.setPreferredSize(popOSibling.getPreferredSize());
    
    gh.add(popFather  ,4,1,1,1);
    gh.add(popMother  ,5,1,1,1);
    gh.add(popOSibling,1,2,2,1,0,new Insets(12,0,12,12));
    gh.add(labelSelf  ,4,2,1,1);
    gh.add(popPartner ,5,2,1,1);
    gh.add(popYSibling,7,2,2,1,0,new Insets(12,12,12,0));
    gh.add(popChildren,4,3,2,1);

    // done
    return result;
  }
  
  /**
   * A panel for the popup buttons that connects 'em with lines
   */
  private class PopupPanel extends JPanel {
    /**
     * @see javax.swing.JComponent#paintChildren(java.awt.Graphics)
     */
    protected void paintChildren(Graphics g) {
    
      // paint lines
      g.setColor(Color.lightGray);
      
      line(g,getPopup(MOTHER), getPopup(OSIBLING));
      line(g,getPopup(MOTHER), getPopup(YSIBLING));
      line(g,getPopup(MOTHER), labelSelf);
      line(g,getPopup(PARTNER), getPopup(CHILD));
          
      // now paint popup buttons
      super.paintChildren(g);
    
      // done
    }
    
    /**
     * connect components (lower/left - top/center)
     */
    private void line(Graphics g, JComponent c1, JComponent c2) {
      Rectangle
        a = c1.getBounds(),
        b = c2.getBounds();
      int y = (a.y+a.height+b.y)/2;
      int x = a.x;
      g.drawLine(x,a.y+a.height,x,y);
      x = b.x+b.width/2;
      g.drawLine(x,y,x,b.y);
      g.drawLine(a.x,y,x,y);
//      g.drawLine(a.x,a.y+a.height,b.x+b.width/2,b.y);
    }
  
  } //PopupPanel

  /**
   * Jump to another indi
   */
  private class Jump extends ActionDelegate {
    /** the target */
    private Indi target;
    /** constructor */
    private Jump(Indi taRget) {
      // remember
      target = taRget;
      // our looks
      setText(getNameOrID(target));
      setImage(target.getImage(false));
    }
    /** do it */
    protected void execute() {
      fireCurrentEntity(target);
    }
  } //Jump

} ///NavigatorView
