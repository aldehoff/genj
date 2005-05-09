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
import genj.util.swing.ImageIcon;
import genj.util.swing.PopupWidget;
import genj.view.Context;
import genj.view.ContextListener;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;
import com.vividsolutions.jump.workbench.ui.renderer.style.SquareVertexStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.VertexStyle;

/**
 * The view showing gedcom data in geographic context
 */
public class GeoView extends JPanel implements ContextListener, ToolBarSupport, GeoModelListener {
  
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
  
  /** our model */
  private GeoModel model;
  
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
  }
  
  /**
   * Lifecycle callback - we're needed
   */
  public void addNotify() {
    
    // continue with super's
    super.addNotify();
    
    // create a model
    model = new GeoModel(gedcom);
    model.addGeoModelListener(this);

    // done
  }
  
  /**
   * Lifecycle callback - we're not needed at the moment
   */
  public void removeNotify() {
    
    // get rid of model
    model.removeGeoModelListener(this);
    model = null;

    // continue with super
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
    Layer layer = layerManager.addLayer("GenJ", "PLACs", new ModelWrapper());
    layer.getBasicStyle().setLineColor(Color.BLACK);
    VertexStyle vertices = new SquareVertexStyle();
    vertices.setEnabled(true);
    vertices.setSize(5);
    layer.addStyle(vertices);
//    LabelStyle labels = new LabelStyle();
//    labels.setEnabled(true);
//    labels.setAttribute("PLAC");
//    labels.setHidingOverlappingLabels(false);
//    layer.addStyle(labels);
    
    // load map
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
 
  /**
   * A wrapper for our model
   */
  private class ModelWrapper implements FeatureCollection {
    
    private GeometryFactory factory = new GeometryFactory();
    private FeatureSchema schema;
    private List features;
    
    /** constructor */
    private ModelWrapper() {
      schema = new FeatureSchema();
      schema.addAttribute("PLAC", AttributeType.STRING);
      schema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);

      // FIXME this collection features once only at the moment
      Collection locations = model.getLocations();
      features = new ArrayList(locations.size());
      for (Iterator it = locations.iterator(); it.hasNext(); ) {
        
        GeoLocation location = (GeoLocation)it.next();
        if (location.isKnown()) {
          BasicFeature feature = new BasicFeature(schema);
          feature.setGeometry(factory.createPoint(new Coordinate(location.getLongitude(),location.getLatitude())));
          feature.setAttribute("PLAC", location.toString());
          features.add(feature);
        }
        
      }
      
      // done
    }

    /** feature collection - our schema */
    public FeatureSchema getFeatureSchema() {
      return schema;
    }

    /** feature collection - our envelope */
    public Envelope getEnvelope() {
      return new Envelope();
      //return new Envelope(new Coordinate(7.099818448299999,50.73455818310999));
    }
    
    /** feature collection - # of features */
    public int size() {
      return features.size();
    }
    
    /** feature collection - feature access */
    public List getFeatures() {
      return features;
    }
    
    /** feature collection - feature access */
    public List query(Envelope envelope) {
      return getFeatures();
    }
    
  } //ModelWrapper
  
} //GeoView
