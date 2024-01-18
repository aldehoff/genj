/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertySex;
import genj.report.Report;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * GenJ - Report.
 * This report exports individuals' information to HTML.
 * $Header: /cygdrive/c/temp/cvs/genj/genj/src/report/ReportMakeHTMLTable.java,v 1.18 2003-06-10 21:52:25 nmeier Exp $
 * @author Nils Meier nils@meiers.net
 * @version $Revision: 1.18 $
 */
public class ReportMakeHTMLTable extends Report {

  /** A <pre>&nbsp</pre> looks better than an empty String in a HTML cell */
  private final static String EMPTY_CELL_STRING = "&nbsp;";

  /** whether to translate between unicode and html or not (slow!) */
  private boolean isTranslateUnicode2HTML = false;

  /** HTML Coded Character Set (see http://www.w3.org/MarkUp/html-spec/html-spec_13.html)*/
  private final static String[] codeTable = {
   "\u00d1" , "&Ntilde;", //    Capital N, tilde
   "\u00d2" , "&Ograve;", //    Capital O, grave accent
   "\u00d3" , "&Oacute;", //    Capital O, acute accent
   "\u00d4" , "&Ocirc;", //     Capital O, circumflex accent
   "\u00d5" , "&Otilde;", //    Capital O, tilde
   "\u00d6" , "&Ouml;", //      Capital O, dieresis or umlaut mark
   "\u00df" , "&szlig;", //     Small sharp s, German (sz ligature)
   "\u00c0" , "&Agrave;", //     Capital A, grave accent
   "\u00c1" , "&Aacute;", //    Capital A, acute accent
   "\u00c2" , "&Acirc;", //     Capital A, circumflex accent
   "\u00c3" , "&Atilde;", //    Capital A, tilde
   "\u00c4" , "&Auml;", //      Capital A, dieresis or umlaut mark
   "\u00c5" , "&Aring;", //     Capital A, ring
   "\u00c6" , "&AElig;", //     Capital AE dipthong (ligature)
   "\u00c7" , "&Ccedil;", //    Capital C, cedilla
   "\u00c8" , "&Egrave;", //    Capital E, grave accent
   "\u00c9" , "&Eacute;", //    Capital E, acute accent
   "\u00ca" , "&Ecirc;", //     Capital E, circumflex accent
   "\u00cb" , "&Euml;", //      Capital E, dieresis or umlaut mark
   "\u00cc" , "&Igrave;", //    Capital I, grave accent
   "\u00cd" , "&Iacute;", //    Capital I, acute accent
   "\u00ce" , "&Icirc;", //     Capital I, circumflex accent
   "\u00cf" , "&Iuml;", //      Capital I, dieresis or umlaut mark
   "\u00d9" , "&Ugrave;", //    Capital U, grave accent
   "\u00da" , "&Uacute;", //    Capital U, acute accent
   "\u00db" , "&Ucirc;", //     Capital U, circumflex accent
   "\u00dc" , "&Uuml;", //      Capital U, dieresis or umlaut mark
   "\u00dd" , "&Yacute;", //    Capital Y, acute accent

   "\u00e0" , "&agrave;", //    Small a, grave accent
   "\u00e1" , "&aacute;", //    Small a, acute accent
   "\u00e2" , "&acirc;", //     Small a, circumflex accent
   "\u00e3" , "&atilde;", //    Small a, tilde
   "\u00e4" , "&auml;", //      Small a, dieresis or umlaut mark
   "\u00e6" , "&aelig;", //     Small ae dipthong (ligature)
   "\u00e7" , "&ccedil;", //    Small c, cedilla
   "\u00e8" , "&egrave;", //    Small e, grave accent
   "\u00e9" , "&eacute;", //    Small e, acute accent
   "\u00ea" , "&ecirc;", //     Small e, circumflex accent
   "\u00eb" , "&euml;", //      Small e, dieresis or umlaut mark
   "\u00ec" , "&igrave;", //    Small i, grave accent
   "\u00ed" , "&iacute;", //    Small i, acute accent
   "\u00ee" , "&icirc;", //     Small i, circumflex accent
   "\u00ef" , "&iuml;", //      Small i, dieresis or umlaut mark
   "\u00f0" , "&eth;", //       Small eth, Icelandic
   "\u00f1" , "&ntilde;", //    Small n, tilde
   "\u00f2" , "&ograve;", //    Small o, grave accent
   "\u00f3" , "&oacute;", //    Small o, acute accent
   "\u00f4" , "&ocirc;", //     Small o, circumflex accent
   "\u00f5" , "&otilde;", //    Small o, tilde
   "\u00f6" , "&ouml;", //      Small o, dieresis or umlaut mark
   "\u00f8" , "&oslash;", //    Small o, slash
   "\u00f9" , "&ugrave;", //    Small u, grave accent
   "\u00fa" , "&uacute;", //    Small u, acute accent
   "\u00fb" , "&ucirc;", //     Small u, circumflex accent
   "\u00fc" , "&uuml;", //      Small u, dieresis or umlaut mark
   "\u00fd" , "&yacute;", //    Small y, acute accent
   "\u00fe" , "&thorn;", //     Small thorn, Icelandic
   "\u00ff" , "&yuml;"  //      Small y, dieresis or umlaut mark
  };

  /** the mapping between unicode and html */
  private static Hashtable unicode2html = initializeUnicodeSupport();

  /** this report's version */
  public static final String VERSION = "1.8";

  /**
   * Returns the version of this script
   */
  public String getVersion() {
    return VERSION;
  }
  
