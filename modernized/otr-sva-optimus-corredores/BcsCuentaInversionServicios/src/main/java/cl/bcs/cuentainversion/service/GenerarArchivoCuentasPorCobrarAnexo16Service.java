package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.port.out.ColaboradoresReportes;
import cl.bcs.cuentainversion.port.out.GeneradorPdf;
import cl.bcs.cuentainversion.port.out.GeneradorPdf.Alineacion;
import cl.bcs.cuentainversion.port.out.GeneradorPdf.CeldaPdf;
import cl.bcs.cuentainversion.port.out.GeneradorPdf.DocumentoPdf;
import cl.bcs.cuentainversion.port.out.GeneradorPdf.FuentePdf;
import cl.bcs.cuentainversion.port.out.GeneradorPdf.LogoPdf;
import cl.bcs.cuentainversion.port.out.GeneradorPdf.PaginaPdf;
import cl.bcs.cuentainversion.port.out.GeneradorPdf.TablaPdf;
import cl.bcs.cuentainversion.reportes.ConfiguracionSistema;
import cl.bcs.cuentainversion.reportes.JsonReportes;
import cl.bcs.cuentainversion.reportes.MensajeSalida;
import cl.bcs.cuentainversion.reportes.Mensajeria;
import cl.bcs.cuentainversion.reportes.SqlReportes;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * "Anexo 16" report, accounts receivable ledger (legacy {@code GenerarArchivoCuentasPorCobrarAnexo16}), behaviours CTA-B-201..212 and 225.
 *
 * <p>Takes the negative cash balances of the day (collaborator {@code ConsultarSaldoCuentaInversion}), ages the debt by tranche
 * (current up to 2 days, up to 10, up to 30, more than 30; payments consume the oldest debt first) from the last 30 days of
 * movements and the opening balances (second call to the collaborator), converts foreign-currency amounts to the system
 * currency ({@code CalcularParidad}), groups by client with the related-party flag, and writes a zip
 * {@code <RutaBase><RutaCta>Anexo16_<ts>.zip} with {@code reporteAnexo16_<date>.xls} and {@code reporteAnexo16_<date>.pdf}.
 *
 * <p><b>PDF:</b> rendered through the {@link GeneradorPdf} port. No production adapter exists yet (no PDF library in the offline cache;
 * which library to use is a human decision), so a deployment must provide a {@code GeneradorPdf} bean before this service can start.
 */
@Service
public class GenerarArchivoCuentasPorCobrarAnexo16Service {

    private static final Logger log = LoggerFactory.getLogger(GenerarArchivoCuentasPorCobrarAnexo16Service.class);

    private static final Date FECHA_SEGURA = Date.valueOf(LocalDate.of(1900, 1, 1));
    private static final int DIAS_VIGENTE = 2;
    private static final int DIAS_SEGUNDO_TRAMO = 10;
    private static final int DIAS_TERCER_TRAMO = 30;

    private static final String SQL_INSERT_CUENTAS = SqlReportes.recurso("GenerarArchivoCuentasPorCobrarAnexo16_InsertCuentasInversion");
    private static final String SQL_CARGOS_ABONOS = SqlReportes.recurso("GenerarArchivoCuentasPorCobrarAnexo16_SelectCargosAbonosCtaInversion");
    private static final String SQL_RELACIONES = SqlReportes.recurso("GenerarArchivoCuentasPorCobrarAnexo16_SelectRelacionesClientes");
    private static final String SQL_CONFIGURACION = SqlReportes.recurso("GenerarArchivoCuentasPorCobrarAnexo16_SelectConfiguracion");
    private static final String SQL_LOGO = SqlReportes.recurso("GenerarArchivoCuentasPorCobrarAnexo16_SelectLogoCorredora");

    private final SqlReportes sql;
    private final ColaboradoresReportes colaboradores;
    private final GeneradorPdf generadorPdf;
    private final Gson gsonColaboradores = new GsonBuilder().serializeNulls().create();

    public GenerarArchivoCuentasPorCobrarAnexo16Service(NamedParameterJdbcTemplate jdbc, ColaboradoresReportes colaboradores,
                                                        GeneradorPdf generadorPdf) {
        this.sql = new SqlReportes(jdbc);
        this.colaboradores = colaboradores;
        this.generadorPdf = generadorPdf;
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        Salida salida = new Ejecucion(JsonReportes.leer(params, Entrada.class)).generar();
        salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
        return JsonReportes.escribir(salida);
    }

    /** State of one run (the legacy kept it in instance fields). */
    private final class Ejecucion {
        private final Entrada in;
        private final Salida out = new Salida();
        private final Mensajeria mensajes;
        private Date fechaConsulta;
        private String monedaSistema;
        private String fechaDesde;

        Ejecucion(Entrada in) {
            this.in = in;
            this.mensajes = new Mensajeria(sql, in.p_IdEmpresa);
        }

