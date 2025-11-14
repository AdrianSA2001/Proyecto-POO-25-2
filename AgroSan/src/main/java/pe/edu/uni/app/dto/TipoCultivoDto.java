package pe.edu.uni.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TipoCultivoDto {

	@JsonProperty("id_tipo_cultivo")
	private int id_tipo_cultivo;

	@JsonProperty("nombre")
	private String nombre;

	@JsonProperty("tipo")
	private String tipo;  // Ejemplo: "Hortaliza", "Fruta", "Cereal"

}

