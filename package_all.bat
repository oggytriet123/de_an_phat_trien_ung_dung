@echo off
chcp 65001 >nul
cd /d "%~dp0"
cls

echo ==============================================================================
echo           QUY TRINH DONG GOI UNG DUNG PETCARE TOAN DIEN (A - Z)
echo ==============================================================================
echo.
echo   [Buoc 1] Build Runnable Fat JAR (dist\PetCare.jar)
echo   [Buoc 2] Nhung JRE runtime vao ung dung bang jpackage (dist\PetCare\)
echo   [Buoc 3] Tao file cai dat Setup.exe bang Inno Setup (installer\Output\)
echo.
echo ==============================================================================
echo.

:: Buoc 1: Build JAR
call "%~dp0build_jar.bat"
if %ERRORLEVEL% neq 0 (
    echo.
    echo [THAT BAI] Qua trinh dong goi JAR gap loi. Dung tien trinh.
    pause
    exit /b 1
)

echo.
:: Buoc 2: jpackage nhung JRE
call "%~dp0build_jpackage.bat"
if %ERRORLEVEL% neq 0 (
    echo.
    echo [THAT BAI] Qua trinh nhung JRE gap loi. Dung tien trinh.
    pause
    exit /b 1
)

echo.
:: Buoc 3: Inno Setup
call "%~dp0build_installer.bat"
if %ERRORLEVEL% neq 0 (
    echo.
    echo [THAT BAI] Qua trinh tao file cai dat gap loi.
    pause
    exit /b 1
)

echo.
echo ==============================================================================
echo                     TAT CA CAC BUOC DA HOAN THANH XUAT SAC!
echo ==============================================================================
echo.
echo  1. File JAR Runnable:
echo     D:\Do_An\project11\dist\PetCare.jar
echo.
echo  2. Ung dung Standalone Portable (kem JRE khong can cai Java):
echo     D:\Do_An\project11\dist\PetCare\PetCare.exe
echo.
echo  3. Bo cai dat Setup hoan chinh:
echo     D:\Do_An\project11\installer\Output\PetCare_Setup_v1.0.0.exe
echo.
echo ==============================================================================
echo.
pause
