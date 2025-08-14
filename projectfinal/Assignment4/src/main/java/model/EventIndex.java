package model;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EventIndex uses two maps:
 * 1) startKeyIndex: for exact (eventName, startDateTime) lookups,
 * 2) nameIndex: for retrieving groups of events by name.
 * NOTE: We are NOT using endDateTime in the key. If two events share
 * the same name and start time, one of them will overwrite the other
 * unless you store them in a list.
 */
class EventIndex {

  // Key: "lowercasedName|startMillis"
  private final Map<String, IEventModel> startKeyIndex;

  // Key: lowercasedName, Value: all events that share that name
  private final Map<String, List<IEventModel>> nameIndex;

  public EventIndex() {
    this.startKeyIndex = new HashMap<>();
    this.nameIndex = new HashMap<>();
  }

  /**
   * Add an event to the indexes.
   */
  protected void addEvent(IEventModel event) {
    // Insert into startKeyIndex
    String startKey = generateStartKey(event.getEventName(), event.getStartDateTime());
    startKeyIndex.put(startKey, event);

    // Insert into nameIndex
    String nameKey = event.getEventName().toLowerCase();
    nameIndex.computeIfAbsent(nameKey, k -> new ArrayList<>()).add(event);
  }

  /**
   * Remove an event from the indexes.
   */
  protected void removeEvent(IEventModel event) {
    // Remove from startKeyIndex
    String startKey = generateStartKey(event.getEventName(), event.getStartDateTime());
    startKeyIndex.remove(startKey);

    // Remove from nameIndex
    String nameKey = event.getEventName().toLowerCase();
    List<IEventModel> list = nameIndex.get(nameKey);
    if (list != null) {
      list.remove(event);
      if (list.isEmpty()) {
        nameIndex.remove(nameKey);
      }
    }
  }

  /**
   * Retrieve a single event by (name, startDateTime).
   * NOTE: This does not take endDateTime into account!
   */
  protected IEventModel getEventByStartKey(String eventName, Date startDateTime) {
    String startKey = generateStartKey(eventName, startDateTime);
    return startKeyIndex.get(startKey);
  }

  /**
   * Retrieve all events by a given event name (case-insensitive).
   */
  protected List<IEventModel> getEventsByName(String eventName) {
    List<IEventModel> found = nameIndex.get(eventName.toLowerCase());
    return (found == null) ? Collections.emptyList() : new ArrayList<>(found);
  }

  /**
   * Re-index an event after its identifying fields (name or start time) change.
   */
  protected void reindexEvent(IEventModel oldEventState, IEventModel updatedEvent) {
    removeEvent(oldEventState);
    addEvent(updatedEvent);
  }

  /**
   * Creates a key for exact matches: name|startMillis.
   */
  private String generateStartKey(String name, Date start) {
    long startMs = (start == null) ? 0 : start.getTime();
    return name.toLowerCase() + "|" + startMs;
  }
}
