package model;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Manages multiple calendar models.
 * Calendars are stored in a map by a unique (case-insensitive) name.
 * Also maintains a reference to the current calendar.
 */
public class CalendarManager {

  /**
   * Map storing calendar models by lowercase calendar name.
   */
  private Map<String, ICalenderModel> calendarMap;

  /**
   * Holds the current calendar in use.
   */
  private ICalenderModel currentCalendar;

  private CalendarModelCallback callback;


  /**
   * Constructs a new CalendarManager with an empty calendar map.
   */
  public CalendarManager() {
    this.calendarMap = new HashMap<>();
    this.currentCalendar = null;
  }

  /**
   * Creates a new calendar with the given name and time zone.
   * The calendar name must be unique.
   *
   * @param calName  the unique calendar name
   * @param timeZone the time zone in "Area/Location" format
   * @return the created ICalenderModel instance
   * @throws InvalidCalenderOperationException if the name already exists
   */
  public ICalenderModel createCalendar(String calName, String timeZone)
          throws InvalidCalenderOperationException {

    String key = calName.toLowerCase();
    if (calendarMap.containsKey(key)) {
      throw new InvalidCalenderOperationException("Calendar with name '"
              + calName + "' already exists.");
    }

    // Validate and convert timezone string
    ZoneId zone;
    try {
      zone = ZoneId.of(timeZone);
    } catch (Exception e) {
      throw new InvalidCalenderOperationException("Invalid time zone: " + timeZone);
    }

    ICalenderModel newCalendar = new CalenderModelImpl(calName, zone);
    calendarMap.put(key, newCalendar);
    currentCalendar = newCalendar;
    System.out.println("Calendar created: calendar manager" + newCalendar);
    if (callback != null) {
      callback.newCalendarAdded();
    }
    return newCalendar;
  }

  /**
   * Retrieves the calendar associated with the given name.
   *
   * @param calName the unique calendar name
   * @return the ICalenderModel instance, or null if not found
   */
  public ICalenderModel getCalendar(String calName) {
    return calendarMap.get(calName.toLowerCase());
  }

  /**
   * Sets the current calendar to the one with the given name.
   *
   * @param calName the calendar name to switch to
   * @throws InvalidCalenderOperationException if the calendar does not exist
   */
  public void useCalendar(String calName)
          throws InvalidCalenderOperationException {
    ICalenderModel cal = getCalendar(calName);
    if (cal == null) {
      throw new InvalidCalenderOperationException(
              "Calendar '" + calName + "' does not exist.");
    }
    currentCalendar = cal;
  }

  /**
   * Returns the current calendar model in use.
   *
   * @return the current ICalenderModel instance, or null if none is set
   */
  public ICalenderModel getCurrentCalendar() {
    return currentCalendar;
  }


  /**
   * Returns the number of calendars managed.
   *
   * @return the count of calendars
   */
  public int getCalendarCount() {
    return calendarMap.size();
  }

  /**
   * Removes the calendar with the given name.
   *
   * @param calName the unique calendar name
   * @return true if a calendar was removed, false otherwise
   */
  public boolean removeCalendar(String calName) {
    String key = calName.toLowerCase();
    ICalenderModel removed = calendarMap.remove(key);
    if (removed != null && removed.equals(currentCalendar)) {
      currentCalendar = null;
    }
    return removed != null;
  }


  /**
   * This method edits the property of a calendar mostly the name and timezone,
   * which is called by controller.
   *
   * @param property  either name or timezone.
   * @param calName   name of the calendar where operation needs to be performed.
   * @param propValue values of the property.
   * @throws InvalidCalenderOperationException Throws exception when something is wrong.
   */
  public void editCalendar(String property, String calName, String propValue)
          throws InvalidCalenderOperationException {
    String prop = property.toLowerCase();
    String oldKey = calName.toLowerCase();
    String newValue = propValue.trim();

    if (!calendarMap.containsKey(oldKey)) {
      throw new InvalidCalenderOperationException("Calendar with name '" +
              calName + "' does not exist.");
    }

    switch (prop) {
      case "name":
        if (newValue.isEmpty()) {
          throw new InvalidCalenderOperationException("New name cannot be empty.");
        }
        editCalendarName(oldKey, newValue);
        break;

      case "timezone":
        if (newValue.isEmpty()) {
          throw new InvalidCalenderOperationException("Timezone cannot be empty.");
        }
        try {
          //System.out.println("STEP 1 - Attempting to parse time zone: " + newValue);
          ZoneId.of(newValue); // validate
          editCalendarTimeZone(oldKey, newValue);
          DateTimeHelper.setDefaultZone(newValue);
        } catch (Exception e) {
          throw new InvalidCalenderOperationException("Invalid time zone: " + newValue);
        }
        break;
      default:
        throw new InvalidCalenderOperationException("Invalid property: " + property);
    }
  }

