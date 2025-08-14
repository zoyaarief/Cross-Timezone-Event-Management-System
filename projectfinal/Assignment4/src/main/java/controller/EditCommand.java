package controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.EventModelImpl;
import model.InvalidCalenderOperationException;

/**
 * Handles edit event commands.
 * Parses command strings and prints edit details.
 */
public class EditCommand implements IInputCommand {

  /**
   * The calendar controller used for executing edits.
   */
  private CalenderControllerImpl calendar;

  /**
   * Constructs an EditCommand with the specified controller.
   *
   * @param calendar the calendar controller instance
   */
  public EditCommand(CalenderControllerImpl calendar) {
    this.calendar = calendar;
  }

  /**
   * Regex pattern for parsing the edit calendar command.
   */
  private static final Pattern EDIT_CALENDAR_PATTERN = Pattern.compile(
          "edit calendar\\s+--name\\s+(.+?)\\s+--property\\s+(.+?)\\s+(.+?)$",
          Pattern.CASE_INSENSITIVE);

  /**
   * Pattern for editing a single event with full date range.
   */
  private static final Pattern EDIT_SINGLE_PATTERN =
          Pattern.compile("edit event\\s+(\\w+)\\s+(.+?)\\s+from\\s+(.+?)\\s"
                          + "+to\\s+(.+?)\\s+with\\s+(.+)$",
                  Pattern.CASE_INSENSITIVE);

  /**
   * Pattern for editing recurring events using a date.
   */
  private static final Pattern EDIT_EVENTS_FROM_PATTERN =
          Pattern.compile("edit events\\s+(\\w+)\\s+(.+?)\\s+from\\s+(.+?)\\s+with\\s+(.+)$",
                  Pattern.CASE_INSENSITIVE);

  /**
   * Pattern for simple editing of events without date.
   */
  private static final Pattern EDIT_EVENTS_SIMPLE_PATTERN =
          Pattern.compile("edit events\\s+(\\w+)\\s+(.+?)\\s+(.+)$",
                  Pattern.CASE_INSENSITIVE);

  /**
   * Converts a date string to a Date object.
   * Appends "T00:00" if time is missing.
   *
   * @param dateStr the date string to convert
   * @return a Date object representing the date/time
   * @throws InvalidCalenderOperationException if parsing fails
   */
  private Date convertStringToDate(String dateStr) throws InvalidCalenderOperationException {
    if (!dateStr.contains("T")) {
      if (!dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
        throw new InvalidCalenderOperationException("Error: Expected "
                + "date in format YYYY-MM-DD. Please try again.");
      }
      dateStr += "T00:00";
    }

    try {
      LocalDateTime ldt = LocalDateTime.parse(dateStr, EventModelImpl.DATE_TIME_FORMATTER);
      ZonedDateTime zdt = ldt.atZone(ZoneId.of("America/New_York"));
      return Date.from(zdt.toInstant());
    } catch (Exception e) {
      throw new InvalidCalenderOperationException("Error: Invalid date/time format: "
              + dateStr + ". Please try again.");
    }
  }


  /**
   * Parses the edit command and executes the appropriate edit.
   *
   * @param command the edit event command string
   * @throws InvalidCalenderOperationException if the command format is invalid
   */
  @Override
  public void parseAndExecute(String command)
          throws InvalidCalenderOperationException {
    Matcher matcher;
    String lower = command.toLowerCase();

    // first, we edit calendar
    if (lower.startsWith("edit calendar")) {
      matcher = EDIT_CALENDAR_PATTERN.matcher(command);
      if (matcher.find()) {
        String calName = matcher.group(1).trim();
        String property = matcher.group(2).trim().toLowerCase();
        String newValue = matcher.group(3).trim();
        System.out.println("Edit calendar: " + calName + " " + property + " " + newValue);
        try {
          calendar.editCalendar(property, calName, newValue);
          System.out.println("Successfully updated calendar for " + calName);
        } catch (InvalidCalenderOperationException e) {
          System.err.println("Error: " + e.getMessage() + " Please try again.");
        }
        return;
      } else {
        throw new InvalidCalenderOperationException("Invalid edit calendar command format.");
      }
    }


    if (lower.startsWith("edit event ")) {
      matcher = EDIT_SINGLE_PATTERN.matcher(command);
      if (matcher.find()) {
        String property = matcher.group(1);
        String eventName = matcher.group(2);
        String startDT = matcher.group(3);
        String endDT = matcher.group(4);
        String newValue = matcher.group(5);
        try {
          Date startDate = convertStringToDate(startDT);
          Date endDate = convertStringToDate(endDT);
          calendar.editEvent(property, eventName, startDate, endDate, newValue);
          System.out.println("Successfully updated event " + eventName);
        } catch (InvalidCalenderOperationException e) {
          System.err.println("Error: " + e.getMessage() + " Please try again.");
        }
        return;
      }
    }

    if (lower.startsWith("edit events ")) {
      if (lower.contains(" from ")) {
        matcher = EDIT_EVENTS_FROM_PATTERN.matcher(command);
        if (matcher.find()) {
          String property = matcher.group(1);
          String eventName = matcher.group(2);
          String startDT = matcher.group(3);
          String newValue = matcher.group(4);
          try {
            Date startDate = convertStringToDate(startDT);
            calendar.editEvent(property, eventName, startDate, newValue);
            System.out.println("Successfully updated recurring events for " + eventName);
          } catch (InvalidCalenderOperationException e) {
            System.err.println("Error: " + e.getMessage() + " Please try again.");
          }
          return;
        }
      } else {
        matcher = EDIT_EVENTS_SIMPLE_PATTERN.matcher(command);
        if (matcher.find()) {
          String property = matcher.group(1);
          String eventName = matcher.group(2);
          String newValue = matcher.group(3);
          calendar.editEvent(property, eventName, newValue);
          System.out.println("Successfully updated all events for " + eventName);
          return;
        }
      }
    }

    throw new InvalidCalenderOperationException("Invalid edit command format.");
  }

}
