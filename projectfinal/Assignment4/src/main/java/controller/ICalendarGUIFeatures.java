package controller;


import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import model.IEventModel;

/**
 * This interface lists all the high-level user actions that the GUI (or any UI)
 * can invoke on the application. The concrete controller implements this interface,
 * thus allowing the view to remain ignorant of the controller's actual class.
 */
public interface ICalendarGUIFeatures {

  /**
   * User wants to move to the next month in the currently active calendar.
   */
  void goToNextMonth();

  /**
   * User wants to move to the previous month in the currently active calendar.
   */
  void goToPreviousMonth();

  /**
   * Get current calendars timezone.
   */
  ZoneId getTimeZone();

  /**
   * User wants to switch to a different calendar, by name.
   *
   * @param calName the unique name of the calendar
   */
  void useCalendar(String calName);

  void createCalendar(String calName, String timeZone);

  /**
   * User wants to create a single event.
   */
  void addEventToCalender(IEventModel event, boolean autoDecline);

  /**
   * User wants to create a recurring event (for a fixed number of weeks).
   */
  void addRecurringEventToCalender(IEventModel event,
                                   boolean autoDecline,
                                   String weekDays,
                                   int noOfWeeks);

  /**
   * User wants to create a recurring event (until a given date).
   */
  void addRecurringEventToCalender(IEventModel event,
                                   boolean autoDecline,
                                   String weekDays,
                                   String untilDateTime);

  List<IEventModel> getEventsForDay(String dateTime);

  void editEvent(String property, String eventName,
                 Date startDateTime, Date endDateTime,
                 String propertyValue);

  void editEvent(String property, String eventName,
                 Date startDateTime, String propertyValue);

  void editEvent(String property, String eventName, String propertyValue);

  void exportCalendar(String fileName);

  void importCalendar(String fileName);
}
