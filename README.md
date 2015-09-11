# Napredne-web-tehnologije-i-servisi-NWTiS-
Advanced web technologies and services

This final project is created for a college course. The project was done in the Java language and used a lot of technologies. Here is a brief description of the project. The project was divided into 3 separate Java applications. The first application is a Java app that simulates socket server, second and third are Java web apps, first user oriented and latter data oriented. Design for applications was taken from free templates and is credited in applications. Everything else is my work. Here is description of applications:

<br><b>First application:</b> 
- Tomcat web app, EE6/7, use of web.xml without annotations, user interface: JSP, DB: MySQL, JDBC, SQL, email server, socket server, SOAP web server, use of openweathermap.org and Google Maps API REST services 
- Web application collects weather data and stores them do DB and gives statistical access to them 
- User request is being charged 
- There is background thread which is started at the start of an application that collects weather data and stores them to the DB
- There is a socket server, which gives user statistical weather data and admins control over application. Users must pass authentication before their request is processed. Each user request is processed in a separate thread. Here are some of the commands. ADMIN commands: PAUSE - puts thread collecting weather data in pause mode, START - resumes collection of weather data, STOP - stops thread for weather data and stops socket server, ADMIN user - gives admin privileges to users, NOADMIN user - removes admin privileges for given admin, the price list, DOWNLOAD - download of price list for user requests in xml format, UPLOAD price_list - upload of new price list. USER commands (admin can run them also): ADD address - adds a new address to DB for collection of weather data, TEST address: return whether weather data is being collected or not, GET address - returns most recent weather data for an address, TYPE - return user type (admin or user), ADD user, pass - Adds new user to the application.


The second task of the app is a SOAP web service which returns all kinds of weather data.

Third task is a user interface in JSP. User authentication is realized through web/servlet container based on form and JDBC to access DB. The application is using SSL for requests. Some of the features are: editing user data, managing resources used for server commands, displaying user logs and similar.

<br><b>Second application:</b> 
- Glassfish web app, EE7 with annotations, user interface: JSF (facelets) or PrimeFaces, DB: JavaDB, ORM (EclipseLink), Criteria API, James email server, JMS messages, use of socket server, use of SOAP web service, gives REST web service
- Application has EJB and Web modules
- First task of the app is managing email messages. A background thread is checking for new messages and sorts them in folders. If the message has special title, then thread reads it's body and adds new users to DB. At the end of every iteration thread sends JMS message with statistical data.
- Second task is user interface. 
- Application marks user actions and saves them to DB log. 
- Application has to support multiple languages, where user can select language on index page. 
- User can login and register. 
- After sucessful registration command through socket server is sent to the first application. 
- After login user can through calls to web service of first app get all addresses for which weather data are being collected, get all addresses which user added, last weather data for given address etc. All user interactions are resolved on one screen using AJAX. User can add new addresses, after successful adding JMS message is sent to queue. For chosen addresses ther is an option to view them on google maps (with Google Maps JavaScript API).
- The administrator can view logs from DB, can change the role of the user (through calls to server socket), can read and manage email messages, and can manage price list of first application through calls to the server socket.
- Third task is REST service which gives a list of all currently logged in users.

<br><b>Third application:</b> 
- Glassfish web app, EE/ with annotations, user interface: JSF (facelets) or PrimeFaces, DB: none, use of JMS messages, use of websocket for refreshing messages view, use of the socket server, use of the REST web service of second app.
- Application has EJB and Web modules
- First task is to process JMS messages in background thread. Application reads messages and checks whether address from the message is in DB of first application (through a call to socket server), and if it is not sends new command to the socket server to add it. All JMS messages are saved to application memory. If the application stops running all messages must be saved to disk through serialization. On restart messages must be deserialized.
- Second task is user interface. The User can read and delete JMS messages. The User can create a new user by sending command through server socket. With the help of websocket user must be informed if the new JMS message has arrived. By calling operations of the REST service of second app user can get a list of all logged in users in second app. For each user there is an option to show addresses which user added, for each address option to show last weather data. 
- There is also one screen where user can send commands to the socket server and get a response (supervision).