        Salida generar() throws Exception {
            log.info("INICIO::GenerarArchivoCuentasPorCobrarAnexo16");
            fechaConsulta = convertirFecha(in.p_FechaHasta);
            monedaSistema = new ConfiguracionSistema(sql).cadena(in.p_IdEmpresa, ConfiguracionSistema.PLATAFORMA, "PLA.MonedaSistema");
            fechaDesde = Date.valueOf(fechaConsulta.toLocalDate().minusDays(DIAS_TERCER_TRAMO)).toString();

            SalidaSaldo saldos = consultarSaldo(null, in.p_FechaHasta, in.p_FechaHasta);
            List<Cuenta> porCobrar = new ArrayList<>();
            filtrarCuentasPorCobrar(saldos, porCobrar);
            if (out.P_OUT_ERRORESCONTADOR > 0) {
                return out;
            }

            List<CargoAbono> cargosAbonos = obtenerCargosAbonos(porCobrar);
            List<SaldoInicialFinal> saldosIniciales = obtenerSaldosIniciales(cargosAbonos);
            List<Cuenta> conMovimientoReciente = clasificarTramos(cargosAbonos, saldosIniciales);

            List<Cuenta> agrupadas = agruparPorCuentaInversion(porCobrar, conMovimientoReciente);
            calcularParidad(agrupadas);
            List<Relacion> relaciones = obtenerRelaciones(agrupadas);
            List<Cuenta> porCliente = agruparPorCliente(agrupadas, relaciones);

            Rutas rutas = obtenerRutas();
            if (out.P_OUT_ERRORESCONTADOR > 0) {
                return out;
            }
            byte[] xls = generarExcel(porCliente);
            byte[] pdf = generarPdf(porCliente);
            guardarZip(xls, pdf, rutas.RutaFisica);
            out.P_OUT_RUTAARCHIVO = rutas.RutaDescarga;
            log.info("FIN::GenerarArchivoCuentasPorCobrarAnexo16");
            return out;
        }

        // ------------------------------------------------------------------------------------ collaborator: balances

        private SalidaSaldo consultarSaldo(List<CuentaAConsultar> cuentas, String desde, String hasta) {
            ConsultaSaldo entrada = new ConsultaSaldo();
            entrada.p_IdEmpresa = in.p_IdEmpresa;
            entrada.p_IdUsuario = in.p_IdUsuario;
            entrada.p_FechaDesde = desde;
            entrada.p_FechaHasta = hasta;
            if (cuentas == null || cuentas.isEmpty()) {
                CuentaAConsultar todas = new CuentaAConsultar();
                todas.IdEjecutivo = 0L;
                cuentas = List.of(todas);
            }
            entrada.p_CuentaInversionAConsultar = cuentas;
            JsonObject respuesta = colaboradores.consultarSaldo(gsonColaboradores.toJsonTree(entrada).getAsJsonObject());
            return Utils.getGsonBuilder().fromJson(respuesta, SalidaSaldo.class);
        }

        private void filtrarCuentasPorCobrar(SalidaSaldo saldos, List<Cuenta> resultado) {
            if (saldos.P_OUT_SALDOPORFECHAEFECTIVA == null || saldos.P_OUT_SALDOPORFECHAEFECTIVA.isEmpty()) {
                agregarError("NO_EXISTEN_MOVIMIENTOS_PARA_GENERAR_ARCHIVO");
                return;
            }
            Map<Long, SaldoInicialFinal> inicialFinal = new HashMap<>();
            saldos.P_OUT_SALDOINICIALFINAL.forEach(s -> inicialFinal.put(s.IdCuentaInversion, s));

            for (SaldoPorFechaEfectiva saldo : saldos.P_OUT_SALDOPORFECHAEFECTIVA) {
                // LEGACY DEFECT kept (see README CTA-B-203): with no negative row left the report is still generated (empty).
                if (saldo.SaldoDisponible.compareTo(BigDecimal.ZERO) >= 0 || "CPRO".equals(saldo.Relacion)) {
                    continue;
                }
                Cuenta c = new Cuenta();
                c.IdCuentaInversion = saldo.IdCuentaInversion;
                c.CodigoCuentaInversion = saldo.CodigoCuentaInversion;
                c.IdCliente = saldo.IdCliente;
                c.DocumentoIdentidad = saldo.DocumentoIdentidad;
                c.NombreCliente = saldo.NombreCliente;
                c.FechaSaldo = saldo.FechaSaldo;
                c.Relacion = saldo.Relacion;
                c.FechaUltimoMovimiento = convertirFecha(saldo.FechaUltimoMovimiento);
                c.SaldoEfectivo = saldo.SaldoDisponible;
                c.IdMoneda = saldo.IdMoneda;
                SaldoInicialFinal sif = inicialFinal.get(saldo.IdCuentaInversion);
                c.SaldoEfectivoInicial = sif != null ? sif.SaldoEfectivoInicial : BigDecimal.ZERO;
                c.SaldoEfectivoFinal = sif != null ? sif.SaldoEfectivoFinal : BigDecimal.ZERO;
                resultado.add(c);
            }
        }

        private List<SaldoInicialFinal> obtenerSaldosIniciales(List<CargoAbono> cargosAbonos) {
            List<CuentaAConsultar> cuentas = cargosAbonos.stream().map(c -> c.IdPortafolio).distinct().map(idPortafolio -> {
                CuentaAConsultar c = new CuentaAConsultar();
                c.IdEjecutivo = 0L;
                c.IdPortafolio = idPortafolio;
                return c;
            }).collect(Collectors.toList());
            return consultarSaldo(cuentas, fechaDesde, in.p_FechaHasta).P_OUT_SALDOINICIALFINAL;
        }

        // ------------------------------------------------------------------------------------ movements of the last 30 days

