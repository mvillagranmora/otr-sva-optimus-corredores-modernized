package cl.bcs.tesoreria.javacall;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.javacall.kit.colaborador.AnularCargoAbono;
import cl.bcs.tesoreria.javacall.kit.colaborador.AnularCargoAbono.AnularCargoAbono_Entrada;
import cl.bcs.tesoreria.javacall.kit.colaborador.AnularCargoAbono.CargoAbono_Entrada;
import cl.bcs.tesoreria.javacall.nor.SalidaServicios;
import cl.bcs.tesoreria.port.out.Colaboradores;

/**
 * Legacy {@code cl.bcs.tesoreria.nor.QuitarDetalleNominasTransferencias} (TES-B-233..242): removes comprobantes from a transfer payroll, recalculates the
 * payroll and partida amounts and sends the TES cargo/abono of the removed comprobantes to AnularCargoAbono.
 *
 * <p>LEGACY DEFECT kept (see README TES-B-239): only the nomina of the FIRST register is recalculated.
 * LEGACY DEFECT kept (see README TES-B-234): removing every detail leaves the nomina and partida amounts NULL.
 * LEGACY DEFECT kept (see README TES-B-241): comprobante and detail states go back to ING even for a comprobante outside the nomina.
 * LEGACY DEFECT kept (see README QuitarDetalle section): every exception is swallowed and the temporary table is then left open.
 */
@Service
public class QuitarDetalleNominasTransferenciasService {

    private static final Logger log = LoggerFactory.getLogger(QuitarDetalleNominasTransferenciasService.class);

    private static final String CREATE_TEMPORARY_TABLE = "CREATE LOCAL TEMPORARY TABLE #QuitarDetalle_p_RegistrosAProcesar (\"IdComprobante\" BIGINT,\"IdNominaTransferencia\" BIGINT)";
    private static final String SQL_INSERT_REGISTROS = "INSERT INTO #QuitarDetalle_p_RegistrosAProcesar (\"IdComprobante\",\"IdNominaTransferencia\") VALUES (:IdComprobante,:IdNominaTransferencia)";
    private static final String DROP_TEMPORARY_TABLE = "DROP TABLE #QuitarDetalle_p_RegistrosAProcesar";

    private static final String SQL_QUITAR_DETALLE_FORMA_PAGO = SqlNombrado.recurso("QuitarDetalleNominasTransferencias_UpdateQuitarDetalleComprobanteFormaPago");
    private static final String SQL_QUITAR_DETALLE_COMPROBANTE = SqlNombrado.recurso("QuitarDetalleNominasTransferencias_UpdateQuitarDetalleComprobante");
    private static final String SQL_QUITAR_DETALLE_COMPROBANTE_DETALLE = SqlNombrado.recurso("QuitarDetalleNominasTransferencias_UpdateQuitarDetalleComprobanteDetalle");
    private static final String SQL_DELETE_NOMINA_DETALLE = SqlNombrado.recurso("QuitarDetalleNominasTransferencias_DeleteQuitarDetalleNominaTransferenciaDetalle");
    private static final String SQL_RECALCULAR_MONTO_NOMINA = SqlNombrado.recurso("QuitarDetalleNominasTransferencias_UpdateRecalcularMontoNominaTransferencia");
    private static final String SQL_SET_MONTO_PARTIDA_EGRESO = SqlNombrado.recurso("QuitarDetalleNominasTransferencias_UpdateSetMontoComprobantePartidaEgreso");
    private static final String SQL_SET_MONTO_DETALLE_PARTIDA_EGRESO = SqlNombrado.recurso("QuitarDetalleNominasTransferencias_UpdateSetMontoComprobanteDetallePartidaEgreso");
    private static final String SQL_SET_MONTO_FORMA_PAGO_PARTIDA_EGRESO = SqlNombrado.recurso("QuitarDetalleNominasTransferencias_UpdateSetMontoComprobanteFormaPagoPartidaEgreso");
    private static final String SQL_SELECT_ANULAR_CARGO_ABONO = SqlNombrado.recurso("QuitarDetalleNominasTransferencias_SelectAnularCargosAbono");

    public record RegistroQuitarDetalle(Long IdComprobante, Long IdNominaTransferencia) {
    }

    public record NominaTransferencia(Long IdNominaTransferencia) {
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_FechaMovimiento;
        public List<RegistroQuitarDetalle> p_RegistrosAProcesar;
    }

