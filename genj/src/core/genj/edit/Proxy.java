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
package genj.edit;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.renderer.EntityRenderer;
import genj.util.ActionDelegate;
import genj.util.ObservableBoolean;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property
 */
/*package*/ abstract class Proxy  {
  
  /** the resources */
  protected final static Resources resources = EditView.resources;
  
  /** the panel we're working in */
  private JPanel panel;
  
  /** the proxied property */
  protected Property property;
  
  /** the edit view */
  protected EditView view;
  
  /** the label header */
  protected JLabel label;
  
  /** buttons */
  protected AbstractButton ok, cancel;
  
  /** change support */
  protected ObservableBoolean change = new ObservableBoolean();  

  /**
   * Setup an editor in given panel
   */
  protected final void start(JPanel setPanel, Property setProp, EditView setView) {

    panel = setPanel;

    // setup pane
    panel.removeAll();
    panel.setLayout(new BorderLayout());
    
    // remember property
    property = setProp;
    view = setView;
    
    // add a header with a label
    label = new JLabel(property.getTag() + " - "+ Gedcom.getName(property.getTag()), property.getImage(true), SwingConstants.LEFT);
    panel.add(BorderLayout.NORTH, label);

    // add the specifics        
    Editor editor = getEditor();
    panel.add(BorderLayout.CENTER, editor);
    
    // add buttons
    if (isEditable()) {
      JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      ButtonHelper bh = new ButtonHelper().setInsets(0).setContainer(buttons).setFocusable(false);
      ok = bh.create(new OK());
      cancel = bh.create(new Cancel());
      panel.add(BorderLayout.SOUTH, buttons);
      
      change.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          ok.setEnabled(change.get());
          cancel.setEnabled(change.get());
        }
      });
      
    }
    
    // propagate layout change
    panel.validate();
    panel.doLayout();

    // set focus
    editor.requestFocus();

    // we're ready for changes now
    change.set(false);
    

    // done
  }
  
  /**
   * Implementation
   */
  protected abstract Editor getEditor();
  
  /**
   * Returns change state of proxy
   */
  protected final boolean hasChanged() {
    // only if property is still in entity/gedcom and change state is true
    return property.getGedcom()!=null & change.get();
  }

  /**
   * Commit any changes made by the user
   */
  protected abstract void commit();
  
  /**
   * Editable? default is yes
   */
  protected boolean isEditable() {
    return true;
  }
  
  /**
   * A preview component using EntityRenderer for an entity
   */
  protected class Preview extends JComponent {
    /** entity */
    private Entity entity;
    /** the blueprint renderer we're using */
    private EntityRenderer renderer;
    /**
     * Constructor
     */
    protected Preview(Entity ent) {
      // remember
      entity = ent;
      setBorder(new EmptyBorder(4,4,4,4));
      // done
    }
    /**
     * @see genj.edit.ProxyXRef.Content#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g) {
      Insets insets = getInsets();
      Rectangle box = new Rectangle(insets.left,insets.top,getWidth()-insets.left-insets.right,getHeight()-insets.top-insets.bottom);     
      // clear background
      g.setColor(Color.white); //Color.WHITE is 1.4 only
      g.fillRect(box.x, box.y, box.width, box.height);
      // render entity
      if (renderer==null) 
        renderer = new EntityRenderer(view.manager.getBlueprintManager().getBlueprint(entity.getTag(), ""));
      renderer.render(g, entity, box);
      // done
    }
  } //Content

  /**
   * An editor with focussable component
   */
  protected class Editor extends JComponent {
    
    /** the focus */
    private JComponent focus;
    
    /** set box layout */
    protected void setBoxLayout() {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }
    
    /** set focus */
    protected void setFocus(JComponent c) {
      focus = c;
    }

    /** overridden requestFocus() */
    public void requestFocus() {
      JComponent c = focus!=null ? focus : this;
      try {
        //setFocusCycleRoot(true);
        c.requestFocusInWindow();
      } catch (Throwable t) {
        c.requestFocus();
      }
    }

  } // Editor

  /**
   * A ok action
   */
  private class OK extends ActionDelegate {

    /** constructor */
    private OK() {
      setText(WindowManager.OPTION_OK);
    }

    /** cancel current proxy */
    protected void execute() {

      // any changes?        
      if (!hasChanged())
        return;
        
      Gedcom gedcom = property.getGedcom();
  
      try {
        gedcom.startTransaction();
        commit();
      } finally {
        gedcom.endTransaction();
      }

      change.set(false);    
    }

  } //OK

  /**
   * A cancel action
   */
  private class Cancel extends ActionDelegate {

    /** constructor */
    private Cancel() {
      setText(WindowManager.OPTION_CANCEL);
    }

    /** cancel current proxy */
    protected void execute() {
      start(panel, property, view);
    }

  } //Cancel

} //Proxy
