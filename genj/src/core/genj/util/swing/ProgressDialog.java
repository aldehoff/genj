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
package genj.util.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import genj.util.*;

/**
 * A Dialog used to show the progress of an operation
 */
public class ProgressDialog extends JDialog implements ActionListener {

  private JProgressBar  bProgress;

  private Trackable     trackable;
  private Thread        worker;
  private JLabel        lState;
  private String        title;
  private int           dlgNumber;
  private Frame         frame;
  private String        item;

  private static Hashtable hashDialogs = new Hashtable();

  /**
   * Constructor
   */
  public ProgressDialog(Frame setFrame, String setTitle, String setItem, Trackable setTrackable, Thread setWorker) {

    // Do the dialog
    super(setFrame,setTitle,false);

    // Data
    trackable  =setTrackable;
    worker     =setWorker;
    title      =setTitle;
    frame      =setFrame;
    item       =setItem;

    int l = setItem.length();
    if (l>32) {
      setItem=setItem.substring(0,13)+"..."+setItem.substring(l-(32-16));
    }

    // Panel with information
    Container p = getContentPane();
    GridBagHelper gbh = new GridBagHelper(p);

    // ... item being worked on
    JLabel label = new JLabel(setItem,JLabel.CENTER);
    gbh.add(label    , 1, 1, 1, 1, gbh.GROW_HORIZONTAL | gbh.FILL_HORIZONTAL);

    // ... explanation
    lState = new JLabel(" ",JLabel.CENTER);
    gbh.add(lState   , 1, 2, 1, 1, gbh.GROW_HORIZONTAL | gbh.FILL_HORIZONTAL);

    // ... progess
    bProgress = new JProgressBar();
    gbh.add(bProgress, 1, 3, 1, 1, gbh.GROW_HORIZONTAL | gbh.FILL_HORIZONTAL);

    // .. cancel
    JButton cancel = new JButton("Cancel");
    cancel.addActionListener(this);
    gbh.add(cancel   , 1, 4, 1, 1, gbh.FILL_NONE                            );

    // No more components
    pack();
    try {
      setLocationRelativeTo(frame);
    } catch (Throwable t) { 
      // 20020813 Having a problem with pre 1.4 VMs - even though 
      // this API call seems to be supported I get a NoSuchMethodError
    }

    // Are there any other dialogs?
    dlgNumber = 0;
    if (hashDialogs.size()>0) {
      // .. try to find free "slot"
      for (;;dlgNumber++) {
        if (!hashDialogs.containsKey(""+frame.hashCode()+dlgNumber)) {
          break;
        }
      }
      // .. shift position by slot
      Point pos = getLocation();
      setLocation(pos.x+dlgNumber*16, pos.y+dlgNumber*16);
    }
    // .. remember me using slot x
    hashDialogs.put(""+frame.hashCode()+dlgNumber, this);

    // Show it
    show();

    // Create thread
    Thread threadProgress = new Thread() {
      public void run() {
        String state = "";
        int progress = -1;
        while (true) {
          int i = trackable.getProgress();
          if (i!=progress) {
            progress = i;
            bProgress.setValue(i);
          }
          String s = trackable.getState();
          if (!state.equals(s)) {
            state = s;
            lState   .setText(s);
          }
          if (!worker.isAlive())
            break;
          try {
            sleep(100);
          } catch (InterruptedException ex) {
          }
        };
        finish();
      }
    };

    // start the work in separate thread
    threadProgress.start();
  }

  /**
   * Cancel-button has been pressed
   */
  public void actionPerformed(ActionEvent e) {
    trackable.cancel();
  }

  /**
   * Finish showing progress
   */
  void finish() {

    // Close ProgressDialog
    dispose();

    // Remember this number as unused
    hashDialogs.remove(""+frame.hashCode()+dlgNumber);

    // Warnings ?
    String warnings=trackable.getWarnings();
    if ((warnings==null)||(warnings.length()==0)) {
      return;
    }

    // Get Warnings into component
    JTextArea taWarnings = new JTextArea();
    taWarnings.setText(warnings);
    taWarnings.setEditable(false);
    JScrollPane spWarnings = new JScrollPane(taWarnings,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS) {
      public Dimension getPreferredSize() {
      return new Dimension(224,128);
      }
    };

    // Prepare a dialog
    JDialog dlgWarnings = new JDialog((Frame)null,title);
    dlgWarnings.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    // Prepare layout
    Container c = dlgWarnings.getContentPane();
    c.setLayout(new BorderLayout());
    c.add(new JLabel(item+" - Warnings:"),"North");
    c.add(spWarnings,"Center");

    dlgWarnings.pack();
    dlgWarnings.show();
  }            
}
