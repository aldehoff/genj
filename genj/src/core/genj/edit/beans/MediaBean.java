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
import genj.gedcom.Gedcom;
import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyBlob;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.io.InputSource;
import genj.util.swing.ImageIcon;
import genj.util.swing.ScrollPaneWidget;
import genj.util.swing.ThumbnailWidget;

import java.awt.BorderLayout;
import java.awt.Dimension;
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

    // done
  }
  
  @Override
  protected void commitImpl(Property property) {
  }

  @Override
  protected void setPropertyImpl(Property prop) {
    // clear?
    if (prop==null) {
      thumbs.clear();
    } else {
      thumbs.setSources(scan(prop));
    }
  }
  
  private List<InputSource> scan(Property property) {
    
    List<InputSource> files = new ArrayList<InputSource>();
    
    // find all contained medias
    for (PropertyFile file : property.getProperties(PropertyFile.class)) {
      if (file.getFile()!=null) {
        files.add(InputSource.get(getTag(file)+file.getFile().getName(), file.getFile()));
      }
    }
    
    // find all referenced medias
    for (PropertyXRef ref : property.getProperties(PropertyXRef.class)) {
      Entity entity = ref.getTargetEntity();
      if (entity instanceof Media) {
        Media media = (Media)entity;
        PropertyFile file = media.getFile();
        if (file!=null&&file.getFile()!=null)
          files.add(InputSource.get(getTag(ref)+file.getFile().getName(), file.getFile()));
        PropertyBlob blob = media.getBlob();
        if (blob!=null)
          files.add(InputSource.get(getTag(ref)+media.getTitle(), blob.getBlobData()));
      }
    }

    return files;
  }
  
  private String getTag(Property prop) {
    TagPath path = prop.getPath();
    if (path.length()<4)
      return "";
    for (int i=0;i<path.length()-2;i++)
      prop = prop.getParent();
    return prop.getPropertyName()+prop.format("{ $y}")+"\n";
  }
  
}
