package cl.bcs.plataforma.evt;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AgregarEmail implements JavaCall {
    // SQL's
    private final String SQL_ObtenerIdEmail = loadResourceToString("cl/bcs/plataforma/evt/sql/ObtenerIdEmail.sql");
    private final String SQL_InsertaEmail = loadResourceToString("cl/bcs/plataforma/evt/sql/InsertaEmail.sql");
    private final String SQL_InsertaDestinatario = loadResourceToString("cl/bcs/plataforma/evt/sql/InsertaDestinatario.sql");
    private final String SQL_InsertaArchivoAdjunto = loadResourceToString("cl/bcs/plataforma/evt/sql/InsertaArchivoAdjunto.sql");
    private final Logger log = LogManager.getLogger(AgregarEmail.class);
    
    public static class Email extends EmailInterna {
        public Long IdEnvio;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
    }

    public static class EmailInterna {
        public Long IdEmail;
        public String Remitente;
        public String Asunto;
        public String MensajeCuerpo;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
    }

    public static class Destinatario {
        public Long IdEnvio;
        public String Destinatario;
        public String TipoDestinatario;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
    }

    public static class ArchivoAdjunto {
        public Long IdEnvio;
        public byte[] Archivo;
        public String Nombre;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
    }

    /* END POJO */
    public static class ServiceEntrada {
    	public List<Email> emails;
    	public List<Destinatario> destinatarios;
    	public List<ArchivoAdjunto> archivosAdjuntos;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
    }

    public static class ServiceSalida {
        public List<?> P_OUT_IDEVENTO;
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = getGsonBuilder();
        ServiceEntrada se = gson.fromJson(in.toString(), ServiceEntrada.class);
        ServiceSalida out = logica(conn, se);
        JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
        return dataout;
    }

    public ServiceSalida logica(Connection conn, ServiceEntrada in) throws Exception {
    	log.info("INICIO");
        SQL sql = new SQL();
        ServiceSalida out = new ServiceSalida();
        if (in != null) {
            int cantidadEmails = in.emails.size();
            List<Email> listaEvento = null;
            HashMap<Long, Long> idNotificacionEmail = new HashMap<Long, Long>();
            if (cantidadEmails > 0) {
                // obtener un id unico para cada email
                HashMap<String, Object> parmsEvts = new HashMap<String, Object>();
                parmsEvts.put("cantidadEmails", cantidadEmails);
                listaEvento = sql.query(conn, SQL_ObtenerIdEmail, parmsEvts, Email.class);
                // asignar a cada email el id unico y prepararlo para el insert
                List<EmailInterna> insertEmails = new ArrayList<>();
                for (int i = 0; i < listaEvento.size(); i++) {
                    in.emails.get(i).IdEmail = listaEvento.get(i).IdEmail;
                    insertEmails.add(in.emails.get(i));
                    // guardar asociacion de idEnvio-idEmail para destinatarios
                    // y archivos adjuntos
                    idNotificacionEmail.put(in.emails.get(i).IdEnvio, in.emails.get(i).IdEmail);
                }
                // insertar todos los emails nuevos
                log.debug("Se ingresaran: "+insertEmails.size()+",con: "+ in.destinatarios+" destinatarios.");
                int[] resEmails = sql.batch(conn, SQL_InsertaEmail, insertEmails, EmailInterna.class);
                out.P_OUT_IDEVENTO = Arrays.asList(resEmails);
                // ingresar en destinatarios
                if (in.destinatarios != null && in.destinatarios.size() > 0) {
                    for (Destinatario destinatario : in.destinatarios) {
                        destinatario.IdEnvio = idNotificacionEmail.get(destinatario.IdEnvio);
                    }
                    int[] resDestinatarios = sql.batch(conn, SQL_InsertaDestinatario, in.destinatarios, Destinatario.class);
                    for (int i = 0; i < resDestinatarios.length; i++) {
                        if(resDestinatarios[i]!=1){
                            log.debug("Ocurrio un error al ingresar un destinatario para email");
                        }
                    }
                }
                // ingresar en archivos adjuntos.
                List<ArchivoAdjunto> lista = new ArrayList<>();
                int parametroTanda = 0;
                try {
                	Long idEmpresa = 1L;
        			parametroTanda = Integer.parseInt(ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.ValorTandaArchivoAdjunto"));
        		} catch (Exception e) {
        			parametroTanda = 30;
        		}
                
                if (in.archivosAdjuntos != null && in.archivosAdjuntos.size() > 0) {
                    for (ArchivoAdjunto archivoAdjunto : in.archivosAdjuntos) {
                       if(idNotificacionEmail.get(archivoAdjunto.IdEnvio) != null) {
                    	   archivoAdjunto.IdEnvio = idNotificacionEmail.get(archivoAdjunto.IdEnvio);
                    	   lista.add(archivoAdjunto);
                           if(lista.size() == parametroTanda) {
                        	   sql.batch(conn, SQL_InsertaArchivoAdjunto, lista, ArchivoAdjunto.class);
                        	   lista.clear();
                           }
                       }
                       
                       else {
                    	   log.debug("este no se enviara:" + archivoAdjunto.IdEnvio);
                       }   
                    }
                    
                    if(!lista.isEmpty()) {
                    	sql.batch(conn, SQL_InsertaArchivoAdjunto, lista, ArchivoAdjunto.class);
                    }                    
                }
            }
        }
        log.debug("TERMINO");
        return out;
    }

}
