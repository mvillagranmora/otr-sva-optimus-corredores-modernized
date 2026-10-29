package cl.bcs.fondomutuo.consultas.detalle;

import cl.bcs.fondomutuo.cierre.FormatoMoneda;
import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.cuotas.CuotasPort;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;

public final class ConsultarDetalleFabrica {

    private ConsultarDetalleFabrica() {
    }

    public static final class Dependencias {
        public ConfiguracionSistemaPort configuracion;
        public DiccionarioMensajesPort diccionario;
        public CalendarioPort calendario;
        public SeriesFondoPort series;
        public CuotasPort cuotasPort;
        public ParidadPort paridad;
        public PreciosTasasPort precios;
        public ConsultarDetalleDatos datos;
    }

    public static ConsultarDetallePort crear(Dependencias d) {
        CalcularMontoNumeroCuota cuotas = new CalcularMontoNumeroCuota(d.configuracion, d.series, d.cuotasPort, d.calendario, d.precios, d.paridad);
        return new ConsultarDetalleService(d.diccionario, d.datos, cuotas, d.configuracion, new FormatoMoneda(d.calendario));
    }
}
