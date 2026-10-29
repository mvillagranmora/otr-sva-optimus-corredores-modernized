package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.com.ValidarExistenciaSubdetalleContratoComision.EntradaValidarExistenciaSubdetalleContratoComision;
import cl.bcs.plataforma.com.pojo.ResultadoSubdetalleContratoComision;
import cl.bcs.plataforma.com.pojo.SubdetalleContratoComision;
import cl.bcs.plataforma.com.pojo.Validacion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ModificarSubdetalleContratoComision implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ModificarSubdetalleContratoComision.class);
	private final String SQL_UPDATE_SUBDETALLE_CONTRATO_COMISION = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarSubdetalleContratoComision_UpdateSubdetalleContratoComision.sql");
	
	public static class EntradaModificarSubdetalleContratoComision extends BcsPojo{
		public Long 							p_IdEmpresa;
		public List<SubdetalleContratoComision> p_SubdetallesContratoComision;
		
		public EntradaModificarSubdetalleContratoComision() {
			this.p_SubdetallesContratoComision = new ArrayList<SubdetalleContratoComision>();
		}
	}
	
	public static class SalidaModificarSubdetalleContratoComision extends BcsPojo{
		
		public List<MensajeSalida>							P_OUT_MENSAJES;
		public Long											P_OUT_ERRORESCONTADOR;
		public List<ResultadoSubdetalleContratoComision> 	P_OUT_REFID;
		
		public SalidaModificarSubdetalleContratoComision() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_REFID = new ArrayList<ResultadoSubdetalleContratoComision>();
			
		}
		
		public void agregarValidacionesSalida(List<Validacion> validaciones) {
			for(Validacion validacion : validaciones) {
				agregarValidacionSalida(validacion);
			}
		}
		
		public void agregarValidacionSalida(Validacion validacion) {
			if(validacion.EsError) {
				this.P_OUT_ERRORESCONTADOR += 1L;
				this.P_OUT_MENSAJES.add(validacion.MensajeError);
			}
		}	
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	public SalidaModificarSubdetalleContratoComision logica(Connection con, EntradaModificarSubdetalleContratoComision in) throws Exception{
		logger.info("INICIO::ModificarSubdetalleContratoComision");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaModificarSubdetalleContratoComision out = new SalidaModificarSubdetalleContratoComision();
		
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
	
		//Se valida si existe el subdetalle
		ValidarExistenciaSubdetalleContratoComision validarExistenciaSubdetalleContratoComision = new ValidarExistenciaSubdetalleContratoComision();
		EntradaValidarExistenciaSubdetalleContratoComision entradaValidarExistenciaSubdetalleContratoComision = new EntradaValidarExistenciaSubdetalleContratoComision();
		entradaValidarExistenciaSubdetalleContratoComision.p_IdEmpresa = in.p_IdEmpresa;
		entradaValidarExistenciaSubdetalleContratoComision.p_SubdetallesContratoComision = in.p_SubdetallesContratoComision;
		List<Validacion> validacionExistenciaSubdetalleContratoComision = validarExistenciaSubdetalleContratoComision.logica(con, entradaValidarExistenciaSubdetalleContratoComision).validaciones;
		out.agregarValidacionesSalida(validacionExistenciaSubdetalleContratoComision);
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
		
		if(!in.p_SubdetallesContratoComision.isEmpty()) {
			actualizarSubdetallesContratoComision(con, in.p_SubdetallesContratoComision);
		}

		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarSubdetalleContratoComision", finServicio - inicioServicio);
		return out;
	}
	
	
	private void actualizarSubdetallesContratoComision(Connection con, List<SubdetalleContratoComision> subdetallesContratoComision) throws Exception{
		logger.info("INICIO::ModificarSubdetalleContratoComision.modificarSubdetalleContratoComision");
		Long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_SUBDETALLE_CONTRATO_COMISION, subdetallesContratoComision, SubdetalleContratoComision.class);
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarSubdetalleContratoComision.modificarSubdetalleContratoComision", finServicio - inicioServicio);
	}
	

}
