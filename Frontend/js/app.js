/**
 * Aplicación Principal AgroSan
 */

// Estado global
const AppState = {
  currentSection: 'dashboard',
  data: {
    parcelas: [],
    siembras: [],
    cosechas: [],
    actividades: [],
    ventas: [],
    empleados: [],
    alertas: []
  }
};

// Inicialización
document.addEventListener('DOMContentLoaded', () => {
  // Verificar autenticación
  if (!AuthService.requireAuth()) {
    return;
  }
  
  // Cargar información del usuario
  loadUserInfo();
  
  // Configurar navegación
  setupNavigation();
  
  // Cargar sección inicial
  loadSection('dashboard');
  
  // Cargar datos iniciales
  loadDashboardData();
});

// Cargar información del usuario
function loadUserInfo() {
  const user = AuthService.getCurrentUser();
  if (user) {
    document.getElementById('userName').textContent = `${user.nombre} ${user.apellido}`;
    document.getElementById('userRole').textContent = user.estado || 'Empleado';
    const avatar = document.getElementById('userAvatar');
    avatar.textContent = user.nombre ? user.nombre.charAt(0).toUpperCase() : 'U';
  }
}

// Configurar navegación
function setupNavigation() {
  const navItems = document.querySelectorAll('.nav-item');
  navItems.forEach(item => {
    item.addEventListener('click', (e) => {
      e.preventDefault();
      const section = item.getAttribute('data-section');
      
      // Actualizar navegación activa
      navItems.forEach(nav => nav.classList.remove('active'));
      item.classList.add('active');
      
      // Cargar sección
      loadSection(section);
    });
  });
}

// Cargar sección
function loadSection(section) {
  AppState.currentSection = section;
  
  // Ocultar todas las secciones
  document.querySelectorAll('.section').forEach(sec => {
    sec.classList.remove('active');
  });
  
  // Mostrar sección actual
  const sectionElement = document.getElementById(`${section}-section`);
  if (sectionElement) {
    sectionElement.classList.add('active');
  }
  
  // Actualizar título
  const titles = {
    dashboard: 'Dashboard',
    parcelas: 'Parcelas',
    siembras: 'Siembras',
    cosechas: 'Cosechas',
    actividades: 'Actividades',
    ventas: 'Ventas',
    empleados: 'Empleados',
    alertas: 'Alertas',
    admin: 'Administración'
  };
  document.getElementById('pageTitle').textContent = titles[section] || 'AgroSan';
  
  // Cargar datos según la sección
  switch(section) {
    case 'dashboard':
      loadDashboardData();
      break;
    case 'parcelas':
      loadParcelas('todas');
      break;
    case 'siembras':
      loadSiembras();
      break;
    case 'cosechas':
      loadCosechas();
      break;
    case 'actividades':
      loadActividades('pendientes');
      break;
    case 'ventas':
      loadVentas();
      break;
    case 'empleados':
      loadEmpleados('todos');
      break;
    case 'alertas':
      loadAlertas();
      break;
    case 'admin':
      loadAdminData();
      break;
  }
}

// ========== DASHBOARD ==========
async function loadDashboardData() {
  try {
    // Actualizar fecha y saludo
    updateDashboardHeader();
    
    // Cargar estadísticas
    const [parcelas, alertas, ventas] = await Promise.all([
      ApiService.listarParcelas().catch(() => []),
      ApiService.obtenerAlertasActivas().catch(() => []),
      ApiService.listarVentas().catch(() => [])
    ]);
    
    // Contar parcelas activas
    const parcelasActivas = parcelas.filter(p => 
      p.id_estado_parcela === 2 || (p.estado_parcela || '').toLowerCase().includes('activa')
    ).length;
    
    document.getElementById('statParcelas').textContent = parcelasActivas || 0;
    document.getElementById('statAlertas').textContent = alertas.length || 0;
    document.getElementById('statVentas').textContent = ventas.length || 0;
    
    // Cargar actividades pendientes (manejar 404)
    try {
      const actividades = await ApiService.listarActividadesPendientes();
      document.getElementById('statActividades').textContent = actividades.length || 0;
    } catch (e) {
      document.getElementById('statActividades').textContent = '0';
    }
    
    // Cargar actividades de hoy (manejar 404)
    try {
      const actividadesHoy = await ApiService.listarActividadesHoy();
      renderActividadesHoy(actividadesHoy);
    } catch (e) {
      document.getElementById('actividadesHoyContent').innerHTML = 
        '<div class="empty-state" style="text-align: center; padding: 2rem;"><div style="font-size: 3rem; margin-bottom: 1rem;">✅</div><p>No hay actividades programadas para hoy</p></div>';
    }
  } catch (error) {
    console.error('Error al cargar datos del dashboard:', error);
    Toast.error('Error al cargar datos del dashboard');
  }
}

