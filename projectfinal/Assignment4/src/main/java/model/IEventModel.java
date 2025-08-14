package model;

import java.util.Date;

/**
 * Defines an event with printable details and conflict checking.
 */
public interface IEventModel {


  /**
   * Determines if this event conflicts with another event.
   *
   * @param event the event to compare
   * @return true if the events conflict, otherwise false
   */
  boolean checkEventConflict(IEventModel event);

  /**
   * Returns the start date/time of this event.
   *
   * @return the start date/time as a Date
   */
  Date getStartDateTime();


  /**
   * Returns the end date/time of this event (may be null if not set).
   *
   * @return the end date/time as a Date
   */
  Date getEndDateTime();

  /**
   * Returns the name of this event.
   *
   * @return the event name
   */
  String getEventName();

  /**
   * Returns the location of this event.
   *
   * @return the event loation
   */
  String getLocation();

  /**
   * Returns long description of this event.
   *
   * @return the event description
   */
  String getLongDescription();

  /**
   * Returns public/private of this event.
   *
   * @return the event description
   */
  EventStatus getStatus();

}
