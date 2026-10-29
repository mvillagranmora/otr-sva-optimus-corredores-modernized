package cl.bcs.plataforma.evt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.itextpdf.text.Document;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.PdfWriter;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EnviarEmail{
	static final Logger logger = LogManager.getLogger(EnviarEmail.class);
	
	//obtener de 100 correos por ejecucion (evita obtener demasiados correos por ejecucion en caso de errores)
    private String SQL_ObtenerEmails = loadResourceToString("cl/bcs/plataforma/evt/sql/EnviarEmail_ObtenerEmails.sql");
    private String SQL_ObtenerDestinatarios = "SELECT \"IdDestinatario\", \"IdEmail\", \"TipoDestinatario\", \"Destinatario\" FROM \"BSCL.Plataforma.BD::EVT.Destinatario\" WHERE \"IdEmail\" IN (:IdEmail);";
    private String SQL_ObtenerArchivosAdjuntos = "SELECT \"IdArchivoAdjunto\", \"IdEmail\", \"Nombre\", \"Archivo\" FROM \"BSCL.Plataforma.BD::EVT.ArchivoAdjunto\" WHERE \"IdEmail\" IN (:IdEmail);";
    private String SQL_ActualizarEmail = "UPDATE \"BSCL.Plataforma.BD::EVT.Mail\" SET \"EstadoEnviado\" = :EstadoEnviado, \"FechaEnviado\" = CURRENT_UTCTIMESTAMP, \"LogEnvio\" = :LogEnvio WHERE \"IdEmail\" = :IdEmail;";
    
    private static final int MAXIMO_INTENTOS_REENVIO = 3;
    private static final String ESTADO_ERROR = "E";
    private static final String ESTADO_EXITO = "S";
    
    private String loadResourceToString(final String path){		
	    final InputStream stream = this.getClass().getClassLoader().getResourceAsStream(path);
	    if (stream==null) {
	    	throw new IllegalStateException("can't load resource:"+path);
	    }
	    try{
	        return IOUtils.toString(stream);
	    } catch(final Exception e){
	        throw new IllegalStateException(e);
	    } finally{
	        IOUtils.closeQuietly(stream);
	    }
	}
    
    public static class EnviarEmailIn {
        public Long IdEmail;
        @Override
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class EnviarEmailOut {
        public boolean resultado;
        @Override
        public String toString() {
            return Utils.printJson(this, false);
        }
    }
    
    /*BO*/
    public static class Email{
        public Long IdEmail;
        public Timestamp FechaCreacion;
        public String Remitente;
        public String Asunto;
        public String MensajeCuerpo;
        public String EstadoEnviado;
        public String RemitenteEvento;
        public String LogEnvio = "";
        public String Usuario;
        public String Password;
        public String Host;
        public String Puerto;
        
        @Override
        public String toString() {
            return Utils.printJson(this, false);
        }
    }
    
    public static class Destinatario{
        public Long IdDestinatario;
        public Long IdEmail;
        public String TipoDestinatario;
        public String Destinatario;
        @Override
        public String toString() {
        	return Utils.printJson(this, false);
        }
    }
    
    public static class ArchivoAdjunto{
        public Long IdArchivoAdjunto;
        public Long IdEmail;
        public String Nombre; 
        public byte[] Archivo;
        public byte[] ArchivoCompilado;
        @Override
        public String toString() {
        	return Utils.printJson(this, false);
        }
    }
    
    public EnviarEmailOut logica(Connection conn, EnviarEmailIn in) throws Exception {
    	logger.info("INICIO::EnviarEmail.logica");
		long inicio = System.currentTimeMillis();
		
    	EnviarEmailOut out = new EnviarEmailOut();
    	
        SQL sql = new SQL();
        
        //obtener emails no enviados
        HashMap<String, Object> params = new HashMap<String, Object>();
        List<Email> emails = sql.query(conn, SQL_ObtenerEmails, params, Email.class);
        
        if(emails == null || emails.isEmpty()) {
        	out.resultado = true;
        	logger.debug("No hay correos para enviar");
        }else {
        
	        logger.debug("Cantidad de correos a procesar: " + emails.size());
	        //guardar ids de los emails para busqueda de destinatarios y archivos adjuntos
	        params.clear();
	        params.put("IdEmail", emails.stream().map(email -> email.IdEmail).collect(Collectors.toList()));
	        
	        //obtener todos destinatarios y archivos adjuntos de los emails a procesar
	        Map<Long, List<Destinatario>> destinatariosAgrupados = sql.query(conn, SQL_ObtenerDestinatarios, params, Destinatario.class).stream().collect(Collectors.groupingBy(dest -> dest.IdEmail));
	        Map<Long, List<ArchivoAdjunto>> archivosAdjuntosAgrupados = sql.query(conn, SQL_ObtenerArchivosAdjuntos, params, ArchivoAdjunto.class).parallelStream().collect(Collectors.groupingBy(adj -> adj.IdEmail));
	        
	        //obtener configuracion para enviar emails
	        Map<String, String> valuesDefaultConfig = new HashMap<>();
	        
	        valuesDefaultConfig.put("PLA.AliasEmisorNotificacion", ConfiguracionSistema.getParametroConfiguracionString(conn, 1l, "PLATAFORMA", "PLA.AliasEmisorNotificacion"));
	        valuesDefaultConfig.put("PLA.HostMailing", ConfiguracionSistema.getParametroConfiguracionString(conn, 1l, "PLATAFORMA", "PLA.HostMailing"));
	        valuesDefaultConfig.put("PLA.EmisorNotificacion", ConfiguracionSistema.getParametroConfiguracionString(conn, 1l, "PLATAFORMA", "PLA.EmisorNotificacion"));
	        
	        String usaEnvioCorreo = ConfiguracionSistema.getParametroConfiguracionString(conn, 1l, "PLATAFORMA", "EVT.UsaEnvioCorreo");
	        String listaEnvioCorreo = ConfiguracionSistema.getParametroConfiguracionString(conn, 1l, "PLATAFORMA", "EVT.ListaEnvioCorreo");
	        
	        
	        Properties prop = new Properties();
	        InputStream input = null;
	        String tipoEnvioCorreo;
	        
	        try {
	        	String rutaArchivosConfiguracion = Utils.getConfiguracion(conn, Utils.getSchema(), 1L, "PLA.RutaArchivosConfiguracion");
	        	input = new FileInputStream(rutaArchivosConfiguracion + "jobm/config.properties"); 
	        	prop.load(input);
	        	logger.debug("Se carga archivo de configuracion: /jobm/config.properties");
			} catch (FileNotFoundException e1) {
				try {
					input = new FileInputStream("jobm/config.properties"); 
		        	prop.load(input);
				}
				catch(FileNotFoundException e2) {
					logger.error("No es posible encontrar archivo de configuracion: /jobm/config.properties");
				}
			
			}
	        
	        tipoEnvioCorreo = (prop.containsKey("TipoEnvioCorreo")) ? prop.getProperty("TipoEnvioCorreo") : "QA";
	        logger.debug("tipoEnvioCorreo: " + tipoEnvioCorreo );
	        
	        final List<String> destinatariosValidos = (Optional.ofNullable(tipoEnvioCorreo).orElse("").equals("LISTA")) ?  Arrays.asList(listaEnvioCorreo.toLowerCase().split(";")) : null;
	        
	        //enviar emails
	        //evaluar conexion con servidor de correos.
	        boolean conexionSMTP = false;
	        if (emails.get(0) != null && emails.get(0).Host.length() > 0 && emails.get(0).Puerto.length() > 0) {
	        	conexionSMTP = conexionSmtpDisponible(emails.get(0).Host, Integer.parseInt(emails.get(0).Puerto));
	        }
	        else {
	        	conexionSMTP = conexionSmtpDisponible(valuesDefaultConfig.get("PLA.HostMailing"), 25);
	        }

	        logger.debug("Conexion con servidor de correos disponible?: " + conexionSMTP);
	        if(conexionSMTP && "S".equals(usaEnvioCorreo)) {
	        	List<Email> emailsProcesados = new ArrayList<>();
	        	//procesar en tandas de 10 correos
	            List<List<Email>> batch = Lists.partition(emails, 10);
	                
	            for (List<Email> correos : batch) {
	            	for (Email email : correos) {
	            		//agregar destinatarios del email
	            		List<Destinatario> destinatariosEmail = destinatariosAgrupados.getOrDefault(email.IdEmail, new ArrayList<>()).parallelStream().filter(destinatario -> {
	            			switch (tipoEnvioCorreo) {
	            				case "TODOS":
	                                return true;
	                            case "LISTA":
	                                return destinatariosValidos.contains(destinatario.Destinatario.toLowerCase());
	                            case "QA":
	                                return Optional.ofNullable(destinatario.Destinatario).orElse("").toUpperCase().endsWith("@NUAMX.COM");
	                            default:
	                                return false;
	                        	}
	                        }).collect(Collectors.toList());
	                        
	            		//agregar archivos adjuntos del email
	            		List<ArchivoAdjunto> archivosAdjuntosEmail = archivosAdjuntosAgrupados.getOrDefault(email.IdEmail, new ArrayList<>());
	            		//enviar
	            		logger.debug("Asunto: " + email.Asunto + ", Destinatarios: " + destinatariosEmail.size() + ", ArchivosAdjuntos: " + archivosAdjuntosEmail.size());
	            		Email resultadoEnvio = enviarEmail(email, destinatariosEmail, archivosAdjuntosEmail, valuesDefaultConfig);
	            		email.EstadoEnviado = resultadoEnvio.EstadoEnviado;
	            		email.LogEnvio = resultadoEnvio.LogEnvio;
	            		emailsProcesados.add(email);
	            	}
	                	
	            	//esperar cinco segundos antes de procesar el siguiente conjunto de correos
	            	Thread.sleep(5000);
	            }
	                
	            if(!emailsProcesados.isEmpty()){
	            	emails = emailsProcesados;
	            }
	            
	            sql.batch(conn, SQL_ActualizarEmail, emails, Email.class);
	            out.resultado = true;
	        }else {
	        	out.resultado = false;	
	        	logger.debug("Envio de correo desactivado o conexion SMTP no disponible");
	        }
        }
        
        long fin = System.currentTimeMillis();
		logger.info("FIN::EnviarEmail.logica", fin - inicio);
        return out;
    }

	protected boolean conexionSmtpDisponible(String host, int puerto) throws SocketException, IOException {
		logger.info("INICIO::EnviarEmail.conexionSmtpDisponible");
		long inicio = System.currentTimeMillis();
		
		boolean conexionSMTP = false;
		try {
        	TelnetClient tc = new TelnetClient();
            tc.setDefaultTimeout(5);
            tc.connect(host, puerto);
            conexionSMTP = tc.isAvailable();
            tc.disconnect();
        }catch(ConnectException ce) {
        	logger.error("NO SE PUEDE CONECTAR CON EL SERVIDOR DE CORREOS:" + host);
        	logger.error(ce);
        }
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::EnviarEmail.conexionSmtpDisponible", fin - inicio);
		return conexionSMTP;
	}
        
	protected Email enviarEmail(Email mail, List<Destinatario> recipients, List<ArchivoAdjunto> attachments, Map<String, String> valuesDefaultConfig) throws Exception {
		logger.info("INICIO::EnviarEmail.enviarEmail");
		long inicio = System.currentTimeMillis();
		
	    StringBuilder mensajeBuilder = new StringBuilder();

	    //Verificar si hay destinatarios
	    if (recipients.isEmpty()) {
	    	mensajeBuilder.append(String.format("El correo con IdEmail %s no tiene destinatarios.", mail.IdEmail));
	        logger.debug(mensajeBuilder.toString());
	        return cambiarEstadoEmail(mail, ESTADO_ERROR, mensajeBuilder.toString());
	    }
	    
	    int intentos = 0;
	    boolean enviado = false;
	    
	    try {
		    //Obtener la direccion de correo del remitente
		    String from = (mail.RemitenteEvento != null) ? mail.RemitenteEvento : valuesDefaultConfig.get("PLA.EmisorNotificacion");
		    
		    if (from == null || from.isEmpty()) {
		    	mensajeBuilder.append(String.format("No se puede enviar el correo con IdEmail %s: el remitente es nulo o vacio.", mail.IdEmail));
		        logger.debug(mensajeBuilder.toString());
		        return cambiarEstadoEmail(mail, ESTADO_ERROR, mensajeBuilder.toString());
		    }
		    
		    
		    //Configuracion de propiedades del servidor de correo
		    Properties properties = System.getProperties();
		    properties.setProperty("mail.smtp.host", valuesDefaultConfig.get("PLA.HostMailing"));
		    properties.setProperty("mail.smtp.port", "25");
		    properties.put("mail.smtp.auth", "false");
		    properties.put("mail.smtp.timeout", 10000);
		    properties.put("mail.smtp.connectiontimeout", 10000);
		    
		    Session session = null;
		    
		    //en el caso que el remitente tenga una configuracion de servidor de correos particular, se asume que es un servidor TLS
		    if (mail.Host.length() > 0 && mail.Puerto.length() > 0) {
		    	
		    	properties.put("mail.smtp.host", mail.Host);
		    	properties.put("mail.smtp.auth", "true");
			    properties.put("mail.smtp.port", mail.Puerto);
			    
			  //Crear la sesion de correo
			    Authenticator auth = new Authenticator() {
					//override the getPasswordAuthentication method
					protected PasswordAuthentication getPasswordAuthentication() {
						String user = mail.Usuario;
			            String password = mail.Password;
			            
						return new PasswordAuthentication(user, password);
					}
				};
				logger.debug("Session with custom server " + mail.Host + ":" + mail.Puerto + " - " + mail.Usuario);
				session = Session.getInstance(properties,auth);
		    }
		    else {
		    	logger.debug("Session with default server " + properties.getProperty("mail.smtp.host"));
		    	session = Session.getInstance(properties);
		    }
		    
		    //session.setDebug(true);
	    		
		    MimeMessage message = new MimeMessage(session);
		    message.setFrom(new InternetAddress(from, valuesDefaultConfig.get("PLA.AliasEmisorNotificacion")));
		    
	        //Crear el cuerpo del mensaje
	        BodyPart messageBodyPart = new MimeBodyPart();
	        messageBodyPart.setContent(StringEscapeUtils.unescapeHtml(mail.MensajeCuerpo), "text/html; charset=utf-8");
	
	        //Crear un mensaje multiparte
	        Multipart multipart = new MimeMultipart();
	        multipart.addBodyPart(messageBodyPart);
	        
	        //Agregar archivos adjuntos
	        for (ArchivoAdjunto fileAttachment : attachments) {
	            BodyPart bodyPart = new MimeBodyPart();
	            DataSource source = new ByteArrayDataSource(fileAttachment.Archivo, "application/pdf");
	            bodyPart.setDataHandler(new DataHandler(source));
	            bodyPart.setFileName(fileAttachment.Nombre);
	            multipart.addBodyPart(bodyPart);
	        }
	
	        //Establecer el contenido del mensaje
	        message.setContent(multipart);
	        
	        List<InternetAddress> toDest = new ArrayList<InternetAddress>();
	        List<InternetAddress> ccDest = new ArrayList<InternetAddress>();
	        
	        //Validar y clasificar destinatarios
	        for (Destinatario destinatario : recipients) {
	            if (Utils.emailValidate(destinatario.Destinatario)) {
	                if ("TO".equals(destinatario.TipoDestinatario)) {
	                    toDest.add(new InternetAddress(destinatario.Destinatario));
	                } else if ("CC".equals(destinatario.TipoDestinatario)) {
	                    ccDest.add(new InternetAddress(destinatario.Destinatario));
	                }
	            } else {
	                logger.debug(String.format("Direccion de email invalida: %s", destinatario.Destinatario));
	            }
	        }
	
	        //Verificar si hay destinatarios validos
	        if (toDest.isEmpty()) {
	        	mensajeBuilder.append(String.format("El correo con IdEmail %s no tiene destinatarios validos.", mail.IdEmail));
	            logger.debug(mensajeBuilder.toString());
	            return cambiarEstadoEmail(mail, ESTADO_ERROR, mensajeBuilder.toString());
	        }
	
	        //Establecer los destinatarios en el mensaje
	        message.addRecipients(Message.RecipientType.TO, toDest.toArray(new InternetAddress[0]));
	        
	        if (!ccDest.isEmpty()) {
	            message.addRecipients(Message.RecipientType.CC, ccDest.toArray(new InternetAddress[0]));
	        }
	
	        //Establecer el asunto del correo
	        message.setSubject(mail.Asunto);

		    while (intentos < MAXIMO_INTENTOS_REENVIO && !enviado) {
		        try {
		            logger.debug(String.format("Intentando enviar correo con IdEmail %s (intento %d)", mail.IdEmail, intentos + 1));
		            Transport.send(message);
		            mensajeBuilder.setLength(0);
		            mensajeBuilder.append(String.format("El correo con IdEmail %s fue enviado correctamente.", mail.IdEmail));
		            logger.debug(mensajeBuilder.toString());
		            enviado = true;
		        } catch (Exception mex) {
		            intentos++;
		            mensajeBuilder.setLength(0);
					mensajeBuilder.append(String.format("Error al enviar el correo con IdEmail %s en el intento %d. Error: %s", mail.IdEmail, intentos, mex.toString()));
		            logger.error(mensajeBuilder.toString());
		        }
		    }
	    }catch(Exception e){
	    	StackTraceElement[] stackTrace = e.getStackTrace();
	        StackTraceElement element = stackTrace[0]; 
			mensajeBuilder.append(String.format("Error al preparar el correo con IdEmail %s. Línea: %d, Error: %s", mail.IdEmail, element.getLineNumber(), e.toString()));
	        logger.error(mensajeBuilder.toString());
	    }
	    
	    long fin = System.currentTimeMillis();
		logger.info("FIN::EnviarEmail.enviarMail", fin - inicio);
	    return cambiarEstadoEmail(mail, enviado ? ESTADO_EXITO : ESTADO_ERROR, mensajeBuilder.toString());
	}

    protected byte[] generarPdfArchivoAdjunto(final String cuerpoMensaje, String nombreArchivo, final String claveUsuario, final String clavePropietario) {
    	logger.info("INICIO::EnviarEmail.generarPdfArchivoAdjunto");
		long inicio = System.currentTimeMillis();
		
        try {
            String filenameWE = nombreArchivo.replace(".", "-").split("-")[0];
            String x = StringEscapeUtils.unescapeJavaScript(cuerpoMensaje);

            nombreArchivo = filenameWE.concat(".pdf");
            try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream();){
            
                Document document = new Document();
                PdfWriter writer = PdfWriter.getInstance(document, outputStream);
                writer.setEncryption(claveUsuario.getBytes(), clavePropietario.getBytes(), PdfWriter.ALLOW_PRINTING, PdfWriter.ENCRYPTION_AES_128);
                writer.close();
                
                document.open();
                
                HTMLWorker htmlWorker = new HTMLWorker(document);
                htmlWorker.parse(new StringReader(x));
                
                document.close();
                htmlWorker.close();
                return outputStream.toByteArray();
            }
        } catch (Exception e) {
            logger.error(e);
        }
        
        long fin = System.currentTimeMillis();
        logger.info("FIN::EnviarEmail.generarPdfArchivoAdjunto", fin - inicio);
        return null;
    }
    
	private Email cambiarEstadoEmail(Email mail, String estado, String mensaje) {
	    mail.EstadoEnviado = estado;
	    mail.LogEnvio = (mensaje == null ? "" : mensaje.length() > 5000 ? mensaje.substring(0, 5000) : mensaje);
	    return mail;
	}
	
    
    public static void main (String args[]) {
    	System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("sura", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"IdEmail\":23469758}";
			EnviarEmailIn Objin = gson.fromJson(json, EnviarEmailIn.class);

			EnviarEmail demo = new EnviarEmail();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SQL sql = new SQL();
				SQL.exec(con, "UPDATE \"BSCL.Plataforma.BD::EVT.Mail\" SET \"EstadoEnviado\" = 'N' WHERE \"IdEmail\" = 23479799;");
				EnviarEmailOut ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println(ObjOut.toString());
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		System.out.println("== END ==");
    }

}
