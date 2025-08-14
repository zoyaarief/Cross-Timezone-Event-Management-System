package model;

import org.junit.Test;

import org.junit.Before;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * Test class for improved date time helper class.
 */
public class DateTimeHelperTest {

  @Before
  public void setup() {
    // Reset to the default "America/New_York" before each test,
    // or pick some known zone for consistency.
    //CalendarManager.resetGlobalState();
    DateTimeHelper.setDefaultZone("America/New_York");
  }

  @Test
  public void testSetDefaultZone_validZone() {
    // Switch to Los Angeles
    DateTimeHelper.setDefaultZone("America/Los_Angeles");
    ZoneId z = DateTimeHelper.getDefaultZone();
    assertEquals("After setDefaultZone, getDefaultZone should match."
            , "America/Los_Angeles", z.getId()
    );
  }

  @Test
  public void testSetDefaultZone_invalidZone() {
    // setDefaultZone should throw a RuntimeException if zoneName is invalid
    assertThrows(RuntimeException.class, () -> {
      DateTimeHelper.setDefaultZone("NotAReal/ZoneName");
    });
  }

  @Test
  public void testParseDate_noTimeComponent() throws InvalidCalenderOperationException {
    Date d = DateTimeHelper.parseDate("2025-03-10");
    // Convert back to local date/time
    LocalDateTime ldt = DateTimeHelper.convertToLocalDateTime(d);
    assertEquals(2025, ldt.getYear());
    assertEquals(3, ldt.getMonthValue());
    assertEquals(10, ldt.getDayOfMonth());
    assertEquals(0, ldt.getHour());
    assertEquals(0, ldt.getMinute());
  }

  @Test
  public void testParseDate_withTimeComponent() throws InvalidCalenderOperationException {
    // parseDate("2025-03-10T13:45") in America/New_York
    Date d = DateTimeHelper.parseDate("2025-03-10T13:45");
    LocalDateTime ldt = DateTimeHelper.convertToLocalDateTime(d);
    assertEquals(2025, ldt.getYear());
    assertEquals(3, ldt.getMonthValue());
    assertEquals(10, ldt.getDayOfMonth());
    assertEquals(13, ldt.getHour());
    assertEquals(45, ldt.getMinute());
  }

  @Test
  public void testParseDate_invalidFormat() {
    // parseDate("invalid-date") => throws InvalidCalenderOperationException
    assertThrows(InvalidCalenderOperationException.class, () -> {
      DateTimeHelper.parseDate("invalid-date");
    });
  }

  @Test
  public void testParseDate_missingYearMonthDay() {
    // parseDate("Mar-10-2025") => not a valid format => exception
    assertThrows(InvalidCalenderOperationException.class, () -> {
      DateTimeHelper.parseDate("Mar-10-2025");
    });
  }

  @Test
  public void testConvertToLocalDateTime() throws InvalidCalenderOperationException {
    // parse something, then convert
    Date d = DateTimeHelper.parseDate("2025-03-10T22:00"); // 10pm
    LocalDateTime ldt = DateTimeHelper.convertToLocalDateTime(d);
    assertEquals(2025, ldt.getYear());
    assertEquals(3, ldt.getMonthValue());
    assertEquals(10, ldt.getDayOfMonth());
    assertEquals(22, ldt.getHour());
    assertEquals(0, ldt.getMinute());
  }

  @Test
  public void testToStartOfDay() throws InvalidCalenderOperationException {
    // If we parse 2025-03-10T14:30 => toStartOfDay => 2025-03-10T00:00 in default zone
    Date d = DateTimeHelper.parseDate("2025-03-10T14:30");
    Date startOfDay = DateTimeHelper.toStartOfDay(d);
    LocalDateTime ldt = DateTimeHelper.convertToLocalDateTime(startOfDay);
    assertEquals(0, ldt.getHour());
    assertEquals(0, ldt.getMinute());
    assertEquals(0, ldt.getSecond());
  }

  @Test
  public void testToEndOfDay() throws InvalidCalenderOperationException {
    // parse 2025-03-10T08:00 => toEndOfDay => 2025-03-10T23:59:59.999...
    Date d = DateTimeHelper.parseDate("2025-03-10T08:00");
    Date endOfDay = DateTimeHelper.toEndOfDay(d);
    LocalDateTime ldt = DateTimeHelper.convertToLocalDateTime(endOfDay);
    assertEquals(23, ldt.getHour());
    assertEquals(59, ldt.getMinute());
    assertEquals(59, ldt.getSecond());
    // nano might be 999999999
  }

  @Test
  public void testFormatDateTime() throws InvalidCalenderOperationException {
    // parse => "2025-03-10T08:00", then format => "2025-03-10T08:00"
    Date d = DateTimeHelper.parseDate("2025-03-10T08:00");
    String str = DateTimeHelper.formatDateTime(d);
    assertEquals("2025-03-10T08:00", str);
  }

  @Test
  public void testParseDateTime_noTimeComponent() throws InvalidCalenderOperationException {
    // parseDateTime("2025-03-10") => same as parseDate
    Date d = DateTimeHelper.parseDateTime("2025-03-10");
    LocalDateTime ldt = DateTimeHelper.convertToLocalDateTime(d);
    assertEquals(2025, ldt.getYear());
    assertEquals(3, ldt.getMonthValue());
    assertEquals(10, ldt.getDayOfMonth());
    assertEquals(0, ldt.getHour());
    assertEquals(0, ldt.getMinute());
  }

  @Test
  public void testParseDateTime_withTime() throws InvalidCalenderOperationException {
    // parseDateTime("2025-03-10T08:30")
    Date d = DateTimeHelper.parseDateTime("2025-03-10T08:30");
    String formatted = DateTimeHelper.formatDateTime(d);
    assertEquals("2025-03-10T08:30", formatted);
  }

  @Test
  public void testParseDateTime_invalid() {
    assertThrows(InvalidCalenderOperationException.class, () -> {
      DateTimeHelper.parseDateTime("03/10/2025 08:30");
    });
  }

}