// Actualizar header del dashboard
function updateDashboardHeader() {
  // Fecha actual
  const now = new Date();
  const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
  const fechaFormateada = now.toLocaleDateString('es-PE', options);
  const dateElement = document.getElementById('currentDate');
  if (dateElement) {
    dateElement.textContent = fechaFormateada.charAt(0).toUpperCase() + fechaFormateada.slice(1);
  }
  
  // Saludo personalizado
  const user = AuthService.getCurrentUser();
  const hora = now.getHours();
  let saludo = 'Bienvenido';
  if (hora < 12) saludo = 'Buenos días';
  else if (hora < 18) saludo = 'Buenas tardes';
  else saludo = 'Buenas noches';
  
  const welcomeElement = document.getElementById('welcomeMessage');
  if (welcomeElement && user) {
    welcomeElement.textContent = `${saludo}, ${user.nombre}. ¡A trabajar!`;
  } else if (welcomeElement) {
    welcomeElement.textContent = `${saludo}. Bienvenido al sistema de gestión agrícola.`;
  }
}

function renderActividadesHoy(actividades) {
  const container = document.getElementById('actividadesHoyContent');
  
  if (!actividades || actividades.length === 0) {
    container.innerHTML = '<div class="empty-state"><p>No hay actividades programadas para hoy</p></div>';
    return;
  }
  
  const html = `
    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>Actividad</th>
            <th>Parcela</th>
            <th>Empleado</th>
            <th>Fecha</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          ${actividades.map(act => `
            <tr>
              <td>${act.actividad || act.tipo_actividad || '-'}</td>
              <td>${act.parcela || '-'}</td>
              <td>${act.empleado || '-'}</td>
              <td>${formatDate(act.fecha_programada)}</td>
              <td><span class="badge badge-${getEstadoBadge(act.estado)}">${act.estado || '-'}</span></td>
              <td>
                ${act.estado === 'Pendiente' ? `
                  <button class="btn btn-secondary" onclick="iniciarActividad(${act.id_actividad_programada || act.id_actividad})">Iniciar</button>
                ` : ''}
                ${act.estado === 'En Proceso' ? `
                  <button class="btn btn-primary" onclick="completarActividad(${act.id_actividad_programada || act.id_actividad})">Completar</button>
                ` : ''}
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
  
  container.innerHTML = html;
}

// ========== PARCELAS ==========
async function loadParcelas(tipo = 'todas') {
  const container = document.getElementById('parcelasContent');
  container.innerHTML = '<div class="loading-spinner"></div><p>Cargando...</p>';
  
  // Actualizar botón activo
  updateActiveTab('parcelas', tipo);
  
  try {
    let parcelas;
    if (tipo === 'disponibles') {
      parcelas = await ApiService.listarParcelasDisponibles();
    } else if (tipo === 'nodisponibles') {
      parcelas = await ApiService.listarParcelasNoDisponibles();
    } else {
      parcelas = await ApiService.listarParcelas();
    }
    
    renderParcelas(parcelas);
  } catch (error) {
    container.innerHTML = `<div class="error-message">${error.message}</div>`;
  }
}

function renderParcelas(parcelas) {
  const container = document.getElementById('parcelasContent');
  
  if (!parcelas || parcelas.length === 0) {
    container.innerHTML = '<div class="empty-state"><p>No hay parcelas registradas</p></div>';
    return;
  }
  
  const html = `
    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Ubicación</th>
            <th>Área (m²)</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          ${parcelas.map(p => {
            const estado = p.estado_parcela || p.estado || 'Desconocido';
            const isActiva = estado.toLowerCase().includes('activa') || estado === 'Activa';
            return `
            <tr>
              <td><strong>#${p.id_parcela || '-'}</strong></td>
              <td>${p.ubicacion || '-'}</td>
              <td>${formatNumber(p.area, 2)}</td>
              <td><span class="badge badge-${isActiva ? 'success' : 'warning'}">${estado}</span></td>
              <td>
                <button class="btn btn-secondary" onclick="verParcela(${p.id_parcela})" title="Ver detalles">
                  <svg width="16" height="16" viewBox="0 0 20 20" fill="currentColor" style="vertical-align: middle;">
                    <path d="M10 12a2 2 0 100-4 2 2 0 000 4z"/>
                    <path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd"/>
                  </svg>
                  Detalles
                </button>
              </td>
            </tr>
          `;
          }).join('')}
        </tbody>
      </table>
    </div>
  `;
  
  container.innerHTML = html;
}

function showParcelaForm() {
  if (typeof showParcelaFormModal === 'function') {
    showParcelaFormModal();
  } else {
    Toast.info('Cargando formulario...');
    setTimeout(() => showParcelaForm(), 100);
  }
}

// ========== SIEMBRAS ==========
async function loadSiembras() {
  const container = document.getElementById('siembrasContent');
  container.innerHTML = '<div class="loading-spinner"></div><p>Cargando...</p>';
  
  try {
    const siembras = await ApiService.listarSiembras();
    renderSiembras(siembras);
  } catch (error) {
    container.innerHTML = `<div class="error-message">${error.message}</div>`;
  }
}

