package cl.bcs.plataforma.evt;

/*
 * Clase generada automaticamente por Sp2Java
 */
	 
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.evc.NotificarEventoCapital.ObjEventoCapitalCliente_Entrada;
import cl.bcs.custodia.evc.NotificarEventoCapital.ObjEventoCapitalEvento;
import cl.bcs.normativa.circulares.NotificarNCG365FFMM.ArchivoAdjunto;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;


public class AgregarEvento implements JavaCall {
	
	
	private static final Logger logger = LogManager.getLogger(AgregarEvento.class);
	
	private final String SQL_INSERT_EVENTO_TABLA_PASO 		= loadResourceToString("cl/bcs/plataforma/evt/sql/AgregarEvento_InsertEventoTablaPaso.sql");
	private final String SQL_INSERT_DIRECCIONES_TABLA_TEMP	= loadResourceToString("cl/bcs/plataforma/evt/sql/InsertDirecciones_Temp.sql");
	private final String SQL_INSERT_ARCHIVOS_ADJUNTOS_TEMP 	= loadResourceToString("cl/bcs/plataforma/evt/sql/AgregarEvento_InsertArchivos_Temp.sql");
	private final String SQL_INSERT_VALORES_TABLA_TEMP		= loadResourceToString("cl/bcs/plataforma/evt/sql/InsertValores_Temp.sql");
	private final String SQL_SELECT_EVENTO_TABLA_PASO 		= loadResourceToString("cl/bcs/plataforma/evt/sql/AgregarEvento_SelectEventoTablaPaso.sql");	
	private final String SQL_INSERT_ORIGEN_EVENTO			= loadResourceToString("cl/bcs/plataforma/evt/sql/AgregarEvento_InsertOrigenEvento.sql");
	private final String SQL_INSERT_EVENTO 					= loadResourceToString("cl/bcs/plataforma/evt/sql/AgregarEvento_InsertEvento.sql");	
	private final String SQL_INSERT_EVENTO_DETALLE 			= loadResourceToString("cl/bcs/plataforma/evt/sql/AgregarEvento_InsertEventoDetalle.sql");	
	private final String SQL_INSERT_EVENTO_ARCHIVO_ADJUNTO 	= loadResourceToString("cl/bcs/plataforma/evt/sql/AgregarEvento_InsertEventoArchivoAdjunto.sql");	
	private final String SQL_INSERT_EVENTO_DIRECCIONES 		= loadResourceToString("cl/bcs/plataforma/evt/sql/AgregarEvento_InsertEventoDirecciones.sql");	
	private final String SQL_SELECT_IDEVENTO 				= loadResourceToString("cl/bcs/plataforma/evt/sql/AgregarEvento_SelectIdEvento.sql");
	private final String SQL_DELETE_EVENTO_TABLA_PASO 		= loadResourceToString("cl/bcs/plataforma/evt/sql/AgregarEvento_DeleteEventoTablaPaso.sql");
	private final String SQL_SELECT_CONFIGURACION 		= loadResourceToString("cl/bcs/plataforma/evt/sql/ObtenerConfiguracionAgregarEventoDetalle.sql");
	private final String SQL_SELECT_EVENTO_ARCHIVO_ADJUNTO_TEMP 	= loadResourceToString("cl/bcs/plataforma/evt/sql/AgregarEvento_SelectEventoArchivoAdjuntoTablaTemporal.sql");
	
	
	public static class ServiceEntrada {	
		public Long P_IDEMPRESA;
		public Long P_IDUSUARIO;
		public String		 P_IDMODULO;
		public List<ObjEVENTOS>  P_EVENTOS;
		public List<ObjVALORES>  P_VALORES;
		public List<ObjARCHIVOSADJUNTOS>  P_ARCHIVOSADJUNTOS;
		public List<ObjIDDIRECCIONES>  P_IDDIRECCIONES;
		public List<String> P_DESCRIPCIONORIGEN = new ArrayList<>();
		public List<String> P_IDTIPOEVENTOORIGEN = new ArrayList<>(); 
		public List<String> P_NEMOEVENTOORIGEN = new ArrayList<>(); 
		public List<Long> P_SECUENCIAORIGEN = new ArrayList<>(); 
		public List<String> P_TIPOEVENTOORIGEN = new ArrayList<>();
		public String P_CONTROLPROCESO = "";
	
