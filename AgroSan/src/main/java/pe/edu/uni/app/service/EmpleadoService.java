package pe.edu.uni.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.uni.app.dto.EmpleadoDto;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

@Service
public class EmpleadoService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * Registrar un nuevo empleado
	 * Estilo Coronel: Variables -> Validaciones -> Proceso
	 */
	@Transactional(
			propagation = Propagation.REQUIRES_NEW,
			rollbackFor = Exception.class
	)
	public EmpleadoDto registrarEmpleado(EmpleadoDto bean) {
		// ******************************
		// Variables
		// ******************************
		String sql;
		KeyHolder keyHolder = new GeneratedKeyHolder();

		// ******************************
		// Validaciones
		// ******************************
		// Validar que el DNI no esté registrado
		this.validarDniUnico(bean.getDni());

		// Validar que el email no esté registrado
		if (bean.getEmail() != null && !bean.getEmail().isEmpty()) {
			this.validarEmailUnico(bean.getEmail());
		}

		// Validar estado inicial (debe ser Activo = 1)
		this.validarEstadoInicial(bean.getId_estado_empleado());

		// ******************************
		// Proceso
		// ******************************
		sql = """
				INSERT INTO EMPLEADO 
				(nombre, apellido, telefono, email, dni, contraseña, id_estado_empleado) 
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""";

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_empleado"});
			ps.setString(1, bean.getNombre());
			ps.setString(2, bean.getApellido());
			ps.setString(3, bean.getTelefono());
			ps.setString(4, bean.getEmail());
			ps.setString(5, bean.getDni());
			ps.setString(6, bean.getContrasena());
			ps.setString(7, bean.getId_estado_empleado());
			return ps;
		}, keyHolder);

		// Obtener ID generado
		int idGenerado = keyHolder.getKey().intValue();
		bean.setId_empleado(idGenerado);

		return bean;
	}

	/**
	 * Obtener empleado por ID
	 */
	public EmpleadoDto obtenerEmpleado(int idEmpleado) {
		this.validarEmpleadoExiste(idEmpleado);

		String sql = """
				SELECT id_empleado, nombre, apellido, telefono, email, dni, 
				       contraseña contrasena, id_estado_empleado 
				FROM EMPLEADO 
				WHERE id_empleado = ?
				""";

		return jdbcTemplate.queryForObject(
				sql,
				new BeanPropertyRowMapper<>(EmpleadoDto.class),
				idEmpleado
		);
	}

	/**
	 * Listar todos los empleados
	 */
	public List<Map<String, Object>> listarEmpleados() {
		String sql = """
				SELECT 
					e.id_empleado, e.nombre, e.apellido, e.telefono, 
					e.email, e.dni, ee.descripcion estado
				FROM EMPLEADO e
				JOIN ESTADO_EMPLEADO ee ON e.id_estado_empleado = ee.id_estado_empleado
				ORDER BY e.apellido, e.nombre
				""";

		return jdbcTemplate.queryForList(sql);
	}

	/**
	 * Listar empleados activos
	 */
	public List<Map<String, Object>> listarEmpleadosActivos() {
		String sql = """
				SELECT 
					e.id_empleado, e.nombre, e.apellido, e.telefono, e.email
				FROM EMPLEADO e
				WHERE e.id_estado_empleado = '1'
				ORDER BY e.apellido, e.nombre
				""";

		return jdbcTemplate.queryForList(sql);
	}

	/**
	 * Autenticar empleado (login)
	 */
	public Map<String, Object> autenticar(String dni, String contrasena) {
		String sql = """
				SELECT 
					e.id_empleado, e.nombre, e.apellido, e.dni,
					ee.descripcion estado
				FROM EMPLEADO e
				JOIN ESTADO_EMPLEADO ee ON e.id_estado_empleado = ee.id_estado_empleado
				WHERE e.dni = ? AND e.contraseña = ? AND e.id_estado_empleado = '1'
				""";

		List<Map<String, Object>> resultado = jdbcTemplate.queryForList(sql, dni, contrasena);

		if (resultado.isEmpty()) {
			throw new RuntimeException("Credenciales inválidas o empleado inactivo.");
		}

		return resultado.get(0);
	}

	// ======================================
	// MÉTODOS DE VALIDACIÓN REUTILIZABLES
	// ======================================

	/**
	 * Validar que el empleado existe
	 */
	public void validarEmpleadoExiste(int idEmpleado) {
		String sql = "SELECT COUNT(1) FROM EMPLEADO WHERE id_empleado = ?";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, idEmpleado);
		if (cont == 0) {
			throw new RuntimeException("No existe empleado con id = " + idEmpleado);
		}
	}

	/**
	 * Validar que el empleado existe y está activo
	 */
	public void validarEmpleadoActivo(int idEmpleado) {
		this.validarEmpleadoExiste(idEmpleado);

		String sql = "SELECT id_estado_empleado FROM EMPLEADO WHERE id_empleado = ?";
		String estado = jdbcTemplate.queryForObject(sql, String.class, idEmpleado);

		if (!"1".equals(estado)) {
			throw new RuntimeException("El empleado no está activo (estado actual: " + estado + ")");
		}
	}

	/**
	 * Validar DNI único
	 */
	public void validarDniUnico(String dni) {
		String sql = "SELECT COUNT(1) FROM EMPLEADO WHERE dni = ?";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, dni);
		if (cont > 0) {
			throw new RuntimeException("Ya existe un empleado registrado con DNI: " + dni);
		}
	}

	/**
	 * Validar email único
	 */
	public void validarEmailUnico(String email) {
		String sql = "SELECT COUNT(1) FROM EMPLEADO WHERE email = ?";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, email);
		if (cont > 0) {
			throw new RuntimeException("Ya existe un empleado registrado con email: " + email);
		}
	}

	/**
	 * Validar estado inicial (debe ser Activo = 1)
	 */
	public void validarEstadoInicial(String idEstadoEmpleado) {
		if (!"1".equals(idEstadoEmpleado)) {
			throw new RuntimeException("El estado inicial del empleado debe ser 'Activo' (1)");
		}
	}

	/**
	 * Verificar si un empleado existe (retorna boolean)
	 */
	public boolean existeEmpleado(int idEmpleado) {
		String sql = "SELECT COUNT(1) FROM EMPLEADO WHERE id_empleado = ?";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, idEmpleado);
		return (cont == 1);
	}

	/**
	 * Cambiar estado de empleado
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public void cambiarEstadoEmpleado(int idEmpleado, String nuevoEstado) {
		this.validarEmpleadoExiste(idEmpleado);

		String sql = "UPDATE EMPLEADO SET id_estado_empleado = ? WHERE id_empleado = ?";
		jdbcTemplate.update(sql, nuevoEstado, idEmpleado);
	}

}

