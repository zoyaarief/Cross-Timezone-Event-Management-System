package model;


import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * This class test the indexbased event saving to faster lookup.
 */
public class EventIndexTest {


  private EventIndex index;

  @Before
  public void setUp() {
    //CalendarManager.resetGlobalState();
    index = new EventIndex();
  }

  /**
   * A minimal IEventModel stub so we can test storing events
   * without depending on your entire EventModelImpl code.
   */
  private static class TestEvent implements IEventModel {
    private final String name;
    private final Date start;
    private final Date end;

    public TestEvent(String name, Date start, Date end) {
      this.name = name;
      this.start = start;
      this.end = end;
    }

    @Override
    public boolean checkEventConflict(IEventModel other) {
      // Not needed for testing EventIndex. Just return false.
      return false;
    }

    @Override
    public Date getStartDateTime() {
      return start;
    }

    @Override
    public Date getEndDateTime() {
      return end;
    }

    @Override
    public String getEventName() {
      return name;
    }

    @Override
    public String getLocation() {
      return null;
    }

    @Override
    public String getLongDescription() {
      return null;
    }

    @Override
    public EventStatus getStatus() {
      return null;
    }
  }

  @Test
  public void testAddEventAndRetrieveByStartKey() {
    // Create a test event
    Date start = new Date(1000L);  // Wed Dec 31 19:00:01 EST 1969
    Date end   = new Date(2000L);
    IEventModel e = new TestEvent("MyEvent", start, end);

    // Add
    index.addEvent(e);

    // Retrieve by start key
    IEventModel found = index.getEventByStartKey("MyEvent", start);
    assertNotNull( "We should retrieve the same event by name+start.",found);
    assertSame( "EventIndex should store the exact same object reference.", e, found);
  }

  @Test
  public void testAddEventAndRetrieveByName() {
    IEventModel e1 = new TestEvent("MyEvent", new Date(5000L), null);
    IEventModel e2 = new TestEvent("myEVENT", new Date(6000L), null);
    IEventModel e3 = new TestEvent("OtherEvent", new Date(7000L), null);

    index.addEvent(e1);
    index.addEvent(e2);
    index.addEvent(e3);

    // Retrieve all events with name "MyEvent" (case-insensitive)
    List<IEventModel> found = index.getEventsByName("MYevent");
    // We expect e1 and e2, but not e3
    assertEquals("Should find two events with name 'MyEvent' ignoring case.", 2, found.size());
    assertTrue("Should contain e1.", found.contains(e1));
    assertTrue("Should contain e2.", found.contains(e2));

    // Ensure "OtherEvent" doesn't appear
    List<IEventModel> foundOther = index.getEventsByName("otherEvent");
    assertEquals(1, foundOther.size());
    assertSame(e3, foundOther.get(0));
  }

  @Test
  public void testRemoveEvent() {
    IEventModel e1 = new TestEvent("Test", new Date(100L), new Date(200L));
    IEventModel e2 = new TestEvent("Test", new Date(300L), new Date(400L));
    index.addEvent(e1);
    index.addEvent(e2);

    // Sanity check: we can retrieve e1
    assertSame(e1, index.getEventByStartKey("Test", e1.getStartDateTime()));

    // Now remove e1
    index.removeEvent(e1);

    // e1 should be gone
    IEventModel found = index.getEventByStartKey("Test", e1.getStartDateTime());
    assertNull("Removed event e1 should not be found.",found);

    // e2 should still be present
    assertSame(e2, index.getEventByStartKey("Test", e2.getStartDateTime()));
  }

  @Test
  public void testReindexEvent() {
    IEventModel oldE = new TestEvent("OldName", new Date(1000L), new Date(2000L));
    index.addEvent(oldE);

    // Suppose we "update" the event to a new name or new start time
    // We'll simulate that by creating an updated object:
    IEventModel newE = new TestEvent("NewName", new Date(3000L), new Date(4000L));

    // reindexEvent expects the old state + the updated event
    index.reindexEvent(oldE, newE);

    // Now old references should be gone
    assertNull(index.getEventByStartKey("OldName", oldE.getStartDateTime()));
    List<IEventModel> oldList = index.getEventsByName("OldName");
    assertTrue( "Should have removed OldName references.", oldList.isEmpty());

    // New references should be present
    IEventModel found = index.getEventByStartKey("NewName", new Date(3000L));
    assertSame( "Should have the new event in the index.", newE, found);
    List<IEventModel> newList = index.getEventsByName("NewName");
    assertEquals(1, newList.size());
    assertSame(newE, newList.get(0));
  }

  @Test
  public void testGetEventByStartKey_noSuchEvent() {
    // There's nothing in the index
    IEventModel found = index.getEventByStartKey("Nothing", new Date(999999L));
    assertNull( "Should return null if event not found.", found);
  }

  @Test
  public void testGetEventsByName_empty() {
    // No events in the index
    List<IEventModel> list = index.getEventsByName("Anything");
    assertNotNull(list);
    assertTrue( "Should return an empty list if no events exist.",list.isEmpty());
  }
}

