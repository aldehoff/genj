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
 * $Revision: 1.51 $ $Author: nmeier $ $Date: 2004-12-06 21:18:25 $
 */
package genj.report;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.option.Option;
import genj.option.PropertyOption;
import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.EnvironmentChecker;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.HeadlessLabel;
import genj.view.Context;
import genj.view.ViewManager;
import genj.view.widgets.SelectEntityWidget;
import genj.window.CloseWindow;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Interface of a user definable GenjJ Report
 */
public abstract class Report implements Cloneable {

  /** global report options */
  protected Options OPTIONS = Options.getInstance();

  /** options */
  protected final static int
    OPTION_YESNO    = 0,
    OPTION_OKCANCEL = 1,
    OPTION_OK       = 2;

  private final static String[][] OPTION_TEXTS = {
    new String[]{CloseWindow.TXT_YES, CloseWindow.TXT_NO     }, 
    new String[]{CloseWindow.TXT_OK , CloseWindow.TXT_CANCEL }, 
    new String[]{CloseWindow.TXT_OK                          }
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

  /**
   * Constructor
   */
  protected Report() {
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
      Debug.log(Debug.ERROR, this, e);
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
    }
    
    // done
    return options;
  }

  /**
   * Flush any of the pending output
   */
  public final void flush() {
    if (out!=null)
      out.flush();
  }

  /**
   * Append a new line to the log
   */
  public final void println() throws ReportCancelledException {
    println("");
  }

  /**
   * Write an arbitrary Object to the log
   */
  public final void println(Object o) throws ReportCancelledException {
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
  public final void println(Throwable t) {
    CharArrayWriter awriter = new CharArrayWriter(256);
    t.printStackTrace(new PrintWriter(awriter));
    log(awriter.toString());
  }

  /**
   * Helper method that queries the user for a directory
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
   * Helper method that shows a component to the user
   */
  public final void showComponentToUser(JComponent component) {
    
    // open a non-modal dialog
    viewManager.getWindowManager().openNonModalDialog(getClass().getName()+"#component",getName(),ReportViewFactory.IMG,component,CloseWindow.OK(),owner);
    
    
  }

  /**
   * Helper method that shows (resulting) items to the user
   */
  public final void showItemsToUser(String msg, Gedcom gedcom, Item[] items) {

    // prepare content
    JPanel content = new JPanel(new BorderLayout());
    content.add(BorderLayout.NORTH, new JLabel(msg));
    content.add(BorderLayout.CENTER, new JScrollPane(new ItemList(gedcom, items, viewManager)));

    // open a non-modal dialog
    viewManager.getWindowManager().openNonModalDialog(getClass().getName()+"#items",getName(),ReportViewFactory.IMG,content,CloseWindow.OK(),owner);

    // done
  }
  
  /**
   * Helper method that shows (resulting) html to the user
   */
  public final void showBrowserToUser(URL url) {

    // get browser command
    Options options = Options.getInstance();
    File browser = options.browser;
    while (!browser.isFile()) {
      
      // show file chooser
      JFileChooser chooser = new JFileChooser();
      chooser.setDialogTitle(Resources.get(ReportView.class).getString("option.browser"));
      int rc = chooser.showOpenDialog(owner);
      browser = chooser.getSelectedFile(); 
      if (rc!=JFileChooser.APPROVE_OPTION||browser==null)
        return;
    
      // keep it
      options.browser = browser;    
    }
    
    // run it
    try {
      Runtime.getRuntime().exec(browser+" "+url);
    } catch (IOException e) {
      println("***Couldn't run "+browser+" on url "+url+"***");
    }

    // done
  }

  /**
   * Helper method that queries the user for an entity
   * @param msg a message for letting the user know what and why he's choosing
   * @param gedcom to use
   * @param tag the tag of the entities to show (for example Gedcom.INDI)
   */
  public final Entity getEntityFromUser(String msg, Gedcom gedcom, String tag) {
    
    SelectEntityWidget select = new SelectEntityWidget(tag, gedcom.getEntities(tag), "");

    int rc = viewManager.getWindowManager().openDialog(null,getName(),WindowManager.IMG_QUESTION,new JComponent[]{new JLabel(msg),select},CloseWindow.OKandCANCEL(),owner);

    return rc==0 ? select.getEntity() : null;
  }

  /**
   * Helper method that queries the user for a choice of non-editable items
   */
  public final Object getValueFromUser(String msg, Object[] choices, Object selected) {

    ChoiceWidget choice = new ChoiceWidget(choices, selected);
    choice.setEditable(false);

    int rc = viewManager.getWindowManager().openDialog(null,getName(),WindowManager.IMG_QUESTION,new JComponent[]{new JLabel(msg),choice},CloseWindow.OKandCANCEL(),owner);

    return rc==0 ? choice.getSelectedItem() : null;
  }

  /**
   * Helper method that queries the user for a text-value giving him a
   * choice of remembered values
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

    int rc = viewManager.getWindowManager().openDialog(null,getName(),WindowManager.IMG_QUESTION,new JComponent[]{new JLabel(msg),choice},CloseWindow.OKandCANCEL(),owner);

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
  public final boolean getOptionFromUser(String msg, int option) {
    return 0==getOptionFromUser(msg, OPTION_TEXTS[option]);
  }
  
  /**
   * Helper method that queries the user for yes/no input
   */
  public final int getOptionFromUser(String msg, String[] options) {
    ActionDelegate[] actions = new ActionDelegate[options.length];
    for (int i = 0; i < actions.length; i++)
      actions[i] = new CloseWindow(options[i]);
    return getOptionFromUser(msg, actions);
  }
    
  /**
   * Helper method that queries the user for yes/no input
   */
  private int getOptionFromUser(String msg, ActionDelegate[] actions) {
    
    return viewManager.getWindowManager().openDialog(
      null,
      getName(),
      WindowManager.IMG_QUESTION,
      msg,
      actions,
      owner
    );
    
    // Done
  }

  /**
   * i18n of a string
   */
  public final String i18n(String key) {
    return i18n(key, (Object[])null);
  }

  /**
   * i18n of a string
   */
  public final String i18n(String key, int sub) {
    return i18n(key, new Integer(sub));
  }

  /**
   * i18n of a string
   */
  public final String i18n(String key, Object sub) {
    return i18n(key, new Object[]{sub});
  }

  /**
   * i18n of a string
   */
  public final String i18n(String key, Object[] subs) {

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
      // 20030529 - don't do a recursive getName() here
      Debug.log(Debug.WARNING, this, "Unknown i18 key : "+key);
      result = key;
    } else {
      if (subs!=null&&subs.length>0) {
        for (int i=0;i<subs.length;i++) 
          if (subs[i]==null) subs[i]="";
        result = Resources.getMessageFormat(result).format(subs);
      }
    }

    // done
    return result;
  }
  
