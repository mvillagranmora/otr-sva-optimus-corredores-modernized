package cl.bcs.valo2.cupones;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.entidades.Bono;
import cl.bcs.valo2.entidades.Cupon;
import cl.bcs.valo2.fechas.Fecha;
import cl.bcs.valo2.repositorios.RepositorioCupones;
import cl.bcs.valo2.repositorios.RepositorioIndicadores;

public class ProveedorCuponesVariable implements ProveedorCupones {
    private final ValorizadorContexto contexto;
    private final RepositorioCupones repositorioCupones;
    private final RepositorioIndicadores repositorioIndicadores;

    public ProveedorCuponesVariable(
            final ValorizadorContexto contexto) throws SQLException {
        this.contexto = contexto;
        this.repositorioCupones = new RepositorioCupones(contexto);
        this.repositorioIndicadores = new RepositorioIndicadores(contexto);
    }

    private BigDecimal calcularInteresesMasLibor(
            final Cupon cupon,
            final String tipoSpread) {
        BigDecimal intereses = cupon.getIntereses();
        if (tipoSpread != null && !tipoSpread.isEmpty()) {
            final BigDecimal spread = repositorioIndicadores
                    .buscar(tipoSpread, Fecha.getInstance(cupon.getFecha()),
                            contexto.getConfig().getDiasBusqueda())
                    .getValor();
            intereses = cupon.getIntereses().add(spread);
        }

        return intereses;
    }

    @Override
    public List<Cupon> obtener(
            final Bono bono,
            final Date fecha) {
        final List<Cupon> list = new ArrayList<Cupon>();
        Date fechaAnterior = bono.getFechaDevengo();
        final List<Cupon> cupones = repositorioCupones.buscar(bono.getNemo());

        for (final Cupon cupon : cupones) {
            if (cupon.getFecha().compareTo(fecha) > 0
                    || bono.isEnFechaVencimiento(fecha) && cupon.getFecha().compareTo(fecha) == 0) {
                final Cupon duplicado = Cupon.duplicar(cupon);
                duplicado.setFechaAnterior(fechaAnterior);
                duplicado.setIntereses(calcularInteresesMasLibor(cupon, bono.getTipoLibor()));
                list.add(duplicado);
            }

            fechaAnterior = cupon.getFecha();
        }

        return list;
    }

}
