package cl.bcs.fondomutuo.agf;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

/** Reads and writes of legacy {@code CrearSolicitudAGF} (verbatim SQL {@code CrearSolicitudAGF_*.sql}); the inserts go to {@code FMM.InterfazAgf}. */
public interface AgfDatos {

    class SelectBeneficiario {
        public String DocumentoIdentidad;
        public String CodigoPortafolio;
        public String NombreCompleto;
        public Long IdCliente;
        public Long IdPortafolio;
    }

    class InsertBeneficiario {
        public String DocumentoIdentidad;
        public String CodigoPortafolio;
        public String NombreCompleto;
        public String FechaIngreso;
        public String IdEstado;
        public String TipoRegistro;
        public Long IdCliente;
        public Long IdPortafolio;
        public Long IdEmpresa;
    }

    class SelectSolicitud {
        public Long IdSolicitudAporteRescate;
        public Long IdSolicitudAporteRescateRelacionado;
        public String DocumentoIdentidad;
        public String CodigoPortafolio;
        public Date FechaLiquidacion;
        public Date FechaAceptacion;
        public String TipoSolicitud;
        public String IdEstadoSolicitud;
        public String IdSerieFondo;
        public Long IdUsuario;
        public BigDecimal NumeroCuotas;
        public BigDecimal ValorCuota;
        public BigDecimal Monto;
        public String CalculoPorMontoCuota;
        public String IdMoneda;
        public String PropositoSerie;
        public String IdSerieFondoDestino;
        public String IdEstadoSerieFondo;
        public String IndicadorMLT;
        public String NombreCompleto;
        public String IdTipoMovimientoFondoMutuo;
        public String TipoContratoOperacion;
        public String FolioContratoOperacion;
    }

    class InsertSolicitud {
        public Long IdSolicitudAporteRescate;
        public Long IdSolicitudAporteRescateRelacionado;
        public String DocumentoIdentidad;
        public String CodigoPortafolio;
        public Date FechaLiquidacion;
        public Date FechaAceptacion;
        public String TipoSolicitud;
        public String IdEstadoSolicitud;
        public String IdSerieFondo;
        public Long IdUsuario;
        public BigDecimal NumeroCuotas;
        public BigDecimal ValorCuota;
        public BigDecimal Monto;
        public String CalculoPorMontoCuota;
        public String IdMoneda;
        public String PropositoSerie;
        public String IdSerieFondoDestino;
        public String IdEstadoSerieFondo;
        public String IndicadorMLT;
        public String NombreCompleto;
        public String FechaIngreso;
        public String IdEstado;
        public String TipoRegistro;
        public Long IdEmpresa;
    }

    List<SelectBeneficiario> beneficiariosNuevos(List<Long> idsSolicitud);

    List<SelectSolicitud> solicitudes(List<Long> idsSolicitud);

    void insertarBeneficiarios(List<InsertBeneficiario> filas);

    void insertarSolicitudes(List<InsertSolicitud> filas);
}
