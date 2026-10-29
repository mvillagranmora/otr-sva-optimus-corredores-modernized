package cl.bcs.valo2.calculadoras;

import java.sql.SQLException;

import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.ValorizadorBonosParams;
import cl.bcs.valo2.entidades.Bono;
import cl.bcs.valo2.fechas.Fecha;
import cl.bcs.valo2.repositorios.RepositorioIndicadores;

public class CalculadoraReajuste implements Calculadora {
    private final ValorizadorContexto contexto;
    private final RepositorioIndicadores repositorioIndicadores;

    public CalculadoraReajuste(
            final ValorizadorContexto contexto) throws SQLException {
        this.contexto = contexto;
        this.repositorioIndicadores = new RepositorioIndicadores(contexto);
    }

    private double buscar(
            final String nombre,
            final Fecha fecha) {
        return repositorioIndicadores
                .buscar(nombre, fecha, contexto.getConfig().getDiasBusqueda()).getValor()
                .doubleValue();
    }

    @Override
    public void calcular(
            final ValorizadorBonosParams params,
            final Bono bono) {
        params.setProporcionReajuste(calcularProporcionReajustes(params, bono));
    }

    protected double calcularProporcionReajustes(
            final ValorizadorBonosParams params,
            final Bono bono) {
        return calcularReajuste(bono.getTipoInflacion(), bono.getTenorInflacion(),
                Fecha.getInstance(bono.getFechaDevengo()),
                Fecha.getInstance(params.getFechaValorizacion()));
    }

    public double calcularReajuste(
            final String tipoInflacion,
            final String tenorInflacion,
            Fecha fechaDesde,
            Fecha fechaHasta) {
        if (tipoInflacion == null || tipoInflacion.isEmpty()) {
            return 0;
        }

        if (tenorInflacion != null && !tenorInflacion.isEmpty()) {
            fechaDesde = fechaDesde.sumarTenor(tenorInflacion);
            fechaHasta = fechaHasta.sumarTenor(tenorInflacion);
        }

        final double indiceIni = buscar(tipoInflacion, fechaDesde);
        final double indiceFin = buscar(tipoInflacion, fechaHasta);
        return indiceFin / indiceIni - 1;
    }

}
