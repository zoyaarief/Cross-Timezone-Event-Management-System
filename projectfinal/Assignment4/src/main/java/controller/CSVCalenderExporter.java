package controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import model.IEventModel;
import model.InvalidCalenderOperationException;

/**
 * Exports calendar data to CSV format.
 */
public class CSVCalenderExporter implements CalenderExporter {

  @Override
  public String exportCalendar(List<IEventModel> events, String filename)
          throws InvalidCalenderOperationException {
    File csvFile = new File(filename).getAbsoluteFile();
    try (PrintWriter writer = new PrintWriter(csvFile)) {
      writer.println("Subject,Start Date,Start Time,End Date,End Time,"
              + "All Day Event,Description,Location,Private");
      for (IEventModel event : events) {
        LocalDateTime ldtStart = event.getStartDateTime().toInstant()
                .atZone(ZoneId.of("America/New_York")).toLocalDateTime();
        LocalDateTime ldtEnd = (event.getEndDateTime() == null)
                ? ldtStart : event.getEndDateTime().toInstant()
                .atZone(ZoneId.of("America/New_York")).toLocalDateTime();
        String subject = event.getEventName().replace(",", " ");
        String startDate = ldtStart.toLocalDate().toString();
        String startTime = ldtStart.toLocalTime().toString();
        String endDate = ldtEnd.toLocalDate().toString();
        String endTime = ldtEnd.toLocalTime().toString();
        String allDay = "False";  // Adjust if needed for all-day logic.
        String description = "";  // Populate if available.
        String location = "";     // Populate if available.
        String isPrivate = "False";  // Adjust if needed.
        writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                subject, startDate, startTime,
                endDate, endTime, allDay,
                description, location, isPrivate);
      }
    } catch (IOException e) {
      throw new InvalidCalenderOperationException("Failed to export CSV: "
              + e.getMessage());
    }
    return csvFile.getAbsolutePath();
  }
}
