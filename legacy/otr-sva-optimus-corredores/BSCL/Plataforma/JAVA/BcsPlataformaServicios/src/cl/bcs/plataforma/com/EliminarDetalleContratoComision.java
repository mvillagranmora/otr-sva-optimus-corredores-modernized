package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.com.EliminarSubdetalleContratoComision.EntradaEliminarSubdetalleContratoComision;
import cl.bcs.plataforma.com.ValidarExistenciaDetalleContratoComisionComun.EntradaValidarExistenciaDetalleContratoComisionComun;
import cl.bcs.plataforma.com.pojo.DetalleContratoComision;
import cl.bcs.plataforma.com.pojo.ResultadoDetalleContratoComision;
import cl.bcs.plataforma.com.pojo.SubdetalleContratoComision;
import cl.bcs.plataforma.com.pojo.Validacion;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarDetalleContratoComision implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ModificarDetalleContratoComision.class);
	
	private final String SQL_SELECT_DETALLE_CONTRATO_COMISION = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarDetalleContratoComision_SelectDetalleContratoComision.sql");
	private final String SQL_DELETE_DETALLE_CONTRATO_COMISION = loadResourceToString("cl/bcs/plataforma/com/sql/EliminarDetalleContratoComision_DeleteDetalleContratoComision.sql");
	private final String SQL_SELECT_SUBDETALLE_CONTRATO_COMISION = loadResourceToString("cl/bcs/plataforma/com/sql/EliminarDetalleContratoComision_SelectSubdetalleContratoComision.sql");
	
	public static class EntradaEliminarDetalleContratoComision extends BcsPojo{
		
		public Long 							p_IdEmpresa;
		public Long 							p_IdUsuario;
		public String 							p_IdModulo;
		public String 							p_UsaReglasNegocio;
		public List<DetalleContratoComision> 	p_DetallesContratoComision;
		
		public EntradaEliminarDetalleContratoComision() {
			this.p_DetallesContratoComision = new ArrayList<DetalleContratoComision>();
		}
	}
	
	public static class SalidaEliminarDetalleContratoComision extends BcsPojo{
		
		public List<MensajeSalida>						P_OUT_MENSAJES;
		public Long										P_OUT_ERRORESCONTADOR;
		public List<ResultadoDetalleContratoComision> 	P_OUT_REFID;
		
		public SalidaEliminarDetalleContratoComision() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_REFID = new ArrayList<ResultadoDetalleContratoComision>();
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
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaEliminarDetalleContratoComision in = gson.fromJson(jin.toString(), EntradaEliminarDetalleContratoComision.class);
		SalidaEliminarDetalleContratoComision out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaEliminarDetalleContratoComision logica(Connection con, EntradaEliminarDetalleContratoComision in) throws Exception{
		logger.info("INICIO::EliminarDetalleContratoComision");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaEliminarDetalleContratoComision out = new SalidaEliminarDetalleContratoComision();
		
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
	
		List<Validacion> validaciones = validarExistenciaDetalleContratoComision(con, in.p_DetallesContratoComision, in.p_IdEmpresa);
		out.agregarValidacionesSalida(validaciones);
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;	
		
		ValidarExistenciaDetalleContratoComisionComun validarExistenciaDetalleContratoComisionComun = new ValidarExistenciaDetalleContratoComisionComun();
		EntradaValidarExistenciaDetalleContratoComisionComun entradaValidarExistenciaDetalleContratoComision = new EntradaValidarExistenciaDetalleContratoComisionComun();
		entradaValidarExistenciaDetalleContratoComision.p_IdEmpresa = in.p_IdEmpresa;
		entradaValidarExistenciaDetalleContratoComision.p_DetallesContratoComision = in.p_DetallesContratoComision;
		List<Validacion> validacionExistenciaDetalleContratoComisionComun = validarExistenciaDetalleContratoComisionComun.logica(con, entradaValidarExistenciaDetalleContratoComision).validaciones;
		out.agregarValidacionesSalida(validacionExistenciaDetalleContratoComisionComun);
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
		
		DetalleContratoComision detalleContratoComision = in.p_DetallesContratoComision.parallelStream().findFirst().orElse(null);
		if(detalleContratoComision != null) {
			eliminarSubdetalleContratoComision(con, detalleContratoComision, in.p_IdEmpresa);			
		}
				
		eliminarDetalleContratoComision(con, in.p_DetallesContratoComision);

		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarDetalleContratoComision", finServicio - inicioServicio);
		return out;
	}
	
	public void eliminarDetalleContratoComision(Connection con, List<DetalleContratoComision> detallesContratoComision) throws Exception{
		logger.info("INICIO::EliminarDetalleContratoComision.insertarDetalleCotratoComision");
		Long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_DELETE_DETALLE_CONTRATO_COMISION, detallesContratoComision, DetalleContratoComision.class);
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarDetalleContratoComision.insertarDetalleContratoComision", finServicio - inicioServicio);
	}
	
	
	
	private List<Validacion> validarExistenciaDetalleContratoComision(Connection con, List<DetalleContratoComision> detallesContratoComision, Long idEmpresa) throws Exception {
        logger.debug("INICIO::EliminarDetalleContratoComision.validarExistenciaDetalleContratoComision");
        Long inicioServicio = System.currentTimeMillis();

        List<Long> idDetalleContratoComision = detallesContratoComision.parallelStream()
        										.map(detalleContratoComision -> detalleContratoComision.IdDetalleContratoComision)
        										.collect(Collectors.toList());
		
		HashMap<String,Object> params = new HashMap<String, Object>();
		params.put("IdDetalleContratoComision", idDetalleContratoComision);

        SQL sql = new SQL();
    	List<DetalleContratoComision> detallesContratoComisionExistentes = sql.query(con, SQL_SELECT_DETALLE_CONTRATO_COMISION, params, DetalleContratoComision.class);
        Map<Long, DetalleContratoComision> detallesContratoComisionPorId = detallesContratoComisionExistentes
        																	.parallelStream()
        																	.collect(Collectors.toMap(detalleContratoComision -> detalleContratoComision.IdDetalleContratoComision,
        																								detalleContratoComision -> detalleContratoComision));

        List<Validacion> validaciones = new ArrayList<>();

        for (DetalleContratoComision detalleContratoComision : detallesContratoComision) {
            if (detallesContratoComisionPorId.get(detalleContratoComision.IdDetalleContratoComision) == null) {
                Validacion validacion = new Validacion();
                validacion.EsError = true;
                String msg = Diccionario.getMensaje(con, idEmpresa, "UI_ERROR_DETALLE_CONTRATO_NO_EXISTE").msg();
                validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_EDITAR_DETALLE_CONTRATO", msg);
                validaciones.add(validacion);
            }
        }

        Long finServicio = System.currentTimeMillis();
        logger.debug("FIN::EliminarDetalleContratoComision.validarExistenciaDetalleContratoComision", finServicio - inicioServicio);

        return validaciones;
    }
	

	
	private void eliminarSubdetalleContratoComision(Connection con, DetalleContratoComision detallesContratoComision, Long idEmpresa) throws Exception {
		logger.info("INICIO::EliminarDetalleContratoComision.eliminarSubdetalleContratoComision");
		Long inicioServicio = System.currentTimeMillis();
		
		HashMap<String,Object> params = new HashMap<String, Object>();
		params.put("IdDetalleContratoComision", detallesContratoComision.IdDetalleContratoComision);

        SQL sql = new SQL();
    	List<SubdetalleContratoComision> subDetallesContratoComision = sql.query(con, SQL_SELECT_SUBDETALLE_CONTRATO_COMISION, params, SubdetalleContratoComision.class);
       
		EliminarSubdetalleContratoComision eliminarSubdetalleContratoComision 				= new EliminarSubdetalleContratoComision();
		EntradaEliminarSubdetalleContratoComision entradaEliminarSubdetalleContratoComision = new EntradaEliminarSubdetalleContratoComision();
		entradaEliminarSubdetalleContratoComision.p_IdEmpresa								= idEmpresa;
		entradaEliminarSubdetalleContratoComision.p_SubdetallesContratoComision 			= subDetallesContratoComision;
		eliminarSubdetalleContratoComision.logica(con, entradaEliminarSubdetalleContratoComision);
		
		Long finServicio = System.currentTimeMillis();
	    logger.debug("FIN::EliminarDetalleContratoComision.eliminarSubdetalleContratoComision", finServicio - inicioServicio);
		
	}
	
	public static void main(String[] args) throws Exception {
	
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();

			String jsonEntrada = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_DetallesContratoComision\":[{\"IdContratoComision\":1,\"IdDetalleContratoComision\":1,\"IdCobroClasificacion\":1,\"IdCobro\":\"COFI\",\"EsPorRangos\":\"N\",\"IdConcepto\":\"OPP3SB\",\"MontoMinimo\":0,\"MontoMaximo\":0,\"SubdetallesContratoComision\":[]}]}";
			EntradaEliminarDetalleContratoComision ent = gson.fromJson(jsonEntrada, EntradaEliminarDetalleContratoComision.class);
			EliminarDetalleContratoComision detalleContratoComision = new EliminarDetalleContratoComision();
			try {
				SalidaEliminarDetalleContratoComision detalleContratoComisionSalida = detalleContratoComision.logica(con, ent);
				con.commit();
				System.out.println(detalleContratoComisionSalida.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
						
    	}
		System.out.println("== END ==");
		
	}

	
	

}
