import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import controller.CalenderControllerImpl;
import model.CalendarManager;
import model.ICalenderModel;
import model.InvalidCalenderOperationException;
import model.IEventModel;
import view.ICalenderView;

/**
 * This test file verifies that the controller properly processes callback events
 * from the calendar model without launching any actual GUI components.
 */
public class ControllerCallbackTest {

  /**
   * A simple dummy view implementation for ICalenderView.
   * We implement the two required methods: displayMessage and displayEvents.
   * displayEvents does nothing here.
   */
  private static class DummyView implements ICalenderView {
    List<String> messages = new ArrayList<>();

    @Override
    public void displayMessage(String message) {
      messages.add(message);
    }

    @Override
    public void displayEvents(List<IEventModel> events) {
      // display events is not required for testing.
    }
  }

  /**
   * Testable version of the controller that overrides callback methods
   * to simply record the parameters passed.
   */
  private static class TestableCalenderControllerImpl extends CalenderControllerImpl {
    public boolean newCalendarAddedCalled = false;
    public int recordedYear = -1;
    public Month recordedMonth = null;

    public TestableCalenderControllerImpl(CalendarManager manager, ICalenderView view,
                                          StringReader input) {
      super(manager, view, input);
    }

    @Override
    public void onMonthChanged(ICalenderModel model, int newYear, Month newMonth) {
      this.recordedYear = newYear;
      this.recordedMonth = newMonth;
    }

    @Override
    public void newCalendarAdded() {
      this.newCalendarAddedCalled = true;
    }
  }

  private CalendarManager manager;
  private DummyView dummyView;
  private StringReader input;

  @Before
  public void setUp() {
    manager = new CalendarManager();
    dummyView = new DummyView();
    input = new StringReader("");
  }

  /**
   * This test verifies that when a new calendar is created, the newCalendarAdded callback
   * is invoked. The controller is registered as the callback listener, and after
   * creating a calendar, the flag recorded in the controller should be true.
   */
  @Test
  public void testNewCalendarAddedCallback() throws InvalidCalenderOperationException {
    // Use the testable controller so that the callback methods record the call.
    TestableCalenderControllerImpl controller =
            new TestableCalenderControllerImpl(manager, dummyView, input);
    // Register the controller as the calendar manager's callback.
    manager.registerCalendarManagerCallback(controller);

    // Create a new calendar.
    controller.createCalendar("TestCal", "America/New_York");

    // Assert that the newCalendarAdded callback was invoked.
    assertTrue("Expected newCalendarAdded callback was not invoked.",
            controller.newCalendarAddedCalled);
  }

  /**
   * This test directly invokes the onMonthChanged callback method on the controller.
   * It simulates a callback with test parameters (year and month) and verifies that the
   * controller records these values.
   */
  @Test
  public void testOnMonthChangedCallbackDirectly() {
    TestableCalenderControllerImpl controller = new
            TestableCalenderControllerImpl(manager, dummyView, input);
    int testYear = 2025;
    Month testMonth = Month.APRIL;
    controller.onMonthChanged(null, testYear, testMonth);

    assertEquals("Recorded year should match test year.",
            testYear, controller.recordedYear);
    assertEquals("Recorded month should match test month.",
            testMonth, controller.recordedMonth);
  }

  /**
   * This test verifies that when the calendar model's month and year are updated,
   * the registered callback (the controller) receives the correct update.
   * The controller is registered with the calendar model, and then we update the model;
   * the controller's recorded year and month should reflect the changes.
   */
  @Test
  public void testOnMonthChangedViaModelUpdate() throws InvalidCalenderOperationException {
    TestableCalenderControllerImpl controller = new
            TestableCalenderControllerImpl(manager, dummyView, input);

    // Create a calendar; the model here is an instance of CalenderModelImpl.
    manager.createCalendar("DummyCal", "America/New_York");
    // Register the testable controller as the model's callback.
    manager.getCurrentCalendar().registerCalendarCallback(controller);

    // Define test values.
    int newYear = 2030;
    Month newMonth = Month.MAY;

    // Update the calendar model's year first (this does not trigger the callback),
    // then update the month, which will trigger onMonthChanged.
    ICalenderModel calendarModel = manager.getCurrentCalendar();
    calendarModel.setCurrentYear(newYear);
    calendarModel.setCurrentMonth(newMonth);

    // Verify that the controller recorded the update.
    assertEquals("Callback recorded year should match the updated year.",
            newYear, controller.recordedYear);
    assertEquals("Callback recorded month should match the updated month.",
            newMonth, controller.recordedMonth);
  }
}
