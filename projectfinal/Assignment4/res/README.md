##Virtual Calendar Application

This project is a virtual calendar application designed to mimic features found in popular calendar apps like Google Calendar and Apple iCalendar. The application allows users to create, edit, and manage calendar events via a text-based interface, and now supports multiple calendars and timezones. New commands have been added to support these features.

##How to Run the Program
Running interactive mode 

Open the Command Prompt/Terminal.

Navigate to the Directory:

Use the cd command to change to the directory where your JAR file is located.

bash
Copy
cd /path/to/jarfile
Run the JAR File:

Execute the following command:

bash
Copy
java -jar NameOfJARFile.jar

Alternatively, you can double-click the JAR file if your system is configured to run Java applications.

Running in Headless Mode
A text file containing valid commands will be used to execute operations. The application will read commands from this file and execute them accordingly.

##Features
#Supported Commands

Event Commands
Create Single Event:
Create Recurring Event (with repetition)
Create Recurring Event (until a specific date)
Create All-Day Event:
Create Recurring All-Day Event:
Edit Event Commands
Print and Export Event Commands
Show Status

##New Calendar and Timezone Commands
Create Calendar:
Edit Calendar:
Use Calendar 
New Copy Event Commands
	Copy a Single Event:
	Copy Events on a Specific Date:
	Copy Events Between Two Dates:


##Design Enhancements and Justifications

In order to support the new features and improve overall flexibility, we have made several design changes and enhancements. The major changes include:

#Creation of an Abstract DateTime Class:

Why: To encapsulate date and time handling in a unified manner, making it easier to extend or modify time-based operations in the future.

Justification: This change improves code reusability and maintainability, and ensures that date/time functionalities are consistent across the application.

#Moving Export Functionality to the Controller:

Why: To separate concerns more clearly between the view and the controller.

Justification: This adheres better to the MVC architecture by ensuring that file operations (such as exporting to CSV) are handled by the controller, leaving the model to focus on data logic and the view on presentation.

#Fixing the Searching Algorithm:

Why: To improve performance and accuracy when querying events.

Justification: A more efficient and robust search algorithm ensures better user experience and reliability, especially as the calendar grows in size.

#Removing the --autoDecline Option:

Why: Conflicts are now handled by default, eliminating the need for the user to specify this behavior.

Justification: This simplification reduces the potential for user error and streamlines the command syntax.

#Supporting Multiple Calendars and Timezones:

Why: Users now have the flexibility to manage multiple calendars and specify different timezones for each.

Justification: This enhancement not only meets user demand for better organization but also lays the groundwork for future extensions, such as integration with external calendar systems.

#Default Calendar Context:

Why: If the user does not specify a calendar using the use calendar command, the system will default to the most recently created calendar.

Justification: This design choice simplifies the user experience by reducing the number of required commands and preventing errors due to an unspecified calendar context.


##Authors
#Contributors:

Siddhant Narode

Zoya Arief

##Author Contributions
Design: Zoya Arief and Siddhant Narode

Controller: Siddhant and Zoya

Edit commands: Zoya Arief

Create commands: Siddhant Narode

Print command: Siddhant Narode

Model:

Edit commands: Zoya Arief

Create commands: Siddhant Narode

Print command: Siddhant Narode

Create, Copy Calendar Commands: Siddhant

Edit, Use Calendar Commands: Zoya

Testing:

Controller tests: Zoya Arief

Model tests: Siddhant Narode

View tests: Siddhant Narode

Integration tests: Zoya Arief


##Command Defaults:

If the user does not issue a use calendar command, the application defaults to the most recently created calendar.



