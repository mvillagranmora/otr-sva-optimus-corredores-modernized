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
import com.google.gson.GsonBuilder;
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

public class ModificarCalendarioPago implements JavaCall{
	static final Logger logger = LogManager.getLogger(ModificarCalendarioPago.class);
	
	private String SQL_INSERT_REGISTRO_CAMBIO_TEMPORAL = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/ModificarCalendarioPago_Insert_Registro_Cambio.sql");
	private String SQL_SELECT_REGISTRO_CAMBIO_TEMPORAL = "SELECT \"IdContrato\",\"CampoModificado\",\"ValorNuevo\" FROM \"BSCL.Normativa.BD::GBL.ModificarCalendarioPago_RegistroCambio\"";
	private String SQL_UPDATE_CALENDARIO_PAGO_TIPOFLUJO = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/ModificarCalendarioPago_Update_CalendarioPago_TipoFlujo.sql");
	private String SQL_UPDATE_CALENDARIO_PAGO_FACTORTASA = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/ModificarCalendarioPago_Update_CalendarioPago_FactorTasa.sql");
	private String SQL_UPDATE_CALENDARIO_PAGO_SPREADTASA = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/ModificarCalendarioPago_Update_CalendarioPago_SpreadTasa.sql");
	private String SQL_UPDATE_CALENDARIO_PAGO_TASAFIJA = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/ModificarCalendarioPago_Update_CalendarioPago_TasaFija.sql");
	private String SQL_UPDATE_CALENDARIO_PAGO_FECHAFIXTASA = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/ModificarCalendarioPago_Update_CalendarioPago_FechaFixTasa.sql");
	private String SQL_UPDATE_CALENDARIO_PAGO_FECHAINICIO = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/ModificarCalendarioPago_Update_CalendarioPago_FechaInicio.sql");
	private String SQL_UPDATE_CALENDARIO_PAGO_FECHATERMINO = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/ModificarCalendarioPago_Update_CalendarioPago_FechaTermino.sql");
	private String SQL_UPDATE_CALENDARIO_PAGO_MONTONOCIONAL = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/ModificarCalendarioPago_Update_CalendarioPago_MontoNocional.sql");
	private String SQL_UPDATE_CALENDARIO_PAGO_MONTOINTERCAMBIO = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/ModificarCalendarioPago_Update_CalendarioPago_MontoIntercambio.sql");
	private String SQL_UPDATE_CALENDARIO_PAGO_MONEDAMONTO = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/ModificarCalendarioPago_Update_CalendarioPago_MonedaMonto.sql");
	private String SQL_SELECT_REGISTRO_RECITFICA = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/Modificar_Select_Registro_Rectifica.sql");
	private String SQL_SELECT_REGISTRO_CAMPOS_ACTUAL = loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/ModificarCalendarioPago_Select_Registro_Campos_Actual.sql");

	public static class EntradaModificarCalendarioPago extends BcsPojo {
		public Long 		p_IdEmpresa;
		public Long			p_IdUsuario;
		public String 		p_IdModulo;
		public Long			p_NumeroFlujo;
		public List<RegistroCambio> p_RegistroCambio;
		
		public EntradaModificarCalendarioPago() {
			this.p_RegistroCambio = new ArrayList<RegistroCambio>();
		}
	}
	
	public static class RegistroCambio extends BcsPojo {
		public String 		RutContraparte;
		public String 		IdContrato;
		public Date 		FechaContrato;
		public String 		IdOperacion;
		public String 		Producto;
		public String 		PeriodoComercial;
		public Long 		NumeroFlujo;
		public Long 		IdUsuario;
		public String 		CampoModificado;
		public String 		ValorNuevo;
	};  
	
	
	public static class RegistroRectifica extends BcsPojo {
		public String 		IdContrato;
	}
	
	public static class RegistroCamposActual extends BcsPojo {
		public String		IdContrato;
		public String		S4_XR_TipoFlujo;
		public BigDecimal	S4_XR_FacTasa;
		public BigDecimal	S4_XR_SpreadTasa;
		public BigDecimal	S4_XR_TasaFija;
		public String		S4_XR_FecFixTasa;
		public String		S4_XR_FecIni;
		public String		S4_XR_FecTer;
		public BigDecimal	S4_XR_MontoNoc;
		public BigDecimal	S4_XR_MontoInt;
		public String		S4_X_MonMonto;
	}

	public static class SalidaModificarCalendarioPago extends BcsPojo {
		public Long 				P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 	P_OUT_MENSAJES;		
		public Long	 				P_OUT_REFID;
		
