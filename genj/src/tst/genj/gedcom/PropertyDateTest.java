/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.gedcom;

import genj.gedcom.PropertyDate.Format;
import genj.gedcom.time.Calendar;
import genj.gedcom.time.PointInTime;
import junit.framework.TestCase;

/**
 * Testing PropertyName
 */
public class PropertyDateTest extends TestCase {
  
  private PropertyDate date = new PropertyDate();

  private Calendar
    GREGORIAN = PointInTime.GREGORIAN,
    JULIAN = PointInTime.JULIAN,
    HEBREW = PointInTime.HEBREW,
    FRENCHR = PointInTime.FRENCHR;
  
  /**
   * Test dates
   */
  public void testDates() {     

    testParsing("", PropertyDate.DATE, GREGORIAN, 0, 0, 0, true);
    testParsing("25 MAY 1970", PropertyDate.DATE, GREGORIAN, 1970, 5, 25, true);
    testParsing("@#DJULIAN@ 25 MAY 1970", PropertyDate.DATE, JULIAN, 1970, 5, 25, true);
    testParsing("@#DFRENCH R@ 3 GERM An I", PropertyDate.DATE, FRENCHR, 1, 7, 3, false);
    testParsing("@#DHEBREW@ 1     CSH 5000", PropertyDate.DATE, HEBREW, 5000,  2, 1, false);

    testParsing("FROM 1 AUG 1999 TO 1 SEP 2001", PropertyDate.FROM_TO, GREGORIAN, 1999, 8, 1, GREGORIAN, 2001, 9, 1, true);
    testParsing("FROM @#DFRENCH R@ 3 GERM 1", PropertyDate.FROM, FRENCHR, 1, 7, 3, true);
    testParsing("TO @#DFRENCH R@ 3 GERM An I", PropertyDate.TO, FRENCHR, 1, 7, 3, false);
    testParsing("BET   @#DHEBREW@ 1    CSH 5000   AND @#DFRENCH R@ 3 GERM 1", PropertyDate.BETWEEN_AND, HEBREW, 5000,  2, 1, FRENCHR, 1, 7, 3,false);
    testParsing("BEF 25 MAY 1970", PropertyDate.BEFORE, GREGORIAN, 1970, 5, 25, true);
    testParsing("AFT     @#DHEBREW@ 1     CSH 5000  ", PropertyDate.AFTER, HEBREW, 5000,  2, 1, false);

    testParsing("ABT 1970", PropertyDate.ABOUT, GREGORIAN, 1970, 0, 0, true);
    testParsing("CAL MAY 1970", PropertyDate.CALCULATED, GREGORIAN, 1970, 5, 0, true);
    testParsing("EST @#DJULIAN@ 25 May 1970", PropertyDate.ESTIMATED, JULIAN, 1970, 5, 25, false);
    
    testParsing("Bef Sep 1846", PropertyDate.BEFORE, GREGORIAN, 1846, 9, 0, false);
    
    testParsing("INT 25 MAY 1970 (foo)", PropertyDate.INTERPRETED, GREGORIAN, 1970, 5, 25, true);
    testParsing("(sometime in may)", PropertyDate.INTERPRETED, GREGORIAN, 0, 0, 0, true);
    testParsing("foo bar", PropertyDate.INTERPRETED, GREGORIAN, 0, 0, 0, false);
    
    // done
  }
  
  private void testParsing(String value, Format format, Calendar cal, int year, int month, int day, boolean verbatim) {
    date.setValue(value);
    if (verbatim)
      assertEquals(value, date.getValue());
    assertEquals("wrong format", date.getFormat(), format);
    testPIT(date.getStart(), cal, year, month, day);
  }
  
  private void testParsing(String value, Format format, Calendar cal1, int year1, int month1, int day1, Calendar cal2, int year2, int month2, int day2, boolean verbatim) {
    date.setValue(value);
    if (verbatim)
      assertEquals(value, date.getValue());
    assertEquals("wrong format", date.getFormat(), format);
    testPIT(date.getStart(), cal1, year1, month1, day1);
    testPIT(date.getEnd(), cal2, year2, month2, day2);
  }
  
  private void testPIT(PointInTime pit, Calendar cal, int year, int month, int day) {
    
    assertEquals("calendar of "+pit, cal, pit.getCalendar());
    
    if (year>0) 
      assertEquals("year of "+pit, year, pit.getYear());
    else
      assertTrue("year of "+pit+" should be unknown", pit.getYear()==PointInTime.UNKNOWN);
    
    if (month>0) 
      assertEquals("month of "+pit, month-1, pit.getMonth());
    else
      assertTrue("month of "+pit+" should be unknown", pit.getMonth()==PointInTime.UNKNOWN);
    
    if (day>0) 
      assertEquals("day of "+pit, day-1, pit.getDay());
    else
      assertTrue("day of "+pit+" should be unknown", pit.getDay()==PointInTime.UNKNOWN);
    
    
  }
  
} //PropertyDateTest
