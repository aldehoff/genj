/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2002-2004 Nils Meier
 * 
 * GraphJ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GraphJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphJ; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package gj.shell;

import gj.geom.Geometry;
import gj.layout.LayoutAlgorithm;
import gj.shell.model.Edge;
import gj.shell.model.Element;
import gj.shell.model.Graph;
import gj.shell.model.Layout;
import gj.shell.model.Vertex;
import gj.shell.swing.Action2;
import gj.shell.swing.SwingHelper;
import gj.shell.util.ReflectHelper;
import gj.util.ModelHelper;

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
  
  /** the graph we're displaying */
  private Graph graph;
  
  /** the layout of it */
  private Layout layout = new Layout();
  
  /** the size of the graph */
  private Rectangle graphBounds;
  
  /** the content for the graph */
  private Content content = new Content();
  
  /** whether quicknode is enabled */
  private boolean quickNode = false;
  
  /** whether antialiasing is on */
  private boolean isAntialiasing = true;
  
  /** our mouse analyzers */
  private DnD
    dndNoOp = new DnDIdle(),
    dndMoveNode = new DnDMoveVertex(),
    dndCreateEdge = new DnDCreateEdge(),
    dndResizeNode = new DnDResizeVertex(),
    dndCurrent = null;
    
  /** a algorithm we know about */
  private LayoutAlgorithm currentAlgorithm;
    
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
  public void setGraph(Graph setGraph) {
    
    // cleanup data
    graph = setGraph;
    graphBounds = ModelHelper.getBounds(graph, layout).getBounds();
    
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
  @Override
  public void revalidate() {
    if (content!=null) content.revalidate();
    super.revalidate();
    repaint();
  }
  
  /** 
   * Accessor - the current algorithm
   */
  public void setCurrentAlgorithm(LayoutAlgorithm set) {
    currentAlgorithm = set;
    repaint();
  }
  
  /**
   * Returns the popup for edges
   */
  private JPopupMenu getEdgePopupMenu(Edge e, Point pos) {

    // Create the popups
    JPopupMenu result = new JPopupMenu();
    result.add(new ActionDeleteEdge());

    // collect public setters(Edge)
    List<ReflectHelper.Property> props = ReflectHelper.getProperties(graph, false);
    for (int a=0; a<props.size(); a++) { 
      if (props.get(a).getType()==Edge.class)
        result.add(new ActionGraphProperty(props.get(a)));
    }
    // done
    return result;
  }

  /**
   * Returns the popup for nodes
   */
  private JPopupMenu getVertexPopupMenu(Vertex v, Point pos) {

    // Create the popups
    JPopupMenu result = new JPopupMenu();
    result.add(new ActionResizeVertex());
    result.add(new ActionSetVertexContent());
    JMenu mShape = new JMenu("Set Shape");
    for (int i=0;i<Shell.shapes.length;i++)
      mShape.add(new ShapeMenuItemWidget(Shell.shapes[i], new ActionSetVertexShape(Shell.shapes[i])));
    result.add(mShape);
    result.add(new ActionDeleteVertex());

    // collect public setters(Vertex)
    List<ReflectHelper.Property> props = ReflectHelper.getProperties(graph, false);
    for (int a=0; a<props.size(); a++) { 
      if (props.get(a).getType()==Vertex.class)
        result.add(new ActionGraphProperty(props.get(a)));
    }

    // done
    return result;    
  }
  
  /**
   * Returns the popup for canvas
   */
  private JPopupMenu getCanvasPopupMenu(Point pos) {
    
    JPopupMenu result = new JPopupMenu();
    result.add(new ActionCreateVertex(content.getPoint(pos)));
    result.add(SwingHelper.getCheckBoxMenuItem(new ActionToggleQuickVertex()));
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
    @Override
    public void mousePressed(MouseEvent e) {}
    /** callback */
    @Override
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
    @Override
    public void mousePressed(MouseEvent e) {
      // nothing to do?
      if (graph==null) return;
      // something there?
      Element oldSelection = graph.getSelection();
      Element newSelection = graph.getElement(content.getPoint(e.getPoint()));
      graph.setSelection(newSelection);
      // popup?
      if (e.isPopupTrigger()) {
        popup(e.getPoint());
        return;
      }
      // start dragging?
      if (e.getButton()==MouseEvent.BUTTON1) {
        if (newSelection instanceof Vertex) {
	        if (oldSelection==newSelection)
	          dndMoveNode.start(e.getPoint());
	        else
	          dndCreateEdge.start(e.getPoint());
        }
      }
      // always show
      repaint();
      // done
    }
    /** callback */
    @Override
    public void mouseReleased(MouseEvent e) {
      
      // context menu?
      if (e.isPopupTrigger()) {
        popup(e.getPoint());
        return;
      }
      
      // quick create?
      if (quickNode) {
        graph.addVertex(content.getPoint(e.getPoint()), Shell.shapes[0], ""+(graph.getVertices().size()+1) );
        repaint();
        return;
      }
      // done
    }
    /** popup */
    private void popup(Point at) {
      Object selection = graph.getSelection();
      JPopupMenu menu = null;
      if (selection instanceof Vertex) 
        menu = getVertexPopupMenu((Vertex)selection, at);
      if (selection instanceof Edge)
        menu = getEdgePopupMenu((Edge)selection, at);
      if (menu==null)
        menu = getCanvasPopupMenu(at);
      menu.show(content,at.x,at.y);
    }
  } //DnDIdle
  
  /**
   * Mouse Analyzer - Drag a Node
   */
  private class DnDMoveVertex extends DnD {
    Point from;
    /** start */
    @Override
    protected void start(Point at) {
      super.start(at);
      from = at;
    }
    /** callback */
    @Override
    public void mouseReleased(MouseEvent e) {
      dndNoOp.start(e.getPoint());
    }
    /** callback */
    @Override
    public void mouseDragged(MouseEvent e) {
      // move the selected
      ModelHelper.translate(layout, graph.getSelection(), Geometry.sub(from, e.getPoint()));
      from = e.getPoint();
      // show
      repaint();
    }
  } //DnDMoveNode
  
  /**
   * Mouse Analyzer - Create an edge
   */
  private class DnDCreateEdge extends DnD{
    /** the from node */
    private Vertex from;
    /** a dummy to */
    private Vertex dummy;
    /** start */
    @Override
    protected void start(Point at) {
      super.start(at);
      from = (Vertex)graph.getSelection();
      dummy = null;
      graph.setSelection(null);
    }
    /** stop */
    @Override
    protected void stop() {
      // delete dummy which will also delete the arc
      if (dummy!=null)  {
        graph.removeVertex(dummy);
        dummy = null;
      }
      // done
    }
    /** callback */
    @Override
    public void mouseReleased(MouseEvent e) {
      // make sure we're stopped
      stop();
      // selection made? create arc!
      if (graph.getSelection()!=null) {
        Object selection = graph.getSelection();
        if (selection instanceof Vertex) {
          Vertex v = (Vertex)selection;
	        if (from.isNeighbour(v))
	          graph.removeEdge(from.getEdge(v));
	        new ActionCreateEdge(from, v).trigger();
        }
      }
      graph.setSelection(from);
      // show
      repaint();
      // continue
      dndNoOp.start(e.getPoint());
    }
    /** callback */
    @Override
    public void mouseDragged(MouseEvent e) {
      // not really dragging yet?
      if (dummy==null) {
        // user has to move mouse outside of shape first (avoid loops)
        if (graph.getVertex(content.getPoint(e.getPoint()))==from)
          return;
        // create dummies
        dummy = graph.addVertex(null, null, null);      
        graph.addEdge(from, dummy, null);
      }
      // place dummy (tip of arc)
      Point2D pos = content.getPoint(e.getPoint());
      dummy.setPosition(pos);
      // find target
      graph.setSelection(graph.getVertex(pos));
      // show
      repaint();
    }
  } //DnDCreateEdge

  /**
   * Mouse Analyzer - Resize a Node
   */
  private class DnDResizeVertex extends DnD {
    /** our status */
    private Vertex vertex;
    private Shape shape;
    private Dimension dim;
    /** start */
    @Override
    protected void start(Point pos) {
      super.start(pos);
      // remember
      vertex = (Vertex)graph.getSelection();
      shape = vertex.getShape();
      dim = shape.getBounds().getSize();
    }
    /** callback */
    @Override
    public void mouseReleased(MouseEvent e) {
      dndNoOp.start(e.getPoint());
    }
    /** callback */
    @Override
    public void mouseMoved(MouseEvent e) {

      // change shape
      Point2D delta = Geometry.sub(vertex.getPosition(), content.getPoint(e.getPoint()));
      double 
        sx = Math.max(0.1,Math.abs(delta.getX())/dim.width *2),
        sy = Math.max(0.1,Math.abs(delta.getY())/dim.height*2);

      GeneralPath gp = new GeneralPath(shape);
      gp.transform(AffineTransform.getScaleInstance(sx, sy));
      vertex.setShape(gp);
      
      // show it
      repaint();
    }
  } //DnDResizeNode

  /**
   * How to handle - Delete a Vertex
   */
  private class ActionDeleteVertex extends Action2 {
    protected ActionDeleteVertex() { super("Delete Vertex"); }
    @Override
    protected void execute() {
      Vertex selection = (Vertex)graph.getSelection();
      if (selection==null)
        return;
      int i = SwingHelper.showDialog(GraphWidget.this, "Delete Node", "Are you sure?", SwingHelper.DLG_YES_NO);
      if (SwingHelper.OPTION_YES!=i) 
        return;
      graph.removeVertex(selection);
      repaint();
    }
  } //ActionDeleteVertex

  /**
   * How to handle - Delete an edge
   */
  private class ActionDeleteEdge extends Action2 {
    protected ActionDeleteEdge() { super("Delete Edge"); }
    @Override
    protected void execute() {
      Edge selection = (Edge)graph.getSelection();
      if (selection==null)
        return;
      int i = SwingHelper.showDialog(GraphWidget.this, "Delete Edge", "Are you sure?", SwingHelper.DLG_YES_NO);
      if (SwingHelper.OPTION_YES!=i) 
        return;
      graph.removeEdge(selection);
      repaint();
    }
  } //ActionDeleteEdge

  /**
   * How to handle - Sets a Node's Shape
   */
  private class ActionSetVertexShape extends Action2 {
    Shape shape;
    protected ActionSetVertexShape(Shape set) {
      shape = set;
    }
    @Override
    protected void execute() {
      Vertex vertex = (Vertex)graph.getSelection();
      vertex.setShape(shape);
      repaint();
    }
  }

  /**
   * How to handle - Change node content
   */
  private class ActionSetVertexContent extends Action2 {
    protected ActionSetVertexContent() { super("Set content"); }
    @Override
    protected void execute() {
      String txt = SwingHelper.showDialog(GraphWidget.this, "Set content", "Please enter text here:");
      if (txt==null) 
        return;
      Vertex vertex = (Vertex)graph.getSelection();
      vertex.setContent(txt);
      repaint();
    }
  }

  /**
   * How to handle - Change node size
   */
  private class ActionResizeVertex extends Action2 {
    protected ActionResizeVertex() { super("Resize"); }
    @Override
    protected void execute() { dndResizeNode.start(null); }
  }
  
  /**
   * How to handle - create an edge
   */
  private class ActionCreateEdge extends Action2 {
    private Vertex from, to;
    protected ActionCreateEdge(Vertex v1, Vertex v2) {
      from = v1;
      to = v2;
    }
    @Override
    protected void execute() throws Exception {
      graph.addEdge(from, to, null);
    }
  }
  
  /**
   * How to handle - Create a vertex
   */
  private class ActionCreateVertex extends Action2 {
    private Point2D pos;
    protected ActionCreateVertex(Point2D setPos) { 
      super("Create node"); 
      pos = setPos;
    }
    @Override
    protected void execute() {
      String txt = SwingHelper.showDialog(GraphWidget.this, "Set content", "Please enter text here:");
      if (txt!=null) 
        graph.addVertex(pos, Shell.shapes[0], txt);
      repaint();
    }
  }

  /**
   * How to handle - Toggle quick node
   */
  private class ActionToggleQuickVertex extends Action2 {
    protected ActionToggleQuickVertex() { super("QuickNode"); }
    @Override
    protected void execute() { quickNode=!quickNode; }
    @Override
    public boolean isSelected() { return quickNode; }
  }
  
  /**
   * How to handle - Graph property 
   */
  private class ActionGraphProperty extends Action2 {
    private ReflectHelper.Property prop;
    protected ActionGraphProperty(ReflectHelper.Property prop) { 
      super.setName("set"+prop.getName()+"()"); 
      this.prop = prop;
    }
    @Override
    protected void execute() { 
      try {
        prop.setValue(graph.getSelection() );
      } catch (Exception e) {
      }
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
    @Override
    public Dimension getPreferredSize() {
      if (graph==null) return new Dimension();
      return graphBounds.getSize();
    }
  
    /**
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    @Override
    protected void paintComponent(Graphics g) {
      
      // clear background
      g.setColor(Color.white);
      g.fillRect(0,0,getWidth(),getHeight());

      // graph there?
      if (graph==null) 
        return;
      
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
        // let the renderer do its work
        graph.render(graphics);
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
