package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.evt.AgregarEmail;
import cl.bcs.plataforma.evt.AgregarEmail.Destinatario;
import cl.bcs.plataforma.evt.AgregarEmail.Email;
import cl.bcs.plataforma.evt.AgregarEmail.ServiceEntrada;
import cl.bcs.plataforma.pau.EntregarSistemaManual;
import cl.bcs.plataforma.pau.EntregarSistemaManual.AdelantarPautaEntrada;
import cl.bcs.plataforma.pau.EntregarSistemaManual.AdelantarPautaSalida;

public class ProgramarPautaJob extends BcsJob implements JavaCall {	

	private final String SQL_CORREOS_NOTIFICACION = loadResourceToString("cl/bcs/plataforma/jobs/sql/ProgramarPautaJob_SelectCorreosNotificacion.sql");
	private final String SQL_PAUTA_YA_EJECUTADA = loadResourceToString("cl/bcs/plataforma/jobs/sql/ProgramarPautaJob_SelectPautaYaEjecutada.sql");
	
	private static final Logger logger = LogManager.getLogger(ProgramarPautaJob.class);
	
	public static class ProgramarPautaJobEntrada {
		public Long 			p_IdEmpresa;
		public Long 			p_IdUsuario;
		public String			p_IdModulo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ProgramarPautaJobSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public ProgramarPautaJobSalida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	public static class RegistroPauta {
		public String FechaSistema;
	}
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		throw new Exception();
	}
	
	public static void main(String[] args) throws SQLException, Exception {
		
		
		
		
	    ProgramarPautaJob demo=new ProgramarPautaJob();
	    try (Connection conn = PlataformaUtils.getPropertiesConnection("desa","connections.properties")) {
	    	ProgramarPautaJobEntrada entrada = new ProgramarPautaJobEntrada();
			ProgramarPautaJobSalida salida = demo.logica(conn, entrada,true);
			conn.commit();
	    }
	        
	}
	
	public static class Correo {
		public String TipoProceso;
		public String NombreLista;
		public String ListaCorreos;
	}
	
	
	
