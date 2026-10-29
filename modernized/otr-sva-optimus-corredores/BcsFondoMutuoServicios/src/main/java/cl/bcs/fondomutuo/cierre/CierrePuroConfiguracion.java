package cl.bcs.fondomutuo.cierre;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.bcs.fondomutuo.comisiones.CalcularComisiones;
import cl.bcs.fondomutuo.comisiones.ComisionesPort;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.custodia.CustodiaParaComisiones;
import cl.bcs.fondomutuo.cuotas.CuotasPort;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.fechas.FechasPort;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.perfil.ObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.perfil.PerfilFondoMutuoPort;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;

/**
 * Wiring of the pure closure of step 3A (part 1): the four plain-Java classes are created here, with their ports, as singletons - they hold no request
 * state (the legacy kept per-call state in instance fields; here it is local to each {@code logica} call). Spring lives only in this class and in the
 * port adapters; the domain classes know nothing of it. The step-4 service ({@code CrearSolicitudAporteRescateNormal}) injects these beans.
 */
@Configuration
public class CierrePuroConfiguracion {

    @Bean
    public CalcularFechaSolicitudAporteRescate calcularFechaSolicitudAporteRescate(ConfiguracionSistemaPort configuracion, EmpresaPort empresas,
            SeriesFondoPort series, FechasPort fechas, CalendarioPort calendario) {
        return new CalcularFechaSolicitudAporteRescate(configuracion, empresas, series, fechas, calendario);
    }

    @Bean
    public CalcularMontoNumeroCuota calcularMontoNumeroCuota(ConfiguracionSistemaPort configuracion, SeriesFondoPort series, CuotasPort cuotas,
            CalendarioPort calendario, PreciosTasasPort precios, ParidadPort paridad) {
        return new CalcularMontoNumeroCuota(configuracion, series, cuotas, calendario, precios, paridad);
    }

    @Bean
    public CalcularComisiones calcularComisiones(ConfiguracionSistemaPort configuracion, EmpresaPort empresas, SeriesFondoPort series,
            ComisionesPort comisiones, CustodiaPort custodia, List<CustodiaPort> custodias, Compuertas compuertas, PreciosTasasPort precios, ParidadPort paridad) {
        // H3 (review 2026-10-28): the commissions keep the HTTP port unless fondomutuo.custodia.comisiones-en-proceso=true, even with the in-process @Primary
        return new CalcularComisiones(configuracion, empresas, series, comisiones, CustodiaParaComisiones.elegir(custodia, custodias, compuertas.comisionesEnProceso()),
                precios, paridad);
    }

    @Bean
    public ObtenerPerfilFondoMutuo obtenerPerfilFondoMutuo(PerfilFondoMutuoPort perfiles) {
        return new ObtenerPerfilFondoMutuo(perfiles);
    }
}
