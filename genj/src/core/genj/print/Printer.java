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
package genj.print;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.text.*;
import java.awt.event.*;
import java.util.*;

import genj.util.*;
import genj.util.swing.*;
import genj.io.*;

/**
 * Type that supplies Printing functionality
 */
public class Printer implements Trackable {

  /**
   * Components
   */
  private JTextField tMarginLeft,tMarginRight,tMarginTop,tMarginBottom,tHeaderLeft,tHeaderCenter,tHeaderRight;
  private JRadioButton rHeaderNone,rHeaderTop,rHeaderBottom;
  private Vector componentsForHeader = new Vector();
  private ActionListener macroListener;

  /**
   * Globals
   */
  private DecimalFormat   format;
  private PrintProperties props;
  private Frame           frame;
  private PrintJob        pjob;
  private boolean         success;
  private PrintRenderer   renderer;
  private Preview         preview;
  private int             page,pages;
  private boolean         cancel;
  private Resources       resources = new Resources("genj.print");

  /**
   * Subclass which does the preview
   */
  private class Preview extends JPanel {

    /**
     * Rendering
     */
    public void paint(Graphics g) {

      // Prepare font
      g.setFont(new Font("Arial",Font.BOLD,24));
      FontMetrics fm = g.getFontMetrics();

      // Clear bg
      g.setColor(Color.lightGray);
      g.fillRect(0,0,0xffff,0xffff);

      // Calculate how many pages it takes
      Dimension dimPage     = props.getPixelSize(PrintProperties.INNERPAGE);
      Dimension dimRenderer = renderer.getSize();

      int pagesx = (int) Math.ceil( ((float)dimRenderer.width )/ dimPage.width );
      int pagesy = (int) Math.ceil( ((float)dimRenderer.height)/ dimPage.height);

      // Draw Page Indications
      Dimension dimPreview = getSize();
      float xratio = ((float)dimPreview.width -8)/(dimPage.width *pagesx);
      float yratio = ((float)dimPreview.height-8)/(dimPage.height*pagesy);
      xratio = yratio = Math.min(xratio,yratio);

      float widthPage = xratio*dimPage.width  ;
      float heightPage= yratio*dimPage.height ;

      Point offset  = new Point(
        (int)( dimPreview.width /2 -  widthPage*pagesx/2 ),
        (int)( dimPreview.height/2 - heightPage*pagesy/2 )
      );
      g.translate(offset.x,offset.y);

      for (int x=0;x<pagesx;x++) {
        for (int y=0;y<pagesy;y++) {
          // Calc parms
          int xpos = (int)(x * widthPage );
          int ypos = (int)(y * heightPage);
          int wdth = (int)(widthPage     );
          int hght = (int)(heightPage    );
          // Clear bg of page
          g.setColor(Color.white);
          g.fillRect(xpos,ypos,wdth-1,hght-1);
          // Draw border
          g.setColor(Color.black);
          g.drawRect(xpos,ypos,wdth,hght);
          // Draw number
          g.setColor(Color.lightGray);
          String s = ""+((x*pagesy)+y+1);
          g.drawString(s, xpos + wdth/2 - fm.stringWidth(s)/2, ypos + hght/2 + fm.getHeight()/2 - fm.getDescent());
          // Next
        }
      }

      // Draw Renderer's Preview
      renderer.renderPreview(
        g,
        new Dimension(
          (int)(pagesx*widthPage ),
          (int)(pagesy*heightPage)
        ),
        xratio
      );

      // Done
    }

    // EOC
  }

