package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.model.MensajeSalida;
import cl.bcs.contabilidad.domain.model.sap.AsientoEnvio;
import cl.bcs.contabilidad.domain.model.sap.CuentaSap;
import cl.bcs.contabilidad.domain.model.sap.PreComprobanteSalida;
import cl.bcs.contabilidad.domain.port.out.DiccionarioPort;
import cl.bcs.contabilidad.domain.port.out.PlanCuentasSapPort;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.infrastructure.persistence.SqlParams;
import cl.bcs.contabilidad.infrastructure.persistence.SqlResources;
import cl.bcs.contabilidad.infrastructure.persistence.SqlRow;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SimplePropertySqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Stages pre-vouchers for SAP: validates them, builds the SAP-shaped lines and writes
 * {@code ITZ.PreComprobanteEnvioContabilidad}, {@code ITZ.PreComprobanteAsientoEnvioContabilidad} and
 * {@code ITZ.PreComprobanteCentralizacionTipo}. It does not call SAP; GenerarJsonAsientoSap / InterfazContable post
 * the staged rows later.
 *
 * <p>Legacy {@code cl.bcs.contabilidad.EnvioContabilidad.logica} (EnvioContabilidad.java:325-682), CONT-B-01..12:
 * <ul>
 *   <li>Pre-vouchers in Estado C or A get ADVERTENCIA PRE_COMPROBANTE_CONTABILIZADO_O_ANULADO, unknown ones
 *       NO_EXISTE_PRE_COMPROBANTE (one message per input occurrence); every other estado is staged. The centralization
 *       types of rejected pre-vouchers are still reported.</li>
 *   <li>Detallado lines come from the events (one line per event), Agrupado lines from the summarised asientos. Both
 *       queries run over ALL accepted pre-vouchers when any definition of that format exists, so a batch mixing
 *       formats stages each pre-voucher in both formats (legacy defect kept, CONT-B-05).</li>
 *   <li>Missing SAP configuration answers ERROR ARCHIVO_CONFIGURACION_NO_ENCONTRADO with counter 1 and writes nothing.</li>
 * </ul>
 * Missing required platform parameters and database failures propagate, as in legacy.
 */
@Service
public class EnvioContabilidadService {

    private static final Logger log = LoggerFactory.getLogger(EnvioContabilidadService.class);

    static final String ARCHIVO_CONFIGURACION_NO_ENCONTRADO = "ARCHIVO_CONFIGURACION_NO_ENCONTRADO";
    /** Legacy literal, including the U+FFFD left by a mis-encoded "ó" (EnvioContabilidad.java:361). */
    static final String TEXTO_CONFIGURACION_NO_ENCONTRADA = "Archivo de configuraci�n no encontrado";
    private static final String CONTABILIDAD = "CONTABILIDAD";
    private static final int LARGO_LINE_MEMO = 50;
    private static final long BPL_ID = 4L;
    private static final long TIPO_CENTRALIZACION_TESORERIA = 210L;

