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
package genj.search;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.MultiLineSupport;
import genj.gedcom.Property;
import genj.util.ActionDelegate;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.WordBuffer;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.ImageIcon;
import genj.view.ContextSupport;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * View for searching
 */
public class SearchView extends JPanel implements ToolBarSupport, ContextSupport {
  
  /** default values */
  private final static String[]
    DEFAULT_OLD_VALUES = {
      "m(a|e)(i|y)er", ".* /.+/", "^M$"
    };
  
  /** how many old values we remember */
  private final static int MAX_OLD = 16;
  
  /** resources */
  /*package*/ static Resources resources = Resources.get(SearchView.class);
  
  /** whether we support regex or not */
  private final boolean isRegExpAvailable = getMatcher("", true)!=null; 
  
  /** gedcom */
  private Gedcom gedcom;
  
  /** registry */
  private Registry registry;
  
  /** manager */
  private ViewManager manager;
  
  /** shown results */
  private JList listResults;
  private Results results = new Results();
  
  /** criterias */
  private ChoiceWidget choicePath, choiceValue;
  private JCheckBox checkAggregate, checkRegExp;

  /** history */
  private LinkedList oldPaths, oldValues;
  
  /** buttons */
  private AbstractButton bSearch, bStop;
  
  /** images */
  private final static ImageIcon
    IMG_START = new ImageIcon(SearchView.class, "Start.gif"),
    IMG_STOP  = new ImageIcon(SearchView.class, "Stop.gif ");

  /**
   * Constructor
   */
  public SearchView(Gedcom geDcom, Registry reGistry, ViewManager maNager) {
    
    // remember
    gedcom = geDcom;
    registry = reGistry;
    manager = maNager;
    
    // lookup old search values
    oldPaths = (LinkedList)registry.get("old.paths", new LinkedList());
    oldValues = (LinkedList)registry.get("old.values", new LinkedList(Arrays.asList(DEFAULT_OLD_VALUES)));
    
    // prepare results
    listResults = new JList(results);
    ListCallback lc = new ListCallback();
    listResults.setCellRenderer(lc);
    listResults.addListSelectionListener(lc);
    
    // prepare search criteria
    JLabel labelValue = new JLabel(resources.getString("label.value"));
    choiceValue = new ChoiceWidget(oldValues);
    choiceValue.addActionListener(new ActionListener() {
      /** button */
      public void actionPerformed(ActionEvent e) {
        bStop.doClick();
        bSearch.doClick();
      }
    });

    JLabel labelPath = new JLabel(resources.getString("label.path"));    
    choicePath = new ChoiceWidget(oldPaths);
    choicePath.setEnabled(false);
    
    checkAggregate = new JCheckBox(resources.getString("label.aggregate"));
    checkAggregate.setEnabled(false);
    
    checkRegExp = new JCheckBox(resources.getString("label.regexp"), isRegExpAvailable);
    checkRegExp.setToolTipText(resources.getString("tip.regexp"));
    checkRegExp.setEnabled(isRegExpAvailable);

    JPanel paneCriteria = new JPanel();
    GridBagHelper gh = new GridBagHelper(paneCriteria);
    gh.add(labelValue    ,0,0,1,1);
    gh.add(checkRegExp   ,1,0,1,1);
    gh.add(choiceValue   ,0,1,2,1, gh.GROW_HORIZONTAL|gh.FILL_HORIZONTAL);
    gh.add(labelPath     ,0,2,2,1);
    gh.add(choicePath    ,0,3,2,1, gh.GROW_HORIZONTAL|gh.FILL_HORIZONTAL);
    gh.add(checkAggregate,0,4,2,1);
    
    // prepare layout
    setLayout(new BorderLayout());
    add(BorderLayout.NORTH , paneCriteria);
    add(BorderLayout.CENTER, new JScrollPane(listResults) );
    choiceValue.requestFocus();

    // done
  }
  
  /**
   * @see javax.swing.JComponent#removeNotify()
   */
  public void removeNotify() {
    // keep old
    registry.put("old.values", oldValues);
    // continue
    super.removeNotify();
  }

  
  /**
   * @see genj.view.ToolBarSupport#populate(javax.swing.JToolBar)
   */
  public void populate(JToolBar bar) {
    ButtonHelper bh = new ButtonHelper().setContainer(bar);
    ActionSearch search = new ActionSearch();
    ActionStop   stop   = new ActionStop  (search);
    bSearch = bh.setEnabled(true ).create(search);
    bStop   = bh.setEnabled(false).create(stop);
  }
  
  /**
   * @see genj.view.ContextSupport#getContextAt(java.awt.Point)
   */
  public Context getContextAt(Point pos) {
    Property context = null;
    int row = listResults.locationToIndex(pos);
    if (row>=0) {
      listResults.setSelectedIndex(row);
      context = results.getProperty(row);
    } 
    return new Context(context);
  }  

  /**
   * @see genj.view.ContextSupport#getContextPopupContainer()
   */
  public JComponent getContextPopupContainer() {
    return listResults;
  }

  /**
   * @see genj.view.ContextSupport#setContext(genj.gedcom.Property)
   */
  public void setContext(Property property) {
    // ignored
  }
  
