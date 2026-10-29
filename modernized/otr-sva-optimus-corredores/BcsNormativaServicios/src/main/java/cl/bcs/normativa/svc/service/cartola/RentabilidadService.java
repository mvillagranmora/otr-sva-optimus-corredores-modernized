package cl.bcs.normativa.svc.service.cartola;

import cl.bcs.normativa.svc.domain.cartola.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servicio de cálculo de rentabilidad de cartola.
 *
 * <p>Implementa la lógica pura del cálculo MTD/YTD/ITD portada de:
 * {@code cl.bcs.normativa.cartola.CalcularRentabilidad} (legacy).
 *
 * <p>Los métodos {@code calcular*} son {@code static} para permitir tests sin Spring.
 */
@Service
public class RentabilidadService {

    /**
     * Calcula la rentabilidad para todos los clientes en la entrada.
     */
    public CalcularRentabilidadSalida calcular(CalcularRentabilidadEntrada entrada) {
        CalcularRentabilidadSalida salida = new CalcularRentabilidadSalida();
        try {
            List<ClienteInfo> clientesDistintos = entrada.listaClientes.stream()
                    .filter(distinctByKey(c -> c.idCliente))
                    .collect(Collectors.toList());

            List<ClienteEmpresa> listaClienteEmpresa = new ArrayList<>();
            if (!Boolean.TRUE.equals(entrada.esPauta)) {
                for (ClienteInfo ci : clientesDistintos) {
                    listaClienteEmpresa.add(new ClienteEmpresa(ci.idCliente, entrada.idEmpresa));
                }
            }

            List<Rentabilidad> resultado = calcularRentabilidades(
                    listaClienteEmpresa, clientesDistintos, entrada);
            salida.salida.addAll(resultado);
        } catch (Exception e) {
            salida.erroresContador = 1L;
            salida.mensaje = e.getMessage();
        }
        return salida;
    }

    /**
     * Calcula las rentabilidades por cliente. Método estático para testear sin Spring.
     *
     * @param listaClienteEmpresa pares cliente-empresa (vacío en modo pauta)
     * @param listaClientes       clientes únicos
     * @param entrada             datos completos de entrada
     * @return lista de rentabilidades calculadas
     */
    public static List<Rentabilidad> calcularRentabilidades(
            List<ClienteEmpresa> listaClienteEmpresa,
            List<ClienteInfo> listaClientes,
            CalcularRentabilidadEntrada entrada) {

        Map<Long, SaldoCuentaCliente> movCaja = indexarPorCliente(entrada.movimientosCaja);
        Map<Long, SaldoCuentaCliente> movTitulos = indexarPorCliente(entrada.movimientosTitulo);
        Map<Long, SaldoFinalCliente> saldosFinales = indexarSaldosPorCliente(entrada.saldoFinal);

        List<?> base = Boolean.TRUE.equals(entrada.esPauta) ? listaClientes : listaClienteEmpresa;

        Map<Long, Rentabilidad> acumulado = new LinkedHashMap<>();

        for (Object item : base) {
            Long idCliente;
            Long idEmpresa;

            if (item instanceof ClienteInfo ci) {
                idCliente = ci.idCliente;
                idEmpresa = entrada.idEmpresa;
            } else {
                ClienteEmpresa ce = (ClienteEmpresa) item;
                idCliente = ce.idCliente;
                idEmpresa = ce.idEmpresa;
            }

            SaldoCuentaCliente caja = movCaja.getOrDefault(idCliente, new SaldoCuentaCliente());
            SaldoCuentaCliente titulos = movTitulos.getOrDefault(idCliente, new SaldoCuentaCliente());
            SaldoFinalCliente saldoFinal = saldosFinales.getOrDefault(idCliente, new SaldoFinalCliente());

            Rentabilidad r = calcularRentabilidadCliente(idCliente, idEmpresa, caja, titulos, saldoFinal);

            acumulado.merge(idCliente, r, RentabilidadService::sumarRentabilidad);
        }

        return new ArrayList<>(acumulado.values());
    }

