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

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import gj.model.factory.EmptyFactory;
import gj.model.factory.Factory;
import gj.model.factory.GraphFactory;
import gj.model.factory.TreeFactory;
import gj.io.GraphReader;
import gj.io.GraphWriter;
import gj.layout.Layout;
import gj.layout.LayoutException;

import gj.model.Graph;
import gj.model.MutableGraph;
import gj.model.Node;

import gj.shell.swing.SwingHelper;
import gj.shell.swing.UnifiedAction;
import gj.shell.util.Properties;
import gj.shell.util.ReflectHelper;
import gj.ui.GraphGraphics;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * A Shell for GraphJ's core functions
 */
public class Shell {

  /** the frame we use */
  private JFrame frame;
  
  /** the graph widget we show */
  private GraphWidget graphWidget;

  /** the scrolling we wrap the graph widget in */  
  private JScrollPane graphScroll;
  
  /** the layout widget we show */
  private LayoutWidget layoutWidget;
  
  /** the graph we're looking at */
  private MutableGraph graph;
  
  /** whether we perform an animation after layout */
  private boolean isAnimation = true;
  
  /** an animation that is going on */
  private Animation animation;
  
  /** our properties */
  private Properties properties = new Properties(Shell.class, "shell.properties");
  
  /** our factories */
  private Factory[] factories = (Factory[])properties.get("factory", new Factory[0]);
  
  /** our layouts */
  private Layout[] layouts = (Layout[])properties.get("layout", new Layout[0]);
  
  /**
   * MAIN
   */
  public static void main(String[] args) {
    new Shell();
  }
  
