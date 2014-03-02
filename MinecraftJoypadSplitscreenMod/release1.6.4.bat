set src=src\com
set srcResources=resources
set mcp=H:\modding\forge-1.6.4-9.11.1.965-src\mcp
set dest=%mcp%\src\minecraft\com
set mcMod=%APPDATA%\.minecraft\mods

if Exist %dest%\shiny rmdir /s %dest%\shiny
copy resources\mcmod.info %mcp%
xcopy %src% %dest% /E
pushd .
cd %mcp%
call recompile.bat
call reobfuscate.bat
copy mcmod.info reobf\minecraft

start reobf\minecraft
echo Zip up the mcMod.info along with the COM folder, name it JoypadMod1.6.4.zip and press any key to continue
pause
if exist reobf\minecraft\JoypadMod1.6.4*.zip goto copystuff

goto end

:copystuff

erase %mcMod%\JoypadMod1.6.4*.zip
copy reobf\minecraft\JoypadMod1.6.4*.zip %mcMod%
rmdir /s %dest%\shiny

:end
popd