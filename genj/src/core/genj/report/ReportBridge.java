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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.PropertyComparator;
import genj.util.Debug;
import genj.util.Registry;
import genj.util.swing.ChoiceWidget;
import genj.window.WindowManager;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.JFileChooser;

/**
 * A Bridge between a running report and the environment it's running in
 */
public class ReportBridge {

  /** members */
  private StringBuffer buffer = new StringBuffer(16*1024);
  private final static long FLUSH_WAIT = 100;
  private long         last = -1;

  private Registry     registry;
  private ReportView   view;
  private Report       report;

  /**
   * Constructor
   */
  public ReportBridge(ReportView theView, Registry theRegistry, Report theReport) {
    view     = theView;
    registry = theRegistry;
    report   = theReport;
  }

  /**
   * Logging a message
   */
  /*package*/ void log(String s) {

    // Remember it
    buffer.append(s+"\n");

    // Time for a display?
    if (new Date().getTime()-last > FLUSH_WAIT) {
      flush();
    }

    // 20021030 threading should work better nowadays - removing yield()
    //Thread.yield();
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
   * Helper that shows a simple text message to user
   */
  public void showMessageToUser(String msg) {
    view.getViewManager().getWindowManager().openDialog(null, report.getName(), WindowManager.IMG_INFORMATION, msg, (String[])null, view);
  }

  /**
   * Helper method that queries the user for an entity
   * @param gedcom to use
   * @param type the type of entities to show 
   * @param sortPath path to sort by or null
   */
  public Entity getEntityFromUser(String msg, Gedcom gedcom, int type, String sortPath) {
    // grab entities
    List ents = gedcom.getEntities(type);
    if (ents.isEmpty())
      return null;
    // sort
    if (sortPath!=null)
      Collections.sort(ents, new PropertyComparator(sortPath));
    // show
    return (Entity)getValueFromUser(msg, ents.toArray(), ents.get(0));
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

    ChoiceWidget choice = new ChoiceWidget(choices, selected);
    choice.setEditable(false);
    
    int rc = view.getViewManager().getWindowManager().openDialog(
      null, 
      report.getName(), 
      WindowManager.IMG_QUESTION,
      choice,
      WindowManager.OPTIONS_OK_CANCEL,
      view
    );
    
    return rc==0 ? choice.getSelectedItem() : null;
  }

  /**
   * Helper method that queries the user for a value giving him a
   * choice of remembered values
   */
  public String getValueFromUser(String key, String msg, String[] choices) {

    // Choice to include already entered stuff?
    if ((key!=null)&&(registry!=null)) {

      // Do we know values for this already?
      String[] presets = registry.get(key, (String[])null);
      if (presets != null) {
        choices = presets;
      }
    }

    // show 'em
    ChoiceWidget choice = new ChoiceWidget(choices, "");
    choice.setEditable(true);
    
    int rc = view.getViewManager().getWindowManager().openDialog(
      null, 
      "Report Input", 
      WindowManager.IMG_QUESTION,
      choice,
      WindowManager.OPTIONS_OK_CANCEL,
      view
    );
    
    String result = rc==0 ? choice.getText() : null;

    // Remember?
    if (key!=null&&result!=null&&result.length()>0) {

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
    int rc = view.getViewManager().getWindowManager().openDialog(
      null, 
      report.getName(),
      WindowManager.IMG_QUESTION, 
      msg,
      yesnoORokcancel ? WindowManager.OPTIONS_YES_NO : WindowManager.OPTIONS_OK_CANCEL,
      view
    );
    // Done
    return rc==0;
  }

  /**
   * log an exception
   */
  public void println(Exception e) {
    CharArrayWriter awriter = new CharArrayWriter(256);
    e.printStackTrace(new PrintWriter(awriter));
    log(awriter.toString());
  }

} //ReportBridge
