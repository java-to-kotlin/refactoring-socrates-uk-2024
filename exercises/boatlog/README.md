# Narrowboat Maintenance Log

_Refactoring serialised classes with expand/contract releases_

# Scenario

"A boat is a hole in the water that you throw money into."

Narrowboats require regular maintenance.  This application helps the user keep track of maintenance tasks and reminds them when they need to be done.

There are two kinds of maintenance. Some must be done every few months, while others – typically engine maintenance – must be done after some number of hours of cruising.  The application calls these "regular" and "cruising" maintenance tasks, respectively.

Using the application, the user can see the maintenance tasks that need to be done right now, and record when they have been done.  They can also log cruising time, which may result in new jobs being necessary.

The application is implemented as a menu-driven terminal application that stores its data in a JSON file. It uses the Kondor library to translate the JSON into an in-memory model.


# Simplifications

To make things practical for this workshop...

* The application always loads the same file: `maintenance-log.json` in the user's home directory.

* The application doesn't let you define new tasks. Run the _createExampleMaintenanceLog_ Gradle task to create an example `maintenance-log.json` file in your home directory.


# Refactoring Task

The code and data format suffer from poor naming.  Both regular and cruising maintenance tasks have a `frequency` property.  However, for regular maintenance tasks, the frequency is measured in months.  For cruising maintenance tasks, the frequency is measured in hours.  This is a cause of much confusion and defects.

Your task is to refactor the code *and* the data file to distinguish between different units. E.g. you could rename the properties in the JSON file to `frequencyHours` and `frequencyMonths`, or you could represent the frequency as a Period that is stored in ISO 8601 format. 

But be aware... we have loyal users that rely on our application to keep their boats afloat and moving.  Any new version of our application _must_ be able to read their existing files.

While refactoring, run the application to confirm that it is still compatible with the `maintenance-log.json` file in your home directory.


# Getting started

Run the _createExampleMaintenanceLog_ Gradle task. Then run the main method in `main.kt` to confirm that the application can read the file.


# Hints and Tips

Things to consider when planning your refactoring:

* How will the application recognise the version of the data file?
* How will you test that the application can read old _and_ new data file versions?
* How will you minimise duplication between the mappers for the format versions, and what refactoring _process_ will you use to achieve that?

The `VersionMapConverter` class in Kondor can be used to implement versioning of JSON formats. 
