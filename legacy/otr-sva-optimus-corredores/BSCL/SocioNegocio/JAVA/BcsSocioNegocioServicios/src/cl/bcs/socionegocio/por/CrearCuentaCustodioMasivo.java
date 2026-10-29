package cl.bcs.socionegocio.por;

import cl.bcs.APV.ReplicaPortafolioAPV;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.socionegocio.por.pojo.CuentaCustodio;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.socionegocio.soc.SocioNegocioUtils.Portafolio;
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

public class CrearCuentaCustodioMasivo implements JavaCall {

    static final Logger logger = LogManager.getLogger(CrearCuentaCustodioMasivo.class);

    private final String SQL_INSERT_CUENTA_CUSTODIO = loadResourceToString("cl/bcs/socionegocio/por/sql/CrearCuentaCustodio_InsertCuentaCustodio.sql");

    private final String ID_CONCEPTO = "NPCCUS";
    private final String ID_ESTADO_AUTORIZACION_PENDIENTE = "PEN";
    private final String ID_ESTADO_AUTORIZACION_AUTORIZADO = "ING";
    private final String PROPOSITO_APV = "PROPOSITOAPV";
    private final String ID_TIPO_ENTIDAD_REPLICA = "CUENTACUSTODIO";
    private final String ID_TIPO_OPERACION = "I";

    public static class EntradaCrearCuentaCustodioMasivo extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public List<CuentaCustodio> p_CuentasCustodio;