		public ServiceEntrada() {
			P_ARCHIVOSADJUNTOS = new ArrayList<>();
			P_IDDIRECCIONES = new ArrayList<>();
			P_VALORES = new ArrayList<>();
			P_EVENTOS = new ArrayList<>();
		}
			
		public String toString() {
			return Utils.printJson(this, false);
		}	
		
		public ServiceEntrada addValor(Long IdSecuencia,String llave,String valor) {
			ObjVALORES obj=new ObjVALORES();
			obj.IdSecuencia	= IdSecuencia;
			obj.Llave		= llave;
			
			obj.Valor		= valor;    //Para valores pequennos (menores a 5000 caracteres, tipo de datos NVARCHAR(5000))			
			this.P_VALORES.add(obj);
			return this;
		}
		

		
		public ServiceEntrada addValorBlob(Long IdSecuencia,String llave,String valorBlob) {
			ObjVALORES obj=new ObjVALORES();
			obj.IdSecuencia	= IdSecuencia;
			obj.Llave		= llave;			
			obj.ValorBlob	= valorBlob; //Para valores grandes (mayores a 5000 caracteres, tipo de datos TEXT)
			this.P_VALORES.add(obj);
			return this;
		}
	}
	
	public static class ServiceSalida {
		public List<ObjIDEVENTO>  P_OUT_IDEVENTO;

		public ServiceSalida() {
			P_OUT_IDEVENTO = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}	
	
	/*
	 * 
	 * Clases utilitarias que representan objetos de procesamiento
	 * 
	 */
	
	public static class ObjEVENTOS {
		public String	CodigoTipoEvento;
		public Long 	IdSecuencia;
		public String 	FechaCreacionSistema;
		public Long 	IdPersonaRemitente;
		public Long 	IdPersonaDestinatario;
		public Long 	IdPortafolio;
		public Long 	IdEvento = 0L;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}

	public static class ObjVALORES {
		public Long 		 IdSecuencia;
		public String		 Llave;
		public Long 		 IdEvento;
		public Long 		 IdEventoDetalle;
		public String		 Valor;             //Para valores pequennos (menores a 5000 caracteres, tipo de datos NVARCHAR(5000))
		public String		 ValorBlob ;  //Para valores grandes (mayores a 5000 caracteres, tipo de datos TEXT)
		
		@Override
		public String toString() {
			return "ObjVALORES [IdSecuencia=" + IdSecuencia + ", Llave=" + Llave +", IdEvento="+ IdEvento +", IdEventoDetalle="+IdEventoDetalle
					+ ", Valor=" + Valor + ", ValorBlob=" + ValorBlob + "]";
		}
	}

	public static class ObjARCHIVOSADJUNTOS {
		public Long IdSecuencia;
		public byte[]		 ArchivoAdjunto;
		public String		 Nombre;
		public String		 Contrasenia;
		public Long			IdEvento;
	}

	public static class ObjIDDIRECCIONES {
		public Long IdSecuencia;
		public Long IdDireccion;
		public String		 TipoDestinatario;
		@Override
		public String toString() {
			return "ObjIDDIRECCIONES [IdSecuencia=" + IdSecuencia + ", IdDireccion=" + IdDireccion
					+ ", TipoDestinatario=" + TipoDestinatario + "]";
		}
	}
	
	
	public static class ObjIDEVENTO {
		public String CodigoTipoEvento;
		public Long IdEvento;
		public Long IdSecuencia;
		public String toString() {
            return Utils.printJson(this, false);
        }
	}
	
	public static class ObjDATOSGENERALES {
		public Long IdEmpresa;
		public Long IdUsuario;
		public Long IdOrigenEvento;
		public String toString() {
            return Utils.printJson(this, false);
        }
	}
	
