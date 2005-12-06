/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.report.Report;
import genj.window.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * GenJ - ReportPSCirc
 * adapted from LifeLines ps-fan report
 *
 * TODO:
 * - multipages split (ne pas oublier gsave grestore )
* - intergre la fenetre de choix de fichier
 */
public class ReportLinesFan extends Report {

    private PrintWriter out;
    private final static Charset CHARSET = Charset.forName("ISO-8859-1");
    public int genPerPage = 6;
    public int reportMaxGenerations = 999;


    private Fifo indiList = new Fifo(50);
    
    /**
     * Helper - Create a PrintWriter wrapper for output stream
     */
    private PrintWriter getWriter(OutputStream out) {
	return new PrintWriter(new OutputStreamWriter(out, CHARSET));
    }

    /**
     * Main for argument individual
     */
    public void start(Indi indi) {
	
	try{

    	    File file = getFileFromUser(i18n("output.file"), WindowManager.TXT_OK,true);
	    if (file == null){
		return ;
	    }
/*
      // .. exits ?
      if (file.exists()) {
        int rc = manager.getWindowManager().openDialog(null, title, WindowManager.WARNING_MESSAGE, "File exists. Overwrite?", WindowManager.ACTIONS_YES_NO, ReportView.this);
        if (rc!=0) {
          return;
        }
      }
*/
	    out = getWriter(new FileOutputStream(file));
	    Reader in = new InputStreamReader(getClass().getResourceAsStream("ps-fan.ps"));

	    int c;
	    /*	    out.println("%!PS-Adobe-2.0 EPSF-1.2");
	    out.println("%%BoundingBox:0 0 1100 790");
	    */
	    out.println("/maxlevel "+genPerPage+" def");

	    while ((c = in.read()) != -1)
		out.write(c);
	    in.close();
	}catch(IOException ioe){
	    System.err.println("IO Exception!");
	    ioe.printStackTrace();
	}

	//	indiList.add(indi);
	//indiList.add(new Integer(1));
	indiList.push(indi);
	indiList.push(new Integer(1));

	while (!indiList.isEmpty()){
	    Indi indiIterator = (Indi)(indiList.pop());
	    //	    indiList.remove(0);
	    Integer genIndex = (Integer) (indiList.pop());
	    //indiList.remove(0);
	    if (genIndex != null){
		out.println("gsave");
		pedigree(1,genIndex.intValue(),1,1,indiIterator);
		out.println("showpage");
		out.println("grestore");
	    }
	}
	out.flush();
        out.close();
    }

    private void  pedigree (int in, int gen, int lev, int ah, Indi indi){
	if (indi == null){
	    return;
	}
	if (gen > reportMaxGenerations){ return;}
	out.println("("+fullname(indi,1,1,50)+")");
	if (in < 7) {
	    out.println(" ("+formatEvent(OPTIONS.getBirthSymbol(),indi,"BIRT",Formatter.DATE_FORMAT_LONG,false,0)+")"+
			" ("+formatEvent(OPTIONS.getDeathSymbol(),indi,"DEAT",Formatter.DATE_FORMAT_LONG,false,0)+")");
	}else if (in == 7){
	    out.println(" ("+formatEvent(OPTIONS.getBirthSymbol(),indi,"BIRT",Formatter.DATE_FORMAT_YEAR,false,0)+")"+
			" ("+formatEvent(OPTIONS.getDeathSymbol(),indi,"DEAT",Formatter.DATE_FORMAT_YEAR,false,0)+")");
	} else {
	    out.println(" () () ");
	}
	out.println(" "+(in-1)+
		    " "+(ah-lev)+
		    " i");

        if (in < genPerPage) {
	    // And we loop through its ascendants
	    Fam famc = indi.getFamilyWhereBiologicalChild();
	    if (famc==null) {
		return;
	    }
	    Indi father = famc.getHusband();
	    Indi mother = famc.getWife();
	    pedigree(in+1, gen+1, lev*2, ah*2, father);
	    pedigree(in+1, gen+1, lev*2, ah*2+1, mother);
	} else {
	    if (indi.getFamilyWhereBiologicalChild()!= null){
		indiList.push(indi);
		indiList.push(new Integer(gen));
	    }
	}

    }
    /*
      Fullname returns the name of a person in a variety of formats. 
      If the second parameter is true the surname is shown in upper case; 
      otherwise the surname is as in the record. 
      If the third parameter is true the parts of the name are shown in the order
      as found in the record; otherwise the surname is given first, followed 
      by a comma, followed by the other name parts. 
      The fourth parameter specifies the maximum length field that can be used
      to show the name; various conversions occur 
      if it is necessary to shorten the name to fit this length.
    */
    private String fullname(Indi indi,int isUpper,int type,int length){
	return escapePsString(indi.getName());
    }


    /**
     * return symbol+' '+eventstring if event is not null
     */
    String formatEvent(String symbol, Entity entity, String tag, int formatDate, boolean isPlace, int placeIndex ) {
	String result = Formatter.formatEvent(entity, tag, formatDate, isPlace, placeIndex);
	if (result != null && result.length()!=0){
	    result = symbol + " " + result;
	}
	return escapePsString(result);
    }

    private String escapePsString(String s){
	String result;
	result = s.replaceAll("\\\\","\\\\\\\\");
	result = result.replaceAll("\\(","\\\\(");
	result = result.replaceAll("\\)","\\\\)");
	return result;
    }


} //ReportLinesFan
