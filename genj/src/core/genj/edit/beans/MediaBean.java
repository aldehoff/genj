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
package genj.edit.beans;

import genj.edit.Images;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyXRef;
import genj.io.InputSource;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.ScrollPaneWidget;
import genj.util.swing.ThumbnailWidget;
import genj.view.ContextProvider;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.FocusManager;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * A property bean for managing multimedia files (and blobs) associated with properties 
 */
public class MediaBean extends PropertyBean {
  
  private final static ImageIcon IMG_PREV = Images.imgBack, IMG_NEXT = Images.imgForward;
  private ThumbnailWidget thumbs = new ThumbnailWidget();
  private FocusListener focusChange = new FocusListener();
  private JButton unfocus = new JButton();
  private Property focus = null;
  private JToolBar tools = new JToolBar();
  
  /**
   * Constructor
   */
  public MediaBean() {
    setBorder(BorderFactory.createLoweredBevelBorder());
    setLayout(new BorderLayout());
    add(BorderLayout.NORTH, tools);
    add(BorderLayout.CENTER, new ScrollPaneWidget(thumbs));
    setPreferredSize(new Dimension(32,32));

    // prepare unfocus action
    unfocus.setFocusable(false); 
    tools.add(unfocus);
    
    // done
  }
  
  @Override
  public void addNotify() {
    super.addNotify();
    
    FocusManager.getCurrentManager().addPropertyChangeListener("focusOwner", focusChange);
  }
  
  @Override
  public void removeNotify() {
    FocusManager.getCurrentManager().removePropertyChangeListener("focusOwner", focusChange);
    
    super.removeNotify();
  }

  @Override
  protected void commitImpl(Property property) {
  }

  @Override
  protected void setPropertyImpl(Property prop) {
    setFocus(prop);
  }
  
  public void restoreFocus() {
    if (getProperty()==null)
      throw new IllegalArgumentException("restore w/o prop");
    setFocus(getProperty());
  }
  
  public void setFocus(Property prop) {

    // noop?
    if (focus==prop)
      return;
    
    // clear?
    if (prop==null) {
      thumbs.clear();
      return;
    }

    // valid?
    if (prop!=getProperty()&&prop.getParent()!=getProperty())
      throw new IllegalArgumentException("prop not sub");
    
    focus = prop;
    
    // update focus indicator
    if (focus!=getProperty()) {
      unfocus.setVisible(true);
      unfocus.setAction(new Unfocus(focus));
      add(BorderLayout.NORTH, unfocus);
    } else {
      unfocus.setVisible(false);
    }
    tools.revalidate();
    tools.repaint();
    
    // find all contained medias
    List<InputSource> files = new ArrayList<InputSource>();
    
    for (PropertyFile file : focus.getProperties(PropertyFile.class)) {
      if (file.getFile()!=null)
        files.add(InputSource.get(file.getFile()));
    }
    
    // find all referenced medias
    for (PropertyXRef ref : focus.getProperties(PropertyXRef.class)) {
      Entity entity = ref.getTargetEntity();
      if (entity instanceof Media) {
        PropertyFile file = ((Media)entity).getFile();
        if (file.getFile()!=null)
          files.add(InputSource.get(file.getFile()));
      }
    }
    
    thumbs.setSources(files);
    
  }
  
  private class FocusListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      Property newFocus = getFocus((Component)evt.getNewValue());
      if (newFocus==null)
        restoreFocus();
      else
        setFocus(newFocus);
    }
    
    private Property getFocus(Component c) {
      // new focus?
      if (c==null)
        return null;
      // in sibling to this bean?
      if (!DialogHelper.isContained(c, getParent().getParent()))
        return null;
      // getting a context?
      Context context = new ContextProvider.Lookup(c).getContext();
      if (context==null)
        return null;
      // resolving to a single property?
      if (context.getProperties().size()!=1) 
        return null;
      // context a sub-property of current root?
      Property prop = context.getProperty();
      while (true) {
        if (prop==null) 
          return null;
        Property parent = prop.getParent();
        if (parent==getProperty())
          break;
        prop = parent;
      }
      // property allows OBJE?
      if (!prop.getMetaProperty().allows("OBJE")) 
        return null;
      // got it
      return prop;
    }
  }
  
  private class Unfocus extends Action2 {
    public Unfocus(Property focus) {
      setText(focus.getPropertyName());
      setImage(focus.getImage(false));
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      restoreFocus();
    }
  }
}
