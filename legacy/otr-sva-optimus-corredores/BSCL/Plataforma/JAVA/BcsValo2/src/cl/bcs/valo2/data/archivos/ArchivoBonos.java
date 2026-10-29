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
import cl.bcs.valo2.entidades.Bono;
import cl.bcs.valo2.fechas.FechaParser;

public class ArchivoBonos implements ProveedorDatos<String, Bono> {
    private static final Logger LOGGER = Logger.getLogger(ArchivoBonos.class.getName());

    public static Bono newBono(
            final String line) {
        final String[] arr = line.split(",");

        final Bono bono = new Bono();
        bono.setNemo(arr[0]);
        bono.setIsin(arr[1]);
        bono.setMonedaEmision(arr[2]);
        bono.setFechaDevengo(FechaParser.parseFromDMY(arr[3]).toDate());
        bono.setFechaVencimiento(FechaParser.parseFromDMY(arr[4]).toDate());
        bono.setTasaEmision(new BigDecimal(arr[5]));
        bono.setCantidadCupones(Integer.valueOf(arr[6]));
        bono.setPeriodoPago(Integer.valueOf(arr[7]));
        bono.setTipoCalculoInteresesDevengados(Integer.valueOf(arr[8]));
        bono.setTipoCalculoInteresesCupon(Integer.valueOf(arr[9]));
        bono.setTipoLibor(arr[10]);
        bono.setCuponeraVariable("S".equalsIgnoreCase(arr[11]));
        bono.setValorizarEnDiaHabil("S".equalsIgnoreCase(arr[12]));
        bono.setTipoInflacion(arr[13]);
        bono.setTenorInflacion(arr[14]);
        bono.setCallConfirmado("S".equalsIgnoreCase(arr[15]));
        bono.setCalendario(arr[16]);
        bono.setConvencionDias(arr[17]);
        bono.setAjusteFinDeMes("S".equalsIgnoreCase(arr[18]));
        bono.setMercadoLocal("S".equalsIgnoreCase(arr[19]));
        return bono;
    }

    private Map<String, Bono> bonos;

    public ArchivoBonos(
            final String path) {
        initialize(path);
    }

    @Override
    public Bono buscar(
            final String nemo) {
        return bonos.get(nemo);
    }

    public Map<String, Bono> getBonos() {
        return bonos;
    }

    public void initialize(
            final String path) {
        bonos = new ConcurrentHashMap<String, Bono>();
        String line = null;
        try (final BufferedReader br = new BufferedReader(new FileReader(path))) {
            while ((line = br.readLine()) != null) {
                final Bono bono = newBono(line);
                bonos.put(bono.getNemo(), bono);
            }
        } catch (final IOException ex) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Al deserializar bonos: " + line, ex);
            }
        }
    }

}
