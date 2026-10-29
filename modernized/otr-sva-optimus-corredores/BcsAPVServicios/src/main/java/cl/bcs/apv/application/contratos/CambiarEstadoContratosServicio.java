package cl.bcs.apv.application.contratos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.apv.api.casouso.CambiarEstadoContratos;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.cambiarestadocontratos.CambiarEstadoContratosEntrada;
import cl.bcs.apv.api.dto.cambiarestadocontratos.CambiarEstadoContratosResultado;
import cl.bcs.apv.api.dto.cambiarestadocontratos.ContratosSAAPV;
import cl.bcs.apv.application.comun.Json;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.application.comun.Parametros;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.contratos.CambiarEstadoContratosRepositorio;
import cl.bcs.apv.domain.port.out.contratos.CambiarEstadoContratosRepositorio.ContratoEstado;
import cl.bcs.apv.domain.port.out.contratos.CambiarEstadoContratosRepositorio.DatosPersona;
import cl.bcs.apv.domain.port.out.contratos.CambiarEstadoContratosRepositorio.EstadoGlosa;
import cl.bcs.apv.domain.port.out.contratos.CambiarEstadoContratosRepositorio.MandatoPACRelacionado;
import cl.bcs.apv.domain.port.out.contratos.CambiarEstadoContratosRepositorio.TraspasoACambiar;

/**
 * Use case {@code CambiarEstadoContratos}: legacy {@code cl.bcs.APV.CambiarEstadoContratosSAAPV} (revoke REV, close CER, make valid VIG, annul ANU or complete
 * any other option the contracts; optional authorization event, traspaso and PAC mandate follow-up, audit log).
 */
@Transactional(rollbackFor = Exception.class)
public class CambiarEstadoContratosServicio implements CambiarEstadoContratos {

    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;
    private final CambiarEstadoContratosRepositorio repositorio;

    public CambiarEstadoContratosServicio(ConfiguracionSistema configuracion, DiccionarioMensajes diccionario, Colaboradores colaboradores,
            CambiarEstadoContratosRepositorio repositorio) {
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
        this.repositorio = repositorio;
    }

    /** The mutable outcome of the legacy {@code logica} (messages and warnings counter). */
    private static final class Salida {
        List<Mensaje> mensajes = new ArrayList<>();
        long advertencias = 0L;
    }

    @Override
    public CambiarEstadoContratosResultado ejecutar(CambiarEstadoContratosEntrada in) {
        Salida out = logica(in);
        // the legacy call() sets P_OUT_ERRORESCONTADOR = getNumeroErrores(P_OUT_MENSAJES) after logica (whatever logica counted)
        return new CambiarEstadoContratosResultado(out.mensajes, Mensajes.numeroErrores(out.mensajes), out.advertencias);
    }

