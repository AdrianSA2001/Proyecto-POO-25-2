package pe.edu.uni.app.rest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;
import pe.edu.uni.app.dto.ActividadProgramadaDto;
import pe.edu.uni.app.dto.ErrorResponse;
import pe.edu.uni.app.service.ActividadService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
@RestController
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowedHeaders = "*")
@RequestMapping("/agrosan/actividad")
public class ActividadRest{
	@Autowired
	private ActividadService actividadService;

	//PROGRAMAR UNA ACTIVIDAD
	@PostMapping("/programar")
	public ResponseEntity<?> programarActividad(@RequestBody ActividadProgramadaDto bean, HttpServletRequest request){
		try {
			actividadService.validarFecha(bean.getFecha_programada(), "yyyy-MM-dd");
			ActividadProgramadaDto resultado = actividadService.programarActividad(bean);
			return ResponseEntity.ok(resultado);
		} catch (Exception e){
			ErrorResponse error = new ErrorResponse(
					e.getMessage(),
					LocalDateTime.now().toString(),
					request.getRequestURI()
			);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
		}
	}

	//MOSTRAR LAS ACTIVIDADES PENDIENTES
	@GetMapping("/pendientes")
	public ResponseEntity<?> listarActividadesPendientes(HttpServletRequest request){
		try {
			List<Map<String, Object>> resultado = actividadService.listarActividadesPendientes();
			if (resultado.isEmpty()){
	            ErrorResponse error = new ErrorResponse(
	                "Actualmente no hay actividades pendientes.",
	                LocalDateTime.now().toString(),
	                request.getRequestURI()
	            );
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	        }
			return ResponseEntity.ok(resultado);
		} catch (Exception e){
			ErrorResponse error = new ErrorResponse(
					e.getMessage(),
					LocalDateTime.now().toString(),
					request.getRequestURI()
			);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}
	}

	//MOSTRAR LAS ACTIVIDADES PROGRAMADAS PARA HOY
	@GetMapping("/hoy")
	public ResponseEntity<?> listarActividadesHoy(HttpServletRequest request){
		try {
			List<Map<String, Object>> resultado = actividadService.listarActividadesHoy();
			if (resultado.isEmpty()){
	            ErrorResponse error = new ErrorResponse(
	                "Actualmente no hay actividades programadas para hoy.",
	                LocalDateTime.now().toString(),
	                request.getRequestURI()
	            );
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	        }
			return ResponseEntity.ok(resultado);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse(
					e.getMessage(),
					LocalDateTime.now().toString(),
					request.getRequestURI()
			);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}
	}

	//MOSTRAR LAS ACTIVIDADES PROGRAMADAS EN UNA PARCELA
	@GetMapping("/parcela/{idParcela}")
	public ResponseEntity<?> listarActividadesPorParcela(@PathVariable int idParcela, HttpServletRequest request){
		try {
			List<Map<String, Object>> resultado = actividadService.listarActividadesPorParcela(idParcela);
			return ResponseEntity.ok(resultado);
		} catch (Exception e){
			ErrorResponse error = new ErrorResponse(
					e.getMessage(),
					LocalDateTime.now().toString(),
					request.getRequestURI()
			);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
		}
	}

	//COMPLETAR UNA ACTIVIDAD
	@PutMapping("/completar/{id}")
	public ResponseEntity<?> completarActividad(@PathVariable int id, HttpServletRequest request){
		try {
			actividadService.completarActividad(id);
			return ResponseEntity.ok(Map.of("mensaje", "Actividad completada exitosamente"));
		} catch (Exception e){
			ErrorResponse error = new ErrorResponse(
					e.getMessage(),
					LocalDateTime.now().toString(),
					request.getRequestURI()
			);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
		}
	}

	//INICIAR UNA ACTIVIDAD
	@PutMapping("/iniciar/{id}")
	public ResponseEntity<?> iniciarActividad(@PathVariable int id, HttpServletRequest request){
		try {
			actividadService.iniciarActividad(id);
			return ResponseEntity.ok(Map.of("mensaje", "Actividad iniciada exitosamente"));
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse(
					e.getMessage(),
					LocalDateTime.now().toString(),
					request.getRequestURI()
			);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
		}
	}

	/**
	 * Listar tipos de actividades
	 */
	@GetMapping("/tipos")
	public ResponseEntity<?> listarTiposActividades(HttpServletRequest request) {
		try {
			List<Map<String, Object>> resultado = actividadService.listarTiposActividades();
			return ResponseEntity.ok(resultado);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse(
					e.getMessage(),
					LocalDateTime.now().toString(),
					request.getRequestURI()
			);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}
	}

	/**
	 * Reporte de actividades por empleado
	 */
	@GetMapping("/reporte/empleado/{idEmpleado}")
	public ResponseEntity<?> reporteActividadesPorEmpleado(@PathVariable int idEmpleado, HttpServletRequest request) {
		try {
			List<Map<String, Object>> resultado = actividadService.reporteActividadesPorEmpleado(idEmpleado);
			return ResponseEntity.ok(resultado);
		} catch (Exception e) {
			ErrorResponse error = new ErrorResponse(
					e.getMessage(),
					LocalDateTime.now().toString(),
					request.getRequestURI()
			);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
		}
	}

}

