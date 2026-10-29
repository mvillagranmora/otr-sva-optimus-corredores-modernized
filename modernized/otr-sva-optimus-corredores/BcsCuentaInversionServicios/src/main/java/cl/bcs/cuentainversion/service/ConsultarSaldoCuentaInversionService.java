package cl.bcs.cuentainversion.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.cuentainversion.domain.colaborador.CalcularParidad;
import cl.bcs.cuentainversion.domain.colaborador.EntregarPreciosTasas;
import cl.bcs.cuentainversion.domain.colaborador.ObtenerPortafoliosEjecutivo;
import cl.bcs.cuentainversion.port.out.Colaboradores;
import cl.bcs.cuentainversion.port.out.ConfiguracionSistemaPort;
import cl.bcs.cuentainversion.port.out.DiccionarioMensajes;
import cl.bcs.cuentainversion.service.SaldoCuentaInversionService.CuentaInversionAConsultar;
import cl.bcs.cuentainversion.service.SaldoCuentaInversionService.SaldoInicialFinal;
import cl.bcs.cuentainversion.service.SaldoCuentaInversionService.SaldoPorFechaEfectiva;
import cl.bcs.cuentainversion.service.SaldoCuentaInversionService.SaldoPorFechaMovimiento;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Utils;

/**
 * Balance query of the investment accounts for a client, a portfolio, a list of portfolios or every portfolio of an executive
 * (README CTA-A-91..107 and, for the money-market valuation, CTA-A-247..254). It resolves which portfolios the caller may see,
 * delegates the per-day balances to {@link SaldoCuentaInversionService}, and completes each row with the account data, the other
 * balances (guarantees, term commitments, withdrawals, retained, ...), contributions in transit, the money-market valuation
 * (custody quantity x unit value, converted with the exchange parity) and the date of the last movement.
 */
