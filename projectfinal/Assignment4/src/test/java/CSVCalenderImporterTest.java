import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.List;

import controller.CSVCalenderImporter;
import model.EventStatus;
import model.IEventModel;
import model.InvalidCalenderOperationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Test class for CSVCalenderImporter.
 * Tests valid and invalid CSV import scenarios.
 */
public class CSVCalenderImporterTest {

  private File tempValidFile;
  private CSVCalenderImporter importer;

  /**
   * Sets up the test environment.
   * Creates a temporary CSV file with valid content.
   */
  @Before
  public void setUp() throws IOException {
    //CalendarManager.resetGlobalState();
    importer = new CSVCalenderImporter();
    // Create a temporary CSV file with valid content.
    tempValidFile = File.createTempFile("test_event_import", ".csv");
    tempValidFile.deleteOnExit();

    String csvContent =
            "Subject,Start Date,Start Time,End Date,End Time," +
                    "All Day Event,Description,Location,Private\n" +
                    "Team Meeting,06/01/2025,09:00 AM,06/01/2025,10:00 AM,False," +
                    "\"Monthly team meeting to discuss project updates\"," +
                    "\"Office, Room 101\",False\n" +
                    "Final Exam,05/30/2020,10:00 AM,05/30/2020,01:00 PM,False," +
                    "\"Final exam for CS101: " +
                    "50 multiple choice questions and two essay questions\"," +
                    "\"Columbia, Schermerhorn 614\",True\n" +
                    "Birthday Party,07/04/2025,06:00 PM,07/04/2025,11:00 PM,False," +
                    "\"Birthday celebration with friends and family\"," +
                    "\"123 Party Lane, Fun City\",False\n" +
                    "Christmas Day,12/25/2025,,,,True,\"Holiday:" +
                    " Christmas Day celebration\",,False";

    Files.write(tempValidFile.toPath(), csvContent.getBytes());
  }

  /**
   * Creates a temporary CSV file using the given content.
   *
   * @param content CSV file content
   * @return the temporary File object
   */
  private File createTempCsv(String content) throws IOException {
    File file = File.createTempFile("test_calendar", ".csv");
    file.deleteOnExit();
    Files.writeString(file.toPath(), content);
    return file;
  }

