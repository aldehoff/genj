/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.report.Report;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * GenJ - Report to run an external program
 * @author Nils Meier nils@meiers.net
 * @version 0.1
 */
public class ReportExec extends Report {

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
    return i18n("info");
  }

  /**
   * @see genj.report.Report#getName()
   */
  public String getName() {
    return i18n("script_name");
  }

  /**
   * @see genj.report.Report#getVersion()
   */
  public String getVersion() {
    return "0.1";
  }

  /**
   * @see genj.report.Report#start(java.lang.Object)
   */
  public void start(Object context) {
    
    // get the name of the executable
    String cmd = getValueFromUser( "executables", i18n("WhichExecutable"), new String[0]);

    if(cmd == null) 
      return;
    
    // run it
    try {
      Process process = Runtime.getRuntime().exec(cmd);
      BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
      while (true) {
        String line = in.readLine();
        if (line==null) break;
        println(line);
      }
    } catch (IOException ioe) {
      println(i18n("Error")+ioe.getMessage());
    }
    
    // done
  }

  /**
   * @see genj.report.Report#usesStandardOut()
   */
  public boolean usesStandardOut() {
    return true;
  }

} //ReportExec
