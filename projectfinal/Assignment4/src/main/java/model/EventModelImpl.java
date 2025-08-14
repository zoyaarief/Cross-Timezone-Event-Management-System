package model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Implementation of IEventModel with a Builder pattern.
 */
public class EventModelImpl implements IEventModel {

  /**
   * Formatter for date/time strings in "yyyy-MM-dd'T'HH:mm" format.
   */
  public static final DateTimeFormatter DATE_TIME_FORMATTER =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  /**
   * For printing in the format "Mon Feb 10 12:00:00 2025" without zone label.
   */
  private static final DateTimeFormatter PRINT_FORMAT =
          DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy");


  private static ZoneId DEFAULT_ZONE = null;

  /**
   * Allows  Calendar code to set the zone that will be used by
   * getBuilder(String, String) calls. For example, in CalenderModelImpl’s
   * constructor or createEvent method, do:
   * EventModelImpl.setDefaultZone(this.zoneId);
   */
  public static void setDefaultZone(ZoneId zone) {
    DEFAULT_ZONE = zone;
  }

  // fields
  String eventName;
  /**
   * The start date/time of this event.
   */
  Date startDateTime;
  /**
   * The end date/time of this event, or null if not specified.
   */
  Date endDateTime;
  /**
   * Detailed description of this event (optional).
   */
  String longDescription;
  /**
   * The location where this event occurs (optional).
   */
  String location;
  /**
   * Status indicating this event's current state (optional).
   */
  EventStatus status;

  /**
   * A per-event ZoneId, if your design requires each event to carry its own zone.
   * If null, we will fallback to the system default in toString().
   */
  ZoneId zoneId;

  /**
   * Private constructor for creating new EventModelImpl instances.
   */
  private EventModelImpl(String eventName,
                         Date startDateTime,
                         Date endDateTime,
                         String location,
                         String longDescription,
                         EventStatus status,
                         ZoneId zoneId) {
    this.eventName = eventName;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.location = location;
    this.longDescription = longDescription;
    this.status = status;
    this.zoneId = zoneId;
  }

  /**
   * Private constructor for creating new EventModelImpl instances.
   */
  private EventModelImpl(String eventName,
                         Date startDateTime,
                         Date endDateTime,
                         String location,
                         String longDescription,
                         EventStatus status) {
    this(eventName, startDateTime, endDateTime, location, longDescription, status, null);
  }

  /**
   * creates a copy of event for copy feature.
   *
   * @param source event to be copied.
   * @return EventModelImpl
   */
  public static EventModelImpl copyOf(EventModelImpl source) {
    EventModelImpl copy = new EventModelImpl(
            source.eventName,
            source.startDateTime,
            source.endDateTime,
            source.location,
            source.longDescription,
            source.status
    );
    copy.zoneId = source.zoneId;
    return copy;
  }

  /**
   * The old builder method, but we remove the America/New_York
   * hard-code and use DEFAULT_ZONE instead.
   */
  public static EventBuilder getBuilder(String eventName, String startDateString)
          throws InvalidCalenderOperationException {
    // If no default zone is set, fall back to system default or throw an error
    ZoneId zoneToUse = (DEFAULT_ZONE != null)
            ? DEFAULT_ZONE
            : ZoneId.systemDefault();  // or throw new RuntimeException(...)

    return new EventBuilder(
            eventName,
            parseDate(startDateString, zoneToUse),
            zoneToUse
    );
  }

  /**
   * Overloaded getBuilder that accepts an explicit ZoneId
   * (unchanged from code).
   */
  public static EventBuilder getBuilder(String eventName, String startDateString, ZoneId zone)
          throws InvalidCalenderOperationException {
    return new EventBuilder(eventName, parseDate(startDateString, zone), zone);
  }

