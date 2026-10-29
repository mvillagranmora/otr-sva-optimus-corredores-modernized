package cl.bcs.apv.application.contratos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.apv.api.casouso.CambiarContratosAPlanActivo;
import cl.bcs.apv.api.casouso.SolicitarCambioAPlanActivo;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.cambiarcontratosaplanactivo.CambiarContratosAPlanActivoEntrada;
import cl.bcs.apv.api.dto.cambiarcontratosaplanactivo.CambiarContratosAPlanActivoResultado;
import cl.bcs.apv.api.dto.cambiarcontratosaplanactivo.ContratoCambioPlanIn;
import cl.bcs.apv.api.dto.solicitarcambioaplanactivo.ContratoCambioPlanInWrapper;
import cl.bcs.apv.api.dto.solicitarcambioaplanactivo.SolicitarCambioAPlanActivoEntrada;
import cl.bcs.apv.api.dto.solicitarcambioaplanactivo.SolicitarCambioAPlanActivoResultado;
import cl.bcs.apv.application.comun.Json;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.application.comun.Parametros;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;

/**
 * Use case {@code SolicitarCambioAPlanActivo}: legacy {@code cl.bcs.APV.CambiarAPlanActivoContratoSAAPVWrapper}. With {@code APV.CambioPlanRequiereAutorizacion}
 * = "S" it creates one authorization event per client; otherwise it delegates in {@link CambiarContratosAPlanActivo}. The legacy {@code call()} recomputes the error
 * counter from the messages in both branches.
 */
@Transactional(rollbackFor = Exception.class)
public class SolicitarCambioAPlanActivoServicio implements SolicitarCambioAPlanActivo {

    private final ConfiguracionSistema configuracion;
    private final Colaboradores colaboradores;
    private final CambiarContratosAPlanActivo cambiarContratosAPlanActivo;

    public SolicitarCambioAPlanActivoServicio(ConfiguracionSistema configuracion, Colaboradores colaboradores,
            CambiarContratosAPlanActivo cambiarContratosAPlanActivo) {
        this.configuracion = configuracion;
        this.colaboradores = colaboradores;
        this.cambiarContratosAPlanActivo = cambiarContratosAPlanActivo;
    }

