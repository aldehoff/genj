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

import genj.util.ActionDelegate;
import genj.window.CloseWindow;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

/**
 * Widget for choosing a file with textfield and button
 */
public class FileChooserWidget extends JPanel {

  /** text field  */
  private TextFieldWidget text = new TextFieldWidget("", 12);
  
  /** file extensions */
  private String extensions;
  
  /** extensions for executables */
  public final static String EXECUTABLES = "exe, bin, sh, cmd, bat";
 
  /** 
   * constructor 
   */
  public FileChooserWidget() {
    this(null);
  }
  
  /** 
   * constructor 
   * @param extensions comma-separated list of extensions applying to selectable files
   */
  public FileChooserWidget(String extensions) {
    super(new BorderLayout());
    add(BorderLayout.CENTER, text);
    add(BorderLayout.EAST  , new ButtonHelper().setInsets(0).setFocusable(false).create(new Choose()));      
    this.extensions = extensions;
  }
  
  /**
   * Add listener
   */
  public void addChangeListener(ChangeListener l) {
    text.addChangeListener(l);
  }
  
  /**
   * Remove listener
   */
  public void removeChangeListener(ChangeListener l) {
    text.removeChangeListener(l);
  }
  
  /**
   * Whether there is an actual selection
   */
  public boolean isEmpty() {
    return text.isEmpty();
  }
  
  /**
   * Set current file selection
   */
  public void setFile(String file) {
    text.setText(file!=null ? file : "");
  }
  
  /**
   * Set current file selection
   */
  public void setFile(File file) {
    text.setText(file!=null ? file.toString() : "");
  }
  
  /**
   * Get current file
   */
  public File getFile() {
    return new File(text.getText());
  }
  
  /**
   * Choose with file dialog
   */
  private class Choose extends ActionDelegate {
    
    /** constructor */
    private Choose() {
      setText("...");
    }

    /** choose file */    
    protected void execute() {

      // create and show chooser      
      FileChooser fc = new FileChooser(FileChooserWidget.this, getName(), CloseWindow.TXT_OK, extensions, "/");
      fc.showDialog();
      
      // check result
      File file = fc.getSelectedFile();
      if (file!=null) text.setText(file.toString());
      
      // done
    }
    
  } //Choose
 
} //FileChooserWidget
