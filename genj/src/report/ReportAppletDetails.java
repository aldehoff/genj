/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.*;
import genj.report.*;

import java.io.*;
import java.util.StringTokenizer;
import java.util.ArrayList;

/**
 * GenJ - Report
 * $Header: /cygdrive/c/temp/cvs/genj/genj/src/report/ReportAppletDetails.java,v 1.13 2002-08-21 21:29:47 nmeier Exp $
 * @author Nils Meier <nils@meiers.net>
 * @version 0.1
 */
public class ReportAppletDetails implements Report {

  /** buffer size for read/write operation on images */
  private static final int IMG_BUFFER_SIZE = 1024;

  /** buffer for read/write operation on images */
  private byte[] imgBuffer = new byte[IMG_BUFFER_SIZE];

  /** maximum text-length of properties */
  public final static int MAX_LINE_LENGTH = 40;

  /** this report's version */
  public static final String VERSION = "0.1";

  /**
   * Returns the version of this script
   */
  public String getVersion() {
    return VERSION;
  }

  private ArrayList propertiesToLink;
  
  /**
   * Returns the name of this report - should be localized.
   */
  public String getName() {
    return "Applet Details";
  }

  /**
   * Some information about this report
   * @return Information as String
   */
  public String getInfo() {
    return "This report creates HTML-files for all individuals and families. "
      + "These files contain details that are normally shown in the "
      + "application's EditView.\n\n"
      + "It also copies one existing image (OBJE:FILE) that is shown in the "
      + "entity's detail-page.\n\n"
      + "For multiline details (e.g., NOTE), any line break that is specified "
      + "will be preserved in the HTML file.  Lines in the HTML file will "
      + "wrap at 40 characters of length.  This is likely a different size "
      + "than the text box in the application's EditView.\n\n" 
      + "To use start this report and choose an existing folder that will "
      + "hold the generated HTML files and images. This folder and its content "
      + "will have to be transferred manually to your website running the applet.\n\n"
      + "Then by specifying the parameter DETAILS you tell the applet to "
      + "open a new browser window with that information if an entity is "
      + "selected and the appropriate button pressed in the TreeView.\n\n"
      + "Example:\n"
      + " <applet code=... > \n"
      + "  <param name=GEDCOM value=...> \n"
      + "  <param name=DETAIL value=\"./details\"> \n"
      + " </applet> \n"
      + "Make sure that specified path is valid relative to the document "
      + "containing the Applet.";
  }  

  /**
   * Exports the given entity to given directory
   */
  private void export(Entity ent, File dir, PrintWriter out) throws IOException {
    propertiesToLink = new ArrayList();
    propertiesToLink.add("FAMS");
    propertiesToLink.add("FAMC");
    propertiesToLink.add("HUSB");
    propertiesToLink.add("WIFE");
    propertiesToLink.add("CHIL");

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
    exportProperty(ent.getProperty(),out,0);
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

    String url = "";

    // Does that entity have an image?
    TagPath path = new TagPath("OBJE:FILE");
    Property prop = ent.getProperty().getProperty(path,true);
    if ( (prop!=null) && (prop instanceof PropertyFile) ) {
      url = exportImage( (PropertyFile) prop, dir , ent.getId());

      // Here comes the IMG-tag
      out.println("<IMG src=\""+url+"\"></IMG>");
    }
    else {
      out.println("No image<Br>available.");
    }
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

    String value = "";
    if (prop.isMultiLine() != Property.NO_MULTI) {
      Property.LineIterator li = prop.getLineIterator();
      while (li.hasMoreValues()) {
        value += li.getNextValue() + '\n';
      }
    } else {
      value = prop.getValue();
    }
    
    String tag = prop.getTag();
    if (tag.equals("NAME")) {
      // Replace /Last/ with LAST
      int slashPos = value.indexOf('/');
      if (slashPos >= 0) {
        String first = value.substring(0, slashPos);
        String last = value.substring(slashPos + 1, value.length() - 1);
        value = first + last.toUpperCase();
      }
    }

    if ( propertiesToLink.contains(tag) ) {
      String idStr = value.replace('@',' ').trim();
      if ( !(prop instanceof PropertyFam) ) {
        try {
          Indi individual = prop.getGedcom().getIndiFromId(idStr);
          if ( individual != null ) {
            value = individual.getName();
          }
        }
        catch ( genj.gedcom.DuplicateIDException x ) {
          // ignore exception
        }
      }
      out.println("<tr><td valign=TOP><b><u>"
                  + Gedcom.getResources().getString(prop.getTag() + ".name")
                  + "</u></b></td><td>"
                  + "<A HREF=\"" + idStr + ".html\">"
                  + value
                  + "</a></td></tr>");
    }
    else {
      exportProperty(tag, value, out, level);
    }

    for (int i=0;i<prop.getNoOfProperties();i++) {
      exportProperty(prop.getProperty(i), out, level+1);
    }

  }