    @Override
    public SolicitarCambioAPlanActivoResultado ejecutar(SolicitarCambioAPlanActivoEntrada entrada) {
        List<Mensaje> mensajes = new ArrayList<>();
        Long advertencias;

        String cambioPlanRequiereAutorizacion = Parametros.texto(configuracion, entrada.idEmpresa(), Parametros.APV, "APV.CambioPlanRequiereAutorizacion");
        String fechaSistema = Parametros.fechaSistema(configuracion, entrada.idEmpresa());

        if ("S".equalsIgnoreCase(cambioPlanRequiereAutorizacion)) {
            // EVENTOS DE AUTORIZACION
            List<JsonObject> tablaEventos = new ArrayList<>();
            List<JsonObject> tablaEventoDetalle = new ArrayList<>();
            Long lngEvento = 0L;

            // LEGACY DEFECT kept (see README APV-A-346): the events follow the HashMap iteration order of the client ids, and a null client id or a null list is a
            // NullPointerException (Collectors.groupingBy)
            Map<Long, List<ContratoCambioPlanInWrapper>> groupByIdCliente = entrada.contratoCambioPlanIn().stream()
                    .collect(Collectors.groupingBy(x -> x.idCliente()));

            Iterator<Long> it = groupByIdCliente.keySet().iterator();
            while (it.hasNext()) {
                Long key = it.next();
                ++lngEvento;
                Long idUnion = lngEvento;

                int contador = 1;
                StringBuilder glosa = new StringBuilder();
                glosa.append("RutCliente: " + groupByIdCliente.get(key).get(0).rutCliente() + " Portafolios APV: ");
                for (ContratoCambioPlanInWrapper contratoIn : groupByIdCliente.get(key)) {
                    // elementos que iran a nivel de detalle de las autorizaciones
                    tablaEventoDetalle.add(Json.objeto()
                            .poner("IdUnion", idUnion)
                            .poner("NombreCampo", "Datos")
                            .poner("TipoDato", "STRING")
                            .poner("Valor", contratoIn.idPersona().toString() + ";" + contratoIn.idCliente().toString() + ";"
                                    + contratoIn.idPortafolio().toString() + ";" + contratoIn.idContratoSAAPV().toString() + ";" + contratoIn.folioContratoSAAPV())
                            .poner("Clasificacion", "1") // indica que este valor ira a nivel de cabecera
                            .construir());
                    glosa.append(contratoIn.regimenTributario().toString());

                    if (contador != groupByIdCliente.get(key).size()) {
                        glosa.append(", ");
                    }
                    contador++;
                }

                tablaEventos.add(Json.objeto()
                        .poner("IdUnion", idUnion)
                        .poner("IdTipoEvento", "MOPLAC")
                        .poner("IdModuloSolicita", "APV")
                        .poner("IdUsuarioSolicita", entrada.idUsuario())
                        .poner("Fecha", fechaSistema)
                        .poner("IdTipoOperacion", null)
                        .poner("IdConcepto", "MOPLAC")
                        .poner("NumeroOperacionConcepto", null)
                        .poner("FechaOperacionConcepto", null)
                        .poner("IdCliente", key)
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
                        .poner("IdMoneda", "")
                        .poner("Glosa", glosa.toString())
                        .construir());
            }

            advertencias = null;
            Long errores = null;
            if (tablaEventos.size() > 0) {
                JsonObject salida = colaboradores.invocar("CrearEventoAutorizacionMasivo", Json.objeto()
                        .poner("P_IDEMPRESA", entrada.idEmpresa())
                        .poner("P_IDUSUARIO", entrada.idUsuario())
                        .poner("P_IDMODULO", entrada.idModulo())
                        .poner("P_SIMULAROPERACION", "N")
                        .poner("P_USAREGLASNEGOCIO", "N")
                        .poner("P_TABLAEVENTOS", tablaEventos)
                        .poner("P_DETALLE", tablaEventoDetalle)
                        .construir());

                // the legacy Salida starts with its counters at 0: a counter that is absent from the answer is 0, one that is JSON null is null (NullPointerException below)
                Long erroresSalida = salida.has("P_OUT_ERRORESCONTADOR") ? Json.largo(salida, "P_OUT_ERRORESCONTADOR") : Long.valueOf(0L);
                if (erroresSalida > 0) {
                    errores = erroresSalida;
                    for (JsonObject error : Json.lista(salida, "P_OUT_ERRORESDETALLE")) {
                        mensajes.add(Mensajes.de(erroresSalida, Mensajes.MENSAJE_ERROR, Json.texto(error, "Codigo"), Json.texto(error, "Descripcion")));
                    }
                } else {
                    advertencias = lngEvento;
                    mensajes.add(Mensajes.de(1L, Mensajes.MENSAJE_ADVERTENCIA, "EVENTO_AUTORIZACION_CREADO", "Evento de autorizacion creado."));
                }
            }
            return new SolicitarCambioAPlanActivoResultado(mensajes, Mensajes.numeroErrores(mensajes), advertencias);
        }

        List<ContratoCambioPlanIn> lstContratos = new ArrayList<>();
        for (ContratoCambioPlanInWrapper in : entrada.contratoCambioPlanIn()) {
            lstContratos.add(new ContratoCambioPlanIn(in.idPersona(), in.idCliente(), in.idPortafolio(), in.idContratoSAAPV(), in.folioContratoSAAPV()));
        }
        CambiarContratosAPlanActivoResultado salida = cambiarContratosAPlanActivo
                .ejecutar(new CambiarContratosAPlanActivoEntrada(entrada.idEmpresa(), entrada.idUsuario(), entrada.idModulo(), lstContratos));
        return new SolicitarCambioAPlanActivoResultado(salida.mensajes(), Mensajes.numeroErrores(salida.mensajes()), 0L);
    }
}
