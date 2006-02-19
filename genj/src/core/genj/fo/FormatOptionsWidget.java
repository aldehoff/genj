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
package genj.fo;

import genj.util.Registry;
import genj.util.swing.FileChooserWidget;
import genj.util.swing.NestedBlockLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A widget for setting formatted output options - choose
 * <il>
 * <li>output file
 * <li>format to use
 * <li>format parameters
 * </il>
 */
public class FormatOptionsWidget extends JPanel {
  
  private Action validAction;
  private Document doc;
  private FileChooserWidget chooseFile;
  private JComboBox chooseFormat;
  
  /**
   * Constructor
   */
  public FormatOptionsWidget(Document document, Registry registry) {
    
    setLayout(new NestedBlockLayout("<col><row><label/><file wx=\"1\"/></row><row><label/><format wx=\"1\"/></row></col>"));

    doc = document;
    
    // let user choose an output file
    chooseFile  = new FileChooserWidget();
    String file = registry.get("file", (String)null);
    if (file!=null) {
      File f = new File(file);
      chooseFile.setFile(f);
      chooseFile.setDirectory(f.getParent());
    }
    add(new JLabel("File"));
    add(chooseFile);
    
    // and the output format as well (restoring old selection)
    chooseFormat = new JComboBox(Format.getFormats());
    chooseFormat.setSelectedItem(Format.getFormat(registry.get("format", (String)null)));
    chooseFormat.setEditable(false);
    add(new JLabel("Format"));
    add(chooseFormat);

    // listen to some events
    chooseFormat.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        validateOptions();
      }
    });
    
    // done for now
  }
  
  /**
   * Remember selections
   */
  public void remember(Registry registry) {
    registry.put("format", getFormat().getFormat());
    registry.put("file", getFile().getAbsolutePath());
  }
  
  /**
   * selected format
   */
  public Format getFormat() {
    return (Format)chooseFormat.getSelectedItem();
  }

  /**
   * selected file
   */
  public File getFile() {
    Format format = getFormat();
    File result = chooseFile.getFile();
    // strip any of our known extensions
    if (format.getFileExtension()==null)
      return  result;
    String path = result.getAbsolutePath();
    Format[] formats = Format.getFormats();
    for (int f=0;f<formats.length;f++) {
      String suffix = "."+formats[f].getFileExtension();
      if (path.endsWith(suffix)) {
        path = path.substring(0, path.length()-suffix.length());
      }
    }
    // add well known extension
    return new File(path+"."+format.getFileExtension());
  }
  
  /**
   * sets the action that we want this widget to manage, enabled if options
   * are ok, disabled otherwise 
   */
  public void connect(Action validAction) {
    this.validAction = validAction;
    validateOptions();
  }
  
  private void validateOptions() {
    
    Format format = getFormat();
    boolean valid = true;
    
    // check file
    if (format.getFileExtension()!=null&&chooseFile.isEmpty())
      valid = false;
    
    // check file support
    chooseFile.setEnabled(format.getFileExtension()!=null);
    
    // check document support
    if (!format.supports(doc))
      valid = false;
    
    // update filename
    chooseFile.setFile(getFile());
    
    // update valid action
    validAction.setEnabled(valid);
  }

} //OutputWidget
