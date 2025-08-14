package model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation of ICalenderModel providing calendar management and export
 * capabilities. Uses only (eventName + startDateTime) as the composite key for
 * fast lookups. Now ensures that copied events' zoneId is set to the target
 * calendar's zone.
 */
public class CalenderModelImpl implements ICalenderModel {

  /**
   * Formatter for date/time strings in "yyyy-MM-dd'T'HH:mm" format.
   */
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  // A list of all events (used for range queries).
  private final List<IEventModel> eventList;

  // Hash-based index keyed by (eventName + startDateTime), plus name-based list indexing.
  private final EventIndex eventIndex;

  // The unique time zone for this calendar.
  private ZoneId zoneId;

  // Name of the calendar.
  private String calendarName;

  private Month month;

  private int year;

  private int dayOfWeek;

  private CalendarModelCallback callback;

  /**
   * Constructs a CalenderModelImpl with the specified name and ZoneId.
   *
   * @param name   the calendar name
   * @param zoneId the time zone for this calendar (e.g., ZoneId.of("America/Los_Angeles"))
   */
  public CalenderModelImpl(String name, ZoneId zoneId) {
    this.zoneId = zoneId;
    this.eventList = new ArrayList<>();
    this.eventIndex = new EventIndex();
    this.calendarName = name;
    this.setDayFromDateTimeString(getCurrentDateTime());
    this.setMonthFromDateTimeString(getCurrentDateTime());
    this.setYearFromDateTimeString(getCurrentDateTime());
  }

  /**
   * Returns the calendar name.
   *
   * @return the name of the calendar
   */
  @Override
  public String getCalendarName() {
    return calendarName;
  }

  /**
   * Adds a single event to the calendar.
   *
   * @param event       the event to add
   * @param autoDecline if true, conflict events are auto-declined
   * @throws InvalidCalenderOperationException if event validation fails or there is a conflict
   */
  @Override
  public void addSingleEvent(IEventModel event, boolean autoDecline)
          throws InvalidCalenderOperationException {

    if (event.getEndDateTime() == null) {
      // If no endDate, treat it as an all-day event.
      Date newStart = toStartOfDayLocal(event.getStartDateTime());
      Date newEnd = toEndOfDayLocal(newStart);
      setEventDates(event, newStart, newEnd);
    }

    validateStartEnd(event.getStartDateTime(), event.getEndDateTime());
    if (hasConflict(event)) {
      throw new InvalidCalenderOperationException(
              "There is already a conflicting event --auto Declined");
    }

    eventList.add(event);
    eventIndex.addEvent(event); // Hash-index by (name, start)
  }

  /**
   * Adds a recurring event for a fixed number of weeks.
   *
   * @param baseEvent the event to recur
   * @param autoDecline if true, conflicts lead to auto-decline
   * @param weekDays  a string representing weekdays (e.g., "MTWRF")
   * @param noOfWeeks number of weeks for recurrence
   * @throws InvalidCalenderOperationException if recurrence causes a conflict or error
   */
  @Override
  public void addRecurringEvent(IEventModel baseEvent,
                                boolean autoDecline,
                                String weekDays,
                                int noOfWeeks)
          throws InvalidCalenderOperationException {

    if (noOfWeeks < 1) {
      throw new InvalidCalenderOperationException("Number of weeks must be >= 1.");
    }

    LocalDateTime baseStart = toLocalDateTime(baseEvent.getStartDateTime());
    LocalDateTime baseEnd = (baseEvent.getEndDateTime() != null)
            ? toLocalDateTime(baseEvent.getEndDateTime())
            : baseStart.withHour(23).withMinute(59).withSecond(59);

    Set<DayOfWeek> days = convertWeekDays(weekDays);

    for (int weekOffset = 0; weekOffset < noOfWeeks; weekOffset++) {
      for (DayOfWeek day : days) {
        LocalDateTime newStart = baseStart.with(TemporalAdjusters.nextOrSame(day))
                .plusWeeks(weekOffset);
        LocalDateTime newEnd = baseEnd.with(TemporalAdjusters.nextOrSame(day))
                .plusWeeks(weekOffset);

        IEventModel recurringEvent = EventModelImpl.getBuilder(
                        baseEvent.getEventName(),
                        newStart.format(DATE_TIME_FORMATTER), this.zoneId)
                .endDateString(newEnd.format(DATE_TIME_FORMATTER))
                // Also set zoneId so that the event matches this calendar's zone.
                .zoneId(this.zoneId)
                .build();

        if (hasConflict(recurringEvent)) {
          throw new InvalidCalenderOperationException(
                  "Conflicting event found for recurring event on " + newStart);
        } else {
          eventList.add(recurringEvent);
          eventIndex.addEvent(recurringEvent);
        }
      }
    }
  }

