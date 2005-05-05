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
package genj.geo;

import genj.util.ActionDelegate;
import genj.util.swing.ButtonHelper;
import genj.util.swing.NestedBlockLayout;
import genj.view.Settings;
import genj.view.ViewManager;

import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Class for providing editable settings to user
 */
public class GeoViewSettings extends JPanel implements Settings {
  
  /** a list of gazetters we have */
  private JList listGazetteers;
  
  /**
   * Initializer
   */
  public void init(ViewManager manager) {

    // setup components
    setLayout(new NestedBlockLayout("<col><row><label/></row><row><col><up gx=\"1\"/><down gx=\"1\"/><import gx=\"1\"/><delete gx=\"1\"/></col><col><list wx=\"1\"/></col></row></col>"));
    
    // .. a list of gazetteers, buttons for up and down, delete, import
    GeoService.Gazetteer[] gazetteers = GeoService.getInstance().getGazetteers();
    Arrays.sort(gazetteers);
    listGazetteers = new JList(gazetteers);
    
    ButtonHelper bh = new ButtonHelper().setResources(GeoView.RESOURCES);
    add("label", new JLabel(GeoView.RESOURCES.getString("label.gazetteers")));
    add("up", bh.create(new UpDown(true)));
    add("down", bh.create(new UpDown(false)));
    add("import", bh.create(new Import()));
    add("delete", bh.create(new Delete()));
    add("list", new JScrollPane(listGazetteers));
    
    // done
  }

  /**
   * Current GeoView
   */
  public void setView(JComponent view) {
  }

  /**
   * 
   */
  public JComponent getEditor() {
    return this;
  }

  /**
   * Callback - apply changes
   */
  public void apply() {
  }

  /**
   * Callback - reset changes
   */
  public void reset() {
  }

  /**
   * Action - Up and Down
   */
  private class UpDown extends ActionDelegate implements ListSelectionListener {
    /** up or down */
    private boolean up;
    /** constructor */
    protected UpDown(boolean up) {
      this.up = up;
      setEnabled(false);
      setText( up ? "action.up" : "action.down");
      listGazetteers.addListSelectionListener(this);
    }
    /** callback - run */
    public void execute() {
    }
    /** callback - selection changed */
    public void valueChanged(ListSelectionEvent e) {
      boolean enabled;
      if (up)
        enabled = listGazetteers.getSelectionModel().getMinSelectionIndex()>0;
      else {
        int max = listGazetteers.getSelectionModel().getMaxSelectionIndex();
        enabled = max>=0 && max < listGazetteers.getModel().getSize()-1;
      }
      setEnabled(enabled);
    }
  } //ActionUpDown

  /**
   * Action - Import
   */
  private class Import extends ActionDelegate {
    /** constructor */
    protected Import() {
      setText( "action.import" );
    }
    /** callback - run */
    public void execute() {
    }
  } //Import
  
  /**
   * Action - Delete
   */
  private class Delete extends ActionDelegate implements ListSelectionListener {
    /** constructor */
    protected Delete() {
      setText( "action.delete" );
      setEnabled(false);
      listGazetteers.addListSelectionListener(this);
    }
    /** callback - run */
    public void execute() {
    }
    /** callback - selection changed */
    public void valueChanged(ListSelectionEvent e) {
      int index = listGazetteers.getSelectedIndex();
      setEnabled(index>=0);
    }
  } //Delete
}