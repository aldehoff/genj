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
import genj.gedcom.MultiLineProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.report.Report;
import genj.window.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * GenJ - Report
 * @author Nils Meier <nils@meiers.net>
 * @version 1.0
 */
public class ReportHTMLSheets extends Report {

  private final static String INDEX = "index.html";

  /** an options - style sheet */
  public String styleSheet = "";

  /** A <pre>&nbsp</pre> looks better than an empty String in a HTML cell */
  private final static String SPACE = "&nbsp;";

  /** whether to translate between unicode and html or not (slow!) */
  public boolean isUnicode2HTML = true;
  
  /** buffer size for read/write operation on images */
  private static final int IMG_BUFFER_SIZE = 1024;

  /** buffer for read/write operation on images */
  private byte[] imgBuffer = new byte[IMG_BUFFER_SIZE];

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

  /**
   * Exports the given entity to given directory
   */
  private void export(Entity ent, File dir, PrintWriter out) throws IOException {

    // HEAD
    printOpenHTML(out, ent.toString());

    // TABLE
    out.println("<TABLE border=\"1\" cellspacing=\"1\" width=\"100%\">");
    out.println("<TR>");
    out.println("<TD colspan=\"2\" bgcolor=\"f0f0f0\">"+wrapText(ent.toString())+"</TD>");
    out.println("</TR>");
    out.println("<TR>");

    // Image Column
    out.println("<TD width=\"50%\" valign=\"center\" align=\"center\">");
    exportImage(ent,dir,out);
    out.println("</TD>");

    // Property Column
    out.println("<TD width=\"50%\" valign=\"top\" align=\"left\">");
    out.println("<TABLE border=0>");
    exportProperty(ent,out,0);
    out.println("</TABLE>");
    out.println("</TD>");

    // END TABLE
    out.println("</TR>");
    out.println("</TABLE>");
    
    // TAIL
    printCloseHTML(out);

    // Done
  }    

  /**
   * Exports the given entity's image
   */
  private void exportImage(Entity ent, File dir, PrintWriter out) throws IOException {

    String url = null;

    // Does that entity have a multimedia object associated with it?
    PropertyFile file = (PropertyFile)ent.getProperty(new TagPath("INDI:OBJE:FILE"));
    if (file!=null&&file.getFile()!=null&&file.getFile().exists()) { 
      url = exportImage( file, dir , ent.getId());
      // Here comes the IMG-tag
      out.println("<IMG src=\""+url+"\"></IMG>");
    }
    if (url==null) out.println(i18n("no.image"));
  }

  /**
   * Exports the given entity's image
   */
  private String exportImage(PropertyFile prop, File dir, String name) throws IOException {

    // Calculate the suffix
    String suffix = null;
    if (prop.getValue().toLowerCase().indexOf("jpg")>0) {
      suffix = ".jpg";
    }
    if (prop.getValue().toLowerCase().indexOf("gif")>0) {
      suffix = ".gif";
    }
    if (prop.getValue().toLowerCase().indexOf("png")>0) {
      suffix = ".png";
    }
    if (suffix==null) {
      return "";
    }

    // Generate the file
    OutputStream imgOut = null;
    InputStream  imgIn  = null;

    try {
      imgOut = new FileOutputStream(new File(dir, name+suffix));
      imgIn = prop.getInputStream();

      while (true) {
        int read = imgIn.read(imgBuffer);
        if (read<=0) {
          break;
        }
        imgOut.write(imgBuffer,0,read);
      }

    } finally {
      // cleanup
      try { imgOut.close(); } catch (Exception e) {};
      try { imgIn .close(); } catch (Exception e) {};
    }

    // Done
    return name+suffix;
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
      if (!(xref.getReferencedEntity() instanceof Indi||xref.getReferencedEntity() instanceof Fam))
        return;
    }

