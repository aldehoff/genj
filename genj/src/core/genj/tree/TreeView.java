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
import genj.renderer.EntityRenderer;
import genj.util.ActionDelegate;
import genj.util.ColorSet;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.PopupWidget;
import genj.util.swing.SliderWidget;
import genj.util.swing.UnitGraphics;
import genj.util.swing.ViewPortOverview;
import genj.view.ActionSupport;
import genj.view.ContextSupport;
import genj.view.FilterSupport;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;
import genj.window.WindowManager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
public class TreeView extends JPanel implements ContextSupport, ToolBarSupport, ActionSupport, FilterSupport {
  
  /** an icon for bookmarking */
  private final static ImageIcon BOOKMARK_ICON = new ImageIcon(TreeView.class, "images/Bookmark.gif");      

  /** need resources */
  private Resources resources = Resources.get(this);
  
  /** the units we use */
  private final Point DPI;
  private final Point2D DPMM;
  
  /** our model */
  private Model model;
  
  /** the manager */
  private ViewManager manager;

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
  private SliderWidget sliderZoom;  
  
  /** the title we have */
  private String title;
  
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
  
  /** our renderers */
  private EntityRenderer[] renderers = new EntityRenderer[Gedcom.NUM_TYPES];
  
  /** our content's font */
  private Font contentFont = new Font("SansSerif", 0, 12);
  
  /** current centered position */
  private Point2D.Double center = new Point2D.Double(0,0);