    public static class Resultado {
        public String Campo;
        public Long Id;
        public String Servicio;
        public String Resultado;
    }

    public static class Salida extends SalidaServicios {
        public List<Resultado> p_out_ResultadoProceso = new ArrayList<>();
    }

    private final SqlNombrado sql;
    private final Colaboradores colaboradores;

    public QuitarDetalleNominasTransferenciasService(NamedParameterJdbcTemplate jdbc, Colaboradores colaboradores) {
        this.sql = new SqlNombrado(jdbc);
        this.colaboradores = colaboradores;
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        var gson = JsonLegacy.gson();
        Entrada in = gson.fromJson(entrada.toString(), Entrada.class);
        return gson.toJsonTree(logica(in)).getAsJsonObject();
    }

    Salida logica(Entrada in) {
        log.info("INICIO SERVICIO");
        Salida out = new Salida();
        List<MensajeSalida> mensajesSalida = new ArrayList<>();
        try {
            if (in.p_RegistrosAProcesar.isEmpty()) {
                mensajesSalida.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_VIENEN_MOVIMIENTOS", "No vienen movimientos"));
                out.parseMensajeSalida(mensajesSalida);
                return out;
            }

            sql.ejecutar(CREATE_TEMPORARY_TABLE);
            sql.lote(SQL_INSERT_REGISTROS, in.p_RegistrosAProcesar);

            Set<Long> movimientosProcesar = new HashSet<>();
            in.p_RegistrosAProcesar.forEach(reg -> {
                movimientosProcesar.add(reg.IdComprobante());
                movimientosProcesar.add(reg.IdNominaTransferencia());
            });

            if (!movimientosProcesar.isEmpty()) {
                List<CargoAbono_Entrada> cargosAbonoAnular = sql.consultar(SQL_SELECT_ANULAR_CARGO_ABONO, Map.of(), CargoAbono_Entrada.class);
                if (!cargosAbonoAnular.isEmpty()) {
                    AnularCargoAbono_Entrada anular = new AnularCargoAbono_Entrada();
                    anular.p_IdEmpresa = in.p_IdEmpresa;
                    anular.p_IdUsuario = in.p_IdUsuario;
                    anular.p_IdModulo = in.p_IdModulo;
                    anular.p_FechaMovimiento = in.p_FechaMovimiento;
                    anular.p_simularOperacion = "N";
                    anular.p_UsaReglasNegocio = "S";
                    anular.p_EjecutaAutorizacion = "N";
                    anular.p_OmiteRegistroSinAutorizar = "N";
                    anular.p_EsAnulacionManual = "N";
                    anular.p_CargoAbonoAAnular = cargosAbonoAnular;
                    out.P_OUT_ERRORESCONTADOR += AnularCargoAbono.invocar(colaboradores, anular).P_OUT_ERRORESCONTADOR;
                }

                sql.lote(SQL_DELETE_NOMINA_DETALLE, in.p_RegistrosAProcesar);
                sql.lote(SQL_QUITAR_DETALLE_FORMA_PAGO, in.p_RegistrosAProcesar);
                sql.lote(SQL_QUITAR_DETALLE_COMPROBANTE, in.p_RegistrosAProcesar);
                sql.lote(SQL_QUITAR_DETALLE_COMPROBANTE_DETALLE, in.p_RegistrosAProcesar);
                // LEGACY DEFECT kept (see README TES-B-239): only the nomina of the first register is recalculated
                List<NominaTransferencia> primera = List.of(new NominaTransferencia(in.p_RegistrosAProcesar.get(0).IdNominaTransferencia()));
                sql.lote(SQL_RECALCULAR_MONTO_NOMINA, primera);
                sql.lote(SQL_SET_MONTO_PARTIDA_EGRESO, primera);
                sql.lote(SQL_SET_MONTO_DETALLE_PARTIDA_EGRESO, primera);
                sql.lote(SQL_SET_MONTO_FORMA_PAGO_PARTIDA_EGRESO, primera);
            }

            sql.ejecutar(DROP_TEMPORARY_TABLE);
        } catch (Exception e) {
            // LEGACY DEFECT kept (see README QuitarDetalle section): every exception is swallowed, the temporary table is left open
            log.debug("QuitarDetalleNominasTransferencias", e);
        }
        log.info("TERMINO SERVICIO");
        return out;
    }
}