        private List<CargoAbono> obtenerCargosAbonos(List<Cuenta> porCobrar) {
            long idTrabajo = idTrabajo();
            List<CuentaPaso> pasos = porCobrar.stream()
                    .filter(c -> (long) Utils.diferenciaEnDias(fechaConsulta, c.FechaUltimoMovimiento) <= DIAS_TERCER_TRAMO)
                    .map(c -> new CuentaPaso(idTrabajo, c.IdCuentaInversion)).collect(Collectors.toList());
            // LEGACY DEFECT kept (see README CTA-B-201): the work table is never cleaned (random job id, rows stay).
            sql.lote(SQL_INSERT_CUENTAS, pasos);

            Map<String, Object> p = new HashMap<>();
            p.put("fechaDesde", Date.valueOf(fechaConsulta.toLocalDate().minusDays(DIAS_TERCER_TRAMO)));
            p.put("fechaHasta", fechaConsulta);
            p.put("idTrabajo", idTrabajo);
            return sql.consultar(SQL_CARGOS_ABONOS, p, CargoAbono.class);
        }

        /**
         * Ages the debt of every account with recent movements: the debt is a queue of (date, amount) tranches, increases add a tranche,
         * payments consume the oldest tranches first; what is left is classified by age.
         */
        private List<Cuenta> clasificarTramos(List<CargoAbono> cargosAbonos, List<SaldoInicialFinal> saldosIniciales) {
            Map<Long, SaldoInicialFinal> saldos = saldosIniciales.stream().collect(Collectors.toMap(s -> s.IdCuentaInversion, s -> s));
            Map<String, List<CargoAbono>> porCuenta = cargosAbonos.stream()
                    .collect(Collectors.groupingBy(c -> c.IdCliente + "_" + c.IdCuentaInversion + "_" + c.IdMoneda));

            List<Cuenta> clasificadas = new ArrayList<>();
            for (Map.Entry<String, List<CargoAbono>> entry : porCuenta.entrySet()) {
                List<CargoAbono> lista = entry.getValue();
                if (lista == null || lista.isEmpty()) {
                    continue;
                }
                lista.sort(Comparator.comparing((CargoAbono c) -> c.FechaEfectivaMovimiento).thenComparingLong(c -> c.IdCargoAbonoMovimiento));

                CargoAbono primero = lista.get(0);
                SaldoInicialFinal sif = saldos.get(primero.IdCuentaInversion);
                BigDecimal saldoEfectivo = sif != null && sif.SaldoEfectivoInicial != null ? sif.SaldoEfectivoInicial : BigDecimal.ZERO;

                List<SaldoTramo> tramos = new ArrayList<>();
                int cabecera = 0;
                BigDecimal deudaAnterior;
                if (saldoEfectivo.compareTo(BigDecimal.ZERO) < 0) {
                    BigDecimal deudaInicial = saldoEfectivo.abs();
                    tramos.add(new SaldoTramo(fechaConsulta.toLocalDate().minusDays(DIAS_TERCER_TRAMO + 10), deudaInicial));
                    deudaAnterior = deudaInicial;
                } else {
                    deudaAnterior = BigDecimal.ZERO;
                }

                for (CargoAbono mov : lista) {
                    BigDecimal delta = efectoEnSaldo(mov);
                    mov.SaldoEfectivoInicio = saldoEfectivo;
                    saldoEfectivo = saldoEfectivo.add(delta);
                    mov.SaldoEFectivoFinal = saldoEfectivo;

                    BigDecimal deudaActual = saldoEfectivo.compareTo(BigDecimal.ZERO) < 0 ? saldoEfectivo.abs() : BigDecimal.ZERO;
                    BigDecimal variacion = deudaActual.subtract(deudaAnterior);
                    if (variacion.compareTo(BigDecimal.ZERO) > 0) {
                        tramos.add(new SaldoTramo(mov.FechaEfectivaMovimiento.toLocalDate(), variacion));
                    } else if (variacion.compareTo(BigDecimal.ZERO) < 0) {
                        BigDecimal porPagar = variacion.abs();
                        while (porPagar.compareTo(BigDecimal.ZERO) > 0 && cabecera < tramos.size()) {
                            SaldoTramo tramo = tramos.get(cabecera);
                            if (tramo.saldo.compareTo(porPagar) > 0) {
                                tramo.saldo = tramo.saldo.subtract(porPagar);
                                porPagar = BigDecimal.ZERO;
                            } else {
                                porPagar = porPagar.subtract(tramo.saldo);
                                cabecera++;
                            }
                        }
                    }
                    deudaAnterior = deudaActual;
                }

                BigDecimal deudaAlCierre = saldoEfectivo.compareTo(BigDecimal.ZERO) < 0 ? saldoEfectivo.abs() : BigDecimal.ZERO;
                BigDecimal sumaTramos = BigDecimal.ZERO;
                for (int i = cabecera; i < tramos.size(); i++) {
                    if (tramos.get(i).saldo != null) {
                        sumaTramos = sumaTramos.add(tramos.get(i).saldo);
                    }
                }
                if (deudaAlCierre.compareTo(sumaTramos) > 0) {
                    tramos.add(new SaldoTramo(lista.get(lista.size() - 1).FechaEfectivaMovimiento.toLocalDate(), deudaAlCierre.subtract(sumaTramos)));
                }

                Tramos total = new Tramos();
                for (int i = cabecera; i < tramos.size(); i++) {
                    SaldoTramo t = tramos.get(i);
                    if (t.saldo == null || t.saldo.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                    total.sumar(clasificar(t));
                }
                total.Saldo = total.totalDeuda();

                CargoAbono ultimo = lista.get(lista.size() - 1);
                Cuenta c = new Cuenta();
                c.IdCliente = ultimo.IdCliente;
                c.IdCuentaInversion = ultimo.IdCuentaInversion;
                c.DocumentoIdentidad = ultimo.DocumentoIdentidadCliente;
                c.IdMoneda = ultimo.IdMoneda;
                c.SaldosPorTramo = total;
                clasificadas.add(c);
            }
            return clasificadas;
        }

        private BigDecimal efectoEnSaldo(CargoAbono mov) {
            BigDecimal monto = mov.Monto != null ? mov.Monto : BigDecimal.ZERO;
            if ("MOVABN".equals(mov.IdTipoMovimiento)) {
                return monto;
            }
            if ("MOVCAR".equals(mov.IdTipoMovimiento)) {
                return monto.negate();
            }
            return BigDecimal.ZERO;
        }

        /** Puts a whole amount in the tranche its age falls in (days since the effective date, negative counts as 0). */
        private Tramos clasificar(SaldoTramo t) {
            Tramos saldo = new Tramos();
            long dias = (long) Utils.diferenciaEnDias(fechaConsulta, Date.valueOf(t.fecha));
            if (dias < 0) {
                dias = 0L;
            }
            if (dias > DIAS_TERCER_TRAMO) {
                saldo.DeudaCuartoTramo = saldo.DeudaCuartoTramo.add(t.saldo);
            } else if (dias > DIAS_SEGUNDO_TRAMO) {
                saldo.DeudaTercerTramo = saldo.DeudaTercerTramo.add(t.saldo);
            } else if (dias > DIAS_VIGENTE) {
                saldo.DeudaSegundoTramo = saldo.DeudaSegundoTramo.add(t.saldo);
            } else {
                saldo.DeudaVigente = saldo.DeudaVigente.add(t.saldo);
            }
            return saldo;
        }

        /** Merges the accounts of the balance query with the ones aged from movements (key client_account_currency). */
        private List<Cuenta> agruparPorCuentaInversion(List<Cuenta> deSaldos, List<Cuenta> deMovimientos) {
            Set<String> clavesMovimientos = deMovimientos.stream().map(Ejecucion::clave).collect(Collectors.toSet());
            Map<String, Cuenta> conMovimientos = new HashMap<>();
            List<Cuenta> sinMovimientos = new ArrayList<>();
            for (Cuenta c : deSaldos) {
                if (clavesMovimientos.contains(clave(c))) {
                    conMovimientos.put(clave(c), c);
                } else {
                    sinMovimientos.add(c);
                }
            }

            Map<String, Cuenta> agrupadas = new HashMap<>();
            for (Cuenta c : sinMovimientos) {
                String clave = clave(c);
                SaldoTramo tramo = new SaldoTramo(c.FechaUltimoMovimiento.toLocalDate(), c.SaldoEfectivo);
                if (!agrupadas.containsKey(clave)) {
                    Cuenta nueva = new Cuenta();
                    nueva.IdCliente = c.IdCliente;
                    nueva.NombreCliente = c.NombreCliente;
                    nueva.DocumentoIdentidad = c.DocumentoIdentidad;
                    nueva.Relacion = c.Relacion;
                    nueva.IdCuentaInversion = c.IdCuentaInversion;
                    nueva.IdMoneda = c.IdMoneda;
                    nueva.SaldosPorTramo = clasificar(tramo);
                    nueva.SaldosPorTramo.Saldo = nueva.SaldosPorTramo.totalDeuda();
                    agrupadas.put(clave, nueva);
                } else {
                    Tramos existentes = agrupadas.get(clave).SaldosPorTramo;
                    existentes.sumar(clasificar(tramo));
                    existentes.Saldo = existentes.totalDeuda();
                }
            }

            for (Cuenta c : deMovimientos) {
                String clave = clave(c);
                // LEGACY DEFECT kept (see README CTA-B-210): an account whose currency differs between the balance row and the account
                // view has no counterpart here -> NullPointerException.
                Cuenta existente = conMovimientos.get(clave);
                Tramos existentes = existente.SaldosPorTramo;
                existentes.sumar(c.SaldosPorTramo);
                existentes.Saldo = existentes.totalDeuda();
                agrupadas.put(clave, existente);
            }
            return new ArrayList<>(agrupadas.values());
        }

        private static String clave(Cuenta c) {
            return c.IdCliente + "_" + c.IdCuentaInversion + "_" + c.IdMoneda;
        }

        // ------------------------------------------------------------------------------------ collaborator: parity

        /**
         * Converts the foreign-currency tranches to the system currency in ONE CalcularParidad request (one calculation per tranche
         * with a non zero amount; IdUso carries the tranche name). A tranche without answer or with a missing local amount becomes zero.
         */
        private void calcularParidad(List<Cuenta> cuentas) {
            JsonArray calculos = new JsonArray();
            Map<String, Cuenta> porCuentaMoneda = new HashMap<>();
            for (Cuenta c : cuentas) {
                porCuentaMoneda.put(c.IdCuentaInversion + "_" + c.IdMoneda, c);
                agregarCalculo(calculos, c, c.SaldosPorTramo.DeudaVigente, "VIGENTE");
                agregarCalculo(calculos, c, c.SaldosPorTramo.DeudaSegundoTramo, "SEGUNDO_TRAMO");
                agregarCalculo(calculos, c, c.SaldosPorTramo.DeudaTercerTramo, "TERCER_TRAMO");
                agregarCalculo(calculos, c, c.SaldosPorTramo.DeudaCuartoTramo, "CUARTO_TRAMO");
            }
            JsonObject entrada = new JsonObject();
            entrada.addProperty("p_IdEmpresa", in.p_IdEmpresa);
            entrada.addProperty("p_IdTrabajo", 0L);
            entrada.add("p_CalculosEntrada", calculos);
            JsonObject respuesta = colaboradores.calcularParidad(entrada);

            for (JsonElement e : respuesta.getAsJsonArray("p_out_CalculosSalida")) {
                JsonObject s = e.getAsJsonObject();
                Cuenta c = porCuentaMoneda.get(s.get("IdInterno").getAsLong() + "_" + s.get("IdMonedaOrigen").getAsString());
                if (c == null) {
                    continue;
                }
                JsonElement local = s.get("MontoLocal");
                BigDecimal montoLocal = local != null && !local.isJsonNull() ? local.getAsBigDecimal() : BigDecimal.ZERO;
                switch (s.get("IdUso").getAsString()) {
                    case "VIGENTE" -> c.SaldosPorTramo.DeudaVigente = montoLocal;
                    case "SEGUNDO_TRAMO" -> c.SaldosPorTramo.DeudaSegundoTramo = montoLocal;
                    case "TERCER_TRAMO" -> c.SaldosPorTramo.DeudaTercerTramo = montoLocal;
                    case "CUARTO_TRAMO" -> c.SaldosPorTramo.DeudaCuartoTramo = montoLocal;
                    default -> {
                    }
                }
            }
        }

        private void agregarCalculo(JsonArray calculos, Cuenta c, BigDecimal monto, String idUso) {
            // LEGACY DEFECT kept (see README CTA-B-211): a foreign amount without a rate comes back with no local amount and becomes zero silently.
            if (!c.IdMoneda.equalsIgnoreCase(monedaSistema) && monto.compareTo(BigDecimal.ZERO) != 0) {
                JsonObject calculo = new JsonObject();
                calculo.addProperty("IdTrabajo", 0L);
                calculo.addProperty("IdInterno", c.IdCuentaInversion);
                calculo.addProperty("Fecha", in.p_FechaHasta);
                calculo.addProperty("IdMonedaOrigen", c.IdMoneda);
                calculo.addProperty("IdMonedaDestino", monedaSistema);
                calculo.addProperty("ValorCambioLocalModificado", BigDecimal.ZERO);
                calculo.addProperty("IdUso", idUso);
                calculo.addProperty("MontoOrigen", monto);
                calculos.add(calculo);
            }
        }

        // ------------------------------------------------------------------------------------ relations and client grouping

        private List<Relacion> obtenerRelaciones(List<Cuenta> cuentas) {
            long idTrabajo = idTrabajo();
            List<CuentaPaso> pasos = cuentas.stream().map(c -> new CuentaPaso(idTrabajo, c.IdCuentaInversion)).collect(Collectors.toList());
            sql.lote(SQL_INSERT_CUENTAS, pasos);
            return sql.consultar(SQL_RELACIONES, Map.of("idTrabajo", idTrabajo), Relacion.class);
        }

        private List<Cuenta> agruparPorCliente(List<Cuenta> cuentas, List<Relacion> relaciones) {
            Map<Long, List<Cuenta>> porCliente = cuentas.stream().collect(Collectors.groupingBy(c -> c.IdCliente));
            Map<Long, Relacion> relacionPorCliente = new HashMap<>();
            for (Relacion r : relaciones) {
                relacionPorCliente.putIfAbsent(r.IdCliente, r);
            }
            List<Cuenta> resultado = new ArrayList<>();
            porCliente.forEach((idCliente, deCliente) -> {
                Cuenta agregada = new Cuenta();
                agregada.IdCliente = idCliente;
                agregada.NombreCliente = deCliente.get(0).NombreCliente;
                agregada.DocumentoIdentidad = deCliente.get(0).DocumentoIdentidad;
                agregada.Relacion = deCliente.get(0).Relacion;
                agregada.SaldosPorTramo = new Tramos();
                deCliente.forEach(c -> agregada.SaldosPorTramo.sumarAbsolutos(c.SaldosPorTramo));
                agregada.SaldosPorTramo.Saldo = agregada.SaldosPorTramo.totalDeuda();
                Relacion relacion = relacionPorCliente.get(idCliente);
                if (relacion != null) {
                    agregada.Relacion = relacion.Relacionado;
                    agregada.DescripcionRelacion = relacion.Traduccion;
                }
                resultado.add(agregada);
            });
            return resultado;
        }

        // ------------------------------------------------------------------------------------ files

        private Rutas obtenerRutas() {
            List<Configuracion> configuraciones = sql.consultar(SQL_CONFIGURACION, Map.of("IdEmpresa", in.p_IdEmpresa), Configuracion.class);
            Rutas rutas = new Rutas();
            // LEGACY DEFECT kept (see README CTA-B-204): the query uses MAX() and always answers one row, so this error can never fire;
            // without configuration the zip is written to the working directory.
            if (configuraciones.isEmpty()) {
                agregarError("FALTA_CONFIGURACION_RUTA_ARCHIVO");
                return rutas;
            }
            Configuracion conf = configuraciones.get(0);
            String nombre = "Anexo16_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".zip";
            rutas.RutaFisica = (conf.RutaArchivo != null ? conf.RutaArchivo : "") + nombre;
            rutas.RutaDescarga = (conf.RutaCta != null ? conf.RutaCta : "") + nombre;
            return rutas;
        }

        private String traduccion(String codigo) {
            return mensajes.texto(codigo, Map.of());
        }

        private byte[] generarExcel(List<Cuenta> lista) throws IOException {
            try (HSSFWorkbook libro = new HSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
                HSSFSheet hoja = libro.createSheet("Anexo16");
                hoja.createRow(0).createCell(0).setCellValue(traduccion("UI_ANEXO16_LIBRO_DIARIO_DE_CUENTAS_POR_COBRAR") + " " + in.p_FechaHasta);
                hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));
                for (int i = 0; i <= 10; i++) {
                    hoja.autoSizeColumn(i);
                }
                String[] titulos = {"UI_SECUENCIA", "UI_RUT_CLIENTE", "UI_NOMBRE_CLIENTE", "UI_MONTOCOBRAR", "UI_VIGENTES", "UI_HASTA10", "UI_HASTA30",
                        "UI_MAS30", "UI_OBSERVACIONES", "UI_CLASIFICACION_CONTABLE", "UI_RELACIONES"};
                HSSFRow cabecera = hoja.createRow(1);
                for (int c = 0; c < titulos.length; c++) {
                    cabecera.createCell(c).setCellValue(traduccion(titulos[c]));
                }
                int fila = 2;
                int secuencia = 1;
                for (Cuenta c : lista) {
                    HSSFRow r = hoja.createRow(fila++);
                    r.createCell(0).setCellValue(secuencia++);
                    r.createCell(1).setCellValue(c.DocumentoIdentidad);
                    r.createCell(2).setCellValue(c.NombreCliente);
                    r.createCell(3).setCellValue(c.SaldosPorTramo.saldoEntero().doubleValue());
                    r.createCell(4).setCellValue(c.SaldosPorTramo.vigenteEntero().doubleValue());
                    r.createCell(5).setCellValue(c.SaldosPorTramo.segundoTramoEntero().doubleValue());
                    r.createCell(6).setCellValue(c.SaldosPorTramo.tercerTramoEntero().doubleValue());
                    r.createCell(7).setCellValue(c.SaldosPorTramo.cuartoTramoEntero().doubleValue());
                    r.createCell(8).setCellValue("");
                    r.createCell(9).setCellValue(c.DescripcionRelacion);
                    r.createCell(10).setCellValue(c.Relacion);
                    hoja.autoSizeColumn(0);
                }
                libro.write(salida);
                return salida.toByteArray();
            }
        }

