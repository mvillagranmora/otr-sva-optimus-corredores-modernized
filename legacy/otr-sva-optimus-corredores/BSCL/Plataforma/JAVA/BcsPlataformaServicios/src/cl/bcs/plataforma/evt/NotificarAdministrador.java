package cl.bcs.plataforma.evt;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.evt.AgregarEmail.Destinatario;
import cl.bcs.plataforma.evt.AgregarEmail.Email;
import cl.bcs.plataforma.evt.AgregarEmail.ServiceEntrada;
import cl.bcs.plataforma.evt.AgregarEvento.ObjARCHIVOSADJUNTOS;
import cl.bcs.plataforma.evt.AgregarEvento.ObjEVENTOS;
import cl.bcs.plataforma.evt.AgregarEvento.ObjIDDIRECCIONES;
import cl.bcs.plataforma.evt.AgregarEvento.ObjVALORES;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class NotificarAdministrador implements JavaCall {
  private final Log log = new Log("EVT.EMAIL_ADMIN");
  private final String SQL_ObtenerDestinatariosNotificacionAdministrativa =
      loadResourceToString(
          "cl/bcs/plataforma/evt/sql/ObtenerDestinatariosNotificacionAdministrativa.sql");

  public static class NotificarAdministradorEntrada {
    public long IdEmpresa;
    public String NombreProceso;
    public String Mensaje;
    public String EsError;
  }

  public static class NotificarAdministradorSalida {
    public boolean resultadoEmailAdministrador;
    public boolean resultadoEnvioEventoNotificacion;
  }

  public static class DestinatarioEventoNotificacion {
    public String CodigoTipoEvento;
    public long IdSecuencia;
    public Timestamp FechaCreacionSistema;
    public long IdPersonaRemitente;
    public long IdPersonaDestinatario;
    public long IdPortafolio;
    public long IdDireccion;
    public String TipoDestinatario;
  }

  @Override
  public JsonObject call(Connection conn, JsonObject in) throws Exception {
    Gson gson = getGsonBuilder();
    NotificarAdministradorEntrada se =
        gson.fromJson(in.toString(), NotificarAdministradorEntrada.class);
    NotificarAdministradorSalida out = logica(conn, se);
    JsonParser parser = new JsonParser();
    JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
    return dataout;
  }

  public NotificarAdministradorSalida logica(Connection conn, NotificarAdministradorEntrada in)
      throws Exception {
    NotificarAdministradorSalida out = new NotificarAdministradorSalida();

    try {
      out.resultadoEmailAdministrador = enviarEmailAdministradores(conn, in);
    } catch (Exception ex) {
      log.write("Error Enviar Email Administrador: " + ex.getMessage(), ex);
      out.resultadoEmailAdministrador = false;
    }

    try {
      out.resultadoEnvioEventoNotificacion = enviarEmailEventoNotificacion(conn, in);
    } catch (Exception ex) {
      ex.printStackTrace();
      out.resultadoEnvioEventoNotificacion = false;
      log.write("Error Enviar Evento Notificacion Administrador: " + ex.getMessage(), ex);
    }

    return out;
  }

  private boolean enviarEmailAdministradores(Connection conn, NotificarAdministradorEntrada in)
      throws Exception {
    //obtener los destinatarios PARA desde la tabla PLA.Configuracion 
    List<String> emailAdministracionTo =
            Arrays.asList(
            		Utils.getConfiguracion(
                        conn, Utils.getSchema(), in.IdEmpresa, "EVT.EmailAdministracionTO")
                    .toLowerCase()
                    .split(";"));
    
    //obtener los destinatarios CONCOPIA desde la tabla PLA.Configuracion
    List<String> emailAdministracionCc =
        Arrays.asList(
            Utils.getConfiguracion(
                    conn, Utils.getSchema(), in.IdEmpresa, "EVT.EmailAdministracionCC")
                .toLowerCase()
                .split(";"));

    //parse template notificaciones
    String templateNotificacion =
        (Utils.getConfiguracion(
                    conn, Utils.getSchema(), in.IdEmpresa, "EVT.TemplateEmailAdministracion")
                .replace("${data.NOTIFICACION_ADMINISTRADOR.Proceso}", in.NombreProceso))
            .replace("${data.NOTIFICACION_ADMINISTRADOR.Mensaje}", in.Mensaje);
    
    
    //Informacion para el asunto
    String asuntoEmail = Utils.getConfiguracion(conn, Utils.getSchema(), in.IdEmpresa, "EVT.AsuntoEmailAdministracion");
    
    AgregarEmail agregarEmail = new AgregarEmail();
    ServiceEntrada entradaAgregarEmail = new ServiceEntrada();

    List<Email> emails = new ArrayList<Email>();
    //Estructura basica del correo.
    Email email = new Email();
    email.IdEnvio = Long.parseLong("0");
    email.Asunto = ((in.EsError!=null && in.EsError.equals("S"))?"[ERROR]":"") + asuntoEmail;
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
    //obtener destinatarios CONCOPIA
    for (String destinatario : emailAdministracionCc) {
      Destinatario dest = new Destinatario();
      dest.IdEnvio = Long.parseLong("0");
      dest.Destinatario = destinatario;
      dest.TipoDestinatario = "CC";
      destinatarios.add(dest);
    }

    //agregar emails para el envio
    entradaAgregarEmail.emails = emails;
    entradaAgregarEmail.destinatarios = destinatarios;
    agregarEmail.logica(conn, entradaAgregarEmail);
    return true;
  }

  private boolean enviarEmailEventoNotificacion(Connection conn, NotificarAdministradorEntrada in)
      throws Exception {
    SQL sql = new SQL();
    //Documentos de identidad para construir la notificacion
    List<String> documentosIdentidad =
        Arrays.asList(
            Utils.getConfiguracion(
                    conn, Utils.getSchema(), in.IdEmpresa, "EVT.DestinatariosEmailAdministracion")
                .toLowerCase()
                .split(";"));
    if (documentosIdentidad.size() > 0) {

      List<DestinatarioEventoNotificacion> destinatariosEvento = new ArrayList<>();
      /*buscar forma optima*/
      for (String documento : documentosIdentidad) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("DocumentoIdentidad", documento);
        List<DestinatarioEventoNotificacion> dests =
            sql.query(
                conn,
                SQL_ObtenerDestinatariosNotificacionAdministrativa,
                params,
                DestinatarioEventoNotificacion.class);
        if (dests != null && dests.isEmpty() == false) {
          destinatariosEvento.add(dests.get(0));
        }
      }
      
      if (destinatariosEvento.size() > 0) {
        //Instanciar objetos necesarios para ingresar un evento de notificacion
        cl.bcs.plataforma.evt.AgregarEvento.ServiceEntrada agregarEventoEntrada =
            new cl.bcs.plataforma.evt.AgregarEvento.ServiceEntrada();
        agregarEventoEntrada.P_IDEMPRESA = in.IdEmpresa;
        agregarEventoEntrada.P_IDMODULO = "PLA";
        agregarEventoEntrada.P_IDUSUARIO = Long.parseLong("0");

        List<ObjEVENTOS> eventos = new ArrayList<>();
        List<ObjVALORES> valores = new ArrayList<>();
        List<ObjARCHIVOSADJUNTOS> archivosAdjuntos = new ArrayList<>();
        List<ObjIDDIRECCIONES> direcciones = new ArrayList<>();

        for (DestinatarioEventoNotificacion destinatarioEvento : destinatariosEvento) {
          ObjEVENTOS evento = new ObjEVENTOS();
          evento.IdSecuencia = destinatarioEvento.IdSecuencia;
          evento.CodigoTipoEvento = destinatarioEvento.CodigoTipoEvento;
          evento.FechaCreacionSistema = Fecha.toStringDate(destinatarioEvento.FechaCreacionSistema);
          evento.IdPersonaRemitente = destinatarioEvento.IdPersonaRemitente;
          evento.IdPersonaDestinatario = destinatarioEvento.IdPersonaDestinatario;
          evento.IdPortafolio = destinatarioEvento.IdPortafolio;
          eventos.add(evento);

          ObjVALORES valor = new ObjVALORES();
          valor.IdSecuencia = destinatarioEvento.IdSecuencia;
          valor.Llave = "Proceso";
          valor.Valor = in.NombreProceso;
          valores.add(valor);
          valor = new ObjVALORES();
          valor.IdSecuencia = destinatarioEvento.IdSecuencia;
          valor.Llave = "Mensaje";
          valor.Valor = in.Mensaje;
          valores.add(valor);

          ObjIDDIRECCIONES direccion = new ObjIDDIRECCIONES();
          direccion.IdSecuencia = destinatarioEvento.IdSecuencia;
          direccion.IdDireccion = destinatarioEvento.IdDireccion;
          direccion.TipoDestinatario = destinatarioEvento.TipoDestinatario;
          direcciones.add(direccion);
        }

        agregarEventoEntrada.P_EVENTOS = eventos;
        agregarEventoEntrada.P_VALORES = valores;
        agregarEventoEntrada.P_IDDIRECCIONES = direcciones;
        agregarEventoEntrada.P_ARCHIVOSADJUNTOS = archivosAdjuntos;

        //agregar eventos de notificacion.
        AgregarEvento servicio = new AgregarEvento();
        servicio.logica(conn, agregarEventoEntrada);
      } else {
        log.write("No se encuentran socios de negocio asociados a los documentos de identidad registrados en la variable EVT.DestinatariosEmailAdministracion");
      }
    } else {
      log.write("No se encuentran configurados documentos de identidad en la variable EVT.DestinatariosEmailAdministracion");
    }
    return true;
  }

  public static void main(String[] args) {
    //Settings by file
    String dbURL = "jdbc:sap://161.238.34.246:30215/?";
    String dbprop = "currentSchema=BSCL;";
    String dbdrv = "com.sap.db.jdbc.Driver";
    String dbqry = "select 1 from dummy";
    String user = "SOPORTE";
    String pass = "Bolsa123";
    //Datasource
    BasicDataSource ds = new BasicDataSource();
    ds.setDriverClassName(dbdrv);
    ds.setUsername(user);
    ds.setPassword(pass);
    ds.setUrl(dbURL);
    ds.setValidationQuery(dbqry);
    ds.setConnectionProperties(dbprop);
    ds.setDefaultAutoCommit(false);

    //Example
    System.out.println("== START ==");
    try {
      Connection con = ds.getConnection();
      con.setAutoCommit(false);
      SQL.exec(con, "set schema BSCL");
      SQL.exec(con, "SET TRANSACTION AUTOCOMMIT DDL OFF");
      //---------------------------------
      try {
        JsonParser parser = new JsonParser();
        JsonObject dataJsonIn =
            parser
                .parse(
                    "{\"IdEmpresa\":1,\"NombreProceso\":\"POR_EJEMPLO_PAUTA\", \"Mensaje\":\"Test job despacho de correos.\"}")
                .getAsJsonObject();
        NotificarAdministrador demo = new NotificarAdministrador();
        System.out.println(demo.call(con, dataJsonIn).toString());
        con.commit();
      } catch (Exception er) {
        er.printStackTrace();
        con.rollback();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("== END ==");
  }
}
