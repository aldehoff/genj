/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.*;
import genj.report.*;
import genj.util.Registry;

import java.sql.*;
import java.io.*;
import java.util.*;

/**
 * GenJ - Report
 * @author Nils Meier nils@meiers.net
 * @version 0.1
 */
public class ReportImportDatabase implements Report {

  /** list of predefined drivers */
  private static final String[] drivers =  {
    "sun.jdbc.odbc.JdbcOdbcDriver",
    "oracle.jdbc.driver.OracleDriver"
  };

  /** list of predefined db urls */
  private static final String[] urls =  {
    "jdbc:odbc:GEDCOM",
    "jdbc:oracle:thin:@DB:1521:SID"
  };

  /** list of resources that this script opens */
  private Vector resources = new Vector();

  /** this report's version */
  public static final String VERSION = "0.1";

  /**
   * Returns the version of this script
   */
  public String getVersion() {
    return VERSION;
  }
  
  /**
   * Cleanup leftover resources
   */
  private void cleanup() {

    Enumeration rs = resources.elements();
    while (rs.hasMoreElements()) {

      Object r = rs.nextElement();

      try {
        if (r instanceof Connection) {
          ((Connection)r).close();
        }
        if (r instanceof Statement) {
          ((Statement)r).close();
        }
        if (r instanceof ResultSet) {
          ((ResultSet)r).close();
        }
      } catch (Throwable t) {
      }

    }
  }

  /**
   * Author
   */
  public String getAuthor() {
    return "Nils Meier <nils@meiers.net>";
  }

  /**
   * Helper that opens a database connection with given credentials
   */
  private Connection getConnection(String url, String user, String pass) throws SQLException {
    Connection result = DriverManager.getConnection(url,user,pass);
    resources.addElement(result);
    return result;
  }

  /**
   * Some information about this report
   * @return Information as String
   */
  public String getInfo() {
    return "This report imports Gedcom information from a database. You have "+
           "to make sure that an appropriate database-driver is in your classpath. "+
           "In case of ODBC define a ODBC-data-source pointing to your .mdb-file "+
           "(no driver necessary).\n"+
           "This report asks you for a database-url ... choose one of the shown "+
           "and adopt it depending on your setup.\n"+
           "YOU WILL HAVE TO CHANGE THE CODE TO REFLECT YOUR DATABAS-LAYOUT! "+
           "Open ReportImportDatabase.java and change the method\n\n"+
           "  boolean readEntities(...) {\n"+
           "  }\n\n"+
           "accordingly!";
  }

  /**
   * Returns the name of this report - should be localized.
   */
  public String getName() {
    return "Import Database";
  }

  /**
   * Helper that returns a SQL statement for use on given Connection
   */
  private Statement getStatement(Connection connection) throws SQLException {
    Statement result = connection.createStatement();
    resources.addElement(result);
    return result;
  }

  /**
   * Tells whether this report doesn't change information in the Gedcom-file
   */
  public boolean isReadOnly() {
    return false;
  }

