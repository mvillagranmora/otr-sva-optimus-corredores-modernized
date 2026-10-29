package cl.bcs.cartolas.master.service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.stereotype.Service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * Servicio de generación de gráficos de serie temporal (line chart).
 *
 * <p>La lógica pura está en el método {@code static} {@link #graficar} para ser
 * testeable sin Spring.</p>
 *
 * <p>Equivalente modernizado de {@code graficos.GraficadorSerie} legacy.</p>
 */
@Service
public class GraficadorSerieService {

    private static final String ROTULO_EJE_X = "";
    private static final String ROTULO_EJE_Y = "";
    private static final String ROTULO_FECHA = "Periodo";
    private static final String ROTULO_NO_HAY_DATOS = "No hay datos";

    /** Color de la serie (azul-verdoso FM de la paleta BCS). */
    private static final Color COLOR_SERIE = new Color(0x00cccc);

    /**
     * Genera un gráfico de líneas con una serie temporal de patrimonio.
     *
     * <p>Tamaño de salida: 780×570 px (idéntico al legacy).</p>
     *
     * @param series serie temporal con datos de patrimonio
     * @param titulo título del gráfico (puede ser vacío)
     * @return imagen en formato {@link BufferedImage}
     */
    public static BufferedImage graficar(final TimeSeries series, final String titulo) {
        Objects.requireNonNull(series, "series es requerido");

        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        final JFreeChart chart = ChartFactory.createXYLineChart(
                titulo != null ? titulo : "",
                ROTULO_EJE_X,
                ROTULO_EJE_Y,
                dataset,
                PlotOrientation.VERTICAL,
                true, false, false);
        chart.setBackgroundPaint(Color.LIGHT_GRAY);

        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setRangeGridlineStroke(
                new BasicStroke(0, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1));
        plot.setNoDataMessage(ROTULO_NO_HAY_DATOS);

        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesStroke(0,
                new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1));
        renderer.setSeriesPaint(0, COLOR_SERIE);
        plot.setRenderer(renderer);

        final DateAxis xAxis = new DateAxis(ROTULO_FECHA);
        plot.setDomainAxis(xAxis);

        final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        yAxis.setNumberFormatOverride(new DecimalFormat("$ #,##0"));
        yAxis.setAutoRangeIncludesZero(false);

        return chart.createBufferedImage(780, 570);
    }

    /**
     * Variante de instancia (delega a la lógica estática).
     */
    public BufferedImage generarGraficoSerie(final TimeSeries series, final String titulo) {
        return graficar(series, titulo);
    }
}
