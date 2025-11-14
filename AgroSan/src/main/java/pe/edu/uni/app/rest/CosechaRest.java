package pe.edu.uni.app.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.uni.app.dto.CosechaDto;
import pe.edu.uni.app.dto.ErrorResponse;
import pe.edu.uni.app.service.CosechaService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agrosan/cosecha")
public class CosechaRest {

	@Autowired
	private CosechaService cosechaService;

	/**
	 * Registrar cosecha (RF4)
	 */
	@PostMapping("/registrar")
	public ResponseEntity<?> registrarCosecha(@RequestBody CosechaDto bean, HttpServletRequest request) {
		try {
			CosechaDto resultado = cosechaService.registrarCosecha(bean);
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

	/**
	 * Listar todas las cosechas
	 */
	@GetMapping("/listar")
	public ResponseEntity<?> listarCosechas(HttpServletRequest request) {
		try {
			List<Map<String, Object>> resultado = cosechaService.listarCosechas();
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
	 * Listar cosechas por parcela
	 */
	@GetMapping("/parcela/{idParcela}")
	public ResponseEntity<?> listarCosechasPorParcela(@PathVariable int idParcela, HttpServletRequest request) {
		try {
			List<Map<String, Object>> resultado = cosechaService.listarCosechasPorParcela(idParcela);
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

	/**
	 * Reporte de rendimiento por cultivo
	 */
	@GetMapping("/reporte/rendimiento")
	public ResponseEntity<?> reporteRendimientoPorCultivo(HttpServletRequest request) {
		try {
			List<Map<String, Object>> resultado = cosechaService.reporteRendimientoPorCultivo();
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
	 * Comparar siembra vs cosecha
	 */
	@GetMapping("/comparar/{idParcela}")
	public ResponseEntity<?> compararSiembraVsCosecha(@PathVariable int idParcela, HttpServletRequest request) {
		try {
			List<Map<String, Object>> resultado = cosechaService.compararSiembraVsCosecha(idParcela);
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

