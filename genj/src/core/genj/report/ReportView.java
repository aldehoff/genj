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

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;

import genj.gedcom.*;
import genj.util.*;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImgIconConverter;

/**
 * Component for running reports on genealogic data
 */
public class ReportView extends JPanel {

  private Gedcom   gedcom;
  private Frame    frame;
  private JLabel      lAuthor,lVersion;
  private JTextPane   tpInfo;
  private JScrollPane spOutput;
  private JTextArea   taOutput;
  private JList       listOfReports;
  private JTabbedPane tabbedPane;
  private JButton     bStart,bStop,bClose,bSave,bReload;
  private static  ReportLoader loader;
  private static  ImageIcon imgShell,imgGui;
  private Registry registry;
  private final static Resources resources = new Resources(ReportView.class);

  /**
   * Report Renderer
   */
  class ReportRenderer extends JLabel implements ListCellRenderer {

    /**
     * Return component for rendering list element
     */
    public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {

      Report report = (Report)value;
      setText(report.getName());
      if (report.usesStandardOut()) {
        setIcon(imgShell);
      } else {
        setIcon(imgGui);
      }

      if (isSelected) {
        setBackground(list.getSelectionBackground());
      } else {
        setBackground(list.getBackground());
      }

      return this;
    }

    /**
      * paint is subclassed to draw the background correctly.  JLabel
      * currently does not allow backgrounds other than white, and it
      * will also fill behind the icon.  Something that isn't desirable.
      */
    public void paint(Graphics g) {
      Color            bColor;

      g.setColor(getBackground());
      g.fillRect(0 , 0, getWidth()-1, getHeight()-1);

      super.paint(g);
    }
    // EOC
  }

