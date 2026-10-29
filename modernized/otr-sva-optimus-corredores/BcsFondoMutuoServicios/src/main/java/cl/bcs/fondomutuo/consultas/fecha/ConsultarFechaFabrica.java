package cl.bcs.fondomutuo.consultas.fecha;

import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.fechas.FechasPort;
import cl.bcs.fondomutuo.perfil.ObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.perfil.PerfilFondoMutuoPort;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;
import cl.bcs.fondomutuo.port.out.kit.RevisarRequisitosBasicosPort;

/** Builds the service from its ports the way {@code RescateReinversionFabrica} does. */
public final class ConsultarFechaFabrica {

    private ConsultarFechaFabrica() {
    }

    public static final class Dependencias {
        public ConfiguracionSistemaPort configuracion;
        public DiccionarioMensajesPort diccionario;
        public RevisarRequisitosBasicosPort basicos;
        public CalendarioPort calendario;
        public EmpresaPort empresas;
        public SeriesFondoPort series;
        public FechasPort fechasPort;
        public PerfilFondoMutuoPort perfilPort;
        public ConsultarFechaDatos datos;
    }

    public static ConsultarFechaPort crear(Dependencias d) {
        CalcularFechaSolicitudAporteRescate fechas = new CalcularFechaSolicitudAporteRescate(d.configuracion, d.empresas, d.series, d.fechasPort, d.calendario);
        ObtenerPerfilFondoMutuo perfil = new ObtenerPerfilFondoMutuo(d.perfilPort);
        return new ConsultarFechaService(d.basicos, d.empresas, perfil, d.datos, fechas, d.diccionario);
    }
}
