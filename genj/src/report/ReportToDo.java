/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
/**
 * TODO Daniel: voir avec ie (page break)
 * TODO Daniel: inclure dans la liste les sources, repo, ... fictifs pour faire un tri
 */
import genj.fo.Document;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import genj.report.Report;

import java.util.List;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

/**
 * GenJ - Report
 * 
 * @author Daniel ANDRE <daniel.andre@free.fr>
 * @version 1.0
 */
public class ReportToDo extends Report {

  private final static String PLACE_AND_DATE_FORMAT = "{$D}{ $p}";

  public String todoTag = "NOTE";

  public String todoStart = "TODO:";

  public boolean outputWorkingSheet = false;

  public boolean outputSummary = true;
  
  private final static String
    ROW_FORMAT_HEADER1 = "font-size=larger,background-color=#00ccff,font-weight=bold";
  
  /*
         ".head1{background-color:#00ccff;font-size:20px;font-weight:bold;}"+
         ".head2{background-color:#33ffff;font-size:16px;font-weight:bold;}"+
         ".head3{background-color:#ffffcc;font-weight:bold;}"+
         ".head3-todo{background-color:#99cccc;font-weight:bold;}"+
         ".head4{background-color:#ffffcc;}"
         */
         
  /**
   * Overriden image - we're using the provided FO image
   */
  protected ImageIcon getImage() {
    return Report.IMG_FO;
  }

  /**
   * we're not generating anything to stdout anymore aside from debugging ino
   */
  public boolean usesStandardOut() {
    return false;
  }

  /**
   * The report's entry point
   */
  public void start(Gedcom gedcom) {
    
    Entity[] indis = gedcom.getEntities(Gedcom.INDI, "INDI:NAME");
    Entity[] fams = gedcom.getEntities(Gedcom.FAM, "FAM");
    
    Entity[] ents = new Entity[indis.length+fams.length];
    System.arraycopy(indis, 0, ents, 0, indis.length);
    System.arraycopy(fams, 0, ents, indis.length, fams.length);
    
    start(ents);
    
  }
  
  /**
   * The report's entry point - for a single individual
   */
  public void start(Indi indi) {
    start(new Indi[] { indi });
  }

  /**
   * The report's entry point - for a single family
   */
  public void start(Fam fam) {
    start(new Fam[]{ fam });
  }

  /**
   * The report's entry point - for a bunch of entities
   */
  public void start(Entity[] entities) {
    
    // create an output document
    Document doc = new Document(translate("titletodos"));

    // generate a detailed working sheet?
    if (outputWorkingSheet) {
      
      doc.startTable();
      doc.addTableColumn("column-width=10%");
      doc.addTableColumn("column-width=10%");
      doc.addTableColumn("column-width=20%");
      doc.addTableColumn("column-width=20%");
      doc.addTableColumn("column-width=20%");
      doc.addTableColumn("column-width=20%");

      exportWorkingSheet(entities, doc);
      doc.endTable();
      
    }

    // generate a summary?
    if (outputSummary) {
      
      // Loop through individuals & families
      doc.startTable();
      
      doc.nextTableRow(ROW_FORMAT_HEADER1);
      doc.addTableColumn("column-width=8%");
      doc.addTableColumn("column-width=8%");
      doc.addTableColumn("column-width=8%");
      doc.addTableColumn("");
      doc.addTableColumn("");
      
      doc.nextTableCell("number-columns-spanned=5");
      doc.addText(translate("titletodos")); // "head1"

      doc.nextTableRow();
      doc.addText( translate("evt.col") ); // strong
      doc.nextTableCell();
      doc.addText( translate("date.col") ); // strong
      doc.nextTableCell();
      doc.addText( translate("place.col") ); // strong
      doc.nextTableCell();
      doc.addText( translate("indi.col") ); // strong
      doc.nextTableCell();
      doc.addText( translate("todo.col") ); // strong
      
      int nbTodos = exportSummary(entities, doc);
      doc.endTable();
      
      doc.addText( translate("nbtodos", "" + nbTodos) );
    }
    
    // Done
    showDocumentToUser(doc);

  }
  
