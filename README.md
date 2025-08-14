# Cross Timezone Event Management Application 

This project is for designing and implementing a **Cross Timezone Event Management Application**. The idea is to mimic features found on widely used calendar apps such as Google Calendar or Apple's iCalendar app.



## How to run the program:

-   **Open the Command Prompt:**
    

-   **Navigate to the Directory:**
    
    -   Use the  `cd`  command to change the current directory to where your Java file is saved. 
```bash
cd C:\your\path\to\file
```
- **Compile the Java File:**
    
    -   Type  `javac CalenderApp.java`  and press Enter.
    - If the compilation is successful, a  `.class`  file with the same name as your class will be created in the same directory.
```bash
javac CalenderApp.java
```
 
-   **Run the Java Program:**
    
```bash
java CalenderApp.java
```
   - If the compilation is successful, a  `.class`  file with the same name as your class will be created in the same directory.
   
  
# Features
### Supported Commands

**Create Event**

- `create event --autoDecline <eventName> from <dateStringTtimeString> to <dateStringTtimeString>`
- `create event <eventName> from <dateStringTtimeString> to <dateStringTtimeString>`

- `create event --autoDecline <eventName> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> for <N> times`
- `create event <eventName> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> for <N> times`

- `create event --autoDecline <eventName> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> until <dateStringTtimeString>`
- `create event  <eventName> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> until <dateStringTtimeString>`

- `create event --autoDecline <eventName> on <dateStringTtimeString>`
- `create event <eventName> on <dateStringTtimeString>`

- `create event --autoDecline <eventName> on <dateString> repeats <weekdays> for <N> times`
- `create event <eventName> on <dateString> repeats <weekdays> for <N> times`

- `create event --autoDecline <eventName> on <dateString> repeats <weekdays> until <dateString>`
- `create event <eventName> on <dateString> repeats <weekdays> until <dateString>` 


**Edit Event**

-   `edit event <property> <eventName> from <dateStringTtimeString> to <dateStringTtimeString> with <NewPropertyValue>`

-   `edit events <property> <eventName> from <dateStringTtimeString> with <NewPropertyValue>`

-   `edit events <property> <eventName> <NewPropertyValue>`

Change the property (e.g., name) of all events with the same event name.


**Print Event**
-   `print events on <dateString>`

-   `print events from <dateStringTtimeString> to <dateStringTtimeString>`


**Export Event**
-   `export cal fileName.csv`


**Status Event**
-   `show status on <dateStringTtimeString>`



**Features that currently don't work**

the two modes ,-- **interactive** and **headless** .




## Authors

Contributors:

- Siddhant Narode  
- Zoya Arief