  /**
   * Constructor
   */
  private Shell() {
    
    // Create our widgets
    graphWidget = new GraphWidget();
    
    layoutWidget = new LayoutWidget();
    layoutWidget.setLayouts(layouts);
    layoutWidget.setBorder(BorderFactory.createEtchedBorder());
    layoutWidget.addActionListener(new ActionExecuteLayout());
    graphScroll = new JScrollPane(graphWidget);
    
    // Create our frame
    frame = new JFrame("GraphJ - Shell") {
      public void dispose() {
        super.dispose();
        System.exit(0);
      }
    };
    frame.setDefaultCloseOperation(frame.DISPOSE_ON_CLOSE);
    Container container = frame.getContentPane();
    container.setLayout(new BorderLayout());
    container.add(graphScroll , BorderLayout.CENTER);
    container.add(layoutWidget, BorderLayout.EAST  );
    
    // Our default button
    frame.getRootPane().setDefaultButton(layoutWidget.getDefaultButton());
    
    // Setup Menu
    frame.setJMenuBar(createMenu());
    
    // Show the whole thing
    frame.setBounds(16,16,640,400);
    frame.show();
    
    // Start with a Graph or load a preset
    Runnable run = null;
    String preset = System.getProperty("gj.preset");
    if (preset==null) { 
      if (factories.length>0) 
        run = (Runnable)new ActionNewGraph(factories[0]).as(Runnable.class);
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
    for (int i=0;i<layouts.length;i++) {
      mLayout.add(new ActionSelectLayout(layouts[i]));
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
   * Creates a MutableGraph
   */
  private MutableGraph createMutableGraph(Rectangle2D dim) {
    MutableGraph result = new gj.model.impl.MutableGraphImpl();
    if (dim!=null) result.getBounds().setRect(dim);
    return result;
  }
  
  /**
   * Sets the graph we're looking at
   */
  private void setGraph(MutableGraph graph) {
    // remember
    this.graph=graph;
    // propagate
    graphWidget.setGraph(graph);
    layoutWidget.setGraph(graph);
  }
  
  /**
   * Creates a new graph
   */
  private void setGraph(Factory factory) {
    // let the user change some properties
    if (PropertyWidget.hasProperties(factory)) {
      PropertyWidget content = new PropertyWidget().setInstance(factory);
      int rc = SwingHelper.showDialog(graphWidget, "Graph Properties", content, SwingHelper.DLG_OK_CANCEL);
      if (SwingHelper.OPTION_OK!=rc) return;
      content.commit();
    }
    // create the graph      
    Rectangle2D box = new Rectangle2D.Double(0,0,graphScroll.getSize().width,graphScroll.getSize().height);
    MutableGraph graph = createMutableGraph(box);
    factory.create(graph,graphWidget.shapes[0]);
    setGraph(graph);
    // done
  }

  /**
   * How to handle - run a layout
   */
  protected class ActionExecuteLayout extends UnifiedAction {
    
    /** an animation that we're working on */
    private Animation animation;
    
    /** initializer (EDT) */
    protected boolean preExecute() throws LayoutException {
      // something to do?
      if (graph==null) return false;
      // something running?
      cancel(true);
      // the layout
      Layout layout = layoutWidget.getSelectedLayout();
      // tell the graph Widget
      graphWidget.setCurrentLayout(layout);
      // increase size of Graph to visible maximum for starters
      graph.getBounds().setRect(0,0,graphScroll.getWidth(), graphScroll.getHeight());
      // apply it?
      if (!isAnimation) {
        layout.applyTo(graph);
        // reflect change      
        graphWidget.revalidate();
        // dont' continue
        return false;
      }
      // create an animation
      animation = new Animation(graph, layout);
      // rip the layout from the graph widget so now 
      // intermediates are rendered
      graphWidget.setCurrentLayout(null);
      // continue
      return true;
    }
    
    /** async execute */
    protected void execute() throws LayoutException {
      try {
        while (true) {
          if (Thread.currentThread().isInterrupted()) break;
          if (!animation.perform()) break;
          graphWidget.revalidate();
        }
      } catch (InterruptedException e) {
        // ignore
      }
      graphWidget.setCurrentLayout(layoutWidget.getSelectedLayout());
    }
    
    /** postExecute (EDT) */
    protected void postExecute() {
    }
    
    /** whether we're async or not */
    protected int getAsync() {
      return isAnimation ? ASYNC_SAME_INSTANCE : ASYNC_NOT_APPLICABLE;
    }

  }
  
  /**
   * How to handle - switch to Layout
   */
  protected class ActionSelectLayout extends UnifiedAction {
    private Layout layout;
    protected ActionSelectLayout(Layout layout) {
      super(layout.toString());
      this.layout=layout;
    }
    public void execute() { 
      layoutWidget.setSelectedLayout(layout); 
    }
  }

  /**
   * How to handle - Load Graph
   */
  protected class ActionLoadGraph extends UnifiedAction {
    private File preset;
    protected ActionLoadGraph(File file) { 
      preset=file;
    }
    protected ActionLoadGraph() { super("Load"); }
    public void execute() throws IOException { 
      File file = preset;
      if (file==null) {
        JFileChooser fc = new JFileChooser(new File("./save"));
        if (JFileChooser.APPROVE_OPTION!=fc.showOpenDialog(frame)) return;
        file = fc.getSelectedFile();
      }
      MutableGraph result = createMutableGraph(null);
      new GraphReader(new FileInputStream(file)).read(result);
      setGraph(result);
    }
  }

  /**
   * How to handle - Save Graph
   */
  protected class ActionSaveGraph extends UnifiedAction {
    protected ActionSaveGraph() { super("Save"); }
    public void execute() throws IOException { 
      JFileChooser fc = new JFileChooser(new File("./save"));
      if (JFileChooser.APPROVE_OPTION!=fc.showSaveDialog(frame)) return;
      new GraphWriter(new FileOutputStream(fc.getSelectedFile())).write(graph);
    }
  }

  /**
   * How to handle - Close Graph
   */
  protected class ActionCloseGraph extends UnifiedAction {
    protected ActionCloseGraph() { super("Close"); }
    public void execute() { frame.dispose(); }
  }
  
  /**
   * How to handle - New Graph
   */
  protected class ActionNewGraph extends UnifiedAction {
    private Factory factory;
    protected ActionNewGraph(Factory factory) { 
      super(factory.getName());
      this.factory = factory;
    }
    public void execute() {
      setGraph(factory);
    }
  }

  /**
   * How to handle - Toggle antialiasing
   */
  protected class ActionToggleAntialias extends UnifiedAction {
    protected ActionToggleAntialias() { super("Antialiasing"); }
    public boolean isSelected() { return graphWidget.isAntialiasing(); }
    public void execute() {
      graphWidget.setAntialiasing(!graphWidget.isAntialiasing());
    }    
  }

  /**
   * How to handle - Toggle animation
   */
  protected class ActionToggleAnimation extends UnifiedAction {
    protected ActionToggleAnimation() { super("Animation"); }
    public boolean isSelected() { return isAnimation; }
    public void execute() {
      isAnimation = !isAnimation;
    }    
  }

}