  /**
   * Exports the working sheet
   */
  private void exportWorkingSheet(Entity[] entities, Document doc) {
    
    // loop over entities
    for (int e = 0; e < entities.length; e++) {
      
      Entity entity = entities[e];
      
      List todos = findProperties(entity);
      if (!todos.isEmpty()) {
        if (entity instanceof Indi)
          exportEntity((Indi)entity, doc);
        if (entity instanceof Fam)
          exportEntity((Fam)entity, doc);
      }
    }
    
  }
  
  /**
   * Exports a family
   */
  private void exportEntity(Fam fam, Document doc) {
    Property prop;
    Property[] propArray;
    List todos;
    String tempString = "";
    Indi tempIndi;
    Fam tempFam;

    todos = findProperties(fam);
    if (todos.size() == 0) 
      return;

    doc.nextTableRow(ROW_FORMAT_HEADER1);
    doc.nextTableCell("number-columns-spanned=6");
    doc.addText( translate("titlefam", new String[] { fam.toString(), fam.getId() }) );
    
    // //// Epoux
    tempIndi = fam.getHusband();
    doc.nextTableRow();
    doc.addText( Gedcom.getName("HUSB"));
    doc.nextTableCell("number-columns-spanned=5");
    doc.addText( tempIndi.getName() );
    
    outputEventRow(tempIndi, "BIRT", todos, doc);
    outputEventRow(tempIndi, "BAPM", todos, doc);
    outputEventRow(tempIndi, "DEAT", todos, doc);
    outputEventRow(tempIndi, "BURI", todos, doc);
    
    tempFam = (tempIndi == null) ? null : tempIndi .getFamilyWhereBiologicalChild();
    if (tempFam != null) {
      doc.nextTableRow();
      doc.addText( translate("father") + ":"); // "head3" 
      doc.nextTableCell("number-columns-spanned=5");
      doc.addText( getIndiString(tempFam.getHusband()) );
      doc.nextTableRow();
      doc.addText( translate("mother") + ":" );
      doc.nextTableCell("number-columns-spanned=5");
      doc.addText( getIndiString(tempFam.getWife()) );
    }

    // //// Epouse
    tempIndi = fam.getWife();
    doc.nextTableRow();
    doc.addText( Gedcom.getName("WIFE") );
    doc.nextTableCell("number-columns-spanned=5");
    doc.addText( tempIndi.getName() );
    
    outputEventRow(tempIndi, "BIRT", todos, doc);
    outputEventRow(tempIndi, "BAPM", todos, doc);
    outputEventRow(tempIndi, "DEAT", todos, doc);
    outputEventRow(tempIndi, "BURI", todos, doc);
    
    tempFam = (tempIndi == null) ? null : tempIndi .getFamilyWhereBiologicalChild();
    if (tempFam != null) {
      doc.nextTableRow();
      doc.addText( translate("father") );
      doc.nextTableCell("number-columns-spanned=5");
      doc.addText( getIndiString(tempFam.getHusband()) );
      doc.nextTableRow();
      doc.addText( translate("mother") + ":" );
      doc.nextTableCell("number-columns-spanned=5");
      doc.addText( getIndiString(tempFam.getWife()) );
    }
    outputEventRow(fam, "MARR", todos, doc);

    // //// Enfants
    Indi[] children = fam.getChildren();
    if (children.length > 0) {
      doc.nextTableRow();
      doc.nextTableCell("number-columns-spanned=6");
      doc.addText( Gedcom.getName("CHIL", children.length > 1) );
      for (int c = 0; c < children.length; c++) {
        doc.nextTableRow();
        doc.addText("" + (c + 1) );
        doc.nextTableCell("number-columns-spanned=5");
        doc.addText( getIndiString(children[c]) );
      }
    }

    /** ************** Notes */
    propArray = fam.getProperties("NOTE");
    boolean seenNote = false;
    for (int i = 0; i < propArray.length; i++) {
      prop = (Property) propArray[i];
      if (todos.contains(prop))
        continue;
      if (!seenNote) {
        doc.nextTableRow();
        doc.nextTableCell("number-columns-spanned=6");
        doc.addText( translate("main.notes") );
        seenNote = true;
      }
      doc.nextTableRow();
      doc.nextTableCell();
      doc.nextTableCell("number-columns-spanned=5");
      outputPropertyValue(prop, doc);
    }

    /** ************** Todos */
    doc.nextTableRow();
    doc.nextTableCell("number-columns-spanned=6");
    doc.addText( translate("titletodo") );
    for (int i = 0; i < todos.size(); i++) {
      prop = (Property) todos.get(i);
      Property parent = prop.getParent();
      doc.nextTableRow();
      if (parent instanceof Fam) {
        doc.nextTableCell();
        doc.nextTableCell("number-columns-spanned=5");
        outputPropertyValue(prop, doc);
      } else {
        doc.addText( Gedcom.getName(parent.getTag()) );
        doc.nextTableCell("number-columns-spanned=5");
        doc.addText( parent.format(PLACE_AND_DATE_FORMAT) );
        doc.nextParagraph();
        outputPropertyValue(prop,doc);
        doc.nextParagraph();
        doc.addText( outputProperty(prop, prop.getPath().toString() + ":REPO") );
        doc.nextParagraph();
        doc.addText( outputProperty(prop, prop.getPath().toString() + ":NOTE") );
      }
    }
    
    // done with fam
  }

