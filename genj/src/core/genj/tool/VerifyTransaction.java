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
 * Transcation Definition - VERIFY
 */
public class VerifyTransaction implements Transaction {

  /** list of changes */
  private Vector        changes = new Vector(16);

  /** panels used */
  private VerifyResultPanel pResult;
  private SelectGedcomPanel pSelect;
  private OptionsPanel      pOptions;

  /** static texts */
  private String selectionHeader =
    "This candidate will be verified in the next few steps "+
    "to become fully Gedcom compliant and consistent.";

  private String optionsHeader =
    "Please choose the actions to be taken";

  private String optionsList[][] = {
   {"Remove duplicate IDs",
    "Entities with same IDs will get new ones",
    "0" ,
    "1" },
   {"Remove contradictory information",
    "e.g. removing a second property SEX or NAME",
    "0",
    "0"},
   {"Remove non-Gedcom information",
    "Only properties that are Gedcom compliant remain",
    "0",
    "0"},
   {"Remove unsatisfied links",
    "References like FAMC or HUSB will be removed if not linked properly",
    "0",
    "1"}
  };

  /** state of transaction (start CANDIDATE) */
  private int                 state = CANDIDATE;

  /** the TransactionPanel dealing with this transaction */
  private TransactionPanel    transactionPanel;

  /** the Gedcom we verify */
  private Gedcom        candidate;

  /** the available Gedcoms */
  private Vector        candidates;

  /** the different states of this transaction */
  private final static int
    CANDIDATE  = 0,
    OPTIONS    = 1,
    RESULT     = 2;

  /**
   * Constructor for Transaction
   */
  public VerifyTransaction(Gedcom candidate, Vector candidates) {
    this.candidate  = candidate;
    this.candidates = candidates;
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
    // Done
    return true;
  }

  /**
   * Returns the current actions for this transaction
   */
  public int getActions() {

    int result = 0;

    // What does the current state has to say about it?
    switch (state) {
      case CANDIDATE:
        if (pSelect.getGedcom()!=null) {
          result |= Transaction.NEXT;
        }
        break;
      case OPTIONS  :
        result |= Transaction.PREV;
        break;
      case RESULT   :
        return Transaction.DONE;
    }

    // Are we ready yet?
    if (isReady()) {
      result |= Transaction.OK;
    }

    // That's all
    return Transaction.CANCEL|result;
  }

  /**
   * Returns an image that represents this transaction's state
   */
  public ImgIcon getImage() {
    return new ImgIcon(this,"Verify.gif");
  }

  /**
   * Returns the current visualization for this transaction
   */
  public JPanel getPanel() {
    switch (state) {
      case CANDIDATE: default:
        pSelect.setCandidate(candidate);
        return pSelect;
      case OPTIONS  :
        return pOptions;
      case RESULT   :
        pResult.setResult(changes);
        return pResult;
    }
  }

  /**
   * Returns wether Transaction is ready for Ok/Done
   */
  private boolean isReady() {

    // Selection o.k.?
    if (pSelect.getGedcom()==null) {
      return false;
    }

    // At least one of the Options set?
    boolean[] choices = pOptions.getState();
    boolean ok=false;
    for (int i=0;i<choices.length;i++) {
      ok |= choices[i];
    }
    if (!ok) {
      return false;
    }

    // Seems to be a go!
    return true;
  }

  /**
   * Moves one further in transaction
   */
  public boolean next() {

    // Move it
    state++;

    // Done
    return true;
  }

  /**
   * Initiates last step of this transaction
   */
  public boolean ok() {

    // Lock Gedcom for write
    if (!candidate.startTransaction()) {
      return false;
    }

    // Remove all unsatisfied PropertyXRefs
    candidate.removeUnsatisfiedLinks();

    // Remove all duplicates
    candidate.removeDuplicates();

    // Unlock Gedcom
    Change change = candidate.endTransaction();

    Enumeration pdel = change.getProperties(change.PDEL).elements();
    Enumeration pmod = change.getProperties(change.PMOD).elements();

    while (pdel.hasMoreElements()) {
      Property p = (Property)pdel.nextElement();
      changes.addElement("Removed "+p.getTag()+" of "+p.getEntity());
    }

    while (pmod.hasMoreElements()) {
      Property p = (Property)pmod.nextElement();
      changes.addElement("New ID for "+p.getEntity());
    }

    // Go directly to last state
    state=RESULT;

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

    // Init our panels
    pOptions = new OptionsPanel(this,optionsHeader,optionsList);
    pSelect  = new SelectGedcomPanel(selectionHeader,candidates);
    pResult  = new VerifyResultPanel();

    ActionListener alistener = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        transactionPanel.showTransaction();
      }
    };
    pSelect.addActionListener(alistener);

    // Init our state
    state = CANDIDATE;
  }          
}
