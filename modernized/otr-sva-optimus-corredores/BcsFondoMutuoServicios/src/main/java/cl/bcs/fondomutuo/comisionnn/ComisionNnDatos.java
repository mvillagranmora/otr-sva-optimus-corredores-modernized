package cl.bcs.fondomutuo.comisionnn;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;

/** Reads and the one write of legacy {@code CrearSolicitudRescateComisionNN} (verbatim SQL {@code Comunes*.sql}). */
public interface ComisionNnDatos {


    /** Row of {@code Comunes_SelectTipoMovimientoFondoMutuo.sql}. */
    class TipoMovimientoFondoMutuo {
        public String IdTipoMovimientoFondoMutuo;
        public String Descripcion;
        public String IndicadorAporteRescate;
        public String IdTipoOperacion;
        public String TieneCarga;
        public String TieneIngresoManual;
        public String TieneCertificacion;
        public String IdEstado;
    }

    /** Row of {@code ComunesSolicitudes_SelectMoneda.sql}. */
    class Moneda {
        public String IdMoneda;
        public Long IdPais;
    }

    /** Row of {@code ComunesSolicitudes_SelectRegimenSerieDeFondo.sql}. */
    class RegimenSerieDeFondo {
        public String IdRegimenFondoMutuoSerieFondo;
        public String IdRegimenFondoMutuo;
        public String IdSerieFondo;
        public String GrupoRegimen;
    }

    List<SerieFondoSolicitudes> series(Long idEmpresa, List<String> idsSerie);

    List<TipoMovimientoFondoMutuo> tiposMovimiento(List<String> idsTipoMovimiento);

    List<RegimenSerieDeFondo> regimenes(Long idEmpresa, List<String> idsSerie, List<String> idsRegimen);

    List<Moneda> monedas(List<String> idsMoneda);

    /** Legacy {@code sql.batch(INSERT_SOLICITUD)} into {@code FMM.SolicitudAporteRescate}. */
    void insertarSolicitudes(List<SolicitudAporteRescate> solicitudes);
}
