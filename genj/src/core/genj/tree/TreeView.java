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

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
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
import genj.view.ContextSupport;
import genj.view.CurrentSupport;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;
import gj.layout.tree.TreeLayoutRenderer;
import gj.ui.UnitGraphics;

/**
 * TreeView
 */
public class TreeView extends JPanel implements CurrentSupport, ContextPopupSupport, ToolBarSupport, ContextSupport {
  
  /*package*/ static final Resources resources = new Resources(TreeView.class); 
  
  /** the units we use */
  private final static double UNITS = UnitGraphics.CENTIMETERS;
  
  /** our model */
  private Model model;

  /** our content */
  private Content content;
  
  /** our overview */
  private Overview overview;
  
  /** our content renderer */
  private ContentRenderer contentRenderer;
  
  /** our current selection */
  private Entity currentEntity = null;
  
  /** our current zoom */
  private double zoom = 1.0D;
  
  /** the frame we're in */
  private Frame frame;
  
  /** the registry we're working with */
  private Registry registry;
  
  /**
   * Constructor
   */
  public TreeView(Gedcom gedcm, Registry regIstry, Frame fRame) {
    // remember
    frame = fRame;
    registry = regIstry;
    // setup sub-parts
    model = new Model(gedcm);
    contentRenderer = new ContentRenderer();
    content = new Content();
    JScrollPane scroll = new JScrollPane(new ViewPortAdapter(content));
    overview = new Overview(scroll);
    overview.setVisible(false);
    // setup content
    setLayout(new MyLayout()); 
    add(overview);
    add(scroll);
    // init model
    model.setRoot((Fam)gedcm.getEntities(Gedcom.FAMILIES).get(0));
    // done
  }
  
