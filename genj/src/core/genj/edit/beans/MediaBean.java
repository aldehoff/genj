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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

/**
 * A property bean for managing multimedia files (and blobs) associated with properties 
 */
public class MediaBean extends PropertyBean {
  
  private final static ImageIcon IMG_PREV = Images.imgBack, IMG_NEXT = Images.imgForward;
  
  private int thumbSize = 48;
  private List<PropertyFile> files = new ArrayList<PropertyFile>();
  private Canvas canvas = new Canvas();
  
  /**
   * Constructor
   */
  public MediaBean() {
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new ScrollPaneWidget(canvas));
    setPreferredSize(new Dimension(32,32));
  }

  @Override
  protected void commitImpl(Property property) {
  }

  @Override
  protected void setPropertyImpl(Property prop) {
    
    files.clear();
    
    // find all contained medias
    for (PropertyFile file : prop.getProperties(PropertyFile.class)) {
      if (file.getFile()!=null)
        files.add(file);
    }
    
    // find all referenced medias
    for (PropertyXRef ref : prop.getProperties(PropertyXRef.class)) {
      Entity entity = ref.getTargetEntity();
      if (entity instanceof Media) {
        PropertyFile file = ((Media)entity).getFile();
        if (file.getFile()!=null)
          files.add(file);
      }
    }
    
  }
  
  private class Canvas extends JComponent {
    
    @Override
    public Dimension getPreferredSize() {
      int cols,rows;
      if (files.isEmpty()) {
        cols = 0;
        rows = 0;
      } else {
        cols = (int)Math.ceil(Math.sqrt(files.size()));
        rows = (int)Math.ceil(files.size()/(float)cols);
      }
      return new Dimension(cols*thumbSize,rows*thumbSize);
    }
    
    @Override
    public void paint(Graphics g) {
      
      Dimension d = getSize();
      g.setColor(Color.WHITE);
      g.fillRect(0, 0, d.width, d.height);
      
      if (files.isEmpty())
        return;
      
      int cols = (int)Math.ceil(Math.sqrt(files.size()));
      int rows = (int)Math.ceil(files.size()/(float)cols);
      
      int x = (d.width - (cols*thumbSize))/2;
      int y = (d.height- (rows*thumbSize))/2;
      
      for (int r=0;r<rows;r++) {
        for (int c=0;c<cols;c++) {
          
          if (r*cols+c==files.size()) break;
          
          g.setColor(Color.RED);
          g.fillRect(x+c*thumbSize, y+r*thumbSize, thumbSize, thumbSize);
          g.setColor(Color.BLUE);
          g.drawRect(x+c*thumbSize, y+r*thumbSize, thumbSize, thumbSize);
          
        }
      }
      
    } //paint
    
  } //Canvas

}
