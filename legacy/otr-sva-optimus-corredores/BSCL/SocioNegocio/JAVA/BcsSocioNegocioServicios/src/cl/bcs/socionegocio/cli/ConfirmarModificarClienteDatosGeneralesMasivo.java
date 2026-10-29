package cl.bcs.socionegocio.cli;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambioMasivo.EntradaConsultarRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambioMasivo.SalidaConsultarRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales.EntidadLogGenerarContrato;
import cl.bcs.socionegocio.cli.ModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento.EntradaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento;
import cl.bcs.socionegocio.cli.ModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento.SalidaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento;
import cl.bcs.socionegocio.cli.pojo.DatosClienteDatosGenerales;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarModificarClienteDatosGeneralesMasivo implements JavaCall {

    static final Logger logger = LogManager.getLogger(ConfirmarModificarClienteDatosGeneralesMasivo.class);

    private String SQL_SELECT_TIPOSEGMENTO = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConfirmarModificarClienteDatosGeneralesMasivo_SelectTipoSegmento.sql");
    private String SQL_SELECT_EJECUTIVO = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConfirmarModificarClienteDatosGeneralesMasivo_SelectEjecutivo.sql");
    private String SQL_SELECT_SUCURSAL = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConfirmarModificarClienteDatosGeneralesMasivo_SelectSucursal.sql");
    private String SQL_UPDATE_DATOS_GENERALES = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConfirmarModificarClienteDatosGeneralesMasivo_UpdateDatosGenerales.sql");
    public  String SQL_INSERT_LOG_GENERAR_DOCUMENTOS		= loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarClienteDatosGenerales_InsertLogGenerarContratos.sql");    
   
    private final String ID_TIPO_ENTIDAD = "CLIENTE";

    public static class EntradaConfirmarModificarClienteDatosGeneralesMasivo extends BcsPojo {
	public Long p_IdEmpresa;
	public Long p_IdUsuario;
	public String p_IdModulo;
	public String p_UsaReglasNegocio;
	public List<InClienteDatosGenerales> p_ClienteDatosGenerales;
	public String p_IdEstadoAutorizacion;

	public EntradaConfirmarModificarClienteDatosGeneralesMasivo() {
	    this.p_ClienteDatosGenerales = new ArrayList<InClienteDatosGenerales>();
	}
    }
    
	public static class ClientePerfilComercial {
    	public Long IdPortafolio;
		public String IdPerfilProductos;
		public String IdPerfilComercialBase;
		public String IdCategoriaInversionista;		
	}

    public static class InClienteDatosGenerales extends BcsPojo {
	public Long IdCliente;

	public InClienteDatosGenerales(Long idCliente) {
	    IdCliente = idCliente;
	}
    }
    
    public static class TipoSegmento{
    	public Long IdSegmentoCliente;
    	public String NombreSegmentoCliente;
    	public String DescripcionSegmento;
    }
    
    public static class Ejecutivo{
    	public Long IdEmpleadoEjecutivo;
    	public long IdPersonaEjecutivo;
    	public String NombreCompleto;
    }
    
    public static class Sucursal{
    	public Long IdSucursal;
    	public String NombreSucursal;
    }

    public static class SalidaConfirmarModificarClienteDatosGeneralesMasivo extends BcsPojo {
	public Long P_OUT_ERRORESCONTADOR;
	public List<MensajeSalida> P_OUT_MENSAJES;

	public SalidaConfirmarModificarClienteDatosGeneralesMasivo() {
	    this.P_OUT_ERRORESCONTADOR = 0L;
	    this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
	}
    }

    public static class ClienteDatosGeneralesActualizar extends BcsPojo {
	public Long IdCliente;
	public String IdEstadoAutorizacion;
    }

    public static class ClienteFatcaPaisResidencia extends BcsPojo {
	public Long IdCliente;
	public Long IdPaisResidenciaAntiguo;
	public Long IdPaisResidenciaNuevo;
    }

    public static class ExcepcionActualizar extends BcsPojo {
	public Long IdCliente;
	public String Valor;
    }

    public static class PaisEstadosUnidos extends BcsPojo {
	public Long IdPais;
    }

    public static class RegistroCambio extends BcsPojo {
	public String CampoModificado;
	public String ValorNuevo;
    };

    public static class ValorAntiguo extends BcsPojo {
	public String Valor;
	public String TipoDato;
    }
    
    public static class ClienteLog{
    	public Long IdCliente;
    	public String Nombres;
    	public String ApellidoPaterno;
    	public String ApellidoMaterno;
    	public String DocumentoIdentidad;
    	public String IdTipoDocumentoIdentidad;
    	public String EstadoCliente;
    	public Long IdPortafolioDefecto;
    	public String EstadoPortafolio;
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
	Gson gson = Utils.getGsonBuilder();
	EntradaConfirmarModificarClienteDatosGeneralesMasivo jin = gson.fromJson(in.toString(), EntradaConfirmarModificarClienteDatosGeneralesMasivo.class);
	SalidaConfirmarModificarClienteDatosGeneralesMasivo out = logica(conn, jin);
	JsonParser parser = new JsonParser();
	JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
	return dataout;
    }

    public SalidaConfirmarModificarClienteDatosGeneralesMasivo logica(Connection con, EntradaConfirmarModificarClienteDatosGeneralesMasivo entrada) throws Exception {
		logger.info("INICIO::SalidaConfirmarModificarClienteDatosGeneralesMasivo.logica");
		long inicioServicio = System.currentTimeMillis();
		SalidaConfirmarModificarClienteDatosGeneralesMasivo out = new SalidaConfirmarModificarClienteDatosGeneralesMasivo();
	
		if (entrada.p_ClienteDatosGenerales.isEmpty()) {
		    out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FALTA_ESPECIFICAR_CLIENTE", null, con, entrada.p_IdEmpresa));
		    return out;
		}
	
		List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios(con, entrada);
	
		if (solicitudesCambios.isEmpty()) {
		    out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "SOLICITUD_NO_EXISTE", null, con, entrada.p_IdEmpresa));
		    return out;
		}
		
	
		List<DatosClienteDatosGenerales> datosClienteDatosGenerales = obtenerEntidadesDatosGenerales(solicitudesCambios);
	
		insertarCambiosEnTablaLogGenerarContratos(solicitudesCambios, con, entrada);
		
		
		 	
		List<MensajeSalida> mensajeSalida = modificarCampoDatosGenerales(con, datosClienteDatosGenerales, entrada.p_IdEmpresa, entrada.p_IdUsuario);
						
		if (MensajeSalida.getNumeroErrores(mensajeSalida) > 0)
		{
			out.P_OUT_MENSAJES.addAll(mensajeSalida);
			return out;
		}
	
		out  = modificarEstadoRegistroSolicitudCambio(con, entrada, solicitudesCambios);
	
		agregarLog(con, entrada, solicitudesCambios);
	
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConfirmarModificarClienteDatosGeneralesMasivo.logica", finServicio - inicioServicio);
	
		return out;
    }
    
    private void insertarCambiosEnTablaLogGenerarContratos(List<SolicitudCambio> solicitudesCambios, Connection con, EntradaConfirmarModificarClienteDatosGeneralesMasivo entrada)  throws Exception{
    	logger.debug("INICIO::ConfirmarModificarClienteDatosGeneralesMasivo.insertarCambiosEnTablaLogGenerarContratos");
		long inicio = System.currentTimeMillis();
    	
    	List<EntidadLogGenerarContrato> params = new ArrayList<EntidadLogGenerarContrato>();
    	List<Long> idSecuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos", (long) solicitudesCambios.size());
    	String fechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
		for(SolicitudCambio solicitud: solicitudesCambios) {
			if(!solicitud.ValorAntiguo.equals(solicitud.ValorNuevo)) {
				EntidadLogGenerarContrato logGenerarContrato = new EntidadLogGenerarContrato();
				logGenerarContrato.Idlog = idSecuencias.remove(0);
				logGenerarContrato.CodigoFormulario = "DATOS_GENERALES_CLIENTE_";
				logGenerarContrato.CampoBackend = solicitud.CampoModificado;
				logGenerarContrato.IdEmpresa = entrada.p_IdEmpresa;
				logGenerarContrato.IdCliente = solicitud.IdEntidadOrigenLong;
				logGenerarContrato.IdPersona = 0L;
				logGenerarContrato.IdUsuario = entrada.p_IdUsuario;
				logGenerarContrato.FechaSistema = fechaSistema;
				logGenerarContrato.IdPrograma = 0L;
				logGenerarContrato.Estado = "PEN";
				logGenerarContrato.Mensaje = "Cliente - ConfirmarModificarClienteDatosGeneralesMasivo.java";
				params.add(logGenerarContrato);
			}
		}				
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_LOG_GENERAR_DOCUMENTOS, params,EntidadLogGenerarContrato.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.insertarCambiosEnTablaLogGenerarContratos", fin - inicio);
    }

    private List<SolicitudCambio> obtenerSolicitudesDeCambios(Connection con, EntradaConfirmarModificarClienteDatosGeneralesMasivo entrada) throws Exception {
    	logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerSolicitudesDeCambios");
    	long inicio = System.currentTimeMillis();
    	
		EntradaConsultarRegistroSolicitudCambioMasivo entradaConsultarRegistroSolicitudCambioMasivo = new EntradaConsultarRegistroSolicitudCambioMasivo();
		
		for (InClienteDatosGenerales dato : entrada.p_ClienteDatosGenerales) {
			ConsultarRegistroSolicitudCambioMasivo.EntradaSolicitudCambio entradaSolicitudCambio = new ConsultarRegistroSolicitudCambioMasivo.EntradaSolicitudCambio();
			entradaSolicitudCambio.IdEntidadOrigen = dato.IdCliente.toString();
			entradaSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
			entradaConsultarRegistroSolicitudCambioMasivo.p_SolicitudesCambio.add(entradaSolicitudCambio);
		}

		ConsultarRegistroSolicitudCambioMasivo consultarRegistroSolicitudCambioMasivo = new ConsultarRegistroSolicitudCambioMasivo();
		SalidaConsultarRegistroSolicitudCambioMasivo salidaConsultarRegistroSolicitudCambioMasivo = consultarRegistroSolicitudCambioMasivo.logica(con, entradaConsultarRegistroSolicitudCambioMasivo);
	
		long fin = System.currentTimeMillis();
		logger.debug("FIN::RechazaModificarClienteDatosGeneralesMasivo.obtenerSolicitudesDeCambios", fin - inicio);
		return salidaConsultarRegistroSolicitudCambioMasivo.P_OUT_SOLICITUDES_CAMBIO;
    }

    private SalidaConfirmarModificarClienteDatosGeneralesMasivo modificarEstadoRegistroSolicitudCambio(Connection con, EntradaConfirmarModificarClienteDatosGeneralesMasivo entrada, List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.debug("INICIO::ConfirmarModificarClienteDatosGeneralesMasivo.modificarEstadoRegistroSolicitudCambio");
		long inicio = System.currentTimeMillis();
		SalidaConfirmarModificarClienteDatosGeneralesMasivo out = new SalidaConfirmarModificarClienteDatosGeneralesMasivo();
		ModificarEstadoRegistroSolicitudCambioMasivo modificarEstadoRegistroSolicitudCambioMasivo = new ModificarEstadoRegistroSolicitudCambioMasivo();
		ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado entradaModificarEstadoRegistroSolicitudCambioMasivo = new ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado();
		entradaModificarEstadoRegistroSolicitudCambioMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarEstadoRegistroSolicitudCambioMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarEstadoRegistroSolicitudCambioMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaModificarEstadoRegistroSolicitudCambioMasivo.p_IdEstadoAutorizacion = entrada.p_IdEstadoAutorizacion;
		
		for (SolicitudCambio sc : solicitudesCambios) {
			ModificarEstadoRegistroSolicitudCambioMasivo.SolicitudCambio solicitud = new ModificarEstadoRegistroSolicitudCambioMasivo.SolicitudCambio();
			solicitud.IdSolicitudCambio = sc.IdSolicitudCambio;
			entradaModificarEstadoRegistroSolicitudCambioMasivo.p_SolicitudCambio.add(solicitud);
		}
		
		ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado salidaModificarEstadoRegistroSolicitudCambioMasivo = modificarEstadoRegistroSolicitudCambioMasivo.logica(con, entradaModificarEstadoRegistroSolicitudCambioMasivo);
		out.P_OUT_ERRORESCONTADOR += salidaModificarEstadoRegistroSolicitudCambioMasivo.p_out_ErroresContador;
		
		for (ModificarEstadoRegistroSolicitudCambioMasivo.ErrorDetalle err : salidaModificarEstadoRegistroSolicitudCambioMasivo.p_out_ErroresDetalle) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, err.Codigo, err.Descripcion));
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarClienteDatosGeneralesMasivo.modificarEstadoRegistroSolicitudCambio", fin - inicio);
		return out;
    }

    private List<DatosClienteDatosGenerales> obtenerEntidadesDatosGenerales(List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.info("INICIO::ConfirmarModificarClienteDatosGeneralesMasivo.obtenerEntidadesDatosGenerales");
		long inicio = System.currentTimeMillis();
	
		Map<Long, List<SolicitudCambio>> solicitudesPorIdEntidad = solicitudesCambios.stream().collect(Collectors.groupingBy(solicitudCambio -> solicitudCambio.IdEntidadOrigenLong));
	
		List<DatosClienteDatosGenerales> datosAActualizar = new ArrayList<>();
		
		for (Entry<Long, List<SolicitudCambio>> cliente : solicitudesPorIdEntidad.entrySet()) {
			DatosClienteDatosGenerales datosClienteDatosGenerales = new DatosClienteDatosGenerales();
		    List<SolicitudCambio> cambiosCliente = cliente.getValue();
		    datosClienteDatosGenerales.IdCliente = (Long) cambiosCliente.stream().findFirst().get().IdEntidadOrigenLong;
	
		    for (SolicitudCambio solicitudCambio : cambiosCliente) {
			Field campo = datosClienteDatosGenerales.getClass().getField(solicitudCambio.CampoModificado);
			if (campo.getType().isAssignableFrom(String.class)) {
			    campo.set(datosClienteDatosGenerales, solicitudCambio.ValorNuevo);
			    continue;
			}
			if (campo.getType().isAssignableFrom(Long.class)) {
			    campo.set(datosClienteDatosGenerales, solicitudCambio.ValorNuevo == null ? null : Long.parseLong(solicitudCambio.ValorNuevo));
			    continue;
			}
			if (campo.getType().isAssignableFrom(java.sql.Timestamp.class)) {
			    campo.set(datosClienteDatosGenerales, Fecha.toDateTime(solicitudCambio.ValorNuevo));
			    continue;
			}
			if (campo.getType().isAssignableFrom(java.sql.Date.class)) {
			    campo.set(datosClienteDatosGenerales, Fecha.toDate(solicitudCambio.ValorNuevo));
			    continue;
			}
		    }
		    datosAActualizar.add(datosClienteDatosGenerales);
		}
	
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarClienteDatosGeneralesMasivo.obtenerEntidadesDatosGenerales", fin - inicio);
	
		return datosAActualizar;
    }

    

    private List<MensajeSalida> modificarCampoDatosGenerales(Connection con, List<DatosClienteDatosGenerales> datosClienteDatosGenerales, Long idEmpresa, Long idUsuario ) throws Exception {
		logger.debug("INICIO::ConfirmarModificarClienteDatosGeneralesMasivo.modificarCampoDatosGenerales");
		long inicio = System.currentTimeMillis();
	
		ArrayList<MensajeSalida> mensajeSalida = new ArrayList<MensajeSalida>();
		
		if (datosClienteDatosGenerales.size() > 0) {
		    SQL sql = new SQL();
		    sql.batch(con, SQL_UPDATE_DATOS_GENERALES, datosClienteDatosGenerales, DatosClienteDatosGenerales.class);		    
		    
		    for(DatosClienteDatosGenerales datosCliente : datosClienteDatosGenerales)
			    if (datosCliente.IdCategoriaInversionista != null) {
			    	
			    	EntradaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento entradaModificarPerfilComercial = new EntradaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento();
					entradaModificarPerfilComercial.p_IdEmpresa = idEmpresa;
					entradaModificarPerfilComercial.p_IdUsuario = idUsuario;
					entradaModificarPerfilComercial.p_IdModulo 	= "SOC";
					entradaModificarPerfilComercial.p_IdCliente = datosCliente.IdCliente;
					ModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento modificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento = new ModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento();
					SalidaModificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento salidaModificarPerfilComercial = modificarPerfilComercialPortafoliosMatrizConfiguracionEnrolamiento.logica(con, entradaModificarPerfilComercial);
	
					mensajeSalida.addAll(salidaModificarPerfilComercial.P_OUT_MENSAJES);
			    }		    
		}
	
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarClienteDatosGeneralesMasivo.modificarCampoDatosGenerales", fin - inicio);
		
		return mensajeSalida;
    }
    
    private HashMap<String, Object> obtenerTipoSegemento(Connection con, SQL sql, List<Long> idsTipoSegmento){
		logger.debug("INICIO::ConfirmarModificarClienteDatosGeneralesMasivo.obtenerTipoSegemento");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> hashLista = new HashMap<String, Object>();
		if(!(idsTipoSegmento.isEmpty())) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdSegmento", idsTipoSegmento);
			try {
				List<TipoSegmento> listado = sql.query(con, SQL_SELECT_TIPOSEGMENTO, params, TipoSegmento.class);
				if(!(listado.isEmpty())) {
					hashLista = Utils.List2Hash(listado, "IdSegmentoCliente");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarClienteDatosGeneralesMasivo.obtenerTipoSegemento", fin - inicio);
		
		return hashLista;
	}
    
    private HashMap<String, Object> obtenerDatosEjecutivo(Connection con, SQL sql, List<Long> idsEjecutivos){
		logger.debug("INICIO::ConfirmarModificarClienteDatosGeneralesMasivo.obtenerDatosEjecutivo");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> hashLista = new HashMap<String, Object>();
		if(!(idsEjecutivos.isEmpty())) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdPersonaEjecutivo", idsEjecutivos);
			try {
				List<Ejecutivo> listado = sql.query(con, SQL_SELECT_EJECUTIVO, params, Ejecutivo.class);
				if(!(listado.isEmpty())) {
					hashLista = Utils.List2Hash(listado, "IdPersonaEjecutivo");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarClienteDatosGeneralesMasivo.obtenerDatosEjecutivo", fin - inicio);
		
		return hashLista;
	}
    
    private HashMap<String, Object> obtenerDatosSucursalPreferencia(Connection con, SQL sql, List<Long> idsSucursal){
		logger.debug("INICIO::ConfirmarModificarClienteDatosGeneralesMasivo.obtenerDatosSucursalPreferencia");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> hashLista = new HashMap<String, Object>();
		if(!(idsSucursal.isEmpty())) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdSucursal", idsSucursal);
			try {
				List<Sucursal> listado = sql.query(con, SQL_SELECT_SUCURSAL, params, Sucursal.class);
				if(!(listado.isEmpty())) {
					hashLista = Utils.List2Hash(listado, "IdSucursal");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarClienteDatosGeneralesMasivo.obtenerDatosSucursalPreferencia", fin - inicio);
		
		return hashLista;
	}
    
    
    
    private void agregarLog(Connection con, EntradaConfirmarModificarClienteDatosGeneralesMasivo entrada, List<SolicitudCambio> solicitudCambio) throws Exception {
		logger.debug("INICIO::ConfirmarModificarClienteDatosGeneralesMasivo.agregarLog");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		AgregarLog.OutAgregarLog salida = new AgregarLog.OutAgregarLog();
		
		AgregarLog AgregarLog = new AgregarLog();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaAgregarLog.p_IdUsuario = entrada.p_IdUsuario;
		entradaAgregarLog.p_IdModulo = entrada.p_IdModulo;
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<Long> idTipoSegmento = new ArrayList<Long>();
		List<Long> idPersonaEjecutivo = new ArrayList<Long>();
		List<Long> idSucursal = new ArrayList<Long>();
		
		for(SolicitudCambio solicitud : solicitudCambio) {
			try {
				if(solicitud.CampoModificado!=null) {
					if(solicitud.CampoModificado.compareToIgnoreCase("IdSegmentoCliente") == 0){
						if(solicitud.ValorAntiguo!=null && !(solicitud.ValorAntiguo.equalsIgnoreCase("null")) && !(idTipoSegmento.contains(Long.parseLong(solicitud.ValorAntiguo)))) idTipoSegmento.add(Long.parseLong(solicitud.ValorAntiguo));
						if(solicitud.ValorNuevo!=null && !(solicitud.ValorNuevo.equalsIgnoreCase("null")) && !(idTipoSegmento.contains(Long.parseLong(solicitud.ValorNuevo)))) idTipoSegmento.add(Long.parseLong(solicitud.ValorNuevo));
					}
					if(solicitud.CampoModificado.compareToIgnoreCase("IdPersonaEjecutivo") == 0){
						if(solicitud.ValorAntiguo!=null && !(solicitud.ValorAntiguo.equalsIgnoreCase("null")) && !(idPersonaEjecutivo.contains(Long.parseLong(solicitud.ValorAntiguo)))) idPersonaEjecutivo.add(Long.parseLong(solicitud.ValorAntiguo));
						if(solicitud.ValorNuevo!=null && !(solicitud.ValorNuevo.equalsIgnoreCase("null")) && !(idPersonaEjecutivo.contains(Long.parseLong(solicitud.ValorNuevo)))) idPersonaEjecutivo.add(Long.parseLong(solicitud.ValorNuevo));
					}
					if(solicitud.CampoModificado.compareToIgnoreCase("IdSucursalPreferencia") == 0){
						if(solicitud.ValorAntiguo!=null && !(solicitud.ValorAntiguo.equalsIgnoreCase("null")) && !(idSucursal.contains(Long.parseLong(solicitud.ValorAntiguo)))) idSucursal.add(Long.parseLong(solicitud.ValorAntiguo));
						if(solicitud.ValorNuevo!=null && !(solicitud.ValorNuevo.equalsIgnoreCase("null")) && !(idSucursal.contains(Long.parseLong(solicitud.ValorNuevo)))) idSucursal.add(Long.parseLong(solicitud.ValorNuevo));
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		HashMap hSegmento = new HashMap<String, Object>();
		HashMap hEjecutivo = new HashMap<String, Object>();
		HashMap hSucursal = new HashMap<String, Object>();
		try {
			hSegmento = obtenerTipoSegemento(con, sql, idTipoSegmento);
			hEjecutivo = obtenerDatosEjecutivo(con, sql, idPersonaEjecutivo);
			hSucursal = obtenerDatosSucursalPreferencia(con, sql, idSucursal);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		List<Long> IdCabecera = new ArrayList<Long>();
		for(SolicitudCambio solicitud : solicitudCambio) {
			//cabecera LOG
			if(solicitud.IdEntidadOrigenLong!=null && !(solicitud.IdEntidadOrigenLong.compareTo(0L)==0) && !(IdCabecera.contains(solicitud.IdEntidadOrigenLong))){
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdCabeceraLog = solicitud.IdEntidadOrigenLong;
				log.IdTipoLog = "MODIFICACION";
				log.IdModulo = entrada.p_IdModulo;
				log.IdUsuario = entrada.p_IdUsuario;
				log.IdEmpresa = entrada.p_IdEmpresa;
				log.FechaSistemaLog = fechaSistema;
				log.IdUsuarioAtendido = entrada.p_IdUsuario;
				log.IdRegistro = solicitud.IdEntidadOrigenLong.toString();
				log.IdCliente = solicitud.IdEntidadOrigenLong;
				log.IdTipoRegistro = ID_TIPO_ENTIDAD;
				log.EstadoRegistro = entrada.p_IdEstadoAutorizacion;
				log.IdProceso = 0L;
				log.EstadoProceso = entrada.p_IdEstadoAutorizacion;
				log.NombreProceso = "Modificar Solicitud Cambio Datos Generales";
				entradaAgregarLog.p_Log.add(log);
			}
			try {
				//detalle LOG
				if(solicitud.CampoModificado!=null) {
					AgregarLog.InAgregarLog.DetalleLog detalleLog = new AgregarLog.InAgregarLog.DetalleLog();
					detalleLog.IdTrabajo = 0L; 
					detalleLog.IdCabeceraLog = solicitud.IdEntidadOrigenLong;			
					detalleLog.SubRegistro = solicitud.IdTipoEntidad;
					detalleLog.CampoRegistro = solicitud.CampoModificado;
					detalleLog.ValorAntiguo = "";
					if(solicitud.ValorAntiguo!=null && !(solicitud.ValorAntiguo.equalsIgnoreCase("null"))) {
						if(solicitud.CampoModificado.compareToIgnoreCase("IdSegmentoCliente") == 0) {
							TipoSegmento segmento = (TipoSegmento) hSegmento.get(Long.parseLong(solicitud.ValorAntiguo));
							if(segmento!=null) {
								detalleLog.ValorAntiguo = (segmento.DescripcionSegmento!=null)? segmento.DescripcionSegmento: "";
							}
						}
						else if(solicitud.CampoModificado.compareToIgnoreCase("IdPersonaEjecutivo") == 0) {
							Ejecutivo ejecutivo = (Ejecutivo) hEjecutivo.get(Long.parseLong(solicitud.ValorAntiguo));
							if(ejecutivo!=null) {
								detalleLog.ValorAntiguo = (ejecutivo.NombreCompleto!=null)? ejecutivo.NombreCompleto: "";
							}
						}
						else if(solicitud.CampoModificado.compareToIgnoreCase("IdSucursalPreferencia") == 0) {
							Sucursal sucursal = (Sucursal) hSucursal.get(Long.parseLong(solicitud.ValorAntiguo));
							if(sucursal!=null) {
								detalleLog.ValorAntiguo = (sucursal.NombreSucursal!=null)? sucursal.NombreSucursal: "";
							}
						}
						else{
							
							detalleLog.ValorAntiguo = solicitud.ValorAntiguo;
						}
					}
					detalleLog.ValorNuevo = "";
					if(solicitud.ValorNuevo!=null && !(solicitud.ValorNuevo.equalsIgnoreCase("null"))) {
						if(solicitud.CampoModificado.compareToIgnoreCase("IdSegmentoCliente") == 0){
							TipoSegmento segmento = (TipoSegmento) hSegmento.get(Long.parseLong(solicitud.ValorNuevo));
							if(segmento!=null) {
								detalleLog.ValorNuevo = (segmento.DescripcionSegmento!=null)? segmento.DescripcionSegmento: "";
							}
						}
						else if(solicitud.CampoModificado.compareToIgnoreCase("IdPersonaEjecutivo") == 0){
							Ejecutivo ejecutivo = (Ejecutivo) hEjecutivo.get(Long.parseLong(solicitud.ValorNuevo));
							if(ejecutivo!=null) {
								detalleLog.ValorNuevo = (ejecutivo.NombreCompleto!=null)? ejecutivo.NombreCompleto: "";
							}
						}
						else if(solicitud.CampoModificado.compareToIgnoreCase("IdSucursalPreferencia") == 0){
							Sucursal sucursal = (Sucursal) hSucursal.get(Long.parseLong(solicitud.ValorNuevo));
							if(sucursal!=null) {
								detalleLog.ValorNuevo = (sucursal.NombreSucursal!=null)? sucursal.NombreSucursal: "";
							}
						}
						else{
							detalleLog.ValorNuevo = solicitud.ValorNuevo;
						}
					}
					entradaAgregarLog.p_LogDetalle.add(detalleLog);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			
		}

		salida = AgregarLog.logica(con, entradaAgregarLog);
		logger.debug("FIN::ConfirmarModificarClienteDatosGeneralesMasivo.agregarLog");
    }


    public static void main(String[] args) throws Exception {
	System.out.println("== TEST ==");

	try (Connection con = PlataformaUtils.getDESAConnection()) {
	    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
	    String json = "{p_IdEmpresa:1,p_IdUsuario:21,p_IdModulo:\"SOC\",p_UsaReglasNegocio: \"N\",p_ClienteDatosGenerales: [{IdCliente:1424326}],p_IdEstadoAutorizacion: \"AUT\"}";
	    EntradaConfirmarModificarClienteDatosGeneralesMasivo Objin = gson.fromJson(json, EntradaConfirmarModificarClienteDatosGeneralesMasivo.class);

	    ConfirmarModificarClienteDatosGeneralesMasivo demo = new ConfirmarModificarClienteDatosGeneralesMasivo();
	    System.out.println("ENTRADA");
	    System.out.println(Objin.toString());
	    System.out.println("SALIDA");

	    try {
		SalidaConfirmarModificarClienteDatosGeneralesMasivo ObjOut = demo.logica(con, Objin);
		con.rollback();
		System.out.println(ObjOut.toString());
	    } catch (Exception er) {
		er.printStackTrace();
	    } finally {
		con.rollback();
		con.close();
	    }
	}
	System.out.println("== END ==");
    }
    
}
