package pe.edu.uni.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlertaService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * Obtener todas las alertas activas (RF3)
	 */
	public List<Map<String, Object>> obtenerAlertasActivas() {
		List<Map<String, Object>> alertas = new ArrayList<>();

		// 1. Alertas de cosecha próxima
		alertas.addAll(this.alertasCosechaProxima());

		// 2. Alertas de actividades pendientes para hoy
		alertas.addAll(this.alertasActividadesHoy());

		// 3. Alertas de stock bajo de semillas
		alertas.addAll(this.alertasStockBajoSemillas());

		// 4. Alertas de stock bajo de insumos
		alertas.addAll(this.alertasStockBajoInsumos());

		return alertas;
	}

	/**
	 * Alertas de cosecha próxima (1 semana antes)
	 */
	public List<Map<String, Object>> alertasCosechaProxima() {
		// Calcular fecha límite (7 días desde hoy)
		LocalDate fechaHoy = LocalDate.now();
		LocalDate fechaLimite = fechaHoy.plusDays(7);

		String sql = """
				SELECT 
					hs.id_siembra,
					tc.nombre tipo_cultivo,
					p.ubicacion parcela,
					hs.fecha_siembra,
					DATEADD(DAY, 
						CASE tc.tipo 
							WHEN 'Hortaliza' THEN 90
							WHEN 'Fruta' THEN 120
							WHEN 'Cereal' THEN 150
							ELSE 100
						END, 
						hs.fecha_siembra
					) fecha_estimada_cosecha,
					DATEDIFF(DAY, GETDATE(), 
						DATEADD(DAY, 
							CASE tc.tipo 
								WHEN 'Hortaliza' THEN 90
								WHEN 'Fruta' THEN 120
								WHEN 'Cereal' THEN 150
								ELSE 100
							END, 
							hs.fecha_siembra
						)
					) dias_faltantes
				FROM HISTORIAL_SIEMBRA hs
				JOIN TIPO_CULTIVO tc ON hs.id_tipo_cultivo = tc.id_tipo_cultivo
				JOIN PARCELA p ON hs.id_parcela = p.id_parcela
				WHERE NOT EXISTS (
					SELECT 1 FROM HISTORIAL_COSECHA hc 
					WHERE hc.id_parcela = hs.id_parcela 
					AND hc.id_tipo_cultivo = hs.id_tipo_cultivo
					AND hc.fecha_cosecha >= hs.fecha_siembra
				)
				AND DATEADD(DAY, 
					CASE tc.tipo 
						WHEN 'Hortaliza' THEN 90
						WHEN 'Fruta' THEN 120
						WHEN 'Cereal' THEN 150
						ELSE 100
					END, 
					hs.fecha_siembra
				) BETWEEN GETDATE() AND DATEADD(DAY, 7, GETDATE())
				""";

		List<Map<String, Object>> siembras = jdbcTemplate.queryForList(sql);
		List<Map<String, Object>> alertas = new ArrayList<>();

		for (Map<String, Object> siembra : siembras) {
			Map<String, Object> alerta = new HashMap<>();
			alerta.put("tipo", "COSECHA_PROXIMA");
			alerta.put("prioridad", "ALTA");
			alerta.put("mensaje", 
				"Cosecha próxima de " + siembra.get("tipo_cultivo") + 
				" en parcela " + siembra.get("parcela") + 
				" (en " + siembra.get("dias_faltantes") + " días)"
			);
			alerta.put("fecha_estimada", siembra.get("fecha_estimada_cosecha"));
			alerta.put("datos", siembra);
			alertas.add(alerta);
		}

		return alertas;
	}

	/**
	 * Alertas de actividades programadas para hoy
	 */
	public List<Map<String, Object>> alertasActividadesHoy() {
		String sql = """
				SELECT 
					ap.id_actividad_programada,
					a.nombre actividad,
					p.ubicacion parcela,
					CONCAT(e.nombre, ' ', e.apellido) empleado
				FROM ACTIVIDAD_PROGRAMADA ap
				JOIN ACTIVIDAD a ON ap.id_actividad = a.id_actividad
				JOIN PARCELA p ON ap.id_parcela = p.id_parcela
				JOIN EMPLEADO e ON ap.id_empleado = e.id_empleado
				WHERE ap.fecha_programada = CAST(GETDATE() AS DATE)
				AND ap.id_estado_actividad = 1
				""";

		List<Map<String, Object>> actividades = jdbcTemplate.queryForList(sql);
		List<Map<String, Object>> alertas = new ArrayList<>();

		for (Map<String, Object> actividad : actividades) {
			Map<String, Object> alerta = new HashMap<>();
			alerta.put("tipo", "ACTIVIDAD_HOY");
			alerta.put("prioridad", "MEDIA");
			alerta.put("mensaje", 
				"Actividad pendiente HOY: " + actividad.get("actividad") + 
				" en parcela " + actividad.get("parcela") + 
				" (Responsable: " + actividad.get("empleado") + ")"
			);
			alerta.put("datos", actividad);
			alertas.add(alerta);
		}

		return alertas;
	}

	/**
	 * Alertas de actividades atrasadas
	 */
	public List<Map<String, Object>> alertasActividadesAtrasadas() {
		String sql = """
				SELECT 
					ap.id_actividad_programada,
					a.nombre actividad,
					p.ubicacion parcela,
					ap.fecha_programada,
					CONCAT(e.nombre, ' ', e.apellido) empleado,
					DATEDIFF(DAY, ap.fecha_programada, GETDATE()) dias_atrasado
				FROM ACTIVIDAD_PROGRAMADA ap
				JOIN ACTIVIDAD a ON ap.id_actividad = a.id_actividad
				JOIN PARCELA p ON ap.id_parcela = p.id_parcela
				JOIN EMPLEADO e ON ap.id_empleado = e.id_empleado
				WHERE ap.fecha_programada < CAST(GETDATE() AS DATE)
				AND ap.id_estado_actividad = 1
				""";

		List<Map<String, Object>> actividades = jdbcTemplate.queryForList(sql);
		List<Map<String, Object>> alertas = new ArrayList<>();

		for (Map<String, Object> actividad : actividades) {
			Map<String, Object> alerta = new HashMap<>();
			alerta.put("tipo", "ACTIVIDAD_ATRASADA");
			alerta.put("prioridad", "ALTA");
			alerta.put("mensaje", 
				"Actividad ATRASADA: " + actividad.get("actividad") + 
				" en parcela " + actividad.get("parcela") + 
				" (" + actividad.get("dias_atrasado") + " días de atraso)"
			);
			alerta.put("datos", actividad);
			alertas.add(alerta);
		}

		return alertas;
	}

	/**
	 * Alertas de stock bajo de semillas
	 */
	public List<Map<String, Object>> alertasStockBajoSemillas() {
		String sql = """
				SELECT 
					tc.nombre tipo_cultivo,
					ss.cantidad_disponible stock_actual,
					ss.fecha_actualizacion
				FROM STOCK_SEMILLAS ss
				JOIN TIPO_CULTIVO tc ON ss.id_tipo_cultivo = tc.id_tipo_cultivo
				WHERE ss.cantidad_disponible < 50
				""";

		List<Map<String, Object>> stocks = jdbcTemplate.queryForList(sql);
		List<Map<String, Object>> alertas = new ArrayList<>();

		for (Map<String, Object> stock : stocks) {
			Map<String, Object> alerta = new HashMap<>();
			alerta.put("tipo", "STOCK_BAJO_SEMILLAS");
			alerta.put("prioridad", "MEDIA");
			alerta.put("mensaje", 
				"Stock bajo de semillas: " + stock.get("tipo_cultivo") + 
				" (Stock actual: " + stock.get("stock_actual") + " kg)"
			);
			alerta.put("datos", stock);
			alertas.add(alerta);
		}

		return alertas;
	}

	/**
	 * Alertas de stock bajo de insumos
	 */
	public List<Map<String, Object>> alertasStockBajoInsumos() {
		String sql = """
				SELECT 
					nombre,
					stock_actual,
					stock_minimo,
					unidad_medida
				FROM INSUMO
				WHERE stock_actual <= stock_minimo
				""";

		List<Map<String, Object>> insumos = jdbcTemplate.queryForList(sql);
		List<Map<String, Object>> alertas = new ArrayList<>();

		for (Map<String, Object> insumo : insumos) {
			Map<String, Object> alerta = new HashMap<>();
			alerta.put("tipo", "STOCK_BAJO_INSUMO");
			alerta.put("prioridad", "ALTA");
			alerta.put("mensaje", 
				"Stock bajo de insumo: " + insumo.get("nombre") + 
				" (Stock actual: " + insumo.get("stock_actual") + " " + 
				insumo.get("unidad_medida") + ", Mínimo: " + 
				insumo.get("stock_minimo") + ")"
			);
			alerta.put("datos", insumo);
			alertas.add(alerta);
		}

		return alertas;
	}

	/**
	 * Generar resumen de alertas por tipo
	 */
	public Map<String, Object> resumenAlertas() {
		Map<String, Object> resumen = new HashMap<>();

		List<Map<String, Object>> todasAlertas = this.obtenerAlertasActivas();

		long alertasAlta = todasAlertas.stream()
				.filter(a -> "ALTA".equals(a.get("prioridad")))
				.count();

		long alertasMedia = todasAlertas.stream()
				.filter(a -> "MEDIA".equals(a.get("prioridad")))
				.count();

		long alertasBaja = todasAlertas.stream()
				.filter(a -> "BAJA".equals(a.get("prioridad")))
				.count();

		resumen.put("total_alertas", todasAlertas.size());
		resumen.put("alertas_alta_prioridad", alertasAlta);
		resumen.put("alertas_media_prioridad", alertasMedia);
		resumen.put("alertas_baja_prioridad", alertasBaja);
		resumen.put("alertas", todasAlertas);

		return resumen;
	}

	/**
	 * Obtener alertas por prioridad
	 */
	public List<Map<String, Object>> obtenerAlertasPorPrioridad(String prioridad) {
		List<Map<String, Object>> todasAlertas = this.obtenerAlertasActivas();
		
		return todasAlertas.stream()
				.filter(a -> prioridad.equalsIgnoreCase((String) a.get("prioridad")))
				.toList();
	}

}

