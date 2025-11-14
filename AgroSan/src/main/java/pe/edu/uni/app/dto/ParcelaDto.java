package pe.edu.uni.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
@NoArgsConstructor
public class ParcelaDto {
	
	@JsonProperty("id_parcela")
	private int id_parcela;
	
	@JsonProperty("ubicacion")
	private String ubicacion;
	
	@JsonProperty("area")
	private double area;
	
	@JsonProperty("id_estado_parcela")
	private int id_estado_parcela;
	
}
