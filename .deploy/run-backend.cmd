@echo off
setlocal
rem Invoked by scheduled task "GoodsManage-Backend" as SYSTEM at boot.
rem NOTE: keep this file ASCII-only with CRLF line endings. cmd.exe parses
rem batch files using the OEM code page (GBK on Chinese Windows), so UTF-8
rem text in comments corrupts parsing and breaks the script.
set ROOT=D:\tool\IDEA\linqi\GoodsManage

if not exist "%ROOT%\.deploy\logs" mkdir "%ROOT%\.deploy\logs"
rem Truncate the console log on each start; MyBatis logs every SQL statement.
break > "%ROOT%\.deploy\logs\backend-console.log"

rem ---- Wait for MySQL on 3306, max ~300s ----
rem Must use ping, NOT timeout.exe: scheduled tasks run in session 0 with no
rem console, and timeout.exe aborts immediately with "Input redirection is not
rem supported", turning this into a busy-wait that starts the JVM too early.
set /a n=0
:waitdb
netstat -an | findstr /c:":3306 " | findstr /c:"LISTENING" >nul 2>&1 && goto dbup
set /a n+=1
if %n% geq 60 goto dbup
ping -n 6 127.0.0.1 -w 1000 >nul 2>&1
goto waitdb
:dbup

rem ---- cd to the backend directory is mandatory ----
rem application.yml sets upload.path to the relative path ./uploads/ ; with the
rem wrong working directory the existing product images 404 and new uploads
rem land elsewhere.
cd /d "%ROOT%\backend"

rem --server.port=8080 is mandatory: application.yml says 8081, but the gateway
rem and both vite proxies target 8080.
rem Absolute java path: PATH differs under the SYSTEM account.
"D:\tool\jdk-17.0.12\bin\java.exe" -Xms128m -Xmx384m -XX:+UseSerialGC ^
  -jar "%ROOT%\backend\target\expiry-food-backend-1.0.0.jar" ^
  --server.port=8080 ^
  --logging.file.name=%ROOT%\.deploy\logs\backend.log ^
  --logging.logback.rollingpolicy.max-file-size=10MB ^
  --logging.logback.rollingpolicy.max-history=5 ^
  >> "%ROOT%\.deploy\logs\backend-console.log" 2>&1

rem Run in the foreground so the exit code reaches Task Scheduler, which is
rem what makes restart-on-failure work.
exit /b %ERRORLEVEL%
