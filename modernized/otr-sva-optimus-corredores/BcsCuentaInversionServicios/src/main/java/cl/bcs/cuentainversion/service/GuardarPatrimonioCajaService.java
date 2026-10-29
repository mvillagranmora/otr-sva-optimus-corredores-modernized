package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.port.out.ColaboradoresReportes;
import cl.bcs.cuentainversion.reportes.ConfiguracionSistema;
import cl.bcs.cuentainversion.reportes.FormatoMoneda;
import cl.bcs.cuentainversion.reportes.JsonReportes;
import cl.bcs.cuentainversion.reportes.MensajeSalida;
import cl.bcs.cuentainversion.reportes.PatrimonioCaja;
import cl.bcs.cuentainversion.reportes.SqlReportes;
import cl.bcs.cuentainversion.service.GuardarPatrimonioCajaCalculoComisionService.CuentaInversionConsultar;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Daily cash patrimony per client / portfolio (legacy {@code GuardarPatrimonioCaja}; CTA-B-124..130).
 *
 * <p>Deletes the rows of the day, asks the account-balance service (collaborator {@code ConsultarSaldoCuentaInversion}) for the active
 * accounts of the system currency and of every other currency (CTA-B-130), and builds one LIQ (by settlement date) and one ADQ (by
 * movement date) row per balance: patrimony = sum of the configured items with their operators (CTA-B-124, 125), amounts formatted by
 * the currency rules. Foreign-currency patrimony is converted with {@code CalcularParidad} and ADDED to the system-currency row of the
 * same client / portfolio / date type (CTA-B-126). With {@code CTA.CobroComisionPatrimonio = S} the ADQ rows take the fee fields from
 * {@link GuardarPatrimonioCajaCalculoComisionService} (CTA-B-129).
 */
@Service
public class GuardarPatrimonioCajaService {

    private static final Logger log = LoggerFactory.getLogger(GuardarPatrimonioCajaService.class);

    private static final String SQL_MONEDAS = SqlReportes.recurso("GuardarPatrimonioCaja_SelectMonedas");
    private static final String SQL_CUENTAS = SqlReportes.recurso("GuardarPatrimonioCaja_SelectCuentaInversion");
    private static final String SQL_ITEMS = SqlReportes.recurso("GuardarPatrimonioCaja_SelectConfiguracionPatrimonioCaja");
    private static final String SQL_INSERT = SqlReportes.recurso("GuardarPatrimonioCaja_InsertPatrimonioCaja");
    private static final String SQL_DELETE = SqlReportes.recurso("GuardarPatrimonioCaja_DeletePatrimonioCaja");

    private final SqlReportes sql;
    private final NamedParameterJdbcTemplate jdbc;
    private final ColaboradoresReportes colaboradores;

    public GuardarPatrimonioCajaService(NamedParameterJdbcTemplate jdbc, ColaboradoresReportes colaboradores) {
        this.jdbc = jdbc;
        this.sql = new SqlReportes(jdbc);
        this.colaboradores = colaboradores;
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        return JsonReportes.escribir(guardar(JsonReportes.leer(params, Entrada.class)));
    }

