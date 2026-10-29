package cl.bcs.apv.jobs.job;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;
import cl.bcs.apv.jobs.job.contrato.CrearOrdenes;
import cl.bcs.apv.jobs.runtime.ApvJob;
import cl.bcs.apv.jobs.runtime.JobContext;

/**
 * Hard-coded demo order job (legacy IngresarOrdenBonificacionFiscal.java:47-142): logs on BAT.LogPrograma and sends ONE fixed test order to CrearOrdenes.
 *
 * <p>Reproduced bug for bug (README_CARACTERIZACION.md, OBF-001..006): the order ("PRUEBA APV V1": portfolio 20021, instrument 31, quantity 2, limit price
 * 406.26, user 5) is sent on EVERY run; a failure of {@code CrearOrdenes} (any {@code Exception}) is swallowed after a rollback that also discards the log
 * row; the job returns true, committing three times in all (two of its own, one of the runner); the messages of {@code CrearOrdenes} are ignored.
 */
public final class IngresarOrdenBonificacionFiscal implements ApvJob {

    /** Legacy class name, the routing key of /internal/job-call. */
    public static final String CLASE = "cl.bcs.APV.jobs.IngresarOrdenBonificacionFiscal";

    private static final Logger log = LoggerFactory.getLogger(IngresarOrdenBonificacionFiscal.class);
    private static final String ID_TIPO_PROGRAMA = "INGRESAR_ORDEN_BONIFICACION_FISCAL"; // :22

    private final SqlNombrado sql;
    private final Colaboradores colaboradores;

    public IngresarOrdenBonificacionFiscal(NamedParameterJdbcTemplate jdbc, Colaboradores colaboradores) {
        this.sql = LogPrograma.sql(jdbc);
        this.colaboradores = colaboradores;
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        log.info("INICIO JOB");
        Long idPrograma = ctx.idPrograma(); // :39 getJobId() (debug branch not ported, RULE-076)
        LogPrograma.agregar(sql, idPrograma, ID_TIPO_PROGRAMA, "PRUEBA APV V1"); // :47

        CrearOrdenes.CrearOrdenesEntrada entrada = new CrearOrdenes.CrearOrdenesEntrada();
        entrada.p_IdEmpresa = 1L;
        entrada.p_IdUsuario = 5L;
        entrada.p_IdModulo = "APV";
        entrada.p_simularOperacion = "N";
        entrada.p_UsaReglasNegocio = "S";
        entrada.p_Ordenes = new ArrayList<>(List.of(ordenDePrueba())); // ArrayList: the legacy list is mutable

        try {
            CrearOrdenes.CrearOrdenesSalida salida = JsonLegacy.de(colaboradores.invocar("CrearOrdenes", JsonLegacy.aJson(entrada)),
                    CrearOrdenes.CrearOrdenesSalida.class);
            log.info("{}", salida); // legacy System.out.println(ObjOut.toString()); its messages are ignored (OBF-002)
            ctx.commit();
        } catch (Exception er) {
            ctx.rollback(); // :130-131 also undoes the log row (OBF-003)
        }
        ctx.commit();
        return true;
    }

    /** Legacy :52-117, field by field. */
    private static CrearOrdenes.ObjOrdenesEntrada ordenDePrueba() {
        CrearOrdenes.ObjOrdenesEntrada orden = new CrearOrdenes.ObjOrdenesEntrada();
        orden.IdInterno = 1L;
        orden.FechaRecepcion = "0001-01-01 00:00:00";
        orden.FechaVigencia = "2017-11-09";
        orden.FechaEmision = "0001-01-01";
        orden.FechaOperacion = "0001-01-01";
        orden.IdCondicionDeLiquidacion = "T2";
        orden.IdTipoOperacion = "COMACN";
        orden.IdTipoVigencia = "SH";
        orden.Glosa = "";
        orden.Cantidad = BigDecimal.valueOf(2L);
        orden.MontoNeto = BigDecimal.ZERO;
        orden.ValorRescate = BigDecimal.valueOf(0L);
        orden.IdBoveda = "DCV";
        orden.IdFormaCustodia = "CUS";
        orden.IdPorcentajeVisibilidad = 1L;
        orden.PrecioLimite = BigDecimal.valueOf(406.26);
        orden.EsFacturaInmediata = "N";
        orden.IdTipoPrecio = "M";
        orden.IdPortafolio = 20021L;
        orden.IdTipoCanalIngreso = "AGE";
        orden.IdTipoReajuste = "";
        orden.IdEmisor = "AGUAS";
        orden.IdBolsa = "BCS";
        orden.EspecificaBolsa = "S";
        orden.IdMonedaLiquidacionOrden = "CLP";
        orden.TipoCambio = null;
        orden.IdInstrumento = 31L;
        orden.IdInstrumentoSubyacente = 0L;
        orden.IdCondicionDeLiquidacionPlazo = "";
        orden.TasaVentaCorta = BigDecimal.valueOf(0);
        orden.PlazoVentaCorta = 0L;
        orden.EsVentaCorta = "N";
        orden.TasaSimultanea = BigDecimal.valueOf(0);
        orden.PlazoSimultanea = 0L;
        orden.PlazoIF = 0L;
        orden.TasaRFIF = BigDecimal.valueOf(0);
        orden.EsOD = "N";
        orden.EsFungible = "N";
        orden.IdTipoLibroOrden = "NORMAL";
        orden.IdLibroOrdenes = null;
        orden.IdCuentaCustodioPortafolio = null;
        orden.EsPorMonto = "S";
        orden.IdCorredorCorresponsal = null;
        orden.IdPersonaAutorizadoDarOrdenes = null;
        orden.IdTipoCanalDistribucion = "MAN";
        orden.EsOrdenEspecial = "N";
        orden.PagoCobroCuentaInversion = "N";
        orden.IdPreferenciaPagoPortafolio = 20021L;
        orden.BaseTasa = 0L;
        orden.TipoInteres = "";
        orden.IdMecanismoCaptacion = "OV";
        return orden;
    }
}
