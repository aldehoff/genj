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
 */package gj.shell;

import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;
import gj.util.ArcHelper;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

/**
 * An animation
 */
/*package*/ class Animation {

  /** the graph */
  private Graph graph;
  
  /** the moves */
  private Movement[] moves;
  
  /** animation status */
  private long 
    totalTime = 1000    ,
    frameTime = 1000/60 ,
    lastFrame, 
    startFrame;
    
  /** an arc layout we use during the animation */
  private ArcHelper arcLayout = new ArcHelper();    
  
  /**
   * Constructor (before)
   */
  /*package*/ Animation(Graph graph) {
    
    // something to animate?
    if (graph.getNodes().isEmpty()) return;

    // keep some data
    this.graph = graph;

    // collect old values for nodes ...
    moves = new Movement[graph.getNodes().size()+graph.getArcs().size()];
    Iterator nodes = graph.getNodes().iterator();
    int m=0; for (;nodes.hasNext();m++) {
      Node node = (Node)nodes.next();
      moves[m]=new NodeMovement(node);
    }
    
    // .. and arcs
    Iterator arcs = graph.getArcs().iterator();
    for (;arcs.hasNext();m++) {
      Arc arc = (Arc)arcs.next();
      moves[m]=new ArcMovement(arc);
    }
  }
  
  /**
   * Start animation - after nodes and arcs are end-position
   */
  /*package*/ void start() {
        
    // collect new values
    for (int m=0;m<moves.length;m++) {
      moves[m].postLayout();
    }
    
    // perform 0D
    perform(moves,0D);
    
    // setup state
    startFrame = System.currentTimeMillis();
    lastFrame = startFrame;
    
  }

  /**
   * Stops the animation by setting it to the last frame
   */
  /*package*/ boolean stop() {
    // perform step to final frame
    if (moves!=null) perform(moves,1D);
    // stop all moves
    moves=null;
    // done
    return false;
  }

  /**
   * Runs 'one frame' of the animation
   * @return whether the animation is done
   */
  public boolean perform() throws InterruptedException {
    
    // Is it only one node?
    if ((moves==null)||(moves.length<2)) return stop();
    
    long now = System.currentTimeMillis();
    
    // has total passed already?
    if (startFrame+totalTime<now) return stop();

    // sleep some?
    long sleep = (lastFrame+frameTime)-now;
    if (sleep>0) Thread.currentThread().sleep(sleep); 
      
    // do the move
    double time = Math.min(1, ((double)now-startFrame)/totalTime);
    if (perform(moves, time)) return stop();
      
    // done
    lastFrame = now;
    return true;
  }
    
  /**
   * Performing one step in the animation
   */
  private boolean perform(Movement[] moves, double time) {
    
    boolean done = true;

    synchronized (graph) {
    
      // loop moves
      for (int m=0;m<moves.length;m++) {
        
        Movement move = moves[m];
        
        // do the move
        done &= move.perform(time);
        
      }
      
    }
        
    // done
    return done;
  }

  /**
   * An abstract movement in the animation
   */
  private abstract class Movement {
    abstract void postLayout();
    abstract boolean perform(double time);
  }

  /**
   * A movement of a node in the animation
   */
  private class NodeMovement extends Movement {
    private Node node;
    private Rectangle2D shape;
    private double oldx,oldy,newx,newy;
    NodeMovement(Node node) { 
      this.node=node; 
      this.shape=node.getShape().getBounds2D();
      oldx=node.getPosition().getX(); 
      oldy=node.getPosition().getY(); 
    }
    void postLayout() { 
      newx=node.getPosition().getX(); 
      newy=node.getPosition().getY(); 
    } 
    boolean perform(double time) {
      // calculate current position
      double
        currentx = oldx+(newx-oldx)*time,
        currenty = oldy+(newy-oldy)*time;
      // change position
      node.getPosition().setLocation(currentx,currenty);
      // done      
      return (Math.abs(currentx-newx)<1)&&(Math.abs(currenty-newy)<1);
    }
  }
  
  /**
   * A movement of an arc in the animation
   */
  private class ArcMovement extends Movement {
    private Arc arc;
    private Node start, end;
    private Shape shape;
    ArcMovement(Arc arc) {
      this.arc = arc;
      this.start = arc.getStart();
      this.end = arc.getEnd();
    }
    void postLayout() {
      this.shape = new GeneralPath(arc.getPath());
    }
    boolean perform(double time) {
      if (time<1)
        ArcHelper.update(arc);
      else 
        arc.getPath().set(shape);
      return true;
    }
  }
  
}
