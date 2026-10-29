package cl.bcs.plataforma.jobs;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.plataforma.evt.pojo.Evento;
import cl.bcs.plataforma.jobs.BcsProcesoPauta;
import cl.bcs.plataforma.jobs.BcsProcesoPauta.SalidaProcesoPauta;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class MailChecker extends BcsJob {
	
	private final Log log = new Log("PLA.MailChecker");

	FileLoader loader = new FileLoader();
	
	@Override
    public boolean transaction(Connection con) throws Throwable {
        return logica(con,false);   
    }
    
    public boolean logica(Connection conn, boolean debug) throws Throwable {
    	SQL _sql = new SQL();
    	log.write("Iniciando MailChecker");
    	//parametros entrada
    	Long idEmpresa = null;
    	Long idUsuario = null;
    	
    	if (debug) {
    		idEmpresa = 1L;
    		idUsuario = 0L;
    		
    	}
    	else {
    		idEmpresa = Long.parseLong(super.getParam("IdEmpresa"));
    		idUsuario = Long.parseLong(super.getParam("IdUsuario"));
    	}
    	
    	List<Evento> rebotados = new ArrayList<>();
    	
    	
    	String host = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.HostMailing");
	    String user = "optimus@mail.bcs.cl";
	    String password = "8rktz3mqmx";

	    Properties properties = System.getProperties();
	    Session session = Session.getDefaultInstance(properties);
	    Store store = session.getStore("pop3");
	    store.connect(host, user, password);
	    Folder inbox = store.getFolder("Inbox");
	    inbox.open(Folder.READ_ONLY);
    	
	    Message[] messages = inbox.getMessages();

	    log.write("Iniciando Lectura " + messages.length);
	    for (int i = 0; i < messages.length; i++) {
	    	
	    	log.write("Leyendo " + i);
	    	String contentType =  messages[i].getContentType();
	    	 
	    	if (contentType.contains("multipart")) {
	    		Multipart multiPart = (Multipart) messages[i].getContent();
	    		
	    		MimeBodyPart rebotado = null;
	    		
	    		for (int j = 0; j < multiPart.getCount(); j++) {
	    			MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(j);
	    			
	    			if (part.getContentType().equalsIgnoreCase("message/rfc822")) {
	    				rebotado = part;
	    				break;
	    				/*Enumeration asdf = part.getAllHeaderLines();
	    				while (asdf.hasMoreElements()) {
	    					String header = (String) asdf.nextElement();
	    					if (header.equalsIgnoreCase("Content-Description: Undelivered Message")) {
	    						rebotado = part;
	    						break;
	    					}
	    				}
	    				if (rebotado != null) {
	    					break;
	    				}*/
	    			}
	    		}
	    		
	    		
	    		if (rebotado != null) {
	    			try {
	    				log.write("Se encontro rebotado");
	    				InputStream stream = MimeUtility.decode(rebotado.getInputStream(),"quoted-printable");
	    				
	    				StringWriter writer = new StringWriter();
	    				IOUtils.copy(stream, writer);
	    				String theString = writer.toString();
	    				
		   		    	
		   		    	Document doc = Jsoup.parse(theString);
		   		    	//System.out.print(theString);
		   		    	Elements resultLinks = doc.select("#bcs-tracker"); 
		   		    	String idEventoCodificado = resultLinks.attr("data-id-evento");
		   		    	String idEventoDecodificado = Utils.decodificarEvento(idEventoCodificado);
		   		    	Evento evt = new Evento();
		   		    	evt.IdEvento = Long.parseLong(idEventoDecodificado);
		   		    	rebotados.add(evt);	
	    			}
	    			catch(Exception e) {
	    				log.write(e);
	    			}
	    		}
	    		
	    	}
	    	else {
	    		//ignoramos otros tipos de correos por ahora, solo buscamos multipart
	    	}
	    }
	    	
	    log.write("Fin Lectura");
	    
	    inbox.close(true);
	    store.close();

	    if (rebotados.size() > 0) {
	    	String update = "UPDATE \"BSCL.Normativa.BD::CIR.Nomina\" SET \"IdEstadoNotificacion\" = 'RBT' WHERE \"IdEvento\" = :IdEvento AND \"IdEstadoNotificacion\" IN ('ENV','PEN')";
    		_sql.batch(conn, update, rebotados, Evento.class);
	    }
	    
	    log.write("Fin MailChecker");
		return true;
    	
    	
    }

    @Override
	public void pause() {

	}
	
	public static void main(String [] args) throws Throwable {
		while (true) {
			System.out.println("INICIANDO");
			try (Connection conn = PlataformaUtils.getSURAConnection()) {
				MailChecker pauta = new MailChecker();
				pauta.logica(conn, true);
				conn.commit();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			Thread.sleep(5000);
		}
	}

	@Override
	public void onError(Throwable t) {
		log.write(t);
		t.printStackTrace();
	}
}