  /**
   * Captures the stderr output when running the given test block.
   *
   * @param testBlock Runnable block to execute
   * @return captured stderr output as a String
   */
  private String captureStderr(Runnable testBlock) {
    PrintStream originalErr = System.err;
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(errContent));
    try {
      testBlock.run();
    } finally {
      System.setErr(originalErr);
    }
    return errContent.toString();
  }

  /**
   * Tests the successful import of events from a valid CSV file.
   */
  @Test
  public void testImportEventsSuccess() throws InvalidCalenderOperationException {
    // Use a known ZoneId, e.g., America/New_York
    ZoneId zone = ZoneId.of("America/New_York");
    List<IEventModel> events = importer.importCalendar(tempValidFile.getAbsolutePath(), zone);

    // Print out the imported events for visual verification.
    System.out.println("=== Imported Events ===");
    for (IEventModel event : events) {
      System.out.println(event.toString());
    }
    System.out.println("=======================");

    // Assert that the expected number of events are imported.
    assertEquals("Expected 4 events imported", 4, events.size());

    // Optionally, you can add more assertions to check details of each event.
    IEventModel event1 = events.get(0);
    assertEquals("Team Meeting", event1.getEventName());

    IEventModel event2 = events.get(1);
    assertEquals("Final Exam", event2.getEventName());
  }

  /**
   * Tests CSV import with an invalid header.
   * Expects an InvalidCalenderOperationException.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testInvalidHeader() throws Exception {
    // Create a temporary CSV file with an invalid header.
    File invalidHeaderFile = File.createTempFile("invalid_header", ".csv");
    invalidHeaderFile.deleteOnExit();

    String invalidCsv = "Name,Date\nEvent 1,06/01/2025";
    Files.write(invalidHeaderFile.toPath(), invalidCsv.getBytes());

    ZoneId zone = ZoneId.of("America/New_York");
    // This should throw an exception because the header does not match
    // the expected format.
    importer.importCalendar(invalidHeaderFile.getAbsolutePath(), zone);
  }

  /**
   * Tests CSV import with an invalid optional header name.
   * Expects an InvalidCalenderOperationException.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testInvalidOptionalHeaderName_shouldThrow() throws Exception {
    String csv = "Subject,Start Date,Wrong Header\n" +
            "Meeting,05/01/2025,10:00 AM";
    File file = File.createTempFile("bad_header", ".csv");
    file.deleteOnExit();
    Files.writeString(file.toPath(), csv);

    new CSVCalenderImporter().importCalendar(file.getAbsolutePath(),
            ZoneId.of("America/New_York"));
  }

  /**
   * Tests that a CSV line missing the start date is skipped.
   */
  @Test
  public void testMissingStartDate_shouldSkip() throws Exception {
    String csv = "Subject,Start Date\nMeeting,\n";
    File file = File.createTempFile("missing_start", ".csv");
    file.deleteOnExit();
    Files.writeString(file.toPath(), csv);

    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            file.getAbsolutePath(), ZoneId.systemDefault());
    assertTrue(events.isEmpty()); // Should be skipped
  }

  /**
   * Tests that a CSV line with an invalid start time is skipped.
   */
  @Test
  public void testInvalidStartTime_shouldSkip() throws Exception {
    String csv = String.join("\n",
            "Subject,Start Date,Start Time",
            "Team Sync,05/01/2025,25:00"
    );
    File file = File.createTempFile("bad_time", ".csv");
    file.deleteOnExit();
    Files.writeString(file.toPath(), csv);

    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            file.getAbsolutePath(), ZoneId.systemDefault());
    assertTrue(events.isEmpty()); // Line should be skipped
  }

  /**
   * Tests that a CSV line with an invalid end time is skipped.
   */
  @Test
  public void testInvalidEndTime_shouldSkip() throws Exception {
    String csv = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time",
            "Meeting,05/01/2025,10:00 AM,05/01/2025,25:00"
    );
    File file = File.createTempFile("bad_end_time", ".csv");
    file.deleteOnExit();
    Files.writeString(file.toPath(), csv);

    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            file.getAbsolutePath(), ZoneId.systemDefault());
    assertTrue(events.isEmpty()); // Should be skipped
  }

  /**
   * Tests that a CSV line with an invalid all-day event value is skipped.
   */
  @Test
  public void testInvalidAllDayEvent_shouldSkip() throws Exception {
    String csv = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time," +
                    "All Day Event,Description,Location,Private",
            "Lunch,05/01/2025,12:00 PM,05/01/2025,01:00 PM,Maybe,Food,,False"
    );
    File file = File.createTempFile("bad_allday", ".csv");
    file.deleteOnExit();
    Files.writeString(file.toPath(), csv);

    List<IEventModel> events = new CSVCalenderImporter()
            .importCalendar(file.getAbsolutePath(), ZoneId.systemDefault());

    assertTrue(events.isEmpty());
  }

  /**
   * Tests that a CSV line with an invalid private flag is skipped.
   */
  @Test
  public void testInvalidPrivateFlag_shouldSkip() throws Exception {
    String csv = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time," +
                    "All Day Event,Description,Location,Private",
            "Call,05/01/2025,09:00 AM,05/01/2025,10:00 AM,False," +
                    "Discussion,Office,Unknown"
    );
    File file = File.createTempFile("bad_private", ".csv");
    file.deleteOnExit();
    Files.writeString(file.toPath(), csv);

    List<IEventModel> events = new CSVCalenderImporter()
            .importCalendar(file.getAbsolutePath(), ZoneId.systemDefault());

    assertTrue(events.isEmpty());
  }

  /**
   * Tests that an invalid optional header causes exception with correct index.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testInvalidOptionalHeaderName_shouldThrowCorrectIndex() throws Exception {
    String csv = "Subject,Start Date,Start Time,WRONG,End Time," +
            "All Day Event,Description,Location,Private\n" +
            "Meeting,05/01/2025,10:00 AM,05/01/2025,11:00 AM," +
            "False,Description,Office,False";

    File file = createTempCsv(csv);
    importer.importCalendar(file.getAbsolutePath(), ZoneId.systemDefault());
  }



  /**
   * Tests CSV import with only subject and start date.
   * This test meets the minimum field requirement.
   */
  @Test
  public void testOnlySubjectAndStartDate_shouldPassMinimumRequirement()
          throws Exception {
    String csv = String.join("\n",
            "Subject,Start Date",
            "QuickNote,05/15/2025"  // Just 2 fields, valid
    );

    File file = createTempCsv(csv);
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            file.getAbsolutePath(), ZoneId.systemDefault());

    assertEquals(1, events.size());
    assertEquals("QuickNote", events.get(0).getEventName());
  }

  /**
   * Tests that CSV lines with empty required fields are skipped and logged.
   */
  @Test
  public void testEmptyRequiredFields_shouldSkipAndLog() throws Exception {
    String csv = String.join("\n",
            "Subject,Start Date,Start Time",
            ",05/10/2025,10:00 AM" // Subject is empty
    );

    File file = createTempCsv(csv);
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            file.getAbsolutePath(), ZoneId.systemDefault());

    assertTrue(events.isEmpty());
  }

  /**
   * Tests that CSV lines with an invalid start time are skipped and logged.
   */
  @Test
  public void testInvalidStartTime_shouldSkipAndLog() throws Exception {
    String csv = String.join("\n",
            "Subject,Start Date,Start Time",
            "BadTime,05/10/2025,notATime"
    );

    File file = createTempCsv(csv);
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            file.getAbsolutePath(), ZoneId.systemDefault());

    assertTrue(events.isEmpty());
  }

  /**
   * Tests that CSV lines with an invalid all-day event value are skipped and logged.
   */
  @Test
  public void testInvalidAllDayEvent_shouldSkipAndLog() throws Exception {
    String csv = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event",
            "BadAllDay,05/10/2025,10:00 AM,05/10/2025,11:00 AM,Maybe"
    );

    File file = createTempCsv(csv);
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            file.getAbsolutePath(), ZoneId.systemDefault());

    assertTrue(events.isEmpty());
  }

  /**
   * Tests that CSV lines with an invalid private field are logged as errors.
   */
  @Test
  public void testInvalidPrivateField_shouldLogError() throws Exception {
    String csv = String.join("\n",
            "Subject,Start Date,Start Time,End Date,End Time," +
                    "All Day Event,Description,Location,Private",
            "Meeting,05/01/2025,10:00 AM,05/01/2025,11:00 AM,False,Test,Room,Maybe"
    );

    File file = createTempCsv(csv);
    String stderr = captureStderr(() -> {
      try {
        importer.importCalendar(file.getAbsolutePath(), ZoneId.systemDefault());
      } catch (Exception ignored) {

      }
    });

    assertTrue(stderr.contains("Line 2 skipped: Invalid Private value."));
  }

  /**
   * Creates a temporary CSV file and returns its path.
   *
   * @param content CSV file content
   * @return path of the temporary CSV file as a String
   */
  private String createTempCSV(String content) throws IOException {
    Path tempFile = Files.createTempFile("calendar", ".csv");
    Files.write(tempFile, content.getBytes());
    tempFile.toFile().deleteOnExit();
    return tempFile.toString();
  }

  /**
   * Tests that a header mismatch causes an exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testHeaderMismatchShouldThrow() throws Exception {
    String csv = "Title,Date\nMeeting,05/01/2024";
    new CSVCalenderImporter().importCalendar(createTempCSV(csv),
            ZoneId.systemDefault());
  }

  /**
   * Tests that multiple valid events are correctly imported.
   */
  @Test
  public void testMultipleValidEventsShouldIncrementLineNumber()
          throws Exception {
    String csv = "Subject,Start Date,Start Time\n" +
            "Event A,05/01/2024,09:00 AM\n" +
            "Event B,05/02/2024,10:30 AM";
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            createTempCSV(csv), ZoneId.systemDefault());
    assertEquals(2, events.size());
  }

  /**
   * Tests that an event with a valid start time is parsed correctly.
   */
  @Test  // Line 148: test Start Time is parsed correctly
  public void testEventWithStartTime() throws Exception {
    String csv = "Subject,Start Date,Start Time\nMeeting,06/01/2024,03:45 PM";
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            createTempCSV(csv), ZoneId.systemDefault());
    assertFalse(events.isEmpty());
  }

  /**
   * Tests that an event without a start time uses midnight as default.
   */
  @Test  // Line 148: test default Start Time (midnight)
  public void testEventWithoutStartTimeUsesMidnight() throws Exception {
    String csv = "Subject,Start Date\nMidnight Event,06/01/2024";
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            createTempCSV(csv), ZoneId.systemDefault());
    assertFalse(events.isEmpty());
  }

  /**
   * Tests that an event with end date and time is handled correctly.
   */
  @Test
  public void testEventWithEndDateTime() throws Exception {
    String csv = "Subject,Start Date,Start Time,End Date,End Time\n" +
            "Event X,07/01/2024,08:00 AM,07/01/2024,09:00 AM";
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            createTempCSV(csv), ZoneId.systemDefault());
    assertEquals(1, events.size());
  }

  /**
   * Tests that an event without end date and time is handled correctly.
   */
  @Test
  public void testEventWithoutEndDateTime() throws Exception {
    String csv = "Subject,Start Date,Start Time\n" +
            "Event Y,07/02/2024,10:00 AM";
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            createTempCSV(csv), ZoneId.systemDefault());
    assertEquals(1, events.size());
  }

  /**
   * Tests that an empty CSV file causes an exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testEmptyCSVFile() throws Exception {
    new CSVCalenderImporter().importCalendar(createTempCSV(""),
            ZoneId.systemDefault());
  }

  /**
   * Tests that CSV files with too few header columns cause an exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testInvalidHeadersTooShort() throws Exception {
    String csv = "Subject\nMeeting";
    new CSVCalenderImporter().importCalendar(createTempCSV(csv),
            ZoneId.systemDefault());
  }

  /**
   * Tests that CSV files with wrong header names cause an exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testWrongHeaderNames() throws Exception {
    String csv = "Title,StartDate\nMeeting,05/01/2024";
    new CSVCalenderImporter().importCalendar(createTempCSV(csv),
            ZoneId.systemDefault());
  }

  /**
   * Tests that CSV lines with missing required fields are skipped.
   */
  @Test
  public void testMissingRequiredFieldsLine() throws Exception {
    String csv = "Subject,Start Date\n,05/01/2024\nMeeting,";
    File file = createTempCsv(csv);
    List<IEventModel> events = importer.importCalendar(file.getAbsolutePath(),
            ZoneId.systemDefault());
    assertEquals(0, events.size()); // both should be skipped
  }

  /**
   * Tests that CSV lines with empty required fields result in an empty list.
   */
  @Test
  public void testEmptyRequiredFields() throws Exception {
    String csv = "Subject,Start Date\n,";
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            createTempCSV(csv), ZoneId.systemDefault());
    assertTrue(events.isEmpty());
  }

  /**
   * Tests that CSV lines with an invalid start date format are skipped.
   */
  @Test
  public void testInvalidStartDateFormat() throws Exception {
    String csv = "Subject,Start Date\nMeeting,2024-05-01";
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            createTempCSV(csv), ZoneId.systemDefault());
    assertTrue(events.isEmpty());
  }

  /**
   * Tests that CSV lines with an invalid start time format are skipped.
   */
  @Test
  public void testInvalidStartTimeFormat() throws Exception {
    String csv = "Subject,Start Date,Start Time\nMeeting,05/01/2024,25:00";
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            createTempCSV(csv), ZoneId.systemDefault());
    assertTrue(events.isEmpty());
  }

  /**
   * Tests that CSV lines with invalid end date/time are skipped.
   */
  @Test
  public void testInvalidEndDateTime() throws Exception {
    String csv = "Subject,Start Date,Start Time,End Date,End Time\nMeeting,05/01/2024,10:00 AM,"
            + "not-a-date,not-a-time";
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            createTempCSV(csv), ZoneId.systemDefault());
    assertTrue(events.isEmpty());
  }
  

  /**
   * Tests that CSV lines with an invalid all-day event value are skipped.
   */
  @Test
  public void testInvalidAllDayValue() throws Exception {
    String csv = "Subject,Start Date,Start Time,End Date,End Time," +
            "All Day Event\nMeeting,05/01/2024,"
            + "10:00 AM,05/01/2024,11:00 AM,Maybe";
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            createTempCSV(csv), ZoneId.systemDefault());
    assertTrue(events.isEmpty());
  }

  /**
   * Tests that CSV lines with an invalid private value are skipped.
   */
  @Test
  public void testInvalidPrivateValue() throws Exception {
    String csv = "Subject,Start Date,Start Time,End Date,End Time," +
            "All Day Event,Description,Location,Private\n" +
            "Meeting,05/01/2024,10:00 AM,,,,,,Definitely";
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            createTempCSV(csv), ZoneId.systemDefault());
    assertTrue(events.isEmpty());
  }

  /**
   * Tests that an event with a private flag is marked as PRIVATE.
   */
  @Test
  public void testPrivateEventFlag() throws Exception {
    String csv = "Subject,Start Date,Start Time,End Date,End Time," +
            "All Day Event,Description,Location,Private\n" +
            "Meeting,05/01/2024,10:00 AM,,,,,,True";
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            createTempCSV(csv), ZoneId.systemDefault());
    assertEquals(1, events.size());
    assertEquals("PRIVATE", events.get(0).getStatus().toString());
  }

  /**
   * Tests that events with optional description and location fields are parsed.
   */
  @Test
  public void testOptionalDescriptionAndLocation() throws Exception {
    String csv = "Subject,Start Date,Start Time,End Date,End Time," +
            "All Day Event,Description,Location,Private\n" +
            "Meeting,05/01/2024,10:00 AM,,,,Description here,\"Location, Inc.\",False";
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            createTempCSV(csv), ZoneId.systemDefault());
    assertEquals(1, events.size());
  }

  /**
   * Tests that an event with valid end time is successfully imported.
   */
  @Test
  public void testSuccessfulEventWithEndTime() throws Exception {
    String csv = "Subject,Start Date,Start Time,End Date,End Time\nMeeting," +
            "05/01/2024,10:00 AM,05/01/2024,11:00 AM";
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            createTempCSV(csv), ZoneId.systemDefault());
    assertEquals(1, events.size());
  }

  /**
   * Tests that an IOException is handled by throwing an exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testIOExceptionHandled() throws Exception {
    // Provide a non-existent file path
    new CSVCalenderImporter().importCalendar("nonexistent.csv", ZoneId.systemDefault());
  }

  /**
   * Tests that an empty line in CSV is skipped.
   */
  @Test
  public void testSkipEmptyLine() throws Exception {
    String csv = "Subject,Start Date\n\nMeeting,05/01/2024";
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            createTempCSV(csv), ZoneId.systemDefault());
    assertEquals(1, events.size());
  }

  /**
   * Tests that a header with wrong order causes an exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testHeaderWrongOrder() throws Exception {
    String csv = "Subject,Start Time,Start Date\nMeeting,10:00 AM,05/01/2024";
    new CSVCalenderImporter().importCalendar(createTempCSV(csv), ZoneId.systemDefault());
  }

  /**
   * Tests that CSV lines with missing required fields are skipped.
   */
  @Test
  public void testMissingRequiredFieldsShouldBeSkipped() throws Exception {
    String csv = "Subject,Start Date\n,\nMeeting,05/01/2024";
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            createTempCSV(csv), ZoneId.systemDefault());
    assertEquals(1, events.size()); // Only the second line is valid
  }

  /**
   * Tests that description and location fields are parsed properly.
   */
  @Test
  public void testDescriptionAndLocationParsing() throws Exception {
    String csv = "Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
            "Description,Location,Private\n" +
            "Meeting,05/01/2024,10:00 AM,,,,Meeting desc,\"Board Room\",False";
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            createTempCSV(csv), ZoneId.systemDefault());
    assertEquals(1, events.size());
    // You can expand this to assert fields using getDescription(), getLocation() if available.
  }

  /**
   * Tests that builder exceptions are caught and the event is skipped.
   */
  @Test
  public void testBuilderThrowsShouldBeCaught() throws Exception {
    String csv = "Subject,Start Date,Start Time,End Date,End Time\n" +
            ",05/01/2024,10:00 AM,05/01/2024,11:00 AM";
    List<IEventModel> events = new CSVCalenderImporter().importCalendar(
            createTempCSV(csv), ZoneId.systemDefault());
    assertTrue(events.isEmpty()); // Should be skipped due to builder exception
  }

  /**
   * Tests that parseCSVLine returns an empty list for null or empty input.
   */
  @Test
  public void testParseCSVLine_nullAndEmpty_returnsEmptyList() throws Exception {
    CSVCalenderImporter importer = new CSVCalenderImporter();

    // Use reflection to access the private method
    var method = CSVCalenderImporter.class.getDeclaredMethod("parseCSVLine", String.class);
    method.setAccessible(true);

    List<String> result1 = (List<String>) method.invoke(importer, (Object) null);
    List<String> result2 = (List<String>) method.invoke(importer, "");

    assertNotNull(result1);
    assertNotNull(result2);
    assertTrue(result1.isEmpty());
    assertTrue(result2.isEmpty());
  }

  /**
   * Tests that parseCSVLine handles null or empty input by returning an empty list.
   */
  @Test
  public void testParseCSVLine_nullOrEmpty_returnsEmptyList() throws Exception {
    var method = CSVCalenderImporter.class.getDeclaredMethod("parseCSVLine", String.class);
    method.setAccessible(true);

    List<String> nullResult = (List<String>) method.invoke(importer, (Object) null);
    List<String> emptyResult = (List<String>) method.invoke(importer, "");

    assertTrue(nullResult.isEmpty());
    assertTrue(emptyResult.isEmpty());
  }

  /**
   * Tests that parseCSVLine correctly parses quoted commas.
   */
  @Test
  public void testParseCSVLine_handlesQuotedComma() throws Exception {
    var method = CSVCalenderImporter.class.getDeclaredMethod("parseCSVLine", String.class);
    method.setAccessible(true);

    String line = "Event,\"Office, Room 101\"";
    List<String> tokens = (List<String>) method.invoke(importer, line);

    assertEquals(2, tokens.size());
    assertEquals("Event", tokens.get(0));
    assertEquals("Office, Room 101", tokens.get(1));
  }

  /**
   * Tests that validation fails when header has less than two columns.
   * Expects an InvalidCalenderOperationException.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testValidateHeader_lessThanTwoColumns_throws() throws Exception {
    String csv = "OnlyOneHeader\nValue";
    File file = createTempCsv(csv);
    importer.importCalendar(file.getAbsolutePath(), ZoneId.systemDefault());
  }

  /**
   * Tests that CSV header not starting with Subject and Start Date causes exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testHeaderDoesNotStartWithSubjectStartDate_shouldThrow() throws Exception {
    String csv = "Title,Date\nMeeting,05/01/2025";
    File file = createTempCsv(csv);
    importer.importCalendar(file.getAbsolutePath(), ZoneId.systemDefault());
  }

  /**
   * Tests that CSV header including a wrong optional header causes an exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testHeaderIncludesWrongOptional_shouldThrow() throws Exception {
    String csv = "Subject,Start Date,Start Time,WrongHeader\nMeeting,05/01/2025," +
            "10:00 AM,SomeVal";
    File file = createTempCsv(csv);
    importer.importCalendar(file.getAbsolutePath(), ZoneId.systemDefault());
  }

  /**
   * Tests that lines with less than two tokens are skipped.
   */
  @Test
  public void testLineWithLessThanTwoTokens_shouldSkip() throws Exception {
    String csv = "Subject,Start Date\nJustSubject";
    File file = createTempCsv(csv);
    List<IEventModel> events = importer.importCalendar(file.getAbsolutePath(),
            ZoneId.systemDefault());
    assertTrue(events.isEmpty());
  }

  /**
   * Tests that lines with empty subject or start date are skipped.
   */
  @Test
  public void testEmptySubjectOrStartDate_shouldSkip() throws Exception {
    String csv = "Subject,Start Date\n,05/01/2024\nMeeting,";
    File file = createTempCsv(csv);
    List<IEventModel> events = importer.importCalendar(file.getAbsolutePath(),
            ZoneId.systemDefault());
    assertTrue(events.isEmpty());
  }

  /**
   * Tests that an event with an invalid start date is skipped.
   */
  @Test
  public void testInvalidStartDate_shouldSkip() throws Exception {
    String csv = "Subject,Start Date\nMeeting,invalid-date";
    File file = createTempCsv(csv);
    List<IEventModel> events = importer.importCalendar(file.getAbsolutePath(),
            ZoneId.systemDefault());
    assertTrue(events.isEmpty());
  }

  /**
   * Tests that an event with an invalid end date/time is skipped.
   */
  @Test
  public void testInvalidEndDateTime_shouldSkip() throws Exception {
    String csv = "Subject,Start Date,Start Time,End Date," +
            "End Time\nMeeting,05/01/2024,10:00 AM,invalid,invalid";
    File file = createTempCsv(csv);
    List<IEventModel> events = importer.importCalendar(file.getAbsolutePath(),
            ZoneId.systemDefault());
    assertTrue(events.isEmpty());
  }

  /**
   * Tests that an event with an invalid all-day value is skipped.
   */
  @Test
  public void testInvalidAllDayValue_shouldSkip() throws Exception {
    String csv = "Subject,Start Date,Start Time,End Date,End Time," +
            "All Day Event\nMeeting,05/01/2024,10:00 AM,05/01/2024,11:00 AM,maybe";
    File file = createTempCsv(csv);
    List<IEventModel> events = importer.importCalendar(file.getAbsolutePath(),
            ZoneId.systemDefault());
    assertTrue(events.isEmpty());
  }

  /**
   * Tests that an event with an invalid private value is skipped.
   */
  @Test
  public void testInvalidPrivateValue_shouldSkip() throws Exception {
    String csv = "Subject,Start Date,Start Time,End Date,End Time,All Day Event," +
            "Description,Location,Private\n" +
            "Meeting,05/01/2024,10:00 AM,,,,,,Definitely";
    File file = createTempCsv(csv);
    List<IEventModel> events = importer.importCalendar(file.getAbsolutePath(),
            ZoneId.systemDefault());
    assertTrue(events.isEmpty());
  }

  /**
   * Tests that an event without the private field defaults to PUBLIC status.
   */
  @Test
  public void testEventWithoutPrivateField_shouldDefaultToPublic() throws Exception {
    String csv = "Subject,Start Date\nPublicMeeting,05/01/2025";
    File file = createTempCsv(csv);
    List<IEventModel> events = importer.importCalendar(file.getAbsolutePath(),
            ZoneId.systemDefault());
    assertEquals(EventStatus.PUBLIC, events.get(0).getStatus());
  }

  /**
   * Tests that an event with a valid end time uses the builder's end time.
   */
  @Test
  public void testEventWithValidEndTime_shouldUseBuilderEndTime() throws Exception {
    String csv = "Subject,Start Date,Start Time,End Date,End Time\nMeeting," +
            "05/01/2025,10:00 AM,05/01/2025,11:00 AM";
    File file = createTempCsv(csv);
    List<IEventModel> events = importer.importCalendar(file.getAbsolutePath(),
            ZoneId.systemDefault());
    assertEquals(1, events.size());
  }

  /**
   * Tests that a builder failure is caught and the event is skipped.
   */
  @Test
  public void testBuilderFailure_shouldBeCaught() throws Exception {
    String csv = "Subject,Start Date\n,05/01/2025"; // Missing subject causes builder to throw
    File file = createTempCsv(csv);
    List<IEventModel> events = importer.importCalendar(file.getAbsolutePath(),
            ZoneId.systemDefault());
    assertTrue(events.isEmpty());
  }

  /**
   * Tests that importing a non-existent file path throws an exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testImportFileNotFound_shouldThrow() throws Exception {
    importer.importCalendar("nonexistent.csv", ZoneId.systemDefault());
  }

}
