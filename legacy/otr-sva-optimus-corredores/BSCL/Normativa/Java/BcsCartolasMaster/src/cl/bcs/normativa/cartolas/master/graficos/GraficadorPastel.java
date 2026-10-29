package cl.bcs.normativa.cartolas.master.graficos;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import cl.bcs.normativa.cartolas.master.config.PaletaColores;

public final class GraficadorPastel {

    public static BufferedImage graficar(
            final Map<String, BigDecimal> data,
            final String titulo) {

        final DefaultPieDataset dataset = new DefaultPieDataset();
        for (final String key : data.keySet()) {
            final double val = data.get(key).doubleValue();
            if (val > 0) {
                dataset.setValue(key, val);
            }
        }

        final JFreeChart chart = ChartFactory.createPieChart(titulo, dataset, false, false, false);
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);

        final PiePlot plot = (PiePlot) chart.getPlot();
        plot.setOutlinePaint(Color.WHITE);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setInteriorGap(0.10);
        plot.setLabelLinksVisible(true);
        plot.setLabelBackgroundPaint(Color.WHITE);
        plot.setLabelOutlinePaint(Color.WHITE);
        plot.setLabelFont(new Font("Arial", 0, 9));
        plot.setLabelShadowPaint(Color.WHITE);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{2}"));
        plot.setLabelGap(0.0000001);

        plot.setBaseSectionOutlinePaint(Color.BLACK);
        plot.setBaseSectionOutlineStroke(
                new BasicStroke(0.25f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

        plot.setShadowPaint(new Color(132, 130, 132));
        setSectionPaints(plot);

        return chart.createBufferedImage(217, 152);
    }

    private static void setSectionPaints(
            final PiePlot plot) {
        for (final String key : PaletaColores.keySet()) {
            plot.setSectionPaint(key, PaletaColores.get(key));
        }
    }

    private GraficadorPastel() {
    }

}