    /**
     * Calcula la rentabilidad MTD/YTD/ITD de un cliente individual. Método estático para tests.
     *
     * <p>Fórmula general para período P:
     * <pre>
     *   aportes   = totalAbonosCajaP + totalIngresoTituloP
     *   rescates  = totalCargosCajaP + totalRetiroTituloP
     *   patrimonio = patrimonioFechaConsulta - saldoInicialP
     *   numerador = patrimonio - aportes + rescates
     *   denominador = saldoInicialP + aportesPeriodoP - rescatesPeriodoP
     *   rentabilidad = (denominador == 0) ? 0 : numerador / denominador
     * </pre>
     */
    public static Rentabilidad calcularRentabilidadCliente(
            Long idCliente, Long idEmpresa,
            SaldoCuentaCliente caja,
            SaldoCuentaCliente titulos,
            SaldoFinalCliente saldoFinal) {

        // --- MTD ---
        BigDecimal aportesMTD = safe(caja.totalAbonosCajaMTD).add(safe(titulos.totalIngresoTituloMTD));
        BigDecimal rescatesMTD = safe(caja.totalCargosCajaMTD).add(safe(titulos.totalRetiroTituloMTD));
        BigDecimal patrimonioMTD = safe(saldoFinal.totalPatrimonioFechaConsulta)
                .subtract(safe(saldoFinal.totalMTDSaldoFinal));
        BigDecimal aportesPeriodoMTD = safe(caja.abonosCajaMTDRentabilidad)
                .add(safe(caja.ventasTitulosMTDRentabilidad));
        BigDecimal rescatesPeriodoMTD = safe(caja.cargosCajaMTDRentabilidad)
                .add(safe(caja.comprasTitulosMTDRentabilidad));
        BigDecimal numeradorMTD = patrimonioMTD.subtract(aportesMTD).add(rescatesMTD);
        BigDecimal denominadorMTD = safe(saldoFinal.totalMTDSaldoFinal)
                .add(aportesPeriodoMTD).subtract(rescatesPeriodoMTD);
        BigDecimal rentMTD = dividir(numeradorMTD, denominadorMTD);

        // --- YTD ---
        BigDecimal aportesYTD = safe(caja.totalAbonosCajaYTD).add(safe(titulos.totalIngresoTituloYTD));
        BigDecimal rescatesYTD = safe(caja.totalCargosCajaYTD).add(safe(titulos.totalRetiroTituloYTD));
        BigDecimal patrimonioYTD = safe(saldoFinal.totalPatrimonioFechaConsulta)
                .subtract(safe(saldoFinal.totalYTDSaldoFinal));
        BigDecimal aportesPeriodoYTD = safe(caja.abonosCajaYTDRentabilidad)
                .add(safe(caja.ventasTitulosYTDRentabilidad));
        BigDecimal rescatesPeriodoYTD = safe(caja.cargosCajaYTDRentabilidad)
                .add(safe(caja.comprasTitulosYTDRentabilidad));
        BigDecimal numeradorYTD = patrimonioYTD.subtract(aportesYTD).add(rescatesYTD);
        BigDecimal denominadorYTD = safe(saldoFinal.totalYTDSaldoFinal)
                .add(aportesPeriodoYTD).subtract(rescatesPeriodoYTD);
        BigDecimal rentYTD = dividir(numeradorYTD, denominadorYTD);

        // --- ITD ---
        BigDecimal aportesITD = safe(caja.totalAbonosCajaITD).add(safe(titulos.totalIngresoTituloITD));
        BigDecimal rescatesITD = safe(caja.totalCargosCajaITD).add(safe(titulos.totalRetiroTituloITD));
        BigDecimal patrimonioITD = safe(saldoFinal.totalPatrimonioFechaConsulta)
                .subtract(safe(saldoFinal.totalITDSaldoFinal));
        BigDecimal aportesPeriodoITD = safe(caja.abonosCajaITDRentabilidad)
                .add(safe(caja.ventasTitulosITDRentabilidad));
        BigDecimal rescatesPeriodoITD = safe(caja.cargosCajaITDRentabilidad)
                .add(safe(caja.comprasTitulosITDRentabilidad));
        BigDecimal numeradorITD = patrimonioITD.subtract(aportesITD).add(rescatesITD);
        BigDecimal denominadorITD = safe(saldoFinal.totalITDSaldoFinal)
                .add(aportesPeriodoITD).subtract(rescatesPeriodoITD);
        BigDecimal rentITD = dividir(numeradorITD, denominadorITD);

        return new Rentabilidad(idCliente, idEmpresa, rentMTD, rentYTD, rentITD);
    }

    // ---- helpers ----

    private static BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal dividir(BigDecimal numerador, BigDecimal denominador) {
        if (denominador.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return numerador.divide(denominador, 2, RoundingMode.HALF_UP);
    }

    private static Rentabilidad sumarRentabilidad(Rentabilidad a, Rentabilidad b) {
        a.rentabilidadMTD = a.rentabilidadMTD.add(b.rentabilidadMTD);
        a.rentabilidadYTD = a.rentabilidadYTD.add(b.rentabilidadYTD);
        a.rentabilidadITD = a.rentabilidadITD.add(b.rentabilidadITD);
        return a;
    }

    private static Map<Long, SaldoCuentaCliente> indexarPorCliente(List<SaldoCuentaCliente> lista) {
        if (lista == null) return Collections.emptyMap();
        Map<Long, SaldoCuentaCliente> map = new LinkedHashMap<>();
        for (SaldoCuentaCliente s : lista) {
            if (s.idCliente != null) map.put(s.idCliente, s);
        }
        return map;
    }

    private static Map<Long, SaldoFinalCliente> indexarSaldosPorCliente(List<SaldoFinalCliente> lista) {
        if (lista == null) return Collections.emptyMap();
        Map<Long, SaldoFinalCliente> map = new LinkedHashMap<>();
        for (SaldoFinalCliente s : lista) {
            if (s.idCliente != null) map.put(s.idCliente, s);
        }
        return map;
    }

    private static <T> java.util.function.Predicate<T> distinctByKey(
            Function<? super T, ?> keyExtractor) {
        Set<Object> seen = Collections.synchronizedSet(new HashSet<>());
        return t -> seen.add(keyExtractor.apply(t));
    }

    /** Par cliente-empresa para indexación interna. */
    public record ClienteEmpresa(Long idCliente, Long idEmpresa) {}
}