    // calc markup
    String markupBeg;
    String markupEnd;
    if ( level == 0 ) {
      // bold underlined
      markupBeg = "<b><u>";
      markupEnd = "</u></b>";
    }
    else if ( level == 1 ) {
      // italic underlined
      markupBeg = "<i><u>";
      markupEnd = "</u></i>";
    }
    else {
      // italic
      markupBeg = "<i>";
      markupEnd = "</i>";
    }

    // start row
    out.println("<tr>");

    // first column
    boolean showValue = level>0 && !prop.isReadOnly(); 

    out.print("<td valign=TOP ");
    if (!showValue) 
      out.print("colspan=2");
    out.print(">");
    
    exportSpaces(out, level);
    
    out.print(markupBeg);
    StringTokenizer tag = new StringTokenizer(wrapText(Gedcom.getName(prop.getTag())), " ");
    while (tag.hasMoreElements())
      out.print(tag.nextToken()+SPACE);
    out.print(markupEnd);
    out.println("</td>");

    // second column
    if (showValue) {
      out.print("<td>");
      exportPropertyValue(prop, out);
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
      Entity ent = xref.getReferencedEntity();
      
      out.println("<A HREF=\"" + ent.getId() + ".html\">" + wrapText(ent.toString()) + "</a>");
      
      // done
      return;
    } 

