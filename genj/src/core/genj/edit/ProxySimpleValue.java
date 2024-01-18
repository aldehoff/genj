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

import genj.util.swing.TextFieldWidget;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : UNKNOWN
 */
class ProxySimpleValue extends Proxy {

  /** members */
  private TextFieldWidget tfield;

  /**
   * Finish editing a property through proxy
   */
  protected void finish() {
    if (hasChanged())
      property.setValue(tfield.getText());
  }

  /**
   * Returns change state of proxy
   */
  protected boolean hasChanged() {
    return tfield!=null&&tfield.hasChanged();
  }

  /**
   * Start editing a property through proxy
   */
  protected JComponent start(JPanel in) {

    // readOnly()?
    if (property.isReadOnly()) {
      in.add(new JLabel(property.getValue()));
    } else {
      tfield = new TextFieldWidget(property.getValue(), 0);
      in.add(tfield);
    }
    
    // Done
    return tfield;
  }

} //ProxyUnknown
