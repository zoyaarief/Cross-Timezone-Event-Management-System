package controller;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JComboBox;

import model.AvailabilityStatus;
import model.CalendarManager;
import model.CalendarModelCallback;
import model.EventModelImpl;
import model.ICalenderModel;
import model.IEventModel;
import model.InvalidCalenderOperationException;
import view.GUICalendarViewImpl;
import view.ICalenderView;
import view.IGUICalendarView;

/**
 * Controller for calendar operations. Uses CalendarManager to handle multiple
 * calendars and the "current" calendar.
 */
public class CalenderControllerImpl implements ICalenderController, ICalendarGUIFeatures,
        CalendarModelCallback {

  private CalendarManager calenderManager; // The manager that stores multiple calendars
  private ICalenderView view;              // Used to display messages/results
  private final Scanner scan;
  private final InputParserHelper inputParserHelper;
  private CalenderExporter exporter;

  private static final Color[] CALENDAR_COLORS = {
    new Color(255, 182, 193),
    new Color(255, 228, 181),  // Pastel Orange
    new Color(255, 255, 204),  // Pastel Yellow
    new Color(216, 191, 216),  // Pastel Purple (Thistle)
    new Color(230, 230, 250),  // Pastel Lavender
    new Color(175, 238, 238),  // Pastel Turquoise
    new Color(255, 218, 185),  // Pastel Peach
    new Color(189, 252, 201),  // Pastel Mint
    new Color(255, 228, 225),  // Misty Rose (Pastel Red)
    new Color(238, 221, 255),  // Pastel Violet
    new Color(224, 255, 255),  // Pastel Cyan
    new Color(204, 255, 204),  // Pastel Lime
    new Color(255, 203, 164),  // Pastel Apricot
    new Color(211, 211, 211),  // Pastel Gray
    new Color(255, 182, 150),  // Pastel Salmon
    new Color(159, 226, 191),  // Pastel Seafoam
    new Color(204, 204, 255),  // Pastel Periwinkle
    new Color(224, 176, 255)   // Pastel Mauve
  };

  private Map<String, Color> calendarColorMap = new HashMap<>();
  private int nextColorIndex = 0;

  /**
   * Constructs the calendar controller.
   */
  public CalenderControllerImpl(CalendarManager calenderManager,
                                ICalenderView view,
                                Readable in) {
    this.calenderManager = calenderManager;
    this.scan = new Scanner(in);
    this.view = view;
    this.inputParserHelper = new InputParserHelper(this);
    this.exporter = new CSVCalenderExporter();
  }

  /**
   * Constructs overloaded calendar controller.
   *
   * @param calenderManager the CalendarManager that tracks multiple calendars
   * @param in              the input stream for commands
   */
  public CalenderControllerImpl(CalendarManager calenderManager,
                                Readable in) {
    this.calenderManager = calenderManager;
    this.scan = new Scanner(in);
    this.inputParserHelper = new InputParserHelper(this);
    this.exporter = new CSVCalenderExporter();
  }

  /**
   * Sets the view for the calendar controller. If the view is a GUI view.
   * Configure calendar and navigation settings.
   *
   * @param v the calendar view to set
   */
  public void setView(ICalenderView v) {
    if (v instanceof IGUICalendarView) {
      //configureButtonListener((IGUICalendarView) view );
      calenderManager.createDefaultCalendar();
      this.view = (IGUICalendarView) v;
      configureDefaultCalendar((IGUICalendarView) view);
      ((GUICalendarViewImpl) view).setNavPanelFeatures(this);
      ((GUICalendarViewImpl) view).setMonthPanelFeatures(this,
              calenderManager.getCurrentCalendar().getCurrentYear(),
              calenderManager.getCurrentCalendar().getCurrentMonth().getValue());
    } else {
      this.view = v;
    }
  }

  /**
   * Executes the main loop. Displays a welcome message if no calendar exists,
   * then processes commands until "exit" is entered.
   */
  @Override
  public void execute() {
    displayWelcomeMessage();
    String inputCommand = scan.nextLine();
    while (!inputCommand.equalsIgnoreCase("exit")) {
      try {
        inputParserHelper.parseInputCommand(inputCommand);
      } catch (InvalidCalenderOperationException e) {
        view.displayMessage("Error: " + e.getMessage());
      }
      if (scan.hasNextLine()) {
        inputCommand = scan.nextLine();
      } else {
        break;
      }
    }
  }

  /**
   * Creates a new calendar with the given name and time zone. Only unique names are
   * allowed.
   *
   * @param calName  the unique calendar name
   * @param timeZone the time zone in "Area/Location" format
   * @throws InvalidCalenderOperationException if a calendar with the same name exists
   */
  public void createCalendar(String calName, String timeZone) {
    try {
      calenderManager.createCalendar(calName, timeZone);
      if (!calendarColorMap.containsKey(calName)) {
        calendarColorMap.put(calName, CALENDAR_COLORS[nextColorIndex]);
        nextColorIndex = (nextColorIndex + 1) % CALENDAR_COLORS.length;
      }
      view.displayMessage("Calendar created: " + calName);
    } catch (InvalidCalenderOperationException e) {
      view.displayMessage("Error: " + e.getMessage());
    }
  }

  /**
   * Switches the manager to use the specified calendar as the "current" calendar.
   *
   * @param calName the name of the calendar to use
   * @throws InvalidCalenderOperationException if no such calendar exists
   */
  @Override
  public void useCalendar(String calName) {
    try {
      calenderManager.useCalendar(calName);
      if (this.view instanceof GUICalendarViewImpl) {
        calenderManager.getCurrentCalendar().registerCalendarCallback(this);
        Color chosenColor = calendarColorMap.getOrDefault(calName, Color.WHITE);
        ((GUICalendarViewImpl) view).getMainFrameHelper().getMonthPanel().
                updateBlankCellBackground(chosenColor);
      }
      view.displayMessage("Now using calendar: " + calName);
    } catch (InvalidCalenderOperationException e) {
      view.displayMessage("Error: " + e.getMessage());
    }
  }

  /**
   * Adds a single event to the active calendar.
   */
  @Override
  public void addEventToCalender(IEventModel event, boolean autoDecline) {
    try {
      ICalenderModel current = getActiveCalendarOrFail();
      current.addSingleEvent(event, autoDecline);
      view.displayMessage("Event added: " + event.getEventName());
    } catch (InvalidCalenderOperationException e) {
      view.displayMessage("Error: " + e.getMessage());
    }
  }

  /**
   * Adds a recurring event for a fixed number of weeks in the active calendar.
   *
   * @param event       the event to add
   * @param autoDecline true if conflicts should auto-decline
   * @param weekDays    the days of the week for recurrence
   * @param noOfWeeks   the number of weeks the event recurs
   */
  @Override
  public void addRecurringEventToCalender(IEventModel event,
                                          boolean autoDecline,
                                          String weekDays,
                                          int noOfWeeks) {
    try {
      ICalenderModel current = getActiveCalendarOrFail();
      current.addRecurringEvent(event, autoDecline, weekDays, noOfWeeks);
      view.displayMessage("Recurring event added: " + event.getEventName());
    } catch (InvalidCalenderOperationException e) {
      view.displayMessage("Error: " + e.getMessage());
    }
  }


  /**
   * Adds a recurring event until a specific date in the active calendar.
   *
   * @param event         the event to add
   * @param autoDecline   true if conflicts should auto-decline
   * @param weekDays      the days of the week for recurrence
   * @param untilDateTime the end date/time for the recurrence
   */
  @Override
  public void addRecurringEventToCalender(IEventModel event,
                                          boolean autoDecline,
                                          String weekDays,
                                          String untilDateTime) {
    try {
      ICalenderModel current = getActiveCalendarOrFail();
      current.addRecurringEvent(event, autoDecline, weekDays, untilDateTime);
      view.displayMessage("Recurring event added (until " + untilDateTime + "): "
              + event.getEventName());
    } catch (InvalidCalenderOperationException e) {
      view.displayMessage("Error: " + e.getMessage());
    }
  }

  /**
   * Retrieves events for a specific day/time from the active calendar.
   *
   * @param dateTime the date/time to search events for
   * @return a list of event models for that day/time
   */
  @Override
  public List<IEventModel> getEventsForDay(String dateTime) {
    List<IEventModel> eventSearchList = null;
    try {
      ICalenderModel current = getActiveCalendarOrFail();
      eventSearchList = current.searchEvents(dateTime);
    } catch (InvalidCalenderOperationException e) {
      view.displayMessage("Error: " + e.getMessage());
    }
    return eventSearchList;
  }

  /**
   * Prints events in the given date/time range from the active calendar.
   *
   * @param fromDateTime the start date/time
   * @param toDateTime   the end date/time
   * @throws InvalidCalenderOperationException if the search fails
   */
  protected void printEvents(String fromDateTime, String toDateTime)
          throws InvalidCalenderOperationException {
    ICalenderModel current = getActiveCalendarOrFail();
    List<IEventModel> eventSearchList = current.searchEvents(fromDateTime, toDateTime);
    view.displayEvents(eventSearchList);
  }

  /**
   * Prints events on a specific date in the active calendar.
   *
   * @param dateTime the date/time for which events are printed
   * @throws InvalidCalenderOperationException if the search fails
   */
  protected void printEvents(String dateTime)
          throws InvalidCalenderOperationException {
    ICalenderModel current = getActiveCalendarOrFail();
    List<IEventModel> eventSearchList = current.searchEvents(dateTime);
    view.displayEvents(eventSearchList);
  }

  /**
   * Exports the current calendar's events to a file.
   */
  @Override
  public void exportCalendar(String fileName) {
    try {
      ICalenderModel current = getActiveCalendarOrFail();
      List<IEventModel> events = current.getEvents();
      String path = exporter.exportCalendar(events, fileName);
      view.displayMessage("Calendar exported to: " + path);
    } catch (InvalidCalenderOperationException e) {
      view.displayMessage("Error: " + e.getMessage());
    }
  }

  /**
   * Imports events from a CSV file. The CSV file must have a .csv extension and be
   * formatted as follows. Where the date times are in the format "yyyy-MM-dd'T'HH:mm".
   * Any rows that do not
   * meet these criteria are skipped.
   *
   * @param filePath the path to the CSV file
   */
  @Override
  public void importCalendar(String filePath) {
    // Check for a proper .csv extension.
    if (filePath == null || !filePath.toLowerCase().endsWith(".csv")) {
      view.displayMessage("Error: File must have a .csv extension.");
      throw new IllegalArgumentException("File must have a .csv extension.");
    }

    int importedCount = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = br.readLine()) != null) {
        // Skip empty lines.
        if (line.trim().isEmpty()) {
          continue;
        }

        // Split the line into tokens (assuming a simple comma-delimited CSV).
        String[] tokens = line.split(",");

        // Check that we have at least 6 columns.
        if (tokens.length < 6) {
          view.displayMessage("Skipping invalid row: " + line);
          continue;
        }

        // Skip header row if the first token equals "Subject" (case-insensitive).
        if (tokens[0].trim().equalsIgnoreCase("Subject")) {
          continue;
        }

        // Extract the relevant fields.
        String eventName = tokens[0].trim();
        String startDate = tokens[1].trim(); // Expected format: YYYY-MM-DD
        String startTime = tokens[2].trim(); // Expected format: HH:mm (can be empty)
        String endDate = tokens[3].trim(); // Expected format: YYYY-MM-DD
        String endTime = tokens[4].trim(); // Expected format: HH:mm (can be empty)
        String allDay = tokens[5].trim(); // "True" or "False"

        String startDateTimeStr;
        String endDateTimeStr;
        if (allDay.equalsIgnoreCase("True")) {
          // For all-day events, use only the dates.
          startDateTimeStr = startDate;
          endDateTimeStr = endDate;
        } else {
          // For non all-day events, join the date and time.
          if (startTime.isEmpty()) {
            startTime = "00:00";
          }
          if (endTime.isEmpty()) {
            endTime = "11:59";
          }
          startDateTimeStr = startDate + "T" + startTime;
          endDateTimeStr = endDate + "T" + endTime;
        }

        try {
          // Build the event using your EventModelImpl builder.
          IEventModel newEvent = EventModelImpl.getBuilder(eventName, startDateTimeStr,
                          getTimeZone())
                  .endDateString(endDateTimeStr)
                  .build();
          addEventToCalender(newEvent, true);
          importedCount++;
        } catch (InvalidCalenderOperationException | IllegalArgumentException ex) {
          view.displayMessage("Error parsing event from row: " + line + ". Error: "
                  + ex.getMessage());
        }
      }
      view.displayMessage("Imported " + importedCount + " events from " + filePath);
    } catch (IOException e) {
      view.displayMessage("Error reading CSV file: " + e.getMessage());
    }
  }

  /**
   * Shows the user's availability status on a specific date/time in the active calendar.
   *
   * @param dateTime the date/time to check availability for
   * @throws InvalidCalenderOperationException if the operation fails
   */
  protected void showStatusOn(String dateTime)
          throws InvalidCalenderOperationException {
    ICalenderModel current = getActiveCalendarOrFail();
    AvailabilityStatus status = current.showStatusOn(dateTime);
    if (status == AvailabilityStatus.BUSY) {
      view.displayMessage("User is BUSY at " + dateTime);
    } else {
      view.displayMessage("User is AVAILABLE at " + dateTime);
    }
  }

  /**
   * The method tells the model to edit calendar properties like name and timezone.
   */
  public void editCalendar(String property, String calName, String newValue)
          throws InvalidCalenderOperationException {

    calenderManager.editCalendar(property, calName, newValue);
    view.displayMessage("Calendar edited: " + calName);
  }

  /**
   * Edits a single event in the active calendar by matching its name, start, and end times,
   * then updating the specified property with a new value.
   */
  @Override
  public void editEvent(String property, String eventName,
                        Date startDateTime, Date endDateTime,
                        String propertyValue) {
    try {
      ICalenderModel current = getActiveCalendarOrFail();
      current.editEvent(property, eventName, startDateTime, endDateTime, propertyValue);
    } catch (InvalidCalenderOperationException e) {
      view.displayMessage("Error: " + e.getMessage());
    }
  }

  /**
   * Edits all events with the given name that start on or after the given date.
   */
  @Override
  public void editEvent(String property, String eventName,
                        Date startDateTime, String propertyValue) {
    try {
      ICalenderModel current = getActiveCalendarOrFail();
      current.editEvent(property, eventName, startDateTime, propertyValue);
    } catch (InvalidCalenderOperationException e) {
      view.displayMessage("Error: " + e.getMessage());
    }
  }

  /**
   * Edits all events with the given event name in the active calendar.
   */
  @Override
  public void editEvent(String property, String eventName, String propertyValue) {
    try {
      ICalenderModel current = getActiveCalendarOrFail();
      current.editEvent(property, eventName, propertyValue);
    } catch (InvalidCalenderOperationException e) {
      view.displayMessage("Error: " + e.getMessage());
    }
  }

  /**
   * Displays a welcome message if no calendars exist. Called at the beginning of execute().
   */
  protected void displayWelcomeMessage() {
    if (calenderManager.getCalendarCount() == 0) {
      view.displayMessage("Welcome to Calender App, please start by creating a new calendar");
    }
  }

  /**
   * Returns the CalendarManager instance.
   */
  public CalendarManager getCalendarManager() {
    return this.calenderManager;
  }

  /**
   * Returns the view used for displaying messages.
   */
  public ICalenderView getView() {
    return this.view;
  }

  /**
   * Retrieves the active calendar or throws an exception if none is active.
   */
  private ICalenderModel getActiveCalendarOrFail() throws InvalidCalenderOperationException {
    ICalenderModel current = calenderManager.getCurrentCalendar();
    if (current == null) {
      throw new InvalidCalenderOperationException(
              "No active calendar selected. Use 'use calendar --name <calName>' first."
      );
    }
    return current;
  }

  /**
   * Copy a single event (by name + start) from the current calendar (calenderModel) to.
   * The target calendar named targetCalName, adjusting its start to targetStart.
   *
   * @param eventName   the name of the event to copy
   * @param sourceStart the source start date/time of the event
   * @param targetCalName the target calendar name
   * @param targetStart the target start date/time for the event copy
   * @throws InvalidCalenderOperationException if the copy operation fails
   */
  protected void copySingleEvent(String eventName, String sourceStart,
                                 String targetCalName, String targetStart)
          throws InvalidCalenderOperationException {

    // Look up the target calendar in the CalendarManager
    ICalenderModel targetCalendar = calenderManager.getCalendar(targetCalName);
    if (targetCalendar == null) {
      throw new InvalidCalenderOperationException(
              "Target calendar not found: " + targetCalName);
    }
    ICalenderModel current = getActiveCalendarOrFail();

    // Now call the model's copySingleEvent method
    current.copySingleEvent(eventName, sourceStart, targetCalendar, targetStart);

    view.displayMessage("Copied event \"" + eventName + "\" from " + sourceStart
            + " to " + targetStart + " in calendar " + targetCalName);
  }

  /**
   * Copy all events on 'sourceDay' from the current calendar to 'targetDay' in the
   * target calendar named targetCalName.
   *
   * @param sourceDay   the source day for events copy
   * @param targetCalName the target calendar name
   * @param targetDay   the target day for the events
   * @throws InvalidCalenderOperationException if the copy operation fails
   */
  protected void copyEventsOnDay(String sourceDay,
                                 String targetCalName,
                                 String targetDay)
          throws InvalidCalenderOperationException {

    ICalenderModel targetCalendar = calenderManager.getCalendar(targetCalName);
    if (targetCalendar == null) {
      throw new InvalidCalenderOperationException(
              "Target calendar not found: " + targetCalName);
    }
    ICalenderModel current = getActiveCalendarOrFail();

    current.copyEventsOnDay(sourceDay, targetCalendar, targetDay);

    view.displayMessage("Copied all events on " + sourceDay + " to " + targetDay
            + " in calendar " + targetCalName);
  }

  /**
   * Copy all events in [fromDate, toDate] (inclusive) from the current calendar to a new
   * date range starting at 'targetBase' in the target calendar named targetCalName.
   *
   * @param fromDate    the start date of the source events range
   * @param toDate      the end date of the source events range
   * @param targetCalName the target calendar name
   * @param targetBase  the start date/time of the new range in the target calendar
   * @throws InvalidCalenderOperationException if the copy operation fails
   */
  protected void copyEventsBetween(String fromDate, String toDate,
                                   String targetCalName, String targetBase)
          throws InvalidCalenderOperationException {

    ICalenderModel targetCalendar = calenderManager.getCalendar(targetCalName);
    if (targetCalendar == null) {
      throw new InvalidCalenderOperationException(
              "Target calendar not found: " + targetCalName);
    }
    ICalenderModel current = getActiveCalendarOrFail();

    current.copyEventsBetween(fromDate, toDate, targetCalendar, targetBase);

    view.displayMessage("Copied events between " + fromDate + " and " + toDate
            + " to " + targetBase + " in calendar " + targetCalName);
  }

  /**
   * Configures the default calendar settings for the GUI view.
   *
   * @param calendarView the GUI calendar view to configure
   */
  private void configureDefaultCalendar(IGUICalendarView calendarView) {
    calenderManager.getCurrentCalendar().registerCalendarCallback(this);
    calenderManager.registerCalendarManagerCallback(this);
    System.out.println("Callback Registered");
    Month month = calenderManager.getCurrentCalendar().getCurrentMonth();
    int year = calenderManager.getCurrentCalendar().getCurrentYear();
    int day = calenderManager.getCurrentCalendar().getCurrentDay();
    calendarView.getMainFrameHelper().getNavPanel().setMonthLabel(month.name(), year);
    calendarView.getMainFrameHelper().getMonthPanel().updateMonth(year,
            month.getValue(), day);
    updateCalendarList(calendarView);
  }

  /**
   * Updates the calendar list in the GUI view and assigns colors to new calendars.
   *
   * @param calendarView the GUI calendar view to update
   */
  private void updateCalendarList(IGUICalendarView calendarView) {
    List<String> calendarList = calenderManager.getCalendarNameList();
    JComboBox<String> calendarSelector = calendarView.getMainFrameHelper()
            .getNavPanel().getCalendarSelector();

    for (String calendarName : calendarList) {
      // If the calendar is not yet mapped, assign the next color.
      if (!calendarColorMap.containsKey(calendarName)) {
        calendarColorMap.put(calendarName, CALENDAR_COLORS[nextColorIndex]);
        nextColorIndex = (nextColorIndex + 1) % CALENDAR_COLORS.length;
      }
      // Add the calendar to the selector if not already present.
      boolean exists = false;
      for (int i = 0; i < calendarSelector.getItemCount(); i++) {
        if (calendarSelector.getItemAt(i).equals(calendarName)) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        calendarSelector.addItem(calendarName);
      }
    }
    calendarSelector.repaint();
    calendarSelector.revalidate();
  }

  /**
   * Advances the active calendar to the next month and updates the GUI view.
   */
  @Override
  public void goToNextMonth() {
    Month month = calenderManager.getCurrentCalendar().getCurrentMonth();
    int currentYear = calenderManager.getCurrentCalendar().getCurrentYear();
    Month nextMonth = month.plus(1);
    int nextYear = currentYear;
    if (nextMonth == Month.JANUARY) {
      nextYear++;
      calenderManager.getCurrentCalendar().setCurrentYear(nextYear);
    }
    calenderManager.getCurrentCalendar().setCurrentMonth(nextMonth);
    ((GUICalendarViewImpl) view).setMonthPanelFeatures(this,
            calenderManager.getCurrentCalendar().getCurrentYear(),
            calenderManager.getCurrentCalendar().getCurrentMonth().getValue());
  }

  /**
   * Moves the active calendar to the previous month and updates the GUI view.
   */
  @Override
  public void goToPreviousMonth() {
    Month month = calenderManager.getCurrentCalendar().getCurrentMonth();
    int currentYear = calenderManager.getCurrentCalendar().getCurrentYear();
    Month prevMonth = month.minus(1);
    int prevYear = currentYear;
    if (prevMonth == Month.DECEMBER) {
      prevYear--;
      calenderManager.getCurrentCalendar().setCurrentYear(prevYear);
    }
    calenderManager.getCurrentCalendar().setCurrentMonth(prevMonth);
    ((GUICalendarViewImpl) view).setMonthPanelFeatures(this,
            calenderManager.getCurrentCalendar().getCurrentYear(),
            calenderManager.getCurrentCalendar().getCurrentMonth().getValue());
  }

  /**
   * Returns the time zone of the current active calendar.
   *
   * @return the ZoneId of the current calendar
   */
  @Override
  public ZoneId getTimeZone() {
    return this.calenderManager.getCurrentCalendar().getZoneId();
  }

  /**
   * Callback method that updates the GUI view when the calendar month changes.
   *
   * @param model    the calendar model that changed
   * @param newYear  the new year after the change
   * @param newMonth the new month after the change
   */
  @Override
  public void onMonthChanged(ICalenderModel model, int newYear, Month newMonth) {
    ((IGUICalendarView) view).getMainFrameHelper().getNavPanel()
            .setMonthLabel(newMonth.name(), newYear);
    ((IGUICalendarView) view).getMainFrameHelper().getMonthPanel().updateMonth(newYear,
            newMonth.getValue(), -1);
  }

  /**
   * Callback method invoked when a new calendar is added. Updates the calendar list.
   */
  @Override
  public void newCalendarAdded() {
    updateCalendarList((IGUICalendarView) view);
  }

}
