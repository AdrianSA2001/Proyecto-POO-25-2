package pe.edu.uni.app.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.uni.app.dto.SiembraDto;
@Service
public class SiembraService{
	@Autowired
	JdbcTemplate jdbctemplate;
	@Transactional(propagation = Propagation.MANDATORY)
	public void validarParcela(int id_parcela){
		String sql = """
				SELECT COUNT(id_parcela) FROM PARCELA WHERE id_parcela = ?
				""";
		int cont = jdbctemplate.queryForObject(sql, Integer.class, id_parcela);
		if (cont == 0){
			throw new RuntimeException("No existe parcela con id = " + id_parcela);
		}
	}
	@Transactional(propagation = Propagation.MANDATORY)
	public void validarParcelaActiva(int id_parcela){
		String sql = """
				SELECT id_estado_parcela FROM PARCELA WHERE id_parcela = ?
				""";
		int estado = jdbctemplate.queryForObject(sql, Integer.class, id_parcela);
		if (estado != 3){
			throw new RuntimeException("No se puede sembrar en esta parcela.");
		}
	}
}