package cl.bcs.normativa.cartolas.master.graficos;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.jfree.chart.ChartUtilities;

public final class TestGraficadorPastel {

    private static Map<String, BigDecimal> data;
    private static final String PATH = "C:/Users/v_fpino/Documents/Proyectos/bcs/Cartolas/Salidas/graficoPastel.png";

    static {
        data = new HashMap();
        data.put("caja", new BigDecimal("5"));
        data.put("rv", new BigDecimal("9"));
        data.put("rf", new BigDecimal("14"));
        data.put("de", new BigDecimal("19"));
        data.put("fm", new BigDecimal("24"));
        data.put("opl", new BigDecimal("34"));
    }

    public static BufferedImage getGraficoActivoPorProducto() {
        return GraficadorPastel.graficar(data, "");
    }

    public static void main(
            final String[] args)
                    throws FileNotFoundException,
                    IOException {
        final File file = new File(PATH);

        BufferedImage img = getGraficoActivoPorProducto();
        try (final OutputStream out = new FileOutputStream(file)) {
            ChartUtilities.writeBufferedImageAsPNG(out, img);
        }

        final ImageInputStream iis = ImageIO.createImageInputStream(file);
        final Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(iis);
        while (imageReaders.hasNext()) {
            final ImageReader reader = imageReaders.next();
            System.out.printf("formatName: %s%n", reader.getFormatName());
        }

        img = ImageIO.read(file);
        System.out.println(img.getType());
    }

    private TestGraficadorPastel() {
    }

}
