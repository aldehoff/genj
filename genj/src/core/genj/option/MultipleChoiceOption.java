/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.option;

import java.lang.reflect.Field;

/**
 * An option based on multiple choices
 * @author nmeier
 */
public class MultipleChoiceOption extends Option {
  
  /** available choices */
  private Object[] choices;

  /**
   * Constructor
   */
  public MultipleChoiceOption(Object inStance, String naMe, Field fiEld, Object[] choIces) {

    super(inStance, naMe, fiEld);

    // remember choices
    choices = choIces;
    
    System.out.print(name+":");
    for (int i = 0; i < choIces.length; i++) {
    	System.out.print(choices[i]+",");
    }
    System.out.println();
    // done
  }
  
  /**
   * The choices
   */
  public Object[] getChoices() {
    return choices;    
  }
  
  /**
   * @see genj.option.Option#setValue(java.lang.Object)
   */
  public void setValue(Object value) {
    // one of the known choices?
    for (int i=0; i<choices.length; i++) {
      if (choices[i]==value) {
        // .. translate to index
        setValue(new Integer(i));
        return;
      }
    }
    // continue unchanged
    super.setValue(value);
  }

  /**
   * @see genj.option.Option#getValue()
   */
  public Object getValue() {
    // get current
    Object value = super.getValue();
    // one of the known choices?
    if (value instanceof Integer) {
      int i = ((Integer)value).intValue();
      if (i>=0&&i<choices.length)
        return choices[i];   
    }
    // continue unchanged
    return value;
  }

  

} //MultipleChoiceOption