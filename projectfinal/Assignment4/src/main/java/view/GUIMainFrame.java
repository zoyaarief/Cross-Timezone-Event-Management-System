package view;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

/**
 * The main frame for the Calendar App GUI. It contains the navigation panel,
 * month view panel, and a message area.
 */
public class GUIMainFrame extends JFrame {

  private JFrame frame;
  private GUINavigationPanel navPanel;
  private GUIMonthViewPanel monthPanel;
  private JTextArea messageArea;

  /**
   * Constructs the main frame with sub-panels and message area.
   *
   * @param title the window title
   */
  public GUIMainFrame(String title) {
    // Create the main frame.
    frame = new JFrame(title);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(800, 600);
    frame.setLayout(new BorderLayout());

    // Create navigation and month view panels.
    navPanel = new GUINavigationPanel();
    monthPanel = new GUIMonthViewPanel();

    // Create the message area.
    messageArea = new JTextArea(4, 80);
    messageArea.setEditable(false);
    JScrollPane messageScroll = new JScrollPane(messageArea);

    // Add components to the frame.
    frame.add(navPanel, BorderLayout.NORTH);
    frame.add(monthPanel, BorderLayout.CENTER);
    frame.add(messageScroll, BorderLayout.SOUTH);

    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    frame.setVisible(true);
  }

  /**
   * Returns the underlying JFrame.
   *
   * @return the constructed JFrame
   */
  public JFrame getFrame() {
    return frame;
  }

  /**
   * Makes the frame visible.
   */
  public void showFrame() {
    frame.setVisible(true);
  }

  /**
   * Returns the navigation panel.
   *
   * @return the navigation panel
   */
  public GUINavigationPanel getNavPanel() {
    return navPanel;
  }

  /**
   * Returns the month view panel.
   *
   * @return the month view panel
   */
  public GUIMonthViewPanel getMonthPanel() {
    return monthPanel;
  }

  /**
   * Returns the text area for displaying messages.
   *
   * @return the message text area
   */
  public JTextArea getMessageArea() {
    return messageArea;
  }
}
