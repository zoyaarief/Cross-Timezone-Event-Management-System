package view;

import controller.ICalendarGUIFeatures;
import model.EventModelImpl;
import model.IEventModel;
import model.InvalidCalenderOperationException;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Color;
import java.awt.Container;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.ButtonGroup;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;

/**
 * A dialog used to create a new event (creation mode) or display event details
 * (read‑only mode). In creation mode (existingEvent == null), all fields are editable
 * and repeat options are shown. In detail mode (existingEvent != null), event details
 * are displayed as plain bold text.
 */
public class GUIViewEventDialogBox extends JDialog {

  private final LocalDate day;                // Base day for the event.
  private final IEventModel existingEvent;    // If non-null, dialog is in detail mode.
  private final ICalendarGUIFeatures features; // Controller for calendar functions.

  // Main input fields (creation mode only)
  private JTextField eventNameField;
  private JTextField startDateField;   // Expected format: "YYYY-MM-DD".
  private JTextField startTimeField;   // Expected format: "HH:mm".
  private JTextField endDateField;     // Expected format: "YYYY-MM-DD".
  private JTextField endTimeField;     // Expected format: "HH:mm".

  // Repeat-related components; used only in creation mode.
  private JCheckBox repeatCheck;
  private JPanel repeatPanel;
  private JCheckBox[] dowCheckBoxes;  // For days: M, T, W, R, F, S, U.
  private JRadioButton forNWeeksRadio;
  private JRadioButton untilDateRadio;
  private JTextField weeksField;      // Integer value.
  private JTextField untilDateField;  // "YYYY-MM-DD".

  // Control buttons.
  private JButton actionButton; // "Save" in creation mode or "Close" in detail mode.
  private JButton cancelButton; // Used only in creation mode.

  /**
   * Constructs the event editor dialog.
   *
   * @param parent        The parent window.
   * @param day           The base day for which this event is created or viewed.
   * @param existingEvent If non-null, shows event details in read-only mode.
   * @param features      The controller interface for calendar features.
   */
  public GUIViewEventDialogBox(Dialog parent, LocalDate day, IEventModel existingEvent,
                               ICalendarGUIFeatures features) {
    this.setModal(true);
    this.day = day;
    this.existingEvent = existingEvent;
    this.features = features;

    setTitle(existingEvent == null ? "Create New Event" : "Event Details");
    setLayout(new BorderLayout(10, 10));

    // Build and add the form panel.
    JPanel formPanel = buildFormPanel();
    add(formPanel, BorderLayout.CENTER);

    // Build and add the button panel.
    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    if (existingEvent == null) {
      actionButton = new JButton("Save");
      cancelButton = new JButton("Cancel");
      bottomPanel.add(cancelButton);
      bottomPanel.add(actionButton);
    } else {
      actionButton = new JButton("Close");
      bottomPanel.add(actionButton);
    }
    add(bottomPanel, BorderLayout.SOUTH);

    // Set up listeners.
    if (existingEvent == null) {
      cancelButton.addActionListener(e -> dispose());
    }
    actionButton.addActionListener(e -> onActionClicked());

    // In creation mode, pre-fill date fields.
    if (existingEvent == null) {
      startDateField.setText(day.toString());
      endDateField.setText(day.toString());
    }

    pack();
    setLocationRelativeTo(parent);
  }