  /**
   * Adds a recurring event until a specified date.
   *
   * @param baseEvent the event to recur
   * @param autoDecline if true, conflicts are auto-declined
   * @param weekDays  a string representing weekdays (e.g., "MTWRF")
   * @param untilDate the end date in "yyyy-MM-dd" or "yyyy-MM-dd'T'HH:mm" format
   * @throws InvalidCalenderOperationException if parsing fails or conflict is found
   */
  @Override
  public void addRecurringEvent(IEventModel baseEvent,
                                boolean autoDecline,
                                String weekDays,
                                String untilDate)
          throws InvalidCalenderOperationException {

    LocalDateTime baseStart = toLocalDateTime(baseEvent.getStartDateTime());
    LocalDateTime baseEnd = (baseEvent.getEndDateTime() != null)
            ? toLocalDateTime(baseEvent.getEndDateTime())
            : baseStart.withHour(23).withMinute(59).withSecond(59);

    // If user gave a date string in YYYY-MM-DD, tack on T23:59.
    if (!untilDate.contains("T")) {
      if (!untilDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
        throw new InvalidCalenderOperationException(
                "Expected until date in format YYYY-MM-DD for all-day events.");
      }
      untilDate += "T23:59";
    }

    LocalDateTime untilLDT;
    try {
      untilLDT = LocalDateTime.parse(untilDate, DATE_TIME_FORMATTER);
    } catch (Exception e) {
      throw new InvalidCalenderOperationException(
              "Invalid date/time format for until date: " + untilDate);
    }

    Set<DayOfWeek> days = convertWeekDays(weekDays);
    int weekOffset = 0;
    boolean addedSomething = true;

    while (addedSomething) {
      addedSomething = false;

      for (DayOfWeek day : days) {
        LocalDateTime newStart = baseStart.with(TemporalAdjusters.nextOrSame(day))
                .plusWeeks(weekOffset);
        if (newStart.isAfter(untilLDT)) {
          continue;
        }

        LocalDateTime newEnd = baseEnd.with(TemporalAdjusters.nextOrSame(day))
                .plusWeeks(weekOffset);
        if (newEnd.isAfter(untilLDT)) {
          newEnd = untilLDT;
        }

        addedSomething = true;
        IEventModel recurringEvent = EventModelImpl.getBuilder(
                        baseEvent.getEventName(),
                        newStart.format(DATE_TIME_FORMATTER), this.zoneId)
                .endDateString(newEnd.format(DATE_TIME_FORMATTER))
                .zoneId(this.zoneId)
                .build();

        if (hasConflict(recurringEvent)) {
          throw new InvalidCalenderOperationException(
                  "Conflicting event found for recurring event on " + newStart);
        } else {
          eventList.add(recurringEvent);
          eventIndex.addEvent(recurringEvent);
        }
      }

      weekOffset++;
    }
  }

  /**
   * Searches for events in the given date/time range.
   *
   * @param fromDateTime the start date/time in "yyyy-MM-dd'T'HH:mm" format
   * @param toDateTime   the end date/time in "yyyy-MM-dd'T'HH:mm" format
   * @return a list of events that occur within the range
   * @throws InvalidCalenderOperationException if parsing fails or an error occurs
   */
  @Override
  public List<IEventModel> searchEvents(String fromDateTime, String toDateTime)
          throws InvalidCalenderOperationException {
    Date fromDate = parseDateLocal(fromDateTime);
    Date toDate = parseDateLocal(toDateTime);
    return filterEventsInRange(fromDate, toDate);
  }

