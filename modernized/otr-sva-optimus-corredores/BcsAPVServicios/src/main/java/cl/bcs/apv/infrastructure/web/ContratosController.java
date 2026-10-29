package cl.bcs.apv.infrastructure.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cl.bcs.apv.api.casouso.AsignarContratoARecaudacion;
import cl.bcs.apv.api.casouso.AsociarPerfiles;
import cl.bcs.apv.api.casouso.BuscarContratosParaRecaudacion;
import cl.bcs.apv.api.casouso.CambiarContratosAPlanActivo;
import cl.bcs.apv.api.casouso.CambiarEstadoContratos;
import cl.bcs.apv.api.casouso.CambiarEstadoNotificacionContratos;
import cl.bcs.apv.api.casouso.CargarContratosMasivo;
import cl.bcs.apv.api.casouso.ConsultarClienteContrato;
import cl.bcs.apv.api.casouso.ConsultarCustodiaPortafolio;
import cl.bcs.apv.api.casouso.ConsultarPreferenciaPagoPortafolio;
import cl.bcs.apv.api.casouso.CrearContrato;
import cl.bcs.apv.api.casouso.GrabarContratos;
import cl.bcs.apv.api.casouso.ModificarContratos;
import cl.bcs.apv.api.casouso.ModificarRelacionLaboral;
import cl.bcs.apv.api.casouso.ObtenerFolioSuscripcion;
import cl.bcs.apv.api.casouso.RevocarContratos;
import cl.bcs.apv.api.casouso.SolicitarCambioAPlanActivo;
import cl.bcs.apv.api.casouso.ValidarContrato;
import cl.bcs.apv.api.casouso.ValidarYAsignarContratosARecaudaciones;
import cl.bcs.apv.api.dto.asignarcontratoarecaudacion.AsignarContratoARecaudacionEntrada;
import cl.bcs.apv.api.dto.asignarcontratoarecaudacion.AsignarContratoARecaudacionResultado;
import cl.bcs.apv.api.dto.asociarperfiles.AsociarPerfilesEntrada;
import cl.bcs.apv.api.dto.asociarperfiles.AsociarPerfilesResultado;
import cl.bcs.apv.api.dto.buscarcontratospararecaudacion.BuscarContratosParaRecaudacionEntrada;
import cl.bcs.apv.api.dto.buscarcontratospararecaudacion.BuscarContratosParaRecaudacionResultado;
import cl.bcs.apv.api.dto.cambiarcontratosaplanactivo.CambiarContratosAPlanActivoEntrada;
import cl.bcs.apv.api.dto.cambiarcontratosaplanactivo.CambiarContratosAPlanActivoResultado;
import cl.bcs.apv.api.dto.cambiarestadocontratos.CambiarEstadoContratosEntrada;
import cl.bcs.apv.api.dto.cambiarestadocontratos.CambiarEstadoContratosResultado;
import cl.bcs.apv.api.dto.cambiarestadonotificacioncontratos.CambiarEstadoNotificacionContratosEntrada;
import cl.bcs.apv.api.dto.cambiarestadonotificacioncontratos.CambiarEstadoNotificacionContratosResultado;
import cl.bcs.apv.api.dto.cargarcontratosmasivo.CargarContratosMasivoEntrada;
import cl.bcs.apv.api.dto.cargarcontratosmasivo.CargarContratosMasivoResultado;
import cl.bcs.apv.api.dto.consultarclientecontrato.ConsultarClienteContratoEntrada;
import cl.bcs.apv.api.dto.consultarclientecontrato.ConsultarClienteContratoResultado;
import cl.bcs.apv.api.dto.consultarcustodiaportafolio.ConsultarCustodiaPortafolioEntrada;
import cl.bcs.apv.api.dto.consultarcustodiaportafolio.ConsultarCustodiaPortafolioResultado;
import cl.bcs.apv.api.dto.consultarpreferenciapagoportafolio.ConsultarPreferenciaPagoPortafolioEntrada;
import cl.bcs.apv.api.dto.consultarpreferenciapagoportafolio.ConsultarPreferenciaPagoPortafolioResultado;
import cl.bcs.apv.api.dto.crearcontrato.CrearContratoEntrada;
import cl.bcs.apv.api.dto.crearcontrato.CrearContratoResultado;
import cl.bcs.apv.api.dto.grabarcontratos.GrabarContratosEntrada;
import cl.bcs.apv.api.dto.grabarcontratos.GrabarContratosResultado;
import cl.bcs.apv.api.dto.modificarcontratos.ModificarContratosEntrada;
import cl.bcs.apv.api.dto.modificarcontratos.ModificarContratosResultado;
import cl.bcs.apv.api.dto.modificarrelacionlaboral.ModificarRelacionLaboralEntrada;
import cl.bcs.apv.api.dto.modificarrelacionlaboral.ModificarRelacionLaboralResultado;
import cl.bcs.apv.api.dto.obtenerfoliosuscripcion.ObtenerFolioSuscripcionEntrada;
import cl.bcs.apv.api.dto.obtenerfoliosuscripcion.ObtenerFolioSuscripcionResultado;
import cl.bcs.apv.api.dto.revocarcontratos.RevocarContratosEntrada;
import cl.bcs.apv.api.dto.revocarcontratos.RevocarContratosResultado;
import cl.bcs.apv.api.dto.solicitarcambioaplanactivo.SolicitarCambioAPlanActivoEntrada;
import cl.bcs.apv.api.dto.solicitarcambioaplanactivo.SolicitarCambioAPlanActivoResultado;
import cl.bcs.apv.api.dto.validarcontrato.ValidarContratoEntrada;
import cl.bcs.apv.api.dto.validarcontrato.ValidarContratoResultado;
import cl.bcs.apv.api.dto.validaryasignarcontratosarecaudaciones.ValidarYAsignarContratosARecaudacionesEntrada;
import cl.bcs.apv.api.dto.validaryasignarcontratosarecaudaciones.ValidarYAsignarContratosARecaudacionesResultado;

