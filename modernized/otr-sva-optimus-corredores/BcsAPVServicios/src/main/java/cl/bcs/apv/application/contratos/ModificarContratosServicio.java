package cl.bcs.apv.application.contratos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.apv.api.casouso.CambiarEstadoContratos;
import cl.bcs.apv.api.casouso.ModificarContratos;
import cl.bcs.apv.api.casouso.ValidarContrato;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.cambiarestadocontratos.CambiarEstadoContratosEntrada;
import cl.bcs.apv.api.dto.modificarcontratos.DetalleOrigenTraspaso;
import cl.bcs.apv.api.dto.modificarcontratos.DistribucionInversion;
import cl.bcs.apv.api.dto.modificarcontratos.ModificarContratosEntrada;
import cl.bcs.apv.api.dto.modificarcontratos.ModificarContratosResultado;
import cl.bcs.apv.api.dto.modificarcontratos.ParametrosContrato;
import cl.bcs.apv.api.dto.modificarcontratos.PortafoliosModificados;
import cl.bcs.apv.api.dto.validarcontrato.ModificaContratoSAAPV;
import cl.bcs.apv.api.dto.validarcontrato.ValidarContratoEntrada;
import cl.bcs.apv.api.dto.validarcontrato.ValidarContratoResultado;
import cl.bcs.apv.application.comun.Json;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.application.comun.Parametros;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.Secuencias;
import cl.bcs.apv.domain.port.out.contratos.ModificarContratosRepositorio;
import cl.bcs.apv.domain.port.out.contratos.ModificarContratosRepositorio.ContratoEstado;
import cl.bcs.apv.domain.port.out.contratos.ModificarContratosRepositorio.ContratoModificado;
import cl.bcs.apv.domain.port.out.contratos.ModificarContratosRepositorio.DetalleOrigenNuevo;
import cl.bcs.apv.domain.port.out.contratos.ModificarContratosRepositorio.DistribucionNueva;
import cl.bcs.apv.domain.port.out.contratos.ModificarContratosRepositorio.PortafolioContratoNuevo;
import cl.bcs.apv.domain.port.out.contratos.ModificarContratosRepositorio.TraspasoEgresoModificado;
import cl.bcs.apv.domain.port.out.contratos.ModificarContratosRepositorio.TraspasoNuevo;

/**
 * Use case {@code ModificarContratos}: legacy {@code cl.bcs.APV.ModificarContratoSAAPV} ({@code call()} = {@link #ejecutar}, {@code logica} = {@link #logica}).
 *
 * <p>The legacy mutated its input objects (the persona/cliente/empleador and the folio of the contract, the id of the portfolio, the contract id of a
 * distribution) and later steps of the SAME call read the mutated values; the records of the contract are immutable, so the mutated values live in the
 * private holders {@link Fila}, {@link Port} and {@link Dist}, shared by every later step exactly as the legacy shared the objects.
 *
 * <p>The legacy ran on one connection committed by the platform: here the whole use case is one transaction ({@code rollbackFor = Exception.class}).
 */
@Transactional(rollbackFor = Exception.class)
public class ModificarContratosServicio implements ModificarContratos {

    private static final String SEC_ID_TRASPASO = "BSCL.APV.BD.Secuencias::TRA.IdTraspaso";
    private static final String SEC_ID_DETALLE_ORIGEN = "BSCL.APV.BD.Secuencias::TRA.IdDetalleOrigenTraspaso";
    private static final String PROCEDIMIENTO_CREAR_PORTAFOLIO = "BSCL.Portafolio.BD.Servicios::CrearPortafolio";

    private final ConfiguracionSistema configuracion;
    private final Secuencias secuencias;
    private final Colaboradores colaboradores;
    private final ValidarContrato validarContrato;
    private final CambiarEstadoContratos cambiarEstadoContratos;
    private final ModificarContratosRepositorio repositorio;

    public ModificarContratosServicio(ConfiguracionSistema configuracion, Secuencias secuencias, Colaboradores colaboradores,
            ValidarContrato validarContrato, CambiarEstadoContratos cambiarEstadoContratos, ModificarContratosRepositorio repositorio) {
        this.configuracion = configuracion;
        this.secuencias = secuencias;
        this.colaboradores = colaboradores;
        this.validarContrato = validarContrato;
        this.cambiarEstadoContratos = cambiarEstadoContratos;
        this.repositorio = repositorio;
    }

    /** The contract row with the values the legacy mutated. */
    private static final class Fila {
        final ParametrosContrato b;
        Long idPersona;
        Long idCliente;
        Long idEmpleador;
        String folioContratoSAAPV;
        String folio;

        Fila(ParametrosContrato b) {
            this.b = b;
            this.idPersona = b.idPersona();
            this.idCliente = b.idCliente();
            this.idEmpleador = b.idEmpleador();
            this.folioContratoSAAPV = b.folioContratoSAAPV();
            this.folio = b.folio();
        }
    }

    private static final class Port {
        final PortafoliosModificados b;
        Long idPortafolio;

        Port(PortafoliosModificados b) {
            this.b = b;
            this.idPortafolio = b.idPortafolio();
        }
    }

    private static final class Dist {
        final DistribucionInversion b;
        Long idContratoSAAPV;

        Dist(DistribucionInversion b) {
            this.b = b;
            this.idContratoSAAPV = b.idContratoSAAPV();
        }
    }

