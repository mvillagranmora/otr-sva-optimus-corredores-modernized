package cl.bcs.apv.application.traspasos;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.apv.api.casouso.CambiarEstadoContratos;
import cl.bcs.apv.api.casouso.GrabarContratosTraspaso;
import cl.bcs.apv.api.casouso.ValidarContrato;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.cambiarestadocontratos.CambiarEstadoContratosEntrada;
import cl.bcs.apv.api.dto.grabarcontratostraspaso.DetalleOrigenTraspaso;
import cl.bcs.apv.api.dto.grabarcontratostraspaso.DistribucionInversion;
import cl.bcs.apv.api.dto.grabarcontratostraspaso.GrabarContratosTraspasoEntrada;
import cl.bcs.apv.api.dto.grabarcontratostraspaso.GrabarContratosTraspasoResultado;
import cl.bcs.apv.api.dto.grabarcontratostraspaso.ParametrosContrato;
import cl.bcs.apv.api.dto.validarcontrato.ModificaContratoSAAPV;
import cl.bcs.apv.api.dto.validarcontrato.ValidarContratoEntrada;
import cl.bcs.apv.api.dto.validarcontrato.ValidarContratoResultado;
import cl.bcs.apv.application.comun.Json;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.application.comun.Parametros;
import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.EntradaCrearCliente;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.EntradaCrearDireccionElectronica;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.EntradaCrearPersona;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.EntradaCrearTelefono;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.Enrolamiento;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.ErrorDetalle;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.SalidaCrearCliente;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.SalidaCrearDireccionElectronica;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.SalidaCrearPersona;
import cl.bcs.apv.application.contratos.PayloadsSocioNegocio.SalidaCrearTelefono;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;
import cl.bcs.apv.domain.port.out.traspasos.GrabarContratosTraspasoRepositorio;
import cl.bcs.apv.domain.port.out.traspasos.GrabarContratosTraspasoRepositorio.IndicadorEncargadoRRHH;
import cl.bcs.apv.domain.port.out.traspasos.GrabarContratosTraspasoRepositorio.TipoIndicador;
import cl.bcs.utils.Fecha;

/**
 * Use case {@code GrabarContratosTraspaso}: legacy {@code cl.bcs.APV.GrabarContratoTraspasoSAAPV}. For every row it validates the person/client/employer
 * through {@link ValidarContrato}, creates the missing person, employer, client and their relation through the SocioNegocio collaborators and stored
 * procedures, and inserts the contract, the traspaso and its origin detail.
 *
 * <p>LEGACY DEFECT kept (see README APV-B-216): the contract INSERT binds {@code :Canal} that the {@code ContratoSAAPV} row class does not have, so every row
 * that reaches it ends in {@code SQLException}; what follows the insert (traspaso, origin detail, state change, distribution) is therefore unreachable in
 * practice and is ported as the legacy wrote it.
 */
@Transactional(rollbackFor = Exception.class)
public class GrabarContratosTraspasoServicio implements GrabarContratosTraspaso {

    private static final String SP_CREAR_RELACION = "BSCL.SocioNegocio.BD.Servicios::REL.CrearRelacionPersona";
    private static final String SP_MODIFICAR_INDICADOR = "BSCL.Plataforma.BD.Servicios::IND.ModificarIndicador";
    private static final String SEQ_CONTRATO = "BSCL.APV.BD.Secuencias::SCS.IdContratoSAAPV";
    private static final String SEQ_TRASPASO = "BSCL.APV.BD.Secuencias::TRA.IdTraspaso";
    private static final String SEQ_DETALLE_ORIGEN = "BSCL.APV.BD.Secuencias::TRA.IdDetalleOrigenTraspaso";

    private final GrabarContratosTraspasoRepositorio repositorio;
    private final ConfiguracionSistema configuracion;
    private final Secuencias secuencias;
    private final Reloj reloj;
    private final Colaboradores colaboradores;
    private final ValidarContrato validarContrato;
    private final CambiarEstadoContratos cambiarEstadoContratos;

    public GrabarContratosTraspasoServicio(GrabarContratosTraspasoRepositorio repositorio, ConfiguracionSistema configuracion, Secuencias secuencias,
            Reloj reloj, Colaboradores colaboradores, ValidarContrato validarContrato, CambiarEstadoContratos cambiarEstadoContratos) {
        this.repositorio = repositorio;
        this.configuracion = configuracion;
        this.secuencias = secuencias;
        this.reloj = reloj;
        this.colaboradores = colaboradores;
        this.validarContrato = validarContrato;
        this.cambiarEstadoContratos = cambiarEstadoContratos;
    }

