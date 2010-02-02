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
import genj.gedcom.Property;
import genj.gedcom.PropertyBlob;
import genj.gedcom.PropertyFile;
import genj.util.Origin;
import genj.util.swing.Action2;
import genj.util.swing.FileChooserWidget;
import genj.util.swing.ImageWidget;
import genj.view.ViewContext;

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
  private JCheckBox updateMeta = new JCheckBox(RESOURCES.getString("file.update"), true);
  
  /** file chooser  */
  private FileChooserWidget chooser = new FileChooserWidget();
  
  private transient ActionListener doPreview = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      
      // remember directory
      REGISTRY.put("bean.file.dir", chooser.getDirectory());
      
      // show file
      File file = getProperty().getGedcom().getOrigin().getFile(chooser.getFile().toString());
      if (file==null) {
        preview.setSource(null);
        return;
      }
      preview.setSource(new ImageWidget.FileSource(file));
      
      // calculate relative
      String relative = getProperty().getGedcom().getOrigin().calcRelativeLocation(file.getAbsolutePath());
      if (relative!=null)
        chooser.setFile(relative);
      
      // done
    }
  };
  
  public FileBean() {
    
    setLayout(new BorderLayout());
    
    // setup chooser
    chooser.setAccessory(updateMeta);
    chooser.addChangeListener(changeSupport);
    chooser.addActionListener(doPreview);

    add(chooser, BorderLayout.NORTH);      
    
    // setup review
    add(preview, BorderLayout.CENTER);
    
    // setup a reasonable preferred size
    setPreferredSize(new Dimension(128,128));
    
    // setup drag'n'drop
    new DropTarget(this, new DropHandler());
    
    // done
    defaultFocus = chooser;
  }
  
  /**
   * Set context to edit
   */
  public void setPropertyImpl(Property property) {

    if (property==null)
      return;
    
    // calc directory
    Origin origin = property.getGedcom().getOrigin();
    String dir = origin.getFile()!=null ? origin.getFile().getParent() : null;
    
    // check if showing file chooser makes sense
    if (dir!=null) try {
      
      SecurityManager sm = System.getSecurityManager();
      if (sm!=null) 
        sm.checkPermission( new FilePermission(dir, "read"));      

      chooser.setDirectory(REGISTRY.get("bean.file.dir", dir));
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
      
    preview.setZoom(REGISTRY.get("file.zoom", 0)/100F);
    
    // Done
  }

  /**
   * Finish editing a property through proxy
   */
  protected void commitImpl(Property property) {
    
    // propagate
    String value = chooser.getFile().toString();
    
    if (property instanceof PropertyFile)
      ((PropertyFile)property).setValue(value, updateMeta.isSelected());
    
    if (property instanceof PropertyBlob) 
      ((PropertyBlob)property).load(value, updateMeta.isSelected());

    // update preview
    File file = getProperty().getGedcom().getOrigin().getFile(value);
    preview.setSource(file!=null?new ImageWidget.FileSource(file):null);
    
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
      
      PropertyFile file = (PropertyFile)getProperty();
      if (file!=null) 
        result.addAction(new RunExternal(file));
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
      setText(zoom==0?RESOURCES.getString("file.zoom.fit"):zoom+"%");
      setEnabled(zoom != (int)(preview.getZoom()*100));
    }
    /**
     * @see genj.util.swing.Action2#execute()
     */
    public void actionPerformed(ActionEvent event) {
      preview.setZoom(zoom/100F);
      REGISTRY.put("file.zoom", zoom);
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
    @SuppressWarnings("unchecked")
    public void drop(DropTargetDropEvent dtde) {
      try {
        dtde.acceptDrop(dtde.getDropAction());
        
        List<File> files = (List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
        chooser.setFile(files.get(0));
        preview.setSource(new ImageWidget.FileSource(files.get(0)));
        
        dtde.dropComplete(true);
        
      } catch (Throwable t) {
        dtde.dropComplete(false);
      }
    }
    
  }

} //FileBean
