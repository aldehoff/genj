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
import genj.option.Option;
import genj.option.OptionsWidget;
import genj.util.ActionDelegate;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImageIcon;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.swing.AbstractButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;

/**
 * Component for running reports on genealogic data
 */
public class ReportView extends JPanel implements ToolBarSupport {

  /** time between flush of output writer to output text area */
  private final static long FLUSH_WAIT = 200;

  /** statics */
  private final static ImageIcon 
    imgShell = new ImageIcon(ReportView.class,"ReportShell.gif"), 
    imgGui   = new ImageIcon(ReportView.class,"ReportGui.gif"  ),
    imgStart = new ImageIcon(ReportView.class,"Start.gif"      ), 
    imgStop  = new ImageIcon(ReportView.class,"Stop.gif"       ),
    imgSave  = new ImageIcon(ReportView.class,"Save.gif"       ),
    imgReload= new ImageIcon(ReportView.class,"Reload.gif"     );


  /** gedcom this view is for */
  private Gedcom      gedcom;
  
  /** components to show report info */
  private JLabel      lAuthor,lVersion;
  private JTextPane   tpInfo;
  private JTextArea   taOutput;
  private JList       listOfReports;
  private JTabbedPane tabbedPane;
  private AbstractButton bStart,bStop,bClose,bSave,bReload;
  private OptionsWidget owOptions;
  
  /** registry for settings */
  private Registry registry;
  
  /** resources */
  private Resources resources = Resources.get(this);
  
  /** manager */
  private ViewManager manager ;

  /** title of this view */  
  private String title;

  /**
   * Constructor
   */
  public ReportView(String theTitle, Gedcom theGedcom, Registry theRegistry, ViewManager theManager) {

    // Data
    gedcom   = theGedcom;
    registry = theRegistry;
    manager  = theManager;
    title    = theTitle;

    // Layout for this component
    setLayout(new BorderLayout());

    // Noteboook in Center
    tabbedPane = new JTabbedPane();
    add(tabbedPane,"Center");

    // three tabs
    Callback callback = new Callback();
    tabbedPane.add(resources.getString("report.reports"),createReportList(callback));
    tabbedPane.add(resources.getString("report.output"),createReportOutput(callback));
    tabbedPane.add(resources.getString("report.options"), createReportOptions());
    
    // done
  }
  
  /**
   * @see javax.swing.JComponent#removeNotify()
   */
  public void removeNotify() {
    // continue
    super.removeNotify();
    // save report options
    ReportLoader.getInstance().saveOptions();
  }

  /**
   * Create tab content for report list/info
   */
  private JPanel createReportList(Callback callback) {
    
    // Panel for Report
    JPanel reportPanel = new JPanel();
    reportPanel.setBorder(new EmptyBorder(3,3,3,3));
    GridBagHelper gh = new GridBagHelper(reportPanel);

    // ... List of reports
    listOfReports = new JList(ReportLoader.getInstance().getReports());
    listOfReports.setCellRenderer(callback);
    listOfReports.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listOfReports.addListSelectionListener(callback);

    JScrollPane spList = new JScrollPane(listOfReports) {
      /** min = preferred */
      public Dimension getMinimumSize() {
        return super.getPreferredSize();
      }
    };
    spList.setHorizontalScrollBarPolicy(spList.HORIZONTAL_SCROLLBAR_NEVER);
    gh.add(spList,1,1,1,4,GridBagHelper.GROWFILL_VERTICAL);

    // ... Report's author
    gh.setParameter(gh.FILL_HORIZONTAL);
    
    lAuthor = new JLabel("");
    lAuthor.setForeground(Color.black);

    gh.add(new JLabel(resources.getString("report.author")),2,1);
    gh.add(lAuthor,3,1,1,1,GridBagHelper.GROWFILL_HORIZONTAL);

    // ... Report's version
    lVersion = new JLabel();
    lVersion.setForeground(Color.black);

    gh.add(new JLabel(resources.getString("report.version")),2,2);
    gh.add(lVersion,3,2);

    // ... Report's infos
    tpInfo = new JTextPane();
    tpInfo.setEnabled(false);
    JScrollPane spInfo = new JScrollPane(tpInfo);
    gh.add(new JLabel(resources.getString("report.info")),2,3);
    gh.add(spInfo,2,4,2,1,gh.FILL_BOTH);

    // done
    return reportPanel;
    
  }

