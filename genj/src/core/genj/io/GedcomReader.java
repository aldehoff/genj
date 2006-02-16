/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.io;

import genj.crypto.Enigma;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Submitter;
import genj.util.Origin;
import genj.util.Resources;
import genj.util.Trackable;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GedcomReader is a custom reader for Gedcom compatible information. Normally
 * it's used by GenJ's application or applet when trying to open a file or
 * simply reading from a stream. This type can be used by 3rd parties that
 * are interested in reading Gedcom into the GenJ object representation as well.
 */
public class GedcomReader implements Trackable {

  private final static Resources resources = Resources.get("genj.io");
  
  private static Logger LOG = Logger.getLogger("genj.io");

  /** estimated average byte size of one entity */
  private final static int ENTITY_AVG_SIZE = 150;
  
  /** stati the reader goes through */
  private final static int READHEADER = 0, READENTITIES = 1, LINKING = 2;

  /** lots of state we keep during reading */
  private Gedcom              gedcom;
  private BufferedReader      in;
  
  private boolean isIndentForLevels = false;
  private int progress;
  private int level = 0;
  private int line = 0;
  private int entity = 0;
  private int read = 0;
  private int state;
  private long length;
  private String gedcomLine;
  private String xref;
  private String tag;
  private String value;
  private boolean redoLine = false;
  private Origin origin;
  private List xrefs = new ArrayList(16);
  private String tempSubmitter;
  private boolean cancel=false;
  private Thread worker;
  private Object lock = new Object();
  
  /** encryption */
  private Enigma enigma;

  /** collecting warnings */
  private List warnings = new ArrayList(128);
  
  /**
   * Read properties from input and create Gedcom properties for given property
   * @param reader where to read from
   * @param parent the parent to add to
   * @param pos the position to add read properties to or -1 for best placement
   * @see genj.gedcom.Property
   */
  public static List read(Reader reader, Property parent, int pos) throws IOException, UnsupportedFlavorException {
    
    // read it through custom reader
    GedcomReader instance = new GedcomReader();
    
    // we expect indent encoded through leading spaced
    instance.isIndentForLevels = true;
    
    // faking a buffered read
    instance.in = new BufferedReader(reader);
    
    // simply read properties into parent
    List result = new ArrayList(16);
    try {
      instance.readProperties(parent, pos, -1, result);
    } catch (GedcomIOException e) {
    }
    
    // link what needs linkage
    instance.linkReferences();

    // done
    return result;
  }
  
  /**
   * Constructor
   */
  private GedcomReader() {
  }
  
  /**
   * Constructor for a reader that reads from given origin
   * @param org the origin of the gedcom stream to read from
   */
  public GedcomReader(Origin org) throws IOException {
    
    LOG.info("Initializing reader for "+org);
    
    // open origin
    InputStream oin = org.open();

    // prepare sniffer
    SniffedInputStream sin = new SniffedInputStream(oin);
    
    // init some data
    in       = new BufferedReader(new InputStreamReader(sin, sin.getCharset()));
    origin   = org;
    length   = oin.available();
    gedcom   = new Gedcom(origin);
    gedcom.setEncoding(sin.getEncoding());
    
    // Done
  }
  
  /**
   * Set password to use to decrypt private properties
   * @param password password to use or Gedcom.PASSWORD_UNKNOWN - if the password
   *  doesn't match a GedcomEncryptionException is thrown during read
   */
  public void setPassword(String password) {
    
    // valid argument?
    if (password==null)
      throw new IllegalArgumentException("Password can't be NULL");
      
    // set it on Gedcom
    gedcom.setPassword(password); 
    
    // done
  }

  /**
   * Thread-safe cancel of read()
   */
  public void cancelTrackable() {

    // Stop it as soon as possible
    cancel=true;
    synchronized (lock) {
      if (worker!=null)
        worker.interrupt();
    }
    // Done
  }

  /**
   * Returns progress of save in %
   * @return percent as 0 to 100
   */
  public int getProgress() {
    return progress;
  }

