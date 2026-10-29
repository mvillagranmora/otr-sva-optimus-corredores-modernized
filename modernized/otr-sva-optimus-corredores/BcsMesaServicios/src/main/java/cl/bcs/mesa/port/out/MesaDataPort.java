package cl.bcs.mesa.port.out;

import cl.bcs.mesa.domain.MovimientoSpot;
import cl.bcs.mesa.domain.PosicionInicial;

import java.util.List;

/** Puerto de salida — acceso a datos de Mesa de Dinero en HANA. */
public interface MesaDataPort {
    List<MovimientoSpot> obtenerMovimientosSpot(String fechaOperacion);
    List<PosicionInicial> obtenerPosicionesIniciales(String fechaOperacion);
}
