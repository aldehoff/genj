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

import genj.gedcom.Gedcom;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.swing.AbstractButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
  private JScrollPane spOutput;
  private JTextArea   taOutput;
  private JList       listOfReports;
  private JTabbedPane tabbedPane;
  private AbstractButton bStart,bStop,bClose,bSave,bReload;
  
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

    // Panel for Report
    JPanel reportPanel = new JPanel();
    reportPanel.setBorder(new EmptyBorder(3,3,3,3));
    GridBagHelper gh = new GridBagHelper(reportPanel);
    tabbedPane.add(resources.getString("report.reports"),reportPanel);

    // ... List of reports
    ListGlue glue = new ListGlue();
    Report reports[] = ReportLoader.getInstance().getReports();
    listOfReports = new JList(reports);
    listOfReports.setCellRenderer(glue);
    listOfReports.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listOfReports.addListSelectionListener(glue);

    JScrollPane spList = new JScrollPane(listOfReports);
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
    spInfo.setPreferredSize(new Dimension(0,0));
    gh.add(new JLabel(resources.getString("report.info")),2,3);
    gh.add(spInfo,2,4,2,1,gh.FILL_BOTH);

    // Panel for Report Output
    taOutput = new JTextArea();
    taOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
    taOutput.setEditable(false);

    spOutput = new JScrollPane(taOutput);
    tabbedPane.add(resources.getString("report.output"),spOutput);

    // Done
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

    if (!spOutput.getVerticalScrollBar().getValueIsAdjusting()&&taOutput.getText().length()>0) {
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
   * Report Renderer
   */
  private class ListGlue extends DefaultListCellRenderer implements ListSelectionListener {

    /**
     * Return component for rendering list element
     */
    public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      Report report = (Report)value;
      setText(report.getName());
      if (report.usesStandardOut()) {
        setIcon(imgShell);
      } else {
        setIcon(imgGui);
      }
      return this;
    }
    
    /**
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {
      // update info
      if (listOfReports.getSelectedIndices().length!=1) {
        lAuthor .setText("");
        lVersion.setText("");
        tpInfo  .setText("");
      } else {
        Report report = (Report)listOfReports.getSelectedValue();
        lAuthor .setText(report.getAuthor());
        lVersion.setText(report.getVersion());
        tpInfo  .setText(report.getInfo());
        tpInfo.setCaretPosition(0);
      }
    }

  } //ListGlue

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
