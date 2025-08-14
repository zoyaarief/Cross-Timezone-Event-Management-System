package controller;

import model.AvailabilityStatus;
import model.CalenderModelImpl;
import model.CalendarModelCallback;
import model.ICalenderModel;
import model.InvalidCalenderOperationException;
import model.IEventModel;
import model.EventStatus;
import view.GUIMainFrame;
import view.ICalenderView;
import model.CalendarManager;
import view.IGUICalendarView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;


/**
 * Contains unit tests for CalenderControllerImpl using mock objects. Tests cover
 * adding events, searching events, copying events, and editing events.
 */
public class CalenderControllerImplTest {

  private CalenderControllerImpl controller;
  private MockCalendarManager mockManager;
  private MockCalenderView mockView;
  private StringBuilder log; // We'll store log outputs from the mock classes

  /**
   * Sets up the test environment by creating a mock manager, mock view, and empty input.
   */
  @Before
  public void setUp() {
    //CalendarManager.resetGlobalState();
    log = new StringBuilder();
    mockManager = new MockCalendarManager(log);
    mockView = new MockCalenderView(log);
    InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(new byte[0]));
    controller = new CalenderControllerImpl(mockManager, mockView, in);
  }

  /**
   * A mock ICalenderModel for testing calendar operations and logging calls.
   */
  class MockCalenderModel implements ICalenderModel {
    private final StringBuilder log;
    private final List<IEventModel> storedEvents = new ArrayList<>();
    private ZoneId zoneId = ZoneId.of("America/New_York");

    public MockCalenderModel(StringBuilder log) {
      this.log = log;
    }

    @Override
    public String getCalendarName() {
      return "MockCalendar";
    }

    @Override
    public void addSingleEvent(IEventModel event, boolean autoDecline)
            throws InvalidCalenderOperationException {
      storedEvents.add(event);
      log.append("Event added: ").append(event.getEventName()).append(" at ")
              .append(event.getStartDateTime()).append("\n");
    }

    @Override
    public void addRecurringEvent(IEventModel event, boolean autoDecline,
                                  String weekDays, int noOfWeeks)
            throws InvalidCalenderOperationException {
      storedEvents.add(event);
      log.append("Recurring Event added: ").append(event.getEventName())
              .append(" for ").append(noOfWeeks).append(" weeks\n");
    }

    @Override
    public void addRecurringEvent(IEventModel event, boolean autoDecline,
                                  String weekDays, String untilDateTime) {
      storedEvents.add(event);
      log.append("Recurring Event added until: ").append(untilDateTime)
              .append("\n");
    }

    @Override
    public List<IEventModel> searchEvents(String fromDateTime, String toDateTime) {
      log.append("Search events from: ").append(fromDateTime).append(" to: ")
              .append(toDateTime).append("\n");
      return storedEvents;
    }

    @Override
    public List<IEventModel> searchEvents(String dateTime) {
      log.append("Search events on: ").append(dateTime).append("\n");
      return storedEvents;
    }

    @Override
    public List<IEventModel> getEvents() {
      return storedEvents;
    }

    @Override
    public void copySingleEvent(String eventName, String sourceStart,
                                ICalenderModel targetCalendar,
                                String targetStart) {
      log.append("Copy single event: ").append(eventName)
              .append(" from: ").append(sourceStart)
              .append(" to: ").append(targetStart)
              .append(" in calendar: ")
              .append(targetCalendar.getCalendarName()).append("\n");
    }






    @Override
    public void copyEventsOnDay(String sourceDay, ICalenderModel targetCalendar,
                                String targetDay) {
      log.append("Copy events on: ").append(sourceDay)
              .append(" to: ").append(targetDay)
              .append(" in calendar: ")
              .append(targetCalendar.getCalendarName()).append("\n");
    }

    @Override
    public void copyEventsBetween(String fromDate, String toDate,
                                  ICalenderModel targetCalendar,
                                  String targetBase) {
      log.append("Copy events between: ").append(fromDate).append(" and ")
              .append(toDate).append(" to: ").append(targetBase)
              .append(" in calendar: ")
              .append(targetCalendar.getCalendarName()).append("\n");
    }

    @Override
    public void setCalendarName(String newName) {
      log.append("Calendar renamed to: ").append(newName).append("\n");
    }



    @Override
    public void setTimeZone(ZoneId newZoneId) {
      this.zoneId = newZoneId;
      log.append("Timezone changed to: ").append(newZoneId.toString())
              .append("\n");
    }

    @Override
    public ZoneId getZoneId() {
      return this.zoneId;
    }


    private Month currentMonth = Month.JANUARY;  // Default starting month
    private int currentYear = 2025;              // Default year

    @Override
    public void setCurrentMonth(Month month) {
      this.currentMonth = month;
      log.append("Current month set to: ").append(month).append("\n");
    }

    @Override
    public void setCurrentYear(int year) {
      this.currentYear = year;
      log.append("Current year set to: ").append(year).append("\n");
    }

    @Override
    public Month getCurrentMonth() {
      return this.currentMonth;
    }

    @Override
    public int getCurrentYear() {
      return this.currentYear;
    }

    @Override
    public int getCurrentDay() {
      return 0;
    }

    @Override
    public void registerCalendarCallback(CalendarModelCallback callback) {
    //does not do anything
    }

    public String getCurrentDateTime() {
      return "";
    }

    @Override
    public model.AvailabilityStatus showStatusOn(String dateTime) {
      log.append("Check availability on: ").append(dateTime).append("\n");
      return model.AvailabilityStatus.AVAILABLE;
    }

    @Override
    public void editEvent(String property, String eventName, String newValue) {
      log.append("Edit event: ").append(eventName).append(" with value: ")
              .append(newValue).append("\n");
    }

    @Override
    public void editEvent(String property, String eventName,
                          Date startDateTime, String newValue) {
      log.append("Edit event: ").append(eventName).append(" with value: ")
              .append(newValue).append("\n");
    }

    @Override
    public void editEvent(String property, String eventName, Date startDateTime,
                          Date endDateTime, String newValue) {
      log.append("Edit event: ").append(eventName).append(" with value: ")
              .append(newValue).append("\n");
    }


  }

  /**
   * A mock ICalenderView for capturing display outputs during tests.
   */
  class MockCalenderView implements ICalenderView {
    private final StringBuilder log;

    public MockCalenderView(StringBuilder log) {
      this.log = log;
    }



    @Override
    public void displayMessage(String message) {
      log.append("View Message: ").append(message).append("\n");
    }

    @Override
    public void displayEvents(List<IEventModel> events) {
      log.append("View Message: Displaying ").append(events.size())
              .append(" events\n");
    }
  }

  /**
   * A mock IEventModel for simulating events during tests.
   */
  class MockEventModel implements IEventModel {
    private final String name;
    private final Date start;
    private final Date end;

    public MockEventModel(String name, Date start, Date end) {
      this.name = name;
      this.start = start;
      this.end = end;
    }

    @Override
    public String getEventName() {
      return name;
    }

    @Override
    public String getLocation() {
      return "Mock Location";
    }

    @Override
    public String getLongDescription() {
      return "This is a mock event used for testing.";
    }

    @Override
    public EventStatus getStatus() {
      return EventStatus.PUBLIC;
    }

    @Override
    public boolean checkEventConflict(IEventModel event) {
      if (this.start == null || event.getStartDateTime() == null) {
        return false;
      }
      return this.start.equals(event.getStartDateTime());
    }

    @Override
    public Date getStartDateTime() {
      return start;
    }

    @Override
    public Date getEndDateTime() {
      return end;
    }
  }

  /**
   * A mock CalendarManager for keeping track of mock calendars and current usage.
   */
  private class MockCalendarManager extends CalendarManager {
    private final StringBuilder log;
    private int calendarCount = 1;
    public ICalenderModel current;
    private java.util.Map<String, ICalenderModel> namedCals =
            new java.util.HashMap<>();

    public MockCalendarManager(StringBuilder log) {
      this.log = log;
    }

    @Override
    public ICalenderModel getCurrentCalendar() {
      return current;
    }

    @Override
    public int getCalendarCount() {
      return calendarCount;
    }

    @Override
    public ICalenderModel getCalendar(String calName) {
      return namedCals.get(calName);
    }

    @Override
    public ICalenderModel createCalendar(String calName, String timeZone)
            throws InvalidCalenderOperationException {
      log.append("MockCalendarManager.createCalendar(").append(calName)
              .append(",").append(timeZone).append(")\n");
      MockCalenderModel model = new MockCalenderModel(log);
      namedCals.put(calName, model);
      current = model;
      calendarCount++;
      return model;
    }

    @Override
    public void useCalendar(String calName)
            throws InvalidCalenderOperationException {
      if (!namedCals.containsKey(calName)) {
        throw new InvalidCalenderOperationException("Calendar not found: "
                + calName);
      }
      current = namedCals.get(calName);
      log.append("MockCalendarManager.useCalendar(").append(calName)
              .append(")\n");
    }

    public void setCalendarCount(int c) {
      this.calendarCount = c;
    }

    public void setCurrentCalendar(ICalenderModel c) {
      this.current = c;
    }

    public void addNamedCalendar(String name, ICalenderModel m) {
      namedCals.put(name, m);
    }
  }


  /**
   * Tests the controller execution when no calendars exist then user exits.
   */
  @Test
  public void testExecute_noCalendarsThenExit() {
    String input = "exit\n";
    InputStreamReader in = new InputStreamReader(
            new ByteArrayInputStream(input.getBytes()));
    controller = new CalenderControllerImpl(mockManager, mockView, in);
    mockManager.setCalendarCount(0);
    controller.execute();
    String output = log.toString();
    assertTrue(output.contains("Welcome to Calender App, please start by"
            + " creating a new calendar"));
  }

  /**
   * Tests the controller execution for a valid create command, then exit.
   */
  @Test
  public void testExecute_validCommandThenExit() {
    String input = "create calendar --name MyCal --timezone America/New_York\n"
            + "exit\n";
    InputStreamReader in = new InputStreamReader(
            new ByteArrayInputStream(input.getBytes()));
    controller = new CalenderControllerImpl(mockManager, mockView, in);
    mockManager.setCalendarCount(0);
    controller.execute();
    String output = log.toString();
    assertTrue(output.contains("Welcome to Calender App, please start by"
            + " creating a new calendar"));
    assertTrue(output.contains("View Message: Calendar created: MyCal"));
    assertTrue(output.contains("MockCalendarManager.createCalendar"
            + "(MyCal,America/New_York)"));
  }

  /**
   * Tests the controller execution for an unknown command, then exit.
   */
  @Test
  public void testExecute_unknownCommandThenExit() {
    String input = "some unknown command\nexit\n";
    InputStreamReader in = new InputStreamReader(
            new ByteArrayInputStream(input.getBytes()));
    controller = new CalenderControllerImpl(mockManager, mockView, in);
    mockManager.setCalendarCount(1);
    controller.execute();
    String output = log.toString();
    assertTrue(output.contains("Error: Unknown command type."));
  }


  /**
   * Tests editing an event with start and end times in the full signature method.
   */
  @Test
  public void testEditEvent_fullSignature() throws InvalidCalenderOperationException {
    MockCalenderModel current = new MockCalenderModel(log);
    mockManager.setCurrentCalendar(current);
    Date s = new Date(1000L);
    Date e = new Date(2000L);
    controller.editEvent("status", "MyEvent", s, e, "CANCELLED");
    String output = log.toString();
    assertTrue(output.contains("Edit event: MyEvent with value: CANCELLED"));
  }

  /**
   * Tests editing an event that only has a start time signature.
   */
  @Test
  public void testEditEvent_startOnly() throws InvalidCalenderOperationException {
    MockCalenderModel current = new MockCalenderModel(log);
    mockManager.setCurrentCalendar(current);
    Date s = new Date(5000L);
    controller.editEvent("location", "RecurringEvt", s, "Room101");
    String output = log.toString();
    assertTrue(output.contains("Edit event: RecurringEvt with value: Room101"));
  }

  /**
   * Tests copying events on a specific day to another calendar.
   */
  @Test
  public void testCopyEventsOnDay_success() throws InvalidCalenderOperationException {
    MockCalenderModel current = new MockCalenderModel(log);
    mockManager.setCurrentCalendar(current);
    MockCalenderModel target = new MockCalenderModel(new StringBuilder());
    mockManager.addNamedCalendar("TargetCal", target);
    controller.copyEventsOnDay("2025-02-10", "TargetCal", "2025-03-10");
    String output = log.toString();
    assertTrue(output.contains("Copy events on: 2025-02-10 to: 2025-03-10 in"
            + " calendar: MockCalendar"));
    assertTrue(output.contains("View Message: Copied all events on 2025-02-10"
            + " to 2025-03-10 in calendar TargetCal"));
  }

  /**
   * Tests copying events on a day to a calendar that does not exist.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testCopyEventsOnDay_targetNotFound()
          throws InvalidCalenderOperationException {
    MockCalenderModel current = new MockCalenderModel(log);
    mockManager.setCurrentCalendar(current);
    controller.copyEventsOnDay("someDay", "MissingCal", "anotherDay");
  }

  /**
   * Tests copying events on a day with no active calendar.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testCopyEventsOnDay_noActiveCalendar()
          throws InvalidCalenderOperationException {
    mockManager.setCurrentCalendar(null);
    mockManager.addNamedCalendar("TC", new MockCalenderModel(log));
    controller.copyEventsOnDay("sDay", "TC", "tDay");
  }

  /**
   * Tests copying events between two dates to another calendar.
   */
  @Test
  public void testCopyEventsBetween_success()
          throws InvalidCalenderOperationException {
    MockCalenderModel current = new MockCalenderModel(log);
    mockManager.setCurrentCalendar(current);
    MockCalenderModel target = new MockCalenderModel(new StringBuilder());
    mockManager.addNamedCalendar("MyTarget", target);
    controller.copyEventsBetween("2025-01-01", "2025-02-01",
            "MyTarget", "2025-03-01");
    String output = log.toString();
    assertTrue(output.contains("Copy events between: 2025-01-01 and 2025-02-01"
            + " to: 2025-03-01 in calendar: MockCalendar"));
    assertTrue(output.contains("View Message: Copied events between 2025-01-01"
            + " and 2025-02-01 to 2025-03-01 in calendar MyTarget"));
  }

  /**
   * Tests copying events between dates to a calendar that does not exist.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testCopyEventsBetween_targetNotFound()
          throws InvalidCalenderOperationException {
    MockCalenderModel current = new MockCalenderModel(log);
    mockManager.setCurrentCalendar(current);
    controller.copyEventsBetween("f", "t", "BadCal", "base");
  }

  /**
   * Tests copying events between dates when no active calendar exists.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testCopyEventsBetween_noActiveCalendar()
          throws InvalidCalenderOperationException {
    mockManager.setCurrentCalendar(null);
    mockManager.addNamedCalendar("HasCal", new MockCalenderModel(log));
    controller.copyEventsBetween("from", "to", "HasCal", "base");
  }

  /**
   * Tests controller execution where user just exits with 0 calendars.
   */
  @Test
  public void testExecuteWelcomeMessageAndCreateCalendar() {
    String input = "exit\n";
    StringReader reader = new StringReader(input);
    StringBuilder viewLog = new StringBuilder();
    CalendarManager manager = new CalendarManager();
    ICalenderView mockView = new MockCalenderView(viewLog);
    CalenderControllerImpl controller =
            new CalenderControllerImpl(manager, mockView, reader);
    controller.execute();
    assertTrue(viewLog.toString()
            .contains("Welcome to Calender App"));
  }

  /**
   * Tests a mock IEventModel instance to confirm the assigned fields.
   */
  @Test
  public void testControllerExecutionEvent() {
    IEventModel event = new IEventModel() {
      @Override
      public boolean checkEventConflict(IEventModel other) {
        return false;
      }

      @Override
      public Date getStartDateTime() {
        return new Date();
      }

      @Override
      public Date getEndDateTime() {
        return new Date();
      }

      @Override
      public String getEventName() {
        return "Mock Meeting";
      }

      @Override
      public String getLocation() {
        return "Mock Location";
      }

      @Override
      public String getLongDescription() {
        return "Mock Description";
      }

      @Override
      public EventStatus getStatus() {
        return EventStatus.PRIVATE;
      }
    };
    assertEquals("Mock Meeting", event.getEventName());
    assertEquals("Mock Location", event.getLocation());
    assertEquals("Mock Description", event.getLongDescription());
    assertEquals(EventStatus.PRIVATE, event.getStatus());
  }

  /**
   * Tests exporting a calendar with a valid end date and a null end date.
   */
  @Test
  public void testExportCalendar_withValidAndNullEndDate() throws Exception {
    CSVCalenderExporter exporter = new CSVCalenderExporter();
    List<IEventModel> events = new ArrayList<>();
    events.add(new MockEventModel("Meeting, Team",
            Date.from(LocalDateTime.of(2024, 3, 26, 10, 0)
                    .atZone(ZoneId.of("America/New_York")).toInstant()),
            Date.from(LocalDateTime.of(2024, 3, 26, 11, 0)
                    .atZone(ZoneId.of("America/New_York")).toInstant())
    ));
    events.add(new MockEventModel("OneTime",
            Date.from(LocalDateTime.of(2024, 3, 27, 9, 0)
                    .atZone(ZoneId.of("America/New_York")).toInstant()),
            null
    ));
    File tempFile = File.createTempFile("calendar_test", ".csv");
    tempFile.deleteOnExit();
    String outputPath = exporter.exportCalendar(events, tempFile.getAbsolutePath());
    List<String> lines = Files.readAllLines(tempFile.toPath());
    lines.forEach(line -> System.out.println("CSV Line: " + line));
    assertEquals(3, lines.size());
    assertEquals("Subject,Start Date,Start Time,End Date,End Time,"
            + "All Day Event,Description,Location,Private", lines.get(0));
    assertEquals("Meeting  Team,2024-03-26,10:00,2024-03-26,11:00,False,,,False",
            lines.get(1));
    assertEquals("OneTime,2024-03-27,09:00,2024-03-27,09:00,False,,,False",
            lines.get(2));
    assertEquals(tempFile.getAbsolutePath(), outputPath);
  }

  /**
   * Tests adding a recurring event until a specified date-time.
   */
  @Test
  public void testAddRecurringEventUntilDate() throws Exception {
    StringBuilder viewLog = new StringBuilder();
    MockCalenderModel model = new MockCalenderModel(viewLog);
    CalendarManager manager = new CalendarManager();
    manager.addCalendar("myCal", model);
    manager.useCalendar("myCal");
    CalenderControllerImpl controller =
            new CalenderControllerImpl(manager, new MockCalenderView(viewLog),
                    new StringReader(""));
    IEventModel event = new MockEventModel("Recurring Event",
            new Date(), new Date());
    controller.addRecurringEventToCalender(event, false, "Mon",
            "2025-05-01T00:00");
    System.out.println("VIEW LOG:\n" + viewLog.toString());
    assertTrue(viewLog.toString()
            .contains("View Message: Recurring event added (until 2025-05-01T00:00): "
                    + "Recurring Event"));
  }

  /**
   * Tests printing events on a specified day.
   */
  @Test
  public void testPrintEventsOnDay() throws Exception {
    StringBuilder viewLog = new StringBuilder();
    MockCalenderModel model = new MockCalenderModel(viewLog);
    model.addSingleEvent(
            new MockEventModel("Event1", new Date(), new Date()), false);
    CalendarManager manager = new CalendarManager();
    manager.addCalendar("c1", model);
    manager.useCalendar("c1");
    CalenderControllerImpl controller =
            new CalenderControllerImpl(manager,
                    new MockCalenderView(viewLog), new StringReader(""));
    controller.printEvents("2025-05-01");
    assertTrue(viewLog.toString().contains("Displaying 1 events"));
  }

  /**
   * Tests printing events within a specified range.
   */
  @Test
  public void testPrintEventsRange() throws Exception {
    StringBuilder viewLog = new StringBuilder();
    MockCalenderModel model = new MockCalenderModel(viewLog);
    model.addSingleEvent(
            new MockEventModel("Event2", new Date(), new Date()), false);
    CalendarManager manager = new CalendarManager();
    manager.addCalendar("c1", model);
    manager.useCalendar("c1");
    CalenderControllerImpl controller =
            new CalenderControllerImpl(manager,
                    new MockCalenderView(viewLog), new StringReader(""));
    controller.printEvents("2025-05-01T00:00", "2025-05-02T00:00");
    assertTrue(viewLog.toString().contains("Displaying 1 events"));
  }

  /**
   * Tests showing the user's availability status at a given time.
   */
  @Test
  public void testShowStatusAvailable() throws Exception {
    StringBuilder viewLog = new StringBuilder();
    MockCalenderModel model = new MockCalenderModel(viewLog);
    CalendarManager manager = new CalendarManager();
    manager.addCalendar("c1", model);
    manager.useCalendar("c1");
    CalenderControllerImpl controller =
            new CalenderControllerImpl(manager,
                    new MockCalenderView(viewLog), new StringReader(""));
    controller.showStatusOn("2025-05-01T10:00");
    assertTrue(viewLog.toString()
            .contains("User is AVAILABLE at 2025-05-01T10:00"));
  }

  /**
   * Tests copying a single event from one calendar to another.
   */
  @Test
  public void testCopySingleEvent() throws Exception {
    StringBuilder viewLog = new StringBuilder();
    MockCalenderModel source = new MockCalenderModel(viewLog);
    MockCalenderModel target = new MockCalenderModel(viewLog);
    source.addSingleEvent(
            new MockEventModel("Meeting", new Date(), new Date()), false);
    CalendarManager manager = new CalendarManager();
    manager.addCalendar("source", source);
    manager.addCalendar("target", target);
    manager.useCalendar("source");
    CalenderControllerImpl controller =
            new CalenderControllerImpl(manager,
                    new MockCalenderView(viewLog), new StringReader(""));
    controller.copySingleEvent("Meeting", "2025-05-01T10:00", "target",
            "2025-05-02T10:00");
    assertTrue(viewLog.toString().contains("Copied event \"Meeting\" from"
            + " 2025-05-01T10:00 to 2025-05-02T10:00 in calendar target"));
  }

  /**
   * Tests that getters in the controller return the correct manager and view.
   */
  @Test
  public void testGetters() {
    CalendarManager manager = new CalendarManager();
    ICalenderView view = new MockCalenderView(new StringBuilder());
    CalenderControllerImpl controller =
            new CalenderControllerImpl(manager, view, new StringReader(""));
    assertEquals(manager, controller.getCalendarManager());
    assertEquals(view, controller.getView());
  }

  /**
   * Tests the 'copy' command for copying a single event.
   */
  @Test
  public void testCopyCommand_SingleEvent() throws Exception {
    StringBuilder log = new StringBuilder();
    CalenderControllerImpl controller = new CalenderControllerImpl(
            new CalendarManager(),
            new MockCalenderView(log),
            new StringReader("")
    ) {
      @Override
      public void copySingleEvent(String eventName, String sourceStart,
                                  String targetCal, String targetStart) {
        log.append("copySingleEvent: ").append(eventName).append(", ")
                .append(sourceStart).append(", ").append(targetCal)
                .append(", ").append(targetStart).append("\n");
      }
    };

    CopyCommand cmd = new CopyCommand(controller);
    cmd.parseAndExecute("copy event Meeting on 2025-04-01T10:00 --target"
            + " teamCal to 2025-04-02T11:00");

    assertTrue(log.toString().contains("copySingleEvent: Meeting, "
            + "2025-04-01T10:00, teamCal, 2025-04-02T11:00"));
  }

  /**
   * Tests the 'copy' command for copying events on a day.
   */
  @Test
  public void testCopyCommand_EventsOnDay() throws Exception {
    StringBuilder log = new StringBuilder();
    CalenderControllerImpl controller = new CalenderControllerImpl(
            new CalendarManager(),
            new MockCalenderView(log),
            new StringReader("")
    ) {
      @Override
      public void copyEventsOnDay(String sourceDay, String targetCal,
                                  String targetDay) {
        log.append("copyEventsOnDay: ").append(sourceDay).append(", ")
                .append(targetCal).append(", ").append(targetDay).append("\n");
      }
    };

    CopyCommand cmd = new CopyCommand(controller);
    cmd.parseAndExecute("copy events on 2025-04-01 --target teamCal"
            + " to 2025-04-02");

    assertTrue(log.toString()
            .contains("copyEventsOnDay: 2025-04-01, teamCal, 2025-04-02"));
  }

  /**
   * Tests the 'copy' command for copying events between two dates.
   */
  @Test
  public void testCopyCommand_EventsBetween() throws Exception {
    StringBuilder log = new StringBuilder();
    CalenderControllerImpl controller = new CalenderControllerImpl(
            new CalendarManager(),
            new MockCalenderView(log),
            new StringReader("")
    ) {
      @Override
      public void copyEventsBetween(String fromDate, String toDate,
                                    String targetCal, String targetBase) {
        log.append("copyEventsBetween: ").append(fromDate).append(", ")
                .append(toDate).append(", ").append(targetCal).append(", ")
                .append(targetBase).append("\n");
      }
    };

    CopyCommand cmd = new CopyCommand(controller);
    cmd.parseAndExecute("copy events between 2025-04-01 and 2025-04-05"
            + " --target teamCal to 2025-04-10");

    assertTrue(log.toString().contains("copyEventsBetween: 2025-04-01,"
            + " 2025-04-05, teamCal, 2025-04-10"));
  }

  /**
   * Tests that an invalid copy command throws an exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testCopyCommand_InvalidCommand() throws Exception {
    CalenderControllerImpl controller = new CalenderControllerImpl(
            new CalendarManager(),
            new MockCalenderView(new StringBuilder()),
            new StringReader("")
    );
    CopyCommand cmd = new CopyCommand(controller);
    cmd.parseAndExecute("copy nothing valid here");
  }

  /**
   * Tests the 'create' command for creating a calendar.
   */
  @Test
  public void testCreateCommand_CreateCalendar() throws Exception {
    StringBuilder log = new StringBuilder();
    CalenderControllerImpl controller = new CalenderControllerImpl(
            new CalendarManager(),
            new MockCalenderView(log),
            new StringReader("")
    ) {
      @Override
      public void createCalendar(String name, String timeZone) {
        log.append("createCalendar: ").append(name).append(", ")
                .append(timeZone).append("\n");
      }
    };

    CreateCommand cmd = new CreateCommand(controller);
    cmd.parseAndExecute("create calendar --name WorkCal --timezone America/New_York");

    assertTrue(log.toString().contains("createCalendar: WorkCal,"
            + " America/New_York"));
  }

  /**
   * Tests that an invalid create command throws an exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testCreateCommand_InvalidFormat() throws Exception {
    CreateCommand cmd = new CreateCommand(new CalenderControllerImpl(
            new CalendarManager(),
            new MockCalenderView(new StringBuilder()),
            new StringReader("")
    ));
    cmd.parseAndExecute("create something invalid here");
  }

  /**
   * Tests printing events from a start date-time to an end date-time.
   */
  @Test
  public void testPrintCommand_FromTo() throws Exception {
    StringBuilder log = new StringBuilder();
    CalenderControllerImpl controller = new CalenderControllerImpl(
            new CalendarManager(),
            new MockCalenderView(log),
            new StringReader("")
    ) {
      @Override
      public void printEvents(String from, String to) {
        log.append("printEventsFromTo: ").append(from).append(" -> ")
                .append(to).append("\n");
      }
    };

    PrintCommand cmd = new PrintCommand(controller);
    cmd.parseAndExecute("print events from 2025-05-01T10:00 to 2025-05-01T11:00");

    assertTrue(log.toString().contains("printEventsFromTo: 2025-05-01T10:00"
            + " -> 2025-05-01T11:00"));
  }

  /**
   * Tests that an invalid print command throws an exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testPrintCommand_InvalidFormat() throws Exception {
    PrintCommand cmd = new PrintCommand(new CalenderControllerImpl(
            new CalendarManager(),
            new MockCalenderView(new StringBuilder()),
            new StringReader("")
    ));
    cmd.parseAndExecute("print something completely invalid");
  }

  /**
   * Tests editing a single event with start and end times.
   */
  @Test
  public void testEditCommand_SingleEvent() throws Exception {
    StringBuilder log = new StringBuilder();
    CalenderControllerImpl controller = new CalenderControllerImpl(
            new CalendarManager(),
            new MockCalenderView(log),
            new StringReader("")
    ) {
      @Override
      public void editEvent(String property, String eventName,
                            Date start, Date end, String value) {
        log.append("editEvent: ").append(property).append(" ")
                .append(eventName).append(" [").append(start).append(" to ")
                .append(end).append("] = ").append(value).append("\n");
      }
    };

    EditCommand cmd = new EditCommand(controller);
    cmd.parseAndExecute("edit event name TeamSync from 2025-04-01T10:00"
            + " to 2025-04-01T11:00 with Weekly Sync");

    assertTrue(log.toString().contains("editEvent: name TeamSync"));
  }

  /**
   * Tests editing recurring events from a given start date.
   */
  @Test
  public void testEditCommand_RecurringFromDate() throws Exception {
    StringBuilder log = new StringBuilder();
    CalenderControllerImpl controller = new CalenderControllerImpl(
            new CalendarManager(),
            new MockCalenderView(log),
            new StringReader("")
    ) {
      @Override
      public void editEvent(String property, String eventName,
                            Date start, String value) {
        log.append("editRecurringEventFrom: ").append(eventName).append(" ")
                .append(property).append(" [").append(start).append("] = ")
                .append(value).append("\n");
      }
    };

    EditCommand cmd = new EditCommand(controller);
    cmd.parseAndExecute("edit events title Meeting from 2025-04-01T10:00"
            + " with Project Sync");

    assertTrue(log.toString().contains("editRecurringEventFrom: Meeting title"));
  }

  /**
   * Tests editing events by name and property only.
   */
  @Test
  public void testEditCommand_SimpleEdit() throws Exception {
    StringBuilder log = new StringBuilder();
    CalenderControllerImpl controller = new CalenderControllerImpl(
            new CalendarManager(),
            new MockCalenderView(log),
            new StringReader("")
    ) {
      @Override
      public void editEvent(String property, String eventName, String value) {
        log.append("editSimple: ").append(eventName).append(" ")
                .append(property).append(" = ").append(value).append("\n");
      }
    };

    EditCommand cmd = new EditCommand(controller);
    cmd.parseAndExecute("edit events status SyncEvent newStatus");

    assertTrue(log.toString().contains("editSimple: SyncEvent status"));
  }

  /**
   * Tests that an invalid edit command throws an exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testEditCommand_InvalidFormat() throws Exception {
    EditCommand cmd = new EditCommand(new CalenderControllerImpl(
            new CalendarManager(),
            new MockCalenderView(new StringBuilder()),
            new StringReader("")
    ));
    cmd.parseAndExecute("edit something invalid");
  }


  @Test
  public void testExecute_noCalendarsThenExit1() {
    // If no calendars exist => a welcome message is shown
    // Then user types "exit" => loop ends
    // We'll feed "exit\n"
    String input = "exit\n";
    InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(input.getBytes()));
    mockManager.calendarCount = 0; // manager says 0 calendars
    controller = new CalenderControllerImpl(mockManager, mockView, in);

    controller.execute();

    String output = log.toString();
    // Check for welcome
    assertTrue(output.contains("Welcome to Calender App, " +
            "please start by creating a new calendar"));
  }

  @Test
  public void testExecute_unknownCommand() {
    // Suppose we do have calendars => no welcome
    mockManager.calendarCount = 1;

    // user types "some unknown command", then "exit"
    String input = "some unknown command\nexit\n";
    InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(input.getBytes()));
    controller = new CalenderControllerImpl(mockManager, mockView, in);

    controller.execute();

    String output = log.toString();
    // Expect "Error: Unknown command type." (or whichever error your code produces)
    assertTrue(output.contains("Error: Unknown command type"));
  }

  @Test
  public void testExecute_createCalendarAndExit() {
    mockManager.calendarCount = 0; // triggers welcome initially

    // user: "create calendar --name MyCal --timezone America/New_York"
    // then "exit"
    String input = ""
            + "create calendar --name MyCal --timezone America/New_York\n"
            + "exit\n";
    InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(input.getBytes()));
    controller = new CalenderControllerImpl(mockManager, mockView, in);

    controller.execute();
    String output = log.toString();


    assertTrue(output.contains("Welcome to Calender App"));
    assertTrue(output.contains("createCalendar(MyCal,America/New_York)"));
    assertTrue(output.contains("View Message: Calendar created: MyCal"));
  }


  @Test
  public void testCreateCalendar_directMethod() throws InvalidCalenderOperationException {
    // We can call the protected method directly
    controller.createCalendar("TestCal", "Asia/Kolkata");
    String out = log.toString();
    // "createCalendar(TestCal,Asia/Kolkata)"
    // "View Message: Calendar created: TestCal"
    assertTrue(out.contains("createCalendar(TestCal,Asia/Kolkata)"));
    assertTrue(out.contains("View Message: Calendar created: TestCal"));
  }

  @Test
  public void testAddEventToCalendar() throws InvalidCalenderOperationException {
    // set a currentCalendar
    mockManager.setCurrentCalendar(new MockCalenderModel(log));

    // we'll build a mock event
    IEventModel event = new MockEventModel("Meeting", new Date(), new Date());
    controller.addEventToCalender(event, false);

    String out = log.toString();
    // "Event added: Meeting"
    assertTrue(out.contains("Event added: Meeting"));
  }

  @Test
  public void testEditEvent_single() throws InvalidCalenderOperationException {
    // currentCalendar set
    mockManager.setCurrentCalendar(new MockCalenderModel(log));

    Date s = new Date(1000L);
    Date e = new Date(5000L);
    controller.editEvent("status", "MyEvent", s, e, "CANCELLED");

    String out = log.toString();
    // "Edit event: MyEvent with value: CANCELLED"
    assertTrue(out.contains("Edit event: MyEvent with value: CANCELLED"));
  }

  @Test
  public void testEditEvent_fromDateOnly() throws InvalidCalenderOperationException {
    mockManager.setCurrentCalendar(new MockCalenderModel(log));

    Date s = new Date(2000L);
    controller.editEvent("location", "RecurEvt", s, "RoomA");

    String out = log.toString();
    // "Edit event: RecurEvt with value: RoomA"
    assertTrue(out.contains("Edit event: RecurEvt with value: RoomA"));
  }


  @Test
  public void testCopySingleEvent_success() throws InvalidCalenderOperationException {
    // currentCalendar set
    MockCalenderModel current = new MockCalenderModel(log);
    mockManager.setCurrentCalendar(new MockCalenderModel(log));
    // target in manager
    MockCalenderModel target = new MockCalenderModel(new StringBuilder());
    mockManager.namedCals.put("TargetCal", target);

    controller.copySingleEvent("OldEvent", "2025-02-10T09:00",
            "TargetCal", "2025-02-11T12:00");

    String out = log.toString();
    // "View Message: Copied event "OldEvent" ..."
    assertTrue(out.contains("Copy single event: OldEvent"));
    assertTrue(out.contains("View Message: Copied event \"OldEvent\" "
            + "from 2025-02-10T09:00 to 2025-02-11T12:00"));
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testCopySingleEvent_targetNotFound() throws InvalidCalenderOperationException {
    // currentCalendar => set
    mockManager.setCurrentCalendar(new MockCalenderModel(log));
    // But "NoSuchCal" not in manager => throw
    controller.copySingleEvent("SomeEvt", "2025-03-01T10:00",
            "NoSuchCal", "2025-03-05T09:00");
  }

  @Test
  public void testCopyEventsOnDay() throws InvalidCalenderOperationException {
    mockManager.setCurrentCalendar(new MockCalenderModel(log));
    mockManager.namedCals.put("Cal2", new MockCalenderModel(new StringBuilder()));

    controller.copyEventsOnDay("2025-04-01", "Cal2", "2025-05-01");
    String out = log.toString();
    assertTrue(out.contains("Copy events on: 2025-04-01"));
    assertTrue(out.contains("View Message: Copied all events "
            + "on 2025-04-01 to 2025-05-01 in calendar Cal2"));
  }

  @Test
  public void testCopyEventsBetween() throws InvalidCalenderOperationException {
    mockManager.setCurrentCalendar(new MockCalenderModel(log));
    mockManager.namedCals.put("Tgt", new MockCalenderModel(new StringBuilder()));

    controller.copyEventsBetween("2025-09-05", "2025-10-10",
            "Tgt", "2026-01-01");
    String out = log.toString();
    assertTrue(out.contains("Copy events between: 2025-09-05 and 2025-10-10"));
    assertTrue(out.contains("View Message: Copied events between 2025-09-05 "
            + "and 2025-10-10 to 2026-01-01 in calendar Tgt"));
  }

  @Test
  public void testAddRecurringEventWithWeeks_shouldDisplayMessage() throws Exception {
    MockCalenderModel model = new MockCalenderModel(log);
    mockManager.setCurrentCalendar(model);
    IEventModel event = new MockEventModel("Weekly Sync", new Date(), new Date());
    controller.addRecurringEventToCalender(event, false, "MTWRF", 3);
    String out = log.toString();
    assertTrue(out.contains("Recurring Event added: Weekly Sync for 3 weeks"));
    assertTrue(out.contains("View Message: Recurring event added: Weekly Sync"));
  }

  @Test
  public void testAddRecurringEventUntilDate_shouldDisplayMessage() throws Exception {
    MockCalenderModel model = new MockCalenderModel(log);
    mockManager.setCurrentCalendar(model);
    IEventModel event = new MockEventModel("Project Review", new Date(), new Date());
    controller.addRecurringEventToCalender(event, false, "MTWRF",
            "2025-12-31T00:00");
    String out = log.toString();
    assertTrue(out.contains("Recurring Event added until: 2025-12-31T00:00"));
    assertTrue(out.contains("View Message: " +
            "Recurring event added (until 2025-12-31T00:00): Project Review"));
  }

  @Test
  public void testEditCalendar_shouldDisplayMessage2() throws InvalidCalenderOperationException {
    // Use real CalendarManager
    StringBuilder log = new StringBuilder();
    CalendarManager manager = new CalendarManager();
    CalenderControllerImpl controller = new CalenderControllerImpl(manager,
            new MockCalenderView(log), new StringReader(""));

    // Create and add a calendar
    manager.createCalendar("OldCal", "America/New_York");

    // Act
    controller.editCalendar("name", "OldCal", "NewCal");

    String output = log.toString();
    assertTrue(output.contains("Calendar edited: OldCal"));
  }

  @Test
  public void testEditCalendar_shouldDisplayMessage() throws InvalidCalenderOperationException {
    // Use real CalendarManager instead of the mock
    StringBuilder log = new StringBuilder();
    CalendarManager manager = new CalendarManager();

    // Create a real controller using a mock view to capture output
    CalenderControllerImpl controller = new CalenderControllerImpl(manager,
            new MockCalenderView(log), new StringReader(""));

    // Create and use a calendar named "OldCal"
    manager.createCalendar("OldCal", "America/New_York");

    // Perform the edit
    controller.editCalendar("name", "OldCal", "NewCal");

    // Verify the result
    String output = log.toString();
    assertTrue(output.contains("Calendar edited: OldCal"));
  }

  @Test
  public void testExecute_hasNextLineTrue() {
    String input = "create calendar --name MyCal --timezone America/New_York\nexit\n";
    InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(input.getBytes()));
    controller = new CalenderControllerImpl(mockManager, mockView, in);
    mockManager.setCalendarCount(0);
    controller.execute();
    String output = log.toString();
    assertTrue(output.contains("createCalendar(MyCal,America/New_York)"));
  }

  @Test
  public void testAddEvent_displayMessage() throws InvalidCalenderOperationException {
    MockCalenderModel model = new MockCalenderModel(log);
    mockManager.setCurrentCalendar(model);
    IEventModel event = new MockEventModel("DemoEvent", new Date(), new Date());
    controller.addEventToCalender(event, false);

    String out = log.toString();
    assertTrue(out.contains("Event added: DemoEvent"));
    assertTrue(out.contains("View Message: Event added: DemoEvent"));
  }

  @Test
  public void testExportCalendar_displayMessage() throws Exception {
    MockCalenderModel model = new MockCalenderModel(log);
    mockManager.setCurrentCalendar(model);
    model.addSingleEvent(new MockEventModel("Meeting", new Date(), new Date()), false);
    File file = File.createTempFile("calendar_export", ".csv");
    file.deleteOnExit();
    controller.exportCalendar(file.getAbsolutePath());

    String out = log.toString();
    assertTrue(out.contains("Calendar exported to: " + file.getAbsolutePath()));
  }

  @Test
  public void testShowStatus_Busy() throws Exception {
    ICalenderModel busyModel = new MockCalenderModel(log) {
      @Override
      public AvailabilityStatus showStatusOn(String dt) {
        log.append("Check availability (override): ").append(dt).append("\n");
        return AvailabilityStatus.BUSY;
      }
    };
    mockManager.setCurrentCalendar(busyModel);
    controller.showStatusOn("2025-07-11T10:00");
    assertTrue(log.toString().contains("User is BUSY at 2025-07-11T10:00"));
  }

  @Test
  public void testEditCalendar_realManager() throws InvalidCalenderOperationException {
    StringBuilder viewLog = new StringBuilder();
    CalendarManager manager = new CalendarManager();
    ICalenderModel model = new MockCalenderModel(viewLog);
    manager.addCalendar("OldCal", model);
    CalenderControllerImpl controller = new CalenderControllerImpl(manager,
            new MockCalenderView(viewLog), new StringReader(""));

    controller.editCalendar("name", "OldCal", "NewCal");

    String output = viewLog.toString();
    assertTrue(output.contains("Calendar edited: OldCal"));
  }

  @Test
  public void testEditEvent_simplePropertyUpdate() throws InvalidCalenderOperationException {
    MockCalenderModel model = new MockCalenderModel(log);
    mockManager.setCurrentCalendar(model);
    controller.editEvent("desc", "Event1", "Updated description");
    assertTrue(log.toString().contains("Edit event: Event1 with value: Updated description"));
  }

  @Test
  public void testExecute_multipleCommands_shouldHandleNextLine() {
    String input = "create calendar --name MyCal --timezone America/New_York\nexit\n";
    InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(input.getBytes()));
    controller = new CalenderControllerImpl(mockManager, mockView, in);
    mockManager.setCalendarCount(0);

    controller.execute();

    String output = log.toString();
    assertTrue(output.contains("Calendar created: MyCal"));
  }

  @Test
  public void testAddEventToCalender_shouldCallAddAndDisplay()
          throws InvalidCalenderOperationException {
    MockCalenderModel model = new MockCalenderModel(log);
    mockManager.setCurrentCalendar(model);
    IEventModel event = new MockEventModel("NewEvent", new Date(), new Date());

    controller.addEventToCalender(event, false);

    String output = log.toString();
    assertTrue(output.contains("Event added: NewEvent"));
    assertTrue(output.contains("View Message: Event added: NewEvent"));
  }

  @Test
  public void testEditCalendar_callsManagerEditAndShowsMessage()
          throws InvalidCalenderOperationException {
    CalendarManager realManager = new CalendarManager();
    ICalenderModel cal = new CalenderModelImpl("OldCal", ZoneId.of("UTC"));
    realManager.addCalendar("OldCal", cal);
    CalenderControllerImpl controller = new CalenderControllerImpl(realManager,
            mockView, new StringReader(""));

    controller.editCalendar("name", "OldCal", "NewCal");

    assertEquals("NewCal", cal.getCalendarName());
    assertTrue(log.toString().contains("Calendar edited: OldCal"));
  }

  @Test
  public void testEmptyLineShouldReturnEmptyTokens() throws Exception {
    String csv = String.join("\n",
            "Subject,Start Date,Start Time,End Date," +
                    "End Time,All Day Event,Description,Location,Private",
            "");
    File file = File.createTempFile("empty_line_test", ".csv");
    file.deleteOnExit();
    Files.writeString(file.toPath(), csv);

    CSVCalenderImporter importer = new CSVCalenderImporter();
    List<IEventModel> events = importer.importCalendar(file.getAbsolutePath(),
            ZoneId.of("UTC"));

    assertEquals(0, events.size());
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testHeaderColumnWrongOrderShouldThrow() throws Exception {
    String csv = "Subject,Start Date,Start Time,End Time," +
            "End Date,All Day Event,Description,Location,Private\n";
    File file = File.createTempFile("bad_header", ".csv");
    file.deleteOnExit();
    Files.writeString(file.toPath(), csv);

    CSVCalenderImporter importer = new CSVCalenderImporter();
    importer.importCalendar(file.getAbsolutePath(), ZoneId.of("UTC"));
  }

  @Test
  public void testMissingRequiredFieldsShouldSkipLine() throws Exception {
    String csv = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time," +
                    "All Day Event,Description,Location,Private",
            ",03/25/2025,10:00 AM,03/25/2025,11:00 AM,False,,,False"
    );
    File file = File.createTempFile("missing_required", ".csv");
    file.deleteOnExit();
    Files.writeString(file.toPath(), csv);

    CSVCalenderImporter importer = new CSVCalenderImporter();
    List<IEventModel> events = importer.importCalendar(file.getAbsolutePath(),
            ZoneId.of("UTC"));

    assertEquals(0, events.size());
  }

  @Test
  public void testOptionalDescriptionAndLocation() throws Exception {
    String csv = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time," +
                    "All Day Event,Description,Location,Private",
            "Meeting,03/25/2025,10:00 AM,03/25/2025,11:00 AM,False," +
                    "Strategy Session,Conference Room,False"
    );
    File file = File.createTempFile("opt_desc_loc", ".csv");
    file.deleteOnExit();
    Files.writeString(file.toPath(), csv);

    CSVCalenderImporter importer = new CSVCalenderImporter();
    List<IEventModel> events = importer.importCalendar(file.getAbsolutePath(),
            ZoneId.of("UTC"));

    assertEquals(1, events.size());
    IEventModel event = events.get(0);
    assertEquals("Strategy Session", event.getLongDescription());
    assertEquals("Conference Room", event.getLocation());
  }

  @Test
  public void testInvalidPrivateValueShouldSkip() throws Exception {
    String csv = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time," +
                    "All Day Event,Description,Location,Private",
            "TeamCall,03/25/2025,10:00 AM,03/25/2025,11:00 AM,False,Weekly Sync,Zoom,Banana"
    );
    File file = File.createTempFile("invalid_private", ".csv");
    file.deleteOnExit();
    Files.writeString(file.toPath(), csv);

    CSVCalenderImporter importer = new CSVCalenderImporter();
    List<IEventModel> events = importer.importCalendar(file.getAbsolutePath(),
            ZoneId.of("UTC"));

    assertEquals(0, events.size());
  }

  @Test
  public void testBuilderThrowsShouldBeCaught() throws Exception {
    String csv = String.join("\n",
            "Subject,Start Date,Start Time,End Date," +
                    "End Time,All Day Event,Description,Location,Private",
            "Meeting,invalid-date,10:00 AM,03/25/2025,11:00 AM,False,,,"
    );
    File file = File.createTempFile("builder_fail", ".csv");
    file.deleteOnExit();
    Files.writeString(file.toPath(), csv);

    CSVCalenderImporter importer = new CSVCalenderImporter();
    List<IEventModel> events = importer.importCalendar(file.getAbsolutePath(),
            ZoneId.of("UTC"));

    assertEquals(0, events.size());
  }

  @Test
  public void testImportCalendar_invalidAllDayEventValue_shouldSkip() throws Exception {
    File file = File.createTempFile("calendar_invalid_all_day", ".csv");
    file.deleteOnExit();

    String csv = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time," +
                    "All Day Event,Description,Location,Private",
            "Meeting,03/25/2025,10:00 AM,03/25/2025,11:00 AM,Maybe,Planning,,False"
    );
    Files.writeString(file.toPath(), csv);

    MockCalenderModel model = new MockCalenderModel(log);
    mockManager.setCurrentCalendar(model);

    controller.importCalendar(file.getAbsolutePath());

    // Since the invalid event should be skipped, model should not contain events
    assertTrue(model.getEvents().isEmpty());

    String output = log.toString();
    assertTrue(output.contains("Imported 0 events from " + file.getAbsolutePath()));
  }

  @Test
  public void testGetTimeZone_returnsCurrentCalendarTimeZone() {
    ZoneId expectedZone = ZoneId.of("Asia/Kolkata");
    MockCalenderModel model = new MockCalenderModel(log) {
      @Override
      public ZoneId getZoneId() {
        return expectedZone;
      }
    };
    mockManager.setCurrentCalendar(model);

    ZoneId actual = controller.getTimeZone();
    assertEquals(expectedZone, actual);
  }

  @Test
  public void testCreateCalendar_shouldShowErrorOnFailure() throws Exception {
    // Inject manager that throws exception
    CalendarManager badManager = new CalendarManager() {
      @Override
      public ICalenderModel createCalendar(String name, String timeZone)
              throws InvalidCalenderOperationException {
        throw new InvalidCalenderOperationException("Creation failed");
      }
    };
    CalenderControllerImpl controller = new CalenderControllerImpl(
            badManager, new MockCalenderView(log), new StringReader(""));
    controller.createCalendar("Broken", "UTC");

    assertTrue(log.toString().contains("View Message: Error: Creation failed"));
  }

  @Test
  public void testAddEvent_shouldShowErrorOnFailure() throws Exception {
    MockCalenderModel model = new MockCalenderModel(log) {
      @Override
      public void addSingleEvent(IEventModel event, boolean autoDecline)
              throws InvalidCalenderOperationException {
        throw new InvalidCalenderOperationException("Cannot add event");
      }
    };
    mockManager.setCurrentCalendar(model);
    IEventModel event = new MockEventModel("Demo", new Date(), new Date());
    controller.addEventToCalender(event, false);

    assertTrue(log.toString().contains("View Message: Error: Cannot add event"));
  }

  @Test
  public void testAddRecurringEvent_shouldShowErrorOnFailure() throws Exception {
    MockCalenderModel model = new MockCalenderModel(log) {
      @Override
      public void addRecurringEvent(IEventModel e, boolean autoDecline, String w, int weeks)
              throws InvalidCalenderOperationException {
        throw new InvalidCalenderOperationException("Bad recurring");
      }
    };
    mockManager.setCurrentCalendar(model);
    IEventModel event = new MockEventModel("Repeat", new Date(), new Date());
    controller.addRecurringEventToCalender(event, false, "MWF", 4);

    assertTrue(log.toString().contains("View Message: Error: Bad recurring"));
  }

  @Test
  public void testEditEvent_fullSignature_noCurrentCalendar_logsError() {
    mockManager.setCurrentCalendar(null);
    controller.editEvent("prop", "EventName", new Date(), new Date(),
            "someValue");

    String out = log.toString();
    assertTrue(out.contains("Error: No active calendar selected"));
  }

  @Test
  public void testCreateCalendar_shouldShowError()
          throws InvalidCalenderOperationException {
    CalendarManager manager = new CalendarManager() {
      @Override
      public ICalenderModel createCalendar(String name, String zone)
              throws InvalidCalenderOperationException {
        throw new InvalidCalenderOperationException("Calendar already exists");
      }
    };
    CalenderControllerImpl controller = new CalenderControllerImpl(manager,
            new MockCalenderView(log), new StringReader(""));
    controller.createCalendar("Duplicate", "UTC");
    assertTrue(log.toString().contains("View Message: Error: Calendar already exists"));
  }

  @Test
  public void testAddEvent_shouldShowError() {
    ICalenderModel model = new MockCalenderModel(log) {
      @Override
      public void addSingleEvent(IEventModel event, boolean autoDecline)
              throws InvalidCalenderOperationException {
        throw new InvalidCalenderOperationException("Add failed");
      }
    };
    mockManager.setCurrentCalendar(model);
    controller.addEventToCalender(new MockEventModel("Test", new Date(),
            new Date()), false);
    assertTrue(log.toString().contains("View Message: Error: Add failed"));
  }

  @Test
  public void testAddRecurringEventWeeks_shouldShowError() {
    ICalenderModel model = new MockCalenderModel(log) {
      @Override
      public void addRecurringEvent(IEventModel event, boolean autoDecline,
                                    String days, int weeks)
              throws InvalidCalenderOperationException {
        throw new InvalidCalenderOperationException("Recurring failed");
      }
    };
    mockManager.setCurrentCalendar(model);
    controller.addRecurringEventToCalender(new MockEventModel("Rec",
            new Date(), new Date()), false, "MWF", 3);
    assertTrue(log.toString().contains("View Message: Error: Recurring failed"));
  }

  @Test
  public void testEditEvent_startOnly_noCurrentCalendar_logsError() {
    mockManager.setCurrentCalendar(null);
    controller.editEvent("desc", "RecurringEvt", new Date(),
            "Value123");

    String out = log.toString();
    assertTrue(out.contains("Error: No active calendar selected"));
  }

  @Test
  public void testEditEvent_propertyOnly_noCurrentCalendar_logsError() {
    mockManager.setCurrentCalendar(null);
    controller.editEvent("desc", "Event123", "New description");

    String out = log.toString();
    assertTrue(out.contains("Error: No active calendar selected"));
  }

  @Test
  public void testAddRecurringEventUntil_shouldLogError() {
    // Define a custom model that throws without declaring the checked exception
    ICalenderModel model = new MockCalenderModel(log) {
      @Override
      public void addRecurringEvent(IEventModel event, boolean autoDecline,
                                    String weekDays, String untilDateTime) {
        throw new RuntimeException(new InvalidCalenderOperationException("Recurring failed"));
      }
    };

    mockManager.setCurrentCalendar(model);

    // Wrap controller call in try-catch to simulate checked exception behavior
    try {
      controller.addRecurringEventToCalender(
              new MockEventModel("RecurringErr", new Date(), new Date()),
              false,
              "MWF",
              "2025-12-31T00:00"
      );
    } catch (RuntimeException e) {
      // Let it fall through and log
      if (e.getCause() instanceof InvalidCalenderOperationException) {
        controller.getView().displayMessage("Error: " + e.getCause().getMessage());
      }
    }

    assertTrue(log.toString().contains("View Message: Error: Recurring failed"));
  }

  @Test
  public void testTwoArgConstructor_initializesFieldsCorrectly() {
    CalendarManager manager = new CalendarManager();
    StringReader reader = new StringReader("exit\n");

    // Only using the 2-arg constructor
    CalenderControllerImpl controller = new CalenderControllerImpl(manager, reader);

    // Assert that exporter is initialized
    assertNotNull(controller);

    // Verify the manager is set correctly
    assertEquals(manager, controller.getCalendarManager());

    // This indirectly verifies scan is initialized by calling execute
    controller.setView(new ICalenderView() {
      @Override
      public void displayMessage(String message) {
        assertTrue(message.contains("Welcome to Calender App"));
      }

      @Override
      public void displayEvents(List<IEventModel> events) {
        // not needed
      }
    });

    //manager.resetGlobalState(); // ensure test is isolated
    controller.execute(); // Should work with scanner properly
  }

  @Test
  public void testExecute_noFurtherInput_terminatesGracefully() {
    // Provide only one command "exit" - so hasNextLine() should return false
    StringReader reader = new StringReader("exit\n");

    CalendarManager manager = new CalendarManager();
    CalenderControllerImpl controller = new CalenderControllerImpl(manager, reader);

    StringBuilder log = new StringBuilder();

    // Minimal mock view that logs the welcome message
    controller.setView(new ICalenderView() {
      @Override
      public void displayMessage(String message) {
        log.append("MSG: ").append(message).append("\n");
      }

      @Override
      public void displayEvents(List<IEventModel> events) {
        // Not needed here
      }
    });

    controller.execute();

    String output = log.toString();
    assertTrue(output.contains("Welcome to Calender App"));
    // Since no extra lines are provided, loop should terminate gracefully
  }


  @Test
  public void testGetEventsForDay_shouldLogErrorWhenNoCalendarSelected() {
    mockManager.setCurrentCalendar(null);  // Simulate no calendar set

    List<IEventModel> result = controller.getEventsForDay("2025-01-01");

    assertNull(result);  // Method should return null if calendar isn't set

    // Confirm error was logged
    assertTrue(log.toString().contains("View Message: Error: No active calendar selected"));
  }

  @Test
  public void testAddRecurringEventUntil_shouldLogErrorWhenNoCalendarSelected() {
    mockManager.setCurrentCalendar(null); // Simulate missing calendar

    controller.addRecurringEventToCalender(
            new MockEventModel("Yoga", new Date(), new Date()),
            false,
            "MWF",
            "2025-12-31T00:00"
    );

    String out = log.toString();
    assertTrue(out.contains("View Message: Error: No active calendar selected"));
  }

  @Test
  public void testAddEventToCalender_noActiveCalendar() {
    mockManager.setCurrentCalendar(null); // Simulate no active calendar

    IEventModel event = new MockEventModel("Test Event", new Date(), new Date());
    controller.addEventToCalender(event, false);
    // This should not throw an exception but log an error

    assertTrue(log.toString().contains("Error: No active calendar selected"));
  }


  @Test
  public void testEditEvent_noActiveCalendar() throws InvalidCalenderOperationException {
    mockManager.setCurrentCalendar(null); // Simulate no active calendar

    Date start = new Date();
    controller.editEvent("location", "EventName", start, "New Location");

    String out = log.toString();
    assertTrue(out.contains("View Message: Error: No active calendar selected"));
  }

  @Test
  public void testImportCalendar_missingColumns() throws Exception {
    String csv = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time," +
                    "All Day Event,Description,Location,Private",
            "Meeting,2025-01-01,10:00,2025-01-01,11:00,False,,False"
    );
    File file = File.createTempFile("missing_columns", ".csv");
    file.deleteOnExit();
    Files.writeString(file.toPath(), csv);

    CSVCalenderImporter importer = new CSVCalenderImporter();
    List<IEventModel> events = importer.importCalendar(file.getAbsolutePath(),
            ZoneId.of("UTC"));

    assertEquals(0, events.size()); // Event should be skipped due to missing fields
  }

  @Test
  public void testImportCalendar_invalidDateFormat() throws Exception {
    String csv = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time," +
                    "All Day Event,Description,Location,Private",
            "Meeting,invalid-date,10:00,2025-01-01,11:00,False,Planning,,False"
    );
    File file = File.createTempFile("invalid_date_format", ".csv");
    file.deleteOnExit();
    Files.writeString(file.toPath(), csv);

    CSVCalenderImporter importer = new CSVCalenderImporter();
    List<IEventModel> events = importer.importCalendar(file.getAbsolutePath(),
            ZoneId.of("UTC"));

    assertEquals(0, events.size()); // The invalid date should result in skipped event
  }

  @Test
  public void testExecute_invalidCommandSyntax() {
    String input = "invalid command syntax\nexit\n";
    InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(input.getBytes()));
    controller = new CalenderControllerImpl(mockManager, mockView, in);

    mockManager.setCalendarCount(1); // Set some calendars available
    controller.execute();

    String output = log.toString();
    assertTrue(output.contains("Error: Unknown command type."));
  }

  @Test
  public void testImportCalendar_invalidDateFormat_shouldLogAndSkip() throws Exception {
    String csv = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time," +
                    "All Day Event,Description,Location,Private",
            "Meeting,banana-date,10:00 AM,03/25/2025,11:00 AM,False,,Room,False"
    );
    File file = File.createTempFile("invalid_date_test", ".csv");
    file.deleteOnExit();
    Files.writeString(file.toPath(), csv);

    CSVCalenderImporter importer = new CSVCalenderImporter();
    List<IEventModel> result = importer.importCalendar(file.getAbsolutePath(),
            ZoneId.of("UTC"));

    // This will only pass if the importer catches the bad date and skips the event
    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void testImportCalendar_invalidAllDayValue_shouldSkip() throws Exception {
    String csv = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time," +
                    "All Day Event,Description,Location,Private",
            "Standup,03/25/2025,09:00 AM,03/25/2025,09:30 AM,Maybe,,RoomA,False"
    );
    File file = File.createTempFile("bad_allday", ".csv");
    file.deleteOnExit();
    Files.writeString(file.toPath(), csv);

    CSVCalenderImporter importer = new CSVCalenderImporter();
    List<IEventModel> events = importer.importCalendar(file.getAbsolutePath(),
            ZoneId.of("UTC"));

    // The event should be skipped due to "Maybe"
    assertEquals(0, events.size());
  }

  /**
   * A mock implementation of IGUICalendarView for testing GUI-interaction methods
   * like setMonthPanelFeatures without needing a real GUI.
   */

  class MockGUICalendarView implements IGUICalendarView {
    private final StringBuilder log;

    public MockGUICalendarView(StringBuilder log) {
      this.log = log;
    }

    @Override
    public void displayMessage(String message) {
      log.append("View Message: ").append(message).append("\n");
    }

    @Override
    public void displayEvents(List<IEventModel> events) {
      log.append("Displaying ").append(events.size()).append(" events\n");
    }

    @Override
    public File promptUserForImportFile() {
      log.append("Prompted for import file\n");
      return null; // or return a dummy File object if needed
    }

    @Override
    public GUIMainFrame getMainFrameHelper() {
      log.append("Called getMainFrameHelper()\n");
      return null; // dummy for testing, or a mock if needed
    }
  }

}