function renderSiembras(siembras) {
  const container = document.getElementById('siembrasContent');
  
  if (!siembras || siembras.length === 0) {
    container.innerHTML = '<div class="empty-state"><p>No hay siembras registradas</p></div>';
    return;
  }
  
  const html = `
    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Cultivo</th>
            <th>Parcela</th>
            <th>Cantidad</th>
            <th>Fecha</th>
            <th>Empleado</th>
          </tr>
        </thead>
        <tbody>
          ${siembras.map(s => `
            <tr>
              <td>${s.id_siembra || '-'}</td>
              <td>${s.tipo_cultivo || s.cultivo || '-'}</td>
              <td>${s.parcela || '-'}</td>
              <td>${formatNumber(s.cantidad_sembrada, 2)}</td>
              <td>${formatDate(s.fecha_siembra)}</td>
              <td>${s.empleado || '-'}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
  
  container.innerHTML = html;
}

function showSiembraForm() {
  if (typeof showSiembraFormModal === 'function') {
    showSiembraFormModal();
  } else {
    Toast.info('Cargando formulario...');
    setTimeout(() => showSiembraForm(), 100);
  }
}

// ========== COSECHAS ==========
async function loadCosechas() {
  const container = document.getElementById('cosechasContent');
  container.innerHTML = '<div class="loading-spinner"></div><p>Cargando...</p>';
  
  try {
    const cosechas = await ApiService.listarCosechas();
    renderCosechas(cosechas);
  } catch (error) {
    container.innerHTML = `<div class="error-message">${error.message}</div>`;
  }
}

function renderCosechas(cosechas) {
  const container = document.getElementById('cosechasContent');
  
  if (!cosechas || cosechas.length === 0) {
    container.innerHTML = '<div class="empty-state"><p>No hay cosechas registradas</p></div>';
    return;
  }
  
  const html = `
    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Cultivo</th>
            <th>Parcela</th>
            <th>Cantidad (kg)</th>
            <th>Fecha</th>
            <th>Empleado</th>
          </tr>
        </thead>
        <tbody>
          ${cosechas.map(c => `
            <tr>
              <td>${c.id_cosecha || '-'}</td>
              <td>${c.tipo_cultivo || c.cultivo || '-'}</td>
              <td>${c.parcela || '-'}</td>
              <td>${formatNumber(c.cantidad_cosechada, 2)}</td>
              <td>${formatDate(c.fecha_cosecha)}</td>
              <td>${c.empleado || '-'}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
  
  container.innerHTML = html;
}

function showCosechaForm() {
  if (typeof showCosechaFormModal === 'function') {
    showCosechaFormModal();
  } else {
    Toast.info('Cargando formulario...');
    setTimeout(() => showCosechaForm(), 100);
  }
}

// ========== ACTIVIDADES ==========
async function loadActividades(tipo = 'pendientes') {
  const container = document.getElementById('actividadesContent');
  container.innerHTML = '<div class="loading-spinner"></div><p>Cargando...</p>';
  
  // Actualizar botón activo
  updateActiveTab('actividades', tipo);
  
  try {
    let actividades;
    if (tipo === 'hoy') {
      actividades = await ApiService.listarActividadesHoy();
    } else if (tipo === 'pendientes') {
      actividades = await ApiService.listarActividadesPendientes();
    } else {
      // Todas - implementar si existe endpoint
      actividades = await ApiService.listarActividadesPendientes();
    }
    
    renderActividades(actividades);
  } catch (error) {
    console.error('Error al cargar actividades:', error);
    container.innerHTML = `<div class="error-message">${error.message || 'Error al cargar actividades'}</div>`;
  }
}

function renderActividades(actividades) {
  const container = document.getElementById('actividadesContent');
  
  if (!actividades || actividades.length === 0) {
    container.innerHTML = '<div class="empty-state"><p>No hay actividades registradas</p></div>';
    return;
  }
  
  const html = `
    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Tipo</th>
            <th>Parcela</th>
            <th>Empleado</th>
            <th>Fecha Programada</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          ${actividades.map(a => `
            <tr>
              <td>${a.id_actividad_programada || a.id_actividad || '-'}</td>
              <td>${a.actividad || a.tipo_actividad || '-'}</td>
              <td>${a.parcela || '-'}</td>
              <td>${a.empleado || '-'}</td>
              <td>${formatDate(a.fecha_programada)}</td>
              <td><span class="badge badge-${getEstadoBadge(a.estado)}">${a.estado || '-'}</span></td>
              <td>
                ${a.estado === 'Pendiente' ? `
                  <button class="btn btn-secondary" onclick="iniciarActividad(${a.id_actividad_programada || a.id_actividad})">Iniciar</button>
                ` : ''}
                ${a.estado === 'En Proceso' ? `
                  <button class="btn btn-primary" onclick="completarActividad(${a.id_actividad_programada || a.id_actividad})">Completar</button>
                ` : ''}
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
  
  container.innerHTML = html;
}

