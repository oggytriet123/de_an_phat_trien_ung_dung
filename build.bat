@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo Dang bien dich ma nguon Java...
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -cp "lib/*;bin" -d bin @sources.txt
if %ERRORLEVEL% equ 0 (
    echo [THANH CONG] Bien dich thanh cong!
) else (
    echo [THAT BAI] Co loi xay ra trong qua trinh bien dich.
)
