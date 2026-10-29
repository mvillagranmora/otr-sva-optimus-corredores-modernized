package cl.bcs.apv.application.contratos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.apv.api.casouso.CrearContrato;
import cl.bcs.apv.api.casouso.GrabarContratos;
import cl.bcs.apv.api.casouso.ObtenerFolioSuscripcion;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.crearcontrato.Contrato;
import cl.bcs.apv.api.dto.crearcontrato.CrearContratoEntrada;
import cl.bcs.apv.api.dto.crearcontrato.CrearContratoResultado;
import cl.bcs.apv.api.dto.crearcontrato.DetalleOrigenTraspaso;
import cl.bcs.apv.api.dto.crearcontrato.Distribucion;
import cl.bcs.apv.api.dto.crearcontrato.EntidadContrato;
import cl.bcs.apv.api.dto.crearcontrato.MensajesErrores;
import cl.bcs.apv.api.dto.crearcontrato.Regimen;
import cl.bcs.apv.api.dto.grabarcontratos.DistribucionInversion;
import cl.bcs.apv.api.dto.grabarcontratos.GrabarContratosEntrada;
import cl.bcs.apv.api.dto.grabarcontratos.GrabarContratosResultado;
import cl.bcs.apv.api.dto.grabarcontratos.ParametrosContrato;
import cl.bcs.apv.api.dto.grabarcontratos.PortafoliosModificados;
import cl.bcs.apv.api.dto.obtenerfoliosuscripcion.ObtenerFolioSuscripcionEntrada;
import cl.bcs.apv.api.dto.obtenerfoliosuscripcion.ObtenerFolioSuscripcionResultado;
import cl.bcs.apv.application.comun.Json;
import cl.bcs.apv.application.comun.Parametros;
import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.contratos.CrearContratoRepositorio;
import cl.bcs.apv.domain.port.out.contratos.CrearContratoRepositorio.FilaContratoVigente;
import cl.bcs.apv.domain.port.out.contratos.CrearContratoRepositorio.FilaDatosApv;
import cl.bcs.apv.domain.port.out.contratos.CrearContratoRepositorio.FilaEntidadRecaudadora;
import cl.bcs.apv.domain.port.out.contratos.CrearContratoRepositorio.FilaEstadoContrato;
import cl.bcs.apv.domain.port.out.contratos.CrearContratoRepositorio.FilaFichaCliente;
import cl.bcs.apv.domain.port.out.contratos.CrearContratoRepositorio.FilaFolioContrato;
import cl.bcs.apv.domain.port.out.contratos.CrearContratoRepositorio.FilaIdPortafolio;
import cl.bcs.apv.domain.port.out.contratos.CrearContratoRepositorio.FilaInstrumento;
import cl.bcs.apv.domain.port.out.contratos.CrearContratoRepositorio.FilaPerfilComercial;

/**
 * Use case {@code CrearContrato} (family A): legacy {@code cl.bcs.APV.CrearContratoSAAPV}. Validates the contract request, derives the portfolio /
 * client / distribution data, and delegates the write to {@link GrabarContratos}; a MODIFICACION also closes the previous contract.
 *
 * <p>The legacy {@code call()} does NOT recount errors (the counter is the one {@code logica} accumulates), so this {@code ejecutar} returns what the
 * translated {@code logica} builds. The legacy mutates its input objects (plan name, grouper, regimen code, distribution amounts...); the records of the
 * typed contract are immutable, so the mutated values live in the working copies {@code RegimenT}, {@code DistT}, {@code DetT} and in locals.
 * Legacy defects are kept bug for bug.
 */
@Transactional(rollbackFor = Exception.class)
public class CrearContratoServicio implements CrearContrato {

    private static final List<String> FONDOS = Arrays.asList("A", "B", "C", "D", "E");
    private static final String PESOS = "CLP";

    private final CrearContratoRepositorio repositorio;
    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;
    private final GrabarContratos grabarContratos;
    private final ObtenerFolioSuscripcion obtenerFolioSuscripcion;

    public CrearContratoServicio(CrearContratoRepositorio repositorio, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Colaboradores colaboradores, GrabarContratos grabarContratos, ObtenerFolioSuscripcion obtenerFolioSuscripcion) {
        this.repositorio = repositorio;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
        this.grabarContratos = grabarContratos;
        this.obtenerFolioSuscripcion = obtenerFolioSuscripcion;
    }

    // ------------------------------------------------------------------------------------------------ working state

    /** The {@code EntidadContrato} and counters the legacy accumulates ({@code entidadContrato}, {@code listMensajesErrores}, {@code ContadorErrores}). */
    private static final class Estado {
        Long refIdPortafolio;
        Long contador = 0L;
        String folio;
        Long id;
        /** {@code entidadContrato.Mensajes} (a reference: it may point to {@link #lista} or to a replacement). */
        List<MensajesErrores> mensajes = new ArrayList<>();
        /** {@code listMensajesErrores}: shared by every contract of the request. */
        final List<MensajesErrores> lista = new ArrayList<>();

        CrearContratoResultado resultado() {
            return new CrearContratoResultado(refIdPortafolio, contador, new EntidadContrato(folio, id, mensajes));
        }

        /** The common error exit: message appended to the shared list, {@code Mensajes} pointing to it, one more error. */
        CrearContratoResultado error(String codigo, String texto, boolean conFolioEId) {
            MensajesErrores m = new MensajesErrores(0L, "ERROR", codigo, texto);
            if (conFolioEId) {
                folio = "";
                id = 0L;
            }
            lista.add(m);
            mensajes = lista;
            contador++;
            return resultado();
        }
    }

    /** Working copy of a {@code Regimen} (the legacy overwrites its {@code Regimen} code and {@code IdPortafolio}). */
    private static final class RegimenT {
        final Regimen origen;
        String regimen;
        Long idPortafolio;

        RegimenT(Regimen r) {
            this.origen = r;
            this.regimen = r.regimen();
            this.idPortafolio = r.idPortafolio();
        }
    }

