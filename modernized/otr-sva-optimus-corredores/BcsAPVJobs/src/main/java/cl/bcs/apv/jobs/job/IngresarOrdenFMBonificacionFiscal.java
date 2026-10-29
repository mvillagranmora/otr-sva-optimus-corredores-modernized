package cl.bcs.apv.jobs.job;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;
import cl.bcs.apv.jobs.job.FilasBonificacion.ApoResFFMM;
import cl.bcs.apv.jobs.job.FilasBonificacion.ParametrosPrograma;
import cl.bcs.apv.jobs.job.FilasBonificacion.SerieFondoSolicitudes;
import cl.bcs.apv.jobs.job.contrato.ConsultarEmpresa;
import cl.bcs.apv.jobs.job.contrato.ConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.apv.jobs.job.contrato.CrearSolicitudAporteRescateNormal;
import cl.bcs.apv.jobs.job.contrato.CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate;
import cl.bcs.apv.jobs.job.contrato.MensajeSalida;
import cl.bcs.apv.jobs.job.contrato.ResultadoSolicitud;
import cl.bcs.apv.jobs.runtime.ApvJob;
import cl.bcs.apv.jobs.runtime.JobContext;

/**
 * Tax-bonus orders in mutual funds: for each requested {@code SolicitudAporteRescate} of a BF portfolio it asks the fund module for a new request of
 * {@code PorcentajeBonificacionFiscal} percent (legacy IngresarOrdenFMBonificacionFiscal.java:99-296). The three collaborators are reached through the
 * {@link Colaboradores} port with the legacy JSON contract.
 *
 * <p>Reproduced bug for bug (README_CARACTERIZACION.md, FMB-*): {@code Monto*pct/100} is exact, no rounding (FMB-002); the custody cap compares only
 * {@code NumeroCuotas}, so a by-amount rescue is never capped (FMB-012); the lines of the custody flow apply in sequence (FMB-013); one
 * {@code InInstrumento} is shared by all the series of a request (FMB-020); the custody flow is requested even without any request (FMB-027); collaborator
 * errors are only logged and the job commits (FMB-044); a missing {@code IdEmpresa} is company 0 (FMB-034).
 */
public final class IngresarOrdenFMBonificacionFiscal implements ApvJob {

    /** Legacy class name, the routing key of /internal/job-call. */
    public static final String CLASE = "cl.bcs.APV.jobs.IngresarOrdenFMBonificacionFiscal";

    private static final Logger log = LoggerFactory.getLogger(IngresarOrdenFMBonificacionFiscal.class);
    private static final String ID_TIPO_PROGRAMA = "INGRESAR_ORDEN_FFMM_BONIFICACION_FISCAL"; // :40
    // legacy ConstantesFondoMutuo (FondoMutuo pojo): CalculoPorMonto, CalculoPorCuota, CuentaInversion, SI
    private static final String CALCULO_POR_MONTO = "M";
    private static final String CALCULO_POR_CUOTA = "C";
    private static final String CUENTA_INVERSION = "CIN";
    private static final String SI = "S";

    private final SqlNombrado sql;
    private final ConfiguracionSistema configuracion;
    private final Colaboradores colaboradores;
    private final String sqlSelectFfmmBf = SqlNombrado.recurso("jobs/IngresarOrdenFMBonificacionFiscal_SelectFFMMBF");
    private final String sqlSelectParametros = SqlNombrado.recurso("jobs/IngresarOrdenFMBonificacionFiscal_SelectParametros");
    private final String sqlSelectSerieFondo = SqlNombrado.recurso("jobs/ComunesSolicitudes_SelectSerieFondo");

