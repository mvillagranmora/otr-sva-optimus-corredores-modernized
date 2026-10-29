package cl.bcs.apv.domain.port.out.contratos;

import java.sql.SQLException;
import java.util.List;

/**
 * Persistence port of the authorization hook of the plan change ({@code AutorizacionCambiarAPlanActivoContratoSAAPV}): the detail rows of the events.
 * {@code SQLException} surfaces unchanged: the legacy hook caught it and carried on with a null result.
 */
public interface EventoAutorizacionRepositorio {

    /**
     * {@code SELECT ... FROM AUT.EventoDetalle WHERE IdEvento = :idsEvento} (inline in the legacy hook). The legacy {@code SQL.query} expands a list parameter to
     * {@code ?,?,...} without parentheses, so with more than one event id the statement is a syntax error (SQLException); an empty list is one NULL item.
     */
    List<DetalleEventoAutorizacion> consultarDetalleEventos(List<Long> idsEvento) throws SQLException;

    record DetalleEventoAutorizacion(Long IdUnion, String NombreCampo, String TipoDato, String Valor, String Clasificacion) {
    }
}
