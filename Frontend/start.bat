@echo off
echo ========================================
echo   AGROSAN - Servidor Frontend Local
echo ========================================
echo.
echo Iniciando servidor en http://localhost:5500
echo.
echo Presiona Ctrl+C para detener el servidor
echo.

REM Intentar con Python primero
python --version >nul 2>&1
if %errorlevel% == 0 (
    echo Usando Python...
    python -m http.server 5500
    goto :end
)

REM Si no hay Python, intentar con Node.js
node --version >nul 2>&1
if %errorlevel% == 0 (
    echo Usando Node.js...
    npx http-server -p 5500
    goto :end
)

REM Si no hay ninguno, abrir directamente
echo No se encontro Python ni Node.js.
echo Abriendo index.html directamente...
start index.html
goto :end

:end
pause


