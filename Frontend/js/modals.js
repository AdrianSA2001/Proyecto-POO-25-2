/**
 * Componentes de Modales y Formularios
 */

// Crear modal genérico
function createModal(title, content, isLarge = false) {
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
}

// Cerrar modal
function closeModal() {
  const modal = document.querySelector('.modal-overlay');
  if (modal) {
    modal.remove();
  }
}

// Modal para registrar Parcela
async function showParcelaFormModal() {
  const content = `
    <form id="parcelaForm" onsubmit="handleParcelaSubmit(event)">
      <div class="form-group">
        <label for="parcela_ubicacion">Ubicación *</label>
        <input type="text" id="parcela_ubicacion" name="ubicacion" required placeholder="Ej: Sector Norte, Lote 5">
      </div>
      
      <div class="form-group">
        <label for="parcela_area">Área (m²) *</label>
        <input type="number" id="parcela_area" name="area" step="0.01" min="0.01" required placeholder="Ej: 100.5">
      </div>
      
      <div class="form-group">
        <label for="parcela_estado">Estado *</label>
        <select id="parcela_estado" name="id_estado_parcela" required>
          <option value="1" selected>Inactiva (Nueva parcela debe estar inactiva)</option>
          <option value="2">Activa</option>
        </select>
        <small style="color: var(--text-secondary); font-size: 0.85rem;">
          Las nuevas parcelas deben registrarse como "Inactiva"
        </small>
      </div>
      
      <div class="form-actions">
        <button type="button" class="btn btn-secondary" onclick="closeModal()">Cancelar</button>
        <button type="submit" class="btn btn-primary">Registrar Parcela</button>
      </div>
    </form>
  `;
  
  createModal('Registrar Nueva Parcela', content);
}

// Handler para submit de Parcela
async function handleParcelaSubmit(event) {
  event.preventDefault();
  const form = event.target;
  const submitBtn = form.querySelector('button[type="submit"]');
  const originalText = submitBtn.textContent;
  
  try {
    submitBtn.disabled = true;
    submitBtn.textContent = 'Registrando...';
    
    const parcela = {
      ubicacion: form.ubicacion.value.trim(),
      area: parseFloat(form.area.value),
      id_estado_parcela: parseInt(form.id_estado_parcela.value)
    };
    
    const resultado = await ApiService.registrarParcela(parcela);
    Toast.success(`Parcela registrada exitosamente (ID: ${resultado.id_parcela}). Ya puedes usarla para sembrar.`);
    closeModal();
    loadParcelas('todas');
  } catch (error) {
    Toast.error(error.message || 'Error al registrar la parcela');
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = originalText;
  }
}

