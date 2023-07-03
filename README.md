# My Day Planner
An old project from school that uses Java to enable days scheduling.

## Repository content
* Java application source code and maven file
* user documentation
* php files, through which the application connects to the database

## Note about php scripts
For simplicity, the application is connected to the database via a php script that runs on the server that connects to the MySQL database. However, the original class (named "MyConnectionToDatabae") that uses Java libraries to connect to the database is also left in the source files.

## Launching
The application should be executable after it is built using a maven file. An internet connection is required for the application to run normally due to the database connection. A slightly more detailed description is available in the user documentation.
