@echo off
chcp 65001 >nul
REM ============================================================
REM Script de compilation et d'exécution - Windows
REM Gestion des Mots de Passe - Province de Safi
REM ============================================================

set PROJ_DIR=%~dp0
set SRC_DIR=%PROJ_DIR%src
set OUT_DIR=%PROJ_DIR%out
set LIB_DIR=%PROJ_DIR%lib
set WEB_DIR=%PROJ_DIR%web

REM Trouver le fichier JDBC
set JDBC_JAR=%LIB_DIR%\mssql-jdbc-13.4.0.jre11.jar
if not exist "%JDBC_JAR%" (
    echo [ERREUR] Fichier JDBC introuvable : %JDBC_JAR%
    exit /b 1
)

echo ========================================
echo Gestion des Mots de Passe - Province de Safi
echo Compilation en cours...
echo ========================================

REM Nettoyage
if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
mkdir "%OUT_DIR%"

REM Compilation de TOUS les fichiers Java
dir /s /b "%SRC_DIR%\*.java" > "%TEMP%\sources.txt"
javac --add-modules jdk.httpserver -cp "%JDBC_JAR%" -d "%OUT_DIR%" @"%TEMP%\sources.txt"

if %ERRORLEVEL% NEQ 0 (
    echo [ERREUR] Echec de la compilation
    pause
    exit /b %ERRORLEVEL%
)

echo [SUCCES] Compilation reussie.

echo.
echo ╔══════════════════════════════════════════════════╗
echo ║  Gestion des Mots de Passe - Province de Safi  ║
echo ╠══════════════════════════════════════════════════╣
echo ║  Serveur demarre: http://localhost:8080         ║
echo ║  Login admin:       admin / admin123            ║
echo ╚══════════════════════════════════════════════════╝
echo.

echo ========================================
echo Lancement de l'application...
echo ========================================
echo.
echo JDBC: %JDBC_JAR%
echo OUT:  %OUT_DIR%
echo WEB:  %WEB_DIR%
echo.

java -Dfile.encoding=UTF-8 --add-modules jdk.httpserver -cp "%OUT_DIR%;%JDBC_JAR%" ma.province.safi.passwordmanager.Main

pause
