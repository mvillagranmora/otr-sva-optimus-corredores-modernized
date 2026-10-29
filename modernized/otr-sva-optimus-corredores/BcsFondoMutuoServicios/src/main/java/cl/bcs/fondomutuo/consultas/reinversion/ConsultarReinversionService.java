package cl.bcs.fondomutuo.consultas.reinversion;

import java.util.List;

/**
 * Legacy {@code ConsultarFondoMutuoReinversion.logica} (FM-FMR-01..04, read only): one read of {@code ConsultarFondoMutuo_SelectReinversion.sql} with the
 * filters of the request passed untouched (the wildcard rules, {@code '-'} and {@code ''}, live in the SQL; a null is a null), and every row copied field by
 * field into a new object. No collaborator, no counter, no messages (D-F3A-27); {@code p_IdUsuario} and {@code p_IdModulo} are never used. No state; the unit
 * of work is the ambient read-only transaction.
 */
public class ConsultarReinversionService implements ConsultarReinversionPort {

    private final ConsultarReinversionDatos datos;

    public ConsultarReinversionService(ConsultarReinversionDatos datos) {
        this.datos = datos;
    }

    @Override
    public ConsultarReinversionContrato.Salida logica(ConsultarReinversionContrato.Entrada entrada) throws Exception {
        ConsultarReinversionContrato.Salida out = new ConsultarReinversionContrato.Salida();

        List<ConsultarReinversionContrato.FiltroFondoMutuo> lista = datos.reinversion(entrada.p_IdEmpresa, entrada.p_IdEstado, entrada.p_IdMoneda,
                entrada.p_FondoExtranjero, entrada.p_IdFamiliaFondo, entrada.p_IdEmisor, entrada.p_IdRegimenFondoMutuo, entrada.p_IdSerieFondoOrigen);

        for (ConsultarReinversionContrato.FiltroFondoMutuo item : lista) {
            ConsultarReinversionContrato.FiltroFondoMutuo aux = new ConsultarReinversionContrato.FiltroFondoMutuo();
            aux.IdSerieFondo = item.IdSerieFondo;
            aux.NombreSerieFondo = item.NombreSerieFondo;
            aux.IdMoneda = item.IdMoneda;
            aux.IdEmisor = item.IdEmisor;
            aux.MLT = item.MLT;
            aux.IdRegimenFondoMutuo = item.IdRegimenFondoMutuo;
            aux.ValorCuotaInstrumento = item.ValorCuotaInstrumento;
            aux.MontoPatrimonioEmisor = item.MontoPatrimonioEmisor;
            aux.MontoMinimo = item.MontoMinimo;
            out.P_OUT_LISTA.add(aux);
        }
        return out;
    }
}