    public IngresarOrdenFMBonificacionFiscal(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, Colaboradores colaboradores) {
        this.sql = LogPrograma.sql(jdbc);
        this.configuracion = configuracion;
        this.colaboradores = colaboradores;
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        log.info("INICIO JOB");
        Long idPrograma = ctx.idPrograma();
        Long idEmpresa = 0L;
        Long idUsuario = 0L;
        String idModulo = "APV";
        List<Long> idsAporteRescateABonificar = new ArrayList<>();

        // :132-150 the parameters of the program (BAT.ParametrosPrograma); a later IdEmpresa wins (FMB-032)
        List<ParametrosPrograma> parametros = sql.consultar(sqlSelectParametros, Map.of("IdPrograma", idPrograma), ParametrosPrograma.class);
        for (ParametrosPrograma parametro : parametros) {
            if (parametro.NombreParametro.equals("IdEmpresa")) idEmpresa = Long.parseLong(parametro.Valor);
            if (parametro.NombreParametro.equals("IdUsuario")) idUsuario = Long.parseLong(parametro.Valor);
            if (parametro.NombreParametro.equals("IdSolicitudAporteRescate")) idsAporteRescateABonificar.add(Long.parseLong(parametro.Valor));
        }
        Long idPais = empresa(idEmpresa).IdPais; // :152-154 ConsultarEmpresa

        long porcentaje = ConfiguracionLegacy.parametroLong(configuracion, idEmpresa, "APV", "APV.PorcentajeBonificacionFiscal"); // :165
        String fechaSistema = ConfiguracionLegacy.parametro(configuracion, idEmpresa, "PLATAFORMA", "PLA.FechaSistema"); // :166

        Map<String, Object> paramsSelect = new HashMap<>();
        paramsSelect.put("IdsAporteRescate", idsAporteRescateABonificar);
        List<ApoResFFMM> aporteRescate = sql.consultar(sqlSelectFfmmBf, paramsSelect, ApoResFFMM.class);

        ConsultarFlujoCustodiaFondoMutuo.SalidaConsultarFlujoCustodiaFondoMutuo salidaFlujo =
                flujoCustodia(idEmpresa, idUsuario, idModulo, idPais, fechaSistema, aporteRescate);

        CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal entrada = new CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal();
        long contador = 1L;
        for (ApoResFFMM apor : aporteRescate) {
            entrada.p_SolicitudesAporteRescate.add(solicitud(apor, contador, porcentaje, salidaFlujo));
            contador++;
            log.info("DISTRIBUCION PARA EL FONDO : {}", apor.IdSerieFondo);
        }
        entrada.p_IdEmpresa = idEmpresa;
        entrada.p_IdUsuario = idUsuario;
        entrada.p_IdModulo = "APV";
        entrada.p_SimularOperacion = "N";
        entrada.p_UsaReglasNegocio = "S";

        if (entrada.p_SolicitudesAporteRescate.size() > 0) {
            CrearSolicitudAporteRescateNormal.SalidaCrearSolicitudAporteRescateNormal rpt = JsonLegacy.de(
                    colaboradores.invocar("CrearSolicitudAporteRescateNormal", JsonLegacy.aJson(entrada)),
                    CrearSolicitudAporteRescateNormal.SalidaCrearSolicitudAporteRescateNormal.class);
            log.info("FIN APORTE");
            for (ResultadoSolicitud fila : rpt.P_OUT_RESULTADO) { // a null list is a NullPointerException (FMB-048)
                log.info("RESULTADO FFMM : {}", fila.IdSolicitudAporteRescate);
            }
            for (MensajeSalida mje : rpt.P_OUT_MENSAJES) { // FMB-043..047: only ADVERTENCIA and ERROR are logged; the job still commits
                if (MensajeSalida.MENSAJE_ADVERTENCIA.equals(mje.tipo)) {
                    LogPrograma.agregar(sql, idPrograma, ID_TIPO_PROGRAMA, "ADVERTENCIAS FFMM : " + mje.descripcion);
                } else if (MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)) {
                    LogPrograma.agregar(sql, idPrograma, ID_TIPO_PROGRAMA, "ERRORES FFMM : " + mje.descripcion);
                }
            }
        } else {
            LogPrograma.agregar(sql, idPrograma, ID_TIPO_PROGRAMA, "NO CUENTA CON PORTAFOLIO BF");
        }
        log.info("FIN JOB");
        ctx.commit(); // :288 the job's own commit; the runner commits again
        return true;
    }

    /** Legacy {@code ConsultarEmpresa.logica(con, idEmpresa)}: the platform answers or raises "Empresa no existe". */
    private ConsultarEmpresa.InformacionEmpresa empresa(Long idEmpresa) throws Exception {
        Map<String, Object> entrada = new HashMap<>();
        entrada.put("IdEmpresa", idEmpresa);
        com.google.gson.JsonObject respuesta = colaboradores.invocar("ConsultarEmpresa", JsonLegacy.aJson(entrada));
        if (respuesta == null) {
            throw new Exception("Empresa no existe"); // ConsultarEmpresa.java:40-41 (no row)
        }
        return JsonLegacy.de(respuesta, ConsultarEmpresa.InformacionEmpresa.class);
    }

    /** Legacy :168-216: the custody flow of the funds of every request (requested even when there is none, FMB-027). */
    private ConsultarFlujoCustodiaFondoMutuo.SalidaConsultarFlujoCustodiaFondoMutuo flujoCustodia(Long idEmpresa, Long idUsuario, String idModulo, Long idPais,
            String fechaSistema, List<ApoResFFMM> aporteRescate) throws SQLException {
        ConsultarFlujoCustodiaFondoMutuo.EntradaConsultarFlujoCustodiaFondoMutuo entradaFlujo = new ConsultarFlujoCustodiaFondoMutuo.EntradaConsultarFlujoCustodiaFondoMutuo();
        entradaFlujo.p_IdEmpresa = idEmpresa;
        entradaFlujo.p_IdUsuario = idUsuario;
        entradaFlujo.p_IdModulo = idModulo;
        entradaFlujo.p_IdPais = idPais;
        entradaFlujo.p_FechaSaldo = fechaSistema;
        entradaFlujo.p_DiasProyeccion = 0;
        for (ApoResFFMM apor : aporteRescate) {
            ConsultarFlujoCustodiaFondoMutuo.InCarteras cartera = new ConsultarFlujoCustodiaFondoMutuo.InCarteras();
            ConsultarFlujoCustodiaFondoMutuo.InInstrumento instrumento = new ConsultarFlujoCustodiaFondoMutuo.InInstrumento();
            cartera.IdCliente = apor.IdCliente;
            cartera.IdPortafolio = apor.IdPortafolio;
            entradaFlujo.p_Carteras.add(cartera);
            Map<String, Object> params = new HashMap<>();
            params.put("IdEmpresa", idEmpresa);
            params.put("IdSerieFondo", apor.IdSerieFondo);
            for (SerieFondoSolicitudes serie : sql.consultar(sqlSelectSerieFondo, params, SerieFondoSolicitudes.class)) {
                instrumento.IdInstrumento = serie.IdInstrumento;
                instrumento.IdRegimenFondoMutuo = "BF";
                entradaFlujo.p_Instrumentos.add(instrumento); // the SAME object for every series (FMB-020)
            }
        }
        return JsonLegacy.de(colaboradores.invocar("ConsultarFlujoCustodiaFondoMutuo", JsonLegacy.aJson(entradaFlujo)),
                ConsultarFlujoCustodiaFondoMutuo.SalidaConsultarFlujoCustodiaFondoMutuo.class);
    }

    /** Legacy :218-273: the request for the bonus of one {@code ApoResFFMM} row. */
    private InSolicitudesAporteRescate solicitud(ApoResFFMM apor, long contador, long porcentaje,
            ConsultarFlujoCustodiaFondoMutuo.SalidaConsultarFlujoCustodiaFondoMutuo salidaFlujo) {
        InSolicitudesAporteRescate s = new InSolicitudesAporteRescate();
        BigDecimal pct = BigDecimal.valueOf(porcentaje);
        BigDecimal cien = BigDecimal.valueOf(100);
        s.IdInterno = contador;
        s.IdCliente = apor.IdCliente;
        s.IdPortafolio = apor.IdPortafolio;
        s.TipoSolicitud = apor.TipoSolicitud;
        s.IdSerieFondo = apor.IdSerieFondo;
        s.CalculoPorMontoCuota = apor.CalculoPorMontoCuota;
        s.EsPorMontoLiquido = apor.EsPorMontoLiquido;
        if (CALCULO_POR_MONTO.equals(s.CalculoPorMontoCuota)) {
            s.Monto = apor.Monto.multiply(pct).divide(cien); // exact, no rounding (FMB-002)
            s.NumeroCuotas = BigDecimal.ZERO;
        } else if (CALCULO_POR_CUOTA.equals(s.CalculoPorMontoCuota)) {
            s.Monto = BigDecimal.ZERO;
            s.NumeroCuotas = apor.NumeroCuotas.multiply(pct).divide(cien);
        } else {
            s.Monto = apor.Monto.multiply(pct).divide(cien);
            s.NumeroCuotas = apor.NumeroCuotas.multiply(pct).divide(cien);
        }
        if ("RESNORM".equalsIgnoreCase(apor.IdTipoMovimientoFondoMutuo)) {
            List<ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado> flujoCustodia = salidaFlujo.P_OUT_FLUJOFONDOMUTUOTOTALIZADO.stream()
                    .filter(custodia -> apor.IdPortafolio.equals(custodia.IdPortafolio) && (apor.IdSerieFondo.equals(custodia.Nemotecnico))).collect(Collectors.toList());
            for (ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado flujoC : flujoCustodia) { // lines apply in sequence (FMB-013)
                if (flujoC.CantidadDisponible.compareTo(s.NumeroCuotas) == -1) {
                    s.NumeroCuotas = flujoC.CantidadDisponible;
                    s.CalculoPorMontoCuota = CALCULO_POR_CUOTA;
                } else if ("T".equals(s.CalculoPorMontoCuota)) {
                    s.NumeroCuotas = flujoC.CantidadDisponible;
                    s.CalculoPorMontoCuota = "T";
                }
            }
            s.IdMonedaPago = apor.IdMonedaPago;
            s.IdMedioDePago = apor.IdMedioDePago;
        } else {
            s.IdMedioDePago = CUENTA_INVERSION; // IdMonedaPago stays unset (FMB-017)
        }
        s.IdPreferenciaPagoPortafolio = 0L;
        s.IdMoneda = apor.IdMoneda;
        s.Visado = apor.Visado;
        s.SolicitudBatch = apor.SolicitudBatch;
        s.OrigenOperacion = apor.OrigenOperacion;
        s.FechaOperacion = "0001-01-01";
        s.IdRegimenTributario = "BF";
        s.IndicadorMLT = "N";
        s.IdSerieFondoDestino = "";
        s.IdEmisorDestino = "";
        s.IdAutorizadoOrigen = 0L;
        s.Porcentaje = BigDecimal.valueOf(0);
        s.FolioContrato = apor.FolioContrato;
        s.EsPorMontoLiquido = "N"; // always N (FMB-017)
        s.IngresoAutomatico = SI;
        s.TipoContratoOperacion = apor.TipoContratoOperacion;
        s.FolioContratoOperacion = apor.FolioContratoOperacion;
        s.IdSolicitudAporteRescateRelacionado = apor.IdSolicitudAporteRescate;
        s.Glosa = apor.Glosa;
        return s;
    }
}
