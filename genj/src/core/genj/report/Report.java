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
import genj.util.EnvironmentChecker;
import genj.util.Registry;
import genj.util.swing.ChoiceWidget;
import genj.window.WindowManager;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;


/**
 * Interface of a user definable GenjJ Report
 */
public abstract class Report implements Cloneable {

  /** language we're trying to use */
  private final static String lang = EnvironmentChecker.getProperty(Report.class, "user.language", null, "i18n for reports");

  /** i18n texts */
  private Properties i18n; 
  
  /** log buffer */
  private StringBuffer logBuffer = new StringBuffer(16*1024);
  
  /** last flush time */
  private long         lastFlush = -1;
  
  /** time between flushs */
  private final static long FLUSH_WAIT = 100;
  
  /** the view */
  private ReportView view;
  
  /** a registry for us */
  private Registry registry;

  /**
   * integration - private instance for a run 
   */
  /*package*/ Report getInstance(ReportView viEw, Registry reGistry) {
    
    try {
      
      // clone this
      Report result = (Report)clone();
      
      // remember context for result
      result.view = viEw;
      result.registry = reGistry;
      
      // done
      return result;
      
    } catch (CloneNotSupportedException e) {
      Debug.log(Debug.ERROR, this, e);
      throw new RuntimeException("getInstance() failed");
    }
  }
  
  /**
   * integration - log a message
   */
  /*package*/ void log(String txt) {

    // Remember it
    logBuffer.append(txt+"\n");

    // Time for a display?
    if (System.currentTimeMillis()-lastFlush > FLUSH_WAIT) 
      flush();

    // done
  }

  /**
   * integration - flush messages
   */
  /*package*/ void flush() {
    
    lastFlush = System.currentTimeMillis();
    if (view!=null) {
      view.addOutput(logBuffer.toString());
    } else {
      Debug.log(Debug.INFO, this, logBuffer.toString());
    }
    logBuffer.setLength(0);
    
  }

  /**
   * Access to context's registry
   */
  protected Registry getRegistry() {
    return registry;
  }

  /**
   * Append a new line to the log
   */
  protected final void println() throws ReportCancelledException {
    println("");
  }

  /**
   * Write an arbitrary Object to the log
   */
  protected final void println(Object o) throws ReportCancelledException {
    // nothing to do?
    if (o==null)
      return;
    // Our hook into checking for Interrupt
    if (Thread.interrupted())
      throw new ReportCancelledException();
    // Append it
    log(o.toString());
    // Done
  }

  /**
   * log an exception
   */
  protected final void println(Exception e) {
    CharArrayWriter awriter = new CharArrayWriter(256);
    e.printStackTrace(new PrintWriter(awriter));
    log(awriter.toString());
  }
  
  /**
   * Helper method that queries the user for a directory
   */
  protected final File getDirectoryFromUser(String title, String button) {
  
    JFileChooser chooser = new JFileChooser(".");
    chooser.setFileSelectionMode(chooser.DIRECTORIES_ONLY);
    chooser.setDialogTitle(title);
    if (chooser.APPROVE_OPTION != chooser.showDialog(view,button)) {
      return null;
    }
    return chooser.getSelectedFile();
    
  }
  
  /**
   * Helper that shows a simple text message to user
   */
  protected final void showMessageToUser(String msg) {
    view.getViewManager().getWindowManager().openDialog(
      null, 
      getName(), 
      WindowManager.IMG_INFORMATION, 
      msg, 
      (String[])null, 
      view
    );
  }
  
