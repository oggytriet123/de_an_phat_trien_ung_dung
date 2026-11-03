@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ===================================================
echo [2/3] DANG NHUNG JRE VA TAO NATIVE APP BANG JPACKAGE
echo ===================================================

:: 1. Tim jpackage
where jpackage >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\jpackage.exe" (
            set "PATH=%JAVA_HOME%\bin;%PATH%"
        )
    )
)

where jpackage >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if exist "D:\jdk\bin\jpackage.exe" (
        set "JAVA_HOME=D:\jdk"
        set "PATH=D:\jdk\bin;%PATH%"
    )
)

where jpackage >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [LOI] Khong tim thay jpackage.exe. Yeu cau JDK 14 tro len (hien tai JDK 25 da san co).
    exit /b 1
)

:: 2. Kiem tra file PetCare.jar
if not exist "dist\PetCare.jar" (
    echo [INFO] Chua tim thay dist\PetCare.jar, dang goi build_jar.bat de tao...
    call "%~dp0build_jar.bat"
    if %ERRORLEVEL% neq 0 exit /b 1
)

:: 3. Kiem tra icon
if not exist "assets\app_icon.ico" (
    echo [INFO] Dang tao icon cho ung dung...
    powershell -ExecutionPolicy Bypass -File "%~dp0assets\generate_icon.ps1"
)

:: 4. Chuan bi input folder cho jpackage
echo [2.1] Chuan bi thu muc input cho jpackage...
if exist "build\input_jar" rmdir /s /q "build\input_jar"
mkdir "build\input_jar"
copy /y "dist\PetCare.jar" "build\input_jar\PetCare.jar" >nul

:: Xoa app image cu neu co
if exist "dist\PetCare" (
    echo [2.2] Xoa thu muc dist\PetCare cu...
    rmdir /s /q "dist\PetCare"
)

:: 5. Chay jpackage de tao app-image kem JRE nhung
echo [2.3] Dang chay jpackage tao bo app-image kem Embedded JRE...
echo       (Bao gom modules: java.base, java.desktop, java.sql, java.naming, v.v.)
jpackage ^
  --type app-image ^
  --name "PetCare" ^
  --input "build\input_jar" ^
  --main-jar "PetCare.jar" ^
  --main-class "GUI.Main" ^
  --dest "dist" ^
  --icon "assets\app_icon.ico" ^
  --add-modules "java.base,java.desktop,java.logging,java.management,java.naming,java.security.jgss,java.sql,java.transaction.xa,java.xml,jdk.crypto.ec" ^
  --java-options "-Dfile.encoding=UTF-8"

if %ERRORLEVEL% neq 0 (
    echo [LOI] jpackage that bai!
    exit /b 1
)

:: Don dep thu muc tam
rmdir /s /q "build\input_jar" >nul 2>&1

echo [THANH CONG] Da nhung JRE va tao xong ung dung doc lap tai:
echo             =^> dist\PetCare\PetCare.exe
echo             =^> dist\PetCare\runtime\ (JRE nhung san - khong can may khach cai Java)
echo ===================================================
