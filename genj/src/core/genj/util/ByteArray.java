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

import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.ImageIcon;

/**
 * Class that represents an improved ByteArray
 */
public class ByteArray {

  /** the block size by which information is read */
  private final static int CLUSTER = 1024*4;

  /** an empty byte array */
  private final static byte[] EMPTY = new byte[]{};

  /** the bits */
  private byte[] bits = EMPTY;

  /**
   * Accessor for bytes
   */
  public byte[] getBytes() {
    return bits;
  }

  /**
   * Constructor
   */
  public ByteArray(InputStream in) throws IOException {

    // Read from stream
    byte buffer[] = new byte[CLUSTER];
    int len=0,total=0;

    while (true) {

      // Read !
      try {
        len = in.read(buffer,total,buffer.length-total);
      } catch (IOException ex) {
        throw ex;
      }

      // End of stream ?
      if (len<0)
        break;

      // Increment amount read !
      total+=len;

      // Did it fit and end ?
      if (total<buffer.length) {
        continue;
      }

      // More than fit !
      byte tmp[] = new byte[buffer.length*2];
      System.arraycopy(buffer,0,tmp,0,buffer.length);
      buffer = tmp;

      // Read on !
    }

    // Remember
    bits = buffer;

    // Done
  }

}
