package pe.edu.uni.app.rest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;
import pe.edu.uni.app.dto.ErrorResponse;
import pe.edu.uni.app.dto.ParcelaDto;
import pe.edu.uni.app.service.ParcelaService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
@RestController
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
@RequestMapping("/agrosan/parcela")
public class ParcelaRest{
	@Autowired
	private ParcelaService parcelaService;

	//REGISTRAR UNA NUEVA PARCELA
	@PostMapping("/registrar")
	public ResponseEntity<?> registrarParcela(@RequestBody ParcelaDto bean, HttpServletRequest request){
		try {
			ParcelaDto resultado = parcelaService.registrarParcela(bean);
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

	//OBTENER UNA PARCELA POR SU ID
	@GetMapping("/obtener/{id}")
	public ResponseEntity<?> obtenerParcela(@PathVariable int id, HttpServletRequest request){
		try {
			ParcelaDto resultado = parcelaService.obtenerParcela(id);
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

	//MOSTRAR TODAS LAS PARCELAS
	@GetMapping("/listar")
	public ResponseEntity<?> listarParcelas(HttpServletRequest request){
		try {
			List<Map<String, Object>> resultado = parcelaService.listarParcelas();
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

	//MOSTRAR TODAS LAS PARCELAS LISTAS (ACTIVAS) PARA LA SIEMBRA
	@GetMapping("/disponibles")
	public ResponseEntity<?> listarParcelasDisponibles(HttpServletRequest request){
		try {
			List<Map<String, Object>> resultado = parcelaService.listarParcelasDisponibles();
			if (resultado.isEmpty()) {
	            ErrorResponse error = new ErrorResponse(
	                "Actualmente no hay parcelas activas en el sistema.",
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
	
	//MOSTRAR TODAS LAS PARCELAS NO LISTAS (INACTIVAS) PARA LA SIEMBRA
		@GetMapping("/nodisponibles")
		public ResponseEntity<?> listarParcelasNoDisponibles(HttpServletRequest request){
			try {
				List<Map<String, Object>> resultado = parcelaService.listarParcelasNoDisponibles();
				if (resultado.isEmpty()) {
		            ErrorResponse error = new ErrorResponse(
		                "Actualmente no hay parcelas inactivas en el sistema.",
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
}