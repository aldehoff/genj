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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

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
  private Element block;
  private String title;
  private Set anchorNodes = new HashSet();
  private Map unresolvedID2textNodes = new HashMap();
  private Map file2imageNodes = new HashMap();
  private boolean isTOC = true;
  
  private final static String NSURI = "http://www.w3.org/1999/XSL/Format";
  
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
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
    
    // boilerplate
    Element root = addElement(doc, "root"); // "xmlns:fo="+NSURI ?
    
    Element layout_master_set = addElement(root, "layout-master-set");
    Element simple_page_master = addElement(layout_master_set, "simple-page-master", "master-name=master,margin-top=1cm,margin-bottom=1cm,margin-left=1cm,margin-right=1cm");
    addElement(simple_page_master, "region-body");

    Element page_sequence = addElement(root, "page-sequence","master-reference=master");
    
    Element flow = addElement(page_sequence, "flow", "flow-name=xsl-region-body");
    
    block = addElement(flow, "block");
    
    // done
  }
  
  /**
   * String representation - the title 
   */
  public String toString() {
    return getTitle();
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
    
    // return to the last block in flow
    Element flow = backtrack("flow", false);
    block = (Element)flow.getLastChild();
      
    // start a new block
    addParagraph("font-size=larger,font-weight=bold");
    
    // add the title
    addText(title);
    
    // create the following block
    addParagraph();
    
    if (id!=null&&id.length()>0) System.err.println("addSection("+title+","+id+") - id is not supported");
//    // pop to containing section
//    Element parent = pop("section");
//    if (parent!=null&&"section".equals(parent.getNodeName()))
//      push(parent);
//    push(sectionNode(title, id));
//    
//    
//    // done
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
   * Add an index entry
   */
  public Document addIndexTerm(String index, String primary, String secondary) {
    System.err.println("addIndexTerm("+index+","+primary+","+secondary+")");
//    // check primary
//    if (primary==null) 
//      throw new IllegalArgumentException("index term without primary");
//    if (primary.length()==0)
//      return this;
//    // add indexterm element
//    Element entry = createElement("indexterm", null);
//    entry.setAttribute("type", index);
//    entry.appendChild(createElement("primary", primary));
//    if (secondary!=null&&secondary.length()>0)
//      entry.appendChild(createElement("secondary", secondary));
//    cursor.appendChild(entry);
    return this;
  }
    
  /**
   * Add an index
   * (need to set index.on.type for XSL)
   */
  public Document addIndex(String index, String title) {
    System.err.println("addIndex("+index+","+title+")");
//    // go back to root element
//    pop("");
//    // add index element
//    Element elem = createElement("index", null);
//    elem.setAttribute("type", index);
//    elem.appendChild(createElement("title", title));
//    cursor.appendChild(elem);
    return this;
  }
  
  /**
   * Add text
   */
  public Document addText(String text) {
    return addText(text, TEXT_PLAIN);
  }
  
  /**
   * Add text
   */
  public Document addText(String text, int format) {
    addTextElement(block, text, format);
    return this;
  }
    
  /**
   * Add image
   */
  public Document addImage(File file, int align) {
    System.err.println("addImage("+file+","+align+")");
    
//    // anything we care about?
//    if (file==null||!file.exists())
//      return this;
//    // create imagedata node
//    Element node = createElement("imagedata", null);
//    node.setAttribute("fileref", file.getAbsolutePath());
//    switch (align) {
//      case HALIGN_LEFT: 
//        node.setAttribute("align", "left");
//        break;
//      case HALIGN_RIGHT: 
//        node.setAttribute("align", "right");
//        break;
//    }
//    //node.setAttribute("valign", "middle");
//    cursor.appendChild(node);
//    // remember
//    List nodes = (List)file2imageNodes.get(file);
//    if (nodes==null) {
//      nodes = new ArrayList();
//      file2imageNodes.put(file, nodes);
//    }
//    nodes.add(node);
    // done
    return this;
  }
  
  /**
   * Add a paragraph
   */
  public Document addParagraph() {
    return addParagraph("");
  }
  public Document addParagraph(String attributes) {
    
    // start a new block if the current is not-empty
    if (block.getFirstChild()!=null)
      block = addElement(block.getParentNode(), "block", attributes);
    else
      setAttributes(block, attributes);
    
    return this;
  }
    
  /**
   * Add a list
   */
  public Document startList() {
    
    //<list-block>
    block = addElement(block, "list-block", "provisional-distance-between-starts=10pt, provisional-label-separation=3pt");
    addListItem();
    
    return this;
  }
    
  /**
   * End a list
   */
  public Document endList() {

    // *
    //  <list-block>
    block = backtrack("list-block", true);
    return this;
  }
    
  /**
   * Add a list item
   */
  public Document addListItem() {
    
    //<list-block>
    //  <list-item>
    //    <list-item-label end-indent="label-end()"><block>&#x2022;</block></list-item-label>
    //    <list-item-body start-indent="body-start()">
    //       <block/>
    //    </list-item-body>
    //  </list-item>
    Element list_block = backtrack("list-block", false);
    
    // check if list-block has already a child (startList might have been called) AND the current block is still empty
    if (list_block.getFirstChild()!=null&&block.getFirstChild()==null)
      return this;
    
    Element list_item = addElement(list_block, "list-item");
    Element list_item_label = addElement(list_item, "list-item-label", "end-indent=label-end()");
    addTextElement(addElement(list_item_label, "block"), "\u2022", 0);
    Element list_item_body = addElement(list_item, "list-item-body", "start-indent=body-start()");
    block = addElement(list_item_body, "block");

    return this;
  }
    
  /**
   * Add an anchor
   */
  public Document addAnchor(String id) {
    System.err.println("addAnchor("+id+")");
    //cursor.appendChild(anchorNode(id));
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
    System.err.println("addLink("+text+","+id+")");
    
//    // make sure there's a paragraph
//    if (!"para".equals(cursor.getNodeName())) 
//      addParagraph();
//    // known anchor? create link
//    if (id.indexOf(':')>0||anchorNodes.contains(id)) 
//      cursor.appendChild(linkNode(text, id));
//    else {
//      // remember a new text node for now
//      Node node = addTextElement(cursor, text, TEXT_PLAIN);
//      List unverified = (List)unresolvedID2textNodes.get(id);
//      if (unverified==null) {
//        unverified = new ArrayList();
//        unresolvedID2textNodes.put(id, unverified);
//      }
//      unverified.add(node);
//    }
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
  
//  private Element linkNode(String text, String id) {
//    
//    Element link;
//    if (id.startsWith("http:")) {
//      link = createElement("ulink", text);
//      link.setAttribute("url", id);
//    } else {
//      link = createElement("link", text);
//      link.setAttribute("linkend", id);
//    }
//    return link;
//  }
//  
//  private Element sectionNode(String title, String id) {
//    if (title==null) throw new IllegalArgumentException("section without title n/a");
//    Element section = createElement("section", null);
//    section.appendChild(createElement("title", title));
//    if (id!=null&&id.length()>0) section.appendChild(anchorNode(id));
//    return section;
//  }
  
//  private Element anchorNode(String id) {
//    // already a known anchor?
//    if (anchorNodes.contains(id)) throw new IllegalArgumentException( "duplicate anchor id "+id);
//    anchorNodes.add(id);
//    // add anchor node
//    Element anchor = createElement("anchor", null);
//    anchor.setAttribute("id", id);
//    // check for unverified links
//    List unverified = (List)unresolvedID2textNodes.remove(id);
//    if (unverified!=null) for (Iterator it=unverified.iterator();it.hasNext();) {
//      Text text = (Text)it.next();
//      text.getParentNode().replaceChild(linkNode(text.getData(), id), text);
//    }
//    return anchor;
//  }
  
  /** matching a=b,c-d=e,f:g=h */
  private static Pattern REGEX_ATTR = Pattern.compile("([^, ]*)=([^, ]*)");
  
  /**
   * Add element qualified by qname to parent
   */
  private Element addElement(Node parent, String qname) {
    return addElement(parent, qname, "");
  }
  
  /**
   * Add element qualified by qname to parent
   */
  private Element addElement(Node parent, String qname, String attributes) {
    // create it, set attributes and hook it up
    Element elem = doc.createElementNS(NSURI, qname);
    setAttributes(elem, attributes);
    parent.appendChild(elem);
    // done
    return elem;
  }
  
  /**
   * Set element attributes
   */
  private void setAttributes(Element element, String attributes) {
    // parse attribues
    Matcher m = REGEX_ATTR.matcher(attributes);
    while (m.find()) {
      element.setAttribute(m.group(1), m.group(2));
    }
  }
  
  /**
   * Add text element
   */
  private Text addTextElement(Element parent, String text, int format) {
    
    if (format!=TEXT_PLAIN)
      System.err.println("only TEXT_PLAIN is supported");
    
    Text result  = doc.createTextNode(text);
    parent.appendChild(result);

    return result;
  }

  /**
   * find element in current stack upwards
   */
  private Element backtrack(String qname, boolean returnParent) {
    Element element = block;
    while (element!=null) {
      Element parent = (Element)element.getParentNode(); 
      if (element.getLocalName().equals(qname))
        return returnParent ? parent : element;
      element = parent;
    }
    throw new IllegalArgumentException();
  }
  
//private Element pop() {
//Element popd = cursor;
//cursor = (Element)popd.getParentNode();
//return popd;
//}
//
//private Element pop(String element) {
//while (true) {
//  if (element.equals(cursor.getNodeName()))
//    return pop();
//  if (cursor.getParentNode()==doc)
//    return null;
//  pop();
//}
//}

  
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
