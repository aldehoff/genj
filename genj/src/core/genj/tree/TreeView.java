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
import genj.gedcom.PropertyXRef;
import genj.io.Filter;
import genj.renderer.Blueprint;
import genj.renderer.BlueprintManager;
import genj.renderer.EntityRenderer;
import genj.util.ActionDelegate;
import genj.util.ColorSet;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.DoubleValueSlider;
import genj.util.swing.UnitGraphics;
import genj.util.swing.ImageIcon;
import genj.util.swing.ScreenResolutionScale;
import genj.util.swing.ViewPortOverview;
import genj.view.ContextPopupSupport;
import genj.view.ContextSupport;
import genj.view.CurrentSupport;
import genj.view.FilterSupport;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;
import gj.model.Node;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * TreeView
 */
public class TreeView extends JPanel implements CurrentSupport, ContextPopupSupport, ToolBarSupport, ContextSupport, FilterSupport {
  
  /*package*/ static final Resources resources = new Resources(TreeView.class);
  
  /** the units we use */
  private final Point2D UNITS = ScreenResolutionScale.getDotsPerCm();
  
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

  /** our current zoom */  
  private DoubleValueSlider sliderZoom;  
  
  /** the frame we're in */
  private Frame frame;
  
  /** the registry we're working with */
  private Registry registry;
  
  /** whether we use antialising */
  private boolean isAntialiasing = false;
  
  /** whether we adjust fonts to correct resolution */
  private boolean isAdjustFonts = false; 
  
  /** our colors */
  /*package*/ ColorSet colors;
  
  /** our blueprints */
  private Blueprint[] blueprints = new Blueprint[Gedcom.NUM_TYPES];
  
  /** our content's font */
  private Font contentFont = new Font("SansSerif", 0, 12);
  
  /** current centered position */
  private Point2D.Double center = new Point2D.Double(0,0);
  
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
    colors.add("fams"   , Color.darkGray);
    colors.add("arcs"   , Color.blue);
    colors.add("selects", Color.red);
    // grab font
    contentFont = registry.get("font", contentFont);
    isAdjustFonts = registry.get("adjust", isAdjustFonts);
    // grab blueprints
    blueprints = BlueprintManager.getInstance().readBlueprints(registry);
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
    isAntialiasing = registry.get("antial", false);
 
    Entity root = null;
    try { 
      root = gedcm.getEntity(registry.get("root",(String)null));
    } catch (Exception e) {
    }
    if (root==null) root = ViewManager.getInstance().getCurrentEntity(gedcm); 
    model.setRoot(root);
    
    try { 
      currentEntity = gedcm.getEntity(registry.get("current",(String)null));
    } catch (Exception e) {
      currentEntity = model.getRoot();
    }

    // setup child components
    contentRenderer = new ContentRenderer();
    content = new Content();
    JScrollPane scroll = new JScrollPane(content);
    overview = new Overview(scroll);
    overview.setVisible(registry.get("overview", false));
    overview.setSize(registry.get("overview", new Dimension(64,64)));
    zoom = registry.get("zoom", 1.0F);
    
    // setup layout
    add(overview);
    add(scroll);
    
