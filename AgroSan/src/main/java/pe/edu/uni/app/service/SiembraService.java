package pe.edu.uni.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.uni.app.dto.SiembraDto;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class SiembraService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ParcelaService parcelaService;

	@Autowired
	private EmpleadoService empleadoService;

	/**
	 * Registrar nueva siembra (RF1)
	 * Estilo Coronel: Variables -> Validaciones -> Proceso
	 */
	@Transactional(
			propagation = Propagation.REQUIRES_NEW,
			rollbackFor = Exception.class
	)
	public SiembraDto registrarSiembra(SiembraDto bean) {
		// ******************************
		// Variables
		// ******************************
		String sql;
		KeyHolder keyHolder = new GeneratedKeyHolder();
		LocalDate fechaSiembra = LocalDate.now();

		// ******************************
		// Validaciones
		// ******************************
		// Validar que la parcela existe y está activa (usa ParcelaService)
		parcelaService.validarParcelaActiva(bean.getId_parcela());

		// Validar que el empleado existe y está activo
		empleadoService.validarEmpleadoActivo(bean.getId_empleado());

		// Validar que el tipo de cultivo existe
		this.validarTipoCultivoExiste(bean.getId_tipo_cultivo());

		// Validar cantidad sembrada
		this.validarCantidadSembrada(bean.getCantidad_sembrada());

		// ******************************
		// Proceso
		// ******************************
		// Insertar en HISTORIAL_SIEMBRA
		sql = """
				INSERT INTO HISTORIAL_SIEMBRA 
				(id_tipo_cultivo, id_parcela, id_empleado, fecha_siembra, cantidad_sembrada) 
				VALUES (?, ?, ?, ?, ?)
				""";

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_siembra"});
			ps.setInt(1, bean.getId_tipo_cultivo());
			ps.setInt(2, bean.getId_parcela());
			ps.setInt(3, bean.getId_empleado());
			ps.setDate(4, Date.valueOf(fechaSiembra));
			ps.setDouble(5, bean.getCantidad_sembrada());
			return ps;
		}, keyHolder);

		// Obtener ID generado
		int idSiembra = keyHolder.getKey().intValue();
		bean.setId_siembra(idSiembra);
		bean.setFecha_siembra(fechaSiembra.toString());

		// Calcular y establecer fecha estimada de cosecha (según tipo de cultivo)
		int diasCosecha = this.obtenerDiasCosechaPorTipo(bean.getId_tipo_cultivo());
		LocalDate fechaEstimadaCosecha = fechaSiembra.plusDays(diasCosecha);
		bean.setFecha_estimada_cosecha(fechaEstimadaCosecha.toString());

		// Actualizar stock de semillas (restar cantidad sembrada)
		this.actualizarStockSemillas(bean.getId_tipo_cultivo(), bean.getCantidad_sembrada());

		return bean;
	}

	/**
	 * Listar todas las siembras activas
	 */
	public List<Map<String, Object>> listarSiembras() {
		String sql = """
				SELECT 
					hs.id_siembra, hs.fecha_siembra, hs.cantidad_sembrada,
					tc.nombre tipo_cultivo, tc.tipo categoria,
					p.ubicacion parcela, p.area area_parcela,
					CONCAT(e.nombre, ' ', e.apellido) empleado
				FROM HISTORIAL_SIEMBRA hs
				JOIN TIPO_CULTIVO tc ON hs.id_tipo_cultivo = tc.id_tipo_cultivo
				JOIN PARCELA p ON hs.id_parcela = p.id_parcela
				JOIN EMPLEADO e ON hs.id_empleado = e.id_empleado
				ORDER BY hs.fecha_siembra DESC
				""";

		return jdbcTemplate.queryForList(sql);
	}

	/**
	 * Consultar siembras por parcela
	 */
	public List<Map<String, Object>> listarSiembrasPorParcela(int idParcela) {
		// Validar que la parcela existe (usa ParcelaService)
		parcelaService.validarParcelaExiste(idParcela);

		String sql = """
				SELECT 
					hs.id_siembra, hs.fecha_siembra, hs.cantidad_sembrada,
					tc.nombre tipo_cultivo, tc.tipo categoria,
					CONCAT(e.nombre, ' ', e.apellido) empleado
				FROM HISTORIAL_SIEMBRA hs
				JOIN TIPO_CULTIVO tc ON hs.id_tipo_cultivo = tc.id_tipo_cultivo
				JOIN EMPLEADO e ON hs.id_empleado = e.id_empleado
				WHERE hs.id_parcela = ?
				ORDER BY hs.fecha_siembra DESC
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
	 * Validar cantidad sembrada
	 */
	public void validarCantidadSembrada(double cantidad) {
		if (cantidad <= 0) {
			throw new RuntimeException("La cantidad sembrada debe ser mayor a 0.");
		}
	}

	// ======================================
	// MÉTODOS AUXILIARES
	// ======================================

	/**
	 * Obtener días estimados para cosecha según tipo de cultivo
	 */
	private int obtenerDiasCosechaPorTipo(int idTipoCultivo) {
		String sql = "SELECT tipo FROM TIPO_CULTIVO WHERE id_tipo_cultivo = ?";
		String tipo = jdbcTemplate.queryForObject(sql, String.class, idTipoCultivo);

		// Días estimados según tipo (aproximado)
		return switch (tipo.toLowerCase()) {
			case "hortaliza" -> 90;  // 3 meses
			case "fruta" -> 120;     // 4 meses
			case "cereal" -> 150;    // 5 meses
			default -> 100;          // Por defecto
		};
	}

	/**
	 * Actualizar stock de semillas al sembrar
	 */
	private void actualizarStockSemillas(int idTipoCultivo, double cantidadSembrada) {
		String sql = """
				UPDATE STOCK_SEMILLAS 
				SET cantidad_disponible = cantidad_disponible - ?,
				    fecha_actualizacion = GETDATE()
				WHERE id_tipo_cultivo = ?
				""";

		jdbcTemplate.update(sql, cantidadSembrada, idTipoCultivo);
	}

	/**
	 * Verificar si hay stock suficiente de semillas
	 */
	public boolean hayStockSuficiente(int idTipoCultivo, double cantidadNecesaria) {
		String sql = "SELECT cantidad_disponible FROM STOCK_SEMILLAS WHERE id_tipo_cultivo = ?";
		Double stockActual = jdbcTemplate.queryForObject(sql, Double.class, idTipoCultivo);

		return stockActual != null && stockActual >= cantidadNecesaria;
	}

}
