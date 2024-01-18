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
package genj.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Class which stands for an origin of a resource - all files
 * loaded relative to this Origin are loaded from the same
 * directory
 */
public abstract class Origin {
  
  /** chars we need */
  private final static char
    BSLASH = '\\',
    FSLASH = '/',
    COLON  = ':';

  /** the url that the origin is based on */
  protected URL url;
  
  /**
   * Constructor
   */
  protected Origin(URL url) {
    this.url = url;
  }

  /**
   * factory for given string location
   */
  public static Origin create(String s) throws MalformedURLException {
    // delegate
    return create(new URL(s));
  }
  
  /**
   * factory for given url
   */
  public static Origin create(URL url) {

    // What will it be File/ZIP?
    if (url.getFile().endsWith(".zip")) {
      return new ZipOrigin(url);
    } else {
      return new DefaultOrigin(url);
    }

  }

  /**
   * Open connection to this origin
   */
  public abstract InputStream open() throws IOException;

  /**
   * Open file based on this origin
   */
  public final InputStream open(String name) throws IOException {

    // Make sure name is correctly encoded
    name = back2forwardslash(name);

    // Absolute file specification?
    if ((name.charAt(0)==FSLASH) || (name.indexOf(":")>0) ) {

      URLConnection uc;
      try {
        uc = new URL(name).openConnection();
      } catch (MalformedURLException e1) {
        // ... hmmm, with "file:"?
        try {
          // 20021210 using file:// here seems to slow things down if no
          // file exists
          uc = new URL("file:"+name).openConnection();
        } catch (MalformedURLException e2) {
          return null;
        }
      }
      return new InputStreamImpl(uc.getInputStream(),uc.getContentLength());

    }

    // relative file
    return openImpl(name);
  }
  
  /**
   * Open file relative to origin
   */
  protected abstract InputStream openImpl(String name) throws IOException;

  /**
   * String representation
   */
  public String toString() {
    return url.toString();
  }

  /**
   * Whether it is possible to save to the Origin's file
   */
  public abstract boolean isFile();

  /**
   * Calculates the relative path for given file if applicable
   */
  public String calcRelativeLocation(String file) {

    file = "file:"+back2forwardslash(file);

    String path = back2forwardslash(url.toString());
    path = path.substring(0,path.lastIndexOf(FSLASH)+1);

    if (!file.startsWith(path)) {
      return null;
    }

    return file.substring(path.length());
  }
  
  /**
   * Returns the Origin as a File (e.g. to use to write to)
   * [new File(file://d:/gedcom/example.ged)]
   */
  public abstract File getFile();
  
  /**
   * Returns a File representation of a resource relative to this origin
   */
  public abstract File getFile(String name);

  /**
   * Returns the Origin's FileName file://d:/gedcom/[example.ged]
   */
  public String getFileName() {
    return getName();
  }

  /**
   * The name of this origin file://d:/gedcom/[example.ged]
   */
  public String getName() {
    String path = back2forwardslash(url.toString());
    return path.substring(path.lastIndexOf(FSLASH)+1);
  }
  
  /**
   * Returns a cleaned up string with forward instead
   * of backwards slash(e)s
   */
  protected String back2forwardslash(String s) {
    return s.toString().replace(BSLASH, FSLASH);
  }
  
  /**
   * A default origin 
   */
  private static class DefaultOrigin extends Origin {

    /**
     * Constructor
     */
    protected DefaultOrigin(URL url) {
      super(url);
    }
    
    /**
     * @see genj.util.Origin#open()
     */
    public InputStream open() throws IOException {
      URLConnection uc = url.openConnection();
      return new InputStreamImpl(uc.getInputStream(),uc.getContentLength());
    }
    