  /**
   * Constructor
   */
  public ReportView(Gedcom theGedcom,Registry theRegistry,Frame theFrame) {

    // Inherited
    super();

    // Data
    gedcom   = theGedcom;
    frame    = theFrame ;
    registry = theRegistry;

    imgShell = ImgIconConverter.get(new ImgIcon(this,"ReportShell.gif"));
    imgGui   = ImgIconConverter.get(new ImgIcon(this,"ReportGui.gif"  ));

    // Look for reports
    loadReports(false);

    // Layout for this component
    setLayout(new BorderLayout());

    // Noteboook in Center
    tabbedPane = new JTabbedPane();
    add(tabbedPane,"Center");

    // Panel for Report
    JPanel reportPanel = new JPanel();
    reportPanel.setBorder(new EmptyBorder(3,3,3,3));
    GridBagHelper reportGridBag = new GridBagHelper(reportPanel);
    tabbedPane.add(resources.getString("report.reports"),reportPanel);

    // ... List of reports
    Report reports[] = loader.getReports();
    listOfReports = new JList(reports);
    listOfReports.setCellRenderer(new ReportRenderer());
    listOfReports.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listOfReports.addListSelectionListener((ListSelectionListener)new ActionSelect().as(ListSelectionListener.class));

    JScrollPane spList = new JScrollPane(listOfReports);
    reportGridBag.add(spList,1,1,1,6,GridBagHelper.GROW_BOTH);

    // ... Report's author
    lAuthor = new JLabel("");
    lAuthor.setForeground(Color.black);

    reportGridBag.add(new JLabel(resources.getString("report.author")),2,1,2,1,0);
    reportGridBag.add(new JLabel(" "     ),2,2,1,1,0);
    reportGridBag.add(lAuthor             ,3,2,1,1,0);

    // ... Report's version
    lVersion = new JLabel("");
    lVersion.setForeground(Color.black);

    reportGridBag.add(new JLabel(resources.getString("report.version")),2,3,2,1,0);
    reportGridBag.add(new JLabel(" "     ),2,4,1,1,0);
    reportGridBag.add(lVersion            ,3,4,1,1,0);

    // ... Report's infos
    tpInfo = new JTextPane();
    tpInfo.setEnabled(false);
    JScrollPane spInfo = new JScrollPane(tpInfo,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
      public Dimension getPreferredSize() {
      return new Dimension(256,128);
      }
    };

    reportGridBag.add(new JLabel(resources.getString("report.info")),2,5,2,1,0                      );
    reportGridBag.add(spInfo                   ,3,6,1,1,GridBagHelper.GROW_BOTH);

    // Panel for Report Output
    taOutput = new JTextArea();
    taOutput.setEditable(false);

    spOutput = new JScrollPane(taOutput,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS) {
      public Dimension getPreferredSize() {
      return new Dimension(256,128);
      }
    };
    tabbedPane.add(resources.getString("report.output"),spOutput);

    // Buttons at bottom
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));
    add(buttonPanel,"South");
    
    ButtonHelper bh = new ButtonHelper().setResources(resources).setContainer(buttonPanel);

    ActionStart astart = new ActionStart();
    bStart = bh.create(astart);
    bStop  = bh.setEnabled(false).create(new ActionStop(astart));    
    bSave  = bh.setEnabled(true).create(new ActionSave());
    bReload= bh.create(new ActionReload());
    if (frame!=null)
      bClose = bh.create(new ActionDelegate.ActionDisposeFrame(frame).setText("report.close"));    
    
    // Done
  }

  /**
   * Adds a line of ouput
   */
  public void addOutput(String line) {
    taOutput.append(line);

    if (!spOutput.getVerticalScrollBar().getValueIsAdjusting()) {
      taOutput.setCaretPosition(taOutput.getText().length()-1);
    }
  }

  /**
   * Load Reports from Disk/Net
   */
  private void loadReports(boolean force) {
    
    // Reload isn't always necessary
    if ((force==false)&&(loader!=null)) {
      return;
    }
    
    // The reports are either 
    String dir = EnvironmentChecker.getProperty(
      this,
      new String[]{ "genj.report.dir", "user.dir/report"},
      "./report",
      "find report class-files"
    );
    File base = new File(dir);
    Debug.log(Debug.INFO, this,"Reading reports from "+base);
    
    // Create the loader
    loader = new ReportLoader(base);
    // Done
  }
  

  /**
   * Select given report
   */
  private void selectReport(Report report) {

    if (report==null) {
      lAuthor .setText("");
      lVersion.setText("");
      tpInfo  .setText("");
    } else {
      lAuthor .setText(report.getAuthor());
      lVersion.setText(report.getVersion());
      tpInfo  .setText(report.getInfo());
      tpInfo.setCaretPosition(0);
    }

    // Done
  }

  /**
   * Helper that sets buttons states
   */
  public boolean setRunning(boolean on) {

    // Show it on buttons
    bStart.setEnabled(!on);
    bStop .setEnabled(on);
    if (bClose!=null) {
      bClose.setEnabled(!on);
    }
    bReload.setEnabled(!on);

    taOutput.setCursor(Cursor.getPredefinedCursor(
      on?Cursor.WAIT_CURSOR:Cursor.DEFAULT_CURSOR
    ));

    // Done
    return true;
  }

  /**
   * Action: Select(Report)
   */
  private class ActionSelect extends ActionDelegate {
    protected void execute() {
      if (listOfReports.getSelectedIndices().length!=1) {
        selectReport(null);
      } else {
        selectReport((Report)listOfReports.getSelectedValue());
      }
    }
  }

  /**
   * Action: RELOAD
   */
  private class ActionReload extends ActionDelegate {
    protected ActionReload() {
      setText("report.reload").setTip("report.reload.tip");
    }
    protected void execute() {
      // show first page and unselect report
      tabbedPane.getModel().setSelectedIndex(0);
      selectReport(null);
      // .. do it (forced!);
      loadReports(true);
      // .. get them
      Report reports[] = loader.getReports();
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
      setText("report.stop"  ).setTip("report.stop.tip");
      this.start=start;
    }
    protected void execute() {
      Thread thread = start.getThread();
      if (thread!=null) thread.interrupt();
    }
  } //ActionStop

  /**
   * Action: START
   */
  private class ActionStart extends ActionDelegate {
    private ReportBridge bridge;
    private Report report;
    private String status;
    protected ActionStart() {
      setAsync(ASYNC_SAME_INSTANCE);
      setText("report.start" ).setTip("report.start.tip");
    }
    /**
     * pre execute
     */
    protected boolean preExecute() {
      
      // Calc Report
      report = (Report)listOfReports.getSelectedValue();
      if (report==null) {
        return false;
      }
  
      // .. change buttons
      setRunning(true);
  
      // .. switch to output
      if (report.usesStandardOut()) {
        tabbedPane.getModel().setSelectedIndex(1);
      }
      taOutput.setText("");
  
      // .. prepare own STDOUT
      bridge = new ReportBridge(ReportView.this, new Registry(registry, report.getName()));      bridge.log("<!--");
      
      bridge.log("   Report : "+report.getClass().getName());
      bridge.log("   Gedcom : "+gedcom.getName());
      bridge.log("   Start  : "+new Date());
      bridge.log("-->");
      bridge.log("");
      bridge.flush();

      // done
      return true;
    }
    /**
     * execute
     */
    protected void execute() {

      // Report actions are subject to interruption
      boolean rc=false;
      boolean readOnly=report.isReadOnly();

      // .. lock Gedcom for read and start report
      try {
        if (readOnly) {
          rc = report.start(bridge,gedcom);

        } else {
          if (!gedcom.startTransaction())
            status = "No Write Access";
          else {
            rc = report.start(bridge,gedcom);
            gedcom.endTransaction();
          }
        }
        if (rc)
          status = "O.K";
        else
          status = "Error";
      } catch (ReportCancelledException ex) {
        // Running report was stopped
        status="Cancelled";
      } catch (Exception ex) {
        // Running report failed
        bridge.println(ex);
        status="Exception";
      }

    }
    
    /**
     * post execute
     */
    protected void postExecute() {
  
      // .. end output
      bridge.log("");
      bridge.log("<!--");
      bridge.log("   End    : "+new Date());
      bridge.log("   Status : "+status);
      bridge.log("-->");
      bridge.flush();
      
      // done
      setRunning(false);

    }
  } //ActionStart
  
  /**
   * Action: SAVE
   */
  private class ActionSave extends ActionDelegate {
    protected ActionSave() {
      setText("report.save").setTip("report.save.tip");
    }
    protected void execute() {

      // .. choose file
      JFileChooser chooser = new JFileChooser(".");
      chooser.setDialogTitle("Save Output");
  
      if (JFileChooser.APPROVE_OPTION != chooser.showDialog(frame,"Save")) {
        return;
      }
      File file = chooser.getSelectedFile();
      if (file==null) {
        return;
      }
  
      // .. exits ?
      if (file.exists()) {
        if (JOptionPane.NO_OPTION==JOptionPane.showConfirmDialog(ReportView.this,"File exists. Overwrite?","Save",JOptionPane.YES_NO_OPTION)) {
          return;
        }
      }
  
      // .. open file
      final FileWriter writer;
      try {
        writer = new FileWriter(file);
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(ReportView.this,"Error while saving to\n"+file.getAbsolutePath(),"File Error",JOptionPane.ERROR_MESSAGE);
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
  
}
