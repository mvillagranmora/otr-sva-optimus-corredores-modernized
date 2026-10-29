package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.port.out.ColaboradoresReportes;
import cl.bcs.cuentainversion.port.out.GeneradorPdf;
import cl.bcs.cuentainversion.port.out.GeneradorPdf.Alineacion;
import cl.bcs.cuentainversion.port.out.GeneradorPdf.CeldaPdf;
import cl.bcs.cuentainversion.port.out.GeneradorPdf.DocumentoPdf;
import cl.bcs.cuentainversion.port.out.GeneradorPdf.FuentePdf;
import cl.bcs.cuentainversion.port.out.GeneradorPdf.PaginaPdf;
import cl.bcs.cuentainversion.port.out.GeneradorPdf.TablaPdf;
import cl.bcs.cuentainversion.reportes.FormatoMoneda;
import cl.bcs.cuentainversion.reportes.JsonReportes;
import cl.bcs.cuentainversion.reportes.MensajeSalida;
import cl.bcs.cuentainversion.reportes.Mensajeria;
import cl.bcs.cuentainversion.reportes.SaldosCuentaInversion;
import cl.bcs.cuentainversion.reportes.SqlReportes;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Balance file of the investment accounts (legacy {@code GenerarArchivoSaldoCuentas}, CTA-B-131..136): asks ConsultarSaldo for the
 * balances, writes them as a {@code ;} separated CSV (UTF-8 with BOM, no quotes, amounts through the currency format rules with a
 * decimal comma) and a PDF rendering of the same table, and answers the path of the {@code .zip} that holds both as the
 * {@code .csv} path (CTA-B-131). {@code p_Encabezados} lists the columns to LEAVE OUT (CTA-B-132).
 *
 * <p>The PDF is described to the {@link GeneradorPdf} port as a table (header row in bold, numbers right aligned and re-formatted
 * as {@code #.##0,##}, dates centred). There is no production adapter yet (human decision: which PDF library).
 *
 * <p>LEGACY DEFECT kept (see README CTA-B-13): messages are built without parameters, so they read "<code> sin traduccion"; a
 * {@code ;} inside a name shifts the CSV columns because every line is split on it again.
 * LEGACY DEFECT kept (not listed in the README, found while porting): a failure while building the PDF or writing the zip is only
 * logged, the service still answers success with the path of a file that does not exist.
 */
@Service
public class GenerarArchivoSaldoCuentasService {

    private static final Logger log = LoggerFactory.getLogger(GenerarArchivoSaldoCuentasService.class);

    private static final String SQL_CONFIGURACION = SqlReportes.recurso("GenerarArchivoCargoAbono_SelectConfiguracion");
    private static final DateTimeFormatter MARCA_TIEMPO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String FECHA_VACIA = "0001-01-01";

    private final SqlReportes sql;
    private final ColaboradoresReportes colaboradores;
    private final GeneradorPdf pdf;

    @Autowired
    public GenerarArchivoSaldoCuentasService(NamedParameterJdbcTemplate jdbc, ColaboradoresReportes colaboradores, GeneradorPdf pdf) {
        this.sql = new SqlReportes(jdbc);
        this.colaboradores = colaboradores;
        this.pdf = pdf;
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        Salida out = generar(JsonReportes.leer(params, Entrada.class));
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(out.P_OUT_MENSAJES);
        return JsonReportes.escribir(out);
    }

    Salida generar(Entrada in) throws Exception {
        Salida out = new Salida();
        Mensajeria mensajes = new Mensajeria(sql, in.p_IdEmpresa);

        String rutaArchivo = "";
        String rutaCta = "";
        for (Configuracion c : sql.consultar(SQL_CONFIGURACION, Map.of("IdEmpresa", in.p_IdEmpresa), Configuracion.class)) {
            rutaArchivo = c.RutaArchivo != null ? c.RutaArchivo : "";
            rutaCta = c.RutaCta != null ? c.RutaCta : "";
        }
        if (rutaArchivo.isEmpty() || rutaCta.isEmpty()) {
            out.P_OUT_MENSAJES.add(mensajes.error("FALTA_CONFIGURACION_RUTA_ARCHIVO", null));
            return out;
        }

        String nombreArchivo = "SaldoCuentas_" + LocalDateTime.now().format(MARCA_TIEMPO) + ".csv";
        String rutaCompleta = rutaArchivo + nombreArchivo;
        rutaCta = rutaCta + nombreArchivo;

        Respuesta saldos = JsonReportes.leer(colaboradores.consultarSaldo(JsonReportes.escribir(solicitud(in))), Respuesta.class);
        if (saldos.P_OUT_ERRORESCONTADOR.compareTo(0L) != 0) {
            out.P_OUT_MENSAJES.add(mensajes.error("ERROR_EN_GENERACION_ARCHIVO", null));
            return out;
        }
        if (saldos.P_OUT_SALDOPORFECHAEFECTIVA.isEmpty()) {
            out.P_OUT_MENSAJES.add(mensajes.error("NO_EXISTEN_MOVIMIENTOS_PARA_GENERAR_ARCHIVO", null));
            return out;
        }

        List<Columna> columnas = columnas(mensajes);
        FormatoMoneda formato = new FormatoMoneda(sql);
        List<String> lineas = new ArrayList<>();
        List<String> excluidas = excluidas(in, mensajes);
        lineas.add(in.p_Encabezados.isEmpty() ? encabezadoCompleto(columnas) : encabezadoSinExcluidas(columnas, excluidas));
        for (SaldoArchivo saldo : saldos.P_OUT_SALDOPORFECHAEFECTIVA) {
            lineas.add(in.p_Encabezados.isEmpty()
                    ? lineaCompleta(saldo, formato)
                    : lineaSinExcluidas(saldo, columnas, excluidas, formato));
        }

        byte[] csv = csv(lineas);
        Map<String, Long> anchosExtra = new HashMap<>();
        for (Columna c : columnas) {
            anchosExtra.put(c.encabezado().toUpperCase(), c.anchoExtra());
        }
        // LEGACY DEFECT kept (not in the README): anything that fails from here on is swallowed, success is answered anyway.
        try {
            byte[] documentoPdf = pdf.generar(documentoPdf(lineas, anchosExtra));
            guardarZip(csv, documentoPdf, rutaCompleta, nombreArchivo);
        } catch (Exception e) {
            log.error("cannot build the zip of {}: {}", rutaCompleta, e.toString());
        }

        out.P_OUT_RUTAARCHIVO = rutaCta;
        return out;
    }

    // ---------------------------------------------------------------------------------------------- balances request

    private static SaldosCuentaInversion.Entrada solicitud(Entrada in) {
        SaldosCuentaInversion.Entrada e = new SaldosCuentaInversion.Entrada();
        e.p_IdEmpresa = in.p_IdEmpresa;
        e.p_IdUsuario = in.p_IdUsuario;
        e.p_IdModulo = in.p_IdModulo;
        e.p_UsaReglasNegocio = "S";
        e.p_IncluyeOtrosSaldos = in.p_IncluyeOtrosSaldos;
        e.p_SoloSaldoNegativo = in.p_SoloClientesSobregirados;
        e.p_SoloSaldoDisponible = in.p_SoloClientesConSaldoDisponible;
        e.p_SoloSaldoDisponibleNegativo = in.p_SoloClientesConSaldoDisponibleClientesSobregirados;
        e.p_FechaDesde = in.p_FechaDesde;
        e.p_FechaHasta = in.p_FechaHasta;
        e.p_IdEjecutivo = in.p_IdEjecutivo;
        e.p_IdMoneda = in.p_IdMoneda;
        e.p_IncluyeProductoApv = in.p_IncluyeProductoApv;
        e.p_IdCliente = null;
        e.p_IdPortafolio = null;
        e.p_IdProductoAgrupador = null;
        SaldosCuentaInversion.CuentaAConsultar c = new SaldosCuentaInversion.CuentaAConsultar();
        c.IdEjecutivo = in.p_IdEjecutivo;
        c.IdMoneda = in.p_IdMoneda;
        e.p_CuentaInversionAConsultar = new ArrayList<>(List.of(c));
        return e;
    }

    // ---------------------------------------------------------------------------------------------- columns

    /** A column of the file: header text (dictionary), the field it reads, how it is formatted and its extra width in the PDF. */
    private record Columna(String encabezado, String campo, String formato, long anchoExtra) {
    }

    private static final String MONTO = FormatoMoneda.MONTO;
    private static final String FECHA = "FECHA";

    /** The 21 columns in file order, de-duplicated by header text (the first one wins, like the legacy set). */
    private static List<Columna> columnas(Mensajeria m) {
        Object[][] definicion = {
                {"UI_FECHA_EFECTIVA", "FechaSaldo", "", 0L},
                {"UI_EJECUTIVO", "NombreEjecutivo", "", 40L},
                {"UI_SUCURSAL", "NombreSucursal", "", 40L},
                {"UI_CLIENTE", "NombreCliente", "", 80L},
                {"UI_TIPO_CLIENTE", "TipoCliente", "", 0L},
                {"UI_DOCUMENTO_IDENTIDAD", "DocumentoIdentidad", "", 0L},
                {"UI_PORTAFOLIO", "NombrePortafolio", "", 40L},
                {"UI_CUENTA_INVERSION", "CodigoMoneda", "", 0L},
                {"UI_DISPONIBLE", "SaldoDisponible", MONTO, 0L},
                {"UI_MONEY_MARKET_CONSOLIDADO", "MontoMoneyMarket", MONTO, 0L},
                {"UI_TOTAL_LIQUIDEZ", "TotalLiquidez", MONTO, 0L},
                {"UI_GARANTIA", "SaldoEnGarantia", MONTO, 0L},
                {"UI_INGRESOS_EGRESOS", "SaldoRetiro", MONTO, 0L},
                {"UI_A_PLAZO_SIMULTANEA", "SaldoAPlazoSimultanea", MONTO, 0L},
                {"UI_A_PLAZO_PRESTAMOS", "SaldoAPlazoVentaCorta", MONTO, 0L},
                {"UI_A_PLAZO_COMPROMISO", "SaldoAPlazoCompromiso", MONTO, 0L},
                {"UI_SALDO_RETENIDO", "SaldoRetenido", MONTO, 0L},
                {"UI_PRODUCTO", "Producto", "", 40L},
                {"UI_RELACION", "Relacion", "", 0L},
                {"UI_CODIGO_EJECUTIVO", "CodigoEjecutivo", "", 0L},
                {"UI_FECHA_ULTIMO_MOVIMIENTO", "FechaUltimoMovimiento", FECHA, 0L},
        };
        Map<String, Columna> unicas = new LinkedHashMap<>();
        for (Object[] d : definicion) {
            Columna c = new Columna(m.texto((String) d[0], Map.of()), (String) d[1], (String) d[2], (Long) d[3]);
            unicas.putIfAbsent(c.encabezado(), c);
        }
        return new ArrayList<>(unicas.values());
    }

    private static String normalizar(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return limpio.trim().toUpperCase();
    }

    /** Header of an excluded column as the caller may write it: accent-free upper case; two names are translated to the dictionary. */
    private static String nombreExcluido(String texto, Mensajeria m) {
        if (texto == null) {
            return null;
        }
        String limpio = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String clave = limpio.trim().toUpperCase();
        if (clave.equalsIgnoreCase("CODIGO PORTAFOLIO")) {
            return m.texto("UI_PORTAFOLIO", Map.of()).trim().toUpperCase();
        }
        if (clave.equalsIgnoreCase("MONEYMARKET")) {
            return m.texto("UI_MONEY_MARKET_CONSOLIDADO", Map.of()).trim().toUpperCase();
        }
        return clave;
    }

    private static List<String> excluidas(Entrada in, Mensajeria m) {
        List<String> r = new ArrayList<>();
        for (String e : in.p_Encabezados) {
            r.add(nombreExcluido(e, m));
        }
        return r;
    }

    /** Columns kept when some are excluded, keyed by the accent-free upper case header (a repeated key keeps the first position). */
    private static Map<String, Columna> conservadas(List<Columna> columnas, List<String> excluidas) {
        Map<String, Columna> porClave = new LinkedHashMap<>();
        for (Columna c : columnas) {
            porClave.put(normalizar(c.encabezado()), c);
        }
        porClave.keySet().removeIf(excluidas::contains);
        return porClave;
    }

    // ---------------------------------------------------------------------------------------------- lines

    private static String encabezadoCompleto(List<Columna> columnas) {
        StringBuilder sb = new StringBuilder();
        for (Columna c : columnas) {
            sb.append(c.encabezado()).append(';');
        }
        return sb.toString();
    }

    private static String encabezadoSinExcluidas(List<Columna> columnas, List<String> excluidas) {
        StringBuilder sb = new StringBuilder();
        for (Columna c : conservadas(columnas, excluidas).values()) {
            sb.append(c.encabezado()).append(';');
        }
        return sb.toString();
    }

    /** All columns; the amounts through the currency rule, decimal comma; the last column is not followed by a separator. */
    private static String lineaCompleta(SaldoArchivo s, FormatoMoneda formato) throws Exception {
        StringBuilder l = new StringBuilder();
        l.append(s.FechaSaldo).append(';').append(s.NombreEjecutivo).append(';').append(s.NombreSucursal).append(';')
                .append(s.NombreCliente).append(';').append(s.TipoCliente).append(';').append(s.DocumentoIdentidad).append(';')
                .append(s.NombrePortafolio).append(';').append(s.CodigoMoneda).append(';');
        s.SaldoDisponible = monto(formato, s.SaldoDisponible, s.CodigoMoneda);
        l.append(coma(s.SaldoDisponible)).append(';');
        s.MontoMoneyMarket = monto(formato, s.MontoMoneyMarket, s.CodigoMoneda);
        l.append(coma(s.MontoMoneyMarket)).append(';');
        s.TotalLiquidez = monto(formato, s.TotalLiquidez, s.CodigoMoneda);
        l.append(coma(s.TotalLiquidez)).append(';');
        s.SaldoEnGarantia = monto(formato, s.SaldoEnGarantia, s.CodigoMoneda);
        l.append(coma(s.SaldoEnGarantia)).append(';');
        s.SaldoRetiro = monto(formato, s.SaldoRetiro, s.CodigoMoneda);
        l.append(coma(s.SaldoRetiro)).append(';');
        s.SaldoAPlazoSimultanea = monto(formato, s.SaldoAPlazoSimultanea, s.CodigoMoneda);
        l.append(coma(s.SaldoAPlazoSimultanea)).append(';');
        s.SaldoAPlazoVentaCorta = monto(formato, s.SaldoAPlazoVentaCorta, s.CodigoMoneda);
        l.append(coma(s.SaldoAPlazoVentaCorta)).append(';');
        s.SaldoAPlazoCompromiso = monto(formato, s.SaldoAPlazoCompromiso, s.CodigoMoneda);
        l.append(coma(s.SaldoAPlazoCompromiso)).append(';');
        s.SaldoRetenido = monto(formato, s.SaldoRetenido, s.CodigoMoneda);
        l.append(coma(s.SaldoRetenido)).append(';');
        l.append(s.Producto).append(';').append(s.Relacion).append(';').append(s.CodigoEjecutivo).append(';');
        l.append(FECHA_VACIA.equals(s.FechaUltimoMovimiento) ? "" : s.FechaUltimoMovimiento);
        return l.toString();
    }

    /**
     * The kept columns, each read by reflection from the balance row. A failure inside a column (e.g. a currency without a format
     * rule) only drops that column's text.
     */
    private static String lineaSinExcluidas(SaldoArchivo s, List<Columna> columnas, List<String> excluidas, FormatoMoneda formato) {
        StringBuilder l = new StringBuilder();
        for (Columna c : conservadas(columnas, excluidas).values()) {
            try {
                Field campo = SaldoArchivo.class.getDeclaredField(c.campo());
                campo.setAccessible(true);
                Object valor = campo.get(s);
                if (MONTO.equals(c.formato())) {
                    BigDecimal v = formato.formatear(new BigDecimal(String.valueOf(valor)), MONTO, s.CodigoMoneda).valor();
                    l.append(coma(v)).append(';');
                } else if (FECHA.equals(c.formato())) {
                    l.append(String.valueOf(valor).equals(FECHA_VACIA) ? "" : String.valueOf(valor));
                } else {
                    l.append(valor).append(';');
                }
            } catch (Exception e) {
                log.warn("column {} skipped: {}", c.campo(), e.toString());
            }
        }
        return l.toString();
    }

    private static BigDecimal monto(FormatoMoneda formato, BigDecimal valor, String moneda) throws Exception {
        return formato.formatear(valor, MONTO, moneda).valor();
    }

    private static String coma(BigDecimal v) {
        return v.toString().replace(".", ",");
    }

    // ---------------------------------------------------------------------------------------------- files

    /** CSV bytes: UTF-8 BOM, every line split again on ';' (trailing empty fields vanish) and written without quotes, LF line ends. */
    private static byte[] csv(List<String> lineas) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        StringBuilder texto = new StringBuilder();
        for (String linea : lineas) {
            // LEGACY DEFECT kept (see README CTA-B-13): a ';' inside a value shifts the columns (the line is split and joined again).
            String[] campos = linea.split(";");
            for (int i = 0; i < campos.length; i++) {
                if (i > 0) {
                    texto.append(';');
                }
                texto.append(campos[i].replace("\"", "\"\""));
            }
            texto.append('\n');
        }
        bytes.write(texto.toString().getBytes(StandardCharsets.UTF_8));
        return bytes.toByteArray();
    }

    private static void guardarZip(byte[] csv, byte[] pdf, String rutaCsv, String nombreCsv) throws IOException {
        ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(zipBytes)) {
            zip.putNextEntry(new ZipEntry(nombreCsv));
            zip.write(csv);
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry(nombreCsv.replace(".csv", ".pdf")));
            zip.write(pdf);
            zip.closeEntry();
        }
        Files.write(Path.of(rutaCsv.replace(".csv", ".zip")), zipBytes.toByteArray());
    }

    // ---------------------------------------------------------------------------------------------- PDF description

    private static final int FUENTE = 6;
    private static final int PADDING = 2;

    /**
     * The table of the CSV on a landscape legal page. Column widths: the page width minus the extra widths of the named columns,
     * shared equally, plus each named column's extra. Numbers (not in the header) are re-formatted as {@code #.##0,##} and right
     * aligned; dd/MM/yyyy dates are centred.
     */
    private static DocumentoPdf documentoPdf(List<String> lineas, Map<String, Long> anchosExtra) {
        int ancho = 1008;
        int alto = 612;
        int margen = 10;
        BigDecimal anchoTabla = BigDecimal.valueOf(ancho - 2 * margen);
        String[] cabecera = lineas.get(0).split(";");
        int columnas = cabecera.length;
        BigDecimal extra = BigDecimal.ZERO;
        for (String nombre : cabecera) {
            Long e = anchosExtra.get(nombre.trim().toUpperCase());
            if (e != null) {
                extra = extra.add(BigDecimal.valueOf(e));
            }
        }
        BigDecimal base = anchoTabla.subtract(extra).divide(BigDecimal.valueOf(columnas), 10, RoundingMode.HALF_UP);
        float[] anchos = new float[columnas];
        for (int i = 0; i < columnas; i++) {
            Long e = anchosExtra.get(cabecera[i].trim().toUpperCase());
            anchos[i] = base.add(e == null ? BigDecimal.ZERO : BigDecimal.valueOf(e)).floatValue();
        }

        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator(',');
        simbolos.setGroupingSeparator('.');
        DecimalFormat formatoNumero = new DecimalFormat("#,##0.##", simbolos);

        List<CeldaPdf> celdas = new ArrayList<>();
        for (int fila = 0; fila < lineas.size(); fila++) {
            boolean esCabecera = fila == 0;
            String[] campos = lineas.get(fila).split(";");
            for (int c = 0; c < columnas; c++) {
                String original = c < campos.length ? campos[c].trim() : "";
                boolean esNumero = false;
                double valor = 0;
                try {
                    valor = Double.parseDouble(original.replace(".", "").replace(",", "."));
                    esNumero = true;
                } catch (Exception ignorada) {
                    // not a number
                }
                boolean esFecha = !esNumero && original.matches("\\d{2}/\\d{2}/\\d{4}");
                String texto = esNumero && !esCabecera ? formatoNumero.format(valor) : original;
                Alineacion horizontal = esNumero && !esCabecera ? Alineacion.DERECHA : esFecha ? Alineacion.CENTRO : Alineacion.IZQUIERDA;
                celdas.add(new CeldaPdf(texto, new FuentePdf(esCabecera, FUENTE), horizontal, null, 15, 1, 1, PADDING, false));
            }
        }
        return new DocumentoPdf(new PaginaPdf(ancho, alto, margen, margen, 20, 20), null, null, null, 0,
                new TablaPdf(anchos, 1, celdas), 0);
    }

    // ---------------------------------------------------------------------------------------------- JSON contract

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_FechaDesde;
        public String p_FechaHasta;
        public String p_SoloClientesSobregirados;
        public String p_SoloClientesConSaldoDisponible;
        public String p_SoloClientesConSaldoDisponibleClientesSobregirados;
        public String p_IncluyeProductoApv;
        public String p_IncluyeOtrosSaldos;
        public Long p_IdEjecutivo;
        public String p_IdMoneda;
        public List<String> p_Encabezados = new ArrayList<>();
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
        public String P_OUT_RUTAARCHIVO;
    }

    public static class Configuracion {
        public String RutaBase;
        public String RutaCta;
        public String RutaArchivo;
    }

    /** Row of {@code P_OUT_SALDOPORFECHAEFECTIVA} as the file uses it. */
    public static class SaldoArchivo {
        public String FechaSaldo;
        public String NombreEjecutivo;
        public String CodigoEjecutivo;
        public String NombreSucursal;
        public String NombreCliente;
        public String TipoCliente = "";
        public String DocumentoIdentidad;
        public String NombrePortafolio;
        public String CodigoMoneda;
        public BigDecimal SaldoDisponible;
        public BigDecimal MontoMoneyMarket;
        public BigDecimal TotalLiquidez;
        public BigDecimal SaldoEnGarantia;
        public BigDecimal SaldoRetiro;
        public BigDecimal SaldoAPlazoSimultanea;
        public BigDecimal SaldoAPlazoVentaCorta;
        public BigDecimal SaldoAPlazoCompromiso;
        public BigDecimal SaldoRetenido = BigDecimal.ZERO;
        public String Producto;
        public String Relacion;
        public String FechaUltimoMovimiento = FECHA_VACIA;
    }

    public static class Respuesta {
        public List<SaldoArchivo> P_OUT_SALDOPORFECHAEFECTIVA = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
    }
}
