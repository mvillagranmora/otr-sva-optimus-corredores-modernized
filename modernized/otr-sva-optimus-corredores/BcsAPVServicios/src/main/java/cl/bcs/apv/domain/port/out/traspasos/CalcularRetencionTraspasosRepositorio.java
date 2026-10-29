package cl.bcs.apv.domain.port.out.traspasos;

import java.math.BigDecimal;
import java.util.List;

/** Persistence port of {@code CalcularRetencionTraspasos} (legacy {@code CalculoRetencionTraspasos}). */
public interface CalcularRetencionTraspasosRepositorio {

    /** Legacy {@code CrearTraspasoTesoreria.OrdenTraspaso} (row of {@code ConsultaOrdenesTraspaso.sql}). */
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

    /** Legacy {@code Retencion} (row of {@code ConsultarRetencion.sql}: the percentage already divided by 100). */
    class Retencion {
        public String CodigoRetiro;
        public Long IdPersonaRetencion;
        public Long IdRegimenTributario;
        public BigDecimal PorcentajeRetencion;
        public BigDecimal MontoTopeRetencion;
    }

    /** Certified FFMM orders / invoiced RV orders of the traspaso. */
    List<OrdenTraspaso> ordenesTraspaso(Long idTraspaso);

    /** Valid (VIG) retention configuration. */
    List<Retencion> configuracionRetencion();

    /** Legacy {@code con.rollback()} of the error branch (the whole connection of the call). */
    void revertir();
}
