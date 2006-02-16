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
import genj.option.OptionsWidget;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImageIcon;
import genj.view.Context;
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
import java.util.Collections;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

/**
 * Component for running reports on genealogic data
 */
public class ReportView extends JPanel implements ToolBarSupport {

  /*package*/ static Logger LOG = Logger.getLogger("genj.report");

  /** time between flush of output writer to output text area */
  private final static long FLUSH_WAIT = 200;
    private final static String EOL= System.getProperty("line.separator");
    private String theText="";
    private final static int MAX_HTML_SIZE=300000;
  /** statics */
  private final static ImageIcon 
    imgStart = new ImageIcon(ReportView.class,"Start.gif"      ), 
    imgStop  = new ImageIcon(ReportView.class,"Stop.gif"       ),
    imgSave  = new ImageIcon(ReportView.class,"Save.gif"       ),
    imgReload= new ImageIcon(ReportView.class,"Reload.gif"     );


  /** gedcom this view is for */
  private Gedcom      gedcom;
  
  /** components to show report info */
  private JLabel      lFile,lAuthor,lVersion;
  private JTextPane   tpInfo;
  private JEditorPane   taOutput;
  private JList       listOfReports;
  private JTabbedPane tabbedPane;
  private AbstractButton bStart,bStop,bClose,bSave,bReload;
  private OptionsWidget owOptions;
  
  /** registry for settings */
  private Registry registry;
  
  /** resources */
  /*package*/ static final Resources RESOURCES = Resources.get(ReportView.class);
  
  /** manager */
  private ViewManager manager ;

  /** title of this view */  
  private String title;

  /**
   * Constructor
   */
  public ReportView(String theTitle, Gedcom theGedcom, Registry theRegistry, ViewManager theManager) {

    // data
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
    tabbedPane.add(RESOURCES.getString("report.reports"),createReportList(callback));
    tabbedPane.add(RESOURCES.getString("report.options"), createReportOptions());
    tabbedPane.add(RESOURCES.getString("report.output"),createReportOutput(callback));
    
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
    spList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    gh.add(spList,1,0,1,5,GridBagHelper.GROWFILL_VERTICAL);

    // ... Report's filename
    gh.setParameter(GridBagHelper.FILL_HORIZONTAL);
    
    lFile = new JLabel("");
    lFile.setForeground(Color.black);
    
    gh.add(new JLabel(RESOURCES.getString("report.file")),2,0);
    gh.add(lFile,3,0,1,1,GridBagHelper.GROWFILL_HORIZONTAL);

    // ... Report's author
    
    lAuthor = new JLabel("");
    lAuthor.setForeground(Color.black);

    gh.add(new JLabel(RESOURCES.getString("report.author")),2,1);
    gh.add(lAuthor,3,1,1,1,GridBagHelper.GROWFILL_HORIZONTAL);

    // ... Report's version
    lVersion = new JLabel();
    lVersion.setForeground(Color.black);

    gh.add(new JLabel(RESOURCES.getString("report.version")),2,2);
    gh.add(lVersion,3,2);

    // ... Report's infos
    tpInfo = new JTextPane();
    tpInfo.setEditable(false);
    tpInfo.setEditorKit(new HTMLEditorKit());
    tpInfo.setFont(new JTextField().getFont()); //don't use standard clunky text area font
    JScrollPane spInfo = new JScrollPane(tpInfo);
    gh.add(new JLabel(RESOURCES.getString("report.info")),2,3);
    gh.add(spInfo,2,4,2,1,GridBagHelper.FILL_BOTH);

    // done
    return reportPanel;
    
  }

  /**
   * Create the tab content for report output
   * Output tab is a JEditorPane that displays either plain text or html text:
   * - If the output starts with <HTML> then the report is considered as 
   *   html document
   * - If the output starts with <html> then the report is considered as 
   *   html document and all newlins charaters (\n) are replaced with <br>. 
   *   This should be used to create near plaint text report but with some
   *   text decoration capability (bold, ...)
   * - otherwise (default) the report is considered as plain text to keep
   *   backward compatibility with all other reports.
   *   
   *   NM 20051031 as discussed I didn't really want another way for
   *   reports to generate formatted output - I fear that report writers
   *   will spread html throughout their code for a quick visual fix even
   *   though the real answer for formatted output is fo.Document
   *   Leaving this in for now and keeping an eye on this :) Fixed an
   *   import warning though.
   */  
  private JComponent createReportOutput(Callback callback) {
    
    // Panel for Report Output
    taOutput = new JEditorPane();
    taOutput.setEditorKit(new HTMLEditorKit());
    taOutput.setContentType("text/plain");
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
    owOptions = new OptionsWidget(manager.getWindowManager());
    return owOptions;
  }

  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(480,320);
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
    ButtonHelper bh = new ButtonHelper().setContainer(bar);

