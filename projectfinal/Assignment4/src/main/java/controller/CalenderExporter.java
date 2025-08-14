package controller;

import java.util.List;
import model.IEventModel;
import model.InvalidCalenderOperationException;

/**
 * Defines a strategy for exporting calendar data.
 */
public interface CalenderExporter {
  /**
   * Exports the given list of events to a file.
   *
   * @param events the list of events to export
   * @param filename the target file name
   * @return the absolute path of the exported file
   * @throws InvalidCalenderOperationException if export fails
   */
  String exportCalendar(List<IEventModel> events, String filename)
          throws InvalidCalenderOperationException;
}
