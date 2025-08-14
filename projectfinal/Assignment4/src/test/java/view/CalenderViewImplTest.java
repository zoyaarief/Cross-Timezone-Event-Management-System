package view;


import model.EventStatus;
import model.IEventModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests CalenderViewImpl by capturing console output.
 */
public class CalenderViewImplTest {

  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  private final PrintStream originalOutput = System.out;
  private CalenderViewImpl view;

  /**
   * Sets up a fresh ByteArrayOutputStream to capture console output before each test.
   */
  @Before
  public void setUp() {
    //CalendarManager.resetGlobalState();
    System.setOut(new PrintStream(outputStream));
    view = new CalenderViewImpl();
  }

  /**
   * Restores the original System.out after each test.
   */
  @After
  public void tearDown() {
    System.setOut(originalOutput);
  }

  /**
   * Tests displayMessage with a normal string message.
   */
  @Test
  public void testDisplayMessageNormalString() {
    String message = "Hello, Calendar!";
    view.displayMessage(message);
    String consoleOutput = outputStream.toString().trim();
    assertEquals("Output should match the message", message, consoleOutput);
  }

  /**
   * Tests displayMessage with an empty string.
   */
  @Test
  public void testDisplayMessageEmptyString() {
    view.displayMessage("");
    String consoleOutput = outputStream.toString().trim();
    assertEquals("Output should be an empty string", "", consoleOutput);
  }

  /**
   * Tests displayMessage with a null message.
   * By default, printing null in Java results in "null".
   * Adjust your expectations if your design differs.
   */
  @Test
  public void testDisplayMessageNull() {
    view.displayMessage(null);
    String consoleOutput = outputStream.toString().trim();
    assertEquals("Printing null should produce 'null'", "null", consoleOutput);
  }

  /**
   * Tests displayEvents when the events list is null.
   * Expect "Empty Calendar".
   */
  @Test
  public void testDisplayEventsNull() {
    view.displayEvents(null);
    String consoleOutput = outputStream.toString().trim();
    assertEquals("Output should be 'Empty Calendar'", "Empty Calendar", consoleOutput);
  }

  /**
   * Tests displayEvents when the events list is empty.
   * Expect "Empty Calendar".
   */
  @Test
  public void testDisplayEventsEmpty() {
    view.displayEvents(new ArrayList<>());
    String consoleOutput = outputStream.toString().trim();
    assertEquals("Output should be 'Empty Calendar'", "Empty Calendar", consoleOutput);
  }

  /**
   * Tests displayEvents with one event in the list.
   * We mock an IEventModel to provide a toString() output.
   */
  @Test
  public void testDisplayEventsSingleEvent() {
    List<IEventModel> events = new ArrayList<>();
    events.add(new FakeMockTestClass("SingleEvent"));
    view.displayEvents(events);

    String consoleOutput = outputStream.toString().trim();
    // We expect a line containing the event's toString()
    assertEquals("Output should match the event's toString()",
            "MockEvent: SingleEvent", consoleOutput);
  }

  /**
   * Tests displayEvents with multiple events.
   */
  @Test
  public void testDisplayEventsMultipleEvents() {
    List<IEventModel> events = new ArrayList<>();
    events.add(new FakeMockTestClass("Event1"));
    events.add(new FakeMockTestClass("Event2"));
    view.displayEvents(events);

    String consoleOutput = outputStream.toString().trim();
    String[] lines = consoleOutput.split(System.lineSeparator());
    // We expect two lines total
    assertEquals("Should print 2 lines for 2 events", 2, lines.length);
    assertEquals("First line not as expected", "MockEvent: Event1", lines[0]);
    assertEquals("Second line not as expected", "MockEvent: Event2", lines[1]);
  }

  /**
   * A simple mock IEventModel that overrides toString() for testing.
   */
  private static class FakeMockTestClass implements IEventModel {
    private final String name;

    private FakeMockTestClass(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return "MockEvent: " + name;
    }

    // Remaining IEventModel methods (stubs)
    public String printEvent() {
      return null;
    }

    @Override
    public boolean checkEventConflict(IEventModel event) {
      return false;
    }

    @Override
    public java.util.Date getStartDateTime() {
      return null;
    }

    @Override
    public java.util.Date getEndDateTime() {
      return null;
    }

    @Override
    public String getEventName() {
      return null;
    }

    @Override
    public String getLocation() {
      return null;
    }

    @Override
    public String getLongDescription() {
      return null;
    }

    @Override
    public EventStatus getStatus() {
      return null;
    }

  }
}
