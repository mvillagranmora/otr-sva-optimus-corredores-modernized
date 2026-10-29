package cl.bcs.custodia.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class NotificarVencimientoSimultaneas extends BcsJob implements JavaCall{
	
	private final String SQL_EMAIL_BASICO = loadResourceToString("cl/bcs/custodia/jobs/sql/NotificarAvisoVencimientoSimultanea_Select.sql");	
	
	private final Log log = new Log("NotificarVencimiento");

	public static class EntradaNotificarVencimientoSimultaneas{
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
		//CABECERA
		public String Agente;
		//DETALLE
		public Long IdSecuencia;
		public Long IdCartera;
		public Long FolioCartera;
		public String Cliente;
		public String TipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String Nemotecnico;
		public BigDecimal Cantidad;
		public String NombreTipoOperacion;
		public String FechaVencimiento;
		public String IdEstado;
		public String NombreEstado;
		//PIE
		public String Corredora;
		public String AtencionCliente;
		//EXTRA
		public Long IdPersonaEmpleado;
		public Long IdDireccionElectronicaEmpleado;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class SalidaNotificarVencimientoInstrumentos{
		public boolean estadoNotificacion = false;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaNotificarVencimientoInstrumentos logica (Connection conn, EntradaNotificarVencimientoSimultaneas in) {
		SQL sql = new SQL();
		System.out.println("INICIO NOTIFICACION VENCIMIENTO");
		SalidaNotificarVencimientoInstrumentos salida = new SalidaNotificarVencimientoInstrumentos();
		
		
		try {			
			String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);
	        String codigoPais = Utils.getConfiguracion(conn, Utils.getSchema(), in.p_IdEmpresa, "PLA.LocalizacionSistema");
	        String producto = (Optional.ofNullable(codigoPais).orElse("CL").equalsIgnoreCase("CL")) ? "Simultaneas" : "Reportes";
	        //obtener proxima fecha de vencimiento;
	        String diasVencimientoSimultanea = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "CUSTODIA", "DiasVencimientoSimultanea");
	        Long diasAvisoVencimiento = Long.parseLong(Optional.ofNullable(diasVencimientoSimultanea).orElse("0"));

	        LocalDate ld = LocalDate.parse(fechaSistema, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	        LocalDate ldProxima = ld.plusDays(diasAvisoVencimiento);
	        String fechaVencimiento = ldProxima.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	        
	        HashMap<String, Object> params = new HashMap<>();
			params.put("fechaDesde", fechaSistema);
			params.put("fechaHasta", fechaVencimiento);
			params.put("idEmpresa", in.p_IdEmpresa);
			List<EmailBasico> correosBasicos = sql.query(conn, SQL_EMAIL_BASICO, params, EmailBasico.class);
			HashMap<Long, List<EmailBasico>> correosAgrupados = new HashMap<>();
			
			//agrupar clientes
			correosBasicos.forEach( correo -> {
				Long IdPersonaEmpleado = correo.IdPersonaEmpleado;
				if(correosAgrupados.get(IdPersonaEmpleado)==null) {
					correosAgrupados.put(IdPersonaEmpleado, new ArrayList<EmailBasico>() );
				}
				correosAgrupados.get(IdPersonaEmpleado).add(correo);
			});
			
			AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada();
	        notifIn.P_IDEMPRESA = in.p_IdEmpresa;
	        notifIn.P_IDUSUARIO = in.p_IdUsuario;
	        notifIn.P_IDMODULO = in.p_IdModulo;
	        notifIn.P_EVENTOS = new ArrayList<>();
	        notifIn.P_VALORES = new ArrayList<>();
	        notifIn.P_ARCHIVOSADJUNTOS = new ArrayList<>();
	        notifIn.P_IDDIRECCIONES = new ArrayList<>();
	        
			//construir evento notificacion
			correosAgrupados.keySet().forEach( IdPersonaEmpleado -> {
				
				EmailBasico email = correosAgrupados.get(IdPersonaEmpleado).get(0);
				//Agregar Evento
				AgregarEvento.ObjEVENTOS evt = new AgregarEvento.ObjEVENTOS();
	            evt.CodigoTipoEvento = (Optional.ofNullable(codigoPais).orElse("CL").equalsIgnoreCase("CL")) ? "AVISO_VENCIMIENTO_SIMULTANEA" : "AVISO_VENCIMIENTO_REPORTE";
	            
	            evt.IdSecuencia = email.IdSecuencia;
	            evt.FechaCreacionSistema =  fechaSistema;            
	            evt.IdPersonaRemitente = 0L;
	            evt.IdPersonaDestinatario = email.IdPersonaEmpleado;            
	            evt.IdPortafolio = email.IdPortafolio;
	            notifIn.P_EVENTOS.add(evt);
	            //Agregar direccion cliente
	            AgregarEvento.ObjIDDIRECCIONES ndir = new AgregarEvento.ObjIDDIRECCIONES();
	            ndir.IdSecuencia = email.IdSecuencia;
	            ndir.TipoDestinatario = "TO";
	            ndir.IdDireccion = email.IdDireccionElectronicaEmpleado;
	            notifIn.P_IDDIRECCIONES.add(ndir);
	            
	            notifIn
	            	//cabecera
	            	.addValor(email.IdSecuencia, "AGENTE", email.Agente)
	            	//detalle
	            	.addValor(email.IdSecuencia, "PRODUCTO", producto)
	            	.addValor(email.IdSecuencia, "FECHADESDE", ld.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
	            	.addValor(email.IdSecuencia, "FECHAHASTA", ldProxima.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
	            	//pie
	            	.addValor(email.IdSecuencia, "CORREDORA", email.Corredora)
	            	.addValor(email.IdSecuencia, "ATENCIONCLIENTE", email.AtencionCliente);
				
	            StringBuilder tabla = new StringBuilder();
				correosAgrupados.get(IdPersonaEmpleado).forEach( resumen -> {
					tabla.append("<tr>");
					tabla.append("<td>" + resumen.FolioCartera + "</td>");
					tabla.append("<td>" + resumen.NombreTipoOperacion + "</td>");
					tabla.append("<td>" + resumen.Nemotecnico + "</td>");
					tabla.append("<td>" + resumen.FechaVencimiento + "</td>");
					tabla.append("</tr>");
				});
				notifIn.addValor(email.IdSecuencia, "RESUMEN", tabla.toString());
			});
			
			if (!notifIn.P_EVENTOS.isEmpty()) {
	            AgregarEvento notif = new AgregarEvento();
	            System.out.println(notif.logica(conn, notifIn));
	            salida.estadoNotificacion = true;
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
		EntradaNotificarVencimientoSimultaneas in = gson.fromJson(jin.toString(), EntradaNotificarVencimientoSimultaneas.class);
		SalidaNotificarVencimientoInstrumentos out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		log.write("*** Fin de la ejecucion ***");
		return dataout;
	}	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"PLA\",\"vencimientos\":[1497032,1495096,1496824,1495272,1496344,1496056,1497416,1495384,1496168,1496328]}";
			EntradaNotificarVencimientoSimultaneas Objin = gson.fromJson(json, EntradaNotificarVencimientoSimultaneas.class);
			
			NotificarVencimientoSimultaneas demo=new NotificarVencimientoSimultaneas();				
 			try {
 				System.out.println(demo.logica(con, Objin));
 				con.commit();
 			}
 			catch(Exception er) {
				con.rollback();
			}
    	}
		System.out.println("== END ==");
	}

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		EntradaNotificarVencimientoSimultaneas in = new EntradaNotificarVencimientoSimultaneas();
		in.p_IdEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
		in.p_IdUsuario = Long.parseLong(this.getParam("IdUsuario"));
		in.p_IdModulo= this.getParam("IdModulo");
		SalidaNotificarVencimientoInstrumentos salida = logica(con, in); 
		return salida.estadoNotificacion;
	}

	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	
	

}
