@echo off
chcp 65001 >nul
cd /d "%~dp0"

where javac >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\javac.exe" (
            set "PATH=%JAVA_HOME%\bin;%PATH%"
        )
    )
)

where javac >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if exist "C:\Users\thinh\Downloads\eclipse-jee-2026-03-R-win32-x86_64\eclipse\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.10.v20260205-0638\jre\bin\javac.exe" (
        set "JAVA_HOME=C:\Users\thinh\Downloads\eclipse-jee-2026-03-R-win32-x86_64\eclipse\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.10.v20260205-0638\jre"
        set "PATH=C:\Users\thinh\Downloads\eclipse-jee-2026-03-R-win32-x86_64\eclipse\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.10.v20260205-0638\jre\bin;%PATH%"
    )
)

echo Dang bien dich ma nguon Java...
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -cp "lib/*;bin" -d bin @sources.txt
if %ERRORLEVEL% equ 0 (
    if exist sources.txt del sources.txt
    echo [THANH CONG] Bien dich thanh cong!
) else (
    echo [THAT BAI] Co loi xay ra trong qua trinh bien dich.
)