  /**
   * Searches for events on a specific date.
   *
   * @param dateTime the date in "yyyy-MM-dd" or "yyyy-MM-dd'T'HH:mm" format
   * @return a list of events for that date
   * @throws InvalidCalenderOperationException if parsing fails or an error occurs
   */
  @Override
  public List<IEventModel> searchEvents(String dateTime)
          throws InvalidCalenderOperationException {
    Date fromDate = parseDateLocal(dateTime);
    LocalDateTime fromLDT = toLocalDateTime(fromDate);
    LocalDateTime toLDT = fromLDT.plusDays(1);
    Date toDate = Date.from(toLDT.atZone(zoneId).toInstant());
    return filterEventsInRange(fromDate, toDate);
  }

  /**
   * Shows the availability status on a specific date/time.
   *
   * @param dateTime the date/time to check in "yyyy-MM-dd'T'HH:mm" format
   * @return AvailabilityStatus.BUSY if an event exists at that time, otherwise AVAILABLE
   * @throws InvalidCalenderOperationException if parsing fails
   */
  @Override
  public AvailabilityStatus showStatusOn(String dateTime)
          throws InvalidCalenderOperationException {
    Date target = parseDateLocal(dateTime);

    for (IEventModel e : eventList) {
      Date start = e.getStartDateTime();
      Date end = (e.getEndDateTime() != null) ? e.getEndDateTime() : start;
      if (!target.before(start) && !target.after(end)) {
        return AvailabilityStatus.BUSY;
      }
    }
    return AvailabilityStatus.AVAILABLE;
  }

  /**
   * Edits a specific event's property using its name, start, and end times.
   *
   * @param property      the property to modify
   * @param eventName     the event name
   * @param startDateTime the event's start time
   * @param endDateTime   the event's end time
   * @param newValue      the new value for the property
   * @throws InvalidCalenderOperationException if no matching event is found or update fails
   */
  @Override
  public void editEvent(String property, String eventName, Date startDateTime,
                        Date endDateTime, String newValue)
          throws InvalidCalenderOperationException {
    validateStartEnd(startDateTime, endDateTime);
    IEventModel target = eventIndex.getEventByStartKey(eventName, startDateTime);
    if (target == null) {
      throw new InvalidCalenderOperationException(
              "No event found with name + start time.");
    }
    if (endDateTime != null && target.getEndDateTime() != null) {
      if (!target.getEndDateTime().equals(endDateTime)) {
        throw new InvalidCalenderOperationException(
                "No event found with matching end time.");
      }
    }
    if (target instanceof EventModelImpl) {
      EventModelImpl oldSnapshot = EventModelImpl.copyOf((EventModelImpl) target);
      updateEvent((EventModelImpl) target, property, newValue);
      reindexIfNeeded(oldSnapshot, (EventModelImpl) target);
    }
  }

  /**
   * Edits events with the given name starting from a specified date/time.
   *
   * @param property      the property to edit
   * @param eventName     the event name
   * @param startDateTime the start time filter
   * @param newValue      the new value for the property
   * @throws InvalidCalenderOperationException if no matching events are found
   */
  @Override
  public void editEvent(String property, String eventName, Date startDateTime,
                        String newValue)
          throws InvalidCalenderOperationException {
    boolean found = false;
    List<IEventModel> candidates = eventIndex.getEventsByName(eventName);
    for (IEventModel e : candidates) {
      if (!e.getStartDateTime().before(startDateTime) && e instanceof EventModelImpl) {
        EventModelImpl oldSnapshot = EventModelImpl.copyOf((EventModelImpl) e);
        updateEvent((EventModelImpl) e, property, newValue);
        reindexIfNeeded(oldSnapshot, (EventModelImpl) e);
        found = true;
      }
    }
    if (!found) {
      throw new InvalidCalenderOperationException(
              "No matching events found starting from that date/time.");
    }
  }

