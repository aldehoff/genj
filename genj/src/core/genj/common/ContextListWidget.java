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
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Transaction;
import genj.view.ContextProvider;
import genj.view.ViewContext;
import genj.view.ViewManager;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A widget for rendering a list of contexts
 */
public class ContextListWidget extends JList implements ContextProvider {

  private ViewManager mgr;
  private Gedcom ged;
  
  private Callback callback = new Callback();
  
  /** 
   * Constructor
   */
  public ContextListWidget(ViewManager manager, Gedcom gedcom) {
    super(new Model());
    mgr = manager;
    ged = gedcom;
    setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    setCellRenderer(callback);
    addListSelectionListener(callback);
  }

  /** 
   * Constructor
   */
  public ContextListWidget(ViewManager manager, Gedcom gedcom, List contextList) {
    this(manager, gedcom);
    setContextList(contextList);
  }
  
  /**
   * Provides a 'current' context
   */
  public ViewContext getContext() {
    ViewContext result = new ViewContext(ged);
    Object[] selection = getSelectedValues();
    for (int i = 0; i < selection.length; i++) {
      Context context = (Context)selection[i];
      result.addContext(context);
    }
    return result;
  }
  
  /**
   * @see JComponent#addNotify()
   */
  public void addNotify() {
    // let super do its thing
    super.addNotify();
    // listen to gedcom 
    ged.addGedcomListener((Model)getModel());
  }
  
  /**
   * @see JComponent#removeNotify()
   */
  public void removeNotify() {
    // disconnect from gedcom
    ged.removeGedcomListener((Model)getModel());
    // let super continue
    super.removeNotify();
  }
  
  /**
   * Set the list to show
   */
  public void setContextList(List contextList) {
    ((Model)getModel()).setContextList(contextList);
  }
  
  /**
   * @see JList#setModel(javax.swing.ListModel)
   */
  public void setModel(ListModel model) {
    if (!(model instanceof Model))
      throw new IllegalArgumentException("setModel() n/a");
    super.setModel(model);
  }

  /**
   * @see JList#setListData(java.lang.Object[])
   */
  public void setListData(Object[] listData) {
    throw new IllegalArgumentException("setListData() n/a");
  }
  
  /**
   * @see JList#setListData(java.util.Vector)
   */
  public void setListData(Vector listData) {
    throw new IllegalArgumentException("setListData() n/a");
  }
  
  /**
   * our model
   */
  private static class Model extends AbstractListModel implements GedcomListener {
    
    private List list = new ArrayList();
    
    private void setContextList(List set) {
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
    
    /** follow changes in gedcom */
    public void handleChange(Transaction tx) {
      
      if (list.size()==0)
        return;
      
      Set propsDeleted = tx.get(Transaction.PROPERTIES_DELETED);
      Set entsDeleted = tx.get(Transaction.ENTITIES_DELETED);
      
      for (Iterator it=list.iterator(); it.hasNext(); ) {
        Context context = (Context)it.next();
        context.removeProperties(propsDeleted);
      }

      for (Iterator it=list.iterator(); it.hasNext(); ) {
        Context context = (Context)it.next();
        context.removeEntities(entsDeleted);
      }
      
      fireContentsChanged(this, 0, list.size());

      // done
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
        mgr.fireContextSelected(context);
    }
    
    /** our patched rendering */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      // let super do its thing
      super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
      // patch up
      Context ctx = (Context)value;
      setIcon(ctx.getImage());
      setText(ctx.getText());
      // done
      return this;
    }
  } //Renderer
}
