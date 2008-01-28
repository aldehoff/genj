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
package gj.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * A vertex implementation - suitable for on-the-fly/lazy generation
 */
public class DefaultVertex implements Vertex {
  
  private Object wrapped;
  
  public DefaultVertex(Object wrapped) {
    this.wrapped = wrapped;
  }
  
  @Override
  public int hashCode() {
    return wrapped.hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof DefaultVertex))
      return false;
    DefaultVertex that = (DefaultVertex)obj;
    return this.wrapped.equals(that.wrapped);
  }
  
  @Override
  public String toString() {
    return wrapped.toString();
  }

  public static Set<Vertex> wrap(Object[] verticies) {
    Set<Vertex> result = new LinkedHashSet<Vertex>();
    for (Object vertex : verticies)
      result.add(new DefaultVertex(vertex));
    return result;
  }
  
  public static Set<Vertex> wrap(Collection<?> verticies) {
    Set<Vertex> result = new LinkedHashSet<Vertex>();
    for (Object vertex : verticies)
      result.add(new DefaultVertex(vertex));
    return result;
  }
  
}
