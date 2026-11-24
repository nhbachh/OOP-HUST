@echo off
REM ==============================================================================
REM HUST Lib — Windows Launch Script (JavaFX Version 2)
REM ==============================================================================

set JAR_FILE=target\LibraryManagementSystem-1.0.0.jar

if exist "%JAR_FILE%" (
    echo [INFO] Khoi chay HUST Lib che do JavaFX Version 2 tu JAR...
    java -jar "%JAR_FILE%" %*
) else (
    echo [INFO] Khoi chay HUST Lib qua Maven...
    mvn exec:java -Dexec.mainClass="com.library.Main" -Dexec.args="%*"
)
