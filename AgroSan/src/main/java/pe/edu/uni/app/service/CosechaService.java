package pe.edu.uni.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.uni.app.dto.CosechaDto;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class CosechaService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ParcelaService parcelaService;

	@Autowired
	private EmpleadoService empleadoService;

	/**
	 * Registrar cosecha (RF4)
	 * Estilo Coronel: Variables -> Validaciones -> Proceso
	 */
	@Transactional(
			propagation = Propagation.REQUIRES_NEW,
			rollbackFor = Exception.class
	)
	public CosechaDto registrarCosecha(CosechaDto bean) {
		// ******************************
		// Variables
		// ******************************
		String sql;
		KeyHolder keyHolder = new GeneratedKeyHolder();
		LocalDate fechaCosecha = LocalDate.now();

		// ******************************
		// Validaciones
		// ******************************
		// Validar que la parcela existe
		parcelaService.validarParcelaExiste(bean.getId_parcela());

		// Validar que el empleado existe y está activo
		empleadoService.validarEmpleadoActivo(bean.getId_empleado());

		// Validar que el tipo de cultivo existe
		this.validarTipoCultivoExiste(bean.getId_tipo_cultivo());

		// Validar cantidad cosechada
		this.validarCantidadCosechada(bean.getCantidad_cosechada());

		// Validar que hay una siembra previa en esa parcela y cultivo
		this.validarExisteSiembra(bean.getId_parcela(), bean.getId_tipo_cultivo());

		// ******************************
		// Proceso
		// ******************************
		// Insertar en HISTORIAL_COSECHA
		sql = """
				INSERT INTO HISTORIAL_COSECHA 
				(id_tipo_cultivo, id_parcela, id_empleado, fecha_cosecha, cantidad_cosechada) 
				VALUES (?, ?, ?, ?, ?)
				""";

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_cosecha"});
			ps.setInt(1, bean.getId_tipo_cultivo());
			ps.setInt(2, bean.getId_parcela());
			ps.setInt(3, bean.getId_empleado());
			ps.setDate(4, Date.valueOf(fechaCosecha));
			ps.setDouble(5, bean.getCantidad_cosechada());
			return ps;
		}, keyHolder);

		// Obtener ID generado
		int idCosecha = keyHolder.getKey().intValue();
		bean.setId_cosecha(idCosecha);
		bean.setFecha_cosecha(fechaCosecha.toString());

		// Obtener cantidad estimada (de la siembra más reciente)
		double cantidadEstimada = this.obtenerCantidadEstimada(bean.getId_parcela(), bean.getId_tipo_cultivo());
		bean.setCantidad_estimada(cantidadEstimada);

		// Calcular rendimiento (porcentaje real vs estimado)
		if (cantidadEstimada > 0) {
			double rendimiento = (bean.getCantidad_cosechada() / cantidadEstimada) * 100;
			bean.setRendimiento(rendimiento);
		} else {
			bean.setRendimiento(100.0); // Si no hay estimado, consideramos 100%
		}

		// Actualizar stock de cosecha (sumar cantidad cosechada)
		this.actualizarStockCosecha(bean.getId_tipo_cultivo(), bean.getCantidad_cosechada());

		return bean;
	}

	/**
	 * Listar todas las cosechas
	 */
	public List<Map<String, Object>> listarCosechas() {
		String sql = """
				SELECT 
					hc.id_cosecha, hc.fecha_cosecha, hc.cantidad_cosechada,
					tc.nombre tipo_cultivo, tc.tipo categoria,
					p.ubicacion parcela, p.area area_parcela,
					CONCAT(e.nombre, ' ', e.apellido) empleado
				FROM HISTORIAL_COSECHA hc
				JOIN TIPO_CULTIVO tc ON hc.id_tipo_cultivo = tc.id_tipo_cultivo
				JOIN PARCELA p ON hc.id_parcela = p.id_parcela
				JOIN EMPLEADO e ON hc.id_empleado = e.id_empleado
				ORDER BY hc.fecha_cosecha DESC
				""";

		return jdbcTemplate.queryForList(sql);
	}

	/**
	 * Consultar cosechas por parcela
	 */
	public List<Map<String, Object>> listarCosechasPorParcela(int idParcela) {
		parcelaService.validarParcelaExiste(idParcela);

		String sql = """
				SELECT 
					hc.id_cosecha, hc.fecha_cosecha, hc.cantidad_cosechada,
					tc.nombre tipo_cultivo, tc.tipo categoria,
					CONCAT(e.nombre, ' ', e.apellido) empleado
				FROM HISTORIAL_COSECHA hc
				JOIN TIPO_CULTIVO tc ON hc.id_tipo_cultivo = tc.id_tipo_cultivo
				JOIN EMPLEADO e ON hc.id_empleado = e.id_empleado
				WHERE hc.id_parcela = ?
				ORDER BY hc.fecha_cosecha DESC
				""";

		return jdbcTemplate.queryForList(sql, idParcela);
	}

	/**
	 * Consultar rendimiento por tipo de cultivo
	 */
	public List<Map<String, Object>> reporteRendimientoPorCultivo() {
		String sql = """
				SELECT 
					tc.nombre tipo_cultivo,
					COUNT(hc.id_cosecha) total_cosechas,
					SUM(hc.cantidad_cosechada) total_cosechado,
					AVG(hc.cantidad_cosechada) promedio_cosecha
				FROM HISTORIAL_COSECHA hc
				JOIN TIPO_CULTIVO tc ON hc.id_tipo_cultivo = tc.id_tipo_cultivo
				GROUP BY tc.nombre
				ORDER BY total_cosechado DESC
				""";

		return jdbcTemplate.queryForList(sql);
	}

	/**
	 * Comparar siembra vs cosecha (rendimiento)
	 */
	public List<Map<String, Object>> compararSiembraVsCosecha(int idParcela) {
		parcelaService.validarParcelaExiste(idParcela);

		String sql = """
				SELECT 
					tc.nombre cultivo,
					SUM(hs.cantidad_sembrada) total_sembrado,
					SUM(hc.cantidad_cosechada) total_cosechado,
					CASE 
						WHEN SUM(hs.cantidad_sembrada) > 0 
						THEN (SUM(hc.cantidad_cosechada) / SUM(hs.cantidad_sembrada)) * 100 
						ELSE 0 
					END rendimiento_porcentaje
				FROM HISTORIAL_SIEMBRA hs
				LEFT JOIN HISTORIAL_COSECHA hc 
					ON hs.id_tipo_cultivo = hc.id_tipo_cultivo 
					AND hs.id_parcela = hc.id_parcela
				JOIN TIPO_CULTIVO tc ON hs.id_tipo_cultivo = tc.id_tipo_cultivo
				WHERE hs.id_parcela = ?
				GROUP BY tc.nombre
				""";

		return jdbcTemplate.queryForList(sql, idParcela);
	}

	// ======================================
	// MÉTODOS DE VALIDACIÓN
	// ======================================

	/**
	 * Validar que el tipo de cultivo existe
	 */
	public void validarTipoCultivoExiste(int idTipoCultivo) {
		String sql = "SELECT COUNT(1) FROM TIPO_CULTIVO WHERE id_tipo_cultivo = ?";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, idTipoCultivo);
		if (cont == 0) {
			throw new RuntimeException("No existe el tipo de cultivo con id = " + idTipoCultivo);
		}
	}

	/**
	 * Validar cantidad cosechada
	 */
	public void validarCantidadCosechada(double cantidad) {
		if (cantidad <= 0) {
			throw new RuntimeException("La cantidad cosechada debe ser mayor a 0.");
		}
	}

	/**
	 * Validar que existe una siembra previa
	 */
	public void validarExisteSiembra(int idParcela, int idTipoCultivo) {
		String sql = """
				SELECT COUNT(1) 
				FROM HISTORIAL_SIEMBRA 
				WHERE id_parcela = ? AND id_tipo_cultivo = ?
				""";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, idParcela, idTipoCultivo);
		if (cont == 0) {
			throw new RuntimeException("No existe siembra previa de este cultivo en la parcela especificada.");
		}
	}

	// ======================================
	// MÉTODOS AUXILIARES
	// ======================================

	/**
	 * Obtener cantidad estimada de la última siembra
	 */
	private double obtenerCantidadEstimada(int idParcela, int idTipoCultivo) {
		String sql = """
				SELECT TOP 1 cantidad_sembrada 
				FROM HISTORIAL_SIEMBRA 
				WHERE id_parcela = ? AND id_tipo_cultivo = ?
				ORDER BY fecha_siembra DESC
				""";

		try {
			Double cantidad = jdbcTemplate.queryForObject(sql, Double.class, idParcela, idTipoCultivo);
			// Estimación: se asume un rendimiento del 80% de lo sembrado
			return cantidad != null ? cantidad * 0.8 : 0.0;
		} catch (Exception e) {
			return 0.0;
		}
	}

	/**
	 * Actualizar stock de cosecha
	 */
	private void actualizarStockCosecha(int idTipoCultivo, double cantidadCosechada) {
		// Verificar si existe registro en STOCK_COSECHA
		String sqlCheck = "SELECT COUNT(1) FROM STOCK_COSECHA WHERE id_tipo_cultivo = ?";
		int existe = jdbcTemplate.queryForObject(sqlCheck, Integer.class, idTipoCultivo);

		if (existe > 0) {
			// Actualizar stock existente
			String sqlUpdate = """
					UPDATE STOCK_COSECHA 
					SET cantidad_disponible = cantidad_disponible + ?,
					    fecha_actualizacion = GETDATE()
					WHERE id_tipo_cultivo = ?
					""";
			jdbcTemplate.update(sqlUpdate, cantidadCosechada, idTipoCultivo);
		} else {
			// Crear nuevo registro de stock
			String sqlInsert = """
					INSERT INTO STOCK_COSECHA (id_tipo_cultivo, cantidad_disponible, fecha_actualizacion)
					VALUES (?, ?, GETDATE())
					""";
			jdbcTemplate.update(sqlInsert, idTipoCultivo, cantidadCosechada);
		}
	}

}