  /**
   * Exports an individual
   */
  private void exportEntity(Indi indi, Document doc) {
    Property prop;
    Property[] propArray;
    List todos;
    String tempString = "";

    todos = findProperties(indi);
    if (todos.size() == 0) 
      return;

    doc.nextTableRow(ROW_FORMAT_HEADER1);
    doc.nextTableCell("number-columns-spanned=6");
    doc.addText( translate("titleindi", new String[] { indi.getName(), indi.getId() }) );

    doc.nextTableRow();
    doc.nextTableCell("number-columns-spanned=6");
    doc.addText( translate("titleinfosperso") ); // "head2"
    
    doc.nextTableRow();
    doc.addText( Gedcom.getName("NAME") ); // "head3"
    doc.nextTableCell("number-columns-spanned=3");
    doc.addText( indi.getLastName() ); // 0, 3
    doc.nextTableCell();
    doc.addText( "ID: " + indi.getId() );
    doc.nextTableCell();
    doc.addText( Gedcom.getName("SEX") + ": " + PropertySex.getLabelForSex(indi.getSex()) );
    
    doc.nextTableRow();
    doc.addText( Gedcom.getName("NICK") ); // "head3"
    doc.nextTableCell("number-columns-spanned=5");
    doc.addText( outputProperty(indi, "INDI:NAME:NICK") );
    
    outputEventRow(indi, "BIRT", todos, doc);
    outputEventRow(indi, "BAPM", todos, doc);
    outputEventRow(indi, "DEAT", todos, doc);
    outputEventRow(indi, "BURI", todos, doc);
    
    doc.nextTableRow();
    doc.addText( Gedcom.getName("REFN") ); // "head3"
    doc.nextTableCell("number-columns-spanned=3");
    doc.addText( outputProperty(indi, "INDI:REFN") );
    doc.nextTableCell();
    doc.addText( Gedcom.getName("CHAN") ); //  "head3"
    doc.nextTableCell();
    doc.addText( outputProperty(indi, "INDI:CHAN") ); 
    
    Fam fam = indi.getFamilyWhereBiologicalChild();
    if (fam != null) {
      doc.nextTableRow();
      doc.addText( translate("father") + ":" ); // "head3"
      doc.nextTableCell("number-columns-spanned=5");
      doc.addText( getIndiString(fam.getHusband()) ); 
      
      doc.nextTableRow();
      doc.addText( translate("mother") + ":" ); // , "head3"
      doc.nextTableCell("number-columns-spanned=5");
      doc.addText( getIndiString(fam.getWife()) ); 
    }

    // And we loop through its families
    Fam[] fams = indi.getFamiliesWhereSpouse();
    if (fams.length > 0) {
      doc.nextTableRow();
      doc.nextTableCell("number-columns-spanned=6");
      doc.addText( Gedcom.getName("FAM", fams.length > 1) ); // "head2"
    }
    
    for (int f = 0; f < fams.length; f++) {
      // .. here's the fam and spouse
      Fam famc = fams[f];
      Indi spouse = famc.getOtherSpouse(indi);
      if (spouse != null) {
        Indi[] children = famc.getChildren();
        
        doc.nextTableRow();
        doc.addText(translate("spouse") + ":" ); // "head3",
        doc.nextTableCell();
        doc.addText( getIndiString(spouse) );
        doc.nextParagraph();
        doc.addText( Gedcom.getName("MARR") + " : "); // strong
        doc.addText( famc.format("MARR", PLACE_AND_DATE_FORMAT) ); // 0, 5

        if (children.length > 0) {
          
          doc.nextTableRow();
          doc.addText(Gedcom.getName("CHIL", children.length > 1) ); // "head4", children.length, 1)
          doc.nextTableCell();
          doc.addText( getIndiString(children[0]) ); // 0, 4
          for (int c = 1; c < children.length; c++) {
            doc.nextTableRow();
            doc.addText( getIndiString(children[c]) ); // 0,4
          }
        }
      }
    }

    doc.nextTableRow();
    doc.nextTableCell("number-columns-spanned=6");
    doc.addText( Gedcom.getName("EVEN", true) ); // "head2"

    outputEventRow(indi, "OCCU", todos, doc);
    outputEventRow(indi, "RESI", todos, doc);

    /** ************** Notes */
    propArray = indi.getProperties("NOTE");
    boolean seenNote = false;
    for (int i = 0; i < propArray.length; i++) {
      prop = (Property) propArray[i];
      if (todos.contains(prop))
        continue;
      if (!seenNote) {
        doc.nextTableRow();
        doc.nextTableCell("number-columns-spanned=6");
        doc.addText( translate("main.notes") ); //  "head2"
        seenNote = true;
      }
      doc.nextTableRow();
      doc.nextTableCell("number-columns-spanned=5");
      outputPropertyValue(prop, doc);
    }

    /** ************** Todos */
    doc.nextTableRow();
    doc.nextTableCell("number-columns-spanned=6");
    doc.addText( translate("titletodo") ); // "head2"
    for (int i = 0; i < todos.size(); i++) {
      prop = (Property) todos.get(i);
      Property parent = prop.getParent();
      String row;
      if (parent instanceof Indi) {
        doc.nextTableRow();
        doc.nextTableCell();
        doc.nextTableCell("number-columns-spanned=5");
        outputPropertyValue(prop,doc);
      } else {
        doc.nextTableRow();
        doc.addText( Gedcom.getName(parent.getTag()) ); // "head3-todo"
        doc.nextTableCell("number-columns-spanned=5");
        doc.addText( parent.format(PLACE_AND_DATE_FORMAT) );
        doc.nextParagraph();
        outputPropertyValue(prop, doc);
        doc.nextParagraph();
        doc.addText( formatString("", outputProperty(prop, prop.getPath().toString() + ":REPO"), "") );
        doc.nextParagraph();
        doc.addText( formatString("", outputProperty(prop, prop.getPath().toString() + ":NOTE"), "") );
      }
    }
  }

