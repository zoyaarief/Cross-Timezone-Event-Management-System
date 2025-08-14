package view;

import java.io.File;

/**
 * IGUICalendarView extends ICalenderView to add GUI-specific methods.
 */
public interface IGUICalendarView extends ICalenderView {

  /**
   * Prompts user for import file.
   * @return selected File or null.
   */
  File promptUserForImportFile();


  /**
   * this is to force the view to have a method to set up actions for buttons.
   * All the buttons must be given this action listener
   */

  GUIMainFrame getMainFrameHelper();


}
