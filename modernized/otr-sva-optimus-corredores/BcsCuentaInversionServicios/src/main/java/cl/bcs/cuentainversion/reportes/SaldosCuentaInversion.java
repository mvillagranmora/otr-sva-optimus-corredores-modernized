package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.port.out.ColaboradoresReportes;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.IntPredicate;

/**
 * Client side of the {@code ConsultarSaldoCuentaInversion} collaborator as the aging / creditors reports use it: the request
 * (JSON form of the legacy {@code EntradaConsultarSaldoCuentaInversion}, defaults included), the part of the answer they read and
 * the balance rules shared by CTA-B-69..108 (CLP truncated to 0 decimals, other currencies rounded HALF_EVEN to 2, before deciding
 * the sign of a balance).
 */
public final class SaldosCuentaInversion {

    private SaldosCuentaInversion() {
    }

    /** Request of the collaborator ({@code p_*} fields, defaults of the legacy POJO). */
    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public List<CuentaAConsultar> p_CuentaInversionAConsultar = new ArrayList<>();
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
        public Long p_IdProductoAgrupador = 9999L;
        public String p_IdMoneda;
        public List<String> p_TipoSaldoConsultar = new ArrayList<>(List.of("TODO"));
    }

    public static class CuentaAConsultar {
        public Long IdEjecutivo;
        public Long IdPortafolio;
        public String IdMoneda;
    }

    /** Row of {@code P_OUT_SALDOPORFECHAEFECTIVA}. */
    public static class Saldo {
        public Long IdCuentaInversion;
        public String CodigoCuentaInversion;
        public Long IdCliente;
        public Long IdPortafolio;
        public String FechaSaldo;
        public String IdMoneda;
        public BigDecimal Saldo;
        public BigDecimal SaldoDisponible;
        public BigDecimal SaldoCargosPendientes;
        public BigDecimal SaldoAbonosPendientes;
        public Long IdEjecutivo;
        public String NombreEjecutivo;
        public String CodigoEjecutivo;
        public String NombreCliente;
        public String DocumentoIdentidad;
        public String NombrePortafolio;
        public String CodigoPortafolio;
        public String CodigoMoneda;
        public BigDecimal SaldoRetenido = BigDecimal.ZERO;
        public String Relacion;

        /** Disponible + retenido (34 digits, the legacy {@code MathContext.DECIMAL128}). */
        public BigDecimal total() {
            return SaldoDisponible.add(SaldoRetenido, MathContext.DECIMAL128);
        }
    }

    public static class Salida {
        public List<Saldo> P_OUT_SALDOPORFECHAEFECTIVA = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
    }

    /** Request with the fixed filters of the reports: business rules and other balances on, APV included, no client / portfolio filter. */
    public static Entrada solicitud(Long idEmpresa, Long idUsuario, String idModulo, String fechaConsulta, Long idEjecutivo, String idMoneda) {
        Entrada e = new Entrada();
        e.p_IdEmpresa = idEmpresa;
        e.p_IdUsuario = idUsuario;
        e.p_IdModulo = idModulo;
        e.p_FechaDesde = fechaConsulta;
        e.p_FechaHasta = fechaConsulta;
        e.p_UsaReglasNegocio = "S";
        e.p_IncluyeOtrosSaldos = "S";
        e.p_IncluyeProductoApv = "S";
        e.p_IdEjecutivo = 0L;
        e.p_IdCliente = null;
        e.p_IdPortafolio = null;
        e.p_IdProductoAgrupador = null;
        CuentaAConsultar c = new CuentaAConsultar();
        c.IdEjecutivo = idEjecutivo;
        c.IdMoneda = idMoneda;
        e.p_CuentaInversionAConsultar = new ArrayList<>(List.of(c));
        return e;
    }

    /** One call to the collaborator; the request is serialised when it is made, so later changes to {@code e} are not seen by it. */
    public static Salida consultar(ColaboradoresReportes colaboradores, Entrada e) {
        return JsonReportes.leer(colaboradores.consultarSaldo(JsonReportes.escribir(e)), Salida.class);
    }

    /** True when the answer has no errors and at least one balance. */
    public static boolean utilizable(Salida s) {
        return s.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0 && !s.P_OUT_SALDOPORFECHAEFECTIVA.isEmpty();
    }

    /** Amount at the precision the sign is decided with: CLP truncated to 0 decimals, other currencies HALF_EVEN to 2. */
    public static BigDecimal aproximar(BigDecimal monto, String idMoneda) {
        return "CLP".equalsIgnoreCase(idMoneda) ? monto.setScale(0, RoundingMode.DOWN) : monto.setScale(2, RoundingMode.HALF_EVEN);
    }

    /** Accounts whose approximated total has the wanted sign ({@code signo} receives -1, 0 or 1). */
    public static List<Saldo> conSigno(List<Saldo> saldos, String idMoneda, IntPredicate signo) {
        return saldos.stream().filter(s -> signo.test(aproximar(s.total(), idMoneda).compareTo(BigDecimal.ZERO))).toList();
    }

    /** A client with its accounts when the client total (all of its accounts) has the wanted sign. */
    public static class ClienteConsolidado {
        public Long idCliente;
        public BigDecimal saldoFinal = BigDecimal.ZERO;
        public List<Long> idCuentasInversion = new ArrayList<>();
        public List<Long> idPortafolios = new ArrayList<>();
    }

    /**
     * Consolidation per client (Consolidar = S): for each client with an account of the wanted sign, ALL its accounts are kept when
     * the client total has the wanted sign too. Clients keep the order of their first qualifying account.
     */
    public static List<ClienteConsolidado> consolidar(List<Saldo> saldos, String idMoneda, IntPredicate signo, List<Saldo> cuentasSalida) {
        List<ClienteConsolidado> clientes = new ArrayList<>();
        List<Long> idClientes = new ArrayList<>(new LinkedHashSet<>(conSigno(saldos, idMoneda, signo).stream().map(s -> s.IdCliente).toList()));
        for (Long idCliente : idClientes) {
            List<Saldo> deCliente = saldos.stream().filter(s -> s.IdCliente.equals(idCliente)).toList();
            BigDecimal total = deCliente.stream().map(Saldo::total).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (signo.test(aproximar(total, idMoneda).compareTo(BigDecimal.ZERO))) {
                ClienteConsolidado c = new ClienteConsolidado();
                c.idCliente = idCliente;
                c.saldoFinal = total;
                c.idCuentasInversion = deCliente.stream().map(s -> s.IdCuentaInversion).distinct().toList();
                c.idPortafolios = deCliente.stream().map(s -> s.IdPortafolio).distinct().toList();
                clientes.add(c);
                cuentasSalida.addAll(deCliente);
            }
        }
        return clientes;
    }
}
