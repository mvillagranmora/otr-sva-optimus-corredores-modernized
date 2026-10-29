package cl.bcs.normativa.svc.domain.cartola;

import java.util.List;

/**
 * Parámetros de entrada para el cálculo de rentabilidad de cartola.
 */
public class CalcularRentabilidadEntrada {

    /** Fecha de consulta formato yyyy-MM-dd */
    public String fechaConsulta;

    /**
     * Indica si el cálculo es en modo pauta (agrupa por idCliente de listaClientes)
     * o modo normal (agrupa por idEmpresa).
     */
    public Boolean esPauta = false;

    /** Empresa a calcular (modo normal) */
    public Long idEmpresa;

    /** Lista de clientes con sus movimientos de caja */
    public List<SaldoCuentaCliente> movimientosCaja;

    /** Lista de clientes con sus movimientos de títulos */
    public List<SaldoCuentaCliente> movimientosTitulo;

    /** Lista de saldos finales por cliente */
    public List<SaldoFinalCliente> saldoFinal;

    /** Lista de clientes (datos de identificación) */
    public List<ClienteInfo> listaClientes;

    public CalcularRentabilidadEntrada() {}
}
