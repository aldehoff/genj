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
package genj.crypto;

import java.io.IOException;

/**
 * The abstract base type for en/decrypting Gedcom information 
 */
public abstract class Enigma {
  
  /** the implementation class we use */
  private static final String DES = "genj.crypto.EnigmaImpl";
  
  /**
   * Get access to an Enigma instance
   * @return instance of Enigma or null if no de/encryption implementation available 
   */
  public static Enigma getInstance(String password) {
    
    try {
      return ((Enigma)Class.forName(DES).newInstance()).init(password);
    } catch (Throwable t) {
      return null;
    }
    
  }
  
  /**
   * encrypt
   * @param value the plain data as Java string to encrypt
   * @return the encrypted value
   */  
  public String encrypt(String value) throws IOException {
    return encryptImpl(value);
  }

  /**
   * decrypt
   * @param value the encrypted data as Java string to decrypt 
   * @return the decrypted value
   */  
  public String decrypt(String value) throws IOException {
    return decryptImpl(value);
  }

  /**
   * implementation contract - initialization
   */
  protected abstract Enigma init(String password);
  
  /**
   * implementation contract - encrypt
   */
  protected abstract String encryptImpl(String value) throws IOException;
  
  /**
   * implementation contract - decrypt
   */
  protected abstract String decryptImpl(String value) throws IOException;
  
} //Enigma
