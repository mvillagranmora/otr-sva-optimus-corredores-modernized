package cl.bcs.socionegocio.per;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.ValidarGrupoDireccionElectronicaDetalleMasivo.EntradaValidarGrupoDireccionElectronicaDetalleMasivo;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.socionegocio.per.pojo.GrupoDireccionElectronica;
import cl.bcs.socionegocio.pojo.Validacion;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CrearPersonaGrupoDireccionElectronicaDetalleMasivo implements JavaCall {

    static final Logger logger = LogManager.getLogger(CrearPersonaGrupoDireccionElectronicaDetalleMasivo.class);

    public final String SQL_INSERT_GRUPO_DIRECCION_ELECTRONICA_DETALLE = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearGrupoDireccionElectronicaDetalleMasivo_InsertGrupoDireccionElectronicaDetalle.sql");

    public static class EntradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;

        public List<GrupoDireccionElectronica.Detalle> p_Detalles;

        public EntradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo() {
            this.p_Detalles = new ArrayList<>();
        }
    }

    public static class SalidaCrearPersonaGrupoDireccionElectronicaDetalleMasivo extends BcsPojo {
        public Long P_OUT_ERRORESCONTADOR;
        public List<MensajeSalida> P_OUT_MENSAJES;
        public List<Entidad> P_OUT_REFID;

        public SalidaCrearPersonaGrupoDireccionElectronicaDetalleMasivo() {
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_REFID = new ArrayList<>();
            this.P_OUT_MENSAJES = new ArrayList<>();
        }

        public void agregarValidaciones(List<Validacion> validaciones) {
            for (Validacion validacion : validaciones) {
                if (validacion.EsError) {
                    this.P_OUT_ERRORESCONTADOR += 1L;
                    this.P_OUT_MENSAJES.add(validacion.MensajeError);
                }
            }
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo jin = gson.fromJson(in.toString(), EntradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo.class);

        SalidaCrearPersonaGrupoDireccionElectronicaDetalleMasivo out = logica(conn, jin, true);
        JsonParser parser = new JsonParser();

        return parser.parse(gson.toJson(out)).getAsJsonObject();
    }

    public SalidaCrearPersonaGrupoDireccionElectronicaDetalleMasivo logica(Connection conn, EntradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo entrada, boolean validar) throws Exception {
        logger.info("INICIO::CrearPersonaGrupoDireccionElectronicaDetalleMasivo");
        long inicio = System.currentTimeMillis();

        SalidaCrearPersonaGrupoDireccionElectronicaDetalleMasivo salida = new SalidaCrearPersonaGrupoDireccionElectronicaDetalleMasivo();

        if (validar) {
            ValidarGrupoDireccionElectronicaDetalleMasivo validarGrupoDireccionElectronicaDetalleMasivo = new ValidarGrupoDireccionElectronicaDetalleMasivo();
            EntradaValidarGrupoDireccionElectronicaDetalleMasivo entradaValidarGrupoDireccionElectronicaDetalleMasivo = new EntradaValidarGrupoDireccionElectronicaDetalleMasivo();
            entradaValidarGrupoDireccionElectronicaDetalleMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
            entradaValidarGrupoDireccionElectronicaDetalleMasivo.p_IdModulo = entrada.p_IdModulo;
            entradaValidarGrupoDireccionElectronicaDetalleMasivo.p_IdUsuario = entrada.p_IdUsuario;
            entradaValidarGrupoDireccionElectronicaDetalleMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
            entradaValidarGrupoDireccionElectronicaDetalleMasivo.p_Detalles = entrada.p_Detalles;
            salida.agregarValidaciones(validarGrupoDireccionElectronicaDetalleMasivo.logica(conn, entradaValidarGrupoDireccionElectronicaDetalleMasivo));
        }

        if (salida.P_OUT_ERRORESCONTADOR > 0) {
            return salida;
        }

        List<GrupoDireccionElectronica.Detalle> detalles = obtenerDetalles(conn, entrada);
        insertarDetalle(conn, detalles);
        agregarLog(conn, entrada, detalles);

        salida.P_OUT_REFID = obtenerEntidadesInsertadas(detalles);

        long fin = System.currentTimeMillis();
        logger.info("FIN::CrearPersonaGrupoDireccionElectronicaDetalleMasivo", fin - inicio);

        return salida;
    }

    private List<GrupoDireccionElectronica.Detalle> obtenerDetalles(Connection conn, EntradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo entrada) throws Exception {
        logger.debug("INICIO::CrearPersonaGrupoDireccionElectronicaDetalleMasivo.obtenerDetalles");
        long inicio = System.currentTimeMillis();

        List<Long> idsGruposDireccionesElectronicas = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::GrupoDireccionDetalle", Long.valueOf(entrada.p_Detalles.size()));
        String fechaSistemaUTC = FechaDB.ObtenerFechaHoraSistemaUTC(conn, entrada.p_IdEmpresa);

        AtomicInteger index = new AtomicInteger();
        List<GrupoDireccionElectronica.Detalle> detalles = entrada.p_Detalles
                .stream()
                .map(entradaDetalle -> {
                    Long idGrupoDireccionElectronicaDetalle = idsGruposDireccionesElectronicas.get(index.getAndIncrement());

                    return obtenerEntidadDetalle(entradaDetalle, entrada.p_IdUsuario, idGrupoDireccionElectronicaDetalle, fechaSistemaUTC);
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.debug("FIN::CrearPersonaGrupoDireccionElectronicaDetalleMasivo.obtenerDetalles", fin - inicio);

        return detalles;
    }

    private GrupoDireccionElectronica.Detalle obtenerEntidadDetalle(GrupoDireccionElectronica.Detalle entradaDetalle, Long idUsuario, Long idGrupoDireccionElectronicaDetalle, String fechaSistemaUTC) {
        logger.trace("INICIO::CrearPersonaGrupoDireccionElectronicaDetalleMasivo.obtenerEntidadDetalle");
        long inicio = System.currentTimeMillis();

        GrupoDireccionElectronica.Detalle detalle = new GrupoDireccionElectronica.Detalle();
        detalle.IdInterno = entradaDetalle.IdInterno;
        detalle.IdGrupoDireccionDetalle = idGrupoDireccionElectronicaDetalle;
        detalle.IdDireccionElectronica = entradaDetalle.IdDireccionElectronica;
        detalle.IdGrupoDireccion = entradaDetalle.IdGrupoDireccion;
        detalle.IdTipoDestinatario = entradaDetalle.IdTipoDestinatario;
        detalle.FechaCreacion = Fecha.toTimeStamp(fechaSistemaUTC);
        detalle.FechaModificacion = Fecha.toTimeStamp(fechaSistemaUTC);
        detalle.FechaIngreso = Fecha.toTimeStamp(fechaSistemaUTC);
        detalle.IdEstado = SocioNegocioUtils.obtenerValorNoNulo(entradaDetalle.IdEstado, "ACT");
        detalle.IdEstadoAutorizacion = "ACT";
        detalle.IdUsuarioCreador = idUsuario;

        long fin = System.currentTimeMillis();
        logger.trace("FIN::CrearPersonaGrupoDireccionElectronicaDetalleMasivo.obtenerEntidadDetalle", fin - inicio);

        return detalle;
    }

    private void insertarDetalle(Connection conn, List<GrupoDireccionElectronica.Detalle> detalles) throws SQLException {
        logger.trace("INICIO::CrearPersonaGrupoDireccionElectronicaDetalleMasivo.insertarDetalle");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        sql.batch(conn, SQL_INSERT_GRUPO_DIRECCION_ELECTRONICA_DETALLE, detalles, GrupoDireccionElectronica.Detalle.class);

        long fin = System.currentTimeMillis();
        logger.trace("FIN::CrearPersonaGrupoDireccionElectronicaDetalleMasivo.insertarDetalle", fin - inicio);
    }

    private void agregarLog(Connection conn, EntradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo entrada, List<GrupoDireccionElectronica.Detalle> detalles) throws Exception {
        logger.trace("INICIO::CrearPersonaGrupoDireccionElectronicaDetalleMasivo.agregarLog");
        long inicio = System.currentTimeMillis();

        List<Long> idsGrupoDireccionElectronica = detalles.stream().map(detalle -> detalle.IdGrupoDireccion).collect(Collectors.toList());
        Map<Long, SocioNegocioUtils.GrupoDireccionElectronica> datosPersonaPorIdGrupoDireccion = SocioNegocioUtils.obtenerDatosPersonaPorIdGrupoDireccionElectronica(conn, idsGrupoDireccionElectronica);

        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

        for (GrupoDireccionElectronica.Detalle detalle : detalles) {
            SocioNegocioUtils.GrupoDireccionElectronica datosPersona = datosPersonaPorIdGrupoDireccion.get(detalle.IdGrupoDireccion);

            entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(detalle.IdGrupoDireccionDetalle, "IdGrupoDireccion", "", String.valueOf(detalle.IdGrupoDireccion)));
            entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(detalle.IdGrupoDireccionDetalle, "IdDireccionElectronica", "", String.valueOf(detalle.IdDireccionElectronica)));
            entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(detalle.IdGrupoDireccionDetalle, "IdTipoDestinatario", "", detalle.IdTipoDestinatario));
            entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(detalle.IdGrupoDireccionDetalle, "IdEstado", "", detalle.IdEstado));

            AgregarLog.InAgregarLog.Log log = SocioNegocioUtils.obtenerLogPorDefecto();
            log.IdCabeceraLog = detalle.IdGrupoDireccionDetalle;
            log.IdTipoLog = "CREACION";
            log.IdModulo = entrada.p_IdModulo;
            log.IdUsuario = entrada.p_IdUsuario;
            log.IdEmpresa = entrada.p_IdEmpresa;
            log.FechaSistemaLog = fechaSistema;
            log.IdUsuarioAtendido = entrada.p_IdUsuario;
            log.IdCliente = datosPersona.IdPersona;
            log.IdTipoRegistro = "CREACION GRUPO DIRECCION ELECTRONICA DET";
            log.EstadoRegistro = detalle.IdEstadoAutorizacion;
            log.EstadoProceso = detalle.IdEstadoAutorizacion;
            log.NombreProceso = "Creacion Grupo Direccion Electronica Detalle";
            log.IdRegistro = String.valueOf(detalle.IdGrupoDireccionDetalle);

            entradaAgregarLog.p_Log.add(log);
        }

        AgregarLog agregarLogService = new AgregarLog();
        agregarLogService.logica(conn, entradaAgregarLog);

        long fin = System.currentTimeMillis();
        logger.trace("FIN::CrearPersonaGrupoDireccionElectronicaDetalleMasivo.agregarLog", fin - inicio);
    }

    private List<Entidad> obtenerEntidadesInsertadas(List<GrupoDireccionElectronica.Detalle> detalles) {
        logger.trace("INICIO::CrearPersonaGrupoDireccionElectronicaDetalleMasivo.obtenerEntidadesInsertadas");
        long inicio = System.currentTimeMillis();

        List<Entidad> entidades = detalles
                .parallelStream()
                .map(detalle -> {
                    Entidad entidad = new Entidad();
                    entidad.IdEntidad = detalle.IdGrupoDireccionDetalle;
                    entidad.IdInterno = detalle.IdInterno;

                    return entidad;
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("FIN::CrearPersonaGrupoDireccionElectronicaDetalleMasivo.obtenerEntidadesInsertadas", fin - inicio);

        return entidades;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        String requestString = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_Detalles\":[{\"IdInterno\":1,\"IdDireccionElectronica\":588085,\"IdGrupoDireccion\":1944238,\"IdTipoDestinatario\":\"PARA\",\"IdEstado\":\"ACT\"},{\"IdInterno\":3,\"IdDireccionElectronica\":1942971,\"IdGrupoDireccion\":1944238,\"IdTipoDestinatario\":\"PARA\",\"IdEstado\":\"ACT\"},{\"IdInterno\":4,\"IdDireccionElectronica\":1943692,\"IdGrupoDireccion\":1944238,\"IdTipoDestinatario\":\"CC\",\"IdEstado\":\"ACT\"}]}";

        try (Connection conn = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo entrada = gson.fromJson(requestString, EntradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo.class);
            CrearPersonaGrupoDireccionElectronicaDetalleMasivo crearPersonaGrupoDireccionElectronicaDetalleMasivo = new CrearPersonaGrupoDireccionElectronicaDetalleMasivo();

            try {
                SalidaCrearPersonaGrupoDireccionElectronicaDetalleMasivo salida = crearPersonaGrupoDireccionElectronicaDetalleMasivo.logica(conn, entrada, true);
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
