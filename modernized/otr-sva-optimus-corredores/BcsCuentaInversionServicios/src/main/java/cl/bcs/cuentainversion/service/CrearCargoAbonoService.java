package cl.bcs.cuentainversion.service;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.cuentainversion.domain.colaborador.AgregarLog;
import cl.bcs.cuentainversion.domain.colaborador.AgregarPrograma;
import cl.bcs.cuentainversion.domain.colaborador.SaldoFlujoCaja;
import cl.bcs.cuentainversion.port.out.Colaboradores;
import cl.bcs.cuentainversion.port.out.ConfiguracionSistemaPort;
import cl.bcs.cuentainversion.port.out.DiccionarioMensajes;
import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Utils;

/**
 * Creates the cargos / abonos (movements) of the investment accounts (README CTA-A-48..76 Java path, CTA-A-241..246 stored-procedure path).
 *
 * <p>Two paths, chosen by the system parameter {@code PLA.UsaCargoAbonoJava}:
 * <ul>
 *   <li>{@code N}: the HANA procedure {@code CrearCargoAbono} does the work; the movements to generate are written to its input table and its
 *       output tables are read back as messages;</li>
 *   <li>otherwise (Java path): the movements are resolved against accounts, currencies, concepts and types in one query, each one is validated,
 *       manual cargos / portfolio changes are checked against the cash flow, abonos and then cargos are inserted (unless simulating) and the
 *       balances are refreshed: by a scheduled job for the broker's own accounts, in line for third parties.</li>
 * </ul>
 */
@Service
public class CrearCargoAbonoService {

    private static final Logger log = LoggerFactory.getLogger(CrearCargoAbonoService.class);

    private static final String SQL = "cl/bcs/cuentainversion/sql/CrearCargoAbono_";
    private static final String SQL_INSERTAR_A_GENERAR = CtaJavaCallInfra.loadSql(SQL + "INS_CargoAbonoAGenerar.sql");
    private static final String SQL_LLAMAR_PROCEDIMIENTO = CtaJavaCallInfra.loadSql(SQL + "CallProcedimiento.sql");
    private static final String SQL_SALIDA_GENERADOS = CtaJavaCallInfra.loadSql(SQL + "SelectCargoAbonoGenerado.sql");
    private static final String SQL_SALIDA_INFORMACION = CtaJavaCallInfra.loadSql(SQL + "SelectInformacionDetalle.sql");
    private static final String SQL_SALIDA_ADVERTENCIAS = CtaJavaCallInfra.loadSql(SQL + "SelectAdvertenciasDetalle.sql");
    private static final String SQL_SALIDA_ERRORES = CtaJavaCallInfra.loadSql(SQL + "SelectErroresDetalle.sql");
    private static final String SQL_DATOS_PROCESAMIENTO = CtaJavaCallInfra.loadSql(SQL + "SelectDatosProcesamiento.sql");
    private static final String SQL_INSERTAR_SALDOS = CtaJavaCallInfra.loadSql(SQL + "InsertSaldos.sql");
    private static final String SQL_SUMA_SALDOS = CtaJavaCallInfra.loadSql(SQL + "SelectSumaSaldos.sql");
    private static final String SQL_INSERTAR_MOVIMIENTO = CtaJavaCallInfra.loadSql(SQL + "InsertCargoAbono.sql");
    private static final String SQL_BORRAR_SALDOS = CtaJavaCallInfra.loadSql(SQL + "DeleteCargoAbonoSaldosPaso.sql");
    private static final String SQL_BORRAR_A_GENERAR = CtaJavaCallInfra.loadSql(SQL + "DeleteCargoAbonoGenerarPaso.sql");
    private static final String SQL_CONCEPTOS_INSTRUCCION = CtaJavaCallInfra.loadSql(SQL + "SelectConceptoOrden.sql");
    private static final String SQL_CUENTAS_PROPIAS = CtaJavaCallInfra.loadSql(SQL + "SelectCuentaPropia.sql");
    private static final String SQL_CORREDORA = CtaJavaCallInfra.loadSql(SQL + "SelectCorredora.sql");
    private static final String SQL_CLIENTE = CtaJavaCallInfra.loadSql(SQL + "SelectCliente.sql");
    private static final String SQL_TIPO_COMPROBANTE_ORIGEN = CtaJavaCallInfra.loadSql(SQL + "SelectIdTipoComprobanteOrigen.sql");
    private static final String SQL_UTC_AHORA = CtaJavaCallInfra.loadSql(SQL + "SelectUtcAhora.sql");

    private static final String ABONO = "MOVABN";
    private static final String CARGO = "MOVCAR";
    private static final String SIN_FECHA = "0001-01-01";

    // ---------------------------------------------------------------------------------------------------- contract

    /** One movement to generate (field names and meaning of the JavaCall contract). */
    public static class CargoAbonoAGenerar {
        public Long NumeroCargoAbono;
        public Long IdPortafolio;
        public String IdMoneda;
        public String IdTipoMovimiento;
        public String IdConcepto;
        public String Descripcion;
        public String IdCondicionDeLiquidacion;
        public String FechaCondicionDeLiquidacion;
        public BigDecimal Monto;
        public Long IdEjecutivo;
        public Long IdSucursal;
        public String IdTipoProducto;
        public String IdTipoOperacion;
        public Long IdOperacion;
        public String FechaOperacion;
        public String FechaLiquidacionOperacion;
        public String FechaVencimientoOperacion;
        public String IdMonedaOperacion;
        public BigDecimal MontoOperacion;
        public BigDecimal TipoCambioOperacion;
        public Long IdContraparte;
        public String IdTipoComprobante;
        public Long IdComprobante;
        public String FolioComprobante;
        public Long IdComprobanteDetalle;
        public Long IdComprobanteFormaDePago;
        public String IdTipoMovimientoAsociado;
        public Long IdMovimientoAsociado;
        public String FechaComprobante;
        public Long IdPersonaPagoCobro;
        public Long IdInstrumento;
        public String IdTipoInstrumento;
        public BigDecimal Cantidad;
        public String Nemo;
        public String EsIndicadorBilateral;
        public String EsCargoAbonoPorTraspaso;
        public Long IdPortafolioTraspaso;
        public String GeneroContraparteEnTesoreria;
        public String CertificadoAutomatico;
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_simularOperacion;
        public String p_UsaReglasNegocio;
        public String p_FechaSistema;
        public String p_EsIngresoManual = "N";
        public List<CargoAbonoAGenerar> p_CargoAbonoAGenerar = new ArrayList<>();
    }

    public static class CargoAbonoGenerado {
        public Long NumeroCargoAbono;
        public Long IdCargoAbonoMovimiento;
        public String IdTipoMovimiento;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<CargoAbonoGenerado> P_OUT_CARGOABONOGENERADO = new ArrayList<>();
    }

