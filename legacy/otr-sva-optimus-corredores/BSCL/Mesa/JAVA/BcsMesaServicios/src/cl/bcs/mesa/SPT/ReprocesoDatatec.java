package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.SPT.IngresoDatatec.DatosDatatecObjOut;
import cl.bcs.mesa.SPT.IngresoDatatec.OperacionSpotDatatec;
import cl.bcs.mesa.SPT.IngresoDatatec.ValidacionesConfiguracion;
import cl.bcs.mesa.SPT.IngresoDatatec.ValidacionesIngresoDatatec;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ReprocesoDatatec implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(ReprocesoDatatec.class);
	
	private final String SQL_SELECT_VALIDACIONES_CONFIGURACION = loadResourceToString("cl/bcs/mesa/SPT/sql/IngresoDatatec_ConsultarValidacionesConfiguracion.sql");
	private final String SQL_SELECT_OPERACIONES_SPOT_DATATEC = loadResourceToString("cl/bcs/mesa/SPT/sql/ReprocesoDatatec_ConsultarOperacionesSpotDatatec.sql");
	private final String SQL_UPDATE_DATATEC_VALIDACIONES = loadResourceToString("cl/bcs/mesa/SPT/sql/ReprocesoDatatec_UpdateDatatecValidaciones.sql");
	private final String SQL_SELECT_VALIDACIONES_REPROCESO_DATATEC = loadResourceToString("cl/bcs/mesa/SPT/sql/ReprocesoDatatec_ConsultarValidacionesReprocesoDatatec.sql");
	private final String SQL_UPDATE_DATATEC_ESTADO_GLOSA = loadResourceToString("cl/bcs/mesa/SPT/sql/IngresoDatatec_UpdateDatatecEstadoGlosa.sql");
	private final String SQL_SELECT_OPERACIONES_SPOT_DATATEC_ESTADO = loadResourceToString("cl/bcs/mesa/SPT/sql/IngresoDatatec_ConsultarOperacionesSpotDatatecEstado.sql");
	private final String SQL_UPDATE_DATATEC_DATOS_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/IngresoDatatec_UpdateDatatecDatosSpot.sql");
	private final String SQL_SELECT_OPERACIONES_SPOT_DATATEC_SALIDA = loadResourceToString("cl/bcs/mesa/SPT/sql/IngresoDatatec_ConsultarOperacionesSpotDatatecSalida.sql");
	private final String SQL_SELECT_OPERACIONES_SPOT_DATATEC_SALIDA_REPROCESO = loadResourceToString("cl/bcs/mesa/SPT/sql/ReprocesoDatatec_SelectOperacionDatatecSalida.sql");
	private final String SQL_UPDATE_OPERACION_SPOT_DATOS_DATATEC = loadResourceToString("cl/bcs/mesa/SPT/sql/IngresoDatatec_UpdateOperacionSpotDatosDatatec.sql");
	
	
	public static class ReprocesoDatatecEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_Fecha;
		public String p_IdEstado;
		public String p_Origen;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ReprocesoDatatecSalida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_ADVERTENCIASCONTADOR;
		public Long						P_OUT_ERRORCONFIGURACIONCONTADOR = 0L;
		public List<MensajeSalida>		P_OUT_ERRORCONFIGURACIONDETALLE;
		public List<DatosDatatecObjOut>	P_OUT_DATOSDATATEC;
		
		public ReprocesoDatatecSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORCONFIGURACIONDETALLE = new ArrayList<MensajeSalida>();
			this.P_OUT_DATOSDATATEC = new ArrayList<DatosDatatecObjOut>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ValidacionesReprocesoDatatec{
		public String IdUnicoDatatec;
		public Long NumeroRegistro;
		public Long NumeroIntentos;
		public Long IdCliente;
		public Long IdClienteValidacion;
		public String IdEstadoCliente;
		public Long IdPortafolio;
		public Long IdPortafolioValidacion;
		public String IdEstadoPortafolio;
		public java.sql.Timestamp FechaOperacion;
		public String HoraOperacion;
		public java.sql.Timestamp FechaReproceso;
		public String IdEstadoRegistro;
		public String GlosaProceso;
		public String InstitucionValidacion;
		public String UsuarioValidacion;
		public Long IdUsuarioCreador;
		public Long IdUsuarioValidacion;
		public String ClaseMensaje;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ReprocesoDatatecEntrada in = gson.fromJson(jin.toString(), ReprocesoDatatecEntrada.class);
		ReprocesoDatatecSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		long c = 0;
		for (MensajeSalida s : out.P_OUT_MENSAJES) {
			if ("AUTORIZACION".equals(s.tipo))
				c++;
		}
		out.P_OUT_ADVERTENCIASCONTADOR = c;
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ReprocesoDatatecSalida logica(Connection conn, ReprocesoDatatecEntrada in) throws Exception {
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		logger.info("Inicio servicio ReprocesoDatatec");
		
		SQL _sql = new SQL();
		ReprocesoDatatecSalida out = new ReprocesoDatatecSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		
		try {
			/***** VARIABLES *****/
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
		    Long idPaisSistema = empresa.IdPais;
			
			Long idMesa = 0L;
			Long idAgente = 0L;
			String tipoComprobanteFacturacion = "";
			String idMedioPago = "";
			String horaFinProceso = "";
			Long intentosDeReproceso = 0L;
			
			HashMap<String,Object> params = new HashMap<>();
			List<String> idUnicoDatatec = new ArrayList<String>();
			List<Long> numeroRegistro = new ArrayList<Long>();
			
			
			/***** VALIDACIONES *****/
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			
			List<ValidacionesConfiguracion> validacionesConfiguracion = _sql.query(conn, SQL_SELECT_VALIDACIONES_CONFIGURACION, params, ValidacionesConfiguracion.class);
			for(ValidacionesConfiguracion val: validacionesConfiguracion){
				if(val.IdMesa.longValue() == 0L){
					out.P_OUT_ERRORCONFIGURACIONDETALLE.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"FALTA CONFIGURAR MESA",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if("0".equals(val.IdAgente)){
					out.P_OUT_ERRORCONFIGURACIONDETALLE.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"FALTA CONFIGURAR AGENTE",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if("".equals(val.TipoComprobanteFacturacion)){
					out.P_OUT_ERRORCONFIGURACIONDETALLE.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"FALTA CONFIGURAR TIPO COMPROBANTE FACTURACION",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if("".equals(val.IdMedioPago)){
					out.P_OUT_ERRORCONFIGURACIONDETALLE.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"FALTA CONFIGURAR IDMEDIOPAGO",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if("".equals(val.NombreFormaDePago)){
					out.P_OUT_ERRORCONFIGURACIONDETALLE.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"FALTA CONFIGURAR FORMA DE PAGO",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if("".equals(val.HoraFinProceso)){
					out.P_OUT_ERRORCONFIGURACIONDETALLE.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"FALTA CONFIGURAR HORA FIN PROCESO",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if("0".equals(val.IntentosDeReproceso)){
					out.P_OUT_ERRORCONFIGURACIONDETALLE.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"FALTA CONFIGURAR INTENTOS DE REPROCESO",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				
				idMesa = val.IdMesa;
				idAgente = Long.parseLong(val.IdAgente);
				tipoComprobanteFacturacion = val.TipoComprobanteFacturacion;
				idMedioPago = val.IdMedioPago;
				horaFinProceso = val.HoraFinProceso;
				intentosDeReproceso = Long.parseLong(val.IntentosDeReproceso);
			}
			
			//Retorna si existen errores
			if(MensajeSalida.getNumeroErrores(out.P_OUT_ERRORCONFIGURACIONDETALLE) != 0L){
				out.P_OUT_ERRORCONFIGURACIONCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_ERRORCONFIGURACIONDETALLE);
				return out;
			}
			
			
			/***** REPROCESO *****/
			//Obtiene operaciones para Reproceso
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("FechaOperacion", in.p_Fecha);
			params.put("IdEstadoRegistro", in.p_IdEstado);
			params.put("Origen", in.p_Origen);
			params.put("IntentosDeReproceso", intentosDeReproceso);
			
			List<OperacionSpotDatatec> operacionesReproceso = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_DATATEC, params, OperacionSpotDatatec.class);
			
			//Valida si hay registros a Reprocesar
			if(operacionesReproceso.size() == 0){
				out.P_OUT_ERRORCONFIGURACIONDETALLE.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_HAY_REGISTROS_A_REPROCESAR",mapMsg,conn,in.p_IdEmpresa));
				out.P_OUT_ERRORCONFIGURACIONCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_ERRORCONFIGURACIONDETALLE);
				return out;
			}
			
			for(OperacionSpotDatatec ope: operacionesReproceso){
				idUnicoDatatec.add(ope.IdUnicoDatatec);
				numeroRegistro.add(ope.NumeroRegistro);
			}
			
			//Actualiza campos IdInstrumento, Cliente, Portafolio y CodigoUsuario para los registros que no pasaron la validacion
			_sql.batch(conn, SQL_UPDATE_DATATEC_VALIDACIONES, operacionesReproceso, OperacionSpotDatatec.class);
			
			
			//Validaciones para llenar campos de estado y glosa
			LocalDateTime fechaHoraFinProceso = LocalDateTime.parse(fechaSistema+" "+horaFinProceso, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
			List<String> fechaInicialList = new ArrayList<String>();
			List<FechasIn> fechasInList = new ArrayList<ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn>();
			
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdUnicoDatatec", idUnicoDatatec);
			//params.put("NumeroRegistro", numeroRegistro);
			logger.debug("Inicio SQL_SELECT_VALIDACIONES_REPROCESO_DATATEC");
			startTime = System.nanoTime();
			List<ValidacionesIngresoDatatec> validacionesIngresoDatatec = _sql.query(conn, SQL_SELECT_VALIDACIONES_REPROCESO_DATATEC, params, ValidacionesIngresoDatatec.class);
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_VALIDACIONES_REPROCESO_DATATEC");
			for(ValidacionesIngresoDatatec val: validacionesIngresoDatatec){
				val.GlosaProceso = "";
				boolean entraIf = false;
				
				//Cliente
				if(val.IdClienteValidacion.longValue() == 0L){
					val.IdEstadoRegistro = "ERR";
					val.GlosaProceso = "CLIENTE_NO_EXISTE";
					entraIf = true;
				}
				if("BLO".equals(val.IdEstadoCliente) || "PEN".equals(val.IdEstadoCliente)){
					val.IdEstadoRegistro = "ERR";
					String glosa = "BLO".equals(val.IdEstadoCliente) ? "CLIENTE_BLOQUEADO" : "PEN".equals(val.IdEstadoCliente) ? "CLIENTE_PENDIENTE_AUTORIZACION" : "";
					val.GlosaProceso = entraIf ? val.GlosaProceso + ", " + glosa : glosa;
					entraIf = true;
				}
				
				//Portafolio
				if(val.IdPortafolioValidacion.longValue() == 0L){
					val.IdEstadoRegistro = "ERR";
					val.GlosaProceso = entraIf ? val.GlosaProceso + ", PORTAFOLIO_NO_EXISTE" : "PORTAFOLIO_NO_EXISTE";
					entraIf = true;
				}
				if("BLO".equals(val.IdEstadoPortafolio) || "PEN".equals(val.IdEstadoPortafolio)){
					val.IdEstadoRegistro = "ERR";
					String glosa = "BLO".equals(val.IdEstadoPortafolio) ? "PORTAFOLIO_BLOQUEADO" : "PEN".equals(val.IdEstadoPortafolio) ? "PORTAFOLIO_PENDIENTE_AUTORIZACION" : "";
					val.GlosaProceso = entraIf ? val.GlosaProceso + ", " + glosa : glosa;
					entraIf = true;
				}
				
				//Usuario Creador
				if(val.IdUsuarioValidacion.longValue() == 0L){
					val.IdEstadoRegistro = "ERR";
					val.GlosaProceso = entraIf ? val.GlosaProceso + ", USUARIO_CREADOR_NO_EXISTE" : "USUARIO_CREADOR_NO_EXISTE";
					entraIf = true;
				}
				
				//Registro queda con estado ERR al superar el numero de Reintentos de Reproceso cuando el origen es el PROCESO
				if ("PROCESO".equals(in.p_Origen)) {
					val.NumeroIntentos++;
					if(val.NumeroIntentos > intentosDeReproceso) {
						val.IdEstadoRegistro = "ERR";
						val.GlosaProceso = entraIf ? val.GlosaProceso + ", EXCEDE_NUMERO_REINTENTOS" : "EXCEDE_NUMERO_REINTENTOS";
						entraIf = true;
					}
				}
				
				//Hora Procesamiento
				//if(!LocalDateTime.parse(Fecha.toStringDatetime(val.FechaReproceso), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).isBefore(fechaHoraFinProceso)){
				if(!LocalDateTime.parse(Fecha.toStringDatetime(val.FechaOperacion), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).isBefore(fechaHoraFinProceso)){
					val.IdEstadoRegistro = "PEN";
					val.GlosaProceso = entraIf ? val.GlosaProceso + ", EXCEDE_HORARIO" : "EXCEDE_HORARIO";
					entraIf = true;
					
					if (!fechaInicialList.contains(Fecha.toStringDate(val.FechaReproceso))) {
						fechaInicialList.add(Fecha.toStringDate(val.FechaReproceso));
						
						FechasIn fechas = new FechasIn();
						fechas.fechaInicial = Fecha.toStringDate(val.FechaReproceso);
						fechas.numeroDias = 1L;
						fechasInList.add(fechas);
					}
				}
				
				//Si pasa todas las validaciones, queda ING
				if (!entraIf) {
					val.IdEstadoRegistro = "ING";
					val.GlosaProceso = "Ingresado";
				}
			}
			
			//Obtiene dia habil siguiente para las operaciones que exceden horario
			if (fechasInList.size() > 0) {
				ObtenerDiaSiguiente diaSgte = new ObtenerDiaSiguiente();
				ObtenerDiaSiguiente.InObtenerDiaSiguiente diaSgteIn = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
				diaSgteIn.p_idPais = idPaisSistema;
				diaSgteIn.p_diaHabil = "S";
				diaSgteIn.p_fechas = fechasInList;
				
				ObtenerDiaSiguiente.OutObtenerDiaSiguiente salDiaSgte = diaSgte.logica(conn, diaSgteIn);
				HashMap<String, Object> salDiaSgteHash = Utils.List2Hash(salDiaSgte.p_out_fechas, "fechaInicial");
				
				for (ValidacionesIngresoDatatec val: validacionesIngresoDatatec) {
					if ("PEN".equals(val.IdEstadoRegistro)) {
						FechasOut fechasOut = (FechasOut) salDiaSgteHash.get(Fecha.toStringDate(val.FechaReproceso));
						if (Fecha.toStringDate(val.FechaReproceso).equals(fechasOut.fechaInicial)){
							val.FechaReproceso = Fecha.toDateTime(fechasOut.fechaCalculada + " " + val.HoraOperacion);
						}
					}
				}
			}
			
			//Actualiza campos de tabla intermedia: NumeroIntentos, IdEstadoRegistro, GlosaProceso, FechaReproceso
			if (validacionesIngresoDatatec.size() > 0) {
				_sql.batch(conn, SQL_UPDATE_DATATEC_ESTADO_GLOSA, validacionesIngresoDatatec, ValidacionesIngresoDatatec.class);
			}
			
			/***** CREAR OPERACIONES SPOT *****/
			//Obtiene operaciones en estado ING de tabla intermedia
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdUnicoDatatec", idUnicoDatatec);
			params.put("IdEstadoRegistro", "ING");
			params.put("ClaseMensaje", "5");
			
			List<OperacionSpotDatatec> operacionesDatatecSrvSpot = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_DATATEC_ESTADO, params, OperacionSpotDatatec.class);
			
			if(operacionesDatatecSrvSpot.size() > 0){
				CrearAnularOperacionSpotDatatec crearAnular = new CrearAnularOperacionSpotDatatec();
				CrearAnularOperacionSpotDatatec.CrearAnularOperacionSpotDatatecEntrada crearAnularEntrada = new CrearAnularOperacionSpotDatatec.CrearAnularOperacionSpotDatatecEntrada();
				crearAnularEntrada.p_IdEmpresa = in.p_IdEmpresa;
				crearAnularEntrada.p_IdUsuario = in.p_IdUsuario;
				crearAnularEntrada.p_IdModulo = in.p_IdModulo;
				crearAnularEntrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				crearAnularEntrada.p_Proceso = "REPROCESO";
				crearAnularEntrada.p_Accion = "CREAR";
				crearAnularEntrada.p_IdMesa = idMesa;
				crearAnularEntrada.p_TipoComprobanteFacturacion = tipoComprobanteFacturacion;
				crearAnularEntrada.p_IdAgente = idAgente;
				crearAnularEntrada.p_IdMedioPago = idMedioPago;
				crearAnularEntrada.p_DatosOperacionSpot = operacionesDatatecSrvSpot;
				CrearAnularOperacionSpotDatatec.CrearAnularOperacionSpotDatatecSalida crearAnularSalida = crearAnular.logica(conn, crearAnularEntrada);
				
				//Actualiza tabla intermedia con datos de la salida del servicio creacion spot
				_sql.batch(conn, SQL_UPDATE_DATATEC_DATOS_SPOT, crearAnularSalida.P_OUT_DATOSSALIDASPOT, DatosDatatecObjOut.class);
				
				//Actualiza Operaciones Spot creadas con el IdUsuarioCreador de la tabla intermedia
				_sql.batch(conn, SQL_UPDATE_OPERACION_SPOT_DATOS_DATATEC, crearAnularSalida.P_OUT_DATOSSALIDASPOT, DatosDatatecObjOut.class);
			}
			
			
			/***** ANULAR OPERACIONES SPOT *****/
			//Obtiene operaciones de tabla intermedia con campo ClaseMensaje = 50
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdUnicoDatatec", idUnicoDatatec);
			params.put("IdEstadoRegistro", Arrays.asList("ING", "PEN", "ERR", "LEI"));
			params.put("ClaseMensaje", "50");
			
			operacionesDatatecSrvSpot = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_DATATEC_ESTADO, params, OperacionSpotDatatec.class);
			
			if(operacionesDatatecSrvSpot.size() > 0){
				CrearAnularOperacionSpotDatatec crearAnular = new CrearAnularOperacionSpotDatatec();
				CrearAnularOperacionSpotDatatec.CrearAnularOperacionSpotDatatecEntrada crearAnularEntrada = new CrearAnularOperacionSpotDatatec.CrearAnularOperacionSpotDatatecEntrada();
				crearAnularEntrada.p_IdEmpresa = in.p_IdEmpresa;
				crearAnularEntrada.p_IdUsuario = in.p_IdUsuario;
				crearAnularEntrada.p_IdModulo = in.p_IdModulo;
				crearAnularEntrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				crearAnularEntrada.p_Proceso = "REPROCESO";
				crearAnularEntrada.p_Accion = "ANULAR";
				crearAnularEntrada.p_DatosOperacionSpot = operacionesDatatecSrvSpot;
				CrearAnularOperacionSpotDatatec.CrearAnularOperacionSpotDatatecSalida crearAnularSalida = crearAnular.logica(conn, crearAnularEntrada);
				
				//Actualiza tabla intermedia con datos de la salida del servicio Anulacion Spot
				_sql.batch(conn, SQL_UPDATE_DATATEC_DATOS_SPOT, crearAnularSalida.P_OUT_DATOSSALIDASPOT, DatosDatatecObjOut.class);
			}
			
			
			/***** SALIDA *****/
			//Retorna operaciones Datatec
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdUnicoDatatec", idUnicoDatatec);
			params.put("Origen", in.p_Origen);
			
			out.P_OUT_DATOSDATATEC = new ArrayList<DatosDatatecObjOut>(_sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_DATATEC_SALIDA_REPROCESO, params, DatosDatatecObjOut.class));
			out.P_OUT_DATOSDATATEC.sort(Comparator.comparing((DatosDatatecObjOut datosDatatecObjOut) -> datosDatatecObjOut.getNumeroRegistro()));
			
			
			/***** AGREGAR LOG *****/
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			for (OperacionSpotDatatec ope: operacionesReproceso) {
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdCabeceraLog = ope.IdOperacionSpot;
				log.IdTipoLog = "INGRESO";
				log.IdModulo = in.p_IdModulo;
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = fechaSistema;
				log.IdUsuarioAtendido = in.p_IdUsuario;
				log.IdCliente = ope.IdCliente;
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
				log.IdPortafolio = ope.IdPortafolio;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = "INGRESOOPERACIONSPOT";
				log.EstadoRegistro = ope.IdEstadoRegistro;
				log.IdTipoOperacion = ope.IdTipoOperacion;
				log.Folio = ope.IdOperacionSpot;
				log.Cantidad = BigDecimal.ZERO;
				log.PrecioTasa = BigDecimal.ZERO;
				log.Monto = ope.MontoMonedaSecundaria;
				log.IdMonedaOrigen = ope.IdMonedaPrincipal;
				log.IdMonedaLiquidacion = "";
				log.TipoCambio = ope.TipoCambioCierre.toString();
				log.Nemo = ope.IdInstrumento.toString();
				log.IdProceso = 0L;
				log.EstadoProceso = ope.IdEstadoRegistro;
				log.NombreProceso = "IngresoDatatec";						
				entradaAgregarLog.p_Log.add(log);
			}
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);
			
		}
		catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error(e);
			throw e;
		}
		
		logger.info("Termino del servicio ReprocesoDatatec, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonTest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"MSP\",\"p_UsaReglasNegocio\":\"S\",\"p_Fecha\":\"2018-10-19\",\"p_IdEstado\":0,\"p_Origen\":\"REPROCESO\"}";
			ReprocesoDatatecEntrada Objin = gson.fromJson(jsonTest, ReprocesoDatatecEntrada.class);
			
			ReprocesoDatatec demo = new ReprocesoDatatec();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ReprocesoDatatecSalida ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			finally {
				con.close();
			}
	    }
		System.out.println("== END ==");
	}
}