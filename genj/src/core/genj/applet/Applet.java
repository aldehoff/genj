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
package genj.applet;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.ZipEntry;

import genj.Version;
import genj.tree.*;
import genj.gedcom.*;
import genj.util.*;
import awtx.*;
import genj.timeline.*;
import genj.table.*;
import genj.io.*;

/**
 * THE GenJ Applet
 */
public class Applet extends java.applet.Applet implements Runnable {

  private final static int
    VIEW_DEFAULT_WIDTH  = 640,
    VIEW_DEFAULT_HEIGHT = 480;

  private final static int
    NOT_READY = -1,
    LOADING   =  0,
    ERROR     =  1,
    READY     =  2;

  private final static String[][] _views = {
    { "Tree" ,   "images/NewTree.gif"     ,"genj.tree.TreeView"         ,"tree.1" },
    { "Table",   "images/NewTable.gif"    ,"genj.table.TableView"       ,"table.1" },
    { "Timeline","images/NewTimeline.gif" ,"genj.timeline.TimelineView" ,"timeline.1" }
  };

  private Hashtable views = new Hashtable();

  private int            state = NOT_READY;
  private Gedcom         gedcom;
  private Registry       registry;
  private GedcomReader   gedReader;
  private DetailBridge   detailBridge;

  private Resources resources;

  /**
   * A Link for a View
   */
  private class ViewLink extends Component implements MouseListener {

    // Properties
    ImgIcon img;
    String title;
    String view;
    String key;

    // Constructor
    ViewLink(String sTitle, String sImage, String sView, String sKey) {
      img = new ImgIcon(this,sImage);
      title = sTitle;
      view = sView;
      key  = sKey ;
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      addMouseListener(this);
    }

    // Mouse
    public void mouseClicked(MouseEvent e) {
      openView(title,view,key);
    }
    public void mouseEntered(MouseEvent e) {
      getAppletContext().showStatus("Open "+title+" View");
    }
    public void mouseExited(MouseEvent e) {
      getAppletContext().showStatus("");
    }
    public void mouseReleased(MouseEvent e) {
      render(getGraphics(),false);
    }
    public void mousePressed(MouseEvent e) {
      render(getGraphics(),true);
    }

    // Painting
    public void paint(Graphics g) {
      render(g,false);
    }
    private void render(Graphics g, boolean pressed) {
      // Parms
      Dimension size = getSize();
      int w = img.getIconWidth(),
          h = img.getIconHeight();
      FontMetrics fm = g.getFontMetrics();
      // Image
      g.drawImage(img.getImage(),(size.width-w)/2,size.height/2-h,null);
      // Text
      if (pressed)
        g.setColor(Color.red);
      else
        g.setColor(Color.black);
      w = fm.stringWidth(title);
      int x = (size.width-w)/2;
      int y = size.height/2+fm.getHeight()-fm.getDescent();
      g.drawString(title,x,y);
      // Link underline
      g.drawLine(x,y,x+w,y);
      // Done
    }
    // EOC
  }

  /**
   * Information about this applet.
   */
  public String getAppletInfo() {
    return resources.getString("applet.info")+" - Version "+Version.getInstance();
  }

  /**
   * Initializes the applet.
   */
  public void init() {

    // Don't need no re-init
    if (state!=NOT_READY) {
      return;
    }
    
    // Read the resources
    // 20020311 Apparently Netscape doesn't like it if we read this
    // in the static initialization block
    resources = new Resources("genj.applet");
    
    // Report some information
    Debug.log(Debug.INFO, this, resources.getString("applet.info"));
    EnvironmentChecker.log();

    // Setup our container
    final Rootpane root = new Rootpane();
    root.setBackground(Color.white);
    root.setLayout(new BorderLayout());
    setLayout(new GridLayout(1,1));
    add(root);

    // Prepare Scala
    final Scala progress = new Scala();
    progress.setEditable(false);
    progress.setValue(0F);
    root.add(progress,"South");

    // Load Gedcom-Data
    state = LOADING;
    Thread t = new Thread(this);
    t.start();

    Runnable tracker = new Runnable() {
      public void run() {
        // Track the loading
        while (state==LOADING) { try {
          Thread.sleep(100);
          if (gedReader!=null) {
            progress.setValue((float)gedReader.getProgress()/100);
            progress.setPrefix(gedReader.getState());
          }
        } catch (Exception e) {
        }}
        // Error?
        if (state!=READY) {
          return;
        }
        // Update display
        root.removeAll();
        root.setLayout(new GridLayout(1,1));
        for (int i=0;i<_views.length;i++) {
          ViewLink link = new ViewLink(
            _views[i][0],
            _views[i][1],
            _views[i][2],
            _views[i][3]
          );
          root.add(link);
        }
        root.validate();
        root.repaint();
        // Done
      }
    };
    t = new Thread(tracker);
    t.start();

    // Done
  }

