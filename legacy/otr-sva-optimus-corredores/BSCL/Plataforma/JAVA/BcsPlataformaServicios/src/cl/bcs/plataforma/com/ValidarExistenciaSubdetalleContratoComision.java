package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.com.pojo.SubdetalleContratoComision;
import cl.bcs.plataforma.com.pojo.Validacion;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ValidarExistenciaSubdetalleContratoComision implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ValidarExistenciaSubdetalleContratoComision.class);

	private final String SQL_SELECT_SUBDETALLE_CONTRATO_COMISION = loadResourceToString("cl/bcs/plataforma/com/sql/ValidarExistenciaSubdetalleContratoComision_SelectSubdetalleContratoComision.sql");
	
	public static class EntradaValidarExistenciaSubdetalleContratoComision extends BcsPojo {
	    public Long p_IdEmpresa;
	    public List<SubdetalleContratoComision> p_SubdetallesContratoComision;
	
	    public EntradaValidarExistenciaSubdetalleContratoComision() {
	        this.p_SubdetallesContratoComision = new ArrayList<SubdetalleContratoComision>();
	    }
	}

	public static class SalidaValidarExistenciaSubdetalleContratoComision extends BcsPojo {
	    public List<Validacion> validaciones;
	
	    public SalidaValidarExistenciaSubdetalleContratoComision() {
	        this.validaciones = new ArrayList<>();
	    }
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	public SalidaValidarExistenciaSubdetalleContratoComision logica(Connection con, EntradaValidarExistenciaSubdetalleContratoComision in) throws Exception {
	    logger.info("INICIO::ValidarExistenciaSubdetalleContratoComision");
	    Long inicio = System.currentTimeMillis();
	
	    SalidaValidarExistenciaSubdetalleContratoComision out = new SalidaValidarExistenciaSubdetalleContratoComision();
	    out.validaciones = validarExistenciaSubdetalleContratoComision(con, in.p_SubdetallesContratoComision, in.p_IdEmpresa);
	
	    Long fin = System.currentTimeMillis();
	    logger.info("FIN::ValidarExistenciaSubdetalleContratoComision", fin - inicio);
	
	    return out;
	}
	
	public List<Validacion> validarExistenciaSubdetalleContratoComision(Connection con, List<SubdetalleContratoComision> subdetallesContratoComision, Long idEmpresa) throws Exception {
			
		logger.info("INICIO::ValidarExistenciaSubdetalleContratoComision.validarExistenciaSubdetalleContratoComision");
		Long inicioServicio = System.currentTimeMillis();

        List<Long> idSubdetalleContratoComision = subdetallesContratoComision.parallelStream()
        										.map(subdetalleContratoComision -> subdetalleContratoComision.IdSubdetalleContratoComision)
        										.collect(Collectors.toList());
		
		HashMap<String,Object> params = new HashMap<String, Object>();
		params.put("IdSubdetalleContratoComision", idSubdetalleContratoComision);

        SQL sql = new SQL();
    	List<SubdetalleContratoComision> subdetallesContratoComisionExistentes = sql.query(con, SQL_SELECT_SUBDETALLE_CONTRATO_COMISION, params, SubdetalleContratoComision.class);
        Map<Long, SubdetalleContratoComision> subdetallesContratoComisionPorId = subdetallesContratoComisionExistentes
        																	.parallelStream()
        																	.collect(Collectors.toMap(subdetalleContratoComision -> subdetalleContratoComision.IdSubdetalleContratoComision,
        																								subdetalleContratoComision -> subdetalleContratoComision));

        List<Validacion> validaciones = new ArrayList<>();
        Validacion validacion = new Validacion();
        for (SubdetalleContratoComision subdetalleContratoComision : subdetallesContratoComision) {  
            if (subdetallesContratoComisionPorId.get(subdetalleContratoComision.IdSubdetalleContratoComision) == null) {
                validacion.EsError = true;
                String msg = Diccionario.getMensaje(con, idEmpresa, "UI_ERROR_SUBDETALLE_CONTRATO_NO_EXISTE").msg();
                validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_EDITAR_SUBDETALLE_CONTRATO", msg);
                
            }
        }
        validaciones.add(validacion);
        Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ValidarExistenciaSubdetalleContratoComision.validarExistenciaSubdetalleContratoComision", finServicio - inicioServicio);
		return validaciones;
		
	}

}
