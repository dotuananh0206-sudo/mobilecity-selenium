@echo off
setlocal
title MobileCity Test Manager

cd /d "%~dp0"

where mvn >nul 2>nul
if errorlevel 1 goto missing_maven

echo Starting MobileCity Test Manager...
call mvn javafx:run
if errorlevel 1 goto run_failed
exit /b 0

:missing_maven
echo ERROR: Maven was not found in PATH.
echo Install Maven 3.9 or newer, then run this file again.
pause
exit /b 1

:run_failed
echo.
echo The application could not be started. Review the Maven output above.
pause
exit /b 1
