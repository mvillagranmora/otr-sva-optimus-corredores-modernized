package cl.bcs.valo2;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import cl.bcs.valo2.calculadoras.CalculadoraMedidas;
import cl.bcs.valo2.calculadoras.CalculadoraMontos;
import cl.bcs.valo2.calculadoras.CalculadoraPagos;
import cl.bcs.valo2.calculadoras.CalculadoraPorMonto;
import cl.bcs.valo2.calculadoras.CalculadoraPorPrecio;
import cl.bcs.valo2.calculadoras.CalculadoraPorTasa;
import cl.bcs.valo2.calculadoras.CalculadoraReajuste;
import cl.bcs.valo2.entidades.Bono;
import cl.bcs.valo2.entidades.Cupon;
import cl.bcs.valo2.fechas.Fecha;
import cl.bcs.valo2.fechas.contadores.ContadorDias;
import cl.bcs.valo2.repositorios.RepositorioBonos;

public class ValorizadorBonos {
    private static final Logger LOGGER = Logger.getLogger(ValorizadorBonos.class.getName());

    private final CalculadoraMedidas calculadoraMedidas;
    private final CalculadoraMontos calculadoraMontos;
    private final CalculadoraPagos calculadoraPagos;
    private final CalculadoraPorMonto calculadoraPorMonto;
    private final CalculadoraPorPrecio calculadoraPorPrecio;
    private final CalculadoraPorTasa calculadoraPorTasa;
    private final CalculadoraReajuste calculadoraReajuste;
    private final ContadorDias contadorDias;
    private final Cuponera cuponera;
    private final RepositorioBonos repositorioBonos;

	private ValorizadorContexto contexto;

    public ValorizadorBonos(
            final DataSource dataSource) throws SQLException {
        this(new ValorizadorContexto(dataSource));
    }

    public ValorizadorBonos(
            final ValorizadorContexto contexto) throws SQLException {
    	this.contexto = contexto;
        this.calculadoraMedidas = new CalculadoraMedidas(contexto);
        this.calculadoraMontos = new CalculadoraMontos(contexto);
        this.calculadoraPagos = new CalculadoraPagos(contexto);
        this.calculadoraPorMonto = new CalculadoraPorMonto(contexto);
        this.calculadoraPorPrecio = new CalculadoraPorPrecio(contexto);
        this.calculadoraPorTasa = new CalculadoraPorTasa(contexto);
        this.calculadoraReajuste = new CalculadoraReajuste(contexto);
        this.contadorDias = new ContadorDias(contexto);
        this.cuponera = new Cuponera(contexto);
        this.repositorioBonos = new RepositorioBonos(contexto);
    }

    public void valorizar(
            final List<ValorizadorBonosParams> list) {
        for (final ValorizadorBonosParams params : list) {
            try {
                valorizar(params);
                params.setCodigoRetorno(0);
            } catch (final Exception ex) {
                params.setCodigoRetorno(-1);
                params.setMensaje(ex.getMessage());
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "Error al valorizar: " + params, ex);
                }
            }
        }
    }

    public void valorizar(
            final ValorizadorBonosParams params) {
        final Bono bono = repositorioBonos.buscar(params.getNemo());
        valorizar(params, bono);
    }

    public void valorizar(
            final ValorizadorBonosParams params,
            final Bono bono) {
        if (params.getPrecio() == null && params.getTasa() == null
                && params.getMontoTotal() == null) {
            throw new ValorizadorException("Demasiadas incognitas");
        }

        if (bono.isInactivo(params.getFechaValorizacion())) {
            throw new ValorizadorException("Valorizacion de bono inactivo", params);
        }

        if (bono.isVencido(params.getFechaValorizacion())) {
            throw new ValorizadorException("Valorizacion de bono vencido", params);
        }

        if (bono.isValorizarEnDiaHabil()) {
            final Fecha fecha = contadorDias.diaHabil(
                    Fecha.getInstance(params.getFechaValorizacion()), bono.getCalendario());
            params.setFechaValorizacion(fecha.toDate());
        }

        final List<Cupon> cupones = cuponera.obtenerCupones(bono, params.getFechaValorizacion());
        if (cupones == null || cupones.isEmpty()) {
            throw new ValorizadorException("No hay cupones", params);
        }

        params.setCupones(cupones);

        calculadoraReajuste.calcular(params, bono);

        if (params.getMontoTotal() != null) {
            calculadoraPorMonto.calcular(params, bono);
        } else if (params.getTasa() != null) {
            calculadoraPorTasa.calcular(params, bono);
        } else {
            calculadoraPorPrecio.calcular(params, bono);
        }

        calculadoraMontos.calcular(params, bono);
        calculadoraMedidas.calcular(params, bono);
        calculadoraPagos.calcular(params, bono);
    }
}
