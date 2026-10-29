package cl.bcs.valo2.data.archivos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import cl.bcs.valo2.data.ProveedorDatos;
import cl.bcs.valo2.fechas.Fecha;
import cl.bcs.valo2.fechas.FechaParser;

public class ArchivoFeriados implements ProveedorDatos<String, Set<Fecha>> {
    private static final Logger LOGGER = Logger.getLogger(ArchivoFeriados.class.getName());

    private Map<String, Set<Fecha>> feriados;

    public ArchivoFeriados(
            final String path) {
        initialize(path);
    }

    @Override
    public Set<Fecha> buscar(
            final String calendario) {
        return feriados.get(calendario);
    }

    private void initialize(
            final String path) {
        feriados = new ConcurrentHashMap<String, Set<Fecha>>();
        String line = null;
        try (final BufferedReader br = new BufferedReader(new FileReader(path))) {
            while ((line = br.readLine()) != null) {
                final String[] arr = line.split(",");
                final String calendario = arr[0];
                final Fecha fecha = FechaParser.parseFromDMY(arr[1]);

                if (!feriados.containsKey(calendario)) {
                    final Set<Fecha> set = new HashSet<Fecha>();
                    feriados.put(calendario, set);
                }

                feriados.get(calendario).add(fecha);
            }
        } catch (final IOException ex) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Al deserializar feriados: " + line, ex);
            }
        }
    }

}
