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
package genj.print;

import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.window.WindowManager;

import java.util.logging.Logger;

import javax.print.PrintException;
import javax.swing.JComponent;

/**
 * A manager for printing */
public class PrintManager {
  
  /*package*/ static Logger LOG = Logger.getLogger("genj.print");
 
  /** singleton */
  private static PrintManager instance = null;
  
  /** resources */
  /*package*/ Resources resources = Resources.get(PrintManager.class);
  
  /** window manager */
  private WindowManager winMgr;
  
  /**
   * Constructor   */
  public PrintManager(WindowManager winManager) {
    winMgr = winManager;
  }
  
  /**
   * Prints a view
   */
  public void print(Printer printer, String title, JComponent owner, Registry registry) {
    try {
      new PrintTask(this, printer, title, owner, new PrintRegistry(registry, "print")).trigger();
    } catch (PrintException e) {
      winMgr.openDialog(null, resources.getString("printer"), WindowManager.ERROR_MESSAGE, resources.getString("unavailable"), Action2.okOnly(), owner);
    }
  }
  
  /**
   * Access to window manager
   */
  public WindowManager getWindowManager() {
    return winMgr;
  }
  
} //PrintManager
