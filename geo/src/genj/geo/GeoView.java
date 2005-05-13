/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2005 Nils Meier <nils@meiers.net>
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
import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.WordBuffer;
import genj.util.swing.ImageIcon;
import genj.util.swing.PopupWidget;
import genj.view.Context;
import genj.view.ContextListener;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;
import genj.window.CloseWindow;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.LabelStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.SquareVertexStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.VertexStyle;

/**
 * The view showing gedcom data in geographic context
 */
public class GeoView extends JPanel implements ContextListener, ToolBarSupport {
  
  private final static ImageIcon IMG_MAP = new ImageIcon(GeoView.class, "images/Map.png");
  
  /*package*/ final static Resources RESOURCES = Resources.get(GeoView.class);
  
  /** gedcom we're looking at */
  private Gedcom gedcom;
  
  /** handle to view manager */
  private ViewManager viewManager;
  
  /** the current map */
  private GeoMap currentMap;
  
  /** the current layer view panel */
  private LayerViewPanel layerPanel;
  
  /** our model & layer */
  private GeoModel model;
  private GedcomLayer gedcomLayer;  
  
  /** a rezoom runnable we can invokeLater() */
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
  public GeoView(String title, Gedcom gedcom, Registry registry, ViewManager viewManager) {
    
    super(new BorderLayout());
    
    // state to remember
    this.viewManager = viewManager;
    this.gedcom = gedcom;
    
    // listen
    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        rezoom.run();
      }
    });
    
    // create our model & layer
    model = new GeoModel(gedcom);
    gedcomLayer = new GedcomLayer();  
    
    // register for popups
    ToolTipManager.sharedInstance().registerComponent(this);
    
    // done
  }
  
  /**
   * tooltip callback
   */
  public String getToolTipText(MouseEvent event) {
    try {
      Coordinate coord  = layerPanel.getViewport().toModelCoordinate(event.getPoint());
      
      WordBuffer text = new WordBuffer("");
      text.append("<html><body>");
      text.append( toString(coord));
      text.setFiller("<br>");
      for (Iterator locations = layerPanel.featuresWithVertex(event.getPoint(), 5,  model.getKnownLocations()).iterator(); locations.hasNext(); )  {
        GeoLocation location = (GeoLocation)locations.next();
        text.append(location.toHTML());
      }
      
      return text.toString();
    } catch (Throwable t) {
      return null;
    }
  }
  
  private String toString(Coordinate coord) {
    double lat = coord.y, lon = coord.x;
    char we = 'E', ns = 'N';
    if (lat<0) { lat = -lat; ns='S'; }
    if (lon<0) { lon = -lon; we='W'; }
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMaximumFractionDigits(1);
    format.setMinimumFractionDigits(1);
    return ns + format.format(lat) + " " + we + format.format(lon);
  }
  
  /**
   * component lifecycle - we're needed
   */
  public void addNotify() {
    // override
    super.addNotify();
    // hook up layer to model
    model.addGeoModelListener(gedcomLayer);
  }
  
  /**
   * component lifecycle - we're not needed anymore
   */
  public void removeNotify() {
    // disconnect layer from model
    model.removeGeoModelListener(gedcomLayer);
    // override
    super.removeNotify();
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
    PopupWidget chooseMap = new PopupWidget(null, IMG_MAP, actions);
    chooseMap.setEnabled(!actions.isEmpty());
    bar.add(chooseMap);
    
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
    
    // setup layer manager and add our own feature collection that's wrapping the model
    LayerManager layerManager = new LayerManager();
    layerManager.addLayer("GenJ", gedcomLayer);
    gedcomLayer.setLayerManager(layerManager);

    // load map
    map.load(layerManager);
    
    // pack into panel
    layerPanel = new LayerViewPanel(layerManager, new ViewContext());    
    layerPanel.setBackground(map.getBackground());
    
    add(BorderLayout.CENTER, layerPanel);

    revalidate();
    repaint();
    
    SwingUtilities.invokeLater(rezoom);
    
    // test for available countries
    WordBuffer missing = new WordBuffer("\n ");
    GeoService service = GeoService.getInstance();
    List available = Arrays.asList(service.getCountries());
    Country[] required = map.getCountries();
    for (int i = 0; i < required.length; i++) {
      if (!available.contains(required[i])) 
        missing.append(required[i]);
    }
    
    if (missing.length()>0) {
      String note = RESOURCES.getString("missing", missing);
      viewManager.getWindowManager().openDialog(null, null, WindowManager.IMG_INFORMATION, note, CloseWindow.OK(), GeoView.this);
    }
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
      // set it
      try {
        setMap(map);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }//ChooseMap
 
  /**
   * A layer for our model
   */
  private class GedcomLayer extends Layer implements FeatureCollection, GeoModelListener, ActionListener {
    
    private Timer timer;
    
    /** constructor */
    private GedcomLayer() {
      
      // prepare a timer for delayed updates
      timer = new Timer(500, this);
      timer.setRepeats(false);
      
      // connect us to Jumps internals
      setName("Gedcom Locations");
      setFeatureCollection(this);
      
      // prepare some styles
      addStyle(new BasicStyle(Color.LIGHT_GRAY));
       
      VertexStyle vertices = new SquareVertexStyle();
      vertices.setEnabled(true);
      vertices.setSize(5);
      addStyle(vertices);
       
      LabelStyle labels = new LabelStyle();
      labels.setColor(Color.BLACK);
      labels.setEnabled(true);
      labels.setAttribute("PLAC");
      labels.setHidingOverlappingLabels(true);
      addStyle(labels);
      
      // done
    }
    
    /** timer - time to propagate update */
    public void actionPerformed(ActionEvent e) {
      LayerManager mgr = getLayerManager();
      if (mgr!=null)
        mgr.fireFeaturesChanged(new ArrayList(), FeatureEventType.ADDED, this);
    }
    
    /** geo model - a location has been found */
    public void locationUpdated(GeoLocation location) {
      timer.start();
    }

    /** feature collection - our schema */
    public FeatureSchema getFeatureSchema() {
      return GeoLocation.SCHEMA;
    }

    /** feature collection - our envelope is empty by default */
    public Envelope getEnvelope() {
      return new Envelope();
    }
    
    /** feature collection - # of features */
    public int size() {
      return model.getKnownLocations().size();
    }
    
    /** feature collection - feature access */
    public List getFeatures() {
      return model.getKnownLocations();
    }
    
    /** feature collection - feature access */
    public List query(Envelope envelope) {
      return getFeatures();
    }
    
  } //GedcomLayer
  
} //GeoView
