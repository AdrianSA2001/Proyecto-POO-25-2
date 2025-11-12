package pe.edu.uni.app.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.uni.app.dto.ParcelaDto;
@Service
public class ParcelaService{
	@Autowired
	JdbcTemplate jdbctemplate;
	@Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
	public void registrarParcela(ParcelaDto bean){
		//texto
	}
	
	public void validarArea(double area){
		if (area < 0.0){
			throw new RuntimeException("El área de la parcela debe ser positiva.");
		}
		if (area < 100.0) {
			throw new RuntimeException("El área para una parcela debe ser como mínimo 100.");
		}
	}
	
	public void validarEstadoInicial(int id_estado_parcela){
		if (id_estado_parcela != 1){
			throw new RuntimeException("El estado de la parcela no es el correcto para el registro, verifique.");
		}
	}
}