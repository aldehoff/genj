/**
 * GraphJ
 * 
 * Copyright (C) 2002 Nils Meier
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package gj.shell;

import gj.awt.geom.Dimension2D;
import gj.awt.geom.Path;
import gj.awt.geom.ShapeHelper;
import gj.layout.Layout;
import gj.layout.PathHelper;
import gj.model.Arc;
import gj.model.MutableGraph;
import gj.model.Node;
import gj.shell.swing.SwingHelper;
import gj.shell.swing.UnifiedAction;
import gj.shell.util.ReflectHelper;
import gj.ui.DefaultGraphRenderer;
import gj.ui.LayoutRenderer;
import gj.ui.UnitGraphics;
import gj.util.ArcIterator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

/**
 * Displaying a Graph with user input
 */
public class GraphWidget extends JPanel {
  
  /** the Shapes we know */
  protected Shape[] shapes = new Shape[] {
    new Rectangle2D.Double(-16,-16,32,32),
    ShapeHelper.createShape(23D,19D,1,1,new double[]{
      0,6,4,1,18,11,1,29,1,1,34,30,1,47,27,1,31,44,1,20,24,1,6,38,1,2,21,1,14,20,1,6,4
    }),
    ShapeHelper.createShape(15D,15D,1,1,new double[] {
      0,10,0,
      1,20,0,
       2,30,0,30,10, // tr : quad
      1,30,20,
       2,20,20,20,30, // br : quad
      1,10,30,
       3,20,20,10,10,0,20, // bl : cubic
      1,0,10,
       3,-10,0,0,-10,10,0 // tl : cubic
    })
  };
  
  /** the graph we're displaying */
  private MutableGraph graph;
  
  /** the content for the graph */
  private Content content = new Content();
  
  /** the renderer we're using */
  private DefaultGraphRenderer graphRenderer = new DefaultGraphRenderer();
  
  /** more renderers */
  private LayoutRenderer layoutRenderer = null;
  
  /** the lastly selected element */
  private Node lastSelection = null;
  
  /** the last moust event */
  private MouseEvent lastEvent = null;
  
  /** whether quicknode is enabled */
  private boolean quickNode = false;
  
  /** whether antialiasing is on */
  private boolean isAntialiasing = true;
  
  /** our mouse analyzers */
  private DnD
    dndNoOp = new DnDIdle(),
    dndMoveNode = new DnDMoveNode(),
    dndCreateArc = new DnDCreateArc(),
    dndResizeNode = new DnDResizeNode(),
    dndCurrent = null;
    
  /** our popups */
  private JPopupMenu
    pmNode,
    pmCanvas;
    
  /** a layout's menu */
  private JMenu mLayout;    
    
  /** a layout we know about */
  private Layout currentLayout;
    
  /**
   * Constructor
   */
  public GraphWidget() {
    
    // Create the popups
    pmNode = new JPopupMenu();
    pmNode.add(new ActionResizeNode());
    pmNode.add(new ActionDeleteNode());
    pmNode.add(new ActionCreateArc());
    pmNode.add(new ActionSetNodeContent());
    JMenu mShape = new JMenu("Set Shape");
    for (int i=0;i<shapes.length;i++)
      mShape.add(new ShapeMenuItemWidget(shapes[i], new ActionSetNodeShape(shapes[i])));
    pmNode.add(mShape);
    mLayout = new JMenu();
    mLayout.setVisible(false);
    pmNode.add(mLayout);
    
    pmCanvas = new JPopupMenu();
    pmCanvas.add(new ActionCreateNode());
    pmCanvas.add(SwingHelper.getCheckBoxMenuItem(new ActionToggleQuickNode()));
    
    // Layout
    setLayout(new BorderLayout());
    add(new JScrollPane(content), BorderLayout.CENTER);
    
    // Done
  }
  
  /**
   * Accessor - Graph
   */
  public void setGraph(MutableGraph set) {
    
    // cleanup data
    graph = set;
    lastSelection = null;
    
    // make sure that's reflected
    revalidate();
    
    // start fresh with DnD
    dndNoOp.start(null);
    
    // done
  }
  
  /**
   * Accessor - Antialiasing enabled or not
   */
  public boolean isAntialiasing() {
    return isAntialiasing;
  }

  /**
   * Accessor - Antialiasing enabled or not
   */
  public void setAntialiasing(boolean set) {
    isAntialiasing=set;
    repaint();
  }
  
  /**
   *  Creates a node at given position in canvase
   */
  public void createNode(double x, double y, Object object) {
    
    // let the graph do it
    graph.createNode(
      new Point2D.Double(x,y),
      shapes[0],
      object
    );
    
    // show it
    repaint();
  }
  
  /**
   * @see JComponent#revalidate()
   */
  public void revalidate() {
    if (content!=null) content.revalidate();
    super.revalidate();
    repaint();
  }
  
