@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ====================================================
echo   KHOI TAO DATABASE PETCARE TU FILE SQL
echo ====================================================
echo.

where sqlcmd >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [LOI] Khong tim thay cong cu sqlcmd tren he thong.
    echo Ban co the mo file data\PetCare_AllInOne.sql truc tiep trong SSMS roi bam Execute (F5).
    pause
    exit /b 1
)

set SQL_FILE=data\PetCare_AllInOne.sql

echo Dang thuc thi file SQL de build database PetCare...
echo [1/4] Thu ket noi Windows Authentication: localhost...
sqlcmd -S localhost -E -i "%SQL_FILE%" -b
if %ERRORLEVEL% equ 0 goto success

echo.
echo [2/4] Thu ket noi Windows Authentication: .\SQLEXPRESS...
sqlcmd -S .\SQLEXPRESS -E -i "%SQL_FILE%" -b
if %ERRORLEVEL% equ 0 goto success

echo.
echo [3/4] Thu ket noi SQL Auth (sa/sapassword) theo ConnectDB.java: localhost...
sqlcmd -S localhost -U sa -P sapassword -i "%SQL_FILE%" -b
if %ERRORLEVEL% equ 0 goto success

echo.
echo [4/4] Thu ket noi SQL Auth (sa/sapassword): .\SQLEXPRESS...
sqlcmd -S .\SQLEXPRESS -U sa -P sapassword -i "%SQL_FILE%" -b
if %ERRORLEVEL% equ 0 goto success

:fail
echo.
echo [HUONG DAN] Khong the ket noi tu dong den SQL Server mac dinh.
echo De build CSDL PetCare bang SQL:
echo   Cach 1: Mo SSMS (SQL Server Management Studio) hoac Azure Data Studio.
echo           Mo file: data\PetCare_AllInOne.sql roi bam Execute (F5).
echo   Cach 2: Chay lenh sqlcmd voi ServerName cua ban:
echo           sqlcmd -S <Ten_Server_Cua_Ban> -E -i data\PetCare_AllInOne.sql
echo.
pause
exit /b 1

:success
echo.
echo ====================================================
echo [THANH CONG] Da build CSDL PetCare thanh cong!
echo ====================================================
echo.
pause
