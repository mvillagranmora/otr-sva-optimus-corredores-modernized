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
import cl.bcs.plataforma.pla.utils.ConsultarImagen;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenEntrada;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class NotificarConfirmacionOrden implements JavaCall {

	private static final Logger logger = LogManager.getLogger(NotificarConfirmacionOrden.class);

	private final String SQL_SELECT_INFO = loadResourceToString("cl/bcs/ordenes/sql/NotificarConfirmacionOrden_SelectInfoOrden.sql");
	private final String SQL_SELECT_DIRS = loadResourceToString("cl/bcs/ordenes/sql/NotificarConfirmacionOrden_SelectDirecciones.sql");

	public static class NotificarConfirmacionOrden_Entrada {
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
		public Long IdOrden;
		public Long IdEmpleadoPersona;
		public Long IdClientePersona;
		public Long IdPortafolioCliente;
		public Long IdCorreoEmpleado;
		//tags notificaciones
		public String NemotecnicoOrden;
		public String DocumentoIdentidadCliente;
		public String NombreCliente;
		public String PortafolioCliente; 
		public String Agente;
		public String FechaOrden;
		public String FechaPlazoConfirmacionOrden;
		public String Producto;
		public String TipoOperacionOrden;
		public String CantidadOrden;
		public String Corredora;
		public String AtencionCliente;		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
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
	
	public static class NotificarConfirmacionOrden_Salida {
		 public Long P_OUT_ERRORESCONTADOR;
	}
	
	


	@Trace
	public void logicaAsync(Connection con,NotificarConfirmacionOrden_Entrada in) throws Exception {
		Gson gson = getGsonBuilder();	
		JsonParser parser 	= new JsonParser();
        JsonObject dataObj	= parser.parse(gson.toJson(in)).getAsJsonObject();		
        String CallBatchURL=ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.CallBatchURL");
        String CallBatchToken=ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.CallBatchToken");
		Utils.CallBatchAsync(CallBatchURL, CallBatchToken, "PLUGIN_ORD.jar@cl.bcs.ordenes.NotificarConfirmacionOrden", dataObj);
	}
	
	@Trace
	public NotificarConfirmacionOrden_Salida logica(Connection con, NotificarConfirmacionOrden_Entrada in) throws Exception {
		Long init = System.currentTimeMillis();
		SQL sql = new SQL();
		NotificarConfirmacionOrden_Salida out = new NotificarConfirmacionOrden_Salida();
		logger.info("Inicio servicio NotificarConfirmacionOrden");
		
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
		parmsInfo.put("ListaIdOrden", ordenes);
		List<ObjInfo> listaInfo= sql.query(con, SQL_SELECT_INFO, parmsInfo, ObjInfo.class);
		//logger.debug("Notificar ordenes: " + listaInfo.size());
		
		if(listaInfo.size()>0) {
			//Obtener info direccion de notificaciones
			HashMap<String, Object> parmsDirs= new HashMap<String, Object>();
			List<Long> portfolios = Utils.getColumn(listaInfo, "IdPortafolioCliente", false);
			parmsDirs.put("ListaIdPortafolio1", portfolios);
			parmsDirs.put("ListaIdPortafolio2", portfolios);
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
	            evt.CodigoTipoEvento = "SOLICITUD_CONFIRMACION_ORDEN";
	            evt.IdSecuencia = ord.IdOrden;
	            evt.FechaCreacionSistema =  Fecha.toStringDate(vFechaHoraSistema);            
	            evt.IdPersonaRemitente = ord.IdEmpleadoPersona;
	            evt.IdPersonaDestinatario = ord.IdClientePersona;            
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
	            }
	            
           
	            
	            
	            
	            // Valores generales
	            notifIn.addValor(ord.IdOrden, "PRODUCTO", ord.Producto)
	            		.addValor(ord.IdOrden, "NombreCliente", ord.NombreCliente)
	            		.addValor(ord.IdOrden, "Agente", ord.Agente)
						.addValor(ord.IdOrden, "DocumentoIdentidadCliente", ord.DocumentoIdentidadCliente)
						.addValor(ord.IdOrden, "PortafolioCliente", ord.PortafolioCliente)
						.addValor(ord.IdOrden, "FechaOrden", ord.FechaOrden)
						.addValor(ord.IdOrden, "TipoOperacionOrden", ord.TipoOperacionOrden)
						.addValor(ord.IdOrden, "CantidadOrden", ord.CantidadOrden)
						.addValor(ord.IdOrden, "NemotecnicoOrden", ord.NemotecnicoOrden)
						.addValor(ord.IdOrden, "FechaPlazoConfirmacionOrden", ord.FechaPlazoConfirmacionOrden)
						.addValor(ord.IdOrden, "Corredora", ord.Corredora)
						.addValor(ord.IdOrden, "AtencionCliente", ord.AtencionCliente)
						.addValor(ord.IdOrden, "LOGO_PUBLICO", rutaLogoCorredora);
	            
	        }
	        
	        
	        if (notifIn.P_EVENTOS.size() > 0) {
	            AgregarEvento notif = new AgregarEvento();
	            AgregarEvento.ServiceSalida notifOut = notif.logica(con, notifIn);
	        }
	        
		}
		logger.info("Termino del servicio NotificarConfirmacionOrden, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();				
		NotificarConfirmacionOrden_Entrada in=gson.fromJson(jin.toString() ,NotificarConfirmacionOrden_Entrada.class);
		NotificarConfirmacionOrden_Salida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	

	public static void main(String[] args){
		
		System.out.println("== TEST ==");
		Long idEmpresa = 1L;
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			NotificarConfirmacionOrden sp = new NotificarConfirmacionOrden();
			NotificarConfirmacionOrden_Entrada in = new NotificarConfirmacionOrden_Entrada();
			in.p_IdEmpresa = idEmpresa;
			in.p_IdModulo = "ORD";
			in.p_IdUsuario = Long.parseLong("9");
			in.p_Ordenes = new ArrayList<>();
			Orden orden = new Orden();
			orden.IdOrden = Long.parseLong("318259");
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