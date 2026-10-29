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

public class ArchivoParidades implements ProveedorDatos<Clave, Indicador> {
    private static final Logger LOGGER = Logger.getLogger(ArchivoParidades.class.getName());

    public static Indicador newParidad(
            final String line) {
        final String[] arr = line.split(",");

        final String nombre = arr[0];
        final Fecha fecha = FechaParser.parseFromDMY(arr[1]);
        final BigDecimal valor = new BigDecimal(arr[2]);

        final Clave clave = new Clave(nombre, fecha);
        final Indicador paridad = new Indicador();
        paridad.setClave(clave);
        paridad.setValor(valor);
        return paridad;
    }

    private Map<String, Map<Fecha, Indicador>> paridades;

    public ArchivoParidades(
            final String path) {
        initialize(path);
    }

    @Override
    public Indicador buscar(
            final Clave clave) {
        if (!paridades.containsKey(clave.getNombre())) {
            return null;
        }

        final Map<Fecha, Indicador> map = paridades.get(clave.getNombre());
        return map.get(clave.getFecha());
    }

    public void initialize(
            final String path) {
        paridades = new ConcurrentHashMap<String, Map<Fecha, Indicador>>();
        String line = null;
        try (final BufferedReader br = new BufferedReader(new FileReader(path))) {
            while ((line = br.readLine()) != null) {
                final Indicador paridad = newParidad(line);
                final String nombre = paridad.getClave().getNombre();
                if (!paridades.containsKey(nombre)) {
                    final Map<Fecha, Indicador> list = new ConcurrentHashMap<Fecha, Indicador>();
                    paridades.put(nombre, list);
                }

                paridades.get(nombre).put(paridad.getClave().getFecha(), paridad);
            }
        } catch (final IOException ex) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Al deserializar paridades: " + line, ex);
            }
        }
    }

}