// Modal para registrar Siembra
async function showSiembraFormModal() {
  // Cargar datos necesarios
  let parcelas, cultivos, empleados;
  
  try {
    // Cargar parcelas NO disponibles (Inactivas) porque solo se puede sembrar en parcelas inactivas
    [parcelas, empleados] = await Promise.all([
      ApiService.listarParcelasNoDisponibles().catch(() => []),
      ApiService.listarEmpleadosActivos().catch(() => [])
    ]);
    
    // Si no hay parcelas inactivas, intentar cargar todas y filtrar
    if (parcelas.length === 0) {
      const todasParcelas = await ApiService.listarParcelas().catch(() => []);
      // Filtrar solo las inactivas (estado_parcela = "Inactiva" o id_estado_parcela = 1)
      parcelas = todasParcelas.filter(p => {
        const estado = (p.estado_parcela || '').toLowerCase();
        return estado.includes('inactiva') || p.id_estado_parcela === 1;
      });
    }
    
    // Obtener tipos de cultivo (necesitarías un endpoint o hardcodear)
    cultivos = [
      { id_tipo_cultivo: 1, nombre: 'Maíz' },
      { id_tipo_cultivo: 2, nombre: 'Papa' },
      { id_tipo_cultivo: 3, nombre: 'Tomate' },
      { id_tipo_cultivo: 4, nombre: 'Zanahoria' }
    ];
  } catch (error) {
    Toast.error('Error al cargar datos: ' + error.message);
    return;
  }
  
  const content = `
    <form id="siembraForm" onsubmit="handleSiembraSubmit(event)">
      <div class="form-group">
        <label for="siembra_cultivo">Tipo de Cultivo *</label>
        <select id="siembra_cultivo" name="id_tipo_cultivo" required>
          <option value="">Seleccione un cultivo</option>
          ${cultivos.map(c => `<option value="${c.id_tipo_cultivo}">${c.nombre}</option>`).join('')}
        </select>
      </div>
      
      <div class="form-group">
        <label for="siembra_parcela">Parcela (Solo Inactivas) *</label>
        <select id="siembra_parcela" name="id_parcela" required>
          <option value="">Seleccione una parcela inactiva</option>
          ${parcelas.length > 0 ? parcelas.map(p => {
            const ubicacion = p.ubicacion || `Parcela ${p.id_parcela}`;
            const estado = p.estado_parcela || 'Inactiva';
            return `<option value="${p.id_parcela}">#${p.id_parcela} - ${ubicacion} (${estado})</option>`;
          }).join('') : '<option disabled>No hay parcelas inactivas disponibles</option>'}
        </select>
        ${parcelas.length === 0 ? '<small style="color: var(--error-color);">No hay parcelas inactivas disponibles. Las parcelas deben estar inactivas para poder sembrar.</small>' : '<small style="color: var(--text-secondary);">Solo se pueden sembrar en parcelas inactivas. Después de sembrar, la parcela se activará automáticamente.</small>'}
      </div>
      
      <div class="form-group">
        <label for="siembra_empleado">Empleado Responsable *</label>
        <select id="siembra_empleado" name="id_empleado" required>
          <option value="">Seleccione un empleado</option>
          ${empleados.length > 0 ? empleados.map(e => `<option value="${e.id_empleado}">${e.nombre} ${e.apellido}</option>`).join('') : '<option disabled>No hay empleados activos</option>'}
        </select>
      </div>
      
      <div class="form-group">
        <label for="siembra_cantidad">Cantidad Sembrada (kg) *</label>
        <input type="number" id="siembra_cantidad" name="cantidad_sembrada" step="0.01" min="0.01" required placeholder="Ej: 25.5">
        <small style="color: var(--text-secondary);">La cosecha se programará automáticamente según el tipo de cultivo</small>
      </div>
      
      <div class="form-actions">
        <button type="button" class="btn btn-secondary" onclick="closeModal()">Cancelar</button>
        <button type="submit" class="btn btn-primary" ${parcelas.length === 0 ? 'disabled' : ''}>Registrar Siembra</button>
      </div>
    </form>
  `;
  
  createModal('Registrar Nueva Siembra', content);
}

// Handler para submit de Siembra
async function handleSiembraSubmit(event) {
  event.preventDefault();
  const form = event.target;
  const submitBtn = form.querySelector('button[type="submit"]');
  const originalText = submitBtn.textContent;
  
  try {
    submitBtn.disabled = true;
    submitBtn.textContent = 'Registrando...';
    
    // Validar campos
    const idTipoCultivo = parseInt(form.id_tipo_cultivo.value);
    const idParcela = parseInt(form.id_parcela.value);
    const idEmpleado = parseInt(form.id_empleado.value);
    const cantidad = parseFloat(form.cantidad_sembrada.value);
    
    if (!idTipoCultivo || !idParcela || !idEmpleado || !cantidad || cantidad <= 0) {
      throw new Error('Por favor, complete todos los campos correctamente');
    }
    
    const siembra = {
      id_tipo_cultivo: idTipoCultivo,
      id_parcela: idParcela,
      id_empleado: idEmpleado,
      cantidad_sembrada: cantidad
    };
    
    console.log('Enviando siembra:', siembra); // Debug
    
    const resultado = await ApiService.registrarSiembra(siembra);
    
    // Mostrar mensaje con información adicional
    const fechaCosecha = resultado.fecha_estimada_cosecha ? 
      ` (Cosecha estimada: ${formatDate(resultado.fecha_estimada_cosecha)})` : '';
    
    Toast.success(`Siembra registrada exitosamente (ID: ${resultado.id_siembra})${fechaCosecha}. La actividad de cosecha ha sido programada automáticamente.`);
    
    closeModal();
    
    // Recargar datos
    loadSiembras();
    loadParcelas('todas'); // Recargar parcelas para ver el cambio de estado
    loadDashboardData();
  } catch (error) {
    console.error('Error al registrar siembra:', error); // Debug
    const errorMessage = error.message || 'Error al registrar la siembra';
    Toast.error(errorMessage);
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = originalText;
  }
}

// Modal para registrar Cosecha
async function showCosechaFormModal() {
  let parcelasActivas, empleados, siembras, cosechas;
  
  try {
    // Obtener empleado actual de la sesión
    const currentUser = AuthService.getCurrentUser();
    const empleadoActual = currentUser ? {
      id_empleado: currentUser.id_empleado,
      nombre: currentUser.nombre,
      apellido: currentUser.apellido
    } : null;
    
    // Empleado actual ya ha sido obtenido antes, no es necesario redeclararlo
    
    // Cargar datos necesarios
    [parcelasActivas, empleados, siembras, cosechas] = await Promise.all([
      ApiService.listarParcelasDisponibles().catch(() => []),
      ApiService.listarEmpleadosActivos().catch(() => []),
      ApiService.listarSiembras().catch(() => []),
      ApiService.listarCosechas().catch(() => [])
    ]);
    
    // Filtrar parcelas activas que tienen siembras sin cosechar
    const opcionesParcelas = [];
    
    for (const parcela of parcelasActivas) {
      // Buscar siembras en esta parcela (comparar por ID o por ubicación)
      const siembrasParcela = siembras.filter(s => {
        // Intentar diferentes formas de comparar
        if (s.id_parcela && parcela.id_parcela) {
          return s.id_parcela === parcela.id_parcela;
        }
        if (s.parcela && parcela.ubicacion) {
          return s.parcela.toString().includes(parcela.ubicacion) || 
                 parcela.ubicacion.includes(s.parcela.toString());
        }
        return false;
      });
      
      if (siembrasParcela.length === 0) continue;
      
      // Para cada siembra, verificar si ya tiene cosecha
      const cultivosDisponibles = [];
      for (const siembra of siembrasParcela) {
        // Verificar si ya hay cosecha para esta siembra
        const tieneCosecha = cosechas.some(c => {
          // Comparar parcela
          const mismaParcela = (c.id_parcela && parcela.id_parcela && c.id_parcela === parcela.id_parcela) ||
                               (c.parcela && parcela.ubicacion && 
                                (c.parcela.toString().includes(parcela.ubicacion) || 
                                 parcela.ubicacion.includes(c.parcela.toString())));
          
          // Comparar cultivo
          const mismoCultivo = (c.id_tipo_cultivo && siembra.id_tipo_cultivo && 
                                c.id_tipo_cultivo === siembra.id_tipo_cultivo) ||
                               (c.tipo_cultivo && siembra.tipo_cultivo && 
                                c.tipo_cultivo === siembra.tipo_cultivo);
          
          return mismaParcela && mismoCultivo;
        });
        
        if (!tieneCosecha) {
          // Agregar cultivo disponible
          const nombreCultivo = siembra.tipo_cultivo || siembra.cultivo || 'Cultivo';
          const idCultivo = siembra.id_tipo_cultivo || 
                           (nombreCultivo === 'Maíz' ? 1 : 
                            nombreCultivo === 'Papa' ? 2 :
                            nombreCultivo === 'Tomate' ? 3 :
                            nombreCultivo === 'Zanahoria' ? 4 : 1);
          
          if (!cultivosDisponibles.find(c => c.id_tipo_cultivo === idCultivo)) {
            cultivosDisponibles.push({
              id_tipo_cultivo: idCultivo,
              nombre: nombreCultivo
            });
          }
        }
      }
      
      if (cultivosDisponibles.length > 0) {
        opcionesParcelas.push({
          parcela: parcela,
          cultivos: cultivosDisponibles
        });
      }
    }
    
    const content = `
      <form id="cosechaForm" onsubmit="handleCosechaSubmit(event)">
        <div class="form-group">
          <label for="cosecha_parcela">Parcela (Solo Activas con Cultivos Listos) *</label>
          <select id="cosecha_parcela" name="id_parcela" required onchange="actualizarCultivosCosecha()">
            <option value="">Seleccione una parcela</option>
            ${opcionesParcelas.map(op => {
              const ubicacion = op.parcela.ubicacion || `Parcela ${op.parcela.id_parcela}`;
              const cultivosStr = op.cultivos.map(c => c.nombre).join(', ');
              return `<option value="${op.parcela.id_parcela}" data-cultivos='${JSON.stringify(op.cultivos)}'>#${op.parcela.id_parcela} - ${ubicacion} (${cultivosStr})</option>`;
            }).join('')}
          </select>
          ${opcionesParcelas.length === 0 ? '<small style="color: var(--error-color);">No hay parcelas activas con cultivos listos para cosechar. Una parcela solo puede tener un cultivo activo a la vez.</small>' : '<small style="color: var(--text-secondary);">Solo se muestran parcelas activas con siembras sin cosechar</small>'}
        </div>
        
        <div class="form-group">
          <label for="cosecha_cultivo">Tipo de Cultivo *</label>
          <select id="cosecha_cultivo" name="id_tipo_cultivo" required disabled>
            <option value="">Primero seleccione una parcela</option>
          </select>
        </div>
        
        <div class="form-group">
          <label for="cosecha_empleado">Empleado Responsable *</label>
          <select id="cosecha_empleado" name="id_empleado" required ${empleadoActual ? 'disabled' : ''}>
            <option value="">Seleccione un empleado</option>
            ${empleados.map(e => {
              const isSelected = empleadoActual && e.id_empleado === empleadoActual.id_empleado;
              return `<option value="${e.id_empleado}" ${isSelected ? 'selected' : ''}>${e.nombre} ${e.apellido}</option>`;
            }).join('')}
          </select>
          ${empleadoActual ? `
            <input type="hidden" name="id_empleado" value="${empleadoActual.id_empleado}">
            <small style="color: var(--text-secondary);">Empleado: ${empleadoActual.nombre} ${empleadoActual.apellido} (sesión actual)</small>
          ` : '<small style="color: var(--text-secondary);">Seleccione el empleado responsable de la cosecha</small>'}
        </div>
        
        <div class="form-group">
          <label for="cosecha_cantidad">Cantidad Cosechada (kg) *</label>
          <input type="number" id="cosecha_cantidad" name="cantidad_cosechada" step="0.01" min="0.01" required placeholder="Ej: 150.5">
        </div>
        
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" onclick="closeModal()">Cancelar</button>
          <button type="submit" class="btn btn-primary" ${opcionesParcelas.length === 0 ? 'disabled' : ''}>Registrar Cosecha</button>
        </div>
      </form>
    `;
    
    createModal('Registrar Nueva Cosecha', content);
    
    // Agregar función para actualizar cultivos cuando se selecciona parcela
    window.actualizarCultivosCosecha = function() {
      const selectParcela = document.getElementById('cosecha_parcela');
      const selectCultivo = document.getElementById('cosecha_cultivo');
      const opcionSeleccionada = selectParcela.options[selectParcela.selectedIndex];
      
      if (!opcionSeleccionada || !opcionSeleccionada.value) {
        selectCultivo.innerHTML = '<option value="">Primero seleccione una parcela</option>';
        selectCultivo.disabled = true;
        return;
      }
      
      try {
        const cultivos = JSON.parse(opcionSeleccionada.getAttribute('data-cultivos') || '[]');
        
        if (cultivos.length === 0) {
          selectCultivo.innerHTML = '<option value="">No hay cultivos disponibles</option>';
          selectCultivo.disabled = true;
          return;
        }
        
        selectCultivo.innerHTML = '<option value="">Seleccione un cultivo</option>' +
          cultivos.map(c => `<option value="${c.id_tipo_cultivo}">${c.nombre}</option>`).join('');
        selectCultivo.disabled = false;
      } catch (e) {
        console.error('Error al parsear cultivos:', e);
        selectCultivo.innerHTML = '<option value="">Error al cargar cultivos</option>';
        selectCultivo.disabled = true;
      }
    };
  } catch (error) {
    Toast.error('Error al cargar datos: ' + error.message);
    return;
  }
}

// Modal para programar Actividad
async function showActividadFormModal() {
  let tiposActividades, parcelas, empleados;
  
  try {
    [tiposActividades, parcelas, empleados] = await Promise.all([
      ApiService.listarTiposActividades().catch(() => []),
      ApiService.listarParcelas().catch(() => []),
      ApiService.listarEmpleadosActivos().catch(() => [])
    ]);
    
    // Filtrar actividades: excluir Siembra y Cosecha (se programan automáticamente)
    const actividadesPermitidas = tiposActividades.filter(t => {
      const nombre = (t.nombre || t.tipo_actividad || '').toLowerCase();
      return !nombre.includes('siembra') && !nombre.includes('cosecha') && 
             t.id_actividad !== 1 && t.id_actividad !== 3; // Excluir IDs comunes de siembra (1) y cosecha (3)
    });
    
    const content = `
      <form id="actividadForm" onsubmit="handleActividadSubmit(event)">
        <div class="form-group">
          <label for="actividad_tipo">Tipo de Actividad *</label>
          <select id="actividad_tipo" name="id_actividad" required>
            <option value="">Seleccione un tipo de actividad</option>
            ${actividadesPermitidas.map(t => `<option value="${t.id_actividad}">${t.nombre || t.tipo_actividad || 'Actividad'}</option>`).join('')}
          </select>
          <small style="color: var(--text-secondary);">Nota: Siembra y Cosecha se programan automáticamente</small>
        </div>
        
        <div class="form-group">
          <label for="actividad_parcela">Parcela *</label>
          <select id="actividad_parcela" name="id_parcela" required>
            <option value="">Seleccione una parcela</option>
            ${parcelas.map(p => {
              const ubicacion = p.ubicacion || `Parcela ${p.id_parcela}`;
              const estado = p.id_estado_parcela === 2 ? 'Activa' : 'Inactiva';
              return `<option value="${p.id_parcela}">#${p.id_parcela} - ${ubicacion} (${estado})</option>`;
            }).join('')}
          </select>
        </div>
        
        <div class="form-group">
          <label for="actividad_empleado">Empleado Responsable *</label>
          <select id="actividad_empleado" name="id_empleado" required>
            <option value="">Seleccione un empleado</option>
            ${empleados.map(e => `<option value="${e.id_empleado}">${e.nombre} ${e.apellido}</option>`).join('')}
          </select>
        </div>
        
        <div class="form-group">
          <label for="actividad_fecha">Fecha Programada *</label>
          <input type="date" id="actividad_fecha" name="fecha_programada" required>
        </div>
        
        <div class="form-group">
          <label for="actividad_estado">Estado Inicial *</label>
          <select id="actividad_estado" name="id_estado_actividad" required disabled>
            <option value="1" selected>Pendiente</option>
          </select>
          <small style="color: var(--text-secondary);">Las actividades nuevas siempre se crean como "Pendiente"</small>
        </div>
        
        <div class="form-group">
          <label for="actividad_observaciones">Observaciones</label>
          <textarea id="actividad_observaciones" name="observaciones" rows="3" placeholder="Notas adicionales sobre la actividad (opcional)"></textarea>
        </div>
        
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" onclick="closeModal()">Cancelar</button>
          <button type="submit" class="btn btn-primary">Programar Actividad</button>
        </div>
      </form>
    `;
    
    createModal('Programar Nueva Actividad', content);
    
    // Establecer fecha mínima como hoy
    const fechaInput = document.getElementById('actividad_fecha');
    if (fechaInput) {
      const hoy = new Date().toISOString().split('T')[0];
      fechaInput.setAttribute('min', hoy);
      fechaInput.value = hoy;
    }
  } catch (error) {
    Toast.error('Error al cargar datos: ' + error.message);
    return;
  }
}

// Handler para submit de Actividad
async function handleActividadSubmit(event) {
  event.preventDefault();
  const form = event.target;
  const submitBtn = form.querySelector('button[type="submit"]');
  const originalText = submitBtn.textContent;
  
  try {
    submitBtn.disabled = true;
    submitBtn.textContent = 'Programando...';
    
    const formData = new FormData(form);
    
    // Validar que todos los campos requeridos estén presentes
    const idActividad = parseInt(formData.get('id_actividad'));
    const idParcela = parseInt(formData.get('id_parcela'));
    const idEmpleado = parseInt(formData.get('id_empleado'));
    const fechaProgramada = formData.get('fecha_programada');
    const observaciones = formData.get('observaciones') || null;
    
    // Validaciones básicas
    if (!idActividad || isNaN(idActividad)) {
      throw new Error('Por favor, seleccione un tipo de actividad');
    }
    if (!idParcela || isNaN(idParcela)) {
      throw new Error('Por favor, seleccione una parcela');
    }
    if (!idEmpleado || isNaN(idEmpleado)) {
      throw new Error('Por favor, seleccione un empleado');
    }
    if (!fechaProgramada) {
      throw new Error('Por favor, seleccione una fecha programada');
    }
    
    // El estado inicial DEBE ser 1 (Pendiente) según el backend
    const actividad = {
      id_actividad: idActividad,
      id_parcela: idParcela,
      id_empleado: idEmpleado,
      fecha_programada: fechaProgramada, // Formato: yyyy-MM-dd
      id_estado_actividad: 1, // SIEMPRE debe ser 1 (Pendiente) para nuevas actividades
      observaciones: observaciones
    };
    
    console.log('📤 [Actividad] Enviando datos:', actividad);
    
    const resultado = await ApiService.programarActividad(actividad);
    
    console.log('✅ [Actividad] Respuesta del servidor:', resultado);
    
    Toast.success(`Actividad programada exitosamente (ID: ${resultado.id_actividad_programada})`);
    
    closeModal();
    
    // Recargar datos
    loadActividades('pendientes');
    loadDashboardData();
  } catch (error) {
    console.error('❌ [Actividad] Error al programar actividad:', error);
    console.error('❌ [Actividad] Error completo:', {
      message: error.message,
      status: error.status,
      data: error.data
    });
    
    // Extraer mensaje de error del backend si está disponible
    let errorMessage = 'Error al programar la actividad';
    if (error.data && error.data.message) {
      errorMessage = error.data.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    Toast.error(errorMessage);
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = originalText;
  }
}

// Modal para registrar Venta
async function showVentaFormModal() {
  let compradores, cultivos, stock;
  
  try {
    [compradores, stock] = await Promise.all([
      ApiService.listarCompradores().catch(() => []),
      ApiService.consultarStockDisponible().catch(() => [])
    ]);
    
    console.log('📦 [Venta] Stock disponible recibido:', stock);
    
    // Obtener tipos de cultivo desde el stock disponible
    const cultivosDisponibles = stock.map(s => {
      console.log('📦 [Venta] Procesando stock item:', s);
      return {
        id_tipo_cultivo: s.id_tipo_cultivo,
        nombre: s.tipo_cultivo || s.nombre || 'Cultivo',
        stock: parseFloat(s.stock_disponible || s.cantidad_disponible || 0)
      };
    }).filter(c => c.id_tipo_cultivo && c.stock > 0);
    
    console.log('📦 [Venta] Cultivos disponibles procesados:', cultivosDisponibles);
    
    const content = `
      <form id="ventaForm" onsubmit="handleVentaSubmit(event)">
        <div class="form-group">
          <label for="venta_comprador">Comprador *</label>
          <select id="venta_comprador" name="id_comprador" required>
            <option value="">Seleccione un comprador</option>
            ${compradores.map(c => `<option value="${c.id_comprador}">${c.nombre || c.razon_social || 'Comprador'} ${c.apellido || ''}</option>`).join('')}
          </select>
          <small style="color: var(--text-secondary);">Si no encuentra el comprador, puede registrarlo primero</small>
        </div>
        
        <div class="form-group">
          <label for="venta_cultivo">Tipo de Cultivo *</label>
          <select id="venta_cultivo" name="id_tipo_cultivo" required onchange="actualizarStockVenta(this.value)">
            <option value="">Seleccione un cultivo</option>
            ${cultivosDisponibles.map(c => `<option value="${c.id_tipo_cultivo}" data-stock="${c.stock}">${c.nombre} (Stock: ${formatNumber(c.stock, 2)} kg)</option>`).join('')}
          </select>
          <small id="stockInfo" style="color: var(--text-secondary);">Seleccione un cultivo para ver el stock disponible</small>
        </div>
        
        <div class="form-group">
          <label for="venta_cantidad">Cantidad a Vender (kg) *</label>
          <input type="number" id="venta_cantidad" name="cantidad_vendida" step="0.01" min="0.01" required placeholder="Ej: 100.5" oninput="calcularTotalVenta()">
          <small id="cantidadInfo" style="color: var(--text-secondary);"></small>
        </div>
        
        <div class="form-group">
          <label for="venta_precio">Precio Unitario (S/) *</label>
          <input type="number" id="venta_precio" name="precio_unitario" step="0.01" min="0.01" required placeholder="Ej: 5.50" oninput="calcularTotalVenta()">
        </div>
        
        <div class="form-group">
          <label for="venta_total">Total (S/)</label>
          <input type="number" id="venta_total" name="precio_total" step="0.01" readonly style="background: var(--background); font-weight: bold;">
          <small style="color: var(--text-secondary);">Se calcula automáticamente</small>
        </div>
        
        <div class="form-group">
          <label for="venta_fecha">Fecha de Venta *</label>
          <input type="date" id="venta_fecha" name="fecha_venta" required>
        </div>
        
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" onclick="closeModal()">Cancelar</button>
          <button type="submit" class="btn btn-primary">Registrar Venta</button>
        </div>
      </form>
    `;
    
    createModal('Registrar Nueva Venta', content);
    
    // Establecer fecha como hoy
    const fechaInput = document.getElementById('venta_fecha');
    if (fechaInput) {
      const hoy = new Date().toISOString().split('T')[0];
      fechaInput.value = hoy;
    }
    
    // Agregar funciones globales para el cálculo
    window.actualizarStockVenta = function(idCultivo) {
      const select = document.getElementById('venta_cultivo');
      const opcion = select.options[select.selectedIndex];
      const stockInfo = document.getElementById('stockInfo');
      const cantidadInput = document.getElementById('venta_cantidad');
      const cantidadInfo = document.getElementById('cantidadInfo');
      
      if (opcion && opcion.value) {
        const stock = parseFloat(opcion.getAttribute('data-stock') || 0);
        stockInfo.textContent = `Stock disponible: ${formatNumber(stock, 2)} kg`;
        stockInfo.style.color = stock > 0 ? 'var(--success-color)' : 'var(--error-color)';
        
        if (cantidadInput) {
          cantidadInput.setAttribute('max', stock);
          cantidadInfo.textContent = `Máximo: ${formatNumber(stock, 2)} kg`;
        }
      } else {
        stockInfo.textContent = 'Seleccione un cultivo para ver el stock disponible';
        stockInfo.style.color = 'var(--text-secondary)';
      }
    };
    
    window.calcularTotalVenta = function() {
      const cantidad = parseFloat(document.getElementById('venta_cantidad')?.value) || 0;
      const precio = parseFloat(document.getElementById('venta_precio')?.value) || 0;
      const total = cantidad * precio;
      const totalInput = document.getElementById('venta_total');
      if (totalInput) {
        totalInput.value = total.toFixed(2);
      }
    };
    
    // Agregar listeners después de crear el modal
    setTimeout(() => {
      const cantidadInput = document.getElementById('venta_cantidad');
      const precioInput = document.getElementById('venta_precio');
      const cultivoSelect = document.getElementById('venta_cultivo');
      
      if (cantidadInput) cantidadInput.addEventListener('input', window.calcularTotalVenta);
      if (precioInput) precioInput.addEventListener('input', window.calcularTotalVenta);
      if (cultivoSelect) cultivoSelect.addEventListener('change', function() {
        window.actualizarStockVenta(this.value);
      });
    }, 100);
  } catch (error) {
    Toast.error('Error al cargar datos: ' + error.message);
    return;
  }
}

// Handler para submit de Venta
async function handleVentaSubmit(event) {
  event.preventDefault();
  const form = event.target;
  const submitBtn = form.querySelector('button[type="submit"]');
  const originalText = submitBtn.textContent;
  
  try {
    submitBtn.disabled = true;
    submitBtn.textContent = 'Registrando...';
    
    const formData = new FormData(form);
    
    // Obtener y validar valores
    const idComprador = parseInt(formData.get('id_comprador'));
    const idTipoCultivo = parseInt(formData.get('id_tipo_cultivo'));
    const cantidad = parseFloat(formData.get('cantidad_vendida'));
    const precioUnitario = parseFloat(formData.get('precio_unitario'));
    
    // Validaciones básicas
    if (!idComprador || isNaN(idComprador)) {
      throw new Error('Por favor, seleccione un comprador');
    }
    if (!idTipoCultivo || isNaN(idTipoCultivo)) {
      throw new Error('Por favor, seleccione un tipo de cultivo');
    }
    if (!cantidad || isNaN(cantidad) || cantidad <= 0) {
      throw new Error('La cantidad vendida debe ser mayor a 0');
    }
    if (!precioUnitario || isNaN(precioUnitario) || precioUnitario <= 0) {
      throw new Error('El precio unitario debe ser mayor a 0');
    }
    
    // El backend calcula precio_total y usa la fecha actual internamente
    // Solo enviamos los campos requeridos
    const venta = {
      id_comprador: idComprador,
      id_tipo_cultivo: idTipoCultivo,
      cantidad_vendida: cantidad,
      precio_unitario: precioUnitario
    };
    
    console.log('📤 [Venta] Enviando datos:', venta);
    
    const resultado = await ApiService.registrarVenta(venta);
    
    console.log('✅ [Venta] Respuesta del servidor:', resultado);
    
    const total = cantidad * precioUnitario;
    Toast.success(`Venta registrada exitosamente (ID: ${resultado.id_venta}). Total: ${formatCurrency(total)}`);
    
    closeModal();
    
    // Recargar datos
    loadVentas();
    loadDashboardData();
  } catch (error) {
    console.error('❌ [Venta] Error al registrar venta:', error);
    console.error('❌ [Venta] Error completo:', {
      message: error.message,
      status: error.status,
      data: error.data
    });
    
    // Extraer mensaje de error del backend si está disponible
    let errorMessage = 'Error al registrar la venta';
    if (error.data && error.data.message) {
      errorMessage = error.data.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    Toast.error(errorMessage);
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = originalText;
  }
}

// Handler para submit de Cosecha
async function handleCosechaSubmit(event) {
  event.preventDefault();
  const form = event.target;
  const submitBtn = form.querySelector('button[type="submit"]');
  const originalText = submitBtn.textContent;
  
  try {
    submitBtn.disabled = true;
    submitBtn.textContent = 'Registrando...';
    
    // Obtener empleado del campo hidden (si existe) o del select
    const empleadoHidden = form.querySelector('input[name="id_empleado"][type="hidden"]');
    const empleadoSelect = form.id_empleado;
    const idEmpleado = empleadoHidden ? parseInt(empleadoHidden.value) : parseInt(empleadoSelect.value);
    
    const cosecha = {
      id_tipo_cultivo: parseInt(form.id_tipo_cultivo.value),
      id_parcela: parseInt(form.id_parcela.value),
      id_empleado: idEmpleado,
      cantidad_cosechada: parseFloat(form.cantidad_cosechada.value)
    };
    
    const resultado = await ApiService.registrarCosecha(cosecha);
    Toast.success(`Cosecha registrada exitosamente (ID: ${resultado.id_cosecha})`);
    closeModal();
    loadCosechas();
    loadDashboardData();
  } catch (error) {
    Toast.error(error.message || 'Error al registrar la cosecha');
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = originalText;
  }
}

// Modal para registrar Empleado
async function showEmpleadoFormModal() {
  const content = `
    <form id="empleadoForm" onsubmit="handleEmpleadoSubmit(event)">
      <div class="form-row" style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
        <div class="form-group">
          <label for="empleado_nombre">Nombre *</label>
          <input type="text" id="empleado_nombre" name="nombre" required placeholder="Ej: Juan">
        </div>
        
        <div class="form-group">
          <label for="empleado_apellido">Apellido *</label>
          <input type="text" id="empleado_apellido" name="apellido" required placeholder="Ej: Pérez">
        </div>
      </div>
      
      <div class="form-group">
        <label for="empleado_dni">DNI *</label>
        <input type="text" id="empleado_dni" name="dni" required pattern="[0-9]{8}" maxlength="8" placeholder="Ej: 12345678">
        <small style="color: var(--text-secondary);">8 dígitos numéricos</small>
      </div>
      
      <div class="form-row" style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
        <div class="form-group">
          <label for="empleado_telefono">Teléfono *</label>
          <input type="tel" id="empleado_telefono" name="telefono" required pattern="[0-9]{9}" maxlength="9" placeholder="Ej: 987654321">
          <small style="color: var(--text-secondary);">9 dígitos numéricos</small>
        </div>
        
        <div class="form-group">
          <label for="empleado_email">Email *</label>
          <input type="email" id="empleado_email" name="email" required placeholder="Ej: juan@agrosan.com">
        </div>
      </div>
      
      <div class="form-group">
        <label for="empleado_contrasena">Contraseña *</label>
        <input type="password" id="empleado_contrasena" name="contrasena" required minlength="4" placeholder="Mínimo 4 caracteres">
        <small style="color: var(--text-secondary);">Esta será la contraseña para iniciar sesión</small>
      </div>
      
      <div class="form-actions">
        <button type="button" class="btn btn-secondary" onclick="closeModal()">Cancelar</button>
        <button type="submit" class="btn btn-primary">Registrar Empleado</button>
      </div>
    </form>
  `;
  
  createModal('Registrar Nuevo Empleado', content);
}

// Handler para submit de Empleado
async function handleEmpleadoSubmit(event) {
  event.preventDefault();
  const form = event.target;
  const submitBtn = form.querySelector('button[type="submit"]');
  const originalText = submitBtn.textContent;
  
  try {
    submitBtn.disabled = true;
    submitBtn.textContent = 'Registrando...';
    
    const empleado = {
      nombre: form.nombre.value.trim(),
      apellido: form.apellido.value.trim(),
      dni: form.dni.value.trim(),
      telefono: form.telefono.value.trim(),
      email: form.email.value.trim(),
      contrasena: form.contrasena.value,
      id_estado_empleado: "1" // Nuevo empleado siempre inicia como Activo
    };
    
    // Validaciones
    if (!/^\d{8}$/.test(empleado.dni)) {
      throw new Error('El DNI debe tener exactamente 8 dígitos');
    }
    if (!/^\d{9}$/.test(empleado.telefono)) {
      throw new Error('El teléfono debe tener exactamente 9 dígitos');
    }
    
    console.log('📤 [Empleado] Enviando datos:', empleado);
    
    const resultado = await ApiService.registrarEmpleado(empleado);
    
    console.log('✅ [Empleado] Respuesta del servidor:', resultado);
    
    Toast.success(`Empleado registrado exitosamente (ID: ${resultado.id_empleado})`);
    closeModal();
    loadEmpleados('todos');
  } catch (error) {
    console.error('❌ [Empleado] Error:', error);
    let errorMessage = 'Error al registrar el empleado';
    if (error.data && error.data.message) {
      errorMessage = error.data.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    Toast.error(errorMessage);
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = originalText;
  }
}

// Modal para ver detalles de Empleado
async function showEmpleadoDetailsModal(id) {
  try {
    const empleado = await ApiService.obtenerEmpleado(id);
    
    // Mapear id_estado_empleado a texto
    const estadoMap = {
      '1': 'Activo',
      '2': 'Vacaciones',
      '3': 'Retirado',
      '4': 'Despedido'
    };
    const estado = empleado.estado || estadoMap[empleado.id_estado_empleado] || 'Activo';
    const badgeClass = estado === 'Activo' ? 'success' : 
                       estado === 'Vacaciones' ? 'warning' : 
                       estado === 'Retirado' || estado === 'Despedido' ? 'error' : 'secondary';
    
    const content = `
      <div class="empleado-details">
        <div class="detail-section">
          <h3>Información Personal</h3>
          <div class="detail-grid" style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
            <div class="detail-item">
              <label>ID</label>
              <p>${empleado.id_empleado}</p>
            </div>
            <div class="detail-item">
              <label>Estado</label>
              <p><span class="badge badge-${badgeClass}">${estado}</span></p>
            </div>
            <div class="detail-item">
              <label>Nombre</label>
              <p>${empleado.nombre || '-'}</p>
            </div>
            <div class="detail-item">
              <label>Apellido</label>
              <p>${empleado.apellido || '-'}</p>
            </div>
            <div class="detail-item">
              <label>DNI</label>
              <p>${empleado.dni || '-'}</p>
            </div>
            <div class="detail-item">
              <label>Teléfono</label>
              <p>${empleado.telefono || '-'}</p>
            </div>
            <div class="detail-item" style="grid-column: span 2;">
              <label>Email</label>
              <p>${empleado.email || '-'}</p>
            </div>
          </div>
        </div>
        
        <div class="form-actions" style="margin-top: 1.5rem;">
          <button type="button" class="btn btn-secondary" onclick="closeModal()">Cerrar</button>
        </div>
      </div>
    `;
    
    createModal(`Empleado: ${empleado.nombre} ${empleado.apellido}`, content);
  } catch (error) {
    console.error('❌ Error al cargar empleado:', error);
    let errorMessage = 'Error al cargar datos del empleado';
    if (error.data && error.data.message) {
      errorMessage = error.data.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    Toast.error(errorMessage);
  }
}

// Hacer funciones globales
window.showParcelaFormModal = showParcelaFormModal;
window.showSiembraFormModal = showSiembraFormModal;
window.showCosechaFormModal = showCosechaFormModal;
window.showActividadFormModal = showActividadFormModal;
window.showVentaFormModal = showVentaFormModal;
window.showEmpleadoFormModal = showEmpleadoFormModal;
window.showEmpleadoDetailsModal = showEmpleadoDetailsModal;
window.handleParcelaSubmit = handleParcelaSubmit;
window.handleSiembraSubmit = handleSiembraSubmit;
window.handleCosechaSubmit = handleCosechaSubmit;
window.handleActividadSubmit = handleActividadSubmit;
window.handleVentaSubmit = handleVentaSubmit;
window.handleEmpleadoSubmit = handleEmpleadoSubmit;
window.closeModal = closeModal;
window.createModal = createModal;

