package cl.bcs.fondomutuo.consultas.custodia;

import java.util.List;

/** Read port: the rows of the four SQL of the class, verbatim, with the same named parameters the legacy builds. */
public interface ConsultarCustodiaPortafolioDatos {

    /** Named parameters of the two solicitud SQL (the legacy {@code HashMap}); an empty text / null stays as given. */
    final class Parametros {
        public String IngresoAutomatico;
        public String FiltraIngresoAutomatico;
        public Long IdEmpresa;
        public Long IdCliente;
        public Long IdPortafolio;
        public Long IdInstrumento;
        public Long IdProductoAgrupador;
        public String IdEmisor;
        public String IdFamiliaFondo;
    }

    List<ConsultarCustodiaPortafolioContrato.Solicitud> solicitudes(Parametros parametros);

    List<ConsultarCustodiaPortafolioContrato.SolicitudTotal> solicitudesTotales(Parametros parametros);

    List<ConsultarCustodiaPortafolioContrato.SerieFondo> series(List<Long> idsInstrumento);

    List<ConsultarCustodiaPortafolioContrato.Regimen> regimenes(List<String> idsSerieFondo);
}
