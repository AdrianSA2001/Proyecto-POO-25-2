package pe.edu.uni.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CosechaDto {

	@JsonProperty("id_cosecha")
	private int id_cosecha;

	@JsonProperty("id_tipo_cultivo")
	private int id_tipo_cultivo;

	@JsonProperty("id_parcela")
	private int id_parcela;

	@JsonProperty("id_empleado")
	private int id_empleado;

	@JsonProperty("fecha_cosecha")
	private String fecha_cosecha;

	@JsonProperty("cantidad_cosechada")
	private double cantidad_cosechada;

	@JsonProperty("cantidad_estimada")
	private double cantidad_estimada;

	@JsonProperty("rendimiento")
	private double rendimiento;  // Porcentaje de rendimiento real vs estimado

}