  /**
   * Edits all events with the given name.
   *
   * @param property  the property to edit
   * @param eventName the event name
   * @param newValue  the new value for the property
   * @throws InvalidCalenderOperationException if no matching events are found
   */
  @Override
  public void editEvent(String property, String eventName, String newValue)
          throws InvalidCalenderOperationException {
    boolean found = false;
    List<IEventModel> candidates = eventIndex.getEventsByName(eventName);
    for (IEventModel e : candidates) {
      if (e instanceof EventModelImpl) {
        EventModelImpl oldSnapshot = EventModelImpl.copyOf((EventModelImpl) e);
        updateEvent((EventModelImpl) e, property, newValue);
        reindexIfNeeded(oldSnapshot, (EventModelImpl) e);
        found = true;
      }
    }
    if (!found) {
      throw new InvalidCalenderOperationException(
              "No matching events found for name: " + eventName);
    }
  }

  /**
   * Copies a single event from the calendar to a target calendar with an adjusted start.
   *
   * @param eventName    the name of the event to copy
   * @param sourceStart  the source start time in "yyyy-MM-dd'T'HH:mm" format
   * @param targetCalendar the target calendar model
   * @param targetStart  the new start time for the copied event
   * @throws InvalidCalenderOperationException if the source event is not found or conflict occurs
   */
  @Override
  public void copySingleEvent(String eventName, String sourceStart,
                              ICalenderModel targetCalendar, String targetStart)
          throws InvalidCalenderOperationException {
    Date sourceStartDate = parseDateLocal(sourceStart);
    IEventModel sourceEvent = eventIndex.getEventByStartKey(eventName, sourceStartDate);
    if (sourceEvent == null) {
      throw new InvalidCalenderOperationException("No event found named '"
              + eventName + "' at start time " + sourceStart);
    }
    Date sEnd = (sourceEvent.getEndDateTime() != null) ? sourceEvent.getEndDateTime()
            : sourceStartDate;
    long durationMs = sEnd.getTime() - sourceStartDate.getTime();
    CalenderModelImpl targetImpl = (CalenderModelImpl) targetCalendar;
    Date targetStartDate = targetImpl.parseDateLocal(targetStart);
    Date newEndDate = new Date(targetStartDate.getTime() + durationMs);
    IEventModel copied = EventModelImpl.getBuilder(sourceEvent.getEventName(),
                    targetImpl.formatDateLocal(targetStartDate), targetImpl.zoneId)
            .endDateString(targetImpl.formatDateLocal(newEndDate))
            .location(sourceEvent.getLocation())
            .longDescription(sourceEvent.getLongDescription())
            .status(sourceEvent.getStatus())
            .zoneId(targetImpl.zoneId)
            .build();
    targetCalendar.addSingleEvent(copied, false);
  }

  /**
   * Copies all events from a specific day to another day in the target calendar.
   *
   * @param sourceDay     the source day (format "yyyy-MM-dd" or with time)
   * @param targetCalendar the target calendar model
   * @param targetDay     the target day (format "yyyy-MM-dd")
   * @throws InvalidCalenderOperationException if the copy operation fails
   */
  @Override
  public void copyEventsOnDay(String sourceDay,
                              ICalenderModel targetCalendar,
                              String targetDay)
          throws InvalidCalenderOperationException {
    Date dayStart = toStartOfDayLocal(parseDateLocal(sourceDay));
    Date dayEnd = toEndOfDayLocal(dayStart);
    List<IEventModel> eventsThisDay = filterEventsInRange(dayStart, dayEnd);
    ZonedDateTime zSrcDayStart = dayStart.toInstant().atZone(this.zoneId);
    CalenderModelImpl targetImpl = (CalenderModelImpl) targetCalendar;
    Date targetDayBase = targetImpl.parseDateLocal(targetDay);
    Date targetDayStart = targetImpl.toStartOfDayLocal(targetDayBase);
    ZonedDateTime zTgtDayStart = targetDayStart.toInstant().atZone(targetImpl.zoneId);
    long dayDiff = java.time.temporal.ChronoUnit.DAYS.between(
            zSrcDayStart.toLocalDate(), zTgtDayStart.toLocalDate());
    for (IEventModel srcEvent : eventsThisDay) {
      ZonedDateTime srcStartZdt = srcEvent.getStartDateTime().toInstant().atZone(this.zoneId);
      ZonedDateTime srcEndZdt = (srcEvent.getEndDateTime() != null)
              ? srcEvent.getEndDateTime().toInstant().atZone(this.zoneId) : srcStartZdt;
      ZonedDateTime plusDaysStart = srcStartZdt.plusDays(dayDiff);
      ZonedDateTime plusDaysEnd = srcEndZdt.plusDays(dayDiff);
      ZonedDateTime finalStartZdt = plusDaysStart.withZoneSameInstant(targetImpl.zoneId);
      ZonedDateTime finalEndZdt = plusDaysEnd.withZoneSameInstant(targetImpl.zoneId);
      String newStartStr = targetImpl.formatZonedDateTime(finalStartZdt);
      String newEndStr = targetImpl.formatZonedDateTime(finalEndZdt);
      IEventModel copied = EventModelImpl.getBuilder(
                      srcEvent.getEventName(), newStartStr, targetImpl.zoneId)
              .endDateString(newEndStr)
              .location(srcEvent.getLocation())
              .longDescription(srcEvent.getLongDescription())
              .status(srcEvent.getStatus())
              .zoneId(targetImpl.zoneId)
              .build();
      targetCalendar.addSingleEvent(copied, false);
    }
  }

