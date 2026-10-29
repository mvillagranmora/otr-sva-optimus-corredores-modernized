package cl.bcs.contabilidad.domain.model.basilea;

/**
 * One configured Basilea account (ITZ.InterfazCuentasBasilea). Legacy {@code ProcesarInterfazBasilea.CuentaBasilea}
 * (ProcesarInterfazBasilea.java:240-256). The filter columns hold '' for "no filter" or a comma-separated list.
 */
public record CuentaBasilea(
        String conceptoAInformar,
        String tipoCuentaContable,
        String cuentaContable,
        String nombreCuentaContable,
        String idBolsa,
        String esVentaCorta,
        String relacionadoCorredora,
        String idClasificacionTipoInstrumento,
        String idTipoInstrumento,
        String idEmisor,
        String nemotecnico,
        String grupo,
        String producto,
        String idEstado,
        String idTipoOperacion) {
}
