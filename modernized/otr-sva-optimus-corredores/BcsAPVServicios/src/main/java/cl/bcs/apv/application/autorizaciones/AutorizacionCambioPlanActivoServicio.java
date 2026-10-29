package cl.bcs.apv.application.autorizaciones;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CambiarContratosAPlanActivo;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.cambiarcontratosaplanactivo.CambiarContratosAPlanActivoEntrada;
import cl.bcs.apv.api.dto.cambiarcontratosaplanactivo.CambiarContratosAPlanActivoResultado;
import cl.bcs.apv.api.dto.cambiarcontratosaplanactivo.ContratoCambioPlanIn;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.contratos.EventoAutorizacionRepositorio;
import cl.bcs.apv.domain.port.out.contratos.EventoAutorizacionRepositorio.DetalleEventoAutorizacion;

/**
 * Legacy {@code cl.bcs.APV.AutorizacionCambiarAPlanActivoContratoSAAPV} (hook {@code BcsAutorizaciones}). When the approvers accept the "MOPLAC" events that
 * {@code SolicitarCambioAPlanActivo} created, it reads the detail rows of the events ({@code "IdPersona;IdCliente;IdPortafolio;IdContratoSAAPV;folio"}), builds the
 * contract list and runs the plan change ({@link CambiarContratosAPlanActivo}, the legacy called {@code CambiarAPlanActivoContratoSAAPV.logica}).
 *
 * <p>Reproduced bug for bug (the user decision: what the legacy does and sends never changes):
 * <ul>
 *   <li>the detail query is {@code WHERE IdEvento = :idsEvento}: with two or more events the expanded statement is a syntax error, the SQLException is swallowed
 *       (the legacy printed the stack trace) and the result stays null, so the loop over it throws NullPointerException; with an empty list there are no rows and
 *       {@code p_Lista.get(0)} throws IndexOutOfBoundsException;</li>
 *   <li>a detail whose value is null or has fewer than four {@code ;} parts, or whose first four parts are not numbers, throws (NullPointerException,
 *       ArrayIndexOutOfBoundsException, NumberFormatException) OUTSIDE the protected block: the exception reaches the caller;</li>
 *   <li>{@code FolioContratoSAAPV} is filled with the contract ID (part 3), not the folio (part 4);</li>
 *   <li>company, user and module of the plan change come from the FIRST event, the message text from the {@code idEmpresa} of the input;</li>
 *   <li>an event succeeds only when the plan change answered an OK message whose id equals the event's client id (0 when the client is null or 0): the OK
 *       message of the plan change carries the NUMBER OF PORTFOLIOS as id, so for a real client id the event is reported with the messages of the plan change
 *       and no {@code EXITO} even though the contracts were changed. For each event that does not succeed the output takes the counter of the plan change
 *       and its (same) message list, replacing what earlier events added; the legacy {@code logica} leaves the counter 0 when no contract was received;</li>
 *   <li>any exception of the plan change or of the result building is swallowed into {@code contadorErrores = 1}, keeping what had been written to the output;</li>
 *   <li>{@code rechazarEvento} does nothing.</li>
 * </ul>
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AutorizacionCambioPlanActivoServicio implements AutorizacionCambioPlanActivo {

    private static final Logger log = LoggerFactory.getLogger(AutorizacionCambioPlanActivoServicio.class);

    private final DiccionarioMensajes diccionario;
    private final CambiarContratosAPlanActivo cambiarContratosAPlanActivo;
    private final EventoAutorizacionRepositorio repositorio;

    public AutorizacionCambioPlanActivoServicio(DiccionarioMensajes diccionario, CambiarContratosAPlanActivo cambiarContratosAPlanActivo,
            EventoAutorizacionRepositorio repositorio) {
        this.diccionario = diccionario;
        this.cambiarContratosAPlanActivo = cambiarContratosAPlanActivo;
        this.repositorio = repositorio;
    }

    @Override
    public SalidaAutorizacion autorizarEvento(EntradaAutorizacion entrada) {
        log.info("INICIO:AUTORIZACION");
        List<Mensaje> mensajes = new ArrayList<>();
        Long errores = 0L;
        List<ResultadoAutorizacion> resultados = new ArrayList<>();

        // ids of the events whose detail is needed
        List<Long> idsEvento = new ArrayList<>();
        for (EventoAutorizacion evento : entrada.lista()) {
            idsEvento.add(evento.idEvento());
        }

        List<DetalleEventoAutorizacion> detallesEvento = null;
        try {
            detallesEvento = repositorio.consultarDetalleEventos(idsEvento);
        } catch (SQLException e) {
            // LEGACY DEFECT kept: the SQLException is only printed, detallesEvento stays null and the loop below throws NullPointerException
            log.error("Error al leer el detalle de los eventos", e);
        }

        List<ContratoCambioPlanIn> lstContratosCambioPlan = new ArrayList<>();
        for (DetalleEventoAutorizacion detalleEvento : detallesEvento) {
            String[] parts = detalleEvento.Valor().split(";");
            // LEGACY DEFECT kept: the folio is the contract id (parts[3]), not the folio (parts[4])
            lstContratosCambioPlan.add(new ContratoCambioPlanIn(Long.parseLong(parts[0]), Long.parseLong(parts[1]), Long.parseLong(parts[2]),
                    Long.parseLong(parts[3]), parts[3]));
        }

        EventoAutorizacion primero = entrada.lista().get(0);
        CambiarContratosAPlanActivoEntrada entradaConfirmar = new CambiarContratosAPlanActivoEntrada(primero.idEmpresa(), primero.idUsuario(),
                primero.idModulo(), lstContratosCambioPlan);

        try {
            CambiarContratosAPlanActivoResultado salidaConfirmacion = cambiarContratosAPlanActivo.ejecutar(entradaConfirmar);
            // the legacy hook called logica (not call): without contracts logica returns at once with its counter still 0 although it added an error message
            Long contadorLogica = lstContratosCambioPlan.isEmpty() ? Long.valueOf(0L) : salidaConfirmacion.contadorErrores();
            List<Mensaje> mensajesConfirmacion = new ArrayList<>(salidaConfirmacion.mensajes());

            Map<Long, String> hashSalidaConfirmar = new HashMap<>();
            for (Mensaje salida : mensajesConfirmacion) {
                hashSalidaConfirmar.put(salida.id(), salida.tipo());
            }

            for (EventoAutorizacion evento : entrada.lista()) {
                long id = 0L;
                if (!(evento.idCliente() == null) && evento.idCliente().compareTo(0L) != 0) {
                    id = evento.idCliente();
                }
                if (hashSalidaConfirmar.containsKey(id) && hashSalidaConfirmar.get(id).equals(Mensajes.MENSAJE_OK)) {
                    String descripcion = diccionario.mensaje(entrada.idEmpresa(), "OPERACION_AUTORIZADA", new HashMap<>());
                    errores = 0L;
                    mensajes.add(Mensajes.de(0L, Mensajes.MENSAJE_INFORMACION, "OPERACION_AUTORIZADA", descripcion));
                    resultados.add(new ResultadoAutorizacion(evento.idEvento(), ResultadoAutorizacion.EXITO));
                } else {
                    errores = contadorLogica;
                    mensajes = mensajesConfirmacion;
                }
            }
        } catch (Exception e1) {
            errores = 1L;
        }
        log.info("TERMINO:AUTORIZACION");
        return new SalidaAutorizacion(mensajes, errores, resultados);
    }

    /** The rejection of a plan change has no effect (the legacy method has an empty protected block). */
    @Override
    public SalidaAutorizacion rechazarEvento(EntradaAutorizacion entrada) {
        log.info("INICIO:RECHAZO");
        entrada.lista(); // legacy: entrada.p_Lista = entrada.p_Lista (NullPointerException for a null input)
        SalidaAutorizacion salida = new SalidaAutorizacion(new ArrayList<>(), 0L, new ArrayList<>());
        log.info("TERMINO:RECHAZO");
        return salida;
    }
}
