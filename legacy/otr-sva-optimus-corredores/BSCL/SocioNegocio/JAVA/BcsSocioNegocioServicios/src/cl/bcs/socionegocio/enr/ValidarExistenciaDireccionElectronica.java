package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.DireccionElectronica;
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

public class ValidarExistenciaDireccionElectronica implements JavaCall {
    static final Logger logger = LogManager.getLogger(ValidarExistenciaDireccionElectronica.class);

    private final String SQL_SELECT_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ValidarExistenciaDireccionElectronica_SelectDireccionElectronica.sql");

    public static class EntradaValidarExistenciaBorradorDireccionElectronica extends BcsPojo {
        public Long p_IdEmpresa;
        public List<DireccionElectronica> p_DireccionesElectronicas;

        public EntradaValidarExistenciaBorradorDireccionElectronica() {
            this.p_DireccionesElectronicas = new ArrayList<>();
        }
    }

    public static class SalidaValidarExistenciaBorradorDireccionElectronica extends BcsPojo {
        public List<Validacion> validaciones;

        public SalidaValidarExistenciaBorradorDireccionElectronica() {
            this.validaciones = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    public SalidaValidarExistenciaBorradorDireccionElectronica logica(Connection con, EntradaValidarExistenciaBorradorDireccionElectronica entrada) throws Exception {
        logger.info("INICIO::EntradaValidarExistenciaBorradorDireccionElectronica");
        long inicio = System.currentTimeMillis();

        SalidaValidarExistenciaBorradorDireccionElectronica out = new SalidaValidarExistenciaBorradorDireccionElectronica();
        out.validaciones = validarExistenciaDireccionesElectronicas(con, entrada.p_DireccionesElectronicas, entrada.p_IdEmpresa);

        long fin = System.currentTimeMillis();
        logger.info("FIN::EntradaValidarExistenciaBorradorDireccionElectronica", fin - inicio);

        return out;
    }

	private List<Validacion> validarExistenciaDireccionesElectronicas(Connection con, List<DireccionElectronica> direccionesElectronicas, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ValidarExistenciaDireccionElectronica.validarExistenciaDireccionesElectronicas");
		long inicio = System.currentTimeMillis();
		
		List<Long> idDireccionesElectronicas = direccionesElectronicas.parallelStream().map(direccionEletronica->{
			return direccionEletronica.IdDireccionElectronica;
		}).collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdDireccionElectronica", idDireccionesElectronicas);
		
		SQL sql = new SQL();
		List<DireccionElectronica> direccionesElectronicasExistentes = sql.query(con, SQL_SELECT_DIRECCION_ELECTRONICA, parametros, DireccionElectronica.class);
		Map<Long, DireccionElectronica> direccionesElectronicasPorIdDireccion = direccionesElectronicasExistentes.parallelStream().collect(Collectors.toMap(direccionElectronica->direccionElectronica.IdDireccionElectronica, direccionElectronica->direccionElectronica));
		
		List<Validacion> validaciones = new ArrayList<Validacion>();
		
		for(DireccionElectronica direccionElectronica : direccionesElectronicas) {
			if(direccionesElectronicasPorIdDireccion.get(direccionElectronica.IdDireccionElectronica) == null) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				HashMap<String,String> datosMensajes = new HashMap<String,String>(1);
				datosMensajes.put("Direccion", direccionElectronica.Direccion);
				validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BORRADOR_DIRECCION_ELECTRONICA_NO_EXISTE", datosMensajes, con, idEmpresa);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarExistenciaDireccionElectronica.validarExistenciaDireccionesElectronicas", fin - inicio);
		return validaciones;
	}
}