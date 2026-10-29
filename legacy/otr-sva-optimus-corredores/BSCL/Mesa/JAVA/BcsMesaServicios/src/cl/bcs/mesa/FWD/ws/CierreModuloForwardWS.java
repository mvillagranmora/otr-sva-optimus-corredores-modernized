package cl.bcs.mesa.FWD.ws;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

public class CierreModuloForwardWS implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CierreModuloForwardWS.class);
	
	private String SQL_SELECT_CODIGO_USUARIO = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/CierreModuloForwardWS_SelectCodigoUsuario.sql");
	private String SQL_CREATE_TABLA_TEMPORAL_CIERRE_MODULO = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/CierreModuloForwardWS_CreateTablaTemporalCierreModulo.sql");
	private String SQL_INSERT_TABLA_TEMPORAL_CIERRE_MODULO = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/CierreModuloForwardWS_InsertTablaTemporalCierreModulo.sql");
	private String SQL_DROP_TABLA_TEMPORAL_CIERRE_MODULO = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/CierreModuloForwardWS_DropTablaTemporalCierreModulo.sql");
	private String SQL_CERRAR_MODULO = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/CierreModuloForwardWS_CerrarModulo.sql");
	
	public static class EntradaCierreModuloForwardWS extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
	}
	
	public static class SalidaCierreModuloForwardWS extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<ResultadoCierreModulo> P_OUT_RESULTADO;
		public SalidaCierreModuloForwardWS() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoCierreModulo>();
		}
	}
	
	public static class ResultadoCierreModulo extends BcsPojo {
		public String CodigoModulo;
		public String IdEstado;
		public String NombreModulo;
	}
	
	public static class Modulo extends BcsPojo {
		public String CodigoModulo;
		public String Estado;
		public String CodigoUsuario;
	}
	
	public static class AdvertenciasDetalle extends BcsPojo {
		public Long IdInterno;
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;

	}
	
	public static class ErrorDetalle extends BcsPojo {
		public Long IdInterno;
		public String Codigo;
		public String Descripcion;
	}
	
	public static class Usuario extends BcsPojo {
		public String CodigoUsuario;
		
		public Usuario() {
			this.CodigoUsuario = "";
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCierreModuloForwardWS jin = gson.fromJson(in.toString(), EntradaCierreModuloForwardWS.class);
		SalidaCierreModuloForwardWS out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCierreModuloForwardWS logica(Connection con, EntradaCierreModuloForwardWS entrada) throws Exception {
		logger.info("INICIO::CierreModuloForwardWS");
		long inicioServicio = System.currentTimeMillis();
		SalidaCierreModuloForwardWS out = new SalidaCierreModuloForwardWS();
		
		out = cerrarModulo(con, entrada);
		agregarLog(con, entrada);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CierreModuloForwardWS", finServicio - inicioServicio);
		return out;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SalidaCierreModuloForwardWS cerrarModulo(Connection con, EntradaCierreModuloForwardWS entrada) throws Exception {
		logger.trace("INICIO::CierreModuloForwardWS.cerrarModulo");
		SalidaCierreModuloForwardWS out = new SalidaCierreModuloForwardWS();
		
		try {
			SQL sql = new SQL();
			HashMap<String, Object> parametrosEntrada = new HashMap<String, Object>(3);
			parametrosEntrada.put("IdEmpresa", entrada.p_IdEmpresa);
			parametrosEntrada.put("IdUsuario", entrada.p_IdUsuario);
			
			HashMap<String, Class> parametrosSalida = new HashMap<String, Class>(6);
			parametrosSalida.put("p_out_Resultado", ResultadoCierreModulo.class);
			parametrosSalida.put("p_out_AdvertenciasContador", Long.class);
		    parametrosSalida.put("p_out_AdvertenciasDetalle", AdvertenciasDetalle.class); 
		    parametrosSalida.put("p_out_ErroresContador", Long.class);
		    parametrosSalida.put("p_out_ErroresDetalle", ErrorDetalle.class);
		    
		    HashMap<String, Object> respuestaCerrarModulo = new HashMap<String, Object>();
		    
			SQL.exec(con, SQL_CREATE_TABLA_TEMPORAL_CIERRE_MODULO);
			
			Modulo modulo = new Modulo();
			modulo.CodigoModulo = "MFW";
			modulo.Estado = "CER";
			modulo.CodigoUsuario = obtenerUsuario(con, entrada.p_IdUsuario).CodigoUsuario;
			
			List<Modulo> modulos = Arrays.asList(modulo);
			sql.batch(con, SQL_INSERT_TABLA_TEMPORAL_CIERRE_MODULO, modulos, Modulo.class);
			
			respuestaCerrarModulo = sql.call(con, SQL_CERRAR_MODULO, parametrosEntrada, parametrosSalida);
			out.P_OUT_ERRORESCONTADOR = (Long) respuestaCerrarModulo.get("p_out_ErroresContador");
			
			if(out.P_OUT_ERRORESCONTADOR > 0) {
				List<ErrorDetalle> erroresDetalle = (List<ErrorDetalle>) respuestaCerrarModulo.get("p_out_ErroresDetalle"); 
				out.P_OUT_MENSAJES = erroresDetalle
						.stream()
						.map(errorDetalle -> new MensajeSalida(errorDetalle.IdInterno, MensajeSalida.MENSAJE_ERROR, errorDetalle.Codigo, errorDetalle.Descripcion))
						.collect(Collectors.toList());
			}
			
			out.P_OUT_RESULTADO = (List<ResultadoCierreModulo>) respuestaCerrarModulo.get("p_out_Resultado"); 
			
		} catch (Exception e) {
			throw e;
		} finally {
			SQL.exec(con, SQL_DROP_TABLA_TEMPORAL_CIERRE_MODULO);
		}
		
		logger.trace("FIN::CierreModuloForwardWS.cerrarModulo");
		return out;
	}
	
	private Usuario obtenerUsuario(Connection con, Long idUsuario) throws Exception {
		logger.trace("INICIO::CierreModuloForwardWS.obtenerUsuario");
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdUsuario", idUsuario);
		
		SQL sql = new SQL();
		List<Usuario> usuarios = sql.query(con, SQL_SELECT_CODIGO_USUARIO, parametros, Usuario.class);
		
		Usuario usuario = usuarios.stream().findFirst().orElse(new Usuario());
		
		logger.trace("FIN::CierreModuloForwardWS.obtenerUsuario");
		return usuario;
	}
	
	private void agregarLog(Connection con, EntradaCierreModuloForwardWS entrada) throws Exception {
		logger.trace("INICIO::CierreModuloForwardWS.agregarLog");
		
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
		log.IdTipoRegistro = "CIERRE-MODULO";
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
		log.NombreProceso = "Cerrar Modulo Forward Por Web Services";						
		entradaAgregarLog.p_Log.add(log);
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		
		logger.trace("FIN::CierreModuloForwardWS.agregarLog");
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = Utils.getGsonBuilder();			
		
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"9\"}";
		EntradaCierreModuloForwardWS Objin = gson.fromJson(json, EntradaCierreModuloForwardWS.class);
		CierreModuloForwardWS demo = new CierreModuloForwardWS();				
		try {
			SalidaCierreModuloForwardWS ObjOut = demo.logica(con, Objin);
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
