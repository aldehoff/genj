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

import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.renderer.PropertyRenderer.RenderDate;
import genj.renderer.PropertyRenderer.RenderEntity;
import genj.renderer.PropertyRenderer.RenderMLE;
import genj.renderer.PropertyRenderer.RenderPlace;
import genj.renderer.PropertyRenderer.RenderSecret;
import genj.renderer.PropertyRenderer.RenderSex;
import genj.renderer.PropertyRenderer.RenderXRef;

/**
 * A factory for renderers
 */
/*package*/ class DefaultPropertyRendererFactory implements PropertyRendererFactory {

  /** cached renderer instances */
  private static PropertyRenderer[] renderers = new PropertyRenderer[]{
    new RenderSecret(),
    new MediaRenderer(),
    new RenderPlace(),
    new RenderMLE(),
    new RenderXRef(),
    new RenderDate(),
    new RenderSex(),
    new RenderEntity(),
    PropertyRenderer.DEFAULT
  };
  
  private static MediaRenderer MEDIA_RENDERER = new MediaRenderer();
  
  /**
   * Constructor
   */
  protected DefaultPropertyRendererFactory() {
  }

  /** 
   * factory
   */
  public PropertyRenderer getRenderer(Property root, TagPath propertyPath, Property property, String rendererType) {
    
    // try type
    if ("media".equals(rendererType))
      return MEDIA_RENDERER;
    
    // loop over known renderers
    for (int i=0;i<renderers.length;i++) {
      PropertyRenderer renderer = renderers[i];
      if (renderer.accepts(root, propertyPath, property))
        return renderer;
    }

    // this shouldn't happen since PropertyRenderer is in the list
    return PropertyRenderer.DEFAULT;
  }  
  
  
}
