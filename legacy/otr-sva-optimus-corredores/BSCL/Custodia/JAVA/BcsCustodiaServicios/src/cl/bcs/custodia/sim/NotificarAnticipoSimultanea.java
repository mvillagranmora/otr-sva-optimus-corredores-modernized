package cl.bcs.custodia.sim;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.pla.utils.ConsultarImagen;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenEntrada;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class NotificarAnticipoSimultanea implements JavaCall{
	
	private final Log log = new Log("CUS.NotificarAnticipoSimultanea");
	private final String SQL_INFO_NOTIFICACION = loadResourceToString("cl/bcs/custodia/sql/NotificarAnticipoSimultanea_SelectInfoNotificacion.sql");
	
	public static class EntradaNotificarAnticipoSimultanea{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<Cartera> p_IdCarteras;
		
		public EntradaNotificarAnticipoSimultanea(){
			this.p_IdCarteras = new ArrayList<Cartera>();
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cartera{
		public Long IdCartera;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaNotificarAnticipoSimultanea{
		public Long P_OUT_ERRORESCONTADOR;
		
		public SalidaNotificarAnticipoSimultanea(){
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InfoNotificaciones {
		//basicos
		public Long 	  IdCartera;
		public Long 	  IdCliente;
		public Long 	  IDCLIENTEPERSONA;
		public Long 	  IdPortafolioCliente;
		public Long 	  IdEmpleadoDigitador;
		public Long 	  IDEMPLEADOPERSONA;
		public Long		  IDCORREOEMPLEADO;
		public Long	      IDCORREOCLIENTE;
		//tags notificaciones
		public Long 	  FOLIOOPERACION;
		public String 	  AGENTE;
		public String 	  FECHA;
		public String 	  HORA;
		public String     DOCUMENTOIDENTIDAD;
		public String	  CLIENTE;	
		public String 	  CORREDORA;
		public String 	  ATENCIONCLIENTE;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}

	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();				
		EntradaNotificarAnticipoSimultanea in=gson.fromJson(jin.toString() ,EntradaNotificarAnticipoSimultanea.class);
		SalidaNotificarAnticipoSimultanea out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaNotificarAnticipoSimultanea logica(Connection con, EntradaNotificarAnticipoSimultanea in) throws Exception{
		log.write("INICIO::NotificarAnticipoSimultanea");
		SalidaNotificarAnticipoSimultanea out = new SalidaNotificarAnticipoSimultanea();
		
		SQL sql = new SQL();
		
		String tFechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(con, in.p_IdEmpresa);
	    Timestamp vFechaHoraSistema = Fecha.toTimeStamp(tFechaHoraSistema);
	    
	    List<Long> idCarteras = new ArrayList<Long>();
	    
	    for(Cartera cartera : in.p_IdCarteras){
	    	idCarteras.add(cartera.IdCartera);
	    }
	    
        String rutaLogoCorredora = "";
        ConsultarImagenEntrada imagenCorredoraIn = new ConsultarImagenEntrada();
        imagenCorredoraIn.p_IdEmpresa = in.p_IdEmpresa;
        imagenCorredoraIn.p_imagenNombres.add("LOGO_CORREDOR");
        ConsultarImagenSalida imagenCorredoraOut = new ConsultarImagen().logica(con, imagenCorredoraIn);
        if(imagenCorredoraOut.OUT_IMAGENES.size() > 0) {
        	rutaLogoCorredora = imagenCorredoraOut.OUT_IMAGENES.get(0).RutaPublica;        	
        }
	    
	    HashMap<String, Object> params = new HashMap<String, Object>();
	    params.put("IdEmpresa", in.p_IdEmpresa);
		params.put("SegundosZonaHoraria", in.p_IdEmpresa);
		params.put("IdCartera", idCarteras);
		List<InfoNotificaciones> listaInfo = sql.query(con, SQL_INFO_NOTIFICACION, params, InfoNotificaciones.class);
		AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada();
		notifIn.P_IDEMPRESA = in.p_IdEmpresa;
        notifIn.P_IDUSUARIO = in.p_IdUsuario;
        notifIn.P_IDMODULO = in.p_IdModulo;
        notifIn.P_EVENTOS = new ArrayList<>();
        notifIn.P_VALORES = new ArrayList<>();
        notifIn.P_ARCHIVOSADJUNTOS = new ArrayList<>();
        notifIn.P_IDDIRECCIONES = new ArrayList<>();
        for (InfoNotificaciones ord:listaInfo) {
        	// Evento de notificacion
            AgregarEvento.ObjEVENTOS evt = new AgregarEvento.ObjEVENTOS();
            evt.CodigoTipoEvento = "ANTICIPO_SIMULTANEA";
            evt.IdSecuencia = ord.IdCartera;
            evt.FechaCreacionSistema =  Fecha.toStringDate(vFechaHoraSistema);            
            evt.IdPersonaRemitente = ord.IDEMPLEADOPERSONA;
            evt.IdPersonaDestinatario = ord.IDCLIENTEPERSONA;            
            evt.IdPortafolio = ord.IdPortafolioCliente;
            notifIn.P_EVENTOS.add(evt);
            
            AgregarEvento.ObjIDDIRECCIONES ndir = new AgregarEvento.ObjIDDIRECCIONES();
            ndir.IdSecuencia = ord.IdCartera;
            ndir.TipoDestinatario = "TO";
            ndir.IdDireccion = new Long(ord.IDCORREOCLIENTE);
            notifIn.P_IDDIRECCIONES.add(ndir);
            
            // Valores generales
            notifIn.addValor(ord.IdCartera, "FOLIO", ord.FOLIOOPERACION+"")
            	   .addValor(ord.IdCartera, "IdCliente", ord.IdCliente+"")
                   .addValor(ord.IdCartera, "IDCLIENTEPERSONA", ord.IDCLIENTEPERSONA+"")  
                   .addValor(ord.IdCartera, "CLIENTE", ord.CLIENTE)  
                   .addValor(ord.IdCartera, "IdPortafolioCliente", ord.IdPortafolioCliente+"") 
                   .addValor(ord.IdCartera, "IdEmpleadoDigitador", ord.IdEmpleadoDigitador+"")
                   .addValor(ord.IdCartera, "AGENTE", ord.AGENTE)
                   .addValor(ord.IdCartera, "IDEMPLEADOPERSONA", ord.IDEMPLEADOPERSONA+"")
                   .addValor(ord.IdCartera, "FECHA", ord.FECHA)
                   .addValor(ord.IdCartera, "HORA", ord.HORA)
                   .addValor(ord.IdCartera, "DOCUMENTOIDENTIDAD", ord.DOCUMENTOIDENTIDAD)
                   .addValor(ord.IdCartera, "CORREDORA", ord.CORREDORA)
                   .addValor(ord.IdCartera, "ATENCIONCLIENTE", ord.ATENCIONCLIENTE)
                   .addValor(ord.IdCartera, "LOGO_CORREDORA", rutaLogoCorredora);
            
        }
        
        if (notifIn.P_EVENTOS.size() > 0) {
            AgregarEvento notif = new AgregarEvento();
            AgregarEvento.ServiceSalida notifOut = notif.logica(con, notifIn);
            log.write("ID EVENTO::",notifOut.P_OUT_IDEVENTO);
        }
        
		log.write("FIN::NotificarAnticipoSimultanea");
		
		return out;
	}
	
	public static void main(String[] args){
		
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			NotificarAnticipoSimultanea sp = new NotificarAnticipoSimultanea();
			EntradaNotificarAnticipoSimultanea in = new EntradaNotificarAnticipoSimultanea();
			in.p_IdEmpresa = 1L;
			in.p_IdModulo = "CUS";
			in.p_IdUsuario = 21L;
			Cartera orden = new Cartera();
			orden.IdCartera = 1522662L;
			in.p_IdCarteras.add(orden);
			sp.logica(con, in);
			con.commit();
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	}

}
