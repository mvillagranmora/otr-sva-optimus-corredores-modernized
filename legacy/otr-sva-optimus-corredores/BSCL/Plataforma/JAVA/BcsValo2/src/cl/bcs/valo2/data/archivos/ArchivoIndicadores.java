package cl.bcs.valo2.data.archivos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import cl.bcs.valo2.data.ProveedorDatos;
import cl.bcs.valo2.entidades.Indicador;
import cl.bcs.valo2.entidades.Indicador.Clave;
import cl.bcs.valo2.fechas.Fecha;
import cl.bcs.valo2.fechas.FechaParser;

public class ArchivoIndicadores implements ProveedorDatos<Clave, Indicador> {
    private static final Logger LOGGER = Logger.getLogger(ArchivoIndicadores.class.getName());

    public static Indicador newIndice(
            final String line) {
        final String[] arr = line.split(",");

        final String nombre = arr[0];
        final Fecha fecha = FechaParser.parseFromDMY(arr[1]);
        final BigDecimal valor = new BigDecimal(arr[2]);

        final Clave clave = new Clave(nombre, fecha);
        final Indicador indicador = new Indicador();
        indicador.setClave(clave);
        indicador.setValor(valor);
        return indicador;
    }

    private Map<String, Map<Fecha, Indicador>> indicadores;

    public ArchivoIndicadores(
            final String path) {
        initialize(path);
    }

    @Override
    public Indicador buscar(
            final Clave clave) {
        if (!indicadores.containsKey(clave.getNombre())) {
            return null;
        }

        final Map<Fecha, Indicador> map = indicadores.get(clave.getNombre());
        return map.get(clave.getFecha());
    }

    public void initialize(
            final String path) {
        indicadores = new ConcurrentHashMap<String, Map<Fecha, Indicador>>();
        String line = null;
        try (final BufferedReader br = new BufferedReader(new FileReader(path))) {
            while ((line = br.readLine()) != null) {
                final Indicador indice = newIndice(line);
                final String nombre = indice.getClave().getNombre();
                if (!indicadores.containsKey(nombre)) {
                    final Map<Fecha, Indicador> list = new ConcurrentHashMap<Fecha, Indicador>();
                    indicadores.put(nombre, list);
                }

                indicadores.get(nombre).put(indice.getClave().getFecha(), indice);
            }
        } catch (final IOException ex) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Al deserializar indices: " + line, ex);
            }
        }
    }

}
