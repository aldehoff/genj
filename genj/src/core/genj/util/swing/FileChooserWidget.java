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
import genj.util.EnvironmentChecker;
import genj.window.CloseWindow;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

/**
 * Widget for choosing a file with textfield and button
 */
public class FileChooserWidget extends JPanel {

  /** text field  */
  private TextFieldWidget text = new TextFieldWidget("", 12);
  
  /** button */
  private AbstractButton button;
  
  /** file extensions */
  private String extensions;
  
  /** extensions for executables */
  public final static String EXECUTABLES = "exe, bin, sh, cmd, bat";
  
  /** start directory */
  private String directory = EnvironmentChecker.getProperty(this, "user.home", ".", "file chooser directory");
  
  /** an accessory if any */
  private JComponent accessory;
  
  /** action listeners */
  private List listeners = new ArrayList();
 
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
    
    button = new ButtonHelper().setInsets(0).setFocusable(false).create(new Choose());
    text.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fireActionEvent();
      }
    });
    
    add(BorderLayout.CENTER, text );
    add(BorderLayout.EAST  , button);      
    this.extensions = extensions;
  }
  
  /**
   * fire action event
   */
  private void fireActionEvent() {
    ActionEvent e = new ActionEvent(this, 0, "");
    ActionListener[] ls = (ActionListener[])listeners.toArray(new ActionListener[listeners.size()]);
    for (int i = 0; i < ls.length; i++)
      ls[i].actionPerformed(e);
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
   * Add listener
   */
  public void addActionListener(ActionListener l) {
    listeners.add(l);
  }
  
  /**
   * Remove listener
   */
  public void removeActionListener(ActionListener l) {
    listeners.remove(l);
  }
  
  /**
   * Setter - a start directory
   */
  public void setDirectory(String set) {
    directory = set;
  }
  
  /**
   * Getter - 'current' directory
   */
  public String getDirectory() {
    return directory;
  }
  
  /**
   * Whether there is an actual selection
   */
  public boolean isEmpty() {
    return text.isEmpty();
  }
  
  /**
   * Sets an image to use
   */
  public void setImage(ImageIcon image) {
    button.setIcon(image);
    button.setText(null);
  }
  
  /**
   * Makes current text in chooser a template
   */
  public void setTemplate(boolean set) {
    text.setTemplate(set);
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
   * Setter - An accessory
   */
  public void setAccessory(JComponent set) {
    accessory = set;
  }

  /**
   * Focus goes to entry field
   */  
  public boolean requestFocusInWindow() {
    return text.requestFocusInWindow();
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
      FileChooser fc = new FileChooser(FileChooserWidget.this, getName(), CloseWindow.TXT_OK, extensions, directory);
      fc.setAccessory(accessory);
      fc.showDialog();
      
      // check result
      File file = fc.getSelectedFile();
      if (file!=null)  {
        setFile(file);
        directory = file.getParent();
      }
      
      // notify
      fireActionEvent();
      
      // done
    }
    
  } //Choose
 
} //FileChooserWidget
