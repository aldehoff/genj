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

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.util.ActionDelegate;
import genj.util.ColorSet;
import genj.util.ImgIcon;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.DoubleValueSlider;
import genj.util.swing.ViewPortAdapter;
import genj.util.swing.ViewPortOverview;
import genj.view.ContextPopupSupport;
import genj.view.ContextSupport;
import genj.view.CurrentSupport;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;
import gj.model.Node;
import gj.ui.UnitGraphics;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * TreeView
 */
public class TreeView extends JPanel implements CurrentSupport, ContextPopupSupport, ToolBarSupport, ContextSupport {
  
  /*package*/ static final Resources resources = new Resources(TreeView.class);
  
  /** the units we use */
  private final static double UNITS = UnitGraphics.CENTIMETERS;
  
  /** our model */
  /*package*/ Model model;

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

  /** our current zoom */  
  private DoubleValueSlider sliderZoom;  
  
  /** the frame we're in */
  private Frame frame;
  
  /** the registry we're working with */
  private Registry registry;
  
  /** our colors */
  /*package*/ ColorSet colors;
  
  /**
   * Constructor
   */
  public TreeView(Gedcom gedcm, Registry regIstry, Frame fRame) {
    // remember
    frame = fRame;
    registry = regIstry;
    // grab colors
    colors = new ColorSet("content", Color.white, resources, registry);
    colors.add("indis"  , Color.black);
    colors.add("fams"   , Color.green);
    colors.add("substs" , Color.lightGray);
    colors.add("arcs"   , Color.blue);
    colors.add("selects", Color.red);
    
    // setup model
    model = new Model(gedcm);
    model.setVertical(registry.get("vertical",true));
    model.setFamilies(registry.get("families",true));
    model.setBendArcs(registry.get("bend"    ,true));
    model.setMode(registry.get("mode", 0));
    TreeMetrics defm = model.getMetrics();
    model.setMetrics(new TreeMetrics(
      registry.get("windis",(float)defm.wIndis),
      registry.get("hindis",(float)defm.hIndis),
      registry.get("wfams" ,(float)defm.wFams ),
      registry.get("hfams" ,(float)defm.hFams ),
      registry.get("pad"   ,(float)defm.pad   )
    ));

    // setup child components
    contentRenderer = new ContentRenderer();
    content = new Content();
    JScrollPane scroll = new JScrollPane(new ViewPortAdapter(content));
    overview = new Overview(scroll);
    overview.setVisible(registry.get("overview", false));
    overview.setSize(registry.get("overview", new Dimension(64,64)));
    zoom = registry.get("zoom", 1.0F);
    
    // setup layout
    setLayout(new MyLayout()); 
    add(overview);
    add(scroll);

    // set root
    Entity root;
    try { 
      root = gedcm.getEntity(registry.get("root",(String)null));
    } catch (Exception e) {
      root = ViewManager.getInstance().getCurrentEntity(gedcm);
    }
    model.setRoot(root);
    
    // done
  }
  
  /**
   * @see javax.swing.JComponent#removeNotify()
   */
  public void removeNotify() {
    registry.put("overview", overview.isVisible());
    registry.put("overview", overview.getSize());
    if (sliderZoom!=null) registry.put("zoom", (float)sliderZoom.getValue());
    registry.put("vertical", model.isVertical());
    registry.put("families", model.isFamilies());
    registry.put("bend"    , model.isBendArcs());
    registry.put("mode"    , model.getMode());
    TreeMetrics m = model.getMetrics();
    registry.put("windis"  ,(float)m.wIndis);
    registry.put("hindis"  ,(float)m.hIndis);
    registry.put("wfams"   ,(float)m.wFams );
    registry.put("hfams"   ,(float)m.hFams );
    registry.put("pad"     ,(float)m.pad   );
    
    if (model.getRoot()!=null) registry.put("root", model.getRoot().getId());
    super.removeNotify();
  }
  
