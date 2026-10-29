package cl.bcs.socionegocio.per;

import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.SalidaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.ModificarPersonaGrupoDireccionElectronicaMasivo.GrupoDireccionElectronica;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ConfirmarModificarPersonaGrupoDireccionElectronicaMasivo implements JavaCall, ConfirmarEjecutarCambiosPersona {

    static final Logger logger = LogManager.getLogger(ConfirmarModificarPersonaGrupoDireccionElectronicaMasivo.class);

    private String SQL_UPDATE_DATOS_GRUPO_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaGrupoDireccionElectronicaMasivo_UpdateDatosGrupoDireccionElectronica.sql");

    private final String TIPO_LOG = "MODIFICACION";
    private final String ID_TIPO_ENTIDAD = "GRUPODIRECCIONELECTRONICA";
    private final String NOMBRE_PROCESO_LOG = "LOG_NOMBRE_PROCESO_MODIFICAR_GRUPO_DIRECCION_ELECTRONICA";

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaConfirmarEjecutarCambiosPersona jin = gson.fromJson(in.toString(), EntradaConfirmarEjecutarCambiosPersona.class);
        SalidaConfirmarEjecutarCambiosPersona out = logica(conn, jin);
        JsonParser parser = new JsonParser();
        return parser.parse(gson.toJson(out)).getAsJsonObject();
    }

    @Override
    public SalidaConfirmarEjecutarCambiosPersona logica(Connection con, EntradaConfirmarEjecutarCambiosPersona entrada) throws Exception {
        logger.info("INICIO::ConfirmarModificarPersonaGrupoDireccionElectronicaMasivo");
        long inicioServicio = System.currentTimeMillis();
        SalidaConfirmarEjecutarCambiosPersona out = new SalidaConfirmarEjecutarCambiosPersona();

        if (entrada.p_Entidad.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FALTA_ESPECIFICAR_GRUPO_DIRECCION_ELECTRONICA", null, con, entrada.p_IdEmpresa));
            return out;
        }

        ObtenerSolicitudesDeCambiosPersonaMasivo obtenerSolicitudesDeCambios = new ObtenerSolicitudesDeCambiosPersonaMasivo();
        List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios.logica(con, entrada, ID_TIPO_ENTIDAD);

        if (solicitudesCambios.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "SOLICITUD_NO_EXISTE", null, con, entrada.p_IdEmpresa));
            return out;
        }

        List<GrupoDireccionElectronica> datosEntidad = obtenerEntidades(solicitudesCambios);

        modificarCampoGruposDireccionesElectronicas(con, datosEntidad);

        ModificarEstadoRegistroSolicitudCambioPersona modificarEstadoRegistroSolicitudCambioPersona = new ModificarEstadoRegistroSolicitudCambioPersona();
        SalidaModificarEstadoRegistroSolicitudCambio salidaModificarEstadoRegistroSolicitudCambio = modificarEstadoRegistroSolicitudCambioPersona.logica(con, entrada, solicitudesCambios);

        out.P_OUT_ERRORESCONTADOR += salidaModificarEstadoRegistroSolicitudCambio.P_OUT_ERRORESCONTADOR;
        out.P_OUT_MENSAJES.addAll(salidaModificarEstadoRegistroSolicitudCambio.P_OUT_MENSAJES);

        AgregarLogPersona agregarLogPersona = new AgregarLogPersona();
        agregarLogPersona.agregarLog(con, entrada, solicitudesCambios, ID_TIPO_ENTIDAD, TIPO_LOG, NOMBRE_PROCESO_LOG);

        long finServicio = System.currentTimeMillis();
        logger.info("FIN::ConfirmarModificarPersonaGrupoDireccionElectronicaMasivo", finServicio - inicioServicio);
        return out;
    }

    private List<GrupoDireccionElectronica> obtenerEntidades(List<SolicitudCambio> solicitudesCambios) throws Exception {
        logger.info("INICIO::ConfirmarModificarPersonaGrupoDireccionElectronicaMasivo.obtenerEntidades");
        long inicio = System.currentTimeMillis();

        Map<Long, List<SolicitudCambio>> solicitudesPorIdEntidad = solicitudesCambios.parallelStream().collect(Collectors.groupingBy(solicitudCambio -> solicitudCambio.IdEntidadOrigenLong));

        List<GrupoDireccionElectronica> datosAActualizar = new ArrayList<>();

        for (Entry<Long, List<SolicitudCambio>> persona : solicitudesPorIdEntidad.entrySet()) {
            GrupoDireccionElectronica datosEntidad = new GrupoDireccionElectronica();

            List<SolicitudCambio> cambiosEntidad = persona.getValue();
            datosEntidad.IdGrupoDireccion = persona.getKey();

            for (SolicitudCambio solicitudCambio : cambiosEntidad) {
                Field campo = datosEntidad.getClass().getField(solicitudCambio.CampoModificado);
                if (campo.getType().isAssignableFrom(String.class)) {
                    campo.set(datosEntidad, solicitudCambio.ValorNuevo);
                }
                if (campo.getType().isAssignableFrom(Long.class)) {
                    campo.set(datosEntidad, Long.parseLong(solicitudCambio.ValorNuevo));
                }
                if (campo.getType().isAssignableFrom(java.sql.Timestamp.class)) {
                    campo.set(datosEntidad, Fecha.toDateTime(solicitudCambio.ValorNuevo));
                }
                if (campo.getType().isAssignableFrom(java.sql.Date.class)) {
                    campo.set(datosEntidad, Fecha.toDate(solicitudCambio.ValorNuevo));
                }
            }
            datosAActualizar.add(datosEntidad);
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConfirmarModificarPersonaGrupoDireccionElectronicaMasivo.obtenerEntidades", fin - inicio);

        return datosAActualizar;
    }

    private void modificarCampoGruposDireccionesElectronicas(Connection con, List<GrupoDireccionElectronica> entidades) throws Exception {
        logger.debug("INICIO::ConfirmarModificarPersonaGrupoDireccionElectronicaMasivo.modificarCampoGruposDireccionesElectronicas");
        long inicio = System.currentTimeMillis();

        if (entidades.size() > 0) {
            SQL sql = new SQL();
            sql.batch(con, SQL_UPDATE_DATOS_GRUPO_DIRECCION_ELECTRONICA, entidades, GrupoDireccionElectronica.class);
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConfirmarModificarPersonaGrupoDireccionElectronicaMasivo.modificarCampoGruposDireccionesElectronicas", fin - inicio);
    }


    public static void main(String[] args) throws SQLException {
        System.out.println("== TEST ==");

        try (Connection con = PlataformaUtils.getDESAConnection()) {
            Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
            String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdEstadoAutorizacion\":\"AUT\",\"p_Entidad\":[{\"IdPersona\":621869,\"IdEntidad\":10023}]}";
            EntradaConfirmarEjecutarCambiosPersona Objin = gson.fromJson(json, EntradaConfirmarEjecutarCambiosPersona.class);

            ConfirmarModificarPersonaGrupoDireccionElectronicaMasivo demo = new ConfirmarModificarPersonaGrupoDireccionElectronicaMasivo();
            System.out.println(Objin);

            try {
                SalidaConfirmarEjecutarCambiosPersona ObjOut = demo.logica(con, Objin);
                System.out.println(ObjOut);

            } catch (Exception er) {
                er.printStackTrace();

            } finally {
                con.rollback();
                con.close();
            }
        }
        System.out.println("== END ==");

    }
}
