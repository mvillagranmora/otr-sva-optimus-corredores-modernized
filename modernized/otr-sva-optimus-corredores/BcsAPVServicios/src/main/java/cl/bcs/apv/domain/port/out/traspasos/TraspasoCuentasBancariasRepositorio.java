package cl.bcs.apv.domain.port.out.traspasos;

import java.math.BigDecimal;
import java.util.List;

/** Persistence port of {@code TraspasarEntreCuentasBancarias} (legacy {@code TraspasoCuentasBancarias}): the three reads of the transition of bank accounts. */
public interface TraspasoCuentasBancariasRepositorio {

    /** Rows of the legacy {@code OrigenDestinoTransicion} (mutable on purpose: the legacy logic overwrites amounts and copies fields between rows). */
    class OrigenDestinoTransicion {
        public String OrigenDestino;
        public String MueveCuentaCorriente;
        public String MueveCuentaInversion;
        public Long IdCuentaBanco;
        public Long IdBanco;
        public String IdTipoCuentaBancaria;
        public String NombreTipoCuentaBancaria;
        public Long IdSocioDeNegocio;
        public Long SocioNegocioCtaBco;
        public Long IdBancoSocio;
        public String IdTipoCuentaBancariaSocio;
        public String NumeroCuentaBancariaSocio;
        public Long SocioNegocioNumCta;
        public Long IdPortafolioDefecto;
        public Long IdPersona;
        public BigDecimal Monto = BigDecimal.ZERO;
        public Long IdPersonaCliente;
        public String Origen;
    }

    /** Legacy {@code RecaudacionPersona}. */
    class RecaudacionPersona {
        public Long IdRecaudacionCliente;
        public Long IdPersonaCliente;
    }

    /** {@code ConsultaOrigenDestinoTransicion.sql}: the configured origin/destination accounts of the operation. */
    List<OrigenDestinoTransicion> transicion(Long idEmpresa, String operacion);

    /** {@code ConsultarPersonaClienteRecaudacion.sql}. */
    List<RecaudacionPersona> personaClienteRecaudacion(List<Long> idsRecaudacionCliente);

    /** {@code ConsultaOrigenRecaudacion.sql}. */
    List<OrigenDestinoTransicion> origenRecaudacion(Long idEmpresa, String operacion, List<Long> idsRecaudacionCliente);
}
