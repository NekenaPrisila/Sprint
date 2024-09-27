@echo off
setlocal

REM Définition des chemins
set "SERVLET_API=%cd%\lib\servlet-api.jar"
set "GSON_JAR=%cd%\lib\gson-2.10.1.jar"
set "SRC=%cd%\src"
set "OUTPUT_DIR=%cd%\bin"
set "JAR_FILE=%cd%\framework.jar"

REM Vérifier si le dossier de sortie existe, sinon le créer
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

REM Compilation des fichiers Java en incluant les dépendances
for /R "%SRC%" %%f in (*.java) do (
    javac -cp "%SERVLET_API%;%GSON_JAR%;%SRC%" -d "%OUTPUT_DIR%" "%%f"
)

REM Vérifier si la compilation a réussi
if %ERRORLEVEL% NEQ 0 (
    echo Erreur lors de la compilation.
    exit /b %ERRORLEVEL%
)

REM Aller dans le répertoire bin
cd /d "%OUTPUT_DIR%"

REM Création du fichier JAR
jar cvf "%JAR_FILE%" *

REM Retour au répertoire initial
cd /d "%~dp0"

echo JAR creation completed.
pause
