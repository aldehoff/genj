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
import genj.window.CloseWindow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * GenJ - Report
 * @author Nils Meier <nils@meiers.net>
 * @version 1.0
 */
public class ReportHTMLSheets extends Report {

  private final static String INDEX = "index.html";
  private final static String NAMES = "names.html";
  private final static Charset UTF8 = Charset.forName("UTF-8");

  /**
   * This is the default stylesheet for the generated report sheets - important
   * elements are
   * 
   * TABLE HEADER:
   * <tr class=header>
   *  ...
   * </tr>
   * 
   * NAME GROUP:
   * <namegroup>
   *  <character>A</character>
   *   <name>Adam<occurances>(<count>4</count>)</occurances></name>
   *   ..
   * </namegroup>
   * 
   * PROPERTY/VALUE
   * <property0>Birth</property0>
   * <property1>Date</property1>
   * <property2>Addr</property2>
   * <property3>City</property3>
   * ...
   * <value>Lohmar</value>
   */
  static final String defaultStylesheet = 
   "body { background: white; }" +
   "a { color: black; text-decoration: none; }" +
   "a:hover { text-decoration: underline; }" +
   "p { color: black; }" +
   "tr.header { background: yellow; }" +
   "footer { display: block; font-size: 10; }" +
   "property0 { font-weight: bold; text-decoration: underline; }" +
   "property1 { margin-left: 1ex; font-style: italic; text-decoration: underline; }" +
   "property2 { margin-left: 2ex; font-style: italic; }" +
   "property3 { margin-left: 3ex; font-style: italic; }" +
   "property4 { margin-left: 4ex; font-style: italic; }" +
   "value { }" +
   "namegroup { display: block; }" +
   "namegroup character { display: block; color: red; font-weight: bold; }" +
   "namegroup name { margin-right: 1ex; font-style: italic; }" +
   "namegroup name occurances { color: 00009c; }" +
   "namegroup name count { color: 3299cc; }";
    

  /** options - style sheet */
  public String css = "./style.css";

  /** options - open file after generation */
  public int openBrowser = 1;
  public String openBrowsers[] = { CloseWindow.TXT_NO , INDEX, NAMES };

  /** A <pre>&nbsp</pre> looks better than an empty String in a HTML cell */
  private final static String SPACE = "&nbsp;";

  /** buffer size for read/write operation on images */
  private static final int IMG_BUFFER_SIZE = 1024;

  /** buffer for read/write operation on images */
  private byte[] imgBuffer = new byte[IMG_BUFFER_SIZE];