  /**
   * Create the tab content for report output
   */  
  private JComponent createReportOutput(Callback callback) {
    
    // Panel for Report Output
    taOutput = new JTextArea();
    taOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
    taOutput.setEditable(false);
    taOutput.addMouseMotionListener(callback);
    taOutput.addMouseListener(callback);

    // Done
    return new JScrollPane(taOutput);
  }
  
  /**
   * Create the tab content for report options
   */
  private JComponent createReportOptions() {
    owOptions = new OptionsWidget();
    return owOptions;
  }

  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(480,320);
  }

  /**
   * Adds a line of ouput
   */
  /*package*/ void addOutput(String line) {
    taOutput.append(line);

    if (taOutput.getText().length()>0) {
// 20030530 - why did I check for valueisadjusting here?      
//    if (!spOutput.getVerticalScrollBar().getValueIsAdjusting()&&taOutput.getText().length()>0) {
      taOutput.setCaretPosition(taOutput.getText().length()-1);
    }
  }
  
  /**
   * Returns the view manager
   */
  /*package*/ ViewManager getViewManager() {
    return manager;
  }
  
  /**
   * Runs a specific report
   */
  /*package*/ void run(Report report, Object context) {
    // not if running
    if (!bStart.isEnabled()) return;
    // to front
    manager.showView(this);
    // start it
    listOfReports.setSelectedValue(report, true);
    new ActionStart(context).trigger();
  }

  /**
   * Helper that sets buttons states
   */
  private boolean setRunning(boolean on) {

    // Show it on buttons
    bStart.setEnabled(!on);
    bStop .setEnabled(on);
    if (bClose!=null) {
      bClose.setEnabled(!on);
    }
    bReload.setEnabled(!ReportLoader.getInstance().isReportsInClasspath());

    taOutput.setCursor(Cursor.getPredefinedCursor(
      on?Cursor.WAIT_CURSOR:Cursor.DEFAULT_CURSOR
    ));

    // Done
    return true;
  }
  
  /**
   * @see genj.view.ToolBarSupport#populate(javax.swing.JToolBar)
   */
  public void populate(JToolBar bar) {
    
    // Buttons at bottom
    ButtonHelper bh = new ButtonHelper()
      .setResources(resources)
      .setContainer(bar)
      .setFocusable(false);

    ActionStart astart = new ActionStart(gedcom);
    bStart = bh.create(astart);
    bStop  = bh.setEnabled(false).create(new ActionStop(astart));    
    bSave  = bh.setEnabled(true).create(new ActionSave());
    bReload= bh.setEnabled(!ReportLoader.getInstance().isReportsInClasspath()).create(new ActionReload());
   
    // done 
  }

  /**
   * Action: RELOAD
   */
  private class ActionReload extends ActionDelegate {
    protected ActionReload() {
      setImage(imgReload);
      setTip("report.reload.tip");
    }
    protected void execute() {
      // show first page and unselect report
      tabbedPane.getModel().setSelectedIndex(0);
      listOfReports.setSelectedIndices(new int[0]);
      // .. do it (forced!);
      ReportLoader.clear();
      // .. get them
      Report reports[] = ReportLoader.getInstance().getReports();
      // .. update
      listOfReports.setListData(reports);
      // .. done
    }
  } //ActionReload

  /**
   * Action: STOP
   */
  private class ActionStop extends ActionDelegate {
    private ActionStart start;
    protected ActionStop(ActionStart start) {
      setImage(imgStop);
      setTip("report.stop.tip");
      this.start=start;
    }
    protected void execute() {
      start.cancel(false);
    }
  } //ActionStop

  /**
   * Action: START
   */
  private class ActionStart extends ActionDelegate {
    /** context to run on */
    private Object context;
    
    /** the running report */
    private Report instance;
    
    /** an output writer */
    private PrintWriter out = new PrintWriter(new OutputWriter());
    
    /** constructor */
    protected ActionStart(Object coNtext) {
      // remember
      context = coNtext;
      // setup async
      setAsync(ASYNC_SAME_INSTANCE);
      // show
      setImage(imgStart);
      setTip("report.start.tip");
    }
    
    /**
     * pre execute
     */
    protected boolean preExecute() {
      
      // .. change buttons
      setRunning(true);
      
      // clear instance
      instance = null;
  
      // Calc Report
      Report report = (Report)listOfReports.getSelectedValue();
      if (report==null) 
        return false;

      // check if appropriate
      if (report.accepts(context)==null) {
        manager.getWindowManager().openDialog(
          null,
          report.getName(),
          WindowManager.IMG_ERROR,
          resources.getString("report.noaccept"),
          WindowManager.OPTIONS_OK,
          ReportView.this
        );
        instance = null;
        return false;
      }

      // create our own private instance  
      instance = report.getInstance(manager, ReportView.this, out);
      
      // .. switch to output
      if (report.usesStandardOut()) {
        tabbedPane.getModel().setSelectedIndex(1);
      }
      taOutput.setText("");
      
      // start transaction
      if (!report.isReadOnly()) {
        if (!gedcom.startTransaction())
          return false; //FIXME we should tell the user
      }
  
      // done
      return true;
    }
    /**
     * execute
     */
    protected void execute() {
      try {
        instance.start(context);
      } catch (ReportCancelledException ex) {
      } catch (Throwable t) {
        // Running report failed
        instance.println(t);
      }
    }
    
    /**
     * post execute
     */
    protected void postExecute() {
      // tx to end?
      if (instance!=null&&!instance.isReadOnly()) {
        gedcom.endTransaction();
      }
      // flush
      out.flush();
      // stop run
      setRunning(false);
    }
  } //ActionStart
  
  /**
   * Action: SAVE
   */
  private class ActionSave extends ActionDelegate {
    protected ActionSave() {
      setImage(imgSave);
      setTip("report.save.tip");
    }
    protected void execute() {

      // .. choose file
      JFileChooser chooser = new JFileChooser(".");
      chooser.setDialogTitle("Save Output");
  
      if (JFileChooser.APPROVE_OPTION != chooser.showDialog(ReportView.this,"Save")) {
        return;
      }
      File file = chooser.getSelectedFile();
      if (file==null) {
        return;
      }
  
      // .. exits ?
      if (file.exists()) {
        int rc = manager.getWindowManager().openDialog(null, title, WindowManager.IMG_WARNING, "File exists. Overwrite?", WindowManager.OPTIONS_YES_NO, ReportView.this);
        if (rc!=0) {
          return;
        }
      }
  
      // .. open file
      final FileWriter writer;
      try {
        writer = new FileWriter(file);
      } catch (IOException ex) {
        manager.getWindowManager().openDialog(
          null, 
          title,
          WindowManager.IMG_ERROR, 
          "Error while saving to\n"+file.getAbsolutePath(), 
          (String[])null,
          ReportView.this
        );
        return;
      }
  
      // .. save data
      try {
  
        BufferedWriter out = new BufferedWriter(writer);
        String data = taOutput.getText();
        out.write(data,0,data.length());
        out.close();
  
      } catch (IOException ex) {
      }
  
      // .. done
    }

  } //ActionSave
  
  /**
   * A private callback for various messages coming in 
   */
  private class Callback extends MouseAdapter implements ListCellRenderer, ListSelectionListener, MouseMotionListener {

    /** a default renderer for list */
    private DefaultListCellRenderer defRenderer = new DefaultListCellRenderer();
    
    /** the currently found entity id */
    private String id = null;

    /**
     * Return component for rendering list element
     */
    public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
      defRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      Report report = (Report)value;
      defRenderer.setText(report.getName());
      if (report.usesStandardOut()) {
        defRenderer.setIcon(imgShell);
      } else {
        defRenderer.setIcon(imgGui);
      }
      return defRenderer;
    }
    
    /**
     * Monitor changes to selection of reports
     */
    public void valueChanged(ListSelectionEvent e) {
      // update info
      if (listOfReports.getSelectedIndices().length!=1) {
        lAuthor  .setText("");
        lVersion .setText("");
        tpInfo   .setText("");
        owOptions.setOptions(new Option[0]);
      } else {
        Report report = (Report)listOfReports.getSelectedValue();
        lAuthor  .setText(report.getAuthor());
        lVersion .setText(report.getVersion());
        tpInfo   .setText(report.getInfo());
        tpInfo   .setCaretPosition(0);
        owOptions.setOptions(report.getOptions());
      }
    }
    
    /**
     * Check if user moves mouse above something recognizeable in output
     */
    public void mouseMoved(MouseEvent e) {
      
      // try to find id at location
      id = markIDat(e.getPoint());
      
      // set cursor
      taOutput.setCursor(Cursor.getPredefinedCursor(id==null?Cursor.DEFAULT_CURSOR:Cursor.HAND_CURSOR));
      
      // done
    }
    
    /**
     * Tries to find an entity id at given position in output
     */
    private String markIDat(Point loc) {
      
      try {
        // do we get a position in the model?
        int pos = taOutput.viewToModel(loc);
        if (pos<0) 
          return null;
          
        // scan doc
        Document doc = taOutput.getDocument();
        Segment seg = new Segment();
        
        // not on 'space' ?
        doc.getText(pos, 1, seg);
        if (!Character.isLetterOrDigit(seg.first())) 
          return null;
          
        // find '@' to the left
        for (int i=0;;i++) {
          // stop looking after 10 or bot
          if (pos==0||i==10)
            return null;
          // check for starting '@'
          doc.getText(--pos, 1, seg);
          if (seg.first()=='@') 
            break;
          // continue
        }
        
        // find '@' to the right
        for (int i=0;;i++) {
          // stop looking after 10 or eot
          if (seg.count==doc.getLength()-pos||i==10)
            return null;
          // get more text and check for ending '@'            
          doc.getText(pos, seg.count+1, seg);
          if (seg.last()=='@') 
            break;
          // continue
        }
        
        // mark it
        taOutput.setCaretPosition(pos);
        taOutput.moveCaretPosition(pos+seg.count);
  
        // return in betwee
        return new String(seg.array, seg.offset+1, seg.count-2);
          
        // done
      } catch (BadLocationException ble) {
      }
      
      // not found
      return null;
    }

    /**
     * have to implement MouseMotionListener.mouseDragger()
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent e) {
      // ignored
    }

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
      // no id no fun
      if (id==null)
        return;
      // try to find entity with id
      Entity entity = gedcom.getEntity(id);
      if (entity!=null)
        manager.setContext(entity);
      // done
    }

  } //Callback

  /**
   * A printwriter that directs output to the text area
   */
  private class OutputWriter extends Writer {
    
    /** buffer */
    private StringBuffer buffer = new StringBuffer(4*1024);
    
    /** timer */
    private long lastFlush = -1;
    
    /**
     * @see java.io.Writer#close()
     */
    public void close() {
      // clear buffer
      buffer.setLength(0);
    }
 
    /**
     * @see java.io.Writer#flush()
     */
    public void flush() {
      // mark
      lastFlush = System.currentTimeMillis();
      // something to flush?
      if (buffer.length()==0)
        return;
      // output
      taOutput.append(buffer.toString());
      // clear buffer
      buffer.setLength(0);
      // done
    }
    
    /**
     * @see java.io.Writer#write(char[], int, int)
     */
    public void write(char[] cbuf, int off, int len) throws IOException {
      // append to buffer
      buffer.append(cbuf, off, len);
      // check flush
      if (System.currentTimeMillis()-lastFlush > FLUSH_WAIT)
        flush(); 
      // done                  
    }

  } //OutputWriter
  
} //ReportView
