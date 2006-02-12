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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An abstract layer above docbook handling and transformations
 *
 */
public class Document {
  
  private org.w3c.dom.Document doc;
  private Element cursor;
  private String title;
  private Set anchorNodes = new HashSet();
  private Map unresolvedID2textNodes = new HashMap();
  private boolean isTOC = true;
  private Map file2elements = new HashMap();
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
    //  <root>
    //   <layout-master-set>
    //    <simple-page-master>
    //     <region-body/>
    //    </simple-page-master>
    //   </layout-master-set>
    //   <page-sequence>
    //    <flow>
    //     <block/>
    //    </flow>
    //   </page-sequence>
    cursor = (Element)doc.appendChild(doc.createElementNS(NSURI, "root")); // "xmlns:fo="+NSURI ?
    push("layout-master-set");
    push("simple-page-master", "master-name=master,margin-top=1cm,margin-bottom=1cm,margin-left=1cm,margin-right=1cm");
    push("region-body");
    pop().pop().pop().push("page-sequence","master-reference=master");
    push("flow", "flow-name=xsl-region-body");
    push("block");
    
    // done - cursor points to first block
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
   * Add section
   */
  public Document addSection(String title, String id) {
    
    // return to the last block in flow
    pop("flow", "addSection() is not applicable outside document flow");
    cursor = (Element)cursor.getLastChild();
      
    // start a new block
    pop().push("block", "font-size=larger,font-weight=bold");
    
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
    return addText(text, "");
  }
  
  /**
   * Add text with given CSS styling
   * @see http://www.w3.org/TR/REC-CSS2/fonts.html#font-styling
   */
  public Document addText(String text, String format) {
    text(text, format);
    return this;
  }
    
  /**
   * Add image file reference to the document
   * @param file the file pointing to the image
   * @param format 
   */
  public Document addImage(File file, String format) {
    
    // anything we care about?
    if (file==null||!file.exists())
      return this;

    //  <fo:external-graphic src="file"/> 
    push("external-graphic", "src="+file.getAbsolutePath()+","+format);
    
    // remember file in case a formatter wants to resolve file location later
    List elements = (List)file2elements.get(file);
    if (elements==null) {
      elements = new ArrayList(3);
      file2elements.put(file, elements);
    }
    elements.add(cursor);
    
    // back to enclosing block
    pop();
    
    // done
    return this;
  }
  
  
  /**
   * Access to external image files
   */
  protected File[] getImages() {
    Set files = file2elements.keySet();
    return (File[])files.toArray(new File[files.size()]);
  }
  
  /**
   * Replace referenced image file with a calculated value
   */
  protected void setImage(File file, String value) {
    List nodes = (List)file2elements.get(file);
    for (int i = 0; i < nodes.size(); i++) {
      Element external = (Element)nodes.get(i);
      external.setAttribute("src", value);
    }
  }
  
  /**
   * Add a paragraph
   */
  public Document addParagraph() {
    
    // start a new block if the current is not-empty
    if (cursor.getFirstChild()!=null)
      pop().push("block", "");
    
    return this;
  }
    
  /**
   * Start a list
   */
  public Document startList() {
    
    //<list-block>
    push("list-block", "provisional-distance-between-starts=10pt, provisional-label-separation=3pt");
    nextListItem();
    
    return this;
  }
    
  /**
   * Add a list item
   */
  public Document nextListItem() {
    
    //<list-block>
    //  <list-item>
    //    <list-item-label end-indent="label-end()"><block>&#x2022;</block></list-item-label>
    //    <list-item-body start-indent="body-start()">
    //       <block/>
    //    </list-item-body>
    //  </list-item>
    
    // are we in an empty list-item-body list already?
    if (cursor.getFirstChild()==null && cursor.getParentNode().getLocalName().equals("list-item-body"))
      return this;

    // pop up to list-block and add new
    Element last = cursor;
    pop("list-block", "nextListItem() is not applicable outside list block");
    push("list-item");
    push("list-item-label", "end-indent=label-end()");
    push("block");
    text("\u2022", "");
    pop().pop().push("list-item-body", "start-indent=body-start()");
    push("block");

    return this;
  }
    
