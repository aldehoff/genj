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

/**
 * GenJ - Report
 * @author Nils Meier nils@meiers.net
 * @version 0.1
 */
public class ReportAppletDetails implements Report {

  /** buffer size for read/write operation on images */
  private static final int IMG_BUFFER_SIZE = 1024;

  /** buffer for read/write operation on images */
  private byte[] imgBuffer = new byte[IMG_BUFFER_SIZE];

  /** maximum text-length of properties */
  public final static int MAX_LINE_LENGTH = 40;

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
    return "This report creates HTML-files for all individuals and families. These files "+
           "contain details that are normally shown in the application's EditView.\n" +
           "It also copies one existing image (OBJE:FILE) that is shown in the entity's "+
           "detail-page.\n"+
           "Then by specifying the parameter DETAILS you tell the applet to open a new " +
           "browser window with that information if an entity is selected. Example:\n" +
           " <applet code=... > \n" +
           " <param name=GEDCOM value=...> \n" +
           " <param name=ZIP    value=...> \n" +
           " <param name=DETAIL value=\"./details\"> \n" +
           " </applet> \n" +
           "Make sure that the generated files are present in the specified directory.";
  }  

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
    out.println("<TD width=\"50%\" valign=\"top\" align=\"left\"><PRE>");
    exportProperty(ent.getProperty(),out,0);
    out.println("</PRE></TD>");

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
    }

    // Here comes the IMG-tag
    out.println("<IMG src=\""+url+"\"></IMG>");
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
        value += li.getNextValue() + ' ';
      }
    } else {
      value = prop.getValue();
    }

    exportProperty(prop.getTag(), value, out, level);

    ReferencePropertySet props = prop.getProperties();
    for (int i=0;i<props.getSize();i++) {
      exportProperty(props.get(i), out, level+1);
    }

  }

  /**
   * Exports the given property
   */
  private void exportProperty(String tag, String value, PrintWriter out, int level) {

    // a loop for multi lines
    while (true) {

      exportSpaces(out, level);

      out.print(tag + " ");

      value = value.trim();

      if (value.length() <= MAX_LINE_LENGTH) {
        out.println(value);
        break;
      }

      out.println(value.substring(0,MAX_LINE_LENGTH));

      value = value.substring(MAX_LINE_LENGTH);

      tag = "    ";
    }

    // Done
  }

  /**
   * Generates Spaces
   */
  public void exportSpaces(PrintWriter out, int num) {
    for (int c=0;c<num;c++) {
      out.print(" ");
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
   * Tells wether this report doesn't change information in the Gedcom-file
   */
  public boolean isReadOnly() {
    return true;
  }

  /**
   * Exports the given entity to given directory
   */
  private void export(Entity ent, File dir, ReportBridge bridge) throws IOException {

    bridge.println("Exporting "+ent);

    File file = new File(dir, ent.getId()+".html" );

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
