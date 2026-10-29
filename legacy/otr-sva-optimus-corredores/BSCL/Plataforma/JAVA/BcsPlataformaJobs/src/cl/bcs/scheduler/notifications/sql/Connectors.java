package cl.bcs.scheduler.notifications.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import cl.bcs.scheduler.notifications.pojo.ArchivoAdjunto;
import cl.bcs.scheduler.notifications.pojo.Destinatario;
import cl.bcs.scheduler.notifications.pojo.Event;
import cl.bcs.scheduler.notifications.pojo.KeyValue;
import cl.bcs.scheduler.notifications.pojo.Mail;
import cl.bcs.scheduler.notifications.pojo.ParametrosDefault;
import cl.bcs.scheduler.notifications.pojo.Programa;
import cl.bcs.scheduler.notifications.util.DateUtils;
import cl.bcs.scheduler.notifications.util.FileGenerator;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

/**
* @author eespinoza
*
*/
/**
 * @author eespinoza
 *
 */
public class Connectors {

    private final String viewEventsNot = "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoNotificacion";
    private final String mailTable = "BSCL.Plataforma.BD::EVT.Mail";
    private final String recipentsTable = "BSCL.Plataforma.BD.Vistas::EVT.ConsultarDestinatarios";
    private final String fileAttachmentTable = "BSCL.Plataforma.BD::EVT.ArchivoAdjunto";
    private final String viewNotifications = "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoPorConfiguracion";
    private final String notificationsTable = "BSCL.Plataforma.BD::EVT.Notificacion";
    private final String addProgramm = "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma";
    private final String schema = "BSCL";
    private final String notificationsUpdate = "BSCL.Plataforma.BD.Servicios::EVT.ActualizarNotificacion";
    private final String process = "Procesado";
    private final String eventId = "IdEvento";
    private final String originalDispatchDate = "FechaDespachoOrginal";
    private final String send = "Enviado";
    private final String ReSend = "ReEnviar";
    private final String body = "MensajeBody";
    private final String notificationId = "IdNotificacion";
    private final String eventsConfigurationsId = "IdConfiguracionEvento";
    private final String groupNotifications = "BSCL.Plataforma.BD.Servicios::EVT.AgruparNotificaciones";
    private final String getMails = "BSCL.Plataforma.BD.Servicios::EVT.AgregarEmailDestinatarioArchivo";
    private final String addNotifications = "BSCL.Plataforma.BD.Servicios::EVT.AgregarNotificacion";
    private final String addNotificationsDetails = "BSCL.Plataforma.BD.Servicios::EVT.AgregarNotificacionDetalle";
    private final String updateMailSent = "BSCL.Plataforma.BD.Servicios::EVT.ActualizarEnvioMail";
    private final String CALLGETCONFIGURACION = "CALL \"BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion\"(?, ?, ?)";
    private final Log log = new Log("EVT.NOTIFY");

    public Connection sapHanaConnection() throws SQLException {
	return DriverManager.getConnection("jdbc:sap://161.238.34.39:30215/BSCL", "VARIACODE", "Variacode123123");
    }

    public List<Event> eventsReadNoti(Connection connection) throws SQLException {
	List<Event> listEvent = new ArrayList<Event>();
	final String query = String.format("select %s, %s, %s, %s from %s.%s where %s = 'N' ", getQuotedString(notificationId), getQuotedString(eventId), getQuotedString(process), getQuotedString(eventsConfigurationsId), getQuotedString(schema), getQuotedString(viewEventsNot), getQuotedString(process));
	log.write(query);
	try {
	    Connection con = connection;
	    try (final Statement st = con.createStatement()) {
		try (final ResultSet rs = st.executeQuery(query);) {

		    while (rs.next()) {
			Event evt = new Event();
			evt.setIdConfigurationEvent(rs.getInt(eventsConfigurationsId));
			evt.setIdEvent(rs.getInt(eventId));
			evt.setProcess(rs.getString(process));
			evt.setNotificationsId(rs.getInt(notificationId));
			listEvent.add(evt);
		    }
		    return listEvent;
		}
	    }
	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	}
	return null;
    }

    public void dropTableTemporary(Connection connection, String tableName) throws SQLException {
	String query = String.format("Drop table %s", tableName);
	log.write(query);
	
	try (PreparedStatement pst = connection.prepareStatement(query)) {
	    pst.execute();
	}
    }

    public List<Integer> notificationsRead(Connection connection) throws SQLException {
	List<Integer> idConfigurations = new ArrayList<Integer>();

	String query = String.format("select %s from %s.%s where %s < now() and (%s = 'N' or %s = 'S') ", getQuotedString(eventsConfigurationsId), getQuotedString(schema), getQuotedString(viewNotifications), getQuotedString(originalDispatchDate), getQuotedString(send), getQuotedString(ReSend));
	try {
	    Connection con = connection;
	    try (Statement st = con.createStatement()) {
		try (ResultSet rs = st.executeQuery(query);) {

		    Logger.getLogger(Connectors.class.getName()).log(Level.INFO, query);
		    while (rs.next()) {

			idConfigurations.add(rs.getInt(1));
		    }
		}
	    }
	} catch (Exception e) {
	    // TODO: handle finally clause
	    log.write(e.getMessage(), e);
	}
	return idConfigurations;
    }

