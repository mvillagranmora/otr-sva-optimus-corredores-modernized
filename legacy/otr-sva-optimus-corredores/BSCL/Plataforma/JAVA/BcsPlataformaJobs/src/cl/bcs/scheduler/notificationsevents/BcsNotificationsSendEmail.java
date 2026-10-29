package cl.bcs.scheduler.notificationsevents;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.lang.StringEscapeUtils;

import cl.bcs.scheduler.notifications.pojo.ArchivoAdjunto;
import cl.bcs.scheduler.notifications.pojo.Destinatario;
import cl.bcs.scheduler.notifications.pojo.Mail;
import cl.bcs.scheduler.notifications.pojo.ParametrosDefault;
import cl.bcs.scheduler.notifications.sql.Connectors;
import cl.bcs.scheduler.notifications.util.FileGenerator;
import cl.bcs.scheduler.notifications.util.Validator;
import cl.bcs.utils.Log;

public class BcsNotificationsSendEmail {
    private final Log log = new Log("EVT.NOTIFY");

    public static List<String> getTO() {
	return TO;
    }

    public static void setTO(List<String> tO) {
	TO = tO;
    }

    public static List<String> getCC() {
	return CC;
    }

    public static void setCC(List<String> cC) {
	CC = cC;
    }

    public static List<String> getBCC() {
	return BCC;
    }

    public static void setBCC(List<String> bCC) {
	BCC = bCC;
    }

    public static List<String> getREPLYTO() {
	return REPLYTO;
    }

    public static void setREPLYTO(List<String> rEPLYTO) {
	REPLYTO = rEPLYTO;
    }

    static List<String> TO;
    static List<String> CC;
    static List<String> BCC;
    static List<String> REPLYTO;

    public InternetAddress[] getAddressRecipents(List<String> mails) throws AddressException {

	List<InternetAddress> multipleMail = new ArrayList<InternetAddress>();
	for (String mail : Validator.duplicateRemoveString(mails)) {
	    if (Validator.emailValidate(mail)) {
		multipleMail.add(new InternetAddress(mail));
	    }
	}
	InternetAddress[] a = new InternetAddress[multipleMail.size()];
	for (int i = 0; i < multipleMail.size(); i++) {
	    a[i] = multipleMail.get(i);
	}
	return a;
    }

    private void addAttachment(Multipart multipart, String filename, byte[] bytes) throws MessagingException {
	DataSource source = new ByteArrayDataSource(bytes, "application/pdf");
	BodyPart messageBodyPart = new MimeBodyPart();
	messageBodyPart.setDataHandler(new DataHandler(source));
	messageBodyPart.setFileName(filename);
	multipart.addBodyPart(messageBodyPart);
    }

    private void ReplyTo(MimeMessage message) throws AddressException, MessagingException {
	List<String> mails = new ArrayList<String>();

	List<InternetAddress> multipleMail = new ArrayList<InternetAddress>();
	for (String mail : mails) {
	    multipleMail.add(new InternetAddress(mail));
	}
	InternetAddress[] a = new InternetAddress[multipleMail.size()];
	for (int i = 0; i < multipleMail.size(); i++) {
	    a[i] = multipleMail.get(i);
	}
	message.setReplyTo(a);

    }

    private void getTypeRecipients(List<Destinatario> recipients) throws SQLException, IOException {
	TO = new ArrayList<>();
	CC = new ArrayList<>();
	BCC = new ArrayList<>();
	REPLYTO = new ArrayList<>();
	List<Destinatario> destinarios = recipients;
	for (Destinatario dest : destinarios) {
	    if (dest.getTipoDestinatario().equals("TO")) {
		TO.add(dest.getDestinatario());
	    }
	    if (dest.getTipoDestinatario().equals("CC")) {
		CC.add(dest.getDestinatario());
	    }
	    if (dest.getTipoDestinatario().equals("BCC")) {
		BCC.add(dest.getDestinatario());
	    }
	    if (dest.getTipoDestinatario().equals("REPLY")) {
		REPLYTO.add(dest.getDestinatario());

	    }
	}
    }

    private void addRecipents(MimeMessage message, List<Destinatario> recipients) throws AddressException, MessagingException, SQLException, IOException {
	getTypeRecipients(recipients);
	if (TO.size() > 0) {
	    message.addRecipients(Message.RecipientType.TO, getAddressRecipents(TO));
	}
	if (CC.size() > 0) {
	    message.addRecipients(Message.RecipientType.CC, getAddressRecipents(CC));
	}
	if (BCC.size() > 0) {
	    message.addRecipients(Message.RecipientType.BCC, getAddressRecipents(BCC));
	}
	if (REPLYTO.size() > 0) {
	    message.setReplyTo(getAddressRecipents(REPLYTO));
	}

    }

