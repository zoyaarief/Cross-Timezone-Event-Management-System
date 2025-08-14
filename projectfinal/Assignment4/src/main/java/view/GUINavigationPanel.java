package view;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * Navigation panel for the Calendar GUI. Contains buttons for page
 * navigation, calendar selection, and calendar operations.
 */
public class GUINavigationPanel extends JPanel {
  private JButton prevButton;
  private JButton nextButton;
  private JButton newCalendar;
  private JButton editButton;
  private JButton importButton;  // New Import button.
  private JButton exportButton;  // New Export button.
  private JLabel monthLabel;
  private JComboBox<String> calendarSelector;

  /**
   * Constructs the navigation panel with left and right sections.
   */
  public GUINavigationPanel() {
    // Use a BorderLayout to separate left and right components.
    setLayout(new BorderLayout());

    // Left panel for navigation components.
    JPanel navLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
    prevButton = new JButton("<");
    nextButton = new JButton(">");
    monthLabel = new JLabel("default Val");
    calendarSelector = new JComboBox<>();
    newCalendar = new JButton("+");

    navLeft.add(prevButton);
    navLeft.add(monthLabel);
    navLeft.add(nextButton);
    navLeft.add(calendarSelector);
    navLeft.add(newCalendar);

    // Right panel for additional buttons.
    JPanel navRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    editButton = new JButton("Edit");
    importButton = new JButton("Import");
    exportButton = new JButton("Export");

    navRight.add(editButton);
    navRight.add(importButton);
    navRight.add(exportButton);

    add(navLeft, BorderLayout.WEST);
    add(navRight, BorderLayout.EAST);
  }

  /**
   * Returns the previous month button.
   *
   * @return the previous button.
   */
  public JButton getPrevButton() {
    return prevButton;
  }

  /**
   * Returns the next month button.
   *
   * @return the next button.
   */
  public JButton getNextButton() {
    return nextButton;
  }

  /**
   * Returns the new calendar button.
   *
   * @return the new calendar button.
   */
  public JButton getNewCalendarButton() {
    return newCalendar;
  }

  /**
   * Returns the edit button.
   *
   * @return the edit button.
   */
  public JButton getEditButton() {
    return editButton;
  }

  /**
   * Returns the import button.
   *
   * @return the import button.
   */
  public JButton getImportButton() {
    return importButton;
  }

  /**
   * Returns the export button.
   *
   * @return the export button.
   */
  public JButton getExportButton() {
    return exportButton;
  }

  /**
   * Returns the month label.
   *
   * @return the month label.
   */
  public JLabel getMonthLabel() {
    return monthLabel;
  }

  /**
   * Returns the calendar selector combo box.
   *
   * @return the calendar selector.
   */
  public JComboBox<String> getCalendarSelector() {
    return calendarSelector;
  }

  /**
   * Sets the month label text with the given month and year.
   *
   * @param currentMonth the current month as a string.
   * @param currentYear  the current year.
   */
  public void setMonthLabel(String currentMonth, int currentYear) {
    monthLabel.setText(currentMonth + " " + currentYear);
    monthLabel.revalidate();
    monthLabel.repaint();
  }
}
