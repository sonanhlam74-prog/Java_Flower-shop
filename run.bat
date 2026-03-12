@echo off
setlocal

if not defined JAVA_HOME (
    echo [ERROR] JAVA_HOME is not set.
    echo Please install JDK 17+ and set JAVA_HOME before running this script.
    exit /b 1
)

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] JAVA_HOME is invalid: "%JAVA_HOME%"
    echo Please point JAVA_HOME to a JDK 17+ installation.
    exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"
call .\mvnw.cmd -q -DskipTests clean javafx:run
exit /b %ERRORLEVEL%