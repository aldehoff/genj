/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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

import genj.edit.Images;
import genj.gedcom.Entity;
import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyXRef;
import genj.util.swing.ImageIcon;
import genj.util.swing.ScrollPaneWidget;
import genj.util.swing.ThumbnailWidget;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;

/**
 * A property bean for managing multimedia files (and blobs) associated with properties 
 */
public class MediaBean extends PropertyBean {
  
  private final static ImageIcon IMG_PREV = Images.imgBack, IMG_NEXT = Images.imgForward;
  private ThumbnailWidget thumbs = new ThumbnailWidget();
  
  /**
   * Constructor
   */
  public MediaBean() {
    setBorder(BorderFactory.createLoweredBevelBorder());
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new ScrollPaneWidget(thumbs));
    setPreferredSize(new Dimension(32,32));
  }

  @Override
  protected void commitImpl(Property property) {
  }

  @Override
  protected void setPropertyImpl(Property prop) {
    
    // find all contained medias
    List<File> files = new ArrayList<File>();
    
    for (PropertyFile file : prop.getProperties(PropertyFile.class)) {
      if (file.getFile()!=null)
        files.add(file.getFile());
    }
    
    // find all referenced medias
    for (PropertyXRef ref : prop.getProperties(PropertyXRef.class)) {
      Entity entity = ref.getTargetEntity();
      if (entity instanceof Media) {
        PropertyFile file = ((Media)entity).getFile();
        if (file.getFile()!=null)
          files.add(file.getFile());
      }
    }
    
    thumbs.setFiles(files);
    
  }
  
  
}
