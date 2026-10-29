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
import cl.bcs.tesoreria.javacall.kit.colaborador.RequisitosBasicos;
import cl.bcs.tesoreria.javacall.nor.SalidaServicios;
import cl.bcs.tesoreria.port.out.Colaboradores;

/**
 * Legacy {@code cl.bcs.tesoreria.nor.AnularNominasTransferencias} (TES-B-221..232): annuls transfer payrolls, their comprobantes and detail, releases the
 * original movements and sends the TES cargo/abono of the annulled comprobantes to AnularCargoAbono.
 *
 * <p>LEGACY DEFECT kept (see README TES-B-232): the requirements query is executed and its result ignored (a received nomina is annulled anyway).
 * LEGACY DEFECT kept (see README TES-B-228, TES-B-231): every exception is swallowed; the temporary table is not dropped and the changes already
 * made stay (the caller commits).
 */
@Service
public class AnularNominasTransferenciasService {

    private static final Logger log = LoggerFactory.getLogger(AnularNominasTransferenciasService.class);

    private static final String CREATE_TEMPORARY_TABLE = "CREATE LOCAL TEMPORARY TABLE #AnularNominasTransferencias_p_RegistrosAProcesar (\"Id\" BIGINT,\"Entidad\" NVARCHAR(300))";
    private static final String SQL_INSERT_REGISTROS = "INSERT INTO #AnularNominasTransferencias_p_RegistrosAProcesar (\"Id\",\"Entidad\") VALUES (:Id,:Entidad)";
    private static final String DROP_TEMPORARY_TABLE = "DROP TABLE #AnularNominasTransferencias_p_RegistrosAProcesar";

    private static final String SQL_ANULAR_NOMINA = SqlNombrado.recurso("AnularNominasTransferencias_AnularNomina");
    private static final String SQL_ANULAR_NOMINA_DETALLES = SqlNombrado.recurso("AnularNominasTransferencias_AnularNominaDetalles");
    private static final String SQL_ANULAR_PARTIDA_EGRESO = SqlNombrado.recurso("AnularNominasTransferencias_AnularComprobantePartidaEgreso");
    private static final String SQL_ANULAR_PARTIDA_EGRESO_DETALLE = SqlNombrado.recurso("AnularNominasTransferencias_AnularComprobantePartidaEgresoDetalle");
    private static final String SQL_ANULAR_PARTIDA_EGRESO_FORMAPAGO = SqlNombrado.recurso("AnularNominasTransferencias_AnularComprobantePartidaEgresoFormaPago");
    private static final String SQL_ANULAR_COMPROBANTES = SqlNombrado.recurso("AnularNominasTransferencias_AnularComprobantes");
    private static final String SQL_ANULAR_COMPROBANTE_DETALLE = SqlNombrado.recurso("AnularNominasTransferencias_AnularComprobanteDetalle");
    private static final String SQL_ANULAR_COMPROBANTE_FORMA_PAGO = SqlNombrado.recurso("AnularNominasTransferencias_AnularComprobanteFormaPago");
    private static final String SQL_LIBERAR_MOVIMIENTOS = SqlNombrado.recurso("AnularNominasTransferencias_UpdateLiberarMovimientosOriginales");
    private static final String SQL_LIBERAR_FORMA_PAGO_MOVIMIENTOS = SqlNombrado.recurso("AnularNominasTransferencias_UpdateLiberarFormaDePagoMovimientosOriginales");
    private static final String SQL_SELECT_ANULAR_CARGO_ABONO = SqlNombrado.recurso("AnularNominasTransferencias_SelectAnularCargosAbono");
    private static final String SQL_SELECT_REQUISITOS = SqlNombrado.recurso("AnularNominasTransferencias_SelectRequisitos");

    public record RegistroAnular(Long Id, String Entidad) {
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_FechaMovimiento;
        public List<RegistroAnular> p_RegistrosAAnular;
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

    /** Columns of the (ignored) requirements query. */
    public record Requisito(Integer Prioridad, Integer Cumple, String CodigoError, String CodigoDato, String ValorDato, String Entidad, Long Id) {
    }

    private final SqlNombrado sql;
    private final Colaboradores colaboradores;

    public AnularNominasTransferenciasService(NamedParameterJdbcTemplate jdbc, Colaboradores colaboradores) {
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
            if (in.p_RegistrosAAnular.isEmpty()) {
                mensajesSalida.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_VIENEN_MOVIMIENTOS", "No vienen movimientos"));
                out.parseMensajeSalida(mensajesSalida);
                return out;
            }

            sql.ejecutar(CREATE_TEMPORARY_TABLE);
            sql.lote(SQL_INSERT_REGISTROS, in.p_RegistrosAAnular);

            Set<Long> movimientosProcesar = new HashSet<>();
            in.p_RegistrosAAnular.forEach(reg -> movimientosProcesar.add(reg.Id()));
            if (in.p_UsaReglasNegocio.equals("S")) {
                mensajesSalida.addAll(RequisitosBasicos.revisar(colaboradores, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo).p_out_Mensajes);
                mensajesSalida.stream().filter(msj -> msj.tipo.equals(MensajeSalida.MENSAJE_ERROR)).forEach(msj -> out.addError(msj.codigo, msj.descripcion));
                // LEGACY DEFECT kept (see README TES-B-232): the requirements are queried and never evaluated
                sql.consultar(SQL_SELECT_REQUISITOS, Map.of(), Requisito.class);
            }

            if (!movimientosProcesar.isEmpty()) {
                sql.lote(SQL_ANULAR_NOMINA, in.p_RegistrosAAnular);
                sql.lote(SQL_ANULAR_NOMINA_DETALLES, in.p_RegistrosAAnular);
                sql.lote(SQL_ANULAR_PARTIDA_EGRESO, in.p_RegistrosAAnular);
                sql.lote(SQL_ANULAR_PARTIDA_EGRESO_DETALLE, in.p_RegistrosAAnular);
                sql.lote(SQL_ANULAR_PARTIDA_EGRESO_FORMAPAGO, in.p_RegistrosAAnular);
                sql.lote(SQL_ANULAR_COMPROBANTES, in.p_RegistrosAAnular);
                sql.lote(SQL_ANULAR_COMPROBANTE_DETALLE, in.p_RegistrosAAnular);
                sql.lote(SQL_ANULAR_COMPROBANTE_FORMA_PAGO, in.p_RegistrosAAnular);
                sql.lote(SQL_LIBERAR_MOVIMIENTOS, in.p_RegistrosAAnular);
                sql.lote(SQL_LIBERAR_FORMA_PAGO_MOVIMIENTOS, in.p_RegistrosAAnular);

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
            }

            sql.ejecutar(DROP_TEMPORARY_TABLE);
        } catch (Exception e) {
            // LEGACY DEFECT kept (see README TES-B-228, TES-B-231): every exception is swallowed, the temporary table is left open
            log.debug("AnularNominasTransferencias", e);
        }
        log.info("TERMINO SERVICIO");
        return out;
    }
}
