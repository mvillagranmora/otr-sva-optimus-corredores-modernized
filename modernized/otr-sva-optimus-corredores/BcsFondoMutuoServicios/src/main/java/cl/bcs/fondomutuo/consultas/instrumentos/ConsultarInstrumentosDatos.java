package cl.bcs.fondomutuo.consultas.instrumentos;

import java.util.List;

/** Read port: the three legacy SELECTs, verbatim (fondos, fondos for reinversion, regimes of the series found). */
public interface ConsultarInstrumentosDatos {

    List<ConsultarInstrumentosContrato.OutFondoMutuo> fondos(Long idEmpresa, ConsultarInstrumentosContrato.InFondos filtro, long filtroRegimen, String mostrarBloqueados);

    List<ConsultarInstrumentosContrato.OutFondoMutuo> fondosReinversion(Long idEmpresa, ConsultarInstrumentosContrato.InFondos filtro, long filtroRegimen);

    List<ConsultarInstrumentosContrato.Regimen> regimenes(List<String> idsSerieFondo);
}