  /**
   * Returns current read state as explanatory string
   */
  public String getState() {
    switch (state) {
      case READHEADER :
        return resources.getString("progress.read.header");
      case READENTITIES :default:
        return resources.getString("progress.read.entities", new String[]{ ""+line, ""+entity} );
      case LINKING      :
        return resources.getString("progress.read.linking");
    }
  }

  /**
   * Returns warnings encountered while reading
   * @return the warnings as a list of String
   */
  public List getWarnings() {
    return warnings;
  }

  /**
   * Helper to get gedcom-line
   * @exception GedcomIOException reading from <code>BufferedReader</code> failed
   * @exception GedcomFormatException reading Gedcom-data brought up wrong format
   */
  private void peekLine() throws GedcomIOException, GedcomFormatException {
    readLine();
    redoLine();
  }

  /**
   * Put back gedcom-line
   */
  private void redoLine() {
    redoLine = true;
  }
  
  /**
   * Read entity
   * @exception GedcomIOException reading from <code>BufferedReader</code> failed
   * @exception GedcomFormatException reading Gedcom-data brought up wrong format
   */
  private void readEntity() throws GedcomIOException, GedcomFormatException {

    readLine();
    
    // records should start with level 0
    if (level!=0) 
      throw new GedcomFormatException(resources.getString("read.error.nonumber"), line);
    
    // warn about missing xref if it's a well known type
    for (int i=0;i<Gedcom.ENTITIES.length;i++) {
      if (tag.equals(Gedcom.ENTITIES[i])&&xref.length()==0) {
        addWarning(line, resources.getString("read.warn.recordnoid", Gedcom.getName(tag)));
      }
    }

    // Create entity and read its properties
    try {
      
      Entity ent = gedcom.createEntity(tag, xref);
      
      // preserve value for those who care
      ent.setValue(value);
      
      // Read entity's properties till end of record
      readProperties(ent, 0,  0, null);

    } catch (GedcomException ex) {
      skipEntity(ex.getMessage());
    }

    // Done
    entity++;
  }
  
  /**
   * Skip entity
   */
  private void skipEntity(String msg) throws GedcomFormatException, GedcomIOException {
    //  track it
    int start = line;
    try {
      do {
        readLine();
      } while (level!=0);
      redoLine();
    } finally {
      addWarning(start, resources.getString("read.warn.skipped", new String[]{ ""+(line-start), msg } ));
    }
  }

  /**
   * Actually writes the gedcom-information 
   * @exception GedcomIOException reading failed
   * @exception GedcomFormatException reading Gedcom-data brought up wrong format
   * @exception GedcomEncryptionException encountered encrypted property and password didn't match
   */
  public Gedcom read() throws GedcomIOException, GedcomFormatException {

    // Remember working thread
    synchronized (lock) {
      worker=Thread.currentThread();
    }
    
    // try it
    try {
      readGedcom();
      return gedcom;
    } catch (GedcomIOException gex) {
      throw gex;
    } catch (Throwable t) {
      // catch anything bubbling up here
      LOG.log(Level.SEVERE, "unexpected throwable", t);
      throw new GedcomIOException(t.toString(), line);
    } finally  {
      // close in
      try { in.close(); } catch (Throwable t) {};
      // forget working thread
      synchronized (lock) {
        worker=null;
      }
    }

    // nothing happening here
  }
  
