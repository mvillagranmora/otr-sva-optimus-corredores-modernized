package cl.bcs.fondomutuo.normal;

import java.util.List;

import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;

/**
 * Reads and the one write of legacy {@code CrearSolicitudAporteRescateNormal} that are its own (verbatim SQL {@code Comunes*.sql} and
 * {@code CrearSolicitudAporteRescateNormal_*.sql}). The series, the portfolios, the movement types and the currencies are read through the ports that
 * 3A/3B already have ({@code SeriesFondoPort}, {@code RequisitosDatos}, {@code ComisionNnDatos}); only what they do not return is here.
 */
public interface NormalDatos {

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

    /** Legacy {@code pojo.IndicadoresPortafolio}. */
    class IndicadoresPortafolio {
        public Long IdCliente;
        public Long IdPortafolio;
        public String ValorIndicador;
        public String CodigoTipoIndicador;
    }

    /** Legacy {@code pojo.PortafolioAPV}. */
    class PortafolioAPV {
        public Long IdPortafolio;
    }

    /** Legacy {@code pojo.ConfiguracionPropositoPortafolio}. */
    class ConfiguracionPropositoPortafolio {
        public String Id;
        public String IdRegimenFondoMutuo;
        public String IdPropositoPortafolio;
    }

    /** Legacy {@code CrearSolicitudAporteRescateNormal.TraspasoAPV}. */
    class TraspasoAPV {
        public Long TraspasoAPV;
        public Long IdTraspaso;
    }

    /** Legacy {@code CrearSolicitudAporteRescateNormal.SalidaTraspasoTotal}. */
    class SalidaTraspasoTotal {
        public String Id;
        public Long Cantidad;
    }

    /** Legacy {@code CrearSolicitudAporteRescateNormal.ParametroOperaCSN}. */
    class ParametroOperaCSN {
        public String ValorIndicador = "N";
        public Long IdEntidad = 0L;
        public String CodigoTipoIndicador = "";
    }

    List<RegimenSerieDeFondo> regimenes(Long idEmpresa, List<String> idsSerie, List<String> idsRegimen);

    List<IndicadoresPortafolio> indicadoresPortafolio(List<Long> idsPortafolio, String codigoTipoIndicador);

    List<PortafolioAPV> portafoliosApv(List<Long> idsPortafolio);

    List<ConfiguracionPropositoPortafolio> configuracionProposito(List<String> idsRegimen);

    List<TraspasoAPV> traspasosApv(List<Long> foliosContrato);

    List<SalidaTraspasoTotal> custodiaTraspasoTotal(List<String> idsSerie, List<Long> idsPortafolio);

    List<ParametroOperaCSN> parametrosOperaConSaldoNegativo(List<Long> idsPortafolio);

    /** The rows just inserted, read back for the OWP notification ({@code CertificarSolicitudAporteRescate_SelectSolicitudes.sql}). */
    List<SolicitudAporteRescate> solicitudes(Long idEmpresa, List<Long> idsSolicitud);

    void insertarSolicitudes(List<SolicitudAporteRescate> solicitudes);
}
