/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */


import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.TagPath;
import genj.report.Report;

// import tree.ReportGraphicalTree;

/**
 * @author Ekran, based on work of Carsten Muessig <carsten.muessig@gmx.net>
 * @version $Revision: 1.2 $
 * @modified by $Author: lukas0815 $, Ekran
 * updated   = $Date: 2009-06-09 21:12:00 $
 */

public class ReportFamilyTex extends Report {

    public boolean reportParents = true;
    public boolean reportOtherSpouses = true;
    public boolean reportDetailedChildrenData = true;


    public boolean reportNumberFamilies = true;
    public boolean reportPages = false;
	public boolean reportNumberIndi = true;
	public boolean reportSymbolFamilies = true;
	public boolean reportSymbolIndi = true;
	public boolean reportNoteFam    = true;
	public boolean reportNoteIndi   = false;
	public boolean reportNoteDeath  = true;
	public boolean reportNoteBirth  = true;
	public boolean reportDetailOccupation  = true;
	public boolean reportFamiliyImage  = false;
	public boolean reportTexHeader  = false;

    /**
     * Main for argument Gedcom
     */
    public void start(Gedcom gedcom) {


	  // Header for TEX File
	  if (reportTexHeader == true){
		  println("\\documentclass[10pt,a4paper]{article}");
		  println("\\usepackage[T1]{fontenc}");
		  println("\\usepackage[latin1]{inputenc}");
		  println("\\usepackage{float}" );
		  println("\\usepackage{ngerman} % or use: \\usepackage[francais]{babel}");
		  println("\\usepackage[pdftex]{graphicx}");
		  println("\\DeclareGraphicsExtensions{.jpg,.pdf,.png} % for pdflatex");
		  println("\\usepackage{subfig} % for subfloat");
		  println("\\usepackage{fancybox}");
		  println("\\usepackage[	pdftex,");
		  println("		colorlinks,");
		  println("		linkcolor=black]");
		  println("		{hyperref}");
		  println("\n\\newcommand{\\Bild}[3]{%");
		  println("\\begin{figure}[H]%");
		  println("\\includegraphics[width=120mm]{#2}%");
		  println("\\caption{#3 \\\\ {\\tiny (#2)}}%");
		  println("\\label{pic_#1}%\n\\end{figure}%\n}");
		  println("\n\\newcommand{\\Bildh}[3]{%");
		  println("\\begin{figure}[H]%");
		  println("\\includegraphics[height=160mm]{#2}%");
		  println("\\caption{#3 \\\\ {\\tiny (#2)}}%");
		  println("\\label{pic_#1}%");
		  println("\\end{figure}%");
		  println("}");
		  println("\n%Notes are from 4 sources: Birth, death, persons, families");
		  println("%with the next options you can select how the notes are printed");
		  println("%\\newcommand{ \\NoteBirth }[1]{ \\footnote{ #1 } }");
		  println("\\newcommand{ \\NoteBirth }[1]{, Notiz: #1 }");
		  println("%\\newcommand{ \\NoteBirth }[1]{ \\\\ \\leftskip=12mm Notiz: #1 }");
		  println("\n%\\newcommand{ \\NoteDeath }[1]{ \\footnote{ #1 } }");
		  println("\\newcommand{ \\NoteDeath }[1]{, Notiz: #1  }");
		  println("\n%\\newcommand{ \\NoteIndi  }[1]{ \\footnote{ #1 } }");
		  println("\\newcommand{ \\NoteIndi  }[1]{ \\\\ \\leftskip=12mm Notiz: #1 }");
		  println("\n\\newcommand{ \\NoteFam   }[1]{ \\\\ \\leftskip=0mm Notiz zur Familie: #1 }");
		  println("% \\newcommand{ \\NoteFam   }[1]{ \\footnote{ #1 } }");
		  println("\n%\\newcommand{\\zeile}[2]{\\hspace*{#1}\\begin{minipage}[t]{\\textwidth} #2 \\end{minipage}\\\\}");
		  println("\n\\begin{document}");
		  println("");
		  println("\n\n\\title{Title}");
		  println("\\author{your name \\and your helper}");
		  println("% \\thanks{to all suporter}");
		  println("\\date{ \\today }\n\n\\restylefloat{figure}");
		  println("\n\\maketitle");
		  println("\n\\section{Introduction}\nsome words ...");
		  println("\nEin Inhaltsverzeichnis der Familien gibt es am Ende der Datei.");
		  println("\n\\subsection{Used symbols}");
		  println("The following symbols are used for the events:\\\\");
		  println("* - Birth \\\\");
		  println("+ - Death \\\\");
		  println("oo - Marriage \\\\");
		  println("/ - Child in Family\\\\");
		  println("\\lbrack \\rbrack - Baptism \\\\");
		  println("\n\\section{Families}");
		  println("\n\n\\parindent0mm");
	  }

      Entity[] fams = gedcom.getEntities(Gedcom.FAM,"");
      for(int i=0; i<fams.length; i++) {
          analyzeFam((Fam)fams[i]);
      }
	  // Footer for TEX File
	  if (reportTexHeader == true){
		  println("\\tableofcontents");
		  println("\\end{document}");
	  }

    }