  /**
   * Copies events between two dates to a new date range starting at targetBase.
   *
   * @param fromDate      the start date of the source range in "yyyy-MM-dd'T'HH:mm" format
   * @param toDate        the end date of the source range in "yyyy-MM-dd'T'HH:mm" format
   * @param targetCalendar the target calendar model
   * @param targetBase    the start date/time for the copied events in the target calendar
   * @throws InvalidCalenderOperationException if the copy operation fails
   */
  @Override
  public void copyEventsBetween(String fromDate, String toDate,
                                ICalenderModel targetCalendar,
                                String targetBase)
          throws InvalidCalenderOperationException {
    Date fromD = parseDateLocal(fromDate);
    Date toD = parseDateLocal(toDate);
    ZonedDateTime zFrom = fromD.toInstant().atZone(this.zoneId);
    ZonedDateTime zTo = toD.toInstant().atZone(this.zoneId);
    List<IEventModel> matching = new ArrayList<>();
    for (IEventModel e : eventList) {
      Date start = e.getStartDateTime();
      if (!start.before(fromD) && !start.after(toD)) {
        matching.add(e);
      }
    }
    CalenderModelImpl targetImpl = (CalenderModelImpl) targetCalendar;
    Date targetBaseDate = targetImpl.parseDateLocal(targetBase);
    ZonedDateTime zTargetBase = targetBaseDate.toInstant().atZone(targetImpl.zoneId);
    long dayDiff = java.time.temporal.ChronoUnit.DAYS.between(
            zFrom.toLocalDate(), zTargetBase.toLocalDate());
    for (IEventModel srcEvent : matching) {
      ZonedDateTime srcStartZdt = srcEvent.getStartDateTime().toInstant().atZone(this.zoneId);
      ZonedDateTime srcEndZdt = (srcEvent.getEndDateTime() != null)
              ? srcEvent.getEndDateTime().toInstant().atZone(this.zoneId) : srcStartZdt;
      ZonedDateTime plusDaysStart = srcStartZdt.plusDays(dayDiff);
      ZonedDateTime plusDaysEnd = srcEndZdt.plusDays(dayDiff);
      ZonedDateTime finalStartZdt = plusDaysStart.withZoneSameInstant(targetImpl.zoneId);
      ZonedDateTime finalEndZdt = plusDaysEnd.withZoneSameInstant(targetImpl.zoneId);
      String newStartStr = targetImpl.formatZonedDateTime(finalStartZdt);
      String newEndStr = targetImpl.formatZonedDateTime(finalEndZdt);
      IEventModel copied = EventModelImpl.getBuilder(
                      srcEvent.getEventName(), newStartStr, targetImpl.zoneId)
              .endDateString(newEndStr)
              .location(srcEvent.getLocation())
              .longDescription(srcEvent.getLongDescription())
              .status(srcEvent.getStatus())
              .zoneId(targetImpl.zoneId)
              .build();
      targetCalendar.addSingleEvent(copied, false);
    }
  }