        private byte[] generarPdf(List<Cuenta> lista) throws IOException {
            // LEGACY DEFECT kept (see README CTA-B-205): `imagenes != null || !imagenes.isEmpty()` is always true, so a missing logo is an IndexOutOfBoundsException.
            LogoCorredora logo = sql.consultar(SQL_LOGO, Map.of(), LogoCorredora.class).get(0);

            LocalDateTime ahora = LocalDateTime.now();
            String fecha = ahora.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            String hora = ahora.format(DateTimeFormatter.ofPattern("HH:mm"));

            FuentePdf titulo = new FuentePdf(true, 10);
            FuentePdf normal = new FuentePdf(false, 7);
            FuentePdf cabecera = new FuentePdf(false, 6);

            List<CeldaPdf> encabezado = List.of(
                    celda(null, null, null, Alineacion.ARRIBA, 0, 1, 1, -1, true),
                    celda(traduccion("UI_ANEXO16_LIBRO_DIARIO_DE_CUENTAS_POR_COBRAR"), titulo, Alineacion.CENTRO,
                            // LEGACY quirk kept: the vertical alignment meant for the detail cell was set on the title cell
                            Alineacion.MEDIO, 0, 1, 1, -1, false),
                    celda("Fecha: " + fecha + "\nHora: " + hora, normal, Alineacion.DERECHA, null, 0, 1, 1, -1, false),
                    celda("", null, null, null, 0, 1, 1, -1, false),
                    celda(traduccion("UI_DETALLE_CTAS") + in.p_FechaHasta, normal, Alineacion.IZQUIERDA, null, 0, 2, 1, -1, false));
            List<CeldaPdf> pie = List.of(
                    celda(traduccion("UI_REFERENCIA"), normal, Alineacion.IZQUIERDA, null, 0, 1, 1, -1, false),
                    celda("(*)" + traduccion("UI_CLAS_CONT_NO") + "\n" + "o " + traduccion("UI_CLAS_CONT_SI"), normal, Alineacion.IZQUIERDA, null, 0, 1, 1, -1, false),
                    celda("(**) Cliente Relacionado: SI - NO.", normal, Alineacion.IZQUIERDA, null, 0, 1, 1, -1, false));

            List<CeldaPdf> celdas = new ArrayList<>();
            celdas.add(cabecera(traduccion("UI_SECUENCIA"), cabecera, 1, 2));
            celdas.add(cabecera(traduccion("UI_RUT_CLIENTE"), cabecera, 1, 2));
            celdas.add(cabecera(traduccion("UI_NOMBRE_CLIENTE"), cabecera, 1, 2));
            celdas.add(cabecera(traduccion("UI_MONTOCOBRAR"), cabecera, 1, 2));
            celdas.add(cabecera(traduccion("UI_ANTIGUEDAD"), cabecera, 4, 1));
            celdas.add(cabecera(traduccion("UI_OBSERVACION"), cabecera, 1, 2));
            celdas.add(cabecera(traduccion("UI_CLASIFICACION_CONTABLE") + "(*)", cabecera, 1, 2));
            celdas.add(cabecera(traduccion("UI_RELACIONADO") + "(**)", cabecera, 1, 2));
            celdas.add(cabecera(traduccion("UI_VIGENTES"), cabecera, 1, 1));
            celdas.add(cabecera(traduccion("UI_HASTA10"), cabecera, 1, 1));
            celdas.add(cabecera(traduccion("UI_HASTA30"), cabecera, 1, 1));
            celdas.add(cabecera(traduccion("UI_MAS30"), cabecera, 1, 1));

            int secuencia = 1;
            for (Cuenta c : lista) {
                celdas.add(cuerpo(String.valueOf(secuencia++), normal, Alineacion.CENTRO));
                celdas.add(cuerpo(c.DocumentoIdentidad, normal, Alineacion.CENTRO));
                celdas.add(cuerpo(c.NombreCliente, normal, Alineacion.IZQUIERDA));
                celdas.add(cuerpo(c.SaldosPorTramo.saldoEntero().toString(), normal, Alineacion.DERECHA));
                celdas.add(cuerpo(c.SaldosPorTramo.vigenteEntero().toString(), normal, Alineacion.DERECHA));
                celdas.add(cuerpo(c.SaldosPorTramo.segundoTramoEntero().toString(), normal, Alineacion.DERECHA));
                celdas.add(cuerpo(c.SaldosPorTramo.tercerTramoEntero().toString(), normal, Alineacion.DERECHA));
                celdas.add(cuerpo(c.SaldosPorTramo.cuartoTramoEntero().toString(), normal, Alineacion.DERECHA));
                celdas.add(cuerpo("", normal, Alineacion.IZQUIERDA));
                celdas.add(cuerpo(c.DescripcionRelacion, normal, Alineacion.IZQUIERDA));
                celdas.add(cuerpo(c.Relacion, normal, Alineacion.CENTRO));
            }

            DocumentoPdf documento = new DocumentoPdf(
                    new PaginaPdf(842, 595, 36, 36, 130, 36),
                    new LogoPdf(logo.imagen, 70, 70),
                    new TablaPdf(new float[]{1, 3, 1}, 0, encabezado),
                    new TablaPdf(new float[]{1}, 0, pie),
                    2,
                    new TablaPdf(new float[]{1.2f, 1.5f, 4.0f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 2.5f, 2f, 2f}, 2, celdas),
                    2);
            return generadorPdf.generar(documento);
        }

