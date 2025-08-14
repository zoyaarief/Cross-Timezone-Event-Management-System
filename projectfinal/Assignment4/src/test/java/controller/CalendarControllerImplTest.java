package controller;

import java.awt.Color;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import model.CalendarManager;
import model.EventModelImpl;
import model.IEventModel;
import model.InvalidCalenderOperationException;
import view.ICalenderView;
import view.IGUICalendarView;
import view.GUICalendarViewImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for CalenderControllerImpl.
 * It tests non-GUI and GUI flows.
 */
public class CalendarControllerImplTest {

  /**
   * A non-GUI view that records messages for testing.
   */
  private static class RecordingNonGUIView implements ICalenderView {
    private final List<String> messages = new ArrayList<>();

    /**
     * Record the provided message.
     *
     * @param message the message to record
     */
    @Override
    public void displayMessage(String message) {
      messages.add(message);
    }

    /**
     * No-op for event display.
     *
     * @param events the list of events
     */
    @Override
    public void displayEvents(List<IEventModel> events) {
      // No-op.
    }

    /**
     * Get all recorded messages.
     *
     * @return a list of messages
     */
    public List<String> getMessages() {
      return messages;
    }

    /**
     * Get the last recorded message.
     *
     * @return the last message or null if none
     */
    public String getLastMessage() {
      return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }
  }

  /**
   * Dummy input parser helper for testing execute().
   */
  private static class DummyInputParserHelper extends InputParserHelper {
    public int callCount = 0;

    /**
     * Constructor that passes the controller.
     *
     * @param controller the CalenderControllerImpl instance
     */
    public DummyInputParserHelper(CalenderControllerImpl controller) {
      super(controller);
    }

    /**
     * Increment callCount and do nothing more.
     *
     * @param inputCommand the input command string
     * @throws InvalidCalenderOperationException never thrown here
     */
    @Override
    public void parseInputCommand(String inputCommand)
            throws InvalidCalenderOperationException {
      callCount++;
      // No further logic.
    }
  }

  /**
   * Dummy GUI calendar view to record month panel updates.
   */
  private static class DummyGUICalendarView
          extends GUICalendarViewImpl implements IGUICalendarView {
    public boolean setMonthPanelFeaturesCalled = false;
    public int capturedYear;
    public int capturedMonth;

    /**
     * Set navigation panel features (no operation).
     *
     * @param features the calendar GUI features
     */
    @Override
    public void setNavPanelFeatures(ICalendarGUIFeatures features) {
      // Can remain empty.
    }

    /**
     * Record the provided year and month values.
     *
     * @param features the calendar GUI features
     * @param year the year value
     * @param month the month value
     */
    @Override
    public void setMonthPanelFeatures(ICalendarGUIFeatures features, int year, int month) {
      setMonthPanelFeaturesCalled = true;
      capturedYear = year;
      capturedMonth = month;
    }
  }

  /**
   * Test setting view for non-GUI.
   */
  @Test
  public void testSetViewNonGUIOnly() {
    CalendarManager manager = new CalendarManager();
    RecordingNonGUIView dummyView = new RecordingNonGUIView();
    CalenderControllerImpl controller = new CalenderControllerImpl(manager, new StringReader(""));
    controller.setView(dummyView);

    // Verify that the nongui branch is taken.
    assertEquals("Controller's view should be set to the provided nongui view.",
            dummyView, controller.getView());
    // Nongui branch does not create a default calendar.
    assertNull("No default calendar should be created for nongui view.",
            manager.getCurrentCalendar());
  }

  /**
   * Test useCalendar method in non-GUI mode.
   */
  @Test
  public void testUseCalendarNonGUI() {
    CalendarManager manager = new CalendarManager();
    try {
      manager.createCalendar("TestCalendar", "America/New_York");
    } catch (Exception e) {
      fail("Failed to create calendar: " + e.getMessage());
    }
    assertNotNull("Calendar 'TestCalendar' should exist.",
            manager.getCalendar("TestCalendar"));

    RecordingNonGUIView dummyView = new RecordingNonGUIView();
    CalenderControllerImpl controller = new CalenderControllerImpl(manager, new StringReader(""));
    controller.setView(dummyView);
    controller.useCalendar("TestCalendar");

    assertEquals("Now using calendar: TestCalendar", dummyView.getLastMessage());
  }

  /**
   * Test that execute() loops once when no extra input line exists.
   *
   * @throws Exception if reflection fails
   */
  @Test
  public void testExecuteLoopsOnceWhenNoExtraLine() throws Exception {
    CalendarManager manager = new CalendarManager();
    RecordingNonGUIView dummyView = new RecordingNonGUIView();
    StringReader sr = new StringReader("dummyCommand");
    CalenderControllerImpl controller = new CalenderControllerImpl(manager, sr);
    controller.setView(dummyView);

    DummyInputParserHelper dummyHelper = new DummyInputParserHelper(controller);
    Field helperField = CalenderControllerImpl.class.getDeclaredField("inputParserHelper");
    helperField.setAccessible(true);
    helperField.set(controller, dummyHelper);

    controller.execute();
    assertEquals("execute() should parse exactly one command.",
            1, dummyHelper.callCount);
  }

