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
public class ParcelaService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * Registrar una nueva parcela en la base de datos
	 * Estilo Coronel: Variables -> Validaciones -> Proceso
	 */
	@Transactional(
			propagation = Propagation.REQUIRES_NEW,
			rollbackFor = Exception.class
	)
	public ParcelaDto registrarParcela(ParcelaDto bean) {
		// ******************************
		// Variables
		// ******************************
		String sql;
		KeyHolder keyHolder = new GeneratedKeyHolder();

		// ******************************
		// Validaciones
		// ******************************
		// Validar área de la parcela
		this.validarArea(bean.getArea());
		
		// Validar estado inicial (debe ser "Inactiva" = 1)
		this.validarEstadoInicial(bean.getId_estado_parcela());

		// ******************************
		// Proceso
		// ******************************
		sql = "INSERT INTO PARCELA (ubicacion, area, id_estado_parcela) VALUES (?, ?, ?)";
		
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_parcela"});
			ps.setString(1, bean.getUbicacion());
			ps.setDouble(2, bean.getArea());
			ps.setInt(3, bean.getId_estado_parcela());
			return ps;
		}, keyHolder);

		// Obtener el ID generado
		int idGenerado = keyHolder.getKey().intValue();
		bean.setId_parcela(idGenerado);

		return bean;
	}

	/**
	 * Obtener parcela por ID
	 */
	public ParcelaDto obtenerParcela(int idParcela) {
		// Validar que existe
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

	/**
	 * Listar todas las parcelas
	 */
	public List<Map<String, Object>> listarParcelas() {
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

	/**
	 * Listar parcelas activas disponibles para siembra
	 */
	public List<Map<String, Object>> listarParcelasDisponibles() {
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

	// ======================================
	// MÉTODOS DE VALIDACIÓN REUTILIZABLES
	// ======================================

	/**
	 * Validar que el área sea válida
	 */
	public void validarArea(double area) {
		if (area < 0.0) {
			throw new RuntimeException("El área de la parcela debe ser positiva.");
		}
		if (area < 100.0) {
			throw new RuntimeException("El área para una parcela debe ser como mínimo 100 m².");
		}
	}

	/**
	 * Validar estado inicial de parcela (debe ser Inactiva = 1)
	 */
	public void validarEstadoInicial(int idEstadoParcela) {
		if (idEstadoParcela != 1) {
			throw new RuntimeException("El estado de la parcela debe ser 'Inactiva' (1) para el registro inicial.");
		}
	}

	/**
	 * Validar que la parcela existe en la base de datos
	 */
	public void validarParcelaExiste(int idParcela) {
		String sql = "SELECT COUNT(1) FROM PARCELA WHERE id_parcela = ?";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, idParcela);
		if (cont == 0) {
			throw new RuntimeException("No existe parcela con id = " + idParcela);
		}
	}

	/**
	 * Validar que la parcela está lista para siembra (estado Activa = 2)
	 */
	public void validarParcelaActiva(int idParcela) {
		// Primero validar que existe
		this.validarParcelaExiste(idParcela);
		
		String sql = "SELECT id_estado_parcela FROM PARCELA WHERE id_parcela = ?";
		int estado = jdbcTemplate.queryForObject(sql, Integer.class, idParcela);
		if (estado != 2) {
			throw new RuntimeException("La parcela debe estar en estado 'Activa' (2) para poder sembrar.");
		}
	}

	/**
	 * Obtener el estado actual de una parcela
	 */
	public int obtenerEstadoParcela(int idParcela) {
		this.validarParcelaExiste(idParcela);
		
		String sql = "SELECT id_estado_parcela FROM PARCELA WHERE id_parcela = ?";
		return jdbcTemplate.queryForObject(sql, Integer.class, idParcela);
	}

	/**
	 * Verificar si una parcela existe (retorna boolean)
	 */
	public boolean existeParcela(int idParcela) {
		String sql = "SELECT COUNT(1) FROM PARCELA WHERE id_parcela = ?";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, idParcela);
		return (cont == 1);
	}

	/**
	 * Cambiar el estado de una parcela
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public void cambiarEstadoParcela(int idParcela, int nuevoEstado) {
		this.validarParcelaExiste(idParcela);
		
		String sql = "UPDATE PARCELA SET id_estado_parcela = ? WHERE id_parcela = ?";
		jdbcTemplate.update(sql, nuevoEstado, idParcela);
	}
}
