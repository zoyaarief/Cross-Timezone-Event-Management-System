package model;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for CalenderModelImpl. Tests single, recurring, copy and time zone ops.
 */
public class CalenderModelImplTest {

  /** Model under test for general single and recurring event tests. */
  private CalenderModelImpl model;

  /** Source calendar for copy tests. */
  private CalenderModelImpl srcCal;
  /** Target calendar for copy tests. */
  private CalenderModelImpl tgtCal;

  /** Day used for some single event tests. */
  private final String day = "2025-05-05";
  /** Name for certain single event tests. */
  private final String eventName = "SoloEvent";
  /** Start for single event. */
  private final String eventStart = day + "T10:00";
  /** End for single event. */
  private final String eventEnd = day + "T11:00";

  /**
   * Minimal IEventModel stub for testing basic event behavior.
   */
  private static class TestEvent implements IEventModel {
    /** Event name. */
    private final String eventName;
    /** Event start date/time. */
    private final Date start;
    /** Event end date/time. */
    private final Date end;
    /** Event location. */
    private final String location;
    /** Event description. */
    private final String desc;
    /** Event status. */
    private final EventStatus status;

    /**
     * Constructor for TestEvent.
     * @param name event name
     * @param start event start date/time
     * @param end event end date/time
     * @param loc event location
     * @param desc event description
     * @param st event status
     */
    TestEvent(String name, Date start, Date end,
              String loc, String desc, EventStatus st) {
      this.eventName = name;
      this.start = start;
      this.end = end;
      this.location = loc;
      this.desc = desc;
      this.status = st;
    }

    /**
     * Always returns false for conflict check.
     * @param other other event model
     * @return false always
     */
    @Override
    public boolean checkEventConflict(IEventModel other) {
      return false;
    }

    /**
     * Returns the event's start date/time.
     * @return start date/time
     */
    @Override
    public Date getStartDateTime() {
      return start;
    }

    /**
     * Returns the event's end date/time.
     * @return end date/time
     */
    @Override
    public Date getEndDateTime() {
      return end;
    }

    /**
     * Returns the event name.
     * @return event name
     */
    @Override
    public String getEventName() {
      return eventName;
    }

    /**
     * Returns the event location.
     * @return location string
     */
    @Override
    public String getLocation() {
      return location;
    }

    /**
     * Returns the long description.
     * @return description string
     */
    @Override
    public String getLongDescription() {
      return desc;
    }

    /**
     * Returns the event status.
     * @return event status
     */
    @Override
    public EventStatus getStatus() {
      return status;
    }
  }

  /**
   * Setup method to reset state and create fresh calendar instances.
   */
  @Before
  public void setUp() {
    //CalendarManager.resetGlobalState();
    model = new CalenderModelImpl("TestCalendar", ZoneId.of("America/New_York"));
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
    srcCal = new CalenderModelImpl("SrcCalendar", ZoneId.of("America/Los_Angeles"));
    tgtCal = new CalenderModelImpl("TgtCalendar", ZoneId.of("America/New_York"));
  }

  /**
   * Test copying a single event to another calendar.
   * @throws InvalidCalenderOperationException if operation fails
   */
  @Test
  public void testCopySingleEvent_basic() throws InvalidCalenderOperationException {
    Date start = srcCal.parseDateLocal("2025-02-10T10:00");
    Date end = srcCal.parseDateLocal("2025-02-10T12:00");
    IEventModel e = new TestEvent("MeetingA", start, end, "Room101",
            "Team meeting", EventStatus.PRIVATE);
    srcCal.addSingleEvent(e, false);
    srcCal.copySingleEvent("MeetingA", "2025-02-10T10:00",
            tgtCal, "2025-03-11T09:00");
    List<IEventModel> found = tgtCal.searchEvents("2025-03-11T00:00",
            "2025-03-12T00:00");
    assertEquals(1, found.size());
    IEventModel copied = found.get(0);
    assertEquals("MeetingA", copied.getEventName());
    Date expectedStart = tgtCal.parseDateLocal("2025-03-11T09:00");
    assertEquals(expectedStart, copied.getStartDateTime());
    Date expectedEnd = tgtCal.parseDateLocal("2025-03-11T11:00");
    assertEquals(expectedEnd, copied.getEndDateTime());
  }

