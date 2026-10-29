package cl.bcs.cuentainversion.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.cuentainversion.port.out.ConfiguracionSistemaPort;
import cl.bcs.cuentainversion.port.out.DiccionarioMensajes;
import cl.bcs.cuentainversion.service.CrearCargoAbonoService.CargoAbonoAGenerar;
import cl.bcs.cuentainversion.service.CrearCargoAbonoService.CargoAbonoGenerado;
import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;
import cl.bcs.utils.Utils;

/**
 * Web-service front of {@link CrearCargoAbonoService} (README CTA-A-208..225): the caller identifies accounts by client document
 * and portfolio code; this service resolves them to portfolio ids, validates the batch (the FIRST problem aborts the whole
 * request) and delegates the creation. A MOVTRN movement is created as a MOVCAR transfer.
 */
@Service
public class CrearCargoAbonoWSService {

    private static final Logger log = LoggerFactory.getLogger(CrearCargoAbonoWSService.class);

    private static final String SQL_CLIENTE_PORTAFOLIO = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/CrearCargoAbonoWS_SelectClientePortafolio.sql");

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public List<Movimiento> p_CargoAbonoAGenerar = new ArrayList<>();
        public String p_SimularOperacion = "N";
    }

    public static class Movimiento {
        public Long NumeroCargoAbono;
        public String DocumentoIdentidad;
        public String CodigoPortafolio;
        public String IdMoneda;
        public String IdTipoMovimiento;
        public String IdConcepto;
        public String Descripcion;
        public String FechaCondicionDeLiquidacion;
        public BigDecimal Monto;
        public String DocumentoIdentidadTraspaso;
        public String CodigoPortafolioTraspaso;
        public String CertificadoAutomatico;
        public String IdTipoOperacion = "";
        public Long IdOperacion = 0L;
        public String FechaOperacion = "0001-01-01";
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
        public List<CargoAbonoGenerado> P_OUT_CARGOABONOGENERADO = new ArrayList<>();
    }

    /** Client and portfolio as the view reports them. */
    private record ClientePortafolio(String documentoIdentidad, String codigoPortafolio, Long idPortafolio, String nombrePortafolio,
                                     String estadoPortafolio, String estadoCliente, String esClienteNuevoProspecto) {
    }

    private record Clave(String documento, String portafolio) {
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final CrearCargoAbonoService crearCargoAbono;
    private final ConfiguracionSistemaPort configuracion;
    private final DiccionarioMensajes diccionario;

    public CrearCargoAbonoWSService(NamedParameterJdbcTemplate jdbc, CrearCargoAbonoService crearCargoAbono,
                                    ConfiguracionSistemaPort configuracion, DiccionarioMensajes diccionario) {
        this.jdbc = jdbc;
        this.crearCargoAbono = crearCargoAbono;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
    }

    @Transactional
    public JsonObject execute(JsonObject params) throws Exception {
        Entrada entrada = Utils.getGsonBuilder().fromJson(params, Entrada.class);
        return Utils.getGsonBuilder().toJsonTree(crear(entrada)).getAsJsonObject();
    }

    public Salida crear(Entrada entrada) throws Exception {
        log.info("INICIO::CrearCargoAbonoWS");
        Salida out = new Salida();
        String fechaSistema = CtaJavaCallInfra.parametro(configuracion, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

        Map<Clave, ClientePortafolio> clientes = resolverClientes(entrada);

        List<Long> idsInternos = new ArrayList<>();
        for (Movimiento mov : entrada.p_CargoAbonoAGenerar) {
            ClientePortafolio cliente = clientes.get(new Clave(mov.DocumentoIdentidad, mov.CodigoPortafolio));

            // LEGACY DEFECT kept (see README CTA-A-215, 216, 221): validation returns at the first error, the rest of the batch is not looked at
            if (idsInternos.contains(mov.NumeroCargoAbono)) {
                return rechazo(out, entrada, mov.NumeroCargoAbono, "ID_INTERNO_DUPLICADO", null);
            }
            idsInternos.add(mov.NumeroCargoAbono);

            if (cliente == null) {
                return rechazo(out, entrada, 0L, "CLIENTE_PORTAFOLIO_NO_EXISTE", null);
            }
            // LEGACY DEFECT kept (see README README, no case pins it): a null client or portfolio state throws NullPointerException
            if (!cliente.estadoCliente().equalsIgnoreCase("ACT") && "N".equals(cliente.esClienteNuevoProspecto())) {
                return rechazo(out, entrada, 0L, "CLIENTE_NO_SE_ENCUENTRA_ACTIVO",
                        DiccionarioMensajes.valores("NombreCliente", cliente.nombrePortafolio()));
            }
            if (!cliente.estadoPortafolio().equalsIgnoreCase("ACT")) {
                return rechazo(out, entrada, 0L, "PORTAFOLIO_NO_SE_ENCUENTRA_ACTIVO",
                        DiccionarioMensajes.valores("NombreCliente", cliente.nombrePortafolio()));
            }
        }

        CrearCargoAbonoService.Entrada crear = new CrearCargoAbonoService.Entrada();
        crear.p_IdEmpresa = entrada.p_IdEmpresa;
        crear.p_IdUsuario = entrada.p_IdUsuario;
        crear.p_IdModulo = "CTA";
        crear.p_simularOperacion = entrada.p_SimularOperacion;
        crear.p_UsaReglasNegocio = "S";
        crear.p_FechaSistema = fechaSistema;
        crear.p_EsIngresoManual = "N";
        for (Movimiento mov : entrada.p_CargoAbonoAGenerar) {
            crear.p_CargoAbonoAGenerar.add(movimientoDe(mov, clientes));
        }
        CrearCargoAbonoService.Salida creados = crearCargoAbono.crear(crear);

        out.P_OUT_MENSAJES.addAll(creados.P_OUT_MENSAJES);
        out.P_OUT_CARGOABONOGENERADO.addAll(creados.P_OUT_CARGOABONOGENERADO);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.countErrors(out.P_OUT_MENSAJES);
        log.info("FIN::CrearCargoAbonoWS");
        return out;
    }

    private Salida rechazo(Salida out, Entrada entrada, Long id, String codigo, Map<String, String> valores) {
        out.P_OUT_MENSAJES.add(new MensajeSalida(id, MensajeSalida.ERROR, codigo, diccionario.traducir(entrada.p_IdEmpresa, codigo, valores)));
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.countErrors(out.P_OUT_MENSAJES);
        return out;
    }

    /** Looks up every (document, portfolio code) pair of the request, transfer counterparts included. */
    private Map<Clave, ClientePortafolio> resolverClientes(Entrada entrada) {
        List<String> documentos = new ArrayList<>();
        List<String> portafolios = new ArrayList<>();
        for (Movimiento mov : entrada.p_CargoAbonoAGenerar) {
            agregarUnico(documentos, mov.DocumentoIdentidad);
            agregarUnico(portafolios, mov.CodigoPortafolio);
            if (mov.DocumentoIdentidadTraspaso != null && !mov.DocumentoIdentidadTraspaso.isEmpty()) {
                agregarUnico(documentos, mov.DocumentoIdentidadTraspaso);
            }
            if (mov.CodigoPortafolioTraspaso != null && !mov.CodigoPortafolioTraspaso.isEmpty()) {
                agregarUnico(portafolios, mov.CodigoPortafolioTraspaso);
            }
        }
        List<ClientePortafolio> filas = jdbc.query(SQL_CLIENTE_PORTAFOLIO, new MapSqlParameterSource()
                        .addValue("DocumentoIdentidad", nuloSiVacio(documentos))
                        .addValue("CodigoPortafolio", nuloSiVacio(portafolios))
                        .addValue("IdEmpresa", entrada.p_IdEmpresa),
                (rs, i) -> new ClientePortafolio(rs.getString("DocumentoIdentidad"), rs.getString("CodigoPortafolio"),
                        rs.getObject("IdPortafolio") == null ? null : rs.getLong("IdPortafolio"), rs.getString("NombreCompletoPortafolio"),
                        rs.getString("IdEstado"), rs.getString("IdEstadoCliente"), rs.getString("EsClienteNuevoProspecto")));
        Map<Clave, ClientePortafolio> mapa = new HashMap<>();
        // LEGACY DEFECT kept (see README CTA-A-222): with duplicate rows in the view the first one (no ORDER BY) decides
        filas.forEach(f -> mapa.putIfAbsent(new Clave(f.documentoIdentidad(), f.codigoPortafolio()), f));
        return mapa;
    }

    private static CargoAbonoAGenerar movimientoDe(Movimiento mov, Map<Clave, ClientePortafolio> clientes) {
        // LEGACY DEFECT kept (see README CTA-A-223): a null IdTipoMovimiento throws NullPointerException
        boolean traspaso = mov.IdTipoMovimiento.equalsIgnoreCase("MOVTRN");
        CargoAbonoAGenerar m = new CargoAbonoAGenerar();
        m.NumeroCargoAbono = mov.NumeroCargoAbono;
        m.IdMoneda = mov.IdMoneda;
        m.IdTipoMovimiento = traspaso ? "MOVCAR" : mov.IdTipoMovimiento;
        m.IdConcepto = mov.IdConcepto;
        m.Descripcion = mov.Descripcion;
        m.IdCondicionDeLiquidacion = "T0"; // the settlement date given by the caller rules
        m.FechaCondicionDeLiquidacion = mov.FechaCondicionDeLiquidacion;
        m.Monto = mov.Monto;
        m.IdEjecutivo = 0L;
        m.IdSucursal = 0L;
        m.EsCargoAbonoPorTraspaso = traspaso ? "S" : "N";
        m.GeneroContraparteEnTesoreria = "N";
        m.CertificadoAutomatico = mov.CertificadoAutomatico;
        m.IdPortafolio = idPortafolio(clientes.get(new Clave(mov.DocumentoIdentidad, mov.CodigoPortafolio)));
        m.IdPortafolioTraspaso = idPortafolio(clientes.get(new Clave(mov.DocumentoIdentidadTraspaso, mov.CodigoPortafolioTraspaso)));
        m.IdTipoOperacion = mov.IdTipoOperacion;
        m.IdOperacion = mov.IdOperacion;
        m.FechaOperacion = mov.FechaOperacion;
        m.EsIndicadorBilateral = "N";
        return m;
    }

    private static Long idPortafolio(ClientePortafolio c) {
        return c == null ? Long.valueOf(0L) : c.idPortafolio();
    }

    private static void agregarUnico(List<String> lista, String valor) {
        if (!lista.contains(valor)) {
            lista.add(valor);
        }
    }

    /** An empty list in a SQL IN must still bind one (NULL) value, as the legacy SQL helper did. */
    private static List<String> nuloSiVacio(List<String> valores) {
        return valores.isEmpty() ? Arrays.asList((String) null) : valores;
    }
}