    /** Legacy {@code call()}: the logic, then {@code P_OUT_ERRORESCONTADOR = getNumeroErrores(P_OUT_MENSAJES)}. */
    @Override
    public GrabarContratosTraspasoResultado ejecutar(GrabarContratosTraspasoEntrada entrada) {
        return Sneaky.sinChequear(() -> {
            List<Mensaje> mensajes = logica(entrada);
            return new GrabarContratosTraspasoResultado(mensajes, Mensajes.numeroErrores(mensajes));
        });
    }

    /** The input row with the fields the legacy flow overwrites ({@code fila.IdContratoSAAPV = ...}). */
    private static final class Fila {
        Long idContratoSAAPV;
        Long idPersona;
        Long idCliente;
        Long idEmpleador;
        String documentoIdentidadEmpleador;

        Fila(ParametrosContrato p) {
            this.idPersona = p.idPersona();
            this.idCliente = p.idCliente();
            this.idEmpleador = p.idEmpleador();
            this.documentoIdentidadEmpleador = p.documentoIdentidadEmpleador();
        }
    }

    private List<Mensaje> logica(GrabarContratosTraspasoEntrada in) throws Exception {
        Enrolamiento enr = new Enrolamiento();
        EntradaCrearPersona entradaPersona = new EntradaCrearPersona();
        SalidaCrearPersona objOut;
        EntradaCrearTelefono entradaTelefonoCom = new EntradaCrearTelefono();
        EntradaCrearTelefono entradaTelefonoPar = new EntradaCrearTelefono();
        SalidaCrearTelefono objOutFonoPar;
        SalidaCrearTelefono objOutFonoCom;
        EntradaCrearDireccionElectronica entradaMail = new EntradaCrearDireccionElectronica();
        SalidaCrearDireccionElectronica objOutMail;
        List<Mensaje> salida = new ArrayList<>();
        EntradaCrearCliente entradaCliente = new EntradaCrearCliente();
        SalidaCrearCliente salidaCliente = new SalidaCrearCliente();

        List<Long> idsContratoSAAPV = siguientes(SEQ_CONTRATO, (long) in.parametros().size());
        int contadorIds = 0;
        boolean existeFolioContrato;

        for (ParametrosContrato p : in.parametros()) {
            Fila fila = new Fila(p);
            List<String> resultadoValidacion = null;
            fila.idContratoSAAPV = idsContratoSAAPV.get(contadorIds);
            existeFolioContrato = false;

            String cliIdTipoClienteDefecto = Parametros.texto(configuracion, in.idEmpresa(), "PLATAFORMA", "APV.ContratoCLI_IdTipoClienteDefecto");
            String fechaFinRelacionDefecto = Parametros.texto(configuracion, in.idEmpresa(), "PLATAFORMA", "APV.ContratoFechaFinRelacionDefecto");

            // the legacy reads the wall clock here (System.currentTimeMillis); the Reloj port gives it
            long time = reloj.ahora().toEpochMilli();
            java.sql.Date fechaSistema = new java.sql.Date(time);
            Timestamp fechatime = new Timestamp(fechaSistema.getTime());

            // *** VALIDACION CONTRATOS SAAPV *** (person, client, employer and folio)
            List<cl.bcs.apv.api.dto.validarcontrato.ContratosSAAPV> tablaValidaContrato = new ArrayList<>();
            tablaValidaContrato.add(new cl.bcs.apv.api.dto.validarcontrato.ContratosSAAPV(null, null, null, null, p.documentoIdentidadCliente(),
                    p.documentoIdentidadEmpleador(), p.folioContratoSAAPV(), "", p.nombrePlan(), ""));
            ValidarContratoResultado outValidaContrato = validarContrato.ejecutar(new ValidarContratoEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(),
                    "I", tablaValidaContrato, new ArrayList<>(), new ArrayList<>()));

            // LEGACY DEFECT kept (see README APV-B-222): the legacy asked 'P_OUT_ERRORESCONTADOR == null' of ValidaContratoSAAPV.logica, which never sets it (only call()
            // does), so the branch is always taken and the ERROR_VALIDAR_PERSONA message is unreachable: the counter of the typed result is not read.
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
                salida.add(new Mensaje(Long.parseLong(p.folioContratoSAAPV()), Mensajes.MENSAJE_ADVERTENCIA, "FOLIO_CONTRATO_EXISTE",
                        "El Folio Contrato SAAPV ya existe."));
                // LEGACY DEFECT kept (see README APV-B-233): the skipped row does not advance contadorIds, so the next row reuses the same contract id
                continue;
            }
            if (p.tipoMovimiento().equals("INGRESO")) {
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
                    if (!p.telefonoLaboral().equals("")) {
                        enr.IdTipoTelefono = "OTRO";
                        enr.Telefono = p.telefonoLaboral();
                    } else if (!p.telefonoFijo().equals("") && p.telefonoLaboral().equals("")) {
                        enr.IdTipoTelefono = "FIJO";
                        enr.Telefono = p.telefonoFijo();
                    } else if (p.telefonoLaboral().equals("") && p.telefonoFijo().equals("")) {
                        enr.IdTipoTelefono = "MOVIL";
                        enr.Telefono = p.telefonoMovil();
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
                    enr.EMP_FechaInicioCargo = fechaSistema;
                    enr.EMP_FechaTerminoCargo = fechaSistema;
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

                        List<Long> consultaDireccion = repositorio.direccionesFisicas(fila.idPersona);
                        repositorio.copiarDireccionesConPropositoApv(consultaDireccion);

                        // a null IdTipoCargoPersona is unboxed in this comparison (NullPointerException), as the legacy
                        if (p.idTipoCargoPersona() != 0) {
                            int[] res = repositorio.cambiarCargoPersona(fila.idPersona, p.idTipoCargoPersona());
                            if (res != null && res.length > 0 && res[0] == 1) {
                                // the OK message is only logged by the legacy (never added to the output)
                            } else {
                                salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ERROR, "CAMBIAR_CARGO_PERSONA_ERROR",
                                        "Modificacion de cargo persona con problemas."));
                            }
                        }

