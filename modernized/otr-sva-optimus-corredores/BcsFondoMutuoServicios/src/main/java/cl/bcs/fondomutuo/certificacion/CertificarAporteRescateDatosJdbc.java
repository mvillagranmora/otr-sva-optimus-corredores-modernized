package cl.bcs.fondomutuo.certificacion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Cobro;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.ComprobanteNoLiquidado;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Estado;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.IndicadorPortafolio;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.OrigenReinversion;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.ParametroOperaCSN;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Persona;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.ReinversionDestino;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudReinversion;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Traspaso;

/** {@link CertificarAporteRescateDatos} (no bean until T5) over the legacy SQL, verbatim (no ORDER BY added: the row order is the engine's). */
public class CertificarAporteRescateDatosJdbc implements CertificarAporteRescateDatos {

    private static final String SQL_SELECT_SOLICITUDES = SqlRecursos.modulo("CertificarSolicitudAporteRescate_SelectSolicitudes.sql");
    private static final String SQL_SELECT_ESTADOS = SqlRecursos.modulo("CertificarSolicitudAporteRescate_SelectEstados.sql");
    private static final String SQL_SELECT_COMPROBANTES_NO_LIQUIDADOS = SqlRecursos.modulo("CertificarSolicitudAporteRescate_SelectComprobantesNoLiquidados.sql");
    private static final String SQL_SELECT_TRASPASO_APV_ASOCIADO = SqlRecursos.modulo("CertificarSolicitudAporteRescate_ConsultaTraspasoAPVAsociado.sql");
    private static final String SQL_SELECT_TRASPASO_NO_APV_ASOCIADO = SqlRecursos.modulo("CertificarSolicitudAporteRescate_ConsultaTraspasoNoAPVAsociado.sql");
    private static final String SQL_SELECT_PERSONA = SqlRecursos.modulo("CertificarSolicitudAporteRescate_SelectPersonaPreferenciaPagoTGR.sql");
    private static final String SQL_SELECT_INDICADORES_PORTAFOLIO_FFMM = SqlRecursos.modulo("ComunesSolicitudes_SelectIndicadoresPortafolioFFMM.sql");
    private static final String SQL_SELECT_PARAMETRO_OPERA_CON_SALDO_NEGATIVO = SqlRecursos.modulo("CrearSolicitudAporteRescateNormal_SelectParametroOperaConSaldoNegativo.sql");

    private static final String SQL_SELECT_GENERA_CARGO_ABONO = SqlRecursos.modulo("CertificarSolicitudAporteRescate_SelectTipoMovimientoFondoMutuoGeneraCargoAbono.sql");
    private static final String SQL_SELECT_COBRO_IVA = SqlRecursos.modulo("CertificarSolicitudAporteRescate_SelectCobroIva.sql");
    private static final String SQL_SELECT_REINVERSION_DESTINO = SqlRecursos.modulo("ComunesSolicitudes_SelectReinversionSerieFondoDestino.sql");
    private static final String SQL_SELECT_REINVERSION_SERIE_FONDO = SqlRecursos.modulo("ComunesSolicitudes_SelectReinversionSerieFondosSolicitudRescateReinversion.sql");
    private static final String SQL_SELECT_MONTO_MONEDA_SISTEMA = SqlRecursos.modulo("CertificarSolicitudAporteRescate_SelectMontoMonedaSistemaSolicitudesReinversion.sql");

    /** Legacy {@code GeneraCargoAbono}: a movement type without row reads as {@code "N"}. */
    public static class GeneraCargoAbonoFila {
        public String GeneraCargoAbono = "N";
    }

    private final ConexionUnidadDeTrabajo conexion;

    public CertificarAporteRescateDatosJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    private <T> List<T> consultar(String sql, Map<String, Object> params, Class<T> tipo) {
        return conexion.usar(con -> SqlNombrado.consultar(con, sql, params, tipo));
    }

