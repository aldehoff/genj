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
import genj.gedcom.Property;
import genj.gedcom.PropertyComparator;
import genj.gedcom.PropertyDate;
import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.WordBuffer;
import genj.util.swing.ButtonHelper;
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
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionListener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.LabelStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.RingVertexStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.SquareVertexStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.VertexStyle;
import com.vividsolutions.jump.workbench.ui.zoom.PanTool;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomTool;

/**
 * The view showing gedcom data in geographic context
 */
public class GeoView extends JPanel implements ContextListener, ToolBarSupport {
  
  private final static ImageIcon 
    IMG_MAP = new ImageIcon(GeoView.class, "images/Map.png"),
    IMG_ZOOM = new ImageIcon(GeoView.class, "images/Zoom.png"),
    IMG_ZOOM_EXTENT = new ImageIcon(GeoView.class, "images/ZoomExtend.png");
  
  /*package*/ final static Resources RESOURCES = Resources.get(GeoView.class);
  
  /** gedcom we're looking at */
  private Gedcom gedcom;
  
  /** handle to view manager */
  private ViewManager viewManager;
  
  /** the current map */
  private GeoMap currentMap;
  
  /** split panel */
  private JSplitPane split;
  
  /** the current layer view panel */
  private LayerViewPanel layerPanel;
  
  /** registry */
  private Registry registry;
  
  /** our model & layer */
  private GeoModel model;
  private GeoList locationList;
  private LocationsLayer locationLayer;  
  private SelectionLayer selectionLayer;
  private CursorTool currentTool;
  