async function iniciarActividad(id) {
  try {
    await ApiService.iniciarActividad(id);
    Toast.success('Actividad iniciada exitosamente');
    loadActividades('pendientes');
    loadDashboardData();
  } catch (error) {
    Toast.error(error.message);
  }
}

async function completarActividad(id) {
  try {
    await ApiService.completarActividad(id);
    Toast.success('Actividad completada exitosamente');
    loadActividades('pendientes');
    loadDashboardData();
  } catch (error) {
    Toast.error(error.message);
  }
}

function showActividadForm() {
  if (typeof showActividadFormModal === 'function') {
    showActividadFormModal();
  } else {
    Toast.info('Cargando formulario...');
    setTimeout(() => showActividadForm(), 100);
  }
}

// ========== VENTAS ==========
async function loadVentas() {
  const container = document.getElementById('ventasContent');
  container.innerHTML = '<div class="loading-spinner"></div><p>Cargando...</p>';
  
  try {
    const ventas = await ApiService.listarVentas();
    renderVentas(ventas);
  } catch (error) {
    container.innerHTML = `<div class="error-message">${error.message}</div>`;
  }
}

function renderVentas(ventas) {
  const container = document.getElementById('ventasContent');
  
  if (!ventas || ventas.length === 0) {
    container.innerHTML = '<div class="empty-state"><p>No hay ventas registradas</p></div>';
    return;
  }
  
  const html = `
    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Comprador</th>
            <th>Cultivo</th>
            <th>Cantidad</th>
            <th>Precio Unitario</th>
            <th>Total</th>
            <th>Fecha</th>
          </tr>
        </thead>
        <tbody>
          ${ventas.map(v => `
            <tr>
              <td>${v.id_venta || '-'}</td>
              <td>${v.comprador || '-'}</td>
              <td>${v.tipo_cultivo || v.cultivo || '-'}</td>
              <td>${formatNumber(v.cantidad_vendida || v.cantidad, 2)}</td>
              <td>${formatCurrency(v.precio_unitario)}</td>
              <td>${formatCurrency(v.precio_total || v.total)}</td>
              <td>${formatDate(v.fecha_venta)}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
  
  container.innerHTML = html;
}

function showVentaForm() {
  if (typeof showVentaFormModal === 'function') {
    showVentaFormModal();
  } else {
    Toast.info('Cargando formulario...');
    setTimeout(() => showVentaForm(), 100);
  }
}

// ========== EMPLEADOS ==========
async function loadEmpleados(tipo = 'todos') {
  const container = document.getElementById('empleadosContent');
  container.innerHTML = '<div class="loading-spinner"></div><p>Cargando...</p>';
  
  // Actualizar botón activo
  updateActiveTab('empleados', tipo);
  
  try {
    let empleados;
    switch (tipo) {
      case 'activos':
        empleados = await ApiService.listarEmpleadosActivos();
        break;
      case 'vacaciones':
        empleados = await ApiService.listarEmpleadosVacaciones();
        break;
      case 'retirados':
        empleados = await ApiService.listarEmpleadosRetirados();
        break;
      default:
        empleados = await ApiService.listarEmpleados();
    }
    
    renderEmpleados(empleados);
  } catch (error) {
    // Si es un error de "no hay empleados", mostrar mensaje amigable
    const mensaje = error.message || 'Error al cargar empleados';
    if (mensaje.includes('No hay empleados')) {
      container.innerHTML = `<div class="empty-state"><p>${mensaje}</p></div>`;
    } else {
      container.innerHTML = `<div class="error-message">${mensaje}</div>`;
    }
  }
}

function renderEmpleados(empleados) {
  const container = document.getElementById('empleadosContent');
  
  if (!empleados || empleados.length === 0) {
    container.innerHTML = '<div class="empty-state"><p>No hay empleados registrados</p></div>';
    return;
  }
  
  const html = `
    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Foto</th>
            <th>Nombre Completo</th>
            <th>DNI</th>
            <th>Contacto</th>
            <th>Rol</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          ${empleados.map(e => {
            const estado = e.estado || 'Activo';
            const badgeClass = estado === 'Activo' ? 'success' : 
                               estado === 'Vacaciones' ? 'warning' : 
                               estado === 'Retirado' ? 'error' : 'secondary';
            // Simular rol basado en ID (1-2 = Admin, otros = Empleado)
            const esAdmin = e.id_empleado <= 2;
            const rol = esAdmin ? 'Administrador' : 'Empleado';
            const rolBadge = esAdmin ? 'admin' : 'empleado';
            // Generar avatar con iniciales
            const iniciales = ((e.nombre?.[0] || '') + (e.apellido?.[0] || '')).toUpperCase();
            const avatarColor = esAdmin ? '#8B5CF6' : '#06B6D4';
            return `
            <tr>
              <td>${e.id_empleado || '-'}</td>
              <td>
                <div class="avatar" style="
                  width: 40px; 
                  height: 40px; 
                  border-radius: 50%; 
                  background: ${avatarColor}; 
                  color: white; 
                  display: flex; 
                  align-items: center; 
                  justify-content: center; 
                  font-weight: bold;
                  font-size: 14px;
                ">${iniciales}</div>
              </td>
              <td><strong>${e.nombre || ''} ${e.apellido || ''}</strong></td>
              <td>${e.dni || '-'}</td>
              <td>
                <div style="font-size: 0.85rem;">
                  📱 ${e.telefono || '-'}<br>
                  ✉️ ${e.email || '-'}
                </div>
              </td>
              <td><span class="badge badge-${rolBadge}">${rol}</span></td>
              <td><span class="badge badge-${badgeClass}">${estado}</span></td>
              <td>
                <button class="btn btn-secondary" onclick="verEmpleado(${e.id_empleado})">Ver</button>
              </td>
            </tr>
          `;}).join('')}
        </tbody>
      </table>
    </div>
  `;
  
  container.innerHTML = html;
}