    private final String SQL_SELECT_PRECOMPROBANTE = SqlResources.load("cl/bcs/contabilidad/sql/EnvioContabilidad_SelectPreComprobante.sql");
    private final String SQL_SELECT_HOMOLOGACION_CAMPOS = SqlResources.load("cl/bcs/contabilidad/sql/EnvioContabilidad_SelectHomologacionCampos.sql");
    private final String SQL_SELECT_DEFINICION_DETALLADO = SqlResources.load("cl/bcs/contabilidad/sql/EnvioContabilidad_SelectDefinicionAsientoDetallado.sql");
    private final String SQL_SELECT_DEFINICION_RESUMIDO = SqlResources.load("cl/bcs/contabilidad/sql/EnvioContabilidad_SelectDefinicionAsientoResumido.sql");
    private final String SQL_SELECT_ASIENTO_DETALLADO = SqlResources.load("cl/bcs/contabilidad/sql/EnvioContabilidad_SelectAsientoProcesoDetallado.sql");
    private final String SQL_SELECT_ASIENTO_RESUMIDO = SqlResources.load("cl/bcs/contabilidad/sql/EnvioContabilidad_SelectAsientoProcesoResumido.sql");
    private final String SQL_SELECT_PRECOMPROBANTE_CREACION = SqlResources.load("cl/bcs/contabilidad/sql/EnvioContabilidad_SelectPrecomprobanteCreacion.sql");
    private final String SQL_SELECT_MOVIMIENTO_DATO = SqlResources.load("cl/bcs/contabilidad/sql/EnvioContabilidad_SelectMovimientoDato.sql");
    private final String SQL_INSERT_PRECOMPROBANTE = SqlResources.load("cl/bcs/contabilidad/sql/EnvioContabilidad_InsertPreComprobanteEnvioContabilidad.sql");
    private final String SQL_INSERT_ASIENTO = SqlResources.load("cl/bcs/contabilidad/sql/EnvioContabilidad_InsertPreComprobanteAsientoEnvioContabilidad.sql");
    private final String SQL_INSERT_CENTRALIZACION_TIPO = SqlResources.load("cl/bcs/contabilidad/sql/EnvioContabilidad_InsertPreComprobanteCentralizacionTipo.sql");

    private final NamedParameterJdbcTemplate jdbc;
    private final PlanCuentasSapPort planCuentas;
    private final PlataformaPort plataforma;
    private final DiccionarioPort diccionario;

    public EnvioContabilidadService(NamedParameterJdbcTemplate jdbc, PlanCuentasSapPort planCuentas,
                                    PlataformaPort plataforma, DiccionarioPort diccionario) {
        this.jdbc = jdbc;
        this.planCuentas = planCuentas;
        this.plataforma = plataforma;
        this.diccionario = diccionario;
    }

    // ------------------------------------------------------------------ contract

    record SecuenciaComprobante(@SerializedName("SecuenciaComprobante") Long secuenciaComprobante) {}

    record Entrada(@SerializedName("p_IdEmpresa") Long idEmpresa,
                   @SerializedName("p_IdUsuario") Long idUsuario,
                   @SerializedName("p_IdModulo") String idModulo,
                   @SerializedName("p_Destino") String destino,
                   @SerializedName("p_UsaReglasNegocio") String usaReglasNegocio,
                   @SerializedName("p_SecCmbte") List<SecuenciaComprobante> secCmbte) {
        List<Long> secuencias() {
            return secCmbte == null ? List.of() : secCmbte.stream().map(SecuenciaComprobante::secuenciaComprobante).toList();
        }
    }

    /** JavaCall output of EnvioContabilidad (legacy SalidaEnvioContabilidad, :98-117). */
    public record Resultado(@SerializedName("P_OUT_MENSAJES") List<MensajeSalida> mensajes,
                            @SerializedName("P_OUT_ERRORESCONTADOR") long erroresContador,
                            @SerializedName("P_OUT_PRECOMPROBANTE") List<PreComprobanteSalida> precomprobantes,
                            @SerializedName("P_OUT_ASIENTOS") List<AsientoEnvio> asientos,
                            @SerializedName("P_OUT_TIPOCENTRALIZACION") List<Long> tiposCentralizacion) {}

    // ------------------------------------------------------------------ rows

    private record PreComprobanteProceso(Long idPreComprobante, String estado, Long idTipoCentralizacion) {}

    private record Definicion(String idCuentaContable, String conRut) {}

    private record CampoHomologado(Long idTipoCentralizacion, String campoSap, String campoTipoCentralizacion) {}

    private record MovimientoDato(Long idMovimiento, String idTipoDatoEvento, String valorVarchar, BigDecimal valorDecimal,
                                  String tipoDato) {
        boolean numerico() { return "N".equals(tipoDato); }
    }

