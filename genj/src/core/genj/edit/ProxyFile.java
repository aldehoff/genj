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
import genj.util.swing.UnitGraphics;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : FILE / BLOB
 */
/*package*/ class ProxyFile extends Proxy {

  /** static image dir */
  private final static String 
    IMAGE_DIR = EnvironmentChecker.getProperty(Proxy.class, 
      new String[]{ "genj.gedcom.dir", "user.home" },
      ".",
      "resolve image directory"
    );


  /** preview */
  private Preview preview;
  
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
    tFile.setTemplate(false);
    tFile.setText(file);

    // try to open
    try {
      Origin.Connection c = property.getGedcom().getOrigin().openFile(file);
      preview.setImage(file, c.getInputStream());
    } catch (Throwable t) {
      preview.setImage(null, null);
    }

    // done
  }

  /**
   * Shows property's image if possible
   */
  private void showFile(PropertyBlob blob) {

    // show value
    tFile.setText(blob.getValue());
    tFile.setTemplate(true);

    // try to open
    byte[] data = blob.getBlobData();
    if (data!=null) {
      preview.setImage(blob.getTitle(),new ByteArrayInputStream(data));
    } else {
      preview.setImage(null, null);
    }

    // done
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
    preview = new Preview();
    
    JScrollPane scroll = new JScrollPane(preview);
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
  
  /**
   * Action - zoom
   */
  private class ActionZoom extends ActionDelegate {
    /** the level of zoom */
    private int zoom;
    /**
     * Constructor
     */
    protected ActionZoom(int zOOm) {
      zoom = zOOm;
      setText(zoom+"%");
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      preview.setZoom(zoom);
    }
  } //ActionZoom
  
  /**
   * Preview
   */
  private class Preview extends JComponent implements MouseListener {
    /** our image */
    private ImageIcon img;
    /** our zoom */
    private int zoom;
    /**
     * Constructor
     */
    protected Preview() {
      addMouseListener(this);
      setZoom(view.registry.get("file.zoom", 100));
    }
    /**
     * Sets the zoom level
     */
    protected void setZoom(int zOOm) {
      zoom = zOOm;
      view.registry.put("file.zoom", zoom);
      setToolTipText(zoom+"%");
      revalidate();
      repaint();
    }
    /**
     * Sets the image to preview
     */
    protected void setImage(String name, InputStream in) {
      if (in==null) {
        img = null;
      } else {
        img = new ImageIcon(name, in);
      }
      revalidate();
      repaint();
    }
    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g) {
      // no image?
      if (img==null) return;
      // Paint in physical size
      UnitGraphics ug = new UnitGraphics(g, 1, 1);
      
      // calculate factor - the image's dpi might be
      // different than that of the rendered surface
      float factor = (float)zoom/100;
      double 
        scalex = factor,
        scaley = factor;
        
      Point idpi = img.getResolution();
      if (idpi!=null) {
        Point dpi = view.manager.getDPI();
        
        scalex *= (double)dpi.x/idpi.x;
        scaley *= (double)dpi.y/idpi.y;
      }
      
      ug.scale(scalex,scaley);
      // paint
      ug.draw(img, 0, 0, 0, 0);
      // done
    }
    /**
     * @see javax.swing.JComponent#getPreferredSize()
     */
    public Dimension getPreferredSize() {
      // no image?
      if (img==null) return new Dimension(0,0);
      // check physical size
      Dimension dim = img.getSize(view.manager.getDPI());
      float factor = (float)zoom/100;
      dim.width *= factor;
      dim.height *= factor;
      return dim;
    }
    /**
     * callback - mouse pressed
     */
    public void mousePressed(MouseEvent e) {
      mouseReleased(e);
    }
    /**
     * callback - mouse released
     */
    public void mouseReleased(MouseEvent e) {
      // no popup trigger no action
      if (!e.isPopupTrigger()) 
        return;
      // show a context menu for file
      String file = tFile.getText();
      MenuHelper mh = new MenuHelper().setTarget(view);
      JPopupMenu popup = mh.createPopup("");
      // zoom levels
      mh.createItem(new ActionZoom( 10));
      mh.createItem(new ActionZoom( 50));
      mh.createItem(new ActionZoom(100));
      mh.createItem(new ActionZoom(150));
      mh.createItem(new ActionZoom(200));
      // lookup associations
      String suffix = PropertyFile.getSuffix(file);
      Iterator it = FileAssociation.get(suffix).iterator();
      while (it.hasNext()) {
        FileAssociation fa = (FileAssociation)it.next(); 
        mh.createItem(new RunExternal(property.getGedcom(),file,fa));
      }
      // show
      if (popup.getComponentCount()>0)
        popup.show(this, e.getPoint().x, e.getPoint().y);
      // done
    }
    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
      // ignored
    }
    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
      // ignored
    }
    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
      // ignored
    }

  } //Preview
  
} //ProxyFile
