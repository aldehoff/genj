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

import genj.gedcom.Gedcom;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A manager for our blueprints */
public class BlueprintManager {

  private final static String BLUEPRINT="blueprint";

  /** blueprints per entity */
  private List[] blueprints = new List[Gedcom.NUM_TYPES];

  /** singleton */
  private static BlueprintManager instance;
  
  /**
   * Singleton access   */
  public static BlueprintManager getInstance() {
    if (instance==null) instance = new BlueprintManager();
    return instance;
  }
  
  /**
   * Constructor   */
  private BlueprintManager() {
    
    try {
      XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
      reader.setContentHandler(new Handler());
      reader.parse(new InputSource(getClass().getResourceAsStream("blueprints.xml")));
    } catch (Throwable t) {
      t.printStackTrace();
    }
    
    // done
  }
  
  /**
   * Blueprint for given type with given name   */
  public Blueprint getBlueprint(int type, String name) {
    // look through blueprints for that type
    List bps = getBlueprints(type);
    for (int i=0; i<bps.size(); i++) {
      Blueprint bp = (Blueprint)bps.get(i);
      // .. found! return
      if (bp.getName().equals(name)) return bp;   	
    }
    // not found! create a dummy
    return new Blueprint(name, Gedcom.getNameFor(type, false));
  }
  
  /**
   * Blueprints for a given type   */
  public List getBlueprints(int type) {
    List result = blueprints[type];
    if (result==null) {
      result = new ArrayList(10);
      blueprints[type]=result;
    }
    return result;
  }
  
  /**
   * Type of given blueprint   */
  public int getType(Blueprint blueprint) {
    // look for it
    for (int i = 0; i < blueprints.length; i++) {
      if (blueprints[i].contains(blueprint)) return i;
    }
    // not found
    throw new IllegalArgumentException("Blueprint is not registered"); 
  }

  /**
   * XML document handler that knows how to get Blueprints   */
  private class Handler extends DefaultHandler {
    
    /** the type of the current blueprint */
    private int type;
    
    /** the name of the current blueprint */
    private String name;
    
    /** the buffer for gathering html */
    private StringBuffer html = new StringBuffer(256);
    
    /**
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      // blueprint?
      if (BLUEPRINT.equals(qName)) {
        name = attributes.getValue("name");
        type = Gedcom.getTypeFor(attributes.getValue("type"));
        html.setLength(0);
      }
      //done
    }
    /**
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
      // blueprint? or no current?
      if (!BLUEPRINT.equals(qName)||name==null) return;
      // create one
      getBlueprints(type).add(new Blueprint(name, html.toString()));
      name = null;
    }
    /**
     * @see org.xml.sax.helpers.DefaultHandler#characters(char, int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
      if (name==null) return;
      html.append(ch, start,length);
    }

  }; //BlueprintHandler
  
} //BlueprintManager
