package cl.bcs.fondomutuo.aportereinversion;

import java.util.List;

import cl.bcs.fondomutuo.aportereinversion.AporteReinversionContrato.ConsultaRegimenReinversion;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.normal.NormalDatos.ConfiguracionPropositoPortafolio;
import cl.bcs.fondomutuo.normal.NormalDatos.IndicadoresPortafolio;
import cl.bcs.fondomutuo.normal.NormalDatos.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.rescatereinversion.RreDatos.ReinversionSerieFondoDestino;

/**
 * Reads and writes of legacy {@code CrearSolicitudAporteReinversion} that are its own (verbatim SQL {@code Comunes*.sql} and {@code CrearSolicitudAporteReinversion_*.sql}).
 * The series, the portfolios, the movement types and the currencies come through the ports that 3A/3B already have; the row types of Normal are shared.
 */
public interface AporteReinversionDatos {

    /** Legacy {@code pojo.IndicadoresPortafolioFondoMutuo} (view {@code FMM.ConsultarIndicadoresPortafolioFondoMutuo}). */
    class IndicadoresPortafolioFondoMutuo {
        public Long IdPortafolio;
        public String OperaLineaSobregiro;
    }

    List<RegimenSerieDeFondo> regimenes(Long idEmpresa, List<String> idsSerie, List<String> idsRegimen);

    List<IndicadoresPortafolio> indicadoresPortafolio(List<Long> idsPortafolio, String codigoTipoIndicador);

    List<IndicadoresPortafolioFondoMutuo> indicadoresPortafolioFondoMutuo(List<Long> idsPortafolio);

    List<ConfiguracionPropositoPortafolio> configuracionProposito(List<String> idsRegimen);

    /** The tax regime of the rescues that the contributions reinvest ({@code CrearSolicitudAporteReinversion_SelectRegimenDestino.sql}). */
    List<ConsultaRegimenReinversion> regimenesDeLosRescates(List<Long> idsRescate);

    /** The destination rows whose final reinversion date differs from the certification date of the new contribution (no ORDER BY, as the legacy). */
    List<ReinversionSerieFondoDestino> reinversionesAActualizar(List<Long> idsRescate);

    /** Legacy {@code sql.batch(CrearSolicitudAporteReinversion_UpdateFechaFinalReinversion.sql)} (accepted write). */
    void actualizarFechaFinalReinversion(List<ReinversionSerieFondoDestino> filas);

    /** Legacy {@code sql.batch(ComunesSolicitudes_InsertSolicitud.sql)} into {@code FMM.SolicitudAporteRescate} (accepted write). */
    void insertarSolicitudes(List<SolicitudAporteRescate> solicitudes);
}