    @Override
    public ModificarContratosResultado ejecutar(ModificarContratosEntrada entrada) {
        List<Mensaje> mensajes = logica(entrada);
        return new ModificarContratosResultado(mensajes, Mensajes.numeroErrores(mensajes));
    }

    private static <T, R> List<R> envolver(List<T> lista, java.util.function.Function<T, R> f) {
        if (lista == null) {
            return null; // the legacy iterates the null list later: NullPointerException there
        }
        List<R> out = new ArrayList<>();
        for (T x : lista) {
            out.add(f.apply(x));
        }
        return out;
    }

    private List<Mensaje> logica(ModificarContratosEntrada in) {
        List<Mensaje> salida = new ArrayList<>();
        long refIdPortafolio = 0L; // SalidaServicioCrearCliente.P_OUT_REFIDPORTAFOLIO: one object for the whole call (stale value kept)

        String apruebaTraspasoAutomatico = Parametros.texto(configuracion, in.idEmpresa(), Parametros.APV, "APV.ApruebaTraspasoAutomatico");

        List<Fila> filas = envolver(in.parametros(), Fila::new);
        List<Port> portafolios = envolver(in.portafoliosModificados(), Port::new);
        List<Dist> distribuciones = envolver(in.distribucionInversion(), Dist::new);
        List<DetalleOrigenTraspaso> detalleOrigen = in.detalleOrigenTraspaso();

        for (Fila fila : filas) {
            Long idContratoSAAPVBorrar = fila.b.idContratoSAAPV();

            // *** VALIDACION CONTRATOS SAAPV ***
            List<cl.bcs.apv.api.dto.validarcontrato.ContratosSAAPV> tablaValidaContrato = new ArrayList<>();
            for (Port filaValidaPortafolio : portafolios) {
                if (idContratoSAAPVBorrar.longValue() != filaValidaPortafolio.b.idContratoSAAPVOrigen().longValue()) {
                    continue;
                }
                // Se envia solo IdPortafolio NULL para que SOLO valide el portafolio
                if (filaValidaPortafolio.idPortafolio == 0L) {
                    tablaValidaContrato.add(new cl.bcs.apv.api.dto.validarcontrato.ContratosSAAPV(fila.idPersona, fila.idCliente, fila.idEmpleador, null,
                            fila.b.documentoIdentidadCliente(), fila.b.documentoIdentidadEmpleador(), null, filaValidaPortafolio.b.regimen(),
                            fila.b.nombrePlan(), filaValidaPortafolio.b.codigoAgrupador()));
                }
            }
            ValidarContratoEntrada porDefecto = ValidarContratoEntrada.porDefecto();
            ValidarContratoResultado outValidaContrato = validarContrato.ejecutar(new ValidarContratoEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(),
                    "M", tablaValidaContrato, porDefecto.recaudacionCliente(), porDefecto.idsDetalleTraspasoReapertura()));

            // LEGACY DEFECT kept (see README APV-A-ModificarContrato): the legacy asked "P_OUT_ERRORESCONTADOR == null" of ValidaContratoSAAPV.logica, which never
            // fills the counter (only call() does), so the condition is always true and the identifiers it returned are always applied. ejecutar fills the counter,
            // so the condition is not evaluated here.
            for (ModificaContratoSAAPV filaValidaContrato : outValidaContrato.idsContrato()) {
                if (filaValidaContrato.idPersona() != null) {
                    fila.idPersona = filaValidaContrato.idPersona();
                }
                if (filaValidaContrato.idCliente() != null) {
                    fila.idCliente = filaValidaContrato.idCliente();
                }
                if (filaValidaContrato.idEmpleador() != null) {
                    fila.idEmpleador = filaValidaContrato.idEmpleador();
                }
                if (filaValidaContrato.codigo() != null) {
                    for (Port filaSeteaPortafolio : portafolios) {
                        if (idContratoSAAPVBorrar.longValue() != filaSeteaPortafolio.b.idContratoSAAPVOrigen().longValue()) {
                            continue;
                        }
                        if (filaValidaContrato.codigo().compareTo(filaSeteaPortafolio.b.regimen()) == 0 && filaValidaContrato.idPortafolio() != null) {
                            filaSeteaPortafolio.idPortafolio = filaValidaContrato.idPortafolio();
                        }
                    }
                }
            }
            if (fila.b.idMedioRecaudacionFormaPago() == null && !fila.b.tipoMovimiento().equals("EGRESO")) {
                salida.add(Mensajes.de(null, "ERROR", "ERROR_FORMA_PAGO_NO_EXISTE", "No existe Forma de pago de medio recaudación."));
                return salida;
            }
            // *** VALIDACION CONTRATOS SAAPV - FIN ***
            if ("".equalsIgnoreCase(fila.folioContratoSAAPV)) {
                fila.folioContratoSAAPV = fila.folio;
            } else if ("".equalsIgnoreCase(fila.folio)) {
                fila.folio = fila.folioContratoSAAPV;
            }
            ContratoModificado contrato = contratoModificado(fila);

            // estado del contrato en linea previo a la modificacion
            List<ContratoEstado> contratoConsultado = repositorio.consultarContrato(idContratoSAAPVBorrar);

            repositorio.modificarContrato(contrato);
            if (!filas.isEmpty()) {
                Fila primera = filas.get(0);
                if (primera.b.codigoTipoSolicitud() != null && primera.b.tipoMovimiento() != null && !primera.b.codigoTipoSolicitud().equals("TRA")
                        && !primera.b.tipoMovimiento().equals("EGRESO")) {
                    agregarLogSAAPV(in, filas, distribuciones, portafolios, detalleOrigen);
                }
            }

            salida.add(Mensajes.de(1L, "OK", "MODIFICACION_CONTRATO_OK", "CONTRATO " + fila.folioContratoSAAPV + ": Modificacion de contrato exitosa"));

            for (ContratoEstado linea : contratoConsultado) {
                if (linea.idEstado().compareTo("PDI") == 0 || linea.idEstado().compareTo("PEN") == 0) {
                    // ===== MODIFICA TRASPASO =====
                    if (fila.b.codigoTipoSolicitud().equals("TRA")) {
                        Long idTraspasoBorrar = 0L;
                        for (Long idTraspasoLinea : repositorio.consultarIdsTraspaso(idContratoSAAPVBorrar)) {
                            idTraspasoBorrar = idTraspasoLinea;
                        }
                        repositorio.eliminarDetalleOrigenTraspaso(idContratoSAAPVBorrar);
                        repositorio.eliminarTraspaso(idContratoSAAPVBorrar);
                        /* INICIO - CREACION DE TRASPASO */
                        if (fila.b.codigoTipoSolicitud().equals("TRA")) {
                            Long idTraspaso = secuencias.siguiente(SEC_ID_TRASPASO);
                            TraspasoNuevo traspaso = new TraspasoNuevo(idTraspaso, fila.b.idContratoSAAPV(), fila.b.idEmpresa(), fila.b.tipoMovimiento(),
                                    fila.b.razonRetiro(), fila.b.fechaCreacion(),
                                    fila.b.tipoMovimiento().equals("INGRESO") ? "0001-01-01" : fila.b.fechaCreacion(), "0001-01-01", fila.b.tipoTraspaso(),
                                    fila.b.descripcion(), fila.idCliente, fila.b.idPersonaOrigen(), fila.b.tipoDeAhorro(), fila.b.nombreEntidadOrigen(),
                                    fila.b.rutEntidadOrigen(), "PDI", fila.b.canal(), null, fila.b.fechaSuscripcion());
                            if (repositorio.crearTraspaso(traspaso)) {
                                salida.add(Mensajes.de(idTraspaso, "OK", "CREACION_TRASPASO_OK", "Creacion del traspaso exitosa"));

                                List<DetalleOrigenNuevo> lstOrigenTraspaso = new ArrayList<>();
                                for (DetalleOrigenTraspaso row : detalleOrigen) {
                                    if (idContratoSAAPVBorrar.longValue() != row.idContratoSAAPVOrigen().longValue()) {
                                        continue;
                                    }
                                    Long idDetalle = secuencias.siguiente(SEC_ID_DETALLE_ORIGEN);
                                    lstOrigenTraspaso.add(detalleNuevo(idDetalle, idTraspaso, row));
                                }
                                if (lstOrigenTraspaso.size() > 0) {
                                    if (repositorio.crearDetallesOrigenTraspaso(lstOrigenTraspaso)) {
                                        salida.add(Mensajes.de(idTraspaso, "OK", "CREACION_ORIGEN_TRASPASO_OK", "Creacion del Origen traspaso exitosa"));
                                    }
                                }

                                /* Modifico el IdTraspaso Nuevo del DetalleTraspaso, ya que el contrato viejo se borro */
                                if (repositorio.modificarIdTraspasoDetalle(idTraspasoBorrar, idTraspaso)) {
                                    salida.add(Mensajes.de(idTraspaso, "OK", "MODIFICACION_DETALLETRASPASO_OK",
                                            "Se modifica DetalleTraspaso por el Contrato Traspaso Nuevo."));
                                }
                                /* Modifico IdContratoSAAPV de PortafolioContratoSAAPV */
                                if (repositorio.modificarContratoDePortafolios(idContratoSAAPVBorrar, fila.b.idContratoSAAPV())) {
                                    salida.add(Mensajes.de(idTraspaso, "OK", "MODIFICACION_PORTAFOLIOCONTRATO_OK",
                                            "Se modifica PortafolioContrato por el Contrato Traspaso Nuevo."));

                                    if (apruebaTraspasoAutomatico.equals("N")) {
                                        // Modifico estado de traspaso a Pendiente de Autorizacion
                                        agregarResultadoEstado(salida, idTraspaso, repositorio.modificarEstadoTraspaso(idTraspaso, "PDA"));
                                        crearEventoIngresoTraspaso(in, fila, idTraspaso);
                                    } else {
                                        // Modifico estado de traspaso
                                        agregarResultadoEstado(salida, idTraspaso, repositorio.modificarEstadoTraspaso(idTraspaso, "ING"));
                                    }
                                }
                            }
                        }
                        /* FIN - CREACION DE TRASPASO */
                    }
                    /* FIN TRASPASO */
                    // ===== ELIMINA DISTRIBUCION =====  (the legacy built its "ELIMINAR_DISTRIBUCION_OK" message but never added it)
                    repositorio.eliminarDistribucion(idContratoSAAPVBorrar);
                } else {
                    repositorio.eliminarDistribucion(idContratoSAAPVBorrar);
                }
            }
            if (!fila.b.codigoTipoSolicitud().equals("TRA")) {
                /* Valido datos minimos del contrato para ingresarlo vigente o pendiente de ingreso */
                List<String> resultadoValidacionPersona = new ArrayList<>();
                if (portafolios.size() > 0) {
                    for (Port portafolio : portafolios) {
                        if (idContratoSAAPVBorrar.longValue() != portafolio.b.idContratoSAAPVOrigen().longValue()) {
                            continue;
                        }
                        resultadoValidacionPersona = validaCamposPersona(fila, portafolio);
                    }
                } else {
                    resultadoValidacionPersona = validaCamposPersona(fila, null);
                }

                if (resultadoValidacionPersona.size() == 0) {
                    // BDH Se cambia estado a VIG
                    CambiarEstadoContratosEntrada d = CambiarEstadoContratosEntrada.porDefecto();
                    cl.bcs.apv.api.dto.cambiarestadocontratos.ContratosSAAPV dc = cl.bcs.apv.api.dto.cambiarestadocontratos.ContratosSAAPV.porDefecto();
                    cambiarEstadoContratos.ejecutar(new CambiarEstadoContratosEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), "VIG",
                            List.of(new cl.bcs.apv.api.dto.cambiarestadocontratos.ContratosSAAPV(fila.idPersona, fila.idCliente, fila.idEmpleador,
                                    fila.folioContratoSAAPV, "1900-01-01", fila.b.idEstadoEnvioNotificacion(), "PDA", "VIG", dc.idGlosa(),
                                    dc.descripcionGlosa())),
                            d.desdePac()));
                } else {
                    salida.add(Mensajes.de(4L, "ADVERTENCIA", "VALIDA_DATOS_MINIMOS_CONTRATO",
                            "CONTRATO " + fila.folioContratoSAAPV + ": Contrato queda Pendiente de Ingreso por que faltan datos Minimos."));
                    for (int i = 0; i < resultadoValidacionPersona.size(); i++) {
                        salida.add(Mensajes.de(4L, "ADVERTENCIA", "VALIDA_DATOS_MINIMOS_CONTRATO", " - " + resultadoValidacionPersona.get(i)));
                    }
                }
            }

            /* Se crean distribuciones asociadas a la modificacion */
            if (distribuciones.size() > 0) {
                List<DistribucionNueva> distribucionInversionList = new ArrayList<>();
                for (Dist row : distribuciones) {
                    if (row.idContratoSAAPV == null) {
                        row.idContratoSAAPV = fila.b.idContratoSAAPV();
                    }
                    if (idContratoSAAPVBorrar.longValue() != row.idContratoSAAPV.longValue()) {
                        continue;
                    }
                    distribucionInversionList.add(new DistribucionNueva(fila.b.idContratoSAAPV(), row.b.folioContratoSAAPV(), row.b.idInstrumento(),
                            row.b.nemotecnico(), row.b.montoPesos(), row.b.porcentaje(), row.b.idEstado()));
                }
                List<DistribucionNueva> aInsertar = distribucionInversionList;
                if (distribucionInversionList.size() == 0) {
                    // LEGACY DEFECT kept (see README APV-A-513): when every distribution belongs to another contract the whole original list is inserted
                    aInsertar = new ArrayList<>();
                    for (Dist d : distribuciones) {
                        aInsertar.add(new DistribucionNueva(d.idContratoSAAPV, d.b.folioContratoSAAPV(), d.b.idInstrumento(), d.b.nemotecnico(),
                                d.b.montoPesos(), d.b.porcentaje(), d.b.idEstado()));
                    }
                }
                if (repositorio.crearDistribuciones(aInsertar)) {
                    salida.add(Mensajes.de(1L, "OK", "CREAR_DISTRIBUCION_OK", "Creacion Distribucion Inversion exitosa"));
                } else {
                    salida.add(Mensajes.de(2L, "ERROR", "ERROR_CREAR_DISTRIBUCION", "Problema al crear Distribucion"));
                }
            }
            // Evalua los Portafolios que vienen ingresando para determinar acciones
            if (portafolios.size() > 0 && !fila.b.codigoTipoSolicitud().equals("TRA")) {
                repositorio.eliminarPortafoliosDelContrato(fila.b.idContratoSAAPV());
                for (Port row : portafolios) {
                    if (row.idPortafolio != null && row.idPortafolio > 0) {
                        // Portafolio existe, solo vincular a nuevo SAAPV
                        if (row.b.idContratoSAAPVOrigen().equals(fila.b.idContratoSAAPV())) {
                            agregarResultadoVinculo(salida, fila, repositorio.crearPortafolioContrato(portafolioContrato(fila, row)));
                        }
                    } else {
                        // Nuevo Portafolio, Crear y vincular.
                        JsonObject entradaProcedimiento = Json.objeto()
                                .poner("CodigoAgrupador", row.b.codigoAgrupador())
                                .poner("CodigoPortafolio", row.b.regimen())
                                .poner("CodigoRegimen", row.b.regimen())
                                .poner("IdCliente", fila.idCliente)
                                .poner("IdEmpresa", in.idEmpresa())
                                .poner("IdModulo", in.idModulo())
                                .poner("IdPerfilComercial", fila.b.idPerfilComercial())
                                .poner("IdUsuario", in.idUsuario())
                                .poner("NombrePlan", fila.b.nombrePlan())
                                .poner("PropositoPortafolio", row.b.idPropositoPortafolio())
                                .construir();
                        JsonObject resultSP = colaboradores.invocar("StoredProcedure",
                                Json.objeto().poner("procedimiento", PROCEDIMIENTO_CREAR_PORTAFOLIO).poner("entrada", entradaProcedimiento).construir());

                        if (Json.largo(resultSP, "p_out_ErroresContador").equals(0L)) {
                            refIdPortafolio = Json.largo(resultSP, "p_out_RefId");
                        } else {
                            // the legacy copied the errors and warnings of the procedure into a SalidaServicioCrearCliente that is never read: only the
                            // iteration (and its NullPointerException on a missing list) is observable
                            for (@SuppressWarnings("unused") JsonObject ignorado : listaONull(resultSP, "p_out_ErroresDetalle")) {
                                // intentionally empty
                            }
                            for (@SuppressWarnings("unused") JsonObject ignorado : listaONull(resultSP, "p_out_AdvertenciasDetalle")) {
                                // intentionally empty
                            }
                        }
                        if (refIdPortafolio != 0) {
                            row.idPortafolio = refIdPortafolio;
                            salida.add(Mensajes.de(refIdPortafolio, "OK", "CREACION_PORTAFOLIO_OK",
                                    "CONTRATO " + fila.folioContratoSAAPV + ": Creacion de portafolio exitosa"));
                            // Se crea relacion entre el Contrato SAAPV y el Portafolio
                            agregarResultadoVinculo(salida, fila, repositorio.crearPortafolioContrato(portafolioContrato(fila, row)));
                        }
                    }
                }
            } else if (fila.b.tipoMovimiento().equals("EGRESO")) {
                Long idTraspasoEgreso = detalleOrigen.get(0).idTraspaso();
                List<Long> idsDetalle = repositorio.consultarDetallesTraspasoEgreso(idTraspasoEgreso);
                agregarLogSAAPV(in, filas, distribuciones, portafolios, detalleOrigen);
                // consulta sobre los traspasos ya existentes y eliminar registro
                repositorio.eliminarDetallesOrigenEgreso(idsDetalle);

                // insertar nuevo detalle de traspasos de egresos
                List<DetalleOrigenNuevo> lstOrigenTraspaso = new ArrayList<>();
                for (DetalleOrigenTraspaso traspsosEgreso : detalleOrigen) {
                    Long idDetalle = secuencias.siguiente(SEC_ID_DETALLE_ORIGEN);
                    lstOrigenTraspaso.add(detalleNuevo(idDetalle, traspsosEgreso.idTraspaso(), traspsosEgreso));
                }
                if (lstOrigenTraspaso.size() > 0) {
                    if (repositorio.crearDetallesOrigenTraspaso(lstOrigenTraspaso)) {
                        salida.add(Mensajes.de(lstOrigenTraspaso.get(0).IdTraspaso(), "OK", "CREACION_ORIGEN_TRASPASO_OK", "Creacion del Origen traspaso exitosa"));
                    }
                }
                String fechaSistema = Parametros.fechaSistema(configuracion, in.idEmpresa());
                ParametrosContrato primero = filas.get(0).b;
                TraspasoEgresoModificado tpTraspaso = new TraspasoEgresoModificado(detalleOrigen.get(0).idTraspaso(), fechaSistema,
                        primero.nombreEntidadOrigen(), primero.rutEntidadOrigen(), primero.tipoTraspaso());
                if (repositorio.modificarTraspasoEgreso(tpTraspaso)) {
                    salida.add(Mensajes.de(lstOrigenTraspaso.get(0).IdTraspaso(), "OK", "MODIFICACION_TRASPASO_OK", "Creacion del traspaso exitosa"));
                }
            }
        }
        return salida;
    }

    private static List<JsonObject> listaONull(JsonObject o, String clave) {
        return o.has(clave) ? Json.lista(o, clave) : null;
    }

    private static void agregarResultadoEstado(List<Mensaje> salida, Long idTraspaso, boolean modificado) {
        if (modificado) {
            salida.add(Mensajes.de(idTraspaso, "OK", "MODIFICA_ESTADO_OK", "Modifica estado del Traspaso."));
        } else {
            salida.add(Mensajes.de(idTraspaso, "ERROR", "ERROR_MODIFICA_ESTADO", "Problema al modificar estado del Traspaso."));
        }
    }

    private static void agregarResultadoVinculo(List<Mensaje> salida, Fila fila, boolean creado) {
        if (creado) {
            salida.add(Mensajes.de(5L, "OK", "CREAR_PORTAFOLIO_CONTRATO_OK",
                    "CONTRATO " + fila.folioContratoSAAPV + ": Se crea relacion entre portafolio con contrato SAAPV."));
        } else {
            // LEGACY DEFECT kept (see README APV-A-515): the failure message is typed "OK"
            salida.add(Mensajes.de(6L, "OK", "ERROR_CREAR_PORTAFOLIO_CONTRATO",
                    "CONTRATO " + fila.folioContratoSAAPV + ": Problema al crear relacion entre portafolio con contrato SAAPV."));
        }
    }

    private static PortafolioContratoNuevo portafolioContrato(Fila fila, Port row) {
        return new PortafolioContratoNuevo(row.b.origenAhorro(), row.idPortafolio, fila.b.idContratoSAAPV(), row.b.montoPesos(), row.b.montoUF(),
                row.b.porcentaje(), row.b.pago(), row.b.mesInicio(), row.b.mesTermino(), "VIG");
    }

    private static DetalleOrigenNuevo detalleNuevo(Long idDetalle, Long idTraspaso, DetalleOrigenTraspaso row) {
        return new DetalleOrigenNuevo(idDetalle, idTraspaso, row.codigoTipoAhorro(), row.idEntidadRecaudadora(), row.nombreEntidad(), row.documentoEntidad(),
                row.fondo(), row.montoTraspaso(), row.ufTraspaso(), row.porcentajeTraspaso(), "VIG", row.idInstrumento(), row.idPortafolio(),
                row.codigoRegimenTributario());
    }

    private void crearEventoIngresoTraspaso(ModificarContratosEntrada in, Fila fila, Long idTraspaso) {
        JsonObject evento = Json.objeto()
                .poner("IdUnion", null)
                .poner("IdTipoEvento", "INGTRA")
                .poner("IdModuloSolicita", "APV")
                .poner("IdUsuarioSolicita", in.idUsuario())
                .poner("Fecha", fila.b.fechaCreacion())
                .poner("IdTipoOperacion", null)
                .poner("IdConcepto", "INGTRA")
                .poner("NumeroOperacionConcepto", idTraspaso.toString())
                .poner("FechaOperacionConcepto", fila.b.fechaCreacion())
                .poner("IdCliente", fila.idCliente)
                .poner("IdFuncionario", 0L)
                .poner("IdProveedor", 0L)
                .poner("IdTipoProducto", null)
                .poner("IdClasificacionTipoInstrumento", null)
                .poner("IdTipoInstrumento", null)
                .poner("IdInstrumento", null)
                .poner("Nemo", null)
                .poner("Cantidad", null)
                .poner("Monto", null)
                .poner("Precio", null)
                .poner("Tasa", null)
                .poner("IdMoneda", "CLP")
                .poner("Glosa", "INGRESO DE TRASPASO APV Folio : " + idTraspaso)
                .construir();
        colaboradores.invocar("CrearEventoAutorizacionMasivo", Json.objeto()
                .poner("P_IDEMPRESA", in.idEmpresa())
                .poner("P_IDUSUARIO", in.idUsuario())
                .poner("P_IDMODULO", in.idModulo())
                .poner("P_SIMULAROPERACION", "N")
                .poner("P_USAREGLASNEGOCIO", "N")
                .poner("P_TABLAEVENTOS", List.of(evento))
                .poner("P_DETALLE", new ArrayList<JsonObject>())
                .construir());
    }

    private static String fechaOUno900(String fecha) {
        return fecha.isEmpty() ? "1900-01-01" : fecha;
    }

    private static ContratoModificado contratoModificado(Fila f) {
        ParametrosContrato p = f.b;
        return new ContratoModificado(
                p.idContratoSAAPV(),
                p.idEmpresa(),
                f.folioContratoSAAPV,
                p.idPlan(),
                p.nombrePlan(),
                fechaOUno900(p.fechaSuscripcion()),
                fechaOUno900(p.fechaCreacion()),
                fechaOUno900(p.fechaAprobacion()),
                fechaOUno900(p.fechaNotificacion()),
                fechaOUno900(p.fechaRecepcion()),
                f.folio,
                f.idPersona,
                f.idEmpleador,
                f.idCliente,
                p.nombreCliente(),
                p.apellidoPaternoCliente(),
                p.apellidoMaternoCliente(),
                p.documentoIdentidadCliente(),
                p.fechaNacimientoCliente(),
                p.estadoCivil(),
                p.idTipoGenero(),
                p.idPaisResidenciaCliente(),
                p.idCiudadCliente(),
                p.idComunaCliente(),
                p.direccionCliente(),
                p.idTipoCargoPersona(),
                p.telefonoFijo(),
                p.telefonoMovil(),
                p.telefonoLaboral(),
                p.emailCliente(),
                p.razonSocialEmpleador(),
                p.idMedioRecaudacionTrabajador(),
                p.idEntidadRecaudadora(),
                p.documentoIdentidadEmpleador(),
                p.direccionComercialEmpleador(),
                p.idComunaEmpleador(),
                p.telefonoEmpleador(),
                p.nombreEncargado(),
                p.emailEncargado(),
                p.telefonoEncargado(),
                p.codigoPrevision(),
                p.pensionado(),
                p.trabajador(),
                p.formaDePago(),
                p.idMedioRecaudacionFormaPago(),
                p.diaDescuento(),
                p.institucion(),
                p.comisionTransferencia(),
                p.codigoTipoSolicitud(),
                p.nombreEntidadOrigen(),
                p.rutEntidadOrigen(),
                p.tipoDeAhorro(),
                p.fondoDeOrigen(),
                p.tipoTraspaso(),
                p.montoTraspaso(),
                p.ufTraspaso(),
                p.porcentajeTraspaso(),
                p.idEstadoEnvioNotificacion(),
                p.idEstadoSolicitud(),
                p.idEstado(),
                p.idCategoriaInversionista(),
                p.idPerfilComercial(),
                p.idCargaMasiva(),
                null /* PC.Canal is never assigned in the legacy */);
    }

    /** Legacy {@code ValidaCamposPersona("DATOS_MINIMOS", ...)}: the list of the minimum data the contract lacks. */
    private static List<String> validaCamposPersona(Fila contrato, Port portafolio) {
        ParametrosContrato c = contrato.b;
        List<String> lstDatosFaltantes = new ArrayList<>();
        if (contrato.folioContratoSAAPV == null || contrato.folioContratoSAAPV.equals("")) {
            lstDatosFaltantes.add("Folio Contrato SAAPV.");
        }
        if (c.nombrePlan() == null || c.nombrePlan().equals("")) {
            lstDatosFaltantes.add("Nombre de Plan SAAPV.");
        }
        if (c.fechaSuscripcion() == null || c.fechaSuscripcion().equals("")) {
            lstDatosFaltantes.add("Fecha Suscripcion.");
        }
        if (c.nombreCliente() == null || c.nombreCliente().equals("")) {
            lstDatosFaltantes.add("Nombre Cliente.");
        }
        if (c.apellidoPaternoCliente() == null || c.apellidoPaternoCliente().equals("")) {
            lstDatosFaltantes.add("Apellido Paterno Cliente.");
        }
        if (c.documentoIdentidadCliente() == null || c.documentoIdentidadCliente().equals("")) {
            lstDatosFaltantes.add("Documento Identidad Cliente.");
        }
        if (c.direccionCliente() == null || c.direccionCliente().equals("")) {
            lstDatosFaltantes.add("Direccion Cliente.");
        }
        if (c.numeroDireccionCliente() == null || c.numeroDireccionCliente().equals("")) {
            lstDatosFaltantes.add("Numero Direccion Cliente.");
        }
        if (c.idPaisResidenciaCliente() == null || c.idPaisResidenciaCliente() == 0) {
            lstDatosFaltantes.add("Id Pais.");
        }
        if (c.idComunaCliente() == null || c.idComunaCliente() == 0) {
            lstDatosFaltantes.add("Id Comuna.");
        }
        if (c.idCiudadCliente() == null || c.idCiudadCliente() == 0) {
            lstDatosFaltantes.add("Id Ciudad.");
        }
        if (c.trabajador() == null || c.trabajador().equals("")) {
            lstDatosFaltantes.add("Tipo Trabajador.");
        } else if (c.trabajador().equals("DEPENDIENTE")) {
            if (c.razonSocialEmpleador() == null || c.razonSocialEmpleador().equals("")) {
                lstDatosFaltantes.add("Razon Social.");
            }
            if (c.documentoIdentidadEmpleador() == null || c.documentoIdentidadEmpleador().equals("")) {
                lstDatosFaltantes.add("Documento Identidad Empleador.");
            }
            if (c.direccionComercialEmpleador() == null || c.direccionComercialEmpleador().equals("")) {
                lstDatosFaltantes.add("Direccion Comercial Empleador.");
            }
            if (c.numeroDireccionEmpleador() == null || c.numeroDireccionEmpleador().equals("")) {
                lstDatosFaltantes.add("Numero Direccion Empleador.");
            }
        }
        if (c.pensionado() == null || c.pensionado().equals("")) {
            lstDatosFaltantes.add("Pensionado.");
        }

        if (portafolio != null) {
            PortafoliosModificados p = portafolio.b;
            if (p.origenAhorro() == null || p.origenAhorro().equals("")) {
                lstDatosFaltantes.add("Origen Ahorro.");
            }
            if (p.pagoNombre() == null || p.pagoNombre().equals("")) {
                lstDatosFaltantes.add("Tipo de Pago.");
            }
            if (p.producto() == null || p.producto().equals("")) {
                lstDatosFaltantes.add("Producto.");
            }
            if (p.regimen() == null || p.regimen().equals("")) {
                lstDatosFaltantes.add("Regimen.");
            }
            if (p.montoPesos() == 0 && p.montoUF() == 0) {
                lstDatosFaltantes.add("Monto Portafolio.");
            }
            if (p.mesInicio() == null || p.mesInicio().equals("")) {
                lstDatosFaltantes.add("Mes Inicio.");
            }
        } else {
            lstDatosFaltantes.add("Portafolio.");
        }
        return lstDatosFaltantes;
    }

    /**
     * Legacy {@code AgregarLogSAAPV}: builds the log of the modification and sends it to AgregarLog. Everything, including the failures of the
     * collaborator, is swallowed ({@code catch (Exception e) { e.printStackTrace(); }}): without detail rows of the traspaso the builder throws
     * IndexOutOfBoundsException before the call (LEGACY DEFECT kept, see README APV-A-510).
     */
    private void agregarLogSAAPV(ModificarContratosEntrada in, List<Fila> filas, List<Dist> distribuciones, List<Port> portafolios,
            List<DetalleOrigenTraspaso> detalleOrigen) {
        try {
            String fechaSistema = Parametros.fechaSistema(configuracion, in.idEmpresa());
            ParametrosContrato primero = filas.get(0).b;
            Long idCliente = filas.get(0).idCliente;
            Long idPersona = filas.get(0).idPersona;
            Long idCabeceraLog = (idCliente == 0) ? idPersona : idCliente;
            String folioContratoSAAPV = filas.get(0).folioContratoSAAPV;
            String fechaSuscripcion = primero.fechaSuscripcion();
            String nombreEntidad = primero.nombreEntidadOrigen();
            String documentoEntidad = primero.rutEntidadOrigen();
            Long idTraspaso = detalleOrigen.get(0).idTraspaso() != null ? detalleOrigen.get(0).idTraspaso() : 0;

            JsonObject log = Json.objeto()
                    .poner("IdTrabajo", 0L)
                    .poner("IdCabeceraLog", idCabeceraLog)
                    .poner("IdTipoLog", "MODIFICACION")
                    .poner("IdModulo", in.idModulo())
                    .poner("IdUsuario", in.idUsuario())
                    .poner("IdEmpresa", in.idEmpresa())
                    .poner("FechaSistemaLog", fechaSistema)
                    .poner("IdUsuarioAtendido", 0L)
                    .poner("IdCliente", idCliente)
                    .poner("EstadoCliente", "")
                    .poner("NombreCliente", "")
                    .poner("IdTipoDocumentoIdentidad", "")
                    .poner("IdDocumentoIdentidad", "")
                    .poner("IdPortafolio", 0L)
                    .poner("EstadoPortafolio", "")
                    .poner("IdTipoRegistro", "CONTRATO-SAAPV")
                    .poner("IdRegistro", "")
                    .poner("EstadoRegistro", "")
                    .poner("IdTipoOperacion", "")
                    .poner("Folio", 0L)
                    .poner("Cantidad", BigDecimal.ZERO)
                    .poner("PrecioTasa", BigDecimal.ZERO)
                    .poner("Monto", BigDecimal.ZERO)
                    .poner("IdMonedaOrigen", "")
                    .poner("IdMonedaLiquidacion", "")
                    .poner("TipoCambio", "")
                    .poner("Nemo", "")
                    .poner("IdProceso", 0L)
                    .poner("EstadoProceso", "ING")
                    .poner("NombreProceso", "Modificación Contrato SAAPV")
                    .poner("IdPersona", idCliente == 0 ? idPersona : 0L)
                    .construir();

            List<JsonObject> detalle = new ArrayList<>();
            // DETALLE DEL LOG
            if (primero.tipoMovimiento().equals("EGRESO")) {
                detalle.add(detalleLog(idCabeceraLog, "Contrato-SAAPV", "FolioTraspaso", idTraspaso.toString()));
                detalle.add(detalleLog(idCabeceraLog, "Contrato-SAAPV", "DocumentoEntidad", documentoEntidad));
                detalle.add(detalleLog(idCabeceraLog, "Contrato-SAAPV", "NombreEntidad", nombreEntidad));
                detalle.add(detalleLog(idCabeceraLog, "Contrato-SAAPV", "FechaSuscripcion", fechaSuscripcion));
            } else {
                detalle.add(detalleLog(idCabeceraLog, "Contrato-SAAPV", "FolioContratoSAAPV", folioContratoSAAPV));
                detalle.add(detalleLog(idCabeceraLog, "Contrato-SAAPV", "FechaSuscripcion", fechaSuscripcion));
            }

            for (int i = 0; i < distribuciones.size(); i++) {
                DistribucionInversion d = distribuciones.get(i).b;
                String idInstrumento = Long.toString(d.idInstrumento());
                String idEstado = d.idEstado();
                String montoPesos = Double.toString(d.montoPesos());
                // LEGACY DEFECT kept (see README APV-A-511): "Porcentaje" is logged with the IdInstrumento, as a double
                String porcentaje = Double.toString(d.idInstrumento());

                detalle.add(detalleLog(idCabeceraLog, "Distribucion-Inversion", "IdInstrumento", idInstrumento));
                detalle.add(detalleLog(idCabeceraLog, "Distribucion-Inversion", "IdEstado", idEstado));
                detalle.add(detalleLog(idCabeceraLog, "Distribucion-Inversion", "MontoPesos", montoPesos));
                detalle.add(detalleLog(idCabeceraLog, "Distribucion-Inversion", "Porcentaje", porcentaje));
            }

            for (int i = 0; i < portafolios.size(); i++) {
                Port p = portafolios.get(i);
                String idPortafolio = Long.toString(p.idPortafolio);
                String producto = p.b.producto();
                String mesInicio = p.b.mesInicio();
                String mesTermino = p.b.mesTermino();
                String montoPesos = Double.toString(p.b.montoPesos());
                String montoUF = Double.toString(p.b.montoUF());

                detalle.add(detalleLog(idCabeceraLog, "Portafolio-SAAPV", "IdPortafolio", idPortafolio));
                detalle.add(detalleLog(idCabeceraLog, "Portafolio-SAAPV", "Producto", producto));
                detalle.add(detalleLog(idCabeceraLog, "Portafolio-SAAPV", "MesInicio", mesInicio));
                detalle.add(detalleLog(idCabeceraLog, "Portafolio-SAAPV", "MesTermino", mesTermino));
                detalle.add(detalleLog(idCabeceraLog, "Portafolio-SAAPV", "MontoPesos", montoPesos));
                detalle.add(detalleLog(idCabeceraLog, "Portafolio-SAAPV", "MontoUF", montoUF));
            }

            colaboradores.invocar("AgregarLog", Json.objeto()
                    .poner("p_IdEmpresa", null)
                    .poner("p_IdUsuario", null)
                    .poner("p_IdModulo", null)
                    .poner("p_UsaReglasNegocio", null)
                    .poner("p_IdEstadoAutorizacion", null)
                    .poner("p_IdTrabajo", 0L)
                    .poner("p_Log", List.of(log))
                    .poner("p_LogDetalle", detalle)
                    .construir());
        } catch (Exception e) {
            // legacy: e.printStackTrace(); the failure is swallowed
        }
    }

    private static JsonObject detalleLog(Long idCabeceraLog, String subRegistro, String campoRegistro, String valorNuevo) {
        return Json.objeto()
                .poner("IdTrabajo", 0L)
                .poner("IdCabeceraLog", idCabeceraLog)
                .poner("SubRegistro", subRegistro)
                .poner("CampoRegistro", campoRegistro)
                .poner("ValorAntiguo", "")
                .poner("ValorNuevo", valorNuevo)
                .construir();
    }
}
