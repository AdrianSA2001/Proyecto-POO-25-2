package pe.edu.uni.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActividadProgramadaDto {

	@JsonProperty("id_actividad_programada")
	private int id_actividad_programada;

	@JsonProperty("id_actividad")
	private int id_actividad;

	@JsonProperty("id_parcela")
	private int id_parcela;

	@JsonProperty("id_empleado")
	private int id_empleado;

	@JsonProperty("fecha_programada")
	private String fecha_programada;

	@JsonProperty("id_estado_actividad")
	private int id_estado_actividad;

	@JsonProperty("periodicidad")
	private String periodicidad;  // Ejemplo: "semanal", "mensual", "unico"

	@JsonProperty("observaciones")
	private String observaciones;

}

