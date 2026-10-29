package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.RegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.ValorAntiguo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.InEntidad;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarPersonaCuentaBancariaMasivo implements JavaCall, EjecutarCambiosPersona {

	static final Logger logger = LogManager.getLogger(ModificarPersonaCuentaBancariaMasivo.class);
	
	private String SQL_SELECT_CUENTA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaCuentaBancariaMasivo_SelectCuentaBancaria.sql");
	private String SQL_SELECT_TIPO_CUENTA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaCuentaBancariaMasivo_SelectTipoCuentaBancaria.sql");
	
	private final String ID_CONCEPTO 			= "MODCTB";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	private final String ID_TIPO_ENTIDAD 		= "CUENTABANCARIA";
	private final String TipoCtaBca				= "IdTipoCuentaBancaria";
	
	public static class CuentaBancaria extends BcsPojo {
		public Long 	IdCuentaBancaria;
		public Long 	IdPersona;
		public String 	NombreCompleto;
		public String 	DocumentoIdentidad;
		public String 	EsPac;
		public String 	Predeterminado;
		public Long 	IdBanco;
		public Long 	IdPlaza;
		public String 	IdMoneda;
		public String 	IdEstado;
		public String 	CodigoAba;
		public String 	CodigoIban;
		public String 	IdentificadorCuenta;
		public Long 	IdPersonaRelacionada;
		public String 	IdTipoCuentaBancaria;
		public String 	NumeroCuentaBancaria;
		public String 	CodigoCuentaInterbancario;
		public String 	IdTipoIdentificadorCuenta;
		public String 	OtroCodigoBancoExtranjero;
		public String 	OtroCodigoCuentaExtranjero;
	}
	
	public static class TipoCuentaBancaria extends BcsPojo{
		public String	IdTipoCuentaBancaria;
	}
	
	public static class ValidacionTipocuentaBancaria extends BcsPojo{
		String	IdCuentaBancaria;
		Boolean	ModificacionTipoCuentaBancariaValida;
	}
	
	// --------------------------------------------------
	// CALL
	// --------------------------------------------------	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEjecutarCambiosPersona jin = gson.fromJson(in.toString(), EntradaEjecutarCambiosPersona.class);
		SalidaEjecutarCambiosPersona out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	// --------------------------------------------------
	// LOGICA
	// --------------------------------------------------
	@Override
	public SalidaEjecutarCambiosPersona logica(Connection con, EntradaEjecutarCambiosPersona entrada) throws Exception {
		logger.info("INICIO::ModificarPersonaCuentaBancariaMasivo");
		long inicioServicio = System.currentTimeMillis();
		SalidaEjecutarCambiosPersona out = new SalidaEjecutarCambiosPersona();
		SQL sql = new SQL();
		
		List<Long> lstIdSolicitudCambio = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
		Long idSolicitudCambio = lstIdSolicitudCambio.get(0);

		List<CuentaBancaria> lstCuentaBancaria = obtenerDatosCuentaBancaria(con, entrada);
		
		List<TipoCuentaBancaria> ltsTipoCuentaBancaria = obtenerTipoCuentaBancaria(con,sql);
		
		if (lstCuentaBancaria.size() == 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ENTIDAD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		List<RegistroSolicitudCambio> registrosSolicitudesCambios = obtenerRegistroSolicitudesCambio(con, idSolicitudCambio, lstCuentaBancaria, entrada);
		
		if (entrada.p_RegistroCambio.isEmpty()) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_REGISTRO_CAMBIO",null,con,entrada.p_IdEmpresa));
			return out;		
		}
		
		List<RegistroSolicitudCambio> cambiosTipoCuentaBancaria = registrosSolicitudesCambios.stream()
					.filter(cambio -> TipoCtaBca.equalsIgnoreCase(cambio.CampoModificado))
					.collect(Collectors.toList());
		
		if(cambiosTipoCuentaBancaria.size() > 0) {
			
			registrosSolicitudesCambios = validarCambioTipoCuentaBancaria(con, out, cambiosTipoCuentaBancaria, ltsTipoCuentaBancaria, registrosSolicitudesCambios, entrada.p_IdEmpresa);
			
			if(registrosSolicitudesCambios.size() == 0) {
				return out;	
			}
			
		}
		
		CrearRegistroSolicitudCambio servicioCrearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
		EntradaCrearRegistroSolicitudCambio inCrearRegistroSolicitudCambio = new EntradaCrearRegistroSolicitudCambio();
		
		inCrearRegistroSolicitudCambio.p_RegistroSolicitudCambio = registrosSolicitudesCambios;
		
		servicioCrearRegistroSolicitudCambio.logica(con, inCrearRegistroSolicitudCambio);
		
		Boolean requiereAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);
		
		if(requiereAutorizacion) {
			out = crearEventoAutorizacion(con, entrada, lstCuentaBancaria);
		} else {
			List<InEntidad> lstInEntidad = obtenerListaPersonaEntidad(lstCuentaBancaria);
			ConfirmarCambiosPersonaEntidad confirmarCambiosEntidad = new ConfirmarCambiosPersonaEntidad();
			out = confirmarCambiosEntidad.logica(con, entrada, lstInEntidad, new ConfirmarModificarPersonaCuentaBancariaMasivo());
		}
		
		out.P_OUT_REFID = idSolicitudCambio;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPersonaCuentaBancariaMasivo", finServicio - inicioServicio);
		return out;
	}
	
	private List<InEntidad> obtenerListaPersonaEntidad(List<CuentaBancaria> lstCuentaBancaria) {
		logger.trace("INICIO::ModificarPersonaDireccionFisicaMasivo.obtenerListaPersonaEntidad");
		long inicio = System.currentTimeMillis();		
		List<InEntidad> lstInEntidad = lstCuentaBancaria.stream()
				  .map(entidad -> new InEntidad(entidad.IdPersona,entidad.IdCuentaBancaria))
				  .distinct()
				  .collect(Collectors.toList());	
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarPersonaDireccionFisicaMasivo.obtenerListaPersonaEntidad", fin - inicio);
		return lstInEntidad;
	}
	
	private List<CuentaBancaria> obtenerDatosCuentaBancaria(Connection con, EntradaEjecutarCambiosPersona entrada) throws Exception {
		logger.debug("INICIO::ModificarPersonaCuentaBancariaMasivo.obtenerDatosCuentaBancaria");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();		
		HashMap<String, Object> params = new HashMap<String, Object>();
		
		List<Long> idEntidades = entrada.p_RegistroCambio.stream()
														 .map(entidad -> entidad.IdEntidad)
														 .distinct()
														 .collect(Collectors.toList());
		params.put("IdCuentaBancaria", idEntidades);		
		List<CuentaBancaria> listaTarjetasBancarias = sql.query(con, SQL_SELECT_CUENTA_BANCARIA, params, CuentaBancaria.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaCuentaBancariaMasivo.obtenerDatosCuentaBancaria", fin - inicio);
		return listaTarjetasBancarias;
	}
	
	private List<RegistroSolicitudCambio> obtenerRegistroSolicitudesCambio(Connection con, Long idSolicitudCambio, List<CuentaBancaria> lstEntidad, EntradaEjecutarCambiosPersona entrada) throws Exception{
		logger.trace("INICIO::ModificarPersonaDireccionFisicaMasivo.obtenerRegistroSolicitudesCambio");
		long inicio 	= System.currentTimeMillis();
		List<RegistroSolicitudCambio> registroSolicitudesCambio = new ArrayList<RegistroSolicitudCambio>();
		
		Map<Long, CuentaBancaria> entidadesPorId = lstEntidad
				.parallelStream()
				.collect(Collectors.toMap(entidad->entidad.IdCuentaBancaria, entidad->entidad));
		
		for(RegistroCambioMasivo registroCambio : entrada.p_RegistroCambio) {
			CuentaBancaria entidadesId = entidadesPorId.get(registroCambio.IdEntidad);
		
			if(entidadesId != null) {
				RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
				registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
				registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
				registroSolicitudCambio.IdEntidadOrigen = String.valueOf(registroCambio.IdEntidad);
				registroSolicitudCambio.CampoModificado = registroCambio.CampoModificado;
				
				ValorAntiguo valorAntiguo = ValorAntiguo.obtenerValorAntiguo(entidadesId, CuentaBancaria.class, registroCambio.CampoModificado);
				
				registroSolicitudCambio.TipoDato = valorAntiguo.TipoDato;
				registroSolicitudCambio.ValorAntiguo = valorAntiguo.Valor;
				registroSolicitudCambio.ValorNuevo = registroCambio.ValorNuevo;
				registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
				
				registroSolicitudesCambio.add(registroSolicitudCambio);
			}
			
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarPersonaDireccionFisicaMasivo.obtenerRegistroSolicitudesCambio", fin - inicio);
		return registroSolicitudesCambio;
	}
	
	private SalidaEjecutarCambiosPersona crearEventoAutorizacion(Connection con, EntradaEjecutarCambiosPersona entrada, List<CuentaBancaria> lstEntidad) throws Exception {
		logger.debug("INICIO::ModificarPersonaCuentaBancariaMasivo.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		
		for (CuentaBancaria entidad : lstEntidad) {
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento 			= ID_CONCEPTO;
			evento.IdModuloSolicita 		= entrada.p_IdModulo;
			evento.IdUsuarioSolicita 		= entrada.p_IdUsuario;
			evento.Fecha 					= fechaSistema;
			evento.IdConcepto 				= ID_CONCEPTO;
			evento.NumeroOperacionConcepto 	= String.valueOf(entidad.IdCuentaBancaria); 
			evento.FechaOperacionConcepto 	= fechaSistema;
			evento.IdCliente 				= entidad.IdPersona;
			evento.Glosa = entidad.DocumentoIdentidad + " / "  + entidad.NombreCompleto + " / " + " / IdCtaBanc: " +  entidad.IdCuentaBancaria;
			entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS.add(evento);			
		}

		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		SalidaEjecutarCambiosPersona salidaModificarPersonaCuentaBancaria = new SalidaEjecutarCambiosPersona();
		salidaModificarPersonaCuentaBancaria.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		
		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia->new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error->new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
		
		salidaModificarPersonaCuentaBancaria.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		salidaModificarPersonaCuentaBancaria.P_OUT_MENSAJES.addAll(mensajesErrores);
		
		if(salidaModificarPersonaCuentaBancaria.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaModificarPersonaCuentaBancaria.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaCuentaBancariaMasivo.crearEventoAutorizacion", fin - inicio);
		return salidaModificarPersonaCuentaBancaria;	
	}
	
	private List<TipoCuentaBancaria> obtenerTipoCuentaBancaria(Connection con, SQL sql) throws SQLException{
		logger.debug("INICIO::ModificarPersonaCuentaBancariaMasivo.obtenerTipoCuentaBancaria");
		long inicio = System.currentTimeMillis();
		
		List<TipoCuentaBancaria> listaTipoCuentaBancaria = new ArrayList<>();
		HashMap<String, Object> param = new HashMap<>(1);
		listaTipoCuentaBancaria = sql.query(con,SQL_SELECT_TIPO_CUENTA_BANCARIA,param,TipoCuentaBancaria.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaCuentaBancariaMasivo.obtenerTipoCuentaBancaria", fin - inicio);
		return listaTipoCuentaBancaria;
	}
	
	private List<RegistroSolicitudCambio> validarCambioTipoCuentaBancaria(Connection con, SalidaEjecutarCambiosPersona out, List<RegistroSolicitudCambio> cambiosTipoCuentaBancaria, List<TipoCuentaBancaria> ltsTipoCuentaBancaria, List<RegistroSolicitudCambio> registrosSolicitudesCambios, Long idEmpresa) throws Exception{
		logger.debug("INICIO::ModificarPersonaCuentaBancariaMasivo.validarCambioTipoCuentaBancaria");
		long inicio = System.currentTimeMillis();
		List<RegistroSolicitudCambio> solicitudes = new ArrayList<>();
		
		List<String> listaTipoCuentaBancaria = ltsTipoCuentaBancaria.stream()
				.map(tipoCta ->{
					return tipoCta.IdTipoCuentaBancaria;
				})
				.collect(Collectors.toList());;
												
		List<ValidacionTipocuentaBancaria> validacionCambiotipoCuentaBancaria = cambiosTipoCuentaBancaria.stream()
		.map(cambio -> {
			ValidacionTipocuentaBancaria validaTipoCuentaBancaria = new ValidacionTipocuentaBancaria();
			validaTipoCuentaBancaria.IdCuentaBancaria = cambio.IdEntidadOrigen;
			validaTipoCuentaBancaria.ModificacionTipoCuentaBancariaValida = listaTipoCuentaBancaria.contains(cambio.ValorNuevo);
			return validaTipoCuentaBancaria;
			})
		.collect(Collectors.toList());
		
		List<String> cuentasBancariasConError = validacionCambiotipoCuentaBancaria.stream()
				.filter(cambio -> !cambio.ModificacionTipoCuentaBancariaValida)
				.map(cambio -> {
					return cambio.IdCuentaBancaria;
				})
				.distinct()
				.collect(Collectors.toList());
		
		if(cuentasBancariasConError.size() > 0) {
			registrosSolicitudesCambios = new ArrayList<>();
			
			String codMsg = "ERROR_TIPO_CUENTA_BANCARIA_NO_EXISTE";
									
			for(String cuenta:cuentasBancariasConError) {
				String msg=Diccionario.getMensaje(con,idEmpresa,codMsg)
						  .add("IdCuentaBancaria", cuenta)
						  .msg();
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
			}
		}
		
		solicitudes = registrosSolicitudesCambios;
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaCuentaBancariaMasivo.validarCambioTipoCuentaBancaria", fin - inicio);
		return solicitudes;
	}

	
	public static void main(String[] args) throws SQLException {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_RegistroCambio\":[{\"CampoModificado\":\"NumeroCuentaBancaria\",\"ValorNuevo\":\"9877-9877\",\"IdEntidad\":108}, {\"CampoModificado\":\"NumeroCuentaBancaria\",\"ValorNuevo\":\"1326-5484-8777\",\"IdEntidad\":107}]}";
			EntradaEjecutarCambiosPersona Objin = gson.fromJson(json, EntradaEjecutarCambiosPersona.class);
			
			ModificarPersonaCuentaBancariaMasivo demo = new ModificarPersonaCuentaBancariaMasivo();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				SalidaEjecutarCambiosPersona ObjOut = demo.logica(con, Objin);
 				con.commit();
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
