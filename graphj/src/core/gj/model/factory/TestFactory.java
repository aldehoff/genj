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
package gj.model.factory;

import gj.model.Factory;
import gj.model.Graph;
import gj.model.Node;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

/**
 * Factory for testing purposes
 */
public class TestFactory extends AbstractGraphFactory {

  /**
   * @see gj.model.factory.AbstractGraphFactory#create(gj.model.Factory, java.awt.geom.Rectangle2D)
   */
  public Graph create(Factory factory, Rectangle2D bounds) {

    Rectangle shape = new Rectangle(-10,-10, 20, 20);

    Graph graph = factory.createGraph();
   
    Node mom = factory.createNode(graph, shape, "mom");
    mom.getPosition().setLocation(-30,0);
    
    Node dad = factory.createNode(graph, shape, "dad");
    dad.getPosition().setLocation( 30,0);
    

    Graph sub = factory.createGraph();
    
    Node lars = factory.createNode(sub, shape, "lars");
    lars.getPosition().setLocation(-30,0);
    Node sven = factory.createNode(sub, shape, "sven");
    sven.getPosition().setLocation(  0,0);
    Node nils = factory.createNode(sub, shape, "nils");
    nils.getPosition().setLocation( 30,0);

    Node brothers = factory.createNode(graph, new Rectangle(-50,-20, 100, 40), sub);
    brothers.getPosition().setLocation( 0,50);
    
    return graph;
  }

} //TestFactory