    private Salida logica(CambiarEstadoContratosEntrada in) {
        Salida out = new Salida();

        String envioBlotterAutorizacion = Parametros.texto(configuracion, in.idEmpresa(), Parametros.PLATAFORMA, "APV.AutorizaGestionContrato");

        String modo = "";
        if (in.opcion().compareTo("REV") == 0) {
            modo = "REVOCA";
        } else if (in.opcion().compareTo("CER") == 0) {
            modo = "CERRAR";
        } else if (in.opcion().compareTo("VIG") == 0) {
            modo = "VIGENT";
        } else {
            modo = "COMPLE";
        }

        if (envioBlotterAutorizacion.compareTo("S") == 0 && !modo.isEmpty()) {
            JsonArray tablaEventos = new JsonArray();
            for (ContratosSAAPV row : in.contratosSAAPV()) {
                String glosa;
                List<DatosPersona> datos = repositorio.datosPersona(row.idPersona());
                if (datos.size() > 0) {
                    if (in.opcion().compareTo("REV") == 0 || in.opcion().compareTo("CER") == 0) {
                        glosa = datos.get(0).DocumentoIdentidad + " / " + datos.get(0).Nombres + " / " + row.idFolioContratoSAAPV() + " / "
                                + row.descripcionGlosa();
                    } else {
                        glosa = datos.get(0).DocumentoIdentidad + " / " + datos.get(0).Nombres + " / " + row.idFolioContratoSAAPV();
                    }
                } else {
                    glosa = row.idFolioContratoSAAPV();
                }
                tablaEventos.add(Json.objeto()
                        .poner("IdUnion", null)
                        .poner("IdTipoEvento", modo)
                        .poner("IdModuloSolicita", "APV")
                        .poner("IdUsuarioSolicita", in.idUsuario())
                        .poner("Fecha", row.fechaRevocacion())
                        .poner("IdTipoOperacion", null)
                        .poner("IdConcepto", modo)
                        .poner("NumeroOperacionConcepto", row.idFolioContratoSAAPV())
                        .poner("FechaOperacionConcepto", row.fechaRevocacion())
                        .poner("IdCliente", row.idCliente())
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
                        .poner("Glosa", glosa)
                        .construir());
            }

            if (tablaEventos.size() > 0) {
                colaboradores.invocar("CrearEventoAutorizacionMasivo", Json.objeto()
                        .poner("P_IDEMPRESA", in.idEmpresa())
                        .poner("P_IDUSUARIO", in.idUsuario())
                        .poner("P_IDMODULO", in.idModulo())
                        .poner("P_SIMULAROPERACION", "N")
                        .poner("P_USAREGLASNEGOCIO", "N")
                        .poner("P_TABLAEVENTOS", tablaEventos)
                        .poner("P_DETALLE", new JsonArray())
                        .construir());
            }

            EstadoGlosa valorAntiguoLog = consultarEstadoGlosa(in.contratosSAAPV().get(0).idFolioContratoSAAPV());
            repositorio.actualizarEstados(in.contratosSAAPV());
            repositorio.actualizarGlosas(in.contratosSAAPV());
            agregarLogSAAPV(in, valorAntiguoLog, in.contratosSAAPV().get(0).idEstadoSolicitud(), in.contratosSAAPV().get(0).idEstadoEnvioNotificacion());
            return out;
        }

        List<ContratosSAAPV> contratos = new ArrayList<>();
        List<String> listaIdFolioContratoSAAPV = new ArrayList<>();
        List<Long> listaIdPersona = new ArrayList<>();

        for (ContratosSAAPV row : in.contratosSAAPV()) {
            contratos.add(copia(row));
            if (!listaIdFolioContratoSAAPV.contains(row.idFolioContratoSAAPV())) {
                listaIdFolioContratoSAAPV.add(row.idFolioContratoSAAPV());
            }
            if (!listaIdPersona.contains(row.idPersona())) {
                listaIdPersona.add(row.idPersona());
            }
        }

        if (in.opcion().compareTo("ANU") == 0) {
            List<ContratoEstado> consultaContratoTraspaso = repositorio.contratosConTraspaso(listaIdFolioContratoSAAPV, listaIdPersona);
            List<TraspasoACambiar> lstTraspaso = new ArrayList<>();
            List<ContratosSAAPV> contratosAnular = new ArrayList<>();

            for (ContratoEstado traspaso : consultaContratoTraspaso) {
                if ("PDI".equalsIgnoreCase(traspaso.IdEstado)) {
                    for (ContratosSAAPV row : in.contratosSAAPV()) {
                        if (traspaso.FolioContratoSAAPV.equalsIgnoreCase(row.idFolioContratoSAAPV())) {
                            contratosAnular.add(copia(row));
                        }
                    }

                    if (traspaso.IdTraspaso > 0) {
                        lstTraspaso.add(new TraspasoACambiar(traspaso.IdContratoSAAPV, in.idEmpresa(), traspaso.IdTraspaso, "ANU",
                                in.contratosSAAPV().get(0).fechaRevocacion()));
                    }
                } else {
                    if ("S".equals(in.desdePac())) {
                        String mensaje = "Contrato SAAPV con Folio " + traspaso.FolioContratoSAAPV + " debe estar en estado Pendiente de Ingreso";
                        out.mensajes.add(Mensajes.de(0L, Mensajes.MENSAJE_ERROR, "ERROR_ANULAR_CONTRATO", mensaje));
                    }
                }
            }

            if (lstTraspaso.size() > 0) {
                repositorio.cambiarEstadoTraspasos(lstTraspaso);
            }

            EstadoGlosa valorAntiguoLog = consultarEstadoGlosa(in.contratosSAAPV().get(0).idFolioContratoSAAPV());
            repositorio.actualizarEstados(contratosAnular);
            repositorio.actualizarGlosas(contratosAnular);

            if (!contratosAnular.isEmpty()) {
                agregarLogSAAPV(in, valorAntiguoLog, contratosAnular.get(0).idEstadoSolicitud(), contratosAnular.get(0).idEstadoEnvioNotificacion());
            }

            // LEGACY DEFECT kept (see README APV-A-269): a null p_DesdePac throws NullPointerException here, after the contracts were updated
            if (!in.desdePac().equals("S")) {
                JsonArray contratosMandato = new JsonArray();
                List<String> idContratosSAAPV = in.contratosSAAPV().stream().map(m -> m.idFolioContratoSAAPV()).collect(Collectors.toList());
                List<MandatoPACRelacionado> mandatosRelacionados = repositorio.mandatosRelacionados(idContratosSAAPV);
                for (MandatoPACRelacionado mov : mandatosRelacionados) {
                    contratosMandato.add(Json.objeto().poner("IdContrato", mov.IdMandatoPAC).poner("Estado", mov.Estado).construir());
                }

                if (contratosMandato.size() != 0) {
                    JsonObject anuMandatoOut = colaboradores.invocar("AnulacionMandatoPAC", Json.objeto()
                            .poner("p_IdEmpresa", in.idEmpresa())
                            .poner("p_IdUsuario", in.idUsuario())
                            .poner("p_IdModulo", "TES")
                            .poner("p_Contrato", contratosMandato)
                            .poner("p_DesdeCSC", "N")
                            .poner("p_DesdeSAAPV", "S")
                            .construir());

                    if (contador(anuMandatoOut).longValue() > 0L) {
                        out.mensajes = Json.mensajes(anuMandatoOut, "P_OUT_MENSAJES");
                        return out;
                    }

                    for (Mensaje mensajeMandato : Json.mensajes(anuMandatoOut, "P_OUT_MENSAJES")) {
                        out.mensajes.add(mensajeMandato);
                    }
                }
            }

            for (ContratosSAAPV salida : contratosAnular) {
                Map<String, String> valores = new HashMap<>();
                valores.put("IdContrato", salida.idFolioContratoSAAPV());
                out.mensajes.add(Mensajes.conDiccionario(diccionario, in.idEmpresa(), 0L, Mensajes.MENSAJE_OK, "ANULACION_CONTRATO_SAAPV_EXITOSA", valores));
            }
        } else if ("CER".equals(in.opcion())) {
            Map<String, ContratoEstado> consultaContratoTraspaso = consultarContratoTraspaso(listaIdFolioContratoSAAPV, listaIdPersona);
            List<TraspasoACambiar> lstTraspaso = new ArrayList<>();
            List<ContratosSAAPV> contratosCerrar = new ArrayList<>();
            JsonArray contratosMandato = new JsonArray();

            for (ContratosSAAPV row : in.contratosSAAPV()) {
                ContratoEstado contratosTraspasos = consultaContratoTraspaso.get(row.idFolioContratoSAAPV());

                // LEGACY DEFECT kept (see README APV-A-284): a folio the query does not return throws NullPointerException on contratosTraspasos.IdEstado
                if ("PDI".equalsIgnoreCase(contratosTraspasos.IdEstado) || contratosTraspasos.IdEstado == null
                        || "TPD".equalsIgnoreCase(contratosTraspasos.IdEstado)) {

                    if ("TPD".equalsIgnoreCase(contratosTraspasos.IdEstado)) {
                        String descripcion = diccionario.mensaje(in.idEmpresa(), "ERROR_CERRAR_CONTRATO_SIN_INFO_CLIENTE", new HashMap<>());
                        descripcion = descripcion.replace("::IdContrato::", contratosTraspasos.IdContratoSAAPV.toString()).replace("::IdTraspaso::",
                                contratosTraspasos.IdTraspaso.toString());
                        out.mensajes.add(Mensajes.de(0L, Mensajes.MENSAJE_ADVERTENCIA, "ERROR_CERRAR_CONTRATO_SIN_INFO_CLIENTE", descripcion));
                        out.advertencias += 1;
                    }

                    if (contratosTraspasos.FolioContratoSAAPV.equalsIgnoreCase(row.idFolioContratoSAAPV())) {
                        contratosCerrar.add(copia(row));

                        Long idContrato = contratosTraspasos.IdMandatoPAC;
                        if (contratosTraspasos.IdMandatoPAC != null) {
                            contratosMandato.add(Json.objeto().poner("IdContrato", idContrato).poner("Estado", "ANU").construir());
                        }
                    }

                    if (contratosTraspasos.IdTraspaso > 0) {
                        lstTraspaso.add(new TraspasoACambiar(contratosTraspasos.IdContratoSAAPV, in.idEmpresa(), contratosTraspasos.IdTraspaso, "CER",
                                row.fechaRevocacion()));
                    }
                } else if ("TPD".equalsIgnoreCase(contratosTraspasos.IdEstado)) {
                    // unreachable in the legacy (TPD is taken by the branch above); kept as written
                    String descripcion = diccionario.mensaje(in.idEmpresa(), "ERROR_CERRAR_CONTRATO_SIN_INFO_CLIENTE", new HashMap<>());
                    descripcion = descripcion.replace("::IdContrato::", contratosTraspasos.IdContratoSAAPV.toString()).replace("::IdTraspaso::",
                            contratosTraspasos.IdTraspaso.toString());
                    out.mensajes.add(Mensajes.de(0L, Mensajes.MENSAJE_ERROR, "ERROR_CERRAR_CONTRATO_SIN_INFO_CLIENTE", descripcion));
                }
            }

            if (lstTraspaso.size() > 0) {
                repositorio.cambiarEstadoTraspasos(lstTraspaso);
            }

            // annul the PAC mandate
            if (contratosMandato.size() > 0) {
                JsonObject salidaAnularMandatoPAC = colaboradores.invocar("AnulacionMandatoPAC", Json.objeto()
                        .poner("p_IdEmpresa", in.idEmpresa())
                        .poner("p_IdUsuario", in.idUsuario())
                        .poner("p_IdModulo", in.idModulo())
                        .poner("p_Contrato", contratosMandato)
                        .poner("p_DesdeCSC", "N")
                        .poner("p_DesdeSAAPV", "N")
                        .construir());

                out.mensajes.addAll(Json.mensajes(salidaAnularMandatoPAC, "P_OUT_MENSAJES"));
                // the error counter the legacy adds here is overwritten by getNumeroErrores in call(); only its NullPointerException on a JSON null remains
                contador(salidaAnularMandatoPAC).compareTo(0L);
            }

            EstadoGlosa valorAntiguoLog = consultarEstadoGlosa(in.contratosSAAPV().get(0).idFolioContratoSAAPV());
            repositorio.actualizarEstados(contratosCerrar);
            repositorio.actualizarGlosas(contratosCerrar);

            if (!contratosCerrar.isEmpty()) {
                agregarLogSAAPV(in, valorAntiguoLog, contratosCerrar.get(0).idEstadoSolicitud(), contratosCerrar.get(0).idEstadoEnvioNotificacion());
            }
        } else if ("REV".equals(in.opcion())) {
            Map<String, ContratoEstado> consultaContratoTraspaso = consultarContratoTraspaso(listaIdFolioContratoSAAPV, listaIdPersona);
            List<ContratoEstado> contratosRevocar = new ArrayList<>();

            for (ContratosSAAPV row : in.contratosSAAPV()) {
                ContratoEstado contratosParaRevocar = consultaContratoTraspaso.get(row.idFolioContratoSAAPV());

                // LEGACY DEFECT kept (see README APV-A-305): a folio the query does not return throws NullPointerException here
                if (contratosParaRevocar.FolioContratoSAAPV.equalsIgnoreCase(row.idFolioContratoSAAPV())) {
                    ContratoEstado c = new ContratoEstado();
                    c.IdContratoSAAPV = contratosParaRevocar.IdContratoSAAPV;
                    c.IdEstado = "ANU";
                    c.IdTraspaso = contratosParaRevocar.IdTraspaso;
                    c.FolioContratoSAAPV = row.idFolioContratoSAAPV();
                    c.EstadoPAC = "";
                    c.IdMandatoPAC = 0L;
                    contratosRevocar.add(c);
                }
            }

            EstadoGlosa valorAntiguoLog = consultarEstadoGlosa(in.contratosSAAPV().get(0).idFolioContratoSAAPV());
            repositorio.actualizarEstados(contratos);
            repositorio.actualizarGlosas(contratos);

            if (!contratos.isEmpty()) {
                agregarLogSAAPV(in, valorAntiguoLog, contratos.get(0).idEstadoSolicitud(), contratos.get(0).idEstadoEnvioNotificacion());
            }

            EstadoGlosa valorAntiguoTraspasoLog = consultarEstadoTraspaso(in.contratosSAAPV().get(0).idFolioContratoSAAPV());
            if (valorAntiguoTraspasoLog != null) {
                repositorio.actualizarEstadoTraspaso(contratosRevocar);
                // LEGACY DEFECT kept (see README APV-A-301): the legacy mutates the first contract of its own input before the second log
                CambiarEstadoContratosEntrada inn = conPrimerContratoModificado(in, contratosRevocar.get(0).IdEstado, valorAntiguoTraspasoLog.DescripcionGlosa);
                agregarLogSAAPV(inn, valorAntiguoTraspasoLog, contratosRevocar.get(0).IdEstado, contratos.get(0).idEstadoEnvioNotificacion());
            }

            // RevocacionMandatoPAC
            if (!in.desdePac().equals("S")) {
                JsonArray mandatos = new JsonArray();
                List<String> idContratosSAAPV = in.contratosSAAPV().stream().map(m -> m.idFolioContratoSAAPV()).collect(Collectors.toList());
                List<MandatoPACRelacionado> mandatosRelacionados = repositorio.mandatosRelacionados(idContratosSAAPV);
                for (MandatoPACRelacionado mov : mandatosRelacionados) {
                    mandatos.add(Json.objeto().poner("IdMandato", mov.IdMandatoPAC).poner("Estado", mov.Estado).construir());
                }

                if (mandatos.size() != 0) {
                    JsonObject revMandatoOut = colaboradores.invocar("RevocacionMandatoPAC", Json.objeto()
                            .poner("p_IdEmpresa", in.idEmpresa())
                            .poner("p_IdUsuario", in.idUsuario())
                            .poner("p_IdModulo", "TES")
                            .poner("p_Mandato", mandatos)
                            .poner("p_DesdeCSC", "N")
                            .poner("p_DesdeSAAPV", "S")
                            .construir());

                    if (contador(revMandatoOut).longValue() > 0L) {
                        out.mensajes = Json.mensajes(revMandatoOut, "P_OUT_MENSAJES");
                        return out;
                    }

                    for (Mensaje mensajeMandato : Json.mensajes(revMandatoOut, "P_OUT_MENSAJES")) {
                        out.mensajes.add(mensajeMandato);
                    }
                }
            }
            for (ContratoEstado salida : contratosRevocar) {
                Map<String, String> valores = new HashMap<>();
                valores.put("IdContrato", salida.FolioContratoSAAPV);
                out.mensajes.add(Mensajes.conDiccionario(diccionario, in.idEmpresa(), 0L, Mensajes.MENSAJE_OK, "REVOCACION_CONTRATO_SAAPV_EXITOSA", valores));
            }
        } else if ("VIG".equals(in.opcion())) {
            Map<String, ContratoEstado> consultaContratoTraspaso = consultarContratoTraspaso(listaIdFolioContratoSAAPV, listaIdPersona);
            List<ContratoEstado> contratosCambioEstadoTraspaso = new ArrayList<>();

            for (ContratosSAAPV row : in.contratosSAAPV()) {
                ContratoEstado contratosDejarVigente = consultaContratoTraspaso.get(row.idFolioContratoSAAPV());

                if (contratosDejarVigente.FolioContratoSAAPV.equalsIgnoreCase(row.idFolioContratoSAAPV())) {
                    ContratoEstado c = new ContratoEstado();
                    c.IdContratoSAAPV = contratosDejarVigente.IdContratoSAAPV;
                    c.IdEstado = "PDI";
                    c.IdTraspaso = contratosDejarVigente.IdTraspaso;
                    c.FolioContratoSAAPV = row.idFolioContratoSAAPV();
                    c.EstadoPAC = "";
                    c.IdMandatoPAC = 0L;
                    contratosCambioEstadoTraspaso.add(c);
                }
            }

            EstadoGlosa valorAntiguoLog = consultarEstadoGlosa(in.contratosSAAPV().get(0).idFolioContratoSAAPV());
            repositorio.actualizarEstados(contratos);
            repositorio.actualizarGlosas(contratos);

            if (!contratos.isEmpty()) {
                agregarLogSAAPV(in, valorAntiguoLog, contratos.get(0).idEstadoSolicitud(), contratos.get(0).idEstadoEnvioNotificacion());
            }

            EstadoGlosa valorAntiguoTraspasoLog = consultarEstadoTraspaso(in.contratosSAAPV().get(0).idFolioContratoSAAPV());
            if (valorAntiguoTraspasoLog != null) {
                repositorio.actualizarEstadoTraspaso(contratosCambioEstadoTraspaso);
                // LEGACY DEFECT kept (see README APV-A-301): the legacy mutates the first contract of its own input before the second log
                CambiarEstadoContratosEntrada inn = conPrimerContratoModificado(in, contratosCambioEstadoTraspaso.get(0).IdEstado,
                        valorAntiguoTraspasoLog.DescripcionGlosa);
                agregarLogSAAPV(inn, valorAntiguoTraspasoLog, contratosCambioEstadoTraspaso.get(0).IdEstado, contratos.get(0).idEstadoEnvioNotificacion());
            }
        } else {
            EstadoGlosa valorAntiguoLog = consultarEstadoGlosa(in.contratosSAAPV().get(0).idFolioContratoSAAPV());
            repositorio.actualizarEstados(contratos);
            repositorio.actualizarGlosas(contratos);

            if (!contratos.isEmpty()) {
                agregarLogSAAPV(in, valorAntiguoLog, contratos.get(0).idEstadoSolicitud(), contratos.get(0).idEstadoEnvioNotificacion());
            }
        }
        return out;
    }