    // ---------------------------------------------------------------------------------------------------- query rows (public fields: mapped by column label)

    /** The movement joined with account, currency, concept, type and client data (SelectDatosProcesamiento.sql). */
    public static class Procesamiento {
        public Long IdInterno;
        public Long IdCargoAbonoMovimiento;
        public Long IdPortafolioInformado;
        public String IdMonedaInformado;
        public String IdTipoMovimientoInformado;
        public String IdConceptoInformado;
        public String Descripcion;
        public String IdCondicionDeLiquidacion;
        public String FechaCondicionDeLiquidacion;
        public BigDecimal Monto;
        public Long IdEjecutivo;
        public Long IdSucursal;
        public String IdTipoProductoInformado;
        public String IdTipoOperacionInformado;
        public Long IdOperacion;
        public String FechaOperacion;
        public String FechaLiquidacionOperacion;
        public String FechaVencimientoOperacion;
        public String IdMonedaOperacion;
        public BigDecimal MontoOperacion;
        public BigDecimal TipoCambioOperacion;
        public Long IdContraparte;
        public String IdTipoComprobante;
        public Long IdComprobante;
        public String FolioComprobante;
        public Long IdComprobanteDetalle;
        public Long IdComprobanteFormaDePago;
        public String IdTipoMovimientoAsociado;
        public Long IdMovimientoAsociado;
        public String FechaComprobante;
        public Long IdPersonaPagoCobro;
        public Long IdInstrumento;
        public String IdTipoInstrumentoInformado;
        public BigDecimal Cantidad;
        public String Nemo;
        public String EsIndicadorBilateral;
        public String EsCargoAbonoPorTraspaso;
        public Long IdPortafolioTraspaso;
        public String GeneroContraparteEnTesoreria;
        public String CertificadoAutomatico;
        public Long IdCuentaInversion;
        public Long IdPortafolio;
        public String IdMoneda;
        public String IdEstado;
        public String IdTipoMovimiento;
        public String IdConcepto;
        public String IdMonedaTesoreria;
        public String IdEstadoMonedaTesoreria;
        public String IdTipoOperacion;
        public String IdTipoProducto;
        public String IdTipoInstrumento;
        public String NombreCompleto;
        public long KeyTraspaso;
        public String OperaCuentaInversion;
        public String DocumentoIdentidad;
        public String BloqueadoPorCumplimiento = "N";
        public String CodigoPortafoliCargo = "";
        /** Validation errors found so far (not a column). */
        int errores;
    }

    /** Row written to CTA.CargoAbonoMovimiento (parameter names of InsertCargoAbono.sql). */
    public static class MovimientoAInsertar {
        public Long IdInterno;
        public Long IdCargoAbonoMovimiento;
        public Long NumeroCargoAbono;
        public Date FechaSistema;
        public String IdModulo;
        public String IdTipoMovimiento;
        public String EsIngresoManual;
        public Long IdCuentaInversion;
        public String NombreCompleto;
        public String IdConcepto;
        public String Descripcion;
        public String IdCondicionDeLiquidacion;
        public Date FechaCondicionDeLiquidacion;
        public BigDecimal Monto;
        public BigDecimal Saldo;
        public BigDecimal SaldoPorFechaMovimiento;
        public BigDecimal SaldoLibroInventario;
        public String EsCertificadoAutomatico;
        public Timestamp FechaCertificacion;
        public Long IdEjecutivo;
        public Long IdSucursal;
        public String IdTipoComprobante;
        public Long IdComprobante;
        public String FolioComprobante;
        public Long IdComprobanteDetalle;
        public Long IdComprobanteFormaDePago;
        public String IdTipoMovimientoAsociado;
        public Long IdMovimientoAsociado;
        public Date FechaComprobante;
        public String IdTipoProducto;
        public String IdTipoOperacion;
        public Long IdOperacion;
        public Date FechaOperacion;
        public Date FechaLiquidacionOperacion;
        public Date FechaVencimientoOperacion;
        public String IdMonedaOperacion;
        public BigDecimal MontoOperacion;
        public BigDecimal TipoCambioOperacion;
        public Long IdInstrumento;
        public String IdTipoInstrumento;
        public String Nemo;
        public BigDecimal Cantidad;
        public String EsCargoAbonoPorTraspaso;
        public String EsIndicadorBilateral;
        public Long IdContraparte;
        public Long IdPersonaPagoCobro;
        public Long IdUsuario;
        public Long IdEmpresa;
        public String IdEstado;
        public Long IdCargoAbonoRelacionadoTraspaso;
    }

    public static class SaldoFlujo {
        public Long IdPortafolio;
        public String IdMoneda;
        public String FechaSaldo;
        public BigDecimal Saldo;
    }

    public static class SaldoFinal {
        public Long IdInterno;
        public BigDecimal SaldoFinal;
    }

    public static class CuentaDeCliente {
        public Long IdCuentaInversion;
        public Long IdCliente;
    }

    public static class ComprobanteDetalle {
        public String IdTipoComprobanteOrigen;
        public Long IdComprobanteDetalle;
    }

    public static class Corredora {
        public String DocumentoIdentidad;
    }

    public static class IdConcepto {
        public String IdConcepto;
    }

    public static class IdCuenta {
        public Long IdCuentaInversion;
    }

    public static class DetalleInformacion {
        public Long IdCargoAbonoMovimiento;
        public String Codigo;
        public String Descripcion;
    }

    public static class DetalleAdvertencia {
        public Long NumeroCargoAbono;
        public String Codigo;
        public String Descripcion;
    }

    public static class DetalleError {
        public Long NumeroCargoAbono;
        public String Codigo;
        public String Descripcion;
    }

    /** Day, time zone and clock the new movements are stamped with. */
    private record Reloj(Timestamp fechaHoraSistema, String fechaLocal, Timestamp fechaLocalHoraUtc) {
    }

    // ---------------------------------------------------------------------------------------------------- collaborators

    private final NamedParameterJdbcTemplate jdbc;
    private final ActualizarSaldoCargoAbonoService actualizarSaldo;
    private final CalendarioHabil calendario;
    private final FormatoMoneda formatoMoneda;
    private final ConfiguracionSistemaPort configuracion;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;

    public CrearCargoAbonoService(NamedParameterJdbcTemplate jdbc, ActualizarSaldoCargoAbonoService actualizarSaldo,
                                  CalendarioHabil calendario, FormatoMoneda formatoMoneda, ConfiguracionSistemaPort configuracion,
                                  DiccionarioMensajes diccionario, Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.actualizarSaldo = actualizarSaldo;
        this.calendario = calendario;
        this.formatoMoneda = formatoMoneda;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
    }

    // ---------------------------------------------------------------------------------------------------- entry points

