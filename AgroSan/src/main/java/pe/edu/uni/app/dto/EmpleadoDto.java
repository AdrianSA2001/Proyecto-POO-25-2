package pe.edu.uni.app.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
@Data @AllArgsConstructor @NoArgsConstructor
public class EmpleadoDto {
	@JsonProperty("id_empleado")
	int id_empleado;
	
	@JsonProperty("nombre")
	String nombre;
	
	@JsonProperty("apellido")
	String apellido;
	
	@JsonProperty("telefono")
	String telefono;
	
	@JsonProperty("email")
	String email;
	
	@JsonProperty("contrasena")
	String contrasena;
	
	@JsonProperty("id_estado_empleado")
	String id_estado_empleado;
	
	@JsonProperty("dni")
	String dni;
}
