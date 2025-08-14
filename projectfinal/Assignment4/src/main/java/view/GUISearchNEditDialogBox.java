package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;

/**
 * This dialog is opened when the "Edit" button is clicked. It provides search
 * functionality and fields for editing events.
 */
class GUISearchNEditDialogBox extends JDialog {
  private JTextField eventNameField;
  private JTextField startDateTimeField;
  private JTextField endDateTimeField;
  private JComboBox<String> propertyDropdown;
  private JTextField newValueField;
  private boolean confirmed;

  private Date startDateTime;
  private Date endDateTime;
  private String propertyName;
  private String newValue;

  private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm";

  /**
   * Constructs a GUISearchNEditDialogBox dialog for searching and editing events.
   */
  public GUISearchNEditDialogBox() {
    setTitle("Search and Edit Events");
    setModal(true);
    setSize(400, 300);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout(10, 10));

    // Create a panel for the search fields with a grid layout.
    JPanel searchPanel = new JPanel(new GridLayout(4, 2, 5, 5));
    searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    searchPanel.add(new JLabel("Event Name (required):"));
    eventNameField = new JTextField();
    searchPanel.add(eventNameField);

    searchPanel.add(new JLabel("Start DateTime (optional):"));
    startDateTimeField = new JTextField();
    searchPanel.add(startDateTimeField);

    searchPanel.add(new JLabel("End DateTime (optional):"));
    endDateTimeField = new JTextField();
    searchPanel.add(endDateTimeField);

    // Dropdown for property selection.
    searchPanel.add(new JLabel("Property to Edit:"));
    propertyDropdown = new JComboBox<>(
            new String[] { "eventname", "startdatetime", "enddatetime" });
    searchPanel.add(propertyDropdown);

    // Create a panel for entering the new value.
    JPanel newValuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    newValuePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
    newValuePanel.add(new JLabel("New Value:"));
    newValueField = new JTextField(20);
    newValuePanel.add(newValueField);

    // Create a panel for the edit button.
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton editEventButton = new JButton("Edit");
    buttonPanel.add(editEventButton);

    // Add panels to the dialog.
    add(searchPanel, BorderLayout.NORTH);
    add(newValuePanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);
    confirmed = false;

    // Action listener for the edit button.
    editEventButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        handleEditAction();
      }
    });
  }

  /**
   * Validates input fields according to the allowed cases: (1) Event name only,
   * (2) Event name with a start datetime, (3) Event name with start and end
   * datetime. Also validates that if end datetime is provided, start datetime cannot
   * be empty.
   */
  private void handleEditAction() {
    String eventName = eventNameField.getText().trim();
    String startText = startDateTimeField.getText().trim();
    String endText = endDateTimeField.getText().trim();
    newValue = newValueField.getText().trim();
    propertyName = (String) propertyDropdown.getSelectedItem();

    // Validate that Event Name is provided.
    if (eventName.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Event Name is required.", "Error",
              JOptionPane.ERROR_MESSAGE);
      return;
    }
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
    sdf.setLenient(false);
    // Attempt to parse the start datetime if provided.
    if (!startText.isEmpty()) {
      try {
        startDateTime = sdf.parse(startText);
      } catch (ParseException ex) {
        JOptionPane.showMessageDialog(this,
                "Invalid Start DateTime format. Please use " + DATE_FORMAT, "Error",
                JOptionPane.ERROR_MESSAGE);
        return;
      }
    } else {
      startDateTime = null;
    }
    // Attempt to parse the end datetime if provided.
    if (!endText.isEmpty()) {
      try {
        endDateTime = sdf.parse(endText);
      } catch (ParseException ex) {
        JOptionPane.showMessageDialog(this,
                "Invalid End DateTime format. Please use " + DATE_FORMAT, "Error",
                JOptionPane.ERROR_MESSAGE);
        return;
      }
    } else {
      endDateTime = null;
    }
    // Validate that if end datetime is provided, start datetime must be provided.
    if (startDateTime == null && endDateTime != null) {
      JOptionPane.showMessageDialog(this,
              "Start DateTime must be provided if End DateTime is given.", "Error",
              JOptionPane.ERROR_MESSAGE);
      return;
    }
    confirmed = true;
    this.dispose();
  }

  /**
   * Returns whether the dialog was confirmed.
   *
   * @return true if the edit button was clicked and inputs validated,
   *         false otherwise.
   */
  public boolean isConfirmed() {
    return confirmed;
  }

  /**
   * Returns the entered event name.
   *
   * @return the event name.
   */
  public String getEventName() {
    return eventNameField.getText().trim();
  }

  /**
   * Returns the parsed start datetime, or null if not provided.
   *
   * @return the start datetime as a Date object.
   */
  public Date getStartDateTime() {
    return startDateTime;
  }

  /**
   * Returns the parsed end datetime, or null if not provided.
   *
   * @return the end datetime as a Date object.
   */
  public Date getEndDateTime() {
    return endDateTime;
  }

  /**
   * Returns the new value entered for the property being edited.
   *
   * @return the new property value.
   */
  public String getNewValue() {
    return newValue;
  }

  /**
   * Returns the selected property name to edit.
   *
   * @return the property name.
   */
  public String getSelectedProperty() {
    return propertyName;
  }
}
