package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Displays a month view in a 7-column grid: row 0 = day-of-week headers, rows
 * 1..6 = day cells for the month.
 */
public class GUIMonthViewPanel extends JPanel {

  private static final String[] WEEKDAY_LABELS = {
    "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
  };

  // List of day-of-month cells.
  private List<GUIDayCellPanel> dayCells;

  // Background color for blank cells.
  private Color blankCellColor = new Color(255, 182, 193);

  /**
   * Constructs a GUIMonthViewPanel with a 7x7 grid layout.
   */
  public GUIMonthViewPanel() {
    // 7 rows: 1 header row plus up to 6 weeks, 7 columns per row.
    setLayout(new GridLayout(7, 7));
    dayCells = new ArrayList<>();
  }

  /**
   * Updates this panel to display the specified month and year, highlighting the
   * given day if within the month.
   *
   * @param year         the year (e.g., 2025)
   * @param month        the month (1..12)
   * @param highlightDay the day to highlight (1..31, or 0 if none)
   */
  public void updateMonth(int year, int month, int highlightDay) {
    // Clear any old cells.
    this.removeAll();
    dayCells.clear();

    // Row 0: day-of-week headers.
    for (String label : WEEKDAY_LABELS) {
      JLabel lbl = new JLabel(label, SwingConstants.CENTER);
      lbl.setOpaque(true);
      lbl.setBackground(new Color(230, 230, 230)); // light grey
      this.add(lbl);
    }

    LocalDate firstOfMonth = LocalDate.of(year, month, 1);
    int lengthOfMonth = firstOfMonth.lengthOfMonth(); // e.g. 28..31
    int startDow = firstOfMonth.getDayOfWeek().getValue(); // Monday=1, Sunday=7
    int dayCounter = 1;
    // There are 6 weeks * 7 columns = 42 cells for days.
    int totalCells = 6 * 7;

    for (int cellIndex = 0; cellIndex < totalCells; cellIndex++) {
      if (cellIndex >= (startDow - 1) && dayCounter <= lengthOfMonth) {
        // Create a cell for an actual day.
        int dayNumber = dayCounter;
        GUIDayCellPanel cell = new GUIDayCellPanel(dayNumber);
        if (highlightDay == dayNumber) {
          cell.setHighlighted(true);
        }
        dayCells.add(cell);
        this.add(cell);
        dayCounter++;
      } else {
        // Create a blank cell with the current blankCellColor.
        JPanel blank = new JPanel();
        blank.setBackground(blankCellColor);
        this.add(blank);
      }
    }
    revalidate();
    repaint();
  }

  /**
   * Updates the background color of all blank cells (i.e. cells that are not
   * GUIDayCellPanel instances) to the specified color. Also saves the color so
   * that future calls to updateMonth use that color for blank cells.
   *
   * @param color the new background color for blank cells.
   */
  public void updateBlankCellBackground(Color color) {
    this.blankCellColor = color; // Save for future updateMonth calls.
    // Iterate through all components excluding the header row.
    Component[] components = getComponents();
    // Header row occupies the first 7 components.
    for (int i = 7; i < components.length; i++) {
      Component comp = components[i];
      if (comp instanceof JPanel && !(comp instanceof GUIDayCellPanel)) {
        comp.setBackground(color);
      }
    }
    revalidate();
    repaint();
  }

  /**
   * Highlights a specific day among the day cells.
   *
   * @param dayOfMonth the day number to highlight.
   */
  public void highlightDay(int dayOfMonth) {
    for (GUIDayCellPanel cell : dayCells) {
      cell.setHighlighted(cell.getDayNumber() == dayOfMonth);
    }
    repaint();
  }

  /**
   * Returns the list of day cells.
   *
   * @return a list of GUIDayCellPanel instances.
   */
  public List<GUIDayCellPanel> getDayCells() {
    return dayCells;
  }
}
