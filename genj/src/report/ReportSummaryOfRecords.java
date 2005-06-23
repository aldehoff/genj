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
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyXRef;
import genj.report.Report;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.ImageIcon;

/**
 * GenJ - Report
 * @author Nils Meier <nils@meiers.net>
 * @version 1.0
 */
public class ReportSummaryOfRecords extends Report {
  
  /**
   * We accept a gedcom file as argument 
   */
  public String accepts(Object context) {
    if (!(context instanceof Gedcom))
      return null;
    Gedcom ged = (Gedcom)context;
    return i18n("title", ged.getName());
  }
  
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
  public void start(Object context) {
    
    // assuming Gedcom
    Gedcom gedcom = (Gedcom)context;
    
    // create a document
    Document doc = new Document(i18n("title", gedcom.getName()));
    
    doc.addText("This report shows information about all records in the Gedcom file "+gedcom.getName());

    // Loop through individuals & families
    exportEntities(gedcom.getEntities(Gedcom.INDI), doc);
    exportEntities(gedcom.getEntities(Gedcom.FAM ),doc);
    
    // add index
    doc.addIndex("names", "Name Index");
    
    // Done
    showDocumentToUser(doc);
    
  }

  /**
   * Exports the given entities 
   */
  private void exportEntities(Collection ents, Document doc)  {
    for (Iterator it=ents.iterator();it.hasNext();)
      exportEntity((Entity)it.next(), doc);
  }
  
  /**
   * Exports the given entity 
   */
  private void exportEntity(Entity ent, Document doc) {

    println(i18n("exporting", ent.toString() ));
      
    // start a new section
    doc.addSection( ent.toString() );
    
    // mark it
    doc.addAnchor(ent);
    if (ent instanceof Indi) {
      Indi indi = (Indi)ent;
      doc.addIndexTerm("names", indi.getLastName(), indi.getFirstName());
    }

//    // TABLE
//    out.println("<table border=\"1\" cellspacing=\"1\" width=\"100%\">");
//    out.println("<tr class=header>");
//    out.println("<td colspan=\"2\">"+ent.toString()+"</td>");
//    out.println("</tr>");
//    out.println("<tr>");
//
//    // Image Column
//    out.println("<td width=\"50%\" valign=\"center\" align=\"center\">");
//    exportImage(ent,dir,out);
//    out.println("</td>");
//
//    // Property Column
//    out.println("<td width=\"50%\" valign=\"top\" align=\"left\">");
//    out.println("<table border=0>");
//    exportProperty(ent,out,0);
//    out.println("</table>");
//    out.println("</td>");
    
    // end section
    doc.endSection();

    // Done
  }    
  
  /**
   * Exports the given entity's properties
   */
  private void exportProperty(Property prop, PrintWriter out, int level) {

    // export the value?
    exportPropertyLine(prop, out, level);

    // check subs
    for (int i=0;i<prop.getNoOfProperties();i++) {
      exportProperty(prop.getProperty(i), out, level+1);
    }

    // done
  }

  /**
   * Exports the given property in a line in the table
   */
  private void exportPropertyLine(Property prop, PrintWriter out, int level) {

    // we don't do anything for xrefs to non-indi/fam
    if (prop instanceof PropertyXRef) {
      PropertyXRef xref = (PropertyXRef)prop;
      if (!(xref.getTargetEntity() instanceof Indi||xref.getTargetEntity() instanceof Fam))
        return;
    }

    // start row
    out.println("<tr>");

    // first column
    boolean showValue = level>0 && !prop.isReadOnly(); 

    out.print("<td valign=TOP ");
    if (!showValue) 
      out.print("colspan=2");
    out.print(">");
    
    out.print("<property"+level+">");
    out.print(Gedcom.getName(prop.getTag()));
    out.print("</property"+level+">");
    
    out.println("</td>");

    // second column
    if (showValue) {
      out.print("<td>");
      out.print("<value>");
      exportPropertyValue(prop, out);
      out.print("</value>");
      out.println("</td>");
    }
        
    // end row
    out.println("</tr>");
    
    // Done
  }

  /**
   * Exports the given property's value
   */
  private void exportPropertyValue(Property prop, PrintWriter out) {

    // check for links to other indi/fams
    if (prop instanceof PropertyXRef) {
      
      PropertyXRef xref = (PropertyXRef)prop;
      Entity ent = xref.getTargetEntity();
      
      out.println("<A HREF=\"" + ent.getId() + ".html\">" + ent.toString() + "</a>");
      
      // done
      return;
    } 

    // multiline needs loop
    if (prop instanceof MultiLineProperty) {
      
      MultiLineProperty.Iterator lines = ((MultiLineProperty)prop).getLineIterator();
      do {
        out.print(lines.getValue());
        out.print("<br>");
      } while (lines.next());
      
      // done
      return;
    }

    // patch for NAME
    String value;    
    if (prop instanceof PropertyName)
      value = ((PropertyName)prop).getName();
    else
      value = prop.toString();

    out.print(value);
      
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
