package pe.edu.uni.app.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.uni.app.dto.SiembraDto;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
@Service
public class SiembraService{
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ParcelaService parcelaService;

	@Autowired
	private EmpleadoService empleadoService;

	//PROGRAMAR UNA SIEMBRA EN LA BD
	@Transactional(rollbackFor = Exception.class)
	public SiembraDto programarSiembra(SiembraDto bean){
		//VARIABLES
		String sql;
		KeyHolder keyHolder = new GeneratedKeyHolder();
		LocalDate fechaSiembra = LocalDate.now();

		//VALIDACIONES
		this.validarTipoCultivoExiste(bean.getId_tipo_cultivo());
		parcelaService.validarParcelaInactiva(bean.getId_parcela());
		this.validarParcelaNoSembrada(bean.getId_parcela());
		empleadoService.validarEmpleadoActivo(bean.getId_empleado());
		this.validarCantidadSembrada(bean.getCantidad_sembrada());
		if (!hayStockSuficiente(bean.getId_tipo_cultivo(), bean.getCantidad_sembrada())){
		    throw new RuntimeException("No hay stock suficiente de semillas.");
		}
		
		//PROCESO
		sql = """
				INSERT INTO PROGRAMACION_SIEMBRA 
				(id_tipo_cultivo, id_parcela, id_empleado, fecha_siembra, cantidad_sembrada, id_estado_actividad) 
				VALUES (?, ?, ?, ?, ?, ?)
				""";
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_siembra"});
			ps.setInt(1, bean.getId_tipo_cultivo());
			ps.setInt(2, bean.getId_parcela());
			ps.setInt(3, bean.getId_empleado());
			ps.setDate(4, Date.valueOf(fechaSiembra));
			ps.setDouble(5, bean.getCantidad_sembrada());
			ps.setInt(6, 1);
			return ps;
		}, keyHolder);
		int idSiembra = keyHolder.getKey().intValue();
		bean.setId_siembra(idSiembra);
		bean.setFecha_siembra(fechaSiembra.toString());
		bean.setId_estado_actividad(1);
		int diasCosecha = this.obtenerDiasCosechaPorTipo(bean.getId_tipo_cultivo());
		LocalDate fechaEstimadaCosecha = fechaSiembra.plusDays(diasCosecha);
		bean.setFecha_estimada_cosecha(fechaEstimadaCosecha.toString());
		this.actualizarStockSemillas(bean.getId_tipo_cultivo(), bean.getCantidad_sembrada());
		parcelaService.cambiarEstadoParcela(bean.getId_parcela(), 2);
		return bean;
	}
	
	//FINALIZAR LA SIEMBRA EN LA BD
	@Transactional(rollbackFor = Exception.class)
	public void cambiarEstadoSiembra(int id_siembra, int estado){
		this.validarSiembraExiste(id_siembra);
		String sql = "UPDATE PROGRAMACION_SIEMBRA SET id_estado_actividad = ? WHERE id_siembra = ?";
		jdbcTemplate.update(sql, estado, id_siembra);
	}
	
	//LISTAR TODAS LAS PARCELAS
	public List<Map<String, Object>> listarSiembras(){
		String sql = """
				SELECT 
					hs.id_siembra, hs.fecha_siembra, hs.cantidad_sembrada,
					tc.nombre tipo_cultivo, tc.tipo categoria,
					p.ubicacion parcela, p.area area_parcela,
					CONCAT(e.nombre, ' ', e.apellido) empleado
				FROM PROGRAMACION_SIEMBRA hs
				JOIN TIPO_CULTIVO tc ON hs.id_tipo_cultivo = tc.id_tipo_cultivo
				JOIN PARCELA p ON hs.id_parcela = p.id_parcela
				JOIN EMPLEADO e ON hs.id_empleado = e.id_empleado
				ORDER BY hs.fecha_siembra ASC
				""";
		return jdbcTemplate.queryForList(sql);
	}
	
	//MOSTRAR LAS PARCELAS LISTAS PARA LA SIEMBRA
	public List<Map<String, Object>> listarSiembrasPorParcela(int idParcela){
		parcelaService.validarParcelaExiste(idParcela);
		String sql = """
				SELECT 
					hs.id_siembra, hs.fecha_siembra, hs.cantidad_sembrada,
					tc.nombre tipo_cultivo, tc.tipo categoria,
					CONCAT(e.nombre, ' ', e.apellido) empleado
				FROM PROGRAMACION_SIEMBRA hs
				JOIN TIPO_CULTIVO tc ON hs.id_tipo_cultivo = tc.id_tipo_cultivo
				JOIN EMPLEADO e ON hs.id_empleado = e.id_empleado
				WHERE hs.id_parcela = ?
				ORDER BY hs.fecha_siembra DESC
				""";
		List<Map<String, Object>> resultado = jdbcTemplate.queryForList(sql, idParcela);
		if (resultado.isEmpty()) {
	        throw new RuntimeException("La parcela con id = " + idParcela + " no tiene siembras registradas.");
	    }
		return resultado;
	}
	

	//VALIDAR QUE LA SIEMBRA EXISTE
	public void validarSiembraExiste(int id_siembra){
		String sql = "SELECT COUNT(1) FROM PROGRAMACION_SIEMBRA WHERE id_siembra = ?";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, id_siembra);
		if (cont == 0){
			throw new RuntimeException("No existe el tipo de cultivo con id = " + id_siembra);
		}
	}
	//VALIDAR QUE EL CULTIVO A SEMBRAR EXISTE
	public void validarTipoCultivoExiste(int idTipoCultivo) {
		String sql = "SELECT COUNT(1) FROM TIPO_CULTIVO WHERE id_tipo_cultivo = ?";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, idTipoCultivo);
		if (cont == 0) {
			throw new RuntimeException("No existe el tipo de cultivo con id = " + idTipoCultivo);
		}
	}
	
	//VALIDAR QUE LA CANTIDAD A SEMBRAR ES CORRECTA
	public void validarCantidadSembrada(double cantidad) {
		if (cantidad <= 0) {
			throw new RuntimeException("La cantidad sembrada debe ser mayor a 0.");
		}
	}
	
	//VALIDAR QUE LA PARCELA NO HA SIDO SEMBRADA ANTES
	private void validarParcelaNoSembrada(int idParcela){
	    String sql = "SELECT COUNT(*) FROM PROGRAMACION_SIEMBRA WHERE id_parcela = ?";
	    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idParcela);

	    if (count != null && count > 0){
	        throw new RuntimeException("La parcela con id = " + idParcela + " ya tiene una siembra registrada.");
	    }
	}

	//OBTENER LOS DÍAS ESTIMADOS PARA LA COSECHA DE LA PARCELA
	private int obtenerDiasCosechaPorTipo(int id_tipo_cultivo){
		return switch (id_tipo_cultivo){
			case 1 -> 90;
			case 2 -> 60;
			case 3 -> 75;
			default -> 90;
		};
	}
	
	//ACTUALIZAR EL STOCK DE SEMILLAS AL SEMBRAR
	private void actualizarStockSemillas(int idTipoCultivo, double cantidadSembrada){
		String sql = """
				UPDATE STOCK_SEMILLAS 
				SET cantidad_disponible = cantidad_disponible - ?,
				    fecha_actualizacion = GETDATE()
				WHERE id_tipo_cultivo = ?
				""";
		jdbcTemplate.update(sql, cantidadSembrada, idTipoCultivo);
	}
	
	//VERIFICAR QUE HAY STOCK SUFICIENTE DE SEMILLAS
	public boolean hayStockSuficiente(int idTipoCultivo, double cantidadNecesaria){
		String sql = "SELECT cantidad_disponible FROM STOCK_SEMILLAS WHERE id_tipo_cultivo = ?";
		Double stockActual = jdbcTemplate.queryForObject(sql, Double.class, idTipoCultivo);
		return stockActual != null && stockActual >= cantidadNecesaria;
	}
}