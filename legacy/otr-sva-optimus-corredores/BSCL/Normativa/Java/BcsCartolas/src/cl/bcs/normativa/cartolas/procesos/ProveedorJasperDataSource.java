package cl.bcs.normativa.cartolas.procesos;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.LazyDynaBean;

import com.opencsv.CSVReader;

import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.entidades.Datos;
import cl.bcs.normativa.cartolas.entidades.FormatoDatos;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public abstract class ProveedorJasperDataSource {

    private static final String VALOR_DATOS_POR_DEFECTO = "prueba\ntrue";

    protected Map<String, Datos> datosCartola = new ConcurrentHashMap();

    public List<DynaBean> getBeanList(
            final Cartola cartola,
            final String nombreReporte)
                    throws SQLException,
                    IOException {
        final List<DynaBean> list = new ArrayList();
        final Datos datos = getDatos(cartola, nombreReporte);
        final Reader reader = new StringReader(datos.getValor());
        try (final CSVReader csv = new CSVReader(reader)) {
            final String[] nombres = csv.readNext();

            String[] fila = csv.readNext();
            while (fila != null) {
                list.add(newBean(nombres, fila));
                fila = csv.readNext();
            }
        }

        return list;
    }

    private Datos getDatos(
            final Cartola cartola,
            final String nombre) {
        if (nombre == null || !datosCartola.containsKey(nombre)) {
            return getDatosPorDefecto(cartola, nombre);
        }

        return datosCartola.get(nombre);
    }

    private Datos getDatosPorDefecto(
            final Cartola cartola,
            final String nombre) {
        final Datos datos = new Datos();
        datos.setFormato(getFormato());
        datos.setIdCartola(cartola.getId());
        datos.setNombre(nombre);
        datos.setValor(VALOR_DATOS_POR_DEFECTO);
        return datos;
    }

    private Map<String, String> getFields(
            final String[] nombres,
            final String[] fila) {
        final Map<String, String> map = new ConcurrentHashMap();
        for (int i = 0; i < nombres.length; i++) {
            map.put(nombres[i], fila[i]);
        }

        return map;
    }

    public String getFormato() {
        return FormatoDatos.CSV;
    }

    public JRDataSource getJRDataSource(
            final Cartola cartola,
            final String nombreReporte)
                    throws SQLException,
                    IOException {

        final List<DynaBean> list = getBeanList(cartola, nombreReporte);
        return new JRBeanCollectionDataSource(list);
    }

    public String getValorDatosPorDefecto() {
        return VALOR_DATOS_POR_DEFECTO;
    }

    private Object getValue(
            final String str) {
        Object val;

        try {
            val = Integer.valueOf(str);
        } catch (Exception ex) {
            try {
                val = Timestamp.valueOf(str);
            } catch (Exception ex1) {
                try {
                    val = new BigDecimal(str);
                } catch (Exception ex2) {
                    val = str;
                }
            }
        }

        return val;
    }

    public boolean hayDatos(
            final String nombre)
                    throws IOException {
        return datosCartola.containsKey(nombre);
    }

    public abstract void load(
            final Connection connection,
            final String schema,
            final Cartola idCartola)
                    throws SQLException,
                    IOException,
                    ParseException;

    public DynaBean newBean(
            final String[] nombres,
            final String[] fila) {
        final LazyDynaBean result = new LazyDynaBean();
        final Map<String, String> fields = getFields(nombres, fila);
        for (final String key : fields.keySet()) {
            final Object obj = getValue(fields.get(key));
            result.set(key, obj);
        }

        return result;
    }

    public void put(
            final Datos datos) {
        this.datosCartola.put(datos.getNombre(), datos);
    }

}