  /**
   * Read Gedcom as a whole
   *
   */
  private void readGedcom() throws GedcomIOException, GedcomFormatException {


    // Create Gedcom
    int expected = Math.max((int)length/ENTITY_AVG_SIZE,100);
    xrefs = new ArrayList(expected);

    // Read the Header
    readHeader();

    // Next state
    state++;

    // Read records
    do {
      // .. still there ?
      peekLine();
      if (level!=0) 
        throw new GedcomFormatException(resources.getString("read.error.norecord"),line);

      // .. end ?
      if (tag.equals("TRLR")) break;

      // .. entity to parse
      readEntity();

      // .. next
    } while (true);


    // Read Tail
    readLine();
    if (level!=0) 
      throw new GedcomFormatException(resources.getString("read.error.notrailer"), line);

    // Next state
    state++;

    // Prepare submitter
    if (tempSubmitter!=null) {
      try {
        Submitter sub = (Submitter)gedcom.getEntity(Gedcom.SUBM, tempSubmitter.replace('@',' ').trim());
        gedcom.setSubmitter(sub);
      } catch (IllegalArgumentException t) {
        addWarning(line, resources.getString("read.warn.setsubmitter", tempSubmitter));
      }
    }

    // Link references
    linkReferences();

    // Done
  }

  /**
   * linkage
   */
  private void linkReferences() {

    // loop over kept references
    for (int i=0,j=xrefs.size();i<j;i++) {
      XRef xref = (XRef)xrefs.get(i);
      try {
        if (xref.prop.getTarget()==null)
          xref.prop.link();
        progress = Math.min(100,(int)(i*(100*2)/j));  // 100*2 because Links are probably backref'd
      } catch (GedcomException ex) {
        addWarning(xref.line, resources.getString("read.warn.reference", new String[]{ xref.prop.getTag(), ex.getMessage() }));
      }
    }

    // done
  }
  
  /**
   * Read Header
   * @exception GedcomIOException reading from <code>BufferedReader</code> failed
   * @exception GedcomFormatException reading Gedcom-data brought up wrong format
   */
  private boolean readHeader() throws GedcomIOException, GedcomFormatException {

    //  0 HEAD
    //  1 SOUR GENJ
    //  2 VERS Version.getInstance().toString()
    //  2 NAME GenealogyJ
    //  2 CORP Nils Meier
    //  3 ADDR http://genj.sourceforge.net
    //  1 DEST ANY
    //  1 DATE date
    //  2 TIME time
    //  1 SUBM '@'+gedcom.getSubmitter().getId()+'@'
    //  1 SUBN '@'+gedcom.getSubmission().getId()+'@'
    //  1 GEDC
    //  2 VERS 5.5
    //  2 FORM Lineage-Linked
    //  1 CHAR encoding
    //  1 LANG language
    //  1 PLAC 
    //  2 FORM place format
    //  1 FILE file
    readLine();
    if (level!=0||!tag.equals("HEAD"))
      throw new GedcomFormatException(resources.getString("read.error.noheader"),line);

    String lastTag = "";
    String sourceID = null;
    int gedc = 0;
    do {

      // read until end of header
      readLine();
      if (level==0)
        break;

      // check for SUBMitter
      if (level==1&&"SUBM".equals(tag)) 
        tempSubmitter = value; 
        
      // check for SOUR
      if (level==1&&"SOUR".equals(tag)) 
        sourceID = value; 
        
      // check for GEDC VERSion and FORMat
      if (level==1&&"GEDC".equals(tag)) gedc |= 1;
      if (level==2&&"VERS".equals(tag)) gedc |= 2;
      if (level==2&&"FORM".equals(tag)) gedc |= 4;
        
      // check for language
      if (level==1&&"LANG".equals(tag)&&value.length()>0) {
        gedcom.setLanguage(value);
        LOG.info("Found LANG "+value+" - Locale is "+gedcom.getLocale());
      }
      
      // check for encoding
      if (level==1&&"CHAR".equals(tag)&&"ASCII".equals(value))
        addWarning(line, resources.getString("read.warn.ascii"));
      
      // check for place hierarchy description
      if (level==2&&"PLAC".equals(lastTag)) {
        if ("FORM".equals(tag)) {
          gedcom.setPlaceFormat(value);
          LOG.info("Found Place.Format "+value);
        } else {
          addWarning(line, resources.getString("read.warn.placform"));
        }
      }
        
      lastTag = tag;
      
      // done
    } while (true);
    
    // check for warnings from header
    if (tempSubmitter==null)
      addWarning(line, resources.getString("read.warn.nosubmitter"));
    if (sourceID==null)
      addWarning(line, resources.getString("read.warn.nosourceid"));
    if (gedc!=7)
      addWarning(line, resources.getString("read.warn.badgedc"));
    
    // Last still to be used
    redoLine();

    // Done
    return true;
  }

