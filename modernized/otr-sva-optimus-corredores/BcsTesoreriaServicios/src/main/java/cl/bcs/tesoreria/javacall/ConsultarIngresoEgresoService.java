package cl.bcs.tesoreria.javacall;

import static cl.bcs.tesoreria.javacall.TesoreriaJdbcHelper.loadSql;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.comun.Consultas;
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.utils.BcsPojo;

/**
 * Legacy {@code cl.bcs.tesoreria.ConsultarIngresoEgreso}, behaviours TES-A-625..638: query of ingresos/egresos by liquidation date range and
 * optional filters, restricted to the portfolios the consulting employee may see (collaborator {@code ObtenerPortafoliosEjecutivo}). The portfolios
 * are staged under a work id in a session table that the query joins, and removed afterwards (the work id never reaches the output).
 */
@Service
public class ConsultarIngresoEgresoService {

    private static final String SQL_INGRESO_EGRESO = loadSql("sql/ConsultarIngresoEgreso_SelectIngresoEgreso.sql");
    private static final String SQL_INSERTAR_PORTAFOLIO = loadSql("sql/ConsultarIngresoEgreso_InsertIdPortafolioTablaPaso.sql");
    private static final String SQL_INSERTAR_TODOS = loadSql("sql/ConsultarIngresoEgreso_InsertFullIdPortafolioTablaPaso.sql");
    private static final String SQL_BORRAR = loadSql("sql/ConsultarIngresoEgreso_DeleteIdPortafolioTablaPaso.sql");

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

    public static class EntradaConsultarIngresoEgreso extends BcsPojo {
        public Long p_IdEmpresa = 0L;
        public Long p_IdUsuario = 0L;
        public String p_IdModulo = "";
        public Long p_IdEmpleadoConsulta = 0L;
        // filters
        public Date p_FechaLiquidacionDesde;
        public Date p_FechaLiquidacionHasta;
        public String p_IdEstado = "";
        public String p_IdTipoMovimiento = "";
        public String p_IdConcepto = "";
        public String p_IdMoneda = "";
        public String p_EsIngresoAutomatico = "";
        public Long p_IdCaja = 0L;
        public Long p_IdPortafolioTitularPago = 0L;
        public Long p_IdPortafolio = 0L;
    }