  /**
   * Accessor - the current selection
   */
  private Node getSelection() {
    return lastSelection;
  }
  
  /** 
   * Accessor - the current layout
   */
  public void setCurrentLayout(Layout layout) {
    currentLayout = layout;
    layoutRenderer = getRenderer(layout);
    repaint();
  }
  
  /**
   * Helper that calculates a Renderer for given Layout
   */
  private LayoutRenderer getRenderer(Layout instance) {
    if (instance==null) return null;
    try {
      return (LayoutRenderer)ReflectHelper.getInstance(instance.getClass().getName()+"Renderer", LayoutRenderer.class);
    } catch (Throwable t) {
      return null;
    }
  }
  
  /**
   * Returns the popup for nodes
   */
  private JPopupMenu getNodePopupMenu() {
    
    // do we have a a layout with input for the node's menu?
    if (currentLayout==null) {
      // fallback to invisible
      mLayout.setVisible(false);
      // done
      return pmNode;
    }
    
    // collect public setters(Node)
    Method[] actions = ReflectHelper.getMethods(currentLayout, "", new Class[]{ Node.class});
    if (actions.length>0) {
      mLayout.removeAll();
      // add add actions
      for (int a=0; a<actions.length; a++) {
        mLayout.add(new ActionNodeLayoutMethod(actions[a]));
      }
      // visible now
      mLayout.setVisible(true);
      mLayout.setText(currentLayout.toString());
      return pmNode;
    }

    // done
    return pmNode;    
  }
  
  /**
   * Returns the popup for canvas
   */
  private JPopupMenu getCanvasPopupMenu() {
    return pmCanvas;
  }

  /**
   * Mouse Analyzer
   */
  private abstract class DnD extends MouseAdapter implements MouseMotionListener {
    /** start */
    public void start(MouseEvent e) {
      // already someone there?
      if (dndCurrent!=null) {
        content.removeMouseListener(dndCurrent);
        content.removeMouseMotionListener(dndCurrent);
      }
      // switch
      dndCurrent = this;
      // start to listen
      content.addMouseListener(dndCurrent);
      content.addMouseMotionListener(dndCurrent);
      // done
    }
    /** callback - node has changed */
    protected void nodeChanged(Node node) {
      // update it's arcs
      ArcIterator it = new ArcIterator(node);
      while (it.next()) {
        Node 
          from = it.arc.getStart(),
          to = it.arc.getEnd();
        PathHelper.update(
          it.arc.getPath(),
          from.getPosition(), from.getShape(), 
          to.getPosition(), to.getShape(),
          it.i,from!=node
        );
      }
      // show it
      repaint();
    }
    /** callback */
    public void mousePressed(MouseEvent e) {}
    /** callback */
    public void mouseReleased(MouseEvent e) {}
    /** callback */
    public void mouseDragged(MouseEvent e) {}
    /** callback */
    public void mouseMoved(MouseEvent e) {}
    /** paint */
    public void paint(UnitGraphics gg) {}
    /**
     * Tries to find an element by coordinate
     */
    protected Node getElement(double x, double y) {
      // try to find a node
      Point2D pos = null;
      Iterator nodes = graph.getNodes().iterator();
      while (nodes.hasNext()) {
        Node node = (Node)nodes.next();
        pos = node.getPosition();
        if (node.getShape().contains(x-pos.getX(),y-pos.getY())) {
          return node;
        }
      }
      // try to find an arc
      // TODO
      // nothing found
      return null;    
    }
    
  } // EOC
  
  /**
   * Mouse Analyzer - Waiting
   */
  private class DnDIdle extends DnD {
    /** start */
    public void start(Node node, MouseEvent e) {
      super.start(e);
    }
    /** callback */
    public void mousePressed(MouseEvent e) {
      // nothing to do?
      if (graph==null) return;
      // something there?
      Node node = getElement(content.getX(e), content.getY(e));
      if (node==null) {
        lastSelection = null;
        return;
      }
      // new?
      if (node!=lastSelection) {
        lastSelection = node;
        repaint();
      }
      // start dragging?
      if ((e.getModifiers()&e.BUTTON1_MASK)!=0) {
      //if (e.getButton()==e.BUTTON1) {
        dndMoveNode.start(e);
      }
      // done
    }
    /** callback */
    public void mouseReleased(MouseEvent e) {
      if ((e.getModifiers()&e.BUTTON3_MASK)!=0) {
      //if (e.getButton()==e.BUTTON3) {
        // popup
        lastEvent = e;
        JPopupMenu menu = lastSelection!=null ? getNodePopupMenu() : getCanvasPopupMenu();
        menu.show(content,e.getX(),e.getY());
        return;
      }
      if ((e.getModifiers()&e.BUTTON1_MASK)!=0) {
      //if (e.getButton()==e.BUTTON1) {
        if (quickNode) createNode(content.getX(e), content.getY(e),""+(graph.getNodes().size()+1));
        return;
      }
    }
  } // EOC
  
