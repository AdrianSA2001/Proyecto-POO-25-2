package pe.edu.uni.app.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.uni.app.dto.CompradorDto;
import pe.edu.uni.app.dto.ErrorResponse;
import pe.edu.uni.app.dto.VentaDto;
import pe.edu.uni.app.service.VentaService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agrosan/venta")
public class VentaRest {

	@Autowired
	private VentaService ventaService;

	/**
	 * Registrar venta (RF5)
	 */
	@PostMapping("/registrar")
	public ResponseEntity<?> registrarVenta(@RequestBody VentaDto bean, HttpServletRequest request) {
		try {
			VentaDto resultado = ventaService.registrarVenta(bean);
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
	 * Listar todas las ventas
	 */
	@GetMapping("/listar")
	public ResponseEntity<?> listarVentas(HttpServletRequest request) {
		try {
			List<Map<String, Object>> resultado = ventaService.listarVentas();
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
	 * Listar ventas por comprador
	 */
	@GetMapping("/comprador/{idComprador}")
	public ResponseEntity<?> listarVentasPorComprador(@PathVariable int idComprador, HttpServletRequest request) {
		try {
			List<Map<String, Object>> resultado = ventaService.listarVentasPorComprador(idComprador);
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
	 * Reporte de ventas por cultivo
	 */
	@GetMapping("/reporte/cultivo")
	public ResponseEntity<?> reporteVentasPorCultivo(HttpServletRequest request) {
		try {
			List<Map<String, Object>> resultado = ventaService.reporteVentasPorCultivo();
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
	 * Consultar stock disponible
	 */
	@GetMapping("/stock")
	public ResponseEntity<?> consultarStockDisponible(HttpServletRequest request) {
		try {
			List<Map<String, Object>> resultado = ventaService.consultarStockDisponible();
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
	 * Registrar nuevo comprador
	 */
	@PostMapping("/comprador/registrar")
	public ResponseEntity<?> registrarComprador(@RequestBody CompradorDto bean, HttpServletRequest request) {
		try {
			CompradorDto resultado = ventaService.registrarComprador(bean);
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
	 * Listar compradores
	 */
	@GetMapping("/comprador/listar")
	public ResponseEntity<?> listarCompradores(HttpServletRequest request) {
		try {
			List<Map<String, Object>> resultado = ventaService.listarCompradores();
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

}

