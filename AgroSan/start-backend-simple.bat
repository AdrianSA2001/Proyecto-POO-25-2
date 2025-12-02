@echo off
echo ========================================
echo   AGROSAN - Iniciar Backend (Simple)
echo ========================================
echo.
echo Iniciando servidor Spring Boot...
echo El servidor estara disponible en: http://localhost:8080
echo.
echo Presiona Ctrl+C para detener
echo.

REM Usar Maven Wrapper si existe, sino usar Maven
if exist "mvnw.cmd" (
    echo Usando Maven Wrapper...
    call mvnw.cmd spring-boot:run
) else (
    echo Usando Maven...
    call mvn spring-boot:run
)

pause


