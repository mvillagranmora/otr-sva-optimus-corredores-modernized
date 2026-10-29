package cl.bcs.normativa.cartolas.master.graficos;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.beanutils.DynaBean;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;

import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.master.config.GraficoMaster;
import cl.bcs.normativa.cartolas.procesos.ProveedorJasperDataSource;
import net.sf.jasperreports.engine.JRException;

public final class ProveedorGraficos {

    private final ProveedorJasperDataSource proveedorJasperDataSource;

    public ProveedorGraficos(
            final ProveedorJasperDataSource proveedorJasperDataSource) {
        this.proveedorJasperDataSource = proveedorJasperDataSource;
    }

    public BufferedImage createGrafico(
            final GraficoMaster grafico,
            final Cartola cartola,
            final String titulo)
                    throws SQLException,
                    IOException,
                    JRException {
        switch (grafico) {
        case GraficoActivoPorProducto:
            return createGraficoPorProducto(grafico, cartola, titulo);
        case GraficoConsolidadoPorProducto:
            return createGraficoPorProducto(grafico, cartola, titulo);
        case GraficoFM:
            return createGraficoPatrimonio(grafico, cartola, titulo);
        default:
            return null;
        }
    }

    private BufferedImage createGraficoPatrimonio(
            final GraficoMaster grafico,
            final Cartola cartola,
            final String titulo)
                    throws SQLException,
                    IOException {
        final TimeSeries serie = new TimeSeries("Patrimonio");
        final List<DynaBean> beans = proveedorJasperDataSource.getBeanList(cartola, grafico.name());
        for (final DynaBean bean : beans) {
            final Timestamp ts = (Timestamp) bean.get("Fecha");
            final BigDecimal value = (BigDecimal) bean.get("Patrimonio_Final");
            serie.add(new Day(new java.util.Date(ts.getTime())), value.doubleValue());
        }

        return GraficadorSerie.graficar(serie, titulo);
    }

    private BufferedImage createGraficoPorProducto(
            final GraficoMaster grafico,
            final Cartola cartola,
            final String titulo)
                    throws SQLException,
                    IOException,
                    JRException {
        final Map<String, BigDecimal> data = new ConcurrentHashMap();
        final List<DynaBean> beans = proveedorJasperDataSource.getBeanList(cartola, grafico.name());
        for (final DynaBean bean : beans) {
            final String key = (String) bean.get("grupo");
            final BigDecimal value = (BigDecimal) bean.get("totPorcActivo");
            data.put(key, value);
        }

        return GraficadorPastel.graficar(data, titulo);
    }

}
