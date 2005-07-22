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
package genj.fo;

import genj.gedcom.Entity;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * An abstract layer above docbook handling and transformations
 *
 */
public class Document {
  
  public final static int
    TEXT_PLAIN = 0,
    TEXT_EMPHASIZED = 1;

  public final static int
    HALIGN_CENTER = 0,
    HALIGN_LEFT      = 1,
    HALIGN_RIGHT   = 2;

  private org.w3c.dom.Document doc;
  private Node cursor;
  private String title;
  private Set anchorNodes = new HashSet();
  private Map unresolvedID2textNodes = new HashMap();
  private Map file2imageNodes = new HashMap();
  private boolean isTOC = true;
  
  /**
   * Constructor
   */
  public Document(String title) {
    
    // remember title
    this.title = title;
    
    // create a dom document
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      doc = dbf.newDocumentBuilder().newDocument();
      
//      DOMImplementation dom = dbf.newDocumentBuilder().getDOMImplementation();
//      doc = dom.createDocument(null, "article", dom.createDocumentType("article", "-//OASIS//DTD DocBook V4.4//EN", "http://www.oasis-open.org/docbook/xml/4.4/docbookx.dtd"));
      
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
    
    // article boilerplate
    cursor = elementNode("article", null);
    cursor.appendChild(elementNode("title", title));
    
    doc.appendChild(cursor);

    // done
  }
  
  /**
   * String representation - the title 
   */
  public String toString() {
    return getTitle();
  }
  
  private Node push(Node elem) {
    cursor.appendChild(elem);
    cursor = elem;
    return elem;
  }
  
  private Node pop() {
    Node popd = cursor;
    cursor = popd.getParentNode();
    return popd;
  }
  
  private Node pop(String element) {
    while (true) {
      if (element.equals(cursor.getNodeName()))
        return pop();
      if (cursor.getParentNode()==doc)
        return null;
      pop();
    }
  }
  
  /**
   * Title access
   */
  public String getTitle() {
    return title;
  }
  
  /**
   * Access to DOM source
   */
  /*package*/ DOMSource getDOMSource() {
    return new DOMSource(doc);
  }
  
  /**
   * Access to referenced image files
   */
  /*package*/ File[] getImageFiles() {
    Set files = file2imageNodes.keySet();
    return (File[])files.toArray(new File[files.size()]);
  }
  
  /**
   * Replace referenced image file with a calculated value
   */
  /*package*/ void setImageFileRef(File file, String value) {
    List nodes = (List)file2imageNodes.remove(file);
    for (int i = 0; i < nodes.size(); i++) {
      Element imageNode = (Element)nodes.get(i);
      imageNode.setAttribute("fileref", value);
    }
  }
  
  /**
   * Add section
   */
  public Document addSection(String title, String id) {
    
    // pop to containing section
    Node parent = pop("section");
    if (parent!=null&&"section".equals(parent.getNodeName()))
      push(parent);
    push(sectionNode(title, id));
    
    // done
    return this;
  }
    
  /**
   * Add section
   */
  public Document addSection(String title, Entity ent) {
    return addSection(title, ent.getId());
  }
    
  /**
   * Add section
   */
  public Document addSection(String title) {
    return addSection(title, (String)null);
  }
    
  /**
   * Ends  a section
   */
  public Document endSection() {
    // pop to containing section
    if (pop("section")==null)
      throw new IllegalArgumentException("end section outside section");
    return this;
  }
  
  /**
   * Add an index entry
   */
  public Document addIndexTerm(String index, String primary, String secondary) {
    // check primary
    if (primary==null) 
      throw new IllegalArgumentException("index term without primary");
    if (primary.length()==0)
      return this;
    // add indexterm element
    Element entry = elementNode("indexterm", null);
    entry.setAttribute("type", index);
    entry.appendChild(elementNode("primary", primary));
    if (secondary!=null&&secondary.length()>0)
      entry.appendChild(elementNode("secondary", secondary));
    cursor.appendChild(entry);
    return this;
  }
    
  /**
   * Add an index
   * (need to set index.on.type for XSL)
   */
  public Document addIndex(String index, String title) {
    // go back to root element
    pop("");
    // add index element
    Element elem = elementNode("index", null);
    elem.setAttribute("type", index);
    elem.appendChild(elementNode("title", title));
    cursor.appendChild(elem);
    return this;
  }
  
  /**
   * Add text
   */
  public Document addText(String text, int format) {
    // make sure there's a paragraph
    if (!"para".equals(cursor.getNodeName())) 
      addParagraph();
    cursor.appendChild(textNode(text, format));
    return this;
  }
    
  /**
   * Add text
   */
  public Document addText(String text) {
    return addText(text, TEXT_PLAIN);
  }
  
  /**
   * Add image
   */
  public Document addImage(File file, int align) {
    // anything we care about?
    if (file==null||!file.exists())
      return this;
    // create imagedata node
    Element node = elementNode("imagedata", null);
    node.setAttribute("fileref", file.getAbsolutePath());
    switch (align) {
      case HALIGN_LEFT: 
        node.setAttribute("align", "left");
        break;
      case HALIGN_RIGHT: 
        node.setAttribute("align", "right");
        break;
    }
    //node.setAttribute("valign", "middle");
    cursor.appendChild(node);
    // remember
    List nodes = (List)file2imageNodes.get(file);
    if (nodes==null) {
      nodes = new ArrayList();
      file2imageNodes.put(file, nodes);
    }
    nodes.add(node);
    // done
    return this;
  }
  
