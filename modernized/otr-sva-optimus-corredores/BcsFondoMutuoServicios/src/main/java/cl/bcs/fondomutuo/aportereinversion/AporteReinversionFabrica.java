package cl.bcs.fondomutuo.aportereinversion;

import cl.bcs.fondomutuo.agf.AgfDatos;
import cl.bcs.fondomutuo.agf.CrearSolicitudAgfService;
import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.cuotas.CuotasPort;
import cl.bcs.fondomutuo.custodia.ConsultarFlujoCustodiaFondoMutuoService;
import cl.bcs.fondomutuo.custodia.FlujoCustodiaFmDatos;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.fechas.FechasPort;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.normal.ProcesosExternos;
import cl.bcs.fondomutuo.normal.Puertos3A;
import cl.bcs.fondomutuo.patrimonio.PatrimonioEmisorPort;
import cl.bcs.fondomutuo.patrimonio.ValidaPatrimonioEmisorService;
import cl.bcs.fondomutuo.perfil.ObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.perfil.PerfilFondoMutuoPort;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;
import cl.bcs.fondomutuo.port.out.http.FlujoCajaPort;
import cl.bcs.fondomutuo.port.out.http.LogPort;
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
 * Builds the {@code CrearSolicitudAporteReinversion} service with the 3A and 3B services of its closure (nothing is ported again). Used by the equivalence harness and, later,
 * by the Spring configuration of its callers: one place knows how the pieces are wired. Nothing here has state.
 */
public final class AporteReinversionFabrica {

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
        public AporteReinversionDatos datos;
        public PerfilFondoMutuoPort perfilPort;
        public FechasPort fechasPort;
        public CuotasPort cuotasPort;
        public PatrimonioEmisorPort patrimonioPort;
        public FlujoCustodiaFmDatos flujoCustodiaDatos;
        public AgfDatos agfDatos;
        public SecuenciaPort secuencias;
        public ParidadPort paridad;
        public PreciosTasasPort precios;
        public CustodiaPort custodia;
        public FlujoCajaPort flujoCaja;
        public LogPort log;
        public ProductosPortafolioPort productos;
        public ProcesosExternos.TesoreriaPort tesoreria;
        public ProcesosExternos.SpotPort spot;
        public Compuertas compuertas;
    }

    private AporteReinversionFabrica() {
    }

    public static AporteReinversionPort crear(Dependencias d) {
        CalcularFechaSolicitudAporteRescate fechas = new CalcularFechaSolicitudAporteRescate(d.configuracion, d.empresas, d.series, d.fechasPort, d.calendario);
        CalcularMontoNumeroCuota cuotas = new CalcularMontoNumeroCuota(d.configuracion, d.series, d.cuotasPort, d.calendario, d.precios, d.paridad);
        ObtenerPerfilFondoMutuo perfil = new ObtenerPerfilFondoMutuo(d.perfilPort);
        ServicioCumpleRequisitosService requisitos = new ServicioCumpleRequisitosService(d.requisitosDatos, d.diccionario, d.configuracion, d.productos);
        ValidaMontoInversionService montoInversion = new ValidaMontoInversionService(d.configuracion, d.diccionario);
        ValidaPatrimonioEmisorService patrimonio = new ValidaPatrimonioEmisorService(d.patrimonioPort, d.paridad, d.diccionario);
        ConsultarFlujoCustodiaFondoMutuoService custodia = new ConsultarFlujoCustodiaFondoMutuoService(d.flujoCustodiaDatos, d.custodia, Puertos3A.cuotas(cuotas),
                d.calendario, d.compuertas);
        CrearSolicitudAgfService agf = new CrearSolicitudAgfService(d.agfDatos, d.configuracion, d.diccionario);
        return new CrearSolicitudAporteReinversionServicio(d.basicos, d.configuracion, d.empresas, d.diccionario, d.calendario, perfil,
                new AporteReinversionLectura(d.datos, d.series, d.requisitosDatos, d.comisionNnDatos),
                new AporteReinversionCalculos(fechas, cuotas, d.paridad, d.secuencias, d.datos),
                new AporteReinversionControles(requisitos, montoInversion, patrimonio, d.flujoCaja, custodia, d.diccionario, d.compuertas),
                new AporteReinversionEscritura(d.compuertas, d.tesoreria, d.spot, d.datos, agf, d.log), d.secuencias);
    }
}
