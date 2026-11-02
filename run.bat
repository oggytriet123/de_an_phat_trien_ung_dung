@echo off
chcp 65001 >nul
cd /d "%~dp0"

where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\java.exe" (
            set "PATH=%JAVA_HOME%\bin;%PATH%"
        )
    )
)

where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if exist "C:\Users\thinh\Downloads\eclipse-jee-2026-03-R-win32-x86_64\eclipse\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.10.v20260205-0638\jre\bin\java.exe" (
        set "JAVA_HOME=C:\Users\thinh\Downloads\eclipse-jee-2026-03-R-win32-x86_64\eclipse\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.10.v20260205-0638\jre"
        set "PATH=C:\Users\thinh\Downloads\eclipse-jee-2026-03-R-win32-x86_64\eclipse\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.10.v20260205-0638\jre\bin;%PATH%"
    )
)

start "" javaw -cp "bin;lib/*" GUI.Main
