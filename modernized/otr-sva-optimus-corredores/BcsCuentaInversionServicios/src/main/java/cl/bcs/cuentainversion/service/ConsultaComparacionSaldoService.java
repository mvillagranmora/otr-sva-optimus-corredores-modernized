package cl.bcs.cuentainversion.service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.cuentainversion.port.out.DiccionarioMensajes;
import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;

/**
 * Compares, per investment account, the balance of its last movement against the balance computed by the account itself and
 * writes the differences to a csv (CTA-A-138..145; legacy ConsultaComparacionSaldo). The csv goes to
 * {@code PLA.RUTABASEARCHIVOS + RUTAARCHIVOSCTA} and is named {@code DiferenciaSaldos_yyyyMMdd_HHmmss.csv}; the answer carries the path
 * relative to the base. The previous balances are staged in {@code Saldo_Cuenta_Proceso}, which is emptied at the end.
 */
@Service
public class ConsultaComparacionSaldoService {

    private static final String SQL = "cl/bcs/cuentainversion/sql/ConsultaComparacionSaldo_";
    private static final String SQL_CONFIGURACION = CtaJavaCallInfra.loadSql(SQL + "SelectConfiguracion.sql");
    private static final String SQL_SALDOS_CUENTAS = CtaJavaCallInfra.loadSql(SQL + "SelectSaldosCuentas.sql");
    private static final String SQL_SALDOS_MOVIMIENTOS = CtaJavaCallInfra.loadSql(SQL + "SelectSaldosMovimientos.sql");
    private static final String SQL_INSERT_SALDO_CUENTA = CtaJavaCallInfra.loadSql(SQL + "InsertSaldoCuentaProceso.sql");
    private static final String SQL_DELETE_SALDO_CUENTA = "DELETE FROM \"BSCL.CuentaInversion.BD::CTA.Saldo_Cuenta_Proceso\" WHERE 1=1";
    private static final DateTimeFormatter MARCA_DE_TIEMPO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static final List<String> ENCABEZADO = List.of("UI_FECHA_EFECTIVA_MOVIMIENTO", "UI_CUENTA_INVERSION", "UI_DOCUMENTO_IDENTIDAD",
            "UI_NOMBRE_CLIENTE", "UI_PORTAFOLIO", "UI_PROPOSITO_PORTAFOLIO", "UI_SALDO_MOVIMIENTO", "UI_SALDO_CUENTA_INVERSION");

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_FechaDesde;
        public String p_FechaHasta;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public String P_OUT_RUTAARCHIVO;
    }

    public static class SaldoCuenta {
        public Long IdCuentaInversion;
        public BigDecimal SaldoAnterior;
        public String FechaAnterior;
    }

    public static class SaldoMovimiento {
        public Long IdCuentaInversion;
        public String IdMoneda;
        public Date FechaEfectivaMovimiento;
        public String CodigoCuentaInversion;
        public String NombreCuentaInversion;
        public String CodigoNombreMoneda;
        public String DocumentoIdentidad;
        public String NombreCliente;
        public String CodigoPortafolio;
        public Long IdCargoAbonoMovimiento;
        public String Proposito;
        public BigDecimal Saldo;
        public BigDecimal SaldoMovimiento;
        public BigDecimal SaldoCuentaInversion;
    }

    public static class Configuracion {
        public String RutaBase;
        public String RutaCta;
        public String RutaArchivo;
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final FormatoMoneda formatoMoneda;
    private final DiccionarioMensajes diccionario;

    public ConsultaComparacionSaldoService(NamedParameterJdbcTemplate jdbc, FormatoMoneda formatoMoneda, DiccionarioMensajes diccionario) {
        this.jdbc = jdbc;
        this.formatoMoneda = formatoMoneda;
        this.diccionario = diccionario;
    }

    @Transactional
    public JsonObject execute(JsonObject params) throws IOException {
        Gson gson = new Gson();
        Salida salida = comparar(gson.fromJson(params, Entrada.class));
        salida.P_OUT_ERRORESCONTADOR = errores(salida);
        return gson.toJsonTree(salida).getAsJsonObject();
    }

    Salida comparar(Entrada in) throws IOException {
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
        String nombre = "DiferenciaSaldos_" + LocalDateTime.now().format(MARCA_DE_TIEMPO) + ".csv";

        // the file is created (with its header) before the balances are read
        BufferedWriter escritor = new BufferedWriter(new FileWriter(rutaArchivo + nombre));
        StringBuilder encabezado = new StringBuilder();
        for (String codigo : ENCABEZADO) {
            encabezado.append(diccionario.traducir(in.p_IdEmpresa, codigo, new HashMap<>())).append(';');
        }
        escritor.write(encabezado.toString());
        escritor.newLine();

        List<SaldoCuenta> saldos = jdbc.query(SQL_SALDOS_CUENTAS, new MapSqlParameterSource()
                .addValue("FechaOperacionDesde", in.p_FechaDesde)
                .addValue("FechaOperacionHasta", in.p_FechaHasta), CtaJavaCallInfra.fieldMapper(SaldoCuenta.class));
        jdbc.batchUpdate(SQL_INSERT_SALDO_CUENTA, saldos.stream()
                .map(s -> (SqlParameterSource) new MapSqlParameterSource()
                        .addValue("IdCuentaInversion", s.IdCuentaInversion)
                        .addValue("SaldoAnterior", CtaJavaCallInfra.escala16(s.SaldoAnterior))
                        .addValue("FechaAnterior", s.FechaAnterior))
                .toArray(SqlParameterSource[]::new));
        List<SaldoMovimiento> diferencias = jdbc.query(SQL_SALDOS_MOVIMIENTOS,
                new MapSqlParameterSource("FechaOperacionDesde", in.p_FechaDesde), CtaJavaCallInfra.fieldMapper(SaldoMovimiento.class));
        if (diferencias.isEmpty()) {
            // LEGACY DEFECT kept (see README CTA-A-140): no differences is reported as an ERROR although the header-only file is generated
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.ERROR, "NO_SE_ENCONTRARON_DIFERENCIAS",
                    diccionario.traducir(in.p_IdEmpresa, "NO_SE_ENCONTRARON_DIFERENCIAS", null)));
        }
        for (SaldoMovimiento d : diferencias) {
            escritor.write(d.FechaEfectivaMovimiento + ";" + d.CodigoNombreMoneda + ";" + d.DocumentoIdentidad + ";" + d.NombreCliente + ";"
                    + d.CodigoPortafolio + ";" + d.Proposito + ";"
                    + formatoMoneda.formatear(d.SaldoMovimiento, FormatoMoneda.Tipo.MONTO, d.IdMoneda).valor() + ";"
                    + formatoMoneda.formatear(d.SaldoCuentaInversion, FormatoMoneda.Tipo.MONTO, d.IdMoneda).valor());
            escritor.newLine();
        }
        escritor.flush();
        escritor.close();
        jdbc.getJdbcTemplate().execute(SQL_DELETE_SALDO_CUENTA);
        out.P_OUT_RUTAARCHIVO = rutaCta + nombre;
        out.P_OUT_ERRORESCONTADOR = errores(out);
        return out;
    }

    private static long errores(Salida s) {
        return s.P_OUT_MENSAJES.stream().filter(m -> MensajeSalida.ERROR.equals(m.tipo)).count();
    }
}
