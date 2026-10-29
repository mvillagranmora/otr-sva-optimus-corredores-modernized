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

public class ValidarExistenciaTipoContratoComision implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ValidarExistenciaTipoContratoComision.class);
	private final String SQL_SELECT_EXISTE_TIPO_CONVENIO_COMISION = loadResourceToString("cl/bcs/plataforma/com/sql/ValidarExistenciaTipoConvenioComision_SelectContratoComision.sql");
	private final String SQL_SELECT_EXISTE_TIPO_CONTRATO_COMISION = loadResourceToString("cl/bcs/plataforma/com/sql/ValidarExistenciaTipoContratoComision_SelectContratoComision.sql");
	
	public static class EntradaValidarExistenciaTipoContratoComision extends BcsPojo {
        public Long p_IdEmpresa;
        public List<ContratoComision> p_ContratosComisiones;

        public EntradaValidarExistenciaTipoContratoComision() {
            this.p_ContratosComisiones = new ArrayList<ContratoComision>();
        }
    }

    public static class SalidaValidarExistenciaTipoContratoComision extends BcsPojo {
        public List<Validacion> validaciones;

        public SalidaValidarExistenciaTipoContratoComision() {
            this.validaciones = new ArrayList<>();
        }
    }

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	public SalidaValidarExistenciaTipoContratoComision logica(Connection con, EntradaValidarExistenciaTipoContratoComision in) throws Exception {
	    logger.info("INICIO::ValidarExistenciaTipoContratoComision");
	    Long inicio = System.currentTimeMillis();
	
	    SalidaValidarExistenciaTipoContratoComision out = new SalidaValidarExistenciaTipoContratoComision();
	    out.validaciones = validarExistenciaTipoContratoComisionComun(con, in);
	
	    Long fin = System.currentTimeMillis();
	    logger.info("FIN::ValidarExistenciaTipoContratoComision", fin - inicio);
	
	    return out;
	 }
	
	 public List<Validacion> validarExistenciaTipoContratoComisionComun(Connection con, EntradaValidarExistenciaTipoContratoComision in) throws Exception {
			
		logger.info("INICIO::ValidarExistenciaTipoContratoComision.validarExistenciaTipoContratoComisionComun");
		Long inicioServicio = System.currentTimeMillis();
		
		List<Validacion> validaciones  = new ArrayList<Validacion>();
		Validacion validacion = new Validacion();
		
		ContratoComision contratoComision = in.p_ContratosComisiones.parallelStream().findFirst().orElse(null);
		
		if(contratoComision != null && "ACT".equalsIgnoreCase(contratoComision.IdEstado)) {
			
			HashMap<String,Object> params = new HashMap<String, Object>();
			params.put("IdContratoComision", contratoComision.IdContratoComision);
			params.put("IdCliente", contratoComision.IdCliente);
			params.put("IdPortafolio", contratoComision.IdPortafolio);
			params.put("IdClasificacionTipoInstrumento", contratoComision.IdClasificacionTipoInstrumento);
			params.put("IdEstado", contratoComision.IdEstado);
			params.put("IdTipoContrato", contratoComision.IdTipoContrato);
			
			SQL sql = new SQL();
			Long count = 0L;
			if(contratoComision.IdTipoContrato.equals("CONV")) {
				count = sql.countQuery(con, SQL_SELECT_EXISTE_TIPO_CONVENIO_COMISION, params);
				if( count > 0 ) {
					validacion.EsError = true;
					String msg = Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_EXISTE_CONTRATO_CONVENIO_SIMILAR").msg();
					validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_EXISTE_CONTRATO_CONVENIO", msg);
				}
			}else{
				
				count = sql.countQuery(con, SQL_SELECT_EXISTE_TIPO_CONVENIO_COMISION, params);
				if( count > 0 ) {
					validacion.EsError = true;
					String msg = Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_EXISTE_CONTRATO_SIMILAR").msg();
					validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_EXISTE_CONTRATO_SIMILAR", msg);
				}else {
					params.remove("IdTipoContrato");
					params.put("IdMoneda", contratoComision.IdMoneda);
					count = sql.countQuery(con, SQL_SELECT_EXISTE_TIPO_CONTRATO_COMISION, params);
					if( count > 0 ) {
						validacion.EsError = true;
						String msg = Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_EXISTE_CONTRATO_SIMILAR").msg();
						validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_EXISTE_CONTRATO_SIMILAR", msg);
					}
				}
				
				
			}
			
			
		}
		
		validaciones.add(validacion);
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ValidarExistenciaTipoContratoComision.validarExistenciaTipoContratoComisionComun", finServicio - inicioServicio);
		return validaciones;
		
	}


}
