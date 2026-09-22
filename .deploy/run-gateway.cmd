@echo off
setlocal
rem Invoked by scheduled task "GoodsManage-Gateway" as SYSTEM at boot.
rem NOTE: keep this file ASCII-only with CRLF line endings -- see run-backend.cmd.
set ROOT=D:\tool\IDEA\linqi\GoodsManage

if not exist "%ROOT%\.deploy\logs" mkdir "%ROOT%\.deploy\logs"
break > "%ROOT%\.deploy\logs\gateway.log"

rem ---- Wait for the backend on 8080, max ~300s ----
rem The gateway itself does not depend on the backend: gateway.js proxies lazily
rem and returns 502 when it is down. Waiting only means the public URL works
rem completely from the first request instead of serving 502s.
rem ping, not timeout.exe -- see run-backend.cmd.
set /a n=0
:waitbe
netstat -an | findstr /c:":8080 " | findstr /c:"LISTENING" >nul 2>&1 && goto up
set /a n+=1
if %n% geq 60 goto up
ping -n 6 127.0.0.1 -w 1000 >nul 2>&1
goto waitbe
:up

rem gateway.js resolves assets from __dirname, so cwd is not load-bearing.
rem node lives at D:\node.exe (not a standard install) and is NOT on the
rem SYSTEM PATH, so the absolute path is required.
cd /d "%ROOT%\.deploy"
"D:\node.exe" "%ROOT%\.deploy\gateway.js" >> "%ROOT%\.deploy\logs\gateway.log" 2>&1

exit /b %ERRORLEVEL%
