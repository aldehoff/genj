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
package genj.app;

import genj.gedcom.Gedcom;
import genj.print.PrintProperties;
import genj.print.PrintRenderer;
import genj.print.Printer;
import genj.util.ActionDelegate;
import genj.util.ImgIcon;
import genj.util.Origin;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImgIconConverter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * A bridge to open/manage Views
 */
public class ViewManager {

  /** instance */
  private static ViewManager instance;

  /** descriptors of views */
  static final private Descriptor[] descriptors = new Descriptor[]{
    new Descriptor("genj.table.TableViewFactory"      ,"table"    ,Images.imgNewTable    , new Dimension(480,320)),
    new Descriptor("genj.tree.TreeViewFactory"        ,"tree"     ,Images.imgNewTree     , new Dimension(480,480)),
    new Descriptor("genj.timeline.TimelineViewFactory","timeline" ,Images.imgNewTimeline , new Dimension(480,256)),
    new Descriptor("genj.edit.EditViewFactory"        ,"edit"     ,Images.imgNewEdit     , new Dimension(256,480)),
    new Descriptor("genj.report.ReportViewFactory"    ,"report"   ,Images.imgNewReport   , new Dimension(480,320)),
    new Descriptor("genj.nav.NavigatorViewFactory"    ,"navigator",Images.imgNewNavigator, new Dimension(140,200))
  };

  /**
   * Singleton access
   */
  public static ViewManager getInstance() {
    if (instance==null) instance = new ViewManager();
    return instance;
  }

  /**
   * Returns all known descriptors
   */
  public Descriptor[] getDescriptors() {
    return descriptors;
  }

  /**
   * Helper that returns the next logical registry-view
   * for given gedcom and name of view
   */
  private Registry getRegistry(Gedcom gedcom, String nameOfView) {

    // Check which iteration number is available next
    Origin origin = gedcom.getOrigin();
    String name = origin.getFileName();
    int number;
    for (number=1;;number++) {
      if (App.getInstance().getFrame(name+"."+nameOfView+"."+number)==null) {
        break;
      }
    }

    // Try to find a registry
    Registry registry = Registry.lookup(name);
    if (registry==null) {
      registry = new Registry(name, origin);
    }

    return new Registry(registry, nameOfView+"."+number);

    // done
  }

  /**
   * Opens settings for given view
   */
  public void openSettings(ViewSettingsWidget vsw) {

    // the frame for the settings
    JFrame frame = App.getInstance().getFrame("settings");
    if (frame==null) {
      // create it
      frame = App.getInstance().createFrame(
        App.resources.getString("cc.title.settings_edit"),
        Images.imgSettings,
        "settings",
        new Dimension(256,480)
      );
      // and the SettingsWidget
      SettingsContainer sw = new SettingsContainer(App.resources, frame);
      frame.getContentPane().add(sw);
      // layout      
      frame.pack();
    }
    
    // get the SettingsWidget
    SettingsContainer sw = (SettingsContainer)frame.getContentPane().getComponent(0);
    sw.setViewSettingsWidget(vsw);
    
    // show it
    frame.show();
        
    // done
  }

  /**
   * Opens a view on a gedcom file
   */
  public void openView(Descriptor descriptor, Gedcom gedcom) {
    
    // get a registry 
    Registry registry = getRegistry(gedcom, descriptor.key);
    
    // a frame
    JFrame frame = App.getInstance().createFrame(
      gedcom.getName()+" - "+App.resources.getString("cc.view."+descriptor.key)+" ("+registry.getViewSuffix()+")",
      descriptor.img,
      gedcom.getName() + "." + registry.getView(),
      descriptor.dim
    );
    
    // and an instance of the view
    Component view = createView(descriptor, gedcom, registry, frame);
    
    // show it
    frame.getContentPane().add(view);
    frame.pack();
    frame.show();
    
    // done
  }
  
  /**
   * Creates an instance of a view for given factory
   */
  private Component createView(Descriptor descriptor, Gedcom gedcom, Registry registry, JFrame frame) {
    
    // create a factory
    ViewFactory factory = descriptor.instantiate();
    
    // ask the factory
    Component result = factory.createViewComponent(gedcom, registry, frame);
    
    // can we add something to it
    if (!(result instanceof awtx.Scrollpane))
      return result;
      
    // treat as Scrollpane
    awtx.Scrollpane scroll = (awtx.Scrollpane)result;
      
    ButtonHelper bh = new ButtonHelper()
      .setResources(App.resources)
      .setInsets(0);

    // A button for editing the View's settings
    ViewSettingsWidget vsWidget = factory.createSettingsComponent(result);
    if (vsWidget!=null) {
      vsWidget.setBorder(new TitledBorder(frame.getTitle()));
      scroll.add2Edge(bh.create(new ActionOpenSettings(vsWidget)));
    }
    
    // A button for printing View
    PrintRenderer renderer = factory.createPrintRenderer(result);
    if (renderer!=null) {
      scroll.add2Edge(bh.create(new ActionPrint(renderer, frame)));
    }
    
    // done
    return result;  
  }
  
