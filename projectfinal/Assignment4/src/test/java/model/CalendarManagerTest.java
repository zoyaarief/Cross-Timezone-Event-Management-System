package model;


import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.time.Month;
import java.time.ZoneId;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;


/**
 * This is a test class for testing the calendar manager.
 */
public class CalendarManagerTest {
  private CalendarManager manager;

  @Before
  public void setUp() {
    //CalendarManager.resetGlobalState();
    manager = new CalendarManager();
  }

  @Test
  public void testCreateCalendar_valid() throws InvalidCalenderOperationException {
    // Initially empty
    assertEquals(0, manager.getCalendarCount());
    // Create one
    ICalenderModel cal1 = manager.createCalendar("WorkCal", "America/New_York");
    assertNotNull(cal1);
    assertEquals(1, manager.getCalendarCount());
    // The newly created calendar should also be currentCalendar
    assertSame(cal1, manager.getCurrentCalendar());
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testCreateCalendar_duplicateName() throws InvalidCalenderOperationException {
    manager.createCalendar("SameName", "America/New_York");
    // Trying again with the same name => exception
    manager.createCalendar("SameName", "America/Los_Angeles");
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testCreateCalendar_invalidTimeZone() throws InvalidCalenderOperationException {
    // This time zone doesn't exist => expect exception
    manager.createCalendar("BadZone", "Fake/InvalidZone");
  }

  @Test
  public void testGetCalendar_andGetCurrentCalendar() throws InvalidCalenderOperationException {
    ICalenderModel calNY = manager.createCalendar("NewYorkCal", "America/New_York");
    assertSame(calNY, manager.getCalendar("NewYorkCal"));
    // currentCalendar should be calNY
    assertSame(calNY, manager.getCurrentCalendar());

    ICalenderModel calLA = manager.createCalendar("LACal", "America/Los_Angeles");
    // Now LACal is the most recently created => currentCalendar
    assertSame(calLA, manager.getCurrentCalendar());
    // but we can still get the first one by name
    assertSame(calNY, manager.getCalendar("NewYorkCal"));
  }

  @Test
  public void testUseCalendar_existing() throws InvalidCalenderOperationException {
    manager.createCalendar("Cal1", "America/New_York");
    manager.createCalendar("Cal2", "America/Los_Angeles");
    assertEquals(2, manager.getCalendarCount());

    // Currently "Cal2" is the last created => currentCalendar is "Cal2"
    // Switch to "Cal1"
    manager.useCalendar("Cal1");
    assertEquals("Cal1", manager.getCurrentCalendar().getCalendarName());
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testUseCalendar_nonExistent() throws InvalidCalenderOperationException {
    manager.useCalendar("NoSuchCal");
  }

  @Test
  public void testRemoveCalendar_basic() throws InvalidCalenderOperationException {
    ICalenderModel c1 = manager.createCalendar("RemoveMe", "America/New_York");
    assertTrue(manager.removeCalendar("RemoveMe"));
    assertEquals(0, manager.getCalendarCount());
    // currentCalendar was c1 => now should be null
    assertNull(manager.getCurrentCalendar());

    // Removing again => false
    assertFalse(manager.removeCalendar("RemoveMe"));
  }

  @Test
  public void testEditCalendar_name_success() throws InvalidCalenderOperationException {
    manager.createCalendar("OldName", "America/New_York");
    assertNotNull(manager.getCalendar("OldName"));

    // Edit name => becomes "NewName"
    manager.editCalendar("name", "OldName", "NewName");
    assertNull(manager.getCalendar("OldName"));
    assertNotNull(manager.getCalendar("NewName"));
    // currentCalendar should be updated if it was "OldName"
    assertEquals("NewName", manager.getCurrentCalendar().getCalendarName());
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testEditCalendar_name_alreadyExists() throws InvalidCalenderOperationException {
    manager.createCalendar("CalA", "America/New_York");
    manager.createCalendar("CalB", "America/Los_Angeles");
    // Try to rename CalA to "CalB" => exception
    manager.editCalendar("name", "CalA", "CalB");
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testEditCalendar_name_calNotFound() throws InvalidCalenderOperationException {
    manager.editCalendar("name", "NoSuchName", "AnyName");
  }

  @Test
  public void testEditCalendar_timezone_success() throws InvalidCalenderOperationException {
    manager.createCalendar("TimeCal", "America/New_York");
    assertNotNull(manager.getCalendar("TimeCal"));

    // Edit timezone => "Europe/London"
    manager.editCalendar("timezone", "TimeCal", "Europe/London");
    ICalenderModel edited = manager.getCalendar("TimeCal");
    // Check that the underlying model has the new zone
    assertTrue(edited instanceof CalenderModelImpl);
    CalenderModelImpl impl = (CalenderModelImpl) edited;
    assertEquals(ZoneId.of("Europe/London"), impl.getZoneId());
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testEditCalendar_timezone_invalidZone() throws InvalidCalenderOperationException {
    manager.createCalendar("TZoneCal", "America/New_York");
    manager.editCalendar("timezone", "TZoneCal", "Bad/InvalidZone");
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testEditCalendar_invalidProperty() throws InvalidCalenderOperationException {
    manager.createCalendar("AnyCal", "America/New_York");
    manager.editCalendar("notAProperty", "AnyCal", "AnyValue");
  }

  /**
   * Dummy callback implementation for testing callback invocation.
   */
  private class DummyCallback implements CalendarModelCallback {
    boolean called = false;

    @Override
    public void newCalendarAdded() {
      called = true;
    }

    @Override
    public void onMonthChanged(ICalenderModel model, int newYear, Month newMonth) {
      // Dummy implementation; you can record values if needed for your tests.
    }
  }


  /**
   * Test that a registered callback is invoked when a new calendar is created.
   */
  @Test
  public void testCallbackInvokedOnCalendarCreation() throws InvalidCalenderOperationException {
    DummyCallback dummyCallback = new DummyCallback();
    manager.registerCalendarManagerCallback(dummyCallback);
    manager.createCalendar("CallbackCal", "America/New_York");
    assertTrue("Callback should have been invoked", dummyCallback.called);
  }

  /**
   * Test that editing the calendar name to an empty string throws an exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testEditCalendar_name_empty() throws InvalidCalenderOperationException {
    manager.createCalendar("TestCal", "America/New_York");
    // Passing empty (or whitespace) new name should fail.
    manager.editCalendar("name", "TestCal", "   ");
  }

  /**
   * Test that editing the calendar timezone to an empty string throws an exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testEditCalendar_timezone_empty() throws InvalidCalenderOperationException {
    manager.createCalendar("TestCalTZ", "America/New_York");
    // Passing empty (or whitespace) timezone should fail.
    manager.editCalendar("timezone", "TestCalTZ", "  ");
  }


  /**
   * Test that getCalendarNameList returns the expected list of
   * calendar names (in lowercase as stored).
   */
  @Test
  public void testGetCalendarNameList() throws InvalidCalenderOperationException {
    manager.createCalendar("CalOne", "America/New_York");
    manager.createCalendar("CalTwo", "America/Los_Angeles");
    java.util.List<String> names = manager.getCalendarNameList();
    // Calendar names are stored in lowercase.
    assertTrue(names.contains("calone"));
    assertTrue(names.contains("caltwo"));
    assertEquals(2, names.size());
  }

  /**
   * Test that createDefaultCalendar successfully creates a default calendar with the correct name.
   */
  @Test
  public void testCreateDefaultCalendar_success() {
    ICalenderModel defaultCal = manager.createDefaultCalendar();
    assertNotNull(defaultCal);
    // Check that the calendar name contains "Personal (default)" (case-insensitive).
    assertTrue(defaultCal.getCalendarName().toLowerCase().contains("personal"));
  }

  /**
   * Test the error path in createDefaultCalendar.
   * If a calendar with the default name already exists, createDefaultCalendar
   * should catch the exception and return null.
   */
  @Test
  public void testCreateDefaultCalendar_failure() throws InvalidCalenderOperationException {
    // Pre-create a calendar with the default name to force a duplicate.
    manager.createCalendar("Personal (default)", "America/New_York");
    ICalenderModel defaultCal = manager.createDefaultCalendar();
    // Expect null due to a caught exception in createDefaultCalendar.
    assertNull(defaultCal);
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testEditCalendar_name_nonExistentThrows() throws InvalidCalenderOperationException {
    // manager has no calendars yet
    manager.editCalendar("name", "DoesNotExist", "NewName");
  }

  @Test
  public void testEditCalendar_timezone_convertsEventTimes() throws Exception {
    manager.createCalendar("TZCal", "America/New_York");
    CalenderModelImpl cal = (CalenderModelImpl) manager.getCalendar("TZCal");

    IEventModel ev = EventModelImpl
            .getBuilder("Lunch", "2025-04-10T12:00", ZoneId.of("America/New_York"))
            .endDateString("2025-04-10T13:00")
            .build();
    cal.addSingleEvent(ev, false);

    Instant beforeInstant = ev.getStartDateTime().toInstant();

    manager.editCalendar("timezone", "TZCal", "UTC");

    EventModelImpl e2 = (EventModelImpl) cal.getEvents().get(0);
    Instant afterInstant = e2.getStartDateTime().toInstant();
    assertEquals("Instant must be preserved", beforeInstant, afterInstant);

    String repr = e2.toString();
    assertTrue("toString should show 16:00 for UTC", repr.contains("16:00"));
  }

  @Test
  public void testPrintlnInvokedOnCreateCalendar() throws InvalidCalenderOperationException {
    // Capture the original System.out.
    PrintStream originalOut = System.out;
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    try {
      // Create a new calendar, which should trigger the println on line 69.
      CalendarManager manager = new CalendarManager();
      manager.createCalendar("PrintTest", "America/New_York");

      // Force flush the output stream.
      System.out.flush();
      String printedOutput = outContent.toString();

      // Assert that the output contains the expected message.
      assertTrue("Expected print statement not found",
              printedOutput.contains("Calendar created: calendar manager"));
    } finally {
      // Always restore the original System.out.
      System.setOut(originalOut);
    }


  }

  @Test
  public void testEditCalendar_timezone_durationPreserved_whenUsingDefaultEndTime()
          throws Exception {
    // Create a new CalendarManager and a calendar with "America/Los_Angeles" time zone.
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("NullEndCal", "America/Los_Angeles");
    CalenderModelImpl cal = (CalenderModelImpl) manager.getCalendar("NullEndCal");

    // Build an event with a given start time.
    EventModelImpl event = (EventModelImpl) EventModelImpl
            .getBuilder("NoEndMeeting", "2025-04-10T10:00", ZoneId.of("America/Los_Angeles"))
            .build();

    cal.addSingleEvent(event, false);

    // Record the original start and end instants and compute the event duration.
    Instant originalStartInstant = event.getStartDateTime().toInstant();
    Instant originalEndInstant = event.getEndDateTime().toInstant();
    long originalDuration = originalEndInstant.toEpochMilli() - originalStartInstant.toEpochMilli();

    // Change the calendar timezone to "UTC" so that conversion takes place.
    manager.editCalendar("timezone", "NullEndCal", "UTC");

    // Retrieve the updated event from the calendar.
    EventModelImpl updatedEvent = (EventModelImpl) cal.getEvents().get(0);
    Instant newStartInstant = updatedEvent.getStartDateTime().toInstant();
    Instant newEndInstant = updatedEvent.getEndDateTime().toInstant();
    long newDuration = newEndInstant.toEpochMilli() - newStartInstant.toEpochMilli();

    // Assert that the event's duration is preserved after the timezone conversion.
    assertEquals("Event duration should be preserved after timezone conversion",
            originalDuration, newDuration);
  }





}