  /**
   * Mouse Analyzer - Drag a Node
   */
  private class DnDMoveNode extends DnD {
    private Node node;
    private Point2D.Double offset = new Point2D.Double();
    /** start */
    public void start(MouseEvent e) {
      super.start(e);
      node = getElement(content.getX(e), content.getY(e));
      offset.setLocation(
        e.getX() - node.getPosition().getX(),
        e.getY() - node.getPosition().getY()
      );
    }
    /** callback */
    public void mouseReleased(MouseEvent e) {
      dndNoOp.start(e);
    }
    /** callback */
    public void mouseDragged(MouseEvent e) {
      // move the node
      node.getPosition().setLocation(
        e.getX() - offset.getX(),
        e.getY() - offset.getY()
      );
      // update after change of a Node
      nodeChanged(node);
    }
  } // EOC
  
  /**
   * Mouse Analyzer - Create an Arc
   */
  private class DnDCreateArc extends DnD implements Arc {
    private Node from;
    private Point2D to = new Point2D.Double();
    /** start */
    public void start(MouseEvent e) {
      super.start(e);
      from = lastSelection;
    }
    /** callback */
    public void mouseReleased(MouseEvent e) {
      Node to = getElement(content.getX(e), content.getY(e));
      if (to!=null) {
        Arc arc = graph.createArc(from, to, new Path());
        PathHelper.update(arc.getPath(), from.getPosition(), from.getShape(), to.getPosition(), to.getShape(), 0,false);
      }
      repaint();
      dndNoOp.start(e);
    }
    /** callback */
    public void mouseMoved(MouseEvent e) {
      to.setLocation(content.getX(e), content.getY(e));
      // show it
      repaint();
    }
    /** paint */
    public void paint(UnitGraphics gg) {
      graphRenderer.renderArc(this, gg);
    }
    /** @see gj.model.Arc#getStart() */
    public Node getStart() {
      return from;
    }
    /** @see gj.model.Arc#getEnd() */
    public Node getEnd() {
      return from;
    }
    /** @see gj.model.Arc#getPath() */
    public Path getPath() {
      return PathHelper.update(new Path(), from.getPosition(),from.getShape(),to,null);
    }
  } // EOC

  /**
   * Mouse Analyzer - Resize a Node
   */
  private class DnDResizeNode extends DnD implements Node {
    /** our state */
    private Point2D.Double pos = new Point2D.Double();
    private Dimension2D.Double dim = new Dimension2D.Double();
    private Point2D.Double fac = new Point2D.Double();
    /** start */
    public void start(MouseEvent e) {
      super.start(e);
      pos.setLocation(lastSelection.getPosition());
      dim.setSize(lastSelection.getShape().getBounds2D());
      fac.setLocation(0,0);
    }
    /** callback */
    public void mouseReleased(MouseEvent e) {
      dndNoOp.start(e);
      graph.setShape(lastSelection,getShape());
      // update after change of a Node
      nodeChanged(lastSelection);
    }
    /** callback */
    public void mouseMoved(MouseEvent e) {
      fac.setLocation(
        Math.max(0.1,Math.abs(content.getX(e)-pos.x)/dim.w*2),
        Math.max(0.1,Math.abs(content.getY(e)-pos.y)/dim.h*2)
      );
      // show it
      repaint();
    }
    /** paint */
    public void paint(UnitGraphics gg) {
      graphRenderer.renderNode(this,gg);
    }
    /** @see gj.model.Node#getArcs() */
    public List getArcs() {
      return new ArrayList();
    }
    /** @see gj.model.Node#getContent() */
    public Object getContent() {
      return null;
    }
    /** @see gj.model.Node#getPosition() */
    public Point2D getPosition() {
      return pos;
    }
    /** @see gj.model.Node#getShape() */
    public Shape getShape() {
      GeneralPath gp = new GeneralPath(lastSelection.getShape());
      gp.transform(AffineTransform.getScaleInstance(fac.x,fac.y));
      return gp;
    }
  } // EOC

  /**
   * How to handle - Delete a Node
   */
  private class ActionDeleteNode extends UnifiedAction {
    protected ActionDeleteNode() { super("Delete"); }
    protected void execute() {
      int i = SwingHelper.showDialog(GraphWidget.this, "Delete Node", "Are you sure?", SwingHelper.DLG_YES_NO);
      if (SwingHelper.OPTION_YES!=i) return;
      graph.removeNode(lastSelection);
      lastSelection=null;
      repaint();
    }
  }

