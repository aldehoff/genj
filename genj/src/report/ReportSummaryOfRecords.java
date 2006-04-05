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
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.report.Report;

import javax.swing.ImageIcon;

/**
 * GenJ - Report
 * @author Nils Meier <nils@meiers.net>
 * @version 1.0
 */
public class ReportSummaryOfRecords extends Report {

  private final static TagPath PATH2IMAGES = new TagPath("INDI:OBJE:FILE");

  /** whether we're genering indexes for places */
  public  int generatePlaceIndex = 0;
  public String[] generatePlaceIndexs = {
    translate("place.index.none"), translate("place.index.one"), translate("place.index.each")
  };

  /** max # of images per record */
  private  int maxImagesPerRecord = 4;

  /**
   * Overriden image - we're using the provided FO image
   */
  protected ImageIcon getImage() {
    return Report.IMG_FO;
  }

  public int getMaxImagesPerRecord() {
    return maxImagesPerRecord;
  }

  public void setMaxImagesPerRecord(int set) {
    maxImagesPerRecord = Math.max(0,set);
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
    Document doc = new Document(translate("title", gedcom.getName()));

    doc.addText("This report shows information about all records in the Gedcom file "+gedcom.getName());

    // Loop through individuals & families
    exportEntities(gedcom.getEntities(Gedcom.INDI, "INDI:NAME"), doc);
    exportEntities(gedcom.getEntities(Gedcom.FAM, "FAM"), doc);

    // add a new page here - before the index is generated
    doc.nextPage();

    // Done
    showDocumentToUser(doc);

  }

  /**
   * Exports the given entities
   */
  private void exportEntities(Entity[] ents, Document doc)  {
    for (int e = 0; e < ents.length; e++) {
      exportEntity(ents[e], doc);
    }
  }

  /**
   * Exports the given entity
   */
  private void exportEntity(Entity ent, Document doc) {

    println(translate("exporting", ent.toString() ));

    // start a new section
    doc.startSection( ent.toString(), ent );

    // start a table for the entity
    doc.startTable("width=100%");
    doc.addTableColumn("column-width=80%");
    doc.addTableColumn("column-width=20%");

    // export its properties
    exportProperties(ent, doc, 0);

    // add images in next column
    doc.nextTableCell();
    Property[] files = ent.getProperties(PATH2IMAGES);
    for (int f=0;f<files.length && f<maxImagesPerRecord; f++) {
      PropertyFile file = (PropertyFile)files[f];
      doc.addImage(file.getFile(),"");
    }

    // done
    doc.endTable();
  }

  /**
   * Exports the given property's properties
   */
  private void exportProperties(Property of, Document doc, int level) {

    // anything to do?
    if (of.getNoOfProperties()==0)
      return;

    // create a list
    doc.startList();

    // an item per property
    for (int i=0;i<of.getNoOfProperties();i++) {

      Property prop = of.getProperty(i);

      // we don't do anything for xrefs to non-indi/fam
      if (prop instanceof PropertyXRef) {
        PropertyXRef xref = (PropertyXRef)prop;
        if (!(xref.getTargetEntity() instanceof Indi||xref.getTargetEntity() instanceof Fam))
          continue;
      }

      // here comes the item
      doc.nextListItem();

      // fill index while we're at it
      if (prop instanceof PropertyName) {
        PropertyName name = (PropertyName)prop;
        doc.addIndexTerm(translate("index.names"), name.getLastName(), name.getFirstName());
      }
      if (generatePlaceIndex>0&&(prop instanceof PropertyPlace)) {
        String index = generatePlaceIndex==1 ? translate("index.places") : translate("index.places.of", prop.getParent().getPropertyName());
        doc.addIndexTerm(index, ((PropertyPlace)prop).getCity());
      }

      // ... and the text
      String format = "";
      if (level==0) format = "text-decoration=underline";
      if (level==1) format =  "font-style=italic";
      doc.addText(Gedcom.getName(prop.getTag()), format);
      doc.addText(" ");

      // with its value
      exportPropertyValue(prop, doc);

      // recurse into it
      exportProperties(prop, doc, level+1);
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


} //SummaryOfRecords
