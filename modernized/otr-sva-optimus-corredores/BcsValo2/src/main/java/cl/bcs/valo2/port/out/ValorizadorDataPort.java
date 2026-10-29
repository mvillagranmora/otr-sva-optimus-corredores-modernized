package cl.bcs.valo2.port.out;

import cl.bcs.valo2.domain.bono.Bono;
import cl.bcs.valo2.domain.comun.Feriado;
import cl.bcs.valo2.domain.comun.Indicador;
import cl.bcs.valo2.domain.comun.Paridad;
import cl.bcs.valo2.domain.cupon.Cupon;

import java.time.LocalDate;
import java.util.List;

public interface ValorizadorDataPort {
    Bono buscarBono(String nemo);
    List<Cupon> buscarCupones(String nemo, LocalDate fechaValorizacion);
    List<Feriado> buscarFeriados(String calendario, LocalDate desde, LocalDate hasta);
    Indicador buscarIndicador(String codigo, LocalDate fecha);
    Paridad buscarParidad(String monedaOrigen, String monedaDestino, LocalDate fecha);
}
