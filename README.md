# Napredne-web-tehnologije-i-servisi-NWTiS-
Advanced web technologies and services

This is final project created for college course. Project was done in Java language and used a lot of technologies. Here is a brief description of project. Project was diveded in 3 seperate Java applications. First application is Java app that simulates socket server, second and third are Java web apps, first user oriented and latter data oriented. Design for applications was taken from free templates and is credited in applications. Everything else is my work. Here is description by applications:
First application:
- Tomcat web app, EE6/7, use of web.xml without annotations, user interface: JSP, DB: MySQL, JDBC, SQL, email server, socket server, SOAP web server, use of openweathermap.org and Google Maps API REST services
- web application collects weather data and stores them do DB and gives statistical access to them
- User request are being charged
- There is backgournd thread which is started at the start of application that collects weather data and stores them to DB
- There is socket server which gives user statistical weather data and admins control over application. Users must pass authentication before their request is processed. Each user request is processed in seperated thread. Here are some of the commands. ADMIN commands: PAUSE - puts thread collecting weather data in pause mode, START - resumes collection of weather data, STOP - stops thread for weather data and stops socket server, ADMIN user - gives admin privilegis to user, NOADMIN user - removes admin privilegies for given admin, DOWNLOAD - download of price list for user requests in xml format, UPLOAD price_list - upload of new price list. USER comands (admin can run them also): ADD address - adds new address to DB for collection of weather data, TEST address: return
