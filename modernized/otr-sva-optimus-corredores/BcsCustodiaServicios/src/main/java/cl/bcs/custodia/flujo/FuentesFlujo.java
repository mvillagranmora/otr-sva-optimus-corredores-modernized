package cl.bcs.custodia.flujo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import cl.bcs.custodia.flujo.FlujoCustodiaModelo.ConteoCartera;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.DatosFlujo;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.MatrizUso;
import cl.bcs.custodia.flujo.jdbc.SqlNombrado;
import cl.bcs.custodia.flujo.jdbc.SqlRecursos;
import cl.bcs.custodia.flujo.kit.FechaLegacy;

/**
 * The reads of the sources of the flow (CUS-FLU-15..23): one SELECT per source over the legacy SQL files verbatim, every row tagged with the item it
 * answers ({@code ItemCalculo}) and the origin of that item ({@code ItemOrigen}). Sources with a historic variant read it when the last projected
 * date is strictly before the system date (CUS-FLU-07). Read only; the only write of the whole service is the work table, done by the caller.
 */
final class FuentesFlujo {

    /** What the reads need to know about the call. */
    record Contexto(Long idEmpresa, long idTrabajo, java.sql.Date fechaMaxima, java.sql.Date fechaMinima, java.sql.Date fechaSistema,
            String fechaSaldo, String agregarSimultaneasEventoCapital, String simultaneasEvc, List<MatrizUso> ganadores, Map<String, String> origenPorItem) {

        boolean historico() {
            return FlujoCustodiaReglas.leerHistorico(fechaMaxima.toLocalDate(), fechaSistema.toLocalDate());
        }

        Map<String, Object> parametrosBase() {
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", idEmpresa);
            p.put("FechaMaxima", fechaMaxima);
            p.put("IdTrabajo", idTrabajo);
            return p;
        }
    }

    private static final String SQL_CARTERAS_PARAM = SqlRecursos.modulo("FlujoCustodia_SelectCarterasParam.sql");
    private static final String SQL_CARTERAS_SELECT_CLAUSE = SqlRecursos.modulo("FlujoCustodia_SelectCarterasSelectClause.sql");
    private static final String SQL_CARTERAS_HISTORICO = SqlRecursos.modulo("FlujoCustodia_SelectCarterasHistorico.sql");
    private static final String SQL_CARTERAS_TRANSITO = SqlRecursos.modulo("FlujoCustodia_SelectCarterasTransito.sql");
    private static final String SQL_CARTERAS_TRANSITO_HISTORICO = SqlRecursos.modulo("FlujoCustodia_SelectCarterasTransitoHistorico.sql");
    private static final String SQL_TRANSITO = SqlRecursos.modulo("FlujoCustodia_SelectTransito.sql");
    private static final String SQL_TRANSITO_HISTORICO = SqlRecursos.modulo("FlujoCustodia_SelectTransitoHistorico.sql");
    private static final String SQL_GARANTIA = SqlRecursos.modulo("FlujoCustodia_SelectGarantia.sql");
    private static final String SQL_GARANTIA_HISTORICO = SqlRecursos.modulo("FlujoCustodia_SelectGarantiaHistorico.sql");
    private static final String SQL_SUBYACENTE = SqlRecursos.modulo("FlujoCustodia_SelectSubyacente.sql");
    private static final String SQL_SUBYACENTE_HISTORICO = SqlRecursos.modulo("FlujoCustodia_SelectSubyacenteHistorico.sql");
    private static final String SQL_CARTERA_RESERVADA = SqlRecursos.modulo("FlujoCustodia_SelectCarteraReservada.sql");
    private static final String SQL_CARTERA_RESERVADA_HISTORICO = SqlRecursos.modulo("FlujoCustodia_SelectCarteraReservadaHistorico.sql");
    private static final String SQL_ORDENES_POR_AUTORIZAR = SqlRecursos.modulo("FlujoCustodia_SelectOrdenesPorAutorizar.sql");
    private static final String SQL_ORDENES_INGRESADAS = SqlRecursos.modulo("FlujoCustodia_SelectOrdenesIngresadas.sql");
    private static final String SQL_ORDENES_REPORTE_POR_AUTORIZAR = SqlRecursos.modulo("FlujoCustodia_SelectOrdenesReportePorAutorizar.sql");
    private static final String SQL_ORDENES_REPORTE_INGRESADAS = SqlRecursos.modulo("FlujoCustodia_SelectOrdenesReporteIngresadas.sql");
    private static final String SQL_ASIGNACIONES_NO_ENVIADAS = SqlRecursos.modulo("FlujoCustodia_SelectAsignacionesNoEnviadasAFacturar.sql");
    private static final String SQL_ASIGNACIONES_REPORTE_NO_ENVIADAS = SqlRecursos.modulo("FlujoCustodia_SelectAsignacionesReporteNoEnviadasAFacturar.sql");
    private static final String SQL_MOVIMIENTOS_FACTURACION = SqlRecursos.modulo("FlujoCustodia_SelectMovimientosFacturacion.sql");
    private static final String SQL_MOVIMIENTOS_REPORTE_FACTURACION = SqlRecursos.modulo("FlujoCustodia_SelectMovimientosReporteFacturacion.sql");
    private static final String SQL_EVENTOS_DE_CAPITAL = SqlRecursos.modulo("FlujoCustodia_SelectEventosDeCapital.sql");
    private static final String SQL_FONDOS_MUTUOS = SqlRecursos.modulo("FlujoCustodia_SelectFondosMutuos.sql");