function showEmpleadoForm() {
  if (typeof showEmpleadoFormModal === 'function') {
    showEmpleadoFormModal();
  } else {
    Toast.info('Cargando formulario...');
    setTimeout(() => showEmpleadoForm(), 100);
  }
}

async function verEmpleado(id) {
  if (typeof showEmpleadoDetailsModal === 'function') {
    showEmpleadoDetailsModal(id);
  } else {
    Toast.info('Cargando detalles...');
    setTimeout(() => verEmpleado(id), 100);
  }
}

// ========== ALERTAS ==========
async function loadAlertas() {
  const container = document.getElementById('alertasContent');
  container.innerHTML = '<div class="loading-spinner"></div><p>Cargando...</p>';
  
  try {
    const alertas = await ApiService.obtenerAlertasActivas();
    renderAlertas(alertas);
  } catch (error) {
    container.innerHTML = `<div class="error-message">${error.message}</div>`;
  }
}

function renderAlertas(alertas) {
  const container = document.getElementById('alertasContent');
  
  if (!alertas || alertas.length === 0) {
    container.innerHTML = '<div class="empty-state"><p>No hay alertas activas</p></div>';
    return;
  }
  
  const html = `
    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>Tipo</th>
            <th>Mensaje</th>
            <th>Prioridad</th>
            <th>Fecha</th>
          </tr>
        </thead>
        <tbody>
          ${alertas.map(a => `
            <tr>
              <td>${a.tipo || '-'}</td>
              <td>${a.mensaje || '-'}</td>
              <td><span class="badge badge-${getPrioridadBadge(a.prioridad)}">${a.prioridad || '-'}</span></td>
              <td>${formatDate(a.fecha)}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
  
  container.innerHTML = html;
}

// ========== ADMIN ==========
async function loadAdminData() {
  try {
    const [parcelas, siembras, cosechas, ventas, empleados] = await Promise.all([
      ApiService.listarParcelas().catch(() => []),
      ApiService.listarSiembras().catch(() => []),
      ApiService.listarCosechas().catch(() => []),
      ApiService.listarVentas().catch(() => []),
      ApiService.listarEmpleados().catch(() => [])
    ]);
    
    // Actualizar estadísticas principales
    document.getElementById('adminTotalParcelas').textContent = parcelas.length || 0;
    document.getElementById('adminTotalSiembras').textContent = siembras.length || 0;
    document.getElementById('adminTotalCosechas').textContent = cosechas.length || 0;
    document.getElementById('adminTotalVentas').textContent = ventas.length || 0;
    
    // Actualizar última actualización
    const now = new Date();
    document.getElementById('adminLastUpdate').textContent = 
      now.toLocaleDateString('es-PE') + ' ' + now.toLocaleTimeString('es-PE', {hour: '2-digit', minute: '2-digit'});
    
    // Gráfico circular de parcelas
    const parcelasActivas = parcelas.filter(p => p.id_estado_parcela === 2 || (p.estado_parcela || '').toLowerCase().includes('activa')).length;
    const parcelasInactivas = parcelas.length - parcelasActivas;
    renderParcelasChart(parcelasActivas, parcelasInactivas);
    
    // Gráfico de barras de actividad
    renderActividadBars(siembras.length, cosechas.length, ventas.length, empleados.length);
    
    // Equipo de trabajo
    renderEquipo(empleados);
    
  } catch (error) {
    console.error('Error al cargar datos de administración:', error);
    Toast.error('Error al cargar datos de administración');
  }
}

