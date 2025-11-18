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
public class CosechaService{
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ParcelaService parcelaService;

	@Autowired
	private EmpleadoService empleadoService;

	//REGISTRAR LA COSECHA EN LA BD
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public CosechaDto registrarCosecha(CosechaDto bean){
		//VARIABLES
		String sql;
		KeyHolder keyHolder = new GeneratedKeyHolder();
		LocalDate fechaCosecha = LocalDate.now();

		//VALIDACIONES
		parcelaService.validarParcelaExiste(bean.getId_parcela());
		empleadoService.validarEmpleadoActivo(bean.getId_empleado());
		this.validarTipoCultivoExiste(bean.getId_tipo_cultivo());
		this.validarCantidadCosechada(bean.getCantidad_cosechada());
		this.validarExisteSiembra(bean.getId_parcela(), bean.getId_tipo_cultivo());

		//PROCESO
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
		int idCosecha = keyHolder.getKey().intValue();
		bean.setId_cosecha(idCosecha);
		bean.setFecha_cosecha(fechaCosecha.toString());
		double cantidadEstimada = this.obtenerCantidadEstimada(bean.getId_parcela(), bean.getId_tipo_cultivo());
		bean.setCantidad_estimada(cantidadEstimada);
		if (cantidadEstimada > 0){
			double rendimiento = (bean.getCantidad_cosechada() / cantidadEstimada) * 100;
			bean.setRendimiento(rendimiento);
		} else{
			bean.setRendimiento(100.0); // Si no hay estimado, consideramos 100%
		}
		this.actualizarStockCosecha(bean.getId_tipo_cultivo(), bean.getCantidad_cosechada());
		return bean;
	}

	//LISTAR TODAS LAS COSECHAS
	public List<Map<String, Object>> listarCosechas(){
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
				ORDER BY hc.fecha_cosecha ASC
				""";
		return jdbcTemplate.queryForList(sql);
	}

	//LISTAR LAS COSECHAS HECHAS EN UNA PARCELA
	public List<Map<String, Object>> listarCosechasPorParcela(int idParcela){
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
				ORDER BY hc.fecha_cosecha ASC
				""";
		return jdbcTemplate.queryForList(sql, idParcela);
	}

	//CONSULTAR EL RENDIMIENTO DE UN CULTIVO
	public List<Map<String, Object>> reporteRendimientoPorCultivo(){
		String sql = """
				SELECT 
					tc.nombre tipo_cultivo,
					COUNT(hc.id_cosecha) total_cosechas,
					SUM(hc.cantidad_cosechada) total_cosechado,
					AVG(hc.cantidad_cosechada) promedio_cosecha
				FROM HISTORIAL_COSECHA hc
				JOIN TIPO_CULTIVO tc ON hc.id_tipo_cultivo = tc.id_tipo_cultivo
				GROUP BY tc.nombre
				ORDER BY total_cosechado ASC
				""";
		return jdbcTemplate.queryForList(sql);
	}

	//COMPARAR SIEMBRA VS COSECHA
	public List<Map<String, Object>> compararSiembraVsCosecha(int idParcela){
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

	//VALIDAR QUE EL TIPO DE CULTIVO EXISTE
	public void validarTipoCultivoExiste(int idTipoCultivo){
		String sql = "SELECT COUNT(1) FROM TIPO_CULTIVO WHERE id_tipo_cultivo = ?";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, idTipoCultivo);
		if (cont == 0) {
			throw new RuntimeException("No existe el tipo de cultivo con id = " + idTipoCultivo);
		}
	}

	//VALIDAR QUE LA CANTIDAD COSECHADA SEA CORRECTA
	public void validarCantidadCosechada(double cantidad){
		if (cantidad <= 0) {
			throw new RuntimeException("La cantidad cosechada debe ser mayor a 0.");
		}
	}

	//VALIDAR QUE SE HA SEMBRADO PREVIAMENTE
	public void validarExisteSiembra(int idParcela, int idTipoCultivo){
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

	//OBTENER CANTIDAD ESTIMADA DE LA ÚLTIMA SIEMBRA
	private double obtenerCantidadEstimada(int idParcela, int idTipoCultivo){
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
		} catch (Exception e){
			return 0.0;
		}
	}

	//ACTUALIZAR EL STOCK LUEGO DE LA COSECHA
	private void actualizarStockCosecha(int idTipoCultivo, double cantidadCosechada){
		String sqlCheck = "SELECT COUNT(1) FROM STOCK_COSECHA WHERE id_tipo_cultivo = ?";
		int existe = jdbcTemplate.queryForObject(sqlCheck, Integer.class, idTipoCultivo);
		if (existe > 0){
			String sqlUpdate = """
					UPDATE STOCK_COSECHA 
					SET cantidad_disponible = cantidad_disponible + ?,
					    fecha_actualizacion = GETDATE()
					WHERE id_tipo_cultivo = ?
					""";
			jdbcTemplate.update(sqlUpdate, cantidadCosechada, idTipoCultivo);
		} else{
			String sqlInsert = """
					INSERT INTO STOCK_COSECHA (id_tipo_cultivo, cantidad_disponible, fecha_actualizacion)
					VALUES (?, ?, GETDATE())
					""";
			jdbcTemplate.update(sqlInsert, idTipoCultivo, cantidadCosechada);
		}
	}
}