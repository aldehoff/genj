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
 * 
 * $Revision: 1.84 $ $Author: nmeier $ $Date: 2005-11-04 20:45:17 $
 */
package genj.report;

import genj.chart.Chart;
import genj.common.SelectEntityWidget;
import genj.fo.Document;
import genj.fo.Formatter;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.io.FileAssociation;
import genj.option.Option;
import genj.option.PropertyOption;
import genj.util.EnvironmentChecker;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.FileChooser;
import genj.view.ViewManager;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


/**
 * Base-class of all GenJ reports. Sub-classes that are compiled
 * and available in ./report will be loaded by GenJ automatically
 * and can be reloaded during runtime.
 */
public abstract class Report implements Cloneable {

  protected final static Logger LOG = Logger.getLogger("genj.report"); 
  
  protected final static ImageIcon     
    IMG_SHELL = new genj.util.swing.ImageIcon(ReportView.class,"ReportShell.gif"), 
    IMG_FO  = new genj.util.swing.ImageIcon(ReportView.class,"ReportFO.gif"  ),
    IMG_GUI   = new genj.util.swing.ImageIcon(ReportView.class,"ReportGui.gif"  );

  /** global report options */
  protected Options OPTIONS = Options.getInstance();

  /** options */
  protected final static int
    OPTION_YESNO    = 0,
    OPTION_OKCANCEL = 1,
    OPTION_OK       = 2;

  private final static String[][] OPTION_TEXTS = {
    new String[]{WindowManager.TXT_YES, WindowManager.TXT_NO     }, 
    new String[]{WindowManager.TXT_OK , WindowManager.TXT_CANCEL }, 
    new String[]{WindowManager.TXT_OK                          }
  };
    
  /** alignment options */
  protected final static int
    ALIGN_LEFT   = 0,
    ALIGN_CENTER = 1,
    ALIGN_RIGHT  = 2;

  /** one report for all reports */
  private final static Registry registry = new Registry("genj-reports");

  /** language we're trying to use */
  private final static String lang = Locale.getDefault().getLanguage();

  /** i18n texts */
  private Properties properties;

  /** out */
  private PrintWriter out;

  /** a view manager */
  private ViewManager viewManager;

  /** owning component */
  private JComponent owner;
  
  /** options */
  private List options;
  
  /** image */
  private ImageIcon image;
  
  /** our accepted types */
  private Map inputType2startMethod = new HashMap();

  /**
   * Constructor
   */
  protected Report() {
    
    // prepare default image
    image = usesStandardOut() ? IMG_SHELL : IMG_GUI;
    
    // try to load a custom one too
    try {
      String file = getTypeName()+".gif";
      InputStream in = getClass().getResourceAsStream(file);
      if (in!=null)
        image = new genj.util.swing.ImageIcon(file, in);
    } catch (Throwable t) {
    }
    
    // check for what this report accepts
    Method[] methods = getClass().getDeclaredMethods();
    for (int m = 0; m < methods.length; m++) {
      // needs to be named start
      if (!methods[m].getName().equals("start")) continue;
      // make sure its a one-arg
      Class[] params = methods[m].getParameterTypes();
      if (params.length!=1) continue;
      // keep it
      inputType2startMethod.put(params[0], methods[m]);
      // next
    }
    
    // done
  }
  
  /**
   * integration - private instance for a run
   */
  /*package*/ Report getInstance(ViewManager viEwManager, JComponent owNer, PrintWriter ouT) {

    try {

      // make sure options are initialized
      getOptions();

      // clone this
      Report result = (Report)clone();

      // remember context for result
      result.viewManager = viEwManager;
      result.out = ouT;
      result.owner = owNer;
      
      // done
      return result;

    } catch (CloneNotSupportedException e) {
      ReportView.LOG.log(Level.SEVERE, "couldn't clone report", e);
      throw new RuntimeException("getInstance() failed");
    }
  }

  /**
   * integration - log a message
   */
  /*package*/ void log(String txt) {
    if (out!=null)
      out.println(txt);
  }
  
