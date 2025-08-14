package controller;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.InvalidCalenderOperationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests EditCommand for handling various edit scenarios. Verifies different
 * command patterns and ensures correct call logging in a stubbed controller.
 */
public class EditCommandTest {

  private EditCommand editCommand;

  private List<String> callLog;

  /**
   * Sets up the test environment by initializing the controller and command.
   */
  @Before
  public void setUp() {
    //CalendarManager.resetGlobalState();
    callLog = new ArrayList<>();
    TestController testController = new TestController();
    editCommand = new EditCommand(testController);
  }

  /**
   * Tests editing a calendar with a valid command pattern.
   */
  @Test
  public void testParseAndExecute_editCalendar_valid()
          throws InvalidCalenderOperationException {
    String cmd = "edit calendar --name MyCal --property name NewCal";
    editCommand.parseAndExecute(cmd);
    assertEquals(1, callLog.size());
    assertEquals("editCalendar(name, MyCal, NewCal)", callLog.get(0));
  }

  /**
   * Tests that an invalid calendar edit command triggers an exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_editCalendar_invalidFormat()
          throws InvalidCalenderOperationException {
    String cmd = "edit calendar --name MyCal ??? ???";
    editCommand.parseAndExecute(cmd);
  }

  /**
   * Tests editing a single event with a valid start-to-end command.
   */
  @Test
  public void testParseAndExecute_editSingleEvent_valid()
          throws InvalidCalenderOperationException {
    String cmd = "edit event eventname MyEvent from 2025-02-10T09:00 "
            + "to 2025-02-10T10:00 with CANCELLED";
    editCommand.parseAndExecute(cmd);
    assertEquals(1, callLog.size());
    System.out.println(callLog.get(0));
    assertTrue(callLog.get(0).startsWith("editEvent(eventname, MyEvent, "
            + "start=2025-02-10T14:00:00Z, end=2025-02-10T15:00:00Z, "
            + "newVal=CANCELLED)"));
  }

  /**
   * Tests editing a recurring event from a specific date.
   */
  @Test
  public void testParseAndExecute_editRecurringFrom_valid()
          throws InvalidCalenderOperationException {
    String cmd = "edit events location MyRecurring from 2025-03-10 with ConfRoom";
    editCommand.parseAndExecute(cmd);
    assertEquals(1, callLog.size());
    assertTrue(callLog.get(0).startsWith("editEvent(location, MyRecurring, "
            + "start=2025-03-10T04:00:00Z, newVal=ConfRoom)"));
  }

  /**
   * Tests a simple recurring event edit command.
   */
  @Test
  public void testParseAndExecute_editRecurringSimple_valid()
          throws InvalidCalenderOperationException {
    String cmd = "edit events description MyRecurring newDesc";
    editCommand.parseAndExecute(cmd);
    assertEquals(1, callLog.size());
    assertEquals("editEvent(description, MyRecurring, newVal=newDesc)",
            callLog.get(0));
  }

  /**
   * Tests that an unknown edit command format triggers an exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_invalidFormat()
          throws InvalidCalenderOperationException {
    String cmd = "edit ??? ??? ??? ??? ???";
    editCommand.parseAndExecute(cmd);
  }

  /**
   * A stub subclass of CalenderControllerImpl to capture edit calls.
   */
  private class TestController extends CalenderControllerImpl {

    public TestController() {
      super(null, null,
              new InputStreamReader(new ByteArrayInputStream(new byte[0])));
    }

    @Override
    public void editCalendar(String property, String calName, String propValue)
            throws InvalidCalenderOperationException {
      callLog.add(String.format("editCalendar(%s, %s, %s)",
              property, calName, propValue));
    }

    @Override
    public void editEvent(String property, String eventName,
                          Date startDateTime, Date endDateTime,
                          String propertyValue) {
      String startStr = "start=" + dateToString(startDateTime);
      String endStr = "end=" + dateToString(endDateTime);
      callLog.add(String.format("editEvent(%s, %s, %s, %s, newVal=%s)",
              property, eventName, startStr, endStr, propertyValue));
    }

    @Override
    public void editEvent(String property, String eventName,
                          Date startDateTime, String propertyValue) {
      String startStr = "start=" + dateToString(startDateTime);
      callLog.add(String.format("editEvent(%s, %s, %s, newVal=%s)",
              property, eventName, startStr, propertyValue));
    }

    @Override
    public void editEvent(String property, String eventName,
                          String propertyValue) {
      callLog.add(String.format("editEvent(%s, %s, newVal=%s)",
              property, eventName, propertyValue));
    }

    /**
     * Converts a Date to an ISO-8601 string for logging.
     */
    private String dateToString(Date date) {
      if (date == null) {
        return "null";
      }
      return date.toInstant().toString();
    }
  }
}
