package cl.bcs.cuentainversion.service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.cuentainversion.port.out.DiccionarioMensajes;
import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;

/**
 * Exports the cargo/abono movements of a date range to a csv under the configured file path (CTA-A-125..137; legacy
 * GenerarArchivoCargoAbono). The file name is {@code MovimientosCargoAbono_yyyyMMdd_HHmmss.csv}; it is written to
 * {@code PLA.RUTABASEARCHIVOS + RUTAARCHIVOSCTA} and the answer carries the path relative to the base ({@code RUTAARCHIVOSCTA + name}).
 * Column titles come from the dictionary; each data line is built by the SQL (a filter value of "0" means "all").
 */
@Service
public class GenerarArchivoCargoAbonoService {

    private static final Logger log = LoggerFactory.getLogger(GenerarArchivoCargoAbonoService.class);

    private static final String SQL_CONFIGURACION = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/GenerarArchivoCargoAbono_SelectConfiguracion.sql");
    private static final String SQL_CARGOS_ABONOS = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/GenerarArchivoCargoAbono_SelectCargosAbonos.sql");
    private static final DateTimeFormatter MARCA_DE_TIEMPO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /** Dictionary codes of the csv header, in column order. */
    private static final List<String> ENCABEZADO = List.of(
            "UI_NOMBRE_CLIENTE", "UI_DOCUMENTO_IDENTIDAD", "UI_PORTAFOLIO", "UI_FECHA_MOVIMIENTO", "UI_FECHA_EFECTIVA", "UI_CONCEPTO",
            "UI_ES_TRANSFERENCIA", "UI_MONEDA", "UI_CARGO", "UI_ABONO", "UI_TIPO_COMPROBANTE", "UI_SECUENCIA_COMPROBANTE",
            "UI_FOLIO_COMPROBANTE", "UI_FECHA_COMPROBANTE", "UI_TIPO_PRODUCTO", "UI_TIPO_OPERACION", "UI_FECHA_OPERACION",
            "UI_FECHA_VENCIMIENTO", "UI_SECUENCIA_OPERACION", "UI_MONTO_OPERACION", "UI_SECUENCIA_MOVIMIENTO",
            "UI_SECUENCIA_TRANSFERENCIA", "UI_NOMBRE_CLIENTE_RELACIONADO", "UI_PORTAFOLIO_RELACIONADO", "UI_EMPLEADO_CREADOR",
            "UI_FECHA_CERTIFICACION", "UI_EMPLEADO_CERTIFICADOR", "UI_DESCRIPCION", "UI_ESTADO", "UI_MOTIVO_ANULACION",
            "UI_AUTORIZACION", "UI_ESTADO_AUTORIZACION", "UI_PRODUCTO", "UI_RELACION");

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_FechaDesde;
        public String p_FechaHasta;
        public Long p_IdEjecutivo;
        public String p_IdMoneda;
        public String p_IdTipoMovimiento;
        public String p_IdConcepto;
        public String p_IdEstado;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
        public String P_OUT_RUTAARCHIVO;
        public Long P_OUT_CANTIDADREGISTROS = 0L;
    }

    public static class Linea {
        public String Linea;
    }

    public static class Configuracion {
        public String RutaBase;
        public String RutaCta;
        public String RutaArchivo;
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final DiccionarioMensajes diccionario;

    public GenerarArchivoCargoAbonoService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario) {
        this.jdbc = jdbc;
        this.diccionario = diccionario;
    }

    @Transactional(readOnly = true)
    public JsonObject execute(JsonObject params) throws IOException {
        Gson gson = new Gson();
        Salida salida = generar(gson.fromJson(params, Entrada.class));
        salida.P_OUT_ERRORESCONTADOR = errores(salida);
        return gson.toJsonTree(salida).getAsJsonObject();
    }

    Salida generar(Entrada in) throws IOException {
        Salida out = new Salida();
        String rutaArchivo = "";
        String rutaCta = "";
        for (Configuracion c : jdbc.query(SQL_CONFIGURACION, new MapSqlParameterSource("IdEmpresa", in.p_IdEmpresa),
                CtaJavaCallInfra.fieldMapper(Configuracion.class))) {
            rutaArchivo = c.RutaArchivo != null ? c.RutaArchivo : "";
            rutaCta = c.RutaCta != null ? c.RutaCta : "";
        }
        if (rutaArchivo.isEmpty() || rutaCta.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.ERROR, "FALTA_CONFIGURACION_RUTA_ARCHIVO",
                    diccionario.traducir(in.p_IdEmpresa, "FALTA_CONFIGURACION_RUTA_ARCHIVO", null)));
            out.P_OUT_ERRORESCONTADOR = errores(out);
            return out;
        }
        String nombre = "MovimientosCargoAbono_" + LocalDateTime.now().format(MARCA_DE_TIEMPO) + ".csv";

        // LEGACY DEFECT kept (see README CTA-A-135): a null filter value (e.g. p_IdMoneda) matches no rows instead of meaning "all"
        MapSqlParameterSource filtros = new MapSqlParameterSource()
                .addValue("FechaDesde", in.p_FechaDesde)
                .addValue("FechaHasta", in.p_FechaHasta)
                .addValue("IdEjecutivo", in.p_IdEjecutivo)
                .addValue("IdMoneda", in.p_IdMoneda)
                .addValue("IdTipoMovimiento", in.p_IdTipoMovimiento)
                .addValue("IdConcepto", in.p_IdConcepto)
                .addValue("IdEstado", in.p_IdEstado);
        List<Linea> lineas = jdbc.query(SQL_CARGOS_ABONOS, filtros, CtaJavaCallInfra.fieldMapper(Linea.class));
        if (!lineas.isEmpty()) {
            escribir(rutaArchivo + nombre, in.p_IdEmpresa, lineas);
            out.P_OUT_RUTAARCHIVO = rutaCta + nombre;
        }
        out.P_OUT_CANTIDADREGISTROS = (long) lineas.size();
        out.P_OUT_ERRORESCONTADOR = errores(out);
        log.info("GenerarArchivoCargoAbono: {} registros", lineas.size());
        return out;
    }

    private void escribir(String ruta, Long idEmpresa, List<Linea> lineas) throws IOException {
        FileWriter archivo = new FileWriter(ruta);
        BufferedWriter escritor = new BufferedWriter(archivo);
        try {
            StringBuilder encabezado = new StringBuilder();
            for (String codigo : ENCABEZADO) {
                encabezado.append(diccionario.traducir(idEmpresa, codigo, new HashMap<>())).append(';');
            }
            escritor.write(encabezado.toString());
            escritor.newLine();
            for (Linea l : lineas) {
                // LEGACY DEFECT kept (see README CTA-A-136): a NULL date in the view makes the whole line NULL and writing it throws
                // NullPointerException; the buffered header is then never flushed, the empty file stays
                escritor.write(l.Linea);
                escritor.newLine();
            }
            escritor.flush();
            escritor.close();
        } catch (IOException | RuntimeException e) {
            archivo.close();
            throw e;
        }
    }

    private static long errores(Salida s) {
        return s.P_OUT_MENSAJES.stream().filter(m -> MensajeSalida.ERROR.equals(m.tipo)).count();
    }
}