        public EntradaCrearCuentaCustodioMasivo() {
            this.p_CuentasCustodio = new ArrayList<>();
        }
    }

    public static class SalidaCrearCuentaCustodioMasivo extends BcsPojo {
        public Long P_OUT_ERRORESCONTADOR;
        public List<Entidad> P_OUT_REFID;
        public List<MensajeSalida> P_OUT_MENSAJES;

        public SalidaCrearCuentaCustodioMasivo() {
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_REFID = new ArrayList<>();
            this.P_OUT_MENSAJES = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaCrearCuentaCustodioMasivo jin = gson.fromJson(in.toString(), EntradaCrearCuentaCustodioMasivo.class);
        SalidaCrearCuentaCustodioMasivo out = logica(conn, jin);

        JsonParser jsonParser = new JsonParser();

        return jsonParser.parse(gson.toJson(out)).getAsJsonObject();
    }

    public SalidaCrearCuentaCustodioMasivo logica(Connection conn, EntradaCrearCuentaCustodioMasivo entrada) throws Exception {
        logger.info("INICIO::CrearCuentaCustodioMasivo");
        long inicio = System.currentTimeMillis();

        SalidaCrearCuentaCustodioMasivo out = new SalidaCrearCuentaCustodioMasivo();
        Boolean requiereAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(conn, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);
        List<CuentaCustodio> cuentasCustodio = obtenerCuentasCustodio(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_CuentasCustodio,requiereAutorizacion);
        insertarCuentasCustodio(conn, cuentasCustodio);

        Map<Long, SocioNegocioUtils.Portafolio> portafoliosPorIdPortafolio = obtenerPortafoliosPorIdPortafolio(conn, cuentasCustodio);

        if (requiereAutorizacion) {
            out = crearEventoAutorizacion(conn, entrada, cuentasCustodio, portafoliosPorIdPortafolio);
        } else {
            agregarLog(conn, entrada, cuentasCustodio, portafoliosPorIdPortafolio);
        }

        replicarPortafoliosAPV(conn, entrada, cuentasCustodio, portafoliosPorIdPortafolio);

        out.P_OUT_REFID = obtenerIdsInsertadas(cuentasCustodio);

        long fin = System.currentTimeMillis();
        logger.info("FIN::CrearCuentaCustodioMasivo", fin - inicio);

        return out;
    }

    private List<CuentaCustodio> obtenerCuentasCustodio(Connection conn, Long idEmpresa, Long idUsuario, List<CuentaCustodio> cuentasCustodioEntrada, boolean requiereAutorizacion) throws Exception {
        logger.info("INICIO::CrearCuentaCustodioMasivo.obtenerCuentasCustodio");
        long inicio = System.currentTimeMillis();

        List<Long> secuenciaCuentaCustodio = Utils.getSecuencia(conn, "BSCL.Portafolio.BD.Secuencias::CuentaCustodioPortafolio", Long.valueOf(cuentasCustodioEntrada.size()));
        String horaSistemaUTC = FechaDB.ObtenerFechaHoraSistemaUTC(conn, idEmpresa);
        AtomicInteger index = new AtomicInteger(0);

        List<CuentaCustodio> cuentasCustodio = cuentasCustodioEntrada.parallelStream().map(peticionCuentaCustodio -> {
            Long idCuentaCustodioPortafolio = secuenciaCuentaCustodio.get(index.getAndIncrement());

            return obtenerEntidadCuentaCustodio(peticionCuentaCustodio, idUsuario, idCuentaCustodioPortafolio, horaSistemaUTC,requiereAutorizacion);
        }).collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.info("FIN::CrearCuentaCustodioMasivo.obtenerCuentasCustodio", fin - inicio);

        return cuentasCustodio;
    }

    private CuentaCustodio obtenerEntidadCuentaCustodio(CuentaCustodio peticionCuentaCustodio, Long idUsuario, Long idCuentaCustodioPortafolio, String horaSistemaUTC, boolean requiereAutorizacion) {
        logger.info("INICIO::CrearCuentaCustodioMasivo.obtenerEntidadCuentaCustodio");
        long inicio = System.currentTimeMillis();

        CuentaCustodio cuentaCustodio = new CuentaCustodio();
        cuentaCustodio.IdInterno = peticionCuentaCustodio.IdInterno;
        cuentaCustodio.IdPortafolio = peticionCuentaCustodio.IdPortafolio;
        cuentaCustodio.IdBoveda = peticionCuentaCustodio.IdBoveda;
        cuentaCustodio.Codigo = peticionCuentaCustodio.Codigo;
        cuentaCustodio.IdTipoCuentaCustodia = peticionCuentaCustodio.IdTipoCuentaCustodia;
        cuentaCustodio.IdTipoCustodia = peticionCuentaCustodio.IdTipoCustodia;
        cuentaCustodio.IdModeloCustodia = peticionCuentaCustodio.IdModeloCustodia;
        cuentaCustodio.IdPersonaBancoCustodio = peticionCuentaCustodio.IdPersonaBancoCustodio;
        cuentaCustodio.IdPersonaBancoLiquidador = peticionCuentaCustodio.IdPersonaBancoLiquidador;
        cuentaCustodio.IdEstado = peticionCuentaCustodio.IdEstado;
        cuentaCustodio.LiquidaPrepagos = peticionCuentaCustodio.LiquidaPrepagos;
        cuentaCustodio.LiquidaVencimientos = peticionCuentaCustodio.LiquidaVencimientos;
        cuentaCustodio.LiquidaCupones = peticionCuentaCustodio.LiquidaCupones;
        cuentaCustodio.LiquidaDividendos = peticionCuentaCustodio.LiquidaDividendos;

        cuentaCustodio.IdCuentaCustodioPortafolio = idCuentaCustodioPortafolio;
        cuentaCustodio.FechaIngreso = Fecha.toTimeStamp(horaSistemaUTC);
        cuentaCustodio.FechaCreacion = Fecha.toTimeStamp(horaSistemaUTC);
        cuentaCustodio.FechaModificacion = Fecha.toTimeStamp(horaSistemaUTC);
        cuentaCustodio.IdUsuarioCreador = idUsuario;
        cuentaCustodio.IdEstadoAutorizacion = requiereAutorizacion? ID_ESTADO_AUTORIZACION_PENDIENTE : ID_ESTADO_AUTORIZACION_AUTORIZADO;

        long fin = System.currentTimeMillis();
        logger.info("FIN::CrearCuentaCustodioMasivo.obtenerEntidadCuentaCustodio", fin - inicio);

        return cuentaCustodio;
    }

    private List<Entidad> obtenerIdsInsertadas(List<CuentaCustodio> cuentasCustodio) {
        logger.trace("INICIO::CrearCuentaCustodioMasivo.obtenerIdsCuentasCustodio");
        long inicio = System.currentTimeMillis();

        List<Entidad> ids = cuentasCustodio
                .stream()
                .map(cuentaCustodio -> {
                    Entidad entidad = new Entidad();
                    entidad.IdEntidad = cuentaCustodio.IdCuentaCustodioPortafolio;
                    entidad.IdInterno = cuentaCustodio.IdInterno;

                    return entidad;
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("FIN::CrearCuentaCustodioMasivo.obtenerIdsCuentasCustodio", fin - inicio);

        return ids;
    }

    private void insertarCuentasCustodio(Connection conn, List<CuentaCustodio> cuentasCustodio) throws Exception {
        logger.debug("INICIO::CrearCuentaCustodioMasivo.insertarCuentasCustodio");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        sql.batch(conn, SQL_INSERT_CUENTA_CUSTODIO, cuentasCustodio, CuentaCustodio.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::CrearCuentaCustodioMasivo.insertarCuentasCustodio", fin - inicio);
    }

    private Map<Long, SocioNegocioUtils.Portafolio> obtenerPortafoliosPorIdPortafolio(Connection conn, List<CuentaCustodio> cuentasCustodio) throws SQLException {
        List<Long> idsPortafolio = cuentasCustodio
                .parallelStream()
                .map(cuentaCustodio -> cuentaCustodio.IdPortafolio)
                .collect(Collectors.toList());

        return SocioNegocioUtils.obtenerDatosPortafolioPorIdPortafolio(conn, idsPortafolio);
    }

    private SalidaCrearCuentaCustodioMasivo crearEventoAutorizacion(Connection conn, EntradaCrearCuentaCustodioMasivo entrada, List<CuentaCustodio> cuentasCustodio, Map<Long, SocioNegocioUtils.Portafolio> portafoliosPorIdPortafolio) throws Exception {
        logger.debug("INICIO::CrearCuentaCustodioMasivo.crearEventoAutorizacion");
        long inicio = System.currentTimeMillis();

        SalidaCrearCuentaCustodioMasivo out = new SalidaCrearCuentaCustodioMasivo();

        CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

        CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
        entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
        entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
        entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
        entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
        entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";

        entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = cuentasCustodio
                .stream()
                .map(cuentaCustodio -> {
                    SocioNegocioUtils.Portafolio portafolio = portafoliosPorIdPortafolio.get(cuentaCustodio.IdPortafolio);
                    CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
                    evento.IdTipoEvento = ID_CONCEPTO;
                    evento.IdModuloSolicita = entrada.p_IdModulo;
                    evento.IdUsuarioSolicita = entrada.p_IdUsuario;
                    evento.Fecha = fechaSistema;
                    evento.IdConcepto = ID_CONCEPTO;
                    evento.NumeroOperacionConcepto = String.valueOf(cuentaCustodio.IdCuentaCustodioPortafolio);
                    evento.IdCliente = portafolio.IdPersona;
                    evento.Glosa = portafolio.DocumentoIdentidad + " / " + portafolio.NombreCompleto + " / CodPort: " + portafolio.CodigoPortafolio + " / Cuenta Custodio: " + cuentaCustodio.Codigo;

                    return evento;
                })
                .collect(Collectors.toList());

        CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(conn, entradaCrearEventoAutorizacionMasivo);

        out.P_OUT_ERRORESCONTADOR += salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
        out.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesAdvertenciaAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE));
        out.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesErroresAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE));

        if (out.P_OUT_ERRORESCONTADOR.equals(0L)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_AUTORIZACION, "REQUIERE_AUTORIZACION", new HashMap<>(), conn, entrada.p_IdEmpresa));
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::CrearCuentaCustodioMasivo.crearEventoAutorizacion", fin - inicio);

        return out;
    }

    private void agregarLog(Connection conn, EntradaCrearCuentaCustodioMasivo entrada, List<CuentaCustodio> cuentasCustodio, Map<Long, SocioNegocioUtils.Portafolio> portafoliosPorIdPortafolio) throws Exception {
        logger.trace("INICIO::CrearCuentaCustodioMasivo.agregarLog");
        long inicio = System.currentTimeMillis();

        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

        for (CuentaCustodio cuentaCustodio : cuentasCustodio) {
            SocioNegocioUtils.Portafolio portafolio = portafoliosPorIdPortafolio.get(cuentaCustodio.IdPortafolio);

            entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(cuentaCustodio.IdCuentaCustodioPortafolio, "Codigo", "", cuentaCustodio.Codigo));
            entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(cuentaCustodio.IdCuentaCustodioPortafolio, "LiquidaCupones", "", cuentaCustodio.LiquidaCupones));
            entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(cuentaCustodio.IdCuentaCustodioPortafolio, "LiquidaDividendos", "", cuentaCustodio.LiquidaDividendos));
            entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(cuentaCustodio.IdCuentaCustodioPortafolio, "LiquidaPrepagos", "", cuentaCustodio.LiquidaPrepagos));
            entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(cuentaCustodio.IdCuentaCustodioPortafolio, "LiquidaVencimientos", "", cuentaCustodio.LiquidaVencimientos));
            entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(cuentaCustodio.IdCuentaCustodioPortafolio, "IdEstado", "", cuentaCustodio.IdEstado));

            AgregarLog.InAgregarLog.Log log = SocioNegocioUtils.obtenerLogPorDefecto();
            log.IdCabeceraLog = cuentaCustodio.IdCuentaCustodioPortafolio;
            log.IdTipoLog = "CREACION";
            log.IdModulo = entrada.p_IdModulo;
            log.IdUsuario = entrada.p_IdUsuario;
            log.IdEmpresa = entrada.p_IdEmpresa;
            log.FechaSistemaLog = fechaSistema;
            log.IdUsuarioAtendido = entrada.p_IdUsuario;
            log.IdCliente = portafolio.IdCliente;
            log.IdTipoRegistro = "CREACION_CUENTA_CUSTODIO";
            log.EstadoRegistro = cuentaCustodio.IdEstadoAutorizacion;
            log.EstadoProceso = cuentaCustodio.IdEstadoAutorizacion;
            log.NombreProceso = "Creacion Cuenta Custodio";
            log.IdRegistro = String.valueOf(cuentaCustodio.IdCuentaCustodioPortafolio);

            entradaAgregarLog.p_Log.add(log);
        }

        AgregarLog agregarLogService = new AgregarLog();
        agregarLogService.logica(conn, entradaAgregarLog);

        long fin = System.currentTimeMillis();
        logger.trace("FIN::CrearCuentaCustodioMasivo.agregarLog", fin - inicio);
    }

    private SalidaCrearCuentaCustodioMasivo replicarPortafoliosAPV(Connection conn, EntradaCrearCuentaCustodioMasivo entrada, List<CuentaCustodio> cuentasCustodio, Map<Long, Portafolio> portafoliosPorIdPortafolio) throws Exception {
        logger.debug("INICIO::CrearCuentaCustodioMasivo.replicarPortafoliosAPV");
        long inicio = System.currentTimeMillis();

        SalidaCrearCuentaCustodioMasivo out = new SalidaCrearCuentaCustodioMasivo();

        for (CuentaCustodio cuentaCustodio : cuentasCustodio) {
            SocioNegocioUtils.Portafolio portafolio = portafoliosPorIdPortafolio.get(cuentaCustodio.IdPortafolio);
            boolean esPropositoAPV = portafolio != null && portafolio.Proposito.equals(PROPOSITO_APV);

            if (esPropositoAPV) {
                ReplicaPortafolioAPV.EntradaReplicaPortafolioAPV entradaReplicaPortafolioAPV = new ReplicaPortafolioAPV.EntradaReplicaPortafolioAPV();
                entradaReplicaPortafolioAPV.p_IdEmpresa = entrada.p_IdEmpresa;
                entradaReplicaPortafolioAPV.p_IdUsuario = entrada.p_IdUsuario;
                entradaReplicaPortafolioAPV.p_IdModulo = entrada.p_IdModulo;
                entradaReplicaPortafolioAPV.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
                entradaReplicaPortafolioAPV.p_Portafolio = cuentaCustodio.IdPortafolio;
                entradaReplicaPortafolioAPV.p_IdEntidad = cuentaCustodio.IdCuentaCustodioPortafolio;
                entradaReplicaPortafolioAPV.p_TipoEntidad = ID_TIPO_ENTIDAD_REPLICA;
                entradaReplicaPortafolioAPV.p_TipoOperacion = ID_TIPO_OPERACION;

                ReplicaPortafolioAPV replicaPortafolioAPV = new ReplicaPortafolioAPV();
                ReplicaPortafolioAPV.SalidaReplicaPortafolioAPV salidaReplicaPortafolioAPV = replicaPortafolioAPV.logica(conn, entradaReplicaPortafolioAPV);

                out.P_OUT_ERRORESCONTADOR += salidaReplicaPortafolioAPV.P_OUT_ERRORESCONTADOR;
                out.P_OUT_MENSAJES.addAll(salidaReplicaPortafolioAPV.P_OUT_MENSAJES);
            }
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::CrearCuentaCustodioMasivo.replicarPortafoliosAPV", fin - inicio);

        return out;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");
        String requestString = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_CuentasCustodio\":[{\"IdInterno\":1,\"IdPortafolio\":1451254,\"IdBoveda\":\"COR\",\"Codigo\":\"DEV-CC-REPLICA-APV-TEST-001\",\"IdEstado\":\"ACT\",\"LiquidaDividendos\":\"S\",\"LiquidaCupones\":\"S\",\"LiquidaVencimientos\":\"S\",\"LiquidaPrepagos\":\"S\"}]}";

        try (Connection conn = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaCrearCuentaCustodioMasivo entrada = gson.fromJson(requestString, EntradaCrearCuentaCustodioMasivo.class);
            CrearCuentaCustodioMasivo crearCuentaCustodioMasivo = new CrearCuentaCustodioMasivo();
            try {
                SalidaCrearCuentaCustodioMasivo out = crearCuentaCustodioMasivo.logica(conn, entrada);
                System.out.println(out);
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