  /**
   * Store report's options
   */
  /*package*/ void saveOptions() {
    // if known
    if (options==null)
      return;
    // save 'em
    Iterator it = options.iterator();
    while (it.hasNext())
      ((Option)it.next()).persist(registry);
    // done
  }
  
  /**
   * Get report's options
   */
  /*package*/ List getOptions() {
    
    // already calculated
    if (options!=null)
      return options;
      
    // calculate options 
    options = PropertyOption.introspect(this);

    // restore options values
    Iterator it = options.iterator();
    while (it.hasNext()) {
      PropertyOption option = (PropertyOption)it.next();
      // restore old value
      option.restore(registry);
      // we use i18n() to resolve names for options
      option.setName(i18n(option.getProperty()));    
      // set category
      option.setCategory(getName());
    }
    
    // done
    return options;
  }
  
  /**
   * An image
   */
  protected ImageIcon getImage() {
    return image; 
  }

  /**
   * When a report is executed all its text output is gathered and
   * shown to the user (if run through ReportView). A sub-class can
   * flush the current log with this method.
   */
  public final void flush() {
    if (out!=null)
      out.flush();
  }

  /**
   * When a report is executed all its text output is gathered and
   * shown to the user (if run through ReportView). A sub-class can
   * append a new line to the current log with this method.
   */
  public final void println() {
    println("");
  }

