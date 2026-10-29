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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.utils.ConsultarImagen;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenEntrada;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class NotificarEstadoOrden implements JavaCall {

	private static final Logger logger = LogManager.getLogger(NotificarEstadoOrden.class);

	private final String SQL_SELECT_COBROS = loadResourceToString("cl/bcs/ordenes/sql/NotificarEstadoOrden_SelectCobros.sql");
	private final String SQL_SELECT_COBROS_PERU = loadResourceToString("cl/bcs/ordenes/sql/NotificarEstadoOrden_SelectCobrosPeru.sql");
	private final String SQL_SELECT_INFO = loadResourceToString("cl/bcs/ordenes/sql/NotificarEstadoOrden_SelectInfoOrden.sql");
	private final String SQL_SELECT_DIRS = loadResourceToString("cl/bcs/ordenes/sql/NotificarEstadoOrden_SelectDirecciones.sql");

	public static class NotificarEstadoOrden_Entrada {
		public Long			p_IdEmpresa;
		public Long			p_IdUsuario;
		public String 		p_IdModulo;
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
		//tags notificaciones
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
		public String 	  DocumentoIdentidadEmpleado;
	}	
	
	public static class ObjDirs {
		public Long 	  IdPortafolio;
		public Integer 	IdDireccion;
		public String 	  TipoDestinatario;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	public static class NotificarEstadoOrden_Salida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public NotificarEstadoOrden_Salida()
		{
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
	}
	
	@Trace
	public void logicaAsync(Connection con,NotificarEstadoOrden_Entrada in) throws Exception {
		Gson gson = getGsonBuilder();	
		JsonParser parser 	= new JsonParser();
        JsonObject dataObj	= parser.parse(gson.toJson(in)).getAsJsonObject();		
        String CallBatchURL=ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.CallBatchURL");
        String CallBatchToken=ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.CallBatchToken");
		Utils.CallBatchAsync(CallBatchURL, CallBatchToken, "PLUGIN_ORD.jar@cl.bcs.ordenes.NotificarEstadoOrden", dataObj);
	}
	@Trace
	public NotificarEstadoOrden_Salida logica(Connection con, NotificarEstadoOrden_Entrada in) throws Exception {
		Long init = System.currentTimeMillis();
		SQL sql = new SQL();
		NotificarEstadoOrden_Salida out = new NotificarEstadoOrden_Salida();
		logger.info("Inicio servicio NotificarEstadoOrden");
		
        int SegundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, in.p_IdEmpresa);
        Fecha.setLocalOffSet(SegundosZonaHoraria);
        String tFechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(con, in.p_IdEmpresa);
        Timestamp vFechaHoraSistema = Fecha.toTimeStamp(tFechaHoraSistema);        
        out.P_OUT_ERRORESCONTADOR=0L;
        
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
			return out;
		}
		

		//Obtener la info de las ordenes
		//TODO: ********************* REVISAR QUERY CON CLAUDIO TIENE PROBLEMAS DUPLICIDAD X DICCIONES ********************
		HashMap<String, Object> parmsInfo = new HashMap<String, Object>();
		parmsInfo.put("IdEmpresa", in.p_IdEmpresa);	
		parmsInfo.put("SegundosZonaHoraria", SegundosZonaHoraria);
		parmsInfo.put("ListaIdOrden", ordenes);
		List<ObjInfo> listaInfo= sql.query(con, SQL_SELECT_INFO, parmsInfo, ObjInfo.class);
		//logger.debug("Notificar ordenes: " + listaInfo.size());
		
		if(listaInfo.size()>0) {
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
			List<Long> portfolios = Utils.getColumn(listaInfo, "IdPortafolioCliente", false);
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
	        for (ObjInfo ord:listaInfo) {        	        	
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
	            	if (ord.IDCORREOEMPLEADO==null || ord.IDCORREOEMPLEADO==0)
	            	{	
	            		 HashMap<String, String> valores = new HashMap<String, String>();
	            		 valores.put("Nombre", ord.AGENTE);
	            		 valores.put("DocumentoIdentidad", ord.DocumentoIdentidadEmpleado);
	            		 out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_EMPREADO_SIN_CORREO_ELECTRONICO", valores, con, in.p_IdEmpresa));
	            		 continue;
	            	}
	            	else
	            	{
			            //Agregar notificacion al empleado
			            ndir = new AgregarEvento.ObjIDDIRECCIONES();
			            ndir.IdSecuencia = ord.IdOrden;
			            ndir.TipoDestinatario = "CC";
			            ndir.IdDireccion = new Long(ord.IDCORREOEMPLEADO);
			            notifIn.P_IDDIRECCIONES.add(ndir);
	            	}
	            	
	            	
		            
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
	                   .addValor(ord.IdOrden, "ESTADO", ord.ESTADO)
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
	        
	        if (notifIn.P_EVENTOS.size() > 0) {
	            new AgregarEvento().logica(con, notifIn);
	        }
		}
		logger.info("Termino del servicio NotificarEstadoOrden, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();				
		NotificarEstadoOrden_Entrada in=gson.fromJson(jin.toString() ,NotificarEstadoOrden_Entrada.class);
		NotificarEstadoOrden_Salida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	

	public static void main(String[] args){
		
		System.out.println("== TEST ==");
		Long idEmpresa = 1L;
		try (Connection con = PlataformaUtils.getDESAPEConnection()) {
			NotificarEstadoOrden sp = new NotificarEstadoOrden();
			NotificarEstadoOrden_Entrada in = new NotificarEstadoOrden_Entrada();
			in.p_IdEmpresa = idEmpresa;
			in.p_IdModulo = "ORD";
			in.p_IdUsuario = Long.parseLong("21");
			in.p_Ordenes = new ArrayList<>();
			Orden orden = new Orden();
			orden.IdOrden = Long.parseLong("24031");//rf
			in.p_Ordenes.add(orden);
			//{"p_IdEmpresa":1,"p_IdUsuario":21,"p_IdModulo":"ORD","p_Ordenes":[{"IdOrden":24857}]}
			sp.logica(con, in);
			con.commit();
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	}

}