# In this file will be instructions for launch of the application
## Requirements:
### To run this application your computer should have installed JRE(Java Runtime Executable from Java 17).
## To run it from command line(Windows) through JRE you should:
1. Fetch this repository to any comfortable place on your PC
2. Open command line and navigate to project folder
3. Run `mvn clean install`
4. In project folder navigate to target folder ```cd target```
5. When you there, just write ```java -jar PragmasoftDemo-0.0.1-SNAPSHOT.jar``` *<--- Name of the -jar can be changed, be careful*
6. After that you will see logs of application launching. If something wrong you\`ll see that either.

### The interaction with the application is preferred through SwaggerUI which is located on `/api` endpoint or `http://localhost:8080/api` full path.

## SwaggerUI
Application has Swagger UI for comfortable using and debugging.

### You can view database state through H2 console which is located on `/db` endpoint. Default username is `1` , default password is `1` , database url is `jdbc:h2:mem:testdb`


