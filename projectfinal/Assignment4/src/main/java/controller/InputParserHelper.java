package controller;

import model.InvalidCalenderOperationException;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to parse user commands.
 * Delegates command strings to proper InputCommand.
 */
public class InputParserHelper {

  /**
   * Enum for supported command types.
   */
  public enum CommandType {
    CREATE, EDIT, PRINT, MISC, COPY
  }

  protected Map<CommandType, IInputCommand> commandMap;

  /**
   * Constructs the helper and maps commands to types.
   */
  public InputParserHelper(CalenderControllerImpl controller) {
    commandMap = new HashMap<>();
    commandMap.put(CommandType.CREATE, new CreateCommand(controller));
    commandMap.put(CommandType.EDIT, new EditCommand(controller));
    commandMap.put(CommandType.PRINT, new PrintCommand(controller));
    commandMap.put(CommandType.COPY, new CopyCommand(controller));
    commandMap.put(CommandType.MISC, new MiscCommand(controller));

  }

  /**
   * Parses the input command and executes it.
   * @param inputCommand the command string from the user
   * @throws InvalidCalenderOperationException if command is empty or unknown
   */
  public void parseInputCommand(String inputCommand)
          throws InvalidCalenderOperationException {
    if (inputCommand == null || inputCommand.trim().isEmpty()) {
      return;
    }
    String trimmed = inputCommand.trim();
    CommandType type = determineCommandType(trimmed.toLowerCase());
    if (type == null) {
      throw new InvalidCalenderOperationException("Unknown command type.");
    }
    commandMap.get(type).parseAndExecute(trimmed);
  }

  /**
   * Determines the command type from the input string.
   * @param command the lower-case command string
   * @return the CommandType or null if not recognized
   */
  protected CommandType determineCommandType(String command) {
    if (command.startsWith("create")) {
      return CommandType.CREATE;
    } else if (command.startsWith("edit")) {
      return CommandType.EDIT;
    } else if (command.startsWith("print events")) {
      return CommandType.PRINT;
    }
    else if (command.startsWith("copy")) {
      return CommandType.COPY;
    }
    else if (command.startsWith("export cal") ||
            command.startsWith("show status on") ||
            command.startsWith("use")) {
      return CommandType.MISC;
    }
    return null;
  }
}