    /** Operation types whose underlying is read (CUS-FLU-19); D-CUS-04: the list depends on the first winner. */
    private static final List<String> TIPOS_OPERACION = List.of("VENSIM", "PRESTA", "EMPRES", "COMPAC", "VENPAC", "COMRRV", "VENRRV", "COMRRF", "VENRRF");
    private static final List<String> TIPOS_OPERACION_EVENTO_CAPITAL = List.of("COMSIM", "VENSIM", "PRESTA", "COMPAC", "VENPAC", "COMRRV", "VENRRV", "COMRRF",
            "VENRRF");

    private static final int UMBRAL_PAGINACION = 500000;
    private static final int TAMANO_PAGINA = 100000;
    private static final String MARCA_MATRIZ = "<<BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso>>";

    private final Connection con;
    private final Contexto ctx;

    FuentesFlujo(Connection con, Contexto ctx) {
        this.con = con;
        this.ctx = ctx;
    }

    /** Reads one source (a value of {@link FlujoCustodiaReglas#fuenteDeItem}); a source nothing reads (D-CUS-07) gives no rows. */
    List<DatosFlujo> leer(String fuente) throws SQLException {
        return switch (fuente) {
            case "CARTERA" -> cartera();
            case "CARTERA_TRANSITO" -> carteraTransito();
            case "TRANSITO" -> transito();
            case "GARANTIA" -> garantia();
            case "SUBYACENTE" -> subyacente();
            case "CARTERA_RESERVADA" -> carteraReservada();
            case "ORDENES_POR_AUTORIZAR" -> porAccion(SQL_ORDENES_POR_AUTORIZAR, ctx.parametrosBase(), a -> "ORDENES_" + a + "_PENDIENTES");
            case "ORDENES_INGRESADAS" -> porAccion(SQL_ORDENES_INGRESADAS, ordenesIngresadasParams(), a -> "ORDENES_" + a + "_INGRESADAS");
            case "ORDENES_REPORTE_POR_AUTORIZAR" -> porAccion(SQL_ORDENES_REPORTE_POR_AUTORIZAR, ctx.parametrosBase(), a -> "ORDENES_REPORTE_" + a + "_PENDIENTES");
            case "ORDENES_REPORTE_INGRESADAS" -> porAccion(SQL_ORDENES_REPORTE_INGRESADAS, ctx.parametrosBase(), a -> "ORDENES_REPORTE_" + a + "_INGRESADAS");
            case "ASIGNACIONES_NO_ENVIAS_A_FACTURAR" -> porAccion(SQL_ASIGNACIONES_NO_ENVIADAS, ctx.parametrosBase(), a -> "ASIGNACIONES_" + a + "_PENDIENTE");
            case "ASIGNACIONES_REPORTE_NO_ENVIAS_A_FACTURAR" -> porAccion(SQL_ASIGNACIONES_REPORTE_NO_ENVIADAS, ctx.parametrosBase(),
                    a -> "ASIGNACIONES_REPORTE_" + a + "_PENDIENTE");
            case "MOVIMIENTOS_FACTURACION" -> porAccion(SQL_MOVIMIENTOS_FACTURACION, ctx.parametrosBase(), a -> "MOVIMIENTOS_" + a + "_FACTURACION");
            case "MOVIMIENTOS_REPORTE_FACTURACION" -> porAccion(SQL_MOVIMIENTOS_REPORTE_FACTURACION, ctx.parametrosBase(),
                    a -> "MOVIMIENTOS_REPORTE_" + a + "_FACTURACION");
            case "EVENTOS_DE_CAPITAL" -> eventosDeCapital();
            case "FONDOS_MUTUOS" -> fondosMutuos();
            default -> new ArrayList<>();
        };
    }

