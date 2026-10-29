package cl.bcs.apv.application.traspasos;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.apv.api.casouso.GrabarTraspasos;
import cl.bcs.apv.api.casouso.ValidarContrato;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.grabartraspasos.GrabarTraspasosEntrada;
import cl.bcs.apv.api.dto.grabartraspasos.GrabarTraspasosResultado;
import cl.bcs.apv.api.dto.grabartraspasos.Traspaso;
import cl.bcs.apv.api.dto.validarcontrato.ContratosSAAPV;
import cl.bcs.apv.api.dto.validarcontrato.ModificaContratoSAAPV;
import cl.bcs.apv.api.dto.validarcontrato.ValidarContratoEntrada;
import cl.bcs.apv.api.dto.validarcontrato.ValidarContratoResultado;
import cl.bcs.apv.application.comun.Json;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.application.comun.Parametros;
import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;
import cl.bcs.apv.domain.port.out.traspasos.GrabarTraspasosRepositorio;
import cl.bcs.apv.domain.port.out.traspasos.GrabarTraspasosRepositorio.DetalleTraspaso;
import cl.bcs.apv.domain.port.out.traspasos.GrabarTraspasosRepositorio.EstadoCliente;
import cl.bcs.apv.domain.port.out.traspasos.GrabarTraspasosRepositorio.PortafolioContratoSAAPV;
import cl.bcs.apv.domain.port.out.traspasos.GrabarTraspasosRepositorio.TraspasoModificar;
import cl.bcs.apv.domain.port.out.traspasos.GrabarTraspasosRepositorio.VistaInfoPort;

/**
 * Use case {@code GrabarTraspasos}: legacy {@code cl.bcs.APV.GrabarTraspasos}. For every traspaso row it validates the client and the portafolio (peer
 * {@code ValidarContrato}), creates the portafolio with the stored procedure when it does not exist, and then writes the detalle del traspaso, the
 * relation portafolio-contrato and the new state of the traspaso (ING, or PDA plus an authorization event when the approval is not automatic).
 */
@Transactional(rollbackFor = Exception.class)
public class GrabarTraspasosServicio implements GrabarTraspasos {

