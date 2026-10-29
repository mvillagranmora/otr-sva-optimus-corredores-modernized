package cl.bcs.apv.domain.port.out.traspasos;

import java.math.BigDecimal;
import java.util.List;

/**
 * Persistence port of {@code ModificarPortafoliosTraspaso} (legacy {@code cl.bcs.APV.ModificarPortafolioTraspaso}). Every {@code int[]} is the per-element count
 * of the legacy {@code SQL.batch}; the row records carry the column names the legacy POJOs mapped (the adapter maps by label).
 */
public interface ModificarPortafoliosTraspasoRepositorio {

    /** Legacy {@code DetalleTraspasoAEliminar}. */
    record DetalleAEliminar(Long IdDetalleTraspaso, Long IdPortafolio, String CodigoPortafolio, Long IdContratoSAAPV, String FolioContratoSAAPV,
            Long IdPlanTipoInversion, Long IdRegimenTributario, Long IdProductoAgrupador, BigDecimal MontoPeso, BigDecimal MontoUF) {
    }

    /** Legacy {@code TabDetalleTraspaso} (only the columns the logic reads). */
    record DetalleVigente(Long IdDetalleTraspaso, Long IdPortafolio, Long IdProductoAgrupador, Long IdRegimenTributario, BigDecimal MontoPeso,
            BigDecimal MontoUF, String IdEstado) {
    }

    /** Legacy {@code ClienteContratoSAAPV}. */
    record ClienteContrato(Long IdCliente, String IdPerfilComercial) {
    }

    /** Legacy {@code EstadoPortafolio} read by {@code ConsultarMatrizAgrupadorAPVPortafolio.sql}. */
    record PortafolioDeMatriz(Long IdPortafolio, String IdEstado) {
    }

    /** Legacy {@code infoPort} read by {@code SelectPortafoliosCreados.sql}. */
    record PortafolioCreado(Long IdPortafolio, Long IdCliente, String IdEstado, String IdEstadoAutorizacion) {
    }

    /** Legacy {@code vistaInfoPort}. */
    record PortafolioCliente(Long IdPortafolio, String CodigoPortafolio, String Proposito, Long IdCliente, String IdEstado) {
    }

    /** Legacy {@code validaPlanApv}. */
    record PlanPortafolio(Long IdPortafolio, Long IdMatrizAgrupadorAPV, Integer IdPlanTipoInversion) {
    }

    /** Legacy {@code PortafolioContratoSAAPV} (batch element of {@code Crear_PortafolioContratoSAAPV.sql}); same public fields. */
    final class RelacionPortafolioContrato {
        public String OrigenAhorro;
        public Long IdPortafolio;
        public Long IdContratoSAAPV;
        public double MontoPesos;
        public double MontoUF;
        public double PorcentajeRegimenImponible;
        public String Pago;
        public String PeriodoInicio;
        public String PeriodoTermino;
        public String IdEstado;
    }

    /** Legacy {@code DetalleTraspaso} (batch element of {@code DetalleTraspaso_Insert.sql}). */
    final class DetalleNuevo {
        public Long IdTraspaso;
        public Long IdDetalleTraspaso;
        public Long IdPortafolio;
        public String FechaIngreso;
        public String FechaModificacion;
        public Long IdPlanTipoInversion;
        public Long IdProductoAgrupador;
        public Long IdRegimenTributario;
        public double MontoPeso;
        public double MontoUF;
        public double Porcentaje;
        public String IdEstado;
        public String EsReapertura;
        public Long IdMatrizAgrupadorAPV;
    }

    /** Legacy {@code DetalleTraspasoModificar} (batch element of {@code ModificarDetalleTraspaso.sql}). */
    final class DetalleModificado {
        public Long IdDetalleTraspaso;
        public Long IdPortafolio;
        public Long IdTraspaso;
        public Long IdProductoAgrupador;
        public Long IdRegimenTributario;
        public String FechaIngreso;
        public BigDecimal MontoPeso;
        public BigDecimal MontoUF;
    }

    List<DetalleAEliminar> detallesAEliminar(Long idTraspaso, Long idContratoSAAPV);

    int[] eliminarDetalleTraspaso(Long idDetalleTraspaso);

    int[] eliminarPortafolioContrato(Long idPortafolio, Long idContratoSAAPV);

    /** Legacy {@code SQL.countQuery(ConsultarIdPortafolioContratoSAAPV)}: the contracts OTHER than {@code idContratoSAAPV} that hold the portafolio. */
    Long contarOtrosContratosDelPortafolio(Long idPortafolio, Long idContratoSAAPV);

    /** Legacy {@code InhabilitarPortafolioTraspaso.sql}: sets state and authorization state of the APV portafolio. */
    int[] cambiarEstadoPortafolio(Long idPortafolio, String idEstado, String idEstadoAutorizacion);

    List<ClienteContrato> clienteDelContrato(Long idContratoSAAPV);

    List<DetalleVigente> detallesDelTraspaso(Long idTraspaso, Long idPortafolio, Long idDetalleTraspaso);

    int[] cambiarEstadoDetalle(Long idDetalleTraspaso, String idEstado);

    /** Legacy {@code ModificarPortafolioTraspaso_SelectMatrizAgrupadorApv.sql}: the ids of the matrix (the legacy reads {@code get(0)}). */
    List<Long> matricesAgrupador(Long idPlanTipoInversion, Long idProductoAgrupador, Long idRegimenTributario);

    List<PortafolioDeMatriz> portafoliosDeMatriz(Long idMatrizAgrupadorAPV, Long idCliente);

    int[] relacionarPortafolioContrato(RelacionPortafolioContrato fila);

    int[] actualizarDetalleTraspaso(DetalleModificado fila);

    /** Legacy {@code ModificarMontoContratoSAAPV.sql}. */
    int[] modificarMontoContrato(Long idContratoSAAPV, Long idPortafolio, Long idPortafolioNuevo, BigDecimal montoPesos, BigDecimal montoUF);

    int[] insertarDetalleTraspaso(DetalleNuevo fila);

    List<PortafolioCreado> portafolioCreado(Long idPortafolio);

    List<PortafolioCliente> portafoliosDelCliente(Long idCliente);

    List<PlanPortafolio> planesDelCliente(Long idCliente);
}
