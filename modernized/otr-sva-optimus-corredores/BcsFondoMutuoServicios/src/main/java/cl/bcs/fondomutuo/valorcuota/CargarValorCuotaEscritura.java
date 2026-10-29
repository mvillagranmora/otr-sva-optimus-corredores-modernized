package cl.bcs.fondomutuo.valorcuota;

import java.util.List;

/** Write port: the four writes of the class, in order. Joins the ambient transaction; never commits or rolls back. */
public interface CargarValorCuotaEscritura {

    void actualizarUltimaCarga(List<CargarValorCuotaContrato.Resultado> filas);

    void insertarHistorico(List<CargarValorCuotaContrato.Resultado> filas, List<Long> ids);

    void eliminarVigentes(List<CargarValorCuotaContrato.Resultado> filas);

    void insertarVigentes(List<CargarValorCuotaContrato.Resultado> filas);
}