  /**
   * Builds the form panel containing event fields. In creation mode, text fields
   * and repeat options are used. In detail mode, event details are shown as bold labels.
   *
   * @return a JPanel containing the event form.
   */
  private JPanel buildFormPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    if (existingEvent != null) {
      // Detail (read‑only) mode: Build a panel with bold labels.
      JPanel detailPanel = new JPanel(new GridLayout(5, 2, 5, 5));
      detailPanel.setBorder(BorderFactory.createTitledBorder("Event Basic Info"));
      Font boldFont = UIManager.getFont("Label.font").deriveFont(Font.BOLD);

      // Event Name.
      detailPanel.add(new JLabel("Event Name:"));
      JLabel eventNameLabel = new JLabel(existingEvent.getEventName());
      eventNameLabel.setFont(boldFont);
      detailPanel.add(eventNameLabel);

      // Start Date.
      detailPanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
      JLabel startDateLabel = new JLabel(day.toString());
      startDateLabel.setFont(boldFont);
      detailPanel.add(startDateLabel);

      // Start Time.
      SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
      String formattedStartTime = timeFormat.format(existingEvent.getStartDateTime());
      detailPanel.add(new JLabel("Start Time (HH:mm):"));
      JLabel startTimeLabel = new JLabel(formattedStartTime);
      startTimeLabel.setFont(boldFont);
      detailPanel.add(startTimeLabel);

      // End Date.
      detailPanel.add(new JLabel("End Date (YYYY-MM-DD):"));
      JLabel endDateLabel = new JLabel(day.toString());
      endDateLabel.setFont(boldFont);
      detailPanel.add(endDateLabel);

      // End Time.
      String formattedEndTime = timeFormat.format(existingEvent.getEndDateTime());
      detailPanel.add(new JLabel("End Time (HH:mm):"));
      JLabel endTimeLabel = new JLabel(formattedEndTime);
      endTimeLabel.setFont(boldFont);
      detailPanel.add(endTimeLabel);

      panel.add(detailPanel);
    } else {
      // Creation mode: Build a panel with text fields.
      JPanel mainFields = new JPanel(new GridLayout(5, 2, 5, 5));
      mainFields.setBorder(BorderFactory.createTitledBorder("Event Basic Info"));

      mainFields.add(new JLabel("Event Name:"));
      eventNameField = new JTextField();
      mainFields.add(eventNameField);

      mainFields.add(new JLabel("Start Date (YYYY-MM-DD):"));
      startDateField = new JTextField();
      mainFields.add(startDateField);

      mainFields.add(new JLabel("Start Time (HH:mm):"));
      startTimeField = new JTextField();
      mainFields.add(startTimeField);

      mainFields.add(new JLabel("End Date (YYYY-MM-DD):"));
      endDateField = new JTextField();
      mainFields.add(endDateField);

      mainFields.add(new JLabel("End Time (HH:mm):"));
      endTimeField = new JTextField();
      mainFields.add(endTimeField);

      panel.add(mainFields);

      // Add repeat options.
      repeatCheck = new JCheckBox("Repeat this event?");
      repeatCheck.setSelected(false);
      repeatPanel = buildRepeatPanel();
      repeatPanel.setVisible(true);
      setRepeatPanelEnabled(false);

      repeatCheck.addItemListener(e -> {
        boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
        setRepeatPanelEnabled(selected);
      });

      panel.add(repeatCheck);
      panel.add(repeatPanel);
    }
    return panel;
  }

  /**
   * Recursively sets the enabled state of all components in the repeat panel.
   *
   * @param enabled true to enable components; false to disable.
   */
  private void setRepeatPanelEnabled(boolean enabled) {
    for (Component comp : repeatPanel.getComponents()) {
      comp.setEnabled(enabled);
      if (comp instanceof Container) {
        setRepeatPanelEnabledRec((Container) comp, enabled);
      }
    }
  }

  /**
   * Helper method to recursively set the enabled state for container components.
   *
   * @param container the container to process.
   * @param enabled   true to enable; false to disable.
   */
  private void setRepeatPanelEnabledRec(Container container, boolean enabled) {
    for (Component comp : container.getComponents()) {
      comp.setEnabled(enabled);
      if (comp instanceof Container) {
        setRepeatPanelEnabledRec((Container) comp, enabled);
      }
    }
  }

  /**
   * Builds the repeat options sub-panel with day-of-week checkboxes and recurrence
   * termination options.
   *
   * @return a JPanel containing recurrence options.
   */
  private JPanel buildRepeatPanel() {
    JPanel rp = new JPanel();
    rp.setLayout(new BoxLayout(rp, BoxLayout.Y_AXIS));
    rp.setBorder(BorderFactory.createTitledBorder("Recurring Options"));

    // Day-of-week checkboxes.
    JPanel daysPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    daysPanel.add(new JLabel("Repeat on:"));
    dowCheckBoxes = new JCheckBox[7];
    String labels = "MTWRFSU"; // M=Mon, T=Tue, etc.
    for (int i = 0; i < 7; i++) {
      dowCheckBoxes[i] = new JCheckBox(String.valueOf(labels.charAt(i)));
      daysPanel.add(dowCheckBoxes[i]);
    }
    rp.add(daysPanel);

    // Radio buttons for recurrence termination.
    JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    ButtonGroup bg = new ButtonGroup();
    forNWeeksRadio = new JRadioButton("Repeat for N weeks");
    untilDateRadio = new JRadioButton("Repeat until date");
    bg.add(forNWeeksRadio);
    bg.add(untilDateRadio);
    radioPanel.add(forNWeeksRadio);
    radioPanel.add(untilDateRadio);
    rp.add(radioPanel);

    // Fields for specifying termination details.
    JPanel fieldsPanel = new JPanel(new GridLayout(1, 4, 5, 5));
    fieldsPanel.add(new JLabel("N weeks:"));
    weeksField = new JTextField();
    fieldsPanel.add(weeksField);
    fieldsPanel.add(new JLabel("Until date (YYYY-MM-DD):"));
    untilDateField = new JTextField();
    fieldsPanel.add(untilDateField);
    rp.add(fieldsPanel);

    forNWeeksRadio.addItemListener(e -> {
      if (forNWeeksRadio.isSelected()) {
        weeksField.setEnabled(true);
        untilDateField.setEnabled(false);
      }
    });
    untilDateRadio.addItemListener(e -> {
      if (untilDateRadio.isSelected()) {
        weeksField.setEnabled(false);
        untilDateField.setEnabled(true);
      }
    });

    weeksField.setEnabled(false);
    untilDateField.setEnabled(false);

    return rp;
  }

  /**
   * Handles the action when the main button is clicked. In detail mode, the
   * dialog is closed. In creation mode, inputs are validated and a new event is
   * created, handling repeat options if selected.
   */
  private void onActionClicked() {
    if (existingEvent != null) {
      dispose();
      return;
    }
    resetFieldBackgrounds();
    String name = eventNameField.getText().trim();
    String sDate = startDateField.getText().trim();
    String sTime = startTimeField.getText().trim();
    String eDate = endDateField.getText().trim();
    String eTime = endTimeField.getText().trim();

    boolean anyError = false;
    if (name.isEmpty()) {
      eventNameField.setBackground(Color.PINK);
      anyError = true;
    }
    if (sDate.isEmpty()) {
      startDateField.setBackground(Color.PINK);
      anyError = true;
    }
    if (sTime.isEmpty()) {
      startTimeField.setBackground(Color.PINK);
      anyError = true;
    }
    if (eDate.isEmpty()) {
      endDateField.setBackground(Color.PINK);
      anyError = true;
    }
    if (eTime.isEmpty()) {
      endTimeField.setBackground(Color.PINK);
      anyError = true;
    }
    if (anyError) {
      JOptionPane.showMessageDialog(this, "Please fill all required fields.",
              "Validation Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    boolean isRepeat = repeatCheck != null && repeatCheck.isSelected();
    String repeatDays = "";
    if (isRepeat) {
      String labels = "MTWRFSU";
      StringBuilder sb = new StringBuilder();
      int selectedCount = 0;
      for (int i = 0; i < dowCheckBoxes.length; i++) {
        if (dowCheckBoxes[i].isSelected()) {
          sb.append(labels.charAt(i));
          selectedCount++;
        }
      }
      repeatDays = sb.toString();
      if (selectedCount == 0) {
        JOptionPane.showMessageDialog(this,
                "Please select at least one repeat day.",
                "Missing Repeat Days", JOptionPane.WARNING_MESSAGE);
        return;
      }
      if (!forNWeeksRadio.isSelected() && !untilDateRadio.isSelected()) {
        JOptionPane.showMessageDialog(this,
                "Please choose a repeat termination option.",
                "Missing Repeat Option", JOptionPane.WARNING_MESSAGE);
        return;
      }
      if (forNWeeksRadio.isSelected() && weeksField.getText().trim().isEmpty()) {
        weeksField.setBackground(Color.PINK);
        JOptionPane.showMessageDialog(this,
                "Please specify the number of weeks.",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
        return;
      }
      if (untilDateRadio.isSelected() && untilDateField.getText().trim().isEmpty()) {
        untilDateField.setBackground(Color.PINK);
        JOptionPane.showMessageDialog(this,
                "Please specify an end date.",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
        return;
      }
    }

    try {
      IEventModel newEvent = EventModelImpl.getBuilder(name,
                      sDate + "T" + sTime, features.getTimeZone())
              .endDateString(eDate + "T" + eTime)
              .build();
      if (!isRepeat) {
        features.addEventToCalender(newEvent, true);
        JOptionPane.showMessageDialog(this, "New event created!");
      } else {
        if (forNWeeksRadio.isSelected()) {
          int nWeeks = Integer.parseInt(weeksField.getText().trim());
          features.addRecurringEventToCalender(newEvent, true, repeatDays, nWeeks);
          JOptionPane.showMessageDialog(this,
                  "New recurring event created for " + nWeeks + " weeks.");
        } else {
          String until = untilDateField.getText().trim();
          features.addRecurringEventToCalender(newEvent, true, repeatDays, until);
          JOptionPane.showMessageDialog(this,
                  "New recurring event created until " + until + ".");
        }
      }
      dispose();
    } catch (InvalidCalenderOperationException | NumberFormatException ex) {
      JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
              "Event Editor Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Resets the background colors of input fields to white.
   */
  private void resetFieldBackgrounds() {
    eventNameField.setBackground(Color.WHITE);
    startDateField.setBackground(Color.WHITE);
    startTimeField.setBackground(Color.WHITE);
    endDateField.setBackground(Color.WHITE);
    endTimeField.setBackground(Color.WHITE);
    if (weeksField != null) {
      weeksField.setBackground(Color.WHITE);
    }
    if (untilDateField != null) {
      untilDateField.setBackground(Color.WHITE);
    }
  }
}
