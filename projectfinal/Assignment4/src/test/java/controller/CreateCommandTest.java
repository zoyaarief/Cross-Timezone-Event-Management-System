package controller;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import model.CalenderModelImpl;
import model.CalendarManager;
import model.ICalenderModel;
import model.IEventModel;
import model.InvalidCalenderOperationException;
import view.ICalenderView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Tests CreateCommand for creating calendars and events.
 * Verifies various input patterns and expected outcomes.
 */
public class CreateCommandTest {

  /**
   * TestController is a specialized subclass of CalenderControllerImpl.
   * It overrides certain methods to capture calls without invoking real logic.
   */
  public class TestController extends CalenderControllerImpl {

    private final List<String> callLog;

    /**
     * Builds a test controller that logs calls instead of performing real work.
     */
    public TestController(CalendarManager calenderManager,
                          ICalenderView view,
                          InputStream inputStream) {
      // Pass null references so actual logic in super is skipped
      super(null, null, new InputStreamReader(System.in));
      this.callLog = new ArrayList<>();
    }

    /**
     * Returns the logged calls made during test execution.
     */
    public List<String> getCallLog() {
      return callLog;
    }

    /**
     * Returns a manager whose current calendar has a known zone.
     */
    @Override
    public CalendarManager getCalendarManager() {
      return new CalendarManager() {
        @Override
        public ICalenderModel getCurrentCalendar() {
          return new CalenderModelImpl("FakeCalendar",
                  ZoneId.of("America/New_York"));
        }
      };
    }

    /**
     * Logs creating a calendar without calling super.
     */
    @Override
    public void createCalendar(String calName, String timeZone) {
      callLog.add("createCalendar(" + calName + ", " + timeZone + ")");
    }

    /**
     * Logs adding a single event without calling super.
     */
    @Override
    public void addEventToCalender(IEventModel event, boolean autoDecline) {
      String msg = String.format("addEventToCalender(%s, autoDecline=%b)",
              event.getEventName(), autoDecline);
      callLog.add(msg);
    }

    /**
     * Logs adding a recurring event for a set number of weeks.
     */
    @Override
    public void addRecurringEventToCalender(IEventModel event,
                                            boolean autoDecline,
                                            String weekDays,
                                            int noOfWeeks) {
      String msg = String.format("addRecurringEventToCalender(%s, autoDecline=%b, %s, %d)",
              event.getEventName(), autoDecline, weekDays, noOfWeeks);
      callLog.add(msg);
    }

    /**
     * Logs adding a recurring event until a certain date.
     */
    @Override
    public void addRecurringEventToCalender(IEventModel event,
                                            boolean autoDecline,
                                            String weekDays,
                                            String untilDateTime) {
      String msg = String.format("addRecurringEventToCalender(%s, autoDecline=%b, %s, until=%s)",
              event.getEventName(), autoDecline, weekDays, untilDateTime);
      callLog.add(msg);
    }
  }

  private CreateCommand createCmd;
  private TestController testController;


  /**
   * Prepares a test controller and command before each test.
   */
  @Before
  public void setUp() {
    //CalendarManager.resetGlobalState();
    CalendarManager dummyManager = new CalendarManager();
    ICalenderView dummyView = new ICalenderView() {
      @Override
      public void displayMessage(String message) {
        // no-op
      }

      @Override
      public void displayEvents(List<IEventModel> events) {
        // no-op
      }
    };
    InputStream dummyIn = new ByteArrayInputStream(new byte[0]);
    testController = new TestController(dummyManager, dummyView, dummyIn);
    createCmd = new CreateCommand(testController);
  }

  /**
   * Tests creating a calendar with valid syntax.
   */
  @Test
  public void testParseAndExecute_createCalendar_valid()
          throws InvalidCalenderOperationException {
    String command = "create calendar --name MyCal --timezone America/Los_Angeles";
    createCmd.parseAndExecute(command);
    List<String> log = testController.getCallLog();
    assertEquals(1, log.size());
    assertEquals("createCalendar(MyCal, America/Los_Angeles)", log.get(0));
  }

