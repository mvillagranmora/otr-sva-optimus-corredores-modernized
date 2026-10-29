package cl.bcs.tesoreria.javacall;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.comun.Consultas;
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.javacall.comun.Parametros;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.utils.BcsPojo;

/**
 * Legacy {@code cl.bcs.tesoreria.LiquidacionMasivaComprobante}, behaviours TES-A-270..279: prepares the mass liquidation of the comprobantes of
 * facturacion identified by their folio. For each comprobante it assigns the corredora bank account (matched by name) to the formas de pago through the
 * {@code EdicionMultiple} stored procedure, validates the folio against facturacion (amounts, quantities, state) and answers which comprobantes,
 * movements to certify and formas de pago the caller has to liquidate afterwards. It does not liquidate anything itself.
 *
 * <p>The stored procedure is outside the proof: it is called through the {@code StoredProcedure} collaborator with its two input tables
 * ({@code #p_FormasDePago}, {@code #p_RegistrosAProcesar}) both created in the session and sent in the request.
 */
@Service
public class LiquidacionMasivaComprobanteService {

    private static final Logger log = LoggerFactory.getLogger(LiquidacionMasivaComprobanteService.class);

    private static final String SQL_SELECT_COMPROBANTE_MASIVO = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_ConsultarFacturacionCargaMasivaTes.sql");
    private static final String SQL_SELECT_COMPROBANTE_FOLIO = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_SelectComprobanteFolio.sql");
    private static final String SQL_SELECT_CANTIDAD_FACTURA = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_SelectCantidadMovimientoTransito.sql");
    private static final String SQL_SELECT_TIPO_OPERACION = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_SelectTipoOperacion.sql");
    private static final String SQL_SELECT_FORMA_DE_PAGO = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_SelectConsultarFormaDePago.sql");
    private static final String SQL_CONSULTAR_FORMA_PAGO = LiquidarComprobanteSql.cargar("sql/LiquidacionMasivaComprobante_ConsultarFormaDePago.sql");
    private static final String SQL_CONSULTAR_BANCO_CORREDORA = LiquidarComprobanteSql.cargar("sql/LiquidacionMasivaComprobante_ConsultarCuentaCorredora.sql");
    private static final String SQL_CREATE_FORMAS_DE_PAGO = TesoreriaJdbcHelper.loadSql("sql/LiquidacionMasivaComprobante_CreateTableFormasDePago.sql");
    private static final String SQL_INSERT_FORMAS_DE_PAGO = LiquidarComprobanteSql.cargar("sql/LiquidacionMasivaComprobante_InsertTableFormasDePago.sql");
    private static final String SQL_DROP_FORMAS_DE_PAGO = TesoreriaJdbcHelper.loadSql("sql/LiquidacionMasivaComprobante_DropTableFormasDePago.sql");
    private static final String SQL_CREATE_REGISTROS = TesoreriaJdbcHelper.loadSql("sql/LiquidacionMasivaComprobante_CreateTableRegistrosAProcesar.sql");
    private static final String SQL_INSERT_REGISTROS = LiquidarComprobanteSql.cargar("sql/LiquidacionMasivaComprobante_InsertTableRegistrosAProcesar.sql");
    private static final String SQL_DROP_REGISTROS = TesoreriaJdbcHelper.loadSql("sql/LiquidacionMasivaComprobante_DropTableRegistrosAProcesar.sql");

    private static final String PROCEDIMIENTO_EDICION_MULTIPLE = "BSCL.Tesoreria.BD.Servicios::EdicionMultiple";
    private static final String ENTIDAD_COMPROBANTE = "TES.Comprobante";

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

