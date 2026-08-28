@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Script for Windows
@REM ----------------------------------------------------------------------------

@IF "%DEBUG%"=="" @ECHO OFF

SET ERROR_CODE=0
SET "DIR=%~dp0"

IF EXIST "%DIR%.tools\apache-maven-3.9.6\bin\mvn.cmd" (
    CALL "%DIR%.tools\apache-maven-3.9.6\bin\mvn.cmd" %*
) ELSE (
    CALL mvn %*
)