  /**
   * Tests that an invalid calendar creation format triggers an exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_createCalendar_invalidFormat()
          throws InvalidCalenderOperationException {
    String command = "create calendar --name MyCal ??? ";
    createCmd.parseAndExecute(command);
  }

  /**
   * Tests creating a single event with from/to syntax.
   */
  @Test
  public void testParseAndExecute_createEvent_fromTo()
          throws InvalidCalenderOperationException {
    String command = "create event Meeting from 2025-02-10T10:00 to 2025-02-10T12:00";
    createCmd.parseAndExecute(command);
    List<String> log = testController.getCallLog();
    assertEquals(1, log.size());
    assertTrue(log.get(0).startsWith("addEventToCalender("));
    assertTrue(log.get(0).contains("Meeting"));
  }

  /**
   * Tests creating a recurring event for a fixed number of weeks.
   */
  @Test
  public void testParseAndExecute_createEvent_fromToRepeatFor()
          throws InvalidCalenderOperationException {
    String command = "create event MyRecurring from 2025-02-10T09:00 "
            + "to 2025-02-10T10:00 repeats MW for 5 times";
    createCmd.parseAndExecute(command);
    List<String> log = testController.getCallLog();
    assertEquals(1, log.size());
    assertTrue(log.get(0).startsWith("addRecurringEventToCalender("));
    assertTrue(log.get(0).contains("MyRecurring"));
    assertTrue(log.get(0).contains("MW"));
    assertTrue(log.get(0).contains("5"));
  }

  /**
   * Tests creating a recurring event until a specified date.
   */
  @Test
  public void testParseAndExecute_createEvent_fromToRepeatUntil()
          throws InvalidCalenderOperationException {
    String command = "create event MyRecurring2 from 2025-02-10T09:00 "
            + "to 2025-02-10T10:00 repeats MW until 2025-03-10T09:00";
    createCmd.parseAndExecute(command);
    List<String> log = testController.getCallLog();
    assertEquals(1, log.size());
    assertTrue(log.get(0).startsWith("addRecurringEventToCalender("));
    assertTrue(log.get(0).contains("MyRecurring2"));
    assertTrue(log.get(0).contains("MW"));
    assertTrue(log.get(0).contains("until=2025-03-10T09:00"));
  }

  /**
   * Tests creating a recurring event by day pattern for a fixed number of times.
   */
  @Test
  public void testParseAndExecute_createEvent_onRepeatFor()
          throws InvalidCalenderOperationException {
    String command = "create event AnotherEvent on 2025-02-10 repeats MTWRFSU for 3 times";
    createCmd.parseAndExecute(command);
    List<String> log = testController.getCallLog();
    assertEquals(1, log.size());
    assertTrue(log.get(0).startsWith("addRecurringEventToCalender("));
    assertTrue(log.get(0).contains("AnotherEvent"));
    assertTrue(log.get(0).contains("MTWRFSU"));
    assertTrue(log.get(0).contains("3"));
  }

  /**
   * Tests creating a recurring event by day pattern until a specified date.
   */
  @Test
  public void testParseAndExecute_createEvent_onRepeatUntil()
          throws InvalidCalenderOperationException {
    String command = "create event AnotherEvent2 on 2025-02-10 repeats MW "
            + "until 2025-03-01";
    createCmd.parseAndExecute(command);
    List<String> log = testController.getCallLog();
    assertEquals(1, log.size());
    assertTrue(log.get(0).startsWith("addRecurringEventToCalender("));
    assertTrue(log.get(0).contains("AnotherEvent2"));
    assertTrue(log.get(0).contains("MW"));
    assertTrue(log.get(0).contains("until=2025-03-01"));
  }

  /**
   * Tests creating a single event on a specific date or date-time.
   */
  @Test
  public void testParseAndExecute_createEvent_onOnly()
          throws InvalidCalenderOperationException {
    String command = "create event SingleDay on 2025-02-10T09:00";
    createCmd.parseAndExecute(command);
    List<String> log = testController.getCallLog();
    assertEquals(1, log.size());
    assertTrue(log.get(0).startsWith("addEventToCalender("));
    assertTrue(log.get(0).contains("SingleDay"));
  }

