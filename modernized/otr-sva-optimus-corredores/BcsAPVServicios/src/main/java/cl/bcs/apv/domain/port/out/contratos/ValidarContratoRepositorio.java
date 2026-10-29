package cl.bcs.apv.domain.port.out.contratos;

import java.math.BigDecimal;
import java.util.List;

import cl.bcs.apv.api.dto.validarcontrato.ModificaContratoSAAPV;

/**
 * Persistence port of the use case {@code ValidarContrato} (legacy {@code ValidaContratoSAAPV}): the lookups of person, client, portfolio and folio, the
 * update of the contract ids and the recaudacion validation query. The row classes are the legacy ones (public fields mapped by column label, Java
 * primitives where the legacy had them). Failures surface as the {@code SQLException} the legacy threw (see {@code SqlNombrado}).
 */
public interface ValidarContratoRepositorio {

    class ExistePersona {
        public long IdPersona;
        public String DocumentoIdentidad;
    }

    class ExisteCliente {
        public long IdCliente;
        public long IdPersona;
    }

    class ExistePortafolio {
        public long IdPersona;
        public String Codigo;
        public long IdPortafolio;
        public long IdCliente;
        public String NombreAgrupador;
        public long IdMatrizAgrupadorAPV;
        public String CodigoAgrupador;
    }

    class ExisteFolioContrato {
        public String FolioContratoSAAPV;
    }

    class RecaudacionValidada {
        public long FolioRecaudacion;
        public long IdRecaudacionEmpleador;
        public long IdRecaudacionCliente;
        public long IdPortafolio;
        public long IdContratoSAAPV;
        public String Validacion_Empleador;
        public String Validacion_FechaTermino;
        public String Validacion_Monto;
        public String Validacion_FondoRestringido;
        public String TipoRecaudacion;
        public String RutEmpleadorEmpleador;
        public String RutEmpleadorContrato;
        public String Pago;
        public String PeriodoTermino;
        public String FormaDePago;
        public BigDecimal RecMonto;
        public BigDecimal MontoAhorroCV;
        public BigDecimal RecMontoDC;
        public BigDecimal MontoAhorroDC;
        public String RegimenTributario;
        public String IdEstadoPortafolio;
        public String IdEstadoCliente;
        public String IdEstadoContrato;
        public String Asignado;
    }

    class TraspasoReapertura {
        public long IdPortafolio;
        public long IdContratoSAAPV;
        public String Codigo;
        public String EsReapertura;
    }

    List<ExistePersona> personasPorDocumento(String documentoIdentidad, String tipoPersona);

    List<ExisteCliente> clientesPorPersona(Long idPersona);

    List<ExistePortafolio> portafoliosApv(Long idPersona, String codigoRegimen, String nombrePlan, String codigoAgrupador);

    List<ExisteFolioContrato> foliosExistentes(String folioContratoSAAPV);

    /** One batch execution per element of the list given (the legacy passes the ACCUMULATED list at every row). */
    void modificarIdsContratos(List<ModificaContratoSAAPV> filas);

    List<RecaudacionValidada> recaudacionValidada(List<Long> idsRecaudacionCliente, String periodoSistema);

    List<TraspasoReapertura> detalleTraspaso(Long idDetalleTraspaso);
}
