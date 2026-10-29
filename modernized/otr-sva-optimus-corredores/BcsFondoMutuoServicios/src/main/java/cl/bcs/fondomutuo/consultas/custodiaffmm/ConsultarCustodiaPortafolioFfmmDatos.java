package cl.bcs.fondomutuo.consultas.custodiaffmm;

import java.util.List;

/** Read port of the five SQL of the class, verbatim, with the same named parameters the legacy builds. */
public interface ConsultarCustodiaPortafolioFfmmDatos {

    /** Named parameters; the guarantee SQL are called WITHOUT {@code TipoFechaConsulta}, the custody SQL with it. */
    final class Parametros {
        public Long IdEmpresa;
        public Long IdCliente;
        public Long IdPortafolio;
        public Long IdInstrumento;
        public Long IdProductoAgrupador;
        public String FechaConsulta;
        public String IdEmisor;
        public String IdFamiliaFondo;
        public String TipoFechaConsulta;
    }

    List<ConsultarCustodiaPortafolioFfmmContrato.Garantia> garantiaVigente(Parametros parametros);

    List<ConsultarCustodiaPortafolioFfmmContrato.Garantia> garantiaHistorico(Parametros parametros);

    List<ConsultarCustodiaPortafolioFfmmContrato.Custodia> custodiaVigente(Parametros parametros);

    List<ConsultarCustodiaPortafolioFfmmContrato.Custodia> custodiaHistorico(Parametros parametros);

    List<ConsultarCustodiaPortafolioFfmmContrato.PersonaDestino> personasDestino(Long idPersonaCuentaPropia);
}