  /**
   * Tests an invalid event creation command.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_invalidCommand()
          throws InvalidCalenderOperationException {
    String command = "create event BadlyFormed from ??? ??? ??? ";
    createCmd.parseAndExecute(command);
  }

  /**
   * Tests that missing timezone for calendar creation triggers an exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_missingCalendarTimeZone()
          throws InvalidCalenderOperationException {
    String command = "create calendar --name SomeCal ??? ";
    createCmd.parseAndExecute(command);
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_emptyDateInFromTo() throws InvalidCalenderOperationException {
    createCmd.parseAndExecute("create event Meeting from   to 2025-02-10T12:00");
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_invalidWeekdaysChar() throws InvalidCalenderOperationException {
    createCmd.parseAndExecute("create event " +
            "MyRecurring from 2025-02-10T09:00 to 2025-02-10T10:00 repeats MZ for 5 times");
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_duplicateWeekdays()
          throws InvalidCalenderOperationException {
    createCmd.parseAndExecute("create event MyRecurring " +
            "from 2025-02-10T09:00 to 2025-02-10T10:00 repeats MM for 5 times");
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_tooManyWeekdays()
          throws InvalidCalenderOperationException {
    createCmd.parseAndExecute("create event MyRecurring " +
            "on 2025-02-10 repeats MTWRFSUM for 3 times");
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_emptyWeekdays() throws InvalidCalenderOperationException {
    createCmd.parseAndExecute("create event MyRecurring on 2025-02-10 repeats   for 3 times");
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_missingStartDate() throws InvalidCalenderOperationException {
    createCmd.parseAndExecute("create event MyEvent from  to 2025-02-10T12:00");
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_missingEndDate() throws InvalidCalenderOperationException {
    createCmd.parseAndExecute("create event MyEvent from 2025-02-10T10:00 to  ");
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_nullStartDate() throws InvalidCalenderOperationException {
    createCmd.parseAndExecute("create event MyEvent from null to 2025-02-10T12:00");
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_nullEndDate() throws InvalidCalenderOperationException {
    createCmd.parseAndExecute("create event MyEvent from 2025-02-10T10:00 to null");
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_repeatUntil_invalidEndBeforeStart()
          throws InvalidCalenderOperationException {
    // This will hit validateDateTimeOrder for both start/end and end/until comparisons
    String command = "create event ConflictEvent from 2025-03-01T10:00 " +
            "to 2025-03-01T09:00 repeats MW until 2025-03-01T08:00";
    createCmd.parseAndExecute(command);
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_fromTo_invalidEmptyStartDate()
          throws InvalidCalenderOperationException {
    // This will directly hit the "startDateTime is empty" check
    String command = "create event Incomplete from    to 2025-04-01T10:00";
    createCmd.parseAndExecute(command);
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_fromTo_invalidEmptyEndDate()
          throws InvalidCalenderOperationException {
    // This will hit the "endDateTime is empty" check
    String command = "create event Incomplete from 2025-04-01T10:00 to  ";
    createCmd.parseAndExecute(command);
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_repeatFor_weekdaysDuplicateLetter()
          throws InvalidCalenderOperationException {
    // This will trigger the duplicate weekday letter case (e.g., "MM")
    String command = "create event DuplicateEvent on 2025-04-01 repeats MM for 2 times";
    createCmd.parseAndExecute(command);
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_repeatFor_invalidCharacterInWeekdays()
          throws InvalidCalenderOperationException {
    // Invalid weekday character (like "Z") triggers validateWeekdays failure
    String command = "create event BadWeekday on 2025-04-01 repeats Z for 2 times";
    createCmd.parseAndExecute(command);
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testInvalidDateOrder_fromTo_shouldThrow() throws InvalidCalenderOperationException {
    String command = "create event TestEvent from 2025-03-01T10:00 to 2025-03-01T09:00";
    createCmd.parseAndExecute(command);
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testTooManyWeekdays_shouldThrow() throws InvalidCalenderOperationException {
    String command = "create event WeeklyEvent on 2025-03-01 repeats MTWRFSUMX for 3 times";
    createCmd.parseAndExecute(command);
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testValidateDateTimeOrder_emptyStart() throws InvalidCalenderOperationException {
    String cmd = "create event Test from   to 2025-02-10T10:00";
    createCmd.parseAndExecute(cmd);
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testValidateDateTimeOrder_endBeforeStart()
          throws InvalidCalenderOperationException {
    String cmd = "create event Test from 2025-02-10T11:00 to 2025-02-10T10:00";
    createCmd.parseAndExecute(cmd);
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testValidateDateTimeOrder_invalidFormat()
          throws InvalidCalenderOperationException {
    String cmd = "create event Test from not-a-date to 2025-02-10T10:00";
    createCmd.parseAndExecute(cmd);
  }

  @Test
  public void testValidateWeekdays_tooManyLetters_logged() {
    try {
      createCmd.parseAndExecute("create event Y on 2025-02-10 repeats MTWRFSUM for 2 times");
      fail("Expected InvalidCalenderOperationException");
    } catch (InvalidCalenderOperationException e) {
      System.out.println("Caught expected weekday error: " + e.getMessage());
    }
  }

  @Test
  public void testValidateDateTimeOrder_endBeforeStart_logged() {
    try {
      createCmd.parseAndExecute("create event X from 2025-03-01T10:00 to 2025-03-01T09:00");
      fail("Expected InvalidCalenderOperationException");
    } catch (InvalidCalenderOperationException e) {
      System.out.println("Caught expected datetime order error: " + e.getMessage());
    }
  }

  @Test
  public void testValidateDateTimeOrder_called_inRepeatUntil() {
    try {
      // Use parsable ISO 8601 format
      String command = "create event MyEvent from 2025-12-12T10:00 to 2025-12-12T09:00 " +
              "repeats MTWRF until 2025-12-13T10:00";
      createCmd.parseAndExecute(command);
      fail("Expected InvalidCalenderOperationException");
    } catch (InvalidCalenderOperationException e) {
      System.out.println("validateDateTimeOrder exception caught: " + e.getMessage());
      // Accept either message for flexibility
      assertTrue(
              e.getMessage().contains("Start time must be before end time") ||
                      e.getMessage().contains("Invalid date/time format")
      );
    }
  }


  @Test
  public void testValidateWeekdays_invalidChar_triggered() {
    try {
      String command = "create event MyEvent from 2025-12-12T09:00 to 2025-12-12T10:00 " +
              "repeats MX until 2025-12-13T10:00";
      createCmd.parseAndExecute(command);
      fail("Expected InvalidCalenderOperationException");
    } catch (InvalidCalenderOperationException e) {
      System.out.println(" validateWeekdays exception caught: " + e.getMessage());
      assertTrue(e.getMessage().contains("Invalid weekday letter: X"));
    }
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testValidateDateTimeOrder_missingEndDate()
          throws InvalidCalenderOperationException {
    createCmd.parseAndExecute("create event TestEvent from 2025-12-01T10:00 to   ");
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testValidateDateTimeOrder_missingStartDate()
          throws InvalidCalenderOperationException {
    createCmd.parseAndExecute("create event TestEvent from   to 2025-12-01T12:00");
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testValidateDateTimeOrder_nullStartDate()
          throws InvalidCalenderOperationException {
    createCmd.parseAndExecute("create event TestEvent from null to 2025-12-01T12:00");
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testValidateWeekdays_invalidCharacter()
          throws InvalidCalenderOperationException {
    createCmd.parseAndExecute("create event TestEvent on 2025-12-01 repeats MX for 5 times");
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testValidateWeekdays_duplicateLetters()
          throws InvalidCalenderOperationException {
    createCmd.parseAndExecute("create event " +
            "TestEvent on 2025-12-01 repeats MM for 3 times");
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testValidateWeekdays_tooManyLetters()
          throws InvalidCalenderOperationException {
    createCmd.parseAndExecute("create event TestEvent " +
            "on 2025-12-01 repeats MTWRFSUX for 5 times");
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testValidateWeekdays_emptyString()
          throws InvalidCalenderOperationException {
    createCmd.parseAndExecute("create event TestEvent on " +
            "2025-12-01 repeats   for 3 times");
  }


}
