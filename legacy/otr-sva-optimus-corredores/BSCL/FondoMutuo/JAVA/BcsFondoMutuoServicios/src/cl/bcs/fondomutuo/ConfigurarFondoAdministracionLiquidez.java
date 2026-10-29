package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfigurarFondoAdministracionLiquidez implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ConfigurarFondoAdministracionLiquidez.class);
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	private final String SQL_SELECT_CONFIGURACIONES = loadResourceToString("cl/bcs/fondomutuo/sql/ConfigurarFondoAdministracionLiquidez_SelectConfiguracion.sql");
	private final String SQL_INSERT_CONFIGURACIONES = loadResourceToString("cl/bcs/fondomutuo/sql/ConfigurarFondoAdministracionLiquidez_InsertConfiguracion.sql");
	private final String SQL_UPDATE_CONFIGURACIONES = loadResourceToString("cl/bcs/fondomutuo/sql/ConfigurarFondoAdministracionLiquidez_UpdateConfiguracion.sql");
	
	public static class EntradaConfigurarFondoAdministracionLiquidez{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<Configuracion> p_FondoAdministracionLiquidez;
		
		public EntradaConfigurarFondoAdministracionLiquidez() {
			this.p_FondoAdministracionLiquidez = new ArrayList<Configuracion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Configuracion{
		public String IdMoneda;
		public String IdSerieFondo;
		public BigDecimal MontoMinimo;
		public String HoraEjecucion;
		public Long IdEmpresa;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConfigurarFondoAdministracionLiquidez{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Configuracion> P_OUT_RESULTADO;
		
		public SalidaConfigurarFondoAdministracionLiquidez() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<Configuracion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfigurarFondoAdministracionLiquidez jin = gson.fromJson(in.toString(), EntradaConfigurarFondoAdministracionLiquidez.class);
		SalidaConfigurarFondoAdministracionLiquidez out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConfigurarFondoAdministracionLiquidez logica (Connection con, EntradaConfigurarFondoAdministracionLiquidez entrada) throws Exception {
		logger.info("INICIO::ConfigurarFondoAdministracionLiquidez");
		SalidaConfigurarFondoAdministracionLiquidez out = new SalidaConfigurarFondoAdministracionLiquidez();
		SQL sql=new SQL();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		if(entrada.p_FondoAdministracionLiquidez.size() == 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_FONDOS",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		HashMap<String,Boolean> hMonedas = new HashMap<String,Boolean>();
		List<String> series = new ArrayList<String>();
		List<String> monedas = new ArrayList<String>();
		for(Configuracion conf : entrada.p_FondoAdministracionLiquidez) {
			if(hMonedas.get(conf.IdMoneda) != null) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MONEDA_REPETIDAS",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			monedas.add(conf.IdMoneda);
			series.add(conf.IdSerieFondo);
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdSerieFondo", series);
		List<SerieFondoSolicitudes> seriesFondo = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
		params.clear();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdMoneda", monedas);
		List<Configuracion> configuracionesExistentes = sql.query(con, SQL_SELECT_CONFIGURACIONES, params, Configuracion.class);
		
		HashMap<String,Object> hSeries = Utils.List2Hash(seriesFondo, "IdSerieFondo");
		HashMap<String,Object> hConfiguraciones = Utils.List2Hash(configuracionesExistentes, "IdMoneda");
		
		for(Configuracion conf : entrada.p_FondoAdministracionLiquidez) {
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hSeries.get(conf.IdSerieFondo);
			if(serie == null) {
				out.P_OUT_ERRORESCONTADOR++;
				HashMap<String,String> valores = new HashMap<String,String>();
				valores.put("IdSerieFondo", conf.IdSerieFondo);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SERIE_NO_EXISTE",valores,con,entrada.p_IdEmpresa));
			}else {
				if(_c.NO.equals(serie.MoneyMarket)) {
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FONDO_NO_MONEY_MARKEY",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				}
				if(!serie.IdMoneda.equals(conf.IdMoneda)) {
					out.P_OUT_ERRORESCONTADOR++;
					HashMap<String,String> valores = new HashMap<String,String>();
					valores.put("IdMoneda", conf.IdMoneda);
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MONEDA_INCORRECTA",valores,con,entrada.p_IdEmpresa));
				}
			}
		}
		
		if(out.P_OUT_ERRORESCONTADOR.equals(0L)) {
			List<Configuracion> configuracionesInsertar = new ArrayList<Configuracion>();
			List<Configuracion> configuracionesActualizar = new ArrayList<Configuracion>();
			for(Configuracion conf : entrada.p_FondoAdministracionLiquidez) {
				conf.IdEmpresa = entrada.p_IdEmpresa;
				Configuracion configuracionExistente = (Configuracion) hConfiguraciones.get(conf.IdMoneda);
				if(configuracionExistente == null) {
					configuracionesInsertar.add(conf);
				}else {
					configuracionesActualizar.add(conf);
				}
			}
			
			sql.batch(con, SQL_INSERT_CONFIGURACIONES, configuracionesInsertar, Configuracion.class);
			sql.batch(con, SQL_UPDATE_CONFIGURACIONES, configuracionesActualizar, Configuracion.class);
			
			//agregamos log
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			entradaAgregarLog.p_IdTrabajo = 1L;
			List<AgregarLog.InAgregarLog.Log> logList = new ArrayList<>();
			AgregarLog.InAgregarLog.Log logProceso = new AgregarLog.InAgregarLog.Log();
			logProceso.IdTrabajo		= entradaAgregarLog.p_IdTrabajo; 
			logProceso.IdCabeceraLog	= 0L; 
			logProceso.IdTipoLog		= "INGRESO"; 
			logProceso.IdModulo			= entrada.p_IdModulo; 
			logProceso.IdUsuario		= entrada.p_IdUsuario; 
			logProceso.IdEmpresa		= entrada.p_IdEmpresa; 
			logProceso.FechaSistemaLog	= fechaSistema; 
			logProceso.IdUsuarioAtendido	= entrada.p_IdUsuario; 
			logProceso.IdCliente			= 0L; 
			logProceso.EstadoCliente		= ""; 
			logProceso.NombreCliente		= ""; 
			logProceso.IdTipoDocumentoIdentidad	= ""; 
			logProceso.IdDocumentoIdentidad	= ""; 
			logProceso.IdPortafolio			= 0L; 
			logProceso.EstadoPortafolio		= ""; 
			logProceso.IdTipoRegistro			= "CONFIGURAR FONDO ADMINISTRACION LIQUIDEZ"; 
			logProceso.IdRegistro				= "";
			logProceso.EstadoRegistro			= "ACT"; 
			logProceso.IdTipoOperacion			= ""; 
			logProceso.Folio					= 0L; 
			logProceso.Cantidad					= BigDecimal.ZERO; 
			logProceso.PrecioTasa				= BigDecimal.ZERO; 
			logProceso.Monto					= BigDecimal.ZERO; 
			logProceso.IdMonedaOrigen			= ""; 
			logProceso.IdMonedaLiquidacion		= ""; 
			logProceso.TipoCambio				= ""; 
			logProceso.Nemo						=""; 
			logProceso.IdProceso				= 0L; 
			logProceso.EstadoProceso			= "ACT"; 
			logProceso.NombreProceso			= "CONFIGURAR FONDO ADMINISTRACION LIQUIDEZ";
			logList.add(logProceso);
			
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(con, entradaAgregarLog);
		}
		
		out.P_OUT_RESULTADO.addAll(entrada.p_FondoAdministracionLiquidez);
		
		logger.info("FIN::ConfigurarFondoAdministracionLiquidez");
		
		return out;
	}
	
	public static void main(String[] args) throws Exception,SQLException {
		Connection conn = null;
		Gson gson = new Gson();
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getDESAConnection();
			//datos entrada
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_FondoAdministracionLiquidez\":[{\"IdMoneda\":\"CLP\",\"IdSerieFondo\":\"FMLIQUIDEZ_A\",\"MontoMinimo\":0,\"HoraEjecucion\":\"00:00:00\"}]}";
			EntradaConfigurarFondoAdministracionLiquidez objIn = gson.fromJson(json,EntradaConfigurarFondoAdministracionLiquidez.class);
			ConfigurarFondoAdministracionLiquidez demo = new ConfigurarFondoAdministracionLiquidez();
			SalidaConfigurarFondoAdministracionLiquidez ObjOut=demo.logica(conn, objIn);
			System.out.println(ObjOut.toString());
			conn.rollback();
			
		}catch (Exception e){
			e.printStackTrace();
			conn.rollback();
		}finally {
			conn.close();
		}
		System.out.println("== END ==");
	};
}
