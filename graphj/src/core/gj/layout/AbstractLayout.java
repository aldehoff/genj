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
package gj.layout;

import gj.util.*;
import gj.util.*;

/**
 * Base for Layouts (optional)
 */
public abstract class AbstractLayout implements Layout {
  
  /** whether we debug or not */
  private boolean isDebug = false;
  
  /** an arc layout for convenience */
  protected ArcHelper arcLayout = new ArcHelper();

  /**
   * Getter - debug
   */
  public boolean isDebug() {
    return isDebug;
  }
  
  /**
   * Setter - debug
   */
  public void setDebug(boolean set) {
    isDebug=set;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    String s = getClass().getName();
    return s.substring(s.lastIndexOf('.')+1);
  }

} //AbstractLayout
