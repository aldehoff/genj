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

import genj.util.ActionDelegate;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.ProgressWidget;
import genj.view.Settings;
import genj.view.ViewManager;
import genj.window.WindowManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.AbstractListModel;
import javax.swing.JComboBox;
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
  
  private final static String[] USSTATES = {
    "AL","AK","AS","AZ","AR","CA","CO","CT","DE","DC","FM","FL","GA","GU","HI","ID","IL",
    "IN","IA","KS","KY","LA","ME","MH","MD","MA","MI","MN","MS","MO","MT","NE","NV","NH",
    "NJ","NM","NY","NC","ND","MP","OH","OK","OR","PW","PA","PR","RI","SC","SD","TN","TX",
    "UT","VT","VI","VA","WA","WV","WI","WY"
  };
  
  /** a list of countries we have */
  private JList listCountries;
  
  /** reference to view manager */
  private ViewManager viewManager;
  
  /**
   * Initializer
   */
  public void init(ViewManager manager) {

    this.viewManager = manager;
    
    // setup components
    setLayout(new NestedBlockLayout("<col><row><label/></row><row><col><up gx=\"1\"/><down gx=\"1\"/><import gx=\"1\"/><delete gx=\"1\"/></col><col><list wx=\"1\"/></col></row></col>"));
    
    // .. a list of gazetteers, buttons for up and down, delete, import
    listCountries = new JList();
    
    ButtonHelper bh = new ButtonHelper().setResources(GeoView.RESOURCES);
    add("label", new JLabel(GeoView.RESOURCES.getString("label.gazetteer")));
    add("up", bh.create(new UpDown(true)));
    add("down", bh.create(new UpDown(false)));
    add("import", bh.create(new DoImport()));
    add("delete", bh.create(new Delete()));
    add("list", new JScrollPane(listCountries));
    
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
    
    final Country[] countries = GeoService.getInstance().getCountries();
    Arrays.sort(countries);
    listCountries.setModel(new AbstractListModel() {
      public int getSize() { return countries.length; }
      public Object getElementAt(int i) { return countries[i]; }
    });
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
      listCountries.addListSelectionListener(this);
    }
    /** callback - run */
    public void execute() {
    }
    /** callback - selection changed */
    public void valueChanged(ListSelectionEvent e) {
      boolean enabled;
      if (up)
        enabled = listCountries.getSelectionModel().getMinSelectionIndex()>0;
      else {
        int max = listCountries.getSelectionModel().getMaxSelectionIndex();
        enabled = max>=0 && max < listCountries.getModel().getSize()-1;
      }
      // FIXME setEnabled(enabled);
    }
  } //ActionUpDown

  /**
   * Action - Import
   */
  private class DoImport extends ActionDelegate {
    /** handle to progress dlg */
    private String progress;
    /** the import */
    private Import gztImport;
    /** constructor */
    protected DoImport() {
      setText( "action.import" );
      setAsync(ActionDelegate.ASYNC_SAME_INSTANCE);
    }
    /** callback - on EDT */
    protected boolean preExecute() {
      // disable actions
      setEnabled(false);
      // let user choose country, state
      SelectImportWidget select = new SelectImportWidget();
      int choice = viewManager.getWindowManager().openDialog(null, null, WindowManager.QUESTION_MESSAGE, select, WindowManager.ACTIONS_OK_CANCEL, GeoViewSettings.this);
      // prepare import
      try {
        gztImport = GeoService.getInstance().getImport(select.getCountry(), select.getState());
        if (choice!=0)
          return false;
      } catch (IOException e) {
        return false;
      }
      // .. show progress dialog
      progress = viewManager.getWindowManager().openNonModalDialog(
        null, GeoView.RESOURCES.getString("action.import"),
        WindowManager.INFORMATION_MESSAGE, new ProgressWidget(gztImport, getThread()), null, GeoViewSettings.this
      );
      // continue
      return true;
    }
    /** callback - run async */
    public void execute() {
      try {
        gztImport.run();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    /** callback -  a problem */
    protected void handleThrowable(String phase, Throwable t) {
      viewManager.getWindowManager().openDialog(null, null, WindowManager.QUESTION_MESSAGE, t.getMessage(), WindowManager.ACTIONS_OK, GeoViewSettings.this);
    }
    /** callback -EDT again */
    protected void postExecute() {
      // close progress
      viewManager.getWindowManager().close(progress);
      // enable actions againe
      setEnabled(true);
      // reset our settings
      reset();
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
      listCountries.addListSelectionListener(this);
    }
    /** callback - run */
    public void execute() {
      Country country = (Country)listCountries.getSelectedValue();
      if (country!=null) try {
        GeoService.getInstance().drop(country);
      } catch (IOException e) {
        viewManager.getWindowManager().openDialog(null, null, WindowManager.ERROR_MESSAGE, e.getMessage(), WindowManager.ACTIONS_OK, GeoViewSettings.this);
      }
      // reset our settings
      reset();
    }
    /** callback - selection changed */
    public void valueChanged(ListSelectionEvent e) {
      setEnabled(listCountries.getSelectedIndex()>=0);
    }
  } //Delete
  
  
  /**
   * A widget that allows to choose geo data to import
   */
  private class SelectImportWidget extends JPanel implements ActionListener {
    
    private JComboBox countries;
    private ChoiceWidget state;
    private Country USA = Country.get("us");
    
    /**
     * Constructor
     */
    public SelectImportWidget() {

      setLayout(new NestedBlockLayout("<col><row><label/></row><row><country wx=\"1\"/></row><row><label/><state/></row></col>"));

      state = new ChoiceWidget(USSTATES, null);
      
      countries = new JComboBox(Country.getAllCountries());
      countries.addActionListener(this);
      countries.setSelectedItem(Country.getDefaultCountry());
      
      add(new JLabel(GeoView.RESOURCES.getString("import.country")));
      add(countries);
      add(new JLabel(GeoView.RESOURCES.getString("import.state")));
      add(state);
    }
    
    /**
     * Callback - Country selected
     */
    public void actionPerformed(ActionEvent e) {
      if (USA.equals(countries.getSelectedItem())) {
        state.setEnabled(true);
      } else {
        state.setText("");
        state.setEnabled(false);
      }
    }
    
    /**
     * Accessor - selected iso country code
     */
    public Country getCountry() {
      return (Country)countries.getSelectedItem();
    }
    
    /**
     * Accessor - selected state
     */
    public String getState() {
      return state.getText();
    }
    
  }//SelectImportWidget
  
} //GeoViewSettings