  /**
   * End a list
   */
  public Document endList() {

    // *
    //  <list-block>
    pop("list-block", "endList() is not applicable outside list-block").pop();
    
    return this;
  }
    
  /**
   * Start a table
   */
  public Document startTable(String columns, boolean header, boolean border) {
    
    StringTokenizer cols = new StringTokenizer(columns, ",", false);
    if (cols.countTokens()==0) cols = new StringTokenizer("25%,25%,25%,25%", ",", false);
    
    //<table>
    // <table-column/>
    // <table-header>
    //  <table-row>
    //   <table-cell>
    //    <block>
    //    ...
    // </table-header>
    // <table-body>
    //  <table-row>
    //   <table-cell>
    //    <block>    
    //    ...
    String atts = "table-layout=fixed,width=100%";
    if (border) atts+=",border=0.5pt solid black";
    push("table", atts);
    while (cols.hasMoreTokens()) {
      String w = cols.nextToken(); 
      push("table-column", "column-width="+w).pop();
    }
    if (header) {
      push("table-header"); 
      push("table-row", "color=#ffffff,background-color=#c0c0c0,font-weight=bold");
    } else { 
      push("table-body");
      push("table-row");
    }
    
    // cell and done
    return nextTableCell();
  }
  
  /**
   * Jump to next cell in table
   */
  public Document nextTableCell() {
    
    // pop to row
    pop("table-row", "nextTableCell() is not applicable outside enclosing table row");
    int cells = cursor.getElementsByTagName("table-cell").getLength();
    
    // peek at table - add new row if we have all columns already
    Element table = peek("table", "nextTableCell() is not applicable outside enclosing table");
    if (cells==table.getElementsByTagName("table-column").getLength()) 
      return nextTableRow();

    // add now
    push("table-cell", "border="+table.getAttribute("border"));
    push("block");

    // done 
    return this;
  }
  
  /**
   * Jump to next row in table
   */
  public Document nextTableRow() {
    // pop to parent of row
    pop("table-row", "nextTableRow() is not applicable outside enclosing table row").pop();
    // leaving header now?
    if (cursor.getNodeName().equals("table-header")) 
      pop().push("table-body");
    // add row
    push("table-row");
    
    // cell and done
    return nextTableCell();
  }
  
  /**
   * Jump to next cell in table
   */
  public Document endTable() {
    
    // leave table
    pop("table", "endTable() is not applicable outside enclosing table").pop();
    push("block");
    
    // done
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
  
  /** matching a=b,c-d=e,f:g=h,x=y(m,n,o),z=1 */
  protected static Pattern REGEX_ATTR = Pattern.compile("([^,]+)=([^,\\(]+(\\(.*?\\))?)");
  
  /**
   * Add element qualified by qname to parent
   */
  private Document push(String path) {
    return push(path, "");
  }
  
  /**
   * Add element qualified by qname to parent
   */
  private Document push(String name, String attributes) {
    // create it, set attributes and hook it up
    Element elem = doc.createElementNS(NSURI, name);
    cursor.appendChild(elem);
    cursor =  elem;
    // parse attributes
    Matcher m = REGEX_ATTR.matcher(attributes);
    while (m.find()) {
      cursor.setAttribute(m.group(1).trim(), m.group(2).trim());
    }
    // done
    return this;
  }
  
  /**
   * Add text element
   */
  private Document text(String text, String format) {
    
    Node txt = doc.createTextNode(text);
    if (format.length()>0) {
      push("inline", format);
      cursor.appendChild(txt);
      pop();
    } else {
      cursor.appendChild(txt);
    }
    return this;
  }

  /**
   * pop element from stack
   */
  private Document pop() {
    cursor = (Element)cursor.getParentNode();
    return this;
  }
  
  /**
   * pop element from stack
   */
  private Document pop(String qname, String error) {
    cursor = peek(qname, error);
    return this;
  }
  
  /**
   * find element in current stack upwards
   */
  private Element peek(String qname, String error) {
    Element loop = cursor;
    while (loop!=null) {
      if (loop.getLocalName().equals(qname)) 
        return loop;
      loop = (Element)loop.getParentNode();
    }
    throw new IllegalArgumentException(error);
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
