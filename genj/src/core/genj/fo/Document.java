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
import genj.util.ImageSniffer;
import genj.util.Resources;

import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
  
  private final static Resources RESOURCES = Resources.get(Document.class);
  
  /** matching a=b,c-d=e,f:g=h,x=y(m,n,o),z=1 */
  protected static Pattern REGEX_ATTR = Pattern.compile("([^,]+)=([^,\\(]*(\\(.*?\\))?)");
  
  /** xsl fo namespace URI */
  private final static String 
    NS_XSLFO = "http://www.w3.org/1999/XSL/Format",
    NS_GENJ = "http://genj.sourceforge.net/XSL/Format";
  
  private org.w3c.dom.Document doc;
  private Element cursor;
  private String title;
  private boolean needsTOC = false;
  private Map file2elements = new HashMap();
  private List sections = new ArrayList();
  private String formatSection = "font-size=larger,font-weight=bold,space-before=0.5cm,space-after=0.2cm,keep-with-next.within-page=always";
  private Map index2primary2secondary2elements = new TreeMap();
  private int idSequence = 0;
  private boolean containsCSV = false;
  
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
    //    <title>
    //    <flow>
    //     <block/>
    //    </flow>
    //   </page-sequence>
    cursor = (Element)doc.appendChild(doc.createElementNS(NS_XSLFO, "root")); 
    cursor.setAttribute("xmlns", NS_XSLFO);
    cursor.setAttribute("xmlns:genj", NS_GENJ);
    
    // FOP crashes when a title element is present so we use an extension to pass it to our fo2html stylesheet
    // @see http://issues.apache.org/bugzilla/show_bug.cgi?id=38710
    cursor.setAttributeNS(NS_GENJ, "genj:title", title);
    
    push("layout-master-set");
    push("simple-page-master", "master-name=master,margin-top=1cm,margin-bottom=1cm,margin-left=1cm,margin-right=1cm");
    push("region-body");
    pop().pop().pop().push("page-sequence","master-reference=master");

    // don't use title - see above
    // push("title").text(getTitle(), "").pop();
    
    push("flow", "flow-name=xsl-region-body");
    push("block");
    
    // done - cursor points to first block
  }
  
  /**
   * Check if there's any CSV in this document
   */
  protected boolean containsCSV() {
    return containsCSV;
  }
  
  /**
   * String representation - the title 
   */
  public String toString() {
    return getTitle();
  }
  
  /**
   * Closes the document finalizing output
   */
  protected void close() {
    
    // closed already?
    if (cursor==null)
      return;
    
    // generate indexes
    indexes();
    
    // generate TOC
    if (needsTOC) 
      toc();
    
    // done
    cursor = null;
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
   * Add Table of Content
   */
  public Document addTOC() {
    needsTOC = true;
    // done
    return this;
  }
  
  /**
   * Add section
   */
  public Document startSection(String title, String id) {
    
    // check if
    if (id.startsWith("_"))
      throw new IllegalArgumentException("underscore is reserved for internal IDs");
    
    // return to the last block in flow
    pop("flow", "addSection() is not applicable outside document flow");
    cursor = (Element)cursor.getLastChild();
    
    // generate an id if necessary
    if (id==null||id.length()==0)
      id = "section"+sections.size();
      
    // start a new block
    pop().push("block", formatSection + ",id="+id);
    
    // remember
    sections.add(cursor);
    
    // add the title
    addText(title);
    
    // create the following block
    nextParagraph();
    
    // done
    return this;
  }
    
  /**
   * Add section
   */
  public Document startSection(String title, Entity entity) {
    return startSection(title,entity.getTag()+"_"+entity.getId());
  }
    
  /**
   * Add section
   */
  public Document startSection(String title) {
    return startSection(title, "");
  }
    
  /**
   * Add an index entry
   */
  public Document addIndexTerm(String index, String primary) {
    return addIndexTerm(index, primary, "");
  }
  
  /**
   * Add an index entry
   */
  public Document addIndexTerm(String index, String primary, String secondary) {
    
    // check index
    if (index==null)
      throw new IllegalArgumentException("addIndexTerm() requires name of index");
    index = index.trim();
    if (index.length()==0)
      throw new IllegalArgumentException("addIndexTerm() name of index can't be empty");
    
    // check primary - ignore indexterm if empty
    primary = trimIndexTerm(primary);
    if (primary.length()==0)
      return this;
    
    // check secondary
    secondary = trimIndexTerm(secondary);
    
    // remember
    Map primary2secondary2elements = (Map)index2primary2secondary2elements.get(index);
    if (primary2secondary2elements==null) {
      primary2secondary2elements = new TreeMap();
      index2primary2secondary2elements.put(index, primary2secondary2elements);
    }
    Map secondary2elements = (Map)primary2secondary2elements.get(primary);
    if (secondary2elements==null) {
      secondary2elements = new TreeMap();
      primary2secondary2elements.put(primary, secondary2elements);
    }
    List elements = (List)secondary2elements.get(secondary);
    if (elements==null) {
      elements = new ArrayList();
      secondary2elements.put(secondary, elements);
    }
    
    // add anchor - normally that would be a fo:inline 
    //   push("inline", "id=_"+(++idSequence));
    // but FOP doesn't support IDs on those elements
    // so instead we attach an id to the surrounding block
    String id = cursor.getAttribute("id");
    if (id.length()==0) {
      id = ""+(++idSequence);
      cursor.setAttribute("id", id);
    }
    
    // remember the element for primary+secondary if the element isn't in there already
    if (!elements.contains(cursor))
        elements.add(cursor);
    
    return this;
  }
  
  private String trimIndexTerm(String term) {
    // null?
    if (term==null) 
      return "";
    // remove anything after (
    int bracket = term.indexOf('(');
    if (bracket>=0) 
      term = term.substring(0,bracket);
    // remove anything after ,
    int comma = term.indexOf('(');
    if (comma>=0) 
      term = term.substring(0,comma);
    // trim
    return term.trim();
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
  public Document addText(String text, String atts) {
    text(text, atts);
    return this;
  }
    
  /**
   * Add image file reference to the document
   * @param file the file pointing to the image
   * @param atts fo attributes for the image
   */
  public Document addImage(File file, String atts) {
    
    // anything we care about?
    if (file==null||!file.exists())
      return this;

    // check dimension - let's not make this bigger than 1x1 inch
    Dimension2D dim = new ImageSniffer(file).getDimensionInInches();
    if (dim==null)
      return this;
    if (dim.getWidth()>dim.getHeight()) {
      if (dim.getWidth()>1) atts = "width=1in,content-width=scale-to-fit,"+atts; // can be overriden
    } else {
      if (dim.getHeight()>1) atts = "height=1in,content-height=scale-to-fit,"+atts; // can be overriden
    }
    
    //  <fo:external-graphic src="file"/> 
    push("external-graphic", "src="+file.getAbsolutePath()+","+atts);
    
    // remember file in case a formatter wants to resolve file location later
    List elements = (List)file2elements.get(file);
    if (elements==null) {
      elements = new ArrayList(3);
      file2elements.put(file, elements);
    }
    elements.add(cursor);
    
    // back to enclosing block
    pop();

    // add opportunity to line break
    addText(" ");
    
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
  public Document nextParagraph() {
    return nextParagraph("");
  } 
  
  /**
   * Add a paragraph
   */
  public Document nextParagraph(String format) {
    
    // start a new block if the current is not-empty
    if (cursor.getFirstChild()!=null)
      pop().push("block", format);
    else
      attributes(cursor, format);
    
    return this;
  }
    
  /**
   * Start a list
   */
  public Document startList() {
    return startList("");
  }
    
  /**
   * Start a list
   */
  public Document startList(String format) {
    
    //<list-block>
    pop();
    push("list-block", "provisional-distance-between-starts=10pt, provisional-label-separation=3pt,"+format);
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
    
    // are we in an empty block within list-item-body already?
    if (cursor.getNodeName().equals("block")&&cursor.getFirstChild()==null && cursor.getParentNode().getLocalName().equals("list-item-body"))
      return this;

    // pop up to list-block and add new
    Element last = cursor;
    pop("list-block", "nextListItem() is not applicable outside list block");
    push("list-item");
    push("list-item-label", "end-indent=label-end()");
    push("block");
    text("\u2219", ""); // &bullet; /u2219 works in JEditPane, &bull; \u2022 doesn't
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
    push("block","");
    
    return this;
  }
  
  /**
   * Start a table
   */
  public Document startTable() {
    return startTable("width=100%,border=0.5pt solid black");
  }
  
  public Document startTable(String atts) {

    // patch format - FOP only supports table-layout=fixed
    atts  = "table-layout=fixed,"+atts;
    
    //<table>
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
    push("table", atts);
    Element table = cursor;
    
    // mark as cvs if applicable - non fo namespace attributes won't be picked up by push() and attributes()
    if (atts.indexOf("genj:csv=true")>=0) {
      containsCSV = true;
      cursor.setAttributeNS(NS_GENJ, "genj:csv", "true");
    }
    
    // head/body & row
    if (atts.indexOf("genj:header=true")>=0) {
      push("table-header"); 
      push("table-row", "color=#ffffff,background-color=#c0c0c0,font-weight=bold");
    } else { 
      push("table-body");
      push("table-row");
    }
    
    // cell and done
    push("table-cell", "border="+table.getAttribute("border"));  
    push("block");
    
    return this;
  }
  
  /**
   * Add a column to the table (this is not necessary)
   */
  public Document addTableColumn(String atts) {
    
    //<table>
    // <table-column/>
    Element save = cursor;
    
    // find the enclosing table
    pop("table", "addTableColumn() is not applicable outside enclosing table");
    
    // find last table definition
    Node before = cursor.getFirstChild();
    while (before!=null && before.getNodeName().equals("table-column"))
      before = before.getNextSibling();
    
    push("table-column", atts, before);

    // done for now
    cursor = save;
    
    return this;
  }
  
  /**
   * Jump to next cell in table
   */
  public Document nextTableCell() {
    return nextTableCell("");
  }
  public Document nextTableCell(String atts) {
    
    // peek at current cell - stay with it IF
    //  + it's the first cell in the row 
    //  + the current cursor points at the first child (a block)
    //  + the block pointed by cursor is empty
    Element cell = peek("table-cell", "nextTableCell() is not applicable outside enclosing table");
    if (cell.getPreviousSibling()==null&&cursor==cell.getFirstChild()&&!cursor.hasChildNodes()) {
      attributes(cell, atts);
      // add empty content to block so another call to nextTableCell() willl actually move forward
      push("inline", "").pop(); 
      return this;
    }
    
    // peek at row
    Element row = peek("table-row", "nextTableCell() is not applicable outside enclosing table row");
    int cells = row.getElementsByTagName("table-cell").getLength();
    
    // peek at table - add new row if we have all columns already
    Element table = peek("table", "nextTableCell() is not applicable outside enclosing table");
    int cols = table.getElementsByTagName("table-column").getLength();
    if (cols>0&&cells==cols) 
      return nextTableRow();

    // pop to row
    pop("table-row", "nextTableCell() is not applicable outside enclosing table row");
    
    // 20060215 wanted to use border=inherit here but that would require table-row
    // and table-body to have border=inherit as well. table-body can't have a
    // border property in a table with border-collapse=separate (which is the only
    // model FOP supports).
    // So we're simply doing our own 'inherit' here :) Alternative would be to do
    // us border=from-nearest-specified-value() on each cell or border=inherit
    // on the table-columns and then border=from-table-column() on the cells.
    
    // add now
    push("table-cell", "border="+table.getAttribute("border")+","+atts);  
    push("block");

    // done 
    return this;
  }
  
  /**
   * Jump to next row in table
   */
  public Document nextTableRow() {
    return nextTableRow("");
  }
  public Document nextTableRow(String atts) {
    
    // peek at current cell - stay with it IF
    //  + it's the first cell in the row 
    //  + the current cursor points at the first child (a block)
    //  + the block pointed by cursor is empty
    Element cell = peek("table-cell", "nextTableRow() is not applicable outside enclosing table");
    if (cell.getPreviousSibling()==null&&cursor==cell.getFirstChild()&&!cursor.hasChildNodes()) {
      attributes((Element)cell.getParentNode(), atts);
      return this;
    }
    
    // pop to table
    pop("table", "nextTableRow() is not applicable outside enclosing table");
    Element table = cursor;
    
    // last child is already table-body?
    if ( table.getLastChild().getNodeName().equals("table-body") ) {
      cursor = (Element)table.getLastChild();
    } else {
      push("table-body");
    }
    
    // add row
    push("table-row", atts);
    
    // add cell
    push("table-cell", "border="+table.getAttribute("border"));  
    push("block");
    
    // done
    return this;
  }
  
  /**
   * Jump to next cell in table
   */
  public Document endTable() {
    
    // leave table
    pop("table", "endTable() is not applicable outside enclosing table").pop();
    
    // done
    return this;
  }
  
  /**
   * Force a page break
   */
  public Document nextPage() {
    pop();
    push("block", "page-break-before=always");
    return this;
  }
  
  /**
   * Add an anchor
   */
  public Document addAnchor(String id) {
    if (id.startsWith("_"))
      throw new IllegalArgumentException("underscore is reserved for internal IDs");
    push("block", "id="+id);
    pop();
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
    
    // <basic-link>text</basic-link>
    push("basic-link", "internal-destination="+id);
    text(text, "");
    pop();
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
  
  /**
   * Add indexes
   */
  private Document indexes() {
    
    // loop over indexes
    for (Iterator indexes = index2primary2secondary2elements.keySet().iterator(); indexes.hasNext(); ) {
      
      String index = (String)indexes.next();
      Map primary2secondary2elements = (Map)index2primary2secondary2elements.get(index);
      
      // add section
      startSection(index);
      push("block", "start-indent=1cm");
      
      // loop over primaries
      for (Iterator primaries = primary2secondary2elements.keySet().iterator(); primaries.hasNext(); ) {
        
        String primary = (String)primaries.next();
        Map secondary2elements = (Map)primary2secondary2elements.get(primary);
        
        // add block and primary
        push("block", "");
        text(primary+" ", "");

        // loop over secondaries
        for (Iterator secondaries = secondary2elements.keySet().iterator(); secondaries.hasNext(); ) {
          
          String secondary = (String)secondaries.next();
          List elements = (List)secondary2elements.get(secondary);
          
          if (secondary.length()>0) {
            push("block", "start-indent=2cm"); //start-indent?
            text(secondary+" ", "");
          }
          
          // loop over elements
          for (int e=0;e<elements.size();e++) {
            if (e>0) text(", ", "");
            Element element = (Element)elements.get(e);
            String id = element.getAttribute("id");
            
            push("basic-link", "internal-destination="+id);
            push("page-number-citation", "ref-id="+id);
            cursor.setAttributeNS(NS_GENJ, "genj:citation", Integer.toString(e+1));
            pop();
            pop();
          }
          
          if (secondary.length()>0)
            pop();
          // next
        }
        
        // next
        pop();
      }

      // next
      pop();
    }

    
    // done
    return this;
  }
  
  /**
   * Add Table of content 
   */
  private Document toc() {
    
    // anything to do?
    if (sections.isEmpty())
      return this;
    Element old = cursor;
    
    // pop back to flow
    pop("flow", "can't create TOC without enclosing flow");
    
    // add block for toc AS FIRST child
    push("block", "", cursor.getFirstChild());
    
    //<block>
    //  Table of Contents
    //  <block>
    //    Title 1<leader/><page-number-citation/>
    //   </block>
    //   ...
    //</block>
    
    // add toc header
    push("block", formatSection);
    text(RESOURCES.getString("toc"), "");
    pop();

    // add toc entries
    for (int i=0;i<sections.size();i++) {
      push("block", "start-indent=1cm,end-indent=1cm,text-indent=0cm,text-align-last=justify,text-align=justify");
      Element section = (Element)sections.get(i);
      String id = section.getAttribute("id");
      String txt = ((Text)section.getFirstChild()).getData();
      addLink(txt, id);
      push("leader", "leader-pattern=dots").pop();
      push("page-number-citation", "ref-id="+id).pop();

      pop();
    }
    
    // done
    cursor = old;
    return this;
  }
  
  /**
   * Add qualified element to parent
   */
  private Document push(String name) {
    return push(name, "");
  }
  
  /**
   * Add qualified element to parent
   */
  private Document push(String name, String attributes) {
    return push(name, attributes, null);
  }
  
  /**
   * Add qualified element to parent
   */
  private Document push(String name, String attributes, Node before) {
    // create it, set attributes and hook it up
    Element elem = doc.createElementNS(NS_XSLFO, name);
    if (before!=null)
      cursor.insertBefore(elem, before);
    else
      cursor.appendChild(elem);
    cursor =  elem;
    // attribute it and done
    return attributes(elem, attributes);
  }
  
  /**
   * Set attributes on current element
   */
  private Document attributes(Element elem, String attributes) {
    // parse attributes
    Matcher m = REGEX_ATTR.matcher(attributes);
    while (m.find()) {
      // accept only fo attributes here
      String key = m.group(1).trim();
      if (key.indexOf(':')<0) {
        String val = m.group(2).trim();
        elem.setAttribute(key, val);
      }
    }
    // done
    return this;
  }
  
  /**
   * Add text element
   */
  private Document text(String text, String atts) {
    
    // ignore empties
    if (text.length()==0)
      return this;

    // create a text node for it
    Node txt = doc.createTextNode(text);
    if (atts.length()>0) {
      push("inline", atts);
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
    Node loop = cursor;
    while (loop instanceof Element) {
      if (loop.getLocalName().equals(qname)) 
        return (Element)loop;
      loop = loop.getParentNode();
    }
    throw new IllegalArgumentException(error);
  }
 
  public static void main(String[] args) {
    
    try {
    
      Document doc = new Document("Testing FO");
      
      doc.addText("A paragraph");
      doc.nextParagraph("start-indent=10pt");
      doc.addText("The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. ");
      doc.startList("start-indent=20pt");
      doc.addText("A");
      doc.nextListItem();
      doc.addText("B");
      doc.nextListItem();
      doc.addText("C");
      
//      doc.addTOC();
//      doc.startSection("Section 1");
//      doc.addText("here comes a ").addText("table", "font-weight=bold, color=rgb(255,0,0)").addText(" for you:");
//      doc.addImage(new File("C:/Documents and Settings/Nils/My Documents/Java/Workspace/GenJ/gedcom/meiern.jpg"), "vertical-align=middle");
//      doc.addImage(new File("C:/Documents and Settings/Nils/My Documents/My Pictures/usamap.gif"), "vertical-align=middle");
//      
//      doc.startTable("width=100%,border=0.5pt solid black,genj:csv=true");
//      doc.addTableColumn("column-width=10%");
//      doc.addTableColumn("column-width=10%");
//      doc.addTableColumn("column-width=80%");
//      doc.nextTableCell("color=red");
//      doc.addText("AA");
//      doc.nextTableCell();
//      doc.addText("AB");
//      doc.nextTableCell();
//      //doc.addText("AC");
//      doc.nextTableCell();
//      doc.addText("BA"); // next row
//      doc.nextTableCell("number-columns-spanned=2");
//      doc.addText("BB+BC");
//      doc.nextTableRow();
//      doc.addText("CA");
//      doc.nextTableCell();
//      doc.addText("CB");
//      doc.nextTableCell();
//      doc.addText("CC");
//      doc.endTable();
//  
//      doc.startList();
//      doc.nextListItem();
//      doc.addText("Item 1");
//      doc.addText(" with text talking about");
//      doc.addIndexTerm("Animals", "Mammals");
//      doc.addText(" elephants and ");
//      doc.addIndexTerm("Animals", "Mammals", "Horse");
//      doc.addText(" horses as well as ");
//      doc.addIndexTerm("Animals", "Mammals", "Horse");
//      doc.addText(" ponys and even ");
//      doc.addIndexTerm("Animals", "Fish", "");
//      doc.addText(" fish");
//      doc.nextParagraph();
//      doc.addText("and a newline");
//      doc.nextListItem();
//      doc.addText("Item 2");
//      doc.startList();
//      doc.addText("Item 2.1");
//      doc.nextListItem();
//      doc.addText("Item 2.2");
//      doc.endList();
//      doc.endList();
//      doc.addText("Text");
//  
//      doc.startSection("Section 2");
//      doc.addText("Text and a page break");
//      doc.nextPage();
//      
//      doc.startSection("Section 2");
//      doc.addText("Text");

      Format format;
      if (args.length>0)
        format = Format.getFormat(args[0]);
      else 
        format = new PDFFormat();

      File file = null;
      String ext = format.getFileExtension();
      if (ext!=null) {
        file = new File("c:/temp/foo."+ext);
      }
      format.format(doc, file);

      if (file!=null)
        Runtime.getRuntime().exec("c:/Program Files/Internet Explorer/iexplore.exe \""+file.getAbsolutePath()+"\"");

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
}
