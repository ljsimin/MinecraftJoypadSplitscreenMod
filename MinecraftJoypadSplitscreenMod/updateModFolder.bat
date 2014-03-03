REM after building using gradle, run this batch to copy the mod to your minecraft mod folder for testing
erase %APPDATA%\.minecraft\mods\JoypadMod-1.7.2*
copy build\libs\* %APPDATA%\.minecraft\mods\
erase build\libs\*