  /**
   * How to handle - Sets a Node's Shape
   */
  private class ActionSetNodeShape extends UnifiedAction {
    Shape shape;
    protected ActionSetNodeShape(Shape set) {
      shape = set;
    }
    protected void execute() {
      graph.setShape(lastSelection,shape);
      //dndResizeNode.start(null);
      dndResizeNode.nodeChanged(lastSelection);
    }
  }

  /**
   * How to handle - Creats an arc
   */
  private class ActionCreateArc extends UnifiedAction {
    protected ActionCreateArc() { super("Arc to"); }
    protected void execute() { dndCreateArc.start(null);  }
  }
  
  /**
   * How to handle - Change node content
   */
  private class ActionSetNodeContent extends UnifiedAction {
    protected ActionSetNodeContent() { super("Set content"); }
    protected void execute() {
      String txt = SwingHelper.showDialog(GraphWidget.this, "Set content", "Please enter text here:");
      if (txt==null) return;
      graph.setContent(lastSelection,txt);
      repaint();
    }
  }

  /**
   * How to handle - Change node size
   */
  private class ActionResizeNode extends UnifiedAction {
    protected ActionResizeNode() { super("Resize"); }
    protected void execute() { dndResizeNode.start(null); }
  }
  
  /**
   * How to handle - Create a node
   */
  private class ActionCreateNode extends UnifiedAction {
    protected ActionCreateNode() { super("Create node"); }
    protected void execute() {
      String txt = SwingHelper.showDialog(GraphWidget.this, "Set content", "Please enter text here:");
      if (txt==null) return;
      createNode(content.getX(lastEvent), content.getY(lastEvent), txt);
    }
  }

  /**
   * How to handle - Toggle quick node
   */
  private class ActionToggleQuickNode extends UnifiedAction {
    protected ActionToggleQuickNode() { super("QuickNode"); }
    protected void execute() { quickNode=!quickNode; }
    public boolean isSelected() { return quickNode; }
  }
  
  /**
   * How to handle - NodePopupProvider's Action
   */
  private class ActionNodeLayoutMethod extends UnifiedAction {
    private Method method;
    protected ActionNodeLayoutMethod(Method method) { 
      super.setName(method.getName()+"()"); 
      this.method = method;
    }
    protected void execute() { 
      try {
        method.invoke(currentLayout, new Object[]{ getSelection()});
      } catch (Exception e) {}
      repaint();
    }
  }
  
  /**
   * The content we use for drawing
   */
  private class Content extends JComponent {

    /**
     * Constructor
     */
    private Content() {
    } 
    
    /**
     * transform MouseEvent into user space
     */
    private double getX(MouseEvent e) {
      return e.getX() - getXOffset();
    }
    
    /**
     * transform MouseEvent into user space
     */
    private double getY(MouseEvent e) {
      return e.getY() - getYOffset();
    }
    
    /**
     * Calculate x offset for centered graph
     */
    private int getXOffset() {
      if (graph==null) return 0;
      Rectangle2D bounds = graph.getBounds();
      return (int)(-bounds.getX()+(getWidth()-bounds.getWidth())/2);
    }
    
    /**
     * Calculate y offset for centered graph
     */
    private int getYOffset() {
      if (graph==null) return 0;
      Rectangle2D bounds = graph.getBounds();
      return (int)(-bounds.getY()+(getHeight()-bounds.getHeight())/2);
    }
    
    /**
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
      if (graph==null) return new Dimension();
      return new Dimension(
        (int)Math.ceil(graph.getBounds().getWidth()),
        (int)Math.ceil(graph.getBounds().getHeight())
      );
    }
  
    /**
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    protected void paintComponent(Graphics g) {
      // clear background
      g.setColor(Color.white);
      g.fillRect(0,0,getWidth(),getHeight());
      // synchronize on graph and go?
      if (graph==null) return;
      synchronized (graph) {
        // create our working graphics
        UnitGraphics graphics = new UnitGraphics(g, 1.0D, 1.0D);
        graphics.setAntialiasing(isAntialiasing);
        // paint at 0,0
        graphics.translate(getXOffset(),getYOffset());
        // LayoutRenderer?
        if (layoutRenderer!=null) 
          layoutRenderer.render(graph,currentLayout,graphics);
        // let the renderer do its work
        graphRenderer.render(graph, currentLayout, graphics);
        // done
        // Is the a current to render?
        if (lastSelection!=null) {
          graphics.setColor(Color.blue);
          graphics.draw(
            lastSelection.getShape(), 
            lastSelection.getPosition().getX(), 
            lastSelection.getPosition().getY(),
            1,1,0,false
          );
        }
        // And let the MouseAnalyzer do what it needs to do
        if (dndCurrent!=null)
          dndCurrent.paint(graphics);
      }
      // done
    }
    
  } //Content
  
} //GraphWidget
