package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.NotificarEstadoOrden.ObjDirs;

import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarImagen;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenEntrada;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class NotificacionOrdenAsignada implements JavaCall{
	private static final Logger logger = LogManager.getLogger(NotificacionOrdenAsignada.class);
	
	private final String SQL_SELECT_COBROS = loadResourceToString("cl/bcs/ordenes/sql/NotificarEstadoOrden_SelectCobros.sql");
	private final String SQL_SELECT_COBROS_PERU = loadResourceToString("cl/bcs/ordenes/sql/NotificarEstadoOrden_SelectCobrosPeru.sql");
	private final String SQL_SELECT_INFO = loadResourceToString("cl/bcs/ordenes/sql/NotificarEstadoOrden_SelectInfoOrden.sql");
	private final String SQL_SELECT_DIRS = loadResourceToString("cl/bcs/ordenes/sql/NotificarEstadoOrden_SelectDirecciones.sql");
	
	
	/************************************************************************************
	 * 								CLASES
	 *************************************************************************************/
	public static class NotificarOrdenAsignada_Entrada {
		public Long			p_IdEmpresa;
		public Long			p_IdUsuario;
		public String 		p_IdModulo;
		public String       p_IdEstadoAsignacion;
		public List<Orden>	p_Ordenes;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Orden {
		public Long IdOrden;
		public Orden() {};
		public Orden(Long idOrden) {
			this.IdOrden = idOrden;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjCobro {
		public Long 	  IdOrden;
		public BigDecimal COMISION;
		public BigDecimal IMPUESTO;
		public BigDecimal GASTO;
		public BigDecimal DERECHO;		
	}
	
	public static class ObjInfo {
		//basicos
		public Long 	  IdOrden;		
		public Long 	  IdCliente;
		public Long 	  IDCLIENTEPERSONA;
		public Long 	  IdPortafolioCliente;
		public Long 	  IdEmpleadoDigitador;
		public Long 	  IDEMPLEADOPERSONA;
		public Long		  IDCORREOEMPLEADO;
		
		public Long 	  FOLIO;
		public String 	  NEMO;
		public String 	  RUT;
		public String 	  RUTCAVALI;
		public String 	  CLIENTE;	
		public String 	  CUENTA;
		public String 	  AGENTE;		
		public String 	  CORREO;
		public String 	  TELEFONO;
		public String 	  FECHA;
		public String 	  HORA;
		public String 	  PRODUCTO;
		public String 	  TIPO_OPERACION;
		public String 	  CANTIDAD;
		public BigDecimal PRECIO;
		public String 	  MONTO;
		public String 	  MONEDA;	
		public String 	  ESTADO;		
		public String 	  CORREDORA;	
		public String 	  ATENCIONCLIENTE;	
		public String 	  CONDICIONLIQUIDACION;	
		public String 	  TIPOPRECIOORDEN;	
		public String 	  FECHAVIGENCIA;	
		public BigDecimal TASA;	
		public String 	  PLAZOVENCIMIENTO;			
		public String 	  MONTOPRIMA;	
		public String 	  VENTACORTA;
		public String	  IDTIPOINSTRUMENTO;
		public String	  CodigoPortafolio;
	}
	
	public static class NotificarOrdenAsignada_Salida {
		public List<MensajeSalida> P_OUT_ERRORESDETALLES;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public NotificarOrdenAsignada_Salida (){
			this.P_OUT_ERRORESDETALLES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR =0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	

	/************************************************************************************
	 * 								CALL
	 *************************************************************************************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();				
		NotificarOrdenAsignada_Entrada in=gson.fromJson(jin.toString() ,NotificarOrdenAsignada_Entrada.class);
		NotificarOrdenAsignada_Salida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	
	
	/************************************************************************************
	 * 								LOGICA
	 *************************************************************************************/
	
	@Trace
	public NotificarOrdenAsignada_Salida logica(Connection con, NotificarOrdenAsignada_Entrada in) throws Exception {
		Long init = System.currentTimeMillis();
		SQL sql = new SQL();
		NotificarOrdenAsignada_Salida salida = new NotificarOrdenAsignada_Salida();
		logger.info("Inicio servicio NotificacionOrdenAsignada");
		
		String tFechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(con, in.p_IdEmpresa);
		Timestamp vFechaHoraSistema = Fecha.toTimeStamp(tFechaHoraSistema);  
		int SegundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, in.p_IdEmpresa);
		
        String rutaLogoCorredora = "";
        ConsultarImagenEntrada imagenCorredoraIn = new ConsultarImagenEntrada();
        imagenCorredoraIn.p_IdEmpresa = in.p_IdEmpresa;
        imagenCorredoraIn.p_imagenNombres.add("LOGO_CORREDOR");
        ConsultarImagenSalida imagenCorredoraOut = new ConsultarImagen().logica(con, imagenCorredoraIn);
        if(imagenCorredoraOut.OUT_IMAGENES.size() > 0) {
        	rutaLogoCorredora = imagenCorredoraOut.OUT_IMAGENES.get(0).RutaPublica;        	
        }
		
		List<Long> ordenes = Utils.getColumn(in.p_Ordenes, "IdOrden", false);
		if (ordenes.size()==0) {
			return salida;
		}
		
		//Obtiene informacion de las ordenes
		HashMap<String, Object> parmsInfo = new HashMap<String, Object>();
		parmsInfo.put("IdEmpresa", in.p_IdEmpresa);	
		parmsInfo.put("SegundosZonaHoraria", SegundosZonaHoraria);
		parmsInfo.put("ListaIdOrden", ordenes);
		List<ObjInfo> listaOrd= sql.query(con, SQL_SELECT_INFO, parmsInfo, ObjInfo.class);
		
		if(listaOrd.size()>0) {
			String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
			//Obtener los cobros de las ordenes
			HashMap<String, Object> parmsCobros = new HashMap<String, Object>();
			parmsCobros.put("ListaIdOrden", ordenes);
			List<ObjCobro> listaCobros;
			if (localizacionSistema.equals("CL")) {
				listaCobros= sql.query(con, SQL_SELECT_COBROS, parmsCobros, ObjCobro.class);	
			}
			else {
				listaCobros= sql.query(con, SQL_SELECT_COBROS_PERU, parmsCobros, ObjCobro.class);
			}
			
			//Obtener info direccion de notificaciones
			HashMap<String, Object> parmsDirs= new HashMap<String, Object>();
			List<Long> portfolios = Utils.getColumn(listaOrd, "IdPortafolioCliente", false);
			parmsDirs.put("ListaIdPortafolio1", portfolios);
			List<ObjDirs> listaDirs  =sql.query(con, SQL_SELECT_DIRS, parmsDirs, ObjDirs.class);
			
			//Agrega notificacion
	        AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada();
	        notifIn.P_IDEMPRESA = in.p_IdEmpresa;
	        notifIn.P_IDUSUARIO = in.p_IdUsuario;
	        notifIn.P_IDMODULO = in.p_IdModulo;
	        notifIn.P_EVENTOS = new ArrayList<>();
	        notifIn.P_VALORES = new ArrayList<>();
	        notifIn.P_ARCHIVOSADJUNTOS = new ArrayList<>();
	        notifIn.P_IDDIRECCIONES = new ArrayList<>(); 
	        
	        for (ObjInfo ord:listaOrd) {
	        	if (ord.IDCORREOEMPLEADO != null && ord.IDCORREOEMPLEADO > 0) {

		        	// Evento de notificacion
		            AgregarEvento.ObjEVENTOS evt = new AgregarEvento.ObjEVENTOS();
		            evt.CodigoTipoEvento = "CAMBIO_ESTADO_ORDEN";
		            evt.IdSecuencia = ord.IdOrden;
		            evt.FechaCreacionSistema =  Fecha.toStringDate(vFechaHoraSistema);            
		            evt.IdPersonaRemitente = ord.IDEMPLEADOPERSONA;
		            evt.IdPersonaDestinatario = ord.IDCLIENTEPERSONA;            
		            evt.IdPortafolio = ord.IdPortafolioCliente;
		            notifIn.P_EVENTOS.add(evt);
		            // Correo a empleados
		            for (ObjDirs dir:listaDirs) { 
		            	if (!dir.IdPortafolio.equals(ord.IdPortafolioCliente)) continue;
		            	if (dir.IdDireccion==null || dir.IdDireccion==0) continue;
			            AgregarEvento.ObjIDDIRECCIONES ndir = new AgregarEvento.ObjIDDIRECCIONES();
			            ndir.IdSecuencia = ord.IdOrden;
			            ndir.TipoDestinatario = dir.TipoDestinatario;
			            ndir.IdDireccion = new Long(dir.IdDireccion);
			            notifIn.P_IDDIRECCIONES.add(ndir);
			            //Agregar notificacion al empleado
			            ndir = new AgregarEvento.ObjIDDIRECCIONES();
			            ndir.IdSecuencia = ord.IdOrden;
			            ndir.TipoDestinatario = "CC";
			            ndir.IdDireccion = new Long(ord.IDCORREOEMPLEADO);
			            notifIn.P_IDDIRECCIONES.add(ndir);
		            }
		            // Valores generales
		            notifIn.addValor(ord.IdOrden, "FOLIO", ord.FOLIO+"")
		            	   .addValor(ord.IdOrden, "IDCLIENTE", ord.IdCliente+"")
		                   .addValor(ord.IdOrden, "IDCLIENTEPERSONA", ord.IDCLIENTEPERSONA+"")
		                   .addValor(ord.IdOrden, "NEMO", ord.NEMO)   
		                   .addValor(ord.IdOrden, "DOCUMENTOIDENTIDAD", ord.RUT)    
		                   .addValor(ord.IdOrden, "CLIENTE", ord.CLIENTE)  
		                   .addValor(ord.IdOrden, "IDPORTAFOLIOCLIENTE", ord.IdPortafolioCliente+"") 
		                   .addValor(ord.IdOrden, "CUENTA", ord.CUENTA)
		                   .addValor(ord.IdOrden, "IDEMPLEADODIGITADOR", ord.IdEmpleadoDigitador+"")
		                   .addValor(ord.IdOrden, "AGENTE", ord.AGENTE)
		                   .addValor(ord.IdOrden, "IDEMPLEADOPERSONA", ord.IDEMPLEADOPERSONA+"")
		                   .addValor(ord.IdOrden, "CORREO", ord.CORREO)
		                   .addValor(ord.IdOrden, "TELEFONO", ord.TELEFONO)
		                   .addValor(ord.IdOrden, "FECHA", ord.FECHA)
		                   .addValor(ord.IdOrden, "HORA", ord.HORA)
		                   .addValor(ord.IdOrden, "PRODUCTO", ord.PRODUCTO)
		                   
		   
		                   
	
		                   .addValor(ord.IdOrden, "OPERACION", ord.TIPO_OPERACION)
		                   .addValor(ord.IdOrden, "TIPO_OPERACION", ord.TIPO_OPERACION)
		                   
		                   .addValor(ord.IdOrden, "CANTIDAD", ord.CANTIDAD)
		                   .addValor(ord.IdOrden, "PRECIO", MonedaFormato.formatear(con, ord.PRECIO, MonedaFormato.PRECIO, ord.MONEDA, ord.IDTIPOINSTRUMENTO, "").valorConFormato)
		                   .addValor(ord.IdOrden, "MONEDA", ord.MONEDA)
		                   .addValor(ord.IdOrden, "ESTADO", in.p_IdEstadoAsignacion)
		                   .addValor(ord.IdOrden, "CORREDORA", ord.CORREDORA)
		                   .addValor(ord.IdOrden, "ATENCIONCLIENTE", ord.ATENCIONCLIENTE)
		                   .addValor(ord.IdOrden, "MONTO", ord.MONTO)
		                   .addValor(ord.IdOrden, "CONDICIONLIQUIDACION", ord.CONDICIONLIQUIDACION)
		                   .addValor(ord.IdOrden, "TIPOPRECIOORDEN", ord.TIPOPRECIOORDEN)
		                   .addValor(ord.IdOrden, "FECHAVIGENCIA", ord.FECHAVIGENCIA)
		                   .addValor(ord.IdOrden, "TASA", MonedaFormato.formatear(con, ord.TASA, MonedaFormato.TASA, ord.MONEDA, ord.IDTIPOINSTRUMENTO, "").valorConFormato)
		                   .addValor(ord.IdOrden, "PLAZOVENCIMIENTO", ord.PLAZOVENCIMIENTO+"")
		                   .addValor(ord.IdOrden, "MONTOPRIMA", ord.MONTOPRIMA)
		                   .addValor(ord.IdOrden, "VENTACORTA", ord.VENTACORTA)
		                   .addValor(ord.IdOrden, "LOGO_PUBLICO", rutaLogoCorredora);
		           
		            if(localizacionSistema.equals("PE")) {
		            	notifIn.addValor(ord.IdOrden, "RUTCAVALI", ord.RUTCAVALI);
		            }
		            
		         // Valores cobro para el mensaje
		            for (ObjCobro cob:listaCobros) { 
		            	if (!cob.IdOrden.equals(ord.IdOrden)) continue;
		            	
		            	notifIn.addValor(ord.IdOrden, "COMISION", MonedaFormato.formatear(con, cob.COMISION, MonedaFormato.MONTO, ord.MONEDA).valorConFormato+" "+ ord.MONEDA)
		            	       .addValor(ord.IdOrden, "DERECHO", MonedaFormato.formatear(con, cob.DERECHO, MonedaFormato.MONTO, ord.MONEDA).valorConFormato+" " + ord.MONEDA)
		            	       .addValor(ord.IdOrden, "GASTO", MonedaFormato.formatear(con, cob.GASTO, MonedaFormato.MONTO, ord.MONEDA).valorConFormato+" " + ord.MONEDA)
		            	       .addValor(ord.IdOrden, "IMPUESTO", MonedaFormato.formatear(con, cob.IMPUESTO, MonedaFormato.MONTO, ord.MONEDA).valorConFormato+" " + ord.MONEDA);          	            	
		            }
	        	} 
	        	else {
	        		String codigo = "EJECUTIVO_PORTAFOLIO_SIN_CORREO";
					String desc = Diccionario.getMensaje(con, in.p_IdEmpresa,codigo).add("CodigoPortafolio", ord.CodigoPortafolio).add("DocumentoIdentidad", ord.RUT).msg();
					salida.P_OUT_MENSAJES.add(new MensajeSalida(ord.IdOrden,MensajeSalida.MENSAJE_ADVERTENCIA,codigo,desc));	    			
	        	}
	        }
	        if (notifIn.P_EVENTOS.size() > 0) {
	            new AgregarEvento().logica(con, notifIn);
	        }
	        
		}else{
			logger.error("Error al notificar ");
			salida.P_OUT_ERRORESCONTADOR=1L;
			salida.P_OUT_ERRORESDETALLES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERR0","No Existen ordenes para notificar"));
		}
		
		logger.info("Termino del servicio NotificacionOrdenAsignada, tiempo:" + (System.currentTimeMillis() - init));
		return salida;
	}
	
	/************************************************************************************
	 * 								MAIN
	 *************************************************************************************/
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"ORD\",\"p_IdEstadoAsignacion\":\"ORDEN_ASIGNADA\",p_Ordenes:[{\"IdOrden\":52830}]}";
					
			NotificarOrdenAsignada_Entrada Objin = gson.fromJson(json, NotificarOrdenAsignada_Entrada.class);
			
			NotificacionOrdenAsignada demo=new NotificacionOrdenAsignada();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				NotificarOrdenAsignada_Salida ObjOut=demo.logica(con, Objin);
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
	    }
		System.out.println("== END ==");
	}
}
