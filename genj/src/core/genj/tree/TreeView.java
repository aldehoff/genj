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
import java.beans.*;

import genj.*;
import genj.gedcom.*;
import genj.util.*;

import awtx.*;

/**
 * This class shows persons and families from a mankind object in a tree-view way
 */
public class TreeView extends Scrollpane {

  private Content                content;
  private Gedcom                 gedcom;
  private Frame                  frame;
  private Registry               registry;
  private Scala                  scala;
  private Combobox               combo;
  private Window                 overview;
  private PropertyChangeSupport  pcsupport;
  private Vector                 bookmarks = new Vector();
  private final static Resources resources = new Resources(TreeView.class);

  /**
   * Constructor
   */
  public TreeView(Gedcom gedcom, Registry registry, Frame frame) {

    // Remember some data
    this.pcsupport= new PropertyChangeSupport(this);
    this.gedcom   = gedcom;
    this.frame    = frame;
    this.registry = registry;
    this.content  = new Content(this,gedcom,registry);

    // Do the layout
    setQuadrant(CENTER,content);

    // Create a listener for controls
    ActionListener alistener = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        // OVERview?
        if (ae.getActionCommand().equals("OVER")) {
          toggleOverview();
          return;
        }
        // ZOOM
        if (ae.getActionCommand().equals("ZOOM")) {
          setZoom(scala.getValue());
          return;
        }
        // bookMARK
        if (ae.getActionCommand().equals("MARK")) {
          selectBookmark(combo.getSelectedIndex()-1);
          if (combo.getSelectedIndex()!=0) {
                combo.setSelectedIndex(0);
          }
          content.requestFocus();
          return;
        }
        // Done
      }
      // EOC
    };

    // Listening to Mouse
    MouseAdapter madapter = new MouseAdapter() {
      /**
       * User clicked a mouse button
       */
      public void mouseClicked(MouseEvent e) {
        content.requestFocus();

        // Try to find Link to entity over which mouse was clicked
        Link link=content.getLinkAt(e.getX(),e.getY());

        // Start action
        if (link!=null) {
          if ((e.getClickCount()>1)||(e.isShiftDown())) {
            Entity entity = link.getEntity();
            link.dclickIn(content.getModel());
            centerLink(content.getModel().getRootLink());
          } else {
            link.clickIn(content.getModel());
          }
        }
        // Done
      }
      // EOC
    };
    content.addMouseListener(madapter);

    // Key listening
    KeyListener klistener = new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        Point old = getScrollPosition();
        try {
        switch (e.getKeyCode()) {
          case KeyEvent.VK_UP :
            setScrollPosition(old.x,old.y-16);
            break;
          case KeyEvent.VK_DOWN :
            setScrollPosition(old.x,old.y+16);
            break;
          case KeyEvent.VK_LEFT :
            setScrollPosition(old.x-16,old.y);
            break;
          case KeyEvent.VK_RIGHT :
            setScrollPosition(old.x+16,old.y);
            break;
          case KeyEvent.VK_SUBTRACT :
            setZoom((float)Math.max(0.1,getZoom()-0.1));
            break;
          case KeyEvent.VK_ADD :
            setZoom((float)Math.min(1.0,getZoom()+0.1));
            break;
          case KeyEvent.VK_PAGE_UP:
            setScrollPosition(old.x,old.y-getContentSize().height);
            break;
          case KeyEvent.VK_PAGE_DOWN:
            setScrollPosition(old.x,old.y+getContentSize().height);
            break;
          }
        } catch (NullPointerException npe) {
        }
      }
      // EOC
    };
    content.addKeyListener(klistener);

    // Create a control for Bookmarks
    combo = ComponentProvider.createCombobox();
    combo.setActionCommand("MARK");
    combo.addActionListener(alistener);
    add2Edge(combo);

    // Create control for ZOOM
    scala = new Scala();
    scala.setActionCommand("ZOOM");
    scala.addActionListener(alistener);
    scala.setValue(content.getZoom());
    add2Edge(scala);

    // Create control for OVERVIEW
    add2Edge(ComponentProvider.createButton(
      Images.imgOverview.getImage(),
      resources.getString("corner.overview.text"),
      resources.getString("corner.overview.tip"),
      "OVER",
      alistener,
      ComponentProvider.IMAGE_ONLY,
      new Insets(0,0,0,0)
    ));

    // Registry : Bookmarks
    String[] bnames = registry.get("bookmark.name",new String[0]);
    String[] bids   = registry.get("bookmark.id"  ,new String[0]);
    if (bnames.length==bids.length) { try {
      for (int i=0;i<bnames.length;i++) {
        Entity e = gedcom.getEntityFromId(bids[i]);
        if (e==null) {
          continue;
        }
        bookmarks.addElement(new Bookmark(e,bnames[i]));
      }
    } catch (DuplicateIDException e) {} }

    // Open overview?
    if (registry.get("over.open",false)) {
      toggleOverview();
    }

    // Fill Bookmark-Combo
    fillBookmarkCombo();

    // Done
  }

  /**
   * Bound property support
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcsupport.addPropertyChangeListener(listener);
  }

  /**
   * Scrolls the view to show the given link centered
   */
  void centerLink(Link link) {

    // Valid link?
    if (link==null) {
      return;
    }

    // Scroll to x/y
    int dit = (int)(link.getDepthInTree() * getZoom() );
    int pig = (int)(link.getPosInGen()    * getZoom() );

    if (!getModel().isVertical()) {
      int t = dit;
      dit = pig;
      pig = t;
    }

    Dimension view = getContentSize();

    setScrollPosition(pig-view.width/2,dit-view.height/2);

    // Done
  }

  /**
   * Making sure that titlebar shows correct information in case
   * some content was being layouted
   */
  public void doLayout() {

    // Delegate
    super.doLayout();

    // Update title
    Bookmark b = getCurrentBookmark();
    if (b==null)
      setTitleExtension(null);
    else
      setTitleExtension(b.getName());

    // Done
  }

  /**
   * A helper that reads bookmarks from content for ComboBox
   * FIXME All viewing settings should be moved from Content to Tree
   */
  private void fillBookmarkCombo() {
    Object[] elements = new Object[bookmarks.size()+1];
    elements[0]="<"+resources.getString("corner.bookmarks.text")+">";
    Enumeration bs = bookmarks.elements();
    for (int i=1;bs.hasMoreElements();i++) {
      elements[i] = bs.nextElement();
    }
    combo.setElements(elements);
  }

  /**
   * Returns a list of bookmarks
   */
  public Vector getBookmarks() {
    return bookmarks;
  }

  /**
   * Returns a bookmark which describes current situation
   */
  public Bookmark getCurrentBookmark() {

    // Look in Bookmarks for root of tree
    Enumeration bs = bookmarks.elements();
    while (bs.hasMoreElements()) {
      Bookmark b = (Bookmark)bs.nextElement();
      if ( b.getEntity() == content.getModel().getRoot() )
            return b;
    }

    // No Bookmark for current situation
    return null;
  }

  /**
   * Returns this tree's Gedcom
   */
  public Gedcom getGedcom() {
    return gedcom;
  }

  /**
   * Returns the model under this tree
   */
  public TreeModel getModel() {
    return content.getModel();
  }

  /**
   * Returns this tree's proxies
   */
  public Proxy[] getProxies(int which) {
    return content.getModel().getProxies(which);
  }

  /**
   * Returns the size of entities
   */
  public Dimension getSize(int which) {
    return content.getModel().getSize(which);
  }

  /**
   * Sets this tree's TagPaths to show
   */
  public TagPath[] getTagPaths(int which) {
    return content.getModel().getTagPaths(which);
  }

  /**
   * Returns the current zoom
   */
  public float getZoom() {
    return content.getZoom();
  }

  /**
   * Requests PropertImage property
   */
  public boolean isPropertyImages() {
    return content.isPropertyImages();
  }

  /**
   * Requests shadow property
   */
  public boolean isShadow() {
    return content.isShadow();
  }

  /**
   * Requests Vertical property
   */
  public boolean isVertical() {
    return content.isVertical();
  }

  /**
   * Requests PropertImage property
   */
  public boolean isZoomBlobs() {
    return content.isZoomBlobs();
  }

  /**
   * Requests AbbreviateDates property
   */
  public boolean isAbbreviateDates() {
    return content.isAbbreviateDates();
  }

  /**
   * Notification that component is not used anymore
   */
  public void removeNotify() {

    // SAVE content
    content.saveToRegistry();

    // SAVE Bookmarks
    String[] bnames = new String[bookmarks.size()];
    String[] bids   = new String[bookmarks.size()];

    Enumeration bs = bookmarks.elements();
    for (int i=0;bs.hasMoreElements();i++) {
      Bookmark b = (Bookmark)bs.nextElement();
      bnames[i] = b.getName();
      bids  [i] = b.getEntity().getId();
    }
    registry.put("bookmark.name",bnames);
    registry.put("bookmark.id"  ,bids  );

    // Done
    super.removeNotify();
  }

  /**
   * Bound property support
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcsupport.removePropertyChangeListener(listener);
  }

  /**
   * Selects a bookmark for viewing
   */
  private void selectBookmark(int which) {

    TreeModel model = content.getModel();

    // Selection o.k.?
    if ((which<0)||(which>=bookmarks.size())) {
      return;
    }
    Bookmark bookmark = (Bookmark)bookmarks.elementAt(which);

    // Valid entity?
    Entity entity = bookmark.getEntity();
    if ((entity==null)||(entity.getGedcom()==null)) {
      bookmarks.removeElement(bookmark);
      fillBookmarkCombo();
      return;
    }

    // Do it
    content.getModel().setRoot(entity);
    centerLink(getModel().getLink(entity));

    // Done
  }

  /**
   * Sets list of bookmarks
   */
  public void setBookmarks(Vector marks) {
    bookmarks = marks;
    fillBookmarkCombo();
  }

  /**
   * Overriding bound changes to hook request of focus
   */
  public void setBounds(int x, int y, int w, int h) {
    Dimension dim = getSize();
    super.setBounds(x,y,w,h);
    content.requestFocus();
    pcsupport.firePropertyChange("size",getSize(),dim);
  }

  /**
   * Set PropertyImages property
   */
  public void setPropertyImages(boolean set) {
    content.setPropertyImages(set);
  }

  /**
   * Set shadow property
   */
  public void setShadow(boolean set) {
    content.setShadow(set);
  }

  /**
   * Helper that appends text to enclosing frame's title
   */
  private void setTitleExtension(String ext) {

    if (frame==null) {
      return;
    }

    // Calculate old title
    String title = frame.getTitle();
    int i = title.indexOf(" /");
    if (i>0) {
      title = title.substring(0,i);
    }

    // Set title
    if (ext!=null) {
      frame.setTitle(title+" / "+ext);
    } else {
      frame.setTitle(title);
    }

    // Done
  }

  /**
   * Sets this tree's direction property
   */
  public void setVertical(boolean set) {
    content.setVertical(set);
  }

  /**
   * Sets this tree's abbreviate-dates property
   */
  public void setAbbreviateDates(boolean set) {
    content.setAbbreviateDates(set);
  }

  /**
   * Changes the current zoom
   */
  public void setZoom(float value) {
    Float oldz = new Float(getZoom());
    Float newz = new Float(value);
    content.setZoom(value);
    pcsupport.firePropertyChange("zoom",oldz,newz);
    if (scala.getValue()!=value) {
      scala.setValue(value);
    }
  }

  /**
   * Set PropertyImages property
   */
  public void setZoomBlobs(boolean set) {
    content.setZoomBlobs(set);
  }

  /**
   * Toggle Overview Dialog on/off
   */
  private void toggleOverview() {

    // Dialog already showing ?
    if (overview!=null) {
      registry.put("over.open",false);
      overview.dispose();
      return;
    }
    registry.put("over.open",true);

    // Create new Dialog for Overview
    Runnable runClosed = new Runnable() {
      public void run() {
        // Safety check
        if (overview==null) {
          return;
        }

        // Look for overview frame position
        Rectangle r = overview.getBounds();
        /* see below 
        if (frame!=null) {
          Point p = frame.getLocation();
          r.x = r.x-p.x;
          r.y = r.y-p.y;
        }
        */
        
        // Remember settings
        registry.put("over.bounds",r);

        if (frame.isVisible()) {
          registry.put("over.open",false);
        }
        // Clear reference
        overview=null;
      }
      // EOC
    };

    overview = ComponentProvider.createDialog(
      frame,
      resources.getString("corner.overview.text"),
      new Rootpane(new Overview(this)),
      runClosed
    );

    // Setup Bound of new window
    Rectangle r = registry.get("over.bounds", (Rectangle)null);
    if (r!=null) {
      
      // .. relative to a frame?
      /* 2002 08 09 remove it because the frame is not be positioned yet
      if (frame!=null) {
        Point p = frame.getLocation();
        r.x = p.x+r.x;
        r.y = p.y+r.y;
      }
      */
      
      // .. set
      overview.setBounds(new AreaInScreen(r));
      
    } else {
      // .. default
      overview.setSize(new Dimension(160,160));
    }

    // Show it
    overview.show();

    // Done
  }

}