  /**
   * Constructor
   */
  private Printer(Frame frame, PrintRenderer renderer, PrintProperties props) {

    // Remember
    this.frame   = frame;
    this.props   = props;
    this.renderer= renderer;
    format = new DecimalFormat();
    format.setMinimumFractionDigits(2);

    // Get PrintJob
    Properties ps = new Properties();
  //    ps.put("awt.print.destination", "file"    );  // printer | file
    ps.put("awt.print.orientation", "portrait");   // portrait | landscape
    ps.put("awt.print.fileName"   , "tree.ps" );
    ps.put("awt.print.paperSize"  , "a4"      );    // letter | legal | executive | a4
    ps.put("awt.print.numCopies"  , "1"       );

    pjob = Toolkit.getDefaultToolkit().getPrintJob(frame, "GenJ", ps);

    // .. o.k. ?
    if (pjob==null) {
      return;
    }

    // Tell properties about printjob
    props.setPrintJob(pjob);

    // Prepare Dialog
    JDialog dlg = new JDialog(frame, props.getTitle(), true);

    // ... panels for page settings and preview
    ChangeListener clistener = new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        getUserInput();
      }
    };
    JTabbedPane pTabs = new JTabbedPane();
    pTabs.addChangeListener(clistener);
    pTabs.add(resources.getString("setup.page"),getPanelForPageSetup());
    pTabs.add(resources.getString("setup.preview"),getPanelForPreview()  );

    // .. panel for actions
    JPanel pActions = getPanelForActions(dlg);

    // ... show it
    dlg.getContentPane().setLayout(new BorderLayout());
    dlg.getContentPane().add(pTabs,"Center");
    dlg.getContentPane().add(pActions,"South");
    dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dlg.pack();
    dlg.show();

    // Printing wanted ?
    if (!success) {
      return;
    }

    // Start a Thread for printing
    Runnable run = new Runnable() {
      public void run() {
        printPages();
      }
    };
    Thread t = new Thread(run);
    t.start();

    // Show progress
    ProgressDialog progress = new ProgressDialog(
      frame,
      resources.getString("progress.title"),
      props.getTitle(),
      this,
      t
    );

    // Done
  }

  /**
   * Cancels operation
   */
  public void cancel() {
    cancel=true;
  }

  /**
   * Helper that creates a JButton for MacroSelection
   */
  private JButton createMacroButton(String macro, String tip){

    // Macro listener ?
    if (macroListener==null) {
      macroListener = new ActionListener() {
        // LCD
        public void actionPerformed(ActionEvent e) {
          // Look though components for header setup
          Enumeration enum = componentsForHeader.elements();
          while (enum.hasMoreElements()) {
            JComponent c = (JComponent)enum.nextElement();
            if ((c instanceof JTextField)&&(c.hasFocus())) {
              ((JTextField)c).replaceSelection(e.getActionCommand());
              break;
            }
          }
          // Done
        }
        // EOC
      };
    }

    // Create button
    JButton b = new JButton(macro);
    b.setToolTipText(tip);
    b.setMargin(new Insets(0,0,0,0));
    b.setRequestFocusEnabled(false);
    b.addActionListener(macroListener);
    componentsForHeader.addElement(b);

    // Done
    return b;
  }

  /**
   * Helper that gets margin values from user input
   */
  private void getMarginInput() {
    // Margins to look for
    JTextField[] tfields = { tMarginLeft, tMarginRight, tMarginTop, tMarginBottom };
    int[]        margins = { props.LEFTMARGIN, props.RIGHTMARGIN, props.TOPMARGIN, props.BOTTOMMARGIN };

    // .. Go through settings for margins
    for (int i=0;i<tfields.length;i++) {

      try {

        // Calculate string value
        String string = tfields[i].getText();
        int pos;
        if ((pos=string.indexOf("cm"))>0) {
          string = string.substring(0,pos);
        }

        // Build float
        float value = format.parse(string).floatValue();

        // Change properties
        props.setValue(margins[i],value);
      } catch (ParseException pe) {
      }

      // Next margin
    }

    // .. reset margins
    tMarginLeft  .setText(format.format(props.getValue(props.LEFTMARGIN  ))+" cm ");
    tMarginRight .setText(format.format(props.getValue(props.RIGHTMARGIN ))+" cm ");
    tMarginTop   .setText(format.format(props.getValue(props.TOPMARGIN   ))+" cm ");
    tMarginBottom.setText(format.format(props.getValue(props.BOTTOMMARGIN))+" cm ");

    // Done
  }

  /**
   * Helper that creates a panel for action buttons
   */
  private JPanel getPanelForActions(final JDialog dlg) {

    // Create components
    final JButton bOK = new JButton(""+UIManager.get("OptionPane.okButtonText"));
    final JButton bCancel = new JButton(""+UIManager.get("OptionPane.cancelButtonText"));

    JPanel result = new JPanel();
    result.add(bOK);
    result.add(bCancel);

    // Listener
    ActionListener alistener = new ActionListener() {
      // LCD
      public void actionPerformed(ActionEvent e) {
        if (e.getSource()==bOK)
          success=true;
        dlg.dispose();
      }
      // EOC
    };
    bOK.addActionListener(alistener);
    bCancel.addActionListener(alistener);

    // Done
    return result;
  }

  /**
   * Helper that creates a panel for editing header information
   */
  protected JPanel getPanelForHeaderSetup(boolean enabled) {

    // Create visual parts
    JPanel result = new JPanel();
    result.setBorder(BorderFactory.createTitledBorder("Header"));

    GridBagHelper helper = new GridBagHelper(result);

    helper.add(new JLabel(resources.getString("header.left"  )), 1, 2, 1, 1, 0);
    helper.add(new JLabel(resources.getString("header.center")), 1, 3, 1, 1, 0);
    helper.add(new JLabel(resources.getString("header.right" )), 1, 4, 1, 1, 0);

    rHeaderNone   = new JRadioButton(resources.getString("header.none"  ),false);
    rHeaderTop    = new JRadioButton(resources.getString("header.top"   ),true );
    rHeaderBottom = new JRadioButton(resources.getString("header.bottom"),false);

    ButtonGroup grp = new ButtonGroup();
    grp.add(rHeaderNone   );
    grp.add(rHeaderTop    );
    grp.add(rHeaderBottom );

    componentsForHeader.addElement(tHeaderLeft   = new JTextField("<d>"));
    componentsForHeader.addElement(tHeaderCenter = new JTextField("<n> - " + props.getTitle() ));
    componentsForHeader.addElement(tHeaderRight  = new JTextField("<p>/<pp>"));

    JPanel pMacros = new JPanel();
    pMacros.add(createMacroButton("<n>" ,resources.getString("tag.filename")  ));
    pMacros.add(createMacroButton("<u>" ,resources.getString("tag.url"     )  ));
    pMacros.add(createMacroButton("<t>" ,resources.getString("tag.time"    )  ));
    pMacros.add(createMacroButton("<d>" ,resources.getString("tag.date"    )  ));
    pMacros.add(createMacroButton("<p>" ,resources.getString("tag.page"    )  ));
    pMacros.add(createMacroButton("<pp>",resources.getString("tag.total"   )  ));

    helper.add(rHeaderNone              , 1, 1, 1, 1, 0);
    helper.add(rHeaderTop               , 2, 1, 1, 1, 0);
    helper.add(rHeaderBottom            , 3, 1, 1, 1, 0);
    helper.add(tHeaderLeft              , 2, 2, 2, 1, helper.GROW_HORIZONTAL);
    helper.add(tHeaderCenter            , 2, 3, 2, 1, helper.GROW_HORIZONTAL);
    helper.add(tHeaderRight             , 2, 4, 2, 1, helper.GROW_HORIZONTAL);
    helper.add(pMacros                  , 1, 5, 3, 1, helper.GROW_BOTH      );


    // Event handling
    ChangeListener clistener = new ChangeListener() {
      // LCD
      public void stateChanged(ChangeEvent e) {
        boolean on = !rHeaderNone.isSelected();
        Enumeration enum = componentsForHeader.elements();
        while (enum.hasMoreElements()) {
          ((Component)enum.nextElement()).setEnabled(on);
        }
        // Done
      }
      // EOC
    };
    rHeaderNone  .addChangeListener(clistener);
    rHeaderTop   .addChangeListener(clistener);
    rHeaderBottom.addChangeListener(clistener);

    // Disable Header ?

    /* Not implemented yet */
    enabled=false;
    rHeaderTop   .setEnabled(false);
    rHeaderBottom.setEnabled(false);
    /* Not implemented yet */

    if (!enabled) {
      rHeaderNone.doClick();
    }

    // Done
    return result;
  }

  /**
   * Helper that creates a panel for editing margin information
   */
  protected JPanel getPanelForMarginSetup() {

    JPanel result = new JPanel();
    result.setBorder(BorderFactory.createTitledBorder(resources.getString("margin")));

    GridBagHelper helper = new GridBagHelper(result);

    helper.add(new JLabel(resources.getString("margin.left"  )), 1, 1, 1, 1, 0);
    helper.add(new JLabel(resources.getString("margin.right" )), 1, 2, 1, 1, 0);
    helper.add(new JLabel(resources.getString("margin.top"   )), 1, 3, 1, 1, 0);
    helper.add(new JLabel(resources.getString("margin.bottom")), 1, 4, 1, 1, 0);

    tMarginLeft   = new JTextField(8);
    tMarginRight  = new JTextField(8);
    tMarginTop    = new JTextField(8);
    tMarginBottom = new JTextField(8);

    helper.add(tMarginLeft              , 2, 1, 1, 1, helper.GROW_HORIZONTAL);
    helper.add(tMarginRight             , 2, 2, 1, 1, helper.GROW_HORIZONTAL);
    helper.add(tMarginTop               , 2, 3, 1, 1, helper.GROW_HORIZONTAL);
    helper.add(tMarginBottom            , 2, 4, 1, 1, helper.GROW_HORIZONTAL);
    helper.add(new JLabel()             , 5, 5, 1, 1, helper.GROW_BOTH      );

    // Prepare Listeners
    FocusAdapter fadapter = new FocusAdapter() {
      public void focusLost(FocusEvent e) {
      // Read from user
      getMarginInput();
      // End
      }
    };

    tMarginLeft  .addFocusListener(fadapter);
    tMarginRight .addFocusListener(fadapter);
    tMarginTop   .addFocusListener(fadapter);
    tMarginBottom.addFocusListener(fadapter);

    // Set up margin values for the first time
    getMarginInput();

    // Done
    return result;

  }

  /**
   * Helper that creates a panel for showing page information
   */
  protected JPanel getPanelForPageInfo() {

    // Create visual parts
    JPanel result = new JPanel();
    result.setBorder(BorderFactory.createTitledBorder(resources.getString("info")));

    GridBagHelper helper = new GridBagHelper(result);

    JTextField tSizeX = new JTextField(format.format(props.getValue(PrintProperties.PAGEWIDTH )     )+" cm "    );
    JTextField tWidth = new JTextField(              props.getPixelValue(PrintProperties.PAGEWIDTH)  +" pixels ");
    JTextField tSizeY = new JTextField(format.format(props.getValue(PrintProperties.PAGEHEIGHT)     )+" cm "    );
    JTextField tHeight= new JTextField(              props.getPixelValue(PrintProperties.PAGEHEIGHT) +" pixels ");
    JTextField tDPI   = new JTextField(              props.getPixelValue(PrintProperties.DPI)        +" dpi "   );

    tSizeX .setEditable(false);
    tSizeY .setEditable(false);
    tWidth .setEditable(false);
    tHeight.setEditable(false);
    tDPI   .setEditable(false);

    helper.add(new JLabel(resources.getString("info.width")+" "), 1, 1, 1, 1, 0);
    helper.add(tSizeX               , 2, 1, 1, 1, 0);
    helper.add(new JLabel(" x ")    , 3, 1, 1, 1, 0);
    helper.add(tWidth               , 4, 1, 1, 1, 0);
    helper.add(new JLabel(" / ")    , 5, 1, 1, 1, 0);
    helper.add(tDPI                 , 6, 1, 1, 1, 0);

    helper.add(new JLabel(resources.getString("info.height")+" "), 1, 2, 1, 1, 0);
    helper.add(tSizeY               , 2, 2, 1, 1, 0);
    helper.add(new JLabel(" x ")    , 3, 2, 1, 1, 0);
    helper.add(tHeight              , 4, 2, 1, 1, 0);

    helper.add(new JLabel()         , 7, 7, 1, 1, helper.GROW_BOTH);

    // Done
    return result;

  }

  /**
   * Helper that creates a panel for editing page setup information
   */
  protected JPanel getPanelForPageSetup() {

    JPanel result = new JPanel();
    GridBagHelper helper = new GridBagHelper(result);

    helper.add(getPanelForPageInfo()        , 1, 1, 2, 1, helper.GROW_HORIZONTAL);
    helper.add(getPanelForMarginSetup()     , 1, 2, 1, 1, helper.GROW_BOTH      );
    helper.add(getPanelForHeaderSetup(false), 2, 2, 1, 1, helper.GROW_BOTH      );

    return result;
  }

  /**
   * Helper that creates a panel for preview
   */
  private JPanel getPanelForPreview() {

    // Prepare result
    preview = new Preview();

    JPanel  editor  = renderer.getEditor(resources);
    editor.setBackground(Color.lightGray);

    JPanel result = new JPanel(new BorderLayout());
    result.add(editor ,"North");
    result.add(preview,"Center");

    renderer.setPrinter(this);

    // Done
    return result;
  }

  /**
   * Returns progress of operation in % (0-100)
   */
  public int getProgress() {
    return page*100/pages;
  }

  /**
   * Returns state as explanatory string
   */
  public String getState() {
    return resources.getString("progress.line", new String[]{ ""+page, ""+pages} );
  }

  /**
   * Helper that gets user input
   */
  private void getUserInput() {

    // Margins ?
    getMarginInput();

    // Header
  //    getHeaderInput();

    // Done
  }

  /**
   * Returns warnings of operation
   */
  public String getWarnings() {
    return null;
  }

  /**
   * Do the printing
   */
  public static void print(Frame frame, PrintRenderer renderer, PrintProperties props) {
    new Printer(frame,renderer,props);
  }

  /**
   * Helper that prints the pages
   */
  private void printPages() {

    // Calculate how many pages it takes
    Dimension dimPage     = props.getPixelSize(PrintProperties.INNERPAGE);
    Dimension dimRenderer = renderer.getSize();

    int pagesx = (int) Math.ceil( ((float)dimRenderer.width )/ dimPage.width );
    int pagesy = (int) Math.ceil( ((float)dimRenderer.height)/ dimPage.height);

    pages = pagesx * pagesy;
    page  = 1;

    Dimension dimPages    = new Dimension( pagesx*dimPage.width , pagesy*dimPage.height );

    // Print it
    for (int x=0;x<pagesx;x++) {
      for (int y=0;y<pagesy;y++) {
        // Create page
        Graphics g = pjob.getGraphics();
        // Clip to page dimension
        g.setClip(
          props.getPixelValue(PrintProperties.LEFTMARGIN ),
          props.getPixelValue(PrintProperties.TOPMARGIN  ),
          dimPage.width,
          dimPage.height
        );
        // Shift to correct view
        g.translate(
          - dimPage.width *x ,
          - dimPage.height*y
        );
        // Shift by margins
        g.translate(
          props.getPixelValue(PrintProperties.LEFTMARGIN),
          props.getPixelValue(PrintProperties.TOPMARGIN )
        );
        // Render page
        renderer.renderPage(g,dimPages);
        // Print page
        g.dispose();
        // Still continue ?
        if (cancel) {
          break;
        }
        // Next page
        page++;
      }
    }
    pjob.end();

    // Done
  }

  /**
   * Notification in case the renderer has changed
   */
  public void rendererChanged() {
    preview.repaint();
  }

}