    /** JavaCall entry point: JSON of {@link Entrada} in, JSON of {@link Salida} out. */
    @Transactional
    public JsonObject execute(JsonObject params) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        Salida salida = crear(gson.fromJson(params, Entrada.class));
        return gson.toJsonTree(salida).getAsJsonObject();
    }

    @Transactional
    public Salida crear(Entrada in) throws Exception {
        log.info("Inicio servicio CrearCargoAbono");
        Salida out = new Salida();
        String fechaSistema = CtaJavaCallInfra.parametro(configuracion, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        String usaCargoAbonoJava = CtaJavaCallInfra.parametro(configuracion, in.p_IdEmpresa, "PLATAFORMA", "PLA.UsaCargoAbonoJava");
        Set<String> conceptosInstruccion = jdbc.query(SQL_CONCEPTOS_INSTRUCCION, new MapSqlParameterSource("Uso", "INGRESOINSTRUCCION"),
                CtaJavaCallInfra.fieldMapper(IdConcepto.class)).stream().map(c -> c.IdConcepto).collect(Collectors.toSet());

        if (usaCargoAbonoJava.equalsIgnoreCase("N")) {
            crearConProcedimiento(in, out);
        } else {
            crearEnJava(in, out, fechaSistema, conceptosInstruccion);
        }
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.countErrors(out.P_OUT_MENSAJES);
        log.info("Fin servicio CrearCargoAbono");
        return out;
    }

    // ---------------------------------------------------------------------------------------------------- stored procedure path

    /** CTA-A-241..246: the procedure reads {@code CrearCargoAbono_IN_*} and leaves its answer in the {@code _OUT_*} tables. */
    private void crearConProcedimiento(Entrada in, Salida out) {
        if (!in.p_CargoAbonoAGenerar.isEmpty()) {
            jdbc.batchUpdate(SQL_INSERTAR_A_GENERAR, lote(in.p_CargoAbonoAGenerar));
        }
        llamarProcedimiento(in);

        MapSqlParameterSource sinParametros = new MapSqlParameterSource();
        out.P_OUT_CARGOABONOGENERADO = jdbc.query(SQL_SALIDA_GENERADOS, sinParametros, CtaJavaCallInfra.fieldMapper(CargoAbonoGenerado.class));
        // LEGACY DEFECT kept (see README CTA-A-241): the information rows are mapped to a POJO whose key field is not named
        // NumeroCargoAbono, so that column is never read; the message id is the movement id.
        for (DetalleInformacion i : jdbc.query(SQL_SALIDA_INFORMACION, sinParametros, CtaJavaCallInfra.fieldMapper(DetalleInformacion.class))) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(i.IdCargoAbonoMovimiento, MensajeSalida.OK, i.Codigo, i.Descripcion));
        }
        for (DetalleAdvertencia a : jdbc.query(SQL_SALIDA_ADVERTENCIAS, sinParametros, CtaJavaCallInfra.fieldMapper(DetalleAdvertencia.class))) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(a.NumeroCargoAbono, MensajeSalida.ADVERTENCIA, a.Codigo, a.Descripcion));
        }
        for (DetalleError e : jdbc.query(SQL_SALIDA_ERRORES, sinParametros, CtaJavaCallInfra.fieldMapper(DetalleError.class))) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(e.NumeroCargoAbono, MensajeSalida.ERROR, e.Codigo, e.Descripcion));
        }
    }

    /**
     * Calls the HANA procedure. The three counters it returns are bound but unused: the output tables are the source of truth.
     * LEGACY DEFECT kept (see README CTA-A-245): the SQL fixes {@code p_UsaReglasNegocio => 'S'}; the input value is ignored.
     */
    private void llamarProcedimiento(Entrada in) {
        CallableStatementCreator crear = con -> con.prepareCall(SQL_LLAMAR_PROCEDIMIENTO);
        CallableStatementCallback<Object> ejecutar = cs -> {
            Object[] entradas = {in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, in.p_simularOperacion, in.p_FechaSistema, in.p_EsIngresoManual};
            for (int i = 0; i < entradas.length; i++) {
                StatementCreatorUtils.setParameterValue(cs, i + 1, SqlTypeValue.TYPE_UNKNOWN, entradas[i]);
            }
            registrarSalidas(cs);
            cs.execute();
            return null;
        };
        jdbc.getJdbcTemplate().execute(crear, ejecutar);
    }

    private static void registrarSalidas(CallableStatement cs) throws SQLException {
        for (int posicion = 7; posicion <= 9; posicion++) {
            cs.registerOutParameter(posicion, Types.BIGINT);
        }
    }

    // ---------------------------------------------------------------------------------------------------- Java path

    private void crearEnJava(Entrada in, Salida out, String fechaSistema, Set<String> conceptosInstruccion) throws Exception {
        if (in.p_CargoAbonoAGenerar.isEmpty()) {
            return;
        }
        Map<Long, CargoAbonoAGenerar> informadoPorOperacion = new HashMap<>();
        for (CargoAbonoAGenerar movimiento : in.p_CargoAbonoAGenerar) {
            // LEGACY DEFECT kept (see README CTA-A-64): keyed by IdOperacion, so movements sharing an operation id share the message values
            informadoPorOperacion.put(movimiento.IdOperacion, movimiento);
        }
        jdbc.batchUpdate(SQL_INSERTAR_A_GENERAR, lote(in.p_CargoAbonoAGenerar));

        List<Procesamiento> procesamiento = jdbc.query(SQL_DATOS_PROCESAMIENTO, new MapSqlParameterSource(),
                CtaJavaCallInfra.fieldMapper(Procesamiento.class));
        registrarLog(in, procesamiento, fechaSistema);

        String fechaDesde = "";
        String fechaHasta = "";
        Set<String> portafolioMonedaVistos = new HashSet<>();
        List<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR> portafolioAConsultar = new ArrayList<>();
        for (Procesamiento proc : procesamiento) {
            if (fechaDesde.equalsIgnoreCase("")) {
                fechaDesde = proc.FechaCondicionDeLiquidacion;
                fechaHasta = proc.FechaCondicionDeLiquidacion;
            }
            CargoAbonoAGenerar informado = informadoPorOperacion.get(proc.IdOperacion);
            validar(in, out, proc, informado, fechaSistema, conceptosInstruccion);

            Map<String, String> valores = new LinkedHashMap<>();
            valores.put("NombreCliente", proc.NombreCompleto);
            valores.put("DocumentoIdentidad", proc.DocumentoIdentidad);
            for (Corredora corredora : jdbc.query(SQL_CORREDORA, new MapSqlParameterSource(), CtaJavaCallInfra.fieldMapper(Corredora.class))) {
                if (proc.DocumentoIdentidad.equalsIgnoreCase(corredora.DocumentoIdentidad)) {
                    proc.BloqueadoPorCumplimiento = "N";
                }
            }
            requisito(out, proc, ABONO.equalsIgnoreCase(proc.IdTipoMovimiento) && "S".equalsIgnoreCase(proc.BloqueadoPorCumplimiento)
                    && !"FFM".equalsIgnoreCase(proc.IdTipoInstrumento), "CLIENTE_BLOQUEADO_POR_CUMPLIMIENTO", in.p_IdEmpresa, valores);

            if (proc.IdTipoMovimiento == null || proc.IdTipoMovimiento.equalsIgnoreCase("")) {
                requisito(out, proc, true, "CUENTA_INVERSION_TRASPASO_INVALIDA", in.p_IdEmpresa, new LinkedHashMap<>());
                out.P_OUT_MENSAJES.add(new MensajeSalida(proc.IdInterno, MensajeSalida.ERROR, "TIPO_MOVIMIENTO_NO_EXISTE",
                        diccionario.traducir(in.p_IdEmpresa, "TIPO_MOVIMIENTO_NO_EXISTE", new LinkedHashMap<>())));
            } else {
                if (CARGO.equalsIgnoreCase(proc.IdTipoMovimiento) && Fecha.toDate(proc.FechaCondicionDeLiquidacion).compareTo(Fecha.toDate(fechaDesde)) < 0) {
                    fechaDesde = proc.FechaCondicionDeLiquidacion;
                }
                if (CARGO.equalsIgnoreCase(proc.IdTipoMovimiento) && Fecha.toDate(proc.FechaCondicionDeLiquidacion).compareTo(Fecha.toDate(fechaHasta)) > 0) {
                    fechaHasta = proc.FechaCondicionDeLiquidacion;
                }
                if (CARGO.equalsIgnoreCase(proc.IdTipoMovimiento) && ("S".equalsIgnoreCase(in.p_EsIngresoManual) || "CMBPTF".equalsIgnoreCase(proc.IdConcepto))
                        && portafolioMonedaVistos.add(proc.IdPortafolio + proc.IdMoneda)) {
                    SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR portafolio = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
                    portafolio.IdMoneda = proc.IdMoneda;
                    portafolio.IdPortafolio = proc.IdPortafolio;
                    portafolioAConsultar.add(portafolio);
                }
            }
            validarExistencias(in, out, proc, informado);
        }
        limpiarConErrores(procesamiento, new ArrayList<>());
        List<Procesamiento> abonos = procesamiento.stream().filter(c -> c.IdTipoMovimiento.equals(ABONO)).collect(Collectors.toList());
        List<Procesamiento> cargos = procesamiento.stream().filter(c -> c.IdTipoMovimiento.equals(CARGO)).collect(Collectors.toList());

        SaldoFlujoCaja.ServiceEntrada flujo = new SaldoFlujoCaja.ServiceEntrada();
        flujo.P_IDEMPRESA = in.p_IdEmpresa;
        flujo.P_FECHADESDE = fechaDesde;
        flujo.P_FECHAHASTA = fechaHasta;
        flujo.P_USO = "CUENTA_INVERSION";
        for (Procesamiento cargo : cargos) {
            if (CARGO.equalsIgnoreCase(cargo.IdTipoMovimiento) && ("S".equalsIgnoreCase(in.p_EsIngresoManual) || "CMBPTF".equalsIgnoreCase(cargo.IdConcepto))) {
                // LEGACY DEFECT kept (see README CTA-A-70): the whole list is added once per cargo, so portfolios repeat in the request
                flujo.P_PORTAFOLIOACONSULTAR.addAll(portafolioAConsultar);
            }
        }
        if (!flujo.P_PORTAFOLIOACONSULTAR.isEmpty()) {
            SaldoFlujoCaja.ServiceSalida salidaFlujo = colaboradores.invocar(SaldoFlujoCaja.CLASE, flujo, SaldoFlujoCaja.ServiceSalida.class);
            if (!salidaFlujo.P_OUT_FLUJOCAJA.isEmpty()) {
                List<MensajeSalida> errores = new ArrayList<>();
                for (SaldoFlujoCaja.ObjFLUJOCAJA f : salidaFlujo.P_OUT_FLUJOCAJA) {
                    for (Procesamiento cargo : cargos) {
                        if (cargo.Monto.compareTo(f.Saldo) > 0 && cargo.IdConcepto.equals("CMBPTF") && CARGO.equals(cargo.IdTipoMovimiento)
                                && cargo.FechaCondicionDeLiquidacion.equals(f.FechaSaldo) && cargo.IdPortafolio.equals(f.IdPortafolio)) {
                            Map<String, String> valores = new HashMap<>(2);
                            valores.put("CodigoPortafolio", cargo.CodigoPortafoliCargo);
                            valores.put("DocumentoIdentidad", cargo.DocumentoIdentidad);
                            errores.add(new MensajeSalida(cargo.IdInterno, MensajeSalida.ERROR, "UI_SALDO_INSUFICIENTE_FLUJO_CAJA",
                                    diccionario.traducir(in.p_IdEmpresa, "UI_SALDO_INSUFICIENTE_FLUJO_CAJA", valores)));
                        }
                    }
                }
                if (!errores.isEmpty()) {
                    // LEGACY DEFECT kept (see README CTA-A-71): returns before inserting and without emptying the step tables
                    out.P_OUT_MENSAJES.addAll(errores);
                    return;
                }
            }
        }

        Map<Object, Object> porClave = Utils.List2Hash(procesamiento, "KeyTraspaso");
        List<MovimientoAInsertar> abonosAInsertar = aMovimientos(in, abonos, porClave, fechaSistema, reloj(in, fechaSistema));
        insertarMovimientos(in, out, abonosAInsertar);

        if (!cargos.isEmpty() && !portafolioAConsultar.isEmpty() && "S".equalsIgnoreCase(in.p_EsIngresoManual)) {
            comprobarSaldoCaja(in, out, cargos, portafolioAConsultar, fechaDesde, fechaHasta);
        }

        limpiarConErrores(cargos, new ArrayList<>());
        List<MovimientoAInsertar> cargosAInsertar = aMovimientos(in, cargos, porClave, fechaSistema, reloj(in, fechaSistema));
        insertarMovimientos(in, out, cargosAInsertar);

        actualizarSaldos(in, abonosAInsertar, cargosAInsertar);

        jdbc.getJdbcTemplate().execute(SQL_BORRAR_SALDOS);
        jdbc.getJdbcTemplate().execute(SQL_BORRAR_A_GENERAR);
    }

    /** CTA-A-48: one INGRESO log entry per movement, grouped in a single AgregarLog call. */
    private void registrarLog(Entrada in, List<Procesamiento> procesamiento, String fechaSistema) throws Exception {
        if (procesamiento == null || procesamiento.isEmpty()) {
            return;
        }
        List<Long> cuentas = procesamiento.stream().map(p -> p.IdCuentaInversion).distinct().collect(Collectors.toList());
        Map<Object, Object> clientePorCuenta = new HashMap<>();
        if (!cuentas.isEmpty()) {
            clientePorCuenta = Utils.List2Hash(jdbc.query(SQL_CLIENTE, new MapSqlParameterSource("IdCuentaInversion", cuentas),
                    CtaJavaCallInfra.fieldMapper(CuentaDeCliente.class)), "IdCuentaInversion");
        }
        AgregarLog.InAgregarLog entradaLog = new AgregarLog.InAgregarLog();
        for (Procesamiento p : procesamiento) {
            CuentaDeCliente cliente = clientePorCuenta.isEmpty() ? new CuentaDeCliente() : (CuentaDeCliente) clientePorCuenta.get(p.IdCuentaInversion);
            AgregarLog.InAgregarLog.Log cabecera = new AgregarLog.InAgregarLog.Log();
            cabecera.NombreProceso = "Crear Cargo Abono";
            cabecera.IdCabeceraLog = p.IdCargoAbonoMovimiento != null ? p.IdCargoAbonoMovimiento : 0L;
            cabecera.IdTipoLog = "INGRESO";
            cabecera.IdModulo = in.p_IdModulo;
            cabecera.IdTipoRegistro = "CARGO_ABONO_MOVIMIENTO";
            cabecera.IdUsuario = in.p_IdUsuario;
            cabecera.IdEmpresa = in.p_IdEmpresa;
            cabecera.FechaSistemaLog = fechaSistema != null ? fechaSistema : "";
            cabecera.IdUsuarioAtendido = in.p_IdUsuario;
            cabecera.IdCliente = cliente != null && cliente.IdCliente != null ? cliente.IdCliente : 0L;
            cabecera.IdPortafolio = p.IdPortafolio != null ? p.IdPortafolio : 0L;
            cabecera.IdTipoOperacion = p.IdTipoMovimiento != null ? p.IdTipoMovimiento : "";
            cabecera.Folio = p.IdCargoAbonoMovimiento != null ? p.IdCargoAbonoMovimiento : 0L;
            cabecera.Monto = p.Monto != null ? p.Monto : BigDecimal.ZERO;
            cabecera.IdMonedaOrigen = p.IdMoneda != null ? p.IdMoneda : "";
            if (ABONO.equals(p.IdTipoMovimiento)) {
                cabecera.IdTipoRegistro = "ABONO_MOVIMIENTO";
            }
            if (CARGO.equals(p.IdTipoMovimiento)) {
                cabecera.IdTipoRegistro = "CARGO_MOVIMIENTO";
            }
            entradaLog.p_Log.add(cabecera);
        }
        colaboradores.invocar(AgregarLog.CLASE, entradaLog, AgregarLog.OutAgregarLog.class);
    }

    // ---------------------------------------------------------------------------------------------------- validation

    /** Requirements on the account, type, concept, settlement date, amount and receipts (CTA-A-53..62). */
    private void validar(Entrada in, Salida out, Procesamiento proc, CargoAbonoAGenerar informado, String fechaSistema,
                         Set<String> conceptosInstruccion) {
        Long empresa = in.p_IdEmpresa;
        requisito(out, proc, proc.IdPortafolioInformado == null || proc.IdMonedaInformado == null, "CUENTA_INVERSION_INVALIDA", empresa, new LinkedHashMap<>());
        requisito(out, proc, "S".equalsIgnoreCase(proc.EsCargoAbonoPorTraspaso) && proc.IdPortafolioTraspaso == null,
                "CUENTA_INVERSION_TRASPASO_INVALIDA", empresa, new LinkedHashMap<>());
        Map<String, String> cuenta = new LinkedHashMap<>();
        cuenta.put("NombreCliente", proc.NombreCompleto);
        cuenta.put("NombreMoneda", informado.IdMoneda);
        requisito(out, proc, proc.IdCuentaInversion == null, "CUENTA_INVERSION_NO_EXISTE", empresa, cuenta);
        requisito(out, proc, "INA".equalsIgnoreCase(proc.IdEstado), "CUENTA_INVERSION_NO_ACTIVA", empresa, cuenta);
        Map<String, String> tipo = new LinkedHashMap<>();
        tipo.put("IdTipoMovimiento", informado.IdTipoMovimiento);
        requisito(out, proc, proc.IdTipoMovimientoInformado == null || proc.IdTipoMovimientoInformado.length() == 0, "TIPO_MOVIMIENTO_INVALIDO", empresa, tipo);
        requisito(out, proc, proc.IdConceptoInformado == null || proc.IdConceptoInformado.length() == 0, "CONCEPTO_CUENTA_INVERSION_INVALIDO", empresa, new LinkedHashMap<>());
        Map<String, String> liquidacion = new LinkedHashMap<>();
        liquidacion.put("NombreCliente", proc.NombreCompleto);
        liquidacion.put("NombreMoneda", informado.IdMoneda);
        // LEGACY DEFECT kept (see README CTA-A-76): a null module throws NullPointerException here
        requisito(out, proc, !conceptosInstruccion.contains(proc.IdConceptoInformado) && !in.p_IdModulo.equalsIgnoreCase("FMM")
                && (Fecha.toDate(proc.FechaCondicionDeLiquidacion).compareTo(Fecha.toDate(fechaSistema)) < 0 && !proc.IdConcepto.equalsIgnoreCase("ABNCSC")),
                "FECHA_LIQUIDACION_MENOR_FECHA_MOVIMIENTO", empresa, liquidacion);
        // LEGACY DEFECT kept (see README CTA-A-58): the code says "menor o igual a cero" but zero is accepted
        requisito(out, proc, esMontoNegativo(proc.Monto), "MONTO_MENOR_IGUAL_A_CERO", empresa, new LinkedHashMap<>());
        requisito(out, proc, proc.IdTipoComprobante != null && proc.IdTipoComprobante.length() > 0
                && (proc.IdComprobante == null || proc.IdComprobante.compareTo(0L) == 0)
                && (proc.FolioComprobante == null || proc.FolioComprobante.equalsIgnoreCase("")),
                "SECUENCIA_O_FOLIO_COMPROBANTE_VACIO", empresa, new LinkedHashMap<>());
        requisito(out, proc, proc.IdTipoOperacion != null && proc.IdTipoOperacion.length() > 0
                && (proc.IdOperacion == null || proc.IdOperacion.compareTo(0L) == 0), "FOLIO_OPERACION_VACIO", empresa, new LinkedHashMap<>());
    }

    /** Checks that the codes informed exist (CTA-A-55, 62, 74). */
    private void validarExistencias(Entrada in, Salida out, Procesamiento proc, CargoAbonoAGenerar informado) {
        Long empresa = in.p_IdEmpresa;
        // LEGACY DEFECT kept (see README CTA-A-62): the outer joins return null, not "", so the *_NO_EXISTE checks never trigger
        Map<String, String> concepto = new LinkedHashMap<>();
        concepto.put("IdConcepto", informado.IdConcepto);
        requisito(out, proc, proc.IdConcepto == null || "".equalsIgnoreCase(proc.IdConcepto), "CONCEPTO_CUENTA_INVERSION_NO_EXISTE", empresa, concepto);
        Map<String, String> moneda = new LinkedHashMap<>();
        moneda.put("IdMoneda", informado.IdMoneda);
        // LEGACY DEFECT kept (see README CTA-A-55): a currency without Tesoreria row (null) throws NullPointerException
        requisito(out, proc, proc.IdMonedaTesoreria.equalsIgnoreCase(""), "MONEDA_NO_EXISTE", empresa, moneda);
        requisito(out, proc, proc.IdEstadoMonedaTesoreria.equalsIgnoreCase("INA"), "MONEDA_NO_ACTIVA", empresa, moneda);
        Map<String, String> operacion = new LinkedHashMap<>();
        operacion.put("IdTipoOperacion", informado.IdTipoOperacion);
        requisito(out, proc, proc.IdTipoOperacionInformado != null && proc.IdTipoOperacionInformado.length() > 0
                && proc.IdTipoOperacion != null && proc.IdTipoOperacion.equalsIgnoreCase(""), "TIPO_OPERACION_NO_EXISTE", empresa, operacion);
        Map<String, String> producto = new LinkedHashMap<>();
        producto.put("IdProducto", informado.IdTipoProducto);
        requisito(out, proc, proc.IdTipoProductoInformado != null && proc.IdTipoProductoInformado.length() > 0
                && proc.IdTipoProducto != null && proc.IdTipoProducto.equalsIgnoreCase(""), "PRODUCTO_NO_EXISTE", empresa, producto);
        Map<String, String> instrumento = new LinkedHashMap<>();
        instrumento.put("IdTipoInstrumento", informado.IdTipoInstrumento);
        requisito(out, proc, proc.IdTipoInstrumentoInformado != null && proc.IdTipoInstrumentoInformado.length() > 0
                && proc.IdTipoInstrumento != null && proc.IdTipoInstrumento.equalsIgnoreCase(""), "TIPO_INSTRUMENTO_NO_EXISTE", empresa, instrumento);
    }

    /** Adds an ERROR message for the movement when the condition holds. */
    private void requisito(Salida out, Procesamiento proc, boolean esError, String codigo, Long idEmpresa, Map<String, String> valores) {
        if (esError) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(proc.IdInterno, MensajeSalida.ERROR, codigo, diccionario.traducir(idEmpresa, codigo, valores)));
            proc.errores++;
        }
    }

    boolean esMontoNegativo(BigDecimal monto) {
        return monto.compareTo(BigDecimal.ZERO) < 0;
    }

    /** Pre-existing formula helper kept for ServiceFormulasTest; the state of a new movement is decided in {@link #aMovimientos}. */
    String idEstadoFormula(String esCertificado, int cantAutorizaciones) {
        return "Y".equalsIgnoreCase(esCertificado) && cantAutorizaciones == 0 ? "CRT" : "PEN";
    }

    /** Moves the movements that failed a requirement from {@code movimientos} to {@code conErrores}. */
    private static void limpiarConErrores(List<Procesamiento> movimientos, List<Procesamiento> conErrores) {
        for (Procesamiento m : movimientos) {
            if (m.errores > 0) {
                conErrores.add(m);
            }
        }
        movimientos.removeAll(conErrores);
    }

    // ---------------------------------------------------------------------------------------------------- cash check, insert, balances

    /** CTA-A-69, 70: manual cargos must not leave the portfolio cash flow negative. */
    private void comprobarSaldoCaja(Entrada in, Salida out, List<Procesamiento> cargos,
                                    List<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR> portafolios, String fechaDesde, String fechaHasta) throws Exception {
        SaldoFlujoCaja.ServiceEntrada entrada = new SaldoFlujoCaja.ServiceEntrada();
        entrada.P_IDEMPRESA = in.p_IdEmpresa;
        entrada.P_FECHADESDE = fechaDesde;
        entrada.P_FECHAHASTA = fechaHasta;
        entrada.P_USO = "CUENTA_INVERSION";
        entrada.P_PORTAFOLIOACONSULTAR = portafolios;
        SaldoFlujoCaja.ServiceSalida salida = colaboradores.invocar(SaldoFlujoCaja.CLASE, entrada, SaldoFlujoCaja.ServiceSalida.class);

        List<SaldoFlujo> saldos = new ArrayList<>();
        for (SaldoFlujoCaja.ObjFLUJOCAJA f : salida.P_OUT_FLUJOCAJA) {
            SaldoFlujo s = new SaldoFlujo();
            s.IdPortafolio = f.IdPortafolio;
            s.IdMoneda = f.IdMoneda;
            s.Saldo = formatoMoneda.formatear(f.Saldo, FormatoMoneda.Tipo.MONTO, f.IdMoneda).valor();
            s.FechaSaldo = f.FechaSaldo;
            saldos.add(s);
        }
        jdbc.batchUpdate(SQL_INSERTAR_SALDOS, lote(saldos));
        List<SaldoFinal> finales = jdbc.query(SQL_SUMA_SALDOS, new MapSqlParameterSource(), CtaJavaCallInfra.fieldMapper(SaldoFinal.class));
        Map<Object, Object> saldoFinalPorMovimiento = Utils.List2Hash(finales, "IdInterno");

        for (Procesamiento proc : cargos) {
            if (CARGO.equalsIgnoreCase(proc.IdTipoMovimiento) && "S".equalsIgnoreCase(proc.OperaCuentaInversion)) {
                SaldoFinal saldoFinal = (SaldoFinal) saldoFinalPorMovimiento.get(proc.IdInterno);
                requisito(out, proc, salida.P_OUT_ERRORESCONTADOR > 0L, "ERROR_CONSULTA_FLUJO_CAJA", in.p_IdEmpresa, new LinkedHashMap<>());
                Map<String, String> valores = new LinkedHashMap<>();
                valores.put("NombreCompleto", proc.NombreCompleto);
                valores.put("DocumentoIdentidad", proc.DocumentoIdentidad);
                requisito(out, proc, salida.P_OUT_ERRORESCONTADOR == 0L && saldoFinal.SaldoFinal.compareTo(BigDecimal.ZERO) < 0,
                        "SALDO_CAJA_INSUFICIENTE", in.p_IdEmpresa, valores);
            }
        }
    }

    /** Inserts the movements (unless simulating) and reports each one as created. */
    private void insertarMovimientos(Entrada in, Salida out, List<MovimientoAInsertar> movimientos) {
        // LEGACY DEFECT kept (see README CTA-A-51): a null p_simularOperacion is not "N", so nothing is inserted
        if ("N".equals(in.p_simularOperacion)) {
            jdbc.batchUpdate(SQL_INSERTAR_MOVIMIENTO, lote(movimientos));
        }
        for (MovimientoAInsertar m : movimientos) {
            CargoAbonoGenerado generado = new CargoAbonoGenerado();
            generado.IdCargoAbonoMovimiento = m.IdCargoAbonoMovimiento;
            generado.IdTipoMovimiento = m.IdTipoMovimiento;
            generado.NumeroCargoAbono = m.NumeroCargoAbono;
            out.P_OUT_CARGOABONOGENERADO.add(generado);
            String mensaje = (m.IdTipoMovimiento.equals(ABONO) ? "Abono N° " : "Cargo N° ") + m.IdCargoAbonoMovimiento
                    + (m.IdEstado.equalsIgnoreCase("PEN") ? " creado pendiente de certificacion" : " creado");
            out.P_OUT_MENSAJES.add(new MensajeSalida(m.IdInterno,
                    m.IdEstado.equalsIgnoreCase("CRT") ? MensajeSalida.INFORMACION : MensajeSalida.ADVERTENCIA, "ABONO_CARGO_CREADO", mensaje));
        }
    }

    /**
     * Own accounts: the balance job is scheduled (even when only simulating, LEGACY DEFECT kept, see README CTA-A-50). Third-party
     * accounts: balances are recalculated in line, only when really inserting.
     */
    private void actualizarSaldos(Entrada in, List<MovimientoAInsertar> abonos, List<MovimientoAInsertar> cargos) {
        List<Long> cuentasPropias = jdbc.query(SQL_CUENTAS_PROPIAS, new MapSqlParameterSource("p_IdEmpresa", in.p_IdEmpresa),
                CtaJavaCallInfra.fieldMapper(IdCuenta.class)).stream().map(c -> c.IdCuentaInversion).toList();

        List<Long> propias = new ArrayList<>();
        List<Long> terceros = new ArrayList<>();
        List<MovimientoAInsertar> todos = new ArrayList<>(abonos);
        todos.addAll(cargos);
        for (MovimientoAInsertar m : todos) {
            List<Long> destino = cuentasPropias.stream().anyMatch(c -> c.equals(m.IdCuentaInversion)) ? propias : terceros;
            if (!destino.contains(m.IdCuentaInversion)) {
                destino.add(m.IdCuentaInversion);
            }
        }

        if (!propias.isEmpty()) {
            AgregarPrograma.InAgregarPrograma programa = new AgregarPrograma.InAgregarPrograma();
            programa.p_IdEmpresa = in.p_IdEmpresa;
            programa.p_IdUsuario = in.p_IdUsuario;
            programa.p_IdModulo = in.p_IdModulo;
            programa.p_FechaSistema = in.p_FechaSistema;
            programa.p_IdTipoPrograma = "ACTUALIZAR_SALDO_CARGO_ABONO";
            programa.p_Segundos = 0L;
            programa.p_Parametros.add(parametro("CuentasInversion", String.join(";", propias.stream().map(String::valueOf).toList())));
            programa.p_Parametros.add(parametro("IdEmpresa", in.p_IdEmpresa.toString()));
            programa.p_Parametros.add(parametro("IdUsuario", in.p_IdUsuario.toString()));
            programa.p_Parametros.add(parametro("FechaSistema", in.p_FechaSistema));
            AgregarPrograma.OutAgregarPrograma resultado = colaboradores.invocar(AgregarPrograma.CLASE, programa, AgregarPrograma.OutAgregarPrograma.class);
            if (resultado.P_OUT_ERRORESCONTADOR > 0) {
                log.warn("Error agregando programa job");
            } else {
                log.info("Programa job agregado exitosamente");
            }
        }

        if (!terceros.isEmpty() && "N".equals(in.p_simularOperacion)) {
            ActualizarSaldoCargoAbonoService.Entrada entrada = new ActualizarSaldoCargoAbonoService.Entrada();
            entrada.p_IdEmpresa = in.p_IdEmpresa;
            entrada.p_IdUsuario = in.p_IdUsuario;
            entrada.p_FechaMovimiento = in.p_FechaSistema;
            for (Long id : terceros) {
                ActualizarSaldoCargoAbonoService.CuentaInversion cuenta = new ActualizarSaldoCargoAbonoService.CuentaInversion();
                cuenta.IdCuentaInversion = id;
                entrada.p_CuentaInversion.add(cuenta);
            }
            actualizarSaldo.actualizar(entrada);
        }
    }

    private static AgregarPrograma.InAgregarPrograma.Parametro parametro(String nombre, String valor) {
        AgregarPrograma.InAgregarPrograma.Parametro p = new AgregarPrograma.InAgregarPrograma.Parametro();
        p.NombreParametro = nombre;
        p.Valor = valor;
        return p;
    }

    // ---------------------------------------------------------------------------------------------------- movements to insert

    /** Dates and clock the new movements use: system date, UTC clock of the database and local business day (CTA-A-75). */
    private Reloj reloj(Entrada in, String fechaSistema) throws Exception {
        Long empresa = in.p_IdEmpresa;
        Timestamp utc = jdbc.getJdbcTemplate().queryForObject(SQL_UTC_AHORA, Timestamp.class);
        Timestamp fechaHoraSistema = Fecha.toDateTime(fechaSistema + " " + Fecha.toStringTime(utc));
        int zonaHoraria = (int) Long.parseLong(CtaJavaCallInfra.parametro(configuracion, empresa, "PLATAFORMA", "PLA.SegundosZonaHoraria"));
        String fechaLocal = Fecha.toStringDate(Fecha.addSeconds(utc, zonaHoraria));
        String horaUtc = Fecha.toStringTime(Fecha.toTime(Fecha.toFormat(utc, "HH:mm:ss")));
        Timestamp fechaLocalHoraUtc = Fecha.toTimeStamp(fechaLocal + " " + horaUtc);
        long idPais = Long.parseLong(CtaJavaCallInfra.parametro(configuracion, empresa, "PLATAFORMA", "PLA.PaisSistema"));

        if (!esDiaHabil(Fecha.toDate(fechaLocal).toLocalDate(), idPais)) {
            fechaLocal = calendario.siguiente(Fecha.toDate(fechaLocal).toLocalDate(), 1L, idPais, true, false, false).fecha().toString();
            fechaLocalHoraUtc = Fecha.addSeconds(Fecha.toTimeStamp(fechaLocal + " 00:01:00"), zonaHoraria * -1);
        }
        return new Reloj(fechaHoraSistema, fechaLocal, fechaLocalHoraUtc);
    }

    /** A day is a business day when counting one business day from the day before lands on it. */
    private boolean esDiaHabil(LocalDate fecha, long idPais) {
        return calendario.siguiente(fecha.minusDays(1), 1L, idPais, true, false, false).fecha().equals(fecha);
    }

    private List<MovimientoAInsertar> aMovimientos(Entrada in, List<Procesamiento> procesamiento, Map<Object, Object> porClave,
                                                   String fechaSistema, Reloj reloj) throws Exception {
        Map<Object, Object> origenPorDetalle = new HashMap<>();
        if (procesamiento != null && !procesamiento.isEmpty()) {
            List<Long> detalles = procesamiento.stream().map(p -> p.IdComprobanteDetalle).distinct().collect(Collectors.toList());
            if (!detalles.isEmpty()) {
                origenPorDetalle = Utils.List2Hash(jdbc.query(SQL_TIPO_COMPROBANTE_ORIGEN, new MapSqlParameterSource("IdComprobanteDetalle", detalles),
                        CtaJavaCallInfra.fieldMapper(ComprobanteDetalle.class)), "IdComprobanteDetalle");
            }
        }

        List<MovimientoAInsertar> movimientos = new ArrayList<>();
        for (Procesamiento p : procesamiento) {
            MovimientoAInsertar m = new MovimientoAInsertar();
            Procesamiento relacionado = (Procesamiento) porClave.get(p.KeyTraspaso * -1);

            m.IdInterno = p.IdInterno;
            m.IdCargoAbonoMovimiento = p.IdCargoAbonoMovimiento;
            m.NumeroCargoAbono = p.IdInterno;
            m.IdModulo = in.p_IdModulo;
            m.IdTipoMovimiento = p.IdTipoMovimiento;
            m.EsIngresoManual = in.p_EsIngresoManual;
            m.IdCuentaInversion = p.IdCuentaInversion;
            m.NombreCompleto = p.NombreCompleto;
            m.IdConcepto = p.IdConcepto;
            m.Descripcion = p.Descripcion;
            m.IdCondicionDeLiquidacion = p.IdCondicionDeLiquidacion;
            m.FechaCondicionDeLiquidacion = Fecha.toDate(p.FechaCondicionDeLiquidacion);
            m.Monto = p.Monto;
            m.Saldo = BigDecimal.ZERO;
            m.SaldoPorFechaMovimiento = BigDecimal.ZERO;
            m.SaldoLibroInventario = in.p_EsIngresoManual.equalsIgnoreCase("S") ? BigDecimal.ZERO : p.Monto;
            m.EsCertificadoAutomatico = p.CertificadoAutomatico;
            m.IdEjecutivo = p.IdEjecutivo;
            m.IdSucursal = p.IdSucursal != null ? p.IdSucursal : 0L;
            m.IdTipoComprobante = p.IdTipoComprobante;
            m.IdComprobante = p.IdComprobante;
            m.FolioComprobante = p.FolioComprobante;
            m.IdComprobanteDetalle = p.IdComprobanteDetalle;
            m.IdComprobanteFormaDePago = p.IdComprobanteFormaDePago;
            m.IdTipoMovimientoAsociado = p.IdTipoMovimientoAsociado;
            m.IdMovimientoAsociado = p.IdMovimientoAsociado;
            m.FechaComprobante = Fecha.toDate(p.FechaComprobante != null ? p.FechaComprobante : SIN_FECHA);
            m.IdTipoProducto = p.IdTipoProducto != null ? p.IdTipoProducto : "";
            m.IdTipoOperacion = p.IdTipoOperacion != null ? p.IdTipoOperacion : "";
            m.IdOperacion = p.IdOperacion != null ? p.IdOperacion : 0L;
            m.FechaOperacion = Fecha.toDate(p.FechaOperacion != null ? p.FechaOperacion : SIN_FECHA);
            m.FechaLiquidacionOperacion = Fecha.toDate(p.FechaLiquidacionOperacion != null ? p.FechaLiquidacionOperacion : SIN_FECHA);
            m.FechaVencimientoOperacion = Fecha.toDate(p.FechaVencimientoOperacion != null ? p.FechaVencimientoOperacion : SIN_FECHA);
            m.IdMonedaOperacion = p.IdMonedaOperacion;
            m.MontoOperacion = p.MontoOperacion;
            m.TipoCambioOperacion = p.TipoCambioOperacion;
            m.IdInstrumento = p.IdInstrumento;
            m.IdTipoInstrumento = p.IdTipoInstrumento != null ? p.IdTipoInstrumento : "";
            m.Nemo = p.Nemo != null ? p.Nemo : "";
            m.Cantidad = p.Cantidad != null ? p.Cantidad : BigDecimal.ZERO;
            m.EsCargoAbonoPorTraspaso = p.EsCargoAbonoPorTraspaso != null ? p.EsCargoAbonoPorTraspaso : "";
            m.EsIndicadorBilateral = p.EsIndicadorBilateral != null ? p.EsIndicadorBilateral : "";
            m.IdContraparte = p.IdContraparte != null ? p.IdContraparte : 0L;
            m.IdPersonaPagoCobro = p.IdPersonaPagoCobro != null ? p.IdPersonaPagoCobro : 0L;
            m.IdUsuario = in.p_IdUsuario;
            m.IdEmpresa = in.p_IdEmpresa;
            // LEGACY DEFECT kept (see README CTA-A-68): a null CertificadoAutomatico throws NullPointerException
            m.IdEstado = p.CertificadoAutomatico.equalsIgnoreCase("N") ? "PEN" : "CRT";
            m.IdCargoAbonoRelacionadoTraspaso = relacionado != null ? relacionado.IdCargoAbonoMovimiento : 0L;

            ComprobanteDetalle comprobante = origenPorDetalle.isEmpty() ? new ComprobanteDetalle() : (ComprobanteDetalle) origenPorDetalle.get(p.IdComprobanteDetalle);
            boolean esCsc = comprobante != null && comprobante.IdComprobanteDetalle != null && "CSC".equalsIgnoreCase(comprobante.IdTipoComprobanteOrigen);
            m.FechaSistema = esCsc ? Fecha.toDate(reloj.fechaLocal()) : Fecha.toDate(fechaSistema);
            m.FechaCertificacion = esCsc ? reloj.fechaLocalHoraUtc()
                    : in.p_EsIngresoManual.equalsIgnoreCase("S") ? Fecha.toTimeStamp("0001-01-01 00:00:00") : reloj.fechaHoraSistema();
            movimientos.add(m);
        }
        return movimientos;
    }

    // ---------------------------------------------------------------------------------------------------- JDBC helpers

    /** Batch parameters read from the public fields of the rows; amounts are written with 16 decimals (legacy SQL.batch). */
    private static SqlParameterSource[] lote(List<?> filas) {
        return filas.stream().map(CrearCargoAbonoService::parametros).toArray(SqlParameterSource[]::new);
    }

    private static MapSqlParameterSource parametros(Object fila) {
        MapSqlParameterSource p = new MapSqlParameterSource();
        for (java.lang.reflect.Field campo : fila.getClass().getFields()) {
            try {
                Object valor = campo.get(fila);
                p.addValue(campo.getName(), valor instanceof BigDecimal bd ? CtaJavaCallInfra.escala16(bd) : valor);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return p;
    }
}
