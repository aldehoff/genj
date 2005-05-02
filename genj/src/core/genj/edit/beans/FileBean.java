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
package genj.edit.beans;

import genj.edit.actions.RunExternal;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyBlob;
import genj.gedcom.PropertyFile;
import genj.io.FileAssociation;
import genj.util.ActionDelegate;
import genj.util.Origin;
import genj.util.swing.FileChooserWidget;
import genj.util.swing.ImageIcon;
import genj.util.swing.MenuHelper;
import genj.util.swing.UnitGraphics;
import genj.util.swing.ViewPortAdapter;
import genj.window.CloseWindow;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FilePermission;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : FILE / BLOB
 */
public class FileBean extends PropertyBean {
  
  /** preview */
  private Preview preview;
  
  /** a checkbox as accessory */
  private JCheckBox updateFormatAndTitle = new JCheckBox(resources.getString("file.update"), false);
  
  /** file chooser  */
  private FileChooserWidget chooser = new FileChooserWidget();
  
  /** current loader*/
  private Loader loader = null;
  
  /**
   * Initialization
   */
  protected void initializeImpl() {

    setLayout(new BorderLayout());
    
    // setup chooser
    chooser.setAccessory(updateFormatAndTitle);
    chooser.addChangeListener(changeSupport);
    chooser.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        load(chooser.getFile().toString(), true);
      }
    });

    add(chooser, BorderLayout.NORTH);      
    
    // setup review
    preview = new Preview();
    add(new JScrollPane(new ViewPortAdapter(preview)), BorderLayout.CENTER);
    
    // setup a reasonable preferred size
    setPreferredSize(new Dimension(128,128));
    
    // done
  }
  
  /**
   * Set context to edit
   */
  protected void setContextImpl(Gedcom ged, Property prop) {

    // calc directory
    Origin origin = gedcom.getOrigin();
    String dir = origin.isFile() ? origin.getFile().getParent() : null;
    
    // check if showing file chooser makes sense
    if (dir!=null) try {
      
      SecurityManager sm = System.getSecurityManager();
      if (sm!=null) 
        sm.checkPermission( new FilePermission(dir, "read"));      

      chooser.setVisible(true);
      defaultFocus = chooser;

    } catch (SecurityException se) {
      chooser.setVisible(false);
      defaultFocus = null;
    }

    // case FILE
    if (property instanceof PropertyFile) {

      PropertyFile file = (PropertyFile)property;
      
      // show value
      chooser.setTemplate(false);
      chooser.setFile(file.getValue());

      load(file.getValue(), false);

      // done
    }

    // case BLOB
    if (property instanceof PropertyBlob) {

      PropertyBlob blob = (PropertyBlob)property;

      // show value
      chooser.setFile(blob.getValue());
      chooser.setTemplate(true);

      // .. preview
      try {
        preview.setImage(new ImageIcon(blob.getTitle(), blob.getBlobData()));
      } catch (Throwable t) {
      }

    }
      
    preview.setZoom(registry.get("file.zoom", 100));
    
    // Done
  }

  /**
   * Finish editing a property through proxy
   */
  public void commit() {
    
    // propagate
    String file = chooser.getFile().toString();
    
    if (property instanceof PropertyFile)
      ((PropertyFile)property).setValue(file, updateFormatAndTitle.isSelected());
    
    if (property instanceof PropertyBlob) 
      ((PropertyBlob)property).load(file, updateFormatAndTitle.isSelected());

    // update chooser
    chooser.setFile(property.getValue());

    // and preview if necessary
    ImageIcon img = preview.getImage();
    if (img==null||!img.getDescription().equals(file))
      load(file, false);

    // done
  }

  /**
   * intercept remove 
   */
  public void removeNotify() {
    // stop loading
    Loader l = loader;
    if (l!=null) l.cancel(true);
    // continue
    super.removeNotify();
  }
  
  /**
   * trigger a load - we currently only allow one concurrent loader
   * per RootPane
   */
  private void load(String file, boolean warnAboutSize) {
    
    // cancel current
    Loader l = loader;
    if (l!=null) l.cancel(true);

    // create new loader
    loader = new Loader(file, warnAboutSize);

    // start loading      
    loader.trigger();
  }
  
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
    }
    /**
     * Sets the zoom level
     */
    protected void setZoom(int zOOm) {
      
      // remember
      zoom = zOOm;
      registry.put("file.zoom", zoom);
      
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
     * Access current image
     */
    protected ImageIcon getImage() {
      return img;
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
          Point dpi = viewManager.getDPI();
          
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
      if (img==null) return new Dimension(32,32);
      // 1:1?
      if (zoom==0)
        return new Dimension(img.getIconWidth(), img.getIconHeight());
      // check physical size
      Dimension dim = img.getSizeInPoints(viewManager.getDPI());
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
      String file = chooser.getFile().toString();
      MenuHelper mh = new MenuHelper().setTarget(this);
      JPopupMenu popup = mh.createPopup();
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
      Iterator it = FileAssociation.getAll(suffix).iterator();
      while (it.hasNext()) {
        FileAssociation fa = (FileAssociation)it.next(); 
        mh.createItem(new RunExternal(gedcom,file,fa));
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
      setAsync(ActionDelegate.ASYNC_SAME_INSTANCE);
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
      return file.length()>0;
    }

    /** 
     * async load
     */
    protected void execute() {
      
      // load it
      try {
        Thread.sleep(200);
          result = new ImageIcon(file, gedcom.getOrigin().open(file));
      } catch (Throwable t) {
      }
      // continue
    }
    /**
     * sync show result
     */
    protected void postExecute() {
      
      loader = null;

      // check 
      preview.setCursor(null);
      if (result==null)
        return;
        
      // warn about size
      if (warn&&result.getByteSize()>PropertyFile.getMaxValueAsIconSize(false)) {
        
        String txt = resources.getString("file.max", new String[]{
          result.getDescription(),
          String.valueOf(result.getByteSize()/1024+1),
          String.valueOf(PropertyFile.getMaxValueAsIconSize(true)),
        }); 
        
        // open dlg
        viewManager.getWindowManager().openDialog(null,null,WindowManager.IMG_INFORMATION,txt,CloseWindow.OK(), FileBean.this);
      }
      
      // show
      preview.setImage(result);
      revalidate();
      
      result = null;
      
      // done
    }

  } //Loader

} //ProxyFile
