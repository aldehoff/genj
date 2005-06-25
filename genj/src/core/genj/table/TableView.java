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
package genj.table;

import genj.common.AbstractPropertyTableModel;
import genj.common.PropertyTableWidget;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.gedcom.Transaction;
import genj.util.ActionDelegate;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.view.Context;
import genj.view.ContextListener;
import genj.view.ContextProvider;
import genj.view.ContextSelectionEvent;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 * Component for showing entities of a gedcom file in a tabular way
 */
public class TableView extends JPanel implements ToolBarSupport, ContextListener, ContextProvider {

  /** a static set of resources */
  private Resources resources = Resources.get(this);
  
  /** the gedcom we're looking at */
  private Gedcom gedcom;
  
  /** the manager around us */
  private ViewManager manager;
  
  /** the registry we keep */
  private Registry registry;
  
  /** the title we keep */
  private String title;
  
  /** the table we're using */
  /*package*/ PropertyTableWidget propertyTable;
  
  /** the gedcom listener we're using */
  private GedcomListener listener;
  
  /** the modes we're offering */
  private Map modes = new HashMap();
    {
      modes.put(Gedcom.INDI, new Mode(Gedcom.INDI, new String[]{"INDI","INDI:NAME","INDI:SEX","INDI:BIRT:DATE","INDI:BIRT:PLAC","INDI:FAMS", "INDI:FAMC", "INDI:OBJE:FILE"}));
      modes.put(Gedcom.FAM , new Mode(Gedcom.FAM , new String[]{"FAM" ,"FAM:MARR:DATE","FAM:MARR:PLAC", "FAM:HUSB", "FAM:WIFE", "FAM:CHIL" }));
      modes.put(Gedcom.OBJE, new Mode(Gedcom.OBJE, new String[]{"OBJE","OBJE:TITL"}));
      modes.put(Gedcom.NOTE, new Mode(Gedcom.NOTE, new String[]{"NOTE","NOTE:NOTE"}));
      modes.put(Gedcom.SOUR, new Mode(Gedcom.SOUR, new String[]{"SOUR","SOUR:TITL", "SOUR:TEXT"}));
      modes.put(Gedcom.SUBM, new Mode(Gedcom.SUBM, new String[]{"SUBM","SUBM:NAME" }));
      modes.put(Gedcom.REPO, new Mode(Gedcom.REPO, new String[]{"REPO","REPO:NAME", "REPO:NOTE"}));
    };
  
  /** current type we're showing */
  private Mode currentMode = getMode(Gedcom.INDI);
  
  /**
   * Constructor
   */
  public TableView(String titl, Gedcom gedcom, Registry registry, ViewManager mgr) {
    
    // keep some stuff
    this.gedcom = gedcom;
    this.registry = registry;
    this.title = titl;
    this.manager = mgr;
    
    // read properties
    loadProperties();
    
    // create our table
    propertyTable = new PropertyTableWidget(null, manager);
    propertyTable.setAutoResize(false);
    propertyTable.addContextListener(new ContextListener() {
      public void handleContextSelectionEvent(ContextSelectionEvent event) {
        manager.fireContextSelected(event);
      }
    });

    // lay it out
    setLayout(new BorderLayout());
    add(propertyTable, BorderLayout.CENTER);
    
    // done
  }
  
