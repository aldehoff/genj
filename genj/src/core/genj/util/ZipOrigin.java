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
 * Class which stands for an origin of a resource - this Origin
 * is pointing to a ZIP file so all relative files are read
 * of that file, too
 */
public class ZipOrigin extends Origin {

  /** cached bytes */
  private byte[] cachedBits;

  /**
   * Constructor
   */
  protected ZipOrigin(URL url) {
    super(url);
  }

  /**
   * Open connection to this origin
   */
  public Connection open() throws IOException {

    // There has to be an anchor into the zip
    String anchor = url.getRef();
    if ((anchor==null)||(anchor.length()==0)) {
      throw new IOException("ZipOrigin needs anchor for open()");
    }

    // get it (now relative)
    return openRelativeFile(anchor);
  }

  /**
   * Returns a file that is relative to this Origin
   */
  protected Connection openRelativeFile(String file) throws IOException {

    // We either load from cached bits or try to open the connection
    if (cachedBits==null) {
      cachedBits = new ByteArray(url.openConnection().getInputStream()).getBytes();
    }

    // Then we can read the zip from the cached bits
    ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(cachedBits));

    // .. loop through files
    ZipEntry zentry;
    while (true) {

      zentry = zin.getNextEntry();
      if (zentry==null) {
        throw new IOException("Couldn't find resource "+file+" in ZIP-file");
      }

      if (zentry.getName().equals(file)) {
        return new Connection(zin,zentry.getSize());
      }

    }
    // Done
  }

  /**
   * Whether it is possible to save to the Origin
   */
  public boolean isFile() {
    return false;
  }

  /**
   * Is not supported by ZipOrigin
   */
  public File getFile() {
    throw new IllegalArgumentException("ZipOrigin doesn support getFile()");
  }

  /**
   * Returns the Origin's Filename file://d:/gedcom/example.zip#[example.ged]
   */
  public String getFileName() {
    return url.getRef();
  }

  /**
   * The name of this origin file://d:/gedcom/[example.zip#example.ged]
   */
  public String getName() {
    return super.getName();
  }


}
