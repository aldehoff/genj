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

import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.util.ActionDelegate;
import genj.util.swing.ButtonHelper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.ListSelectionListener;
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

    Update update = new Update();
    tree.getSelectionModel().addTreeSelectionListener(update);
    tree.setCellRenderer(new Renderer());
    
    // layout
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new JScrollPane(tree));
    add(BorderLayout.SOUTH, new ButtonHelper().create(update));
    
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
  public List getSelectedLocations() {
    
    List list = new ArrayList();
// FIXME    
//    int[] rows = table.getSelectedRows();
//    for (int i=0;i<rows.length;i++)
//      list.add(table.getValueAt(rows[i], 0));

    return list;
  }
  
  /**
   * Selection access
   */
  public void setSelectedLocations(Set set) {

// FIXME    
//    ListSelectionModel selection = table.getSelectionModel();
//    
//    // collect selected rows
//    selection.setValueIsAdjusting(true);
//    selection.clearSelection();
//    try {
//      TableModel tmodel = (TableModel)table.getModel();
//      for (int i=0, j=tmodel.getRowCount(); i<j; i++) {
//        GeoLocation l = tmodel.getLocationAt(i);
//        if (set.contains(l)) 
//          selection.addSelectionInterval(i, i);
//      }
//    } finally {
//      selection.setValueIsAdjusting(false);
//    }
    
    // done
  }
  
  /**
   * Add a listener
   */
  public void addListSelectionListener(ListSelectionListener l) {
// FIXME    
//    table.getSelectionModel().addListSelectionListener(l);
  }

  /**
   * Remove a listener
   */
  public void removeListSelectionListener(ListSelectionListener l) {
//  FIXME    
///    table.getSelectionModel().removeListSelectionListener(l);
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
  private class Model extends AbstractTreeModel  implements GeoModelListener {
    
    private GeoModel geo;
    private List locations = new ArrayList();
    
    private Model() {
    }
    private Model(GeoModel geo) {
      this.geo = geo;
    }
    
    /** model lifecycle */
    public void addTreeModelListener(TreeModelListener l) {
      // continue
      super.addTreeModelListener(l);
      // start working?
      if (model!=null&&getListeners(TreeModelListener.class).length==1) {
        model.addGeoModelListener(this);
        locations.clear();
        locations.addAll(model.getLocations());
        Collections.sort(locations);
      }
      // done
    }
    
    public void removeTreeModelListener(TreeModelListener l) {
      // continue
      super.removeTreeModelListener(l);
      // stop working?
      if (model!=null&&getListeners(TreeModelListener.class).length==0) {
        model.removeGeoModelListener(this);
        locations.clear();
      }
    }
    
    /** geo model event */
    public void locationAdded(GeoLocation location) {
      fireTreeNodesInserted(this, new TreePath(this), null, new Object[] { location });
      locations.add(location);
      Collections.sort(locations);
    }

    public void locationUpdated(GeoLocation location) {
      fireTreeStructureChanged(this, new TreePath(this), new int[]{ locations.indexOf(location)} , new Object[] { location });
    }

    public void locationRemoved(GeoLocation location) {
      locations.remove(location);
      fireTreeNodesRemoved(this, new TreePath(this), null, new Object[] { location });
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

}
