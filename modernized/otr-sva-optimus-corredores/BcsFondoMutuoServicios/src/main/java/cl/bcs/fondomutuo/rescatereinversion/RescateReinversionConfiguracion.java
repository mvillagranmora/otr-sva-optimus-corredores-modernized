package cl.bcs.fondomutuo.rescatereinversion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.bcs.fondomutuo.agf.AgfDatos;
import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos;
import cl.bcs.fondomutuo.cuotas.CuotasPort;
import cl.bcs.fondomutuo.custodia.FlujoCustodiaFmDatos;
import cl.bcs.fondomutuo.fechas.FechasPort;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.patrimonio.PatrimonioEmisorPort;
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

/**
 * Spring wiring of {@link RescateReinversionPort}. Being wired does NOT mean being served: both entries (the direct JavaCall FQN and the Manejador list)
 * answer 503 unless {@code fondomutuo.solicitud.listas-servidas} names {@code RESCATE_REINVERSION} (closed by default; the user opens it after PROVEN and
 * sign-off). Even open, gate G-CUS (closed by default) stops every request that reaches the custody flow.
 */
@Configuration
public class RescateReinversionConfiguracion {

    /** Name of the list in {@code fondomutuo.solicitud.listas-servidas} (= {@code ManejadorSolicitudAporteRescateService.Lista.RESCATE_REINVERSION}). */
    public static final String LISTA = "RESCATE_REINVERSION";

    @Bean
    public RescateReinversionPort rescateReinversionPort(RevisarRequisitosBasicosPort basicos, ConfiguracionSistemaPort configuracion, EmpresaPort empresas,
            DiccionarioMensajesPort diccionario, CalendarioPort calendario, SeriesFondoPort series, RequisitosDatos requisitosDatos,
            ComisionNnDatos comisionNnDatos, RreDatos datos, BloqueoReinversionPort bloqueo, PerfilFondoMutuoPort perfilPort, FechasPort fechasPort,
            CuotasPort cuotasPort, PatrimonioEmisorPort patrimonioPort, FlujoCustodiaFmDatos flujoCustodiaDatos, AgfDatos agfDatos, SecuenciaPort secuencias,
            ParidadPort paridad, PreciosTasasPort precios, CustodiaPort custodia, LogPort log, BatchPort batch, ProductosPortafolioPort productos,
            Compuertas compuertas) {
        RescateReinversionFabrica.Dependencias d = new RescateReinversionFabrica.Dependencias();
        d.basicos = basicos;
        d.configuracion = configuracion;
        d.empresas = empresas;
        d.diccionario = diccionario;
        d.calendario = calendario;
        d.series = series;
        d.requisitosDatos = requisitosDatos;
        d.comisionNnDatos = comisionNnDatos;
        d.datos = datos;
        d.bloqueo = bloqueo;
        d.perfilPort = perfilPort;
        d.fechasPort = fechasPort;
        d.cuotasPort = cuotasPort;
        d.patrimonioPort = patrimonioPort;
        d.flujoCustodiaDatos = flujoCustodiaDatos;
        d.agfDatos = agfDatos;
        d.secuencias = secuencias;
        d.paridad = paridad;
        d.precios = precios;
        d.custodia = custodia;
        d.log = log;
        d.batch = batch;
        d.productos = productos;
        d.compuertas = compuertas;
        return RescateReinversionFabrica.crear(d);
    }
}
