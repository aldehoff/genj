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
package genj.tree;

import java.awt.*;

import genj.util.*;
import genj.gedcom.*;

/**
 * A Graphics 'context' that wrappes Tree painting
 */
/*package*/ class TreeGraphics {

  Proxy[] indisProxies;
  Proxy[] famsProxies ;

  boolean isVertical  ,
          isShadow    ,
          isFolding   ,
          isFillHighlight,
          isPropertyImages,
          isZoomBlobs,
          isAbbreviateDates;

  int charHeight  ,
      charAscent  ,
      charDescent ,
      charWidth   ;

  private TreeModel          model;
  private Graphics           graphics;
  private Rectangle          clip;
  private double             zoom;
  private int                shadowWidth;
  private FontMetrics        fm;
  private Dimension          sizeOfIndis,sizeOfFams;
  private Entity             root;
  private int                distOfFamPlusMinus,distOfIndiPlusMinus;

  /**
   * Constructor
   */
  /*package*/ TreeGraphics(Graphics setGraphics, TreeModel setModel, float setZoom, boolean setShadow, boolean outlineHighlight, boolean setPropertyImages, boolean setZoomBlobs, boolean setAbbreviateDates) {

    // Remember
    model      = setModel;
    graphics   = setGraphics;
    root       = model.getRoot();

    // Prepare Parameters
    zoom             = setZoom;
    isVertical       = model.isVertical();
    isShadow         = setShadow;
    shadowWidth      = 6;
    isFolding        = true;
    isFillHighlight  = !outlineHighlight;
    isPropertyImages = setPropertyImages;
    isZoomBlobs      = setZoomBlobs;
    isAbbreviateDates= setAbbreviateDates;
    indisProxies     = model.getProxies(Gedcom.INDIVIDUALS);
    famsProxies      = model.getProxies(Gedcom.FAMILIES   );

    sizeOfIndis = model.getSize(Gedcom.INDIVIDUALS);
    sizeOfFams  = model.getSize(Gedcom.FAMILIES   );

    distOfFamPlusMinus  = model.getPlusMinusOffset(Gedcom.FAMILIES   );
    distOfIndiPlusMinus = model.getPlusMinusOffset(Gedcom.INDIVIDUALS);

    clip        = graphics.getClipBounds();
    if (clip==null) {
      clip = new Rectangle(0,0,Integer.MAX_VALUE,Integer.MAX_VALUE);
    }

    // Prepare Fontmetrics
    Font font      = model.getFont();
    graphics.setFont(font);
    fm             = graphics.getFontMetrics();
    charHeight     = fm.getHeight() ;
    charAscent     = fm.getAscent() ;
    charDescent    = fm.getDescent();
    charWidth      = fm.charWidth('O');

    // Prepare Zoomed Font
    font           = new Font(font.getName(),font.getStyle(),(int)(font.getSize()*zoom));
    graphics.setFont(font);

    // Done
  }

  /**
   * Helper function sets clipping of a graphics object according to tree layout
   */
  /*package*/ void clip(int dit, int pig, Rectangle rect) {

    dit         = (int)(dit        *zoom);
    pig         = (int)(pig        *zoom);
    rect.x      = (int)(rect.x     *zoom);
    rect.y      = (int)(rect.y     *zoom);
    rect.width  = (int)(rect.width *zoom);
    rect.height = (int)(rect.height*zoom);

    if (isVertical) {
      graphics.clipRect(
      pig+rect.x,
      dit+rect.y,
      rect.width,rect.height
      );
    } else {
      graphics.clipRect(
      dit+rect.x,
      pig+rect.y,
      rect.width,rect.height
      );
    }

  }

  /**
   * Helper function that draws an image according to tree layout
   * Naming an Image as being Blob allows for en/disabling zoom globally
   * given x and y dimension/position
   */
  void drawBlob(ImgIcon img,int dit,int pig, Point offset) {
    drawImage(img,dit,pig,offset,isZoomBlobs);
  }

  /**
   * Draws a highlighting rectangle
   */
  void drawHighlight(int dit, int pig, int w, int h, Color color, Color back) {

    setColor(color);
    setXORMode(back);

    if (isFillHighlight) {
      fillRect(dit, pig, w+1+1, h+1+1 );
    } else {
      drawRect(dit, pig, w , h );
      drawRect(dit, pig, w+2+2, h+2+2 );
    }

    setPaintMode();
  }

  /**
   * Draws a highlight for a link
   */
  void drawHighlight(Link link) {

    // Calc some parms
    Dimension s = link.getSize(model);

    int pig=link.getPosInGen()   ,
        dit=link.getDepthInTree();

    // Draw Highlight's box
    drawHighlight(dit, pig, s.width+2+2, s.height+2+2, Color.red, Color.white);

    // Cleanup
  }

  /**
   * Helper function that draws an image according to tree layout
   */
  void drawImage(ImgIcon img,int dit,int pig) {

    dit         = (int)(dit        *zoom);
    pig         = (int)(pig        *zoom);

    if (!isVertical)  {
      img.paintIcon(graphics ,dit, pig, zoom);
    } else {
      img.paintIcon(graphics ,pig, dit, zoom);
    }
  }

  /**
   * Helper function that draws an image according to tree layout
   * given x and y dimension/position
   */
  void drawImage(ImgIcon img,int dit,int pig, Point offset) {
    drawImage(img,dit,pig,offset,true);
  }

  /**
   * Helper function that draws an image according to tree layout
   * @param img Image to draw
   * @param dit Depth in tree
   * @param pig Position in generation
   * @param offset Offset to position in x-y-world
   * @param doZoom enable zooming
   */
  void drawImage(ImgIcon img,int dit,int pig, Point offset,boolean doZoom) {

    dit         = (int)(dit        *zoom);
    pig         = (int)(pig        *zoom);
    offset.x    = (int)(offset.x   *zoom);
    offset.y    = (int)(offset.y   *zoom);

    if (!isVertical)  {
      img.paintIcon(graphics ,dit + offset.x, pig + offset.y, doZoom ? zoom : 1.0F);
    } else {
      img.paintIcon(graphics ,pig + offset.x, dit + offset.y, doZoom ? zoom : 1.0F);
    }
  }

  /**
   * Helper function that draws a line according to tree layout
   */
  void drawLine(int dit1, int pig1, int dit2, int pig2) {

    dit1 = (int)(dit1*zoom);
    pig1 = (int)(pig1*zoom);
    dit2 = (int)(dit2*zoom);
    pig2 = (int)(pig2*zoom);

    if (!isVertical) {
      graphics.drawLine(dit1,pig1,dit2,pig2);
    } else {
      graphics.drawLine(pig1,dit1,pig2,dit2);
    }
  }

  /**
   * Helper function that draws an oval according to tree layout
   */
  void drawOval(int dit1, int pig1, Rectangle rect) {

    dit1        = (int)(dit1       *zoom);
    pig1        = (int)(pig1       *zoom);
    rect.x      = (int)(rect.x     *zoom);
    rect.y      = (int)(rect.y     *zoom);
    rect.width  = (int)(rect.width *zoom)-1;
    rect.height = (int)(rect.height*zoom)-1;
    if (!isVertical) {
      graphics.drawOval(dit1+rect.x,pig1+rect.y,rect.width,rect.height);
    } else {
      graphics.drawOval(pig1+rect.x,dit1+rect.y,rect.width,rect.height);
    }
  }

  /**
   * Helper function that draws a rectangle according to tree layout
   */
  void drawRect(int dit1, int pig1, int width, int height) {

    dit1  = (int)(dit1  *zoom);
    pig1  = (int)(pig1  *zoom);
    width = (int)(width *zoom);
    height= (int)(height*zoom);

    if (!isVertical) {
      graphics.drawRect(dit1-width/2,pig1-height/2,width,height);
    } else {
      graphics.drawRect(pig1-width/2,dit1-height/2,width,height);
    }
  }

  /**
   * Helper function that draws a rectangle according to tree layout with
   * given x and y dimension/position
   */
  void drawRect(int dit1, int pig1, Rectangle rect) {

    dit1        = (int)(dit1       *zoom);
    pig1        = (int)(pig1       *zoom);
    rect.x      = (int)(rect.x     *zoom);
    rect.y      = (int)(rect.y     *zoom);
    rect.width  = (int)(rect.width *zoom);
    rect.height = (int)(rect.height*zoom);
    if (!isVertical) {
      graphics.drawRect(dit1+rect.x,pig1+rect.y,rect.width,rect.height);
    } else {
      graphics.drawRect(pig1+rect.x,dit1+rect.y,rect.width,rect.height);
    }
  }

  /**
   * Helper function that draws a shadow according to tree layout
   */
  void drawShadow(int dit, int pig, int width, int height) {
    fillRect(dit+shadowWidth,pig+shadowWidth,width,height);
  }

  /**
   * Helper function that draws a string according to tree layout
   */
  void drawString(String text,int dit,int pig,int xoff,int yoff) {

    dit         = (int)(dit        *zoom);
    pig         = (int)(pig        *zoom);
    xoff        = (int)(xoff       *zoom);
    yoff        = (int)(yoff       *zoom);

    // Transform
    if (isVertical) {
      // ... & draw
      graphics.drawString(text,pig+xoff,dit+yoff);
    } else {
      // ... & draw
      graphics.drawString(text,dit+xoff,pig+yoff);
    }
    // Done
  }

  /**
   * Helper function that fills a rectangle according to tree layout
   */
  void fillRect(int dit, int pig, int width, int height) {

    dit         = (int)(dit        *zoom);
    pig         = (int)(pig        *zoom);
    width       = (int)(width      *zoom);
    height      = (int)(height     *zoom);

    if (!isVertical) {
      graphics.fillRect(dit-(width>>1),pig-(height>>1),width+1,height+1);
    } else {
      graphics.fillRect(pig-(width>>1),dit-(height>>1),width+1,height+1);
    }
  }

  /**
   * Returns the model behind this graphics
   */
  /*package*/ TreeModel getModel() {
    return model;
  }

  /**
   * Returns offset of plusminus regarding to entity
   */
  /*package*/ int getPlusMinusOffset(int of) {
    switch (of) {
    case Gedcom.INDIVIDUALS:
      return distOfIndiPlusMinus;
    case Gedcom.FAMILIES:
      return distOfFamPlusMinus;
    }
    throw new IllegalArgumentException();
  }

  /**
   * Returns dimension of entity
   */
  /*package*/ Dimension getSize(int which) {
    switch (which) {
    case Gedcom.INDIVIDUALS:
      return sizeOfIndis;
    case Gedcom.FAMILIES:
      return sizeOfFams;
    }
  throw new IllegalArgumentException();
  }

  /**
   * Calculates string width in this context
   */
  int getWidth(String of) {
    return fm.stringWidth(of);
  }

  /**
   * Returns wether given entity is root
   */
  boolean isRoot(Entity entity) {
    return (entity==root);
  }

  /**
   * Checks for visibility in invalidated region
   * @param dit1 depth in tree of edge 1
   * @param pig1 position in generation of edge 1
   * @param dit2 depth in tree of edge 2
   * @param pig2 position in generation of edge 2
   * @return is true when space between (dit1,pig1) and (dit2,pig2)
   * intersects invalidated region
   */
  boolean isVisible(int dit1, int pig1, int dit2, int pig2)  {

    int h   = dit2-dit1;
    int w   = pig2-pig1;

    int dit = dit1+(h>>1);
    int pig = pig1+(w>>1);

    if (!isVertical) {
      int t=w;w=h;h=t;
    }

    return isVisible(dit,pig,new Dimension(w,h));
  }

  /**
   * Checks for visibility in invalidated region
   * @param dit depth in tree of center
   * @param pig position in generation of center
   * @param size dimension of space in width and height
   * @return is true when space height/2 around (dit,pig)
   * intersects invalidated region
   */
  boolean isVisible(int dit, int pig, Dimension size)  {

    // Calc parms
    dit         = (int)( dit                       *zoom);
    pig         = (int)( pig                       *zoom);
    int width   = (int)((size.width +shadowWidth*2)*zoom);
    int height  = (int)((size.height+shadowWidth*2)*zoom);

    if (!isVertical) {
      int t=dit;dit=pig;pig=t;
    }

    //    |  clip   | x
    if (pig-(width >>1) > clip.x + clip.width ) {
      return false;
    }
    //  x |  clip   |
    if (pig+(width >>1) < clip.x              ) {
      return false;
    }
    //    |  clip   | y
    if (dit-(height>>1) > clip.y + clip.height) {
      return false;
    }
    //  y |  clip   |
    if (dit+(height>>1) < clip.y              ) {
      return false;
    }

    // Visible !
    return true;
  }

  /**
   * Helper function that restore clip bounds
   */
  void restoreClip() {
    graphics.setClip(clip.x,clip.y,clip.width,clip.height);
  }

  /**
   * Sets actual color to use
   */
  void setColor(Color color) {
    graphics.setColor(color);
  }

  /**
   * Sets paint-mode
   */
  void setPaintMode() {
    graphics.setPaintMode();
  }

  /**
   * Sets xor-mode
   */
  void setXORMode(Color color) {
    graphics.setXORMode(color);
  }
}