    private record AsientoProceso(Long idMovimiento, Long idTipoContabilidad, Long idTipoCentralizacion, Long idPreComprobante,
                                  String codigoMoneda, BigDecimal monto, BigDecimal montoDebe, BigDecimal montoHaber,
                                  String glosa, String glosaDetalle, String centroCosto, String documentoIdentidad,
                                  Long cuenta, String fechaContable, String cuentaContabilidad, String codigoContabilidad,
                                  String idCuentaContable) {}

    /** Platform parameters read once per call (EnvioContabilidad.java:331-351). */
    private record Configuracion(boolean usaCuentasSys, String monedaSistema, String prefijoCliente,
                                 String prefijoContraparte, String prefijoProveedor, boolean usaCentroCosto,
                                 boolean calculaParidadTesoreria) {
        String cuentaSap(AsientoProceso a) {
            return usaCuentasSys ? a.codigoContabilidad() : a.cuentaContabilidad();
        }
    }

    // ------------------------------------------------------------------ entry points

    @Transactional
    public JsonObject execute(JsonObject params) {
        Entrada e = JavaCallJson.read(JavaCallJson.legacy(), params, Entrada.class);
        return JavaCallJson.write(JavaCallJson.legacy(),
                enviar(e.idEmpresa(), e.idUsuario(), e.idModulo(), e.destino(), e.usaReglasNegocio(), e.secuencias()));
    }

    /**
     * Stages the pre-vouchers {@code secuencias} (input order, duplicates allowed). {@code idUsuario}, {@code idModulo},
     * {@code destino} and {@code usaReglasNegocio} are accepted for contract fidelity; legacy does not use them.
     */
    @Transactional
    public Resultado enviar(Long idEmpresa, Long idUsuario, String idModulo, String destino, String usaReglasNegocio,
                            List<Long> secuencias) {
        Configuracion cfg = configuracion(idEmpresa);
        List<CuentaSap> cuentas;
        try {
            cuentas = planCuentas.consultarPlanCuentas();
        } catch (PlanCuentasSapPort.ConfiguracionNoEncontradaException ex) {
            log.error("EnvioContabilidad: SAP configuration not found: {}", ex.getMessage());
            return new Resultado(List.of(new MensajeSalida(0L, MensajeSalida.ERROR, ARCHIVO_CONFIGURACION_NO_ENCONTRADO,
                    TEXTO_CONFIGURACION_NO_ENCONTRADA)), 1L, List.of(), List.of(), List.of());
        }
        // Legacy Collectors.toMap: a repeated AcctCode in OACT fails the call (EnvioContabilidad.java:376).
        Map<String, CuentaSap> plan = cuentas.stream().collect(Collectors.toMap(CuentaSap::AcctCode, c -> c, (a, b) -> {
            throw new IllegalStateException("Duplicate AcctCode in SAP chart of accounts: " + a.AcctCode());
        }));

        List<MensajeSalida> mensajes = new ArrayList<>();
        List<Long> tipos = new ArrayList<>();
        List<Long> aProcesar = new ArrayList<>();
        List<MapSqlParameterSource> tiposPorPreComprobante = new ArrayList<>();
        List<PreComprobanteProceso> existentes = consultarPreComprobantes(secuencias);
        for (Long secuencia : secuencias) {
            PreComprobanteProceso pre = null;
            for (PreComprobanteProceso p : existentes) {
                if (Objects.equals(p.idPreComprobante(), secuencia)) {
                    pre = p; // legacy keeps the LAST row of a pre-voucher with several centralization types
                    if (!tipos.contains(p.idTipoCentralizacion())) tipos.add(p.idTipoCentralizacion());
                }
            }
            Map<String, String> valores = Map.of("NumeroPreComprobante", String.valueOf(secuencia));
            if (pre == null) {
                mensajes.add(mensaje(idEmpresa, secuencia, MensajeSalida.ADVERTENCIA, "NO_EXISTE_PRE_COMPROBANTE", valores));
            } else if ("C".equals(pre.estado()) || "A".equals(pre.estado())) {
                // legacy :407 tests Estado 'D' here, which can never hold: always this code
                mensajes.add(mensaje(idEmpresa, secuencia, MensajeSalida.ADVERTENCIA, "PRE_COMPROBANTE_CONTABILIZADO_O_ANULADO", valores));
            } else {
                if (!aProcesar.contains(secuencia)) aProcesar.add(secuencia);
                tiposPorPreComprobante.add(new MapSqlParameterSource("IdPreComprobante", pre.idPreComprobante())
                        .addValue("IdTipoCentralizacion", pre.idTipoCentralizacion()));
            }
        }

        List<PreComprobanteSalida> cabeceras = new ArrayList<>();
        List<AsientoEnvio> asientos = new ArrayList<>();
        if (!aProcesar.isEmpty()) {
            MapSqlParameterSource porPreComprobante = new MapSqlParameterSource("IdPrecomprobante", aProcesar);
            MapSqlParameterSource porPreYEmpresa = new MapSqlParameterSource("IdPrecomprobante", aProcesar).addValue("IdEmpresa", idEmpresa);
            Map<String, Definicion> detallado = definiciones(SQL_SELECT_DEFINICION_DETALLADO, porPreYEmpresa);
            Map<String, Definicion> resumido = definiciones(SQL_SELECT_DEFINICION_RESUMIDO, porPreYEmpresa);
            List<CampoHomologado> campos = camposHomologados(tipos);
            cabeceras.addAll(jdbc.query(SQL_SELECT_PRECOMPROBANTE_CREACION, porPreComprobante,
                    new DataClassRowMapper<>(PreComprobanteSalida.class)));
            if (!detallado.isEmpty()) {
                asientos.addAll(lineasDetalladas(porPreComprobante, detallado, campos, plan, cfg));
            }
            if (!resumido.isEmpty()) {
                jdbc.query(SQL_SELECT_ASIENTO_RESUMIDO, porPreComprobante, ASIENTO_PROCESO)
                        .forEach(a -> asientos.add(lineaAgrupada(a, cfg)));
            }
        }

        jdbc.batchUpdate(SQL_INSERT_PRECOMPROBANTE, cabeceras.stream().map(PreComprobanteSalida::paraInsertar)
                .map(SimplePropertySqlParameterSource::new).toArray(SimplePropertySqlParameterSource[]::new));
        jdbc.batchUpdate(SQL_INSERT_ASIENTO, asientos.stream().map(SimplePropertySqlParameterSource::new)
                .toArray(SimplePropertySqlParameterSource[]::new));
        jdbc.batchUpdate(SQL_INSERT_CENTRALIZACION_TIPO, SqlParams.batch(tiposPorPreComprobante));
        return new Resultado(mensajes, 0L, cabeceras, asientos, tipos);
    }