  /**
   * create a row for an event
   */
  private void outputEventRow(Entity indi, String tag, List todos, Document doc) {
    
    if (indi == null)
      return;
    
    Property props[] = indi.getProperties(tag);
    if (props.length==0)
      return;
    
    if (props.length == 1) {
      
      doc.nextTableRow();
      doc.addText( Gedcom.getName(tag) ); // "head3"
      doc.nextTableCell("number-columns-spanned=5");
      doc.addText( indi.format(tag, PLACE_AND_DATE_FORMAT) );
      doc.nextParagraph();
      outputNotes( "Note : ", indi.getProperty(tag), todos, doc); // Note should be emphasized
      
      return;
    }
    
    for (int i = 0; i < props.length; i++) {
      doc.nextTableRow();
      doc.addText(Gedcom.getName(tag) ); // "head3"
      doc.nextTableCell("number-columns-spanned=5");
      doc.addText( props[i].format(PLACE_AND_DATE_FORMAT) );
      doc.nextParagraph();
      outputNotes( "Note : ", props[i], todos, doc); // Note should be emphasized
    }
    
    // done
  }

  /**
   * Export todo summary only into a 5 column table
   */
  private int exportSummary(Entity[] ents, Document doc) {
    
    List todos;
    boolean isFirstPage = true;
    int nbTodos = 0;

    // loop over all entities
    for (int e = 0; e < ents.length; e++) {
      
      todos = findProperties(ents[e]);
      if (todos.size() == 0) 
        continue;
      
      // loop over todos for entity
      for (int i = 0; i < todos.size(); i++) {
        Property prop = (Property) todos.get(i);
        Property parent = prop.getParent();
        
        doc.nextTableRow();
        if (parent instanceof Indi) {
          doc.nextTableCell();
          doc.nextTableCell();
          doc.nextTableCell();
        } else if (parent instanceof Fam) {
          doc.nextTableCell();
          doc.nextTableCell();
          doc.nextTableCell();
        } else {
          doc.addText( Gedcom.getName(parent.getTag()) + parent.getValue() );
          doc.nextTableCell();
          doc.addText( parent.getPropertyDisplayValue("DATE") );
          doc.nextTableCell();
          doc.addText( parent.getPropertyDisplayValue("PLAC") );
        }
        doc.nextTableCell();
        doc.addText( prop.getEntity().toString() );
        doc.nextTableCell();
        outputPropertyValue(prop, doc);
        
        nbTodos++;
      }
    }
    
    // done
    return nbTodos;
  }