                        // MOVIL (the legacy tests TelefonoLaboral and sends it as OTRO)
                        if (p.telefonoLaboral().equals("") == false) {
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
                            errorTelefono(objOutFonoPar, salida, idsContratoSAAPV, contadorIds, "Error al crear Telefono Particular");
                        }

                        // PARTICULAR
                        if (p.telefonoFijo().equals("") == false) {
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
                            errorTelefono(objOutFonoPar, salida, idsContratoSAAPV, contadorIds, "Error al crear Telefono Particular");
                        }

                        if (p.telefonoMovil().equals("") == false) {
                            entradaTelefonoCom.p_IdEmpresa = p.idEmpresa();
                            entradaTelefonoCom.p_IdUsuario = in.idUsuario();
                            entradaTelefonoCom.p_IdModulo = in.idModulo();
                            entradaTelefonoCom.p_UsaReglasNegocio = "N";
                            entradaTelefonoCom.p_IdPais = p.idPaisResidenciaCliente();
                            // COMERCIAL
                            entradaTelefonoCom.p_IdTipoTelefono = "MOVIL";
                            entradaTelefonoCom.p_Numero = p.telefonoMovil();
                            entradaTelefonoCom.p_Anexo = "";
                            entradaTelefonoCom.p_CodigoProposito = "PREV";
                            entradaTelefonoCom.p_Comentario = "";
                            entradaTelefonoCom.p_IdEstado = "ACT";
                            entradaTelefonoCom.p_Tipo = "";
                            entradaTelefonoCom.p_IdPersona = fila.idPersona;

                            objOutFonoCom = llamar("CrearTelefono", entradaTelefonoCom, SalidaCrearTelefono.class);
                            errorTelefono(objOutFonoCom, salida, idsContratoSAAPV, contadorIds, "Error al crear Telefono Comercial");
                        }

