/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2005 Nils Meier <nils@meiers.net>
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
package genj.geo;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.util.ActionDelegate;
import genj.util.swing.ButtonHelper;
import genj.view.Context;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import swingx.tree.AbstractTreeModel;

/**
 * A list of locations and opertions on them
 */
/*package*/ class GeoList extends JPanel {
  
  private static final String
    TXT_LOCATION = GeoView.RESOURCES.getString("location"),
    TXT_CHANGE = GeoView.RESOURCES.getString("location.change"),
    TXT_LATLON = GeoView.RESOURCES.getString("location.latlon"),
    TXT_UNKNOWN = GeoView.RESOURCES.getString("location.unknown");
  
  /** model */
  private GeoModel model;

  /** wrapped tree */
  private JTree tree; 
  
  /** propagate selection changes */
  private boolean isPropagateSelectionChanges = true;
  
  /**
   * Constructor
   */
  public GeoList(GeoModel model) {
    
    // remember 
    this.model = model;

    // create some components
    tree = new JTree(new Model());
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        // notify about selection changes?
        if (!isPropagateSelectionChanges)
          return;
        // collect selection
        Set props = new HashSet();
        Set locs = new HashSet();
          
        TreePath[] paths = tree.getSelectionModel().getSelectionPaths();
        if (paths!=null) for (int i=0; i<paths.length; i++) {
          if (paths[i].getPathCount()==3)
            props.add(paths[i].getPathComponent(2));
          if (paths[i].getPathCount()>1)
            locs.add(paths[i].getPathComponent(1));
        }

        // notify
        EventListener[] ls = listenerList.getListeners(SelectionListener.class);
        for (int i=0; i<ls.length; i++)
          ((SelectionListener)ls[i]).listSelectionChanged(locs, props);
        
        // done
      }
    });

    Update update = new Update();
    tree.getSelectionModel().addTreeSelectionListener(update);
    tree.setCellRenderer(new Renderer());
    
    Box buttonPanel = new Box(BoxLayout.X_AXIS);
    ButtonHelper bh = new ButtonHelper().setContainer(buttonPanel);
    bh.create(new UnFold(true));
    bh.create(new UnFold(false));
    bh.create(update);
    
    // layout
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new JScrollPane(tree));
    add(BorderLayout.SOUTH, buttonPanel);
    
    setPreferredSize(new Dimension(160,64));
    
    // done
  }

  /**
   * Component lifecycle - we're needed
   */
  public void addNotify() {
    super.addNotify();
    // setup hooked-up model now
    tree.setModel(new Model(model));
  }
  
  /**
   * Component lifecycle - we're not needed anymore
   */
  public void removeNotify() {
    super.removeNotify();
    // setup empty model 
    tree.setModel(new Model());
  }
  
  /**
   * Selection access
   */
  public void setSelectedLocations(Collection locations) {
    TreePath[] paths = ((Model)tree.getModel()).getPathsToLocations(locations);
    if (paths.length==0)
      return;
    
    // scroll to visible patching height to show as much as possible 'beneath' first path
    Rectangle bounds = tree.getPathBounds(paths[0]);
    bounds.height = tree.getParent().getHeight();
    tree.scrollRectToVisible(bounds);

    // select now
    isPropagateSelectionChanges = false;
    tree.setSelectionPaths(paths);
    isPropagateSelectionChanges = true;
  }
  
  /**
   * Selection access
   */
  public void setSelectedContext(Context context) {
    // need a property to start with
    Property prop = context.getProperty();
    if (prop==null)
      prop = context.getEntity();
    if (prop==null)
      return;
    // try to find a path
    TreePath path = ((Model)tree.getModel()).getPathToProperty(prop);
    if (path==null)
      return;
    // set selection
    isPropagateSelectionChanges = false;
    tree.makeVisible(path);
    Rectangle bounds = tree.getPathBounds(path);
    bounds.width = 1;
    tree.scrollRectToVisible(bounds);
    tree.setSelectionPath(path);
    isPropagateSelectionChanges = true;

    // done
  }
  
  /**
   * Add a listener
   */
  public void addSelectionListener(SelectionListener l) {
    listenerList.add(SelectionListener.class, l);
  }

  /**
   * Remove a listener
   */
  public void removeTreeSelectionListener(TreeSelectionListener l) {
    listenerList.remove(SelectionListener.class, l);
  }
  
  /**
   * An action for (unf)folding
   */
  private class UnFold extends ActionDelegate {
    private boolean fold;
    private UnFold(boolean fold) {
      setText( fold ? "+" : "-");
      this.fold = fold;
    }
    protected void execute() {
      TreePath[] paths = ((Model)tree.getModel()).getPathsToLocations();
      for (int i=0;i<paths.length;i++) {
        if (!fold) tree.collapsePath(paths[i]); else tree.expandPath(paths[i]); 
      }
    }
  }
  
  /**
   * An action for updating a location
   */
  private class Update extends ActionDelegate implements TreeSelectionListener {
    private Update() {
      setText(TXT_CHANGE);
      setEnabled(false);
    }
    public void valueChanged(TreeSelectionEvent e) {
      //setEnabled(table.getSelectedRowCount()==1);
    }
    protected void execute() {
    }
  }
  
  /**
   * our smart renderer
   */
  private class Renderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
      StringBuffer sb  = new StringBuffer();
      if (value instanceof GeoLocation) {
        GeoLocation loc = (GeoLocation)value;
        sb.append(loc.getJurisdictionsAsString());
        sb.append(" (");
        if (loc.getMatches()==0) {
          sb.append(TXT_UNKNOWN);
        } else {
          sb.append(loc.getCoordinateAsString());
          if (loc.getMatches()>1)
            sb.append("?");
        }
        sb.append(")");
        setText(sb.toString());
        setIcon(null);
        return this;
      } 
      if (value instanceof Property) {
        Property prop = (Property)value;
        Property date = prop.getProperty("DATE", true);
        if (date!=null) {
          sb.append(date.toString());
          sb.append(" ");
        }
        sb.append(Gedcom.getName(prop.getTag()));
        sb.append(" ");
        sb.append(prop.getEntity().toString());
        setText(sb.toString());
        setIcon(prop.getImage(false));
      }
      // done
      return this;
    }
  }
  
  /**
   * our model shown in tree
   */
  private static class Model extends AbstractTreeModel  implements GeoModelListener {
    
    private GeoModel geo;
    private List locations = new ArrayList();
    
    private Model() {
    }
    private Model(GeoModel geo) {
      this.geo = geo;
    }
    
    private TreePath[] getPathsToLocations() {
      return getPathsToLocations(locations);
    }
    private TreePath[] getPathsToLocations(Collection locations) {
      TreePath[] result = new TreePath[locations.size()];
      Iterator it = locations.iterator();
      for (int i=0;i<result.length;i++) 
        result[i] = new TreePath(new Object[] { this, it.next() });
      return result;
    }
    
    private TreePath getPathToProperty(Property prop) {
      
      while (!(prop.getParent() instanceof Entity))
        prop = prop.getParent();
      
      for (int i=0;i<locations.size();i++) {
        GeoLocation loc = (GeoLocation)locations.get(i);
        if (loc.properties.contains(prop))
          return new TreePath(new Object[] { this, loc, prop} );
      }
      
      return null;
    }
    
    /** model lifecycle */
    public void addTreeModelListener(TreeModelListener l) {
      // start working?
      if (geo!=null&&getListeners(TreeModelListener.class).length==0) {
        geo.addGeoModelListener(this);
        locations.clear();
        locations.addAll(geo.getLocations());
        Collections.sort(locations);
      }
      // continue
      super.addTreeModelListener(l);
      // done
    }
    
    public void removeTreeModelListener(TreeModelListener l) {
      // continue
      super.removeTreeModelListener(l);
      // stop working?
      if (geo!=null&&getListeners(TreeModelListener.class).length==0) {
        geo.removeGeoModelListener(this);
        locations.clear();
      }
    }
    
    /** geo model event */
    public void locationAdded(GeoLocation location) {
      int pos = 0;
      for (ListIterator it = locations.listIterator(); it.hasNext(); pos++) {
        Comparable other = (Comparable)it.next();
        if (other.compareTo(location)>0) {
          it.previous();
          it.add(location);
          break;
        }
      }
      // add as last
      if (pos==locations.size())
        locations.add(location);
      // tell about it
      fireTreeNodesInserted(this, new TreePath(this), new int[] { pos }, new Object[] { location });
    }

    public void locationUpdated(GeoLocation location) {
      fireTreeStructureChanged(this, new TreePath(this).pathByAddingChild(location), null , null);
      //fireTreeNodesChanged(this, new TreePath(this), new int[]{ locations.indexOf(location)} , new Object[] { location });
    }

    public void locationRemoved(GeoLocation location) {
      int i = locations.indexOf(location);
      locations.remove(i);
      fireTreeNodesRemoved(this, new TreePath(this), new int[] { i }, new Object[] { location });
    }

    /** tree model */
    protected Object getParent(Object node) {
      throw new IllegalArgumentException();
    }
    public Object getRoot() {
      return this;
    }
    public int getChildCount(Object parent) {
      if (geo==null)
        return 0;
      return parent==this ? locations.size() : ((GeoLocation)parent).getNumProperties();
    }
    public boolean isLeaf(Object node) {
      return node instanceof Property;
    }
    public Object getChild(Object parent, int index) {
      return parent==this ? locations.get(index) :  ((GeoLocation)parent).getProperty(index);
    }
    public int getIndexOfChild(Object parent, Object child) {
      if (parent==this)
        return locations.indexOf(child);
      return ((GeoLocation)parent).getPropertyIndex((Property)child);
    }
    
  } //Model

  /**
   * Listeners Callback interface
   */
  public interface SelectionListener extends EventListener {
    public void listSelectionChanged(Set locations, Set properties);
  }
  
}
