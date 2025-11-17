package pe.edu.uni.app.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.uni.app.dto.ParcelaDto;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
@Service
public class ParcelaService{
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	//REGISTRAR UNA NUEVA PARCELA EN LA BD
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public ParcelaDto registrarParcela(ParcelaDto bean){
		//VARIABLES
		String sql;
		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		//VALIDACIONES
		this.validarArea(bean.getArea());
		this.validarEstadoInicial(bean.getId_estado_parcela());
		
		//PROCESO
		sql = "INSERT INTO PARCELA (ubicacion, area, id_estado_parcela) VALUES (?, ?, ?)";
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_parcela"});
			ps.setString(1, bean.getUbicacion());
			ps.setDouble(2, bean.getArea());
			ps.setInt(3, bean.getId_estado_parcela());
			return ps;
		}, keyHolder);
		int idGenerado = keyHolder.getKey().intValue();
		bean.setId_parcela(idGenerado);
		return bean;
	}
	
	//OBTENER DATOS DE UNA PARCELA
	public ParcelaDto obtenerParcela(int idParcela){
		this.validarParcelaExiste(idParcela);
		String sql = """
				SELECT id_parcela, area, id_estado_parcela 
				FROM PARCELA 
				WHERE id_parcela = ?
				""";
		return jdbcTemplate.queryForObject(
				sql,
				new BeanPropertyRowMapper<>(ParcelaDto.class),
				idParcela
		);
	}
	
	//LISTAR TODAS LAS PARCELAS
	public List<Map<String, Object>> listarParcelas(){
		String sql = """
				SELECT 
					p.id_parcela, p.ubicacion, p.area,
					ep.descripcion estado_parcela
				FROM PARCELA p
				JOIN ESTADO_PARCELA ep ON p.id_estado_parcela = ep.id_estado_parcela
				ORDER BY p.id_parcela
				""";
		return jdbcTemplate.queryForList(sql);
	}
	
	//LISTAR LAS PARCELAS DISPONIBLES (ACTIVADAS)
	public List<Map<String, Object>> listarParcelasDisponibles(){
		String sql = """
				SELECT 
					p.id_parcela, p.ubicacion, p.area,
					ep.descripcion estado_parcela
				FROM PARCELA p
				JOIN ESTADO_PARCELA ep ON p.id_estado_parcela = ep.id_estado_parcela
				WHERE p.id_estado_parcela = 2
				ORDER BY p.id_parcela
				""";
		return jdbcTemplate.queryForList(sql);
	}
	
	//LISTAR LAS PARCELAS NO DISPONIBLES (INACTIVADAS)
		public List<Map<String, Object>> listarParcelasNoDisponibles(){
			String sql = """
					SELECT 
						p.id_parcela, p.ubicacion, p.area,
						ep.descripcion estado_parcela
					FROM PARCELA p
					JOIN ESTADO_PARCELA ep ON p.id_estado_parcela = ep.id_estado_parcela
					WHERE p.id_estado_parcela = 1
					ORDER BY p.id_parcela
					""";
			return jdbcTemplate.queryForList(sql);
		}

	//VALIDAR EL AREA DE LA PARCELA
	public void validarArea(double area){
		if (area < 0.0) {
			throw new RuntimeException("El área de la parcela debe ser positiva.");
		}
		if (area < 100.0) {
			throw new RuntimeException("El área para una parcela debe ser como mínimo 100 m².");
		}
	}
	
	//VALIDAR QUE LA PARCELA ESTE INACTIVA AL REGISTRARSE
	public void validarEstadoInicial(int idEstadoParcela){
		if (idEstadoParcela != 1) {
			throw new RuntimeException("El estado de la parcela debe ser 'Inactiva' (1) para el registro inicial.");
		}
	}
	
	//VALIDAR QUE LA PARCELA EXISTE EN LA BD
	public void validarParcelaExiste(int idParcela){
		String sql = "SELECT COUNT(1) FROM PARCELA WHERE id_parcela = ?";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, idParcela);
		if (cont == 0) {
			throw new RuntimeException("No existe parcela con id = " + idParcela);
		}
	}
	
	//VALIDAR QUE LA PARCELA ESTÁ ACTIVA
	public void validarParcelaActiva(int idParcela){
		this.validarParcelaExiste(idParcela);
		String sql = "SELECT id_estado_parcela FROM PARCELA WHERE id_parcela = ?";
		int estado = jdbcTemplate.queryForObject(sql, Integer.class, idParcela);
		if (estado != 2){
			throw new RuntimeException("La parcela debe estar en estado 'Activa' (2) para poder sembrar.");
		}
	}
	
	//OBTENER EL ESTADO DE LA PARCELA
	public int obtenerEstadoParcela(int idParcela){
		this.validarParcelaExiste(idParcela);
		String sql = "SELECT id_estado_parcela FROM PARCELA WHERE id_parcela = ?";
		return jdbcTemplate.queryForObject(sql, Integer.class, idParcela);
	}
	
	//VALIDAR QUE LA PARCELA EXISTE (RETORNA UN BOOLEANO)
	public boolean existeParcela(int idParcela){
		String sql = "SELECT COUNT(1) FROM PARCELA WHERE id_parcela = ?";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, idParcela);
		return (cont == 1);
	}
	
	//ACTUALIZAR EL ESTADO DE LA PARCELA
	@Transactional(propagation = Propagation.MANDATORY)
	public void cambiarEstadoParcela(int idParcela, int nuevoEstado){
		this.validarParcelaExiste(idParcela);
		String sql = "UPDATE PARCELA SET id_estado_parcela = ? WHERE id_parcela = ?";
		jdbcTemplate.update(sql, nuevoEstado, idParcela);
	}
}