    ActionStart astart = new ActionStart();
    bStart = bh.create(astart);
    bStop  = bh.create(new ActionStop(astart));    
    bSave  = bh.create(new ActionSave());
    bReload= bh.create(new ActionReload());
   
    // done 
  }

  /**
   * Action: RELOAD
   */
  private class ActionReload extends Action2 {
    protected ActionReload() {
      setImage(imgReload);
      setTip(RESOURCES, "report.reload.tip");
      setEnabled(!ReportLoader.getInstance().isReportsInClasspath());
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
  private class ActionStop extends Action2 {
    private ActionStart start;
    protected ActionStop(ActionStart start) {
      setImage(imgStop);
      setTip(RESOURCES, "report.stop.tip");
      setEnabled(false);
      this.start=start;
    }
    protected void execute() {
      start.cancel(false);
    }
  } //ActionStop

  /**
   * Action: START
   */
  private class ActionStart extends Action2 {
    
    /** context to run on */
    private Object context, preset;
    
    /** the running report */
    private Report instance;
    
    /** an output writer */
    private PrintWriter out;
    
    /** constructor */
    protected ActionStart() {
      this(null);
    }
    
    /** constructor */
    protected ActionStart(Object preset) {
      // remember preset context
      this.preset = preset;
      // setup async
      setAsync(ASYNC_SAME_INSTANCE);
      // show
      setImage(imgStart);
      setTip(RESOURCES, "report.start.tip");
    }
    
    /**
     * pre execute
     */
    protected boolean preExecute() {
      
      // .. change buttons
      setRunning(true);
      
      // Calc Report
      Report report = (Report)listOfReports.getSelectedValue();
      if (report==null) 
        return false;
      
      out = new PrintWriter(new OutputWriter());
      theText = "";
      
      // create our own private instance  
      instance = report.getInstance(manager, ReportView.this, out);
      
      // either use preset context, gedcom file or ask for entity
      context = preset;
      if (context==null) {
        if (instance.getInputTypes().contains(Gedcom.class))
          context = gedcom;
        else  for (int i=0;i<Gedcom.ENTITIES.length;i++) { 
          String tag = Gedcom.ENTITIES[i];
          if (instance.getInputTypes().contains(Gedcom.getEntityType(tag))) {
            context = instance.getEntityFromUser(Gedcom.getName(tag), gedcom, tag);
            if (context==null) return false;
            break;
          }
        }        
      }

      // check if appropriate
      if (context==null||report.accepts(context)==null) {
        manager.getWindowManager().openDialog(null,report.getName(),WindowManager.ERROR_MESSAGE,RESOURCES.getString("report.noaccept"),Action2.okOnly(),ReportView.this);
        return false;
      }
      
      // commit options
      owOptions.stopEditing();

      // clear the current output
      taOutput.setContentType("text/plain");
      taOutput.setText("");
      
      // start transaction
      if (!report.isReadOnly()) try {
        gedcom.startTransaction();
      } catch (IllegalStateException e) {
        return false; 
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
    protected void postExecute(boolean preExecuteResult) {
      // tx to end?
      if (preExecuteResult&&!instance.isReadOnly()) 
        gedcom.endTransaction();
      // flush
      out.flush();
      out.close();
      // stop run
      setRunning(false);
    }
  } //ActionStart
  
  /**
   * Action: SAVE
   */
  private class ActionSave extends Action2 {
    protected ActionSave() {
      setImage(imgSave);
      setTip(RESOURCES, "report.save.tip");
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
        int rc = manager.getWindowManager().openDialog(null, title, WindowManager.WARNING_MESSAGE, "File exists. Overwrite?", Action2.yesNo(), ReportView.this);
        if (rc!=0) {
          return;
        }
      }
  
      // .. open file
      final FileWriter writer;
      try {
        writer = new FileWriter(file);
      } catch (IOException ex) {
        manager.getWindowManager().openDialog(null,title,WindowManager.ERROR_MESSAGE,"Error while saving to\n"+file.getAbsolutePath(),Action2.okOnly(),ReportView.this);
        return;
      }
  
      // .. save data
      try {
  
        BufferedWriter out = new BufferedWriter(writer);
/*        String data = taOutput.getText();
        out.write(data,0,data.length());
*/
        out.write(theText,0,theText.length());
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
      defRenderer.setIcon(report.getImage());
      return defRenderer;
    }
    
    /**
     * Monitor changes to selection of reports
     */
    public void valueChanged(ListSelectionEvent e) {
      // update info
      if (listOfReports.getSelectedIndices().length!=1) {
        lFile    .setText("");
        lAuthor  .setText("");
        lVersion .setText("");
        tpInfo   .setText("");
        owOptions.setOptions(Collections.EMPTY_LIST);
      } else {
        Report report = (Report)listOfReports.getSelectedValue();
        lFile    .setText(report.getFilename());
        lAuthor  .setText(report.getAuthor());
        lVersion .setText(report.getVersion());
        tpInfo   .setText(report.getInfo().replaceAll("\n", "<br>"));
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
        
        // find ' ' to the left
        for (int i=0;;i++) {
          // stop looking after 10
          if (i==10)
            return null;
          // check for starting line or non digit/character
          if (pos==0 || !Character.isLetterOrDigit(doc.getText(pos-1, 1).charAt(0)) ) 
            break;
          // continue
          pos--;
        }
        
        // find ' ' to the right
        int len = 0;
        while (true) {
          // stop looking after 10
          if (len==10)
            return null;
          // stop at end of doc
          if (pos+len==doc.getLength())
            break;
          // or non digit/character          
          if (!Character.isLetterOrDigit(doc.getText(pos+len, 1).charAt(0)))
            break;
          // continue
          len++;
        }
        
        // check if it's an ID
        if (len<2)
          return null;
        String id = doc.getText(pos, len);
        if (gedcom.getEntity(id)==null)
          return null;
        
        // mark it
        taOutput.requestFocusInWindow();
        taOutput.setCaretPosition(pos);
        taOutput.moveCaretPosition(pos+len);
  
        // return in betwee
        return id;
          
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
      // propagate to other views through manager
      Entity entity = gedcom.getEntity(id);
      if (entity!=null)
        manager.fireContextSelected(new Context(entity), e.getClickCount()>1, null);
      
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
      
      // did we buffer html?
      if (taOutput.getContentType().equals("text/html")) {
        // replace end-line-designators and dump it
        String txt = buffer.toString();
	theText = txt;
	//        txt  =txt.replaceAll(EOL, "<br>"+EOL);
	if (txt.length() > MAX_HTML_SIZE){
	    txt = txt.substring(0,MAX_HTML_SIZE);
	}
        taOutput.setText(txt);
      }
      
      // clear buffer
      buffer.setLength(0);
    }
 
    /**
     * @see java.io.Writer#flush()
     */
    public void flush() {
      
      // something to flush?
      if (buffer.length()==0)
        return;
      
      // make sure we see output pane
      tabbedPane.getModel().setSelectedIndex(2);
      
      // first flush we check for a html marker
      if (lastFlush==-1) {
        
        // html?
        if (buffer.length()>=6&&buffer.substring(0,6).equalsIgnoreCase("<html>"))
          taOutput.setContentType("text/html");
        else
          taOutput.setContentType("text/plain");

      }
      
      // mark
      lastFlush = System.currentTimeMillis();
      
      // dump partial plain text?
      if (taOutput.getContentType().equals("text/plain")) {
      
        // grab text, reset buffer and dump it 
        String txt = buffer.toString();
        buffer.setLength(0);
        Document doc = taOutput.getDocument();
        try {
          doc.insertString(doc.getLength(), txt, null);
        } catch (Throwable t) {
        }
	theText += txt;
        
      }
      
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
