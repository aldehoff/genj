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

import genj.edit.actions.CreateChild;
import genj.edit.actions.CreateParent;
import genj.edit.actions.CreateSibling;
import genj.edit.actions.CreateSpouse;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.Indi;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.DialogHelper;
import genj.util.swing.NestedBlockLayout;
import genj.view.SelectionSink;
import genj.view.View;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

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
  
  private void addExpander(String key, boolean more) {
    String label = RES.getString(key);
    NestedBlockLayout.Expander expander = new NestedBlockLayout.Expander(label,more?label+"...":label);
    Registry r = new Registry(REG, key);
    expander.setCollapsed(r.get("folded", false));
    expander.addPropertyChangeListener("folded", r);
    add(key, expander);
  }
  
  private void setFam(Fam fam) {

    Indi husband = fam.getHusband();
    if (husband!=null)
      add("parent"       , indi(husband));
    Indi wife = fam.getWife();
    if (wife!=null)
      add("parent"       , indi(wife));
    if (husband==null||wife==null)
      add("parent"       , create(new CreateParent(fam)));

    Indi[] children = fam.getChildren();
    for (Indi child : children)
      add("child"       , indi(child));
    add("child"         , create(new CreateChild(fam, true)));
    
    addExpander("parents", husband!=null||wife!=null);
    addExpander("children", children.length>0);
  }    
  
  private void setIndi(Indi indi) {
  
    // connect
    context = new Context(indi);
    context.getGedcom().addGedcomListener(callback);
    
    List<Indi> grandparents = getParents(indi.getParents());
    addExpander("grandparents", !grandparents.isEmpty());
    for (Indi grandparent : grandparents)
      add("grandparent", indi(grandparent));

    List<Indi> parents = indi.getParents();
    addExpander("parents", !parents.isEmpty());
    for (Indi parent : parents)
      add("parent", indi(parent));
    if (parents.size()<2)
      add("parent", create(new CreateParent(indi)));
    
    Indi[] siblings = indi.getSiblings(false);
    addExpander("siblings", siblings.length>0);
    for (Indi sibling : siblings)
      add("sibling", indi(sibling));
    add("sibling", create(new CreateSibling(indi, true)));

    Indi[] spouses = indi.getPartners();
    addExpander("spouses", spouses.length>0);
    for (Indi spouse : spouses)
      add("spouse", indi(spouse));
    if (spouses.length==0)
      add("spouse", create(new CreateSpouse(indi)));

    Indi[] children = indi.getChildren();
    addExpander("children", children.length>0);
    for (Indi child : children)
      add("child", indi(child));
    add("child", create(new CreateChild(indi, true)));
    
    List<Indi> grandchildren = getChildren(Arrays.asList(children));
    addExpander("grandchildren",!grandchildren.isEmpty());
    for (Indi grandchild : grandchildren)
      add("grandchild", indi(grandchild));
    
  }
  
  private JLabel create(Action action) {
    JLabel result = new JLabel("["+RES.getString("create")+"]");
    Color c = result.getForeground();
    result.setForeground(new Color(c.getRed(), c.getGreen(), c.getBlue(), 128));
    result.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
    result.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    result.putClientProperty(Action.class, action);
    result.addMouseListener(CLICK);
    result.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return result;
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
  
  private JLabel indi(Indi indi) {
    JLabel result = new JLabel(indi.toString(), indi.getImage(), SwingConstants.LEFT);
    result.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
    result.putClientProperty(Indi.class, indi);
    result.addMouseListener(CLICK);
    result.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return result;
  }

  private final static MouseListener CLICK = new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent e) {
      
      JLabel label = (JLabel)e.getComponent();
      Indi target = (Indi)label.getClientProperty(Indi.class);
      if (target!=null)
        SelectionSink.Dispatcher.fireSelection(DialogHelper.getComponent(e), new Context(target), false);
      Action action = (Action)label.getClientProperty(Action.class);
      if (action!=null)
        action.actionPerformed(new ActionEvent(e.getSource(), 0, "", e.getModifiers()));

    }
  };

} //NavigatorView
