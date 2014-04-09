set src=src\com
set srcResources=resources
set mcp=H:\modding\forge-1.6.4-9.11.1.965-src\mcp
set dest=%mcp%\src\minecraft\com
set mcMod=%APPDATA%\.minecraft\mods
set modFileNamePrefix=JoypadMod-1.6.4

if Exist %dest%\shiny rmdir /s %dest%\shiny
xcopy %src% %dest% /E
xcopy resources %mcp%\joypadResources /E
pushd .
cd %mcp%
call recompile.bat
call reobfuscate.bat
xcopy joypadResources reobf\minecraft /E


start reobf\minecraft
echo Zip up the mcMod.info along with the COM folder, name it %modFileNamePrefix%-{version_number}.zip and press any key to continue
pause
if exist reobf\minecraft\%modFileNamePrefix%*.zip goto copystuff

goto end

:copystuff

erase %mcMod%\%modFileNamePrefix%*.zip
copy reobf\minecraft\%modFileNamePrefix%*.zip %mcMod%
rmdir /s %dest%\shiny

:end
popd