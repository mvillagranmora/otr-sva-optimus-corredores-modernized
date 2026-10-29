package cl.bcs.tesoreria.javacall;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.ComprobanteLiquidar_Entrada;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.LiquidarComprobante_Entrada;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.LiquidarComprobante_Salida;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.ResultadoProceso_Salida;
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;

/**
 * Legacy {@code cl.bcs.tesoreria.LiquidarComprobanteTodos}, behaviours TES-A-400..412: liquidates every comprobante of the grid that matches a set of
 * filters ("-1" means "any"). The comprobantes found are handed to {@link LiquidarComprobanteService#logica} with business rules on, without
 * simulation and without a selection of formas de pago (so all of them are liquidated).
 */
@Service
public class LiquidarComprobanteTodosService {

    private static final String SQL_SELECT_COMPROBANTES_A_LIQUIDAR_GRILLA = LiquidarComprobanteSql.cargar("sql/LiquidarComprobanteTodos_TodosComprobantesALiquidar.sql");

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

    public static class LiquidarComprobanteTodos_Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public FiltrosGrilla p_Filtros;
        public String p_FechaMovimiento;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
    }

    public static class FiltrosGrilla {
        public String IdTipoMovimiento;
        public Long IdPersona;
        public Long IdPortafolioTitularPago;
        public Long IdCaja;
        public String IdMoneda;
        public String IdEstado;
        public String IdConcepto;
        public String FechaLiquidacionDesde;
        public String FechaLiquidacionHasta;
        public String IdTipoPersona;
        public String IdMedioDePago;
        public String NombreBancoSocio;
        public String NombreBancoCorredora;
        public String AliasBanco = "-1";
        public String NumeroCuentaBancariaCorredora = "-1";
        public String IdClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public String FechaComprobanteOrigen;
    }

    public static class LiquidarComprobanteTodos_Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
        public List<ResultadoProceso_Salida> P_OUT_RESULTADOPROCESO = new ArrayList<>();
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final LiquidarComprobanteService liquidarComprobante;

    public LiquidarComprobanteTodosService(NamedParameterJdbcTemplate jdbc, LiquidarComprobanteService liquidarComprobante) {
        this.jdbc = jdbc;
        this.liquidarComprobante = liquidarComprobante;
    }

    /** Legacy {@code call}: the error counter is recounted from the messages. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        LiquidarComprobanteTodos_Entrada in = JsonLegacy.PLATAFORMA.fromJson(entrada, LiquidarComprobanteTodos_Entrada.class);
        LiquidarComprobanteTodos_Salida out = logica(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        return JsonLegacy.PLATAFORMA.toJsonTree(out).getAsJsonObject();
    }

    @Transactional(rollbackFor = Exception.class)
    public LiquidarComprobanteTodos_Salida logica(LiquidarComprobanteTodos_Entrada in) throws Exception {
        LiquidarComprobanteTodos_Salida out = new LiquidarComprobanteTodos_Salida();
        List<ComprobanteLiquidar_Entrada> comprobantes = jdbc.query(SQL_SELECT_COMPROBANTES_A_LIQUIDAR_GRILLA, filtros(in.p_Filtros),
                Filas.de(ComprobanteLiquidar_Entrada.class));
        if (comprobantes.isEmpty()) {
            out.P_OUT_ERRORESCONTADOR = 0L;
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "SIN_COMPROBANTE_TESORERIA_A_LIQUIDAR_O_YA_LIQUIDADOS",
                    "No Existen Comprobantes a Liquidar o Ya se encuentran Liquidados"));
            return out;
        }

        LiquidarComprobante_Entrada entrada = new LiquidarComprobante_Entrada();
        entrada.p_IdEmpresa = in.p_IdEmpresa;
        entrada.p_IdUsuario = in.p_IdUsuario;
        entrada.p_IdModulo = in.p_IdModulo;
        entrada.p_SimularOperacion = "N";
        entrada.p_UsaReglasNegocio = "S";
        entrada.p_FechaMovimiento = in.p_FechaMovimiento;
        entrada.p_ComprobanteLiquidar = comprobantes;
        entrada.p_FormaDePagoLiquidar = new ArrayList<>();
        LiquidarComprobante_Salida salida = liquidarComprobante.logica(entrada);

        // LEGACY DEFECT kept (see README TES-A-410): it rolls back when the counter is null (a process that stopped early) and also when it is positive
        if (salida.P_OUT_ERRORESCONTADOR == null || salida.P_OUT_ERRORESCONTADOR > 0) {
            LiquidarComprobanteTransaccion.rollback(jdbc);
        }
        out.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR;
        out.P_OUT_MENSAJES = salida.P_OUT_MENSAJES;
        out.P_OUT_RESULTADOPROCESO = salida.P_OUT_RESULTADOPROCESO;
        return out;
    }

    private static MapSqlParameterSource filtros(FiltrosGrilla f) {
        return new MapSqlParameterSource()
                .addValue("IdTipoMovimiento", f.IdTipoMovimiento)
                .addValue("IdPersona", f.IdPersona)
                .addValue("IdPortafolioTitularPago", f.IdPortafolioTitularPago)
                .addValue("IdCaja", f.IdCaja)
                .addValue("IdMoneda", f.IdMoneda)
                .addValue("IdEstado", f.IdEstado)
                .addValue("IdConcepto", f.IdConcepto)
                .addValue("FechaLiquidacionDesde", f.FechaLiquidacionDesde)
                .addValue("FechaLiquidacionHasta", f.FechaLiquidacionHasta)
                .addValue("IdTipoPersona", f.IdTipoPersona)
                .addValue("IdMedioDePago", f.IdMedioDePago)
                .addValue("NombreBancoSocio", f.NombreBancoSocio)
                .addValue("NombreBancoCorredora", f.NombreBancoCorredora)
                .addValue("AliasBanco", f.AliasBanco)
                .addValue("NumeroCuentaBancariaCorredora", f.NumeroCuentaBancariaCorredora)
                .addValue("IdClasificacionTipoInstrumento", f.IdClasificacionTipoInstrumento)
                .addValue("IdTipoInstrumento", f.IdTipoInstrumento)
                .addValue("FechaComprobanteOrigen", f.FechaComprobanteOrigen);
    }
}
