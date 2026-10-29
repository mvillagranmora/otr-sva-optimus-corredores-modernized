package cl.bcs.plataforma.adapter.in.rest;

import org.springframework.web.bind.annotation.*;

/**
 * Stub controller for remaining domain areas.
 *
 * Covers legacy packages:
 *   - lim (21)  — Limits: LIM.Limite, LIM.LimitePortafolio, etc.
 *   - mer (20)  — Market: MER.Paridad, MER.VectorPrecio, MER.Deterioro, etc.
 *   - evt (14)  — Events: EVT.Evento, EVT.CalendarioEvento, etc.
 *   - enc (14)  — Surveys: ENC.Encuesta, ENC.Respuesta, ENC.Puntaje, etc.
 *   - val (11)  — Validation: VAL.Validacion, VAL.Regla, etc.
 *   - frm (11)  — Forms: FRM.Formulario, FRM.Campo, FRM.Validacion, etc.
 *   - ctr (8)   — Contracts: CTR.Contrato, CTR.ContratoDetalle, etc.
 *   - tas (7)   — Tasks: TAS.Tarea, TAS.AgendaTarea, etc.
 *   - doc (7)   — Documents: DOC.Documento, DOC.PlantillaDocumento, etc.
 *   - ind (6)   — Indicators: IND.Indicador, IND.TipoIndicador, etc.
 *   - bat (6)   — Batch: BAT.Programa, BAT.Agenda, BAT.Log, etc.
 *   - bck (1)   — Blockchain: BCK.Login
 *   - sap (1)   — SAP integration
 *   - ftp (1)   — FTP file transfer
 *   - cua (1)   — ?
 *   - gta (2)   — ?
 *   - cre (3)   — ?
 *
 * TODO: wire each endpoint when PLA.Servicio DDL + SELECT * export is provided.
 */
@RestController
@RequestMapping("/api/plataforma")
public class RemainingDomainsController {

    // -----------------------------------------------------------------------
    // LIM — Limits
    // -----------------------------------------------------------------------

    @PostMapping("/limite/consultar")
    public Object consultarLimite(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — LIM.Limite not yet wired");
    }

    @PostMapping("/limite/crear")
    public Object crearLimite(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — LIM.Limite not yet wired");
    }

    @PostMapping("/limite/modificar")
    public Object modificarLimite(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — LIM.Limite not yet wired");
    }

    // -----------------------------------------------------------------------
    // MER — Market
    // -----------------------------------------------------------------------

    @PostMapping("/mercado/calcular-paridad")
    public Object calcularParidad(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — MER.Paridad not yet wired");
    }

    @PostMapping("/mercado/cargar-vector-precio-ws")
    public Object cargaArchivoVectorPrecioWS(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — MER.VectorPrecio not yet wired");
    }

    @PostMapping("/mercado/generar-log-deterioro-emisor")
    public Object generarLogDeterioroEmisor(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — MER.DeterioroEmisor not yet wired");
    }

    // -----------------------------------------------------------------------
    // EVT — Events
    // -----------------------------------------------------------------------

    @PostMapping("/evento/crear-calendario")
    public Object crearCalendarioEvento(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — EVT.CalendarioEvento not yet wired");
    }

    @PostMapping("/evento/consultar-calendario")
    public Object consultarCalendarioEvento(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — EVT.CalendarioEvento not yet wired");
    }

    // -----------------------------------------------------------------------
    // ENC — Surveys
    // -----------------------------------------------------------------------

    @PostMapping("/encuesta/crear-definicion")
    public Object crearDefinicionEncuesta(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ENC.Encuesta not yet wired");
    }

    @PostMapping("/encuesta/responder")
    public Object responderEncuesta(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ENC.Respuesta not yet wired");
    }

    @PostMapping("/encuesta/eliminar-definicion-puntaje")
    public Object eliminarDefinicionPuntaje(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ENC.Puntaje not yet wired");
    }

    // -----------------------------------------------------------------------
    // VAL — Validation
    // -----------------------------------------------------------------------

    @PostMapping("/validacion/ejecutar")
    public Object ejecutarValidacion(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — VAL.Validacion not yet wired");
    }

    // -----------------------------------------------------------------------
    // FRM — Forms
    // -----------------------------------------------------------------------

    @PostMapping("/formulario/validar-campos-obligatorios")
    public Object validarCamposObligatorios(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — FRM.Formulario not yet wired");
    }

    @PostMapping("/formulario/asignar-campos")
    public Object asignarCamposFormulario(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — FRM.Campo not yet wired");
    }

    // -----------------------------------------------------------------------
    // CTR — Contracts
    // -----------------------------------------------------------------------

    @PostMapping("/contrato/crear")
    public Object crearContrato(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — CTR.Contrato not yet wired");
    }

    @PostMapping("/contrato/modificar")
    public Object modificarContrato(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — CTR.Contrato not yet wired");
    }

    @PostMapping("/contrato/consultar")
    public Object consultarContrato(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — CTR.Contrato not yet wired");
    }

    // -----------------------------------------------------------------------
    // TAS — Tasks
    // -----------------------------------------------------------------------

    @PostMapping("/tarea/crear")
    public Object crearTarea(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — TAS.Tarea not yet wired");
    }

    @PostMapping("/tarea/consultar")
    public Object consultarTarea(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — TAS.Tarea not yet wired");
    }

    // -----------------------------------------------------------------------
    // DOC — Documents
    // -----------------------------------------------------------------------

    @PostMapping("/documento/generar")
    public Object generarDocumento(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — DOC.Documento not yet wired");
    }

    @PostMapping("/documento/consultar")
    public Object consultarDocumento(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — DOC.Documento not yet wired");
    }

    // -----------------------------------------------------------------------
    // IND — Indicators
    // -----------------------------------------------------------------------

    @PostMapping("/indicador/consultar")
    public Object consultarIndicador(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — IND.Indicador not yet wired");
    }

    @PostMapping("/indicador/cargar")
    public Object cargarIndicador(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — IND.Indicador not yet wired");
    }

    // -----------------------------------------------------------------------
    // BAT — Batch
    // -----------------------------------------------------------------------

    @PostMapping("/batch/agregar-agenda")
    public Object agregarAgenda(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — BAT.Agenda not yet wired");
    }

    @PostMapping("/batch/agregar-programa")
    public Object agregarPrograma(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — BAT.Programa not yet wired");
    }

    @PostMapping("/batch/agregar-log")
    public Object agregarLogPrograma(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — BAT.Log not yet wired");
    }

    @PostMapping("/batch/inscribir-proceso")
    public Object inscribirProcesoBatch(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — BAT.Proceso not yet wired");
    }

    @PostMapping("/batch/reprocesar-programa")
    public Object reprocesarPrograma(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — BAT.Programa not yet wired");
    }

    @PostMapping("/batch/consultar-tipo-programa-en-proceso")
    public Object consultarTipoProgramaEnProceso(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — BAT.Programa not yet wired");
    }

    // -----------------------------------------------------------------------
    // BCK — Blockchain
    // -----------------------------------------------------------------------

    @PostMapping("/blockchain/login")
    public Object loginBlockchain(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires Blockchain integration not yet wired");
    }

    // -----------------------------------------------------------------------
    // SAP — SAP Integration
    // -----------------------------------------------------------------------

    @PostMapping("/sap/integrar")
    public Object integrarSAP(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires SAP integration not yet wired");
    }

    // -----------------------------------------------------------------------
    // FTP — File Transfer
    // -----------------------------------------------------------------------

    @PostMapping("/ftp/transferir")
    public Object transferirFTP(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires FTP integration not yet wired");
    }
}
