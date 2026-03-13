@echo off
setlocal EnableDelayedExpansion

if not defined JAVA_HOME (
    echo [ERROR] JAVA_HOME is not set.
    echo Please install JDK 17+ and set JAVA_HOME before running this script.
    exit /b 1
)

if not exist "!JAVA_HOME!\bin\java.exe" (
    echo [ERROR] JAVA_HOME is invalid: "!JAVA_HOME!"
    echo Please point JAVA_HOME to a JDK 17+ installation.
    exit /b 1
)

set "PATH=!JAVA_HOME!\bin;%PATH%"

if /I "%~1"=="dev" goto :run_dev

set "APP_JAR="
for %%F in (target\demo-*.jar) do (
    if not defined APP_JAR if exist "%%~fF" set "APP_JAR=%%~fF"
)

:jar_found
if not defined APP_JAR goto :build_once

echo [INFO] Starting packaged app: "!APP_JAR!"
"!JAVA_HOME!\bin\java.exe" -jar "!APP_JAR!"
goto :end

:build_once
echo [INFO] Packaged JAR not found. Building once...
call .\mvnw.cmd -q -DskipTests package
if errorlevel 1 goto :end

set "APP_JAR="
for %%F in (target\demo-*.jar) do (
    if not defined APP_JAR if exist "%%~fF" set "APP_JAR=%%~fF"
)

if defined APP_JAR goto :run_jar_after_build

echo [ERROR] Build completed but no packaged JAR was found in target\.
goto :end

:run_jar_after_build
echo [INFO] Starting packaged app: "!APP_JAR!"
"!JAVA_HOME!\bin\java.exe" -jar "!APP_JAR!"
goto :end

:run_dev
echo [INFO] Running in dev mode via Maven (slower startup, rebuilds classes).
"!JAVA_HOME!\bin\java.exe" "-Dmaven.multiModuleProjectDirectory=%CD%" -classpath ".mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain -q -DskipTests javafx:run

:end
exit /b %ERRORLEVEL%