// Renderizar gráfico circular de parcelas
function renderParcelasChart(activas, inactivas) {
  const total = activas + inactivas;
  const porcentajeActivas = total > 0 ? (activas / total) * 100 : 0;
  
  const chartContainer = document.getElementById('parcelasChart');
  const legendContainer = document.getElementById('parcelasLegend');
  
  if (chartContainer) {
    chartContainer.innerHTML = `
      <div style="
        width: 150px;
        height: 150px;
        border-radius: 50%;
        background: conic-gradient(
          #4ade80 0deg ${porcentajeActivas * 3.6}deg,
          #f87171 ${porcentajeActivas * 3.6}deg 360deg
        );
        display: flex;
        align-items: center;
        justify-content: center;
      ">
        <div style="
          width: 90px;
          height: 90px;
          border-radius: 50%;
          background: var(--card-bg, #1e1e2e);
          display: flex;
          align-items: center;
          justify-content: center;
          flex-direction: column;
        ">
          <div style="font-size: 1.5rem; font-weight: bold;">${total}</div>
          <div style="font-size: 0.7rem; opacity: 0.7;">Total</div>
        </div>
      </div>
    `;
  }
  
  if (legendContainer) {
    legendContainer.innerHTML = `
      <div style="display: flex; flex-direction: column; gap: 0.75rem;">
        <div style="display: flex; align-items: center; gap: 0.5rem;">
          <div style="width: 16px; height: 16px; border-radius: 4px; background: #4ade80;"></div>
          <span>Activas: <strong>${activas}</strong></span>
        </div>
        <div style="display: flex; align-items: center; gap: 0.5rem;">
          <div style="width: 16px; height: 16px; border-radius: 4px; background: #f87171;"></div>
          <span>Inactivas: <strong>${inactivas}</strong></span>
        </div>
        <div style="margin-top: 0.5rem; padding-top: 0.5rem; border-top: 1px solid var(--border-color, #333);">
          <small style="opacity: 0.7;">Tasa de uso: ${porcentajeActivas.toFixed(1)}%</small>
        </div>
      </div>
    `;
  }
}

// Renderizar gráfico de barras de actividad
function renderActividadBars(siembras, cosechas, ventas, empleados) {
  const container = document.getElementById('actividadBars');
  if (!container) return;
  
  const max = Math.max(siembras, cosechas, ventas, empleados, 1);
  
  const data = [
    { label: 'Siembras', value: siembras, color: '#f093fb' },
    { label: 'Cosechas', value: cosechas, color: '#4facfe' },
    { label: 'Ventas', value: ventas, color: '#fa709a' },
    { label: 'Empleados', value: empleados, color: '#667eea' }
  ];
  
  container.innerHTML = data.map(item => {
    const height = (item.value / max) * 100;
    return `
      <div style="flex: 1; display: flex; flex-direction: column; align-items: center;">
        <div style="font-size: 0.85rem; font-weight: bold; margin-bottom: 0.25rem;">${item.value}</div>
        <div style="
          width: 100%;
          height: ${Math.max(height, 5)}%;
          background: linear-gradient(to top, ${item.color}, ${item.color}aa);
          border-radius: 8px 8px 0 0;
          min-height: 20px;
          transition: height 0.5s ease;
        "></div>
        <div style="font-size: 0.7rem; margin-top: 0.5rem; text-align: center; opacity: 0.8;">${item.label}</div>
      </div>
    `;
  }).join('');
}

