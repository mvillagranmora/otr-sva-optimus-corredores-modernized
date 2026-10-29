package cl.bcs.cartolas.master.service;

import cl.bcs.cartolas.master.domain.PaletaColores;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * Servicio de generación de gráficos de torta (pie chart) por clase de activo.
 *
 * <p>La lógica pura está en el método {@code static} {@link #graficar} para ser
 * testeable sin Spring.</p>
 *
 * <p>Equivalente modernizado de {@code graficos.GraficadorPastel} legacy.</p>
 */
@Service
public class GraficadorPastelService {

    /**
     * Genera un gráfico de torta a partir de un mapa clave→valor.
     *
     * <p>Sólo se incluyen valores {@code > 0}. El color de cada sector es tomado
     * desde {@link PaletaColores}. El tamaño de salida es 217×152 px (idéntico al legacy).</p>
     *
     * @param data   mapa con clave de producto y porcentaje (p.e. {"RV": 35.5, "RF": 20.0})
     * @param titulo título del gráfico (puede ser vacío)
     * @return imagen en formato {@link BufferedImage}
     */
    public static BufferedImage graficar(final Map<String, BigDecimal> data, final String titulo) {
        Objects.requireNonNull(data, "data es requerido");

        final DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (final Map.Entry<String, BigDecimal> entry : data.entrySet()) {
            final double val = entry.getValue().doubleValue();
            if (val > 0) {
                dataset.setValue(entry.getKey(), val);
            }
        }

        final JFreeChart chart = ChartFactory.createPieChart(
                titulo != null ? titulo : "",
                dataset, false, false, false);
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);

        @SuppressWarnings("unchecked")
        final PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setOutlinePaint(Color.WHITE);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setInteriorGap(0.10);
        plot.setLabelLinksVisible(true);
        plot.setLabelBackgroundPaint(Color.WHITE);
        plot.setLabelOutlinePaint(Color.WHITE);
        plot.setLabelFont(new Font("Arial", Font.PLAIN, 9));
        plot.setLabelShadowPaint(Color.WHITE);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{2}"));
        plot.setLabelGap(0.0000001);

        plot.setDefaultSectionOutlinePaint(Color.BLACK);
        plot.setDefaultSectionOutlineStroke(
                new BasicStroke(0.25f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

        plot.setShadowPaint(new Color(132, 130, 132));

        // Aplicar colores de la paleta BCS
        for (final String key : PaletaColores.keySet()) {
            final Color color = PaletaColores.get(key);
            if (color != null) {
                plot.setSectionPaint(key, color);
            }
        }

        return chart.createBufferedImage(217, 152);
    }

    /**
     * Variante de instancia (delega a la lógica estática).
     */
    public BufferedImage generarGraficoPastel(final Map<String, BigDecimal> data,
                                              final String titulo) {
        return graficar(data, titulo);
    }
}
