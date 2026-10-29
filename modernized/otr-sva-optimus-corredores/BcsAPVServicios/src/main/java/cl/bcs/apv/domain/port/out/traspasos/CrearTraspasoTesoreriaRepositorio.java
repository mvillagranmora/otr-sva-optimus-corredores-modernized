package cl.bcs.apv.domain.port.out.traspasos;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/** Persistence port of {@code CrearTraspasoTesoreria} (legacy {@code cl.bcs.APV.CrearTraspasoTesoreria}): reads only, plus the session temporary table of the saldo procedure. */
public interface CrearTraspasoTesoreriaRepositorio {

    /** Rows of the DEV transition of the company (the caller takes the first row: an empty list is the legacy IndexOutOfBounds). */
    List<DestinoCtaBco> destinoCuentaBanco(Long idEmpresa) throws SQLException;

    List<DatosEmpresa> portafolioEmpresa(Long idEmpresa) throws SQLException;

    List<Retencion> configuracionRetencion() throws SQLException;

    List<Portafolio> portafoliosPersona(Long idPersona) throws SQLException;

    /** {@code CREATE LOCAL TEMPORARY TABLE #p_CuentaInversionAConsultar}. */
    void crearTablaCuentaInversionAConsultar() throws SQLException;

    void insertarCuentaInversionAConsultar(List<DatosCuentaInversion> filas) throws SQLException;

    /** {@code DROP TABLE #p_CuentaInversionAConsultar}. */
    void eliminarTablaCuentaInversionAConsultar() throws SQLException;

    List<OrdenTraspaso> ordenesDelTraspaso(Long idTraspaso) throws SQLException;

    List<ContratoSAAPV> contratoDelTraspaso(Long idTraspaso) throws SQLException;

    class DestinoCtaBco {
        public Long DestinoCtaBco;
    }

    class DatosEmpresa {
        public Long IdPersona;
        public Long IdPortafolioDefecto;
    }

    class Retencion {
        public String CodigoRetiro;
        public Long IdPersonaRetencion;
        public Long IdRegimenTributario;
        public BigDecimal PorcentajeRetencion;
        public BigDecimal MontoTopeRetencion;
    }

    class Portafolio {
        public Long IdPortafolio;
        public Long IdCliente;
    }

    /** Row of the temporary table and element of the procedure input {@code p_CuentaInversionAConsultar}. */
    class DatosCuentaInversion {
        public Long IdEjecutivo;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdMoneda;
    }

    class OrdenTraspaso {
        public Long IdTraspaso;
        public Long IdOrdenTraspaso;
        public String RazonRetiro;
        public String Regimen;
        public Long IdOrden;
        public Long IdSolicitudAporteRescate;
        public BigDecimal MontoOrden;
        public BigDecimal MontoOrdenFFMM;
        public Long IdPortafolio;
        public Long IdMatrizAgrupadorAPV;
        public Long IdRegimenTributarioMatriz;
    }

    class ContratoSAAPV {
        public Long IdContratoSAAPV;
    }
}
