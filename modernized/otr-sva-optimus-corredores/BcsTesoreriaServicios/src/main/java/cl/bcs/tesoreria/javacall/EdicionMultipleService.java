package cl.bcs.tesoreria.javacall;

import static cl.bcs.tesoreria.javacall.TesoreriaJdbcHelper.loadSql;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.ComprobanteLiquidar_Entrada;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.FormaDePagoLiquidar_Entrada;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.LiquidarComprobante_Entrada;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.LiquidarComprobante_Salida;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.utils.BcsPojo;

/**
 * Legacy {@code cl.bcs.tesoreria.EdicionMultiple}, behaviours TES-A-800..809: edits several comprobantes at once (forms of payment and actions per
 * record) through the HANA procedure {@code EdicionMultiple} and, when asked and the procedure reported nothing but information, liquidates the
 * edited comprobantes with {@link LiquidarComprobanteService}.
 *
 * <p>The procedure is outside the equivalence proof: it is called through the {@code StoredProcedure} collaborator (name and scalar inputs, the
 * forms of payment and the records travel in session temporary tables that this service fills and drops), and its messages are read from the
 * global tables it leaves. The legacy API is kept: {@link #logica} is the typed entry point used by {@code LiquidarMovimientoAutomatico}.
 *
 * <p>LEGACY DEFECTS kept: warnings or authorizations silently skip the liquidation without any message (see README TES-A-803 / 804); the
 * procedure always receives {@code p_LiquidaComprobante = 'N'} (the flag only decides whether this service liquidates afterwards).
 */
@Service
public class EdicionMultipleService {

    static final String PROCEDIMIENTO = "BSCL.Tesoreria.BD.Servicios::EdicionMultiple";

    private static final String SQL_CREAR_TABLA_FORMA_DE_PAGO = loadSql("sql/EdicionMultiple_CreateTableFormaPago.sql");
    private static final String SQL_CREAR_TABLA_REGISTRO = loadSql("sql/EdicionMultiple_CreateTableRegistro.sql");
    private static final String SQL_INSERTAR_FORMA_DE_PAGO = loadSql("sql/EdicionMultiple_InsertTableFormaPago.sql");
    private static final String SQL_INSERTAR_REGISTRO = loadSql("sql/EdicionMultiple_InsertTableRegistro.sql");
    private static final String SQL_MENSAJES_DE_SALIDA = loadSql("sql/EdicionMultiple_SelectMensajeSalidaEdicionMultiple.sql");
    private static final String SQL_ELIMINAR_TABLA_FORMA_DE_PAGO = loadSql("sql/EdicionMultiple_DropTableFormaPago.sql");
    private static final String SQL_ELIMINAR_TABLA_REGISTRO = loadSql("sql/EdicionMultiple_DropTableRegistro.sql");

    private static final Type FORMAS_DE_PAGO_COMPROBANTE = new TypeToken<List<FormaDePagoComprobante>>() { }.getType();

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

        public static class EntradaEdicionMultiple extends BcsPojo {
            public Long p_IdEmpresa;
            public Long p_IdUsuario;
            public String p_IdModulo;
            public String p_UsaReglasNegocio;
            public String p_FechaMovimiento;
            public String p_Entidad;
            public String p_LiquidaComprobante;
            public List<FormaDePago> p_FormasDePago;
            public List<RegistroAProcesar> p_RegistrosAProcesar;
		
            public EntradaEdicionMultiple() {
                this.p_FormasDePago = new ArrayList<FormaDePago>();
                this.p_RegistrosAProcesar = new ArrayList<RegistroAProcesar>();
            }
        }

