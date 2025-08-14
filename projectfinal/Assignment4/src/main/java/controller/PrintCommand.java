package controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.InvalidCalenderOperationException;

/**
 * Handles print event commands.
 * Parses and calls methods to print events.
 */
public class PrintCommand implements IInputCommand {

  private final CalenderControllerImpl controller;

  public PrintCommand(CalenderControllerImpl controller) {
    this.controller = controller;
  }

  /**
   * Pattern for printing events on a specific date.
   */
  private static final Pattern PRINT_ON_PATTERN =
          Pattern.compile("print events on\\s+(.+)$", Pattern.CASE_INSENSITIVE);

  /**
   * Pattern for printing events in a date range.
   */
  private static final Pattern PRINT_FROM_TO_PATTERN =
          Pattern.compile("print events from\\s+(.+?)\\s+to\\s+(.+)$",
                  Pattern.CASE_INSENSITIVE);

  /**
   * Parses the print command and prints events.
   * @param command the print command string.
   * @throws InvalidCalenderOperationException if format is invalid.
   */
  @Override
  public void parseAndExecute(String command)
          throws InvalidCalenderOperationException {
    Matcher matcher = PRINT_ON_PATTERN.matcher(command);
    if (matcher.find()) {
      return;
    }
    matcher = PRINT_FROM_TO_PATTERN.matcher(command);
    if (matcher.find()) {
      controller.printEvents(matcher.group(1),
              matcher.group(2));
      return;
    }
    throw new InvalidCalenderOperationException(
            "Invalid print events command format.");
  }
}
