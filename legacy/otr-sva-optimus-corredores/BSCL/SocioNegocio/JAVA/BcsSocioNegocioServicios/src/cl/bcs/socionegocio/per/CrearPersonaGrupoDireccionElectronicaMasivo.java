package cl.bcs.socionegocio.per;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.socionegocio.per.pojo.GrupoDireccionElectronica;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CrearPersonaGrupoDireccionElectronicaMasivo implements JavaCall {

    static final Logger logger = LogManager.getLogger(CrearPersonaGrupoDireccionElectronicaMasivo.class);

    public final String SQL_INSERT_GRUPO_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearGrupoDireccionElectronicaMasivo_InsertGrupoDireccionElectronica.sql");

    public final String ID_CONCEPTO = "NUGDIE";

    public static class EntradaCrearPersonaGrupoDireccionElectronicaMasivo extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;

        public List<GrupoDireccionElectronica> p_GruposDireccionesElectronicas;

        public EntradaCrearPersonaGrupoDireccionElectronicaMasivo() {
            this.p_GruposDireccionesElectronicas = new ArrayList<>();
        }
    }

    public static class SalidaCrearPersonaGrupoDireccionElectronicaMasivo extends BcsPojo {
        public Long P_OUT_ERRORESCONTADOR;
        public List<MensajeSalida> P_OUT_MENSAJES;
        public List<Entidad> P_OUT_REFID;

        public SalidaCrearPersonaGrupoDireccionElectronicaMasivo() {
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_REFID = new ArrayList<>();
            this.P_OUT_MENSAJES = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaCrearPersonaGrupoDireccionElectronicaMasivo jin = gson.fromJson(in.toString(), EntradaCrearPersonaGrupoDireccionElectronicaMasivo.class);

        SalidaCrearPersonaGrupoDireccionElectronicaMasivo out = logica(conn, jin);
        JsonParser parser = new JsonParser();

        return parser.parse(gson.toJson(out)).getAsJsonObject();
    }

    public SalidaCrearPersonaGrupoDireccionElectronicaMasivo logica(Connection conn, EntradaCrearPersonaGrupoDireccionElectronicaMasivo entrada) throws Exception {
        logger.info("INICIO::CrearPersonaGrupoDireccionElectronicaMasivo");
        long inicio = System.currentTimeMillis();

        SalidaCrearPersonaGrupoDireccionElectronicaMasivo salida = new SalidaCrearPersonaGrupoDireccionElectronicaMasivo();
        Boolean requiereAutorizarion = SocioNegocioUtils.esOperacionConAutorizacion(conn, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);
        List<GrupoDireccionElectronica> gruposDireccionesElectronicas = obtenerGruposDireccionesElectronicas(conn, entrada, requiereAutorizarion);
        insertarGrupoDireccionElectronica(conn, gruposDireccionesElectronicas);

        if (requiereAutorizarion) {
            salida = crearEventoAutorizacion(conn, entrada, gruposDireccionesElectronicas);
        } else {
            agregarLog(conn, entrada, gruposDireccionesElectronicas);
        }

        salida.P_OUT_REFID = obtenerEntidadesInsertadas(gruposDireccionesElectronicas);

        long fin = System.currentTimeMillis();
        logger.info("FIN::CrearPersonaGrupoDireccionElectronicaMasivo", fin - inicio);

        return salida;
    }

    private List<GrupoDireccionElectronica> obtenerGruposDireccionesElectronicas(Connection conn, EntradaCrearPersonaGrupoDireccionElectronicaMasivo entrada, Boolean requiereAutorizacion) throws Exception {
        logger.trace("INICIO::CrearPersonaGrupoDireccionElectronicaMasivo.obtenerGruposDireccionesElectronicas");
        long inicio = System.currentTimeMillis();

        List<Long> idsGruposDireccionesElectronicas = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::GrupoDireccion", Long.valueOf(entrada.p_GruposDireccionesElectronicas.size()));
        String fechaSistemaUTC = FechaDB.ObtenerFechaHoraSistemaUTC(conn, entrada.p_IdEmpresa);
        AtomicInteger index = new AtomicInteger();

        List<GrupoDireccionElectronica> gruposDireccionesElectronicas = entrada.p_GruposDireccionesElectronicas
                .stream()
                .map(entradaGrupoDireccionElectronica -> {
                    Long idGrupoDireccionElectronica = idsGruposDireccionesElectronicas.get(index.getAndIncrement());

                    return obtenerEntidadGrupoDireccionElectronica(entradaGrupoDireccionElectronica, entrada.p_IdUsuario, idGrupoDireccionElectronica, fechaSistemaUTC, requiereAutorizacion);
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("FIN::CrearPersonaGrupoDireccionElectronicaMasivo.obtenerGruposDireccionesElectronicas", fin - inicio);

        return gruposDireccionesElectronicas;
    }

    private GrupoDireccionElectronica obtenerEntidadGrupoDireccionElectronica(GrupoDireccionElectronica entradaGrupoDireccionElectroncia, Long idUsuario, Long idGrupoDireccionElectronica, String fechaSistemaUTC, Boolean requiereAutorizacion) {
        logger.trace("INICIO::CrearPersonaGrupoDireccionElectronicaMasivo.obtenerEntidadGrupoDireccionElectronica");
        long inicio = System.currentTimeMillis();

        GrupoDireccionElectronica grupoDireccionElectronica = new GrupoDireccionElectronica();
        grupoDireccionElectronica.IdInterno = entradaGrupoDireccionElectroncia.IdInterno;
        grupoDireccionElectronica.IdGrupoDireccion = idGrupoDireccionElectronica;
        grupoDireccionElectronica.Grupo = entradaGrupoDireccionElectroncia.Grupo;
        grupoDireccionElectronica.Comentario = entradaGrupoDireccionElectroncia.Comentario;
        grupoDireccionElectronica.IdPersona = entradaGrupoDireccionElectroncia.IdPersona;
        grupoDireccionElectronica.FechaCreacion = Fecha.toTimeStamp(fechaSistemaUTC);
        grupoDireccionElectronica.FechaModificacion = Fecha.toTimeStamp(fechaSistemaUTC);
        grupoDireccionElectronica.FechaIngreso = Fecha.toTimeStamp(fechaSistemaUTC);
        grupoDireccionElectronica.IdEstado = SocioNegocioUtils.obtenerValorNoNulo(entradaGrupoDireccionElectroncia.IdEstado, "ACT");
        grupoDireccionElectronica.IdEstadoAutorizacion = SocioNegocioUtils.obtenerIdEstadoAutorizacion(requiereAutorizacion);
        grupoDireccionElectronica.IdUsuarioCreador = idUsuario;

        long fin = System.currentTimeMillis();
        logger.trace("FIN::CrearPersonaGrupoDireccionElectronicaMasivo.obtenerEntidadGrupoDireccionElectronica", fin - inicio);

        return grupoDireccionElectronica;
    }

    private void insertarGrupoDireccionElectronica(Connection conn, List<GrupoDireccionElectronica> gruposDireccionesElectronicas) throws SQLException {
        logger.trace("INICIO::CrearPersonaGrupoDireccionElectronicaMasivo.insertarGrupoDireccionElectronica");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        sql.batch(conn, SQL_INSERT_GRUPO_DIRECCION_ELECTRONICA, gruposDireccionesElectronicas, GrupoDireccionElectronica.class);

        long fin = System.currentTimeMillis();
        logger.trace("FIN::CrearPersonaGrupoDireccionElectronicaMasivo.insertarGrupoDireccionElectronica", fin - inicio);
    }

    private SalidaCrearPersonaGrupoDireccionElectronicaMasivo crearEventoAutorizacion(Connection conn, EntradaCrearPersonaGrupoDireccionElectronicaMasivo entrada, List<GrupoDireccionElectronica> gruposDireccionesElectronicas) throws Exception {
        logger.debug("INICIO::CrearPersonaGrupoDireccionElectronicaMasivo.crearEventoAutorizacion");
        long inicio = System.currentTimeMillis();

        SalidaCrearPersonaGrupoDireccionElectronicaMasivo salidaGrupoDireccionElectronicaMasivo = new SalidaCrearPersonaGrupoDireccionElectronicaMasivo();

        CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

        List<Long> idsPersona = gruposDireccionesElectronicas
                .parallelStream()
                .map(grupoDireccionElectronica -> grupoDireccionElectronica.IdPersona)
                .collect(Collectors.toList());
        Map<Long, SocioNegocioUtils.Persona> personasPorIdPersona = SocioNegocioUtils.obtenerDatosPersonaPorIdPersona(conn, idsPersona);

        CrearEventoAutorizacionMasivo.ServiceEntrada entradaEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
        entradaEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
        entradaEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
        entradaEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
        entradaEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
        entradaEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";

        entradaEventoAutorizacionMasivo.P_TABLAEVENTOS = gruposDireccionesElectronicas
                .stream()
                .map(grupoDireccionElectronica -> {
                    SocioNegocioUtils.Persona persona = personasPorIdPersona.get(grupoDireccionElectronica.IdPersona);
                    CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
                    evento.IdTipoEvento = ID_CONCEPTO;
                    evento.IdModuloSolicita = entrada.p_IdModulo;
                    evento.IdUsuarioSolicita = entrada.p_IdUsuario;
                    evento.Fecha = fechaSistema;
                    evento.IdConcepto = ID_CONCEPTO;
                    evento.NumeroOperacionConcepto = String.valueOf(grupoDireccionElectronica.IdGrupoDireccion);
                    evento.FechaOperacionConcepto = fechaSistema;
                    evento.IdCliente = grupoDireccionElectronica.IdPersona;
                    evento.Glosa = persona.DocumentoIdentidad + " / " + persona.NombreCompleto + " / Grupo: " + grupoDireccionElectronica.Grupo;

                    return evento;
                })
                .collect(Collectors.toList());

        CrearEventoAutorizacionMasivo.ServiceSalida salidaEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(conn, entradaEventoAutorizacionMasivo);

        salidaGrupoDireccionElectronicaMasivo.P_OUT_ERRORESCONTADOR = salidaEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
        salidaGrupoDireccionElectronicaMasivo.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesAdvertenciaAutorizacion(salidaEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE));
        salidaGrupoDireccionElectronicaMasivo.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesErroresAutorizacion(salidaEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE));

        if (salidaGrupoDireccionElectronicaMasivo.P_OUT_ERRORESCONTADOR.equals(0L)) {
            salidaGrupoDireccionElectronicaMasivo.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_AUTORIZACION, "REQUIERE_AUTORIZACION", new HashMap<>(), conn, entrada.p_IdEmpresa));
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::CrearPersonaGrupoDireccionElectronicaMasivo.crearEventoAutorizacion", fin - inicio);

        return salidaGrupoDireccionElectronicaMasivo;
    }

    private void agregarLog(Connection conn, EntradaCrearPersonaGrupoDireccionElectronicaMasivo entrada, List<GrupoDireccionElectronica> gruposDireccionesElectronicas) throws Exception {
        logger.trace("INICIO::CrearPersonaGrupoDireccionElectronicaMasivo.agregarLog");
        long inicio = System.currentTimeMillis();

        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

        for (GrupoDireccionElectronica grupoDireccionElectronica : gruposDireccionesElectronicas) {
            entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(grupoDireccionElectronica.IdGrupoDireccion, "Grupo", "", grupoDireccionElectronica.Grupo));
            entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(grupoDireccionElectronica.IdGrupoDireccion, "Comentario", "", grupoDireccionElectronica.Comentario));
            entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(grupoDireccionElectronica.IdGrupoDireccion, "IdEstado", "", grupoDireccionElectronica.IdEstado));

            AgregarLog.InAgregarLog.Log log = SocioNegocioUtils.obtenerLogPorDefecto();
            log.IdCabeceraLog = grupoDireccionElectronica.IdGrupoDireccion;
            log.IdTipoLog = "CREACION";
            log.IdModulo = entrada.p_IdModulo;
            log.IdUsuario = entrada.p_IdUsuario;
            log.IdEmpresa = entrada.p_IdEmpresa;
            log.FechaSistemaLog = fechaSistema;
            log.IdUsuarioAtendido = entrada.p_IdUsuario;
            log.IdCliente = grupoDireccionElectronica.IdPersona;
            log.IdTipoRegistro = "CREACION GRUPO DIRECCION ELECTRONICA";
            log.EstadoRegistro = grupoDireccionElectronica.IdEstadoAutorizacion;
            log.EstadoProceso = grupoDireccionElectronica.IdEstadoAutorizacion;
            log.NombreProceso = "Creacion Grupo Direccion Electronica";
            log.IdRegistro = String.valueOf(grupoDireccionElectronica.IdGrupoDireccion);

            entradaAgregarLog.p_Log.add(log);
        }

        AgregarLog agregarLogService = new AgregarLog();
        agregarLogService.logica(conn, entradaAgregarLog);

        long fin = System.currentTimeMillis();
        logger.trace("FIN::CrearPersonaGrupoDireccionElectronicaMasivo.agregarLog", fin - inicio);
    }

    private List<Entidad> obtenerEntidadesInsertadas(List<GrupoDireccionElectronica> gruposDireccionesElectronicas) {
        logger.trace("INICIO::CrearPersonaGrupoDireccionElectronicaMasivo.obtenerEntidadesInsertadas");
        long inicio = System.currentTimeMillis();

        List<Entidad> entidades = gruposDireccionesElectronicas
                .parallelStream()
                .map(grupoDireccionElectronica -> {
                    Entidad entidad = new Entidad();
                    entidad.IdEntidad = grupoDireccionElectronica.IdGrupoDireccion;
                    entidad.IdInterno = grupoDireccionElectronica.IdInterno;

                    return entidad;
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("FIN::CrearPersonaGrupoDireccionElectronicaMasivo.obtenerEntidadesInsertadas", fin - inicio);

        return entidades;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        String requestString = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_GruposDireccionesElectronicas\":[{\"IdInterno\":1,\"Grupo\":\"Prueba migracion DEV-120321-009\",\"Comentario\":\"Prueba migracion DEV-120321-009\",\"IdPersona\":621869,\"IdEstado\":\"ACT\"},{\"IdInterno\":2,\"Grupo\":\"Prueba migracion DEV-120321-010\",\"Comentario\":\"Prueba migracion DEV-120321-010\",\"IdPersona\":621869,\"IdEstado\":\"ACT\"},{\"IdInterno\":3,\"Grupo\":\"Prueba migracion DEV-120321-011\",\"Comentario\":\"Prueba migracion DEV-120321-011\",\"IdPersona\":621869,\"IdEstado\":\"ACT\"}]}";

        try (Connection conn = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaCrearPersonaGrupoDireccionElectronicaMasivo entrada = gson.fromJson(requestString, EntradaCrearPersonaGrupoDireccionElectronicaMasivo.class);
            CrearPersonaGrupoDireccionElectronicaMasivo crearPersonaGrupoDireccionElectronicaMasivo = new CrearPersonaGrupoDireccionElectronicaMasivo();

            try {
                SalidaCrearPersonaGrupoDireccionElectronicaMasivo salida = crearPersonaGrupoDireccionElectronicaMasivo.logica(conn, entrada);
                System.out.println(salida);
                conn.commit();

            } catch (Exception e) {
                e.printStackTrace();
                conn.rollback();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }
}
