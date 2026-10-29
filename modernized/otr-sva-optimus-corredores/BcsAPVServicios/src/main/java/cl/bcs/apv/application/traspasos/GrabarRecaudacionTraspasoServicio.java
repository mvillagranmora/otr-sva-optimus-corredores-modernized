package cl.bcs.apv.application.traspasos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.AsignarContratoARecaudacion;
import cl.bcs.apv.api.casouso.ConciliarDeposito;
import cl.bcs.apv.api.casouso.CrearRecaudacionCliente;
import cl.bcs.apv.api.casouso.CrearRecaudacionDeposito;
import cl.bcs.apv.api.casouso.GrabarRecaudacionTraspaso;
import cl.bcs.apv.api.casouso.ProcesarDistribucion;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.asignarcontratoarecaudacion.AsignarContratoARecaudacionEntrada;
import cl.bcs.apv.api.dto.asignarcontratoarecaudacion.ParametrosAsignacionContratoRecaudacion;
import cl.bcs.apv.api.dto.conciliardeposito.ConciliarDepositoEntrada;
import cl.bcs.apv.api.dto.conciliardeposito.ConciliarDepositoResultado;
import cl.bcs.apv.api.dto.crearrecaudacioncliente.Cliente;
import cl.bcs.apv.api.dto.crearrecaudacioncliente.CrearRecaudacionClienteEntrada;
import cl.bcs.apv.api.dto.crearrecaudacioncliente.CrearRecaudacionClienteResultado;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.CrearRecaudacionDepositoEntrada;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.CrearRecaudacionDepositoResultado;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.Recaudacion;
import cl.bcs.apv.api.dto.grabarrecaudaciontraspaso.GrabarRecaudacionTraspasoEntrada;
import cl.bcs.apv.api.dto.grabarrecaudaciontraspaso.GrabarRecaudacionTraspasoResultado;
import cl.bcs.apv.api.dto.grabarrecaudaciontraspaso.RecaudacionConciliacion;
import cl.bcs.apv.api.dto.grabarrecaudaciontraspaso.Resultado;
import cl.bcs.apv.api.dto.procesardistribucion.ParametrosRecaudacion;
import cl.bcs.apv.api.dto.procesardistribucion.ProcesarDistribucionEntrada;
import cl.bcs.apv.api.dto.procesardistribucion.ProcesarDistribucionResultado;
import cl.bcs.apv.domain.port.out.traspasos.GrabarRecaudacionTraspasoRepositorio;
import cl.bcs.apv.domain.port.out.traspasos.GrabarRecaudacionTraspasoRepositorio.EstadoContrato;
import cl.bcs.apv.domain.port.out.traspasos.GrabarRecaudacionTraspasoRepositorio.EstadoTraspaso;

/**
 * Use case {@code GrabarRecaudacionTraspaso}: legacy {@code cl.bcs.APV.GrabarRecaudacionTraspaso}. The legacy shares ONE {@code MensajeSalida} instance between
 * several list entries and mutates it later, so the messages are the mutable {@link MensajeLegacy} until the outcome is built. The family C use cases are
 * injected typed (the legacy called their {@code logica}).
 */
@Transactional(rollbackFor = Exception.class)
public class GrabarRecaudacionTraspasoServicio implements GrabarRecaudacionTraspaso {

    private final CrearRecaudacionDeposito crearRecaudacionDeposito;
    private final CrearRecaudacionCliente crearRecaudacionCliente;
    private final ConciliarDeposito conciliarDeposito;
    private final AsignarContratoARecaudacion asignarContratoARecaudacion;
    private final ProcesarDistribucion procesarDistribucion;
    private final GrabarRecaudacionTraspasoRepositorio repositorio;

