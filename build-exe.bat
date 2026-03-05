@echo off
REM ============================================================
REM  Build script: creates a standalone FlowerShop.exe
REM  Requirements: JDK 21+ with jpackage (bundled)
REM ============================================================

setlocal
set "APP_VERSION=1.1.0"

REM --- Detect JAVA_HOME ---
if defined JAVA_HOME (
    if not exist "%JAVA_HOME%\bin\jpackage.exe" (
        echo [WARN] JAVA_HOME is set but invalid for packaging: "%JAVA_HOME%"
        echo [WARN] Auto-detecting a valid JDK from PATH...
        set "JAVA_HOME="
    )
)

if not defined JAVA_HOME (
    set "JAVA_HOME_CANDIDATE="
    for /f "usebackq delims=" %%I in (`powershell -NoProfile -Command "Get-Command javac -All -ErrorAction SilentlyContinue ^| ForEach-Object { Split-Path -Parent (Split-Path -Parent $_.Source) } ^| Where-Object { Test-Path (Join-Path $_ 'bin\\jpackage.exe') } ^| Select-Object -First 1"`) do set "JAVA_HOME_CANDIDATE=%%I"

    if defined JAVA_HOME_CANDIDATE (
        set "JAVA_HOME=%JAVA_HOME_CANDIDATE%"
        echo [INFO] Detected JAVA_HOME: "%JAVA_HOME%"
    ) else (
        echo [ERROR] JAVA_HOME is not set and no JDK with jpackage was found in PATH.
        echo Please install JDK 21+ and set JAVA_HOME, or add JDK\bin to PATH.
        echo Example: set JAVA_HOME=C:\Users\dell\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.7.6-hotspot
        exit /b 1
    )
)

echo ============================================================
echo   Flower Shop - Build Standalone EXE
echo ============================================================
echo.
echo [1/4] Building fat JAR with Maven...
call mvnw.cmd -q -DskipTests clean package
if errorlevel 1 (
    echo [ERROR] Maven build failed!
    exit /b 1
)

set "APP_JAR="
for /f "delims=" %%F in ('dir /b /a:-d /o:-d "target\demo-*.jar"') do (
    set "APP_JAR=%%F"
    goto :jar_found
)

:jar_found
if not defined APP_JAR (
    echo [ERROR] Cannot find packaged JAR in target\ (expected demo-*.jar)
    exit /b 1
)

echo       Done.

REM --- Prepare staging directory (only the fat JAR) ---
echo.
echo [2/4] Preparing staging directory...
if exist "target\staging" rmdir /s /q "target\staging"
if exist "target\flower-app" rmdir /s /q "target\flower-app"
mkdir "target\staging"
copy "target\%APP_JAR%" "target\staging\" >nul
echo       Done.

echo.
echo [3/4] Creating portable app with jpackage (stripped JRE)...
"%JAVA_HOME%\bin\jpackage" ^
    --type app-image ^
    --input target\staging ^
    --main-jar %APP_JAR% ^
    --main-class com.example.Launcher ^
    --name FlowerShop ^
    --app-version %APP_VERSION% ^
    --vendor "Flower Shop" ^
    --icon photo\download.ico ^
    --dest target\flower-app ^
    --jlink-options "--strip-debug --no-man-pages --no-header-files"

if errorlevel 1 (
    echo [ERROR] jpackage failed!
    exit /b 1
)
echo       Done.

REM --- Create Start.bat launcher (works with spaces in path) ---
echo.
echo [4/5] Creating Start.bat launcher...
(
    echo @echo off
    echo cd /d "%%~dp0"
    echo "%%~dp0runtime\bin\javaw.exe" -jar "%%~dp0app\%APP_JAR%"
) > "target\flower-app\FlowerShop\Start.bat"
echo       Done.

echo.
echo [5/5] Build complete!
echo.
echo ============================================================
echo   Output: target\flower-app\FlowerShop\
echo   - FlowerShop.exe  (may not work if path has spaces)
echo   - Start.bat        (works everywhere, use this if exe fails)
echo   Size: ~96 MB (includes JRE, no installation needed)
echo ============================================================
echo.
echo   To distribute: zip the "target\flower-app\FlowerShop" folder.
echo   Users can double-click Start.bat or FlowerShop.exe!
echo ============================================================

endlocal