    public static class LiquidarComprobante_Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion = "N";
        public String p_UsaReglasNegocio;
        public String p_FechaMovimiento;
        public List<NombreCuentaBancariaEntrada> p_Banco;
        public List<ComprobanteLiquidarEntrada> p_ComprobanteLiquidar = new ArrayList<>();
        public List<FormaDePagoLiquidar_Entrada> p_FormaDePagoLiquidar = new ArrayList<>();
    }

    public static class ComprobanteLiquidarEntrada {
        public Long IdComprobante;
    }

    public static class NombreCuentaBancariaEntrada {
        public String NombreCuentaBancaria;
    }

    public static class NombreCuentaBancaria {
        public String NombreCuentaBancaria;
        public Long IdCuentaBancariaCorredora;
    }

    public static class FormaDePagoLiquidar_Entrada {
        public Long IdComprobante;
        public Long IdComprobanteFormaDePago;
    }

    public static class FormaDePagoLiquidar_Salida {
        public Long IdComprobante;
        public Long IdComprobanteFormaDePago;
    }

    public static class ComprobanteLiquidar_Salida {
        public Long IdComprobante;
        public String IdTipoMovimiento;
        public String AfectaCuentaInversion;
    }

    public static class LiquidarComprobante_Salida extends BcsPojo {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
        public List<ResultadoProceso_Salida> P_OUT_RESULTADOPROCESO = new ArrayList<>();
        public List<MovimientosCertifificar_Salida> P_OUT_MOVIMIENTOSCERTIFICAR = new ArrayList<>();
        public List<ComprobanteLiquidar_Salida> P_OUT_COMPROBANTELIQUIDAR = new ArrayList<>();
        public List<FormaDePagoLiquidar_Salida> P_OUT_FORMADEPAGO = new ArrayList<>();
    }

    public static class ResultadoProceso_Salida {
        public String Campo;
        public Long Id;
        public String Servicio;
        public String Resultado;
    }

    public static class MovimientosCertifificar_Salida {
        public Long IdMovimientoCarteraEnTransito;
        public String IdTipoOperacion;
    }

    public static class ComprobanteFolioSalida {
        public Long IdComprobante;
        public Long FolioComprobante;
        public String IdEstado;
        public Long IdComprobanteFacturacion;
        public BigDecimal MontoFactura;
        public BigDecimal MontoTesoreria;
        public String IdTipoMovimiento;
    }

    public static class CantidadFactura {
        public Long IdCfact;
        public BigDecimal CantidadFactura;
        public BigDecimal CantidadMovimiento;
    }

    public static class Datos_Usuario_Entrada {
        public Long IdComprobante;
        public String IdTipoMovimiento;
    }

    public static class FormaDePagoEntrada {
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

    public static class FormaDePagoSalida {
        public Long NumeroReferencia;
        public String IdMedioDePago;
        public String IdModalidadDePago;
        public String IdMoneda;
        public BigDecimal Monto;
        public String FechaDocumentoPagoSocio;
    }

    public static class RegistrosProcesar {
        public Long Id;
        public String Entidad;
        public String Accion;
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistema configuracion;
    private final Colaboradores colaboradores;

    public LiquidacionMasivaComprobanteService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.configuracion = configuracion;
        this.colaboradores = colaboradores;
    }

    /** Legacy {@code call}: the error counter is recounted from the messages. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        LiquidarComprobante_Entrada in = JsonLegacy.PLATAFORMA.fromJson(entrada, LiquidarComprobante_Entrada.class);
        LiquidarComprobante_Salida out = logica(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        return JsonLegacy.PLATAFORMA.toJsonTree(out).getAsJsonObject();
    }

    @Transactional(rollbackFor = Exception.class)
    public LiquidarComprobante_Salida logica(LiquidarComprobante_Entrada in) throws Exception {
        LiquidarComprobante_Salida out = new LiquidarComprobante_Salida();

        List<Long> idsFolio = in.p_ComprobanteLiquidar.stream().map(c -> c.IdComprobante).collect(Collectors.toList());
        List<ComprobanteFolioSalida> comprobantes = jdbc.query(SQL_SELECT_COMPROBANTE_FOLIO, Map.of("folio", Consultas.enLista(idsFolio)),
                Filas.de(ComprobanteFolioSalida.class));
        if (comprobantes.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(null, MensajeSalida.MENSAJE_ERROR, "Factura", "Factura sin Folio Asociado "));
        }

        asignarCuentaDeLaCorredora(in, out, comprobantes);
        validarContraFacturacion(out, comprobantes);

        List<Long> idsFacturacion = comprobantes.stream().map(c -> c.IdComprobanteFacturacion).collect(Collectors.toList());
        out.P_OUT_MOVIMIENTOSCERTIFICAR = jdbc.query(SQL_SELECT_TIPO_OPERACION, Map.of("FolioCoprobante", Consultas.enLista(idsFacturacion)),
                Filas.de(MovimientosCertifificar_Salida.class));

        List<Long> idsComprobanteMasivo = new ArrayList<>();
        List<ComprobanteLiquidar_Salida> aLiquidar = new ArrayList<>();
        for (ComprobanteFolioSalida c : comprobantes) {
            for (Datos_Usuario_Entrada d : jdbc.query(SQL_SELECT_COMPROBANTE_MASIVO, Map.of("IdComprobante", c.IdComprobante),
                    Filas.de(Datos_Usuario_Entrada.class))) {
                ComprobanteLiquidar_Salida comprobante = new ComprobanteLiquidar_Salida();
                comprobante.IdComprobante = d.IdComprobante;
                comprobante.IdTipoMovimiento = d.IdTipoMovimiento;
                comprobante.AfectaCuentaInversion = "";
                aLiquidar.add(comprobante);
                idsComprobanteMasivo.add(d.IdComprobante);
            }
            out.P_OUT_COMPROBANTELIQUIDAR = aLiquidar;
        }

        // LEGACY DEFECT kept (see README TES-A-270): the ids of the COMPROBANTES are compared with the ids of the FORMAS DE PAGO, so this list is always empty
        out.P_OUT_FORMADEPAGO = jdbc.query(SQL_SELECT_FORMA_DE_PAGO, Map.of("IdComprobante", Consultas.enLista(idsComprobanteMasivo)),
                Filas.de(FormaDePagoLiquidar_Salida.class));

        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        return out;
    }

    /**
     * Assigns the corredora bank account to the forma de pago of each comprobante through the EdicionMultiple procedure.
     *
     * <p>The legacy decides it with four lists that are accumulated and cleared at different nesting levels, and compared as whole lists. The behaviour
     * (which is what the goldens pin) is reproduced with the same lists and the same clearing points:
     * <ul>
     * <li>{@code bancosEntrada}: names of the requested banks seen since the last clear (it is cleared only after a bank matched and the formas de pago
     *     were scanned, so it keeps growing across banks and comprobantes until then);</li>
     * <li>{@code bancosSalida}: the name of the account being looked at (cleared after each account);</li>
     * <li>a bank "matches" when both lists are equal, i.e. it is the first pending requested bank and has the name of the account;</li>
     * <li>{@code comprobanteEntrada}: the comprobante being processed, emptied once its forma de pago was modified (one call per comprobante).</li>
     * </ul>
     */
    private void asignarCuentaDeLaCorredora(LiquidarComprobante_Entrada in, LiquidarComprobante_Salida out, List<ComprobanteFolioSalida> comprobantes)
            throws Exception {
        List<String> nombresBanco = in.p_Banco.stream().map(b -> b.NombreCuentaBancaria).collect(Collectors.toList());
        List<Long> idsTesoreria = comprobantes.stream().map(c -> c.IdComprobante).collect(Collectors.toList());
        String fechaSistema = Parametros.texto(configuracion, in.p_IdEmpresa, Parametros.PLATAFORMA, "PLA.FechaSistema");

        List<FormaDePagoSalida> formasDePago = jdbc.query(SQL_CONSULTAR_FORMA_PAGO, Map.of("IdComprobante", Consultas.enLista(idsTesoreria)),
                Filas.de(FormaDePagoSalida.class));
        List<NombreCuentaBancaria> cuentas = jdbc.query(SQL_CONSULTAR_BANCO_CORREDORA, Map.of("banco", Consultas.enLista(nombresBanco)),
                Filas.de(NombreCuentaBancaria.class));

        List<Long> comprobanteEntrada = new ArrayList<>();
        List<Long> referenciaEntrada = new ArrayList<>();
        List<String> bancosEntrada = new ArrayList<>();
        List<String> bancosSalida = new ArrayList<>();
        List<FormaDePagoEntrada> formasAEditar = new ArrayList<>();
        List<RegistrosProcesar> registros = new ArrayList<>();
        for (ComprobanteFolioSalida comprobante : comprobantes) {
            comprobanteEntrada.add(comprobante.IdComprobante);
            for (String bancoCorredora : nombresBanco) {
                bancosEntrada.add(bancoCorredora);
                for (NombreCuentaBancaria cuenta : cuentas) {
                    bancosSalida.add(cuenta.NombreCuentaBancaria);
                    if (bancosEntrada.equals(bancosSalida)) {
                        for (FormaDePagoSalida forma : formasDePago) {
                            referenciaEntrada.add(forma.NumeroReferencia);
                            if (comprobanteEntrada.equals(referenciaEntrada)) {
                                formasAEditar.add(formaDePagoConCuenta(forma, cuenta, fechaSistema));
                                registros.add(registro(formasAEditar.get(0).NumeroReferencia, "AGN"));
                                registros.add(registro(formasAEditar.get(0).NumeroReferencia, "NTN"));
                                JsonObject respuesta = modificarFormasDePago(in, fechaSistema, formasAEditar, registros);
                                JsonElement informacion = respuesta.get("p_out_InformacionContador");
                                if (informacion != null && !informacion.isJsonNull()) {
                                    out.P_OUT_MENSAJES.add(new MensajeSalida(null, MensajeSalida.MENSAJE_OK, "Comprobate" + comprobanteEntrada, "Forma de pago Modificada "));
                                }
                                comprobanteEntrada.clear();
                            }
                            referenciaEntrada.clear();
                            bancosEntrada.clear();
                            formasAEditar.clear();
                            registros.clear();
                        }
                    }
                    bancosSalida.clear();
                }
            }
            comprobanteEntrada.clear();
        }
    }

    private static FormaDePagoEntrada formaDePagoConCuenta(FormaDePagoSalida forma, NombreCuentaBancaria cuenta, String fechaSistema) {
        FormaDePagoEntrada f = new FormaDePagoEntrada();
        f.NumeroReferencia = forma.NumeroReferencia;
        f.IdMedioDePago = forma.IdMedioDePago;
        f.IdModalidadDePago = forma.IdModalidadDePago;
        f.IdMoneda = forma.IdMoneda;
        f.Monto = forma.Monto;
        f.IdCuentaBancariaCorredora = cuenta.IdCuentaBancariaCorredora;
        f.IdTarjetaBancariaCorredora = 0L;
        f.IdChequera = 0L;
        f.NumeroDocumentoCorredora = 0L;
        f.FechaDocumentoPagoCorredora = fechaSistema;
        f.FechaDocumentoPagoSocio = fechaSistema;
        return f;
    }

    private static RegistrosProcesar registro(Long id, String accion) {
        RegistrosProcesar r = new RegistrosProcesar();
        r.Id = id;
        r.Entidad = ENTIDAD_COMPROBANTE;
        r.Accion = accion;
        return r;
    }

    /** Fills the two session tables, calls EdicionMultiple (no liquidation: {@code p_LiquidaComprobante = N}) and drops the tables. */
    private JsonObject modificarFormasDePago(LiquidarComprobante_Entrada in, String fechaSistema, List<FormaDePagoEntrada> formas,
            List<RegistrosProcesar> registros) throws Exception {
        jdbc.getJdbcOperations().execute(SQL_CREATE_FORMAS_DE_PAGO);
        LiquidarComprobanteSql.lote(jdbc, SQL_INSERT_FORMAS_DE_PAGO, formas);
        jdbc.getJdbcOperations().execute(SQL_CREATE_REGISTROS);
        LiquidarComprobanteSql.lote(jdbc, SQL_INSERT_REGISTROS, registros);

        Map<String, Object> escalares = new LinkedHashMap<>();
        escalares.put("p_IdEmpresa", in.p_IdEmpresa);
        escalares.put("p_IdUsuario", in.p_IdUsuario);
        escalares.put("p_IdModulo", in.p_IdModulo);
        escalares.put("p_UsaReglasNegocio", in.p_UsaReglasNegocio);
        escalares.put("p_FechaMovimiento", fechaSistema);
        escalares.put("p_LiquidaComprobante", "N");
        escalares.put("p_Entidad", ENTIDAD_COMPROBANTE);
        JsonObject parametros = JsonLegacy.PLATAFORMA.toJsonTree(escalares).getAsJsonObject();
        parametros.add("p_FormasDePago", JsonLegacy.PLATAFORMA.toJsonTree(formas));
        parametros.add("p_RegistrosAProcesar", JsonLegacy.PLATAFORMA.toJsonTree(registros));
        JsonObject peticion = new JsonObject();
        peticion.addProperty("procedimiento", PROCEDIMIENTO_EDICION_MULTIPLE);
        peticion.add("entrada", parametros);
        JsonObject respuesta = colaboradores.invocar("StoredProcedure", peticion);

        jdbc.getJdbcOperations().execute(SQL_DROP_REGISTROS);
        jdbc.getJdbcOperations().execute(SQL_DROP_FORMAS_DE_PAGO);
        return respuesta == null ? new JsonObject() : respuesta;
    }

    /** First check: the tesoreria amounts of a folio add up to the invoice; second: invoice and movement quantities agree; third: not already liquidated. */
    private void validarContraFacturacion(LiquidarComprobante_Salida out, List<ComprobanteFolioSalida> comprobantes) {
        List<Long> idsFacturacion = comprobantes.stream().map(c -> c.IdComprobanteFacturacion).collect(Collectors.toList());
        List<CantidadFactura> cantidades = jdbc.query(SQL_SELECT_CANTIDAD_FACTURA, Map.of("idComprobanteFacturacion", Consultas.enLista(idsFacturacion)),
                Filas.de(CantidadFactura.class));
        for (ComprobanteFolioSalida c : comprobantes) {
            log.debug("{} - {} - {}", c.FolioComprobante, c.MontoFactura, c.MontoTesoreria);
            BigDecimal suma = BigDecimal.ZERO;
            for (ComprobanteFolioSalida otro : comprobantes) {
                if (c.FolioComprobante.equals(otro.FolioComprobante)) {
                    suma = suma.add(otro.MontoTesoreria);
                }
            }
            if (suma.compareTo(c.MontoFactura) != 0) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(c.FolioComprobante, MensajeSalida.MENSAJE_ERROR, " Montos",
                        "Monto Factura " + c.FolioComprobante + " Montos no son iguales " + c.MontoFactura + " - " + suma));
            }
            for (CantidadFactura cantidad : cantidades) {
                if (c.IdComprobanteFacturacion.equals(cantidad.IdCfact) && cantidad.CantidadFactura.compareTo(cantidad.CantidadMovimiento) != 0) {
                    out.P_OUT_MENSAJES.add(new MensajeSalida(c.FolioComprobante, MensajeSalida.MENSAJE_ERROR, "Cantidad",
                            "Cantidad Factura : " + c.FolioComprobante + " - " + cantidad.CantidadFactura + " - " + cantidad.CantidadMovimiento
                                    + " La cantidad de facturacion no son iguales"));
                }
            }
            if (c.IdEstado.equalsIgnoreCase("LIQ")) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(c.FolioComprobante, MensajeSalida.MENSAJE_ERROR, "Factura",
                        "Factura: " + c.FolioComprobante + " Con Problemas de Liquidacion " + comprobantes.size()));
            }
        }
    }
}
