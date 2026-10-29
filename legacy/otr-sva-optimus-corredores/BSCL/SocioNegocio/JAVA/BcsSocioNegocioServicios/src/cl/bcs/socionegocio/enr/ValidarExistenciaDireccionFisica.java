package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.DireccionFisica;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ValidarExistenciaDireccionFisica implements JavaCall {
    static final Logger logger = LogManager.getLogger(ValidarExistenciaDireccionFisica.class);

    private final String SQL_SELECT_DIRECCION_FISICA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ValidarExistenciaDireccionFisica_SelectDireccionFisica.sql");

    public static class EntradaValidarExistenciaBorradorDireccionFisica extends BcsPojo {
        public Long p_IdEmpresa;
        public List<DireccionFisica> p_DireccionesFisicas;

        public EntradaValidarExistenciaBorradorDireccionFisica() {
            this.p_DireccionesFisicas = new ArrayList<>();
        }
    }

    public static class SalidaValidarExistenciaBorradorDireccionFisica extends BcsPojo {
        public List<Validacion> validaciones;

        public SalidaValidarExistenciaBorradorDireccionFisica() {
            this.validaciones = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    public SalidaValidarExistenciaBorradorDireccionFisica logica(Connection con, EntradaValidarExistenciaBorradorDireccionFisica entrada) throws Exception {
        logger.info("INICIO::EntradaValidarExistenciaBorradorDireccionFisica");
        long inicio = System.currentTimeMillis();

        SalidaValidarExistenciaBorradorDireccionFisica out = new SalidaValidarExistenciaBorradorDireccionFisica();
        out.validaciones = validarExistenciaDireccionesFisicas(con, entrada.p_DireccionesFisicas, entrada.p_IdEmpresa);

        long fin = System.currentTimeMillis();
        logger.info("FIN::EntradaValidarExistenciaBorradorDireccionFisica", fin - inicio);

        return out;
    }

	private List<Validacion> validarExistenciaDireccionesFisicas(Connection con, List<DireccionFisica> direccionesFisicas, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ValidarExistenciaDireccionFisica.validarExistenciaDireccionesFisicas");
		long inicio = System.currentTimeMillis();
		
		List<Long> idDireccionesFisicas = direccionesFisicas.parallelStream().map(direccionEletronica->{
			return direccionEletronica.IdDireccionFisica;
		}).collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdDireccionFisica", idDireccionesFisicas);
		
		SQL sql = new SQL();
		List<DireccionFisica> direccionesFisicasExistentes = sql.query(con, SQL_SELECT_DIRECCION_FISICA, parametros, DireccionFisica.class);
		Map<Long, DireccionFisica> direccionesFisicasPorIdDireccion = direccionesFisicasExistentes.parallelStream().collect(Collectors.toMap(direccionFisica->direccionFisica.IdDireccionFisica, direccionFisica->direccionFisica));
		
		List<Validacion> validaciones = new ArrayList<Validacion>();
		
		for(DireccionFisica direccionFisica : direccionesFisicas) {
			if(direccionesFisicasPorIdDireccion.get(direccionFisica.IdDireccionFisica) == null) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				HashMap<String,String> datosMensajes = new HashMap<String,String>(1);
				datosMensajes.put("Direccion", direccionFisica.Calle + " #"+ direccionFisica.Numero + ", "+ direccionFisica.Sector);
				validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BORRADOR_DIRECCION_ELECTRONICA_NO_EXISTE", datosMensajes, con, idEmpresa);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarExistenciaDireccionFisica.validarExistenciaDireccionesFisicas", fin - inicio);
		return validaciones;
	}
	
	
}