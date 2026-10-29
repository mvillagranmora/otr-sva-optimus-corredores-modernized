package cl.bcs.valo2.infrastructure.persistence;

import cl.bcs.valo2.domain.bono.Bono;
import cl.bcs.valo2.domain.comun.Feriado;
import cl.bcs.valo2.domain.comun.Indicador;
import cl.bcs.valo2.domain.comun.Paridad;
import cl.bcs.valo2.domain.cupon.Cupon;
import cl.bcs.valo2.port.out.ValorizadorDataPort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class ValorizadorRepository implements ValorizadorDataPort {

    @Override
    public Bono buscarBono(String nemo) {
        throw new UnsupportedOperationException("requires HANA: OPTIMUS.INSTRUMENTO / BONO");
    }

    @Override
    public List<Cupon> buscarCupones(String nemo, LocalDate fechaValorizacion) {
        throw new UnsupportedOperationException("requires HANA: OPTIMUS.CUPON");
    }

    @Override
    public List<Feriado> buscarFeriados(String calendario, LocalDate desde, LocalDate hasta) {
        throw new UnsupportedOperationException("requires HANA: OPTIMUS.FERIADO");
    }

    @Override
    public Indicador buscarIndicador(String codigo, LocalDate fecha) {
        throw new UnsupportedOperationException("requires HANA: OPTIMUS.INDICADOR");
    }

    @Override
    public Paridad buscarParidad(String monedaOrigen, String monedaDestino, LocalDate fecha) {
        throw new UnsupportedOperationException("requires HANA: OPTIMUS.PARIDAD");
    }
}
