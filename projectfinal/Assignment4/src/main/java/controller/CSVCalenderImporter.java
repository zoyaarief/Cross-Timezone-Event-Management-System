package controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import model.EventModelImpl;
import model.EventStatus;
import model.IEventModel;
import model.InvalidCalenderOperationException;

/**
 * CSV importer for calendar events.
 *
 */
public class CSVCalenderImporter {

  // Expected headers in order. Only the first two are required.
  private static final String[] EXPECTED_HEADERS = {
      "Subject", "Start Date", "Start Time", "End Date", "End Time",
      "All Day Event", "Description", "Location", "Private"
  };

  // Input formats for date and time.
  private static final DateTimeFormatter INPUT_DATE_FORMAT =
          DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter INPUT_TIME_FORMAT =
          DateTimeFormatter.ofPattern("hh:mm a");
  // Output format for building events.
  private static final DateTimeFormatter OUTPUT_DATE_TIME_FORMAT =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  /**
   * Parses a CSV line into tokens while handling quoted values.
   * This parser toggles inQuotes upon encountering double quotes.
   *
   */
  private List<String> parseCSVLine(String line) {
    List<String> tokens = new ArrayList<>();
    if (line == null || line.isEmpty()) {
      return tokens;
    }
    StringBuilder token = new StringBuilder();
    boolean inQuotes = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (c == '"') {
        inQuotes = !inQuotes;
      } else if (c == ',' && !inQuotes) {
        tokens.add(token.toString().trim());
        token.setLength(0);
      } else {
        token.append(c);
      }
    }
    tokens.add(token.toString().trim());
    return tokens;
  }

  /**
   * Validates the header line of the CSV file.
   * Only the first two headers ("Subject" and "Start Date") are required.
   * If additional headers are provided, they must match the expected names.
   *
   * @param headerLine the header line from the CSV file.
   * @throws InvalidCalenderOperationException if the header does not meet
   *         requirements.
   */
  private void validateHeader(String headerLine)
          throws InvalidCalenderOperationException {
    List<String> headers = parseCSVLine(headerLine);
    if (headers.size() < 2) {
      throw new InvalidCalenderOperationException(
              "CSV header must have at least 2 columns: Subject and Start Date.");
    }
    // Check required headers.
    if (!headers.get(0).equalsIgnoreCase("Subject") ||
            !headers.get(1).equalsIgnoreCase("Start Date")) {
      throw new InvalidCalenderOperationException(
              "CSV header must start with 'Subject,Start Date'.");
    }
    // Check optional headers if present.
    for (int i = 2; i < headers.size() && i < EXPECTED_HEADERS.length; i++) {
      if (!headers.get(i).equalsIgnoreCase(EXPECTED_HEADERS[i])) {
        throw new InvalidCalenderOperationException(
                "CSV header at position " + (i + 1) +
                        " must be '" + EXPECTED_HEADERS[i] + "'.");
      }
    }
  }

  /**
   * Imports events from a CSV file.
   *
   */
  public List<IEventModel> importCalendar(String filename, ZoneId zone)
          throws InvalidCalenderOperationException {
    List<IEventModel> events = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
      String headerLine = br.readLine();
      if (headerLine == null) {
        throw new InvalidCalenderOperationException("CSV file is empty.");
      }
      // Validate the header format.
      validateHeader(headerLine);

      String line;
      int lineNumber = 1;
      while ((line = br.readLine()) != null) {
        lineNumber++;
        List<String> tokens = parseCSVLine(line);
        // Require at least the two required fields.
        if (tokens.size() < 2) {
          System.err.println("Line " + lineNumber +
                  " skipped: Missing required fields.");
          continue;
        }
        String subject = tokens.get(0);
        String startDateStr = tokens.get(1);
        if (subject.isEmpty() || startDateStr.isEmpty()) {
          System.err.println("Line " + lineNumber +
                  " skipped: Required fields are empty.");
          continue;
        }
        // Parse the Start Date.
        LocalDate startDate;
        try {
          startDate = LocalDate.parse(startDateStr, INPUT_DATE_FORMAT);
        } catch (Exception e) {
          System.err.println("Line " + lineNumber +
                  " skipped: Invalid Start Date format.");
          continue;
        }
        // Parse Start Time if available; default to midnight if not.
        LocalTime startTime = LocalTime.MIDNIGHT;
        if (tokens.size() >= 3 && !tokens.get(2).isEmpty()) {
          try {
            startTime = LocalTime.parse(tokens.get(2), INPUT_TIME_FORMAT);
          } catch (Exception e) {
            System.err.println("Line " + lineNumber +
                    " skipped: Invalid Start Time format.");
            continue;
          }
        }
        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
        String formattedStartDateTime =
                startDateTime.format(OUTPUT_DATE_TIME_FORMAT);

        // Optional End Date and End Time.
        String formattedEndDateTime = null;
        if (tokens.size() >= 5 && !tokens.get(3).isEmpty() &&
                !tokens.get(4).isEmpty()) {
          try {
            LocalDate endDate = LocalDate.parse(tokens.get(3), INPUT_DATE_FORMAT);
            LocalTime endTime = LocalTime.parse(tokens.get(4), INPUT_TIME_FORMAT);
            LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);
            formattedEndDateTime =
                    endDateTime.format(OUTPUT_DATE_TIME_FORMAT);
          } catch (Exception e) {
            System.err.println("Line " + lineNumber +
                    " skipped: Invalid End Date/Time format.");
            continue;
          }
        }
        // Optional All Day Event: must be either True or False if provided.
        if (tokens.size() >= 6 && !tokens.get(5).isEmpty()) {
          String allDayStr = tokens.get(5);
          if (!allDayStr.equalsIgnoreCase("True") &&
                  !allDayStr.equalsIgnoreCase("False")) {
            System.err.println("Line " + lineNumber +
                    " skipped: Invalid All Day Event value.");
            continue;
          }
        }
        // Optional Description.
        String description = (tokens.size() >= 7) ? tokens.get(6) : "";
        // Optional Location.
        String location = (tokens.size() >= 8) ? tokens.get(7) : "";
        // Optional Private flag: must be either True or False if provided.
        String privateStr = (tokens.size() >= 9) ? tokens.get(8) : "";
        if (!privateStr.isEmpty() && !privateStr.equalsIgnoreCase("True") &&
                !privateStr.equalsIgnoreCase("False")) {
          System.err.println("Line " + lineNumber +
                  " skipped: Invalid Private value.");
          continue;
        }
        // Determine event status based on the Private field.
        EventStatus status = EventStatus.PUBLIC;
        if (!privateStr.isEmpty() &&
                privateStr.equalsIgnoreCase("True")) {
          status = EventStatus.PRIVATE;
        }
        // Build the event using the builder pattern.
        try {
          EventModelImpl.EventBuilder builder =
                  EventModelImpl.getBuilder(subject, formattedStartDateTime, zone);
          if (formattedEndDateTime != null) {
            builder = builder.endDateString(formattedEndDateTime);
          }
          builder = builder.longDescription(description)
                  .location(location)
                  .status(status);
          IEventModel event = builder.build();
          events.add(event);
        } catch (InvalidCalenderOperationException e) {
          System.err.println("Line " + lineNumber +
                  " skipped: " + e.getMessage());
        }
      }
    } catch (IOException e) {
      throw new InvalidCalenderOperationException(
              "Failed to import CSV: " + e.getMessage());
    }
    return events;
  }
}
