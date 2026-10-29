package cl.bcs.fondomutuo.certificacion;

import java.util.List;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.ComprobanteNoLiquidado;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Estado;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.IndicadorPortafolio;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.ParametroOperaCSN;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Persona;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Traspaso;

/* RED SKELETON (F3 W3, T1 CER): the compiling shape the characterization tests expect (API_ASSUMED.md). The behaviour is NOT written yet: every entry point
 * fails closed with UnsupportedOperationException, so the CER tests are red until Step 2 replaces this body. Not registered as a bean, no handler, no list. */
public class CertificarAporteRescateDatosJdbc implements CertificarAporteRescateDatos {

    private final ConexionUnidadDeTrabajo conexion;

    public CertificarAporteRescateDatosJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    private static UnsupportedOperationException pendiente(String metodo) {
        return new UnsupportedOperationException("CertificarAporteRescateDatosJdbc." + metodo + ": not written yet (F3 W3 T1, Step 2)");
    }

    @Override
    public List<SolicitudProceso> solicitudes(Long idEmpresa, List<Long> idSolicitudes) {
        throw pendiente("solicitudes");
    }

    @Override
    public List<Estado> estados() {
        throw pendiente("estados");
    }

    @Override
    public List<ComprobanteNoLiquidado> comprobantesNoLiquidados(List<Long> idSolicitudes) {
        throw pendiente("comprobantesNoLiquidados");
    }

    @Override
    public List<Traspaso> traspasosApv(List<Long> idSolicitudes) {
        throw pendiente("traspasosApv");
    }

    @Override
    public List<Traspaso> traspasosNoApv(List<Long> idSolicitudes) {
        throw pendiente("traspasosNoApv");
    }

    @Override
    public List<Persona> personasPorDocumento(String documentoIdentidad) {
        throw pendiente("personasPorDocumento");
    }

    @Override
    public List<IndicadorPortafolio> indicadoresPortafolio(List<Long> idPortafolios) {
        throw pendiente("indicadoresPortafolio");
    }

    @Override
    public List<ParametroOperaCSN> parametrosOperaConSaldoNegativo(List<Long> idPortafolios) {
        throw pendiente("parametrosOperaConSaldoNegativo");
    }
}