    /**
     * Main for argument Family
     */
    public void start(Fam fam) {
      analyzeFam(fam);
    }

    private String trim(Object o) {
        if(o == null)
            return "";
        return o.toString();
    }

	/**
	 * Function deletes or modify some characters which cause malfunction of tex
	 */
    private String TexEncode(String str) {
        str = str.replaceAll("[_]", "\\_");
        // str = str.replaceAll("\<_", " "); // \<_\([[:Alpha:]]*\)_\>
        str = str.replaceAll("[\"]", "\\grqq ");
        str = str.replaceAll("[&]", "\\\\& ");
        // soll Zeichen & ersetzen
        return str;
    }

	/**
	 * Function prints the command for indent a line
	 */
	private String getIndentTex( int i) {

		// problem: only one line is indented
		String str = "\\leftskip=";
		str = str + (6*(i-1)) + "mm ";

		return str;
	}


	/**
	 * Function prints the command for a note
	 * specify in the header (or tex output) how the note should be shown
	 */
	private String familyNote(Fam f) {
		String str = "";

		for(int n = 0; n < f.getProperties("NOTE").length; n++) {
			str += "\\NoteFam{"+trim(f.getProperties("NOTE")[n])+"}";
		}
        return TexEncode(str);
    }

	/**
	 * Function prints the command for a note
	 * specify in the header (or tex output) how the note should be shown
	 */
	private String BirthNote(Indi i) {
		String str = "";

		// for(int n = 0; n < i.getProperty(new TagPath("INDI:BIRTH:NOTE")).length; n++) { // f.getProperty(new TagPath("FAM:MARR:PLAC"))
			str += trim(i.getProperty(new TagPath("INDI:BIRT:NOTE")));
			if (str.length() <1)
				return "";
			str = "\\NoteBirth{"+ str +"}";
		// }

        return TexEncode(str);
    }

	/**
	 * Function prints the command for a note
	 * specify in the header (or tex output) how the note should be shown
	 */
	private String DeathNote(Indi i) {
		String str = "";

		// for(int n = 0; n < i.getProperty(new TagPath("INDI:BIRTH:NOTE")).length; n++) { // f.getProperty(new TagPath("FAM:MARR:PLAC"))
			str += trim(i.getProperty(new TagPath("INDI:DEAT:NOTE")));
			if (str.length() <1)
				return "";
			str = "\\NoteDeath{"+ str +"}";
		// }
        return TexEncode(str);
    }



	/**
	 * Function prints the names of husband and wife
	 */
	private String familyToString(Fam f) {
		Indi husband = f.getHusband(), wife = f.getWife();
		String str = "\\hyperlink{"+f.getId()+"}{"+(reportNumberFamilies==true?f.getId()+" ":"");

		if(husband!=null)
			str = str + (reportNumberIndi==true?husband:husband.getName());
		if(husband!=null & wife!=null)
			str = str + " + ";
		if(wife!=null)
			str = str + (reportNumberIndi==true?wife:wife.getName());

		if (reportPages)
			str = str + " (Kap. \\ref*{"+f.getId()+"}, S. \\pageref*{"+f.getId()+"})";
		// str = str + (reportPages==false?"":"; (Kap. \\ref*{"+f.getId()+"}, S. \\pageref*{"+f.getId()+"})")+"}"; // Link zu Familie geht �ber gesamte Namen oder nur �ber Fxx Nummer
		str += "}";
        return TexEncode(str);
    }

	/**
	 * Function prints the names of husband and wife as subsection
	 */
    private String familyToStringSubsection(Fam f) {
        Indi husband = f.getHusband(), wife = f.getWife();


        String str = "\\leftskip=0mm \\subsection{"+(reportNumberFamilies==true?f.getId()+" ":"");
        // str += f.getId()+" ";
        if(husband!=null)
            str = str + (reportNumberIndi==true?husband:husband.getName());
        if(husband!=null & wife!=null)
            str=str+" + ";
        if(wife!=null)
            str = str + (reportNumberIndi==true?wife:wife.getName());

        str += "} \n\\hypertarget{"+f.getId()+"}{}\n\\label{"+f.getId()+"}";
        return TexEncode(str);
    }

