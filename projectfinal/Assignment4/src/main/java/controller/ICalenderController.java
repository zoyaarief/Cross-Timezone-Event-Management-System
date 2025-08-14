package controller;

import view.ICalenderView;

/**
 * Interface for calendar controller.
 * Provides a method to run the controller.
 */
public interface ICalenderController {
  /**
   * Runs the main controller loop.
   */
  void execute();

  /**
   * Sets the view type.
   *
   */
  void setView(ICalenderView view);
}