	public static class OrigenEvento{
		public Long IdOrigenEvento;
		public Long IdEmpresa;
		public Long IdUsuario;
		public String IdModulo;
		public String CodigoTipoEvento;
		public Long CantidadEventos;
		public String	DescripcionOrigen; 
		public String	SecuenciaOrigen; 
		public String	TipoEventoOrigen;
		public String IdEstado;
		
		public OrigenEvento(Long idOrigenEvento, Long idEmpresa, Long idUsuario, String idModulo, String codigoTipoEvento, Long cantidadEventos, String descripcionOrigen, String secuenciaOrigen, String tipoEventoOrigen, String idEstado){
			super();
			IdOrigenEvento = idOrigenEvento;
			IdEmpresa = idEmpresa;
			IdUsuario = idUsuario;
			IdModulo = idModulo;
			CodigoTipoEvento = codigoTipoEvento;
			CantidadEventos = cantidadEventos;
			DescripcionOrigen = descripcionOrigen; 
			SecuenciaOrigen = secuenciaOrigen;
			TipoEventoOrigen = tipoEventoOrigen;
			IdEstado = idEstado;
		}

		public String toString() {
            return Utils.printJson(this, false);
        }
		
	}
	
	public static class Configuraciones  {
		public Long IdConfiguracionEventoDetalle; 
		public Long IdConfiguracionEvento; 
		public Long IdUsuario;
		public java.sql.Timestamp FechaCreacion;
		public String CodigoTipoEvento; 
		public String IdEstado;
		   
		public String toString() {
			return Utils.printJson(this, false);
	    }
	}
	
	
	
