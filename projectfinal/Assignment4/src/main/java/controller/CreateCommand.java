package controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.EventModelImpl;
import model.InvalidCalenderOperationException;
import model.IEventModel;

/**
 * Handles create commands for both events and calendars.
 * Parses command strings and calls the controller's methods.
 */
public class CreateCommand implements IInputCommand {

  private final CalenderControllerImpl controller;

  /**
   * Constructs a CreateCommand with the specified controller.
   * @param controller the calendar controller instance
   */
  public CreateCommand(CalenderControllerImpl controller) {
    this.controller = controller;
  }

  /**
   * Regex Pattern for creating a calendar.
   */
  private static final Pattern CALENDAR_PATTERN =
          Pattern.compile("create calendar\\s+--name\\s+(.+?)\\s+--timezone\\s+(.+)$",
                  Pattern.CASE_INSENSITIVE);

  /**
   * Regex Pattern for event creation with from/to and repeats for times.
   */
  private static final Pattern FROM_TO_REPEAT_FOR =
          Pattern.compile("create event\\s+(.+?)\\s+from\\s+(.+?)\\s+to\\s+(.+?)" +
                          "\\s+repeats\\s+(.+?)\\s+for\\s+(\\d+)\\s+times\\s*$",
                  Pattern.CASE_INSENSITIVE);

  /**
   * Regex Pattern for event creation with from/to and repeats until.
   */
  private static final Pattern FROM_TO_REPEAT_UNTIL =
          Pattern.compile("create event\\s+(.+?)\\s+from\\s+(.+?)\\s+to\\s+(.+?)" +
                          "\\s+repeats\\s+(.+?)\\s+until\\s+(.+?)\\s*$",
                  Pattern.CASE_INSENSITIVE);

  /**
   * Regex Pattern for event creation with from/to.
   */
  private static final Pattern FROM_TO =
          Pattern.compile("create event\\s+(.+?)\\s+from\\s+(.+?)\\s+to\\s+(.+?)\\s*$",
                  Pattern.CASE_INSENSITIVE);

  /**
   * Regex Pattern for event creation on a date with repeats for times.
   */
  private static final Pattern ON_REPEAT_FOR =
          Pattern.compile("create event\\s+(.+?)\\s+on\\s+(\\d{4}-\\d{2}-\\d{2})\\s+repeats" +
                          "\\s+([MTWRFSU]+)\\s+for\\s+(\\d+)\\s+times\\s*$",
                  Pattern.CASE_INSENSITIVE);

  /**
   * Regex Pattern for event creation on a date with repeats until.
   */
  private static final Pattern ON_REPEAT_UNTIL =
          Pattern.compile("create event\\s+(.+?)\\s+on\\s+(\\d{4}-\\d{2}-\\d{2})\\s+repeats" +
                          "\\s+([MTWRFSU]+)\\s+until\\s+(\\d{4}-\\d{2}-\\d{2})\\s*$",
                  Pattern.CASE_INSENSITIVE);

  /**
   * Regex Pattern for event creation on a specific date.
   */
  private static final Pattern ON_ONLY =
          Pattern.compile("create event\\s+(.+?)\\s+on\\s+(.+?)\\s*$",
                  Pattern.CASE_INSENSITIVE);

