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
package genj.tool;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import genj.gedcom.*;
import genj.util.*;
import genj.util.swing.ButtonHelper;
import awtx.table.*;
import genj.app.*;
import awtx.Table;

/**
 * A panel allowing to match entities that show up on a left/right list
 * (used in MergeTransaction)
 */
class MatchEntitiesPanel extends JPanel implements GedcomListener {

  private Table    tMatch;
  private JButton  bDont,bDontAll;
  private Gedcom[] candidates = new Gedcom[2];

  private String   header =
    "These entities will be merged (their IDs match):";

  /**
   * Class which models matching entities of two Gedcoms
   */
  class MatchModel extends AbstractTableModel {

    private Vector matches = new Vector();
    private Vector breaks  = new Vector();

    /**
     * Constructor
     */
    /*package*/ MatchModel() {

      // Are we ready?
      for (int i=0;i<candidates.length;i++)
      if (candidates[i]==null)
        return;

      // Loop through ents
      try {

        EntityList[] entlists = candidates[0].getEntities();
        Vector ms = new Vector();

        for (int i=0;i<entlists.length;i++) {

          for (int e=0;e<entlists[i].getSize();e++) {

            // .. get entity
            Entity e1 = entlists[i].get(e);
            Entity e2 = candidates[1].getEntityFromId(e1.getId(),e1.getType());

            // .. and twin
            if (e2!=null) {
              ms.addElement(new Match(e1,e2));
            }

            // .. next entity
          }

          // .. next list of entities
        }

        // remember matches
        matches = ms;

      } catch (Exception e) {
        System.out.println(e);
        e.printStackTrace();
      }

      // Done
    }

    /**
     * Removes all matches from this model
     */
    void removeAll() {
      matches.removeAllElements();
      fireNumRowsChanged();
    }

    /**
     * Removes a selection of matches from this model
     */
    void remove(int selection) {

      if (matches.size()>selection) {
        matches.removeElementAt(selection);
      }

      fireNumRowsChanged();
    }

    /**
     * Returns number of Rows - that's the number of joined entities
     */
    public int getNumRows() {
      return matches.size();
    }

    /**
     * Returns number of Columns for initialization
     */
    public int getNumColumns() {
      return candidates.length;
    }

    /**
     * Returns the Cell Object row,col - that's one of the entities
     */
    public Object getObjectAt(int row, int col) {

      Match m = getMatchAt(row);

      switch (col) {
      case 0: default:
        return m.e1.toString();
      case 1:
        return m.e2.toString();
      }
    }

    /**
     * Returns a column name
     */
    public Object getHeaderAt(int col) {

      switch (col) {
      case 0: default:
        return (candidates[0]==null?"?":candidates[0].getName());
      case 1:
        return (candidates[1]==null?"?":candidates[1].getName());
      }

    }

    /**
     * Returns the match for given row
     */
    public Match getMatchAt(int row) {
      return (Match)matches.elementAt(row);
    }

    // EOC
  }

  /**
   * A Match of two entities
   */
  class Match {
    Entity e1,e2;
    Match(Entity e1, Entity e2) {
      this.e1=e1;this.e2=e2;
    }
    // EOC
  }

  /**
   * Constructor
   */
  /*package*/ MatchEntitiesPanel() {

  // Create Table for joining info
  tMatch = new Table();
  tMatch.setModel(new MatchModel());

  TableListener tlistener = new TableListener() {
    // LCD
    /** Handle selection */
    public void rowSelectionChanged(int[] rows) {
      bDont   .setEnabled(rows.length>0);
      bDontAll.setEnabled(tMatch.getNumRows()>0);
    }
    /** action performed on row */
    public void actionPerformed(int row) {
    }
    // EOC
  };
  tMatch.addTableListener(tlistener);

  // Create buttons for modifications
  ActionListener alistener = new ActionListener() {
    // LCD
    /** action performed */
    public void actionPerformed(ActionEvent e) {
      // Break all matches
      if (e.getActionCommand().equals("DONTALL")) {
        dontMatches(true);
        return;
      }
      // Break single match
      if (e.getActionCommand().equals("DONT")) {
        dontMatches(false);
        return;
      }
    // Done
    }
    // EOC
  };
  
  ButtonHelper bh = new ButtonHelper().setListener(alistener).setInsets(new Insets(0,0,0,0));
  bDont    = bh.setText("Don't match").setAction("DONT"   ).setEnabled(false).create();
  bDontAll = bh.setText("Match none" ).setAction("DONTALL").setEnabled(true ).create();

  JPanel pActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
  pActions.add(bDont);
  pActions.add(bDontAll);

  // Create master panel
  setLayout(new BorderLayout());

  add(new JLabel(header),"North");
  add(tMatch            ,"Center");
  add(pActions          ,"South" );

  // Done
  }

  /**
   * Break Matches (entities' ID will be changed later)
   */
  void dontMatches(boolean all) {

    // Anything to do?
    if ((!all)&&(tMatch.getSelectedRow()==-1)) {
      return;
    }

    MatchModel matches = (MatchModel)tMatch.getModel();
    if (all) {
      matches.removeAll();
    } else {
      matches.remove(tMatch.getSelectedRow());
    }

    // Done
  }

  /**
   * Returns a list of matching entities for further processing
   */
  public Entity[][] getMatches() {

    // Check model
    MatchModel matches = (MatchModel)tMatch.getModel();

    // Make result array
    Entity[][] result = new Entity[matches.getNumRows()][2];

    // Fill array
    for (int r=0;r<matches.getNumRows();r++) {
      Match m = matches.getMatchAt(r);
      result[r][0] = m.e1;
      result[r][1] = m.e2;
    }

    // Done
    return result;
  }

  /**
   * Notification that a change in a Gedcom-object took place.
   */
  public void handleChange(Change change) {

    if ((change!=null)&&(change.getCause()==this)) {
      return;
    }

    // Recalculate Matches
    tMatch.setInitialColumnLayout(tMatch.SIZE_ALL_COLUMNS);
    tMatch.setModel(new MatchModel());

    // Done
  }

  /**
   * Notification that a Gedcom has been closed
   */
  public void handleClose(Gedcom which) {
    // FIXME
  }

  /**
   * Notification that an entity has been selected.
   */
  public void handleSelection(Selection selection) {
  }

  /**
   * Component is not used anymore
   */
  public void removeNotify() {

    super.removeNotify();

    for (int i=0;i<candidates.length;i++) {
      if (candidates[i]!=null)
      candidates[i].removeListener(this);
    }
  }

  /**
   * Re-sets the candidates to use
   */
  void setCandidates(Gedcom candidate1, Gedcom candidate2) {

    // Known?
    if (candidates!=null) {
      if ((candidate1==candidates[0])&&(candidate2==candidates[1])) {
        return;
      }
    }

    // Remove from old
    for (int i=0;i<candidates.length;i++) {
      if (candidates[i]!=null) {
        candidates[i].removeListener(this);
      }
    }

    // One null?
    if ((candidate1==null)||(candidate2==null)) {

      // .. remember empty
      candidates=new Gedcom[2];

    } else {

      // .. remember them
      candidates = new Gedcom[]{candidate1,candidate2};

      // .. add listening
      for (int i=0;i<candidates.length;i++) {
      candidates[i].addListener(this);
      }

    }

    // Show it
    handleChange(null);

    // Done
  }

}