  /**
   * Test calendar creation success.
   *
   * @throws Exception if reflection fails
   */
  @Test
  public void testCreateCalendarSuccess() throws Exception {
    CalendarManager manager = new CalendarManager();
    RecordingNonGUIView dummyView = new RecordingNonGUIView();
    CalenderControllerImpl controller = new CalenderControllerImpl(manager, new StringReader(""));
    controller.setView(dummyView);

    controller.createCalendar("MyCal", "America/New_York");
    assertEquals("Calendar created: MyCal", dummyView.getLastMessage());

    Field mapField = CalenderControllerImpl.class.getDeclaredField("calendarColorMap");
    mapField.setAccessible(true);
    @SuppressWarnings("unchecked")
    java.util.Map<String, Color> colorMap = (java.util.Map<String, Color>) mapField.get(controller);
    assertTrue("calendarColorMap should contain the key 'MyCal'.",
            colorMap.containsKey("MyCal"));

    Field nextIndexField = CalenderControllerImpl.class.getDeclaredField("nextColorIndex");
    nextIndexField.setAccessible(true);
    int nextColorIndex = nextIndexField.getInt(controller);
    assertEquals("nextColorIndex should be incremented to 1.", 1, nextColorIndex);
  }

  /**
   * Test duplicate calendar creation.
   *
   * @throws Exception if calendar creation fails
   */
  @Test
  public void testCreateCalendarDuplicate() throws Exception {
    CalendarManager manager = new CalendarManager();
    RecordingNonGUIView dummyView = new RecordingNonGUIView();
    CalenderControllerImpl controller = new CalenderControllerImpl(manager, new StringReader(""));
    controller.setView(dummyView);

    controller.createCalendar("MyCal", "America/New_York");
    assertEquals("Calendar created: MyCal", dummyView.getLastMessage());
    controller.createCalendar("MyCal", "America/New_York");
    String errorMsg = dummyView.getLastMessage();
    assertTrue("Duplicate calendar creation should yield error message.",
            errorMsg.contains("already exists"));
  }

  /**
   * Test adding an event to a calendar successfully.
   *
   * @throws Exception if event creation fails
   */
  @Test
  public void testAddEventToCalendarSuccess() throws Exception {
    CalendarManager manager = new CalendarManager();
    // Create and select an active calendar.
    manager.createCalendar("TestCal", "America/New_York");
    RecordingNonGUIView dummyView = new RecordingNonGUIView();
    CalenderControllerImpl controller = new CalenderControllerImpl(manager, new StringReader(""));
    controller.setView(dummyView);
    controller.useCalendar("TestCal");

    // Create a dummy event.
    IEventModel event = EventModelImpl.getBuilder("Dummy Event", "2025-04-20T09:00",
                    ZoneId.of("America/New_York"))
            .endDateString("2025-04-20T10:00")
            .build();
    controller.addEventToCalender(event, true);

    assertEquals("Event added: Dummy Event", dummyView.getLastMessage());
  }

  /**
   * Test importCalendar with an invalid file extension.
   */
  @Test
  public void testImportCalendarInvalidExtension() {
    CalendarManager manager = new CalendarManager();
    RecordingNonGUIView view = new RecordingNonGUIView();
    CalenderControllerImpl controller = new CalenderControllerImpl(manager, new StringReader(""));
    controller.setView(view);

    try {
      controller.importCalendar("data.txt");
      fail("Expected IllegalArgumentException for invalid file extension.");
    } catch (IllegalArgumentException e) {
      assertEquals("File must have a .csv extension.", e.getMessage());
      assertTrue("Should display extension error message.",
              view.getMessages().contains("Error: File must have a .csv extension."));
    }
  }

