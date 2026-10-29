package cl.bcs.fondomutuo.valorcuota;

import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.fechas.ObtenerDiaAnterior;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.port.out.kit.RevisarRequisitosBasicosPort;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

public final class CargarValorCuotaFabrica {

    private CargarValorCuotaFabrica() {
    }

    public static final class Dependencias {
        public ConfiguracionSistemaPort configuracion;
        public DiccionarioMensajesPort diccionario;
        public RevisarRequisitosBasicosPort basicos;
        public CalendarioPort calendario;
        public SeriesFondoPort series;
        public CargarValorCuotaDatos datos;
        public CargarValorCuotaEscritura escritura;
        public SecuenciaPort secuencias;
        public LogPort log;
    }

    public static CargarValorCuotaPort crear(Dependencias d) {
        return new CargarValorCuotaService(d.basicos, d.diccionario, d.configuracion, d.series, d.datos, d.escritura, new ObtenerDiaAnterior(d.calendario), d.secuencias, d.log);
    }
}
