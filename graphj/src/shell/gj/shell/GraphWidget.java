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

import gj.awt.geom.Geometry;
import gj.awt.geom.ShapeHelper;
import gj.layout.Layout;
import gj.layout.LayoutRenderer;
import gj.shell.model.ShellGraph;
import gj.shell.model.ShellNode;
import gj.shell.swing.SwingHelper;
import gj.shell.swing.UnifiedAction;
import gj.shell.util.ReflectHelper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Method;

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
  private ShellGraph graph;
  
  /** the size of the graph */
  private Rectangle graphBounds;
  
  /** the content for the graph */
  private Content content = new Content();
  
  /** more renderers */
  private LayoutRenderer layoutRenderer = null;
  
  /** the handle on current selection */
  private ShellNode selection = null;
  
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
    
  /** a layout we know about */
  private Layout currentLayout;
    
  /** the renderer we're using */
  private GraphRenderer graphRenderer = new GraphRenderer() {
    protected Color getColor(ShellNode node) {
      return selection==node ? Color.blue : Color.black; 
    }
  };
  
  /**
   * Constructor
   */
  public GraphWidget() {
    
    // Layout
    setLayout(new BorderLayout());
    add(new JScrollPane(content), BorderLayout.CENTER);
    
    // Done
  }
  
  /**
   * Accessor - Graph
   */
  public void setGraph(ShellGraph setGraph, Rectangle setBounds) {
    
    // cleanup data
    graph = setGraph;
    graphBounds = setBounds;
    selection = null;
    
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
   * @see JComponent#revalidate()
   */
  public void revalidate() {
    if (content!=null) content.revalidate();
    super.revalidate();
    repaint();
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
  private JPopupMenu getNodePopupMenu(ShellNode node, Point pos) {

    // Create the popups
    JPopupMenu result = new JPopupMenu();
    if (selection.getContainedGraph()!=null)
      result.add(new ActionCreateNode(selection.getContainedGraph(), Geometry.sub(graph.getPosition(node), content.getPoint(pos)) ));
    result.add(new ActionResizeNode());
    result.add(new ActionDeleteNode());
    result.add(new ActionSetNodeContent());
    JMenu mShape = new JMenu("Set Shape");
    for (int i=0;i<shapes.length;i++)
      mShape.add(new ShapeMenuItemWidget(shapes[i], new ActionSetNodeShape(shapes[i])));
    result.add(mShape);

    // do we have a a layout with input for the node's menu?
    if (currentLayout==null) 
      return result;
      
    // continue with menu's submenu
    JMenu mLayout = new JMenu();
    
    // collect public setters(Node)
    Method[] actions = ReflectHelper.getMethods(currentLayout, "", new Class[]{ gj.model.Node.class});
    if (actions.length>0) {
      // add add actions
      for (int a=0; a<actions.length; a++) {
        mLayout.add(new ActionNodeLayoutMethod(actions[a]));
      }
      // visible now
      mLayout.setText(currentLayout.toString());
      result.add(mLayout);
    }

    // done
    return result;    
  }
  
  /**
   * Returns the popup for canvas
   */
  private JPopupMenu getCanvasPopupMenu(Point pos) {
    
    JPopupMenu result = new JPopupMenu();
    result.add(new ActionCreateNode(graph, content.getPoint(pos)));
    result.add(SwingHelper.getCheckBoxMenuItem(new ActionToggleQuickNode()));
    return result;
  }

  /**
   * Mouse Analyzer
   */
  private abstract class DnD extends MouseAdapter implements MouseMotionListener {
    /** start */
    protected void start(Point p) {
      // already someone there?
      if (dndCurrent!=null) {
        dndCurrent.stop();
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
    /** stop */
    protected void stop() { }
    /** callback */
    public void mousePressed(MouseEvent e) {}
    /** callback */
    public void mouseReleased(MouseEvent e) {}
    /** callback */
    public void mouseDragged(MouseEvent e) {}
    /** callback */
    public void mouseMoved(MouseEvent e) {}
  } // EOC
  
  /**
   * Mouse Analyzer - Waiting
   */
  private class DnDIdle extends DnD {
    /** callback */
    public void mousePressed(MouseEvent e) {
      // nothing to do?
      if (graph==null) return;
      // something there?
      ShellNode oldSelection = selection;
      selection = graph.getNode(content.getPoint(e.getPoint()));
      // popup?
      if (e.isPopupTrigger()) {
        popup(e.getPoint());
        return;
      }
      // start dragging?
      if (e.getButton()==e.BUTTON1&&selection!=null) {
        if (oldSelection==selection)
          dndMoveNode.start(e.getPoint());
        else
          dndCreateArc.start(e.getPoint());
      }
      // always show
      repaint();
      // done
    }
    /** callback */
    public void mouseReleased(MouseEvent e) {
      
      // context menu?
      if (e.isPopupTrigger()) {
        popup(e.getPoint());
        return;
      }
      
      // quick create?
      if (quickNode) {
        graph.createNode(shapes[0], ""+(graph.getNodes().size()+1), content.getPoint(e.getPoint()));
        repaint();
        return;
      }
      // done
    }
    /** popup */
    private void popup(Point at) {
      JPopupMenu menu = selection!=null ? getNodePopupMenu(selection, at) : getCanvasPopupMenu(at);
      menu.show(content,at.x,at.y);
    }
  } //DnDIdle
  
  /**
   * Mouse Analyzer - Drag a Node
   */
  private class DnDMoveNode extends DnD {
    Point from;
    /** start */
    protected void start(Point at) {
      super.start(at);
      from = at;
    }
    /** callback */
    public void mouseReleased(MouseEvent e) {
      dndNoOp.start(e.getPoint());
    }
    /** callback */
    public void mouseDragged(MouseEvent e) {
      // move the selected
      selection.translate(Geometry.sub(from, e.getPoint()));
      from = e.getPoint();
      // show
      repaint();
    }
  } //DnDMoveNode
  
  /**
   * Mouse Analyzer - Create an Arc
   */
  private class DnDCreateArc extends DnD{
    /** the from node */
    private ShellNode from;
    /** a dummy to */
    private ShellNode dummy;
    /** start */
    protected void start(Point at) {
      super.start(at);
      from = selection;
      dummy = null;
      selection = null;
    }
    /** stop */
    protected void stop() {
      // delete dummy which will also delete the arc
      if (dummy!=null) 
        dummy.delete();
      // done
    }
    /** callback */
    public void mouseReleased(MouseEvent e) {
      // make sure we're stopped
      stop();
      // selection made? create arc!
      if (selection!=null) {
        selection.getGraph().createArc(from, selection);
      }
      selection = from;
      // show
      repaint();
      // continue
      dndNoOp.start(e.getPoint());
    }
    /** callback */
    public void mouseDragged(MouseEvent e) {
      // not really dragging yet?
      if (dummy==null) {
        // user has to move mouse outside of shape first (avoid loops)
        if (graph.getNode(content.getPoint(e.getPoint()))==from)
          return;
        // create dummies
        dummy = from.getGraph().createNode(null, null);      
        from.getGraph().createArc(from, dummy);
      }
      // place dummy (tip of arc)
      graph.setPosition(dummy, content.getPoint(e.getPoint()));
      // find target
      selection = graph.getNode(content.getPoint(e.getPoint()));
      if (selection!=null&&selection.getGraph()!=from.getGraph()) {
        selection = null;
      }
      // show
      repaint();
    }
  } //DnDCreateArc

  /**
   * Mouse Analyzer - Resize a Node
   */
  private class DnDResizeNode extends DnD {
    /** our status */
    private Shape shape;
    private Dimension dim;
    /** start */
    protected void start(Point pos) {
      super.start(pos);
      // remember
      shape = selection.getShape();
      dim = shape.getBounds().getSize();
    }
    /** callback */
    public void mouseReleased(MouseEvent e) {
      dndNoOp.start(e.getPoint());
    }
    /** callback */
    public void mouseMoved(MouseEvent e) {

      // change shape
      Point2D delta = Geometry.sub(graph.getPosition(selection), content.getPoint(e.getPoint()));
      double 
        sx = Math.max(0.1,Math.abs(delta.getX())/dim.width *2),
        sy = Math.max(0.1,Math.abs(delta.getY())/dim.height*2);

      GeneralPath gp = new GeneralPath(shape);
      gp.transform(AffineTransform.getScaleInstance(sx, sy));
      selection.setShape(gp);
      
      // show it
      repaint();
    }
  } //DnDResizeNode

  /**
   * How to handle - Delete a Node
   */
  private class ActionDeleteNode extends UnifiedAction {
    protected ActionDeleteNode() { super("Delete"); }
    protected void execute() {
      int i = SwingHelper.showDialog(GraphWidget.this, "Delete Node", "Are you sure?", SwingHelper.DLG_YES_NO);
      if (SwingHelper.OPTION_YES!=i) return;
      selection.delete();
      selection=null;
      repaint();
    }
  } //ActionDeleteNode

  /**
   * How to handle - Sets a Node's Shape
   */
  private class ActionSetNodeShape extends UnifiedAction {
    Shape shape;
    protected ActionSetNodeShape(Shape set) {
      shape = set;
    }
    protected void execute() {
      selection.setShape(shape);
      repaint();
    }
  }

  /**
   * How to handle - Change node content
   */
  private class ActionSetNodeContent extends UnifiedAction {
    protected ActionSetNodeContent() { super("Set content"); }
    protected void execute() {
      String txt = SwingHelper.showDialog(GraphWidget.this, "Set content", "Please enter text here:");
      if (txt==null) return;
      if (txt.length()>0)
        selection.setContent(txt);
      else 
        selection.setContent(new ShellGraph());
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
    private ShellGraph in;
    private Point2D pos;
    protected ActionCreateNode(ShellGraph setIn, Point2D setPos) { 
      super("Create node"); 
      pos = setPos;
      in = setIn; 
    }
    protected void execute() {
      String txt = SwingHelper.showDialog(GraphWidget.this, "Set content", "Please enter text here:");
      if (txt!=null) in.createNode(shapes[0], txt, pos);
      repaint();
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
        method.invoke(currentLayout, new Object[]{ selection });
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
     * Calculate x offset for centered graph
     */
    private int getXOffset() {
      if (graph==null) return 0;
      return -graphBounds.x+(getWidth()-graphBounds.width)/2;
    }
    
    /**
     * Calculate y offset for centered graph
     */
    private int getYOffset() {
      if (graph==null) return 0;
      return -graphBounds.y+(getHeight()-graphBounds.height)/2;
    }
    
    /**
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
      if (graph==null) return new Dimension();
      return graphBounds.getSize();
    }
  
    /**
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    protected void paintComponent(Graphics g) {
      
      // clear background
      g.setColor(Color.white);
      g.fillRect(0,0,getWidth(),getHeight());

      // graph there?
      if (graph==null) return;
      
      // cast to 2d
      Graphics2D graphics = (Graphics2D)g;
      
      // switch on antialiasing?
      graphics.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        isAntialiasing ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF
      );
      
      // synchronize on graph and go?
      synchronized (graph) {
        // create our working graphics
        // paint at 0,0
        graphics.translate(getXOffset(),getYOffset());
        // LayoutRenderer?
        if (layoutRenderer!=null) 
          layoutRenderer.render(graph,currentLayout,graphics);
        // let the renderer do its work
        graphRenderer.render(graph, currentLayout, graphics);
      }

      // done
    }
    
    /**
     * Convert screen postition to model
     */
    private Point getPoint(Point p) {
      return new Point(
        p.x - getXOffset(),
        p.y - getYOffset()
      );
    }
  
  } //Content
  
} //GraphWidget