// Renderizar equipo de trabajo
function renderEquipo(empleados) {
  const container = document.getElementById('adminEquipo');
  if (!container) return;
  
  const admins = empleados.filter(e => e.id_empleado <= 2);
  const trabajadores = empleados.filter(e => e.id_empleado > 2);
  
  container.innerHTML = `
    <div style="display: grid; gap: 1rem;">
      <!-- Admins -->
      <div>
        <div style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 0.5rem;">
          👑 Administradores (${admins.length})
        </div>
        <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
          ${admins.map(e => `
            <div style="
              display: flex;
              align-items: center;
              gap: 0.5rem;
              padding: 0.5rem 0.75rem;
              background: linear-gradient(135deg, #8B5CF6, #7C3AED);
              border-radius: 20px;
              color: white;
              font-size: 0.85rem;
            ">
              <div style="
                width: 24px;
                height: 24px;
                border-radius: 50%;
                background: rgba(255,255,255,0.2);
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 0.7rem;
              ">${(e.nombre?.[0] || '')}</div>
              ${e.nombre} ${e.apellido?.[0] || ''}.
            </div>
          `).join('')}
          ${admins.length === 0 ? '<span style="opacity: 0.5;">Sin administradores</span>' : ''}
        </div>
      </div>
      
      <!-- Empleados -->
      <div>
        <div style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 0.5rem;">
          👷 Empleados (${trabajadores.length})
        </div>
        <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
          ${trabajadores.slice(0, 6).map(e => `
            <div style="
              display: flex;
              align-items: center;
              gap: 0.5rem;
              padding: 0.5rem 0.75rem;
              background: linear-gradient(135deg, #06B6D4, #0891B2);
              border-radius: 20px;
              color: white;
              font-size: 0.85rem;
            ">
              <div style="
                width: 24px;
                height: 24px;
                border-radius: 50%;
                background: rgba(255,255,255,0.2);
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 0.7rem;
              ">${(e.nombre?.[0] || '')}</div>
              ${e.nombre} ${e.apellido?.[0] || ''}.
            </div>
          `).join('')}
          ${trabajadores.length > 6 ? `<span style="padding: 0.5rem; opacity: 0.7;">+${trabajadores.length - 6} más</span>` : ''}
          ${trabajadores.length === 0 ? '<span style="opacity: 0.5;">Sin empleados</span>' : ''}
        </div>
      </div>
      
      <!-- Estadística rápida -->
      <div style="
        margin-top: 0.5rem;
        padding: 1rem;
        background: var(--background);
        border-radius: 8px;
        display: flex;
        justify-content: space-around;
        text-align: center;
      ">
        <div>
          <div style="font-size: 1.5rem; font-weight: bold; color: #4ade80;">${empleados.length}</div>
          <div style="font-size: 0.75rem; opacity: 0.7;">Total Equipo</div>
        </div>
        <div>
          <div style="font-size: 1.5rem; font-weight: bold; color: #8B5CF6;">${admins.length}</div>
          <div style="font-size: 0.75rem; opacity: 0.7;">Admins</div>
        </div>
        <div>
          <div style="font-size: 1.5rem; font-weight: bold; color: #06B6D4;">${trabajadores.length}</div>
          <div style="font-size: 0.75rem; opacity: 0.7;">Empleados</div>
        </div>
      </div>
    </div>
  `;
}

// ========== UTILIDADES ==========
function getEstadoBadge(estado) {
  const estados = {
    'Pendiente': 'warning',
    'En Proceso': 'info',
    'Completada': 'success',
    'Cancelada': 'error'
  };
  return estados[estado] || 'info';
}

function getPrioridadBadge(prioridad) {
  const prioridades = {
    'Alta': 'error',
    'Media': 'warning',
    'Baja': 'info'
  };
  return prioridades[prioridad] || 'info';
}

async function verParcela(id) {
  try {
    // Cargar datos de la parcela
    const [parcela, siembras, cosechas, actividades] = await Promise.all([
      ApiService.obtenerParcela(id).catch(() => null),
      ApiService.listarSiembrasPorParcela(id).catch(() => []),
      ApiService.listarCosechasPorParcela(id).catch(() => []),
      ApiService.listarActividadesPorParcela(id).catch(() => [])
    ]);
    
    if (!parcela) {
      Toast.error('No se pudo cargar la información de la parcela');
      return;
    }
    
    // Determinar estado
    const estado = parcela.id_estado_parcela === 2 ? 'Activa' : 'Inactiva';
    const estadoBadge = estado === 'Activa' ? 'success' : 'warning';
    
    const content = `
      <div class="parcela-details">
        <div class="detail-section">
          <h3>Información General</h3>
          <div class="detail-grid">
            <div class="detail-item">
              <label>ID Parcela:</label>
              <span><strong>#${parcela.id_parcela}</strong></span>
            </div>
            <div class="detail-item">
              <label>Ubicación:</label>
              <span>${parcela.ubicacion || 'No especificada'}</span>
            </div>
            <div class="detail-item">
              <label>Área:</label>
              <span>${formatNumber(parcela.area, 2)} m²</span>
            </div>
            <div class="detail-item">
              <label>Estado:</label>
              <span><span class="badge badge-${estadoBadge}">${estado}</span></span>
            </div>
          </div>
        </div>
        
        <div class="detail-section">
          <h3>Historial de Siembras (${siembras.length})</h3>
          ${siembras.length > 0 ? `
            <div class="table-container" style="max-height: 200px; overflow-y: auto;">
              <table>
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Cultivo</th>
                    <th>Cantidad (kg)</th>
                  </tr>
                </thead>
                <tbody>
                  ${siembras.map(s => `
                    <tr>
                      <td>${formatDate(s.fecha_siembra)}</td>
                      <td>${s.cultivo || s.tipo_cultivo || '-'}</td>
                      <td>${formatNumber(s.cantidad_sembrada, 2)}</td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            </div>
          ` : '<p style="color: var(--text-secondary);">No hay siembras registradas</p>'}
        </div>
        
        <div class="detail-section">
          <h3>Historial de Cosechas (${cosechas.length})</h3>
          ${cosechas.length > 0 ? `
            <div class="table-container" style="max-height: 200px; overflow-y: auto;">
              <table>
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Cultivo</th>
                    <th>Cantidad (kg)</th>
                    <th>Empleado</th>
                  </tr>
                </thead>
                <tbody>
                  ${cosechas.map(c => `
                    <tr>
                      <td>${formatDate(c.fecha_cosecha)}</td>
                      <td>${c.tipo_cultivo || '-'}</td>
                      <td>${formatNumber(c.cantidad_cosechada, 2)}</td>
                      <td>${c.empleado || '-'}</td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            </div>
          ` : '<p style="color: var(--text-secondary);">No hay cosechas registradas</p>'}
        </div>
        
        <div class="detail-section">
          <h3>Actividades Programadas (${actividades.length})</h3>
          ${actividades.length > 0 ? `
            <div class="table-container" style="max-height: 200px; overflow-y: auto;">
              <table>
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Tipo</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  ${actividades.map(a => `
                    <tr>
                      <td>${formatDate(a.fecha_programada)}</td>
                      <td>${a.tipo_actividad || '-'}</td>
                      <td><span class="badge badge-${getEstadoBadge(a.estado)}">${a.estado || '-'}</span></td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            </div>
          ` : '<p style="color: var(--text-secondary);">No hay actividades programadas</p>'}
        </div>
        
        <div class="form-actions" style="margin-top: 20px;">
          ${parcela.id_estado_parcela === 1 ? `
            <button type="button" class="btn btn-primary" onclick="activarParcela(${parcela.id_parcela})">
              Activar Parcela
            </button>
          ` : ''}
          ${parcela.id_estado_parcela === 2 ? `
            <button type="button" class="btn btn-warning" onclick="inactivarParcela(${parcela.id_parcela})" style="background: var(--warning-color); color: white;">
              Inactivar Parcela
            </button>
          ` : ''}
          <button type="button" class="btn btn-secondary" onclick="closeModal()">Cerrar</button>
        </div>
      </div>
    `;
    
    const modal = createModal(`Detalles de Parcela #${id}`, content, true);
  } catch (error) {
    Toast.error('Error al cargar detalles: ' + error.message);
  }
}

// Activar parcela
async function activarParcela(id) {
  if (!confirm('¿Estás seguro de activar esta parcela? Una vez activada, podrá ser usada para sembrar.')) {
    return;
  }
  
  try {
    console.log('🔵 [APP] Intentando activar parcela:', id);
    await ApiService.activarParcela(id);
    Toast.success('Parcela activada exitosamente');
    closeModal();
    loadParcelas('todas');
    loadDashboardData();
  } catch (error) {
    console.error('❌ [APP] Error completo:', error);
    console.error('❌ [APP] Error data:', error.data);
    const errorMessage = error.message || error.data?.message || 'Error al activar la parcela';
    Toast.error(errorMessage);
  }
}

// Inactivar parcela
async function inactivarParcela(id) {
  if (!confirm('¿Estás seguro de inactivar esta parcela? Una vez inactiva, no podrá ser usada para sembrar hasta que se reactive.')) {
    return;
  }
  
  try {
    console.log('🟡 [APP] Intentando inactivar parcela:', id);
    await ApiService.inactivarParcela(id);
    Toast.success('Parcela inactivada exitosamente');
    closeModal();
    loadParcelas('todas');
    loadDashboardData();
  } catch (error) {
    console.error('❌ [APP] Error completo:', error);
    console.error('❌ [APP] Error data:', error.data);
    const errorMessage = error.message || error.data?.message || 'Error al inactivar la parcela';
    Toast.error(errorMessage);
  }
}

// Función para actualizar el botón activo en los tabs
function updateActiveTab(section, tipo) {
  // Mapeo de secciones a sus contenedores de tabs
  const tabContainers = {
    parcelas: document.querySelector('#parcelas-section .tabs'),
    actividades: document.querySelector('#actividades-section .tabs'),
    empleados: document.querySelector('#empleados-section .tabs')
  };
  
  // Mapeo de tipos a índices
  const tabIndexMap = {
    parcelas: { 'todas': 0, 'disponibles': 1, 'nodisponibles': 2 },
    actividades: { 'pendientes': 0, 'hoy': 1, 'todas': 2 },
    empleados: { 'todos': 0, 'activos': 1, 'vacaciones': 2, 'retirados': 3 }
  };
  
  const container = tabContainers[section];
  if (!container) return;
  
  const tabs = container.querySelectorAll('.tab');
  const activeIndex = tabIndexMap[section]?.[tipo] ?? 0;
  
  tabs.forEach((tab, index) => {
    if (index === activeIndex) {
      tab.classList.add('active');
    } else {
      tab.classList.remove('active');
    }
  });
}

// Hacer funciones globales para onclick
window.loadParcelas = loadParcelas;
window.loadActividades = loadActividades;
window.loadEmpleados = loadEmpleados;
window.showParcelaForm = showParcelaForm;
window.showSiembraForm = showSiembraForm;
window.showCosechaForm = showCosechaForm;
window.showActividadForm = showActividadForm;
window.showVentaForm = showVentaForm;
window.showEmpleadoForm = showEmpleadoForm;
window.iniciarActividad = iniciarActividad;
window.completarActividad = completarActividad;
window.verParcela = verParcela;
window.activarParcela = activarParcela;
window.inactivarParcela = inactivarParcela;
window.verEmpleado = verEmpleado;

// Función para crear modal (si no existe en modals.js)
if (typeof createModal === 'undefined') {
  window.createModal = function(title, content, isLarge = false) {
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    const modalClass = isLarge ? 'modal large' : 'modal';
    modal.innerHTML = `
      <div class="${modalClass}">
        <div class="modal-header">
          <h2>${title}</h2>
          <button class="modal-close" onclick="closeModal()">&times;</button>
        </div>
        <div class="modal-body">
          ${content}
        </div>
      </div>
    `;
    
    modal.addEventListener('click', (e) => {
      if (e.target === modal) {
        closeModal();
      }
    });
    
    document.body.appendChild(modal);
    return modal;
  };
}

// Inicializar Toast
Toast.init();

