@echo off
cd /d "%~dp0"
start "" javaw -cp "bin;lib/*" GUI.Main