    Salida guardar(Entrada entrada) throws Exception {
        log.info("INICIO::GuardarPatrimonioCaja");
        ConfiguracionSistema config = new ConfiguracionSistema(sql);
        FormatoMoneda formato = new FormatoMoneda(sql);
        String monedaSistema = config.cadena(entrada.p_IdEmpresa, ConfiguracionSistema.PLATAFORMA, "PLA.MonedaSistema");
        String cobraComision = config.cadena(entrada.p_IdEmpresa, ConfiguracionSistema.PLATAFORMA, "CTA.CobroComisionPatrimonio");

        sql.actualizar(SQL_DELETE, Map.of("FechaPatrimonio", entrada.p_Fecha));

        AtomicLong indice = new AtomicLong(1);
        List<Item> items = sql.consultar(SQL_ITEMS, Item.class);
        List<Moneda> monedas = sql.consultar(SQL_MONEDAS, Map.of("IdMonedaSistema", monedaSistema), Moneda.class);

        List<PatrimonioCaja> sistema = patrimonios(monedaSistema, monedaSistema, entrada, items, indice, formato);
        List<CuentaInversionConsultar> cuentasAdq = sistema.stream().filter(p -> "ADQ".equals(p.TipoFechaConsulta))
                .map(p -> new CuentaInversionConsultar(p.IdCuentaInversion, p.IdPortafolio, p.IdCliente, monedaSistema, entrada.p_Fecha))
                .toList();

        for (Moneda moneda : monedas) {
            List<PatrimonioCaja> extranjeras = patrimonios(moneda.IdMoneda, monedaSistema, entrada, items, indice, formato).stream()
                    .filter(p -> !"INA".equals(p.IdEstado)).toList();
            Map<Long, JsonObject> paridades = convertir(entrada, extranjeras);
            for (PatrimonioCaja base : sistema) {
                BigDecimal convertido = BigDecimal.ZERO;
                for (PatrimonioCaja ext : extranjeras) {
                    if (base.IdCliente.equals(ext.IdCliente) && base.IdPortafolio.equals(ext.IdPortafolio)
                            && base.TipoFechaConsulta.equals(ext.TipoFechaConsulta)) {
                        JsonObject paridad = paridades.get(ext.IdInterno);
                        // a missing answer for an id that was requested cannot happen with the real service (NPE as in the legacy)
                        BigDecimal destino = paridad.has("MontoDestino") && !paridad.get("MontoDestino").isJsonNull()
                                ? paridad.get("MontoDestino").getAsBigDecimal() : BigDecimal.ZERO;
                        convertido = destino.add(convertido, MathContext.DECIMAL128);
                    }
                }
                base.MontoPatrimonio = base.MontoPatrimonio.add(convertido, MathContext.DECIMAL128);
                base.IdEstado = base.MontoPatrimonio.compareTo(BigDecimal.ZERO) == 0 ? "INA" : "ACT";
            }
        }

        if ("S".equals(cobraComision)) {
            aplicarComision(sistema, entrada, cuentasAdq);
        }
        sql.lote(SQL_INSERT, sistema);
        log.info("FIN::GuardarPatrimonioCaja");
        return new Salida();
    }

    /** One LIQ row per effective-date balance and one ADQ row per movement-date balance of the active accounts of {@code moneda}. */
    private List<PatrimonioCaja> patrimonios(String moneda, String monedaSistema, Entrada entrada, List<Item> items, AtomicLong indice,
                                             FormatoMoneda formato) {
        List<InCuentaInversionConsultar> cuentas = sql.consultar(SQL_CUENTAS, Map.of("IdMoneda", moneda), InCuentaInversionConsultar.class);
        List<PatrimonioCaja> resultado = new ArrayList<>();
        if (cuentas.isEmpty()) {
            return resultado;
        }
        EntradaSaldo pedido = new EntradaSaldo();
        pedido.p_IdEmpresa = entrada.p_IdEmpresa;
        pedido.p_IdModulo = entrada.p_IdModulo;
        pedido.p_IdUsuario = entrada.p_IdUsuario;
        pedido.p_FechaDesde = entrada.p_Fecha;
        pedido.p_FechaHasta = entrada.p_Fecha;
        pedido.p_CuentaInversionAConsultar = cuentas;
        SalidaSaldo saldos = JsonReportes.leer(colaboradores.consultarSaldo(JsonReportes.escribir(pedido)), SalidaSaldo.class);

        saldos.P_OUT_SALDOPORFECHAEFECTIVA.forEach(s -> resultado.add(patrimonio(s, "LIQ", monedaSistema, entrada, items, indice, formato)));
        saldos.P_OUT_SALDOPORFECHAMOVIMIENTO.forEach(s -> resultado.add(patrimonio(s, "ADQ", monedaSistema, entrada, items, indice, formato)));
        return resultado;
    }