                        if (p.emailCliente().equals("") == false) {
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
                                        salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ERROR, "ERROR_CREAR_EMAIL",
                                                "Error al crear email Cliente"));
                                    }
                                }
                            }
                        }
                        salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_OK, "CREACION_PERSONA_OK", "Creacion de persona exitosa"));
                    } else {
                        for (ErrorDetalle lineaError : objOut.P_OUT_ERRORESDETALLE) {
                            if (lineaError.Codigo.compareTo("PERSONA_EXISTE") == 0) {
                                salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ADVERTENCIA, "ERROR_CREAR_PERSONA",
                                        "La Persona ya Existe."));
                            } else {
                                salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ADVERTENCIA, "ERROR_CREAR_PERSONA",
                                        "Error al crear persona"));
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
                    enr.FechaNacimiento = fechaSistema;
                    enr.Sexo = "";
                    enr.EstadoCivil = "";
                    enr.IdCiudad = p.idCiudadEmpleador();
                    enr.IdDivisionPais = p.idComunaEmpleador();
                    enr.DireccionCalle = p.direccionComercialEmpleador();
                    enr.DireccionNumero = String.valueOf(p.numeroDireccionEmpleador());
                    enr.DireccionDepartamento = "";
                    if (!p.telefonoLaboral().equals("")) {
                        enr.IdTipoTelefono = "OTRO";
                        enr.Telefono = p.telefonoLaboral();
                    } else if (!p.telefonoFijo().equals("") && p.telefonoLaboral().equals("")) {
                        enr.IdTipoTelefono = "FIJO";
                        enr.Telefono = p.telefonoFijo();
                    } else if (p.telefonoLaboral().equals("") && p.telefonoFijo().equals("")) {
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
                    enr.EMP_FechaInicioCargo = fechaSistema;
                    enr.EMP_FechaTerminoCargo = fechaSistema;
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
                        // the legacy builds the "Creacion de persona exitosa" message of the employer but never adds it to the output
                        fila.idEmpleador = objOut.P_OUT_REFID;

                        List<IndicadorEncargadoRRHH> lstEncargado = new ArrayList<>();
                        List<TipoIndicador> lstTipoIndicador = repositorio.tiposIndicador("PERSONA", "DATOS EMPLEADOR");
                        boolean existeTipoIndicador = (lstTipoIndicador.isEmpty()) ? false : true;
                        if (existeTipoIndicador) {
                            for (TipoIndicador tp : lstTipoIndicador) {
                                IndicadorEncargadoRRHH encargadoRRHH = new IndicadorEncargadoRRHH();
                                encargadoRRHH.IdIndicador = 0L;
                                encargadoRRHH.IdTipoIndicador = tp.IdTipoIndicador;
                                if (tp.CodigoTipoIndicador.compareTo("NOMBRE_ENCARGADO_RRHH") == 0) {
                                    encargadoRRHH.ValorIndicador = p.nombreEncargado();
                                } else if (tp.CodigoTipoIndicador.compareTo("MAIL_ENCARGADO_RRHH") == 0) {
                                    encargadoRRHH.ValorIndicador = p.emailEncargado();
                                } else if (tp.CodigoTipoIndicador.compareTo("TELEFONO_ENCARGADO_RRHH") == 0) {
                                    encargadoRRHH.ValorIndicador = p.telefonoEncargado();
                                }
                                encargadoRRHH.FechaVencimiento = Fecha.toDate("0001-01-01 00:00:00");
                                lstEncargado.add(encargadoRRHH);
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
                                    salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ERROR, "ERROR_ENCARGADO_RRHH",
                                            "Error al ingresar encargado de recursos humanos."));
                                }
                                for (JsonObject advertenciaDetalle : listaONull(resultSP, "p_out_AdvertenciasDetalle")) {
                                    salidaCliente.addAdvertencia(Json.texto(advertenciaDetalle, "Codigo"), Json.texto(advertenciaDetalle, "Descripcion"));
                                    salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ADVERTENCIA, "ADVERTENCIA_ENCARGADO_RRHH",
                                            "Error al ingresar encargado de recursos humanos."));
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
                                salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ADVERTENCIA, "ERROR_CREAR_PERSONA",
                                        "Persona Empleador ya existe."));
                            } else {
                                salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ERROR, "ERROR_CREAR_PERSONA",
                                        "Error al crear persona Empleador"));
                            }
                        }
                    }
                }
                resultadoValidacion = validaCamposPersona("CLIENTE", p);
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
                        enr.FechaNacimiento = fechaSistema;
                        enr.Sexo = p.idTipoGenero();
                        enr.EstadoCivil = p.estadoCivil();
                        enr.IdCiudad = p.idCiudadCliente();
                        enr.IdDivisionPais = p.idComunaCliente();
                        enr.DireccionCalle = p.direccionCliente();
                        enr.DireccionNumero = "";
                        enr.DireccionDepartamento = "";
                        if (!p.telefonoMovil().equals("")) {
                            enr.IdTipoTelefono = "MOVIL";
                            enr.Telefono = p.telefonoMovil();
                        } else if (p.telefonoMovil().equals("") && !p.telefonoFijo().equals("")) {
                            enr.IdTipoTelefono = "FIJO";
                            enr.Telefono = p.telefonoFijo();
                        } else if (p.telefonoMovil().equals("") && p.telefonoFijo().equals("") && !p.telefonoLaboral().equals("")) {
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
                        enr.EMP_FechaInicioCargo = fechaSistema;
                        enr.EMP_FechaTerminoCargo = fechaSistema;
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
                            salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_OK, "CREACION_CLIENTE_OK", "Creacion de cliente exitosa"));
                        }
                        if (salidaCliente.P_OUT_ERRORESCONTADOR > 0) {
                            for (ErrorDetalle lineaError : salidaCliente.P_OUT_ERRORESDETALLE) {
                                if (lineaError.Codigo.compareTo("CLIENTE_EXISTE") == 0) {
                                    salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ADVERTENCIA, "ERROR_CREAR_CLIENTE",
                                            "CLIENTE_EXISTE"));
                                } else if (lineaError.Codigo.compareTo("PERFIL_COMERCIAL_NO_EXISTE") == 0) {
                                    salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ADVERTENCIA, "ERROR_CREAR_PERFIL_CLIENTE",
                                            "PERFIL_COMERCIAL_NO_EXISTE"));
                                } else {
                                    salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ERROR, "ERROR_CREAR_CLIENTE",
                                            "Error al crear cliente"));
                                }
                            }
                        }
                    }
                } else {
                    salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ADVERTENCIA, "VALIDA_DATOS_MINIMOS_CLIENTE",
                            "Error al crear Cliente por que faltan datos."));
                    for (int i = 0; i < resultadoValidacion.size(); i++) {
                        salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ADVERTENCIA, "VALIDA_DATOS_MINIMOS_CLIENTE",
                                " - " + resultadoValidacion.get(i)));
                    }
                }

                // Se ingresa Relacion Persona Cliente Empleador
                if ((fila.idPersona != null || fila.idPersona != 0) && ((fila.idEmpleador != null && fila.idEmpleador != 0) || fila.idEmpleador > 0)) {
                    // Valida la existencia de la relacion de la persona con el empleador
                    List<Long> consultaRelacion = repositorio.relacionesPersona(fila.idPersona, fila.idEmpleador);

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
                        paramsIn.put("FechaInicioRelacion", fechaSistema);
                        paramsIn.put("FechaFinRelacion", Fecha.toDate(fechaFinRelacionDefecto + " 00:00:00"));
                        paramsIn.put("PorcentajeParticipacion", 0);

                        JsonObject resultSP = procedimiento(SP_CREAR_RELACION, paramsIn);
                        if (Json.largo(resultSP, "p_out_ErroresContador").equals(0L)) {
                            // the relation id is read and only logged by the legacy
                        } else {
                            for (JsonObject errorDetalle : listaONull(resultSP, "p_out_ErroresDetalle")) {
                                salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ERROR, Json.texto(errorDetalle, "Codigo"),
                                        Json.texto(errorDetalle, "Descripcion")));
                                salidaCliente.addError(Json.texto(errorDetalle, "Codigo"), Json.texto(errorDetalle, "Descripcion"));
                            }
                            for (JsonObject advertenciaDetalle : listaONull(resultSP, "p_out_AdvertenciasDetalle")) {
                                salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ADVERTENCIA, Json.texto(advertenciaDetalle, "Codigo"),
                                        Json.texto(advertenciaDetalle, "Descripcion")));
                                salidaCliente.addAdvertencia(Json.texto(advertenciaDetalle, "Codigo"), Json.texto(advertenciaDetalle, "Descripcion"));
                            }
                        }
                    }
                }
            }

            GrabarContratosTraspasoRepositorio.ContratoSAAPV pc = nuevoContrato(p, fila);
            // LEGACY DEFECT kept (see README APV-B-216): the INSERT binds :Canal, which the row class does not have: SQLException for every row that reaches it
            int[] res = repositorio.insertarContrato(pc);

            if (res != null && res.length > 0 && res[0] == 1) {
                salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_OK, "CREACION_CONTRATO_OK", "Creacion de contrato exitosa"));

                List<Long> idsContratoTraspasoSAAPV = siguientes(SEQ_TRASPASO, 1L);

                GrabarContratosTraspasoRepositorio.Traspaso traspasoContrato = new GrabarContratosTraspasoRepositorio.Traspaso();
                traspasoContrato.IdTraspaso = idsContratoTraspasoSAAPV.get(0);
                traspasoContrato.IdContratoSAAPV = fila.idContratoSAAPV;
                traspasoContrato.IdEmpresa = p.idEmpresa();
                traspasoContrato.TipoMovimiento = p.tipoMovimiento();
                traspasoContrato.RazonRetiro = p.razonRetiro();
                traspasoContrato.FechaIngreso = p.fechaCreacion();
                traspasoContrato.FechaEgreso = p.tipoMovimiento().equals("INGRESO") ? "0001-01-01" : p.fechaCreacion();
                traspasoContrato.FechaModificacion = "0001-01-01";
                traspasoContrato.CodigoTipoTraspaso = p.tipoTraspaso();
                traspasoContrato.Descripcion = p.descripcion();
                traspasoContrato.IdCliente = fila.idCliente;
                traspasoContrato.IdPersona = p.idPersonaOrigen();
                traspasoContrato.CodigoTipoAhorro = p.tipoDeAhorro();
                traspasoContrato.NombreEntidad = p.nombreEntidad();
                traspasoContrato.DocumentoEntidad = p.documentoEntidad();
                traspasoContrato.IdEstado = p.tipoMovimiento().equals("INGRESO") ? resultadoValidacion.size() == 0 ? "VIG" : "PDI" : "VIG";
                traspasoContrato.Canal = p.canal();
                int[] resultado = repositorio.insertarTraspaso(traspasoContrato);
                if (resultado != null && resultado.length > 0 && resultado[0] == 1) {
                    salida.add(new Mensaje(idsContratoTraspasoSAAPV.get(0), Mensajes.MENSAJE_OK, "CREACION_TRASPASO_OK", "Creacion del traspaso exitosa"));

                    List<GrabarContratosTraspasoRepositorio.DetalleOrigenTraspaso> lstOrigenTraspaso = new ArrayList<>();
                    for (DetalleOrigenTraspaso row : in.detalleOrigenTraspaso()) {
                        List<Long> idsDetalleOrigenTraspaso = siguientes(SEQ_DETALLE_ORIGEN, 1L);
                        GrabarContratosTraspasoRepositorio.DetalleOrigenTraspaso dt = new GrabarContratosTraspasoRepositorio.DetalleOrigenTraspaso();
                        dt.IdDetalleOrigenTraspaso = idsDetalleOrigenTraspaso.get(0);
                        dt.IdTraspaso = idsContratoTraspasoSAAPV.get(0);
                        dt.CodigoTipoAhorro = row.codigoTipoAhorro();
                        dt.NombreEntidadOrigen = row.nombreEntidadOrigen();
                        dt.DocumentoEntidadOrigen = row.documentoEntidadOrigen();
                        dt.FondoOrigen = row.fondoOrigen();
                        dt.MontoTotalTraspaso = row.montoTotalTraspaso();
                        dt.UFTotalTraspaso = row.ufTotalTraspaso();
                        dt.PorcentajeTotalTraspaso = row.porcentajeTotalTraspaso();
                        dt.IdEstado = "VIG";
                        lstOrigenTraspaso.add(dt);
                    }
                    int[] result = repositorio.insertarDetalleOrigenTraspaso(lstOrigenTraspaso);
                    if (result != null && result.length > 0 && resultado[0] == 1) {
                        salida.add(new Mensaje(idsContratoTraspasoSAAPV.get(0), Mensajes.MENSAJE_OK, "CREACION_ORIGEN_TRASPASO_OK",
                                "Creacion del Origen traspaso exitosa"));
                    }
                }

                // Valido datos minimos del contrato para ingresarlo vig o pdi
                List<String> resultadoValidacionPersona = validaCamposPersona("DATOS_MINIMOS", p);

                if (resultadoValidacionPersona.size() == 0) {
                    // Se cambia estado a VIG
                    List<cl.bcs.apv.api.dto.cambiarestadocontratos.ContratosSAAPV> tablaContratos = new ArrayList<>();
                    tablaContratos.add(new cl.bcs.apv.api.dto.cambiarestadocontratos.ContratosSAAPV(fila.idPersona, fila.idCliente, fila.idEmpleador,
                            p.folioContratoSAAPV(), "1900-01-01", p.idEstadoEnvioNotificacion(), "PDA", p.idEstado(), 0L, ""));
                    // the legacy keeps no answer of the state change
                    cambiarEstadoContratos.ejecutar(new CambiarEstadoContratosEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), "VIG", tablaContratos, "N"));
                } else {
                    salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ADVERTENCIA, "VALIDA_DATOS_MINIMOS_CONTRATO",
                            "Contrato queda Pendiente de Ingreso por que faltan datos Minimos."));
                    for (int i = 0; i < resultadoValidacionPersona.size(); i++) {
                        salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ADVERTENCIA, "VALIDA_DATOS_MINIMOS_CONTRATO",
                                " - " + resultadoValidacionPersona.get(i)));
                    }
                }

            } else {
                salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ERROR, "ERROR_CREAR_CONTRATO", "Problemas al crear contrato"));
            }

            if (in.distribucionInversion().size() > 0) {
                List<GrabarContratosTraspasoRepositorio.DistribucionInversion> distribucion = new ArrayList<>();
                for (DistribucionInversion row : in.distribucionInversion()) {
                    GrabarContratosTraspasoRepositorio.DistribucionInversion d = new GrabarContratosTraspasoRepositorio.DistribucionInversion();
                    d.IdContratoSAAPV = fila.idContratoSAAPV;
                    d.FolioContratoSAAPV = row.folioContratoSAAPV();
                    d.IdInstrumento = row.idInstrumento();
                    d.Nemotecnico = row.nemotecnico();
                    d.MontoPesos = row.montoPesos();
                    d.Porcentaje = row.porcentaje();
                    d.IdEstado = row.idEstado();
                    distribucion.add(d);
                }
                int[] result = repositorio.insertarDistribucionInversion(distribucion);
                if (result != null && result.length > 0 && result[0] == 1) {
                    salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_OK, "CREAR_DISTRIBUCION_OK", "Creacion Distribucion Inversion exitosa"));
                } else {
                    salida.add(new Mensaje(idsContratoSAAPV.get(contadorIds), Mensajes.MENSAJE_ERROR, "ERROR_CREAR_DISTRIBUCION", "ERROR al crear Distribucion"));
                }
            }
            contadorIds++;
        }
        return salida;
    }

    /** Legacy {@code ContratoSAAPV PC = ...}: the contract row built from the (possibly overwritten) input row. */
    private static GrabarContratosTraspasoRepositorio.ContratoSAAPV nuevoContrato(ParametrosContrato p, Fila fila) {
        GrabarContratosTraspasoRepositorio.ContratoSAAPV pc = new GrabarContratosTraspasoRepositorio.ContratoSAAPV();
        pc.IdContratoSAAPV = fila.idContratoSAAPV;
        pc.IdEmpresa = p.idEmpresa();
        pc.FolioContratoSAAPV = p.folioContratoSAAPV();
        pc.IdPlan = p.idPlan();
        pc.NombrePlan = p.nombrePlan();
        pc.FechaSuscripcion = p.fechaSuscripcion();
        pc.FechaCreacion = p.fechaCreacion();
        pc.FechaAprobacion = p.fechaAprobacion();
        pc.FechaNotificacion = p.fechaNotificacion();
        pc.FechaRecepcion = p.fechaRecepcion();
        pc.CodigoTipoSolicitud = p.codigoTipoSolicitud();
        pc.Folio = p.folio();
        pc.IdPersona = fila.idPersona;
        pc.IdEmpleador = fila.idEmpleador;
        pc.IdCliente = fila.idCliente;
        pc.NombreCliente = p.nombreCliente();
        pc.ApellidoPaternoCliente = p.apellidoPaternoCliente();
        pc.ApellidoMaternoCliente = p.apellidoMaternoCliente();
        pc.DocumentoIdentidadCliente = p.documentoIdentidadCliente();
        pc.FechaNacimientoCliente = p.fechaNacimientoCliente();
        pc.EstadoCivil = p.estadoCivil();
        pc.IdTipoGenero = p.idTipoGenero();
        pc.IdPaisResidenciaCliente = p.idPaisResidenciaCliente();
        pc.IdCiudadCliente = p.idCiudadCliente();
        pc.IdComunaCliente = p.idComunaCliente();
        pc.DireccionCliente = p.direccionCliente();
        pc.IdTipoCargoPersona = p.idTipoCargoPersona();
        pc.TelefonoCliente = p.telefonoFijo();
        pc.TelefonoComercialCliente = p.telefonoMovil();
        pc.TelefonoCelularCliente = p.telefonoLaboral();
        pc.EmailCliente = p.emailCliente();
        pc.RazonSocialEmpleador = p.razonSocialEmpleador();
        pc.IdMedioRecaudacionTrabajador = p.idMedioRecaudacionTrabajador();
        pc.IdEntidadRecaudadora = p.idEntidadRecaudadora();
        pc.DocumentoIdentidadEmpleador = fila.documentoIdentidadEmpleador;
        pc.DireccionComercialEmpleador = p.direccionComercialEmpleador();
        pc.IdComunaEmpleador = p.idComunaEmpleador();
        pc.TelefonoEmpleador = p.telefonoEmpleador();
        pc.NombreEncargado = p.nombreEncargado();
        pc.EmailEncargado = p.emailEncargado();
        pc.TelefonoEncargado = p.telefonoEncargado();
        pc.CodigoPrevision = p.codigoPrevision();
        pc.Pensionado = p.pensionado();
        pc.Trabajador = p.trabajador();
        pc.FormaDePago = p.formaDePago();
        pc.IdMedioRecaudacionFormaPago = p.idMedioRecaudacionFormaPago();
        pc.DiaDescuento = p.diaDescuento();
        pc.Institucion = p.institucion();
        pc.ComisionTransferencia = p.comisionTransferencia();
        pc.NombreEntidadOrigen = p.nombreEntidad();
        pc.RutEntidadOrigen = p.documentoEntidad();
        pc.TipoDeAhorro = p.tipoDeAhorro();
        pc.FondoDeOrigen = p.fondoDeOrigen();
        pc.TipoTraspaso = p.tipoTraspaso();
        pc.MontoTraspaso = p.montoTraspaso();
        pc.UFTraspaso = p.ufTraspaso();
        pc.PorcentajeTraspaso = p.porcentajeTraspaso();
        pc.IdEstadoEnvioNotificacion = p.idEstadoEnvioNotificacion();
        pc.IdEstadoSolicitud = p.idEstadoSolicitud();
        pc.IdEstado = p.idEstado();
        pc.IdCategoriaInversionista = p.idCategoriaInversionista();
        pc.IdPerfilComercial = p.idPerfilComercial();
        pc.IdCargaMasiva = p.idCargaMasiva();
        return pc;
    }

    private void errorTelefono(SalidaCrearTelefono objOut, List<Mensaje> salida, List<Long> ids, int contadorIds, String descripcion) {
        if (objOut.P_OUT_ERRORESCONTADOR != 0) {
            for (ErrorDetalle lineaErrorFono : objOut.P_OUT_ERRORESDETALLE) {
                if (lineaErrorFono.Codigo.compareTo("UI_ERROR_AL_CREAR_TELEFONO") == 0) {
                    // only logged by the legacy
                } else {
                    salida.add(new Mensaje(ids.get(contadorIds), Mensajes.MENSAJE_ERROR, "ERROR_CREAR_TELEFONO", descripcion));
                }
            }
        }
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

    /** Legacy {@code ValidaCamposPersona}: the missing mandatory data of the client / contract. */
    private List<String> validaCamposPersona(String tipoValidacion, ParametrosContrato contrato) {
        List<String> lstDatosFaltantes = new ArrayList<>();
        if (tipoValidacion.equals("CLIENTE")) {
            // Datos minimos para crear cliente
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
            if (contrato.idPaisResidenciaCliente() == null || contrato.idPaisResidenciaCliente() == 0) {
                lstDatosFaltantes.add("Id Pais.");
            }
            if (contrato.idComunaCliente() == null || contrato.idComunaCliente() == 0) {
                lstDatosFaltantes.add("Id Comuna.");
            }
            if (contrato.idCiudadCliente() == null || contrato.idCiudadCliente() == 0) {
                lstDatosFaltantes.add("Id Ciudad.");
            }
            if (contrato.tipoMovimiento().equals("INGRESO")) {
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
        }

        return lstDatosFaltantes;
    }
}