  /**
   * Helper to get gedcom-line
   * @exception GedcomIOException reading from <code>BufferedReader</code> failed
   * @exception GedcomFormatException reading Gedcom-data brought up wrong format
   */
  private void readLine() throws GedcomIOException, GedcomFormatException {

    // Still running ?
    if (cancel)
      throw new GedcomIOException(resources.getString("read.error.cancelled"), line);
      

    // Still undo we can use?
    if (redoLine) {
      redoLine = false;
      return;
    }

    // .. get new
    try {
      
      // Read
      do {

        line++;
        gedcomLine = in.readLine();
        
        // no more data - where has the trailer gone?
        if (gedcomLine==null) 
          throw new GedcomIOException(resources.getString("read.error.notrailer"), line);
        
        // .. update statistics
        read+=gedcomLine.length()+2;
        if (length>0) {
          progress = Math.min(100,(int)(read*100/length));
        }
        
        // check for empty line now - don't trim though because
        // that's need for indentation level computation and we
        // don't want to take stuff away if we don't have to (trailing spaces)
        if (gedcomLine.trim().length()==0) 
          addWarning(line, resources.getString("read.error.emptyline"));
        
        
      } while ( gedcomLine.length()==0 );

    } catch (Exception ex) {

      // .. cancel
      if (cancel) 
        throw new GedcomIOException(resources.getString("read.error.cancelled"), line);

      // .. file erro
      if (ex instanceof GedcomIOException)
        throw (GedcomIOException)ex;
      
      throw new GedcomIOException(ex.getMessage(), line);
    }

    // Parse gedcom-line 
    // 20040322 use space and also \t for delim in case someone used tabs in file
    StringTokenizer tokens = new StringTokenizer(gedcomLine," \t");

    try {

      // .. caclulate level by looking at spaces or parsing a number
      try {
        if (isIndentForLevels) {
          level = 0;
          while (gedcomLine.charAt(level)==' ') level++;
        } else {
          level = Integer.parseInt(tokens.nextToken(),10);
        }
      } catch (StringIndexOutOfBoundsException sioobe) {
        throw new GedcomFormatException(resources.getString("read.error.emptyline"),line);
      } catch (NumberFormatException nfe) {
        throw new GedcomFormatException(resources.getString("read.error.nonumber"),line);
      }

      // .. tag (?)
      if (tokens.hasMoreTokens()) 
        tag = tokens.nextToken();
      else {
        tag = "_MISSING";
        addWarning(line, resources.getString("read.error.emptyline"));
      }
        
      // .. xref ?
      if (tag.startsWith("@")&&level==0) {

        // .. valid ?
        if (!tag.endsWith("@")||tag.length()<=2)
          throw new GedcomFormatException(resources.getString("read.error.invalidid"),line);
 
        // .. indeed, xref !
        xref = tag.substring(1,tag.length()-1);
        
        // .. tag is the next token
        tag = tokens.nextToken();

      } else {

        // .. no reference in line !
        xref = "";
      }

      // .. value
      if (tokens.hasMoreElements()) {
        // 20030530 o.k. gotta switch to delim "\n" because we want everything 
        // to end of line including contained spaces 
        value = tokens.nextToken("\n");
        // 20030609 strip leading space that forms delimiter to tag/xref
        // (this was trim() once but identified as too greedy)
        if (value.startsWith(" "))
          value = value.substring(1);
      } else {
        value = "";
      }

    } catch (NoSuchElementException ex) {
      // .. not enough tokens
      throw new GedcomFormatException(resources.getString("read.error.cantparse"),line);
    }
    
    // TUNING: for tags we expect a lot of repeating strings (limited numbe of tags) so
    // we build the intern representation of tag here - this makes us share string 
    // instances for an upfront cost
    tag = tag.intern();
    
    // Done
  }