    // ------------------------------------------------------------------------------------------------ CUS-FLU-15 .. 20

    private List<DatosFlujo> cartera() throws SQLException {
        Map<String, Object> params = ctx.parametrosBase();
        params.put("SimultaneaEVC", ctx.simultaneasEvc());
        List<DatosFlujo> filas = ctx.historico() ? SqlNombrado.consultar(con, SQL_CARTERAS_HISTORICO, params, DatosFlujo.class)
                : carterasActuales(ctx.ganadores().get(0), params);
        for (DatosFlujo rec : filas) {
            rec.ItemCalculo = "INFORMACION_CARTERA";
            rec.ItemOrigen = ctx.origenPorItem().get("INFORMACION_CARTERA");
        }
        return filas;
    }

    private List<DatosFlujo> carteraTransito() throws SQLException {
        List<DatosFlujo> filas = SqlNombrado.consultar(con, ctx.historico() ? SQL_CARTERAS_TRANSITO_HISTORICO : SQL_CARTERAS_TRANSITO, ctx.parametrosBase(),
                DatosFlujo.class);
        for (DatosFlujo rec : filas) {
            rec.ItemCalculo = "CARTERA_TRANSITO_" + rec.Accion;
            rec.ItemOrigen = ctx.origenPorItem().get("CARTERA_TRANSITO_" + rec.Accion);
        }
        return filas;
    }

    private List<DatosFlujo> transito() throws SQLException {
        List<DatosFlujo> filas = SqlNombrado.consultar(con, ctx.historico() ? SQL_TRANSITO_HISTORICO : SQL_TRANSITO, ctx.parametrosBase(), DatosFlujo.class);
        for (DatosFlujo rec : filas) {
            if ("S".equalsIgnoreCase(rec.EsVentaDescubierta) && rec.IdAccionOperacion.equalsIgnoreCase("RET")) {
                rec.ItemCalculo = "MOV_TRANSITO_RETIRO_VENTA_DESCUBIERTA";
                rec.ItemOrigen = ctx.origenPorItem().get("MOV_TRANSITO_RETIRO_VENTA_DESCUBIERTA");
            } else {
                String accion = "ING".equals(rec.IdAccionOperacion) ? "_INGRESO" : "RET".equals(rec.IdAccionOperacion) ? "_RETIRO" : "";
                rec.ItemCalculo = "INFORMACION_TRANSITO" + accion;
                rec.ItemOrigen = ctx.origenPorItem().get("INFORMACION_TRANSITO" + accion);
            }
        }
        return filas;
    }

    private List<DatosFlujo> garantia() throws SQLException {
        List<DatosFlujo> filas = SqlNombrado.consultar(con, ctx.historico() ? SQL_GARANTIA_HISTORICO : SQL_GARANTIA, ctx.parametrosBase(), DatosFlujo.class);
        for (DatosFlujo rec : filas) {
            rec.ItemCalculo = "INFORMACION_GARANTIA";
            rec.ItemOrigen = ctx.origenPorItem().get("INFORMACION_GARANTIA");
        }
        return filas;
    }

