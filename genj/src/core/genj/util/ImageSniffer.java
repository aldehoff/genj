/*
 * Created on Apr 23, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package genj.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Nils
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ImageSniffer {

  /** sniffed suffix */
  private String suffix = null;

  /** stream */
  private InputStream in;
  
  /** bytes read */
  private int read = 0;

  /** sniffed values */  
  protected int 
    width = 0, 
    height= 0, 
    dpiy  = -1, 
    dpix  = -1;
    
  /**
   * Constructor
   */
  public ImageSniffer(InputStream stream) {

    // keep stream
    in = stream;
    
    try {
  
      // check first 2 bytes for type
      int tag = (read()&0xff) << 8 | (read()&0xff);
  
      switch (tag) {
        case 0x4749: sniffGif();break;
        case 0x8950: sniffPng();break;
        case 0xffd8: sniffJpg();break;
        case 0x424d: sniffBmp();break;
        default:
      }    
      
    } catch (IOException e) {
    }

    //System.out.println(suffix+":"+width+"x"+height+" / "+dpix+"x"+dpiy+"["+read+"]");
    
    // done    
  }
  
  /**
   * Accessor - suffix
   */
  public String getSuffix() {
    return suffix;
  }
  
  /**
   * Accessor - resolution (dpi)
   */
  public int getDPIx() {
    return dpix;
  }

  /**
   * Accessor - resolution (dpi)
   */
  public int getDPIy() {
    return dpiy;
  }

  /**
   * Accessor - width
   */
  public int getWidth() {
    return width;
  }

  /**
   * Accessor - height
   */
  public int getHeight() {
    return height;
  }

  /**
   * sniffer - gif
   */
  private void sniffGif() throws IOException {
    
    // two possible magic headers
    final int
      F89A = string2int("F89a"),
      F87A = string2int("F87a");
      
    // sniff rest of magic
    int magic = sniffIntBigEndian();
    if (magic!=F89A&&magic!=F87A)
      return;

    // width & height
    width = sniffShortLittleEndian();
    height = sniffShortLittleEndian();

    // no resolution
          
    //  int flags = read();
    //  bitsPerPixel = ((flags >> 4) & 0x07) + 1;

    // done
    suffix = "gif";
  }

  /**
   * sniffer - png
   * 
   * @see http://www.libpng.org/pub/png/spec
   */
  private void sniffPng() throws IOException {

    // prepare chunk type identifies
    final int
      IHDR = string2int("IHDR"),
      IDAT = string2int("IDAT"),
      IEND = string2int("IEND"),
      PHYS = string2int("pHYs");
            
    // sniff rest of magic
    if (!sniff(new byte[]{0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a}))
      return;

    // len and type
    int len = sniffIntBigEndian();
    int type = sniffIntBigEndian();
    if (IHDR!=type)
      return;

    // width & height
    width  = sniffIntBigEndian();
    height = sniffIntBigEndian();
    
    // skip rest + crc
    skip(len-8+4);

    // look for pHYs
    while (true) {
      
      // len and type
      len = sniffIntBigEndian();
      type = sniffIntBigEndian();

      // chunk : dat?
      if (IDAT==type) break;

      // chunk : end?
      if (IEND==type) break;

      // chunk : pHYs?      
      if (PHYS==type) {
        int
          x = sniffIntBigEndian(),
          y = sniffIntBigEndian();
        if (in.read()==1) { //meter
          dpix = (int)Math.round(2.54D*x/100);
          dpiy = (int)Math.round(2.54D*y/100);
        }
        break;
      }
      
      // skip data + crc
      skip(len+4);
      
    }
    
    // set
    suffix = "png";
  }

  /**
   * sniffer - jpg
   * 
   * @see http://www.dcs.ed.ac.uk/home/mxr/gfx/2d/JPEG.txt
   */
  private void sniffJpg() throws IOException {

    final byte[] 
      JFIF = "JFIF".getBytes();

    // loop chunks
    chunks: while (true) {
    
      // marker and size
      int 
        marker = sniffShortBigEndian(),
        size = sniffShortBigEndian() - 2, // without 'size' itself
        start = read; 
        
      // marker?
      switch (marker) {
        case 0xffe0: // APPx
          // looking for JFIF
          if (sniff(JFIF)) {
            // skip '0'(1) and version(2)
            skip(3);
            // check units
            switch (read()) {
              case 1: // dots per inch
                dpix = sniffShortBigEndian();
                dpiy = sniffShortBigEndian();
                break;
              case 2: // dots per cm
                dpix = (int)(sniffShortBigEndian() * 2.54f);
                dpiy = (int)(sniffShortBigEndian() * 2.54f);
                break;
              }
          }
          break;
        case 0xffc0: // SOFn  ffc0 - ffcf (without 4&8)
        case 0xffc1:
        case 0xffc2:
        case 0xffc3:
        //case 0xffc4:
        case 0xffc5:
        case 0xffc6:
        case 0xffc7:
        //case 0xffc8:
        case 0xffc9:
        case 0xffca:
        case 0xffcb:
        case 0xffcc:
        case 0xffcd:
        case 0xffce:
        case 0xffcf:
          // bitsPerPixel = a * b
          read(); //a
          height = sniffShortBigEndian();
          width = sniffShortBigEndian();
          read(); //b
          break chunks;
        default:
          if ((marker & 0xff00) != 0xff00)
            return; // not a valid marker
      }
      
      // skip rest of chunk
      skip(size-(read-start));
    }    
    
    // done
    suffix = "jpg";
  }

  /**
   * sniffer - bmp
   */
  private void sniffBmp() throws IOException {

    // skip some stuff
    skip(16);
    
    // width & height    
    width = sniffIntLittleEndian();
    height = sniffIntLittleEndian();
    
    if (width < 1 || height < 1) return;

    // skip short
    skip(2);

    // bits per pixel    
    int bitsPerPixel = sniffShortLittleEndian();
    if (bitsPerPixel != 1 && bitsPerPixel != 4 &&
        bitsPerPixel != 8 && bitsPerPixel != 16 &&
        bitsPerPixel != 24 && bitsPerPixel != 32) {
        return;
    }
    
    // skip two longs
    skip(8);
    
    // resolution
    int x = sniffIntLittleEndian();
    if (x > 0) {
      dpix  = (int)Math.round(2.54D*x/100); // dots per meter
    }
    int y = sniffIntLittleEndian();
    if (y > 0) {
      dpiy = (int)Math.round(2.54D*y/100); // dots per meter
    }

    // done
    suffix = "bmp";
  }

  /**
   * Read one byte 
   */
  private int read() throws IOException {
    read++;
    return in.read();
  }
  
  /**
   * Skip bytes
   */
  private void skip(int num) throws IOException {
    read += num;
    in.skip(num);
  }

  /**
   * Sniffer - check magic
   */
  private boolean sniff(byte[] magic) throws IOException {
    for (int m=0;m<magic.length;m++) {
      int i = read();
      if (i==-1||i!=magic[m]) return false;
    }
    return true;
  }

  /**
   * Sniffer - check magic
   */
  private boolean sniff(String magic) throws IOException {
    return sniff(magic.getBytes());
  }
  
  /**
   * Sniffer - int big endian
   */
  private int sniffIntBigEndian() throws IOException {
    return
      (read() & 0xff) << 24 | 
      (read() & 0xff) << 16 | 
      (read() & 0xff) <<  8 | 
      (read() & 0xff)       ;
  }
  
  /**
   * Sniffer - int little endian
   */
  private int sniffIntLittleEndian() throws IOException {
    return
      (read() & 0xff)       | 
      (read() & 0xff) <<  8 | 
      (read() & 0xff) << 16 | 
      (read() & 0xff) << 24 ;
  }

  /**
   * Sniffer - short big endian
   */  
  private int sniffShortBigEndian() throws IOException {
    return
      (read() & 0xff) << 8 | 
      (read() & 0xff)      ;
  }
  
  /**
   * Sniffer - short big endian
   */
  private int sniffShortLittleEndian() throws IOException {
    return 
      (read() & 0xff)      | 
      (read() & 0xff) << 8 ;
  }
  
  /**
   * transform a string to int (4 bytes)
   */
  private int string2int(String s) {
    if (s.length()!=4) throw new IllegalArgumentException();
    return
      (s.charAt(0) & 0xff) << 24 | 
      (s.charAt(1) & 0xff) << 16 | 
      (s.charAt(2) & 0xff) <<  8 | 
      (s.charAt(3) & 0xff)       ;
  }
  
} //ImageSniffer
