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

import gj.geom.ShapeHelper;
import gj.io.GraphReader;
import gj.io.GraphWriter;
import gj.layout.GraphNotSupportedException;
import gj.layout.LayoutAlgorithm;
import gj.layout.LayoutAlgorithmException;
import gj.shell.factory.AbstractGraphFactory;
import gj.shell.model.Graph;
import gj.shell.model.Layout;
import gj.shell.model.Vertex;
import gj.shell.swing.SwingHelper;
import gj.shell.swing.UnifiedAction;
import gj.shell.util.Properties;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * A Shell for GraphJ's core functions
 */
public class Shell {

  /** the Shapes we know */
  public final static Shape[] shapes = new Shape[] {
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
  
  /** the frame we use */
  private JFrame frame;
  
  /** the graph widget we show */
  private GraphWidget graphWidget;

  /** the algorithm widget we show */
  private AlgorithmWidget algorithmWidget;
  
  /** the graph we're looking at */
  private Graph graph;
  
  /** the view of the graph */
  private Layout layout = new Layout();
  
  /** whether we perform an animation after layout */
  private boolean isAnimation = true;
  
  /** an animation that is going on */
  private Animation animation;
  
  /** our properties */
  private Properties properties = new Properties(Shell.class, "shell.properties");
  
  /** our factories */
  private AbstractGraphFactory[] factories = (AbstractGraphFactory[])properties.get("factory", new AbstractGraphFactory[0]);
  
  /** our algorithms */
  private LayoutAlgorithm[] algorithms = (LayoutAlgorithm[])properties.get("algorithm", new LayoutAlgorithm[0]);
  
  /**
   * MAIN
   */
  public static void main(String[] args) {
    
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Throwable t) {
    }
    
    new Shell();
  }
  
  /**
   * Constructor
   */
  private Shell() {
    
    // Create our widgets
    graphWidget = new GraphWidget();
    
    algorithmWidget = new AlgorithmWidget();
    algorithmWidget.setAlgorithms(algorithms);
    algorithmWidget.setBorder(BorderFactory.createEtchedBorder());
    algorithmWidget.addActionListener(new ActionExecuteLayout());
    
    // Create our frame
    frame = new JFrame("GraphJ - Shell") {
      public void dispose() {
        super.dispose();
        System.exit(0);
      }
    };
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    Container container = frame.getContentPane();
    container.setLayout(new BorderLayout());
    container.add(graphWidget, BorderLayout.CENTER);
    container.add(algorithmWidget, BorderLayout.EAST  );
    
    // Our default button
    frame.getRootPane().setDefaultButton(algorithmWidget.getDefaultButton());
    
    // Setup Menu
    frame.setJMenuBar(createMenu());
    
    // Show the whole thing
    frame.setBounds(128,128,480,480);
    frame.show();
    
    // Start with a Graph or load a preset
    Runnable run = null;
    String preset = System.getProperty("gj.preset");
    if (preset==null) { 
//      if (factories.length>0) 
//        run = (Runnable)new ActionNewGraph(factories[0]).as(Runnable.class);
    } else {
      run = (Runnable)new ActionLoadGraph(new File(preset)).as(Runnable.class);
    }
    if (run!=null) SwingUtilities.invokeLater(run);
    
    // Done
  }
  
