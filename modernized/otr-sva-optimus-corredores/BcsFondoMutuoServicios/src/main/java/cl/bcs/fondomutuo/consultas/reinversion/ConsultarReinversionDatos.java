package cl.bcs.fondomutuo.consultas.reinversion;

import java.util.List;

/** Read port: all rows of {@code ConsultarFondoMutuo_SelectReinversion.sql}, verbatim; nulls are passed as nulls. */
public interface ConsultarReinversionDatos {

    List<ConsultarReinversionContrato.FiltroFondoMutuo> reinversion(Long idEmpresa, String idEstado, String idMoneda, String fondoExtranjero, String idFamiliaFondo,
            String idEmisor, String idRegimenFondoMutuo, String idSerieFondoOrigen);
}
