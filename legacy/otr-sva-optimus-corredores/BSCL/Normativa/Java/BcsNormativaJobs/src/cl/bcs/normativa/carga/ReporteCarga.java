package cl.bcs.normativa.carga;

import java.io.PrintStream;
import java.util.Date;

import cl.bcs.normativa.esquema.Documento;
import cl.bcs.normativa.esquema.ResumenCarga;
import cl.bcs.normativa.procesos.Reporte;

/**
 * Implementa reporte de resultado de carga.
 *
 * @author fpino@email.com
 *
 */
final class ReporteCarga extends Reporte {

    /**
     * Constructor.
     *
     * @param out
     *            La salida.
     */
    public ReporteCarga(final PrintStream out) {
        super(out);
    }

    /**
     * Imprime el encabezado del reporte.
     *
     * @param contexto
     *            El contexto de carga.
     */
    public void printEncabezado(final ContextoCarga contexto) {

        final Documento documento = contexto.getDocumento();

        println("PARAMETROS DE CARGA");
        println("-------------------");

        println("Fecha/Hora          : " + format(new Date()));

        println("Nombre Empresa      : " + contexto.getEmpresa().getNombre());
        println("Codigo Usuario      : " + contexto.getUsuario().getCodigo());
        println("Secuencia Documento : " + documento.getId());

        println("Tipo Carga          : " + contexto.getOpciones().getTipoCarga());
        println("Esquema             : " + documento.getNombreEsquema());
        println("Version             : " + documento.getVersionEsquema());
        println("Fecha Desde         : " + format(contexto.getFiltroFechas().getFechaDesde()));
        println("Fecha Hasta         : " + format(contexto.getFiltroFechas().getFechaHasta()));

        println("Reemplazar          : " + contexto.getOpciones().isReemplazar());
        println("Grabar              : " + contexto.getOpciones().isGrabar());
        println("Registros/Lote      : " + contexto.getOpciones().getBatchSize());

        if (contexto.getOpciones().getTipoCarga().equals(TipoCarga.EXTERNA)) {
            println("Tipo Archivo        : " + contexto.getOpciones().getTipoArchivo());
            println("Ruta Archivo        : " + contexto.getRutaArchivo());
            println("Separador Listas    : " + contexto.getOpciones().getSeparadorListas());
            println("Simbolo Decimal     : " + contexto.getOpciones().getSimboloDecimal());
        }

        println();
    }

    /**
     * Imprime el resumen del reporte.
     *
     * @param resumen
     *            El resumen.
     */
    public void printResumen(final ResumenCarga resumen) {
        println();
        println("RESUMEN DE CARGA");
        println("----------------");

        println("Registros Leidos    : " + resumen.getTotalRegistrosLeidos());
        println("Registros Cargados  : " + resumen.getTotalRegistrosCargados());
        println("Registros Erroneos  : " + resumen.getTotalRegistrosErroneos());
    }

}