    /**
     * @param connection
     * @param tableName
     *            Name of table temporary for to do join with view for get
     *            FileAttachment
     * @return
     * @throws SQLException
     */
    public List<ArchivoAdjunto> getFileAttachment(Connection connection) throws SQLException {
	List<ArchivoAdjunto> archivoList = new ArrayList<ArchivoAdjunto>();

	String query = String.format("" + "select cvw.%s,%s,%s " + "from %s.%s cvw " + "LEFT JOIN \"BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion\" tn " + "on tn.\"IdConfiguracionEvento\" = cvw.\"IdConfiguracionEvento\"" + " WHERE (%s < now() and (%s='N' or %s= 'S')) or \"Enviar\"='S'", getQuotedString(eventsConfigurationsId), getQuotedString(body), getQuotedString("CodigoTipoEvento"), getQuotedString(schema), getQuotedString(viewNotifications),
		getQuotedString(originalDispatchDate), getQuotedString(send), getQuotedString(ReSend));
	String date = DateUtils.getDateFormat("YYYYMMDD");
	log.write(query);
	try {
	    Connection con = connection;
	    try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(query);) {

		while (rs.next()) {
		    ArchivoAdjunto archivo = new ArchivoAdjunto();
		    archivo.setIdSecuenciaArchivoAdjunto(rs.getInt(1));
		    archivo.setArchivoNoProcesado(rs.getString(2));
		    archivo.setNombre(rs.getString(3) + "_" + date);
		    archivoList.add(archivo);
		}

	    }
	} catch (Exception e) {
	    // TODO: handle finally clause
	    log.write(e.getMessage(), e);
	    return null;
	}
	return archivoList;
    }

    public List<Mail> mailRead(Connection connection) throws SQLException {
	List<Mail> mailList = new ArrayList<Mail>();

	String query = "select mail.\"IdEmail\",\"FechaCreacion\",\"Remitente\",\"Asunto\", to_char(\"MensajeCuerpo\") AS \"MensajeCuerpo\",\"EstadoEnviado\",\"FechaEnviado\" from \"BSCL.Plataforma.BD::EVT.Mail\" mail  where \"EstadoEnviado\" = 'N'";

	log.write(query);
	try {
	    Connection con = connection;
	    try (Statement st = con.createStatement()) {
		try (ResultSet rs = st.executeQuery(query);) {

		    while (rs.next()) {
			Mail mail = new Mail();
			mail.setIdMail(rs.getInt("IdEmail"));
			mail.setAsunto(rs.getString("Asunto"));
			mail.setEstadoEnviado("EstadoEnviado");
			mail.setFechaEnviado(rs.getString("FechaEnviado"));
			mail.setFechaCreacion(rs.getString("FechaCreacion"));
			mail.setMensajeCuerpo(rs.getString("MensajeCuerpo"));
			mail.setRemitente(rs.getString("Remitente"));
			mailList.add(mail);
		    }
		}
	    }
	} catch (Exception e) {
	    // TODO: handle finally clause
	    log.write(e.getMessage(), e);
	}
	return mailList;
    }

    public void createTableMail(Connection connection) throws SQLException {
	String create = "CREATE LOCAL TEMPORARY TABLE #mail (\"IdEmail\" BIGINT)";
	try (PreparedStatement pst = connection.prepareStatement(create); Statement st = connection.createStatement()) {
	    pst.executeUpdate();
	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	}
    }

    public String insertTableMail(Connection connection, List<Mail> mailList) throws SQLException {
	String tableMail = "#mail";
	String insert = "INSERT INTO " + tableMail + " VALUES (?)";
	try (PreparedStatement pst = connection.prepareStatement(insert);) {
	    for (Mail mail : mailList) {
		pst.setInt(1, mail.getIdMail());
		pst.addBatch();
	    }
	    pst.executeBatch();
	    return tableMail;
	}
    }

    public Map<String, List<Destinatario>> recipientsReadByMailId(Connection connection, String environment, ParametrosDefault params) throws SQLException {
	List<Destinatario> recipientsListCC = new ArrayList<Destinatario>();
	List<Destinatario> recipientsListTO = new ArrayList<Destinatario>();

	Map<String, List<Destinatario>> lista = new HashMap<>();
	String query = String.format("" + "select" + " distinct " + "\"IdDestinatario\"," + "dest.\"IdEmail\"," + "\"TipoDestinatario\"," + "\"Destinatario\"  from %s.%s dest " + " INNER JOIN %s.%s tb " + " ON dest.\"IdEmail\" = tb.\"IdEmail\" " + " WHERE tb.\"EstadoEnviado\" = 'N' ORDER BY dest.\"IdEmail\" DESC ", getQuotedString(schema), getQuotedString(recipentsTable), getQuotedString(schema), getQuotedString("BSCL.Plataforma.BD::EVT.Mail"));
	log.write(query);

	List<String> destinatariosValidos = null;

	try {
	    Connection con = connection;
	    try (CallableStatement pstmConf = con.prepareCall(CALLGETCONFIGURACION);) {
		pstmConf.setLong(1, params.getIdEmpresa());
		pstmConf.setString(2, "EVT.UsaEnvioCorreo");
		pstmConf.registerOutParameter(3, Types.NVARCHAR);
		pstmConf.execute();
		String usaEnvioCorreo = pstmConf.getString(3);

		if (usaEnvioCorreo != null && usaEnvioCorreo.toUpperCase().equals("S")) {
		    pstmConf.setLong(1, params.getIdEmpresa());
		    pstmConf.setString(2, "EVT.TipoEnvioCorreo");
		    pstmConf.registerOutParameter(3, Types.NVARCHAR);
		    pstmConf.execute();
		    String tipoEnvioCorreo = (pstmConf.getString(3) != null) ? pstmConf.getString(3).toUpperCase() : "QA";

		    if (tipoEnvioCorreo != null && tipoEnvioCorreo.toUpperCase().equals("LISTA")) {
			pstmConf.setLong(1, params.getIdEmpresa());
			pstmConf.setString(2, "EVT.ListaEnvioCorreo");
			pstmConf.registerOutParameter(3, Types.NVARCHAR);
			pstmConf.execute();
			if (pstmConf.getString(3) != null && pstmConf.getString(3).length() > 0) {
			    destinatariosValidos = Arrays.asList(pstmConf.getString(3).toLowerCase().split(";"));
			} else {
			    log.write("LA LISTA DE CORREOS A ENVIAR ESTA VACIA.");
			}

		    }

		    try (PreparedStatement st = con.prepareStatement(query)) {
			try (ResultSet rs = st.executeQuery();) {
			    while (rs.next()) {
				// Nuevo metodo, crear una lista ordenada por los idEmail
				String idMail = String.valueOf(rs.getInt("IdEmail"));
				if (lista.get(idMail) == null) {
				    lista.put(idMail, new ArrayList<Destinatario>());
				}

				Destinatario recipient = new Destinatario();
				recipient.setIdDestinatario(rs.getInt("IdDestinatario"));
				recipient.setIdEmail(rs.getInt("IdEmail"));
				recipient.setTipoDestinatario(rs.getString("TipoDestinatario"));
				String email = rs.getString("Destinatario").toUpperCase();
				log.write("#" + recipient.getIdEmail() + ", direccion: " + email + ", Tipo correo: " + recipient.getTipoDestinatario());

				if (tipoEnvioCorreo.equals("TODOS")) {
				    recipient.setDestinatario(email);
				} else if (tipoEnvioCorreo.equals("LISTA") && destinatariosValidos.contains(email.toLowerCase())) {
				    recipient.setDestinatario(email);
				} else if (tipoEnvioCorreo.equals("QA") && email.endsWith("@BOLSADESANTIAGO.COM")) {
				    recipient.setDestinatario(email);
				} else {
				    recipient.setDestinatario("");
				    log.write("CORREO " + recipient.getTipoDestinatario() + " " + email + " no tiene esta permitido para la configuracion: " + tipoEnvioCorreo);
				}

				lista.get(idMail).add(recipient);

			    }
			}
		    }
		} else {
		    log.write("EL ENVIO DE CORREOS ESTÁ DESACTIVADO");
		}
	    }
	} catch (Exception e) {
	    // TODO: handle finally clause
	    log.write(e.getMessage(), e);
	}

	return lista;
    }

    public List<ArchivoAdjunto> fileAttachmentByMailId(Connection connection, String tableName) throws SQLException {
	List<ArchivoAdjunto> fileAttachmentList = new ArrayList<ArchivoAdjunto>();

	String query = String.format("select " + "\"IdArchivoAdjunto\", " + "fa.\"IdEmail\", " + "\"Nombre\"," + "\"Archivo\" " + "from %s.%s fa " + "LEFT JOIN " + tableName + " aux " + " ON aux.\"IdEmail\" = fa.\"IdEmail\" " + "", getQuotedString(schema), getQuotedString(fileAttachmentTable));
	log.write(query);
	try {
	    Connection con = connection;
	    try (Statement st = con.createStatement()) {
		try (ResultSet rs = st.executeQuery(query);) {

		    while (rs.next()) {
			ArchivoAdjunto fileAttachment = new ArchivoAdjunto();
			fileAttachment.setIdArchivoAdjunto(rs.getInt("IdArchivoAdjunto"));
			fileAttachment.setIdEmail(rs.getInt("IdEmail"));
			fileAttachment.setNombre(rs.getString("Nombre"));
			fileAttachment.setArchivo(rs.getBytes("Archivo"));
			fileAttachmentList.add(fileAttachment);
		    }
		}
	    }
	} catch (Exception e) {
	    // TODO: handle finally clause
	    log.write(e.getMessage(), e);
	}
	return fileAttachmentList;
    }

    public boolean delete(Connection connection, String tableName) {
	String query = "Delete from " + tableName;
	try {
	    Connection con = connection;
	    try (PreparedStatement pst = con.prepareStatement(query)) {
		return pst.execute();
	    }

	} catch (SQLException e) {

	    log.write(e.getMessage(), e);
	    return false;
	}

    }

    public Map<String, String> getValuesConfigurations(Connection connection) {
	String query = "SELECT \"CodigoParametro\", \"Valor\" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"CodigoParametro\" = 'PLA.AliasEmisorNotificacion' or  \"CodigoParametro\" = 'PLA.HostMailing' or \"CodigoParametro\" = 'PLA.SMTP' or \"CodigoParametro\" = 'PLA.EmisorNotificacion' ";
	Map<String, String> values = new HashMap<String, String>();
	try {
	    Connection con = connection;
	    try (PreparedStatement pst = con.prepareStatement(query)) {
		ResultSet rs = pst.executeQuery();
		while (rs.next()) {

		    values.put(rs.getString(1), rs.getString(2));

		}
	    }

	} catch (SQLException e) {

	    log.write(e.getMessage(), e);
	    return null;
	}
	return values;
    }

    public List<Event> existsInNotification(Connection connection, ParametrosDefault params, List<Event> evt) throws SQLException {
	String query = "";
	int aux = 0;
	for (int i = 0; i < evt.size(); i++) {
	    aux++;
	    if (aux == evt.size()) {
		query += String.format("select count(1) as contador, " + evt.get(i).getIdConfigurationEvent() + "  from %s.%s where %s = %s", getQuotedString(schema), getQuotedString(notificationsTable), getQuotedString(eventsConfigurationsId), evt.get(i).getIdConfigurationEvent());

	    } else {
		query += String.format("select count(1) as contador, " + evt.get(i).getIdConfigurationEvent() + "  from %s.%s where %s = %s UNION ALL ", getQuotedString(schema), getQuotedString(notificationsTable), getQuotedString(eventsConfigurationsId), evt.get(i).getIdConfigurationEvent());

	    }

	}
	List<Event> event = new ArrayList<Event>();
	log.write(query);

	try {
	    Connection con = connection;
	    try (PreparedStatement cstm = con.prepareStatement(query)) {

		if (cstm.execute()) {
		    try (ResultSet rs = cstm.getResultSet()) {
			while (rs.next()) {
			    Event evnt = new Event();
			    evnt.setGroup(rs.getInt(1) == 0 ? false : true);
			    evnt.setIdConfigurationEvent(rs.getInt(2));
			    event.add(evnt);

			}
		    }
		    return event;
		}
	    }
	} catch (Exception e) {
	    // TODO: handle finally clause
	    log.write(e.getMessage(), e);
	    return null;
	}
	return null;

    }

    public int createTablesNotiAndNotiDetails(Connection connection) throws SQLException {
	String query = "Create local temporary table #notifications (\"IdConfiguracionEvento\" BIGINT NOT NULL)";
	String queryDos = "Create local temporary table #notificationsDetails (\"IdConfiguracionEvento\" BIGINT NOT NULL)";
	String queryTres = "create local temporary table #events (\"IdEvento\" BIGINT)";
	try (PreparedStatement cstm = connection.prepareStatement(query); PreparedStatement cstmTwo = connection.prepareStatement(queryDos); PreparedStatement cstmThree = connection.prepareStatement(queryTres)) {

	    Statement st = connection.createStatement();
	    st.execute("SET SCHEMA BSCL");
	    boolean t = cstmThree.execute();
	    boolean r = cstm.execute();
	    boolean f = cstmTwo.execute();
	    log.write(t + " " + r + " " + f);

	}
	return 0;

    }

    public int createLocalTemporaryTable(Connection connection) {
	try {
	    String query = "Create local temporary table #ConfiguracionIds (\"IdConfiguracionEvento\" BIGINT)";
	    String queryEvetns = "Create local temporary table #events (\"IdEvento\" BIGINT)";
	    try (PreparedStatement cst = connection.prepareStatement(queryEvetns); PreparedStatement cstm = connection.prepareStatement(query); PreparedStatement pst = connection.prepareStatement("SET SCHEMA BSCL")) {
		pst.executeUpdate();
		cstm.execute();
		cst.execute();
		return 0;

	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return -1;

    }

    public String insertIdConfigurations(Connection connection, List<Event> Event) throws SQLException {
	String query = "INSERT INTO  #ConfiguracionIds VALUES  (?)";
	Logger.getLogger(Connectors.class.getName()).log(Level.INFO, query);

	try (PreparedStatement cstm = connection.prepareStatement(query)) {

	    for (Event condIf : Event) {
		cstm.setInt(1, condIf.getIdConfigurationEvent());
		cstm.addBatch();
	    }
	    cstm.executeBatch();
	    return "#ConfiguracionIds";
	}
    }

    public boolean fileAttachmentInsert(Connection connection, List<ArchivoAdjunto> archv) throws SQLException {
	String query = "INSERT INTO  #archivoadjunto VALUES  (?,?,?)";
	log.write(query);

	try (PreparedStatement cstm = connection.prepareStatement(query)) {

	    for (ArchivoAdjunto archivo : archv) {
		File dir = new File(archivo.getNombre());
		File[] files = dir.listFiles();
		try {

		    if (files != null) {
			for (File file : files) {

			    String fileName = file.getName();
			    FileInputStream fis = new FileInputStream(file);
			    cstm.setInt(1, archivo.getIdSecuenciaArchivoAdjunto());
			    cstm.setString(2, fileName);
			    cstm.setBinaryStream(3, fis, (int) file.length());
			    cstm.addBatch();

			}
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	    cstm.executeBatch();

	    return true;
	}
    }

    public boolean insertNotifications(Connection connection, List<Integer> IdConf) throws SQLException {
	String query = "INSERT INTO  #notifications VALUES  (?)";
	Logger.getLogger(Connectors.class.getName()).log(Level.INFO, query);

	try (PreparedStatement cstm = connection.prepareStatement(query)) {

	    for (Integer condIf : IdConf) {
		cstm.setInt(1, condIf);
		cstm.addBatch();
	    }
	    cstm.executeBatch();
	    return true;
	}
    }

    public String insertEventsLocal(Connection connection, List<Event> event) throws SQLException {
	String query = "INSERT INTO  #events VALUES  (?)";
	log.write(query);

	try (PreparedStatement cstm = connection.prepareStatement(query)) {

	    for (Event e : event) {
		cstm.setInt(1, e.getIdEvent());
		cstm.addBatch();
	    }

	    cstm.executeBatch();

	    return "#events";
	}
    }

    public boolean insertNotificationsDetails(Connection connection, List<Integer> event) throws SQLException {
	String query = "INSERT INTO  #notificationsDetails  VALUES  (?)";
	log.write(query);

	try (PreparedStatement cstm = connection.prepareStatement(query)) {
	    for (Integer e : event) {
		cstm.setInt(1, e);
		cstm.addBatch();
	    }
	    cstm.executeBatch();
	    return true;
	}
    }

    public void addNotifications(Connection connection, String tableName, ParametrosDefault params) throws SQLException {
	String query = String.format("{CALL %s.%s(?,?,?,%s,?)}", getQuotedString(schema), getQuotedString(addNotifications), tableName);
	log.write(query);

	try (CallableStatement cstm = connection.prepareCall(query)) {
	    cstm.setInt(1, params.getIdEmpresa());
	    cstm.setInt(2, params.getIdUsuario());
	    cstm.setString(3, params.getModulo());
	    cstm.registerOutParameter(4, Types.NVARCHAR);
	    cstm.execute();

	}
    }

    public void updateMailSent(Connection connection, String tableName, ParametrosDefault params) throws SQLException {
	String query = String.format("{CALL %s.%s(?,?,?,%s,?,?)}", getQuotedString(schema), getQuotedString(updateMailSent), tableName);
	log.write(query);

	try (CallableStatement cstm = connection.prepareCall(query)) {
	    cstm.setInt(1, params.getIdEmpresa());
	    cstm.setInt(2, params.getIdUsuario());
	    cstm.setString(3, params.getModulo());
	    cstm.registerOutParameter(4, Types.NVARCHAR);

	    if (cstm.execute()) {
		try (ResultSet rs = cstm.getResultSet()) {
		    while (rs.next()) {
			log.write(rs.getString(1));
		    }
		}
	    }

	}
    }

    public void addNotificationsDetails(Connection connection, String tableName, ParametrosDefault params) throws SQLException {
	String query = String.format("{CALL %s.%s(?,?,?,%s,?)}", getQuotedString(schema), getQuotedString(addNotificationsDetails), tableName);
	log.write(query);

	try (CallableStatement cstm = connection.prepareCall(query)) {
	    cstm.setInt(1, params.getIdEmpresa());
	    cstm.setInt(2, params.getIdUsuario());
	    cstm.setString(3, params.getModulo());
	    cstm.registerOutParameter(4, Types.NVARCHAR);
	    cstm.execute();
	}
    }

    public void updateFiles(Connection connection, ParametrosDefault params, List<ArchivoAdjunto> files) {
	final String query = "UPDATE \"BSCL.Plataforma.BD::EVT.ArchivoAdjunto\" SET \"Archivo\"= ?, \"Nombre\"=? WHERE \"IdEmail\" =?";

	try {

	    try (PreparedStatement pst = connection.prepareStatement(query);) {
		Logger.getLogger(Connectors.class.getName()).log(Level.INFO, query);
		for (ArchivoAdjunto c : files) {

		    Blob blob = new javax.sql.rowset.serial.SerialBlob(c.getArchivo());
		    pst.setBlob(1, blob);
		    pst.setString(2, c.getNombre());
		    pst.setInt(3, c.getIdArchivoAdjunto());
		    pst.addBatch();

		}
		pst.executeBatch();
		connection.commit();

	    }

	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	}

    }

    public List<ArchivoAdjunto> addRecipientsAndFile(Connection connection, ParametrosDefault params) throws SQLException {
	String query = String.format("{CALL %s.%s(?,?,?,?,?)}", getQuotedString(schema), getQuotedString(getMails));
	log.write(query);
	List<ArchivoAdjunto> archivos = new ArrayList<ArchivoAdjunto>();
	try {
	    Connection con = connection;
	    try (CallableStatement cstm = con.prepareCall(query)) {
		cstm.setInt(1, params.getIdEmpresa());
		cstm.setInt(2, params.getIdUsuario());
		cstm.setString(3, params.getModulo());
		cstm.registerOutParameter(4, java.sql.Types.NVARCHAR);

		if (cstm.execute()) {
		    log.write("registerout = " + cstm.getString(4));
		    try (ResultSet rs = cstm.getResultSet()) {
			while (rs.next()) {
			    ArchivoAdjunto add = new ArchivoAdjunto();

			    add.setIdEmail(rs.getInt("IdEmail"));
			    add.setIdArchivoAdjunto(rs.getInt("IdArchivoAdjunto"));
			    add.setArchivoNoProcesado(rs.getString("Archivo"));
			    add.setNombre(rs.getString("Nombre"));
			    archivos.add(add);
			}
		    }
		}
	    }

	} catch (Exception e) {
	    // TODO: handle finally clause
	    log.write(e.getMessage(), e);
	}

	return archivos;
    }

    public String addBatchEvents(List<Event> eventList, Connection connection) throws SQLException {
	final String nombreTabla = getTemporaryName();

	String createTable = "CREATE LOCAL TEMPORARY TABLE " + nombreTabla + " (\"IdEvent\" BIGINT, \"Procesado\" NVARCHAR(1))";
	String insertTable = "INSERT INTO " + nombreTabla + " values (?,?)";

	try {
	    Connection con = connection;

	    try (Statement anotherSt = con.createStatement();) {
		anotherSt.execute(createTable);
		log.write(insertTable);
		try (PreparedStatement cstmt = con.prepareStatement(insertTable)) {
		    for (Event evt : eventList) {
			cstmt.setInt(1, evt.getIdEvent());
			cstmt.setString(2, evt.getProcess());
			cstmt.addBatch();
		    }
		    cstmt.executeBatch();
		    return nombreTabla;
		}
	    }
	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	}
	return null;
    }

    public String configurationEventIdsAddBatch(List<Integer> idconfigurations, Connection connection) throws SQLException {
	final String tableName = getTemporaryName();

	String createTable = "CREATE LOCAL TEMPORARY TABLE " + tableName + " (\"IdConfiguracionEvento\" BIGINT)";
	String insertTable = "INSERT INTO " + tableName + " values (?)";

	try {
	    Connection con = connection;

	    try (PreparedStatement anotherSt = con.prepareStatement(createTable);) {
		anotherSt.execute();
		Logger.getLogger(Connectors.class.getName()).log(Level.INFO, createTable);

		try (PreparedStatement cstmt = con.prepareStatement(insertTable)) {
		    Logger.getLogger(Connectors.class.getName()).log(Level.INFO, insertTable);

		    for (Integer archivo : idconfigurations) {
			cstmt.setInt(1, archivo);
			cstmt.addBatch();
		    }
		    cstmt.executeBatch();

		    return tableName;
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    log.write(e.getMessage(), e);
	}
	return null;
    }

    /**
     * @param archivosList
     * @param connection
     * @return
     * @throws SQLException
     */
    public String notificationIdsAddBatch(List<ArchivoAdjunto> archivosList, long jobId, Connection connection) throws SQLException {
	final String nombreTabla = "#notif_" + jobId;

	String createTable = "CREATE LOCAL TEMPORARY TABLE " + nombreTabla + " (\"IdNotificacion\" BIGINT)";
	String insertTable = "INSERT INTO " + nombreTabla + " values (?)";

	try {
	    Connection con = connection;

	    try (PreparedStatement anotherSt = con.prepareStatement(createTable);) {
		anotherSt.execute();
		Logger.getLogger("******" + Connectors.class.getName()).log(Level.INFO, createTable);

		try (PreparedStatement cstmt = con.prepareStatement(insertTable)) {
		    Logger.getLogger(Connectors.class.getName()).log(Level.INFO, insertTable);

		    for (ArchivoAdjunto archivo : archivosList) {
			cstmt.setInt(1, archivo.getIdArchivoAdjunto());
			cstmt.addBatch();
		    }
		    cstmt.executeBatch();
		    return nombreTabla;
		}
	    }
	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	}
	return null;
    }

    /**
     * @param connection
     * @param params
     * @param tableName
     * @return
     */
    public Map<String, List<Long>> groupNotifications(Connection connection, ParametrosDefault params) {
	List<Long> idEventosAgrupados = new ArrayList<Long>();
	List<Long> idEventosNoAgrupados = new ArrayList<Long>();
	List<Long> idConfiguracionAgrupados = new ArrayList<Long>();
	List<Long> idConfiguracionNoAgrupados = new ArrayList<Long>();
	List<Long> idNotificaciones = new ArrayList<Long>();
	Map<String, List<Long>> ids = new HashMap<String, List<Long>>();
	String query = "{CALL \""+Utils.getSchema()+"\".\"BSCL.Plataforma.BD.Servicios::EVT.AgruparNotificaciones\"(?,?,?,?,?,?,?)}";

        
	try {
	    Connection con = connection;

	    try (CallableStatement cstm = con.prepareCall(query);) {
		cstm.setInt(1, params.getIdEmpresa());
		cstm.setInt(2, params.getIdUsuario());
		cstm.setString(3, params.getModulo());
		cstm.registerOutParameter(4, Types.BIGINT);
		boolean f = cstm.execute();
		int contador = 0;
		while (f) {
		    contador++;
		    try (ResultSet rs = cstm.getResultSet()) {
			while (rs.next()) {
			    if (contador == 1) {
				idEventosAgrupados.add(rs.getLong("IdEventoAgrupado"));
				idConfiguracionAgrupados.add(rs.getLong("IdConfiguracionEvento"));
			    }
			    if (contador == 2) {
				idEventosNoAgrupados.add(rs.getLong("IdEventoNoAgrupado"));
				idConfiguracionNoAgrupados.add(rs.getLong("IdConfiguracionEvento"));
			    }
			    if (contador == 3) {
			        idNotificaciones.add(rs.getLong("IdNotificacion"));
                            }
			}
		    }
		    f = cstm.getMoreResults();
		}
		ids.put("IdEventoAgrupados", idEventosAgrupados);
		ids.put("IdConfiguracionEventoAgrupados", idConfiguracionAgrupados);
		ids.put("IdEventoNoAgrupados", idEventosNoAgrupados);
		ids.put("IdConfiguracionEventoNoAgrupados", idConfiguracionNoAgrupados);
		ids.put("IdNotificaciones", idNotificaciones);

	    }

	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	    return null;
	}
	return ids;
    }

    public boolean notifyOutTime(Connection connection, ParametrosDefault params) {

	String query = "{CALL \""+Utils.getSchema()+"\".\"BSCL.Plataforma.BD.Servicios::EVT.ActualizarNotificacionVencida\"(?,?,?,?)}";
	try {
	    try (CallableStatement cstm = connection.prepareCall(query);) {
		cstm.setInt(1, params.getIdEmpresa());
		cstm.setInt(2, params.getIdUsuario());
		cstm.setString(3, params.getModulo());
		cstm.registerOutParameter(4, Types.NVARCHAR);
		return cstm.execute();
	    }
	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	    return false;
	}
    }

    public Map<Integer, Integer> getIdConfAndEvt(Connection connection, ParametrosDefault params) {

	final String sql = "select a.\"IdConfiguracionEvento\", c.\"IdEvento\" from \"BSCL.Plataforma.BD::EVT.ConfiguracionEvento\" a INNER JOIN bscl.\"BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle\" b ON b.\"IdConfiguracionEvento\" = a.\"IdConfiguracionEvento\" INNER JOIN bscl.\"BSCL.Plataforma.BD::EVT.Evento\" c ON c.\"CodigoTipoEvento\" = b.\"CodigoTipoEvento\"";
	Map<Integer, Integer> ids = new HashMap<Integer, Integer>();

	try {
	    Connection con = connection;
	    try (PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {

		while (rs.next()) {
		    ids.put(rs.getInt("IdConfiguracionEvento"), rs.getInt("IdEvento"));
		}
	    } catch (Exception e) {
		log.write(e.getMessage(), e);
	    }
	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	}

	return ids;
    }

    public String getEnvironment(Connection con, ParametrosDefault params) {

	final String sql = "select \"Valor\" from \"BSCL.Plataforma.BD::PLA.Configuracion\" where \"CodigoParametro\" = 'PLA.Ambiente' and \"IdEmpresa\" = ?";
	String environment = "";

	try {
	    try (PreparedStatement pst = con.prepareStatement(sql)) {
		pst.setLong(1, params.getIdEmpresa());

		try (ResultSet rs = pst.executeQuery()) {
		    while (rs.next()) {
			environment = rs.getString("Valor");
		    }
		}
	    } catch (Exception e) {
		log.write(e.getMessage(), e);
	    }
	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	}

	return environment;
    }

    public String getUrl(Connection connection, ParametrosDefault params) {

	final String sql = "select \"Valor\" from \"BSCL.Plataforma.BD::PLA.Configuracion\" where \"CodigoParametro\" = 'PLA.UrlPortalConfirmacion' and \"IdEmpresa\" = ?";
	String environment = "";

	try {
	    Connection con = connection;
	    try (PreparedStatement pst = con.prepareStatement(sql);) {
		pst.setLong(1, params.getIdEmpresa());

		try (ResultSet rs = pst.executeQuery()) {
		    while (rs.next()) {
			environment = rs.getString("Valor");
		    }
		}
	    } catch (Exception e) {
		log.write(e.getMessage(), e);
	    }
	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	}

	return environment;
    }

    public String getConfirmationMacro(Connection connection) {

	final String sql = "select \"ValorDefault\" from \"BSCL.Plataforma.BD::EVT.TagGenerales\" where \"Llave\" = 'Confirmar'";
	String tag = "";

	try {
	    Connection con = connection;
	    try (PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {

		while (rs.next()) {
		    tag = rs.getString("ValorDefault");
		}
	    } catch (Exception e) {
		log.write(e.getMessage(), e);
	    }
	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	}

	return tag;
    }

    /**
     * Método que identifica si el mismo JOB se esta ejecutando en otro nodo
     * 
     * @param connection
     * @param IdPrograma
     *            ID del JOB actual en la tabla programa
     * @return
     */
    public int inExecutionThisTask(Connection connection, long IdPrograma) {

	final String sql = "select count(1) AS \"CONTADOR\" from \"BSCL.Plataforma.BD::BAT.Programa\" where \"IdTipoPrograma\" = 'NOTIFY' and ( \"idEstado\"='ASI') and \"IdPrograma\" != ? ";
	int tag = 0;

	try {
	    Connection con = connection;
	    try (PreparedStatement pst = con.prepareStatement(sql);) {

		pst.setLong(1, IdPrograma);
		ResultSet rs = pst.executeQuery();
		while (rs.next()) {
		    int i = rs.getInt("CONTADOR");
		    System.out.println("Contador I =:::::=" + i);
		    return i;
		}
	    } catch (Exception e) {
		log.write(e.getMessage(), e);
	    }
	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	}

	return tag;
    }

    public Map<Integer, String> getTokens(Connection connection, ParametrosDefault params) {

	final String sql = "select distinct \"IdConfiguracionEvento\", \"TokenConfirmacion\"  from \"BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion\"  ";
	log.write(sql);
	Map<Integer, String> template = new HashMap<Integer, String>();

	try {
	    Connection con = connection;
	    try (PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {

		while (rs.next()) {
		    template.put(rs.getInt("IdConfiguracionEvento"), rs.getString("TokenConfirmacion"));
		}
	    } catch (Exception e) {
		log.write(e.getMessage(), e);
	    }
	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	}

	return template;
    }

    public Map<String, Long> getIdPersona(Connection connection, ParametrosDefault params) {

	final String sql = "SELECT \"IdEvento\", \"IdPersonaDestinatario\" FROM \"BSCL.Plataforma.BD::EVT.Evento\" where \"Procesado\"='N'";
	Map<String, Long> idPersona = new HashMap<String, Long>();

	try {
	    Connection con = connection;
	    try (PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {

		while (rs.next()) {
		    idPersona.put(rs.getString(1), rs.getLong(2));
		}
	    } catch (Exception e) {
		log.write(e.getMessage(), e);
	    }
	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	}

	return idPersona;
    }

    public Map<Long, String> getTemplate(Connection connection, ParametrosDefault params) {

	final String sql = "select distinct to_char(\"Template\") as \"Template\", cce.\"IdConfiguracionEvento\" from \"BSCL.Plataforma.BD.Vistas::EVT.ConsultarPlantillas\"  cp LEFT JOIN \"BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventos\" cce ON cce.\"IdPlantilla\" = cp.\"IdPlantilla\"  WHERE  cce.\"IdConfiguracionEvento\" is not null";
	Map<Long, String> template = new HashMap<Long, String>();

	try {
	    Connection con = connection;
	    try (PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {

		while (rs.next()) {
		    template.put(rs.getLong("IdConfiguracionEvento"), rs.getString("Template"));
		}
	    } catch (Exception e) {
		log.write(e.getMessage(), e);
	    }
	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	}

	return template;
    }

    public Map<Long, String> getCodeEventType(Connection connection, ParametrosDefault params) {

	final String sql = "SELECT  DISTINCT \"IdConfiguracionEvento\",evt.\"CodigoTipoEvento\" FROM \"BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle\" ced INNER JOIN \"BSCL.Plataforma.BD::EVT.Evento\" evt ON CED.\"CodigoTipoEvento\" = evt.\"CodigoTipoEvento\" WHERE \"Procesado\" = 'N'";

	Map<Long, String> template = new HashMap<Long, String>();

	try {
	    Connection con = connection;
	    try (PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {

		while (rs.next()) {
		    template.put(rs.getLong("IdConfiguracionEvento"), rs.getString("CodigoTipoEvento"));
		}
	    } catch (Exception e) {
		log.write(e.getMessage(), e);
	    }
	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	}

	return template;
    }

    public Map<Long, String> getTemplateRecordatory(Connection connection, ParametrosDefault params) {

	final String sql = "select distinct to_char(\"Template\") as \"Template\", cce.\"IdConfiguracionEvento\" from \"BSCL.Plataforma.BD.Vistas::EVT.ConsultarPlantillas\"  cp LEFT JOIN \"BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventos\" cce ON cce.\"IdPlantillaRecordatorio\" = cp.\"IdPlantilla\"  WHERE  cce.\"IdConfiguracionEvento\" is not null";
	Map<Long, String> template = new HashMap<Long, String>();

	try {
	    Connection con = connection;
	    try (PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {

		while (rs.next()) {
		    template.put(rs.getLong("IdConfiguracionEvento"), rs.getString("Template"));
		}
	    } catch (Exception e) {
		log.write(e.getMessage(), e);
	    }
	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	}

	return template;
    }

    public Map<String, String> getTagPredefinidos(Connection connection, ParametrosDefault params) {

	final String sql = "SELECT \"IdTagGenerales\",\"Llave\",\"ValorDefault\",'TagGenerales' FROM \"BSCL.Plataforma.BD.Vistas::EVT.ConsultarTagsGenerales\"";
	Map<String, String> tagGenerales = new HashMap<String, String>();
	try {
	    try (PreparedStatement pst = connection.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
			while (rs.next()) {
			    tagGenerales.put(rs.getString("Llave"), rs.getString("ValorDefault"));
			}
	    }
		//obtener logo corredora.
		final String sqlLogo = "SELECT TOP 1 \"IdImagen\", \"NombreImagen\", \"Tipo\", \"Extension\", \"Version\", \"Binario\" FROM BSCL.\"BSCL.Plataforma.BD::PLA.Imagenes\" WHERE \"NombreImagen\" = 'LOGO_CORREDOR_NOTIFICACION' AND \"IdEmpresa\" = ? ORDER BY \"Version\" DESC";
		try(PreparedStatement pst = connection.prepareStatement(sqlLogo);){
			pst.setInt(1, params.getIdEmpresa());
			try(ResultSet rs = pst.executeQuery()){
				while (rs.next()) {
					byte[] bytes = IOUtils.toByteArray(rs.getBinaryStream("Binario"));
					String file = Base64.getEncoder().encodeToString(bytes);
				    tagGenerales.put("LOGOCORREDOR", file);
				}
			}
		}
	
	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	}
	return tagGenerales;
    }

    public Map<String, Object> getKeyAndValue(Connection connection, ParametrosDefault params) {

		final String sql = "SELECT \"Agrupa\", ce.\"IdConfiguracionEvento\", cte.\"CodigoTipoEvento\",\"Llave\", CASE WHEN \"ValorBlob\" IS NOT NULL THEN TO_NCLOB(\"ValorBlob\") ELSE TO_NCLOB(\"Valor\") END AS \"Valor\",e.\"IdEvento\",\"IdPersonaDestinatario\"  FROM BSCL.\"BSCL.Plataforma.BD::EVT.Evento\" e LEFT JOIN \"BSCL.Plataforma.BD::EVT.EventoDetalle\" ed ON ed.\"IdEvento\" = e.\"IdEvento\" LEFT JOIN \"BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento\" cte ON cte.\"CodigoTipoEvento\" = e.\"CodigoTipoEvento\" LEFT JOIN \"BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle\" ced ON ced.\"CodigoTipoEvento\" = cte.\"CodigoTipoEvento\"  LEFT JOIN \"BSCL.Plataforma.BD::EVT.ConfiguracionEvento\" ce ON ce.\"IdConfiguracionEvento\" = ced.\"IdConfiguracionEvento\" WHERE e.\"Procesado\" = 'N' AND ed.\"IdEvento\" IS NOT NULL AND cte.\"CodigoTipoEvento\" IS NOT NULL AND ced.\"CodigoTipoEvento\" IS NOT NULL AND ce.\"IdConfiguracionEvento\" IS NOT NULL ORDER BY e.\"IdEvento\" DESC";
		Map<String, Object> cam = new HashMap<String, Object>();
		List<KeyValue> agrupados = new ArrayList<>();
		List<KeyValue> noagrupados = new ArrayList<>();
		log.write(sql);
		try {
		    Connection con = connection;
		    try (PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
			while (rs.next()) {
	
			    if (rs.getString("Agrupa").equals("S")) {
				KeyValue keyvalue = new KeyValue(rs.getString("CodigoTipoEvento"), rs.getString("Llave"), rs.getString("Valor"), rs.getInt("IdEvento"), rs.getInt("IdPersonaDestinatario"));
				agrupados.add(keyvalue);
			    } else {
				KeyValue keyvalue = new KeyValue(rs.getString("CodigoTipoEvento"), rs.getString("Llave"), rs.getString("Valor"), rs.getInt("IdEvento"), rs.getInt("IdPersonaDestinatario"));
				noagrupados.add(keyvalue);
			    }
			}
			cam.put("Agrupa", agrupados);
			cam.put("NoAgrupa", noagrupados);
		    } catch (Exception e) {
			log.write(e.getMessage(), e);
		    }
		} catch (Exception e) {
		    log.write(e.getMessage(), e);
		}
	
		return cam;
    }

    public boolean updateEvent(Connection conn, List<Long> idEventos, ParametrosDefault params) {

	String sql = "UPDATE \"BSCL.Plataforma.BD::EVT.Evento\" set \"Procesado\" = 'S' where \"IdEvento\" = ?";
	try {
	    Logger.getLogger(Connectors.class.getName()).log(Level.INFO, sql);
	    try (PreparedStatement pst = conn.prepareStatement(sql)) {
		for (Long eventos : idEventos) {
		    pst.setLong(1, eventos);
		    pst.addBatch();
		}
		pst.executeBatch();
		return true;
	    }

	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	}
	return false;
    }

    /**
     * @param connection
     * @param params
     * @param nameTable
     * @return
     * @throws SQLException
     */
    public boolean updateEvents(Connection connection, ParametrosDefault params, String nameTable) throws SQLException {
	String query = "update \"BSCL.Plataforma.BD::EVT.Mail\" set \"Procesado\" = ? where \"Procesado\" = ? ";
	try {
	    Connection con = connection;
	    Logger.getLogger(Connectors.class.getName()).log(Level.INFO, query);

	    try (PreparedStatement pst = con.prepareStatement(query)) {
		pst.setString(1, "S");
		pst.setString(2, "N");
		return pst.execute();
	    }

	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	}
	return false;
    }

    /**
     * @param connection
     * @param params
     * @param nameTable
     * @return
     * @throws SQLException
     */
    public boolean updateNotifications(Connection connection, ParametrosDefault params) throws SQLException {
	String query = String.format("{CALL %s.%s(?,?,?,?)}", getQuotedString(schema), getQuotedString(notificationsUpdate));
	try {
	    Connection con = connection;
	    Logger.getLogger(Connectors.class.getName()).log(Level.INFO, query);

	    try (CallableStatement cstm = con.prepareCall(query);) {
		cstm.setInt(1, params.getIdEmpresa());
		cstm.setInt(2, params.getIdUsuario());
		cstm.setString(3, params.getModulo());
		cstm.registerOutParameter(4, Types.NVARCHAR);
		return cstm.execute();

	    }

	} catch (Exception e) {
	    log.write(e.getMessage(), e);
	}
	return false;
    }

    public String getTemporaryName() {
	final Date now = new Date();
	final long time = now.getTime();
	return "#temp" + time;
    }

    public String getDateNowString(Connection connection) {
	try {
	    Connection con = connection;
	    try (Statement st = con.createStatement()) {
		try (ResultSet rs = st.executeQuery("select now() from dummy");) {
		    while (rs.next()) {

			return rs.getString(1);
		    }
		}
	    }
	} catch (SQLException e) {
	    log.write(e.getMessage(), e);
	}

	return null;
    }

    public int addProgramm(Programa programm, ParametrosDefault paramsDefault, Map<String, String> parameters, Connection connection) throws SQLException {

	final String nombreTabla = getTemporaryName();
	final String createTable = "CREATE LOCAL TEMPORARY TABLE " + nombreTabla + " (\"NombreParametro\" NVARCHAR(100), \"Valor\" NVARCHAR(100))";
	final String insertTable = "INSERT INTO " + nombreTabla + " values (?,?)";
	String query = String.format("{call %s.%s (?,?,?,?,?,?,%s,?,?,?,?,?,?)}", getQuotedString(schema), getQuotedString(addProgramm), nombreTabla);
	Logger.getLogger(Connectors.class.getName()).log(Level.INFO, query);
	Connection con = connection;

	try (PreparedStatement createTab = con.prepareStatement(createTable)) {

	    createTab.execute();
	    Logger.getLogger(Connectors.class.getName()).log(Level.INFO, createTable);

	}
	try (PreparedStatement insertTab = con.prepareStatement(insertTable)) {
	    Logger.getLogger(Connectors.class.getName()).log(Level.INFO, insertTable);
	    for (Entry<String, String> parametros : parameters.entrySet()) {
		insertTab.setString(1, parametros.getKey());
		insertTab.setString(2, parametros.getValue());
		insertTab.addBatch();
	    }
	    insertTab.executeBatch();
	}

	try (CallableStatement cstmt = con.prepareCall(query);) {
	    cstmt.setInt(1, paramsDefault.getIdEmpresa());
	    cstmt.setInt(2, paramsDefault.getIdUsuario());
	    cstmt.setString(3, paramsDefault.getModulo());
	    cstmt.setInt(4, 0);
	    cstmt.setString(5, programm.getIdTipoPrograma());
	    cstmt.setInt(6, programm.getFechaEjecucion());
	    cstmt.setString(7, programm.getCreaParametros());
	    cstmt.registerOutParameter(8, Types.BIGINT);
	    cstmt.registerOutParameter(9, Types.BIGINT);

	    if (cstmt.execute()) {
		try (ResultSet rs = cstmt.getResultSet()) {

		    while (rs.next()) {

			return Integer.parseInt(rs.getString("IdPrograma"));

		    }
		} catch (Exception e) {
		    log.write(e.getMessage(), e);
		}
	    }

	} catch (Exception e) {
	    // TODO: handle exception
	    log.write(e.getMessage(), e);
	}
	try (PreparedStatement anotherSt = con.prepareStatement("drop table " + nombreTabla);) {
	    try {
		anotherSt.execute();
	    } catch (SQLException e) {
		log.write("No existe tabla, no puedo dropear (NO GRAVE)");

	    }
	}

	return -1;

    }

    private String getQuotedString(final String name) {
	return "\"" + name + "\"";
    }

    public List<ArchivoAdjunto> getFile(String mensajecuerpo, int IdEmail) throws SQLException, IOException {
	List<ArchivoAdjunto> files = new ArrayList<ArchivoAdjunto>();
	try (Connection con = sapHanaConnection(); Statement st = con.createStatement();) {
	    try (ResultSet rs = st.executeQuery(" select \"Nombre\", \"Archivo\" from \"BSCL.Plataforma.BD::EVT.ArchivoAdjunto\" where \"IdEmail\" = " + IdEmail);) {
		while (rs.next()) {
		    files.add(FileGenerator.pdfGenerateFromHtml(mensajecuerpo, rs.getString("Nombre"), "", ""));
		    // ("C:\\", rs.getString("Nombre"), rs.getBlob("Archivo")));

		}
	    }
	}
	return files;
    }

    public List<Destinatario> getReceiver() throws SQLException, IOException {
	List<Destinatario> mails = new ArrayList<Destinatario>();
	try (Connection con = sapHanaConnection(); Statement st = con.createStatement();) {
	    try (ResultSet rs = st.executeQuery("select \"TipoDestinatario\", \"Destinatario\" from \"BSCL.Plataforma.BD::EVT.Destinatario\"");) {
		while (rs.next()) {
		    Destinatario c = new Destinatario();
		    c.setDestinatario(rs.getString("Destinatario"));
		    c.setTipoDestinatario(rs.getString("TipoDestinatario"));
		    mails.add(c);
		}
	    }
	}

	return mails;
    }

    public String getSubject() throws SQLException, IOException {
	try (Connection con = sapHanaConnection(); Statement st = con.createStatement();) {
	    try (ResultSet rs = st.executeQuery(" select \"Asunto\" from \"BSCL.Plataforma.BD::EVT.Mail\"");) {
		while (rs.next()) {
		    String subject = rs.getString(1);
		    return subject;
		}
	    }
	}

	return null;
    }

    public String getMessage() throws SQLException, IOException {
	try (Connection con = sapHanaConnection(); Statement st = con.createStatement();) {
	    try (ResultSet rs = st.executeQuery(" select \"MensajeCuerpo\" from \"BSCL.Plataforma.BD::EVT.Mail\"");) {
		while (rs.next()) {
		    String subject = rs.getString(1);
		    return subject;
		}
	    }
	}
	return null;
    }

    public List<Integer> getIdMails() throws SQLException {
	List<Integer> ids = null;
	try (Connection con = sapHanaConnection(); Statement st = con.createStatement();) {

	    try (ResultSet rs = st.executeQuery(" select \"IdEmail\" from \"BSCL.Plataforma.BD::EVT.Mail\" Where \"EstadoEnviado\" = 'N'");) {
		while (rs.next()) {

		    ids.add(rs.getInt("IdEmail"));
		    return ids;
		}
	    }
	}
	return null;
    }

    public boolean updateEmail(List<Integer> IdEmail, Connection connection) throws SQLException {
	final String query = "  UPDATE \"BSCL.Plataforma.BD::EVT.Mail\" SET \"EstadoEnviado\" = ? ,  \"FechaEnviado\" = now() Where \"IdEmail\" = ?";
	Connection con = connection;
	try (PreparedStatement st = con.prepareStatement(query);) {
	    Logger.getLogger(Connectors.class.getName()).log(Level.INFO, query);

	    for (int idmail : IdEmail) {
		st.setString(1, "S");
		st.setInt(2, idmail);
		st.addBatch();
	    }

	    st.executeBatch();
	    return true;
	} catch (SQLException e) {
	    log.write(e.getMessage(), e);
	    return false;
	}
    }
    
    public Set<Long> obtenerIdsNotificacionesReenviar(Connection conn){
    	Set<Long> notificaciones = new HashSet<>();
    	final String query = "SELECT \"IdNotificacion\" FROM \"BSCL.Plataforma.BD::EVT.Notificacion\" WHERE \"ReEnviar\" = 'S' AND \"ReEnviado\" = 'N';";
    	try(Statement st = conn.createStatement();){
    		try(ResultSet rs = st.executeQuery(query)){
    			while(rs.next()) {
    				notificaciones.add(rs.getLong("IdNotificacion"));
    			}
    		}
    	} catch (SQLException e) {
			e.printStackTrace();
		}
    	return notificaciones;
    }

}