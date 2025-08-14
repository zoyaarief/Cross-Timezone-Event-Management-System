package view;

import controller.ICalendarGUIFeatures;
import model.IEventModel;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A dialog that displays all events for a specific day in a single panel.
 * The user can click the "+" button to add a new event, or select an existing
 * event from the list to edit it.
 */
public class GUIDayEventsDialogBox extends JDialog {

  private final LocalDate currentDate;
  private final ICalendarGUIFeatures features;

  // Center list components.
  private JList<String> eventsList;
  private DefaultListModel<String> listModel;

  /**
   * Constructs a GUIDayEventsDialogBox that displays the events for a given day.
   *
   * @param parent   the parent frame for modality.
   * @param date     the date for which events are shown.
   * @param features the GUI features for event management.
   */
  public GUIDayEventsDialogBox(Frame parent, LocalDate date,
                               ICalendarGUIFeatures features) {
    super(parent, "Events on " + date.toString(), true); // Modal dialog.
    this.currentDate = date;
    this.features = features;
    setLayout(new BorderLayout(20, 10));

    // Build top panel with date label and add event button.
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    // Top panel components.
    JLabel dateLabel = new JLabel(date.toString());
    JButton addEventButton = new JButton("+");
    addEventButton.setToolTipText("Create a new event for this day");
    topPanel.add(dateLabel);
    topPanel.add(addEventButton);

    // Build center panel with events list.
    listModel = new DefaultListModel<>();
    eventsList = new JList<>(listModel);
    eventsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane scrollPane = new JScrollPane(eventsList);

    add(topPanel, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);

    // Set up button and list selection listeners.
    addEventButton.addActionListener(e -> onAddEvent());
    eventsList.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        int idx = eventsList.getSelectedIndex();
        if (idx >= 0) {
          onSelectEvent(idx);
        }
      }
    });

    // Load the events.
    refreshEventsList();
    pack();
    setLocationRelativeTo(parent);
  }

  /**
   * Refreshes the list of events by retrieving events for the current date
   * from the features controller.
   */
  private void refreshEventsList() {
    listModel.clear();
    List<IEventModel> events = features.getEventsForDay(
            currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    for (IEventModel ev : events) {
      listModel.addElement(ev.getEventName());
    }
  }

  /**
   * Handles the action when the user clicks the add-event button.
   * Opens a new event editor dialog and then refreshes the events list.
   */
  private void onAddEvent() {
    // Open a new event editor in "new event" mode.
    GUIViewEventDialogBox editor = new GUIViewEventDialogBox(this, currentDate,
            null, features);
    editor.setVisible(true);
    // Refresh the list after editor closes.
    refreshEventsList();
  }

  /**
   * Handles the action when the user selects an event from the list.
   * Opens the event editor dialog in "edit" mode and refreshes the list after
   * editing.
   *
   * @param index the index of the selected event in the list.
   */
  private void onSelectEvent(int index) {
    List<IEventModel> events = features.getEventsForDay(
            currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    if (index < 0 || index >= events.size()) {
      return;
    }
    IEventModel ev = events.get(index);
    // Open an editor in "edit" mode.
    GUIViewEventDialogBox editor = new GUIViewEventDialogBox(this, currentDate, ev,
            features);
    editor.setVisible(true);
    // Refresh the list after editing.
    refreshEventsList();
  }
}