    // ------------------------------------------------------------------ queries

    private Configuracion configuracion(Long idEmpresa) {
        return new Configuracion(
                "S".equals(requerido(idEmpresa, CONTABILIDAD, "UsaCuentasSYS")),
                requeridoNoNulo(plataforma.obtenerMonedaSistema(idEmpresa), "PLATAFORMA", "PLA.MonedaSistema"),
                requerido(idEmpresa, CONTABILIDAD, "PrefijoClienteSAP"),
                requerido(idEmpresa, CONTABILIDAD, "PrefijoContraparteSAP"),
                requerido(idEmpresa, CONTABILIDAD, "PrefijoProveedorSAP"),
                "S".equals(plataforma.obtenerParametroConfiguracion(idEmpresa, CONTABILIDAD, "UsaCentroCosto", "N")),
                "S".equalsIgnoreCase(plataforma.obtenerParametroConfiguracion(idEmpresa, CONTABILIDAD,
                        "CalculaParidadCLPCentralizacionTesoreria", "N")));
    }

    private String requerido(Long idEmpresa, String modulo, String parametro) {
        return requeridoNoNulo(plataforma.obtenerParametroConfiguracion(idEmpresa, modulo, parametro, null), modulo, parametro);
    }

    /** Legacy ConfiguracionSistema throws when a parameter does not exist. */
    private static String requeridoNoNulo(String valor, String modulo, String parametro) {
        if (valor == null) throw new IllegalStateException("Parametro " + parametro + " en modulo " + modulo + " no encontrado");
        return valor;
    }

