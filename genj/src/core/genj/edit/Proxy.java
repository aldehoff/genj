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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import genj.gedcom.Property;
import genj.util.*;
import genj.util.swing.ImgIconConverter;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property
 */
abstract class Proxy  {

  /** the property that is proxy'd */
  protected Property prop;

  /**
   * Stop editing a property through proxy. Return <b>true</b>
   * in case that sub-properties have been created/removed.
   */
  protected abstract void finish();

  /**
   * Returns change state of proxy
   */
  protected abstract boolean hasChanged();

  /**
   * Start editing a property through proxy
   */
  protected abstract void start(JPanel in, JLabel setLabel, Property setProp, EditView edit);

  /**
   * Helper : generate JTextField
   */
  protected JTextField createTextField(String text, String name, DocumentListener listener, String tip) {

    // Here's the textfield
    JTextField result = new JTextField(text);

    // we'll make sure that it doesn't grow like crazy
    // because the box-layout wants to
    result.setMaximumSize(new Dimension(Integer.MAX_VALUE,result.getPreferredSize().height));

    // the alignment should be that the left's align
    result.setAlignmentX(0);

    // we also set the name (why?)
    result.setName(name);

    // and add an optional DocumentListener
    if (listener!=null) {
      result.getDocument().addDocumentListener(listener);
    }

    // tool tip
    if (tip!=null) {
      result.setToolTipText(tip);
    }

    // done
    return result;
  }

  /**
   * Helper : generate JLabel
   */
  protected JLabel createLabel(String text, String name) {
    JLabel result = new JLabel( text );
    result.setAlignmentX(0);
    result.setName(name);
    return result;
  }

  /**
   * Helper : generate JButton
   */
  protected JButton createButton(String text, String command, boolean enabled, ActionListener listener, ImgIcon icon) {

    JButton result = new JButton( text );
    result.addActionListener(listener);
    result.setActionCommand(command);
    result.setEnabled(enabled);
    if (icon!=null) {
      result.setIcon(ImgIconConverter.get(icon));
    }

    return result;
  }

}
