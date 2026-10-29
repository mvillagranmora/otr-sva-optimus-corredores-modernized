package cl.bcs.tesoreria.javacall;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.Comprobante;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.ComprobanteDetalle;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.ComprobanteFormaDePago;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.ConceptoTesoreria;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.DatoPortafolio;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.FlujoCajaElem;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.LiquidarComprobante_Entrada;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.LiquidarComprobante_Salida;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.ParametrosConfiguracion;
import cl.bcs.tesoreria.javacall.comun.Colaborador;
import cl.bcs.tesoreria.javacall.comun.Consultas;
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.javacall.comun.Parametros;
import cl.bcs.tesoreria.javacall.contrato.ActualizarSaldosCuentaBanco;
import cl.bcs.tesoreria.javacall.contrato.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.tesoreria.javacall.contrato.SaldoFlujoCaja;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.utils.Fecha;

/**
 * Legacy {@code cl.bcs.tesoreria.PreLiquidarComprobante}, behaviours TES-A-290..298: runs the liquidation of the comprobantes (formas de pago,
 * comprobantes, bank balances) and checks the cash flow that the movements to the cuenta de inversion would need, to tell the user what a liquidation
 * would do; it then rolls everything back. Input and output are those of {@link LiquidarComprobanteService} (the legacy declared copies of the classes).
 *
 * <p>Early returns (an error from the bank balances or from the cash flow) leave the work in place instead of rolling it back (TES-A-291, 293).
 */
@Service
public class PreLiquidarComprobanteService {

    private static final String SQL_INSERT_COMPROBANTES_A_LIQUIDAR = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_ComprobantesALiquidar.sql");
    private static final String SQL_INSERT_COMPROBANTE = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_InsertComprobante.sql");
    private static final String SQL_INSERT_COMPROBANTE_DETALLE = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_InsertComprobanteDetalle.sql");
    private static final String SQL_INSERT_COMPROBANTE_FORMA_DE_PAGO = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_InsertComprobanteFormaDepago.sql");
    private static final String SQL_LIQUIDAR_FORMAS_DE_PAGO = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_LiquidarFormasDepago.sql");
    private static final String SQL_LIQUIDAR_COMPROBANTE = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_LiquidarComprobante.sql");
    private static final String SQL_INSERT_COMPROBANTES_MUEVEN_CTA_INVERSION = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_InsertComprobantesMuevenCuentaInversion.sql");
    private static final String SQL_SELECT_CARGO_ABONO_A_GENERAR = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_SelectCargoAbonoAGenerar.sql");
    private static final String SQL_SELECT_CUENTA_BANCO_A_ACTUALIZAR = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_SelectCuentaBancoAActualizar.sql");
    private static final String SQL_DELETE_COMPROBANTE = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_DeleteComprobante.sql");
    private static final String SQL_DELETE_COMPROBANTE_DETALLE = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_DeleteComprobanteDetalle.sql");
    private static final String SQL_DELETE_COMPROBANTE_FORMA_DE_PAGO = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_DeleteComprobanteFormaDePago.sql");
    private static final String SQL_DELETE_COMPROBANTES_A_LIQUIDAR = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_DeleteComprobantesALiquidar.sql");
    private static final String SQL_DELETE_FORMASDEPAGO_A_LIQUIDAR = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_DeleteFormasDepagoALiquidar.sql");
    private static final String SQL_SELECT_DATOS_PORTAFOLIO = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_SelectDatosPortafolio.sql");
    private static final String SQL_SELECT_CONCEPTO_TESORERIA = LiquidarComprobanteSql.cargar("sql/LiquidarComprobante_SelectConceptoTesoreria.sql");

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;

