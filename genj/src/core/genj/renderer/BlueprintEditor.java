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
package genj.renderer;

import genj.app.TagPathTree;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.util.ActionDelegate;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.HashMap;

import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * An editor component for changing a rendering scheme */
public class BlueprintEditor extends Box {

  /** the text are for the html */
  private JTextArea html;
  
  /** the preview */
  private Preview preview;
  
  /** resources */
  private final static Resources resources = new Resources(BlueprintEditor.class);
  
  /** the gedcom we're looking at*/
  private Gedcom gedcom;
  
  /** the current scheme */
  private Blueprint blueprint;

  /** the insert button */
  private AbstractButton bInsert;
  
  /** an example entity we use */
  private Example example = new Example(); 
    
  /**
   * Constructor   */
  public BlueprintEditor() { super(BoxLayout.Y_AXIS);
    // preview
    preview = new Preview();
    preview.setBorder(BorderFactory.createTitledBorder("Preview"));
    // html
    html = new JTextArea(3,32);
    html.setFont(new Font("Monospaced", Font.PLAIN, 12));
    JScrollPane scroll = new JScrollPane(html);
    scroll.setBorder(BorderFactory.createTitledBorder("HTML"));
    // buttons
    Box buttons = new Box(BoxLayout.X_AXIS);
    ButtonHelper helper = new ButtonHelper().setContainer(buttons).setResources(resources);
    bInsert = helper.create(new ActionInsert());
    // layout
    add(preview);
    add(scroll);
    add(buttons);
    // event listening
    html.getDocument().addDocumentListener(preview);
    // intial set
    set(null,null);
    // done
  }
  
  /**
   * Set Gedcom, Blueprint
   */
  public void set(Gedcom geDcom, Blueprint scHeme) {
    boolean b;
    if (geDcom==null||scHeme==null) {
      gedcom = null;
      blueprint = null;
      html.setText("");
      b = false;
    } else {
      gedcom = geDcom;
      blueprint = scHeme;
      html.setText(blueprint.getHTML());
      b = true;
    }
    bInsert.setEnabled(b);
    html.setEditable(b);
    preview.repaint();
    // done    
  }
  
  /**
   * Commits changes   */
  public void commit() {
    if (blueprint!=null) blueprint.setHTML(html.getText());
  }
  
  /**
   * The preview   */
  private class Preview extends JComponent implements DocumentListener {
    /**
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
     */
    public void changedUpdate(DocumentEvent e) {
      repaint();
    }
    /**
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
    public void insertUpdate(DocumentEvent e) {
      repaint();
    }
    /**
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
     */
    public void removeUpdate(DocumentEvent e) {
      repaint();
    }
    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g) {
      // fix bounds (border changes insets)
      Rectangle bounds = getBounds();
      Insets insets = getInsets();
      bounds.x += insets.left;
      bounds.y += insets.top ;
      bounds.width -= insets.left+insets.right;
      bounds.height-= insets.top +insets.bottom;
      // clear background
      g.setColor(html.getText().length()==0 ? getBackground() : Color.white);
      g.fillRect(bounds.x,bounds.y,bounds.width,bounds.height);
      // render content
      new EntityRenderer(g, new Blueprint("",html.getText())).render(g, example, bounds);
    }
  } //Preview

  /**
   * Insert a property   */
  private class ActionInsert extends ActionDelegate {
    /** constructor */
    private ActionInsert() {
      super.setText("prop.insert");
      super.setTip("prop.insert.tip");
    }
    /** @see genj.util.ActionDelegate#execute() */
    protected void execute() {
      // create a tree of available TagPaths
      TagPathTree tree = new TagPathTree(); 
      tree.setPaths(TagPath.getUsedTagPaths(
        gedcom, BlueprintManager.getInstance().getType(blueprint)
      ));      
      // Recheck with the user
      int option = JOptionPane.showConfirmDialog(
        BlueprintEditor.this, tree, resources.getString("prop.insert.tip"), JOptionPane.OK_CANCEL_OPTION
      );
      // .. OK?
      if (option != JOptionPane.OK_OPTION) return;
      // add those properties
      TagPath[] paths = tree.getSelection();
      for (int p=0;p<paths.length; p++) {
        html.insert("<prop path="+paths[p].toString()+">\n", html.getCaretPosition());
      }
      // done
    }
  } //ActionInsert

  /**
   * Example
   */
  private class Example extends Indi {
    private Map tag2value = new HashMap();
    /**
     * Constructor
     */
    private Example() {
      tag2value.put("NAME", "John /Doe/");
      tag2value.put("SEX" , "M");
      tag2value.put("DATE", "01 JAN 1900");
      tag2value.put("PLAC", "Somewhere");
    }
    /**
     * @see genj.gedcom.Indi#getId()
     */
    public String getId() {
      String prefix;
      if (blueprint==null) prefix = "X";
      else prefix = Gedcom.getPrefixFor(BlueprintManager.getInstance().getType(blueprint));
      return prefix+"999";
    }
    /**
     * @see genj.gedcom.PropertyIndi#getTag()
     */
    public String getTag() {
      return blueprint==null ? 
        super.getTag() : 
        Gedcom.getTagFor(BlueprintManager.getInstance().getType(blueprint));
    }
    /**
     * @see genj.gedcom.Property#getProperty(genj.gedcom.TagPath, boolean)
     */
    public Property getProperty(TagPath path, boolean validOnly) {
      // single?
      if (path.length()==1) return path.getLast().equals(getTag()) ? this : null;
      // grab a value and wrap it in the property
      Object value = tag2value.get(path.getLast());
      if (value==null) value = "some "+path.getLast().toLowerCase();
      // .. create the property
      Property result = Property.createInstance(path.getLast(), value.toString(), true);
      // done
      return result;
    }
  } //ExampleIndi  
} //RenderingSchemeEditor
