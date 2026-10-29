package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.port.out.ColaboradoresReportes;
import cl.bcs.cuentainversion.reportes.JsonReportes;
import cl.bcs.cuentainversion.reportes.Mensajeria;
import cl.bcs.cuentainversion.reportes.ReporteXlsxCuentas;
import cl.bcs.cuentainversion.reportes.ReporteXlsxCuentas.Consulta;
import cl.bcs.cuentainversion.reportes.SqlReportes;
import cl.bcs.cuentainversion.service.ConsultaReporteAcreedoresService.Fila;
import com.google.gson.JsonObject;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * "Reporte de acreedores" as an .xlsx workbook (legacy {@code GenerarReporteAcreedores}, CTA-B-137..144): what the house owes its
 * clients per age tranche, taken from {@link ConsultaReporteAcreedoresService}. See {@link ReporteXlsxCuentas} for the shared flow.
 *
 * <p>LEGACY DEFECT kept (see README CTA-B-10): the sheet is named "...cuentas_por_cobrar" although the report is about payables.
 */
@Service
public class GenerarReporteAcreedoresService {

    private static final Logger log = LoggerFactory.getLogger(GenerarReporteAcreedoresService.class);

    private static final ReporteXlsxCuentas.Recursos RECURSOS = new ReporteXlsxCuentas.Recursos(
            "GenerarReporteAcreedores_SelectConfiguracionArchivo",
            "GenerarReporteAcreedores_SelectCodigoCorredora",
            "GenerarReporteAcreedores_SelectArchivoFormatoConfiguracion",
            false);

    private final SqlReportes sql;
    private final ConsultaReporteAcreedoresService consulta;

    public GenerarReporteAcreedoresService(NamedParameterJdbcTemplate jdbc, ColaboradoresReportes colaboradores) {
        this.sql = new SqlReportes(jdbc);
        this.consulta = new ConsultaReporteAcreedoresService(jdbc, colaboradores);
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        Entrada in = JsonReportes.leer(params, Entrada.class);
        ReporteXlsxCuentas.Salida out = new ReporteXlsxCuentas(sql, new Mensajeria(sql, in.p_IdEmpresa), RECURSOS)
                .generar(in.p_IdEmpresa, in.p_FechaSolicitud, in.p_Consolidar, in.p_NombreInternoArchivo,
                        () -> consultar(in),
                        (fila, posiciones, dato, secuencia) -> escribir(fila, posiciones, dato, secuencia, in.p_Consolidar));
        out.P_OUT_ERRORESCONTADOR = ReporteXlsxCuentas.contarErrores(out);
        return JsonReportes.escribir(out);
    }

    private Consulta<Fila> consultar(Entrada in) {
        ConsultaReporteAcreedoresService.Entrada e = new ConsultaReporteAcreedoresService.Entrada();
        e.p_IdEmpresa = in.p_IdEmpresa;
        e.p_IdUsuario = in.p_IdUsuario;
        e.p_IdModulo = in.p_IdModulo;
        e.p_IdEjecutivo = 0L;
        e.p_FechaConsulta = in.p_FechaSolicitud;
        e.p_IdMoneda = in.p_IdMoneda;
        e.p_Consolidar = in.p_Consolidar;
        try {
            ConsultaReporteAcreedoresService.Salida s = consulta.consultar(e);
            return new Consulta<>(s.P_OUT_TABLA, s.P_OUT_ERRORESCONTADOR, s.P_OUT_MENSAJES);
        } catch (Exception ex) {
            log.error("consulta de acreedores fallida: {}", ex.toString());
            return Consulta.vacia();
        }
    }

    private static void escribir(Row fila, Map<String, Integer> pos, Fila d, long secuencia, String consolidar) {
        fila.createCell(pos.get("RACREEDORES_SECUENCIA")).setCellValue(secuencia);
        fila.createCell(pos.get("RACREEDORES_RUTCLIENTE")).setCellValue(d.DocumentoIndentidad);
        fila.createCell(pos.get("RACREEDORES_NOMBRECLIENTE")).setCellValue(d.NombreCliente);
        if ("N".equalsIgnoreCase(consolidar)) {
            fila.createCell(pos.get("RACREEDORES_CODIGOPORTAFOLIO")).setCellValue(d.CodigoPortafolio);
        }
        fila.createCell(pos.get("RACREEDORES_MONTOTOTALPORPAGAR")).setCellValue(d.MontoPorPagarTotal.toPlainString());
        fila.createCell(pos.get("RACREEDORES_HABERESVIGENTES")).setCellValue(d.TramoA.toPlainString());
        fila.createCell(pos.get("RACREEDORES_HABERESDE3A10DIAS")).setCellValue(d.TramoB.toPlainString());
        fila.createCell(pos.get("RACREEDORES_HABERESDE11A30DIAS")).setCellValue(d.TramoC.toPlainString());
        fila.createCell(pos.get("RACREEDORES_HABERESMASDE30DIAS")).setCellValue(d.TramoD.toPlainString());
        fila.createCell(pos.get("RACREEDORES_OBSERVACIONES")).setCellValue(d.Observacion);
        fila.createCell(pos.get("RACREEDORES_CLASIFICACIONCONTABLE")).setCellValue(d.ClasificacionContable);
        fila.createCell(pos.get("RACREEDORES_RELACIONADO")).setCellValue(d.Relacionado);
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_FechaSolicitud;
        public String p_IdMoneda;
        public String p_Consolidar;
        public String p_NombreInternoArchivo;
    }
}
