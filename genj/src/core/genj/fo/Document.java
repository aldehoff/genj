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
  private Element root;
  private Element section;
  private Element paragraph;
  
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
    root = elementNode(doc, "article", null);
    elementNode(root, "title", title);
    
    // start with a paragraph
    paragraph = paragraphNode(root);

    // done
  }
  
  /**
   * Add section
   */
  public Document addSection(String title, String id) {
    section = sectionNode(section!=null  ? section : root, title, id);
    paragraph = paragraphNode(section);
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
    if (section==null)
      throw new IllegalArgumentException("end section outside section");
    Node parent = section.getParentNode();
    if ("section".equals(parent.getNodeName()))
        section = (Element)parent;
    else 
      section = null;
    paragraph = null;
    return this;
  }
    
  /**
   * Add text
   */
  public Document addText(String text) {
    if (paragraph==null) throw new IllegalArgumentException( "text not allowed here");
    textNode(paragraph, text);
    return this;
  }
    
  /**
   * Add a paragraph
   */
  public Document addParagraph() {
    if (paragraph==null) throw new IllegalArgumentException( "paragraph not allowed here");
    if (paragraph.hasChildNodes())
      paragraph = paragraphNode( paragraph.getParentNode() );
    return this;
  }
    
  /**
   * Add a list
   */
  public Document addList() {
    if (paragraph==null) throw new IllegalArgumentException( "list not allowed here");
    Element list = elementNode(paragraph.getParentNode(), "itemizedlist", null );
    Element item = elementNode(list, "listitem", null);
    paragraph = paragraphNode(item);
    return this;
  }
    
  /**
   * End a list
   */
  public Document endList() {
    Node listitem = paragraph.getParentNode();
    if (!"listitem".equals(listitem.getNodeName()))
      throw new IllegalArgumentException("endList outside list");
    Node list = listitem.getParentNode();
    paragraph = paragraphNode(list.getParentNode());
    return this;
  }
    
  /**
   * Add a list item
   */
  public Document addListItem() {
    Node listitem = paragraph.getParentNode();
    if (!"listitem".equals(listitem.getNodeName()))
      throw new IllegalArgumentException("listitem without enclosing list");
    if (!paragraph.hasChildNodes())
      return this;
    listitem = elementNode(listitem.getParentNode(), "listitem", null);
    paragraph = paragraphNode(listitem);
    return this;
  }
    
  /**
   * Add an anchor
   */
  public Document addAnchor(String id) {
    if (paragraph==null) throw new IllegalArgumentException( "anchor not allowed here");
    anchorNode(paragraph, id);
    // check for 
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
    if (paragraph==null) throw new IllegalArgumentException( "link not allowed here");
    // known anchor? create link
    if (id.indexOf(':')>0||anchors.contains(id)) 
      linkNode(paragraph, text, id);
    else {
      // create a text node for now 
      Text node = textNode(paragraph, text);
      // but remember it
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
  
  private Element paragraphNode(Node parent) {
    return elementNode( parent, "para", null);
  }
    
  private Element linkNode(Node parent, String text, String id) {
    assert parent!=null;
    assert text!=null;
    assert id!=null;
    
    Element link;
    if (id.startsWith("http:")) {
      link = elementNode(parent, "ulink", text);
      link.setAttribute("url", id);
    } else {
      link = elementNode(parent, "link", text);
      link.setAttribute("linkend", id);
    }
    return link;
  }
  
  private Element sectionNode(Node parent, String title, String id) {
    if (title==null) throw new IllegalArgumentException("section without title n/a");
    Element section = elementNode(parent, "section", null);
    elementNode(section, "title", title);
    if (id!=null&&id.length()>0) anchorNode(section, id);
    
    return section;
  }
  
  private Element anchorNode(Node parent, String id) {
    // already a known anchor?
    if (anchors.contains(id)) throw new IllegalArgumentException( "duplicate anchor id "+id);
    anchors.add(id);
    // add anchor node
    Element anchor = elementNode(parent, "anchor", null);
    anchor.setAttribute("id", id);
    // check for unverified links
    List unverified = (List)id2unverifiedLink.remove(id);
    if (unverified!=null) for (Iterator it=unverified.iterator();it.hasNext();) {
      Text text = (Text)it.next();
      text.getParentNode().replaceChild(linkNode(null, text.getData(), id), text);
    }
    return anchor;
  }
  
  private Element elementNode(Node parent, String qname, String text) {
    Element elem = doc.createElement(qname);
    if (text!=null) textNode(elem, text);
    if (parent!=null) parent.appendChild(elem);
    return elem;
  }
  
  private Text textNode(Node parent, String text) {
    Text node = doc.createTextNode(text);
    parent.appendChild(node);
    return node;
  }
  
  public static void main(String[] args) {
    try {
      Document db = new Document("Ancestors of Clair Milford Daniel");
      
      db.addText("This report was generated on Sun Jun 12 14:07:18 CEST 2005 with GenealogyJ - see ")
       .addLink("http://genj.sourceforge.net", "http://genj.sourceforge.net")
       .addText("!")
      
       .addSection("Generation 1", "foo")

       .addSection("Clair Milford Daniel", "I571")
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
