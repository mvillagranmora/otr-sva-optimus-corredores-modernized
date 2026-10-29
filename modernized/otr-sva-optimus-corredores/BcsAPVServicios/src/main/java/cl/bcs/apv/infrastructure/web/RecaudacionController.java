package cl.bcs.apv.infrastructure.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cl.bcs.apv.api.casouso.AsignarDepositoEmpleador;
import cl.bcs.apv.api.casouso.CambiarEstadoRecaudacion;
import cl.bcs.apv.api.casouso.CargarArchivoBanco;
import cl.bcs.apv.api.casouso.CargarArchivoPrevired;
import cl.bcs.apv.api.casouso.CargarBonificacionTGR;
import cl.bcs.apv.api.casouso.CargarContratosPAC;
import cl.bcs.apv.api.casouso.CargarMasivaRecaudacion;
import cl.bcs.apv.api.casouso.CargarPAC;
import cl.bcs.apv.api.casouso.CargarRecaudacion;
import cl.bcs.apv.api.casouso.ConciliarDeposito;
import cl.bcs.apv.api.casouso.ConsultarTotalesDistribucion;
import cl.bcs.apv.api.casouso.ConsultarTotalesOrigenPorDeposito;
import cl.bcs.apv.api.casouso.CrearAnotacionRecaudacion;
import cl.bcs.apv.api.casouso.CrearBancoPrevired;
import cl.bcs.apv.api.casouso.CrearRecaudacionCliente;
import cl.bcs.apv.api.casouso.CrearRecaudacionDeposito;
import cl.bcs.apv.api.casouso.CrearRecaudacionEmpleador;
import cl.bcs.apv.api.casouso.DevolverBonificacionEntidadRecaudadora;
import cl.bcs.apv.api.casouso.DevolverPagoRezagos;
import cl.bcs.apv.api.casouso.EjecutarBonificacion;
import cl.bcs.apv.api.casouso.EliminarBancoPrevired;
import cl.bcs.apv.api.casouso.EliminarRegistrosRecaudacion;
import cl.bcs.apv.api.casouso.ModificarAnotacionRecaudacion;
import cl.bcs.apv.api.casouso.ModificarBancoPrevired;
import cl.bcs.apv.api.casouso.ModificarBonificacion;
import cl.bcs.apv.api.casouso.ProcesarBonificacion;
import cl.bcs.apv.api.casouso.ProcesarCargaMasivaRecaudacion;
import cl.bcs.apv.api.casouso.ProcesarDistribucion;
import cl.bcs.apv.api.casouso.ReplicarPortafolio;
import cl.bcs.apv.api.dto.asignardepositoempleador.AsignarDepositoEmpleadorEntrada;
import cl.bcs.apv.api.dto.asignardepositoempleador.AsignarDepositoEmpleadorResultado;
import cl.bcs.apv.api.dto.cambiarestadorecaudacion.CambiarEstadoRecaudacionEntrada;
import cl.bcs.apv.api.dto.cambiarestadorecaudacion.CambiarEstadoRecaudacionResultado;
import cl.bcs.apv.api.dto.cargararchivobanco.CargarArchivoBancoEntrada;
import cl.bcs.apv.api.dto.cargararchivobanco.CargarArchivoBancoResultado;
import cl.bcs.apv.api.dto.cargararchivoprevired.CargarArchivoPreviredEntrada;
import cl.bcs.apv.api.dto.cargararchivoprevired.CargarArchivoPreviredResultado;
import cl.bcs.apv.api.dto.cargarbonificaciontgr.CargarBonificacionTGREntrada;
import cl.bcs.apv.api.dto.cargarbonificaciontgr.CargarBonificacionTGRResultado;
import cl.bcs.apv.api.dto.cargarcontratospac.CargarContratosPACEntrada;
import cl.bcs.apv.api.dto.cargarcontratospac.CargarContratosPACResultado;
import cl.bcs.apv.api.dto.cargarmasivarecaudacion.CargarMasivaRecaudacionEntrada;
import cl.bcs.apv.api.dto.cargarmasivarecaudacion.CargarMasivaRecaudacionResultado;
import cl.bcs.apv.api.dto.cargarpac.CargarPACEntrada;
import cl.bcs.apv.api.dto.cargarpac.CargarPACResultado;
import cl.bcs.apv.api.dto.cargarrecaudacion.CargarRecaudacionEntrada;
import cl.bcs.apv.api.dto.cargarrecaudacion.CargarRecaudacionResultado;
import cl.bcs.apv.api.dto.conciliardeposito.ConciliarDepositoEntrada;
import cl.bcs.apv.api.dto.conciliardeposito.ConciliarDepositoResultado;
import cl.bcs.apv.api.dto.consultartotalesdistribucion.ConsultarTotalesDistribucionEntrada;
import cl.bcs.apv.api.dto.consultartotalesdistribucion.ConsultarTotalesDistribucionResultado;
import cl.bcs.apv.api.dto.consultartotalesorigenpordeposito.ConsultarTotalesOrigenPorDepositoEntrada;
import cl.bcs.apv.api.dto.consultartotalesorigenpordeposito.ConsultarTotalesOrigenPorDepositoResultado;
import cl.bcs.apv.api.dto.crearanotacionrecaudacion.CrearAnotacionRecaudacionEntrada;
import cl.bcs.apv.api.dto.crearanotacionrecaudacion.CrearAnotacionRecaudacionResultado;
import cl.bcs.apv.api.dto.crearbancoprevired.CrearBancoPreviredEntrada;
import cl.bcs.apv.api.dto.crearbancoprevired.CrearBancoPreviredResultado;
import cl.bcs.apv.api.dto.crearrecaudacioncliente.CrearRecaudacionClienteEntrada;
import cl.bcs.apv.api.dto.crearrecaudacioncliente.CrearRecaudacionClienteResultado;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.CrearRecaudacionDepositoEntrada;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.CrearRecaudacionDepositoResultado;
import cl.bcs.apv.api.dto.crearrecaudacionempleador.CrearRecaudacionEmpleadorEntrada;
import cl.bcs.apv.api.dto.crearrecaudacionempleador.CrearRecaudacionEmpleadorResultado;
import cl.bcs.apv.api.dto.devolverbonificacionentidadrecaudadora.DevolverBonificacionEntidadRecaudadoraEntrada;
import cl.bcs.apv.api.dto.devolverbonificacionentidadrecaudadora.DevolverBonificacionEntidadRecaudadoraResultado;
import cl.bcs.apv.api.dto.devolverpagorezagos.DevolverPagoRezagosEntrada;
import cl.bcs.apv.api.dto.devolverpagorezagos.DevolverPagoRezagosResultado;
import cl.bcs.apv.api.dto.ejecutarbonificacion.EjecutarBonificacionEntrada;
import cl.bcs.apv.api.dto.ejecutarbonificacion.EjecutarBonificacionResultado;
import cl.bcs.apv.api.dto.eliminarbancoprevired.EliminarBancoPreviredEntrada;
import cl.bcs.apv.api.dto.eliminarbancoprevired.EliminarBancoPreviredResultado;
import cl.bcs.apv.api.dto.eliminarregistrosrecaudacion.EliminarRegistrosRecaudacionEntrada;
import cl.bcs.apv.api.dto.eliminarregistrosrecaudacion.EliminarRegistrosRecaudacionResultado;
import cl.bcs.apv.api.dto.modificaranotacionrecaudacion.ModificarAnotacionRecaudacionEntrada;
import cl.bcs.apv.api.dto.modificaranotacionrecaudacion.ModificarAnotacionRecaudacionResultado;
import cl.bcs.apv.api.dto.modificarbancoprevired.ModificarBancoPreviredEntrada;
import cl.bcs.apv.api.dto.modificarbancoprevired.ModificarBancoPreviredResultado;
import cl.bcs.apv.api.dto.modificarbonificacion.ModificarBonificacionEntrada;
import cl.bcs.apv.api.dto.modificarbonificacion.ModificarBonificacionResultado;
import cl.bcs.apv.api.dto.procesarbonificacion.ProcesarBonificacionEntrada;
import cl.bcs.apv.api.dto.procesarbonificacion.ProcesarBonificacionResultado;
import cl.bcs.apv.api.dto.procesarcargamasivarecaudacion.ProcesarCargaMasivaRecaudacionEntrada;
import cl.bcs.apv.api.dto.procesarcargamasivarecaudacion.ProcesarCargaMasivaRecaudacionResultado;
import cl.bcs.apv.api.dto.procesardistribucion.ProcesarDistribucionEntrada;
import cl.bcs.apv.api.dto.procesardistribucion.ProcesarDistribucionResultado;
import cl.bcs.apv.api.dto.replicarportafolio.ReplicarPortafolioEntrada;
import cl.bcs.apv.api.dto.replicarportafolio.ReplicarPortafolioResultado;