  /**
   * Sets the calendar name.
   *
   * @param newName the new calendar name
   */
  @Override
  public void setCalendarName(String newName) {
    this.calendarName = newName;
  }

  /**
   * Returns a copy of the list of events.
   *
   * @return a list of event models in the calendar
   */
  @Override
  public List<IEventModel> getEvents() {
    return new ArrayList<>(eventList);
  }

  /**
   * Returns the ZoneId of this calendar.
   *
   * @return the ZoneId of this calendar
   */
  @Override
  public ZoneId getZoneId() {
    return zoneId;
  }

  /**
   * Converts a Date to a ZonedDateTime in the specified zone.
   *
   * @param date the date to convert
   * @param z the ZoneId to use
   * @return the corresponding ZonedDateTime
   */
  private ZonedDateTime toZonedDateTime(Date date, ZoneId z) {
    return date.toInstant().atZone(z);
  }

  /**
   * Formats a ZonedDateTime as a string in "yyyy-MM-dd'T'HH:mm" format.
   *
   * @param zdt the ZonedDateTime to format
   * @return the formatted string
   */
  String formatZonedDateTime(ZonedDateTime zdt) {
    return zdt.format(DATE_TIME_FORMATTER);
  }

  /**
   * Parses a date/time string in local time to a Date.
   *
   * @param dateString the date/time string to parse
   * @return the corresponding Date object
   * @throws InvalidCalenderOperationException if parsing fails or format is invalid
   */
  protected Date parseDateLocal(String dateString)
          throws InvalidCalenderOperationException {
    if (!dateString.contains("T")) {
      if (!dateString.matches("\\d{4}-\\d{2}-\\d{2}")) {
        throw new InvalidCalenderOperationException(
                "Expected date in format YYYY-MM-DD for all-day events.");
      }
      dateString += "T00:00";
    }
    try {
      LocalDateTime ldt = LocalDateTime.parse(dateString, DATE_TIME_FORMATTER);
      ZonedDateTime zdt = ldt.atZone(this.zoneId);
      return Date.from(zdt.toInstant());
    } catch (Exception e) {
      throw new InvalidCalenderOperationException(
              "Invalid date/time format: " + dateString);
    }
  }

  /**
   * Formats a Date as a string in local time using the formatter.
   *
   * @param date the Date to format
   * @return the formatted string
   */
  String formatDateLocal(Date date) {
    LocalDateTime ldt = toLocalDateTime(date);
    return ldt.format(DATE_TIME_FORMATTER);
  }

  /**
   * Converts a Date to a LocalDateTime in the calendar's zone.
   *
   * @param date the date to convert
   * @return the LocalDateTime representation
   */
  LocalDateTime toLocalDateTime(Date date) {
    return date.toInstant().atZone(this.zoneId).toLocalDateTime();
  }

  /**
   * Converts a Date to the start of its day (00:00:00).
   *
   * @param date the date to process
   * @return the Date representing the start of the day
   */
  Date toStartOfDayLocal(Date date) {
    LocalDateTime ldt = toLocalDateTime(date).withHour(0)
            .withMinute(0).withSecond(0).withNano(0);
    return Date.from(ldt.atZone(this.zoneId).toInstant());
  }

  /**
   * Converts a Date to the end of its day (23:59:59.999999999).
   *
   * @param date the date to process
   * @return the Date representing the end of the day
   */
  Date toEndOfDayLocal(Date date) {
    LocalDateTime ldt = toLocalDateTime(date).withHour(23)
            .withMinute(59).withSecond(59).withNano(999999999);
    return Date.from(ldt.atZone(this.zoneId).toInstant());
  }

