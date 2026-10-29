package cl.bcs.mesa.FWD.ws;

import java.math.BigDecimal;
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

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CerrarMesaForwardWS implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CerrarMesaForwardWS.class);
	
	private String SQL_SELECT_MESA_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/CerrarMesaForwardWS_SelectMesaForward.sql");
	private String SQL_CREATE_TABLA_TEMPORAL_CIERRE_MESA = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/CerrarMesaForwardWS_CreateTablaTemporalCierreMesa.sql");
	private String SQL_INSERT_TABLA_TEMPORAL_CIERRE_MESA = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/CerrarMesaForwardWS_InsertTablaTemporalCierreMesa.sql");
	private String SQL_DROP_TABLA_TEMPORAL_CIERRE_MESA = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/CerrarMesaForwardWS_DropTablaTemporalCierreMesa.sql");
	private String SQL_CERRAR_MESAS = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/CerrarMesaForwardWS_CerrarMesas.sql");
	
	public static class EntradaCerrarMesaForwardWS extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public List<MesaForwardEntrada> p_Mesas;
		
		public EntradaCerrarMesaForwardWS() {
			this.p_Mesas = new ArrayList<MesaForwardEntrada>();
		}
	}
	
	public static class MesaForwardEntrada extends BcsPojo {
		public Long IdMesa;
	}
	
	public static class SalidaCerrarMesaForwardWS extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<ResultadoCierreMesa> P_OUT_RESULTADO;
		
		public SalidaCerrarMesaForwardWS() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoCierreMesa>();
		}
	}
	
	public static class MesaForward extends BcsPojo {
		public Long IdMesa;
		public String NombreMesa;
	}
	
	public static class ResultadoCierreMesa extends BcsPojo {
		public Long IdMesa;
		public String Resultado;
	}
		
	public static class AdvertenciasDetalle extends BcsPojo {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;

	}
	
	public static class ErrorDetalle extends BcsPojo {
		public String Codigo;
		public String Descripcion;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCerrarMesaForwardWS jin = gson.fromJson(in.toString(), EntradaCerrarMesaForwardWS.class);
		SalidaCerrarMesaForwardWS out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCerrarMesaForwardWS logica(Connection con, EntradaCerrarMesaForwardWS entrada) throws Exception {
		logger.info("INICIO::CerrarMesaForwardWS");
		long inicioServicio = System.currentTimeMillis();
		SalidaCerrarMesaForwardWS out = new SalidaCerrarMesaForwardWS();
		
		List<MesaForward> mesasForward = obtenerMesasForward(con, entrada.p_IdEmpresa, entrada.p_Mesas);
		out = validar(con, entrada, mesasForward);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		out = cerrarMesas(con, entrada, mesasForward);
		agregarLog(con, entrada);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CerrarMesaForwardWS", finServicio - inicioServicio);
		return out;
	}
	
	private List<MesaForward> obtenerMesasForward(Connection con, Long idEmpresa, List<MesaForwardEntrada> mesas) throws Exception {
		logger.trace("INICIO::CerrarMesaForwardWS.obtenerMesasForward");
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdEmpresa", idEmpresa);
		List<Long> idMesas = mesas.stream().map(mesa->mesa.IdMesa).collect(Collectors.toList());
		parametros.put("IdMesa", idMesas);
		
		SQL sql = new SQL();
		List<MesaForward> mesasForward = sql.query(con, SQL_SELECT_MESA_FORWARD, parametros, MesaForward.class);
		
		logger.trace("FIN::CerrarMesaForwardWS.obtenerMesasForward");
		return mesasForward;
	}
	
	private SalidaCerrarMesaForwardWS validar(Connection con, EntradaCerrarMesaForwardWS entrada, List<MesaForward> mesas) throws Exception {
		logger.trace("INICIO::CerrarMesaForwardWS.validar");
		SalidaCerrarMesaForwardWS out = new SalidaCerrarMesaForwardWS();
		Map<Long, MesaForward> mesasForwardPorId = mesas.stream().collect(Collectors.toMap(mesa->mesa.IdMesa, mesa->mesa, (primeraMesa, segundaMesa)->primeraMesa));
		
		for(MesaForwardEntrada mesaEntrada : entrada.p_Mesas) {
			MesaForward mesaForward = mesasForwardPorId.get(mesaEntrada.IdMesa);
			if(mesaForward == null) {
				HashMap<String, String> parametros = new HashMap<String,String>(2);
				parametros.put("IdMesa", String.valueOf(mesaEntrada.IdMesa));
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CIERRE_MESA_FORWARD_WS_MESA_NO_EXISTE", parametros, con, entrada.p_IdEmpresa));
			}
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		logger.trace("FIN::CerrarMesaForwardWS.validar");
		return out;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SalidaCerrarMesaForwardWS cerrarMesas(Connection con, EntradaCerrarMesaForwardWS entrada, List<MesaForward> mesas) throws Exception {
		logger.trace("INICIO::CerrarMesaForwardWS.cerrarMesas");
		SalidaCerrarMesaForwardWS out = new SalidaCerrarMesaForwardWS();
		
		try {
			SQL sql = new SQL();
			HashMap<String, Object> parametrosEntrada = new HashMap<String, Object>(3);
			parametrosEntrada.put("IdEmpresa", entrada.p_IdEmpresa);
			parametrosEntrada.put("IdUsuario", entrada.p_IdUsuario);
			
			HashMap<String, Class> parametrosSalida = new HashMap<String, Class>(6);
			parametrosSalida.put("p_out_ResultadoProceso", ResultadoCierreMesa.class);
			parametrosSalida.put("p_out_AdvertenciasContador", Long.class);
		    parametrosSalida.put("p_out_AdvertenciasDetalle", AdvertenciasDetalle.class); 
		    parametrosSalida.put("p_out_ErroresContador", Long.class);
		    parametrosSalida.put("p_out_ErroresDetalle", ErrorDetalle.class);
		    
		    HashMap<String, Object> respuestaOficializar = new HashMap<String, Object>();
		    
			SQL.exec(con, SQL_CREATE_TABLA_TEMPORAL_CIERRE_MESA);

			sql.batch(con, SQL_INSERT_TABLA_TEMPORAL_CIERRE_MESA, mesas, MesaForward.class);
			
			respuestaOficializar = sql.call(con, SQL_CERRAR_MESAS, parametrosEntrada, parametrosSalida);
			out.P_OUT_ERRORESCONTADOR = (Long) respuestaOficializar.get("p_out_ErroresContador");
			
			if(out.P_OUT_ERRORESCONTADOR > 0) {
				List<ErrorDetalle> erroresDetalle = (List<ErrorDetalle>) respuestaOficializar.get("p_out_ErroresDetalle"); 
				out.P_OUT_MENSAJES = erroresDetalle
						.stream()
						.map(errorDetalle -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, errorDetalle.Codigo, errorDetalle.Descripcion))
						.collect(Collectors.toList());
			}
			
			out.P_OUT_RESULTADO = (List<ResultadoCierreMesa>) respuestaOficializar.get("p_out_ResultadoProceso"); 
			
		} catch (Exception e) {
			throw e;
		} finally {
			SQL.exec(con, SQL_DROP_TABLA_TEMPORAL_CIERRE_MESA);
		}
		
		logger.trace("FIN::CerrarMesaForwardWS.cerrarMesas");
		return out;
	}
	
	private void agregarLog(Connection con, EntradaCerrarMesaForwardWS entrada) throws Exception {
		logger.trace("INICIO::CerrarMesaForwardWS.agregarLog");
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		
		log.IdCabeceraLog = 1L;
		log.IdTipoLog = "MODIFICACION";
		log.IdModulo = "MFW";
		log.IdUsuario = entrada.p_IdUsuario;
		log.IdEmpresa = entrada.p_IdEmpresa;
		log.FechaSistemaLog = fechaSistema;
		log.IdUsuarioAtendido = entrada.p_IdUsuario;
		log.IdCliente = 0L;
		log.EstadoCliente = "";
		log.NombreCliente = "";
		log.IdTipoDocumentoIdentidad = "";
		log.IdDocumentoIdentidad = "";
		log.IdPortafolio = 0L;
		log.EstadoPortafolio = "";
		log.IdTipoRegistro = "MESA";
		log.EstadoRegistro = "VIG";
		log.IdTipoOperacion = "";
		log.Folio = 0L;
		log.Cantidad = BigDecimal.ZERO;
		log.PrecioTasa = BigDecimal.ZERO;
		log.Monto = BigDecimal.ZERO;
		log.IdMonedaOrigen = "";
		log.IdMonedaLiquidacion = "";
		log.TipoCambio = "";
		log.Nemo = "";
		log.IdProceso = 0L;
		log.EstadoProceso = "VIG";
		log.NombreProceso = "Cerrar Mesa Forward Por Web Services";						
		entradaAgregarLog.p_Log.add(log);
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		
		logger.trace("FIN::CerrarMesaForwardWS.agregarLog");
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = Utils.getGsonBuilder();			
		
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"9\",\"p_Mesas\":[{\"IdMesa\":21}]}";
		EntradaCerrarMesaForwardWS Objin = gson.fromJson(json, EntradaCerrarMesaForwardWS.class);
		CerrarMesaForwardWS demo = new CerrarMesaForwardWS();				
		try {
			SalidaCerrarMesaForwardWS ObjOut = demo.logica(con, Objin);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
						
    	};
		System.out.println("== END ==");
	}

}