	/*
	 *  Objeto de transporte
	 */
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();		
		ServiceEntrada in=gson.fromJson(jin.toString() ,ServiceEntrada.class);
		ServiceSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/*
	 * Logica Servicio
	 * 	 
	 */
	@Trace
	public ServiceSalida logica(Connection con, ServiceEntrada in) throws Exception {
		Long init = System.currentTimeMillis();
		ServiceSalida out = new ServiceSalida();
		logger.info("Inicio servicio AgregarEvento");
		
		SQL _sql = new SQL();

		HashMap<String, Object> parmsIN = new HashMap<String, Object>();
		
		
		
		
		List<Configuraciones> AllConfig = _sql.query(con, SQL_SELECT_CONFIGURACION, parmsIN, Configuraciones.class);
		
	
		List <Configuraciones> FilteredConfig =  new ArrayList<Configuraciones>();
		
		for(Configuraciones conf : AllConfig) {
			if(!conf.IdEstado.equalsIgnoreCase("INA")) {
				FilteredConfig.add(conf);
			}
		}
		
	
		List <ObjEVENTOS> EventosClean =  new ArrayList<ObjEVENTOS>();
		List <ObjVALORES> DetalleClean =  new ArrayList<ObjVALORES>();
		List <ObjIDDIRECCIONES> DireccionesClean =  new ArrayList<ObjIDDIRECCIONES>();
		List <ObjARCHIVOSADJUNTOS> ArchivosClean =  new ArrayList<ObjARCHIVOSADJUNTOS>();
		
		for(Configuraciones filtros: FilteredConfig) {
			for(ObjEVENTOS eventos:  in.P_EVENTOS) {
				if(eventos.CodigoTipoEvento.equalsIgnoreCase(filtros.CodigoTipoEvento)) {
					EventosClean.add(eventos);
				}
			}
		}
		
		for(ObjEVENTOS evento: EventosClean) {
			for(ObjVALORES valor: in.P_VALORES) {
				if(valor.IdSecuencia!=null && valor.IdSecuencia.compareTo(evento.IdSecuencia)==0) {
					DetalleClean.add(valor);
				}
			}
		}
		for(ObjEVENTOS evento: EventosClean) {
			for(ObjIDDIRECCIONES direccion: in.P_IDDIRECCIONES) {
				if(direccion.IdSecuencia.compareTo(evento.IdSecuencia)==0) {
					DireccionesClean.add(direccion);
				}
			}
		}
		
		SQL.exec(con,SQL_DELETE_EVENTO_TABLA_PASO);
		
		List<Long> ids = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::EVT.Evento", (long)EventosClean.size());
		
		
		for(int i =0 ; i<EventosClean.size(); i++) {
			ObjEVENTOS evento =EventosClean.get(i);
			evento.IdEvento = ids.get(i);
			
		}
		
		for(ObjEVENTOS evento: EventosClean) {
			for(ObjARCHIVOSADJUNTOS archivo: in.P_ARCHIVOSADJUNTOS) {
				if(archivo.IdSecuencia.compareTo(evento.IdSecuencia)==0) {
					archivo.IdEvento = evento.IdEvento;
					ArchivosClean.add(archivo);
				}
			}
		}
		
		//Se guardan datos de entrada en tabla global temporal
		logger.info("Se guardan datos de entrada en tabla global temporal");
		_sql.batch(con, SQL_INSERT_EVENTO_TABLA_PASO,EventosClean, ObjEVENTOS.class);
		
		
		//Se guardan datos de entrada en tabla global temporal
		logger.info("Se guardan direcciones en tabla global temporal");
		_sql.batch(con, SQL_INSERT_DIRECCIONES_TABLA_TEMP, DireccionesClean , ObjIDDIRECCIONES.class);
		
		logger.info("Se guardan archivos adjuntos en tabla global temporal");
		_sql.batch(con, SQL_INSERT_ARCHIVOS_ADJUNTOS_TEMP, ArchivosClean , ObjARCHIVOSADJUNTOS.class);
	
	
		//Para pruebas conocer el contenido de global)
		logger.debug("INICIO::SQL_SELECT_EVENTO_TABLA_PASO");
		List<ObjEVENTOS> eventosTemp = _sql.query(con, SQL_SELECT_EVENTO_TABLA_PASO, new HashMap<String, Object>(), ObjEVENTOS.class);
		logger.debug("FIN::SQL_SELECT_EVENTO_TABLA_PASO");
		//obtener secuencia origen evento
		long idOrigenEvento = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::EVT.OrigenEvento", 1l).get(0);
				
		// Se guardan datos del evento
		logger.info("Se guardan los datos generales del evento");
		List<ObjDATOSGENERALES> lstDatosGenerales = new ArrayList<ObjDATOSGENERALES>();
		ObjDATOSGENERALES objDatosGenerales = new ObjDATOSGENERALES();
		objDatosGenerales.IdEmpresa = in.P_IDEMPRESA;
		objDatosGenerales.IdUsuario = in.P_IDUSUARIO;
		objDatosGenerales.IdOrigenEvento= idOrigenEvento;
		lstDatosGenerales.add(objDatosGenerales);		
		_sql.batch(con, SQL_INSERT_EVENTO, lstDatosGenerales, ObjDATOSGENERALES.class);
		
		// Se guardan datos del detalle del evento
		logger.info("Se guardan datos del detalle del evento");
	
		HashMap<Long, Object> hashEventos = Utils.List2Hash(EventosClean,"IdSecuencia");
		Long max = DetalleClean.size() + 1L;
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::EVT.EventoDetalle", max);
		int io = 0;
		
		for(ObjVALORES detalle : DetalleClean) {
			ObjEVENTOS evento = (ObjEVENTOS) hashEventos.get(detalle.IdSecuencia);
			if (evento == null) continue;
			else {
				detalle.IdEvento = evento.IdEvento;
				detalle.IdEventoDetalle = secuencias.get(io++) ;
				detalle.Valor = detalle.Valor==null ? "" : detalle.Valor; 
			}
		}
	
		_sql.batch(con,SQL_INSERT_EVENTO_DETALLE, DetalleClean, ObjVALORES.class);
		
		// Se guardan datos de archivos adjuntos
		logger.info("Se guardan datos de archivos adjuntos");
		if (ArchivosClean.size() > 0) {
			SQL sql =new SQL();
			List<ArchivoAdjunto> adjuntos = sql.query(con,SQL_SELECT_EVENTO_ARCHIVO_ADJUNTO_TEMP, new HashMap<String,Object>(0), ArchivoAdjunto.class);
			if(!adjuntos.isEmpty()) {
				for(ArchivoAdjunto adjunto : adjuntos) {
					adjunto.Archivo = ArchivosClean.stream().filter(adj -> adj.Nombre.equals(adjunto.NombreArchivo ) && adj.IdEvento.equals(adjunto.IdEvento)).count() == 0 ? adjunto.Archivo : ArchivosClean.stream().filter(adj -> adj.Nombre.equals(adjunto.NombreArchivo) && adj.IdEvento.equals(adjunto.IdEvento)).findFirst().get().ArchivoAdjunto;
				}
			}
			
			sql.batch(con, SQL_INSERT_EVENTO_ARCHIVO_ADJUNTO, adjuntos, ArchivoAdjunto.class);
		}
		
		
		// Se guardan datos de direcciones de envio
		 logger.info("Se guardan datos de direcciones de envio");
		 if(DireccionesClean.size() >0) {
			_sql.exec(con,SQL_INSERT_EVENTO_DIRECCIONES );
		}
		 

		//Prepara Salida
		logger.info("Prepara Salida del proceso");
		logger.debug("INICIO::SQL_SELECT_IDEVENTO");
		out.P_OUT_IDEVENTO = _sql.query(con, SQL_SELECT_IDEVENTO, new HashMap<String, Object>(), ObjIDEVENTO.class);
		logger.debug("FIN::SQL_SELECT_IDEVENTO");
		
		//si se creo algun evento, insertar el origen evento.
		if(!out.P_OUT_IDEVENTO.isEmpty()) {
			String idEstado="";
			
			if(in.P_CONTROLPROCESO.equals("S")) {
				idEstado = "VIG";
			}else if(in.P_CONTROLPROCESO.equals("N")){
				idEstado = "APD";
			}else {
				idEstado = "VIG";
			}
			String codigoTipoEvento = (!out.P_OUT_IDEVENTO.isEmpty()) ? out.P_OUT_IDEVENTO.get(0).CodigoTipoEvento : null;
			Long cantidadEventos = (long) out.P_OUT_IDEVENTO.size();
			
			//Casteo de Lista a String
			String listaTipoEventoOrigen = String.join(", ", in.P_TIPOEVENTOORIGEN);
			String listaNemoEventoOrigen = String.join(", ", in.P_NEMOEVENTOORIGEN);
			List<String> listaSecuenciaOrigenAux = in.P_SECUENCIAORIGEN.stream().map(Object::toString).collect(Collectors.toList());
			String listaSecuenciaOrigen = String.join(", ", listaSecuenciaOrigenAux);
			String listaIdTipoEventoOrigen = String.join(", ", in.P_IDTIPOEVENTOORIGEN);
			String descripcionOrigen = ""; 
			String secuenciaOrigen = ""; 
			String TipoEventoOrigen = "";
			
			//Filtro para mostrar secuencia, descripcion y tipo de evento solo para eventos capitales
			if(codigoTipoEvento.equals("CARTAVAR_CAP") || codigoTipoEvento.equals("CARTAVAR_CAP_DV")) {
				descripcionOrigen = "Notificación de " + listaTipoEventoOrigen + " con el nemotécnico " +  listaNemoEventoOrigen; 
				secuenciaOrigen = listaSecuenciaOrigen; 
				TipoEventoOrigen = listaIdTipoEventoOrigen;
		
			}
			
			_sql.batch(con, SQL_INSERT_ORIGEN_EVENTO, Arrays.asList(new OrigenEvento(idOrigenEvento, in.P_IDEMPRESA, in.P_IDUSUARIO, in.P_IDMODULO, codigoTipoEvento, cantidadEventos, descripcionOrigen,  secuenciaOrigen,  TipoEventoOrigen, idEstado)), OrigenEvento.class);
		}
				
		logger.info("Termino del servicio AgregarEvento, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
}
