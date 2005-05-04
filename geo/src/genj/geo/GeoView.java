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
package genj.geo;

import genj.gedcom.Gedcom;
import genj.gedcom.PropertyPlace;
import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.Registry;
import genj.util.swing.PopupWidget;
import genj.view.Context;
import genj.view.ContextListener;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;

/**
 * The view showing gedcom data in geographic context
 */
public class GeoView extends JPanel implements ContextListener, ToolBarSupport {
  
  private ViewManager viewManager;
  
  private GeoMap currentMap;
  
  private LayerViewPanel layerPanel;
  
  private Runnable rezoom = new Runnable() {
    public void run() {
      if (layerPanel!=null) try {
            layerPanel.getViewport().zoomToFullExtent();
          } catch (Throwable t) {
            Debug.log(Debug.WARNING, this, t.getMessage());
          }
      }
    };
  
  /**
   * Constructor
   */
  public GeoView(String title, Gedcom gedcom, Registry registry, ViewManager mgr) {
    
    super(new BorderLayout());
    
    // state to remember
    viewManager = mgr;
    
    
    // listen
    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        rezoom.run();
      }
    });
  }
  
  /**
   * Lifecycle callback - we're needed
   */
  public void addNotify() {
    
    // continue with super's
    super.addNotify();

//    // trigger a current map
//    if (currentMap==null) {
//      // set map
//      GeoMap[] maps = GeoService.getInstance().getMaps();
//      if (maps.length>0) new ChooseMap(maps[0]).trigger();
//    }
  
    // done
  }
  
  /**
   * Callback for context changes
   */
  public void setContext(Context context) {
  }
  
  /**
   * Callback for populating toolbar 
   */
  public void populate(JToolBar bar) {
    
    // get maps
    GeoMap[] maps = GeoService.getInstance().getMaps();
    List actions = new ArrayList(maps.length);
    for (int i=0;i<maps.length;i++) 
      actions.add(new ChooseMap(maps[i]));

    // add a popup for them
    PopupWidget popup = new PopupWidget(null, PropertyPlace.IMAGE, actions);
    popup.setEnabled(!actions.isEmpty());
    bar.add(popup);
    
    // done
  }
  
  /**
   * Choose current map
   */
  public void setMap(GeoMap map) throws IOException {
    
    // remove old
    removeAll();
    
    // keep
    currentMap = map;
    
    // setup layer manager and add a layer for each feature collection
    LayerManager layerManager = new LayerManager();
    map.load(layerManager);
    
    // pack into panel
    layerPanel = new LayerViewPanel(layerManager, new ViewContext());    
    layerPanel.setBackground(map.getBackground());
    
    add(BorderLayout.CENTER, layerPanel);

    revalidate();
    repaint();
    
    SwingUtilities.invokeLater(rezoom);
    
    // done
  }
  
  /**
   * View context for layer view panel
   */
  private class ViewContext implements LayerViewPanelContext {
    public void warnUser(String warning) {
      Debug.log(Debug.WARNING, GeoView.this, warning);
    }
    public void handleThrowable(Throwable t) {
      Debug.log(Debug.WARNING, GeoView.this, t);
    }
    public void setStatusMessage(String message) {
      Debug.log(Debug.INFO, GeoView.this, message);
    }
  }

  /**
   * Action - choose a map
   */
  private class ChooseMap extends ActionDelegate {
    private GeoMap map;
    /** constructor */
    private ChooseMap(GeoMap map) {
      this.map = map;
      setText(map.getName());
    }
    /** choose current map */
    protected void execute() {
      try {
        setMap(map);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }//ChooseMap
  
} //GeoView
