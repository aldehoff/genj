/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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
package export;

import genj.app.Workbench;
import genj.app.WorkbenchAdapter;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import genj.util.swing.FileChooserWidget;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.Action2.Group;
import genj.view.ActionProvider;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Plugin logic
 */
public class Plugin extends WorkbenchAdapter implements ActionProvider {

  private final static Registry REGISTRY = Registry.get(Plugin.class);  
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
  private class Export extends Action2 implements ChangeListener {
    
    private Gedcom gedcom;
    private FileChooserWidget file = new FileChooserWidget("ged");
    private Action[] okCancel = Action2.okCancel();
    
    Export(Gedcom gedcom) {
      this.gedcom = gedcom;
      setText(RESOURCES.getString("export"));
      setImage(new ImageIcon(this, "plugin.png"));
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
      
      RelationshipFilter rela = new RelationshipFilter(gedcom);
      DataFilter data = new DataFilter(gedcom);
      TypeFilter type = new TypeFilter(gedcom);
      
      JTabbedPane filters =  new JTabbedPane(JTabbedPane.TOP);
      filters.addTab(rela.name(), rela);
      filters.addTab(data.name(), data);
      filters.addTab(type.name(), type);
      
      JPanel content = new JPanel(new NestedBlockLayout(
        "<col wx=\"1\" wy=\"1\">"+
         "<row><file/><file wx=\"1\"/></row>"+
         "<filters/>"+
        "</col>"
      ));
      content.add(new JLabel(RESOURCES.getString("file")));
      content.add(file);
      content.add(filters);

      // add ok verification
      okCancel[0].setEnabled(false);
      file.addChangeListener(this);
      
      // restore
      file.setFile(REGISTRY.get("file", ""));      
            
      // show a simple hello dialog with an ok button only
      DialogHelper.openDialog(
          getText(), 
          DialogHelper.INFORMATION_MESSAGE, 
          content, 
          okCancel,
          DialogHelper.getComponent(event));
      
      // done
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
      
      boolean ok = true;
      
      if (file.getFile().getName().length()==0)
        ok = false;            
      
      okCancel[0].setEnabled(ok);
    }
    
  }
  
  

}
