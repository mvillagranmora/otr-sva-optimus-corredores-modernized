package cl.bcs.plataforma.aut;

/*
 * Clase generada automaticamente por Sp2Java
 */

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.NotificarAutorizaciones;
import cl.bcs.plataforma.aut.NotificarAutorizaciones.NotificarAutorizacion_Entrada;
import cl.bcs.plataforma.aut.NotificarAutorizaciones.NotificarEventoAutorizacion_Salida;
import cl.bcs.plataforma.aut.NotificarAutorizaciones.ObjEventoAutorizacion_Entrada;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CrearEventoAutorizacionMasivo implements JavaCall {

	/*
	 * El log
	 */
	static final Logger logger = LogManager.getLogger(CrearEventoAutorizacionMasivo.class);

	/*
	 * 
	 * Queries
	 * 
	 */
	// private final String SQL_XXX = loadResourceToString(".....");

	/*
	 * 
	 * Clases utilitarias que representan objetos IN/OUT
	 */
	public static class ServiceEntrada {

		public Long						P_IDEMPRESA;
		public Long						P_IDUSUARIO;
		public String					P_IDMODULO;
		public String					P_SIMULAROPERACION;
		public String					P_USAREGLASNEGOCIO;
		public List<ObjTABLAEVENTOS>	P_TABLAEVENTOS;
		public List<ObjTABLADETALLE> 	P_DETALLE = null ;
		public ServiceEntrada() {
			this.P_TABLAEVENTOS = new ArrayList<ObjTABLAEVENTOS>();
			this.P_DETALLE      = new ArrayList<ObjTABLADETALLE>(); 
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ServiceSalida {

		public List<ObjIDEVENTO>			P_OUT_IDEVENTO;
		public Long							P_OUT_ADVERTENCIASCONTADOR;
		public List<ObjADVERTENCIASDETALLE>	P_OUT_ADVERTENCIASDETALLE;
		public Long							P_OUT_ERRORESCONTADOR;
		public List<ObjERRORESDETALLE>		P_OUT_ERRORESDETALLE;
		
		public ServiceSalida() {
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_IDEVENTO = new ArrayList<ObjIDEVENTO>();
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<ObjADVERTENCIASDETALLE>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<ObjERRORESDETALLE>();
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

	public static class ObjTABLAEVENTOS {
		public Long			IdUnion = null;
		public String		IdTipoEvento;
		public String		IdModuloSolicita;
		public Long			IdUsuarioSolicita;
		public String		Fecha;
		public String		IdTipoOperacion;
		public String		IdConcepto;
		public String		NumeroOperacionConcepto;
		public String		FechaOperacionConcepto;
		public Long			IdCliente;
		public Long			IdFuncionario;
		public Long			IdProveedor;
		public String		IdTipoProducto;
		public String		IdClasificacionTipoInstrumento;
		public String		IdTipoInstrumento;
		public Long			IdInstrumento;
		public String		Nemo;
		public Long			Cantidad;
		public BigDecimal	Monto;
		public BigDecimal	Precio;
		public BigDecimal	Tasa;
		public String		IdMoneda;
		public String		Glosa;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ObjTABLADETALLE {
		public Long		  IdUnion          = null;
		public String	  NombreCampo      = "";
		public String	  TipoDato         = "";
		public String	  Valor            = "";
		public String     Clasificacion    = "";
		
	}
	public static class ObjIDEVENTO {
		public Long		IdEvento;
		public String	NumeroOperacionConcepto;
	}

	public static class ObjADVERTENCIASDETALLE {
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;
	}

	public static class ObjERRORESDETALLE {
		public String	Codigo;
		public String	Descripcion;
	}

	/*
	 * Objeto de transporte
	 */
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ServiceEntrada in = gson.fromJson(jin.toString(), ServiceEntrada.class);
		ServiceSalida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	/*
	 * Logica Servicio
	 * 
	 */
	@Trace
	public ServiceSalida logica(Connection con, ServiceEntrada in) throws Exception {
		SQL sql = new SQL();
		ServiceSalida out = new ServiceSalida();
		logger.info("INICIO");
		try {

			// ------------------------------
			// Crea las tablas temporales

			SQL.exec(con,
					"CREATE LOCAL TEMPORARY TABLE #CrearEventoAutorizacionMasivo_P_TABLAEVENTOS (\"IdTipoEvento\" NVARCHAR(6),\"IdModuloSolicita\" NVARCHAR(3),\"IdUsuarioSolicita\" BIGINT,\"Fecha\" NVARCHAR(10),\"IdTipoOperacion\" NVARCHAR(6),\"IdConcepto\" NVARCHAR(6),\"NumeroOperacionConcepto\" NVARCHAR(40),\"FechaOperacionConcepto\" NVARCHAR(10),\"IdCliente\" BIGINT,\"IdFuncionario\" BIGINT,\"IdProveedor\" BIGINT,\"IdTipoProducto\" NVARCHAR(4),\"IdClasificacionTipoInstrumento\" NVARCHAR(2),\"IdTipoInstrumento\" NVARCHAR(3),\"IdInstrumento\" BIGINT,\"Nemo\" NVARCHAR(40),\"Cantidad\" BIGINT,\"Monto\" DECIMAL(26,8),\"Precio\" DECIMAL(26,8),\"Tasa\" DECIMAL(26,8),\"IdMoneda\" NVARCHAR(3),\"Glosa\" NVARCHAR(500),\"IdUnion\" BIGINT)"); 

			sql.batch(con,
					"INSERT INTO #CrearEventoAutorizacionMasivo_P_TABLAEVENTOS (\"IdTipoEvento\",\"IdModuloSolicita\",\"IdUsuarioSolicita\",\"Fecha\",\"IdTipoOperacion\",\"IdConcepto\",\"NumeroOperacionConcepto\",\"FechaOperacionConcepto\",\"IdCliente\",\"IdFuncionario\",\"IdProveedor\",\"IdTipoProducto\",\"IdClasificacionTipoInstrumento\",\"IdTipoInstrumento\",\"IdInstrumento\",\"Nemo\",\"Cantidad\",\"Monto\",\"Precio\",\"Tasa\",\"IdMoneda\",\"Glosa\",\"IdUnion\") VALUES (:IdTipoEvento,:IdModuloSolicita,:IdUsuarioSolicita,:Fecha,:IdTipoOperacion,:IdConcepto,:NumeroOperacionConcepto,:FechaOperacionConcepto,:IdCliente,:IdFuncionario,:IdProveedor,:IdTipoProducto,:IdClasificacionTipoInstrumento,:IdTipoInstrumento,:IdInstrumento,:Nemo,:Cantidad,:Monto,:Precio,:Tasa,:IdMoneda,:Glosa, :IdUnion)",
					in.P_TABLAEVENTOS, ObjTABLAEVENTOS.class);
			
			SQL.exec(con, 
					"CREATE LOCAL TEMPORARY TABLE #CrearEventoAutorizacionMasivo_P_TABLADETALLE (\"IdUnion\" BIGINT,\"NombreCampo\" NVARCHAR(100),\"TipoDato\" NVARCHAR(50),\"Valor\" NVARCHAR(300),\"Clasificacion\" NVARCHAR(50))");

			sql.batch(con,
					"INSERT INTO #CrearEventoAutorizacionMasivo_P_TABLADETALLE (\"IdUnion\",\"NombreCampo\",\"TipoDato\",\"Valor\", \"Clasificacion\") VALUES (:IdUnion,:NombreCampo,:TipoDato,:Valor,:Clasificacion)",
					in.P_DETALLE, ObjTABLADETALLE.class);
 

			// ------------------------------
			// Inserta tablas temporales globales

			// ------------------------------
			// Ejecuta el SP
			HashMap<String, Object> parmsIN = new HashMap<String, Object>();

			parmsIN.put("P_IDEMPRESA", in.P_IDEMPRESA);
			parmsIN.put("P_IDUSUARIO", in.P_IDUSUARIO);
			parmsIN.put("P_IDMODULO", in.P_IDMODULO);
			parmsIN.put("P_SIMULAROPERACION", in.P_SIMULAROPERACION);
			parmsIN.put("P_USAREGLASNEGOCIO", in.P_USAREGLASNEGOCIO);
			parmsIN.put("P_TABLAEVENTOS", in.P_TABLAEVENTOS);
			parmsIN.put("P_TABLADETALLE", in.P_DETALLE); 

			HashMap<String, Class> parmsOUT = new HashMap<String, Class>();

			parmsOUT.put("P_OUT_IDEVENTO", ObjIDEVENTO.class);
			parmsOUT.put("P_OUT_ADVERTENCIASCONTADOR", Long.class);
			parmsOUT.put("P_OUT_ADVERTENCIASDETALLE", ObjADVERTENCIASDETALLE.class);
			parmsOUT.put("P_OUT_ERRORESCONTADOR", Long.class);
			parmsOUT.put("P_OUT_ERRORESDETALLE", ObjERRORESDETALLE.class);
			
			logger.debug("INICIO::CrearEventoAutorizacionMasivo");
			String queryCall = "call \"BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacionMasivo\" (:P_IDEMPRESA,:P_IDUSUARIO,:P_IDMODULO,:P_SIMULAROPERACION,:P_USAREGLASNEGOCIO,#CrearEventoAutorizacionMasivo_P_TABLAEVENTOS, #CrearEventoAutorizacionMasivo_P_TABLADETALLE,$P_OUT_IDEVENTO,@P_OUT_ADVERTENCIASCONTADOR,$P_OUT_ADVERTENCIASDETALLE,@P_OUT_ERRORESCONTADOR,$P_OUT_ERRORESDETALLE)";
			logger.debug("FIN::CrearEventoAutorizacionMasivo");
			HashMap<String, Object> rpt = sql.call(con, queryCall, parmsIN, parmsOUT);

			// ------------------------------
			// Lee scalar

			out.P_OUT_ADVERTENCIASCONTADOR = (Long) rpt.get("P_OUT_ADVERTENCIASCONTADOR");
			out.P_OUT_ERRORESCONTADOR = (Long) rpt.get("P_OUT_ERRORESCONTADOR");

			// ------------------------------

			out.P_OUT_IDEVENTO = (List<ObjIDEVENTO>) rpt.get("P_OUT_IDEVENTO");
			out.P_OUT_ADVERTENCIASDETALLE = (List<ObjADVERTENCIASDETALLE>) rpt.get("P_OUT_ADVERTENCIASDETALLE");
			out.P_OUT_ERRORESDETALLE = (List<ObjERRORESDETALLE>) rpt.get("P_OUT_ERRORESDETALLE");
			
			// crear objeto de tipo noticacion
			if (out.P_OUT_ERRORESCONTADOR==0) {
				logger.debug("Entra en proceso");
				NotificarAutorizaciones notificarEvento = new NotificarAutorizaciones();	
				NotificarAutorizacion_Entrada NotifIN = new NotificarAutorizacion_Entrada();
				NotifIN.p_IdUsuario = in.P_IDUSUARIO;
				NotifIN.p_IdEmpresa = in.P_IDEMPRESA;
				NotifIN.p_IdModulo = in.P_IDMODULO;

				
				for(ObjIDEVENTO objid : out.P_OUT_IDEVENTO){
					ObjEventoAutorizacion_Entrada objIdEventoAutorizacion = new ObjEventoAutorizacion_Entrada();
					objIdEventoAutorizacion.p_IdEventoAutorizacion=objid.IdEvento;
					NotifIN.p_IdsEventoAutorizacion.add(objIdEventoAutorizacion);
				}
			
				
				try {
					NotificarEventoAutorizacion_Salida ObjOut= notificarEvento.logica(con, NotifIN);
					List<MensajeSalida> mensajeAdvertencia = ObjOut.P_OUT_MENSAJES.stream().filter(s -> s.tipo.equalsIgnoreCase("ADVERTENCIA")).collect(Collectors.toList());	
					if(ObjOut.P_OUT_ERRORESCONTADOR>0) {
						//out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA,"UI_NOTIFICACION_AUTORIZACION_NO_SE_REALIZO",new HashMap<String,String>(),con,in.p_IdEmpresa) );								
					}else if(mensajeAdvertencia.size()>0) {
						//out.P_OUT_MENSAJES=ObjOut.P_OUT_MENSAJES.stream().filter(s -> s.tipo.equalsIgnoreCase("ADVERTENCIA")).collect(Collectors.toList());	
					}
				}
				catch(Exception e) {				
					//out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA,"UI_NOTIFICACION_AUTORIZACION_NO_SE_REALIZO",new HashMap<String,String>(),con,in.p_IdEmpresa) );				
				}
				
			}
			
			
			// ------------------------------
			// Lee tablas temporales globales

		} 
		catch(Exception e) {
			throw e;
		}
		finally {
			// Elimina tablas temporales locales

			try {
				SQL.exec(con, "DROP TABLE #CrearEventoAutorizacionMasivo_P_TABLAEVENTOS");
				SQL.exec(con, "DROP TABLE #CrearEventoAutorizacionMasivo_P_TABLADETALLE");
			} catch (Exception er) {
			}

		}
		logger.info("TERMINO");
		return out;

	}
	
	public static void main(String[] args) throws Exception {
		
		try {
			Connection con = PlataformaUtils.getDESAConnection();
			
			CrearEventoAutorizacionMasivo c = new CrearEventoAutorizacionMasivo();
			ServiceEntrada in = new ServiceEntrada();
			in.P_IDEMPRESA = 1L;
			in.P_IDUSUARIO = 25L;
			
			ObjTABLAEVENTOS evento = new ObjTABLAEVENTOS();;
			
			evento.IdTipoEvento = "EJEMPL";
			evento.IdModuloSolicita = "ORD";
			evento.IdUsuarioSolicita = 25L;
			evento.Fecha = "2020-03-17";
			evento.IdTipoOperacion = "";
			evento.IdConcepto = "INGORD";
			evento.NumeroOperacionConcepto = "0";
			evento.FechaOperacionConcepto = "0001-01-01";
			evento.IdCliente = 0L;
			evento.IdFuncionario = 0L;
			evento.IdProveedor = 0L;
			evento.IdTipoProducto = "";
			evento.IdClasificacionTipoInstrumento = "";
			evento.IdTipoInstrumento = "";
			evento.IdInstrumento = 0L;
			evento.Nemo = "";
			evento.Cantidad = 0L;
			evento.Monto = BigDecimal.ZERO;
			evento.Precio = BigDecimal.ZERO;
			
			
			in.P_TABLAEVENTOS.add(evento);
			ServiceSalida asdf = c.logica(con, in);
			con.commit();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

}