  /**
   * Exports the given property
   */
  private void exportProperty(String tag, String value, PrintWriter out, int level) {

    String spanColumns = "";
    if ( (level == 0) && (value.length() == 0) ) {
      spanColumns = "colspan = 2";
    }

    out.print("<tr><td valign=TOP " + spanColumns + ">");

    // a loop for multi lines
    exportSpaces(out, level);
    
    
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

    out.print(markupBeg + 
              Gedcom.getResources().getString(tag + ".name") +
              markupEnd + "</td><td><pre>");

    value = value.trim();

    // below conditional is an optimization, but is not logically necessary.
    if (value.length() <= MAX_LINE_LENGTH) {
      out.println(value);
      out.println("</pre></td></tr>");
      return;
    }

    // This big string tokenization mess is so we breaks lines on word
    // boundaries and newline characters.
    boolean first = true;
    StringTokenizer linetok = new StringTokenizer(value, "\n");
    while (linetok.hasMoreTokens()) {
      StringTokenizer wordtok = new StringTokenizer(linetok.nextToken());
      String line = wordtok.nextToken() + " ";
      final String padding = "     ";
      while (wordtok.hasMoreTokens()) {
        String next = wordtok.nextToken();
        if (line.length() + next.length() > MAX_LINE_LENGTH) {
          out.println(line);
          line = "";
        }
        line += next + " ";
      }
    
      if (line.length() > 0) {
        out.println(line);
        line = "";
      }
    }
    out.println("</pre></td></tr>");
    // Done
  }

  /**
   * Generates Spaces
   */
  public void exportSpaces(PrintWriter out, int num) {
    for (int c=0;c<num;c++) {
      out.print("&nbsp;");
    }
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
   * Helper that resolves a filename for given entity
   */
  public static File getFileForEntity(File dir, Entity entity) {
    return new File(dir, entity.getId()+".html");
  }

  /**
   * Exports the given entity to given directory
   */
  private void export(Entity ent, File dir, ReportBridge bridge) throws IOException {

    bridge.println("Exporting "+ent);

    File file = getFileForEntity(dir, ent);

    PrintWriter htmlOut = new PrintWriter(new FileOutputStream(file));

    export(ent, dir, htmlOut);

    if (htmlOut.checkError()) {
      throw new IOException("IOError while writing "+ent);
    }

    htmlOut.close();

  }

  /**
   * Exports the given entities to given directory
   */
  private void export(EntityList ents, File dir, ReportBridge bridge)  throws IOException {
    for (int e=0;e<ents.getSize();e++) {
      export(ents.get(e),dir,bridge);
    }
  }

  /**
   * This method actually starts this report
   */
  public boolean start(ReportBridge bridge, Gedcom gedcom) {

    // Get a directory to write to
    File dir = bridge.getDirectoryFromUser("Details export directory", "Choose");
    if (dir==null) {
      bridge.println("Aborting ...");
      return false;
    }
    bridge.println("O.K. I'll create the HTML files for the applet in "+dir);

    // Loop through individuals & families
    try {
      export(gedcom.getEntities(gedcom.INDIVIDUALS),dir,bridge);
      export(gedcom.getEntities(gedcom.FAMILIES   ),dir,bridge);
    } catch (IOException e) {
      bridge.println("IOError while exporting :(");
    }

    // Done
    return true;
  }

}