        public static class FormaDePago extends BcsPojo {
            public Long NumeroReferencia;
            public String IdMedioDePago;
            public String IdModalidadDePago;
            public String IdMoneda;
            public BigDecimal Monto;
            public Long IdCuentaBancariaCorredora;
            public Long IdTarjetaBancariaCorredora;
            public Long IdChequera;
            public Long NumeroDocumentoCorredora;
            public String FechaDocumentoPagoCorredora;
            public Long IdCuentaBancariaSocio;
            public Long IdBancoSocio;
            public String NombreCuentaBancariaSocio;
            public String NumeroCuentaBancariaSocio;
            public String IdTipoCuentaBancariaSocio;
            public String NombreTipoCuentaBancariaSocio;
            public Long IdTarjetaBancariaSocio;
            public String IdTipoTarjetaBancariaSocio;
            public String NombreTipoTarjetaBancariaSocio;
            public String NumeroTarjetaBancariaSocio;
            public String NumeroSerieSocio;
            public Long NumeroDocumentoSocio;
            public String FechaDocumentoPagoSocio;
        }

        public static class RegistroAProcesar extends BcsPojo {
            public Long Id;
            public String Entidad;
            public String Accion;
        }

        public static class SalidaEdicionMultiple extends BcsPojo {
            public Long P_OUT_ERRORESCONTADOR;
            public List<MensajeSalida> P_OUT_MENSAJES;
            public List<FormaDePagoComprobante> P_OUT_FORMADEPAGOCOMPROBANTE;
		
            public SalidaEdicionMultiple() {
                this.P_OUT_ERRORESCONTADOR = 0L;
                this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
            }
        }

        public static class FormaDePagoComprobante extends BcsPojo {
            public Long IdComprobante;
            public Long IdComprobanteFormaDePago;
            public String IdTipoMovimiento;
        }

    private final NamedParameterJdbcTemplate jdbc;
    private final LiquidarComprobanteService liquidarComprobante;
    private final Colaboradores colaboradores;

    public EdicionMultipleService(NamedParameterJdbcTemplate jdbc, LiquidarComprobanteService liquidarComprobante, Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.liquidarComprobante = liquidarComprobante;
        this.colaboradores = colaboradores;
    }

    /** Legacy {@code call}. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        EntradaEdicionMultiple in = JsonLegacy.PLATAFORMA.fromJson(entrada, EntradaEdicionMultiple.class);
        return JsonLegacy.PLATAFORMA.toJsonTree(logica(in)).getAsJsonObject();
    }

    /** Legacy {@code logica}: edits, then liquidates the edited comprobantes when {@code p_LiquidaComprobante = S} and nothing else was reported. */
    @Transactional(rollbackFor = Exception.class)
    public SalidaEdicionMultiple logica(EntradaEdicionMultiple entrada) throws Exception {
        SalidaEdicionMultiple out = new SalidaEdicionMultiple();

        SalidaEdicionMultiple edicion = editarMultiple(entrada);
        out.P_OUT_ERRORESCONTADOR += edicion.P_OUT_ERRORESCONTADOR;
        out.P_OUT_MENSAJES.addAll(edicion.P_OUT_MENSAJES);
        out.P_OUT_FORMADEPAGOCOMPROBANTE = edicion.P_OUT_FORMADEPAGOCOMPROBANTE;

        long erroresYAdvertencias = edicion.P_OUT_MENSAJES.stream().filter(m -> MensajeSalida.MENSAJE_ERROR.equals(m.tipo())
                || MensajeSalida.MENSAJE_ADVERTENCIA.equals(m.tipo()) || MensajeSalida.MENSAJE_AUTORIZACION.equals(m.tipo())).count();
        if ("S".equals(entrada.p_LiquidaComprobante) && erroresYAdvertencias == 0L) {
            LiquidarComprobante_Salida liquidacion = liquidar(entrada, edicion.P_OUT_FORMADEPAGOCOMPROBANTE);
            out.P_OUT_ERRORESCONTADOR += liquidacion.P_OUT_ERRORESCONTADOR != null ? liquidacion.P_OUT_ERRORESCONTADOR
                    : MensajeSalida.numeroErrores(liquidacion.P_OUT_MENSAJES);
            out.P_OUT_MENSAJES.addAll(liquidacion.P_OUT_MENSAJES);
        }
        return out;
    }

