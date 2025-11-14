package pe.edu.uni.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
@NoArgsConstructor
public class SiembraDto {
	
	@JsonProperty("id_siembra")
	private int id_siembra;
	
	@JsonProperty("id_tipo_cultivo")
	private int id_tipo_cultivo;
	
	@JsonProperty("id_parcela")
	private int id_parcela;
	
	@JsonProperty("id_empleado")
	private int id_empleado;
	
	@JsonProperty("fecha_siembra")
	private String fecha_siembra;
	
	@JsonProperty("cantidad_sembrada")
	private double cantidad_sembrada;
	
	@JsonProperty("fecha_estimada_cosecha")
	private String fecha_estimada_cosecha;
	
}
