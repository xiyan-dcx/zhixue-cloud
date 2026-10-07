@echo off
setlocal
cd /d "%~dp0"

rem Override with ZHIXUE_JAVA_HOME when using a different complete JDK.
if defined ZHIXUE_JAVA_HOME (
    set "JAVA_HOME=%ZHIXUE_JAVA_HOME%"
) else if exist "%USERPROFILE%\.jdks\corretto-24.0.2\bin\javac.exe" (
    set "JAVA_HOME=%USERPROFILE%\.jdks\corretto-24.0.2"
)

if not exist "%JAVA_HOME%\bin\javac.exe" (
    echo A complete JDK supporting release 21 is required.
    echo Set ZHIXUE_JAVA_HOME to its installation directory.
    exit /b 1
)
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo Using JAVA_HOME=%JAVA_HOME%
if "%~1"=="" (
    call "%~dp0mvnw.cmd" spring-boot:run
) else (
    call "%~dp0mvnw.cmd" %*
)
exit /b %ERRORLEVEL%
