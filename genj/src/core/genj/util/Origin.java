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

import java.io.*;
import java.net.*;
import java.util.zip.*;

/**
 * Class which stands for an origin of a resource - all files
 * loaded relative to this Origin are loaded from the same
 * directory
 */
public class Origin {

  /** the url that the origin is based on */
  protected URL url;

  /**
   * Sub-Class which forms a connection to an origin
   */
  public class Connection {

    // LCD
    private InputStream in;
    private long        len;

    Connection(InputStream in,long len) {
      this.in=in;this.len=len;
    }

    public InputStream getInputStream() {
      return in;
    }

    public long getLength() {
      return len;
    }

    // EOC
  }

  /**
   * factory for given url and relative file location
   */
  public static Origin create(Origin o, String file) throws MalformedURLException {
    return create(o.toString().substring(0, o.toString().lastIndexOf("/") + 1 ) + file.replace('\\','/'));
  }

  /**
   * factory for given string location
   */
  public static Origin create(String s) throws MalformedURLException {

    // Check the URL
    URL url = new URL(s.replace('\\','/'));

    // What will it be File/ZIP?
    if (isZip(url)) {
      return new ZipOrigin(url);
    } else {
      return new Origin(url);
    }

  }

  /**
   * Helper that checks whether the origin is a ZIP file
   */
  private static boolean isZip(URL url) {
    boolean result = url.getFile().endsWith(".zip");
    return result;
  }

  /**
   * Factory for given url location
   */
  public static Origin create(URL u) throws MalformedURLException {
    return create(u.toString());
  }

  /**
   * Constructor
   */
  protected Origin(URL pUrl) {
    url = pUrl;
  }

  /**
   * Open connection to this origin
   */
  public Connection open() throws IOException {

    URLConnection uc = url.openConnection();

    return new Connection(uc.getInputStream(),uc.getContentLength());

    // Done
  }

  /**
   * Open file relative to origin
   */
  public Connection openFile(String name) throws IOException {

    // Make sure we have '/' and not '\\'
    name = name.replace('\\','/');

    // Absolute file specification?
    if ((name.charAt(0)=='/') || (name.indexOf(":")>0) ) {

      URLConnection uc;
      try {
        uc = new URL(name).openConnection();
      } catch (MalformedURLException e1) {
        // ... hmmm, with "file://"?
        try {
          uc = new URL("file://"+name).openConnection();
        } catch (MalformedURLException e2) {
          return null;
        }
      }
      return new Connection(uc.getInputStream(),uc.getContentLength());

    }

    return openRelativeFile(name);
  }

  /**
   * Returns a file that is relative to this Origin
   */
  protected Connection openRelativeFile(String file) throws IOException {

    // Calc the file's name
    String u = url.toString();
    u = u.substring(0, u.lastIndexOf("/") +1) + file;

    // Connect
    URLConnection uc;
    try {
      uc = new URL(u).openConnection();
    } catch (MalformedURLException e) {
      throw new IOException();
    }

    // Done
    return new Connection(uc.getInputStream(),uc.getContentLength());
  }

  /**
   * String representation
   */
  public String toString() {
    return url.toString();
  }

  /**
   * Whether it is possible to save to the Origin's file
   */
  public boolean isFile() {
    return url.getProtocol().equals("file");
  }

  /**
   * Calculates the relative path for given file if applicable
   */
  public String calcRelativeLocation(String file) {

    String u = url.toString();
    int i = u.lastIndexOf('/');
    u = u.substring(0,i+1);

    if (!file.replace('\\','/').startsWith(u)) {
      return null;
    }

    return file.substring(u.length());
  }

  /**
   * Returns the Origin as a File (e.g. to use to write to)
   * [new File(file://d:/gedcom/example.ged)]
   */
  public File getFile() {
    return new File(url.getFile());
  }

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
    String path = url.toString(); // this will get
    return path.substring(path.lastIndexOf('/')+1);
  }

}