  /**
   * Constructor
   */
  public GeoView(String title, Gedcom gedcom, Registry registry, ViewManager viewManager) {
    
    // state to remember
    this.registry = registry;
    this.viewManager = viewManager;
    this.gedcom = gedcom;
    
    // create our model 
    model = new GeoModel(gedcom);
    
    // create a location grid
    locationList = new GeoList(model);
    
    // create layers
    locationLayer = new LocationsLayer();  
    selectionLayer = new SelectionLayer();
    
    // hook em up
    locationList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(javax.swing.event.ListSelectionEvent e) {
        selectionLayer.setLocations(locationList.getSelectedLocations());
      }
    });

    // set layout
    split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, null, new JScrollPane(locationList));
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, split);
    
    // done
  }
  
  /**
   * Generate a textual representation of a bunch of locations
   */
  private String getSummary(Coordinate coord, Collection locations) {

    // collect text
    StringBuffer text = new StringBuffer();
    text.append("<html><body>");
    text.append( GeoLocation.getString(coord));
    int rows = 1;
    
    // loop over locations
    outer: for (Iterator it = locations.iterator(); it.hasNext(); )  {
      GeoLocation location = (GeoLocation)it.next();
      
      text.append("<br><b>");
      text.append(location);
      text.append("</b>");
      rows++;

      Property[] properties = location.getProperties();
      Arrays.sort(properties, new PropertyComparator(".:DATE"));
      
      List residents = new ArrayList();
      
      // loop over properties at location
      for (int i=0; i<properties.length; i++) {
        Property prop = properties[i];
        if (prop.getTag()=="RESI") {
          residents.add(prop.getEntity());
          continue;
        }
        text.append("<br>");
        if (rows>16) {
          text.append("...");
          break outer;
        }
        PropertyDate date = (PropertyDate)prop.getProperty(PropertyDate.TAG);
        if (date!=null) {
          text.append(date);
          text.append(" ");
        }
        text.append(Gedcom.getName(prop.getTag()));
        text.append(" ");
        text.append(prop.getEntity());
        rows++;
        // next property at current location
      }
      
      // add residents
      if (!residents.isEmpty()) {
        text.append("<br>");
        text.append(Gedcom.getName("RESI", true)+": ");
        for (int i=0;i<residents.size();i++) {
          if (i>0) text.append( i%3==0 ? "<br>&nbsp;" : "  ");
          text.append(residents.get(i));
        }
      }
      
      // next location
    }

    // done
    return text.toString();
  }
  
  /**
   * component lifecycle - we're needed
   */
  public void addNotify() {
    // override
    super.addNotify();
    // show map 
    String map = registry.get("map", (String)null);
    if (map!=null) {
      GeoMap[] maps = GeoService.getInstance().getMaps();
      for (int i=0;i<maps.length;i++) {
        if (maps[i].getKey().equals(map)) {
          try { setMap(maps[i], false); } catch (Throwable t) {}
          break;
        }
      }
    }
    // done
  }
  
  /**
   * component lifecycle - we're not needed anymore
   */
  public void removeNotify() {
    // remember map
    if (currentMap!=null)
      registry.put("map", currentMap.getKey());
    // remember split
    registry.put("split", split.getDividerLocation());
    // tell to layers
    selectionLayer.setLayerManager(null);
    locationLayer.setLayerManager(null);
    // override
    super.removeNotify();
  }
    
  /**
   * Callback for context changes
   */
  public void setContext(Context context) {
    // change selection in list - that'll trigger a selection layer update as well
    locationList.setSelectedLocations(model.getLocations(context));
    // done
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
    chooseMap.setToolTipText(RESOURCES.getString("toolbar.map"));
    chooseMap.setEnabled(!actions.isEmpty());
    bar.add(chooseMap);
    
    // add zoom
    ButtonHelper bh = new ButtonHelper();
    bh.setContainer(bar).setResources(RESOURCES);
    bh.create(new ZoomExtent());
    bh.setButtonType(JToggleButton.class).create(new ZoomOnOff());
    
    // done
  }
  
  /**
   * Choose current map
   */
  public void setMap(GeoMap map, boolean warn) throws IOException {
    
    // keep
    currentMap = map;
    
    // setup layer manager and add our own feature collection that's wrapping the model
    LayerManager layerManager = new LayerManager();
    layerManager.addLayer("GenJ", locationLayer);
    layerManager.addLayer("GenJ", selectionLayer);
    
    selectionLayer.setLayerManager(layerManager);
    locationLayer.setLayerManager(layerManager);

    // load map
    map.load(layerManager);
    
    // pack into panel
    layerPanel = new LayerPanel(layerManager);
    layerPanel.setBackground(map.getBackground());
    if (currentTool!=null)
      layerPanel.setCurrentCursorTool(currentTool);

    // show
    split.setLeftComponent(layerPanel);
    revalidate();
    repaint();
    
    // set split position
    int pos = registry.get("split", 400);
    split.setLastDividerLocation(pos);
    split.setDividerLocation(pos);
    
    // enable tooltips
    ToolTipManager.sharedInstance().registerComponent(layerPanel);
    ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
    
    // test for available countries
    if (warn) {
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
        viewManager.getWindowManager().openDialog("missingcountries", null, WindowManager.IMG_INFORMATION, note, CloseWindow.OK(), GeoView.this);
      }
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
      if (message!=null&&message.length()>0)
        Debug.log(Debug.INFO, GeoView.this, message);
    }
  }
  
  /**
   * Action - Zoom to Map Extent
   */
  private class ZoomExtent extends ActionDelegate {

    /** constructor */
    private ZoomExtent() {
      setImage(IMG_ZOOM_EXTENT);
      setTip("toolbar.extent");
    }
    /** zoom to all */
    public void execute() {
      if (layerPanel!=null) try {
          layerPanel.getViewport().zoomToFullExtent();
        } catch (Throwable t) {
        }
      }
    
  } //ZoomAll

  /**
   * Action - Zoom On Off
   */
  private class ZoomOnOff extends ActionDelegate {
    /** constructor */
    private ZoomOnOff() {
      setImage(IMG_ZOOM);
      setTip("toolbar.zoom");
    }
    /** choose current map */
    protected void execute() {
      currentTool =  currentTool instanceof ZoomTool ? (CursorTool)new PanTool(null) :  new ZoomTool(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      if (layerPanel!=null) 
        layerPanel.setCurrentCursorTool(currentTool);
    }
  }//ZoomOnOff
 
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
      // remember split
      registry.put("split", split.getDividerLocation());
      // set it
      try {
        setMap(map, true);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }//ChooseMap
 
  /**
   * A layer for our selection
   */
  private class SelectionLayer extends LocationsLayer{
    
    private List selection = Collections.EMPTY_LIST;
    
    /** constructor */
    private SelectionLayer() {
    }

    /** initializer */
    protected void initStyles() {
      
      // prepare some styles
      addStyle(new BasicStyle(Color.RED));
       
      VertexStyle vertices = new RingVertexStyle();
      vertices.setEnabled(true);
      vertices.setSize(5);
      addStyle(vertices);
       
      LabelStyle labels = new LabelStyle();
      labels.setEnabled(false);
      addStyle(labels);
      
      // done
    }
    
    /** set selection */
    public void setLocations(List set) {
      synchronized (this) {
        selection = set;
      }
      LayerManager mgr = getLayerManager();
      if (mgr!=null)
        mgr.fireFeaturesChanged(new ArrayList(), FeatureEventType.ADDED, this);
    }
    
    /** geo model - a location has been updated */
    public void locationAdded(GeoLocation location) {
      setLocations(Collections.EMPTY_LIST);
      super.locationAdded(location);
    }

    /** geo model - a location has been updated */
    public void locationUpdated(GeoLocation location) {
      setLocations(Collections.EMPTY_LIST);
      super.locationUpdated(location);
    }

    /** geo model - a location has been removed */
    public void locationRemoved(GeoLocation location) {
      setLocations(Collections.EMPTY_LIST);
      super.locationRemoved(location);
    }

    /** selection size */
    public int size() {
      return selection.size();
    }
    
    /** selection access */
    public List getFeatures() {
      return selection;
    }
    
  } //SelectionLayer
  
  /**
   * A layer for our model's locations
   */
  private class LocationsLayer extends Layer implements FeatureCollection, GeoModelListener, ActionListener {
    
    private List locations = new ArrayList();
    
    protected Timer updateTimer;
    
    /** constructor */
    private LocationsLayer() {
      // prepare a timer for delayed updates
      updateTimer = new Timer(500, this);
      updateTimer.setRepeats(false);
      
      // connect us to Jumps internals
      setName(getClass().toString());
      setFeatureCollection(this);

      // init styles
      initStyles();
    }
    
    public void setLayerManager(LayerManager set) {
      LayerManager old = super.getLayerManager();
      if (set!=null) {
        super.setLayerManager(set);
        if (old==null) {
          reset();
          model.addGeoModelListener(this);
        }
      } else {
        if (old!=null) 
          model.removeGeoModelListener(this);
      }
    }
    
    private void reset() {
      for (Iterator it = model.getLocations().iterator(); it.hasNext(); ) {
        GeoLocation location = (GeoLocation)it.next();
        if (location.isValid())
          locations.add(location);
      }
      updateTimer.start();
    }
    
    /** initializer */
    protected void initStyles() {
      
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
    
    /** geo model - a location has been added */
    public void locationAdded(GeoLocation location) {
      if (location.isValid())
        locations.add(location);
      updateTimer.start();
    }

    /** geo model - a location has been updated */
    public void locationUpdated(GeoLocation location) {
      if (location.isValid()&&!locations.contains(location))
        locations.add(location);
      updateTimer.start();
    }

    /** geo model - a location has been removed */
    public void locationRemoved(GeoLocation location) {
      locations.remove(location);
      updateTimer.start();
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
      return locations.size();
    }
    
    /** feature collection - feature access */
    public List getFeatures() {
      return locations;
    }
    
    /** feature collection - feature access */
    public List query(Envelope envelope) {
      return getFeatures();
    }
    
  } //LocationsLayer
  
  /**
   * The layer panel
   */
  private class LayerPanel extends LayerViewPanel implements ComponentListener {

    public LayerPanel(LayerManager mgr) {
      super(mgr, new ViewContext());
      addComponentListener(this);
    }
    
    public String getToolTipText(MouseEvent event) {
      try {
        // convert to coordinate
        Coordinate coord  = getViewport().toModelCoordinate(event.getPoint());
        // find features
        Collection locations = layerPanel.featuresWithVertex(event.getPoint(), 3,  model.getLocations());
        // generate text
        return getSummary(coord, locations);
      } catch (Throwable t) {
        return null;
      }
    }

    /** component listener callback */
    public void componentHidden(ComponentEvent e) {
    }
    /** component listener callback */
    public void componentMoved(ComponentEvent e) {
    }
    /** component listener callback */
    public void componentResized(ComponentEvent e) {
      new ZoomExtent().trigger();
    }
    /** component listener callback */
    public void componentShown(ComponentEvent e) {
    }
    
  }
  
} //GeoView
