/*
 * Created on Apr 23, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package genj.util;

import java.awt.Dimension;
import java.awt.Point;
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
  protected Dimension dimension;
  protected Point dpi;
    
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

    // validate what we've got
    if (dpi!=null&&(dpi.x<=0||dpi.y<=0))
      dpi = null;
    if (dimension!=null&&(dimension.width<1||dimension.height<1))
      dimension = null;
    
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
   * @return null if unknown
   */
  public Point getDPI() {
    return dpi;
  }

  /**
   * Accessor - dimension
   * @return null if unknown
   */
  public Dimension getDimension() {
    return dimension;
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
    dimension = new Dimension(
    	sniffShortLittleEndian(),
    	sniffShortLittleEndian()
    );

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
    dimension = new Dimension(
      sniffIntBigEndian(),
      sniffIntBigEndian()
    );
    
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
          dpi = new Point(
          	(int)Math.round(2.54D*x/100),
          	(int)Math.round(2.54D*y/100)
          );
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
   * sniffer - tiff
   * 
   * @see http://www.media.mit.edu/pia/Research/deepview/exif.html
   */
  private boolean sniffTiff() throws IOException {
    
    int start = read;

    // analyze TIFF header
    boolean intel;
    switch (sniffShortLittleEndian()) {
      case 0x4949: //II = intel
        intel = true;
        break;
      case 0x4d4d: //MM = motorola
        intel = false; 
        break;
      default:
        return false;
    }
    
    // skip 0x002a
    skip(2);
    
    // jump to IFD (Image File Directory)
    skip(sniffInt(intel)-(read-start));
    
    // loop directory looking for x/y resolution
    int xres = 0, yres = 0;
    for (int i=0,j=sniffShort(intel);i<j;i++) {
      // directory image information entry - 12 bytes
      int tag = sniffShort(intel),
          format = sniffShort(intel),
          components = sniffInt(intel),
          value = sniffInt(intel);
      switch (tag) {
      	case 0x011a: //x-resolution
      	  xres = value;
      		break;
      	case 0x011b: //y-resolution
      	  yres = value;
      		break;
      }
      // check next
    }
    
    // did we get resolution offsets that still work?
    if (xres<(read-start)||yres<(read-start)) 
      return false;
    
    // lookup resolution values
    if (xres<yres) {
      skip(xres-(read-start));
      xres = sniffInt(intel) / sniffInt(intel);
      skip(yres-(read-start));
      yres = sniffInt(intel) / sniffInt(intel);
    } else {
      skip(yres-(read-start));
      yres = sniffInt(intel) / sniffInt(intel);
      skip(xres-(read-start));
      xres = sniffInt(intel) / sniffInt(intel);
    }
    dpi = new Point(xres, yres);
    
    // done
    return true;
    
  }
  
  /**
   * sniffer - jpg
   * 
   * @see http://www.dcs.ed.ac.uk/home/mxr/gfx/2d/JPEG.txt
   */
  private void sniffJpg() throws IOException {

    final byte[] 
      JFIF = "JFIF".getBytes(),
      EXIF = "Exif".getBytes();

    // loop chunks
    chunks: while (true) {
    
      // marker and size
      int 
        marker = sniffShortBigEndian(),
        size = sniffShortBigEndian() - 2, // without 'size' itself
        start = read; 
        
      // marker?
      switch (marker) {
        case 0xffe1: // EXIF
          // looking for Exif and trailing short
          if (!sniff(EXIF))
            break;
          skip(2);
          // tiff from here
          sniffTiff();
          break;
        case 0xffe0: // jpeg APPx
          // looking for JFIF
          if (sniff(JFIF)) {
            // skip '0'(1) and version(2)
            skip(3);
            // check units
            switch (read()) {
              case 1: // dots per inch
                dpi = new Point(sniffShortBigEndian(), sniffShortBigEndian());
                break;
              case 2: // dots per cm
                dpi = new Point(
	                (int)(sniffShortBigEndian() * 2.54f),
	                (int)(sniffShortBigEndian() * 2.54f)
                );
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
          dimension = new Dimension(
	          sniffShortBigEndian(),
	          sniffShortBigEndian()
          );
          read(); //b
          break;
        case 0xffd9:
          // EOI
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
    dimension = new Dimension(
	    sniffIntLittleEndian(),
	    sniffIntLittleEndian()
    );
    
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
    dpi = new Point(
      (int)Math.round(2.54D*sniffIntLittleEndian()/100), // dots per meter
      (int)Math.round(2.54D*sniffIntLittleEndian()/100)  // dots per meter
    );

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
   * Sniffer - int as intel or motorola
   */
  private int sniffInt(boolean intel) throws IOException {
    return intel ? sniffIntLittleEndian() : sniffIntBigEndian();  
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
   * Sniffer - short as intel or motorola
   */
  private int sniffShort(boolean intel) throws IOException {
    return intel ? sniffShortLittleEndian() : sniffShortBigEndian();  
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
