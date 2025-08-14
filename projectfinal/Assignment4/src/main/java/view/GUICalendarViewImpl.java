package view;

import controller.ICalendarGUIFeatures;
import model.IEventModel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.JFileChooser;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

/**
 * GUI view implementation for the Calendar App. Manages display of messages, events,
 * and GUI component interactions.
 */
public class GUICalendarViewImpl implements IGUICalendarView {

  private GUIMainFrame mainFrameHelper;
  private ICalendarGUIFeatures features;

  /**
   * Constructs the GUI calendar view by creating and displaying the main frame.
   */
  public GUICalendarViewImpl() {
    // Create the main frame that sets up all panels.
    mainFrameHelper = new GUIMainFrame("Calendar App (GUI Mode)");
    // Make frame visible.
    mainFrameHelper.showFrame();
  }

  /**
   * Displays a message in the main frame text area.
   *
   * @param message the message to display.
   */
  @Override
  public void displayMessage(String message) {
    // Append to the text area in the main frame.
    JTextArea msgArea = mainFrameHelper.getMessageArea();
    msgArea.append("[MESSAGE] " + message + "\n");
  }

  /**
   * Displays a list of events in the main frame text area.
   *
   * @param events a list of event models to display.
   */
  @Override
  public void displayEvents(List<IEventModel> events) {
    JTextArea msgArea = mainFrameHelper.getMessageArea();
    msgArea.append("[EVENTS] " + events.size() + " events.\n");
    for (IEventModel e : events) {
      msgArea.append("   " + e.getEventName() + "\n");
    }
  }

  /**
   * Prompts the user to select a file for importing events.
   *
   * @return the selected file or null if cancelled.
   */
  @Override
  public File promptUserForImportFile() {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Import Events File");
    int result = chooser.showOpenDialog(mainFrameHelper.getFrame());
    if (result == JFileChooser.APPROVE_OPTION) {
      return chooser.getSelectedFile();
    }
    return null;
  }

  /**
   * Returns the main frame helper for the GUI.
   *
   * @return the GUIMainFrame instance.
   */
  @Override
  public GUIMainFrame getMainFrameHelper() {
    return mainFrameHelper;
  }


  /**
   * Sets navigation panel features and attaches event listeners to
   * navigation buttons and controls.
   *
   * @param features the GUI features to use.
   */
  public void setNavPanelFeatures(ICalendarGUIFeatures features) {
    this.features = features;
    configureNavigationButtons();
    configureNewCalendarButton();
    configureEditButton();
    configureImportButton();
    configureExportButton();
    configureCalendarSelector();
  }

  private void configureNavigationButtons() {
    // Next month button
    mainFrameHelper.getNavPanel().getNextButton().addActionListener(
        e -> features.goToNextMonth());
    // Previous month button
    mainFrameHelper.getNavPanel().getPrevButton().addActionListener(
        e -> features.goToPreviousMonth());
  }

  private void configureNewCalendarButton() {
    mainFrameHelper.getNavPanel().getNewCalendarButton().addActionListener(
        e -> {
            GUICreateCalendarDialogBox dialog = new GUICreateCalendarDialogBox(mainFrameHelper);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
              String calName = dialog.getCalendarName();
              String timeZone = dialog.getTimeZoneInput();
              features.createCalendar(calName, timeZone);
            }
      });
  }

  private void configureEditButton() {
    mainFrameHelper.getNavPanel().getEditButton().addActionListener(
        e -> {
            GUISearchNEditDialogBox dialog = new GUISearchNEditDialogBox();
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
              String eventName = dialog.getEventName();
              Date startDateTime = dialog.getStartDateTime();
              Date endDateTime = dialog.getEndDateTime();
              String selectedProperty = dialog.getSelectedProperty();
              String newPropertyValue = dialog.getNewValue();
              if (startDateTime == null && endDateTime == null) {
                features.editEvent(selectedProperty, eventName, newPropertyValue);
              } else if (startDateTime != null && endDateTime == null) {
                features.editEvent(selectedProperty, eventName, startDateTime, newPropertyValue);
              } else {
                features.editEvent(selectedProperty, eventName,
                        startDateTime, endDateTime, newPropertyValue);
              }
            }
        });
  }

  private void configureImportButton() {
    mainFrameHelper.getNavPanel().getImportButton().addActionListener(
        e -> {
            File importFile = promptUserForImportFile();
            if (importFile != null) {
              features.importCalendar(importFile.getAbsolutePath());
            }
        });
  }

  private void configureExportButton() {
    mainFrameHelper.getNavPanel().getExportButton().addActionListener(
        e -> {
            String fileName = JOptionPane.showInputDialog(
                    mainFrameHelper.getFrame(),
                    "Enter file name for CSV export:",
                    "Export CSV",
                    JOptionPane.PLAIN_MESSAGE);
            if (fileName != null && !fileName.trim().isEmpty()) {
              // Ensure the file name ends with ".csv"
              if (!fileName.toLowerCase().endsWith(".csv")) {
                fileName += ".csv";
              }
              features.exportCalendar(fileName);
              JOptionPane.showMessageDialog(
                      mainFrameHelper.getFrame(),
                      "Calendar exported successfully to " + fileName);
            }
        });
  }

  private void configureCalendarSelector() {
    JComboBox<String> calendarSelector = mainFrameHelper.getNavPanel().getCalendarSelector();
    calendarSelector.addActionListener(
        e -> {
            String chosen = (String) calendarSelector.getSelectedItem();
            features.useCalendar(chosen);
        });
  }

  /**
   * Sets month panel features and attaches mouse listeners to day cells.
   *
   * @param features the GUI features to use.
   * @param year     the current year.
   * @param month    the current month (1-12).
   */
  public void setMonthPanelFeatures(ICalendarGUIFeatures features,
                                    int year, int month) {
    mainFrameHelper.getMonthPanel().getDayCells().forEach(dayCell -> {
      dayCell.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if (features != null) {
            System.out.println("Day cell clicked: " + dayCell.getDayNumber());
            dayCellClicked(year, month, dayCell.getDayNumber());
          }
        }
      });
    });
  }

  /**
   * Handles a day cell click and opens the dialog to show events for that day.
   *
   * @param year       the year of the clicked cell.
   * @param month      the month of the clicked cell.
   * @param dayOfMonth the day of the month clicked.
   */
  private void dayCellClicked(int year, int month, int dayOfMonth) {
    if (features != null) {
      // Build the date from year, month, and day.
      LocalDate clickedDate = LocalDate.of(year, month, dayOfMonth);
      // Open the day events dialog.
      GUIDayEventsDialogBox dialog =
              new GUIDayEventsDialogBox(mainFrameHelper, clickedDate, features);
      dialog.setVisible(true);
    }
  }
}
