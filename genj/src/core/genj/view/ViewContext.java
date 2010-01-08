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
package genj.view;

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.util.swing.Action2;
import genj.util.swing.ImageIcon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A context represents a 'current context in Gedcom terms', a gedcom
 * an entity and a property
 */  
public class ViewContext extends Context implements Comparable<ViewContext> {
  
  private List<Action2> actions = new ArrayList<Action2>();
  private ImageIcon img = null;
  private String txt = null;
  
  /**
   * Constructor
   */
  public ViewContext(String text, Context context) {
    super(context);
    setText(text);
  }
  
  /**
   * Constructor
   */
  public ViewContext(String text, ImageIcon img, Context context) {
    super(context);
    setText(text);
    setImage(img);
  }
  
  /**
   * Constructor
   */
  public ViewContext(Context context) {
    super(context);
  }
  
  /**
   * Constructor
   */
  public ViewContext(Gedcom gedcom, List<Entity> entities, List<Property> properties) {
    super(gedcom, entities, properties);
  }
  
  /**
   * Constructor
   */
  public ViewContext(Gedcom ged) {
    super(ged);
  }
  
  /**
   * Constructor
   */
  public ViewContext(Property prop) {
    super(prop);
  }
  
  /**
   * Constructor
   */
  public ViewContext(Entity entity) {
    super(entity);
  }
  
  /**
   * Add an action
   */
  public ViewContext addAction(Action2 action) {
    actions.add(action);
    return this;
  }
  
  /**
   * Add actions
   */
  public ViewContext addActions(Action2.Group group) {
    actions.add(group);
    return this;
  }
  
  /**
   * Access to actions
   */
  public List<Action2> getActions() {
    return Collections.unmodifiableList(actions);
  }
  
  /**
   * Accessor
   */
  public String getText() {

    if (txt!=null)
      return txt;

    List<? extends Property> ps = getProperties();
    List<? extends Entity> es = getEntities();
    if (ps.size()==1) 
      txt = Gedcom.getName(ps.get(0).getTag()) + "/" + ps.get(0).getEntity();
    else if (!ps.isEmpty())
      txt = Property.getPropertyNames(ps, 5);
    else  if (es.size()==1)
      txt = es.get(0).toString();
    else if (!es.isEmpty())
      txt = Entity.getPropertyNames(es, 5);
    else txt = getGedcom().getName();

    return txt;
  }

  /**
   * Accessor
   */
  public ViewContext setText(String text) {
    txt = text;
    return this;
  }

  /**
   * Accessor
   */
  public ImageIcon getImage() {
    // an override?
    if (img!=null)
      return img;
    // check prop/entity/gedcom
    if (getProperties().size()==1)
      img = getProperties().get(0).getImage(false);
    else if (getEntities().size()==1)
      img = getEntities().get(0).getImage(false);
    else img = Gedcom.getImage();
    return img;
  }

  /**
   * Accessor
   */
  public ViewContext setImage(ImageIcon set) {
    img = set;
    return this;
  }

  /** comparison  */
  public int compareTo(ViewContext that) {
    if (this.txt==null)
      return -1;
    if (that.txt==null)
      return 1;
    return this.txt.compareTo(that.txt);
  }

  /**
   * marker list
   */
  public static class ContextList extends ArrayList<ViewContext> {
    
    private String title;
    private Gedcom gedcom;
    
    public ContextList(Gedcom gedcom, String title) {
      this.gedcom = gedcom;
      this.title = title;
    }
    
    public ContextList(Gedcom gedcom, String title, List<ViewContext> items) {
      this(gedcom, title);
      addAll(items);
    }
    
    @Override
    public void add(int index, ViewContext c) {
      if (c.getGedcom()!=gedcom)
        throw new IllegalArgumentException(gedcom+"!="+c.getGedcom());
      super.add(index, c);
    }
    
    @Override
    public boolean add(ViewContext c) {
      if (c.getGedcom()!=gedcom)
        throw new IllegalArgumentException(gedcom+"!="+c.getGedcom());
      return super.add(c);
    }
    
    public String getTitle() {
      return title;
    }

    public Gedcom getGedcom() {
      return gedcom;
    }
  }
  
} //Context
