package cl.bcs.apv.application.contratos;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.apv.api.casouso.CambiarEstadoContratos;
import cl.bcs.apv.api.casouso.GrabarContratos;
import cl.bcs.apv.api.casouso.ValidarContrato;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.cambiarestadocontratos.CambiarEstadoContratosEntrada;
import cl.bcs.apv.api.dto.grabarcontratos.DetalleOrigenTraspaso;
import cl.bcs.apv.api.dto.grabarcontratos.DistribucionInversion;
import cl.bcs.apv.api.dto.grabarcontratos.GrabarContratosEntrada;
import cl.bcs.apv.api.dto.grabarcontratos.GrabarContratosResultado;
import cl.bcs.apv.api.dto.grabarcontratos.ParametrosContrato;
import cl.bcs.apv.api.dto.grabarcontratos.PortafoliosModificados;
import cl.bcs.apv.api.dto.validarcontrato.ModificaContratoSAAPV;
import cl.bcs.apv.api.dto.validarcontrato.ValidarContratoEntrada;
import cl.bcs.apv.api.dto.validarcontrato.ValidarContratoResultado;
import cl.bcs.apv.application.comun.Json;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.application.comun.Parametros;
import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.EntradaCrearCliente;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.EntradaCrearDireccionElectronica;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.EntradaCrearEventoAutorizacion;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.EntradaCrearPersona;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.EntradaCrearTelefono;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.Enrolamiento;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.ErrorDetalle;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.EventoAutorizacion;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.InAgregarLog;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.SalidaCrearCliente;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.SalidaCrearDireccionElectronica;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.SalidaCrearPersona;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.SalidaCrearTelefono;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;
import cl.bcs.apv.domain.port.out.contratos.ContratoNuevo;
import cl.bcs.apv.domain.port.out.contratos.GrabarContratosRepositorio;
import cl.bcs.apv.domain.port.out.contratos.GrabarContratosRepositorio.CambiarCargoPersona;
import cl.bcs.apv.domain.port.out.contratos.GrabarContratosRepositorio.ConsultaDireccion;
import cl.bcs.apv.domain.port.out.contratos.GrabarContratosRepositorio.ConsultaRelacionPersona;
import cl.bcs.apv.domain.port.out.contratos.GrabarContratosRepositorio.ContratoConsultado;
import cl.bcs.apv.domain.port.out.contratos.GrabarContratosRepositorio.ContratoSAAPVID;
import cl.bcs.apv.domain.port.out.contratos.GrabarContratosRepositorio.DetalleOrigenNuevo;
import cl.bcs.apv.domain.port.out.contratos.GrabarContratosRepositorio.DistribucionNueva;
import cl.bcs.apv.domain.port.out.contratos.GrabarContratosRepositorio.EstadoCliente;
import cl.bcs.apv.domain.port.out.contratos.GrabarContratosRepositorio.IndicadorEncargadoRRHH;
import cl.bcs.apv.domain.port.out.contratos.GrabarContratosRepositorio.MedioRecaudacion;
import cl.bcs.apv.domain.port.out.contratos.GrabarContratosRepositorio.PortafolioACerrar;
import cl.bcs.apv.domain.port.out.contratos.GrabarContratosRepositorio.PortafolioContratoNuevo;
import cl.bcs.apv.domain.port.out.contratos.GrabarContratosRepositorio.PortafoliosSAAPV;
import cl.bcs.apv.domain.port.out.contratos.GrabarContratosRepositorio.TipoIndicador;
import cl.bcs.apv.domain.port.out.contratos.GrabarContratosRepositorio.TraspasoEstado;
import cl.bcs.apv.domain.port.out.contratos.GrabarContratosRepositorio.TraspasoNuevo;
import cl.bcs.utils.Fecha;

/**
 * Use case {@code GrabarContratos} (family A): legacy {@code cl.bcs.APV.GrabarContratoSAAPV}. Saves one or more SAAPV contracts: validates the
 * person/client/employer/portfolio through {@link ValidarContrato}, creates the missing person, employer, client and relation through the SocioNegocio
 * collaborators and stored procedures, inserts the contract with its distribution and portfolio links (closing the contracts of the same conditions), puts
 * it in force through {@link CambiarEstadoContratos}, and creates the transfer (traspaso) of a TRA request.
 *
 * <p>The legacy mutates its input (ids filled in, portfolios linked) and shares state between the rows of one call (the enrolment object, the client answer,
 * the phone and e-mail requests): the working copies {@link Fila} and {@link Portafolio} and the local variables of {@code logica} reproduce that, bug for bug.
 */
@Transactional(rollbackFor = Exception.class)
public class GrabarContratosServicio implements GrabarContratos {

    private static final String SP_CREAR_PORTAFOLIO = "BSCL.Portafolio.BD.Servicios::CrearPortafolio";
    private static final String SP_CREAR_RELACION = "BSCL.SocioNegocio.BD.Servicios::REL.CrearRelacionPersona";
    private static final String SP_MODIFICAR_INDICADOR = "BSCL.Plataforma.BD.Servicios::IND.ModificarIndicador";
    private static final String SEQ_CONTRATO = "BSCL.APV.BD.Secuencias::SCS.IdContratoSAAPV";
    private static final String SEQ_TRASPASO = "BSCL.APV.BD.Secuencias::TRA.IdTraspaso";
    private static final String SEQ_DETALLE_ORIGEN = "BSCL.APV.BD.Secuencias::TRA.IdDetalleOrigenTraspaso";

    private final GrabarContratosRepositorio repositorio;
    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final Secuencias secuencias;
    private final Reloj reloj;
    private final Colaboradores colaboradores;
    private final ValidarContrato validarContrato;
    private final CambiarEstadoContratos cambiarEstadoContratos;

    public GrabarContratosServicio(GrabarContratosRepositorio repositorio, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, Colaboradores colaboradores, ValidarContrato validarContrato,
            CambiarEstadoContratos cambiarEstadoContratos) {
        this.repositorio = repositorio;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.secuencias = secuencias;
        this.reloj = reloj;
        this.colaboradores = colaboradores;
        this.validarContrato = validarContrato;
        this.cambiarEstadoContratos = cambiarEstadoContratos;
    }

    /** Legacy {@code call()}: the logic, then {@code P_OUT_ERRORESCONTADOR = getNumeroErrores(P_OUT_MENSAJES)}. */
    @Override
    public GrabarContratosResultado ejecutar(GrabarContratosEntrada entrada) {
        return Sneaky.sinChequear(() -> {
            Salida salida = logica(entrada);
            return new GrabarContratosResultado(salida.refIdPortafolio, salida.mensajes, Mensajes.numeroErrores(salida.mensajes));
        });
    }

    // ------------------------------------------------------------------------------------------------ working copies of the legacy input

    /** The input row with the fields the legacy flow overwrites ({@code fila.IdContratoSAAPV = ...}). */
    private static final class Fila {
        final ParametrosContrato p;
        Long idContratoSAAPV;
        Long idPersona;
        Long idCliente;
        Long idEmpleador;
        String documentoIdentidadEmpleador;
        String fechaCreacion;

        Fila(ParametrosContrato p) {
            this.p = p;
            this.idContratoSAAPV = p.idContratoSAAPV();
            this.idPersona = p.idPersona();
            this.idCliente = p.idCliente();
            this.idEmpleador = p.idEmpleador();
            this.documentoIdentidadEmpleador = p.documentoIdentidadEmpleador();
            this.fechaCreacion = p.fechaCreacion();
        }
    }

    /** The portfolio row; {@code IdPortafolio} is filled by the validation / the creation of the portfolio and survives to the next contract. */
    private static final class Portafolio {
        final PortafoliosModificados p;
        Long idPortafolio;

        Portafolio(PortafoliosModificados p) {
            this.p = p;
            this.idPortafolio = p.idPortafolio();
        }
    }

    private static final class Salida {
        Long refIdPortafolio;
        final List<Mensaje> mensajes = new ArrayList<>();
    }

    // ------------------------------------------------------------------------------------------------ logic

