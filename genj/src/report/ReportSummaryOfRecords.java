/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.fo.Document;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.report.Report;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ImageIcon;

/**
 * GenJ - Report
 * @author Nils Meier <nils@meiers.net>
 * @version 1.0
 */
public class ReportSummaryOfRecords extends Report {
  
  /** whether we're genering indexes for places */
  public  int generatePlaceIndex = 0;
  public String[] generatePlaceIndexs = {
    i18n("place.index.none"), i18n("place.index.one"), i18n("place.index.each")
  };
  
  /**
   * Overriden image - we're using the provided FO image 
   */
  protected ImageIcon getImage() {
    return Report.IMG_FO;
  }

  /**
   * While we generate information on stdout it's not really
   * necessary because we're returning a Document
   * that is handled by the UI anyways
   */
  public boolean usesStandardOut() {
    return false;
  }

  /**
   * The report's entry point
   */
  public void start(Gedcom gedcom) {
    
    // create a document
    Document doc = new Document(i18n("title", gedcom.getName()));

    doc.addText("This report shows information about all records in the Gedcom file "+gedcom.getName());
    
    // prepare some space to collect places
    Set placeTags = new HashSet();

    // Loop through individuals & families
    exportEntities(gedcom.getEntities(Gedcom.INDI, "INDI:NAME"), placeTags, doc);
    exportEntities(gedcom.getEntities(Gedcom.FAM, "FAM"), placeTags, doc);
    
    // add index for names and each place category
    doc.addIndex("names", "Name Index");
    
    if (generatePlaceIndex==1)
      doc.addIndex("places.*", "Place Index");
    if (generatePlaceIndex==2) {
      for (Iterator it=placeTags.iterator(); it.hasNext(); ) {
        String tag = (String)it.next();
        doc.addIndex("places."+tag, Gedcom.getName(tag)+ " - Place Index");
      }
    }
    
    // Done
    showDocumentToUser(doc);
    
  }

  /**
   * Exports the given entities 
   */
  private void exportEntities(Entity[] ents, Set placeTags, Document doc)  {
    for (int e = 0; e < ents.length; e++) {
      exportEntity(ents[e], placeTags, doc);
    }
  }
  
  /**
   * Exports the given entity 
   */
  private void exportEntity(Entity ent, Set placeTags, Document doc) {

    println(i18n("exporting", ent.toString() ));
      
    // start a new section
    doc.addSection( ent.toString() );
    
    // mark it
    doc.addAnchor(ent);
    
    // start a table for the entity
    doc.startTable("80%,20%", false, false);

    // export its properties
    exportProperties(ent, placeTags, doc);

    // add image in next column
    doc.nextTableCell();
    PropertyFile file = (PropertyFile)ent.getProperty(new TagPath("INDI:OBJE:FILE"));
    if (file!=null)
      doc.addImage(file.getFile(),"");
    
    // done
    doc.endTable();
  }    
  
  /**
   * Exports the given property's properties
   */
  private void exportProperties(Property of, Set placeTags, Document doc) {

    // anything to do?
    if (of.getNoOfProperties()==0)
      return;
    
    // create a list
    doc.startList();

    // an item per property
    for (int i=0;i<of.getNoOfProperties();i++) {
      
      Property prop = of.getProperty(i);

      // fill index while we're at it
      if (prop instanceof PropertyName) {
        PropertyName name = (PropertyName)prop;
        doc.addIndexTerm("names", name.getLastName(), name.getFirstName());
      }
      if (generatePlaceIndex>0&&prop.getTag().equals("PLAC")) {
        String tag = generatePlaceIndex==2  ? prop.getParent().getTag() : "*";
        doc.addIndexTerm("places."+tag, prop.getDisplayValue(), null);
        placeTags.add(tag);
      }

      // we don't do anything for xrefs to non-indi/fam
      if (prop instanceof PropertyXRef) {
        PropertyXRef xref = (PropertyXRef)prop;
        if (!(xref.getTargetEntity() instanceof Indi||xref.getTargetEntity() instanceof Fam))
          continue;
      }

      // here comes the item
      doc.nextListItem();
      doc.addText(Gedcom.getName(prop.getTag()), "font-style=italic");
      doc.addText(" ");
      
      // with its value
      exportPropertyValue(prop, doc);

      // recurse into it
      exportProperties(prop, placeTags, doc);
    }
    doc.endList();
  }

  /**
   * Exports the given property's value
   */
  private void exportPropertyValue(Property prop, Document doc) {

    // check for links to other indi/fams
    if (prop instanceof PropertyXRef) {
      
      PropertyXRef xref = (PropertyXRef)prop;
      doc.addLink(xref.getTargetEntity());
      
      // done
      return;
    } 

    // multiline needs loop
    if (prop instanceof MultiLineProperty) {
      MultiLineProperty.Iterator lines = ((MultiLineProperty)prop).getLineIterator();
      do {
        doc.addText(lines.getValue());
      } while (lines.next());
      // done
      return;
    }

    // patch for NAME
    String value;    
    if (prop instanceof PropertyName)
      value = ((PropertyName)prop).getName();
    else
      value = prop.getDisplayValue();

    doc.addText(value);
      
    // done
  }

//  /**
//   * Exports index.html row
//   */
//  private void exportIndexRow(PrintWriter out, Indi indi) throws IOException {
//
//    // Standard
//    printCell(out,wrapID(indi));
//
//    printCell(out,indi.getLastName() );
//    printCell(out,indi.getFirstName());
//
//    printCell(out,indi.getProperty("SEX",true));
//    printCell(out,indi.getProperty(new TagPath("INDI:BIRT:DATE")));
//    printCell(out,indi.getProperty(new TagPath("INDI:BIRT:PLAC")));
//    printCell(out,indi.getProperty(new TagPath("INDI:DEAT:DATE")));
//    printCell(out,indi.getProperty(new TagPath("INDI:DEAT:PLAC")));
//
//    // done
//  }
//  
//  /** 
//   * Exports index.html
//   */
//  private void exportIndex(Gedcom gedcom, File dir) throws IOException {
//
//    File file = getFileForIndex(dir);
//    println(i18n("exporting", new String[]{ file.getName(), dir.toString() }));
//    PrintWriter out = getWriter(new FileOutputStream(file));
//
//    // HEAD
//    printOpenHTML(out, gedcom.getName());
//
//    // TABLE
//    out.println("<table border=1 cellspacing=1>");
//
//    // TABLE HEADER
//    out.println("<tr class=header>"); 
//    printCell(out, "ID");
//    printCell(out, PropertyName.getLabelForLastName());
//    printCell(out, PropertyName.getLabelForFirstName());
//    printCell(out, PropertySex.TXT_SEX);
//    printCell(out, Gedcom.getName("BIRT"));
//    printCell(out, Gedcom.getName("PLAC"));
//    printCell(out, Gedcom.getName("DEAT"));
//    printCell(out, Gedcom.getName("PLAC"));
//    out.println("</tr>");  //F. Massonneau 03/04/2002
//
//    // Go through individuals
//    Entity[] indis = gedcom.getEntities(Gedcom.INDI, "INDI:NAME");
//    for (int i=0;i<indis.length;i++) {
//      out.println("<tr>");
//      exportIndexRow(out, (Indi)indis[i]);
//      out.println("</tr>");
//      // .. next individual
//    }
//
//    // END TABLE
//    out.println("</table>");
//
//    // TAIL
//    printCloseHTML(out);
//
//    // done
//    out.close();
//    
//  }

} //ReportHTMLSheets