  /**
   * Closes all views on given Gedcom
   */
  public void closeViews(Gedcom gedcom) {
    
    // the view looking on that gedcom
    Vector targets = new Vector();

    // search through frames
    Hashtable frames = App.getInstance().getFrames();
    Enumeration vs = frames.keys();
    while (vs.hasMoreElements()) {
      String key = (String)vs.nextElement();
      if (key.indexOf('.'+gedcom.getName()+'.')>0) targets.add(frames.get(key));
    }

    // close those views
    vs = targets.elements();
    while (vs.hasMoreElements()) {
      ((JFrame)vs.nextElement()).dispose();
    }
    
    // and close the Settings, too
    JFrame frame = App.getInstance().getFrame("settings");
    if (frame!=null) frame.dispose();
    

    // done
    
  }

  /**
   * A descriptor of a View
   */
  public static class Descriptor {
    
    public String factory;
    public String key;
    public ImgIcon img;
    public Dimension dim;
    
    /**
     * Constructor
     */
    protected Descriptor(String f, String k, ImgIcon i, Dimension d) {
      factory=f;key=k;img=i;dim=d;
    }
    
    /**
     * Get an instance of the factory
     */
    protected ViewFactory instantiate() {
      try {
        return (ViewFactory)Class.forName(factory).newInstance();
      } catch (Throwable t) {
        throw new RuntimeException("ViewFactory "+factory+" couldn't be instantiated");
      }
    }
    
  } //Descriptor

  /**
   * Action - print view
   */
  private class ActionPrint extends ActionDelegate {
    /** the renderer */
    private PrintRenderer renderer;
    /** the frame */
    private Frame frame;
    /** constructor */
    protected ActionPrint(PrintRenderer r, Frame f) {
      renderer=r;
      frame=f;
      super.setImage(Images.imgPrint).setTip("cc.tip.print");
    }
    /** run */
    protected void execute() {
      Printer.print(frame, renderer, new PrintProperties(frame.getTitle()));
    }
  } //ActionOpenSettings
  
  /**
   * Action - open the settings of a view
   */
  private class ActionOpenSettings extends ActionDelegate {
    /** the settings widget */
    private ViewSettingsWidget vsw;
    /** constructor */
    protected ActionOpenSettings(ViewSettingsWidget vsw) {
      this.vsw=vsw;
      super.setImage(Images.imgSettings).setTip("cc.tip.settings");
    }
    /** run */
    protected void execute() {
      openSettings(vsw);
    }
  } //ActionOpenSettings
  
  /**
   * A settings component 
   */
  private class SettingsContainer extends JPanel {
    
    /** members */
    private JPanel pSettings,pActions;
    private Vector vButtons = new Vector();
    private ViewSettingsWidget vsWidget = null;
    private JLabel lIdle;
    
    /**
     * Constructor
     */
    protected SettingsContainer(Resources resources, JFrame frame) {
      
      // Idle case prep
      lIdle = new JLabel(resources.getString("view.choose"),ImgIconConverter.get(Images.imgSettings),JLabel.CENTER);
      lIdle.setHorizontalTextPosition(lIdle.LEADING);
      
      // Panel for ViewSettingsWidget
      pSettings = new JPanel(new BorderLayout());

      // Panel for Actions
      JPanel pActions = new JPanel();

      ButtonHelper bh = new ButtonHelper()
        .setResources(resources)
        .setContainer(pActions)
        .setCollection(vButtons)
        .setEnabled(false);
        
      bh.create(new ActionApply());
      bh.create(new ActionReset());
      bh.setCollection(null)
        .setEnabled(true)
        .create(new ActionDelegate.ActionDisposeFrame(frame).setText("view.close"));

      // Layout
      setLayout(new BorderLayout());
      add(pSettings,"Center");
      add(pActions ,"South" );
      
      // init
      setViewSettingsWidget(null);
    }

    /**
     * Sets the ViewSettingsWidget to display
     */
    protected void setViewSettingsWidget(ViewSettingsWidget vsw) {
      
      vsWidget = vsw;

      ButtonHelper.setEnabled(vButtons, vsw!=null);
      
      pSettings.removeAll();
      
      if (vsw==null) {
        pSettings.add(lIdle, BorderLayout.CENTER);
      } else {
        vsw.reset();
        pSettings.add(vsw, BorderLayout.CENTER);
      }
      pSettings.invalidate();
      pSettings.validate();
      pSettings.repaint();
    }

    /**
     * Applies the changes currently being done
     */
    private class ActionApply extends ActionDelegate {
      protected ActionApply() { super.setText("view.apply"); }
      protected void execute() {
        if (vsWidget!=null) vsWidget.apply();
      }
    }
  
    /**
     * Resets any change being done
     */
    private class ActionReset extends ActionDelegate {
      protected ActionReset() { super.setText("view.reset"); }
      protected void execute() {
        if (vsWidget!=null) vsWidget.reset();
      }
    }
    
  } //SettingsWidget
  
}
