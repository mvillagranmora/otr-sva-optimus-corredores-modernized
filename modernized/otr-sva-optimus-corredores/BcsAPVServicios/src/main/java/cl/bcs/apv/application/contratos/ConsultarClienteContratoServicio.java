package cl.bcs.apv.application.contratos;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.apv.api.casouso.ConsultarClienteContrato;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.consultarclientecontrato.ClienteContratoSAAPV;
import cl.bcs.apv.api.dto.consultarclientecontrato.ConsultarClienteContratoEntrada;
import cl.bcs.apv.api.dto.consultarclientecontrato.ConsultarClienteContratoResultado;
import cl.bcs.apv.api.dto.consultarclientecontrato.ContratoSAAPV;
import cl.bcs.apv.api.dto.consultarclientecontrato.ContratoSAAPVModal;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.domain.port.out.contratos.ClienteContratoRepositorio;
import cl.bcs.apv.domain.port.out.contratos.ClienteContratoRepositorio.ClienteFila;
import cl.bcs.apv.domain.port.out.contratos.ClienteContratoRepositorio.ContratoFila;
import cl.bcs.apv.domain.port.out.contratos.ClienteContratoRepositorio.ModalFila;

/** Use case {@code ConsultarClienteContrato} (legacy {@code cl.bcs.APV.ConsultarClienteContratoSAAPV}); read only. */
public class ConsultarClienteContratoServicio implements ConsultarClienteContrato {

    private final ClienteContratoRepositorio repositorio;

    public ConsultarClienteContratoServicio(ClienteContratoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public ConsultarClienteContratoResultado ejecutar(ConsultarClienteContratoEntrada in) {
        List<Mensaje> mensajes = new ArrayList<>();
        List<ClienteContratoSAAPV> clientes = new ArrayList<>();
        List<ContratoSAAPV> contratos = new ArrayList<>();
        List<ContratoSAAPVModal> modal = new ArrayList<>();

        List<Long> idPersonaList = new ArrayList<>();
        int filtroIdPersona = 0;
        if (in.personaPortafolio().size() > 0) {
            idPersonaList = in.personaPortafolio().stream().filter(pp -> pp.idPersona() != null).map(pp -> pp.idPersona()).distinct().collect(Collectors.toList());
            filtroIdPersona = idPersonaList.size();
        }

        if ("GRILLA".equals(in.consulta())) {
            List<ClienteFila> filas = repositorio.clientes(in.idEmpresa(), 1, filtroIdPersona, idPersonaList);
            HashMap<String, Boolean> hash = new HashMap<>();
            for (ClienteFila c : filas) {
                if (!hash.containsKey(c.IdPersona() + "_" + c.IdPortafolio())) {
                    hash.put(c.IdPersona() + "_" + c.IdPortafolio(), true);
                    clientes.add(new ClienteContratoSAAPV(c.IdPersona(), c.RutCliente(), c.NombrePersona(), c.IdPortafolio(), c.Producto()));
                }
            }
        } else if ("SUBGRILLA".equals(in.consulta()) || "MODAL".equals(in.consulta())) {
            List<Long> idPortafolioList = new ArrayList<>();
            int filtroIdPortafolio = 0;
            if (in.personaPortafolio().size() > 0) {
                idPortafolioList = in.personaPortafolio().stream().filter(pp -> pp.idPortafolio() != null).map(pp -> pp.idPortafolio()).distinct()
                        .collect(Collectors.toList());
                filtroIdPortafolio = idPortafolioList.size();
            }
            if ("SUBGRILLA".equals(in.consulta())) {
                List<ContratoFila> filas = repositorio.contratos(in.idEmpresa(), 1, filtroIdPersona, idPersonaList, filtroIdPortafolio, idPortafolioList);
                for (ContratoFila contrato : filas) {
                    contrato.NombreMedioRecaudacion = "DIRECTA".equals(contrato.FormaDePago) ? contrato.NombreMedioRecaudacion : null;
                    contrato.DiaDescuento = "DIRECTA".equals(contrato.FormaDePago) ? "PAC".equals(contrato.NombreMedioRecaudacion) ? contrato.DiaDescuento : null : null;
                    contrato.Institucion = "INDIRECTA".equals(contrato.FormaDePago) ? contrato.Institucion : null;
                    contrato.ComisionTransferencia = "INDIRECTA".equals(contrato.FormaDePago) ? contrato.ComisionTransferencia : null;
                    contrato.EstadoNuevo = "VCD".equals(contrato.IdEstadoEvtAut) || "VIG".equals(contrato.IdEstadoEvtAut) ? "PENDIENTE DE APROBACION"
                            : "RZD".equals(contrato.IdEstadoEvtAut) ? "RECHAZADO" : contrato.NombreEstadoAPV;
                }
                // the legacy NullPointerException on a contract without id (comparing a null Long) is kept
                filas.sort(Comparator.comparing((ContratoFila c) -> c.IdContratoSAAPV));
                for (ContratoFila c : filas) {
                    contratos.add(aContrato(c));
                }
            } else {
                List<ModalFila> filas = repositorio.modal(in.idEmpresa(), 1, filtroIdPersona, idPersonaList, filtroIdPortafolio, idPortafolioList);
                filas.sort(Comparator.comparing((ModalFila m) -> m.IdContratoSAAPV()));
                for (ModalFila m : filas) {
                    modal.add(new ContratoSAAPVModal(m.IdPersona(), m.RutCliente(), m.NombrePersona(), m.IdPortafolio(), m.Producto(), m.IdContratoSAAPV(),
                            m.FolioContratoSAAPV()));
                }
            }
        }
        // legacy call(): the counter is set after logica
        return new ConsultarClienteContratoResultado(mensajes, Mensajes.numeroErrores(mensajes), clientes, contratos, modal);
    }

    private static LocalDate fecha(Date d) {
        return d == null ? null : d.toLocalDate();
    }

    private static ContratoSAAPV aContrato(ContratoFila c) {
        return new ContratoSAAPV(c.IdContratoSAAPV, c.FolioContratoSAAPV, c.Folio, c.NombrePlan, c.RutEmpleador, c.NombreEmpleador, c.NombreEncargado,
                fecha(c.FechaSuscripcion), fecha(c.FechaCreacion), fecha(c.FechaAprobacion), fecha(c.FechaNotificacion), fecha(c.FechaRecepcion),
                c.OrigenAhorro, c.MontoPesos, c.MontoUF, c.PorcentajeRegimenImponible, c.Pago, c.PeriodoInicio, c.PeriodoTermino, c.RegimenTributario,
                c.FormaDePago, c.NombreMedioRecaudacion, c.DiaDescuento, c.Institucion, c.ComisionTransferencia, c.CodigoTipoSolicitud, c.RutEntidadOrigen,
                c.NombreEntidadOrigen, c.TipoDeAhorro, c.FondoDeOrigen, c.TipoTraspaso, c.MontoTraspaso, c.UFTraspaso, c.PorcentajeTraspaso,
                c.EstadoNotificacion, c.IdEstadoEvtAut, c.NombreEstadoAPV, c.EstadoNuevo, c.EstadoPAC);
    }
}
