package controller;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import model.CalendarManager;
import model.InvalidCalenderOperationException;

import static org.junit.Assert.assertEquals;

/**
 * Test class for all misc commands.
 */
public class MiscCommandTest {


  private MiscCommand miscCommand;
  private List<String> callLog;

  @Before
  public void setUp() {
    //CalendarManager.resetGlobalState();
    callLog = new ArrayList<>();
    // We'll use a minimal test subclass of CalenderControllerImpl
    TestController testController = new TestController();

    // The MiscCommand under test
    miscCommand = new MiscCommand(testController);
  }

  @Test
  public void testParseAndExecute_exportCal_valid() throws InvalidCalenderOperationException {
    // e.g. "export cal myCalendar.csv"
    String command = "export cal myCalendar.csv";
    miscCommand.parseAndExecute(command);

    // Check that we got a single call: "exportCalendar(myCalendar.csv)"
    assertEquals(1, callLog.size());
    assertEquals("exportCalendar(myCalendar.csv)", callLog.get(0));
  }

  @Test
  public void testParseAndExecute_showStatus_valid() throws InvalidCalenderOperationException {
    // e.g. "show status on 2025-02-10T09:00"
    String command = "show status on 2025-02-10T09:00";
    miscCommand.parseAndExecute(command);

    // Should log "showStatusOn(2025-02-10T09:00)"
    assertEquals(1, callLog.size());
    assertEquals("showStatusOn(2025-02-10T09:00)", callLog.get(0));
  }

  @Test
  public void testParseAndExecute_useCalendar_valid() throws InvalidCalenderOperationException {
    // e.g. "use calendar --name MyCal"
    String command = "use calendar --name MyCal";
    miscCommand.parseAndExecute(command);

    // Should log "useCalendar(MyCal)"
    assertEquals(1, callLog.size());
    assertEquals("useCalendar(MyCal)", callLog.get(0));
  }

  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_unknownMiscCommand() throws InvalidCalenderOperationException {
    String command = "some unknown misc command";
    miscCommand.parseAndExecute(command);
  }


  private class TestController extends CalenderControllerImpl {

    public TestController() {
      super(null, null, new InputStreamReader(
              // pass an empty InputStream
              new ByteArrayInputStream(new byte[0])
      ));
    }

    @Override
    public void exportCalendar(String fileName) {
      // Instead of real logic, just record
      callLog.add("exportCalendar(" + fileName + ")");
    }

    @Override
    public void showStatusOn(String dateTime) throws InvalidCalenderOperationException {
      // record
      callLog.add("showStatusOn(" + dateTime + ")");
    }

    @Override
    public void useCalendar(String calName) {
      // record
      callLog.add("useCalendar(" + calName + ")");
    }
  }

  @Test
  public void testImportCalBranch() throws Exception {
    // 1) Make a dummy flag container
    final String[] captured = new String[1];

    // 2) Fake controller that only records importCalendar calls
    IInputCommand misc = new MiscCommand(new CalenderControllerImpl(
            new CalendarManager(), new StringReader("")) {
      @Override
      public void importCalendar(String fileName) {
        captured[0] = fileName;
      }
    });

    misc.parseAndExecute("import cal events.csv");
    assertEquals("events.csv", captured[0]);
  }


  @Test(expected = InvalidCalenderOperationException.class)
  public void testParseAndExecute_importCal_badFormat() throws Exception {
    // missing ".csv"
    miscCommand.parseAndExecute("import cal events.txt");
  }
}