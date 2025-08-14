package view;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * A simple modal dialog that lets the user enter a new calendar's name and
 * time zone.
 */
public class GUICreateCalendarDialogBox extends JDialog {

  private JTextField nameField;
  private JTextField timezoneField;

  // Stored user inputs for retrieval by the caller.
  private String calendarName;
  private String timeZoneInput;
  private boolean confirmed;

  /**
   * Constructs a GUICreateCalendarDialogBox for creating a new calendar.
   *
   * @param parentFrame the parent frame over which the dialog is centered.
   */
  public GUICreateCalendarDialogBox(Frame parentFrame) {
    super(parentFrame, "Create New Calendar", true);
    setLayout(new BorderLayout());

    // Top panel: instructions.
    JLabel instructions = new JLabel("Enter calendar name and timezone:");
    add(instructions, BorderLayout.NORTH);

    // Center panel: input fields.
    JPanel centerPanel = new JPanel(new GridLayout(2, 2, 5, 5));
    centerPanel.add(new JLabel("Calendar Name:"));
    nameField = new JTextField();
    centerPanel.add(nameField);
    centerPanel.add(new JLabel("Timezone (Area/Location):"));
    timezoneField = new JTextField("America/New_York");
    centerPanel.add(timezoneField);
    add(centerPanel, BorderLayout.CENTER);

    // Bottom panel: action buttons.
    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton addButton = new JButton("Add");
    JButton cancelButton = new JButton("Cancel");
    bottomPanel.add(cancelButton);
    bottomPanel.add(addButton);
    add(bottomPanel, BorderLayout.SOUTH);

    // Listeners for add and cancel actions.
    addButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        calendarName = nameField.getText().trim();
        timeZoneInput = timezoneField.getText().trim();
        confirmed = true;
        dispose(); // close dialog
      }
    });
    cancelButton.addActionListener(e -> {
      confirmed = false;
      dispose();
    });

    pack();
    setLocationRelativeTo(parentFrame); // center over parent
  }

  /**
   * Returns whether the user confirmed the input.
   *
   * @return true if the add button was pressed; false otherwise.
   */
  public boolean isConfirmed() {
    return confirmed;
  }

  /**
   * Returns the calendar name entered by the user.
   *
   * @return the entered calendar name.
   */
  public String getCalendarName() {
    return calendarName;
  }

  /**
   * Returns the timezone input entered by the user.
   *
   * @return the entered timezone.
   */
  public String getTimeZoneInput() {
    return timeZoneInput;
  }
}