    // scroll 1st time
    // scroll to last centered year
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        scrollToCurrent();
      }
    });

    // done
  }
  
  /**
   * @see java.awt.Container#doLayout()
   */
  public void doLayout() {
    // layout components
    int 
      w = getWidth(),
      h = getHeight();
    Component[] cs = getComponents();
    for (int c=0; c<cs.length; c++) {
      if (cs[c]==overview) continue;
      cs[c].setBounds(0,0,w,h);
    }
    // done
  }

  /**
   * @see javax.swing.JComponent#removeNotify()
   */
  public void removeNotify() {
    // settings
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
    registry.put("antial"  , isAntialiasing );
    registry.put("font"    , contentFont);
    registry.put("adjust"  , isAdjustFonts);
    // blueprints
    BlueprintManager.getInstance().writeBlueprints(blueprints, registry);
    // root    
    if (model.getRoot()!=null) registry.put("root", model.getRoot().getId());
    if (currentEntity!=null) registry.put("current", currentEntity.getId());
    // done
    super.removeNotify();
  }
  
  /**
   * @see javax.swing.JComponent#isOptimizedDrawingEnabled()
   */
  public boolean isOptimizedDrawingEnabled() {
    return !overview.isVisible();
  }

  /**
   * Accessor - isAntialising.
   */
  public boolean isAntialising() {
    return isAntialiasing;
  }

  /**
   * Accessor - isAntialising.
   */
  public void setAntialiasing(boolean set) {
    if (isAntialiasing==set) return;
    isAntialiasing = set;
    repaint();
  }
  
  /**
   * Access - isAdjustFonts.
   */
  public boolean isAdjustFonts() {
    return isAdjustFonts;
  }

  /**
   * Access - isAdjustFonts
   */
  public void setAdjustFonts(boolean set) {
    if (isAdjustFonts==set) return;
    isAdjustFonts = set;
    repaint();
  }
  
  /**
   * Access - contentFont
   */
  public Font getContentFont() {
    return contentFont;
  }

  /**
   * Access - contentFont
   */
  public void setContentFont(Font set) {
    if (contentFont.equals(set)) return;
    contentFont = set;
    repaint();
  }

  /**
   * Access - blueprints
   */
  public Blueprint[] getBlueprints() {
    return (Blueprint[])blueprints.clone();
  }

  /**
   * Access - blueprints
   */
  public void setBlueprints(Blueprint[] set) {
    // check 
    for (int i=0; i<set.length; i++) {
      if (!blueprints[i].equals(set[i])) {
        blueprints = set;
        repaint();
        return;
      }
    }
    // done
  }

  /**
   * Access - Mode
   */
  public Model getModel() {
    return model;
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
    scrollTo(node.getPosition());
    // make sure it's reflected
    content.repaint();
    // done
  }
  
  /**
   * Scroll to given position   */
  private void scrollTo(Point2D p) {
    // remember
    center.setLocation(p);
    // scroll
    Rectangle2D b = model.getBounds();
    Dimension   d = getSize();
    content.scrollRectToVisible(new Rectangle(
      (int)( (p.getX()-b.getMinX()) * (UNITS.getX()*zoom) ) - d.width /2,
      (int)( (p.getY()-b.getMinY()) * (UNITS.getY()*zoom) ) - d.height/2,
      d.width ,
      d.height
    ));
    // done
  }
  
  /**
   * Scroll to current entity   */
  private void scrollToCurrent() {
    // something to do?
    if (currentEntity==null) currentEntity=model.getRoot();
    if (currentEntity==null) return;
    // Node for it?
    Node node = model.getNode(currentEntity);
    if (node==null) return;
    // scroll
    scrollTo(node.getPosition());
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
    bh.create(new ActionOverview())
      .setSelected(overview.isVisible());
    
    // vertical/horizontal
    bh.create(new ActionOrientation())
      .setSelected(model.isVertical());
    
    // families?
    bh.create(new ActionFamsAndSpouses())
      .setSelected(model.isFamilies());
    
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
      pos.x / (UNITS.getX()*zoom) + bounds.getMinX(), 
      pos.y / (UNITS.getY()*zoom) + bounds.getMinY()
    );
  }
  
  /**
   * Resolve a renderer   */
  /*package*/ EntityRenderer getEntityRenderer(int type) {
    EntityRenderer result = new EntityRenderer(blueprints[type], contentFont);
    return result;
  }

  /**
   * @see genj.view.FilterSupport#getFilterName()
   */
  public String getFilterName() {
    return model.getEntities().size()+" shown in "+frame.getTitle();
  }
  
  /** 
   * @see genj.view.FilterSupport#getFilter()
   */
  public Filter getFilter() {
    return new VisibleFilter(model);
  }
  
  /**
   * A filter that includes visible indis/families
   */
  private static class VisibleFilter implements Filter {
    /** entities that are 'in' */
    private Set ents;
    /** whether we're showing families */
    private boolean fams;
    /**
     * Constructor
     */
    private VisibleFilter(Model model) {
      // all ents from the model
      ents = new HashSet(model.getEntities());
      fams = model.isFamilies();
      // done
    }
    /**
     * @see genj.io.Filter#accept(genj.gedcom.Entity)
     */
    public boolean accept(Entity ent) {
      // fam/indi
      if (ent.getType()==Gedcom.INDIVIDUALS||(ent.getType()==Gedcom.FAMILIES&&fams))
        return ents.contains(ent);
      // maybe a referenced other type?
      Entity[] refs = PropertyXRef.getReferences(ent);
      for (int r=0; r<refs.length; r++) {
        if (ents.contains(refs[r])) return true;
      }
      // not
      return false;
    }
    /**
     * @see genj.io.Filter#accept(genj.gedcom.Property)
     */
    public boolean accept(Property property) {
      return true;
    }
  } //VisibleFilter

  /**
   * Overview   */
  private class Overview extends ViewPortOverview implements ModelListener {
    /**
     * Constructor     */
    private Overview(JScrollPane scroll) {
      super(scroll.getViewport());
      model.addListener(this);
      super.setSize(new Dimension(TreeView.this.getWidth()/4,TreeView.this.getHeight()/4));
    }
    /**
     * @see java.awt.Component#setSize(int, int)
     */
    public void setSize(int width, int height) {
      width = Math.max(32,width);
      height = Math.max(32,height);
      super.setSize(width, height);
    }
    /**
     * @see java.awt.Container#removeNotify()
     */
    public void removeNotify() {
      super.removeNotify();
      model.removeListener(this);
    }
    /**
     * @see genj.util.swing.ViewPortOverview#paintContent(java.awt.Graphics, double, double)
     */
    protected void renderContent(Graphics g, double zoomx, double zoomy) {

      // go 2d
      UnitGraphics gw = new UnitGraphics(g,UNITS.getX()*zoomx*zoom, UNITS.getY()*zoomy*zoom);
      
      // init renderer
      contentRenderer.cBackground    = Color.white;
      contentRenderer.cIndiShape     = Color.black;
      contentRenderer.cFamShape      = Color.black;
      contentRenderer.cArcs          = Color.lightGray;
      //contentRenderer.cSelectedShape = Color.white;
      contentRenderer.selection      = null;
      contentRenderer.indiRenderer   = null;
      contentRenderer.famRenderer    = null;
      
      // let the renderer do its work
      contentRenderer.render(gw, model);
      
      // restore
      gw.popTransformation();

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
      double 
        w = bounds.getWidth () * (UNITS.getX()*zoom),
        h = bounds.getHeight() * (UNITS.getY()*zoom);
      return new Dimension((int)w,(int)h);
    }
  
    /**
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paint(Graphics g) {
      // resolve our Graphics
      UnitGraphics gw = new UnitGraphics(g,UNITS.getX()*zoom, UNITS.getY()*zoom);
      gw.setAntialiasing(isAntialiasing);
      // init renderer
      contentRenderer.cBackground    = colors.getColor("content");
      contentRenderer.cIndiShape     = colors.getColor("indis");
      contentRenderer.cFamShape      = colors.getColor("fams");
      contentRenderer.cArcs          = colors.getColor("arcs");
      contentRenderer.cSelectedShape = colors.getColor("selects");
      contentRenderer.selection      = currentEntity;
      contentRenderer.indiRenderer   = getEntityRenderer(Gedcom.INDIVIDUALS);
      contentRenderer.famRenderer    = getEntityRenderer(Gedcom.FAMILIES   );
      // special handling for adjusting fonts?
      if (isAdjustFonts) {
        contentRenderer.indiRenderer.setResolution(UNITS);
        contentRenderer. famRenderer.setResolution(UNITS);
      }
      // let the renderer do its work
      contentRenderer.render(gw, model);
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
      scrollToCurrent();
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
      super.setToggle(Images.imgOverview);
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      overview.setVisible(!overview.isVisible());
    }
  } //ActionOverview    

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
      ImageIcon i = null;
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
      scrollToCurrent();
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
      scrollToCurrent();
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
      scrollToCurrent();
    }
  } //ActionOrientation

} //TreeView