  /**
   * Helper method that queries the user for an entity
   * @param gedcom to use
   * @param type the type of entities to show 
   * @param sortPath path to sort by or null
   */
  protected final Entity getEntityFromUser(String msg, Gedcom gedcom, int type, String sortPath) {
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
   * Helper method that queries the user for a choice of non-editable items
   */
  protected final Object getValueFromUser(String msg, Object[] choices, Object selected) {
  
    ChoiceWidget choice = new ChoiceWidget(choices, selected);
    choice.setEditable(false);
      
    int rc = view.getViewManager().getWindowManager().openDialog(
      null, 
      getName(), 
      WindowManager.IMG_QUESTION,
      new JComponent[]{new JLabel(msg),choice},
      WindowManager.OPTIONS_OK_CANCEL,
      view
    );
      
    return rc==0 ? choice.getSelectedItem() : null;
  }
  
  /**
   * Helper method that queries the user for a text-value giving him a
   * choice of remembered values
   */
  protected final String getValueFromUser(String key, String msg, String[] choices) {
  
    // Choice to include already entered stuff?
    if ((key!=null)&&(registry!=null)) {
  
      // Do we know values for this already?
      String[] presets = registry.get(key, (String[])null);
      if (presets != null) {
        choices = presets;
      }
    }
    
    // prepare txt 
  
    // show 'em
    ChoiceWidget choice = new ChoiceWidget(choices, "");
      
    int rc = view.getViewManager().getWindowManager().openDialog(
      null, 
      "Report Input", 
      WindowManager.IMG_QUESTION,
      new JComponent[]{new JLabel(msg),choice},
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
  protected final boolean getValueFromUser(String msg, boolean yesnoORokcancel) {
    int rc = view.getViewManager().getWindowManager().openDialog(
      null, 
      getName(),
      WindowManager.IMG_QUESTION, 
      msg,
      yesnoORokcancel ? WindowManager.OPTIONS_YES_NO : WindowManager.OPTIONS_OK_CANCEL,
      view
    );
    // Done
    return rc==0;
  }

  /**
   * i18n of a string
   */
  protected final String i18n(String key) {
    return i18n(key, (String[])null);
  }
  
  /**
   * i18n of a string
   */
  protected final String i18n(String key, String sub) {
    return i18n(key, new String[]{sub});
  }
  
  /**
   * i18n of a string
   */
  protected final String i18n(String key, String[] subs) {
    
    // get i18n properties
    if (i18n==null) {
      i18n = new Properties();
      try {
        i18n.load(getClass().getResourceAsStream(getClass().getName()+".properties"));
      } catch (Throwable t) {
        Debug.log(Debug.INFO, this, "Couldn't read i18n for "+this);
      }
    }
    
    // look it up in language
    String result = null;
    if (lang!=null) {
      result = i18n.getProperty(key+'.'+lang);
    }
    
    // fallback if necessary
    if (result==null)
      result = i18n.getProperty(key);
    
    // check result and apply format
    if (result==null) {
      Debug.log(Debug.WARNING, this, "Unknown i18 '"+key+"' for "+getName());
      result = key;
    } else {
      if (subs!=null&&subs.length>0)
        result = new MessageFormat(result).format(subs);
    }
    
    // done
    return result;
  }

  /**
   * Returns the author of this script
   */
  public abstract String getAuthor();

  /**
   * Returns the version of this script
   */
  public abstract String getVersion();

  /**
   * Returns information about this report. A report
   * has to override this method to return a <code>String</code>
   * containing information about the author, version
   * and copyright of the report.
   * @return a string containing information about the author, version
   * and copyright of the report
   */
  public abstract String getInfo();

  /**
   * Returns the name of this report - should be localized.
   */
  public abstract String getName();

  /**
   * Called by GenJ to start this report's execution - has to be
   * overriden by a user defined report.
   * @param context normally the gedcom-file but depending on acceptsProperty,
   * acceptsEntity, acceptsGedcom also either an Entity or Property
   * @exception InterruptedException in case running Thread is interrupted
   */
  public abstract void start(Object context);

  /**
   * Tells wether this report doesn't change information in the Gedcom-file
   */
  public boolean isReadOnly() {
    return true;
  }

  /**
   * Returns true if this report uses STDOUT
   */
  public boolean usesStandardOut() {
    return true;
  }
  
  /**
   * Whether the report allows to be run on a given context - default
   * only accepts Gedcom 
   * @param context will an instance of either Gedcom, Entity or Property
   * @return null for no - string specific to context for yes 
   */
  public String accepts(Object context) {
    return context instanceof Gedcom ? getName() :  null;
  }

} //Report