  /**
   * Reads entities into the given Gedcom object - the statement
   * is used to get the data out of an existing database connection.
   * -----------------------------------------------------------
   * You'll have to change this to reflect your database layout!
   * -----------------------------------------------------------
   */
  private boolean readEntities(ReportBridge bridge, Gedcom gedcom, Statement statement) throws GedcomException, SQLException {

    // Constants
    String SELECT   = "select * from ";
    
    String TBL_INDI = "tblIndividual";
    String TBL_FAM  = "tblFamily";

    String COL_INDI = "Individual";
    String COL_NAME = "Surname";
    String COL_FNAME= "Name";
    String COL_SEX  = "Sex";
    String COL_FAMC = "Child";

    String COL_FAM  = "Family";
    String COL_HUSB = "Husband";
    String COL_WIFE = "Wife";

    Vector       xrefs = new Vector(100);
    PropertyXRef xref  = null;

    // Look for Individuals
    ResultSet indiRow = statement.executeQuery(SELECT+TBL_INDI);

    int iCount = 0;
    while (indiRow.next()) {

      String id   = indiRow.getString(COL_INDI );
      String name = indiRow.getString(COL_FNAME) + " /"+indiRow.getString(COL_NAME)+"/";
      String sex  = indiRow.getString(COL_SEX  );
      String famc = indiRow.getString(COL_FAMC );

      Indi indi = gedcom.createIndi(id);
      if (name!=null) {
        indi.addProperty(new PropertyName       ("",name));
      }
      if (sex !=null) {
        indi.addProperty(new PropertySex        ("",sex ));
      }
      if (famc!=null) {
        xref = new PropertyFamilyChild("",famc);
        indi.addProperty(xref);
        xrefs.addElement(xref);
      }

      iCount++;
    }

    bridge.println(" Loaded "+iCount+" Individuals ...");

    // Look for Families
    ResultSet famRow  = statement.executeQuery(SELECT+TBL_FAM );

    int fCount = 0;
    while (famRow.next()) {

      String id   = famRow.getString(COL_FAM );
      String husb = famRow.getString(COL_HUSB);
      String wife = famRow.getString(COL_WIFE);

      Fam fam = gedcom.createFam(id);
      if (husb!=null) {
        xref = new PropertyHusband("",husb);
        fam.addProperty(xref);
        xrefs.addElement(xref);
      }
      if (wife!=null) {
        xref = new PropertyWife   ("",wife);
        fam.addProperty(xref);
        xrefs.addElement(xref);
      }

      fCount++;
    }

    bridge.println(" Loaded "+fCount+" Families...");

    // Link the stuff up
    int xGoodCount = 0;
    int xBadCount  = 0;

    Enumeration erefs = xrefs.elements();
    while (erefs.hasMoreElements()) {
      xref= (PropertyXRef)erefs.nextElement();
      try {
        xref.link();
        xGoodCount++;
      } catch (GedcomException ex) {
        xBadCount++;
      }
    }

    bridge.println(" Linked "+xGoodCount+" References successfully ("+xBadCount+" didn't work)...");

    // Done
    return true;
  }

  /**
   * This method actually starts this report
   */
  public boolean start(ReportBridge bridge, Gedcom gedcom) {

    // Get the database URL and driver name
    String driver = bridge.getValueFromUser("Please enter/choose the database driver", drivers      , "dbdriver");
    if (driver==null) {
      return false;
    }
    String url = bridge.getValueFromUser("Please enter the database url"          , urls         , "dburl" );
    if (url   ==null) {
      return false;
    }
    String user   = bridge.getValueFromUser("Please enter the user name"             , new String[0], "dbuser");
    if (user  ==null) {
      return false;
    }
    String pass   = bridge.getValueFromUser("Please enter the password"              , new String[0], "dbpass");
    if (pass  ==null) {
      return false;
    }

    // Here comes our database working stuff
    boolean success = false;
    try {

      // Register the Driver
      Class.forName(driver);
              
      // Connect to the database
      Properties props = new Properties();
      Connection connection = getConnection(url, user, pass);

      // Read from it
      success = readEntities(bridge,gedcom,getStatement(connection));

      // So much about the database stuff

    } catch (SQLException e) {
      bridge.println(e);
    } catch (Exception e) {
      bridge.println(e);
    } finally {
      cleanup();
    }

    if (success) {
      bridge.showMessageToUser(bridge.INFORMATION_MESSAGE, "Done - please see Output for details!");
    } else {
      bridge.showMessageToUser(bridge.ERROR_MESSAGE      , "An error occurred - please see Output for details!");
    }

    // Done
    return true;
  }
  
  /**
   * Indication of how this reports shows information
   * to the user. Standard Out here only.
   */
  public boolean usesStandardOut() {
          return false;
  }
        
}
