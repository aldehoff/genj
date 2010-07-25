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
import genj.io.Filter;
import genj.io.GedcomWriter;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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
    private FileChooserWidget fileChooser = new FileChooserWidget("ged");
    private Action[] okCancel = Action2.okCancel();
    
    Export(Gedcom gedcom) {
      this.gedcom = gedcom;
      setText(RESOURCES.getString("export"));
      setImage(new ImageIcon(this, "plugin.png"));
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
      
      RelationshipFilter rela = new RelationshipFilter(gedcom);
      DataFilter data = new DataFilter();
      List<Filter> filters = new ArrayList<Filter>();
      filters.add(rela);
      filters.add(data);
      
      JTabbedPane tabs =  new JTabbedPane(JTabbedPane.TOP);
      tabs.addTab(rela.getName(), rela);
      tabs.addTab(data.getName(), data);
      
      JPanel content = new JPanel(new NestedBlockLayout(
        "<col wx=\"1\" wy=\"1\">"+
         "<row><file/><file wx=\"1\"/></row>"+
         "<filters/>"+
        "</col>"
      ));
      content.add(new JLabel(RESOURCES.getString("file")));
      content.add(fileChooser);
      content.add(tabs);

      // add ok verification
      okCancel[0].setEnabled(false);
      fileChooser.addChangeListener(this);
      
      // restore
      fileChooser.setFile(REGISTRY.get("file", ""));      
            
      // show it
      int ok = DialogHelper.openDialog(getText(), DialogHelper.INFORMATION_MESSAGE, content, okCancel, event);
      if (ok!=0)
        return;

      // check exist
      File file = fileChooser.getFile();
      if (file.exists()) {
        if (0!=DialogHelper.openDialog(getText(), DialogHelper.QUESTION_MESSAGE, RESOURCES.getString("overwrite", file), Action2.okCancel(), event))
          return;
      }
      REGISTRY.put("file", file.getAbsolutePath());      
      
      // open file and write
      OutputStream out = null;
      try {
        out = new FileOutputStream(file);
        GedcomWriter writer = new GedcomWriter(gedcom, out);
        writer.setFilters(filters);
        writer.write();
      } catch (Throwable t) {
        DialogHelper.showError(getText(), t.getMessage(), t, event);
      } finally {
        if (out!=null) try { out.close(); } catch (Throwable t) {};
      }
      
      // done
      DialogHelper.showInfo(getText(), RESOURCES.getString("done", file.getName()), event);
      
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
      
      boolean ok = true;
      
      if (fileChooser.getFile().getName().length()==0)
        ok = false;            
      
      okCancel[0].setEnabled(ok);
    }
    
  }
  
  

}
