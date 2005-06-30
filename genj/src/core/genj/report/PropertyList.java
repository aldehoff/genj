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
 * 
 * $Revision: 1.3 $ $Author: nmeier $ $Date: 2005-06-30 13:52:52 $
 */
package genj.report;

import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.util.swing.HeadlessLabel;
import genj.view.Context;
import genj.view.ContextProvider;
import genj.view.ViewManager;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A list of properties that can be filled programmatically and shown to the user
 */
public class PropertyList {

  private Gedcom gedcom;
  private List entries = new ArrayList();
  
  /**
   * Constructor
   */
  public PropertyList(Gedcom gedcom) {
    this.gedcom = gedcom;
  }
  
  /**
   * Count
   */
  public int size() {
    return entries.size();
  }
  
  /**
   * Sort by natural order
   */
  public void sort() {
    Collections.sort(entries);
  }
  
  /**
   * Add a reference
   */
  public void add(String name, Property property) {
    entries.add(new Entry(name, property.getImage(false), property));
  }

  /**
   * Add a reference
   */
  public void add(String name, ImageIcon img, Property property) {
    entries.add(new Entry(name, img, property));
  }

  /**
   * one entry
   */
  /*package*/ class Entry implements Comparable {

    /** attrs */
    private String name;
    private ImageIcon img;
    private Property target;
    
    /**
     * Constructor
     * @param description text description of item
     * @param image an image to show with item
     * @param target reference to a gedcom property instance
     */
    public Entry(String description, ImageIcon image, Property target) {
      this.name = description;
      this.img = image;
      this.target = target;
    }
    
    /**
     * name
     */
    private String getName() {
      return name;
    }
    
    /**
     * img
     */
    private ImageIcon getImage() {
      return img;
    }
    
    /**
     * The target of this item
     */
    private Property getTarget() {
      return target==null ? null : target.getGedcom()==null ? null : target;
    }
    
    /**
     * Text comparison provided by super-class
     */
    public int compareTo(Object o) {
      Entry that = (Entry)o;
      return this.name.compareTo(that.name);
    }
    
  }  //Entry
  
  
  /**
   * A UI representation of the list
   */
  /*package*/ class UI extends JList implements ListCellRenderer, ListSelectionListener, ContextProvider {

    /** the view manager */
    private ViewManager manager;

    /** a headless label for rendering */
    private HeadlessLabel label = new HeadlessLabel();

    /** gedcom */
    private Gedcom gedcom;

    /**
     * Constructor
     */
    /*package*/ UI(ViewManager maNager) {
      super(entries.toArray());
      
      // remember
      manager = maNager;
      
      // setup looks
      setCellRenderer(this);
      label.setOpaque(true);
      addListSelectionListener(this);
      // done
    }
    
    /**
     * ContextProvider - callback
     */
    public Context getContext() {
      Entry entry = (Entry)getSelectedValue();
      return entry==null ? new Context(gedcom) : new Context( entry.getTarget() );
    }

    /**
     * Selection changed
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {
      // check selected item
      Entry entry = (Entry)getSelectedValue();
      if (entry!=null)
        manager.fireContextSelected(new Context(gedcom, null, entry.getTarget()));
    }

    /**
     * Our own rendering
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      // colors
      label.setBackground(isSelected ? getSelectionBackground() : getBackground());
      label.setForeground(isSelected ? getSelectionForeground() : getForeground());
      // display item
      Entry item = (Entry)value;
      label.setText(item.getName());
      label.setIcon(item.getImage());
      // done
      return label;
    }

  } //PropertyList
  
} //Bookmarks