  /**
   * Checks if the new event conflicts with any existing event.
   *
   * @param newEvent the event to check
   * @return true if a conflict is found, else false
   */
  private boolean hasConflict(IEventModel newEvent) {
    for (IEventModel existing : eventList) {
      if (existing.checkEventConflict(newEvent)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Filters events that overlap with the given date range.
   *
   * @param fromDate the start date of the range
   * @param toDate   the end date of the range
   * @return a list of events that overlap with the range
   */
  private List<IEventModel> filterEventsInRange(Date fromDate, Date toDate) {
    List<IEventModel> matching = new ArrayList<>();
    for (IEventModel e : eventList) {
      Date eStart = e.getStartDateTime();
      Date eEnd = (e.getEndDateTime() == null) ? eStart : e.getEndDateTime();
      if (eStart.before(toDate) && eEnd.after(fromDate)) {
        matching.add(e);
      }
    }
    return matching;
  }

  /**
   * Converts a string of week day abbreviations to a Set of DayOfWeek.
   *
   * @param weekDays the string representing weekdays (e.g., "MTWRF")
   * @return a Set of DayOfWeek corresponding to the letters
   */
  private static Set<DayOfWeek> convertWeekDays(String weekDays) {
    Set<DayOfWeek> days = new HashSet<>();
    for (char ch : weekDays.toUpperCase().toCharArray()) {
      switch (ch) {
        case 'M':
          days.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          days.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          days.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          days.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          days.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          days.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          days.add(DayOfWeek.SUNDAY);
          break;
        default:
          // ignore invalid letters
      }
    }
    return days;
  }

  /**
   * Validates that the start and end dates are in the correct order.
   *
   * @param start the start date/time
   * @param end   the end date/time
   * @throws InvalidCalenderOperationException if end is before start
   */
  private void validateStartEnd(Date start, Date end)
          throws InvalidCalenderOperationException {
    if (start == null || end == null) {
      return;
    }
    if (end.before(start)) {
      throw new InvalidCalenderOperationException(
              "Event end date/time (" + end + ") cannot be "
                      + "before start date/time (" + start + ").");
    }
  }

  /**
   * Updates the given event's property to the new value.
   *
   * @param event    the event to update
   * @param property the property name to update
   * @param newValue the new value for the property
   * @throws InvalidCalenderOperationException if the property is invalid
   */
  static void updateEvent(EventModelImpl event, String property, String newValue)
          throws InvalidCalenderOperationException {
    String prop = property.trim().toLowerCase();
    switch (prop) {
      case "eventname":
      case "name":
        if (newValue.trim().isEmpty()) {
          throw new InvalidCalenderOperationException("Event name cannot be empty.");
        }
        event.eventName = newValue;
        break;
      case "location":
        event.location = newValue;
        break;
      case "description":
        event.longDescription = newValue;
        break;
      case "status":
        try {
          event.status = EventStatus.valueOf(newValue.toUpperCase());
        } catch (IllegalArgumentException ex) {
          throw new InvalidCalenderOperationException("Invalid status value: " + newValue);
        }
        break;
      case "startdatetime":
        if (newValue.trim().isEmpty()) {
          throw new InvalidCalenderOperationException("Start date/time cannot be empty.");
        }
        Date newStart = DateTimeHelper.parseDate(newValue);
        if (event.endDateTime != null && newStart.after(event.getEndDateTime())) {
          throw new InvalidCalenderOperationException(
                  "Start date/time cannot be after end date/time.");
        }
        event.startDateTime = newStart;
        break;
      case "enddatetime":
        Date newEnd = DateTimeHelper.parseDate(newValue);
        if (event.startDateTime != null && newEnd.before(event.startDateTime)) {
          throw new InvalidCalenderOperationException(
                  "End date/time cannot be before start date/time.");
        }
        event.endDateTime = newEnd;
        break;
      default:
        throw new InvalidCalenderOperationException("Invalid property: " + property);
    }
  }

  /**
   * Reindexes the event in the index if its key has changed.
   *
   * @param oldEventState the original event state
   * @param updatedEvent  the updated event
   */
  private void reindexIfNeeded(EventModelImpl oldEventState, EventModelImpl updatedEvent) {
    boolean keyChanged =
            !Objects.equals(oldEventState.eventName, updatedEvent.eventName) ||
                    !Objects.equals(oldEventState.startDateTime, updatedEvent.startDateTime);
    if (keyChanged) {
      eventIndex.reindexEvent(oldEventState, updatedEvent);
    }
  }

  /**
   * Sets the start and end dates for an event.
   *
   * @param event    the event to update
   * @param newStart the new start date/time
   * @param newEnd   the new end date/time
   */
  private static void setEventDates(IEventModel event, Date newStart, Date newEnd) {
    EventModelImpl base = (EventModelImpl) event;
    base.setStartDateTime(newStart);
    base.setEndDateTime(newEnd);
  }

  /**
   * Sets the time zone for the calendar.
   *
   * @param newZoneId the new ZoneId to set
   * @throws IllegalArgumentException if newZoneId is null
   */
  @Override
  public void setTimeZone(ZoneId newZoneId) {
    if (newZoneId == null) {
      throw new IllegalArgumentException("Time zone cannot be null.");
    }
    this.zoneId = newZoneId;
  }

  /**
   * Gets the current date/time formatted as a string in the calendar's zone.
   *
   * @return the formatted current date/time string
   */
  private String getCurrentDateTime() {
    try {
      ZonedDateTime zonedDateTime = ZonedDateTime.now(getZoneId());
      return zonedDateTime.format(DATE_TIME_FORMATTER);
    } catch (java.time.zone.ZoneRulesException e) {
      return "Invalid Zone ID";
    }
  }

  /**
   * Sets the current month from a date/time string.
   *
   * @param dateTimeString the date/time string to parse
   */
  private void setMonthFromDateTimeString(String dateTimeString) {
    try {
      ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeString,
              DATE_TIME_FORMATTER.withZone(ZoneId.systemDefault()));
      this.month = zonedDateTime.getMonth();
    } catch (DateTimeParseException e) {
      System.err.println("Error parsing date/time string: " + e.getMessage());
      this.month = null;
    }
  }

  /**
   * Sets the current day (day of month) from a date/time string.
   *
   * @param dateTimeString the date/time string to parse
   */
  private void setDayFromDateTimeString(String dateTimeString) {
    try {
      ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeString,
              DATE_TIME_FORMATTER.withZone(ZoneId.systemDefault()));
      this.dayOfWeek = zonedDateTime.getDayOfMonth();
    } catch (DateTimeParseException e) {
      System.err.println("Error parsing date/time string: " + e.getMessage());
      dayOfWeek = -1;
    }
  }

  /**
   * Sets the current year from a date/time string.
   *
   * @param dateTimeString the date/time string to parse
   */
  private void setYearFromDateTimeString(String dateTimeString) {
    try {
      ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeString,
              DATE_TIME_FORMATTER.withZone(ZoneId.systemDefault()));
      this.year = zonedDateTime.getYear();
    } catch (DateTimeParseException e) {
      System.err.println("Error parsing date/time string: " + e.getMessage());
      this.year = -1;
    }
  }

  /**
   * Sets the current month and triggers a callback.
   *
   * @param month the month to set
   */
  @Override
  public void setCurrentMonth(Month month) {
    this.month = month;
    callback.onMonthChanged(this, this.year, this.month);
  }

  /**
   * Sets the current year.
   *
   * @param year the new year to set
   */
  @Override
  public void setCurrentYear(int year) {
    System.out.println("year set to " + year);
    this.year = year;
  }

  /**
   * Returns the current month of the calendar.
   *
   * @return the current Month
   */
  @Override
  public Month getCurrentMonth() {
    return this.month;
  }

  /**
   * Returns the current year of the calendar.
   *
   * @return the current year as an int
   */
  @Override
  public int getCurrentYear() {
    return this.year;
  }

  /**
   * Returns the current day of the month.
   *
   * @return the day of month as an int
   */
  @Override
  public int getCurrentDay() {
    return this.dayOfWeek;
  }

  /**
   * Registers a callback for calendar model updates.
   *
   * @param callback the callback to register
   */
  @Override
  public void registerCalendarCallback(CalendarModelCallback callback) {
    this.callback = callback;
  }
}
