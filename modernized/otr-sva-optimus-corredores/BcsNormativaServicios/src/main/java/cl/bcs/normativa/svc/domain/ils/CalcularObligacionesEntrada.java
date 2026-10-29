package cl.bcs.normativa.svc.domain.ils;

/**
 * Parámetros de entrada para el cálculo de obligaciones y derechos en moneda extranjera.
 */
public class CalcularObligacionesEntrada {

    /** ID de empresa */
    public Long idEmpresa;

    /** Fecha contable formato yyyy-MM-dd */
    public String fechaContable;

    public CalcularObligacionesEntrada() {}

    public CalcularObligacionesEntrada(Long idEmpresa, String fechaContable) {
        this.idEmpresa = idEmpresa;
        this.fechaContable = fechaContable;
    }
}
