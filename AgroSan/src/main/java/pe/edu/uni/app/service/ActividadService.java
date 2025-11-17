package pe.edu.uni.app.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.uni.app.dto.ActividadProgramadaDto;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
@Service
public class ActividadService{
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ParcelaService parcelaService;

	@Autowired
	private EmpleadoService empleadoService;

	//PROGRAMAR UNA ACTIVIDAD Y REGISTRARLA EN LA BD
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public ActividadProgramadaDto programarActividad(ActividadProgramadaDto bean) {
		//VARIABLES
		String sql;
		KeyHolder keyHolder = new GeneratedKeyHolder();

		//VALIDACIONES
		this.validarActividadExiste(bean.getId_actividad());
		parcelaService.validarParcelaExiste(bean.getId_parcela());
		empleadoService.validarEmpleadoActivo(bean.getId_empleado());
		this.validarFechaProgramada(bean.getFecha_programada());
		this.validarEstadoInicial(bean.getId_estado_actividad());
		
		//PROCESO
		sql = """
				INSERT INTO ACTIVIDAD_PROGRAMADA 
				(id_actividad, id_parcela, id_empleado, fecha_programada, id_estado_actividad) 
				VALUES (?, ?, ?, ?, ?)
				""";
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_actividad_programada"});
			ps.setInt(1, bean.getId_actividad());
			ps.setInt(2, bean.getId_parcela());
			ps.setInt(3, bean.getId_empleado());
			ps.setDate(4, Date.valueOf(bean.getFecha_programada()));
			ps.setInt(5, bean.getId_estado_actividad());
			return ps;
		}, keyHolder);
		int idGenerado = keyHolder.getKey().intValue();
		bean.setId_actividad_programada(idGenerado);
		return bean;
	}

	//LISTAR ACTIVIDADES PROGRAMADAS QUE ESTAN PENDIENTES
	public List<Map<String, Object>> listarActividadesPendientes() {
		String sql = """
				SELECT 
					ap.id_actividad_programada, ap.fecha_programada,
					a.nombre actividad, a.descripcion descripcion_actividad,
					p.ubicacion parcela, p.area area_parcela,
					CONCAT(e.nombre, ' ', e.apellido) empleado,
					ea.descripcion estado
				FROM ACTIVIDAD_PROGRAMADA ap
				JOIN ACTIVIDAD a ON ap.id_actividad = a.id_actividad
				JOIN PARCELA p ON ap.id_parcela = p.id_parcela
				JOIN EMPLEADO e ON ap.id_empleado = e.id_empleado
				JOIN ESTADO_ACTIVIDAD ea ON ap.id_estado_actividad = ea.id_estado_actividad
				WHERE ap.id_estado_actividad = 1
				ORDER BY ap.fecha_programada ASC
				""";
		return jdbcTemplate.queryForList(sql);
	}

	//LISTAR ACTIVIDADES PROGRAMADAS PARA HOY
	public List<Map<String, Object>> listarActividadesHoy() {
		String sql = """
				SELECT 
					ap.id_actividad_programada, ap.fecha_programada,
					a.nombre actividad, a.descripcion descripcion_actividad,
					p.ubicacion parcela,
					CONCAT(e.nombre, ' ', e.apellido) empleado,
					ea.descripcion estado
				FROM ACTIVIDAD_PROGRAMADA ap
				JOIN ACTIVIDAD a ON ap.id_actividad = a.id_actividad
				JOIN PARCELA p ON ap.id_parcela = p.id_parcela
				JOIN EMPLEADO e ON ap.id_empleado = e.id_empleado
				JOIN ESTADO_ACTIVIDAD ea ON ap.id_estado_actividad = ea.id_estado_actividad
				WHERE ap.fecha_programada = CAST(GETDATE() AS DATE)
				ORDER BY a.nombre
				""";
		return jdbcTemplate.queryForList(sql);
	}

	//LISTAR LAS ACTIVIDADES PROGRAMADAS EN UNA PARCELA
	public List<Map<String, Object>> listarActividadesPorParcela(int idParcela) {
		parcelaService.validarParcelaExiste(idParcela);
		String sql = """
				SELECT 
					ap.id_actividad_programada, ap.fecha_programada,
					a.nombre actividad, a.descripcion descripcion_actividad,
					CONCAT(e.nombre, ' ', e.apellido) empleado,
					ea.descripcion estado
				FROM ACTIVIDAD_PROGRAMADA ap
				JOIN ACTIVIDAD a ON ap.id_actividad = a.id_actividad
				JOIN EMPLEADO e ON ap.id_empleado = e.id_empleado
				JOIN ESTADO_ACTIVIDAD ea ON ap.id_estado_actividad = ea.id_estado_actividad
				WHERE ap.id_parcela = ?
				ORDER BY ap.fecha_programada DESC
				""";
		return jdbcTemplate.queryForList(sql, idParcela);
	}

	//CAMBIAR EL ESTADO DE LA ACTIVIDAD A COMPLETADA
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void completarActividad(int idActividadProgramada){
		this.validarActividadProgramadaExiste(idActividadProgramada);
		String sql = """
				UPDATE ACTIVIDAD_PROGRAMADA 
				SET id_estado_actividad = 3 
				WHERE id_actividad_programada = ?
				""";
		jdbcTemplate.update(sql, idActividadProgramada);
	}

	//MOSTRAR LA ACTIVIDAD COMO EN "CURSO"
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void iniciarActividad(int idActividadProgramada){
		this.validarActividadProgramadaExiste(idActividadProgramada);
		String sql = """
				UPDATE ACTIVIDAD_PROGRAMADA 
				SET id_estado_actividad = 2 
				WHERE id_actividad_programada = ?
				""";
		jdbcTemplate.update(sql, idActividadProgramada);
	}

	//LISTAR LAS ACTIVIDADES MARCADAS COMO DISPONIBLES
	public List<Map<String, Object>> listarTiposActividades(){
		String sql = """
				SELECT 
					id_actividad, nombre, descripcion
				FROM ACTIVIDAD
				ORDER BY nombre
				""";
		return jdbcTemplate.queryForList(sql);
	}

	//LISTAR LAS ACTIVIDADES COMPLETADAS POR UN EMPLEADO
	public List<Map<String, Object>> reporteActividadesPorEmpleado(int idEmpleado) {
		empleadoService.validarEmpleadoExiste(idEmpleado);
		String sql = """
				SELECT 
					a.nombre actividad,
					COUNT(ap.id_actividad_programada) total_actividades,
					SUM(CASE WHEN ap.id_estado_actividad = 3 THEN 1 ELSE 0 END) completadas,
					SUM(CASE WHEN ap.id_estado_actividad = 1 THEN 1 ELSE 0 END) pendientes
				FROM ACTIVIDAD_PROGRAMADA ap
				JOIN ACTIVIDAD a ON ap.id_actividad = a.id_actividad
				WHERE ap.id_empleado = ?
				GROUP BY a.nombre
				ORDER BY total_actividades DESC
				""";
		return jdbcTemplate.queryForList(sql, idEmpleado);
	}

	//VALIDAR QUE LA ACTIVIDAD EXISTE EN LA BD
	public void validarActividadExiste(int idActividad) {
		String sql = "SELECT COUNT(1) FROM ACTIVIDAD WHERE id_actividad = ?";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, idActividad);
		if (cont == 0) {
			throw new RuntimeException("No existe actividad con id = " + idActividad);
		}
	}

	//VALIDAR QUE LA ACTIVIDAD ESTA PROGRAMADA
	public void validarActividadProgramadaExiste(int idActividadProgramada) {
		String sql = "SELECT COUNT(1) FROM ACTIVIDAD_PROGRAMADA WHERE id_actividad_programada = ?";
		int cont = jdbcTemplate.queryForObject(sql, Integer.class, idActividadProgramada);
		if (cont == 0) {
			throw new RuntimeException("No existe actividad programada con id = " + idActividadProgramada);
		}
	}

	//VALIDAR QUE LA FECHA PROGRAMADA ES CORRECTA
	public void validarFechaProgramada(String fechaProgramada) {
		LocalDate fecha = LocalDate.parse(fechaProgramada);
		LocalDate hoy = LocalDate.now();
		if (fecha.isBefore(hoy)) {
			throw new RuntimeException("La fecha programada no puede ser anterior a la fecha actual.");
		}
	}

	//VALIDAR EL ESTADO (PENDIENTE) AL PROGRAMAR UNA ACTIVIDAD
	public void validarEstadoInicial(int idEstadoActividad) {
		if (idEstadoActividad != 1) {
			throw new RuntimeException("El estado inicial de la actividad debe ser 'Pendiente' (1).");
		}
	}
}