  /**
   * Returns the name of this report - should be localized.
   */
  public String getName() {
    return "Make HTML-Table";
  }

  /**
   * Some information about this report
   * @return Information as String
   */
  public String getInfo() {
    return "This report exports individuals' information to HTML";
  }

  /**
   * Indication of how this reports shows information
   * to the user. Standard Out here only.
   */
  public boolean usesStandardOut() {
    return true;
  }

  /**
   * Author
   */
  public String getAuthor() {
    return "Nils Meier <nils@meiers.net>";
  }

  /**
   * Tells whether this report doesn't change information in the Gedcom-file
   */
  public boolean isReadOnly() {
    return true;
  }

  /**
   * Calculate an individual's id with possible wrapping HREF
   */
  private String calcIndiId(Indi indi) {

//    if ((url==null)||(url.length()==0)) {
      return indi.getId();
//    }
//
//    return "<a href=\"" + url + indi.getId() + ".html\">" + indi.getId() + "</a>";
  }


  /**
   * Export individual's information
   */
  private void export(Indi indi) {

    // Standard
    htmlCell(calcIndiId(indi));
    htmlCell(indi.getLastName() );
    htmlCell(indi.getFirstName());

    switch (indi.getSex()) {
    case PropertySex.MALE:
            htmlCell("Male");
            break;
    case PropertySex.FEMALE:
            htmlCell("Female");
            break;
    default:
            htmlCell(EMPTY_CELL_STRING);
            break;
    }

    // Birth Date
    try {
      // ... this might fail in case we don't get a PropertyEvent back
      PropertyEvent event = (PropertyEvent)indi.getProperty("BIRT");
      PropertyDate date = (PropertyDate)event.getProperty("DATE");
      Property place = event.getProperty("PLAC");
      htmlCell(date);
      htmlCell(place);
    } catch (Exception e) {
      htmlCell(null);
      htmlCell(null);
    }

    try {
      PropertyEvent event = (PropertyEvent)indi.getProperty("DEAT");
      PropertyDate date = (PropertyDate)event.getProperty("DATE");
      Property place = event.getProperty("PLAC");
      htmlCell(date);
      htmlCell(place);
    } catch (Exception e) {
      htmlCell(null);
      htmlCell(null);
    }

    // Done
  }

  /**
   * Writes HTML table cell information
   */
  private void htmlCell(Object content) {

    // We don't want to see 'null' but ''
    if (content == null) {

      content = EMPTY_CELL_STRING;

    } else {

      // We ask a property for it's value instead of just toString()
      if (content instanceof Property) {
        content = ((Property)content).getValue();
      }

      // Check to make sure it's not empty
      if (content.toString().trim().length()==0) {
        content = EMPTY_CELL_STRING;
      }

    }

    // Here comes the HTML
    println("<TD>"+unicode2html(content.toString())+"</TD>");

  }

  /**
   * This method actually starts this report
   */
  public void start(Object context) {
    
    // has to be Gedcom
    Gedcom gedcom = (Gedcom)context;

    // And whether code translation should happen or not
    isTranslateUnicode2HTML = getOptionFromUser(
      "Do you want to transform Unicode- into HTML-codes (slow)?", OPTION_YESNO
    );

    // HEAD
    println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
    println("<HTML>");
    println("<HEAD>");
    println("<TITLE>"+gedcom.getName()+" - HTML Table</TITLE>");
    println("</HEAD>");
    println("<BODY bgcolor=\"#ffffff\">");

    // TABLE
    println("<TABLE border=1 cellspacing=1>");

    // TABLE HEADER
    println("<TR BGCOLOR=\"yellow\">");  //F. Massonneau 03/04/2002
    htmlCell("ID");
    htmlCell("Last Name");
    htmlCell("First Name");
    htmlCell("Sex");
    htmlCell("Birth");
    htmlCell("Place");
    htmlCell("Death");
    htmlCell("Place");
    println("</TR>");  //F. Massonneau 03/04/2002

    // Go through individuals
    List indis = gedcom.getEntities(Gedcom.INDIVIDUALS);

    TreeMap indiMap = new TreeMap ();  // use to sort by name
    for (int i=0;i<indis.size();i++) {
      Indi indi = (Indi)indis.get(i);
      indiMap.put( indi.getLastName() + "\t" + indi.getFirstName() + "\t" + i,
                   indi );
    }
    Iterator iter = indiMap.values().iterator();
    while ( iter.hasNext() ) {
      println("<TR>");

      export((Indi)iter.next());

      println("</TR>");

      // .. next individual
    }

    // END TABLE
    println("</TABLE>");

    // TAIL
    println("</BODY>");
    println("</HTML>");

    // Done
  }

  /**
   * Helper to make sure that a Unicode text ends up nicely in HTML
   */
  private String unicode2html(String text) {

    if (!isTranslateUnicode2HTML) {
      return text;
    }

    StringBuffer result = new StringBuffer(256);
    for (int c=0;c<text.length();c++) {
      String unicode = text.substring(c,c+1);
      Object html    = unicode2html.get(unicode);
      if (html==null) {
        result.append(unicode);
      } else {
        result.append(html);
      }
    }

    return result.toString();
  }

  /**
   * Initializes a Hashtable of Unicode 2 HTML code mappings
   */
  private static Hashtable initializeUnicodeSupport() {

    Hashtable result = new Hashtable();

    // loop and copy
    for (int c=0;c<codeTable.length;c+=2) {
      result.put(
        codeTable[c+0],
        codeTable[c+1]
      );
    }

    // done
    return result;
  }

}
