cd C:\ComplaintSystem
del *.class

javac -cp .;mysql-connector-j-9.5.0.jar AnonymousComplaintSystem.java

"C:\Program Files\Java\jdk-23\bin\java.exe" -cp .;mysql-connector-j-9.5.0.jar AnonymousComplaintSystem