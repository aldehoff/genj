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
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.util.ActionDelegate;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.SwingFactory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * An editor component for changing a rendering scheme */
public class BlueprintEditor extends JSplitPane {

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
  
  /** whether we've changed */
  private boolean isChanged = false;
    
  /**
   * Constructor   */
  public BlueprintEditor() { 
    // preview
    preview = new Preview();
    preview.setBorder(BorderFactory.createTitledBorder(resources.getString("blueprint.preview")));
    // edit
    JPanel edit = new JPanel(new BorderLayout());
      // html
      html = new JTextArea(3,32);
      html.setFont(new Font("Monospaced", Font.PLAIN, 12));
      JScrollPane scroll = new JScrollPane(html);
      scroll.setBorder(BorderFactory.createTitledBorder("HTML"));
      // buttons
      ButtonHelper helper = new ButtonHelper().setResources(resources);
      bInsert = helper.create(new ActionInsert());
    edit.setMinimumSize(new Dimension(0,0));
    edit.add(scroll, BorderLayout.CENTER);
    edit.add(bInsert, BorderLayout.SOUTH);
    // layout
    setLeftComponent(preview);
    setRightComponent(edit);
    setDividerLocation(Integer.MAX_VALUE);
    setOrientation(JSplitPane.VERTICAL_SPLIT);
    setOneTouchExpandable(true);
    // event listening
    html.getDocument().addDocumentListener(preview);
    // intial set
    set(null,null, false);
    // done
  }
  
  /**
   * @see javax.swing.JSplitPane#getLastDividerLocation()
   */
  public int getLastDividerLocation() {
    return getSize().height/2;
  }
  
  /**
   * Set Gedcom, Blueprint
   */
  public void set(Gedcom geDcom, Blueprint scHeme, boolean editable) {
    // resolve buttons and html
    gedcom = geDcom;
    if (scHeme==null) {
      blueprint = null;
      html.setText("");
      editable = false;
    } else {
      blueprint = scHeme;
      html.setText(blueprint.getHTML());
      html.setCaretPosition(0);
    }
    bInsert.setEnabled(editable&&gedcom!=null);
    html.setEditable(editable);
    html.setToolTipText(editable||blueprint==null?null:resources.getString("blueprint.readonly", blueprint.getName()));
    // mark unchanged
    isChanged = false;
    // make sure that changes
    preview.repaint();
    // is it an editable one?
    if (blueprint!=null&&!blueprint.isReadOnly()) setHTMLVisible(true);
    // done    
  }
  
  /**
   * Commits changes   */
  public void commit() {
    if (blueprint!=null&&isChanged) {
      blueprint.setHTML(html.getText());
      // mark unchanged
      isChanged = false;
    }
  }
  
  /**
   * Make sure html is visible
   */
  public void setHTMLVisible(boolean v) {
    setDividerLocation( v ? 0.5D : 1.0D);
  }
  
  /**
   * The preview   */
  private class Preview extends JComponent implements DocumentListener {
    /**
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
     */
    public void changedUpdate(DocumentEvent e) {
      isChanged = true;
      repaint();
    }
    /**
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
    public void insertUpdate(DocumentEvent e) {
      isChanged = true;
      repaint();
    }
    /**
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
     */
    public void removeUpdate(DocumentEvent e) {
      isChanged = true;
      repaint();
    }
    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g) {
      // no html doing nothing
      if (html.getText().length()==0) return; 
      // fix bounds (border changes insets)
      Rectangle bounds = getBounds();
      Insets insets = getInsets();
      bounds.x += insets.left;
      bounds.y += insets.top ;
      bounds.width -= insets.left+insets.right;
      bounds.height-= insets.top +insets.bottom;
      // clear background
      g.setColor(Color.white);
      g.fillRect(bounds.x,bounds.y,bounds.width,bounds.height);
      // render content
      EntityRenderer renderer = new EntityRenderer(new Blueprint(html.getText()));
      renderer.setDebug(isChanged);
      renderer.render(g, example, bounds);
      // done
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
      // only if gedcom is valid
      if (gedcom==null) return;
      // create a tree of available TagPaths
      TagPathTree tree = new TagPathTree();
      TagPath[] paths = MetaProperty.getPaths(Property.class); 
      TagPath.filter(paths, BlueprintManager.getInstance().getType(blueprint));
      tree.setPaths(paths, new TagPath[0]);
      // Recheck with the user
      int option = JOptionPane.showConfirmDialog(
        BlueprintEditor.this, tree, resources.getString("prop.insert.tip"), JOptionPane.OK_CANCEL_OPTION
      );
      // .. OK?
      if (option != JOptionPane.OK_OPTION) return;
      // add those properties
      paths = tree.getSelection();
      for (int p=0;p<paths.length; p++) {
        html.insert(
          "<prop path="+paths[p].toString()+">"+(p==paths.length-1?"":"\n"), 
          html.getCaretPosition()
        );
      }
      // request focus
      SwingFactory.requestFocusFor(html);
      // done
    }
  } //ActionInsert

  /**
   * Example
   */
  private class Example extends Entity  {
    
    /** faked values */
    private Map tag2value = new HashMap();
    
    /**
     * Constructor
     */
    private Example() {
      tag2value.put("NAME", "John /Doe/");
      tag2value.put("SEX" , "M");
      tag2value.put("DATE", "01 JAN 1900");
      tag2value.put("PLAC", "Nice Place");
      tag2value.put("ADDR", "Long Address");
      tag2value.put("CITY", "Big City");
      tag2value.put("POST", "12345");
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
      // safety check for root-tag
      if (!path.get(0).equals(getTag())) return null;
      // fake it
      return fakeProperty(this, path, 0);
    }
    
    /**
     * Fake having a property
     */
    private Property fakeProperty(Property prop, TagPath path, int pos) {

      // me?
      if (path.length()-1==pos) return prop;

      // check if we have a property for tag at pos in path
      String tag = path.get(++pos);
      Property result = prop.getProperty(tag);
      if (result==null) {
        // otherwise create it
        Object value = tag2value.get(tag);
        if (value==null) value = "Something";
        result = prop.addProperty(MetaProperty.get(prop).get(tag).create(value.toString()));
      }      
      // done
      return fakeProperty(result, path, pos);
    }
    
  } //ExampleIndi
  
} //RenderingSchemeEditor
