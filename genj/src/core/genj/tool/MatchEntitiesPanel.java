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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import genj.gedcom.*;
import genj.util.*;
import genj.util.swing.ButtonHelper;
import genj.app.*;

/**
 * A panel allowing to match entities that show up on a left/right list
 * (used in MergeTransaction)
 */
class MatchEntitiesPanel extends JPanel implements GedcomListener {

  private JTable   tMatch;
  private JButton  bDont,bDontAll;
  private Gedcom[] candidates = new Gedcom[2];

  private String   header =
    "These entities will be merged (their IDs match):";

  /**
   * Constructor
   */
  /*package*/ MatchEntitiesPanel() {

    // Create Table for joining info
    tMatch = new JTable(new MatchModel());
    tMatch.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      /**
       * @see javax.swing.event.ListSelectionListener#valueChanged(ListSelectionEvent)
       */
      public void valueChanged(ListSelectionEvent e) {
        bDont   .setEnabled(tMatch.getSelectedRowCount()>0);
        bDontAll.setEnabled(tMatch.getRowCount()>0);
      }
    });
  
    ButtonHelper bh = new ButtonHelper().setInsets(new Insets(0,0,0,0));
    bDont    = bh.setEnabled(false).create(new ActionMatch(false));
    bDontAll = bh.setEnabled(true).create(new ActionMatch(true));
  
    JPanel pActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    pActions.add(bDont);
    pActions.add(bDontAll);
  
    // Create master panel
    setLayout(new BorderLayout());
  
    add(new JLabel(header)     ,"North");
    add(new JScrollPane(tMatch),"Center");
    add(pActions               ,"South" );
  
    // Done
  }

  /**
   * Returns a list of matching entities for further processing
   */
  public Entity[][] getMatches() {

    // Check model
    MatchModel matches = (MatchModel)tMatch.getModel();

    // Make result array
    Entity[][] result = new Entity[matches.getRowCount()][2];

    // Fill array
    for (int r=0;r<matches.getRowCount();r++) {
      MatchModel.Match m = matches.getMatchAt(r);
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

  /**
   * Action - Match
   */
  private class ActionMatch extends ActionDelegate {
    /** all? */
    private boolean all;
    /** constructor */
    protected ActionMatch(boolean a) {
      all=a;
      if (all) super.setText("Match none");
      else super.setText("Don't match");
    }
    /** run */
    protected void execute() {
      MatchModel matches = (MatchModel)tMatch.getModel();
      if (all) {
        matches.removeAll();
      } else {
        matches.remove(tMatch.getSelectedRows());
      }
    }
  } //ActionMatch
  
  /**
   * Class which models matching entities of two Gedcoms
   */
  private class MatchModel extends AbstractTableModel {
    
    /** matches we know about */
    private Vector matches = new Vector();
    
    /**
     * Constructor
     */
    protected MatchModel() {

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
        Debug.log(Debug.WARNING, this, e);
      }

      // Done
    }

    /**
     * Removes all matches from this model
     */
    protected void removeAll() {
      int s = matches.size();
      matches.removeAllElements();
      super.fireTableRowsDeleted(0,s-1);
    }

    /**
     * Removes a selection of matches from this model
     */
    protected void remove(int[] selection) {
      if (selection.length==0) return;
      for (int i=selection.length-1; i>=0; i--) {
        matches.removeElementAt(selection[i]);
      }
      super.fireTableRowsDeleted(selection[0],selection[selection.length-1]);
    }

    /**
     * Returns the match for given row
     */
    public Match getMatchAt(int row) {
      return (Match)matches.elementAt(row);
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
      return candidates.length;
    }
    /**
     * @see javax.swing.table.TableModel#getColumnName(int)
     */
    public String getColumnName(int columnIndex) {
      return (candidates[columnIndex]==null?"?":candidates[columnIndex].getName());
    }
    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
      return matches.size();
    }
    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int col) {
      Match m = (Match)matches.elementAt(row);
      if (col==0) return m.e1;
      return m.e2;
    }

    /**
     * A Match of two entities
     */
    private class Match {
      Entity e1,e2;
      Match(Entity e1, Entity e2) {
        this.e1=e1;this.e2=e2;
      }
    } //Match

  }
  
}