    private List<PreComprobanteProceso> consultarPreComprobantes(List<Long> secuencias) {
        List<Long> distintas = secuencias.stream().filter(Objects::nonNull).distinct().toList();
        if (distintas.isEmpty()) return List.of(); // legacy IN (NULL): nothing matches
        return jdbc.query(SQL_SELECT_PRECOMPROBANTE, new MapSqlParameterSource("IdPrecomprobante", distintas),
                SqlRow.mapper(r -> new PreComprobanteProceso(r.lng("IdPreComprobante"), r.str("Estado"), r.lng("IdTipoCentralizacion"))));
    }

    private Map<String, Definicion> definiciones(String sql, MapSqlParameterSource params) {
        Map<String, Definicion> porCuenta = new HashMap<>();
        jdbc.query(sql, params, SqlRow.mapper(r -> new Definicion(r.str("IdCuentaContable"), r.str("ConRut"))))
                .forEach(d -> porCuenta.put(d.idCuentaContable(), d));
        return porCuenta;
    }

    private List<CampoHomologado> camposHomologados(List<Long> tipos) {
        List<Long> conValor = tipos.stream().filter(Objects::nonNull).toList();
        if (conValor.isEmpty()) return List.of();
        return jdbc.query(SQL_SELECT_HOMOLOGACION_CAMPOS, new MapSqlParameterSource("IdTipoCentralizacion", conValor),
                SqlRow.mapper(r -> new CampoHomologado(r.lng("IdTipoCentralizacion"), r.str("CampoSAP"), r.str("CampoTipoCentralizacion"))));
    }

    private static final org.springframework.jdbc.core.RowMapper<AsientoProceso> ASIENTO_PROCESO = SqlRow.mapper(r -> new AsientoProceso(
            r.lng("IdMovimiento"), r.lng("IdTipoContabilidad"), r.lng("IdTipoCentralizacion"), r.lng("IdPreComprobante"),
            r.str("CodigoMoneda"), r.dec("Monto"), r.dec("MontoDebe"), r.dec("MontoHaber"), r.str("Glosa"),
            r.str("GlosaDetalle"), r.str("CentroCosto"), r.str("DocumentoIdentidad"), r.lng("Cuenta"), r.str("FechaContable"),
            r.str("CuentaContabilidad"), r.str("CodigoContabilidad"), r.str("IdCuentaContable")));

    // ------------------------------------------------------------------ Detallado (EnvioContabilidad.java:438-625)

    private List<AsientoEnvio> lineasDetalladas(MapSqlParameterSource porPreComprobante, Map<String, Definicion> definiciones,
                                                List<CampoHomologado> campos, Map<String, CuentaSap> plan, Configuracion cfg) {
        List<AsientoProceso> asientos = jdbc.query(SQL_SELECT_ASIENTO_DETALLADO, porPreComprobante, ASIENTO_PROCESO);
        Map<Long, List<MovimientoDato>> datosPorMovimiento = jdbc.query(SQL_SELECT_MOVIMIENTO_DATO, porPreComprobante,
                        SqlRow.mapper(r -> new MovimientoDato(r.lng("IdMovimiento"), r.str("IdTipoDatoEvento"),
                                r.str("ValorVarchar"), r.dec("ValorDecimal"), r.str("TipoDato"))))
                .stream().filter(d -> d.idMovimiento() != null)
                .collect(Collectors.groupingBy(MovimientoDato::idMovimiento));
        Map<Long, List<CampoHomologado>> camposPorTipo = campos.stream().filter(c -> c.idTipoCentralizacion() != null)
                .collect(Collectors.groupingBy(CampoHomologado::idTipoCentralizacion));

        List<AsientoEnvio> lineas = new ArrayList<>();
        for (AsientoProceso a : asientos) {
            lineas.add(lineaDetallada(a, definiciones.get(a.idCuentaContable()),
                    datosPorMovimiento.getOrDefault(a.idMovimiento(), List.of()),
                    camposPorTipo.getOrDefault(a.idTipoCentralizacion(), List.of()), plan, cfg));
        }
        return lineas;
    }

