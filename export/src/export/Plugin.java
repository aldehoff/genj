/**
 * GenJ plugin - Export Gedcom File
 */
package export;

import genj.app.Workbench;
import genj.app.WorkbenchAdapter;
import genj.common.SelectEntityWidget;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.DateWidget;
import genj.util.swing.DialogHelper;
import genj.util.swing.FileChooserWidget;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.Action2.Group;
import genj.view.ActionProvider;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * Plugin logic
 */
public class Plugin extends WorkbenchAdapter implements ActionProvider {
  
  private final static ImageIcon LEGEND = new ImageIcon(Plugin.class, "legend.png");
  
  private final static String LAYOUT = 
    "<col>"+
      "<row><file/><file wx=\"1\"/></row>"+
      "<col><filter/>"+
        "<row><root/><root wx=\"1\"/></row>"+
        "<img/>"+
        "<table pad=\"8\">"+
          "<row><gen/><gen/><gen/><gen/></row>"+
          "<row><deg/><deg/><deg/><deg/></row>"+
        "</table>"+
      "</col>"+
      "<col><filter/>"+
        "<table pad=\"8\">"+
          "<row><even/><event wx=\"1\"/></row>"+
          "<row><born/><born wx=\"1\"/></row>"+
          "<row><living cols=\"2\"/></row>"+
          "<row><empty cols=\"2\"/></row>"+
        "</table>"+
      "</col>"+
    "</col>";
  
  private final static Resources RESOURCES = Resources.get(Plugin.class);

  public void createActions(Context context, Purpose purpose, Group into) {
    
    if (context.getGedcom()==null)
      return;
    
    switch (purpose) {
      case CONTEXT:
        // none
        break;
      case MENU:
        into.add(new Export(context.getGedcom()));
        break;
      case TOOLBAR:
        // none
        break;
    }
  }
  
  @Override
  public void gedcomOpened(Workbench workbench, Gedcom gedcom) {
  }
  
  @Override
  public void gedcomClosed(Workbench workbench, Gedcom gedcom) {
  }
  
  
  /**
   * An action we plug-into the workbench
   */
  private class Export extends Action2 {
    
    private Gedcom gedcom;
    
    public Export(Gedcom gedcom) {
      this.gedcom = gedcom;
      setText(RESOURCES.getString("export"));
      setImage(new ImageIcon(this, "plugin.png"));
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
      
      JPanel content = new JPanel(new NestedBlockLayout(LAYOUT));

      content.add(new JLabel(RESOURCES.getString("file")));
      content.add(new FileChooserWidget("ged"));
      
      content.add(new NestedBlockLayout.Expander(RESOURCES.getString("filter.rela")));
      content.add(new JLabel(RESOURCES.getString("filter.rela.root")));
      content.add(new SelectEntityWidget(gedcom, "INDI", RESOURCES.getString("filter.rela.root.select")));
      content.add(new JLabel(LEGEND));
      content.add(new JLabel(RESOURCES.getString("filter.rela.ancestors")));
      content.add(new JTextField(3));
      content.add(new JLabel(RESOURCES.getString("filter.rela.descendants")));
      content.add(new JTextField(3));
      content.add(new JLabel(RESOURCES.getString("filter.rela.degree")));
      content.add(new JTextField(3));
      content.add(new JLabel(RESOURCES.getString("filter.rela.removed")));
      content.add(new JTextField(3));

      content.add(new NestedBlockLayout.Expander(RESOURCES.getString("filter.data")));
      content.add(new JLabel(RESOURCES.getString("filter.data.even")));
      content.add(new DateWidget());
      content.add(new JLabel(RESOURCES.getString("filter.data.born")));
      content.add(new DateWidget());
      content.add(new JCheckBox(RESOURCES.getString("filter.data.living")));
      content.add(new JCheckBox(RESOURCES.getString("filter.data.empty")));
            
      // show a simple hello dialog with an ok button only
      DialogHelper.openDialog(
          getText(), 
          DialogHelper.INFORMATION_MESSAGE, 
          new JScrollPane(content), 
          Action2.okOnly(), 
          DialogHelper.getComponent(event));
      
      // done
    }
    
  }

}
