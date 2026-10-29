package cl.bcs.mesa.FWD.ws;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.FWD.CuadraturaCierreDiario;
import cl.bcs.mesa.FWD.CuadraturaCierreDiario.CuadraturaCierreDiarioEntrada;
import cl.bcs.mesa.FWD.CuadraturaCierreDiario.CuadraturaCierreDiarioSalida;
import cl.bcs.plataforma.pau.AgregarControlProceso;
import cl.bcs.plataforma.pau.AgregarControlProceso.ControlProcesoEntrada;
import cl.bcs.plataforma.pau.AgregarControlProceso.ControlProcesoSalida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CuadraturaForwardWS implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CuadraturaForwardWS.class);
	
	private String SQL_SELECT_CUADRATURA = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/CuadraturaForwardWS_SelectCuadratura.sql");
	private String SQL_SELECT_CODIGO_USUARIO = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/CuadraturaForwardWS_SelectCodigoUsuario.sql");
	
	public static class EntradaCuadraturaForwardWS extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_Fecha;
		public String p_Cuadrar;
		public String p_Aprobar;
	}
	
	public static class SalidaCuadraturaForwardWS extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Cuadratura> P_OUT_CUADRATURAS;
		public SalidaCuadraturaForwardWS() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_CUADRATURAS = new ArrayList<Cuadratura>();
		}
	}
	
	public static class Cuadratura extends BcsPojo {
		public String IdCuadraturaDiariaOperaciones;
		public String FechaEjecucion;
		public String OperacionCuadraturaDiariaOperaciones;
		public String Resultado;
		public Long OperacionesConProblemas;
		public String DetalleCuadraturaJSON;
		public List<Operacion> Operaciones;
		
		public Cuadratura() {
			this.Operaciones = new ArrayList<Operacion>();
		}
		
	}
	
	public static class Operacion extends BcsPojo {
		public Long IdOperacion; 
		public Long FolioCartera;     
		public String RazonSocial;     
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal MontoMonedaSecundaria;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public BigDecimal RazonableActivo;
		public BigDecimal RazonablePasivo;
		public String IdMesa;
		public String IdEstado;
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
		EntradaCuadraturaForwardWS jin = gson.fromJson(in.toString(), EntradaCuadraturaForwardWS.class);
		SalidaCuadraturaForwardWS out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCuadraturaForwardWS logica(Connection con, EntradaCuadraturaForwardWS entrada) throws Exception {
		logger.info("INICIO::CuadraturaForwardWS");
		long inicioServicio = System.currentTimeMillis();
		SalidaCuadraturaForwardWS out = new SalidaCuadraturaForwardWS();
		
		if("S".equals(entrada.p_Cuadrar)) {
			SalidaCuadraturaForwardWS cuadratura = cuadrar(con,entrada);
			out.P_OUT_ERRORESCONTADOR = cuadratura.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(cuadratura.P_OUT_MENSAJES);
			out.P_OUT_CUADRATURAS = cuadratura.P_OUT_CUADRATURAS;
		}
		
		if("S".equals(entrada.p_Aprobar)) {
			SalidaCuadraturaForwardWS aprobacion = aprobar(con,entrada);
			out.P_OUT_ERRORESCONTADOR = aprobacion.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(aprobacion.P_OUT_MENSAJES);
		}
		
		agregarLog(con, entrada);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CuadraturaForwardWS", finServicio - inicioServicio);
		return out;
	}
	
	@SuppressWarnings("unchecked")
	private List<Cuadratura> obtenerCuadraturas(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CuadraturaForwardWS.obtenerCuadraturas");
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdEmpresa", idEmpresa);
		parametros.put("Localizacion", localizacionSistema);
		
		SQL sql = new SQL();
		List<Cuadratura> cuadraturas = sql.query(con, SQL_SELECT_CUADRATURA, parametros, Cuadratura.class);
		Gson gson = Utils.getGsonBuilder();
		
		cuadraturas.stream().forEach(cuadratura->{
			cuadratura.Operaciones = (List<Operacion>) gson.fromJson(cuadratura.DetalleCuadraturaJSON, List.class);
		});
		
		logger.trace("FIN::CuadraturaForwardWS.obtenerCuadraturas");
		return cuadraturas;
	}
	
	private SalidaCuadraturaForwardWS cuadrar(Connection con, EntradaCuadraturaForwardWS entrada) throws Exception {
		logger.trace("INICIO::CuadraturaForwardWS.cuadrar");
		SalidaCuadraturaForwardWS out = new SalidaCuadraturaForwardWS();
		
		List<Cuadratura> cuadraturas = obtenerCuadraturas(con, entrada.p_IdEmpresa);
		
		CuadraturaCierreDiarioEntrada entradaCuadratura = new CuadraturaCierreDiarioEntrada();
		entradaCuadratura.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCuadratura.p_IdUsuario = entrada.p_IdUsuario;
		entradaCuadratura.p_IdModulo = "MES";
		entradaCuadratura.p_FechaInicio = Fecha.toDate(entrada.p_Fecha);
		entradaCuadratura.p_IdProceso = cuadraturas.stream()
				.map(cuadratura->{
					CuadraturaCierreDiario.CuadraturaDiariaObj cuadraturaDiaria = new CuadraturaCierreDiario.CuadraturaDiariaObj();
					cuadraturaDiaria.IdCuadraturaDiariaOperaciones = cuadratura.IdCuadraturaDiariaOperaciones;
					return cuadraturaDiaria;
				})
				.collect(Collectors.toList());
		entradaCuadratura.p_TipoLlamado = "Proceso";
		
		CuadraturaCierreDiario cuadraturaCierreDiario = new CuadraturaCierreDiario();
		CuadraturaCierreDiarioSalida cuadraturaCierreDiarioSalida = cuadraturaCierreDiario.logica(con, entradaCuadratura);
		out.P_OUT_ERRORESCONTADOR = cuadraturaCierreDiarioSalida.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(cuadraturaCierreDiarioSalida.P_OUT_MENSAJES);
		
		out.P_OUT_CUADRATURAS = obtenerCuadraturas(con, entrada.p_IdEmpresa);
		
		logger.trace("FIN::CuadraturaForwardWS.cuadrar");
		return out;
	}
	
	private SalidaCuadraturaForwardWS aprobar(Connection con, EntradaCuadraturaForwardWS entrada) throws Exception {
		logger.trace("INICIO::CuadraturaForwardWS.aprobar");
		SalidaCuadraturaForwardWS out = new SalidaCuadraturaForwardWS();
		
		Long numeroDescuadres = obtenerCuadraturas(con, entrada.p_IdEmpresa).stream().filter(cuadratura->cuadratura.OperacionesConProblemas > 0).count();
		
		ControlProcesoEntrada controlProcesoEntrada = new ControlProcesoEntrada();
		controlProcesoEntrada.IdEmpresa = entrada.p_IdEmpresa;
		controlProcesoEntrada.CodigoModulo = "MFW";
		controlProcesoEntrada.CodigoProceso = "CUADRATURA_MFW";
		controlProcesoEntrada.FechaSistema = entrada.p_Fecha;
		controlProcesoEntrada.CodigoUsuario = obtenerUsuario(con, entrada.p_IdUsuario).CodigoUsuario;
		controlProcesoEntrada.ResultadoProceso = numeroDescuadres > 0 ? "UI_DESCUADRADO" : "UI_CUADRADO";
		AgregarControlProceso agregarControlProceso = new AgregarControlProceso();
		
		ControlProcesoSalida controlProcesoSalida = agregarControlProceso.logica(con, controlProcesoEntrada);
		out.P_OUT_ERRORESCONTADOR = controlProcesoSalida.P_OUT_ERRORESCONTADOR;
		if(!controlProcesoSalida.P_OUT_MENSAJE.isEmpty()) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "", controlProcesoSalida.P_OUT_MENSAJE));
		} else if(controlProcesoSalida.P_OUT_ERRORESCONTADOR == 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_EXITO", new HashMap<String,String> (1), con, entrada.p_IdEmpresa));
		}
		
		logger.trace("FIN::CuadraturaForwardWS.aprobar");
		return out;
	}
	
	private Usuario obtenerUsuario(Connection con, Long idUsuario) throws Exception {
		logger.trace("INICIO::CuadraturaForwardWS.obtenerUsuario");
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdUsuario", idUsuario);
		
		SQL sql = new SQL();
		List<Usuario> usuarios = sql.query(con, SQL_SELECT_CODIGO_USUARIO, parametros, Usuario.class);
		
		Usuario usuario = usuarios.stream().findFirst().orElse(new Usuario());
		
		logger.trace("FIN::CuadraturaForwardWS.obtenerUsuario");
		return usuario;
	}
	
	private void agregarLog(Connection con, EntradaCuadraturaForwardWS entrada) throws Exception {
		logger.trace("INICIO::CuadraturaForwardWS.agregarLog");
		
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
		log.NombreProceso = "Cuadratura Mesa Forward Por Web Services";						
		entradaAgregarLog.p_Log.add(log);
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		
		logger.trace("FIN::CuadraturaForwardWS.agregarLog");
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = Utils.getGsonBuilder();			
		
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"9\",\"p_Fecha\":\"2023-01-25\",\"p_Cuadrar\":\"S\",\"p_Aprobar\":\"S\"}";
		EntradaCuadraturaForwardWS Objin = gson.fromJson(json, EntradaCuadraturaForwardWS.class);
		CuadraturaForwardWS demo = new CuadraturaForwardWS();				
		try {
			SalidaCuadraturaForwardWS ObjOut = demo.logica(con, Objin);
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
