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
import gj.layout.Layout2D;
import gj.layout.LayoutAlgorithm;
import gj.model.Edge;
import gj.model.Graph;
import gj.model.Vertex;
import gj.shell.model.EditableEdge;
import gj.shell.model.EditableGraph;
import gj.shell.model.EditableLayout;
import gj.shell.model.EditableVertex;
import gj.shell.swing.Action2;
import gj.shell.swing.SwingHelper;
import gj.shell.util.ReflectHelper;
import gj.ui.DefaultGraphRenderer;
import gj.ui.GraphRenderer;
import gj.ui.GraphWidget;
import gj.util.ModelHelper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;


/**
 * Displaying a Graph with user input
 */
public class EditableGraphWidget extends GraphWidget {
  
  private final static Stroke DASH = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[]{ 2, 3}, 0.0f);
  
  /** our editable graph */
  private EditableGraph graph;
  private Collection<Shape> debugShapes;
  
  /** whether quicknode is enabled */
  private boolean quickNode = false;
  
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
   * our graph renderer
   */
  private GraphRenderer renderer = new DefaultGraphRenderer() {
    /**
     * Color resolve
     */
    @Override
    protected Color getColor(Vertex vertex) {
      return vertex==graph.getSelection() ? Color.BLUE : Color.BLACK;    
    }
    
    /**
     * Color resolve
     */
    @Override
    protected Color getColor(Edge edge) {
      return edge==graph.getSelection() ? Color.BLUE : Color.BLACK;    
    }
    /** 
     * Stroke resolve
     */
    @Override
    protected Stroke getStroke(Vertex vertexOrEdge) {
      // check layout getters for vertex or edge (TreeLayout's root for example)
      if (currentAlgorithm!=null) {
        for (Method prop : ReflectHelper.getMethods(currentAlgorithm, "get.*", new Class[] { Graph.class }, false)) {
          try {
            Object value = prop.invoke(currentAlgorithm, graph);
            if (value.equals(vertexOrEdge))
              return DASH;
          } catch (Throwable t) {
          }
        }
      }
      return super.getStroke(vertexOrEdge);
    }
    
    @Override
    public void render(Graph graph, Layout2D layout, Graphics2D graphics) {
    
      graphics.setColor(Color.LIGHT_GRAY);
      if (debugShapes!=null) for (Shape shape : debugShapes) {
        graphics.draw(shape);
      }
      
      super.render(graph, layout, graphics);
    }

  };
  
  /**
   * Constructor
   */
  public EditableGraphWidget() {
    super(new EditableLayout());
    setRenderer(renderer);
  }
  
  /**
   * set debug shapes
   */
  public void setDebugShapes(Collection<Shape> shapes) {
    debugShapes = shapes;
    repaint();
  }
    
  /**
   * Accessor - Graph
   */
  @Override
  public void setGraph(Graph setGraph, Rectangle bounds) {

    if (!(setGraph instanceof EditableGraph))
      throw new IllegalArgumentException();
    
    // reset debug shapes
    debugShapes = null;
    
    // remember
    graph = (EditableGraph)setGraph;
    
    // let super do its thing
    super.setGraph(setGraph, bounds);
    
    // start fresh with DnD
    dndNoOp.start(null);
    
    // done
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
  protected JPopupMenu getEdgePopupMenu(EditableEdge e, Point pos) {

    // Create the popups
    JPopupMenu result = new JPopupMenu();
    result.add(new ActionDeleteEdge());

    // collect public setters(Edge,*) on layout
    if (currentAlgorithm!=null) {
      List<Method> methods = ReflectHelper.getMethods(currentAlgorithm, "set.*", new Class[] { Graph.class, e.getClass() }, true );
      for (Method method : methods) { 
        result.add(new ActionInvoke(currentAlgorithm, method, new Object[] { graph, e }));
      }
    }
    // done
    return result;
  }

  /**
   * Returns the popup for nodes
   */
  protected JPopupMenu getVertexPopupMenu(EditableVertex v, Point pos) {

    // Create the popups
    JPopupMenu result = new JPopupMenu();
    result.add(new ActionResizeVertex());
    result.add(new ActionSetVertexContent());
    JMenu mShape = new JMenu("Set Shape");
    for (int i=0;i<Shell.shapes.length;i++)
      mShape.add(new ShapeMenuItemWidget(Shell.shapes[i], new ActionSetVertexShape(Shell.shapes[i])));
    result.add(mShape);
    result.add(new ActionDeleteVertex());

    // collect public setters(Graph, Vertex) on layout
    if (currentAlgorithm!=null) {
      List<Method> methods = ReflectHelper.getMethods(currentAlgorithm, "set.*", new Class[] { Graph.class, v.getClass()}, true );
      for (Method method : methods) { 
        result.add(new ActionInvoke(currentAlgorithm, method, new Object[] { graph, v }));
      }
    }
    
    // done
    return result;    
  }
  
  /**
   * Returns the popup for canvas
   */
  protected JPopupMenu getCanvasPopupMenu(Point pos) {
    
    JPopupMenu result = new JPopupMenu();
    result.add(new ActionCreateVertex(getPoint(pos)));
    result.add(SwingHelper.getCheckBoxMenuItem(new ActionToggleQuickVertex()));
    return result;
  }
  
  /**
   * Algorithm as been changed
   */
  protected void algorithmChangeNotify() {
    
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
    /** stop */
    protected void stop() { }
    /** callback */
    @Override
    public void mousePressed(MouseEvent e) {}
    /** callback */
    @Override
    public void mouseReleased(MouseEvent e) {}
    /** callback */
    @Override
    public void mouseDragged(MouseEvent e) {}
    /** callback */
    @Override
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
      Object oldSelection = graph.getSelection();
      Object newSelection = graph.getElement(getPoint(e.getPoint()));
      graph.setSelection(newSelection);
      // popup?
      if (e.isPopupTrigger()) {
        popup(e.getPoint());
        return;
      }
      // start dragging?
      if (e.getButton()==MouseEvent.BUTTON1) {
        if (newSelection instanceof EditableVertex) {
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
        graph.addVertex(getPoint(e.getPoint()), Shell.shapes[0], ""+(graph.getNumVertices() + 1) );
        repaint();
        return;
      }
      // done
    }
    /** popup */
    private void popup(Point at) {
      Object selection = graph.getSelection();
      JPopupMenu menu = null;
      if (selection instanceof EditableVertex) 
        menu = getVertexPopupMenu((EditableVertex)selection, at);
      if (selection instanceof EditableEdge)
        menu = getEdgePopupMenu((EditableEdge)selection, at);
      if (menu==null)
        menu = getCanvasPopupMenu(at);
      menu.show(EditableGraphWidget.this,at.x,at.y);
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
      ModelHelper.translate(getGraphLayout(), (EditableVertex)graph.getSelection(), Geometry.getDelta(from, e.getPoint()));
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
    private EditableVertex from;
    /** a dummy to */
    private EditableVertex dummy;
    /** start */
    @Override
    protected void start(Point at) {
      super.start(at);
      from = (EditableVertex)graph.getSelection();
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
        if (selection instanceof EditableVertex) {
          EditableVertex v = (EditableVertex)selection;
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
        if (graph.getVertex(getPoint(e.getPoint()))==from)
          return;
        // create dummies
        dummy = graph.addVertex(null, null, null);      
        graph.addEdge(from, dummy);
      }
      // place dummy (tip of arc)
      Point2D pos = getPoint(e.getPoint());
      dummy.setPosition(pos);
      // find target
      EditableVertex selection = graph.getVertex(pos);
      if (selection!=from)
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
    private EditableVertex vertex;
    private Shape shape;
    private Dimension dim;
    /** start */
    @Override
    protected void start(Point pos) {
      super.start(pos);
      // remember
      vertex = (EditableVertex)graph.getSelection();
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
      Point2D delta = Geometry.getDelta(vertex.getPosition(), getPoint(e.getPoint()));
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
      EditableVertex selection = (EditableVertex)graph.getSelection();
      if (selection==null)
        return;
      int i = SwingHelper.showDialog(EditableGraphWidget.this, "Delete Node", "Are you sure?", SwingHelper.DLG_YES_NO);
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
      EditableEdge selection = (EditableEdge)graph.getSelection();
      if (selection==null)
        return;
      int i = SwingHelper.showDialog(EditableGraphWidget.this, "Delete Edge", "Are you sure?", SwingHelper.DLG_YES_NO);
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
      EditableVertex vertex = (EditableVertex)graph.getSelection();
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
      String txt = SwingHelper.showDialog(EditableGraphWidget.this, "Set content", "Please enter text here:");
      if (txt==null) 
        return;
      EditableVertex vertex = (EditableVertex)graph.getSelection();
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
    private EditableVertex from, to;
    protected ActionCreateEdge(EditableVertex v1, EditableVertex v2) {
      from = v1;
      to = v2;
    }
    @Override
    protected void execute() throws Exception {
      graph.addEdge(from, to);
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
      String txt = SwingHelper.showDialog(EditableGraphWidget.this, "Set content", "Please enter text here:");
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
  private class ActionInvoke extends Action2 {
    private Object target;
    private Method method;
    private Object[] values;
    protected ActionInvoke(Object target, Method method, Object[] values) { 
      super.setName(ReflectHelper.getName(target.getClass())+"."+method.getName()+"(...)"); 
      this.target = target;
      this.method = method;
      this.values = values;
    }
    @Override
    protected void execute() { 
      try {
        Class<?>[] types = method.getParameterTypes();
        Object[] args = new Object[types.length];

        // collect parameters
        for (int i=0;i<args.length;i++) {
          if (i<values.length)
            args[i] = values[i];
          else
            args[i] = ReflectHelper.wrap(JOptionPane.showInputDialog("Provide input for "+getName()), types[i] );
        }
        
        // invoke
        method.invoke(target, args);
        
        // notify
        algorithmChangeNotify();
        
      } catch (Exception e) {
      }
      repaint();
    }
  }
  
} //GraphWidget