    /** Working copy of a {@code Distribucion} (the legacy fills the missing amount or percentage). */
    private static final class DistT {
        final Distribucion origen;
        BigDecimal montoParcial;
        BigDecimal porcentajeParcial;

        DistT(Distribucion d) {
            this.origen = d;
            this.montoParcial = d.montoParcial();
            this.porcentajeParcial = d.porcentajeParcial();
        }
    }

    /** Working copy of a {@code DetalleOrigenTraspaso} (the legacy fills amount and percentage of a TOT / PAR transfer). */
    private static final class DetT {
        final DetalleOrigenTraspaso origen;
        BigDecimal montoTraspaso;
        BigDecimal porcentajeTraspaso;

        DetT(DetalleOrigenTraspaso d) {
            this.origen = d;
            this.montoTraspaso = d.montoTraspaso();
            this.porcentajeTraspaso = d.porcentajeTraspaso();
        }
    }

    private static List<RegimenT> trabajoRegimenes(Contrato c) {
        List<RegimenT> l = new ArrayList<>();
        for (Regimen r : c.regimen()) {
            l.add(new RegimenT(r));
        }
        return l;
    }

    private static List<DistT> trabajoDistribuciones(Contrato c) {
        List<DistT> l = new ArrayList<>();
        for (Distribucion d : c.distribucion()) {
            l.add(new DistT(d));
        }
        return l;
    }

    private static List<DetT> trabajoDetalles(Contrato c) {
        List<DetT> l = new ArrayList<>();
        for (DetalleOrigenTraspaso d : c.detalleOrigenTraspaso()) {
            l.add(new DetT(d));
        }
        return l;
    }

    // ------------------------------------------------------------------------------------------------ logica