    /**
     * CUS-FLU-19: the operation types depend on the FIRST winner (D-CUS-04). D-CUS-03: when the switch {@code CUS.AgregarSimultaneasEventoCapitalCli}
     * is not "S" the legacy {@code case} had no {@code break} and went on to read the reserved portfolio: kept.
     */
    private List<DatosFlujo> subyacente() throws SQLException {
        List<String> tipos = "FLUJO_CLIENTES_DERECHO_CAPITAL".equalsIgnoreCase(ctx.ganadores().get(0).BusquedaEspecifica) ? TIPOS_OPERACION_EVENTO_CAPITAL
                : TIPOS_OPERACION;
        Map<String, Object> params = new HashMap<>();
        params.put("idsTipoOperacion", new ArrayList<>(tipos));
        params.put("IdEmpresa", ctx.idEmpresa());
        params.put("IdTrabajo", ctx.idTrabajo());
        if (!"S".equalsIgnoreCase(ctx.agregarSimultaneasEventoCapital())) {
            return carteraReservada();
        }
        List<DatosFlujo> filas;
        if (ctx.historico()) {
            params.put("FechaMaxima", ctx.fechaMaxima());
            filas = SqlNombrado.consultar(con, SQL_SUBYACENTE_HISTORICO, params, DatosFlujo.class);
        } else {
            params.put("FechaSaldo", ctx.fechaSaldo());
            filas = SqlNombrado.consultar(con, SQL_SUBYACENTE, params, DatosFlujo.class);
        }
        String texto = "";   // legacy: not reset per row, a row of an unmapped type keeps the previous row's item
        for (DatosFlujo rec : filas) {
            switch (rec.IdTipoOperacion) {
                case "COMSIM": texto = "SIMULTANEA_COMPRA"; break;
                case "VENSIM": texto = "SIMULTANEA_VENTA"; break;
                case "COMRRV":
                case "COMRRF": texto = "REPORTE_COMPRA"; break;
                case "VENRRV":
                case "VENRRF": texto = "REPORTE_VENTA"; break;
                case "PRESTA": texto = "PRESTAMO"; break;
                case "EMPRES": texto = "EMPRESTITO"; break;
                case "COMPAC": texto = "COMPRA_PACTO"; break;
                case "VENPAC": texto = "VENTA_PACTO"; break;
                default: break;
            }
            rec.ItemCalculo = texto;
            rec.ItemOrigen = ctx.origenPorItem().get(texto);
        }
        return filas;
    }

    private List<DatosFlujo> carteraReservada() throws SQLException {
        List<DatosFlujo> filas = SqlNombrado.consultar(con, ctx.historico() ? SQL_CARTERA_RESERVADA_HISTORICO : SQL_CARTERA_RESERVADA, ctx.parametrosBase(),
                DatosFlujo.class);
        String texto = "";
        for (DatosFlujo rec : filas) {
            switch (rec.IdTipoOperacion) {
                case "COMPAC": texto = "RESERVA_COMPRA_PACTOS"; break;
                case "VENPAC": texto = "RESERVA_VENTA_PACTO"; break;
                case "COMTER": texto = "RESERVA_COMPRA_TERMINO"; break;
                case "VENTER": texto = "RESERVA_VENTA_TERMINO"; break;
                default: break;
            }
            rec.ItemCalculo = texto;
            rec.ItemOrigen = ctx.origenPorItem().get(texto);
        }
        return filas;
    }

    // ------------------------------------------------------------------------------------------------ CUS-FLU-21 .. 23

    private Map<String, Object> ordenesIngresadasParams() {
        Map<String, Object> p = new HashMap<>();
        p.put("IdEmpresa", ctx.idEmpresa());
        p.put("FechaMaxima", ctx.fechaMaxima());
        p.put("FechaMinima", ctx.fechaMinima());
        p.put("IdTrabajo", ctx.idTrabajo());
        return p;
    }

    /** Sources with no historic variant whose item is a prefix, the row's action and a suffix (CUS-FLU-21). */
    private List<DatosFlujo> porAccion(String sql, Map<String, Object> params, Function<String, String> item) throws SQLException {
        List<DatosFlujo> filas = SqlNombrado.consultar(con, sql, params, DatosFlujo.class);
        for (DatosFlujo rec : filas) {
            rec.ItemCalculo = item.apply(rec.Accion);
            rec.ItemOrigen = ctx.origenPorItem().get(item.apply(rec.Accion));
        }
        return filas;
    }

    private List<DatosFlujo> eventosDeCapital() throws SQLException {
        List<DatosFlujo> filas = SqlNombrado.consultar(con, SQL_EVENTOS_DE_CAPITAL, ctx.parametrosBase(), DatosFlujo.class);
        String texto = "";
        for (DatosFlujo rec : filas) {
            switch (rec.Accion) {
                case "SU": texto = "SUBSCRIPCIONES"; break;
                case "SL": texto = "SORTEO_LETRAS"; break;
                case "PB": texto = "PREPAGO_BONO"; break;
                default: break;
            }
            rec.ItemCalculo = texto;
            rec.ItemOrigen = texto;
        }
        return filas;
    }

    private List<DatosFlujo> fondosMutuos() throws SQLException {
        List<DatosFlujo> filas = SqlNombrado.consultar(con, SQL_FONDOS_MUTUOS, ordenesIngresadasParams(), DatosFlujo.class);
        for (DatosFlujo rec : filas) {
            String texto = "APORFM".equals(rec.IdTipoOperacion) ? "APORTE_FONDOS_MUTUOS" : "RESCFM".equals(rec.IdTipoOperacion) ? "RESCATE_FONDOS_MUTUOS" : "";
            rec.ItemCalculo = texto;
            rec.ItemOrigen = "FONDOSMUTUOS";
        }
        return filas;
    }

