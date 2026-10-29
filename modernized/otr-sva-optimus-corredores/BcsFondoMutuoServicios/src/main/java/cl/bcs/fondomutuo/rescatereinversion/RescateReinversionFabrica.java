package cl.bcs.fondomutuo.rescatereinversion;

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
import cl.bcs.fondomutuo.normal.Puertos3A;
import cl.bcs.fondomutuo.patrimonio.PatrimonioEmisorPort;
import cl.bcs.fondomutuo.patrimonio.ValidaPatrimonioEmisorService;
import cl.bcs.fondomutuo.perfil.ObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.perfil.PerfilFondoMutuoPort;
import cl.bcs.fondomutuo.port.out.http.BatchPort;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;
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

/**
 * Builds {@link CrearSolicitudRescateReinversionService} with the 3A/3B services of the F1 closure (frozen; instantiated here as Normal does, no shared
 * state). Used by the Spring configuration and by the equivalence harness (no Spring needed). Nothing here has state.
 */
public final class RescateReinversionFabrica {

    /** Every port the class and its closure need. */
    public static final class Dependencias {
        public RevisarRequisitosBasicosPort basicos;
        public ConfiguracionSistemaPort configuracion;
        public EmpresaPort empresas;
        public DiccionarioMensajesPort diccionario;
        public CalendarioPort calendario;
        public SeriesFondoPort series;
        public RequisitosDatos requisitosDatos;
        public ComisionNnDatos comisionNnDatos;
        public RreDatos datos;
        public BloqueoReinversionPort bloqueo;
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
        public LogPort log;
        public BatchPort batch;
        public ProductosPortafolioPort productos;
        public Compuertas compuertas;
    }

    private RescateReinversionFabrica() {
    }

    public static RescateReinversionPort crear(Dependencias d) {
        CalcularFechaSolicitudAporteRescate fechas = new CalcularFechaSolicitudAporteRescate(d.configuracion, d.empresas, d.series, d.fechasPort, d.calendario);
        CalcularMontoNumeroCuota cuotas = new CalcularMontoNumeroCuota(d.configuracion, d.series, d.cuotasPort, d.calendario, d.precios, d.paridad);
        ObtenerPerfilFondoMutuo perfil = new ObtenerPerfilFondoMutuo(d.perfilPort);
        ServicioCumpleRequisitosService requisitos = new ServicioCumpleRequisitosService(d.requisitosDatos, d.diccionario, d.configuracion, d.productos);
        ValidaPatrimonioEmisorService patrimonio = new ValidaPatrimonioEmisorService(d.patrimonioPort, d.paridad, d.diccionario);
        ConsultarFlujoCustodiaFondoMutuoService custodia = new ConsultarFlujoCustodiaFondoMutuoService(d.flujoCustodiaDatos, d.custodia, Puertos3A.cuotas(cuotas),
                d.calendario, d.compuertas);
        CrearSolicitudAgfService agf = new CrearSolicitudAgfService(d.agfDatos, d.configuracion, d.diccionario);
        return new CrearSolicitudRescateReinversionService(d.basicos, d.configuracion, d.empresas, d.diccionario, perfil,
                new RreLectura(d.series, d.requisitosDatos, d.comisionNnDatos, d.datos), d.bloqueo, new RreFechas(fechas, d.configuracion),
                new RreCuotas(cuotas, d.diccionario), new RreArmado(d.secuencias, d.configuracion, d.calendario),
                new RreValidacion(requisitos, patrimonio, custodia, d.diccionario), new RreEscritura(d.datos, agf, d.log, d.batch, d.diccionario));
    }
}