    /**
     * LEGACY QUIRK kept: a balance whose amounts cannot be formatted (no currency, no format rule) is answered as a {@code null}
     * element of the list, which later fails with a NullPointerException, as in the legacy.
     */
    private PatrimonioCaja patrimonio(SaldoCuenta saldo, String tipo, String monedaSistema, Entrada entrada, List<Item> items,
                                      AtomicLong indice, FormatoMoneda formato) {
        try {
            PatrimonioCaja p = new PatrimonioCaja();
            p.IdInterno = indice.getAndIncrement();
            p.IdCliente = saldo.IdCliente;
            p.IdPortafolio = saldo.IdPortafolio;
            p.FechaPatrimonio = entrada.p_Fecha;
            p.TipoFechaConsulta = tipo;
            p.IdMoneda = saldo.IdMoneda == null ? monedaSistema : saldo.IdMoneda;
            p.IdMonedaSistema = monedaSistema;
            p.SaldoDisponible = monto(formato, saldo.SaldoDisponible, saldo.IdMoneda);
            p.SaldoEnGarantia = monto(formato, saldo.SaldoEnGarantia, saldo.IdMoneda);
            p.SaldoRetiro = monto(formato, saldo.SaldoRetiro, saldo.IdMoneda);
            p.SaldoAPlazoSimultanea = monto(formato, saldo.SaldoAPlazoSimultanea, saldo.IdMoneda);
            p.SaldoAPlazoVentaCorta = monto(formato, saldo.SaldoAPlazoVentaCorta, saldo.IdMoneda);
            p.SaldoAPlazoCompromiso = monto(formato, saldo.SaldoAPlazoCompromiso, saldo.IdMoneda);
            for (Item item : items) {
                try {
                    Field campo = SaldoCuenta.class.getDeclaredField(item.CodigoItem);
                    BigDecimal valor = (BigDecimal) campo.get(saldo);
                    if (valor != null) {
                        p.MontoPatrimonio = p.MontoPatrimonio.add("+".equals(item.FuncionOperador) ? valor : valor.negate(MathContext.DECIMAL128));
                    }
                } catch (ReflectiveOperationException | ClassCastException e) {
                    log.warn("No existe item: {}", item.CodigoItem);
                }
            }
            p.MontoPatrimonio = monto(formato, p.MontoPatrimonio, saldo.IdMoneda);
            p.IdEstado = p.MontoPatrimonio.compareTo(BigDecimal.ZERO) == 0 ? "INA" : "ACT";
            p.IdCuentaInversion = saldo.IdCuentaInversion;
            return p;
        } catch (Exception e) {
            return null;
        }
    }

    private static BigDecimal monto(FormatoMoneda formato, BigDecimal valor, String moneda) throws Exception {
        return formato.formatear(valor, FormatoMoneda.MONTO, moneda).valor();
    }

    /** CalcularParidad of the foreign amounts to the system currency, indexed by the request id. */
    private Map<Long, JsonObject> convertir(Entrada entrada, List<PatrimonioCaja> extranjeras) throws Exception {
        EntradaParidad pedido = new EntradaParidad();
        pedido.p_IdEmpresa = entrada.p_IdEmpresa;
        for (PatrimonioCaja p : extranjeras) {
            CalculoEntrada c = new CalculoEntrada();
            c.Fecha = p.FechaPatrimonio;
            c.IdInterno = p.IdInterno;
            c.IdMonedaOrigen = p.IdMoneda;
            c.IdMonedaDestino = p.IdMonedaSistema;
            c.MontoOrigen = p.MontoPatrimonio;
            pedido.p_CalculosEntrada.add(c);
        }
        JsonObject respuesta = colaboradores.calcularParidad(JsonReportes.escribir(pedido));
        if (!respuesta.has("p_out_CalculosSalida") || respuesta.get("p_out_CalculosSalida").isJsonNull()) {
            throw new Exception("List must be not null");
        }
        Map<Long, JsonObject> indexadas = new HashMap<>();
        for (var e : respuesta.getAsJsonArray("p_out_CalculosSalida")) {
            JsonObject fila = e.getAsJsonObject();
            if (indexadas.put(fila.get("IdInterno").getAsLong(), fila) != null) {
                throw new Exception("key field is not unique");
            }
        }
        return indexadas;
    }