  /**
   * ContextProvider callback 
   */
  public Context getContext() {
    return new Context(gedcom);
  }
  
  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(480,320);
  }
  
  /**
   * callback - chance to hook-up on add
   */  
  public void addNotify() {
    // continue
    super.addNotify();
    // hook on
    Mode set = currentMode;
    currentMode = null;
    setMode(set);
  }

  /**
   * callback - chance to hook-off on remove
   */
  public void removeNotify() {
    // save state
    saveProperties();
    // delegate
    super.removeNotify();
    // make sure the swing model is disconnected from gedcom model
    propertyTable.setModel(null);
  }
  
  /**
   * Returns a mode for given tag
   */
  /*package*/ Mode getMode() {
    return currentMode;
  }
  
  /**
   * Returns a mode for given tag
   */
  /*package*/ Mode getMode(String tag) {
    // known mode?
    Mode mode = (Mode)modes.get(tag); 
    if (mode==null) {
      mode = new Mode(tag, new String[0]);
      modes.put(tag, mode);
    }
    return mode;
  }
  
  /**
   * Sets the type of entities to look at
   */
  /*package*/ void setMode(Mode set) {
    // give mode a change to grab what it wants to preserve
    if (currentMode!=null)
      currentMode.save(registry);
    // remember current mode
    currentMode = set;
    // tell to table
    propertyTable.setModel(new Model(currentMode));
    // update its columns
    propertyTable.setColumnWidths(currentMode.getWidths());
    // and sorting
    propertyTable.setSortedColumn(currentMode.sort);
  }
  
  /**
   * callback - context changed
   */
  public void handleContextSelectionEvent(ContextSelectionEvent event) {
    
    // a type that we're interested in?
    Context context  = event.getContext();
    Entity entity = context.getEntity();
    if (entity==null||!entity.getTag().equals(currentMode.getTag())) 
      return;
      
    // change selection
    propertyTable.select(entity, context.getProperty());

    // done
  }

  /**
   * @see genj.view.ToolBarSupport#populate(JToolBar)
   */
  public void populate(JToolBar bar) {
    // create buttons for mode switch
    ButtonHelper bh = new ButtonHelper();
    bh.setFocusable(false);
    
    for (int i=0;i<Gedcom.ENTITIES.length;i++) {
      bar.add(bh.create(new ActionChangeType(getMode(Gedcom.ENTITIES[i]))));
    }
    
    // done
  }
  
  /**
   * Read properties from registry
   */
  private void loadProperties() {

    // get modes
    Iterator it = modes.values().iterator();
    while (it.hasNext()) {
      Mode mode = (Mode)it.next();
      mode.load(registry);
    }

    // get current mode
    String tag = registry.get("mode", "");
    if (modes.containsKey(tag))
      currentMode = getMode(tag);
    
    // Done
  }
  
  /**
   * Write properties from registry
   */
  private void saveProperties() {
    
    // save current type
    registry.put("mode", currentMode.getTag());
    
    // save modes
    Iterator it = modes.values().iterator();
    while (it.hasNext()) {
      Mode mode = (Mode)it.next();
      mode.save(registry);
    }
    // Done
  }  
  
  /**
   * Action - flip view to entity type
   */
  private class ActionChangeType extends ActionDelegate {
    /** the mode this action triggers */
    private Mode mode;
    /** constructor */
    ActionChangeType(Mode mode) {
      this.mode = mode;
      setTip(resources.getString("mode.tip", Gedcom.getName(mode.getTag(),true)));
      setImage(Gedcom.getEntityImage(mode.getTag()));
    }
    /** run */
    public void execute() {
      setMode(mode);
    }
  } //ActionMode
  
  /** 
   * A PropertyTableModelWrapper
   */
  private class Model extends AbstractPropertyTableModel {

    /** mode */
    private Mode mode;
    
    /** our cached rows */
    private Entity[] rows;
    
    /** constructor */
    private Model(Mode set) {
      mode = set;
      handleChange(null);
    }
    
    /**
     * Gedcom callback
     */
    public void handleChange(Transaction tx) {

      // no drastic change?
      if (tx!=null&&tx.get(Transaction.ENTITIES_ADDED).isEmpty()&&tx.get(Transaction.ENTITIES_DELETED).isEmpty()) { 
        fireContentChanged();
        return;
      }
      
      // cache entities
      Collection es = gedcom.getEntities(mode.getTag());
      rows = (Entity[])es.toArray(new Entity[es.size()]);
      
      // continue
      fireRowsChanged();
    }

    /** gedcom */
    public Gedcom getGedcom() {
      return gedcom;
    }

    /** # columns */
    public int getNumCols() {
      return mode.getPaths().length;
    }
    
    /** # rows */
    public int getNumRows() {
      return gedcom.getEntities(mode.getTag()).size();
    }
    
    /** path for colum */
    public TagPath getPath(int col) {
      return mode.getPaths()[col];
    }

    /** property for row */
    public Property getProperty(int row) {
      return rows[row];
    }
    
  } //Model

  /**
   * A mode is a configuration for a set of entities
   */
  /*package*/ class Mode {
    
    /** attributes */
    private String tag;
    private String[] defaults;
    private TagPath[] paths;
    private int[] widths;
    private int sort = 0;
    
    /** constructor */
    private Mode(String t, String[] d) {
      // remember
      tag      = t;
      defaults = d;
      paths    = TagPath.toArray(defaults);
      widths   = new int[paths.length];
    }
    
    /** load properties from registry */
    private void load(Registry r) {
      
      String[] ps = r.get(tag+".paths" , (String[])null);
      if (ps!=null) 
        paths = TagPath.toArray(ps);
      
      int[] ws = r.get(tag+".widths", (int[])null);
      if (ws!=null) {
        widths = ws;
      } else {
        widths = new int[paths.length];
        for (int i=0;i<widths.length;i++)  widths[i]=64;
      }

      sort = registry.get(tag+".sort", 0);
      
    }
    
    /** set paths */
    /*package*/ void setPaths(TagPath[] set) {
      paths = set;
      if (currentMode==this)
        setMode(currentMode);
    }
    
    /** get paths */
    /*package*/ TagPath[] getPaths() {
      return paths;
    }
    
    /** get column widths */
    private int[] getWidths() {
      return widths;
    }

    /** set column widths */
    private void setWidths(int[] set) {
      widths = set;
    }

    /** save properties from registry */
    private void save(Registry r) {
      
      // grab current column widths & sort column
      if (currentMode==this) {
        widths = propertyTable.getColumnWidths();
        sort = propertyTable.getSortedColumn();
      }

	    registry.put(tag+".paths" , paths);
	    registry.put(tag+".widths", widths);
	    registry.put(tag+".sort"  , sort);
    }
    
    /** tag */
    /*package*/ String getTag() {
      return tag;
    }
    
  } //Mode
  
} //TableView
