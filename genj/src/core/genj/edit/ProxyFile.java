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

import genj.edit.actions.RunExternal;
import genj.gedcom.GedcomException;
import genj.gedcom.PropertyBlob;
import genj.gedcom.PropertyFile;
import genj.io.FileAssociation;
import genj.util.ActionDelegate;
import genj.util.EnvironmentChecker;
import genj.util.Origin;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.MenuHelper;
import genj.util.swing.TextFieldWidget;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : FILE / BLOB
 */
// FIXME checkout dpi of pic
class ProxyFile extends Proxy {

  /** static image dir */
  private final static String 
    IMAGE_DIR = EnvironmentChecker.getProperty(Proxy.class, 
      new String[]{ "genj.gedcom.dir", "user.home" },
      ".",
      "resolve image directory"
    );


  /** preview */
  private JLabel       lImage;
  
  /** enter text */
  private TextFieldWidget tFile;

  /**
   * Finish editing a property through proxy
   */
  protected void finish() {
    // changes?
    if (!hasChanged()) return;
    // propagate
    if (property instanceof PropertyFile)
      property.setValue(tFile.getText());
    if (property instanceof PropertyBlob) try {
      ((PropertyBlob)property).load(tFile.getText());
    } catch (GedcomException e) {}

    // done
  }

  /**
   * Returns change state of proxy
   */
  protected boolean hasChanged() {
    return tFile.hasChanged();
  }

  /**
   * Shows property's image if possible
   */
  private void showFile(String file) {

    // show value
    tFile.setText(file);

    // try to open
    try {
      Origin.Connection c = property.getGedcom().getOrigin().openFile(file);
      lImage.setIcon(new ImageIcon(c.getInputStream()));
      lImage.setText("");
    } catch (Throwable t) {
      lImage.setText("No preview available");
      lImage.setIcon(null);
    }

    // Update visually
    lImage.revalidate();
  }

  /**
   * Shows property's image if possible
   */
  private void showFile(PropertyBlob blob) {

    // show value
    tFile.setText(blob.getValue());
    tFile.setTemplate(true);

    // try to open
    ImageIcon img = blob.getValueAsIcon();
    if (img!=null) {
      lImage.setIcon(img);
      lImage.setText("");
    } else {
      lImage.setText("No preview available");
      lImage.setIcon(null);
    }

    // Update visually
    lImage.revalidate();
  }

  /**
   * Start editing a property through proxy
   */
  protected JComponent start(JPanel in) {
    
    // Create Text and button for current value
    tFile = new TextFieldWidget("", 80);
    
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
    p.add(tFile);
    p.add(new ButtonHelper().create(new ActionChoose()));
    p.setAlignmentX(0);
    in.add(p);

    // Any graphical information that could be shown ?
    lImage = new JLabel();
    lImage.addMouseListener(new Popup());
    lImage.setHorizontalAlignment(SwingConstants.CENTER);
    
    JScrollPane scroll = new JScrollPane(lImage);
    scroll.setAlignmentX(0);
    in.add(scroll);

    // display what we've got
    if (property instanceof PropertyFile)
      showFile(((PropertyFile)property).getValue());
    if (property instanceof PropertyBlob)
      showFile((PropertyBlob)property);
      
    // Done
    return null;
  }

  /**
   * Popup   */
  private class Popup extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
      mouseReleased(e);
    }
    public void mouseReleased(MouseEvent e) {
      // no popup trigger no action
      if (!e.isPopupTrigger()) 
        return;
      // show a context menu for file
      String file = tFile.getText();
      MenuHelper mh = new MenuHelper().setTarget(view);
      JPopupMenu popup = mh.createPopup("");
      // lookup associations
      String suffix = PropertyFile.getSuffix(file);
      Iterator it = FileAssociation.get(suffix).iterator();
      while (it.hasNext()) {
        FileAssociation fa = (FileAssociation)it.next(); 
        mh.createItem(new RunExternal(property.getGedcom(),file,fa));
      }
      // show
      if (popup.getComponentCount()>0)
        popup.show(lImage, e.getPoint().x, e.getPoint().y);
      // done
    }
  } //Popup

  /**
   * Action - Choose file
   */
  private class ActionChoose extends ActionDelegate {
    /**
     * Constructor
     */
    protected ActionChoose() {
      setText(">>");
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      
      // Let the user choose a file
      JFileChooser chooser = new JFileChooser(IMAGE_DIR);
      chooser.setDialogTitle("Choose a file");

      int rc=chooser.showDialog(view, "Choose file");

      // Cancel ?
      if (JFileChooser.APPROVE_OPTION != rc)
        return;
        
      // show it 
      showFile(chooser.getSelectedFile().toString());
      
      // remember changed
      tFile.setChanged(true);

    }
  } //ActionChoose
  
} //ProxyFile