    /**
     * @see genj.util.Origin#openImpl(java.lang.String)
     */
    protected InputStream openImpl(String name) throws IOException {

      // Calc the file's name
      String path = back2forwardslash(url.toString());
      path = path.substring(0, path.lastIndexOf(FSLASH) +1) + name;

      // Connect
      try {

        URLConnection uc = new URL(path).openConnection();
        return new InputStreamImpl(uc.getInputStream(),uc.getContentLength());

      } catch (MalformedURLException e) {
        throw new IOException(e.getMessage());
      }

    }
    
    /**
     * @see genj.util.Origin#isFile()
     */
    public boolean isFile() {
      return url.getProtocol().equals("file");
    }
    
    /**
     * @see genj.util.Origin#getFile()
     */
    public File getFile() {
      return new File(url.getFile());
    }

    /**
     * @see genj.util.Origin#getFile(java.lang.String)
     */
    public File getFile(String file) {
      
      // good argument?
      if (file.length()<1) return null;
      
      // Absolute file specification?
      if (file.charAt(0)==FSLASH || file.indexOf(COLON)>0 ) 
        return new File(file);
      
      // should be in parent directory
      return new File(getFile().getParent(), file);
    }


  } //DefaultOrigin
 

  /**
   * Class which stands for an origin of a resource - this Origin
   * is pointing to a ZIP file so all relative files are read
   * from the same archive
   */
  private static class ZipOrigin extends Origin {

    /** cached bytes */
    private byte[] cachedBits;

    /**
     * Constructor
     */
    protected ZipOrigin(URL url) {
      super(url);
    }

    /**
     * @see genj.util.Origin#open()
     */
    public InputStream open() throws IOException {

      // There has to be an anchor into the zip
      String anchor = url.getRef();
      if ((anchor==null)||(anchor.length()==0)) {
        throw new IOException("ZipOrigin needs anchor for open()");
      }

      // get it (now relative)
      return openImpl(anchor);
    }
    
    /**
     * @see genj.util.Origin#openImpl(java.lang.String)
     */
    protected InputStream openImpl(String file) throws IOException {

      // We either load from cached bits or try to open the connection
      if (cachedBits==null) {
        cachedBits = new ByteArray(url.openConnection().getInputStream()).getBytes();
      }

      // Then we can read the zip from the cached bits
      ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(cachedBits));

      // .. loop through files
      for (ZipEntry zentry = zin.getNextEntry();zentry!=null;zentry=zin.getNextEntry()) {
        if (zentry.getName().equals(file)) 
          return new InputStreamImpl(zin, (int)zentry.getSize());
      }

      // not found
      throw new IOException("Couldn't find resource "+file+" in ZIP-file");
    }

    /**
     * @see genj.util.Origin#isFile()
     */
    public boolean isFile() {
      return false;
    }

    /**
     * @see genj.util.Origin#getFile()
     */
    public File getFile() {
      throw new IllegalArgumentException("ZipOrigin doesn support getFile()");
    }

    /**
     * Returns the Origin's Filename file://d:/gedcom/example.zip#[example.ged]
     * @see genj.util.Origin#getFileName()
     */
    public String getFileName() {
      return url.getRef();
    }

    /**
     * File not available
     * @see genj.util.Origin#getFile(java.lang.String)
     */
    public File getFile(String name) {
      return null;
    }

  } //ZipOrigin
  
  /**
   * An InputStream returned from Origin
   */
  private static class InputStreamImpl extends InputStream {

    /** wrapped input stream */
    private InputStream in;
    
    /** length of data */
    private int len;

    /**
     * Constructor
     */
    protected InputStreamImpl(InputStream in, int len) {
      this.in=in;
      this.len=len;
    }

    /**
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
      return in.read();
    }

    /**
     * @see java.io.InputStream#available()
     */
    public int available() throws IOException {
      return len;
    }
    
    /**
     * 20040220 have to delegate close() to 'in' to make
     * sure the input is closed right (file open problems)
     * @see java.io.InputStream#close()
     */
    public void close() throws IOException {
      in.close();
    }

    // EOC
  }

} //Origin
