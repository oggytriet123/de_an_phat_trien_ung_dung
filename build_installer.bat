@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ===================================================
echo [3/3] DANG TAO BO CAI DAT SETUP.EXE BANG INNO SETUP
echo ===================================================

:: 1. Tim trinh bien dich ISCC.exe cua Inno Setup
set "ISCC_PATH="

where iscc >nul 2>&1
if %ERRORLEVEL% equ 0 (
    set "ISCC_PATH=iscc"
) else (
    if exist "%LOCALAPPDATA%\Programs\Inno Setup 6\ISCC.exe" (
        set "ISCC_PATH=%LOCALAPPDATA%\Programs\Inno Setup 6\ISCC.exe"
    ) else if exist "C:\Program Files (x86)\Inno Setup 6\ISCC.exe" (
        set "ISCC_PATH=C:\Program Files (x86)\Inno Setup 6\ISCC.exe"
    ) else if exist "C:\Program Files\Inno Setup 6\ISCC.exe" (
        set "ISCC_PATH=C:\Program Files\Inno Setup 6\ISCC.exe"
    )
)

if not defined ISCC_PATH (
    echo [LOI] Khong tim thay ISCC.exe cua Inno Setup!
    echo Vui long cai dat Inno Setup 6 tu https://jrsoftware.org/isdl.php
    echo hoac chay lenh: winget install JRSoftware.InnoSetup -e
    exit /b 1
)

:: 2. Kiem tra thu muc ung dung dist\PetCare
if not exist "dist\PetCare\PetCare.exe" (
    echo [INFO] Chua tim thay dist\PetCare\PetCare.exe, dang goi build_jpackage.bat...
    call "%~dp0build_jpackage.bat"
    if %ERRORLEVEL% neq 0 exit /b 1
)

:: 3. Tao thu muc Output
if not exist "installer\Output" mkdir "installer\Output"

:: 4. Bien dich script Inno Setup
echo [3.1] Dang bien dich bo cai dat voi ISCC.exe...
"%ISCC_PATH%" "%~dp0installer\petcare_setup.iss"

if %ERRORLEVEL% neq 0 (
    echo [LOI] Bien dich Inno Setup that bai!
    exit /b 1
)

echo [THANH CONG] Da tao bo cai dat hoan chinh tai:
for %%f in ("%~dp0installer\Output\PetCare_Setup*.exe") do (
    echo             =^> %%~ff
)
echo ===================================================
