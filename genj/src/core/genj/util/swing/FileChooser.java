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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.*;
import java.io.File;

/**
 * Enhanced file chooser that accepts filter+description
 */
public class FileChooser extends JFileChooser {

  private String[] extensions;
  private String   description;

  private String command;

  private JFrame frame;

  /**
   * Filter Definition
   */
  class Filter extends FileFilter {

    // LCD

    /**
     * Files to accept
     */
    public boolean accept(File f) {

      // directory is o.k.
      if (f.isDirectory()) {
        return true;
      }

      String s= f.getName();
      int i =s.lastIndexOf('.');
      if (i>0) {

        for (int e=0;e<extensions.length;e++) {
          if (extensions[e].equals( s.substring(i+1).toLowerCase() )) {
            return true;
          }
        }

      }
      return false;
    }

    /**
     * Description
     */
    public String getDescription() {
      return description;
    }

    // EOC
  }

  /**
   * Constructor
   */
  public FileChooser(JFrame frame, String title, String command, String[] fileExtensions, String fileDescription, String baseDir) {
    super(baseDir!=null?baseDir:".");
    this.frame  =frame;
    this.command=command;
    extensions  = fileExtensions;
    description = fileDescription;

    Filter filter = new Filter();
    addChoosableFileFilter(filter);
    setFileFilter(filter);
    setDialogTitle(title);
  }

  /**
   * show it
   */
  public int showDialog() {
    return showDialog(frame,command);
  }
}