    private static Map<String, Object> porIdSolicitudes(List<Long> idSolicitudes) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdSolicitudAporteRescate", idSolicitudes);
        return params;
    }

    @Override
    public List<SolicitudProceso> solicitudes(Long idEmpresa, List<Long> idSolicitudes) {
        Map<String, Object> params = porIdSolicitudes(idSolicitudes);
        params.put("IdEmpresa", idEmpresa);
        return consultar(SQL_SELECT_SOLICITUDES, params, SolicitudProceso.class);
    }

    @Override
    public List<Estado> estados() {
        return consultar(SQL_SELECT_ESTADOS, new HashMap<>(), Estado.class);
    }

    @Override
    public List<ComprobanteNoLiquidado> comprobantesNoLiquidados(List<Long> idSolicitudes) {
        return consultar(SQL_SELECT_COMPROBANTES_NO_LIQUIDADOS, porIdSolicitudes(idSolicitudes), ComprobanteNoLiquidado.class);
    }

    @Override
    public List<Traspaso> traspasosApv(List<Long> idSolicitudes) {
        return consultar(SQL_SELECT_TRASPASO_APV_ASOCIADO, porIdSolicitudes(idSolicitudes), Traspaso.class);
    }

    @Override
    public List<Traspaso> traspasosNoApv(List<Long> idSolicitudes) {
        return consultar(SQL_SELECT_TRASPASO_NO_APV_ASOCIADO, porIdSolicitudes(idSolicitudes), Traspaso.class);
    }

    @Override
    public List<Persona> personasPorDocumento(String documentoIdentidad) {
        Map<String, Object> params = new HashMap<>();
        params.put("DocumentoIdentidad", documentoIdentidad);
        return consultar(SQL_SELECT_PERSONA, params, Persona.class);
    }

    @Override
    public List<IndicadorPortafolio> indicadoresPortafolio(List<Long> idPortafolios) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdPortafolio", idPortafolios);
        return consultar(SQL_SELECT_INDICADORES_PORTAFOLIO_FFMM, params, IndicadorPortafolio.class);
    }

    @Override
    public List<ParametroOperaCSN> parametrosOperaConSaldoNegativo(List<Long> idPortafolios) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdPortafolio", idPortafolios);
        return consultar(SQL_SELECT_PARAMETRO_OPERA_CON_SALDO_NEGATIVO, params, ParametroOperaCSN.class);
    }

    @Override
    public String generaCargoAbono(String idTipoMovimientoFondoMutuo) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdTipoMovimientoFondoMutuo", idTipoMovimientoFondoMutuo);
        return consultar(SQL_SELECT_GENERA_CARGO_ABONO, params, GeneraCargoAbonoFila.class).stream().findFirst().orElse(new GeneraCargoAbonoFila()).GeneraCargoAbono;
    }

    @Override
    public List<Cobro> cobrosIva() {
        return consultar(SQL_SELECT_COBRO_IVA, new HashMap<>(), Cobro.class);
    }

    @Override
    public List<ReinversionDestino> destinosReinversion(List<Long> idSolicitudes) {
        return consultar(SQL_SELECT_REINVERSION_DESTINO, porIdSolicitudes(idSolicitudes), ReinversionDestino.class);
    }

    @Override
    public List<OrigenReinversion> seriesOrigenReinversion(Long idEmpresa, List<Long> idSolicitudes) {
        Map<String, Object> params = porIdSolicitudes(idSolicitudes);
        params.put("IdEmpresa", idEmpresa);
        return consultar(SQL_SELECT_REINVERSION_SERIE_FONDO, params, OrigenReinversion.class);
    }

    @Override
    public List<SolicitudReinversion> sumaMontoMonedaSistema(List<Long> idRescatesRelacionados) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdSolicitudAporteRescateRelacionado", idRescatesRelacionados);
        return consultar(SQL_SELECT_MONTO_MONEDA_SISTEMA, params, SolicitudReinversion.class);
    }
}
