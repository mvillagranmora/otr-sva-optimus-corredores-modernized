package cl.bcs.normativa.generacion;

import java.io.PrintStream;
import java.util.Date;

import cl.bcs.normativa.esquema.Documento;
import cl.bcs.normativa.procesos.Reporte;

/**
 * Implementa reporte de la generaci&oacute;n.
 *
 * @author fpino@email.com
 *
 */
public class ReporteGeneracion extends Reporte {

    /**
     * Constructor.
     *
     * @param out
     *            La salida.
     */
    public ReporteGeneracion(final PrintStream out) {
        super(out);
    }

    /**
     * Imprime el encabezado del reporte.
     *
     * @param contexto
     *            El contexto de generaci&oacute;n.
     */
    public void printEncabezado(final ContextoGeneracion contexto) {

        final Documento documento = contexto.getDocumento();

        println("PARAMETROS DE GENERACION");
        println("------------------------");

        println("Fecha/Hora          : " + format(new Date()));

        println("Nombre Empresa      : " + contexto.getEmpresa().getNombre());
        println("Codigo Usuario      : " + contexto.getUsuario().getCodigo());
        println("Secuencia Documento : " + documento.getId());
        println("Folio Declaracion   : " + contexto.getFolioDeclaracion());
        println("Tipo Declaracion    : " + contexto.getTipoDeclaracion());
        println("Modo Presentacion   : " + contexto.getModoPresentacion());

        println("Esquema             : " + documento.getNombreEsquema());
        println("Version             : " + documento.getVersionEsquema());
        println("Fecha Desde         : " + format(contexto.getFiltroFechas().getFechaDesde()));
        println("Fecha Hasta         : " + format(contexto.getFiltroFechas().getFechaHasta()));

        println();
    }

}