  /** 
   * Helper that creates the MenuBar
   */
  private JMenuBar createMenu() {

    // GRAPH GRAPH GRAPH GRAPH GRAPH GRAPH GRAPH GRAPH GRAPH 
    JMenu mNew = new JMenu("New");
    for (int i=0;i<factories.length;i++) {
      mNew.add(new ActionNewGraph(factories[i]));
    }
    
    JMenu mGraph = new JMenu("Graph");
    mGraph.add(mNew);
    mGraph.add(new ActionLoadGraph());
    mGraph.add(new ActionSaveGraph());
    mGraph.add(new ActionCloseGraph());

    // LAYOUT LAYOUT LAYOUT LAYOUT LAYOUT LAYOUT LAYOUT LAYOUT 
    JMenu mLayout = new JMenu("Layout");
    for (int i=0;i<algorithms.length;i++) {
      mLayout.add(new ActionSelectLayout(algorithms[i]));
    }
    
    // OPTIONS OPTIONS OPTIONS OPTIONS OPTIONS OPTIONS OPTIONS 
    JMenu mOptions = new JMenu("Options");
    mOptions.add(SwingHelper.getCheckBoxMenuItem(new ActionToggleAntialias()));
    mOptions.add(SwingHelper.getCheckBoxMenuItem(new ActionToggleAnimation()));
    
    // MAIN MAIN MAIN MAIN MAIN MAIN MAIN MAIN MAIN MAIN MAIN 
    JMenuBar result = new JMenuBar();
    result.add(mGraph);
    result.add(mLayout);
    result.add(mOptions);

    // done
    return result;
  }
  
  /**
   * Sets the graph we're looking at
   */
  private void setGraph(Graph set) {
    // remember
    graph = set;
    // propagate
    graphWidget.setGraph(graph);
    algorithmWidget.setGraph(graph);
  }
  
  /**
   * Creates a new graph
   */
  private void setGraph(AbstractGraphFactory factory) {
    // let the user change some properties
    if (PropertyWidget.hasProperties(factory)) {
      PropertyWidget content = new PropertyWidget().setInstance(factory);
      int rc = SwingHelper.showDialog(graphWidget, "Graph Properties", content, SwingHelper.DLG_OK_CANCEL);
      if (SwingHelper.OPTION_OK!=rc) 
        return;
      content.commit();
    }
    // create the graph
    factory.setNodeShape(shapes[0]);
    setGraph((Graph)factory.create(createGraphBounds()));
    // done
  }
  
  /**
   * Creates a graph bounds rectangle
   */
  private Rectangle createGraphBounds() {
    return new Rectangle(0,0,graphWidget.getWidth()-32,graphWidget.getHeight()-32);
  }
  
  /**
   * How to handle - run an algorithm
   */
  /*package*/ class ActionExecuteLayout extends UnifiedAction {
    
    /** an animation that we're working on */
    private Animation animation;

    /**
     * constructor
     */
    /*package*/ ActionExecuteLayout() {
      super.setAsync(ASYNC_SAME_INSTANCE);
    }
    
    /** initializer (EDT) */
    protected boolean preExecute() throws LayoutAlgorithmException {
      // something to do?
      if (graph==null) 
        return false;
      // something running?
      cancel(true);
      // create animation (it'll take a snapshot of current configuration)
      animation = new Animation(graph, layout);
      // create suggested bounds
      Rectangle bounds = createGraphBounds();
      // apply algorithm
      try {
        layout(bounds);
      } catch (LayoutAlgorithmException e) {
        throw e;
      }
      // continue into animation?
      if (!isAnimation) {
        graphWidget.setGraph(graph);
        return false;
      }
      // continue into async
      return true;
    }
    /** layout */
    private Layout layout(Rectangle bounds) throws LayoutAlgorithmException {

      // make sure the current graph is valid
      try {
        graph.validate();
      } finally {
        graph = new Graph(graph);
      }
      
      // the algorithm
      LayoutAlgorithm algorithm = algorithmWidget.getSelectedAlgorithm();
      
      // try to layout
      Layout result = new Layout();
      try {
        algorithm.apply(graph, layout, bounds);
      } catch (GraphNotSupportedException s) {
        try {
	        String impl = properties.get("impl."+s.getSupportedGraphType().getName(), (String)null);
	        graph = (Graph)Class.forName(impl).getConstructor(new Class[]{Graph.class}).newInstance(new Object[]{graph});
        } catch (InvocationTargetException t) {
      	  throw new LayoutAlgorithmException(t.getCause().getMessage());
        } catch (Throwable t) {
          throw new LayoutAlgorithmException("couldn't find implementation for "+s.getSupportedGraphType().getName()+" needed for "+algorithm, t);
        }
        // try again
        algorithm.apply(graph, layout, bounds);
      }

      // done
      return result;
    }
    /** async execute */
    protected void execute() throws LayoutAlgorithmException {
      try {
        while (true) {
          if (Thread.currentThread().isInterrupted()) break;
          if (animation.animate()) break;
          graphWidget.setGraph(graph);
        }
      } catch (InterruptedException e) {
        // ignore
      }
    }
    
    /** sync post-execute */
    protected void postExecute() throws Exception {
      graphWidget.setGraph(graph);
      graphWidget.setCurrentAlgorithm(algorithmWidget.getSelectedAlgorithm());
    }
    
  } //ActionExecuteLayout
  
