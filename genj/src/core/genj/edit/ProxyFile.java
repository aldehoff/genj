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
import genj.gedcom.PropertyBlob;
import genj.gedcom.PropertyFile;
import genj.io.FileAssociation;
import genj.util.ActionDelegate;
import genj.util.EnvironmentChecker;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.MenuHelper;
import genj.util.swing.TextFieldWidget;
import genj.util.swing.UnitGraphics;
import genj.window.WindowManager;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FilePermission;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
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

  /** one loader per view */
  private static Map view2loader = new HashMap();
  
  /** whether we'll update title/format or not */
  private boolean updateFormatAndTitle = true;

  /**
   * Finish editing a property through proxy
   */
  protected void finish() {
    
    // changes?
    if (!hasChanged()) return;
    
    // propagate
    String file = tFile.getText();
    
    if (property instanceof PropertyFile)
      ((PropertyFile)property).setValue(file, updateFormatAndTitle);
    
    if (property instanceof PropertyBlob) 
      ((PropertyBlob)property).load(file, updateFormatAndTitle);

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
  private void showFile(String file, boolean warnAboutSize) {

    // show value
    tFile.setTemplate(false);
    tFile.setText(file);

    // load it
    synchronized (view2loader) {
      Loader loader = (Loader)view2loader.get(view);
      if (loader!=null) {
        loader.cancel(true);
      }
      loader = new Loader(file, warnAboutSize);
      view2loader.put(view, loader);
      loader.trigger();
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

    // .. preview
    try {
      preview.setImage(new ImageIcon(blob.getTitle(), blob.getBlobData()));
    } catch (Throwable t) {
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
    p.setAlignmentX(0);
    in.add(p);

    // but check permissions first
    try {

      SecurityManager sm = System.getSecurityManager();
      if (sm!=null) sm.checkPermission( new FilePermission(IMAGE_DIR, "read"));      
      
      // a text-field and button for file
      p.add(tFile);
      p.add(new ButtonHelper().create(new ActionChoose()));

    } catch (SecurityException se) {
    }

    // Any graphical information that could be shown ?
    preview = new Preview();
    
    JScrollPane scroll = new JScrollPane(preview);
    scroll.setAlignmentX(0);
    in.add(scroll);

    // display what we've got
    if (property instanceof PropertyFile)
      showFile(((PropertyFile)property).getValue(), false);
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
      
      // a simple button with only an image
      setImage(property.getImage(false));

      // done
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      
      
      // Let the user choose a file
      JFileChooser chooser = new JFileChooser(IMAGE_DIR);
      chooser.setControlButtonsAreShown(false);
      chooser.setBorder(null);
      JCheckBox check = new JCheckBox(view.resources.getString("proxy.file.update"), false);

      int option = view.manager.getWindowManager().openDialog( 
        null, view.resources.getString("proxy.file.title"), WindowManager.IMG_QUESTION, 
        new JComponent[]{chooser,check}, 
        WindowManager.OPTIONS_OK_CANCEL, 
        view 
      );
      
      File file = chooser.getSelectedFile();
      if (option!=0||file==null) 
        return;
      
      // remember
      updateFormatAndTitle = check.isSelected();
      
      // show it 
      showFile(file.toString(), true);
      
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
      setText(zoom==0?"1:1":zoom+"%");
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
      
      // remember
      zoom = zOOm;
      view.registry.put("file.zoom", zoom);
      
      // calc tooltip
      setToolTipText(zoom==0 ? "1:1" : zoom+"%");
      
      // show
      revalidate();
      repaint();
    }
    /**
     * Sets the image to preview
     */
    protected void setImage(ImageIcon set) {
      // remember
      img = set;
      // show
      setZoom(zoom);
    }
    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g) {
      // no image?
      if (img==null) return;
      // Maybe we'll paint in physical size
      UnitGraphics ug = new UnitGraphics(g, 1, 1);
      // not 1:1?
      if (zoom!=0) {
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
      }
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
      // 1:1?
      if (zoom==0)
        return new Dimension(img.getIconWidth(), img.getIconHeight());
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
      // zoom levels for images
      if (img!=null) {
        mh.createItem(new ActionZoom( 10));
        mh.createItem(new ActionZoom( 25));
        mh.createItem(new ActionZoom( 50));
        mh.createItem(new ActionZoom(100));
        mh.createItem(new ActionZoom(150));
        mh.createItem(new ActionZoom(200));
        mh.createItem(new ActionZoom(  0));
      }
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

  /**
   * Async Image Loader
   */
  private class Loader extends ActionDelegate {
    /** warn about filesize */
    private boolean warn;
    /** file to load */
    private String file;
    /** the result */
    private ImageIcon result;
    /**
     * constructor
     */
    private Loader(String setFile, boolean setWarn) {
      warn = setWarn;
      file = setFile;
      setAsync(super.ASYNC_SAME_INSTANCE);
    }
    /**
     * @see genj.util.ActionDelegate#preExecute()
     */
    protected boolean preExecute() {
      // kill current
      preview.setImage(null);
      // show wait
      preview.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      // continue
      return true;
    }

    /** 
     * async load
     */
    protected void execute() {
      // load it
      try {
        result = new ImageIcon(file, property.getGedcom().getOrigin().open(file));
      } catch (Throwable t) {
      }
      // continue
    }
    /**
     * sync show result
     */
    protected void postExecute() {

      // check 
      preview.setCursor(null);
      if (result==null)
        return;
        
      // warn about size
      if (warn&&result.getByteSize()>PropertyFile.getMaxValueAsIconSize(false)) {
        
        String txt = view.resources.getString("proxy.file.max", new String[]{
          result.getDescription(),
          String.valueOf(PropertyFile.toKB(result.getByteSize())),
          String.valueOf(PropertyFile.getMaxValueAsIconSize(true)),
          PropertyFile.MINUS_D_KEY
        }); 
        
        view.manager.getWindowManager().openDialog(
          null,null,
          WindowManager.IMG_INFORMATION,
          txt,
          WindowManager.OPTIONS_OK,
          view
        );
      }
      
      // show
      preview.setImage(result);
      result = null;
      
      // done
    }

  } //Loader
  
} //ProxyFile
