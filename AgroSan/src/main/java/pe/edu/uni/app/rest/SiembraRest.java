package pe.edu.uni.app.rest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.uni.app.dto.ErrorResponse;
import pe.edu.uni.app.dto.SiembraDto;
import pe.edu.uni.app.service.SiembraService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/agrosan/siembra")
public class SiembraRest{
	@Autowired
	private SiembraService siembraService;
	
	//REGISTRAR UNA NUEVA SIEMBRA EN LA BD
	@PostMapping("/registrar")
	public ResponseEntity<?> registrarSiembra(@RequestBody SiembraDto bean, HttpServletRequest request){
		try {
			SiembraDto resultado = siembraService.registrarSiembra(bean);
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

	//MOSTRAR TODAS LAS SIEMBRAS
	@GetMapping("/listar")
	public ResponseEntity<?> listarSiembras(HttpServletRequest request){
		try {
			List<Map<String, Object>> resultado = siembraService.listarSiembras();
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
	
	//MOSTRAR LAS SIEMBRAS HECHAS EN UNA PARCELA
	@GetMapping("/parcela/{idParcela}")
	public ResponseEntity<?> listarSiembrasPorParcela(@PathVariable int idParcela, HttpServletRequest request){
		try {
			List<Map<String, Object>> resultado = siembraService.listarSiembrasPorParcela(idParcela);
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
}