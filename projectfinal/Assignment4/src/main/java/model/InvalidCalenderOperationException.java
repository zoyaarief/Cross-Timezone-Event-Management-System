package model;

/**
 * Exception class common for all kinds of exception message in the project.
 */
public class InvalidCalenderOperationException extends Exception {
  public InvalidCalenderOperationException(String message) {
    super(message);
  }
}