  /**
   * Read propertiees of property
   * @exception GedcomIOException reading from <code>BufferedReader</code> failed
   * @exception GedcomFormatException reading Gedcom-data brought up wrong format
   */
  private void readProperties(Property prop, int pos, int currentlevel, List trackAdded) throws GedcomIOException, GedcomFormatException {

    // read more for multiline property prop?
    if (prop instanceof MultiLineProperty) {

      MultiLineProperty.Collector collector = ((MultiLineProperty)prop).getLineCollector();
      try { 
        
        while (true) {
          
          // check next line
          readLine();
          
          // end of property ?
          if (level<=currentlevel) 
            break;
            
          // can we continue with current?
          if (!collector.append(level-currentlevel, tag, value)) 
            break;
        } 
      
      } finally {
        // commit collected value
        prop.setValue(collector.getValue());
      }

      // redo last line
      redoLine();
      
    }

    // decrypt value now
    decryptLazy(prop);
    
    // Get subs of property
    Property sub;
    do {
  
      // check next line
      readLine();
  
      // end of property ?
      if (level<=currentlevel) 
        break;
        
      // level>currentLevel would be wrong e.g.
      //  0 INDI
      //  1 BIRT
      //  3 DATE
      // we simply spit out a warning and continue as if nothing happened
      if (level>currentlevel+1) 
        addWarning(line, resources.getString("read.warn.badlevel", ""+level));
  
      // add sub property
      if (pos<0)
        sub = prop.addProperty(tag, value, true);
      else
        sub  = prop.addProperty(tag, value, pos++);
      
      // track it?
      if (trackAdded!=null)
        trackAdded.add(sub);
      
      // a reference ? Remember !
      if (sub instanceof PropertyXRef)
        xrefs.add(new XRef(line,(PropertyXRef)sub));
  
      // recurse into its properties
      readProperties(sub, 0, level, null);
      
      // next property
    } while (true);

    // restore what we haven't consumed
    redoLine();
  }
  
  /**
   * Decrypt a value if necessary
   */
  private void decryptLazy(Property prop) throws GedcomEncryptionException {

    String value = prop.getValue();
    
    // no need to do anything if not encrypted value 
    if (!Enigma.isEncrypted(value))
      return;
      
    // set property private
    prop.setPrivate(true, false);
      
    // no need to do anything for unknown password
    String password = gedcom.getPassword();
    if (password==Gedcom.PASSWORD_UNKNOWN) {
      addWarning(line, resources.getString("crypt.password.unknown"));
      return;
    }
      
    // not set password with encrypted value is error
    if (password==Gedcom.PASSWORD_NOT_SET) 
      throw new GedcomEncryptionException(resources.getString("crypt.password.required"), line);
    
    // try to init decryption
    if (enigma==null) {
      enigma = Enigma.getInstance(password);
      if (enigma==null) {
        addWarning(line, resources.getString("crypt.password.mismatch"));
        gedcom.setPassword(Gedcom.PASSWORD_UNKNOWN);
        return;
      }
    }

    // try to decrypt    
    try {
      // set decrypted value
      prop.setValue(enigma.decrypt(value));
    } catch (IOException e) {
      throw new GedcomEncryptionException(resources.getString("crypt.password.invalid"), line);
    }
      
    // done
  }

  /**
   * Add a warning
   */
  private void addWarning(int wline, String txt) {
    warnings.add(resources.getString("read.warn", new String[]{  ""+wline, txt} ));
  }
  
  /**
   * Keeping track of XRefs
   */
  private static class XRef {
    /** attributes */
    int line;
    PropertyXRef prop;
    /** constructor */
    XRef(int l, PropertyXRef p) {
      line = l;
      prop = p;
    }
  } //XRef
  
  /**
   * SniffedInputStream
   */
  private static class SniffedInputStream extends BufferedInputStream {
    
