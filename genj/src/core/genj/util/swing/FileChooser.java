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

import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Enhanced file chooser that accepts filter+description
 */
public class FileChooser extends JFileChooser {

  /** the textual command on the non-cancel button */
  private String command;

  /** the owning component */
  private JComponent owner;

  /**
   * Constructor
   */
  public FileChooser(JComponent owner, String title, String command, String extension, String baseDir) {
    
    super(baseDir!=null?baseDir:".");
    
    this.owner  = owner;
    this.command= command;

    Filter filter = new Filter(extension);
    addChoosableFileFilter(filter);
    setFileFilter(filter);
    setDialogTitle(title);
  }

  /**
   * show it
   */
  public int showDialog() {
    int rc = showDialog(owner,command);
    // unselect selected file if not 'ok'
    if (rc!=0)
      setSelectedFile(null);
    return rc;
  }


  /**
   * Filter Definition
   */
  private class Filter extends FileFilter {
    
    /** extension we're looking for */
    private String ext;

    /**
     * Constructor
     */
    private Filter(String extension) {
      ext = extension;
    }

    /**
     * Files to accept
     */
    public boolean accept(File f) {

      // directory is o.k.
      if (f.isDirectory())
        return true;

      // check extension
      String name = f.getName();
      
      int i = name.lastIndexOf('.');
      
      return i>0 && ext.equals(name.substring(i+1).toLowerCase()); 
    }

    /**
     * Description
     */
    public String getDescription() {
      return "*."+ext;
    }

  } //Filter

} //FileChooser