		public SalidaModificarCalendarioPago(){
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();			
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarCalendarioPago jin = gson.fromJson(in.toString(), EntradaModificarCalendarioPago.class);
		SalidaModificarCalendarioPago out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaModificarCalendarioPago logica(Connection con, EntradaModificarCalendarioPago entrada) throws Exception {
		logger.info("INICIO::EntradaModificarCalendarioPago");
		long inicioServicio = System.currentTimeMillis();
		SalidaModificarCalendarioPago out = new SalidaModificarCalendarioPago();
		
		SQL sql = new SQL();		

		 //Inserta en tabla global registro cambio
		if (entrada.p_RegistroCambio.size() > 0)
		{
			sql.batch(con, SQL_INSERT_REGISTRO_CAMBIO_TEMPORAL, entrada.p_RegistroCambio, RegistroCambio.class);			
		}
		else
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_REGISTRO_CAMBIO",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		// Selecciona marca Rectifica de los contratos a modificar
		List<Long> lstIdContrato = new ArrayList<>();
		HashMap<String,Object> params = new HashMap<>();
		for(RegistroCambio r : entrada.p_RegistroCambio) {
			if (!lstIdContrato.contains(Long.parseLong(r.IdContrato))) {
				lstIdContrato.add(Long.parseLong(r.IdContrato));
			}
		}
		params.put("KIdContrato",lstIdContrato);
		List<RegistroRectifica> registroRectifica = sql.query(con, SQL_SELECT_REGISTRO_RECITFICA, params, RegistroRectifica.class);
		
		// Selecciona los valores actuales
		List<RegistroCamposActual> registroCamposActual = sql.query(con, SQL_SELECT_REGISTRO_CAMPOS_ACTUAL, params, RegistroCamposActual.class);
		HashMap<String,Object> hashCamposActual = new HashMap<>();
		for (RegistroCamposActual r: registroCamposActual) {
			hashCamposActual.put(r.IdContrato, r);
		}
		

		// Obtiene datos de registro de cambio
		List<RegistroCambio> lstRegistroCambio = sql.query(con, SQL_SELECT_REGISTRO_CAMBIO_TEMPORAL, new HashMap<>(), RegistroCambio.class);
			
		if (lstRegistroCambio.size() == 0)
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"REGISTRO_CAMBIO_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}

		// Actualiza los campos de CALENDARIO PAGOS de acuerdo a los registros de cambio 
		modificarCampoCalendarioPago(con, sql, "S4_XR_TipoFlujo", lstRegistroCambio, SQL_UPDATE_CALENDARIO_PAGO_TIPOFLUJO);
		modificarCampoCalendarioPago(con, sql, "S4_XR_FacTasa", lstRegistroCambio, SQL_UPDATE_CALENDARIO_PAGO_FACTORTASA);
		modificarCampoCalendarioPago(con, sql, "S4_XR_SpreadTasa", lstRegistroCambio, SQL_UPDATE_CALENDARIO_PAGO_SPREADTASA);
		modificarCampoCalendarioPago(con, sql, "S4_XR_TasaFija", lstRegistroCambio, SQL_UPDATE_CALENDARIO_PAGO_TASAFIJA);		
		modificarCampoCalendarioPago(con, sql, "S4_XR_FecFixTasa", lstRegistroCambio, SQL_UPDATE_CALENDARIO_PAGO_FECHAFIXTASA);
		modificarCampoCalendarioPago(con, sql, "S4_XR_FecIni", lstRegistroCambio, SQL_UPDATE_CALENDARIO_PAGO_FECHAINICIO);
		modificarCampoCalendarioPago(con, sql, "S4_XR_FecTer", lstRegistroCambio, SQL_UPDATE_CALENDARIO_PAGO_FECHATERMINO);
		modificarCampoCalendarioPago(con, sql, "S4_XR_MontoNoc", lstRegistroCambio, SQL_UPDATE_CALENDARIO_PAGO_MONTONOCIONAL);
		modificarCampoCalendarioPago(con, sql, "S4_XR_MontoInt", lstRegistroCambio, SQL_UPDATE_CALENDARIO_PAGO_MONTOINTERCAMBIO);
		modificarCampoCalendarioPago(con, sql, "S4_X_MonMonto", lstRegistroCambio, SQL_UPDATE_CALENDARIO_PAGO_MONEDAMONTO);
		
		out.P_OUT_REFID = entrada.p_NumeroFlujo;
		
		// Registra LOG de cambios si el contrato estaba marcado para Rectificar
		agregarLog(con,entrada,lstRegistroCambio,registroRectifica,hashCamposActual);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarCalendarioPago", finServicio - inicioServicio);
		return out;
	}

	private void modificarCampoCalendarioPago(Connection con, SQL sql, String campoModificado,List<RegistroCambio> registroCambio, String queryUpdate) throws Exception {
		logger.debug("INICIO::ModificarCalendarioPago.modificarCampoCalendarioPago");
		long inicio = System.currentTimeMillis();
				
		List<RegistroCambio> registroCambioCalendarioPago = registroCambio.stream().filter(c -> c.CampoModificado.equals(campoModificado)).collect(Collectors.toList());

		if (!registroCambioCalendarioPago.isEmpty())
		{				
			List<RegistroCambio> paramsUpdateCalendarioPago = new ArrayList<RegistroCambio>();
			RegistroCambio objUpdateCalendarioPago = new RegistroCambio();			
			objUpdateCalendarioPago.CampoModificado = campoModificado;	
			paramsUpdateCalendarioPago.add(objUpdateCalendarioPago);			
			sql.batch(con, queryUpdate, paramsUpdateCalendarioPago, RegistroCambio.class);
		}
				
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarCalendarioPago.modificarCampoCalendarioPago", fin - inicio);
	}
	
	
	//-------------------------------------------------------------------------------------------
	// METODOS COMPLEMANTARIOS
	//-------------------------------------------------------------------------------------------
	
	private void agregarLog(Connection con, EntradaModificarCalendarioPago entrada, List<RegistroCambio> registroCambio, List<RegistroRectifica> registroRectifica,  HashMap<String,Object> registroCamposActual) throws Exception {
		logger.debug("INICIO::GrabarDetalle.agregarLog");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<Field> fields =  Arrays.asList(RegistroCamposActual.class.getDeclaredFields());
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		// filtra los registros rectificados
		List<RegistroCambio> rectificadosCambio = registroCambio.parallelStream().filter(registro -> {
			for (RegistroRectifica r: registroRectifica) {
				if (r.IdContrato.equals(registro.IdContrato))
					return true;
			}
			return false;
		}).collect(Collectors.toList());
		
		
		List<AgregarLog.InAgregarLog.DetalleLog> detalleLogValor = rectificadosCambio.parallelStream().map(solicitud->{
			String valorAntiguo =  "";
			for(Field f : fields) {
				if (solicitud.CampoModificado.equals(f.getName())) {
					RegistroCamposActual registroActual = (RegistroCamposActual) registroCamposActual.get(solicitud.IdContrato);
					try {
						valorAntiguo = String.valueOf(f.get(registroActual));
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
			
			AgregarLog.InAgregarLog.DetalleLog detalle = new AgregarLog.InAgregarLog.DetalleLog();
			detalle.IdCabeceraLog = Long.parseLong(solicitud.IdContrato,10);
			detalle.SubRegistro = solicitud.IdContrato;
			detalle.CampoRegistro = solicitud.CampoModificado;
			detalle.ValorAntiguo = valorAntiguo;
			detalle.ValorNuevo = solicitud.ValorNuevo;
			return detalle;
		}).collect(Collectors.toList());
		
		entradaAgregarLog.p_LogDetalle.addAll(detalleLogValor);
		
		entradaAgregarLog.p_Log = rectificadosCambio.parallelStream().map(solicitud->{
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = Long.parseLong(solicitud.IdContrato,10);
			log.IdTipoLog = "MODIFICACION";
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
			log.IdRegistro = solicitud.IdContrato;
			return log;
		}).collect(Collectors.toList());
		
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GrabarDetalle.agregarLog", fin - inicio);
	}
	

	// --------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// --------------------------------------------------	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"NOR\",\"p_NumeroFlujo\":0,\"p_RegistroCambio\":[{\"RutContraparte\":\"90947000-1\",\"IdContrato\":\"20693274\",\"FechaContrato\":\"2023-07-05\",\"IdOperacion\":\"0\",\"Producto\":\"FWD\",\"PeriodoComercial\":\"2023-07-19\",\"NumeroFlujo\":0,\"IdUsuario\":0,\"CampoModificado\":\"S4_XR_FacTasa\",\"ValorNuevo\":2},{\"RutContraparte\":\"90947000-1\",\"IdContrato\":\"20694163\",\"FechaContrato\":\"2023-07-05\",\"IdOperacion\":\"0\",\"Producto\":\"FWD\",\"PeriodoComercial\":\"2023-07-19\",\"NumeroFlujo\":0,\"IdUsuario\":0,\"CampoModificado\":\"S4_XR_TasaFija\",\"ValorNuevo\":1},{\"RutContraparte\":\"90947000-1\",\"IdContrato\":\"20694163\",\"FechaContrato\":\"2023-07-05\",\"IdOperacion\":\"0\",\"Producto\":\"FWD\",\"PeriodoComercial\":\"2023-07-19\",\"NumeroFlujo\":0,\"IdUsuario\":0,\"CampoModificado\":\"S4_XR_MontoInt\",\"ValorNuevo\":21}]}";
					
			EntradaModificarCalendarioPago Objin = gson.fromJson(json, EntradaModificarCalendarioPago.class);
			
			ModificarCalendarioPago demo = new ModificarCalendarioPago();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 		
 			try {
 				SalidaModificarCalendarioPago ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();
			}
			finally {
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}
}
