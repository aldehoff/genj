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
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * GenJ - Report
 * @author Nils Meier <nils@meiers.net>
 * @version 1.0
 */
public class ReportHTMLSheets extends Report {

  /** buffer size for read/write operation on images */
  private static final int IMG_BUFFER_SIZE = 1024;

  /** buffer for read/write operation on images */
  private byte[] imgBuffer = new byte[IMG_BUFFER_SIZE];

  /**
   * Exports the given entity to given directory
   */
  private void export(Entity ent, File dir, PrintWriter out) throws IOException {

    // HEAD
    out.println("<HTML>");
    out.println("<HEAD>");
    out.println("<TITLE>"+ent+"</TITLE>");
    out.println("</HEAD>");
    out.println("<BODY bgcolor=\"#ffffff\">");

    // TABLE
    out.println("<TABLE border=\"1\" cellspacing=\"1\" width=\"100%\">");
    out.println("<TR>");
    out.println("<TD colspan=\"2\" bgcolor=\"f0f0f0\">"+ent+"</TD>");
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
    out.println("</BODY>");
    out.println("</HTML>");
    
    // Done
  }    

  /**
   * Exports the given entity's image
   */
  private void exportImage(Entity ent, File dir, PrintWriter out) throws IOException {

    String url = null;

    // Does that entity have a multimedia object associated with it?
    PropertyFile file = (PropertyFile)ent.getProperty(new TagPath("INDI:OBJE:FILE"));
    if (file!=null) { 
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
    if (!showValue) out.print("colspan=2");
    out.print(">");
    
    exportSpaces(out, level);
    
    out.print(markupBeg);
    StringTokenizer tag = new StringTokenizer(Gedcom.getName(prop.getTag()), " ");
    while (tag.hasMoreElements())
      out.print(tag.nextToken()+"&nbsp;");
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

    // assuming simple value 
    String value = prop.getValue().trim();

    // patch for NAME
    if (prop instanceof PropertyName)
      value = ((PropertyName)prop).getName();

    out.print(value);
      
    // done
  }

  /**
   * Generates Spaces
   */
  private void exportSpaces(PrintWriter out, int num) {
    for (int c=0;c<num;c++) {
      out.print("&nbsp;");
    }
  }

  /**
   * Author
   */
  public String getAuthor() {
    return "Nils Meier <nils@meiers.net>";
  }  

  /**
   * Helper that resolves a filename for given entity
   */
  public static File getFileForEntity(File dir, Entity entity) {
    return new File(dir, entity.getId()+".html");
  }

  /**
   * Exports the given entity to given directory
   */
  private void export(Entity ent, File dir) throws IOException {

    File file = getFileForEntity(dir, ent);

    println(i18n("exporting", new String[]{ ent.toString(), file.getName() }));

    PrintWriter htmlOut = new PrintWriter(new FileOutputStream(file));

    export(ent, dir, htmlOut);

    if (htmlOut.checkError()) 
      throw new IOException("IOError while writing "+ent);

    htmlOut.close();

  }

  /**
   * Exports the given entities to given directory
   */
  private void export(Collection ents, File dir)  throws IOException {
    for (Iterator it=ents.iterator();it.hasNext();)
      export((Entity)it.next(),dir);
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
      export(gedcom.getEntities(gedcom.INDI),dir);
      export(gedcom.getEntities(gedcom.FAM ),dir);
    } catch (IOException e) {
      println("IOError while exporting :(");
    }

    // Done
  }

} //ReportHTMLSheets
