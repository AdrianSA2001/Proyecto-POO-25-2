package pe.edu.uni.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.uni.app.dto.CompradorDto;
import pe.edu.uni.app.dto.VentaDto;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class VentaService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * Registrar venta (RF5)
	 * Estilo Coronel: Variables -> Validaciones -> Proceso
	 */
	@Transactional(
			propagation = Propagation.REQUIRES_NEW,
			rollbackFor = Exception.class
	)
	public VentaDto registrarVenta(VentaDto bean) {
		// ******************************
		// Variables
		// ******************************
		String sql;
		KeyHolder keyHolder = new GeneratedKeyHolder();
		LocalDate fechaVenta = LocalDate.now();

		// ******************************
		// Validaciones
		// ******************************
		// Validar que el comprador existe
		this.validarCompradorExiste(bean.getId_comprador());

		// Validar que el tipo de cultivo existe
		this.validarTipoCultivoExiste(bean.getId_tipo_cultivo());

		// Validar cantidad vendida
		this.validarCantidadVendida(bean.getCantidad_vendida());

		// Validar precio unitario
		this.validarPrecioUnitario(bean.getPrecio_unitario());

		// Validar que hay stock suficiente
		this.validarStockSuficiente(bean.getId_tipo_cultivo(), bean.getCantidad_vendida());

		// ******************************
		// Proceso
		// ******************************
		// Calcular precio total
		double precioTotal = bean.getCantidad_vendida() * bean.getPrecio_unitario();
		bean.setPrecio_total(precioTotal);

		// Insertar en VENTA
		sql = """
				INSERT INTO VENTA 
				(id_comprador, id_tipo_cultivo, fecha_venta, cantidad_vendida, precio_unitario) 
				VALUES (?, ?, ?, ?, ?)
				""";

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_venta"});
			ps.setInt(1, bean.getId_comprador());
			ps.setInt(2, bean.getId_tipo_cultivo());
			ps.setDate(3, Date.valueOf(fechaVenta));
			ps.setDouble(4, bean.getCantidad_vendida());
			ps.setDouble(5, bean.getPrecio_unitario());
			return ps;
		}, keyHolder);

		// Obtener ID generado
		int idVenta = keyHolder.getKey().intValue();
		bean.setId_venta(idVenta);
		bean.setFecha_venta(fechaVenta.toString());

		// Actualizar stock de cosecha (restar cantidad vendida)
		this.actualizarStockCosecha(bean.getId_tipo_cultivo(), bean.getCantidad_vendida());

		return bean;
	}

	/**
	 * Listar todas las ventas
	 */
	public List<Map<String, Object>> listarVentas() {
		String sql = """
				SELECT 
					v.id_venta, v.fecha_venta, v.cantidad_vendida, 
					v.precio_unitario, (v.cantidad_vendida * v.precio_unitario) precio_total,
					c.nombre comprador, c.telefono telefono_comprador,
					tc.nombre tipo_cultivo
				FROM VENTA v
				JOIN COMPRADOR c ON v.id_comprador = c.id_comprador
				JOIN TIPO_CULTIVO tc ON v.id_tipo_cultivo = tc.id_tipo_cultivo
				ORDER BY v.fecha_venta DESC
				""";

		return jdbcTemplate.queryForList(sql);
	}

	/**
	 * Listar ventas por comprador
	 */
	public List<Map<String, Object>> listarVentasPorComprador(int idComprador) {
		this.validarCompradorExiste(idComprador);

		String sql = """
				SELECT 
					v.id_venta, v.fecha_venta, v.cantidad_vendida, 
					v.precio_unitario, (v.cantidad_vendida * v.precio_unitario) precio_total,
					tc.nombre tipo_cultivo
				FROM VENTA v
				JOIN TIPO_CULTIVO tc ON v.id_tipo_cultivo = tc.id_tipo_cultivo
				WHERE v.id_comprador = ?
				ORDER BY v.fecha_venta DESC
				""";

		return jdbcTemplate.queryForList(sql, idComprador);
	}

	/**
	 * Reporte de ventas por tipo de cultivo
	 */
	public List<Map<String, Object>> reporteVentasPorCultivo() {
		String sql = """
				SELECT 
					tc.nombre tipo_cultivo,
					COUNT(v.id_venta) total_ventas,
					SUM(v.cantidad_vendida) cantidad_total_vendida,
					SUM(v.cantidad_vendida * v.precio_unitario) ingresos_totales,
					AVG(v.precio_unitario) precio_promedio
				FROM VENTA v
				JOIN TIPO_CULTIVO tc ON v.id_tipo_cultivo = tc.id_tipo_cultivo
				GROUP BY tc.nombre
				ORDER BY ingresos_totales DESC
				""";

		return jdbcTemplate.queryForList(sql);
	}

	/**
	 * Consultar stock disponible por cultivo
	 */
	public List<Map<String, Object>> consultarStockDisponible() {
		String sql = """
				SELECT 
					tc.nombre tipo_cultivo,
					sc.cantidad_disponible stock_disponible,
					sc.fecha_actualizacion ultima_actualizacion
				FROM STOCK_COSECHA sc
				JOIN TIPO_CULTIVO tc ON sc.id_tipo_cultivo = tc.id_tipo_cultivo
				WHERE sc.cantidad_disponible > 0
				ORDER BY tc.nombre
				""";

		return jdbcTemplate.queryForList(sql);
	}

	// ======================================
	// MÉTODOS PARA COMPRADOR
	// ======================================

	/**
	 * Registrar nuevo comprador
	 */
	@Transactional(
			propagation = Propagation.REQUIRES_NEW,
			rollbackFor = Exception.class
	)
	public CompradorDto registrarComprador(CompradorDto bean) {
		// ******************************
		// Variables
		// ******************************
		String sql;
		KeyHolder keyHolder = new GeneratedKeyHolder();

		// ******************************
		// Validaciones
		// ******************************
		// Validar que el nombre no esté vacío
		if (bean.getNombre() == null || bean.getNombre().trim().isEmpty()) {
			throw new RuntimeException("El nombre del comprador es obligatorio.");
		}

		// ******************************
		// Proceso
		// ******************************
		sql = """
				INSERT INTO COMPRADOR (nombre, telefono, email, direccion) 
				VALUES (?, ?, ?, ?)
				""";

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_comprador"});
			ps.setString(1, bean.getNombre());
			ps.setString(2, bean.getTelefono());
			ps.setString(3, bean.getEmail());
			ps.setString(4, bean.getDireccion());
			return ps;
		}, keyHolder);

		// Obtener ID generado
		int idGenerado = keyHolder.getKey().intValue();
		bean.setId_comprador(idGenerado);

		return bean;
	}

	/**
	 * Listar todos los compradores
	 */
	public List<Map<String, Object>> listarCompradores() {
		String sql = """
				SELECT 
					id_comprador, nombre, telefono, email, direccion
				FROM COMPRADOR
				ORDER BY nombre
				""";

		return jdbcTemplate.queryForList(sql);
	}

	// ======================================
	// MÉTODOS DE VALIDACIÓN
	// ======================================

	/**
	 * Validar que el comprador existe
	 */
	public void validarCompradorExiste(int idComprador) {
		String sql = "SELECT COUNT(1) FROM COMPRADOR WHERE id_comprador = ?";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, idComprador);
		if (cont == 0) {
			throw new RuntimeException("No existe comprador con id = " + idComprador);
		}
	}

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
	 * Validar cantidad vendida
	 */
	public void validarCantidadVendida(double cantidad) {
		if (cantidad <= 0) {
			throw new RuntimeException("La cantidad vendida debe ser mayor a 0.");
		}
	}

	/**
	 * Validar precio unitario
	 */
	public void validarPrecioUnitario(double precio) {
		if (precio <= 0) {
			throw new RuntimeException("El precio unitario debe ser mayor a 0.");
		}
	}

	/**
	 * Validar que hay stock suficiente para la venta
	 */
	public void validarStockSuficiente(int idTipoCultivo, double cantidadVendida) {
		String sql = "SELECT cantidad_disponible FROM STOCK_COSECHA WHERE id_tipo_cultivo = ?";
		
		try {
			Double stockDisponible = jdbcTemplate.queryForObject(sql, Double.class, idTipoCultivo);
			
			if (stockDisponible == null || stockDisponible < cantidadVendida) {
				throw new RuntimeException(
						"Stock insuficiente. Disponible: " + 
						(stockDisponible != null ? stockDisponible : 0) + 
						", Solicitado: " + cantidadVendida
				);
			}
		} catch (org.springframework.dao.EmptyResultDataAccessException e) {
			throw new RuntimeException("No hay stock disponible para este tipo de cultivo.");
		}
	}

	// ======================================
	// MÉTODOS AUXILIARES
	// ======================================

	/**
	 * Actualizar stock de cosecha (restar cantidad vendida)
	 */
	private void actualizarStockCosecha(int idTipoCultivo, double cantidadVendida) {
		String sql = """
				UPDATE STOCK_COSECHA 
				SET cantidad_disponible = cantidad_disponible - ?,
				    fecha_actualizacion = GETDATE()
				WHERE id_tipo_cultivo = ?
				""";

		jdbcTemplate.update(sql, cantidadVendida, idTipoCultivo);
	}

	/**
	 * Obtener stock actual de un cultivo
	 */
	public double obtenerStockActual(int idTipoCultivo) {
		String sql = "SELECT ISNULL(cantidad_disponible, 0) FROM STOCK_COSECHA WHERE id_tipo_cultivo = ?";
		
		try {
			return jdbcTemplate.queryForObject(sql, Double.class, idTipoCultivo);
		} catch (Exception e) {
			return 0.0;
		}
	}

}

