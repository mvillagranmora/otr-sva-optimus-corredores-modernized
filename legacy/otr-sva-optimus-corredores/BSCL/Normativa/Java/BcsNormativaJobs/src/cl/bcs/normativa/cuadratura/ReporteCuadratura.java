package cl.bcs.normativa.cuadratura;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.esquema.Campo;
import cl.bcs.normativa.esquema.Documento;
import cl.bcs.normativa.esquema.ResumenCuadratura;
import cl.bcs.normativa.procesos.Reporte;

/**
 * Implementa reporte de cuadratura.
 *
 * @author fpino@email.com
 *
 */
final class ReporteCuadratura extends Reporte {

    private static final DateFormat FORMATO_MES = new SimpleDateFormat("MMMM yyyy",
            new Locale("es"));

    /**
     * Constructor.
     *
     * @param out
     *            La salida.
     */
    public ReporteCuadratura(final PrintStream out) {
        super(out);
    }

    private String format(Registro registro) {
        String str = registro.toString();
        for (final Campo campo : registro.getEsquema().getCampos()) {
            str = str.replaceAll(campo.getNombre(), "'" + campo.getTitulo() + "'");
        }

        return str;
    }

    /**
     * Imprime el resultado de la cuadratura.
     *
     * @param resultado
     *            El resultado de la cuadratura.
     */
    public void print(final ResultadoCuadratura resultado) {
        final Map<Date, Map<TipoDiferencia, ResumenCuadratura>> mapMeses = resultado.getMapMeses();
        for (final Date anoMes : mapMeses.keySet()) {
            synchronized (FORMATO_MES) {
                println(FORMATO_MES.format(anoMes));
            }

            final Map<TipoDiferencia, ResumenCuadratura> mapResumen = mapMeses.get(anoMes);
            for (final TipoDiferencia tipoDiferencia : mapResumen.keySet()) {
                final ResumenCuadratura resumen = mapResumen.get(tipoDiferencia);
                String msg = String.format("\t%s: %d", resumen.getGlosa(),
                        resumen.getCantidadCasos());
                println(msg);

                if (tipoDiferencia != TipoDiferencia.CUADRADO) {
                    for (final Registro registro : resumen.getRegistros()) {
                        msg = String.format("\t\t%s", format(registro));
                        println(msg);
                    }
                }
            }
        }
    }

    /**
     * Imprime el encabezado del reporte.
     *
     * @param contexto
     *            El contexto de cuadratura.
     */
    public void printEncabezado(final ContextoCuadratura contexto) {

        final Documento documento = contexto.getDocumento();

        println("PARAMETROS DE CUADRATURA");
        println("------------------------");

        println("Fecha/Hora          : " + format(new Date()));

        println("Nombre Empresa      : " + contexto.getEmpresa().getNombre());
        println("Codigo Usuario      : " + contexto.getUsuario().getCodigo());
        println("Secuencia Documento : " + documento.getId());

        println("Tipo Cuadratura     : " + contexto.getTipoCuadratura());

        String tipoClasificador = contexto.getTipoClasificador();
        if (tipoClasificador == null) {
            tipoClasificador = "Sin Clasificador";
        }
        println("Tipo Clasificador   : " + tipoClasificador);

        println("Esquema             : " + documento.getNombreEsquema());
        println("Version             : " + documento.getVersionEsquema());
        println("Fecha Desde         : " + format(contexto.getFiltroFechas().getFechaDesde()));
        println("Fecha Hasta         : " + format(contexto.getFiltroFechas().getFechaHasta()));

        println();
    }

}
