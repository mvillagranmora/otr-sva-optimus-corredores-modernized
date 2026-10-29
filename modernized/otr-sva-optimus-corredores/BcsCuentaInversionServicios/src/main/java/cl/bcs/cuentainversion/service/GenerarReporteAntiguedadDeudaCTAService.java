package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.port.out.ColaboradoresReportes;
import cl.bcs.cuentainversion.reportes.ConfiguracionSistema;
import cl.bcs.cuentainversion.reportes.JsonReportes;
import cl.bcs.cuentainversion.reportes.Mensajeria;
import cl.bcs.cuentainversion.reportes.ReporteXlsxCuentas;
import cl.bcs.cuentainversion.reportes.ReporteXlsxCuentas.Consulta;
import cl.bcs.cuentainversion.reportes.SqlReportes;
import cl.bcs.cuentainversion.service.ConsultaAntiguedadDeudasCuentaInversionService.Fila;
import com.google.gson.JsonObject;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

/**
 * "Reporte de antiguedad de deuda CTA" as an .xlsx workbook (legacy {@code GenerarReporteAntiguedadDeudaCTA}, CTA-B-145..150), taken
 * from {@link ConsultaAntiguedadDeudasCuentaInversionService}. The debt date column is blank for a row without debt. See
 * {@link ReporteXlsxCuentas} for the shared flow.
 */
@Service
public class GenerarReporteAntiguedadDeudaCTAService {

    private static final Logger log = LoggerFactory.getLogger(GenerarReporteAntiguedadDeudaCTAService.class);

    private static final ReporteXlsxCuentas.Recursos RECURSOS = new ReporteXlsxCuentas.Recursos(
            "GenerarReporteAntiguedadDeudaCTA_SelectConfiguracionArchivo",
            "GenerarReporteAntiguedadDeudaCTA_SelectCodigoCorredora",
            "GenerarReporteAntiguedadDeudaCTA_SelectArchivoFormatoConfiguracion",
            true);

    private final SqlReportes sql;
    private final ConsultaAntiguedadDeudasCuentaInversionService consulta;

    public GenerarReporteAntiguedadDeudaCTAService(NamedParameterJdbcTemplate jdbc, ColaboradoresReportes colaboradores) {
        this.sql = new SqlReportes(jdbc);
        this.consulta = new ConsultaAntiguedadDeudasCuentaInversionService(jdbc, colaboradores);
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        Entrada in = JsonReportes.leer(params, Entrada.class);
        // the legacy reads the system date first (and fails when the parameter is missing) although it does not use it
        new ConfiguracionSistema(sql).cadena(in.p_IdEmpresa, ConfiguracionSistema.PLATAFORMA, "PLA.FechaSistema");
        ReporteXlsxCuentas.Salida out = new ReporteXlsxCuentas(sql, new Mensajeria(sql, in.p_IdEmpresa), RECURSOS)
                .generar(in.p_IdEmpresa, in.p_FechaSolicitud, in.p_Consolidar, in.p_NombreInternoArchivo,
                        () -> consultar(in),
                        (fila, posiciones, dato, secuencia) -> escribir(fila, posiciones, dato, secuencia, in.p_Consolidar));
        out.P_OUT_ERRORESCONTADOR = ReporteXlsxCuentas.contarErrores(out);
        return JsonReportes.escribir(out);
    }

    private Consulta<Fila> consultar(Entrada in) {
        ConsultaAntiguedadDeudasCuentaInversionService.Entrada e = new ConsultaAntiguedadDeudasCuentaInversionService.Entrada();
        e.p_IdEmpresa = in.p_IdEmpresa;
        e.p_IdUsuario = in.p_IdUsuario;
        e.p_IdModulo = in.p_IdModulo;
        e.p_IdEjecutivo = 0L;
        e.p_FechaConsulta = in.p_FechaSolicitud;
        e.p_IdMoneda = in.p_IdMoneda;
        e.p_SeparaIntermediacion = in.p_SeparaIntermediacion;
        e.p_AgrupaPortafolios = in.p_AgrupaPortafolios;
        e.p_Consolidar = in.p_Consolidar;
        try {
            ConsultaAntiguedadDeudasCuentaInversionService.Salida s = consulta.consultar(e);
            return new Consulta<>(s.P_OUT_TABLA, s.P_OUT_ERRORESCONTADOR, s.P_OUT_MENSAJES);
        } catch (Exception ex) {
            log.error("consulta de antiguedad de deuda fallida: {}", ex.toString());
            return Consulta.vacia();
        }
    }

    private static void escribir(Row fila, Map<String, Integer> pos, Fila d, long secuencia, String consolidar) {
        fila.createCell(pos.get("SECUENCIA")).setCellValue(secuencia);
        fila.createCell(pos.get("RUTCLIENTE")).setCellValue(d.DocumentoIndentidad);
        fila.createCell(pos.get("NOMBRECLIENTE")).setCellValue(d.NombreCliente);
        if (consolidar.equalsIgnoreCase("N")) {
            fila.createCell(pos.get("CODIGOPORTAFOLIO")).setCellValue(d.CodigoPortafolio);
        }
        fila.createCell(pos.get("MONTOTOTALPORCOBRAR")).setCellValue(d.MontoPorCobrarTotal.toPlainString());
        fila.createCell(pos.get("DEUDASVIGENTES")).setCellValue(d.TramoA.toPlainString());
        fila.createCell(pos.get("DEUDASVENCIDASDE3A10DIAS")).setCellValue(d.TramoB.toPlainString());
        fila.createCell(pos.get("DEUDASVENCIDASDE11A30DIAS")).setCellValue(d.TramoC.toPlainString());
        fila.createCell(pos.get("DEUDASVENCIDAMASDE30DIAS")).setCellValue(d.TramoD.toPlainString());
        fila.createCell(pos.get("OBSERVACIONES")).setCellValue(d.Observacion);
        fila.createCell(pos.get("RELACIONADO")).setCellValue(d.Relacionado);
        fila.createCell(pos.get("FECHADEUDA")).setCellValue(d.MontoPorCobrarTotal.compareTo(BigDecimal.ZERO) == 0 ? null : d.FechaDeuda);
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_FechaSolicitud;
        public String p_IdMoneda;
        public String p_SeparaIntermediacion;
        public String p_AgrupaPortafolios;
        public String p_Consolidar;
        public String p_NombreInternoArchivo;
    }
}
