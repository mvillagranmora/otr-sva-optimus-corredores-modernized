package cl.bcs.fondomutuo.consultas.instrumentos;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;

/**
 * Legacy {@code ConsultarInstrumentosFondosMutuos.logica} (FM-IFM-01..04, read only): the funds (series) that match the filter, each with its regimes. Bug for
 * bug: a missing filter is the message {@code UI_SIN_VALORES} before any read, a null regime list is a NullPointerException (FM-IFM-01), any
 * {@code IdSerieFondoOrigen} other than the empty text selects the reinversion SQL (D-F3A-13), the regimes are read once with the ids of the series found and
 * matched by {@code equals}. The counter is {@code ContadorErrores} (D-F3A-12/27). No state; the unit of work is the ambient read-only transaction.
 */
public class ConsultarInstrumentosService implements ConsultarInstrumentosPort {

    private final DiccionarioMensajesPort diccionario;
    private final ConsultarInstrumentosDatos datos;

    public ConsultarInstrumentosService(DiccionarioMensajesPort diccionario, ConsultarInstrumentosDatos datos) {
        this.diccionario = diccionario;
        this.datos = datos;
    }

    @Override
    public ConsultarInstrumentosContrato.Salida logica(ConsultarInstrumentosContrato.Entrada entrada) throws Exception {
        ConsultarInstrumentosContrato.Salida out = new ConsultarInstrumentosContrato.Salida();

        if (entrada.p_EntradaFondosMutuos == null) {
            ConsultarInstrumentosContrato.MensajesErrores m = new ConsultarInstrumentosContrato.MensajesErrores();
            m.IdInterno = 0L;
            m.TipoMensaje = MensajeSalida.MENSAJE_ERROR;
            m.Codigo = "UI_SIN_VALORES";
            m.Mensaje = diccionario.mensaje(entrada.p_IdEmpresa, "UI_SIN_VALORES", new HashMap<String, String>());
            out.Mensajes.add(m);
            out.ContadorErrores = 1L;
            return out;
        }

        ConsultarInstrumentosContrato.InFondos filtro = entrada.p_EntradaFondosMutuos;
        long filtroRegimen = filtro.IdRegimenFondoMutuo.size() > 0 ? 1L : 0L;   // a null list is a NullPointerException, as the legacy
        if ("".equals(filtro.IdSerieFondoOrigen)) {
            String mostrarBloqueados = "S".equalsIgnoreCase(filtro.OcultarBloqueados) ? "N" : "S";
            out.FondosMutuos = datos.fondos(entrada.p_IdEmpresa, filtro, filtroRegimen, mostrarBloqueados);
        } else {
            out.FondosMutuos = datos.fondosReinversion(entrada.p_IdEmpresa, filtro, filtroRegimen);
        }

        List<String> serieFondo = out.FondosMutuos.stream().map(serie -> serie.IdSerieFondo).collect(Collectors.toList());
        List<ConsultarInstrumentosContrato.Regimen> regimenes = datos.regimenes(serieFondo);

        out.FondosMutuos.forEach(salida -> salida.ListaRegimen = regimenes.stream().filter(regimen -> regimen.IdSerieFondo.equals(salida.IdSerieFondo)).map(regimen -> {
            ConsultarInstrumentosContrato.OutListaRegimen outRegimen = new ConsultarInstrumentosContrato.OutListaRegimen();
            outRegimen.DescripcionRegimen = regimen.DescripcionRegimen;
            outRegimen.IdRegimen = regimen.IdRegimenFondoMutuo;
            outRegimen.PermiteMLT = regimen.PermiteMLT;
            return outRegimen;
        }).collect(Collectors.toList()));

        return out;
    }
}
