package cl.bcs.cuentainversion.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.cuentainversion.port.out.ConfiguracionSistemaPort;
import cl.bcs.cuentainversion.port.out.DiccionarioMensajes;
import cl.bcs.cuentainversion.service.CrearCargoAbonoService.CargoAbonoAGenerar;
import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;
import cl.bcs.utils.Utils;

/**
 * Turns the records of an uploaded bulk load into pending cash movements (through {@link CrearCargoAbonoService}) and records,
 * per record, whether it was accepted (OK) or rejected (INV) with the reason; the header becomes PRO when every record
 * was accepted and INV otherwise (README CTA-A-120..124).
 */
@Service
public class ProcesaCargaSeleccionadaService {

    private static final Logger log = LoggerFactory.getLogger(ProcesaCargaSeleccionadaService.class);

    private static final String SQL_SELECT_DETALLE = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/CrearCargoAbono_SelectObtieneDetalleCarga.sql");
    private static final String SQL_UPDATE_CABECERA = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/CrearCargoAbono_UpdateActualizaEstadoCabecera.sql");
    private static final String SQL_UPDATE_DETALLE = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/CrearCargoAbono_UpdateActualizaDetalle.sql");

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_FechaCarga;
        public String p_IdCargaMasiva;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }

    /** Detail row of the load as read by the SQL (portfolio already resolved from the client and the code). */
    private record DetalleCarga(Long idCargaMasivaDetalle, String documentoIdentidad, Long idPortafolio, String idMoneda,
                                String idTipoMovimiento, String idConcepto, BigDecimal monto, String descripcion) {
    }

    private record ResultadoRegistro(Long idCargaMasivaDetalle, String idEstado, String observacion, String descripcion) {
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final CrearCargoAbonoService crearCargoAbono;
    private final ConfiguracionSistemaPort configuracion;
    private final DiccionarioMensajes diccionario;

    public ProcesaCargaSeleccionadaService(NamedParameterJdbcTemplate jdbc, CrearCargoAbonoService crearCargoAbono,
                                           ConfiguracionSistemaPort configuracion, DiccionarioMensajes diccionario) {
        this.jdbc = jdbc;
        this.crearCargoAbono = crearCargoAbono;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        Entrada entrada = Utils.getGsonBuilder().fromJson(params, Entrada.class);
        Salida salida = procesar(entrada);
        salida.P_OUT_ERRORESCONTADOR = MensajeSalida.countErrors(salida.P_OUT_MENSAJES);
        return Utils.getGsonBuilder().toJsonTree(salida).getAsJsonObject();
    }

    private Salida procesar(Entrada entrada) {
        Salida salida = new Salida();
        try {
            String fechaSistema = CtaJavaCallInfra.parametro(configuracion, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
            List<DetalleCarga> detalle = jdbc.query(SQL_SELECT_DETALLE,
                    new MapSqlParameterSource("p_IdCargaMasivaDetalle", entrada.p_IdCargaMasiva),
                    (rs, i) -> new DetalleCarga(longOrNull(rs.getObject("IdCargaMasivaDetalle")), rs.getString("DocumentoIdentidad"),
                            longOrNull(rs.getObject("IdPortafolio")), rs.getString("IdMoneda"), rs.getString("IdTipoMovimiento"),
                            rs.getString("IdConcepto"), rs.getBigDecimal("Monto"), rs.getString("Descripcion")));

            CrearCargoAbonoService.Entrada crear = new CrearCargoAbonoService.Entrada();
            crear.p_IdEmpresa = entrada.p_IdEmpresa;
            crear.p_IdUsuario = entrada.p_IdUsuario;
            crear.p_IdModulo = "CTA";
            crear.p_simularOperacion = "N";
            crear.p_UsaReglasNegocio = "S";
            crear.p_FechaSistema = fechaSistema;
            crear.p_EsIngresoManual = "S";
            detalle.forEach(d -> crear.p_CargoAbonoAGenerar.add(movimientoDe(d, fechaSistema)));
            CrearCargoAbonoService.Salida creados = crearCargoAbono.crear(crear);

            List<ResultadoRegistro> resultados = new ArrayList<>();
            List<String> descripcionesError = new ArrayList<>();
            long errores = 0;
            for (MensajeSalida mensaje : creados.P_OUT_MENSAJES) {
                boolean error = mensaje.tipo.equals(MensajeSalida.ERROR);
                if (error) {
                    descripcionesError.add(mensaje.descripcion);
                    errores++;
                } else {
                    salida.P_OUT_MENSAJES.add(mensaje);
                }
                resultados.add(new ResultadoRegistro(mensaje.id, error ? "INV" : "OK", mensaje.codigo, mensaje.descripcion));
            }

            // LEGACY DEFECT kept (see README CTA-A-123): two messages with the same id make the lookup fail ("key field is not unique")
            Set<Long> idsProcesados = new HashSet<>();
            for (MensajeSalida mensaje : creados.P_OUT_MENSAJES) {
                if (!idsProcesados.add(mensaje.id)) {
                    throw new Exception("key field is not unique");
                }
            }
            // LEGACY DEFECT kept (see README CTA-A-121): a record dropped silently by CrearCargoAbono counts as unprocessed but is not an error of the header
            detalle.stream().filter(d -> !idsProcesados.contains(d.idCargaMasivaDetalle()))
                    .forEach(d -> descripcionesError.add(d.documentoIdentidad()));
            if (!descripcionesError.isEmpty()) {
                salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.ERROR, "UI_CARGAS_NO_PROCESADAS",
                        diccionario.traducir(entrada.p_IdEmpresa, "UI_CARGAS_NO_PROCESADAS",
                                DiccionarioMensajes.valores("cantidad", descripcionesError.size()))));
            }

            jdbc.batchUpdate(SQL_UPDATE_DETALLE, resultados.stream()
                    .map(r -> (SqlParameterSource) new MapSqlParameterSource()
                            .addValue("p_IdCargaMasivaDetalle", r.idCargaMasivaDetalle())
                            .addValue("p_IdEstado", r.idEstado())
                            .addValue("p_ObservacionValidacion", r.observacion())
                            .addValue("p_Descripcion", r.descripcion()))
                    .toArray(SqlParameterSource[]::new));
            jdbc.update(SQL_UPDATE_CABECERA, new MapSqlParameterSource()
                    .addValue("p_IdEstado", errores == 0 ? "PRO" : "INV")
                    .addValue("p_IdCargaMasiva", entrada.p_IdCargaMasiva));
        } catch (Exception e) {
            // LEGACY DEFECT kept (see README CTA-A-123): any exception becomes this generic message
            log.warn("ProcesaCargaSeleccionada failed", e);
            salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.ERROR, "UI_PROCESO_EJECUTADO_ERROR",
                    " Error en procesamiento de carga de archivo cargado, verifique!"));
        }
        return salida;
    }

    /** Movement to create for a record: same-day settlement (T0), no operation or comprobante data. */
    private static CargoAbonoAGenerar movimientoDe(DetalleCarga d, String fechaSistema) {
        CargoAbonoAGenerar m = new CargoAbonoAGenerar();
        m.NumeroCargoAbono = d.idCargaMasivaDetalle();
        m.IdPortafolio = d.idPortafolio();
        m.IdMoneda = d.idMoneda();
        m.IdTipoMovimiento = d.idTipoMovimiento();
        m.IdConcepto = d.idConcepto();
        m.Descripcion = d.descripcion();
        m.IdEjecutivo = 0L;
        m.Monto = d.monto();
        m.IdCondicionDeLiquidacion = "T0";
        m.FechaCondicionDeLiquidacion = fechaSistema;
        m.IdSucursal = 0L;
        m.IdTipoProducto = "";
        m.IdTipoOperacion = "";
        m.IdOperacion = 0L;
        m.FechaOperacion = "0001-01-01";
        m.FechaLiquidacionOperacion = "0001-01-01";
        m.IdMonedaOperacion = "";
        m.TipoCambioOperacion = BigDecimal.ZERO;
        m.Cantidad = BigDecimal.ZERO;
        m.Nemo = "";
        m.EsIndicadorBilateral = "N";
        m.EsCargoAbonoPorTraspaso = "N";
        m.GeneroContraparteEnTesoreria = "N";
        m.CertificadoAutomatico = "N";
        return m;
    }

    private static Long longOrNull(Object v) {
        return v == null ? null : ((Number) v).longValue();
    }
}