        private void guardarZip(byte[] xls, byte[] pdf, String ruta) throws IOException {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
                zip.putNextEntry(new ZipEntry("reporteAnexo16_" + fechaConsulta + ".xls"));
                zip.write(xls);
                zip.closeEntry();
                zip.putNextEntry(new ZipEntry("reporteAnexo16_" + fechaConsulta + ".pdf"));
                zip.write(pdf);
                zip.closeEntry();
            }
            // LEGACY DEFECT kept (see README CTA-B-204): an unwritable directory is a NoSuchFileException nobody catches.
            Files.write(Paths.get(ruta), bytes.toByteArray());
        }

        // ------------------------------------------------------------------------------------ helpers

        private void agregarError(String codigo) {
            out.P_OUT_MENSAJES.add(mensajes.error(codigo, Map.of()));
            out.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(out.P_OUT_MENSAJES);
        }

        /** A date that cannot be parsed becomes 1900-01-01 and everything is "current" (README CTA-B-212). */
        private Date convertirFecha(String fecha) {
            try {
                return Date.valueOf(fecha.trim());
            } catch (Exception e) {
                // LEGACY DEFECT kept (see README CTA-B-212): unparseable date -> 1900-01-01.
                return FECHA_SEGURA;
            }
        }

        private long idTrabajo() {
            return (long) (Math.random() * 10000000000L);
        }
    }

    private static CeldaPdf celda(String texto, FuentePdf fuente, Alineacion horizontal, Alineacion vertical, int borde, int colspan, int rowspan,
                                  float padding, boolean imagen) {
        return new CeldaPdf(texto, fuente, horizontal, vertical, borde, colspan, rowspan, padding, imagen);
    }

    private static CeldaPdf cabecera(String texto, FuentePdf fuente, int colspan, int rowspan) {
        return celda(texto, fuente, Alineacion.CENTRO, null, 15, colspan, rowspan, 5, false);
    }

    private static CeldaPdf cuerpo(String texto, FuentePdf fuente, Alineacion horizontal) {
        return celda(texto, fuente, horizontal, null, 15, 1, 1, 5, false);
    }

    // =============================================================================================================
    // JSON contract and working types
    // =============================================================================================================

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_FechaHasta;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public String P_OUT_RUTAARCHIVO;
    }

    public static class Configuracion {
        public String RutaBase;
        public String RutaCta;
        public String RutaArchivo;
    }

    public static class Rutas {
        public String RutaFisica;
        public String RutaDescarga;
    }

    /** Receivable account while it is being aged (legacy {@code CuentaPorCobrarAnexo16}). */
    public static class Cuenta {
        public Long IdCuentaInversion;
        public String CodigoCuentaInversion;
        public String DocumentoIdentidad;
        public String NombreCliente;
        public String DescripcionRelacion;
        public String Relacion;
        public Long IdCliente;
        public String FechaSaldo;
        public Date FechaUltimoMovimiento;
        public BigDecimal SaldoEfectivo = BigDecimal.ZERO;
        public Tramos SaldosPorTramo = new Tramos();
        public String IdMoneda;
        public BigDecimal SaldoEfectivoInicial;
        public BigDecimal SaldoEfectivoFinal;
    }

    /** Debt by age tranche. */
    public static class Tramos {
        public BigDecimal DeudaVigente = BigDecimal.ZERO;
        public BigDecimal DeudaSegundoTramo = BigDecimal.ZERO;
        public BigDecimal DeudaTercerTramo = BigDecimal.ZERO;
        public BigDecimal DeudaCuartoTramo = BigDecimal.ZERO;
        public BigDecimal Saldo = BigDecimal.ZERO;

        BigDecimal totalDeuda() {
            return DeudaVigente.add(DeudaSegundoTramo).add(DeudaTercerTramo).add(DeudaCuartoTramo);
        }

        void sumar(Tramos o) {
            DeudaVigente = DeudaVigente.add(o.DeudaVigente);
            DeudaSegundoTramo = DeudaSegundoTramo.add(o.DeudaSegundoTramo);
            DeudaTercerTramo = DeudaTercerTramo.add(o.DeudaTercerTramo);
            DeudaCuartoTramo = DeudaCuartoTramo.add(o.DeudaCuartoTramo);
        }

        void sumarAbsolutos(Tramos o) {
            DeudaVigente = DeudaVigente.add(o.DeudaVigente.abs());
            DeudaSegundoTramo = DeudaSegundoTramo.add(o.DeudaSegundoTramo.abs());
            DeudaTercerTramo = DeudaTercerTramo.add(o.DeudaTercerTramo.abs());
            DeudaCuartoTramo = DeudaCuartoTramo.add(o.DeudaCuartoTramo.abs());
        }

        /** Whole units, truncated (the report shows no decimals). */
        private static BigInteger entero(BigDecimal v) {
            return v.setScale(0, RoundingMode.DOWN).toBigIntegerExact();
        }

        BigInteger saldoEntero() {
            return entero(Saldo);
        }

        BigInteger vigenteEntero() {
            return entero(DeudaVigente);
        }

        BigInteger segundoTramoEntero() {
            return entero(DeudaSegundoTramo);
        }

        BigInteger tercerTramoEntero() {
            return entero(DeudaTercerTramo);
        }

        BigInteger cuartoTramoEntero() {
            return entero(DeudaCuartoTramo);
        }
    }

    private static final class SaldoTramo {
        final LocalDate fecha;
        BigDecimal saldo;

        SaldoTramo(LocalDate fecha, BigDecimal saldo) {
            this.fecha = fecha;
            this.saldo = saldo;
        }
    }

    /** Row of the work table and parameters of its insert. */
    public static class CuentaPaso {
        public Long IdTrabajo;
        public Long IdCuentaInversion;

        CuentaPaso(Long idTrabajo, Long idCuentaInversion) {
            this.IdTrabajo = idTrabajo;
            this.IdCuentaInversion = idCuentaInversion;
        }
    }

    public static class CargoAbono {
        public Long IdCargoAbonoMovimiento;
        public String IdTipoMovimiento;
        public String IdConcepto;
        public Long IdCuentaInversion;
        public Long IdCliente;
        public String DocumentoIdentidadCliente;
        public Long IdPortafolio;
        public String CodigoPortafolio;
        public String Proposito;
        public String IdMoneda;
        public String IdCondicionDeLiquidacion;
        public BigDecimal Monto = BigDecimal.ZERO;
        public String IdEstado;
        public BigDecimal SaldoPorFechaMovimiento = BigDecimal.ZERO;
        public Date FechaSistema;
        public String EsCargoAbonoPorTraspaso;
        public String IdModulo;
        public Date FechaEfectivaMovimiento;
        public Timestamp FechaIngresoRegistro;
        public BigDecimal SaldoEfectivoInicio = BigDecimal.ZERO;
        public BigDecimal SaldoEFectivoFinal = BigDecimal.ZERO;
    }

    public static class LogoCorredora {
        public byte[] imagen;
    }

    public static class Relacion {
        public Long IdCliente;
        public String Relacionado;
        public String Traduccion;
    }

    // ---- ConsultarSaldoCuentaInversion contract (JSON of the legacy POJOs)

    static class ConsultaSaldo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo = "CTA";
        public String p_UsaReglasNegocio = "S";
        public List<CuentaAConsultar> p_CuentaInversionAConsultar;
        public String p_FechaDesde;
        public String p_FechaHasta;
        public String p_SoloSaldoNegativo = "S";
        public String p_SoloSaldoDisponible = "N";
        public String p_SoloSaldoDisponibleNegativo = "N";
        public String p_IncluyeCargosPendientes = "N";
        public String p_IncluyeAbonosPendientes = "N";
        public String p_IncluyeOtrosSaldos = "S";
        public Long p_IdPortafolio;
        public String p_IncluyeProductoApv = "N";
        public Long p_IdEjecutivo = 0L;
        public Long p_IdCliente;
        public Long p_IdProductoAgrupador;
        public String p_IdMoneda;
        public List<String> p_TipoSaldoConsultar = List.of("TODO");
    }

    static class CuentaAConsultar {
        public Long IdEjecutivo;
        public Long IdPortafolio;
        public String IdMoneda;
    }

    public static class SalidaSaldo {
        public List<SaldoPorFechaEfectiva> P_OUT_SALDOPORFECHAEFECTIVA;
        public List<SaldoInicialFinal> P_OUT_SALDOINICIALFINAL;
    }

    public static class SaldoPorFechaEfectiva {
        public Long IdCuentaInversion;
        public String CodigoCuentaInversion;
        public Long IdCliente;
        public String DocumentoIdentidad;
        public String NombreCliente;
        public String FechaSaldo;
        public String Relacion;
        public String FechaUltimoMovimiento;
        public BigDecimal SaldoDisponible;
        public String IdMoneda;
    }

    public static class SaldoInicialFinal {
        public Long IdCuentaInversion;
        public BigDecimal SaldoEfectivoInicial;
        public BigDecimal SaldoEfectivoFinal;
    }
}
