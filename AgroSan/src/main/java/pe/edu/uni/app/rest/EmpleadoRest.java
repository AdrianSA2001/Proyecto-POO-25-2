package pe.edu.uni.app.rest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.uni.app.dto.EmpleadoDto;
import pe.edu.uni.app.dto.ErrorResponse;
import pe.edu.uni.app.service.EmpleadoService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/agrosan/empleado")
public class EmpleadoRest{
	@Autowired
	private EmpleadoService empleadoService;
	
	//REGISTRAR A UN NUEVO EMPLEADO
	@PostMapping("/registrar")
	public ResponseEntity<?> registrarEmpleado(@RequestBody EmpleadoDto bean, HttpServletRequest request){
		try {
			EmpleadoDto resultado = empleadoService.registrarEmpleado(bean);
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
	
	//MOSTRAR LOS DATOS DE UN EMPLEADO FILTRADO POR SU ID
	@GetMapping("/obtener/{id}")
	public ResponseEntity<?> obtenerEmpleado(@PathVariable int id, HttpServletRequest request){
		try {
			EmpleadoDto resultado = empleadoService.obtenerEmpleado(id);
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
	
	//LISTAR A TODOS LOS EMPLEADOS
	@GetMapping("/listar")
	public ResponseEntity<?> listarEmpleados(HttpServletRequest request){
		try {
			List<Map<String, Object>> resultado = empleadoService.listarEmpleados();
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
	
	//LISTAR A LOS EMPLEADOS ACTIVOS
	@GetMapping("/activos")
	public ResponseEntity<?> listarEmpleadosActivos(HttpServletRequest request){
		try {
			List<Map<String, Object>> resultado = empleadoService.listarEmpleadosActivos();
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

	/**
	 * Autenticar empleado (login)
	 */
	@PostMapping("/autenticar")
	public ResponseEntity<?> autenticar(@RequestBody Map<String, String> credenciales, HttpServletRequest request){
		try {
			String dni = credenciales.get("dni");
			String contrasena = credenciales.get("contrasena");
			
			Map<String, Object> resultado = empleadoService.autenticar(dni, contrasena);
			return ResponseEntity.ok(resultado);
		} catch (Exception e){
			ErrorResponse error = new ErrorResponse(
					e.getMessage(),
					LocalDateTime.now().toString(),
					request.getRequestURI()
			);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
		}
	}
}