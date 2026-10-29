package cl.bcs.custodia.evc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CrearSolicitudAportePorDividendos;
import cl.bcs.fondomutuo.CrearSolicitudAportePorDividendos.EntradaCrearSolicitudAportePorDividendos;
import cl.bcs.fondomutuo.CrearSolicitudAportePorDividendos.InSolicitudesAporte;
import cl.bcs.fondomutuo.CrearSolicitudAportePorDividendos.SalidaCrearSolicitudAportePorDividendos;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CerrarEventoCapitalDVFFMM implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CerrarEventoCapitalDVFFMM.class);
	
	private final String SQL_SELECT_GET_CLIENTES = loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalDVFFMM_SelectEventoCapitalCliente.sql");
	private final String SQL_UPDATE_ESTADO_EVENTO_CAPITAL = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalDVFFMM_UpdateEstadoEventoCapital.sql");
	
	public static class Entrada{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdEventoCapital;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
    
    public static class DetalleEventoCapitalCliente{
    	public Long IdEventoCapitalCliente;
    	public Long IdEventoCapital;
    	public String IdTipoEventoCapital;
    	public Long Secuencia;
    	public Long IdSocioNegocio;
    	public String TipoSocioNegocio;
    	public Long IdPortafolio;
    	public String IdTipoDocumentoIdentidad;
    	public String DocumentoIdentidad;
    	public String NombreCliente;
    	public String IdClasificacionTipoInstrumento;
    	public String IdTipoInstrumento;
    	public Long IdInstrumento;
    	public String Nemo;
    	public Long IdInstrumentoNuevo;
    	public String NemoNuevo;
    	public Date FechaCierre;
    	public String FechaPago;
    	public BigDecimal CantidadDerecho;
    	public String TipoCartera;
    	public BigDecimal CantidadNueva;
    	public BigDecimal MontoCapital;
    	public BigDecimal MontoInteres;
    	public BigDecimal Total;
    	public String IdMoneda;
    	public Long SecuenciaCupon;
    	public String CodigoUsuarioAgente;
    	public String NombreAgente;
    	public String CodigoSucursal;
    	public Timestamp FechaHoraIngresoRegistro;
    	public Timestamp FechaHoraUltimaActualizacion;
    	public Long IdUsuario;
    	public String IdEstado;
    	public String IdBoveda;
    	public String IdBolsa;
    	public BigDecimal Precio;
    	public BigDecimal PrecioNuevo;
    	public Long NumeroDividendo;
    	public String Origen;
    	public Long IdEmpleadoEjecutivoPortafolio;
    	public Long IdCliente;
    	public String TipoCarteraNormativa;
    	public String IdRegimenFondoMutuo;
    	public String toString() {
			return Utils.printJson(this, false);
		}
    }
    
    public static class EventoCapital{
    	public Long IdEventoCapital;
    	public String IdEstado;
    	
    	public String toString() {
			return Utils.printJson(this, false);
		}
    }
	
	public static class Salida{
    	public List<MensajeSalida> P_OUT_MENSAJES;
    	public Long P_OUT_ERRORESCONTADOR;
    	
    	public Salida(){
    		this.P_OUT_ERRORESCONTADOR = 0L;
    		this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
    	}

    	public String toString() {
    		return Utils.printJson(this, false);
    	}
    }

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		Entrada jin = gson.fromJson(in.toString(), Entrada.class);
		Salida out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public Salida logica(Connection con, Entrada entrada) throws Exception{ 
		logger.info("INICIO::CerrarEventoCapitalDVFFMM");
		long inicioServicio = System.currentTimeMillis();
		Salida salida = new Salida();
		try {
			
			List<DetalleEventoCapitalCliente> eventosCapitalCliente = obtenerEventosCapitalCliente(con, entrada.p_IdEventoCapital);
			
			if(eventosCapitalCliente.size() == 0){
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_CLIENTES",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return salida;
			}
			
			SalidaCrearSolicitudAportePorDividendos salidaSolicitud = crearSolicitudAporte(con, entrada,eventosCapitalCliente);
			List<MensajeSalida> mensajesErrores = salidaSolicitud.P_OUT_MENSAJES
					.stream()
					.filter(mensaje->MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo))
					.collect(Collectors.toList());
			salida.P_OUT_MENSAJES.addAll(mensajesErrores);
			salida.P_OUT_ERRORESCONTADOR += salidaSolicitud.P_OUT_ERRORESCONTADOR;
			
			actualizarEventoCapital(con, entrada.p_IdEventoCapital);
			
			if(salidaSolicitud.P_OUT_ERRORESCONTADOR.equals(0L)) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_EXITO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			}
		}catch (Exception e) {
			logger.error("Error::CerrarEventoCapitalDVFFMM", e);
			throw e;
		}
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CerrarEventoCapitalDVFFMM", finServicio - inicioServicio);
		return salida;
	}
	
	private List<DetalleEventoCapitalCliente> obtenerEventosCapitalCliente(Connection con, Long idEventoCapitalCliente) throws Exception {
		logger.info("INICIO::CerrarEventoCapitalDVFFMM.obtenerEventosCapitalCliente");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String,Object> params =new HashMap<String,Object>();
		params.put("IdEventoCapital", idEventoCapitalCliente);
		List<DetalleEventoCapitalCliente> eventosCapitalCliente = sql.query(con, SQL_SELECT_GET_CLIENTES, params, DetalleEventoCapitalCliente.class);
		long fin = System.currentTimeMillis();
		logger.info("FIN::CerrarEventoCapitalDVFFMM.obtenerEventosCapitalCliente", fin - inicio);
		return eventosCapitalCliente;
		
	}

	private SalidaCrearSolicitudAportePorDividendos crearSolicitudAporte(Connection con, Entrada entrada,List<DetalleEventoCapitalCliente> eventosCapitalCliente) throws Exception {
		logger.info("INICIO::CerrarEventoCapitalDVFFMM.crearSolicitudAporte");
		long inicio = System.currentTimeMillis();
		EntradaCrearSolicitudAportePorDividendos entradaSolicitud = new EntradaCrearSolicitudAportePorDividendos();				
		entradaSolicitud.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaSolicitud.p_IdUsuario = entrada.p_IdUsuario;
		entradaSolicitud.p_IdModulo = entrada.p_IdModulo;
		entradaSolicitud.p_SimularOperacion = "N";
		entradaSolicitud.p_UsaReglasNegocio = "N";
		entradaSolicitud.p_IdTipoCanalIngreso = "AGE";//OPTIMUS(AGE),WEBSERVICE(WSR),FIORI(AGE),CLIENTE(CLI)
		//Debido a una mantencion correctiva de Sura el atributo p_NumeroDividendo pasa a tener por valor IdEventoCapital en vez de NumeroDividendo
		entradaSolicitud.p_NumeroDividendo = eventosCapitalCliente.size() > 0 ? eventosCapitalCliente.get(0).IdEventoCapital : 0L;

		
		for (DetalleEventoCapitalCliente eventoCapitalCliente : eventosCapitalCliente) {
			InSolicitudesAporte inSolicitud = new InSolicitudesAporte();
			inSolicitud.IdInterno = eventoCapitalCliente.IdEventoCapitalCliente;
			inSolicitud.IdCliente = eventoCapitalCliente.IdCliente;
			inSolicitud.IdPortafolio = eventoCapitalCliente.IdPortafolio;
			inSolicitud.IdSerieFondo = eventoCapitalCliente.Nemo;
			inSolicitud.NumeroCuotas = eventoCapitalCliente.CantidadDerecho;
			inSolicitud.IdMoneda = eventoCapitalCliente.IdMoneda;
			inSolicitud.FechaOperacion = eventoCapitalCliente.FechaPago;
			inSolicitud.FolioContrato = 0L;
			inSolicitud.IdRegimenTributario = eventoCapitalCliente.IdRegimenFondoMutuo;
			inSolicitud.IngresoAutomatico = "S";
			entradaSolicitud.p_SolicitudesAporte.add(inSolicitud);

		}

		CrearSolicitudAportePorDividendos crearSolicitud = new CrearSolicitudAportePorDividendos();
		SalidaCrearSolicitudAportePorDividendos salidaSolicitud = crearSolicitud.logica(con, entradaSolicitud);
		long fin = System.currentTimeMillis();
		logger.info("FIN::CerrarEventoCapitalDVFFMM.crearSolicitudAporte", fin - inicio);
		return salidaSolicitud;
	}
	
	private void actualizarEventoCapital(Connection con, Long idEventoCapital) throws Exception {
		logger.info("INICIO::CerrarEventoCapitalDVFFMM.actualizarEventoCapital");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<EventoCapital> updateList = new ArrayList<EventoCapital>();
		EventoCapital entradaUpdate = new EventoCapital();
		entradaUpdate.IdEventoCapital = idEventoCapital;
		entradaUpdate.IdEstado = "CER";
		updateList.add(entradaUpdate);
		sql.batch(con, SQL_UPDATE_ESTADO_EVENTO_CAPITAL, updateList, EventoCapital.class);
		long fin = System.currentTimeMillis();
		logger.info("FIN::CerrarEventoCapitalDVFFMM.actualizarEventoCapital", fin - inicio);
	}

		public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues()
					.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_IdEventoCapital\":366664,\"p_IdTipoInstrumento\":\"FFM\",\"p_IdEstado\":\"PRO\",\"p_Nemo\":\"FMLIQUIDEZ_A\"}";
			Entrada Objin = gson.fromJson(json, Entrada.class);
			
			CerrarEventoCapitalDVFFMM demo = new CerrarEventoCapitalDVFFMM();				
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			System.out.println("SALIDA");
			try {
				Salida ObjOut= demo.logica(con, Objin);
				con.rollback();
				System.out.println(ObjOut.toString());
			} catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
		}	
		System.out.println("== END ==");
    }
}