  /**
   * When a report is executed all its text output is gathered and
   * shown to the user (if run through ReportView). A sub-class can
   * append the text-representation of an object (toString) to the 
   * current log with this method.
   */
  public final void println(Object o) {
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
   * When a report is executed all its text output is gathered and
   * shown to the user (if run through ReportView). A sub-class can
   * let the user know about an exception with this method. The
   * information about the exception is appended in text-form to 
   * the current log.
   */
  public final void println(Throwable t) {
    CharArrayWriter awriter = new CharArrayWriter(256);
    t.printStackTrace(new PrintWriter(awriter));
    log(awriter.toString());
  }

  /**
   * A sub-class can ask the user for a directory with this method.
   */
  public final File getDirectoryFromUser(String title, String button) {

    String key = getClass().getName()+".dir";

    // show directory chooser
    String dir = registry.get(key, EnvironmentChecker.getProperty(this, "user.home", ".", "looking for report dir to let the user choose from"));
    JFileChooser chooser = new JFileChooser(dir);
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setDialogTitle(title);
    int rc = chooser.showDialog(owner,button);
    
    // check result
    File result = chooser.getSelectedFile(); 
    if (rc!=JFileChooser.APPROVE_OPTION||result==null)
      return null;
    
    // keep it
    registry.put(key, result.toString());    
    return result;
  }

  /**
   * A sub-class can show a document to the user with this method allowing
   * to save, transform and view it
   */
  public final void showDocumentToUser(Document doc) {
    
    String title = "Document '"+doc.getTitle();
    JLabel label = new JLabel("Choose formatted output for document");
    
    ChoiceWidget formatters = new ChoiceWidget(Formatter.getFormatters(), Formatter.getFormatter(registry.get("formatter", (String)null)));
    formatters.setEditable(false);
    int rc = viewManager.getWindowManager().openDialog(
        "reportdoc", title, WindowManager.QUESTION_MESSAGE, new JComponent[] {label, formatters}, WindowManager.ACTIONS_OK_CANCEL, owner);
    
    // cancel?
    if (rc!=0)
      return;
    
    // remember formatter
    Formatter formatter = (Formatter)formatters.getSelectedItem();
    registry.put("formatter", formatter.getKey());
    
    // ask user for output file
    String key = getClass().getName()+".dir";
    String dir = registry.get(key, EnvironmentChecker.getProperty(this, "user.home", ".", "document output directory"));
    FileChooser chooser = new FileChooser(owner, "Choose file", "Save", formatter.getFileExtension(), dir);
    chooser.showDialog();
    File file = chooser.getSelectedFile();
    if (file==null)
      return;
    registry.put(key, chooser.getCurrentDirectory().getAbsolutePath());
    if (!file.getName().endsWith("."+formatter.getFileExtension()))
      file = new File(file.getAbsolutePath()+"."+formatter.getFileExtension());
    
    // show a progress dialog
    String progress = viewManager.getWindowManager().openNonModalDialog(
        null, title, WindowManager.INFORMATION_MESSAGE, new JLabel("Writing Document to File "+file+" ..."), WindowManager.ACTIONS_OK, owner);
    
    // format and write
    try {
      file.getParentFile().mkdirs();
      formatter.format(doc, file);
    } catch (IOException e) {
      LOG.log(Level.WARNING, "formatting "+doc+" failed", e);
    }
    
    // close progress dialog
    viewManager.getWindowManager().close(progress);
    
    // open document
    FileAssociation association = FileAssociation.get(formatter.getFileExtension(), formatter.getFileExtension(), "Open", owner);
    if (association!=null)
      association.execute(file.getAbsolutePath());
      
    // done
  }
  
  /**
   * A sub-class can show a chart to the user with this method
   */
  public final void showChartToUser(Chart chart) {
    showComponentToUser(chart);
  }
  
  /**
   * A sub-class can show a Java Swing component to the user with this method
   */
  public final void showComponentToUser(JComponent component) {
    
    // open a non-modal dialog
    viewManager.getWindowManager().openNonModalDialog(getClass().getName()+"#component",getName(), WindowManager.INFORMATION_MESSAGE,component,WindowManager.ACTIONS_OK,owner);
    
    // done
  }

  /**
   * A sub-class can show items containing text and references to Gedcom
   * objects to the user with this method
   */
  public final void showPropertiesToUser(String msg, PropertyList bookmarks) {

    // prepare content
    JPanel content = new JPanel(new BorderLayout());
    content.add(BorderLayout.NORTH, new JLabel(msg));
    content.add(BorderLayout.CENTER, new JScrollPane(bookmarks.new UI(viewManager)));

    // open a non-modal dialog
    viewManager.getWindowManager().openNonModalDialog(getClass().getName()+"#items",getName(),WindowManager.INFORMATION_MESSAGE,content,WindowManager.ACTIONS_OK,owner);

    // done
  }
  
  /**
   * A sub-class can open a browser that will show the given URL with this method
   */
  public final void showBrowserToUser(URL url) {

    // run browser
    FileAssociation association = FileAssociation.get("html", "html, htm, xml", "Browse", owner);
    if (association!=null)  
      association.execute(url.toString());

    // done
  }

  /**
   * A sub-class can ask the user for an entity (e.g. Individual) with this method
   * @param msg a message for letting the user know what and why he's choosing
   * @param gedcom to use
   * @param tag tag of entities to show for selection (e.g. Gedcom.INDI)
   */
  public final Entity getEntityFromUser(String msg, Gedcom gedcom, String tag) {
    
    SelectEntityWidget select = new SelectEntityWidget(gedcom, tag, null);
    
    // preselect something?
    select.setSelection(gedcom.getEntity(registry.get("select."+tag, (String)null)));

    // show it
    int rc = viewManager.getWindowManager().openDialog(null,getName(),WindowManager.QUESTION_MESSAGE,new JComponent[]{new JLabel(msg),select},WindowManager.ACTIONS_OK_CANCEL,owner);
    if (rc!=0)
      return null;

    // remember selection
    Entity result = select.getSelection();
    if (result==null)
      return null;
    registry.put("select."+result.getTag(), result.getId());
    
    // done
    return result;
  }

  /**
   * A sub-class can query the user for a selection of given choices with this method
   */
  public final Object getValueFromUser(String msg, Object[] choices, Object selected) {

    ChoiceWidget choice = new ChoiceWidget(choices, selected);
    choice.setEditable(false);

    int rc = viewManager.getWindowManager().openDialog(null,getName(),WindowManager.QUESTION_MESSAGE,new JComponent[]{new JLabel(msg),choice},WindowManager.ACTIONS_OK_CANCEL,owner);

    return rc==0 ? choice.getSelectedItem() : null;
  }

  /**
   * A sub-class can query the user for a text value with this method. The value
   * that was selected the last time is automatically suggested.
   */
  public final String getValueFromUser(String key, String msg, String[] choices) {

    // Choice to include already entered stuff?
    if (key!=null) {

      key = getClass().getName()+"."+key;

      // Do we know values for this already?
      String[] presets = registry.get(key, (String[])null);
      if (presets != null) {
        choices = presets;
      }
    }

    // prepare txt

    // show 'em
    ChoiceWidget choice = new ChoiceWidget(choices, "");

    int rc = viewManager.getWindowManager().openDialog(null,getName(),WindowManager.QUESTION_MESSAGE,new JComponent[]{new JLabel(msg),choice},WindowManager.ACTIONS_OK_CANCEL,owner);

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
   * A sub-class can query the user for a simple yes/no selection with
   * this method.
   * @param msg the message explaining to the user what he's choosing
   * @param option one of OPTION_YESNO, OPTION_OKCANCEL, OPTION_OK
   */
  public final boolean getOptionFromUser(String msg, int option) {
    return 0==getOptionFromUser(msg, OPTION_TEXTS[option]);
  }
  
  /**
   * Helper method that queries the user for yes/no input
   */
  private int getOptionFromUser(String msg, String[] actions) {
    
    return viewManager.getWindowManager().openDialog(
      null,
      getName(),
      WindowManager.QUESTION_MESSAGE,
      msg,
      actions,
      owner
    );
    
    // Done
  }

  /**
   * Sub-classes that are accompanied by a [ReportName].properties file
   * containing simple key=value pairs can lookup internationalized
   * text-values with this method.
   * @param key the key to lookup in [ReportName].properties
   */
  public final String i18n(String key) {
    return i18n(key, (Object[])null);
  }

  /**
   * Sub-classes that are accompanied by a [ReportName].properties file
   * containing simple key=value pairs can lookup internationalized
   * text-values with this method.
   * @param key the key to lookup in [ReportName].properties
   * @param value an integer value to replace %1 in value with
   */
  public final String i18n(String key, int value) {
    return i18n(key, new Integer(value));
  }

  /**
   * Sub-classes that are accompanied by a [ReportName].properties file
   * containing simple key=value pairs can lookup internationalized
   * text-values with this method.
   * @param key the key to lookup in [ReportName].properties
   * @param value an object value to replace %1 in value with
   */
  public final String i18n(String key, Object value) {
    return i18n(key, new Object[]{value});
  }

  /**
   * Sub-classes that are accompanied by a [ReportName].properties file
   * containing simple key=value pairs can lookup internationalized
   * text-values with this method.
   * @param key the key to lookup in [ReportName].properties
   * @param values an array of values to replace %1, %2, ... in value with
   */
  public final String i18n(String key, Object[] values) {

    // get i18n properties
    Properties i18n = getProperties();

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
      result = "";
    } else {
      if (values!=null&&values.length>0) {
        for (int i=0;i<values.length;i++) 
          if (values[i]==null) values[i]="";
        result = Resources.getMessageFormat(result).format(values);
      }
    }

    // done
    return result;
  }
  
  /**
   * Filename
   */
  public String getFilename() {
    return getClass().getName().replace('.','/')+".java";
  }
  
  /**
   * Type name (name without packages)
   */
  private String getTypeName() {
    String rtype = getClass().getName();
    while (rtype.indexOf('.') >= 0)
      rtype = rtype.substring(rtype.indexOf('.')+1);
    return rtype;
  }
  
  /**
   * Access to report properties
   */
  private Properties getProperties() {
    if (properties==null) {
      properties = new Properties();
      try {
        properties.load(getClass().getResourceAsStream(getTypeName()+".properties"));
      } catch (Throwable t) {
        ReportView.LOG.info("Couldn't read properties for "+this);
      }
    }
    return properties;
  }

  /**
   * Creates an indent for text outputs. The method supports several levels
   * and front strings.
   *
   * @param level for indent (can be thought of columns)
   * @param spacesPerLevel space character between one level
   * @param prefix String in front of the indented text (can be null)
   */
    public final String getIndent(int level, int spacesPerLevel, String prefix) {
        String oneLevel = "";
        while(oneLevel.length() != spacesPerLevel)
            oneLevel=oneLevel+" ";
        StringBuffer buffer = new StringBuffer(256);
        while (--level>0) {
            buffer.append(oneLevel);
        }
        if (prefix!=null)
          buffer.append(prefix);
        return buffer.toString();
    }  
    
    /**
     * Creates an empty String for text output. Spaces per Level are taken from
     * OPTIONS.getIndentPerLevel()
     */
    public final String getIndent(int level) {
      return getIndent(level, OPTIONS.getIndentPerLevel(), null);
    }
  
  
  /**
   * Aligns a simple text for text outputs.
   * @param txt the text to align
   * @param length the length of the result
   * @param alignment one of LEFT,CENTER,RIGHT
   */
  public final String align(String txt, int length, int alignment) {

    // check txt length
    int n = txt.length();
    if (n>length)
      return txt.substring(0, length);
    n = length-n;

    // prepare result
    StringBuffer buffer = new StringBuffer(length);

    int before,after;
    switch (alignment) {
      default:
      case ALIGN_LEFT:
        before = 0;
        break;
      case ALIGN_CENTER:
        before = (int)(n*0.5F);
        break;
      case ALIGN_RIGHT:
        before = n;
        break;
    }
    after = n-before;

    // space before
    for (int i=0; i<before; i++)
      buffer.append(' ');

    // txt
    buffer.append(txt);

    // space after
    for (int i=0; i<after; i++)
      buffer.append(' ');

    // done
    return buffer.toString();
  }
  
  /**
   * Returns the name of a report - this by default is the value of key "name"
   * in the file [ReportName].properties. A report has to override this method 
   * to provide a localized name if that file doesn't exist.
   * @return name of the report
   */
  public String getName() {
    String name =  i18n("name");
    if (name.length()==0) name = getTypeName();
    return name;
  }

  /**
   * Returns the author of a report - this by default is the value of key "author"
   * in the file [ReportName].properties. A report has to override this method 
   * to provide the author if that file doesn't exist.
   * @return name of the author
   */
  public String getAuthor() {
    return i18n("author");
  }

  /**
   * Returns the version of a report - this by default is the value of key "version"
   * in the file [ReportName].properties. A report has to override this method 
   * to provide the version if that file doesn't exist.
   * @return version of report
   */
  public String getVersion() {
     return i18n("version");
  }

  /**
   * Returns information about a report - this by default is the value of key "info"
   * in the file [ReportName].properties. A report has to override this method 
   * to provide localized information if that file doesn't exist.
   * @return information about report
   */
  public String getInfo() {
    return i18n("info");
  }

  /**
   * Called by GenJ to start this report's execution - can be overriden by a user defined report.
   * @param context normally an instance of type Gedcom but depending on 
   *    accepts() could also be of type Entity or Property
   */
  public void start(Object context) {
    try {
      Class contextType = context.getClass();
      Method method = null;
      while (contextType!=null) {
         method = (Method)inputType2startMethod.get(contextType);
         if (method!=null) break;
         contextType = contextType.getSuperclass();
      }
      method.invoke(this, new Object[]{ context });
    } catch (Throwable t) {
      String msg = "can't run report on input";
      if (t instanceof InvocationTargetException) {
        t = ((InvocationTargetException)t).getTargetException();
        msg = "report failed";
      }
      if (t instanceof RuntimeException) throw (RuntimeException)t;
      throw new RuntimeException(msg, t);
    }
  }

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
     * checks for methods called 
     * <il>
     *  <li>start(Gedcom|Object)
     *  <li>start(Property)
     *  <li>start(Entity)
     *  <li>start(Indi[])
     *  <li>...
     * </il>
     * @return title of action for given context or null for n/a
     */
    public String accepts(Object context) {
      Class contextType = context.getClass();
      while (contextType!=null) {
        if (inputType2startMethod.containsKey(contextType)) return getName();
        contextType = contextType.getSuperclass();
      }
      // not applicable
      return null;
    }
  
  /**
   * Resolve acceptable types
   */
  /*package*/ final Set getInputTypes() {
    return inputType2startMethod.keySet();
  }
  
} //Report
