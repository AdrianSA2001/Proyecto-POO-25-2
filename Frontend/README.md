# 🚀 Frontend AgroSan - Guía de Inicio Rápido

## 📋 Requisitos Previos

1. **Backend corriendo**: El servidor Spring Boot debe estar ejecutándose en `http://localhost:8080`
2. **Base de datos**: SQL Server debe estar corriendo con la base de datos `AgroSanDB` creada
3. **Navegador moderno**: Chrome, Firefox, Edge (últimas versiones)

## 🏃 Cómo Ejecutar

### Opción 1: Abrir directamente (más simple)
1. Abre `index.html` en tu navegador
2. Si aparece un error de CORS, usa la Opción 2

### Opción 2: Con servidor local (recomendado)

#### Usando Python (si lo tienes instalado):
```bash
# Python 3
cd Frontend
python -m http.server 5500

# Luego abre en el navegador:
# http://localhost:5500/index.html
```

#### Usando Node.js (si lo tienes instalado):
```bash
# Instalar http-server globalmente
npm install -g http-server

# Ejecutar
cd Frontend
http-server -p 5500

# Luego abre en el navegador:
# http://localhost:5500/index.html
```

#### Usando VS Code:
1. Instala la extensión "Live Server"
2. Click derecho en `index.html`
3. Selecciona "Open with Live Server"

## 🔐 Credenciales de Prueba

Según los datos de prueba en la base de datos:
- **DNI**: `12345678`
- **Contraseña**: `1234`

*(O usa cualquier empleado activo de tu base de datos)*

## 📁 Estructura de Archivos

```
Frontend/
├── index.html          # Página de login
├── app.html            # Aplicación principal
├── css/
│   └── styles.css      # Estilos completos
├── js/
│   ├── auth.js         # Autenticación
│   ├── api.js          # Servicios API
│   ├── utils.js        # Utilidades
│   └── app.js          # Lógica principal
└── README.md           # Este archivo
```

## ✨ Características

- ✅ Sistema de login moderno
- ✅ Protección de rutas (requiere autenticación)
- ✅ Dashboard con estadísticas
- ✅ Gestión completa de:
  - Parcelas
  - Siembras
  - Cosechas
  - Actividades
  - Ventas
  - Empleados
  - Alertas
  - Administración (protegido)

## 🐛 Solución de Problemas

### Error de CORS
Si ves errores de CORS, asegúrate de:
1. Abrir el frontend desde `http://localhost:5500` o `http://127.0.0.1:5500`
2. El backend tiene CORS configurado para estos orígenes

### No se conecta al backend
1. Verifica que el backend esté corriendo en `http://localhost:8080`
2. Abre la consola del navegador (F12) para ver errores
3. Verifica la URL en `js/api.js` si cambiaste el puerto

### El login no funciona
1. Verifica que el backend esté corriendo
2. Verifica que la base de datos tenga empleados activos
3. Revisa la consola del navegador para errores

## 🎨 Personalización

- **Cambiar puerto del backend**: Edita `API_BASE_URL` en `js/api.js`
- **Cambiar colores**: Edita las variables CSS en `css/styles.css`
- **Agregar funcionalidades**: Edita `js/app.js`

## 📝 Notas

- Los formularios de creación muestran "en desarrollo" - puedes implementarlos después
- Todas las pestañas están protegidas y requieren login
- La sesión se mantiene en localStorage


