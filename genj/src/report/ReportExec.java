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
 * GenJ - Report to run an external program
 * @author Nils Meier nils@meiers.net
 * @version 0.1
 */
public class ReportExec implements Report {

  /**
   * @see genj.report.Report#getAuthor()
   */
  public String getAuthor() {
    return "Nils Meier <nils@meiers.net>";
  }

  /**
   * @see genj.report.Report#getInfo()
   */
  public String getInfo() {
    return "This report prompts for an executable and runs it";
  }

  /**
   * @see genj.report.Report#getName()
   */
  public String getName() {
    return "Run Executable";
  }

  /**
   * @see genj.report.Report#getVersion()
   */
  public String getVersion() {
    return "0.1";
  }

  /**
   * @see genj.report.Report#isReadOnly()
   */
  public boolean isReadOnly() {
    return true;
  }

  /**
   * @see genj.report.Report#start(ReportBridge, Gedcom)
   */
  public boolean start(ReportBridge bridge, Gedcom gedcom) {
    
    // get the name of the executable
    String cmd = bridge.getValueFromUser("Please enter path and name of the executable to run", new String[0], "executables");
    
    // run it
    try {
      Process process = Runtime.getRuntime().exec(cmd);
      BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
      while (true) {
        String line = in.readLine();
        if (line==null) break;
        bridge.println(line);
      }
    } catch (IOException ioe) {
      bridge.println("*** Sorry, failed with "+ioe.getMessage());
    }
    
    // done
    return true;
  }

  /**
   * @see genj.report.Report#usesStandardOut()
   */
  public boolean usesStandardOut() {
    return true;
  }

}
