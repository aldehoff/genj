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

import gj.model.MutableGraph;
import gj.model.Node;
import gj.model.impl.MutableGraphImpl;
import gj.util.ModelHelper;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 * Factory for testing purposes
 */
public class TestFactory extends AbstractFactory implements Factory {

  /**
   * @see gj.model.factory.Factory#create(gj.model.MutableGraph, java.awt.geom.Rectangle2D, java.awt.Shape)
   */
  public Rectangle2D create(MutableGraph graph, Rectangle2D bounds, Shape nodeShape) {
   
    Rectangle shape = new Rectangle(-10,-10,20,20);
   
    Node mom = graph.addNode(getPoint(-30,0), shape, "mom"); 
    Node dad = graph.addNode(getPoint( 30,0), shape, "dad");
    
    MutableGraph sub = new MutableGraphImpl();
    Node lars =  sub.addNode(getPoint(-30,0), shape, "lars"); 
    Node sven =  sub.addNode(getPoint(  0,0), shape, "sven"); 
    Node nils =  sub.addNode(getPoint(+30,0), shape, "nils"); 

    graph.addNode(getPoint(00,70), new Rectangle(-50,-20, 100, 40), sub);

    return ModelHelper.getBounds(graph.getNodes());
  }

} //TestFactory