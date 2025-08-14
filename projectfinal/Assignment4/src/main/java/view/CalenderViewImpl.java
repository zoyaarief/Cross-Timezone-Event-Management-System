package view;

import model.IEventModel;
import java.util.List;

/**
 * Implements ICalenderView to display messages and event lists on console.
 */
public class CalenderViewImpl implements ICalenderView {

  /**
   * Displays the provided message to the console.
   * @param message the message to display
   */
  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  /**
   * Displays the list of events to the console.
   * If the list is null or empty, "Empty Calendar" is printed.
   * @param events the list of events to display
   */
  @Override
  public void displayEvents(List<IEventModel> events) {
    if (events == null || events.isEmpty()) {
      System.out.println("Empty Calendar");
    } else {
      for (IEventModel event : events) {
        System.out.println(event.toString());
      }
    }
  }

}