  /**
   * Constructor
   */
  public TreeView(String titl, Gedcom gedcm, Registry regIstry, ViewManager manAger) {
    
    // remember
    registry = regIstry;
    title = titl;
    manager = manAger;
    DPI = manager.getDPI();
    DPMM = new Point2D.Float(
      DPI.x / 2.54F / 10,
      DPI.y / 2.54F / 10
    );
    
    // grab colors
    colors = new ColorSet("content", Color.white, resources, registry);
    colors.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        repaint();
      }
    });
    colors.add("indis"  , Color.black);
    colors.add("fams"   , Color.darkGray);
    colors.add("arcs"   , Color.blue);
    colors.add("selects", Color.red);
    
    // grab font
    contentFont = registry.get("font", contentFont);
    isAdjustFonts = registry.get("adjust", isAdjustFonts);
    
    // grab blueprints
    blueprints = manager.getBlueprintManager().recallBlueprints(registry);
    
    // setup model
    model = new Model(gedcm);
    model.setVertical(registry.get("vertical",true));
    model.setFamilies(registry.get("families",true));
    model.setBendArcs(registry.get("bend"    ,true));
    model.setMarrSymbols(registry.get("marrs",true));
    TreeMetrics defm = model.getMetrics();
    model.setMetrics(new TreeMetrics(
      registry.get("windis",defm.wIndis),
      registry.get("hindis",defm.hIndis),
      registry.get("wfams" ,defm.wFams ),
      registry.get("hfams" ,defm.hFams ),
      registry.get("pad"   ,defm.pad   )
    ));
    isAntialiasing = registry.get("antial", false);
    model.setHideAncestorsIDs(registry.get("hide.ancestors", new ArrayList()));
    model.setHideDescendantsIDs(registry.get("hide.descendants", new ArrayList()));
 
    // root
    Entity root = null;
    try { 
      root = gedcm.getEntity(registry.get("root",(String)null));
    } catch (Exception e) {
    }
    if (root==null) {
      Property context = manager.getContext(gedcm);
      if (context!=null) root = context.getEntity();  
    } 
    model.setRoot(root);
    
    try { 
      currentEntity = gedcm.getEntity(registry.get("current",(String)null));
    } catch (Exception e) {
      currentEntity = model.getRoot();
    }
    
    // bookmarks
    String[] bs = registry.get("bookmarks", new String[0]);
    List bookmarks = new ArrayList();
    for (int i=0;i<bs.length;i++) {
      try {
        bookmarks.add(new Bookmark(this, gedcm, bs[i]));
      } catch (Throwable t) {
      }
    }
    model.setBookmarks(bookmarks);

    // setup child components
    contentRenderer = new ContentRenderer();
    content = new Content();
    JScrollPane scroll = new JScrollPane(content);
    overview = new Overview(scroll);
    overview.setVisible(registry.get("overview", false));
    overview.setSize(registry.get("overview", new Dimension(64,64)));
    zoom = Math.max(0.1, Math.min(1.0, registry.get("zoom", 1.0F)));
    
    // setup layout
    add(overview);
    add(scroll);
    
    // scroll to current
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        scrollToCurrent();
      }
    });
    
    // done
  }
  
  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(480,480);
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
    registry.put("zoom", (float)zoom);
    registry.put("vertical", model.isVertical());
    registry.put("families", model.isFamilies());
    registry.put("bend"    , model.isBendArcs());
    registry.put("marrs"   , model.isMarrSymbols());
    TreeMetrics m = model.getMetrics();
    registry.put("windis"  , m.wIndis);
    registry.put("hindis"  , m.hIndis);
    registry.put("wfams"   , m.wFams );
    registry.put("hfams"   , m.hFams );
    registry.put("pad"     , m.pad   );
    registry.put("antial"  , isAntialiasing );
    registry.put("font"    , contentFont);
    registry.put("adjust"  , isAdjustFonts);
    // blueprints
    manager.getBlueprintManager().rememberBlueprints(blueprints, registry);
    // root    
    if (model.getRoot()!=null) registry.put("root", model.getRoot().getId());
    if (currentEntity!=null) registry.put("current", currentEntity.getId());
    // bookmarks
    String[] bs = new String[model.getBookmarks().size()];
    Iterator it = model.getBookmarks().iterator();
    for (int b=0;it.hasNext();b++) {
      bs[b] = it.next().toString();
    }
    registry.put("bookmarks", bs);
    // stoppers
    registry.put("hide.ancestors"  , model.getHideAncestorsIDs());
    registry.put("hide.descendants", model.getHideDescendantsIDs());
    
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
    // reset renderers
    renderers = new EntityRenderer[Gedcom.NUM_TYPES];
    // show
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
    // change?
    if (contentFont.equals(set)) return;
    // remember
    contentFont = set;
    // reset renderers
    renderers = new EntityRenderer[Gedcom.NUM_TYPES];
    // show
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
    // grab 'em
    for (int i=0; i<set.length; i++) {
      blueprints[i] = set[i];
      renderers[i] = null;
    }
    // show
    repaint();
    // done
  }

  /**
   * Access - Mode
   */
  public Model getModel() {
    return model;
  }

  /**
   * @see genj.view.ContextPopupSupport#setContext(genj.gedcom.Property)
   */
  public void setContext(Property property) {
    // anything new?
    Entity entity = property.getEntity();
    if (entity==currentEntity) return;
    // allowed?
    if (!(entity instanceof Indi||entity instanceof Fam)) return;
    // Node for it?
    TreeNode node = model.getNode(entity);
    if (node==null) return;
    // remember
    currentEntity = entity;
    // scroll
    scrollTo(node.pos);
    // make sure it's reflected
    content.repaint();
    // done
  }
  
  /**
   * Scroll to given position   */
  private void scrollTo(Point p) {
    // remember
    center.setLocation(p);
    // scroll
    Rectangle2D b = model.getBounds();
    Dimension   d = getSize();
    content.scrollRectToVisible(new Rectangle(
      (int)( (p.getX()-b.getMinX()) * (DPMM.getX()*zoom) ) - d.width /2,
      (int)( (p.getY()-b.getMinY()) * (DPMM.getY()*zoom) ) - d.height/2,
      d.width ,
      d.height
    ));
    // done
  }
  
  /**
   * Scroll to current entity   */
  private void scrollToCurrent() {
    // fallback to root if current is not set
    if (currentEntity==null) 
      currentEntity=model.getRoot();
    // has to have something though
    if (currentEntity==null) 
      return;
    // Node for it?
    TreeNode node = model.getNode(currentEntity);
    if (node==null) {
      // hmm, retry with other
      currentEntity = null;
      scrollToCurrent();
      return;
    } 
    // scroll
    scrollTo(node.pos);
    // done    
  }
  
  
  /**
   * @see genj.view.ContextPopupSupport#getContextAt(Point)
   */
  public Context getContextAt(Point pos) {
    
    Point p = view2model(pos);
    Entity e = model.getEntityAt(p.x, p.y);
    if (e==null) return new Context(null);
    return new Context(e, Collections.singletonList(new ActionBookmark(e, true)));
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
    sliderZoom = new SliderWidget(1, 100, (int)(zoom*100));
    sliderZoom.addChangeListener(new ZoomGlue());
    sliderZoom.setAlignmentX(0F);
    bar.add(sliderZoom);
    
    // overview
    ButtonHelper bh = new ButtonHelper()
      .setContainer(bar)
      .setResources(resources)
      .setFocusable(false);
    bh.create(new ActionOverview())
      .setSelected(overview.isVisible());
    
    // gap
    bar.addSeparator();
    
    // vertical/horizontal
    bh.create(new ActionOrientation())
      .setSelected(model.isVertical());
    
    // families?
    bh.create(new ActionFamsAndSpouses())
      .setSelected(model.isFamilies());
      
    // toggless?
    bh.create(new ActionFoldSymbols())
      .setSelected(model.isFoldSymbols());
      
    // gap
    bar.addSeparator();
        
    // bookmarks
    PopupWidget pb = new PopupWidget("",BOOKMARK_ICON) {
      /**
       * @see genj.util.swing.PopupButton#getActions()
       */
      public List getActions() {
        return TreeView.this.model.getBookmarks();
      }
    };
    pb.setToolTipText(resources.getString("bookmark.tip"));
    bar.add(pb);
    
    // done
  }

  /**
   * @see genj.view.ContextSupport#createActions(genj.gedcom.Entity)
   */
  public List createActions(Entity entity, ViewManager manager) {
    // fam or indi?
    if (!(entity instanceof Indi||entity instanceof Fam)) 
      return null;
    // create an action for our tree
    List result = new ArrayList(2);
    result.add(new ActionRoot(entity));
    result.add(new ActionBookmark(entity, false));
    // done
    return result;
  }

  /**
   * @see genj.view.ContextSupport#createActions(genj.gedcom.Gedcom)
   */
  public List createActions(Gedcom gedcom, ViewManager manager) {
    return null;
  }

  /**
   * @see genj.view.ContextSupport#createActions(genj.gedcom.Property)
   */
  public List createActions(Property property, ViewManager manager) {
    return null;
  }

  /**
   * Sets the root of this view
   */
  public void setRoot(Entity root) {
    // allowed?
    if (!(root instanceof Indi||root instanceof Fam)) return;
    // make it current
    currentEntity = root;
    // keep it
    model.setRoot(root);
    // done
  }

  /**
   * Translate a view position into a model position
   */
  private Point view2model(Point pos) {
    Rectangle bounds = model.getBounds();
    return new Point(
      (int)Math.rint(pos.x / (DPMM.getX()*zoom) + bounds.getMinX()), 
      (int)Math.rint(pos.y / (DPMM.getY()*zoom) + bounds.getMinY())
    );
  }
  
  /**
   * Resolve a renderer   */
  /*package*/ EntityRenderer getEntityRenderer(int type) {
    EntityRenderer result = renderers[type];
    if (result==null) { 
      result = new EntityRenderer(blueprints[type], contentFont);
      result.setResolution(DPI);
      result.setScaleFonts(isAdjustFonts);
      renderers[type] = result;
    }
    return result;
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
  private class VisibleFilter implements Filter {
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
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return model.getEntities().size()+" nodes in "+title;
    }
  } //VisibleFilter

  /**
   * Overview   */
  private class Overview extends ViewPortOverview implements ModelListener {
    /**
     * Constructor     */
    private Overview(JScrollPane scroll) {
      super(scroll.getViewport());
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
     * @see java.awt.Panel#addNotify()
     */
    public void addNotify() {
      // listen
      model.addListener(this);
      // continue
      super.addNotify();
    }

    /**
     * @see java.awt.Container#removeNotify()
     */
    public void removeNotify() {
      // no listen
      model.removeListener(this);
      // continue
      super.removeNotify();
    }
    /**
     * @see genj.util.swing.ViewPortOverview#paintContent(java.awt.Graphics, double, double)
     */
    protected void renderContent(Graphics g, double zoomx, double zoomy) {

      // go 2d
      UnitGraphics gw = new UnitGraphics(g,DPMM.getX()*zoomx*zoom, DPMM.getY()*zoomy*zoom);
      
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
      addMouseListener(this);
    }
    
    /**
     * @see javax.swing.JComponent#addNotify()
     */
    public void addNotify() {
      model.addListener(this);
      // continue
      super.addNotify();
    }
    
    /**
     * @see javax.swing.JComponent#removeNotify()
     */
    public void removeNotify() {
      model.removeListener(this);
      // continue
      super.removeNotify();
    }

    /**
     * @see genj.tree.ModelListener#structureChanged(Model)
     */
    public void structureChanged(Model model) {
      // 20030403 dropped revalidate() here because it works
      // lazily - for scrolling to work the invalidate()/validate()
      // has to run synchronously and the component has to 
      // be layouted correctly. Then no intermediates are
      // painted and the scroll calculation is correct
      invalidate();
      TreeView.this.validate(); // note: call on parent
      // still shuffle a repaint
      repaint();
      // scrolling should work now
      scrollToCurrent();
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
        w = bounds.getWidth () * (DPMM.getX()*zoom),
        h = bounds.getHeight() * (DPMM.getY()*zoom);
      return new Dimension((int)w,(int)h);
    }
  
    /**
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paint(Graphics g) {
      // resolve our Graphics
      UnitGraphics gw = new UnitGraphics(g,DPMM.getX()*zoom, DPMM.getY()*zoom);
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
      // let the renderer do its work
      contentRenderer.render(gw, model);
      // done
    }
    
    /**
     * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
      // check node
      Point p = view2model(e.getPoint());
      Object content = model.getContentAt(p.x, p.y);
      // entity?
      if (content instanceof Entity) {
        // change current!
        currentEntity = (Entity)content;
        repaint();
        // propagate it
        manager.setContext(currentEntity);
      }
      // runnable?
      if (content instanceof Runnable) {
        ((Runnable)content).run();
      }
      // done
    }
    
    /**
     * @see java.awt.event.MouseAdapter#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
      // double click
      if (e.getClickCount()>1) {
        Point p = view2model(e.getPoint());
        Object content = model.getContentAt(p.x, p.y);
        if (content instanceof Entity)
          setRoot((Entity)content);
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
      zoom = sliderZoom.getValue()*0.01D;
      content.invalidate();
      TreeView.this.validate();
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
      setText(resources.getString("root",title));
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
  } //ActionFamsAndSpouses

  /**
   * Action FoldSymbols on/off
   */
  private class ActionFoldSymbols extends ActionDelegate {
    /**
     * Constructor
     */
    private ActionFoldSymbols() {
      super.setImage(Images.imgFoldSymbols);
      super.setToggle(Images.imgFoldSymbols);
      super.setTip("foldsymbols.tip");
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      model.setFoldSymbols(!model.isFoldSymbols());
      scrollToCurrent();
    }
  } //ActionFolding

  /**
   * Action - bookmark something
   */
  private class ActionBookmark extends ActionDelegate {
    /** the entity */
    private Entity entity;
    /** 
     * Constructor 
     */
    private ActionBookmark(Entity e, boolean local) {
      entity = e;
      if (local) {
        setText(resources.getString("bookmark.add"));
        setImage(BOOKMARK_ICON);
      } else {
        setText(resources.getString("bookmark.in",title));
        setImage(Images.imgView);
      }
    } 
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      
      // calculate a name
      String name = "";
      if (entity instanceof Indi) {
        name = ((Indi)entity).getName();
      }
      if (entity instanceof Fam) {
        Indi husb = ((Fam)entity).getHusband();
        Indi wife = ((Fam)entity).getWife();
        if (husb!=null&&wife!=null) name = husb.getName() + " & " + wife.getName();
      }
      
      // Ask for name of bookmark
      name = manager.getWindowManager().openDialog(
        null, title, WindowManager.IMG_QUESTION, resources.getString("bookmark.name"), name, TreeView.this
      );
      
      if (name==null) return;
      
      // create it
      model.addBookmark(new Bookmark(TreeView.this, name, entity));
      
      // done
    }
  
  } //ActionBookmark

} //TreeView
