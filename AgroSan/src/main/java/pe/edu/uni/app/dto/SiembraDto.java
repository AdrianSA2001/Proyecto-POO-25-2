package pe.edu.uni.app.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
@Data @AllArgsConstructor @NoArgsConstructor
public class SiembraDto{
	@JsonProperty("id_parcela")
	int id_parcela;
	
	@JsonProperty("id_empleado")
	int id_empleado;
	
	@JsonProperty("id_estado_parcela")
	int id_estado_parcela;
	
	@JsonProperty("id_estado_empleado")
	int id_estado_empleado;
}