    private void aplicarComision(List<PatrimonioCaja> patrimonios, Entrada entrada, List<CuentaInversionConsultar> cuentas) throws Exception {
        GuardarPatrimonioCajaCalculoComisionService.Entrada pedido = new GuardarPatrimonioCajaCalculoComisionService.Entrada();
        pedido.p_IdEmpresa = entrada.p_IdEmpresa;
        pedido.p_IdModulo = entrada.p_IdModulo;
        pedido.p_IdUsuario = entrada.p_IdUsuario;
        pedido.p_UsaReglasNegocio = "S";
        pedido.p_FechaConsulta = entrada.p_Fecha;
        pedido.p_CuentaInversionAConsultar = cuentas;
        var resultado = new GuardarPatrimonioCajaCalculoComisionService(jdbc).calcular(pedido);

        Map<Long, PatrimonioCaja> porPortafolio = new HashMap<>();
        for (PatrimonioCaja p : resultado.P_TBL_PATRIMONIO) {
            if (porPortafolio.put(p.IdPortafolio, p) != null) {
                throw new Exception("key field is not unique");
            }
        }
        for (PatrimonioCaja p : patrimonios) {
            if ("ADQ".equals(p.TipoFechaConsulta)) {
                PatrimonioCaja calculado = porPortafolio.getOrDefault(p.IdPortafolio, new PatrimonioCaja());
                p.MontoPatrimonioAfectoComision = calculado.MontoPatrimonioAfectoComision;
                p.MontoComision = calculado.MontoComision;
                p.IdEstadoComision = calculado.IdEstadoComision;
                p.PorcentajeCobroComision = calculado.PorcentajeCobroComision;
            }
        }
    }

    // ---------------------------------------------------------------------------------------------- JSON contract

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_Fecha;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }

    /** Row of {@code PLA.ConfiguracionPatrimonioCaja}: balance field and the operator that adds it to the patrimony. */
    public static class Item {
        public String CodigoItem;
        public String FuncionOperador;
    }

    public static class Moneda {
        public String IdMoneda = "";
    }

    /** Account selector of the balance service (also the row of the active-accounts query). */
    public static class InCuentaInversionConsultar {
        public Long IdEjecutivo;
        public Long IdPortafolio;
        public String IdMoneda;
    }

    /** Request of {@code ConsultarSaldoCuentaInversion}: fixed filters, defaults of the legacy request POJO. */
    public static class EntradaSaldo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio = "S";
        public List<InCuentaInversionConsultar> p_CuentaInversionAConsultar = new ArrayList<>();
        public String p_FechaDesde;
        public String p_FechaHasta;
        public String p_SoloSaldoNegativo = "N";
        public String p_SoloSaldoDisponible = "N";
        public String p_SoloSaldoDisponibleNegativo = "N";
        public String p_IncluyeCargosPendientes = "N";
        public String p_IncluyeAbonosPendientes = "N";
        public String p_IncluyeOtrosSaldos = "S";
        public Long p_IdPortafolio;
        public String p_IncluyeProductoApv = "N";
        public Long p_IdEjecutivo;
        public Long p_IdCliente;
        public Long p_IdProductoAgrupador;
        public String p_IdMoneda;
        public List<String> p_TipoSaldoConsultar = new ArrayList<>(Arrays.asList("TODO"));
    }

    public static class SalidaSaldo {
        public List<SaldoCuenta> P_OUT_SALDOPORFECHAEFECTIVA = new ArrayList<>();
        public List<SaldoCuenta> P_OUT_SALDOPORFECHAMOVIMIENTO = new ArrayList<>();
    }

    /** Balance row of an account (the fields the configured items may name; both date types share this shape). */
    public static class SaldoCuenta {
        public Long IdCuentaInversion;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdMoneda;
        public BigDecimal Saldo;
        public BigDecimal SaldoDisponible;
        public BigDecimal SaldoCargosPendientes;
        public BigDecimal SaldoAbonosPendientes;
        public BigDecimal SaldoEnGarantia;
        public BigDecimal SaldoAPlazoSimultanea;
        public BigDecimal SaldoAPlazoVentaCorta;
        public BigDecimal SaldoAPlazoCompromiso;
        public BigDecimal SaldoRetiro;
        public BigDecimal SaldoRetenido = BigDecimal.ZERO;
        public BigDecimal MontoTransSolicitudesAporte = BigDecimal.ZERO;
        public BigDecimal SaldoPendienteLibDAP = BigDecimal.ZERO;
        public BigDecimal MontoMoneyMarket;
        public BigDecimal TotalLiquidez;
    }

    public static class EntradaParidad {
        public Long p_IdEmpresa;
        public Long p_IdTrabajo = 0L;
        public List<CalculoEntrada> p_CalculosEntrada = new ArrayList<>();
    }

    public static class CalculoEntrada {
        public Long IdTrabajo = 0L;
        public Long IdInterno;
        public String Fecha;
        public String IdMonedaOrigen;
        public String IdMonedaDestino;
        public BigDecimal ValorCambioLocalModificado = BigDecimal.ZERO;
        public BigDecimal MontoOrigen;
        public String IdUso = "";
    }
}
