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
import javax.swing.table.*;

import genj.gedcom.*;
import genj.util.*;
import genj.app.*;

/**
 * Transcation Definition - MERGE
 */
public class MergeTransaction implements Transaction {

  private int                 state = CANDIDATE1;
  private TransactionPanel    transactionPanel;

  private SelectGedcomPanel  pSelect1,pSelect2;
  private MatchEntitiesPanel pMatch;
  private OptionsPanel       pOptions;
  private JPanel             pResult;

  private Gedcom         candidate;
  private Vector         candidates;
  private ControlCenter  center;

  private final static int
    CANDIDATE1 = 0,
    CANDIDATE2 = 1,
    MATCH      = 2,
    OPTIONS    = 3,
    DONE       = 4;

  private String headerCandidate1 =
    "This candidate will be merged into the next one. ";

  private String headerCandidate2 =
    "This candidate will be filled with data from the first selected candidate. ";

  private String headerCandidates =
    "Please make sure first to VERIFY this candidate for "+
    "Gedcom compliancy before using it in this transaction!";

  private String headerOptions =
    "Please choose your merging options";

  private String listOptions[][] = {
   {"Tag every property of merged entities",
    "Merged entities' properties will show where they came from",
    "1" ,
    "1" },
   {"Tag every entity",
    "All entities will show where they came from",
    "1",
    "1"}
  };


  /**
   * Constructor for Transaction
   */
  public MergeTransaction(Gedcom candidate, Vector candidates, ControlCenter center) {

    // Remember data
    this.candidate =candidate;
    this.candidates=candidates;
    this.center    =center;

    // Done
  }

  /**
   * Ends this transaction
   */
  public void cancel() {
  }

  /**
   * Ends this transaction
   */
  public boolean done() {
    return true;
  }

  /**
   * Returns the current actions for this transaction
   */
  public int getActions() {

    int result = 0;

    Gedcom g1 = pSelect1.getGedcom();
    Gedcom g2 = pSelect2.getGedcom();

    switch (state) {
      case CANDIDATE1:
        if (g1!=null) {
          result |= Transaction.NEXT;
        }
        break;
      case CANDIDATE2:
        if ((g2!=null)&&(g2!=g1)) {
          result |= Transaction.NEXT;
        }
        result |= Transaction.PREV;
        break;
      case MATCH:
        result |= Transaction.PREV | Transaction.NEXT;
        break;
      case OPTIONS:
        result |= Transaction.PREV;
        break;
      case DONE:
        return Transaction.DONE;
    }

    if ( (g1!=null)&&(g2!=null)
       &&(!g1.hasDuplicates())
       &&(!g2.hasDuplicates())
       &&(g2!=g1) ) {
      result |= Transaction.OK;
    }

    return result | Transaction.CANCEL;
  }

  /**
   * Returns an image that represents this transaction's state
   */
  public ImgIcon getImage() {
    return new ImgIcon(this,"Merge.gif");
  }

  /**
   * Returns the current visualization for this transaction
   */
  public JPanel getPanel() {

    // Return next panel
    switch (state) {
      case CANDIDATE1:
        return pSelect1;
      case CANDIDATE2:
        return pSelect2;
      case MATCH:
        pMatch.setCandidates(pSelect1.getGedcom(),pSelect2.getGedcom());
        return pMatch;
      case OPTIONS:
        return pOptions;
      case DONE:
        return pResult;
    }

    // Hmmm ... unknown state
    throw new NoSuchElementException();
  }

  /**
   * Merge the candidates technically
   */
  private void merge() {

    Gedcom g1 = pSelect1.getGedcom(),
           g2 = pSelect2.getGedcom();

    // Lock Candidates
    RuntimeException e = new RuntimeException("Couldn't lock candidate");
    if (!g1.startTransaction())
      throw e;
    if (!g2.startTransaction())
      throw e;

    // Prepare matches
    Entity[][] matches = pMatch.getMatches();

    // Do the Merge
    boolean state[] = pOptions.getState();

    Gedcom result = Gedcom.merge(
      g1,
      g2,
      matches,
       (state[0]?Gedcom.TAG_PROPERTY_SOURCE:0)
      |(state[1]?Gedcom.TAG_ENTITY_SOURCE  :0)
    );

    // Finish work by unlocking
    g1.endTransaction();
    g2.endTransaction();

    // Done
    center.addGedcom(result);
  }

  /**
   * Moves one further in transaction
   */
  public boolean next() {

    // Check wether selected candidate is o.k.
    Gedcom g=null;
    switch (state) {
      case CANDIDATE1:
        g = pSelect1.getGedcom();
        break;
      case CANDIDATE2:
        g = pSelect2.getGedcom();
        break;
    }

    if ((g!=null)&&(g.hasDuplicates())) {

      JOptionPane.showMessageDialog(
        transactionPanel.getFrame(),
         "This Gedcom file has entities with duplicate IDs.\n"
        +"Please VERIFY it before using it here or choose another one.",
         "Achtung",
        JOptionPane.ERROR_MESSAGE
      );

      return false;
    }

    // Move it
    state++;

    // Done
    return true;
  }

  /**
   * Initiates last step of Transaction
   */
  public boolean ok() {

    // First make sure the matching entities shows the correct candidates
    pMatch.setCandidates(pSelect1.getGedcom(),pSelect2.getGedcom());

    // Merge the candidates
    merge();

    // Change state
    state=DONE;

    // Done
    return true;
  }

  /**
   * Moves one back in transaction
   */
  public boolean prev() {

    // Move it
    state--;

    // Done
    return true;
  }

  /**
   * Set the TransactionPanel to use
   */
  public void setPanel(TransactionPanel set) {
    transactionPanel=set;
  }

  /**
   * Starts this transaction
   */
  public void start() {

    //state
    state = CANDIDATE1;

    // Our panels
    pResult = new JPanel();
    pResult.add(new JLabel("This feature is alpha!"));
    pResult.add(new JLabel("Please save now and reload!"));
    pResult.add(new JLabel("Keep a copy of the originals!"));

    pSelect1 = new SelectGedcomPanel(
      headerCandidate1+headerCandidates,
      candidates
    );
    pSelect1.setCandidate(candidate);

    pSelect2 = new SelectGedcomPanel(
      headerCandidate2+headerCandidates,
      candidates
    );

    ActionListener alistener = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        transactionPanel.showTransaction();
      }
    };
    pSelect1.addActionListener(alistener);
    pSelect2.addActionListener(alistener);

    pMatch = new MatchEntitiesPanel();

    pOptions = new OptionsPanel(this,headerOptions,listOptions);


    // Done
  }
}