/**
 * Typed REST adapter of the use cases of family A (19 operations): one method per use case, the route and the verb come from the registry of
 * {@code bcs-apv-javacall-adapter} (the single catalogue {@code BcsAPVApi/tools/catalogo.py}); the body is the typed Entrada record and the answer the typed
 * Resultado record (JSON of the typed contract, NOT the legacy JavaCall JSON, which the adapter library produces for the gateway). Every path is protected by
 * {@code TokenInternoFilter} (allow-list); errors are mapped in one place by {@code ApvExceptionHandler}. Consistency with the registry is proved by
 * {@code RestRegistroConsistenciaTest}. GENERATED by scratchpad gen_controller.py from the registry: edit by hand from here on.
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ContratosController {

    private final AsignarContratoARecaudacion asignarContratoARecaudacion;
    private final AsociarPerfiles asociarPerfiles;
    private final BuscarContratosParaRecaudacion buscarContratosParaRecaudacion;
    private final CambiarContratosAPlanActivo cambiarContratosAPlanActivo;
    private final SolicitarCambioAPlanActivo solicitarCambioAPlanActivo;
    private final CambiarEstadoContratos cambiarEstadoContratos;
    private final CambiarEstadoNotificacionContratos cambiarEstadoNotificacionContratos;
    private final RevocarContratos revocarContratos;
    private final CargarContratosMasivo cargarContratosMasivo;
    private final ConsultarClienteContrato consultarClienteContrato;
    private final ConsultarCustodiaPortafolio consultarCustodiaPortafolio;
    private final ConsultarPreferenciaPagoPortafolio consultarPreferenciaPagoPortafolio;
    private final CrearContrato crearContrato;
    private final GrabarContratos grabarContratos;
    private final ModificarContratos modificarContratos;
    private final ModificarRelacionLaboral modificarRelacionLaboral;
    private final ObtenerFolioSuscripcion obtenerFolioSuscripcion;
    private final ValidarContrato validarContrato;
    private final ValidarYAsignarContratosARecaudaciones validarYAsignarContratosARecaudaciones;

    public ContratosController(AsignarContratoARecaudacion asignarContratoARecaudacion,
            AsociarPerfiles asociarPerfiles,
            BuscarContratosParaRecaudacion buscarContratosParaRecaudacion,
            CambiarContratosAPlanActivo cambiarContratosAPlanActivo,
            SolicitarCambioAPlanActivo solicitarCambioAPlanActivo,
            CambiarEstadoContratos cambiarEstadoContratos,
            CambiarEstadoNotificacionContratos cambiarEstadoNotificacionContratos,
            RevocarContratos revocarContratos,
            CargarContratosMasivo cargarContratosMasivo,
            ConsultarClienteContrato consultarClienteContrato,
            ConsultarCustodiaPortafolio consultarCustodiaPortafolio,
            ConsultarPreferenciaPagoPortafolio consultarPreferenciaPagoPortafolio,
            CrearContrato crearContrato,
            GrabarContratos grabarContratos,
            ModificarContratos modificarContratos,
            ModificarRelacionLaboral modificarRelacionLaboral,
            ObtenerFolioSuscripcion obtenerFolioSuscripcion,
            ValidarContrato validarContrato,
            ValidarYAsignarContratosARecaudaciones validarYAsignarContratosARecaudaciones) {
        this.asignarContratoARecaudacion = asignarContratoARecaudacion;
        this.asociarPerfiles = asociarPerfiles;
        this.buscarContratosParaRecaudacion = buscarContratosParaRecaudacion;
        this.cambiarContratosAPlanActivo = cambiarContratosAPlanActivo;
        this.solicitarCambioAPlanActivo = solicitarCambioAPlanActivo;
        this.cambiarEstadoContratos = cambiarEstadoContratos;
        this.cambiarEstadoNotificacionContratos = cambiarEstadoNotificacionContratos;
        this.revocarContratos = revocarContratos;
        this.cargarContratosMasivo = cargarContratosMasivo;
        this.consultarClienteContrato = consultarClienteContrato;
        this.consultarCustodiaPortafolio = consultarCustodiaPortafolio;
        this.consultarPreferenciaPagoPortafolio = consultarPreferenciaPagoPortafolio;
        this.crearContrato = crearContrato;
        this.grabarContratos = grabarContratos;
        this.modificarContratos = modificarContratos;
        this.modificarRelacionLaboral = modificarRelacionLaboral;
        this.obtenerFolioSuscripcion = obtenerFolioSuscripcion;
        this.validarContrato = validarContrato;
        this.validarYAsignarContratosARecaudaciones = validarYAsignarContratosARecaudaciones;
    }

    /** Legacy cl.bcs.APV.AsignarContratoARecaudacion. */
    @PostMapping("/apv/recaudaciones/asignacion-contrato")
    public AsignarContratoARecaudacionResultado asignarContratoARecaudacion(@RequestBody AsignarContratoARecaudacionEntrada entrada) {
        return asignarContratoARecaudacion.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.AsociarPerfilesAPV. */
    @PostMapping("/apv/perfiles/asociacion")
    public AsociarPerfilesResultado asociarPerfiles(@RequestBody AsociarPerfilesEntrada entrada) {
        return asociarPerfiles.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.BusquedaContratoSAAPVRecaudacion. */
    @PostMapping("/apv/contratos/busqueda-recaudacion")
    public BuscarContratosParaRecaudacionResultado buscarContratosParaRecaudacion(@RequestBody BuscarContratosParaRecaudacionEntrada entrada) {
        return buscarContratosParaRecaudacion.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CambiarAPlanActivoContratoSAAPV. */
    @PostMapping("/apv/contratos/plan-activo")
    public CambiarContratosAPlanActivoResultado cambiarContratosAPlanActivo(@RequestBody CambiarContratosAPlanActivoEntrada entrada) {
        return cambiarContratosAPlanActivo.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CambiarAPlanActivoContratoSAAPVWrapper. */
    @PostMapping("/apv/contratos/plan-activo/solicitud")
    public SolicitarCambioAPlanActivoResultado solicitarCambioAPlanActivo(@RequestBody SolicitarCambioAPlanActivoEntrada entrada) {
        return solicitarCambioAPlanActivo.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CambiarEstadoContratosSAAPV. */
    @PostMapping("/apv/contratos/cambio-estado")
    public CambiarEstadoContratosResultado cambiarEstadoContratos(@RequestBody CambiarEstadoContratosEntrada entrada) {
        return cambiarEstadoContratos.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CambiarEstadoEnvioNotificacionContratoSAAPV. */
    @PostMapping("/apv/contratos/notificacion/cambio-estado")
    public CambiarEstadoNotificacionContratosResultado cambiarEstadoNotificacionContratos(@RequestBody CambiarEstadoNotificacionContratosEntrada entrada) {
        return cambiarEstadoNotificacionContratos.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CambiarEstadoRevocacionSAAPV. */
    @PostMapping("/apv/contratos/revocacion")
    public RevocarContratosResultado revocarContratos(@RequestBody RevocarContratosEntrada entrada) {
        return revocarContratos.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CargaMasivaArchivo. */
    @PostMapping("/apv/contratos/carga-masiva")
    public CargarContratosMasivoResultado cargarContratosMasivo(@RequestBody CargarContratosMasivoEntrada entrada) {
        return cargarContratosMasivo.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.ConsultarClienteContratoSAAPV. */
    @PostMapping("/apv/contratos/cliente")
    public ConsultarClienteContratoResultado consultarClienteContrato(@RequestBody ConsultarClienteContratoEntrada entrada) {
        return consultarClienteContrato.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.ConsultarCustodiaPortafolioAPV. */
    @GetMapping("/apv/portafolios/custodia")
    public ConsultarCustodiaPortafolioResultado consultarCustodiaPortafolio(ConsultarCustodiaPortafolioEntrada entrada) {
        return consultarCustodiaPortafolio.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.ConsultarPreferenciaPagoPortafolio. */
    @GetMapping("/apv/portafolios/preferencia-pago")
    public ConsultarPreferenciaPagoPortafolioResultado consultarPreferenciaPagoPortafolio(ConsultarPreferenciaPagoPortafolioEntrada entrada) {
        return consultarPreferenciaPagoPortafolio.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CrearContratoSAAPV. */
    @PostMapping("/apv/contratos")
    public CrearContratoResultado crearContrato(@RequestBody CrearContratoEntrada entrada) {
        return crearContrato.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.GrabarContratoSAAPV. */
    @PostMapping("/apv/contratos/grabacion")
    public GrabarContratosResultado grabarContratos(@RequestBody GrabarContratosEntrada entrada) {
        return grabarContratos.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.ModificarContratoSAAPV. */
    @PutMapping("/apv/contratos")
    public ModificarContratosResultado modificarContratos(@RequestBody ModificarContratosEntrada entrada) {
        return modificarContratos.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.ModificarRelacionLaboral. */
    @PutMapping("/apv/contratos/relacion-laboral")
    public ModificarRelacionLaboralResultado modificarRelacionLaboral(@RequestBody ModificarRelacionLaboralEntrada entrada) {
        return modificarRelacionLaboral.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.ObtenerFolioSuscripcion. */
    @PostMapping("/apv/contratos/folios-suscripcion")
    public ObtenerFolioSuscripcionResultado obtenerFolioSuscripcion(@RequestBody ObtenerFolioSuscripcionEntrada entrada) {
        return obtenerFolioSuscripcion.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.ValidaContratoSAAPV. */
    @PostMapping("/apv/contratos/validacion")
    public ValidarContratoResultado validarContrato(@RequestBody ValidarContratoEntrada entrada) {
        return validarContrato.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.ValidarYAsignarContratosARecaudaciones. */
    @PostMapping("/apv/recaudaciones/validacion-asignacion-contratos")
    public ValidarYAsignarContratosARecaudacionesResultado validarYAsignarContratosARecaudaciones(@RequestBody ValidarYAsignarContratosARecaudacionesEntrada entrada) {
        return validarYAsignarContratosARecaudaciones.ejecutar(entrada);
    }
}
