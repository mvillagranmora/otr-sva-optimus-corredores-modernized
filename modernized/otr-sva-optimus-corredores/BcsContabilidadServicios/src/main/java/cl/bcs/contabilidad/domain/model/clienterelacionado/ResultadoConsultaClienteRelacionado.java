package cl.bcs.contabilidad.domain.model.clienterelacionado;

import java.util.List;

/**
 * Successful result of the related-client query: per-concept totals, the "Total" row and the operations listed in
 * the report (COCR:103-109 {@code conceptos}, {@code totalizado}, {@code operaciones}).
 */
public record ResultadoConsultaClienteRelacionado(List<TotalConcepto> conceptos, TotalConcepto totalizado,
                                                  List<OperacionClienteRelacionado> operaciones) {

    public ResultadoConsultaClienteRelacionado {
        conceptos = conceptos == null ? List.of() : List.copyOf(conceptos);
        operaciones = operaciones == null ? List.of() : List.copyOf(operaciones);
    }
}
