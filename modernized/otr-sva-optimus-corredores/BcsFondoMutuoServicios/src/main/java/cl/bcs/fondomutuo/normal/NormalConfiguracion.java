package cl.bcs.fondomutuo.normal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.bcs.fondomutuo.agf.AgfDatos;
import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.comisiones.ComisionesPort;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos;
import cl.bcs.fondomutuo.cuotas.CuotasPort;
import cl.bcs.fondomutuo.custodia.FlujoCustodiaFmDatos;
import cl.bcs.fondomutuo.fechas.FechasPort;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.patrimonio.PatrimonioEmisorPort;
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

/**
 * Spring wiring of {@link NormalPort}. F2 W1: {@code ProcesarIngresoEgresoTesoreria} is the in-process port of {@code tesoreria.TesoreriaEnProcesoConfiguration}
 * (Tesoreria library, same transaction; gate G1 open by configuration). {@code ProcesarIngresoOperacionSpot} still defaults to "not served": the gate G2
 * (closed by default) stops the request before it is reached, and a gate opened by configuration without F2 code answers 503 too.
 */
@Configuration
public class NormalConfiguracion {

    @Bean
    @ConditionalOnMissingBean(ProcesosExternos.SpotPort.class)
    public ProcesosExternos.SpotPort spotPortF2() {
        return e -> {
            throw new ServicioNoServidoException("G2", "ProcesarIngresoOperacionSpot is ported in F2");
        };
    }

    @Bean
    public NormalPort normalPort(RevisarRequisitosBasicosPort basicos, ConfiguracionSistemaPort configuracion, EmpresaPort empresas,
            DiccionarioMensajesPort diccionario, CalendarioPort calendario, SeriesFondoPort series, RequisitosDatos requisitosDatos,
            ComisionNnDatos comisionNnDatos, NormalDatos normalDatos, BloqueoSolicitudPort bloqueo, PerfilFondoMutuoPort perfilPort, FechasPort fechasPort,
            CuotasPort cuotasPort, ComisionesPort comisionesPort, PatrimonioEmisorPort patrimonioPort, FlujoCustodiaFmDatos flujoCustodiaDatos,
            AgfDatos agfDatos, SecuenciaPort secuencias, ParidadPort paridad, PreciosTasasPort precios, CustodiaPort custodia, FlujoCajaPort flujoCaja,
            LogPort log, BatchPort batch, NotificacionOwpPort owp, ProductosPortafolioPort productos, ProcesosExternos.TesoreriaPort tesoreria,
            ProcesosExternos.SpotPort spot, Compuertas compuertas, java.util.List<CustodiaPort> custodias) {
        NormalFabrica.Dependencias d = new NormalFabrica.Dependencias();
        d.basicos = basicos;
        d.configuracion = configuracion;
        d.empresas = empresas;
        d.diccionario = diccionario;
        d.calendario = calendario;
        d.series = series;
        d.requisitosDatos = requisitosDatos;
        d.comisionNnDatos = comisionNnDatos;
        d.normalDatos = normalDatos;
        d.bloqueo = bloqueo;
        d.perfilPort = perfilPort;
        d.fechasPort = fechasPort;
        d.cuotasPort = cuotasPort;
        d.comisionesPort = comisionesPort;
        d.patrimonioPort = patrimonioPort;
        d.flujoCustodiaDatos = flujoCustodiaDatos;
        d.agfDatos = agfDatos;
        d.secuencias = secuencias;
        d.paridad = paridad;
        d.precios = precios;
        d.custodia = custodia;
        d.custodiaComisiones = cl.bcs.fondomutuo.custodia.CustodiaParaComisiones.elegir(custodia, custodias, compuertas.comisionesEnProceso());   // H3
        d.flujoCaja = flujoCaja;
        d.log = log;
        d.batch = batch;
        d.owp = owp;
        d.productos = productos;
        d.tesoreria = tesoreria;
        d.spot = spot;
        d.compuertas = compuertas;
        return NormalFabrica.crear(d);
    }
}