    private final byte[]
      BOM_UTF8    = { (byte)0xEF, (byte)0xBB, (byte)0xBF },
      BOM_UTF16BE = { (byte)0xFE, (byte)0xFF },
      BOM_UTF16LE = { (byte)0xFF, (byte)0xFE };
      
    private String encoding;
    private Charset charset;
    
    /**
     * Constructor
     */
    private SniffedInputStream(InputStream in) throws IOException {
      
      super(in, 4096);

      // fill buffer and reset
      super.mark(4096); 
      super.read();
      super.reset();
      
      // BOM present?
      if (matchPrefix(BOM_UTF8)) {
        LOG.info("Found BOM_UTF8 - trying encoding UTF-8");
        charset = Charset.forName("UTF-8");
        encoding = Gedcom.UNICODE;
        return;
      }
      if (matchPrefix(BOM_UTF16BE)) {
        LOG.info("Found BOM_UTF16BE - trying encoding UTF-16BE");
        charset = Charset.forName("UTF-16BE");
        encoding = Gedcom.UNICODE;
        return;
      }
      if (matchPrefix(BOM_UTF16LE)) {
        LOG.info("Found BOM_UTF16LE - trying encoding UTF-16LE");
        charset = Charset.forName("UTF-16LE");
        encoding = Gedcom.UNICODE;
        return;
      }
      
      // sniff gedcom header
      String header = new String(super.buf, super.pos, super.count);
      
      // tests
      if (matchHeader(header,Gedcom.UNICODE)) {
        LOG.info("Found "+Gedcom.UNICODE+" - trying encoding UTF-8");
        charset = Charset.forName("UTF-8");
        encoding = Gedcom.UNICODE;
        return;
      } 
      if (matchHeader(header,Gedcom.ASCII)) {
        // ASCII - 20050705 using Latin1 (ISO-8859-1) from now on to preserve extended ASCII characters
        LOG.info("Found "+Gedcom.ASCII+" - trying encoding ISO-8859-1");
        charset = Charset.forName("ISO-8859-1"); // was ASCII
        encoding = Gedcom.ASCII; 
        return;
      } 
      if (matchHeader(header,Gedcom.ANSEL)) {
        LOG.info("Found "+Gedcom.ANSEL+" - trying encoding ANSEL");
        charset = new AnselCharset();
        encoding = Gedcom.ANSEL;
        return;
      } 
      if (matchHeader(header,Gedcom.ANSI)) {
        LOG.info("Found "+Gedcom.ANSI+" - trying encoding Windows-1252");
        charset = Charset.forName("Windows-1252");
        encoding = Gedcom.ANSI;
        return;
      } 
      if (matchHeader(header,Gedcom.LATIN1)||matchHeader(header,"IBMPC")) { // legacy - old style ISO-8859-1/latin1
        LOG.info("Found "+Gedcom.LATIN1+" or IBMPC - trying encoding ISO-8859-1");
        charset = Charset.forName("ISO-8859-1");
        encoding = Gedcom.LATIN1;
        return;
      } 

      // no clue - will default to Ansel
      LOG.info("Could not sniff encoding - trying ANSEL");
      charset = new AnselCharset();
      encoding = Gedcom.ANSEL;
    }
    
    /**
     * Match a header encoding
     */
    private boolean matchHeader(String header, String encoding) {
      return header.indexOf("1 CHAR "+encoding)>0;
    }
    
    /**
     * Match a prefix byte sequence
     */
    private boolean matchPrefix(byte[] prefix) throws IOException {
      // too match to match?
      if (super.count<prefix.length)
        return false;
      // try it
      for (int i=0;i<prefix.length;i++) {
        if (super.buf[pos+i]!=prefix[i])
          return false;
      }
      // skip match
      super.skip(prefix.length);
      // matched!
      return true;
    }
          
    /**
     * result - charset
     */
    /*result*/ Charset getCharset() {
      return charset;
    }
    
    /**
     * result - encoding
     */
    /*result*/ String getEncoding() {
      return encoding;
    }
    
  } //InputStreamSniffer
  
} //GedcomReader
