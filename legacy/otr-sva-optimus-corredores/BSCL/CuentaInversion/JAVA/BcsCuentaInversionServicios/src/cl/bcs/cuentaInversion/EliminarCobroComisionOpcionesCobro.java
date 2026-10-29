package cl.bcs.cuentaInversion;

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

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarCobroComisionOpcionesCobro implements JavaCall {
	private static final Logger logger = LogManager.getLogger(EliminarCobroComisionOpcionesCobro.class);

	private final String SQL_DELETE_OPCION_COBRO = loadResourceToString(
			"cl/bcs/cuentaInversion/sql/OpcionesCobroComisiones_DeleteRegistro.sql");

	public static class EntradaEliminarOpcionCobroComision extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<OpcionesCobro> p_Opciones;
		
		public EntradaEliminarOpcionCobroComision() {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "CTA";
			this.p_UsaReglasNegocio = "N";
			this.p_Opciones = new ArrayList<OpcionesCobro>();

		}

	}
	
	public static class OpcionesCobro extends BcsPojo{
		public Long IdOpcionCobro;
		
		public OpcionesCobro() {
			this.IdOpcionCobro = 0L;
		}

	}

	public static class SalidaEliminarOpcionCobroComision extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		
		public SalidaEliminarOpcionCobroComision () {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
		}
	}
	
	public static class BuscaOpciones {
		public Long Existe;
		
		public BuscaOpciones() {
			this.Existe = 0L;
		}
	}
	
	private void agregarLog(Connection con, List<OpcionesCobro> opciones, Long idEmpresa, Long idUsuario) throws Exception {
		logger.info("INICIO::EliminarCobroComisionOpcionesCobro.agregarLog");
		long inicio = System.currentTimeMillis();
		
		String fechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, 1L,"PLATAFORMA", "PLA.FechaSistema");
		
		AgregarLog agregarLogService = new AgregarLog();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 1L;
		
		for (OpcionesCobro opcion : opciones) {
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();

			log.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
			log.IdCabeceraLog = opcion.IdOpcionCobro;
			log.IdTipoLog = "ELIMINACION";
			log.IdModulo = "CTA";
			log.IdUsuario = idUsuario;
			log.IdEmpresa = idEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = idUsuario;
			log.IdCliente = 0L;
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = 0L;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "ELIMINACION_OPCION_COBRO_COMISION";
			log.EstadoRegistro = "";
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
			log.EstadoProceso = "";
			log.NombreProceso = "Eliminación de opción de cobro "+ opcion.IdOpcionCobro +" en menú de opciones de Cobro Comision Patrimonio";						
			entradaAgregarLog.p_Log.add(log);
		}
		
		agregarLogService.logica(con, entradaAgregarLog);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::EliminarCobroComisionOpcionesCobro.agregarLog", fin - inicio);
	}
	
	public SalidaEliminarOpcionCobroComision logica(Connection conn, EntradaEliminarOpcionCobroComision in) throws SQLException, Exception {
		logger.info("INICIO::EliminarCobroComisionOpcionesCobro");
		long inicio = System.currentTimeMillis();
		
		SQL sql=new SQL();
		SalidaEliminarOpcionCobroComision Salida = new SalidaEliminarOpcionCobroComision();
		List<OpcionesCobro> deleteOpciones = new ArrayList<OpcionesCobro>();

		if(in.p_Opciones.isEmpty()) {
			Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_ENTRADA_SIN_CONCEPTOS",new HashMap<String, String>(),conn,in.p_IdEmpresa));
			Salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(Salida.P_OUT_MENSAJES);
			return Salida;
		}
		
		sql.batch(conn, SQL_DELETE_OPCION_COBRO, in.p_Opciones, OpcionesCobro.class);
		
		agregarLog(conn, deleteOpciones, in.p_IdEmpresa, in.p_IdUsuario);

		long fin = System.currentTimeMillis();
		logger.info("FIN::EliminarCobroComisionOpcionesCobro.", fin - inicio);
		
		return Salida;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaEliminarOpcionCobroComision jin=gson.fromJson(in.toString() , EntradaEliminarOpcionCobroComision.class);
		SalidaEliminarOpcionCobroComision out = logica(con,jin);
		JsonParser parser = new JsonParser();
		JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01","connections.properties")) {
			Gson gson = Utils.getGsonBuilder();			
			String json = "{\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 33,\r\n"
					+ "  \"p_IdModulo\": \"CTA\",\r\n"
					+ "  \"p_UsaReglasNegocio\": \"N\",\r\n"
					+ "  \"p_Opciones\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdOpcionCobro\": 5000 \r\n"
					+ "    }\r\n"
					+ "  ]\r\n"
					+ "}";
			EntradaEliminarOpcionCobroComision Objin = gson.fromJson(json, EntradaEliminarOpcionCobroComision.class);
			EliminarCobroComisionOpcionesCobro demo = new EliminarCobroComisionOpcionesCobro();
			try {
				SalidaEliminarOpcionCobroComision salida = demo.logica(con, Objin);
				System.out.println(salida.toString());
				//con.commit();
				con.rollback();
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}

}