    /**
     * Metodo encargado de leer los correos pendientes por enviar y los envia
     */
    public synchronized boolean mailSender(Connection connection, Connectors connectors, ParametrosDefault params, String environment, Map<Integer, String> tokens, String url) throws SQLException, IOException {

	List<ArchivoAdjunto> files = connectors.addRecipientsAndFile(connection, params);
	List<ArchivoAdjunto> filesAux = new ArrayList<ArchivoAdjunto>();
	for (ArchivoAdjunto c : files) {
	    if (c.IdArchivoAdjunto != -1) {
		ArchivoAdjunto r = FileGenerator.pdfGenerateFromHtml(c.getArchivoNoProcesado(), c.getNombre(), "", "");
		r.setIdArchivoAdjunto(c.getIdArchivoAdjunto());
		r.setIdEmail(c.getIdEmail());
		filesAux.add(r);
	    }
	}
	connectors.updateFiles(connection, params, filesAux);
	List<Mail> mailList = connectors.mailRead(connection);
	Map<String, List<Destinatario>> todosLosDestinatarios = connectors.recipientsReadByMailId(connection, environment, params);
	Map<String, String> valuesDefaultConfig = connectors.getValuesConfigurations(connection);

	List<Integer> IdMails = new ArrayList<Integer>();

	if (todosLosDestinatarios.size() > 0) {

	    for (final Mail mail : mailList) {
		String idMail = String.valueOf(mail.idMail);
		if (todosLosDestinatarios.get(idMail) != null) {
		    Map<String, List<Destinatario>> recipients = new HashMap<String, List<Destinatario>>();
		    recipients.put("CC", new ArrayList<Destinatario>());
		    recipients.put("TO", new ArrayList<Destinatario>());
		    for (Destinatario destinatario : todosLosDestinatarios.get(idMail)) {
			if (destinatario.getTipoDestinatario().equals("TO")) {
			    recipients.get("TO").add(destinatario);
			} else if (destinatario.getTipoDestinatario().equals("CC")) {
			    recipients.get("CC").add(destinatario);
			}
		    }
		    if (enviarEmail(mail, todosLosDestinatarios.get(idMail), filesAux, valuesDefaultConfig, tokens, url)) {
			IdMails.add(mail.getIdMail());
		    }
		}
	    }
	    connectors.updateEmail(IdMails, connection);
	    return true;
	}

	return false;

    }

    public boolean enviarEmail(Mail mail, List<Destinatario> recipients, List<ArchivoAdjunto> file, Map<String, String> valuesDefaultConfig, Map<Integer, String> tokens, String url) {
	if (recipients.size() > 0) {
	    // Sender's email ID needs to be mentioned
	    String from = valuesDefaultConfig.get("PLA.EmisorNotificacion");
	    // Assuming you are sending email from localhost

	    // Get system properties
	    Properties properties = System.getProperties();

	    // Setup mail server
	    properties.setProperty(valuesDefaultConfig.get("PLA.SMTP"), valuesDefaultConfig.get("PLA.HostMailing"));

	    properties.put("mail.smtp.timeout", 1000);
	    // Get the default Session object.
	    Session session = Session.getDefaultInstance(properties);

	    try {
		// Create a default MimeMessage object.
		MimeMessage message = new MimeMessage(session);

		ReplyTo(message);
		// Set From: header field of the header.
		message.setFrom(new InternetAddress(from, valuesDefaultConfig.get("PLA.AliasEmisorNotificacion")));

		// Set To: header field of the header.
		addRecipents(message, recipients);

		// Set Subject: header field
		message.setSubject(mail.getAsunto());

		// Create the message part
		BodyPart messageBodyPart = new MimeBodyPart();

		// Fill the message
		// messageBodyPart.setText();
		String mensajeCuerpo = mail.getMensajeCuerpo();
		/*???
		 * if(TypeRecipients.equals("CC")){
		 * 
		 * for(Map.Entry<Integer, String> c : tokens.entrySet()) {
		 * if(c.getValue().trim().length()>0) mensajeCuerpo =
		 * mensajeCuerpo.replace(c.getValue(), "#");
		 * 
		 * } }
		 */
		messageBodyPart.setContent(StringEscapeUtils.unescapeHtml(mensajeCuerpo), "text/html; charset=utf-8");
		// Create a multipar message
		Multipart multipart = new MimeMultipart();

		// Set text message part
		multipart.addBodyPart(messageBodyPart);

		for (ArchivoAdjunto fileAttachment : file) {

		    if (mail.getIdMail() == fileAttachment.getIdEmail())
			addAttachment(multipart, fileAttachment.getNombre(), fileAttachment.getArchivo());
		}

		// Send the complete message parts
		message.setContent(multipart);

		// Send message
		Transport.send(message);
		log.write("Enviando Correo!");
	    } catch (Exception mex) {
		log.write(mex.getMessage(), mex);
		return false;
	    }
	}
	return true;
    }

}