  /**
   * Add a paragraph
   */
  public Document addParagraph() {
    // look for current paragraph
    if ("para".equals(cursor.getNodeName())) { 
      // one already there
      if (cursor.hasChildNodes()) {
        pop();
        push(elementNode("para", null));
      }
    } else {
      // can't do a paragraph if following a section
      Node prev = cursor.getLastChild();
      if (prev!=null && prev.getNodeName().equals("section"))
        throw new IllegalArgumentException("paragraph after /section n/a");
      // create a new paragraph
      push(elementNode("para", null));
    }
    return this;
  }
    
  /**
   * Add a list
   */
  public Document addList() {
    push(elementNode("itemizedlist", null));
    push(elementNode("listitem", null));
    push(elementNode("para", null));
    return this;
  }
    
  /**
   * End a list
   */
  public Document endList() {
    if (pop("itemizedlist")==null)
      throw new IllegalArgumentException("endList outside list");
    return this;
  }
    
  /**
   * Add a list item
   */
  public Document addListItem() {

    // grab last
    Node item = pop("listitem");
    if (item==null)
      throw new IllegalArgumentException("listitem without enclosing list");
    
    // still contains an empty paragraph?
    if (!item.getFirstChild().hasChildNodes()) {
      push(item);
      push(item.getFirstChild());
    } else {
      push(elementNode("listitem", null));
      push(elementNode("para", null));
    }
    return this;
  }
    
  /**
   * Add an anchor
   */
  public Document addAnchor(String id) {
    cursor.appendChild(anchorNode(id));
    return this;
  }
    
  /**
   * Add an anchor
   */
  public Document addAnchor(Entity entity) {
    return addAnchor(entity.getTag()+"_"+entity.getId());
  }
  
  /**
   * Add a link
   */
  public Document addLink(String text, String id) {
    // make sure there's a paragraph
    if (!"para".equals(cursor.getNodeName())) 
      addParagraph();
    // known anchor? create link
    if (id.indexOf(':')>0||anchorNodes.contains(id)) 
      cursor.appendChild(linkNode(text, id));
    else {
      // remember a new text node for now
      Node node = cursor.appendChild(textNode(text, TEXT_PLAIN));
      List unverified = (List)unresolvedID2textNodes.get(id);
      if (unverified==null) {
        unverified = new ArrayList();
        unresolvedID2textNodes.put(id, unverified);
      }
      unverified.add(node);
    }
    // done
    return this;
  }
  
  /**
   * Add a link
   */
  public Document addLink(String text, Entity entity) {
    addLink(text, entity.getTag()+"_"+entity.getId());
    return this;
  }
  
  /**
   * Add a link
   */
  public Document addLink(Entity entity) {
    return addLink(entity.toString(), entity);
  }
  
  private Element linkNode(String text, String id) {
    
    Element link;
    if (id.startsWith("http:")) {
      link = elementNode("ulink", text);
      link.setAttribute("url", id);
    } else {
      link = elementNode("link", text);
      link.setAttribute("linkend", id);
    }
    return link;
  }
  
  private Element sectionNode(String title, String id) {
    if (title==null) throw new IllegalArgumentException("section without title n/a");
    Element section = elementNode("section", null);
    section.appendChild(elementNode("title", title));
    if (id!=null&&id.length()>0) section.appendChild(anchorNode(id));
    return section;
  }
  
  private Element anchorNode(String id) {
    // already a known anchor?
    if (anchorNodes.contains(id)) throw new IllegalArgumentException( "duplicate anchor id "+id);
    anchorNodes.add(id);
    // add anchor node
    Element anchor = elementNode("anchor", null);
    anchor.setAttribute("id", id);
    // check for unverified links
    List unverified = (List)unresolvedID2textNodes.remove(id);
    if (unverified!=null) for (Iterator it=unverified.iterator();it.hasNext();) {
      Text text = (Text)it.next();
      text.getParentNode().replaceChild(linkNode(text.getData(), id), text);
    }
    return anchor;
  }
  
  private Element elementNode(String qname, String text) {
    Element elem = doc.createElement(qname);
    if (text!=null) elem.appendChild(textNode(text, TEXT_PLAIN));
    return elem;
  }
  
  private Node textNode(String text, int format) {
    switch (format) {
    case TEXT_PLAIN: default:
      return doc.createTextNode(text);
    case TEXT_EMPHASIZED:
      return elementNode("emphasis", text);
    }
  }
  
  /**
   * Write the content to stream
   */
  public void write(OutputStream out) throws IOException {
    write(new OutputStreamWriter(out, "UTF-8"));
  }
  
  /**
   * Write the content to stream
   */
  public void write(Writer out) throws IOException {
    try {
      Transformer t = TransformerFactory.newInstance().newTransformer();
      t.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//OASIS//DTD DocBook V4.4//EN");
      t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.oasis-open.org/docbook/xml/4.4/docbookx.dtd");
      t.transform(new DOMSource(doc), new StreamResult(out));
    } catch (TransformerException e) {
      throw new IOException(e.getMessage());
    }
  }
  
  /**
   * Accessor - whether a TOC is included
   */
  public boolean isTOC() {
    return isTOC;
  }
  
  /**
   * Accessor - whether a TOC is included
   */
  public void setTOC(boolean set) {
    isTOC = set;
  }
  
}
