@echo off

set PATH=%PATH%;./inno

ISCC.exe /O"%1" /DAppVersion="%2"  /Q "./setup.iss"