    private static AsientoEnvio lineaDetallada(AsientoProceso a, Definicion definicion, List<MovimientoDato> datos,
                                               List<CampoHomologado> campos, Map<String, CuentaSap> plan, Configuracion cfg) {
        Linea l = new Linea();
        l.idTipoContabilidad = a.idTipoContabilidad();
        l.parentKey = a.idPreComprobante();
        l.set("TieneProject", definicion != null ? definicion.conRut() : "");
        l.set("Project", definicion != null && "S".equals(definicion.conRut()) ? a.documentoIdentidad() : "");
        l.set("U_glosa", a.glosa());
        l.set("LineMemo", cortar(Objects.requireNonNullElse(a.glosaDetalle(), ""), LARGO_LINE_MEMO));
        l.set("ProfitCode", a.centroCosto());
        l.set("AccountCode", cfg.cuentaSap(a));
        l.set("Formato", AsientoEnvio.FORMATO_DETALLADO);
        l.bplId = BPL_ID;
        l.set("Reference1", a.documentoIdentidad());
        l.set("Reference2", String.valueOf(a.cuenta()));
        l.set("ReferenceDate1", a.fechaContable());
        l.idMovimiento = a.idMovimiento();

        CuentaSap cuenta = plan.get(l.get("AccountCode"));
        boolean analitica = cuenta != null && cuenta.esAnalitica();
        boolean centroCosto = cuenta != null && cuenta.usaCentroCosto();
        BigDecimal montoMonedaSistema = BigDecimal.ZERO;

        for (MovimientoDato dato : datos) {
            for (CampoHomologado campo : campos) {
                if (campo.campoTipoCentralizacion() == null || !campo.campoTipoCentralizacion().equals(dato.idTipoDatoEvento())) continue;
                if ("MontoMonedaSistema".equals(campo.campoSap())) {
                    BigDecimal valor = dato.numerico() ? dato.valorDecimal() : new BigDecimal(dato.valorVarchar());
                    montoMonedaSistema = valor.abs(MathContext.DECIMAL128);
                    continue;
                }
                homologar(l, campo.campoSap(), dato);
            }
            aplicarCasoEspecial(l, dato, a.documentoIdentidad(), analitica, cfg);
            if (cfg.usaCentroCosto() && centroCosto && CAMPOS_CENTRO_COSTO.contains(dato.idTipoDatoEvento())) {
                l.set(dato.idTipoDatoEvento(), dato.valorVarchar());
            }
        }
        if (l.get("ShortName").isEmpty() && analitica) {
            l.set("ShortName", "C" + a.documentoIdentidad()); // hardcoded "C", not PrefijoClienteSAP (CONT-B-07)
            l.set("TipoSocioNegocio", "CLIENTE");
        }

        boolean monedaLocal = cfg.monedaSistema().equals(a.codigoMoneda())
                || (cfg.calculaParidadTesoreria() && Long.valueOf(TIPO_CENTRALIZACION_TESORERIA).equals(a.idTipoCentralizacion()));
        BigDecimal monto = a.monto() == null ? null : a.monto().abs(MathContext.DECIMAL128);
        if (a.montoDebe().signum() > 0) {
            if (monedaLocal) {
                l.debit = monto;
                l.fcDebit = BigDecimal.ZERO;
                l.set("FCCurrency", "");
            } else {
                l.fcDebit = monto;
                l.debit = montoMonedaSistema;
                l.set("FCCurrency", a.codigoMoneda());
            }
        } else {
            l.debit = BigDecimal.ZERO;
            l.fcDebit = BigDecimal.ZERO;
        }
        if (a.montoHaber().signum() > 0) {
            if (monedaLocal) {
                l.credit = monto;
                l.fcCredit = BigDecimal.ZERO;
                l.set("FCCurrency", "");
            } else {
                l.fcCredit = monto;
                l.credit = montoMonedaSistema;
                l.set("FCCurrency", a.codigoMoneda());
            }
        } else {
            l.credit = BigDecimal.ZERO;
            l.fcCredit = BigDecimal.ZERO;
        }
        return l.build();
    }