  /**
   * Test that copying a non-existent event throws an exception.
   * @throws InvalidCalenderOperationException always thrown
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testCopySingleEvent_noSuchEvent()
          throws InvalidCalenderOperationException {
    srcCal.copySingleEvent("NonExistent", "2025-02-10T10:00",
            tgtCal, "2025-03-11T09:00");
  }

  /**
   * Test copying events on a specific day to a target calendar.
   * @throws InvalidCalenderOperationException if operation fails
   */
  @Test
  public void testCopyEventsOnDay_basic() throws InvalidCalenderOperationException {
    Date s1 = srcCal.parseDateLocal("2025-02-10T08:00");
    Date e1 = srcCal.parseDateLocal("2025-02-10T09:00");
    IEventModel eA = new TestEvent("EventA", s1, e1, null, null, EventStatus.PUBLIC);
    srcCal.addSingleEvent(eA, false);
    Date s2 = srcCal.parseDateLocal("2025-02-10T15:00");
    Date e2 = srcCal.parseDateLocal("2025-02-10T16:00");
    IEventModel eB = new TestEvent("EventB", s2, e2, null, null, EventStatus.PUBLIC);
    srcCal.addSingleEvent(eB, false);
    srcCal.copyEventsOnDay("2025-02-10", tgtCal, "2025-03-11");
    List<IEventModel> found = tgtCal.searchEvents("2025-03-11");
    assertEquals(2, found.size());
  }

  /**
   * Test copying events between given date/time ranges.
   * @throws InvalidCalenderOperationException if operation fails
   */
  @Test
  public void testCopyEventsBetween_basic()
          throws InvalidCalenderOperationException {
    Date s1 = srcCal.parseDateLocal("2025-02-10T08:00");
    Date e1 = srcCal.parseDateLocal("2025-02-10T10:00");
    IEventModel e1Model = new TestEvent("BetweenEvent1", s1, e1,
            null, null, EventStatus.PUBLIC);
    srcCal.addSingleEvent(e1Model, false);
    Date s2 = srcCal.parseDateLocal("2025-02-12T13:00");
    Date e2 = srcCal.parseDateLocal("2025-02-12T14:00");
    IEventModel e2Model = new TestEvent("BetweenEvent2", s2, e2,
            null, null, EventStatus.PUBLIC);
    srcCal.addSingleEvent(e2Model, false);
    srcCal.copyEventsBetween("2025-02-10T00:00",
            "2025-02-15T23:59",
            tgtCal, "2025-03-20T00:00");
    List<IEventModel> found = tgtCal.searchEvents("2025-03-20", "2025-03-25");
    assertEquals(2, found.size());
  }

  /**
   * Test that a fresh reset creates a new calendar with no events.
   * @throws InvalidCalenderOperationException if operation fails
   */
  @Test
  public void testResetGlobalStateCreatesFreshInstance()
          throws InvalidCalenderOperationException {
    ICalenderModel newCal = new CalenderModelImpl("FreshCal",
            ZoneId.of("America/New_York"));
    assertEquals("FreshCal", newCal.getCalendarName());
    List<IEventModel> events = newCal.getEvents();
    assertTrue("The events list should be empty after " +
            "a fresh reset.", events.isEmpty());
  }

  /**
   * Test that the constructor sets day, month, and year correctly.
   */
  @Test
  public void testConstructor_setsDayMonthYearCorrectly() {
    CalenderModelImpl cal = new CalenderModelImpl("MyCal",
            ZoneId.of("America/New_York"));
    assertTrue("Day of week should be between 1 and 31",
            cal.getCurrentDay() >= 1 && cal.getCurrentDay() <= 31);
    assertNotNull("Month should not be null", cal.getCurrentMonth());
    int year = cal.getCurrentYear();
    assertTrue("Year should be >= 2020", year >= 2020 && year <= 2100);
  }

  /**
   * Test that adding an event with end before start throws an error.
   * @throws InvalidCalenderOperationException always thrown
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testAddSingleEvent_endBeforeStart_throwsError()
          throws InvalidCalenderOperationException {
    Date start = model.parseDateLocal("2025-04-11T10:00");
    Date end = model.parseDateLocal("2025-04-11T09:00");
    IEventModel bad = new CalenderModelImplTest.TestEvent("Bad", start, end,
            "Nowhere", "Oops", EventStatus.PUBLIC);
    model.addSingleEvent(bad, false);
  }

  /**
   * Test that an event with null end time is set as an all-day event.
   * @throws Exception if operation fails
   */
  @Test
  public void testAddSingleEvent_setsAllDayIfEndDateIsNull()
          throws Exception {
    Date start = model.parseDateLocal("2025-04-10T00:00");
    IEventModel event = EventModelImpl.getBuilder("NoEndEvent",
                    "2025-04-10T00:00",
                    model.getZoneId())
            .location("Park")
            .longDescription("All day picnic")
            .status(EventStatus.PUBLIC)
            .build();
    model.addSingleEvent(event, false);
    List<IEventModel> events = model.getEvents();
    assertEquals(1, events.size());
    IEventModel added = events.get(0);
    assertNotNull(added.getEndDateTime());
    assertTrue(added.getEndDateTime().after(added.getStartDateTime()));
  }

