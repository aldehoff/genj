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

import gj.io.GraphReader;
import gj.io.GraphWriter;
import gj.layout.Layout;
import gj.layout.LayoutException;
import gj.layout.random.RandomLayout;
import gj.model.factory.AbstractGraphFactory;
import gj.shell.model.ShellFactory;
import gj.shell.model.ShellGraph;
import gj.shell.swing.SwingHelper;
import gj.shell.swing.UnifiedAction;
import gj.shell.util.Properties;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;

/**
 * A Shell for GraphJ's core functions
 */
public class Shell {

  /** the frame we use */
  private JFrame frame;
  
  /** the graph widget we show */
  private GraphWidget graphWidget;

  /** the layout widget we show */
  private LayoutWidget layoutWidget;
  
  /** the graph we're looking at */
  private ShellGraph graph;
  
  /** whether we perform an animation after layout */
  private boolean isAnimation = true;
  
  /** an animation that is going on */
  private Animation animation;
  
  /** our properties */
  private Properties properties = new Properties(Shell.class, "shell.properties");
  
  /** our factories */
  private AbstractGraphFactory[] factories = (AbstractGraphFactory[])properties.get("factory", new AbstractGraphFactory[0]);
  
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
    container.add(graphWidget, BorderLayout.CENTER);
    container.add(layoutWidget, BorderLayout.EAST  );
    
    // Our default button
    frame.getRootPane().setDefaultButton(layoutWidget.getDefaultButton());
    
    // Setup Menu
    frame.setJMenuBar(createMenu());
    
    // Show the whole thing
    frame.setBounds(128,128,480,480);
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
   * Sets the graph we're looking at
   */
  private void setGraph(ShellGraph grAph) {
    // remember
    graph = grAph;
    // propagate
    graphWidget.setGraph(graph);
    layoutWidget.setGraph(graph);
  }
  
  /**
   * Creates a new graph
   */
  private void setGraph(AbstractGraphFactory factory) {
    // let the user change some properties
    if (PropertyWidget.hasProperties(factory)) {
      PropertyWidget content = new PropertyWidget().setInstance(factory);
      int rc = SwingHelper.showDialog(graphWidget, "Graph Properties", content, SwingHelper.DLG_OK_CANCEL);
      if (SwingHelper.OPTION_OK!=rc) return;
      content.commit();
    }
    // create the graph
    ShellGraph grAph = (ShellGraph)factory.create(
      new ShellFactory(graphWidget.shapes[0]),
      createGraphBounds()
    );
    setGraph(grAph);    
    // done
  }
  
  /**
   * Creates a graph bounds rectangle
   */
  private Rectangle createGraphBounds() {
    return new Rectangle(0,0,graphWidget.getWidth()-32,graphWidget.getHeight()-32);
  }
  
  /**
   * How to handle - run a layout
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
    protected boolean preExecute() throws LayoutException {
      // something to do?
      if (graph==null) return false;
      // something running?
      cancel(true);
      // the layout
      Layout layout = layoutWidget.getSelectedLayout();
      // tell the graph Widget
      graphWidget.setCurrentLayout(layout);
      // create animation (it'll take a snapshot of current configuration)
      animation = new Animation(graph);
      // apply layout
      Rectangle bounds = createGraphBounds();
      try {
        layout.layout(graph, bounds);
      } catch (LayoutException e) {
        new RandomLayout().layout(graph, bounds);
        // can't handle it really
        throw e;
      }
      // continue into animation?
      if (!isAnimation)
        return false;
      // start it
      animation.start();
      // rip the layout from the graph widget  
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
          graphWidget.setGraph(graph);
        }
      } catch (InterruptedException e) {
        // ignore
      }
      graphWidget.setGraph(graph);
      graphWidget.setCurrentLayout(layoutWidget.getSelectedLayout());
    }
    
  } //ActionExecuteLayout
  
  /**
   * How to handle - switch to Layout
   */
  /*package*/ class ActionSelectLayout extends UnifiedAction {
    private Layout layout;
    /*package*/ ActionSelectLayout(Layout layout) {
      super(layout.toString());
      this.layout=layout;
    }
    protected void execute() { 
      layoutWidget.setSelectedLayout(layout); 
    }
  }

  /**
   * How to handle - Load Graph
   */
  /*package*/ class ActionLoadGraph extends UnifiedAction {
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
      ShellGraph grAph = (ShellGraph)new GraphReader(new FileInputStream(file))
        .read(new ShellFactory(graphWidget.shapes[0]));
      setGraph(grAph);
    }
  }

  /**
   * How to handle - Save Graph
   */
  /*package*/ class ActionSaveGraph extends UnifiedAction {
    /*package*/ ActionSaveGraph() { super("Save"); }
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
