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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import gj.awt.geom.Dimension2D;
import gj.awt.geom.ShapeHelper;
import gj.awt.geom.Path;

import gj.layout.Layout;
import gj.layout.PathHelper;
import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;
import gj.model.MutableGraph;

import gj.shell.swing.*;
import gj.shell.util.ReflectHelper;

import gj.ui.GraphGraphics;
import gj.ui.DefaultGraphRenderer;
import gj.ui.GraphRenderer;

import gj.util.ArcIterator;

/**
 * Displaying a Graph with user input
 */
public class GraphWidget extends JComponent {
  
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
  
  /** the origin of the graph */
  private Point2D.Double origin = new Point2D.Double(0,0);

  /** the graph we're displaying */
  private MutableGraph graph;
  
  /** the renderer we're using */
  private GraphRenderer graphRenderer = new DefaultGraphRenderer();
  
  /** more renderers */
  private GraphRenderer layoutRenderer = null;
  
  /** the lastly selected element */
  private Node lastSelection = null;
  
  /** the lastly clicked position */
  private Point2D lastPosition = null;
  
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
    
    // We want to know when the size changes
    this.addComponentListener(new ComponentAdapter() {
      /**
       * @see ComponentListener#componentResized(ComponentEvent)
       */
      public void componentResized(ComponentEvent e) {
        revalidate();
      }
    });
    
    // Done
  }
  
  /**
   * Callback - painting
   */
  public void paintComponent(Graphics g) {
    
    // No graph
    if (graph==null) {
      // Clear the background 
      g.setColor(Color.GRAY);
      g.fillRect(0, 0, getSize().width, getSize().height);
      // Done
      return;
    }
    
    // Convert g->graphics
    Graphics2D graphics = SwingHelper.getGraphics2D(g,isAntialiasing());
    
    // Clear the background
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0,0,getSize().width, getSize().height);
      
    synchronized (graph) {
    
      // Move the origin 0,0 to 64,64
      graphics.translate(origin.getX(),origin.getY());
      
      // Create a GraphGraphics
      GraphGraphics gg = new GraphGraphics(graphics);
      
      // LayoutRenderer?
      if (layoutRenderer!=null) {
        layoutRenderer.render(graph,currentLayout,gg);
      }
      
      // Render the graph
      graphRenderer.render(graph, currentLayout, gg);
      
      // Is the a current to render?
      if (lastSelection!=null) {
        gg.setColor(Color.BLUE);
        gg.draw(
          lastSelection.getShape(), 
          lastSelection.getPosition().getX(), 
          lastSelection.getPosition().getY(),
          1,1,0,false
        );
      }
      
      // And let the MouseAnalyzer do what it needs to do
      if (dndCurrent!=null)
        dndCurrent.paint(gg);

    }
        
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
  public void createNode(Point2D position, Object content) {
    
    // let the graph do it
    graph.createNode(
      new Point2D.Double(position.getX()-origin.getX(),position.getY()-origin.getY()),
      shapes[0],
      content
    );
    
    // show it
    repaint();
  }
  
  /**
   * @see Component#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    int w=128,h=128;
    if (graph!=null) {
      w = (int)Math.ceil(graph.getBounds().getWidth()+1);
      h = (int)Math.ceil(graph.getBounds().getHeight()+1);
    }
    return new Dimension(w,h);
  }

  /**
   * @see JComponent#revalidate()
   */
  public void revalidate() {
    // set the origin
    if (graph!=null) {
      synchronized (graph) {
        origin.setLocation(
          -graph.getBounds().getX() + (getWidth()-graph.getBounds().getWidth())/2,
          -graph.getBounds().getY() + (getHeight()-graph.getBounds().getHeight())/2
        );
      }
    }
    // display
    repaint();
    super.revalidate();
  }
  
  /**
   * Accessor - the current selection
   */
  public Node getSelection() {
    return lastSelection;
  }
  
  /** 
   * Accessor - the current layout
   */
  public void setCurrentLayout(Layout layout) {
    currentLayout = layout;
    layoutRenderer = getRenderer(layout);
    revalidate();
  }
  
  /**
   * Helper that calculates a Renderer for given Layout
   */
  private GraphRenderer getRenderer(Layout instance) {
    if (instance==null) return null;
    try {
      return (GraphRenderer)ReflectHelper.getInstance(instance.getClass().getName()+"Renderer", GraphRenderer.class);
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
        removeMouseListener(dndCurrent);
        removeMouseMotionListener(dndCurrent);
      }
      // switch
      dndCurrent = this;
      // start to listen
      addMouseListener(dndCurrent);
      addMouseMotionListener(dndCurrent);
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
    public void paint(GraphGraphics gg) {}
    /**
     * Tries to find an element by coordinate
     */
    protected Node getElement(int x, int y) {
      // adjusting the position to graph-space
      x -= origin.getX();
      y -= origin.getY();
      // try to find a node
      Point2D selectionPosition = null;
      Iterator nodes = graph.getNodes().iterator();
      while (nodes.hasNext()) {
        Node node = (Node)nodes.next();
        selectionPosition = node.getPosition();
        if (node.getShape().contains(x-selectionPosition.getX(),y-selectionPosition.getY())) {
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
      Node node = getElement(e.getX(), e.getY());
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
      if (e.getButton()==e.BUTTON1) {
        dndMoveNode.start(e);
      }
      // done
    }
    /** callback */
    public void mouseReleased(MouseEvent e) {
      switch (e.getButton()) {
        case MouseEvent.BUTTON3:
          // popup
          lastPosition = e.getPoint();
          JPopupMenu menu = lastSelection!=null ? getNodePopupMenu() : getCanvasPopupMenu();
          menu.show(GraphWidget.this,e.getX(),e.getY());
          // done
          break;
        case MouseEvent.BUTTON1:
          // quick node
          if (quickNode) createNode(e.getPoint(),""+(graph.getNodes().size()+1));
          // done
          break;
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
      node = getElement(e.getX(),e.getY());
      offset.setLocation(
        e.getX() - origin.getX() - node.getPosition().getX(),
        e.getY() - origin.getY() - node.getPosition().getY()
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
        e.getX() - origin.getX() - offset.getX(),
        e.getY() - origin.getY() - offset.getY()
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
      Node to = getElement(e.getX(), e.getY());
      if (to!=null) {
        Arc arc = graph.createArc(from, to, new Path());
        PathHelper.update(arc.getPath(), from.getPosition(), from.getShape(), to.getPosition(), to.getShape(), 0,false);
      }
      repaint();
      dndNoOp.start(e);
    }
    /** callback */
    public void mouseMoved(MouseEvent e) {
      to.setLocation(
        e.getX()-origin.getX(), 
        e.getY()-origin.getY()
      );
      // show it
      repaint();
    }
    /** paint */
    public void paint(GraphGraphics gg) {
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
        Math.max(0.1,Math.abs(e.getX()-origin.x-pos.x)/dim.w*2),
        Math.max(0.1,Math.abs(e.getY()-origin.y-pos.y)/dim.h*2)
      );
      // show it
      repaint();
    }
    /** paint */
    public void paint(GraphGraphics gg) {
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
      dndResizeNode.start(null);
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
      createNode(lastPosition, txt);
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
  
}