  /**
   * @see javax.swing.JComponent#isOptimizedDrawingEnabled()
   */
  public boolean isOptimizedDrawingEnabled() {
    return !overview.isVisible();
  }

  
  /**
   * @see genj.view.CurrentSupport#setCurrentEntity(Entity)
   */
  public void setCurrentEntity(Entity entity) {
    // anything new?
    if (entity==currentEntity) return;
    // allowed?
    if (!(entity instanceof Indi||entity instanceof Fam)) return;
    // Node for it?
    Node node = model.getNode(entity);
    if (node==null) return;
    // remember
    currentEntity = entity;
    // scroll
    Rectangle2D b = model.getBounds();
    Point2D     p = node.getPosition();
    Dimension   d = getSize();
    content.scrollRectToVisible(new Rectangle(
      UnitGraphics.units2pixels( p.getX() - b.getMinX(), UNITS*zoom ) - d.width /2,
      UnitGraphics.units2pixels( p.getY() - b.getMinY(), UNITS*zoom ) - d.height/2,
      d.width ,
      d.height
    ));
    // make sure it's reflected
    content.repaint();
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
    sliderZoom = new DoubleValueSlider(0.1D,1.0D,zoom,false);
    sliderZoom.addChangeListener(new ZoomGlue());
    sliderZoom.setText("%");
    sliderZoom.setAlignmentX(0F);
    bar.add(sliderZoom);
    
    // overview
    ButtonHelper bh = new ButtonHelper().setContainer(bar).setResources(resources);
    bh.create(new ActionOverview());
    
    // vertical/horizontal
    bh.create(new ActionOrientation())
      .setSelected(model.isVertical());
    
    // families?
    bh.create(new ActionFamsAndSpouses())
      .setSelected(model.isFamilies());
    
    // bending?
    bh.create(new ActionBend())
      .setSelected(model.isBendArcs());
    
    // modes
    bh.createGroup();
    bh.create(new ActionAsDsAnDs(Model.ANCESTORS_AND_DESCENDANTS))
      .setSelected(model.getMode()==Model.ANCESTORS_AND_DESCENDANTS);
    bh.create(new ActionAsDsAnDs(Model.ANCESTORS))
      .setSelected(model.getMode()==Model.ANCESTORS);
    bh.create(new ActionAsDsAnDs(Model.DESCENDANTS))
      .setSelected(model.getMode()==Model.DESCENDANTS);
    
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
    // make it current
    currentEntity = null;
    setCurrentEntity(root);
    // done
  }

  /**
   * Resolves entity at given position
   */
  public Entity getEntityAt(Point pos) {
    Rectangle2D bounds = model.getBounds();
    return model.getEntityAt(
      UnitGraphics.pixels2units(pos.x,UNITS*zoom)+bounds.getMinX(), 
      UnitGraphics.pixels2units(pos.y,UNITS*zoom)+bounds.getMinY()
    );
  }

  /**
   * Overview   */
  private class Overview extends ViewPortOverview implements ModelListener {
    /**
     * Constructor     */
    private Overview(JScrollPane scroll) {
      super(scroll.getViewport());
      model.addListener(this);
//      super.setMaximumSize(new Dimension(128,128));
      super.setSize(new Dimension(128,128));
    }
    /**
     * @see java.awt.Container#getMaximumSize()
     */
    public Dimension getMaximumSize() {
      return super.getSize();
    }
//    /**
//     * @see javax.swing.JComponent#removeNotify()
//     */
//    public void removeNotify() {
//      super.removeNotify();
//      model.removeListener(this);
//    }
//    /**
//     * @see java.awt.Component#setSize(java.awt.Dimension)
//     */
//    public void setSize(Dimension d) {
//      super.setSize(d);
//      setMaximumSize(d);
//    }
    /**
     * @see genj.util.swing.ViewPortOverview#paintContent(java.awt.Graphics, double, double)
     */
    protected void renderContent(Graphics g, double zoomx, double zoomy) {

      // go 2d
      UnitGraphics ug = new UnitGraphics(g, UNITS*zoomx*zoom, UNITS*zoomy*zoom);
      // init renderer
      contentRenderer.cBackground    = Color.white;
      contentRenderer.cIndiShape     = Color.black;
      contentRenderer.cFamShape      = Color.black;
      contentRenderer.cArcs          = Color.lightGray;
      contentRenderer.cUnknownShape  = Color.white;
      //contentRenderer.cSelectedShape = Color.white;
      contentRenderer.selection      = null;
      contentRenderer.isRenderContent= false;
      // let the renderer do its work
      ug.pushTransformation();
      contentRenderer.render(ug, model);
      ug.popTransformation();

      // done  
    }
    /**
     * @see genj.tree.ModelListener#nodesChanged(genj.tree.Model, java.util.List)
     */
    public void nodesChanged(Model arg0, List arg1) {
      repaint();
    }
    /**
     * @see genj.tree.ModelListener#structureChanged(genj.tree.Model)
     */
    public void structureChanged(Model arg0) {
      repaint();
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
      contentRenderer.cBackground    = colors.getColor("content");
      contentRenderer.cIndiShape     = colors.getColor("indis");
      contentRenderer.cFamShape      = colors.getColor("fams");
      contentRenderer.cUnknownShape  = colors.getColor("substs");
      contentRenderer.cArcs          = colors.getColor("arcs");
      contentRenderer.cSelectedShape = colors.getColor("selects");
      contentRenderer.selection      = currentEntity;
      contentRenderer.isRenderContent= true;
      // let the renderer do its work
      contentRenderer.render(ug, model);
      // done
    }
    
