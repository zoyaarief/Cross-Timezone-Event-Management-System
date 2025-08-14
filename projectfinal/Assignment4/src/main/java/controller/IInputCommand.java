package controller;

import model.InvalidCalenderOperationException;

/**
 * Interface for input commands.
 * Each command parses and executes a command string.
 */
public interface IInputCommand {
  /**
   * Parses and executes the given command.
   * @param command the command string
   * @throws InvalidCalenderOperationException if command is invalid
   */
  void parseAndExecute(String command)
          throws InvalidCalenderOperationException;
}
