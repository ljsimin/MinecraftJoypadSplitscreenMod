REM after building using gradle, run this batch to copy the mod to your minecraft mod folder for testing
erase %APPDATA%\.minecraft\mods\joypadmod*
copy build\libs\* %APPDATA%\.minecraft\mods