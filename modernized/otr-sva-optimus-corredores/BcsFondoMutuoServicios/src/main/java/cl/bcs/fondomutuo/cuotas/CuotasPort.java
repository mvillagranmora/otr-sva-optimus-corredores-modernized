package cl.bcs.fondomutuo.cuotas;

import java.util.List;

import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota.ValorCuotaTipoParidad;

/**
 * Read port of {@link CalcularMontoNumeroCuota}: {@code CalcularMontoNumeroCuota_SelectValorCuotaTipoCambio.sql} (verbatim) - per series, the decimals of the
 * quota/quota value and the observed-dollar rates for contributions and redemptions (0 when there is no rate). The series data of the other legacy SELECT
 * come from {@code SeriesFondoPort}. Reads only.
 */
public interface CuotasPort {

    List<ValorCuotaTipoParidad> valorCuotaTipoCambio(Long idEmpresa, List<String> idSerieFondo);
}
