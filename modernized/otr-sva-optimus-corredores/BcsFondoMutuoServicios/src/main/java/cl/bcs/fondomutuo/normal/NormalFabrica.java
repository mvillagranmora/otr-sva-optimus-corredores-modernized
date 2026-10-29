package cl.bcs.fondomutuo.normal;

import cl.bcs.fondomutuo.agf.AgfDatos;
import cl.bcs.fondomutuo.agf.CrearSolicitudAgfService;
import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.comisiones.CalcularComisiones;
import cl.bcs.fondomutuo.comisiones.ComisionesPort;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos;
import cl.bcs.fondomutuo.comisionnn.CrearSolicitudRescateComisionNnService;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.cuotas.CuotasPort;
import cl.bcs.fondomutuo.custodia.ConsultarFlujoCustodiaFondoMutuoService;
import cl.bcs.fondomutuo.custodia.FlujoCustodiaFmDatos;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.fechas.FechasPort;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.patrimonio.PatrimonioEmisorPort;
import cl.bcs.fondomutuo.patrimonio.ValidaPatrimonioEmisorService;
import cl.bcs.fondomutuo.perfil.ObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.perfil.PerfilFondoMutuoPort;
import cl.bcs.fondomutuo.port.out.http.BatchPort;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;
import cl.bcs.fondomutuo.port.out.http.FlujoCajaPort;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.port.out.http.NotificacionOwpPort;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;
import cl.bcs.fondomutuo.port.out.http.ProductosPortafolioPort;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;
import cl.bcs.fondomutuo.port.out.kit.RevisarRequisitosBasicosPort;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService;
import cl.bcs.fondomutuo.requisitos.ValidaMontoInversionService;

/**
 * Builds the Normal service with the 3A and 3B services of its closure and binds the typed ports of 3B to the 3A services ({@link Puertos3A}). Used by the
 * Spring configuration and by the equivalence harness (no Spring needed): one place knows how the pieces are wired. Nothing here has state.
 */
public final class NormalFabrica {

    /** Every port the closure needs. */
    public static final class Dependencias {
        public RevisarRequisitosBasicosPort basicos;
        public ConfiguracionSistemaPort configuracion;
        public EmpresaPort empresas;
        public DiccionarioMensajesPort diccionario;
        public CalendarioPort calendario;
        public SeriesFondoPort series;
        public RequisitosDatos requisitosDatos;
        public ComisionNnDatos comisionNnDatos;
        public NormalDatos normalDatos;
        public BloqueoSolicitudPort bloqueo;
        public PerfilFondoMutuoPort perfilPort;
        public FechasPort fechasPort;
        public CuotasPort cuotasPort;
        public ComisionesPort comisionesPort;
        public PatrimonioEmisorPort patrimonioPort;
        public FlujoCustodiaFmDatos flujoCustodiaDatos;
        public AgfDatos agfDatos;
        public SecuenciaPort secuencias;
        public ParidadPort paridad;
        public PreciosTasasPort precios;
        public CustodiaPort custodia;
        /** Port of {@code CalcularComisiones} (H3: the HTTP one unless comisiones-en-proceso); null = {@link #custodia}, as the harnesses build it. */
        public CustodiaPort custodiaComisiones;
        public FlujoCajaPort flujoCaja;
        public LogPort log;
        public BatchPort batch;
        public NotificacionOwpPort owp;
        public ProductosPortafolioPort productos;
        public ProcesosExternos.TesoreriaPort tesoreria;
        public ProcesosExternos.SpotPort spot;
        public Compuertas compuertas;
    }

    private NormalFabrica() {
    }

    public static NormalPort crear(Dependencias d) {
        CalcularFechaSolicitudAporteRescate fechas = new CalcularFechaSolicitudAporteRescate(d.configuracion, d.empresas, d.series, d.fechasPort, d.calendario);
        CalcularMontoNumeroCuota cuotas = new CalcularMontoNumeroCuota(d.configuracion, d.series, d.cuotasPort, d.calendario, d.precios, d.paridad);
        CalcularComisiones comisiones = new CalcularComisiones(d.configuracion, d.empresas, d.series, d.comisionesPort, d.custodiaComisiones != null ? d.custodiaComisiones : d.custodia, d.precios, d.paridad);
        ObtenerPerfilFondoMutuo perfil = new ObtenerPerfilFondoMutuo(d.perfilPort);
        var puertoFechas = Puertos3A.fechas(fechas);
        var puertoCuotas = Puertos3A.cuotas(cuotas);
        ServicioCumpleRequisitosService requisitos = new ServicioCumpleRequisitosService(d.requisitosDatos, d.diccionario, d.configuracion, d.productos);
        ValidaMontoInversionService montoInversion = new ValidaMontoInversionService(d.configuracion, d.diccionario);
        ValidaPatrimonioEmisorService patrimonio = new ValidaPatrimonioEmisorService(d.patrimonioPort, d.paridad, d.diccionario);
        ConsultarFlujoCustodiaFondoMutuoService custodia = new ConsultarFlujoCustodiaFondoMutuoService(d.flujoCustodiaDatos, d.custodia, puertoCuotas,
                d.calendario, d.compuertas);
        CrearSolicitudAgfService agf = new CrearSolicitudAgfService(d.agfDatos, d.configuracion, d.diccionario);
        CrearSolicitudRescateComisionNnService comisionNn = new CrearSolicitudRescateComisionNnService(d.basicos, d.configuracion, d.empresas, d.diccionario,
                d.comisionNnDatos, d.requisitosDatos, puertoFechas, puertoCuotas, d.secuencias, requisitos, d.calendario, d.log);
        return new CrearSolicitudAporteRescateNormalService(d.basicos, d.configuracion, d.empresas, d.diccionario, d.bloqueo, perfil,
                new NormalLectura(d.normalDatos, d.series, d.requisitosDatos, d.comisionNnDatos), new NormalCalculos(fechas, cuotas, comisiones, d.paridad, d.secuencias),
                new NormalValidacion(requisitos, montoInversion, patrimonio), new NormalFlujos(d.flujoCaja, custodia, d.diccionario, d.compuertas),
                new NormalControles(d.normalDatos, d.diccionario), new NormalEscritura(d.compuertas, d.tesoreria, d.spot, d.normalDatos, agf, d.batch, d.log),
                new NormalCierre(comisionNn, d.normalDatos, d.owp), d.secuencias, d.calendario);
    }
}