    @SuppressWarnings("unused")
    private Salida logica(GrabarContratosEntrada in) throws Exception {
        Enrolamiento enr = new Enrolamiento();
        EntradaCrearPersona entradaPersona = new EntradaCrearPersona();
        SalidaCrearPersona objOut;
        EntradaCrearTelefono entradaTelefonoCom = new EntradaCrearTelefono();
        EntradaCrearTelefono entradaTelefonoPar = new EntradaCrearTelefono();
        SalidaCrearTelefono objOutFonoPar;
        SalidaCrearTelefono objOutFonoCom;
        EntradaCrearDireccionElectronica entradaMail = new EntradaCrearDireccionElectronica();
        SalidaCrearDireccionElectronica objOutMail;
        Salida salida = new Salida();
        EntradaCrearCliente entradaCliente = new EntradaCrearCliente();
        SalidaCrearCliente salidaCliente = new SalidaCrearCliente();

        long idPortafolioAPV = 0;
        long idRelacionPersona = 0;

        List<Long> idsContratoSAAPV = siguientes(SEQ_CONTRATO, (long) in.parametros().size());
        String apruebaTraspasoAutomatico = Parametros.texto(configuracion, in.idEmpresa(), "APV", "APV.ApruebaTraspasoAutomatico");
        int contadorIds = 0;
        boolean existeFolioContrato;

        List<String> listaDocumentoIdentidad = new ArrayList<>();
        List<EstadoCliente> listaEstadoCliente = new ArrayList<>();
        HashMap<String, EstadoCliente> hashCliente = new HashMap<>();
        if (in.parametros().size() > 0) {
            listaDocumentoIdentidad = in.parametros().stream()
                    .map(parametro -> parametro.documentoIdentidadCliente() != null ? parametro.documentoIdentidadCliente() : "")
                    .distinct()
                    .collect(Collectors.toList());
            if (listaDocumentoIdentidad.size() > 0) {
                listaEstadoCliente = repositorio.estadosCliente(listaDocumentoIdentidad);
                if (listaEstadoCliente.size() > 0) {
                    hashCliente = listaAHash(listaEstadoCliente);
                }
            }
        }

        String fechaSistema = Parametros.texto(configuracion, in.idEmpresa(), "PLATAFORMA", "PLA.FechaSistema");

        List<Fila> filas = new ArrayList<>();
        for (ParametrosContrato p : in.parametros()) {
            filas.add(new Fila(p));
        }
        List<Portafolio> portafolios = null;
        if (in.portafoliosModificados() != null) {
            portafolios = new ArrayList<>();
            for (PortafoliosModificados pm : in.portafoliosModificados()) {
                portafolios.add(new Portafolio(pm));
            }
        }
        List<DistribucionInversion> distribucion = in.distribucionInversion();
        List<DetalleOrigenTraspaso> detalleOrigen = in.detalleOrigenTraspaso();

        for (Fila fila : filas) {
            ParametrosContrato p = fila.p;

            if (hashCliente.size() > 0) {
                EstadoCliente estadoCliente = hashCliente.get(p.documentoIdentidadCliente());
                if (estadoCliente != null) {
                    if ("TRA".equalsIgnoreCase(p.codigoTipoSolicitud()) && "INGRESO".equalsIgnoreCase(p.tipoMovimiento())) {
                        if ("BLO".equalsIgnoreCase(estadoCliente.IdEstado())) {
                            String documentoIdentidad = estadoCliente.DocumentoIdentidad();
                            String codMsg = "CLIENTE_BLOQUEADO";
                            Map<String, String> valores = new HashMap<>();
                            if (documentoIdentidad != null) {
                                valores.put("DocumentoIdentidad", documentoIdentidad);
                            }
                            salida.mensajes.add(Mensajes.conDiccionario(diccionario, in.idEmpresa(), 0L, Mensajes.MENSAJE_ERROR, codMsg, valores));
                            // LEGACY DEFECT kept (see README APV-A-380): the row is skipped by 'continue' without consuming its id (contadorIds is not incremented)
                            continue;
                        }
                    }
                }
            }

            Long idContratoSAAPVBorrar = p.idContratoSAAPV();

            fila.idContratoSAAPV = idsContratoSAAPV.get(contadorIds);
            existeFolioContrato = false;

            String cliIdTipoClienteDefecto = Parametros.texto(configuracion, in.idEmpresa(), "PLATAFORMA", "APV.ContratoCLI_IdTipoClienteDefecto");
            String fechaFinRelacionDefecto = Parametros.texto(configuracion, in.idEmpresa(), "PLATAFORMA", "APV.ContratoFechaFinRelacionDefecto");

            // the legacy reads the wall clock here (System.currentTimeMillis); the Reloj port gives it
            long time = reloj.ahora().toEpochMilli();
            java.sql.Date fechaSistemaDate = new java.sql.Date(time);
            Timestamp fechatime = new Timestamp(fechaSistemaDate.getTime());

            String fechaOptimus = Parametros.texto(configuracion, 1L, "PLATAFORMA", "PLA.FechaSistema");

            // *** VALIDACION CONTRATOS SAAPV ***
            List<cl.bcs.apv.api.dto.validarcontrato.ContratosSAAPV> tablaValidaContrato = new ArrayList<>();
            if (portafolios.size() > 0) {
                for (Portafolio filaValidaPortafolio : portafolios) {
                    tablaValidaContrato.add(new cl.bcs.apv.api.dto.validarcontrato.ContratosSAAPV(null, null, null, null, p.documentoIdentidadCliente(),
                            p.documentoIdentidadEmpleador(), p.folioContratoSAAPV(), filaValidaPortafolio.p.regimen(), p.nombrePlan(),
                            filaValidaPortafolio.p.codigoAgrupador()));
                }
            } else {
                tablaValidaContrato.add(new cl.bcs.apv.api.dto.validarcontrato.ContratosSAAPV(null, null, null, null, p.documentoIdentidadCliente(),
                        p.documentoIdentidadEmpleador(), p.folioContratoSAAPV(), "", p.nombrePlan(), ""));
            }
            ValidarContratoResultado outValidaContrato = validarContrato.ejecutar(new ValidarContratoEntrada(in.idEmpresa(), in.idUsuario(),
                    in.idModulo(), "I", tablaValidaContrato, new ArrayList<>(), new ArrayList<>()));

            // LEGACY DEFECT kept (see README APV-A-385): the legacy asked 'P_OUT_ERRORESCONTADOR == null' of ValidaContratoSAAPV.logica, which never sets it
            // (only call() does), so the branch is always taken and the ERROR_VALIDAR_PERSONA branch is unreachable: the counter of the typed result is not read.
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
                    for (Portafolio filaSeteaPortafolio : portafolios) {
                        if (filaValidaContrato.codigo().compareTo(filaSeteaPortafolio.p.regimen()) == 0 && filaValidaContrato.idPortafolio() != null) {
                            filaSeteaPortafolio.idPortafolio = filaValidaContrato.idPortafolio();
                        }
                    }
                }
                if (!filaValidaContrato.folioContratoSAAPV().equals("")) {
                    existeFolioContrato = true;
                }
            }

            if (fila.idPersona == null) {
                fila.idPersona = 0L;
            }
            if (fila.idCliente == null) {
                fila.idCliente = 0L;
            }
            if (fila.idEmpleador == null) {
                fila.idEmpleador = 0L;
            }
            if (fila.documentoIdentidadEmpleador == null) {
                fila.documentoIdentidadEmpleador = "";
            }

