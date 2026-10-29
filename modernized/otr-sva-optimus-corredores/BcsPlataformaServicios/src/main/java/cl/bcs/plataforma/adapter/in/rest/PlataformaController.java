package cl.bcs.plataforma.adapter.in.rest;

import org.springframework.web.bind.annotation.*;

/**
 * Stub controller for platform core (PLA) operations.
 * Legacy package: cl.bcs.plataforma.pla (81 classes)
 *
 * Covers:
 *   - ConsultarParametros, ModificarParametrosConfiguracion
 *   - AgregarLog, InsertarLogServicio, InsertarLogVentana
 *   - CambiarFechaSistema, AjustarFechaSistema
 *   - ObtenerDiaAnterior, ObtenerDiaSiguiente, ObtenerPlazo
 *   - ObtenerDiasHabilesAnteriores, ObtenerDiaHabilMultiplesPaises
 *   - CargarDiasInhabiles, AperturaMesasModulos
 *   - GuardarConfiguracionGrillaUsuario, GuardarConfiguracionGrillaPredeterminado
 *   - AgregarDiccionario, ModificarDiccionario
 *   - ProcesarCargaMasivaPlataforma, GestionDataHistorica
 *   - MonitorPlataforma, RevisarRequisitosBasicos
 *   - ConsultarFlujoCaja, SaldoFlujoCaja, ConsultarFlujoCajaCliente
 *   - FormatearMonedaFormato, ConvertirNumeroTexto (→ ConvertirMontoEscritoController)
 *   - AgregarGlosaTipoInstrumento, QuitarGlosaTipoInstrumento
 *   - CrearRegistroSolicitudCambio, ConsultarRegistroSolicitudCambio
 *   - GenerarInformeParametrosSistema, RegistrarUsoVentana
 *   - EjecutarTareaJenkins, ExtraerPreciosBECRuebasDB
 *   - FirmasImageManager, AgregarMensajeWebsocket
 *   - NotificarEstadoOrdenesOWP
 *
 * TODO: wire when PLA.Servicio DDL + SELECT * export is provided.
 */
@RestController
@RequestMapping("/api/plataforma")
public class PlataformaController {

    @GetMapping("/health")
    public String health() {
        return "plataforma-service OK";
    }

    @PostMapping("/parametros/consultar")
    public Object consultarParametros(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.Parametro not yet wired");
    }

    @PostMapping("/parametros/modificar")
    public Object modificarParametros(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.Parametro not yet wired");
    }

    @PostMapping("/log/agregar")
    public Object agregarLog(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.Log not yet wired");
    }

    @PostMapping("/log/insertar-servicio")
    public Object insertarLogServicio(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.LogServicio not yet wired");
    }

    @PostMapping("/log/insertar-ventana")
    public Object insertarLogVentana(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.LogVentana not yet wired");
    }

    @PostMapping("/fecha/cambiar")
    public Object cambiarFechaSistema(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.FechaSistema not yet wired");
    }

    @PostMapping("/fecha/ajustar")
    public Object ajustarFechaSistema(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.FechaSistema not yet wired");
    }

    @PostMapping("/dias/anterior")
    public Object obtenerDiaAnterior(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.Feriado not yet wired");
    }

    @PostMapping("/dias/siguiente")
    public Object obtenerDiaSiguiente(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.Feriado not yet wired");
    }

    @PostMapping("/dias/plazo")
    public Object obtenerPlazo(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.Feriado not yet wired");
    }

    @PostMapping("/dias/habiles-anteriores")
    public Object obtenerDiasHabilesAnteriores(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.Feriado not yet wired");
    }

    @PostMapping("/dias/habil-multiples-paises")
    public Object obtenerDiaHabilMultiplesPaises(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.Feriado not yet wired");
    }

    @PostMapping("/dias/cargar-inhabiles")
    public Object cargarDiasInhabiles(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.Feriado not yet wired");
    }

    @PostMapping("/flujo-caja/consultar")
    public Object consultarFlujoCaja(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.FlujoCaja not yet wired");
    }

    @PostMapping("/flujo-caja/saldo")
    public Object saldoFlujoCaja(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.FlujoCaja not yet wired");
    }

    @PostMapping("/grilla/guardar-usuario")
    public Object guardarConfiguracionGrillaUsuario(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.ConfigGrilla not yet wired");
    }

    @PostMapping("/grilla/guardar-predeterminado")
    public Object guardarConfiguracionGrillaPredeterminado(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.ConfigGrilla not yet wired");
    }

    @PostMapping("/diccionario/agregar")
    public Object agregarDiccionario(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.Diccionario not yet wired");
    }

    @PostMapping("/diccionario/modificar")
    public Object modificarDiccionario(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.Diccionario not yet wired");
    }

    @PostMapping("/monitor")
    public Object monitorPlataforma(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.Monitor not yet wired");
    }

    @PostMapping("/solicitud-cambio/crear")
    public Object crearRegistroSolicitudCambio(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.SolicitudCambio not yet wired");
    }

    @PostMapping("/solicitud-cambio/consultar")
    public Object consultarRegistroSolicitudCambio(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.SolicitudCambio not yet wired");
    }

    @PostMapping("/formato-moneda")
    public Object formatearMonedaFormato(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.MonedaFormato not yet wired");
    }

    @PostMapping("/apertura-mesas")
    public Object aperturaMesasModulos(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.MesaModulo not yet wired");
    }

    @PostMapping("/glosa-tipo-instrumento/agregar")
    public Object agregarGlosaTipoInstrumento(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.GlosaTipoInstrumento not yet wired");
    }

    @PostMapping("/glosa-tipo-instrumento/quitar")
    public Object quitarGlosaTipoInstrumento(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.GlosaTipoInstrumento not yet wired");
    }

    @PostMapping("/mapa-relaciones/consultar")
    public Object consultarMapaRelaciones(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.MapaRelaciones not yet wired");
    }

    @PostMapping("/nemo-svs/obtener")
    public Object obtenerNemoSVS(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.NemoSVS not yet wired");
    }

    @PostMapping("/garantia/crear-cobertura")
    public Object crearCoberturaGarantia(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.CoberturaGarantia not yet wired");
    }

    @PostMapping("/ventana/registrar-uso")
    public Object registrarUsoVentana(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.UsoVentana not yet wired");
    }

    @PostMapping("/websocket/mensaje")
    public Object agregarMensajeWebsocket(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.MensajeWebsocket not yet wired");
    }

    @PostMapping("/carga-masiva")
    public Object procesarCargaMasivaPlataforma(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.CargaMasiva not yet wired");
    }

    @PostMapping("/data-historica/gestionar")
    public Object gestionDataHistorica(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.DataHistorica not yet wired");
    }

    @PostMapping("/data-historica/limpiar")
    public Object limpiarDataHistorico(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.DataHistorica not yet wired");
    }

    @PostMapping("/data-historica/migrar")
    public Object migrarDataHistorico(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PLA.DataHistorica not yet wired");
    }

    @PostMapping("/ordenes/notificar-estado-owp")
    public Object notificarEstadoOrdenesOWP(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — OWP integration not yet wired");
    }

    @PostMapping("/firmas/imagen")
    public Object firmasImageManager(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires external image service not yet wired");
    }

    @PostMapping("/jenkins/ejecutar-tarea")
    public Object ejecutarTareaJenkins(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires Jenkins integration not yet wired");
    }

    @PostMapping("/becb/extraer-precios")
    public Object extraerPreciosBECB(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires BECB integration not yet wired");
    }

    @PostMapping("/pac/validar-requisitos")
    public Object revisarRequisitosBasicos(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PAC validation not yet wired");
    }
}
