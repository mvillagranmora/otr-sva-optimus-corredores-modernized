package cl.bcs.custodia;

import java.sql.Connection;
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

public class NotificarVencimientoInstrumentos implements JavaCall {
	
	private final String SQL_EMAIL_BASICO = loadResourceToString("cl/bcs/custodia/sql/NotificarVencimientoInstrumentos_Select.sql");	
	
	private final Log log = new Log("NotificarVencimiento");

	public static class EntradaNotificarVencimientoInstrumentos{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<Long> vencimientos;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class EmailBasico{
		public Long IdSecuencia;
		public String Nombre;
		public String TipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public Long IdPortafolio;
		public String Telefono;
		public String Correo;
		public String Agente;
		public Long FolioCartera;
		public String Nemotecnico;
		public String NombreTipoOperacion;
		public String FechaVencimiento;
		public Long IdDireccionElectronicaPrincipal;
		public Long IdPersona;
		public String Corredora;
		public String CallCenter;
		public Long IdPersonaEmpleado;
		public Long IdDireccionElectronicaEmpleado;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class SalidaNotificarVencimientoInstrumentos{
		public boolean estadoNotificacion;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	
	public SalidaNotificarVencimientoInstrumentos logica (Connection conn, EntradaNotificarVencimientoInstrumentos in) {
		SQL sql = new SQL();
		System.out.println("INICIO NOTIFICACION VENCIMIENTO");
		SalidaNotificarVencimientoInstrumentos salida = new SalidaNotificarVencimientoInstrumentos();
		
		try {			
			String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);
	        String horaSistema = FechaDB.ObtenerHoraSistemaLocal(conn, in.p_IdEmpresa);
	        	        
	        HashMap<String, Object> params = new HashMap<>();
			params.put("IdCarteras", in.vencimientos);
			List<EmailBasico> correosBasicos = sql.query(conn, SQL_EMAIL_BASICO, params, EmailBasico.class);
			HashMap<String, List<EmailBasico>> correosAgrupados = new HashMap<>();
			
			
			//agrupar clientes
			correosBasicos.forEach( correo -> {
				String documentoIdentidad = correo.DocumentoIdentidad;
				if(correosAgrupados.get(documentoIdentidad)==null) {
					correosAgrupados.put(documentoIdentidad, new ArrayList<EmailBasico>() );
				}
				correosAgrupados.get(documentoIdentidad).add(correo);
			});
			
			final String[] rutaLogoCorredora = new String[1];

			rutaLogoCorredora[0] = "";
	        ConsultarImagenEntrada imagenCorredoraIn = new ConsultarImagenEntrada();
	        imagenCorredoraIn.p_IdEmpresa = in.p_IdEmpresa;
	        imagenCorredoraIn.p_imagenNombres.add("LOGO_CORREDOR");
	        ConsultarImagenSalida imagenCorredoraOut = new ConsultarImagen().logica(conn, imagenCorredoraIn);
	        if(imagenCorredoraOut.OUT_IMAGENES.size() > 0) {
	        	rutaLogoCorredora[0] = imagenCorredoraOut.OUT_IMAGENES.get(0).RutaPublica;        	
	        }
			
			AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada();
	        notifIn.P_IDEMPRESA = in.p_IdEmpresa;
	        notifIn.P_IDUSUARIO = in.p_IdUsuario;
	        notifIn.P_IDMODULO = in.p_IdModulo;
	        notifIn.P_EVENTOS = new ArrayList<>();
	        notifIn.P_VALORES = new ArrayList<>();
	        notifIn.P_ARCHIVOSADJUNTOS = new ArrayList<>();
	        notifIn.P_IDDIRECCIONES = new ArrayList<>();
	        
			//construir evento notificacion
			correosAgrupados.keySet().forEach( documentoIdentidad -> {
				
				EmailBasico email = correosAgrupados.get(documentoIdentidad).get(0);
				//Agregar Evento
				AgregarEvento.ObjEVENTOS evt = new AgregarEvento.ObjEVENTOS();
	            evt.CodigoTipoEvento = "VENCIMIENTO_PRODUCTOS";
	            evt.IdSecuencia = email.IdSecuencia;
	            evt.FechaCreacionSistema =  fechaSistema;            
	            evt.IdPersonaRemitente = email.IdPersonaEmpleado;
	            evt.IdPersonaDestinatario = email.IdPersona;            
	            evt.IdPortafolio = email.IdPortafolio;
	            notifIn.P_EVENTOS.add(evt);
	            //Agregar direccion cliente
	            AgregarEvento.ObjIDDIRECCIONES ndir = new AgregarEvento.ObjIDDIRECCIONES();
	            ndir.IdSecuencia = email.IdSecuencia;
	            ndir.TipoDestinatario = "TO";
	            ndir.IdDireccion = new Long(email.IdDireccionElectronicaPrincipal);
	            notifIn.P_IDDIRECCIONES.add(ndir);
	            //Agregar notificacion al empleado
	            ndir = new AgregarEvento.ObjIDDIRECCIONES();
	            ndir.IdSecuencia = email.IdSecuencia;
	            ndir.TipoDestinatario = "CC";
	            ndir.IdDireccion = new Long(email.IdDireccionElectronicaEmpleado);
	            notifIn.P_IDDIRECCIONES.add(ndir);
	            //Agregar Detalle evento
	            notifIn.addValor(email.IdSecuencia, "CLIENTE", email.Nombre)
	            	.addValor(email.IdSecuencia, "LOGO_CORREDORA", rutaLogoCorredora[0])
	            	.addValor(email.IdSecuencia, "AGENTE", email.Agente)
	            	.addValor(email.IdSecuencia, "TIPODOCUMENTOIDENTIDAD", email.TipoDocumentoIdentidad)
	            	.addValor(email.IdSecuencia, "DOCUMENTOIDENTIDAD", email.DocumentoIdentidad)
	            	.addValor(email.IdSecuencia, "PORTAFOLIO", email.CodigoPortafolio)
	            	.addValor(email.IdSecuencia, "TELEFONO", email.Telefono)
	            	.addValor(email.IdSecuencia, "CORREO", email.Correo)
	            	.addValor(email.IdSecuencia, "FECHA", Fecha.toFormat(Fecha.toDate(fechaSistema),"dd-MM-yyyy"))
	            	.addValor(email.IdSecuencia, "HORA", horaSistema)
	            	.addValor(email.IdSecuencia, "PROXIMIDADFECHA",  email.FechaVencimiento.equals(fechaSistema) ? "el día de hoy" : "los proximos días")
	            	.addValor(email.IdSecuencia, "CORREDORA", email.Corredora)
	            	.addValor(email.IdSecuencia, "ATENCIONCLIENTE", email.CallCenter);
				
	            StringBuilder tabla = new StringBuilder();
				correosAgrupados.get(documentoIdentidad).forEach( resumen -> {
					tabla.append("<tr>");
					tabla.append("<td style=\"text-align: center;vertical-align: middle;\">" + resumen.FolioCartera + "</td>");
					tabla.append("<td>" + resumen.NombreTipoOperacion + "</td>");
					tabla.append("<td>" + resumen.Nemotecnico + "</td>");
					tabla.append("<td>" + resumen.FechaVencimiento + "</td>");
					tabla.append("</tr>");
				});
				notifIn.addValor(email.IdSecuencia, "RESUMEN", tabla.toString());
			});
			
			System.out.println(notifIn);
			if (notifIn.P_EVENTOS.size() > 0) {
	            AgregarEvento notif = new AgregarEvento();
	            System.out.println(notif.logica(conn, notifIn));
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return salida;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		log.write("*** Inicio de la ejecucion ***");
		Gson gson = new Gson();
		EntradaNotificarVencimientoInstrumentos in = gson.fromJson(jin.toString(), EntradaNotificarVencimientoInstrumentos.class);
		SalidaNotificarVencimientoInstrumentos out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		log.write("*** Fin de la ejecucion ***");
		return dataout;
	}	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getMBConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"PLA\",\"vencimientos\":[1566,1593,1642,1643,2286,2329,2500,2502,3228,3545,4057,4084,4103,4335,1983567,1983569,1983571,1983573,1983575,1983577,1983579,1983581,1983583,1983585,1983587,1983588,1983627,1983610,1983611,1983612,1983613,1983614,1983615,1983616,1983617,1983618,1983619,1983620,1983621,1983626,1982719,1982733,1982734,1982777,1982780,1982858,1982859,1982860,1982864,1982873,1982887,1982888,1982931,1982934,1982944,1982945,1982946,1982950,1983535,1983536,1983537,1983538,1983539,1983540,1983541,1983542,1983543,1983544,1983545,1983546,1983547,1983548,1983549,1983550,1983551,1983552,1983553,1983554,1983624,1983625,1983638,1983639,1983642]}";
			EntradaNotificarVencimientoInstrumentos Objin = gson.fromJson(json, EntradaNotificarVencimientoInstrumentos.class);
			
			NotificarVencimientoInstrumentos demo=new NotificarVencimientoInstrumentos();				
 			try {
 				System.out.println(demo.logica(con, Objin));
 				con.rollback();
 			}
 			catch(Exception er) {
				con.rollback();
			}
    	}
		System.out.println("== END ==");
	}
	
	

}
