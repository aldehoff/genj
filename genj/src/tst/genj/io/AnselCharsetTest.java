/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.io;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/**
 * Test Ansel encoding/decoding
 */
public class AnselCharsetTest extends TestCase {
  
  /**
   * Test encoding/decoding
   */
  public void testEncodingDecoding() {

    Iterator strings = getStrings().iterator();
    while (strings.hasNext()) {

      String s = strings.next().toString();

      // encode string 
      byte[] anselbytes = null;
      try {     
        ByteArrayOutputStream bout = new ByteArrayOutputStream(s.length());
        OutputStreamWriter out = new OutputStreamWriter(bout, new AnselCharset());
        out.write(s);
        out.flush();
        anselbytes = bout.toByteArray();
      } catch (IOException e) {
        fail("ioex during encode("+s+")");
      }

      // decode string
      String unicode = null;
      try {     
        BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(anselbytes), new AnselCharset()));
        unicode = in.readLine();
      } catch (IOException e) {
        fail("ioex during decode(encode("+s+"))");
      }
      
      // compare
      assertEquals("decode(encode("+s+"))!="+s, s, unicode);
    }

    // done    
  }
  
  /**
   * Calculate test strings
   */
  private List getStrings() {
    
    ArrayList result = new ArrayList(32);

    // abc�
    result.add("����");
    
    // xxx...xz  (8192+1)    
    StringBuffer buf = new StringBuffer(8193);
    for (int i=0;i<8192;i++)
      buf.append('x');
    buf.append('z');
    result.add(buf.toString());
      
    // abc
    result.add("abc");
      
    // ab�
    result.add("ab�");
      
    // abc�
    result.add("abc�");

    // xxx...x� (8191+1)
    buf.setLength(0);
    for (int i=0;i<8191;i++)
      buf.append('x');
    buf.append('�');
    result.add(buf.toString());

    // xxx...x�� (8191+2)      
    buf.append('�');
    result.add(buf.toString());
    
    // done
    return result;
  }

} //AnselCharsetTest
