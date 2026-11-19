@echo off
REM ==============================================================================
REM HUST Lib — Windows Launch Script (Ready for Users)
REM ==============================================================================

set JAR_FILE=target\LibraryManagementSystem-1.0.0.jar

if exist "%JAR_FILE%" (
    if "%1"=="--cli" (
        echo [INFO] Khoi chay HUST Lib che do Console CLI tu JAR...
        java -jar "%JAR_FILE%" --cli
    ) else (
        if "%1"=="-c" (
            echo [INFO] Khoi chay HUST Lib che do Console CLI tu JAR...
            java -jar "%JAR_FILE%" --cli
        ) else (
            echo [INFO] Khoi chay HUST Lib che do Giao dien Do hoa Swing GUI...
            start javaw -jar "%JAR_FILE%"
        )
    )
) else (
    echo [INFO] Khong tim thay file JAR dong goi san. Khoi chay qua Maven...
    if "%1"=="--cli" (
        mvn exec:java -Dexec.mainClass="com.library.Main" -Dexec.args="--cli"
    ) else (
        if "%1"=="-c" (
            mvn exec:java -Dexec.mainClass="com.library.Main" -Dexec.args="--cli"
        ) else (
            mvn exec:java -Dexec.mainClass="com.library.Main"
        )
    )
)