    private static final String SECUENCIA_DETALLE = "BSCL.APV.BD.Secuencias::TRA.IdDetalleTraspaso";
    private static final DateTimeFormatter PERIODO = DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneId.systemDefault());

    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final Secuencias secuencias;
    private final Reloj reloj;
    private final Colaboradores colaboradores;
    private final ValidarContrato validarContrato;
    private final GrabarTraspasosRepositorio repositorio;

    public GrabarTraspasosServicio(ConfiguracionSistema configuracion, DiccionarioMensajes diccionario, Secuencias secuencias, Reloj reloj,
            Colaboradores colaboradores, ValidarContrato validarContrato, GrabarTraspasosRepositorio repositorio) {
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.secuencias = secuencias;
        this.reloj = reloj;
        this.colaboradores = colaboradores;
        this.validarContrato = validarContrato;
        this.repositorio = repositorio;
    }

    @Override
    public GrabarTraspasosResultado ejecutar(GrabarTraspasosEntrada in) {
        try {
            List<Mensaje> mensajes = logica(in);
            // the legacy call() sets the counter AFTER logica
            return new GrabarTraspasosResultado(mensajes, Mensajes.numeroErrores(mensajes));
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private List<Mensaje> logica(GrabarTraspasosEntrada in) throws Exception {
        List<Mensaje> salida = new ArrayList<>();

        int contador = 0;
        Long idPortafolioCreado = 0L;
        Long idTraspaso = 0L;
        Long idCliente = 0L;
        String fechaIngresoTraspaso = "";
        List<DetalleTraspaso> lstDetalleTraspaso = new ArrayList<>();
        List<PortafolioContratoSAAPV> lstPortCont = new ArrayList<>();
        List<Long> idDetalleTraspaso = secuencia(SECUENCIA_DETALLE, (long) in.traspasos().size());
        String apruebaTraspasoAutomatico = Parametros.texto(configuracion, in.idEmpresa(), "APV", "APV.ApruebaTraspasoAutomatico");

        String fechaPeriodo = PERIODO.format(reloj.ahora());
        List<Long> listaIdCliente = new ArrayList<>();
        List<EstadoCliente> listaEstadoCliente = new ArrayList<>();
        Map<Long, EstadoCliente> hashCliente = new HashMap<>();

        if (in.traspasos().size() > 0) {
            listaIdCliente = in.traspasos().stream()
                    .map(traspaso -> traspaso.idCliente() != null ? traspaso.idCliente() : 0L)
                    .distinct()
                    .collect(Collectors.toList());

            if (listaIdCliente.size() > 0) {
                listaEstadoCliente = repositorio.estadoClientes(listaIdCliente);

                if (listaEstadoCliente.size() > 0) {
                    hashCliente = listaAHash(listaEstadoCliente, e -> e.IdCliente);
                }
            }
        }
        Long clienteAnterior = 0L;
        for (Traspaso fila : in.traspasos()) {

            String documentoIdentidad = "";

            // client state validation
            if (hashCliente.size() > 0) {
                EstadoCliente estadoCliente = hashCliente.get(fila.idCliente());

                if (estadoCliente != null) {
                    documentoIdentidad = estadoCliente.DocumentoIdentidad;
                    if ("BLO".equalsIgnoreCase(estadoCliente.IdEstado)) {
                        if (clienteAnterior.compareTo(estadoCliente.IdCliente) != 0) {
                            String codMsg = "CLIENTE_BLOQUEADO";
                            String descripcion = texto(in.idEmpresa(), codMsg, "DocumentoIdentidad", documentoIdentidad);
                            salida.add(new Mensaje(0L, Mensajes.MENSAJE_ERROR, codMsg, descripcion));
                        }
                        clienteAnterior = estadoCliente.IdCliente;
                        continue;
                    }
                }
            }

            // state of the portafolios of the client
            List<VistaInfoPort> portPrevioSelect = repositorio.portafoliosDelCliente(fila.idCliente());

            // does the portafolio exist? (peer ValidarContrato, option I)
            idPortafolioCreado = 0L;
            List<ContratosSAAPV> tablaValidaContrato = new ArrayList<>();
            tablaValidaContrato.add(new ContratosSAAPV(null, null, null, null, fila.documentoIdentidad(), "", "", fila.regimenTributario(),
                    fila.nombrePlan(), fila.codigoAgrupador()));
            ValidarContratoResultado outValidaContrato = validarContrato.ejecutar(new ValidarContratoEntrada(in.idEmpresa(), in.idUsuario(),
                    in.idModulo(), "I", tablaValidaContrato, new ArrayList<>(), new ArrayList<>()));

            // LEGACY DEFECT kept (see README APV-B-181; NEW finding, not listed in the README): the legacy tests P_OUT_ERRORESCONTADOR == null on the answer of ValidaContratoSAAPV.logica, which never sets it
            // (only its call() does), so the error branch ("ERROR_VALIDA_PORTAFOLIO", never added to the output) is unreachable and the result is always read
            for (ModificaContratoSAAPV filaValidaContrato : outValidaContrato.idsContrato()) {
                if (filaValidaContrato.codigo() != null) {
                    if (filaValidaContrato.idPortafolio() != null) {
                        idPortafolioCreado = filaValidaContrato.idPortafolio();
                    }
                }
            }

            if (idPortafolioCreado > 0) {
                if (portPrevioSelect.size() > 0) {
                    Map<Long, VistaInfoPort> hashIdportafolio = listaAHash(portPrevioSelect, p -> p.IdPortafolio);
                    if (hashIdportafolio.size() > 0) {
                        VistaInfoPort estadoPortafolio = hashIdportafolio.get(idPortafolioCreado);
                        if (estadoPortafolio != null) {
                            if ("BLO".equalsIgnoreCase(estadoPortafolio.IdEstado)) {
                                String codMsg = "PORTAFOLIO_BLOQUEADO";
                                // LEGACY DEFECT kept (see README APV-B-187): String compared by reference with the literal "" (documentoIdentidad is "" only while never assigned)
                                String descripcion = texto(in.idEmpresa(), codMsg, "CodigoPortafolio", fila.regimenTributario(),
                                        "DocumentoIdentidad", documentoIdentidad != "" ? documentoIdentidad : fila.documentoIdentidad());
                                salida.add(new Mensaje(0L, Mensajes.MENSAJE_ERROR, codMsg, descripcion));
                                continue;
                            }
                        }
                    }
                }
            }

            if (idPortafolioCreado == 0) {
                // creates the portafolio according to the regimen
                JsonObject resultSP = colaboradores.invocar("StoredProcedure", Json.objeto()
                        .poner("procedimiento", "BSCL.Portafolio.BD.Servicios::CrearPortafolio")
                        .poner("entrada", Json.objeto()
                                .poner("IdEmpresa", in.idEmpresa())
                                .poner("IdUsuario", in.idUsuario())
                                .poner("IdModulo", in.idModulo())
                                .poner("PropositoPortafolio", fila.idPropositoPortafolio())
                                .poner("IdCliente", fila.idCliente())
                                .poner("IdPerfilComercial", fila.idPerfilComercial())
                                .poner("CodigoPortafolio", fila.regimenTributario())
                                .poner("CodigoRegimen", fila.regimenTributario())
                                .poner("NombrePlan", fila.nombrePlan())
                                .poner("CodigoAgrupador", fila.codigoAgrupador())
                                .construir())
                        .construir());

                List<VistaInfoPort> portPostSelect = repositorio.portafoliosDelCliente(in.traspasos().get(0).idCliente());

                // a portafolio foreign to the process may have been affected
                for (VistaInfoPort portafolioPrev : portPrevioSelect) {
                    for (VistaInfoPort portafolioPost : portPostSelect) {
                        if (portafolioPrev.IdPortafolio.compareTo(portafolioPost.IdPortafolio) == 0) {
                            if (portafolioPrev.IdEstado.compareTo(portafolioPost.IdEstado) != 0) {
                                // LEGACY DEFECT kept (see README APV-B-188): the state restored is portafolioPrev.IdEstado but the legacy method only knows ACT (any other state becomes INA)
                                repositorio.modificarEstadoPortafolio(portafolioPrev.IdPortafolio, portafolioPrev.IdEstado);
                            }
                        }
                    }
                }

                if (Json.largo(resultSP, "p_out_ErroresContador").equals(Long.valueOf(0))) {
                    idPortafolioCreado = Json.largo(resultSP, "p_out_RefId");
                } else {
                    for (JsonObject errorDetalle : Json.lista(resultSP, "p_out_ErroresDetalle")) {
                        salida.add(new Mensaje(idDetalleTraspaso.get(contador), "ERROR", Json.texto(errorDetalle, "Codigo"), Json.texto(errorDetalle, "Descripcion")));
                    }
                    for (JsonObject advertenciaDetalle : Json.lista(resultSP, "p_out_AdvertenciasDetalle")) {
                        salida.add(new Mensaje(idDetalleTraspaso.get(contador), "ADVERTENCIA", Json.texto(advertenciaDetalle, "Codigo"),
                                Json.texto(advertenciaDetalle, "Descripcion")));
                    }
                }
                if (idPortafolioCreado != 0) {
                    salida.add(new Mensaje(idDetalleTraspaso.get(contador), "OK", "CREACION_PORTAFOLIO_OK",
                            "Creacion de portafolio exitosa N: " + idPortafolioCreado.toString()));
                }
            }
            if (idDetalleTraspaso.get(contador) > 0 && idPortafolioCreado > 0) {

                if (!portPrevioSelect.isEmpty()) {
                    for (VistaInfoPort portafolioPrev : portPrevioSelect) {
                        if (idPortafolioCreado.compareTo(portafolioPrev.IdPortafolio) == 0) {
                            if (portafolioPrev.IdEstado.compareTo("INA") == 0) {
                                repositorio.modificarEstadoPortafolio(portafolioPrev.IdPortafolio, "ACT");
                            }
                        }
                    }
                }

                idTraspaso = fila.idTraspaso();
                fechaIngresoTraspaso = fila.fechaIngreso();
                idCliente = fila.idCliente();
                DetalleTraspaso dtTraspaso = new DetalleTraspaso();
                dtTraspaso.IdTraspaso = fila.idTraspaso();
                dtTraspaso.IdDetalleTraspaso = idDetalleTraspaso.get(contador);
                dtTraspaso.IdCliente = fila.idCliente();
                dtTraspaso.IdPortafolio = idPortafolioCreado;
                dtTraspaso.FechaIngreso = fila.fechaIngreso();
                dtTraspaso.FechaModificacion = fila.fechaModificacion();
                dtTraspaso.IdPlanTipoInversion = fila.idPlanTipoInversion();
                dtTraspaso.IdProductoAgrupador = fila.idProductoAgrupador();
                dtTraspaso.IdRegimenTributario = fila.idRegimenTributario();
                dtTraspaso.MontoPeso = fila.montoPeso();
                dtTraspaso.MontoUF = fila.montoUF();
                dtTraspaso.Porcentaje = fila.porcentaje();
                dtTraspaso.IdEstado = "VIG";
                dtTraspaso.EsReapertura = "N";
                lstDetalleTraspaso.add(dtTraspaso);

                if (fila.idContratoSAAPV() > 0) {
                    // once the portafolios exist they are assigned to the contract
                    String origenAhorro = "";
                    if (fila.regimenTributario().equals("BF")) {
                        origenAhorro = "BF";
                    } else {
                        if (fila.regimenTributario().substring(0, 3).equals("APV")) {
                            origenAhorro = "APV";
                        }
                        if (fila.regimenTributario().substring(0, 2).equals("DC")) {
                            origenAhorro = "DC";
                        }
                    }

                    PortafolioContratoSAAPV inPortCont = new PortafolioContratoSAAPV();
                    inPortCont.IdContratoSAAPV = fila.idContratoSAAPV();
                    inPortCont.IdPortafolio = idPortafolioCreado;
                    inPortCont.OrigenAhorro = origenAhorro;
                    inPortCont.MontoPesos = Double.valueOf(fila.montoPeso().toString());
                    inPortCont.MontoUF = Double.valueOf(fila.montoUF().toString());
                    inPortCont.PorcentajeRegimenImponible = Double.valueOf(fila.porcentaje().toString());
                    inPortCont.Pago = "UNI";
                    inPortCont.PeriodoInicio = fechaPeriodo;
                    inPortCont.PeriodoTermino = fechaPeriodo;
                    inPortCont.IdEstado = "VIG";
                    lstPortCont.add(inPortCont);
                }
            }
            contador++;
        }

        if (lstDetalleTraspaso.size() > 0 && salida.stream().filter(mensaje -> "ERROR".equalsIgnoreCase(mensaje.tipo())).count() == 0) {
            int[] result = repositorio.insertarDetallesTraspaso(lstDetalleTraspaso);
            contador--;
            if (result != null && result.length > 0 && result[0] == 1) {
                salida.add(new Mensaje(idDetalleTraspaso.get(contador), "OK", "CREAR_DETALLE_TRASPASO_OK", "Se crea detalle del traspaso correctamente."));

                int[] resultado = repositorio.insertarPortafoliosContrato(lstPortCont);

                if (resultado != null && resultado.length > 0 && resultado[0] == 1) {
                    salida.add(new Mensaje(idDetalleTraspaso.get(contador), "OK", "ASIGNA_PORTAFOLIO_CONTRATO_OK",
                            "Se asigna Portafolio al detalle traspaso correctamente."));
                } else {
                    salida.add(new Mensaje(idDetalleTraspaso.get(contador), "ERROR", "ERROR_CREAR_PORTAFOLIO_CONTRATO",
                            "Problema al asignar portafolio al detalle traspaso."));
                }

                if (apruebaTraspasoAutomatico.equals("N")) {
                    // the traspaso goes to "pending authorization"
                    List<TraspasoModificar> lstTraspasoMod = new ArrayList<>();
                    TraspasoModificar tm = new TraspasoModificar();
                    tm.IdEstado = "PDA";
                    tm.IdTraspaso = idTraspaso;
                    lstTraspasoMod.add(tm);

                    int[] resultadoMod = repositorio.modificarEstadoTraspaso(lstTraspasoMod);
                    if (resultadoMod != null && resultadoMod.length > 0 && resultadoMod[0] == 1) {
                        salida.add(new Mensaje(idDetalleTraspaso.get(contador), "OK", "MODIFICA_ESTADO_OK", "Modifica estado del Traspaso Folio: " + idTraspaso.toString()));
                    } else {
                        salida.add(new Mensaje(idDetalleTraspaso.get(contador), "ERROR", "ERROR_MODIFICA_ESTADO", "Problema al modificar estado del Traspaso."));
                    }

                    JsonObject evento = Json.objeto()
                            .poner("IdUnion", null)
                            .poner("IdTipoEvento", "INGTRA")
                            .poner("IdModuloSolicita", "APV")
                            .poner("IdUsuarioSolicita", in.idUsuario())
                            .poner("Fecha", fechaIngresoTraspaso)
                            .poner("IdTipoOperacion", null)
                            .poner("IdConcepto", "INGTRA")
                            .poner("NumeroOperacionConcepto", idTraspaso.toString())
                            .poner("FechaOperacionConcepto", fechaIngresoTraspaso)
                            .poner("IdCliente", idCliente)
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
                            .poner("Glosa", "INGRESO DE TRASPASO APV Folio: " + idTraspaso.toString())
                            .construir();
                    List<JsonObject> tablaEventos = new ArrayList<>();
                    tablaEventos.add(evento);

                    if (tablaEventos.size() > 0) {
                        JsonObject salidaAut = colaboradores.invocar("CrearEventoAutorizacionMasivo", Json.objeto()
                                .poner("P_IDEMPRESA", in.idEmpresa())
                                .poner("P_IDUSUARIO", in.idUsuario())
                                .poner("P_IDMODULO", in.idModulo())
                                .poner("P_SIMULAROPERACION", "N")
                                .poner("P_USAREGLASNEGOCIO", "N")
                                .poner("P_TABLAEVENTOS", tablaEventos)
                                .poner("P_DETALLE", new ArrayList<>())
                                .construir());
                        for (JsonObject ad : Json.lista(salidaAut, "P_OUT_ADVERTENCIASDETALLE")) {
                            salida.add(new Mensaje(idDetalleTraspaso.get(contador), Mensajes.MENSAJE_ADVERTENCIA, Json.texto(ad, "Codigo"), Json.texto(ad, "Descripcion")));
                        }
                        for (JsonObject ad : Json.lista(salidaAut, "P_OUT_ERRORESDETALLE")) {
                            salida.add(new Mensaje(idDetalleTraspaso.get(contador), Mensajes.MENSAJE_ERROR, Json.texto(ad, "Codigo"), Json.texto(ad, "Descripcion")));
                        }
                    }
                } else {
                    // the traspaso goes to "ingresado"
                    List<TraspasoModificar> lstTraspasoMod = new ArrayList<>();
                    TraspasoModificar tm = new TraspasoModificar();
                    tm.IdEstado = "ING";
                    tm.IdTraspaso = idTraspaso;
                    lstTraspasoMod.add(tm);

                    int[] resultadoMod = repositorio.modificarEstadoTraspaso(lstTraspasoMod);
                    if (resultadoMod != null && resultadoMod.length > 0 && resultadoMod[0] == 1) {
                        salida.add(new Mensaje(idDetalleTraspaso.get(contador), "OK", "MODIFICA_ESTADO_OK", "Modifica estado del Traspaso Folio: " + idTraspaso.toString()));
                    } else {
                        salida.add(new Mensaje(idDetalleTraspaso.get(contador), "ERROR", "ERROR_MODIFICA_ESTADO", "Problema al modificar estado del Traspaso."));
                    }
                }
            } else {
                salida.add(new Mensaje(1L, "ERROR", "ERROR_CREAR_DETALLE_TRASPASO", "Problema al crear detalle del traspaso."));
            }
        }
        return salida;
    }

    /** Legacy {@code Utils.getSecuencia(con, name, n)}: n values (empty when n is not positive); the port draws one value per call. */
    private List<Long> secuencia(String nombre, Long cantidad) {
        List<Long> valores = new ArrayList<>();
        if (cantidad == null || cantidad <= 0) {
            return valores;
        }
        for (long i = 0; i < cantidad; i++) {
            valores.add(secuencias.siguiente(nombre));
        }
        return valores;
    }

    /** Legacy {@code Diccionario.getMensaje(...).add(k, v)...msg()}: a null value is ignored. */
    private String texto(Long idEmpresa, String codigo, String... clavesYValores) {
        Map<String, String> valores = new HashMap<>();
        for (int i = 0; i < clavesYValores.length; i += 2) {
            if (clavesYValores[i + 1] != null) {
                valores.put(clavesYValores[i], clavesYValores[i + 1]);
            }
        }
        return diccionario.mensaje(idEmpresa, codigo, valores);
    }

    /** Legacy {@code Utils.List2Hash}: a repeated key is an {@code Exception("key field is not unique")}. */
    private static <K, T> Map<K, T> listaAHash(List<T> lista, Function<T, K> clave) throws Exception {
        Map<K, T> hash = new HashMap<>();
        for (T obj : lista) {
            K k = clave.apply(obj);
            if (hash.containsKey(k)) {
                throw new Exception("key field is not unique");
            }
            hash.put(k, obj);
        }
        return hash;
    }
}
