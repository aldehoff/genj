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
 * 
 * $Revision: 1.3 $ $Author: nmeier $ $Date: 2004-05-26 20:03:01 $
 */
package genj.option;

import genj.util.ActionDelegate;
import genj.util.swing.ButtonHelper;

import javax.swing.JComponent;

/**
 * A custom option with custom UI
 */
public abstract class CustomOption extends Option {

  /** reference widget */
  protected OptionsWidget widget;

  /** our ui */
  private UI ui = new UI();
  
  /** callback - ui access */
  public OptionUI getUI(OptionsWidget widget) {
    this.widget = widget;
    return ui;
  }
  
  /** 
   * implementation requirement - edit visually 
   */
  protected abstract void edit();
    
  /** 
   * Custom UI is a button only
   */
  private class UI extends ActionDelegate implements OptionUI {
    
    /** callback - text representation = none */
    public String getTextRepresentation() {
      return null;
    }

    /** callback - component representation = button */
    public JComponent getComponentRepresentation() {
      setText("...");
      return new ButtonHelper().setFocusable(false).create(this);
    }

    /** commit - noop */    
    public void endRepresentation() {
    }
    
    /** callback - button pressed */
    protected void execute() {
      edit();
    }
  
  } //UI

} //CustomOption
 