  /**
   * @see javax.swing.JComponent#isOptimizedDrawingEnabled()
   */
  public boolean isOptimizedDrawingEnabled() {
    return false;
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
    content.repaint();
    overview.repaint();
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
   * @see genj.view.ContextSupport#createActions(genj.gedcom.Entity)
   */
  public List createActions(Entity entity) {
    // fam or indi?
    if (!(entity instanceof Indi||entity instanceof Fam)) 
      return null;
    // create an action for our tree
    List result = new ArrayList(1);
    result.add(new ActionRoot(entity));
    // done
    return result;
  }

  /**
   * @see genj.view.ContextSupport#createActions(genj.gedcom.Gedcom)
   */
  public List createActions(Gedcom gedcom) {
    return null;
  }

  /**
   * @see genj.view.ContextSupport#createActions(genj.gedcom.Property)
   */
  public List createActions(Property property) {
    return null;
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
   * Overview
   */
  private class Overview extends JComponent implements ChangeListener, ModelListener, MouseListener, MouseMotionListener {
    /** keep the viewport */
    private JViewport viewport;
    /** the last indicator */
    private Rectangle last = null; 
    /**
     * Constructor
     */
    private Overview(JScrollPane scroll) {
      viewport = scroll.getViewport();
      viewport.addChangeListener(this);
      addMouseListener(this); 
      addMouseMotionListener(this);
    }
    /**
     * @see javax.swing.JComponent#getMaximumSize()
     */
    public Dimension getMaximumSize() {
      return new Dimension(128, 128);
    }
    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paint(Graphics g) {
      
      // calc zoom
      Dimension bounds = getSize();
      double 
        zoomx = bounds.width /(model.getBounds().getWidth ()*UNITS),
        zoomy = bounds.height/(model.getBounds().getHeight()*UNITS);
  
      // clear it
      g.setColor(Color.white);
      g.fillRect(0,0,bounds.width,bounds.height);
  
      // go 2d
      UnitGraphics ug = new UnitGraphics(g, UNITS*zoomx, UNITS*zoomy);
      // init renderer
      contentRenderer.cBackground    = Color.white;
      contentRenderer.cIndiShape     = Color.black;
      contentRenderer.cFamShape      = Color.black;
      contentRenderer.cMarrShape     = Color.black;
      contentRenderer.cArcs          = Color.black;
      contentRenderer.cSelectedShape = Color.red;
      contentRenderer.selection      = currentEntity;
      contentRenderer.isRenderContent= false;
      contentRenderer.isRenderArcs   = false;
      // let the renderer do its work
      ug.pushTransformation();
      contentRenderer.render(ug, model);
      ug.popTransformation();
  
      // frame it
      g.setColor(Color.blue);
      g.drawRect(0,0,bounds.width-1,bounds.height-1);

      // reset last
      last = null;
      
      // indicate viewport
      paintViewport(g);
      
      // done
    }
    /**
     * Draw viewport indicator     */
    private void paintViewport(Graphics g) {
      // have to undo last one?
      if (last!=null) {
        g.setColor(Color.white);
        g.setXORMode(Color.lightGray);
        g.fillRect(last.x, last.y, last.width, last.height);
      }
      // analyze content
      Rectangle bounds = viewport.getViewRect();
      double 
        xzoom = ((double)getWidth() )/content.getWidth(),
        yzoom = ((double)getHeight())/content.getHeight();
      // build rect
      last = new Rectangle(
        (int)(bounds.x * xzoom),
        (int)(bounds.y * yzoom),
        (int)(bounds.width * xzoom),
        (int)(bounds.height* yzoom)
      );
      // indicate content bounds
      g.setColor(Color.lightGray);
      g.setXORMode(Color.white);
      g.fillRect(last.x, last.y, last.width, last.height);
      // done
    }
    /**
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent arg0) {
      if (!isVisible()) return;
      paintViewport(getGraphics());
    }
    /**
     * @see genj.tree.ModelListener#structureChanged(Model)
     */
    public void structureChanged(Model model) {
      repaint();
    }
    /**
     * @see genj.tree.ModelListener#nodesChanged(Model, List)
     */
    public void nodesChanged(Model model, List nodes) {
      repaint();
    }
    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
      // FIXME : change viewport
    }
    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
    }
    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
    }
    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
    }
    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent arg0) {
    }
    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent e) {
      // FIXME : scroll viewport
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(MouseEvent e) {
      int cursor = Cursor.DEFAULT_CURSOR;
      if (last.contains(e.getPoint()))
        cursor = Cursor.MOVE_CURSOR;
      setCursor(Cursor.getPredefinedCursor(cursor));
    }
  } //Overview
  
  /**
   * The content we use for drawing
   */
  private class Content extends JComponent implements ModelListener, MouseListener {

    /**
     * Constructor
     */
    private Content() {
      model.addListener(this);
      addMouseListener(this);
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
      repaint();
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
    public void paint(Graphics g) {
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
      contentRenderer.isRenderContent= true;
      contentRenderer.isRenderArcs   = true;
      // let the renderer do its work
      contentRenderer.render(ug, model);
      // render the layout, too
//      ug.setColor(Color.green);
//      new TreeLayoutRenderer().render(model, model.getLayout(), ug);
      // done
    }
    
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
    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
    }
    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
    }
    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
    }
  } //Content

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
      overview.setVisible(!overview.isVisible());
    }
  } //ActionOverview    

  /**
   * Special layout   */
  private class MyLayout implements LayoutManager {
    /**
     * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
     */
    public void addLayoutComponent(String name, Component comp) {
      // ignored
    }
    /**
     * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
     */
    public void removeLayoutComponent(Component arg0) {
      // ignored
    }
    /**
     * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
     */
    public void layoutContainer(Container parent) {
      // layout components
      int 
        w = parent.getWidth(),
        h = parent.getHeight();
      Component[] cs = parent.getComponents();
      for (int c=0; c<cs.length; c++) {
        Dimension max = cs[c].getMaximumSize();
        cs[c].setBounds(0,0,Math.min(w,max.width),Math.min(h,max.height));
      }
      // done
    }
    /**
     * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
     */
    public Dimension preferredLayoutSize(Container arg0) {
      return new Dimension(640,480);
    }
    /**
     * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
     */
    public Dimension minimumLayoutSize(Container arg0) {
      return new Dimension(128,128);
    }
  } //MyLayout
  
  /**
   * ActionTree
   */
  private class ActionRoot extends ActionDelegate {
    /** entity */
    private Entity root;
    /**
     * Constructor
     */
    private ActionRoot(Entity entity) {
      root = entity;
      setText("Make root in "+frame.getTitle());
      setImage(Images.imgView);
    }
    
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      setRoot(root);
    }
  } //ActionTree
    
} //TreeView
