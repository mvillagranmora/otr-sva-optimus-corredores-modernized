package cl.bcs.contabilidad.adapter.in;

import cl.bcs.contabilidad.service.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Receives J-type JavaCall dispatches from BcsPlataformaWeb's CallController.
 *
 * Protocol:
 *   POST /internal/java-call
 *   Body: {"class": "fully.qualified.ClassName", "params": {...}, "skip": N, "take": M}
 *
 * Unknown classes return 503 → BcsGateway falls back to TomEE.
 *
 * Source: PluginLoader pattern from legacy CallServlet.java
 */
@RestController
public class InternalJavaCallController {

    private static final Logger log = LoggerFactory.getLogger(InternalJavaCallController.class);

    private final AgregarCuentasPorMetadataService agregarCuentasPorMetadata;
    private final AgregarMetadataService agregarMetadata;
    private final AnularCentralizacionService anularCentralizacion;
    private final CargarAsientoComprobanteService cargarAsientoComprobante;
    private final CargarCabeceraComprobanteService cargarCabeceraComprobante;
    private final CargarDeterminanteComprobanteService cargarDeterminanteComprobante;
    private final CentralizaGeneraCabeceraService centralizaGeneraCabecera;
    private final CentralizaMultiSeleccionService centralizaMultiSeleccion;
    private final ConfigurarConceptoNormativoService configurarConceptoNormativo;
    private final ConsultarConfiguracionesConceptoNormativoService consultarConfiguracionesConceptoNormativo;
    private final ConsultarConceptoNormativoService consultarConceptoNormativo;
    private final ConsultarCondicionesConceptosNormativosService consultarCondicionesConceptosNormativos;
    private final ConsultarOperacionesClienteRelacionadoService consultarOperacionesClienteRelacionado;
    private final CuadraturaCierreDiarioService cuadraturaCierreDiario;
    private final EditarCuentaBasileaService editarCuentaBasilea;
    private final EditarMetadataService editarMetadata;
    private final EliminarMetadataService eliminarMetadata;
    private final EnviarPreComprobanteService enviarPreComprobante;
    private final EnvioContabilidadService envioContabilidad;
    private final FiltrarCuentasService filtrarCuentas;
    private final GeneraPreComprobanteMultiSeleccionService generaPreComprobanteMultiSeleccion;
    private final GenerarInformeClienteRelacionadoService generarInformeClienteRelacionado;
    private final GenerarJsonAsientoSapService generarJsonAsientoSap;
    private final HomologarCuentasContablesService homologarCuentasContables;
    private final ProcesarInterfazBasileaService procesarInterfazBasilea;
    private final ReversaPreComprobanteService reversaPreComprobante;

    public InternalJavaCallController(
            AgregarCuentasPorMetadataService agregarCuentasPorMetadata,
            AgregarMetadataService agregarMetadata,
            AnularCentralizacionService anularCentralizacion,
            CargarAsientoComprobanteService cargarAsientoComprobante,
            CargarCabeceraComprobanteService cargarCabeceraComprobante,
            CargarDeterminanteComprobanteService cargarDeterminanteComprobante,
            CentralizaGeneraCabeceraService centralizaGeneraCabecera,
            CentralizaMultiSeleccionService centralizaMultiSeleccion,
            ConfigurarConceptoNormativoService configurarConceptoNormativo,
            ConsultarConfiguracionesConceptoNormativoService consultarConfiguracionesConceptoNormativo,
            ConsultarConceptoNormativoService consultarConceptoNormativo,
            ConsultarCondicionesConceptosNormativosService consultarCondicionesConceptosNormativos,
            ConsultarOperacionesClienteRelacionadoService consultarOperacionesClienteRelacionado,
            CuadraturaCierreDiarioService cuadraturaCierreDiario,
            EditarCuentaBasileaService editarCuentaBasilea,
            EditarMetadataService editarMetadata,
            EliminarMetadataService eliminarMetadata,
            EnviarPreComprobanteService enviarPreComprobante,
            EnvioContabilidadService envioContabilidad,
            FiltrarCuentasService filtrarCuentas,
            GeneraPreComprobanteMultiSeleccionService generaPreComprobanteMultiSeleccion,
            GenerarInformeClienteRelacionadoService generarInformeClienteRelacionado,
            GenerarJsonAsientoSapService generarJsonAsientoSap,
            HomologarCuentasContablesService homologarCuentasContables,
            ProcesarInterfazBasileaService procesarInterfazBasilea,
            ReversaPreComprobanteService reversaPreComprobante) {
        this.agregarCuentasPorMetadata = agregarCuentasPorMetadata;
        this.agregarMetadata = agregarMetadata;
        this.anularCentralizacion = anularCentralizacion;
        this.cargarAsientoComprobante = cargarAsientoComprobante;
        this.cargarCabeceraComprobante = cargarCabeceraComprobante;
        this.cargarDeterminanteComprobante = cargarDeterminanteComprobante;
        this.centralizaGeneraCabecera = centralizaGeneraCabecera;
        this.centralizaMultiSeleccion = centralizaMultiSeleccion;
        this.configurarConceptoNormativo = configurarConceptoNormativo;
        this.consultarConfiguracionesConceptoNormativo = consultarConfiguracionesConceptoNormativo;
        this.consultarConceptoNormativo = consultarConceptoNormativo;
        this.consultarCondicionesConceptosNormativos = consultarCondicionesConceptosNormativos;
        this.consultarOperacionesClienteRelacionado = consultarOperacionesClienteRelacionado;
        this.cuadraturaCierreDiario = cuadraturaCierreDiario;
        this.editarCuentaBasilea = editarCuentaBasilea;
        this.editarMetadata = editarMetadata;
        this.eliminarMetadata = eliminarMetadata;
        this.enviarPreComprobante = enviarPreComprobante;
        this.envioContabilidad = envioContabilidad;
        this.filtrarCuentas = filtrarCuentas;
        this.generaPreComprobanteMultiSeleccion = generaPreComprobanteMultiSeleccion;
        this.generarInformeClienteRelacionado = generarInformeClienteRelacionado;
        this.generarJsonAsientoSap = generarJsonAsientoSap;
        this.homologarCuentasContables = homologarCuentasContables;
        this.procesarInterfazBasilea = procesarInterfazBasilea;
        this.reversaPreComprobante = reversaPreComprobante;
    }

