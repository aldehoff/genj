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
package genj.report;

import java.io.*;

import javax.swing.event.*;
import genj.gedcom.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;
import genj.util.*;
import java.util.*;
import javax.swing.tree.*;
import javax.swing.*;
import java.net.*;

/**
 * A Bridge between a running report and the environment it's running in
 */
public class ReportBridge {

  private StringBuffer buffer = new StringBuffer(16*1024);
  private final static long FLUSH_WAIT = 100;
  private long         last = -1;

  public static final int ERROR_MESSAGE       = JOptionPane.ERROR_MESSAGE      ;
  public static final int INFORMATION_MESSAGE = JOptionPane.INFORMATION_MESSAGE;

  private Registry     registry;
  private ReportView   view;

  /**
   * Logging a message
   */
  void log(String s) {

    // Remember it
    buffer.append(s+"\n");

    // Time for a display?
    if (new Date().getTime()-last > FLUSH_WAIT) {
      flush();
    }

    // FIXME : this is only for green threads
    Thread.yield();
  }

  /**
   * Append a new line to the log
   */
  public void println() throws ReportCancelledException {
    println("");
  }

  /**
   * Write an arbitrary Object to the log
   */
  public void println(Object o) throws ReportCancelledException {
    if (o==null) {
      return;
    }
    println(o.toString());
  }
  
  /**
   * Write a String to the log
   */
  public void println(String s) throws ReportCancelledException {

    // Our hook into checking for Interrupt
    if (Thread.interrupted())
      throw new ReportCancelledException();

    // Append it
    log(s);

    // Done
  }

  /**
   * Flushes any uncommitted data from this writer
   */
  public void flush() {

    last = new Date().getTime();
    if (view!=null) {
      view.addOutput(buffer.toString());
    } else {
      Debug.log(Debug.INFO, this, buffer.toString());
    }
    buffer.setLength(0);

  }

  /**
   * Constructor
   */
  public ReportBridge(ReportView theView, Registry theRegistry) {
    view     = theView;
    registry = theRegistry;
  }

  /**
   * Helper method that queries the user for a directory
   */
  public File getDirectoryFromUser(String title, String button) {

    JFileChooser chooser = new JFileChooser(".");
    chooser.setFileSelectionMode(chooser.DIRECTORIES_ONLY);
    chooser.setDialogTitle(title);
    if (chooser.APPROVE_OPTION != chooser.showDialog(view,button)) {
      return null;
    }
    return chooser.getSelectedFile();
  
  }

  /**
   * Access to context's registry
   */
  public Registry getRegistry() {
    return registry;
  }

  /**
   * Helper method that queries the user for a value by
   * showing a JOptionDialog.
   */
  public String getValueFromUser(String msg) {
    return (String)getValueFromUser(msg,new String[0],null);
  }

  /**
   * Helper method that queries the user for a value by
   * showing a JOptionDialog.
   */
  public Object getValueFromUser(String msg, Object[] choices, Object selected) {

    Object result = JOptionPane.showInputDialog(
      view,
      msg,
      "Report Input",
      JOptionPane.QUESTION_MESSAGE,
      null,
      choices,
      selected);

    return result;
  }

  /**
   * Helper method that queries the user for a value giving him a
   * choice of remembered values
   */
  public String getValueFromUser(String msg, String[] choices, String key) {

    // Choice to include already entered stuff?
    if ((key!=null)&&(registry!=null)) {

      // Do we know values for this already?
      String[] presets = registry.get(key, (String[])null);
      if (presets != null) {
        choices = presets;
      }
    }

    // Create our visual element
    JComboBox combo = new JComboBox(choices);
    combo.setEditable(true);

    // And let the user choose
    int rc = JOptionPane.showConfirmDialog(
      view,
      new Object[]{ msg, combo },
      "Report Input",
      JOptionPane.OK_CANCEL_OPTION
    );

    // Abort?
    if (rc!=JOptionPane.OK_OPTION) {
      return null;
    }

    String result = combo.getEditor().getItem().toString();

    // Remember?
    if ((key!=null)&&(registry!=null)) {

      Vector v = new Vector(choices.length+1);
      v.addElement(result);
      for (int i=0;i<choices.length;i++) {
        if (!choices[i].equals(result))
          v.addElement(choices[i]);
      }
      registry.put(key,v);
    }

    // Done
    return result;
  }

  /**
   * Helper method that queries the user for yes/no input
   */
  public boolean getValueFromUser(String msg, boolean yesnoORokcancel) {

    // And let the user choose
    int rc = JOptionPane.showConfirmDialog(
      view,
      msg,
      "Report Input",
      yesnoORokcancel?JOptionPane.YES_NO_OPTION:JOptionPane.OK_CANCEL_OPTION
    );

    // Done
    return rc==JOptionPane.YES_OPTION;
  }

  /**
   * log an exception
   */
  public void println(Exception e) {
    CharArrayWriter awriter = new CharArrayWriter(256);
    e.printStackTrace(new PrintWriter(awriter));
    log(awriter.toString());
  }

  /**
   * Shows a message to the user with option OK
   */
  public void showMessageToUser(int type, String msg) {

    JOptionPane.showMessageDialog(
      view,
      msg,
      "Report Output",
      type
    );

  }

}
