package cl.bcs.fondomutuo.consultas.custodia;

import cl.bcs.fondomutuo.cierre.FormatoMoneda;
import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.cuotas.CuotasPort;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;

public final class ConsultarCustodiaPortafolioFabrica {

    private ConsultarCustodiaPortafolioFabrica() {
    }

    public static final class Dependencias {
        public ConfiguracionSistemaPort configuracion;
        public CalendarioPort calendario;
        public SeriesFondoPort series;
        public CuotasPort cuotasPort;
        public ParidadPort paridad;
        public PreciosTasasPort precios;
        public EmpresaPort empresas;
        public ConsultarCustodiaPortafolioCustodiaPort custodia;
        public ConsultarCustodiaPortafolioDatos datos;
    }

    public static ConsultarCustodiaPortafolioPort crear(Dependencias d) {
        CalcularMontoNumeroCuota cuotas = new CalcularMontoNumeroCuota(d.configuracion, d.series, d.cuotasPort, d.calendario, d.precios, d.paridad);
        return new ConsultarCustodiaPortafolioService(d.configuracion, d.datos, d.custodia, cuotas, new ConsultarCustodiaPortafolioDiaSiguiente(d.calendario), d.empresas,
                d.paridad, new FormatoMoneda(d.calendario));
    }
}
