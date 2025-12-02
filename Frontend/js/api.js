/**
 * Servicio API
 * Maneja todas las llamadas al backend
 */
const API_BASE_URL = 'http://localhost:8080/agrosan';

const ApiService = {
  /**
   * Realizar petición HTTP genérica
   */
  async request(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const defaultOptions = {
      headers: {
        'Content-Type': 'application/json',
      },
    };
    
    const config = { ...defaultOptions, ...options };
    
    console.log('📤 [API Request]', {
      method: config.method || 'GET',
      url: url,
      headers: config.headers,
      body: config.body
    });
    
    try {
      const response = await fetch(url, config);
      console.log('📥 [API Response]', {
        status: response.status,
        statusText: response.statusText,
        url: response.url,
        headers: Object.fromEntries(response.headers.entries())
      });
      
      // Manejar respuestas sin contenido JSON
      let data;
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        data = await response.json();
      } else {
        // Si no hay JSON pero es 404, retornar array vacío
        if (response.status === 404) {
          return [];
        }
        data = { message: `Error ${response.status}` };
      }
      
      if (!response.ok) {
        // Intentar obtener el mensaje de error del backend
        let errorMessage = `Error ${response.status}`;
        if (data && data.message) {
          errorMessage = data.message;
        } else if (data && typeof data === 'string') {
          errorMessage = data;
        } else if (data && data.error) {
          errorMessage = data.error;
        }
        const error = new Error(errorMessage);
        error.status = response.status;
        error.data = data; // Guardar los datos completos del error
        console.error('❌ [API] Error response data:', data);
        throw error;
      }
      
      return data;
    } catch (error) {
      // Si es un error de red, mantener el error original
      if (!error.status && error.message) {
        throw error;
      }
      // Si tiene status, agregarlo al mensaje
      if (error.status) {
        error.message = error.message || `Error ${error.status}`;
      }
      throw error;
    }
  },
  
  // ========== EMPLEADOS ==========
  async listarEmpleados() {
    return this.request('/empleado/listar');
  },
  
  async obtenerEmpleado(id) {
    return this.request(`/empleado/obtener/${id}`);
  },
  
  async registrarEmpleado(empleado) {
    return this.request('/empleado/registrar', {
      method: 'POST',
      body: JSON.stringify(empleado),
    });
  },
  
  async listarEmpleadosActivos() {
    try {
      return await this.request('/empleado/activos');
    } catch (error) {
      // Si no hay empleados activos, retornar array vacío
      if (error.status === 500 || error.message?.includes('No hay empleados')) {
        return [];
      }
      throw error;
    }
  },
  
  async listarEmpleadosVacaciones() {
    try {
      return await this.request('/empleado/vacaciones');
    } catch (error) {
      // Si no hay empleados de vacaciones, retornar array vacío
      if (error.status === 500 || error.message?.includes('No hay empleados')) {
        return [];
      }
      throw error;
    }
  },
  
  async listarEmpleadosRetirados() {
    try {
      return await this.request('/empleado/retirados');
    } catch (error) {
      // Si no hay empleados retirados, retornar array vacío
      if (error.status === 500 || error.message?.includes('No hay empleados')) {
        return [];
      }
      throw error;
    }
  },
  
  async listarEmpleadosDespedidos() {
    try {
      return await this.request('/empleado/despedidos');
    } catch (error) {
      // Si no hay empleados despedidos, retornar array vacío
      if (error.status === 500 || error.message?.includes('No hay empleados')) {
        return [];
      }
      throw error;
    }
  },
  
  // ========== PARCELAS ==========
  async listarParcelas() {
    return this.request('/parcela/listar');
  },
  
  async obtenerParcela(id) {
    return this.request(`/parcela/obtener/${id}`);
  },
  
  async registrarParcela(parcela) {
    return this.request('/parcela/registrar', {
      method: 'POST',
      body: JSON.stringify(parcela),
    });
  },
  
  async listarParcelasDisponibles() {
    return this.request('/parcela/disponibles');
  },
  
  async listarParcelasNoDisponibles() {
    try {
      return await this.request('/parcela/nodisponibles');
    } catch (error) {
      // Si no hay parcelas inactivas, el backend devuelve 404, retornar array vacío
      if (error.message && error.message.includes('404')) {
        return [];
      }
      throw error;
    }
  },
  
  async activarParcela(id) {
    const url = `${API_BASE_URL}/parcela/activar/${id}`;
    console.log('🔵 [API] Activando parcela:', id);
    console.log('🔵 [API] URL completa:', url);
    try {
      const result = await this.request(`/parcela/activar/${id}`, {
        method: 'PUT',
      });
      console.log('✅ [API] Parcela activada exitosamente:', result);
      return result;
    } catch (error) {
      console.error('❌ [API] Error al activar parcela:', error);
      console.error('❌ [API] Error status:', error.status);
      console.error('❌ [API] Error message:', error.message);
      throw error;
    }
  },
  
  async inactivarParcela(id) {
    const url = `${API_BASE_URL}/parcela/inactivar/${id}`;
    console.log('🟡 [API] Inactivando parcela:', id);
    console.log('🟡 [API] URL completa:', url);
    try {
      const result = await this.request(`/parcela/inactivar/${id}`, {
        method: 'PUT',
      });
      console.log('✅ [API] Parcela inactivada exitosamente:', result);
      return result;
    } catch (error) {
      console.error('❌ [API] Error al inactivar parcela:', error);
      console.error('❌ [API] Error status:', error.status);
      console.error('❌ [API] Error message:', error.message);
      throw error;
    }
  },
  
  // ========== SIEMBRAS ==========
  async listarSiembras() {
    return this.request('/siembra/listar');
  },
  
  async registrarSiembra(siembra) {
    return this.request('/siembra/registrar', {
      method: 'POST',
      body: JSON.stringify(siembra),
    });
  },
  
  async listarSiembrasPorParcela(idParcela) {
    try {
      return await this.request(`/siembra/parcela/${idParcela}`);
    } catch (error) {
      // Si es 400, significa que no hay siembras en esa parcela
      if (error.status === 400 || (error.message && (error.message.includes('400') || error.message.includes('no tiene siembras')))) {
        return [];
      }
      throw error;
    }
  },
  
  // ========== COSECHAS ==========
  async listarCosechas() {
    return this.request('/cosecha/listar');
  },
  
  async registrarCosecha(cosecha) {
    return this.request('/cosecha/registrar', {
      method: 'POST',
      body: JSON.stringify(cosecha),
    });
  },
  
  async listarCosechasPorParcela(idParcela) {
    return this.request(`/cosecha/parcela/${idParcela}`);
  },
  
  async reporteRendimientoPorCultivo() {
    return this.request('/cosecha/reporte/rendimiento');
  },
  
  async compararSiembraVsCosecha(idParcela) {
    return this.request(`/cosecha/comparar/${idParcela}`);
  },
  
  // ========== ACTIVIDADES ==========
  async programarActividad(actividad) {
    return this.request('/actividad/programar', {
      method: 'POST',
      body: JSON.stringify(actividad),
    });
  },
  
  async listarActividadesPendientes() {
    try {
      return await this.request('/actividad/pendientes');
    } catch (error) {
      // Si es 404, significa que no hay actividades pendientes
      if (error.status === 404 || (error.message && error.message.includes('404'))) {
        return [];
      }
      throw error;
    }
  },
  
  async listarActividadesHoy() {
    try {
      return await this.request('/actividad/hoy');
    } catch (error) {
      // Si es 404, significa que no hay actividades hoy
      if (error.status === 404 || (error.message && error.message.includes('404'))) {
        return [];
      }
      throw error;
    }
  },
  
  async listarActividadesPorParcela(idParcela) {
    return this.request(`/actividad/parcela/${idParcela}`);
  },
  
  async completarActividad(id) {
    return this.request(`/actividad/completar/${id}`, {
      method: 'PUT',
    });
  },
  
  async iniciarActividad(id) {
    return this.request(`/actividad/iniciar/${id}`, {
      method: 'PUT',
    });
  },
  
  async listarTiposActividades() {
    return this.request('/actividad/tipos');
  },
  
  async reporteActividadesPorEmpleado(idEmpleado) {
    return this.request(`/actividad/reporte/empleado/${idEmpleado}`);
  },
  
  // ========== VENTAS ==========
  async listarVentas() {
    return this.request('/venta/listar');
  },
  
  async registrarVenta(venta) {
    return this.request('/venta/registrar', {
      method: 'POST',
      body: JSON.stringify(venta),
    });
  },
  
  async listarVentasPorComprador(idComprador) {
    return this.request(`/venta/comprador/${idComprador}`);
  },
  
  async reporteVentasPorCultivo() {
    return this.request('/venta/reporte/cultivo');
  },
  
  async consultarStockDisponible() {
    return this.request('/venta/stock');
  },
  
  async registrarComprador(comprador) {
    return this.request('/venta/comprador/registrar', {
      method: 'POST',
      body: JSON.stringify(comprador),
    });
  },
  
  async listarCompradores() {
    try {
      return await this.request('/venta/comprador/listar');
    } catch (error) {
      console.error('Error al listar compradores:', error);
      return [];
    }
  },
  
  // ========== ALERTAS ==========
  async obtenerAlertasActivas() {
    return this.request('/alerta/activas');
  },
  
  async alertasCosechaProxima() {
    return this.request('/alerta/cosecha-proxima');
  },
  
  async alertasActividadesHoy() {
    return this.request('/alerta/actividades-hoy');
  },
  
  async alertasActividadesAtrasadas() {
    return this.request('/alerta/actividades-atrasadas');
  },
  
  async alertasStockBajoSemillas() {
    return this.request('/alerta/stock-bajo-semillas');
  },
  
  async resumenAlertas() {
    return this.request('/alerta/resumen');
  },
  
  async obtenerAlertasPorPrioridad(prioridad) {
    return this.request(`/alerta/prioridad/${prioridad}`);
  },
};

