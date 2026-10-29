package cl.bcs.apv.application.contratos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.apv.api.casouso.BuscarContratosParaRecaudacion;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.buscarcontratospararecaudacion.AsignarContratoRecaudacion;
import cl.bcs.apv.api.dto.buscarcontratospararecaudacion.BuscarContratosParaRecaudacionEntrada;
import cl.bcs.apv.api.dto.buscarcontratospararecaudacion.BuscarContratosParaRecaudacionResultado;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.contratos.BusquedaContratoRepositorio;
import cl.bcs.apv.domain.port.out.contratos.BusquedaContratoRepositorio.ContratoFila;
import cl.bcs.apv.domain.port.out.contratos.BusquedaContratoRepositorio.RecaudacionFila;

/** Use case {@code BuscarContratosParaRecaudacion} (legacy {@code cl.bcs.APV.BusquedaContratoSAAPVRecaudacion}); read only. */
public class BuscarContratosParaRecaudacionServicio implements BuscarContratosParaRecaudacion {

    private final BusquedaContratoRepositorio repositorio;
    private final DiccionarioMensajes diccionario;

    public BuscarContratosParaRecaudacionServicio(BusquedaContratoRepositorio repositorio, DiccionarioMensajes diccionario) {
        this.repositorio = repositorio;
        this.diccionario = diccionario;
    }

    @Override
    public BuscarContratosParaRecaudacionResultado ejecutar(BuscarContratosParaRecaudacionEntrada in) {
        List<Mensaje> mensajes = new ArrayList<>();
        List<AsignarContratoRecaudacion> resultado = new ArrayList<>();
        logica(in, mensajes, resultado);
        // legacy call(): the counter is set after logica
        return new BuscarContratosParaRecaudacionResultado(mensajes, Mensajes.numeroErrores(mensajes), resultado);
    }

    private void logica(BuscarContratosParaRecaudacionEntrada in, List<Mensaje> mensajes, List<AsignarContratoRecaudacion> resultado) {
        if (in.idsRecaudacion().size() == 0) {
            mensajes.add(Mensajes.conDiccionario(diccionario, in.idEmpresa(), 0L, Mensajes.MENSAJE_ERROR, "UI_NO_HAY_RECAUDACIONES", null));
            return;
        }
        List<RecaudacionFila> recaudaciones = repositorio.recaudaciones(in.idsRecaudacion());
        List<Long> idsClientes = recaudaciones.stream().map(e -> e.IdCliente()).collect(Collectors.toList());
        List<ContratoFila> contratos = repositorio.contratos(idsClientes);
        resultado.addAll(asignarContratos(contratos, recaudaciones));
    }

    private static List<AsignarContratoRecaudacion> asignarContratos(List<ContratoFila> contratos, List<RecaudacionFila> recaudaciones) {
        List<AsignarContratoRecaudacion> asignados = new ArrayList<>();
        for (RecaudacionFila recaudacion : recaudaciones) {
            List<ContratoFila> contratosCliente;
            if (recaudacion.IdRegimenTributario().equalsIgnoreCase("APV") || recaudacion.IdRegimenTributario().equalsIgnoreCase("DC")) {
                contratosCliente = contratos.stream().filter(item -> item.IdCliente().equals(recaudacion.IdCliente())
                        && item.TipoContrato().equalsIgnoreCase(recaudacion.IdRegimenTributario())
                        && item.CodigoMedioRecaudacion().equalsIgnoreCase(recaudacion.DetalleRecaudacion())).collect(Collectors.toList());
            } else {
                contratosCliente = contratos.stream().filter(item -> item.IdCliente().equals(recaudacion.IdCliente())
                        && item.IdRegimenTributario().equalsIgnoreCase(recaudacion.IdRegimenTributario())
                        && item.CodigoMedioRecaudacion().equalsIgnoreCase(recaudacion.DetalleRecaudacion())).collect(Collectors.toList());
            }
            // LEGACY DEFECT kept (see README, BusquedaContratoSAAPVRecaudacion): an ambiguous search (2 or more contracts) answers the empty contract, id 0
            ContratoFila contrato = contratosCliente.size() > 1 || contratosCliente.size() == 0 ? ContratoFila.vacio() : contratosCliente.get(0);
            asignados.add(new AsignarContratoRecaudacion(recaudacion.IdRecaudacion(), recaudacion.IdRecaudacionCliente(), contrato.IdContratoSAAPV(), null));
        }
        return asignados;
    }
}