    /**
     * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
      // a new sleection?
      Entity entity = getEntityAt(e.getPoint());
      if (entity==null||entity==currentEntity) return;
      // note it already
      currentEntity = entity;
      repaint();
      // propagate it
      ViewManager.getInstance().setCurrentEntity(entity);
      // done
    }
    
    /**
     * @see java.awt.event.MouseAdapter#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
      // double -> root
      if (e.getClickCount()>1) {
        Entity entity = getEntityAt(e.getPoint());
        if (entity!=null) setRoot(entity);
      }
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
      setText(resources.getString("root",frame.getTitle()));
      setImage(Images.imgView);
    }
    
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      setRoot(root);
    }
  } //ActionTree

  /**
   * Actions As/Ds/AnDs
   */
  private class ActionAsDsAnDs extends ActionDelegate {
    /** the mode this toggles */
    private int mode;
    /**
     * Constructor     */
    private ActionAsDsAnDs(int moDe) {
      // remember
      mode = moDe; 
      // image
      ImgIcon i = null;
      String t = null;
      switch (mode) {
        case Model.ANCESTORS_AND_DESCENDANTS:
          i = Images.imgAnDs;
          t = "ancestorsdescendants.tip";
          break;
        case Model.DESCENDANTS:
          i = Images.imgDs; 
          t = "descendants.tip";
          break;
        case Model.ANCESTORS: 
          i = Images.imgAs; 
          t = "ancestors.tip";
          break;
      }
      super.setImage(i);
      super.setToggle(i);
      super.setTip(t);
      // done      
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      model.setMode(mode);
    }
  } //ActionAsDsAnDs
  
  /**
   * Action Orientation change   */
  private class ActionOrientation extends ActionDelegate {
    /**
     * Constructor     */
    private ActionOrientation() {
      super.setImage(Images.imgHori);
      super.setToggle(Images.imgVert);
      super.setTip("orientation.tip");
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      model.setVertical(!model.isVertical());
    }
  } //ActionOrientation
  
  /**
   * Action Families n Spouses
   */
  private class ActionFamsAndSpouses extends ActionDelegate {
    /**
     * Constructor
     */
    private ActionFamsAndSpouses() {
      super.setImage(Images.imgDontFams);
      super.setToggle(Images.imgDoFams);
      super.setTip("families.tip");
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      model.setFamilies(!model.isFamilies());
    }
  } //ActionOrientation

  /**
   * Action Bend Arcs
   */
  private class ActionBend extends ActionDelegate {
    /**
     * Constructor
     */
    private ActionBend() {
      super.setImage(Images.imgDontBend);
      super.setToggle(Images.imgDoBend);
      super.setTip("bend.tip");
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      model.setBendArcs(!model.isBendArcs());
    }
  } //ActionOrientation
       
} //TreeView
