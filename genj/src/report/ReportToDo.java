/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
/**
 * TODO voir avec ie (page break)
 * TODO inclure dans la liste les sources, repo, ... fictifs pour faire un tri
 */
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import genj.report.Report;

import java.util.List;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

/**
 * GenJ - Report
 * @author Daniel ANDRE <daniel.andre@free.fr>
 * @version 1.0
 */
public class ReportToDo extends Report {
  
  private final static String PLACE_AND_DATE_FORMAT = "{$D}{ $p}";

    private Formatter output;

    public String todoTag = "NOTE";
    public String todoStart = "TODO:";
//    public boolean isRegExpr = false;
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
		println(translate("evt.col")
		    +","+translate("date.col")
		    +","+translate("place.col")
		    +","+translate("indi.col")
		    +","+translate("todo.col")
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
	    output.print(output.row(output.cell(translate("titletodos"),"head1",0,5)));
	    output.print(output.row(output.cell(output.strong(translate("evt.col")))+
				    output.cell(output.strong(translate("date.col")))+
				    output.cell(output.strong(translate("place.col")))+
				    output.cell(output.strong(translate("indi.col")))+
				    output.cell(output.strong(translate("todo.col")))
				    ));
	    int nbTodos=exportTodos(gedcom.getEntities(Gedcom.INDI, "INDI:NAME"));
	    nbTodos += exportTodos(gedcom.getEntities(Gedcom.FAM, "FAM"));
	    output.endTable();
	    output.println(translate("nbtodos",""+nbTodos));
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
	List props;

	if (!outputWorkingSheet) return;
	for (int e = 0; e < ents.length; e++) {
	    props = findProperties(ents[e]);
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
	List todos;
	String tempString = "";
	Indi tempIndi;
	Fam tempFam;

	todos = findProperties(fam);
	if (todos.size() == 0){
	    return;
	}

	output.print(output.row(output.cell(translate("titlefam",new String[]{fam.toString(),fam.getId()}),"head1",0,6)));
	////// Epoux
	tempIndi = fam.getHusband();
	output.print(output.row(output.cell(Gedcom.getName("HUSB"),"head2")+
				output.cell(getFormattedName(tempIndi),"head2",0,5)));
	outputEventRow(tempIndi,"BIRT",todos);
	outputEventRow(tempIndi,"BAPM",todos);
	outputEventRow(tempIndi,"DEAT",todos);
	outputEventRow(tempIndi,"BURI",todos);
	tempFam = (tempIndi == null)?null:tempIndi.getFamilyWhereBiologicalChild();
	if (tempFam != null) {
	    output.print(output.row(output.cell(translate("father")+":","head3")+
				    output.cell(getIndiString(tempFam.getHusband()),0,5)));
	    output.print(output.row(output.cell(translate("mother")+":","head3")+
				    output.cell(getIndiString(tempFam.getWife()),0,5)));
	}

	////// Epouse
	tempIndi = fam.getWife();
	output.print(output.row(output.cell(Gedcom.getName("WIFE"),"head2")+
				output.cell(getFormattedName(tempIndi),"head2",0,5)));
	outputEventRow(tempIndi,"BIRT",todos);
	outputEventRow(tempIndi,"BAPM",todos);
	outputEventRow(tempIndi,"DEAT",todos);
	outputEventRow(tempIndi,"BURI",todos);
	tempFam = (tempIndi == null)?null:tempIndi.getFamilyWhereBiologicalChild();
	if (tempFam != null) {
	    output.print(output.row(output.cell(translate("father")+":","head3")+
				    output.cell(getIndiString(tempFam.getHusband()),0,5)));
	    output.print(output.row(output.cell(translate("mother")+":","head3")+
				    output.cell(getIndiString(tempFam.getWife()),0,5)));
	}
	outputEventRow(fam,"MARR",todos);

	////// Enfants
	Indi[] children = fam.getChildren();
	if (children.length>0){
	    output.print(output.row(output.cell(Gedcom.getName("CHIL", children.length>1),"head2",0,6)));
	    for (int c = 0; c < children.length; c++) {
		output.print(output.row(output.cell(""+(c+1),"head3")+ output.cell(getIndiString(children[c]),0,5)));
	    }
	}

	/**************** Notes */
	propArray = fam.getProperties("NOTE");
	boolean seenNote=false;
	for (int i = 0; i<propArray.length; i++){
	    String noteString = "";
	    prop = (Property)propArray[i];
	    if (todos.contains(prop)) continue;
	    if (!seenNote) {
		output.print(output.row(output.cell(translate("main.notes"),"head2",0,6)));
		seenNote = true;
	    }
	    noteString = output.cell(outputPropertyValue(prop),0,6);
	    output.print(output.row(noteString));
	}
				  
	/**************** Todos */
	output.print(output.row(output.cell(translate("titletodo"),"head2",0,6)));
	for (int i = 0; i < todos.size(); i++){
	    prop = (Property) todos.get(i);
	    Property parent = prop.getParent();
	    String row;
	    if (parent instanceof Fam) {
		row = output.cell("");
		row += output.cell(outputPropertyValue(prop),0,5);
	    } else {
		row = output.cell(Gedcom.getName(parent.getTag()),"head3-todo");
		row += output.cell(parent.format(PLACE_AND_DATE_FORMAT)+
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
	List todos;
	String tempString = "";

	todos = findProperties(indi);
	if (todos.size() == 0){
	    return;
	}

	output.print(output.row(output.cell(translate("titleindi",new String[]{indi.getName(),indi.getId()}),"head1",0,6)));
	output.print(output.row(output.cell(translate("titleinfosperso"),"head2",0,6)));
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
	    output.print(output.row(output.cell(translate("father")+":","head3")+
				    output.cell(getIndiString(fam.getHusband()),0,5)));
	    output.print(output.row(output.cell(translate("mother")+":","head3")+
				    output.cell(getIndiString(fam.getWife()),0,5)));
	}

	// And we loop through its families
	Fam[] fams = indi.getFamiliesWhereSpouse();
	if (fams.length>0){
	    output.print(output.row(output.cell(Gedcom.getName("FAM", fams.length>1),"head2",0,6)));
	}
	for (int f=0;f<fams.length;f++) {
	    // .. here's the fam and spouse
	    Fam famc = fams[f];
	    Indi spouse= famc.getOtherSpouse(indi);
	    if (spouse != null){
		Indi[] children = famc.getChildren();
		output.print(output.row(output.cell(translate("spouse")+":","head3",children.length+1,1)+
					output.cell(getIndiString(spouse)
						    +output.br()
						    +output.strong(Gedcom.getName("MARR")+" : ")
						    +famc.format("MARR", PLACE_AND_DATE_FORMAT)
						    ,0,5)));
           
		if (children.length>0){
		    output.print(output.row(output.cell(Gedcom.getName("CHIL", children.length>1),"head4",children.length,1)+
					    output.cell(getIndiString(children[0]),0,4)));
		    for (int c = 1; c < children.length; c++) {
			output.print(output.row( output.cell(getIndiString(children[c]),0,4)));
		    }
		}
	    }
	}

	output.print(output.row(output.cell(Gedcom.getName("EVEN", true),"head2",0,6)));

	outputEventRow(indi,"OCCU",todos);
	outputEventRow(indi,"RESI",todos);

	/**************** Notes */
	propArray = indi.getProperties("NOTE");
	boolean seenNote=false;
	for (int i = 0; i<propArray.length; i++){
	    String noteString = "";
	    prop = (Property)propArray[i];
	    if (todos.contains(prop)) continue;
	    if (!seenNote) {
		output.print(output.row(output.cell(translate("main.notes"),"head2",0,6)));
		seenNote = true;
	    }
	    noteString = output.cell(outputPropertyValue(prop),0,6);
	    output.print(output.row(noteString));
	}
				  
	/**************** Todos */
	output.print(output.row(output.cell(translate("titletodo"),"head2",0,6)));
	for (int i = 0; i < todos.size(); i++){
	    prop = (Property) todos.get(i);
	    Property parent = prop.getParent();
	    String row;
	    if (parent instanceof Indi) {
		row = output.cell("");
		row += output.cell(outputPropertyValue(prop),0,5);
	    } else {
		row = output.cell(Gedcom.getName(parent.getTag()),"head3-todo");
		row += output.cell(parent.format(PLACE_AND_DATE_FORMAT)+
				   formatString(output.br(),outputPropertyValue(prop),"")+
				   formatString(output.br(),getPropertyString(prop,prop.getPath().toString()+":REPO"),"")+
				   formatString(output.br(),getPropertyString(prop,prop.getPath().toString()+":NOTE"),"")
				    ,0,5);
	    }
	    output.print(output.row(row));
	}
    }


    private void outputEventRow(Entity indi, String tag, List todos){
		if (indi == null) return;
	Property props[] = indi.getProperties(tag);
	
	if (props == null) return;
	if (props.length == 1)
	    output.print(output.row(output.cell(Gedcom.getName(tag),"head3")+
				    output.cell(indi.format(tag, PLACE_AND_DATE_FORMAT)
						+getNotesString(output.br()+output.strong("Note : "),indi.getProperty(tag),todos),0,5)));
	else if (props.length != 0) {
	    for (int i = 0; i<props.length; i++){
		output.print(output.row(output.cell(Gedcom.getName(tag),"head3")+
					output.cell(props[i].format(PLACE_AND_DATE_FORMAT)+
						    getNotesString(output.br()+output.strong("Note : "),props[i],todos),0,5)));
	    }
	}
    }

    private int exportTodos(Entity[] ents)  {
	List todos;
	boolean isFirstPage=true;
	int nbTodos=0;

	if (!outputSummary) return 0;

	for (int e = 0; e < ents.length; e++) {
	    todos = findProperties(ents[e]);
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
		    row += output.cell(parent.getPropertyDisplayValue("DATE"));
		    row += output.cell(parent.getPropertyDisplayValue("PLAC"));
		}
		row += output.cell((prop.getEntity()).toString());
		row += output.cell(outputPropertyValue(prop));
		output.print(output.row(row));
		nbTodos++;
	    }
	}
	return nbTodos;
    }

    private void exportTodosCsv(Entity[] ents)  {
	List todos;
	boolean isFirstPage=true;

	for (int e = 0; e < ents.length; e++) {
	    todos = findProperties(ents[e]);
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
		    row += ",\""+parent.getPropertyDisplayValue("DATE")+"\"";
		    row += ",\""+parent.getPropertyDisplayValue("PLAC")+"\"";
		}
		row += ",\""+(prop.getEntity()).toString()+"\"";
		row += ",\""+outputPropertyValue(prop)+"\"";
		println(row);
	    }
	}
    }


    private String getNotesString(String prefix, Property prop, List exclude){
	String result = "";
	// prop exists?
	if (prop==null)
	    return "";
	Property[] props = prop.getProperties("NOTE");
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
	birth = indi.format("BIRT", OPTIONS.getBirthSymbol()+" "+PLACE_AND_DATE_FORMAT);
	death = indi.format("DEAT", OPTIONS.getDeathSymbol()+" "+PLACE_AND_DATE_FORMAT);
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

	// patch for NAME
	if (prop instanceof PropertyName)
	    result = ((PropertyName)prop).getName();
	else
	    result = prop.getDisplayValue();
	
	return result.replaceAll("\n",outputSummaryCsv?" ":output.br());
	
	// done
  }

    private String getFormattedName(Indi indi){
	if (indi == null) return "";
	String first = indi.getFirstName();
	String last = indi.getLastName();

	if (first == null) first = "";
	if (last == null) last = "";
	return output.strong(last.toUpperCase())+" "+first;
    }
    
    private List findProperties(Property of) {
      return of.findProperties(Pattern.compile(todoTag), Pattern.compile(todoStart+".*", Pattern.DOTALL));
    }


} //ReportToDo
