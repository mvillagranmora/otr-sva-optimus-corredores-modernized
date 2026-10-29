package cl.bcs.valo2.data.consultas.sqlserver;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import cl.bcs.valo2.TipoInteres;
import cl.bcs.valo2.data.ProveedorDatos;
import cl.bcs.valo2.data.ProveedorDatosException;
import cl.bcs.valo2.data.consultas.Consulta;
import cl.bcs.valo2.data.consultas.sqlserver.ConsultaInstrumentoSS.Instrumento;
import cl.bcs.valo2.data.consultas.sqlserver.ConsultaSerieSS.Serie;
import cl.bcs.valo2.entidades.Bono;

public class ConsultaBonosSS extends Consulta<String, Bono>implements ProveedorDatos<String, Bono> {
    private static final Logger LOGGER = Logger.getLogger(ConsultaBonosSS.class.getName());

    private static boolean getAjusteFinDeMes(
            final Serie serie) {
        return serie.getOrden() == 19;
    }

    private static String getCalendario(
            final Instrumento instrumento) {
        final String cal = instrumento.getCalendario();
        if (cal == null) {
            return null;
        }

        if (cal.length() < 2) {
            return cal;
        }

        return cal.substring(0, 2);
    }

    private static String getConvencionDias(
            final Serie serie) {
        switch (serie.getOrden()) {
        case 1:
        case 3:
        case 5:
        case 6:
        case 9:
        case 10:
        case 11:
        case 12:
        case 13:
            return "30/360";
        case 14:
            return "ACT/360";
        case 2:
        case 4:
        case 7:
        case 8:
        case 15:
            return "ACT/365";
        case 16:
            return "ACT/ACT ISDA";
        case 17:
            return "BUS/252";
        case 18:
        case 19:
            return "ISMA 30/360";
        case 20:
            return "NL/365";
        default:
            throw new ProveedorDatosException("Orden no soportado: " + serie.getOrden());
        }
    }

    private static boolean getCuponeraVariable(
            final Serie serie) {
        return serie.getTipoAmortizacion().equals("2");
    }

    private static int getTipoCalculoIntereses(
            final Serie serie) {
        return serie.getOrden() == 17 ? TipoInteres.COMPUESTO : TipoInteres.SIMPLE;
    }

    private static boolean getValorizarEnDiaHabil(
            final Serie serie) {
        return serie.getOrden() == 17;
    }

    private static Bono newBono(
            final Instrumento instrumento,
            final Serie serie) {
        final Bono bono = new Bono();
        bono.setNemo(instrumento.getNemo().trim());
        bono.setFechaDevengo(instrumento.getFechaEmision());
        bono.setFechaVencimiento(instrumento.getFechaVencimiento());
        bono.setCalendario(getCalendario(instrumento).trim());

        bono.setCantidadCupones(serie.getCantidadCupones());
        bono.setPeriodoPago(serie.getPeriodoPago());
        bono.setTasaEmision(serie.getTasaEmision());
        bono.setMonedaEmision(serie.getCodigoMoneda().trim());
        bono.setValorizarEnDiaHabil(getValorizarEnDiaHabil(serie));
        bono.setCuponeraVariable(getCuponeraVariable(serie));
        bono.setTipoCalculoInteresesCupon(getTipoCalculoIntereses(serie));
        bono.setTipoCalculoInteresesDevengados(getTipoCalculoIntereses(serie));
        bono.setAjusteFinDeMes(getAjusteFinDeMes(serie));
        bono.setConvencionDias(getConvencionDias(serie));

        bono.setCallConfirmado(false);
        bono.setMercadoLocal(false);
        bono.setTenorInflacion(null);
        bono.setTipoLibor(null);
        bono.setTipoInflacion(null);
        return bono;
    }

    private final ConsultaInstrumentoSS consultaInstrumento;
    private final ConsultaSerieSS consultaSerie;

    public ConsultaBonosSS(
            final DataSource dataSource) throws SQLException {
        super(dataSource);
        this.consultaInstrumento = new ConsultaInstrumentoSS(dataSource);
        this.consultaSerie = new ConsultaSerieSS(dataSource);
    }

    @Override
    public Bono buscar(
            final Connection conn,
            final String nemo)
                    throws SQLException {
        final Instrumento instrumento = consultaInstrumento.buscar(conn, nemo);
        if (instrumento == null) {
            return null;
        }

        final Serie serie = consultaSerie.buscar(conn, instrumento.getCodigoSerie());
        if (serie == null) {
            throw new ProveedorDatosException("No existe serie asociada al instrumento: " + nemo);
        }

        final Bono bono = newBono(instrumento, serie);
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(bono.toString());
        }

        if (serie.getFechaPrepago() != null) {
            bono.setFechaDevengo(serie.getFechaPrepago());
        }

        return bono;
    }

}
