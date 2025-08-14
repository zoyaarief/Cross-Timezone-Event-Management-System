package controller;

import model.InvalidCalenderOperationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles the new "copy" commands, parsing the command string
 * and calling the appropriate methods in CalenderControllerImpl.
 */
public class CopyCommand implements IInputCommand {

  private final CalenderControllerImpl controller;

  //  copy event <eventName> on <dateTime> --target <calName> to <dateTime>
  private static final Pattern COPY_SINGLE_EVENT_PATTERN = Pattern.compile(
          "^copy\\s+event\\s+(.+?)\\s+on\\s+(.+?)\\s+--target\\s+(.+?)\\s+to\\s+(.+?)$",
          Pattern.CASE_INSENSITIVE
  );

  //  copy events on <date> --target <calName> to <date>
  private static final Pattern COPY_EVENTS_ON_PATTERN = Pattern.compile(
          "^copy\\s+events\\s+on\\s+(.+?)\\s+--target\\s+(.+?)\\s+to\\s+(.+?)$",
          Pattern.CASE_INSENSITIVE
  );

  //  copy events between <date> and <date> --target <calName> to <date>
  private static final Pattern COPY_EVENTS_BETWEEN_PATTERN = Pattern.compile(
          "^copy\\s+events\\s+between\\s+(.+?)\\s+and\\s+(.+?)"
                  + "\\s+--target\\s+(.+?)\\s+to\\s+(.+?)$",
          Pattern.CASE_INSENSITIVE
  );

  public CopyCommand(CalenderControllerImpl controller) {
    this.controller = controller;
  }

  @Override
  public void parseAndExecute(String command)
          throws InvalidCalenderOperationException {

    Matcher m = COPY_SINGLE_EVENT_PATTERN.matcher(command);
    if (m.find()) {
      String eventName    = m.group(1).trim();
      String sourceStart  = m.group(2).trim();    // "on <dateTime>"
      String targetCal    = m.group(3).trim();    // " --target <calendarName>"
      String targetStart  = m.group(4).trim();    // " to <dateTime>"

      controller.copySingleEvent(eventName, sourceStart, targetCal, targetStart);
      return;
    }

    m = COPY_EVENTS_ON_PATTERN.matcher(command);
    if (m.find()) {
      String sourceDay  = m.group(1).trim();
      String targetCal  = m.group(2).trim();
      String targetDay  = m.group(3).trim();

      controller.copyEventsOnDay(sourceDay, targetCal, targetDay);
      return;
    }

    m = COPY_EVENTS_BETWEEN_PATTERN.matcher(command);
    if (m.find()) {
      String fromDate   = m.group(1).trim();
      String toDate     = m.group(2).trim();
      String targetCal  = m.group(3).trim();
      String targetBase = m.group(4).trim();

      controller.copyEventsBetween(fromDate, toDate, targetCal, targetBase);
      return;
    }

    throw new InvalidCalenderOperationException("Invalid copy command format.");
  }
}