  /**
   * Exports the given entity to given directory
   */
  private void export(Entity ent, File dir, PrintWriter out) throws IOException {

    // HEAD
    printOpenHTML(out, ent.getGedcom().getName() + " - " + ent.toString());

    // TABLE
    out.println("<table border=\"1\" cellspacing=\"1\" width=\"100%\">");
    out.println("<tr class=header>");
    out.println("<td colspan=\"2\">"+ent.toString()+"</td>");
    out.println("</tr>");
    out.println("<tr>");

    // Image Column
    out.println("<td width=\"50%\" valign=\"center\" align=\"center\">");
    exportImage(ent,dir,out);
    out.println("</td>");

    // Property Column
    out.println("<td width=\"50%\" valign=\"top\" align=\"left\">");
    out.println("<table border=0>");
    exportProperty(ent,out,0);
    out.println("</table>");
    out.println("</td>");

    // END TABLE
    out.println("</tr>");
    out.println("</table>");
    
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
      Entity ent = xref.getReferencedEntity();
      
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

  private File getFileForNames(File dir) {
    return new File(dir, NAMES);
  }

  private File getFileForName(File dir, String name) {
    return new File(dir, name+".html");
  }
  
  private File getFileForStylesheet(File dir) {
    return css.length()>0 ? new File(dir, css) : null;
  }

  /**
   * Exports the given entity to given directory
   */
  private void exportSheet(Entity ent, File dir) throws IOException {

    File file = getFileForEntity(dir, ent);

    println(i18n("exporting", new String[]{ ent.toString(), file.getName() }));
    
    PrintWriter out = getWriter(new FileOutputStream(file));
    export(ent, dir, out);
    out.close();

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
    printCell(out,indi.getProperty(new TagPath("INDI:BIRT:DATE")));
    printCell(out,indi.getProperty(new TagPath("INDI:BIRT:PLAC")));
    printCell(out,indi.getProperty(new TagPath("INDI:DEAT:DATE")));
    printCell(out,indi.getProperty(new TagPath("INDI:DEAT:PLAC")));

    // done
  }
  
  /** 
   * Exports index.html
   */
  private void exportIndex(Gedcom gedcom, File dir) throws IOException {

    File file = getFileForIndex(dir);
    println(i18n("exporting", new String[]{ file.getName(), dir.toString() }));
    PrintWriter out = getWriter(new FileOutputStream(file));

    // HEAD
    printOpenHTML(out, gedcom.getName());

    // TABLE
    out.println("<table border=1 cellspacing=1>");

    // TABLE HEADER
    out.println("<tr class=header>"); 
    printCell(out, "ID");
    printCell(out, PropertyName.getLabelForLastName());
    printCell(out, PropertyName.getLabelForFirstName());
    printCell(out, PropertySex.TXT_SEX);
    printCell(out, Gedcom.getName("BIRT"));
    printCell(out, Gedcom.getName("PLAC"));
    printCell(out, Gedcom.getName("DEAT"));
    printCell(out, Gedcom.getName("PLAC"));
    out.println("</tr>");  //F. Massonneau 03/04/2002

    // Go through individuals
    Entity[] indis = gedcom.getEntities(Gedcom.INDI, "INDI:NAME");
    for (int i=0;i<indis.length;i++) {
      out.println("<tr>");
      exportIndexRow(out, (Indi)indis[i]);
      out.println("</tr>");
      // .. next individual
    }

    // END TABLE
    out.println("</table>");

    // TAIL
    printCloseHTML(out);

    // done
    out.close();
    
  }

  /**
   * Exports names of individuals into a names page
   */
  private void exportNames(Gedcom gedcom, File dir) throws IOException {
    
    File file = getFileForNames(dir);
    
    println(i18n("exporting", new String[]{ file.getName(), dir.toString() }));

    PrintWriter out = getWriter(new FileOutputStream(file));

    // HEAD
    printOpenHTML(out, gedcom.getName());
    
    // Create link for each last name
    out.println("<namegroup>");
    Iterator it = PropertyName.getLastNames(gedcom, true).iterator();
    char last = ' ';
    while (it.hasNext()) {
      // create new name class (first char) if necessary
      String name = it.next().toString();
      if (name.length()>0&&Character.toUpperCase(name.charAt(0))!=last) {
        last = Character.toUpperCase(name.charAt(0));
        out.println("</namegroup>");
        out.println("<namegroup>");
        out.println("<character>"+last+"</character>");
      }
      // create link to name file
      out.print("<name>");
      out.print("<a href=\""+getFileForIndex(dir).getName()+'#'+name+"\">"+name+"</a>");
      out.print("<occurances>(<count>"+PropertyName.getLastNameCount(gedcom, name)+"</count>)</occurances>");
      out.print("</name>");
    }
    out.println("</namegroup>");

    // TAIL
    printCloseHTML(out);

    // done
    out.close();
    
  }
  
  /**
   * Export a stylesheet - if not there already
   */
  private void exportStylesheet(File dir) throws IOException {

    File file = getFileForStylesheet(dir);
    if (file==null||file.exists())
      return;
    
    println(i18n("exporting", new String[]{ file.getName(), dir.toString() }));
    PrintWriter out = getWriter(new FileOutputStream(file));
    StringTokenizer tokens = new StringTokenizer(defaultStylesheet, "}", true);
    while (tokens.hasMoreTokens()) {
      String token = tokens.nextToken();
      out.print(token);
      if (token.equals("}"))
        out.println();
    }
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
    File dir = getDirectoryFromUser(i18n("target.dir"), CloseWindow.TXT_OK);
    if (dir==null)
      return;

    println(i18n("target.dir")+" = "+dir);
    
    // Make sure directory is there
    if (!dir.exists()&&!dir.mkdirs()) {
      println("***Couldn't create output directory "+dir);
      return;
    }

    // Loop through individuals & families
    try {
      exportSheets(gedcom.getEntities(Gedcom.INDI),dir);
      exportSheets(gedcom.getEntities(Gedcom.FAM ),dir);
      exportIndex(gedcom, dir);
      exportNames(gedcom, dir);
      exportStylesheet(dir);
    } catch (IOException e) {
      println("Error while exporting: "+e.getMessage());
    }
    
    // Bring up the result
    try {
      switch (openBrowser) {
        case 0: break;
        case 1: showBrowserToUser(getFileForIndex(dir).toURL());
        case 2: showBrowserToUser(getFileForNames(dir).toURL());
      }
    } catch (MalformedURLException e) {
      // shouldn't happen
    }

    // Done
  }

  /**
   * Helper - Create a PrintWriter wrapper for output stream
   */
  private PrintWriter getWriter(OutputStream out) {
    return new PrintWriter(new OutputStreamWriter(out, UTF8));
  }

  /**
   * Helper - Calculate a url for individual's id 
   */
  private String wrapID(Indi indi) {
    StringBuffer result = new StringBuffer();
    result.append("<a name=\"");
    result.append(indi.getLastName());
    result.append("\"/>");

    result.append("<a href=\"");
    result.append(getFileForEntity(null, indi).getName());
    result.append("\">");
    result.append(indi.getId());
    result.append("</a>");
    return result.toString();
  }
  
  /**
   * Helper - Writes HTML table cell information
   */
  private void printCell(PrintWriter out, Object content) {

  
    // We ask a property for it's value instead of just toString()
    if (content instanceof Property) 
      content = ((Property)content).toString();
      
    // We don't want to see 'null' but ''
    if (content == null || content.toString().length() == 0)
      content = SPACE;

    // Here comes the HTML
    out.println("<td>"+content.toString()+"</td>");

  }
  
  /**
   * Helper - Writes HTML header and body information
   */
  private void printOpenHTML(PrintWriter out, String title) {
    
    // HEAD
    out.println("<html>");
    out.println("<head>");
    out.println("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" >");
    out.println("<title>"+title+"</title>");
    if (css.length()>0)
      out.println("<link rel=StyleSheet href=\""+css+"\" type=\"text/css\"></link>");
    
    out.println("</head>");
    out.println("<body>");
    
    // done
  }

  /**
   * Helper - Writes HTML end header and end body information
   */
  private void printCloseHTML(PrintWriter out) {
    out.print("<footer>");
    out.print("<a href=\""+INDEX+"\">Index</a>");
    out.print(" ");
    out.print("<a href=\""+NAMES+"\">Names</a>");
    out.println("</footer>");
    
    out.println("</body>");
    out.println("</html>");
  }

} //ReportHTMLSheets
