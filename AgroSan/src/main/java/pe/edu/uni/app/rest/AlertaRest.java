package pe.edu.uni.app.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;
import pe.edu.uni.app.dto.ErrorResponse;
import pe.edu.uni.app.service.AlertaService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
@RequestMapping("/agrosan/alerta")
public class AlertaRest {

	@Autowired
	private AlertaService alertaService;

	/**
	 * Obtener todas las alertas activas (RF3)
	 */
	@GetMapping("/activas")
	public ResponseEntity<?> obtenerAlertasActivas(HttpServletRequest request) {
		try {
			List<Map<String, Object>> resultado = alertaService.obtenerAlertasActivas();
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
	 * Obtener alertas de cosecha próxima
	 */
	@GetMapping("/cosecha-proxima")
	public ResponseEntity<?> alertasCosechaProxima(HttpServletRequest request) {
		try {
			List<Map<String, Object>> resultado = alertaService.alertasCosechaProxima();
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
	 * Obtener alertas de actividades de hoy
	 */
	@GetMapping("/actividades-hoy")
	public ResponseEntity<?> alertasActividadesHoy(HttpServletRequest request) {
		try {
			List<Map<String, Object>> resultado = alertaService.alertasActividadesHoy();
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
	 * Obtener alertas de actividades atrasadas
	 */
	@GetMapping("/actividades-atrasadas")
	public ResponseEntity<?> alertasActividadesAtrasadas(HttpServletRequest request) {
		try {
			List<Map<String, Object>> resultado = alertaService.alertasActividadesAtrasadas();
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
	 * Obtener alertas de stock bajo
	 */
	@GetMapping("/stock-bajo-semillas")
	public ResponseEntity<?> alertasStockBajoSemillas(HttpServletRequest request) {
		try {
			List<Map<String, Object>> resultado = alertaService.alertasStockBajoSemillas();
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
	 * Obtener resumen de alertas
	 */
	@GetMapping("/resumen")
	public ResponseEntity<?> resumenAlertas(HttpServletRequest request) {
		try {
			Map<String, Object> resultado = alertaService.resumenAlertas();
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
	 * Obtener alertas por prioridad
	 */
	@GetMapping("/prioridad/{prioridad}")
	public ResponseEntity<?> obtenerAlertasPorPrioridad(@PathVariable String prioridad, HttpServletRequest request) {
		try {
			List<Map<String, Object>> resultado = alertaService.obtenerAlertasPorPrioridad(prioridad);
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