    private SalidaEdicionMultiple editarMultiple(EntradaEdicionMultiple entrada) throws Exception {
        SalidaEdicionMultiple out = new SalidaEdicionMultiple();
        JsonObject parametros = new JsonObject();
        parametros.addProperty("p_IdEmpresa", entrada.p_IdEmpresa);
        parametros.addProperty("p_IdUsuario", entrada.p_IdUsuario);
        parametros.addProperty("p_IdModulo", entrada.p_IdModulo);
        parametros.addProperty("p_UsaReglasNegocio", entrada.p_UsaReglasNegocio);
        parametros.addProperty("p_FechaMovimiento", entrada.p_FechaMovimiento);
        parametros.addProperty("p_Entidad", entrada.p_Entidad);
        parametros.addProperty("p_LiquidaComprobante", "N"); // LEGACY DEFECT kept (see README TES-A-803 / 804): the procedure never liquidates

        JsonObject llamada = new JsonObject();
        llamada.addProperty("procedimiento", PROCEDIMIENTO);
        llamada.add("entrada", parametros);
        try {
            jdbc.getJdbcOperations().execute(SQL_CREAR_TABLA_FORMA_DE_PAGO);
            jdbc.getJdbcOperations().execute(SQL_CREAR_TABLA_REGISTRO);
            GirarComprobanteLote.ejecutar(jdbc, SQL_INSERTAR_FORMA_DE_PAGO, entrada.p_FormasDePago);
            GirarComprobanteLote.ejecutar(jdbc, SQL_INSERTAR_REGISTRO, entrada.p_RegistrosAProcesar);
            JsonObject respuesta = colaboradores.invocar("StoredProcedure", llamada);

            // a missing counter unboxes to NPE, as the legacy
            JsonElement errores = respuesta.get("p_out_ErroresContador");
            Long contador = errores == null || errores.isJsonNull() ? null : errores.getAsLong();
            out.P_OUT_ERRORESCONTADOR += contador;
            JsonElement formas = respuesta.get("p_out_FormaDePagoComprobante");
            out.P_OUT_FORMADEPAGOCOMPROBANTE = formas == null || formas.isJsonNull() ? null : JsonLegacy.PLATAFORMA.fromJson(formas, FORMAS_DE_PAGO_COMPROBANTE);
            out.P_OUT_MENSAJES = jdbc.query(SQL_MENSAJES_DE_SALIDA, Map.of(), (rs, fila) -> new MensajeSalida(rs.getLong("id"), rs.getString("tipo"), rs.getString("codigo"), rs.getString("descripcion")));
        } finally {
            jdbc.getJdbcOperations().execute(SQL_ELIMINAR_TABLA_FORMA_DE_PAGO);
            jdbc.getJdbcOperations().execute(SQL_ELIMINAR_TABLA_REGISTRO);
        }
        return out;
    }

    private LiquidarComprobante_Salida liquidar(EntradaEdicionMultiple entrada, List<FormaDePagoComprobante> formasDePago) throws Exception {
        LiquidarComprobante_Entrada e = new LiquidarComprobante_Entrada();
        e.p_IdEmpresa = entrada.p_IdEmpresa;
        e.p_IdUsuario = entrada.p_IdUsuario;
        e.p_IdModulo = entrada.p_IdModulo;
        e.p_SimularOperacion = "N";
        e.p_UsaReglasNegocio = "S";
        e.p_FechaMovimiento = entrada.p_FechaMovimiento;
        e.p_ComprobanteLiquidar = formasDePago.stream().map(f -> {
            ComprobanteLiquidar_Entrada c = new ComprobanteLiquidar_Entrada();
            c.IdComprobante = f.IdComprobante;
            c.IdTipoMovimiento = f.IdTipoMovimiento;
            c.AfectaCuentaInversion = "CLI";
            return c;
        }).collect(Collectors.toList());
        e.p_FormaDePagoLiquidar = formasDePago.stream().map(f -> {
            FormaDePagoLiquidar_Entrada fp = new FormaDePagoLiquidar_Entrada();
            fp.IdComprobante = f.IdComprobante;
            fp.IdComprobanteFormaDePago = f.IdComprobanteFormaDePago;
            return fp;
        }).collect(Collectors.toList());
        return liquidarComprobante.logica(e);
    }
}
