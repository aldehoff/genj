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
package genj.tree;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.util.ActionDelegate;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.DoubleValueSlider;
import genj.util.swing.ViewPortAdapter;
import genj.view.ContextPopupSupport;
import genj.view.CurrentSupport;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;
import gj.layout.tree.TreeLayoutRenderer;
import gj.ui.UnitGraphics;

/**
 * TreeView
 */
public class TreeView extends JScrollPane implements CurrentSupport, ContextPopupSupport, ToolBarSupport {
  
  /*package*/ static final Resources resources = new Resources(TreeView.class); 
  
  /** the units we use */
  private final static double UNITS = UnitGraphics.CENTIMETERS;
  
  /** our model */
  private Model model;

  /** our content */
  private Content content;
  
  /** our content renderer */
  private ContentRenderer contentRenderer;
  
  /** our current selection */
  private Entity currentEntity = null;
  
  /** our current zoom */
  private double zoom = 1.0D;
  
  /** overview or not */
  private boolean overview = false;
  
  /**
   * Constructor
   */
  public TreeView(Gedcom gedcm, Registry regstry, Frame frame) {
    // setup sub-parts
    model = new Model(gedcm);
    content = new Content();
    contentRenderer = new ContentRenderer();
    // setup content
    setViewportView(new ViewPortAdapter(content));
    // init model
    model.setRoot((Fam)gedcm.getEntities(Gedcom.FAMILIES).get(0));
    // click listening
    content.addMouseListener(new MouseGlue());
    // done
  }
  
  /**
   * @see genj.view.CurrentSupport#setCurrentEntity(Entity)
   */
  public void setCurrentEntity(Entity entity) {
    // anything new?
    if (entity==currentEntity) return;
    // allowed?
    if (!(entity instanceof Indi||entity instanceof Fam)) return;
    // get and show
    currentEntity = entity;
    repaint();
    // done
  }
  
  /**
   * @see genj.view.CurrentSupport#setCurrentProperty(Property)
   */
  public void setCurrentProperty(Property property) {
  }

  /**
   * @see genj.view.ContextPopupSupport#getContextAt(Point)
   */
  public Object getContextAt(Point pos) {
    return getEntityAt(pos);
  }

  /**
   * @see genj.view.ContextPopupSupport#getContextPopupContainer()
   */
  public JComponent getContextPopupContainer() {
    return content;
  }
  
  /**
   * @see genj.view.ToolBarSupport#populate(JToolBar)
   */
  public void populate(JToolBar bar) {

    // zooming!    
    DoubleValueSlider sliderZoom = new DoubleValueSlider(0.1D,1.0D,1.0D,false);
    sliderZoom.addChangeListener(new ZoomGlue());
    sliderZoom.setText("%");
    bar.add(sliderZoom);
    
    // overview
    ButtonHelper bh = new ButtonHelper().setContainer(bar);
    bh.create(new ActionOverview());
    
    // done
  }

  /**
   * Sets the root of this view
   */
  public void setRoot(Entity root) {
    // allowed?
    if (!(root instanceof Indi||root instanceof Fam)) return;
    // keep it
    model.setRoot(root);
    // done
  }


  /**
   * Resolves entity at given position
   */
  public Entity getEntityAt(Point pos) {
    Rectangle2D bounds = model.getBounds();
    return model.getEntityAt(
      UnitGraphics.pixels2units(pos.x,UNITS)+bounds.getMinX(), 
      UnitGraphics.pixels2units(pos.y,UNITS)+bounds.getMinY()
    );
  }
  
  /**
   * @see javax.swing.JComponent#paint(Graphics)
   */
  public void paint(Graphics g) {
    
    // normal paint first
    super.paint(g);
    
    // overview?
    if (!overview) return;
    int dim = 128;
    double zoom = 
      dim/(Math.max(model.getBounds().getWidth(), model.getBounds().getHeight())*UNITS);

    // clear it
    g.setColor(Color.white);
    g.fillRect(0,0,dim,dim);

    // go 2d
    UnitGraphics ug = new UnitGraphics(g, UNITS*zoom, UNITS*zoom);
    // init renderer
    contentRenderer.cBackground    = Color.white;
    contentRenderer.cIndiShape     = Color.black;
    contentRenderer.cFamShape      = Color.black;
    contentRenderer.cMarrShape     = Color.black;
    contentRenderer.cArcs          = Color.black;
    contentRenderer.cSelectedShape = Color.red;
    contentRenderer.selection      = currentEntity;
    // let the renderer do its work
    ug.pushTransformation();
    contentRenderer.render(ug, model);
    ug.popTransformation();

    // frame it
    g.setColor(Color.blue);
    g.drawRect(0,0,dim,dim);
    
    // done
  }

  /**
   * The content we use for drawing
   */
  private class Content extends JComponent implements ModelListener {

    /**
     * Constructor
     */
    private Content() {
      model.addListener(this);
    }
    
    /**
     * @see javax.swing.JComponent#removeNotify()
     */
    public void removeNotify() {
      super.removeNotify();
      model.removeListener(this);
    }

    /**
     * @see genj.tree.ModelListener#structureChanged(Model)
     */
    public void structureChanged(Model model) {
      revalidate();
      repaint();
    }
    
    /**
     * @see genj.tree.ModelListener#nodesChanged(Model, List)
     */
    public void nodesChanged(Model model, List nodes) {
    }
    
    /**
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
      Rectangle2D bounds = model.getBounds();
      int 
        w = UnitGraphics.units2pixels(bounds.getWidth (), UNITS*zoom),
        h = UnitGraphics.units2pixels(bounds.getHeight(), UNITS*zoom);
      return new Dimension(w,h);
    }
  
    /**
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    protected void paintComponent(Graphics g) {
      // go 2d
      UnitGraphics ug = new UnitGraphics(g, UNITS*zoom, UNITS*zoom);
      // init renderer
      contentRenderer.cBackground    = Color.white;
      contentRenderer.cIndiShape     = Color.black;
      contentRenderer.cFamShape      = Color.green;
      contentRenderer.cMarrShape     = Color.pink;
      contentRenderer.cArcs          = Color.blue;
      contentRenderer.cSelectedShape = Color.red;
      contentRenderer.selection      = currentEntity;
      // let the renderer do its work
      contentRenderer.render(ug, model);
      // render the layout, too
//      ug.setColor(Color.green);
//      new TreeLayoutRenderer().render(model, model.getLayout(), ug);
      // done
    }
    
  } //Content

  /**
   * Glue to Mouse Stuff
   */
  private class MouseGlue extends MouseAdapter {
    /**
     * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
      // a new sleection?
      Entity entity = getEntityAt(e.getPoint());
      if (entity==null||entity==currentEntity) return;
      // propagate it
      ViewManager.getInstance().setCurrentEntity(entity);
      // done
    }
    
    /**
     * @see java.awt.event.MouseAdapter#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
      // double -> root
      if (e.getClickCount()>1&&currentEntity!=null) model.setRoot(currentEntity);
      // done
    }

  } //MouseGlue

  /**
   * Glue for zooming
   */
  private class ZoomGlue implements ChangeListener {
    /**
     * @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) {
      DoubleValueSlider dvs = (DoubleValueSlider)e.getSource();
      zoom = dvs.getValue();
      content.revalidate();
      repaint();
    }
  } //ZoomGlue
    
  /**
   * Action for opening overview
   */
  private class ActionOverview extends ActionDelegate {
    /**
     * Constructor
     */
    private ActionOverview() {
      super.setImage(Images.imgOverview);
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      overview = !overview;
      repaint();
    }
  } //ActionOverview    
  
} //TreeView