    public GrabarRecaudacionTraspasoServicio(CrearRecaudacionDeposito crearRecaudacionDeposito, CrearRecaudacionCliente crearRecaudacionCliente,
            ConciliarDeposito conciliarDeposito, AsignarContratoARecaudacion asignarContratoARecaudacion, ProcesarDistribucion procesarDistribucion,
            GrabarRecaudacionTraspasoRepositorio repositorio) {
        this.crearRecaudacionDeposito = crearRecaudacionDeposito;
        this.crearRecaudacionCliente = crearRecaudacionCliente;
        this.conciliarDeposito = conciliarDeposito;
        this.asignarContratoARecaudacion = asignarContratoARecaudacion;
        this.procesarDistribucion = procesarDistribucion;
        this.repositorio = repositorio;
    }

    @SuppressWarnings("StringEquality")
    @Override
    public GrabarRecaudacionTraspasoResultado ejecutar(GrabarRecaudacionTraspasoEntrada in) {
        List<MensajeLegacy> mensajes = new ArrayList<>();
        List<Resultado> idResultado = new ArrayList<>();
        MensajeLegacy msg = new MensajeLegacy();
        Long idRecaudacion = 0L;
        String folioContratoSAAPV = "";
        Long idContratoSAAPV = 0L;
        Long idTraspaso = 0L;
        List<Recaudacion> lstParamsRecaudacion = new ArrayList<>();
        List<Cliente> lstParamsRecaudacionCliente = new ArrayList<>();
        List<EstadoTraspaso> lstTraspaso = new ArrayList<>();
        List<EstadoContrato> lstContrato = new ArrayList<>();

        RecaudacionConciliacion primera = in.recaudacionConciliacion().get(0);
        Recaudacion recaudacionEntrada = new Recaudacion(0L, 0L, in.idEmpresa(), null,
                primera.folioCartola(),
                primera.lineaCartola(),
                primera.idSucursal() == null ? "" : primera.idSucursal().toString(),
                primera.fechaIngreso(),
                0L,
                primera.idBanco(),
                primera.cuentaCorriente().toString(),
                BigDecimal.valueOf(primera.montoDeposito()),
                "",
                in.origen(),
                "",
                primera.idContratoSAAPV(),
                primera.idTipoDeposito(),
                primera.idRegimenTributario(),
                0L,
                "ING");
        lstParamsRecaudacion.add(recaudacionEntrada);

        /* Despues de crear la relacion entre un contrato saapv de tipo traspaso y el portafolio, se debe crear en recaudacion y recaudacion cliente */
        CrearRecaudacionDepositoResultado salidaDeposito = crearRecaudacionDeposito.ejecutar(
                new CrearRecaudacionDepositoEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), "I", in.origen(), lstParamsRecaudacion));

        for (RecaudacionConciliacion fila : in.recaudacionConciliacion()) {
            // LEGACY DEFECT kept (see README APV-B-304): the messages of the deposit service are never read; a null P_OUT_IDREC makes this unboxing throw NullPointerException
            if (salidaDeposito.idRec() > 0) {
                idRecaudacion = salidaDeposito.idRec();

                idResultado.add(new Resultado("REC", "Recaudacion", idRecaudacion));

                BigDecimal convenido = BigDecimal.valueOf(fila.regimen().contains("DC") == true ? fila.montoDetalle() : 0L);
                BigDecimal apv = BigDecimal.valueOf(fila.regimen().contains("APV") == true ? fila.montoDetalle() : fila.regimen().contains("BF") ? fila.montoDetalle() : 0L);
                lstParamsRecaudacionCliente.add(new Cliente(0L, 0L, idRecaudacion, 0L, in.idEmpresa(), fila.idCliente(), fila.fechaIngreso(), fila.tipoAfiliado(),
                        fila.regimen(), convenido, apv, fila.origen(), "ING"));
            } else {
                msg.id = 2L;
                msg.tipo = "ERROR";
                msg.codigo = "ERROR_CREAR_RECAUDACION";
                msg.descripcion = "ERROR al crear registro Recaudacion.";
                mensajes.add(msg);
            }

            /* Se llena Lista para cambiar Estado Contrato Traspaso y Traspaso */
            // LEGACY DEFECT kept (see README APV-B-314): String and Long compared by REFERENCE (!=): with JSON-built values and ids above 127 the test is always true
            if (fila.folioContratoSAAPV() != folioContratoSAAPV && fila.idTraspaso() != idTraspaso) {
                folioContratoSAAPV = fila.folioContratoSAAPV();
                idTraspaso = fila.idTraspaso();
                idContratoSAAPV = fila.idContratoSAAPV();

                EstadoTraspaso traspaso = new EstadoTraspaso();
                traspaso.IdContratoSAAPV = fila.idContratoSAAPV();
                traspaso.IdEmpresa = in.idEmpresa();
                traspaso.IdTraspaso = fila.idTraspaso();
                traspaso.IdEstado = "COM";
                traspaso.CierreSolicitud = in.fecha();
                lstTraspaso.add(traspaso);
            }

            EstadoContrato contrato = new EstadoContrato();
            contrato.IdPersona = fila.idPersona();
            contrato.IdEmpleador = fila.idEmpleador();
            contrato.IdFolioContratoSAAPV = fila.folioContratoSAAPV();
            contrato.IdEstado = "COM";
            contrato.IdEstadoEnvioNotificacion = "";
            contrato.IdEstadoSolicitud = "COM";
            lstContrato.add(contrato);
        }

        CrearRecaudacionClienteResultado salidaRecaudacionCliente = crearRecaudacionCliente.ejecutar(
                new CrearRecaudacionClienteEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), "", in.origen(), lstParamsRecaudacionCliente));

        // LEGACY DEFECT kept (see README APV-B-309): the CrearRecaudacionCliente messages are typed APV, so its counter is 0 and this branch is unreachable
        if (salidaRecaudacionCliente.contadorErrores() != null && salidaRecaudacionCliente.contadorErrores() > 0) {
            msg.id = 2L;
            msg.tipo = "ERROR";
            msg.codigo = "ERROR_RECAUDACION_CLIENTE";
            msg.descripcion = "Problema al crear registro Recaudacion.";
            mensajes.add(msg);
        } else {
            /* Una vez que se envio a Recaudar se debe enviar a Conciliar */
            List<cl.bcs.apv.api.dto.conciliardeposito.Recaudacion> paramsConci = new ArrayList<>();
            paramsConci.add(new cl.bcs.apv.api.dto.conciliardeposito.Recaudacion(idRecaudacion, null));
            ConciliarDepositoResultado conciliarResultado = conciliarDeposito
                    .ejecutar(new ConciliarDepositoEntrada(in.idEmpresa(), in.idUsuario(), "APV", "N", "N", paramsConci));

            for (Mensaje fila : conciliarResultado.mensajes()) {
                msg = new MensajeLegacy();
                msg.id = idRecaudacion;
                msg.tipo = "OK";
                msg.codigo = fila.codigo();
                msg.descripcion = fila.descripcion();
                mensajes.add(msg);

                if (fila.codigo().equals("CONCILIADO_OK")) {
                    /* Una vez que se envio a Conciliar se debe relacionar el Contrato con la Recaudacion */
                    List<ParametrosAsignacionContratoRecaudacion> lstParamsContrRecaudacion = new ArrayList<>();
                    for (cl.bcs.apv.api.dto.crearrecaudacioncliente.Resultado resultado : salidaRecaudacionCliente.resultado()) {
                        lstParamsContrRecaudacion.add(new ParametrosAsignacionContratoRecaudacion(resultado.idRecaudacionCliente(), idContratoSAAPV, 0L));
                    }
                    asignarContratoARecaudacion.ejecutar(new AsignarContratoARecaudacionEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), in.origen(), "N",
                            lstParamsContrRecaudacion));

                    /* Una vez que se envio a Conciliar se debe enviar a Distribuir */
                    List<ParametrosRecaudacion> listRec = new ArrayList<>();
                    listRec.add(new ParametrosRecaudacion(idRecaudacion, null, null));
                    ProcesarDistribucionResultado procesarDistribucionResultado = procesarDistribucion.ejecutar(new ProcesarDistribucionEntrada(in.idEmpresa(),
                            in.idUsuario(), in.idModulo(), "RECTRA", listRec, "N", "", "N", "N", "N", in.idsDetalleTraspasoReapertura(), "S", "OPT"));

                    for (Mensaje row : procesarDistribucionResultado.mensajes()) {

                        if ("ERROR".equalsIgnoreCase(row.tipo())) {
                            // LEGACY DEFECT kept (see README APV-B-308): the shared instance is overwritten, so the CONCILIADO_OK message already added becomes this error
                            msg.id = 2L;
                            msg.tipo = "ERROR";
                            msg.codigo = "ERROR_RECAUDACION_CLIENTE";
                            msg.descripcion = "Problema en el proceso de distribucion.";
                            mensajes.add(MensajeLegacy.de(row));
                        } else {

                            msg = new MensajeLegacy();
                            msg.id = idRecaudacion;
                            msg.tipo = "OK";
                            msg.codigo = row.codigo();
                            msg.descripcion = row.descripcion();
                            mensajes.add(msg);

                            // legacy: row.tipo == "OK" (the tipo literals of in-process services are interned: a value comparison here)
                            if ("OK".equals(row.tipo())) {
                                /* Una vez enviado a Recaudar, Conciliar y Distribuir se debe cambiar el Estado del Traspaso y el Contrato Traspaso */
                                int[] result = repositorio.cambiarEstadoTraspasos(lstTraspaso);
                                if (result != null && result.length > 0 && result[0] == 1) {
                                    msg = new MensajeLegacy();
                                    msg.id = idRecaudacion;
                                    msg.tipo = "OK";
                                    msg.codigo = "CAMBIAR_ESTADO_TRASPASO";
                                    msg.descripcion = "Cambio de Estado de Traspaso exitoso Folio: " + idTraspaso.toString();
                                    mensajes.add(msg);

                                    int[] resultadoContrato = repositorio.cambiarEstadoContratos(lstContrato);
                                    if (resultadoContrato != null && resultadoContrato.length > 0 && resultadoContrato[0] == 1) {
                                        msg = new MensajeLegacy();
                                        msg.id = idRecaudacion;
                                        msg.tipo = "OK";
                                        msg.codigo = "CAMBIAR_ESTADO_CONTRATO_TRASPASO";
                                        msg.descripcion = "Cambio de Estado del Contrato Traspaso exitoso Folio : " + folioContratoSAAPV.toString();
                                        mensajes.add(msg);
                                    } else {
                                        // LEGACY DEFECT kept (see README APV-B-310): a failed contract update leaves the traspaso already COM
                                        msg = new MensajeLegacy();
                                        msg.id = 2L;
                                        msg.tipo = "ERROR";
                                        msg.codigo = "ERROR_CAMBIO_ESTADO_CONTRATO_TRASPASO";
                                        msg.descripcion = "No es posible cambiar el Estado al Contrato Traspaso Folio : " + folioContratoSAAPV.toString();
                                        mensajes.add(msg);
                                    }
                                } else {
                                    msg = new MensajeLegacy();
                                    msg.id = 2L;
                                    msg.tipo = "ERROR";
                                    msg.codigo = "ERROR_CAMBIO_ESTADO_TRASPASO";
                                    msg.descripcion = "No es posible cambiar el Estado al Traspaso.";
                                    mensajes.add(msg);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        // the answer counter is the legacy call(): getNumeroErrores(P_OUT_MENSAJES) over the final state of the (shared) instances
        return new GrabarRecaudacionTraspasoResultado(MensajeLegacy.aMensajes(mensajes), MensajeLegacy.contarErrores(mensajes), idResultado);
    }
}
