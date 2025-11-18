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
public class EmpleadoService{
	@Autowired
	private JdbcTemplate jdbcTemplate;

	//REGISTRAR A UN NUEVO EMPLEADO EN LA BD
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public EmpleadoDto registrarEmpleado(EmpleadoDto bean){
		//VARIABLES
		String sql;
		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		//VALIDACIONES
		this.validarDni(bean.getDni());
		this.validarDniUnico(bean.getDni());
		this.validarTelefono(bean.getTelefono());
		this.validarEmail(bean.getEmail());
		if (bean.getEmail() != null && !bean.getEmail().isEmpty()) {
			this.validarEmailUnico(bean.getEmail());
		}
		this.validarEstadoInicial(bean.getId_estado_empleado());
		
		//PROCESO
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
		int idGenerado = keyHolder.getKey().intValue();
		bean.setId_empleado(idGenerado);
		return bean;
	}

	//LISTAR EMPLEADO POR SU ID
	public EmpleadoDto obtenerEmpleado(int idEmpleado){
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
	
	//LISTAR A TODOS LOS EMPLEADOS
	public List<Map<String, Object>> listarEmpleados(){
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
	
	//LISTAR A LOS EMPLEADOS ACTIVOS
	public List<Map<String, Object>> listarEmpleadosActivos(){
		String sql = """
				SELECT 
					e.id_empleado, e.nombre, e.apellido, e.telefono, e.email
				FROM EMPLEADO e
				WHERE e.id_estado_empleado = '1'
				ORDER BY e.apellido, e.nombre
				""";
		return jdbcTemplate.queryForList(sql);
	}
	
	//AUTENTICAR EMPLEADO (LOGIN)
	public Map<String, Object> autenticar(String dni, String contrasena){
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

	//VALIDAR QUE EL EMPLEADO EXISTE EN LA BD
	public void validarEmpleadoExiste(int idEmpleado){
		String sql = "SELECT COUNT(1) FROM EMPLEADO WHERE id_empleado = ?";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, idEmpleado);
		if (cont == 0) {
			throw new RuntimeException("No existe empleado con id = " + idEmpleado);
		}
	}

	//VALIDAR QUE EL EMPLEADO ESTA ACTIVO
	public void validarEmpleadoActivo(int idEmpleado){
		this.validarEmpleadoExiste(idEmpleado);
		String sql = "SELECT id_estado_empleado FROM EMPLEADO WHERE id_empleado = ?";
		String estado = jdbcTemplate.queryForObject(sql, String.class, idEmpleado);
		if (!"1".equals(estado)) {
			throw new RuntimeException("El empleado no está activo (estado actual: " + estado + ")");
		}
	}
	
	//VALIDAR QUE EL DNI SEA ÚNICO
	public void validarDniUnico(String dni){
		String sql = "SELECT COUNT(1) FROM EMPLEADO WHERE dni = ?";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, dni);
		if (cont > 0) {
			throw new RuntimeException("Ya existe un empleado registrado con DNI: " + dni);
		}
	}

	//VALIDAR QUE EL EMAIL SEA ÚNICO
	public void validarEmailUnico(String email){
		String sql = "SELECT COUNT(1) FROM EMPLEADO WHERE email = ?";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, email);
		if (cont > 0) {
			throw new RuntimeException("Ya existe un empleado registrado con email: " + email);
		}
	}
	
	//VALIDAR QUE EL ESTADO INICIAL SEA ACTIVO
	public void validarEstadoInicial(String idEstadoEmpleado){
		if (!"1".equals(idEstadoEmpleado)) {
			throw new RuntimeException("El estado inicial del empleado debe ser 'Activo' (1)");
		}
	}

	//VALIDAR QUE EL EMPLEADO EXISTE (RETORNA UN BOOLEANO)
	public boolean existeEmpleado(int idEmpleado){
		String sql = "SELECT COUNT(1) FROM EMPLEADO WHERE id_empleado = ?";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, idEmpleado);
		return (cont == 1);
	}
	
	//VALIDAR QUE EL DNI TIENE 8 DIGITOS
	public void validarDni(String dni){
	    if (dni == null || !dni.matches("\\d{8}")){
	        throw new IllegalArgumentException("El DNI debe ser una cadena de 8 dígitos numéricos.");
	    }
	}
	
	//VALIDAR QUE EL EMAIL TIENE EL FORMATO CORRECTO
	public void validarEmail(String email){
	    if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")){
	        throw new IllegalArgumentException("Formato de email no válido.");
	    }
	}
	
	//VALIDAR QUE EL TELEFONO TIENE 9 DIGITOS
	public void validarTelefono(String telefono){
	    if (telefono == null || !telefono.matches("\\d{9}")) {
	        throw new IllegalArgumentException("El teléfono debe tener exactamente 9 dígitos numéricos.");
	    }
	}

	//CAMBIAR EL ESTADO DE UN EMPLEADO
	@Transactional(propagation = Propagation.MANDATORY)
	public void cambiarEstadoEmpleado(int idEmpleado, String nuevoEstado) {
		this.validarEmpleadoExiste(idEmpleado);

		String sql = "UPDATE EMPLEADO SET id_estado_empleado = ? WHERE id_empleado = ?";
		jdbcTemplate.update(sql, nuevoEstado, idEmpleado);
	}
}