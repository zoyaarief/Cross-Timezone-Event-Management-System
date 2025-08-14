import controller.CalenderControllerImpl;
import controller.ICalenderController;
import model.CalendarManager;
import view.CalenderViewImpl;
import view.ICalenderView;
import view.GUICalendarViewImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * Main application class for the Calendar App.
 * It initializes the model, view, and controller, then starts the app.
 */
public class CalenderApp {

  /**
   * Main method to run the Calendar application.
   * Any other usage => error message & quit.
   */
  public static void main(String[] args) {

    // If no arguments => GUI mode
    if (args.length == 0) {
      runGuiMode();
      return;
    }

    // If arguments exist, check for "--mode" ...
    if (args.length >= 2 && "--mode".equalsIgnoreCase(args[0])) {
      String mode = args[1].toLowerCase(Locale.ROOT);

      switch (mode) {
        case "interactive":
          // Must have exactly 2 args: ["--mode", "interactive"]
          if (args.length != 2) {
            System.out.println("Invalid usage. " +
                    "For interactive mode: java -jar Program.jar --mode interactive");
            System.exit(1);
          }
          runInteractiveMode();
          break;

        case "headless":
          // Must have exactly 3 args: ["--mode", "headless", <script-file>]
          if (args.length != 3) {
            System.out.println("Invalid usage. " +
                    "For headless mode: java -jar Program.jar --mode headless <script-file>");
            System.exit(1);
          }
          runHeadlessMode(args[2]);
          break;

        default:
          // Invalid mode => error
          System.out.println("Error: Invalid mode. Use 'interactive' or 'headless'.");
          System.exit(1);
      }

    } else {
      // If user provided some arguments but not in the correct form => error
      System.out.println("Error: Invalid command-line arguments.");
      System.out.println("Valid:");
      System.out.println("  java -jar Program.jar                 => GUI mode");
      System.out.println("  java -jar Program.jar --mode interactive");
      System.out.println("  java -jar Program.jar --mode headless <script-file>");
      System.exit(1);
    }
  }

  /**
   * Runs the GUI mode.
   */
  private static void runGuiMode() {
    CalendarManager manager = new CalendarManager();
    ICalenderView view = new GUICalendarViewImpl(); // your new Swing-based view
    // We do not really need System.in for GUI, but we can pass a dummy:
    ICalenderController controller =
            new CalenderControllerImpl(manager, new InputStreamReader(System.in));
    controller.setView(view);
    controller.execute();
  }

  /**
   * Runs the interactive console mode.
   */
  private static void runInteractiveMode() {
    CalendarManager manager = new CalendarManager();
    ICalenderView view = new CalenderViewImpl(); // your old console-based view
    ICalenderController controller =
            new CalenderControllerImpl(manager, new InputStreamReader(System.in));
    controller.setView(view);
    controller.execute();
  }

  /**
   * Runs the headless mode with commands from a script file.
   * @param filename the path to the script file
   */
  private static void runHeadlessMode(String filename) {
    CalendarManager manager = new CalendarManager();
    ICalenderView view = new CalenderViewImpl(); // console or a quiet version
    try {
      FileInputStream fileInputStream = new FileInputStream(new File(filename));
      ICalenderController controller =
              new CalenderControllerImpl(manager, new InputStreamReader(fileInputStream));
      controller.setView(view);
      controller.execute();
    } catch (FileNotFoundException e) {
      System.out.println("Error: File not found - " + filename);
      System.exit(1);
    }
  }
}