    /**
     * CamposHomologadosSap: the movement datum goes to the String field named CampoSAP. Legacy used reflection and
     * silently ignored unknown or non-String fields and a failed value conversion (EnvioContabilidad.java:496-504).
     */
    private static void homologar(Linea l, String campoSap, MovimientoDato dato) {
        if (!Linea.esCampoTexto(campoSap)) {
            log.debug("EnvioContabilidad: homologated field {} is not a text field of the line; ignored", campoSap);
            return;
        }
        if (dato.numerico() && dato.valorDecimal() == null) return;
        l.set(campoSap, dato.numerico() ? dato.valorDecimal().toPlainString() : dato.valorVarchar());
    }

    private static final Set<String> CAMPOS_CENTRO_COSTO = Set.of("CostingCode", "CostingCode2", "CostingCode3", "CostingCode4", "CostingCode5");

    /** EnvioContabilidad.java:509-556. */
    private static void aplicarCasoEspecial(Linea l, MovimientoDato dato, String rut, boolean analitica, Configuracion cfg) {
        String valor = dato.valorVarchar();
        switch (dato.idTipoDatoEvento()) {
            case "ProductoMovimiento", "RelacionCorredora", "CodSecuencia" -> l.set(dato.idTipoDatoEvento(), valor);
            case "EsContraparte" -> {
                if (l.get("ShortName").isEmpty()) {
                    boolean contraparte = "SI".equals(valor);
                    l.set("ShortName", analitica ? (contraparte ? cfg.prefijoContraparte() : cfg.prefijoCliente()) + rut : "");
                    l.set("TipoSocioNegocio", contraparte ? "CONTRAPARTE" : "CLIENTE");
                }
            }
            case "EsProveedor" -> {
                if ("SI".equals(valor)) {
                    l.set("ShortName", analitica ? cfg.prefijoProveedor() + rut : "");
                    l.set("TipoSocioNegocio", "PROVEEDOR");
                }
            }
            case "FolioDTE" -> {
                l.set("U_BCS_FolioDTE", valor);
                if (!"0".equals(valor)) {
                    String sufijo = "/" + valor;
                    String base = Objects.requireNonNullElse(l.get("LineMemo"), "");
                    l.set("LineMemo", cortar(base, Math.max(0, LARGO_LINE_MEMO - sufijo.length())) + sufijo);
                }
            }
            default -> { /* not a special datum */ }
        }
    }

    // ------------------------------------------------------------------ Agrupado (EnvioContabilidad.java:629-660)

    private static AsientoEnvio lineaAgrupada(AsientoProceso a, Configuracion cfg) {
        Linea l = new Linea();
        l.idTipoContabilidad = a.idTipoContabilidad();
        l.parentKey = a.idPreComprobante();
        l.debit = a.montoDebe();
        l.credit = a.montoHaber();
        l.fcDebit = BigDecimal.ZERO;
        l.fcCredit = BigDecimal.ZERO;
        l.set("FCCurrency", "");
        l.set("U_glosa", a.glosa());
        l.set("LineMemo", a.glosaDetalle()); // the SQL already cuts it at 50
        l.set("ProfitCode", a.centroCosto());
        l.bplId = BPL_ID;
        l.set("AccountCode", cfg.cuentaSap(a));
        l.set("Formato", AsientoEnvio.FORMATO_AGRUPADO);
        l.set("DueDate", a.fechaContable());
        l.set("TieneProject", "N");
        l.set("Project", "");
        l.set("ReferenceDate1", "");
        l.idMovimiento = 0L;
        return l.build();
    }

