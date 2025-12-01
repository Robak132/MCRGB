@echo off
echo ====================================
echo Testing MCRGB Project Build
echo ====================================
echo.

echo [1/3] Checking Gradle version and Java...
call gradlew.bat --version
echo.

echo [2/3] Building libgui subproject...
call gradlew.bat :libgui:build --console=plain
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: libgui build failed!
    exit /b 1
)
echo.

echo [3/3] Building main project...
call gradlew.bat build --console=plain
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Main project build failed!
    exit /b 1
)

echo.
echo ====================================
echo BUILD SUCCESSFUL!
echo ====================================

