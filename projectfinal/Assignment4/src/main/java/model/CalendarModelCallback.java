package model;

import java.time.Month;

/**
 * Callback interface so the model can notify whoever is interested (usually the controller).
 */
public interface CalendarModelCallback {

  /**
   * Called whenever the model's current month or year is changed.
   *
   * @param model the model that changed
   * @param newYear the new year
   * @param newMonth the new month (1..12)
   */
  void onMonthChanged(ICalenderModel model, int newYear, Month newMonth);


  /**
   * Called when a new calendar has been added through dialogu box.
   *
   */
  void newCalendarAdded();


}