    // ------------------------------------------------------------------ helpers

    private MensajeSalida mensaje(Long idEmpresa, Long id, String tipo, String codigo, Map<String, String> valores) {
        return new MensajeSalida(id, tipo, codigo, diccionario.mensaje(idEmpresa, codigo, valores));
    }

    private static String cortar(String texto, int largo) {
        return texto.length() > largo ? texto.substring(0, largo) : texto;
    }

    /**
     * A line being built. Text fields live in a map keyed by their {@link AsientoEnvio} component name so the
     * CamposHomologadosSap rule can target any of them by name; the defaults are legacy's (EnvioContabilidad.java:182-218):
     * "" except U_glosa, LineMemo, ProfitCode, AccountCode, Formato, FCCurrency and ReferenceDate1 (null).
     */
    private static final class Linea {

        private static final Set<String> CAMPOS_TEXTO = Arrays.stream(AsientoEnvio.class.getRecordComponents())
                .filter(c -> c.getType() == String.class).map(RecordComponent::getName)
                .collect(Collectors.toUnmodifiableSet());
        private static final Set<String> SIN_VALOR_INICIAL = Set.of("U_glosa", "LineMemo", "ProfitCode", "AccountCode",
                "Formato", "FCCurrency", "ReferenceDate1");

        Long idTipoContabilidad;
        Long parentKey;
        Long bplId;
        Long idMovimiento;
        BigDecimal debit;
        BigDecimal credit;
        BigDecimal fcDebit;
        BigDecimal fcCredit;
        private final Map<String, String> texto = new LinkedHashMap<>();

        Linea() {
            CAMPOS_TEXTO.forEach(c -> texto.put(c, SIN_VALOR_INICIAL.contains(c) ? null : ""));
        }

        static boolean esCampoTexto(String nombre) {
            return nombre != null && CAMPOS_TEXTO.contains(nombre);
        }

        void set(String campo, String valor) {
            if (!CAMPOS_TEXTO.contains(campo)) throw new IllegalArgumentException("not a text field: " + campo);
            texto.put(campo, valor);
        }

        String get(String campo) {
            if (!CAMPOS_TEXTO.contains(campo)) throw new IllegalArgumentException("not a text field: " + campo);
            return texto.get(campo);
        }

        AsientoEnvio build() {
            return new AsientoEnvio(idTipoContabilidad, parentKey, debit, credit, get("U_glosa"), get("LineMemo"),
                    get("ProfitCode"), bplId, get("AccountCode"), get("DueDate"), get("Formato"), get("TieneProject"),
                    get("Project"), get("ProductoMovimiento"), get("RelacionCorredora"), get("CodSecuencia"), idMovimiento,
                    get("Reference1"), get("Reference2"), get("AdditionalReference"), fcDebit, fcCredit, get("FCCurrency"),
                    get("ShortName"), get("TipoSocioNegocio"), get("ReferenceDate1"), get("TaxDate"), get("CostingCode"),
                    get("CostingCode2"), get("CostingCode3"), get("CostingCode4"), get("CostingCode5"),
                    get("U_BCS_TipoPersona"), get("U_BCS_Rut"), get("U_BCS_Portafolio"), get("U_BCS_Nombre"),
                    get("U_BCS_CtaDCV"), get("U_BCS_EmitidoA"), get("U_BCS_Comprobante"), get("U_BCS_Folio"),
                    get("U_BCS_NDcto"), get("U_BCS_Concepto"), get("U_BCS_CV"), get("U_BCS_Nemo"),
                    get("U_BCS_IFRSCtaPropia"), get("U_BCS_CodigoProducto"), get("U_BCS_Bolsa"), get("U_BCS_Relacion"),
                    get("U_RelacionCodigoSap"), get("U_BCS_FolioDTE"));
        }
    }
}