  /**
   * Overloaded parseDate that uses a provided ZoneId.
   */
  public static Date parseDate(String dateString, ZoneId zone)
          throws InvalidCalenderOperationException {
    if (!dateString.contains("T")) {
      if (!dateString.matches("\\d{4}-\\d{2}-\\d{2}")) {
        throw new InvalidCalenderOperationException(
                "Expected date in format YYYY-MM-DD for all-day events."
        );
      }
      dateString += "T00:00";
    }
    try {
      LocalDateTime ldt = LocalDateTime.parse(dateString, DATE_TIME_FORMATTER);
      ZonedDateTime zdt = ldt.atZone(zone);
      return Date.from(zdt.toInstant());
    } catch (Exception e) {
      throw new InvalidCalenderOperationException(
              "Invalid date/time format: " + dateString
      );
    }
  }


  @Override
  public boolean checkEventConflict(IEventModel other) {
    if (other == null) {
      return false;
    }
    Date otherStart = other.getStartDateTime();
    Date otherEnd = other.getEndDateTime();
    if (otherEnd == null) {
      otherEnd = otherStart;
    }
    Date myEnd = (endDateTime == null) ? startDateTime : endDateTime;
    return (startDateTime.before(otherEnd) && myEnd.after(otherStart));
  }

  @Override
  public Date getStartDateTime() {
    return startDateTime;
  }

  @Override
  public Date getEndDateTime() {
    return endDateTime;
  }

  @Override
  public String getEventName() {
    return eventName;
  }

  @Override
  public String getLocation() {
    return location;
  }

  @Override
  public String getLongDescription() {
    return longDescription;
  }

  @Override
  public EventStatus getStatus() {
    return status;
  }

  @Override
  public String toString() {
    ZoneId z = (this.zoneId != null) ? this.zoneId : ZoneId.systemDefault();
    LocalDateTime startLDT = startDateTime.toInstant().atZone(z).toLocalDateTime();
    LocalDateTime endLDT = (endDateTime != null)
            ? endDateTime.toInstant().atZone(z).toLocalDateTime()
            : startLDT;
    return String.format(
            "Event: %s | Start: %s | End: %s | Location: %s | Status: %s",
            eventName,
            startLDT.format(DATE_TIME_FORMATTER),
            endLDT.format(DATE_TIME_FORMATTER),
            location,
            (status != null ? status.name() : "N/A")
    );
  }

  // Setters
  public void setStartDateTime(Date newStart) {
    this.startDateTime = newStart;
  }

  public void setEndDateTime(Date newEnd) {
    this.endDateTime = newEnd;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public void setZoneId(ZoneId zoneId) {
    this.zoneId = zoneId;
  }

  /**
   * Implementation of IEventModel with a Builder pattern.
   */
  public static class EventBuilder {
    private String eventName;
    private Date startDateTime;
    private Date endDateTime;
    private String longDescription;
    private String location;
    private EventStatus status;
    private ZoneId zoneId; // optional

    private EventBuilder(String eventName, Date startDateTime) {
      this.eventName = eventName;
      this.startDateTime = startDateTime;
    }

    private EventBuilder(String eventName, Date startDateTime, ZoneId zone) {
      this.eventName = eventName;
      this.startDateTime = startDateTime;
      this.zoneId = zone;
    }

    /**
     * Produces a correct fromatted end Date string.
     *
     * @param endDateString input enddate string.
     */
    public EventBuilder endDateString(String endDateString)
            throws InvalidCalenderOperationException {
      ZoneId zToUse = (zoneId != null) ? zoneId
              : ((DEFAULT_ZONE != null) ? DEFAULT_ZONE : ZoneId.systemDefault());
      this.endDateTime = parseDate(endDateString, zToUse);
      return this;
    }

    public EventBuilder location(String location) {
      this.location = location;
      return this;
    }

    public EventBuilder longDescription(String desc) {
      this.longDescription = desc;
      return this;
    }

    public EventBuilder status(EventStatus s) {
      this.status = s;
      return this;
    }

    public EventBuilder zoneId(ZoneId z) {
      this.zoneId = z;
      return this;
    }

    /**
     * Build function which retuen the final event created using builder pattern.
     *
     * @return IEventModel
     */
    public IEventModel build() {
      return new EventModelImpl(
              eventName,
              startDateTime,
              endDateTime,
              location,
              longDescription,
              status,
              zoneId
      );
    }
  }
}
