package cl.bcs.contabilidad.jobs.support;

import cl.bcs.contabilidad.domain.port.out.InterfazSapSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory SAP B1 source for the SIGA interface jobs (stands for the legacy {@code getDESAPEB1Datasource()} queries,
 * CreaInterfazDiaria.java:111-113). Records every query in call order, with the fecha for the dated ones, and can be
 * told to throw from one query to exercise the per-file try/catch of the legacy jobs.
 */
public final class FakeInterfazSapSource implements InterfazSapSource {

    private final List<String> llamadas = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, RuntimeException> fallas = new HashMap<>();
    private List<BroadMovimiento> movimientos = List.of();

    /** Makes the named query throw {@code e}; names are the method names of {@link InterfazSapSource}. */
    public FakeInterfazSapSource fallar(String metodo, RuntimeException e) {
        fallas.put(metodo, e);
        return this;
    }

    public FakeInterfazSapSource conMovimientos(List<BroadMovimiento> filas) {
        this.movimientos = List.copyOf(filas);
        return this;
    }

    /** One BroadMovimiento row with fixed synthetic values (FecDia 2026-02-27, amount 1500.50). */
    public static BroadMovimiento movimientoSintetico() {
        LocalDate d = LocalDate.of(2026, 2, 27);
        return new BroadMovimiento("1", "AP", "ENT0001", d, 101500, "OP1", "P1", "R9", "1101001", "TC", "PRD1",
                "CC01", d, "S1", "D1", "AF01", "C", "CLP", new BigDecimal("1500.50"), "N", "N", "F1", "F2");
    }

    public List<String> llamadas() {
        synchronized (llamadas) {
            return List.copyOf(llamadas);
        }
    }

    private void registrar(String llamada, String metodo) {
        llamadas.add(llamada);
        RuntimeException e = fallas.get(metodo);
        if (e != null) throw e;
    }

    @Override
    public List<BroadCuenta> cuentasBroad() {
        registrar("cuentasBroad", "cuentasBroad");
        return List.of();
    }

    @Override
    public List<BroadMovimiento> movimientosBroad(LocalDate fechaEjecucion) {
        registrar("movimientosBroad(" + fechaEjecucion + ")", "movimientosBroad");
        return movimientos;
    }

    @Override
    public List<BroadSaldo> saldosBroad(LocalDate fechaEjecucion) {
        registrar("saldosBroad(" + fechaEjecucion + ")", "saldosBroad");
        return List.of();
    }

    @Override
    public List<Entidad> entidades() {
        registrar("entidades", "entidades");
        return List.of();
    }

    @Override
    public List<TipoCuenta> tiposCuenta() {
        registrar("tiposCuenta", "tiposCuenta");
        return List.of();
    }

    @Override
    public List<CentroCosto> centrosCosto() {
        registrar("centrosCosto", "centrosCosto");
        return List.of();
    }

    @Override
    public List<CuentaBpc> cuentasBpc() {
        registrar("cuentasBpc", "cuentasBpc");
        return List.of();
    }

    @Override
    public List<MovimientoBpc> movimientosBpc(LocalDate fechaEjecucion) {
        registrar("movimientosBpc(" + fechaEjecucion + ")", "movimientosBpc");
        return List.of();
    }
}
