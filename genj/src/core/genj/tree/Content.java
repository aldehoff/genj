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
import java.awt.event.*;
import java.util.*;
import java.net.URL;
import java.awt.image.*;

import genj.gedcom.*;
import genj.util.*;

/**
 * The content pane displaying a Tree
 */
public class Content extends Component implements TreeModelListener {

  /**
   * Tree Model and Co.
   */
  private TreeView  tree;
  private TreeModel treeModel;
  private Point     treeCenter;
  private float     treeZoom;

  /**
   * Tree Looks
   */
  private boolean   isShadow;
  private int       shadowWidth=6;
  private boolean   isPropertyImages;
  private boolean   isZoomBlobs;
  private boolean   isAbbreviateDates;

  /**
   * Misc
   */
  private Registry  registry;

  /**
   * Our implementation of getGraphics makes sure that
   * no parent clipping is destroyed via setClip as in Component.getGraphics();
   */
  public Graphics getGraphics() {

    // Resolve Graphics by using the one from parent
    Graphics  g = getParent().getGraphics();

    // Change to our parameters
    Rectangle r = getBounds();
    g.translate(r.x, r.y);
    g.clipRect(0, 0, r.width, r.height);
    g.setFont(getFont());

    // Done
    return g;
  }

  /**
   * Calculates Link under point
   * @param x horizontal position in component
   * @param y vertical position in component
   */
  /*package*/ Link getLinkAt(int x,int y) {

    // Calculate position in original tree without zoom
    x = (int)(x/treeZoom);
    y = (int)(y/treeZoom);

    // Calculate PIG and DIT
    final int pig,dit;
    if (isVertical()) {
      pig=x;
      dit=y;
    } else {
      pig=y;
      dit=x;
    }

    // Iterate and Search
    java.util.Enumeration links = treeModel.getLinks();
    while (links.hasMoreElements()) {

      Link link = (Link)links.nextElement();

      // Investigate link
      Dimension dim = link.getSize(treeModel);
      int       w   = dim.width ;
      int       h   = dim.height;

      if (isHorizontal()) {
        int t=w;w=h;h=t;
      }
      w>>=1;
      h>>=1;

      // ... check this link
      if (link.getEntity()!=null) {
        if ( java.lang.Math.abs( link.getPosInGen()    - pig) < w )
        if ( java.lang.Math.abs( link.getDepthInTree() - dit) < h ) {
        return link;
        }
      }
    }

    return null;
  }

  /**
   * Returns the model of this tree
   */
  public TreeModel getModel() {
    return treeModel;
  }

  /**
   * Returns the preferred size (minimum here)
   */
  public Dimension getPreferredSize() {
    Dimension size = treeModel.getSize();
    return new Dimension(
      (int)(size.width*treeZoom ),
      (int)(size.height*treeZoom)
    );
  }

  /**
   * Returns the width of drawn shadows
   */
  public int getShadowWidth() {
    return shadowWidth;
  }

  /**
   * Returns this tree's zoom rate
   */
  public float getZoom() {
    return treeZoom;
  }

  /**
   * Notification in case actual link has changed
   */
  public void handleActualChanged(final Link oldActual, final Link newActual) {

    // Highlight has to be cleared/set
    final TreeGraphics tg = new TreeGraphics(
      getGraphics(),
      treeModel,
      treeZoom,
      isShadow,
      true,
      isPropertyImages,
      isZoomBlobs,
      isAbbreviateDates
    );

    java.util.Enumeration links = treeModel.getLinks();
    while (links.hasMoreElements()) {
      Link link = (Link)links.nextElement();
      // Paint link
      if ((link==oldActual)||(link==newActual))
        tg.drawHighlight(link);
    }

    // Done
  }

  /**
   * Notification in case data and not structure of model has changed
   */
  public void handleDataChanged() {
    repaint();
  }

  /**
   * Notification in case entities have changed without change in layout
   * A listener can use iterate to do actions on links to that entities.
   */
  public void handleEntitiesChanged(Vector changed) {
    repaint();
  }

  /**
   * Notification in case layout of model has changed
   */
  public void handleStructureChanged() {

    // Start with new center
    treeCenter = null;

    // Signal
    invalidate();
    
    // 20020311 - well apparently without a repaint
    // it can happen that the canvas doesn't refresh
    // because bounds stay the same (?)
    repaint();

    // Done
  }

  /**
   * Returns wether this tree shows itself horizontally
   */
  public boolean isHorizontal() {
    return !treeModel.isVertical();
  }

  /**
   * Test for state of var PropertyImages
   */
  public boolean isPropertyImages() {
    return isPropertyImages;
  }

  /**
   * Returns wether this tree draws shadows
   */
  public boolean isShadow() {
    return isShadow;
  }

  /**
   * Returns whether this tree abbreviates dates
   */
  public boolean isAbbreviateDates() {
    return isAbbreviateDates;
  }

  /**
   * Returns wether this tree shows itself vertically
   */
  public boolean isVertical() {
    return treeModel.isVertical();
  }

  /**
   * Test for state of var PropertyImages
   */
  public boolean isZoomBlobs() {
    return isZoomBlobs;
  }

  /**
   * Load Proxies from registry
   */
  private Proxy[] loadProxies(String name) {

    // Get registry values
    String[]    paths=registry.get(name+".path",(String[]   )null);
    Rectangle[] boxes=registry.get(name+".box" ,(Rectangle[])null);

    // Invalid ?
    if ((paths==null)||(boxes==null)) {
      return null;
    }
    if ((paths.length==0)||(paths.length!=boxes.length)) {
      return null;
    }

    // Remember
    return treeModel.generateProxies(paths,boxes);
  }

