package model;

import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Provides calendar export, status-check, and event management capabilities.
 */
public interface ICalenderModel {

  /**
   * Returns the name of the calendar.
   *
   * @return the calendar name
   */
  String getCalendarName();

  /**
   * Adds a single event to this calendar, optionally auto-declining conflicts.
   *
   * @param event       the event to add
   * @param autoDecline if true, throw an exception when a conflict occurs
   * @throws InvalidCalenderOperationException if a conflict is found or event is invalid
   */
  void addSingleEvent(IEventModel event, boolean autoDecline)
          throws InvalidCalenderOperationException;

  /**
   * Adds a recurring event for a fixed number of weeks, optionally auto-declining conflicts.
   *
   * @param event       the base event
   * @param autoDecline if true, throw an exception when a conflict occurs
   * @param weekDays    a string of single-letter weekdays (MTWRFSU)
   * @param noOfWeeks   how many weeks to schedule
   * @throws InvalidCalenderOperationException if a conflict is found or event is invalid
   */
  void addRecurringEvent(IEventModel event, boolean autoDecline, String weekDays,
                         int noOfWeeks) throws InvalidCalenderOperationException;

  /**
   * Adds a recurring event until a specified date/time, optionally auto-declining conflicts.
   *
   * @param event       the base event
   * @param autoDecline if true, throw an exception when a conflict occurs
   * @param weekDays    a string of single-letter weekdays (MTWRFSU)
   * @param untilDate   the last date/time to consider (in "yyyy-MM-dd'T'HH:mm" or "yyyy-MM-dd")
   * @throws InvalidCalenderOperationException if a conflict is found or event is invalid
   */
  void addRecurringEvent(IEventModel event, boolean autoDecline, String weekDays,
                         String untilDate) throws InvalidCalenderOperationException;

  /**
   * Searches for events within [fromDateTime, toDateTime).
   *
   * @param fromDateTime start date/time in "yyyy-MM-dd'T'HH:mm" (or "yyyy-MM-dd") format
   * @param toDateTime   end date/time in "yyyy-MM-dd'T'HH:mm" (or "yyyy-MM-dd") format
   * @return a list of matching events
   * @throws InvalidCalenderOperationException if date formats are invalid
   */
  List<IEventModel> searchEvents(String fromDateTime, String toDateTime)
          throws InvalidCalenderOperationException;

  /**
   * Searches for events on a single day or moment.
   *
   * @param dateTime the day or date/time in "yyyy-MM-dd" or
   *                 "yyyy-MM-dd'T'HH:mm" format
   * @return a list of matching events
   * @throws InvalidCalenderOperationException if the format is invalid
   */
  List<IEventModel> searchEvents(String dateTime)
          throws InvalidCalenderOperationException;

  /**
   * Shows the user's availability status on a given date/time. If an event covers that
   * date/time, returns BUSY; otherwise, returns AVAILABLE.
   *
   * @param dateTime the date/time string to check
   * @return BUSY if an event exists at the given time, otherwise AVAILABLE
   * @throws InvalidCalenderOperationException if the format is invalid or an error occurs
   */
  AvailabilityStatus showStatusOn(String dateTime)
          throws InvalidCalenderOperationException;

  /**
   * Edits all events with the given event name.
   *
   * @param property  the property to edit
   * @param eventName the event name (case-insensitive)
   * @param newValue  the new value for the property
   * @throws InvalidCalenderOperationException if no matching events are found
   */
  void editEvent(String property, String eventName, String newValue)
          throws InvalidCalenderOperationException;

  /**
   * Edits all events with the given name that start on or after the given date.
   *
   * @param property      the property to edit
   * @param eventName     the event name (case-insensitive)
   * @param startDateTime the minimum start Date of events to edit
   * @param newValue      the new value for the property
   * @throws InvalidCalenderOperationException if no matching events are found
   */
  void editEvent(String property, String eventName, Date startDateTime,
                 String newValue) throws InvalidCalenderOperationException;

  /**
   * Edits a single event by matching event name, start and end times.
   *
   * @param property      the property to edit
   * @param eventName     the event name (case-insensitive)
   * @param startDateTime the start Date of the event
   * @param endDateTime   the end Date of the event
   * @param newValue      the new value for the property
   * @throws InvalidCalenderOperationException if no matching event is found or update fails
   */
  void editEvent(String property, String eventName, Date startDateTime,
                 Date endDateTime, String newValue)
          throws InvalidCalenderOperationException;

  /**
   * Returns the list of events currently stored in the calendar.
   *
   * @return a list of events
   */
  List<IEventModel> getEvents();

  /**
   * Copies a single event from one calendar to another.
   *
   * @param eventName      the name of the event to copy
   * @param sourceStart    the start time of the event in the source calendar
   * @param targetCalendar the calendar to copy the event to
   * @param targetStart    the start time for the copied event in the target calendar
   * @throws InvalidCalenderOperationException if the event cannot be copied or an issue occurs
   */
  void copySingleEvent(String eventName, String sourceStart,
                       ICalenderModel targetCalendar, String targetStart)
          throws InvalidCalenderOperationException;

  /**
   * Copies all events on a specific day from one calendar to another.
   *
   * @param sourceDay      the day in the source calendar to copy from
   * @param targetCalendar the calendar to copy events to
   * @param targetDay      the day in the target calendar for the copied events
   * @throws InvalidCalenderOperationException if the events cannot be copied
   */
  void copyEventsOnDay(String sourceDay, ICalenderModel targetCalendar,
                       String targetDay) throws InvalidCalenderOperationException;

  /**
   * Copies all events within a date range from one calendar to another.
   *
   * @param fromDate       the start date of the range in "yyyy-MM-dd" format
   * @param toDate         the end date of the range in "yyyy-MM-dd" format
   * @param targetCalendar the calendar to copy events to
   * @param targetBase     the base date for the target calendar
   * @throws InvalidCalenderOperationException if events cannot be copied or dates are invalid
   */
  void copyEventsBetween(String fromDate, String toDate,
                         ICalenderModel targetCalendar, String targetBase)
          throws InvalidCalenderOperationException;

  /**
   * Sets the name of the calendar.
   *
   * @param newName the new calendar name
   */
  void setCalendarName(String newName);

  /**
   * Sets the time zone for the calendar.
   *
   * @param newZoneId the new time zone
   */
  void setTimeZone(ZoneId newZoneId);

  /**
   * Returns the time zone of the calendar.
   *
   * @return the calendar's ZoneId
   */
  ZoneId getZoneId();

  /**
   * Sets the current month for the calendar.
   *
   * @param month the month to set as current
   */
  void setCurrentMonth(Month month);

  /**
   * Sets the current year for the calendar.
   *
   * @param year the year to set as current
   */
  void setCurrentYear(int year);

  /**
   * Returns the current month of the calendar.
   *
   * @return the current month
   */
  Month getCurrentMonth();

  /**
   * Returns the current year of the calendar.
   *
   * @return the current year
   */
  int getCurrentYear();

  /**
   * Returns the current day of the month for the calendar.
   *
   * @return the current day as an int
   */
  int getCurrentDay();

  /**
   * Registers a callback for calendar model changes.
   *
   * @param callback an implementation of CalendarModelCallback to invoke on changes
   */
  void registerCalendarCallback(CalendarModelCallback callback);
}
