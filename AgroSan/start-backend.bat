@echo off
echo ========================================
echo   AGROSAN - Iniciar Backend Spring Boot
echo ========================================
echo.

REM Verificar que estamos en el directorio correcto
if not exist "pom.xml" (
    echo ERROR: No se encontro pom.xml
    echo Asegurate de ejecutar este script desde la carpeta AgroSan
    pause
    exit /b 1
)

echo Verificando Maven...
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo ERROR: Maven no esta instalado o no esta en el PATH
    echo.
    echo Instalacion de Maven:
    echo 1. Descarga desde: https://maven.apache.org/download.cgi
    echo 2. Extrae y agrega a las variables de entorno PATH
    echo.
    echo O usa el Maven Wrapper incluido...
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo   Verificando Base de Datos
echo ========================================
echo.
echo IMPORTANTE: Asegurate de que:
echo   1. SQL Server este corriendo
echo   2. La base de datos AgroSanDB este creada
echo   3. Las credenciales en application.properties sean correctas
echo.
echo Presiona cualquier tecla para continuar o Ctrl+C para cancelar...
pause >nul

echo.
echo ========================================
echo   Compilando y Ejecutando Backend
echo ========================================
echo.
echo Esto puede tomar unos minutos la primera vez...
echo.

REM Limpiar y compilar
echo [1/3] Limpiando proyecto...
call mvn clean

echo.
echo [2/3] Compilando proyecto...
call mvn compile

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Error al compilar el proyecto
    echo Revisa los errores arriba
    pause
    exit /b 1
)

echo.
echo [3/3] Iniciando servidor Spring Boot...
echo.
echo El servidor se iniciara en: http://localhost:8080
echo.
echo Presiona Ctrl+C para detener el servidor
echo.

REM Ejecutar Spring Boot
call mvn spring-boot:run

pause


