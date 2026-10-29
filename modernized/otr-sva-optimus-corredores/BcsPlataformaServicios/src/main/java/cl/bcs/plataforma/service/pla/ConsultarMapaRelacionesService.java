package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsultarMapaRelacionesService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarMapaRelacionesService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private static final String WHERE_COMPROBANTE     = "\nWHERE CMBTE.\"IdComprobante\" IN (:ids)";
    private static final String WHERE_MOV_COMPROBANTE = "\nWHERE MOV.\"IdMovimientoComprobante\" IN (:ids)";
    private static final String WHERE_TRANSACCION     = "\nWHERE TRAN.\"IdTransaccion\" IN (:ids);";
    private static final String WHERE_ORDEN           = "\nWHERE ORD.\"IdOrden\" IN (:ids);";
    private static final String WHERE_ASIGNACION      = "\nWHERE ASIG.\"IdAsignacion\" IN (:ids);";
    private static final String WHERE_SPOT            = "\nWHERE B.\"IdMovimientoComprobante\" IN (:ids);";
    private static final String WHERE_FFMM            = "\nWHERE MvtoCmbte.\"IdMovimientoComprobante\" IN (:ids);";

    private final NamedParameterJdbcTemplate jdbc;

    private final String SQL_ORDENES;
    private final String SQL_ASIGNACIONES;
    private final String SQL_TRANSACCIONES;
    private final String SQL_MOVFACTURAR;
    private final String SQL_CMBTE_FACTURACION;
    private final String SQL_OPERACION_SPOT;
    private final String SQL_OPERACION_FFMM;
    private final String SQL_TRAN_X_ASIG;
    private final String SQL_ASIG_X_ORDEN;
    private final String SQL_ASIG_X_MOVFACT;
    private final String SQL_MOV_X_CMBTE;
    private final String SQL_SPOT_X_MOVFACT;
    private final String SQL_SPOT_X_OPEFFMM;

    public ConsultarMapaRelacionesService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        SQL_ORDENES           = loadSql("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectOrdenes.sql");
        SQL_ASIGNACIONES      = loadSql("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectAsignaciones.sql");
        SQL_TRANSACCIONES     = loadSql("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectTransacciones.sql");
        SQL_MOVFACTURAR       = loadSql("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectMovfacturar.sql");
        SQL_CMBTE_FACTURACION = loadSql("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectCmbteFacturacion.sql");
        SQL_OPERACION_SPOT    = loadSql("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectOperacionSpot.sql");
        SQL_OPERACION_FFMM    = loadSql("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectOperacionFFMM.sql");
        SQL_TRAN_X_ASIG       = loadSql("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectTranXAsig.sql");
        SQL_ASIG_X_ORDEN      = loadSql("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectAsigXOrden.sql");
        SQL_ASIG_X_MOVFACT    = loadSql("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectAsigXMovFact.sql");
        SQL_MOV_X_CMBTE       = loadSql("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectMovXCmbte.sql");
        SQL_SPOT_X_MOVFACT    = loadSql("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectSpotXMovFact.sql");
        SQL_SPOT_X_OPEFFMM    = loadSql("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectSpotXOpeFFMM.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ConsultarMapaRelaciones"; }

    @Override
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ConsultarMapaRelaciones");
        Salida out = new Salida();
        try {
            Long idEntidad    = params.has("p_IdEntidad")    ? params.get("p_IdEntidad").getAsLong()       : 0L;
            String tipoEntidad = params.has("p_TipoEntidad") ? params.get("p_TipoEntidad").getAsString()   : "";

            List<OrdenProceso>         ordenes           = new ArrayList<>();
            List<AsignacionProceso>    asignaciones      = new ArrayList<>();
            List<TransaccionProceso>   transacciones     = new ArrayList<>();
            List<MovFacturarProceso>   movsfacturar      = new ArrayList<>();
            List<CmbteFacturarProceso> cmbtesFacturacion = new ArrayList<>();
            List<OperacionSpotProceso> operacionesSpot   = new ArrayList<>();
            List<OperacionFFMMProceso> operacionesFFMM   = new ArrayList<>();

            List<ConectorOrdenAsignacion>       cnOrdenAsig      = new ArrayList<>();
            List<ConectorTransaccionAsignacion> cnTranAsig       = new ArrayList<>();
            List<ConectorAsignacionMovFacturar> cnAsigMovFact    = new ArrayList<>();
            List<ConectorMovFacturarCmbte>      cnMovCmbte       = new ArrayList<>();
            List<ConectorOpSpotMovComprobante>  cnSpotMovComp    = new ArrayList<>();
            List<ConectorOpSpotFFMM>            cnSpotFFMM       = new ArrayList<>();

            if (tipoEntidad.equalsIgnoreCase("TRANSACCION")) {
                transacciones  = cargar(SQL_TRANSACCIONES, WHERE_TRANSACCION, List.of(idEntidad), TransaccionProceso.class);
                cnTranAsig     = cargar(SQL_TRAN_X_ASIG,   WHERE_TRANSACCION, ids(transacciones, "IdTransaccion"), ConectorTransaccionAsignacion.class);
                asignaciones   = cargar(SQL_ASIGNACIONES,  WHERE_ASIGNACION,  ids(cnTranAsig, "IdAsignacion"), AsignacionProceso.class);
                cnOrdenAsig    = cargar(SQL_ASIG_X_ORDEN,  WHERE_ASIGNACION,  ids(asignaciones, "IdAsignacion"), ConectorOrdenAsignacion.class);
                ordenes        = cargar(SQL_ORDENES,        WHERE_ORDEN,       ids(cnOrdenAsig, "IdOrden"), OrdenProceso.class);
                cnAsigMovFact  = cargar(SQL_ASIG_X_MOVFACT, WHERE_ASIGNACION, ids(asignaciones, "IdAsignacion"), ConectorAsignacionMovFacturar.class);
                movsfacturar   = cargar(SQL_MOVFACTURAR,    WHERE_MOV_COMPROBANTE, ids(cnAsigMovFact, "IdMovimientoComprobante"), MovFacturarProceso.class);
                cnMovCmbte     = cargar(SQL_MOV_X_CMBTE,   WHERE_MOV_COMPROBANTE, ids(movsfacturar, "IdMovimientoComprobante"), ConectorMovFacturarCmbte.class);
                cmbtesFacturacion = cargar(SQL_CMBTE_FACTURACION, WHERE_COMPROBANTE, ids(cnMovCmbte, "IdComprobante"), CmbteFacturarProceso.class);
            }
            else if (tipoEntidad.equalsIgnoreCase("ORDEN")) {
                ordenes        = cargar(SQL_ORDENES,        WHERE_ORDEN,       List.of(idEntidad), OrdenProceso.class);
                cnOrdenAsig    = cargar(SQL_ASIG_X_ORDEN,  WHERE_ORDEN,       ids(ordenes, "IdOrden"), ConectorOrdenAsignacion.class);
                asignaciones   = cargar(SQL_ASIGNACIONES,  WHERE_ASIGNACION,  ids(cnOrdenAsig, "IdAsignacion"), AsignacionProceso.class);
                cnTranAsig     = cargar(SQL_TRAN_X_ASIG,   WHERE_ASIGNACION,  ids(asignaciones, "IdAsignacion"), ConectorTransaccionAsignacion.class);
                transacciones  = cargar(SQL_TRANSACCIONES, WHERE_TRANSACCION, ids(cnTranAsig, "IdTransaccion"), TransaccionProceso.class);
                cnAsigMovFact  = cargar(SQL_ASIG_X_MOVFACT, WHERE_ASIGNACION, ids(asignaciones, "IdAsignacion"), ConectorAsignacionMovFacturar.class);
                movsfacturar   = cargar(SQL_MOVFACTURAR,   WHERE_MOV_COMPROBANTE, ids(cnAsigMovFact, "IdMovimientoComprobante"), MovFacturarProceso.class);
                cnMovCmbte     = cargar(SQL_MOV_X_CMBTE,   WHERE_MOV_COMPROBANTE, ids(movsfacturar, "IdMovimientoComprobante"), ConectorMovFacturarCmbte.class);
                cmbtesFacturacion = cargar(SQL_CMBTE_FACTURACION, WHERE_COMPROBANTE, ids(cnMovCmbte, "IdComprobante"), CmbteFacturarProceso.class);
            }
            else if (tipoEntidad.equalsIgnoreCase("CMBTEFACTURACION")) {
                cmbtesFacturacion = cargar(SQL_CMBTE_FACTURACION, WHERE_COMPROBANTE, List.of(idEntidad), CmbteFacturarProceso.class);
                cnMovCmbte     = cargar(SQL_MOV_X_CMBTE,   WHERE_COMPROBANTE,     ids(cmbtesFacturacion, "IdComprobante"), ConectorMovFacturarCmbte.class);
                movsfacturar   = cargar(SQL_MOVFACTURAR,   WHERE_MOV_COMPROBANTE, ids(cnMovCmbte, "IdMovimientoComprobante"), MovFacturarProceso.class);
                cnAsigMovFact  = cargar(SQL_ASIG_X_MOVFACT, WHERE_MOV_COMPROBANTE, ids(movsfacturar, "IdMovimientoComprobante"), ConectorAsignacionMovFacturar.class);
                asignaciones   = cargar(SQL_ASIGNACIONES,  WHERE_ASIGNACION,  ids(cnAsigMovFact, "IdAsignacion"), AsignacionProceso.class);
                cnTranAsig     = cargar(SQL_TRAN_X_ASIG,   WHERE_ASIGNACION,  ids(asignaciones, "IdAsignacion"), ConectorTransaccionAsignacion.class);
                transacciones  = cargar(SQL_TRANSACCIONES, WHERE_TRANSACCION, ids(cnTranAsig, "IdTransaccion"), TransaccionProceso.class);
                cnOrdenAsig    = cargar(SQL_ASIG_X_ORDEN,  WHERE_ASIGNACION,  ids(asignaciones, "IdAsignacion"), ConectorOrdenAsignacion.class);
                ordenes        = cargar(SQL_ORDENES,        WHERE_ORDEN,       ids(cnOrdenAsig, "IdOrden"), OrdenProceso.class);
                cnSpotMovComp  = cargar(SQL_SPOT_X_MOVFACT, WHERE_SPOT,       ids(movsfacturar, "IdMovimientoComprobante"), ConectorOpSpotMovComprobante.class);
                operacionesSpot = cargar(SQL_OPERACION_SPOT, WHERE_SPOT,      ids(cnSpotMovComp, "IdOperacionSpot"), OperacionSpotProceso.class);
            }
            else if (tipoEntidad.equalsIgnoreCase("MOVFACTURAR")) {
                movsfacturar   = cargar(SQL_MOVFACTURAR,   WHERE_MOV_COMPROBANTE, List.of(idEntidad), MovFacturarProceso.class);
                cnMovCmbte     = cargar(SQL_MOV_X_CMBTE,  WHERE_MOV_COMPROBANTE, ids(movsfacturar, "IdMovimientoComprobante"), ConectorMovFacturarCmbte.class);
                cmbtesFacturacion = cargar(SQL_CMBTE_FACTURACION, WHERE_COMPROBANTE, ids(cnMovCmbte, "IdComprobante"), CmbteFacturarProceso.class);
                cnAsigMovFact  = cargar(SQL_ASIG_X_MOVFACT, WHERE_MOV_COMPROBANTE, ids(movsfacturar, "IdMovimientoComprobante"), ConectorAsignacionMovFacturar.class);
                asignaciones   = cargar(SQL_ASIGNACIONES,  WHERE_ASIGNACION,  ids(cnAsigMovFact, "IdAsignacion"), AsignacionProceso.class);
                cnTranAsig     = cargar(SQL_TRAN_X_ASIG,   WHERE_ASIGNACION,  ids(asignaciones, "IdAsignacion"), ConectorTransaccionAsignacion.class);
                transacciones  = cargar(SQL_TRANSACCIONES, WHERE_TRANSACCION, ids(cnTranAsig, "IdTransaccion"), TransaccionProceso.class);
                cnOrdenAsig    = cargar(SQL_ASIG_X_ORDEN,  WHERE_ASIGNACION,  ids(asignaciones, "IdAsignacion"), ConectorOrdenAsignacion.class);
                ordenes        = cargar(SQL_ORDENES,        WHERE_ORDEN,       ids(cnOrdenAsig, "IdOrden"), OrdenProceso.class);
                cnSpotMovComp  = cargar(SQL_SPOT_X_MOVFACT, WHERE_SPOT,       ids(movsfacturar, "IdMovimientoComprobante"), ConectorOpSpotMovComprobante.class);
                operacionesSpot = cargar(SQL_OPERACION_SPOT, WHERE_SPOT,      ids(cnSpotMovComp, "IdOperacionSpot"), OperacionSpotProceso.class);
                cnSpotFFMM     = cargar(SQL_SPOT_X_OPEFFMM, WHERE_FFMM,      ids(movsfacturar, "IdMovimientoComprobante"), ConectorOpSpotFFMM.class);
                operacionesFFMM = cargar(SQL_OPERACION_FFMM, WHERE_FFMM,     ids(cnSpotFFMM, "IdSolicitudAporteRescate"), OperacionFFMMProceso.class);
            }
            else if (tipoEntidad.equalsIgnoreCase("ASIGNACION")) {
                asignaciones   = cargar(SQL_ASIGNACIONES,  WHERE_ASIGNACION,  List.of(idEntidad), AsignacionProceso.class);
                cnOrdenAsig    = cargar(SQL_ASIG_X_ORDEN,  WHERE_ASIGNACION,  ids(asignaciones, "IdAsignacion"), ConectorOrdenAsignacion.class);
                ordenes        = cargar(SQL_ORDENES,        WHERE_ORDEN,       ids(cnOrdenAsig, "IdOrden"), OrdenProceso.class);
                cnTranAsig     = cargar(SQL_TRAN_X_ASIG,   WHERE_ASIGNACION,  ids(asignaciones, "IdAsignacion"), ConectorTransaccionAsignacion.class);
                transacciones  = cargar(SQL_TRANSACCIONES, WHERE_TRANSACCION, ids(cnTranAsig, "IdTransaccion"), TransaccionProceso.class);
                cnAsigMovFact  = cargar(SQL_ASIG_X_MOVFACT, WHERE_ASIGNACION, ids(asignaciones, "IdAsignacion"), ConectorAsignacionMovFacturar.class);
                movsfacturar   = cargar(SQL_MOVFACTURAR,   WHERE_MOV_COMPROBANTE, ids(cnAsigMovFact, "IdMovimientoComprobante"), MovFacturarProceso.class);
                cnMovCmbte     = cargar(SQL_MOV_X_CMBTE,  WHERE_MOV_COMPROBANTE, ids(movsfacturar, "IdMovimientoComprobante"), ConectorMovFacturarCmbte.class);
                cmbtesFacturacion = cargar(SQL_CMBTE_FACTURACION, WHERE_COMPROBANTE, ids(cnMovCmbte, "IdComprobante"), CmbteFacturarProceso.class);
            }

            // Build nodes
            for (OperacionFFMMProceso ffmm : operacionesFFMM) {
                out.p_out_entidades.add(nodo("FFMM"+ffmm.IdSolicitudAporteRescate, "consultaOperacionFFMM",
                    "IdSolicitudAporteRescate", ffmm.IdSolicitudAporteRescate, "FFMM "+ffmm.IdSolicitudAporteRescate,
                    ffmm.TipoSolicitud, str(ffmm.FechaCertificacion), amt(ffmm.Monto)+" "+ffmm.IdMoneda, amt(ffmm.MontoPago)+" "+ffmm.IdMonedaSistema));
            }
            for (OperacionSpotProceso spt : operacionesSpot) {
                out.p_out_entidades.add(nodo("SPOT"+spt.IdOperacionSpot, "consutaOperacionSpot",
                    "IdOperacionSpot", spt.IdOperacionSpot, "Spot "+spt.IdOperacionSpot,
                    spt.IdTipoOperacion, str(spt.FechaOperacion), amt(spt.MontoMonedaPrincipal)+" "+spt.IdMonedaPrincipal, amt(spt.MontoMonedaSecundaria)+" "+spt.IdMonedaSecundaria));
            }
            for (OrdenProceso ord : ordenes) {
                out.p_out_entidades.add(nodo("ORDEN"+ord.IdOrden, "DetalleOrden",
                    "IdOrden", ord.IdOrden, "Orden "+ord.IdOrden,
                    ord.DocumentoIdentidad, ord.Nemotecnico, amt(ord.Cantidad)+" - "+amt(ord.PrecioOrden)+" "+ord.IdMonedaOrden, amt(ord.MontoOrden)+" "+ord.IdMonedaOrden));
            }
            for (AsignacionProceso asig : asignaciones) {
                out.p_out_entidades.add(nodo("ASIGNACION"+asig.IdAsignacion, "DetalleAsignacion",
                    "IdAsignacion", asig.IdAsignacion, "Asignacion "+asig.IdAsignacion,
                    amt(asig.CantidadAsignada), amt(asig.MontoAsignado)+" "+asig.IdMonedaAsignacion, "", ""));
            }
            for (TransaccionProceso tran : transacciones) {
                out.p_out_entidades.add(nodo("TRANSACCION"+tran.IdTransaccion, "DetalleTransaccion",
                    "IdTransaccion", tran.IdTransaccion, "Transaccion "+tran.IdTransaccion,
                    tran.FolioTransaccion+" "+str(tran.FechaTransaccion)+" "+tran.IdBolsa,
                    tran.Nemotecnico, amt(tran.Cantidad)+" - "+amt(tran.Precio)+" "+tran.IdMoneda, amt(tran.Monto)+" "+tran.IdMoneda));
            }
            for (MovFacturarProceso mov : movsfacturar) {
                out.p_out_entidades.add(nodo("MOVFACT"+mov.IdMovimientoComprobante, "DetalleMovimientoComprobante",
                    "IdMovimientoComprobante", mov.IdMovimientoComprobante, "Mov. Facturar "+mov.IdMovimientoComprobante,
                    amt(mov.Monto)+" "+mov.IdMoneda, mov.NombreTipoComprobante, mov.NombreEstadoFacturacion, ""));
            }
            for (CmbteFacturarProceso cmbte : cmbtesFacturacion) {
                out.p_out_entidades.add(nodo("CMBTEFACT"+cmbte.IdComprobante, "DetalleComprobanteFacturacion",
                    "IdComprobante", cmbte.IdComprobante, "Cmbte. Fact. "+cmbte.IdComprobante,
                    cmbte.DocumentoIdentidad,
                    (cmbte.MontoLiquidacionFinal != null ? cmbte.MontoLiquidacionFinal.abs().toPlainString() : "0")+" "+cmbte.IdMonedaLiquidacion,
                    cmbte.NombreEstadoComprobante, ""));
            }

            // Build connectors
            for (ConectorOpSpotMovComprobante cn : cnSpotMovComp) {
                out.p_out_conectores.add(conector("SPOT"+cn.IdOperacionSpot, "MOVFACT"+cn.IdMovimientoComprobante));
            }
            for (ConectorOrdenAsignacion cn : cnOrdenAsig) {
                out.p_out_conectores.add(conector("ORDEN"+cn.IdOrden, "ASIGNACION"+cn.IdAsignacion));
            }
            for (ConectorTransaccionAsignacion cn : cnTranAsig) {
                out.p_out_conectores.add(conector("ASIGNACION"+cn.IdAsignacion, "TRANSACCION"+cn.IdTransaccion));
            }
            for (ConectorAsignacionMovFacturar cn : cnAsigMovFact) {
                out.p_out_conectores.add(conector("ASIGNACION"+cn.IdAsignacion, "MOVFACT"+cn.IdMovimientoComprobante));
            }
            for (ConectorMovFacturarCmbte cn : cnMovCmbte) {
                out.p_out_conectores.add(conector("MOVFACT"+cn.IdMovimientoComprobante, "CMBTEFACT"+cn.IdComprobante));
            }

            out.P_OUT_ERRORESCONTADOR = 0L;
        } catch (Exception e) {
            log.error("Error en ConsultarMapaRelaciones: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }
        return toJson(out);
    }

    private <T> List<T> cargar(String baseSql, String whereSql, List<Long> ids, Class<T> cls) {
        if (ids.isEmpty()) return new ArrayList<>();
        return jdbc.query(baseSql + whereSql,
            new MapSqlParameterSource("ids", ids),
            BeanPropertyRowMapper.newInstance(cls));
    }

    /** Extract a named Long field from each element using BeanPropertyRowMapper-compatible reflection */
    private <T> List<Long> ids(List<T> list, String field) {
        List<Long> result = new ArrayList<>();
        for (T item : list) {
            try {
                Object val = item.getClass().getField(field).get(item);
                if (val instanceof Number) result.add(((Number) val).longValue());
            } catch (Exception e) { /* skip null */ }
        }
        return result;
    }

    private NodoSalida nodo(String idNodo, String tipoEnlace, String nomParam, Long idEnlace,
                             String titulo, String l1, String l2, String l3, String l4) {
        NodoSalida n = new NodoSalida();
        n.IdentificadorNodo = idNodo; n.TipoEnlace = tipoEnlace;
        n.NombreParametroEnlace = nomParam; n.IdentificadorEnlace = idEnlace;
        n.Titulo = titulo; n.Linea1 = nvl(l1); n.Linea2 = nvl(l2); n.Linea3 = nvl(l3); n.Linea4 = nvl(l4);
        return n;
    }

    private ConectorSalida conector(String desde, String hacia) {
        ConectorSalida c = new ConectorSalida();
        c.IdentificadorDesde = desde; c.IdentificadorHacia = hacia; c.TextoConector = "";
        return c;
    }

    private String amt(BigDecimal v) { return v != null ? v.toPlainString() : "0"; }
    private String str(java.sql.Date d) { return d != null ? d.toString() : ""; }
    private String nvl(String s) { return s != null ? s : ""; }

    private JsonObject toJson(Object o) { return JsonParser.parseString(GSON.toJson(o)).getAsJsonObject(); }

    private String loadSql(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("SQL not found: " + path, e); }
    }

    // ---- Data classes ----

    public static class OrdenProceso {
        public Long IdOrden; public String IdEstadoOrden;
        public BigDecimal Cantidad; public BigDecimal PrecioOrden; public BigDecimal MontoOrden;
        public String Nemotecnico; public String DocumentoIdentidad;
        public String IdMonedaOrden; public String IdTipoInstrumento; public String IdClasificacionTipoInstrumento;
    }

    public static class AsignacionProceso {
        public Long IdAsignacion; public String IdEstadoAsignacion;
        public BigDecimal CantidadAsignada; public BigDecimal MontoAsignado; public String IdMonedaAsignacion;
    }

    public static class TransaccionProceso {
        public Long IdTransaccion; public String FolioTransaccion;
        public Date FechaTransaccion; public String IdEstado;
        public BigDecimal Cantidad; public BigDecimal Monto;
        public String Nemotecnico; public String IdBolsa; public String IdMoneda;
        public BigDecimal Precio; public String IdTipoInstrumento; public String IdClasificacionTipoInstrumento;
    }

    public static class MovFacturarProceso {
        public Long IdMovimientoComprobante; public BigDecimal Monto;
        public String IdEstadoFacturacion; public String IdMoneda;
        public String IdTipoComprobante; public String NombreTipoComprobante; public String NombreEstadoFacturacion;
    }

    public static class CmbteFacturarProceso {
        public Long IdComprobante; public String IdTipoComprobante; public Long FolioComprobante;
        public String IdEstadoComprobante; public String NombreEstadoComprobante; public String NombreTipoComprobante;
        public BigDecimal MontoLiquidacionFinal; public String IdMonedaLiquidacion; public String DocumentoIdentidad;
    }

    public static class OperacionSpotProceso {
        public Long IdOperacionSpot; public String IdTipoOperacion;
        public String IdMonedaPrincipal; public String IdMonedaSecundaria;
        public BigDecimal MontoMonedaPrincipal; public BigDecimal MontoMonedaSecundaria;
        public Date FechaOperacion;
    }

    public static class OperacionFFMMProceso {
        public Long IdSolicitudAporteRescate; public String TipoSolicitud;
        public String IdMoneda; public String IdMonedaSistema;
        public BigDecimal Monto; public BigDecimal MontoPago;
        public Date FechaOriginalInversion; public Date FechaCertificacion; public Date FechaLiquidacion;
    }

    public static class ConectorOrdenAsignacion       { public Long IdOrden; public Long IdAsignacion; }
    public static class ConectorTransaccionAsignacion { public Long IdTransaccion; public Long IdAsignacion; }
    public static class ConectorAsignacionMovFacturar { public Long IdMovimientoComprobante; public Long IdAsignacion; }
    public static class ConectorMovFacturarCmbte      { public Long IdMovimientoComprobante; public Long IdComprobante; }
    public static class ConectorOpSpotMovComprobante  { public Long IdOperacionSpot; public Long IdMovimientoComprobante; }
    public static class ConectorOpSpotFFMM            { public Long IdSolicitudAporteRescate; public Long IdOperacionSpot; }

    public static class NodoSalida {
        public String Titulo; public String Color;
        public String Linea1; public String Linea2; public String Linea3; public String Linea4;
        public String TipoEnlace; public String NombreParametroEnlace;
        public Long IdentificadorEnlace; public String IdentificadorNodo;
    }

    public static class ConectorSalida { public String IdentificadorDesde; public String IdentificadorHacia; public String TextoConector; }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<NodoSalida> p_out_entidades = new ArrayList<>();
        public List<ConectorSalida> p_out_conectores = new ArrayList<>();
    }
}
