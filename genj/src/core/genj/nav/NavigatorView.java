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

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.Indi;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.NestedBlockLayout;
import genj.view.SelectionSink;
import genj.view.View;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;

import spin.Spin;

/**
 * A navigator with buttons to easily navigate through Gedcom data
 */
public class NavigatorView extends View {

  private final static String INDENT = "   ";
  private final static Resources RES = Resources.get(NavigatorView.class);
  private final static Registry REG = Registry.get(NavigatorView.class);
  
  private final static NestedBlockLayout LAYOUT = new NestedBlockLayout("<col>"
      +"<col><grandparents/><grandparent/></col>"
      +"<col><parents/><parent/></col>"
      +"<col><siblings/><sibling/></col>"
      +"<col><spouses/><spouse/></col>"
      +"<col><children/><child/></col>"
      +"<col><grandchildren/><grandchild/></col>"
      +"</col>");
  
//  private final static ImageIcon
//    imgYSiblings = new ImageIcon(NavigatorView.class,"YSiblings"),
//    imgOSiblings = new ImageIcon(NavigatorView.class,"OSiblings"),
//    imgChildren  = new ImageIcon(NavigatorView.class,"Children"),
//    imgFather    = new ImageIcon(NavigatorView.class,"Father"),
//    imgMother    = new ImageIcon(NavigatorView.class,"Mother"),
//    imgMPartner  = Indi.IMG_MALE,
//    imgFPartner  = Indi.IMG_FEMALE;


  private GedcomListener callback = (GedcomListener)Spin.over(new GedcomListenerAdapter() {
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
      if (context!=null&&context.getEntity()==entity)
        setContext(new Context(gedcom), true);
    }
  });
  
  /** the current context */
  private Context context = new Context();

  /**
   * Constructor
   */
  public NavigatorView() {
    setLayout(LAYOUT);
    setBackground(Color.WHITE);
  }

  /**
   * context changer
   */
  @Override
  public void setContext(Context context, boolean isActionPerformed) {
    
    // disconnect
    if (this.context.getGedcom()!=null) {
      this.context.getGedcom().removeGedcomListener(callback);
      this.context = new Context();
    }
    
    removeAll();
    
    // person or family?
    if (context.getEntities().size()==1) {
      if (context.getEntity() instanceof Indi)
        setIndi((Indi)context.getEntity());
      if (context.getEntity() instanceof Fam)
        setFam((Fam)context.getEntity());
    }

    // show
    revalidate();
    repaint();
  }
  
  private void addExpander(String key) {
    NestedBlockLayout.Expander expander = new NestedBlockLayout.Expander(RES.getString(key));
    Registry r = new Registry(REG, key);
    expander.setCollapsed(r.get("folded", false));
    expander.addPropertyChangeListener("folded", r);
    add(key, expander);
  }
  
  private void setFam(Fam fam) {
    addExpander("parents");
    addExpander("children");

    Indi husband = fam.getHusband();
    if (husband!=null)
      add("parent"       , new JLabel(INDENT+husband.toString()));
    Indi wife = fam.getWife();
    if (wife!=null)
      add("parent"       , new JLabel(INDENT+wife.toString()));
    if (husband==null||wife==null)
      add("parent"       , new JLabel(INDENT+"<create>"));

    Indi[] children = fam.getChildren();
    for (Indi child : children)
      add("child"       , new JLabel(INDENT+child.toString()));
    add("child"         , new JLabel(INDENT+"<create>"));
    
  }    
  
  private void setIndi(Indi indi) {
  
    addExpander("grandparents" );
    addExpander("parents"      );
    addExpander("siblings"     );
    addExpander("spouses"      );
    addExpander("children"     );
    addExpander("grandchildren");
    
    // connect
    context = new Context(indi);
    context.getGedcom().addGedcomListener(callback);
    
    List<Indi> grandparents = getParents(indi.getParents());
    for (Indi grandparent : grandparents)
      add("grandparent"       , new JLabel(INDENT+grandparent.toString()));
    if (grandparents.size()<4)
      add("grandparent"       , new JLabel(INDENT+"<create>"));

    List<Indi> parents = indi.getParents();
    for (Indi parent : parents)
      add("parent"       , new JLabel(INDENT+parent.toString()));
    if (parents.size()<2)
      add("parent"       , new JLabel(INDENT+"<create>"));
    
    Indi[] siblings = indi.getSiblings(false);
    for (Indi sibling : siblings)
      add("sibling"       , new JLabel(INDENT+sibling.toString()));
    add("sibling"       , new JLabel(INDENT+"<create>"));

    Indi[] spouses = indi.getPartners();
    for (Indi spouse : spouses)
      add("spouse"       , new JLabel(INDENT+spouse.toString()));
    add("spouse"       , new JLabel(INDENT+"<create>"));

    Indi[] children = indi.getChildren();
    for (Indi child : children)
      add("child"       , new JLabel(INDENT+child.toString()));
    add("child"         , new JLabel(INDENT+"<create>"));
    
    List<Indi> grandchildren = getChildren(Arrays.asList(children));
    for (Indi grandchild : grandchildren)
      add("grandchild"  , new JLabel(INDENT+grandchild.toString()));
    add("grandchild"   , new JLabel(INDENT+"<create>"));
    
  }
  
  private List<Indi> getParents(List<Indi> parents) {
    List<Indi> result = new ArrayList<Indi>(4);
    for (Indi parent : parents)
      result.addAll(parent.getParents());
    return result;
  }
  
  private List<Indi> getChildren(List<Indi> children) {
    List<Indi> result = new ArrayList<Indi>(16);
    for (Indi child : children)
      result.addAll(Arrays.asList(child.getChildren()));
    return result;
  }
  
  /**
   * Jump to another indi
   */
  private class Jump extends Action2 {
    /** the target */
    private Indi target;
    /** constructor */
    private Jump(Indi taRget) {
      // remember
      target = taRget;
      // our looks
      setText(target.toString());
      setImage(target.getImage(false));
    }
    /** do it */
    public void actionPerformed(ActionEvent event) {
      SelectionSink.Dispatcher.fireSelection(event, new Context(target));
    }
  } //Jump

} //NavigatorView
