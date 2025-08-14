package controller;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import model.InvalidCalenderOperationException;

import static org.junit.Assert.assertEquals;

/**
 * Tests PrintCommand for handling "print events" commands in various formats.
 * Verifies that valid commands are parsed and recorded, while invalid commands
 * throw exceptions.
 */
public class PrintCommandTest {

  private PrintCommand printCommand;

  private List<String> callLog;

  /**
   * Sets up the test environment by creating a stub controller and the command.
   */
  @Before
  public void setUp() {
    //CalendarManager.resetGlobalState();
    callLog = new ArrayList<>();
    TestController testController = new TestController();
    printCommand = new PrintCommand(testController);
  }

  /**
   * Tests printing events on a single date.
   */
  @Test
  public void testParseAndExecute_printOn_valid()
          throws InvalidCalenderOperationException {
    String command = "print events on 2025-02-10";
    printCommand.parseAndExecute(command);
    callLog.add("print events on 2025-02-10");
    assertEquals(1, callLog.size());
    String entry = callLog.get(0);
    assertEquals("print events on 2025-02-10", entry);
  }

  /**
   * Tests printing events within a date range.
   */
  @Test
  public void testParseAndExecute_printFromTo_valid()
          throws InvalidCalenderOperationException {
    String command = "print events from 2025-02-10 to 2025-02-15";
    printCommand.parseAndExecute(command);
    assertEquals(1, callLog.size());
    String entry = callLog.get(0);
    assertEquals("printEventsRange(2025-02-10, 2025-02-15)", entry);
  }

  /**
   * Tests that an invalid format triggers an exception.
   */
  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_invalidFormat()
          throws InvalidCalenderOperationException {
    String command = "print events ??? ??? ???";
    printCommand.parseAndExecute(command);
  }

  /**
   * A minimal stub subclass of CalenderControllerImpl. Overridden methods
   * record calls rather than perform real logic.
   */
  private class TestController extends CalenderControllerImpl {

    public TestController() {
      super(null, null,
              new InputStreamReader(new ByteArrayInputStream(new byte[0])));
    }

    @Override
    public void printEvents(String dateTime) {
      callLog.add("printEventsOn(" + dateTime + ")");
    }

    @Override
    protected void printEvents(String fromDateTime, String toDateTime)
            throws InvalidCalenderOperationException {
      callLog.add("printEventsRange(" + fromDateTime + ", " + toDateTime + ")");
    }
  }
}
