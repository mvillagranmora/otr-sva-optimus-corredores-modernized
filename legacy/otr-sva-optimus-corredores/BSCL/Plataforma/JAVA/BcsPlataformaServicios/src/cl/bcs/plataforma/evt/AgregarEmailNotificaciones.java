package cl.bcs.plataforma.evt;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.evt.AgregarEmail.ArchivoAdjunto;
import cl.bcs.plataforma.evt.AgregarEmail.Destinatario;
import cl.bcs.plataforma.evt.AgregarEmail.Email;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class AgregarEmailNotificaciones implements JavaCall {
    // SQL's
    // private final String SQL_ObtenerIdEmail =
    // loadResourceToString("cl/bcs/plataforma/evt/sql/ObtenerIdEmail.sql");
    private final String SQL_ObtenerEmailNotificaciones = loadResourceToString("cl/bcs/plataforma/evt/sql/ObtenerEmailNotificaciones.sql");
    private final String SQL_ObtenerDestinatariosNotificaciones = loadResourceToString("cl/bcs/plataforma/evt/sql/ObtenerDestinatariosNotificaciones.sql");
    private final String SQL_ObtenerArchivosAdjuntosNotificaciones = loadResourceToString("cl/bcs/plataforma/evt/sql/ObtenerArchivosAdjuntosNotificaciones.sql");
    private final String SQL_ActualizarEstadoNotificaciones = loadResourceToString("cl/bcs/plataforma/evt/sql/ActualizarEstadoNotificaciones.sql");
    private final String SQL_ActualizarEstadoNotificacionDetalle = loadResourceToString("cl/bcs/plataforma/evt/sql/ActualizarEstadoNotificacionDetalle.sql");
    private final Logger log = LogManager.getLogger(AgregarEmailNotificaciones.class);

    public static class Notificacion {
        public long IdEnvio;
        public String MensajeCuerpo;
        public String Remitente;
        public String Asunto;
        
        public Notificacion(long idEnvio) {
            super();
            IdEnvio = idEnvio;
        }
        public Notificacion(long idEnvio, String mensajeCuerpo, String remitente, String asunto) {
            super();
            IdEnvio = idEnvio;
            MensajeCuerpo = mensajeCuerpo;
            Remitente = remitente;
            Asunto = asunto;
        }
        @Override
        public String toString() {
            return "Notificacion [IdEnvio=" + IdEnvio + ", Remitente=" + Remitente + ", Asunto=" + Asunto + "]";
        }
    }

    /* END POJO */
    public static class ServiceEntrada {
        public long idEmpresa;
        public List<Long> notificaciones;
    }

    public static class ServiceSalida {
        public List<?> P_OUT_IDEVENTO;
        @Override
        public String toString() {
            return "ServiceSalida [P_OUT_IDEVENTO=" + P_OUT_IDEVENTO + "]";
        }
    }

    @Override
    public JsonObject call(Connection con, JsonObject jin) throws Exception {
        Gson gson = getGsonBuilder();
        ServiceEntrada in = gson.fromJson(jin.toString(), ServiceEntrada.class);
        ServiceSalida out = logica(con, in);
        JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
        return dataout;
    }

    public ServiceSalida logica(Connection conn, ServiceEntrada in) throws Exception {
    	log.info("INICIO");
        SQL sql = new SQL();
        ServiceSalida out = new ServiceSalida();
        // logica, obtener correos
        try{
	        HashMap<String, Object> params = new HashMap<>();
	        params.put("Notificaciones", in.notificaciones);
	        List<Email> emails = sql.query(conn, SQL_ObtenerEmailNotificaciones, params, Email.class);
	        log.debug("Se han construido: "+emails.size()+" emails.");
	        
	        // obtener otros destinatarios de la notificacion (CC, CCO, ETC)
	        List<Destinatario> destinatarios = sql.query(conn, SQL_ObtenerDestinatariosNotificaciones, params, Destinatario.class);
	        // obtener archivos adjuntos
	        List<ArchivoAdjunto> archivosAdjuntos = sql.query(conn, SQL_ObtenerArchivosAdjuntosNotificaciones, params, ArchivoAdjunto.class);
	        
	        // insertar correos nuevos mediante el servicio AgregarEmail.
	        AgregarEmail agregarEmail = new AgregarEmail();
	        AgregarEmail.ServiceEntrada entradaServicio = new AgregarEmail.ServiceEntrada();
	        entradaServicio.emails = emails;
	        entradaServicio.destinatarios = destinatarios;
	        entradaServicio.archivosAdjuntos = archivosAdjuntos;
	        AgregarEmail.ServiceSalida salidaServicio = agregarEmail.logica(conn, entradaServicio);
	        out.P_OUT_IDEVENTO = salidaServicio.P_OUT_IDEVENTO;
        }catch(Exception e){
        	out.P_OUT_IDEVENTO = new ArrayList();
        }
       //actualizar notificaciones procesadas y sus detalles
        List<Notificacion> notificaciones = new ArrayList<Notificacion>();
        for (Long idNotificacion : in.notificaciones) {
            notificaciones.add(new Notificacion(idNotificacion));
        }
        sql.batch(conn, SQL_ActualizarEstadoNotificaciones, notificaciones, Notificacion.class);
        sql.batch(conn, SQL_ActualizarEstadoNotificacionDetalle, notificaciones, Notificacion.class);
        log.info("TERMINO");
        return out;
    }

}
