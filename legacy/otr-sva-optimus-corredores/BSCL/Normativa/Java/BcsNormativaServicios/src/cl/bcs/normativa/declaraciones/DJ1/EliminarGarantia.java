package cl.bcs.normativa.declaraciones.DJ1;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

public class EliminarGarantia implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(EliminarGarantia.class);
	
	private static final List<String> CAMPOS = Arrays.asList("S6_TipoGar", "S6_MonVal", "KS6_IdGar", "S6_MontoUmb", "S6_ValGtia");
	
	public final String SQL_DELETE_GARANTIA = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/EliminarGarantia_Delete_Garantia.sql");
	public final String SQL_INSERT_REGISTRO_ELIMINAR_TEMPORAL = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/EliminarGarantia_Insert_Registro_Eliminar_Temporal.sql");
	public final String SQL_SELECT_REGISTRO_CAMPOS_ACTUAL = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/EliminarGarantia_Select_Registro_Campos_Actual.sql");
	public final String SQL_SELECT_REGISTRO_RECITFICA = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/Modificar_Select_Registro_Rectifica.sql");
	
	public static class EntradaEliminarGarantia {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<GarantiaEntrada> p_Garantias;
		
		public EntradaEliminarGarantia() {
			this.p_Garantias = new ArrayList<GarantiaEntrada>();
		}
	}
	
	public class GarantiaEntrada {
		public String KRutCtraparte1;
		public String KIdContrato;
		public Date KFechaContrato;
		public String KIdOpeEstruc;
		public String kProducto;
		public String kPeriodoComercial;
		public String KS6_IdGar;
	}
	
	public static class RegistroRectifica extends BcsPojo {
		public String 		IdContrato;
	}

	public static class RegistroCamposActual extends BcsPojo {
		public String		KRutCtraparte1;
		public String		KIdContrato;
		public Date			KFechaContrato;
		public String		KIdOpeEstruc;
		public String		kProducto;
		public String		kPeriodoComercial;
		public String		S6_TipoGar;
		public String		S6_MonVal;
		public String		KS6_IdGar;
		public BigDecimal	S6_MontoUmb;
		public BigDecimal	S6_ValGtia;
	}
	
	public static class SalidaEliminarGarantia extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaEliminarGarantia() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new  ArrayList<MensajeSalida>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaEliminarGarantia jin = gson.fromJson(in.toString(), EntradaEliminarGarantia.class);
		SalidaEliminarGarantia out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaEliminarGarantia logica(Connection con, EntradaEliminarGarantia entrada) throws Exception {
		logger.info("INICIO::EliminarGarantia");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		SalidaEliminarGarantia out = new SalidaEliminarGarantia();
		
		//Inserta en tabla global registro cambio
		if (entrada.p_Garantias.size() > 0)
		{
			sql.batch(con, SQL_INSERT_REGISTRO_ELIMINAR_TEMPORAL, entrada.p_Garantias, GarantiaEntrada.class);			
		}
		else
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_REGISTRO_CAMBIO",null,con,entrada.p_IdEmpresa));
			return out;
		}

		// Selecciona marca Rectifica de los contratos
		List<Long> lstIdContrato = new ArrayList<>();
		HashMap<String,Object> params = new HashMap<>();
		for(GarantiaEntrada r : entrada.p_Garantias) {
			if (!lstIdContrato.contains(Long.parseLong(r.KIdContrato))) {
				lstIdContrato.add(Long.parseLong(r.KIdContrato));
			}
		}
		params.put("KIdContrato",lstIdContrato);
		List<RegistroRectifica> registroRectifica = sql.query(con, SQL_SELECT_REGISTRO_RECITFICA, params, RegistroRectifica.class);

		// Selecciona los valores actuales
		List<RegistroCamposActual> registroCamposActual = sql.query(con, SQL_SELECT_REGISTRO_CAMPOS_ACTUAL, params, RegistroCamposActual.class);

		// Filtra los registros rectificados
		List<RegistroCamposActual> rectificadosElimina = registroCamposActual.parallelStream().filter(registro -> {
			for (RegistroRectifica r: registroRectifica) {
				if (r.IdContrato.equals(registro.KIdContrato))
					return true;
			}
			return false;
		}).collect(Collectors.toList());
		
		int contador = entrada.p_Garantias.size();
		try {
			if(contador > 0) {
				sql.batch(con, SQL_DELETE_GARANTIA, entrada.p_Garantias, GarantiaEntrada.class);	
				String mensaje = contador > 1 ? "Se eliminaron "+ contador +" registros." : "El registro fue eliminado.";
				out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_OK,"UI_PROCESO_EJECUTADO_EXITO", mensaje));
			}
			
			// Registra LOG si el contrato estaba marcado para Rectificar
			agregarLog(con,entrada,registroRectifica,rectificadosElimina);
		}catch(Exception e) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_EJECUTADO_ERROR","No se puedieron eliminar los "+ contador + " registros."));
			e.printStackTrace();
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		long fin = System.currentTimeMillis();
		logger.info("FIN::EliminarGarantia", fin - inicio);
		return out;
	}
	
	//-------------------------------------------------------------------------------------------
	// METODOS COMPLEMANTARIOS
	//-------------------------------------------------------------------------------------------

	private void agregarLog(Connection con, EntradaEliminarGarantia entrada, List<RegistroRectifica> registroRectifica, List<RegistroCamposActual> registroCamposActual) throws Exception {
		logger.debug("INICIO::GrabarDetalle.agregarLog");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<Field> fields =  Arrays.asList(RegistroCamposActual.class.getDeclaredFields());
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		List<AgregarLog.InAgregarLog.DetalleLog> detalleLogValor = new ArrayList<AgregarLog.InAgregarLog.DetalleLog>();
		for(RegistroCamposActual r: registroCamposActual) {
			for(Field f : fields) {
				if (CAMPOS.contains(f.getName())) {
					AgregarLog.InAgregarLog.DetalleLog detalle = new AgregarLog.InAgregarLog.DetalleLog();
					detalle.IdCabeceraLog = Long.parseLong(r.KIdContrato,10);
					detalle.SubRegistro = r.KIdContrato;
					detalle.CampoRegistro = f.getName();
					detalle.ValorAntiguo = String.valueOf(f.get(r));
					detalle.ValorNuevo =  "";
					detalleLogValor.add(detalle);
				}
			}
		}
		
		entradaAgregarLog.p_LogDetalle.addAll(detalleLogValor);
		
		entradaAgregarLog.p_Log = registroCamposActual.parallelStream().map(solicitud->{
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = Long.parseLong(solicitud.KIdContrato,10);
			log.IdTipoLog = "ELIMINACION";
			log.IdModulo = "TRI";
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
			log.IdTipoRegistro = "RECTIFICACION";
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
			log.NombreProceso = "Cambios en contrato SIID marcado para Rectificar";
			log.IdRegistro = solicitud.KIdContrato;
			return log;
		}).collect(Collectors.toList());
		
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GrabarDetalle.agregarLog", fin - inicio);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"NOR\",\"p_Garantias\":[{\"KRutCtraparte1\":\"90947000-1\",\"KIdContrato\":\"20694163\",\"KFechaContrato\":\"2023-07-05\",\"KIdOpeEstruc\":\"0\",\"kProducto\":\"FWD\",\"kPeriodoComercial\":\"2023-07-19\",\"KS6_IdGar\":\"7\"},{\"KRutCtraparte1\":\"90947000-1\",\"KIdContrato\":\"20694163\",\"KFechaContrato\":\"2023-07-05\",\"KIdOpeEstruc\":\"0\",\"kProducto\":\"FWD\",\"kPeriodoComercial\":\"2023-07-19\",\"KS6_IdGar\":\"6\"}]}";
			EntradaEliminarGarantia Objin = gson.fromJson(json, EntradaEliminarGarantia.class);
			EliminarGarantia demo = new EliminarGarantia();				
			try {
				SalidaEliminarGarantia ObjOut=demo.logica(con, Objin);
				System.out.println(ObjOut.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback(); 
			}
			finally {
				con.rollback(); 			
	    	}
			System.out.println("== END ==");
		}
	}
	
}