    // ------------------------------------------------------------------------------------------------ CUS-FLU-15 current portfolio

    /**
     * The current portfolio is read with a dynamic predicate built from the FIRST winner only (D-CUS-04) and paged by 100 000 rows when it holds more
     * than 500 000 (a count first).
     */
    private List<DatosFlujo> carterasActuales(MatrizUso primero, Map<String, Object> params) throws SQLException {
        String base = predicados(SQL_CARTERAS_PARAM, primero);
        String consulta = base.replace("<<SELECT_CLAUSE>>", SQL_CARTERAS_SELECT_CLAUSE);
        String conteo = base.replace("<<SELECT_CLAUSE>>", "COUNT(1) AS \"Count\"").replace("<<LIMIT_OFFSET_CLAUSE>>", "");
        List<ConteoCartera> cuenta = SqlNombrado.consultar(con, conteo, params, ConteoCartera.class);
        if (cuenta.get(0).Count > UMBRAL_PAGINACION) {
            List<DatosFlujo> todas = new ArrayList<>();
            for (int desde = 0; desde < cuenta.get(0).Count; desde += TAMANO_PAGINA) {
                todas.addAll(SqlNombrado.consultar(con, consulta.replace("<<LIMIT_OFFSET_CLAUSE>>", "LIMIT " + TAMANO_PAGINA + " OFFSET " + desde), params,
                        DatosFlujo.class));
            }
            return todas;
        }
        return SqlNombrado.consultar(con, consulta.replace("<<LIMIT_OFFSET_CLAUSE>>", ""), params, DatosFlujo.class);
    }

    private static String predicados(String sql, MatrizUso mu) {
        List<String> predicados = new ArrayList<>();
        if (mu.IdCliente != null) {
            predicados.add("(CAR.\"IdCliente\" = MZU.\"IdCliente\")");
        }
        if (mu.IdPortafolio != null) {
            predicados.add("(CAR.\"IdPortafolio\" = MZU.\"IdPortafolio\")");
        }
        if (mu.IdClasificacionTipoInstrumento != null) {
            predicados.add("(CAR.\"IdClasificacionTipoInstrumento\" = MZU.\"IdClasificacionTipoInstrumento\")");
        }
        if (mu.IdTipoInstrumento != null) {
            predicados.add("(CAR.\"IdTipoInstrumento\" = MZU.\"IdTipoInstrumento\")");
        }
        if (mu.IdInstrumento != null) {
            predicados.add("(CAR.\"IdInstrumento\" = MZU.\"IdInstrumento\")");
        }
        if (mu.IdEmisor != null) {
            predicados.add("( rtrim(CAR.\"IdEmisor\") = MZU.\"IdEmisor\")");
        }
        if (mu.FechaVencimiento != null && mu.FechaVencimiento.compareTo(FechaLegacy.aFechaSql("0001-01-01")) != 0) {
            if ("IF".equalsIgnoreCase(mu.IdClasificacionTipoInstrumento) || "RF".equalsIgnoreCase(mu.IdClasificacionTipoInstrumento)) {
                predicados.add("(CAR.\"FechaVencimientoInstrumento\" = MZU.\"FechaVencimiento\")");
            } else {
                predicados.add("(CAR.\"FechaVencimientoOperacion\" = MZU.\"FechaVencimiento\")");
            }
        }
        if (mu.IdTipoReajuste != null) {
            predicados.add("(CAR.\"IdTipoReajuste\" = MZU.\"IdTipoReajuste\")");
        }
        if (mu.EsFungible != null) {
            predicados.add("(CAR.\"EsFungible\" = MZU.\"EsFungible\")");
        }
        if (mu.IdBoveda != null) {
            predicados.add("(CAR.\"IdBoveda\" = MZU.\"IdBoveda\")");
        }
        if (mu.IdRegimenFondoMutuo != null) {
            predicados.add("(CAR.\"IdRegimenFondoMutuo\" = MZU.\"IdRegimenFondoMutuo\")");
        }
        return sql.replace(MARCA_MATRIZ, predicados.isEmpty() ? "(1==1)" : String.join(" AND ", predicados));
    }
}
