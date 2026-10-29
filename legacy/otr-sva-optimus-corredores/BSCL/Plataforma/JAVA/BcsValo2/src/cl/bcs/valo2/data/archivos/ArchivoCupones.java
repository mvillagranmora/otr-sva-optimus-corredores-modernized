package cl.bcs.valo2.data.archivos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import cl.bcs.valo2.data.ProveedorDatos;
import cl.bcs.valo2.entidades.Cupon;
import cl.bcs.valo2.fechas.FechaParser;

public class ArchivoCupones implements ProveedorDatos<String, List<Cupon>> {
    private static final Logger LOGGER = Logger.getLogger(ArchivoCupones.class.getName());

    public static Cupon newCupon(
            final String line) {
        final String[] arr = line.split(",");

        final Cupon cupon = new Cupon();
        cupon.setNemo(arr[0]);
        cupon.setNumero(Integer.valueOf(arr[1]));
        cupon.setFecha(FechaParser.parseFromDMY(arr[2]).toDate());
        cupon.setAmortizacion(new BigDecimal(arr[3]));
        cupon.setSaldoPorAmortizar(new BigDecimal(arr[4]));
        cupon.setIntereses(new BigDecimal(arr[5]));
        cupon.setCall("S".equalsIgnoreCase(arr[6]));
        return cupon;
    }

    private Map<String, List<Cupon>> cupones;

    public ArchivoCupones(
            final String path) {
        initialize(path);
    }

    @Override
    public List<Cupon> buscar(
            final String nemo) {
        return cupones.get(nemo);
    }

    public Map<String, List<Cupon>> getCupones() {
        return cupones;
    }

    public void initialize(
            final String path) {
        cupones = new ConcurrentHashMap<String, List<Cupon>>();
        String line = null;
        try (final BufferedReader br = new BufferedReader(new FileReader(path))) {
            while ((line = br.readLine()) != null) {
                final Cupon cupon = newCupon(line);
                final String nemo = cupon.getNemo();
                if (!cupones.containsKey(nemo)) {
                    final List<Cupon> list = new ArrayList<Cupon>();
                    cupones.put(nemo, list);
                }

                cupones.get(nemo).add(cupon);
            }
        } catch (final IOException ex) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Al deserializar cupones: " + line, ex);
            }
        }
    }

}