    public PreLiquidarComprobanteService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
    }

    /** Legacy {@code call}: recounts the errors and, for a simulation, rolls back (the result of a pre-liquidation is rolled back anyway). */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        LiquidarComprobante_Entrada in = JsonLegacy.PLATAFORMA.fromJson(entrada, LiquidarComprobante_Entrada.class);
        LiquidarComprobante_Salida out = logica(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        if (in.p_SimularOperacion.equalsIgnoreCase("S")) {
            LiquidarComprobanteTransaccion.rollback(jdbc);
        }
        return JsonLegacy.PLATAFORMA.toJsonTree(out).getAsJsonObject();
    }

    @Transactional(rollbackFor = Exception.class)
    public LiquidarComprobante_Salida logica(LiquidarComprobante_Entrada in) throws Exception {
        LiquidarComprobante_Salida out = new LiquidarComprobante_Salida();
        List<Comprobante> comprobantesAProcesar = in.p_ComprobanteLiquidar.stream().map(c -> {
            Comprobante comprobante = new Comprobante();
            comprobante.IdComprobante = c.IdComprobante;
            return comprobante;
        }).collect(Collectors.toList());
        String liquidacionParcialEnviaACTA = Parametros.texto(configuracion, in.p_IdEmpresa, Parametros.TESORERIA, "LIQUIDACIONPARCIALENVIAACTA");

        boolean terminado;
        try {
            terminado = simular(in, out, liquidacionParcialEnviaACTA);
        } finally {
            limpiarTablasDeTrabajo(comprobantesAProcesar);
        }
        if (!terminado) {
            return out;
        }
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        LiquidarComprobanteTransaccion.rollback(jdbc);
        return out;
    }

    /** @return true when the simulation reached its end, false when it stopped on an error (the work is then NOT rolled back). */
    private boolean simular(LiquidarComprobante_Entrada in, LiquidarComprobante_Salida out, String liquidacionParcialEnviaACTA) throws Exception {
        LiquidarComprobanteSql.lote(jdbc, SQL_INSERT_COMPROBANTES_A_LIQUIDAR, in.p_ComprobanteLiquidar);
        Comprobante fecha = new Comprobante();
        fecha.FechaMovimiento = in.p_FechaMovimiento;
        LiquidarComprobanteSql.lote(jdbc, SQL_INSERT_COMPROBANTE, List.of(fecha));
        ComprobanteDetalle soloIdInterno = new ComprobanteDetalle();
        soloIdInterno.IdInterno = 1L; // only so that a parameter exists, otherwise the batch does not run
        LiquidarComprobanteSql.lote(jdbc, SQL_INSERT_COMPROBANTE_DETALLE, List.of(soloIdInterno));
        ComprobanteFormaDePago fechaFormaDePago = new ComprobanteFormaDePago();
        fechaFormaDePago.FechaMovimiento = in.p_FechaMovimiento;
        LiquidarComprobanteSql.lote(jdbc, SQL_INSERT_COMPROBANTE_FORMA_DE_PAGO, List.of(fechaFormaDePago));

        ComprobanteFormaDePago liquidarFormaDePago = new ComprobanteFormaDePago();
        liquidarFormaDePago.FechaMovimiento = in.p_FechaMovimiento;
        liquidarFormaDePago.IdUsuario = in.p_IdUsuario;
        LiquidarComprobanteSql.lote(jdbc, SQL_LIQUIDAR_FORMAS_DE_PAGO, List.of(liquidarFormaDePago));
        Comprobante liquidarComprobante = new Comprobante();
        liquidarComprobante.FechaMovimiento = in.p_FechaMovimiento;
        LiquidarComprobanteSql.lote(jdbc, SQL_LIQUIDAR_COMPROBANTE, List.of(liquidarComprobante));

        if (!actualizarSaldosBancarios(in, out)) {
            return false;
        }

        ParametrosConfiguracion parametros = new ParametrosConfiguracion();
        parametros.LiquidacionParcialEnviaACTA = liquidacionParcialEnviaACTA;
        LiquidarComprobanteSql.lote(jdbc, SQL_INSERT_COMPROBANTES_MUEVEN_CTA_INVERSION, List.of(parametros));
        List<ObjCargoAbonoAGenerar> cargosAbonos = jdbc.query(SQL_SELECT_CARGO_ABONO_A_GENERAR,
                Map.of("IdEmpresa", in.p_IdEmpresa, "FechaMovimiento", in.p_FechaMovimiento), Filas.de(ObjCargoAbonoAGenerar.class));

        // LEGACY DEFECT kept (see README TES-A-291): the indicator is inverted, the cash flow is only checked when some concept is flagged "no validar saldo"
        if (algunConceptoNoValidaSaldo(cargosAbonos) && !cargosAbonos.isEmpty()) {
            return validarFlujoDeCaja(in, out, cargosAbonos);
        }
        return true;
    }

    private boolean actualizarSaldosBancarios(LiquidarComprobante_Entrada in, LiquidarComprobante_Salida out) {
        List<ActualizarSaldosCuentaBanco.CuentaBanco_Entrada> cuentas = jdbc.query(SQL_SELECT_CUENTA_BANCO_A_ACTUALIZAR, Map.of(),
                Filas.de(ActualizarSaldosCuentaBanco.CuentaBanco_Entrada.class));
        if (cuentas.isEmpty()) {
            return true;
        }
        ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Entrada entrada = new ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Entrada();
        entrada.p_FechaMovimiento = in.p_FechaMovimiento;
        entrada.p_CuentaBanco = cuentas;
        ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Salida salida = Colaborador.invocar(colaboradores, "ActualizarSaldosCuentaBanco", entrada,
                ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Salida.class);
        out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        return out.P_OUT_ERRORESCONTADOR.compareTo(0L) <= 0;
    }

    /** True when at least one of the concepts to send is flagged "no validar saldo de caja" (the legacy {@code IndicadorNoValidaSaldoCaja}). */
    private boolean algunConceptoNoValidaSaldo(List<ObjCargoAbonoAGenerar> cargosAbonos) {
        List<String> conceptos = cargosAbonos.stream().map(c -> c.IdConcepto).collect(Collectors.toList());
        return !jdbc.query(SQL_SELECT_CONCEPTO_TESORERIA, Map.of("ConceptoIds", Consultas.enLista(conceptos)), Filas.de(ConceptoTesoreria.class)).stream()
                .filter(c -> c.IndicadorNoValidaSaldoCaja.equals("S")).collect(Collectors.toMap(c -> c.IdConcepto, c -> c.IndicadorNoValidaSaldoCaja)).isEmpty();
    }

    /** Compares the egresses (MOVCAR) to be sent with the cash flow of their portafolio and date. @return false when a shortfall was reported. */
    private boolean validarFlujoDeCaja(LiquidarComprobante_Entrada in, LiquidarComprobante_Salida out, List<ObjCargoAbonoAGenerar> cargosAbonos) {
        java.sql.Date fechaMinima = Fecha.toDate(cargosAbonos.get(0).FechaCondicionDeLiquidacion);
        java.sql.Date fechaMaxima = fechaMinima;
        // LEGACY DEFECT kept (see README TES-A-291): the portafolios were collected in a HashSet whose elements use identity equality, so none is deduplicated
        List<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR> portafolios = new ArrayList<>();
        Map<FlujoCajaElem, BigDecimal> egresos = new HashMap<>();
        for (ObjCargoAbonoAGenerar cargo : cargosAbonos) {
            if ("MOVCAR".equals(cargo.IdTipoMovimiento)) {
                java.sql.Date fechaCargo = Fecha.toDate(cargo.FechaCondicionDeLiquidacion);
                if (fechaCargo.before(fechaMinima)) {
                    fechaMinima = fechaCargo;
                }
                if (fechaCargo.after(fechaMaxima)) {
                    fechaMaxima = fechaCargo;
                }
                egresos.merge(new FlujoCajaElem(cargo.IdMoneda, cargo.FechaCondicionDeLiquidacion, cargo.IdPortafolio), cargo.Monto, BigDecimal::add);
                SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR p = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
                p.IdMoneda = cargo.IdMoneda;
                p.IdPortafolio = cargo.IdPortafolio;
                portafolios.add(p);
            }
        }
        if (!portafolios.isEmpty()) {
            SaldoFlujoCaja.ServiceEntrada entradaFlujo = new SaldoFlujoCaja.ServiceEntrada();
            entradaFlujo.P_IDEMPRESA = in.p_IdEmpresa;
            entradaFlujo.P_FECHADESDE = Fecha.toStringDate(fechaMinima);
            entradaFlujo.P_FECHAHASTA = Fecha.toStringDate(fechaMaxima);
            entradaFlujo.P_USO = "LIQUIDACION_TESORERIA";
            entradaFlujo.P_PORTAFOLIOACONSULTAR = portafolios;
            SaldoFlujoCaja.ServiceSalida salidaFlujo = Colaborador.invocar(colaboradores, "SaldoFlujoCaja", entradaFlujo, SaldoFlujoCaja.ServiceSalida.class);
            Map<FlujoCajaElem, BigDecimal> flujo = new HashMap<>();
            for (SaldoFlujoCaja.ObjFLUJOCAJA f : salidaFlujo.P_OUT_FLUJOCAJA) {
                flujo.merge(new FlujoCajaElem(f.IdMoneda, f.FechaSaldo, f.IdPortafolio), f.Saldo, BigDecimal::add);
            }

            List<Long> idsPortafolio = cargosAbonos.stream().map(c -> c.IdPortafolio).collect(Collectors.toList());
            Map<Long, DatoPortafolio> datosPortafolio = jdbc.query(SQL_SELECT_DATOS_PORTAFOLIO, Map.of("IdPortafolio", idsPortafolio),
                    Filas.de(DatoPortafolio.class)).stream().collect(Collectors.toMap(p -> p.IdPortafolio, p -> p));
            for (ObjCargoAbonoAGenerar cargo : cargosAbonos) {
                if ("MOVCAR".equals(cargo.IdTipoMovimiento)) {
                    FlujoCajaElem llave = new FlujoCajaElem(cargo.IdMoneda, cargo.FechaCondicionDeLiquidacion, cargo.IdPortafolio);
                    if (egresos.get(llave).compareTo(flujo.get(llave)) == 1) {
                        saldoInsuficiente(in.p_IdEmpresa, cargo, datosPortafolio.get(cargo.IdPortafolio), out.P_OUT_MENSAJES);
                    }
                }
            }
            out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
            if (out.P_OUT_ERRORESCONTADOR > 0) {
                return false;
            }
        }
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        return out.P_OUT_ERRORESCONTADOR.compareTo(0L) <= 0;
    }

    private void saldoInsuficiente(Long empresa, ObjCargoAbonoAGenerar cargo, DatoPortafolio datos, List<MensajeSalida> mensajes) {
        // a null value is left out of the message values, as the legacy dictionary builder did
        Map<String, String> valores = new HashMap<>();
        agregar(valores, "IdComprobante", cargo.NumeroCargoAbono);
        agregar(valores, "Monto", cargo.Monto);
        agregar(valores, "IdMoneda", cargo.IdMoneda);
        agregar(valores, "DocumentoIdentidad", datos.DocumentoIdentidad);
        agregar(valores, "NombreCompleto", "IdPortafolio " + datos.IdPortafolio + " " + datos.NombreCompleto);
        mensajes.add(new MensajeSalida(cargo.NumeroCargoAbono, MensajeSalida.MENSAJE_ERROR, "SALDO_CAJA_INSUFICIENTE",
                diccionario.mensaje(empresa, "SALDO_CAJA_INSUFICIENTE", valores)));
    }

    private static void agregar(Map<String, String> valores, String clave, Object valor) {
        if (valor != null) {
            valores.put(clave, valor.toString());
        }
    }

    private void limpiarTablasDeTrabajo(List<Comprobante> comprobantesAProcesar) throws SQLException {
        LiquidarComprobanteSql.lote(jdbc, SQL_DELETE_COMPROBANTE, comprobantesAProcesar);
        LiquidarComprobanteSql.lote(jdbc, SQL_DELETE_COMPROBANTE_DETALLE, comprobantesAProcesar);
        LiquidarComprobanteSql.lote(jdbc, SQL_DELETE_COMPROBANTE_FORMA_DE_PAGO, comprobantesAProcesar);
        LiquidarComprobanteSql.lote(jdbc, SQL_DELETE_COMPROBANTES_A_LIQUIDAR, comprobantesAProcesar);
        LiquidarComprobanteSql.lote(jdbc, SQL_DELETE_FORMASDEPAGO_A_LIQUIDAR, comprobantesAProcesar);
    }
}
