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
package genj.edit;

import genj.gedcom.MultiLineSupport;
import genj.util.swing.TextAreaWidget;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : NAME
 */
class ProxyMLE extends Proxy {

  /** members */
  private TextAreaWidget tarea;

  /**
   * Finish editing a property through proxy
   */
  protected void finish() {

    if (hasChanged())
      property.setValue(tarea.getText());
  }

  /**
   * Returns change state of proxy
   */
  protected boolean hasChanged() {
    return tarea.hasChanged();
  }

  /**
   * Start editing a property through proxy
   */
  protected JComponent start(JPanel in) {

    // Calculate value to show
    String value;
    if (property instanceof MultiLineSupport) {
      value = ((MultiLineSupport)property).getAllLines();
    } else {
      value = property.getValue(); 
    }

    tarea = new TextAreaWidget(value,6,20);
    tarea.setLineWrap(true);
    tarea.setWrapStyleWord(true);

    JScrollPane spane = new JScrollPane(tarea);
    spane.setBorder(BorderFactory.createMatteBorder(4,4,4,4,in.getBackground()));
    spane.setAlignmentX(0);
    in.add(spane);

    // Done
    return tarea;
  }
  
} //ProxyMLE
