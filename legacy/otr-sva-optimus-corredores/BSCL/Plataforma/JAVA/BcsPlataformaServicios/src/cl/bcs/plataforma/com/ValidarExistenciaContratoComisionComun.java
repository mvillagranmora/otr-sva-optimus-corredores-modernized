package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.com.pojo.ContratoComision;
import cl.bcs.plataforma.com.pojo.Validacion;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ValidarExistenciaContratoComisionComun implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ValidarExistenciaContratoComisionComun.class);
	private final String SQL_SELECT_EXISTE_CONTRATO_COMISION = loadResourceToString("cl/bcs/plataforma/com/sql/ValidarExistenciaContratoComisionComun_SelectContratoComision.sql");
	
	public static class EntradaValidarExistenciaContratoComisionComun extends BcsPojo {
	    public Long p_IdEmpresa;
	    public List<ContratoComision> p_ContratosComisiones;
	
	    public EntradaValidarExistenciaContratoComisionComun() {
	        this.p_ContratosComisiones = new ArrayList<ContratoComision>();
	    }
	}

	public static class SalidaValidarExistenciaContratoComisionComun extends BcsPojo {
	    public List<Validacion> validaciones;
	
	    public SalidaValidarExistenciaContratoComisionComun() {
	        this.validaciones = new ArrayList<>();
	    }
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	public SalidaValidarExistenciaContratoComisionComun logica(Connection con, EntradaValidarExistenciaContratoComisionComun in) throws Exception {
	     logger.info("INICIO::ValidarExistenciaContratoComisionComun");
	    Long inicio = System.currentTimeMillis();
	
	    SalidaValidarExistenciaContratoComisionComun out = new SalidaValidarExistenciaContratoComisionComun();
	    out.validaciones = validarExistenciaContratoComisionComun(con, in);
	
	    Long fin = System.currentTimeMillis();
	    logger.info("FIN::ValidarExistenciaContratoComisionComun", fin - inicio);
	
	    return out;
	}
	
	public List<Validacion> validarExistenciaContratoComisionComun(Connection con, EntradaValidarExistenciaContratoComisionComun in) throws Exception {
			
		logger.info("INICIO::ValidarExistenciaContratoComisionComun.validarExistenciaContratoComision");
		Long inicioServicio = System.currentTimeMillis();
		
		List<Validacion> validaciones  = new ArrayList<Validacion>();
		Validacion validacion = new Validacion();
		
		ContratoComision contratoComision = in.p_ContratosComisiones.parallelStream().findFirst().orElse(null);
		
		if(contratoComision != null && "ACT".equalsIgnoreCase(contratoComision.IdEstado)) {
			HashMap<String,Object> params = new HashMap<String, Object>();
			params.put("IdContratoComision", contratoComision.IdContratoComision);
			params.put("IdCliente", contratoComision.IdCliente);
			params.put("IdPortafolio", contratoComision.IdPortafolio);
			params.put("IdMoneda", contratoComision.IdMoneda);
			params.put("IdClasificacionTipoInstrumento", contratoComision.IdClasificacionTipoInstrumento);
			params.put("IdFormatoCobro", contratoComision.IdFormatoCobro);
			params.put("IdEstado", contratoComision.IdEstado);
			
			SQL sql = new SQL();
			Long count = sql.countQuery(con, SQL_SELECT_EXISTE_CONTRATO_COMISION, params);
			if(count > 0) {
				validacion.EsError = true;
				String msg = Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_EXISTE_CONTRATO_COMISION_COMUN").msg();
				validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_EXISTE_CONTRATO_COMISION", msg);
			}
		}
		
		validaciones.add(validacion);
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ValidarExistenciaContratoComisionComun.validarExistenciaContratoComision", finServicio - inicioServicio);
		return validaciones;
		
	}
	 

}
