package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class IngresoDatatec implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(IngresoDatatec.class);
	
	private final String SQL_INSERT_OPERACIONES_SPOT_DATATEC = loadResourceToString("cl/bcs/mesa/SPT/sql/IngresoDatatec_InsertOperacionesSpotDatatec.sql");
	private final String SQL_SELECT_INSTRUMENTO_MONEDA_MESA = loadResourceToString("cl/bcs/mesa/sql/ConsultarInstrumentoMonedaMesa.sql");
	private final String SQL_SELECT_INDICADORES_CLIENTE = loadResourceToString("cl/bcs/mesa/sql/ConsultarIndicadoresCliente.sql");
	private final String SQL_SELECT_INDICADORES_USUARIO = loadResourceToString("cl/bcs/mesa/sql/ConsultarIndicadoresUsuario.sql");
	private final String SQL_SELECT_VALIDACIONES_CONFIGURACION = loadResourceToString("cl/bcs/mesa/SPT/sql/IngresoDatatec_ConsultarValidacionesConfiguracion.sql");
	private final String SQL_SELECT_OPERACIONES_SPOT_DATATEC_EXISTEN = loadResourceToString("cl/bcs/mesa/SPT/sql/IngresoDatatec_ConsultarOperacionesSpotDatatecExisten.sql");
	private final String SQL_SELECT_VALIDACIONES_INGRESO_DATATEC = loadResourceToString("cl/bcs/mesa/SPT/sql/IngresoDatatec_ConsultarValidacionesIngresoDatatec.sql");
	private final String SQL_UPDATE_DATATEC_ESTADO_GLOSA = loadResourceToString("cl/bcs/mesa/SPT/sql/IngresoDatatec_UpdateDatatecEstadoGlosa.sql");
	private final String SQL_SELECT_OPERACIONES_SPOT_DATATEC_ESTADO = loadResourceToString("cl/bcs/mesa/SPT/sql/IngresoDatatec_ConsultarOperacionesSpotDatatecEstado.sql");
	private final String SQL_UPDATE_DATATEC_DATOS_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/IngresoDatatec_UpdateDatatecDatosSpot.sql");
	private final String SQL_SELECT_OPERACIONES_SPOT_DATATEC_SALIDA = loadResourceToString("cl/bcs/mesa/SPT/sql/IngresoDatatec_ConsultarOperacionesSpotDatatecSalida.sql");
	private final String SQL_UPDATE_OPERACION_SPOT_DATOS_DATATEC = loadResourceToString("cl/bcs/mesa/SPT/sql/IngresoDatatec_UpdateOperacionSpotDatosDatatec.sql");
	private final String SQL_SELECT_CLIENTE_PORTAFOLIO = loadResourceToString("cl/bcs/mesa/SPT/sql/IngresoDatatec_ConsultarClientePortafolio.sql");
	private final String SQL_SELECT_OPERACIONES_SPOT_DATATEC_ANULACION	= loadResourceToString("cl/bcs/mesa/SPT/sql/IngresoDatatec_ConsultarOperacionesSpotDatatecExistenAnulacion.sql");

	public static class DatosDatatecObjIn{
		public String IDDATATEC;
		public String HORA;
		public String FECHA;
		public String CLASEMENSAJE;
		public String OD;
		public String INSTITUCIONPOSTURA;
		public String CIUDADPOSTURA;
		public String USUARIOPOSTURA;
		public String INSTITUCIONACEPTANTE;
		public String CIUDADACEPTANTE;
		public String USUARIOACEPTANTE;
		public String MERCADO;
		public String MONEDA;
		public String PRECIO;
		public String NUMERO;
		public String NUMEROTRANSADO;
		public String DIAS;
		public String BANDERA;
		public String CONTADOR;
		public String NUMERODELIQUIDACION;
		public String FACTORIVA;
		public String BANDERAFORMAPAGO;
		public String BANDERAMETODO;
		public String IDOPERACION;
		public String VALORPUNTACOMPRA;
		public String VALORPUNTAVENTA;
		public String MARGEN;
		public String MONTOPUNTA;
		public String MensajeValidacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class IngresoDatatecEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<DatosDatatecObjIn> p_DatosDatatec;
		
		public IngresoDatatecEntrada() {
			this.p_DatosDatatec = new ArrayList<DatosDatatecObjIn>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosDatatecObjOut{
		public String IdUnicoDatatec;
		public Long NumeroRegistro;
		public Long IdOperacionSpot;
		public String IdDatatec;
		public String IdEstadoRegistro;
		public String GlosaProceso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public Long getNumeroRegistro() {
			return NumeroRegistro;
		}
	}
	
	public static class IngresoDatatecSalida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_ADVERTENCIASCONTADOR;
		public Long						P_OUT_ERRORCONFIGURACIONCONTADOR = 0L;
		public List<MensajeSalida>		P_OUT_ERRORCONFIGURACIONDETALLE;
		public List<DatosDatatecObjOut>	P_OUT_DATOSDATATEC;
		
		public IngresoDatatecSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORCONFIGURACIONDETALLE = new ArrayList<MensajeSalida>();
			this.P_OUT_DATOSDATATEC = new ArrayList<DatosDatatecObjOut>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionSpotDatatec{
		public String IdUnicoDatatec;
		public Long NumeroRegistro;
		public Long NumeroIntentos;
		public String IdEstadoRegistro;
		public String GlosaProceso;
		public Long IdEmpresa;
		public Long IdOperacionSpot;
		public java.sql.Timestamp FechaOperacion;
		public Long IdCliente;
		public Long IdPortafolio;
		public String NombreCliente;
		public Long IdInstrumento;
		public String IdTipoOperacion;
		public String IdMonedaPrincipal;	
		public String IdMonedaSecundaria;
		public String IdMonedaLocal;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal TipoCambioCierre;
		public BigDecimal MontoMonedaSecundaria;
		public BigDecimal TipoCambioCosto;
		public String IdFormaPagoRecibo;
		public java.sql.Timestamp FechaValutaRecibo;
		public String IdFormaPagoEntrego;
		public java.sql.Timestamp FechaValutaEntrego;
		public String Origen = "DATATEC";
		public Long IdUsuarioCreador;
		public java.sql.Timestamp FechaCreacion;
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public String IdDatatec;
		public String InstitucionValidacion;
		public String UsuarioValidacion;
		public String ClaseMensaje;
		public java.sql.Timestamp FechaReproceso;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class InstrumentoMonedaMesa{
		public String IdMonedaPrincipal;
		public Long IdInstrumento;
		public String Nemotecnico;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class IndicadorCliente{
		public String ValorIndicador;
		public Long IdCliente;
		public Long IdPortafolio;
		public String NombreCompleto;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class IndicadorUsuario{
		public String ValorIndicador;
		public Long IdEmpleado;
		public Long IdUsuario;
		public String CodigoUsuario;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class ValidacionesConfiguracion{
		public Long IdMesa;
		public String IdAgente;
		public String TipoComprobanteFacturacion;
		public String IdMedioPago;
		public String NombreFormaDePago;
		public String HoraFinProceso;
		public String IntentosDeReproceso;
		public String RazonSocialDatatec;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class ValidacionesIngresoDatatec{
		public String IdUnicoDatatec;
		public Long NumeroRegistro;
		public Long NumeroIntentos;
		public Long IdClienteValidacion;
		public String IdEstadoCliente;
		public Long IdPortafolioValidacion;
		public String IdEstadoPortafolio;
		public java.sql.Timestamp FechaOperacion;
		public String HoraOperacion;
		public java.sql.Timestamp FechaReproceso;
		public String IdEstadoRegistro;
		public String GlosaProceso;
		public Long IdUsuarioCreador;
		public Long IdUsuarioValidacion;
		public String ClaseMensaje;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class UpdateDatatecDatosSpot{
		public String IdUnicoDatatec;
		public Long NumeroRegistro;
		public String IdEstadoRegistro;
		public String GlosaProceso;
		public Long IdOperacionSpot;
		public String IdMonedaLocal;
		public String IdFormaPagoRecibo;
		public java.sql.Timestamp FechaValutaRecibo;
		public String IdFormaPagoEntrego;
		public java.sql.Timestamp FechaValutaEntrego;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosClientesEntrada{
		public Long IdCliente;
		public Long IdPortafolio;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosClientesSalida{
		public String DocumentoIdentidad;
		public String CodigoPropositoPortafolio;
		public Long IdCliente;
		public Long IdPortafolio;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		IngresoDatatecEntrada in = gson.fromJson(jin.toString(), IngresoDatatecEntrada.class);
		IngresoDatatecSalida out = logica(con, in);
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
	
	public IngresoDatatecSalida logica(Connection conn, IngresoDatatecEntrada in) throws Exception {
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		logger.info("Inicio servicio IngresoDatatec");
		
		SQL _sql = new SQL();
		IngresoDatatecSalida out = new IngresoDatatecSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		
		try {
			if(in.p_DatosDatatec.size() == 0){
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_ENVIARON_REGISTROS",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				return out;
			}
			
			
			/***** VARIABLES *****/
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			java.sql.Timestamp fechaHoraSistemaTimestamp = FechaDB.ObtenerFechaHoraSistemaTs(conn,in.p_IdEmpresa);
			
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
			String nombreCorredora="";
		    Long idPaisSistema = empresa.IdPais;

			Long idMesa = 0L;
			Long idAgente = 0L;
			String tipoComprobanteFacturacion = "";
			String idMedioPago = "";
			String horaFinProceso = "";
			
			HashMap<String,Object> params = new HashMap<>();
			ArrayList<OperacionSpotDatatec> operacionesSpotDatatec = new ArrayList<>();
			
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			List<ValidacionesConfiguracion> validacionesConfiguracion = _sql.query(conn, SQL_SELECT_VALIDACIONES_CONFIGURACION, params, ValidacionesConfiguracion.class);
			for(ValidacionesConfiguracion validacionDatatec : validacionesConfiguracion) {
				nombreCorredora = validacionDatatec.RazonSocialDatatec;
			}
			
			
			List<String> numeroOperacion = new ArrayList<String>();
			List<String> idDatatec = new ArrayList<String>();
			List<String> claseMensaje = new ArrayList<String>();
			List<String> moneda = new ArrayList<String>();
			List<String> institucionPosturaOAceptante = new ArrayList<String>();
			List<String> usuarioPosturaOAceptante = new ArrayList<String>();
			for(DatosDatatecObjIn dat: in.p_DatosDatatec){
				numeroOperacion.add(dat.CONTADOR);
				idDatatec.add(dat.IDDATATEC);
				claseMensaje.add(dat.CLASEMENSAJE);
				
				if (!moneda.contains(dat.MONEDA)) moneda.add(dat.MONEDA);
				
				String institucion = dat.INSTITUCIONPOSTURA.equals(nombreCorredora) ? dat.INSTITUCIONACEPTANTE : dat.INSTITUCIONPOSTURA;
				if (!institucionPosturaOAceptante.contains(institucion)) institucionPosturaOAceptante.add(institucion);
				
				String usuario = dat.INSTITUCIONPOSTURA.equals(nombreCorredora) ? dat.USUARIOPOSTURA : dat.USUARIOACEPTANTE;
				if (!usuarioPosturaOAceptante.contains(usuario)) usuarioPosturaOAceptante.add(usuario.toUpperCase());
			}
			
			//Consulta Instrumento Moneda
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdMonedaPrincipal", moneda);
			List<InstrumentoMonedaMesa> instrumentoMonedaMesaList = _sql.query(conn, SQL_SELECT_INSTRUMENTO_MONEDA_MESA, params, InstrumentoMonedaMesa.class);
			HashMap<String, Object> instrumentoMonedaMesaHash = Utils.List2Hash(instrumentoMonedaMesaList, "IdMonedaPrincipal");
			
			//Consulta Indicadores Cliente
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("ValorIndicador", institucionPosturaOAceptante);
			List<IndicadorCliente> indicadorClienteList = _sql.query(conn, SQL_SELECT_INDICADORES_CLIENTE, params, IndicadorCliente.class);
			HashMap<String, Object> indicadorClienteHash = Utils.List2Hash(indicadorClienteList, "ValorIndicador");
			
			//Consulta Indicadores para Usuario
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("ValorIndicador", usuarioPosturaOAceptante);
			List<IndicadorUsuario> indicadorUsuarioList = _sql.query(conn, SQL_SELECT_INDICADORES_USUARIO, params, IndicadorUsuario.class);
			HashMap<String, Object> indicadorUsuarioHash = Utils.List2Hash(indicadorUsuarioList, "ValorIndicador");
			
			
			/***** VALIDACIONES *****/

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
				if("".equals(val.RazonSocialDatatec)){
					out.P_OUT_ERRORCONFIGURACIONDETALLE.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"FALTA CONFIGURAR LA RAZON SOCIAL DATATEC",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}

				idMesa = val.IdMesa;
				idAgente = Long.parseLong(val.IdAgente);
				tipoComprobanteFacturacion = val.TipoComprobanteFacturacion;
				idMedioPago = val.IdMedioPago;
				horaFinProceso = val.HoraFinProceso;
			}
			
			//Retorna si existen errores
			if(MensajeSalida.getNumeroErrores(out.P_OUT_ERRORCONFIGURACIONDETALLE) != 0L){
				out.P_OUT_ERRORCONFIGURACIONCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_ERRORCONFIGURACIONDETALLE);
				return out;
			}
			
			
			//Verifica las operaciones de entrada que ya existen en tabla intermedia
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			//params.put("NumeroRegistro", numeroOperacion);//posiblemente no se considera como llave para los registros a ingresar
			params.put("IdDatatec", idDatatec);
			params.put("ClaseMensaje", claseMensaje);
			
			List<OperacionSpotDatatec> operacionesExisten = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_DATATEC_EXISTEN, params, OperacionSpotDatatec.class);
			
			//Continua solo con las operaciones que no existen y estan ingresadas
			for (OperacionSpotDatatec ope: operacionesExisten) {
				//in.p_DatosDatatec.removeIf(opeIn -> opeIn.CONTADOR.equals(ope.NumeroRegistro.toString()) && opeIn.IDDATATEC.equals(ope.IdDatatec));
				in.p_DatosDatatec.removeIf(opeIn -> opeIn.IDDATATEC.equals(ope.IdDatatec) 
						&& opeIn.CLASEMENSAJE.equals(ope.ClaseMensaje)
						&& "ING".equals(ope.IdEstadoRegistro));
			}
			
			//Saca las operaciones PENDIENTES y que tenganlas fechas 
			for (OperacionSpotDatatec ope: operacionesExisten) {
				//in.p_DatosDatatec.removeIf(opeIn -> opeIn.CONTADOR.equals(ope.NumeroRegistro.toString()) && opeIn.IDDATATEC.equals(ope.IdDatatec));
				in.p_DatosDatatec.removeIf(opeIn -> opeIn.IDDATATEC.equals(ope.IdDatatec)
						&& "PEN".equals(ope.IdEstadoRegistro) 
						&& ope.FechaReproceso.after(ope.FechaOperacion));
			}	
			
			//Si no quedan operaciones de entrada, finaliza
			if(in.p_DatosDatatec.size() == 0){
				List<DatosDatatecObjOut> datosOutList = new ArrayList<IngresoDatatec.DatosDatatecObjOut>();
				for (OperacionSpotDatatec ope: operacionesExisten) {
					DatosDatatecObjOut datosOut = new DatosDatatecObjOut();
					datosOut.IdUnicoDatatec = ope.IdUnicoDatatec;
					datosOut.NumeroRegistro = ope.NumeroRegistro;
					datosOut.IdDatatec = ope.IdDatatec;
					datosOut.IdOperacionSpot = ope.IdOperacionSpot;
					datosOut.IdEstadoRegistro = ope.IdEstadoRegistro;
					datosOut.GlosaProceso = ope.GlosaProceso;
					datosOutList.add(datosOut);
				}
				out.P_OUT_DATOSDATATEC = new ArrayList<DatosDatatecObjOut>(datosOutList);
				out.P_OUT_DATOSDATATEC.sort(Comparator.comparing((DatosDatatecObjOut datosDatatecObjOut) -> datosDatatecObjOut.getNumeroRegistro()));
				return out;
			}
			
			
			/***** TABLA INTERMEDIA BSCL.Mesa.BD::SPT.OperacionSpotDatatec *****/
			List<String> idUnicoDatatec = new ArrayList<String>();
			List<Long> secuenciaIdUnicoDatatec = Utils.getSecuencia(conn, "BSCL.Mesa.BD.Secuencias::SPT.IdUnicoDatatec",Long.valueOf(in.p_DatosDatatec.size()));
			int i = 0;
			
			for(DatosDatatecObjIn dat: in.p_DatosDatatec){
				//Campos tabla BSCL.Mesa.BD::SPT.OperacionSpotDatatec
				OperacionSpotDatatec operacionSpotDatatec = new OperacionSpotDatatec();
				operacionSpotDatatec.IdUnicoDatatec = secuenciaIdUnicoDatatec.get(i).toString();
				operacionSpotDatatec.NumeroRegistro = Long.valueOf(dat.CONTADOR);
				operacionSpotDatatec.NumeroIntentos = 0L;
				operacionSpotDatatec.IdEstadoRegistro = "LEI";
				operacionSpotDatatec.GlosaProceso = "Leido";
				operacionSpotDatatec.IdEmpresa = in.p_IdEmpresa;
				operacionSpotDatatec.IdOperacionSpot = 0L;
				operacionSpotDatatec.FechaOperacion = Fecha.toDateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new SimpleDateFormat("yyyyMMdd HHmmss").parse(dat.FECHA+" "+dat.HORA)));
				operacionSpotDatatec.InstitucionValidacion = dat.INSTITUCIONPOSTURA.equals(nombreCorredora) ? dat.INSTITUCIONACEPTANTE : dat.INSTITUCIONPOSTURA;
				operacionSpotDatatec.IdCliente = indicadorClienteHash.get(operacionSpotDatatec.InstitucionValidacion) == null ? 0L : ((IndicadorCliente) indicadorClienteHash.get(operacionSpotDatatec.InstitucionValidacion)).IdCliente;
				operacionSpotDatatec.IdPortafolio = indicadorClienteHash.get(operacionSpotDatatec.InstitucionValidacion) == null ? 0L : ((IndicadorCliente) indicadorClienteHash.get(operacionSpotDatatec.InstitucionValidacion)).IdPortafolio;
				operacionSpotDatatec.NombreCliente = indicadorClienteHash.get(operacionSpotDatatec.InstitucionValidacion) == null ? "" : ((IndicadorCliente) indicadorClienteHash.get(operacionSpotDatatec.InstitucionValidacion)).NombreCompleto;
				operacionSpotDatatec.IdInstrumento = instrumentoMonedaMesaHash.get(dat.MONEDA) == null ? 0L : ((InstrumentoMonedaMesa) instrumentoMonedaMesaHash.get(dat.MONEDA)).IdInstrumento;
				operacionSpotDatatec.IdTipoOperacion = dat.IDOPERACION;
				operacionSpotDatatec.IdMonedaPrincipal = dat.MONEDA;
				operacionSpotDatatec.IdMonedaSecundaria = "CLP";
				operacionSpotDatatec.IdMonedaLocal = "";
				operacionSpotDatatec.MontoMonedaPrincipal = new BigDecimal(dat.NUMEROTRANSADO);
				operacionSpotDatatec.TipoCambioCierre = new BigDecimal(dat.PRECIO);
				operacionSpotDatatec.MontoMonedaSecundaria = operacionSpotDatatec.MontoMonedaPrincipal.multiply(operacionSpotDatatec.TipoCambioCierre);
				operacionSpotDatatec.TipoCambioCosto = operacionSpotDatatec.TipoCambioCierre;//revisar si cambiar por el TC del mantenedor PyTC
				operacionSpotDatatec.IdFormaPagoRecibo = "";
				operacionSpotDatatec.FechaValutaRecibo = Fecha.toDateTime("0001-01-01 00:00:00");
				operacionSpotDatatec.IdFormaPagoEntrego = "";
				operacionSpotDatatec.FechaValutaEntrego = Fecha.toDateTime("0001-01-01 00:00:00");
				operacionSpotDatatec.Origen = "DATATEC";
				operacionSpotDatatec.UsuarioValidacion = dat.INSTITUCIONPOSTURA.equals(nombreCorredora) ? dat.USUARIOPOSTURA : dat.USUARIOACEPTANTE;
				operacionSpotDatatec.IdUsuarioCreador = indicadorUsuarioHash.get(operacionSpotDatatec.UsuarioValidacion.toUpperCase()) == null ? 0L : ((IndicadorUsuario) indicadorUsuarioHash.get(operacionSpotDatatec.UsuarioValidacion.toUpperCase())).IdUsuario;
				operacionSpotDatatec.FechaCreacion = fechaHoraSistemaTimestamp;
				operacionSpotDatatec.PuntaCompra = new BigDecimal(dat.VALORPUNTACOMPRA);
				operacionSpotDatatec.PuntaVenta = new BigDecimal(dat.VALORPUNTACOMPRA);
				operacionSpotDatatec.IdDatatec = dat.IDDATATEC;
				operacionSpotDatatec.ClaseMensaje = dat.CLASEMENSAJE;
				operacionSpotDatatec.FechaReproceso = operacionSpotDatatec.FechaOperacion;
				
				i++;
				operacionesSpotDatatec.add(operacionSpotDatatec);
				idUnicoDatatec.add(operacionSpotDatatec.IdUnicoDatatec);
			}
			
			//Obtiene datos de operacion relacionada a la operacion a Anular
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			//params.put("NumeroRegistro", numeroOperacion);//posiblemente no se considera como llave para los registros a ingresar
			params.put("IdDatatec", idDatatec);
			params.put("ClaseMensaje", "5");
			operacionesExisten = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_DATATEC_EXISTEN, params, OperacionSpotDatatec.class);
			
			for (OperacionSpotDatatec ope: operacionesSpotDatatec) {
				for (OperacionSpotDatatec opeExiste: operacionesExisten) {
					if (ope.IdDatatec.equals(opeExiste.IdDatatec) && !ope.ClaseMensaje.equals(opeExiste.ClaseMensaje)) {
						ope.IdOperacionSpot = opeExiste.IdOperacionSpot;
					}
				}
			}
			
			//Insert tabla intermedia del proceso Datatec
			_sql.batch(conn, SQL_INSERT_OPERACIONES_SPOT_DATATEC, operacionesSpotDatatec, OperacionSpotDatatec.class);
			
			//Validaciones para llenar campos de estado y glosa
			LocalDateTime fechaHoraFinProceso = LocalDateTime.parse(fechaSistema+" "+horaFinProceso, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
			List<String> fechaInicialList = new ArrayList<String>();
			List<FechasIn> fechasInList = new ArrayList<ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn>();
			
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdUnicoDatatec", idUnicoDatatec);
			//params.put("NumeroRegistro", numeroOperacion);
			logger.debug("Inicio SQL_SELECT_VALIDACIONES_INGRESO_DATATEC");
			startTime = System.nanoTime();
			List<ValidacionesIngresoDatatec> validacionesIngresoDatatec = _sql.query(conn, SQL_SELECT_VALIDACIONES_INGRESO_DATATEC, params, ValidacionesIngresoDatatec.class);
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_VALIDACIONES_INGRESO_DATATEC");
			//IdClienteValidacion, IdPortafolioValidacion
			List<Long> IdClientes  = new ArrayList<Long>();
			List<Long> IdPortafolios  = new ArrayList<Long>();
			for(ValidacionesIngresoDatatec val: validacionesIngresoDatatec) {
				IdClientes.add(val.IdClienteValidacion);
				IdPortafolios.add(val.IdPortafolioValidacion);
			}
			params = new HashMap<>();
			params.put("IdCliente", IdClientes);
			params.put("IdPortafolio", IdPortafolios);
			startTime = System.nanoTime();
			logger.debug("Inicio SQL_SELECT_CLIENTE_PORTAFOLIO");
			List<DatosClientesSalida> clientesPortafoliosSalida  =  _sql.query(conn, SQL_SELECT_CLIENTE_PORTAFOLIO, params,DatosClientesSalida.class);
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_CLIENTE_PORTAFOLIO");

			int contadorRegistrosDatatec = 0;
			for(ValidacionesIngresoDatatec val: validacionesIngresoDatatec){
				val.GlosaProceso = "";
				boolean entraIf = false;
				
				//Cliente
				if(val.IdClienteValidacion.longValue() == 0L){
					val.IdEstadoRegistro = "ERR";
					String nombreCliente = in.p_DatosDatatec.get(contadorRegistrosDatatec).INSTITUCIONPOSTURA.equals(nombreCorredora) ? in.p_DatosDatatec.get(contadorRegistrosDatatec).INSTITUCIONACEPTANTE : in.p_DatosDatatec.get(contadorRegistrosDatatec).INSTITUCIONPOSTURA;
					val.GlosaProceso = "CLIENTE_NO_EXISTE"+" Cliente: "+  nombreCliente;
					entraIf = true;
				}else{
					if("BLO".equals(val.IdEstadoCliente) || "PEN".equals(val.IdEstadoCliente)){
						//obtener el documento identidad del cliente bloqueado o pendiente
						List<DatosClientesSalida> datosCliente = clientesPortafoliosSalida.stream()
								.filter(dc ->dc.IdCliente.equals(val.IdClienteValidacion)).collect(Collectors.toList());
						val.IdEstadoRegistro = "ERR";
						String glosa = "BLO".equals(val.IdEstadoCliente) ? "CLIENTE_BLOQUEADO" + " Documento Identidad: "+datosCliente.get(0).DocumentoIdentidad  : "PEN".equals(val.IdEstadoCliente) ? "CLIENTE_PENDIENTE_AUTORIZACION" + " Documento Identidad: "+datosCliente.get(0).DocumentoIdentidad : "";
						val.GlosaProceso = entraIf ? val.GlosaProceso + ", " + glosa : glosa;
						entraIf = true;
					}
				}
				
				//Portafolio
				if(val.IdPortafolioValidacion.longValue() == 0L){
					val.IdEstadoRegistro = "ERR";
					val.GlosaProceso = entraIf ? val.GlosaProceso + ", PORTAFOLIO_NO_EXISTE" : "PORTAFOLIO_NO_EXISTE";
					entraIf = true;
				}else {
					if("BLO".equals(val.IdEstadoPortafolio) || "PEN".equals(val.IdEstadoPortafolio)){
						//obtener el portafolio del cliente bloqueado o pendiente
						List<DatosClientesSalida> portafolio = clientesPortafoliosSalida.stream()
								.filter(dc ->dc.IdPortafolio.equals(val.IdPortafolioValidacion)).collect(Collectors.toList());
						val.IdEstadoRegistro = "ERR";
						String glosa = "BLO".equals(val.IdEstadoPortafolio) ? "PORTAFOLIO_BLOQUEADO" + " Portafolio: "+portafolio.get(0).CodigoPropositoPortafolio : "PEN".equals(val.IdEstadoPortafolio) ? "PORTAFOLIO_PENDIENTE_AUTORIZACION" + " Portafolio: "+portafolio.get(0).CodigoPropositoPortafolio : "";
						val.GlosaProceso = entraIf ? val.GlosaProceso + ", " + glosa : glosa;
						entraIf = true;
					}
				}
				
				//Usuario Creador
				if(val.IdUsuarioValidacion.longValue() == 0L){
					String nombreUsuarioCreador = in.p_DatosDatatec.get(contadorRegistrosDatatec).INSTITUCIONPOSTURA.equals(nombreCorredora) ? in.p_DatosDatatec.get(contadorRegistrosDatatec).USUARIOPOSTURA : in.p_DatosDatatec.get(contadorRegistrosDatatec).USUARIOACEPTANTE;
					val.IdEstadoRegistro = "ERR";
					val.GlosaProceso = entraIf ? val.GlosaProceso + ", USUARIO_CREADOR_NO_EXISTE" +" Usuario: "+nombreUsuarioCreador : "USUARIO_CREADOR_NO_EXISTE" +" Usuario: "+nombreUsuarioCreador;
					entraIf = true;
				}
				
				//Hora Procesamiento
				//if(!LocalTime.parse(val.HoraOperacion).isBefore(LocalTime.parse(horaFinProceso))){
				if(!LocalDateTime.parse(Fecha.toStringDatetime(val.FechaReproceso), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).isBefore(fechaHoraFinProceso)){
					if("LEI".equals(val.IdEstadoRegistro)) {
						List<DatosClientesSalida> datosCliente = clientesPortafoliosSalida.stream()
								.filter(dc ->dc.IdCliente.equals(val.IdClienteValidacion)).collect(Collectors.toList());
						val.IdEstadoRegistro = "PEN";
						String glosaTipoOperacion = in.p_DatosDatatec.get(contadorRegistrosDatatec).IDOPERACION == "VENSPOT"? " Tipo de operacion: "+"VENTA": " Tipo de operacion: "+"COMPRA";
						String glosaCantidadTransada = " Cantidad transada de la operacion: " + in.p_DatosDatatec.get(contadorRegistrosDatatec).NUMERO;
						String glosaPrecio = " Precio: " + in.p_DatosDatatec.get(contadorRegistrosDatatec).PRECIO;
						String glosaDocumentoIdentidadCliente = ", Documento Identidad Cliente: "+datosCliente.get(0).DocumentoIdentidad;
						val.GlosaProceso = entraIf ? val.GlosaProceso + ", EXCEDE_HORARIO" : "EXCEDE_HORARIO";
						val.GlosaProceso = val.GlosaProceso + glosaDocumentoIdentidadCliente + glosaTipoOperacion + glosaCantidadTransada+glosaPrecio;
						entraIf = true;
					} else {
						val.IdEstadoRegistro = "PEN";
						val.GlosaProceso = entraIf ? val.GlosaProceso + ", EXCEDE_HORARIO" : "EXCEDE_HORARIO";
						entraIf = true;
					}
				
					
					if (!fechaInicialList.contains(Fecha.toStringDate(val.FechaReproceso))) {
						fechaInicialList.add(Fecha.toStringDate(val.FechaReproceso));
						
						FechasIn fechas = new FechasIn();
						fechas.fechaInicial = Fecha.toStringDate(val.FechaReproceso);
						fechas.numeroDias = 1L;
						fechasInList.add(fechas);
					}
				}
				
				//val.NumeroIntentos = 0L;
				
				//Si pasa todas las validaciones, queda ING
				if (!entraIf) {
					val.IdEstadoRegistro = "ING";
					val.GlosaProceso = "Ingresado";
				}
				contadorRegistrosDatatec++;
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
				crearAnularEntrada.p_Proceso = "INGRESO";
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
			params.put("IdDatatec", idDatatec);
			params.put("ClaseMensaje", "50");
			params.put("IdEstadoRegistro", "LEI");
			
			
			//Obtiene los datatec a anular
			logger.debug("Inicio SQL_SELECT_OPERACIONES_SPOT_DATATEC_ANULACION");
			startTime = System.nanoTime();
			List<OperacionSpotDatatec> operacionesDatatecSrvSpotAnu = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_DATATEC_ANULACION, params, OperacionSpotDatatec.class);
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_OPERACIONES_SPOT_DATATEC_ANULACION");

			//consulta nuevamente por la operacion spot que se ingreso
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdDatatec", idDatatec);
			params.put("ClaseMensaje", "5");
			params.put("IdEstadoRegistro", "ING");
			
			
			operacionesDatatecSrvSpot = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_DATATEC_ANULACION, params, OperacionSpotDatatec.class);
			
			//coincidencias de IdDatatec que se ingreso a la que va a anular
			List<OperacionSpotDatatec> opSpotAnular = new ArrayList<IngresoDatatec.OperacionSpotDatatec>();
			
			for(OperacionSpotDatatec opIngr : operacionesDatatecSrvSpot ) {
				operacionesDatatecSrvSpotAnu.stream().forEach(opAnu -> {
				if(opAnu.IdDatatec.equals(opIngr.IdDatatec)) {
					opSpotAnular.add(opIngr);
				}
				
				});
			}
			
			if(opSpotAnular.size() > 0){
				CrearAnularOperacionSpotDatatec crearAnular = new CrearAnularOperacionSpotDatatec();
				CrearAnularOperacionSpotDatatec.CrearAnularOperacionSpotDatatecEntrada crearAnularEntrada = new CrearAnularOperacionSpotDatatec.CrearAnularOperacionSpotDatatecEntrada();
				crearAnularEntrada.p_IdEmpresa = in.p_IdEmpresa;
				crearAnularEntrada.p_IdUsuario = in.p_IdUsuario;
				crearAnularEntrada.p_IdModulo = in.p_IdModulo;
				crearAnularEntrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				crearAnularEntrada.p_Proceso = "INGRESO";
				crearAnularEntrada.p_Accion = "ANULAR";
				crearAnularEntrada.p_DatosOperacionSpot = opSpotAnular;
				CrearAnularOperacionSpotDatatec.CrearAnularOperacionSpotDatatecSalida crearAnularSalida = crearAnular.logica(conn, crearAnularEntrada);
				
				//Actualiza tabla intermedia con datos de la salida del servicio Anulacion Spot
				_sql.batch(conn, SQL_UPDATE_DATATEC_DATOS_SPOT, crearAnularSalida.P_OUT_DATOSSALIDASPOT, DatosDatatecObjOut.class);
			}
			
			for (OperacionSpotDatatec ope: operacionesExisten) {
				idDatatec.removeIf(opeIn -> opeIn.equals(ope.IdDatatec));
			}	
			
			/***** SALIDA *****/
			//Retorna operaciones Datatec
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdDatatec", idDatatec);
			params.put("Origen", "PROCESO");
			
			out.P_OUT_DATOSDATATEC = new ArrayList<DatosDatatecObjOut>(_sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_DATATEC_SALIDA, params, DatosDatatecObjOut.class));
			out.P_OUT_DATOSDATATEC.sort(Comparator.comparing((DatosDatatecObjOut datosDatatecObjOut) -> datosDatatecObjOut.getNumeroRegistro()));
			
			
			/***** AGREGAR LOG *****/
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			for (OperacionSpotDatatec ope: operacionesSpotDatatec) {
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
		
		logger.info("Termino del servicio IngresoDatatec, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonDesa = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":33,\"p_IdModulo\":\"MSP\",\"p_UsaReglasNegocio\":\"S\",\"p_DatosDatatec\":[{\"IDDATATEC\":\"6X1009C606RHA201\",\"HORA\":\"140403\",\"FECHA\":\"20181019\",\"CLASEMENSAJE\":\"5\",\"OD\":\"D\",\"INSTITUCIONPOSTURA\":\"CREDICORP\",\"CIUDADPOSTURA\":\"SANTIAGO\",\"USUARIOPOSTURA\":\"EQUIJON\",\"INSTITUCIONACEPTANTE\":\"GONZALEZ GONZALEZ\",\"CIUDADACEPTANTE\":\"SANTIAGO\",\"USUARIOACEPTANTE\":\"GONZALEZ GONZALEZ\",\"MERCADO\":\"39\",\"MONEDA\":\"EUR\",\"PRECIO\":\"740.2200\",\"NUMERO\":\"100000\",\"NUMEROTRANSADO\":\"100000\",\"DIAS\":\"\",\"BANDERA\":\"\",\"CONTADOR\":\"201\",\"NUMERODELIQUIDACION\":\"1\",\"FACTORIVA\":\"\",\"BANDERAFORMAPAGO\":\"\",\"BANDERAMETODO\":\"\",\"IDOPERACION\":\"VENSPT\",\"VALORPUNTACOMPRA\":\"599.00000000\",\"VALORPUNTAVENTA\":\"600.00000000\",\"MARGEN\":\"4002000.00000000\",\"MONTOPUNTA\":\"600.20000000\",\"MensajeValidacion\":\"OK\"},{\"IDDATATEC\":\"6X1009C606RHA202\",\"HORA\":\"100403\",\"FECHA\":\"20181020\",\"CLASEMENSAJE\":\"5\",\"OD\":\"D\",\"INSTITUCIONPOSTURA\":\"CREDICORP\",\"CIUDADPOSTURA\":\"SANTIAGO\",\"USUARIOPOSTURA\":\"EQUIJON\",\"INSTITUCIONACEPTANTE\":\"GONZALEZ GONZALEZ\",\"CIUDADACEPTANTE\":\"SANTIAGO\",\"USUARIOACEPTANTE\":\"GONZALEZ GONZALEZ\",\"MERCADO\":\"39\",\"MONEDA\":\"USD\",\"PRECIO\":\"650.2200\",\"NUMERO\":\"200000\",\"NUMEROTRANSADO\":\"200000\",\"DIAS\":\"\",\"BANDERA\":\"\",\"CONTADOR\":\"202\",\"NUMERODELIQUIDACION\":\"1\",\"FACTORIVA\":\"\",\"BANDERAFORMAPAGO\":\"\",\"BANDERAMETODO\":\"\",\"IDOPERACION\":\"VENSPT\",\"VALORPUNTACOMPRA\":\"599.00000000\",\"VALORPUNTAVENTA\":\"600.00000000\",\"MARGEN\":\"10004000.00000000\",\"MONTOPUNTA\":\"600.20000000\",\"MensajeValidacion\":\"OK\"},{\"IDDATATEC\":\"6X1009C606RHA203\",\"HORA\":\"110403\",\"FECHA\":\"20181019\",\"CLASEMENSAJE\":\"5\",\"OD\":\"D\",\"INSTITUCIONPOSTURA\":\"CREDICORP X\",\"CIUDADPOSTURA\":\"SANTIAGO\",\"USUARIOPOSTURA\":\"EQUIJON\",\"INSTITUCIONACEPTANTE\":\"GONZALEZ GONZALEZ\",\"CIUDADACEPTANTE\":\"SANTIAGO\",\"USUARIOACEPTANTE\":\"GONZALEZ GONZALEZ\",\"MERCADO\":\"39\",\"MONEDA\":\"USD\",\"PRECIO\":\"660.2200\",\"NUMERO\":\"300000\",\"NUMEROTRANSADO\":\"300000\",\"DIAS\":\"\",\"BANDERA\":\"\",\"CONTADOR\":\"203\",\"NUMERODELIQUIDACION\":\"1\",\"FACTORIVA\":\"\",\"BANDERAFORMAPAGO\":\"\",\"BANDERAMETODO\":\"\",\"IDOPERACION\":\"VENSPT\",\"VALORPUNTACOMPRA\":\"599.00000000\",\"VALORPUNTAVENTA\":\"600.00000000\",\"MARGEN\":\"18006000.00000000\",\"MONTOPUNTA\":\"600.20000000\",\"MensajeValidacion\":\"OK\"}]}";
			IngresoDatatecEntrada Objin = gson.fromJson(jsonDesa, IngresoDatatecEntrada.class);
			
			IngresoDatatec demo = new IngresoDatatec();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				IngresoDatatecSalida ObjOut = demo.logica(con, Objin);
 				//con.commit();
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