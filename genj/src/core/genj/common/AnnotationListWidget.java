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

import genj.gedcom.Annotation;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.gedcom.Transaction;
import genj.view.Context;
import genj.view.ContextProvider;
import genj.view.ViewManager;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
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
 * A widget for rendering a list of annotations
 */
public class AnnotationListWidget extends JList implements ContextProvider {

  private ViewManager mgr;
  private Gedcom ged;
  
  private Callback callback = new Callback();
  
  /** 
   * Constructor
   */
  public AnnotationListWidget(ViewManager manager, Gedcom gedcom) {
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
  public AnnotationListWidget(ViewManager manager, Gedcom gedcom, List annotations) {
    this(manager, gedcom);
    setAnnotations(annotations);
  }
  
  /**
   * Provides a 'current' context
   */
  public Context getContext() {
    Context result = null;
    Object[] selection = getSelectedValues();
    for (int i = 0; i < selection.length; i++) {
      Annotation annotation = (Annotation)selection[i];
      Property context = annotation.getProperty();
      if (context!=null) {
        if (result==null) result = new Context(ged);
        result.addProperty(context);
      }
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
   * Set the annotations to show
   */
  public void setAnnotations(List annotations) {
    ((Model)getModel()).setAnnotations(annotations);
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
    
    private List annos = new ArrayList();
    
    private void setAnnotations(List annotations) {
      // clear old
      int n = annos.size();
      annos.clear();
      if (n>0)
        fireIntervalRemoved(this, 0, n-1);
      // keep new
      annos.addAll(annotations);
      n = annos.size();
      if (n>0)
        fireIntervalAdded(this, 0, n-1);
      // done
    }

    public int getSize() {
      return annos.size();
    }

    public Object getElementAt(int index) {
      return annos.get(index);
    }
    
    /** follow changes in gedcom */
    public void handleChange(Transaction tx) {
      
      Set deleted = tx.get(Transaction.PROPERTIES_DELETED);

      // throw away annotations as necessary
      int row = 0;
      for (ListIterator it = annos.listIterator(); it.hasNext(); row++) {
        Annotation anno = (Annotation) it.next();
        Property prop = anno.getProperty();
        if (prop!=null&&deleted.contains(prop)) {
          it.remove();
          fireIntervalRemoved(this, row, row);
          row--;
        }
      }
      
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
      Context context = getContext();
      if (context!=null)
        mgr.fireContextSelected(context);
    }
    
    /** our patched rendering */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      // let super do its thing
      super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
      // patch up
      Annotation annotation = (Annotation)value;
      setIcon(annotation.getImage());
      setText(annotation.getText());
      // done
      return this;
    }
  } //Renderer
}
