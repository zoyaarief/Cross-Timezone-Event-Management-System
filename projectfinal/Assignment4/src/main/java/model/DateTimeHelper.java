package model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * AbstractDateTimeHelper centralizes common date/time operations.
 * It provides static methods for parsing, converting, and formatting dates.
 */
abstract class DateTimeHelper {

  /**
   * Formatter for date/time strings in "yyyy-MM-dd'T'HH:mm" format.
   */
  protected static final DateTimeFormatter DATE_TIME_FORMATTER =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  /**
   * The current default ZoneId used by all methods below.
   * It is initialized to "America/New_York" to preserve existing behavior.
   */
  private static ZoneId CURRENT_ZONE = ZoneId.of("America/New_York");

  /**
   * Allows overriding the default time zone at runtime.
   * If never called, the code behaves just like before (using America/New_York).
   *
   * @param zoneName a valid zone string (e.g., "America/Los_Angeles")
   * @throws RuntimeException if zoneName is invalid
   */
  protected static void setDefaultZone(String zoneName) {
    //System.out.println("STEP 2 - Setting default zone to: " + zoneName);
    CURRENT_ZONE = ZoneId.of(zoneName);
  }

  /**
   * Returns the current default ZoneId in use.
   */
  protected static ZoneId getDefaultZone() {
    return CURRENT_ZONE;
  }

  /**
   * Parses a date/time string into a Date.
   * If time is missing, "T00:00" is appended.
   *
   * @param dateString the date/time string to parse
   * @return the parsed Date object
   * @throws InvalidCalenderOperationException if parsing fails
   */
  protected static Date parseDate(String dateString)
          throws InvalidCalenderOperationException {
    if (!dateString.contains("T")) {
      if (!dateString.matches("\\d{4}-\\d{2}-\\d{2}")) {
        throw new InvalidCalenderOperationException(
                "Expected date in format YYYY-MM-DD for all-day events.");
      }
      dateString += "T00:00";
    }
    try {
      LocalDateTime ldt = LocalDateTime.parse(dateString, DATE_TIME_FORMATTER);
      ZonedDateTime zdt = ldt.atZone(CURRENT_ZONE);  // Use CURRENT_ZONE
      return Date.from(zdt.toInstant());
    } catch (Exception e) {
      throw new InvalidCalenderOperationException(
              "Invalid date/time format: " + dateString);
    }
  }

  /**
   * Converts a Date to a LocalDateTime in the current default zone.
   *
   * @param date the Date to convert
   * @return the corresponding LocalDateTime
   */
  protected static LocalDateTime convertToLocalDateTime(Date date) {
    return date.toInstant().atZone(CURRENT_ZONE).toLocalDateTime();
  }

  /**
   * Returns a Date representing the start of the day (00:00:00.000) for the given Date.
   *
   * @param date the Date for which to compute the start of day
   * @return a Date at 00:00 of the same day
   */
  protected static Date toStartOfDay(Date date) {
    LocalDateTime ldt = convertToLocalDateTime(date)
            .withHour(0).withMinute(0).withSecond(0).withNano(0);
    return Date.from(ldt.atZone(CURRENT_ZONE).toInstant());
  }

  /**
   * Returns a Date representing the end of the day (23:59:59.999999999) for the given Date.
   *
   * @param date the Date for which to compute the end of day
   * @return a Date at 23:59:59.999 of the same day
   */
  protected static Date toEndOfDay(Date date) {
    LocalDateTime ldt = convertToLocalDateTime(date)
            .withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    return Date.from(ldt.atZone(CURRENT_ZONE).toInstant());
  }

  /**
   * Formats the given Date into a string in "yyyy-MM-dd'T'HH:mm" format,
   * using the current default zone for interpretation.
   *
   * @param date the Date to format
   * @return a string like "2025-03-28T14:05"
   */
  protected static String formatDateTime(Date date) {
    LocalDateTime ldt = convertToLocalDateTime(date);
    return ldt.format(DATE_TIME_FORMATTER);
  }

  /**
   * Parses a date/time string into a Date.
   * If the time component is missing, "T00:00" is appended.
   *
   * @param dateTime the date/time string to parse
   * @return the resulting Date object
   * @throws InvalidCalenderOperationException if parsing fails
   */
  protected static Date parseDateTime(String dateTime)
          throws InvalidCalenderOperationException {
    if (!dateTime.contains("T")) {
      dateTime += "T00:00";
    }
    try {
      LocalDateTime ldt = LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
      return Date.from(ldt.atZone(CURRENT_ZONE).toInstant());
    } catch (Exception e) {
      throw new InvalidCalenderOperationException("Invalid date/time: " + dateTime);
    }
  }
}