    @Override
    public CrearContratoResultado ejecutar(CrearContratoEntrada in) {
        Estado e = new Estado();
        String tipoOperacion = "I";
        List<FilaEstadoContrato> validaContrato = new ArrayList<>();

        if (in.contrato().size() > 0) {
            for (Contrato contrato : in.contrato()) {
                if (!contrato.folioContratoSAAPV().isEmpty()) {
                    String nuevoFolioContratoSAAPV = "";
                    String folioPrevio = "";
                    // values the legacy writes into the input contract
                    String nombrePlan = contrato.nombrePlan();
                    String codigoAgrupador = contrato.codigoAgrupador();
                    Long idMatrizAgrupador = contrato.idMatrizAgrupador();
                    Long diaDescuento = contrato.diaDescuento();
                    Long idMedioRecaudacionFormaPago = contrato.idMedioRecaudacionFormaPago();
                    // working copies of the lists, built the first time the legacy touches them (a null list is a NullPointerException there too)
                    List<RegimenT> regimenes = null;
                    List<DistT> distribuciones = null;
                    List<DetT> detalles = null;

                    try {
                        Long.parseLong(contrato.folioContratoSAAPV().trim());
                        nuevoFolioContratoSAAPV = contrato.folioContratoSAAPV().trim();
                    } catch (NumberFormatException excepcion1) {
                        if (esGeneracionFolioAutomatico(contrato.folioContratoSAAPV())) {
                            ObtenerFolioSuscripcionResultado salidaFolio = obtenerFolioSuscripcion
                                    .ejecutar(new ObtenerFolioSuscripcionEntrada(in.idEmpresa(), in.idUsuario(), "APV", contrato.folioContratoSAAPV()));
                            if (salidaFolio.outContadorError() > 0) {
                                cl.bcs.apv.api.dto.obtenerfoliosuscripcion.MensajesErrores origen = salidaFolio.outMensajes();
                                MensajesErrores mensajesErrores = new MensajesErrores(origen.idInterno(), origen.tipoMensaje(), origen.codigo(), origen.mensaje());
                                e.folio = "";
                                e.id = 0L;
                                e.lista.add(mensajesErrores);
                                e.mensajes = e.lista;
                                e.contador++;
                                break;
                            } else {
                                nuevoFolioContratoSAAPV = salidaFolio.outFolioSuscripcion();
                            }
                        } else {
                            nuevoFolioContratoSAAPV = in.contrato().get(0).folioContratoSAAPV();
                        }
                    }

                    if (contrato.codigoTipoSolicitud().equals("MODIFICACION") && contrato.idContratoSAAPV() > 0) {
                        if (!contrato.folio().isEmpty()) {
                            folioPrevio = contrato.folio().trim();

                            List<FilaFolioContrato> selectFolio = repositorio.consultarFolioContrato(contrato.idCliente(), contrato.idContratoSAAPV());

                            if (selectFolio.isEmpty()) {
                                return e.error("ERROR_CONTRATO_NO_EXISTE", "El valor de IdContratoSAAPV proporcionado no es valido", true);
                            }

                            if (!selectFolio.get(0).FolioContratoSAAPV.equalsIgnoreCase(contrato.folio())) {
                                return e.error("ERROR_FOLIO_NO_CORRESPONDE", "El valor de IdContratoSAAPV proporcionado no corresponde con el Folio", true);
                            }

                            tipoOperacion = "M";
                        } else {
                            regimenes = trabajoRegimenes(contrato);
                            List<FilaDatosApv> datos = repositorio.consultarDatosApv(regimenes.get(0).origen.regimen(), regimenes.get(0).origen.producto(),
                                    contrato.idPlan());
                            nombrePlan = datos.get(0).NombrePlanTipoInversion;
                            codigoAgrupador = datos.get(0).CodigoAgrupador;
                            idMatrizAgrupador = datos.get(0).IdMatrizAgrupadorAPV;

                            List<FilaFolioContrato> selectFolio = repositorio.consultarFolioContrato(contrato.idCliente(), contrato.idContratoSAAPV());

                            // si el Idportafolio es nulo usar IdPortafolioContrato como IdPortafolio
                            if (selectFolio.get(0).IdPortafolio == null) {
                                regimenes.get(0).idPortafolio = selectFolio.get(0).IdPortafolioContrato;
                            } else {
                                regimenes.get(0).idPortafolio = selectFolio.get(0).IdPortafolio;
                            }

                            if (!selectFolio.isEmpty()) {
                                folioPrevio = selectFolio.get(0).FolioContratoSAAPV;
                                tipoOperacion = "M";
                            } else {
                                tipoOperacion = "I";
                            }

                            for (FilaFolioContrato folioContratoSAAPV : selectFolio) {
                                if (folioContratoSAAPV.FolioContratoSAAPV.equalsIgnoreCase(folioPrevio) && !"".equalsIgnoreCase(folioPrevio)) {
                                    diaDescuento = diaDescuento.compareTo(0L) == 0 ? folioContratoSAAPV.DiaDescuento : diaDescuento;
                                    idMedioRecaudacionFormaPago = idMedioRecaudacionFormaPago.compareTo(0L) == 0
                                            ? folioContratoSAAPV.IdMedioRecaudacionFormaPago : idMedioRecaudacionFormaPago;
                                }
                            }
                        }
                    } else {
                        List<FilaContratoVigente> selectContrato = repositorio.consultarContratosVigentesAprobados(contrato.idCliente());

                        if (!selectContrato.isEmpty()) {
                            for (FilaContratoVigente contratoSAAPV : selectContrato) {
                                if (contratoSAAPV.IdEstado.equals("VIG") && contratoSAAPV.IdEstadoSolicitud.equals("APR")) {
                                    MensajesErrores m = new MensajesErrores(0L, "ERROR", "CONTRATO_VIGENTE_APROBADO",
                                            "Contrato Folio " + contratoSAAPV.FolioContratoSAAPV
                                                    + " se encuentra con estado vigente y con estado solicitud aprobado, intente cerrar el contrato para continuar");
                                    e.folio = contratoSAAPV.FolioContratoSAAPV;
                                    e.id = 0L;
                                    e.lista.add(m);
                                    e.mensajes = e.lista;
                                    e.contador++;
                                    return e.resultado();
                                }
                            }

                            Long idProductoAgrupador = Long.parseLong(codigoAgrupador.replace("GRUPO ", ""), 10);

                            for (FilaContratoVigente selectContratoSAAPV : selectContrato) {
                                if (selectContratoSAAPV.IdPlan.equals(contrato.idPlan())
                                        && selectContratoSAAPV.IdMedioRecaudacionFormaPago.equals(idMedioRecaudacionFormaPago)
                                        && selectContratoSAAPV.IdMedioRecaudacionTrabajador.equals(contrato.idMedioRecaudacionTrabajador())
                                        && selectContratoSAAPV.IdMatrizAgrupadorAPV.equals(idMatrizAgrupador)
                                        && selectContratoSAAPV.IdProductoAgrupador.equals(idProductoAgrupador)) {
                                    if (regimenes == null) {
                                        regimenes = trabajoRegimenes(contrato);
                                    }
                                    if (!regimenes.isEmpty()) {
                                        for (RegimenT regimen : regimenes) {
                                            if (selectContratoSAAPV.Pago.equals(regimen.origen.pago())
                                                    && selectContratoSAAPV.CodigoPortafolio.equals(regimen.regimen)
                                                    && selectContratoSAAPV.IdRegimenTributario.equals(regimen.origen.idRegimen())) {
                                                folioPrevio = selectContratoSAAPV.FolioContratoSAAPV;
                                                tipoOperacion = "M";
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (tipoOperacion.equals("M")) {
                        validaContrato = repositorio.consultarEstadoContrato(in.contrato().get(0).idContratoSAAPV());

                        if (validaContrato.isEmpty()) {
                            return e.error("ERROR_CONTRATO_NO_EXISTE", "El valor de IdContratoSAAPV proporcionado no es valido", true);
                        }
                    }

                    // DATOS RESIDENCIA (the answer is not used: the query itself is part of the behaviour)
                    repositorio.consultarResidencia(in.contrato().get(0).rut());

                    // *************************************************
                    // PORTAFOLIO MODIFICADOS
                    // *************************************************
                    List<PortafoliosModificados> listaPortafoliosModificados = new ArrayList<>();
                    PortafoliosModificados porDefectoPortafolio = PortafoliosModificados.porDefecto();

                    if (regimenes == null) {
                        regimenes = trabajoRegimenes(contrato);
                    }
                    if (!regimenes.isEmpty()) {
                        for (RegimenT regimen : regimenes) {
                            String origenAhorro = regimen.origen.origenAhorro();

                            // SI NO SE ENVIA REGIMEN buscamos codigo
                            if (regimenes.get(0).regimen.isEmpty()) {
                                regimen.regimen = repositorio.consultarCodigoRegimen(regimen.origen.idRegimen()).get(0).Codigo;
                            }

                            // en caso que sea creacion Contrato
                            if (contrato.codigoTipoSolicitud().equalsIgnoreCase("AUTORIZACION") || contrato.codigoTipoSolicitud().equalsIgnoreCase("INGRESO")) {
                                List<FilaDatosApv> datos = repositorio.consultarDatosApv(regimen.regimen, regimen.origen.producto(), contrato.idPlan());

                                if (datos.isEmpty()) {
                                    return e.error("DATOS_APV_INCORRECTOS", "Los Datos de APV ingresados, no son correctos(Matriz)", true);
                                }

                                nombrePlan = datos.get(0).NombrePlanTipoInversion;

                                if (regimen.origen.codigoAgrupador().isEmpty() || regimen.origen.idMatrizAgrupador().equals(0L)) {
                                    codigoAgrupador = datos.get(0).CodigoAgrupador;
                                    idMatrizAgrupador = datos.get(0).IdMatrizAgrupadorAPV;
                                } else {
                                    codigoAgrupador = datos.get(0).CodigoAgrupador;
                                    idMatrizAgrupador = regimen.origen.idMatrizAgrupador();
                                }
                                // buscamos IdPortafolio
                                List<FilaIdPortafolio> selectIdPortafolio = repositorio.consultarIdPortafolio(contrato.idCliente(),
                                        datos.get(0).IdMatrizAgrupadorAPV, datos.get(0).IdRegimenTributario);

                                // en caso que no encuentre Portafolio
                                if (selectIdPortafolio.isEmpty()) {
                                    // portafolio para crear
                                    regimen.idPortafolio = repositorio.consultarPrimerIdPortafolio(contrato.idCliente()).get(0).IdPortafolio;
                                } else {
                                    regimen.idPortafolio = selectIdPortafolio.get(0).IdPortafolio;
                                }
                            }
                            double montoPesos = porDefectoPortafolio.montoPesos();
                            double montoUF = porDefectoPortafolio.montoUF();
                            if (regimen.origen.moneda().equals(PESOS)) {
                                montoPesos = regimen.origen.montoTotal().doubleValue();
                            } else {
                                montoUF = regimen.origen.montoTotal().doubleValue();
                            }
                            listaPortafoliosModificados.add(new PortafoliosModificados(codigoAgrupador, idMatrizAgrupador, origenAhorro, regimen.idPortafolio,
                                    regimen.origen.idPropositoPortafolio().equalsIgnoreCase("") ? "PROPOSITOAPV" : regimen.origen.idPropositoPortafolio(),
                                    regimen.origen.mesInicio(), !"".equalsIgnoreCase(regimen.origen.mesTermino()) ? regimen.origen.mesTermino() : "9999-01",
                                    montoPesos, montoUF, regimen.origen.pago(), regimen.origen.tipoRecaudacion(),
                                    regimen.origen.porcentajeRegimenImponible().doubleValue(), regimen.origen.producto(), regimen.regimen,
                                    porDefectoPortafolio.idInterno()));
                        }
                        if (("TOT").equals(contrato.tipoTraspaso())) {
                            detalles = trabajoDetalles(contrato);
                            detalles.get(0).montoTraspaso = regimenes.get(0).origen.montoTotal().longValue() > 0L ? regimenes.get(0).origen.montoTotal()
                                    : BigDecimal.ZERO;
                            detalles.get(0).porcentajeTraspaso = new BigDecimal(100);
                        } else if (("PAR").equals(contrato.tipoTraspaso())) {
                            detalles = trabajoDetalles(contrato);
                            for (DetT detalle : detalles) {
                                if (detalle.porcentajeTraspaso != null && detalle.porcentajeTraspaso.compareTo(BigDecimal.ZERO) > 0) {
                                    detalle.montoTraspaso = regimenes.get(0).origen.montoTotal().longValue() > 0L ? regimenes.get(0).origen.montoTotal()
                                            : BigDecimal.ZERO;
                                }
                            }
                        }
                    }

                    // RESCATE DATOS FICHA CLIENTE
                    List<FilaFichaCliente> salidaFicha = repositorio.consultarFichaCliente(contrato.idCliente(),
                            listaPortafoliosModificados.get(0).idPortafolio());

                    // rellenar o no con los datos de la ficha de cliente - CASO BIZNET Y WEB
                    if (contrato.usarDatosFichaCliente().equalsIgnoreCase("N")) {
                        FilaFichaCliente f = salidaFicha.get(0);
                        f.IdPersona = contrato.idPersona();
                        f.DocumentoIdentidad = contrato.rut();
                        f.Nombres = contrato.nombres();
                        f.ApellidoPaterno = contrato.apellidoPaterno();
                        f.ApellidoMaterno = contrato.apellidoMaterno();
                        f.FechaNacimiento = contrato.fechaNacimiento();
                        f.Direccion = contrato.direccion();
                        f.NumeroDireccion = contrato.numeroDireccion();
                        f.TelefonoFijo = contrato.telefonoFijo();
                        f.TelefonoMovil = contrato.telefonoMovil();
                        f.TelefonoLaboral = contrato.telefonoLaboral();
                        f.Pais = contrato.pais();
                        f.Region = contrato.region();
                        f.Comuna = contrato.comuna();
                        f.Ciudad = contrato.ciudad();
                        f.EstadoCivil = contrato.idEstadoCivil();
                        f.Genero = contrato.idGenero();
                        f.DireccionElectronica = contrato.email();
                        f.IdCategoriaInversionista = contrato.idCategoriaInversionista();
                        f.IdPersonaEmpleador = contrato.idPersonaEmpleador();
                        f.RazonSocialEmpleador = contrato.razonSocialEmpleador();
                        f.RutEmpleador = contrato.rutEmpleador();
                        f.IdPaisEmpleador = contrato.idPaisEmpleador();
                        f.DireccionEmpleador = contrato.direccionEmpleador();
                        f.NumeroDireccionEmpleador = "0";
                        f.IdCiudadEmpleador = contrato.idCiudadEmpleador();
                        f.IdComunaEmpleador = contrato.idComunaEmpleador();
                        f.TelefonoEmpleador = contrato.telefonoEmpleador();
                    }

                    // *************************************************
                    // PARAMETROS CONTRATO
                    // *************************************************
                    String apvHabilitado = Parametros.texto(configuracion, in.idEmpresa(), Parametros.PLATAFORMA, "APV.Habilitado");
                    boolean validacionesAPV = "PROPOSITOAPV".equalsIgnoreCase(contrato.idPropositoPortafolio()) && "S".equalsIgnoreCase(apvHabilitado);
                    String perfilComercialPorDefecto = "";
                    if (validacionesAPV) {
                        try {
                            perfilComercialPorDefecto = obtenerPerfilComercialPorDefecto(in.idEmpresa(), contrato.idCategoriaInversionista());
                        } catch (Exception ex) {
                            String codigo = "PERFIL_COMERCIAL_APV_EXENTO_INVALIDO";
                            MensajesErrores mensajesErrores = new MensajesErrores(0L, "ERROR", codigo,
                                    diccionario.mensaje(in.idEmpresa(), codigo, new HashMap<>()));
                            e.folio = "";
                            e.id = 0L;
                            e.lista.add(mensajesErrores);
                            e.mensajes = e.lista;
                            e.contador++;
                            return e.resultado();
                        }
                    }

                    boolean esDependiente = "DEPENDIENTE".equalsIgnoreCase(contrato.trabajador());

                    // the legacy fills ParametrosContrato field by field; these are the reads that can throw, in the same order
                    FilaFichaCliente ficha = salidaFicha.get(0);
                    String fechaNacimientoCliente = ficha.FechaNacimiento.toString();
                    String institucion = contrato.codigoPrevision().toString();
                    String perfilComercial = validacionesAPV ? perfilComercialPorDefecto : ficha.IdPerfilComercial == null ? "" : ficha.IdPerfilComercial;
                    Long idEmpleado = ficha.IdEjecutivo != null ? ficha.IdEjecutivo : contrato.idEmpleador();

                    Long idEmpleador = contrato.idEmpleador();
                    String razonSocialEmpleador = contrato.razonSocialEmpleador();
                    String documentoIdentidadEmpleador = contrato.rutEmpleador();
                    Long idPaisEmpleador = contrato.idPaisEmpleador();
                    String direccionComercialEmpleador = contrato.direccionEmpleador();
                    String numeroDireccionEmpleador = contrato.numeroDireccionEmpleador();
                    Long idCiudadEmpleador = contrato.idCiudadEmpleador();
                    Long idComunaEmpleador = contrato.idComunaEmpleador();
                    String telefonoEmpleador = contrato.telefonoEmpleador();

                    if (contrato.rutEmpleador().equals("") && esDependiente) {
                        idEmpleador = ficha.IdPersonaEmpleador;
                        razonSocialEmpleador = ficha.RazonSocialEmpleador;
                        documentoIdentidadEmpleador = ficha.RutEmpleador;
                        idPaisEmpleador = ficha.IdPaisEmpleador;
                        direccionComercialEmpleador = ficha.DireccionEmpleador;
                        numeroDireccionEmpleador = ficha.NumeroDireccionEmpleador;
                        idCiudadEmpleador = ficha.IdCiudadEmpleador;
                        idComunaEmpleador = ficha.IdComunaEmpleador;
                        telefonoEmpleador = ficha.TelefonoEmpleador;
                    }

                    // *************************************************
                    // DISTRIBUCION INVERSION
                    // *************************************************
                    List<DistribucionInversion> listaDistribucionInversion = new ArrayList<>();

                    // LEGACY DEFECT kept (see README APV-A-454): the empty regimen list already failed above (IndexOutOfBounds on get(0)), this error is unreachable
                    if (contrato.regimen().isEmpty()) {
                        return e.error("ERROR_REGIMEN_VACIO", "Debe ingresar un regimen", true);
                    }

                    if (!contrato.distribucion().isEmpty()) {
                        distribuciones = trabajoDistribuciones(contrato);
                        int i = 0;
                        BigDecimal montoDistribucionTotal = BigDecimal.ZERO;

                        for (DistT distribucion : distribuciones) {
                            // validacion monto porcentaje
                            if (distribuciones.get(i).montoParcial == null && distribuciones.get(i).porcentajeParcial == null) {
                                return e.error("ERROR_MONTO_O_PORCENTAJE_VACIO", "Debe ingresar monto o porcentaje a la distribucion " + String.valueOf(i + 1),
                                        true);
                            }
                            // si solo viene porcentaje en la distribucion
                            if (distribuciones.get(i).porcentajeParcial != null && distribuciones.get(i).montoParcial == null) {
                                // si porcentaje es 0
                                // LEGACY DEFECT kept (see README APV-A-467): BigDecimal.equals is scale sensitive, 0.0 is not "zero"
                                if (distribuciones.get(i).porcentajeParcial.equals(BigDecimal.ZERO)) {
                                    return e.error("ERROR_PORCENTAJE_CERO", "Porcentaje en cero, Debe ingresar un porcentaje en la distribucion", true);
                                }
                                // Se calcula el monto equivalente al porcentaje de la distribucion
                                if (!regimenes.get(0).origen.montoTotal().equals(BigDecimal.ZERO)) {
                                    distribuciones.get(i).montoParcial = regimenes.get(0).origen.montoTotal()
                                            .multiply(distribuciones.get(i).porcentajeParcial).divide(new BigDecimal(100));
                                } else {
                                    // this branch does not set FolioContrato / IdContratoSAAPV (legacy)
                                    return e.error("ERROR_MONTO_DEPOSITO_CERO",
                                            "monto en cero, Debe ingresar monto de deposito para la creacion de la distribucion", false);
                                }
                            }

                            // si solo viene monto en la distribucion
                            if (distribuciones.get(i).porcentajeParcial == null && distribuciones.get(i).montoParcial != null) {
                                // si monto es 0
                                if (distribuciones.get(i).montoParcial.equals(BigDecimal.ZERO)) {
                                    return e.error("ERROR_MONTO_CERO", "monto en cero, Debe ingresar un monto en la distribucion", true);
                                }
                                // si monto total es 0
                                if (!regimenes.get(0).origen.montoTotal().equals(BigDecimal.ZERO)) {
                                    distribuciones.get(i).porcentajeParcial = new BigDecimal(100).multiply(distribuciones.get(i).montoParcial)
                                            .divide(regimenes.get(0).origen.montoTotal(), 8, RoundingMode.HALF_UP);
                                } else {
                                    return e.error("ERROR_MONTO_DEPOSITO_CERO",
                                            "monto en cero, Debe ingresar monto  deposito para la creacion de la distribucion", true);
                                }
                            }

                            Map<String, FilaInstrumento> hashInstrumentos = obtenerInstrumentos(contrato.distribucion());
                            FilaInstrumento instrumento = hashInstrumentos.get(distribucion.origen.idSerieFondo());
                            listaDistribucionInversion.add(new DistribucionInversion(contrato.idContratoSAAPV(), nuevoFolioContratoSAAPV,
                                    instrumento.IdInstrumento, instrumento.Nemotecnico, distribucion.montoParcial.doubleValue(),
                                    distribucion.porcentajeParcial.doubleValue(), distribucion.origen.idEstado(), DistribucionInversion.porDefecto().idInterno()));
                            i++;

                            montoDistribucionTotal = montoDistribucionTotal.add(distribucion.montoParcial);
                        }

                        // se valida monto total distribucion con el monto deposito
                        if (montoDistribucionTotal.compareTo(regimenes.get(0).origen.montoTotal()) != 0) {
                            MensajesErrores m = new MensajesErrores(0L, "ERROR", "UI_TOTAL_DISTRIBUCION_DIFERENTE_A_MONTO_DEPOSITO",
                                    "UI_TOTAL_DISTRIBUCION_DIFERENTE_A_MONTO_DEPOSITO");
                            e.folio = "";
                            e.id = 0L;
                            e.lista.add(m);
                            e.mensajes = e.lista;
                            e.contador++;
                            break;
                        }
                    }

                    // *************************************************
                    // DETALLE ORIGEN TRASPASO
                    // *************************************************
                    List<cl.bcs.apv.api.dto.grabarcontratos.DetalleOrigenTraspaso> listaDetalleOrigenTraspaso = new ArrayList<>();
                    ParametrosContrato defecto = ParametrosContrato.porDefecto();
                    String rutEntidadOrigen = "";
                    String nombreEntidadOrigen = "";
                    String tipoDeAhorro = "";
                    Long idPersonaOrigen = defecto.idPersonaOrigen();

                    if (!contrato.detalleOrigenTraspaso().isEmpty()) {
                        if (detalles == null) {
                            detalles = trabajoDetalles(contrato);
                        }
                        String documentoEntidadConsulta = null;
                        for (DetT detalleOrigenTraspasoContrato : detalles) {

                            if (detalleOrigenTraspasoContrato.origen.documentoEntidad() != null
                                    && !detalleOrigenTraspasoContrato.origen.documentoEntidad().equalsIgnoreCase("")) {
                                documentoEntidadConsulta = detalleOrigenTraspasoContrato.origen.documentoEntidad();
                            } else {
                                return e.error("ERROR_ENTIDAD_RECAUDADORA_TRASPASO", "Debe ingresar una entidad recaudadora valida", true);
                            }

                            if (contrato.tipoTraspaso() == null || contrato.tipoTraspaso().equalsIgnoreCase("")) {
                                return e.error("ERROR_TIPO_TRASPASO", "Debe ingresar un tipo de traspaso valido", true);
                            }

                            if (detalleOrigenTraspasoContrato.origen.codigoTipoAhorro() != null
                                    && detalleOrigenTraspasoContrato.origen.codigoTipoAhorro().equalsIgnoreCase("APV")) {
                                if (detalleOrigenTraspasoContrato.origen.fondo() == null || !FONDOS.contains(detalleOrigenTraspasoContrato.origen.fondo())) {
                                    return e.error("ERROR_TIPO_AHORRO_FONDO", "Fondo no valido para Tipo Ahorro APV", true);
                                }
                            }

                            if (contrato.tipoTraspaso().equalsIgnoreCase("PAR")
                                    && !(detalleOrigenTraspasoContrato.montoTraspaso.compareTo(BigDecimal.ZERO) > 0
                                            || detalleOrigenTraspasoContrato.origen.ufTraspaso().compareTo(BigDecimal.ZERO) > 0
                                            || detalleOrigenTraspasoContrato.porcentajeTraspaso.compareTo(BigDecimal.ZERO) > 0)) {
                                return e.error("ERROR_MONTO_UF_PORCENTAJE_TRASPASO", "Si el traspaso es parcial, debe ingresar monto, uf o porcentaje", true);
                            }

                        }
                        List<FilaEntidadRecaudadora> entidadRecaudadora = repositorio.consultarEntidadRecaudadora(documentoEntidadConsulta);
                        Map<String, FilaEntidadRecaudadora> entidadHash = list2Hash(entidadRecaudadora);
                        for (DetT detalleOrigenTraspasoContrato : detalles) {
                            FilaEntidadRecaudadora entidad = entidadHash.get(detalleOrigenTraspasoContrato.origen.documentoEntidad());
                            if (entidad == null) {
                                return e.error("ERROR_ENTIDAD_RECAUDADORA_TRASPASO", "Debe ingresar una entidad recaudadora valida", true);
                            }
                            cl.bcs.apv.api.dto.grabarcontratos.DetalleOrigenTraspaso porDefecto = cl.bcs.apv.api.dto.grabarcontratos.DetalleOrigenTraspaso
                                    .porDefecto();
                            listaDetalleOrigenTraspaso.add(new cl.bcs.apv.api.dto.grabarcontratos.DetalleOrigenTraspaso(0L, 0L,
                                    detalleOrigenTraspasoContrato.origen.codigoTipoAhorro(), entidad.IdEntidadRecaudadora, entidad.Nombre,
                                    entidad.DocumentoIdentidad,
                                    detalleOrigenTraspasoContrato.origen.codigoTipoAhorro().equalsIgnoreCase("APV")
                                            ? detalleOrigenTraspasoContrato.origen.fondo() : "",
                                    detalleOrigenTraspasoContrato.montoTraspaso.doubleValue(),
                                    contrato.tipoTraspaso().equalsIgnoreCase("TOT") ? 0 : detalleOrigenTraspasoContrato.origen.ufTraspaso().doubleValue(),
                                    detalleOrigenTraspasoContrato.porcentajeTraspaso.doubleValue(), "VIG", 0L, "0", 0L, porDefecto.idInterno()));
                            idPersonaOrigen = entidad.IdPersona;
                            rutEntidadOrigen = entidad.DocumentoIdentidad;
                            nombreEntidadOrigen = entidad.Nombre;
                            tipoDeAhorro = detalleOrigenTraspasoContrato.origen.codigoTipoAhorro();
                        }
                    }
                    String codigoTipoSolicitud = listaDetalleOrigenTraspaso.size() > 0 ? "TRA" : "";
                    String tipoMovimiento = listaDetalleOrigenTraspaso.size() > 0 ? "INGRESO" : contrato.tipoMovimiento();

                    ParametrosContrato parametrosContrato = new ParametrosContrato(contrato.idContratoSAAPV(), in.idEmpresa(), nuevoFolioContratoSAAPV,
                            contrato.idPlan(), nombrePlan, contrato.fechaSuscripcion(), contrato.fechaCreacion(), contrato.fechaAprobacion(),
                            contrato.fechaNotificacion(), contrato.fechaRecepcion(), folioPrevio, ficha.IdPersona, idEmpleador, contrato.idCliente(),
                            ficha.IdCategoriaInversionista, perfilComercial, ficha.Nombres, ficha.ApellidoPaterno, ficha.ApellidoMaterno, contrato.rut(),
                            fechaNacimientoCliente, ficha.EstadoCivil, ficha.Genero, ficha.IdPais, ficha.IdCiudad, ficha.IdComuna, ficha.Direccion,
                            ficha.NumeroDireccion, contrato.idCargoPersona(), ficha.TelefonoFijo != null ? ficha.TelefonoFijo : "",
                            ficha.TelefonoMovil != null ? ficha.TelefonoMovil : "", ficha.TelefonoLaboral != null ? ficha.TelefonoLaboral : "",
                            ficha.DireccionElectronica, razonSocialEmpleador, contrato.idMedioRecaudacionTrabajador(), contrato.idEntidadRecaudadora(),
                            documentoIdentidadEmpleador, idPaisEmpleador, direccionComercialEmpleador, numeroDireccionEmpleador, idCiudadEmpleador,
                            idComunaEmpleador, telefonoEmpleador, contrato.nombreEncargado(), contrato.emailEncargado(), contrato.telefonoEncargado(),
                            contrato.codigoPrevision(), "SI".equalsIgnoreCase(contrato.pensionado()) ? "SI" : "NO",
                            esDependiente ? "DEPENDIENTE" : "INDEPENDIENTE", contrato.formaDePago(), idMedioRecaudacionFormaPago, diaDescuento, institucion,
                            contrato.comisionTransferencia(), codigoTipoSolicitud, tipoMovimiento, contrato.razonRetiro(), nombreEntidadOrigen,
                            rutEntidadOrigen, idPersonaOrigen, contrato.descripcion(), tipoDeAhorro, "", contrato.tipoTraspaso(), 0L, 0L, 0L,
                            contrato.idEstadoEnvioNotificacion(), contrato.idEstadoSolicitud(), contrato.estado(), contrato.idCargaMasiva(), idEmpleado,
                            contrato.idPropositoPortafolio(), idMatrizAgrupador, codigoAgrupador, contrato.canal(), contrato.esAporteCuentaInversion(),
                            defecto.idInterno());

                    // *************************************************
                    // GRABAR CONTRATO SAAPV
                    // *************************************************
                    GrabarContratosResultado salidaGrabar = grabarContratos.ejecutar(new GrabarContratosEntrada(in.idEmpresa(), in.idUsuario(), "APV",
                            tipoOperacion, List.of(parametrosContrato), listaDistribucionInversion, listaPortafoliosModificados, listaDetalleOrigenTraspaso));

                    // Cerramos el contrato Antiguo en caso que el tipo de solicitud sea una modificacion
                    if (tipoOperacion.equals("M")) {
                        // validamos si el contrato anterior es con estado vigente
                        if (validaContrato.get(0).IdEstado.equals("VIG")) {
                            if (in.contrato().get(0).idContratoSAAPV() != null) {
                                CrearContratoRepositorio.ContratoACerrar param = new CrearContratoRepositorio.ContratoACerrar();
                                param.IdContratoSAAPV = in.contrato().get(0).idContratoSAAPV();
                                repositorio.cerrarContratos(List.of(param));
                                agregarLog(in);
                            }
                        } else {
                            // LEGACY DEFECT kept (see README APV-A-449): the error comes AFTER GrabarContratoSAAPV already wrote the new contract
                            return e.error("ERROR_CONTRATO_NO_VIGENTE", "El monto a modificar no es vigente", true);
                        }
                    }

                    e.refIdPortafolio = salidaGrabar.refIdPortafolio();

                    for (Mensaje mensajeSalida : salidaGrabar.mensajes()) {
                        MensajesErrores mensajesErrores = new MensajesErrores(0L, mensajeSalida.tipo(), mensajeSalida.codigo(), mensajeSalida.descripcion());

                        if (mensajeSalida.tipo().equals("ERROR")) {
                            List<MensajesErrores> listErrores = new ArrayList<>();

                            listErrores.add(mensajesErrores);
                            e.mensajes = listErrores;
                            e.id = mensajeSalida.id();

                            e.contador++;
                            break;
                        } else {
                            e.lista.add(mensajesErrores);
                            e.mensajes = e.lista;
                            e.id = mensajeSalida.id();
                        }
                    }

                    e.folio = nuevoFolioContratoSAAPV;
                    // LEGACY DEFECT kept (see README APV-A-483): only the first contract with a folio is processed
                    break;
                } else {
                    MensajesErrores mensajesErrores = new MensajesErrores(0L, "ERROR", "DEBE_INGRESAR_UN_FOLIO_CONTRATO", "DEBE_INGRESAR_UN_FOLIO_CONTRATO");

                    e.folio = "";
                    e.id = 0L;

                    e.lista.add(mensajesErrores);
                    e.mensajes = e.lista;
                    e.contador++;
                }
            }
            return e.resultado();
        } else {
            MensajesErrores mensajesErrores = new MensajesErrores(0L, "ERROR", "NO_HAY_CONTRATOS_PARA_INGRESAR", "NO_HAY_CONTRATOS_PARA_INGRESAR");
            List<MensajesErrores> lista = new ArrayList<>();
            lista.add(mensajesErrores);
            return new CrearContratoResultado(null, 1L, new EntidadContrato("", 0L, lista));
        }
    }

    // ------------------------------------------------------------------------------------------------ helpers

    private static Boolean esGeneracionFolioAutomatico(String folioEntrada) {
        return folioEntrada != null && folioEntrada.length() == 1 && Character.isLetter(folioEntrada.charAt(0));
    }

    /** Legacy {@code obtenerInstrumentos}: instruments by the distinct series, keyed by nemotecnico (legacy {@code Utils.List2Hash}). */
    private Map<String, FilaInstrumento> obtenerInstrumentos(List<Distribucion> distribuciones) {
        List<String> nemotecnicos = new ArrayList<>();
        for (Distribucion distribucion : distribuciones) {
            if (!nemotecnicos.contains(distribucion.idSerieFondo())) {
                nemotecnicos.add(distribucion.idSerieFondo());
            }
        }
        List<FilaInstrumento> instrumentos = repositorio.consultarInstrumentos(nemotecnicos);
        Map<String, FilaInstrumento> hash = new HashMap<>();
        if (instrumentos.size() == 0) {
            return hash;
        }
        for (FilaInstrumento instrumento : instrumentos) {
            if (hash.containsKey(instrumento.Nemotecnico)) {
                throw Sneaky.lanzar(new Exception("key field is not unique"));
            }
            hash.put(instrumento.Nemotecnico, instrumento);
        }
        return hash;
    }

    /** Legacy {@code Utils.List2Hash(entidades, "DocumentoIdentidad")}. */
    private static Map<String, FilaEntidadRecaudadora> list2Hash(List<FilaEntidadRecaudadora> entidades) {
        Map<String, FilaEntidadRecaudadora> hash = new HashMap<>();
        if (entidades.size() == 0) {
            return hash;
        }
        for (FilaEntidadRecaudadora entidad : entidades) {
            if (hash.containsKey(entidad.DocumentoIdentidad)) {
                throw Sneaky.lanzar(new Exception("key field is not unique"));
            }
            hash.put(entidad.DocumentoIdentidad, entidad);
        }
        return hash;
    }

    private String obtenerPerfilComercialPorDefecto(Long idEmpresa, String categoriaInversionista) {
        String parametroPerfilComercial = "APV.PerfilComercial." + categoriaInversionista;
        String perfilComercial = Parametros.texto(configuracion, idEmpresa, Parametros.PLATAFORMA, parametroPerfilComercial);

        if (perfilComercial == null || "".equalsIgnoreCase(perfilComercial)) {
            throw Sneaky.lanzar(new Exception("No existe configuración APV para el parámetro " + parametroPerfilComercial));
        }
        List<FilaPerfilComercial> perfiles = repositorio.consultarPerfilComercial(perfilComercial);
        FilaPerfilComercial perfil = perfiles.stream().findFirst().orElse(null);
        if (perfil == null) {
            throw Sneaky.lanzar(new Exception("El perfil comercial configurado [" + perfilComercial + "] no existe, está inactivo o no tiene asociado APV_EXENTO"));
        }
        return perfil.IdPerfilComercialBase;
    }

    /** Legacy {@code AgregarLog(con, in)}: log of the closing of the old contract; every failure is swallowed (as the legacy did). */
    private void agregarLog(CrearContratoEntrada in) {
        try {
            String fechaSistema = Parametros.texto(configuracion, in.idEmpresa(), Parametros.PLATAFORMA, "PLA.FechaSistema");
            Long idCliente = in.contrato().get(0).idCliente();
            String folioContratoSAAPV = in.contrato().get(0).folioContratoSAAPV();
            Long idPersona = in.contrato().get(0).idPersona();
            Long idCabeceraLog = (idCliente == 0) ? idPersona : idCliente;

            // CABECERA: legacy AgregarLog.InAgregarLog.Log with its constructor defaults
            Json.Objeto log = Json.objeto().poner("IdTrabajo", 0L).poner("IdCabeceraLog", idCabeceraLog).poner("IdTipoLog", "MODIFICACION")
                    .poner("IdModulo", "APV").poner("IdUsuario", in.idUsuario()).poner("IdEmpresa", in.idEmpresa()).poner("FechaSistemaLog", fechaSistema)
                    .poner("IdUsuarioAtendido", 0L).poner("IdCliente", idCliente).poner("EstadoCliente", "").poner("NombreCliente", "")
                    .poner("IdTipoDocumentoIdentidad", "").poner("IdDocumentoIdentidad", "").poner("IdPortafolio", 0L).poner("EstadoPortafolio", "")
                    .poner("IdTipoRegistro", "CONTRATO-SAAPV").poner("IdRegistro", "").poner("EstadoRegistro", "").poner("IdTipoOperacion", "")
                    .poner("Folio", 0L).poner("Cantidad", BigDecimal.ZERO).poner("PrecioTasa", BigDecimal.ZERO).poner("Monto", BigDecimal.ZERO)
                    .poner("IdMonedaOrigen", "").poner("IdMonedaLiquidacion", "").poner("TipoCambio", "").poner("Nemo", "").poner("IdProceso", 0L)
                    .poner("EstadoProceso", "ING").poner("NombreProceso", "Cerrar Contrato SAAPV");
            log.poner("IdPersona", idCliente == 0 ? idPersona : (Long) 0L);

            JsonObject entrada = Json.objeto().poner("p_IdEmpresa", null).poner("p_IdUsuario", null).poner("p_IdModulo", null)
                    .poner("p_UsaReglasNegocio", null).poner("p_IdEstadoAutorizacion", null).poner("p_IdTrabajo", 0L)
                    .poner("p_Log", List.of(log.construir()))
                    .poner("p_LogDetalle", List.of(detalleLog(idCabeceraLog, "FolioContratoSAAPV", folioContratoSAAPV),
                            detalleLog(idCabeceraLog, "Descripción", "Cierre de contrato antiguo por tipo de solicitud")))
                    .construir();
            colaboradores.invocar("AgregarLog", entrada);
        } catch (Exception ex) {
            // legacy: e.printStackTrace(), the log is best effort
        }
    }

    private static JsonObject detalleLog(Long idCabeceraLog, String campoRegistro, String valorNuevo) {
        return Json.objeto().poner("IdTrabajo", 0L).poner("IdCabeceraLog", idCabeceraLog).poner("SubRegistro", "CONTRATO-SAAPV")
                .poner("CampoRegistro", campoRegistro).poner("ValorAntiguo", "").poner("ValorNuevo", valorNuevo).construir();
    }
}
