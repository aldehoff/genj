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

import genj.gedcom.Indi;

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
  
  public final static String SUFFIX = ".xml";

  private org.w3c.dom.Document doc;
  private Node cursor;
  
  /**
   * Constructor
   */
  public Document(String title) {
    
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
  public Document addSection(String title, Indi indi) {
    return addSection(title, indi.getId());
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
  public Document addIndexEntry(String index, String primary, String secondary) {
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
  public Document addText(String text) {
    // make sure there's a paragraph
    if (!"para".equals(cursor.getNodeName())) 
      addParagraph();
    cursor.appendChild(textNode(text));
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
    push(anchorNode(id));
    return this;
  }
    
  /**
   * Add an anchor
   */
  public Document addAnchor(Indi indi) {
    return addAnchor(indi.getId());
  }
  
  /**
   * Add a link
   */
  public Document addLink(String text, String id) {
    // make sure there's a paragraph
    if (!"para".equals(cursor.getNodeName())) 
      addParagraph();
    // known anchor? create link
    if (id.indexOf(':')>0||anchors.contains(id)) 
      cursor.appendChild(linkNode(text, id));
    else {
      // remember a new text node for now
      Node node = cursor.appendChild(textNode(text));
      List unverified = (List)id2unverifiedLink.get(id);
      if (unverified==null) {
        unverified = new ArrayList();
        id2unverifiedLink.put(id, unverified);
      }
      unverified.add(node);
    }
    // done
    return this;
  }
  
  private Set anchors = new HashSet();
  private Map id2unverifiedLink = new HashMap();
  
  /**
   * Add a link
   */
  public Document addLink(String text, Indi indi) {
    addLink(text, indi.getId());
    return this;
  }
  
  /**
   * Add a link
   */
  public Document addLink(Indi indi) {
    return addLink(indi.toString(), indi);
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
    if (anchors.contains(id)) throw new IllegalArgumentException( "duplicate anchor id "+id);
    anchors.add(id);
    // add anchor node
    Element anchor = elementNode("anchor", null);
    anchor.setAttribute("id", id);
    // check for unverified links
    List unverified = (List)id2unverifiedLink.remove(id);
    if (unverified!=null) for (Iterator it=unverified.iterator();it.hasNext();) {
      Text text = (Text)it.next();
      text.getParentNode().replaceChild(linkNode(text.getData(), id), text);
    }
    return anchor;
  }
  
  private Element elementNode(String qname, String text) {
    Element elem = doc.createElement(qname);
    if (text!=null) elem.appendChild(textNode(text));
    return elem;
  }
  
  private Text textNode(String text) {
    return doc.createTextNode(text);
  }
  
  public static void main(String[] args) {
    try {
      Document db = new Document("Ancestors of Clair Milford Daniel");
      
      db.addText("This report was generated on Sun Jun 12 14:07:18 CEST 2005 with GenealogyJ - see ")
       .addLink("http://genj.sourceforge.net", "http://genj.sourceforge.net")
       .addText("!")
      
       .addSection("Generation 1", "foo")

       .addSection("Clair Milford Daniel", "I571")
       .addIndexEntry("names", "Daniel", "Clair Milford")
       .addText("Clair Milford Daniel, b. 10 Mar 1906 (child of ")
       .addLink("Elmer Snyder Daniel", "foo")
       .addText(" and ")
       .addLink("Alice Catherine Miller", "foo")
       .addText("), m. 13 Jun 1931 to Mary Regina Smith, d. Nov 1988. He graduated from Lebanon Valley College in Annville, Lebanon Co., PA. He was a high school teacher. He resided in Florence, Burlington Co., NJ. [Note: Graduated from Lebanon Valley College. Taught mathematics in the high school of Florence, New Jersey.]")
       
       .addParagraph()
       .addText("Parents:")
       .addList()
       .addListItem()
       .addText("Father")
       .addListItem()
       .addText("Mother")
      
       .endList()
      .addParagraph()
      .addText("more")
      
      .endSection()
      
      .addIndex("names", "Name Index")
      
      .addSection("Generation 2", "bar")
      .addText("outside now")
      
      .write(System.out);
    } catch (Throwable t) {
      t.printStackTrace();
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

  
}