	public ProgramarPautaJobSalida logica(Connection conn, ProgramarPautaJobEntrada entrada,boolean debug) throws Exception {
		ProgramarPautaJobSalida out = new ProgramarPautaJobSalida();
		SQL _sql = new SQL();
		
		
		Long idEmpresa;
		Long idUsuario;
		
		if (debug) {
			idEmpresa = 1L;
			idUsuario = 97L;
		}
		else {
			idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			idUsuario = Long.parseLong(this.getParam("IdUsuario"));
		}
		
		Long parIdPrograma= super.getJobId();
		
		List<String> correos = new ArrayList<>();
		try {
			List<Correo> f = _sql.query(conn, SQL_CORREOS_NOTIFICACION,new HashMap<>(), Correo.class);
			if (f.size() == 1) {
				correos = Arrays.asList(f.get(0).ListaCorreos.split(";"));
			}
		}
		catch (Exception e) {
			logger.error("Error configurando correos");
			logger.error(e);
		}
		
		//enviarCorreo(conn, idEmpresa, "PAUTA_PROGRAMADA", "ERROR", "ASUNTO", correos);
		
		//corrimos pauta hoy?
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");

		String localdate = LocalDate.now().toString();
		HashMap<String, Object> params = new HashMap<String,Object>();
		params.put("FechaSistema", fechaSistema);
		List<RegistroPauta> rp = _sql.query(conn, SQL_PAUTA_YA_EJECUTADA, params, RegistroPauta.class);
		
		//pauta correra solo si estoy en el mismo dia de la fecha de sistema y no hay otra pauta ya ejecutada este dia
		if (fechaSistema.equalsIgnoreCase(localdate) && rp.size() == 0) {
			logger.info("Puedo correr pauta porque estoy en el mismo real y de sistema y no hay otra pauta creada");
		}
		else if (!fechaSistema.equalsIgnoreCase(localdate) && rp.size() == 0 && debug) {
			logger.info("Puedo correr pauta en otro dia porque estoy en debug y no hay pauta creada");
		}
		else if (!fechaSistema.equalsIgnoreCase(localdate)) {
			String msj = "No se puede ejecutar pauta automaticamente cuando la fecha real (" + localdate + ") no corresponde a la fecha de sistema (" + fechaSistema + ")";
			logger.error(msj);
			enviarCorreo(conn, idEmpresa, "PAUTA_PROGRAMADA", "La tarea " + parIdPrograma + " generó el siguiente mensaje: " + msj, "Pauta Programada", correos);
			return out;
		}
		else { // rp.size() > 0
			String msj = "No se puede ejecutar pauta automaticamente ya que ya existe un proceso de pauta creado para el dia actual" + localdate;
			logger.error(msj);
			enviarCorreo(conn, idEmpresa, "PAUTA_PROGRAMADA", "La tarea " + parIdPrograma + " generó el siguiente mensaje: " + msj, "Pauta Programada", correos);
			return out;
		}
		
		
		EntregarSistemaManual m = new EntregarSistemaManual();
		AdelantarPautaEntrada entradaEntregarSistemaManual = new AdelantarPautaEntrada();
		entradaEntregarSistemaManual.p_IdEmpresa = idEmpresa;
		entradaEntregarSistemaManual.p_IdModulo = "PLA";
		entradaEntregarSistemaManual.p_IdUsuario = idUsuario;
		AdelantarPautaSalida salida = m.logica(conn, entradaEntregarSistemaManual );
		
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			
			if (salida.P_OUT_MENSAJES.size() > 0) {
				logger.error(salida.P_OUT_MENSAJES.get(0).descripcion);
				enviarCorreo(conn, idEmpresa, "PAUTA_PROGRAMADA", "La tarea " + parIdPrograma + " generó el siguiente mensaje: " + salida.P_OUT_MENSAJES.get(0).descripcion, "Pauta Programada", correos);
			}
			else {
				enviarCorreo(conn, idEmpresa, "PAUTA_PROGRAMADA", "La tarea " + parIdPrograma + " generó el siguiente mensaje: Ha ocurrido un error con la pauta programada, por favor avisar a Soporte SVA", "Pauta Programada", correos);
			}
			
			out.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR;
		}
		else {
			out.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		return out;
		
	}

	public static void enviarCorreo(Connection conn,Long idEmpresa,String nombreProceso,String mensaje,
			String asuntoEmail,List<String> emailAdministracionTo) throws Exception {
		AgregarEmail agregarEmail = new AgregarEmail();
	    ServiceEntrada entradaAgregarEmail = new ServiceEntrada();
	    
	    String templateNotificacion =
	            (Utils.getConfiguracion(
	                        conn, Utils.getSchema(), idEmpresa, "EVT.TemplateEmailAdministracion")
	                    .replace("${data.NOTIFICACION_ADMINISTRADOR.Proceso}", nombreProceso))
	                .replace("${data.NOTIFICACION_ADMINISTRADOR.Mensaje}", mensaje);

	    List<Email> emails = new ArrayList<Email>();
	    //Estructura basica del correo.
	    Email email = new Email();
	    email.IdEnvio = Long.parseLong("0");
	    email.Asunto = asuntoEmail;
	    email.Remitente = "emisor";
	    email.MensajeCuerpo = templateNotificacion;
	    emails.add(email);

	    List<Destinatario> destinatarios = new ArrayList<Destinatario>();
	    //obtener destinatarios PARA:
	    for (String destinatario : emailAdministracionTo) {
	      Destinatario dest = new Destinatario();
	      dest.IdEnvio = Long.parseLong("0");
	      dest.Destinatario = destinatario;
	      dest.TipoDestinatario = "TO";
	      destinatarios.add(dest);
	    }
	    

	    //agregar emails para el envio
	    entradaAgregarEmail.emails = emails;
	    entradaAgregarEmail.destinatarios = destinatarios;
	    agregarEmail.logica(conn, entradaAgregarEmail);
	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {

		ProgramarPautaJobSalida salida = this.logica(con, null, false);
		return true;
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
