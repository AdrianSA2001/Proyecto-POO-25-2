package pe.edu.uni.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
@NoArgsConstructor
public class VentaDto {

	@JsonProperty("id_venta")
	private int id_venta;

	@JsonProperty("id_comprador")
	private int id_comprador;

	@JsonProperty("id_tipo_cultivo")
	private int id_tipo_cultivo;

	@JsonProperty("fecha_venta")
	private String fecha_venta;

	@JsonProperty("cantidad_vendida")
	private double cantidad_vendida;

	@JsonProperty("precio_unitario")
	private double precio_unitario;

	@JsonProperty("precio_total")
	private double precio_total;

}

