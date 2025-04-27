@echo off
setlocal

echo [INFO] Creating installer for Board...

jpackage ^
  --type exe ^
  --input . ^
  --name AutoCheck ^
  --app-version 0.0.1 ^
  --main-jar AutoCheck.jar ^
  --main-class com.aloha.Main ^
  --java-options "--module-path app/lib/javafx-sdk-23.0.2/lib --add-modules javafx.controls,javafx.fxml -Xmx512m -Djava.awt.headless=false" ^
  --icon Auto.ico ^
  --win-dir-chooser ^
  --win-menu ^
  --win-shortcut

echo [INFO] Installer build complete.

pause
