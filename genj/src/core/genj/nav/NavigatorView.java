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
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.Resources;
import genj.view.ContextSupport;
import genj.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * A navigator with buttons to easily navigate through Gedcom data
 */
public class NavigatorView extends JPanel implements ContextSupport {
  
  private static Resources resources = Resources.get(NavigatorView.class);

  private final static String 
    TIP_FATHER   = resources.getString("tip.father"),
    TIP_MOTHER   = resources.getString("tip.mother"),
    TIP_YSIBLING = resources.getString("tip.ysibling"),
    TIP_OSIBLING = resources.getString("tip.osibling"),
    TIP_PARTNER  = resources.getString("tip.partner"),
    TIP_CHILD    = resources.getString("tip.child");

  /** entity per tip */
  private Map tip2indi = new HashMap();
  
  /** the label holding information about the current individual */
  private JLabel labelName;
  private TitledBorder borderName;
  
  /** the current individual */
  private Indi current;
  
  /** the buttons */
  private Map tip2button = new HashMap();
  
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

    borderName = BorderFactory.createTitledBorder("");
    labelName = new JLabel();
    labelName.setFont(new Font("Arial", Font.PLAIN, 10));
    labelName.setBorder(borderName);
    add(labelName,BorderLayout.NORTH);
    
    JPanel panel = createPanel();
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
   * Set jump 
   */
  private void setJump(String tip, Entity e) {
    JButton b = (JButton)tip2button.get(tip);
    if (e==null) {
      tip2indi.remove(tip);
      b.setEnabled(false);
    } else {
      tip2indi.put(tip, e);
      b.setEnabled(true);
    }
  }
  
  /**
   * Sets the label and title
   */
  private void setLabel(String title, Indi indi) {
    if (title==null) title = Gedcom.getNameFor(Gedcom.INDIVIDUALS,false);
    borderName.setTitle(title);
    labelName.setText(indi!=null ? indi.getName() : "n/a");
    repaint();
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
    tip2indi.clear();
    
    // and current
    current = (Indi)e;

    // update label
    setLabel(null, current);
    
    // nothing?
    if (current == null) {
      // buttons
      Iterator buttons = tip2button.values().iterator();
      while (buttons.hasNext()) {
        ((JButton)buttons.next()).setEnabled(false);
      }
    } else {
      // buttons
      setJump(TIP_FATHER  , current.getFather());
      setJump(TIP_MOTHER  , current.getMother());
      setJump(TIP_OSIBLING, current.getOlderSibling());
      setJump(TIP_PARTNER , current.getPartners().length==0?null:current.getPartners()[0]);
      setJump(TIP_YSIBLING, current.getYoungerSibling());
      setJump(TIP_CHILD   , current.getChildren().length==0?null:current.getChildren()[0]);
    }
          
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
  private JButton createButton(String key, ImageIcon i, ImageIcon r, ActionListener al, MouseListener ml) {
    
    // create result
    JButton result = new JButton();
    result.setIcon(i);
    result.setRolloverIcon(r);
    result.setFocusable(false);
    result.setBorder(null);
    result.setEnabled(false);
    result.setActionCommand(key);
    result.addActionListener(al);
    result.setToolTipText(key);
    result.addMouseListener(ml);

    // remember    
    tip2button.put(key, result);
    
    // done
    return result;
  }
  
  /**
   * Creates the panel
   */
  private JPanel createPanel() {    
    
    JPanel result = new JPanel();    
    result.setBorder(BorderFactory.createTitledBorder(resources.getString("nav.navigate.title")));
    GridBagHelper gh = new GridBagHelper(result);
    
    // create listener
    ActionListener al = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fireCurrentEntity((Entity)tip2indi.get(e.getActionCommand()));
      }
    };
    
    MouseListener ml = new MouseAdapter() {
      /** show preview */
      public void mouseEntered(MouseEvent e) {
        String tip = ((JButton)e.getSource()).getActionCommand();
        setLabel(tip, (Indi)tip2indi.get(tip));
      }
      /** restore current */
      public void mouseExited(MouseEvent e) {
        setLabel(null, current);
      }
    };
    
    // add the buttons
    gh.add(
      createButton(TIP_FATHER, Images.imgNavFatherOff, Images.imgNavFatherOn, al, ml) 
      ,2,1,1,1
    );
    gh.add(
      createButton(TIP_MOTHER, Images.imgNavMotherOff, Images.imgNavMotherOn, al, ml) 
      ,3,1,1,1
    );
    gh.add(
      createButton(TIP_OSIBLING, Images.imgNavOlderSiblingOff, Images.imgNavOlderSiblingOn, al, ml) 
      ,0,2,2,1
    );
    gh.add(
      createButton(TIP_PARTNER, Images.imgNavPartnerOff, Images.imgNavPartnerOn, al, ml)
      ,2,2,2,1,0,new Insets(12,0,12,0)
    );
    gh.add(
      createButton(TIP_YSIBLING, Images.imgNavYoungerSiblingOff, Images.imgNavYoungerSiblingOn, al, ml)
      ,4,2,2,1
    );
    gh.add(
      createButton(TIP_CHILD, Images.imgNavChildOff, Images.imgNavChildOn, al, ml)  
      ,2,3,2,1
    );

    // done
    return result;
  }

} ///NavigatorView
