package cl.bcs.fondomutuo.rescatereinversion;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;

/**
 * The reads and the two writes of {@code CrearSolicitudRescateReinversion} that are its own (verbatim legacy SQL {@code ComunesSolicitudes_*.sql}). The
 * series, the portfolios, the movement types and the currencies are read through the ports the closure already has ({@code SeriesFondoPort},
 * {@code RequisitosDatos}, {@code ComisionNnDatos}); the lock is {@link BloqueoReinversionPort}. Pojos: the legacy {@code pojo.*} with the same fields.
 */
public interface RreDatos {

    /** Legacy {@code pojo.RegimenSerieDeFondo} (row of {@code ComunesSolicitudes_SelectRegimenSerieDeFondo.sql}), every field. */
    class RegimenSerieDeFondo {
        public String IdRegimenFondoMutuoSerieFondo;
        public String IdRegimenFondoMutuo;
        public String IdSerieFondo;
        public String GrupoRegimen;
        public String PropositoSerieRelacionado;
        public String PermiteOtrosGrupos;
        public String ValidaPortafolioCliente;
        public String ValidaPortafolioAGF;
        public String PermiteRescate;
        public String TipoCarteraNormativa;
        public String PermiteCambioGrupo;
        public String RescateConMandato;
        public String AceptaMandatoEnReinversion;
        public String PermiteAporte;
        public String PermiteTraspaso;
        public String ValidaMontoMinimoInstrumento;
        public String CertificaSinAsociado;
        public String PermiteMLT;
    }

    /** Legacy {@code pojo.RegimenMandato} ({@code ComunesSolicitudes_SelectRegimenSerieDeFondoMandato.sql}). */
    class RegimenMandato {
        public Long IdEmpresa;
        public String IdSerieFondo;
        public String RescateConMandato;
    }

    /** Legacy {@code pojo.ConfiguracionRegimenFondoMutuo} ({@code ComunesSolicitudes_SelectConfiguracionRegimenFondoMutuo.sql}). */
    class ConfiguracionRegimenFondoMutuo {
        public String Id;
        public String IdRegimenFondoMutuoOrigen;
        public String IdRegimenFondoMutuoDestino;
    }

    /** Legacy {@code pojo.ConfiguracionPropositoPortafolio}. */
    class ConfiguracionPropositoPortafolio {
        public String Id;
        public String IdRegimenFondoMutuo;
        public String IdPropositoPortafolio;
    }

    /** Legacy {@code pojo.PortafolioAPV}. */
    class PortafolioAPV {
        public Long IdPortafolio;
    }

    /** Legacy {@code pojo.ReinversionSerieFondoDestino}: the fields are the parameters of {@code ComunesSolicitudes_InsertReinversionSolicitud.sql}. */
    class ReinversionSerieFondoDestino {
        public Long IdReinversionSerieFondo;
        public Long IdSolicitudAporteRescate;
        public String IdSerieFondoDestino;
        public BigDecimal Porcentaje;
        public String IdRegimenTributario;
        public String IdFamiliaFondo;
        public String Switch;
        public String IdMoneda;
        public BigDecimal PorcentajeReal = BigDecimal.ZERO;
        public Date FechaFinalReinversion;

        public ReinversionSerieFondoDestino() {
            this.Switch = "N";
            this.FechaFinalReinversion = Date.valueOf(RreConstantes.FECHA_DEFECTO);
        }
    }

    List<RegimenSerieDeFondo> regimenes(Long idEmpresa, List<String> idsSerie, List<String> idsRegimen);

    List<RegimenMandato> regimenesMandato(Long idEmpresa, List<String> idsSerie);

    List<ConfiguracionRegimenFondoMutuo> configuracionRegimen(List<String> idsRegimenOrigen);

    List<ConfiguracionPropositoPortafolio> configuracionProposito(List<String> idsRegimen);

    List<PortafolioAPV> portafoliosApv(List<Long> idsPortafolio);

    /** Legacy {@code sql.batch(ComunesSolicitudes_InsertReinversionSolicitud.sql)} into {@code FMM.ReinversionSerieFondoDestino} (accepted write). */
    void insertarReinversion(List<ReinversionSerieFondoDestino> filas);

    /** Legacy {@code sql.batch(ComunesSolicitudes_InsertSolicitud.sql)} into {@code FMM.SolicitudAporteRescate} (accepted write). */
    void insertarSolicitudes(List<SolicitudAporteRescate> solicitudes);
}
