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

import genj.gedcom.Change;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.HeadlessLabel;
import genj.util.swing.ImageIcon;
import genj.util.swing.MenuHelper;
import genj.util.swing.TextFieldWidget;
import genj.view.ContextSupport;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.AbstractListModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.View;

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

  /** our actions for patterns */
  private final List actionPatterns = createActionPatterns();
  
  /** gedcom */
  private Gedcom gedcom;
  
  /** registry */
  private Registry registry;
  
  /** manager */
  private ViewManager manager;
  
  /** shown results */
  private ResultWidget listResults;
  private Results results = new Results();
  
  /** criterias */
  private ChoiceWidget choicePath, choiceValue;
  private JCheckBox checkAggregate, checkRegExp;
  private JLabel labelCount;
  
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
        bStop.doClick();
        bSearch.doClick();
      }
    });
    choiceValue.getEditor().getEditorComponent().addMouseListener(new MouseAdapter() {
      /**
       * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
       */
      public void mousePressed(MouseEvent e) {
        mouseReleased(e);
      }
      /**
       * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
       */
      public void mouseReleased(MouseEvent e) {
        if (isRegExpAvailable&&e.isPopupTrigger())
          showRegExPopup(e.getComponent(), e.getPoint());
      }
    });

    checkRegExp = new JCheckBox(resources.getString("label.regexp"), isRegExpAvailable);
    checkRegExp.setEnabled(isRegExpAvailable);

    JLabel labelPath = new JLabel(resources.getString("label.path"));    
    choicePath = new ChoiceWidget(oldPaths);
    choicePath.setEnabled(false);
    
    checkAggregate = new JCheckBox(resources.getString("label.aggregate"));
    checkAggregate.setEnabled(false);
    
    labelCount = new JLabel();
    
    JPanel paneCriteria = new JPanel();
    GridBagHelper gh = new GridBagHelper(paneCriteria);
    gh.add(labelValue    ,0,0,1,1);
    gh.add(checkRegExp   ,1,0,1,1);
    gh.add(choiceValue   ,0,1,3,1, gh.GROW_HORIZONTAL|gh.FILL_HORIZONTAL);
    gh.add(labelPath     ,0,2,3,1);
    gh.add(choicePath    ,0,3,3,1, gh.GROW_HORIZONTAL|gh.FILL_HORIZONTAL);
    gh.add(checkAggregate,0,4,2,1, gh.GROW_HORIZONTAL|gh.FILL_HORIZONTAL);
    gh.add(labelCount    ,2,4,1,1);
    
    // prepare layout
    setLayout(new BorderLayout());
    add(BorderLayout.NORTH , paneCriteria);
    add(BorderLayout.CENTER, new JScrollPane(listResults) );
    choiceValue.requestFocus();

    // done
  }

  /**
   * @see javax.swing.JComponent#addNotify()
   */
  public void addNotify() {
    // start listening
    gedcom.addListener(results);
    // continue
    super.addNotify();
  }
  
  /**
   * @see javax.swing.JComponent#removeNotify()
   */
  public void removeNotify() {
    // stop listening
    gedcom.removeListener(results);
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
   * Show a popup for given 
   */
  private void showRegExPopup(Component comp, Point point) {
    // create a popup
    MenuHelper mh = new MenuHelper();
    JPopupMenu popup = mh.createPopup("");
    // fill with reg exp constructs
    mh.createItems(actionPatterns);
    // show
    popup.show(comp, point.x, point.y);
    choiceValue.getTextWidget().getCaret().setVisible(true);
    // done
  }
  
  /**
   * Create RegExp Pattern Actions
   */
  private List createActionPatterns() {
    // loop until ...
    List result = new ArrayList();
    for (int i=0;;i++) {
      // check text and pattern
      String 
        key = "regexp."+i,
        txt = resources.getString(key+".txt", false),
        pat = resources.getString(key+".pat", false);
      // no more?
      if (txt==null) break;
      // pattern?
      if (pat==null) {
        Debug.log(Debug.WARNING, this, "Encountered regexp entry "+txt+" without pattern");
        continue;
      }
      // create action
      result.add(new ActionPattern(txt,pat));
    }
    return result; 
  }

  /**
   * Action - insert regexp construct
   *   {0} all text
   *   {1} before selection
   *   {2} (selection)
   *   {3} after selection
   */
  private class ActionPattern extends ActionDelegate {
    /** pattern */
    private String pattern;
    /**
     * Constructor
     */
    private ActionPattern(String txt, String pat) {
      super.setText(txt);
      pattern = pat;
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      // analyze what we've got
      TextFieldWidget field = choiceValue.getTextWidget();
      int 
        selStart = field.getSelectionStart(),
        selEnd   = field.getSelectionEnd  ();
      if (selEnd<=selStart) {
        selStart = field.getCaretPosition();
        selEnd   = selStart;
      }
      // {0} all text
      String all = field.getText();
      // {1} before selection
      String before = all.substring(0, selStart);
      // {2} (selection)
      String selection = selEnd>selStart ? '('+all.substring(selStart, selEnd)+')' : "";
      // {3] after selection
      String after = all.substring(selEnd);

      // calculate result
      String result = MessageFormat.format(pattern, new String[]{ all, before, selection, after} );
      int pos = result.indexOf('#');
      result = result.substring(0,pos)+result.substring(pos+1);
      
      // show
      field.setText(result);
      field.select(0,0);
      field.setCaretPosition(pos);
      
      // done
    }
  } //ActionInsert

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
    protected void syncExecute() {
      synchronized (hits) {
        results.add(hits);
        hits.clear();
      }
      labelCount.setText(""+count);
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
      // update count
      labelCount.setText(""+count);
      // toggle buttons
      bSearch.setEnabled(true);
      bStop.setEnabled(false);
      // done
    }
    
    /** search in gedcom (not on EDT) */
    private void search(Gedcom gedcom) {
      for (int t=0; t<gedcom.NUM_TYPES; t++) {
        List es = gedcom.getEntities(t);
        for (int e=0; e<es.size(); e++) {
          search((Entity)es.get(e));
        }
      }
    }
    
    /** search entity (not on EDT) */
    private void search(Entity entity) {
      search((Property)entity);
    }
    
    /** search property (not on EDT) */
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
    
    /** got a hit (not on EDT) */
    private void add(Hit hit) {
      // too many?
      if (count==MAX_HITS)
        throw new IndexOutOfBoundsException("Too many hits found! Restricting result to "+MAX_HITS+" hits.");
      count++;
      // create a view
      listResults.init(hit);
      // synchronized keep
      synchronized (hits) {
        // keep
        hits.add(hit);
        // sync (on first)?
        if (hits.size()==1) sync();
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
  private class Results extends AbstractListModel implements GedcomListener {
    
    /** the results */
    private List hits = new ArrayList(255);
    
    /**
     * clear the results
     */
    private void clear() {
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
    private void add(List list) {
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
     * @see genj.gedcom.GedcomListener#handleChange(genj.gedcom.Change)
     */
    public void handleChange(Change change) {
      if (change.isChanged(Change.PDEL))
        clear();
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
    
    /** our label used for rendering */
    private HeadlessLabel label = new HeadlessLabel(); 

    /** our label used for (async) view calculation */
    private HeadlessLabel viewFactory = new HeadlessLabel(); 
    
    /**
     * Constructor
     */
    private ResultWidget() {
      super(results);
      setCellRenderer(this);
      addListSelectionListener(this);

      label.setOpaque(true);
      label.setFont(getFont());
      viewFactory.setFont(getFont());
    }
    
    /**
     * Create (async) view for given hit
     */
    private void init(Hit hit) {
      hit.setAttribute(viewFactory.setHTML(hit.getHTML()));
    }
    
    /**
     * we know about action delegates and will use that here if applicable
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      // prepare color
      if (isSelected) {
        label.setBackground(list.getSelectionBackground());
        label.setForeground(list.getSelectionForeground());
      } else {
        label.setBackground(list.getBackground());
        label.setForeground(list.getForeground());
      }

      // prepare hit information
      Hit hit = (Hit)value;
      
      // show image
      label.setIcon(hit.getImage());

      // show text view
      label.setView((View)hit.getAttribute());

      // done
      return label;
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