    public static class IngresoEgreso extends BcsPojo {
    public Long IdIngresoEgreso;
    public Long IdEmpresa;
    public String NombreEmpresa;
    public String IdModulo;
    public Long IdUsuario;
    public String IdTipoMovimiento;
    public String NombreTipoMovimiento;
    public String IdConcepto;
    public String NombreConcepto;
    public Long IdCaja;
    public String NombreCaja;
    public String IdMoneda;
    public String NombreMoneda;
    public BigDecimal Monto;
    public BigDecimal MontoEgreso;
    public BigDecimal MontoIngreso;
    public BigDecimal MontoConSigno;
    public Date FechaLiquidacion;
    public Timestamp FechaIngresoRegistro;
    public Date FechaMovimiento;
    public String IdTipoPersona;
    public String NombreTipoSocioDeNegocio;
    public Long IdPersona;
    public Long IdCliente;
    public Long IdSocioDeNegocio;
    public String NombrePersona;
    public Long IdPortafolio;
    public String NombrePortafolio;
    public String IdTipoDocumentoIdentidad;
    public String DocumentoIdentidad;
    public String IdTipoPersonaTitularPago;
    public Long IdPersonaTitularPago;
    public String NombrePersonaTitularPago;
    public String MueveCuentaInversion;
    public Long IdPortafolioTitularPago;
    public String IdTipoDocumentoIdentidadTitularPago;
    public String DocumentoIdentidadTitularPago;
    public String NombrePortafolioTitularPago;
    public Long IdClienteTitularPago;
    public String SecuenciaAgrupacion;
    public Long SecuenciaRelacionada;
    public String Glosa;
    public Long IdEmpleadoDigitador;
    public Long IdSucursalEmpleadoDigitador;
    public String IdTipoComprobanteOrigen;
    public Long IdComprobanteOrigen;
    public Long FolioComprobanteDTE;
    public Timestamp FechaComprobanteOrigen;
    public Timestamp FechaLiquidacionComprobanteOrigen;
    public String IdTipoProducto;
    public String NombreTipoProducto;
    public String IdTipoOperacion;
    public String NombreTipoOperacion;
    public String IdBolsa;
    public String IdMonedaOperacion;
    public BigDecimal MontoOperacion;
    public Timestamp FechaParidad;
    public String IdTipoReajuste;
    public BigDecimal ValorParidad;
    public Long FolioCartera;
    public Long IdInstrumento;
    public String Nemotecnico;
    public String IdTipoInstrumento;
    public String IdClasificacionTipoInstrumento;
    public String EsBilateral;
    public String TitularPago;
    public Long IdUsuarioEmision;
    public String NombreUsuarioEmision;
    public Timestamp FechaHoraEmision;
    public Long IdUsuarioBloqueo;
    public Timestamp FechaHoraBloqueo;
    public Long IdCarga;
    public String IdEstado;
    public String NombreEstado;
    public Long IdComprobante;
    public Date FechaOperacion;
    public String IdCondicionLiquidacion;
    public Long IdOperacion;
    public String EsIngresoAutomatico;
    public String AgrupaComprobante;
    public String NeteaComprobante;
    public String NombreVentana;
    public String IdMedioDePago;
    public String NombreMedioDePago;
    public String IdModalidadDePago;
    public String NombreModalidadDePago;
    public Long IdCuentaBancoCorredora;
    public Long IdChequera;
    public String NombreBancoCorredora;
    public String NumeroCuentaBancariaCorredora;
    public String NombreTipoCuentaBancariaCorredora;
    public String NombreBancoSocio;
    public String NumeroCuentaBancariaSocio;
    public String NombreTipoCuentaBancariaSocio;
    public String NumeroSerieSocio;
    public Long NumeroDocumentoSocio;
    public Timestamp FechaDocumentoPagoSocio;
    public Long IdBancoSocio;
    public String IdEstadoComprobante;
    public String NombreEstadoComprobante;
    public Long IdUsuarioAnulacion;
    public Timestamp FechaHoraAnulacion;
    public String IdTipoEvento;
    public String Autorizacion;
    public String SolicitudAutorizacion;
    public String MotivoAutorizacion;
    public String IdEstadoAutorizacion;
    public String NombreEstadoAutorizacion;
    public String RegimenMatrizAgrupadorTitularPago;
    public String AliasBanco;
    public String IdTipoCanalIngreso;
    public String CuentaDCVOrigen;
    public String CuentaDCVDestino;
    }

    public static class SalidaConsultarIngresoEgreso extends BcsPojo {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public List<IngresoEgreso> P_OUT_RESULTADO = new ArrayList<>();
    }

    /** Input of the {@code ObtenerPortafoliosEjecutivo} collaborator (legacy {@code ObtenerPortafoliosEjecutivo_entrada}). */
    public static class ObtenerPortafoliosEjecutivo_entrada extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public Long p_IdEmpleadoConsulta;
        public String p_filtrarListaPortafolios = "N";
        public List<Long> p_ListaIdPortafolios = new ArrayList<>();
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final Colaboradores colaboradores;

    public ConsultarIngresoEgresoService(NamedParameterJdbcTemplate jdbc, Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.colaboradores = colaboradores;
    }