  /**
   * Edits the calendar name.
   *
   * @param oldName the current calendar name
   * @param newName the new calendar name
   * @throws InvalidCalenderOperationException if the old calendar does not exist or
   *                                           the new name is already used
   */
  private void editCalendarName(String oldName, String newName) throws
          InvalidCalenderOperationException {
    String oldKey = oldName.toLowerCase();
    String newKey = newName.toLowerCase();
    if (!calendarMap.containsKey(oldKey)) {
      throw new InvalidCalenderOperationException("Calendar '" + oldName + "' does not exist.");
    }
    if (calendarMap.containsKey(newKey)) {
      throw new InvalidCalenderOperationException("Calendar with name '" + newName
              + "' already exists.");
    }
    ICalenderModel cal = calendarMap.remove(oldKey);
    // Assuming the calendar model has a setCalendarName method:
    if (cal instanceof CalenderModelImpl) {
      ((CalenderModelImpl) cal).setCalendarName(newName);
    }
    calendarMap.put(newKey, cal);
    // Optionally, update the current calendar reference if needed.
    if (currentCalendar == cal) {
      currentCalendar = cal;
    }
  }

  /**
   * Method to edit calendar timezone.
   *
   * @param calKey   calendar name which has to be edited.
   * @param timeZone timezone to be edited.
   * @throws InvalidCalenderOperationException throws execption when something is wrong.
   */
  private void editCalendarTimeZone(String calKey, String timeZone)
          throws InvalidCalenderOperationException {
    ICalenderModel model = calendarMap.get(calKey.toLowerCase());
    if (model == null) {
      throw new InvalidCalenderOperationException("Invalid calendar or unsupported model type.");
    }

    ZoneId newZone = ZoneId.of(timeZone);

    if (model instanceof CalenderModelImpl) {
      CalenderModelImpl concreteModel = (CalenderModelImpl) model;
      ZoneId oldZone = concreteModel.getZoneId();

      // Step 1: update calendar timezone
      concreteModel.setTimeZone(newZone);

      // Step 2: update each event
      for (IEventModel e : concreteModel.getEvents()) {
        if (e instanceof EventModelImpl) {
          EventModelImpl event = (EventModelImpl) e;
          ZonedDateTime oldStartZdt = event.getStartDateTime().toInstant().atZone(oldZone);
          ZonedDateTime oldEndZdt = (event.getEndDateTime() != null)
                  ? event.getEndDateTime().toInstant().atZone(oldZone)
                  : oldStartZdt;

          ZonedDateTime newStartZdt = oldStartZdt.withZoneSameInstant(newZone);
          ZonedDateTime newEndZdt = oldEndZdt.withZoneSameInstant(newZone);

          event.setStartDateTime(Date.from(newStartZdt.toInstant()));
          event.setEndDateTime(Date.from(newEndZdt.toInstant()));
          event.setZoneId(newZone); // For toString() formatting
        }
      }
    } else {
      throw new InvalidCalenderOperationException("Unsupported calendar type for timezone update.");
    }
  }

  /**
   * Adds calendar to managers map, where hash set is maintained.
   *
   * @param name  name of the calendar.
   * @param model object of calendar model class.
   */
  public void addCalendar(String name, ICalenderModel model) {
    if (!calendarMap.containsKey(name.toLowerCase())) {
      calendarMap.put(name.toLowerCase(), model);
    }
  }

  /**
   * Return a list of names of all available calendars.
   */
  public List<String> getCalendarNameList() {
    return new ArrayList<String>(calendarMap.keySet());
  }

  /**
   * Creates a default calendar on current time zone of the system.
   */
  public ICalenderModel createDefaultCalendar() {
    String defaultCalendarName = "Personal (default)";
    String defaultCalendarTimeZone = TimeZone.getDefault().toZoneId().toString();
    ICalenderModel defaultCalendar = null;
    try {
      defaultCalendar = createCalendar(defaultCalendarName, defaultCalendarTimeZone);
    } catch (model.InvalidCalenderOperationException e) {
      //System.err.println("Error creating calendar: " + e.getMessage());
    }
    return defaultCalendar;
  }

  public void registerCalendarManagerCallback(CalendarModelCallback callback) {
    this.callback = callback;
  }
}
