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
package genj.search;

import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.util.ActionDelegate;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.ImageIcon;
import genj.view.ContextSupport;
import genj.view.ToolBarSupport;

import java.awt.BorderLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

/**
 * View for searching
 */
public class SearchView extends JPanel implements ToolBarSupport, ContextSupport {
  
  /** resources */
  /*package*/ static Resources resources = Resources.get(SearchView.class);
  
  /** gedcom */
  private Gedcom gedcom;
  
  /** registry */
  private Registry registry;
  
  /** shown results */
  private JList listResults;
  
  /** criterias */
  private ChoiceWidget choicePath, choiceValue;

  /** history */
  private List oldPaths, oldValues;
  
  /** buttons */
  private AbstractButton bSearch, bStop;
  
  /** images */
  private final static ImageIcon
    IMG_START = new ImageIcon(SearchView.class, "Start.gif"),
    IMG_STOP  = new ImageIcon(SearchView.class, "Stop.gif ");

  /**
   * Constructor
   */
  public SearchView(Gedcom geDcom, Registry reGistry) {
    
    // remember
    gedcom = geDcom;
    registry = reGistry;
    
    // lookup old search values
    oldPaths = (List)registry.get("old.paths", new ArrayList());
    oldValues = (List)registry.get("old.values", new ArrayList());
    
    // prepare results
    listResults = new JList(new Object[]{ "!!!Search is inop!!!"});
    
    // prepare search criteria
    choicePath = new ChoiceWidget(oldPaths);
    choiceValue = new ChoiceWidget(oldValues);
    
    JCheckBox checkEntities = new JCheckBox("Entities only");
    JCheckBox checkRegExp = new JCheckBox("Regular Expressions", true);

    JPanel paneCriteria = new JPanel();
    GridBagHelper gh = new GridBagHelper(paneCriteria);
    gh.add(new JLabel("Value")   ,0,0,2,1);
    gh.add(choiceValue           ,0,1,2,1, gh.GROW_HORIZONTAL|gh.FILL_HORIZONTAL);
    gh.add(new JLabel("Tag Path"),0,2,2,1);
    gh.add(choicePath            ,0,3,2,1, gh.GROW_HORIZONTAL|gh.FILL_HORIZONTAL);
    gh.add(checkEntities         ,0,4,1,1);
    gh.add(checkRegExp           ,1,4,1,1);
    
    // prepare layout
    setLayout(new BorderLayout());
    add(BorderLayout.NORTH , paneCriteria);
    add(BorderLayout.CENTER, new JScrollPane(listResults) );
    choiceValue.requestFocus();

    // done
  }
  
  /**
   * @see genj.view.ToolBarSupport#populate(javax.swing.JToolBar)
   */
  public void populate(JToolBar bar) {
    ButtonHelper bh = new ButtonHelper().setContainer(bar);
    bSearch = bh.setEnabled(true ).create(new ActionSearch());
    bStop   = bh.setEnabled(false).create(new ActionStop  ());
  }
  
  /**
   * @see genj.view.ContextSupport#getContextAt(java.awt.Point)
   */
  public Context getContextAt(Point pos) {
    return new Context(null);
  }  

  /**
   * @see genj.view.ContextSupport#getContextPopupContainer()
   */
  public JComponent getContextPopupContainer() {
    return listResults;
  }

  /**
   * @see genj.view.ContextSupport#setContext(genj.gedcom.Property)
   */
  public void setContext(Property property) {
    // ignored
  }

  /**
   * Action - trigger search
   */
  private class ActionSearch extends ActionDelegate {
    /** constructor */
    private ActionSearch() {
      setImage(IMG_START);
    }
    /** run */
    protected void execute() {
    }
  } //ActionSearch
  
  /**
   * Action - stop search
   */
  private class ActionStop extends ActionDelegate {
    /** constructor */
    private ActionStop() {
      setImage(IMG_STOP);
    }
    /** run */
    protected void execute() {
    }
  } //ActionStop


} //SearchView