    // multiline needs loop
    if (prop instanceof MultiLineProperty) {
      
      MultiLineProperty.Iterator lines = ((MultiLineProperty)prop).getLineIterator();
      do {
        out.print(wrapText(lines.getValue()));
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
      value = wrapText(prop.toString());

    out.print(value);
      
    // done
  }

  /**
   * Generates Spaces
   */
  private void exportSpaces(PrintWriter out, int num) {
    for (int c=0;c<num;c++) {
      out.print(SPACE);
    }
  }

  /**
   * Helper that resolves a filename for given entity
   */
  private File getFileForEntity(File dir, Entity entity) {
    return new File(dir, entity.getId()+".html");
  }
  
  private File getFileForIndex(File dir) {
    return new File(dir, INDEX);
  }

  /**
   * Exports the given entity to given directory
   */
  private void exportSheet(Entity ent, File dir) throws IOException {

    File file = getFileForEntity(dir, ent);

    println(i18n("exporting", new String[]{ ent.toString(), file.getName() }));

    PrintWriter htmlOut = new PrintWriter(new FileOutputStream(file));

    export(ent, dir, htmlOut);

    if (htmlOut.checkError()) 
      throw new IOException("Error while writing "+ent);

    htmlOut.close();

  }

  /**
   * Exports the given entities to given directory
   */
  private void exportSheets(Collection ents, File dir)  throws IOException {
    for (Iterator it=ents.iterator();it.hasNext();)
      exportSheet((Entity)it.next(),dir);
  }
  
  /**
   * Exports index.html row
   */
  private void exportIndexRow(PrintWriter out, Indi indi) throws IOException {

    // Standard
    printCell(out,wrapID(indi));
    printCell(out,indi.getLastName() );
    printCell(out,indi.getFirstName());

    printCell(out,indi.getProperty("SEX",true));
    printCell(out,indi.getProperty(new TagPath("INDI:BIRT:DATE"), Property.QUERY_VALID_TRUE));
    printCell(out,indi.getProperty(new TagPath("INDI:BIRT:PLAC"), Property.QUERY_VALID_TRUE));
    printCell(out,indi.getProperty(new TagPath("INDI:DEAT:DATE"), Property.QUERY_VALID_TRUE));
    printCell(out,indi.getProperty(new TagPath("INDI:DEAT:PLAC"), Property.QUERY_VALID_TRUE));

    // done
  }
  
  /** 
   * Exports index.html
   */
  private void exportIndex(Gedcom gedcom, File dir) throws IOException {

    PrintWriter out = new PrintWriter(new FileOutputStream(getFileForIndex(dir)));

    println(i18n("exporting", new String[]{ "index.html", dir.toString() }));

    // HEAD
    printOpenHTML(out, gedcom.getName());

    // TABLE
    out.println("<TABLE border=1 cellspacing=1>");

    // TABLE HEADER
    out.println("<TR BGCOLOR=\"yellow\">");  //F. Massonneau 03/04/2002
    printCell(out, "ID");
    printCell(out, PropertyName.getLabelForLastName());
    printCell(out, PropertyName.getLabelForFirstName());
    printCell(out, PropertySex.getLabelForSex());
    printCell(out, Gedcom.getName("BIRT"));
    printCell(out, Gedcom.getName("PLAC"));
    printCell(out, Gedcom.getName("DEAT"));
    printCell(out, Gedcom.getName("PLAC"));
    out.println("</TR>");  //F. Massonneau 03/04/2002

    // Go through individuals
    Entity[] indis = gedcom.getEntities(Gedcom.INDI, "INDI:NAME");
    for (int i=0;i<indis.length;i++) {
      out.println("<TR>");
      exportIndexRow(out, (Indi)indis[i]);
      out.println("</TR>");
      // .. next individual
    }

    // END TABLE
    out.println("</TABLE>");

    // TAIL
    printCloseHTML(out);

    // done
    if (out.checkError()) 
      throw new IOException("Error while writing index.html");
    out.close();
    
  }

  /**
   * While we generate information on stdout it's not really
   * necessary because we're bringing up the result in a
   * browser anyways
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

    // Get a directory to write to
    File dir = getDirectoryFromUser(i18n("target.dir"), WindowManager.OPTION_OK);
    if (dir==null)
      return;

    println(i18n("target.dir")+" = "+dir);

    // Loop through individuals & families
    try {
      exportSheets(gedcom.getEntities(gedcom.INDI),dir);
      exportSheets(gedcom.getEntities(gedcom.FAM ),dir);
      exportIndex(gedcom, dir);
    } catch (IOException e) {
      println("Error while exporting: "+e.getMessage());
    }
    
    // Bring up the result
    try {
      showBrowserToUser(getFileForIndex(dir).toURL());
    } catch (MalformedURLException e) {
      // shouldn't happen
    }

    // Done
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
  
  /**
   * Calculate a url for individual's id 
   */
  private String wrapID(Indi indi) {

    return "<a href=\"" + getFileForEntity(null, indi).getName() + "\">" + indi.getId() + "</a>";
  }
  
  /**
   * Helper to make sure that a Unicode text ends up nicely in HTML
   */
  private String wrapText(String text) {

    // no need to transform if !isUnicode2HTML
    if (!isUnicode2HTML) 
      return text;

    // transform characters
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

    // print it
    return result.toString();
  }

  /**
   * Writes HTML table cell information
   */
  private void printCell(PrintWriter out, Object content) {

  
    // We ask a property for it's value instead of just toString()
    if (content instanceof Property) 
      content = ((Property)content).toString();
      
    // We don't want to see 'null' but ''
    if (content == null)
      content = SPACE;

    // Here comes the HTML
    out.println("<TD>"+wrapText(content.toString())+"</TD>");

  }
  
  /**
   * Writes HTML header and body information
   */
  private void printOpenHTML(PrintWriter out, String title) {
    
    // HEAD
    out.println("<HTML>");
    out.println("<HEAD>");
    out.println("<TITLE>"+title+"</TITLE>");
    if (styleSheet.length()>0)
      out.println("<link rel=StyleSheet href=\""+styleSheet+"\" type=\"text/css\"></link>");
    
    out.println("</HEAD>");
    out.println("<BODY bgcolor=\"#ffffff\">");
    
    // done
  }

  /**
   * Writes HTML end header and end body information
   */
  private void printCloseHTML(PrintWriter out) {
    out.println("<a href=\""+INDEX+"\">Index</a>");
    out.println("</BODY>");
    out.println("</HTML>");
  }

} //ReportHTMLSheets
