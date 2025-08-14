package controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.InvalidCalenderOperationException;

/**
 * Handles miscellaneous commands like export cal and show status.
 */
public class MiscCommand implements IInputCommand {

  private final CalenderControllerImpl controller;

  public MiscCommand(CalenderControllerImpl controller) {
    this.controller = controller;
  }

  /**
   * Matches "export cal fileName.csv" captures the file name in group(1).
   */
  private static final Pattern EXPORT_PATTERN =
          Pattern.compile("export cal\\s+(.+\\.csv)$",
                  Pattern.CASE_INSENSITIVE);

  private static final Pattern IMPORT_PATTERN =
          Pattern.compile("import cal\\s+(.+\\.csv)$", Pattern.CASE_INSENSITIVE);

  /**
   * Matches "show status on dateTime"
   * Captures the dateTime in group(1).
   */
  private static final Pattern SHOW_STATUS_PATTERN =
          Pattern.compile("show status on\\s+(.+)$",
                  Pattern.CASE_INSENSITIVE);


  /**
   * Matches "use calendar --name calendarName"
   * Captures calendarName in group(1).
   */
  private static final Pattern USE_CALENDAR_PATTERN =
          Pattern.compile("use calendar\\s+--name\\s+(.+)$", Pattern.CASE_INSENSITIVE);


  /**
   * Parses the command and delegates to the model.
   *
   * @param command the command string
   * @throws InvalidCalenderOperationException if no pattern matches
   */
  @Override
  public void parseAndExecute(String command)
          throws InvalidCalenderOperationException {

    // Check for export command
    Matcher matcher = EXPORT_PATTERN.matcher(command);
    if (matcher.find()) {
      String fileName = matcher.group(1);
      controller.exportCalendar(fileName);
      return;
    }

    // Check for show status command
    matcher = SHOW_STATUS_PATTERN.matcher(command);
    if (matcher.find()) {
      String dateTime = matcher.group(1);
      controller.showStatusOn(dateTime);
      return;
    }

    // Check for "use calendar --name <name>" command
    matcher = USE_CALENDAR_PATTERN.matcher(command);
    if (matcher.find()) {
      String calName = matcher.group(1).trim();
      controller.useCalendar(calName);
      return;
    }

    matcher = IMPORT_PATTERN.matcher(command);
    if (matcher.find()) {
      String fileName = matcher.group(1);
      controller.importCalendar(fileName);
      return;
    }

    throw new InvalidCalenderOperationException(
            "Unknown misc command format.");
  }
}
