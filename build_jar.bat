@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ===================================================
echo [1/3] DANG DONG GOI EXECUTABLE RUNNABLE JAR
echo ===================================================

:: 1. Tim trinh bien dich javac & jar
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
    if exist "D:\jdk\bin\javac.exe" (
        set "JAVA_HOME=D:\jdk"
        set "PATH=D:\jdk\bin;%PATH%"
    )
)

where javac >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [LOI] Khong tim thay javac.exe. Vui long cai dat JDK hoac thiet lap JAVA_HOME.
    exit /b 1
)

:: 2. Bien dich source code
echo [1.1] Bien dich ma nguon Java tu src sang bin...
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -cp "lib/*;bin" -d bin @sources.txt
if %ERRORLEVEL% neq 0 (
    echo [LOI] Bien dich that bai!
    if exist sources.txt del sources.txt
    exit /b 1
)
if exist sources.txt del sources.txt
echo [OK] Bien dich Java thanh cong.

:: 3. Chuan bi thu muc staging
echo [1.2] Chuan bi thu muc dong goi Fat JAR...
if exist "build\staging_jar" rmdir /s /q "build\staging_jar"
mkdir "build\staging_jar"

:: Copy class files tu bin
xcopy /s /e /q /y "bin\*" "build\staging_jar\" >nul

:: Giai nen dependencies tu lib
echo [1.3] Giai nen cac thu vien vao staging...
pushd "build\staging_jar"
for %%f in ("..\..\lib\*.jar") do (
    jar xf "%%f"
)

:: Xoa cac chu ky so (signatures) de tranh SecurityException
if exist "META-INF\*.SF" del /q "META-INF\*.SF" >nul 2>&1
if exist "META-INF\*.RSA" del /q "META-INF\*.RSA" >nul 2>&1
if exist "META-INF\*.DSA" del /q "META-INF\*.DSA" >nul 2>&1

:: Tao file MANIFEST.MF chuan
if not exist "META-INF" mkdir "META-INF"
(
echo Manifest-Version: 1.0
echo Main-Class: GUI.Main
echo Created-By: PetCare Application Packager
) > "META-INF\MANIFEST.MF"

popd

:: 4. Dong goi thanh file PetCare.jar
echo [1.4] Dang dong goi dist\PetCare.jar...
if not exist dist mkdir dist
if exist "dist\PetCare.jar" del /f /q "dist\PetCare.jar"

jar cvfm "dist\PetCare.jar" "build\staging_jar\META-INF\MANIFEST.MF" -C "build\staging_jar" . >nul
if %ERRORLEVEL% neq 0 (
    echo [LOI] Khong the tao file PetCare.jar!
    exit /b 1
)

:: Don dep thu muc staging
rmdir /s /q "build\staging_jar" >nul 2>&1

echo [THANH CONG] Da tao thanh cong file Executable Runnable JAR:
echo             =^> dist\PetCare.jar
echo ===================================================