  /**
   * Starts tree drawing
   */
  public void paint(Graphics g) {

    // Gather some parms
    Dimension treeSize = treeModel.getSize();
    Dimension compSize = getSize();

    // Clear initially
    g.setColor(Color.white);
    g.fillRect(0,0,compSize.width,compSize.height);

    // Create graphics context
    final TreeGraphics tg = new TreeGraphics(
      g,
      treeModel,
      treeZoom,
      isShadow,
      true,
      isPropertyImages,
      isZoomBlobs,
      isAbbreviateDates
    );

    java.util.Enumeration links = treeModel.getLinks();
    while (links.hasMoreElements()) {
      ((Link)links.nextElement()).paint(tg,false,true);
    }

    Link actual = treeModel.getActualLink();
    if (actual!=null) {
      tg.drawHighlight(actual);
    }

    // Done
  }

  /**
   * Helper that saves Proxies to registry
   */
  private void saveProxies(String name, Proxy[] proxies) {

    // Go through proxies
    String[]    paths = new String   [proxies.length];
    Rectangle[] rects = new Rectangle[proxies.length];

    for (int i=0;i<proxies.length;i++) {
      paths[i] = proxies[i].getPath().toString();
      rects[i] = proxies[i].getBox ();
    }

    registry.put(name+".path",paths);
    registry.put(name+".box",rects);

    // Done
  }

  /**
   * The "destructor" called from the using Tree
   */
  void saveToRegistry() {

    // StopListening
    treeModel.removeTreeModelListener(this);

    // Registry : Model
    saveProxies("indis",treeModel.getProxies(Gedcom.INDIVIDUALS));
    saveProxies("fams" ,treeModel.getProxies(Gedcom.FAMILIES   ));

    registry.put("vertical",treeModel.isVertical());
    registry.put("indis"   ,treeModel.getSize(Gedcom.INDIVIDUALS));
    registry.put("fams"    ,treeModel.getSize(Gedcom.FAMILIES)   );

    if (treeModel.getRoot()==null) {
      registry.put("origin","");
    } else {
      registry.put("origin",treeModel.getRoot().getId());
    }

    Vector stoppers = treeModel.getStoppers();
    String stops[] = new String[stoppers.size()];
    Enumeration e = stoppers.elements();
    for (int i=0;e.hasMoreElements();i++) {
      stops[i] = ((Entity)e.nextElement()).getId();
    }
    registry.put("stoppers",stops);

    registry.put("font"    ,treeModel.getFont()   );

    // Registry : View
    registry.put("shadow"  ,isShadow         );
    registry.put("zoom"    ,treeZoom         );
    registry.put("pimages" ,isPropertyImages );
    registry.put("zblobs"  ,isZoomBlobs      );
    registry.put("adates"  ,isAbbreviateDates);

    if (treeCenter!=null) {
      registry.put("center"  ,treeCenter );
    }
  }

  /**
   * En/Disable PropertyImages for proxy rendering
   */
  public void setPropertyImages(boolean set) {
    isPropertyImages=set;
    repaint();
  }

  /**
   * Sets drawing of shadows
   */
  public void setShadow(boolean set) {
    isShadow = set;
    repaint();
  }

  /**
   * Sets the abbreviation of dates
   */
  public void setAbbreviateDates(boolean set) {
    isAbbreviateDates = set;
    repaint();
  }

  /**
   * Sets this tree's direction property
   * @param set true for vertical, false for horizontal
   */
  void setVertical(boolean set) {

    // Real change ?
    if (treeModel.isVertical()==set) {
      return;
    }

    // Remember
    treeModel.setVertical(set);

    // Done
  }

  /**
   * Set this tree's zoom rate
   * @param zoom rate as float between 0.1 and 1
   */
  void setZoom(float zoom) {

    // Valid and not equal ?
    if ((zoom<=0)||(zoom>1)||(zoom==treeZoom)) {
      return;
    }

    // Remember
    treeZoom = zoom;

    // We changed
    handleStructureChanged();
    
    // Done
  }

  /**
   * En/Disable PropertyImages for proxy rendering
   */
  public void setZoomBlobs(boolean set) {
    isZoomBlobs=set;
    repaint();
  }

  /**
   * Constructor
   * @param gedcom Gedcom to display tree of
   * @param registry Registry to use
   */
  public Content(TreeView tree, Gedcom gedcom, Registry registry) {

    this.registry = registry;
    this.tree     = tree;

    setBackground(Color.white);

    // Registry : Model
    boolean isVertical   = registry.get("vertical",true);

    Proxy[] indisProxies = loadProxies("indis");
    Proxy[] famsProxies  = loadProxies("fams");

    Dimension sizeOfIndis= registry.get("indis"   ,(Dimension)null);
    Dimension sizeOfFams = registry.get("fams"    ,(Dimension)null );

    String rootId = registry.get("origin",(String)null);

    String stoppersId[] = registry.get("stoppers",(String[])null);

    Font font = registry.get("font",new Font("SansSerif",Font.PLAIN,12));

    treeModel  = new TreeModel(gedcom,rootId,stoppersId,indisProxies,famsProxies,sizeOfIndis,sizeOfFams,isVertical,font);

    // Registry : View
    isShadow         = registry.get("shadow" ,false);
    isPropertyImages = registry.get("pimages",false);
    isZoomBlobs      = registry.get("zblobs" ,false);
    isAbbreviateDates= registry.get("adates" ,false);
    treeZoom         = Math.min(1,Math.max(0,registry.get("zoom",1.0F)));
    treeCenter       = registry.get("center",(Point)null);

    treeModel.addTreeModelListener(this);

    // Done
  }
}
