package controller;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test class for imput parser helper class.
 */
public class InputParserHelperTest {

  private InputParserHelper helper;

  @Before
  public void setUp() {
    //CalendarManager.resetGlobalState();
    helper = new InputParserHelper(null);
  }

  @Test
  public void testDetermineCommandType_create() {
    assertEquals(InputParserHelper.CommandType.CREATE,
            helper.determineCommandType("create event something"));
  }

  @Test
  public void testDetermineCommandType_edit() {
    assertEquals(InputParserHelper.CommandType.EDIT,
            helper.determineCommandType("edit events description"));
  }

  @Test
  public void testDetermineCommandType_print() {
    assertEquals(InputParserHelper.CommandType.PRINT,
            helper.determineCommandType("print events"));
  }

  @Test
  public void testDetermineCommandType_copy() {
    assertEquals(InputParserHelper.CommandType.COPY,
            helper.determineCommandType("copy event name"));
  }

  @Test
  public void testDetermineCommandType_misc_export() {
    assertEquals(InputParserHelper.CommandType.MISC,
            helper.determineCommandType("export cal myfile.csv"));
  }

  @Test
  public void testDetermineCommandType_misc_showStatus() {
    assertEquals(InputParserHelper.CommandType.MISC,
            helper.determineCommandType("show status on 2025-01-01"));
  }

  @Test
  public void testDetermineCommandType_misc_use() {
    assertEquals(InputParserHelper.CommandType.MISC,
            helper.determineCommandType("use calendar --name MyCal"));
  }

  @Test
  public void testDetermineCommandType_unknown() {
    assertNull(helper.determineCommandType("foobar something"));
  }


}
