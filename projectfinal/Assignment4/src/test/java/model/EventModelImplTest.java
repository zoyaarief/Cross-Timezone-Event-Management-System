package model;


import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/** Tests EventModelImpl for builder-based creation, getters, and setters. */
public class EventModelImplTest {

  
  /** Formatter matching "yyyy-MM-dd'T'HH:mm" in the same zone as the model. */
  private SimpleDateFormat sdf;

  /** Initializes the SimpleDateFormat with America/New_York time zone. */
  @Before
  public void setUp() {
    //CalendarManager.resetGlobalState();
    sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
  }

  /**
   * Helper method: Build an event using the zone-based builder,
   * then (optionally) tweak fields via setters to get exact test scenario.
   */
  private EventModelImpl buildEvent(
          String eventName,
          String startString,
          String endString,
          ZoneId zone,
          String location,
          String desc,
          EventStatus st
  ) throws InvalidCalenderOperationException {
    // Step 1: use the builder
    IEventModel e = EventModelImpl.getBuilder(eventName, startString, zone)
            .endDateString(endString)
            .location(location)
            .longDescription(desc)
            .status(st)
            .zoneId(zone)
            .build();

    // Step 2: If we need to do something special (like set endDateTime = null),
    // we could do it here via casting + setters. But in normal usage,
    // the builder is enough. We'll just return e as EventModelImpl for tests.
    return (EventModelImpl) e;
  }

  @Test
  public void testGetBuilder_noZone_defaultToSystemZone() throws InvalidCalenderOperationException {
    // If we haven't set the default zone, it should fallback to system default.
    IEventModel event = EventModelImpl.getBuilder("EventA", "2025-02-10T10:00")
            .endDateString("2025-02-10T12:00")
            .location("Room101")
            .longDescription("Desc")
            .status(EventStatus.PRIVATE)
            .build();
    assertNotNull(event);
    assertEquals("EventA", event.getEventName());
    assertEquals("Room101", event.getLocation());
    assertEquals("Desc", event.getLongDescription());
    assertEquals(EventStatus.PRIVATE, event.getStatus());

    Date start = event.getStartDateTime();
    Date end   = event.getEndDateTime();
    assertNotNull(start);
    assertNotNull(end);
    assertTrue(end.after(start));
  }

  @Test
  public void testGetBuilder_withDefaultZone() throws InvalidCalenderOperationException {
    // If we set a default zone to "America/Los_Angeles"
    EventModelImpl.setDefaultZone(ZoneId.of("America/Los_Angeles"));

    // Then getBuilder(...) uses that zone for parsing
    IEventModel event = EventModelImpl.getBuilder("LAEvent", "2025-03-10T09:00")
            .endDateString("2025-03-10T10:00")
            .build();
    assertNotNull(event);
    assertEquals("LAEvent", event.getEventName());
    // Start/end times are in LA, but we won't do an exact date check here.
  }

