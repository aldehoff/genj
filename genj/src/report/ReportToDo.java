/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
/**
 * todo
 * mettre le nombre de taches a faire dans le resume
 */
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import genj.report.Report;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.ImageIcon;

/**
 * GenJ - Report
 * @author Daniel ANDRE <daniel.andre@free.fr>
 * @version 1.0
 */
public class ReportToDo extends Report {
    private Formatter output;
    private String eol= System.getProperty("line.separator");

    public String todoTag = "NOTE";
    public String todoStart = "A Faire:";
    public boolean outputWorkingSheet = false;
    public boolean outputSummary = true;
    public boolean outputSummaryCsv = false;
    private boolean isFirstPage=true;
  
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
    return true;
  }

  /**
   * The report's entry point
   */
    public void start(Gedcom gedcom) {
    
	if (outputSummaryCsv){
	    println(i18n("evt.col")
		    +","+i18n("date.col")
		    +","+i18n("place.col")
		    +","+i18n("indi.col")
		    +","+i18n("todo.col")
		    );
	    exportTodosCsv(gedcom.getEntities(Gedcom.INDI, "INDI:NAME"));
	    exportTodosCsv(gedcom.getEntities(Gedcom.FAM, "FAM"));
	    return;
	}
	initializeReport();
	output.start();
	
	if (outputWorkingSheet) {
	    output.startTable();
	    exportEntities(gedcom.getEntities(Gedcom.INDI, "INDI:NAME"));
	    exportEntities(gedcom.getEntities(Gedcom.FAM, "FAM"));
	    output.endTable();
	}
	if (outputSummary) {
	    // Loop through individuals & families
	    output.print(output.page());
	    output.startTable();
	    output.print(output.row(output.cell(i18n("titletodos"),"head1",0,5)));
	    output.print(output.row(output.cell(output.strong(i18n("evt.col")))+
				    output.cell(output.strong(i18n("date.col")))+
				    output.cell(output.strong(i18n("place.col")))+
				    output.cell(output.strong(i18n("indi.col")))+
				    output.cell(output.strong(i18n("todo.col")))
				    ));
	    exportTodos(gedcom.getEntities(Gedcom.INDI, "INDI:NAME"));
	    exportTodos(gedcom.getEntities(Gedcom.FAM, "FAM"));
	    output.endTable();
	}
	// Done
	output.end();
    
  }

  /**
   * The report's entry point
   */
    public void start(Indi indi) {
	Indi[] indis = {indi};
	start(indis);
    }

    public void start(Indi[] indis) {
   
	//	initializeReport();
	//output.start();
	exportEntities(indis);
	//output.end();
    }

    public void start(Fam fam) {
	Fam[] fams = {fam};
	start(fams);
    }

    public void start(Fam[] fams) {
	exportEntities(fams);
    }

    private void initializeReport(){
	output = new FormatterHtml(this);
	((FormatterHtml) output).
	    setStyle("td{vertical-align:top;border-style:solid;border-color:black;border-width:1;}"+
		     "div.indent {margin-left:30px;"+
		     "}"+
		     "p{margin-top:0;margin-bottom:0;"+
		     "text-indent: - 20px;"+
		     //"padding-left:20px;"+
		     "}"+
		     "h2.report{border-color:black;background-color:#f0f0f0;border-style:solid;border-width:0 0 2 0;text-transform:uppercase;}"+
		     ".head1,.head2,.head3{margin-top:0;margin-bottom:0;}"+
		     ".head1{background-color:#00ccff;font-size:20px;font-weight:bold;}"+
		     ".head2{background-color:#33ffff;font-size:16px;font-weight:bold;}"+
		     ".head3{background-color:#ffffcc;font-weight:bold;}"+
		     ".head3-todo{background-color:#99cccc;font-weight:bold;}"+
		     ".head4{background-color:#ffffcc;}"

);
	isFirstPage = true;
    }
  /**
   * Exports the given entities 
   */
    private void exportEntities(Entity[] ents)  {
	ArrayList props;

	if (!outputWorkingSheet) return;
	for (int e = 0; e < ents.length; e++) {
	    props = getPropertiesWithTag(ents[e],todoTag,todoStart);
	    if (props.size() == 0){
		continue;
	    }
	    if (!isFirstPage) {
		output.print(output.tablePageBreak());
	    }else{
		isFirstPage = false;
	    }
	    if (ents[e] instanceof Indi)
		exportEntity((Indi)ents[e]);
	    else if (ents[e] instanceof Fam)
		exportEntity((Fam)ents[e]);
	     
	}
    }
  
    private void exportEntity(Fam fam){
	Property prop;
	Property[] propArray;
	ArrayList todos;
	ArrayList props;
	String tempString = "";
	Indi tempIndi;
	Fam tempFam;

	todos = getPropertiesWithTag(fam,todoTag,todoStart);
	if (todos.size() == 0){
	    return;
	}

	output.print(output.row(output.cell(i18n("titlefam",new String[]{fam.toString(),fam.getId()}),"head1",0,6)));
	////// Epoux
	tempIndi = fam.getHusband();
	output.print(output.row(output.cell(i18n("husband"),"head2")+
				output.cell(getFormattedName(tempIndi),"head2",0,5)));
	outputEventRow(tempIndi,"BIRT",todos);
	outputEventRow(tempIndi,"BAPM",todos);
	outputEventRow(tempIndi,"DEAT",todos);
	outputEventRow(tempIndi,"BURI",todos);
	tempFam = tempIndi.getFamilyWhereBiologicalChild();
	if (tempFam != null) {
	    output.print(output.row(output.cell(i18n("father")+":","head3")+
				    output.cell(getIndiString(tempFam.getHusband()),0,5)));
	    output.print(output.row(output.cell(i18n("mother")+":","head3")+
				    output.cell(getIndiString(tempFam.getWife()),0,5)));
	}

	////// Epouse
	tempIndi = fam.getWife();
	output.print(output.row(output.cell(i18n("wife"),"head2")+
				output.cell(getFormattedName(tempIndi),"head2",0,5)));
	outputEventRow(tempIndi,"BIRT",todos);
	outputEventRow(tempIndi,"BAPM",todos);
	outputEventRow(tempIndi,"DEAT",todos);
	outputEventRow(tempIndi,"BURI",todos);
	tempFam = tempIndi.getFamilyWhereBiologicalChild();
	if (tempFam != null) {
	    output.print(output.row(output.cell(i18n("father")+":","head3")+
				    output.cell(getIndiString(tempFam.getHusband()),0,5)));
	    output.print(output.row(output.cell(i18n("mother")+":","head3")+
				    output.cell(getIndiString(tempFam.getWife()),0,5)));
	}
	outputEventRow(fam,"MARR",todos);

	////// Enfants
	Indi[] children = fam.getChildren();
	if ((tempString=Formatter.getPluralSuffix(children.length)) != null){
	    output.print(output.row(output.cell(i18n("childrentitle."+tempString),"head2",0,6)));
	    for (int c = 0; c < children.length; c++) {
		output.print(output.row(output.cell(""+(c+1),"head3")+ output.cell(getIndiString(children[c]),0,5)));
	    }
	}

	/**************** Notes */
	propArray = Formatter.getProperties(fam,"NOTE");
	boolean seenNote=false;
	for (int i = 0; i<propArray.length; i++){
	    String noteString = "";
	    prop = (Property)propArray[i];
	    if (todos.contains(prop)) continue;
	    if (!seenNote) {
		output.print(output.row(output.cell(i18n("main.notes"),"head2",0,6)));
		seenNote = true;
	    }
	    noteString = output.cell(outputPropertyValue(prop),0,6);
	    output.print(output.row(noteString));
	}
				  
	/**************** Todos */
	output.print(output.row(output.cell(i18n("titletodo"),"head2",0,6)));
	for (int i = 0; i < todos.size(); i++){
	    prop = (Property) todos.get(i);
	    Property parent = prop.getParent();
	    String row;
	    if (parent instanceof Fam) {
		row = output.cell("");
		row += output.cell(outputPropertyValue(prop),0,5);
	    } else {
		row = output.cell(Gedcom.getName(parent.getTag()),"head3-todo");
		row += output.cell(Formatter.formatEvent(parent,Formatter.DATE_FORMAT_LONG, true, 0)+
				   formatString(output.br(),outputPropertyValue(prop),"")+
				   formatString(output.br(),getPropertyString(prop,prop.getPath().toString()+":REPO"),"")+
				   formatString(output.br(),getPropertyString(prop,prop.getPath().toString()+":NOTE"),"")
				    ,0,5);
	    }
	    output.print(output.row(row));
	}
    }

    private void exportEntity(Indi indi){
	Property prop;
	Property[] propArray;
	ArrayList todos;
	ArrayList props;
	String tempString = "";

	todos = getPropertiesWithTag(indi,todoTag,todoStart);
	if (todos.size() == 0){
	    return;
	}

	output.print(output.row(output.cell(i18n("titleindi",new String[]{indi.getName(),indi.getId()}),"head1",0,6)));
	output.print(output.row(output.cell(i18n("titleinfosperso"),"head2",0,6)));
	output.print(output.row(output.cell(Gedcom.getName("NAME"),"head3")+
				  output.cell(getFormattedName(indi),0,3)+
				  output.cell("ID: "+indi.getId())+
				  output.cell(Gedcom.getName("SEX")+": "+
					      PropertySex.getLabelForSex(indi.getSex()))));
	output.print(output.row(output.cell(Gedcom.getName("NICK"),"head3")+
				  output.cell(getPropertyString(indi,"INDI:NAME:NICK"),0,5)));
	outputEventRow(indi,"BIRT",todos);
	outputEventRow(indi,"BAPM",todos);
	outputEventRow(indi,"DEAT",todos);
	outputEventRow(indi,"BURI",todos);
	output.print(output.row(output.cell(Gedcom.getName("REFN"),"head3")+
				  output.cell(getPropertyString(indi,"INDI:REFN"),0,3)+
				  output.cell(Gedcom.getName("CHAN"),"head3")+
				  output.cell(getPropertyString(indi,"INDI:CHAN"))
				  ));
	Fam fam = indi.getFamilyWhereBiologicalChild();
	if (fam != null) {
	    output.print(output.row(output.cell(i18n("father")+":","head3")+
				    output.cell(getIndiString(fam.getHusband()),0,5)));
	    output.print(output.row(output.cell(i18n("mother")+":","head3")+
				    output.cell(getIndiString(fam.getWife()),0,5)));
	}

	// And we loop through its families
	Fam[] fams = indi.getFamiliesWhereSpouse();
	if ((tempString=Formatter.getPluralSuffix(fams.length)) != null){
	    output.print(output.row(output.cell(i18n("familytitle."+tempString),"head2",0,6)));
	}
	for (int f=0;f<fams.length;f++) {
	    // .. here's the fam and spouse
	    Fam famc = fams[f];
	    Indi spouse= famc.getOtherSpouse(indi);
	    if (spouse != null){
		Indi[] children = famc.getChildren();
		output.print(output.row(output.cell(i18n("spouse")+":","head3",children.length+1,1)+
					output.cell(getIndiString(spouse)
						    +output.br()
						    +output.strong(Gedcom.getName("MARR")+" : ")
						    +Formatter.formatEvent(famc, "MARR", Formatter.DATE_FORMAT_LONG, true, 0)
						    ,0,5)));
           
		if ((tempString=Formatter.getPluralSuffix(children.length)) != null){
		    output.print(output.row(output.cell(i18n("childrentitle."+tempString),"head4",children.length,1)+
					    output.cell(getIndiString(children[0]),0,4)));
		    for (int c = 1; c < children.length; c++) {
			output.print(output.row( output.cell(getIndiString(children[c]),0,4)));
		    }
		}
	    }
	}

	output.print(output.row(output.cell(i18n("eventstitle"),"head2",0,6)));

	outputEventRow(indi,"OCCU",todos);
	outputEventRow(indi,"RESI",todos);

	/**************** Notes */
	propArray = Formatter.getProperties(indi,"NOTE");
	boolean seenNote=false;
	for (int i = 0; i<propArray.length; i++){
	    String noteString = "";
	    prop = (Property)propArray[i];
	    if (todos.contains(prop)) continue;
	    if (!seenNote) {
		output.print(output.row(output.cell(i18n("main.notes"),"head2",0,6)));
		seenNote = true;
	    }
	    noteString = output.cell(outputPropertyValue(prop),0,6);
	    output.print(output.row(noteString));
	}
				  
	/**************** Todos */
	output.print(output.row(output.cell(i18n("titletodo"),"head2",0,6)));
	for (int i = 0; i < todos.size(); i++){
	    prop = (Property) todos.get(i);
	    Property parent = prop.getParent();
	    String row;
	    if (parent instanceof Indi) {
		row = output.cell("");
		row += output.cell(outputPropertyValue(prop),0,5);
	    } else {
		row = output.cell(Gedcom.getName(parent.getTag()),"head3-todo");
		row += output.cell(Formatter.formatEvent(parent,Formatter.DATE_FORMAT_LONG, true, 0)+
				   formatString(output.br(),outputPropertyValue(prop),"")+
				   formatString(output.br(),getPropertyString(prop,prop.getPath().toString()+":REPO"),"")+
				   formatString(output.br(),getPropertyString(prop,prop.getPath().toString()+":NOTE"),"")
				    ,0,5);
	    }
	    output.print(output.row(row));
	}
    }


    private void outputEventRow(Entity indi, String tag, ArrayList todos){
	Property props[] = Formatter.getProperties(indi,tag);
	
	if (props == null) return;
	if (props.length == 1)
	    output.print(output.row(output.cell(Gedcom.getName(tag),"head3")+
				    output.cell(Formatter.formatEvent(indi, tag, Formatter.DATE_FORMAT_LONG, true, 0)
						+getNotesString(output.br()+output.strong("Note : "),indi.getProperty(tag),todos),0,5)));
	else if (props.length != 0) {
	    for (int i = 0; i<props.length; i++){
		output.print(output.row(output.cell(Gedcom.getName(tag),"head3")+
					output.cell(Formatter.formatEvent(props[i], Formatter.DATE_FORMAT_LONG, true, 0)+
						    getNotesString(output.br()+output.strong("Note : "),props[i],todos),0,5)));
	    }
	}
    }

    private void exportTodos(Entity[] ents)  {
	ArrayList todos;
	boolean isFirstPage=true;

	if (!outputSummary) return;

	for (int e = 0; e < ents.length; e++) {
	    todos = getPropertiesWithTag(ents[e],todoTag,todoStart);
	    if (todos.size() == 0){
		continue;
	    }
	    for (int i = 0; i < todos.size(); i++){
		Property prop = (Property) todos.get(i);
		Property parent = prop.getParent();
		String row;
		if (parent instanceof Indi) {
		    row = output.cell("");
		    row += output.cell("");
		    row += output.cell("");
		} else if (parent instanceof Fam) {
		    row = output.cell("");
		    row += output.cell("");
		    row += output.cell("");
		} else {
		    row = output.cell(Gedcom.getName(parent.getTag())+parent.getValue());
		    row += output.cell(Formatter.getPropertyDate(parent,Formatter.DATE_FORMAT_LONG));
		    row += output.cell(Formatter.getPropertyPlace(parent,0));
		}
		row += output.cell((prop.getEntity()).toString());
		row += output.cell(outputPropertyValue(prop));
		output.print(output.row(row));
	    }
	}
    }

    private void exportTodosCsv(Entity[] ents)  {
	ArrayList todos;
	boolean isFirstPage=true;

	for (int e = 0; e < ents.length; e++) {
	    todos = getPropertiesWithTag(ents[e],todoTag,todoStart);
	    if (todos.size() == 0){
		continue;
	    }
	    for (int i = 0; i < todos.size(); i++){
		Property prop = (Property) todos.get(i);
		Property parent = prop.getParent();
		String row;
		if (parent instanceof Indi) {
		    row = ",,";
		} else if (parent instanceof Fam) {
		    row = ",,";
		} else {
		    row = "\""+Gedcom.getName(parent.getTag())+parent.getValue()+"\"";
		    row += ",\""+Formatter.getPropertyDate(parent,Formatter.DATE_FORMAT_LONG)+"\"";
		    row += ",\""+Formatter.getPropertyPlace(parent,0)+"\"";
		}
		row += ",\""+(prop.getEntity()).toString()+"\"";
		row += ",\""+outputPropertyValue(prop)+"\"";
		println(row);
	    }
	}
    }


    private String getNotesString(String prefix, Property prop, ArrayList exclude){
	String result = "";
	// prop exists?
	if (prop==null)
	    return "";
	Property[] props = Formatter.getProperties(prop,"NOTE");
	if (props.length == 0) return "";
	for (int i=0; i<props.length;i++){
	    if (exclude.contains(props[i])) continue;
	    result += prefix+outputPropertyValue(props[i]);
	}
	return result;
    }

    private String getPropertyString(Property prop, String tagPath){
	Property subProp = prop.getPropertyByPath(tagPath);
	return (subProp == null)?"":subProp.toString();
    }

    private String formatString(String start, String middle, String end){
	if (middle != null && middle.length() != 0){
	    return ((start == null)?"":start)+middle+((end == null)?"":end);
	} else {
	    return "";
	}
    }

    private String getIndiString(Indi indi){
	String name = "";
	String birth = "";
	String death = "";

	// Might be null
	if (indi==null) 
	    return "";

	name = output.strong(indi.getName())+" ("+indi.getId()+")";
	birth = Formatter.formatEvent(OPTIONS.getBirthSymbol(), indi, "BIRT", true,true,0);
	death = Formatter.formatEvent(OPTIONS.getDeathSymbol(), indi, "DEAT", true,true,0);
	return name+" "+birth+" "+death;
    }

  /**
   * Exports the given property's value
   */
    private String outputPropertyValue(Property prop) {
	String result = "";

    // check for links to other indi/fams
    if (prop instanceof PropertyXRef) {
      
      PropertyXRef xref = (PropertyXRef)prop;
      return outputPropertyValue((Property)xref.getTargetEntity());
    } 

    // multiline needs loop
    if (prop instanceof MultiLineProperty) {
      MultiLineProperty.Iterator lines = ((MultiLineProperty)prop).getLineIterator();
      /* TODO: remove endin br */
      String eol="";
      do {
        result += eol+lines.getValue();
	if (!outputSummaryCsv) eol = output.br();
	else eol=" ";
      } while (lines.next());
      // done
      return result;
    }

    // patch for NAME
    if (prop instanceof PropertyName)
      result = ((PropertyName)prop).getName();
    else
      result = prop.getDisplayValue();

      return result;
      
    // done
  }

    private ArrayList getPropertiesWithTag(Property prop, String tag){
	ArrayList result = new ArrayList();
	result.addAll(Arrays.asList(prop.getProperties(tag)));
	if (prop.getTag() == tag) result.add(prop);
	for (int i = 0; i<prop.getNoOfProperties(); i++){
	    result.addAll(getPropertiesWithTag(prop.getProperty(i),tag));

	}
	return result;
    }

    private ArrayList getPropertiesWithTag(Property prop, String tag, String start){
	ArrayList result = new ArrayList();
	if (prop.getTag().compareTo(tag) == 0) {
	    if (start.length() == 0){
		result.add(prop);
	    } else {
		String value = outputPropertyValue(prop);
		if (value != null) 
		    if (value.startsWith(start))
			result.add(prop); 
	    }
	}
	//	if ((prop.getTag() == tag) && (start.length() == 0 || prop.getValue().startsWith(start))) result.add(prop);
	for (int i = 0; i<prop.getNoOfProperties(); i++){
	    result.addAll(getPropertiesWithTag(prop.getProperty(i),tag,start));

	}
	return result;
    }
    /*
    private void outputToDo(Indi indi){
	ArrayList todoList = getPropertiesWithTag(indi,"_TODO");
	Property prop = (Property)todoList.get(0);
	println(Gedcom.getName(prop.getParent().getTag())+" "+outputPropertyValue(prop.getParent()));
	outputProperties(prop.getParent());
	//	println(prop.getParent().getDisplayValue());
	//println(prop.getDisplayValue());
	}*/
  /**
   * Exports the given property's properties
   */
    private void outputProperties(Property of) {

    // anything to do?
    if (of.getNoOfProperties()==0)
      return;
    
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
      println(Gedcom.getName(prop.getTag())+" "+outputPropertyValue(prop));

      // recurse into it
      outputProperties(prop);
    }
  }

    private String getFormattedName(Indi indi){
	String first = indi.getFirstName();
	String last = indi.getLastName();

	if (first == null) first = "";
	if (last == null) last = "";
	return output.strong(last.toUpperCase())+" "+first;
    }


} //ReportToDo