  /**
   * Remembers a value
   */
  private void rememberValue(String value) {
    // not if empty
    if (value.trim().length()==0) return;
    // keep (up to max)
    oldValues.remove(value);
    oldValues.addFirst(value);
    if (oldValues.size()>MAX_OLD) oldValues.removeLast();
    // update choice
    choiceValue.setValues(oldValues);
    // done
  }

  /**
   * Returns a matcher for given pattern and regex flag
   */
  private Matcher getMatcher(String pattern, boolean regex) {

    Matcher result = null;
    
    // regexp?
    if (regex)  {
      try {
        result = (Matcher)Class.forName("genj.search.RegExMatcher").newInstance(); 
      } catch (Throwable t) {
        return null;
      }
    } else {
      result = new SimpleMatcher();
    }
    
    // init
    result.init(pattern);
    
    // done
    return result;
  }

  /**
   * Action - trigger search
   */
  private class ActionSearch extends ActionDelegate {
    /** hits */
    private List hits = new ArrayList(255);
    /** the current matcher*/
    private Matcher matcher;
    /** constructor */
    private ActionSearch() {
      setImage(IMG_START);
      setAsync(ASYNC_SAME_INSTANCE);
    }
    /**
     * before execute (sync)
     */
    protected boolean preExecute() {
      // clear old
      hits.clear();
      results.clear();
      // update buttons
      bSearch.setEnabled(false);
      bStop.setEnabled(true);
      // prepare matcher
      String value = choiceValue.getText();
      try {
        matcher = getMatcher(value, checkRegExp.isSelected());
      } catch (IllegalArgumentException e) {
        manager.getWindowManager().openDialog(
          null,
          value,
          WindowManager.IMG_ERROR,
          e.getMessage(),
          WindowManager.OPTIONS_OK,
          SearchView.this
        );
        return false;
      }
      // remember
      rememberValue(value);
      // continue
      return true;
    }

    /** run (async) */
    protected void execute() {
      search(gedcom);
    }
    
    /** run (sync() callback on EDT) */
    protected synchronized void syncExecute() {
      results.add(hits);
      hits.clear();
    }

    /**
     * after execute (on EDT)
     */
    protected void postExecute() {
      bSearch.setEnabled(true);
      bStop.setEnabled(false);
    }
    
    /** got a hit */
    private synchronized void add(Object hit) {
      // keep
      hits.add(hit);
      // sync (on first)?
      if (hits.size()==1) sync();
      // done
    }
    
    /** search in gedcom */
    private void search(Gedcom gedcom) {
      for (int t=0; t<gedcom.NUM_TYPES; t++) {
        List es = gedcom.getEntities(t);
        for (int e=0; e<es.size(); e++) {
          search((Entity)es.get(e));
        }
      }
    }
    
    /** search entity */
    private void search(Entity entity) {
      search((Property)entity);
    }
    
    /** search property */
    private void search(Property prop) {
      // still going?
      if (getThread().isInterrupted()) return;
      // check prop
      String value = prop instanceof MultiLineSupport ? ((MultiLineSupport)prop).getLinesValue() : prop.getValue(); 
      if (matcher.matches(value))      
        add(prop);
      // check subs
      int n = prop.getNoOfProperties();
      for (int i=0;i<n;i++) {
        search(prop.getProperty(i));
      }
      // done
    }
    
  } //ActionSearch
  
  /**
   * Action - stop search
   */
  private class ActionStop extends ActionDelegate {
    /** start */
    private ActionSearch start;
    /** constructor */
    private ActionStop(ActionSearch start) {
      setImage(IMG_STOP);
      this.start = start;
    }
    /** run */
    protected void execute() {
      start.cancel(false);
    }
  } //ActionStop

  /**
   * Our result bucket
   */
  private class Results extends AbstractListModel {
    
    /** the results */
    private List properties = new ArrayList(255);
    
    /**
     * clear the results
     */
    private synchronized void clear() {
      // nothing to do?
      if (properties.isEmpty())
        return;
      // clear&notify
      int size = properties.size();
      properties.clear();
      fireIntervalRemoved(this, 0, size-1);
      // done
    }
    
    /**
     * add a result
     */
    private synchronized void add(List list) {
      // nothing to do?
      if (list.isEmpty()) 
        return;
      // remember 
      int size = properties.size();
      properties.addAll(list);
      fireIntervalAdded(this, size, properties.size()-1);
      // done
    }
    
    /**
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public Object getElementAt(int index) {
      return properties.get(index);
    }
    
    /**
     * @see javax.swing.ListModel#getSize()
     */
    public int getSize() {
      return properties.size();
    }
    
    /**
     * access to property
     */
    private Property getProperty(int i) {
      return (Property)properties.get(i);
    }

  } //Results

  /**
   * our specialized renderer
   */  
  private class ListCallback extends DefaultListCellRenderer implements ListSelectionListener {
    /**
     * we know about action delegates and will use that here if applicable
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      Property prop = (Property)value; 
      WordBuffer words = new WordBuffer();
      words.append(prop.getTag());
      if (prop instanceof Entity) {
        words.append('@'+((Entity)prop).getId()+'@');
      }
      words.append(prop.getValue());
      setText(words.toString());
      setIcon(prop.getImage(true));
      return this;
    }
    /**
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {
      int row = listResults.getSelectedIndex();
      if (row>=0) {
        manager.setContext(results.getProperty(row));
      }
    }
  } //Renderer

} //SearchView