    @PostMapping(path = "/internal/java-call",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> dispatch(@RequestBody String rawBody,
                                           @RequestHeader(value = "X-SESSION-TOKEN",
                                                          required = false) String token) {
        JsonObject body;
        try {
            body = JsonParser.parseString(rawBody).getAsJsonObject();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON body");
        }

        String fqn        = body.has("class")  ? body.get("class").getAsString()         : "";
        JsonObject params = body.has("params") ? body.get("params").getAsJsonObject()     : new JsonObject();
        int skip          = body.has("skip")   ? body.get("skip").getAsInt()              : 0;
        int take          = body.has("take")   ? body.get("take").getAsInt()              : 0;

        if (fqn.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'class' field required");
        }

        log.debug("InternalJavaCall: class={} skip={} take={}", fqn, skip, take);

        JsonObject result;
        try {
            result = route(fqn, params);
        } catch (FuncionalidadNoMigradaException e) {
            log.warn("InternalJavaCall: {} needs the legacy runtime: {}", fqn, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
        }

        if (result == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "JavaCall class '" + fqn + "' not yet migrated; TomEE fallback active");
        }

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(result.toString());
    }

    private JsonObject route(String fqn, JsonObject params) {
        return switch (fqn) {
            case "cl.bcs.contabilidad.AgregarCuentasPorMetadata"
                -> agregarCuentasPorMetadata.execute(params);
            case "cl.bcs.contabilidad.AgregarMetadata"
                -> agregarMetadata.execute(params);
            case "cl.bcs.contabilidad.AnularCentralizacion"
                -> anularCentralizacion.execute(params);
            case "cl.bcs.contabilidad.CargarAsientoComprobante"
                -> cargarAsientoComprobante.execute(params);
            case "cl.bcs.contabilidad.CargarCabeceraComprobante"
                -> cargarCabeceraComprobante.execute(params);
            case "cl.bcs.contabilidad.CargarDeterminanteComprobante"
                -> cargarDeterminanteComprobante.execute(params);
            case "cl.bcs.contabilidad.CentralizaGeneraCabecera"
                -> centralizaGeneraCabecera.execute(params);
            case "cl.bcs.contabilidad.CentralizaMultiSeleccion"
                -> centralizaMultiSeleccion.execute(params);
            case "cl.bcs.contabilidad.ConfigurarConceptoNormativo"
                -> configurarConceptoNormativo.execute(params);
            case "cl.bcs.contabilidad.ConsultarConfiguracionesConceptoNormativo"
                -> consultarConfiguracionesConceptoNormativo.execute(params);
            case "cl.bcs.contabilidad.ConsultarConceptoNormativo"
                -> consultarConceptoNormativo.execute(params);
            case "cl.bcs.contabilidad.ConsultarCondicionesConceptosNormativos"
                -> consultarCondicionesConceptosNormativos.execute(params);
            case "cl.bcs.contabilidad.ConsultarOperacionesClienteRelacionado"
                -> consultarOperacionesClienteRelacionado.execute(params);
            case "cl.bcs.contabilidad.CuadraturaCierreDiario"
                -> cuadraturaCierreDiario.execute(params);
            case "cl.bcs.contabilidad.EditarCuentaBasilea"
                -> editarCuentaBasilea.execute(params);
            case "cl.bcs.contabilidad.EditarMetadata"
                -> editarMetadata.execute(params);
            case "cl.bcs.contabilidad.EliminarMetadata"
                -> eliminarMetadata.execute(params);
            case "cl.bcs.contabilidad.EnviarPreComprobante"
                -> enviarPreComprobante.execute(params);
            case "cl.bcs.contabilidad.EnvioContabilidad"
                -> envioContabilidad.execute(params);
            case "cl.bcs.contabilidad.FiltrarCuentas"
                -> filtrarCuentas.execute(params);
            case "cl.bcs.contabilidad.GeneraPreComprobanteMultiSeleccion"
                -> generaPreComprobanteMultiSeleccion.execute(params);
            case "cl.bcs.contabilidad.GenerarInformeClienteRelacionado"
                -> generarInformeClienteRelacionado.execute(params);
            case "cl.bcs.contabilidad.GenerarJsonAsientoSap"
                -> generarJsonAsientoSap.execute(params);
            case "cl.bcs.contabilidad.HomologarCuentasContables"
                -> homologarCuentasContables.execute(params);
            case "cl.bcs.contabilidad.ProcesarInterfazBasilea"
                -> procesarInterfazBasilea.execute(params);
            case "cl.bcs.contabilidad.ReversaPreComprobante"
                -> reversaPreComprobante.execute(params);
            default -> null;
        };
    }
}