  /**
   * Test that adding a conflicting event throws an error.
   * @throws Exception if operation fails
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testAddSingleEvent_conflictingEvent_throwsError()
          throws Exception {
    IEventModel e1 = EventModelImpl.getBuilder("Meeting",
                    "2025-04-12T10:00", model.getZoneId())
            .endDateString("2025-04-12T11:00")
            .build();
    IEventModel e2 = EventModelImpl.getBuilder("Meeting",
                    "2025-04-12T10:30", model.getZoneId())
            .endDateString("2025-04-12T11:30")
            .build();
    model.addSingleEvent(e1, false);
    model.addSingleEvent(e2, false);
  }

  /**
   * Test adding a recurring event over weeks and checking conflict resolution.
   * @throws Exception if operation fails
   */
  @Test
  public void testAddRecurringEvent_basicAndConflicts()
          throws Exception {
    IEventModel baseEvent = EventModelImpl.getBuilder("Yoga",
                    "2025-04-07T10:00", model.getZoneId())
            .endDateString("2025-04-07T11:00")
            .build();
    model.addRecurringEvent(baseEvent, false, "MW",
            2);
    List<IEventModel> events = model.getEvents();
    assertEquals(4, events.size());
    List<String> expectedDates = List.of(
            "2025-04-07T10:00", "2025-04-09T10:00",
            "2025-04-14T10:00", "2025-04-16T10:00"
    );
    for (String date : expectedDates) {
      boolean found = events.stream().anyMatch(e ->
              model.formatDateLocal(e.getStartDateTime()).equals(date));
      assertTrue("Missing recurring event on " + date, found);
    }
  }

  /**
   * Test that adding a recurring event with 0 weeks throws an error.
   * @throws Exception if operation fails
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testAddRecurringEvent_noOfWeeksTooSmall_shouldThrow()
          throws Exception {
    IEventModel base = EventModelImpl.getBuilder("ErrorEvent",
                    "2025-05-01T10:00", model.getZoneId())
            .endDateString("2025-05-01T11:00")
            .build();
    model.addRecurringEvent(base, false, "M", 0);
  }

  /**
   * Test that recurring events added until a given date work correctly.
   * @throws Exception if operation fails
   */
  @Test
  public void testAddRecurringEventUntil_basicCase() throws Exception {
    IEventModel event = EventModelImpl.getBuilder("Zumba",
                    "2025-04-07T10:00", model.getZoneId())
            .endDateString("2025-04-07T11:00")
            .build();
    model.addRecurringEvent(event, false, "MW",
            "2025-04-20T23:59");
    List<IEventModel> events = model.getEvents();
    assertEquals(4, events.size());
  }

  /**
   * Test that recurring events without time in end date append T23:59.
   * @throws Exception if operation fails
   */
  @Test
  public void testAddRecurringEventUntil_allDayFormat_shouldAppendT2359()
          throws Exception {
    IEventModel event = EventModelImpl.getBuilder("Spin",
                    "2025-04-08T09:00", model.getZoneId())
            .endDateString("2025-04-08T10:00")
            .build();
    model.addRecurringEvent(event, false, "T",
            "2025-04-22");
    List<IEventModel> events = model.getEvents();
    assertEquals(3, events.size());
  }