    /** Legacy {@code row} copy with the notification and request states cleared (a new object of the legacy class, not the input row). */
    private static ContratosSAAPV copia(ContratosSAAPV row) {
        return new ContratosSAAPV(row.idPersona(), row.idCliente(), row.idEmpleador(), row.idFolioContratoSAAPV(), row.fechaRevocacion(), "", "",
                row.idEstado(), row.idGlosa(), row.descripcionGlosa());
    }

    /**
     * The legacy {@code Utils.copyPojo(in, inn)} shares the contract list and then writes IdEstado and DescripcionGlosa into the FIRST contract: the
     * records are immutable, so the second log gets a copy of the input whose first contract carries those two values.
     */
    private static CambiarEstadoContratosEntrada conPrimerContratoModificado(CambiarEstadoContratosEntrada in, String idEstado, String descripcionGlosa) {
        List<ContratosSAAPV> lista = new ArrayList<>(in.contratosSAAPV());
        ContratosSAAPV c = lista.get(0);
        lista.set(0, new ContratosSAAPV(c.idPersona(), c.idCliente(), c.idEmpleador(), c.idFolioContratoSAAPV(), c.fechaRevocacion(),
                c.idEstadoEnvioNotificacion(), c.idEstadoSolicitud(), idEstado, c.idGlosa(), descripcionGlosa));
        return new CambiarEstadoContratosEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), in.opcion(), lista, in.desdePac());
    }

    /** The error counter of a mandate service answer: absent keeps the legacy default 0, JSON null is a NullPointerException on unboxing. */
    private static Long contador(JsonObject salida) {
        if (!salida.has("P_OUT_ERRORESCONTADOR")) {
            return 0L;
        }
        return Json.largo(salida, "P_OUT_ERRORESCONTADOR");
    }

    private Map<String, ContratoEstado> consultarContratoTraspaso(List<String> folios, List<Long> personas) {
        // LEGACY DEFECT kept (see README APV-A-286): Collectors.toMap throws IllegalStateException when two rows share the folio (two PAC mandates)
        return repositorio.contratosConTraspasoYMandato(folios, personas).stream()
                .collect(Collectors.toMap(traspaso -> traspaso.FolioContratoSAAPV, traspaso -> traspaso));
    }

    /** Legacy {@code ConsultarEstadoGlosa}: any failure is swallowed and answers null. */
    private EstadoGlosa consultarEstadoGlosa(String idFolioContratoSAAPV) {
        EstadoGlosa valoresAntiguos = new EstadoGlosa();
        try {
            List<EstadoGlosa> resultadoConsulta = repositorio.estadoGlosa(idFolioContratoSAAPV);
            if (!resultadoConsulta.isEmpty()) {
                valoresAntiguos.IdEstadoEnvioNotificacion = resultadoConsulta.get(0).IdEstadoEnvioNotificacion;
                valoresAntiguos.IdEstadoSolicitud = resultadoConsulta.get(0).IdEstadoSolicitud;
                valoresAntiguos.IdEstado = resultadoConsulta.get(0).IdEstado;
                valoresAntiguos.IdGlosa = resultadoConsulta.get(0).IdGlosa;
                valoresAntiguos.DescripcionGlosa = resultadoConsulta.get(0).DescripcionGlosa;
                return valoresAntiguos;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Legacy {@code ConsultarEstadoTraspaso}: any failure is swallowed and answers null. */
    private EstadoGlosa consultarEstadoTraspaso(String idFolioContratoSAAPV) {
        EstadoGlosa valoresAntiguos = new EstadoGlosa();
        try {
            List<ContratoEstado> resultadoExisteTraspaso = repositorio.traspasoDelContrato(idFolioContratoSAAPV);
            if (!resultadoExisteTraspaso.isEmpty()) {
                valoresAntiguos.IdEstadoEnvioNotificacion = "";
                valoresAntiguos.IdEstadoSolicitud = resultadoExisteTraspaso.get(0).IdEstado;
                valoresAntiguos.IdEstado = resultadoExisteTraspaso.get(0).IdEstado;
                valoresAntiguos.IdGlosa = resultadoExisteTraspaso.get(0).IdMandatoPAC;
                valoresAntiguos.DescripcionGlosa = "Modificacion traspaso asociado a contrato SAAPV " + idFolioContratoSAAPV;
                return valoresAntiguos;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Legacy {@code AgregarLogSAAPV}: builds the audit log of the first contract and sends it to AgregarLog; ANY failure (including a null) is swallowed. */
    private void agregarLogSAAPV(CambiarEstadoContratosEntrada in, EstadoGlosa valorAntiguo, String estadoSolicitud, String estadoEnvioNotificacion) {
        try {
            String fechaSistema = Parametros.fechaSistema(configuracion, in.idEmpresa());
            String folioContratoSAAPV = in.contratosSAAPV().get(0).idFolioContratoSAAPV();
            String idEstadoEnvioNotificacion = estadoEnvioNotificacion;
            String idEstadoSolicitud = estadoSolicitud;
            String idEstado = in.contratosSAAPV().get(0).idEstado();
            Long idGlosa = in.contratosSAAPV().get(0).idGlosa();
            String descripcionGlosa = in.contratosSAAPV().get(0).descripcionGlosa();
            Long idCliente = in.contratosSAAPV().get(0).idCliente();
            Long idPersona = in.contratosSAAPV().get(0).idPersona();
            Long idCabeceraLog = (idCliente == 0) ? idPersona : idCliente;

            String valorAntiguoIdEstadoEnvioNotificacion = "";
            String valorAntiguoIdEstadoSolicitud = "";
            String valorAntiguoIdEstado = "";
            Long valorAntiguoIdGlosa = 0L;
            String valorAntiguoDescripcionGlosa = "";

            if (valorAntiguo != null) {
                valorAntiguoIdEstadoEnvioNotificacion = valorAntiguo.IdEstadoEnvioNotificacion;
                valorAntiguoIdEstadoSolicitud = valorAntiguo.IdEstadoSolicitud;
                valorAntiguoIdEstado = valorAntiguo.IdEstado;
                valorAntiguoIdGlosa = valorAntiguo.IdGlosa;
                valorAntiguoDescripcionGlosa = valorAntiguo.DescripcionGlosa;
            }

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
                    .poner("NombreProceso", "Cambiar Estado Contrato SAAPV")
                    .poner("IdPersona", idCliente == 0 ? idPersona : (Object) 0L)
                    .construir();

            JsonArray detalle = new JsonArray();
            detalle.add(detalleLog(idCabeceraLog, "FolioContratoSAAPV", "", folioContratoSAAPV));

            if (!valorAntiguoIdEstadoEnvioNotificacion.isEmpty() || !idEstadoEnvioNotificacion.isEmpty()) {
                detalle.add(detalleLog(idCabeceraLog, "IdEstadoEnvioNotificacion", valorAntiguoIdEstadoEnvioNotificacion, idEstadoEnvioNotificacion));
            }

            if (!valorAntiguoIdEstadoSolicitud.isEmpty() || !idEstadoSolicitud.isEmpty()) {
                detalle.add(detalleLog(idCabeceraLog, "IdEstadoSolicitud", valorAntiguoIdEstadoSolicitud, idEstadoSolicitud));
            }

            detalle.add(detalleLog(idCabeceraLog, "IdEstado", valorAntiguoIdEstado, idEstado));
            detalle.add(detalleLog(idCabeceraLog, "IdGlosa", Long.toString(valorAntiguoIdGlosa), Long.toString(idGlosa)));
            detalle.add(detalleLog(idCabeceraLog, "DescripcionGlosa", valorAntiguoDescripcionGlosa, descripcionGlosa));

            JsonArray logs = new JsonArray();
            logs.add(log);
            colaboradores.invocar("AgregarLog", Json.objeto()
                    .poner("p_IdEmpresa", null)
                    .poner("p_IdUsuario", null)
                    .poner("p_IdModulo", null)
                    .poner("p_UsaReglasNegocio", null)
                    .poner("p_IdEstadoAutorizacion", null)
                    .poner("p_IdTrabajo", 0L)
                    .poner("p_Log", logs)
                    .poner("p_LogDetalle", detalle)
                    .construir());
        } catch (Exception e) {
            // LEGACY DEFECT kept (see README APV-A-269): the audit log never fails the operation, a failure is only printed
        }
    }

    private static JsonObject detalleLog(Long idCabeceraLog, String campoRegistro, String valorAntiguo, String valorNuevo) {
        return Json.objeto()
                .poner("IdTrabajo", 0L)
                .poner("IdCabeceraLog", idCabeceraLog)
                .poner("SubRegistro", "CONTRATO-SAAPV")
                .poner("CampoRegistro", campoRegistro)
                .poner("ValorAntiguo", valorAntiguo)
                .poner("ValorNuevo", valorNuevo)
                .construir();
    }
}
