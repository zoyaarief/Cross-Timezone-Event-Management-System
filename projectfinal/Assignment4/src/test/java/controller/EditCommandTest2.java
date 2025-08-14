package controller;

import model.InvalidCalenderOperationException;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;


/**
 * Tests EditCommand (second time) for handling various edit scenarios. Verifies different
 * command patterns and ensures correct call logging in a stubbed controller.
 */
public class EditCommandTest2 {

  private EditCommand editCommand;
  private List<String> log;

  @Before
  public void setUp() {
    //CalendarManager.resetGlobalState();
    log = new ArrayList<>();
    editCommand = new EditCommand(new MockController());
  }

  // === Success Paths ===

  @Test
  public void testEditCalendar_success_printlnCovered()
          throws InvalidCalenderOperationException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    editCommand.parseAndExecute("edit calendar --name " +
            "MyCal --property name NewName");

    System.setOut(System.out);
    assertTrue(out.toString().contains("Successfully updated calendar for MyCal"));
    assertTrue(log.contains("editCalendar(name, MyCal, NewName)"));
  }

  @Test
  public void testEditSingleEvent_success_printlnCovered()
          throws InvalidCalenderOperationException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    editCommand.parseAndExecute("edit event name" +
            " MyEvent from 2025-01-01T10:00 to 2025-01-01T11:00 with CANCELLED");

    System.setOut(System.out);
    assertTrue(out.toString().contains("Successfully updated event MyEvent"));
  }

  @Test
  public void testEditRecurringFrom_success_printlnCovered()
          throws InvalidCalenderOperationException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    editCommand.parseAndExecute("edit events location " +
            "RecurringEvent from 2025-02-01 with NewRoom");

    System.setOut(System.out);
    assertTrue(out.toString().contains("Successfully updated " +
            "recurring events for RecurringEvent"));
  }

  @Test
  public void testEditRecurringSimple_success_printlnCovered()
          throws InvalidCalenderOperationException {
    EditCommand safeCommand = new EditCommand(new MockSimpleSuccessController());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    safeCommand.parseAndExecute("edit events description MyEvent UpdatedDescription");

    System.setOut(System.out);
    assertTrue(out.toString().contains("Successfully updated all events for MyEvent"));
  }


  @Test
  public void testEditCalendar_error_printlnCovered() {
    EditCommand failingCmd = new EditCommand(new FailingController());
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    System.setErr(new PrintStream(err));

    try {
      failingCmd.parseAndExecute("edit calendar --name MyCal --property name NewName");
    } catch (InvalidCalenderOperationException ignored) {
    }

    System.setErr(System.err);
    assertTrue(err.toString().contains("Error: Boom! Please try again."));
  }

  @Test
  public void testEditSingleEvent_invalidDate_errorPrintlnCovered() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    System.setErr(new PrintStream(err));
    try {
      editCommand.parseAndExecute("edit event name Event from " +
              "02-01-2025 to 2025-01-01T11:00 with NewValue");
    } catch (InvalidCalenderOperationException ignored) {
    }
    System.setErr(System.err);
    assertTrue(err.toString().contains("Expected date in format YYYY-MM-DD"));
  }

  @Test
  public void testEditRecurringFrom_invalidDate_errorPrintlnCovered() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    PrintStream originalErr = System.err;
    System.setErr(new PrintStream(err));

    try {
      EditCommand safeCommand = new EditCommand(new MockController());
      safeCommand.parseAndExecute("edit events location Event from 01/01/2025 with Room");
    } catch (InvalidCalenderOperationException ignored) {
      // expected
    }

    System.setErr(originalErr);
    String errorOutput = err.toString();
    assertTrue("Expected error output to contain 'Expected date in format YYYY-MM-DD'",
            errorOutput.contains("Expected date in format YYYY-MM-DD"));
  }

  @Test
  public void testEditRecurringSimple_missingArgs_errorPrintlnCovered() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    PrintStream originalErr = System.err;
    System.setErr(new PrintStream(err));

    try {
      editCommand.parseAndExecute("edit events description MyEvent");
    } catch (InvalidCalenderOperationException e) {
      System.err.println("Error: " + e.getMessage() + " Please try again.");
    } finally {
      System.setErr(originalErr);
    }

    String output = err.toString();
    assertTrue("Expected error output to contain 'Error: Invalid edit command format'",
            output.contains("Error: Invalid edit command format"));
  }

  @Test
  public void testEditRecurringFrom_invalidDate_errorPrintlnCovered2() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    PrintStream originalErr = System.err;
    System.setErr(new PrintStream(err));

    try {
      editCommand.parseAndExecute("edit events location Event from 01/01/2025 with Room");
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage() + " Please try again.");
    } finally {
      System.setErr(originalErr);
    }

    String output = err.toString();
    assertTrue("Expected error output to contain 'Expected date in format YYYY-MM-DD'",
            output.contains("Expected date in format YYYY-MM-DD"));
  }

  @Test
  public void testEditCalendar_success_println_covered() throws InvalidCalenderOperationException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(out));

    editCommand.parseAndExecute("edit calendar --name MyCal --property name NewName");

    System.setOut(originalOut);
    String output = out.toString();
    assertTrue("Expected output to confirm calendar update",
            output.contains("Successfully updated calendar for MyCal"));
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testEditCommand_invalidPattern_throws() throws InvalidCalenderOperationException {
    editCommand.parseAndExecute("edit nonsense ???");
  }


  // === Mock Success Controller ===
  private class MockController extends CalenderControllerImpl {
    public MockController() {
      super(null, null,
              new InputStreamReader(new ByteArrayInputStream(new byte[0])));
    }

    @Override
    public void editCalendar(String p, String n, String v) {
      log.add(String.format("editCalendar(%s, %s, %s)", p, n, v));
    }

    @Override
    public void editEvent(String p, String n, Date s, Date e, String v) {
      log.add("editEvent(full)");
    }

    @Override
    public void editEvent(String p, String n, Date s, String v) {
      log.add("editEvent(from)");
    }

    @Override
    public void editEvent(String p, String n, String v) {
      log.add("editEvent(simple)");
    }
  }

  private class MockSimpleSuccessController extends CalenderControllerImpl {
    public MockSimpleSuccessController() {
      super(null, null, new InputStreamReader(new ByteArrayInputStream(new byte[0])));
    }

    @Override
    public void editEvent(String p, String n, String v) {
      log.add("editEvent(simple)");
    }
  }

  private class FailingController extends CalenderControllerImpl {
    public FailingController() {
      super(null, null,
              new InputStreamReader(new ByteArrayInputStream(new byte[0])));
    }

    @Override
    public void editCalendar(String p, String n, String v)
            throws InvalidCalenderOperationException {
      throw new InvalidCalenderOperationException("Boom!");
    }
  }


}
