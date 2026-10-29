package cl.bcs.cuentainversion.reportes;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.reportes.support.FakeColaboradores;
import cl.bcs.cuentainversion.reportes.support.GrabadorTablaPdf;
import cl.bcs.cuentainversion.reportes.support.PendienteSupport;
import cl.bcs.cuentainversion.service.ConsultaAntiguedadDeudasCuentaInversionService;
import cl.bcs.cuentainversion.service.ConsultaReporteAcreedoresService;
import cl.bcs.cuentainversion.service.ConsultaReporteAntiguedadBolsaService;
import cl.bcs.cuentainversion.service.EditarCobroComisionRegistrosService;
import cl.bcs.cuentainversion.service.GenerarArchivoSaldoCuentasService;
import cl.bcs.cuentainversion.service.IndiceRentabilidadAttributionAnalysisService;
import cl.bcs.cuentainversion.service.ProcesarCobroComisionPatrimonioService;
import cl.bcs.cuentainversion.support.VolcadoEquivalencia;

/** Fresh-input driver (verifier) for the reporting family: same wiring as the development tests, one JVM, zone America/Santiago. */
class FreshReportesTest {

    static List<String> casos() {
        return CasoReporte.todos().stream().filter(n -> n.startsWith("CTA-F-")).toList();
    }

    @ParameterizedTest
    @MethodSource("casos")
    void fresco(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        FakeColaboradores colab = FakeColaboradores.de(caso.caso());
        GrabadorTablaPdf pdf = new GrabadorTablaPdf();
        String clase = caso.caso().get("clase").getAsString();
        Resultado r = switch (clase) {
            case "IndiceRentabilidadAtributtionAnalysis" -> caso.ejecutar(new IndiceRentabilidadAttributionAnalysisService(caso.jdbc())::execute);
            case "ProcesarCobroComisionPatrimonio" -> caso.ejecutar(new ProcesarCobroComisionPatrimonioService(caso.dataSource(), colab)::execute);
            case "EditarCobroComisionRegistros" -> caso.ejecutar(new EditarCobroComisionRegistrosService(caso.dataSource())::execute);
            case "ConsultaAntiguedadDeudasCuentaInversion" -> caso.ejecutar(new ConsultaAntiguedadDeudasCuentaInversionService(caso.jdbc(), colab)::execute);
            case "ConsultaReporteAntiguedadBolsa" -> caso.ejecutar(new ConsultaReporteAntiguedadBolsaService(caso.jdbc(), colab)::execute);
            case "ConsultaReporteAcreedores" -> caso.ejecutar(new ConsultaReporteAcreedoresService(caso.jdbc(), colab)::execute);
            case "GenerarArchivoSaldoCuentas" -> caso.ejecutar(new GenerarArchivoSaldoCuentasService(caso.jdbc(), colab, pdf)::execute);
            default -> throw new AssertionError("clase sin conductor: " + clase);
        };
        List<String> fallos = new ArrayList<>();
        try {
            caso.assertEquivalente(r);
        } catch (Throwable t) {
            fallos.add("salida/tablas: " + t.getMessage());
        }
        try {
            PendienteSupport.assertLlamadas(colab, caso);
        } catch (Throwable t) {
            fallos.add("llamadas: " + t.getMessage());
        }
        try {
            PendienteSupport.assertArchivos(caso);
        } catch (Throwable t) {
            fallos.add("archivos: " + t.getMessage());
        }
        if (clase.equals("GenerarArchivoSaldoCuentas") && caso.esperado().has("pdfTextos")) {
            StringBuilder e = new StringBuilder();
            caso.esperado().getAsJsonArray("pdfTextos").forEach(x -> e.append(x.getAsString()).append('\n'));
            StringBuilder a = new StringBuilder();
            pdf.textos().forEach(x -> a.append(x).append('\n'));
            VolcadoEquivalencia.escribir("reportes", nombre + " pdfTextos", "canon", e.toString(), a.toString());
        }
        if (!fallos.isEmpty()) {
            throw new AssertionError(nombre + " differs: " + fallos);
        }
    }
}
