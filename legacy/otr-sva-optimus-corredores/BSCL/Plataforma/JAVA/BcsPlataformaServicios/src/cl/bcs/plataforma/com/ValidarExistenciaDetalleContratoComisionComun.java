package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.com.pojo.DetalleContratoComision;
import cl.bcs.plataforma.com.pojo.Validacion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ValidarExistenciaDetalleContratoComisionComun implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ValidarExistenciaDetalleContratoComisionComun.class);
	private final String SQL_SELECT_EXISTE_DETALLE_CONTRATO_COMISION = loadResourceToString("cl/bcs/plataforma/com/sql/ValidarExistenciaDetalleContratoComisionComun_SelectDetalleContratoComision.sql");
	private final String SQL_SELECT_EXISTE_DETALLE_CONTRATO_COMISION_FORMA_COBRO = loadResourceToString("cl/bcs/plataforma/com/sql/ValidarExistenciaDetalleContratoComisionComun_SelectDetalleContratoComisionFormaCobro.sql");

	public static class EntradaValidarExistenciaDetalleContratoComisionComun extends BcsPojo {
        public Long p_IdEmpresa;
        public List<DetalleContratoComision> p_DetallesContratoComision;

        public EntradaValidarExistenciaDetalleContratoComisionComun() {
            this.p_DetallesContratoComision = new ArrayList<DetalleContratoComision>();
        }
    }

    public static class SalidaValidarExistenciaDetalleContratoComisionComun extends BcsPojo {
        public List<Validacion> validaciones;

        public SalidaValidarExistenciaDetalleContratoComisionComun() {
            this.validaciones = new ArrayList<>();
        }
    }

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	public SalidaValidarExistenciaDetalleContratoComisionComun logica(Connection con, EntradaValidarExistenciaDetalleContratoComisionComun in) throws Exception {
	    logger.info("INICIO::ValidarExistenciaDetalleContratoComision");
	    Long inicio = System.currentTimeMillis();
	
	    SalidaValidarExistenciaDetalleContratoComisionComun out = new SalidaValidarExistenciaDetalleContratoComisionComun();
	    out.validaciones = validarExistenciaDetalleContratoComisionComun(con, in);
	
	    Long fin = System.currentTimeMillis();
	    logger.info("FIN::ValidarExistenciaDetalleContratoComision", fin - inicio);
	
	    return out;
	 }
	
	 public List<Validacion> validarExistenciaDetalleContratoComisionComun(Connection con, EntradaValidarExistenciaDetalleContratoComisionComun in) throws Exception {
			
		logger.info("INICIO::ValidarExistenciaDetalleContratoComision.validarExistenciaDetalleContratoComision");
		Long inicioServicio = System.currentTimeMillis();
		
		List<Validacion> validaciones  = new ArrayList<Validacion>();
		Validacion validacion = new Validacion();
		
		DetalleContratoComision detalleContratoComision = in.p_DetallesContratoComision.parallelStream().findFirst().orElse(null);
		
		if(detalleContratoComision != null) {
			HashMap<String,Object> params = new HashMap<String, Object>();
			params.put("IdDetalleContratoComision", detalleContratoComision.IdDetalleContratoComision);
			params.put("IdContratoComision", detalleContratoComision.IdContratoComision);
			
			SQL sql = new SQL();
			Long count = sql.countQuery(con, SQL_SELECT_EXISTE_DETALLE_CONTRATO_COMISION, params);
			if(count > 0) {
				validacion.EsError = true;
				validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_EXISTE_DETALLE_CONTRATO_COMISION", new HashMap<String,String>(), con, in.p_IdEmpresa);
			}
			
			
		}
		
		validaciones.add(validacion);
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ValidarExistenciaDetalleContratoComision.validarExistenciaDetalleContratoComision", finServicio - inicioServicio);
		return validaciones;
		
	}
	 

}
