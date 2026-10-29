package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.com.pojo.ResultadoSubdetalleContratoComision;
import cl.bcs.plataforma.com.pojo.SubdetalleContratoComision;
import cl.bcs.plataforma.com.pojo.Validacion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearSubdetalleContratoComision implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CrearSubdetalleContratoComision.class);
	private final String SQL_INSERT_SUBDETALLE_CONTRATO_COMISION = loadResourceToString("cl/bcs/plataforma/com/sql/CrearSubdetalleContratoComision_InsertSubdetalleContratoComision.sql");
	
	public static class EntradaCrearSubdetalleContratoComision extends BcsPojo{
		public Long p_IdDetalleContratoComision;
		public List<SubdetalleContratoComision> p_SubdetallesContratoComision;
		
		public EntradaCrearSubdetalleContratoComision() {
			this.p_SubdetallesContratoComision = new ArrayList<SubdetalleContratoComision>();
		}
		
	}
		
	public static class SalidaCrearSubdetalleContratoComision extends BcsPojo{
		
		public List<MensajeSalida>							P_OUT_MENSAJES;
		public Long											P_OUT_ERRORESCONTADOR;
		public List<ResultadoSubdetalleContratoComision> 	P_OUT_REFID;
		
		public SalidaCrearSubdetalleContratoComision() {
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
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		
		return null;
		
	}
	
	public SalidaCrearSubdetalleContratoComision logica(Connection con, EntradaCrearSubdetalleContratoComision in) throws Exception {
		logger.info("INICIO::CrearSubdetalleContratoComision");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaCrearSubdetalleContratoComision out = new SalidaCrearSubdetalleContratoComision();
		
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
	
		List<SubdetalleContratoComision> subDetallesContratoComision = obtenerSubdetallesContratoComision(con, in);
		if(!subDetallesContratoComision.isEmpty()) {
			insertarSubdetallesContratoComision(con, subDetallesContratoComision);
		}

		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearSubdetalleContratoComision", finServicio - inicioServicio);
		
		return out;
	
	}
	
	private List<SubdetalleContratoComision> obtenerSubdetallesContratoComision(Connection con, EntradaCrearSubdetalleContratoComision in) throws Exception {
		
		logger.info("INICIO::CrearSubdetalleContratoComision.obtenerSubdetallesCotratoComision");
		Long inicioServicio = System.currentTimeMillis();
		Long cantidadElementos = new Long(in.p_SubdetallesContratoComision.size()) ;
		List<Long> secuenciasSubdetalleContratoComision = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::COM.SubdetalleContratoComision", cantidadElementos);
		List<SubdetalleContratoComision> subdetallesContratoComision = new ArrayList<SubdetalleContratoComision>();
		Integer i = 0;
		for(SubdetalleContratoComision subdetalleContratoComision : in.p_SubdetallesContratoComision) {
			if(subdetalleContratoComision.IdSubdetalleContratoComision == null) {
				SubdetalleContratoComision subdetalle = new SubdetalleContratoComision();
				subdetalle.IdSubdetalleContratoComision = secuenciasSubdetalleContratoComision.get(i);
				subdetalle.IdDetalleContratoComision 	= in.p_IdDetalleContratoComision;
				subdetalle.Monto						= subdetalleContratoComision.Monto;
				subdetalle.Porcentaje 					= subdetalleContratoComision.Porcentaje;
				subdetalle.MontoMaximo 					= subdetalleContratoComision.MontoMaximo;
				subdetallesContratoComision.add(subdetalle);
				i++;
			}
			
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearSubdetalleContratoComision.obtenerSubdetallesCotratoComision", finServicio - inicioServicio);
		
		return subdetallesContratoComision;
	}
	
	private void insertarSubdetallesContratoComision(Connection con, List<SubdetalleContratoComision> subdetallesContratoComision) throws Exception {
		
		logger.info("INICIO::CrearSubdetalleContratoComision.insertarSubdetallesCotratoComision");
		Long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_SUBDETALLE_CONTRATO_COMISION, subdetallesContratoComision, SubdetalleContratoComision.class);
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearSubdetalleContratoComision.insertarSubdetallesCotratoComision", finServicio - inicioServicio);
	}

}
