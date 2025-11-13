package pe.edu.uni.app.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
@Data @AllArgsConstructor @NoArgsConstructor

public class VentaDto {
	@JsonProperty("id_comprador")
	int id_comprador;
	
	@JsonProperty("nombre")
	String nombre;
	
	@JsonProperty("telefono")
	String telefono;
	
	@JsonProperty("direccion")
	String direccion;
	
	@JsonProperty("id_venta")
	int id_venta;
	
	@JsonProperty("fecha_venta")
	int fecha_venta;
	
	@JsonProperty("cantidad_vendida")
	int cantidad_vendida;
	
	@JsonProperty("precio_unitario")
	double precio_unitario;
}