	/**
	 * Function prints the caption of the picture for each family
	 */

	private String familyImageCaption(Fam f) {
		String str = "\n";
		Indi husband = f.getHusband(), wife = f.getWife();
		str += "Stammbaum der Famile "+(reportNumberFamilies==true?f.getId()+" ":"");
		if(husband!=null)
			str = str + (reportNumberIndi==true?husband:husband.getName());
		if(husband!=null & wife!=null)
			str = str + " " + translate("and") + " ";
		if(wife!=null)
			str = str + (reportNumberIndi==true?wife:wife.getName());

		return str;


	}


	/**
	 * Function prints the picture for each family
	 */
	private String familyImage(Fam f) {
		// Indi husband = f.getHusband();
		Indi husband = f.getHusband(), wife = f.getWife();
		// str += "} \n\\hypertarget{"+f.getId()+"}{}\n\\label{"+f.getId()+"}";
		// \Bild{bild060}{./Schreiter-Bild-060.jpg}{Testunterschrift f�r Bild 60}
		String str = "\n";

		if(husband!=null){

		// str += "\\IfFileExists{"+f.getId()+".pdf}{"; // Picture for family
	// or
		str += "\\IfFileExists{"+husband.getId()+".pdf}{"; // Picture for husband
	// or
		// str += "\\IfFileExists{"+wife.getId()+".pdf}{"; // Picture for wife

		// str += "\\Bild{Bild_"+f.getId()+"}{"+f.getId()+".pdf}{" + familyImageCaption(f) + "}}\n";
		str += "\\Bild{Bild_"+husband.getId()+"}{"+husband.getId()+".pdf}{" + familyImageCaption(f) + "}}\n";
		// str += "\\Bild{Bild_"+wife.getId()+"}{"+wife.getId()+".pdf}{" + familyImageCaption(f) + "}}\n";
	}
        return TexEncode(str);
	}

	/**
	 * Function prints the data for family
	 */
    private void analyzeFam(Fam f) {
		String str = "";
		str += familyToStringSubsection(f);
		if (reportFamiliyImage == true) {str += familyImage(f);}

		println(TexEncode(str));

		str = getIndentTex(1)+familyToString(f);
        if(reportNoteFam) { str += familyNote(f); }
		println(TexEncode(str +"\\par"));

        if( (trim(f.getMarriageDate()).length()>0) || (trim(f.getProperty(new TagPath("FAM:MARR:PLAC"))).length()>0) )
            println(getIndentTex(1)+OPTIONS.getMarriageSymbol()+" "+trim(f.getMarriageDate())+" "+trim(f.getProperty(new TagPath("FAM:MARR:PLAC")))+"\\par");
        analyzeIndi(f.getHusband(), f);
        analyzeIndi(f.getWife(), f);
        analyzeChildren(f);
    }

