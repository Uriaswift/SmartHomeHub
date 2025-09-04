@echo off
setlocal

rem === resolve project base dir ===
set "MAVEN_PROJECTBASEDIR=%~dp0"
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

rem === wrapper paths ===
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"
set "WRAPPER_MAIN=org.apache.maven.wrapper.MavenWrapperMain"

rem === find java ===
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if exist "%JAVA_EXE%" goto haveJava
set "JAVA_EXE=java"
:haveJava

rem === checks ===
if not exist "%WRAPPER_JAR%" (
  echo [ERROR] File not found: %WRAPPER_JAR%
  exit /b 1
)
if not exist "%WRAPPER_PROPERTIES%" (
  echo [ERROR] File not found: %WRAPPER_PROPERTIES%
  exit /b 1
)

rem === run wrapper with explicit settings and local repo ===
"%JAVA_EXE%" ^
  -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" ^
  -Dmaven.repo.local="%MAVEN_PROJECTBASEDIR%\.m2\repository" ^
  -classpath "%WRAPPER_JAR%" %WRAPPER_MAIN% ^
  -s "%MAVEN_PROJECTBASEDIR%\.mvn\settings.xml" %*

endlocal
