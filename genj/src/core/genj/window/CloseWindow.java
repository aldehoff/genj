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
package genj.window;

import genj.util.ActionDelegate;

import javax.swing.JDialog;
import javax.swing.UIManager;

/**
 * An action that closes a dialog/window
 */
public class CloseWindow extends ActionDelegate {

  /** predefined strings */
  public final static String
    TXT_YES         = UIManager.getString("OptionPane.yesButtonText"),
    TXT_NO          = UIManager.getString("OptionPane.noButtonText"),
    TXT_OK          = UIManager.getString("OptionPane.okButtonText"),
    TXT_CANCEL      = UIManager.getString("OptionPane.cancelButtonText");

  /** a dialog that will be closed */
  private JDialog dialog;
  
  /** whether this action was performed */
  private boolean isPerformed = false;
  
  /** 
   * predefined yesno
   */
  public static CloseWindow[] YESandNO() {
    return new CloseWindow[] { new CloseWindow(TXT_YES), new CloseWindow(TXT_NO) };
  }

  /** 
   * predefined yesnocancel
   */
  public static CloseWindow[] YESandNOandCancel() {
    return new CloseWindow[] { new CloseWindow(TXT_YES), new CloseWindow(TXT_NO), new CloseWindow(TXT_CANCEL) };
  }

  /** 
   * predefined okcancel
   */
  public static CloseWindow[] OKandCANCEL() {
    return new CloseWindow[] { new CloseWindow(TXT_OK), new CloseWindow(TXT_CANCEL) };
  }

  /** 
   * predefined ok
   */
  public static CloseWindow[] OK() {
    return new CloseWindow[]{ new CloseWindow(TXT_OK) };
  }

  /** 
   * predefined cancel
   */
  public static CloseWindow[] CANCEL() {
    return new CloseWindow[]{ new CloseWindow(TXT_CANCEL) };
  }
  
  /**
   * predefined ok/custom
   */
  public static CloseWindow[] OKand(String other) {
    return new CloseWindow[]{ new CloseWindow(TXT_OK), new CloseWindow(other) };
  }

  /**
   * predefined custom/cancel
   */
  public static CloseWindow[] andCANCEL(String other) {
    return new CloseWindow[]{ new CloseWindow(other), new CloseWindow(TXT_CANCEL) };
  }

  /**
   * Constructor
   */
  public CloseWindow(String txt) {
    setText(txt);
  }

  /** 
   * close main
   */
  protected void execute() {
    if (dialog!=null) {
      isPerformed = true;
      dialog.dispose();
    }
  }
  
  /**
   * connect to dialog
   */
  /*package*/ void setDialog(JDialog dlg) {
    dialog = dlg;
  }
  
  /**
   * resolve whether fired
   */
  /*package*/ boolean isPerformed() {
    return isPerformed;
  }

} //WindowClose
