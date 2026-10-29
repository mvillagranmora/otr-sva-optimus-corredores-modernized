package cl.bcs.plataforma.mer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.mer.pojo.DeterioroEmisor;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class GenerarLogDeterioroEmisor implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(GenerarLogDeterioroEmisor.class);

	public static class GenerarLogDeterioroEmisor_Entrada extends BcsPojo {
		public String TipoLog;
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
	    public String p_IdModulo;
	    public String p_UsaReglasNegocio;
		public Map<String,CopiaDeterioro> mapRegistrosOriginales;
		public List<DeterioroEmisor> registros;
		
		public GenerarLogDeterioroEmisor_Entrada() {
			this.registros = new ArrayList<DeterioroEmisor>();
			this.mapRegistrosOriginales = new HashMap<>();
		}
	}
	
	public static class GenerarLogDeterioroEmisor_Salida extends BcsPojo {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
	
		public GenerarLogDeterioroEmisor_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	public static class CopiaDeterioro {
		public Long IdDeterioroEmisor;
		public String FlagDeterioro;
		public String Emisor;
		public BigDecimal PorcentajeDeterioro;
		public Integer Fase;
		public String ValorAntiguo;
		public String ValorNuevo;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		return null;
	}
	
	public GenerarLogDeterioroEmisor_Salida logica(Connection con, GenerarLogDeterioroEmisor_Entrada entrada) throws Exception {
		
		logger.info("INICIO::LogDeterioroEmisor");
		Long inicioServicio = System.currentTimeMillis();
		
		GenerarLogDeterioroEmisor_Salida out = new GenerarLogDeterioroEmisor_Salida();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaAgregarLog.p_IdUsuario = entrada.p_IdUsuario;
		entradaAgregarLog.p_IdModulo  = entrada.p_IdModulo;
		
		CopiaDeterioro deterioroPorDefecto = new CopiaDeterioro();
		deterioroPorDefecto.IdDeterioroEmisor = 1L;
		deterioroPorDefecto.FlagDeterioro = "";
		deterioroPorDefecto.PorcentajeDeterioro = BigDecimal.ZERO;
		deterioroPorDefecto.Fase = 0;
		
		for (DeterioroEmisor deterioro : entrada.registros) {
			CopiaDeterioro registroOriginal = entrada.mapRegistrosOriginales.getOrDefault(deterioro.Emisor, deterioroPorDefecto);
			entradaAgregarLog.p_Log.add(generarCabeceraLog(deterioro, entrada, fechaSistema));
			entradaAgregarLog.p_LogDetalle.addAll(generarDetalleLog(deterioro, registroOriginal));	
		}
		
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::LogDeterioroEmisor", finServicio - inicioServicio);
		return out;
	}
	
	
	private AgregarLog.InAgregarLog.Log generarCabeceraLog(DeterioroEmisor deterioro, GenerarLogDeterioroEmisor_Entrada entrada, String fechaSistema) {
		
		logger.trace("INICIO::LogDeterioroEmisor.generarCabeceraLog");
		Long inicioMetodo = System.currentTimeMillis();

		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		log.IdTrabajo = 1L;
		log.IdCabeceraLog = deterioro.IdDeterioroEmisor;
		log.IdTipoLog =  ("INA".equals(deterioro.IdEstado)) ? "ELIMINACION" :entrada.TipoLog;
		log.IdModulo = entrada.p_IdModulo;
		log.IdUsuario = entrada.p_IdUsuario;
		log.IdEmpresa = entrada.p_IdEmpresa;
		log.FechaSistemaLog = fechaSistema;
		log.IdTipoRegistro = "DETERIORO";
		
		Long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::LogDeterioroEmisor.generarCabeceraLog :", finMetodo - inicioMetodo);
		return log;

	}
		
	private List<AgregarLog.InAgregarLog.DetalleLog> generarDetalleLog(DeterioroEmisor deterioro, CopiaDeterioro copia) {
		
		logger.trace("INICIO::LogDeterioroEmisor.generarDetalleLog");
		Long inicioMetodo = System.currentTimeMillis();
		
		List<AgregarLog.InAgregarLog.DetalleLog> DetalleLog = new ArrayList<>();

		if (!deterioro.FlagDeterioro.equals(copia.FlagDeterioro) || "INA".equals(deterioro.IdEstado)) {
			AgregarLog.InAgregarLog.DetalleLog flagLog = new AgregarLog.InAgregarLog.DetalleLog();
			flagLog.SubRegistro = deterioro.Emisor;
			flagLog.CampoRegistro = "Flag Deterioro";
			flagLog.IdCabeceraLog = deterioro.IdDeterioroEmisor;
			flagLog.ValorAntiguo = copia.FlagDeterioro;
			flagLog.ValorNuevo = "INA".equals(deterioro.IdEstado) ? "" : deterioro.FlagDeterioro;
			DetalleLog.add(flagLog);
		}
		
		if (deterioro.PorcentajeDeterioro.compareTo(copia.PorcentajeDeterioro) != 0 || "INA".equals(deterioro.IdEstado)) {
			AgregarLog.InAgregarLog.DetalleLog porcentajeLog = new AgregarLog.InAgregarLog.DetalleLog();
			porcentajeLog.SubRegistro = deterioro.Emisor;
			porcentajeLog.CampoRegistro = "Porcentaje Deterioro";
			porcentajeLog.IdCabeceraLog = deterioro.IdDeterioroEmisor;
			porcentajeLog.ValorAntiguo = copia.PorcentajeDeterioro.toString();
			porcentajeLog.ValorNuevo = "INA".equals(deterioro.IdEstado) ? "" : deterioro.PorcentajeDeterioro.toString();
			DetalleLog.add(porcentajeLog);
		}
		
		if (!deterioro.Fase.equals(copia.Fase) || "INA".equals(deterioro.IdEstado)) {
			AgregarLog.InAgregarLog.DetalleLog faseLog = new AgregarLog.InAgregarLog.DetalleLog();
			faseLog.SubRegistro = deterioro.Emisor;
			faseLog.CampoRegistro = "Fase";
			faseLog.IdCabeceraLog = deterioro.IdDeterioroEmisor;
			faseLog.ValorAntiguo = copia.Fase.toString();
			faseLog.ValorNuevo = "INA".equals(deterioro.IdEstado) ? "" : deterioro.Fase.toString();
			DetalleLog.add(faseLog);
		}

		Long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::LogDeterioroEmisor.generarDetalleLog :", finMetodo - inicioMetodo);
		return DetalleLog;

	}
	
	public static void main(String[] args) throws Exception {
		// Prueba de la clase
		try {
			//Connection con2 = PlataformaUtils.getPropertiesConnection(SQL_SELECT_DETERIORO_EMISOR)
			Connection con = PlataformaUtils.getDESAConnection();
			try {
				Gson gson = Utils.getGsonBuilder();

				String jsonCrear = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\\\":\"N\" ,"
						+ " \"p_DeteriorosEmisores\": [{\"IdDeterioroEmisor\":33, \"Emisor\": \"AGUAS\", \"FlagDeterioro\": \"N\", \"PorcentajeDeterioro\": \"10\", \"Fase\": \"2\", \"Accion\": \"MODIFICAR\"},"
						+ "{\"IdDeterioroEmisor\": 18, \"Emisor\": \"BCI\", \"FlagDeterioro\": \"N\", \"PorcentajeDeterioro\": \"27.8\", \"Fase\": \"2\", \"Accion\": \"MODIFICAR\"}]}";
				GenerarLogDeterioroEmisor_Entrada entradaCrear = gson.fromJson(jsonCrear,GenerarLogDeterioroEmisor_Entrada.class);

				GenerarLogDeterioroEmisor demo = new GenerarLogDeterioroEmisor();

				GenerarLogDeterioroEmisor_Salida salidaModificar = demo.logica(con, entradaCrear);
				System.out.println(salidaModificar.toString());

				con.rollback();
				//con.commit();

			} catch (Exception e) {
				con.rollback();
				e.printStackTrace();
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("== END ==");
	}


}
