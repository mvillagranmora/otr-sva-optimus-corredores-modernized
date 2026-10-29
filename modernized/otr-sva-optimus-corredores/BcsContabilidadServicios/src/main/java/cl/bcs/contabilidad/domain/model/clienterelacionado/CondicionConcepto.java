package cl.bcs.contabilidad.domain.model.clienterelacionado;

import java.util.List;

/**
 * A condition group of a normative concept, keyed by IdTipoDatoEvento (TipoInstrumento, TipoDocumento, CompraVenta,
 * TipoDato). Legacy {@code ConsultarCondicionesConceptosNormativos.CondicionesConceptoNormativo}.
 */
public record CondicionConcepto(String idTipoDatoEvento, List<ValorCondicion> valores) {

    public CondicionConcepto {
        valores = valores == null ? List.of() : List.copyOf(valores);
    }
}
