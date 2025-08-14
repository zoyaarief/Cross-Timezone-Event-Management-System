package view;

import model.IEventModel;
import java.util.List;

/**
 * Interface for the Calendar View.
 * Provides methods to display messages and events.
 */
public interface ICalenderView {
  /**
   * Display a plain text message to the user.
   * @param message the message to display
   */
  void displayMessage(String message);

  /**
   * Display a list of events.
   * @param events the list of events to display
   */
  void displayEvents(List<IEventModel> events);
}