  // private void exportTodosCsv(Entity[] ents) {
  // List todos;
  // boolean isFirstPage=true;
  //
  // for (int e = 0; e < ents.length; e++) {
  // todos = findProperties(ents[e]);
  // if (todos.size() == 0){
  // continue;
  // }
  // for (int i = 0; i < todos.size(); i++){
  // Property prop = (Property) todos.get(i);
  // Property parent = prop.getParent();
  // String row;
  // if (parent instanceof Indi) {
  // row = ",,";
  // } else if (parent instanceof Fam) {
  // row = ",,";
  // } else {
  // row = "\""+Gedcom.getName(parent.getTag())+parent.getValue()+"\"";
  // row += ",\""+parent.getPropertyDisplayValue("DATE")+"\"";
  // row += ",\""+parent.getPropertyDisplayValue("PLAC")+"\"";
  // }
  // row += ",\""+(prop.getEntity()).toString()+"\"";
  // row += ",\""+outputPropertyValue(prop)+"\"";
  // println(row);
  // }
  // }
  // }

  /**
   * Output notes for given property
   */
  private void outputNotes(String prefix, Property prop, List exclude, Document doc) {
    // prop exists?
    if (prop == null)
      return;
    
    Property[] props = prop.getProperties("NOTE");
    for (int i = 0; i < props.length; i++) {
      if (exclude.contains(props[i]))
        continue;
      doc.addText( prefix );
      outputPropertyValue(props[i], doc);
    }
    
    // done
  }

  private String outputProperty(Property prop, String tagPath) {
    Property subProp = prop.getPropertyByPath(tagPath);
    return (subProp == null) ? "" : subProp.toString();
  }

  private String formatString(String start, String middle, String end) {
    if (middle != null && middle.length() != 0) {
      return ((start == null) ? "" : start) + middle
          + ((end == null) ? "" : end);
    } else {
      return "";
    }
  }

  private String getIndiString(Indi indi) {
    // Might be null
    if (indi == null)
      return "";
    String birth = indi.format("BIRT", OPTIONS.getBirthSymbol() + " " + PLACE_AND_DATE_FORMAT);
    String death = indi.format("DEAT", OPTIONS.getDeathSymbol() + " " + PLACE_AND_DATE_FORMAT);
    return indi.toString() + " " + birth + " " + death; // name was strong
  }

  /**
   * Exports the given property's value
   */
  private void outputPropertyValue(Property prop, Document doc) {

    // check for links to other indi/fams
    if (prop instanceof PropertyXRef) {
      PropertyXRef xref = (PropertyXRef) prop;
      outputPropertyValue( xref.getTargetEntity(), doc);
      return;
    }

    // simply property - use display value
    if (!(prop instanceof MultiLineProperty)) {
      doc.addText(prop.getDisplayValue());
      return;
    }
      
    // loop over multilines
    MultiLineProperty.Iterator lines = ((MultiLineProperty)prop).getLineIterator();
    do {
      doc.nextParagraph();
      doc.addText(lines.getValue());
    } while (lines.next());

    // done
  }

  private List findProperties(Property of) {
    return of.findProperties(Pattern.compile(todoTag), Pattern.compile(
        todoStart + ".*", Pattern.DOTALL));
  }

} // ReportToDo