  /**
   * Test importCalendar with an empty CSV file.
   *
   * @throws Exception if file operations fail
   */
  @Test
  public void testImportCalendarEmptyFile() throws Exception {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("ActiveCal", "America/New_York");
    RecordingNonGUIView view = new RecordingNonGUIView();
    CalenderControllerImpl controller = new CalenderControllerImpl(manager, new StringReader(""));
    controller.setView(view);

    Path tempFile = Files.createTempFile("empty", ".csv");
    try {
      Files.write(tempFile, "".getBytes());
      controller.importCalendar(tempFile.toAbsolutePath().toString());
      String expected = "Imported 0 events from " +
              tempFile.toAbsolutePath().toString();
      assertEquals(expected, view.getLastMessage());
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  /**
   * Test importCalendar with a row having too few columns.
   *
   * @throws Exception if file operations fail
   */
  @Test
  public void testImportCalendarRowWithTooFewColumns() throws Exception {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("ActiveCal", "America/New_York");
    RecordingNonGUIView view = new RecordingNonGUIView();
    CalenderControllerImpl controller = new CalenderControllerImpl(manager, new StringReader(""));
    controller.setView(view);

    // Provide CSV content with a row that has fewer than 6 tokens.
    String content = "bad,row\n";
    Path tempFile = Files.createTempFile("invalidRow", ".csv");
    try {
      Files.write(tempFile, content.getBytes());
      controller.importCalendar(tempFile.toAbsolutePath().toString());
      boolean found = view.getMessages().stream()
              .anyMatch(msg -> msg.contains("Skipping invalid row: bad,row"));
      assertTrue("Should display message for skipping invalid row", found);
      String expectedFinal = "Imported 0 events from " +
              tempFile.toAbsolutePath().toString();
      assertEquals(expectedFinal, view.getLastMessage());
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  /**
   * Test that the header row is skipped during CSV import.
   *
   * @throws Exception if file operations fail
   */
  @Test
  public void testImportCalendarHeaderRowSkipped() throws Exception {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("ActiveCal", "America/New_York");
    RecordingNonGUIView view = new RecordingNonGUIView();
    CalenderControllerImpl controller = new CalenderControllerImpl(manager, new StringReader(""));
    controller.setView(view);

    String content = "Subject,Start Date,Start Time,End Date,End Time,All Day Event\n" +
            "Meeting,2025-04-20,09:00,2025-04-20,10:00,False\n";
    Path tempFile = Files.createTempFile("headerAndValid", ".csv");
    try {
      Files.write(tempFile, content.getBytes());
      controller.importCalendar(tempFile.toAbsolutePath().toString());
      String expectedFinal = "Imported 1 events from " +
              tempFile.toAbsolutePath().toString();
      assertEquals(expectedFinal, view.getLastMessage());
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  /**
   * Test CSV import with missing times; defaults are used.
   *
   * @throws Exception if file operations fail
   */
  @Test
  public void testImportCalendarMissingTimes() throws Exception {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("ActiveCal", "America/New_York");
    RecordingNonGUIView view = new RecordingNonGUIView();
    CalenderControllerImpl controller = new CalenderControllerImpl(manager, new StringReader(""));
    controller.setView(view);

    // Provide a row with missing times.
    String content = "EventX,2025-04-21,,2025-04-21,,False\n";
    Path tempFile = Files.createTempFile("missingTimes", ".csv");
    try {
      Files.write(tempFile, content.getBytes());
      controller.importCalendar(tempFile.toAbsolutePath().toString());
      String expectedFinal = "Imported 1 events from " +
              tempFile.toAbsolutePath().toString();
      assertEquals(expectedFinal, view.getLastMessage());
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  /**
   * Test CSV import for an all-day event.
   *
   * @throws Exception if file operations fail
   */
  @Test
  public void testImportCalendarAllDayEvent() throws Exception {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("ActiveCal", "America/New_York");
    RecordingNonGUIView view = new RecordingNonGUIView();
    CalenderControllerImpl controller = new CalenderControllerImpl(manager, new StringReader(""));
    controller.setView(view);

    // For all-day events, the times are ignored.
    String content = "EventAllDay,2025-04-22,ignored,2025-04-22,ignored,True\n";
    Path tempFile = Files.createTempFile("allDay", ".csv");
    try {
      Files.write(tempFile, content.getBytes());
      controller.importCalendar(tempFile.toAbsolutePath().toString());
      String expectedFinal = "Imported 1 events from " +
              tempFile.toAbsolutePath().toString();
      assertEquals(expectedFinal, view.getLastMessage());
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  /**
   * Test CSV import with an invalid date in a row.
   *
   * @throws Exception if file operations fail
   */
  @Test
  public void testImportCalendarInvalidDateRow() throws Exception {
    CalendarManager manager = new CalendarManager();
    manager.createCalendar("ActiveCal", "America/New_York");
    RecordingNonGUIView view = new RecordingNonGUIView();
    CalenderControllerImpl controller = new CalenderControllerImpl(manager, new StringReader(""));
    controller.setView(view);

    // Provide a row with an invalid date to trigger an error.
    String content = "BadEvent,invalid-date,09:00,2025-04-20,10:00,False\n";
    Path tempFile = Files.createTempFile("invalidDate", ".csv");
    try {
      Files.write(tempFile, content.getBytes());
      controller.importCalendar(tempFile.toAbsolutePath().toString());
      boolean found = view.getMessages().stream().anyMatch(msg ->
              msg.startsWith("Error parsing event from row: BadEvent," +
                      "invalid-date,09:00,2025-04-20,10:00,False. Error:"));
      assertTrue("Should display error parsing message for invalid date row", found);
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  /**
   * Test CSV import when file cannot be read.
   *
   * @throws Exception if file operations fail
   */
  @Test
  public void testImportCalendarIOException() throws Exception {
    CalendarManager manager = new CalendarManager();
    RecordingNonGUIView view = new RecordingNonGUIView();
    CalenderControllerImpl controller = new CalenderControllerImpl(manager, new StringReader(""));
    controller.setView(view);

    String fakePath = "nonexistent_file.csv";
    controller.importCalendar(fakePath);
    boolean found = view.getMessages().stream().anyMatch(msg ->
            msg.startsWith("Error reading CSV file:"));
    assertTrue("Should display error message for IOException", found);
  }

}
