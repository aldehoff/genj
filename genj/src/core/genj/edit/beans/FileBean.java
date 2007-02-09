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

import genj.gedcom.Property;
import genj.gedcom.PropertyBlob;
import genj.gedcom.PropertyFile;
import genj.util.Origin;
import genj.util.Registry;
import genj.util.swing.Action2;
import genj.util.swing.FileChooserWidget;
import genj.util.swing.ImageWidget;
import genj.view.ViewContext;
import genj.view.ViewManager;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilePermission;
import java.util.List;

import javax.swing.JCheckBox;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : FILE / BLOB
 */
public class FileBean extends PropertyBean {
  
  /** preview */
  private ImageWidget preview = new ImageWidget();
  
  /** a checkbox as accessory */
  private JCheckBox updateFormatAndTitle = new JCheckBox(resources.getString("file.update"), false);
  
  /** file chooser  */
  private FileChooserWidget chooser = new FileChooserWidget();
  
  void initialize(ViewManager setViewManager, Registry setRegistry) {
    super.initialize(setViewManager, setRegistry);
    
    setLayout(new BorderLayout());
    
    // setup chooser
    chooser.setAccessory(updateFormatAndTitle);
    chooser.addChangeListener(changeSupport);
    chooser.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        registry.put("bean.file.dir", chooser.getDirectory());
        File file = getProperty().getGedcom().getOrigin().getFile(chooser.getFile().toString());
        preview.setSource(new ImageWidget.FileSource(file));

        // warn about size
        if (file.exists()&&file.length()>PropertyFile.getMaxValueAsIconSize(false)) {
        
          String txt = resources.getString("file.max", new String[]{
            file.getName(),
            String.valueOf(file.length()/1024+1),
            String.valueOf(PropertyFile.getMaxValueAsIconSize(true)),
          }); 
        
          viewManager.getWindowManager().openDialog(null,null,WindowManager.INFORMATION_MESSAGE,txt,Action2.okOnly(), FileBean.this);
        }

        // done
      }
    });

    add(chooser, BorderLayout.NORTH);      
    
    // setup review
    add(preview, BorderLayout.CENTER);
    
    // setup a reasonable preferred size
    setPreferredSize(new Dimension(128,128));
    
    // setup drag'n'drop
    new DropTarget(this, new DropHandler());
    
    // done
  }
  
  /**
   * Set context to edit
   */
  public void setProperty(PropertyFile file) {
    set(file);
  }
  
  public void setProperty(PropertyBlob blob) {
    set(blob);
  }
  
  private void set(Property property) {

    // remember property
    this.property = property;
    
    // calc directory
    Origin origin = property.getGedcom().getOrigin();
    String dir = origin.getFile()!=null ? origin.getFile().getParent() : null;
    
    // check if showing file chooser makes sense
    if (dir!=null) try {
      
      SecurityManager sm = System.getSecurityManager();
      if (sm!=null) 
        sm.checkPermission( new FilePermission(dir, "read"));      

      chooser.setDirectory(registry.get("bean.file.dir", dir));
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

      if (property.getValue().length()>0)
        preview.setSource(new ImageWidget.RelativeSource(property.getGedcom().getOrigin(), property.getValue()));
      else
        preview.setSource(null);
      
      // done
    }

    // case BLOB
    if (property instanceof PropertyBlob) {

      PropertyBlob blob = (PropertyBlob)property;

      // show value
      chooser.setFile(blob.getValue());
      chooser.setTemplate(true);

      // .. preview
      preview.setSource(new ImageWidget.ByteArraySource( ((PropertyBlob)property).getBlobData() ));

    }
      
    new ActionZoom(registry.get("file.zoom", 100)).trigger();
    
    // Done
  }

  /**
   * Finish editing a property through proxy
   */
  public void commit(Property property) {
    
    super.commit(property);
    
    // propagate
    String file = chooser.getFile().toString();
    
    if (property instanceof PropertyFile)
      ((PropertyFile)property).setValue(file, updateFormatAndTitle.isSelected());
    
    if (property instanceof PropertyBlob) 
      ((PropertyBlob)property).load(file, updateFormatAndTitle.isSelected());

    // update chooser
    chooser.setFile(property.getValue());

    // done
  }

  /**
   * ContextProvider callback 
   */
  public ViewContext getContext() {
    ViewContext result = super.getContext();
    if (result!=null) {
      result.addAction(new ActionZoom( 10));
      result.addAction(new ActionZoom( 25));
      result.addAction(new ActionZoom( 50));
      result.addAction(new ActionZoom(100));
      result.addAction(new ActionZoom(150));
      result.addAction(new ActionZoom(200));
      result.addAction(new ActionZoom(  0));
    }
    // all done
    return result;
  }
  
  /**
   * Action - zoom
   */
  private class ActionZoom extends Action2 {
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
     * @see genj.util.swing.Action2#execute()
     */
    protected void execute() {
      preview.setZoom(zoom/100F);
      preview.setToolTipText(zoom==0 ? "1:1" : zoom+"%");
      registry.put("file.zoom", zoom);
    }
  } //ActionZoom

  /**
   * Our DnD support
   */
  private class DropHandler extends DropTargetAdapter {
    
    /** callback - dragged  */
    public void dragEnter(DropTargetDragEvent dtde) {
      if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        dtde.acceptDrag(dtde.getDropAction());
      else
        dtde.rejectDrag();
    }
     
    /** callback - dropped */
    public void drop(DropTargetDropEvent dtde) {
      try {
        dtde.acceptDrop(dtde.getDropAction());
        
        List files = (List)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
        File file = (File)files.get(0);
        chooser.setFile(file);
        
        preview.setSource(new ImageWidget.FileSource(file));
        
        dtde.dropComplete(true);
        
      } catch (Throwable t) {
        dtde.dropComplete(false);
      }
    }
    
  }

} //FileBean
