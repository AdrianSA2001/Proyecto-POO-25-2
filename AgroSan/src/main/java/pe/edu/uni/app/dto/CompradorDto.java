package pe.edu.uni.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompradorDto {

	@JsonProperty("id_comprador")
	private int id_comprador;

	@JsonProperty("nombre")
	private String nombre;

	@JsonProperty("telefono")
	private String telefono;

	@JsonProperty("email")
	private String email;

	@JsonProperty("direccion")
	private String direccion;

}

