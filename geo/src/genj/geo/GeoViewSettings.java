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
import genj.window.CloseWindow;
import genj.window.WindowManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

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
  
  private static Country DEFAULT_COUNTRY;
  private final static Country[] COUNTRIES = initCountries();
  private final static String[] USSTATES = {
    "AL","AK","AS","AZ","AR","CA","CO","CT","DE","DC","FM","FL","GA","GU","HI","ID","IL",
    "IN","IA","KS","KY","LA","ME","MH","MD","MA","MI","MN","MS","MO","MT","NE","NV","NH",
    "NJ","NM","NY","NC","ND","MP","OH","OK","OR","PW","PA","PR","RI","SC","SD","TN","TX",
    "UT","VT","VI","VA","WA","WV","WI","WY"
  };
  
  /** a list of gazetters we have */
  private JList listGazetteers;
  
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
    listGazetteers = new JList();
    
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
    
    final GeoService.Gazetteer[] gazetteers = GeoService.getInstance().getGazetteers();
    Arrays.sort(gazetteers);
    listGazetteers.setModel(new AbstractListModel() {
      public int getSize() { return gazetteers.length; }
      public Object getElementAt(int i) { return gazetteers[i]; }
    });
  }

  /**
   * Lazy countries initializer
   */
  private static Country[] initCountries() {

    // grab current locale
    Locale locale = Locale.getDefault();
    
    // grab all country codes
    String[] codes = Locale.getISOCountries(); 
    Country[] result = new Country[codes.length];
    for (int i=0;i<result.length;i++) {
      Country country = new Country(codes[i]);
      result[i] = country;
      if (locale.getCountry().equals(country.iso))
        DEFAULT_COUNTRY = country;
    }
    
    // sort array & Done
    Arrays.sort(result);
    return result;
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
      // FIXME setEnabled(enabled);
    }
  } //ActionUpDown

  /**
   * Action - Import
   */
  private class Import extends ActionDelegate {
    /** handle to progress dlg */
    private String progress;
    /** the import */
    private GeoService.Import gztImport;
    /** what we've imported */
    private GeoService.Gazetteer imported;
    /** constructor */
    protected Import() {
      setText( "action.import" );
      setAsync(ActionDelegate.ASYNC_SAME_INSTANCE);
    }
    /** callback - on EDT */
    protected boolean preExecute() {
      // disable actions
      setEnabled(false);
      // let user choose country, state
      SelectImportWidget select = new SelectImportWidget();
      int choice = viewManager.getWindowManager().openDialog(null, null, WindowManager.IMG_QUESTION, select, CloseWindow.OKandCANCEL(), GeoViewSettings.this);
      // prepare import
      gztImport = GeoService.getInstance().getImport(select.getCountry(), select.getState());
      if (choice!=0)
        return false;
      // .. show progress dialog
      progress = viewManager.getWindowManager().openNonModalDialog(
        null, "Importing ...",
        WindowManager.IMG_INFORMATION, new ProgressWidget(gztImport, getThread()), null, GeoViewSettings.this
      );
      // continue
      return true;
    }
    /** callback - run async */
    public void execute() {
      try {
        imported = gztImport.run();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    /** callback -  a problem */
    protected void handleThrowable(String phase, Throwable t) {
      viewManager.getWindowManager().openDialog(null, null, WindowManager.IMG_QUESTION, t.getMessage(), CloseWindow.OK(), GeoViewSettings.this);
    }
    /** callback -EDT again */
    protected void postExecute() {
      // close progress
      viewManager.getWindowManager().close(progress);
      // enable actions againe
      setEnabled(true);
      // FIXME listGazetteers.add
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
      listGazetteers.addListSelectionListener(this);
    }
    /** callback - run */
    public void execute() {
    }
    /** callback - selection changed */
    public void valueChanged(ListSelectionEvent e) {
      int index = listGazetteers.getSelectedIndex();
      // FIXME setEnabled(index>=0);
    }
  } //Delete
  
  
  /**
   * A widget that allows to choose geo data to import
   */
  private class SelectImportWidget extends JPanel {
    
    private JComboBox countries;
    private ChoiceWidget state;

    /**
     * Constructor
     */
    public SelectImportWidget() {

      setLayout(new NestedBlockLayout("<col><row><label/></row><row><country wx=\"1\"/></row><row><label/><state/></row></col>"));

      countries = new JComboBox(COUNTRIES);
      countries.setSelectedItem(DEFAULT_COUNTRY);
      state = new ChoiceWidget(USSTATES, null);
      
      add(new JLabel("Please select a country"));
      add(countries);
      add(new JLabel("State (necessary for USA)"));
      add(state);
    }
    
    /**
     * Accessor - selected iso country code
     */
    public String getCountry() {
      return ((Country)countries.getSelectedItem()).iso;
    }
    
    /**
     * Accessor - selected state
     */
    public String getState() {
      return state.getText();
    }
    
  }//SelectImportWidget
  
  /**
   * A country - why isn't that in java.util
   */
  private static class Country implements Comparable {
    private String iso;
    private String name;
    private Country(String code) {
      iso = code;
      name =  new Locale("en", code).getDisplayCountry();
    }
    public String toString() {
      return name;
    }
    public int compareTo(Object o) {
      return toString().compareTo(o.toString());
    }
  } //Country
  
} //GeoViewSettings