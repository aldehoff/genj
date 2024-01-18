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
import genj.gedcom.Property;
import genj.util.ActionDelegate;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.Resources;
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
import java.awt.event.MouseEvent;
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
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * View for searching
 */
public class SearchView extends JPanel implements ToolBarSupport, ContextSupport {
  
  /** max # hits */
  private final static int MAX_HITS = 100;
  
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
    IMG_STOP  = new ImageIcon(SearchView.class, "Stop.gif" );

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
    listResults = new ResultWidget(); 
    
    // prepare search criteria
    JLabel labelValue = new JLabel(resources.getString("label.value"));
    choiceValue = new ChoiceWidget(oldValues);
    choiceValue.addActionListener(new ActionListener() {
      /** button */
      public void actionPerformed(ActionEvent e) {
        //bStop.doClick();
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
      context = results.getHit(row).getProperty();
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
    /** count of hits found */
    private int count;
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
      count = 0;
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
     * @see genj.util.ActionDelegate#handleThrowable(java.lang.String, java.lang.Throwable)
     */
    protected void handleThrowable(String phase, Throwable t) {
      manager.getWindowManager().openDialog(
        null,
        null,
        WindowManager.IMG_INFORMATION,
        t.getMessage() ,
        WindowManager.OPTIONS_OK,
        SearchView.this 
      );
    }

    /**
     * after execute (on EDT)
     */
    protected void postExecute() {
      // toggle buttons
      bSearch.setEnabled(true);
      bStop.setEnabled(false);
      // done
    }
    
    /** got a hit */
    private synchronized void add(Object hit) {
      // keep
      hits.add(hit);
      // sync (on first)?
      if (hits.size()==1) sync();
      // too many?
      if (count++>MAX_HITS)
        throw new IndexOutOfBoundsException("Too many hits found! Restricting result to "+MAX_HITS+" hits.");
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
      // all but transients
      if (!prop.isTransient()) {
        // check prop's value
        Hit hit = Hit.test(matcher, prop);
        if (hit!=null)
          add(hit);
      }
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
    private List hits = new ArrayList(255);
    
    /**
     * clear the results
     */
    private synchronized void clear() {
      // nothing to do?
      if (hits.isEmpty())
        return;
      // clear&notify
      int size = hits.size();
      hits.clear();
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
      int size = hits.size();
      hits.addAll(list);
      fireIntervalAdded(this, size, hits.size()-1);
      // done
    }
    
    /**
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public Object getElementAt(int index) {
      return hits.get(index);
    }
    
    /**
     * @see javax.swing.ListModel#getSize()
     */
    public int getSize() {
      return hits.size();
    }
    
    /**
     * access to property
     */
    private Hit getHit(int i) {
      return (Hit)hits.get(i);
    }

  } //Results

  /**
   * our specialized list
   */  
  private class ResultWidget extends JList implements ListSelectionListener, ListCellRenderer {
    /** the default renderer */
    private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer(); 
    /**
     * Constructor
     */
    private ResultWidget() {
      super(results);
      setCellRenderer(this);
      addListSelectionListener(this);
      defaultRenderer.setVerticalAlignment(SwingConstants.TOP);
      defaultRenderer.setVerticalTextPosition(SwingConstants.TOP);
    }
    
    /**
     * @see javax.swing.JComponent#addNotify()
     */
    public void addNotify() {
      // continue
      super.addNotify();
      // ready for tooltips
      ToolTipManager.sharedInstance().registerComponent(this);
    }
    
    /**
     * @see javax.swing.JComponent#removeNotify()
     */
    public void removeNotify() {
      // stop tooltips
      ToolTipManager.sharedInstance().unregisterComponent(this);
      // continue
      super.removeNotify();
    }
    
    /**
     * @see javax.swing.JList#getToolTipText(java.awt.event.MouseEvent)
     */
    public String getToolTipText(MouseEvent event) {
      // something to show?
      int i = locationToIndex(event.getPoint());
      if (i<0) return null;
      // return text  
      Hit hit = results.getHit(i);
      return hit.getHtml();
    }

    /**
     * we know about action delegates and will use that here if applicable
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      // delegate component preparation to super
      defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      // prepare hit information
      Hit hit = (Hit)value;
      Property prop = hit.getProperty(); 
      // show hit information
      defaultRenderer.setText(hit.getHtml());
      defaultRenderer.setIcon(hit.getImage());
      // done
      return defaultRenderer;
    }
    /**
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {
      int row = listResults.getSelectedIndex();
      if (row>=0) {
        manager.setContext(results.getHit(row).getProperty());
      }
    }
  } //Renderer
  
 
} //SearchView