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
package genj.util.swing;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 * A patched JToggleButton
 */
public class ToggleWidget extends JToggleButton {
  
  /**
   * Constructor  
   */
  public ToggleWidget() {
  }

  /**
   * Constructor  
   */
  public ToggleWidget(Icon icon) {
    this(null, icon);
  }

  /**
   * Constructor  
   */
  public ToggleWidget(String text) {
    this(text, null);
  }

  /**
   * Constructor  
   */
  public ToggleWidget(String text, Icon icon) {
    super(text, icon);
  }

  /**
   * In pre 1.4 world the insets for metal buttons can be
   * really strange - patching
   * @see javax.swing.JComponent#getInsets()
   */
  public Insets getInsets() {
    Insets result = super.getInsets();
    int min = Math.min(result.top, Math.min(result.bottom, Math.min(result.left, result.right)));
    result.top    = min;
    result.bottom = min;
    result.left   = min;
    result.right  = min;
    return result;
  }

  /**
   * @see javax.swing.JComponent#addNotify()
   */
  public void addNotify() {
    // delegate
    super.addNotify();
    // patch max size in toolbar (helps buttons look uniformly in toolbar)
    if (getToolBar()!=null) 
      setMaximumSize(new Dimension(128,128));
    // done
  }
  
  /**
   * Gets the toolbar we're in (might be null)
   */
  protected JToolBar getToolBar() {
    if (!(getParent() instanceof JToolBar)) return null;
    return (JToolBar)getParent();
  }

} //ToggleWidget
