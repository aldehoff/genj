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
package genj.app;

import javax.swing.*;

import java.util.Vector;
import genj.util.swing.ImgIconConverter;
import java.awt.*;
import java.awt.event.*;

/**
 * TransactionPanel - a input transaction through several panels
 */
public class TransactionPanel extends JPanel implements ActionListener {

  /** members */
  private Transaction transaction;
  private JButton bNext,bPrev,bCancel,bOK;
  private JPanel pState;
  private Frame frame;
  private CardLayout pStateLayout;

  private Vector vPanels = new Vector(8);

  /**
   * Constructor
   */
  public TransactionPanel(Frame frame,Transaction transaction) {

    this.transaction = transaction;
    this.frame = frame;

    transaction.setPanel(this);

    // Panel for Image
    JPanel pimage = new JPanel(new BorderLayout(2,2));
    pimage.add(
      new JLabel(ImgIconConverter.get(transaction.getImage())),
      "Center"
    );

    // Panel for Actions
    JPanel paction = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    bNext  = createAction(App.resources.getString("transaction_panel.action.next")  ,"NEXT"  );
    bPrev  = createAction(App.resources.getString("transaction_panel.action.back")  ,"PREV"  );
    bCancel= createAction(App.resources.getString("transaction_panel.action.cancel"),"CANCEL");
    bOK    = createAction(App.resources.getString("transaction_panel.action.ok")    ,"OK"  );

    paction.add(bPrev  );
    paction.add(bNext  );
    paction.add(bCancel);
    paction.add(bOK    );

    // Panel for Transaction State(s)
    pStateLayout = new CardLayout();
    pState = new JPanel(pStateLayout);

    // Layout
    setLayout(new BorderLayout());
    add(paction,"South" );
    add(pimage ,"West"  );
    add(pState ,"Center");

    // Done
  }

  /**
   * Listen to ActionEvents
   */
  public void actionPerformed(ActionEvent e) {

    // Last step of transaction?
    if (e.getActionCommand().equals("OK")) {
      if (transaction.ok())
        showTransaction();
      return;
    }

    // Done with transaction?
    if (e.getActionCommand().equals("DONE")) {
      if (transaction.done())
        frame.dispose();
      return;
    }

    // Cancel transaction?
    if (e.getActionCommand().equals("CANCEL")) {
      // .cancel() is called when component is removed
      frame.dispose();
      return;
    }

    // Next step of transaction?
    if (e.getActionCommand().equals("NEXT")) {
      if (transaction.next())
        showTransaction();
      return;
    }

    // Previous step of transaction?
    if (e.getActionCommand().equals("PREV")) {
      if (transaction.prev())
        showTransaction();
      return;
    }

    // Unknown
  }

  /**
   * Panel is used
   */
  public void addNotify() {

    // Start Transaction
    transaction.start();

    // Show it
    showTransaction();

    // Done
    super.addNotify();
  }

  /**
   * Helper that creates an action
   */
  private JButton createAction(String name, String id) {
    JButton result = new JButton(name);
    result.setActionCommand(id);
    result.addActionListener(this);
    return result;
  }

  /**
   * Returns the frame in which this transaction is shown
   */
  public Frame getFrame() {
    return frame;
  }

  /**
   * Helper for checking actions
   */
  private boolean is(int test,int action) {
    return (test & action)!=0;
  }

  /**
   * Panel isn't used anymore
   */
  public void removeNotify() {

    super.removeNotify();

    // End Transaction
    transaction.cancel();

    // Done
  }

  /**
   * Show state of Transaction
   */
  public void showTransaction() {

    // Get (new) transaction state
    JPanel p = transaction.getPanel();

    // New or known?
    int i = vPanels.indexOf(p);

    // .. setup and remember if unknown
    if (i<0) {
      i=vPanels.size();
      p.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
      pState.add(p,""+i);
      vPanels.addElement(p);
    }

    // Update Actions
    int actions = transaction.getActions();
    bPrev  .setEnabled( is(actions,Transaction.PREV  ) );
    bNext  .setEnabled( is(actions,Transaction.NEXT  ) );
    bCancel.setEnabled( is(actions,Transaction.CANCEL) );
    bOK    .setEnabled( is(actions,Transaction.OK    ) || is(actions,Transaction.DONE) );

    if (is(actions,Transaction.DONE)) {
      bOK.setText(App.resources.getString("transaction_panel.setText.done"));
      bOK.setActionCommand("DONE");
    }
    if (is(actions,Transaction.OK)) {
      bOK.setText(App.resources.getString("transaction_panel.setText.ok"));
      bOK.setActionCommand("OK");
    }

    while (true) {
      if (bNext.isEnabled()) {
        bNext.requestFocus();
        break;
      }
      if (bOK.isEnabled()) {
        bOK.requestFocus();
        break;
      }
      bCancel.requestFocus();
      break;
    }

    // Show change
    pStateLayout.show(pState,""+i);

    invalidate();
    validate();
    repaint();

    // Done
  }
}