  /**
   * Access to report properties
   */
  private Properties getProperties() {
    if (properties==null) {
      properties = new Properties();
      try {
        String rtype = getClass().getName();
        while (rtype.indexOf('.') >= 0)
          rtype = rtype.substring(rtype.indexOf('.')+1);
        properties.load(getClass().getResourceAsStream(rtype+".properties"));
      } catch (Throwable t) {
        Debug.log(Debug.INFO, this, "Couldn't read properties for "+this);
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
   * Returns the name of this report.
   * This default version get the information from the .property file. A report
   * has to override this method to use another method.
   * @return  Name of the report.
   */
  public String getName() {
    return i18n("name");
  }

  /**
   * Returns the author of this script.
   * This default version get the information from the .property file. A report
   * has to override this method to use another method.
   * @return  Name of the author.
   */
  public String getAuthor() {
    return i18n("author");
  }

  /**
   * Returns the version of this script
   * This default version get the information from the .property file. A report
   * has to override this method to use another method.
   * @return  Version of the report.
   */
  public String getVersion() {
     return i18n("version");
  }

  /**
   * Returns information about this report.
   * This default version get the information from the .property file. A report
   * has to override this method to use another method.
   * @return  Description about the report.
   */
  public String getInfo() {
    return i18n("info");
  }

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

  /**
   * A list of items - I'm currently simply checking if target.getGedcom()!=null
   * to figure out whether the item is still o.k. to propagate to the view
   * manager (a.k.a. not deleted)
   * Listening for updates as a GedcomListener was just too much work  ;)
   */
  private static class ItemList extends JList implements ListCellRenderer, ListSelectionListener, MouseListener {

    /** the view manager */
    private ViewManager manager;

    /** a headless label for rendering */
    private HeadlessLabel label = new HeadlessLabel();

    /** gedcom */
    private Gedcom gedcom;

    /**
     * Constructor
     */
    private ItemList(Gedcom geDcom, Item[] props, ViewManager maNager) {
      super(props);
      // remember
      manager = maNager;
      gedcom = geDcom;
      // setup looks
      setCellRenderer(this);
      label.setOpaque(true);
      addListSelectionListener(this);
      addMouseListener(this);
      // done
    }

    /**
     * Selection changed
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {
      // check selected item
      Item item = (Item)getSelectedValue();
      if (item==null)
        return;
      // propagate
      manager.setContext(new Context(gedcom, null, item.getTarget()));
    }

    /**
     * Our own rendering
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      // colors
      label.setBackground(isSelected ? getSelectionBackground() : getBackground());
      label.setForeground(isSelected ? getSelectionForeground() : getForeground());
      // display item
      Item item = (Item)value;
      label.setText(item.getName());
      label.setIcon(item.getImage());
      // done
      return label;
    }

    /**
     * mouse callbacks
     */
    public void mouseClicked(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
      mouseReleased(e);
    }
    public void mouseReleased(MouseEvent e) {
      // no popup trigger no action
      if (!e.isPopupTrigger()) 
        return;
      Point pos = e.getPoint();
      // find row
      int row = locationToIndex(pos);
      if (row>=0) {
        // select
        setSelectedIndex(row);
        // check item
        Item item = (Item)getModel().getElementAt(row);
        Property target = item.getTarget();
        // create context
        Context context = target==null ? new Context(gedcom) : new Context(target);
        // propagate
        manager.showContextMenu(context, null, this, pos);
      }
    }
  } //PropertyList

  /**
   * A (result) item can be shown to the user
   */
  public static class Item implements Comparable {

    /** attrs */
    private String name;
    private ImageIcon img;
    private Property target;
    
    /**
     * Constructor
     */
    public Item(String naMe, ImageIcon imG, Property taRget) {
      name = naMe;
      img = imG;
      target = taRget;
    }
    
    /**
     * name
     */
    private String getName() {
      return name;
    }
    
    /**
     * img
     */
    private ImageIcon getImage() {
      return img;
    }
    
    /**
     * The target of this item
     */
    private Property getTarget() {
      return target==null ? null : target.getGedcom()==null ? null : target;
    }
    
    /**
     * Compare by text
     */
    public int compareTo(Object o) {
      Item that = (Item)o;
      return this.name.compareTo(that.name);
    }
    
  } //Item

} //Report