	/**
	 * Function prints the datas for a person
	 */
    private void analyzeIndi(Indi indi, Fam f) {

        if(indi==null)
            return;

		String str = getIndentTex(2)+(reportNumberIndi==true?indi:indi.getName())+"\\par";

        // println(str.replaceAll("[_]", " "));
        println(TexEncode(str));

        if(reportParents) {
          Fam fam = indi.getFamilyWhereBiologicalChild();
            if(fam!=null)
                println(getIndentTex(3)+OPTIONS.getChildOfSymbol()+" "+familyToString(fam)+"\\par");
        }

        if( (trim(indi.getBirthAsString()).length()>0) || (trim(indi.getProperty(new TagPath("INDI:BIRT:PLAC"))).length()>0) ) {
            str = getIndentTex(3)+OPTIONS.getBirthSymbol()+" "+trim(indi.getBirthAsString())+" "+trim(indi.getProperty(new TagPath("INDI:BIRT:PLAC")));
        	if( reportNoteBirth ) {
				str += BirthNote(indi);
				}
			println(TexEncode(str)+"\\par");
			}

        if(indi.getProperty("DEAT")!=null && ( (trim(indi.getDeathAsString()).length()>0) || (trim(indi.getProperty(new TagPath("INDI:DEAT:PLAC"))).length()>0) ) ) {
            str = getIndentTex(3)+OPTIONS.getDeathSymbol()+" "+trim(indi.getDeathAsString())+" "+trim(indi.getProperty(new TagPath("INDI:DEAT:PLAC")));
        	if( reportNoteDeath ) {
				str += DeathNote(indi);
				}
        	println(TexEncode(str)+"\\par");
		}

        if(reportOtherSpouses) {
            Fam[] families = indi.getFamiliesWhereSpouse();
            if(families.length > 1) {
                println(getIndentTex(3)+translate("otherSpouses")+"\\par");
                for(int i=0; i<families.length; i++) {
                    if(families[i]!=f) {
                        // String str = "";
                        str = "";
                        if((trim(families[i].getMarriageDate()).length()>0) || (trim(families[i].getProperty(new TagPath("FAM:MARR:PLAC"))).length()>0))
                            str = OPTIONS.getMarriageSymbol()+" "+trim(families[i].getMarriageDate())+" "+trim(families[i].getProperty(new TagPath("FAM:MARR:PLAC")))+" ";
                        println(getIndentTex(4)+str+" "+familyToString(families[i])+"\\par");
                    }
                }
            }
        }
        if (reportDetailOccupation & trim(indi.getProperty(new TagPath("INDI:OCCU"))).length()>0) {
			str = getIndentTex(3)+translate("occupation")+": "+ trim(indi.getProperty(new TagPath("INDI:OCCU"))) +"\\par";
			// println(str);
			println(TexEncode(str));
		}
    }

    
	/**
	 * Function prints the data for the children
	 */
    private void analyzeChildren(Fam f) {

        Indi[] children = f.getChildren();
        Indi child;
        Fam[] families;
        Fam family;
		String str = "";

        if(children.length>0)
            println(getIndentTex(2)+translate("children")+"\\par");
        for(int i=0; i<children.length; i++) {
            child = children[i];
            str = getIndentTex(3)+(reportNumberIndi==true?child:child.getName())+"\\par";
            println(TexEncode(str));
            if(reportDetailedChildrenData) {
                if ( (trim(child.getBirthAsString()).length()>0) || (trim(child.getProperty(new TagPath("INDI:BIRT:PLAC"))).length()>0) ) {
                    str = getIndentTex(4)+OPTIONS.getBirthSymbol()+" "+trim(child.getBirthAsString())+" "+trim(child.getProperty(new TagPath("INDI:BIRT:PLAC")));
					if( reportNoteBirth ) {
						str += BirthNote(child);
					}
					str += "\\par";
					println(TexEncode(str));
				}
                printBaptism(child, "BAPM");
                printBaptism(child, "BAPL");
                printBaptism(child, "CHR");
                printBaptism(child, "CHRA");
                families = child.getFamiliesWhereSpouse();
                for(int j=0; j<families.length; j++) {
                    family = (Fam)families[j];
                    // println(getIndentTex(4)+OPTIONS.getMarriageSymbol()+family+" "+trim(family.getMarriageDate())+" "+trim(family.getProperty(new TagPath("FAM:MARR:PLAC")))+"\\par");
                    println(getIndentTex(4)+OPTIONS.getMarriageSymbol()+" "+familyToString(family)+" "+trim(family.getMarriageDate())+" "+trim(family.getProperty(new TagPath("FAM:MARR:PLAC")))+"\\par");
                }
                if (reportDetailOccupation & trim(child.getProperty(new TagPath("INDI:OCCU"))).length()>0) {
					println(TexEncode(getIndentTex(4)+translate("occupation")+": "+ trim(child.getProperty(new TagPath("INDI:OCCU"))) +"\\par"));
				}
                if(child.getProperty("DEAT")!=null && ( (trim(child.getDeathAsString()).length()>0) || (trim(child.getProperty(new TagPath("INDI:DEAT:PLAC"))).length()>0) ) ) {
                    str = getIndentTex(4)+OPTIONS.getDeathSymbol()+" "+trim(child.getDeathAsString())+" "+trim(child.getProperty(new TagPath("INDI:DEAT:PLAC")));
                    if( reportNoteDeath ) {
						str += DeathNote(child);
					}
					str += "\\par";
					println(TexEncode(str));
				}
            }
        }
    }

	/**
	 * Function prints the infos for Baptism
	 */
    private void printBaptism(Indi indi, String tag) {

        if( (indi.getProperty(tag)!=null) && ( (trim(indi.getProperty(new TagPath("INDI:"+tag+":DATE"))).length()>0) || (trim(indi.getProperty(new TagPath("INDI:"+tag+":PLAC"))).length()>0) ) )
            println(getIndentTex(4)+OPTIONS.getBaptismSymbol()+" ("+tag+"): "+trim(indi.getProperty(new TagPath("INDI:"+tag+":DATE")))+" "+trim(indi.getProperty(new TagPath("INDI:"+tag+":PLAC")))+"\\par");
    }

} //ReportFamily
