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

import genj.Version;
import genj.gedcom.Gedcom;
import genj.io.GedcomReader;
import genj.option.OptionProvider;
import genj.util.ActionDelegate;
import genj.util.EnvironmentChecker;
import genj.util.Origin;
import genj.util.Registry;
import genj.util.Trackable;
import genj.util.swing.ProgressWidget;
import genj.view.ViewManager;
import genj.window.DefaultWindowManager;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.UIManager;

/**
 * THE GenJ Applet
 */
public class Applet extends java.applet.Applet {

  private final static Logger LOG = Logger.getLogger("genj");
  
  /** views we offer */
  static final private String[] FACTORIES = new String[]{
    "genj.table.TableViewFactory",
    "genj.tree.TreeViewFactory",
    "genj.timeline.TimelineViewFactory",
    "genj.edit.EditViewFactory",
    "genj.nav.NavigatorViewFactory",
    "genj.entity.EntityViewFactory", 
    "genj.search.SearchViewFactory" 
  };
  
  private final static String[] OPTIONPROVIDERS = {
    "genj.gedcom.Options",
    "genj.renderer.Options"
  };

  /** whether we're initialized */
  private boolean isInitialized = false;

  /**
   * @see java.applet.Applet#getAppletInfo()
   */
  public String getAppletInfo() {
    return "GenealogyJ "+Version.getInstance().getBuildString();
  }

  /**
   * @see java.applet.Applet#init()
   */
  public void init() {
    
    // work to do?
    if (isInitialized)
      return;
    isInitialized = true;

    // disclaimer
    LOG.info(getAppletInfo());

    EnvironmentChecker.log();

    // set our layout
    setLayout(new BorderLayout());

    // calculate gedcom url
    String url = getParameter("gedcom");
    if (url!=null&&url.indexOf(':')<0) {
      String base = getDocumentBase().toString();
      url = base.substring(0, base.lastIndexOf('/')+1)+url;
    } 

    // Log
    String msg = "Loading Gedcom "+url;

    showStatus(msg);
    
    LOG.info(msg);

    // try load gedcom
    new Init(url).trigger();

    // done 
  }
    
  /**
   * load
   */
  private class Init extends ActionDelegate implements Trackable {

    /** url we're loading from */
    private String url;

    /** reader we're working with */
    private GedcomReader reader;

    /** gedcom we we load */
    private Gedcom gedcom;
    
    /** registry we work on */
    private Registry registry;
    
    /** throwable we might encounter */
    private Throwable throwable;

    /**
     * Constructor
     */
    private Init(String url) {

      // keep url
      this.url = url;
      
      // setup async
      setAsync(ASYNC_SAME_INSTANCE);

      // done for now
    }
    
    /**
     * @see genj.util.ActionDelegate#preExecute()
     */
    protected boolean preExecute() {

      // clear possible throwable
      throwable = null;

      // setup progress indicator
      ProgressWidget progress = new ProgressWidget(this, getThread());
      progress.setBackground(getBackground());
      
      removeAll();
      add(BorderLayout.NORTH , new JLabel(getAppletInfo()));
      add(BorderLayout.CENTER, progress);
      
      // continue
      return true;
    }
    
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {

      // read 
      try {
        
        // the origin we're loading from
        Origin origin = Origin.create(new URL(url));
        
        // the registry and some options
        try {
          registry = new Registry(origin.open("genj.properties"));
          
          // we can't use our OptionProvider service lookup method in an applet
          // because of security restrictions (no sun.misc access)
          OptionProvider.setOptionProviders(OPTIONPROVIDERS);
          OptionProvider.restoreAll(registry);
          
        } catch (Throwable t) {
          registry = new Registry();
        }
        
        // setup system look'n'feel
        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable t) {
        }
        
        // the gedcom file
        reader = new GedcomReader(origin);
        reader.setPassword(Gedcom.PASSWORD_UNKNOWN);
         
        gedcom = reader.read();
        Thread.sleep(100);
        
      } catch (Throwable t) {
        throwable = t;
        LOG.log(Level.SEVERE, "Encountered throwable", throwable);
      }

      // back to sync   
    }
    
    /**
     * @see genj.util.ActionDelegate#postExecute()
     */
    protected void postExecute() {

      // prepare window manager
      WindowManager winMgr = new DefaultWindowManager(registry, Gedcom.getImage());
      
      // check load status      
      if (throwable!=null) {
        
        String[] actions = { "Retry",  WindowManager.TXT_CANCEL };
        int rc = winMgr.openDialog(null, "Error", WindowManager.ERROR_MESSAGE, url+"\n"+throwable.getMessage(), actions, Applet.this);        
        
        if (rc==0) trigger();
        
      } else {
        
        // prepare view manager
        ViewManager vmanager = new ViewManager(new Registry(registry, "views"), null, winMgr, FACTORIES);

        // change what we show
        removeAll();
        add(BorderLayout.CENTER, new ControlCenter(vmanager, gedcom));
        invalidate();
        validate();
        repaint();
    
      }

      // done
    }

    /**
     * @see genj.util.Trackable#cancel()
     */
    public void cancel() {
      if (reader!=null) reader.cancel();
    }
    
    /**
     * @see genj.util.Trackable#getProgress()
     */
    public int getProgress() {
      return reader!=null ? reader.getProgress() : 0;
    }

    /**
     * @see genj.util.Trackable#getState()
     */
    public String getState() {
      return reader!=null ? reader.getState() : "Connecting";
    }

    
  } //load
  
} //Applet
