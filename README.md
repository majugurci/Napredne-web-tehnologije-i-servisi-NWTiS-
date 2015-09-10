# Napredne-web-tehnologije-i-servisi-NWTiS-
Advanced web technologies and services

This final project is created for a college course. The project was done in the Java language and used a lot of technologies. Here is a brief description of the project. The project was divided into 3 separate Java applications. The first application is a Java app that simulates socket server, second and third are Java web apps, first user oriented and latter data oriented. Design for applications was taken from free templates and is credited in applications. Everything else is my work. Here is description of applications:
First application: 
- Tomcat web app, EE6/7, use of web.xml without annotations, user interface: JSP, DB: MySQL, JDBC, SQL, email server, socket server, SOAP web server, use of openweathermap.org and Google Maps API REST services 
- - Web application collects weather data and stores them do DB and gives statistical access to them 
- - User request is being charged 
- - There is background thread which is started at the start of an application that collects weather data and stores them to the DB
- There is a socket server, which gives user statistical weather data and admins control over application. Users must pass authentication before their request is processed. Each user request is processed in a separate thread. Here are some of the commands. ADMIN commands: PAUSE - puts thread collecting weather data in pause mode, START - resumes collection of weather data, STOP - stops thread for weather data and stops socket server, ADMIN user - gives admin privileges to users, NOADMIN user - removes admin privileges for given admin, the price list, DOWNLOAD - download of price list for user requests in xml format, UPLOAD price_list - upload of new price list. USER commands (admin can run them also): ADD address - adds a new address to DB for collection of weather data, TEST address: return whether weather data is being collected or not, GET address - returns most recent weather data for an address, TYPE - return user type (admin or user), ADD user, pass - Adds new user to the application.


The second task of the app is a SOAP web service which returns all kinds of weather data.

Third task is a user interface in JSP. User authentication is realized through web/servlet container based on form and JDBC to access DB. The application is using SSL for requests. Some of the features are: editing user data, managing resources used for server commands, displaying user logs and similar.
