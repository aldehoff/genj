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

import genj.edit.EditViewFactory;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.option.Option;
import genj.util.Debug;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.HeadlessLabel;
import genj.view.ViewManager;
import genj.view.widgets.SelectEntityWidget;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.PrintWriter;
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

  /** options */
  protected final static int
    OPTION_YESNO    = 0,
    OPTION_OKCANCEL = 1,
    OPTION_OK       = 2;
    
  private final static String[][] OPTIONS = {
    WindowManager.OPTIONS_YES_NO, 
    WindowManager.OPTIONS_OK_CANCEL,
    WindowManager.OPTIONS_OK
  };

  /** alignment options */
  protected final static int
    ALIGN_LEFT   = 0,
    ALIGN_CENTER = 1,
    ALIGN_RIGHT  = 2;

  /** one report for all reports */
  private final static Registry REGISTRY = new Registry("genj-reports");

  /** language we're trying to use */
  private final static String lang = Locale.getDefault().getLanguage();

  /** i18n texts */
  private Properties i18n;

  /** out */
  private PrintWriter out;

  /** a view manager */
  private ViewManager viewManager;

  /** local registry */
  protected Registry registry;

  /** owning component */
  private JComponent owner;
  
  /** options */
  private Option[] options;

  /**
   * Constructor
   */
  protected Report() {
    
    registry = new Registry(REGISTRY, getClass().getName());
    
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
    for (int i=0;i<options.length;i++) {
      Option option = options[i];
      registry.put(option.getKey(), option.getValue().toString());
    }
    // done
  }
  
  /**
   * Get report's options
   */
  /*package*/ Option[] getOptions() {
    
    // already calculated
    if (options!=null)
      return options;
      
    // calculate options
    options = Option.getOptions(this, new Properties() {
      public String getProperty(String key) {
        return i18n(key);
      }
    });
    
    // restore options values
    for (int i=0;i<options.length;i++) {
      Option option = options[i];
      String value = registry.get(option.getKey(), (String)null);
      if (value!=null) 
        option.setValue(value);
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

    JFileChooser chooser = new JFileChooser(".");
    chooser.setFileSelectionMode(chooser.DIRECTORIES_ONLY);
    chooser.setDialogTitle(title);
    if (chooser.APPROVE_OPTION != chooser.showDialog(owner,button)) {
      return null;
    }
    return chooser.getSelectedFile();

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
    viewManager.getWindowManager().openNonModalDialog(
      registry.getView()+"#items",
      getName(),
      ReportViewFactory.IMG,
      content,
      WindowManager.OPTION_OK,
      owner
    );

    // done
  }

  /**
   * Helper method that queries the user for an entity
   * @param gedcom to use
   * @param tag the tag of the entities to show
   * @param sortPath path to sort by or null
   */
  public final Entity getEntityFromUser(String msg, Gedcom gedcom, String tag) {
    
    SelectEntityWidget select = new SelectEntityWidget(tag, gedcom.getEntities(tag), "");

    int rc = viewManager.getWindowManager().openDialog(
      null,
      getName(),
      WindowManager.IMG_QUESTION,
      new JComponent[]{new JLabel(msg),select},
      WindowManager.OPTIONS_OK_CANCEL,
      owner
    );

    return rc==0 ? select.getEntity() : null;
  }

  /**
   * Helper method that queries the user for a choice of non-editable items
   */
  public final Object getValueFromUser(String msg, Object[] choices, Object selected) {

    ChoiceWidget choice = new ChoiceWidget(choices, selected);
    choice.setEditable(false);

    int rc = viewManager.getWindowManager().openDialog(
      null,
      getName(),
      WindowManager.IMG_QUESTION,
      new JComponent[]{new JLabel(msg),choice},
      WindowManager.OPTIONS_OK_CANCEL,
      owner
    );

    return rc==0 ? choice.getSelectedItem() : null;
  }

  /**
   * Helper method that queries the user for a text-value giving him a
   * choice of remembered values
   */
  public final String getValueFromUser(String key, String msg, String[] choices) {

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

    int rc = viewManager.getWindowManager().openDialog(
      null,
      "Report Input",
      WindowManager.IMG_QUESTION,
      new JComponent[]{new JLabel(msg),choice},
      WindowManager.OPTIONS_OK_CANCEL,
      owner
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
  public final boolean getOptionFromUser(String msg, int option) {
    return 0==getOptionFromUser(msg, OPTIONS[option]);
  }
  
  /**
   * Helper method that queries the user for yes/no input
   */
  public final int getOptionFromUser(String msg, String[] options) {
    
    return viewManager.getWindowManager().openDialog(
      null,
      getName(),
      WindowManager.IMG_QUESTION,
      msg,
      options,
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
    if (i18n==null) {
      i18n = new Properties();
      try {
        String rtype = getClass().getName();
        while (rtype.indexOf('.') >= 0)
          rtype = rtype.substring(rtype.indexOf('.')+1);
        i18n.load(getClass().getResourceAsStream(rtype+".properties"));
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
      // 20030529 - don't do a recursive getName() here
      Debug.log(Debug.WARNING, this, "Unknown i18 key : "+key);
      result = key;
    } else {
      if (subs!=null&&subs.length>0)
        result = Resources.getMessageFormat(result).format(subs);
    }

    // done
    return result;
  }

  /**
   * Align a simple text
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
  private static class ItemList extends JList implements ListCellRenderer, ListSelectionListener {

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
      Property target = item.getTarget();
      if (target==null)
        return;
      // make sure an EditView is opened
      if (!EditViewFactory.isEditViewAvailable(manager, target.getGedcom()))
        EditViewFactory.openForEdit(manager, target.getEntity());
      // propagate
      manager.setContext(target);
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

  } //PropertyList

  /**
   * A (result) item can be shown to the user
   */
  public static class Item {

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
    
  } //Item

} //Report
