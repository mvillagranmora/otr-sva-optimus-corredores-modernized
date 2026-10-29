package cl.bcs.ordenes;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CambiarEstadoOrdenes.CambiarEstadoOrdenes_Entrada;
import cl.bcs.ordenes.CambiarEstadoOrdenes.CambiarEstadoOrdenes_Salida;
import cl.bcs.ordenes.CambiarEstadoOrdenes.Orden_Entrada;
import cl.bcs.ordenes.RutearOrdenes.RutearOrdenes_Entrada;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket.EntradaServicio;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket.MensajeWebSocket;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarAutorizacionOrdenes implements JavaCall { 
	
	private Logger log = LogManager.getLogger(ConfirmarAutorizacionOrdenes.class);
	private final String SQL_INSERT_TABLA_PASO  	= loadResourceToString("cl/bcs/ordenes/sql/ConfirmarAutorizacionOrdenes_InsertTablaPaso.sql");
	private final String SQL_UPDATE_ESTADO_ORDEN  	= loadResourceToString("cl/bcs/ordenes/sql/ConfirmarAutorizacionOrdenes_UpdateEstadoOrden.sql");
	private final String SQL_SELECT_ORDEN_AUTORIZADAS 	= loadResourceToString("cl/bcs/ordenes/sql/ConfirmarAutorizacionOrdenes_SelectOrdenAutorizadas.sql");
	private final String SQL_UPDATE_FECHA_AUTORIZACION 	= loadResourceToString("cl/bcs/ordenes/sql/ConfirmarAutorizacionOrdenes_UpdateFechaAutorizacion.sql");
	private final ConstantesOrdenes _c= new ConstantesOrdenes();
	
	public static class EntradaConfirmarAutorizacionOrdenes {
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		
		public List<OrdenesPorAutorizar> p_Ordenes;
		
		public EntradaConfirmarAutorizacionOrdenes() {
			this.p_Ordenes = new ArrayList<OrdenesPorAutorizar>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class OrdenesPorAutorizar {
		public Long 	IdOrden;
		
		public OrdenesPorAutorizar() {}
		public OrdenesPorAutorizar(Long idOrden) {
			super();
			IdOrden = idOrden;
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OrdenesAutorizadas {
		public Long 	IdOrden;
		public String	Nemotecnico;
		public Timestamp FechaAceptacion;
		public Timestamp FechaAutorizacion;
		//para averiguar el ruteo
		public String IdBolsaDistribucion;
		public String IdTipoCanalDistribucion;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConfirmarAutorizacionOrdenes {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
				
		public SalidaConfirmarAutorizacionOrdenes() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();			
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
			
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaConfirmarAutorizacionOrdenes in = gson.fromJson(jin.toString(),EntradaConfirmarAutorizacionOrdenes.class);
		SalidaConfirmarAutorizacionOrdenes out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	
	
	
	public SalidaConfirmarAutorizacionOrdenes logica(Connection conn,  EntradaConfirmarAutorizacionOrdenes in) throws Exception {
		SalidaConfirmarAutorizacionOrdenes out = new SalidaConfirmarAutorizacionOrdenes();
		SQL sql = new SQL();
		log.info("INICIO::ConfirmarAutorizacionOrdenes");
		
		//ingresar ordenes a autorizar a tabla temporal
		sql.batch(conn, SQL_INSERT_TABLA_PASO, in.p_Ordenes, OrdenesPorAutorizar.class);
		
		//cambiar el IdEstadoIntero a AUT
		sql.batch(conn, SQL_UPDATE_ESTADO_ORDEN, new ArrayList<OrdenesPorAutorizar>(), OrdenesPorAutorizar.class);
		
		//obtener ordenes a procesar que tengan IdEstadoLimite = AUT.
		List<OrdenesAutorizadas> ordenesAutorizadas = sql.query(conn, SQL_SELECT_ORDEN_AUTORIZADAS, new HashMap<String, Object>(), OrdenesAutorizadas.class);
		
		//averiguar si las ordenes de la entrada cumplen con los requisitos para ser autorizadas.
		HashMap<String, Object> ordenesValidasAutorizar = Utils.List2Hash(ordenesAutorizadas, "IdOrden");
		in.p_Ordenes.forEach(ord -> {
			if(!ordenesValidasAutorizar.containsKey(ord.IdOrden)) {
				out.P_OUT_MENSAJES.add(new MensajeSalida((long)out.P_OUT_MENSAJES.size()+1, MensajeSalida.MENSAJE_ERROR, "UI_ESTADO_LIMITE_INCORRECTO", "UI_ESTADO_LIMITE_INCORRECTO"));
			}
		});
		
		if(!out.P_OUT_MENSAJES.isEmpty()) {
			//si hay errores, terminar el flujo.
			return out;
		}
		
		log.debug(ordenesValidasAutorizar.values());
		
		//Cambiar estado de las ordenes desde PDA a ING
		CambiarEstadoOrdenes cambiarEstadoOrdenes = new CambiarEstadoOrdenes();
		CambiarEstadoOrdenes_Entrada entradaCambiarEstadoOrdenes = new CambiarEstadoOrdenes_Entrada();
		entradaCambiarEstadoOrdenes.p_IdEmpresa = in.p_IdEmpresa;
		entradaCambiarEstadoOrdenes.p_IdUsuario = in.p_IdUsuario;
		entradaCambiarEstadoOrdenes.p_Ordenes = ordenesAutorizadas.stream().map(ord -> {
			cl.bcs.ordenes.CambiarEstadoOrdenes.Orden_Entrada mapOrd = new Orden_Entrada();
			mapOrd.IdOrden = ord.IdOrden;
			mapOrd.IdNuevoEstado = _c.ESTADO_ORDEN_INGRESADA;
			return mapOrd;
		}).collect(Collectors.toList());
		CambiarEstadoOrdenes_Salida salidaCambiarEstadoOrdenes = cambiarEstadoOrdenes.logica(conn, entradaCambiarEstadoOrdenes);
		salidaCambiarEstadoOrdenes.P_OUT_MENSAJES.forEach(msj -> {
			if(!MensajeSalida.MENSAJE_OK.equalsIgnoreCase(msj.tipo)) {
				out.P_OUT_MENSAJES.add(new MensajeSalida((long)out.P_OUT_MENSAJES.size()+1, msj.tipo, msj.codigo, msj.descripcion));
			}
		});
		if(!out.P_OUT_MENSAJES.isEmpty()) {
			//hay errores terminar.
			return out;
		}
		
		//notificar los cambios de estado correctos
		AgregarMensajeWebsocket agregarMensajeWebSocket = new AgregarMensajeWebsocket();
		EntradaServicio entradaAgregarMensajeWebSocket = new EntradaServicio();
		entradaAgregarMensajeWebSocket.p_IdEmpresa = in.p_IdEmpresa;
		entradaAgregarMensajeWebSocket.p_IdUsuario = in.p_IdUsuario;
		entradaAgregarMensajeWebSocket.p_IdModulo = in.p_IdModulo;
		entradaAgregarMensajeWebSocket.p_Mensajes = new ArrayList<>();
		
		salidaCambiarEstadoOrdenes.P_OUT_MENSAJES.forEach(msj -> {
			OrdenesAutorizadas ord = (OrdenesAutorizadas) ordenesValidasAutorizar.get(msj.id);
			//lanzar notificacion en la pantalla
			MensajeWebSocket msw = new MensajeWebSocket();
			msw.IdEmpresa = in.p_IdEmpresa;
			msw.Evento = "AVISO";
			msw.DATA = String.format("INGRESO ORDEN\r\nNemotecnico: %s\r\nId Orden: %d\r\n", ord.Nemotecnico, ord.IdOrden);
			entradaAgregarMensajeWebSocket.p_Mensajes.add(msw);
			//inserta tag para refrescar la grilla de negociacion de ordenes
			msw = new MensajeWebSocket();
			msw.IdEmpresa = in.p_IdEmpresa;
			msw.Evento = "CREAR_ORDEN";
			msw.DATA = "";
			entradaAgregarMensajeWebSocket.p_Mensajes.add(msw);
		});
		agregarMensajeWebSocket.logica(conn, entradaAgregarMensajeWebSocket);
		
		//actualizar fechaAceptacion y fechaAutorizacion
		Timestamp fechaHoraSistemaTs = FechaDB.ObtenerFechaHoraSistemaTs(conn, in.p_IdEmpresa);
		ordenesAutorizadas.forEach(ord ->{
			ord.FechaAceptacion = fechaHoraSistemaTs;
			ord.FechaAutorizacion = fechaHoraSistemaTs;
		});
		sql.batch(conn, SQL_UPDATE_FECHA_AUTORIZACION, ordenesAutorizadas, OrdenesAutorizadas.class);
		
		//obtener ordenes por ruteo
		RutearOrdenes rutearOrdenes = new RutearOrdenes();
		RutearOrdenes_Entrada entradaRutearOrdenes = new RutearOrdenes_Entrada();
		entradaRutearOrdenes.p_IdEmpresa = in.p_IdEmpresa;
		entradaRutearOrdenes.p_Ordenes = ordenesAutorizadas.stream().map(ord -> {
			//preguntar si la orden tiene canal de distribucion distinto a MANUAL
			if(!_c.TIPO_CANAL_DISTRIBUCION_MANUAL.equalsIgnoreCase(ord.IdTipoCanalDistribucion)) {
				cl.bcs.ordenes.RutearOrdenes.Orden ordRuteo = new cl.bcs.ordenes.RutearOrdenes.Orden();
				ordRuteo.IdOrden = ord.IdOrden;
				ordRuteo.IdBolsaDistribucion = ord.IdBolsaDistribucion;
				return ordRuteo;
			}else {
				return null;	
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
		if(!entradaRutearOrdenes.p_Ordenes.isEmpty()) {
			//hay ordenes por rutear
			rutearOrdenes.logica(conn, entradaRutearOrdenes);
		}
		
		//agregar log cambio estado de ordenes.
		String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);
		AgregarLog agregarLog = new AgregarLog();
		InAgregarLog entradaAgregarLog = new InAgregarLog();
		AtomicLong contador = new AtomicLong();
		ordenesAutorizadas.forEach(ord ->{
			entradaAgregarLog.agregarLogBasico(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, ord.IdOrden, contador.get(), "INGRESO", "ORDEN", fechaSistema, "AUTORIZACION ORDENES");
			entradaAgregarLog.agregarDetalleLogBasico(ord.IdOrden, contador.getAndIncrement(), "IdEstadoOrden", _c.ESTADO_ORDEN_PENDIENTE_AUTORIZAR, _c.ESTADO_ORDEN_INGRESADA);
		});
		agregarLog.logica(conn, entradaAgregarLog);
		
		log.info("TERMINO::ConfirmarAutorizacionOrdenes");
		return out;		
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("master01")) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":164,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_Ordenes\":[{\"IdOrden\":35512}]}";
								
				EntradaConfirmarAutorizacionOrdenes Objin = gson.fromJson(json, EntradaConfirmarAutorizacionOrdenes.class);				
				ConfirmarAutorizacionOrdenes demo=new ConfirmarAutorizacionOrdenes();				
	 			System.out.println(Objin.toString());
	 			SalidaConfirmarAutorizacionOrdenes ObjOut=demo.logica(con, Objin);	 			
		    	System.out.println(ObjOut.toString());	
		    	con.rollback();
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	}	
}
