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

/**
 * Component for running reports on genealogic data
 */
public class ReportView extends JPanel implements ListSelectionListener, ActionListener {

  private Gedcom   gedcom;
  private Frame    frame;
  private JLabel      lAuthor,lVersion;
  private JTextPane   tpInfo;
  private JScrollPane spOutput;
  private JTextArea   taOutput;
  private JList       listOfReports;
  private JTabbedPane tabbedPane;
  private JButton     bStart,bStop,bClose,bSave,bReload;
  private Thread  thread;
  private boolean isInterrupted;
  private static  ReportLoader loader;
  private static  ImageIcon imgShell,imgGui;
  private Registry registry;
  private final static Resources resources = new Resources(ReportView.class);
  private Closure deferredSetRunning;

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

    imgShell = new ImageIcon(new ImgIcon(this,"ReportShell.gif").getImage());
    imgGui   = new ImageIcon(new ImgIcon(this,"ReportGui.gif"  ).getImage());

    deferredSetRunning = new Closure(this, "setRunning", false);

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
    listOfReports.addListSelectionListener(this);

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
    add(buttonPanel,"South");

    buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));
    buttonPanel.add(   bStart = newButton(resources.getString("report.start" ), "Start selected report","START"  ,true ) );
    buttonPanel.add(   bStop  = newButton(resources.getString("report.stop"  ), "Stop running report"  ,"STOP"   ,false) );
    if (frame!=null)
      buttonPanel.add( bClose = newButton(resources.getString("report.close" ), "Close dialog"         ,"CLOSE"  ,true ) );
    buttonPanel.add(   bSave  = newButton(resources.getString("report.save"  ), "Save report output"   ,"SAVE"   ,true) );
    buttonPanel.add(   bReload= newButton(resources.getString("report.reload"), "Reload report classes","RELOAD" ,true) );

    // Done
  }

  /**
   * Action: RELOAD
   */
  private void actionReload() {
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

  /**
   * Action: STOP
   */
  private void actionStop() {
    if (thread!=null) {
      isInterrupted=true;
      thread.interrupt();
    }
  }

  /**
   * Action: START
   */
  private void actionStart() {

    // Calc Report
    final Report report = (Report)listOfReports.getSelectedValue();
    if (report==null) {
      return;
    }

    // .. change buttons
    setRunning(true);
    isInterrupted = false;

    // .. switch to output
    if (report.usesStandardOut()) {
      tabbedPane.getModel().setSelectedIndex(1);
    }
    taOutput.setText("");

    // .. prepare own STDOUT
    final ReportBridge bridge = new ReportBridge(this, new Registry(registry, report.getName()));

    bridge.log("<!--");
    bridge.log("   Report : "+report.getClass().getName());
    bridge.log("   Gedcom : "+gedcom.getName());
    bridge.log("   Start  : "+new Date());
    bridge.log("-->");
    bridge.log("");
    bridge.flush();

    // .. start Thread
    thread = new Thread() {

      // LCD

      /** main Thread routine */
      public void run() {

        // Report actions are subject to interruption
        boolean rc=false;
        boolean readOnly=report.isReadOnly();
        String status;

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
        isInterrupted = false;

        // .. end output
        bridge.log("");
        bridge.log("<!--");
        bridge.log("   End    : "+new Date());
        bridge.log("   Status : "+status);
        bridge.log("-->");
        bridge.flush();

        // .. end thread officially
        thread=null;

        // .. change buttons back in main thread again
        SwingUtilities.invokeLater(deferredSetRunning);
      }

      // EOC
    };

    thread.start();

    // done
  }

  /**
   * One of the buttons has been pressed
   */
  public void actionPerformed(ActionEvent e) {

    if (e.getActionCommand()=="SAVE") {
      actionSave();
    }
    if (e.getActionCommand()=="RELOAD") {
      actionReload();
    }
    if (e.getActionCommand()=="STOP") {
      actionStop();
    }
    if (e.getActionCommand()=="START") {
      actionStart();
    }
    if (e.getActionCommand()=="CLOSE") {
      actionClose();
    }

    // Done
  }

  /**
   * Action: CLOSE
   */
  private void actionClose() {
    frame.dispose();
  }

  /**
   * Action: SAVE
   */
  private void actionSave() {

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
      if (JOptionPane.NO_OPTION==JOptionPane.showConfirmDialog(this,"File exists. Overwrite?","Save",JOptionPane.YES_NO_OPTION)) {
        return;
      }
    }

    // .. open file
    final FileWriter writer;
    try {
      writer = new FileWriter(file);
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(this,"Error while saving to\n"+file.getAbsolutePath(),"File Error",JOptionPane.ERROR_MESSAGE);
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
   * Is this report cancelled?
   */
  public boolean isInterrupted() {
    return isInterrupted;
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
	  File base;
	  if (System.getProperty("genj.report.dir")!=null) {
	    // .. in "genj.report.dir"
	    base = new File(System.getProperty("genj.report.dir"));
	  } else {
	    // .. or in "user.dir"/report
	    base = new File(System.getProperty("user.dir"),"report");
	  }
	  System.out.println("[Debug]Reading reports from "+base);
	  
	  // Create the loader
	  loader = new ReportLoader(base);
	  // Done
	}

  /**
   * Helper for easy button creation
   */
  private JButton newButton(String title, String tip,String action,boolean enabled) {
    JButton b = new JButton(title);
    b.setActionCommand(action);
    b.addActionListener(this);
    b.setToolTipText(tip);
    b.setEnabled(enabled);
    return b;
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
   * One of the reports in the list has been selected
   */
  public void valueChanged(ListSelectionEvent e) {

    // Too much selection ?
    if (e.getLastIndex()!=e.getFirstIndex()) {
      tpInfo.setText("");
    }

    // Calc Report
    selectReport((Report)listOfReports.getSelectedValue());

  }

}