  /**
   * Test that an invalid until date format in recurring event throws error.
   * @throws Exception if operation fails
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testAddRecurringEventUntil_invalidDateFormat_shouldThrow()
          throws Exception {
    IEventModel event = EventModelImpl.getBuilder("Invalid",
                    "2025-04-09T09:00", model.getZoneId())
            .endDateString("2025-04-09T10:00")
            .build();
    model.addRecurringEvent(event, false, "W",
            "not-a-date");
  }

  /**
   * Test that an invalid short until date format throws an error.
   * @throws Exception if operation fails
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testAddRecurringEventUntil_invalidShortFormat_shouldThrow()
          throws Exception {
    IEventModel event = EventModelImpl.getBuilder("Invalid2",
                    "2025-04-09T09:00", model.getZoneId())
            .endDateString("2025-04-09T10:00")
            .build();
    model.addRecurringEvent(event, false, "W",
            "04-22-2025");
  }

  /**
   * Test that recurring event until a conflicting event throws an error.
   * @throws Exception if operation fails
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testAddRecurringEventUntil_conflictingEvent_shouldThrow()
          throws Exception {
    IEventModel event = EventModelImpl.getBuilder("Clash",
                    "2025-04-07T10:00", model.getZoneId())
            .endDateString("2025-04-07T11:00")
            .build();
    model.addRecurringEvent(event, false, "M", "2025-04-28");
    model.addRecurringEvent(event, false, "M", "2025-04-28");
  }

  /**
   * Test that adding a recurring event with invalid week count throws error.
   * @throws Exception if operation fails
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testAddRecurringEvent_invalidWeeks_shouldThrow()
          throws Exception {
    IEventModel event = EventModelImpl.getBuilder("Yoga",
                    "2025-04-10T08:00", model.getZoneId())
            .endDateString("2025-04-10T09:00")
            .build();
    model.addRecurringEvent(event, false, "M", 0);
  }

  /**
   * Test that recurring event with an end date works as intended.
   * @throws Exception if operation fails
   */
  @Test
  public void testAddRecurringEvent_withEndDate_shouldUseItCorrectly()
          throws Exception {
    IEventModel event = EventModelImpl.getBuilder("Workshop",
                    "2025-04-08T09:00", model.getZoneId())
            .endDateString("2025-04-08T10:30")
            .build();
    model.addRecurringEvent(event, false, "T", 1);
    List<IEventModel> events = model.getEvents();
    assertEquals(1, events.size());
    assertEquals("Workshop", events.get(0).getEventName());
  }

  /**
   * Test that showStatusOn returns BUSY when an event is scheduled.
   * @throws Exception if operation fails
   */
  @Test
  public void testShowStatusOn_returnsBusyWhenEventExists() throws Exception {
    Date start = model.parseDateLocal("2025-05-10T10:00");
    Date end = model.parseDateLocal("2025-05-10T12:00");
    IEventModel event = new CalenderModelImplTest.TestEvent(
            "BusyEvent", start, end, null, null, EventStatus.PUBLIC);
    model.addSingleEvent(event, false);
    AvailabilityStatus status = model.showStatusOn("2025-05-10T11:00");
    assertEquals(AvailabilityStatus.BUSY, status);
  }

  /**
   * Test that showStatusOn returns AVAILABLE when no event exists.
   * @throws Exception if operation fails
   */
  @Test
  public void testShowStatusOn_returnsAvailableWhenNoEvent() throws Exception {
    AvailabilityStatus status = model.showStatusOn("2025-05-10T11:00");
    assertEquals(AvailabilityStatus.AVAILABLE, status);
  }

  /**
   * Test that editing event with full match updates description.
   * @throws Exception if operation fails
   */
  @Test
  public void testEditEvent_fullMatch_shouldUpdateAndReindex()
          throws Exception {
    Date start = model.parseDateLocal("2025-05-01T10:00");
    Date end = model.parseDateLocal("2025-05-01T12:00");
    IEventModel event = EventModelImpl.getBuilder("UpdateMe",
                    "2025-05-01T10:00",
                    model.getZoneId())
            .endDateString("2025-05-01T12:00")
            .build();
    model.addSingleEvent(event, false);
    model.editEvent("description", "UpdateMe",
            start, end, "Updated description");
    List<IEventModel> results = model.searchEvents("2025-05-01T10:00");
    assertEquals("Updated description", results.get(0).getLongDescription());
  }

