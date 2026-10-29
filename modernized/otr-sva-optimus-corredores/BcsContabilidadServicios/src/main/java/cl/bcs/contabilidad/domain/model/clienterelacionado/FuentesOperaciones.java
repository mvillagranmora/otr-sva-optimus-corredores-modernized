package cl.bcs.contabilidad.domain.model.clienterelacionado;

import java.util.List;

/**
 * The seven result sets of legacy ConsultarOperacionesClienteRelacionado (COCR:175-181), one per SQL file
 * {@code ConsultarOperacionesClienteRelacionado_Select*.sql}.
 */
public record FuentesOperaciones(
        List<Comprobante> comprobantes,
        List<Comprobante> prestamos,
        List<PactoTermino> pactos,
        List<PactoTermino> terminos,
        List<OperacionDolar> spots,
        List<PactoTermino> terminosCartera,
        List<OperacionDolar> forwards) {

    public FuentesOperaciones {
        comprobantes = copia(comprobantes);
        prestamos = copia(prestamos);
        pactos = copia(pactos);
        terminos = copia(terminos);
        spots = copia(spots);
        terminosCartera = copia(terminosCartera);
        forwards = copia(forwards);
    }

    private static <T> List<T> copia(List<T> l) {
        return l == null ? List.of() : List.copyOf(l);
    }
}