  @Test
  public void testGetBuilder_withExplicitZone() throws InvalidCalenderOperationException {
    // Overload that includes a zone:
    IEventModel event = EventModelImpl.getBuilder("ExplicitZoneEvent",
                    "2025-04-10T11:00", ZoneId.of("Europe/London"))
            .endDateString("2025-04-10T12:30")
            .zoneId(ZoneId.of("Europe/London"))
            .build();
    assertEquals("ExplicitZoneEvent", event.getEventName());
    EventModelImpl impl = (EventModelImpl) event;
    assertEquals(ZoneId.of("Europe/London"), impl.zoneId);
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testGetBuilder_parseDate_invalidFormat() throws InvalidCalenderOperationException {
    // "2025/02/10 09:00" => invalid
    EventModelImpl.getBuilder("BadFormat", "2025/02/10 09:00").build();
  }

  @Test
  public void testParseDate_withTime() throws InvalidCalenderOperationException {
    // parseDate(...) with explicit zone
    Date d = EventModelImpl.parseDate("2025-02-10T14:30", ZoneId.of("America/New_York"));
    assertNotNull(d);
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseDate_missingYearMonthDay() throws InvalidCalenderOperationException {
    EventModelImpl.parseDate("Feb-10-2025", ZoneId.of("America/New_York"));
  }

  @Test
  public void testCopyOf_retainsFields() throws InvalidCalenderOperationException {
    // We'll build an event with the builder, then call copyOf
    EventModelImpl original = buildEvent(
            "OriginalEvt",
            "2025-02-10T10:00",
            "2025-02-10T12:00",
            ZoneId.of("America/New_York"),
            "RoomX",
            "OriginalDesc",
            EventStatus.PUBLIC
    );
    EventModelImpl copy = EventModelImpl.copyOf(original);

    assertNotSame(original, copy);
    assertEquals(original.eventName, copy.eventName);
    assertEquals(original.startDateTime, copy.startDateTime);
    assertEquals(original.endDateTime, copy.endDateTime);
    assertEquals(original.location, copy.location);
    assertEquals(original.longDescription, copy.longDescription);
    assertEquals(original.status, copy.status);
    assertEquals(original.zoneId, copy.zoneId);
  }

  @Test
  public void testCheckEventConflict_noConflict() throws InvalidCalenderOperationException {
    // event A: [1000..2000], event B: [2001..3000] => no overlap
    EventModelImpl a = buildEvent(
            "EvtA",
            "1970-01-01T00:00",
            "1970-01-01T00:00",
            ZoneId.systemDefault(),
            null,
            null,
            EventStatus.PRIVATE
    );
    a.setStartDateTime(new Date(1000L));
    a.setEndDateTime(new Date(2000L));

    EventModelImpl b = buildEvent(
            "EvtB",
            "1970-01-01T00:00",
            "1970-01-01T00:00",
            ZoneId.systemDefault(),
            null,
            null,
            EventStatus.PRIVATE
    );
    b.setStartDateTime(new Date(2001L));
    b.setEndDateTime(new Date(3000L));

    assertFalse(a.checkEventConflict(b));
    assertFalse(b.checkEventConflict(a));
  }

  @Test
  public void testCheckEventConflict_overlap() throws InvalidCalenderOperationException {
    // event A: [1000..2000], event B: [1500..1600] => overlap
    EventModelImpl a = buildEvent("EvtA", "1970-01-01T00:00",
            "1970-01-01T00:00",
            ZoneId.systemDefault(), null, null, EventStatus.PRIVATE);
    a.setStartDateTime(new Date(1000L));
    a.setEndDateTime(new Date(2000L));

    EventModelImpl b = buildEvent("EvtB", "1970-01-01T00:00",
            "1970-01-01T00:00",
            ZoneId.systemDefault(), null, null, EventStatus.PRIVATE);
    b.setStartDateTime(new Date(1500L));
    b.setEndDateTime(new Date(1600L));

    assertTrue(a.checkEventConflict(b));
    assertTrue(b.checkEventConflict(a));
  }

  @Test
  public void testToString_includesMainFields() throws InvalidCalenderOperationException {
    EventModelImpl.setDefaultZone(ZoneId.of("America/Los_Angeles"));
    IEventModel e = EventModelImpl.getBuilder("ToStringTest", "2025-02-10T10:00")
            .endDateString("2025-02-10T11:00")
            .location("ConfRoom")
            .status(EventStatus.PRIVATE)
            .build();
    String str = e.toString();
    assertTrue(str.contains("ToStringTest"));
    assertTrue(str.contains("2025-02-10T10:00"));
    assertTrue(str.contains("2025-02-10T11:00"));
    assertTrue(str.contains("ConfRoom"));
    assertTrue(str.contains("PRIVATE"));
  }

  @Test
  public void testSetters_1() throws InvalidCalenderOperationException {
    // We'll build a simple event, then call setters
    EventModelImpl e = buildEvent(
            "SetterEvt",
            "2025-02-10T09:00",
            "2025-02-10T10:00",
            ZoneId.of("America/New_York"),
            "Loc",
            "Desc",
            EventStatus.PUBLIC
    );

    e.setEventName("NewName");
    e.setStartDateTime(new Date(3000L));
    e.setEndDateTime(new Date(4000L));
    e.setZoneId(ZoneId.of("America/Los_Angeles"));

    assertEquals("NewName", e.getEventName());
    assertEquals(new Date(3000L), e.getStartDateTime());
    assertEquals(new Date(4000L), e.getEndDateTime());
    assertEquals(ZoneId.of("America/Los_Angeles"), e.zoneId);
  }

  /** Tests that setters properly update the event's fields. */
  @Test
  public void testSetters() throws InvalidCalenderOperationException {
    String initialName = "Initial Event";
    String initialStart = "2024-10-10T10:00";
    String initialEnd = "2024-10-10T11:00";
    IEventModel event = EventModelImpl.getBuilder(initialName, initialStart)
            .endDateString(initialEnd)
            .build();
    EventModelImpl evt = (EventModelImpl) event;
    String newName = "Updated Event";
    String newStart = "2024-10-11T12:00";
    String newEnd = "2024-10-11T13:00";
    evt.setEventName(newName);
    evt.setStartDateTime(parseDate(newStart));
    evt.setEndDateTime(parseDate(newEnd));
    assertEquals("Event name update failed", newName, evt.getEventName());
    assertEquals("Start time update failed", newStart, sdf.format(evt.getStartDateTime()));
    assertEquals("End time update failed", newEnd, sdf.format(evt.getEndDateTime()));
  }

  /** Mimics the model's parse logic to convert a date string to a Date object. */
  private Date parseDate(String dateStr) throws InvalidCalenderOperationException {
    if (!dateStr.contains("T")) {
      if (!dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
        throw new InvalidCalenderOperationException("Expected date in " +
                "format YYYY-MM-DD for all-day events.");
      }
      dateStr += "T00:00";
    }
    try {
      LocalDateTime ldt = LocalDateTime.parse(dateStr, EventModelImpl.DATE_TIME_FORMATTER);
      ZonedDateTime zdt = ldt.atZone(ZoneId.of("America/New_York"));
      return Date.from(zdt.toInstant());
    } catch (Exception e) {
      throw new InvalidCalenderOperationException("Invalid date/time format: " + dateStr);
    }
  }
}