  /**
   * Test that editing an event with no matching start throws an exception.
   * @throws Exception if operation fails
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testEditEvent_noMatchingStart_shouldThrow() throws Exception {
    Date start = model.parseDateLocal("2025-05-01T10:00");
    Date end = model.parseDateLocal("2025-05-01T12:00");
    model.editEvent("description", "DoesNotExist",
            start, end, "Update");
  }

  /**
   * Test that editing event with mismatched end time throws an exception.
   * @throws Exception if operation fails
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testEditEvent_mismatchedEndTime_shouldThrow() throws Exception {
    Date start = model.parseDateLocal("2025-05-01T10:00");
    Date end = model.parseDateLocal("2025-05-01T12:00");
    IEventModel event = EventModelImpl.getBuilder("MismatchEnd",
                    "2025-05-01T10:00",
                    model.getZoneId())
            .endDateString("2025-05-01T12:00")
            .build();
    model.addSingleEvent(event, false);
    Date wrongEnd = model.parseDateLocal("2025-05-01T13:00");
    model.editEvent("description", "MismatchEnd",
            start, wrongEnd, "Update");
  }

  /**
   * Test that editing event with a start-only match updates location.
   * @throws Exception if operation fails
   */
  @Test
  public void testEditEvent_startOnlyMatch_shouldUpdateAndReindex()
          throws Exception {
    Date start = model.parseDateLocal("2025-05-01T10:00");
    Date end = model.parseDateLocal("2025-05-01T12:00");
    IEventModel event = EventModelImpl.getBuilder("EditStartOnly",
                    "2025-05-01T10:00",
                    model.getZoneId())
            .endDateString("2025-05-01T12:00")
            .build();
    model.addSingleEvent(event, false);
    model.editEvent("location", "EditStartOnly", start, "Room 101");
    List<IEventModel> results = model.searchEvents("2025-05-01T10:00");
    assertEquals("Room 101", results.get(0).getLocation());
  }

  /**
   * Test that editing event with start-only match but no event found throws error.
   * @throws Exception if operation fails
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testEditEvent_startOnlyNoneFound_shouldThrow() throws Exception {
    Date start = model.parseDateLocal("2025-05-01T10:00");
    model.editEvent("location", "UnknownEvent", start,
            "Nowhere");
  }

  /**
   * Test that editing event with name-only match updates status.
   * @throws Exception if operation fails
   */
  @Test
  public void testEditEvent_nameOnlyMatch_shouldUpdateAndReindex()
          throws Exception {
    Date start = model.parseDateLocal("2025-05-01T10:00");
    Date end = model.parseDateLocal("2025-05-01T12:00");
    IEventModel event = EventModelImpl.getBuilder("NameEdit",
                    "2025-05-01T10:00",
                    model.getZoneId())
            .endDateString("2025-05-01T12:00")
            .build();
    model.addSingleEvent(event, false);
    model.editEvent("status", "NameEdit", "PRIVATE");
    List<IEventModel> results = model.searchEvents("2025-05-01T10:00");
    assertEquals(EventStatus.PRIVATE, results.get(0).getStatus());
  }

  /**
   * Test that editing event with name-only match but no event found throws error.
   * @throws Exception if operation fails
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testEditEvent_nameOnlyNoneFound_shouldThrow() throws Exception {
    model.editEvent("status", "GhostEvent", "PRIVATE");
  }

  /**
   * Test that a recurring event with no end date time defaults to end of day.
   * @throws Exception if operation fails
   */
  @Test
  public void testRecurringEvent_noEndDateTime_defaultsToEndOfDay()
          throws Exception {
    IEventModel base = EventModelImpl.getBuilder("NoEndRec",
                    "2025-05-05T08:00", model.getZoneId())
            .build();
    model.addRecurringEvent(base, false, "M", 1);
    List<IEventModel> events = model.getEvents();
    assertEquals(1, events.size());
    IEventModel rec = events.get(0);
    assertNotNull(rec.getEndDateTime());
    assertTrue(rec.getEndDateTime().after(rec.getStartDateTime()));
  }

  /**
   * Test that a recurring event is indexed by name.
   * @throws Exception if operation fails
   */
  @Test
  public void testRecurringEvent_indexedByName() throws Exception {
    IEventModel base = EventModelImpl.getBuilder("Yoga",
                    "2025-05-05T09:00", model.getZoneId())
            .endDateString("2025-05-05T10:00")
            .build();
    model.addRecurringEvent(base, false, "M", 1);
    List<IEventModel> matches = model.searchEvents("2025-05-05");
    assertEquals(1, matches.size());
  }