  /**
   * Parses the create command and executes the corresponding action.
   * Supports both event creation and calendar creation.
   *
   * @param command the create command string.
   * @throws InvalidCalenderOperationException if command format is invalid.
   */
  @Override
  public void parseAndExecute(String command)
          throws InvalidCalenderOperationException {
    String lower = command.toLowerCase();
    // Process calendar creation commands.
    if (lower.startsWith("create calendar")) {
      Matcher calMatcher = CALENDAR_PATTERN.matcher(command);
      if (calMatcher.find()) {
        String calName = calMatcher.group(1).trim();
        String timeZone = calMatcher.group(2).trim();
        controller.createCalendar(calName, timeZone);
        return;
      } else {
        throw new InvalidCalenderOperationException(
                "Invalid create calendar command format.");
      }
    }
    // Process event creation commands.
    if (lower.startsWith("create event")) {
      Matcher matcher = FROM_TO_REPEAT_FOR.matcher(command);
      if (matcher.find()) {
        validateDateTimeOrder(matcher.group(2), matcher.group(3));
        validateWeekdays(matcher.group(4));
        IEventModel event = EventModelImpl.getBuilder(matcher.group(1).trim(),
                        matcher.group(2).trim(), controller.getCalendarManager()
                                .getCurrentCalendar().getZoneId())
                .endDateString(matcher.group(3).trim())
                .build();
        controller.addRecurringEventToCalender(event, false,
                matcher.group(4).trim(), Integer.parseInt(matcher.group(5).trim()));
        return;
      }
      matcher = FROM_TO_REPEAT_UNTIL.matcher(command);
      if (matcher.find()) {
        validateDateTimeOrder(matcher.group(2), matcher.group(3));
        validateDateTimeOrder(matcher.group(3), matcher.group(5));
        validateWeekdays(matcher.group(4));
        IEventModel event = EventModelImpl.getBuilder(matcher.group(1).trim(),
                        matcher.group(2).trim(), controller.getCalendarManager()
                                .getCurrentCalendar().getZoneId())
                .endDateString(matcher.group(3).trim())
                .build();
        controller.addRecurringEventToCalender(event, false,
                matcher.group(4).trim(), matcher.group(5).trim());
        return;
      }
      matcher = FROM_TO.matcher(command);
      if (matcher.find()) {
        validateDateTimeOrder(matcher.group(2), matcher.group(3));
        IEventModel event = EventModelImpl.getBuilder(matcher.group(1).trim(),
                        matcher.group(2).trim(), controller.getCalendarManager()
                                .getCurrentCalendar().getZoneId())
                .endDateString(matcher.group(3).trim())
                .build();
        controller.addEventToCalender(event, false);
        return;
      }
      matcher = ON_REPEAT_FOR.matcher(command);
      if (matcher.find()) {
        validateWeekdays(matcher.group(3));
        IEventModel event = EventModelImpl.getBuilder(matcher.group(1).trim(),
                        matcher.group(2).trim(), controller.getCalendarManager()
                                .getCurrentCalendar().getZoneId())
                .build();
        controller.addRecurringEventToCalender(event, false,
                matcher.group(3).trim(), Integer.parseInt(matcher.group(4).trim()));
        return;
      }
      matcher = ON_REPEAT_UNTIL.matcher(command);
      if (matcher.find()) {
        validateWeekdays(matcher.group(3));
        IEventModel event = EventModelImpl.getBuilder(matcher.group(1).trim(),
                        matcher.group(2).trim(), controller.getCalendarManager()
                                .getCurrentCalendar().getZoneId())
                .build();
        controller.addRecurringEventToCalender(event, false,
                matcher.group(3).trim(), matcher.group(4).trim());
        return;
      }
      matcher = ON_ONLY.matcher(command);
      if (matcher.find()) {
        IEventModel event = EventModelImpl.getBuilder(matcher.group(1).trim(),
                        matcher.group(2).trim(), controller.getCalendarManager()
                                .getCurrentCalendar().getZoneId())
                .build();
        controller.addEventToCalender(event, false);
        return;
      }
    }
    throw new InvalidCalenderOperationException("Invalid create event command format.");
  }

  /**
   * Checks that start and end date strings are non-empty.
   *
   * @param startDateTime the start date string
   * @param endDateTime the end date string
   * @throws InvalidCalenderOperationException if any date string is empty
   */
  private void validateDateTimeOrder(String startDateTime, String endDateTime)
          throws InvalidCalenderOperationException {
    if (startDateTime == null || startDateTime.trim().isEmpty() ||
            endDateTime == null || endDateTime.trim().isEmpty()) {
      throw new InvalidCalenderOperationException("Date/time values cannot be empty.");
    }
    try {
      var start = java.time.LocalDateTime.parse(startDateTime);
      var end = java.time.LocalDateTime.parse(endDateTime);
      if (start.isAfter(end)) {
        throw new InvalidCalenderOperationException("Start time must be before end time.");
      }
    } catch (Exception e) {
      throw new InvalidCalenderOperationException("Invalid date/time format: "
              + startDateTime + " or " + endDateTime);
    }
  }

  /**
   * Checks that the weekdays string contains valid letters.
   *
   * @param weekdays the weekdays string
   * @throws InvalidCalenderOperationException if invalid letters or duplicates are found
   */
  private void validateWeekdays(String weekdays)
          throws InvalidCalenderOperationException {
    //System.err.println("✅ validateWeekdays CALLED with: " + weekdays);
    if (weekdays == null || weekdays.trim().isEmpty()) {
      throw new InvalidCalenderOperationException("Weekdays string cannot be empty.");
    }
    String allowed = "MTWRFSU";
    java.util.Set<Character> seen = new java.util.HashSet<>();
    for (char ch : weekdays.toUpperCase().toCharArray()) {
      if (allowed.indexOf(ch) == -1) {
        throw new InvalidCalenderOperationException("Invalid weekday letter: " + ch +
                ". Allowed letters are M, T, W, R, F, S, U.");
      }
      if (seen.contains(ch)) {
        throw new InvalidCalenderOperationException("Duplicate weekday letter: " + ch +
                " is not allowed.");
      }
      seen.add(ch);
    }
    if (weekdays.length() > 7) {
      throw new InvalidCalenderOperationException("Weekdays string cannot contain more " +
              "than 7 letters.");
    }
  }
}