            // En caso de que el FolioContrato exista en el ingreso se avisa y se continua con el ciclo
            if (existeFolioContrato) {
                Long id = p.folioContratoSAAPV() == null || p.folioContratoSAAPV().equals("") ? null : idsContratoSAAPV.get(contadorIds);
                String tipo;
                if (p.idCargaMasiva() != null) {
                    tipo = p.idCargaMasiva() == 0 ? Mensajes.MENSAJE_ERROR : Mensajes.MENSAJE_ADVERTENCIA;
                } else {
                    tipo = Mensajes.MENSAJE_ERROR;
                }
                salida.mensajes.add(new Mensaje(id, tipo, "FOLIO_CONTRATO_EXISTE", "El Folio Contrato SAAPV ya existe."));
                continue;
            }
            if (p.codigoTipoSolicitud().equals("") || (p.codigoTipoSolicitud().equals("TRA") && p.tipoMovimiento().equals("INGRESO"))) {
                // *** VALIDACION CONTRATOS SAAPV - FIN ***
                if (fila.idPersona == null || fila.idPersona == 0) {
                    // CREACION DE PERSONA
                    enr.IdEnrolamiento = 0L;
                    enr.IdEmpresa = in.idEmpresa();
                    enr.IdUsuario = in.idUsuario();
                    enr.IdTipoEntidad = "PERSONA";
                    enr.IdEntidadEnrolada = 0L;
                    enr.IdTipoDocumentoIdentidad = "R";
                    enr.IdTipoDocumentoIdentidadAsociado = "R";
                    enr.DocumentoIdentidad = p.documentoIdentidadCliente();
                    enr.IdTipoDocumentoIdentidadAdicional = "R";
                    enr.DocumentoIdentidadAdicional = "";
                    enr.Tipo = "N";
                    enr.RazonSocial = "";
                    enr.Nombres = p.nombreCliente();
                    enr.ApellidoPaterno = p.apellidoPaternoCliente();
                    enr.ApellidoMaterno = p.apellidoMaternoCliente();
                    enr.IdPaisNacimiento = p.idPaisResidenciaCliente();
                    enr.FechaNacimiento = Fecha.toDate(p.fechaNacimientoCliente());
                    enr.Sexo = p.idTipoGenero();
                    enr.EstadoCivil = p.estadoCivil();
                    enr.IdCiudad = p.idCiudadCliente();
                    enr.IdDivisionPais = p.idComunaCliente();
                    enr.DireccionCalle = p.direccionCliente();
                    enr.DireccionNumero = String.valueOf(p.numeroDireccionCliente());
                    enr.DireccionDepartamento = "";
                    if (p.telefonoMovil() != null && !p.telefonoMovil().equals("")) {
                        enr.IdTipoTelefono = "MOVIL";
                        enr.Telefono = p.telefonoMovil();
                    } else if (p.telefonoFijo() != null && !p.telefonoFijo().equals("")) {
                        enr.IdTipoTelefono = "FIJO";
                        enr.Telefono = p.telefonoFijo();
                    } else {
                        enr.IdTipoTelefono = "OTRO";
                        enr.Telefono = p.telefonoLaboral();
                    }
                    enr.Email = p.emailCliente();
                    enr.FechaCreacion = fechatime;
                    enr.FechaModificacion = fechatime;
                    enr.IdPersonaConyuge = 0L;
                    enr.RegimenConyugal = "";
                    enr.CLI_IdCategoriaInversionista = "";
                    enr.CLI_IdTipoCliente = cliIdTipoClienteDefecto;
                    enr.CLI_IdClaseCliente = "";
                    enr.CLI_IdEmpleadoEjecutivo = 0L;
                    enr.CLI_CodigoAdicionalHolding = "";
                    enr.POR_IdPerfilComercial = "";
                    enr.EMP_CodigoEmpleado = "";
                    enr.EMP_CodigoTipoCargoEmpleado = "";
                    enr.EMP_IdEmpleadoSupervisor = 0L;
                    enr.EMP_IdSucursal = 0L;
                    enr.EMP_FechaInicioCargo = fechaSistemaDate;
                    enr.EMP_FechaTerminoCargo = fechaSistemaDate;
                    enr.CNT_CodigoInscripcion = "";
                    enr.CNT_Emisor = "";
                    enr.CNT_Depositos = "";
                    enr.CNT_Bolsa = "";
                    enr.CNT_Corredora = "";
                    enr.CNT_CamaraDeCompensacion = "";
                    enr.CNT_Banco = "";
                    enr.CNT_Boveda = "";
                    enr.PRO_IdAreaProveedor = 0L;
                    enr.EMP_CargoIndefinido = "";

                    entradaPersona.p_IdEmpresa = in.idEmpresa();
                    entradaPersona.p_IdModulo = in.idModulo();
                    entradaPersona.p_IdUsuario = in.idUsuario();
                    List<Enrolamiento> enrPersona = new ArrayList<>();
                    enrPersona.add(enr);
                    entradaPersona.p_ConsultarEnrolamiento = enrPersona;

                    objOut = llamar("CrearPersona", entradaPersona, SalidaCrearPersona.class);
                    if (objOut.P_OUT_ERRORESCONTADOR == 0) {
                        fila.idPersona = objOut.P_OUT_REFID;

                        List<ConsultaDireccion> consultaDireccion = repositorio.direccionesFisicas(fila.idPersona);
                        int[] resUpdate = repositorio.copiarDireccionesConPropositoApv(consultaDireccion);

                        // LEGACY DEFECT kept (see README APV-A-401): a null IdTipoCargoPersona is unboxed in this comparison (NullPointerException)
                        if (p.idTipoCargoPersona() != 0) {
                            List<CambiarCargoPersona> listCambiarCargoPersona = new ArrayList<>();
                            listCambiarCargoPersona.add(new CambiarCargoPersona(fila.idPersona, p.idTipoCargoPersona()));
                            int[] res = repositorio.cambiarCargoPersona(listCambiarCargoPersona);
                            if (res != null && res.length > 0 && res[0] == 1) {
                                // the OK message is only logged by the legacy (never added to the output)
                            } else {
                                salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ERROR,
                                        "CAMBIAR_CARGO_PERSONA_ERROR", "Modificacion de cargo persona con problemas."));
                            }
                        }

                        // LABORAL
                        if (p.telefonoLaboral() != null) {
                            if (!p.telefonoLaboral().equals("")) {
                                entradaTelefonoPar.p_IdEmpresa = p.idEmpresa();
                                entradaTelefonoPar.p_IdUsuario = in.idUsuario();
                                entradaTelefonoPar.p_IdModulo = in.idModulo();
                                entradaTelefonoPar.p_UsaReglasNegocio = "N";
                                entradaTelefonoPar.p_IdPais = p.idPaisResidenciaCliente();
                                entradaTelefonoPar.p_IdTipoTelefono = "OTRO";
                                entradaTelefonoPar.p_Numero = p.telefonoLaboral();
                                entradaTelefonoPar.p_Anexo = "";
                                entradaTelefonoPar.p_CodigoProposito = "PREV";
                                entradaTelefonoPar.p_Comentario = "";
                                entradaTelefonoPar.p_IdEstado = "ACT";
                                entradaTelefonoPar.p_Tipo = "";
                                entradaTelefonoPar.p_IdPersona = fila.idPersona;

                                objOutFonoPar = llamar("CrearTelefono", entradaTelefonoPar, SalidaCrearTelefono.class);
                                errorTelefono(objOutFonoPar, salida, p, idsContratoSAAPV, contadorIds, "No es posible crear Telefono Particular");
                            }
                        }

                        // PARTICULAR (the legacy tests TelefonoLaboral again and sends TelefonoFijo)
                        if (p.telefonoLaboral() != null) {
                            if (!p.telefonoLaboral().equals("")) {
                                entradaTelefonoPar.p_IdEmpresa = p.idEmpresa();
                                entradaTelefonoPar.p_IdUsuario = in.idUsuario();
                                entradaTelefonoPar.p_IdModulo = in.idModulo();
                                entradaTelefonoPar.p_UsaReglasNegocio = "N";
                                entradaTelefonoPar.p_IdPais = p.idPaisResidenciaCliente();
                                entradaTelefonoPar.p_IdTipoTelefono = "FIJO";
                                entradaTelefonoPar.p_Numero = p.telefonoFijo();
                                entradaTelefonoPar.p_Anexo = "";
                                entradaTelefonoPar.p_CodigoProposito = "PREV";
                                entradaTelefonoPar.p_Comentario = "";
                                entradaTelefonoPar.p_IdEstado = "ACT";
                                entradaTelefonoPar.p_Tipo = "";
                                entradaTelefonoPar.p_IdPersona = fila.idPersona;

                                objOutFonoPar = llamar("CrearTelefono", entradaTelefonoPar, SalidaCrearTelefono.class);
                                errorTelefono(objOutFonoPar, salida, p, idsContratoSAAPV, contadorIds, "No es posible crear Telefono Particular");
                            }
                        }
                        // MOVIL
                        if (p.telefonoMovil() != null) {
                            if (!p.telefonoMovil().equals("")) {
                                entradaTelefonoCom.p_IdEmpresa = p.idEmpresa();
                                entradaTelefonoCom.p_IdUsuario = in.idUsuario();
                                entradaTelefonoCom.p_IdModulo = in.idModulo();
                                entradaTelefonoCom.p_UsaReglasNegocio = "N";
                                entradaTelefonoCom.p_IdPais = p.idPaisResidenciaCliente();
                                entradaTelefonoCom.p_IdTipoTelefono = "MOVIL";
                                entradaTelefonoCom.p_Numero = p.telefonoMovil();
                                entradaTelefonoCom.p_Anexo = "";
                                entradaTelefonoCom.p_CodigoProposito = "PREV";
                                entradaTelefonoCom.p_Comentario = "";
                                entradaTelefonoCom.p_IdEstado = "ACT";
                                entradaTelefonoCom.p_Tipo = "";
                                entradaTelefonoCom.p_IdPersona = fila.idPersona;

                                objOutFonoCom = llamar("CrearTelefono", entradaTelefonoCom, SalidaCrearTelefono.class);
                                errorTelefono(objOutFonoCom, salida, p, idsContratoSAAPV, contadorIds, "No es posible crear Telefono Comercial");
                            }
                        }

                        if (p.emailCliente() != null) {
                            if (!p.emailCliente().equals("")) {
                                entradaMail.p_IdEmpresa = p.idEmpresa();
                                entradaMail.p_IdUsuario = in.idUsuario();
                                entradaMail.p_IdModulo = in.idModulo();
                                entradaMail.p_UsaReglasNegocio = "N";
                                entradaMail.p_Direccion = p.emailCliente();
                                entradaMail.p_CodigoProposito = "PREV";
                                entradaMail.p_Comentario = "";
                                entradaMail.p_IdEstado = "ACT";
                                entradaMail.p_IdPersona = fila.idPersona;

                                objOutMail = llamar("CrearDireccionElectronica", entradaMail, SalidaCrearDireccionElectronica.class);

                                if (objOutMail.P_OUT_ERRORESCONTADOR != 0) {
                                    for (ErrorDetalle lineaErroremail : objOutMail.P_OUT_ERRORESDETALLE) {
                                        if (lineaErroremail.Codigo.compareTo("UI_ERROR_AL_CREAR_EMAIL") == 0) {
                                            // only logged by the legacy
                                        } else {
                                            salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ERROR,
                                                    "ERROR_CREAR_EMAIL", "No es posible crear email Cliente"));
                                        }
                                    }
                                }
                            }
                        }

                        salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_OK, "CREACION_PERSONA_OK",
                                "Creacion de persona exitosa"));
                    } else {
                        for (ErrorDetalle lineaError : objOut.P_OUT_ERRORESDETALLE) {
                            if (lineaError.Codigo.compareTo("PERSONA_EXISTE") == 0) {
                                salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ADVERTENCIA, "PERSONA_EXISTE",
                                        "La Persona ya Existe."));
                            } else {
                                salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ADVERTENCIA,
                                        "ERROR_CREAR_PERSONA", "No es posible crear persona"));
                            }
                        }
                    }
                }

                if ((fila.idEmpleador == null || fila.idEmpleador == 0) && !fila.documentoIdentidadEmpleador.equals("")) {
                    // Creacion Empleador
                    enr.IdEnrolamiento = 0L;
                    enr.IdEmpresa = in.idEmpresa();
                    enr.IdUsuario = in.idUsuario();
                    enr.IdTipoEntidad = "PERSONA";
                    enr.IdEntidadEnrolada = 0L;
                    enr.IdTipoDocumentoIdentidad = "R";
                    enr.IdTipoDocumentoIdentidadAsociado = "R";
                    enr.DocumentoIdentidad = p.documentoIdentidadEmpleador();
                    enr.IdTipoDocumentoIdentidadAdicional = "R";
                    enr.DocumentoIdentidadAdicional = "";
                    enr.Tipo = "J";
                    enr.RazonSocial = p.razonSocialEmpleador();
                    enr.Nombres = "";
                    enr.ApellidoPaterno = "";
                    enr.ApellidoMaterno = "";
                    enr.IdPaisNacimiento = p.idPaisEmpleador();
                    enr.FechaNacimiento = fechaSistemaDate;
                    enr.Sexo = "";
                    enr.EstadoCivil = "";
                    enr.IdCiudad = p.idCiudadEmpleador();
                    enr.IdDivisionPais = p.idComunaEmpleador();
                    enr.DireccionCalle = p.direccionComercialEmpleador();
                    enr.DireccionNumero = String.valueOf(p.numeroDireccionEmpleador());
                    enr.DireccionDepartamento = "";
                    if (p.telefonoLaboral() != null && !p.telefonoLaboral().equals("")) {
                        enr.IdTipoTelefono = "OTRO";
                        enr.Telefono = p.telefonoLaboral();
                    } else if (p.telefonoFijo() != null && !p.telefonoFijo().equals("") && p.telefonoLaboral().equals("")) {
                        enr.IdTipoTelefono = "FIJO";
                        enr.Telefono = p.telefonoFijo();
                    } else if (p.telefonoMovil() != null && p.telefonoLaboral().equals("") && p.telefonoFijo().equals("")) {
                        enr.IdTipoTelefono = "MOVIL";
                        enr.Telefono = p.telefonoMovil();
                    }
                    enr.Email = "";
                    enr.FechaCreacion = fechatime;
                    enr.FechaModificacion = fechatime;
                    enr.IdPersonaConyuge = 0L;
                    enr.RegimenConyugal = "";
                    enr.CLI_IdCategoriaInversionista = "";
                    enr.CLI_IdTipoCliente = cliIdTipoClienteDefecto;
                    enr.CLI_IdClaseCliente = "";
                    enr.CLI_IdEmpleadoEjecutivo = 0L;
                    enr.CLI_CodigoAdicionalHolding = "";
                    enr.POR_IdPerfilComercial = "";
                    enr.EMP_CodigoEmpleado = "";
                    enr.EMP_CodigoTipoCargoEmpleado = "";
                    enr.EMP_IdEmpleadoSupervisor = 0L;
                    enr.EMP_IdSucursal = 0L;
                    enr.EMP_FechaInicioCargo = fechaSistemaDate;
                    enr.EMP_FechaTerminoCargo = fechaSistemaDate;
                    enr.CNT_CodigoInscripcion = "";
                    enr.CNT_Emisor = "";
                    enr.CNT_Depositos = "";
                    enr.CNT_Bolsa = "";
                    enr.CNT_Corredora = "";
                    enr.CNT_CamaraDeCompensacion = "";
                    enr.CNT_Banco = "";
                    enr.CNT_Boveda = "";
                    enr.PRO_IdAreaProveedor = 0L;
                    enr.EMP_CargoIndefinido = "";

                    entradaPersona.p_IdEmpresa = in.idEmpresa();
                    entradaPersona.p_IdModulo = in.idModulo();
                    entradaPersona.p_IdUsuario = in.idUsuario();
                    List<Enrolamiento> enrEmpleador = new ArrayList<>();
                    enrEmpleador.add(enr);
                    entradaPersona.p_ConsultarEnrolamiento = enrEmpleador;

                    objOut = llamar("CrearPersona", entradaPersona, SalidaCrearPersona.class);
                    if (objOut.P_OUT_ERRORESCONTADOR == 0) {
                        // the legacy builds the "Creacion de persona Empleador exitosa" message but never adds it to the output
                        fila.idEmpleador = objOut.P_OUT_REFID;

                        List<IndicadorEncargadoRRHH> lstEncargado = new ArrayList<>();
                        List<TipoIndicador> lstTipoIndicador = repositorio.tiposIndicador("PERSONA", "DATOS EMPLEADOR");
                        boolean existeTipoIndicador = (lstTipoIndicador.isEmpty()) ? false : true;
                        boolean indicadoresEmpleadoSonVacio = "".equalsIgnoreCase(p.nombreEncargado()) && "".equalsIgnoreCase(p.emailEncargado())
                                && "".equalsIgnoreCase(p.telefonoEncargado()) ? true : false;
                        if (existeTipoIndicador) {
                            for (TipoIndicador tp : lstTipoIndicador) {
                                String valorIndicador = null;
                                if (tp.CodigoTipoIndicador().compareTo("NOMBRE_ENCARGADO_RRHH") == 0) {
                                    valorIndicador = indicadoresEmpleadoSonVacio ? "N" : p.nombreEncargado();
                                } else if (tp.CodigoTipoIndicador().compareTo("MAIL_ENCARGADO_RRHH") == 0) {
                                    valorIndicador = indicadoresEmpleadoSonVacio ? "N" : p.emailEncargado();
                                } else if (tp.CodigoTipoIndicador().compareTo("TELEFONO_ENCARGADO_RRHH") == 0) {
                                    valorIndicador = indicadoresEmpleadoSonVacio ? "N" : p.telefonoEncargado();
                                }
                                lstEncargado.add(new IndicadorEncargadoRRHH(0L, tp.IdTipoIndicador(), valorIndicador,
                                        Fecha.toDate("0001-01-01 00:00:00")));
                            }
                        }

                        repositorio.crearTablaRegistroAtributo();
                        boolean tablaLimpia = false;
                        try {

                            if (lstEncargado.size() > 0) {
                                repositorio.insertarRegistroAtributo(lstEncargado);
                            }

                            Map<String, Object> paramsIn = new TreeMap<>();
                            paramsIn.put("IdEmpresa", in.idEmpresa());
                            paramsIn.put("IdUsuario", in.idUsuario());
                            paramsIn.put("IdModulo", in.idModulo());
                            paramsIn.put("IdEntidad", fila.idEmpleador);
                            paramsIn.put("RegistroAtributo", lstEncargado);

                            JsonObject resultSP = procedimiento(SP_MODIFICAR_INDICADOR, paramsIn);

                            if (Json.largo(resultSP, "p_out_ErroresContador").equals(0L)) {
                                // Ingreso de indicador exitoso
                            } else {
                                for (JsonObject errorDetalle : listaONull(resultSP, "p_out_ErroresDetalle")) {
                                    salidaCliente.addError(Json.texto(errorDetalle, "Codigo"), Json.texto(errorDetalle, "Descripcion"));
                                    salida.mensajes.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ERROR, "ERROR_ENCARGADO_RRHH",
                                            "No es posible Ingresar encargado de recursos humanos."));
                                }
                                for (JsonObject advertenciaDetalle : listaONull(resultSP, "p_out_AdvertenciasDetalle")) {
                                    salidaCliente.addAdvertencia(Json.texto(advertenciaDetalle, "Codigo"), Json.texto(advertenciaDetalle, "Descripcion"));
                                    salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ADVERTENCIA,
                                            "ADVERTENCIA_ENCARGADO_RRHH", "No es posible Ingresar encargado de recursos humanos."));
                                }
                            }

                            repositorio.eliminarTablaRegistroAtributo();
                            tablaLimpia = true;
                        } finally {
                            // the session-scoped temporary table must not outlive a failed block (the pooled connection is reused)
                            if (!tablaLimpia) {
                                try {
                                    repositorio.eliminarTablaRegistroAtributo();
                                } catch (Exception ignorada) {
                                    // best effort, the original failure is the one that matters
                                }
                            }
                        }
                    } else {
                        for (ErrorDetalle lineaError : objOut.P_OUT_ERRORESDETALLE) {
                            if (lineaError.Codigo.compareTo("PERSONA_EXISTE") == 0) {
                                salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ADVERTENCIA,
                                        "PERSONA_EMPLEADOR_EXISTE", "Persona Empleador ya existe."));
                            } else {
                                salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ERROR, "ERROR_CREAR_PERSONA",
                                        "No es posible crear persona Empleador"));
                            }
                        }
                    }
                }
                List<String> resultadoValidacion = validaCamposPersona("CLIENTE", p, null);
                if (resultadoValidacion.size() == 0) {
                    if (fila.idCliente == null || fila.idCliente == 0) {
                        // Creacion Cliente
                        enr.IdEnrolamiento = 0L;
                        enr.IdEmpresa = in.idEmpresa();
                        enr.IdUsuario = in.idUsuario();
                        enr.IdTipoEntidad = "CLIENTE";
                        enr.IdEntidadEnrolada = 0L;
                        enr.IdTipoDocumentoIdentidad = "R";
                        enr.IdTipoDocumentoIdentidadAsociado = "R";
                        enr.DocumentoIdentidad = p.documentoIdentidadCliente();
                        enr.IdTipoDocumentoIdentidadAdicional = "R";
                        enr.DocumentoIdentidadAdicional = "";
                        enr.Tipo = "N";
                        enr.RazonSocial = "";
                        enr.Nombres = p.nombreCliente();
                        enr.ApellidoPaterno = p.apellidoPaternoCliente();
                        enr.ApellidoMaterno = p.apellidoMaternoCliente();
                        enr.IdPaisNacimiento = p.idPaisResidenciaCliente();
                        enr.FechaNacimiento = fechaSistemaDate;
                        enr.Sexo = p.idTipoGenero();
                        enr.EstadoCivil = p.estadoCivil();
                        enr.IdCiudad = p.idCiudadCliente();
                        enr.IdDivisionPais = p.idComunaCliente();
                        enr.DireccionCalle = p.direccionCliente();
                        enr.DireccionNumero = "";
                        enr.DireccionDepartamento = "";
                        if (p.telefonoMovil() != null && !p.telefonoMovil().equals("")) {
                            enr.IdTipoTelefono = "MOVIL";
                            enr.Telefono = p.telefonoMovil();
                        } else if (p.telefonoFijo() != null && p.telefonoMovil().equals("") && !p.telefonoFijo().equals("")) {
                            enr.IdTipoTelefono = "FIJO";
                            enr.Telefono = p.telefonoFijo();
                        } else if (p.telefonoLaboral() != null && p.telefonoMovil().equals("") && p.telefonoFijo().equals("")
                                && !p.telefonoLaboral().equals("")) {
                            enr.IdTipoTelefono = "OTRO";
                            enr.Telefono = p.telefonoLaboral();
                        }
                        enr.Email = p.emailCliente();
                        enr.FechaCreacion = fechatime;
                        enr.FechaModificacion = fechatime;
                        enr.IdPersonaConyuge = 0L;
                        enr.RegimenConyugal = "";
                        enr.CLI_IdCategoriaInversionista = p.idCategoriaInversionista();
                        enr.CLI_IdTipoCliente = cliIdTipoClienteDefecto;
                        enr.CLI_IdClaseCliente = "";
                        enr.CLI_IdEmpleadoEjecutivo = p.idEmpleado();
                        enr.CLI_CodigoAdicionalHolding = "";
                        enr.POR_IdPerfilComercial = p.idPerfilComercial();
                        enr.EMP_CodigoEmpleado = "";
                        enr.EMP_CodigoTipoCargoEmpleado = "";
                        enr.EMP_IdEmpleadoSupervisor = 0L;
                        enr.EMP_IdSucursal = 0L;
                        enr.EMP_FechaInicioCargo = fechaSistemaDate;
                        enr.EMP_FechaTerminoCargo = fechaSistemaDate;
                        enr.CNT_CodigoInscripcion = "";
                        enr.CNT_Emisor = "";
                        enr.CNT_Depositos = "";
                        enr.CNT_Bolsa = "";
                        enr.CNT_Corredora = "";
                        enr.CNT_CamaraDeCompensacion = "";
                        enr.CNT_Banco = "";
                        enr.CNT_Boveda = "";
                        enr.PRO_IdAreaProveedor = 0L;
                        enr.EMP_CargoIndefinido = "";
                        enr.CLI_ClienteFinal = "N";
                        enr.POR_IdPersonaBrokerExtranjero = 0L;

                        entradaCliente.p_IdEmpresa = in.idEmpresa();
                        entradaCliente.p_IdModulo = in.idModulo();
                        entradaCliente.p_IdUsuario = in.idUsuario();
                        List<Enrolamiento> enrCliente = new ArrayList<>();
                        enrCliente.add(enr);
                        entradaCliente.p_ConsultarEnrolamiento = enrCliente;

                        salidaCliente = llamar("CrearCliente", entradaCliente, SalidaCrearCliente.class);
                        if (salidaCliente.P_OUT_REFIDCLIENTE != 0) {
                            fila.idCliente = salidaCliente.P_OUT_REFIDCLIENTE;
                            salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_OK, "CREACION_CLIENTE_OK",
                                    "Creacion de cliente exitosa"));
                        }
                        if (salidaCliente.P_OUT_ERRORESCONTADOR > 0) {
                            for (ErrorDetalle lineaError : salidaCliente.P_OUT_ERRORESDETALLE) {
                                if (lineaError.Codigo.equals("CLIENTE_EXISTE")) {
                                    salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ADVERTENCIA,
                                            "CLIENTE_EXISTE", "CLIENTE_EXISTE"));
                                } else if (lineaError.Codigo.equals("PERFIL_COMERCIAL_NO_EXISTE")) {
                                    salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ADVERTENCIA,
                                            "PERFIL_NO_EXISTE", "PERFIL_COMERCIAL_NO_EXISTE"));
                                } else if (lineaError.Codigo.equals("CLIENTE_NO_PUEDE_SER_ENROLADO")) {
                                    // LEGACY DEFECT kept (see README APV-A-408): the CLIENTE_NO_PUEDE_CREAR_PORTAFOLIO warning is built but never added
                                } else {
                                    salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ERROR,
                                            "ERROR_CREAR_CLIENTE", "No es posible crear cliente"));
                                }
                            }
                        }
                    }
                } else {
                    salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ADVERTENCIA, "VALIDA_DATOS_MINIMOS_CLIENTE",
                            "No es posible completar la creacion del cliente por datos faltantes."));
                }

                // Se ingresa Relacion Persona Cliente Empleador
                if ((fila.idPersona != null || fila.idPersona != 0) && ((fila.idEmpleador != null && fila.idEmpleador != 0) || fila.idEmpleador > 0)) {
                    // Valida la existencia de la relacion de la persona con el empleador
                    List<ConsultaRelacionPersona> consultaRelacion = repositorio.relacionesPersona(fila.idPersona, fila.idEmpleador);

                    boolean existeRelacionPersona = (consultaRelacion.isEmpty()) ? false : true;
                    if (!existeRelacionPersona) {
                        Map<String, Object> paramsIn = new TreeMap<>();
                        paramsIn.put("IdEmpresa", in.idEmpresa());
                        paramsIn.put("IdUsuario", in.idUsuario());
                        paramsIn.put("IdModulo", in.idModulo());
                        paramsIn.put("IdTipoRelacion", "EMPDOR");
                        paramsIn.put("IdPersonaOrigen", fila.idPersona);
                        paramsIn.put("IdPersonaDestino", fila.idEmpleador);
                        paramsIn.put("IdEstado", "ACT");
                        paramsIn.put("FechaInicioRelacion", fechaSistemaDate);
                        paramsIn.put("FechaFinRelacion", Fecha.toDate(fechaFinRelacionDefecto + " 00:00:00"));
                        paramsIn.put("PorcentajeParticipacion", 0);

                        JsonObject resultSP = procedimiento(SP_CREAR_RELACION, paramsIn);
                        if (Json.largo(resultSP, "p_out_ErroresContador").equals(0L)) {
                            idRelacionPersona = Json.largo(resultSP, "p_out_RefId");
                        } else {
                            for (JsonObject errorDetalle : listaONull(resultSP, "p_out_ErroresDetalle")) {
                                salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ERROR,
                                        Json.texto(errorDetalle, "Codigo"), Json.texto(errorDetalle, "Descripcion")));
                                salidaCliente.addError(Json.texto(errorDetalle, "Codigo"), Json.texto(errorDetalle, "Descripcion"));
                            }
                            for (JsonObject advertenciaDetalle : listaONull(resultSP, "p_out_AdvertenciasDetalle")) {
                                salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ADVERTENCIA,
                                        Json.texto(advertenciaDetalle, "Codigo"), Json.texto(advertenciaDetalle, "Descripcion")));
                                salidaCliente.addAdvertencia(Json.texto(advertenciaDetalle, "Codigo"), Json.texto(advertenciaDetalle, "Descripcion"));
                            }
                        }
                    }
                }
            }
            if (p.codigoTipoSolicitud().equals("")) {
                if (portafolios.size() > 0 && fila.idCliente != 0) {
                    for (Portafolio filaCreacionPortafolio : portafolios) {
                        // LEGACY DEFECT kept (see README APV-A-421): IdInterno is compared with == on Long objects (reference comparison)
                        if (p.idInterno() == filaCreacionPortafolio.p.idInterno()) {
                            Map<String, Object> paramsIn = new TreeMap<>();
                            paramsIn.put("IdEmpresa", in.idEmpresa());
                            paramsIn.put("IdUsuario", in.idUsuario());
                            paramsIn.put("IdModulo", in.idModulo());
                            paramsIn.put("PropositoPortafolio", filaCreacionPortafolio.p.idPropositoPortafolio());
                            paramsIn.put("IdCliente", fila.idCliente);
                            paramsIn.put("IdPerfilComercial", p.idPerfilComercial());
                            paramsIn.put("CodigoPortafolio", filaCreacionPortafolio.p.regimen());
                            paramsIn.put("CodigoRegimen", filaCreacionPortafolio.p.regimen());
                            paramsIn.put("NombrePlan", p.nombrePlan());
                            paramsIn.put("CodigoAgrupador", filaCreacionPortafolio.p.codigoAgrupador());

                            JsonObject resultSP = procedimiento(SP_CREAR_PORTAFOLIO, paramsIn);

                            if (Json.largo(resultSP, "p_out_ErroresContador").equals(0L) && Json.largo(resultSP, "p_out_AdvertenciasContador").equals(0L)) {
                                salidaCliente.P_OUT_REFIDPORTAFOLIO = Json.largo(resultSP, "p_out_RefId");
                                salida.refIdPortafolio = salidaCliente.P_OUT_REFIDPORTAFOLIO;
                            } else {
                                for (JsonObject errorDetalle : listaONull(resultSP, "p_out_ErroresDetalle")) {
                                    salidaCliente.addError(Json.texto(errorDetalle, "Codigo"), Json.texto(errorDetalle, "Descripcion"));
                                    String tipo;
                                    if (p.idCargaMasiva() != null) {
                                        tipo = p.idCargaMasiva() == 0 ? Mensajes.MENSAJE_ERROR : Mensajes.MENSAJE_ADVERTENCIA;
                                    } else {
                                        tipo = Mensajes.MENSAJE_ERROR;
                                    }
                                    salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), tipo, Json.texto(errorDetalle, "Codigo"),
                                            Json.texto(errorDetalle, "Descripcion")));
                                }
                                for (JsonObject advertenciaDetalle : listaONull(resultSP, "p_out_AdvertenciasDetalle")) {
                                    salidaCliente.addAdvertencia(Json.texto(advertenciaDetalle, "Codigo"), Json.texto(advertenciaDetalle, "Descripcion"));
                                    salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ADVERTENCIA,
                                            Json.texto(advertenciaDetalle, "Codigo"), Json.texto(advertenciaDetalle, "Descripcion")));
                                }
                            }
                            // LEGACY DEFECT kept (see README APV-A-389): the client answer object lives across rows, so a previous REFIDPORTAFOLIO is reused
                            if (salidaCliente.P_OUT_REFIDPORTAFOLIO != 0) {
                                filaCreacionPortafolio.idPortafolio = salidaCliente.P_OUT_REFIDPORTAFOLIO;
                                idPortafolioAPV = salidaCliente.P_OUT_REFIDPORTAFOLIO;
                                salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_OK, "CREACION_PORTAFOLIO_OK",
                                        "Creacion de portafolio exitosa"));
                            }
                        }
                    }
                } else {
                    salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ADVERTENCIA, "VALIDA_DATOS_MINIMOS_CLIENTE",
                            "No es posible Crear Plan de Ahorro (Portafolio) porque existen datos pendientes de Cliente."));
                }
            }

            if (p.codigoTipoSolicitud().equals("") || (p.codigoTipoSolicitud().equals("TRA") && p.tipoMovimiento().equals("INGRESO"))) {
                ContratoNuevo pc = nuevoContrato(fila);

                int[] res;
                try {
                    res = repositorio.insertarContrato(pc);
                    agregarLogSAAPV(in, filas, "");
                } catch (Exception er) {
                    String codigoError = er.getMessage().substring(er.getMessage().indexOf("[") + 1, er.getMessage().indexOf("]"));
                    if ("301".equalsIgnoreCase(codigoError)) {
                        String tipo;
                        if (p.idCargaMasiva() != null) {
                            tipo = p.idCargaMasiva() == 0 ? Mensajes.MENSAJE_ERROR : Mensajes.MENSAJE_ADVERTENCIA;
                        } else {
                            tipo = Mensajes.MENSAJE_ERROR;
                        }
                        salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), tipo, "FOLIO_CONTRATO_EXISTE",
                                "El Folio Contrato SAAPV ya existe."));
                    } else {
                        salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ERROR, "ERROR_CREAR_CONTRATO",
                                "Problemas al crear contrato"));
                    }
                    return salida;
                }

                if (res != null && res.length > 0 && res[0] == 1) {
                    String descripcion = "";
                    if (p.folioContratoSAAPV() == null || p.folioContratoSAAPV().equals("")) {
                        descripcion = "Creacion de contrato exitosa";
                    } else {
                        descripcion = "Creacion de contrato exitosa Folio: " + p.folioContratoSAAPV();
                    }
                    salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_OK, "CREACION_CONTRATO_OK", descripcion));

                    if (in.tipo().compareTo("M") == 0) {
                        List<ContratoConsultado> contratoConsultado = repositorio.consultarContrato(idContratoSAAPVBorrar);
                        for (ContratoConsultado linea : contratoConsultado) {
                            if (linea.IdEstado().compareTo("PDI") == 0 || linea.IdEstado().compareTo("PEN") == 0) {
                                List<ContratoConsultado> paramBorrar = new ArrayList<>();
                                paramBorrar.add(new ContratoConsultado(idContratoSAAPVBorrar, null));
                                int[] resDelete = repositorio.eliminarContrato(paramBorrar);
                                if (resDelete != null && resDelete.length > 0 && resDelete[0] == 1) {
                                    agregarLogSAAPV(in, filas, "ELIMINAR");
                                    // LEGACY DEFECT kept (see README APV-A-418): the ELIMINA_* messages are built but never added to the output
                                    int[] resDeleteDistrib = repositorio.eliminarDistribucion(paramBorrar);
                                }
                            }
                        }
                    } else {
                        repositorio.crearTablaPortafoliosIngresados();
                        boolean tablaLimpia = false;
                        try {

                            List<PortafolioACerrar> portafoliosACerrar = new ArrayList<>();
                            for (Portafolio linea : portafolios) {
                                portafoliosACerrar.add(new PortafolioACerrar(linea.idPortafolio, linea.p.producto()));
                            }

                            repositorio.insertarPortafoliosIngresados(portafoliosACerrar);

                            List<ContratoSAAPVID> contratosMismosRequisitos = repositorio.contratosConLasMismasCondiciones(p.idMedioRecaudacionFormaPago(),
                                    p.formaDePago());

                            MedioRecaudacion medioRecaudacionPAC = repositorio.mediosRecaudacionPac().stream().findFirst().orElse(MedioRecaudacion.vacio());

                            List<ContratoSAAPVID> listaEntradaCerrarContratos = new ArrayList<>();

                            for (ContratoSAAPVID linea : contratosMismosRequisitos) {
                                if (linea.Pago().equalsIgnoreCase(portafolios.get(0).p.pago())) {
                                    if ("UNI".equalsIgnoreCase(linea.Pago()) && linea.PeriodoInicio().equalsIgnoreCase(portafolios.get(0).p.mesInicio())) {
                                        if (debeCerrarContratoUnico(linea, p, medioRecaudacionPAC)) {
                                            listaEntradaCerrarContratos.add(ContratoSAAPVID.soloId(linea.IdContratoSAAPV()));
                                        }
                                    } else if (!"UNI".equalsIgnoreCase(linea.Pago()) && !("IND").equalsIgnoreCase(linea.Pago())) {
                                        listaEntradaCerrarContratos.add(ContratoSAAPVID.soloId(linea.IdContratoSAAPV()));
                                    } else if (("IND").equalsIgnoreCase(linea.Pago()) && debeCerrarContratoIndefinido(linea, portafolios, fila)) {
                                        listaEntradaCerrarContratos.add(ContratoSAAPVID.soloId(linea.IdContratoSAAPV()));
                                    }
                                }
                            }

                            int[] resCerrarContrato = repositorio.cerrarContratos(listaEntradaCerrarContratos);
                            if (resCerrarContrato != null && resCerrarContrato.length > 0) {
                                agregarLogSAAPV(in, filas, "CERRAR");
                                salida.mensajes.add(new Mensaje(1L, Mensajes.MENSAJE_OK, "CONTRATO_CERRADO_OK", "Se cierra el contrato anterior"));
                                int[] resCerrarDistrib = repositorio.cerrarDistribuciones(listaEntradaCerrarContratos);
                                if (resCerrarDistrib != null && resCerrarDistrib.length > 0) {
                                    salida.mensajes.add(new Mensaje(1L, Mensajes.MENSAJE_OK, "CIERRA_DISTRIBUCION_CONTRATO_OK",
                                            "Se cierra la Distribucion Inversion anterior"));
                                }
                            }

                            repositorio.eliminarTablaPortafoliosIngresados();
                            tablaLimpia = true;
                        } finally {
                            // the session-scoped temporary table must not outlive a failed block (the pooled connection is reused)
                            if (!tablaLimpia) {
                                try {
                                    repositorio.eliminarTablaPortafoliosIngresados();
                                } catch (Exception ignorada) {
                                    // best effort, the original failure is the one that matters
                                }
                            }
                        }
                    }

                    // Valido datos minimos del contrato para ingresarlo vig o pdi
                    List<String> resultadoValidacionContrato;
                    if (portafolios.size() > 0) {
                        resultadoValidacionContrato = validaCamposPersona("DATOS_MINIMOS", p, portafolios.get(0).p);
                    } else {
                        resultadoValidacionContrato = validaCamposPersona("DATOS_MINIMOS", p, null);
                    }

                    if (resultadoValidacionContrato.size() == 0 && !p.codigoTipoSolicitud().equals("TRA") && (distribucion.size() > 0)) {
                        // Se cambia estado a VIG
                        List<cl.bcs.apv.api.dto.cambiarestadocontratos.ContratosSAAPV> tablaContratos = new ArrayList<>();
                        tablaContratos.add(new cl.bcs.apv.api.dto.cambiarestadocontratos.ContratosSAAPV(fila.idPersona, fila.idCliente, fila.idEmpleador,
                                p.folioContratoSAAPV() == null || p.folioContratoSAAPV().equals("") ? null : p.folioContratoSAAPV(), "1900-01-01",
                                p.idEstadoEnvioNotificacion(), "PDA", "VIG", 0L, ""));
                        // the legacy keeps the answer in a variable it never reads
                        cambiarEstadoContratos.ejecutar(new CambiarEstadoContratosEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), "VIG",
                                tablaContratos, "N"));
                    } else {
                        salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ADVERTENCIA, "VALIDA_DATOS_MINIMOS_CONTRATO",
                                "Contrato Ingresado con datos Pendientes de completar."));
                        for (int i = 0; i < resultadoValidacionContrato.size(); i++) {
                            if (resultadoValidacionContrato.get(i).equals("Razon Social.")
                                    || resultadoValidacionContrato.get(i).equals("Documento Identidad Empleador.")
                                    || resultadoValidacionContrato.get(i).equals("Direccion Comercial Empleador.")
                                    || resultadoValidacionContrato.get(i).equals("Numero Direccion Empleador.")) {
                                salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ADVERTENCIA,
                                        "VALIDA_DATOS_MINIMOS_CONTRATO", "Trabajador 'DEPENDIENTE' sin informar Empleador."));
                                break;
                            }
                        }

                        if (!p.codigoTipoSolicitud().equals("TRA") && (distribucion.size() == 0) && "N".equals(p.esAporteCuentaInversion())) {
                            salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ADVERTENCIA,
                                    "VALIDA_DATOS_MINIMOS_CONTRATO", "No posee distribucion inversion."));
                            // LEGACY DEFECT kept (see README APV-A-386): this 'break' ends the loop over the contracts: the following rows are not processed
                            break;
                        }
                    }

                } else {
                    salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ERROR, "ERROR_CREAR_CONTRATO",
                            "Problemas al crear contrato"));
                }
                if (distribucion.size() > 0) {
                    List<DistribucionNueva> lstInversion = new ArrayList<>();
                    for (DistribucionInversion row : distribucion) {
                        // LEGACY DEFECT kept (see README APV-A-421): IdInterno compared with == on Long objects
                        if (p.idInterno() == row.idInterno() && row.idInstrumento() != 0) {
                            lstInversion.add(new DistribucionNueva(fila.idContratoSAAPV, row.folioContratoSAAPV(), row.idInstrumento(), row.nemotecnico(),
                                    row.montoPesos(), row.porcentaje(), row.idEstado()));
                        }
                    }
                    if (lstInversion.size() > 0) {
                        // the legacy ValidaDistribucion marks every row "SI" and round-trips the values through text: the rows are the same
                        List<DistribucionNueva> lstInversionOK = new ArrayList<>(lstInversion);

                        int[] result = repositorio.insertarDistribuciones(lstInversionOK);
                        if (result != null && result.length > 0 && result[0] == 1) {
                            salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_OK, "CREAR_DISTRIBUCION_OK",
                                    "Creacion Distribucion Inversion exitosa"));
                        } else {
                            salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ERROR, "ERROR_CREAR_DISTRIBUCION",
                                    "No es posible crear Distribucion"));
                        }
                    }
                }

                // Se crea relacion entre el Contrato SAAPV y el Portafolio
                if (portafolios.size() > 0 && !p.codigoTipoSolicitud().equals("TRA") && fila.idCliente != 0) {
                    for (Portafolio row : portafolios) {
                        if (p.idInterno() == row.p.idInterno()) {
                            if (row.idPortafolio != 0) {
                                List<PortafolioContratoNuevo> listPortCont = new ArrayList<>();
                                listPortCont.add(new PortafolioContratoNuevo(row.p.origenAhorro(), row.idPortafolio, fila.idContratoSAAPV,
                                        row.p.montoPesos(), row.p.montoUF(), row.p.porcentajeRegimenImponible(), row.p.pago(), row.p.mesInicio(),
                                        row.p.mesTermino(), "VIG"));

                                int[] result = repositorio.insertarPortafoliosContrato(listPortCont);

                                if (result != null && result.length > 0 && result[0] == 1) {
                                    salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_OK,
                                            "CREAR_PORTAFOLIO_CONTRATO_OK", "Se crea relacion entre portafolio con contrato SAAPV."));
                                } else {
                                    salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ERROR,
                                            "ERROR_CREAR_PORTAFOLIO_CONTRATO", "No es posible crear relacion entre portafolio con contrato SAAPV."));
                                }
                            } else {
                                salida.mensajes.add(new Mensaje(idFolio(p, idsContratoSAAPV, contadorIds), Mensajes.MENSAJE_ERROR,
                                        "ERROR_CREAR_PORTAFOLIO_CONTRATO",
                                        "No es posible crear relacion entre portafolio con contrato SAAPV. Debe coincidir el plan de ahorro del contrato y portafolio del cliente."));
                            }
                        }
                    }
                }
            }

            // INICIO - CREACION DE TRASPASO
            if (p.codigoTipoSolicitud().equals("TRA")) {
                fila.fechaCreacion = (fila.fechaCreacion == null || fila.fechaCreacion.equals("")) ? fechaSistema : fila.fechaCreacion;
                List<String> validatraspaso = validaCamposPersona("TRASPASO", p, null);
                List<Long> idsContratoTraspasoSAAPV = siguientes(SEQ_TRASPASO, 1L);
                Long idTraspaso = idsContratoTraspasoSAAPV.get(0);
                long idContratoTraspaso = p.tipoMovimiento().equals("INGRESO") ? fila.idContratoSAAPV : 0;
                List<TraspasoNuevo> lstTraspaso = new ArrayList<>();
                lstTraspaso.add(new TraspasoNuevo(idTraspaso, idContratoTraspaso, p.idEmpresa(), p.tipoMovimiento(), p.razonRetiro(), fila.fechaCreacion,
                        p.tipoMovimiento().equals("INGRESO") ? "0001-01-01" : fila.fechaCreacion, "0001-01-01", p.tipoTraspaso(), p.descripcion(),
                        fila.idCliente, p.idPersonaOrigen(), p.tipoDeAhorro(), p.nombreEntidadOrigen(), p.rutEntidadOrigen(),
                        validatraspaso.size() == 0 ? "PDI" : "TPD", p.canal(), "0001-01-01", p.fechaSuscripcion()));
                int[] resultado = repositorio.insertarTraspaso(lstTraspaso);
                if (resultado != null && resultado.length > 0 && resultado[0] == 1) {
                    salida.mensajes.add(new Mensaje(idTraspaso, Mensajes.MENSAJE_OK, "CREACION_TRASPASO_OK",
                            "Creacion del traspaso exitosa Folio: " + idTraspaso.toString()));

                    if (p.tipoMovimiento().equals("EGRESO")) {
                        if (apruebaTraspasoAutomatico.equals("N")) {
                            // Modifico estado de traspaso a Pendiente de Autorizacion
                            List<TraspasoEstado> lstTraspasoMod = new ArrayList<>();
                            lstTraspasoMod.add(new TraspasoEstado("PDA", idTraspaso));

                            int[] resultadoMod = repositorio.modificarEstadoTraspaso(lstTraspasoMod);
                            if (resultadoMod != null && resultadoMod.length > 0 && resultadoMod[0] == 1) {
                                salida.mensajes.add(new Mensaje(idTraspaso, Mensajes.MENSAJE_OK, "MODIFICA_ESTADO_OK",
                                        "Modifica estado del Traspaso Folio: " + idTraspaso.toString()));
                            } else {
                                salida.mensajes.add(new Mensaje(idTraspaso, Mensajes.MENSAJE_ERROR, "ERROR_MODIFICA_ESTADO",
                                        "Problema al modificar estado del Traspaso."));
                            }

                            EntradaCrearEventoAutorizacion inCrearAutorizacion = new EntradaCrearEventoAutorizacion();
                            List<EventoAutorizacion> tablaEventos = new ArrayList<>();
                            EventoAutorizacion evento = new EventoAutorizacion();
                            evento.IdTipoEvento = "INGTRA";
                            evento.IdModuloSolicita = "APV";
                            evento.IdUsuarioSolicita = in.idUsuario();
                            evento.Fecha = fechaOptimus;
                            evento.IdTipoOperacion = null;
                            evento.IdConcepto = "INGTRA";
                            evento.NumeroOperacionConcepto = idTraspaso.toString();
                            evento.FechaOperacionConcepto = fechaOptimus;
                            evento.IdCliente = fila.idCliente;
                            evento.IdFuncionario = 0L;
                            evento.IdProveedor = 0L;
                            evento.IdTipoProducto = null;
                            evento.IdClasificacionTipoInstrumento = null;
                            evento.IdTipoInstrumento = null;
                            evento.IdInstrumento = null;
                            evento.Nemo = null;
                            evento.Cantidad = null;
                            evento.Monto = null;
                            evento.Precio = null;
                            evento.IdMoneda = "CLP";
                            evento.Glosa = "INGRESO DE TRASPASO APV Folio: " + idTraspaso.toString();
                            tablaEventos.add(evento);

                            if (tablaEventos.size() > 0) {
                                inCrearAutorizacion.P_IDEMPRESA = in.idEmpresa();
                                inCrearAutorizacion.P_IDUSUARIO = in.idUsuario();
                                inCrearAutorizacion.P_IDMODULO = in.idModulo();
                                inCrearAutorizacion.P_SIMULAROPERACION = "N";
                                inCrearAutorizacion.P_USAREGLASNEGOCIO = "N";
                                inCrearAutorizacion.P_TABLAEVENTOS = tablaEventos;
                                llamar("CrearEventoAutorizacionMasivo", inCrearAutorizacion, JsonObject.class);
                            }
                        } else {
                            // Modifico estado de traspaso
                            List<TraspasoEstado> lstTraspasoMod = new ArrayList<>();
                            lstTraspasoMod.add(new TraspasoEstado("ING", idTraspaso));

                            int[] resultadoMod = repositorio.modificarEstadoTraspaso(lstTraspasoMod);
                            if (resultadoMod != null && resultadoMod.length > 0 && resultadoMod[0] == 1) {
                                salida.mensajes.add(new Mensaje(idTraspaso, Mensajes.MENSAJE_OK, "MODIFICA_ESTADO_OK",
                                        "Modifica estado del Traspaso Folio: " + idTraspaso.toString()));
                            } else {
                                salida.mensajes.add(new Mensaje(idTraspaso, Mensajes.MENSAJE_ERROR, "ERROR_MODIFICA_ESTADO",
                                        "Problema al modificar estado del Traspaso."));
                            }
                        }
                    }

                    List<DetalleOrigenNuevo> lstOrigenTraspaso = new ArrayList<>();
                    for (DetalleOrigenTraspaso row : detalleOrigen) {
                        if (p.idInterno() == row.idInterno() && !row.documentoEntidad().equals("")) {
                            List<Long> idsDetalleOrigenTraspaso = siguientes(SEQ_DETALLE_ORIGEN, 1L);
                            lstOrigenTraspaso.add(new DetalleOrigenNuevo(idsDetalleOrigenTraspaso.get(0), idTraspaso, row.codigoTipoAhorro(),
                                    row.idEntidadRecaudadora(), row.nombreEntidad(), row.documentoEntidad(), row.fondo(), row.montoTraspaso(),
                                    row.ufTraspaso(), row.porcentajeTraspaso(), "VIG", row.idInstrumento(), row.codigoRegimenTributario(),
                                    row.idPortafolio()));
                        }
                    }
                    if (lstOrigenTraspaso.size() > 0) {
                        int[] result = repositorio.insertarDetallesOrigen(lstOrigenTraspaso);
                        // LEGACY DEFECT kept (see README APV-A-430): the check reads 'resultado[0]' (the traspaso insert) and not 'result[0]'
                        if (result != null && result.length > 0 && resultado[0] == 1) {
                            salida.mensajes.add(new Mensaje(idTraspaso, Mensajes.MENSAJE_OK, "CREACION_ORIGEN_TRASPASO_OK",
                                    "Creacion del Origen traspaso exitosa."));
                        }
                    }
                }
            }
            // FIN - CREACION DE TRASPASO

            contadorIds++;
        }
        return salida;
    }

    // ------------------------------------------------------------------------------------------------ helpers of the legacy flow

    /** Legacy {@code ContratoSAAPV PC = ...}: the contract row built from the (possibly overwritten) input row. */
    private ContratoNuevo nuevoContrato(Fila f) {
        return new ContratoNuevo(
                f.idContratoSAAPV,
                f.p.idEmpresa(),
                f.p.folioContratoSAAPV(),
                f.p.idPlan(),
                f.p.nombrePlan(),
                f.p.fechaSuscripcion(),
                f.p.fechaCreacion(),
                f.p.fechaAprobacion(),
                f.p.fechaNotificacion(),
                f.p.fechaRecepcion(),
                f.p.folio(),
                f.idPersona,
                f.idEmpleador,
                f.idCliente,
                f.p.nombreCliente(),
                f.p.apellidoPaternoCliente(),
                f.p.apellidoMaternoCliente(),
                f.p.documentoIdentidadCliente(),
                f.p.fechaNacimientoCliente(),
                f.p.estadoCivil(),
                f.p.idTipoGenero(),
                f.p.idPaisResidenciaCliente(),
                f.p.idCiudadCliente(),
                f.p.idComunaCliente(),
                f.p.direccionCliente(),
                f.p.idTipoCargoPersona(),
                f.p.telefonoFijo(),
                f.p.telefonoLaboral(),
                f.p.telefonoMovil(),
                f.p.emailCliente(),
                f.p.razonSocialEmpleador(),
                f.p.idMedioRecaudacionTrabajador(),
                f.p.idEntidadRecaudadora(),
                f.documentoIdentidadEmpleador,
                f.p.direccionComercialEmpleador(),
                f.p.idComunaEmpleador(),
                f.p.telefonoEmpleador(),
                f.p.nombreEncargado(),
                f.p.emailEncargado(),
                f.p.telefonoEncargado(),
                f.p.codigoPrevision(),
                f.p.pensionado(),
                f.p.trabajador(),
                f.p.formaDePago(),
                f.p.idMedioRecaudacionFormaPago(),
                f.p.diaDescuento(),
                f.p.institucion(),
                f.p.comisionTransferencia(),
                f.p.codigoTipoSolicitud(),
                f.p.nombreEntidadOrigen(),
                f.p.rutEntidadOrigen(),
                f.p.tipoDeAhorro(),
                f.p.fondoDeOrigen(),
                f.p.tipoTraspaso(),
                f.p.montoTraspaso(),
                f.p.ufTraspaso(),
                f.p.porcentajeTraspaso(),
                f.p.idEstadoEnvioNotificacion(),
                f.p.idEstadoSolicitud(),
                f.p.idEstado(),
                f.p.idCategoriaInversionista(),
                f.p.idPerfilComercial(),
                f.p.idCargaMasiva(),
                f.p.canal());
    }

    private void errorTelefono(SalidaCrearTelefono objOut, Salida salida, ParametrosContrato p, List<Long> ids, int contadorIds, String descripcion) {
        if (objOut.P_OUT_ERRORESCONTADOR != 0) {
            for (ErrorDetalle lineaErrorFono : objOut.P_OUT_ERRORESDETALLE) {
                if (lineaErrorFono.Codigo.compareTo("UI_ERROR_AL_CREAR_TELEFONO") == 0) {
                    // only logged by the legacy
                } else {
                    salida.mensajes.add(new Mensaje(idFolio(p, ids, contadorIds), Mensajes.MENSAJE_ERROR, "ERROR_CREAR_TELEFONO", descripcion));
                }
            }
        }
    }

    /** {@code fila.FolioContratoSAAPV == null || equals("") ? null : idsContratoSAAPV.get(contadorIds)}. */
    private static Long idFolio(ParametrosContrato p, List<Long> ids, int contadorIds) {
        return p.folioContratoSAAPV() == null || p.folioContratoSAAPV().equals("") ? null : ids.get(contadorIds);
    }

    /** Legacy {@code Utils.getSecuencia(con, name, cantidad)}: {@code cantidad} consecutive values (none when cantidad <= 0). */
    private List<Long> siguientes(String nombre, Long cantidad) {
        List<Long> out = new ArrayList<>();
        if (cantidad == null || cantidad <= 0) {
            return out;
        }
        for (long i = 0; i < cantidad; i++) {
            out.add(secuencias.siguiente(nombre));
        }
        return out;
    }

    /** Legacy {@code Utils.List2Hash(list, "DocumentoIdentidad")}: a repeated key is an {@code Exception}. */
    private static HashMap<String, EstadoCliente> listaAHash(List<EstadoCliente> lista) throws Exception {
        HashMap<String, EstadoCliente> hobj = new HashMap<>();
        for (EstadoCliente obj : lista) {
            String kf = obj.DocumentoIdentidad();
            if (hobj.containsKey(kf)) {
                throw new Exception("key field is not unique");
            }
            hobj.put(kf, obj);
        }
        return hobj;
    }

    /** An output table the procedure did not answer is null (the legacy iterated it and failed with NullPointerException). */
    private static List<JsonObject> listaONull(JsonObject o, String clave) {
        return o.has(clave) ? Json.lista(o, clave) : null;
    }

    private <T> T llamar(String servicio, Object entrada, Class<T> salida) {
        JsonObject respuesta = colaboradores.invocar(servicio, Json.GSON.toJsonTree(entrada).getAsJsonObject());
        return Json.GSON.fromJson(respuesta, salida);
    }

    /** Legacy {@code SQL.call}: the stored procedure runs through the Colaboradores port ({@code StoredProcedure}, {@code {procedimiento, entrada}}). */
    private JsonObject procedimiento(String nombre, Map<String, Object> entrada) {
        return colaboradores.invocar("StoredProcedure", Json.objeto().poner("procedimiento", nombre).poner("entrada", entrada).construir());
    }

    private Boolean debeCerrarContratoUnico(ContratoSAAPVID linea, ParametrosContrato fila, MedioRecaudacion medioRecaudacionPAC) {
        Boolean debeCerrar = true;
        if ("DIRECTA".equalsIgnoreCase(linea.FormaDePago()) && linea.IdMedioRecaudacionFormaPago().equals(fila.idMedioRecaudacionFormaPago())
                && !fila.idMedioRecaudacionFormaPago().equals(medioRecaudacionPAC.IdMedioRecaudacion())) {
            debeCerrar = false;
        }
        return debeCerrar;
    }

    private Boolean debeCerrarContratoIndefinido(ContratoSAAPVID contratoExistente, List<Portafolio> portafolios, Fila fila) throws SQLException {
        Boolean debeCerrar = false;
        List<PortafoliosSAAPV> portafoliosPorContratoExistente = repositorio.portafoliosDeContrato(contratoExistente.IdContratoSAAPV());
        if (contratosTienenMismosPortafolios(portafolios, portafoliosPorContratoExistente) && contratoExistente.FormaDePago().equals(fila.p.formaDePago())
                && contratoExistente.IdEmpleador().equals(fila.idEmpleador)) {
            debeCerrar = true;
        }
        return debeCerrar;
    }

    private Boolean contratosTienenMismosPortafolios(List<Portafolio> portafolios, List<PortafoliosSAAPV> portafoliosPorContratoExistente) {
        Boolean mismosPortafolios = true;
        if (portafoliosPorContratoExistente.size() != portafolios.size()) {
            mismosPortafolios = !mismosPortafolios;
        } else {
            for (Portafolio portafolio : portafolios) {
                if (portafoliosPorContratoExistente.stream()
                        .filter(pExiste -> portafolio.idPortafolio.equals(pExiste.IdPortafolio()) && portafolio.p.regimen().equals(pExiste.Regimen()))
                        .count() == 0L) {
                    mismosPortafolios = !mismosPortafolios;
                    break;
                }
            }
        }
        return mismosPortafolios;
    }

    /** Legacy {@code ValidaCamposPersona}: the missing mandatory data of the person / client / contract / transfer. */
    private List<String> validaCamposPersona(String tipoValidacion, ParametrosContrato contrato, PortafoliosModificados portafolio) {
        List<String> lstDatosFaltantes = new ArrayList<>();
        if (tipoValidacion.equals("CLIENTE") || tipoValidacion.equals("TRASPASO")) {
            if (contrato.documentoIdentidadCliente() == null || contrato.documentoIdentidadCliente().equals("")) {
                lstDatosFaltantes.add("Documento Identidad Cliente.");
            }
            if (contrato.nombreCliente() == null || contrato.nombreCliente().equals("")) {
                lstDatosFaltantes.add("Nombre Cliente.");
            }
            if (contrato.apellidoPaternoCliente() == null || contrato.apellidoPaternoCliente().equals("")) {
                lstDatosFaltantes.add("Apellido Paterno Cliente.");
            }
            if (contrato.idPaisResidenciaCliente() == null || contrato.idPaisResidenciaCliente() == 0) {
                lstDatosFaltantes.add("Pais Residencia Cliente.");
            }
            if (contrato.estadoCivil() == null || contrato.estadoCivil().equals("")) {
                lstDatosFaltantes.add("Estado Civil Cliente.");
            }
            if (contrato.fechaNacimientoCliente() == null || contrato.fechaNacimientoCliente().equals("")) {
                lstDatosFaltantes.add("Fecha Nacimiento Cliente.");
            }
            if (contrato.idTipoGenero() == null || contrato.idTipoGenero().equals("")) {
                lstDatosFaltantes.add("Genero Cliente.");
            }
            if (contrato.direccionCliente() == null || contrato.direccionCliente().equals("")) {
                lstDatosFaltantes.add("Direccion Cliente.");
            }
            if (contrato.numeroDireccionCliente() == null || contrato.numeroDireccionCliente().equals("")) {
                lstDatosFaltantes.add("Numero Direccion Cliente.");
            }
            if ((contrato.telefonoLaboral() == null || contrato.telefonoLaboral().equals(""))
                    && (contrato.telefonoFijo() == null || contrato.telefonoFijo().equals(""))
                    && (contrato.telefonoMovil() == null || contrato.telefonoMovil().equals(""))) {
                lstDatosFaltantes.add("Telefono(s) Cliente.");
            }
            if (contrato.emailCliente() == null || contrato.emailCliente().equals("")) {
                lstDatosFaltantes.add("Email Cliente.");
            }
            if (contrato.idCategoriaInversionista() == null || contrato.idCategoriaInversionista().equals("")) {
                lstDatosFaltantes.add("Categoria Inversionista Cliente.");
            }
            if (contrato.idPerfilComercial() == null || contrato.idPerfilComercial().equals("")) {
                lstDatosFaltantes.add("Perfil Comercial Cliente.");
            }
            if (contrato.idEmpleado() == null || contrato.idEmpleado() == 0) {
                lstDatosFaltantes.add("Ejecutivo Cliente.");
            }
        }

        if (tipoValidacion.equals("DATOS_MINIMOS")) {
            if (contrato.folioContratoSAAPV() == null || contrato.folioContratoSAAPV().equals("")) {
                lstDatosFaltantes.add("Folio Contrato SAAPV.");
            }
            if (contrato.nombrePlan() == null || contrato.nombrePlan().equals("")) {
                lstDatosFaltantes.add("Nombre de Plan SAAPV.");
            }
            if (contrato.fechaSuscripcion() == null || contrato.fechaSuscripcion().equals("")) {
                lstDatosFaltantes.add("Fecha Suscripcion.");
            }
            if (contrato.nombreCliente() == null || contrato.nombreCliente().equals("")) {
                lstDatosFaltantes.add("Nombre Cliente.");
            }
            if (contrato.apellidoPaternoCliente() == null || contrato.apellidoPaternoCliente().equals("")) {
                lstDatosFaltantes.add("Apellido Paterno Cliente.");
            }
            if (contrato.documentoIdentidadCliente() == null || contrato.documentoIdentidadCliente().equals("")) {
                lstDatosFaltantes.add("Documento Identidad Cliente.");
            }
            if (contrato.direccionCliente() == null || contrato.direccionCliente().equals("")) {
                lstDatosFaltantes.add("Direccion Cliente.");
            }
            if (contrato.numeroDireccionCliente() == null || contrato.numeroDireccionCliente().equals("")) {
                lstDatosFaltantes.add("Numero Direccion Cliente.");
            }
            if (contrato.idPaisResidenciaCliente() == null || contrato.idPaisResidenciaCliente() == 0) {
                lstDatosFaltantes.add("Id Pais.");
            }
            if (contrato.idComunaCliente() == null || contrato.idComunaCliente() == 0 && contrato.idPaisResidenciaCliente() == 44) {
                lstDatosFaltantes.add("Id Comuna.");
            }
            if (contrato.idCiudadCliente() == null || contrato.idCiudadCliente() == 0) {
                lstDatosFaltantes.add("Id Ciudad.");
            }
            if (contrato.idPerfilComercial() == null || contrato.idPerfilComercial().equals("")) {
                lstDatosFaltantes.add("Perfil Comercial.");
            }

            if (contrato.codigoTipoSolicitud().equals("") || (contrato.codigoTipoSolicitud().equals("TRA") && contrato.tipoMovimiento().equals("INGRESO"))) {
                if (contrato.trabajador() == null || contrato.trabajador().equals("")) {
                    lstDatosFaltantes.add("Tipo Trabajador.");
                } else if (contrato.trabajador().equals("DEPENDIENTE")) {
                    if (contrato.razonSocialEmpleador() == null || contrato.razonSocialEmpleador().equals("")) {
                        lstDatosFaltantes.add("Razon Social.");
                    }
                    if (contrato.documentoIdentidadEmpleador() == null || contrato.documentoIdentidadEmpleador().equals("")) {
                        lstDatosFaltantes.add("Documento Identidad Empleador.");
                    }
                    if (contrato.direccionComercialEmpleador() == null || contrato.direccionComercialEmpleador().equals("")) {
                        lstDatosFaltantes.add("Direccion Comercial Empleador.");
                    }
                    if (contrato.numeroDireccionEmpleador() == null || contrato.numeroDireccionEmpleador().equals("")) {
                        lstDatosFaltantes.add("Numero Direccion Empleador.");
                    }
                }
                if (contrato.pensionado() == null || contrato.pensionado().equals("")) {
                    lstDatosFaltantes.add("Pensionado.");
                }
            }
            if (!contrato.codigoTipoSolicitud().equals("TRA")) {
                if (portafolio != null) {
                    if (portafolio.origenAhorro() == null || portafolio.origenAhorro().equals("")) {
                        lstDatosFaltantes.add("Origen Ahorro.");
                    }
                    if (portafolio.pagoNombre() == null || portafolio.pagoNombre().equals("")) {
                        lstDatosFaltantes.add("Tipo de Pago.");
                    }
                    if (portafolio.producto() == null || portafolio.producto().equals("")) {
                        lstDatosFaltantes.add("Producto.");
                    }
                    if (portafolio.regimen() == null || portafolio.regimen().equals("")) {
                        lstDatosFaltantes.add("Regimen.");
                    }
                    if (portafolio.montoPesos() == 0 && portafolio.montoUF() == 0) {
                        lstDatosFaltantes.add("Monto Portafolio.");
                    }
                    if (portafolio.mesInicio() == null || portafolio.mesInicio().equals("")) {
                        lstDatosFaltantes.add("Mes Inicio.");
                    }
                } else {
                    lstDatosFaltantes.add("Portafolio.");
                }
            }
        }
        return lstDatosFaltantes;
    }

    /** Legacy {@code AgregarLogSAAPV}: the log of the contract through the AgregarLog collaborator; every failure is swallowed. */
    private void agregarLogSAAPV(GrabarContratosEntrada in, List<Fila> filas, String opcion) {
        try {
            String idTipoLog;
            String nombreProceso;
            String fechaSistema = Parametros.texto(configuracion, in.idEmpresa(), "PLATAFORMA", "PLA.FechaSistema");
            Fila primera = filas.get(0);
            Long idCliente = primera.idCliente;
            String folioContratoSAAPV = primera.p.folioContratoSAAPV();
            String fechaCreacion = primera.fechaCreacion;
            String idEstado = primera.p.idEstado();
            Long idPersona = primera.idPersona;
            Long idCabeceraLog = (idCliente == 0) ? idPersona : idCliente;
            String idEstadoEnvioNotificacion = primera.p.idEstadoEnvioNotificacion();
            String documentoIdentidadEmpleador = primera.documentoIdentidadEmpleador;

            InAgregarLog entradaAgregarLog = new InAgregarLog();
            InAgregarLog.Log log = new InAgregarLog.Log();

            log.IdCabeceraLog = idCabeceraLog;
            log.IdModulo = in.idModulo();
            log.IdUsuario = in.idUsuario();
            log.IdEmpresa = in.idEmpresa();
            log.FechaSistemaLog = fechaSistema;
            log.IdUsuarioAtendido = 0L;
            log.IdCliente = idCliente;
            log.EstadoCliente = "";
            log.NombreCliente = "";
            log.IdTipoDocumentoIdentidad = "";
            log.IdDocumentoIdentidad = "";
            log.IdPortafolio = 0L;
            log.EstadoPortafolio = "";
            log.IdTipoRegistro = "CONTRATO-SAAPV";
            log.EstadoRegistro = "";
            log.IdTipoOperacion = "";
            log.Folio = 0L;
            log.Cantidad = java.math.BigDecimal.ZERO;
            log.PrecioTasa = java.math.BigDecimal.ZERO;
            log.Monto = java.math.BigDecimal.ZERO;
            log.IdMonedaOrigen = "";
            log.IdMonedaLiquidacion = "";
            log.TipoCambio = "";
            log.Nemo = "";
            log.IdProceso = 0L;
            log.EstadoProceso = "ING";

            if (idCliente == 0) {
                log.IdPersona = idPersona;
            }

            if ("ELIMINAR".equals(opcion)) {
                idTipoLog = "ELIMINACION";
                nombreProceso = "Eliminacion Contrato SAAPV";
                entradaAgregarLog.p_LogDetalle.add(detalleLog(idCabeceraLog, "FolioContratoSAAPV", folioContratoSAAPV));
                entradaAgregarLog.p_LogDetalle.add(detalleLog(idCabeceraLog, "Descripcion", "Se elimina contrato anterior"));
            } else if ("CERRAR".equals(opcion)) {
                idTipoLog = "MODIFICACION";
                nombreProceso = "Cambio Estado Contrato SAAPV";
                entradaAgregarLog.p_LogDetalle.add(detalleLog(idCabeceraLog, "FolioContratoSAAPV", folioContratoSAAPV));
                entradaAgregarLog.p_LogDetalle.add(detalleLog(idCabeceraLog, "Descripcion", "Se cierra el contrato anterior"));
            } else {
                idTipoLog = "INGRESO";
                nombreProceso = "Ingresar Contrato SAAPV";
                entradaAgregarLog.p_LogDetalle.add(detalleLog(idCabeceraLog, "FolioContratoSAAPV", folioContratoSAAPV));
                entradaAgregarLog.p_LogDetalle.add(detalleLog(idCabeceraLog, "FechaCreacion", fechaCreacion));
                entradaAgregarLog.p_LogDetalle.add(detalleLog(idCabeceraLog, "IdEstado", idEstado));
                entradaAgregarLog.p_LogDetalle.add(detalleLog(idCabeceraLog, "IdEstadoEnvioNotificacion", idEstadoEnvioNotificacion));
                entradaAgregarLog.p_LogDetalle.add(detalleLog(idCabeceraLog, "DocumentoIdentidadEmpleador", documentoIdentidadEmpleador));
            }

            log.IdTipoLog = idTipoLog;
            log.NombreProceso = nombreProceso;
            entradaAgregarLog.p_Log.add(log);

            llamar("AgregarLog", entradaAgregarLog, JsonObject.class);
        } catch (Exception e) {
            // LEGACY: every failure of the log (including its own NullPointerException) is swallowed; the contract flow goes on
        }
    }

    private static InAgregarLog.DetalleLog detalleLog(Long idCabeceraLog, String campoRegistro, String valorNuevo) {
        InAgregarLog.DetalleLog d = new InAgregarLog.DetalleLog();
        d.IdCabeceraLog = idCabeceraLog;
        d.IdTrabajo = 0L;
        d.SubRegistro = "CONTRATO-SAAPV";
        d.CampoRegistro = campoRegistro;
        d.ValorAntiguo = "";
        d.ValorNuevo = valorNuevo;
        return d;
    }
}