  /**
   * Test that a recurring event with invalid until date throws an error.
   * @throws Exception if operation fails
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testRecurringEventUntil_invalidUntilDate_shouldThrow()
          throws Exception {
    IEventModel base = EventModelImpl.getBuilder("InvalidDate",
                    "2025-04-10T09:00",
                    model.getZoneId())
            .endDateString("2025-04-10T10:00").build();
    model.addRecurringEvent(base, false, "W",
            "bad-date");
  }

  /**
   * Test that recurring event until trims the end date correctly.
   * @throws Exception if operation fails
   */
  @Test
  public void testRecurringEventUntil_trimEndDateCorrectly()
          throws Exception {
    IEventModel base = EventModelImpl.getBuilder("TrimEvent",
                    "2025-04-07T09:00",
                    model.getZoneId())
            .endDateString("2025-04-07T11:00").build();
    model.addRecurringEvent(base, false, "M",
            "2025-04-07T10:00");
    IEventModel event = model.getEvents().get(0);
    assertEquals(model.parseDateLocal("2025-04-07T10:00"),
            event.getEndDateTime());
  }

  /**
   * Test that editing an event with wrong start/end times throws an error.
   * @throws Exception if operation fails
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testEditEvent_wrongStartEnd_shouldThrow() throws Exception {
    Date s = model.parseDateLocal("2025-04-11T10:00");
    Date e = model.parseDateLocal("2025-04-11T11:00");
    IEventModel ev = EventModelImpl.getBuilder("Mismatch",
                    "2025-04-11T10:00",
                    model.getZoneId())
            .endDateString("2025-04-11T11:00").build();
    model.addSingleEvent(ev, false);
    Date wrongEnd = model.parseDateLocal("2025-04-11T12:00");
    model.editEvent("description", "Mismatch", s,
            wrongEnd, "This won't work");
  }

  /**
   * Test that editing an event triggers reindex on name or start change.
   * @throws Exception if operation fails
   */
  @Test
  public void testReindexIfNeededTriggersOnNameOrStartChange()
          throws Exception {
    IEventModel event = EventModelImpl.getBuilder("OldName",
                    "2025-05-03T09:00",
                    model.getZoneId())
            .endDateString("2025-05-03T10:00")
            .build();
    model.addSingleEvent(event, false);
    model.editEvent("name", "OldName",
            model.parseDateLocal("2025-05-03T09:00"),
            "NewName");
    List<IEventModel> result = model.searchEvents("2025-05-03T00:00",
            "2025-05-03T23:59");
    assertEquals("NewName", result.get(0).getEventName());
  }

  /**
   * Test that setEventDates assigns start and end dates correctly.
   * @throws Exception if reflection fails
   */
  @Test
  public void testSetEventDatesAssignsStartAndEndCorrectly() throws Exception {
    IEventModel event = EventModelImpl.getBuilder("SetterTest",
                    "2025-05-04T10:00",
                    model.getZoneId())
            .build();
    Date newStart = model.parseDateLocal("2025-05-04T10:00");
    Date newEnd = model.parseDateLocal("2025-05-04T12:00");
    java.lang.reflect.Method method = CalenderModelImpl.class.getDeclaredMethod(
            "setEventDates", IEventModel.class, Date.class, Date.class);
    method.setAccessible(true);
    method.invoke(model, event, newStart, newEnd);
    assertEquals(newStart, event.getStartDateTime());
    assertEquals(newEnd, event.getEndDateTime());
  }

  /**
   * Test that setting time zone to null throws IllegalArgumentException.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testSetTimeZoneToNullThrows() {
    model.setTimeZone(null);
  }

  /**
   * Test that parsing an invalid date string throws an exception.
   * @throws Exception if operation fails
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseDateLocal_invalidFormat_throws() throws Exception {
    model.parseDateLocal("bad-input");
  }

  /**
   * Test that setEventDates calls setStartDate and setEndDate correctly.
   * @throws Exception if reflection fails
   */
  @Test
  public void testSetEventDates_callsSetStartAndEnd() throws Exception {
    IEventModel event = EventModelImpl.getBuilder("SetterTest",
                    "2025-05-04T10:00",
                    model.getZoneId())
            .build();
    Date newStart = model.parseDateLocal("2025-05-04T10:00");
    Date newEnd = model.parseDateLocal("2025-05-04T11:00");
    java.lang.reflect.Method method = CalenderModelImpl.class.getDeclaredMethod(
            "setEventDates", IEventModel.class, Date.class, Date.class);
    method.setAccessible(true);
    method.invoke(model, event, newStart, newEnd);
    assertEquals(newStart, event.getStartDateTime());
    assertEquals(newEnd, event.getEndDateTime());
  }

}
