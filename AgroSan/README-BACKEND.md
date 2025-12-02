# 🚀 Backend AgroSan - Guía de Inicio

## 📋 Requisitos Previos

1. **Java 17** instalado
   - Verificar: `java -version`
   - Descargar: https://adoptium.net/

2. **Maven** instalado (o usar Maven Wrapper incluido)
   - Verificar: `mvn --version`
   - Descargar: https://maven.apache.org/download.cgi

3. **SQL Server** corriendo
   - Puerto: `1433`
   - Base de datos: `AgroSanDB` debe estar creada
   - Usuario: `sa` (o el que configuraste)
   - Contraseña: `sql` (o la que configuraste)

## 🏃 Inicio Rápido

### Opción 1: Script Automático (Recomendado)

**Windows:**
```bash
cd AgroSan
start-backend-simple.bat
```

**O con verificación completa:**
```bash
cd AgroSan
start-backend.bat
```

### Opción 2: Manualmente con Maven

```bash
cd AgroSan
mvn spring-boot:run
```

### Opción 3: Con Maven Wrapper (sin instalar Maven)

```bash
cd AgroSan
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### Opción 4: Desde tu IDE

1. Abre el proyecto en IntelliJ IDEA o Eclipse
2. Busca `AgroSanApplication.java`
3. Click derecho → Run 'AgroSanApplication'

## ⚙️ Configuración

### Base de Datos

Edita `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=AgroSanDB;encrypt=true;TrustServerCertificate=True;
spring.datasource.username=sa
spring.datasource.password=sql
```

**Ajusta según tu configuración:**
- `localhost:1433` → Tu servidor SQL Server
- `AgroSanDB` → Nombre de tu base de datos
- `sa` → Tu usuario de SQL Server
- `sql` → Tu contraseña

### Puerto del Servidor

Por defecto corre en `http://localhost:8080`

Para cambiar, agrega en `application.properties`:
```properties
server.port=8080
```

## ✅ Verificar que Funciona

Una vez iniciado, deberías ver:
```
Started AgroSanApplication in X.XXX seconds
```

Prueba en el navegador o Postman:
```
GET http://localhost:8080/agrosan/parcela/listar
```

Deberías recibir un JSON con las parcelas (o un array vacío si no hay datos).

## 🐛 Solución de Problemas

### Error: "Cannot connect to database"
- Verifica que SQL Server esté corriendo
- Verifica que la base de datos `AgroSanDB` exista
- Verifica usuario y contraseña en `application.properties`
- Verifica que el puerto 1433 esté abierto

### Error: "Port 8080 already in use"
- Cierra otras aplicaciones que usen el puerto 8080
- O cambia el puerto en `application.properties`:
  ```properties
  server.port=8081
  ```

### Error: "Maven not found"
- Instala Maven y agrégalo al PATH
- O usa el Maven Wrapper: `mvnw.cmd spring-boot:run`

### Error: "Java version"
- Verifica que tengas Java 17 instalado
- `java -version` debe mostrar versión 17 o superior

### Error al compilar
- Limpia el proyecto: `mvn clean`
- Recompila: `mvn compile`
- Verifica que todas las dependencias estén descargadas

## 📝 Endpoints Disponibles

Una vez corriendo, los endpoints están en:
- Base URL: `http://localhost:8080/agrosan`
- Ejemplos:
  - `GET /agrosan/parcela/listar`
  - `GET /agrosan/empleado/listar`
  - `POST /agrosan/empleado/autenticar`
  - etc.

## 🔗 Conectar con el Frontend

El frontend está configurado para conectarse a:
- `http://localhost:8080`

Si cambias el puerto del backend, actualiza `API_BASE_URL` en:
- `Frontend/js/api.js`