    /** Legacy {@code call}. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        EntradaConsultarIngresoEgreso in = JsonLegacy.PLATAFORMA.fromJson(entrada, EntradaConsultarIngresoEgreso.class);
        return JsonLegacy.PLATAFORMA.toJsonTree(logica(in)).getAsJsonObject();
    }

    /** Legacy {@code logica}. */
    @Transactional(rollbackFor = Exception.class)
    public SalidaConsultarIngresoEgreso logica(EntradaConsultarIngresoEgreso entrada) throws Exception {
        SalidaConsultarIngresoEgreso salida = new SalidaConsultarIngresoEgreso();
        long idTrabajo = ThreadLocalRandom.current().nextLong(10_000_000_000L);
        prepararPortafolios(entrada, idTrabajo);

        Map<String, Object> p = new HashMap<>();
        p.put("IdTrabajo", idTrabajo);
        p.put("FechaDesde", entrada.p_FechaLiquidacionDesde);
        p.put("FechaHasta", entrada.p_FechaLiquidacionHasta);
        p.put("IgnorarEstado", ignorar("".equals(entrada.p_IdEstado)));
        p.put("IdEstado", entrada.p_IdEstado);
        p.put("IgnorarTipoMovimiento", ignorar("".equals(entrada.p_IdTipoMovimiento)));
        p.put("IdTipoMovimiento", entrada.p_IdTipoMovimiento);
        p.put("IgnorarIdConcepto", ignorar("".equals(entrada.p_IdConcepto)));
        p.put("IdConcepto", entrada.p_IdConcepto);
        p.put("IgnorarIdMoneda", ignorar("".equals(entrada.p_IdMoneda)));
        p.put("IdMoneda", entrada.p_IdMoneda);
        p.put("IgnorarIngresoAutomatico", ignorar("".equals(entrada.p_EsIngresoAutomatico)));
        p.put("IngresoAutomatico", entrada.p_EsIngresoAutomatico);
        p.put("IgnorarIdCaja", ignorar(entrada.p_IdCaja <= 0L));
        p.put("IdCaja", entrada.p_IdCaja);
        p.put("IgnorarIdPortafolioTitular", ignorar(entrada.p_IdPortafolioTitularPago <= 0L));
        p.put("IdPortafolioTitularPago", entrada.p_IdPortafolioTitularPago);
        p.put("IgnorarIdPortafolio", ignorar(entrada.p_IdPortafolio <= 0L));
        p.put("IdPortafolio", entrada.p_IdPortafolio);
        salida.P_OUT_RESULTADO = jdbc.query(SQL_INGRESO_EGRESO, p, Filas.de(IngresoEgreso.class));

        jdbc.update(SQL_BORRAR, Map.of("IdTrabajo", idTrabajo));
        return salida;
    }

    private static int ignorar(boolean sinFiltro) {
        return sinFiltro ? 1 : 0;
    }

    /**
     * Stages the portfolios the query may return: the one asked for; else, when the employee has a restricted portfolio, those the employee may
     * see; else every portfolio.
     */
    private void prepararPortafolios(EntradaConsultarIngresoEgreso entrada, long idTrabajo) {
        if (entrada.p_IdPortafolio != 0) {
            insertarPortafolios(List.of(entrada.p_IdPortafolio), idTrabajo);
            return;
        }
        ObtenerPortafoliosEjecutivo_entrada consulta = new ObtenerPortafoliosEjecutivo_entrada();
        consulta.p_IdEmpresa = entrada.p_IdEmpresa;
        consulta.p_IdUsuario = entrada.p_IdUsuario;
        consulta.p_IdModulo = entrada.p_IdModulo;
        consulta.p_IdEmpleadoConsulta = entrada.p_IdEmpleadoConsulta;
        JsonObject entradaJson = JsonLegacy.PLATAFORMA.toJsonTree(consulta).getAsJsonObject();
        if (valor(colaboradores.invocar("ObtenerPortafoliosEjecutivo.restringeCartera", entradaJson)).getAsBoolean()) {
            List<Long> ids = new ArrayList<>();
            valor(colaboradores.invocar("ObtenerPortafoliosEjecutivo", entradaJson)).getAsJsonArray().forEach(e -> ids.add(e.getAsLong()));
            insertarPortafolios(ids, idTrabajo);
        } else {
            jdbc.update(SQL_INSERTAR_TODOS, Map.of("IdTrabajo", idTrabajo));
        }
    }

    private void insertarPortafolios(List<Long> ids, long idTrabajo) {
        Consultas.lote(jdbc, SQL_INSERTAR_PORTAFOLIO, ids.stream().map(id -> Map.<String, Object>of("IdPortafolio", id, "IdTrabajo", idTrabajo)).toList());
    }

    /** The collaborators that are not JSON services answer {@code {"valor": ...}}. */
    private static JsonElement valor(JsonObject respuesta) {
        return respuesta.get("valor");
    }
}
