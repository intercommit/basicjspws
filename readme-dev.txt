README Basic JSP Webservice project

This is the jar-only project for the Basic JSP Webservice package.
To actually use this jar, you will need the files and settings as shown in the basicjspws-demo project
(which also includes a manual).
To use the demo-project the jar from this project must be installed in your Maven repository.
To install, open a command prompt, go to this project's directory and run:
mvn clean install

If mvn is not a known command, install Maven 2 first (http://maven.apache.org/download.html).

### Import in Eclipse

Before importing this project into Eclipse,
open a command prompt, go to this project's directory and run:
mvn eclipse:clean eclipse:eclipse -DdownloadSources

Now import this project in Eclipse as a normal project via "General> Existing project".
