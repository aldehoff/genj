/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2006 Nils Meier <nils@meiers.net>
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
package genj.common;

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.view.ContextProvider;
import genj.view.SelectionSink;
import genj.view.ViewContext;
import genj.view.ViewContext.ContextList;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import spin.Spin;

/**
 * A widget for rendering a list of contexts
 */
public class ContextListWidget extends JPanel implements ContextProvider {

  private Gedcom gedcom;
  private Callback callback = new Callback();
  private JList list;
  private JLabel title = new JLabel();
  private List<ViewContext> contexts = new ArrayList<ViewContext>();
  
  /** 
   * Constructor
   */
  public ContextListWidget(ContextList list) {
    this(list.getGedcom(), list);
    
    title.setText(list.getTitle());
    title.setOpaque(true);
    title.setBackground(this.list.getBackground());
    add(title, BorderLayout.NORTH);
  }

  /** 
   * Constructor
   */
  public ContextListWidget(Gedcom gedcom, List<ViewContext> contextList) {
    
    super(new BorderLayout());
    
    this.gedcom = gedcom;
    this.contexts.addAll(contextList);
    
    list = new JList(new Model(contextList));
    list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    list.setCellRenderer(callback);
    list.addListSelectionListener(callback);
    
    add(list, BorderLayout.CENTER);
  }

  public String getTitle() {
    return title.getText();
  }
  
  public List<ViewContext> getContexts() {
    return contexts;
  }
    
  
  /**
   * Provides a 'current' context
   */
  public ViewContext getContext() {
    
    Object[] selection = list.getSelectedValues();
    
    // one selected?
    if (selection.length==1&&selection[0] instanceof ViewContext)
      return (ViewContext)selection[0];
    
    // merge
    List<Property> props = new ArrayList<Property>(16);
    List<Entity> ents = new ArrayList<Entity>(16);
    
    for (int i = 0; i < selection.length; i++) {
      ViewContext context = (ViewContext)selection[i];
      props.addAll(context.getProperties());
      ents.addAll(context.getEntities());
    }
    
    ViewContext result = new ViewContext(gedcom, ents, props);
    
    // done
    return result;
  }
  
  /**
   * Component added is listening to gedcom
   */
  @Override
  public void addNotify() {
    // let super do its thing
    super.addNotify();
    // listen to gedcom 
    gedcom.addGedcomListener((GedcomListener)Spin.over(list.getModel()));
  }
  
  /**
   * Component removed is not listening to gedcom
   */
  @Override
  public void removeNotify() {
    // disconnect from gedcom
    gedcom.removeGedcomListener((GedcomListener)Spin.over(list.getModel()));
    // let super continue
    super.removeNotify();
  }
  
  /**
   * our model
   */
  private static class Model extends AbstractListModel implements GedcomListener {
    
    private List<ViewContext> list = new ArrayList<ViewContext>();
    
    private Model(List<ViewContext> set) {
      // clear old
      int n = list.size();
      list.clear();
      if (n>0)
        fireIntervalRemoved(this, 0, n-1);
      // keep new
      list.addAll(set);
      n = list.size();
      if (n>0)
        fireIntervalAdded(this, 0, n-1);
      // done
    }

    public int getSize() {
      return list.size();
    }

    public Object getElementAt(int index) {
      return list.get(index);
    }
    
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
      // ignore
    }

    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
      for (ListIterator<ViewContext> it=list.listIterator(); it.hasNext(); ) {
        ViewContext context = (ViewContext)it.next();
        if (context.getEntities().contains(entity))
          it.set(new ViewContext(context.getGedcom()));
      }
      // TODO this could be less coarse grained
      fireContentsChanged(this, 0, list.size());
    }

    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
      // ignore
    }

    public void gedcomPropertyChanged(Gedcom gedcom, Property prop) {
      // TODO this could be less coarse grained
      fireContentsChanged(this, 0, list.size());
    }

    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property removed) {
      for (ListIterator<ViewContext> it=list.listIterator(); it.hasNext(); ) {
        ViewContext context = (ViewContext)it.next();
        if (context.getProperties().contains(property))
          it.set(new ViewContext(context.getText(), context.getImage(), new Context(context.getGedcom())));
      }
      // TODO this could be less coarse grained
      fireContentsChanged(this, 0, list.size());
    }
    
  } //Model
  
  /** 
   * various callbacks in here
   */
  private class Callback extends DefaultListCellRenderer implements ListSelectionListener {
    
    /** propagate selection changes */
    public void valueChanged(ListSelectionEvent e) {
      if (e.getValueIsAdjusting())
        return;
      ViewContext context = getContext();
      if (context!=null)
    	  SelectionSink.Dispatcher.fireSelection(ContextListWidget.this,context, false);
    }
    
    /** our patched rendering */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      // let super do its thing
      super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
      // patch up
      ViewContext ctx = (ViewContext)value;
      setIcon(ctx.getImage());
      setText(ctx.getText());
      // done
      return this;
    }
  } //Renderer
}