  /**
   * Opens a specified view
   */
  private void openView(String title,String view,String key) {

    // Already there?
    if (views.containsKey(key)) {
      Frame frame = (Frame)views.get(key);
      frame.show();
      frame.toFront();
      return;
    }

    // Resolve class for viewing component
    Class c;
    try {
      c = Class.forName(view);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Couldn't find viewing class "+view);
    }

    Class ptypes[] = new Class[]{
      Gedcom.class,
      Registry.class,
      Frame.class
    };

    java.lang.reflect.Constructor constructor;
    try {
      constructor = c.getConstructor(ptypes);
    } catch (Exception e) {
      throw new IllegalArgumentException("Couldn't find constructor for viewing class "+view);
    }

    // Create viewing component
    final Frame frame = new Frame(title);

    WindowAdapter wadapter = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        frame.setVisible(false);
      }
    };
    frame.addWindowListener(wadapter);

    Component component;
    try {
      Object params[] = new Object[]{
        gedcom,
        new Registry(registry,key),
        frame
      };

      component = (Component)constructor.newInstance(params);

      frame.add( new Rootpane(component) );
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

    // Hook up to DetailBridge?
    connectDetailBridge(component);

    // Done
    views.put(key,frame);

    frame.setSize(new Dimension(VIEW_DEFAULT_WIDTH, VIEW_DEFAULT_HEIGHT));
    frame.show();

  }

  /**
   * Helper that connects the(?) DetailBridge with given component
   * In case the component is a awtx.ScrollPane then a button is added to
   * the lower right corner
   */
  private void connectDetailBridge(Component component) {

    // something to do?
    if (detailBridge==null) {
      return;
    }

    // Something we can work with?
    if (!(component instanceof Scrollpane)) {
      return;
    }

    // Let's add a button for the bridge activation
    Scrollpane pane = (Scrollpane)component;

    pane.add2Edge(ComponentProvider.createButton(null, resources.getString("applet.browse_details"), null, "DETAIL", detailBridge));

    // Done
  }

  /**
   * Our background loading thread
   */
  public void run() {

    String error =null;
    Origin origin=null;

    // 20020307 The new parameters are only GEDCOM and DETAIL
    // but we accept the ZIP+GEDCOM combination, too
    String sarchive  = getParameter("ZIP"    );
    String sgedcom   = getParameter("GEDCOM" );
    String detailUrl = getParameter("DETAIL");

    if (sarchive!=null) {
      sgedcom = sarchive+"#"+sgedcom;
    }

    try {

      // .. Connect to Origin
      origin = Origin.create(new URL(getDocumentBase(),sgedcom ));
      System.out.println(resources.getString("applet.loading",origin.toString()));
      Origin.Connection connection = connection = origin.open();

      // .. read gedcom from it
      gedReader = new GedcomReader(
        connection.getInputStream(),
        origin,
        connection.getLength()
      );
      gedcom=gedReader.readGedcom();

      // .. hopefully done
      System.out.println(resources.getString("applet.ready"));

    } catch (MalformedURLException ex) {
      error=resources.getString("applet.malformed_url");
      ex.printStackTrace();
    } catch (GedcomFormatException ex) {
      error=resources.getString("applet.illegal_format")+" ("+ex.getLine()+"/"+ex.getMessage()+")";
      ex.printStackTrace();
    } catch (Exception ex) {
      error=resources.getString("applet.read_error");
      ex.printStackTrace();
    } catch (Error er) {
      er.printStackTrace();
    }

    // Error?
    if (error!=null) {
      getAppletContext().showStatus(error);
      state=ERROR;
      return;
    }

    // Load Registry
    try {

      Origin.Connection c = origin.openFile(origin.getFileName()+".properties");
      registry = new Registry(c.getInputStream());

      // .. hopefully done
      System.out.println(resources.getString("applet.ready"));

    } catch (IOException ex) {
      ex.printStackTrace();
      registry = new Registry();
    }

    // Hookup our DetailBridge
    if (detailUrl!=null) {
      System.out.println("Will show details for entities from "+detailUrl);
      detailBridge = new DetailBridge(this,detailUrl,gedcom);
    }

    // Done
    state=READY;
  }
}