@Service
public class ConsultarSaldoCuentaInversionService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarSaldoCuentaInversionService.class);

    private static final String SQL = "cl/bcs/cuentainversion/sql/ConsultarSaldoCuentaInversion_";
    private static final String SQL_INSERT_CUENTA_PASO = CtaJavaCallInfra.loadSql(SQL + "Insert_Cuenta_Inversion_Tabla_Paso.sql");
    private static final String SQL_INSERT_CUENTA_CLIENTE_PASO = CtaJavaCallInfra.loadSql(SQL + "Insert_Cuenta_Inversion_Cliente_Tabla_Paso.sql");
    private static final String SQL_INSERT_PORTAFOLIO_MONEDA = CtaJavaCallInfra.loadSql(SQL + "Insert_Portafolio_Moneda.sql");
    private static final String SQL_INSERT_PORTAFOLIO_MONEDA_FILTRO = CtaJavaCallInfra.loadSql(SQL + "Insert_Portafolio_Moneda_Filtro.sql");
    private static final String SQL_DELETE_CUENTA_PASO = CtaJavaCallInfra.loadSql(SQL + "Delete_Cuenta_Inversion_Tabla_Paso.sql");
    private static final String SQL_DELETE_CUENTA_SIN_MONEDA = CtaJavaCallInfra.loadSql(SQL + "Delete_Cuenta_Sin_Moneda.sql");
    private static final String SQL_SELECT_PORTAFOLIOS_PASO = CtaJavaCallInfra.loadSql(SQL + "Select_Portafolios_Paso.sql");
    private static final String SQL_INSERT_PERMITIDOS = CtaJavaCallInfra.loadSql(SQL + "Insert_Portafolios_Permitidos.sql");
    private static final String SQL_DELETE_NO_PERMITIDAS = CtaJavaCallInfra.loadSql(SQL + "Delete_Cuentas_No_Permitidas.sql");
    private static final String SQL_DELETE_PERMITIDOS = CtaJavaCallInfra.loadSql(SQL + "Delete_Portafolios_Permitidos.sql");
    private static final String SQL_SELECT_ID_ENTIDAD = CtaJavaCallInfra.loadSql(SQL + "Select_Id_Empleado.sql");
    private static final String SQL_SELECT_INFO_CUENTA = CtaJavaCallInfra.loadSql(SQL + "Select_Info_Cuenta_Inversion.sql");
    private static final String SQL_SELECT_SOLICITUDES = CtaJavaCallInfra.loadSql(SQL + "Select_SolicitudesFFMM.sql");
    private static final String SQL_GARANTIA = CtaJavaCallInfra.loadSql(SQL + "Select_Saldo_En_Garantia.sql");
    private static final String SQL_COMPROMISOS = CtaJavaCallInfra.loadSql(SQL + "Select_Saldo_A_Plazo_Compromisos.sql");
    private static final String SQL_SIMULTANEAS = CtaJavaCallInfra.loadSql(SQL + "Select_Saldo_A_Plazo_Simultaneas.sql");
    private static final String SQL_SIMULTANEAS_HISTORICO = CtaJavaCallInfra.loadSql(SQL + "Select_Historico_Saldo_A_Plazo_Simultaneas.sql");
    private static final String SQL_PRESTAMOS_EMPRESTITOS = CtaJavaCallInfra.loadSql(SQL + "Select_Saldo_A_Plazo_PrestamosEmprestitos.sql");
    private static final String SQL_TESORERIA_EFECTIVA = CtaJavaCallInfra.loadSql(SQL + "Select_SaldoFechaEfectiva_Tesoreria.sql");
    private static final String SQL_TESORERIA_EFECTIVA_INGRESADAS =
            CtaJavaCallInfra.loadSql(SQL + "Select_SaldoFechaEfectiva_Tesoreria_Partidas_Estado_Ingresado.sql");
    private static final String SQL_TESORERIA_MOVIMIENTO = CtaJavaCallInfra.loadSql(SQL + "Select_SaldoFechaMovimiento_Tesoreria.sql");
    private static final String SQL_RETENIDOS = CtaJavaCallInfra.loadSql(SQL + "Select_Saldos_Retenidos.sql");
    private static final String SQL_PENDIENTE_LIBERACION_DAP = CtaJavaCallInfra.loadSql(SQL + "Select_Saldos_Pend_Liberacion_DAP.sql");
    private static final String SQL_MONEY_MARKET = CtaJavaCallInfra.loadSql(SQL + "Select_Instrumentos_Money_Market.sql");
    private static final String SQL_SERIE_FONDO = CtaJavaCallInfra.loadSql(SQL + "Select_SerieFondo.sql");
    private static final String SQL_CUSTODIA = CtaJavaCallInfra.loadSql(SQL + "Select_Cantidad_Custodia.sql");
    private static final String SQL_CUSTODIA_HISTORICA = CtaJavaCallInfra.loadSql(SQL + "Select_Cantidad_CustodiaHistorica.sql");
    private static final String SQL_INSERT_ULTIMO_MOVIMIENTO = CtaJavaCallInfra.loadSql(SQL + "Insert_Ultimo_Movimiento_Tabla_Paso.sql");
    private static final String SQL_SELECT_ULTIMO_MOVIMIENTO = CtaJavaCallInfra.loadSql(SQL + "Select_Ultimo_Movimiento.sql");

    /** Default product-grouper filter of the request: "no filter" for accounts, but still sent as-is to the contributions query. */
    private static final long SIN_PRODUCTO_AGRUPADOR = 9999L;

    // ---------------------------------------------------------------------------------------------------- contract

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public List<CuentaInversionAConsultar> p_CuentaInversionAConsultar = new ArrayList<>();
        public String p_FechaDesde;
        public String p_FechaHasta;
        public String p_SoloSaldoNegativo = "N";
        public String p_SoloSaldoDisponible = "N";
        public String p_SoloSaldoDisponibleNegativo = "N";
        public String p_IncluyeCargosPendientes = "N";
        public String p_IncluyeAbonosPendientes = "N";
        public String p_IncluyeOtrosSaldos = "N";
        public Long p_IdPortafolio;
        public String p_IncluyeProductoApv = "N";
        public Long p_IdEjecutivo;
        public Long p_IdCliente;
        public Long p_IdProductoAgrupador = SIN_PRODUCTO_AGRUPADOR;
        public String p_IdMoneda;
        /** TODO, CONTABLE, DISPONIBLE, RETENIDO, TRANSITO, PENDIENTE. */
        public List<String> p_TipoSaldoConsultar = new ArrayList<>(Arrays.asList("TODO"));
    }

    public static class Salida {
        public List<SaldoInicialFinal> P_OUT_SALDOINICIALFINAL = new ArrayList<>();
        public List<SaldoPorFechaEfectiva> P_OUT_SALDOPORFECHAEFECTIVA = new ArrayList<>();
        public List<SaldoPorFechaMovimiento> P_OUT_SALDOPORFECHAMOVIMIENTO = new ArrayList<>();
        public Long P_OUT_INFORMACIONCONTADOR;
        public List<InformacionDetalle> P_OUT_INFORMACIONDETALLE = new ArrayList<>();
        public Long P_OUT_ADVERTENCIASCONTADOR;
        public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
        public List<ErrorDetalle> P_OUT_ERRORESDETALLE = new ArrayList<>();
    }

    public static class InformacionDetalle {
        public String Codigo;
        public String Descripcion;
    }

    public static class AdvertenciaDetalle {
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;
    }

    public static class ErrorDetalle {
        public String Codigo;
        public String Descripcion;
    }

    // ---------------------------------------------------------------------------------------------------- internals

    /** One portfolio/currency row of the working table of the request. */
    private record CuentaConsulta(Long idEjecutivo, Long idPortafolio, String idMoneda) {
    }

    private record Info(Long idCuentaInversion, Long idEjecutivo, String nombreEjecutivo, String codigoSucursal, String nombreSucursal,
                        String documentoIdentidad, String nombreCliente, String nombrePortafolio, String propositoPortafolio,
                        String codigoPortafolio, String codigoMoneda, String nombreMoneda, String idMoneda, Long idPortafolio,
                        String producto, Long idMatrizAgrupador, String esAdc, String relacion, String tipoCliente,
                        String codigoEjecutivo, Long idProductoAgrupador) {
    }

    private record OtroSaldo(Long idCliente, Long idPortafolio, String idMoneda, BigDecimal monto, String fechaSaldo) {
        OtroSaldo conMonto(BigDecimal nuevo) {
            return new OtroSaldo(idCliente, idPortafolio, idMoneda, nuevo, fechaSaldo);
        }
    }

    private record Solicitud(BigDecimal cantidadMontoAporte, Long idCliente, Long idPortafolio, String idMonedaFondo, String fechaEmision) {
    }

    private record Custodia(Long idPortafolio, String idMoneda, BigDecimal cantidad, String nemotecnico) {
    }

    private record ValorCuota(String idSerieFondo, BigDecimal valorCuota) {
    }

    private record UltimoMovimiento(String clave, java.sql.Date fecha) {
    }

    /** Answer of ObtenerPortafoliosEjecutivo: its legacy result is a list, exchanged as {@code {"valor": [...]}}. */
    private static class ListaPortafolios {
        public List<Long> valor = new ArrayList<>();
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final SaldoCuentaInversionService saldoService;
    private final ConfiguracionSistemaPort configuracion;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;

    public ConsultarSaldoCuentaInversionService(NamedParameterJdbcTemplate jdbc, SaldoCuentaInversionService saldoService,
                                                ConfiguracionSistemaPort configuracion, DiccionarioMensajes diccionario,
                                                Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.saldoService = saldoService;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
    }

    @Transactional
    public JsonObject execute(JsonObject params) throws Exception {
        Entrada entrada = Utils.getGsonBuilder().fromJson(params, Entrada.class);
        return Utils.getGsonBuilder().toJsonTree(consultar(entrada)).getAsJsonObject();
    }

    /** Typed entry point for in-module callers. */
    @Transactional
    public Salida consultar(Entrada entrada) throws Exception {
        log.info("INICIO::ConsultarSaldoCuentaInversion");
        Salida out = new Salida();
        List<String> tipos = entrada.p_TipoSaldoConsultar;
        if (tipos.isEmpty()) {
            tipos.add("TODO");
        }

        // who is asking decides which portfolios are visible: executive of the request, of its first element, or of the user
        if (entrada.p_IdEjecutivo == null) {
            if (!entrada.p_CuentaInversionAConsultar.isEmpty() && entrada.p_CuentaInversionAConsultar.get(0).IdEjecutivo != null) {
                entrada.p_IdEjecutivo = entrada.p_CuentaInversionAConsultar.get(0).IdEjecutivo;
            } else {
                List<Map<String, Object>> entidades = jdbc.queryForList(SQL_SELECT_ID_ENTIDAD,
                        new MapSqlParameterSource("p_IdUsuario", entrada.p_IdUsuario));
                if (entidades.isEmpty()) {
                    return errorDeEntrada(out, entrada, "UI_ENTRADA_SIN_ID_EJECUTIVO_EMPLEADO");
                }
                // LEGACY DEFECT kept (see README CTA-A-92): the entity id column is not read (the SQL does not alias it as IdEntidad),
                // so an employee user never gets an executive id and the portfolio filter is skipped
                entrada.p_IdEjecutivo = "CLI".equalsIgnoreCase((String) entidades.get(0).get("Tipo")) ? Long.valueOf(-1L) : null;
            }
        }

        String fechaSistema = CtaJavaCallInfra.parametro(configuracion, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        String monedaSistema = CtaJavaCallInfra.parametro(configuracion, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");

        // accounts to consult: 1 client without portfolio, 2 one portfolio, 3a a list of portfolios, 3b every portfolio of the executive
        long idTrabajo = ThreadLocalRandom.current().nextLong(10_000_000_000L);
        boolean todosLosPortafolios = false;
        if (entrada.p_IdCliente != null && entrada.p_IdCliente > 0 && entrada.p_IdPortafolio == null) {
            jdbc.update(SQL_INSERT_CUENTA_CLIENTE_PASO, new MapSqlParameterSource()
                    .addValue("p_IdCliente", entrada.p_IdCliente)
                    .addValue("p_IdMoneda", entrada.p_CuentaInversionAConsultar.get(0).IdMoneda)
                    .addValue("p_IdTrabajo", idTrabajo));
            completarMonedas(idTrabajo);
        } else if (entrada.p_IdPortafolio != null) {
            insertarCuentas(List.of(new CuentaConsulta(null, entrada.p_IdPortafolio, entrada.p_CuentaInversionAConsultar.get(0).IdMoneda)), idTrabajo);
            completarMonedas(idTrabajo);
        } else if (entrada.p_IdCliente == null && entrada.p_IdPortafolio == null && !entrada.p_CuentaInversionAConsultar.isEmpty()) {
            Map<List<Object>, CuentaConsulta> portafolios = new LinkedHashMap<>();
            for (CuentaInversionAConsultar c : entrada.p_CuentaInversionAConsultar) {
                if (c.IdPortafolio == null) {
                    continue;
                }
                // LEGACY DEFECT kept (see README CTA-A-91): every row takes the currency of the FIRST element
                CuentaConsulta cc = new CuentaConsulta(c.IdEjecutivo, c.IdPortafolio, entrada.p_CuentaInversionAConsultar.get(0).IdMoneda);
                portafolios.putIfAbsent(Arrays.asList(cc.idEjecutivo(), cc.idMoneda(), cc.idPortafolio()), cc);
            }
            if (!portafolios.isEmpty()) {
                insertarCuentas(new ArrayList<>(portafolios.values()), idTrabajo);
                completarMonedas(idTrabajo);
            } else {
                List<CuentaConsulta> cuentas = new ArrayList<>();
                for (Long idPortafolio : portafoliosPermitidos(consultaPortafolios(entrada, new ArrayList<>(), "N"))) {
                    cuentas.add(new CuentaConsulta(null, idPortafolio, entrada.p_CuentaInversionAConsultar.get(0).IdMoneda));
                }
                insertarCuentas(cuentas, idTrabajo);
                completarMonedas(idTrabajo);
                todosLosPortafolios = true;
            }
        } else {
            return errorDeEntrada(out, entrada, "ENTRADA_INVALIDA");
        }

        // keep only the portfolios the executive is allowed to see (a client user, id -1, sees its own)
        if (!todosLosPortafolios && entrada.p_IdEjecutivo != null && entrada.p_IdEjecutivo != -1) {
            List<Long> enTabla = jdbc.queryForList(SQL_SELECT_PORTAFOLIOS_PASO, new MapSqlParameterSource()).stream()
                    .map(r -> num(r.get("IdPortafolio"))).collect(Collectors.toList());
            List<Long> permitidos = portafoliosPermitidos(consultaPortafolios(entrada, enTabla, "S"));
            jdbc.batchUpdate(SQL_INSERT_PERMITIDOS, permitidos.stream()
                    .map(p -> (SqlParameterSource) new MapSqlParameterSource("IdPortafolio", p).addValue("IdTrabajo", idTrabajo))
                    .toArray(SqlParameterSource[]::new));
            MapSqlParameterSource trabajo = new MapSqlParameterSource("IdTrabajo", idTrabajo);
            jdbc.update(SQL_DELETE_NO_PERMITIDAS, trabajo);
            jdbc.update(SQL_DELETE_PERMITIDOS, trabajo);
        }

        // account information
        Long idProductoAgrupador = entrada.p_IdProductoAgrupador;
        if (idProductoAgrupador != null) {
            idProductoAgrupador = idProductoAgrupador == SIN_PRODUCTO_AGRUPADOR ? Long.valueOf(0L) : entrada.p_IdProductoAgrupador;
        }
        List<Info> infoCuentas = jdbc.queryForList(SQL_SELECT_INFO_CUENTA, new MapSqlParameterSource()
                .addValue("p_IdProductoAgrupador", idProductoAgrupador).addValue("p_IdTrabajo", idTrabajo))
                .stream().map(ConsultarSaldoCuentaInversionService::aInfo).toList();

        boolean soloNoAutomaticas = "S".equals(CtaJavaCallInfra.parametro(configuracion, entrada.p_IdEmpresa, "FONDOMUTUO",
                "WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS"));
        List<Solicitud> solicitudes = new ArrayList<>();
        if (tipos.contains("TODO") || tipos.contains("TRANSITO")) {
            // LEGACY DEFECT kept (see README CTA-A-107): the raw product grouper (9999 by default) is used, so requests in transit match nothing
            solicitudes = jdbc.queryForList(SQL_SELECT_SOLICITUDES, new MapSqlParameterSource()
                    .addValue("IngresoAutomatico", soloNoAutomaticas ? "N" : "")
                    .addValue("FiltraIngresoAutomatico", soloNoAutomaticas ? "S" : "N")
                    .addValue("IdTrabajo", idTrabajo)
                    .addValue("IdProductoAgrupador", entrada.p_IdProductoAgrupador))
                    .stream().map(r -> new Solicitud(dec(r.get("CantidadMontoAporte")), num(r.get("IdCliente")), num(r.get("IdPortafolio")),
                            (String) r.get("IdMonedaFondo"), texto(r.get("FechaEmision")))).toList();
        }

        List<CuentaInversionAConsultar> listaCuentas = infoCuentas.stream().map(p -> {
            CuentaInversionAConsultar n = new CuentaInversionAConsultar();
            n.IdEjecutivo = p.idEjecutivo();
            n.IdMoneda = p.idMoneda();
            n.IdPortafolio = p.idPortafolio();
            return n;
        }).collect(Collectors.toList());

        SaldoCuentaInversionService.Salida salidaSaldo = new SaldoCuentaInversionService.Salida();
        if (!listaCuentas.isEmpty()) {
            SaldoCuentaInversionService.Entrada entradaSaldo = new SaldoCuentaInversionService.Entrada();
            entradaSaldo.p_IdEmpresa = entrada.p_IdEmpresa;
            entradaSaldo.p_FechaDesde = entrada.p_FechaDesde;
            entradaSaldo.p_FechaHasta = entrada.p_FechaHasta;
            entradaSaldo.p_SoloSaldoNegativo = entrada.p_SoloSaldoNegativo;
            entradaSaldo.p_SoloSaldoDisponible = entrada.p_SoloSaldoDisponible;
            entradaSaldo.p_SoloSaldoDisponibleNegativo = entrada.p_SoloSaldoDisponibleNegativo;
            entradaSaldo.p_IncluyeCargosPendientes = entrada.p_IncluyeCargosPendientes;
            entradaSaldo.p_IncluyeAbonosPendientes = entrada.p_IncluyeAbonosPendientes;
            entradaSaldo.p_CuentaInversionAConsultar = listaCuentas;
            salidaSaldo = saldoService.saldo(entradaSaldo);

            Map<Long, Info> infoPorCuenta = indexar(infoCuentas, Info::idCuentaInversion);

            for (SaldoPorFechaMovimiento s : salidaSaldo.P_OUT_SALDOPORFECHAMOVIMIENTO) {
                s.MontoTransSolicitudesAporte = s.MontoTransSolicitudesAporte
                        .add(montoEnTransito(solicitudes, s.IdCliente, s.IdPortafolio, s.IdMoneda, s.FechaSaldo), MathContext.DECIMAL128);
            }
            for (SaldoPorFechaEfectiva s : salidaSaldo.P_OUT_SALDOPORFECHAEFECTIVA) {
                s.MontoTransSolicitudesAporte = s.MontoTransSolicitudesAporte
                        .add(montoEnTransito(solicitudes, s.IdCliente, s.IdPortafolio, s.IdMoneda, s.FechaSaldo), MathContext.DECIMAL128);
            }

            if (entrada.p_IncluyeOtrosSaldos.equalsIgnoreCase("S")) {
                agregarOtrosSaldos(entrada, tipos, idTrabajo, fechaSistema, salidaSaldo);
            }

            // money market: custody quantity x unit value of the funds flagged MoneyMarket
            List<String> nemotecnicos = jdbc.queryForList(SQL_MONEY_MARKET, new MapSqlParameterSource()).stream()
                    .filter(s -> s.get("IdInstrumento") != null).map(s -> (String) s.get("Nemotecnico")).collect(Collectors.toList());
            boolean hayPortafolios = !salidaSaldo.P_OUT_SALDOPORFECHAEFECTIVA.isEmpty();

            // the CLP portfolio of a USD/EUR filter is needed to value the custody in those currencies
            if (entrada.p_CuentaInversionAConsultar.get(0).IdMoneda != null) {
                jdbc.getJdbcTemplate().execute(conTrabajo(SQL_INSERT_PORTAFOLIO_MONEDA_FILTRO, idTrabajo));
            }

            Map<String, ValorCuota> valoresCuota = new HashMap<>();
            Map<String, List<Custodia>> custodiaPorPortafolioMoneda = new HashMap<>();
            if (!nemotecnicos.isEmpty() && hayPortafolios) {
                List<ValorCuota> precios = obtenerPrecios(entrada, nemotecnicos);
                List<Map<String, Object>> filas = entrada.p_FechaDesde.equals(fechaSistema)
                        ? jdbc.queryForList(SQL_CUSTODIA, new MapSqlParameterSource("Nemotecnico", nemotecnicos))
                        : jdbc.queryForList(SQL_CUSTODIA_HISTORICA, new MapSqlParameterSource("Nemotecnico", nemotecnicos)
                                .addValue("FechaConsulta", entrada.p_FechaDesde));
                List<Custodia> custodia = filas.stream().map(r -> new Custodia(num(r.get("IdPortafolio")), (String) r.get("IdMoneda"),
                        dec(r.get("CantidadCustodia")), (String) r.get("Nemotecnico"))).toList();
                // LEGACY DEFECT kept (see README CTA-A-254): no series found for the company -> "List must be not null"
                valoresCuota = indexar(precios, ValorCuota::idSerieFondo);
                custodiaPorPortafolioMoneda = custodia.stream().collect(Collectors.groupingBy(c -> c.idPortafolio() + "_" + c.idMoneda()));
            }

            CalcularParidad.CalcularParidadEntrada paridad = new CalcularParidad.CalcularParidadEntrada();
            paridad.p_IdEmpresa = entrada.p_IdEmpresa;
            paridad.p_IdTrabajo = 0L;
            List<CalcularParidad.CalculoEntrada> calculos = new ArrayList<>();
            for (SaldoPorFechaEfectiva saldo : salidaSaldo.P_OUT_SALDOPORFECHAEFECTIVA) {
                saldo.MontoMoneyMarket = BigDecimal.ZERO;
                for (Custodia c : custodiaPorPortafolioMoneda.getOrDefault(saldo.IdPortafolio + "_" + saldo.IdMoneda, List.of())) {
                    ValorCuota valor = valoresCuota.get(c.nemotecnico());
                    if (valor != null) {
                        saldo.MontoMoneyMarket = saldo.MontoMoneyMarket.add(c.cantidad().multiply(valor.valorCuota(), MathContext.DECIMAL128));
                    }
                }
                if (!saldo.IdMoneda.equalsIgnoreCase(monedaSistema)) {
                    CalcularParidad.CalculoEntrada c = new CalcularParidad.CalculoEntrada();
                    c.IdTrabajo = 0L;
                    c.IdInterno = saldo.IdCuentaInversion;
                    c.Fecha = entrada.p_FechaDesde;
                    c.IdMonedaOrigen = monedaSistema;
                    c.IdMonedaDestino = saldo.IdMoneda;
                    c.ValorCambioLocalModificado = BigDecimal.ZERO;
                    c.IdUso = "";
                    c.MontoOrigen = saldo.MontoMoneyMarket;
                    calculos.add(c);
                }
            }
            Map<Long, BigDecimal> montoMoneyMarketEnMoneda = new HashMap<>();
            if (!calculos.isEmpty()) {
                paridad.p_CalculosEntrada = calculos;
                CalcularParidad.CalcularParidadSalida r = colaboradores.invocar(CalcularParidad.CLASE, paridad, CalcularParidad.CalcularParidadSalida.class);
                for (CalcularParidad.CalculoSalida c : r.p_out_CalculosSalida) {
                    montoMoneyMarketEnMoneda.put(c.IdInterno, c.MontoDestino);
                }
            }

            Map<String, UltimoMovimiento> ultimosMovimientos = ultimosMovimientos(salidaSaldo.P_OUT_SALDOPORFECHAEFECTIVA, idTrabajo);

            for (SaldoPorFechaEfectiva saldo : salidaSaldo.P_OUT_SALDOPORFECHAEFECTIVA) {
                Info info = infoPorCuenta.get(saldo.IdCuentaInversion);
                if (info == null) {
                    continue;
                }
                saldo.IdEjecutivo = info.idEjecutivo();
                saldo.NombreEjecutivo = info.nombreEjecutivo();
                saldo.CodigoEjecutivo = info.codigoEjecutivo();
                saldo.DocumentoIdentidad = info.documentoIdentidad();
                saldo.CodigoSucursal = vacioSiNulo(info.codigoSucursal());
                saldo.NombreSucursal = vacioSiNulo(info.nombreSucursal());
                saldo.NombreCliente = info.nombreCliente();
                saldo.NombrePortafolio = "(" + info.codigoPortafolio() + ") - " + info.propositoPortafolio();
                saldo.PropositoPortafolio = info.propositoPortafolio();
                saldo.CodigoPortafolio = info.codigoPortafolio();
                saldo.NombreMoneda = info.nombreMoneda();
                saldo.CodigoMoneda = info.codigoMoneda();
                saldo.Producto = vacioSiNulo(info.producto());
                saldo.IdProductoAgrupador = info.idProductoAgrupador();
                saldo.SaldoEnGarantia = ceroSiNulo(saldo.SaldoEnGarantia);
                saldo.SaldoAPlazoSimultanea = ceroSiNulo(saldo.SaldoAPlazoSimultanea);
                saldo.SaldoAPlazoVentaCorta = ceroSiNulo(saldo.SaldoAPlazoVentaCorta);
                saldo.SaldoAPlazoCompromiso = ceroSiNulo(saldo.SaldoAPlazoCompromiso);
                saldo.SaldoRetiro = ceroSiNulo(saldo.SaldoRetiro);
                saldo.IdMatrizAgrupador = info.idMatrizAgrupador();
                saldo.EsADC = info.esAdc();
                saldo.Relacion = info.relacion();
                if (saldo.CodigoMoneda.equalsIgnoreCase("CLP")) {
                    saldo.Saldo = truncarCLP(saldo.Saldo);
                    saldo.SaldoDisponible = truncarCLP(saldo.SaldoDisponible);
                    saldo.SaldoCargosPendientes = truncarCLP(saldo.SaldoCargosPendientes);
                    saldo.SaldoAbonosPendientes = truncarCLP(saldo.SaldoAbonosPendientes);
                    saldo.SaldoEnGarantia = truncarCLP(saldo.SaldoEnGarantia);
                    saldo.SaldoAPlazoSimultanea = truncarCLP(saldo.SaldoAPlazoSimultanea);
                    saldo.SaldoAPlazoVentaCorta = truncarCLP(saldo.SaldoAPlazoVentaCorta);
                    saldo.SaldoAPlazoCompromiso = truncarCLP(saldo.SaldoAPlazoCompromiso);
                    saldo.SaldoRetiro = truncarCLP(saldo.SaldoRetiro);
                    saldo.SaldoRetenido = truncarCLP(saldo.SaldoRetenido);
                    saldo.MontoTransSolicitudesAporte = truncarCLP(saldo.MontoTransSolicitudesAporte);
                    saldo.SaldoPendienteLibDAP = truncarCLP(saldo.SaldoPendienteLibDAP);
                }
                saldo.TipoCliente = vacioSiNulo(info.tipoCliente());
                if (!saldo.IdMoneda.equalsIgnoreCase(monedaSistema)) {
                    BigDecimal enMoneda = montoMoneyMarketEnMoneda.get(saldo.IdCuentaInversion);
                    saldo.MontoMoneyMarket = enMoneda != null ? enMoneda : BigDecimal.ZERO;
                }
                saldo.TotalLiquidez = saldo.MontoMoneyMarket.add(saldo.Saldo);
                UltimoMovimiento ultimo = ultimosMovimientos.get(idTrabajo + saldo.IdCuentaInversion.toString() + saldo.FechaSaldo);
                // LEGACY DEFECT kept (see README CTA-A-103): without movement the default date 0001-01-01 is rendered with the
                // JVM calendar and comes out as 0001-01-03
                saldo.FechaUltimoMovimiento = Fecha.stringToSqlDate(ultimo != null ? ultimo.fecha() : Fecha.toDate("0001-01-01"));
            }
            for (SaldoPorFechaMovimiento saldo : salidaSaldo.P_OUT_SALDOPORFECHAMOVIMIENTO) {
                Info info = infoPorCuenta.get(saldo.IdCuentaInversion);
                if (info == null) {
                    continue;
                }
                saldo.IdEjecutivo = info.idEjecutivo();
                saldo.NombreEjecutivo = info.nombreEjecutivo();
                saldo.CodigoEjecutivo = info.codigoEjecutivo();
                saldo.DocumentoIdentidad = info.documentoIdentidad();
                saldo.CodigoSucursal = vacioSiNulo(info.codigoSucursal());
                saldo.NombreSucursal = vacioSiNulo(info.nombreSucursal());
                saldo.NombreCliente = info.nombreCliente();
                saldo.NombrePortafolio = info.nombrePortafolio();
                saldo.PropositoPortafolio = info.propositoPortafolio();
                saldo.CodigoPortafolio = info.codigoPortafolio();
                saldo.CodigoMoneda = info.codigoMoneda();
                saldo.NombreMoneda = info.nombreMoneda();
                saldo.Producto = vacioSiNulo(info.producto());
                saldo.IdProductoAgrupador = info.idProductoAgrupador();
                saldo.SaldoEnGarantia = ceroSiNulo(saldo.SaldoEnGarantia);
                saldo.SaldoAPlazoSimultanea = ceroSiNulo(saldo.SaldoAPlazoSimultanea);
                saldo.SaldoAPlazoVentaCorta = ceroSiNulo(saldo.SaldoAPlazoVentaCorta);
                saldo.SaldoAPlazoCompromiso = ceroSiNulo(saldo.SaldoAPlazoCompromiso);
                saldo.SaldoRetiro = ceroSiNulo(saldo.SaldoRetiro);
                saldo.IdMatrizAgrupador = info.idMatrizAgrupador();
                saldo.EsADC = info.esAdc();
                if (saldo.CodigoMoneda.equalsIgnoreCase("CLP")) {
                    saldo.Saldo = truncarCLP(saldo.Saldo);
                    saldo.SaldoDisponible = truncarCLP(saldo.SaldoDisponible);
                    saldo.SaldoCargosPendientes = truncarCLP(saldo.SaldoCargosPendientes);
                    saldo.SaldoAbonosPendientes = truncarCLP(saldo.SaldoAbonosPendientes);
                    saldo.SaldoEnGarantia = truncarCLP(saldo.SaldoEnGarantia);
                    saldo.SaldoAPlazoSimultanea = truncarCLP(saldo.SaldoAPlazoSimultanea);
                    saldo.SaldoAPlazoVentaCorta = truncarCLP(saldo.SaldoAPlazoVentaCorta);
                    saldo.SaldoAPlazoCompromiso = truncarCLP(saldo.SaldoAPlazoCompromiso);
                    saldo.SaldoRetiro = truncarCLP(saldo.SaldoRetiro);
                    saldo.SaldoRetenido = truncarCLP(saldo.SaldoRetenido);
                    saldo.MontoTransSolicitudesAporte = truncarCLP(saldo.MontoTransSolicitudesAporte);
                    saldo.SaldoPendienteLibDAP = truncarCLP(saldo.SaldoPendienteLibDAP);
                }
            }
        }

        jdbc.getJdbcTemplate().execute(conTrabajo(SQL_DELETE_CUENTA_PASO, idTrabajo));

        out.P_OUT_SALDOPORFECHAEFECTIVA = salidaSaldo.P_OUT_SALDOPORFECHAEFECTIVA;
        out.P_OUT_SALDOPORFECHAMOVIMIENTO = salidaSaldo.P_OUT_SALDOPORFECHAMOVIMIENTO;
        out.P_OUT_SALDOINICIALFINAL = salidaSaldo.P_OUT_SALDOINICIALFINAL;
        out.P_OUT_ERRORESCONTADOR = 0L;
        out.P_OUT_ADVERTENCIASCONTADOR = 0L;
        out.P_OUT_INFORMACIONCONTADOR = 0L;
        log.info("FIN::ConsultarSaldoCuentaInversion");
        return out;
    }

    // ---------------------------------------------------------------------------------------------------- steps

    /** Output of an invalid request: one error, the counters of the other severities stay null (README CTA-A-95, CTA-A-96). */
    private Salida errorDeEntrada(Salida out, Entrada entrada, String codigo) {
        out.P_OUT_ERRORESCONTADOR = 1L;
        ErrorDetalle e = new ErrorDetalle();
        e.Codigo = codigo;
        e.Descripcion = diccionario.traducir(entrada.p_IdEmpresa, codigo);
        out.P_OUT_ERRORESDETALLE.add(e);
        return out;
    }

    private void insertarCuentas(List<CuentaConsulta> cuentas, long idTrabajo) {
        jdbc.batchUpdate(SQL_INSERT_CUENTA_PASO, cuentas.stream()
                .map(c -> (SqlParameterSource) new MapSqlParameterSource().addValue("IdTrabajo", idTrabajo)
                        .addValue("IdEjecutivo", c.idEjecutivo()).addValue("IdPortafolio", c.idPortafolio()).addValue("IdMoneda", c.idMoneda()))
                .toArray(SqlParameterSource[]::new));
    }

    /** Portfolios without currency are expanded to every currency account they have; the rows left without currency are dropped. */
    private void completarMonedas(long idTrabajo) {
        jdbc.getJdbcTemplate().execute(conTrabajo(SQL_INSERT_PORTAFOLIO_MONEDA, idTrabajo));
        jdbc.getJdbcTemplate().execute(SQL_DELETE_CUENTA_SIN_MONEDA);
    }

    private ObtenerPortafoliosEjecutivo.ObtenerPortafoliosEjecutivo_entrada consultaPortafolios(Entrada entrada, List<Long> portafolios,
                                                                                                String filtrar) {
        ObtenerPortafoliosEjecutivo.ObtenerPortafoliosEjecutivo_entrada c = new ObtenerPortafoliosEjecutivo.ObtenerPortafoliosEjecutivo_entrada();
        c.p_IdEmpresa = entrada.p_IdEmpresa;
        c.p_IdUsuario = entrada.p_IdUsuario;
        c.p_IdModulo = entrada.p_IdModulo;
        c.p_IdEmpleadoConsulta = entrada.p_IdEjecutivo;
        c.p_ListaIdPortafolios = portafolios;
        c.p_filtrarListaPortafolios = filtrar;
        return c;
    }

    private List<Long> portafoliosPermitidos(ObtenerPortafoliosEjecutivo.ObtenerPortafoliosEjecutivo_entrada consulta) {
        return colaboradores.invocar(ObtenerPortafoliosEjecutivo.CLASE, consulta, ListaPortafolios.class).valor;
    }

    /** Contributions in transit of the same client/portfolio/currency issued on or before the balance date. */
    private static BigDecimal montoEnTransito(List<Solicitud> solicitudes, Long idCliente, Long idPortafolio, String idMoneda, String fechaSaldo) {
        BigDecimal total = BigDecimal.ZERO;
        for (Solicitud sol : solicitudes) {
            if (idCliente.compareTo(sol.idCliente()) == 0 && idPortafolio.compareTo(sol.idPortafolio()) == 0
                    && idMoneda.equalsIgnoreCase(sol.idMonedaFondo())
                    && !LocalDate.parse(sol.fechaEmision()).isAfter(LocalDate.parse(fechaSaldo))) {
                total = total.add(sol.cantidadMontoAporte(), MathContext.DECIMAL128);
            }
        }
        return total;
    }

    private void agregarOtrosSaldos(Entrada entrada, List<String> tipos, long idTrabajo, String fechaSistema,
                                    SaldoCuentaInversionService.Salida salidaSaldo) {
        boolean todo = tipos.contains("TODO");
        MapSqlParameterSource conFechaHasta = new MapSqlParameterSource().addValue("p_IdEmpresa", entrada.p_IdEmpresa)
                .addValue("p_FechaHasta", entrada.p_FechaHasta).addValue("IdTrabajo", idTrabajo);
        Map<String, OtroSaldo> garantia = todo || tipos.contains("CONTABLE") ? porCliente(leerOtrosSaldos(SQL_GARANTIA, conFechaHasta)) : Map.of();

        MapSqlParameterSource p = new MapSqlParameterSource().addValue("p_IdEmpresa", entrada.p_IdEmpresa)
                .addValue("p_FechaDesde", entrada.p_FechaDesde).addValue("IdTrabajo", idTrabajo);
        boolean transito = todo || tipos.contains("TRANSITO");
        Map<String, OtroSaldo> compromisos = transito ? porCliente(leerOtrosSaldos(SQL_COMPROMISOS, p)) : Map.of();
        Map<String, OtroSaldo> simultaneas = Map.of();
        if (transito) {
            // from the system date on, the live positions; before it, the history
            boolean vigente = LocalDate.parse(entrada.p_FechaDesde).compareTo(LocalDate.parse(fechaSistema)) >= 0;
            simultaneas = porCliente(leerOtrosSaldos(vigente ? SQL_SIMULTANEAS : SQL_SIMULTANEAS_HISTORICO, p));
        }
        Map<String, OtroSaldo> prestamos = transito ? porCliente(leerOtrosSaldos(SQL_PRESTAMOS_EMPRESTITOS, p)) : Map.of();

        Map<String, OtroSaldo> tesoreriaEfectiva = new HashMap<>();
        if (todo || tipos.contains("DISPONIBLE")) {
            List<OtroSaldo> filas = new ArrayList<>(leerOtrosSaldos(SQL_TESORERIA_EFECTIVA, p));
            filas.addAll(leerOtrosSaldos(SQL_TESORERIA_EFECTIVA_INGRESADAS,
                    new MapSqlParameterSource().addValue("p_IdEmpresa", entrada.p_IdEmpresa).addValue("p_FechaDesde", entrada.p_FechaDesde)
                            .addValue("IdTrabajo", idTrabajo).addValue("p_FechaHasta", entrada.p_FechaHasta)));
            for (OtroSaldo s : filas) {
                String clave = "0" + s.idPortafolio() + s.idMoneda() + s.fechaSaldo();
                OtroSaldo previo = tesoreriaEfectiva.get(clave);
                tesoreriaEfectiva.put(clave, previo != null ? s.conMonto(s.monto().add(previo.monto())) : s);
            }
        }
        Map<String, OtroSaldo> tesoreriaMovimiento = todo || tipos.contains("CONTABLE") ? porPortafolio(leerOtrosSaldos(SQL_TESORERIA_MOVIMIENTO, p)) : Map.of();
        Map<String, OtroSaldo> retenidos = todo || tipos.contains("RETENIDO") ? porPortafolio(leerOtrosSaldos(SQL_RETENIDOS, p)) : Map.of();
        Map<String, OtroSaldo> pendientesDap = todo || tipos.contains("PENDIENTE") ? porPortafolio(leerOtrosSaldos(SQL_PENDIENTE_LIBERACION_DAP, p)) : Map.of();

        for (SaldoPorFechaEfectiva s : salidaSaldo.P_OUT_SALDOPORFECHAEFECTIVA) {
            String clave = s.IdCliente.toString() + s.IdPortafolio.toString() + s.IdMoneda + s.FechaSaldo;
            String clavePortafolio = "0" + s.IdPortafolio.toString() + s.IdMoneda + s.FechaSaldo;
            s.SaldoEnGarantia = monto(garantia.get(clave));
            s.SaldoAPlazoCompromiso = monto(compromisos.get(clave));
            s.SaldoAPlazoSimultanea = monto(simultaneas.get(clave));
            s.SaldoAPlazoVentaCorta = monto(prestamos.get(clave));
            s.SaldoRetiro = monto(tesoreriaEfectiva.get(clavePortafolio));
            s.SaldoRetenido = monto(retenidos.get(clavePortafolio));
            s.SaldoPendienteLibDAP = monto(pendientesDap.get(clavePortafolio));
        }
        for (SaldoPorFechaMovimiento s : salidaSaldo.P_OUT_SALDOPORFECHAMOVIMIENTO) {
            String clave = s.IdCliente.toString() + s.IdPortafolio.toString() + s.IdMoneda + s.FechaSaldo;
            s.SaldoEnGarantia = monto(garantia.get(clave));
            s.SaldoAPlazoCompromiso = monto(compromisos.get(clave));
            s.SaldoAPlazoSimultanea = monto(simultaneas.get(clave));
            s.SaldoAPlazoVentaCorta = monto(prestamos.get(clave));
            s.SaldoRetiro = monto(tesoreriaMovimiento.get("0" + s.IdPortafolio + s.IdMoneda + s.FechaSaldo));
            s.SaldoRetenido = BigDecimal.ZERO;
            s.SaldoPendienteLibDAP = BigDecimal.ZERO;
        }
    }

    private List<OtroSaldo> leerOtrosSaldos(String sql, MapSqlParameterSource p) {
        return jdbc.queryForList(sql, p).stream().map(r -> new OtroSaldo(num(r.get("IdCliente")), num(r.get("IdPortafolio")),
                (String) r.get("IdMoneda"), dec(r.get("Monto")), texto(r.get("FechaSaldo")))).toList();
    }

    /** Balances keyed by client + portfolio + currency + date (the last row of a key wins). */
    private static Map<String, OtroSaldo> porCliente(List<OtroSaldo> filas) {
        Map<String, OtroSaldo> m = new HashMap<>();
        for (OtroSaldo s : filas) {
            m.put(s.idCliente().toString() + s.idPortafolio().toString() + s.idMoneda() + s.fechaSaldo(), s);
        }
        return m;
    }

    /** Treasury-style balances: keyed by portfolio only (client "0"). */
    private static Map<String, OtroSaldo> porPortafolio(List<OtroSaldo> filas) {
        Map<String, OtroSaldo> m = new HashMap<>();
        for (OtroSaldo s : filas) {
            m.put("0" + s.idPortafolio().toString() + s.idMoneda() + s.fechaSaldo(), s);
        }
        return m;
    }

    /** Unit values of the money-market series from EntregarPreciosTasas; {@code null} when the company has none of the series. */
    private List<ValorCuota> obtenerPrecios(Entrada entrada, List<String> series) throws Exception {
        List<Map<String, Object>> filas = jdbc.queryForList(SQL_SERIE_FONDO, new MapSqlParameterSource("IdEmpresa", entrada.p_IdEmpresa)
                .addValue("IdSerieFondo", series));
        if (filas.isEmpty()) {
            return null;
        }
        Map<String, Map<String, Object>> serieFondo = indexar(filas, f -> (String) f.get("IdSerieFondo"));
        EntregarPreciosTasas.EntregarPreciosTasas_Entrada e = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada();
        e.p_IdEmpresa = entrada.p_IdEmpresa;
        e.p_IdModulo = null;
        e.p_IdPais = Long.parseLong(CtaJavaCallInfra.parametro(configuracion, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema"));
        e.p_IdUsuario = null;
        e.p_DatosInstrumentos = series.stream().map(nemo -> {
            Map<String, Object> serie = serieFondo.get(nemo);
            EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos d = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos();
            d.FechaConsulta = Fecha.utilDateToSqlDate(Fecha.toDate(entrada.p_FechaDesde));
            d.IdClasificacionTipoInstrumento = "FM";
            d.IdInstrumento = num(serie.get("IdInstrumento"));
            d.IdMoneda = entrada.p_IdMoneda;
            d.IdRegistro = d.IdInstrumento;
            d.IdTipoCriterioTasa = "0";
            d.IdTipoInstrumento = (String) serie.get("IdTipoInstrumento");
            d.IdUso = "CARTERA";
            d.Nemotecnico = nemo;
            d.PlazoResidual = 0L;
            d.TipoCartera = "0";
            return d;
        }).collect(Collectors.toList());
        EntregarPreciosTasas.EntregarPreciosTasas_Salida s = colaboradores.invocar(EntregarPreciosTasas.CLASE, e,
                EntregarPreciosTasas.EntregarPreciosTasas_Salida.class);
        return s.P_OUT_PRECIOSTASAS.stream().map(p -> new ValorCuota(p.Nemotecnico, p.Precio)).toList();
    }

    private Map<String, UltimoMovimiento> ultimosMovimientos(List<SaldoPorFechaEfectiva> saldos, long idTrabajo) throws Exception {
        List<UltimoMovimiento> filas = new ArrayList<>();
        if (!saldos.isEmpty()) {
            jdbc.batchUpdate(SQL_INSERT_ULTIMO_MOVIMIENTO, saldos.stream()
                    .map(s -> (SqlParameterSource) new MapSqlParameterSource().addValue("IdTrabajo", idTrabajo)
                            .addValue("IdCuentaInversion", s.IdCuentaInversion).addValue("FechaSaldo", Fecha.toDate(s.FechaSaldo)))
                    .toArray(SqlParameterSource[]::new));
            filas = jdbc.queryForList(SQL_SELECT_ULTIMO_MOVIMIENTO, new MapSqlParameterSource("p_IdTrabajo", idTrabajo)).stream()
                    .map(r -> new UltimoMovimiento(texto(r.get("KeyUltimoMovimiento")), aFecha(r.get("FechaUltimoMovimiento")))).toList();
        }
        return indexar(filas, UltimoMovimiento::clave);
    }

    // ---------------------------------------------------------------------------------------------------- helpers

    /** Index by key; like the legacy list-to-hash helper a null list or a repeated key is an error (plain Exception). */
    private static <K, T> Map<K, T> indexar(List<T> lista, Function<T, K> clave) throws Exception {
        if (lista == null) {
            throw new Exception("List must be not null");
        }
        Map<K, T> m = new HashMap<>();
        for (T t : lista) {
            if (m.put(clave.apply(t), t) != null) {
                throw new Exception("key field is not unique");
            }
        }
        return m;
    }

    /** The legacy replaced the job id in the text of these statements (insert-select with a constant). */
    private static String conTrabajo(String sql, long idTrabajo) {
        return sql.replace(":IdTrabajo", Long.toString(idTrabajo));
    }

    private static Info aInfo(Map<String, Object> r) {
        return new Info(num(r.get("IdCuentaInversion")), num(r.get("IdEjecutivo")), (String) r.get("NombreEjecutivo"),
                (String) r.get("CodigoSucursal"), (String) r.get("NombreSucursal"), (String) r.get("DocumentoIdentidad"),
                (String) r.get("NombreCliente"), (String) r.get("NombrePortafolio"), (String) r.get("PropositoPortafolio"),
                (String) r.get("CodigoPortafolio"), (String) r.get("CodigoMoneda"), (String) r.get("NombreMoneda"),
                (String) r.get("IdMoneda"), num(r.get("IdPortafolio")), (String) r.get("Producto"), num(r.get("IdMatrizAgrupador")),
                (String) r.get("EsADC"), (String) r.get("Relacion"), (String) r.get("TipoCliente"), (String) r.get("CodigoEjecutivo"),
                num(r.get("IdProductoAgrupador")));
    }

    private static BigDecimal monto(OtroSaldo s) {
        return s != null ? s.monto() : BigDecimal.ZERO;
    }

    private static BigDecimal ceroSiNulo(BigDecimal v) {
        return Objects.requireNonNullElse(v, BigDecimal.ZERO);
    }

    private static String vacioSiNulo(String v) {
        return v == null ? "" : v;
    }

    /** CLP has no decimals: amounts are truncated, never rounded. */
    BigDecimal truncarCLP(BigDecimal v) {
        return v.setScale(0, RoundingMode.DOWN);
    }

    /** Adds two amounts keeping the larger scale. */
    BigDecimal sumarMoneyMarket(BigDecimal a, BigDecimal b) {
        return a.add(b);
    }

    private static Long num(Object o) {
        return o == null ? null : ((Number) o).longValue();
    }

    private static BigDecimal dec(Object o) {
        if (o == null) {
            return null;
        }
        return o instanceof BigDecimal b ? b : new BigDecimal(o.toString());
    }

    private static String texto(Object o) {
        return o == null ? null : o.toString();
    }

    private static java.sql.Date aFecha(Object o) {
        if (o instanceof java.sql.Date d) {
            return d;
        }
        if (o instanceof Timestamp t) {
            return new java.sql.Date(t.getTime());
        }
        return o == null ? null : Fecha.toDate(o.toString());
    }
}
