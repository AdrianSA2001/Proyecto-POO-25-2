package pe.edu.uni.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsumoDto {

	@JsonProperty("id_insumo")
	private int id_insumo;

	@JsonProperty("nombre")
	private String nombre;

	@JsonProperty("descripcion")
	private String descripcion;

	@JsonProperty("unidad_medida")
	private String unidad_medida;

	@JsonProperty("stock_actual")
	private double stock_actual;

	@JsonProperty("stock_minimo")
	private double stock_minimo;

}

