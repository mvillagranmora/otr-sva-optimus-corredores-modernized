package cl.bcs.normativa.cartolas.master.graficos;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import cl.bcs.normativa.cartolas.master.config.PaletaColores;

public final class GraficadorSerie {

    private static final String ROTULO_EJE_X = "";
    private static final String ROTULO_EJE_Y = "";
    private static final String ROTULO_FECHA = "Periodo";
    private static final String ROTULO_NO_HAY_DATOS = "No hay datos";

    public static BufferedImage graficar(
            final TimeSeries series,
            final String titulo) {
        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        final JFreeChart chart = ChartFactory.createXYLineChart(titulo, ROTULO_EJE_X, ROTULO_EJE_Y,
                dataset, PlotOrientation.VERTICAL, true, false, false);
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
        renderer.setSeriesPaint(0, PaletaColores.get("fm"));

        plot.setRenderer(renderer);

        final DateAxis xAxis = new DateAxis(ROTULO_FECHA);
        plot.setDomainAxis(xAxis);

        final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        yAxis.setNumberFormatOverride(new DecimalFormat("$ #,##0"));
        yAxis.setAutoRangeIncludesZero(false);

        return chart.createBufferedImage(780, 570);
    }

    private GraficadorSerie() {
    }

}