/**
 * Typed REST adapter of the use cases of family C (29 operations): one method per use case, the route and the verb come from the registry of
 * {@code bcs-apv-javacall-adapter} (the single catalogue {@code BcsAPVApi/tools/catalogo.py}); the body is the typed Entrada record and the answer the typed
 * Resultado record (JSON of the typed contract, NOT the legacy JavaCall JSON, which the adapter library produces for the gateway). Every path is protected by
 * {@code TokenInternoFilter} (allow-list); errors are mapped in one place by {@code ApvExceptionHandler}. Consistency with the registry is proved by
 * {@code RestRegistroConsistenciaTest}. GENERATED by scratchpad gen_controller.py from the registry: edit by hand from here on.
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class RecaudacionController {

    private final AsignarDepositoEmpleador asignarDepositoEmpleador;
    private final CambiarEstadoRecaudacion cambiarEstadoRecaudacion;
    private final CargarBonificacionTGR cargarBonificacionTGR;
    private final CargarArchivoBanco cargarArchivoBanco;
    private final CargarPAC cargarPAC;
    private final CargarArchivoPrevired cargarArchivoPrevired;
    private final CargarMasivaRecaudacion cargarMasivaRecaudacion;
    private final CargarContratosPAC cargarContratosPAC;
    private final CargarRecaudacion cargarRecaudacion;
    private final ConciliarDeposito conciliarDeposito;
    private final ConsultarTotalesDistribucion consultarTotalesDistribucion;
    private final ConsultarTotalesOrigenPorDeposito consultarTotalesOrigenPorDeposito;
    private final CrearAnotacionRecaudacion crearAnotacionRecaudacion;
    private final CrearBancoPrevired crearBancoPrevired;
    private final CrearRecaudacionCliente crearRecaudacionCliente;
    private final CrearRecaudacionDeposito crearRecaudacionDeposito;
    private final CrearRecaudacionEmpleador crearRecaudacionEmpleador;
    private final DevolverBonificacionEntidadRecaudadora devolverBonificacionEntidadRecaudadora;
    private final DevolverPagoRezagos devolverPagoRezagos;
    private final EjecutarBonificacion ejecutarBonificacion;
    private final EliminarBancoPrevired eliminarBancoPrevired;
    private final EliminarRegistrosRecaudacion eliminarRegistrosRecaudacion;
    private final ModificarAnotacionRecaudacion modificarAnotacionRecaudacion;
    private final ModificarBancoPrevired modificarBancoPrevired;
    private final ModificarBonificacion modificarBonificacion;
    private final ProcesarBonificacion procesarBonificacion;
    private final ProcesarCargaMasivaRecaudacion procesarCargaMasivaRecaudacion;
    private final ProcesarDistribucion procesarDistribucion;
    private final ReplicarPortafolio replicarPortafolio;

    public RecaudacionController(AsignarDepositoEmpleador asignarDepositoEmpleador,
            CambiarEstadoRecaudacion cambiarEstadoRecaudacion,
            CargarBonificacionTGR cargarBonificacionTGR,
            CargarArchivoBanco cargarArchivoBanco,
            CargarPAC cargarPAC,
            CargarArchivoPrevired cargarArchivoPrevired,
            CargarMasivaRecaudacion cargarMasivaRecaudacion,
            CargarContratosPAC cargarContratosPAC,
            CargarRecaudacion cargarRecaudacion,
            ConciliarDeposito conciliarDeposito,
            ConsultarTotalesDistribucion consultarTotalesDistribucion,
            ConsultarTotalesOrigenPorDeposito consultarTotalesOrigenPorDeposito,
            CrearAnotacionRecaudacion crearAnotacionRecaudacion,
            CrearBancoPrevired crearBancoPrevired,
            CrearRecaudacionCliente crearRecaudacionCliente,
            CrearRecaudacionDeposito crearRecaudacionDeposito,
            CrearRecaudacionEmpleador crearRecaudacionEmpleador,
            DevolverBonificacionEntidadRecaudadora devolverBonificacionEntidadRecaudadora,
            DevolverPagoRezagos devolverPagoRezagos,
            EjecutarBonificacion ejecutarBonificacion,
            EliminarBancoPrevired eliminarBancoPrevired,
            EliminarRegistrosRecaudacion eliminarRegistrosRecaudacion,
            ModificarAnotacionRecaudacion modificarAnotacionRecaudacion,
            ModificarBancoPrevired modificarBancoPrevired,
            ModificarBonificacion modificarBonificacion,
            ProcesarBonificacion procesarBonificacion,
            ProcesarCargaMasivaRecaudacion procesarCargaMasivaRecaudacion,
            ProcesarDistribucion procesarDistribucion,
            ReplicarPortafolio replicarPortafolio) {
        this.asignarDepositoEmpleador = asignarDepositoEmpleador;
        this.cambiarEstadoRecaudacion = cambiarEstadoRecaudacion;
        this.cargarBonificacionTGR = cargarBonificacionTGR;
        this.cargarArchivoBanco = cargarArchivoBanco;
        this.cargarPAC = cargarPAC;
        this.cargarArchivoPrevired = cargarArchivoPrevired;
        this.cargarMasivaRecaudacion = cargarMasivaRecaudacion;
        this.cargarContratosPAC = cargarContratosPAC;
        this.cargarRecaudacion = cargarRecaudacion;
        this.conciliarDeposito = conciliarDeposito;
        this.consultarTotalesDistribucion = consultarTotalesDistribucion;
        this.consultarTotalesOrigenPorDeposito = consultarTotalesOrigenPorDeposito;
        this.crearAnotacionRecaudacion = crearAnotacionRecaudacion;
        this.crearBancoPrevired = crearBancoPrevired;
        this.crearRecaudacionCliente = crearRecaudacionCliente;
        this.crearRecaudacionDeposito = crearRecaudacionDeposito;
        this.crearRecaudacionEmpleador = crearRecaudacionEmpleador;
        this.devolverBonificacionEntidadRecaudadora = devolverBonificacionEntidadRecaudadora;
        this.devolverPagoRezagos = devolverPagoRezagos;
        this.ejecutarBonificacion = ejecutarBonificacion;
        this.eliminarBancoPrevired = eliminarBancoPrevired;
        this.eliminarRegistrosRecaudacion = eliminarRegistrosRecaudacion;
        this.modificarAnotacionRecaudacion = modificarAnotacionRecaudacion;
        this.modificarBancoPrevired = modificarBancoPrevired;
        this.modificarBonificacion = modificarBonificacion;
        this.procesarBonificacion = procesarBonificacion;
        this.procesarCargaMasivaRecaudacion = procesarCargaMasivaRecaudacion;
        this.procesarDistribucion = procesarDistribucion;
        this.replicarPortafolio = replicarPortafolio;
    }

    /** Legacy cl.bcs.APV.AsignarDepositoEmpleador. */
    @PostMapping("/apv/recaudaciones/depositos/asignacion-empleador")
    public AsignarDepositoEmpleadorResultado asignarDepositoEmpleador(@RequestBody AsignarDepositoEmpleadorEntrada entrada) {
        return asignarDepositoEmpleador.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CambiarEstadoRecaudacion. */
    @PostMapping("/apv/recaudaciones/cambio-estado")
    public CambiarEstadoRecaudacionResultado cambiarEstadoRecaudacion(@RequestBody CambiarEstadoRecaudacionEntrada entrada) {
        return cambiarEstadoRecaudacion.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CargaBonificacionTGR. */
    @PostMapping("/apv/bonificaciones/carga-tgr")
    public CargarBonificacionTGRResultado cargarBonificacionTGR(@RequestBody CargarBonificacionTGREntrada entrada) {
        return cargarBonificacionTGR.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CargaMasivaBanco. */
    @PostMapping("/apv/recaudaciones/carga-masiva/banco")
    public CargarArchivoBancoResultado cargarArchivoBanco(@RequestBody CargarArchivoBancoEntrada entrada) {
        return cargarArchivoBanco.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CargaMasivaPAC. */
    @PostMapping("/apv/pac/carga-masiva")
    public CargarPACResultado cargarPAC(@RequestBody CargarPACEntrada entrada) {
        return cargarPAC.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CargaMasivaPrevired. */
    @PostMapping("/apv/recaudaciones/carga-masiva/previred")
    public CargarArchivoPreviredResultado cargarArchivoPrevired(@RequestBody CargarArchivoPreviredEntrada entrada) {
        return cargarArchivoPrevired.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CargaMasivaRecaudacion. */
    @PostMapping("/apv/recaudaciones/carga-masiva")
    public CargarMasivaRecaudacionResultado cargarMasivaRecaudacion(@RequestBody CargarMasivaRecaudacionEntrada entrada) {
        return cargarMasivaRecaudacion.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CargaMasivaSAAPVPAC. */
    @PostMapping("/apv/pac/contratos/carga-masiva")
    public CargarContratosPACResultado cargarContratosPAC(@RequestBody CargarContratosPACEntrada entrada) {
        return cargarContratosPAC.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CargaRecaudacion. */
    @PostMapping("/apv/recaudaciones/carga")
    public CargarRecaudacionResultado cargarRecaudacion(@RequestBody CargarRecaudacionEntrada entrada) {
        return cargarRecaudacion.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.ConciliarDeposito. */
    @PostMapping("/apv/recaudaciones/depositos/conciliacion")
    public ConciliarDepositoResultado conciliarDeposito(@RequestBody ConciliarDepositoEntrada entrada) {
        return conciliarDeposito.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.ConsultarTotalesDistribucionRecaudacion. */
    @GetMapping("/apv/recaudaciones/distribucion/totales")
    public ConsultarTotalesDistribucionResultado consultarTotalesDistribucion(ConsultarTotalesDistribucionEntrada entrada) {
        return consultarTotalesDistribucion.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.ConsultarTotalesOrigenPorDeposito. */
    @GetMapping("/apv/recaudaciones/depositos/totales-origen")
    public ConsultarTotalesOrigenPorDepositoResultado consultarTotalesOrigenPorDeposito(ConsultarTotalesOrigenPorDepositoEntrada entrada) {
        return consultarTotalesOrigenPorDeposito.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CrearAnotacionRecaudacion. */
    @PostMapping("/apv/recaudaciones/anotaciones")
    public CrearAnotacionRecaudacionResultado crearAnotacionRecaudacion(@RequestBody CrearAnotacionRecaudacionEntrada entrada) {
        return crearAnotacionRecaudacion.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CrearBancoPrevired. */
    @PostMapping("/apv/bancos-previred")
    public CrearBancoPreviredResultado crearBancoPrevired(@RequestBody CrearBancoPreviredEntrada entrada) {
        return crearBancoPrevired.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CrearRecaudacionCliente. */
    @PostMapping("/apv/recaudaciones/clientes")
    public CrearRecaudacionClienteResultado crearRecaudacionCliente(@RequestBody CrearRecaudacionClienteEntrada entrada) {
        return crearRecaudacionCliente.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CrearRecaudacionDeposito. */
    @PostMapping("/apv/recaudaciones/depositos")
    public CrearRecaudacionDepositoResultado crearRecaudacionDeposito(@RequestBody CrearRecaudacionDepositoEntrada entrada) {
        return crearRecaudacionDeposito.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.CrearRecaudacionEmpleador. */
    @PostMapping("/apv/recaudaciones/empleadores")
    public CrearRecaudacionEmpleadorResultado crearRecaudacionEmpleador(@RequestBody CrearRecaudacionEmpleadorEntrada entrada) {
        return crearRecaudacionEmpleador.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.DevolucionBF_EntidadRecaudadora. */
    @PostMapping("/apv/bonificaciones/devolucion-entidad-recaudadora")
    public DevolverBonificacionEntidadRecaudadoraResultado devolverBonificacionEntidadRecaudadora(@RequestBody DevolverBonificacionEntidadRecaudadoraEntrada entrada) {
        return devolverBonificacionEntidadRecaudadora.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.DevolucionPagoRezagos. */
    @PostMapping("/apv/recaudaciones/devoluciones/rezagos")
    public DevolverPagoRezagosResultado devolverPagoRezagos(@RequestBody DevolverPagoRezagosEntrada entrada) {
        return devolverPagoRezagos.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.EjecutarBonificacion. */
    @PostMapping("/apv/bonificaciones/ejecucion")
    public EjecutarBonificacionResultado ejecutarBonificacion(@RequestBody EjecutarBonificacionEntrada entrada) {
        return ejecutarBonificacion.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.EliminarBancoPrevired. */
    @PostMapping("/apv/bancos-previred/eliminacion")
    public EliminarBancoPreviredResultado eliminarBancoPrevired(@RequestBody EliminarBancoPreviredEntrada entrada) {
        return eliminarBancoPrevired.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.EliminarRegistrosRecaudacion. */
    @PostMapping("/apv/recaudaciones/registros/eliminacion")
    public EliminarRegistrosRecaudacionResultado eliminarRegistrosRecaudacion(@RequestBody EliminarRegistrosRecaudacionEntrada entrada) {
        return eliminarRegistrosRecaudacion.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.ModificarAnotacionRecaudacion. */
    @PutMapping("/apv/recaudaciones/anotaciones")
    public ModificarAnotacionRecaudacionResultado modificarAnotacionRecaudacion(@RequestBody ModificarAnotacionRecaudacionEntrada entrada) {
        return modificarAnotacionRecaudacion.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.ModificarBancoPrevired. */
    @PutMapping("/apv/bancos-previred")
    public ModificarBancoPreviredResultado modificarBancoPrevired(@RequestBody ModificarBancoPreviredEntrada entrada) {
        return modificarBancoPrevired.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.ModificarBonificacion. */
    @PutMapping("/apv/bonificaciones")
    public ModificarBonificacionResultado modificarBonificacion(@RequestBody ModificarBonificacionEntrada entrada) {
        return modificarBonificacion.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.ProcesarBonificacion. */
    @PostMapping("/apv/bonificaciones/proceso")
    public ProcesarBonificacionResultado procesarBonificacion(@RequestBody ProcesarBonificacionEntrada entrada) {
        return procesarBonificacion.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.ProcesarCargaMasivaRecaudacion. */
    @PostMapping("/apv/recaudaciones/carga-masiva/proceso")
    public ProcesarCargaMasivaRecaudacionResultado procesarCargaMasivaRecaudacion(@RequestBody ProcesarCargaMasivaRecaudacionEntrada entrada) {
        return procesarCargaMasivaRecaudacion.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.ProcesarDistribucion. */
    @PostMapping("/apv/recaudaciones/distribucion")
    public ProcesarDistribucionResultado procesarDistribucion(@RequestBody ProcesarDistribucionEntrada entrada) {
        return procesarDistribucion.ejecutar(entrada);
    }

    /** Legacy cl.bcs.APV.ReplicaPortafolioAPV. */
    @PostMapping("/apv/portafolios/replica")
    public ReplicarPortafolioResultado replicarPortafolio(@RequestBody ReplicarPortafolioEntrada entrada) {
        return replicarPortafolio.ejecutar(entrada);
    }
}