  /**
   * How to handle - switch to Layout
   */
  /*package*/ class ActionSelectLayout extends UnifiedAction {
    private LayoutAlgorithm algorithm;
    /*package*/ ActionSelectLayout(LayoutAlgorithm set) {
      super(set.toString());
      algorithm=set;
    }
    protected void execute() { 
      algorithmWidget.setSelectedAlgorithm(algorithm); 
    }
  }

  /**
   * How to handle - Load Graph
   */
  /*package*/ class ActionLoadGraph extends UnifiedAction  {
    private File preset;
    /*package*/ ActionLoadGraph(File file) { 
      preset=file;
    }
    /*package*/ ActionLoadGraph() { super("Load"); }
    protected void execute() throws IOException { 
      File file = preset;
      if (file==null) {
        JFileChooser fc = new JFileChooser(new File("./save"));
        if (JFileChooser.APPROVE_OPTION!=fc.showOpenDialog(frame)) return;
        file = fc.getSelectedFile();
      }
      setGraph(new GraphReader(new FileInputStream(file)).read());
    }
    
    /**
     * io interface implementation
     */
    public Map getAttributes(Object element) {
      return Collections.singletonMap("c", ((Vertex)element).getContent());
    }
    
    /**
     * io interface implementation
     */
    public void setAttribute(Object element, String key, String val) {
      if (element instanceof Vertex&&"c".equals(key)) 
        ((Vertex)element).setContent(val);
    }
  } //LoadGraph

  /**
   * How to handle - Save Graph
   */
  /*package*/ class ActionSaveGraph extends UnifiedAction {
    /*package*/ ActionSaveGraph() { 
      setName("Save"); 
    }
    protected void execute() throws IOException { 
      JFileChooser fc = new JFileChooser(new File("./save"));
      if (JFileChooser.APPROVE_OPTION!=fc.showSaveDialog(frame)) return;
      new GraphWriter(new FileOutputStream(fc.getSelectedFile())).write(graph);
    }
  }

  /**
   * How to handle - Close Graph
   */
  /*package*/ class ActionCloseGraph extends UnifiedAction {
    /*package*/ ActionCloseGraph() { super("Close"); }
    protected void execute() { frame.dispose(); }
  }
  
  /**
   * How to handle - New Graph
   */
  /*package*/ class ActionNewGraph extends UnifiedAction {
    private AbstractGraphFactory factory;
    /*package*/ ActionNewGraph(AbstractGraphFactory factory) { 
      super(factory.toString());
      this.factory = factory;
    }
    protected void execute() {
      setGraph(factory);
    }
  }

  /**
   * How to handle - Toggle antialiasing
   */
  /*package*/ class ActionToggleAntialias extends UnifiedAction {
    /*package*/ ActionToggleAntialias() { super("Antialiasing"); }
    public boolean isSelected() { return graphWidget.isAntialiasing(); }
    protected void execute() {
      graphWidget.setAntialiasing(!graphWidget.isAntialiasing());
    }    
  }

  /**
   * How to handle - Toggle animation
   */
  /*package*/ class ActionToggleAnimation extends UnifiedAction {
    /*package*/ ActionToggleAnimation() { super("Animation"); }
    public boolean isSelected() { return isAnimation; }
    protected void execute() {
      isAnimation = !isAnimation;
    }    
  }

}
