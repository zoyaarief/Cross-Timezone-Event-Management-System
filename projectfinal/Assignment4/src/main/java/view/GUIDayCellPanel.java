package view;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;

/**
 * A panel representing a single day cell in the calendar view.
 * It displays the day number and can be highlighted.
 */
public class GUIDayCellPanel extends JPanel {
  private int dayNumber;

  /**
   * Constructs a GUIDayCellPanel with the specified day number.
   *
   * @param dayNumber the day number to display on the cell.
   */
  public GUIDayCellPanel(int dayNumber) {
    this.dayNumber = dayNumber;
    setLayout(new BorderLayout());
    JLabel dayLabel = new JLabel(String.valueOf(dayNumber), SwingConstants.CENTER);
    add(dayLabel, BorderLayout.CENTER);
    setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
  }

  /**
   * Returns the day number displayed in this cell.
   *
   * @return the day number.
   */
  public int getDayNumber() {
    return dayNumber;
  }

  /**
   * Sets whether the cell should be highlighted.
   * When highlighted, the border color changes to blue.
   *
   * @param highlighted true to highlight the cell, false to remove highlighting.
   */
  public void setHighlighted(boolean highlighted) {
    if (highlighted) {
      setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
    } else {
      setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
    }
  }
}
