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
package genj.view.widgets;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.MetaProperty;
import genj.gedcom.PropertyComparator;
import genj.gedcom.PropertyDate;
import genj.gedcom.TagPath;
import genj.util.ActionDelegate;
import genj.util.swing.PopupWidget;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;

/**
 * A widget for choosing an entity amongst many
 */
public class SelectEntityWidget extends JPanel {

  /** type of entities to choose from */
  private String type = Gedcom.INDI;
  
  /** entities to choose from */
  private Object[] list;
  
  /** widgets */
  private PopupWidget sortWidget;
  private JComboBox listWidget;
  
  /** sorts */
  private Sort sort;
  
  private final static String[] SORTS = {
    "INDI",
    "INDI:NAME",
    "INDI:BIRT:DATE",
    "INDI:DEAT:DATE",
    "FAM",
    "FAM:MARR:DATE",
    "OBJE", 
    "NOTE", 
    "NOTE:NOTE", 
    "SOUR", 
    "SUBM", 
    "REPO"
  };
  
  /**
   * Constructor
   */
  public SelectEntityWidget(String type, Collection entities, String none) {

    // remember
    this.type = type;

    // init list
    list = new Object[entities.size()+1];
    list[0] = none;
    Iterator es=entities.iterator();
    for (int e=1;e<list.length;e++) {
      Entity ent = (Entity)es.next();
      if (!ent.getTag().equals(type))
        throw new IllegalArgumentException("Type of all entities has to be "+type);
      list[e] = ent;
    }

    // prepare sorting widget
    sortWidget = new PopupWidget();
    ArrayList sorts = new ArrayList();
    for (int i=0;i<SORTS.length;i++) {
      String path = SORTS[i];
      if (!path.startsWith(type))
        continue;
      Sort s = new Sort(path);
      sorts.add(s);
      if (sort==null) sort = s;
    }
    sortWidget.setActions(sorts);

    // prepare list widget    
    listWidget = new JComboBox();
    listWidget.setEditable(false);
    listWidget.setRenderer(new Renderer());

    // layout
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, listWidget);
    add(BorderLayout.WEST  , sortWidget);

    // init state
    sort.trigger();
    listWidget.setSelectedIndex(0);

    // done
  }
  
  /**
   * @see javax.swing.JComponent#getMaximumSize()
   */
  public Dimension getMaximumSize() {
    return new Dimension(super.getMaximumSize().width, super.getPreferredSize().height);
  }
  
  /**
   * The selected entity
   */
  public Entity getEntity() {
    Object item = listWidget.getSelectedItem();
    return item instanceof Entity ? (Entity)item : null;
  }
  
  /**
   * Add a listener
   */
  public void addActionListener(ActionListener listener) {
    listWidget.addActionListener(listener);
  }
  
  /**
   * Remove a listener
   */
  public void removeActionListener(ActionListener listener) {
    listWidget.removeActionListener(listener);
  }
  
  /**
   * Entity Rendering
   */
  private class Renderer extends DefaultListCellRenderer {
    /**
     * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

      // might be text of entity
      String txt;
      if (value instanceof Entity) {
        txt = getString((Entity)value, sort.tagPath);
      } else {
        txt = value.toString();
      }

      return super.getListCellRendererComponent(list, txt, index, isSelected, cellHasFocus);
    }

    /**
     * generate a string to show for entity&path
     */
    private String getString(Entity e, TagPath path) {
      
      // DATE?
      if (path.getLast().equals("DATE")) {
        PropertyDate pd = (PropertyDate)e.getProperty(path);
        return  pd!=null ? pd.getDisplayValue() + " - " + e.toString() : e.toString();
      }

      // default
      return e.toString();
    }

  } //Renderer
  
  /**
   * Sort action
   */
  private class Sort extends ActionDelegate {

    /** path */
    private TagPath tagPath;
    
    /**
     * Constructor
     */
    private Sort(String path) {
      
      // path
      tagPath = new TagPath(path);

      // image
      MetaProperty meta;
      if (tagPath.length()>1&&tagPath.getLast().equals(PropertyDate.TAG))
        meta = MetaProperty.get(new TagPath(tagPath, tagPath.length()-1));
      else
        meta = MetaProperty.get(tagPath);
      setImage(meta.getImage());
      
      // text
      String txt = tagPath.length()==1?"ID":meta.getName();
      setText("Sort by "+txt);
      // done
    }      
    
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      // remember
      sort = this;
      // Sort
      Comparator comparator = new PropertyComparator(tagPath);
      Arrays.sort(list, 1, list.length, comparator);
      listWidget.setModel(new DefaultComboBoxModel(list));
      sortWidget.setIcon(getImage());
    }
        
  } //Sort
   
} //PickEntityWidget
